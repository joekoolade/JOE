// SimpleCompiler.java, created Thu Mar  6  0:42:32 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad.x86;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joeq.Allocator.CodeAllocator;
import joeq.Allocator.DefaultCodeAllocator;
import joeq.Allocator.DefaultHeapAllocator;
import joeq.Allocator.HeapAllocator;
import joeq.Allocator.ObjectLayout;
import joeq.Assembler.Code2CodeReference;
import joeq.Assembler.Code2HeapReference;
import joeq.Assembler.DirectBindCall;
import joeq.Assembler.x86.x86;
import joeq.Assembler.x86.x86Assembler;
import joeq.Assembler.x86.x86Constants;
import joeq.Bootstrap.BootstrapCodeAllocator;
import joeq.Bootstrap.SinglePassBootImage;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_TryCatch;
import joeq.Class.jq_Type;
import joeq.ClassLib.ClassLibInterface;
import joeq.Compiler.CompilerInterface;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ExceptionHandler;
import joeq.Compiler.Quad.ExceptionHandlerList;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.RegisterFactory;
import joeq.Compiler.Quad.Operand.AConstOperand;
import joeq.Compiler.Quad.Operand.BasicBlockTableOperand;
import joeq.Compiler.Quad.Operand.Const4Operand;
import joeq.Compiler.Quad.Operand.Const8Operand;
import joeq.Compiler.Quad.Operand.ParamListOperand;
import joeq.Compiler.Quad.Operand.RegisterOperand;
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
import joeq.Compiler.Reference.x86.x86ReferenceCompiler;
import joeq.Compiler.Reference.x86.x86ReferenceLinker;
import joeq.Main.HostedVM;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Runtime.ExceptionDeliverer;
import joeq.Runtime.MathSupport;
import joeq.Runtime.ObjectTraverser;
import joeq.Runtime.Reflection;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.TypeCheck;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_x86RegisterState;
import jwutil.collections.AppendIterator;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: SimpleCompiler.java,v 1.28 2004/09/30 03:37:06 joewhaley Exp $
 */
public class SimpleCompiler implements x86Constants, BasicBlockVisitor, QuadVisitor {

    static {
        // we are using x86, so initialize the register state factory.
        jq_x86RegisterState.initFactory();
    }
    
    public static class Factory implements CompilerInterface {
        public static final Factory INSTANCE = new Factory();
        public Factory() {}
        public jq_CompiledCode compile(jq_Method m) {
            return new SimpleCompiler(m).compile();
        }
        public jq_CompiledCode generate_compile_stub(jq_Method m) {
            return x86ReferenceCompiler.generate_compile_stub(m);
        }
        public jq_StaticMethod getInvokestaticLinkMethod() {
            return x86ReferenceLinker._invokestatic;
        }
        public jq_StaticMethod getInvokespecialLinkMethod() {
            return x86ReferenceLinker._invokespecial;
        }
        public jq_StaticMethod getInvokeinterfaceLinkMethod() {
            return x86ReferenceLinker._invokeinterface;
        }
    }
    
    public static /*final*/ boolean ALWAYS_TRACE = false;
    public static /*final*/ boolean TRACE_STUBS = false;

    public static final Set TraceMethod_MethodNames = new HashSet();
    public static final Set TraceMethod_ClassNames = new HashSet();
    public static final Set TraceQuad_MethodNames = new HashSet();
    public static final Set TraceQuad_ClassNames = new HashSet();
    
    public boolean TraceQuads;
    public boolean TraceMethods;
    public boolean TraceArguments;
    
    public static final int DEFAULT_ALIGNMENT = 32;
    
    public ControlFlowGraph cfg;
    public jq_Method method;
    public Map registerLocations;
    
    public boolean TRACE;
    
    public SimpleCompiler() { }
    public SimpleCompiler(ControlFlowGraph cfg) { this.init(cfg); }
    public SimpleCompiler(jq_Method m) { this.init(m); }
    
    public void init(jq_Method m) {
        init(CodeCache.getCode(m));
    }
    
    public void init(ControlFlowGraph cfg) {
        this.cfg = cfg;
        method = cfg.getMethod();
        TRACE = ALWAYS_TRACE;
        if (TraceQuad_MethodNames.contains(method.getName().toString())) {
            TraceQuads = true;
            TraceMethods = true;
        } else if (TraceQuad_ClassNames.contains(method.getDeclaringClass().getName().toString())) {
            TraceQuads = true;
            TraceMethods = true;
        } else if (TraceMethod_MethodNames.contains(method.getName().toString())) {
            TraceQuads = false;
            TraceMethods = true;
        } else if (TraceMethod_ClassNames.contains(method.getDeclaringClass().getName().toString())) {
            TraceQuads = false;
            TraceMethods = true;
        } else {
            TraceQuads = false;
            TraceMethods = false;
        }
        TraceArguments = false;
        registerLocations = new HashMap();
        code_relocs = new LinkedList();
        data_relocs = new LinkedList();
    }
    
    public String toString() {
        return "x86Quad/"+Strings.left(method.getName().toString(), 10);
    }
    
    private x86Assembler asm;   // Assembler to output to.
    private int n_paramwords;   // number of words used by incoming parameters.

    private List code_relocs;
    private List data_relocs;
    
    public final void emitCallRelative(jq_Method target) { emitCallRelative(target, asm, code_relocs); }
    public static final void emitCallRelative(jq_Method target, x86Assembler asm, List code_relocs) {
        asm.emitCALL_rel32(x86.CALL_rel32, 0);
        DirectBindCall r = new DirectBindCall((CodeAddress) asm.getCurrentAddress().offset(-4), target);
        code_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Direct bind call: "+r);
    }
    public final void emitPushAddressOf(Object o) { emitPushAddressOf(o, asm, data_relocs); }
    public static final void emitPushAddressOf(Object o, x86Assembler asm, List data_relocs) {
        if (o != null) {
            HeapAddress addr = HeapAddress.addressOf(o);
            asm.emit1_Imm32(x86.PUSH_i32, addr.to32BitValue());
            Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
            data_relocs.add(r);
            if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
        } else {
            asm.emit1_Imm8(x86.PUSH_i8, (byte)0);
        }
    }
    public final void emitPushMemory(jq_StaticField f) { emitPushMemory(f, asm, data_relocs); }
    public static final void emitPushMemory(jq_StaticField f, x86Assembler asm, List data_relocs) {
        HeapAddress addr = f.getAddress();
        asm.emit2_Mem(x86.PUSH_m, addr.to32BitValue());
        Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
    }
    public final void emitPushMemory8(jq_StaticField f) { emitPushMemory8(f, asm, data_relocs); }
    public static final void emitPushMemory8(jq_StaticField f, x86Assembler asm, List data_relocs) {
        HeapAddress addr = f.getAddress();
        asm.emit2_Mem(x86.PUSH_m, addr.offset(4).to32BitValue()); // hi
        Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), (HeapAddress) addr.offset(4));
        data_relocs.add(r);
        asm.emit2_Mem(x86.PUSH_m, addr.to32BitValue()); // lo
        r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
    }
    public final void emitPopMemory(jq_StaticField f) { emitPopMemory(f, asm, data_relocs); }
    public static final void emitPopMemory(jq_StaticField f, x86Assembler asm, List data_relocs) {
        HeapAddress addr = f.getAddress();
        asm.emit2_Mem(x86.POP_m, addr.to32BitValue());
        Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
    }
    public final void emitPopMemory8(jq_StaticField f) { emitPopMemory8(f, asm, data_relocs); }
    public static final void emitPopMemory8(jq_StaticField f, x86Assembler asm, List data_relocs) {
        HeapAddress addr = f.getAddress();
        asm.emit2_Mem(x86.POP_m, addr.to32BitValue()); // lo
        Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
        asm.emit2_Mem(x86.POP_m, addr.offset(4).to32BitValue()); // hi
        r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), (HeapAddress) addr.offset(4));
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
    }
    public final void emitCallMemory(jq_StaticField f) { emitCallMemory(f, asm, data_relocs); }
    public static final void emitCallMemory(jq_StaticField f, x86Assembler asm, List data_relocs) {
        HeapAddress addr = f.getAddress();
        asm.emit2_Mem(x86.CALL_m, addr.to32BitValue());
        Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
    }
    public final void emitFLD64(jq_StaticField f) { emitFLD64(f, asm, data_relocs); }
    public static final void emitFLD64(jq_StaticField f, x86Assembler asm, List data_relocs) {
        HeapAddress addr = f.getAddress();
        asm.emit2_Mem(x86.FLD_m64, addr.to32BitValue());
        Code2HeapReference r = new Code2HeapReference((CodeAddress) asm.getCurrentAddress().offset(-4), addr);
        data_relocs.add(r);
        if (ALWAYS_TRACE) System.out.println("Code2Heap reference: "+r);
    }
    
    public final List getCodeRelocs() { return code_relocs; }
    public final List getDataRelocs() { return data_relocs; }
    
    public static final jq_CompiledCode generate_compile_stub(jq_Method method) {
        if (TRACE_STUBS) System.out.println("x86 Quad Compiler: generating compile stub for "+method);
        x86Assembler asm = new x86Assembler(0, 24, 0, DEFAULT_ALIGNMENT);
        asm.setEntrypoint();
        List code_relocs = new LinkedList();
        List data_relocs = new LinkedList();
        if (TRACE_STUBS) {
            emitPushAddressOf(SystemInterface.toCString("Stub compile: "+method), asm, data_relocs);
            emitCallMemory(SystemInterface._debugwriteln, asm, data_relocs);
        }
        emitPushAddressOf(method, asm, data_relocs);
        emitCallRelative(jq_Method._compile, asm, code_relocs);
        asm.emit2_Mem(x86.JMP_m, jq_CompiledCode._entrypoint.getOffset(), EAX);
        // return generated code
        return asm.getCodeBuffer().allocateCodeBlock(null, null, null, null, 0, code_relocs, data_relocs);
    }
    
    int getParamOffset(int i) {
        Assert._assert(i < n_paramwords);
        return (n_paramwords-i+1)<<2;
    }
    
    int getStackOffset(RegisterOperand r) { return getStackOffset(r.getRegister()); }
    
    int getStackOffset(Register r) {
        Integer i = (Integer) registerLocations.get(r);
        return i.intValue();
    }
    
    int getStackFrameWords() {
        return cfg.getRegisterFactory().size();
    }
    
    void initializeRegisterLocations() {
        int current = -((getStackFrameWords()) << 2);
        RegisterFactory rf = cfg.getRegisterFactory();
        for (Iterator i=rf.iterator(); i.hasNext(); current += 4) {
            Register r = (Register) i.next();
            registerLocations.put(r, new Integer(current));
            if (TRACE) System.out.println("Register: "+r+" offset: "+current);
        }
        Assert._assert(current == 0);
    }
    
    // Generate code for the given method.
    public final jq_CompiledCode compile() {
        if (TRACE) System.out.println("x86 Quad Compiler: compiling "+method);
        
        // temporary kludge: no switching a thread during compilation.
        if (jq.RunningNative)
            Unsafe.getThreadBlock().disableThreadSwitch();
        
        try {
            // initialize stuff
            this.initializeRegisterLocations();
            
            int quadcount = 0;
            for (QuadIterator i=new QuadIterator(cfg); i.hasNext(); i.next())
                ++quadcount;
            
            asm = new x86Assembler(quadcount, quadcount*8, 5, DEFAULT_ALIGNMENT);
            asm.skip(5); // space for jump point
            asm.setEntrypoint();
            jq_Type[] params = method.getParamTypes();
            n_paramwords = method.getParamWords();
            int n_localwords = getStackFrameWords();
            
            // stack frame before prolog:
            // b0: FP->| caller's saved FP  |
            // ac:     | caller's locals    |
            //         |        ...         |
            // 94:     | caller's opstack   |
            //         |        ...         |
            // 80:     | pushed params      |
            //         |        ...         |
            // 74: SP->| ret addr in caller |
            
            // emit prolog
            asm.emitShort_Reg(x86.PUSH_r, EBP);         // push old FP
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EBP, ESP); // set new FP
            asm.emit2_Reg_Mem(x86.LEA, ESP, -n_localwords<<2, ESP);
            
            // stack frame after prolog:
            // b0:     | caller's saved FP  |
            // ac:     | caller's locals    |
            //         |        ...         |
            // 94:     | caller's opstack   |
            //         |        ...         |
            // 80:     | pushed params      |
            //         |        ...         |
            // 74:     | ret addr in caller |
            // 70: FP->| callee's FP (b0)   |
            // 6c:     | callee's locals    |
            //     SP->|        ...         |
    
            // print a debug message
            if (TraceMethods) {
                emitPushAddressOf(SystemInterface.toCString("Entering: "+method));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            
            RegisterFactory rf = cfg.getRegisterFactory();
            for (int i=0, j=0; i<params.length; ++i, ++j) {
                if (params[i].getReferenceSize() == 8) {
                    int param_offset = getParamOffset(j+1); // lo
                    int stack_offset = getStackOffset(rf.getOrCreateLocal(j, params[i])); // lo
                    asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, param_offset, EBP);
                    asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, stack_offset, EBP);
                    param_offset = getParamOffset(j); // hi
                    stack_offset = getStackOffset(rf.getOrCreateLocal(j+1, params[i])); // hi
                    asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, param_offset, EBP);
                    asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, stack_offset, EBP);
                    ++j;
                } else {
                    int param_offset = getParamOffset(j);
                    int stack_offset = getStackOffset(rf.getOrCreateLocal(j, params[i]));
                    asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, param_offset, EBP);
                    asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, stack_offset, EBP);
                }
            }
            
            // add monitorenter for synchronized methods.
            if (method.isSynchronized()) {
                if (method.isStatic()) {
                    if (TraceQuads) {
                        emitPushAddressOf(SystemInterface.toCString("entry: STATIC SYNCH ENTER"));
                        emitCallMemory(SystemInterface._debugwriteln);
                    }
                    // lock the java.lang.Class object
                    Class c = Reflection.getJDKType(method.getDeclaringClass());
                    Assert._assert(c != null);
                    emitPushAddressOf(c);
                } else {
                    if (TraceQuads) {
                        emitPushAddressOf(SystemInterface.toCString("entry: INSTANCE SYNCH ENTER"));
                        emitCallMemory(SystemInterface._debugwriteln);
                    }
                    // lock the this pointer
                    asm.emit2_Mem(x86.PUSH_m, getParamOffset(0), EBP);
                }
                emitCallRelative(joeq.Runtime.Monitor._monitorenter);
            }
            
            // generate code for each quad in order
            cfg.visitBasicBlocks(this);
    
            // generate exception table
            List tcs = new LinkedList();
            for (Iterator i=cfg.reversePostOrderIterator(); i.hasNext(); ) {
                BasicBlock bb = (BasicBlock) i.next();
                ExceptionHandlerList ex = bb.getExceptionHandlers();
                int start = asm.getBranchTarget(bb);
                int end = asm.getBranchTarget(new Integer(bb.getID()));
                for (Iterator j=ex.exceptionHandlerIterator(); j.hasNext(); ) {
                    ExceptionHandler e = (ExceptionHandler) j.next();
                    int handler = asm.getBranchTarget(e.getEntry());
                    Iterator k = e.getEntry().iterator();
                    Assert._assert(k.hasNext());
                    Quad x = (Quad) k.next();
                    Assert._assert(x.getOperator() instanceof Special.GET_EXCEPTION);
                    RegisterOperand rop = (RegisterOperand) Special.getOp1(x);
                    jq_TryCatch tc = new jq_TryCatch(start, end, handler, e.getExceptionType(), getStackOffset(rop));
                    tcs.add(tc);
                }
            }
            jq_TryCatch[] tcs_a = (jq_TryCatch[]) tcs.toArray(new jq_TryCatch[tcs.size()]);
            
            // TODO: generate bytecode map
            
            // return generated code
            jq_CompiledCode code;
            code = asm.getCodeBuffer().allocateCodeBlock(method, tcs_a, null,
                                                         x86QuadExceptionDeliverer.INSTANCE,
                                                         n_localwords*4,
                                                         code_relocs, data_relocs);
            CodeCache.free(cfg);
            // temporary kludge: no switching a thread during compilation.
            if (jq.RunningNative)
                Unsafe.getThreadBlock().enableThreadSwitch();
            return code;
        } catch (RuntimeException x) {
            SystemInterface.debugwriteln("Exception occurred while compiling: "+method);
            SystemInterface.debugwriteln("Exception: "+x);
            x.printStackTrace();
            SystemInterface.die(-1);
            return null;
        }
    }
    
    public void visitBasicBlock(BasicBlock bb) {
        // record start of basic block
        asm.recordBranchTarget(bb);
        // resolve forward branches to this block
        asm.resolveForwardBranches(bb);
        // generate code for this block
        for (joeq.Util.Templates.ListIterator.Quad i=bb.iterator(); i.hasNext(); ) {
            this.handled = false;
            Quad q = i.nextQuad();
            q.accept(this);
            Assert._assert(handled, q.toString());
        }
        if (bb.getFallthroughSuccessor() != null)
            branchHelper(BytecodeVisitor.CMP_UNCOND, bb.getFallthroughSuccessor());
        // record end of basic block (for exception handler ranges)
        asm.recordBranchTarget(new Integer(bb.getID()));
    }
    
    public static byte THREAD_BLOCK_PREFIX = x86.PREFIX_FS;
    public static int  THREAD_BLOCK_OFFSET = 0x14;

    private int getPairedRegister(int register) {
        switch (register) {
        case EAX: return EDX;
        case EBX: return ECX;
        case ECX: return EBX;
        case EDX: return EAX;
        default: Assert.UNREACHABLE(); return 0;
        }
    }
    
    private void loadOperand(Operand o, int register) {
        if (o instanceof RegisterOperand) {
            int src = getStackOffset((RegisterOperand) o);
            asm.emit2_Reg_Mem(x86.MOV_r_m32, register, src, EBP);
            if (((RegisterOperand) o).getType().getReferenceSize() == 8) {
                asm.emit2_Reg_Mem(x86.MOV_r_m32, getPairedRegister(register), src+4, EBP);
            }
        } else if (o instanceof AConstOperand) {
            Object a = ((AConstOperand) o).getValue();
            emitPushAddressOf(a);
            asm.emitShort_Reg(x86.POP_r, register);
        } else if (o instanceof Const4Operand) {
            int v = ((Const4Operand) o).getBits();
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, register, v);
        } else if (o instanceof Const8Operand) {
            long v = ((Const8Operand) o).getBits();
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, register, (int)(v));                        // lo
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, getPairedRegister(register), (int)(v>>32)); // hi
        } else {
            Assert.UNREACHABLE("x86 register "+register+": "+o);
        }
    }
    
    private void storeOperand(RegisterOperand o, int register) {
        int dest = getStackOffset(o);
        asm.emit2_Reg_Mem(x86.MOV_m_r32, register, dest, EBP);
        if (o.getType().getReferenceSize() == 8) {
            asm.emit2_Reg_Mem(x86.MOV_m_r32, getPairedRegister(register), dest+4, EBP); // hi
        }
    }
    
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitExceptionThrower(joeq.Compiler.Quad.Quad)
     */
    public void visitExceptionThrower(Quad obj) {
        if (TRACE) System.out.println(this+" PEI: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitLoad(joeq.Compiler.Quad.Quad)
     */
    public void visitLoad(Quad obj) {
        if (TRACE) System.out.println(this+" Load: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitStore(joeq.Compiler.Quad.Quad)
     */
    public void visitStore(Quad obj) {
        if (TRACE) System.out.println(this+" Store: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitBranch(joeq.Compiler.Quad.Quad)
     */
    public void visitBranch(Quad obj) {
        if (TRACE) System.out.println(this+" Branch: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitCondBranch(joeq.Compiler.Quad.Quad)
     */
    public void visitCondBranch(Quad obj) {
        if (TRACE) System.out.println(this+" CondBranch: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitCheck(joeq.Compiler.Quad.Quad)
     */
    public void visitCheck(Quad obj) {
        if (TRACE) System.out.println(this+" Check: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitStaticField(joeq.Compiler.Quad.Quad)
     */
    public void visitStaticField(Quad obj) {
        if (TRACE) System.out.println(this+" StaticField: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitInstanceField(joeq.Compiler.Quad.Quad)
     */
    public void visitInstanceField(Quad obj) {
        if (TRACE) System.out.println(this+" InstanceField: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitArray(joeq.Compiler.Quad.Quad)
     */
    public void visitArray(Quad obj) {
        if (TRACE) System.out.println(this+" Array: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitAllocation(joeq.Compiler.Quad.Quad)
     */
    public void visitAllocation(Quad obj) {
        if (TRACE) System.out.println(this+" Allocation: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitTypeCheck(joeq.Compiler.Quad.Quad)
     */
    public void visitTypeCheck(Quad obj) {
        if (TRACE) System.out.println(this+" TypeCheck: "+obj);
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitALoad(joeq.Compiler.Quad.Quad)
     */
    public void visitALoad(Quad obj) {
        if (TRACE) System.out.println(this+" ALoad: "+obj);
        loadOperand(ALoad.getBase(obj), EAX);
        loadOperand(ALoad.getIndex(obj), EBX);
        jq_Type t = ((ALoad) obj.getOperator()).getType();
        int scale = t.getReferenceSize();
        switch (scale) {
        case 4:
            asm.emit2_Reg_Mem(x86.MOV_r_m32, ECX, EAX, EBX, SCALE_4, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            break;
        case 2:
            if (t == jq_Primitive.CHAR)
                asm.emit3_Reg_Mem(x86.MOVZX_r_m16, ECX, EAX, EBX, SCALE_2, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            else
                asm.emit3_Reg_Mem(x86.MOVSX_r_m16, ECX, EAX, EBX, SCALE_2, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            break;
        case 1:
            asm.emit3_Reg_Mem(x86.MOVSX_r_m8, ECX, EAX, EBX, SCALE_1, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            break;
        case 8:
            asm.emit2_Reg_Mem(x86.MOV_r_m32, ECX, EAX, EBX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET);   // lo
            asm.emit2_Reg_Mem(x86.MOV_r_m32, EBX, EAX, EBX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET+4); // hi
            break;
        default: Assert.UNREACHABLE(); break;
        }
        storeOperand(ALoad.getDest(obj), ECX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitAStore(joeq.Compiler.Quad.Quad)
     */
    public void visitAStore(Quad obj) {
        if (TRACE) System.out.println(this+" AStore: "+obj);
        
        loadOperand(AStore.getBase(obj), EAX);
        loadOperand(AStore.getIndex(obj), EDX);
        loadOperand(AStore.getValue(obj), ECX);
        
        jq_Type t = ((AStore) obj.getOperator()).getType();
        int scale = t.getReferenceSize();
        switch (scale) {
        case 4:
            asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, EAX, EDX, SCALE_4, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            break;
        case 2:
            asm.emitprefix(x86.PREFIX_16BIT);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, EAX, EDX, SCALE_2, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            break;
        case 1:
            asm.emit2_Reg_Mem(x86.MOV_m_r8, ECX, EAX, EDX, SCALE_1, ObjectLayout.ARRAY_ELEMENT_OFFSET);
            break;
        case 8:
            asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, EAX, EDX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET  ); // lo
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, EAX, EDX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET+4); // hi
            break;
        default: Assert.UNREACHABLE(); break;
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitALength(joeq.Compiler.Quad.Quad)
     */
    public void visitALength(Quad obj) {
        if (TRACE) System.out.println(this+" ALength: "+obj);
        loadOperand(ALength.getSrc(obj), EAX);
        asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        storeOperand(ALength.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitBinary(joeq.Compiler.Quad.Quad)
     */
    public void visitBinary(Quad obj) {
        if (TRACE) System.out.println(this+" Binary: "+obj);
        loadOperand(Binary.getSrc1(obj), EAX);
        loadOperand(Binary.getSrc2(obj), EBX);
        Binary op = (Binary) obj.getOperator();
        if (op instanceof Binary.ADD_I || op instanceof Binary.ADD_P) {
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EAX, EBX);
        } else if (op instanceof Binary.SUB_I || op instanceof Binary.SUB_P) {
            asm.emitARITH_Reg_Reg(x86.SUB_r_r32, EAX, EBX);
        } else if (op instanceof Binary.MUL_I) {
            asm.emit2_Reg(x86.IMUL_rda_r32, EBX);
        } else if (op instanceof Binary.DIV_I) {
            asm.emit1(x86.CWD);
            asm.emit2_Reg(x86.IDIV_r32, EBX);
        } else if (op instanceof Binary.REM_I) {
            asm.emit1(x86.CWD);
            asm.emit2_Reg(x86.IDIV_r32, EBX);
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, EDX);
        } else if (op instanceof Binary.AND_I) {
            asm.emitARITH_Reg_Reg(x86.AND_r_r32, EAX, EBX);
        } else if (op instanceof Binary.OR_I) {
            asm.emitARITH_Reg_Reg(x86.OR_r_r32, EAX, EBX);
        } else if (op instanceof Binary.XOR_I) {
            asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EAX, EBX);
        } else if (op instanceof Binary.ADD_L) {
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EAX, EBX);
            asm.emitARITH_Reg_Reg(x86.ADC_r_r32, EDX, ECX);
        } else if (op instanceof Binary.SUB_L) {
            asm.emitARITH_Reg_Reg(x86.SUB_r_r32, EAX, EBX);
            asm.emitARITH_Reg_Reg(x86.SBB_r_r32, EDX, ECX);
        } else if (op instanceof Binary.MUL_L) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ESI, EAX);
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EDI, EDX);
            asm.emitARITH_Reg_Reg(x86.OR_r_r32, EDX, ECX); // hi1 | hi2
            asm.emitCJUMP_Short(x86.JNE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emit2_Reg(x86.MUL_rda_r32, EBX); // lo1*lo2
            asm.emitJUMP_Short(x86.JMP, (byte)0);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            cloc = asm.getCurrentOffset();
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ESI); // lo2
            asm.emit2_Reg(x86.MUL_rda_r32, ECX); // hi1*lo2
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EAX); // hi1*lo2
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, EDI); // hi2
            asm.emit2_Reg(x86.MUL_rda_r32, EBX); // hi2*lo1
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, ECX, EAX); // hi2*lo1 + hi1*lo2
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ESI); // lo2
            asm.emit2_Reg(x86.MUL_rda_r32, EBX); // lo1*lo2
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EDX, ECX); // hi2*lo1 + hi1*lo2 + hi(lo1*lo2)
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
        } else if (op instanceof Binary.DIV_L) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            emitCallRelative(MathSupport._ldiv);
        } else if (op instanceof Binary.REM_L) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            emitCallRelative(MathSupport._lrem);
        } else if (op instanceof Binary.AND_L) {
            asm.emitARITH_Reg_Reg(x86.AND_r_r32, EAX, EBX);
            asm.emitARITH_Reg_Reg(x86.AND_r_r32, EDX, ECX);
        } else if (op instanceof Binary.OR_L) {
            asm.emitARITH_Reg_Reg(x86.OR_r_r32, EAX, EBX);
            asm.emitARITH_Reg_Reg(x86.OR_r_r32, EDX, ECX);
        } else if (op instanceof Binary.XOR_L) {
            asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EAX, EBX);
            asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EDX, ECX);
        } else if (op instanceof Binary.ADD_F) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
            asm.emit2_Mem(x86.FADD_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Binary.SUB_F) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
            asm.emit2_Mem(x86.FSUB_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Binary.MUL_F) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
            asm.emit2_Mem(x86.FMUL_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Binary.DIV_F) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
            asm.emit2_Mem(x86.FDIV_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Binary.REM_F) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP); // reverse because pushing on fp stack
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
            asm.emit2(x86.FPREM);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emit2_FPReg(x86.FFREE, 0);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Binary.ADD_D) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
            asm.emit2_Mem(x86.FADD_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Binary.SUB_D) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
            asm.emit2_Mem(x86.FSUB_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Binary.MUL_D) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
            asm.emit2_Mem(x86.FMUL_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Binary.DIV_D) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
            asm.emit2_Mem(x86.FDIV_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Binary.REM_D) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
            asm.emit2(x86.FPREM);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emit2_FPReg(x86.FFREE, 0);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Binary.SHL_I) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emit2_Reg(x86.SHL_r32_rc, EAX);
        } else if (op instanceof Binary.SHR_I) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emit2_Reg(x86.SAR_r32_rc, EAX);
        } else if (op instanceof Binary.USHR_I) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emit2_Reg(x86.SHR_r32_rc, EAX);
        } else if (op instanceof Binary.SHL_L) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emitARITH_Reg_Imm(x86.AND_r_i32, ECX, 63);
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, ECX, 32);
            asm.emitCJUMP_Short(x86.JAE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitSHLD_r_r_rc(EDX, EAX);
            asm.emit2_Reg(x86.SHL_r32_rc, EAX);
            asm.emitJUMP_Short(x86.JMP, (byte)0);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            cloc = asm.getCurrentOffset();
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EDX, EAX);
            asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EAX, EAX);
            asm.emit2_Reg(x86.SHL_r32_rc, EDX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
        } else if (op instanceof Binary.SHR_L) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emitARITH_Reg_Imm(x86.AND_r_i32, ECX, 63);
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, ECX, 32);
            asm.emitCJUMP_Short(x86.JAE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitSHRD_r_r_rc(EAX, EDX);
            asm.emit2_Reg(x86.SAR_r32_rc, EDX);
            asm.emitJUMP_Short(x86.JMP, (byte)0);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            cloc = asm.getCurrentOffset();
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, EDX);
            asm.emit2_SHIFT_Reg_Imm8(x86.SAR_r32_i, EDX, (byte)31);
            asm.emit2_Reg(x86.SAR_r32_rc, EAX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
        } else if (op instanceof Binary.USHR_L) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emitARITH_Reg_Imm(x86.AND_r_i32, ECX, 63);
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, ECX, 32);
            asm.emitCJUMP_Short(x86.JAE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitSHRD_r_r_rc(EAX, EDX);
            asm.emit2_Reg(x86.SHR_r32_rc, EDX);
            asm.emitJUMP_Short(x86.JMP, (byte)0);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            cloc = asm.getCurrentOffset();
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, EDX);
            asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EDX, EDX);
            asm.emit2_Reg(x86.SHR_r32_rc, EAX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
        } else if (op instanceof Binary.CMP_L) {
            asm.emitARITH_Reg_Reg(x86.SUB_r_r32, EAX, EBX);
            asm.emitARITH_Reg_Reg(x86.SBB_r_r32, EDX, ECX);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, -1);
            asm.emitCJUMP_Short(x86.JL, (byte)0);
            int cloc1 = asm.getCurrentOffset();
            asm.emitARITH_Reg_Reg(x86.XOR_r_r32, ECX, ECX);
            asm.emitARITH_Reg_Reg(x86.OR_r_r32, EAX, EDX);
            asm.emitCJUMP_Short(x86.JE, (byte)0);
            int cloc2 = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
            asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ECX);
        } else if (op instanceof Binary.CMP_FL) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
            asm.emit2(x86.FUCOMPP);
            asm.emit2(x86.FNSTSW_ax);
            asm.emit1(x86.SAHF);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, -1);
            asm.emitCJUMP_Short(x86.JB, (byte)0);
            int cloc1 = asm.getCurrentOffset();
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JE, (byte)0);
            int cloc2 = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
            asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ECX);
        } else if (op instanceof Binary.CMP_FG) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP); // reverse order
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2(x86.FUCOMPP);
            asm.emit2(x86.FNSTSW_ax);
            asm.emit1(x86.SAHF);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 1);
            asm.emitCJUMP_Short(x86.JB, (byte)0);
            int cloc1 = asm.getCurrentOffset();
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JE, (byte)0);
            int cloc2 = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.DEC_r32, ECX);
            asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
            asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ECX);
        } else if (op instanceof Binary.CMP_DL) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
            asm.emit2(x86.FUCOMPP);
            asm.emit2(x86.FNSTSW_ax);
            asm.emit1(x86.SAHF);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 16, ESP);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, -1);
            asm.emitCJUMP_Short(x86.JB, (byte)0);
            int cloc1 = asm.getCurrentOffset();
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JE, (byte)0);
            int cloc2 = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
            asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ECX);
        } else if (op instanceof Binary.CMP_DG) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP); // reverse order
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2(x86.FUCOMPP);
            asm.emit2(x86.FNSTSW_ax);
            asm.emit1(x86.SAHF);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 16, ESP);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 1);
            asm.emitCJUMP_Short(x86.JB, (byte)0);
            int cloc1 = asm.getCurrentOffset();
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JE, (byte)0);
            int cloc2 = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.DEC_r32, ECX);
            asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
            asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ECX);
        } else if (op instanceof Binary.ALIGN_P) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ECX, EBX);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, EBX, 1);
            asm.emit2_Reg(x86.SHL_r32_rc, EBX);
            asm.emitShort_Reg(x86.DEC_r32, EBX);
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EAX, EBX);
            asm.emit2_Reg(x86.NOT_r32, EBX);
            asm.emitARITH_Reg_Reg(x86.AND_r_r32, EAX, EBX);
        }
        else {
            Assert.UNREACHABLE(obj.toString());
        }
        storeOperand(Binary.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitBoundsCheck(joeq.Compiler.Quad.Quad)
     */
    public void visitBoundsCheck(Quad obj) {
        if (TRACE) System.out.println(this+" BoundsCheck: "+obj);
        loadOperand(BoundsCheck.getRef(obj), EAX);
        loadOperand(BoundsCheck.getIndex(obj), EBX);
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitCheckCast(joeq.Compiler.Quad.Quad)
     */
    public void visitCheckCast(Quad obj) {
        if (TRACE) System.out.println(this+" CheckCast: "+obj);
        loadOperand(CheckCast.getSrc(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        jq_Type f = CheckCast.getType(obj).getType();
        emitPushAddressOf(f);
        emitCallRelative(TypeCheck._checkcast);
        storeOperand(CheckCast.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitGetfield(joeq.Compiler.Quad.Quad)
     */
    public void visitGetfield(Quad obj) {
        if (TRACE) System.out.println(this+" Getfield: "+obj);
        jq_InstanceField f = (jq_InstanceField) Getfield.getField(obj).getField();
        loadOperand(Getfield.getBase(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        if (f.getWidth() == 1) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 9
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._getfield1);
                asm.endDynamicPatch();
            } else {
                asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
                asm.emit3_Reg_Mem(x86.MOVSX_r_m8, EBX, f.getOffset(), EAX);
                asm.emitShort_Reg(x86.PUSH_r, EBX);
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (f.getWidth() == 4) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 7
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._getfield4);
                asm.endDynamicPatch();
            } else {
                asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
                asm.emit2_Mem(x86.PUSH_m, f.getOffset(), EAX);
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (f.getWidth() == 8) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(13);
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._getfield8);
                asm.endDynamicPatch();
            } else {
                asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
                asm.emit2_Mem(x86.PUSH_m, f.getOffset()+4, EAX); // hi
                asm.emit2_Mem(x86.PUSH_m, f.getOffset(), EAX);   // lo
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (f.getType() == jq_Primitive.SHORT) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 9
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._sgetfield);
                asm.endDynamicPatch();
            } else {
                asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
                asm.emit3_Reg_Mem(x86.MOVSX_r_m16, EBX, f.getOffset(), EAX);
                asm.emitShort_Reg(x86.PUSH_r, EBX);
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (f.getType() == jq_Primitive.CHAR) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 9
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._cgetfield);
                asm.endDynamicPatch();
            } else {
                asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
                asm.emit3_Reg_Mem(x86.MOVZX_r_m16, EBX, f.getOffset(), EAX);
                asm.emitShort_Reg(x86.PUSH_r, EBX);
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
        }
        else {
            Assert.UNREACHABLE();
        }
        storeOperand(Getfield.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitGetstatic(joeq.Compiler.Quad.Quad)
     */
    public void visitGetstatic(Quad obj) {
        if (TRACE) System.out.println(this+" Getstatic: "+obj);
        jq_StaticField f = (jq_StaticField) Getstatic.getField(obj).getField();
        if (f.getWidth() == 8) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(12); // 6
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._getstatic8);
                asm.endDynamicPatch();
            } else {
                emitPushMemory(f);
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 6
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._getstatic4);
                asm.endDynamicPatch();
            } else {
                emitPushMemory(f);
            }
            asm.emitShort_Reg(x86.POP_r, EAX);
        }
        storeOperand(Getstatic.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitGoto(joeq.Compiler.Quad.Quad)
     */
    public void visitGoto(Quad obj) {
        if (TRACE) System.out.println(this+" Goto: "+obj);
        branchHelper(BytecodeVisitor.CMP_UNCOND, Goto.getTarget(obj).getTarget());
        this.handled = true;
    }
    private void branchHelper(byte op, BasicBlock target) {
        if (op == BytecodeVisitor.CMP_UNCOND) {
            if (asm.containsTarget(target))
                asm.emitJUMP_Back(x86.JMP, target);
            else
                asm.emitJUMP_Forw(x86.JMP, target);
        } else {
            x86 opc = null;
            switch(op) {
                case BytecodeVisitor.CMP_EQ: opc = x86.JE; break;
                case BytecodeVisitor.CMP_NE: opc = x86.JNE; break;
                case BytecodeVisitor.CMP_LT: opc = x86.JL; break;
                case BytecodeVisitor.CMP_GE: opc = x86.JGE; break;
                case BytecodeVisitor.CMP_LE: opc = x86.JLE; break;
                case BytecodeVisitor.CMP_GT: opc = x86.JG; break;
                case BytecodeVisitor.CMP_AE: opc = x86.JAE; break;
                default: Assert.UNREACHABLE();
            }
            if (asm.containsTarget(target))
                asm.emitCJUMP_Back(opc, target);
            else
                asm.emitCJUMP_Forw(opc, target);
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitInstanceOf(joeq.Compiler.Quad.Quad)
     */
    public void visitInstanceOf(Quad obj) {
        if (TRACE) System.out.println(this+" InstanceOf: "+obj);
        loadOperand(InstanceOf.getSrc(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        jq_Type f = InstanceOf.getType(obj).getType();
        emitPushAddressOf(f);
        emitCallRelative(TypeCheck._instance_of);
        storeOperand(InstanceOf.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitIntIfCmp(joeq.Compiler.Quad.Quad)
     */
    public void visitIntIfCmp(Quad obj) {
        if (TRACE) System.out.println(this+" IntIfCmp: "+obj);
        loadOperand(IntIfCmp.getSrc1(obj), EAX);
        loadOperand(IntIfCmp.getSrc2(obj), ECX);
        asm.emitARITH_Reg_Reg(x86.CMP_r_r32, EAX, ECX);
        branchHelper(IntIfCmp.getCond(obj).getCondition(), IntIfCmp.getTarget(obj).getTarget());
        this.handled = true;
    }
    private void INVOKEDPATCHhelper(byte op, jq_Method f) {
        int dpatchsize;
        jq_StaticMethod dpatchentry;
        switch (op) {
            case BytecodeVisitor.INVOKE_VIRTUAL:
                dpatchsize = 16;
                dpatchentry = x86ReferenceLinker._invokevirtual;
                break;
            case BytecodeVisitor.INVOKE_STATIC:
                dpatchsize = 11; // 5
                dpatchentry = x86ReferenceLinker._invokestatic;
                break;
            case BytecodeVisitor.INVOKE_SPECIAL:
                dpatchsize = 11; // 5
                dpatchentry = x86ReferenceLinker._invokespecial;
                break;
            case BytecodeVisitor.INVOKE_INTERFACE:
                // fallthrough
            default:
                throw new InternalError();
        }
        // generate a runtime call, which will be backpatched.
        asm.startDynamicPatch(dpatchsize);
        emitPushAddressOf(f);
        emitCallRelative(dpatchentry);
        asm.endDynamicPatch();
        this.handled = true;
    }
    private void INVOKENODPATCHhelper(byte op, jq_Method f) {
        switch(op) {
            case BytecodeVisitor.INVOKE_VIRTUAL: {
                int objptroffset = (f.getParamWords() << 2) - 4;
                int m_off = ((jq_InstanceMethod)f).getOffset();
                asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, objptroffset, ESP); // 7
                asm.emit2_Reg_Mem(x86.MOV_r_m32, EBX, ObjectLayout.VTABLE_OFFSET, EAX); // 3
                asm.emit2_Mem(x86.CALL_m, m_off, EBX); // 6
                break;
            }
            case BytecodeVisitor.INVOKE_SPECIAL:
                f = jq_Class.getInvokespecialTarget(method.getDeclaringClass(), (jq_InstanceMethod)f);
                emitCallRelative(f);
                break;
            case BytecodeVisitor.INVOKE_STATIC:
                emitCallRelative(f);
                break;
            case BytecodeVisitor.INVOKE_INTERFACE:
                //jq.Assert(!jq.RunningNative || f.getDeclaringClass().isInterface());
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._invokeinterface);
                // need to pop args ourselves.
                asm.emit2_Reg_Mem(x86.LEA, ESP, f.getParamWords()<<2, ESP);
                break;
            default:
                Assert.UNREACHABLE();
        }
        this.handled = true;
    }
    private void INVOKEhelper(byte op, jq_Method f) {
        switch (op) {
            case BytecodeVisitor.INVOKE_VIRTUAL:
                if (f.needsDynamicLink(method))
                    INVOKEDPATCHhelper(op, f);
                else
                    INVOKENODPATCHhelper(op, f);
                break;
            case BytecodeVisitor.INVOKE_STATIC:
                // fallthrough
            case BytecodeVisitor.INVOKE_SPECIAL:
                if (f.needsDynamicLink(method))
                    INVOKEDPATCHhelper(op, f);
                else
                    INVOKENODPATCHhelper(op, f);
                break;
            case BytecodeVisitor.INVOKE_INTERFACE:
                INVOKENODPATCHhelper(op, f);
                break;
            default:
                throw new InternalError();
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitInvoke(joeq.Compiler.Quad.Quad)
     */
    public void visitInvoke(Quad obj) {
        if (TRACE) System.out.println(this+" Invoke: "+obj);
        byte type = ((Invoke)obj.getOperator()).getType();
        ParamListOperand plo = Invoke.getParamList(obj);
        for (int i=0; i<plo.length(); ++i) {
            RegisterOperand r = plo.get(i);
            loadOperand(r, EAX);
            if (r.getType().getReferenceSize() == 8)
                asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        }
        jq_Method f = Invoke.getMethod(obj).getMethod();
        INVOKEhelper(type, f);
        
        // clean up extra pushed arguments for varargs methods like multinewarray.
        int diff = plo.words() - f.getParamWords();
        if (diff != 0)
            asm.emit2_Reg_Mem(x86.LEA, ESP, diff<<2, ESP);
        
        jq_Type t = ((Invoke)obj.getOperator()).getReturnType();
        if (t.getReferenceSize() > 0) {
            storeOperand(Invoke.getDest(obj), EAX);
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitJsr(joeq.Compiler.Quad.Quad)
     */
    public void visitJsr(Quad obj) {
        if (TRACE) System.out.println(this+" Jsr: "+obj);
        BasicBlock succ = Jsr.getSuccessor(obj).getTarget();
        if (asm.containsTarget(succ)) {
            int offset = asm.getBranchTarget(succ);
            CodeAddress a = (CodeAddress) asm.getStartAddress().offset(offset);
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, EAX, a.to32BitValue());
            Code2CodeReference r = new Code2CodeReference((CodeAddress) asm.getCurrentAddress().offset(-4), a);
            code_relocs.add(r);
        } else {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, EAX, 0x77777777);
            asm.recordAbsoluteReference(4, succ);
            // hack: null references don't actually get written, so this works.
            Code2CodeReference r = new Code2CodeReference((CodeAddress) asm.getCurrentAddress().offset(-4), null);
            code_relocs.add(r);
        }
        storeOperand(Jsr.getDest(obj), EAX);
        branchHelper(BytecodeVisitor.CMP_UNCOND, Jsr.getTarget(obj).getTarget());
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitLookupSwitch(joeq.Compiler.Quad.Quad)
     */
    public void visitLookupSwitch(Quad obj) {
        if (TRACE) System.out.println(this+" LookupSwitch: "+obj);
        loadOperand(LookupSwitch.getSrc(obj), EAX);
        int n = LookupSwitch.getSize(obj);
        for (int i=0; i<n; ++i) {
            int match = LookupSwitch.getMatch(obj, i);
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, match);
            BasicBlock target = LookupSwitch.getTarget(obj, i);
            branchHelper(BytecodeVisitor.CMP_EQ, target);
        }
        branchHelper(BytecodeVisitor.CMP_UNCOND, LookupSwitch.getDefault(obj).getTarget());
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitMemLoad(joeq.Compiler.Quad.Quad)
     */
    public void visitMemLoad(Quad obj) {
        if (TRACE) System.out.println(this+" MemLoad: "+obj);
        loadOperand(MemLoad.getAddress(obj), EAX);
        MemLoad op = (MemLoad) obj.getOperator();
        if (op instanceof MemLoad.PEEK_1) {
            asm.emit3_Reg_Mem(x86.MOVSX_r_m8, ECX, 0, EAX);
        } else if (op instanceof MemLoad.PEEK_2) {
            asm.emit3_Reg_Mem(x86.MOVSX_r_m16, ECX, 0, EAX);
        } else if (op instanceof MemLoad.PEEK_4) {
            asm.emit2_Reg_Mem(x86.MOV_r_m32, ECX, 0, EAX);
        } else if (op instanceof MemLoad.PEEK_8) {
            asm.emit2_Reg_Mem(x86.MOV_r_m32, ECX, 0, EAX);
            asm.emit2_Reg_Mem(x86.MOV_r_m32, EBX, 4, EAX);
        } else if (op instanceof MemLoad.PEEK_P) {
            asm.emit2_Reg_Mem(x86.MOV_r_m32, ECX, 0, EAX);
        }
        else {
            Assert.UNREACHABLE(obj.toString());
        }
        storeOperand(MemLoad.getDest(obj), ECX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitMemStore(joeq.Compiler.Quad.Quad)
     */
    public void visitMemStore(Quad obj) {
        if (TRACE) System.out.println(this+" MemStore: "+obj);
        loadOperand(MemStore.getAddress(obj), EAX);
        loadOperand(MemStore.getValue(obj), EBX);
        MemStore op = (MemStore) obj.getOperator();
        if (op instanceof MemStore.POKE_1) {
            asm.emit2_Reg_Mem(x86.MOV_m_r8, EBX, 0, EAX);
        } else if (op instanceof MemStore.POKE_2) {
            asm.emitprefix(x86.PREFIX_16BIT);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
        } else if (op instanceof MemStore.POKE_4) {
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
        } else if (op instanceof MemStore.POKE_8) {
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, 4, EAX);
        } else if (op instanceof MemStore.POKE_P) {
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
        }
        else {
            Assert.UNREACHABLE(obj.toString());
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitMonitor(joeq.Compiler.Quad.Quad)
     */
    public void visitMonitor(Quad obj) {
        if (TRACE) System.out.println(this+" Monitor: "+obj);
        loadOperand(Monitor.getSrc(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        jq_StaticMethod m = (obj.getOperator() instanceof Monitor.MONITORENTER)?joeq.Runtime.Monitor._monitorenter:joeq.Runtime.Monitor._monitorexit;
        emitCallRelative(m);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitMove(joeq.Compiler.Quad.Quad)
     */
    public void visitMove(Quad obj) {
        if (TRACE) System.out.println(this+" Move: "+obj);
        //Move o = (Move) obj.getOperator();
        RegisterOperand dest_o = Move.getDest(obj);
        Operand src_o = Move.getSrc(obj);
        loadOperand(src_o, EAX);
        storeOperand(dest_o, EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitPhi(joeq.Compiler.Quad.Quad)
     */
    public void visitPhi(Quad obj) {
        if (TRACE) System.out.println(this+" Phi: "+obj);
        Assert.UNREACHABLE();
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitNew(joeq.Compiler.Quad.Quad)
     */
    public void visitNew(Quad obj) {
        if (TRACE) System.out.println(this+" New: "+obj);
        jq_Type f = New.getType(obj).getType();
        if (f.isClassType() && !f.needsDynamicLink(method)) {
            jq_Class k = (jq_Class)f;
            asm.emitPUSH_i(k.getInstanceSize());
            emitPushAddressOf(k.getVTable());
            emitCallRelative(DefaultHeapAllocator._allocateObject);
        } else {
            emitPushAddressOf(f);
            emitCallRelative(HeapAllocator._clsinitAndAllocateObject);
        }
        storeOperand(New.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitNewArray(joeq.Compiler.Quad.Quad)
     */
    public void visitNewArray(Quad obj) {
        if (TRACE) System.out.println(this+" NewArray: "+obj);
        jq_Array f = (jq_Array) NewArray.getType(obj).getType();
        // initialize type now, to avoid backpatch.
        if (!jq.RunningNative) {
            //jq.Assert(jq.boot_types.contains(f), f.toString());
        } else {
            f.cls_initialize();
        }
        byte width = f.getLogElementSize();
        loadOperand(NewArray.getSize(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        if (width != 0) asm.emit2_SHIFT_Mem_Imm8(x86.SHL_m32_i, 0, ESP, width);
        asm.emitARITH_Mem_Imm(x86.ADD_m_i32, 0, ESP, ObjectLayout.ARRAY_HEADER_SIZE);
        emitPushAddressOf(f.getVTable());
        emitCallRelative(DefaultHeapAllocator._allocateArray);
        storeOperand(NewArray.getDest(obj), EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitNullCheck(joeq.Compiler.Quad.Quad)
     */
    public void visitNullCheck(Quad obj) {
        if (TRACE) System.out.println(this+" NullCheck: "+obj);
        loadOperand(NullCheck.getSrc(obj), EAX);
        asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, 0, EAX);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitPutfield(joeq.Compiler.Quad.Quad)
     */
    public void visitPutfield(Quad obj) {
        if (TRACE) System.out.println(this+" Putfield: "+obj);
        jq_InstanceField f = (jq_InstanceField) Putfield.getField(obj).getField();
        loadOperand(Putfield.getBase(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        loadOperand(Putfield.getSrc(obj), EAX);
        if (f.getWidth() == 8)
            asm.emitShort_Reg(x86.PUSH_r, EDX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        if (f.getWidth() == 1) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 8
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._putfield1);
                asm.endDynamicPatch();
            } else {
                // field has already been resolved.
                asm.emitShort_Reg(x86.POP_r, EBX);
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit2_Reg_Mem(x86.MOV_m_r8, EBX, f.getOffset(), EAX);
            }
        } else if (f.getWidth() == 4) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 8
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._putfield4);
                asm.endDynamicPatch();
            } else {
                // field has already been resolved.
                asm.emitShort_Reg(x86.POP_r, EBX);
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, f.getOffset(), EAX);
            }
        } else if (f.getWidth() == 8) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(15);
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._putfield8);
                asm.endDynamicPatch();
            } else {
                // field has already been resolved.
                asm.emitShort_Reg(x86.POP_r, EBX); // lo
                asm.emitShort_Reg(x86.POP_r, EAX); // hi
                asm.emitShort_Reg(x86.POP_r, EDX);
                asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, f.getOffset()  , EDX); // lo
                asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, f.getOffset()+4, EDX); // hi
            }
        } else if (f.getWidth() == 2) {
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 9
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._putfield2);
                asm.endDynamicPatch();
            } else {
                // field has already been resolved.
                asm.emitShort_Reg(x86.POP_r, EBX);
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emitprefix(x86.PREFIX_16BIT);
                asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, f.getOffset(), EAX);
            }
        }
        else {
            Assert.UNREACHABLE(obj.toString());
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitPutstatic(joeq.Compiler.Quad.Quad)
     */
    public void visitPutstatic(Quad obj) {
        if (TRACE) System.out.println(this+" Putstatic: "+obj);
        jq_StaticField f = (jq_StaticField) Putstatic.getField(obj).getField();
        loadOperand(Putstatic.getSrc(obj), EAX);
        if (f.getWidth() == 8) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(12);
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._putstatic8);
                asm.endDynamicPatch();
            } else {
                emitPopMemory8(f);
            }
        } else {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            if (f.needsDynamicLink(method)) {
                // generate a runtime call, which will be backpatched.
                asm.startDynamicPatch(10); // 6
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._putstatic4);
                asm.endDynamicPatch();
            } else {
                emitPopMemory(f);
            }
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitRet(joeq.Compiler.Quad.Quad)
     */
    public void visitRet(Quad obj) {
        if (TRACE) System.out.println(this+" Ret: "+obj);
        asm.emit2_Mem(x86.JMP_m, getStackOffset(Ret.getTarget(obj)), EBP);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitReturn(joeq.Compiler.Quad.Quad)
     */
    public void visitReturn(Quad obj) {
        if (TRACE) System.out.println(this+" Return: "+obj);
        Return o = (Return)obj.getOperator();
        if (o instanceof Return.THROW_A) {
            loadOperand(Return.getSrc(obj), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            emitCallRelative(ExceptionDeliverer._athrow);
        }
        if (method.isSynchronized()) SYNCHEXIThelper();
        if (TraceMethods) {
            emitPushAddressOf(SystemInterface.toCString("Leaving: "+method));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if (!(o instanceof Return.RETURN_V)) {
            loadOperand(Return.getSrc(obj), EAX);
        }
        // epilogue
        asm.emit1(x86.LEAVE);              // esp<-ebp, pop ebp
        asm.emit1_Imm16(x86.RET_i, (char)(n_paramwords<<2));
        this.handled = true;
    }
    private void SYNCHEXIThelper() {
        if (method.isStatic()) {
            if (TraceQuads) {
                emitPushAddressOf(SystemInterface.toCString("exit: STATIC SYNCH EXIT"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // lock the java.lang.Class object
            Class c = Reflection.getJDKType(method.getDeclaringClass());
            Assert._assert(c != null);
            emitPushAddressOf(c);
        } else {
            if (TraceQuads) {
                emitPushAddressOf(SystemInterface.toCString("exit: INSTANCE SYNCH EXIT"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // lock the this pointer
            asm.emit2_Mem(x86.PUSH_m, getParamOffset(0), EBP);
        }
        emitCallRelative(joeq.Runtime.Monitor._monitorexit);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitSpecial(joeq.Compiler.Quad.Quad)
     */
    public void visitSpecial(Quad obj) {
        if (TRACE) System.out.println(this+" Special: "+obj);
        Special o = (Special) obj.getOperator();
        if (o instanceof Special.GET_EXCEPTION) {
            // already done.
        } else if (o instanceof Special.GET_BASE_POINTER) {
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, EBP);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.GET_STACK_POINTER) {
            asm.emitShort_Reg(x86.PUSH_r, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.GET_THREAD_BLOCK) {
            asm.emitprefix(THREAD_BLOCK_PREFIX);
            asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, THREAD_BLOCK_OFFSET);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.SET_THREAD_BLOCK) {
            loadOperand(Special.getOp2(obj), EAX);
            asm.emitprefix(THREAD_BLOCK_PREFIX);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, THREAD_BLOCK_OFFSET);
        } else if (o instanceof Special.ALLOCA) {
            loadOperand(Special.getOp2(obj), EAX);
            asm.emit2_Reg(x86.NEG_r32, EAX);
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EAX, ESP);
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ESP, EAX);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.ATOMICADD_I) {
            loadOperand(Special.getOp2(obj), EAX);
            loadOperand(Special.getOp3(obj), EBX);
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emitARITH_Reg_Mem(x86.ADD_m_r32, EBX, 0, EAX);
        } else if (o instanceof Special.ATOMICSUB_I) {
            loadOperand(Special.getOp2(obj), EAX);
            loadOperand(Special.getOp3(obj), EBX);
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emitARITH_Reg_Mem(x86.SUB_m_r32, EBX, 0, EAX);
        } else if (o instanceof Special.ATOMICAND_I) {
            loadOperand(Special.getOp2(obj), EAX);
            loadOperand(Special.getOp3(obj), EBX);
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emitARITH_Reg_Mem(x86.AND_m_r32, EBX, 0, EAX);
        } else if (o instanceof Special.ATOMICCAS4) {
            loadOperand(Special.getOp2(obj), ECX);
            loadOperand(Special.getOp3(obj), EAX);
            loadOperand(Special.getOp4(obj), EBX);
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emit3_Reg_Mem(x86.CMPXCHG_32, EBX, 0, ECX);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.ATOMICCAS8) {
            // untested.
            loadOperand(Special.getOp2(obj), EDI);
            loadOperand(Special.getOp3(obj), EAX);
            loadOperand(Special.getOp4(obj), EDI);
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emit3_Reg_Mem(x86.CMPXCHG8B, EAX, 0, EDI);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.LONG_JUMP) {
            loadOperand(Special.getOp1(obj), ECX);
            loadOperand(Special.getOp3(obj), EBX);
            loadOperand(Special.getOp4(obj), EAX);
            loadOperand(Special.getOp2(obj), EBP);
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ESP, EBX);
            asm.emit2_Reg(x86.JMP_r, ECX);
        } else if (o instanceof Special.POP_FP32) {
            asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.POP_FP64) {
            asm.emit2_Reg_Mem(x86.LEA, ESP, -8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.PUSH_FP32) {
            loadOperand(Special.getOp2(obj), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
        } else if (o instanceof Special.PUSH_FP64) {
            loadOperand(Special.getOp2(obj), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
        } else if (o instanceof Special.GET_EAX) {
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.PUSHARG_I || o instanceof Special.PUSHARG_P) {
            loadOperand(Special.getOp2(obj), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        } else if (o instanceof Special.INVOKE_L || o instanceof Special.INVOKE_P) {
            loadOperand(Special.getOp2(obj), EAX);
            asm.emit2_Reg(x86.CALL_r, EAX);
            storeOperand((RegisterOperand) Special.getOp1(obj), EAX);
        } else if (o instanceof Special.ISEQ) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JNE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            storeOperand((RegisterOperand) Special.getOp1(obj), ECX);
        } else if (o instanceof Special.ISGE) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JL, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            storeOperand((RegisterOperand) Special.getOp1(obj), ECX);
        }
        else {
            Assert.UNREACHABLE(obj.toString());
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitStoreCheck(joeq.Compiler.Quad.Quad)
     */
    public void visitStoreCheck(Quad obj) {
        if (TRACE) System.out.println(this+" StoreCheck: "+obj);
        loadOperand(StoreCheck.getElement(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        loadOperand(StoreCheck.getRef(obj), EAX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        emitCallRelative(TypeCheck._arrayStoreCheck);
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitTableSwitch(joeq.Compiler.Quad.Quad)
     */
    public void visitTableSwitch(Quad obj) {
        if (TRACE) System.out.println(this+" TableSwitch: "+obj);
        int low = TableSwitch.getLow(obj).getValue();
        BasicBlockTableOperand targets = TableSwitch.getTargetTable(obj);
        //int high = low+targets.size()-1;
        loadOperand(TableSwitch.getSrc(obj), EAX);
        if (low != 0)
            asm.emitARITH_Reg_Imm(x86.SUB_r_i32, EAX, low);
        asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, targets.size());
        branchHelper(BytecodeVisitor.CMP_AE, TableSwitch.getDefault(obj).getTarget());
        asm.emitCALL_rel32(x86.CALL_rel32, 0);
        int cloc = asm.getCurrentOffset();
        asm.emitShort_Reg(x86.POP_r, ECX);
        // val from table + abs position in table
        asm.emit2_Reg_Mem(x86.LEA, EDX, ECX, EAX, SCALE_4, 127);
        int cloc2 = asm.getCurrentOffset();
        asm.emitARITH_Reg_Mem(x86.ADD_r_m32, EDX, -4, EDX);
        asm.emit2_Reg(x86.JMP_r, EDX);
        asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc+4));
        for (int i=0; i<targets.size(); ++i) {
            BasicBlock target = targets.get(i);
            if (asm.containsTarget(target)) {
                int offset = asm.getBranchTarget(target) - asm.getCurrentOffset() + 4;
                asm.emitDATA(offset);
            } else {
                asm.emitDATA(0x77777777);
                asm.recordForwardBranch(4, target);
            }
        }
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitUnary(joeq.Compiler.Quad.Quad)
     */
    public void visitUnary(Quad obj) {
        if (TRACE) System.out.println(this+" Unary: "+obj);
        loadOperand(Unary.getSrc(obj), EAX);
        Unary op = (Unary) obj.getOperator();
        if (op instanceof Unary.NEG_I) {
            asm.emit2_Reg(x86.NEG_r32, EAX);
        } else if (op instanceof Unary.NEG_L) {
            asm.emit2_Reg(x86.NEG_r32, EDX);
            asm.emit2_Reg(x86.NEG_r32, EAX);
            asm.emitARITH_Reg_Imm(x86.SBB_r_i32, EDX, 0);
        } else if (op instanceof Unary.NEG_F) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2(x86.FCHS);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Unary.NEG_D) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2(x86.FCHS);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Unary.INT_2LONG) {
            asm.emit1(x86.CWD);
        } else if (op instanceof Unary.INT_2FLOAT) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FILD_m32, 0, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Unary.INT_2DOUBLE) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FILD_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Unary.LONG_2INT) {
        } else if (op instanceof Unary.LONG_2FLOAT) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FILD_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Unary.LONG_2DOUBLE) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FILD_m64, 0, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Unary.FLOAT_2INT) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            toIntHelper();
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Unary.FLOAT_2LONG) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
            toLongHelper();
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Unary.FLOAT_2DOUBLE) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Unary.DOUBLE_2INT) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            toIntHelper();
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Unary.DOUBLE_2LONG) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            toLongHelper();
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitShort_Reg(x86.POP_r, EDX);
        } else if (op instanceof Unary.DOUBLE_2FLOAT) {
            asm.emitShort_Reg(x86.PUSH_r, EDX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
        } else if (op instanceof Unary.INT_2BYTE) {
            asm.emit3_Reg_Reg(x86.MOVSX_r_r8, EAX, AL);
        } else if (op instanceof Unary.INT_2SHORT) {
            asm.emit3_Reg_Reg(x86.MOVSX_r_r16, EAX, AX);
        } else if (op instanceof Unary.INT_2CHAR) {
            asm.emit3_Reg_Reg(x86.MOVZX_r_r16, EAX, AX);
        } else if (op instanceof Unary.FLOAT_2INTBITS) {
            // do nothing.
        } else if (op instanceof Unary.INTBITS_2FLOAT) {
            // do nothing.
        } else if (op instanceof Unary.DOUBLE_2LONGBITS) {
            // do nothing.
        } else if (op instanceof Unary.LONGBITS_2DOUBLE) {
            // do nothing.
        } else if (op instanceof Unary.OBJECT_2ADDRESS) {
            // do nothing.
        } else if (op instanceof Unary.ADDRESS_2OBJECT) {
            // do nothing.
        } else if (op instanceof Unary.INT_2ADDRESS) {
            // do nothing.
        } else if (op instanceof Unary.ADDRESS_2INT) {
            // do nothing.
        } else if (op instanceof Unary.ISNULL_P) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, 0);
            asm.emitCJUMP_Short(x86.JNE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ECX);
        }
        else {
            Assert.UNREACHABLE(obj.toString());
        }
        storeOperand(Binary.getDest(obj), EAX);
        this.handled = true;
    }
    private void toIntHelper() {
        // check for NaN
        emitFLD64(MathSupport._maxint);
        asm.emit2_FPReg(x86.FUCOMIP, 1);
        asm.emitCJUMP_Short(x86.JP, (byte)0);
        int cloc1 = asm.getCurrentOffset();
        // check for >=MAX_INT
        asm.emitCJUMP_Short(x86.JBE, (byte)0);
        int cloc2 = asm.getCurrentOffset();
        // check for <=MIN_INT
        emitFLD64(MathSupport._minint);
        asm.emit2_FPReg(x86.FUCOMIP, 1);
        asm.emitCJUMP_Short(x86.JAE, (byte)0);
        int cloc3 = asm.getCurrentOffset();
        // default case
        asm.emit2_Reg_Mem(x86.LEA, ESP, -8, ESP);
        {   // set rounding mode to round-towards-zero
            asm.emit2_Mem(x86.FNSTCW, 4, ESP);
            asm.emit2_Mem(x86.FNSTCW, 0, ESP);
            asm.emitARITH_Mem_Imm(x86.OR_m_i32, 4, ESP, 0x0c00);
            asm.emit2(x86.FNCLEX);
            asm.emit2_Mem(x86.FLDCW, 4, ESP);
        }
        asm.emit2_Mem(x86.FISTP_m32, 8, ESP);
        {
            // restore fpu control word
            asm.emit2(x86.FNCLEX);
            asm.emit2_Mem(x86.FLDCW, 0, ESP);
        }
        asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
        asm.emitJUMP_Short(x86.JMP, (byte)0);
        int cloc4 = asm.getCurrentOffset();
        asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
        // NaN -> 0
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 0, ESP, 0);
        asm.emitJUMP_Short(x86.JMP, (byte)0);
        int cloc5 = asm.getCurrentOffset();
        asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
        // >=MAX_INT -> MAX_INT
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 0, ESP, Integer.MAX_VALUE);
        asm.emitJUMP_Short(x86.JMP, (byte)0);
        int cloc6 = asm.getCurrentOffset();
        asm.patch1(cloc3-1, (byte)(asm.getCurrentOffset()-cloc3));
        // <=MIN_INT -> MIN_INT
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 0, ESP, Integer.MIN_VALUE);
        asm.patch1(cloc5-1, (byte)(asm.getCurrentOffset()-cloc5));
        asm.patch1(cloc6-1, (byte)(asm.getCurrentOffset()-cloc6));
        asm.emit2_FPReg(x86.FFREE, 0);
        asm.patch1(cloc4-1, (byte)(asm.getCurrentOffset()-cloc4));
        this.handled = true;
    }
    private void toLongHelper() {
        // check for NaN
        emitFLD64(MathSupport._maxlong);
        asm.emit2_FPReg(x86.FUCOMIP, 1);
        asm.emitCJUMP_Short(x86.JP, (byte)0);
        int cloc1 = asm.getCurrentOffset();
        // check for >=MAX_LONG
        asm.emitCJUMP_Short(x86.JBE, (byte)0);
        int cloc2 = asm.getCurrentOffset();
        // check for <=MIN_LONG
        emitFLD64(MathSupport._minlong);
        asm.emit2_FPReg(x86.FUCOMIP, 1);
        asm.emitCJUMP_Short(x86.JAE, (byte)0);
        int cloc3 = asm.getCurrentOffset();
        // default case
        asm.emit2_Reg_Mem(x86.LEA, ESP, -8, ESP);
        {   // set rounding mode to round-towards-zero
            asm.emit2_Mem(x86.FNSTCW, 4, ESP);
            asm.emit2_Mem(x86.FNSTCW, 0, ESP);
            asm.emitARITH_Mem_Imm(x86.OR_m_i32, 4, ESP, 0x0c00);
            asm.emit2(x86.FNCLEX);
            asm.emit2_Mem(x86.FLDCW, 4, ESP);
        }
        asm.emit2_Mem(x86.FISTP_m64, 8, ESP);
        {
            // restore fpu control word
            asm.emit2(x86.FNCLEX);
            asm.emit2_Mem(x86.FLDCW, 0, ESP);
        }
        asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
        asm.emitJUMP_Short(x86.JMP, (byte)0);
        int cloc4 = asm.getCurrentOffset();
        asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
        // NaN -> 0
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 0, ESP, 0);
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 4, ESP, 0);
        asm.emitJUMP_Short(x86.JMP, (byte)0);
        int cloc5 = asm.getCurrentOffset();
        asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
        // >=MAX_LONG -> MAX_LONG
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 0, ESP, (int)Long.MAX_VALUE);
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 4, ESP, (int)(Long.MAX_VALUE>>32));
        asm.emitJUMP_Short(x86.JMP, (byte)0);
        int cloc6 = asm.getCurrentOffset();
        asm.patch1(cloc3-1, (byte)(asm.getCurrentOffset()-cloc3));
        // <=MIN_LONG -> MIN_LONG
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 0, ESP, (int)Long.MIN_VALUE);
        asm.emit2_Mem_Imm(x86.MOV_m_i32, 4, ESP, (int)(Long.MIN_VALUE>>32));
        asm.patch1(cloc5-1, (byte)(asm.getCurrentOffset()-cloc5));
        asm.patch1(cloc6-1, (byte)(asm.getCurrentOffset()-cloc6));
        asm.emit2_FPReg(x86.FFREE, 0);
        asm.patch1(cloc4-1, (byte)(asm.getCurrentOffset()-cloc4));
        this.handled = true;
    }
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitZeroCheck(joeq.Compiler.Quad.Quad)
     */
    public void visitZeroCheck(Quad obj) {
        if (TRACE) System.out.println(this+" ZeroCheck: "+obj);
        asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EAX, EAX);
        asm.emitARITH_Reg_Reg(x86.XOR_r_r32, EDX, EDX);
        loadOperand(ZeroCheck.getSrc(obj), EBX);
        asm.emit2_Reg(x86.IDIV_r32, EBX);
        this.handled = true;
    }
    boolean handled;
    /**
     * @see joeq.Compiler.Quad.QuadVisitor#visitQuad(joeq.Compiler.Quad.Quad)
     */
    public void visitQuad(Quad obj) {
        Assert._assert(handled, obj.toString());
    }

    public static void main(String[] args) {
        HostedVM.initialize();
        CodeAddress.FACTORY = joeq.Bootstrap.BootstrapCodeAddress.FACTORY;
        HeapAddress.FACTORY = joeq.Bootstrap.BootstrapHeapAddress.FACTORY;
        ClassLibInterface.useJoeqClasslib(true);
        CodeAllocator.initializeCompiledMethodMap();
        BootstrapCodeAllocator bca = BootstrapCodeAllocator.DEFAULT;
        DefaultCodeAllocator.default_allocator = bca;
        //CodeAddress.FACTORY = BootstrapCodeAddress.FACTORY = new BootstrapCodeAddressFactory(bca);
        bca.init();
        ObjectTraverser obj_trav = ClassLibInterface.DEFAULT.getObjectTraverser();
        Reflection.obj_trav = obj_trav;
        obj_trav.initialize();
        SinglePassBootImage objmap = SinglePassBootImage.DEFAULT;
        
        // enable allocations
        objmap.enableAllocations();
        
        String className = args[0];
        jq_Class c = (jq_Class) jq_Type.parseType(className);
        c.cls_initialize();
        
        ALWAYS_TRACE = true;
        x86Assembler.TRACE = true;
        
        Iterator i = Arrays.asList(c.getDeclaredStaticMethods()).iterator();
        i = new AppendIterator(i, Arrays.asList(c.getDeclaredInstanceMethods()).iterator());
        while (i.hasNext()) {
            jq_Method m = (jq_Method) i.next();
            if (m.getBytecode() != null) {
                ControlFlowGraph cfg = CodeCache.getCode(m);
                SimpleCompiler compiler = new SimpleCompiler(cfg);
                jq_CompiledCode cc = compiler.compile();
                m.setDefaultCompiledVersion(cc);
            }
        }
    }

}
