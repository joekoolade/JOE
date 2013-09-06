// MethodSummary.java, created Aug 13, 2003 10:52:08 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.FlowInsensitive;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_FakeInstanceMethod;
import joeq.Class.jq_FakeStaticMethod;
import joeq.Class.jq_Field;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.Class.jq_Reference.jq_NullType;
import joeq.Compiler.Analysis.IPA.LoopAnalysis;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Analysis.IPA.ProgramLocation.FakeProgramLocation;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.ExceptionHandler;
import joeq.Compiler.Quad.JSRInfo;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.RegisterFactory;
import joeq.Compiler.Quad.BytecodeToQuad.jq_ReturnAddressType;
import joeq.Compiler.Quad.Operand.AConstOperand;
import joeq.Compiler.Quad.Operand.Const4Operand;
import joeq.Compiler.Quad.Operand.PConstOperand;
import joeq.Compiler.Quad.Operand.ParamListOperand;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operator.ALoad;
import joeq.Compiler.Quad.Operator.AStore;
import joeq.Compiler.Quad.Operator.Binary;
import joeq.Compiler.Quad.Operator.CheckCast;
import joeq.Compiler.Quad.Operator.Getfield;
import joeq.Compiler.Quad.Operator.Getstatic;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.Jsr;
import joeq.Compiler.Quad.Operator.Monitor;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.New;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.Operator.Phi;
import joeq.Compiler.Quad.Operator.Putfield;
import joeq.Compiler.Quad.Operator.Putstatic;
import joeq.Compiler.Quad.Operator.Return;
import joeq.Compiler.Quad.Operator.Special;
import joeq.Compiler.Quad.Operator.Unary;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Compiler.Quad.SSA.EnterSSA;
import joeq.Main.HostedVM;
import joeq.Memory.Address;
import joeq.Memory.StackAddress;
import joeq.Runtime.Reflection;
import joeq.Runtime.TypeCheck;
import jwutil.collections.CollectionTestWrapper;
import jwutil.collections.Filter;
import jwutil.collections.FilterIterator;
import jwutil.collections.FlattenedCollection;
import jwutil.collections.HashCodeComparator;
import jwutil.collections.IdentityHashCodeWrapper;
import jwutil.collections.IndexMap;
import jwutil.collections.InstrumentedSetWrapper;
import jwutil.collections.MultiMap;
import jwutil.collections.Pair;
import jwutil.collections.SetFactory;
import jwutil.collections.SortedArraySet;
import jwutil.collections.Triple;
import jwutil.graphs.Navigator;
import jwutil.io.Textualizable;
import jwutil.io.Textualizer;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * MethodSummary
 * 
 * @author John Whaley
 * @version $Id: MethodSummary.java,v 1.98 2006/03/09 03:42:51 livshits Exp $
 */
public class MethodSummary {
    
    public static PrintStream out = System.out;
    public static /*final*/ boolean TRACE_INTRA = System.getProperty("ms.traceintra") != null;
    public static /*final*/ boolean TRACE_INTER = System.getProperty("ms.traceinter") != null;
    public static /*final*/ boolean TRACE_INST = System.getProperty("ms.traceinst") != null;
    public static /*final*/ boolean TRACE_DOT = System.getProperty("ms.tracedot") != null;
    public static final boolean IGNORE_INSTANCE_FIELDS = false;
    public static final boolean IGNORE_STATIC_FIELDS = false;
    public static final boolean VERIFY_ASSERTIONS = false;

    public static boolean SSA = System.getProperty("ssa") != null;
    
    public static final boolean USE_IDENTITY_HASHCODE = false;
    public static final boolean DETERMINISTIC = !USE_IDENTITY_HASHCODE && true;
    
    public static final boolean SPLIT_THREADS = true;
    
    public static Set ssaEntered = new HashSet();
    public static Map stringNodes2Values = new HashMap();
    
    /**
     * Helper class to output method summary in dot graph format.
     * @author jwhaley
     */
    public static final class MethodSummaryBuilder implements ControlFlowGraphVisitor {
        public void visitCFG(ControlFlowGraph cfg) {
            MethodSummary s = getSummary(cfg);
            //System.out.println("Summary for " + cfg.getMethod() + ": " + s.toString());
            try {
                BufferedWriter dos = new BufferedWriter(new OutputStreamWriter(System.out));
                s.dotGraph(dos);
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }
    
    /**
     * Holds the cache of method summary graphs.
     */
    public static HashMap summary_cache = new HashMap();
    
    /**
     * Get the method summary for the given CFG.  Builds and caches it if it doesn't
     * already exist.
     * 
     * @param cfg
     * @return  summary for the given CFG
     */
    public static MethodSummary getSummary(ControlFlowGraph cfg) {
        MethodSummary s = (MethodSummary) summary_cache.get(cfg);
        if (s == null) {
            if (TRACE_INTER) out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
            if (TRACE_INTER) out.println("Building summary for "+cfg.getMethod());
            try {
                BuildMethodSummary b = new BuildMethodSummary(cfg);
                s = b.getSummary();
            } catch (RuntimeException t) {
                System.err.println("Runtime exception when getting method summary for "+cfg.getMethod());
                throw t;
            } catch (Error t) {
                System.err.println("Error when getting method summary for "+cfg.getMethod());
                throw t;
            }
            summary_cache.put(cfg, s);
            if (TRACE_INTER) out.println("Summary for "+cfg.getMethod()+":");
            if (TRACE_INTER) out.println(s);
            if (TRACE_INTER) out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            if (TRACE_DOT) {
                try {
                    BufferedWriter dos = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("out.txt", true)));
                    dos.write("Method summary for " + s.getMethod() + "\n");
                    s.dotGraph(dos);
                    dos.flush();
                    dos.close();
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }            
        }
        //System.out.println("Summary for " + cfg.getMethod() + ": " + s.toString());
        return s;
    }

    /**
     * Get the method summary for the given method.  
     * If we know how to fake a method summary, do so.
     *
     * @return null if method has no bytecode and we do not know how to fake it
     */
    public static MethodSummary getSummary(jq_Method m) {
        if(m instanceof jq_FakeStaticMethod || m instanceof jq_FakeInstanceMethod) {
            return null;
        }
        MethodSummary hasFake = fakeMethodSummary(m);
        if (hasFake != null)
            return hasFake;

        if (m.getBytecode() == null) {
            return null;
        }
        
//        jq_Method replacement = bogusSummaryProvider.getReplacementMethod(m);
//        if(replacement != null) {
//            System.out.println("Replacing a summary of " + m + 
//                " with one for "+ replacement);
//            ControlFlowGraph cfg = CodeCache.getCode(replacement);
//            return getSummary(cfg);
//        }

        ControlFlowGraph cfg = CodeCache.getCode(m);
        if (SSA & !ssaEntered.contains(cfg)) {
            //out.println("CFG BEFORE SSA:");
            //out.println(cfg.fullDump());
            if (SSA) new EnterSSA().visitCFG(cfg);
            //out.println("CFG AFTER SSA:");
            //out.println(cfg.fullDump());
            ssaEntered.add(cfg);
        }
        return getSummary(cfg);
    }
    
    /**
     * Clear the method summary graph.
     */
    public static void clearSummaryCache() {
        summary_cache.clear();
        ConcreteTypeNode.FACTORY.clear();
        ConcreteTypeNode.FACTORY2.clear();
        ConcreteObjectNode.FACTORY.clear();
        UnknownTypeNode.FACTORY.clear();
        ReturnValueNode.FACTORY.clear();
        ThrownExceptionNode.FACTORY.clear();
        FieldNode.FACTORY.clear();
        GlobalNode.FACTORY.clear();
        GlobalNode.GLOBAL = GlobalNode.get((jq_Method) null);
    }
    
    /** Cache of cloned method summaries. */
    public static HashMap clone_cache;
    
    /** Get the (context-sensitive) method summary for the given control flow graph
     * when called from the given call site.  If clone_cache is null, falls back to
     * the context-insensitive version.
     * 
     * @param cfg
     * @param cs
     * @return  summary for the given CFG and context
     */
    public static MethodSummary getSummary(ControlFlowGraph cfg, CallSite cs) {
        if (clone_cache != null) {
            //System.out.println("Checking cache for "+new Pair(cfg, cs));
            MethodSummary ms = (MethodSummary) clone_cache.get(new Pair(cfg, cs));
            if (ms != null) {
                //System.out.println("Using specialized version of "+ms.getMethod()+" for call site "+cs);
                return ms;
            }
        }
        return getSummary(cfg);
    }
    
    /** Visitor class to build an intramethod summary. */
    public static final class BuildMethodSummary extends QuadVisitor.EmptyVisitor {
        
        /** The method that we are building a summary for. */
        protected final jq_Method method;
        /** The register factory. */
        protected final RegisterFactory rf;
        /** The parameter nodes. */
        protected final ParamNode[] param_nodes;
        /** The global node. */
        protected final GlobalNode my_global;
        /** The start states of the iteration. */
        protected final State[] start_states;
        /** The set of returned and thrown nodes. */
        protected final Set returned, thrown;
        /** The set of method calls made. */
        protected final Set methodCalls;
        /** Map from a method call to its ReturnValueNode. */
        protected final HashMap callToRVN;
        /** Map from a method call to its ThrownExceptionNode. */
        protected final HashMap callToTEN;
        /** Map from a (Node,Quad) pair to the node it's cast to. */
        protected final LinkedHashMap castMap;
        /** Set of nodes that lead to a cast. */
        protected final LinkedHashSet castPredecessors;
        /** The set of nodes that were ever passed as a parameter, or returned/thrown from a call site. */
        protected final Set passedAsParameter;
        /** The current basic block. */
        protected BasicBlock bb;
        /** The current state. */
        protected State s;
        /** Change bit for worklist iteration. */
        protected boolean change;
        
        boolean include_sync_ops = true;
        boolean include_cast_ops = System.getProperty("ms.withcasts", "yes").equals("yes");
        
        /** Map from sync ops to their nodes. */
        protected final Map sync_ops;
        
        /** Factory for nodes. */
        protected final HashMap nodeCache;
        
        BuildMethodSummary(BuildMethodSummary that) {
            this.method = that.method;
            this.rf = that.rf;
            this.param_nodes = that.param_nodes;
            this.my_global = that.my_global;
            this.start_states = that.start_states;
            this.returned = that.returned;
            this.thrown = that.thrown;
            this.methodCalls = that.methodCalls;
            this.callToRVN = that.callToRVN;
            this.callToTEN = that.callToTEN;
            this.castMap = that.castMap;
            this.castPredecessors = that.castPredecessors;
            this.passedAsParameter = that.passedAsParameter;
            this.bb = that.bb;
            this.s = that.s;
            this.change = that.change;
            this.sync_ops = that.sync_ops;
            this.nodeCache = that.nodeCache;
        }
        
        /** Returns the summary. Call this after iteration has completed. */
        public MethodSummary getSummary() {
            MethodSummary s = new MethodSummary(this,
                                                method,
                                                param_nodes,
                                                my_global,
                                                methodCalls,
                                                callToRVN,
                                                callToTEN,
                                                castMap,
                                                castPredecessors,
                                                returned,
                                                thrown,
                                                passedAsParameter,
                                                sync_ops);
            return s;
        }

        /** Set the given local in the current state to point to the given node. */
        protected void setLocal(int i, Node n) { s.registers[i] = n; }
        /** Set the given register in the current state to point to the given node. */
        protected void setRegister(Register r, Node n) {
            int i = r.getNumber();
            s.registers[i] = n;
            if (TRACE_INTRA) out.println("Setting register "+r+" to "+n);
        }
        /** Set the given register in the current state to point to the given node or set of nodes. */
        protected void setRegister(Register r, Object n) {
            int i = r.getNumber();
            if (n instanceof Collection) n = NodeSet.FACTORY.makeSet((Collection)n);
            else if (n != null) Assert._assert(n instanceof Node);
            s.registers[i] = n;
            if (TRACE_INTRA) out.println("Setting register "+r+" to "+n);
        }
        /** Get the node or set of nodes in the given register in the current state. */
        public Object getRegister(Register r) {
            int i = r.getNumber();
            if (s.registers[i] == null) {
                //System.out.println(method+" ::: Reg "+i+" is null");
                //Assert.UNREACHABLE();
            }
            return s.registers[i];
        }

        /** Calculate the register set up to a given point. */
        public void updateLocation(BasicBlock bb, Quad q) {
            this.bb = bb;
            this.s = start_states[bb.getID()];
            this.s = this.s.copy();
            for (joeq.Util.Templates.ListIterator.Quad i = bb.iterator(); i.hasNext(); ) {
                Quad q2 = i.nextQuad();
                q2.accept(this);
                if (q2 == q) break;
            }
        }
        
        /** Build a summary for the given method. */
        public BuildMethodSummary(ControlFlowGraph cfg) {
            this.rf = cfg.getRegisterFactory();
            this.method = cfg.getMethod();
            this.start_states = new State[cfg.getNumberOfBasicBlocks()];
            this.methodCalls = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            this.callToRVN = new HashMap();
            this.callToTEN = new HashMap();
            this.passedAsParameter = NodeSet.FACTORY.makeSet();
            this.nodeCache = new HashMap();
            this.s = this.start_states[0] = new State(rf.size());
            jq_Type[] params = this.method.getParamTypes();
            this.param_nodes = new ParamNode[params.length];
            for (int i=0, j=0; i<params.length; ++i, ++j) {
                if (params[i].isReferenceType()
                    /*&& !params[i].isAddressType()*/
                    ) {
                    setLocal(j, param_nodes[i] = ParamNode.get(method, i, (jq_Reference)params[i]));
                } else if (params[i].getReferenceSize() == 8) {
                    //++j;
                }
            }
            this.my_global = GlobalNode.get(this.method);
            this.sync_ops = new HashMap();
            this.castMap = new LinkedHashMap();
            this.castPredecessors = new LinkedHashSet();
            this.returned = NodeSet.FACTORY.makeSet(); this.thrown = NodeSet.FACTORY.makeSet();
            
            if (TRACE_INTRA) out.println("Building summary for "+this.method);
            
            // iterate until convergence.
            joeq.Util.Templates.List.BasicBlock rpo_list = cfg.reversePostOrder(cfg.entry());
            for (;;) {
                joeq.Util.Templates.ListIterator.BasicBlock rpo = rpo_list.basicBlockIterator();
                this.change = false;
                while (rpo.hasNext()) {
                    this.bb = rpo.nextBasicBlock();
                    this.s = start_states[bb.getID()];
                    if (this.s == null) {
                        continue;
                    }
                    this.s = this.s.copy();
                    /*
                    if (this.bb.isExceptionHandlerEntry()) {
                        java.util.Iterator i = cfg.getExceptionHandlersMatchingEntry(this.bb);
                        jq.Assert(i.hasNext());
                        ExceptionHandler eh = (ExceptionHandler)i.next();
                        CaughtExceptionNode n = new CaughtExceptionNode(eh);
                        if (i.hasNext()) {
                            Set set = NodeSet.FACTORY.makeSet(); set.add(n);
                            while (i.hasNext()) {
                                eh = (ExceptionHandler)i.next();
                                n = new CaughtExceptionNode(eh);
                                set.add(n);
                            }
                            s.merge(nLocals, set);
                        } else {
                            s.merge(nLocals, n);
                        }
                    }
                     */
                    if (TRACE_INTRA) {
                        out.println("State at beginning of "+this.bb+":");
                        this.s.dump(out);
                    }
                    this.bb.visitQuads(this);
                    joeq.Util.Templates.ListIterator.BasicBlock succs = this.bb.getSuccessors().basicBlockIterator();
                    while (succs.hasNext()) {
                        BasicBlock succ = succs.nextBasicBlock();
                        if (this.bb.endsInRet()) {
                            if (jsr_states != null) {
                                State s2 = (State) jsr_states.get(succ);
                                if (s2 != null) {
                                    JSRInfo info = cfg.getJSRInfo(this.bb);
                                    boolean[] changedLocals = info.changedLocals;
                                    mergeWithJSR(succ, s2, changedLocals);
                                } else {
                                    if (TRACE_INTRA) out.println("jsr before "+succ+" not yet visited!");
                                }
                            } else {
                                if (TRACE_INTRA) out.println("no jsr's visited yet! was looking for jsr successor "+succ);
                            }
                        } else {
                            mergeWith(succ);
                        }
                    }
                }
                if (!this.change) break;
            }
        }

        /** Merge the current state into the start state for the given basic block.
         *  If that start state is uninitialized, it is initialized with a copy of
         *  the current state.  This updates the change flag if anything is changed. */
        protected void mergeWith(BasicBlock succ) {
            if (this.start_states[succ.getID()] == null) {
                if (TRACE_INTRA) out.println(succ+" not yet visited.");
                this.start_states[succ.getID()] = this.s.copy();
                this.change = true;
            } else {
                //if (TRACE_INTRA) out.println("merging out set of "+bb+" "+Strings.hex8(this.s.hashCode())+" into in set of "+succ+" "+Strings.hex8(this.start_states[succ.getID()].hashCode()));
                if (TRACE_INTRA) out.println("merging out set of "+bb+" into in set of "+succ);
                if (this.start_states[succ.getID()].merge(this.s)) {
                    if (TRACE_INTRA) out.println(succ+" in set changed");
                    this.change = true;
                }
            }
        }
        
        protected void mergeWithJSR(BasicBlock succ, State s2, boolean[] changedLocals) {
            State state = this.start_states[succ.getID()];
            if (state == null) {
                if (TRACE_INTRA) out.println(succ+" not yet visited.");
                this.start_states[succ.getID()] = state = this.s.copy();
                this.change = true;
            }
            //if (TRACE_INTRA) out.println("merging out set of jsr "+bb+" "+Strings.hex8(this.s.hashCode())+" into in set of "+succ+" "+Strings.hex8(this.start_states[succ.getID()].hashCode()));
            if (TRACE_INTRA) out.println("merging out set of jsr "+bb+" into in set of "+succ);
            for (int i=0; i<changedLocals.length; ++i) {
                if (changedLocals[i]) {
                    if (state.merge(i, this.s.registers[i])) {
                        if (TRACE_INTRA) out.println(succ+" in set changed by register "+i+" in jsr subroutine");
                        this.change = true;
                    }
                } else {
                    if (state.merge(i, s2.registers[i])) {
                        if (TRACE_INTRA) out.println(succ+" in set changed by register "+i+" before jsr subroutine");
                        this.change = true;
                    }
                }
            }
        }
        
        /** Merge the current state into the start state for the given basic block.
         *  If that start state is uninitialized, it is initialized with a copy of
         *  the current state.  This updates the change flag if anything is changed. */
        protected void mergeWith(ExceptionHandler eh) {
            BasicBlock succ = eh.getEntry();
            if (this.start_states[succ.getID()] == null) {
                if (TRACE_INTRA) out.println(succ+" not yet visited.");
                this.start_states[succ.getID()] = this.s.copy();
                for (Iterator i = rf.iterator(); i.hasNext(); ) {
                    Register r = (Register) i.next();
                    if (r.isTemp())
                        this.start_states[succ.getID()].registers[r.getNumber()] = null;
                }
                this.change = true;
            } else {
                //if (TRACE_INTRA) out.println("merging out set of "+bb+" "+Strings.hex8(this.s.hashCode())+" into in set of ex handler "+succ+" "+Strings.hex8(this.start_states[succ.getID()].hashCode()));
                if (TRACE_INTRA) out.println("merging out set of "+bb+" into in set of ex handler "+succ);
                for (Iterator i = rf.iterator(); i.hasNext(); ) {
                    Register r = (Register) i.next();
                    if (r.isTemp()) continue;
                    if (this.start_states[succ.getID()].merge(r.getNumber(), this.s.registers[r.getNumber()]))
                        this.change = true;
                }
                if (TRACE_INTRA && this.change) out.println(succ+" in set changed");
            }
        }
        
        public static final boolean INSIDE_EDGES = false;
        
        /** Abstractly perform a heap load operation on the given base and field
         *  with the given field node, putting the result in the given set. */
        protected void heapLoad(Set result, Node base, jq_Field f, FieldNode fn) {
            //base.addAccessPathEdge(f, fn);
            result.add(fn);
            if (INSIDE_EDGES)
                base.getAllEdges(f, result);
        }
        /** Abstractly perform a heap load operation corresponding to quad 'obj'
         *  with the given destination register, bases and field.  The destination
         *  register in the current state is changed to the result. */
        protected void heapLoad(ProgramLocation obj, Register dest_r, Set base_s, jq_Field f) {
            Set result = NodeSet.FACTORY.makeSet();
            for (Iterator i=base_s.iterator(); i.hasNext(); ) {
                Node base = (Node)i.next();
                FieldNode fn = FieldNode.get(base, f, obj);
                heapLoad(result, base, f, fn);
            }
            setRegister(dest_r, result);
        }
        /** Abstractly perform a heap load operation corresponding to quad 'obj'
         *  with the given destination register, base and field.  The destination
         *  register in the current state is changed to the result. */
        protected void heapLoad(ProgramLocation obj, Register dest_r, Node base_n, jq_Field f) {
            FieldNode fn = FieldNode.get(base_n, f, obj);
            Set result = NodeSet.FACTORY.makeSet();
            heapLoad(result, base_n, f, fn);
            setRegister(dest_r, result);
        }
        /** Abstractly perform a heap load operation corresponding to quad 'obj'
         *  with the given destination register, base register and field.  The
         *  destination register in the current state is changed to the result. */
        protected void heapLoad(ProgramLocation obj, Register dest_r, Register base_r, jq_Field f) {
            Object o = getRegister(base_r);
            if (o instanceof Set) {
                heapLoad(obj, dest_r, (Set)o, f);
            } else {
                heapLoad(obj, dest_r, (Node)o, f);
            }
        }
        
        /** Abstractly perform a heap store operation of the given source node on
         *  the given base node and field. */
        protected void heapStore(Node base, Node src, jq_Field f) {
            base.addEdge(f, src);
        }
        /** Abstractly perform a heap store operation of the given source nodes on
         *  the given base node and field. */
        protected void heapStore(Node base, Set src, jq_Field f) {
            base.addEdges(f, NodeSet.FACTORY.makeSet(src));
        }
        protected void heapStore(Node base, Object src, jq_Field f) {
            if (src instanceof Node) {
                heapStore(base, (Node) src, f);
            } else {
                heapStore(base, (Set) src, f);
            }
        }
        /** Abstractly perform a heap store operation of the given source node on
         *  the nodes in the given register in the current state and the given field. */
        protected void heapStore(Object base, Object src, jq_Field f) {
            if (base instanceof Node) {
                if (src instanceof Node) {
                    heapStore((Node) base, (Node) src, f);
                } else {
                    heapStore((Node) base, (Set) src, f);
                }
            } else {
                Set s = (Set) base;
                if (src instanceof Node) {
                    for (Iterator i = s.iterator(); i.hasNext(); ) {
                        heapStore((Node)i.next(), (Node) src, f);
                    }
                } else {
                    for (Iterator i = s.iterator(); i.hasNext(); ) {
                        heapStore((Node)i.next(), (Set) src, f);
                    }
                }
            }
        }

        protected void monitorOp(Quad q, Register r) {
            Object src = getRegister(r);
            if (src instanceof Node) {
                monitorOp(q, Collections.singleton(src));
            } else {
                monitorOp(q, (Set) src);
            }
        }
        
        protected void monitorOp(Quad q, Set s) {
            Set old = (Set) sync_ops.get(q);
            if (old != null) {
                Assert._assert(s.containsAll(old));
            }
            sync_ops.put(q, s);
        }
        
        /** Record that the nodes in the given register were passed to the given
         *  method call as the given parameter. */
        void passParameter(Register r, ProgramLocation m, int p) {
            Object v = getRegister(r);
            if (TRACE_INTRA) out.println("Passing "+r+" to "+m+" param "+p+": "+v);
            if (v instanceof Set) {
                for (Iterator i = ((Set)v).iterator(); i.hasNext(); ) {
                    Node n = (Node)i.next();
                    n.recordPassedParameter(m, p);
                    passedAsParameter.add(n);
                }
            } else {
                Node n = (Node)v;
                n.recordPassedParameter(m, p);
                passedAsParameter.add(n);
            }
        }
        
        // use a single node for all like constants across all method summaries.
        static final boolean GLOBAL_OBJECT_CONSTANTS = false;
        // use a single node for all constants of the same type across all method summaries.
        static final boolean GLOBAL_TYPE_CONSTANTS = false;
        // use a single node for all like constants within a method.
        static final boolean MERGE_LOCAL_CONSTANTS = false;
        public static boolean PATCH_UP_FAKE = false;
        Node handleConst(Const4Operand op, ProgramLocation pl) {
            return handleConst(op, pl, 0);
        }
        Node handleConst(Const4Operand op, ProgramLocation pl, int opn) {
            Node n;
            if (op instanceof AConstOperand) {
                AConstOperand aop = (AConstOperand) op;
                if (GLOBAL_OBJECT_CONSTANTS) {
                    n = ConcreteObjectNode.get(aop, (ProgramLocation)null);
                } else if (GLOBAL_TYPE_CONSTANTS) {
                    n = ConcreteTypeNode.get(aop.getType());
                } else {
                    if (MERGE_LOCAL_CONSTANTS) {
                        n = ConcreteObjectNode.get(aop, pl);
                    } else {
                        n = ConcreteTypeNode.get(aop.getType(), pl, new Integer(opn));
                        if(aop.getValue() instanceof String){
                            String value = (String) aop.getValue();
                            
                            stringNodes2Values.put(n, value);
                            //System.out.println("Saved mapping " + n + " -> " + value);
                        }
                    }
                }
            } else {
                jq_Reference type = ((PConstOperand)op).getType();
                n = UnknownTypeNode.get(type);
            }
            return n;
        }
        
        /** Visit an array load instruction. */
        public void visitALoad(Quad obj) {
            if (obj.getOperator() instanceof Operator.ALoad.ALOAD_A
                || obj.getOperator() instanceof Operator.ALoad.ALOAD_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register r = ALoad.getDest(obj).getRegister();
                Operand o = ALoad.getBase(obj);
                ProgramLocation pl = new QuadProgramLocation(method, obj);
                if (o instanceof RegisterOperand) {
                    Register b = ((RegisterOperand)o).getRegister();
                    heapLoad(pl, r, b, null);
                } else {
                    // base is not a register?!
                    Node n = handleConst((AConstOperand) o, pl);
                    heapLoad(pl, r, n, null);
                }
            }
        }
        /** Visit an array store instruction. */
        public void visitAStore(Quad obj) {
            if (obj.getOperator() instanceof Operator.AStore.ASTORE_A
                || obj.getOperator() instanceof Operator.AStore.ASTORE_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Operand val_op = AStore.getValue(obj);
                Operand base_op = AStore.getBase(obj);
                Object val, base;
                if (base_op instanceof RegisterOperand) {
                    Register base_r = ((RegisterOperand)base_op).getRegister();
                    base = getRegister(base_r);
                } else {
                    // base is not a register?!
                    base = handleConst((AConstOperand) base_op, new QuadProgramLocation(method, obj), 0);
                }
                if (val_op instanceof RegisterOperand) {
                    Register src_r = ((RegisterOperand)val_op).getRegister();
                    val = getRegister(src_r);
                } else {
                    val = handleConst((Const4Operand) val_op, new QuadProgramLocation(method, obj), 1);
                }
                heapStore(base, val, null);
            }
        }
        public void visitBinary(Quad obj) {
            if (obj.getOperator() == Binary.ADD_P.INSTANCE) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register dest_r = Binary.getDest(obj).getRegister();
                Operand src = Binary.getSrc1(obj);
                if (src instanceof RegisterOperand) {
                    RegisterOperand rop = ((RegisterOperand)src);
                    Register src_r = rop.getRegister();
                    setRegister(dest_r, getRegister(src_r));
                } else {
                    Node n = handleConst((Const4Operand) src, new QuadProgramLocation(method, obj));
                    setRegister(dest_r, n);
                }
            }
        }
        /** Visit a type cast check instruction. */
        public void visitCheckCast(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            Register dest_r = CheckCast.getDest(obj).getRegister();
            Operand src = CheckCast.getSrc(obj);
            if (src instanceof RegisterOperand) {
                Register src_r = ((RegisterOperand)src).getRegister();
                Object s = getRegister(src_r);
                if (!include_cast_ops) {
                    setRegister(dest_r, s);
                    return;
                }
                CheckCastNode n = (CheckCastNode)nodeCache.get(obj);
                if (n == null) {
                    n = CheckCastNode.get((jq_Reference)CheckCast.getType(obj).getType(), 
                                          new QuadProgramLocation(method, obj));
                    nodeCache.put(obj, n);
                }
                Collection from = (s instanceof Collection) ? (Collection)s : Collections.singleton(s);
                Iterator i = from.iterator();
                while (i.hasNext()) {
                    Node fn = (Node)i.next();
                    Pair key = new Pair(fn, obj);
                    if (castMap.put(key, n) == null)
                        castPredecessors.add(fn);
                }
                setRegister(dest_r, n);
            } else {
                Node n = handleConst((Const4Operand) src, new QuadProgramLocation(method, obj));
                setRegister(dest_r, n);
            }
        }
        /** Visit a get instance field instruction. */
        public void visitGetfield(Quad obj) {
            if (obj.getOperator() instanceof Operator.Getfield.GETFIELD_A
                || obj.getOperator() instanceof Operator.Getfield.GETFIELD_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register r = Getfield.getDest(obj).getRegister();
                Operand o = Getfield.getBase(obj);
                Getfield.getField(obj).resolve();
                jq_Field f = Getfield.getField(obj).getField();
                if (IGNORE_INSTANCE_FIELDS) f = null;
                ProgramLocation pl = new QuadProgramLocation(method, obj);
                if (o instanceof RegisterOperand) {
                    Register b = ((RegisterOperand)o).getRegister();
                    heapLoad(pl, r, b, f);
                } else {
                    // base is not a register?!
                    Node n = handleConst((Const4Operand) o, pl);
                    heapLoad(pl, r, n, f);
                }
            }
        }
        /** Visit a get static field instruction. */
        public void visitGetstatic(Quad obj) {
            if (obj.getOperator() instanceof Operator.Getstatic.GETSTATIC_A
                || obj.getOperator() instanceof Operator.Getstatic.GETSTATIC_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register r = Getstatic.getDest(obj).getRegister();
                Getstatic.getField(obj).resolve();
                jq_Field f = Getstatic.getField(obj).getField();
                if (IGNORE_STATIC_FIELDS) f = null;
                ProgramLocation pl = new QuadProgramLocation(method, obj);
                heapLoad(pl, r, my_global, f);
            }
        }
        /** Visit a type instance of instruction. */
        public void visitInstanceOf(Quad obj) {
            // skip for now.
        }
        /** Visit an invoke instruction. */
        public void visitInvoke(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            Invoke.getMethod(obj).resolve();
            jq_Method m = Invoke.getMethod(obj).getMethod();
            ProgramLocation mc = new ProgramLocation.QuadProgramLocation(method, obj);
            if (m == joeq.Runtime.Arrays._multinewarray) {
                // special case: multi-dimensional array.
                RegisterOperand dest = Invoke.getDest(obj);
                if (dest != null) {
                    Register dest_r = dest.getRegister();
                    // todo: get the real type.
                    jq_Reference type = PrimordialClassLoader.getJavaLangObject().getArrayTypeForElementType();
                    Node n = ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj));
                    setRegister(dest_r, n);
                }
                return;
            }
            
            this.methodCalls.add(mc);
            jq_Type[] params = m.getParamTypes();
            ParamListOperand plo = Invoke.getParamList(obj);
            Assert._assert(m == joeq.Runtime.Arrays._multinewarray || params.length == plo.length(),
                obj + " calling " + m + ": params.length: " + params.length + ", plo: " + plo);
//            System.out.println("plo: " + plo);
            for (int i=0; i<params.length; ++i) {
                if (!params[i].isReferenceType()
                    /*|| params[i].isAddressType()*/
                    ) continue;
                Assert._assert(plo.get(i) != null, "Element " + i + " of plo " + plo + " is bogus");
                Register r = plo.get(i).getRegister();
                passParameter(r, mc, i);
            }
            if (m.getReturnType().isReferenceType()
                /*&& !m.getReturnType().isAddressType()*/
                )
            {
                if(false /* && PA.getBogusSummaryProvider().getReplacementMethod(m) != null*/) {
//                  special case: replaced methods.
                    RegisterOperand dest = Invoke.getDest(obj);
                    if (dest != null) {
                        Register dest_r = dest.getRegister();
                        // todo: get the real type.                    
                        Node n = ConcreteTypeNode.get((jq_Reference) m.getReturnType(), new QuadProgramLocation(method, obj));
                        setRegister(dest_r, n);
                    }
                } else {
                    RegisterOperand dest = Invoke.getDest(obj);
                    if (dest != null) {
                        Register dest_r = dest.getRegister();
                        ReturnValueNode n = (ReturnValueNode)callToRVN.get(mc);
                        if (n == null) {
                            callToRVN.put(mc, n = new ReturnValueNode(mc));
                            passedAsParameter.add(n);
                        }
                        setRegister(dest_r, n);
                    }
                }
            }
            // exceptions are handled by visitExceptionThrower.
        }
        
        /**
         * Holds the current state at each jsr call.
         */
        HashMap jsr_states;
        /**
         * @see joeq.Compiler.Quad.QuadVisitor#visitJsr(joeq.Compiler.Quad.Quad)
         */
        public void visitJsr(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            if (jsr_states == null) jsr_states = new HashMap();
            BasicBlock succ = Jsr.getSuccessor(obj).getTarget();
            jsr_states.put(succ, this.s);
        }
        
        /** Visit a register move instruction. */
        public void visitMonitor(Quad obj) {
            if (!include_sync_ops) return;
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            Operand src = Monitor.getSrc(obj);
            if (src instanceof RegisterOperand) {
                RegisterOperand rop = ((RegisterOperand)src);
                Register src_r = rop.getRegister();
                monitorOp(obj, src_r);
            } else {
                Node n = handleConst((Const4Operand) src, new QuadProgramLocation(method, obj));
                monitorOp(obj, Collections.singleton(n));
            }
        }
        
        /** Visit a register move instruction. */
        public void visitMove(Quad obj) {
            if (obj.getOperator() instanceof Operator.Move.MOVE_A
                || obj.getOperator() instanceof Operator.Move.MOVE_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register dest_r = Move.getDest(obj).getRegister();
                Operand src = Move.getSrc(obj);
                if (src instanceof RegisterOperand) {
                    RegisterOperand rop = ((RegisterOperand)src);
                    if (rop.getType() instanceof jq_ReturnAddressType) return;
                    Register src_r = rop.getRegister();
                    setRegister(dest_r, getRegister(src_r));
                } else {
                    Node n = handleConst((Const4Operand) src, new QuadProgramLocation(method, obj));
                    setRegister(dest_r, n);
                }
            }
        }
        
        public void visitPhi(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            Set set = NodeSet.FACTORY.makeSet();
            ParamListOperand plo = Phi.getSrcs(obj);
            for (int i=0; i<plo.length(); ++i) {
                RegisterOperand rop = plo.get(i);
                if (rop == null) continue;
                Register r = rop.getRegister();
                Object foo = s.registers[r.getNumber()];
                if (foo instanceof Collection)
                    set.addAll((Collection) foo);
                else if (foo != null)
                    set.add(foo);
                else {
                    // foo is null because it is from a path we haven't seen yet.
                }
            }
            s.registers[Phi.getDest(obj).getRegister().getNumber()] = set;
        }
        
        LoopAnalysis la;
        /** Visit an object allocation instruction. */
        public void visitNew(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            Register dest_r = New.getDest(obj).getRegister();
            jq_Reference type = (jq_Reference)New.getType(obj).getType();
            if (SPLIT_THREADS && type != null) {
                type.prepare();
                jq_Class jlt = PrimordialClassLoader.getJavaLangThread();
                jq_Class jlr = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Runnable;");
                jlt.prepare(); jlr.prepare();
                if (type.isSubtypeOf(jlt) ||
                    type.isSubtypeOf(jlr)) {
                    if (la == null) la = new LoopAnalysis();
                    if (la.isInLoop(method, bb)) {
                        Object key = obj;
                        Pair p = (Pair) nodeCache.get(key);
                        if (p == null) {
                            System.out.println("Found thread creation in loop: "+obj);
                            p = new Pair(ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj)),
                                         ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj)));
                            nodeCache.put(key, p);
                        }
                        setRegister(dest_r, p);
                        return;
                    }
                }
            }
            
            // TODO: special-case allocations of HashMaps
            Node n = ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj));
            setRegister(dest_r, n);
        }
        
        /** Visit an array allocation instruction. */
        public void visitNewArray(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            Register dest_r = NewArray.getDest(obj).getRegister();
            jq_Reference type = (jq_Reference)NewArray.getType(obj).getType();
            Node n = ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj));
            setRegister(dest_r, n);
        }
        
        /** Visit a put instance field instruction. */
        public void visitPutfield(Quad obj) {
            if (obj.getOperator() instanceof Operator.Putfield.PUTFIELD_A
                || obj.getOperator() instanceof Operator.Putfield.PUTFIELD_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Operand base_op = Putfield.getBase(obj);
                Operand val_op = Putfield.getSrc(obj);
                Putfield.getField(obj).resolve();
                jq_Field f = Putfield.getField(obj).getField();
                if (IGNORE_INSTANCE_FIELDS) f = null;
                Object base, val;
                if (val_op instanceof RegisterOperand) {
                    Register src_r = ((RegisterOperand)val_op).getRegister();
                    val = getRegister(src_r);
                } else {
                    val = handleConst((Const4Operand) val_op, new QuadProgramLocation(method, obj), 0);
                }
                if (base_op instanceof RegisterOperand) {
                    Register base_r = ((RegisterOperand)base_op).getRegister();
                    base = getRegister(base_r);
                } else {
                    base = handleConst((Const4Operand) base_op, new QuadProgramLocation(method, obj), 1);
                }
                heapStore(base, val, f);
            }
        }
        /** Visit a put static field instruction. */
        public void visitPutstatic(Quad obj) {
            if (obj.getOperator() instanceof Operator.Putstatic.PUTSTATIC_A
                || obj.getOperator() instanceof Operator.Putstatic.PUTSTATIC_P
                ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Operand val = Putstatic.getSrc(obj);
                Putstatic.getField(obj).resolve();
                jq_Field f = Putstatic.getField(obj).getField();
                if (IGNORE_STATIC_FIELDS) f = null;
                if (val instanceof RegisterOperand) {
                    Register src_r = ((RegisterOperand)val).getRegister();
                    heapStore(my_global, getRegister(src_r), f);
                } else {
                    Node n = handleConst((Const4Operand) val, new QuadProgramLocation(method, obj));
                    heapStore(my_global, n, f);
                }
            }
        }
        
        static void addToSet(Set s, Object o) {
            if (o instanceof Set) s.addAll((Set)o);
            else if (o != null) s.add(o);
        }
        
        /** Visit a return/throw instruction. */
        public void visitReturn(Quad obj) {
            Operand src = Return.getSrc(obj);
            Set r;
            if (obj.getOperator() == Return.RETURN_A.INSTANCE
                || obj.getOperator() == Return.RETURN_P.INSTANCE
                ) r = returned;
            else if (obj.getOperator() == Return.THROW_A.INSTANCE) r = thrown;
            else return;
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            if (src instanceof RegisterOperand) {
                Register src_r = ((RegisterOperand)src).getRegister();
                addToSet(r, getRegister(src_r));
            } else {
                Node n = handleConst((Const4Operand) src, new QuadProgramLocation(method, obj));
                r.add(n);
            }
        }
        
        static void setAsEscapes(Object o) {
            if (o instanceof Set) {
                for (Iterator i=((Set)o).iterator(); i.hasNext(); ) {
                    ((Node)i.next()).escapes = true;
                }
            } else {
                ((Node)o).escapes = true;
            }
        }
        
        public void visitSpecial(Quad obj) {
            if (obj.getOperator() == Special.GET_THREAD_BLOCK.INSTANCE) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register dest_r = ((RegisterOperand)Special.getOp1(obj)).getRegister();
                jq_Reference type = (jq_Reference) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Scheduler/jq_Thread;");
                Node n = ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj));
                n.setEscapes();
                setRegister(dest_r, n);
            } else if (obj.getOperator() == Special.SET_THREAD_BLOCK.INSTANCE) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register src_r = ((RegisterOperand)Special.getOp2(obj)).getRegister();
                setAsEscapes(getRegister(src_r));
            } else if (obj.getOperator() == Special.GET_STACK_POINTER.INSTANCE ||
                    obj.getOperator() == Special.GET_BASE_POINTER.INSTANCE ||
                    obj.getOperator() == Special.ALLOCA.INSTANCE ) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register dest_r = ((RegisterOperand)Special.getOp1(obj)).getRegister();
                jq_Reference type = StackAddress._class;
                Node n = ConcreteTypeNode.get(type, new QuadProgramLocation(method, obj));
                //n.setEscapes();
                setRegister(dest_r, n);
                /*
            } else if (obj.getOperator() == Special.GET_TYPE_OF.INSTANCE) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register dest_r = ((RegisterOperand)Special.getOp1(obj)).getRegister();
                jq_Reference type = jq_Reference._class;
                UnknownTypeNode n = UnknownTypeNode.get(type);
                setRegister(dest_r, n);
                */
            }
        }
        public void visitUnary(Quad obj) {
            if (obj.getOperator() == Unary.OBJECT_2ADDRESS.INSTANCE ||
                obj.getOperator() == Unary.ADDRESS_2OBJECT.INSTANCE) {
                if (TRACE_INTRA) out.println("Visiting: "+obj);
                Register dest_r = Unary.getDest(obj).getRegister();
                Operand src = Unary.getSrc(obj);
                if (src instanceof RegisterOperand) {
                    RegisterOperand rop = ((RegisterOperand)src);
                    Register src_r = rop.getRegister();
                    setRegister(dest_r, getRegister(src_r));
                } else {
                    Node n = handleConst((Const4Operand) src, new QuadProgramLocation(method, obj));
                    setRegister(dest_r, n);
                }
            } else if (obj.getOperator() == Unary.INT_2ADDRESS.INSTANCE) {
                Register dest_r = Unary.getDest(obj).getRegister();
                jq_Reference type = Address._class;
                UnknownTypeNode n = UnknownTypeNode.get(type);
                setRegister(dest_r, n);
            }
        }
        public void visitExceptionThrower(Quad obj) {
            if (TRACE_INTRA) out.println("Visiting: "+obj);
            // special case for method invocation.
            if (obj.getOperator() instanceof Invoke) {
                Invoke.getMethod(obj).resolve();
                //jq_Method m = Invoke.getMethod(obj).getMethod();
                ProgramLocation mc = new ProgramLocation.QuadProgramLocation(method, obj);
                ThrownExceptionNode n = (ThrownExceptionNode) callToTEN.get(mc);
                if (n == null) {
                    callToTEN.put(mc, n = ThrownExceptionNode.get(mc));
                    passedAsParameter.add(n);
                }
                joeq.Util.Templates.ListIterator.ExceptionHandler eh = bb.getExceptionHandlers().exceptionHandlerIterator();
                while (eh.hasNext()) {
                    ExceptionHandler h = eh.nextExceptionHandler();
                    this.mergeWith(h);
                    Register r = null;
                    if (h.getEntry().size() > 0) {
                        for (Iterator ge = h.getEntry().iterator(); ge.hasNext();) {
                            Quad q = (Quad) ge.next();
                            if (q.getOperator() instanceof Special.GET_EXCEPTION) {
                                r = ((RegisterOperand) Special.getOp1(q)).getRegister();
                                break;
                            }
                        }
                    }
                    if (r == null) {
                        // hmmm, the handler doesn't start with GET_EXCEPTION, 
                        // so just assume it is the first stack register.
                        // this hack doesn't work with SSA.
                        Assert._assert(!SSA);
                        r = rf.getOrCreateStack(0, PrimordialClassLoader.getJavaLangObject());
                    }
                    this.start_states[h.getEntry().getID()].merge(r.getNumber(), n);
                    if (h.mustCatch(PrimordialClassLoader.getJavaLangThrowable()))
                        return;
                }
                this.thrown.add(n);
                return;
            }
            joeq.Util.Templates.ListIterator.jq_Class xs = obj.getThrownExceptions().classIterator();
            while (xs.hasNext()) {
                jq_Class x = xs.nextClass();
                UnknownTypeNode n = UnknownTypeNode.get(x);
                joeq.Util.Templates.ListIterator.ExceptionHandler eh = bb.getExceptionHandlers().exceptionHandlerIterator();
                boolean caught = false;
                while (eh.hasNext()) {
                    ExceptionHandler h = eh.nextExceptionHandler();
                    if (h.mayCatch(x)) {
                        this.mergeWith(h);
                        Register r = null;
                        if (h.getEntry().size() > 0) {
                            for (Iterator ge = h.getEntry().iterator(); ge.hasNext();) {
                                Quad q = (Quad) ge.next();
                                if (q.getOperator() instanceof Special.GET_EXCEPTION) {
                                    r = ((RegisterOperand) Special.getOp1(q)).getRegister();
                                    break;
                                }
                            }
                        }
                        if (r == null) {
                            // hmmm, the handler doesn't start with GET_EXCEPTION, 
                            // so just assume it is the first stack register.
                            // this hack doesn't work with SSA.
                            Assert._assert(!SSA);
                            r = rf.getOrCreateStack(0, PrimordialClassLoader.getJavaLangObject());
                        }
                        this.start_states[h.getEntry().getID()].merge(r.getNumber(), n);
                    }
                    if (h.mustCatch(x)) {
                        caught = true;
                        break;
                    }
                }
                if (!caught) this.thrown.add(n);
            }
        }
        
    }
    
    /** Represents a particular parameter passed to a particular method call. */
    public static class PassedParameter implements Textualizable {
        final ProgramLocation m; final int paramNum;
        public PassedParameter(ProgramLocation m, int paramNum) {
            this.m = m; this.paramNum = paramNum;
        }
        public ProgramLocation getCall() { return m; }
        public int getParamNum() { return paramNum; }
        public int hashCode() {
            return m.hashCode() ^ paramNum;
        }
        public boolean equals(PassedParameter that) { return this.m.equals(that.m) && this.paramNum == that.paramNum; }
        public boolean equals(Object o) { if (o instanceof PassedParameter) return equals((PassedParameter)o); return false; }
        public String toString() { return "Param "+paramNum+" for "+m; }
        public void write(Textualizer t) throws IOException {
            m.write(t);
            t.writeString(" "+paramNum);
        }
        public void writeEdges(Textualizer t) throws IOException { }
        public void addEdge(String edgeName, Textualizable t) { }
        public static PassedParameter read(StringTokenizer st) {
            ProgramLocation l = ProgramLocation.read(st);
            int k = Integer.parseInt(st.nextToken());
            return new PassedParameter(l, k);
        }
    }
    
    /** Represents a particular call site in a method. */
    public static class CallSite {
        final MethodSummary caller; final ProgramLocation m;
        public CallSite(MethodSummary caller, ProgramLocation m) {
            this.caller = caller; this.m = m;
        }
        public MethodSummary getCaller() { return caller; }
        public ProgramLocation getLocation() { return m; }
        public int hashCode() { return (caller == null?0x0:caller.hashCode()) ^ m.hashCode(); }
        public boolean equals(CallSite that) { return this.m.equals(that.m) && this.caller == that.caller; }
        public boolean equals(Object o) { if (o instanceof CallSite) return equals((CallSite)o); return false; }
        public String toString() { return (caller!=null?caller.getMethod():null)+" "+m.getID()+" "+(m.getTargetMethod()!=null?m.getTargetMethod().getName():null); }
    }
    
    /** Represents a field edge between two nodes. */
    /*
    public static class Edge {
        // Node source;
        Node dest;
        jq_Field field;
        public Edge(Node source, Node dest, jq_Field field) {
            //this.source = source;
            this.dest = dest; this.field = field;
        }
        
        private static Edge INSTANCE = new Edge(null, null, null);
        
        private static Edge get(Node source, Node dest, jq_Field field) {
            //INSTANCE.source = source;
            INSTANCE.dest = dest; INSTANCE.field = field;
            return INSTANCE;
        }
        
        public int hashCode() {
            return 
                // source.hashCode() ^
                dest.hashCode() ^ ((field==null)?0x1ee7:field.hashCode());
        }
        public boolean equals(Edge that) {
            return this.field == that.field &&
                // this.source.equals(that.source) &&
                this.dest.equals(that.dest);
        }
        public boolean equals(Object o) {
            return equals((Edge)o);
        }
        public String toString() {
            return
                //source+
                "-"+((field==null)?"[]":field.getName().toString())+"->"+dest;
        }
    }
    */
    
    public static class InsideEdgeNavigator implements Navigator {

        /* (non-Javadoc)
         * @see jwutil.graphs.Navigator#next(java.lang.Object)
         */
        public Collection next(Object node) {
            Node n = (Node) node;
            return n.getNonEscapingEdgeTargets();
        }

        /* (non-Javadoc)
         * @see jwutil.graphs.Navigator#prev(java.lang.Object)
         */
        public Collection prev(Object node) {
            Node n = (Node) node;
            return n.getPredecessorTargets();
        }
        
    }
    
    public static interface Variable {}
    public static interface HeapObject {
        ProgramLocation getLocation();
        jq_Reference getDeclaredType();
    }
    
    public abstract static class Node implements Textualizable, Comparable, Variable {
        /** Map from fields to sets of predecessors on that field. 
         *  This only includes inside edges; outside edge predecessors are in FieldNode. */
        protected Map predecessors;
        /** Set of passed parameters for this node. */
        protected Set passedParameters;
        /** Map from fields to sets of inside edges from this node on that field. */
        protected Map addedEdges;
        /** Map from fields to sets of outside edges from this node on that field. */
        protected Map accessPathEdges;
        /** Unique id number. */
        public final int id;
        /** Whether or not this node escapes into some unanalyzable code. */
        private boolean escapes;
        
        public static boolean TRACK_REASONS = false;
        
        /** Maps added edges to the quads that they come from.
            Only used if TRACK_REASONS is true. */
        //HashMap edgesToReasons;
        
        public static int numberOfNodes() { return current_id; }
        private static int current_id = 0;
        
        protected Node() { this.id = ++current_id; }
        protected Node(Node that) {
            this.predecessors = that.predecessors;
            this.passedParameters = that.passedParameters;
            this.addedEdges = that.addedEdges;
            this.accessPathEdges = that.accessPathEdges;
            this.id = ++current_id;
            this.escapes = that.escapes;
            //if (TRACK_REASONS) this.edgesToReasons = that.edgesToReasons;
        }
        
        public int hashCode() {
            if (USE_IDENTITY_HASHCODE)
                return System.identityHashCode(this);
            else
                return id;
        }
        
        public final int compareTo(Node that) {
            if (this.id > that.id) return 1;
            else if (this.id == that.id) return 0;
            else return -1;
        }
        public final int compareTo(Object o) {
            return compareTo((Node)o);
        }
        
        public boolean isPassedAsParameter() {
            return passedParameters != null;
        }
        public Set getPassedParameters() {
            return passedParameters;
        }
        
        /** Replace this node by the given set of nodes.  All inside and outside
         *  edges to and from this node are replaced by sets of edges to and from
         *  the nodes in the set.  The passed parameter set of this node is also
         *  added to every node in the given set. */
        public void replaceBy(Set set, boolean removeSelf) {
            if (TRACE_INTRA) out.println("Replacing "+this+" with "+set+(removeSelf?", and removing self":""));
            if (set.contains(this)) {
                if (TRACE_INTRA) out.println("Replacing a node with itself, turning off remove self.");
                set.remove(this);
                if (set.isEmpty()) {
                    if (TRACE_INTRA) out.println("Replacing a node with only itself! Nothing to do.");
                    return;
                }
                removeSelf = false;
            }
            if (VERIFY_ASSERTIONS) Assert._assert(!set.contains(this));
            if (this.predecessors != null) {
                for (Iterator i=this.predecessors.entrySet().iterator(); i.hasNext(); ) {
                    java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                    jq_Field f = (jq_Field)e.getKey();
                    Object o = e.getValue();
                    if (removeSelf)
                        i.remove();
                    if (TRACE_INTRA) out.println("Looking at predecessor on field "+f+": "+o);
                    if (o == null) continue;
                    if (o instanceof Node) {
                        Node that = (Node)o;
                        //Object q = null;
                        //if (TRACK_REASONS && edgesToReasons != null)
                        //    q = edgesToReasons.get(Edge.get(that, this, f));
                        if (removeSelf)
                            that._removeEdge(f, this);
                        if (that == this) {
                            // add self-cycles on f to all nodes in set.
                            if (TRACE_INTRA) out.println("Adding self-cycles on field "+f);
                            for (Iterator j=set.iterator(); j.hasNext(); ) {
                                Node k = (Node)j.next();
                                k.addEdge(f, k);
                            }
                        } else {
                            for (Iterator j=set.iterator(); j.hasNext(); ) {
                                that.addEdge(f, (Node)j.next());
                            }
                        }
                    } else {
                        for (Iterator k=((Set)o).iterator(); k.hasNext(); ) {
                            Node that = (Node)k.next();
                            if (removeSelf) {
                                k.remove();
                                that._removeEdge(f, this);
                            }
                            //Object q = null;
                            //if (TRACK_REASONS && edgesToReasons != null)
                            //    q = edgesToReasons.get(Edge.get(that, this, f));
                            if (that == this) {
                                // add self-cycles on f to all mapped nodes.
                                if (TRACE_INTRA) out.println("Adding self-cycles on field "+f);
                                for (Iterator j=set.iterator(); j.hasNext(); ) {
                                    Node k2 = (Node)j.next();
                                    k2.addEdge(f, k2);
                                }
                            } else {
                                for (Iterator j=set.iterator(); j.hasNext(); ) {
                                    that.addEdge(f, (Node)j.next());
                                }
                            }
                        }
                    }
                }
            }
            if (this.addedEdges != null) {
                for (Iterator i=this.addedEdges.entrySet().iterator(); i.hasNext(); ) {
                    java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                    jq_Field f = (jq_Field)e.getKey();
                    Object o = e.getValue();
                    if (removeSelf)
                        i.remove();
                    if (o == null) continue;
                    if (TRACE_INTRA) out.println("Looking at successor on field "+f+": "+o);
                    if (o instanceof Node) {
                        Node that = (Node)o;
                        if (that == this) continue; // cyclic edges handled above.
                        //Object q = (TRACK_REASONS && edgesToReasons != null) ? edgesToReasons.get(Edge.get(this, that, f)) : null;
                        if (removeSelf) {
                            boolean b = that.removePredecessor(f, this);
                            if (TRACE_INTRA) out.println("Removed "+this+" from predecessor set of "+that+"."+f);
                            Assert._assert(b);
                        }
                        for (Iterator j=set.iterator(); j.hasNext(); ) {
                            Node node2 = (Node)j.next();
                            node2.addEdge(f, that);
                        }
                    } else {
                        for (Iterator k=((Set)o).iterator(); k.hasNext(); ) {
                            Node that = (Node)k.next();
                            if (removeSelf)
                                k.remove();
                            if (that == this) continue; // cyclic edges handled above.
                            //Object q = (TRACK_REASONS && edgesToReasons != null) ? edgesToReasons.get(Edge.get(this, that, f)) : null;
                            if (removeSelf) {
                                boolean b = that.removePredecessor(f, this);
                                if (TRACE_INTRA) out.println("Removed "+this+" from predecessor set of "+that+"."+f);
                                Assert._assert(b);
                            }
                            for (Iterator j=set.iterator(); j.hasNext(); ) {
                                Node node2 = (Node)j.next();
                                node2.addEdge(f, that);
                            }
                        }
                    }
                }
            }
            if (this.accessPathEdges != null) {
                for (Iterator i=this.accessPathEdges.entrySet().iterator(); i.hasNext(); ) {
                    java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                    jq_Field f = (jq_Field)e.getKey();
                    Object o = e.getValue();
                    if (removeSelf)
                        i.remove();
                    if (o == null) continue;
                    if (TRACE_INTRA) out.println("Looking at access path successor on field "+f+": "+o);
                    if (o instanceof FieldNode) {
                        FieldNode that = (FieldNode)o;
                        if (that == this) continue; // cyclic edges handled above.
                        if (removeSelf) {
                            that.field_predecessors.remove(this);
                            if (TRACE_INTRA) out.println("Removed "+this+" from access path predecessor set of "+that);
                        }
                        for (Iterator j=set.iterator(); j.hasNext(); ) {
                            Node node2 = (Node)j.next();
                            if (TRACE_INTRA) out.println("Adding access path edge "+node2+"->"+that);
                            node2.addAccessPathEdge(f, that);
                        }
                    } else {
                        for (Iterator k=((Set)o).iterator(); k.hasNext(); ) {
                            FieldNode that = (FieldNode)k.next();
                            if (removeSelf)
                                k.remove();
                            if (that == this) continue; // cyclic edges handled above.
                            if (removeSelf)
                                that.field_predecessors.remove(this);
                            for (Iterator j=set.iterator(); j.hasNext(); ) {
                                Node node2 = (Node)j.next();
                                node2.addAccessPathEdge(f, that);
                            }
                        }
                    }
                }
            }
            if (this.passedParameters != null) {
                if (TRACE_INTRA) out.println("Node "+this+" is passed as parameters: "+this.passedParameters+", adding those parameters to "+set);
                for (Iterator i=this.passedParameters.iterator(); i.hasNext(); ) {
                    PassedParameter pp = (PassedParameter)i.next();
                    for (Iterator j=set.iterator(); j.hasNext(); ) {
                        ((Node)j.next()).recordPassedParameter(pp);
                    }
                }
            }
        }
        
        /** Helper function to update map m given an update map um. */
        static void updateMap(Map um, Iterator i, Map m) {
            while (i.hasNext()) {
                java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                Object f = e.getKey();
                Object o = e.getValue();
                if (o == null) continue;
                if (o instanceof Node) {
                    Object q = um.get(o);
                    if (o instanceof UnknownTypeNode) q = o;
                    if (o == GlobalNode.GLOBAL) q = o;
                    if (VERIFY_ASSERTIONS) Assert._assert(q != null, o+" is missing from map");
                    if (TRACE_INTRA) out.println("Updated edge "+f+" "+o+" to "+q);
                    m.put(f, q);
                } else {
                    Set lhs = NodeSet.FACTORY.makeSet();
                    m.put(f, lhs);
                    for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                        Object r = j.next();
                        Assert._assert(r != null);
                        Object q = um.get(r);
                        if (r instanceof UnknownTypeNode) q = r;
                        if (r == GlobalNode.GLOBAL) q = o;
                        if (VERIFY_ASSERTIONS) Assert._assert(q != null, r+" is missing from map");
                        if (TRACE_INTRA) out.println("Updated edge "+f+" "+r+" to "+q);
                        lhs.add(q);
                    }
                }
            }
        }
        
        static void addGlobalEdges(Node n) {
            if (n.predecessors != null) {
                for (Iterator i=n.predecessors.entrySet().iterator(); i.hasNext(); ) {
                    java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                    jq_Field f = (jq_Field)e.getKey();
                    Object o = e.getValue();
                    if (o == GlobalNode.GLOBAL) {
                        GlobalNode.GLOBAL.addEdge(f, n);
                    } else if (o instanceof UnknownTypeNode) {
                        ((UnknownTypeNode)o).addEdge(f, n);
                    } else if (o instanceof Set) {
                        for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                            Object r = j.next();
                            if (r == GlobalNode.GLOBAL) {
                                GlobalNode.GLOBAL.addEdge(f, n);
                            } else if (r instanceof UnknownTypeNode) {
                                ((UnknownTypeNode)r).addEdge(f, n);
                            }
                        }
                    }
                }
            }
            if (n.addedEdges != null) {
                for (Iterator i=n.addedEdges.entrySet().iterator(); i.hasNext(); ) {
                    java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                    jq_Field f = (jq_Field)e.getKey();
                    Object o = e.getValue();
                    if (o instanceof UnknownTypeNode) {
                        n.addEdge(f, (UnknownTypeNode)o);
                    } else if (o instanceof Set) {
                        for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                            Object r = j.next();
                            if (r instanceof UnknownTypeNode) {
                                n.addEdge(f, (UnknownTypeNode)r);
                            }
                        }
                    }
                }
            }
        }
        
        static void updateMap_unknown(Map um, Iterator i, Map m) {
            while (i.hasNext()) {
                java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                jq_Field f = (jq_Field)e.getKey();
                Object o = e.getValue();
                if (o == null) continue;
                if (o instanceof Node) {
                    Object q = um.get(o);
                    if (q == null) q = o;
                    else if (TRACE_INTRA) out.println("Updated edge "+f+" "+o+" to "+q);
                    m.put(f, q);
                } else {
                    Set lhs = NodeSet.FACTORY.makeSet();
                    m.put(f, lhs);
                    for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                        Object r = j.next();
                        Assert._assert(r != null);
                        Object q = um.get(r);
                        if (q == null) q = r;
                        else if (TRACE_INTRA) out.println("Updated edge "+f+" "+r+" to "+q);
                        lhs.add(q);
                    }
                }
            }
        }
        
        /** Update all predecessor and successor nodes with the given update map.
         *  Also clones the passed parameter set.
         */
        public void update(HashMap um) {
            if (TRACE_INTRA) out.println("Updating edges for node "+this.toString_long());
            Map m = this.predecessors;
            if (m != null) {
                this.predecessors = new LinkedHashMap();
                updateMap(um, m.entrySet().iterator(), this.predecessors);
            }
            m = this.addedEdges;
            if (m != null) {
                this.addedEdges = new LinkedHashMap();
                updateMap(um, m.entrySet().iterator(), this.addedEdges);
            }
            m = this.accessPathEdges;
            if (m != null) {
                this.accessPathEdges = new LinkedHashMap();
                updateMap(um, m.entrySet().iterator(), this.accessPathEdges);
            }
            if (this.passedParameters != null) {
                Set pp = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
                pp.addAll(this.passedParameters);
                this.passedParameters = pp;
            }
            addGlobalEdges(this);
        }
        
        /** Return the declared type of this node. */
        public abstract jq_Reference getDeclaredType();
        
        /** Return the method that this node is defined in, null if it
         * doesn't come from a method.
         */
        public abstract jq_Method getDefiningMethod();
        
        /** Return a shallow copy of this node. */
        public abstract Node copy();
        
        public boolean hasPredecessor(jq_Field f, Node n) {
            Object o = this.predecessors.get(f);
            if (o instanceof Node) {
                if (n != o) {
                    Assert.UNREACHABLE("predecessor of "+this+" should be "+n+", but is "+o);
                    return false;
                }
            } else if (o == null) {
                Assert.UNREACHABLE("predecessor of "+this+" should be "+n+", but is missing");
                return false;
            } else {
                Set s = (Set) o;
                if (!s.contains(n)) {
                    Assert.UNREACHABLE("predecessor of "+this+" should be "+n);
                    return false;
                }
            }
            return true;
        }

        /** Remove the given predecessor node on the given field from the predecessor set.
         *  Returns true if that predecessor existed, false otherwise. */
        public boolean removePredecessor(jq_Field m, Node n) {
            if (predecessors == null) return false;
            Object o = predecessors.get(m);
            if (o instanceof Set) return ((Set)o).remove(n);
            else if (o == n) { predecessors.remove(m); return true; }
            else return false;
        }
        /** Add the given predecessor node on the given field to the predecessor set.
         *  Returns true if that predecessor didn't already exist, false otherwise. */
        public boolean addPredecessor(jq_Field m, Node n) {
            if (predecessors == null) predecessors = new LinkedHashMap();
            Object o = predecessors.get(m);
            if (o == null) {
                predecessors.put(m, n);
                return true;
            }
            if (o instanceof Set) return ((Set)o).add(n);
            if (o == n) return false;
            Set s = NodeSet.FACTORY.makeSet(); s.add(o); s.add(n);
            predecessors.put(m, s);
            return true;
        }
        
        /** Return a set of Map.Entry objects corresponding to the incoming inside edges
         *  of this node. */
        public Set getPredecessors() {
            if (predecessors == null) return Collections.EMPTY_SET;
            return predecessors.entrySet();
        }
        
        public Collection getPredecessorTargets() {
            if (predecessors == null) return Collections.EMPTY_SET;
            return new FlattenedCollection(predecessors.values());
        }
        
        /** Record the given passed parameter in the set for this node.
         *  Returns true if that passed parameter didn't already exist, false otherwise. */
        public boolean recordPassedParameter(PassedParameter cm) {
            if (passedParameters == null) passedParameters = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            return passedParameters.add(cm);
        }
        /** Record the passed parameter of the given method call and argument number in
         *  the set for this node.
         *  Returns true if that passed parameter didn't already exist, false otherwise. */
        public boolean recordPassedParameter(ProgramLocation m, int paramNum) {
            if (passedParameters == null) passedParameters = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            PassedParameter cm = new PassedParameter(m, paramNum);
            return passedParameters.add(cm);
        }
        private boolean _removeEdge(jq_Field m, Node n) {
            Object o = addedEdges.get(m);
            if (o instanceof Set) return ((Set)o).remove(n);
            else if (o == n) { addedEdges.remove(m); return true; }
            else return false;
        }
        /** Remove the given successor node on the given field from the inside edge set.
         *  Also removes the predecessor link from the successor node to this node.
         *  Returns true if that edge existed, false otherwise. */
        public boolean removeEdge(jq_Field m, Node n) {
            if (addedEdges == null) return false;
            n.removePredecessor(m, this);
            return _removeEdge(m, n);
        }
        public boolean hasNonEscapingEdge(jq_Field m, Node n) {
            if (addedEdges == null) return false;
            Object o = addedEdges.get(m);
            if (o == n) return true;
            if (o instanceof Set) {
                return ((Set)o).contains(n);
            }
            return false;
        }
        /** Add the given successor node on the given field to the inside edge set.
         *  Also adds a predecessor link from the successor node to this node.
         *  Returns true if that edge didn't already exist, false otherwise. */
        public boolean addEdge(jq_Field m, Node n) {
            //if (TRACK_REASONS) {
            //    if (edgesToReasons == null) edgesToReasons = new HashMap();
                //if (!edgesToReasons.containsKey(Edge.get(this, n, m)))
            //        edgesToReasons.put(new Edge(this, n, m), q);
            //}
            n.addPredecessor(m, this);
            if (addedEdges == null) addedEdges = new LinkedHashMap();
            Object o = addedEdges.get(m);
            if (o == null) {
                addedEdges.put(m, n);
                return true;
            }
            if (o instanceof Set) {
                return ((Set)o).add(n);
            }
            if (o == n) {
                return false;
            }
            Set s = NodeSet.FACTORY.makeSet(); s.add(o); s.add(n);
            addedEdges.put(m, s);
            return true;
        }
        /** Add the given set of successor nodes on the given field to the inside edge set.
         *  The given set is consumed.
         *  Also adds predecessor links from the successor nodes to this node.
         *  Returns true if the inside edge set changed, false otherwise. */
        public boolean addEdges(jq_Field m, Set s) {
            //if (TRACK_REASONS) {
            //    if (edgesToReasons == null) edgesToReasons = new HashMap();
            //}
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                Node n = (Node)i.next();
                //if (TRACK_REASONS) {
                    //if (!edgesToReasons.containsKey(Edge.get(this, n, m)))
                //        edgesToReasons.put(new Edge(this, n, m), q);
                //}
                n.addPredecessor(m, this);
            }
            if (addedEdges == null) addedEdges = new LinkedHashMap();
            Object o = addedEdges.get(m);
            if (o == null) {
                addedEdges.put(m, s);
                return true;
            }
            if (o instanceof Set) {
                return ((Set)o).addAll(s);
            }
            addedEdges.put(m, s); return s.add(o); 
        }
        /** Add the given successor node on the given field to the inside edge set
         *  of all of the given set of nodes.
         *  Also adds predecessor links from the successor node to the given nodes.
         *  Returns true if anything was changed, false otherwise. */
        public static boolean addEdges(Set s, jq_Field f, Node n) {
            boolean b = false;
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                Node a = (Node)i.next();
                if (a.addEdge(f, n))
                    b = true;
            }
            return b;
        }
        
        private boolean _removeAccessPathEdge(jq_Field m, FieldNode n) {
            Object o = accessPathEdges.get(m);
            if (o instanceof Set) return ((Set)o).remove(n);
            else if (o == n) { accessPathEdges.remove(m); return true; }
            else return false;
        }
        /** Remove the given successor node on the given field from the outside edge set.
         *  Also removes the predecessor link from the successor node to this node.
         *  Returns true if that edge existed, false otherwise. */
        public boolean removeAccessPathEdge(jq_Field m, FieldNode n) {
            if (accessPathEdges == null) return false;
            if (n.field_predecessors != null) n.field_predecessors.remove(this);
            return _removeAccessPathEdge(m, n);
        }
        public boolean hasAccessPathEdge(jq_Field m, Node n) {
            if (accessPathEdges == null) return false;
            Object o = accessPathEdges.get(m);
            if (o == n) return true;
            if (o instanceof Set) {
                return ((Set)o).contains(n);
            }
            return false;
        }
        /** Add the given successor node on the given field to the outside edge set.
         *  Also adds a predecessor link from the successor node to this node.
         *  Returns true if that edge didn't already exist, false otherwise. */
        public boolean addAccessPathEdge(jq_Field m, FieldNode n) {
            if (n.field_predecessors == null) n.field_predecessors = NodeSet.FACTORY.makeSet();
            n.field_predecessors.add(this);
            if (accessPathEdges == null) accessPathEdges = new LinkedHashMap();
            Object o = accessPathEdges.get(m);
            if (o == null) {
                accessPathEdges.put(m, n);
                return true;
            }
            if (o instanceof Set) return ((Set)o).add(n);
            if (o == n) return false;
            Set s = NodeSet.FACTORY.makeSet(); s.add(o); s.add(n);
            accessPathEdges.put(m, s);
            return true;
        }
        /** Add the given set of successor nodes on the given field to the outside edge set.
         *  The given set is consumed.
         *  Also adds predecessor links from the successor nodes to this node.
         *  Returns true if the inside edge set changed, false otherwise. */
        public boolean addAccessPathEdges(jq_Field m, Set s) {
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                FieldNode n = (FieldNode)i.next();
                if (n.field_predecessors == null) n.field_predecessors = NodeSet.FACTORY.makeSet();
                n.field_predecessors.add(this);
            }
            if (accessPathEdges == null) accessPathEdges = new LinkedHashMap();
            Object o = accessPathEdges.get(m);
            if (o == null) {
                accessPathEdges.put(m, s);
                return true;
            }
            if (o instanceof Set) return ((Set)o).addAll(s);
            accessPathEdges.put(m, s); return s.add(o); 
        }
        
        /** Add the nodes that are targets of inside edges on the given field
         *  to the given result set. */
        public final void getAllEdges(jq_Field m, Set result) {
            if (addedEdges != null) {
                Object o = addedEdges.get(m);
                if (o != null) {
                    if (o instanceof Set) {
                        result.addAll((Set)o);
                    } else {
                        result.add(o);
                    }
                }
            }
            if (this.escapes)
                getEdges_escaped(m, result);
        }
        
        public final Set getAllEdges(jq_Field m) {
            if (addedEdges != null) {
                Object o = addedEdges.get(m);
                if (o != null) {
                    if (o instanceof Set) {
                        Set s = NodeSet.FACTORY.makeSet((Set)o);
                        if (this.escapes)
                            getEdges_escaped(m, s);
                        return s;
                    } else {
                        if (this.escapes) {
                            Set s = NodeSet.FACTORY.makeSet(2);
                            s.add(o);
                            getEdges_escaped(m, s);
                            return s;
                        }
                        return Collections.singleton(o);
                    }
                }
            }
            if (this.escapes) {
                Set s = NodeSet.FACTORY.makeSet(1);
                getEdges_escaped(m, s);
                return s;
            }
            return Collections.EMPTY_SET;
        }
        
        public final Set getAllEdges() {
            if (this.escapes) {
                jq_Reference type = getDeclaredType();
                Set result = new LinkedHashSet();
                if (type instanceof jq_Class) {
                    jq_Class c = (jq_Class) type;
                    c.prepare();
                    for (Iterator i = Arrays.asList(c.getInstanceFields()).iterator();
                         i.hasNext(); ) {
                        final jq_InstanceField f = (jq_InstanceField) i.next();
                        if (!f.getType().isReferenceType()) continue;
                        final Set r = NodeSet.FACTORY.makeSet();
                        getEdges_escaped(f, r);
                        result.add(new Map.Entry() {
                            public Object getKey() {
                                return f;
                            }
                            public Object getValue() {
                                return r;
                            }
                            public Object setValue(Object value) {
                                throw new UnsupportedOperationException();
                            }
                        });
                    }
                }
                if (addedEdges != null) {
                    result.addAll(addedEdges.entrySet());
                }
                return result;
            }
            if (addedEdges != null) {
                return addedEdges.entrySet();
            }
            return Collections.EMPTY_SET;
        }
        
        public final Set getNonEscapingEdges(jq_Field m) {
            if (addedEdges == null) return Collections.EMPTY_SET;
            Object o = addedEdges.get(m);
            if (o == null) return Collections.EMPTY_SET;
            if (o instanceof Set) {
                return (Set)o;
            } else {
                return Collections.singleton(o);
            }
        }
        
        /** Add the nodes that are targets of inside edges on the given field
         *  to the given result set. */
        public void getEdges_escaped(jq_Field m, Set result) {
            if (TRACE_INTER) out.println("Getting escaped edges "+this+"."+m);
            jq_Reference type = this.getDeclaredType();
            if (m == null) {
                if (type != null && (type.isArrayType() || type == PrimordialClassLoader.getJavaLangObject()))
                    result.add(UnknownTypeNode.get(PrimordialClassLoader.getJavaLangObject()));
                return;
            }
            if (type != null) {
                type.prepare();
                m.getDeclaringClass().prepare();
                if (TypeCheck.isAssignable((jq_Type)type, (jq_Type)m.getDeclaringClass()) ||
                    TypeCheck.isAssignable((jq_Type)m.getDeclaringClass(), (jq_Type)type)) {
                    jq_Reference r = (jq_Reference)m.getType();
                    result.add(UnknownTypeNode.get(r));
                } else {
                    if (TRACE_INTER) out.println("Object of type "+type+" cannot possibly have field "+m);
                }
            }
            if (TRACE_INTER) out.println("New result: "+result);
        }
        
        /** Return a set of Map.Entry objects corresponding to the inside edges
         *  of this node. */
        public Set getNonEscapingEdges() {
            if (addedEdges == null) return Collections.EMPTY_SET;
            return addedEdges.entrySet();
        }

        /** Return the set of fields that this node has inside edges with. */
        public Set getNonEscapingEdgeFields() {
            if (addedEdges == null) return Collections.EMPTY_SET;
            return addedEdges.keySet();
        }
        
        /** Return the collection of target nodes that this node has inside
         * edges with. */
        public Collection getNonEscapingEdgeTargets() {
            if (addedEdges == null) return Collections.EMPTY_SET;
            return new FlattenedCollection(addedEdges.values());
        }
        
        /** Returns true if this node has any added inside edges. */
        public boolean hasNonEscapingEdges() {
            return addedEdges != null;
        }
        
        /** Returns true if this node has any added outside edges. */
        public boolean hasAccessPathEdges() {
            return accessPathEdges != null;
        }
        
        public final Set getAccessPathEdges(jq_Field m) {
            if (accessPathEdges == null) return Collections.EMPTY_SET;
            Object o = accessPathEdges.get(m);
            if (o == null) return Collections.EMPTY_SET;
            if (o instanceof Set) {
                return (Set)o;
            } else {
                return Collections.singleton(o);
            }
        }
        
        /** Add the nodes that are targets of outside edges on the given field
         *  to the given result set. */
        public void getAccessPathEdges(jq_Field m, Set result) {
            if (accessPathEdges == null) return;
            Object o = accessPathEdges.get(m);
            if (o == null) return;
            if (o instanceof Set) {
                result.addAll((Set)o);
            } else {
                result.add(o);
            }
        }
        
        /** Return a set of Map.Entry objects corresponding to the outside edges
         *  of this node. */
        public Set getAccessPathEdges() {
            if (accessPathEdges == null) return Collections.EMPTY_SET;
            return accessPathEdges.entrySet();
        }
        
        /** Return the set of fields that this node has outside edges with. */
        public Set getAccessPathEdgeFields() {
            if (accessPathEdges == null) return Collections.EMPTY_SET;
            return accessPathEdges.keySet();
        }
        
        /** Return the collection of target nodes that this node has inside
         * edges with. */
        public Collection getAccessPathEdgeTargets() {
            if (accessPathEdges == null) return Collections.EMPTY_SET;
            return new FlattenedCollection(accessPathEdges.values());
        }
        
        //public Quad getSourceQuad(jq_Field f, Node n) {
            //if (false) {
                //if (edgesToReasons == null) return null;
                //return (Quad)edgesToReasons.get(Edge.get(this, n, f));
            //}
            //return null;
        //}
        
        public void setEscapes() { this.escapes = true; }
        public boolean getEscapes() { return this.escapes; }
        
        /** Return a string representation of the node in short form. */
        public abstract String toString_short();
        public String toString() {
            return toString_short() + (this.escapes?"*":"");
        }
        /** Return a string representation of the node in long form.
         *  Includes inside and outside edges and passed parameters. */
        public String toString_long() {
            StringBuffer sb = new StringBuffer();
            if (addedEdges != null) {
                sb.append(" writes: ");
                for (Iterator i=addedEdges.entrySet().iterator(); i.hasNext(); ) {
                    java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                    jq_Field f = (jq_Field)e.getKey();
                    Object o = e.getValue();
                    if (o == null) continue;
                    sb.append(f);
                    sb.append("={");
                    if (o instanceof Node)
                        sb.append(((Node)o).toString_short());
                    else {
                        for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                           sb.append(((Node)j.next()).toString_short());
                           if (j.hasNext()) sb.append(", ");
                        }
                    }
                    sb.append("} ");
                }
            }
            if (accessPathEdges != null) {
                sb.append(" reads: ");
                sb.append(accessPathEdges);
            }
            if (passedParameters != null) {
                sb.append(" called: ");
                sb.append(passedParameters);
            }
            return sb.toString();
        }
        
        public void write(Textualizer t) throws IOException {
            if (addedEdges != null) {
                for (Iterator i = this.getNonEscapingEdges().iterator(); i.hasNext(); ) {
                    Map.Entry e = (Map.Entry) i.next();
                    jq_Field f = (jq_Field) e.getKey();
                    Collection c;
                    if (e.getValue() instanceof Collection)
                        c = (Collection) e.getValue();
                    else
                        c = Collections.singleton(e.getValue());
                    for (Iterator j = c.iterator(); j.hasNext(); ) {
                        Node n = (Node) j.next();
                        if (!t.contains(n)) continue;
                        t.writeString(" succ ");
                        t.writeObject(f);
                        t.writeString(" ");
                        t.writeReference(n);
                    }
                }
            }
            if (predecessors != null) {
                for (Iterator i = this.getPredecessors().iterator(); i.hasNext(); ) {
                    Map.Entry e = (Map.Entry) i.next();
                    jq_Field f = (jq_Field) e.getKey();
                    Collection c;
                    if (e.getValue() instanceof Collection)
                        c = (Collection) e.getValue();
                    else
                        c = Collections.singleton(e.getValue());
                    for (Iterator j = c.iterator(); j.hasNext(); ) {
                        Node n = (Node) j.next();
                        if (!t.contains(n)) continue;
                        t.writeString(" pred ");
                        t.writeObject(f);
                        t.writeString(" ");
                        t.writeReference(n);
                    }
                }
            }
            if (accessPathEdges != null) {
                for (Iterator i = this.getAccessPathEdgeTargets().iterator(); i.hasNext(); ) {
                    Node n = (Node) i.next();
                    if (!t.contains(n)) continue;
                    t.writeEdge("fsucc", n);
                }
            }
        }
        
        public void readEdges(IndexMap map, StringTokenizer st) {
            while (st.hasMoreElements()) {
                String edgeName = st.nextToken();
                if (edgeName.equals("succ")) {
                    jq_Field f = (jq_Field) jq_Member.read(st);
                    int index = Integer.parseInt(st.nextToken());
                    if (index < map.size()) {
                        addEdge(f, (Node) map.get(index));
                    }
                } else if (edgeName.equals("pred")) {
                    jq_Field f = (jq_Field) jq_Member.read(st);
                    int index = Integer.parseInt(st.nextToken());
                    if (index < map.size()) {
                        Node that = (Node) map.get(index);
                        that.addEdge(f, this);
                    }
                } else if (edgeName.equals("fsucc")) {
                    int index = Integer.parseInt(st.nextToken());
                    if (index < map.size()) {
                        FieldNode fn = (FieldNode) map.get(index);
                        addAccessPathEdge(fn.getField(), fn);
                    }
                } else if (edgeName.equals("fpred")) {
                    int index = Integer.parseInt(st.nextToken());
                    if (index < map.size()) {
                        Node n = (Node) map.get(index);
                        FieldNode fn = (FieldNode) this;
                        n.addAccessPathEdge(fn.getField(), fn);
                    }
                } else {
                    Assert.UNREACHABLE(edgeName);
                }
            }
        }
        
        /* (non-Javadoc)
         * @see jwutil.io.Textualizable#addEdge(java.lang.String, jwutil.io.Textualizable)
         */
        public void addEdge(String edge, Textualizable t) { }

        /* (non-Javadoc)
         * @see jwutil.io.Textualizable#writeEdges(jwutil.io.Textualizer)
         */
        public void writeEdges(Textualizer t) throws IOException { }

    }

    /** A CheckCastNode refers to the result of a CheckCast instruction
     */
    public static final class CheckCastNode extends Node {
        jq_Reference dstType;
        final ProgramLocation q;        

        public String toString_short() {
            return q.getEmacsName() + " Cast to (" + dstType.shortName() + ") @ "+(q==null?-1:q.getID());
        }
        private CheckCastNode(jq_Reference dstType, ProgramLocation q) {
            this.dstType = dstType;
            this.q = q;
        }
        private static HashMap/*<Pair<jq_Reference, ProgramLocation>,CheckCastNode>*/ FACTORY = new HashMap();
        public static CheckCastNode get(jq_Reference dstType, ProgramLocation q) {
            Pair key = new Pair(dstType, q);
            CheckCastNode n = (CheckCastNode)FACTORY.get(key);
            if (n == null) {
                FACTORY.put(key, n = new CheckCastNode(dstType, q));
            }
            return n;
        }
        public CheckCastNode(CheckCastNode that) {
            super(that);
            this.dstType = that.dstType;
            this.q = that.q;
        }
        public Node copy() { return new CheckCastNode(this); }
        public jq_Reference getDeclaredType() { return dstType; }
        public jq_Method getDefiningMethod() { return q.getMethod(); }
        public ProgramLocation getLocation() { return q; }

        public void write(Textualizer t) throws IOException {
            dstType.write(t);
            t.writeString(" ");
            q.write(t);
            super.write(t);
        }

        public static Node read(StringTokenizer st) {
            jq_Reference type = (jq_Reference) jq_Type.read(st);
            ProgramLocation q = ProgramLocation.read(st);
            CheckCastNode n = CheckCastNode.get(type, q);
            //n.readEdges(map, st);
            return n;
        }
    }
    
    /** A ConcreteTypeNode refers to an object with a concrete type.
     *  This is the result of a 'new' operation or a constant object.
     *  It is tied to the quad that created it, so nodes of the same type but
     *  from different quads are not equal.
     */
    public static final class ConcreteTypeNode extends Node implements HeapObject {
        final jq_Reference type;
        final ProgramLocation q;
        final Integer opn;
        
        static final HashMap FACTORY = new HashMap();
        public static ConcreteTypeNode get(jq_Reference type) {
            ConcreteTypeNode n = (ConcreteTypeNode)FACTORY.get(type);
            if (n == null) {
                FACTORY.put(type, n = new ConcreteTypeNode(type));
            }
            return n;
        }
        
        public final Node copy() { return new ConcreteTypeNode(this); }
        
        private ConcreteTypeNode(jq_Reference type) {
            this.type = type; this.q = null; this.opn = null;
        }
        static final HashMap FACTORY2 = new HashMap();
        public static ConcreteTypeNode get(jq_Reference type, ProgramLocation q) {
            return get(type, q, new Integer(0));
        }
        public static ConcreteTypeNode get(jq_Reference type, ProgramLocation q, Integer opn) {
            Triple key = new Triple(type, q, opn);
            ConcreteTypeNode n = (ConcreteTypeNode)FACTORY2.get(key);
            if (n == null) {
                FACTORY2.put(key, n = new ConcreteTypeNode(type, q, opn));
            }
            return n;
        }    
        
        private ConcreteTypeNode(jq_Reference type, ProgramLocation q, Integer opn) {
            this.type = type; this.q = q; this.opn = opn;
        }

        private ConcreteTypeNode(ConcreteTypeNode that) {
            super(that);
            this.type = that.type; this.q = that.q; this.opn = that.opn;
        }
        
        public ProgramLocation getLocation() { return q; }
        
        public jq_Method getDefiningMethod() {
            if (q == null) return null;
            return q.getMethod();
        }
        
        public jq_Reference getDeclaredType() { return type; }
        
        public String toString_long() {
            return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long();
        }
        public String toString_short() {
            return (q==null?"":q.getEmacsName())+" Concrete: "+(type==null?"null":type.shortName())+" @ "+(q==null?-1:q.getID());
        }

        public void write(Textualizer t) throws IOException {
            if (type == null) t.writeString("null ");
            else {
                type.write(t);
                t.writeString(" ");
            }
            if (opn == null) t.writeString("null ");
            else t.writeString(opn.toString()+" ");
            t.writeObject(q);
            super.write(t);
        }
        
        public static ConcreteTypeNode read(StringTokenizer st) {
            jq_Reference type = (jq_Reference) jq_Type.read(st);
            String opns = st.nextToken();
            Integer opn = opns.equals("null") ? null : Integer.decode(opns);
            ProgramLocation pl = ProgramLocation.read(st);
            ConcreteTypeNode n = ConcreteTypeNode.get(type, pl, opn);
            //n.readEdges(map, st);
            return n;
        }
    }
    
    /** A ConcreteObjectNode refers to an object that we discovered through reflection.
     * It includes a reference to the actual object instance.
     */
    public static final class ConcreteObjectNode extends Node implements HeapObject {
        Object object;          // null for nullconstant, String for stringconstant, Object for object
        final Object key;       // ProgramLocation for nullconstant, StringConstant for stringconstant, List<jq_Field> for object
        final ProgramLocation q;
        
        public static Collection getAll() {
            return FACTORY.values();
        }
        
        public static final boolean ADD_EDGES = true;
        static final HashMap FACTORY = new HashMap();
        public static ConcreteObjectNode get(AConstOperand op, ProgramLocation q) {
            Object key = q;
            if (!Operand.Util.isNullConstant(op)) {
                key = q.getContainingClass().findStringConstant((String)op.getValue());
                Assert._assert(key != null);
                jq_Method meth = q.getMethod(); // one per constant, per method
                key = new Pair(key, meth);
            }
            ConcreteObjectNode n = get(key, op.getValue(), q);
            return n;
        }
        private static ConcreteObjectNode get(Object key, Object o, ProgramLocation q) {
            ConcreteObjectNode n = (ConcreteObjectNode) FACTORY.get(key);
            if (n == null) {
                FACTORY.put(key, n = new ConcreteObjectNode(key, o, q));
            }
            return n;
        }
        public static ConcreteObjectNode get(jq_Field f, Object o) {
            List path = new ArrayList();
            path.add(f);
            return explore(path, o);
        }
        static HashSet explored = new HashSet();
        private static ConcreteObjectNode explore(List/*<jq_Field>*/ path, Object o) {
            ConcreteObjectNode n = get(/*key=*/path, o, /*ProgramLocation*/null);
            n.object = o;       // set object if path was read via read()
            if (o != null) {
                if (ADD_EDGES && !explored.contains(o)) {
                    explored.add(o);
                    // add edges.
                    jq_Reference type = jq_Reference.getTypeOf(o);
                    if (type.isClassType()) {
                        jq_Class c = (jq_Class) type;
                        c.prepare();
                        jq_InstanceField[] ifs = c.getInstanceFields();
                        for (int i=0; i<ifs.length; ++i) {
                            if (ifs[i].getType().isPrimitiveType()) continue;
                            Object p = Reflection.getfield_A(o, ifs[i]);
                            ArrayList np = new ArrayList(path);
                            np.add(ifs[i]);
                            n.addEdge(ifs[i], explore(np, p));
                        }
                    } else {
                        Assert._assert(type.isArrayType());
                        jq_Array a = (jq_Array) type;
                        if (a.getElementType().isReferenceType()) {
                            Object[] oa = (Object[]) o;
                            for (int i=0; i<oa.length; ++i) {
                                ArrayList np = new ArrayList(path);
                                np.add((jq_Field) null);
                                n.addEdge((jq_Field) null, explore(np, oa[i]));
                            }
                        }
                    }
                }
            }
            return n;
        }
        
        public ProgramLocation getLocation() { return q; }
        
        public final Node copy() { return new ConcreteObjectNode(this); }
        
        private ConcreteObjectNode(Object key, Object o, ProgramLocation q) { 
            this.key = key; this.object = o; this.q = q; 
        }

        private ConcreteObjectNode(ConcreteObjectNode that) {
            super(that);
            this.object = that.object;
            this.key = that.key;
            this.q = that.q;
        }
        
        public jq_Method getDefiningMethod() { return null; }
        
        public jq_Reference getDeclaredType() {
            if (object == null) return null;
            return jq_Reference.getTypeOf(object);
        }
        
        public String toString_long() { return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long(); }
        public String toString_short() {
            return (q==null?"":q.getEmacsName())+" Object: "+(getDeclaredType()==null?"null":getDeclaredType().shortName())+" @ "+(q==null?-1:q.getID());
        }
        
        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.MethodSummary.Node#getNonEscapingEdgeFields()
         */
        public Set getNonEscapingEdgeFields() {
            if (ADD_EDGES)
                return super.getNonEscapingEdgeFields();
            if (object == null) return Collections.EMPTY_SET;
            jq_Reference type = jq_Reference.getTypeOf(object);
            HashSet ll = new HashSet();
            if (type.isClassType()) {
                jq_Class c = (jq_Class) type;
                c.prepare();
                jq_InstanceField[] ifs = c.getInstanceFields();
                for (int i=0; i<ifs.length; ++i) {
                    if (ifs[i].getType().isPrimitiveType()) continue;
                    ll.add(ifs[i]);
                }
            } else {
                Assert._assert(type.isArrayType());
                jq_Array a = (jq_Array) type;
                if (a.getElementType().isReferenceType()) {
                    ll.add(null);
                }
            }
            ll.addAll(super.getNonEscapingEdgeFields());
            return ll;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.MethodSummary.Node#getEdges()
         */
        public Set getEdges() {
            if (ADD_EDGES)
                return super.getNonEscapingEdges();
            if (object == null) return Collections.EMPTY_SET;
            jq_Reference type = jq_Reference.getTypeOf(object);
            HashMap ll = new HashMap();
            if (type.isClassType()) {
                jq_Class c = (jq_Class) type;
                c.prepare();
                jq_InstanceField[] ifs = c.getInstanceFields();
                for (int i=0; i<ifs.length; ++i) {
                    if (ifs[i].getType().isPrimitiveType()) continue;
                    ll.put(ifs[i], get(ifs[i], Reflection.getfield_A(object, ifs[i])));
                }
            } else {
                Assert._assert(type.isArrayType());
                jq_Array a = (jq_Array) type;
                if (a.getElementType().isReferenceType()) {
                    Object[] oa = (Object[]) object;
                    for (int i=0; i<oa.length; ++i) {
                        ll.put(null, get((jq_Field)null, oa[i]));
                    }
                }
            }
            if (addedEdges != null)
                ll.putAll(addedEdges);
            return ll.entrySet();
        }

        public boolean hasNonEscapingEdge(jq_Field m, Node n) {
            if (ADD_EDGES)
                return super.hasNonEscapingEdge(m, n);
            if (object == null)
                return false;
            if (!(n instanceof ConcreteObjectNode))
                return super.hasNonEscapingEdge(m, n);
            Object other = ((ConcreteObjectNode) n).object;
            jq_Reference type = jq_Reference.getTypeOf(object);
            if (type.isClassType()) {
                jq_Class c = (jq_Class) type;
                c.prepare();
                jq_InstanceField[] ifs = c.getInstanceFields();
                if (!Arrays.asList(ifs).contains(m)) return false;
                Object p = Reflection.getfield_A(object, (jq_InstanceField) m);
                if (p == other) return true;
            } else {
                Assert._assert(type.isArrayType());
                if (m != null) return false;
                jq_Array a = (jq_Array) type;
                if (!a.getElementType().isReferenceType()) return false;
                Object[] oa = (Object[]) object;
                for (int i=0; i<oa.length; ++i) {
                    if (other == oa[i]) return true;
                }
            }
            return super.hasNonEscapingEdge(m, n);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.MethodSummary.Node#hasEdges()
         */
        public boolean hasEdges() {
            if (ADD_EDGES)
                return super.hasNonEscapingEdges();
            return object != null;
        }

        public boolean removeEdge(jq_Field m, Node n) {
            Assert._assert(!(n instanceof ConcreteObjectNode));
            return super.removeEdge(m, n);
        }

        public void write(Textualizer t) throws IOException {
            if (object == null && q instanceof ProgramLocation) t.writeString("nullconstant");
            else if (key instanceof Pair) {
                t.writeString("stringconstant ");
                Pair p = (Pair)key;
                ((jq_Class.StringConstant)p.left).write(t);
                t.writeString(" ");
                t.writeObject((jq_Method)p.right);
            } else {
                t.writeString("object");
                List l = (List)key;
                t.writeString(" " + l.size());
                for (int i = 0; i < l.size(); i++) {
                    t.writeString(" ");
                    jq_Field f = ((jq_Field)l.get(i));
                    t.writeObject(f);
                }
            }

            t.writeString(" ");
            t.writeObject(q);
            super.write(t);
        }

        public static Node read(StringTokenizer st) {
            String what = st.nextToken();
            Object key, o;
            ProgramLocation pl;
            if (what.equals("nullconstant")) {
                o = null;
                key = pl = ProgramLocation.read(st);
            } else if (what.equals("stringconstant")) {
                jq_Class.StringConstant sc = jq_Class.readStringConstant(st);
                jq_Method m = (jq_Method)jq_Member.read(st);
                key = new Pair(sc, m);
                o = sc.getString();
                pl = ProgramLocation.read(st);
            } else if (what.equals("object")) {
                int n = Integer.parseInt(st.nextToken());
                ArrayList al = new ArrayList(n);
                for (int i = 0; i < n; i++) {
                    al.add(jq_Member.read(st));
                }
                key = al;
                pl = ProgramLocation.read(st);
                o = null;
                // o stays null, will be initialized later
            } else
                throw new InternalError("bad tag " + what);
            //n.readEdges(map, st);
            return ConcreteObjectNode.get(key, o, pl);
        }
    }
    
    /** A UnknownTypeNode refers to an object with an unknown type.  All that is
     *  known is that the object is the same or a subtype of some given type.
     *  Nodes with the same "type" are considered to be equal.
     *  This class includes a factory to get UnknownTypeNode's.
     */
    public static final class UnknownTypeNode extends Node implements HeapObject {
        public static final boolean ADD_DUMMY_EDGES = false;
        
        static final HashMap FACTORY = new HashMap();
        public static UnknownTypeNode get(jq_Reference type) {
            UnknownTypeNode n = (UnknownTypeNode)FACTORY.get(type);
            if (n == null) {
                FACTORY.put(type, n = new UnknownTypeNode(type));
                if (ADD_DUMMY_EDGES) n.addDummyEdges();
            }
            return n;
        }
        
        public static Collection getAll() {
            return FACTORY.values();
        }
        
        final jq_Reference type;
        
        private UnknownTypeNode(jq_Reference type) {
            this.type = type;
            this.setEscapes();
        }
        
        private void addDummyEdges() {
            if (type instanceof jq_Class) {
                jq_Class klass = (jq_Class)type;
                klass.prepare();
                jq_InstanceField[] fields = klass.getInstanceFields();
                for (int i=0; i<fields.length; ++i) {
                    jq_InstanceField f = fields[i];
                    if (f.getType() instanceof jq_Reference) {
                        UnknownTypeNode n = get((jq_Reference)f.getType());
                        this.addEdge(f, n);
                    }
                }
            } else {
                jq_Array array = (jq_Array)type;
                if (array.getElementType() instanceof jq_Reference) {
                    UnknownTypeNode n = get((jq_Reference)array.getElementType());
                    this.addEdge((jq_Field) null, n);
                }
            }
        }
        
        /** Update all predecessor and successor nodes with the given update map.
         *  Also clones the passed parameter set.
         */
        public void update(HashMap um) {
            if (false) {
                if (TRACE_INTRA) out.println("Updating edges for node "+this.toString_long());
                Map m = this.predecessors;
                if (m != null) {
                    this.predecessors = new LinkedHashMap();
                    updateMap_unknown(um, m.entrySet().iterator(), this.predecessors);
                }
                m = this.addedEdges;
                if (m != null) {
                    this.addedEdges = new LinkedHashMap();
                    updateMap_unknown(um, m.entrySet().iterator(), this.addedEdges);
                }
                m = this.accessPathEdges;
                if (m != null) {
                    this.accessPathEdges = new LinkedHashMap();
                    updateMap_unknown(um, m.entrySet().iterator(), this.accessPathEdges);
                }
                if (this.passedParameters != null) {
                    Set pp = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
                    pp.addAll(this.passedParameters); 
                    this.passedParameters = pp;
                }
                addGlobalEdges(this);
            }
        }
        
        public ProgramLocation getLocation() { return null; }
        
        public jq_Method getDefiningMethod() { return null; }
        
        public jq_Reference getDeclaredType() { return type; }
        
        public final Node copy() { return this; }
        
        public String toString_long() { return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long(); }
        public String toString_short() {
            return "Unknown: "+type;
        }

        public void write(Textualizer t) throws IOException {
            getDeclaredType().write(t);
            super.write(t);
        }
        public static UnknownTypeNode read(StringTokenizer st) {
            jq_Reference type = (jq_Reference) jq_Type.read(st);
            //n.readEdges(map, st);
            return new UnknownTypeNode(type);
        }
    }
    
    /** A PlaceholderNode is used to signify an object that is out-of-scope.
     */
    public static final class PlaceholderNode extends OutsideNode implements HeapObject {
        public static final boolean ADD_DUMMY_EDGES = false;
        
        static final HashMap FACTORY = new HashMap();
        public static PlaceholderNode get(jq_Method m, int k) {
            jq_Reference type = (jq_Reference) m.getParamTypes()[k];
            return get(m, ""+k, type);
        }
        public static PlaceholderNode get(jq_StaticField f) {
            jq_Reference type = (jq_Reference) f.getType();
            return get(f, "field", type);
        }
        public static PlaceholderNode get(jq_Method m, String s) {
            jq_Reference type;
            if (s.equals("return")) type = (jq_Reference) m.getReturnType();
            else if (s.equals("throw")) type = PrimordialClassLoader.getJavaLangThrowable();
            else type = (jq_Reference) m.getParamTypes()[Integer.parseInt(s)];
            return get(m, s, type);
        }
        public static PlaceholderNode get(jq_Member m, String s, jq_Reference type) {
            Object key = new Pair(m, s);
            PlaceholderNode n = (PlaceholderNode)FACTORY.get(key);
            if (n == null) {
                FACTORY.put(key, n = new PlaceholderNode(m, s, type));
            }
            return n;
        }
        
        public static Collection getAll() {
            return FACTORY.values();
        }
        
        final jq_Member member;
        final String s;
        final jq_Reference type;
        
        private PlaceholderNode(jq_Member m, String s, jq_Reference type) {
            this.member = m;
            this.s = s;
            this.type = type;
            this.setEscapes();
        }
        
        /** Update all predecessor and successor nodes with the given update map.
         *  Also clones the passed parameter set.
         */
        public void update(HashMap um) {
            if (false) {
                if (TRACE_INTRA) out.println("Updating edges for node "+this.toString_long());
                Map m = this.predecessors;
                if (m != null) {
                    this.predecessors = new LinkedHashMap();
                    updateMap_unknown(um, m.entrySet().iterator(), this.predecessors);
                }
                m = this.addedEdges;
                if (m != null) {
                    this.addedEdges = new LinkedHashMap();
                    updateMap_unknown(um, m.entrySet().iterator(), this.addedEdges);
                }
                m = this.accessPathEdges;
                if (m != null) {
                    this.accessPathEdges = new LinkedHashMap();
                    updateMap_unknown(um, m.entrySet().iterator(), this.accessPathEdges);
                }
                if (this.passedParameters != null) {
                    Set pp = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
                    pp.addAll(this.passedParameters); 
                    this.passedParameters = pp;
                }
                addGlobalEdges(this);
            }
        }
        
        public ProgramLocation getLocation() { return null; }
        
        public jq_Method getDefiningMethod() { return null; }
        
        public jq_Reference getDeclaredType() { return type; }
        
        public final Node copy() { return this; }
        
        public String toString_long() { return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long(); }
        public String toString_short() {
            return "Placeholder: "+member+" "+s+" type "+type;
        }

        public void write(Textualizer t) throws IOException {
            member.write(t);
            t.writeString(s);
            super.write(t);
        }
        public static PlaceholderNode read(StringTokenizer st) {
            jq_Member m = (jq_Member) jq_Member.read(st);
            String s = st.nextToken();
            //n.readEdges(map, st);
            jq_Reference type;
            if (m instanceof jq_Method) {
                jq_Method m2 = (jq_Method) m;
                if (s.equals("return")) type = (jq_Reference) m2.getReturnType();
                else if (s.equals("throw")) type = PrimordialClassLoader.getJavaLangThrowable();
                else type = (jq_Reference) m2.getParamTypes()[Integer.parseInt(s)];
            } else {
                type = (jq_Reference) ((jq_Field) m).getType();
            }
            return new PlaceholderNode(m, s, type);
        }
    }
    
    /** An outside node is some node that can be mapped to other nodes.
     *  This is just a marker for some of the other node classes below.
     */
    public abstract static class OutsideNode extends Node {
        OutsideNode() {}
        OutsideNode(Node n) { super(n); }
        
        public abstract jq_Reference getDeclaredType();
        
        public OutsideNode skip;
        public boolean visited;
        
    }
    
    /** A GlobalNode stores references to the static variables.
     *  It has no predecessors, and there is a global copy stored in GLOBAL.
     */
    public static final class GlobalNode extends OutsideNode {
        jq_Method method;
        static final HashMap FACTORY = new HashMap();
        public static GlobalNode get(jq_Method m) {
            GlobalNode n = (GlobalNode)FACTORY.get(m);
            if (n == null) {
                FACTORY.put(m, n = new GlobalNode(m));
            }
            return n;
        }
        private GlobalNode(jq_Method m) {
            this.method = m;
            if (TRACE_INTRA) out.println("Created "+this.toString_long());
        }
        private GlobalNode(GlobalNode that) {
            super(that);
            this.method = that.method;
        }
        public jq_Reference getDeclaredType() { return null; }
        public jq_Method getDefiningMethod() { return method; }
        public final Node copy() {
            Assert._assert(this != GLOBAL);
            return new GlobalNode(this);
        }
        public String toString_long() { return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long(); }
        public String toString_short() {
            return "global("+(method==null?"null":method.toString())+")";
        }
        public static GlobalNode GLOBAL = GlobalNode.get((jq_Method) null);
        
        public void addDefaultStatics() {
            jq_Class c;
            jq_StaticField f;
            Node n;
            
            c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/System;");
            c.load();
            f = (jq_StaticField) c.getDeclaredMember("in", "Ljava/io/InputStream;");
            Assert._assert(f != null);
            n = ConcreteObjectNode.get(f, System.in);
            addEdge(f, n);
            f = (jq_StaticField) c.getDeclaredMember("out", "Ljava/io/PrintStream;");
            Assert._assert(f != null);
            n = ConcreteObjectNode.get(f, System.out);
            addEdge(f, n);
            f = (jq_StaticField) c.getDeclaredMember("err", "Ljava/io/PrintStream;");
            Assert._assert(f != null);
            n = ConcreteObjectNode.get(f, System.err);
            addEdge(f, n);
            
            //System.out.println("Edges from global: "+getEdges());
        }
        
        public void write(Textualizer t) throws IOException {
            t.writeObject(method);
            super.write(t);
        }
        public static GlobalNode read(StringTokenizer st) {
            jq_Method m = (jq_Method) jq_Member.read(st);
            //n.readEdges(map, st);
            return GlobalNode.get(m);
        }
        
    }
    
    /** A ReturnedNode represents a return value or thrown exception from a method call. */
    public abstract static class ReturnedNode extends OutsideNode {
        final ProgramLocation m;
        public ReturnedNode(ProgramLocation m) { this.m = m; }
        public ReturnedNode(ReturnedNode that) {
            super(that); this.m = that.m;
        }
        public jq_Method getDefiningMethod() {
            return m.getMethod();
        }
        public final ProgramLocation getLocation() { return m; }
    }
    
    /** A ReturnValueNode represents the return value of a method call.
     */
    public static final class ReturnValueNode extends ReturnedNode {
        static final HashMap FACTORY = new HashMap();
        public static ReturnValueNode get(ProgramLocation m) {
            ReturnValueNode n = (ReturnValueNode)FACTORY.get(m);
            if (n == null) {
                FACTORY.put(m, n = new ReturnValueNode(m));
            }
            return n;
        }
        private ReturnValueNode(ProgramLocation m) { super(m); }
        private ReturnValueNode(ReturnValueNode that) { super(that); }
        
        public jq_Reference getDeclaredType() {
            return (jq_Reference) m.getTargetMethod().getReturnType();
        }
        
        public final Node copy() { return new ReturnValueNode(this); }
        
        public String toString_long() {
            return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long();
        }
        public String toString_short() {
            return m.getEmacsName()+" Return value of "+m;
        }
        
        public void write(Textualizer t) throws IOException {
            m.write(t);
            super.write(t);
        }
        public static ReturnValueNode read(StringTokenizer st) {
            ProgramLocation m = ProgramLocation.read(st);
            //n.readEdges(map, st);
            return ReturnValueNode.get(m);
        }
    }
    
    /*
    public static final class CaughtExceptionNode extends OutsideNode {
        final ExceptionHandler eh;
        Set caughtExceptions;
        public CaughtExceptionNode(ExceptionHandler eh) { this.eh = eh; }
        private CaughtExceptionNode(CaughtExceptionNode that) {
            super(that);
            this.eh = that.eh; this.caughtExceptions = that.caughtExceptions;
        }
        
        public void addCaughtException(ThrownExceptionNode n) {
            if (caughtExceptions == null) caughtExceptions = NodeSet.FACTORY.makeSet();
            caughtExceptions.add(n);
        }
        
        public final Node copy() {
            return new CaughtExceptionNode(this);
        }
        
        public jq_Reference getDeclaredType() { return (jq_Reference)eh.getExceptionType(); }
        
        public String toString_long() { return toString_short()+super.toString_long(); }
        public String toString_short() { return Strings.hex(this)+": "+"Caught exception: "+eh; }
    }
    */
    
    /** A ThrownExceptionNode represents the thrown exception of a method call.
     */
    public static final class ThrownExceptionNode extends ReturnedNode {
        static final HashMap FACTORY = new HashMap();
        public static ThrownExceptionNode get(ProgramLocation m) {
            ThrownExceptionNode n = (ThrownExceptionNode)FACTORY.get(m);
            if (n == null) {
                FACTORY.put(m, n = new ThrownExceptionNode(m));
            }
            return n;
        }
        private ThrownExceptionNode(ProgramLocation m) { super(m); }
        private ThrownExceptionNode(ThrownExceptionNode that) { super(that); }
        
        public jq_Reference getDeclaredType() { return PrimordialClassLoader.getJavaLangObject(); }
        
        public final Node copy() { return new ThrownExceptionNode(this); }
        
        public String toString_long() {
            return Integer.toHexString(this.hashCode())+": "+toString_short()+super.toString_long();
        }
        public String toString_short() {
            return m.getEmacsName()+" Thrown exception of "+m;
        }
        
        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.MethodSummary.Node#print(joeq.Compiler.Quad.MethodSummary, java.io.PrintWriter)
         */
        public void write(Textualizer t) throws IOException {
            m.write(t);
            super.write(t);
        }
        public static ThrownExceptionNode read(StringTokenizer st) {
            ProgramLocation m = ProgramLocation.read(st);
            //n.readEdges(map, st);
            return ThrownExceptionNode.get(m);
        }
    }
    
    /** A ParamNode represents an incoming parameter.
     */
    public static class ParamNode extends OutsideNode {
        final jq_Method m; final int n; final jq_Reference declaredType;
        static HashMap /*<Triple<jq_Method, Integer, jq_Reference>, ParamNode>*/ FACTORY = new HashMap();
        
        private ParamNode(jq_Method m, int n, jq_Reference declaredType) { this.m = m; this.n = n; this.declaredType = declaredType; }
        public static ParamNode get(jq_Method m, int n, jq_Reference declaredType) { 
            Triple key = new Triple(m, new Integer(n), declaredType);
            ParamNode pnode = (ParamNode)FACTORY.get(key);
            if (pnode == null) {
                FACTORY.put(key, pnode = new ParamNode(m, n, declaredType));
            }
            return pnode; 
        }
        private ParamNode(ParamNode that) {
            this.m = that.m; this.n = that.n; this.declaredType = that.declaredType;
        }
        public jq_Reference getDeclaredType() { return declaredType; }
        public jq_Method getDefiningMethod() { return m; }
        
        public jq_Method getMethod() { return m; }
        public int getIndex() { return n; }
        
        public Node copy() { return new ParamNode(this); }
        
        public String toString_long() {
            return Integer.toHexString(this.hashCode())+": "+this.toString_short()+super.toString_long();
        }
        public String toString_short() {
            return "Param#"+n+" method "+m;
        }
        
        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.MethodSummary.Node#print(joeq.Compiler.Quad.MethodSummary, java.io.PrintWriter)
         */
        public void write(Textualizer t) throws IOException {
            m.write(t);
            t.writeString(" "+n);
            super.write(t);
        }
        public static ParamNode read(StringTokenizer st) {
            jq_Method m = (jq_Method) jq_Member.read(st);
            int k = Integer.parseInt(st.nextToken());
            jq_Reference type = (jq_Reference) m.getParamTypes()[k];
            //n.readEdges(map, st);
            return get(m, k, type);
        }
    }

    // fake methods have fake param nodes
    public static final class FakeParamNode extends ParamNode {
        static final HashMap FACTORY = new HashMap();
        public static FakeParamNode getFake(jq_Method m, int n, jq_Reference declaredType) {
            Triple key = new Triple(m, new Integer(n), declaredType);
            FakeParamNode fn = (FakeParamNode)FACTORY.get(key);
            if (fn == null) {
                FACTORY.put(key, fn = new FakeParamNode(m, n, declaredType));
            }
            return fn;
        }
        private FakeParamNode(jq_Method m, int n, jq_Reference declaredType) {
            super(m, n, declaredType);
        }
        private FakeParamNode(FakeParamNode that) {
            super(that);
        }
        public final Node copy() { return new FakeParamNode(this); }

        public static ParamNode read(StringTokenizer st) {
            jq_Method m = (jq_Method) jq_FakeInstanceMethod.read(st);
            int k = Integer.parseInt(st.nextToken());
            jq_Reference type = (jq_Reference) m.getParamTypes()[k];
            //n.readEdges(map, st);
            return FakeParamNode.get(m, k, type);
        }
    }
    
    /** A FieldNode represents the result of a 'load' instruction.
     *  There are outside edge links from the nodes that can be the base object
     *  of the load to this node.
     *  Two nodes are equal if the fields match and they are from the same instruction.
     */
    public static final class FieldNode extends OutsideNode {
        final jq_Field f; final Set locs;
        Set field_predecessors;
        
        private static FieldNode findPredecessor(FieldNode base, ProgramLocation obj) {
            if (TRACE_INTRA) out.println("Checking "+base+" for predecessor "+obj.getID());
            if (base.locs.contains(obj)) {
                if (TRACE_INTRA) out.println("Success!");
                return base;
            }
            if (base.visited) {
                if (TRACE_INTRA) out.println(base+" already visited");
                return null;
            }
            base.visited = true;
            if (base.field_predecessors != null) {
                for (Iterator i=base.field_predecessors.iterator(); i.hasNext(); ) {
                    Object o = i.next();
                    if (o instanceof FieldNode) {
                        FieldNode fn = (FieldNode)o;
                        FieldNode fn2 = findPredecessor(fn, obj);
                        if (fn2 != null) {
                            base.visited = false;
                            return fn2;
                        }
                    }
                }
            }
            base.visited = false;
            return null;
        }
        
        public static FieldNode get(Node base, jq_Field f, ProgramLocation obj) {
            if (TRACE_INTRA) out.println("Getting field node for "+base+(f==null?"[]":("."+f.getName()))+" loc "+(obj==null?-1:obj.getID()));
            Set s = null;
            if (base.accessPathEdges != null) {
                Object o = base.accessPathEdges.get(f);
                if (o instanceof FieldNode) {
                    if (TRACE_INTRA) out.println("Field node for "+base+" already exists, reusing: "+o);
                    return (FieldNode)o;
                } else if (o != null) {
                    s = (Set)o;
                    if (!s.isEmpty()) {
                        if (TRACE_INTRA) out.println("Field node for "+base+" already exists, reusing: "+o);
                        return (FieldNode)s.iterator().next();
                    }
                }
            } else {
                base.accessPathEdges = new LinkedHashMap();
            }
            FieldNode fn;
            if (base instanceof FieldNode) fn = findPredecessor((FieldNode)base, obj);
            else fn = null;
            if (fn == null) {
                fn = FieldNode.get(f, obj);
                if (TRACE_INTRA) out.println("Created field node: "+fn.toString_long());
            } else {
                if (TRACE_INTRA) out.println("Using existing field node: "+fn.toString_long());
            }
            if (fn.field_predecessors == null) fn.field_predecessors = NodeSet.FACTORY.makeSet();
            fn.field_predecessors.add(base);
            if (s != null) {
                if (VERIFY_ASSERTIONS) Assert._assert(base.accessPathEdges.get(f) == s);
                s.add(fn);
            } else {
                base.accessPathEdges.put(f, fn);
            }
            if (TRACE_INTRA) out.println("Final field node: "+fn.toString_long());
            return fn;
        }
        
        static final HashMap FACTORY = new HashMap();
        private static FieldNode get(jq_Field f, ProgramLocation q) {
            Set locs = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            locs.add(q);
            return get(f, locs);
        }
        private static FieldNode get(jq_Field f, Set s) {
            Pair key = new Pair(f, s);
            FieldNode n = (FieldNode)FACTORY.get(key);
            if (n == null) {
                FACTORY.put(key, n = new FieldNode(f, s));
            }
            return n;
        }
        private FieldNode(jq_Field f, Set s) {
            this.f = f;
            this.locs = s;
        }

        private FieldNode(FieldNode that) {
            this.f = that.f;
            this.locs = SortedArraySet.FACTORY.makeSet(that.locs);
            this.field_predecessors = that.field_predecessors;
        }

        /** Returns a new FieldNode that is the unification of the given set of FieldNodes.
         *  In essence, all of the given nodes are replaced by a new, returned node.
         *  The given field nodes must be on the given field.
         */
        public static FieldNode unify(jq_Field f, Set s) {
            if (TRACE_INTRA) out.println("Unifying the set of field nodes: "+s);
            Set dislocs = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            // go through once to add all instructions, so that the hash code will be stable.
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                FieldNode dat = (FieldNode)i.next();
                Assert._assert(f == dat.f);
                dislocs.addAll(dat.locs);
            }
            FieldNode dis = FieldNode.get(f, dislocs);
            // once again to do the replacement.
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                FieldNode dat = (FieldNode)i.next();
                Set s2 = Collections.singleton(dis);
                dat.replaceBy(s2, true);
            }
            if (TRACE_INTRA) out.println("Resulting field node: "+dis.toString_long());
            return dis;
        }
        
        public void replaceBy(Set set, boolean removeSelf) {
            if (TRACE_INTRA) out.println("Replacing "+this+" with "+set+(removeSelf?", and removing self":""));
            if (set.contains(this)) {
                if (TRACE_INTRA) out.println("Replacing a node with itself, turning off remove self.");
                set.remove(this);
                if (set.isEmpty()) {
                    if (TRACE_INTRA) out.println("Replacing a node with only itself! Nothing to do.");
                    return;
                }
                removeSelf = false;
            }
            if (VERIFY_ASSERTIONS) Assert._assert(!set.contains(this));
            if (this.field_predecessors != null) {
                for (Iterator i=this.field_predecessors.iterator(); i.hasNext(); ) {
                    Node that = (Node)i.next();
                    Assert._assert(that != null);
                    if (removeSelf) {
                        i.remove();
                        that._removeAccessPathEdge(f, this);
                    }
                    if (that == this) {
                        // add self-cycles on f to all nodes in set.
                        if (TRACE_INTRA) out.println("Found self-cycle on outside edge of "+that);
                        for (Iterator j=set.iterator(); j.hasNext(); ) {
                            FieldNode k = (FieldNode)j.next();
                            k.addAccessPathEdge(f, k);
                        }
                    } else {
                        for (Iterator j=set.iterator(); j.hasNext(); ) {
                            that.addAccessPathEdge(f, (FieldNode)j.next());
                        }
                    }
                }
            }
            super.replaceBy(set, removeSelf);
        }
        
        static void addGlobalAccessPathEdges(FieldNode n) {
            if (n.field_predecessors == null) return;
            jq_Field f = n.f;
            for (Iterator i=n.field_predecessors.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o == GlobalNode.GLOBAL) {
                    GlobalNode.GLOBAL.addAccessPathEdge(f, n);
                } else if (o instanceof UnknownTypeNode) {
                    ((UnknownTypeNode)o).addAccessPathEdge(f, n);
                }
            }
        }
        
        public void update(HashMap um) {
            super.update(um);
            Set m = this.field_predecessors;
            if (m != null) {
                this.field_predecessors = NodeSet.FACTORY.makeSet();
                for (Iterator j=m.iterator(); j.hasNext(); ) {
                    Object p = j.next();
                    Assert._assert(p != null);
                    Object o = um.get(p);
                    if (p instanceof UnknownTypeNode) o = p;
                    if (p == GlobalNode.GLOBAL) o = p;
                    if (VERIFY_ASSERTIONS) Assert._assert(o != null, ((Node)p).toString_long()+" (field predecessor of "+this.toString_long()+")");
                    this.field_predecessors.add(o);
                }
                addGlobalAccessPathEdges(this);
            }
        }
        
        /** Return the set of outside edge predecessors of this node. */
        public Set getAccessPathPredecessors() {
            if (field_predecessors == null) return Collections.EMPTY_SET;
            return field_predecessors;
        }
        
        public jq_Field getField() { return f; }
        
        public jq_Method getDefiningMethod() {
            Iterator i = locs.iterator();
            if (!i.hasNext()) return null;
            return ((ProgramLocation) i.next()).getMethod();
        }
        
        public Set getLocations() { return locs; }
        
        public String fieldName() {
            if (f != null) return f.getName().toString();
            return getDeclaredType()+"[]";
        }
        
        public final Node copy() {
            return new FieldNode(this);
        }
        
        public jq_Reference getDeclaredType() {
            if (f != null) {
                return (jq_Reference)f.getType();
            }
            if (locs.isEmpty()) return PrimordialClassLoader.getJavaLangObject();
            return (jq_Reference) ((ProgramLocation) locs.iterator().next()).getResultType();
        }
        
        public String toString_long() {
            StringBuffer sb = new StringBuffer();
            //sb.append(Strings.hex(this));
            //sb.append(": ");
            sb.append(this.toString_short());
            sb.append(super.toString_long());
            if (field_predecessors != null) {
                sb.append(" field pred:");
                sb.append(field_predecessors);
            }
            return sb.toString();
        }
        public String toString_short() {
            StringBuffer sb = new StringBuffer();
            Iterator i=locs.iterator();
            while (i.hasNext()) {
                ProgramLocation pl = (ProgramLocation) i.next();
                sb.append(pl.getEmacsName());
                sb.append(' ');
            }
            sb.append("FieldLoad ");
            sb.append(fieldName());
            i=locs.iterator();
            if (i.hasNext()) {
                int id = ((ProgramLocation)i.next()).getID();
                if (!i.hasNext()) {
                    sb.append(" loc ");
                    sb.append(id);
                } else {
                    sb.append(" locs {");
                    sb.append(id);
                    while (i.hasNext()) {
                        sb.append(',');
                        sb.append(((ProgramLocation)i.next()).getID());
                    }
                    sb.append('}');
                }
            }
            return sb.toString();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.MethodSummary.Node#print
         */
        public void write(Textualizer t) throws IOException {
            t.writeObject(f);
            t.writeString(" "+locs.size());
            for (Iterator i = locs.iterator(); i.hasNext(); ) {
                t.writeString(" ");
                ProgramLocation pl = (ProgramLocation) i.next();
                pl.write(t);
            }
            for (Iterator i = this.field_predecessors.iterator(); i.hasNext(); ) {
                Node n = (Node) i.next();
                if (!t.contains(n)) continue;
                t.writeEdge("fpred", n);
            }
            super.write(t);
        }
        public static FieldNode read(StringTokenizer st) {
            jq_Field f = (jq_Field) jq_Member.read(st);
            int k = Integer.parseInt(st.nextToken());
            Set locs = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            for (int i = 0; i < k; ++i) {
                ProgramLocation pl = ProgramLocation.read(st);
                locs.add(pl);
            }
            //n.readEdges(map, st);
            return FieldNode.get(f, locs);
        }
    }
    
    /** Records the state of the intramethod analysis at some point in the method. */
    public static final class State implements Cloneable {
        final Object[] registers;
        /** Return a new state with the given number of registers. */
        public State(int nRegisters) {
            this.registers = new Object[nRegisters];
        }
        /** Return a shallow copy of this state.
         *  Sets of nodes are copied, but the individual nodes are not. */
        public State copy() {
            State that = new State(this.registers.length);
            for (int i=0; i<this.registers.length; ++i) {
                Object a = this.registers[i];
                if (a == null) continue;
                if (a instanceof Node)
                    that.registers[i] = a;
                else
                    that.registers[i] = NodeSet.FACTORY.makeSet((Set)a);
            }
            return that;
        }
        /** Merge two states.  Mutates this state, the other is unchanged. */
        public boolean merge(State that) {
            boolean change = false;
            for (int i=0; i<this.registers.length; ++i) {
                if (merge(i, that.registers[i])) change = true;
            }
            return change;
        }
        /** Merge the given node or set of nodes into the given register. */
        public boolean merge(int i, Object b) {
            if (b == null) return false;
            Object a = this.registers[i];
            if (b.equals(a)) return false;
            Set q;
            if (!(a instanceof Set)) {
                this.registers[i] = q = NodeSet.FACTORY.makeSet();
                if (a != null) q.add(a);
            } else {
                q = (Set)a;
            }
            if (b instanceof Set) {
                if (q.addAll((Set)b)) {
                    if (TRACE_INTRA) out.println("change in register "+i+" from adding set");
                    return true;
                }
            } else {
                if (q.add(b)) {
                    if (TRACE_INTRA) out.println("change in register "+i+" from adding "+b);
                    return true;
                }
            }
            return false;
        }
        /** Dump a textual representation of the state to the given print stream. */
        public void dump(java.io.PrintStream out) {
            for (int i=0; i<registers.length; ++i) {
                if (registers[i] == null) continue;
                out.print(i+": "+registers[i]+" ");
            }
            out.println();
        }
    }
    
    public static final class NodeSet implements Set, Cloneable {
    
        private Node elementData[];
        private int size;
        
        public NodeSet(int initialCapacity) {
            super();
            if (initialCapacity < 0)
                throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
            this.elementData = new Node[initialCapacity];
            this.size = 0;
        }
        
        public NodeSet() {
            this(10);
        }
        
        public NodeSet(Collection c) {
            this((int) Math.min((c.size()*110L)/100, Integer.MAX_VALUE));
            this.addAll(c);
        }
        
        public int size() {
            //jq.Assert(new LinkedHashSet(this).size() == this.size);
            return this.size;
        }
        
        private static final int compare(Node n1, Node n2) {
            int n1i = n1.id, n2i = n2.id;
            if (n1i > n2i) return 1;
            if (n1i < n2i) return -1;
            return 0;
        }
        
        private int whereDoesItGo(Node o) {
            int lo = 0;
            int hi = this.size-1;
            if (hi < 0)
                return 0;
            int mid = hi >> 1;
            for (;;) {
                Node o2 = this.elementData[mid];
                int r = compare(o, o2);
                if (r < 0) {
                    hi = mid - 1;
                    if (lo > hi) return mid;
                } else if (r > 0) {
                    lo = mid + 1;
                    if (lo > hi) return lo;
                } else {
                    return mid;
                }
                mid = ((hi - lo) >> 1) + lo;
            }
        }
        
        public boolean add(Object arg0) { return this.add((Node)arg0); }
        public boolean add(Node arg0) {
            int i = whereDoesItGo(arg0);
            int s = this.size;
            if (i != s && elementData[i].equals(arg0)) {
                return false;
            }
            ensureCapacity(s+1);
            System.arraycopy(this.elementData, i, this.elementData, i + 1, s - i);
            elementData[i] = arg0;
            this.size++;
            return true;
        }
        
        public void ensureCapacity(int minCapacity) {
            int oldCapacity = elementData.length;
            if (minCapacity > oldCapacity) {
                Object oldData[] = elementData;
                int newCapacity = ((oldCapacity * 3) >> 1) + 1;
                if (newCapacity < minCapacity)
                    newCapacity = minCapacity;
                this.elementData = new Node[newCapacity];
                System.arraycopy(oldData, 0, this.elementData, 0, this.size);
            }
        }
        
        // Set this to true if allocations are more expensive than arraycopy.
        public static final boolean REDUCE_ALLOCATIONS = false;
    
        public boolean addAll(java.util.Collection that) {
            if (that instanceof NodeSet) {
                boolean result = addAll((NodeSet) that);
                return result;
            } else {
                boolean change = false;
                for (Iterator i=that.iterator(); i.hasNext(); )
                    if (this.add((Node)i.next())) change = true;
                return change;
            }
        }
    
        public boolean addAll(NodeSet that) {
            if (this == that) {
                return false;
            }
            int s2 = that.size();
            if (s2 == 0) {
                return false;
            }
            int s1 = this.size;
            Node[] e1 = this.elementData, e2 = that.elementData;
            int newSize = Math.max(e1.length, s1 + s2);
            int i1, new_i1=0, i2=0;
            Node[] new_e1;
            if (REDUCE_ALLOCATIONS && newSize <= e1.length) {
                System.arraycopy(e1, 0, e1, s2, s1);
                new_e1 = e1;
                i1 = s2; s1 += s2;
            } else {
                new_e1 = new Node[newSize];
                this.elementData = new_e1;
                i1 = 0;
            }
            boolean change = false;
            for (;;) {
                if (i2 == s2) {
                    int size2 = s1-i1;
                    if (size2 > 0)
                        System.arraycopy(e1, i1, new_e1, new_i1, size2);
                    this.size = new_i1 + size2;
                    return change;
                }
                Node o2 = e2[i2++];
                for (;;) {
                    if (i1 == s1) {
                        new_e1[new_i1++] = o2;
                        int size2 = s2-i2;
                        System.arraycopy(e2, i2, new_e1, new_i1, size2);
                        this.size = new_i1 + size2;
                        return true;
                    }
                    Node o1 = e1[i1];
                    int r = compare(o1, o2);
                    if (r <= 0) {
                        new_e1[new_i1++] = o1;
                        if (REDUCE_ALLOCATIONS && new_e1 == e1) e1[i1] = null;
                        i1++;
                        if (r == 0) break;
                    } else {
                        new_e1[new_i1++] = o2;
                        change = true;
                        break;
                    }
                }
            }
        }
        public int indexOf(Node arg0) {
            int i = whereDoesItGo(arg0);
            if (i == size || arg0.id != elementData[i].id) return -1;
            return i;
        }
        public boolean contains(Object arg0) { return contains((Node)arg0); }
        public boolean contains(Node arg0) {
            boolean result = this.indexOf(arg0) != -1;
            return result;
        }
        public boolean remove(Object arg0) { return remove((Node)arg0); }
        public boolean remove(Node arg0) {
            int i = whereDoesItGo(arg0);
            if (i == size) {
                return false;
            }
            Object oldValue = elementData[i];
            if (arg0 != oldValue) {
                return false;
            }
            int numMoved = this.size - i - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, i+1, elementData, i, numMoved);
            elementData[--this.size] = null; // for gc
            return true;
        }
        public Object clone() {
            try {
                NodeSet s = (NodeSet) super.clone();
                int initialCapacity = this.elementData.length;
                s.elementData = new Node[initialCapacity];
                s.size = this.size;
                System.arraycopy(this.elementData, 0, s.elementData, 0, this.size);
                return s;
            } catch (CloneNotSupportedException _) { return null; }
        }
        
        public boolean equals(Object o) {
            if (o instanceof NodeSet) {
                boolean result = equals((NodeSet)o);
                return result;
            } else if (o instanceof Collection) {
                Collection that = (Collection) o;
                if (this.size() != that.size()) return false;
                for (Iterator i=that.iterator(); i.hasNext(); ) {
                    if (!this.contains(i.next())) return false;
                }
                return true;
            } else {
                return false;
            }
        }
        public boolean equals(NodeSet that) {
            if (this.size != that.size) {
                return false;
            }
            Node[] e1 = this.elementData; Node[] e2 = that.elementData;
            for (int i=0; i<this.size; ++i) {
                if (e1[i] != e2[i]) {
                    return false;
                }
            }
            return true;
        }
        
        public int hashCode() {
            int hash = 0;
            for (int i=0; i<this.size; ++i) {
                if (USE_IDENTITY_HASHCODE)
                    hash += System.identityHashCode(this.elementData[i]);
                else
                    hash += this.elementData[i].hashCode();
            }
            return hash;
        }
            
        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (false) {
                sb.append(Integer.toHexString(System.identityHashCode(this)));
                sb.append(':');
            }
            sb.append('{');
            for (int i=0; i<size; ++i) {
                sb.append(elementData[i]);
                if (i+1 < size) sb.append(',');
            }
            sb.append('}');
            return sb.toString();
        }
        
        public void clear() { this.size = 0; }
        public boolean containsAll(Collection arg0) {
            if (arg0 instanceof NodeSet) return containsAll((NodeSet)arg0);
            else {
                for (Iterator i=arg0.iterator(); i.hasNext(); )
                    if (!this.contains((Node)i.next())) return false;
                return true;
            }
        }
        public boolean containsAll(NodeSet that) {
            if (this == that) {
                return true;
            }
            int s1 = this.size;
            int s2 = that.size;
            if (s2 > s1) {
                return false;
            }
            Node[] e1 = this.elementData, e2 = that.elementData;
            for (int i1 = 0, i2 = 0; i2 < s2; ++i2) {
                Node o2 = e2[i2];
                for (;;) {
                    Node o1 = e1[i1++];
                    if (o1 == o2) break;
                    if (o1.id > o2.id) {
                        return false;
                    }
                }
            }
            return true;
        }
        public boolean isEmpty() { return this.size == 0; }
        public Iterator iterator() {
            final Node[] e = this.elementData;
            final int s = this.size;
            return new Iterator() {
                int n = s;
                int i = 0;
                public Object next() {
                    return e[i++];
                }
                public boolean hasNext() {
                    return i < n;
                }
                public void remove() {
                    int numMoved = s - i;
                    if (numMoved > 0)
                        System.arraycopy(e, i, e, i-1, numMoved);
                    elementData[--size] = null; // for gc
                    --i; --n;
                }
            };
        }
        public boolean removeAll(Collection arg0) {
            if (arg0 instanceof NodeSet)
                return removeAll((NodeSet)arg0);
            else {
                boolean change = false;
                for (Iterator i=arg0.iterator(); i.hasNext(); )
                    if (this.remove((Node)i.next())) change = true;
                return change;
            }
        }
        public boolean removeAll(NodeSet that) {
            if (this.isEmpty()) return false;
            if (this == that) {
                this.clear(); return true;
            }
            int s1 = this.size;
            int s2 = that.size;
            Node[] e1 = this.elementData, e2 = that.elementData;
            int i1 = 0, i2 = 0, i3 = 0;
            Node o1 = e1[i1++];
            Node o2 = e2[i2++];
outer:
            for (;;) {
                while (o1.id < o2.id) {
                    e1[i3++] = o1;
                    if (i1 == s1) break outer;
                    o1 = e1[i1++];
                }
                while (o1.id > o2.id) {
                    if (i2 == s2) break outer;
                    o2 = e2[i2++];
                }
                while (o1 == o2) {
                    if (i1 == s1) break outer;
                    o1 = e1[i1++];
                    if (i2 == s2) {
                        System.arraycopy(e1, i1, e1, i3, s1-i1);
                        i3 += s1-i1;
                        break outer;
                    }
                    o2 = e2[i2++];
                }
            }
            this.size = i3;
            return true;
        }
        
        public boolean retainAll(Collection arg0) {
            if (arg0 instanceof NodeSet)
                return retainAll((NodeSet)arg0);
            else {
                boolean change = false;
                for (Iterator i=this.iterator(); i.hasNext(); )
                    if (!arg0.contains(i.next())) {
                        i.remove();
                        change = true;
                    }
                return change;
            }
        }
        public boolean retainAll(NodeSet that) {
            if (this == that) return false;
            int s1 = this.size;
            int s2 = that.size;
            Node[] e1 = this.elementData, e2 = that.elementData;
            int i1 = 0, i2 = 0, i3 = 0;
            Node o1 = e1[i1++];
            Node o2 = e2[i2++];
outer:
            for (;;) {
                while (o1.id < o2.id) {
                    if (i1 == s1) break outer;
                    o1 = e1[i1++];
                }
                while (o1.id > o2.id) {
                    if (i2 == s2) break outer;
                    o2 = e2[i2++];
                }
                while (o1 == o2) {
                    e1[i3++] = o1;
                    if (i1 == s1) break outer;
                    o1 = e1[i1++];
                    if (i2 == s2) break outer;
                    o2 = e2[i2++];
                }
            }
            this.size = i3;
            return true;
        }
        public Object[] toArray() {
            Node[] n = new Node[this.size];
            System.arraycopy(this.elementData, 0, n, 0, this.size);
            return n;
        }
        public Object[] toArray(Object[] arg0) {
            return this.toArray();
        }
    
        public static final boolean TEST = false;
        public static final boolean PROFILE = false;
    
        public static final SetFactory FACTORY = new SetFactory() {
            /**
             * Version ID for serialization.
             */
            private static final long serialVersionUID = 3257845485078459956L;

            public final Set makeSet(Collection c) {
                if (TEST)
                    return new CollectionTestWrapper(new LinkedHashSet(c), new NodeSet(c));
                if (PROFILE)
                    return new InstrumentedSetWrapper(new NodeSet(c));
                return new NodeSet(c);
            }
        };
    
    }
    
    
    /** Encodes an access path.
     *  An access path is an NFA, where transitions are field names.
     *  Each node in the NFA is represented by an AccessPath object.
     *  We try to share AccessPath objects as much as possible.
     */
    public static class AccessPath {
        /** All incoming transitions have this field. */
        jq_Field _field;
        /** The incoming transitions are associated with this AccessPath object. */
        Node _n;
        /** Whether this is a valid end state. */
        boolean _last;
        
        /** The set of (wrapped) successor AccessPath objects. */
        Set succ;

        /** Adds the set of (wrapped) AccessPath objects that are reachable from this
         *  AccessPath object to the given set. */
        private void reachable(Set s) {
            for (Iterator i = this.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper ap = (IdentityHashCodeWrapper)i.next();
                if (!s.contains(ap)) {
                    s.add(ap);
                    ((AccessPath)ap.getObject()).reachable(s);
                }
            }
        }
        /** Return an iteration of the AccessPath objects that are reachable from
         *  this AccessPath. */
        public Iterator reachable() {
            Set s = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            s.add(IdentityHashCodeWrapper.create(this));
            this.reachable(s);
            return new FilterIterator(s.iterator(), filter);
        }
        
        /** Add the given AccessPath object as a successor to this AccessPath object. */
        private void addSuccessor(AccessPath ap) {
            succ.add(IdentityHashCodeWrapper.create(ap));
        }
        
        /** Return an access path that is equivalent to the given access path prepended
         *  with a transition on the given field and node.  The given access path can
         *  be null (empty). */
        public static AccessPath create(jq_Field f, Node n, AccessPath p) {
            if (p == null) return new AccessPath(f, n, true);
            AccessPath that = p.findNode(n);
            if (that == null) {
                that = new AccessPath(f, n);
            } else {
                p = p.copy();
                that = p.findNode(n);
            }
            that.addSuccessor(p);
            return that;
        }
        
        /** Return an access path that is equivalent to the given access path appended
         *  with a transition on the given field and node.  The given access path can
         *  be null (empty). */
        public static AccessPath create(AccessPath p, jq_Field f, Node n) {
            if (p == null) return new AccessPath(f, n, true);
            p = p.copy();
            AccessPath that = p.findNode(n);
            if (that == null) {
                that = new AccessPath(f, n);
            }
            that.setLast();
            for (Iterator i = p.findLast(); i.hasNext(); ) {
                AccessPath last = (AccessPath)i.next();
                last.unsetLast();
                last.addSuccessor(that);
            }
            return p;
        }
        
        /** Helper function for findLast(), below. */
        private void findLast(HashSet s, Set last) {
            for (Iterator i = this.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper ap = (IdentityHashCodeWrapper)i.next();
                if (!s.contains(ap)) {
                    s.add(ap);
                    AccessPath that = (AccessPath)ap.getObject();
                    if (that._last) last.add(ap);
                    that.findLast(s, last);
                }
            }
        }
        
        /** Return an iteration of the AccessPath nodes that correspond to end states. */
        public Iterator findLast() {
            HashSet visited = new HashSet();
            Set last = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            IdentityHashCodeWrapper ap = IdentityHashCodeWrapper.create(this);
            visited.add(ap);
            if (this._last) last.add(ap);
            this.findLast(visited, last);
            return new FilterIterator(last.iterator(), filter);
        }
        
        /** Helper function for findNode(Node n), below. */
        private AccessPath findNode(Node n, HashSet s) {
            for (Iterator i = this.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper ap = (IdentityHashCodeWrapper)i.next();
                if (!s.contains(ap)) {
                    AccessPath p = (AccessPath)ap.getObject();
                    if (n == p._n) return p;
                    s.add(ap);
                    AccessPath q = p.findNode(n, s);
                    if (q != null) return q;
                }
            }
            return null;
        }
        
        /** Find the AccessPath object that corresponds to the given node. */
        public AccessPath findNode(Node n) {
            if (n == this._n) return this;
            HashSet visited = new HashSet();
            IdentityHashCodeWrapper ap = IdentityHashCodeWrapper.create(this);
            visited.add(ap);
            return findNode(n, visited);
        }
        
        /** Set this transition as a valid end transition. */
        private void setLast() { this._last = true; }
        /** Unset this transition as a valid end transition. */
        private void unsetLast() { this._last = false; }
        
        /** Helper function for copy(), below. */
        private void copy(HashMap m, AccessPath that) {
            for (Iterator i = this.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper ap = (IdentityHashCodeWrapper)i.next();
                AccessPath p = (AccessPath)m.get(ap);
                if (p == null) {
                    AccessPath that2 = (AccessPath)ap.getObject();
                    p = new AccessPath(that2._field, that2._n, that2._last);
                    m.put(ap, p);
                    that2.copy(m, p);
                }
                that.addSuccessor(p);
            }
        }

        /** Return a copy of this (complete) access path. */
        public AccessPath copy() {
            HashMap m = new HashMap();
            IdentityHashCodeWrapper ap = IdentityHashCodeWrapper.create(this);
            AccessPath p = new AccessPath(this._field, this._n, this._last);
            m.put(ap, p);
            this.copy(m, p);
            return p;
        }
        
        /** Helper function for toString(), below. */
        private void toString(StringBuffer sb, HashSet set) {
            if (this._field == null) sb.append("[]");
            else sb.append(this._field.getName());
            if (this._last) sb.append("<e>");
            sb.append("->(");
            for (Iterator i = this.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper ap = (IdentityHashCodeWrapper)i.next();
                if (set.contains(ap)) {
                    sb.append("<backedge>");
                } else {
                    set.add(ap);
                    ((AccessPath)ap.getObject()).toString(sb, set);
                }
            }
            sb.append(')');
        }
        /** Returns a string representation of this (complete) access path. */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            HashSet visited = new HashSet();
            IdentityHashCodeWrapper ap = IdentityHashCodeWrapper.create(this);
            visited.add(ap);
            toString(sb, visited);
            return sb.toString();
        }
        
        /** Private constructor.  Use the create() methods above. */
        private AccessPath(jq_Field f, Node n, boolean last) {
            this._field = f; this._n = n; this._last = last;
            this.succ = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
        }
        /** Private constructor.  Use the create() methods above. */
        private AccessPath(jq_Field f, Node n) {
            this(f, n, false);
        }
        
        /** Helper function for equals(AccessPath), below. */
        private boolean oneEquals(AccessPath that) {
            //if (this._n != that._n) return false;
            if (this._field != that._field) return false;
            if (this._last != that._last) return false;
            if (this.succ.size() != that.succ.size()) return false;
            return true;
        }
        /** Helper function for equals(AccessPath), below. */
        private boolean equals(AccessPath that, HashSet s) {
            // Relies on the fact that the iterators are stable for equivalent sets.
            // Otherwise, it is an n^2 algorithm.
            for (Iterator i = this.succ.iterator(), j = that.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper a = (IdentityHashCodeWrapper)i.next();
                IdentityHashCodeWrapper b = (IdentityHashCodeWrapper)j.next();
                AccessPath p = (AccessPath)a.getObject();
                AccessPath q = (AccessPath)b.getObject();
                if (!p.oneEquals(q)) return false;
                if (s.contains(a)) continue;
                s.add(a);
                if (!p.equals(q, s)) return false;
            }
            return true;
        }
        /** Returns true if this access path is equal to the given access path. */
        public boolean equals(AccessPath that) {
            HashSet s = new HashSet();
            if (!oneEquals(that)) return false;
            s.add(IdentityHashCodeWrapper.create(this));
            return this.equals(that, s);
        }
        public boolean equals(Object o) {
            if (o instanceof AccessPath) return equals((AccessPath)o);
            return false;
        }
        /** Returns the hashcode for this access path. */
        public int hashCode() {
            int x = this.local_hashCode();
            for (Iterator i = this.succ.iterator(); i.hasNext(); ) {
                IdentityHashCodeWrapper a = (IdentityHashCodeWrapper)i.next();
                x ^= (((AccessPath)a.getObject()).local_hashCode() << 1);
            }
            return x;
        }
        /** Returns the hashcode for this individual AccessPath object. */
        private int local_hashCode() {
            return _field != null ? _field.hashCode() : 0x31337;
        }
        /** Returns the first field of this access path. */
        public jq_Field first() { return _field; }
        /** Returns an iteration of the next AccessPath objects. */
        public Iterator next() {
            return new FilterIterator(succ.iterator(), filter);
        }
        /** A filter to unwrap objects from their IdentityHashCodeWrapper. */
        public static final Filter filter = new Filter() {
            public Object map(Object o) { return ((IdentityHashCodeWrapper)o).getObject(); }
        };
    }
    
    /** vvvvv   Actual MethodSummary stuff is below.   vvvvv */
    
    /** The method that this is a summary for. */
    final jq_Method method;
    /** The parameter nodes. */
    final ParamNode[] params;
    /** All nodes in the summary graph. */
    final Map nodes;
    /** The returned nodes. */
    final Set returned;
    /** The thrown nodes. */
    final Set thrown;
    /** The global node. */
    /*final*/ GlobalNode global;
    /** The method calls that this method makes. */
    final Set calls;
    /** Map from a method call that this method makes, and its ReturnValueNode. */
    final Map callToRVN;
    /** Map from a method call that this method makes, and its ThrownExceptionNode. */
    final Map callToTEN;
    /** Map from a (node,castquad) pair to its cast node. */
    final Map castMap;
    /** Set of nodes being casts. */
    final Set castPredecessors;
    /** Map from a sync op to the nodes it operates on. */
    final Map sync_ops;
    
    BuildMethodSummary builder;
    
    public static final boolean USE_PARAMETER_MAP = true;
    final Map passedParamToNodes;

    public MethodSummary(ParamNode[] param_nodes) {
        this.method = null;
        this.params = param_nodes;
        this.calls = Collections.EMPTY_SET;
        this.callToRVN = Collections.EMPTY_MAP;
        this.callToTEN = Collections.EMPTY_MAP;
        this.nodes = Collections.EMPTY_MAP;
        this.returned = Collections.EMPTY_SET;
        this.thrown = Collections.EMPTY_SET;
        this.castMap = Collections.EMPTY_MAP;
        this.castPredecessors = Collections.EMPTY_SET;
        this.passedParamToNodes = Collections.EMPTY_MAP;
        this.sync_ops = Collections.EMPTY_MAP;
    }

    public static boolean CACHE_BUILDER = true;

    public MethodSummary(BuildMethodSummary builder,
                         jq_Method method,
                         ParamNode[] param_nodes,
                         GlobalNode my_global,
                         Set methodCalls,
                         Map callToRVN,
                         Map callToTEN,
                         Map castMap,
                         Set castPredecessors,
                         Set returned,
                         Set thrown,
                         Set passedAsParameters,
                         Map sync_ops) {
        this.method = method;
        this.params = param_nodes;
        this.calls = methodCalls;
        this.callToRVN = callToRVN;
        this.callToTEN = callToTEN;
        this.passedParamToNodes = USE_PARAMETER_MAP?new HashMap():null;
        this.sync_ops = sync_ops;
        this.castMap = castMap;
        this.castPredecessors = castPredecessors;
        this.returned = returned;
        this.thrown = thrown;
        this.global = my_global;
        if (CACHE_BUILDER)
            this.builder = builder;
        this.nodes = new LinkedHashMap();
        
        // build useful node set
        this.nodes.put(my_global, my_global);
        for (int i=0; i<params.length; ++i) {
            if (params[i] == null) continue;
            this.nodes.put(params[i], params[i]);
        }
        for (Iterator i=returned.iterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            if (n instanceof UnknownTypeNode) continue;
            this.nodes.put(n, n);
        }
        for (Iterator i=thrown.iterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            if (n instanceof UnknownTypeNode) continue;
            this.nodes.put(n, n);
        }
        for (Iterator i=passedAsParameters.iterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            if (n instanceof UnknownTypeNode) continue;
            this.nodes.put(n, n);
            if (USE_PARAMETER_MAP) {
                if (n.passedParameters != null) {
                    for (Iterator j=n.passedParameters.iterator(); j.hasNext(); ) {
                        PassedParameter pp = (PassedParameter)j.next();
                        Set s2 = (Set)this.passedParamToNodes.get(pp);
                        if (s2 == null) this.passedParamToNodes.put(pp, s2 = NodeSet.FACTORY.makeSet());
                        s2.add(n);
                    }
                }
            }
        }
        
        HashSet visited = new HashSet();
        HashSet path = new HashSet();
        addAsUseful(visited, path, my_global);
        for (int i=0; i<params.length; ++i) {
            if (params[i] == null) continue;
            addAsUseful(visited, path, params[i]);
        }
        for (Iterator i=returned.iterator(); i.hasNext(); ) {
            addAsUseful(visited, path, (Node)i.next());
        }
        for (Iterator i=thrown.iterator(); i.hasNext(); ) {
            addAsUseful(visited, path, (Node)i.next());
        }
        for (Iterator i=passedAsParameters.iterator(); i.hasNext(); ) {
            addAsUseful(visited, path, (Node)i.next());
        }
        // castPredecessors is redundant
        castPredecessors = null;        
        
        if (UNIFY_ACCESS_PATHS) {
            HashSet roots = new HashSet();
            for (int i=0; i<params.length; ++i) {
                if (params[i] == null) continue;
                roots.add(params[i]);
            }
            roots.addAll(returned); roots.addAll(thrown); roots.addAll(passedAsParameters);
            unifyAccessPaths(roots);
        }
        
        if (VERIFY_ASSERTIONS) {
            this.verify();
            for (Iterator i=returned.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof UnknownTypeNode) continue;
                if (!nodes.containsKey(o)) {
                    Assert.UNREACHABLE("Returned node "+o+" not in set.");
                }
            }
            for (Iterator i=thrown.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof UnknownTypeNode) continue;
                if (!nodes.containsKey(o)) {
                    Assert.UNREACHABLE("Returned node "+o+" not in set.");
                }
            }
            for (Iterator i=nodes.keySet().iterator(); i.hasNext(); ) {
                Node nod = (Node)i.next();
                if (nod.predecessors == null) continue;
                for (Iterator j=nod.predecessors.values().iterator(); j.hasNext(); ) {
                    Object o = j.next();
                    if (o instanceof Node) {
                        if (o instanceof UnknownTypeNode) continue;
                        if (!nodes.containsKey(o)) {
                            Assert.UNREACHABLE("Predecessor node "+o+" of "+nod+" not in set.");
                        }
                    } else {
                        for (Iterator k=((Set)o).iterator(); k.hasNext(); ) {
                            Node q = (Node)k.next();
                            if (q instanceof UnknownTypeNode) continue;
                            if (!nodes.containsKey(q)) {
                                Assert.UNREACHABLE("Predecessor node "+q+" of "+nod+" not in set.");
                            }
                        }
                    }
                }
            }
            //this.copy();
        }
    }

    public static final boolean UNIFY_ACCESS_PATHS = false;
    
    private MethodSummary(BuildMethodSummary builder,
                          jq_Method method,
                          ParamNode[] params,
                          Set methodCalls,
                          Map callToRVN,
                          Map callToTEN,
                          Map castMap,
                          Set castPredecessors,
                          Map passedParamToNodes,
                          Map sync_ops,
                          Set returned,
                          Set thrown,
                          Map nodes) {
        this.method = method;
        this.params = params;
        this.calls = methodCalls;
        this.callToRVN = callToRVN;
        this.callToTEN = callToTEN;
        this.castMap = castMap;
        this.castPredecessors = castPredecessors;
        this.passedParamToNodes = passedParamToNodes;
        this.sync_ops = sync_ops;
        this.returned = returned;
        this.thrown = thrown;
        this.nodes = nodes;
        this.builder = builder;
    }

    /** Get the global node for this method. */
    public GlobalNode getGlobal() { return global; }

    /** Get the ith parameter node. */
    public ParamNode getParamNode(int i) { return params[i]; }
    
    /** Get the number of parameters passed into this method. */
    public int getNumOfParams() { return params.length; }
    
    /** Get the set of method calls made by this method. */
    public Set getCalls() { return calls; }

    /** Add all nodes that are passed as the given passed parameter to the given result set. */
    public void getNodesThatCall(PassedParameter pp, Set result) {
        if (USE_PARAMETER_MAP) {
            Set s = (Set)passedParamToNodes.get(pp);
            if (s == null) return;
            result.addAll(s);
            return;
        }
        for (Iterator i = this.nodeIterator(); i.hasNext(); ) {
            Node n = (Node)i.next();
            if ((n.passedParameters != null) && n.passedParameters.contains(pp))
                result.add(n);
        }
    }

    public Set getNodesThatCall(ProgramLocation mc, int k) {
        return getNodesThatCall(new PassedParameter(mc, k));
    }
    
    /** Return the set of nodes that are passed as the given parameter. */
    public Set getNodesThatCall(PassedParameter pp) {
        if (USE_PARAMETER_MAP) {
            Set s = (Set)passedParamToNodes.get(pp);
            if (s == null) return Collections.EMPTY_SET;
            return s;
        }
        Set s = NodeSet.FACTORY.makeSet();
        getNodesThatCall(pp, s);
        return s;
    }
    
    /** Merge the global node for this method summary with the main global node. */
    public void mergeGlobal() {
        if (global == null) return;
        // merge global nodes.
        Set set = Collections.singleton(GlobalNode.GLOBAL);
        global.replaceBy(set, true);
        nodes.remove(global);
        unifyAccessPaths(new LinkedHashSet(set));
        if (VERIFY_ASSERTIONS) {
            verifyNoReferences(global);
        }
        global = null;
    }
        
    /** Utility function to add to a multi map. */
    public static boolean addToMultiMap(HashMap mm, Object from, Object to) {
        Set s = (Set) mm.get(from);
        if (s == null) {
            mm.put(from, s = NodeSet.FACTORY.makeSet());
        }
        return s.add(to);
    }

    /** Utility function to add to a multi map. */
    public static boolean addToMultiMap(HashMap mm, Object from, Set to) {
        Set s = (Set) mm.get(from);
        if (s == null) {
            mm.put(from, s = NodeSet.FACTORY.makeSet());
        }
        return s.addAll(to);
    }

    /** Utility function to get the mapping for a callee node. */
    static Set get_mapping(HashMap callee_to_caller, Node callee_n) {
        Set s = (Set)callee_to_caller.get(callee_n);
        if (s != null) return s;
        s = NodeSet.FACTORY.makeSet(); s.add(callee_n);
        return s;
    }

    /** Return a deep copy of this analysis summary.
     *  Nodes, edges, everything is copied.
     */
    public MethodSummary copy() {
        if (TRACE_INTRA) out.println("Copying summary: "+this);
        if (VERIFY_ASSERTIONS) this.verify();
        HashMap m = new HashMap();
        //m.put(GlobalNode.GLOBAL, GlobalNode.GLOBAL);
        for (Iterator i=nodeIterator(); i.hasNext(); ) {
            Node a = (Node)i.next();
            Node b = a.copy();
            m.put(a, b);
        }
        for (Iterator i=nodeIterator(); i.hasNext(); ) {
            Node a = (Node)i.next();
            Node b = (Node)m.get(a);
            b.update(m);
        }
        Set calls = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
        calls.addAll(this.calls);
        Set returned = NodeSet.FACTORY.makeSet();
        for (Iterator i=this.returned.iterator(); i.hasNext(); ) {
            Node a = (Node)i.next();
            Node b = (Node)m.get(a);
            if (a instanceof UnknownTypeNode) b = a;
            Assert._assert(b != null);
            returned.add(b);
        }
        Set thrown = NodeSet.FACTORY.makeSet();
        for (Iterator i=this.thrown.iterator(); i.hasNext(); ) {
            Node a = (Node)i.next();
            Node b = (Node)m.get(a);
            if (a instanceof UnknownTypeNode) b = a;
            Assert._assert(b != null);
            thrown.add(b);
        }
        ParamNode[] params = new ParamNode[this.params.length];
        for (int i=0; i<params.length; ++i) {
            if (this.params[i] == null) continue;
            params[i] = (ParamNode)m.get(this.params[i]);
        }
        HashMap callToRVN = new HashMap();
        for (Iterator i=this.callToRVN.entrySet().iterator(); i.hasNext(); ) {
            java.util.Map.Entry e = (java.util.Map.Entry)i.next();
            ProgramLocation mc = (ProgramLocation) e.getKey();
            Object o = e.getValue();
            if (o != null) {
                o = m.get(o);
                Assert._assert(o != null, e.toString());
            }
            callToRVN.put(mc, o);
        }
        HashMap callToTEN = new HashMap();
        for (Iterator i=this.callToTEN.entrySet().iterator(); i.hasNext(); ) {
            java.util.Map.Entry e = (java.util.Map.Entry)i.next();
            ProgramLocation mc = (ProgramLocation) e.getKey();
            Object o = e.getValue();
            if (o != null) {
                o = m.get(o);
                Assert._assert(o != null, e.toString());
            }
            callToTEN.put(mc, o);
        }
        LinkedHashMap nodes = new LinkedHashMap();
        for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
            java.util.Map.Entry e = (java.util.Map.Entry)i.next();
            Assert._assert(e.getValue() != GlobalNode.GLOBAL);
            Assert._assert(!(e.getValue() instanceof UnknownTypeNode));
            nodes.put(e.getValue(), e.getValue());
        }
        Map passedParamToNodes = null;
        if (USE_PARAMETER_MAP) {
            passedParamToNodes = new HashMap(this.passedParamToNodes);
            Node.updateMap(m, passedParamToNodes.entrySet().iterator(), passedParamToNodes);
        }
        MethodSummary that = new MethodSummary(builder, method, params, calls, callToRVN, callToTEN, castMap, castPredecessors, passedParamToNodes, sync_ops, returned, thrown, nodes);
        if (VERIFY_ASSERTIONS) that.verify();
        return that;
    }

    /** Unify similar access paths from the given roots.
     *  The given set is consumed.
     */
    public void unifyAccessPaths(Set roots) {
        LinkedList worklist = new LinkedList();
        for (Iterator i = roots.iterator(); i.hasNext(); ) {
            worklist.add(i.next());
        }
        while (!worklist.isEmpty()) {
            Node n = (Node) worklist.removeFirst();
            if (n instanceof UnknownTypeNode) continue;
            unifyAccessPathEdges(n);
            for (Iterator i = n.getAccessPathEdges().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                FieldNode n2 = (FieldNode) e.getValue();
                Assert._assert(n2 != null);
                if (roots.contains(n2)) continue;
                worklist.add(n2); roots.add(n2);
            }
            for (Iterator i=n.getNonEscapingEdges().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                Object o = e.getValue();
                if (o instanceof Node) {
                    Node n2 = (Node)o;
                    Assert._assert(n2 != null);
                    if (roots.contains(n2)) continue;
                    worklist.add(n2); roots.add(n2);
                } else {
                    Set s = NodeSet.FACTORY.makeSet((Set) o);
                    for (Iterator j = s.iterator(); j.hasNext(); ) {
                        Object p = j.next();
                        Assert._assert(p != null);
                        if (roots.contains(p)) j.remove();
                    }
                    if (!s.isEmpty()) {
                        worklist.addAll(s); roots.addAll(s);
                    }
                }
            }
        }
    }

    /** Unify similar access path edges from the given node.
     */
    public void unifyAccessPathEdges(Node n) {
        if (n instanceof UnknownTypeNode) return;
        if (TRACE_INTRA) out.println("Unifying access path edges from: "+n);
        if (n.accessPathEdges != null) {
            for (Iterator i = n.accessPathEdges.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry)i.next();
                jq_Field f = (jq_Field)e.getKey();
                Object o = e.getValue();
                Assert._assert(o != null);
                FieldNode n2;
                if (o instanceof FieldNode) {
                    n2 = (FieldNode) o;
                } else {
                    Set s = (Set) NodeSet.FACTORY.makeSet((Set) o);
                    if (s.size() == 0) {
                        i.remove();
                        continue;
                    }
                    if (s.size() == 1) {
                        n2 = (FieldNode) s.iterator().next();
                        e.setValue(n2);
                        continue;
                    }
                    if (TRACE_INTRA) out.println("Node "+n+" has duplicate access path edges on field "+f+": "+s);
                    n2 = FieldNode.unify(f, s);
                    for (Iterator j = s.iterator(); j.hasNext(); ) {
                        FieldNode n3 = (FieldNode)j.next();
                        if (returned.contains(n3)) {
                            returned.remove(n3); returned.add(n2);
                        }
                        if (thrown.contains(n3)) {
                            thrown.remove(n3); thrown.add(n2);
                        }
                        nodes.remove(n3);
                        if (VERIFY_ASSERTIONS)
                            this.verifyNoReferences(n3);
                    }
                    nodes.put(n2, n2);
                    e.setValue(n2);
                }
            }
        }
    }

    /** Instantiate a copy of the callee summary into the caller. */
    public static void instantiate(MethodSummary caller, ProgramLocation mc, MethodSummary callee, boolean removeCall) {
        callee = callee.copy();
        if (TRACE_INST) out.println("Instantiating "+callee+" into "+caller+", mc="+mc+" remove call="+removeCall);
        if (VERIFY_ASSERTIONS) {
            callee.verify();
            caller.verify();
        }
        Assert._assert(caller.calls.contains(mc));
        HashMap callee_to_caller = new HashMap();
        if (TRACE_INST) out.println("Adding global node to map: "+GlobalNode.GLOBAL.toString_long());
        callee_to_caller.put(GlobalNode.GLOBAL, GlobalNode.GLOBAL);
        if (TRACE_INST) out.println("Initializing map with "+callee.params.length+" parameters");
        // initialize map with parameters.
        for (int i=0; i<callee.params.length; ++i) {
            ParamNode pn = callee.params[i];
            if (pn == null) continue;
            PassedParameter pp = new PassedParameter(mc, i);
            Set s = caller.getNodesThatCall(pp);
            if (TRACE_INST) out.println("Adding param node to map: "+pn.toString_long()+" maps to "+s);
            callee_to_caller.put(pn, s);
            if (removeCall) {
                if (TRACE_INST) out.println("Removing "+pn+" from nodes "+s);
                for (Iterator jj=s.iterator(); jj.hasNext(); ) {
                    Node n = (Node)jj.next();
                    n.passedParameters.remove(pp);
                }
                if (USE_PARAMETER_MAP) caller.passedParamToNodes.remove(pp);
            }
        }
        
        if (TRACE_INST) out.println("Adding all callee calls: "+callee.calls);
        caller.calls.addAll(callee.calls);
        for (Iterator i=callee.callToRVN.entrySet().iterator(); i.hasNext(); ) {
            java.util.Map.Entry e = (java.util.Map.Entry) i.next();
            ProgramLocation mc2 = (ProgramLocation) e.getKey();
            if (VERIFY_ASSERTIONS) {
                Assert._assert(caller.calls.contains(mc2));
                Assert._assert(!mc.equals(mc2));
            }
            Object rvn2 = e.getValue();
            if (TRACE_INST) out.println("Adding rvn for callee call: "+rvn2);
            Object o2 = caller.callToRVN.get(mc2);
            Assert._assert(o2 == null);
            caller.callToRVN.put(mc2, rvn2);
        }
        for (Iterator i=callee.callToTEN.entrySet().iterator(); i.hasNext(); ) {
            java.util.Map.Entry e = (java.util.Map.Entry) i.next();
            ProgramLocation mc2 = (ProgramLocation) e.getKey();
            if (VERIFY_ASSERTIONS) {
                Assert._assert(caller.calls.contains(mc2));
                Assert._assert(!mc.equals(mc2));
            }
            Object ten2 = e.getValue();
            if (TRACE_INST) out.println("Adding ten for callee call: "+ten2);
            Object o2 = caller.callToTEN.get(mc2);
            Assert._assert(o2 == null);
            caller.callToTEN.put(mc2, ten2);
        }
        
        if (TRACE_INST) out.println("Replacing formal parameters with actuals");
        for (int ii=0; ii<callee.params.length; ++ii) {
            ParamNode pn = callee.params[ii];
            if (pn == null) continue;
            Set s = (Set)callee_to_caller.get(pn);
            if (TRACE_INST) out.println("Replacing "+pn+" by "+s);
            pn.replaceBy(s, removeCall);
            if (callee.returned.contains(pn)) {
                if (TRACE_INST) out.println(pn+" is returned, updating callee returned set");
                if (removeCall) {
                    callee.returned.remove(pn);
                }
                callee.returned.addAll(s);
            }
            if (callee.thrown.contains(pn)) {
                if (TRACE_INST) out.println(pn+" is thrown, updating callee thrown set");
                if (removeCall) {
                    callee.thrown.remove(pn);
                }
                callee.thrown.addAll(s);
            }
            if (removeCall) {
                callee.nodes.remove(pn);
                if (VERIFY_ASSERTIONS) callee.verifyNoReferences(pn);
            }
        }
        
        ReturnValueNode rvn = caller.getRVN(mc);
        if (rvn != null) {
            if (TRACE_INST) out.println("Replacing return value "+rvn+" by "+callee.returned);
            rvn.replaceBy(callee.returned, removeCall);
            if (caller.returned.contains(rvn)) {
                if (TRACE_INST) out.println(rvn+" is returned, updating returned set");
                if (removeCall) caller.returned.remove(rvn);
                caller.returned.addAll(callee.returned);
            }
            if (caller.thrown.contains(rvn)) {
                if (TRACE_INST) out.println(rvn+" is thrown, updating thrown set");
                if (removeCall) caller.thrown.remove(rvn);
                caller.thrown.addAll(callee.returned);
            }
            if (removeCall) {
                if (TRACE_INST) out.println("Removing old return value node "+rvn+" from nodes list");
                caller.nodes.remove(rvn);
            }
            if (VERIFY_ASSERTIONS && removeCall) caller.verifyNoReferences(rvn);
        }
        
        ThrownExceptionNode ten = caller.getTEN(mc);
        if (ten != null) {
            if (TRACE_INST) out.println("Replacing thrown exception "+ten+" by "+callee.thrown);
            ten.replaceBy(callee.thrown, removeCall);
            if (caller.returned.contains(ten)) {
                if (TRACE_INST) out.println(ten+" is returned, updating caller returned set");
                if (removeCall) caller.returned.remove(ten);
                caller.returned.addAll(callee.thrown);
            }
            if (caller.thrown.contains(ten)) {
                if (TRACE_INST) out.println(ten+" is thrown, updating caller thrown set");
                if (removeCall) caller.thrown.remove(ten);
                caller.thrown.addAll(callee.thrown);
            }
            if (removeCall) {
                if (TRACE_INST) out.println("Removing old thrown exception node "+ten+" from nodes list");
                caller.nodes.remove(ten);
            }
            if (VERIFY_ASSERTIONS && removeCall) caller.verifyNoReferences(ten);
        }
        
        if (TRACE_INST) out.println("Adding all callee nodes: "+callee.nodes);
        caller.nodes.putAll(callee.nodes);
        
        if (TRACE_INST) out.println("Building a root set for access path unification");
        Set s = NodeSet.FACTORY.makeSet();
        s.addAll(callee.returned);
        s.addAll(callee.thrown);
        for (int ii=0; ii<callee.params.length; ++ii) {
            ParamNode pn = callee.params[ii];
            if (pn == null) continue;
            Set t = (Set)callee_to_caller.get(pn);
            s.addAll(t);
        }
        if (TRACE_INST) out.println("Root set: "+s);
        caller.unifyAccessPaths(s);
        if (removeCall) {
            if (TRACE_INST) out.println("Removing instantiated call: "+mc);
            caller.calls.remove(mc);
            caller.callToRVN.remove(mc);
            caller.callToTEN.remove(mc);
        }
        
        if (VERIFY_ASSERTIONS) {
            caller.verify();
            //caller.copy();
        }
    }

    public jq_Method getMethod() { return method; }
    
    public int hashCode() {
        if (DETERMINISTIC)
            return method.hashCode();
        else
            return System.identityHashCode(this);
    }
    
    /** Return a string representation of this summary. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Summary for ");
        sb.append(method.toString());
        sb.append(':');
        sb.append(Strings.lineSep);
        for (Iterator i=nodes.keySet().iterator(); i.hasNext(); ) {
            Node n = (Node)i.next();
            sb.append(n.toString_long());
            sb.append(Strings.lineSep);
        }
        if (returned != null && !returned.isEmpty()) {
            sb.append("Returned: ");
            sb.append(returned);
            sb.append(Strings.lineSep);
        }
        if (thrown != null && !thrown.isEmpty()) {
            sb.append("Thrown: ");
            sb.append(thrown);
            sb.append(Strings.lineSep);
        }
        if (calls != null && !calls.isEmpty()) {
            sb.append("Calls: ");
            sb.append(calls);
            sb.append(Strings.lineSep);
        }
        return sb.toString();
    }

    /** Utility function to add the given node to the node set if it is useful,
     *  and transitively for other nodes. */
    private boolean addIfUseful(HashSet visited, HashSet path, Node n) {
        if (path.contains(n)) return true;
        path.add(n);
        if (visited.contains(n)) return nodes.containsKey(n);
        visited.add(n);
        boolean useful = false;
        if (nodes.containsKey(n)) {
            if (TRACE_INTER) out.println("Useful: "+n);
            useful = true;
        }
        if (n instanceof UnknownTypeNode) {
            path.remove(n);
            return true;
        }
        if (n.addedEdges != null) {
            if (TRACE_INTER) out.println("Useful because of added edge: "+n);
            useful = true;
            for (Iterator i = n.getNonEscapingEdgeTargets().iterator(); i.hasNext(); ) {
                addAsUseful(visited, path, (Node) i.next());
            }
        }
        if (n.accessPathEdges != null) {
            for (Iterator i = n.accessPathEdges.entrySet().iterator(); i.hasNext(); ) {
                java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                //jq_Field f = (jq_Field)e.getKey();
                Object o = e.getValue();
                if (o instanceof Node) {
                    if (addIfUseful(visited, path, (Node)o)) {
                        if (TRACE_INTER && !useful) out.println("Useful because outside edge: "+n+"->"+o);
                        useful = true;
                    } else {
                        if (n != o) i.remove();
                    }
                } else {
                    for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                        Node n2 = (Node)j.next();
                        if (addIfUseful(visited, path, n2)) {
                            if (TRACE_INTER && !useful) out.println("Useful because outside edge: "+n+"->"+n2);
                            useful = true;
                        } else {
                            if (n != n2) j.remove();
                        }
                    }
                    if (!useful) i.remove();
                }
            }
        }
        if (castPredecessors.contains(n)) {
            for (Iterator i = castMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry)i.next();
                Node goestocast = (Node)((Pair)e.getKey()).left;
                CheckCastNode castsucc = (CheckCastNode)e.getValue();
                // Call "addIfUseful()" on all successor checkcast nodes, and set the "useful" flag if the call returns true.
                if (n == goestocast && addIfUseful(visited, path, castsucc))
                    useful = true;
            }
        }
        if (n instanceof ReturnedNode) {
            if (TRACE_INTER && !useful) out.println("Useful because ReturnedNode: "+n);
            useful = true;
        }
        if (n.predecessors != null) {
            useful = true;
            if (TRACE_INTER && !useful) out.println("Useful because target of added edge: "+n);
            for (Iterator i = n.getPredecessorTargets().iterator(); i.hasNext(); ) {
                addAsUseful(visited, path, (Node) i.next());
            }
        }
        if (useful) {
            this.nodes.put(n, n);
            if (n instanceof FieldNode) {
                FieldNode fn = (FieldNode)n;
                for (Iterator i = fn.getAccessPathPredecessors().iterator(); i.hasNext(); ) {
                    addAsUseful(visited, path, (Node)i.next());
                }
            }
            if (n instanceof CheckCastNode) {
                // If the "useful" flag is true and the node is a checkcast node,
                // call "addAsUseful()" on all predecessors.
                Quad thiscast = ((QuadProgramLocation)((CheckCastNode)n).getLocation()).getQuad();
                for (Iterator i = castPredecessors.iterator(); i.hasNext(); ) {
                    Node goestocast = (Node)i.next();
                    CheckCastNode castsucc = (CheckCastNode)castMap.get(new Pair(goestocast, thiscast));
                    if (castsucc != null)
                        addAsUseful(visited, path, goestocast);
                }
            }
        }
        if (TRACE_INTER && !useful) out.println("Not useful: "+n);
        path.remove(n);
        return useful;
    }
    
    /** Utility function to add the given node to the node set as useful,
     *  and transitively for other nodes. */
    private void addAsUseful(HashSet visited, HashSet path, Node n) {
        if (path.contains(n)) {
            return;
        }
        path.add(n);
        if (visited.contains(n)) {
            if (VERIFY_ASSERTIONS) Assert._assert(nodes.containsKey(n), n.toString());
            return;
        }
        if (n instanceof UnknownTypeNode) {
            path.remove(n);
            return;
        }
        visited.add(n); this.nodes.put(n, n);
        if (TRACE_INTER) out.println("Useful: "+n);
        for (Iterator i = n.getNonEscapingEdgeTargets().iterator(); i.hasNext(); ) {
            addAsUseful(visited, path, (Node) i.next());
        }
        if (n.accessPathEdges != null) {
            for (Iterator i=n.accessPathEdges.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                Object o = e.getValue();
                if (o instanceof Node) {
                    if (!addIfUseful(visited, path, (Node)o)) {
                        i.remove();
                    }
                } else {
                    boolean any = false;
                    for (Iterator j=((Set)o).iterator(); j.hasNext(); ) {
                        Node j_n = (Node)j.next();
                        if (!addIfUseful(visited, path, j_n)) {
                            j.remove();
                        } else {
                            any = true;
                        }
                    }
                    if (!any) i.remove();
                }
            }
        }
        for (Iterator i = n.getPredecessorTargets().iterator(); i.hasNext(); ) {
            addAsUseful(visited, path, (Node) i.next());
        }
        if (n instanceof FieldNode) {
            FieldNode fn = (FieldNode) n;
            for (Iterator i = fn.getAccessPathPredecessors().iterator(); i.hasNext(); ) {
                addAsUseful(visited, path, (Node)i.next());
            }
        }
        // Call "addIfUseful()" on all successor checkcast nodes.
        if (castPredecessors.contains(n)) {
            for (Iterator i = castMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry)i.next();
                Node goestocast = (Node)((Pair)e.getKey()).left;
                CheckCastNode castsucc = (CheckCastNode)e.getValue();
                if (n == goestocast)
                    addIfUseful(visited, path, castsucc);
            }
        }

        // call "addAsUseful()" on all predecessors.
        if (n instanceof CheckCastNode) {
            Quad thiscast = ((QuadProgramLocation)((CheckCastNode)n).getLocation()).getQuad();
            for (Iterator i = castPredecessors.iterator(); i.hasNext(); ) {
                Node goestocast = (Node)i.next();
                CheckCastNode castsucc = (CheckCastNode)castMap.get(new Pair(goestocast, thiscast));
                if (castsucc != null)
                    addAsUseful(visited, path, goestocast);
            }
        }
        path.remove(n);
    }

    /** Returns an iteration of all nodes in this summary. */
    public Iterator nodeIterator() { return nodes.keySet().iterator(); }

    /** Get the set of returned nodes. */
    public Set getReturned() {
        return returned;
    }
    
    /** Get the set of thrown nodes. */
    public Set getThrown() {
        return thrown;
    }

    /** Get the map of casts. */
    public Map getCastMap() {
        return castMap;
    }

    /** Get the return value node corresponding to the given method call. */
    public ReturnValueNode getRVN(ProgramLocation mc) {
        return (ReturnValueNode) callToRVN.get(mc);
    }
    
    /** Get the thrown exception node corresponding to the given method call. */
    public ThrownExceptionNode getTEN(ProgramLocation mc) {
        return (ThrownExceptionNode) callToTEN.get(mc);
    }

    /** Verify the integrity of the method summary data structure. */
    void verify() {
        for (int i=0; i<this.params.length; ++i) {
            if (this.params[i] == null) continue;
            if (!nodes.containsKey(this.params[i])) {
                Assert.UNREACHABLE(this.params[i].toString_long());
            }
        }
        for (Iterator i=returned.iterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            if (n instanceof UnknownTypeNode) continue;
            if (!nodes.containsKey(n)) {
                Assert.UNREACHABLE(n.toString_long());
            }
        }
        for (Iterator i=thrown.iterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            if (n instanceof UnknownTypeNode) continue;
            if (!nodes.containsKey(n)) {
                Assert.UNREACHABLE(n.toString_long());
            }
        }
        for (Iterator i=callToRVN.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (!calls.contains(e.getKey())) {
                Assert.UNREACHABLE(e.toString());
            }
            Object o = e.getValue();
            if (o != null) {
                if (!nodes.containsKey(o)) {
                    Assert.UNREACHABLE(e.toString());
                }
            }
        }
        for (Iterator i=callToTEN.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (!calls.contains(e.getKey())) {
                Assert.UNREACHABLE(e.toString());
            }
            Object o = e.getValue();
            if (o instanceof Set) {
                for (Iterator j=((Set) o).iterator(); j.hasNext(); ) {
                    Object o2 = j.next();
                    if (!nodes.containsKey(o2)) {
                        Assert.UNREACHABLE(e.toString());
                    }
                }
            } else if (o != null) {
                if (!nodes.containsKey(o)) {
                    Assert.UNREACHABLE(e.toString());
                }
            }
        }
        for (Iterator i=nodeIterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            if (n instanceof UnknownTypeNode) {
                Assert.UNREACHABLE(n.toString_long());
            }
            if (n.addedEdges != null) {
                for (Iterator j=n.addedEdges.entrySet().iterator(); j.hasNext(); ) {
                    Map.Entry e = (Map.Entry) j.next();
                    jq_Field f = (jq_Field) e.getKey();
                    Object o = e.getValue();
                    if (o instanceof Node) {
                        Node n2 = (Node) o;
                        if (!(n2 instanceof UnknownTypeNode) && !nodes.containsKey(n2)) {
                            Assert.UNREACHABLE(n2.toString_long());
                        }
                        if (!n2.hasPredecessor(f, n)) {
                            Assert.UNREACHABLE(n2.toString_long()+" has no predecessor "+n.toString_long());
                        }
                    } else if (o == null) {
                        
                    } else {
                        Set s = (Set) o;
                        for (Iterator k=s.iterator(); k.hasNext(); ) {
                            Node n2 = (Node) k.next();
                            if (!(n2 instanceof UnknownTypeNode) && !nodes.containsKey(n2)) {
                                Assert.UNREACHABLE(n2.toString_long());
                            }
                            if (!n2.hasPredecessor(f, n)) {
                                Assert.UNREACHABLE(n2.toString_long()+" has no predecessor "+n.toString_long());
                            }
                        }
                    }
                }
            }
            if (n.predecessors != null) {
                for (Iterator j=n.predecessors.entrySet().iterator(); j.hasNext(); ) {
                    Map.Entry e = (Map.Entry) j.next();
                    jq_Field f = (jq_Field) e.getKey();
                    Object o = e.getValue();
                    if (o instanceof Node) {
                        Node n2 = (Node) o;
                        if (n2 != GlobalNode.GLOBAL && !(n2 instanceof UnknownTypeNode) && !nodes.containsKey(n2)) {
                            Assert.UNREACHABLE(this.toString()+" ::: "+n2);
                        }
                        if (!n2.hasNonEscapingEdge(f, n)) {
                            Assert.UNREACHABLE(this.toString()+" ::: "+n2+" -> "+n);
                        }
                    } else if (o == null) {
                        
                    } else {
                        Set s = (Set) o;
                        for (Iterator k=s.iterator(); k.hasNext(); ) {
                            Node n2 = (Node) k.next();
                            if (n2 != GlobalNode.GLOBAL && !(n2 instanceof UnknownTypeNode) && !nodes.containsKey(n2)) {
                                Assert.UNREACHABLE(n2.toString_long());
                            }
                            if (!n2.hasNonEscapingEdge(f, n)) {
                                Assert.UNREACHABLE(n2.toString_long()+" has no edge "+n.toString_long());
                            }
                        }
                    }
                }
            }
            if (n.accessPathEdges != null) {
                for (Iterator j = n.accessPathEdges.entrySet().iterator(); j.hasNext(); ) {
                    Map.Entry e = (Map.Entry) j.next();
                    //jq_Field f = (jq_Field) e.getKey();
                    Object o = e.getValue();
                    if (o instanceof FieldNode) {
                        FieldNode n2 = (FieldNode) o;
                        if (!nodes.containsKey(n2)) {
                            Assert.UNREACHABLE(n2.toString_long());
                        }
                        if (!n2.field_predecessors.contains(n)) {
                            Assert.UNREACHABLE(n2.toString_long()+" has no field pred "+n.toString_long());
                        }
                    } else if (o == null) {
                        
                    } else {
                        Set s = (Set) o;
                        for (Iterator k=s.iterator(); k.hasNext(); ) {
                            FieldNode n2 = (FieldNode) k.next();
                            if (!nodes.containsKey(n2)) {
                                Assert.UNREACHABLE(n2.toString_long());
                            }
                            if (!n2.field_predecessors.contains(n)) {
                                Assert.UNREACHABLE(n2.toString_long()+" has no field pred "+n.toString_long());
                            }
                        }
                    }
                }
            }
            if (n instanceof FieldNode) {
                FieldNode fn = (FieldNode) n;
                if (fn.field_predecessors != null) {
                    jq_Field f = (jq_Field) fn.f;
                    for (Iterator j=fn.field_predecessors.iterator(); j.hasNext(); ) {
                        Node n2 = (Node) j.next();
                        if (n2 != GlobalNode.GLOBAL && !(n2 instanceof UnknownTypeNode) && !nodes.containsKey(n2)) {
                            Assert.UNREACHABLE(this.toString()+" ::: "+n2.toString_long());
                        }
                        if (!n2.hasAccessPathEdge(f, fn)) {
                            Assert.UNREACHABLE(this.toString()+" ::: "+n2.toString_long()+" => "+fn.toString_long());
                        }
                    }
                }
            }
            if (n instanceof ReturnValueNode) {
                if (!callToRVN.containsValue(n)) {
                    Assert.UNREACHABLE(n.toString_long());
                }
            }
            if (n instanceof ThrownExceptionNode) {
                if (!callToTEN.containsValue(n)) {
                    System.out.println(callToTEN);
                    Assert.UNREACHABLE(this.toString()+" ::: "+n.toString_long());
                }
            }
        }
    }

    /** Helper function for multiset contains relation. */
    static boolean multiset_contains(Map m, Object o) {
        if (m == null) return false;
        for (Iterator i = m.values().iterator(); i.hasNext(); ) {
            Object p = i.next();
            if (p == o) return true;
            if (p instanceof Collection)
                if (((Collection) p).contains(o)) return true;
        }
        return false;
    }

    public Collection getSyncedVars() {
        return new FlattenedCollection(this.sync_ops.values());
    }
    
    /** Verify that there are no references to the given node in this method summary. */
    void verifyNoReferences(Node n) {
        if (returned.contains(n))
            Assert.UNREACHABLE("ERROR: returned set contains "+n);
        if (thrown.contains(n))
            Assert.UNREACHABLE("ERROR: thrown set contains "+n);
        if (false) {
            for (int i=0; i<this.params.length; ++i) {
                if (this.params[i] == n)
                    Assert.UNREACHABLE("ERROR: param #"+i+" "+n);
            }
        }
        for (Iterator i = nodeIterator(); i.hasNext(); ) {
            Node n2 = (Node) i.next();
            if (n2 instanceof UnknownTypeNode) continue;
            if (multiset_contains(n2.addedEdges, n)) {
                Assert.UNREACHABLE("ERROR: "+n2+" contains an edge to "+n);
            }
            if (multiset_contains(n2.predecessors, n)) {
                Assert.UNREACHABLE("ERROR: "+n2+" contains predecessor "+n);
            }
            if (multiset_contains(n2.accessPathEdges, n)) {
                Assert.UNREACHABLE("ERROR: "+n2+" contains access path edge to "+n);
            }
            if (n2 instanceof FieldNode) {
                FieldNode fn = (FieldNode) n2;
                if (fn.field_predecessors != null &&
                    fn.field_predecessors.contains(n)) {
                    Assert.UNREACHABLE("ERROR: "+fn+" contains a field predecessor "+n);
                }
            }
        }
    }

    /** Dumps this method summary as a dot graph. */
    public void dotGraph(BufferedWriter out) throws IOException {
        out.write("digraph \""+this.method+"\" {\n");
        IndexMap m = new IndexMap("MethodCallMap");
        for (Iterator i=nodeIterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            out.write("n"+n.id+" [label=\""+n.toString_short()+"\"];\n");
        }
        for (Iterator i=getCalls().iterator(); i.hasNext(); ) {
            ProgramLocation mc = (ProgramLocation) i.next();
            int k = m.get(mc);
            out.write("mc"+k+" [label=\""+mc+"\"];\n");
        }
        for (Iterator i=nodeIterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            for (Iterator j=n.getNonEscapingEdges().iterator(); j.hasNext(); ) {
                Map.Entry e = (Map.Entry) j.next();
                String fieldName = ""+e.getKey();
                Iterator k;
                if (e.getValue() instanceof Set) k = ((Set)e.getValue()).iterator();
                else k = Collections.singleton(e.getValue()).iterator();
                while (k.hasNext()) {
                    Node n2 = (Node) k.next();
                    out.write("n"+n.id+" -> n"+n2.id+" [label=\""+fieldName+"\"];\n");
                }
            }
            for (Iterator j=n.getAccessPathEdges().iterator(); j.hasNext(); ) {
                Map.Entry e = (Map.Entry) j.next();
                String fieldName = ""+e.getKey();
                Iterator k;
                if (e.getValue() instanceof Set) k = ((Set)e.getValue()).iterator();
                else k = Collections.singleton(e.getValue()).iterator();
                while (k.hasNext()) {
                    Node n2 = (Node) k.next();
                    out.write("n"+n.id+" -> n"+n2.id+" [label=\""+fieldName+"\",style=dashed];\n");
                }
            }
            if (n.getPassedParameters() != null) {
                for (Iterator j=n.getPassedParameters().iterator(); j.hasNext(); ) {
                    PassedParameter pp = (PassedParameter) j.next();
                    int k = m.get(pp.m);
                    out.write("n"+n.id+" -> mc"+k+" [label=\"p"+pp.paramNum+"\",style=dotted];\n");
                }
            }
            if (n instanceof ReturnedNode) {
                ReturnedNode rn = (ReturnedNode) n;
                int k = m.get(rn.m);
                out.write("mc"+k+" -> n"+n.id+" [label=\"r\",style=dotted];\n");
            }
        }
        for (Iterator i=castMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            Node n = (Node)((Pair)e.getKey()).left;
            Node n2 = (Node)e.getValue();
            if (nodes.containsKey(n2))
                out.write("n"+n.id+" -> n"+n2.id+" [label=\"(cast)\"];\n");
        }
        out.write("}\n");
    }

    public static final boolean DUMP_DOTGRAPH = System.getProperty("ms.dotgraph") != null;
    
    public static void main(String[] args) throws IOException {
        HostedVM.initialize();
        joeq.ClassLib.ClassLibInterface.useJoeqClasslib(true);
        jq_Class c = (jq_Class) jq_Type.parseType(args[0]);
        c.load();
        String name = null, desc = null;
        if (args.length > 1) {
            name = args[1];

            if (name.equals(fakeCloneName)) {
                MethodSummary ms = fakeCloneMethodSummary((jq_FakeInstanceMethod)jq_FakeInstanceMethod.fakeMethod(c, 
                                                                                fakeCloneName, "()"+c.getName()));
                if (DUMP_DOTGRAPH) ms.dotGraph(new BufferedWriter(new OutputStreamWriter(System.out)));
                else System.out.println(ms);
                return;
            }

            if (args.length > 2) {
                desc = args[2];
            }
        }
        Collection methods;
        if (name != null) {
            jq_Method m;
            if (desc != null) {
                m = (jq_Method) c.getDeclaredMember(name, desc);
            } else {
                m = c.getDeclaredMethod(name);
            }
            methods = Collections.singleton(m);
        } else {
            methods = new LinkedList();
            methods.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            methods.addAll(Arrays.asList(c.getDeclaredInstanceMethods()));
        }
        
        for (Iterator i = methods.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m.getBytecode() == null) continue;
            ControlFlowGraph cfg = CodeCache.getCode(m);
//            out.println("CFG BEFORE SSA:");
//            out.println(cfg.fullDump());
            if (SSA) new EnterSSA().visitCFG(cfg);
//            out.println("CFG AFTER SSA:");
//            out.println(cfg.fullDump());
            MethodSummary ms = getSummary(cfg);
            if (DUMP_DOTGRAPH) ms.dotGraph(new BufferedWriter(new OutputStreamWriter(System.out)));
            else System.out.println(ms);
        }
    }
    
    public Collection getRegisterAtLocation(BasicBlock bb, Quad q, Register r) {
        builder.updateLocation(bb, q);
        Object o = builder.getRegister(r);
        if (o instanceof Node) return Collections.singleton(o);
        else return ((Collection) o);
    }
    
    /**
     * fake method summaries for fake methods.
     */
    private static HashMap fakeCache = new HashMap();
    public static MethodSummary fakeMethodSummary(jq_Method method) {
        MethodSummary ms = (MethodSummary)fakeCache.get(method);
        if (ms != null)
            return ms;

        boolean mustfake = method instanceof jq_FakeInstanceMethod;
        if (mustfake && method.getName().toString().equals(fakeCloneName)) {
            ms = fakeCloneMethodSummary((jq_FakeInstanceMethod)method);
        } else if (identityMethods.contains(method)) {
            ms = fakeIdentityMethodSummary(method);
        } else {
            if (!mustfake)
                return null;
            throw new Error("don't know how to fake " + method);
        }
        fakeCache.put(method, ms);
        return ms;
    }

    private static HashSet/*jq_Method*/ identityMethods = new HashSet();
    {
        jq_Class throwable_class = PrimordialClassLoader.getJavaLangThrowable();
        identityMethods.add(throwable_class.getDeclaredMember("fillInStackTrace", "()Ljava/lang/Throwable;"));
    }

    /**
     * fake a method summary that simulates the effect of '{ return this; }'
     */
    static MethodSummary fakeIdentityMethodSummary(jq_Method method) {
        jq_Class clazz = method.getDeclaringClass();
        ParamNode []params = new ParamNode[] { FakeParamNode.getFake(method, 0, clazz) };

        return new MethodSummary((BuildMethodSummary)null,
                         method,
                         params, 
                         GlobalNode.get(method),
                         /* methodCalls */Collections.EMPTY_SET,
                         /* callToRVN */Collections.EMPTY_MAP,
                         /* callToTEN */Collections.EMPTY_MAP,
                         /* castMap */Collections.EMPTY_MAP,
                         /* castPredecessors */Collections.EMPTY_SET,
                         /* returned */Collections.singleton(params[0]),
                         /* thrown */Collections.EMPTY_SET,
                         /* passedAsParameters */Collections.EMPTY_SET,
                         /* sync_ops */Collections.EMPTY_MAP);
    }

    /**
     * fake a method summary that simulates the effect of the inherited default clone().
     */
    public static String fakeCloneName = "fake$clone";
    public static MethodSummary fakeCloneMethodSummary(jq_FakeInstanceMethod method) {
        jq_Class clazz = method.getDeclaringClass();
        ParamNode []params = new ParamNode[] { FakeParamNode.getFake(method, 0, clazz) };
        ConcreteTypeNode clone = ConcreteTypeNode.get(clazz, new FakeProgramLocation(method, "fakedclone"));

        clazz.prepare();
        jq_InstanceField [] f = clazz.getInstanceFields();
        for (int i = 0; i < f.length; i++) {
            if (f[i].getType().isReferenceType())
                clone.addEdge(f[i], FieldNode.get(params[0], f[i], new FakeProgramLocation(method, "field="+f[i].getName())));
        }

        return new MethodSummary((BuildMethodSummary)null,
                         method,
                         params, 
                         GlobalNode.get(method),
                         /* methodCalls */Collections.EMPTY_SET,
                         /* callToRVN */Collections.EMPTY_MAP,
                         /* callToTEN */Collections.EMPTY_MAP,
                         /* castMap */Collections.EMPTY_MAP,
                         /* castPredecessors */Collections.EMPTY_SET,
                         /* returned */Collections.singleton(clone),
                         /* thrown */Collections.EMPTY_SET,
                         /* passedAsParameters */Collections.EMPTY_SET,
                         /* sync_ops */Collections.EMPTY_MAP);
    }
    
    public static class OperandToNodeMap {

        MultiMap operandToNode;
        
        public static void write(Textualizer t) throws IOException {
            for (Iterator i = MethodSummary.summary_cache.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                jq_Method m = (jq_Method) e.getKey();
                if (m.getBytecode() == null) continue;
                MethodSummary s = (MethodSummary) e.getValue();
                ControlFlowGraph cfg = CodeCache.getCode(m);
                m.write(t);
                for (Iterator j = cfg.reversePostOrderIterator(); j.hasNext(); ) {
                    BasicBlock bb = (BasicBlock) j.next();
                    s.builder.bb = bb;
                    State state = s.builder.start_states[bb.getID()];
                    s.builder.s = state.copy();
                    for (Iterator k = bb.iterator(); k.hasNext(); ) {
                        Quad q = (Quad) k.next();
                        t.writeString("quad "+q.getID()+" ");
                        int num = 0;
                        for (Iterator l = q.getUsedRegisters().iterator(); l.hasNext(); ) {
                            RegisterOperand op = (RegisterOperand) l.next();
                            t.writeString("op ");
                            Register r = ((RegisterOperand) op).getRegister();
                            Object o = s.builder.getRegister(r);
                            Set set;
                            if (o instanceof Set) {
                                set = (Set) o;
                            } else {
                                set = Collections.singleton(o);
                            }
                            for (Iterator n = set.iterator(); n.hasNext(); ) {
                                
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static boolean isNullConstant(Node node) {
        if (node instanceof ConcreteTypeNode || node instanceof ConcreteObjectNode) {
            jq_Reference type = node.getDeclaredType();
            if (type == null || type == jq_NullType.NULL_TYPE) {
                return true;
            }
        }
        return false;
    }
}
