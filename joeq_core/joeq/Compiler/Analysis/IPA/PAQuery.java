// PAQuery.java, created Oct 21, 2003 12:56:45 AM by livshits
// Copyright (C) 2003 Vladimir Livshits
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.util.Iterator;
import java.io.PrintStream;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ParamNode;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

/**
 * A query on top of PAResults. Will probably need to move nested classes 
 * to other places later.  
 * @see PAQuery.ParamAliasFinder
 * @see PAQuery.ConstParameterFinder
 * 
 * @author Vladimir Livshits
 * @version $Id: PAQuery.java,v 1.19 2004/10/16 04:13:04 joewhaley Exp $
 * */
public class PAQuery {
    /**
     * Finds parameter aliases under different constexts.
     * */
    public static class ParamAliasFinder extends IPSSABuilder.Application {
        PAResults _paResults = null;
        PA _r = null;
         
        public ParamAliasFinder() {
            super(null, null, null);
        }
        public ParamAliasFinder(IPSSABuilder builder, String name, String[] args) {
            super(builder, name, args);
        }
    
        protected void parseParams(String[] args) {}
        
        class ModifiableBoolean {
            boolean _value;
            
            ModifiableBoolean(boolean value){
                this._value = value;
            }
            boolean getValue() {return _value;}
            void setValue(boolean value) {this._value = value;}
        }
        
        void visitMethod(jq_Method m){            
            //if(getBuilder().skipMethod(m)) return;
            
            MethodSummary ms = MethodSummary.getSummary(m);
            if(ms == null) return;
            if(ms.getNumOfParams() < 2) return;
            
            _paResults = getBuilder().getPAResults();
            _r = _paResults.getPAResults();
 
            // get formal arguments for the method
            BDD methodBDD = _r.M.ithVar(_paResults.getMethodIndex(m));
            BDD params = _r.formal.relprod(methodBDD, _r.Mset);
            //System.out.println("params: " + params.toStringWithDomains());
            TypedBDD contexts = (TypedBDD)params.relprod(_r.vP, 
                _r.V1.set().andWith(_r.H1cset).andWith(_r.H1.set()).andWith(_r.Z.set()) );
            //System.out.println("contexts: \n" + paResults.toString(contexts, -1));
            //TypedBDD pointsTo = (TypedBDD)params.relprod(r.vP, r.V1cH1cset);
            //System.out.println("pointsTo: \n" + paResults.toString(pointsTo, -1));
            int i = 0;
            ModifiableBoolean printedInfo = new ModifiableBoolean(false);
            long contextSize = (long)contexts.satCount(_r.V1cset);
            for(Iterator contextIter = contexts.iterator(); contextIter.hasNext(); i++) {
                TypedBDD context = (TypedBDD)contextIter.next();

                processContext(m, ms, params, context, contextSize, printedInfo, i);
            }
        }
        
        void processContext(jq_Method m, MethodSummary ms, BDD params, TypedBDD context, long contextSize, ModifiableBoolean printedInfo, int i){
            //System.out.println("context #" + i + ": " + context.toStringWithDomains());
                
            Assert._assert(_r.vPfilter != null);
            TypedBDD t = (TypedBDD)_r.vP.and(_r.vPfilter.id());   // restrict by the type filter
            TypedBDD t2 = (TypedBDD)params.relprod(t, _r.V1.set());
            t.free();
            t = t2;
                
            //TypedBDD t = (TypedBDD)params.relprod(r.vP, r.V1.set());
            TypedBDD pointsTo = (TypedBDD)context.relprod(t, _r.V1cset.andWith(_r.H1cset));                
            t.free();
                
            t = (TypedBDD)pointsTo.exist(_r.Z.set());
            //System.out.println(t.satCount() + ", " + pointsTo.satCount());
            int pointsToSize = (int)pointsTo.satCount(_r.H1.set().and(_r.Zset));
            int projSize     = (int)t.satCount( _r.H1.set() ); 
            if(projSize < pointsToSize) {
                if(!printedInfo.getValue()) {
                    printMethodInfo(m, ms);
                    printedInfo.setValue(true);
                }                
                ProgramLocation loc = new ProgramLocation.BCProgramLocation(m, 0);
                System.out.println("\tPotential aliasing in context #" + i + " calling " + m.toString() + " at " + 
                    loc.getSourceFile() + ":" + loc.getLineNumber());
                if(contextSize > 5) {
                    System.out.println("\t\t(A total of " + contextSize + " contexts) \n");  
                    return;
                }
            }
            t.free();
        }
        
        void printMethodInfo(jq_Method m, MethodSummary ms) {
            System.out.println("Processing method " + m + ":\t[" + ms.getNumOfParams() + "]");
            for(int i = 0; i < ms.getNumOfParams(); i++) {
                ParamNode paramNode = ms.getParamNode(i);
                System.out.print("\t\t");
                System.out.println(paramNode == null ? "<null>" : paramNode.toString_long());
            }
            System.out.print("\n");
        }
        public void run() {
            for(Iterator iter = getBuilder().getCallGraph().getAllMethods().iterator(); iter.hasNext();) {
                jq_Method m = (jq_Method)iter.next();
            
                visitMethod(m);
            }
        }
    }
    
    /**
     * Application for finding and printing const-parameters.
     * */
    public static class ConstParameterFinder extends IPSSABuilder.Application {
        static final String NON_CONST_QUALIFIER = "non_const ";
        static final String CONST_QUALIFIER     = "const ";
        PAResults _paResults;
        PA _r;

        public ConstParameterFinder() {
            this(null, "ConstParameterFinder", null);
        }
        public ConstParameterFinder(IPSSABuilder builder, String name, String[] args) {
            super(builder, name, args);
        }

        void visitMethod(jq_Method m){            
            if(getBuilder().skipMethod(m)) return;
    
            MethodSummary ms = MethodSummary.getSummary(m);
            if(ms == null) return;
    
            _paResults = getBuilder().getPAResults();
            _r = _paResults.getPAResults();
            
            TypedBDD params = (TypedBDD)_r.formal.relprod(_r.M.ithVar(_paResults.getMethodIndex(m)), _r.Mset);
            
            System.out.print(m.toString() + "( ");
            for(Iterator paramIter = params.iterator(); paramIter.hasNext();) {
                TypedBDD param = (TypedBDD)((TypedBDD)paramIter.next()).exist(_r.Zset);                
                
                boolean isConst = isConst(param, m, true);
                
                System.out.print(isConst ? CONST_QUALIFIER : NON_CONST_QUALIFIER);
                System.out.print(param.toStringWithDomains() /*_paResults.toString(param, -1) */ + " ");
            }
            System.out.print(") \n");
        }

        /**
         * Check whether parameter param : V1 can of method m can be declared a const parameter.
         * recursive determines whether we consider callees to look for modifications.  
         * */
        public boolean isConst(TypedBDD param, jq_Method m, boolean recursive) {
            // pointsTo is what this particular parameter may point to
            BDD pointsTo = _r.vP.restrict(param);                               // H1xH1cxF
            BDD methodBDD = _r.M.ithVar(_paResults.getMethodIndex(m));          // M
            
            BDD mods = null;
            if (!recursive) {               
                // these are the modified heap locations
                mods = _r.S.relprod(param, _r.V1set);                           // H1xH1cxFxV2xV1cxV2c
            } else {
                BDD method_plus_context0 = methodBDD.andWith(_r.V1cset);
                BDD reachableVars = _paResults.getReachableVars(method_plus_context0); // V1xV1c
                reachableVars = reachableVars.exist(_r.V1cset);
                reachableVars.or(param);
                System.err.println("reachableVars: " + _paResults.toString((TypedBDD)reachableVars, -1));
                
                BDD stores = _r.S.relprod(reachableVars, _r.V2set);             // V1xV1c x V1xV1cxFxV2xV2c = V1xV1cxF
                mods = stores.relprod(_r.vP, _r.V1set);                         // V1xV1cxF x V1xV1cxH1xH1c = H1xH1cxF
            }
            
            boolean result = mods.isZero();
            mods.free();
            
            return result;
        }

        protected void parseParams(String[] args) {}

        public void run() {
            for(Iterator iter = getBuilder().getCallGraph().getAllMethods().iterator(); iter.hasNext();) {
                jq_Method m = (jq_Method)iter.next();

                visitMethod(m);
            }
        }
    }
    
    /**
     * Produces statistics on how many locations are references by a given load 
     * or store within a given context.
     * */
    public static class HeapReferenceStat extends IPSSABuilder.Application {
        private int _stores = 0;
        private int _loads  = 0;
        private int _mods   = 0;
        private int _refs   = 0;
        
        public HeapReferenceStat() {
            super(null, null, null);
        }
 
        protected void parseParams(String[] args) {}

        public void run() {
           for(Iterator iter = getBuilder().getCallGraph().getAllMethods().iterator(); iter.hasNext();) {
               jq_Method m = (jq_Method)iter.next();
    
               visitMethod(m);
           }

           printStat(System.out);
       }

        void printStat(PrintStream out) {
            out.println("Statistics:");
            out.println("\tLoads:\t" + _loads);                        
            out.println("\tStores:\t" + _stores);

            out.println("\tAvg mod:\t" + _mods/_stores);
            out.println("\tAvg ref:\t" + _refs/_loads);
        }

        private void visitMethod(jq_Method m) {
            if(getBuilder().skipMethod(m)) return;

            MethodSummary ms = MethodSummary.getSummary(m);
            if(ms == null) return;            
            
            for(QuadIterator iter = new QuadIterator(CodeCache.getCode(m)); iter.hasNext(); ) {
                Quad quad = iter.nextQuad();
                
                /*  // doesn't compile.
                if(IPSSABuilder.isLoad(quad)) {
                    Set refs = this.getBuilder().getPAResults().ref(m, iter.getCurrentBasicBlock(), quad);
                    System.out.println("Quad: " + quad + refs);
                    _loads++;
                    _refs += refs.size();
                }else
                if(IPSSABuilder.isStore(quad)) {
                    Set mods = this.getBuilder().getPAResults().mod(m, iter.getCurrentBasicBlock(), quad);
                    System.out.println("Quad: " + quad + mods);
                    _stores++;
                    _mods += mods.size();
                }
                */
            }                                               
        }    
    }
}