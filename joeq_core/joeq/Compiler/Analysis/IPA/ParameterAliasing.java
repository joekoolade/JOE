package joeq.Compiler.Analysis.IPA;

import java.util.Iterator;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ParamNode;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

class ParameterAliasing {
    /**
     * Finds parameter aliases under different constexts.
     * */
    public static class ParamAliasFinder extends IPSSABuilder.Application {
        PAResults _paResults            = null;
        PA _r                           = null;
        private boolean _verbose        = false;
        private boolean _RECURSIVE      = true;
        private int MAX_CONTEXT_PRINT   = 1;
        private boolean _CONSTRUCTORS   = false;
        private int _aliasedCalls       = 0;
        
        BDDDomain Z2 = null;
         
        public ParamAliasFinder() {
            super(null, null, null);
        }
        public ParamAliasFinder(IPSSABuilder builder, String name, String[] args) {
            super(builder, name, args);
        }
        
        protected void initialize() {
            _CONSTRUCTORS = !System.getProperty("paf.constructors", "yes").equals("no");
            _RECURSIVE    = !System.getProperty("paf.recursive", "yes").equals("no");
            _verbose      = !System.getProperty("paf.verbose", "yes").equals("no");
            
            Z2 = _r.makeDomain("Z2", _r.Z.varNum());
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
            //if(m.toString().startsWith("java.")) return;
            if(_verbose) {
                System.out.println("Processing method " + m.toString());
            }
            MethodSummary ms = MethodSummary.getSummary(m);
            if(ms == null) return;
            if(ms.getNumOfParams() < 2) return;
            if(!_CONSTRUCTORS && m instanceof jq_Initializer) {
                return;
            }
            
            _paResults = getBuilder().getPAResults();
            _r = _paResults.getPAResults();
 
            // get formal arguments for the method
            BDD methodBDD = _r.M.ithVar(_paResults.getMethodIndex(m));  // 
            BDD params    = _r.formal.relprod(methodBDD, _r.Mset);      // V1xZ
            //System.out.println("params: " + params.toStringWithDomains());
            Assert._assert(_r.H1cset != null);
            Assert._assert(_r.H1.set() != null);
            Assert._assert(_r.Z.set() != null);
            TypedBDD contexts =                                         // V1c
                (TypedBDD)params.relprod ( _r.vP, _r.V1.set().and(_r.H1cset).andWith(_r.H1.set()).andWith(_r.Z.set()) );
            //System.out.println("contexts: \n" + contexts.toStringWithDomains());
            //TypedBDD pointsTo = (TypedBDD)params.relprod(r.vP, r.V1cH1cset);
            //System.out.println("pointsTo: \n" + paResults.toString(pointsTo, -1));
            
            /** Iterate through all the relevant contexts for this method */            
            int i = 0;
            ModifiableBoolean printedInfo = new ModifiableBoolean(false);
            long contextSize = (long)contexts.satCount(_r.V1cset);
            boolean foundAliasing = false;
            for ( Iterator contextIter = contexts.iterator(); contextIter.hasNext() && i < MAX_CONTEXT_PRINT; i++ ) {
                // for this particular context #
                TypedBDD context = (TypedBDD)contextIter.next();
                //System.out.println("context: \n" + context.toStringWithDomains());

                Assert._assert(_r.vPfilter != null);
                TypedBDD pointsTo  = (TypedBDD)_r.vP.and(_r.vPfilter.id());   // restrict by the type filter
                TypedBDD t2 = (TypedBDD)params.relprod(pointsTo, _r.V1.set().and(_r.V1cset));
                pointsTo.free();
                pointsTo = t2;
                //t = (TypedBDD)t2.exist(_r.Z.set());
                
                //System.out.println("pointsTo 1: " + pointsTo.toStringWithDomains());
                if(_RECURSIVE) {
                    pointsTo = (TypedBDD) calculateHeapConnectivity(pointsTo);
                }
                //System.out.println("pointsTo 2: " + pointsTo.toStringWithDomains());

                //System.out.println("t: " + t.toStringWithDomains());

                //t: H1xH1cxZ
                foundAliasing |= processContext(m, ms, pointsTo, context, printedInfo);
            }
            if(foundAliasing) {
                if ( contextSize > MAX_CONTEXT_PRINT ) {
                    System.out.println("\t\t(A total of " + contextSize + " contexts) ");  
                }
                _aliasedCalls++;
            }
        }        
        
        public BDD calculateHeapConnectivity(BDD h1) {
            BDD result = _r.bdd.zero();
            result.orWith(h1.id());
            BDD h1h2 = _r.hP.exist(_r.Fset);
            for (;;) {
                BDD b = h1.relprod(h1h2, _r.H1set);
                b.replaceWith(_r.H2toH1);
                b.applyWith(result.id(), BDDFactory.diff);
                result.orWith(b.id());
                if (b.isZero()) break;
                h1 = b;
                //++heapConnectivitySteps;
            }
            h1h2.free();
            
            return result;
        }
        
        /**
         *  Process context #i in the set of contexts.
         * */
        boolean processContext(jq_Method m, MethodSummary ms, BDD t, TypedBDD context, ModifiableBoolean printedInfo){
            boolean result = false;  

            TypedBDD pointsTo = (TypedBDD)context.relprod(t, (TypedBDD) _r.V1cset.and(_r.H1cset));   // H1xZ
            //System.out.println("pointsTo: " + pointsTo.toStringWithDomains());
            t.free();
            
            /*    
            t = (TypedBDD)pointsTo.exist(_r.Z.set());
            //System.out.println(t.satCount() + ", " + pointsTo.satCount());
            int pointsToSize = (int)pointsTo.satCount(_r.H1.set().and(_r.Zset));
            int projSize     = (int)t.satCount( _r.H1.set() );
            if(projSize < pointsToSize) {
                if(!printedInfo.getValue()) {
                    //printMethodInfo(m, ms);
                    printedInfo.setValue(true);
                }
                ProgramLocation loc = new ProgramLocation.BCProgramLocation(m, 0);
                
                System.out.println("\tPotential aliasing in context calling " + 
                        m.getDeclaringClass().toString() + "." + m.getName().toString() + "(" + loc.getSourceFile() + ":" + loc.getLineNumber() + ")");
                //System.out.println(pointsTo.toStringWithDomains() + ", " + t.toStringWithDomains());
                result = true;
                //b.applyWith(result.id(), BDDFactory.diff);
            }
            t.free();
            */
            BDDDomain Z1 = _r.Z;
            
            TypedBDD pointsTo2 = (TypedBDD) pointsTo.replace(_r.bdd.makePair(Z1, Z2)); 
            
            BDD notEq = Z1.buildEquals(Z2).not();
            TypedBDD pairs = (TypedBDD)pointsTo.and(pointsTo2).and(notEq);
            
            System.out.println("pairs: " + pairs.toStringWithDomains());
            
            return result;
        }
        
        void printMethodInfo(jq_Method m, MethodSummary ms) {
            if(_verbose == false) {
                System.out.println("Processing method " + m + ":\t[" + ms.getNumOfParams() + "]");
            }
            
            for(int i = 0; i < ms.getNumOfParams(); i++) {
                ParamNode paramNode = ms.getParamNode(i);
                System.out.print("\t\t");
                System.out.println("Param: " + paramNode == null ? "<null>" : paramNode.toString_long());
            }
            System.out.print("\n");
        }
        public void run() {
            for(Iterator iter = getBuilder().getCallGraph().getAllMethods().iterator(); iter.hasNext();) {
                jq_Method m = (jq_Method)iter.next();
            
                visitMethod(m);
            }
            if(_aliasedCalls > 0) {
                System.out.println("A total of " + _aliasedCalls + " aliased calls");
            }
        }
    }
}
