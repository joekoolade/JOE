// LiveRefAnalysis.java, created Fri Jan 11 16:49:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.Set;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.Runtime.TypeCheck;
import jwutil.collections.LinearSet;
import jwutil.math.BitString;
import jwutil.math.BitString.BitStringIterator;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * A combination liveness and type analysis for Java bytecode.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: LiveRefAnalysis.java,v 1.28 2005/05/28 11:14:27 joewhaley Exp $
 */
public class LiveRefAnalysis {

    private jq_Method method;
    private ExactState[] start_states;
    private ExactState[] end_states;
    
    /** Creates new LiveRefAnalysis */
    public LiveRefAnalysis(jq_Method method) {
        this.method = method;
    }

    public ExactState getState(BasicBlock bb) {
        return start_states[bb.id];
    }
    
    public static final byte NOT_LIVE = 0;
    public static final byte LIVE_INT = 1;
    public static final byte LIVE_FLOAT = 2;
    public static final byte LIVE_LONG1 = 3;
    public static final byte LIVE_LONG2 = 4;
    public static final byte LIVE_DOUBLE1 = 5;
    public static final byte LIVE_DOUBLE2 = 6;
    public static final byte LIVE_REF = 7;
    public static final byte LIVE_DERIVED_REF = 8;
    public static final byte LIVE_RETADDR = 9;
    public static final String[] TYPE_NAMES = {
        "NOT_LIVE", "LIVE_INT", "LIVE_FLOAT", "LIVE_LONG1", "LIVE_LONG2",
        "LIVE_DOUBLE1", "LIVE_DOUBLE2", "LIVE_REF", "LIVE_DERIVED_REF", "LIVE_RETADDR" };
    public static final byte SET_TO_INT = 33;        // for JSR subroutines
    public static final byte SET_TO_FLOAT = 34;
    public static final byte SET_TO_LONG1 = 35;
    public static final byte SET_TO_LONG2 = 36;
    public static final byte SET_TO_DOUBLE1 = 37;
    public static final byte SET_TO_DOUBLE2 = 38;
    public static final byte SET_TO_REF = 39;
    public static final byte SET_TO_DERIVED_REF = 40;
    public static final byte SET_TO_RETADDR = 41;
    
    public void compute() {
        ControlFlowGraph bc_cfg = ControlFlowGraph.computeCFG(method);
        compute(bc_cfg);
    }
    
    public void compute(ControlFlowGraph bc_cfg) {
        
        // Pass 1 (forward): Compute bytecode boundaries and initial sets, and identify
        // bytecodes that manipulate heap addresses.
        FirstPassVisitor fpv = new FirstPassVisitor(method, bc_cfg);
        // traverse reverse post-order over basic blocks
        this.start_states = new ExactState[bc_cfg.getNumberOfBasicBlocks()];
        this.end_states = new ExactState[bc_cfg.getNumberOfBasicBlocks()];
        this.start_states[2] = ExactState.allocateInitialState(method);
        for (;;) {
            fpv.go_again = false;
            joeq.Compiler.BytecodeAnalysis.ControlFlowGraph.RPOBasicBlockIterator rpo = bc_cfg.reversePostOrderIterator();
            BasicBlock first_bb = rpo.nextBB();
            Assert._assert(first_bb == bc_cfg.getEntry());
            // initialize start states
            while (rpo.hasNext()) {
                if (ALWAYS_TRACE)
                    System.out.println("Iteration : "+rpo.toString());
                BasicBlock bc_bb = rpo.nextBB();
                if (ALWAYS_TRACE)
                    System.out.println(bc_bb+" : start state "+this.start_states[bc_bb.id]);
                fpv.traverseBB(bc_bb);
                if (ALWAYS_TRACE)
                    System.out.println(bc_bb+" : end state "+this.end_states[bc_bb.id]);
            }
            if (!fpv.go_again) break;
        }
        
        // Pass 1.5: Search for blocks that are unreachable by a backward traversal
        // (caused by infinite loops) and add fake backedges to make them reachable.
        
        // Pass 2 (backward): Perform live variable analysis
        SecondPassVisitor spv = new SecondPassVisitor(method, fpv.getBytecodeStart());
        // traverse post-order over basic blocks
        for (;;) {
            spv.go_again = false;
            joeq.Compiler.BytecodeAnalysis.ControlFlowGraph.RPOBasicBlockIterator rpo = bc_cfg.reversePostOrderIterator();
            rpo.jumpToEnd();
            while (rpo.hasPrevious()) {
                if (ALWAYS_TRACE)
                    System.out.println("Iteration : "+rpo.toString());
                BasicBlock bc_bb = rpo.previousBB();
                if (ALWAYS_TRACE)
                    System.out.println(bc_bb+" : end state "+this.end_states[bc_bb.id]);
                spv.traverseBB(bc_bb);
                if (ALWAYS_TRACE)
                    System.out.println(bc_bb+" : start state "+this.start_states[bc_bb.id]);
            }
            if (!spv.go_again) break;
        }
    }

    public void dump() {
        for(int i=0; i<start_states.length; ++i) {
            if (start_states[i] != null) {
                System.out.println("BB"+i+" start: "+start_states[i].toString());
            }
            if (end_states[i] != null) {
                System.out.println("BB"+i+" end: "+end_states[i].toString());
            }
        }
        System.out.println();
    }
    
    /*
    public static class State {
        private int stackDepth;
        private byte[] stack; private byte[] locals;
        public static State allocateEmptyState(jq_Method m) {
            return new State(m.getMaxStack(), m.getMaxLocals());
        }
        public static State allocateInitialState(jq_Method m) {
            State s = new State(m.getMaxStack(), m.getMaxLocals());
            jq_Type[] paramTypes = m.getParamTypes();
            for (int i=0,j=0; i<paramTypes.length; ++i, ++j) {
                if (paramTypes[i].isReferenceType()) {
                    s.locals[j] = LIVE_REF;
                } else if (paramTypes[i].isIntLike()) {
                    s.locals[j] = LIVE_INT;
                } else if (paramTypes[i] == jq_Primitive.FLOAT) {
                    s.locals[j] = LIVE_FLOAT;
                } else if (paramTypes[i] == jq_Primitive.LONG) {
                    s.locals[j] = LIVE_LONG1;
                    s.locals[++j] = LIVE_LONG2;
                } else if (paramTypes[i] == jq_Primitive.DOUBLE) {
                    s.locals[j] = LIVE_DOUBLE1;
                    s.locals[++j] = LIVE_DOUBLE2;
                } else {
                    jq.UNREACHABLE();
                }
            }
            return s;
        }
        State(int stacksize, int localsize) {
            stack = new byte[stacksize]; locals = new byte[localsize];
            stackDepth = 0;
        }
        public State copy() {
            State that = new State(this.stack.length, this.locals.length);
            System.arraycopy(this.stack, 0, that.stack, 0, this.stackDepth);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            that.stackDepth = this.stackDepth;
            return that;
        }
        public boolean merge(State that) {
            jq.Assert(this.stackDepth == that.stackDepth);
            boolean change = false;
            for (int i=0; i<this.stackDepth; ++i) {
                if (this.stack[i] != that.stack[i]) { this.stack[i] = NOT_LIVE; change = true; }
            }
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] != that.locals[i]) { this.locals[i] = NOT_LIVE; change = true; }
            }
            return change;
        }
        public void overwriteWith(State that) {
            jq.Assert(this.stack.length == that.stack.length);
            jq.Assert(this.locals.length == that.locals.length);
            System.arraycopy(that.stack, 0, this.stack, 0, that.stackDepth);
            System.arraycopy(that.locals, 0, this.locals, 0, that.locals.length);
            this.stackDepth = that.stackDepth;
        }
        void push_I() { stack[stackDepth++] = LIVE_INT; }
        void push_F() { stack[stackDepth++] = LIVE_FLOAT; }
        void push_L() { stack[stackDepth++] = LIVE_LONG1; stack[stackDepth++] = LIVE_LONG2; }
        void push_D() { stack[stackDepth++] = LIVE_DOUBLE1; stack[stackDepth++] = LIVE_DOUBLE2; }
        void push_A() { stack[stackDepth++] = LIVE_REF; }
        void push_R() { stack[stackDepth++] = LIVE_DERIVED_REF; }
        void push_RetAddr() { stack[stackDepth++] = LIVE_RETADDR; }
        void pop_I() { --stackDepth; jq.Assert(stack[stackDepth] == LIVE_INT); }
        void pop_F() { --stackDepth; jq.Assert(stack[stackDepth] == LIVE_FLOAT); }
        void pop_L() { stackDepth-=2; jq.Assert(stack[stackDepth] == LIVE_LONG1); jq.Assert(stack[stackDepth+1] == LIVE_LONG2); }
        void pop_D() { stackDepth-=2; jq.Assert(stack[stackDepth] == LIVE_DOUBLE1); jq.Assert(stack[stackDepth+1] == LIVE_DOUBLE2); }
        void pop_A() { --stackDepth; jq.Assert(stack[stackDepth] == LIVE_REF); }
        void pop_R() { --stackDepth; jq.Assert(stack[stackDepth] == LIVE_DERIVED_REF); }
        byte pop() { return stack[--stackDepth]; }
        void push(byte t) { stack[stackDepth++] = t; }
        void pop(jq_Type t) {
            if (t.isReferenceType()) pop_A();
            else if (t.isIntLike()) {
                byte t2 = pop();
                if (t2 != LIVE_DERIVED_REF)
                    System.err.println("WARNING: method takes derived ref as an argument");
            }
            else if (t == jq_Primitive.FLOAT) pop_F();
            else if (t == jq_Primitive.LONG) pop_L();
            else if (t == jq_Primitive.DOUBLE) pop_D();
            else jq.UNREACHABLE();
        }
        public int getStackDepth() { return stackDepth; }
        public byte getStack(int i) { return stack[i]; }

        //static boolean isLiveIntType(byte type) {
        //    return type == LIVE_INT || type == LIVE_DERIVED_REF;
        //}
        
        void setLocal_I(int i) { locals[i] = LIVE_INT; }
        void setLocal_F(int i) { locals[i] = LIVE_FLOAT; }
        void setLocal_L(int i) { locals[i] = LIVE_LONG1; locals[i+1] = LIVE_LONG2; }
        void setLocal_D(int i) { locals[i] = LIVE_DOUBLE1; locals[i+1] = LIVE_DOUBLE2; }
        void setLocal_A(int i) { locals[i] = LIVE_REF; }
        void setLocal_RetAddr(int i) { locals[i] = LIVE_RETADDR; }
        void setLocal(int i, byte t) { locals[i] = t; }
        public byte getLocal(int i) { return locals[i]; }
        public int getNumberOfLocals() { return locals.length; }
        
        public String toString() {
            StringBuffer sb = new StringBuffer("Locals: { ");
            for (int i=0; i<locals.length; ++i) {
                if (locals[i] == NOT_LIVE) continue;
                sb.append(i);
                sb.append('=');
                sb.append(TYPE_NAMES[locals[i]]);
                if (i < locals.length-1) sb.append(',');
            }
            sb.append(" }");
            if (stackDepth > 0) {
                sb.append(Strings.lineSep+"Stack: {");
                for (int i=0; i<stackDepth; ++i) {
                    sb.append(i);
                    sb.append('=');
                    sb.append(stack[i]==null?"null":stack[i].toString());
                    if (i < stackDepth-1) sb.append(',');
                }
                sb.append(" }");
            }
            return sb.toString();
        }
    }
    */

    public abstract static class Type {
        public abstract Type findCommonSuperclass(Type that);
        public abstract boolean isReferenceType();
        public Type getElementType() { return NullConstant.INSTANCE; }
    }
    
    public static class SystemType extends Type {
        private final jq_Type type;
        SystemType(jq_Type jq_t) {
            if (jq_t.isIntLike()) jq_t = jq_Primitive.INT;
            this.type = jq_t;
        }
        public jq_Type getType() { return type; }
        public Type findCommonSuperclass(Type that) {
            if (that instanceof SystemType) {
                SystemType t = (SystemType)that;
                jq_Type jq_t = TypeCheck.findCommonSuperclass(this.type, t.type, true);
                if (jq_t == null) return null;
                if (jq_t == this.type) return this;
                if (jq_t == t.type) return t;
                if (ALWAYS_TRACE) System.out.println("Superclass of "+this+" and "+that+" is "+jq_t);
                return new SystemType(jq_t);
            }
            if (that instanceof NullConstant) {
                if (this.type.isReferenceType()) return this;
            }
            return null;
        }
        public Type getElementType() {
            jq_Array a = (jq_Array)type;
            if (a.getElementType().isAddressType()) return DerivedRef.INSTANCE;
            return new SystemType(a.getElementType());
        }
        public boolean equals(SystemType that) {
            return this.type == that.type;
        }
        public boolean equals(Object that) {
            return equals((SystemType)that);
        }
        public int hashCode() { return type.hashCode(); }
        public boolean isReferenceType() { return type.isReferenceType(); }
        public String toString() { return type.toString(); }
        public static final SystemType INT    = new SystemType(jq_Primitive.INT);
        public static final SystemType FLOAT  = new SystemType(jq_Primitive.FLOAT);
        public static final SystemType LONG   = new SystemType(jq_Primitive.LONG);
        public static final SystemType DOUBLE = new SystemType(jq_Primitive.DOUBLE);
        public static final SystemType OBJECT = new SystemType(PrimordialClassLoader.getJavaLangObject());
    }
    
    public static class DerivedRef extends Type {
        public Type findCommonSuperclass(Type that) {
            if (that instanceof DerivedRef) return this;
            return null;
        }
        public boolean isReferenceType() { return false; }
        public String toString() { return "DerivedRef"; }
        public static final DerivedRef INSTANCE = new DerivedRef();
    }
    
    public static class NullConstant extends Type {
        
        public Type findCommonSuperclass(Type that) {
            if (that.isReferenceType()) return that;
            return null;
        }
        public boolean isReferenceType() { return true; }
        public String toString() { return "NULL"; }
        public static final NullConstant INSTANCE = new NullConstant();
    }

    public static class HalfOfNumber extends Type {
        public Type findCommonSuperclass(Type that) {
            if (that instanceof HalfOfNumber) return that;
            return null;
        }
        public boolean isReferenceType() { return false; }
        public String toString() { return "HALF"; }
        public static final HalfOfNumber INSTANCE = new HalfOfNumber();
    }
    
    public static class Retaddr extends Type {
        int location;
        Retaddr(int l) { this.location = l; }
        public Type findCommonSuperclass(Type that) {
            if (that instanceof Retaddr) {
                if (this.equals((Retaddr)that)) return this;
            }
            return null;
        }
        public boolean equals(Retaddr that) {
            return this.location == that.location;
        }
        public boolean equals(Object that) {
            return equals((Retaddr)that);
        }
        public int hashCode() { return location; }
        public boolean isReferenceType() { return false; }
        public String toString() { return "RetAddr:"+location; }
    }

    public static class ExactJSRState extends ExactState {
        protected boolean[] mayChangeLocals;
        protected boolean[] mustChangeLocals;
        ExactJSRState(int stacksize, int localsize) {
            super(stacksize, localsize);
            mayChangeLocals = new boolean[localsize];
            mustChangeLocals = new boolean[localsize];
        }
        public ExactState copy() {
            ExactJSRState that = new ExactJSRState(this.stack.length, this.locals.length);
            System.arraycopy(this.stack, 0, that.stack, 0, this.stackDepth);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            System.arraycopy(this.mayChangeLocals, 0, that.mayChangeLocals, 0, this.mayChangeLocals.length);
            System.arraycopy(this.mustChangeLocals, 0, that.mustChangeLocals, 0, this.mustChangeLocals.length);
            that.stackDepth = this.stackDepth;
            return that;
        }
        public ExactJSRState copyAsJSR() {
            // nested jsr's!
            if (ALWAYS_TRACE) System.out.println("nested jsr's! adding nesting level");
            // we need a fresh may/mustChangeLocals array for the nested jsr
            return super.copyAsJSR();
        }
        public ExactState copyJSR(ExactJSRState jsr_state) {
            // nested jsr's!
            if (ALWAYS_TRACE) System.out.println("nested jsr's! removing nesting level");
            ExactJSRState that = new ExactJSRState(this.stack.length, this.locals.length);
            System.arraycopy(jsr_state.stack, 0, that.stack, 0, jsr_state.stackDepth);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            that.stackDepth = jsr_state.stackDepth;
            for (int i=0; i<this.locals.length; ++i) {
                if (jsr_state.mayChangeLocals[i]) {
                    if (ALWAYS_TRACE) System.out.println("nested jsr may change local "+i);
                    this.locals[i] = jsr_state.locals[i];
                    this.mayChangeLocals[i] = true;
                    if (jsr_state.mustChangeLocals[i]) {
                        this.mustChangeLocals[i] = true;
                    }
                }
            }
            return that;
        }
        public ExactState copyHandler(jq_Type t) {
            ExactJSRState that = new ExactJSRState(this.stack.length, this.locals.length);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            System.arraycopy(this.mayChangeLocals, 0, that.mayChangeLocals, 0, this.mayChangeLocals.length);
            System.arraycopy(this.mustChangeLocals, 0, that.mustChangeLocals, 0, this.mustChangeLocals.length);
            that.stackDepth = 1; that.stack[0] = new SystemType(t);
            return that;
        }
        public boolean mergeBeforeJSR(ExactState that) {
            // don't merge changedLocals from 'that'
            return super.merge(that);
        }
        public boolean merge(ExactState that) {
            Assert._assert(this.stackDepth == that.stackDepth);
            boolean change = false;
            for (int i=0; i<this.stackDepth; ++i) {
                if (this.stack[i] == null) continue;
                if (that.stack[i] == null) { this.stack[i] = null; change = true; continue; }
                Type t = this.stack[i].findCommonSuperclass(that.stack[i]);
                if (t != this.stack[i]) change = true;
                this.stack[i] = t;
            }
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] == null) continue;
                if (that.locals[i] == null) { this.locals[i] = null; change = true; continue; }
                Type t = this.locals[i].findCommonSuperclass(that.locals[i]);
                if (t != this.locals[i]) change = true;
                this.locals[i] = t;
            }
            if (that instanceof ExactJSRState) {
                ExactJSRState that2 = (ExactJSRState)that;
                for (int i=0; i<this.mayChangeLocals.length; ++i) {
                    if (that2.mayChangeLocals[i]) {
                        if (!this.mayChangeLocals[i]) {
                            if (ALWAYS_TRACE) System.out.println("updated: may change local "+i+" during merge");
                            this.mayChangeLocals[i] = true;
                            change = true;
                        }
                    }
                    if (!that2.mustChangeLocals[i]) {
                        if (this.mustChangeLocals[i]) {
                            this.mustChangeLocals[i] = false;
                            change = true;
                        }
                    }
                }
            }
            return change;
        }
        public boolean mergeWithHandler(ExactState that) {
            Assert._assert(this.stackDepth == 1);
            boolean change = false;
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] == null) continue;
                if (that.locals[i] == null) { this.locals[i] = null; change = true; continue; }
                Type t = this.locals[i].findCommonSuperclass(that.locals[i]);
                if (t != this.locals[i]) change = true;
                this.locals[i] = t;
            }
            if (that instanceof ExactJSRState) {
                ExactJSRState that2 = (ExactJSRState)that;
                for (int i=0; i<this.mayChangeLocals.length; ++i) {
                    if (that2.mayChangeLocals[i]) {
                        if (!this.mayChangeLocals[i]) {
                            if (ALWAYS_TRACE) System.out.println("updated: may change local "+i+" during exception handler merge");
                            this.mayChangeLocals[i] = true;
                            change = true;
                        }
                    }
                    if (!that2.mustChangeLocals[i]) {
                        if (this.mustChangeLocals[i]) {
                            this.mustChangeLocals[i] = false;
                            change = true;
                        }
                    }
                }
            }
            return change;
        }
        
        void setLocal_I(int i) { super.setLocal_I(i); this.mayChangeLocals[i] = this.mustChangeLocals[i] = true; }
        void setLocal_F(int i) { super.setLocal_F(i); this.mayChangeLocals[i] = this.mustChangeLocals[i] = true; }
        void setLocal_L(int i) { super.setLocal_L(i); this.mayChangeLocals[i] = this.mustChangeLocals[i] = true; this.mayChangeLocals[i+1] = this.mustChangeLocals[i+1] = true; }
        void setLocal_D(int i) { super.setLocal_D(i); this.mayChangeLocals[i] = this.mustChangeLocals[i] = true; this.mayChangeLocals[i+1] = this.mustChangeLocals[i+1] = true; }
        void setLocal(int i, Type t) { super.setLocal(i, t); this.mayChangeLocals[i] = this.mustChangeLocals[i] = true; }
        
        public String toString_live() {
            StringBuffer sb = new StringBuffer("Live Locals: { ");
            for (int i=0; i<locals.length; ++i) {
                if (getLocal(i) == null) continue;
                sb.append(i);
                sb.append('=');
                sb.append(locals[i].toString());
                if (mayChangeLocals[i]) sb.append('*');
                if (mustChangeLocals[i]) sb.append('&');
                if (i < locals.length-1) sb.append(',');
            }
            sb.append(" }");
            if (stackDepth > 0) {
                sb.append(Strings.lineSep);
                sb.append("Stack: {");
                for (int i=0; i<stackDepth; ++i) {
                    sb.append(i);
                    sb.append('=');
                    sb.append(stack[i]==null?"null":stack[i].toString());
                    if (i < stackDepth-1) sb.append(',');
                }
                sb.append(" }");
            }
            return sb.toString();
        }
        public String toString() {
            if (liveness != null) return toString_live();
            StringBuffer sb = new StringBuffer("Locals: { ");
            for (int i=0; i<locals.length; ++i) {
                if (locals[i] == null) continue;
                sb.append(i);
                sb.append('=');
                sb.append(locals[i].toString());
                if (mayChangeLocals[i]) sb.append('*');
                if (mustChangeLocals[i]) sb.append('&');
                if (i < locals.length-1) sb.append(',');
            }
            sb.append(" }");
            if (stackDepth > 0) {
                sb.append(Strings.lineSep);
                sb.append("Stack: {");
                for (int i=0; i<stackDepth; ++i) {
                    sb.append(i);
                    sb.append('=');
                    sb.append(stack[i]==null?"null":stack[i].toString());
                    if (i < stackDepth-1) sb.append(',');
                }
                sb.append(" }");
            }
            return sb.toString();
        }
    }
    
    public static class ExactState {
        protected int stackDepth;
        protected Type[] stack; protected Type[] locals;
        protected boolean[] liveness;
        protected Set last_uses;
        public static ExactState allocateEmptyState(jq_Method m) {
            return new ExactState(m.getMaxStack(), m.getMaxLocals());
        }
        public static ExactState allocateInitialState(jq_Method m) {
            ExactState s = new ExactState(m.getMaxStack(), m.getMaxLocals());
            jq_Type[] paramTypes = m.getParamTypes();
            for (int i=0, j=0; i<paramTypes.length; ++i, ++j) {
                s.locals[j] = new SystemType(paramTypes[i]);
                if (paramTypes[i].getReferenceSize() == 8) {
                    s.locals[++j] = HalfOfNumber.INSTANCE;
                }
            }
            return s;
        }
        ExactState(int stacksize, int localsize) {
            stack = new Type[stacksize]; locals = new Type[localsize];
            stackDepth = 0;
        }
        public void allocateLiveness() {
            if (liveness == null)
                liveness = new boolean[locals.length];
        }
        public void initializeLastUses() {
            last_uses = new LinearSet();
        }
        public boolean compareLiveness(ExactState that) {
            if (that.liveness == null) return true;
            for (int i=0; i<this.liveness.length; ++i) {
                if (this.liveness[i] != that.liveness[i]) return true;
            }
            return false;
        }
        public ExactState copy() {
            ExactState that = new ExactState(this.stack.length, this.locals.length);
            System.arraycopy(this.stack, 0, that.stack, 0, this.stackDepth);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            if (this.liveness != null) {
                that.liveness = new boolean[this.liveness.length];
                System.arraycopy(this.liveness, 0, that.liveness, 0, this.liveness.length);
            }
            that.stackDepth = this.stackDepth;
            return that;
        }
        public ExactJSRState copyAsJSR() {
            ExactJSRState that = new ExactJSRState(this.stack.length, this.locals.length);
            System.arraycopy(this.stack, 0, that.stack, 0, this.stackDepth);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            that.stackDepth = this.stackDepth;
            return that;
        }
        public ExactState copyJSR(ExactJSRState jsr_state) {
            ExactState that = new ExactState(this.stack.length, this.locals.length);
            System.arraycopy(jsr_state.stack, 0, that.stack, 0, jsr_state.stackDepth);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            that.stackDepth = jsr_state.stackDepth;
            for (int i=0; i<this.locals.length; ++i) {
                if (jsr_state.mayChangeLocals[i]) {
                    this.locals[i] = jsr_state.locals[i];
                }
            }
            return that;
        }
        public ExactState copyHandler(jq_Type t) {
            ExactState that = new ExactState(this.stack.length, this.locals.length);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            that.stackDepth = 1; that.stack[0] = new SystemType(t);
            return that;
        }
        public boolean mergeLiveness(ExactState that) {
            if (that.liveness == null) return false;
            if (this.liveness == null) {
                this.liveness = new boolean[this.locals.length];
                System.arraycopy(that.liveness, 0, this.liveness, 0, this.liveness.length);
                return true;
            }
            boolean change = false;
            for (int i=0; i<this.liveness.length; ++i) {
                if (this.liveness[i]) continue;
                if (that.liveness[i]) {
                    this.liveness[i] = true;
                    change = true;
                    continue;
                }
            }
            return change;
        }
        // Conservative approximation!  we union the two live sets
        // this == at JSR call, that == after JSR call
        /*
        public boolean mergeLivenessJSR(ExactState that, ExactJSRState jsr_start) {
            if (that.liveness == null) return false;
            if (jsr_state.liveness == null) return false;
            if (this.liveness == null) {
                this.liveness = new boolean[this.locals.length];
            }
            boolean change = false;
            for (int i=0; i<this.liveness.length; ++i) {
                if (this.liveness[i]) continue;
                if (jsr_start.liveness[i] || that.liveness[i]) {
                    this.liveness[i] = true;
                    change = true;
                    continue;
                }
            }
            return change;
        }
        */

        public boolean merge(ExactState that) {
            Assert._assert(this.stackDepth == that.stackDepth);
            boolean change = false;
            for (int i=0; i<this.stackDepth; ++i) {
                if (this.stack[i] == null) continue;
                if (that.stack[i] == null) { this.stack[i] = null; change = true; continue; }
                Type t = this.stack[i].findCommonSuperclass(that.stack[i]);
                if (t != this.stack[i]) change = true;
                this.stack[i] = t;
            }
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] == null) {
                    continue;
                }
                if (that.locals[i] == null) {
                    this.locals[i] = null;
                    change = true;
                    continue;
                }
                Type t = this.locals[i].findCommonSuperclass(that.locals[i]);
                if (t != this.locals[i]) change = true;
                this.locals[i] = t;
            }
            return change;
        }
        // this == after JSR call, that == before JSR call, jsr_state == end of JSR body
        public boolean mergeJSR(ExactState that, ExactJSRState jsr_state) {
            Assert._assert(this.stackDepth == jsr_state.stackDepth);
            boolean change = false;
            for (int i=0; i<this.stackDepth; ++i) {
                if (this.stack[i] == null) continue;
                if (jsr_state.stack[i] == null) { this.stack[i] = null; change = true; continue; }
                Type t = this.stack[i].findCommonSuperclass(jsr_state.stack[i]);
                if (t != this.stack[i]) change = true;
                this.stack[i] = t;
            }
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] == null) {
                    continue;
                }
                Type that_type;
                if (jsr_state.mayChangeLocals[i]) {
                    that_type = jsr_state.locals[i];
                } else {
                    that_type = that.locals[i];
                }
                if (that_type == null) {
                    this.locals[i] = null;
                    change = true;
                    continue;
                }
                Type t = this.locals[i].findCommonSuperclass(that_type);
                if (t != this.locals[i]) change = true;
                this.locals[i] = t;
            }
            return change;
        }
        public boolean mergeWithHandler(ExactState that) {
            Assert._assert(this.stackDepth == 1);
            boolean change = false;
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] == null) {
                    continue;
                }
                if (that.locals[i] == null) {
                    this.locals[i] = null;
                    change = true;
                    continue;
                }
                Type t = this.locals[i].findCommonSuperclass(that.locals[i]);
                if (t != this.locals[i]) change = true;
                this.locals[i] = t;
            }
            return change;
        }

        void push_I() { stack[stackDepth++] = SystemType.INT; }
        void push_F() { stack[stackDepth++] = SystemType.FLOAT; }
        void push_L() { stack[stackDepth++] = SystemType.LONG; stack[stackDepth++] = HalfOfNumber.INSTANCE; }
        void push_D() { stack[stackDepth++] = SystemType.DOUBLE; stack[stackDepth++] = HalfOfNumber.INSTANCE; }
        void push_R() { stack[stackDepth++] = DerivedRef.INSTANCE; }
        void push_RetAddr(int target) { stack[stackDepth++] = new Retaddr(target); }
        void pop_I() { --stackDepth; Assert._assert(stack[stackDepth].equals(SystemType.INT)); }
        void pop_F() { --stackDepth; Assert._assert(stack[stackDepth].equals(SystemType.FLOAT)); }
        void pop_L() { stackDepth-=2; Assert._assert(stack[stackDepth].equals(SystemType.LONG)); Assert._assert(stack[stackDepth+1] == HalfOfNumber.INSTANCE); }
        void pop_D() { stackDepth-=2; Assert._assert(stack[stackDepth].equals(SystemType.DOUBLE)); Assert._assert(stack[stackDepth+1] == HalfOfNumber.INSTANCE); }
        void pop_A() { --stackDepth; Assert._assert(stack[stackDepth].isReferenceType()); }
        void pop_R() { --stackDepth; Assert._assert(stack[stackDepth] == DerivedRef.INSTANCE); }
        Type pop() { return stack[--stackDepth]; }
        void push(Type t) {
            stack[stackDepth++] = t;
            //if (t.equals(SystemType.LONG) || t.equals(SystemType.DOUBLE)) stack[stackDepth++] = HalfOfNumber.INSTANCE;
        }
        void pop(jq_Type t) {
            if (t.isAddressType()) pop_R();
            else if (t.isReferenceType()) pop_A();
            else if (t.isIntLike()) pop_I();
            else if (t == jq_Primitive.FLOAT) pop_F();
            else if (t == jq_Primitive.LONG) pop_L();
            else if (t == jq_Primitive.DOUBLE) pop_D();
            else Assert.UNREACHABLE();
        }
        public int getStackDepth() { return stackDepth; }
        public Type getStack(int i) { return stack[i]; }

        //static boolean isLiveIntType(byte type) {
        //    return type == LIVE_INT || type == LIVE_DERIVED_REF;
        //}
        
        void setLocal_I(int i) { locals[i] = SystemType.INT; }
        void setLocal_F(int i) { locals[i] = SystemType.FLOAT; }
        void setLocal_L(int i) { locals[i] = SystemType.LONG; locals[i+1] = HalfOfNumber.INSTANCE; }
        void setLocal_D(int i) { locals[i] = SystemType.DOUBLE; locals[i+1] = HalfOfNumber.INSTANCE; }
        void setLocal_R(int i) { locals[i] = DerivedRef.INSTANCE; }
        void setLocal(int i, Type t) { locals[i] = t; }
        public Type getLocal(int i) { return locals[i]; }

        public Type getLiveLocal(int i) {
            if (liveness[i]) return locals[i];
            else return null;
        }
        void liveLocal_I(int bci, int i) {
            checkLastUse(bci, i); liveness[i] = true;
        }
        void liveLocal_F(int bci, int i) {
            checkLastUse(bci, i); liveness[i] = true;
        }
        void liveLocal_L(int bci, int i) {
            checkLastUse(bci, i); liveness[i] = true;
            checkLastUse(bci, i+1); liveness[i+1] = true;
        }
        void liveLocal_D(int bci, int i) {
            checkLastUse(bci, i); liveness[i] = true;
            checkLastUse(bci, i+1); liveness[i+1] = true;
        }
        void liveLocal_A(int bci, int i) {
            checkLastUse(bci, i); liveness[i] = true;
        }
        void deadLocal_I(int i) { liveness[i] = false; }
        void deadLocal_F(int i) { liveness[i] = false; }
        void deadLocal_L(int i) { liveness[i] = false; liveness[i+1] = false; }
        void deadLocal_D(int i) { liveness[i] = false; liveness[i+1] = false; }
        void deadLocal_A(int i) { liveness[i] = false; }

        void checkLastUse(int bci, int i) {
            if (liveness[i] == false) {
                if (ALWAYS_TRACE)
                    System.out.println(bci+": Last use of local "+i);
                last_uses.add(new LastUse(bci, i));
            }
        }

        static class LastUse {
            int bci, i;
            LastUse(int bci, int i) { this.bci = bci; this.i = i; }
        }

        public int getNumberOfLocals() { return locals.length; }
        
        public String toString_live() {
            StringBuffer sb = new StringBuffer("Live Locals: { ");
            for (int i=0; i<locals.length; ++i) {
                if (getLiveLocal(i) == null) continue;
                sb.append(i);
                sb.append('=');
                sb.append(getLiveLocal(i).toString());
                if (i < locals.length-1) sb.append(',');
            }
            sb.append(" }");
            if (stackDepth > 0) {
                sb.append(Strings.lineSep);
                sb.append("Stack: {");
                for (int i=0; i<stackDepth; ++i) {
                    sb.append(i);
                    sb.append('=');
                    sb.append(stack[i]==null?"null":stack[i].toString());
                    if (i < stackDepth-1) sb.append(',');
                }
                sb.append(" }");
            }
            return sb.toString();
        }
        public String toString() {
            if (liveness != null) return toString_live();
            StringBuffer sb = new StringBuffer("Locals: { ");
            for (int i=0; i<locals.length; ++i) {
                if (getLocal(i) == null) continue;
                sb.append(i);
                sb.append('=');
                sb.append(getLocal(i).toString());
                if (i < locals.length-1) sb.append(',');
            }
            sb.append(" }");
            if (stackDepth > 0) {
                sb.append(Strings.lineSep);
                sb.append("Stack: {");
                for (int i=0; i<stackDepth; ++i) {
                    sb.append(i);
                    sb.append('=');
                    sb.append(stack[i]==null?"null":stack[i].toString());
                    if (i < stackDepth-1) sb.append(',');
                }
                sb.append(" }");
            }
            return sb.toString();
        }
    }
    
    public static boolean ALWAYS_TRACE = false;
    
    public class FirstPassVisitor extends BytecodeVisitor {
        private BitString bytecode_start;
        private ExactState current_state;
        private BasicBlock current_bb;
        private ControlFlowGraph cfg;

        FirstPassVisitor(jq_Method method, ControlFlowGraph cfg) {
            super(method);
            this.bytecode_start = new BitString(bcs.length);
            this.current_state = ExactState.allocateEmptyState(method);
            this.cfg = cfg;
            this.TRACE = ALWAYS_TRACE;
        }
        
        public String toString() { return "LR1/"+this.method.getName(); }
        
        boolean isEndOfBB(BasicBlock bb) { return i_start > bb.getEnd(); }
        
        boolean go_again = false;
        boolean endsWithJSR = false, endsWithRET = false;

        public BitString getBytecodeStart() { return bytecode_start; }

        public void traverseBB(joeq.Compiler.BytecodeAnalysis.BasicBlock bb) {
            if (start_states[bb.id] == null) {
                // unreachable block!
                if (TRACE) out.println("Basic block "+bb+" is unreachable!");
                return;
            }
            if (bb.getStart() == -1) {
                return; // entry or exit
            }
            if (TRACE) out.println("Visiting "+bb);
            current_state = start_states[bb.id].copy();
            //current_state.overwriteWith(start_states[bb.id]);
            current_bb = bb;
            endsWithJSR = false; endsWithRET = false;
            for (i_end=bb.getStart()-1; ; ) {
                i_start = i_end+1;
                if (isEndOfBB(bb)) break;
                bytecode_start.set(i_start);
                try {
                    this.visitBytecode();
                } catch (RuntimeException x) {
                    System.err.println("EXCEPTION OCCURRED while analyzing "+this.method+" bc "+(int)i_start);
                    throw x;
                }
            }
            //end_states[bb.id] = copyStateInto(end_states[bb.id], current_state);
            end_states[bb.id] = current_state;
            if (endsWithRET) {
                for (int i=0; i<bb.getNumberOfSuccessors(); ++i) {
                    BasicBlock jsr_caller = cfg.getBasicBlock(bb.getSuccessor(i).id-1);
                    if (TRACE) out.println("Merging with jsr successor "+bb.getSuccessor(i)+" from jsr call at "+jsr_caller);
                    if (start_states[jsr_caller.id] != null) {
                        if (this.mergeJSRStateWith(jsr_caller, bb.getSuccessor(i))) go_again = true;
                    } else {
                        if (TRACE) out.println("jsr "+jsr_caller+" is not yet reached (or is unreachable");
                    }
                }
            } else {
                for (int i=0; i<bb.getNumberOfSuccessors(); ++i) {
                    if (bb.getSuccessor(i).id == 1) continue;
                    if (TRACE) out.println("Merging with successor "+bb.getSuccessor(i));
                    if (this.mergeStateWith(bb.getSuccessor(i), endsWithJSR)) go_again = true;
                }
            }
        }
        
        private boolean mergeWithExceptionHandlers() {
            boolean change = false;
            ExceptionHandlerIterator ehi = current_bb.getExceptionHandlers();
            while (ehi.hasNext()) {
                ExceptionHandler eh = ehi.nextEH();
                BasicBlock bb2 = eh.getEntry();
                jq_Type t = eh.getExceptionType();
                if (t == null) t = PrimordialClassLoader.getJavaLangThrowable();
                if (TRACE) out.println("Merging with handler "+bb2+" type "+t);
                if (start_states[bb2.id] == null) {
                    change = true;
                    start_states[bb2.id] = current_state.copyHandler(t);
                } else {
                    if (start_states[bb2.id].mergeWithHandler(current_state)) change = true;
                }
            }
            if (change) go_again = true;
            return change;
        }
        
        private boolean mergeStateWith(BasicBlock bb2, boolean jsr) {
            if (start_states[bb2.id] == null) {
                if (jsr)
                    start_states[bb2.id] = current_state.copyAsJSR();
                else
                    start_states[bb2.id] = current_state.copy();
                return true;
            } else {
                if (jsr) {
                    Assert._assert(start_states[bb2.id] instanceof ExactJSRState);
                    return ((ExactJSRState)start_states[bb2.id]).mergeBeforeJSR(current_state);
                } else {
                    return start_states[bb2.id].merge(current_state);
                }
            }
        }
        
        private boolean mergeJSRStateWith(BasicBlock before, BasicBlock after) {
            Assert._assert(current_state instanceof ExactJSRState);
            if (end_states[before.id] == null) {
                System.err.println(this.method+" ::: Warning! Successor of JSR block "+before+" has not yet been visited.");
                return false;
            }
            ExactJSRState jsr_state = (ExactJSRState)current_state;
            if (start_states[after.id] == null) {
                start_states[after.id] = end_states[before.id].copyJSR(jsr_state);
                return true;
            } else return start_states[after.id].mergeJSR(end_states[before.id], jsr_state);
        }
        
        /*
        private ExactState copyStateInto(ExactState s1, ExactState s2) {
            if (s1 == null) return s2.copy();
            s1.overwriteWith(s2); return s1;
        }
         */
        
        public void visitNOP() {
            super.visitNOP();
        }
        public void visitACONST(Object s) {
            super.visitACONST(s);
            current_state.push(NullConstant.INSTANCE);
        }
        public void visitICONST(int c) {
            super.visitICONST(c);
            current_state.push_I();
        }
        public void visitLCONST(long c) {
            super.visitLCONST(c);
            current_state.push_L();
        }
        public void visitFCONST(float c) {
            super.visitFCONST(c);
            current_state.push_F();
        }
        public void visitDCONST(double c) {
            super.visitDCONST(c);
            current_state.push_D();
        }
        public void visitILOAD(int i) {
            super.visitILOAD(i);
            current_state.push_I();
        }
        public void visitLLOAD(int i) {
            super.visitLLOAD(i);
            current_state.push_L();
        }
        public void visitFLOAD(int i) {
            super.visitFLOAD(i);
            current_state.push_F();
        }
        public void visitDLOAD(int i) {
            super.visitDLOAD(i);
            current_state.push_D();
        }
        public void visitALOAD(int i) {
            super.visitALOAD(i);
            current_state.push(current_state.getLocal(i));
        }
        public void visitISTORE(int i) {
            super.visitISTORE(i);
            current_state.pop_I(); current_state.setLocal_I(i);
        }
        public void visitLSTORE(int i) {
            super.visitLSTORE(i);
            current_state.pop_L(); current_state.setLocal_L(i);
        }
        public void visitFSTORE(int i) {
            super.visitFSTORE(i);
            current_state.pop_F(); current_state.setLocal_F(i);
        }
        public void visitDSTORE(int i) {
            super.visitDSTORE(i);
            current_state.pop_D(); current_state.setLocal_D(i);
        }
        public void visitASTORE(int i) {
            super.visitASTORE(i);
            current_state.setLocal(i, current_state.pop());
        }
        public void visitIALOAD() {
            super.visitIALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitLALOAD() {
            super.visitLALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_L();
            mergeWithExceptionHandlers();
        }
        public void visitFALOAD() {
            super.visitFALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_F();
            mergeWithExceptionHandlers();
        }
        public void visitDALOAD() {
            super.visitDALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_D();
            mergeWithExceptionHandlers();
        }
        public void visitAALOAD() {
            super.visitAALOAD();
            current_state.pop_I();
            Type t = current_state.pop();
            Type t2 = t.getElementType();
            current_state.push(t2);
            mergeWithExceptionHandlers();
        }
        public void visitBALOAD() {
            super.visitBALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitCALOAD() {
            super.visitCALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitSALOAD() {
            super.visitSALOAD();
            current_state.pop_I(); current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitIASTORE() {
            super.visitIASTORE();
            current_state.pop_I(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitLASTORE() {
            super.visitLASTORE();
            current_state.pop_L(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitFASTORE() {
            super.visitFASTORE();
            current_state.pop_F(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitDASTORE() {
            super.visitDASTORE();
            current_state.pop_D(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitAASTORE() {
            super.visitAASTORE();
            Type t = current_state.pop(); // may be storing derived ref
            current_state.pop_I();
            Type t2 = current_state.pop();
            if (t instanceof DerivedRef)
                Assert._assert(t2.getElementType() instanceof DerivedRef);
            mergeWithExceptionHandlers();
        }
        public void visitBASTORE() {
            super.visitBASTORE();
            current_state.pop_I(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitCASTORE() {
            super.visitCASTORE();
            current_state.pop_I(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitSASTORE() {
            super.visitSASTORE();
            current_state.pop_I(); current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitPOP() {
            super.visitPOP();
            current_state.pop();
        }
        public void visitPOP2() {
            super.visitPOP2();
            current_state.pop(); current_state.pop();
        }
        public void visitDUP() {
            super.visitDUP();
            Type t = current_state.pop(); current_state.push(t); current_state.push(t);
        }
        public void visitDUP_x1() {
            super.visitDUP_x1();
            Type t1 = current_state.pop(); Type t2 = current_state.pop();
            current_state.push(t1); current_state.push(t2); current_state.push(t1);
        }
        public void visitDUP_x2() {
            super.visitDUP_x2();
            Type t1 = current_state.pop(); Type t2 = current_state.pop(); Type t3 = current_state.pop();
            current_state.push(t1); current_state.push(t3); current_state.push(t2); current_state.push(t1);
        }
        public void visitDUP2() {
            super.visitDUP2();
            Type t1 = current_state.pop(); Type t2 = current_state.pop();
            current_state.push(t2); current_state.push(t1); current_state.push(t2); current_state.push(t1);
        }
        public void visitDUP2_x1() {
            super.visitDUP2_x1();
            Type t1 = current_state.pop(); Type t2 = current_state.pop(); Type t3 = current_state.pop();
            current_state.push(t2); current_state.push(t1); current_state.push(t3); current_state.push(t2); current_state.push(t1);
        }
        public void visitDUP2_x2() {
            super.visitDUP2_x2();
            Type t1 = current_state.pop(); Type t2 = current_state.pop(); Type t3 = current_state.pop(); Type t4 = current_state.pop();
            current_state.push(t2); current_state.push(t1); current_state.push(t4); current_state.push(t3); current_state.push(t2); current_state.push(t1);
        }
        public void visitSWAP() {
            super.visitSWAP();
            Type t1 = current_state.pop(); Type t2 = current_state.pop();
            current_state.push(t1); current_state.push(t2);
        }
        public void visitIBINOP(byte op) {
            super.visitIBINOP(op);
            current_state.pop_I();
        }
        public void visitLBINOP(byte op) {
            super.visitLBINOP(op);
            current_state.pop_L();
        }
        public void visitFBINOP(byte op) {
            super.visitFBINOP(op);
            current_state.pop_F();
        }
        public void visitDBINOP(byte op) {
            super.visitDBINOP(op);
            current_state.pop_D();
        }
        public void visitIUNOP(byte op) {
            super.visitIUNOP(op);
        }
        public void visitLUNOP(byte op) {
            super.visitLUNOP(op);
        }
        public void visitFUNOP(byte op) {
            super.visitFUNOP(op);
        }
        public void visitDUNOP(byte op) {
            super.visitDUNOP(op);
        }
        public void visitISHIFT(byte op) {
            super.visitISHIFT(op);
            current_state.pop_I();
        }
        public void visitLSHIFT(byte op) {
            super.visitLSHIFT(op);
            current_state.pop_I();
        }
        public void visitIINC(int i, int v) {
            super.visitIINC(i, v);
        }
        public void visitI2L() {
            super.visitI2L();
            current_state.pop_I(); current_state.push_L();
        }
        public void visitI2F() {
            super.visitI2F();
            current_state.pop_I(); current_state.push_F();
        }
        public void visitI2D() {
            super.visitI2D();
            current_state.pop_I(); current_state.push_D();
        }
        public void visitL2I() {
            super.visitL2I();
            current_state.pop_L(); current_state.push_I();
        }
        public void visitL2F() {
            super.visitL2F();
            current_state.pop_L(); current_state.push_F();
        }
        public void visitL2D() {
            super.visitL2D();
            current_state.pop_L(); current_state.push_D();
        }
        public void visitF2I() {
            super.visitF2I();
            current_state.pop_F(); current_state.push_I();
        }
        public void visitF2L() {
            super.visitF2L();
            current_state.pop_F(); current_state.push_L();
        }
        public void visitF2D() {
            super.visitF2D();
            current_state.pop_F(); current_state.push_D();
        }
        public void visitD2I() {
            super.visitD2I();
            current_state.pop_D(); current_state.push_I();
        }
        public void visitD2L() {
            super.visitD2L();
            current_state.pop_D(); current_state.push_L();
        }
        public void visitD2F() {
            super.visitD2F();
            current_state.pop_D(); current_state.push_F();
        }
        public void visitI2B() {
            super.visitI2B();
        }
        public void visitI2C() {
            super.visitI2C();
        }
        public void visitI2S() {
            super.visitI2S();
        }
        public void visitLCMP2() {
            super.visitLCMP2();
            current_state.pop_L(); current_state.pop_L(); current_state.push_I();
        }
        public void visitFCMP2(byte op) {
            super.visitFCMP2(op);
            current_state.pop_F(); current_state.pop_F(); current_state.push_I();
        }
        public void visitDCMP2(byte op) {
            super.visitDCMP2(op);
            current_state.pop_D(); current_state.pop_D(); current_state.push_I();
        }
        public void visitIF(byte op, int target) {
            super.visitIF(op, target);
            current_state.pop_I();
        }
        public void visitIFREF(byte op, int target) {
            super.visitIFREF(op, target);
            current_state.pop_A();
        }
        public void visitIFCMP(byte op, int target) {
            super.visitIFCMP(op, target);
            current_state.pop_I(); current_state.pop_I();
        }
        public void visitIFREFCMP(byte op, int target) {
            super.visitIFREFCMP(op, target);
            current_state.pop_A(); current_state.pop_A();
        }
        public void visitGOTO(int target) {
            super.visitGOTO(target);
        }
        public void visitJSR(int target) {
            super.visitJSR(target);
            endsWithJSR = true;
            current_state.push_RetAddr(target);
        }
        public void visitRET(int i) {
            super.visitRET(i);
            Retaddr r = (Retaddr)current_state.getLocal(i);
            endsWithRET = true;
            // add JSR edges, if they don't already exist.
            BasicBlock jsub_bb = cfg.getBasicBlockByBytecodeIndex(r.location);
            if (current_bb.getNumberOfSuccessors() == 0) {
                if (TRACE) out.println("Adding jsr subroutine edges to "+current_bb);
                current_bb.setSubroutineRet(cfg, jsub_bb);
                if (TRACE) {
                    out.println("Number of jsr subroutine edges: "+current_bb.getNumberOfSuccessors());
                    for (int j=0; j<current_bb.getNumberOfSuccessors(); ++j) {
                        out.println("Successor "+j+": "+current_bb.getSuccessor(j));
                    }
                }
            }
            cfg.addJSRInfo(jsub_bb, current_bb, ((ExactJSRState)current_state).mayChangeLocals);
        }
        public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
            super.visitTABLESWITCH(default_target, low, high, targets);
            current_state.pop_I();
        }
        public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
            super.visitLOOKUPSWITCH(default_target, values, targets);
            current_state.pop_I();
        }
        public void visitIRETURN() {
            super.visitIRETURN();
        }
        public void visitLRETURN() {
            super.visitLRETURN();
        }
        public void visitFRETURN() {
            super.visitFRETURN();
        }
        public void visitDRETURN() {
            super.visitDRETURN();
        }
        public void visitARETURN() {
            super.visitARETURN();
        }
        public void visitVRETURN() {
            super.visitVRETURN();
        }
        public void visitIGETSTATIC(jq_StaticField f) {
            super.visitIGETSTATIC(f);
            current_state.push_I();
        }
        public void visitLGETSTATIC(jq_StaticField f) {
            super.visitLGETSTATIC(f);
            current_state.push_L();
        }
        public void visitFGETSTATIC(jq_StaticField f) {
            super.visitFGETSTATIC(f);
            current_state.push_F();
        }
        public void visitDGETSTATIC(jq_StaticField f) {
            super.visitDGETSTATIC(f);
            current_state.push_D();
        }
        public void visitAGETSTATIC(jq_StaticField f) {
            super.visitAGETSTATIC(f);
            Type t;
            if (f.getType().isAddressType()) t = DerivedRef.INSTANCE;
            else t = new SystemType(f.getType());
            current_state.push(t);
        }
        public void visitZGETSTATIC(jq_StaticField f) {
            super.visitZGETSTATIC(f);
            current_state.push_I();
        }
        public void visitBGETSTATIC(jq_StaticField f) {
            super.visitBGETSTATIC(f);
            current_state.push_I();
        }
        public void visitCGETSTATIC(jq_StaticField f) {
            super.visitCGETSTATIC(f);
            current_state.push_I();
        }
        public void visitSGETSTATIC(jq_StaticField f) {
            super.visitSGETSTATIC(f);
            current_state.push_I();
        }
        public void visitIPUTSTATIC(jq_StaticField f) {
            super.visitIPUTSTATIC(f);
            current_state.pop_I();
        }
        public void visitLPUTSTATIC(jq_StaticField f) {
            super.visitLPUTSTATIC(f);
            current_state.pop_L();
        }
        public void visitFPUTSTATIC(jq_StaticField f) {
            super.visitFPUTSTATIC(f);
            current_state.pop_F();
        }
        public void visitDPUTSTATIC(jq_StaticField f) {
            super.visitDPUTSTATIC(f);
            current_state.pop_D();
        }
        public void visitAPUTSTATIC(jq_StaticField f) {
            super.visitAPUTSTATIC(f);
            if (f.getType().isAddressType()) current_state.pop_R();
            else current_state.pop_A();
        }
        public void visitZPUTSTATIC(jq_StaticField f) {
            super.visitZPUTSTATIC(f);
            current_state.pop_I();
        }
        public void visitBPUTSTATIC(jq_StaticField f) {
            super.visitBPUTSTATIC(f);
            current_state.pop_I();
        }
        public void visitCPUTSTATIC(jq_StaticField f) {
            super.visitCPUTSTATIC(f);
            current_state.pop_I();
        }
        public void visitSPUTSTATIC(jq_StaticField f) {
            super.visitSPUTSTATIC(f);
            current_state.pop_I();
        }
        public void visitIGETFIELD(jq_InstanceField f) {
            super.visitIGETFIELD(f);
            current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitLGETFIELD(jq_InstanceField f) {
            super.visitLGETFIELD(f);
            current_state.pop_A(); current_state.push_L();
            mergeWithExceptionHandlers();
        }
        public void visitFGETFIELD(jq_InstanceField f) {
            super.visitFGETFIELD(f);
            current_state.pop_A(); current_state.push_F();
            mergeWithExceptionHandlers();
        }
        public void visitDGETFIELD(jq_InstanceField f) {
            super.visitDGETFIELD(f);
            current_state.pop_A(); current_state.push_D();
            mergeWithExceptionHandlers();
        }
        public void visitAGETFIELD(jq_InstanceField f) {
            super.visitAGETFIELD(f);
            current_state.pop_A();
            Type t;
            if (f.getType().isAddressType()) t = DerivedRef.INSTANCE;
            else t = new SystemType(f.getType());
            current_state.push(t);
            mergeWithExceptionHandlers();
        }
        public void visitBGETFIELD(jq_InstanceField f) {
            super.visitBGETFIELD(f);
            current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitCGETFIELD(jq_InstanceField f) {
            super.visitCGETFIELD(f);
            current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitSGETFIELD(jq_InstanceField f) {
            super.visitSGETFIELD(f);
            current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitZGETFIELD(jq_InstanceField f) {
            super.visitZGETFIELD(f);
            current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitIPUTFIELD(jq_InstanceField f) {
            super.visitIPUTFIELD(f);
            current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitLPUTFIELD(jq_InstanceField f) {
            super.visitLPUTFIELD(f);
            current_state.pop_L(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitFPUTFIELD(jq_InstanceField f) {
            super.visitFPUTFIELD(f);
            current_state.pop_F(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitDPUTFIELD(jq_InstanceField f) {
            super.visitDPUTFIELD(f);
            current_state.pop_D(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitAPUTFIELD(jq_InstanceField f) {
            super.visitAPUTFIELD(f);
            if (f.getType().isAddressType()) current_state.pop_R();
            else current_state.pop_A();
            current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitBPUTFIELD(jq_InstanceField f) {
            super.visitBPUTFIELD(f);
            current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitCPUTFIELD(jq_InstanceField f) {
            super.visitCPUTFIELD(f);
            current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitSPUTFIELD(jq_InstanceField f) {
            super.visitSPUTFIELD(f);
            current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitZPUTFIELD(jq_InstanceField f) {
            super.visitZPUTFIELD(f);
            current_state.pop_I(); current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        private void INVOKEhelper(jq_Method f) {
            jq_Type[] paramTypes = f.getParamTypes();
            for (int i=paramTypes.length-1; i>=0; --i) {
                current_state.pop(paramTypes[i]);
            }
            mergeWithExceptionHandlers();
        }
        public void visitIINVOKE(byte op, jq_Method f) {
            super.visitIINVOKE(op, f);
            INVOKEhelper(f); current_state.push_I();
        }
        public void visitLINVOKE(byte op, jq_Method f) {
            super.visitLINVOKE(op, f);
            INVOKEhelper(f); current_state.push_L();
        }
        public void visitFINVOKE(byte op, jq_Method f) {
            super.visitFINVOKE(op, f);
            INVOKEhelper(f); current_state.push_F();
        }
        public void visitDINVOKE(byte op, jq_Method f) {
            super.visitDINVOKE(op, f);
            INVOKEhelper(f); current_state.push_D();
        }
        public void visitAINVOKE(byte op, jq_Method f) {
            super.visitAINVOKE(op, f);
            INVOKEhelper(f);
            Type t;
            if (f.getReturnType().isAddressType()) t = DerivedRef.INSTANCE;
            else t = new SystemType(f.getReturnType());
            current_state.push(t);
        }
        public void visitVINVOKE(byte op, jq_Method f) {
            super.visitVINVOKE(op, f);
            INVOKEhelper(f);
        }
        public void visitNEW(jq_Type f) {
            super.visitNEW(f);
            current_state.push(new SystemType(f));
            mergeWithExceptionHandlers();
        }
        public void visitNEWARRAY(jq_Array f) {
            super.visitNEWARRAY(f);
            current_state.pop_I();
            current_state.push(new SystemType(f));
            mergeWithExceptionHandlers();
        }
        public void visitCHECKCAST(jq_Type f) {
            super.visitCHECKCAST(f);
            current_state.pop_A();
            current_state.push(new SystemType(f));
            mergeWithExceptionHandlers();
        }
        public void visitINSTANCEOF(jq_Type f) {
            super.visitINSTANCEOF(f);
            current_state.pop_A(); current_state.push_I();
        }
        public void visitARRAYLENGTH() {
            super.visitARRAYLENGTH();
            current_state.pop_A(); current_state.push_I();
            mergeWithExceptionHandlers();
        }
        public void visitATHROW() {
            super.visitATHROW();
            mergeWithExceptionHandlers();
        }
        public void visitMONITOR(byte op) {
            super.visitMONITOR(op);
            current_state.pop_A();
            mergeWithExceptionHandlers();
        }
        public void visitMULTINEWARRAY(jq_Type f, char dim) {
            super.visitMULTINEWARRAY(f, dim);
            for (int i=0; i<dim; ++i) current_state.pop_I();
            current_state.push(new SystemType(f));
            mergeWithExceptionHandlers();
        }
    }
    
    public class SecondPassVisitor extends BytecodeVisitor {
        private BitString bytecode_start;
        private BasicBlock current_bb;
        private ExactState current_state;

        boolean go_again = false;

        SecondPassVisitor(jq_Method method, BitString bytecode_start) {
            super(method);
            this.bytecode_start = bytecode_start;
            this.current_state = ExactState.allocateEmptyState(method);
            this.TRACE = ALWAYS_TRACE;
        }
        
        public String toString() { return "LR2/"+this.method.getName(); }
        
        public void traverseBB(joeq.Compiler.BytecodeAnalysis.BasicBlock bb) {
            if ((end_states[bb.id] == null) || (start_states[bb.id] == null)) {
                // unreachable block!
                if (TRACE) out.println("Basic block "+bb+" is unreachable!");
                return;
            }
            if (bb.getStart() == -1) {
                return;
            }
            if (TRACE) out.println("Visiting "+bb);
            current_state = end_states[bb.id].copy();
            current_state.allocateLiveness();
            current_state.initializeLastUses();
            current_bb = bb;
            i_end = bb.getEnd();
            BitStringIterator it = bytecode_start.backwardsIterator(i_end);
            while (it.hasNext()) {
                i_start = it.nextIndex(); i_end = i_start-1;
                this.visitBytecode();
                if (i_start <= bb.getStart()) break;
            }
            if (current_state.compareLiveness(start_states[bb.id]))
                go_again = true;
            start_states[bb.id] = current_state;
            for (int i=0; i<bb.getNumberOfPredecessors(); ++i) {
                this.mergeStateWith(bb.getPredecessor(i));
            }
        }
        
        private boolean mergeStateWith(BasicBlock bb2) {
            if (end_states[bb2.id] == null) {
                end_states[bb2.id] = current_state.copy();
                return true;
            } else return end_states[bb2.id].mergeLiveness(current_state);
        }
        
        public void visitPEI() {
            ExceptionHandlerIterator ehi = current_bb.getExceptionHandlers();
            while (ehi.hasNext()) {
                ExceptionHandler eh = ehi.nextEH();
                BasicBlock bb2 = eh.getEntry();
                if (start_states[bb2.id] == null) {
                    if (TRACE) out.println("No live var info for handler "+bb2+" yet");
                    continue;
                }
                if (TRACE) out.println("Merging current state "+current_state+" with live var info from handler "+bb2+": "+start_states[bb2.id]);
                // merge call is the same as the normal one
                if (current_state.mergeLiveness(start_states[bb2.id])) {
                    if (TRACE) out.println("Change in current state: "+current_state);
                }
            }
        }

        public void visitILOAD(int i) {
            super.visitILOAD(i);
            current_state.liveLocal_I(i_start, i);
        }
        public void visitLLOAD(int i) {
            super.visitLLOAD(i);
            current_state.liveLocal_L(i_start, i);
        }
        public void visitFLOAD(int i) {
            super.visitFLOAD(i);
            current_state.liveLocal_F(i_start, i);
        }
        public void visitDLOAD(int i) {
            super.visitDLOAD(i);
            current_state.liveLocal_D(i_start, i);
        }
        public void visitALOAD(int i) {
            super.visitALOAD(i);
            current_state.liveLocal_A(i_start, i);
        }
        public void visitISTORE(int i) {
            super.visitISTORE(i);
            current_state.deadLocal_I(i);
        }
        public void visitLSTORE(int i) {
            super.visitLSTORE(i);
            current_state.deadLocal_L(i);
        }
        public void visitFSTORE(int i) {
            super.visitFSTORE(i);
            current_state.deadLocal_F(i);
        }
        public void visitDSTORE(int i) {
            super.visitDSTORE(i);
            current_state.deadLocal_D(i);
        }
        public void visitASTORE(int i) {
            super.visitASTORE(i);
            current_state.deadLocal_A(i);
        }
        
    }

}
