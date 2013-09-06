// BytecodeToQuad.java, created Fri Jan 11 16:42:38 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_TryCatchBC;
import joeq.Class.jq_Type;
import joeq.Compiler.CompilationState;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Compiler.Quad.Operand.AConstOperand;
import joeq.Compiler.Quad.Operand.ConditionOperand;
import joeq.Compiler.Quad.Operand.DConstOperand;
import joeq.Compiler.Quad.Operand.FConstOperand;
import joeq.Compiler.Quad.Operand.FieldOperand;
import joeq.Compiler.Quad.Operand.IConstOperand;
import joeq.Compiler.Quad.Operand.LConstOperand;
import joeq.Compiler.Quad.Operand.MethodOperand;
import joeq.Compiler.Quad.Operand.PConstOperand;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operand.TargetOperand;
import joeq.Compiler.Quad.Operand.TypeOperand;
import joeq.Compiler.Quad.Operand.UnnecessaryGuardOperand;
import joeq.Compiler.Quad.Operator.ALength;
import joeq.Compiler.Quad.Operator.ALoad;
import joeq.Compiler.Quad.Operator.AStore;
import joeq.Compiler.Quad.Operator.Binary;
import joeq.Compiler.Quad.Operator.BoundsCheck;
import joeq.Compiler.Quad.Operator.CheckCast;
import joeq.Compiler.Quad.Operator.Getfield;
import joeq.Compiler.Quad.Operator.Getstatic;
import joeq.Compiler.Quad.Operator.Goto;
import joeq.Compiler.Quad.Operator.InstanceOf;
import joeq.Compiler.Quad.Operator.IntIfCmp;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.Jsr;
import joeq.Compiler.Quad.Operator.LookupSwitch;
import joeq.Compiler.Quad.Operator.MemLoad;
import joeq.Compiler.Quad.Operator.MemStore;
import joeq.Compiler.Quad.Operator.Monitor;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.New;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.Operator.NullCheck;
import joeq.Compiler.Quad.Operator.Putfield;
import joeq.Compiler.Quad.Operator.Putstatic;
import joeq.Compiler.Quad.Operator.Ret;
import joeq.Compiler.Quad.Operator.Return;
import joeq.Compiler.Quad.Operator.Special;
import joeq.Compiler.Quad.Operator.StoreCheck;
import joeq.Compiler.Quad.Operator.TableSwitch;
import joeq.Compiler.Quad.Operator.Unary;
import joeq.Compiler.Quad.Operator.ZeroCheck;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * Converts stack-based Java bytecode to Quad intermediate format.
 * This utilizes the ControlFlowGraph in the BytecodeAnalysis package to build
 * up a control flow graph, then iterates over the graph to generate the Quad
 * code.
 *
 * @see  BytecodeVisitor
 * @see  joeq.Compiler.BytecodeAnalysis.ControlFlowGraph
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BytecodeToQuad.java,v 1.74 2005/05/27 20:03:34 joewhaley Exp $
 */
public class BytecodeToQuad extends BytecodeVisitor {
    
    private ControlFlowGraph quad_cfg;
    private BasicBlock quad_bb;
    private joeq.Compiler.BytecodeAnalysis.ControlFlowGraph bc_cfg;
    private joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb;
    private BasicBlock[] quad_bbs;
    private RegisterFactory rf;
    
    private boolean[] visited;
    private boolean uncond_branch;
    private LinkedList regenerate;

    private HashMap quad2bci = new HashMap();

    public static boolean ALWAYS_TRACE = false;

    /** Initializes the conversion from bytecode to quad format for the given method.
     * @param  method the method to convert. */
    public BytecodeToQuad(jq_Method method) {
        this(CompilationState.DEFAULT, method);
    }
    
    /** Initializes the conversion from bytecode to quad format for the given method.
     * @param  method the method to convert. */
    public BytecodeToQuad(CompilationState state, jq_Method method) {
        super(state, method);
        TRACE = ALWAYS_TRACE;
    }
    
    /** Returns a string with the name of the pass and the method being converted.
     * @return  a string with the name of the pass and the method being converted. */
    public String toString() {
        return "BC2Q/"+Strings.left(method.getName().toString(), 10);
    }
    /** Perform conversion process from bytecode to quad.
     * @return  the control flow graph of the resulting quad representation. */
    public ControlFlowGraph convert() {
        bc_cfg = joeq.Compiler.BytecodeAnalysis.ControlFlowGraph.computeCFG(method);
        
        // initialize register factory
        this.rf = new RegisterFactory(method);
        
        // copy bytecode cfg to quad cfg
        jq_TryCatchBC[] exs = method.getExceptionTable();
        this.quad_cfg = new ControlFlowGraph(method, bc_cfg.getExit().getNumberOfPredecessors(),
                                                 exs.length, this.rf);
        quad_bbs = new BasicBlock[bc_cfg.getNumberOfBasicBlocks()];
        quad_bbs[0] = this.quad_cfg.entry();
        quad_bbs[1] = this.quad_cfg.exit();
        for (int i=2; i<quad_bbs.length; ++i) {
            joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb = bc_cfg.getBasicBlock(i);
            int n_pred = bc_bb.getNumberOfPredecessors();
            int n_succ = bc_bb.getNumberOfSuccessors();
            int n_inst = bc_bb.getEnd() - bc_bb.getStart() + 1; // estimate
            quad_bbs[i] = BasicBlock.createBasicBlock(i, n_pred, n_succ, n_inst);
        }
        this.quad_cfg.updateBBcounter(quad_bbs.length);

        // add exception handlers.
        for (int i=exs.length-1; i>=0; --i) {
            jq_TryCatchBC ex = exs[i];
            joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb = bc_cfg.getBasicBlockByBytecodeIndex(ex.getStartPC());
            Assert._assert(bc_bb.getStart() < ex.getEndPC());
            BasicBlock ex_handler = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(ex.getHandlerPC()).id];
            ex_handler.setExceptionHandlerEntry();
            int numOfProtectedBlocks = (ex.getEndPC()==method.getBytecode().length?quad_bbs.length:bc_cfg.getBasicBlockByBytecodeIndex(ex.getEndPC()).id) - bc_bb.id;
            ExceptionHandler eh = new ExceptionHandler(ex.getExceptionType(), numOfProtectedBlocks, ex_handler);
            quad_cfg.addExceptionHandler(eh);
            ExceptionHandlerList ehs = new ExceptionHandlerList(eh, null);
            BasicBlock bb = quad_bbs[bc_bb.id];
            bb.addExceptionHandler_first(ehs);
            for (;;) {
                bc_bb = bc_cfg.getBasicBlock(bc_bb.id+1);
                bb = quad_bbs[bc_bb.id];
                if (bc_bb.getStart() >= ex.getEndPC()) break;
                ehs = bb.addExceptionHandler(ehs);
            }
        }
        this.start_states = new AbstractState[quad_bbs.length];
        for (int i=0; i<quad_bbs.length; ++i) {
            joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb = bc_cfg.getBasicBlock(i);
            BasicBlock bb = quad_bbs[i];
            for (int j=0; j<bc_bb.getNumberOfPredecessors(); ++j) {
                bb.addPredecessor(quad_bbs[bc_bb.getPredecessor(j).id]);
            }
            for (int j=0; j<bc_bb.getNumberOfSuccessors(); ++j) {
                bb.addSuccessor(quad_bbs[bc_bb.getSuccessor(j).id]);
            }
            // --> start state allocated on demand in merge
            //this.start_states[i] = new AbstractState(max_stack, max_locals);
        }

        // initialize start state
        this.start_states[2] = allocateInitialState();
        this.current_state = allocateEmptyState();
        
        regenerate = new LinkedList();
        visited = new boolean[quad_bbs.length];
        // traverse reverse post-order over basic blocks to generate instructions
        joeq.Compiler.BytecodeAnalysis.ControlFlowGraph.RPOBasicBlockIterator rpo = bc_cfg.reversePostOrderIterator();
        joeq.Compiler.BytecodeAnalysis.BasicBlock first_bb = rpo.nextBB();
        Assert._assert(first_bb == bc_cfg.getEntry());
        while (rpo.hasNext()) {
            joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb = rpo.nextBB();
            visited[bc_bb.id] = true;
            this.traverseBB(bc_bb);
        }
        while (!regenerate.isEmpty()) {
            if (TRACE) out.println("Blocks to regenerate: "+regenerate);
            joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb =
                (joeq.Compiler.BytecodeAnalysis.BasicBlock)regenerate.removeFirst();
            visited[bc_bb.id] = true;
            this.traverseBB(bc_bb);
        }
        if (quad_cfg.removeUnreachableBasicBlocks()) {
            if (TRACE) out.println("Unreachable code in "+method);
        }
        // TODO: need to fix cfg's with infinite loops.
        return this.quad_cfg;
    }

    private boolean endBasicBlock;
    private boolean endsWithRET;
    
    /**
     * @param  bc_bb  */
    public void traverseBB(joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb) {
        if (start_states[bc_bb.id] == null) {
            // unreachable block!
            if (TRACE) out.println("Basic block "+bc_bb+" is unreachable!");
            return;
        }
        if (bc_bb.getStart() == -1) {
            return; // entry or exit
        }
        if (TRACE) out.println("Visiting "+bc_bb);
        this.quad_bb = quad_bbs[bc_bb.id];
        for (Iterator i = this.quad_bb.iterator(); i.hasNext(); ) {
            Object o = i.next();
            Object old = this.quad2bci.remove(o);
            if (old == null) {
                // GET_EXCEPTION has no bytecode index.
                //System.out.println(o+" was not in bcmap.");
            }
        }
        this.quad_bb.removeAllQuads();
        this.bc_bb = bc_bb;
        this.uncond_branch = false;
        this.current_state.overwriteWith(start_states[bc_bb.id]);
        if (this.quad_bb.isExceptionHandlerEntry()) {
            // TODO: find non-exceptional branches to exception handler entries and split the basic block.
            jq_Type type = ((RegisterOperand)this.current_state.peekStack(0)).getType();
            RegisterOperand t = getStackRegister(type, 0);
            this.quad_bb.appendQuad(Special.create(quad_cfg.getNewQuadID(), Special.GET_EXCEPTION.INSTANCE, t));
        }
        if (TRACE) this.current_state.dumpState();
        this.endBasicBlock = false;
        this.endsWithRET = false;
        for (i_end=bc_bb.getStart()-1; ; ) {
            i_start = i_end+1;
            if (isEndOfBB()) break;
            this.visitBytecode();
        }
        saveStackIntoRegisters();
        if (!endsWithRET) {
            for (int i=0; i<bc_bb.getNumberOfSuccessors(); ++i) {
                this.mergeStateWith(bc_bb.getSuccessor(i));
            }
        }
        if (TRACE) out.println("Finished visiting "+bc_bb);
    }
    
    private boolean isEndOfBB() {
        return i_start > bc_bb.getEnd() || endBasicBlock;
    }
    
    private void mergeStateWith(joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb) {
        if (start_states[bc_bb.id] == null) {
            if (TRACE) out.println("Copying current state to "+bc_bb);
            start_states[bc_bb.id] = current_state.copy();
        } else {
            if (TRACE) out.println("Merging current state with "+bc_bb);
            if (start_states[bc_bb.id].merge(current_state, rf)) {
                if (TRACE) out.println("in set of "+bc_bb+" changed");
                if (visited[bc_bb.id]) {
                    if (TRACE) out.println("must regenerate code for "+bc_bb);
                    if (!regenerate.contains(bc_bb)) {
                        regenerate.add(bc_bb);
                        start_states[bc_bb.id].rebuildStack();
                    }
                    visited[bc_bb.id] = false;
                }
            }
        }
    }
    private void mergeStateWith(joeq.Compiler.BytecodeAnalysis.ExceptionHandler eh) {
        joeq.Compiler.BytecodeAnalysis.BasicBlock bc_bb = eh.getEntry();
        if (start_states[bc_bb.id] == null) {
            if (TRACE) out.println("Copying exception state to "+bc_bb);
            start_states[bc_bb.id] = current_state.copyExceptionHandler(eh.getExceptionType(), rf);
        } else {
            if (TRACE) out.println("Merging exception state with "+bc_bb);
            if (start_states[bc_bb.id].mergeExceptionHandler(current_state, eh.getExceptionType(), rf)) {
                if (TRACE) out.println("in set of exception handler "+bc_bb+" changed");
                if (visited[bc_bb.id]) {
                    if (TRACE) out.println("must regenerate code for "+bc_bb);
                    if (!regenerate.contains(bc_bb)) {
                        regenerate.add(bc_bb);
                        start_states[bc_bb.id].rebuildStack();
                    }
                    visited[bc_bb.id] = false;
                }
            }
        }
    }

    private void saveStackIntoRegisters() {
        for (int i=0; i<current_state.getStackSize(); ++i) {
            Operand op = current_state.peekStack(i);
            if (op instanceof DummyOperand) continue;
            if (op instanceof RegisterOperand) {
                RegisterOperand rop = (RegisterOperand)op;
                Register r = rf.getOrCreateStack(current_state.getStackSize()-i-1, rop.getType());
                if (rop.getRegister() == r)
                    continue;
            }
            jq_Type type = getTypeOf(op);
            RegisterOperand t = getStackRegister(type, i);
            Quad q = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type), t, op);
            appendQuad(q);
            current_state.pokeStack(i, t.copy());
        }
    }

    public boolean isLocal(Operand op, int index, jq_Type type) {
        if (op instanceof RegisterOperand) {
            Register r = ((RegisterOperand)op).getRegister();
            if (r.isTemp()) return false;
            return rf.getOrCreateLocal(index, type) == r;
        }
        return false;
    }
    
    private void replaceLocalsOnStack(int index, jq_Type type) {
        for (int i=0; i<current_state.getStackSize(); ++i) {
            Operand op = current_state.peekStack(i);
            if (isLocal(op, index, type)) {
                RegisterOperand rop = (RegisterOperand)op;
                RegisterOperand t = getStackRegister(type, i);
                t.setFlags(rop.getFlags()); t.scratchObject = rop.scratchObject;
                Quad q = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type), t, rop);
                appendQuad(q);
                current_state.pokeStack(i, t.copy());
            }
        }
    }
    
    void appendQuad(Quad q) {
        if (TRACE) out.println(q.toString());
        quad_bb.appendQuad(q);
        quad2bci.put(q, new Integer(i_start));
    }
    
    /**
     * return quad->bytecode map, may be incomplete
     * @return Map<Quad, Integer> 
     */
    java.util.Map getQuadToBytecodeMap() {
        return quad2bci;
    }
    
    RegisterOperand getStackRegister(jq_Type type, int i) {
        if (current_state.getStackSize()-i-1 < 0) {
            System.out.println("Error in "+method+" offset "+i_start);
            current_state.dumpState();
        }
        return new RegisterOperand(rf.getOrCreateStack(current_state.getStackSize()-i-1, type), type);
    }
    
    RegisterOperand getStackRegister(jq_Type type) {
        return getStackRegister(type, -1);
    }

    RegisterOperand makeLocal(int i, jq_Type type) {
        return new RegisterOperand(rf.getOrCreateLocal(i, type), type);
    }
    
    RegisterOperand makeLocal(int i, RegisterOperand rop) {
        jq_Type type = rop.getType();
        return new RegisterOperand(rf.getOrCreateLocal(i, type), type, rop.getFlags());
    }
    
    static boolean hasGuard(RegisterOperand rop) { return rop.scratchObject != null; }
    static void setGuard(RegisterOperand rop, Operand guard) { rop.scratchObject = guard; }
    
    static Operand getGuard(Operand op) {
        if (op instanceof RegisterOperand) {
            RegisterOperand rop = (RegisterOperand)op;
            return (Operand)rop.scratchObject;
        }
        Assert._assert(op instanceof AConstOperand);
        return new UnnecessaryGuardOperand();
    }
    
    Operand currentGuard;
    
    void setCurrentGuard(Operand guard) { currentGuard = guard; }
    void clearCurrentGuard() { currentGuard = null; }
    Operand getCurrentGuard() { if (currentGuard == null) return null; return currentGuard.copy(); }
    
    private AbstractState[] start_states;
    private AbstractState current_state;
    
    public void visitNOP() {
        super.visitNOP();
        // do nothing
    }
    public void visitACONST(Object s) {
        super.visitACONST(s);
        current_state.push_A(new AConstOperand(s));
    }
    public void visitICONST(int c) {
        super.visitICONST(c);
        current_state.push_I(new IConstOperand(c));
    }
    public void visitLCONST(long c) {
        super.visitLCONST(c);
        current_state.push_L(new LConstOperand(c));
    }
    public void visitFCONST(float c) {
        super.visitFCONST(c);
        current_state.push_F(new FConstOperand(c));
    }
    public void visitDCONST(double c) {
        super.visitDCONST(c);
        current_state.push_D(new DConstOperand(c));
    }
    public void visitILOAD(int i) {
        super.visitILOAD(i);
        current_state.push_I(current_state.getLocal_I(i));
    }
    public void visitLLOAD(int i) {
        super.visitLLOAD(i);
        current_state.push_L(current_state.getLocal_L(i));
    }
    public void visitFLOAD(int i) {
        super.visitFLOAD(i);
        current_state.push_F(current_state.getLocal_F(i));
    }
    public void visitDLOAD(int i) {
        super.visitDLOAD(i);
        current_state.push_D(current_state.getLocal_D(i));
    }
    public void visitALOAD(int i) {
        super.visitALOAD(i);
        // could be A or R
        current_state.push(current_state.getLocal(i));
    }
    private void STOREhelper(int i, jq_Type type) {
        replaceLocalsOnStack(i, type);
        Operand op1 = current_state.pop(type);
        Operand local_value;
        RegisterOperand op0;
        if (op1 instanceof RegisterOperand) {
            // move from one local variable to another.
            local_value = op0 = makeLocal(i, (RegisterOperand)op1); // copy attributes.
        } else {
            // move a constant to a local variable.
            local_value = op1;
            op0 = makeLocal(i, type);
        }
        if (type.getReferenceSize() == 8) current_state.setLocalDual(i, local_value);
        else current_state.setLocal(i, local_value);
        Quad q = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type), op0, op1);
        appendQuad(q);
    }
    public void visitISTORE(int i) {
        super.visitISTORE(i);
        STOREhelper(i, jq_Primitive.INT);
    }
    public void visitLSTORE(int i) {
        super.visitLSTORE(i);
        STOREhelper(i, jq_Primitive.LONG);
    }
    public void visitFSTORE(int i) {
        super.visitFSTORE(i);
        STOREhelper(i, jq_Primitive.FLOAT);
    }
    public void visitDSTORE(int i) {
        super.visitDSTORE(i);
        STOREhelper(i, jq_Primitive.DOUBLE);
    }
    public void visitASTORE(int i) {
        super.visitASTORE(i);
        Operand op1 = current_state.peekStack(0);
        STOREhelper(i, getTypeOf(op1));
    }
    private void ALOADhelper(ALoad operator, jq_Type t) {
        Operand index = current_state.pop_I();
        Operand ref = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(ref)) {
            if (TRACE) System.out.println("Null check triggered on "+ref);
            return;
        }
        if (performBoundsCheck(ref, index)) {
            if (TRACE) System.out.println("Bounds check triggered on "+ref+" "+index);
            return;
        }
        if (t.isReferenceType()) {
            // refine type.
            t = getArrayElementTypeOf(ref);
            Assert._assert(!t.isAddressType());
        }
        RegisterOperand r = getStackRegister(t);
        Quad q = ALoad.create(quad_cfg.getNewQuadID(), operator, r, ref, index, getCurrentGuard());
        appendQuad(q);
        current_state.push(r.copy(), t);
    }
    public void visitIALOAD() {
        super.visitIALOAD();
        ALOADhelper(ALoad.ALOAD_I.INSTANCE, jq_Primitive.INT);
    }
    public void visitLALOAD() {
        super.visitLALOAD();
        ALOADhelper(ALoad.ALOAD_L.INSTANCE, jq_Primitive.LONG);
    }
    public void visitFALOAD() {
        super.visitFALOAD();
        ALOADhelper(ALoad.ALOAD_F.INSTANCE, jq_Primitive.FLOAT);
    }
    public void visitDALOAD() {
        super.visitDALOAD();
        ALOADhelper(ALoad.ALOAD_D.INSTANCE, jq_Primitive.DOUBLE);
    }
    public void visitAALOAD() {
        super.visitAALOAD();
        Operand index = current_state.pop_I();
        Operand ref = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(ref)) {
            if (TRACE) System.out.println("Null check triggered on "+ref);
            return;
        }
        if (performBoundsCheck(ref, index)) {
            if (TRACE) System.out.println("Bounds check triggered on "+ref+" "+index);
            return;
        }
        jq_Type t = getArrayElementTypeOf(ref);
        ALoad operator = t.isAddressType()?(ALoad)ALoad.ALOAD_P.INSTANCE:ALoad.ALOAD_A.INSTANCE;
        RegisterOperand r = getStackRegister(t);
        Quad q = ALoad.create(quad_cfg.getNewQuadID(), operator, r, ref, index, getCurrentGuard());
        appendQuad(q);
        current_state.push(r.copy(), t);
    }
    public void visitBALOAD() {
        super.visitBALOAD();
        ALOADhelper(ALoad.ALOAD_B.INSTANCE, jq_Primitive.BYTE);
    }
    public void visitCALOAD() {
        super.visitCALOAD();
        ALOADhelper(ALoad.ALOAD_C.INSTANCE, jq_Primitive.CHAR);
    }
    public void visitSALOAD() {
        super.visitSALOAD();
        ALOADhelper(ALoad.ALOAD_S.INSTANCE, jq_Primitive.SHORT);
    }
    private void ASTOREhelper(AStore operator, jq_Type t) {
        Operand val = current_state.pop(t);
        Operand index = current_state.pop_I();
        Operand ref = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(ref)) {
            if (TRACE) System.out.println("Null check triggered on "+ref);
            return;
        }
        if (performBoundsCheck(ref, index)) {
            if (TRACE) System.out.println("Bounds check triggered on "+ref+" "+index);
            return;
        }
        if (t.isReferenceType() && ref instanceof RegisterOperand) {
            // perform checkstore
            if (performCheckStore((RegisterOperand)ref, val)) return;
            Assert._assert(!t.isAddressType());
        }
        Quad q = AStore.create(quad_cfg.getNewQuadID(), operator, val, ref, index, getCurrentGuard());
        appendQuad(q);
    }
    public void visitIASTORE() {
        super.visitIASTORE();
        ASTOREhelper(AStore.ASTORE_I.INSTANCE, jq_Primitive.INT);
    }
    public void visitLASTORE() {
        super.visitLASTORE();
        ASTOREhelper(AStore.ASTORE_L.INSTANCE, jq_Primitive.LONG);
    }
    public void visitFASTORE() {
        super.visitFASTORE();
        ASTOREhelper(AStore.ASTORE_F.INSTANCE, jq_Primitive.FLOAT);
    }
    public void visitDASTORE() {
        super.visitDASTORE();
        ASTOREhelper(AStore.ASTORE_D.INSTANCE, jq_Primitive.DOUBLE);
    }
    public void visitAASTORE() {
        // could be A or R
        Operand val = current_state.pop();
        Operand index = current_state.pop_I();
        Operand ref = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(ref)) {
            if (TRACE) System.out.println("Null check triggered on "+ref);
            return;
        }
        if (performBoundsCheck(ref, index)) {
            if (TRACE) System.out.println("Bounds check triggered on "+ref+" "+index);
            return;
        }
        jq_Type arrayElemType = getArrayElementTypeOf(ref);
        AStore operator = arrayElemType.isAddressType()?(AStore)AStore.ASTORE_P.INSTANCE:AStore.ASTORE_A.INSTANCE;
        if (ref instanceof RegisterOperand) {
            // perform checkstore
            if (performCheckStore((RegisterOperand)ref, val)) return;
        }
        Quad q = AStore.create(quad_cfg.getNewQuadID(), operator, val, ref, index, getCurrentGuard());
        appendQuad(q);
    }
    public void visitBASTORE() {
        super.visitBASTORE();
        ASTOREhelper(AStore.ASTORE_B.INSTANCE, jq_Primitive.BYTE);
    }
    public void visitCASTORE() {
        super.visitCASTORE();
        ASTOREhelper(AStore.ASTORE_C.INSTANCE, jq_Primitive.CHAR);
    }
    public void visitSASTORE() {
        super.visitSASTORE();
        ASTOREhelper(AStore.ASTORE_S.INSTANCE, jq_Primitive.SHORT);
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
        Operand op = current_state.pop();
        int d = current_state.getStackSize();
        jq_Type type = getTypeOf(op);
        RegisterOperand t = new RegisterOperand(rf.getOrCreateStack(d+1, type), type);
        Quad q = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type), t, op);
        appendQuad(q);
        current_state.push(op.copy(), type);
        current_state.push(t.copy(), type);
    }
    private void do_DUP_x1() {
        Operand op1 = current_state.pop();
        Operand op2 = current_state.pop();
        int d = current_state.getStackSize();
        jq_Type type1 = getTypeOf(op1);
        jq_Type type2 = getTypeOf(op2);
        RegisterOperand t1 = new RegisterOperand(rf.getOrCreateStack(d+2, type1), type1);
        Quad q1 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type1), t1, op1);
        appendQuad(q1);
        RegisterOperand t2 = new RegisterOperand(rf.getOrCreateStack(d+1, type2), type2);
        Quad q2 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type2), t2, op2);
        appendQuad(q2);
        RegisterOperand t3 = new RegisterOperand(rf.getOrCreateStack(d, type1), type1);
        Quad q3 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type1), t3, t1.copy());
        appendQuad(q3);
        current_state.push(t3.copy(), type1);
        current_state.push(t2.copy(), type2);
        current_state.push(t1.copy(), type1);
    }
    public void visitDUP_x1() {
        super.visitDUP_x1();
        do_DUP_x1();
    }
    public void visitDUP_x2() {
        super.visitDUP_x2();
        Operand op1 = current_state.pop();
        Operand op2 = current_state.pop();
        Operand op3 = current_state.pop();
        int d = current_state.getStackSize();
        jq_Type type1 = getTypeOf(op1);
        RegisterOperand t1 = new RegisterOperand(rf.getOrCreateStack(d+3, type1), type1);
        Quad q1 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type1), t1, op1);
        appendQuad(q1);
        RegisterOperand t2 = null; jq_Type type2 = null;
        if (op2 != DummyOperand.DUMMY) {
            type2 = getTypeOf(op2);
            t2 = new RegisterOperand(rf.getOrCreateStack(d+2, type2), type2);
            Quad q2 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type2), t2, op2);
            appendQuad(q2);
        }
        jq_Type type3 = getTypeOf(op3);
        RegisterOperand t3 = new RegisterOperand(rf.getOrCreateStack(d+1, type3), type3);
        Quad q3 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type3), t3, op3);
        appendQuad(q3);
        RegisterOperand t4 = new RegisterOperand(rf.getOrCreateStack(d, type1), type1);
        Quad q4 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type1), t4, t1.copy());
        appendQuad(q4);
        current_state.push(t4.copy(), type1);
        current_state.push(t3.copy(), type3);
        if (op2 != DummyOperand.DUMMY)
            current_state.push(t2.copy(), type2);
        current_state.push(t1.copy(), type1);
    }
    public void visitDUP2() {
        super.visitDUP2();
        Operand op1 = current_state.pop();
        Operand op2 = current_state.pop();
        int d = current_state.getStackSize();
        RegisterOperand t1 = null; jq_Type type1 = null;
        if (op1 != DummyOperand.DUMMY) {
            type1 = getTypeOf(op1);
            t1 = new RegisterOperand(rf.getOrCreateStack(d+3, type1), type1);
            Quad q1 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type1), t1, op1);
            appendQuad(q1);
        }
        jq_Type type2 = getTypeOf(op2);
        RegisterOperand t2 = new RegisterOperand(rf.getOrCreateStack(d+2, type2), type2);
        Quad q2 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(type2), t2, op2);
        appendQuad(q2);
        current_state.push(t2.copy(), type2);
        if (op1 != DummyOperand.DUMMY)
            current_state.push(t1.copy(), type1);
        current_state.push(op2.copy(), type2);
        if (op1 != DummyOperand.DUMMY)
            current_state.push(op1.copy(), type1);
    }
    public void visitDUP2_x1() {
        super.visitDUP2_x1();
        // TODO: do this correctly.
        Operand op1 = current_state.pop();
        Operand op2 = current_state.pop();
        Operand op3 = current_state.pop();
        current_state.push(op2);
        current_state.push(op1);
        current_state.push(op3);
        current_state.push(op2.copy());
        current_state.push(op1.copy());
    }
    public void visitDUP2_x2() {
        super.visitDUP2_x2();
        // TODO: do this correctly.
        Operand op1 = current_state.pop();
        Operand op2 = current_state.pop();
        Operand op3 = current_state.pop();
        Operand op4 = current_state.pop();
        current_state.push(op2);
        current_state.push(op1);
        current_state.push(op4);
        current_state.push(op3);
        current_state.push(op2.copy());
        current_state.push(op1.copy());
    }
    public void visitSWAP() {
        super.visitSWAP();
        do_DUP_x1();
        current_state.pop();
    }
    private void BINOPhelper(Binary operator, jq_Type tr, jq_Type t1, jq_Type t2, boolean zero_check) {
        Operand op2 = current_state.pop(t2);
        Operand op1 = current_state.pop(t1);
        if (zero_check && performZeroCheck(op2)) {
            if (TRACE) System.out.println("Zero check triggered on "+op2);
            return;
        }
        RegisterOperand r = getStackRegister(tr);
        Quad q = Binary.create(quad_cfg.getNewQuadID(), operator, r, op1, op2);
        appendQuad(q);
        current_state.push(r.copy(), tr);
    }
    public void visitIBINOP(byte op) {
        super.visitIBINOP(op);
        Binary operator=null; boolean zero_check = false;
        switch (op) {
            case BINOP_ADD: operator = Binary.ADD_I.INSTANCE; break;
            case BINOP_SUB: operator = Binary.SUB_I.INSTANCE; break;
            case BINOP_MUL: operator = Binary.MUL_I.INSTANCE; break;
            case BINOP_DIV: operator = Binary.DIV_I.INSTANCE; zero_check = true; break;
            case BINOP_REM: operator = Binary.REM_I.INSTANCE; zero_check = true; break;
            case BINOP_AND: operator = Binary.AND_I.INSTANCE; break;
            case BINOP_OR: operator = Binary.OR_I.INSTANCE; break;
            case BINOP_XOR: operator = Binary.XOR_I.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        BINOPhelper(operator, jq_Primitive.INT, jq_Primitive.INT, jq_Primitive.INT, zero_check);
    }
    public void visitLBINOP(byte op) {
        super.visitLBINOP(op);
        Binary operator=null; boolean zero_check = false;
        switch (op) {
            case BINOP_ADD: operator = Binary.ADD_L.INSTANCE; break;
            case BINOP_SUB: operator = Binary.SUB_L.INSTANCE; break;
            case BINOP_MUL: operator = Binary.MUL_L.INSTANCE; break;
            case BINOP_DIV: operator = Binary.DIV_L.INSTANCE; zero_check = true; break;
            case BINOP_REM: operator = Binary.REM_L.INSTANCE; zero_check = true; break;
            case BINOP_AND: operator = Binary.AND_L.INSTANCE; break;
            case BINOP_OR: operator = Binary.OR_L.INSTANCE; break;
            case BINOP_XOR: operator = Binary.XOR_L.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        BINOPhelper(operator, jq_Primitive.LONG, jq_Primitive.LONG, jq_Primitive.LONG, zero_check);
    }
    public void visitFBINOP(byte op) {
        super.visitFBINOP(op);
        Binary operator=null;
        switch (op) {
            case BINOP_ADD: operator = Binary.ADD_F.INSTANCE; break;
            case BINOP_SUB: operator = Binary.SUB_F.INSTANCE; break;
            case BINOP_MUL: operator = Binary.MUL_F.INSTANCE; break;
            case BINOP_DIV: operator = Binary.DIV_F.INSTANCE; break;
            case BINOP_REM: operator = Binary.REM_F.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        BINOPhelper(operator, jq_Primitive.FLOAT, jq_Primitive.FLOAT, jq_Primitive.FLOAT, false);
    }
    public void visitDBINOP(byte op) {
        super.visitDBINOP(op);
        Binary operator=null;
        switch (op) {
            case BINOP_ADD: operator = Binary.ADD_D.INSTANCE; break;
            case BINOP_SUB: operator = Binary.SUB_D.INSTANCE; break;
            case BINOP_MUL: operator = Binary.MUL_D.INSTANCE; break;
            case BINOP_DIV: operator = Binary.DIV_D.INSTANCE; break;
            case BINOP_REM: operator = Binary.REM_D.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        BINOPhelper(operator, jq_Primitive.DOUBLE, jq_Primitive.DOUBLE, jq_Primitive.DOUBLE, false);
    }
    public void UNOPhelper(Unary operator, jq_Type tr, jq_Type t1) {
        Operand op1 = current_state.pop(t1);
        RegisterOperand r = getStackRegister(tr);
        Quad q = Unary.create(quad_cfg.getNewQuadID(), operator, r, op1);
        appendQuad(q);
        current_state.push(r.copy(), tr);
    }
    public void visitIUNOP(byte op) {
        super.visitIUNOP(op);
        Unary operator=null;
        switch (op) {
            case UNOP_NEG: operator = Unary.NEG_I.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        UNOPhelper(operator, jq_Primitive.INT, jq_Primitive.INT);
    }
    public void visitLUNOP(byte op) {
        super.visitLUNOP(op);
        Unary operator=null;
        switch (op) {
            case UNOP_NEG: operator = Unary.NEG_L.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        UNOPhelper(operator, jq_Primitive.LONG, jq_Primitive.LONG);
    }
    public void visitFUNOP(byte op) {
        super.visitFUNOP(op);
        Unary operator=null;
        switch (op) {
            case UNOP_NEG: operator = Unary.NEG_F.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        UNOPhelper(operator, jq_Primitive.FLOAT, jq_Primitive.FLOAT);
    }
    public void visitDUNOP(byte op) {
        super.visitDUNOP(op);
        Unary operator=null;
        switch (op) {
            case UNOP_NEG: operator = Unary.NEG_D.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        UNOPhelper(operator, jq_Primitive.DOUBLE, jq_Primitive.DOUBLE);
    }
    public void visitISHIFT(byte op) {
        super.visitISHIFT(op);
        Binary operator=null;
        switch (op) {
            case SHIFT_LEFT: operator = Binary.SHL_I.INSTANCE; break;
            case SHIFT_RIGHT: operator = Binary.SHR_I.INSTANCE; break;
            case SHIFT_URIGHT: operator = Binary.USHR_I.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        BINOPhelper(operator, jq_Primitive.INT, jq_Primitive.INT, jq_Primitive.INT, false);
    }
    public void visitLSHIFT(byte op) {
        super.visitLSHIFT(op);
        Binary operator=null;
        switch (op) {
            case SHIFT_LEFT: operator = Binary.SHL_L.INSTANCE; break;
            case SHIFT_RIGHT: operator = Binary.SHR_L.INSTANCE; break;
            case SHIFT_URIGHT: operator = Binary.USHR_L.INSTANCE; break;
            default: Assert.UNREACHABLE(); break;
        }
        BINOPhelper(operator, jq_Primitive.LONG, jq_Primitive.LONG, jq_Primitive.INT, false);
    }
    public void visitIINC(int i, int v) {
        super.visitIINC(i, v);
        Operand op1 = current_state.getLocal_I(i);
        replaceLocalsOnStack(i, jq_Primitive.INT);
        RegisterOperand op0 = makeLocal(i, jq_Primitive.INT);
        Quad q = Binary.create(quad_cfg.getNewQuadID(), Binary.ADD_I.INSTANCE, op0, op1, new IConstOperand(v));
        appendQuad(q);
        current_state.setLocal(i, op0.copy());
    }
    public void visitI2L() {
        super.visitI2L();
        UNOPhelper(Unary.INT_2LONG.INSTANCE, jq_Primitive.LONG, jq_Primitive.INT);
    }
    public void visitI2F() {
        super.visitI2F();
        UNOPhelper(Unary.INT_2FLOAT.INSTANCE, jq_Primitive.FLOAT, jq_Primitive.INT);
    }
    public void visitI2D() {
        super.visitI2D();
        UNOPhelper(Unary.INT_2DOUBLE.INSTANCE, jq_Primitive.DOUBLE, jq_Primitive.INT);
    }
    public void visitL2I() {
        super.visitL2I();
        UNOPhelper(Unary.LONG_2INT.INSTANCE, jq_Primitive.INT, jq_Primitive.LONG);
    }
    public void visitL2F() {
        super.visitL2F();
        UNOPhelper(Unary.LONG_2FLOAT.INSTANCE, jq_Primitive.FLOAT, jq_Primitive.LONG);
    }
    public void visitL2D() {
        super.visitL2D();
        UNOPhelper(Unary.LONG_2DOUBLE.INSTANCE, jq_Primitive.DOUBLE, jq_Primitive.LONG);
    }
    public void visitF2I() {
        super.visitF2I();
        UNOPhelper(Unary.FLOAT_2INT.INSTANCE, jq_Primitive.INT, jq_Primitive.FLOAT);
    }
    public void visitF2L() {
        super.visitF2L();
        UNOPhelper(Unary.FLOAT_2LONG.INSTANCE, jq_Primitive.LONG, jq_Primitive.FLOAT);
    }
    public void visitF2D() {
        super.visitF2D();
        UNOPhelper(Unary.FLOAT_2DOUBLE.INSTANCE, jq_Primitive.DOUBLE, jq_Primitive.FLOAT);
    }
    public void visitD2I() {
        super.visitD2I();
        UNOPhelper(Unary.DOUBLE_2INT.INSTANCE, jq_Primitive.INT, jq_Primitive.DOUBLE);
    }
    public void visitD2L() {
        super.visitD2L();
        UNOPhelper(Unary.DOUBLE_2LONG.INSTANCE, jq_Primitive.LONG, jq_Primitive.DOUBLE);
    }
    public void visitD2F() {
        super.visitD2F();
        UNOPhelper(Unary.DOUBLE_2FLOAT.INSTANCE, jq_Primitive.FLOAT, jq_Primitive.DOUBLE);
    }
    public void visitI2B() {
        super.visitI2B();
        UNOPhelper(Unary.INT_2BYTE.INSTANCE, jq_Primitive.BYTE, jq_Primitive.INT);
    }
    public void visitI2C() {
        super.visitI2C();
        UNOPhelper(Unary.INT_2CHAR.INSTANCE, jq_Primitive.CHAR, jq_Primitive.INT);
    }
    public void visitI2S() {
        super.visitI2S();
        UNOPhelper(Unary.INT_2SHORT.INSTANCE, jq_Primitive.SHORT, jq_Primitive.INT);
    }
    public void visitLCMP2() {
        super.visitLCMP2();
        BINOPhelper(Binary.CMP_L.INSTANCE, jq_Primitive.INT, jq_Primitive.LONG, jq_Primitive.LONG, false);
    }
    public void visitFCMP2(byte op) {
        super.visitFCMP2(op);
        Binary o = op==CMP_L?(Binary)Binary.CMP_FL.INSTANCE:(Binary)Binary.CMP_FG.INSTANCE;
        BINOPhelper(o, jq_Primitive.INT, jq_Primitive.FLOAT, jq_Primitive.FLOAT, false);
    }
    public void visitDCMP2(byte op) {
        super.visitDCMP2(op);
        Binary o = op==CMP_L?(Binary)Binary.CMP_DL.INSTANCE:(Binary)Binary.CMP_DG.INSTANCE;
        BINOPhelper(o, jq_Primitive.INT, jq_Primitive.DOUBLE, jq_Primitive.DOUBLE, false);
    }
    public void visitIF(byte op, int target) {
        super.visitIF(op, target);
        Operand op0 = current_state.pop_I();
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(target).id];
        ConditionOperand cond = new ConditionOperand(op);
        Quad q = IntIfCmp.create(quad_cfg.getNewQuadID(), IntIfCmp.IFCMP_I.INSTANCE, op0, new IConstOperand(0), cond, new TargetOperand(target_bb));
        appendQuad(q);
    }
    public void visitIFREF(byte op, int target) {
        super.visitIFREF(op, target);
        // could be A or R
        Operand op0 = current_state.pop();
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(target).id];
        ConditionOperand cond = new ConditionOperand(op);
        jq_Type t = getTypeOf(op0);
        IntIfCmp operator = t.isAddressType()?(IntIfCmp)IntIfCmp.IFCMP_P.INSTANCE:IntIfCmp.IFCMP_A.INSTANCE;
        Operand op1 = t.isAddressType()?(Operand)new PConstOperand(null):new AConstOperand(null);
        Quad q = IntIfCmp.create(quad_cfg.getNewQuadID(), operator, op0, op1, cond, new TargetOperand(target_bb));
        appendQuad(q);
    }
    public void visitIFCMP(byte op, int target) {
        super.visitIFCMP(op, target);
        Operand op1 = current_state.pop_I();
        Operand op0 = current_state.pop_I();
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(target).id];
        ConditionOperand cond = new ConditionOperand(op);
        Quad q = IntIfCmp.create(quad_cfg.getNewQuadID(), IntIfCmp.IFCMP_I.INSTANCE, op0, op1, cond, new TargetOperand(target_bb));
        appendQuad(q);
    }
    public void visitIFREFCMP(byte op, int target) {
        super.visitIFREFCMP(op, target);
        // could be A or R
        Operand op1 = current_state.pop();
        // could be A or R
        Operand op0 = current_state.pop();
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(target).id];
        ConditionOperand cond = new ConditionOperand(op);
        jq_Type t1 = getTypeOf(op1);
        jq_Type t0 = getTypeOf(op0);
        IntIfCmp operator;
        if (t1.isAddressType()) {
            if (!t0.isAddressType() && t0 != jq_Reference.jq_NullType.NULL_TYPE) {
                Assert.UNREACHABLE("comparing address type "+op1+" with non-address type "+op0);
            }
            operator = IntIfCmp.IFCMP_P.INSTANCE;
        } else if (t0.isAddressType()) {
            if (t1 != jq_Reference.jq_NullType.NULL_TYPE) {
                Assert.UNREACHABLE("comparing address type "+op0+" with non-address type "+op1);
            }
            operator = IntIfCmp.IFCMP_P.INSTANCE;
        } else {
            operator = IntIfCmp.IFCMP_A.INSTANCE;
        }
        Quad q = IntIfCmp.create(quad_cfg.getNewQuadID(), operator, op0, op1, cond, new TargetOperand(target_bb));
        appendQuad(q);
    }
    public void visitGOTO(int target) {
        super.visitGOTO(target);
        this.uncond_branch = true;
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(target).id];
        Quad q = Goto.create(quad_cfg.getNewQuadID(), Goto.GOTO.INSTANCE, new TargetOperand(target_bb));
        appendQuad(q);
    }
    java.util.Map jsr_states = new java.util.HashMap();
    void setJSRState(joeq.Compiler.BytecodeAnalysis.BasicBlock bb, AbstractState s) {
        jsr_states.put(bb, s.copyAfterJSR());
    }
    AbstractState getJSRState(joeq.Compiler.BytecodeAnalysis.BasicBlock bb) {
        return (AbstractState)jsr_states.get(bb);
    }
    public void visitJSR(int target) {
        super.visitJSR(target);
        this.uncond_branch = true;
        joeq.Compiler.BytecodeAnalysis.BasicBlock target_bcbb = bc_cfg.getBasicBlockByBytecodeIndex(target);
        BasicBlock target_bb = quad_bbs[target_bcbb.id];
        BasicBlock successor_bb = quad_bbs[bc_bb.id+1];
        joeq.Compiler.BytecodeAnalysis.JSRInfo jsrinfo = bc_cfg.getJSRInfo(target_bcbb);
        if (jsrinfo == null) {
            if (TRACE) out.println("jsr with no ret! converting to GOTO.");
            // push a null constant in place of the return address,
            // in case the return address is stored into a local variable.
            current_state.push_A(new AConstOperand(null));
            saveStackIntoRegisters();
            Quad q = Goto.create(quad_cfg.getNewQuadID(), Goto.GOTO.INSTANCE, new TargetOperand(target_bb));
            appendQuad(q);
            return;
        }
        Assert._assert(quad_bbs[jsrinfo.entry_block.id] == target_bb);
        BasicBlock last_bb = quad_bbs[jsrinfo.exit_block.id];
        JSRInfo q_jsrinfo = new JSRInfo(target_bb, last_bb, jsrinfo.changedLocals);
        this.quad_cfg.addJSRInfo(q_jsrinfo);
        saveStackIntoRegisters();
        RegisterOperand op0 = getStackRegister(jq_ReturnAddressType.INSTANCE);
        Quad q = Jsr.create(quad_cfg.getNewQuadID(), Jsr.JSR.INSTANCE, op0, new TargetOperand(target_bb), new TargetOperand(successor_bb));
        appendQuad(q);
        joeq.Compiler.BytecodeAnalysis.BasicBlock next_bb = bc_cfg.getBasicBlock(bc_bb.id+1);
        joeq.Compiler.BytecodeAnalysis.BasicBlock ret_bb = jsrinfo.exit_block;
        setJSRState(next_bb, current_state);
        // we need to visit the ret block even when it has been visited before,
        // so that when we visit its ret instruction, next_bb will get updated
        // with the new jsr state
        if (visited[ret_bb.id]) {
            if (TRACE) out.println("marking ret bb "+ret_bb+" for regeneration.");
            if (!regenerate.contains(ret_bb)) {
                regenerate.add(ret_bb);
                start_states[ret_bb.id].rebuildStack();
            }
            visited[ret_bb.id] = false;
        }
        current_state.push(op0.copy());
    }
    public void visitRET(int i) {
        super.visitRET(i);
        this.uncond_branch = true;
        saveStackIntoRegisters();
        RegisterOperand op0 = makeLocal(i, jq_ReturnAddressType.INSTANCE);
        Quad q = Ret.create(quad_cfg.getNewQuadID(), Ret.RET.INSTANCE, op0);
        appendQuad(q);
        current_state.setLocal(i, null);
        endsWithRET = true;
        // get JSR info
        joeq.Compiler.BytecodeAnalysis.JSRInfo jsrinfo = bc_cfg.getJSRInfo(bc_bb);
        // find all callers to this subroutine.
        for (int j=0; j<bc_bb.getNumberOfSuccessors(); ++j) {
            joeq.Compiler.BytecodeAnalysis.BasicBlock caller_next = bc_bb.getSuccessor(j);
            AbstractState caller_state = getJSRState(caller_next);
            if (caller_state == null) {
                if (TRACE) out.println("haven't seen jsr call from "+caller_next+" yet.");
                Assert._assert(!visited[caller_next.id]);
                if (!regenerate.contains(caller_next)) {
                    regenerate.add(caller_next);
                }
                continue;
            }
            caller_state.mergeAfterJSR(jsrinfo.changedLocals, current_state);
            if (start_states[caller_next.id] == null) {
                if (TRACE) out.println("Copying jsr state to "+caller_next);
                start_states[caller_next.id] = caller_state.copy();
                if (visited[caller_next.id]) {
                    if (TRACE) out.println("must regenerate code for "+caller_next);
                    if (!regenerate.contains(caller_next)) {
                        regenerate.add(caller_next);
                        start_states[caller_next.id].rebuildStack();
                    }
                    visited[caller_next.id] = false;
                }
            } else {
                if (TRACE) out.println("Merging jsr state with "+caller_next);
                if (start_states[caller_next.id].merge(caller_state, rf)) {
                    if (TRACE) out.println("in set of "+caller_next+" changed");
                    if (visited[caller_next.id]) {
                        if (TRACE) out.println("must regenerate code for "+caller_next);
                        if (!regenerate.contains(caller_next)) {
                            regenerate.add(caller_next);
                            start_states[caller_next.id].rebuildStack();
                        }
                        visited[caller_next.id] = false;
                    }
                }
            }
        }
    }
    public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
        super.visitTABLESWITCH(default_target, low, high, targets);
        this.uncond_branch = true;
        Operand op0 = current_state.pop_I();
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(default_target).id];
        Assert._assert(high-low+1 == targets.length);
        Quad q = TableSwitch.create(quad_cfg.getNewQuadID(), TableSwitch.TABLESWITCH.INSTANCE, op0, new IConstOperand(low),
                                    new TargetOperand(target_bb), targets.length);
        for (int i = 0; i < targets.length; ++i) {
            target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(targets[i]).id];
            TableSwitch.setTarget(q, i, target_bb);
        }
        appendQuad(q);
    }
    public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
        super.visitLOOKUPSWITCH(default_target, values, targets);
        this.uncond_branch = true;
        Operand op0 = current_state.pop_I();
        saveStackIntoRegisters();
        BasicBlock target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(default_target).id];
        Quad q = LookupSwitch.create(quad_cfg.getNewQuadID(), LookupSwitch.LOOKUPSWITCH.INSTANCE, op0, new TargetOperand(target_bb), values.length);
        for (int i = 0; i < values.length; ++i) {
            LookupSwitch.setMatch(q, i, values[i]);
            target_bb = quad_bbs[bc_cfg.getBasicBlockByBytecodeIndex(targets[i]).id];
            LookupSwitch.setTarget(q, i, target_bb);
        }
        appendQuad(q);
    }
    public void visitIRETURN() {
        super.visitIRETURN();
        this.uncond_branch = true;
        Operand op0 = current_state.pop_I();
        Quad q = Return.create(quad_cfg.getNewQuadID(), Return.RETURN_I.INSTANCE, op0);
        appendQuad(q);
        current_state.clearStack();
    }
    public void visitLRETURN() {
        super.visitLRETURN();
        this.uncond_branch = true;
        Operand op0 = current_state.pop_L();
        Quad q = Return.create(quad_cfg.getNewQuadID(), Return.RETURN_L.INSTANCE, op0);
        appendQuad(q);
        current_state.clearStack();
    }
    public void visitFRETURN() {
        super.visitFRETURN();
        this.uncond_branch = true;
        Operand op0 = current_state.pop_F();
        Quad q = Return.create(quad_cfg.getNewQuadID(), Return.RETURN_F.INSTANCE, op0);
        appendQuad(q);
        current_state.clearStack();
    }
    public void visitDRETURN() {
        super.visitDRETURN();
        this.uncond_branch = true;
        Operand op0 = current_state.pop_D();
        Quad q = Return.create(quad_cfg.getNewQuadID(), Return.RETURN_D.INSTANCE, op0);
        appendQuad(q);
        current_state.clearStack();
    }
    public void visitARETURN() {
        super.visitARETURN();
        this.uncond_branch = true;
        // could be A or R
        Operand op0 = current_state.pop();
        jq_Type t = getTypeOf(op0);
        Return operator;
        if (method.getReturnType().isAddressType()) {
            operator = Return.RETURN_P.INSTANCE;
            Assert._assert(t.isAddressType() ||
                      t == jq_Reference.jq_NullType.NULL_TYPE ||
                      state.isSubtype(t, Address._class) != NO, t.toString());
        } else {
            operator = t.isAddressType()?(Return)Return.RETURN_P.INSTANCE:Return.RETURN_A.INSTANCE;
        }
        Quad q = Return.create(quad_cfg.getNewQuadID(), operator, op0);
        appendQuad(q);
        current_state.clearStack();
    }
    public void visitVRETURN() {
        super.visitVRETURN();
        this.uncond_branch = true;
        Quad q = Return.create(quad_cfg.getNewQuadID(), Return.RETURN_V.INSTANCE);
        appendQuad(q);
        current_state.clearStack();
    }
    private void GETSTATIChelper(jq_StaticField f, Getstatic oper1, Getstatic oper2) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Getstatic operator = dynlink?oper1:oper2;
        jq_Type t = f.getType();
        RegisterOperand op0 = getStackRegister(t);
        Quad q = Getstatic.create(quad_cfg.getNewQuadID(), operator, op0, new FieldOperand(f));
        appendQuad(q);
        current_state.push(op0.copy(), t);
    }
    public void visitIGETSTATIC(jq_StaticField f) {
        super.visitIGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_I_DYNLINK.INSTANCE, Getstatic.GETSTATIC_I.INSTANCE);
    }
    public void visitLGETSTATIC(jq_StaticField f) {
        super.visitLGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_L_DYNLINK.INSTANCE, Getstatic.GETSTATIC_L.INSTANCE);
    }
    public void visitFGETSTATIC(jq_StaticField f) {
        super.visitFGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_F_DYNLINK.INSTANCE, Getstatic.GETSTATIC_F.INSTANCE);
    }
    public void visitDGETSTATIC(jq_StaticField f) {
        super.visitDGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_D_DYNLINK.INSTANCE, Getstatic.GETSTATIC_D.INSTANCE);
    }
    public void visitAGETSTATIC(jq_StaticField f) {
        super.visitAGETSTATIC(f);
        Getstatic operator1 = f.getType().isAddressType()?(Getstatic)Getstatic.GETSTATIC_P_DYNLINK.INSTANCE:Getstatic.GETSTATIC_A_DYNLINK.INSTANCE;
        Getstatic operator2 = f.getType().isAddressType()?(Getstatic)Getstatic.GETSTATIC_P.INSTANCE:Getstatic.GETSTATIC_A.INSTANCE;
        GETSTATIChelper(f, operator1, operator2);
    }
    public void visitZGETSTATIC(jq_StaticField f) {
        super.visitZGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_Z_DYNLINK.INSTANCE, Getstatic.GETSTATIC_Z.INSTANCE);
    }
    public void visitBGETSTATIC(jq_StaticField f) {
        super.visitBGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_B_DYNLINK.INSTANCE, Getstatic.GETSTATIC_B.INSTANCE);
    }
    public void visitCGETSTATIC(jq_StaticField f) {
        super.visitCGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_C_DYNLINK.INSTANCE, Getstatic.GETSTATIC_C.INSTANCE);
    }
    public void visitSGETSTATIC(jq_StaticField f) {
        super.visitSGETSTATIC(f);
        GETSTATIChelper(f, Getstatic.GETSTATIC_S_DYNLINK.INSTANCE, Getstatic.GETSTATIC_S.INSTANCE);
    }
    private void PUTSTATIChelper(jq_StaticField f, Putstatic oper1, Putstatic oper2) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Putstatic operator = dynlink?oper1:oper2;
        jq_Type t = f.getType();
        Operand op0 = current_state.pop(t);
        Quad q = Putstatic.create(quad_cfg.getNewQuadID(), operator, op0, new FieldOperand(f));
        appendQuad(q);
    }
    public void visitIPUTSTATIC(jq_StaticField f) {
        super.visitIPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_I_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_I.INSTANCE);
    }
    public void visitLPUTSTATIC(jq_StaticField f) {
        super.visitLPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_L_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_L.INSTANCE);
    }
    public void visitFPUTSTATIC(jq_StaticField f) {
        super.visitFPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_F_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_F.INSTANCE);
    }
    public void visitDPUTSTATIC(jq_StaticField f) {
        super.visitDPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_D_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_D.INSTANCE);
    }
    public void visitAPUTSTATIC(jq_StaticField f) {
        super.visitAPUTSTATIC(f);
        Putstatic operator1 = f.getType().isAddressType()?(Putstatic)Putstatic.PUTSTATIC_P_DYNLINK.INSTANCE:Putstatic.PUTSTATIC_A_DYNLINK.INSTANCE;
        Putstatic operator2 = f.getType().isAddressType()?(Putstatic)Putstatic.PUTSTATIC_P.INSTANCE:Putstatic.PUTSTATIC_A.INSTANCE;
        PUTSTATIChelper(f, operator1, operator2);
    }
    public void visitZPUTSTATIC(jq_StaticField f) {
        super.visitZPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_Z_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_Z.INSTANCE);
    }
    public void visitBPUTSTATIC(jq_StaticField f) {
        super.visitBPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_B_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_B.INSTANCE);
    }
    public void visitCPUTSTATIC(jq_StaticField f) {
        super.visitCPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_C_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_C.INSTANCE);
    }
    public void visitSPUTSTATIC(jq_StaticField f) {
        super.visitSPUTSTATIC(f);
        PUTSTATIChelper(f, Putstatic.PUTSTATIC_S_DYNLINK.INSTANCE, Putstatic.PUTSTATIC_S.INSTANCE);
    }
    private void GETFIELDhelper(jq_InstanceField f, Getfield oper1, Getfield oper2) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Operand op1 = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(op1)) {
            if (TRACE) System.out.println("Null check triggered on "+op1);
            return;
        }
        jq_Type t = f.getType();
        RegisterOperand op0 = getStackRegister(t);
        Getfield operator = dynlink?oper1:oper2;
        Quad q = Getfield.create(quad_cfg.getNewQuadID(), operator, op0, op1, new FieldOperand(f), getCurrentGuard());
        appendQuad(q);
        current_state.push(op0.copy(), t);
    }
    public void visitIGETFIELD(jq_InstanceField f) {
        super.visitIGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_I_DYNLINK.INSTANCE, Getfield.GETFIELD_I.INSTANCE);
    }
    public void visitLGETFIELD(jq_InstanceField f) {
        super.visitLGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_L_DYNLINK.INSTANCE, Getfield.GETFIELD_L.INSTANCE);
    }
    public void visitFGETFIELD(jq_InstanceField f) {
        super.visitFGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_F_DYNLINK.INSTANCE, Getfield.GETFIELD_F.INSTANCE);
    }
    public void visitDGETFIELD(jq_InstanceField f) {
        super.visitDGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_D_DYNLINK.INSTANCE, Getfield.GETFIELD_D.INSTANCE);
    }
    public void visitAGETFIELD(jq_InstanceField f) {
        super.visitAGETFIELD(f);
        Getfield operator1 = f.getType().isAddressType()?(Getfield)Getfield.GETFIELD_P_DYNLINK.INSTANCE:Getfield.GETFIELD_A_DYNLINK.INSTANCE;
        Getfield operator2 = f.getType().isAddressType()?(Getfield)Getfield.GETFIELD_P.INSTANCE:Getfield.GETFIELD_A.INSTANCE;
        GETFIELDhelper(f, operator1, operator2);
    }
    public void visitBGETFIELD(jq_InstanceField f) {
        super.visitBGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_B_DYNLINK.INSTANCE, Getfield.GETFIELD_B.INSTANCE);
    }
    public void visitCGETFIELD(jq_InstanceField f) {
        super.visitCGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_C_DYNLINK.INSTANCE, Getfield.GETFIELD_C.INSTANCE);
    }
    public void visitSGETFIELD(jq_InstanceField f) {
        super.visitSGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_S_DYNLINK.INSTANCE, Getfield.GETFIELD_S.INSTANCE);
    }
    public void visitZGETFIELD(jq_InstanceField f) {
        super.visitZGETFIELD(f);
        GETFIELDhelper(f, Getfield.GETFIELD_Z_DYNLINK.INSTANCE, Getfield.GETFIELD_Z.INSTANCE);
    }
    private void PUTFIELDhelper(jq_InstanceField f, Putfield oper1, Putfield oper2) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Operand op0 = current_state.pop(f.getType());
        Operand op1 = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(op1)) {    
            if (TRACE) System.out.println("Null check triggered on "+op1);
            return;
        }
        Putfield operator = dynlink?oper1:oper2;
        Quad q = Putfield.create(quad_cfg.getNewQuadID(), operator, op1, new FieldOperand(f), op0, getCurrentGuard());
        appendQuad(q);
    }
    public void visitIPUTFIELD(jq_InstanceField f) {
        super.visitIPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_I_DYNLINK.INSTANCE, Putfield.PUTFIELD_I.INSTANCE);
    }
    public void visitLPUTFIELD(jq_InstanceField f) {
        super.visitLPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_L_DYNLINK.INSTANCE, Putfield.PUTFIELD_L.INSTANCE);
    }
    public void visitFPUTFIELD(jq_InstanceField f) {
        super.visitFPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_F_DYNLINK.INSTANCE, Putfield.PUTFIELD_F.INSTANCE);
    }
    public void visitDPUTFIELD(jq_InstanceField f) {
        super.visitDPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_D_DYNLINK.INSTANCE, Putfield.PUTFIELD_D.INSTANCE);
    }
    public void visitAPUTFIELD(jq_InstanceField f) {
        super.visitAPUTFIELD(f);
        Putfield operator1 = f.getType().isAddressType()?(Putfield)Putfield.PUTFIELD_P_DYNLINK.INSTANCE:Putfield.PUTFIELD_A_DYNLINK.INSTANCE;
        Putfield operator2 = f.getType().isAddressType()?(Putfield)Putfield.PUTFIELD_P.INSTANCE:Putfield.PUTFIELD_A.INSTANCE;
        PUTFIELDhelper(f, operator1, operator2);
    }
    public void visitBPUTFIELD(jq_InstanceField f) {
        super.visitBPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_B_DYNLINK.INSTANCE, Putfield.PUTFIELD_B.INSTANCE);
    }
    public void visitCPUTFIELD(jq_InstanceField f) {
        super.visitCPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_C_DYNLINK.INSTANCE, Putfield.PUTFIELD_C.INSTANCE);
    }
    public void visitSPUTFIELD(jq_InstanceField f) {
        super.visitSPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_S_DYNLINK.INSTANCE, Putfield.PUTFIELD_S.INSTANCE);
    }
    public void visitZPUTFIELD(jq_InstanceField f) {
        super.visitZPUTFIELD(f);
        PUTFIELDhelper(f, Putfield.PUTFIELD_Z_DYNLINK.INSTANCE, Putfield.PUTFIELD_Z.INSTANCE);
    }
    public static final Utf8 peek = Utf8.get("peek");
    public static final Utf8 peek1 = Utf8.get("peek1");
    public static final Utf8 peek2 = Utf8.get("peek2");
    public static final Utf8 peek4 = Utf8.get("peek4");
    public static final Utf8 peek8 = Utf8.get("peek8");
    public static final Utf8 poke = Utf8.get("poke");
    public static final Utf8 poke1 = Utf8.get("poke1");
    public static final Utf8 poke2 = Utf8.get("poke2");
    public static final Utf8 poke4 = Utf8.get("poke4");
    public static final Utf8 poke8 = Utf8.get("poke8");
    public static final Utf8 offset = Utf8.get("offset");
    public static final Utf8 align = Utf8.get("align");
    public static final Utf8 difference = Utf8.get("difference");
    public static final Utf8 isNull = Utf8.get("isNull");
    public static final Utf8 addressOf = Utf8.get("addressOf");
    public static final Utf8 address32 = Utf8.get("address32");
    public static final Utf8 asObject = Utf8.get("asObject");
    public static final Utf8 asReferenceType = Utf8.get("asReferenceType");
    public static final Utf8 to32BitValue = Utf8.get("to32BitValue");
    public static final Utf8 stringRep = Utf8.get("stringRep");
    public static final Utf8 getNull = Utf8.get("getNull");
    public static final Utf8 size = Utf8.get("size");
    public static final Utf8 getBasePointer = Utf8.get("getBasePointer");
    public static final Utf8 getStackPointer = Utf8.get("getStackPointer");
    public static final Utf8 alloca = Utf8.get("alloca");
    public static final Utf8 atomicAdd = Utf8.get("atomicAdd");
    public static final Utf8 atomicSub = Utf8.get("atomicSub");
    public static final Utf8 atomicCas4 = Utf8.get("atomicCas4");
    public static final Utf8 atomicCas8 = Utf8.get("atomicCas8");
    public static final Utf8 atomicAnd = Utf8.get("atomicAnd");
    public static final Utf8 min = Utf8.get("min");
    public static final Utf8 max = Utf8.get("max");
    private void ADDRESShelper(jq_Method m, Invoke oper) {
        Utf8 name = m.getName();
        Quad q;
        if (name == poke) {
            Operand val = current_state.pop_P();
            Operand loc = current_state.pop_P();
            q = MemStore.create(quad_cfg.getNewQuadID(), MemStore.POKE_P.INSTANCE, loc, val);
        } else if (name == poke1) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            q = MemStore.create(quad_cfg.getNewQuadID(), MemStore.POKE_1.INSTANCE, loc, val);
        } else if (name == poke2) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            q = MemStore.create(quad_cfg.getNewQuadID(), MemStore.POKE_2.INSTANCE, loc, val);
        } else if (name == poke4) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            q = MemStore.create(quad_cfg.getNewQuadID(), MemStore.POKE_4.INSTANCE, loc, val);
        } else if (name == poke8) {
            Operand val = current_state.pop_L();
            Operand loc = current_state.pop_P();
            q = MemStore.create(quad_cfg.getNewQuadID(), MemStore.POKE_8.INSTANCE, loc, val);
        } else if (name == peek) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(Address._class);
            q = MemLoad.create(quad_cfg.getNewQuadID(), MemLoad.PEEK_P.INSTANCE, res, loc);
            current_state.push_P(res.copy());
        } else if (name == peek1) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.BYTE);
            q = MemLoad.create(quad_cfg.getNewQuadID(), MemLoad.PEEK_1.INSTANCE, res, loc);
            current_state.push_I(res.copy());
        } else if (name == peek2) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.SHORT);
            q = MemLoad.create(quad_cfg.getNewQuadID(), MemLoad.PEEK_2.INSTANCE, res, loc);
            current_state.push_I(res.copy());
        } else if (name == peek4) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.INT);
            q = MemLoad.create(quad_cfg.getNewQuadID(), MemLoad.PEEK_4.INSTANCE, res, loc);
            current_state.push_I(res.copy());
        } else if (name == peek8) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.LONG);
            q = MemLoad.create(quad_cfg.getNewQuadID(), MemLoad.PEEK_8.INSTANCE, res, loc);
            current_state.push_L(res.copy());
        } else if (name == offset) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(Address._class);
            q = Binary.create(quad_cfg.getNewQuadID(), Binary.ADD_P.INSTANCE, res, loc, val);
            current_state.push_P(res.copy());
        } else if (name == align) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(Address._class);
            q = Binary.create(quad_cfg.getNewQuadID(), Binary.ALIGN_P.INSTANCE, res, loc, val);
            current_state.push_P(res.copy());
        } else if (name == difference) {
            Operand val = current_state.pop_P();
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.INT);
            q = Binary.create(quad_cfg.getNewQuadID(), Binary.SUB_P.INSTANCE, res, loc, val);
            current_state.push_I(res.copy());
        } else if (name == alloca) {
            Operand amt = current_state.pop_I();
            RegisterOperand res = getStackRegister(StackAddress._class);
            q = Special.create(quad_cfg.getNewQuadID(), Special.ALLOCA.INSTANCE, res, amt);
            current_state.push_P(res.copy());
        } else if (name == isNull) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.BOOLEAN);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.ISNULL_P.INSTANCE, res, loc);
            current_state.push_I(res.copy());
        } else if (name == addressOf) {
            Operand loc = current_state.pop_A();
            RegisterOperand res = getStackRegister(Address._class);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.OBJECT_2ADDRESS.INSTANCE, res, loc);
            current_state.push_P(res.copy());
        } else if (name == address32) {
            Operand loc = current_state.pop_I();
            RegisterOperand res = getStackRegister(Address._class);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.INT_2ADDRESS.INSTANCE, res, loc);
            current_state.push_P(res.copy());
        } else if (name == asObject) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(PrimordialClassLoader.getJavaLangObject());
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.ADDRESS_2OBJECT.INSTANCE, res, loc);
            current_state.push_A(res.copy());
        } else if (name == asReferenceType) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Reference._class);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.ADDRESS_2OBJECT.INSTANCE, res, loc);
            current_state.push_A(res.copy());
        } else if (name == to32BitValue) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.INT);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.ADDRESS_2INT.INSTANCE, res, loc);
            current_state.push_I(res.copy());
        } else if (name == stringRep) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.INT);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.ADDRESS_2INT.INSTANCE, res, loc);
            current_state.push_I(res.copy());
            appendQuad(q);
            
            jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljwutil/strings/Strings;");
            jq_StaticMethod sm = k.getOrCreateStaticMethod("hex8", "(I)Ljava/lang/String;");
            INVOKEhelper(Invoke.INVOKESTATIC_A.INSTANCE, sm, sm.getReturnType(), false);
            return;
        } else if (name == getNull) {
            PConstOperand p = new PConstOperand(null);
            current_state.push_P(p);
            return;
        } else if (name == size) {
            IConstOperand p = new IConstOperand(HeapAddress.size());
            current_state.push_I(p);
            return;
        } else if (name == getBasePointer) {
            RegisterOperand res = getStackRegister(StackAddress._class);
            q = Special.create(quad_cfg.getNewQuadID(), Special.GET_BASE_POINTER.INSTANCE, res);
            current_state.push_P(res.copy());
        } else if (name == getStackPointer) {
            RegisterOperand res = getStackRegister(StackAddress._class);
            q = Special.create(quad_cfg.getNewQuadID(), Special.GET_STACK_POINTER.INSTANCE, res);
            current_state.push_P(res.copy());
        } else if (name == atomicAdd) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            q = Special.create(quad_cfg.getNewQuadID(), Special.ATOMICADD_I.INSTANCE, loc, val);
        } else if (name == atomicSub) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            q = Special.create(quad_cfg.getNewQuadID(), Special.ATOMICSUB_I.INSTANCE, loc, val);
        } else if (name == atomicAnd) {
            Operand val = current_state.pop_I();
            Operand loc = current_state.pop_P();
            q = Special.create(quad_cfg.getNewQuadID(), Special.ATOMICAND_I.INSTANCE, loc, val);
        } else if (name == atomicCas4) {
            Operand val2 = current_state.pop_I();
            Operand val1 = current_state.pop_I();
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.INT);
            q = Special.create(quad_cfg.getNewQuadID(), Special.ATOMICCAS4.INSTANCE, res, loc, val1, val2);
            current_state.push_I(res.copy());
        } else if (name == atomicCas8) {
            Operand val2 = current_state.pop_L();
            Operand val1 = current_state.pop_L();
            Operand loc = current_state.pop_P();
            RegisterOperand res = getStackRegister(jq_Primitive.LONG);
            q = Special.create(quad_cfg.getNewQuadID(), Special.ATOMICCAS8.INSTANCE, res, loc, val1, val2);
            current_state.push_L(res.copy());
        } else {
            // TODO
            INVOKEhelper(oper, m, m.getReturnType(), false);
            return;
        }
        appendQuad(q);
        mergeStateWithAllExHandlers(false);
    }
    private void UNSAFEhelper(jq_Method m, Invoke oper) {
        if (_unsafe.handleMethod(this, quad_cfg, current_state, m, oper)) {
            mergeStateWithAllExHandlers(false);
            if (_unsafe.endsBB(m)) {
                endBasicBlock = true;
            }
        } else {
            // TODO
            INVOKEhelper(oper, m, m.getReturnType(), false);
            return;
        }
    }
    private void INVOKEhelper(Invoke oper, jq_Method f, jq_Type returnType, boolean instance_call) {
        jq_Type[] paramTypes = f.getParamTypes();
        RegisterOperand result;
        if (returnType == jq_Primitive.VOID) result = null;
        else result = getStackRegister(returnType, f.getParamWords()-1);
        Quad q = Invoke.create(quad_cfg.getNewQuadID(), oper, result, new MethodOperand(f), paramTypes.length);
        Operand op = null;
        for (int i = paramTypes.length; --i >= 0; ) {
            jq_Type ptype = paramTypes[i];
            op = current_state.pop(ptype);
            RegisterOperand rop;
            if (op instanceof RegisterOperand) rop = (RegisterOperand)op;
            else {
                rop = getStackRegister(ptype);
                Quad q2 = Move.create(quad_cfg.getNewQuadID(), Move.getMoveOp(ptype), (RegisterOperand) rop.copy(), op);
                appendQuad(q2);
            }
            Invoke.setParam(q, i, rop);
        }
        clearCurrentGuard();
        if (instance_call && performNullCheck(op)) {
            if (TRACE) System.out.println("Null check triggered on "+op);
            return;
        }
        appendQuad(q);
        mergeStateWithAllExHandlers(false);
        if (result != null) current_state.push(result.copy(), returnType);
    }
    public void visitIINVOKE(byte op, jq_Method f) {
        super.visitIINVOKE(op, f);
        if (_unsafe.isUnsafe(f)) {
            UNSAFEhelper(f, Invoke.INVOKESTATIC_I.INSTANCE);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            ADDRESShelper(f, f.isStatic()?(Invoke)Invoke.INVOKESTATIC_I.INSTANCE:Invoke.INVOKEVIRTUAL_I.INSTANCE);
            return;
        }
        f = (jq_Method) tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Invoke oper;
        boolean instance_call;
        switch (op) {
            case INVOKE_VIRTUAL:
                instance_call = true;
                if (dynlink)
                    oper = Invoke.INVOKEVIRTUAL_I_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKEVIRTUAL_I.INSTANCE;
                }
                //f.getDeclaringClass().load();
                //jq.Assert(!f.getDeclaringClass().isInterface());
                break;
            case INVOKE_STATIC:
                instance_call = false;
                if (dynlink)
                    oper = Invoke.INVOKESTATIC_I_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKESTATIC_I.INSTANCE;
                }
                break;
            case INVOKE_SPECIAL:
                instance_call = true;
                Assert._assert(f instanceof jq_InstanceMethod);
                if (dynlink)
                    oper = Invoke.INVOKESPECIAL_I_DYNLINK.INSTANCE;
                else {
                    f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                    oper = Invoke.INVOKESTATIC_I.INSTANCE;
                }
                break;
            case INVOKE_INTERFACE:
                instance_call = true;
                oper = Invoke.INVOKEINTERFACE_I.INSTANCE;
                break;
            default:
                throw new InternalError();
        }
        INVOKEhelper(oper, f, jq_Primitive.INT, instance_call);
    }
    public void visitLINVOKE(byte op, jq_Method f) {
        super.visitLINVOKE(op, f);
        if (_unsafe.isUnsafe(f)) {
            UNSAFEhelper(f, Invoke.INVOKESTATIC_L.INSTANCE);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            ADDRESShelper(f, f.isStatic()?(Invoke)Invoke.INVOKESTATIC_L.INSTANCE:Invoke.INVOKEVIRTUAL_L.INSTANCE);
            return;
        }
        f = (jq_Method) tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Invoke oper;
        boolean instance_call;
        switch (op) {
            case INVOKE_VIRTUAL:
                instance_call = true;
                if (dynlink)
                    oper = Invoke.INVOKEVIRTUAL_L_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKEVIRTUAL_L.INSTANCE;
                }
                break;
            case INVOKE_STATIC:
                instance_call = false;
                if (dynlink)
                    oper = Invoke.INVOKESTATIC_L_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKESTATIC_L.INSTANCE;
                }
                break;
            case INVOKE_SPECIAL:
                instance_call = true;
                Assert._assert(f instanceof jq_InstanceMethod);
                if (dynlink)
                    oper = Invoke.INVOKESPECIAL_L_DYNLINK.INSTANCE;
                else {
                    f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                    oper = Invoke.INVOKESTATIC_L.INSTANCE;
                }
                break;
            case INVOKE_INTERFACE:
                instance_call = true;
                oper = Invoke.INVOKEINTERFACE_L.INSTANCE;
                break;
            default:
                throw new InternalError();
        }
        INVOKEhelper(oper, f, jq_Primitive.LONG, instance_call);
    }
    public void visitFINVOKE(byte op, jq_Method f) {
        super.visitFINVOKE(op, f);
        if (_unsafe.isUnsafe(f)) {
            UNSAFEhelper(f, Invoke.INVOKESTATIC_F.INSTANCE);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            ADDRESShelper(f, f.isStatic()?(Invoke)Invoke.INVOKESTATIC_F.INSTANCE:Invoke.INVOKEVIRTUAL_F.INSTANCE);
            return;
        }
        f = (jq_Method) tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Invoke oper;
        boolean instance_call;
        switch (op) {
            case INVOKE_VIRTUAL:
                instance_call = true;
                if (dynlink)
                    oper = Invoke.INVOKEVIRTUAL_F_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKEVIRTUAL_F.INSTANCE;
                }
                break;
            case INVOKE_STATIC:
                instance_call = false;
                if (dynlink)
                    oper = Invoke.INVOKESTATIC_F_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKESTATIC_F.INSTANCE;
                }
                break;
            case INVOKE_SPECIAL:
                instance_call = true;
                Assert._assert(f instanceof jq_InstanceMethod);
                if (dynlink)
                    oper = Invoke.INVOKESPECIAL_F_DYNLINK.INSTANCE;
                else {
                    f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                    oper = Invoke.INVOKESTATIC_F.INSTANCE;
                }
                break;
            case INVOKE_INTERFACE:
                instance_call = true;
                oper = Invoke.INVOKEINTERFACE_F.INSTANCE;
                break;
            default:
                throw new InternalError();
        }
        INVOKEhelper(oper, f, jq_Primitive.FLOAT, instance_call);
    }
    public void visitDINVOKE(byte op, jq_Method f) {
        super.visitDINVOKE(op, f);
        if (_unsafe.isUnsafe(f)) {
            UNSAFEhelper(f, Invoke.INVOKESTATIC_D.INSTANCE);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            ADDRESShelper(f, f.isStatic()?(Invoke)Invoke.INVOKESTATIC_D.INSTANCE:Invoke.INVOKEVIRTUAL_D.INSTANCE);
            return;
        }
        f = (jq_Method) tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Invoke oper;
        boolean instance_call;
        switch (op) {
            case INVOKE_VIRTUAL:
                instance_call = true;
                if (dynlink)
                    oper = Invoke.INVOKEVIRTUAL_D_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKEVIRTUAL_D.INSTANCE;
                }
                break;
            case INVOKE_STATIC:
                instance_call = false;
                if (dynlink)
                    oper = Invoke.INVOKESTATIC_D_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKESTATIC_D.INSTANCE;
                }
                break;
            case INVOKE_SPECIAL:
                instance_call = true;
                Assert._assert(f instanceof jq_InstanceMethod);
                if (dynlink)
                    oper = Invoke.INVOKESPECIAL_D_DYNLINK.INSTANCE;
                else {
                    f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                    oper = Invoke.INVOKESTATIC_D.INSTANCE;
                }
                break;
            case INVOKE_INTERFACE:
                instance_call = true;
                oper = Invoke.INVOKEINTERFACE_D.INSTANCE;
                break;
            default:
                throw new InternalError();
        }
        INVOKEhelper(oper, f, jq_Primitive.DOUBLE, instance_call);
    }
    public void visitAINVOKE(byte op, jq_Method f) {
        super.visitAINVOKE(op, f);
        if (_unsafe.isUnsafe(f)) {
            UNSAFEhelper(f, f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKESTATIC_P.INSTANCE:Invoke.INVOKESTATIC_A.INSTANCE);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            Invoke oper;
            if (f.isStatic()) {
                oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKESTATIC_P.INSTANCE:Invoke.INVOKESTATIC_A.INSTANCE;
            } else {
                oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKEVIRTUAL_P.INSTANCE:Invoke.INVOKEVIRTUAL_A.INSTANCE;
            }
            ADDRESShelper(f, oper);
            return;
        }
        f = (jq_Method) tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Invoke oper;
        boolean instance_call;
        switch (op) {
            case INVOKE_VIRTUAL:
                instance_call = true;
                if (dynlink)
                    oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKEVIRTUAL_P_DYNLINK.INSTANCE:Invoke.INVOKEVIRTUAL_A_DYNLINK.INSTANCE;
                else {
                    oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKEVIRTUAL_P.INSTANCE:Invoke.INVOKEVIRTUAL_A.INSTANCE;
                }
                break;
            case INVOKE_STATIC:
                instance_call = false;
                if (dynlink)
                    oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKESTATIC_P_DYNLINK.INSTANCE:Invoke.INVOKESTATIC_A_DYNLINK.INSTANCE;
                else {
                    oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKESTATIC_P.INSTANCE:Invoke.INVOKESTATIC_A.INSTANCE;
                }
                break;
            case INVOKE_SPECIAL:
                instance_call = true;
                Assert._assert(f instanceof jq_InstanceMethod);
                if (dynlink)
                    oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKESPECIAL_P_DYNLINK.INSTANCE:Invoke.INVOKESPECIAL_A_DYNLINK.INSTANCE;
                else {
                    f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                    oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKESTATIC_P.INSTANCE:Invoke.INVOKESTATIC_A.INSTANCE;
                }
                break;
            case INVOKE_INTERFACE:
                instance_call = true;
                oper = f.getReturnType().isAddressType()?(Invoke)Invoke.INVOKEINTERFACE_P.INSTANCE:Invoke.INVOKEINTERFACE_A.INSTANCE;
                break;
            default:
                throw new InternalError();
        }
        INVOKEhelper(oper, f, f.getReturnType(), instance_call);
    }
    public void visitVINVOKE(byte op, jq_Method f) {
        super.visitVINVOKE(op, f);
        if (_unsafe.isUnsafe(f)) {
            UNSAFEhelper(f, Invoke.INVOKESTATIC_V.INSTANCE);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            ADDRESShelper(f, f.isStatic()?(Invoke)Invoke.INVOKESTATIC_V.INSTANCE:Invoke.INVOKEVIRTUAL_V.INSTANCE);
            return;
        }
        f = (jq_Method) tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        Invoke oper;
        boolean instance_call;
        switch (op) {
            case INVOKE_VIRTUAL:
                instance_call = true;
                if (dynlink)
                    oper = Invoke.INVOKEVIRTUAL_V_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKEVIRTUAL_V.INSTANCE;
                }
                break;
            case INVOKE_STATIC:
                instance_call = false;
                if (dynlink)
                    oper = Invoke.INVOKESTATIC_V_DYNLINK.INSTANCE;
                else {
                    oper = Invoke.INVOKESTATIC_V.INSTANCE;
                }
                break;
            case INVOKE_SPECIAL:
                instance_call = true;
                Assert._assert(f instanceof jq_InstanceMethod);
                if (dynlink)
                    oper = Invoke.INVOKESPECIAL_V_DYNLINK.INSTANCE;
                else {
                    f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                    oper = Invoke.INVOKESTATIC_V.INSTANCE;
                }
                break;
            case INVOKE_INTERFACE:
                instance_call = true;
                oper = Invoke.INVOKEINTERFACE_V.INSTANCE;
                break;
            default:
                throw new InternalError();
        }
        INVOKEhelper(oper, f, f.getReturnType(), instance_call);
    }
    public void visitNEW(jq_Type f) {
        super.visitNEW(f);
        RegisterOperand res = getStackRegister(f);
        Quad q = New.create(quad_cfg.getNewQuadID(), New.NEW.INSTANCE, res, new TypeOperand(f));
        appendQuad(q);
        current_state.push_A(res.copy());
    }
    public void visitNEWARRAY(jq_Array f) {
        super.visitNEWARRAY(f);
        Operand size = current_state.pop_I();
        RegisterOperand res = getStackRegister(f);
        Quad q = NewArray.create(quad_cfg.getNewQuadID(), NewArray.NEWARRAY.INSTANCE, res, size, new TypeOperand(f));
        appendQuad(q);
        mergeStateWithAllExHandlers(false);
        current_state.push_A(res.copy());
    }
    public void visitCHECKCAST(jq_Type f) {
        super.visitCHECKCAST(f);
        Operand op = current_state.pop(); // could be P or A
        RegisterOperand res = getStackRegister(f);
        if (!f.isAddressType()) {
            Quad q = CheckCast.create(quad_cfg.getNewQuadID(), CheckCast.CHECKCAST.INSTANCE, res, op, new TypeOperand(f));
            appendQuad(q);
            mergeStateWithAllExHandlers(false);
            current_state.push_A(res.copy());
        } else {
            current_state.push_P(res);
        }
    }
    public void visitINSTANCEOF(jq_Type f) {
        super.visitINSTANCEOF(f);
        Assert._assert(!f.isAddressType(), method.toString());
        Operand op = current_state.pop_A();
        RegisterOperand res = getStackRegister(jq_Primitive.BOOLEAN);
        Quad q = InstanceOf.create(quad_cfg.getNewQuadID(), InstanceOf.INSTANCEOF.INSTANCE, res, op, new TypeOperand(f));
        appendQuad(q);
        current_state.push_I(res.copy());
    }
    public void visitARRAYLENGTH() {
        super.visitARRAYLENGTH();
        Operand op = current_state.pop_A();
        clearCurrentGuard();
        if (performNullCheck(op)) {
            if (TRACE) System.out.println("Null check triggered on "+op);
            return;
        }
        RegisterOperand res = getStackRegister(jq_Primitive.INT);
        Quad q = ALength.create(quad_cfg.getNewQuadID(), ALength.ARRAYLENGTH.INSTANCE, res, op);
        appendQuad(q);
        current_state.push_I(res.copy());
    }
    public void visitATHROW() {
        super.visitATHROW();
        this.uncond_branch = true;
        Operand op0 = current_state.pop_A();
        Quad q = Return.create(quad_cfg.getNewQuadID(), Return.THROW_A.INSTANCE, op0);
        appendQuad(q);
        current_state.clearStack();
    }
    public void visitMONITOR(byte op) {
        super.visitMONITOR(op);
        Operand op0 = current_state.pop_A();
        Monitor oper = op==MONITOR_ENTER ? (Monitor)Monitor.MONITORENTER.INSTANCE : (Monitor)Monitor.MONITOREXIT.INSTANCE;
        Quad q = Monitor.create(quad_cfg.getNewQuadID(), oper, op0);
        appendQuad(q);
        mergeStateWithAllExHandlers(false);
    }
    public void visitMULTINEWARRAY(jq_Type f, char dim) {
        super.visitMULTINEWARRAY(f, dim);
        RegisterOperand result = getStackRegister(f, dim-1);
        Quad q = Invoke.create(quad_cfg.getNewQuadID(), Invoke.INVOKESTATIC_A.INSTANCE, result, new MethodOperand(joeq.Runtime.Arrays._multinewarray), dim+2);
        RegisterOperand rop = new RegisterOperand(rf.getOrCreateStack(current_state.getStackSize(), jq_Primitive.INT), jq_Primitive.INT);
        Quad q2 = Move.create(quad_cfg.getNewQuadID(), Move.MOVE_I.INSTANCE, rop, new IConstOperand(dim));
        appendQuad(q2);
        Invoke.setParam(q, 0, (RegisterOperand) rop.copy());
        rop = new RegisterOperand(rf.getOrCreateStack(current_state.getStackSize()+1, jq_Type._class), jq_Type._class);
        q2 = Move.create(quad_cfg.getNewQuadID(), Move.MOVE_A.INSTANCE, rop, new AConstOperand(f));
        appendQuad(q2);
        Invoke.setParam(q, 1, (RegisterOperand) rop.copy());
        for (int i=0; i<dim; ++i) {
            Operand op = current_state.pop_I();
            if (op instanceof RegisterOperand) rop = (RegisterOperand)op;
            else {
                rop = getStackRegister(jq_Primitive.INT);
                q2 = Move.create(quad_cfg.getNewQuadID(), Move.MOVE_I.INSTANCE, (RegisterOperand) rop.copy(), op);
                appendQuad(q2);
            }
            Invoke.setParam(q, i+2, rop);
        }
        appendQuad(q);
        mergeStateWithAllExHandlers(false);
        current_state.push(result.copy(), f);
    }

    public static final boolean ELIM_NULL_CHECKS = true;
    
    boolean performNullCheck(Operand op) {
        if (op instanceof AConstOperand) {
            Object val = ((AConstOperand)op).getValue();
            if (val != null) {
                setCurrentGuard(new UnnecessaryGuardOperand());
                return false;
            } else {
                Quad q = NullCheck.create(quad_cfg.getNewQuadID(), NullCheck.NULL_CHECK.INSTANCE, null, op.copy());
                appendQuad(q);
                if (false) {
                    endBasicBlock = true;
                    mergeStateWithNullPtrExHandler(true);
                    return true;
                } else {
                    mergeStateWithNullPtrExHandler(false);
                    return false;
                }
            }
        }
        RegisterOperand rop = (RegisterOperand)op;
        if (ELIM_NULL_CHECKS) {
            if (hasGuard(rop)) {
                Operand guard = getGuard(rop);
                setCurrentGuard(guard);
                return false;
            }
        }
        RegisterOperand guard = makeGuardReg();
        Quad q = NullCheck.create(quad_cfg.getNewQuadID(), NullCheck.NULL_CHECK.INSTANCE, guard, rop.copy());
        appendQuad(q);
        mergeStateWithNullPtrExHandler(false);
        setCurrentGuard(guard);
        setGuard(rop, guard);
        
        jq_Type type = rop.getType();
        if (type.isAddressType()) {
            // occurs when we compile Address.<init>, etc.
            return false;
        }
        int number = getLocalNumber(rop.getRegister(), type);
        if (isLocal(rop, number, type)) {
            Operand op2 = current_state.getLocal_A(number);
            if (op2 instanceof RegisterOperand) {
                setGuard((RegisterOperand)op2, guard);
            }
            current_state.setLocal(number, op2);
            replaceLocalsOnStack(number, type);
        }
        return false;
    }
    
    boolean performBoundsCheck(Operand ref, Operand index) {
        Quad q = BoundsCheck.create(quad_cfg.getNewQuadID(), BoundsCheck.BOUNDS_CHECK.INSTANCE, ref.copy(), index.copy(), getCurrentGuard());
        appendQuad(q);
        mergeStateWithArrayBoundsExHandler(false);
        return false;
    }
    
    boolean performCheckStore(RegisterOperand ref, Operand elem) {
        jq_Type type = getTypeOf(elem);
        if (type == jq_Reference.jq_NullType.NULL_TYPE) return false;
        jq_Type arrayElemType = getArrayElementTypeOf(ref);
        if (arrayElemType.isAddressType()) {
            if (type.isAddressType() || type == jq_Reference.jq_NullType.NULL_TYPE)
                return false;
            Assert.UNREACHABLE("Storing non-address value into address array! Array: "+ref+" Type: "+type);
        }
        if (type.isAddressType()) {
            Assert.UNREACHABLE("Storing address value into non-address array! Array: "+ref+" Type: "+type);
        }
        if (ref.isExactType()) {
            if (state.isSubtype(type, arrayElemType) == YES)
                return false;
        }
        jq_Type arrayElemType2 = arrayElemType;
        if (arrayElemType.isArrayType()) {
            arrayElemType2 = ((jq_Array)arrayElemType).getInnermostElementType();
        }
        if (arrayElemType2.isLoaded() && arrayElemType2.isFinal()) {
            if (arrayElemType == type)
                return false;
        }
        Quad q = StoreCheck.create(quad_cfg.getNewQuadID(), StoreCheck.ASTORE_CHECK.INSTANCE, ref.copy(), elem.copy(), getCurrentGuard());
        appendQuad(q);
        mergeStateWithObjArrayStoreExHandler(false);
        return false;
    }

    boolean performZeroCheck(Operand op) {
        if (op instanceof IConstOperand) {
            int val = ((IConstOperand)op).getValue();
            if (val != 0) {
                setCurrentGuard(new UnnecessaryGuardOperand());
                return false;
            } else {
                Quad q = ZeroCheck.create(quad_cfg.getNewQuadID(), ZeroCheck.ZERO_CHECK_I.INSTANCE, null, op.copy());
                appendQuad(q);
                if (false) {
                    endBasicBlock = true;
                    mergeStateWithArithExHandler(true);
                    return true;
                } else {
                    mergeStateWithArithExHandler(false);
                    return false;
                }
            }
        }
        if (op instanceof LConstOperand) {
            long val = ((LConstOperand)op).getValue();
            if (val != 0) {
                setCurrentGuard(new UnnecessaryGuardOperand());
                return false;
            } else {
                Quad q = ZeroCheck.create(quad_cfg.getNewQuadID(), ZeroCheck.ZERO_CHECK_L.INSTANCE, null, op.copy());
                appendQuad(q);
                if (false) {
                    endBasicBlock = true;
                    mergeStateWithArithExHandler(true);
                    return true;
                } else {
                    mergeStateWithArithExHandler(false);
                    return false;
                }
            }
        }
        RegisterOperand rop = (RegisterOperand)op;
        if (hasGuard(rop)) {
            Operand guard = getGuard(rop);
            setCurrentGuard(guard);
            return false;
        }
        RegisterOperand guard = makeGuardReg();
        ZeroCheck oper = null;
        if (rop.getType() == jq_Primitive.LONG) oper = ZeroCheck.ZERO_CHECK_L.INSTANCE;
        else if (rop.getType().isIntLike()) oper = ZeroCheck.ZERO_CHECK_I.INSTANCE;
        else Assert.UNREACHABLE("Zero check on "+rop+" type "+rop.getType());
        Quad q = ZeroCheck.create(quad_cfg.getNewQuadID(), oper, guard, rop.copy());
        appendQuad(q);
        mergeStateWithArithExHandler(false);
        setCurrentGuard(guard);
        setGuard(rop, guard);
        
        jq_Type type = rop.getType();
        int number = getLocalNumber(rop.getRegister(), type);
        if (isLocal(rop, number, type)) {
            Operand op2 = null;
            if (type == jq_Primitive.LONG)
                op2 = current_state.getLocal_L(number);
            else if (type.isIntLike())
                op2 = current_state.getLocal_I(number);
            else
                Assert.UNREACHABLE("Unknown type for local "+number+" "+rop+": "+type);
            if (TRACE) System.out.println(rop+" is a local variable of type "+type+": currently "+op2);
            if (op2 instanceof RegisterOperand) {
                setGuard((RegisterOperand)op2, guard);
            }
            current_state.setLocal(number, op2);
            replaceLocalsOnStack(number, type);
        }
        return false;
    }
    
    static jq_Type getTypeOf(Operand op) {
        if (op instanceof IConstOperand) return jq_Primitive.INT;
        if (op instanceof FConstOperand) return jq_Primitive.FLOAT;
        if (op instanceof LConstOperand) return jq_Primitive.LONG;
        if (op instanceof DConstOperand) return jq_Primitive.DOUBLE;
        if (op instanceof PConstOperand) return Address._class;
        if (op instanceof AConstOperand) {
            Object val = ((AConstOperand)op).getValue();
            if (val == null) return jq_Reference.jq_NullType.NULL_TYPE;
            return Reflection.getTypeOf(val);
        }
        Assert._assert(op instanceof RegisterOperand, op.toString() + " is not a RegisterOperand");
        return ((RegisterOperand)op).getType();
    }
    static jq_Type getArrayElementTypeOf(Operand op) {
        if (op instanceof RegisterOperand) {
            return ((jq_Array)((RegisterOperand)op).getType()).getElementType();
        } else if (op instanceof AConstOperand && ((AConstOperand)op).getValue() == null) {
            // what is the element type of an array constant 'null'?
            return PrimordialClassLoader.getJavaLangObject();
        } else {
            Assert.UNREACHABLE(op.toString());
            return null;
        }
    }
    
    void mergeStateWithAllExHandlers(boolean cfgEdgeToExit) {
        joeq.Compiler.BytecodeAnalysis.ExceptionHandlerIterator i =
            bc_bb.getExceptionHandlers();
        while (i.hasNext()) {
            joeq.Compiler.BytecodeAnalysis.ExceptionHandler eh = i.nextEH();
            mergeStateWith(eh);
        }
    }
    void mergeStateWithNullPtrExHandler(boolean cfgEdgeToExit) {
        joeq.Compiler.BytecodeAnalysis.ExceptionHandlerIterator i =
            bc_bb.getExceptionHandlers();
        while (i.hasNext()) {
            joeq.Compiler.BytecodeAnalysis.ExceptionHandler eh = i.nextEH();
            jq_Class k = eh.getExceptionType();
            if (k == PrimordialClassLoader.getJavaLangNullPointerException() ||
                k == PrimordialClassLoader.getJavaLangRuntimeException() ||
                k == PrimordialClassLoader.getJavaLangException() ||
                k == PrimordialClassLoader.getJavaLangThrowable() ||
                k == null) {
                mergeStateWith(eh);
                break;
            }
        }
    }
    void mergeStateWithArithExHandler(boolean cfgEdgeToExit) {
        joeq.Compiler.BytecodeAnalysis.ExceptionHandlerIterator i =
            bc_bb.getExceptionHandlers();
        while (i.hasNext()) {
            joeq.Compiler.BytecodeAnalysis.ExceptionHandler eh = i.nextEH();
            jq_Class k = eh.getExceptionType();
            if (k == PrimordialClassLoader.getJavaLangArithmeticException() ||
                k == PrimordialClassLoader.getJavaLangRuntimeException() ||
                k == PrimordialClassLoader.getJavaLangException() ||
                k == PrimordialClassLoader.getJavaLangThrowable() ||
                k == null) {
                mergeStateWith(eh);
                break;
            }
        }
    }
    void mergeStateWithArrayBoundsExHandler(boolean cfgEdgeToExit) {
        joeq.Compiler.BytecodeAnalysis.ExceptionHandlerIterator i =
            bc_bb.getExceptionHandlers();
        while (i.hasNext()) {
            joeq.Compiler.BytecodeAnalysis.ExceptionHandler eh = i.nextEH();
            jq_Class k = eh.getExceptionType();
            if (k == PrimordialClassLoader.getJavaLangArrayIndexOutOfBoundsException() ||
                k == PrimordialClassLoader.getJavaLangIndexOutOfBoundsException() ||
                k == PrimordialClassLoader.getJavaLangRuntimeException() ||
                k == PrimordialClassLoader.getJavaLangException() ||
                k == PrimordialClassLoader.getJavaLangThrowable() ||
                k == null) {
                mergeStateWith(eh);
                break;
            }
        }
    }
    void mergeStateWithObjArrayStoreExHandler(boolean cfgEdgeToExit) {
        joeq.Compiler.BytecodeAnalysis.ExceptionHandlerIterator i =
            bc_bb.getExceptionHandlers();
        while (i.hasNext()) {
            joeq.Compiler.BytecodeAnalysis.ExceptionHandler eh = i.nextEH();
            jq_Class k = eh.getExceptionType();
            if (k == PrimordialClassLoader.getJavaLangArrayStoreException() ||
                k == PrimordialClassLoader.getJavaLangRuntimeException() ||
                k == PrimordialClassLoader.getJavaLangException() ||
                k == PrimordialClassLoader.getJavaLangThrowable() ||
                k == null) {
                mergeStateWith(eh);
                break;
            }
        }
    }
    
    RegisterOperand makeGuardReg() {
        return RegisterFactory.makeGuardReg();
    }
    
    int getLocalNumber(Register r, jq_Type t) {
        return r.getNumber();
    }
    
    static class DummyOperand implements Operand {
        private DummyOperand() {}
        static final DummyOperand DUMMY = new DummyOperand();
        public Quad getQuad() { throw new InternalError(); }
        public void attachToQuad(Quad q) { throw new InternalError(); }
        public Operand copy() { return DUMMY; }
        public boolean isSimilar(Operand that) { return that == DUMMY; }
        public String toString() { return "<dummy>"; }
    }
    
    AbstractState allocateEmptyState() {
        // +1 because SWAP requires a temporary location.
        AbstractState s = new AbstractState(method.getMaxStack()+1, method.getMaxLocals());
        return s;
    }
    
    AbstractState allocateInitialState() {
        // +1 because SWAP requires a temporary location.
        AbstractState s = new AbstractState(method.getMaxStack()+1, method.getMaxLocals());
        jq_Type[] paramTypes = method.getParamTypes();
        for (int i=0, j=-1; i<paramTypes.length; ++i) {
            jq_Type paramType = paramTypes[i];
            ++j;
            s.locals[j] = this.makeLocal(j, paramType);
            if (paramType.getReferenceSize() == 8) {
                s.locals[++j] = DummyOperand.DUMMY;
            }
        }
        return s;
    }
    
    /** Class used to store the abstract state of the bytecode-to-quad converter. */
    public class AbstractState {

        private int stackptr;
        private Operand[] stack;
        private Operand[] locals;
        
        private AbstractState(int nstack, int nlocals) {
            this.stack = new Operand[nstack]; this.locals = new Operand[nlocals];
        }
        
        AbstractState copy() {
            AbstractState that = new AbstractState(this.stack.length, this.locals.length);
            System.arraycopy(this.stack, 0, that.stack, 0, this.stackptr);
            System.arraycopy(this.locals, 0, that.locals, 0, this.locals.length);
            that.stackptr = this.stackptr;
            return that;
        }
        
        AbstractState copyFull() {
            AbstractState that = new AbstractState(this.stack.length, this.locals.length);
            for (int i=0; i<stackptr; ++i) {
                that.stack[i] = this.stack[i].copy();
            }
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] != null)
                    that.locals[i] = this.locals[i].copy();
            }
            that.stackptr = this.stackptr;
            return that;
        }
        
        AbstractState copyAfterJSR() {
            AbstractState that = new AbstractState(this.stack.length, this.locals.length);
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] != null)
                    that.locals[i] = this.locals[i].copy();
            }
            return that;
        }
        
        AbstractState copyExceptionHandler(jq_Class exType, RegisterFactory rf) {
            if (exType == null) exType = PrimordialClassLoader.getJavaLangThrowable();
            AbstractState that = new AbstractState(this.stack.length, this.locals.length);
            that.stackptr = 1;
            RegisterOperand ex = new RegisterOperand(rf.getOrCreateStack(0, exType), exType);
            that.stack[0] = ex;
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] != null)
                    that.locals[i] = this.locals[i].copy();
            }
            return that;
        }

        void overwriteWith(AbstractState that) {
            Assert._assert(this.stack.length == that.stack.length);
            Assert._assert(this.locals.length == that.locals.length);
            System.arraycopy(that.stack, 0, this.stack, 0, that.stackptr);
            System.arraycopy(that.locals, 0, this.locals, 0, that.locals.length);
            this.stackptr = that.stackptr;
        }

        void rebuildStack() {
            for (int i = 0; i < stackptr; ++i) {
                if (TRACE) System.out.println("Rebuilding stack: "+stack[i]);
                stack[i] = stack[i].copy();
            }
        }
        
        void mergeAfterJSR(boolean[] changedLocals, AbstractState that) {
            for (int j=0; j<this.locals.length; ++j) {
                if (!changedLocals[j]) continue;
                if (TRACE) System.out.println("local "+j+" changed in jsr to "+that.locals[j]);
                if (that.locals[j] == null) this.locals[j] = null;
                else this.locals[j] = that.locals[j].copy();
            }
            this.stackptr = that.stackptr;
            for (int i=0; i<stackptr; ++i) {
                this.stack[i] = that.stack[i].copy();
            }
        }
        boolean merge(AbstractState that, RegisterFactory rf) {
            if (this.stackptr != that.stackptr) throw new VerifyError(this.stackptr+" != "+that.stackptr);
            Assert._assert(this.locals.length == that.locals.length);
            boolean change = false;
            for (int i=0; i<this.stackptr; ++i) {
                Operand o = meet(this.stack[i], that.stack[i], true, i);
                if (o != this.stack[i] && (o == null || !o.isSimilar(this.stack[i]))) change = true;
                this.stack[i] = o;
            }
            for (int i=0; i<this.locals.length; ++i) {
                Operand o = meet(this.locals[i], that.locals[i], false, i);
                if (o != this.locals[i] && (o == null || !o.isSimilar(this.locals[i]))) change = true;
                this.locals[i] = o;
            }
            return change;
        }
        
        boolean mergeExceptionHandler(AbstractState that, jq_Class exType, RegisterFactory rf) {
            if (exType == null) exType = PrimordialClassLoader.getJavaLangThrowable();
            Assert._assert(this.locals.length == that.locals.length);
            Assert._assert(this.stackptr == 1);
            boolean change = false;
            RegisterOperand ex = new RegisterOperand(rf.getOrCreateStack(0, exType), exType);
            Operand o = meet(this.stack[0], ex, true, 0);
            if (o != this.stack[0] && (o == null || !o.isSimilar(this.stack[0]))) change = true;
            this.stack[0] = o;
            for (int i=0; i<this.locals.length; ++i) {
                 o = meet(this.locals[i], that.locals[i], false, i);
                 if (o != this.locals[i] && (o == null || !o.isSimilar(this.locals[i]))) change = true;
                 this.locals[i] = o;
            }
            return change;
        }

        Operand meet(Operand op1, Operand op2, boolean stack, int index) {
            if (TRACE) System.out.println("Meeting "+op1+" with "+op2+", "+(stack?"S":"L")+index);
            if (op1 == op2) {
                // same operand, or both null.
                return op1;
            }
            if ((op1 == null) || (op2 == null)) {
                // no information about one of the operands.
                return null;
            }
            if (Operand.Util.isConstant(op1)) {
                if (op1.isSimilar(op2)) {
                    // same constant value.
                    return op1;
                }
                if (op2 instanceof DummyOperand) {
                    return null;
                }
                jq_Type type = state.findCommonSuperclass(getTypeOf(op1), getTypeOf(op2));
                if (type != null) {
                    // different constants of the same type
                    RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, type):rf.getOrCreateLocal(index, type), type);
                    return res;
                } else {
                    // constants of incompatible types.
                    return null;
                }
            }
            if (op1 instanceof RegisterOperand) {
                if (op2 instanceof DummyOperand) {
                    // op1 is a register, op2 is a dummy
                    return null;
                }
                RegisterOperand rop1 = (RegisterOperand)op1;
                jq_Type t1 = rop1.getType();
                if (t1 == jq_ReturnAddressType.INSTANCE) {
                    // op1 is a return address.
                    if (op2 instanceof RegisterOperand &&
                        ((RegisterOperand)op2).getType() == jq_ReturnAddressType.INSTANCE) {
                        return op1;
                    }
                    return null;
                }
                if (op2 instanceof RegisterOperand) {
                    // both are registers.
                    RegisterOperand rop2 = (RegisterOperand)op2;
                    jq_Type t2 = rop2.getType();
                    
                    if (t1 == t2) {
                        // registers have same type.
                        if (rop1.hasMoreConservativeFlags(rop2)) {
                            // registers have compatible flags.
                            if ((rop1.scratchObject == null) ||
                                ((Operand)rop1.scratchObject).isSimilar((Operand)rop2.scratchObject)) {
                                // null guards match.
                                return rop1;
                            }
                            // null guards don't match.
                            RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                            res.setFlags(rop1.getFlags());
                            return res;
                        }
                        // incompatible flags.
                        RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                        if ((rop1.scratchObject == null) ||
                            ((Operand)rop1.scratchObject).isSimilar((Operand)rop2.scratchObject)) {
                            // null guards match.
                            res.scratchObject = rop1.scratchObject;
                        }
                        res.setFlags(rop1.getFlags());
                        res.meetFlags(rop2.getFlags());
                        return res;
                    }
                    if (t2 == jq_ReturnAddressType.INSTANCE) {
                        // op2 is a return address, while op1 isn't.
                        return null;
                    }
                    if (state.isSubtype(t2, t1) == YES) {
                        // t2 is a subtype of t1.
                        if (!rop1.isExactType() && rop1.hasMoreConservativeFlags(rop2)) {
                            // flags and exact type matches.
                            if ((rop1.scratchObject == null) ||
                                ((Operand)rop1.scratchObject).isSimilar((Operand)rop2.scratchObject)) {
                                // null guards match.
                                return rop1;
                            }
                            // null guards don't match.
                            RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                            res.setFlags(rop1.getFlags());
                            return res;
                        }
                        // doesn't match.
                        RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                        if ((rop1.scratchObject == null) ||
                            ((Operand)rop1.scratchObject).isSimilar((Operand)rop2.scratchObject)) {
                            // null guards match.
                            res.scratchObject = rop1.scratchObject;
                        }
                        res.setFlags(rop1.getFlags());
                        res.meetFlags(rop2.getFlags());
                        res.clearExactType();
                        return res;
                    }
                    if ((t2 = state.findCommonSuperclass(t1, t2)) != null) {
                        // common superclass
                        RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t2):rf.getOrCreateLocal(index, t2), t2);
                        if (rop1.scratchObject != null) {
                            if (((Operand)rop1.scratchObject).isSimilar((Operand)rop2.scratchObject)) {
                                // null guards match.
                                res.scratchObject = rop1.scratchObject;
                            }
                        }
                        res.setFlags(rop1.getFlags());
                        res.meetFlags(rop2.getFlags());
                        res.clearExactType();
                        return res;
                    }
                    // no common superclass
                    return null;
                }
                // op2 is not a register.
                jq_Type t2 = getTypeOf(op2);
                if (t1 == t2) {
                    // same type.
                    if ((rop1.scratchObject == null) || (t2 != jq_Reference.jq_NullType.NULL_TYPE)) {
                        // null guard matches.
                        return rop1;
                    }
                    // null guard doesn't match.
                    RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                    res.setFlags(rop1.getFlags());
                    return res;
                }
                Assert._assert(t2 != jq_ReturnAddressType.INSTANCE);
                if (state.isSubtype(t2, t1) == YES) {
                    // compatible type.
                    if (!rop1.isExactType()) {
                        if ((rop1.scratchObject == null) || (t2 != jq_Reference.jq_NullType.NULL_TYPE)) {
                            // null guard matches.
                            return rop1;
                        }
                        // null guard doesn't match.
                        RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                        res.setFlags(rop1.getFlags());
                        return res;
                    }
                    RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t1):rf.getOrCreateLocal(index, t1), t1);
                    if (t2 != jq_Reference.jq_NullType.NULL_TYPE) {
                        // null guard matches.
                        res.scratchObject = rop1.scratchObject;
                    }
                    res.setFlags(rop1.getFlags());
                    res.clearExactType();
                    return res;
                }
                if ((t2 = state.findCommonSuperclass(t1, t2)) != null) {
                    // common superclass
                    RegisterOperand res = new RegisterOperand(stack?rf.getOrCreateStack(index, t2):rf.getOrCreateLocal(index, t2), t2);
                    if (t2 != jq_Reference.jq_NullType.NULL_TYPE) {
                        // null guard matches.
                        res.scratchObject = rop1.scratchObject;
                    }
                    res.setFlags(rop1.getFlags());
                    res.clearExactType();
                    return res;
                }
                // no common superclass
                return null;
            }
            // op1 is not a register.
            if (op1.isSimilar(op2)) {
                return op1;
            } else {
                return null;
            }
        }
        
        int getStackSize() { return this.stackptr; }
        
        void push_I(Operand op) { Assert._assert(getTypeOf(op).isIntLike()); push(op); }
        void push_F(Operand op) { Assert._assert(getTypeOf(op) == jq_Primitive.FLOAT); push(op); }
        void push_L(Operand op) { Assert._assert(getTypeOf(op) == jq_Primitive.LONG); push(op); pushDummy(); }
        void push_D(Operand op) { Assert._assert(getTypeOf(op) == jq_Primitive.DOUBLE); push(op); pushDummy(); }
        void push_A(Operand op) { Assert._assert(getTypeOf(op).isReferenceType() && !getTypeOf(op).isAddressType()); push(op); }
        void push_P(Operand op) { Assert._assert(getTypeOf(op).isAddressType()); push(op); }
        void push(Operand op, jq_Type t) {
            Assert._assert(state.isSubtype(getTypeOf(op), t) == YES);
            push(op); if (t.getReferenceSize() == 8) pushDummy();
        }
        void pushDummy() { push(DummyOperand.DUMMY); }
        void push(Operand op) {
            if (TRACE) System.out.println("Pushing "+op+" on stack "+(this.stackptr));
            this.stack[this.stackptr++] = op;
        }

        Operand pop_I() { Operand op = pop(); Assert._assert(getTypeOf(op).isIntLike()); return op; }
        Operand pop_F() { Operand op = pop(); Assert._assert(getTypeOf(op) == jq_Primitive.FLOAT); return op; }
        Operand pop_L() { popDummy(); Operand op = pop(); Assert._assert(getTypeOf(op) == jq_Primitive.LONG); return op; }
        Operand pop_D() { popDummy(); Operand op = pop(); Assert._assert(getTypeOf(op) == jq_Primitive.DOUBLE); return op; }
        Operand pop_A() { Operand op = pop(); Assert._assert(getTypeOf(op).isReferenceType() && !getTypeOf(op).isAddressType()); return op; }
        Operand pop_P() {
            Operand op = pop();
            if (op instanceof AConstOperand) {
                op = new PConstOperand(null);
            }
            Assert._assert(getTypeOf(op).isAddressType() ||
                      state.isSubtype(getTypeOf(op), Address._class) != NO);
            return op;
        }
        void popDummy() { Operand op = pop(); Assert._assert(op == DummyOperand.DUMMY); }
        Operand pop(jq_Type t) {
            if (t.getReferenceSize() == 8) popDummy();
            Operand op = pop();
            if (t.isAddressType()) {
                if (op instanceof AConstOperand) {
                    op = new PConstOperand(null);
                }
                jq_Type t2 = getTypeOf(op);
                Assert._assert(t2 == jq_Reference.jq_NullType.NULL_TYPE ||
                          t2.isAddressType() ||
                          state.isSubtype(t2, Address._class) != NO);
            }
            //jq.Assert(state.isSubtype(getTypeOf(op), t) != NO);
            return op;
        }
        Operand pop() {
            if (TRACE) System.out.println("Popping "+this.stack[this.stackptr-1]+" from stack "+(this.stackptr-1));
            return this.stack[--this.stackptr];
        }

        Operand peekStack(int i) { return this.stack[this.stackptr-i-1]; }
        void pokeStack(int i, Operand op) { this.stack[this.stackptr-i-1] = op; }
        void clearStack() { this.stackptr = 0; }
        
        Operand getLocal_I(int i) { Operand op = getLocal(i); Assert._assert(getTypeOf(op).isIntLike()); return op; }
        Operand getLocal_F(int i) { Operand op = getLocal(i); Assert._assert(getTypeOf(op) == jq_Primitive.FLOAT); return op; }
        Operand getLocal_L(int i) {
            Operand op = getLocal(i);
            Assert._assert(getTypeOf(op) == jq_Primitive.LONG);
            Assert._assert(getLocal(i+1) == DummyOperand.DUMMY);
            return op;
        }
        Operand getLocal_D(int i) {
            Operand op = getLocal(i);
            Assert._assert(getTypeOf(op) == jq_Primitive.DOUBLE);
            Assert._assert(getLocal(i+1) == DummyOperand.DUMMY);
            return op;
        }
        Operand getLocal_A(int i) {
            Operand op = getLocal(i);
            Assert._assert(getTypeOf(op).isReferenceType());
            Assert._assert(!getTypeOf(op).isAddressType());
            return op;
        }
        Operand getLocal(int i) {
            return this.locals[i].copy();
        }
        void setLocal(int i, Operand op) {
            this.locals[i] = op;
        }
        void setLocalDual(int i, Operand op) {
            this.locals[i] = op; this.locals[i+1] = DummyOperand.DUMMY;
        }
        void dumpState() {
            System.out.print("Locals:");
            for (int i=0; i<this.locals.length; ++i) {
                if (this.locals[i] != null)
                    System.out.print(" L"+i+":"+this.locals[i]);
            }
            System.out.print("\nStack: ");
            for (int i=0; i<this.stackptr; ++i) {
                System.out.print(" S"+i+":"+this.stack[i]);
            }
            System.out.println();
        }
    }

    public static class jq_ReturnAddressType extends jq_Reference {
        public static final jq_ReturnAddressType INSTANCE = new jq_ReturnAddressType();
        private BasicBlock returnTarget;
        private jq_ReturnAddressType() { super(Utf8.get("L&ReturnAddress;"), PrimordialClassLoader.loader); }
        private jq_ReturnAddressType(BasicBlock returnTarget) {
            super(Utf8.get("L&ReturnAddress;"), PrimordialClassLoader.loader);
            this.returnTarget = returnTarget;
        }
        public boolean isAddressType() { return false; }
        public String getJDKName() { return desc.toString(); }
        public String getJDKDesc() { return getJDKName(); }
        public jq_Class[] getInterfaces() { Assert.UNREACHABLE(); return null; }
        public jq_Class getInterface(Utf8 desc) { Assert.UNREACHABLE(); return null; }
        public boolean implementsInterface(jq_Class k) { Assert.UNREACHABLE(); return false; }
        public jq_InstanceMethod getVirtualMethod(jq_NameAndDesc nd) { Assert.UNREACHABLE(); return null; }
        public String getName() { return "<retaddr>"; }
        public String shortName() { return "<retaddr>"; }
        public boolean isClassType() { Assert.UNREACHABLE(); return false; }
        public boolean isArrayType() { Assert.UNREACHABLE(); return false; }
        public boolean isFinal() { Assert.UNREACHABLE(); return false; }
        public jq_Reference getDirectPrimarySupertype() { Assert.UNREACHABLE(); return null; }
        public int getDepth() { Assert.UNREACHABLE(); return 0; }
        public void load() { Assert.UNREACHABLE(); }
        public void verify() { Assert.UNREACHABLE(); }
        public void prepare() { Assert.UNREACHABLE(); }
        public void sf_initialize() { Assert.UNREACHABLE(); }
        public void compile() { Assert.UNREACHABLE(); }
        public void cls_initialize() { Assert.UNREACHABLE(); }
        public String toString() { return "<retaddr> (target="+returnTarget+")"; }
        public boolean equals(Object rat) {
            if (!(rat instanceof jq_ReturnAddressType)) return false;
            return ((jq_ReturnAddressType)rat).returnTarget.equals(this.returnTarget);
        }
        public int hashCode() {
            if (returnTarget == null) return 0;
            return returnTarget.hashCode();
        }
    }
    static interface UnsafeHelper {
        public boolean isUnsafe(jq_Method m);
        public boolean endsBB(jq_Method m);
        public boolean handleMethod(BytecodeToQuad b2q, ControlFlowGraph quad_cfg, BytecodeToQuad.AbstractState current_state, jq_Method m, Operator.Invoke oper);
    }

    private static UnsafeHelper _unsafe;
    static {
        /* Set up delegates. */
        _unsafe = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _unsafe = attemptDelegate("joeq.Compiler.Quad.B2QUnsafeHandler");
        }
        if (_unsafe == null) {
            _unsafe = new joeq.Compiler.Quad.B2QUnsafeIgnorer();
        }
    }

    private static UnsafeHelper attemptDelegate(String s) {
        String type = "BC2Q delegate";
        try {
            Class c = Class.forName(s);
            return (UnsafeHelper)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            //System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            //System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            //System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }
}
