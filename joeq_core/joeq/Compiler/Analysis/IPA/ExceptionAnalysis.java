// ExceptionAnalysis.java, created May 5, 2004 2:10:39 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Dataflow.ReachingDefs;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ExceptionHandler;
import joeq.Compiler.Quad.ExceptionHandlerList;
import joeq.Compiler.Quad.LoadedCallGraph;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.Operand.AConstOperand;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operator.CheckCast;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.New;
import joeq.Compiler.Quad.Operator.Return;
import joeq.Compiler.Quad.Operator.Special;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.HostedVM;
import jwutil.graphs.SCComponent;
import jwutil.graphs.Traversals;

/**
 * Uses a call graph to figure out what exceptions can be thrown by a method invocation.
 * 
 * @author John Whaley
 * @version $Id: ExceptionAnalysis.java,v 1.3 2004/09/22 22:17:30 joewhaley Exp $
 */
public class ExceptionAnalysis {
    
    /**
     * Visit basic blocks to figure out what exceptions they can throw.
     */
    class BBVisit implements BasicBlockVisitor {
        private final ControlFlowGraph cfg;
        private final jq_Method method;
        private final Set s;
        boolean change;
        ReachingDefs rd;

        private BBVisit(ControlFlowGraph cfg, jq_Method method, Set s) {
            this.cfg = cfg;
            this.method = method;
            this.s = s;
        }

        public void visitBasicBlock(final BasicBlock bb) {
            bb.visitQuads(new QVisit(bb));
        }
        
        /**
         * Visit quads to figure out what exceptions they can throw.
         * If toExHandler is set, we figure out the exceptions that can
         * go to that handler.  If it is null, we figure out the exceptions
         * that can go to method exit.
         */
        class QVisit extends QuadVisitor.EmptyVisitor {
            private BasicBlock bb;
            private ExceptionHandler toExHandler;

            QVisit(BasicBlock bb) {
                this.bb = bb;
                this.toExHandler = null;
            }

            QVisit(BasicBlock bb, ExceptionHandler ex) {
                this.bb = bb;
                this.toExHandler = ex;
            }
            
            /** 
             * Find the containing basic block for a quad.
             */
            BasicBlock findBasicBlock(Quad q) {
                if (bb.getQuadIndex(q) != -1) return bb;
                for (Iterator i = cfg.reversePostOrderIterator(); i.hasNext(); ) {
                    BasicBlock bb2 = (BasicBlock) i.next();
                    if (bb2.getQuadIndex(q) != -1) return bb2;
                }
                return null;
            }
            
            void updateMySet(Collection exTypes) {
                if (toExHandler != null) {
                    if (updateSet(exTypes, s, toExHandler))
                        change = true;
                } else {
                    if (updateSet(exTypes, s, bb.getExceptionHandlers()))
                        change = true;
                }
            }
            
            public void visitExceptionThrower(Quad obj) {
                if (obj.getOperator() instanceof Invoke) return; // handled elsewhere
                if (obj.getOperator() instanceof Return.THROW_A) return; // handled elsewhere
                updateMySet(obj.getThrownExceptions());
            }

            public void visitReturn(Quad obj) {
                if (obj.getOperator() instanceof Return.THROW_A) {
                    Operand op = Return.getSrc(obj);
                    if (op instanceof RegisterOperand) {
                        // TODO: common case is that the def is in the same basic block.
                        // so just scan backwards in current basic block for definition.
                        
                        // use-def chain to figure out where this came from.
                        if (rd == null) rd = ReachingDefs.solve(cfg);
                        Register r = ((RegisterOperand) op).getRegister();
                        Set d = rd.getReachingDefs(bb, obj, r);
                        
                        // Keep track of visited quads, so we don't get caught in a cycle.
                        Set visited = new HashSet();
                        for (Iterator i = d.iterator(); i.hasNext(); ) {
                            Quad q = (Quad) i.next();
                            BasicBlock bb2 = findBasicBlock(q);
                            traceUseDef(visited, bb2, q);
                        }
                    } else if (op instanceof AConstOperand) {
                        // evil: throw null constant.
                        updateMySet(Collections.singleton(PrimordialClassLoader.getJavaLangNullPointerException()));
                    }
                }
            }

            public void visitInvoke(Quad obj) {
                ProgramLocation callSite = new QuadProgramLocation(method, obj);
                Collection targets = cg.getTargetMethods(callSite);
                for (Iterator i = targets.iterator(); i.hasNext(); ) {
                    updateMySet(getThrownExceptions((jq_Method) i.next()));
                }
            }
            
            void traceUseDef(Set visited, BasicBlock my_bb, Quad q) {
                if (!visited.add(q)) return;
                if (q.getOperator() instanceof New) {
                    jq_Class type = (jq_Class) New.getType(q).getType();
                    updateMySet(Collections.singleton(type));
                } else if (q.getOperator() instanceof Special.GET_EXCEPTION) {
                    Iterator i = cfg.getExceptionHandlersMatchingEntry(my_bb);
                    while (i.hasNext()) {
                        ExceptionHandler eh = (ExceptionHandler) i.next();
                        List bbs = eh.getHandledBasicBlocks();
                        for (Iterator j = bbs.iterator(); j.hasNext(); ) {
                            BasicBlock bb3 = (BasicBlock) j.next();
                            // TODO: this will loop infinitely if there are mutually-dependent
                            // exception handlers.
                            bb3.visitQuads(new QVisit(bb3, eh));
                        }
                    }
                } else if (q.getOperator() instanceof Move ||
                           q.getOperator() instanceof CheckCast) {
                    Operand src = Move.getSrc(q);
                    if (src instanceof RegisterOperand) {
                        Register r = ((RegisterOperand) src).getRegister();
                        if (rd == null) rd = ReachingDefs.solve(cfg);
                        Set d = rd.getReachingDefs(my_bb, q, r);
                        for (Iterator i = d.iterator(); i.hasNext(); ) {
                            Quad q2 = (Quad) i.next();
                            BasicBlock bb2 = findBasicBlock(q2);
                            traceUseDef(visited, bb2, q2);
                        }
                    } else {
                        // const null
                        updateMySet(Collections.singleton(PrimordialClassLoader.getJavaLangNullPointerException()));
                    }
                } else {
                    // Comes from somewhere unknown.
                    updateMySet(Collections.singleton(null));
                }
            }

        }

    }

    static final boolean USE_SOFTREF = true;
    static final boolean TRACE = false;
    static final PrintStream out = System.out;
    
    CallGraph cg;
    Map cache;
    Map recursive;
    
    /**
     * Construct exception analysis using the given call graph.
     */
    public ExceptionAnalysis(CallGraph cg) {
        if (TRACE) out.println("Initializing exception analysis");
        this.cg = cg;
        this.cache = new HashMap();
        this.recursive = new HashMap();
    }
    
    private void findRecursiveMethods() {
        Set/*SCComponent*/ roots = SCComponent.buildSCC(cg);
        List list = Traversals.reversePostOrder(SCComponent.SCC_NAVIGATOR, roots);
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            SCComponent scc = (SCComponent) i.next();
            if (scc.isLoop()) {
                for (Iterator j = scc.nodeSet().iterator(); j.hasNext(); ) {
                    this.recursive.put(j.next(), scc);
                }
            }
        }
        if (TRACE) out.println("Recursive methods: "+recursive);
    }
    
    static boolean updateSet(Collection exTypes, Set s, ExceptionHandler eh) {
        boolean c = false;
        for (Iterator i = exTypes.iterator(); i.hasNext(); ) {
            jq_Class r = (jq_Class) i.next();
            if (s.contains(r)) continue;
            jq_Class r2 = r;
            if (r2 == null) r2 = PrimordialClassLoader.JavaLangThrowable;
            if (eh != null && !eh.mayCatch(r2)) continue;
            if (s.add(r)) c = true;
        }
        return c;
    }
    
    static boolean updateSet(Collection exTypes, Set s, ExceptionHandlerList ex) {
        boolean c = false;
        for (Iterator i = exTypes.iterator(); i.hasNext(); ) {
            jq_Class r = (jq_Class) i.next();
            if (s.contains(r)) continue;
            jq_Class r2 = r;
            if (r2 == null) r2 = PrimordialClassLoader.JavaLangThrowable;
            if (ex != null && ex.mustCatch(r2) != null) continue;
            if (s.add(r)) c = true;
        }
        return c;
    }
    
    /**
     * Return the set of exception types that can be thrown by this call.
     * 
     * @param callSite call site
     * @return set of exception types
     */
    public Set getThrownExceptions(ProgramLocation callSite) {
        Set s = new HashSet();
        getThrownExceptions(callSite, s, null);
        return s;
    }
    
    /**
     * Add the set of exception types that can be thrown by this call and
     * that are not caught by the given exception handlers to the given set.
     * Returns true iff the set changed.
     * 
     * @param callSite call site
     * @param s set
     * @param ex exception handler list
     * @return whether set changed
     */
    public boolean getThrownExceptions(ProgramLocation callSite, Set s, ExceptionHandlerList ex) {
        if (TRACE) out.println("Call site "+callSite);
        Collection targets = cg.getTargetMethods(callSite);
        if (TRACE) out.println("    Targets "+targets);
        boolean change = false;
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            if (updateSet(getThrownExceptions((jq_Method) i.next()), s, ex))
                change = true;
        }
        if (TRACE) out.println("    Exceptions: "+s);
        return change;
    }
    
    private Set checkCache(jq_Method method) {
        Object o = cache.get(method);
        if (USE_SOFTREF && o instanceof SoftReference) {
            return (Set) ((SoftReference) o).get();
        } else {
            return (Set) o;
        }
    }
    
    /**
     * Return the set of exception types that can be thrown by this method.
     * 
     * @param method
     * @return set of exception types
     */
    public Set getThrownExceptions(jq_Method method) {
        Set s = checkCache(method);
        if (s != null) {
            if (TRACE) out.println("Cache hit: "+method);
            return s;
        }
        
        SCComponent scc = (SCComponent) recursive.get(method);
        if (scc != null) {
            if (TRACE) out.println(method+" is recursive, iterating around "+scc);
            iterateScc(scc);
            s = checkCache(method);
        } else {
            s = new HashSet();
            cache.put(method, USE_SOFTREF ? (Object) new SoftReference(s) : (Object) s);
            calcThrownExceptions(method, s);
        }
        return s;
    }
    
    private boolean calcThrownExceptions(final jq_Method method, final Set s) {
        if (TRACE) out.println("Calculating thrown exceptions of "+method);
        if (method.getBytecode() == null) {
            if (method.isAbstract()) {
                if (TRACE) out.println(method+" is abstract");
                return s.add(PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/AbstractMethodError;"));
            }
            if (method.isNative()) {
                // Native methods can throw arbitrary exceptions.
                if (TRACE) out.println(method+" is native");
                boolean change = s.add(null);
                return s.add(PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/LinkageError;")) || change;
            }
            // huh?
            return s.add(null);
        }
        
        ControlFlowGraph cfg = CodeCache.getCode(method);
        BBVisit bbv = new BBVisit(cfg, method, s);
        cfg.visitBasicBlocks(bbv);
        
        if (TRACE) out.println("Thrown exceptions of "+method+": "+s);
        return bbv.change;
    }

    private void iterateScc(SCComponent scc) {
        // Pre-allocate all cache entries, so we don't reenter iterateScc on
        // methods in the same SCC.
        for (Iterator i = scc.nodeSet().iterator(); i.hasNext(); ) {
            jq_Method method = (jq_Method) i.next();
            Set s = checkCache(method);
            if (s == null) {
                s = new HashSet();
                cache.put(method, USE_SOFTREF ? (Object) new SoftReference(s) : (Object) s);
            }
        }
        // Iterate until no more changes.
        boolean change;
        do {
            change = false;
            if (TRACE) out.println("Iterating: "+scc);
            for (Iterator i = scc.nodeSet().iterator(); i.hasNext(); ) {
                jq_Method method = (jq_Method) i.next();
                Set s = checkCache(method);
                if (USE_SOFTREF && s == null) {
                    // Soft reference was cleared, make it a hard reference so that
                    // we can finish the SCC iteration.
                    if (TRACE) out.println("Soft ref for "+method+" was cleared, changing to hard ref");
                    s = new HashSet();
                    cache.put(method, s);
                }
                if (calcThrownExceptions(method, s)) {
                    change = true;
                }
            }
            if (TRACE && change) out.println("Change occurred, iterating again.");
        } while (change);
    }
    
    public static void main(String[] args) throws IOException {
        HostedVM.initialize();
        CodeCache.AlwaysMap = true;
        String cgFilename = System.getProperty("callgraph", "callgraph");
        if (args.length > 1) cgFilename = args[0];
        CallGraph cg = new LoadedCallGraph(cgFilename);
        ExceptionAnalysis ea = new ExceptionAnalysis(cg);
        long time = System.currentTimeMillis();
        for (Iterator i = cg.getAllMethods().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            Set s = ea.getThrownExceptions(m);
            //System.out.println("Method "+m+" can throw:");
            //System.out.println("    "+s);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent: "+time+" ms");
    }
}
