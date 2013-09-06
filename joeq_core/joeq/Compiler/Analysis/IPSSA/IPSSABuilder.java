package joeq.Compiler.Analysis.IPSSA;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.PAResults;
import joeq.Compiler.Analysis.IPA.PointerAnalysisResults;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Analysis.IPSSA.SSAProcInfo.Helper;
import joeq.Compiler.Analysis.IPSSA.SSAProcInfo.Query;
import joeq.Compiler.Analysis.IPSSA.SSAProcInfo.SSABindingAnnote;
import joeq.Compiler.Analysis.IPSSA.SSAValue.ActualOut;
import joeq.Compiler.Analysis.IPSSA.Utils.SSAGraphPrinter;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.HostedVM;
import joeq.Util.Templates.ListIterator;
import jwutil.collections.AppendIterator;
import jwutil.graphs.SCCTopSortedGraph;
import jwutil.graphs.SCComponent;
import jwutil.graphs.Traversals;
import jwutil.util.Assert;

/**
 * This is where the main action pertaining to IPSSA construction happens. 
 * A subclass is SSABuilder, which is responsible for intraprocedural IPSSA
 * construction.
 * 
 * @author V.Benjamin Livshits
 * @see IPSSABuilder.SSABuilder
 * @version $Id: IPSSABuilder.java,v 1.24 2005/05/28 11:14:47 joewhaley Exp $
 * */
public class IPSSABuilder implements Runnable {
    protected int                                  _verbosity;
    private static HashMap                         _builderMap = new HashMap();
    private PointerAnalysisResults              _ptr = null;
    private IPSSABuilder.ApplicationLaunchingPad _appPad = null; 
    private Collection                          _classes = null;
    
    boolean PRINT_CFG         = !System.getProperty("ipssa.print_cfg", "no").equals("no");
    boolean PRINT_SSA_GRAPH = !System.getProperty("ipssa.print_ssa", "no").equals("no");
    boolean RUN_BUILDER     = !System.getProperty("ipssa.run_builder", "no").equals("no");
    boolean RUN_APPS        = !System.getProperty("ipssa.run_apps", "no").equals("no");       
        

    public IPSSABuilder(Collection classes, int verbosity){
        //System.err.println("Creating " + this.getClass().toString());
        CodeCache.AlwaysMap = true;
        this._verbosity     = verbosity;
        this._classes = classes;
        // get pointer analysis results            
        try {
            String resdir = System.getProperty("pa.resultdir");
            String[] args = null;
            if(resdir != null) {
                args = new String[1];
                args[0] = resdir;
                System.out.println("Reading pointer analysis results from directory " + resdir);
            }
            _ptr = PAResults.loadResults(args, null);
        } catch (IOException e) {
            System.err.println("Caught an exception: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        if(RUN_APPS) {
            _appPad = new IPSSABuilder.ApplicationLaunchingPad(this, true);
        }
    }
    
    public PAResults getPAResults() {
        return (PAResults)_ptr;
    }
    
    public CallGraph getCallGraph() {
        return _ptr.getCallGraph();
    }
    
    /**
     * Handle an SCC in the call graph. Nodes of the SCC are jq_Method's.
     * */
    protected void processSCC(SCComponent c) {
        Set nodes = c.nodeSet();
        for(Iterator iter = nodes.iterator(); iter.hasNext();) {
            jq_Method method = (jq_Method)iter.next();
            if(skipMethod(method)) continue;
            
            SSABuilder builder = new SSABuilder(method, _ptr, _verbosity);
            Assert._assert(_builderMap.get(method ) == null);
            _builderMap.put(method, builder);       

            // do the first two stages now          
            builder.run(0);
            builder.run(1);
        }
        
        for(Iterator iter = nodes.iterator(); iter.hasNext();) {
            jq_Method method = (jq_Method)iter.next();
            if(skipMethod((method))) continue;
            
            SSABuilder builder = (SSABuilder)_builderMap.get(method);
            Assert._assert(builder != null);
            
            builder.run(2);
            //builder.run(3);
        }        
    }

    /**
     * Do the whole analysis.
     * */
    public void run() {
        if(RUN_BUILDER){
            if(_classes.size() == 1) {
                System.out.println("Analyzing one class...");    
            }else if(_classes.size() > 1) {
                System.out.println("Analyzing these " + _classes.size() + " classes...");
            }
    
            Collection rootMethods = new LinkedList();
            Iterator i = _classes.iterator();
            while (i.hasNext()) {
                jq_Class c = (jq_Class)i.next();
                rootMethods.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            }
    
            System.out.println("Using " + rootMethods.size() + " root(s)...");
                    
            CallGraph cg = this.getCallGraph();
            for(Iterator iter = cg.getAllMethods().iterator(); iter.hasNext(); ) {
                jq_Method method = (jq_Method)iter.next();
                if(skipMethod(method)) continue;
                
                System.err.println("Allowing \t" + method);
            }
            SCCTopSortedGraph sccGraph = SCCTopSortedGraph.topSort(SCComponent.buildSCC(rootMethods, /*new ReverseNavigator*/(cg.getNavigator())));
            System.err.println("Found " + sccGraph.list().size() + " components");
            
            // We want bottom-up order here...
            //for (Iterator graphIter = sccGraph.getFirst().listTopSort().iterator(); graphIter.hasNext(); ) {
            for(Iterator graphIter = Traversals.postOrder(sccGraph.getNavigator(), sccGraph.getFirst()).iterator(); graphIter.hasNext();){            
                SCComponent d = (SCComponent) graphIter.next();
                //System.err.println("Processing SCC # " + d.getId() + " with nodes " + d.nodeSet().toString());
                this.processSCC(d);
            }
        }
        
        if(RUN_APPS) {
            _appPad.run();
        }        
    }
    
    /**
     * A method filter.
     * */
    public boolean skipMethod(jq_Method method) {
        jq_Class k = method.getDeclaringClass();
        if(!_classes.contains(k)) {
            // unknown, potentially library class -- skip it
            if(_verbosity > 3) {
                System.err.println("Skipping " +  method);
            }
            return true;
        }
        return false;
    }

    /** The return result may be NULL */
    public static SSABuilder getBuilder(jq_Method m){
        return (SSABuilder)_builderMap.get(m);
    }
    
    
    /**
     * SSABuilder takes care of a single method.
     * */
    class SSABuilder {
        protected int                      _verbosity;
        protected jq_Method             _method;
        protected ControlFlowGraph         _cfg;
        protected SSAProcInfo.Query     _q;
        private PointerAnalysisResults     _ptr;
        SSABuilder(jq_Method method, PointerAnalysisResults ptr, int verbosity){
            this._method     = method;
            this._cfg         = CodeCache.getCode(_method);
            this._verbosity = verbosity;
            this._q         = null; 
            this._ptr        = ptr;
        }        

        public ControlFlowGraph getCFG() { return _cfg; }
        public SSAProcInfo.Query getQuery() { return _q; }
        

        //////////////////////////////////////////////////////////////////////////////////////////////////
        /***************************************** Auxilary routines ************************************/
        //////////////////////////////////////////////////////////////////////////////////////////////////    
        protected int addBinding(Quad quad, SSALocation loc, SSAValue value){
            if(_ptr.hasAliases(_method, loc)){
                // add the binding to potential aliased locations
                int i = 0;
                for(Iterator iter = _ptr.getAliases(_method, loc).iterator(); iter.hasNext();){
                    ContextSet.ContextLocationPair clPair = (ContextSet.ContextLocationPair)iter.next();
                        
                    // process aliasedLocation
                    i += addBinding(quad, clPair.getLocation(), value, clPair.getContext());                                    
                }
                return i;
            }else{
                addBinding(quad, loc, value, null);
                return 1;
            }                    
        }
            
        /**
         * This is used by addBinding(Quad quad, SSALocation loc, SSAValue value) and
         * should never be called directly.
         * */
        private int addBinding(Quad quad, SSALocation loc, SSAValue value, ContextSet context){
            // initialize the location
            if(quad != _q.getFirstQuad()){
                initializeLocation(loc);
            }
    
            SSABindingAnnote ba = (SSABindingAnnote)_q._bindingMap.get(quad);
            if(ba == null){
                ba = new SSABindingAnnote();
                _q._bindingMap.put(quad, ba);
            }
            
            int result = 0;
            if(context == null){
                ba.addBinding(loc, value, quad, _method);
                result++;
                if(quad != _q.getFirstQuad()){
                    result += markIteratedDominanceFrontier(loc, quad);                    
                }
            }else{
                SSADefinition tmpForValue = makeTemporary(value, quad, context);
                result++;
                SSADefinition lastDef = _q.getLastDefinitionFor(loc, quad, true);
                
                SSAValue.SigmaPhi sigma = new SSAValue.SigmaPhi(context, tmpForValue, lastDef);
                ba.addBinding(loc, sigma, quad, _method);
                result++;
                result += markIteratedDominanceFrontier(loc, quad);
            }
            
            return result;
        }
            
        /**
         * This is used by addBinding(...) routines and should not be called directly.
         * */
        private int initializeLocation(SSALocation loc) {            
            if(_q.getDefinitionFor(loc, _q.getFirstQuad()) == null){
                if(loc instanceof SSALocation.LocalLocation){
                    // no previous value to speak of for the locals
                    return addBinding(_q.getFirstQuad(), loc, null, null);
                }else{
                    // the RHS is always a FormalIn
                    return addBinding(_q.getFirstQuad(), loc, new SSAValue.FormalIn(), null);
                }
            }else{
                return 0;
            }                                
        }
        
        /**
         * Creates new empty definitions at the dominance frontier of quad for 
         * location loc.
         */
        private int markIteratedDominanceFrontier(SSALocation loc, Quad quad) {
            if(loc instanceof SSALocation.Unique){
                // don't create Gamma nodes for unique locations
                return 0;
            }
            int result = 0;
            HashSet set = new HashSet();
            _q.getDominatorQuery().getIteratedDominanceFrontier(quad, set);
            if(_verbosity > 2) System.err.println("There are " + set.size() + " element(s) on the frontier");
            
            for(Iterator iter = set.iterator(); iter.hasNext();){
                Quad dom = (Quad)iter.next();
                Assert._assert(dom.getOperator() instanceof Operator.Special.NOP, "" +                    "Expected the quad on the dominance frontier to be a NOP, not a " + dom);
                if(_q.getDefinitionFor(loc, dom) == null){                
                    SSAValue.Gamma gamma = new SSAValue.Gamma();
                    
                    // to be filled in later
                    result += addBinding(dom, loc, gamma, null);
                    if(_verbosity > 3) System.err.println("Created a gamma function for " + loc + " at " + dom);
                }else{
                    // the gamma is already there, do nothing
                }
            }
            
            return result;        
        }
            
        /**
         * Creates a temporary definition at quad with the RHS value in 
         * the given context.
         * */
        private SSADefinition makeTemporary(SSAValue value, Quad quad, ContextSet context) {
            // TODO We need to create a temporary definition at quad
            SSALocation.Temporary temp = SSALocation.Temporary.FACTORY.get();
                
            addBinding(quad, temp, value, context);
            
            SSADefinition def = _q.getDefinitionFor(temp, quad);
            Assert._assert(def != null);
            
            return def; 
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////
        /******************************************** Stages ********************************************/
        //////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * This functions runs one of the analysis stages for the given procedure. 
         * */        
        void run(int stage){
            if(stage == 0) {
                // lift the merge points
                _cfg.visitBasicBlocks(new LiftMergesVisitor());
                // create the query now after the lifting has been done
                _q = SSAProcInfo.retrieveQuery(_method);                              

                if(_verbosity>2) System.out.println("Created query: " + _q.toString());
                if(_verbosity > 0){
                    String name = _method.toString();
                    if(name.length() > 40){
                        name = name.substring(40);
                    }else{
                        name = repeat(" ", 40-name.length())+name;
                    }
                    System.out.println("============= Processing method " + name + " in IPSSABuilder =============");
                }
                return;
            }
            
            /*
             * Stages of intraprocedural processing:
             *     Stage 1     : Process all statements in turn and create slots for each modified location.
             *  Invariant 1 : All necessary assignments are created by this point and all definitions are numbered.
             *  
             *     Stage 2     : Walk over and fill in all RHSs that don't require dereferencing.
             *  Invariant 2 : All remaining RHSs that haven't been filled in require dereferencing.
             * 
             *  Stage 3     : Walk over and do all remaining pointer resolution.
             *  Invariant 3 : All RHSs are filled in.
             * */
            if(stage == 1) {
                // 1.             
                Stage1Visitor vis1 = new Stage1Visitor(_method);  
                for (QuadIterator j=new QuadIterator(_cfg, true); j.hasNext(); ) {
                    Quad quad = j.nextQuad();
                    quad.accept(vis1);
                }            
                if(_verbosity > 2){
                    System.err.println("Created a total of " + vis1.getBindingCount() + " bindings");
                }
                vis1 = null;
            }

/*
            //    2.
            Stage2Visitor vis2 = new Stage2Visitor();
            vis2.visitCFG(_cfg);
            
            //    3.            
            Stage3Visitor vis3 = new Stage3Visitor();  
            vis3.visitCFG(_cfg);
*/            
            if(stage == 2) {
                Stage2Visitor vis2 = new Stage2Visitor(_method);  
                for (QuadIterator j=new QuadIterator(_cfg, true); j.hasNext(); ) {
                    Quad quad = j.nextQuad();
                    quad.accept(vis2);
                }
            }
            
            if(stage == 3) {                            
                /** Now print the results */
                if(PRINT_CFG){
                    // print the CFG annotated with SSA information
                    _q.printDot();    
                }
                
                if(PRINT_SSA_GRAPH) {
                    try {
                        FileOutputStream file = new FileOutputStream("ssa.dot");
                        PrintStream out = new PrintStream(file);
                        SSAGraphPrinter.printAllToDot(out);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(2);
                    }                
                }
            }
        }
        
        /**
         * This pass adds dummy NOP quads at the first quad of a basic block with more than one predecessors.
         * That quad will later be used by gamma functions.
         * */
        private class LiftMergesVisitor implements BasicBlockVisitor {
            public void visitBasicBlock(BasicBlock bb) {
                if(bb.getPredecessors().size() > 1) {
                    // more than one predecessor -- add a padding NOP quad int he beginning of the block
                    Quad padding = Operator.Special.create(0, Operator.Special.NOP.INSTANCE);
                    int oldSize = bb.size();
                    // TODO: what index should we really be using here?
                    bb.addQuad(0, padding);
                    Assert._assert(oldSize + 1 == bb.size());
                }
            }
        }
        
        /** 
         * Stage 1     : Process all statements in turn and create slots for each modified location. 
         * Invariant 1 : All necessary assignments are created by this point and all definitions are numbered.
         * */
        private class Stage1Visitor extends QuadVisitor.EmptyVisitor {
            jq_Method _method;
            SSAProcInfo.Helper _h;
            SSAProcInfo.Query  _q;
            private int        _bindings;
            
            Stage1Visitor(jq_Method method){
                this._method   = method;
                this._h        = SSAProcInfo.retrieveHelper(_method);
                this._q        = SSAProcInfo.retrieveQuery(_method);
                this._bindings = 0;
            }
            
            int getBindingCount(){
                return _bindings;
            }        
            
            /**************************** Begin handlers ****************************/
            /** A get static field instruction. */
            public void visitGetstatic(Quad quad) {
                processLoad(quad);
            }
            /** A get instance field instruction. */
            public void visitGetfield(Quad quad) {
                processLoad(quad);
            }
            private void processLoad(Quad quad) {
                markDestinations(quad);                
            }            
            /** A put instance field instruction. */
            public void visitPutfield(Quad quad) {
                processStore(quad);
            }
            /** A put static field instruction. */
            public void visitPutstatic(Quad quad) {
                processStore(quad);
            }
            /** A register move instruction. */
            public void visitMove(Quad quad) {
                markDestinations(quad);
            }
            /** An array load instruction. */
            public void visitALoad(Quad quad) {
                processLoad(quad);
            }
            /** An array store instruction. */
            public void visitAStore(Quad quad) {
                print(quad);
            }
            /** An quadect allocation instruction. */
            public void visitNew(Quad quad) {
                markDestinations(quad);
            }
            /** An array allocation instruction. */
            public void visitNewArray(Quad quad) {
                markDestinations(quad);
            }
            /** A return from method instruction. */
            public void visitReturn(Quad quad) {
                // TODO: make up a location for return?
                print(quad);
            }
            public void visitInvoke(Quad quad) {
                //printAlways(quad);
                processDefs(quad);    
            }
            /**************************** End of handlers ****************************/ 
            
            private void markDestinations(Quad quad) {                
                Register reg = getOnlyDefinedRegister(quad); 
                Assert._assert(reg != null);
                SSALocation.LocalLocation loc = SSALocation.LocalLocation.FACTORY.createLocalLocation(reg);

                addBinding(quad, loc, null, null);
            }
            private void processStore(Quad quad) {
                processDefs(quad);
            }
    
            private void processDefs(Quad quad) {
                QuadProgramLocation pl = new QuadProgramLocation(_method, quad);
                Assert._assert(isCall(quad) || isStore(quad));
                Set mods = _ptr.mod(pl, _q.getDominatorQuery().getBasicBlock(quad));

                // create bindingins for all modified locations
                if(mods != null && mods.size() > 0){
                    if(_verbosity > 2) System.out.print("Found " + mods.size() + " mods at " + pl.toString() + ": [ ");
                    Iterator iter = mods.iterator();
                    while(iter.hasNext()){
                        SSALocation loc = (SSALocation)iter.next();
                        if(_verbosity > 2) System.out.print(loc.toString(_ptr.getPAResults()) + " ");
                        if(isCall(quad)){
                            _bindings += addBinding(quad, loc, new SSAValue.ActualOut(), null);
                        }else
                        if(isStore(quad)){
                            _bindings += addBinding(quad, loc, null, null);
                        }else{
                            Assert._assert(false);
                        }
                    }
                    if(_verbosity > 2) System.out.println("]\n");
                }
            }

            /** Any quad */
            public void visitQuad(Quad quad) {print(quad);}
            
            protected void print(Quad quad, boolean force){
                if(!force) return;
                ProgramLocation loc = new QuadProgramLocation(_method, quad);
                String loc_str = null;
                
                try {
                    loc_str = loc.getSourceFile() + ":" + loc.getLineNumber();
                }catch(Exception e){
                    loc_str = "<unknown>";
                }
                
                System.out.println("Visited quad # " + quad.toString() + "\t\t\t at " + loc_str);
            }
            
            protected void printAlways(Quad quad){
                print(quad, true);
            }
            
            protected void print(Quad quad){
                print(quad, false);
            }
                        
            protected void warn(String s){
                System.err.println(s);
            }
        }
    
        /** 
         * Stage 2     : Walk over and fill in all RHSs that don't require dereferencing. 
         * Invariant 2 : Update RHSs referring to heap objects to refer to the right locations.
         * */
        private class Stage2Visitor extends QuadVisitor.EmptyVisitor {
            private jq_Method _method;
            private Query     _q;
            private Helper    _h;

            Stage2Visitor(jq_Method method){
                this._method   = method;
                this._h        = SSAProcInfo.retrieveHelper(_method);
                this._q        = SSAProcInfo.retrieveQuery(_method);
            }
            
            /**************************** Begin handlers ****************************/
            /** A get static field instruction. */
            public void visitGetstatic(Quad quad) {
                processLoad(quad);
            }
            /** A get instance field instruction. */
            public void visitGetfield(Quad quad) {
                processLoad(quad);
            }
            /** A put instance field instruction. */
            public void visitPutfield(Quad quad) {
                processStore(quad);
            }
            /** A put static field instruction. */
            public void visitPutstatic(Quad quad) {
                processStore(quad);
            }
            /** A register move instruction. */
            public void visitMove(Quad quad) {
                // there is only one binding at this quad
                Assert._assert(_q.getBindingCount(quad) == 1);
                SSABinding b = _q.getBindingIterator(quad).nextBinding();
                Assert._assert(b.getValue() == null);
                b.setValue(markUses(quad));
            }
            /** An array load instruction. */
            public void visitALoad(Quad quad) {
                processLoad(quad);
            }
            /** An array store instruction. */
            public void visitAStore(Quad quad) {
                processStore(quad);
            }
            /** An quadect allocation instruction. */
            public void visitNew(Quad quad) {
                 // there is only one binding at this quad
                 Assert._assert(_q.getBindingCount(quad) == 1);
                 SSABinding b =  _q.getBindingIterator(quad).nextBinding();
                 Assert._assert(b.getValue() == null);
                 b.setValue(makeAlloc(quad));
            }
            /** An array allocation instruction. */
            public void visitNewArray(Quad quad) {
                // there is only one binding at this quad
                 Assert._assert(_q.getBindingCount(quad) == 1);
                 SSABinding b = _q.getBindingIterator(quad).nextBinding();
                 Assert._assert(b.getValue() == null);
                 b.setValue(makeAlloc(quad));
            }
            /** A return from method instruction. */
            public void visitReturn(Quad quad) {
                // TODO: make up a location for return?
            }            
            public void visitInvoke(Quad quad) {
                processCall(quad);                               
            }
            /**************************** End of handlers ****************************/ 
            
            private void processStore(Quad quad) {
                // the destinations have been marked at this point
                // need to fill in the RHSs
                for(SSAIterator.BindingIterator iter = _q.getBindingIterator(quad); iter.hasNext();) {                      
                    SSABinding b = iter.nextBinding();
                    Assert._assert(b.getValue() == null);
                    b.setValue(markUses(quad));
                }
            }

            /// Fill in all the gammas
            /** A special instruction. */
            public void visitSpecial(Quad quad) {
                if(quad.getOperator() instanceof Operator.Special.NOP) {
                    SSAIterator.BindingIterator bindingIter = _q.getBindingIterator(quad);
                    while(bindingIter.hasNext()){
                        SSABinding b = bindingIter.nextBinding();
                        SSAValue value = b.getValue();
                    
                        if(value != null && value instanceof SSAValue.Gamma){
                            SSAValue.Gamma gamma = (SSAValue.Gamma)value;
                            fillInGamma(quad, gamma);
                        }
                    }
                }
            }
            
            /**
             * Fill in the gamma function with reaching definitions
             * */
            private void fillInGamma(Quad quad, SSAValue.Gamma gamma) {
                SSALocation loc = gamma.getDestination().getLocation();                
                
                BasicBlock basicBlock = _q.getDominatorQuery().getBasicBlock(quad);
                Assert._assert(basicBlock != null);
                Assert._assert(basicBlock.size() > 0);
                Assert._assert(basicBlock.getQuad(0) == quad);
                ListIterator.BasicBlock predIter = basicBlock.getPredecessors().basicBlockIterator();
                while(predIter.hasNext()){
                    BasicBlock predBlock = predIter.nextBasicBlock();
                    Quad predQuad = predBlock.isEntry() ? _q.getFirstQuad() : predBlock.getLastQuad();
                    SSADefinition predDef = _q.getLastDefinitionFor(loc, predQuad, false);
                    gamma.add(predDef, null);
                }
            }

            /**
             * This method fills in the RHS of loads.
             * */
            private void processLoad(Quad quad) {
                QuadProgramLocation pl = new QuadProgramLocation(_method, quad);
                Assert._assert(isLoad(quad));
                Set refs = _ptr.ref(pl, _q.getDominatorQuery().getBasicBlock(quad));

                SSAValue.OmegaPhi value = new SSAValue.OmegaPhi(); 

                // create bindingins for all modified locations
                if(refs != null && refs.size() > 0){
                    if(_verbosity > 2) System.out.print("Found " + refs.size() + " refs at " + pl.toString() + ": [ ");
                    Iterator iter = refs.iterator();
                    while(iter.hasNext()){
                        SSALocation loc = (SSALocation)iter.next();
                        if(_verbosity > 2) System.out.print(loc.toString(_ptr.getPAResults()) + " ");
                        // figure out the reaching definition for loc
                        initializeLocation(loc);
                        SSADefinition def = _q.getLastDefinitionFor(loc, quad, true);
                        Assert._assert(def != null);                        
                        if(_verbosity > 1) System.out.println("Using " + def + " at " + quad);
                        value.addUsedDefinition(def);
                    }
                    if(_verbosity > 2) System.out.println("]\n");
                }
                
                Assert._assert(_q.getBindingCount(quad) == 1, "Have " + _q.getBindingCount(quad) + " bindings at " + quad);
                SSABinding b = _q.getBindingIterator(quad).nextBinding();
                Assert._assert(b.getValue() == null);
                Assert._assert(b.getDestination().getLocation() instanceof SSALocation.LocalLocation);
                SSALocation.LocalLocation loc = (SSALocation.LocalLocation) b.getDestination().getLocation();
                Assert._assert(loc.getRegister() == getOnlyDefinedRegister(quad));
                b.setValue(value);
            }
            
            private void processCall(Quad quad) {
                Assert._assert(isCall(quad));
                QuadProgramLocation pl = new QuadProgramLocation(_method, quad);
                Set/*jq_Method*/ targets = _ptr.getCallTargets(pl);
                if(targets.size() == 0) {
                    System.err.println("No targets of call " + quad);
                    return;
                }
                /**
                 * Fill in the existing rho values at this call site.
                 * */
                for(SSAIterator.BindingIterator iter = _q.getBindingIterator(quad); iter.hasNext(); ) {
                    SSABinding b  = iter.nextBinding();                    
                    Assert._assert(b.getValue() instanceof SSAValue.ActualOut);
                    SSAValue.ActualOut value = (ActualOut)b.getValue();
                    SSALocation loc = b.getDestination().getLocation();                    
                
                    //System.out.print(targets.size() + " targets of " + quad + ": "); 
                    for(Iterator targetIter = targets.iterator(); targetIter.hasNext();) {
                        jq_Method method = (jq_Method)targetIter.next();
                        //System.out.print(method.toString() + " ");
                        SSABuilder calleeBuilder = getBuilder(method);
                        if(calleeBuilder == null) {
                            Assert._assert(false, "Method " + method + " hasn't been processed");
                        }
                        calleeBuilder.initializeLocation(loc);
                        SSADefinition lastDef = calleeBuilder.getQuery().getLastDefinitionFor(loc, 
                            calleeBuilder.getCFG().exit().getLastQuad(), false);
  
                        value.add(lastDef, method);
                    }
                    //System.out.print("\n");                        
                }
                
                /**
                 * Add this call site to FormalIn's at all the callees.
                 * */               
                for(Iterator targetIter = targets.iterator(); targetIter.hasNext();) {
                    jq_Method method = (jq_Method)targetIter.next();
                    if(skipMethod(method)) continue;
                    
                    SSABuilder calleeBuilder = getBuilder(method);
                    if(calleeBuilder == null) {
                        Assert._assert(false, "Method " + method + " hasn't been processed yet");
                    }
                    
                    for(Iterator iter = calleeBuilder.getQuery().getBindingIterator(calleeBuilder.getQuery().getFirstQuad()); iter.hasNext();) {
                        SSABinding b = (SSABinding)iter.next();
                        if(!(b.getValue() instanceof SSAValue.FormalIn)) continue;
                        
                        SSALocation loc = b.getDestination().getLocation();                        
                    
                        // got the iota function, fill it in
                        SSAValue.FormalIn value = (SSAValue.FormalIn)b.getValue();
                        if(!value.hasCallSite(quad)) {
                            initializeLocation(loc);
                            SSADefinition lastDef = _q.getLastDefinitionFor(loc, quad, true);
                            Assert._assert(lastDef != null);  
                            value.add(lastDef, quad);
                        } else {
                            Assert._assert(false, "Already added a definition for " + method + " to " + value + ". Recursion?");
                        }        
                    }
                }                
            }
            
            private SSAValue.Normal markUses(Quad quad) {
                SSAValue.UseCollection value = SSAValue.UseCollection.FACTORY.createUseCollection();
                ListIterator.RegisterOperand iter = quad.getUsedRegisters().registerOperandIterator();
                while(iter.hasNext()) {
                    Register reg = iter.nextRegisterOperand().getRegister();
                    SSALocation loc = SSALocation.LocalLocation.FACTORY.createLocalLocation(reg);
                    initializeLocation(loc);
                    SSADefinition  def =_q.getLastDefinitionFor(loc, quad, true);
                    Assert._assert(def != null);
                    
                    value.addUsedDefinition(def);
                }
                                
                return value;
            }
            
            private SSAValue makeAlloc(Quad quad) {
                return SSAValue.Alloc.FACTORY.createAlloc(quad);
            }
        }
    } // End of SSABuilder
    
    // ----------------------------- Auxilary procedures ----------------------------- // 
    public static boolean isLoad(Quad quad) {
        return 
            (quad.getOperator() instanceof Operator.Getfield) ||
            (quad.getOperator() instanceof Operator.Getstatic);
    }
    public static boolean isStore(Quad quad) {
        return
            (quad.getOperator() instanceof Operator.Putfield) ||
            (quad.getOperator() instanceof Operator.Putstatic);
    }
    public static boolean isCall(Quad quad) {
        return (quad.getOperator() instanceof Operator.Invoke);
    }
    private static String repeat(String string, int n) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i<n; i++) result.append(string);
        
        return result.toString();
    }
    private static Register getOnlyDefinedRegister(Quad quad) {
        joeq.Util.Templates.ListIterator.RegisterOperand iter = quad.getDefinedRegisters().registerOperandIterator();
        if(!iter.hasNext()){
            // no definition here
            return null;
        }
        Register reg = iter.nextRegisterOperand().getRegister();
        Assert._assert(!iter.hasNext(), "More than one defined register");
            
        return reg;
    }
    private static Register getOnlyUsedRegister(Quad quad) {
        joeq.Util.Templates.ListIterator.RegisterOperand iter = quad.getUsedRegisters().registerOperandIterator();
        if(!iter.hasNext()){
            // no definition here
            return null;
        }
        Register reg = iter.nextRegisterOperand().getRegister();
        Assert._assert(!iter.hasNext(), "More than one used register");
        
        return reg;
    }
    
    /**
     * This class allows to specify applications to be 
     * run after IPSSA has been constructed.
     * 
     * @see IPSSABuilder.Application
     * */
    public static class ApplicationLaunchingPad implements Runnable {
        LinkedList _applications;
        boolean _verbosity;
        private IPSSABuilder _builder;
        
        public ApplicationLaunchingPad(IPSSABuilder builder, boolean verbosity){
            _builder = builder;
            _applications = new LinkedList();
            _verbosity = verbosity;
            
            readConfig();
        }
        public ApplicationLaunchingPad(IPSSABuilder builder, Application app, boolean verbosity){
            this(builder, verbosity);

            addApplication(app);
        }
        public ApplicationLaunchingPad(IPSSABuilder builder){
            this(builder, false);
        }
        public void addApplication(Application app){
            _applications.addLast(app);            
        }
        public IPSSABuilder getBuilder() {
            return _builder;
        }       
        public void run() {
            for(Iterator iter = _applications.iterator(); iter.hasNext(); ) {
                Application app = (Application)iter.next();
                
                if(_verbosity){
                    System.out.println("Running application " + app.getName());
                }
                app.run();
            }
        }
        /**
            Read the configuration for applications.
        */
        private void readConfig(){
            String filename = "app.config";
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(fi));
                String line = r.readLine();
                while(line != null){
                    if(line.charAt(0) == '#') {
                        line = r.readLine();
                        continue;
                    }
                    Application app = Application.create(_builder, line);
                    if(app != null){
                        addApplication(app);
                        app.setBuilder(this.getBuilder());
                    }else{
                        System.err.println("Skipped " + line);
                    }
                    line = r.readLine();                    
                }
            } catch (FileNotFoundException e) {
                System.err.println("Couldn't read file " + filename);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                if (fi != null) try { fi.close(); } catch (IOException _) { }
            }
        }
    }
    
    /**
     * This is something we typically run afte the IPSSABuilder.
     * @see IPSSABuilder
     * @see IPSSABuilder.ApplicationLaunchingPad
     * */
    public abstract static class Application implements Runnable {
        private String _name;
        protected IPSSABuilder _builder;

        public Application() {
            this(null, null);
        }        
        public Application(IPSSABuilder builder, String name, String[] args){
            parseParams(args);
            _name = name;
            _builder = builder;
            //
            initialize();
        }
        protected void initialize() {}
        
        protected void setBuilder(IPSSABuilder builder) {            
            _builder = builder;            
        }
        protected IPSSABuilder getBuilder() {
             return _builder;
        }
        public static Application create(IPSSABuilder builder, String line) {
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            String className = tokenizer.nextToken();
            String appName  = tokenizer.nextToken();

            Vector argv = new Vector(); 
            while(tokenizer.hasMoreTokens()) {
                argv.add(tokenizer.nextToken());
            }

            Application app = null;
            
            Class c;
            try {
                //className = Main.Driver.canonicalizeClassName(className);
                //System.err.println("'" + className + "'");
                c = Class.forName(className);
                try {
                    app = (Application)c.newInstance();
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                    return null;
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                    return null;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            
            if(app == null) {
                System.err.println("Can't create an instance of " + className);
                return null;
            }
            
            app.setBuilder(builder);
            app.setName(appName);
            app.parseParams(argv.toArray());
            
            // now that the values are set, call initialize
            app.initialize();

            return app;
        }
        private void setName(String appName) {
            _name = appName;
            
        }
        public String getName() {
            return _name;
        }
        Application(String name, String args){
            _name = name;
            StringTokenizer tokenizer = new StringTokenizer(args, " ");
            Vector argv = new Vector(); 
            while(tokenizer.hasMoreTokens()) {
                argv.add(tokenizer.nextToken());
            }
            parseParams((String[])argv.toArray());
        }        
        
        protected abstract void parseParams(String[] args);
        
        private void parseParams(Object[] objects) {
            String[] argv = new String[objects.length];
            for(int i = 0; i < objects.length; i++) {
                argv[i] = (String)objects[i];
            }             
            
            parseParams(argv);                         
        }       

        public abstract void run();
    }
    
    /**
     * This is an entry point for IPSSABuilder with a main(...) function.
     * */
    public static class Main {
        static boolean _verbose = false;
        
        public static void main(String[] args) {
            HostedVM.initialize();

            Iterator i = null; String memberName = null;
            for (int x=0; x<args.length; ++x) {
                if (args[x].equals("-file")) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(args[++x]));
                        LinkedList list = new LinkedList();
                        for (;;) {
                            String s = br.readLine();
                            if (s == null) break;
                            if (s.length() == 0) continue;
                            if (s.startsWith("%")) continue;
                            if (s.startsWith("#")) continue;
                            list.add(s);
                        }
                        i = new AppendIterator(list.iterator(), i);
                    }catch(IOException e) {
                        e.printStackTrace();
                        System.exit(2);
                    }
                    
                } else
                if (args[x].endsWith("*")) {
                    i = new AppendIterator(PrimordialClassLoader.loader.listPackage(args[x].substring(0, args[x].length()-1)), i);
                } else 
                if(args[x].charAt(0) == '-'){
                    usage();
                    System.exit(2);                    
                }else {
                    String classname = args[x];
                    i = new AppendIterator(Collections.singleton(classname).iterator(), i);
                }
            } // end argument processing
            if (i == null) {
                System.err.println("At least one class is requred for execution");
                System.exit(2);
            }
        
            LinkedList classes = new LinkedList();
            while (i.hasNext()) {
                String classname = (String)i.next();
                if (classname.endsWith(".properties")) continue;
                if (classname.endsWith(".class")) classname = classname.substring(0, classname.length()-6);
                String classdesc = "L" + classname.replace('.', '/') + ";";
                jq_Class c = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType(classdesc);
                System.err.println("Preparing class [" + classdesc + "] ...");
                c.prepare();
                classes.add(c);
            }
            
            // done with preparation, run the builder
            IPSSABuilder builder = new IPSSABuilder(classes, 2);
            builder.run();
        }

        private static void usage() {
            System.err.println("Usage: ");
            // TODO: specify usage...
        }
    }
};
