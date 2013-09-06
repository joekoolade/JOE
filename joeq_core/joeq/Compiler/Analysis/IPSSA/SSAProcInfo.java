package joeq.Compiler.Analysis.IPSSA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.PrintStream;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPSSA.Utils.SimpleDominatorQuery;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.DotGraph;
import joeq.Compiler.Quad.ExceptionHandler;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Util.Templates.ListIterator;
import jwutil.util.Assert;

/**
 * @author V.Benjamin Livshits
 * @see SSAProcInfo.Query
 * @version $Id: SSAProcInfo.java,v 1.11 2004/09/22 22:17:32 joewhaley Exp $
 * */
public final class SSAProcInfo {
    protected static HashMap /*<Query,  SSABindingAnnote>*/     _queryMap  = new HashMap();
    protected static HashMap /*<Helper, SSABindingAnnote>*/     _helperMap = new HashMap();
    private static Iterator                                     _emptyIterator = null;
    
    public static Query retrieveQuery(jq_Method method){
        if(_queryMap.containsKey(method)){
            return (Query)_queryMap.get(method);
        }else{
            Query q = new Query(method);
            _queryMap.put(method, q);
            
            return q;
        }
    }
    public static Helper retrieveHelper(jq_Method method){
        if(_queryMap.containsKey(method)){
            return (Helper)_helperMap.get(method);
        }else{
            Helper q = new Helper(method);
            _helperMap.put(method, q);
            
            return q;
        }
    }
    
    static Iterator emptyIterator(){
        if(_emptyIterator == null){
            _emptyIterator = new HashSet().iterator();
        }
        return _emptyIterator;
    }
    
    /**
     * This class is used to get information about the IPSSA representation.
     * Use SSAProcInfo.retreiveQuery to get an appropriate query.
     * @see SSAProcInfo.Helper
     * */
    public static class Query {
        jq_Method                                            _method;
        protected ControlFlowGraph                          _cfg;
        protected DominatorQuery                          _dom_query;     
        protected HashMap /*<ProgramStatement, SSABindingAnnote>*/  _bindingMap;
        private Quad                                    _firstQuad;
                
        protected Query(jq_Method method){
            this._method      = method;
            this._cfg         = CodeCache.getCode(method);
            this._bindingMap = new HashMap();
            this._dom_query  = new SimpleDominatorQuery(_method);
            
            makeFirstStatement();        
        }
        
        private void makeFirstStatement(){
            _firstQuad = Operator.Special.create(0, Operator.Special.NOP.INSTANCE);
        }
        
        public String toString(){
            return "Query for " + _method.toString();
        }
        
        public SSADefinition getDefinitionFor(SSALocation loc, Quad q){
            SSABindingAnnote ba = (SSABindingAnnote)_bindingMap.get(q);
            if(ba == null) return null;

            return ba.getDefinitionFor(loc);
        }
                
        public SSADefinition getLastDefinitionFor(SSALocation loc, Quad q, boolean strict){
            if(strict){
                q = _dom_query.getImmediateDominator(q);                
            }
            
            while(q != null){
                SSADefinition def = getDefinitionFor(loc, q);
                if(def != null){
                    return def;
                }
                q = _dom_query.getImmediateDominator(q);
            }
            
            // reached the first quad, need to do a special lookup here
            return getDefinitionFor(loc, _firstQuad);
        }
        
        public SSAIterator.BindingIterator getBindingIterator(Quad q){
            Iterator iter = null;
            if(_bindingMap.containsKey(q)){
                iter = ((SSABindingAnnote)_bindingMap.get(q)).getBindingIterator(); 
            }else{
                iter = emptyIterator();    
            }
            
            return new SSAIterator.BindingIterator(iter);
        }
        
        public int getBindingCount(Quad quad) {
            if(!_bindingMap.containsKey(quad)){
                return 0;
            }else{                
                SSABindingAnnote ba = ((SSABindingAnnote)_bindingMap.get(quad));
                return ba.size();
            }
        }
    
        /**
         * An iterator for all bindings in method.
         * */    
        public SSAIterator.BindingIterator getBindingIterator(jq_Method method){
            class MethodBindingIterator implements Iterator {
                protected jq_Method _method;
                protected Iterator     _bindingIter;
                protected Iterator     _quadIter;
                protected Query     _query;
                
                public MethodBindingIterator(jq_Method method){
                    this._method      = method; 
                    this._quadIter       = new QuadIterator(CodeCache.getCode(_method));
                    this._bindingIter = emptyIterator();
                    this._query       = retrieveQuery(_method);                                         
                }
                public boolean hasNext(){
                    if(_bindingIter.hasNext()) return true;
                    
                    while(_quadIter.hasNext()){
                        Quad quad = (Quad)_quadIter.next();
                        if(_query.getBindingCount(quad) > 0){
                            _bindingIter = _query.getBindingIterator(quad);
                            
                            return true;
                        }
                    }
                    
                    return false;
                }
                public Object next(){
                    if(_bindingIter.hasNext()){
                        return _bindingIter.next();
                    }else{
                        Quad quad = (Quad)_quadIter.next();                        
                        _bindingIter = _query.getBindingIterator(quad);
                        
                        return _bindingIter.next();                         
                    }
                }                
                public void remove(){
                    Assert._assert(false, "Don't call this method");
                }    
            }
            return new SSAIterator.BindingIterator(new MethodBindingIterator(method));
        }
        
        public void print(PrintStream out){
            for (QuadIterator j=new QuadIterator(_cfg, true); j.hasNext(); ) {
                Quad q = j.nextQuad();
            
                SSABindingAnnote ba = (SSABindingAnnote)_bindingMap.get(q);
                if(ba == null) continue;
                out.println(q.toString() + "\n" + ba.toString("\t"));                    
            }
        }
        
        public void printDot(){
            new DotGraph(){
            /** Overwrite the CFG traversal method */
            public void visitCFG(ControlFlowGraph cfg) {
                try {
                    String filename = createMethodName(_method) + ".ssa.dot";
                    //System.err.println("Opening "+filename);
                    dot.openGraph("ssagraphs", filename);
                    
                    cfg.visitBasicBlocks(new BasicBlockVisitor() {
                        public void visitBasicBlock(BasicBlock bb) {
                            SSAProcInfo.Query q = SSAProcInfo.retrieveQuery(_cfg.getMethod());
                            if (bb.isEntry()) {
                                if (bb.getNumberOfSuccessors() != 1)
                                    throw new Error("entry bb has != 1 successors " + bb.getNumberOfSuccessors());
                                dot.addEntryEdge(bb.toString(), bb.getSuccessors().iterator().next().toString(), null);
                                
                                StringBuffer l = new StringBuffer("Init:\\l");
                                Quad quad = getFirstQuad();
                                Iterator iter = q.getBindingIterator(quad); 
                                    
                                if(iter.hasNext()){
                                    do {
                                        SSABinding b = (SSABinding)iter.next();
                                        l.append("     => " + b.toString() + "\\l");
                                    } while(iter.hasNext());
                                    dot.userDefined("\t\"" + bb.toString() + "\" [shape=box,label=\"" + l + "\"];\n");
                                }
                            } else
                            if (!bb.isExit()) {
                                ListIterator.Quad qit = bb.iterator();
                                StringBuffer l = new StringBuffer("Basic Block " + bb.toString() + "\\l");
                                HashSet allExceptions = new HashSet();
                                while (qit.hasNext()) {
                                    // This is where the text of the bb is created
                                    l.append(" ");
                                    Quad quad = qit.nextQuad();
                                    l.append(dot.escape(quad.toString()));
                                    if(q.getBindingCount(quad) > 0){
                                        l.append("(" + q.getBindingCount(quad) + ") \\l");
                                        for(Iterator iter = q.getBindingIterator(quad); iter.hasNext();){
                                            SSABinding b = (SSABinding)iter.next();
                                            l.append("     => " + b.toString() + "\\l");
                                        }
                                    }else{
                                        l.append("\\l");
                                    }
                                                                        
                                    ListIterator.jq_Class exceptions = quad.getThrownExceptions().classIterator();
                                    while (exceptions.hasNext()) {
                                        allExceptions.add(exceptions.nextClass());
                                    }
                                }
                                dot.userDefined("\t\"" + bb.toString() + "\" [shape=box,label=\"" + l + "\"];\n");

                                ListIterator.BasicBlock bit = bb.getSuccessors().basicBlockIterator();
                                while (bit.hasNext()) {
                                    BasicBlock nextbb = bit.nextBasicBlock();
                                    if (nextbb.isExit()) {
                                        dot.addLeavingEdge(bb.toString(), nextbb.toString(), null);
                                    } else {
                                        dot.addEdge(bb.toString(), nextbb.toString());
                                    }
                                }

                                Iterator eit = allExceptions.iterator();
                                while (eit.hasNext()) {
                                    jq_Class exc = (jq_Class)eit.next();
                                    ListIterator.ExceptionHandler mayCatch;
                                    mayCatch = bb.getExceptionHandlers().mayCatch(exc).exceptionHandlerIterator();
                                    while (mayCatch.hasNext()) {
                                        ExceptionHandler exceptionHandler = mayCatch.nextExceptionHandler();
                                        BasicBlock nextbb = exceptionHandler.getEntry();
                                        dot.addEdge(bb.toString(), nextbb.toString(), exceptionHandler.getExceptionType().toString());
                                    }
                                    // if (bb.getExceptionHandlers().mustCatch(exc) == null) { }
                                }
                            }
                        }
                    });
                } catch(Exception e){
                    System.err.println("Error while writing ");
                    e.printStackTrace();
                    System.exit(2);
                } finally {
                    dot.closeGraph();
                }
            }}.visitCFG(_cfg);         
        }

        public DominatorQuery getDominatorQuery() {
            return _dom_query;            
        }

        public Quad getFirstQuad() {
            return _firstQuad;
        }
    }
        
    /**
     * This class is used to make modifications to the IPSSA representation.
     * @see SSAProcInfo.Query
     * */
    public static class Helper {
        jq_Method _method;
        Query     _query;
        
        protected Helper(jq_Method method){
            this._method = method;
            this._query  = SSAProcInfo.retrieveQuery(_method);
        }
        
        public static SSADefinition create_ssa_definition(SSALocation loc, Quad quad, jq_Method method) {
            return SSADefinition.Helper.create_ssa_definition(loc, quad, method);
        }
    }
    
    static class SSABindingAnnote {
        protected LinkedList _bindings;
        
        SSABindingAnnote(){
            _bindings = new LinkedList();
        }
                
        public SSADefinition getDefinitionFor(SSALocation loc) {
            for(Iterator iter = _bindings.iterator(); iter.hasNext();){
                SSABinding b = (SSABinding)iter.next();
                SSADefinition def = b.getDestination();
                if(def.getLocation() == loc){
                    return def;
                }
            }            
            return null;
        }

        public SSADefinition addBinding(SSALocation loc, SSAValue value, Quad quad, jq_Method method) {
            SSABinding b = new SSABinding(quad, loc, value, method);            
            Assert._assert(quad == b.getDestination().getQuad());
            
            this._bindings.addLast(b);
            //System.err.println("Have a total of " + _bindings.size() + " bindings");
            // TODO: uncomment this
            //Assert._assert(is_valid(), "Adding " + b + " to the binding annote " + this + " at " + quad + " makes it invalid");
            
            return b.getDestination();         
        }

        /**
         * Checks for duplicates among defined locations.
         * */
        // TODO: this is an expensive check, make it conditional on a flag
        public boolean is_valid() {
            return true;
            /*                    
            HashSet locations = new HashSet();
             
            Iterator iter = _bindings.iterator();
            while(iter.hasNext()){
                SSALocation loc = ((SSABinding)iter.next()).getDestination().getLocation();
                if(locations.contains(loc)){
                    locations = null;
                    return false;
                }else{
                    locations.add(loc);
                }
            }
                
            locations = null;
            return true;
            */
        }
        
        public Iterator getBindingIterator(){
            return _bindings.iterator();
        }
        
        public int size(){return _bindings.size();}
        
        public String toString(String prepend){
            StringBuffer result = new StringBuffer();
            for(Iterator iter = _bindings.iterator(); iter.hasNext();){
                SSABinding b = (SSABinding)iter.next();
                result.append(prepend);
                result.append(b.toString());
                result.append("\n");
            }
            
            return result.toString();
        }
        
        public String toString(){return toString("");}
    }
}

