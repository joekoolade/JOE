// x86ReferenceCompiler.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Reference.x86;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joeq.Allocator.DefaultHeapAllocator;
import joeq.Allocator.HeapAllocator;
import joeq.Allocator.ObjectLayout;
import joeq.Assembler.Code2HeapReference;
import joeq.Assembler.DirectBindCall;
import joeq.Assembler.x86.x86;
import joeq.Assembler.x86.x86Assembler;
import joeq.Assembler.x86.x86Constants;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_BytecodeMap;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_TryCatch;
import joeq.Class.jq_TryCatchBC;
import joeq.Class.jq_Type;
import joeq.Compiler.CompilationState;
import joeq.Compiler.CompilerInterface;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Runtime.ExceptionDeliverer;
import joeq.Runtime.MathSupport;
import joeq.Runtime.Monitor;
import joeq.Runtime.Reflection;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.TypeCheck;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_x86RegisterState;
import joeq.UTF.Utf8;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: x86ReferenceCompiler.java,v 1.43 2004/12/10 10:52:36 joewhaley Exp $
 */
public class x86ReferenceCompiler extends BytecodeVisitor implements x86Constants, jq_ClassFileConstants {

    static {
        // we are using x86, so initialize the register state factory.
        jq_x86RegisterState.initFactory();
    }
    
    public static class Factory implements CompilerInterface {
        public static final Factory INSTANCE = new Factory();
        public Factory() {}
        public jq_CompiledCode compile(jq_Method m) {
            return new x86ReferenceCompiler(m).compile();
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
    public static final Set TraceBytecode_MethodNames = new HashSet();
    public static final Set TraceBytecode_ClassNames = new HashSet();
    
    public final boolean TraceBytecodes;
    public final boolean TraceMethods;
    public final boolean TraceArguments;
    
    public static final int DEFAULT_ALIGNMENT = 32;
    
    public x86ReferenceCompiler(jq_Method method) {
        super(CompilationState.DEFAULT, method);
        TRACE = ALWAYS_TRACE;
        if (TraceBytecode_MethodNames.contains(method.getName().toString())) {
            TraceBytecodes = true;
            TraceMethods = true;
        } else if (TraceBytecode_ClassNames.contains(method.getDeclaringClass().getName().toString())) {
            TraceBytecodes = true;
            TraceMethods = true;
        } else if (TraceMethod_MethodNames.contains(method.getName().toString())) {
            TraceBytecodes = false;
            TraceMethods = true;
        } else if (TraceMethod_ClassNames.contains(method.getDeclaringClass().getName().toString())) {
            TraceBytecodes = false;
            TraceMethods = true;
        } else {
            TraceBytecodes = false;
            TraceMethods = false;
        }
        TraceArguments = false;
    }
    
    public void init(jq_Method method) {
        Assert._assert(method == this.method);
    }
    
    public String toString() {
        return "x86RC/"+Strings.left(method.getName().toString(), 10);
    }
    
    private x86Assembler asm;   // Assembler to output to.
    private int n_paramwords;   // number of words used by incoming parameters.

    private int getLocalOffset(int local) {
        if (local < n_paramwords) {
            return (n_paramwords-local+1)<<2;
        } else {
            return (n_paramwords-local-1)<<2;
        }
    }
    
    private List code_relocs = new LinkedList();
    private List data_relocs = new LinkedList();
    
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
        if (TRACE_STUBS) System.out.println("x86 Reference Compiler: generating compile stub for "+method);
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
    
    // Generate code for the given method.
    public final jq_CompiledCode compile() {
        if (TRACE) System.out.println("x86 Reference Compiler: compiling "+method);
        
        // temporary kludge: no switching a thread during compilation.
        if (jq.RunningNative)
            Unsafe.getThreadBlock().disableThreadSwitch();
        
        try {
            // initialize stuff
            asm = new x86Assembler(bcs.length, bcs.length*8, 5, DEFAULT_ALIGNMENT);
            asm.skip(5); // space for jump point
            asm.setEntrypoint();
            n_paramwords = method.getParamWords();
            int n_localwords = method.getMaxLocals();
            Assert._assert(n_paramwords <= n_localwords);
            
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
            if (n_paramwords != n_localwords)
                asm.emit2_Reg_Mem(x86.LEA, ESP, (n_paramwords-n_localwords)<<2, ESP);
            
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
            // 50:     | callee's opstack   |
            //         |        ...         |
    
            // print a debug message
            if (TraceMethods) {
                emitPushAddressOf(SystemInterface.toCString("Entering: "+method));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            /*
            if (TraceArguments) {
                for (int i=0,j=0; i<params.length; ++i,++j) {
                    emitPushAddressOf(SystemInterface.toCString("Arg"+i+" type "+params[i]+": "));
                    emitCallMemory(SystemInterface._debugwriteln);
                    asm.emit2_Mem(x86.PUSH_m, getLocalOffset(j), EBP);
                    if (params[i] == jq_Primitive.LONG) {
                        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(++j), EBP);
                        emitCallRelative(jq._hex16);
                    } else if (params[i] == jq_Primitive.DOUBLE) {
                        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(++j), EBP);
                        emitCallRelative(Strings._hex16);
                    } else {
                        emitCallRelative(Strings._hex8);
                    }
                }
            }
             */
            
            // add a sentinel value to the bottom of the opstack
            asm.emitPUSH_i(0x0000d00d);
            
            // add monitorenter for synchronized methods.
            if (method.isSynchronized()) {
                if (method.isStatic()) {
                    if (TraceBytecodes) {
                        emitPushAddressOf(SystemInterface.toCString("entry: STATIC SYNCH ENTER"));
                        emitCallMemory(SystemInterface._debugwriteln);
                    }
                    // lock the java.lang.Class object
                    Class c = Reflection.getJDKType(method.getDeclaringClass());
                    Assert._assert(c != null);
                    emitPushAddressOf(c);
                } else {
                    if (TraceBytecodes) {
                        emitPushAddressOf(SystemInterface.toCString("entry: INSTANCE SYNCH ENTER"));
                        emitCallMemory(SystemInterface._debugwriteln);
                    }
                    // lock the this pointer
                    asm.emit2_Mem(x86.PUSH_m, getLocalOffset(0), EBP);
                }
                emitCallRelative(Monitor._monitorenter);
            }
            
            // generate code for each bytecode in order
            this.forwardTraversal();
    
            // record the end of the code as a branch target --- it may
            // be referenced by an exception handler.
            Integer loc = new Integer(bcs.length);
            asm.recordBranchTarget(loc);
    
            // generate exception table
            jq_TryCatchBC[] tcs_bc = method.getExceptionTable();
            jq_TryCatch[] tcs = new jq_TryCatch[tcs_bc.length];
            for (int i=0; i<tcs_bc.length; ++i) {
                jq_TryCatchBC tc_bc = tcs_bc[i];
                Integer start = new Integer(tc_bc.getStartPC());
                Integer end = new Integer(tc_bc.getEndPC());
                Integer handler = new Integer(tc_bc.getHandlerPC());
                jq_Class extype = tc_bc.getExceptionType();
                int offset = ((n_paramwords-n_localwords)<<2) - 4;
                tcs[i] = new jq_TryCatch(asm.getBranchTarget(start), asm.getBranchTarget(end),
                                         asm.getBranchTarget(handler), extype, offset);
            }
            
            // generate bytecode map
            Map m = asm.getBranchTargetMap();
            int numOfBC = m.size();
            int[] offsets = new int[numOfBC];
            int[] bcs = new int[numOfBC];
            ArrayList keySet = new ArrayList(m.keySet());
            java.util.Collections.sort(keySet);
            Iterator it = keySet.iterator();
            for (int i=0; i<numOfBC; ++i) {
                Integer bc = (Integer)it.next();
                bcs[i] = bc.intValue();
                offsets[i] = ((Integer)m.get(bc)).intValue();
            }
            jq_BytecodeMap bcm = new jq_BytecodeMap(offsets, bcs);
            
            // return generated code
            jq_CompiledCode code;
            code = asm.getCodeBuffer().allocateCodeBlock(method, tcs, bcm,
                                                         x86ReferenceExceptionDeliverer.INSTANCE,
                                                         (n_paramwords-n_localwords)<<2,
                                                         code_relocs, data_relocs);
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
    
    public void visitBytecode() throws VerifyError {
        Integer loc = new Integer(i_start);
        asm.recordBranchTarget(loc);
        asm.resolveForwardBranches(loc);
        // do dispatch
        super.visitBytecode();
    }
    
    public void visitNOP() {
        super.visitNOP();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": NOP"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit1(x86.NOP);
    }
    public void visitACONST(Object s) {
        super.visitACONST(s);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ACONST"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        emitPushAddressOf(s);
    }
    public void visitICONST(int c) {
        super.visitICONST(c);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ICONST "+c));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitPUSH_i(c);
    }
    public void visitLCONST(long c) {
        super.visitLCONST(c);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LCONST "+c));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitPUSH_i((int)(c>>32)); // hi
        asm.emitPUSH_i((int)c);       // lo
    }
    public void visitFCONST(float c) {
        super.visitFCONST(c);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FCONST "+c));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitPUSH_i(Float.floatToRawIntBits(c));
    }
    public void visitDCONST(double c) {
        super.visitDCONST(c);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DCONST "+c));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        long v = Double.doubleToRawLongBits(c);
        asm.emitPUSH_i((int)(v>>32)); // hi
        asm.emitPUSH_i((int)v);       // lo
    }
    public void visitILOAD(int i) {
        super.visitILOAD(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ILOAD "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i), EBP);
    }
    public void visitLLOAD(int i) {
        super.visitLLOAD(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LLOAD "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i), EBP);   // hi
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i+1), EBP); // lo
    }
    public void visitFLOAD(int i) {
        super.visitFLOAD(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FLOAD "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i), EBP);
    }
    public void visitDLOAD(int i) {
        super.visitDLOAD(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DLOAD "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i), EBP);   // hi
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i+1), EBP); // lo
    }
    public void visitALOAD(int i) {
        super.visitALOAD(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ALOAD "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.PUSH_m, getLocalOffset(i), EBP);
    }
    public void visitISTORE(int i) {
        super.visitISTORE(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ISTORE "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i), EBP);
    }
    public void visitLSTORE(int i) {
        super.visitLSTORE(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LSTORE "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i+1), EBP); // lo
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i), EBP);   // hi
    }
    public void visitFSTORE(int i) {
        super.visitFSTORE(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FSTORE "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i), EBP);
    }
    public void visitDSTORE(int i) {
        super.visitDSTORE(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DSTORE "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i+1), EBP); // lo
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i), EBP);   // hi
    }
    public void visitASTORE(int i) {
        super.visitASTORE(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ASTORE "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.POP_m, getLocalOffset(i), EBP);
    }
    private void ALOAD4helper() {
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit2_Mem(x86.PUSH_m, EAX, EBX, SCALE_4, ObjectLayout.ARRAY_ELEMENT_OFFSET);
    }
    private void ALOAD8helper() {
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit2_Mem(x86.PUSH_m, EAX, EBX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET+4); // hi
        asm.emit2_Mem(x86.PUSH_m, EAX, EBX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET  ); // lo
    }
    public void visitIALOAD() {
        super.visitIALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ALOAD4helper();
    }
    public void visitLALOAD() {
        super.visitLALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ALOAD8helper();
    }
    public void visitFALOAD() {
        super.visitFALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ALOAD4helper();
    }
    public void visitDALOAD() {
        super.visitDALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ALOAD8helper();
    }
    public void visitAALOAD() {
        super.visitAALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": AALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ALOAD4helper();
    }
    public void visitBALOAD() {
        super.visitBALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": BALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit3_Reg_Mem(x86.MOVSX_r_m8, ECX, EAX, EBX, SCALE_1, ObjectLayout.ARRAY_ELEMENT_OFFSET);
        asm.emitShort_Reg(x86.PUSH_r, ECX);
    }
    public void visitCALOAD() {
        super.visitCALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": CALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit3_Reg_Mem(x86.MOVZX_r_m16, ECX, EAX, EBX, SCALE_2, ObjectLayout.ARRAY_ELEMENT_OFFSET);
        asm.emitShort_Reg(x86.PUSH_r, ECX);
    }
    public void visitSALOAD() {
        super.visitSALOAD();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": SALOAD"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit3_Reg_Mem(x86.MOVSX_r_m16, ECX, EAX, EBX, SCALE_2, ObjectLayout.ARRAY_ELEMENT_OFFSET);
        asm.emitShort_Reg(x86.PUSH_r, ECX);
    }
    private void ASTORE2helper() {
        asm.emitShort_Reg(x86.POP_r, ECX);   // value
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emitprefix(x86.PREFIX_16BIT);
        asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, EAX, EBX, SCALE_2, ObjectLayout.ARRAY_ELEMENT_OFFSET);
    }
    private void ASTORE4helper() {
        asm.emitShort_Reg(x86.POP_r, ECX);   // value
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, EAX, EBX, SCALE_4, ObjectLayout.ARRAY_ELEMENT_OFFSET);
    }
    private void ASTORE8helper() {
        asm.emitShort_Reg(x86.POP_r, ECX);   // lo value
        asm.emitShort_Reg(x86.POP_r, EDX);   // hi value
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, EAX, EBX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET  ); // lo
        asm.emit2_Reg_Mem(x86.MOV_m_r32, EDX, EAX, EBX, SCALE_8, ObjectLayout.ARRAY_ELEMENT_OFFSET+4); // hi
    }
    public void visitIASTORE() {
        super.visitIASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ASTORE4helper();
    }
    public void visitLASTORE() {
        super.visitLASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ASTORE8helper();
    }
    public void visitFASTORE() {
        super.visitFASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ASTORE4helper();
    }
    public void visitDASTORE() {
        super.visitDASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ASTORE8helper();
    }
    public void visitAASTORE() {
        super.visitAASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": AASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        // call arraystorecheck
        asm.emit2_Mem(x86.PUSH_m, 0, ESP);  // push value
        asm.emit2_Mem(x86.PUSH_m, 12, ESP);  // push arrayref
        emitCallRelative(TypeCheck._arrayStoreCheck);
        ASTORE4helper();
    }
    public void visitBASTORE() {
        super.visitBASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": BASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, ECX);   // value
        asm.emitShort_Reg(x86.POP_r, EBX);   // array index
        asm.emitShort_Reg(x86.POP_r, EAX);   // array ref
        asm.emitARITH_Reg_Mem(x86.CMP_r_m32, EBX, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
        asm.emitCJUMP_Short(x86.JB, (byte)2); asm.emit1_Imm8(x86.INT_i8, BOUNDS_EX_NUM);
        asm.emit2_Reg_Mem(x86.MOV_m_r8, ECX, EAX, EBX, SCALE_1, ObjectLayout.ARRAY_ELEMENT_OFFSET);
    }
    public void visitCASTORE() {
        super.visitCASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": CASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ASTORE2helper();
    }
    public void visitSASTORE() {
        super.visitSASTORE();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": SASTORE"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        ASTORE2helper();
    }
    public void visitPOP() {
        super.visitPOP();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": POP"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
    }
    public void visitPOP2() {
        super.visitPOP2();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": POP2"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
    }
    public void visitDUP() {
        super.visitDUP();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUP"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.PUSH_m, 0, ESP);
    }
    public void visitDUP_x1() {
        super.visitDUP_x1();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUP_x1"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitShort_Reg(x86.POP_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitDUP_x2() {
        super.visitDUP_x2();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUP_x2"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitShort_Reg(x86.POP_r, EBX);
        asm.emitShort_Reg(x86.POP_r, ECX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, ECX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitDUP2() {
        super.visitDUP2();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUP2"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitShort_Reg(x86.POP_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitDUP2_x1() {
        super.visitDUP2_x1();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUP2_x1"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitShort_Reg(x86.POP_r, EBX);
        asm.emitShort_Reg(x86.POP_r, ECX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, ECX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitDUP2_x2() {
        super.visitDUP2_x2();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUP2_x2"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitShort_Reg(x86.POP_r, EBX);
        asm.emitShort_Reg(x86.POP_r, ECX);
        asm.emitShort_Reg(x86.POP_r, EDX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, EDX);
        asm.emitShort_Reg(x86.PUSH_r, ECX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitSWAP() {
        super.visitSWAP();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": SWAP"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitShort_Reg(x86.POP_r, EBX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
        asm.emitShort_Reg(x86.PUSH_r, EBX);
    }
    public void visitIBINOP(byte op) {
        super.visitIBINOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IBINOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case BINOP_ADD:
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emitARITH_Reg_Mem(x86.ADD_m_r32, EAX, 0, ESP);
                break;
            case BINOP_SUB:
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emitARITH_Reg_Mem(x86.SUB_m_r32, EAX, 0, ESP); // a-b
                break;
            case BINOP_MUL:
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit2_Mem(x86.IMUL_rda_m32, 0, ESP);
                asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, 0, ESP);
                break;
            case BINOP_DIV:
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit1(x86.CWD);
                asm.emit2_Reg(x86.IDIV_r32, ECX);
                asm.emitShort_Reg(x86.PUSH_r, EAX);
                break;
            case BINOP_REM:
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit1(x86.CWD);
                asm.emit2_Reg(x86.IDIV_r32, ECX);
                asm.emitShort_Reg(x86.PUSH_r, EDX);
                break;
            case BINOP_AND:
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit2_Reg_Mem(x86.AND_m_r32, EAX, 0, ESP);
                break;
            case BINOP_OR:
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit2_Reg_Mem(x86.OR_m_r32, EAX, 0, ESP);
                break;
            case BINOP_XOR:
                asm.emitShort_Reg(x86.POP_r, EAX);
                asm.emit2_Reg_Mem(x86.XOR_m_r32, EAX, 0, ESP);
                break;
            default:
                Assert.UNREACHABLE();
        }
    }
    public void visitLBINOP(byte op) {
        super.visitLBINOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LBINOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case BINOP_ADD:
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EBX); // hi
                asm.emit2_Reg_Mem(x86.ADD_m_r32, EAX, 0, ESP);
                asm.emit2_Reg_Mem(x86.ADC_m_r32, EBX, 4, ESP);
                break;
            case BINOP_SUB:
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EBX); // hi
                asm.emit2_Reg_Mem(x86.SUB_m_r32, EAX, 0, ESP);
                asm.emit2_Reg_Mem(x86.SBB_m_r32, EBX, 4, ESP);
                break;
            case BINOP_MUL:
                asm.emitShort_Reg(x86.POP_r, EBX); // lo1
                asm.emitShort_Reg(x86.POP_r, ECX); // hi1
                asm.emitShort_Reg(x86.POP_r, ESI); // lo2
                asm.emitShort_Reg(x86.POP_r, EDI); // hi2
                asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, EDI); // hi2
                asm.emitARITH_Reg_Reg(x86.OR_r_r32, EAX, ECX); // hi1 | hi2
                asm.emitCJUMP_Short(x86.JNE, (byte)0);
                int cloc = asm.getCurrentOffset();
                asm.emit2_Reg_Reg(x86.MOV_r_r32, EAX, ESI); // lo2
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
                asm.emitShort_Reg(x86.PUSH_r, EDX); // res_hi
                asm.emitShort_Reg(x86.PUSH_r, EAX); // res_lo
                break;
            case BINOP_DIV:
                emitCallRelative(MathSupport._ldiv);
                asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
                asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
                break;
            case BINOP_REM:
                emitCallRelative(MathSupport._lrem);
                asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
                asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
                break;
            case BINOP_AND:
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EDX); // hi
                asm.emit2_Reg_Mem(x86.AND_m_r32, EAX, 0, ESP);
                asm.emit2_Reg_Mem(x86.AND_m_r32, EDX, 4, ESP);
                break;
            case BINOP_OR:
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EDX); // hi
                asm.emit2_Reg_Mem(x86.OR_m_r32, EAX, 0, ESP);
                asm.emit2_Reg_Mem(x86.OR_m_r32, EDX, 4, ESP);
                break;
            case BINOP_XOR:
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EDX); // hi
                asm.emit2_Reg_Mem(x86.XOR_m_r32, EAX, 0, ESP);
                asm.emit2_Reg_Mem(x86.XOR_m_r32, EDX, 4, ESP);
                break;
            default:
                Assert.UNREACHABLE();
        }
    }
    public void visitFBINOP(byte op) {
        super.visitFBINOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FBINOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case BINOP_ADD:
                asm.emit2_Mem(x86.FLD_m32, 4, ESP);
                asm.emit2_Mem(x86.FADD_m32, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
                asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
                break;
            case BINOP_SUB:
                asm.emit2_Mem(x86.FLD_m32, 4, ESP);
                asm.emit2_Mem(x86.FSUB_m32, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
                asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
                break;
            case BINOP_MUL:
                asm.emit2_Mem(x86.FLD_m32, 4, ESP);
                asm.emit2_Mem(x86.FMUL_m32, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
                asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
                break;
            case BINOP_DIV:
                asm.emit2_Mem(x86.FLD_m32, 4, ESP);
                asm.emit2_Mem(x86.FDIV_m32, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
                asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
                break;
            case BINOP_REM:
                asm.emit2_Mem(x86.FLD_m32, 0, ESP); // reverse because pushing on fp stack
                asm.emit2_Mem(x86.FLD_m32, 4, ESP);
                asm.emit2(x86.FPREM);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
                asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
                asm.emit2_FPReg(x86.FFREE, 0);
                break;
            default:
                Assert.UNREACHABLE();
        }
    }
    public void visitDBINOP(byte op) {
        super.visitDBINOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DBINOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case BINOP_ADD:
                asm.emit2_Mem(x86.FLD_m64, 8, ESP);
                asm.emit2_Mem(x86.FADD_m64, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
                asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
                break;
            case BINOP_SUB:
                asm.emit2_Mem(x86.FLD_m64, 8, ESP);
                asm.emit2_Mem(x86.FSUB_m64, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
                asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
                break;
            case BINOP_MUL:
                asm.emit2_Mem(x86.FLD_m64, 8, ESP);
                asm.emit2_Mem(x86.FMUL_m64, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
                asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
                break;
            case BINOP_DIV:
                asm.emit2_Mem(x86.FLD_m64, 8, ESP);
                asm.emit2_Mem(x86.FDIV_m64, 0, ESP);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
                asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
                break;
            case BINOP_REM:
                asm.emit2_Mem(x86.FLD_m64, 0, ESP);
                asm.emit2_Mem(x86.FLD_m64, 8, ESP);
                asm.emit2(x86.FPREM);
                asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
                asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
                asm.emit2_FPReg(x86.FFREE, 0);
                break;
            default:
                Assert.UNREACHABLE();
        }
    }
    public void visitIUNOP(byte op) {
        super.visitIUNOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IUNOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        Assert._assert(op == UNOP_NEG);
        asm.emit2_Mem(x86.NEG_m32, 0, ESP);
    }
    public void visitLUNOP(byte op) {
        super.visitLUNOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LUNOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        Assert._assert(op == UNOP_NEG);
        asm.emit2_Mem(x86.NEG_m32, 4, ESP);  // hi
        asm.emit2_Mem(x86.NEG_m32, 0, ESP);  // lo
        asm.emitARITH_Mem_Imm(x86.SBB_m_i32, 4, ESP, 0);
    }
    public void visitFUNOP(byte op) {
        super.visitFUNOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FUNOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        Assert._assert(op == UNOP_NEG);
        asm.emit2_Mem(x86.FLD_m32, 0, ESP);
        asm.emit2(x86.FCHS);
        asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
    }
    public void visitDUNOP(byte op) {
        super.visitDUNOP(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DUNOP "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m64, 0, ESP);
        asm.emit2(x86.FCHS);
        asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
    }
    public void visitISHIFT(byte op) {
        super.visitISHIFT(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ISHIFT "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case SHIFT_LEFT:
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emit2_Mem(x86.SHL_m32_rc, 0, ESP);
                break;
            case SHIFT_RIGHT:
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emit2_Mem(x86.SAR_m32_rc, 0, ESP);
                break;
            case SHIFT_URIGHT:
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emit2_Mem(x86.SHR_m32_rc, 0, ESP);
                break;
            default:
                Assert.UNREACHABLE();
        }
    }
    public void visitLSHIFT(byte op) {
        super.visitLSHIFT(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LSHIFT"+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case SHIFT_LEFT: {
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EDX); // hi
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
                asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
                asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
                break;
            }
            case SHIFT_RIGHT: {
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EDX); // hi
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
                asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
                asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
                break;
            }
            case SHIFT_URIGHT: {
                asm.emitShort_Reg(x86.POP_r, ECX);
                asm.emitShort_Reg(x86.POP_r, EAX); // lo
                asm.emitShort_Reg(x86.POP_r, EDX); // hi
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
                asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
                asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
                break;
            }
            default:
                Assert.UNREACHABLE();
        }
    }
    public void visitIINC(int i, int v) {
        super.visitIINC(i, v);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IINC "+i+" "+v));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitARITH_Mem_Imm(x86.ADD_m_i32, getLocalOffset(i), EBP, v);
    }
    public void visitI2L() {
        super.visitI2L();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": I2L"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX); // lo
        asm.emit1(x86.CWD);
        asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
        asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
    }
    public void visitI2F() {
        super.visitI2F();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": I2F"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FILD_m32, 0, ESP);
        asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
    }
    public void visitI2D() {
        super.visitI2D();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": I2D"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FILD_m32, 0, ESP);
        asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
        asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
    }
    public void visitL2I() {
        super.visitL2I();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": L2I"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX); // lo
        asm.emitShort_Reg(x86.POP_r, ECX); // hi
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitL2F() {
        super.visitL2F();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": L2F"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FILD_m64, 0, ESP);
        asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
        asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
    }
    public void visitL2D() {
        super.visitL2D();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": L2D"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FILD_m64, 0, ESP);
        asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
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
    }
    public void visitF2I() {
        super.visitF2I();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": F2I"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m32, 0, ESP);
        toIntHelper();
    }
    public void visitF2L() {
        super.visitF2L();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": F2L"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m32, 0, ESP);
        asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
        toLongHelper();
    }
    public void visitF2D() {
        super.visitF2D();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": F2D"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m32, 0, ESP);
        asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
        asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
    }
    public void visitD2I() {
        super.visitD2I();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": D2I"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m64, 0, ESP);
        asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
        toIntHelper();
    }
    public void visitD2L() {
        super.visitD2L();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": D2L"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m64, 0, ESP);
        toLongHelper();
    }
    public void visitD2F() {
        super.visitD2F();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": D2F"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.FLD_m64, 0, ESP);
        asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
        asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
    }
    public void visitI2B() {
        super.visitI2B();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": I2B"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emit3_Reg_Reg(x86.MOVSX_r_r8, EAX, AL);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitI2C() {
        super.visitI2C();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": I2C"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emit3_Reg_Reg(x86.MOVZX_r_r16, EAX, AX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitI2S() {
        super.visitI2S();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": I2S"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emit3_Reg_Reg(x86.MOVSX_r_r16, EAX, AX);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitLCMP2() {
        super.visitLCMP2();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LCMP2"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EBX); // lo
        asm.emitShort_Reg(x86.POP_r, ECX); // hi
        asm.emitShort_Reg(x86.POP_r, EAX); // lo
        asm.emitShort_Reg(x86.POP_r, EDX); // hi
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
        asm.emitShort_Reg(x86.PUSH_r, ECX);
    }
    public void visitFCMP2(byte op) {
        super.visitFCMP2(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FCMP2 "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if (op == CMP_L) {
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2_Mem(x86.FLD_m32, 4, ESP);
        } else {
            asm.emit2_Mem(x86.FLD_m32, 4, ESP); // reverse order
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
        }
        asm.emit2(x86.FUCOMPP);
        asm.emit2(x86.FNSTSW_ax);
        asm.emit1(x86.SAHF);
        asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
        if (op == CMP_L) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, -1);
        } else {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 1);
        }
        asm.emitCJUMP_Short(x86.JB, (byte)0);
        int cloc1 = asm.getCurrentOffset();
        asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
        asm.emitCJUMP_Short(x86.JE, (byte)0);
        int cloc2 = asm.getCurrentOffset();
        if (op == CMP_L) {
            asm.emitShort_Reg(x86.INC_r32, ECX);
        } else {
            asm.emitShort_Reg(x86.DEC_r32, ECX);
        }
        asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
        asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
        asm.emitShort_Reg(x86.PUSH_r, ECX);
    }
    public void visitDCMP2(byte op) {
        super.visitDCMP2(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DCMP2 "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if (op == CMP_L) {
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Mem(x86.FLD_m64, 8, ESP);
        } else {
            asm.emit2_Mem(x86.FLD_m64, 8, ESP); // reverse order
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
        }
        asm.emit2(x86.FUCOMPP);
        asm.emit2(x86.FNSTSW_ax);
        asm.emit1(x86.SAHF);
        asm.emit2_Reg_Mem(x86.LEA, ESP, 16, ESP);
        if (op == CMP_L) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, -1);
        } else {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 1);
        }
        asm.emitCJUMP_Short(x86.JB, (byte)0);
        int cloc1 = asm.getCurrentOffset();
        asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
        asm.emitCJUMP_Short(x86.JE, (byte)0);
        int cloc2 = asm.getCurrentOffset();
        if (op == CMP_L) {
            asm.emitShort_Reg(x86.INC_r32, ECX);
        } else {
            asm.emitShort_Reg(x86.DEC_r32, ECX);
        }
        asm.patch1(cloc1-1, (byte)(asm.getCurrentOffset()-cloc1));
        asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc2));
        asm.emitShort_Reg(x86.PUSH_r, ECX);
    }
    private void branchHelper(byte op, int target) {
        Integer t = new Integer(target);
        if (op == CMP_UNCOND) {
            if (target <= i_start)
                asm.emitJUMP_Back(x86.JMP, t);
            else
                asm.emitJUMP_Forw(x86.JMP, t);
        } else {
            x86 opc = null;
            switch(op) {
                case CMP_EQ: opc = x86.JE; break;
                case CMP_NE: opc = x86.JNE; break;
                case CMP_LT: opc = x86.JL; break;
                case CMP_GE: opc = x86.JGE; break;
                case CMP_LE: opc = x86.JLE; break;
                case CMP_GT: opc = x86.JG; break;
                case CMP_AE: opc = x86.JAE; break;
                default: Assert.UNREACHABLE();
            }
            if (target <= i_start)
                asm.emitCJUMP_Back(opc, t);
            else
                asm.emitCJUMP_Forw(opc, t);
        }
    }
    public void visitIF(byte op, int target) {
        super.visitIF(op, target);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IF "+op+" "+target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, 0);
        branchHelper(op, target);
    }
    public void visitIFREF(byte op, int target) {
        super.visitIFREF(op, target);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IFREF "+op+" "+target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, 0);
        branchHelper(op, target);
    }
    public void visitIFCMP(byte op, int target) {
        super.visitIFCMP(op, target);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IFCMP "+op+" "+target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, ECX);
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitARITH_Reg_Reg(x86.CMP_r_r32, EAX, ECX);
        branchHelper(op, target);
    }
    public void visitIFREFCMP(byte op, int target) {
        super.visitIFREFCMP(op, target);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IFREFCMP "+op+" "+target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, ECX);
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emitARITH_Reg_Reg(x86.CMP_r_r32, EAX, ECX);
        branchHelper(op, target);
    }
    public void visitGOTO(int target) {
        super.visitGOTO(target);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": GOTO "+target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        branchHelper(CMP_UNCOND, target);
    }
    public void visitJSR(int target) {
        super.visitJSR(target);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": JSR "+target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        Integer t = new Integer(target);
        if (target <= i_start) {
            asm.emitCALL_Back(x86.CALL_rel32, t);
        } else {
            asm.emitCALL_Forw(x86.CALL_rel32, t);
        }
    }
    public void visitRET(int i) {
        super.visitRET(i);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": RET "+i));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit2_Mem(x86.JMP_m, getLocalOffset(i), EBP);
    }
    public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
        super.visitTABLESWITCH(default_target, low, high, targets);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": TABLESWITCH "+default_target+" "+low+" "+high));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        int count = high-low+1;
        Assert._assert(count == targets.length);
        asm.emitShort_Reg(x86.POP_r, EAX);
        if (low != 0)
            asm.emitARITH_Reg_Imm(x86.SUB_r_i32, EAX, low);
        asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, count);
        branchHelper(CMP_AE, default_target);
        asm.emitCALL_rel32(x86.CALL_rel32, 0);
        int cloc = asm.getCurrentOffset();
        asm.emitShort_Reg(x86.POP_r, ECX);
        // val from table + abs position in table
        asm.emit2_Reg_Mem(x86.LEA, EDX, ECX, EAX, SCALE_4, 127);
        int cloc2 = asm.getCurrentOffset();
        asm.emitARITH_Reg_Mem(x86.ADD_r_m32, EDX, -4, EDX);
        asm.emit2_Reg(x86.JMP_r, EDX);
        asm.patch1(cloc2-1, (byte)(asm.getCurrentOffset()-cloc+4));
        for (int i=0; i<count; ++i) {
            int target = targets[i];
            Integer t = new Integer(target);
            if (target <= i_start) {
                int offset = asm.getBranchTarget(t) - asm.getCurrentOffset() + 4;
                asm.emitDATA(offset);
            } else {
                asm.emitDATA(0x77777777);
                asm.recordForwardBranch(4, t);
            }
        }
    }
    public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
        super.visitLOOKUPSWITCH(default_target, values, targets);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LOOKUPSWITCH "+default_target));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        for (int i=0; i<values.length; ++i) {
            int match = values[i];
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, match);
            int target = targets[i];
            branchHelper(CMP_EQ, target);
        }
        branchHelper(CMP_UNCOND, default_target);
    }
    private void SYNCHEXIThelper() {
        if (method.isStatic()) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString("exit: STATIC SYNCH EXIT"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // lock the java.lang.Class object
            Class c = Reflection.getJDKType(method.getDeclaringClass());
            Assert._assert(c != null);
            emitPushAddressOf(c);
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString("exit: INSTANCE SYNCH EXIT"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // lock the this pointer
            asm.emit2_Mem(x86.PUSH_m, getLocalOffset(0), EBP);
        }
        emitCallRelative(Monitor._monitorexit);
    }
    private void RETURN4helper() {
        if (method.isSynchronized()) SYNCHEXIThelper();
        if (TraceMethods) {
            emitPushAddressOf(SystemInterface.toCString("Leaving: "+method));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        // epilogue
        asm.emitShort_Reg(x86.POP_r, EAX); // store return value
        asm.emit1(x86.LEAVE);              // esp<-ebp, pop ebp
        asm.emit1_Imm16(x86.RET_i, (char)(n_paramwords<<2));
    }
    private void RETURN8helper() {
        if (method.isSynchronized()) SYNCHEXIThelper();
        if (TraceMethods) {
            emitPushAddressOf(SystemInterface.toCString("Leaving: "+method));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        // epilogue
        asm.emitShort_Reg(x86.POP_r, EAX); // return value lo
        asm.emitShort_Reg(x86.POP_r, EDX); // return value hi
        asm.emit1(x86.LEAVE);              // esp<-ebp, pop ebp
        asm.emit1_Imm16(x86.RET_i, (char)(n_paramwords<<2));
    }
    public void visitIRETURN() {
        super.visitIRETURN();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": IRETURN"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        RETURN4helper();
    }
    public void visitLRETURN() {
        super.visitLRETURN();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": LRETURN"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        RETURN8helper();
    }
    public void visitFRETURN() {
        super.visitFRETURN();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": FRETURN"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        RETURN4helper();
    }
    public void visitDRETURN() {
        super.visitDRETURN();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": DRETURN"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        RETURN8helper();
    }
    public void visitARETURN() {
        super.visitARETURN();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ARETURN"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        RETURN4helper();
    }
    public void visitVRETURN() {
        super.visitVRETURN();
        if (method.isSynchronized()) SYNCHEXIThelper();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": VRETURN"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if (TraceMethods) {
            emitPushAddressOf(SystemInterface.toCString("Leaving: "+method));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emit1(x86.LEAVE);              // esp<-ebp, pop ebp
        asm.emit1_Imm16(x86.RET_i, (char)(n_paramwords<<2));
    }
    public void GETSTATIC4helper(jq_StaticField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETSTATIC4 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 6
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._getstatic4);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETSTATIC4 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            emitPushMemory(f);
        }
    }
    static int patch_getstatic4(CodeAddress retloc, jq_StaticField f) {
        // todo: register backpatched reference
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x35FF9090);
        retloc.offset(-4 ).poke(f.getAddress());
        retloc.offset(-10).poke2((short)0x9090);
        return 6;
    }
    public void GETSTATIC8helper(jq_StaticField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETSTATIC8 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(12);
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._getstatic8);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETSTATIC8 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            emitPushMemory8(f);
        }
    }
    static int patch_getstatic8(CodeAddress retloc, jq_StaticField f) {
        // todo: register backpatched reference
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke(f.getAddress().offset(4));
        retloc.offset(-4 ).poke2((short)0x35FF);
        retloc.offset(-2 ).poke(f.getAddress());
        retloc.offset(-10).poke2((short)0x35FF);
        return 10;
    }
    public void visitIGETSTATIC(jq_StaticField f) {
        super.visitIGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void visitLGETSTATIC(jq_StaticField f) {
        super.visitLGETSTATIC(f);
        GETSTATIC8helper(f);
    }
    public void visitFGETSTATIC(jq_StaticField f) {
        super.visitFGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void visitDGETSTATIC(jq_StaticField f) {
        super.visitDGETSTATIC(f);
        GETSTATIC8helper(f);
    }
    public void visitAGETSTATIC(jq_StaticField f) {
        super.visitAGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void visitZGETSTATIC(jq_StaticField f) {
        super.visitZGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void visitBGETSTATIC(jq_StaticField f) {
        super.visitBGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void visitCGETSTATIC(jq_StaticField f) {
        super.visitCGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void visitSGETSTATIC(jq_StaticField f) {
        super.visitSGETSTATIC(f);
        GETSTATIC4helper(f);
    }
    public void PUTSTATIC4helper(jq_StaticField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTSTATIC4 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 6
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._putstatic4);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTSTATIC4 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            emitPopMemory(f);
        }
    }
    static int patch_putstatic4(CodeAddress retloc, jq_StaticField f) {
        // todo: register backpatched reference
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x058F9090);
        retloc.offset(-4 ).poke(f.getAddress());
        retloc.offset(-10).poke2((short)0x9090);
        return 6;
    }
    public void PUTSTATIC8helper(jq_StaticField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            // generate a runtime call, which will be backpatched.
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTSTATIC8 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.startDynamicPatch(12);
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._putstatic8);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTSTATIC8 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            emitPopMemory8(f);
        }
    }
    static int patch_putstatic8(CodeAddress retloc, jq_StaticField f) {
        // todo: register backpatched reference
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke(f.getAddress().offset(4));
        retloc.offset(-4 ).poke2((short)0x058F);
        retloc.offset(-2 ).poke(f.getAddress());
        retloc.offset(-10).poke2((short)0x058F);
        return 10;
    }
    public void visitIPUTSTATIC(jq_StaticField f) {
        super.visitIPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void visitLPUTSTATIC(jq_StaticField f) {
        super.visitLPUTSTATIC(f);
        PUTSTATIC8helper(f);
    }
    public void visitFPUTSTATIC(jq_StaticField f) {
        super.visitFPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void visitDPUTSTATIC(jq_StaticField f) {
        super.visitDPUTSTATIC(f);
        PUTSTATIC8helper(f);
    }
    public void visitAPUTSTATIC(jq_StaticField f) {
        super.visitAPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void visitZPUTSTATIC(jq_StaticField f) {
        super.visitZPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void visitBPUTSTATIC(jq_StaticField f) {
        super.visitBPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void visitCPUTSTATIC(jq_StaticField f) {
        super.visitCPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void visitSPUTSTATIC(jq_StaticField f) {
        super.visitSPUTSTATIC(f);
        PUTSTATIC4helper(f);
    }
    public void GETFIELD1helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            // generate a runtime call, which will be backpatched.
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETFIELD1 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.startDynamicPatch(10); // 9
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._getfield1);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETFIELD1 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
            asm.emit3_Reg_Mem(x86.MOVSX_r_m8, EBX, f.getOffset(), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
        }
    }
    static int patch_getfield1(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x0098BE0F);
        retloc.offset(-5 ).poke4(f.getOffset());
        retloc.offset(-1 ).poke1((byte)0x53);
        retloc.offset(-10).poke2((short)0x5890);
        return 9;
    }
    public void GETFIELD4helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            // generate a runtime call, which will be backpatched.
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETFIELD4 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.startDynamicPatch(10); // 7
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._getfield4);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETFIELD4 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
            asm.emit2_Mem(x86.PUSH_m, f.getOffset(), EAX);
        }
    }
    static int patch_getfield4(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0xB0FF5890);
        retloc.offset(-4 ).poke4(f.getOffset());
        retloc.offset(-10).poke2((short)0x9090);
        return 7;
    }
    public void GETFIELD8helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETFIELD8 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(13);
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._getfield8);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": GETFIELD8 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
            asm.emit2_Mem(x86.PUSH_m, f.getOffset()+4, EAX); // hi
            asm.emit2_Mem(x86.PUSH_m, f.getOffset(), EAX);   // lo
        }
    }
    static int patch_getfield8(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke4(0x00B0FFEB);
        retloc.offset(-7 ).poke4(f.getOffset()+4);
        retloc.offset(-3 ).poke2((short)0xB0FF);
        retloc.offset(-1 ).poke4(f.getOffset());
        retloc.offset(-10).poke1((byte)0x58);
        return 10;
    }
    public void visitIGETFIELD(jq_InstanceField f) {
        super.visitIGETFIELD(f);
        GETFIELD4helper(f);
    }
    public void visitLGETFIELD(jq_InstanceField f) {
        super.visitLGETFIELD(f);
        GETFIELD8helper(f);
    }
    public void visitFGETFIELD(jq_InstanceField f) {
        super.visitFGETFIELD(f);
        GETFIELD4helper(f);
    }
    public void visitDGETFIELD(jq_InstanceField f) {
        super.visitDGETFIELD(f);
        GETFIELD8helper(f);
    }
    public void visitAGETFIELD(jq_InstanceField f) {
        super.visitAGETFIELD(f);
        GETFIELD4helper(f);
    }
    public void visitBGETFIELD(jq_InstanceField f) {
        super.visitBGETFIELD(f);
        GETFIELD1helper(f);
    }
    public void visitCGETFIELD(jq_InstanceField f) {
        super.visitCGETFIELD(f);
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": CGETFIELD "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 9
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._cgetfield);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": CGETFIELD "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
            asm.emit3_Reg_Mem(x86.MOVZX_r_m16, EBX, f.getOffset(), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
        }
    }
    static int patch_cgetfield(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x0098B70F);
        retloc.offset(-5 ).poke4(f.getOffset());
        retloc.offset(-1 ).poke1((byte)0x53);
        retloc.offset(-10).poke2((short)0x5890);
        return 9;
    }
    public void visitSGETFIELD(jq_InstanceField f) {
        super.visitSGETFIELD(f);
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": SGETFIELD "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 9
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._sgetfield);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": SGETFIELD "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            asm.emitShort_Reg(x86.POP_r, EAX); // obj ref
            asm.emit3_Reg_Mem(x86.MOVSX_r_m16, EBX, f.getOffset(), EAX);
            asm.emitShort_Reg(x86.PUSH_r, EBX);
        }
    }
    static int patch_sgetfield(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x0098BF0F);
        retloc.offset(-5 ).poke4(f.getOffset());
        retloc.offset(-1 ).poke1((byte)0x53);
        retloc.offset(-10).poke2((short)0x5890);
        return 9;
    }
    public void visitZGETFIELD(jq_InstanceField f) {
        super.visitZGETFIELD(f);
        GETFIELD1helper(f);
    }
    public void PUTFIELD1helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD1 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 8
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._putfield1);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD1 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // field has already been resolved.
            asm.emitShort_Reg(x86.POP_r, EBX);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emit2_Reg_Mem(x86.MOV_m_r8, EBX, f.getOffset(), EAX);
        }
    }
    static int patch_putfield1(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x9888585B);
        retloc.offset(-4 ).poke4(f.getOffset());
        retloc.offset(-10).poke2((short)0x9090);
        return 8;
    }
    public void PUTFIELD2helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD2 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 9
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._putfield2);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD2 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // field has already been resolved.
            asm.emitShort_Reg(x86.POP_r, EBX);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitprefix(x86.PREFIX_16BIT);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, f.getOffset(), EAX);
        }
    }
    static int patch_putfield2(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke4(0x6658FFEB);
        retloc.offset(-6 ).poke2((short)0x9889);
        retloc.offset(-4 ).poke4(f.getOffset());
        retloc.offset(-10).poke2((short)0x5B90);
        return 9;
    }
    public void PUTFIELD4helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD4 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(10); // 8
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._putfield4);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD4 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // field has already been resolved.
            asm.emitShort_Reg(x86.POP_r, EBX);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, f.getOffset(), EAX);
        }
    }
    static int patch_putfield4(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke4(0x9889585B);
        retloc.offset(-4 ).poke4(f.getOffset());
        retloc.offset(-10).poke2((short)0x9090);
        return 8;
    }
    public void PUTFIELD8helper(jq_InstanceField f) {
        f = tryResolve(f);
        boolean dynlink = state.needsDynamicLink(method, f);
        if (dynlink) {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD8 "+f+" (dynpatch)"));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // generate a runtime call, which will be backpatched.
            asm.startDynamicPatch(15);
            emitPushAddressOf(f);
            emitCallRelative(x86ReferenceLinker._putfield8);
            asm.endDynamicPatch();
        } else {
            if (TraceBytecodes) {
                emitPushAddressOf(SystemInterface.toCString(i_start+": PUTFIELD8 "+f));
                emitCallMemory(SystemInterface._debugwriteln);
            }
            // field has already been resolved.
            asm.emitShort_Reg(x86.POP_r, EBX); // lo
            asm.emitShort_Reg(x86.POP_r, EAX); // hi
            asm.emitShort_Reg(x86.POP_r, EDX);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, f.getOffset()  , EDX); // lo
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EAX, f.getOffset()+4, EDX); // hi
        }
    }
    static int patch_putfield8(CodeAddress retloc, jq_InstanceField f) {
        retloc.offset(-10).poke4(0x895AFFEB);
        retloc.offset(-6 ).poke1((byte)0x9A);
        retloc.offset(-5 ).poke4(f.getOffset());
        retloc.offset(-1 ).poke2((short)0x8289);
        retloc.offset( 1 ).poke4(f.getOffset()+4);
        retloc.offset(-10).poke2((short)0x585B);
        return 10;
    }
    public void visitIPUTFIELD(jq_InstanceField f) {
        super.visitIPUTFIELD(f);
        PUTFIELD4helper(f);
    }
    public void visitLPUTFIELD(jq_InstanceField f) {
        super.visitLPUTFIELD(f);
        PUTFIELD8helper(f);
    }
    public void visitFPUTFIELD(jq_InstanceField f) {
        super.visitFPUTFIELD(f);
        PUTFIELD4helper(f);
    }
    public void visitDPUTFIELD(jq_InstanceField f) {
        super.visitDPUTFIELD(f);
        PUTFIELD8helper(f);
    }
    public void visitAPUTFIELD(jq_InstanceField f) {
        super.visitAPUTFIELD(f);
        PUTFIELD4helper(f);
    }
    public void visitBPUTFIELD(jq_InstanceField f) {
        super.visitBPUTFIELD(f);
        PUTFIELD1helper(f);
    }
    public void visitCPUTFIELD(jq_InstanceField f) {
        super.visitCPUTFIELD(f);
        PUTFIELD2helper(f);
    }
    public void visitSPUTFIELD(jq_InstanceField f) {
        super.visitSPUTFIELD(f);
        PUTFIELD2helper(f);
    }
    public void visitZPUTFIELD(jq_InstanceField f) {
        super.visitZPUTFIELD(f);
        PUTFIELD1helper(f);
    }
    private void INVOKEDPATCHhelper(byte op, jq_Method f) {
        int dpatchsize;
        jq_StaticMethod dpatchentry;
        switch (op) {
            case INVOKE_VIRTUAL:
                dpatchsize = 16;
                dpatchentry = x86ReferenceLinker._invokevirtual;
                break;
            case INVOKE_STATIC:
                dpatchsize = 11; // 5
                dpatchentry = x86ReferenceLinker._invokestatic;
                break;
            case INVOKE_SPECIAL:
                dpatchsize = 11; // 5
                dpatchentry = x86ReferenceLinker._invokespecial;
                break;
            case INVOKE_INTERFACE:
                // fallthrough
            default:
                throw new InternalError();
        }
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": INVOKE "+op+" "+f+" (dynpatch)"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        // generate a runtime call, which will be backpatched.
        asm.startDynamicPatch(dpatchsize);
        emitPushAddressOf(f);
        emitCallRelative(dpatchentry);
        asm.endDynamicPatch();
    }
    static int patch_invokevirtual(CodeAddress retloc, jq_InstanceMethod f) {
        retloc.offset(-10).poke2((short)0xFFEB);
        retloc.offset(-8 ).poke1((byte)0x24);
        int objptroffset = (f.getParamWords() << 2) - 4;
        retloc.offset(-7 ).poke4(objptroffset);
        retloc.offset(-3 ).poke2((short)0x588B);
        retloc.offset(-1 ).poke1((byte)ObjectLayout.VTABLE_OFFSET);
        retloc.            poke2((short)0x93FF);
        retloc.offset( 2 ).poke4(f.getOffset());
        retloc.offset(-10).poke2((short)0x848B);
        return 10;
    }
    static int patch_invokestatic(CodeAddress retloc, jq_Method f) {
        retloc.offset(-10).poke4(0x9090FFEB);
        retloc.offset(-6 ).poke2((short)0xE890);
        retloc.offset(-4 ).poke4(f.getDefaultCompiledVersion().getEntrypoint().difference(retloc));
        retloc.offset(-10).poke2((short)0x9090);
        return 5;
    }
    private void INVOKENODPATCHhelper(byte op, jq_Method f) {
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": INVOKE "+op+" "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        switch(op) {
            case INVOKE_VIRTUAL: {
                int objptroffset = (f.getParamWords() << 2) - 4;
                int m_off = ((jq_InstanceMethod)f).getOffset();
                asm.emit2_Reg_Mem(x86.MOV_r_m32, EAX, objptroffset, ESP); // 7
                asm.emit2_Reg_Mem(x86.MOV_r_m32, EBX, ObjectLayout.VTABLE_OFFSET, EAX); // 3
                asm.emit2_Mem(x86.CALL_m, m_off, EBX); // 6
                break;
            }
            case INVOKE_SPECIAL:
                f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
                emitCallRelative(f);
                break;
            case INVOKE_STATIC:
                emitCallRelative(f);
                break;
            case INVOKE_INTERFACE:
                //jq.Assert(!jq.RunningNative || f.getDeclaringClass().isInterface());
                emitPushAddressOf(f);
                emitCallRelative(x86ReferenceLinker._invokeinterface);
                // need to pop args ourselves.
                asm.emit2_Reg_Mem(x86.LEA, ESP, f.getParamWords()<<2, ESP);
                break;
            default:
                Assert.UNREACHABLE();
        }
    }
    private void INVOKEhelper(byte op, jq_Method f) {
        f = (jq_Method) tryResolve(f);
        Assert._assert(!f.getDeclaringClass().getName().equals("joeq.Runtime.Unsafe"));
        boolean dynlink = state.needsDynamicLink(method, f);
        switch (op) {
            case INVOKE_VIRTUAL:
                if (dynlink)
                    INVOKEDPATCHhelper(op, f);
                else
                    INVOKENODPATCHhelper(op, f);
                break;
            case INVOKE_STATIC:
                // fallthrough
            case INVOKE_SPECIAL:
                if (dynlink)
                    INVOKEDPATCHhelper(op, f);
                else
                    INVOKENODPATCHhelper(op, f);
                break;
            case INVOKE_INTERFACE:
                INVOKENODPATCHhelper(op, f);
                break;
            default:
                throw new InternalError();
        }
    }
    public void visitIINVOKE(byte op, jq_Method f) {
        super.visitIINVOKE(op, f);
        if (f.getDeclaringClass() == Unsafe._class) {
            gen_unsafe(f);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            genAddress(f);
            return;
        }
        INVOKEhelper(op, f);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitLINVOKE(byte op, jq_Method f) {
        super.visitLINVOKE(op, f);
        if (f.getDeclaringClass() == Unsafe._class) {
            gen_unsafe(f);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            genAddress(f);
            return;
        }
        INVOKEhelper(op, f);
        asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
        asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
    }
    public void visitFINVOKE(byte op, jq_Method f) {
        super.visitFINVOKE(op, f);
        if (f.getDeclaringClass() == Unsafe._class) {
            gen_unsafe(f);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            genAddress(f);
            return;
        }
        INVOKEhelper(op, f);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitDINVOKE(byte op, jq_Method f) {
        super.visitDINVOKE(op, f);
        if (f.getDeclaringClass() == Unsafe._class) {
            gen_unsafe(f);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            genAddress(f);
            return;
        }
        INVOKEhelper(op, f);
        asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
        asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
    }
    public void visitAINVOKE(byte op, jq_Method f) {
        super.visitAINVOKE(op, f);
        if (f.getDeclaringClass() == Unsafe._class) {
            gen_unsafe(f);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            genAddress(f);
            return;
        }
        INVOKEhelper(op, f);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitVINVOKE(byte op, jq_Method f) {
        super.visitVINVOKE(op, f);
        if (f.getDeclaringClass() == Unsafe._class) {
            gen_unsafe(f);
            return;
        }
        if (f.getDeclaringClass().isAddressType()) {
            genAddress(f);
            return;
        }
        INVOKEhelper(op, f);
    }
    public void visitNEW(jq_Type f) {
        super.visitNEW(f);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": NEW "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        boolean dynlink = state.needsDynamicLink(method, f);
        if (f.isClassType() && !dynlink) {
            jq_Class k = (jq_Class)f;
            asm.emitPUSH_i(k.getInstanceSize());
            emitPushAddressOf(k.getVTable());
            emitCallRelative(DefaultHeapAllocator._allocateObject);
        } else {
            emitPushAddressOf(f);
            emitCallRelative(HeapAllocator._clsinitAndAllocateObject);
        }
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitNEWARRAY(jq_Array f) {
        super.visitNEWARRAY(f);
        // initialize type now, to avoid backpatch.
        // TODO: maybe we want to support delaying initialization.
        if (!jq.RunningNative) {
            //jq.Assert(jq.boot_types.contains(f), f.toString());
        } else {
            f.cls_initialize();
        }
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": NEWARRAY "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        byte width = f.getLogElementSize();
        asm.emit2_Mem(x86.PUSH_m, 0, ESP);
        if (width != 0) asm.emit2_SHIFT_Mem_Imm8(x86.SHL_m32_i, 0, ESP, width);
        asm.emitARITH_Mem_Imm(x86.ADD_m_i32, 0, ESP, ObjectLayout.ARRAY_HEADER_SIZE);
        emitPushAddressOf(f.getVTable());
        emitCallRelative(DefaultHeapAllocator._allocateArray);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitARRAYLENGTH() {
        super.visitARRAYLENGTH();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ARRAYLENGTH"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitShort_Reg(x86.POP_r, EAX);
        asm.emit2_Mem(x86.PUSH_m, ObjectLayout.ARRAY_LENGTH_OFFSET, EAX);
    }
    public void visitATHROW() {
        super.visitATHROW();
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ATHROW"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if (TraceMethods) {
            emitPushAddressOf(SystemInterface.toCString("Leaving: "+method+" (explicit athrow)"));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        emitCallRelative(ExceptionDeliverer._athrow);
    }
    public void visitCHECKCAST(jq_Type f) {
        super.visitCHECKCAST(f);
        if (f.isAddressType()) return;
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": CHECKCAST "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        emitPushAddressOf(f);
        emitCallRelative(TypeCheck._checkcast);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitINSTANCEOF(jq_Type f) {
        super.visitINSTANCEOF(f);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": INSTANCEOF "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        emitPushAddressOf(f);
        emitCallRelative(TypeCheck._instance_of);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }
    public void visitMONITOR(byte op) {
        super.visitMONITOR(op);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": MONITOR "+op));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        jq_StaticMethod m = (op==MONITOR_ENTER)?Monitor._monitorenter:Monitor._monitorexit;
        emitCallRelative(m);
    }
    public void visitMULTINEWARRAY(jq_Type f, char dim) {
        super.visitMULTINEWARRAY(f, dim);
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": MULTINEWARRAY "+f+" "+dim));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        asm.emitPUSH_i(dim);
        emitPushAddressOf(f);
        emitCallRelative(joeq.Runtime.Arrays._multinewarray);
        // pop dim args, because the callee doesn't do it.
        asm.emit2_Reg_Mem(x86.LEA, ESP, dim<<2, ESP);
        asm.emitShort_Reg(x86.PUSH_r, EAX);
    }

    public static byte THREAD_BLOCK_PREFIX = x86.PREFIX_FS;
    public static int  THREAD_BLOCK_OFFSET = 0x14;

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
    public static final Utf8 logSize = Utf8.get("logSize");
    public static final Utf8 pageAlign = Utf8.get("pageAlign");
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

    private void genAddress(jq_Method f) {
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": ADDRESS "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if (f.getName() == peek1) {
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit3_Reg_Mem(x86.MOVSX_r_m8, ECX, 0, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
        } else if (f.getName() == peek2) {
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit3_Reg_Mem(x86.MOVSX_r_m16, ECX, 0, EAX);
            asm.emitShort_Reg(x86.PUSH_r, ECX);
        } else if (f.getName() == peek4 || f.getName() == peek) {
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit2_Mem(x86.PUSH_m, 0, EAX);
        } else if (f.getName() == peek8) {
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit2_Mem(x86.PUSH_m, 4, EAX); // hi
            asm.emit2_Mem(x86.PUSH_m, 0, EAX); // lo
        } else if (f.getName() == poke1) {
            asm.emitShort_Reg(x86.POP_r, EBX); // value
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit2_Reg_Mem(x86.MOV_m_r8, EBX, 0, EAX);
        } else if (f.getName() == poke2) {
            asm.emitShort_Reg(x86.POP_r, EBX); // value
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emitprefix(x86.PREFIX_16BIT);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
        } else if (f.getName() == poke4 || f.getName() == poke) {
            asm.emitShort_Reg(x86.POP_r, EBX); // value
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
        } else if (f.getName() == poke8) {
            asm.emitShort_Reg(x86.POP_r, EBX); // lo
            asm.emitShort_Reg(x86.POP_r, ECX); // hi
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emit2_Reg_Mem(x86.MOV_m_r32, EBX, 0, EAX);
            asm.emit2_Reg_Mem(x86.MOV_m_r32, ECX, 4, EAX);
        } else if (f.getName() == offset) {
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitARITH_Reg_Mem(x86.ADD_m_r32, EAX, 0, ESP);
        } else if (f.getName() == align) {
            asm.emitShort_Reg(x86.POP_r, ECX); // shift
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, EBX, 1);
            asm.emit2_Reg(x86.SHL_r32_rc, EBX);
            asm.emitShort_Reg(x86.DEC_r32, EBX);
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EAX, EBX);
            asm.emit2_Reg(x86.NOT_r32, EBX);
            asm.emitARITH_Reg_Reg(x86.AND_r_r32, EAX, EBX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        } else if (f.getName() == difference) {
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitARITH_Reg_Mem(x86.SUB_m_r32, EAX, 0, ESP); // a-b
        } else if (f.getName() == isNull) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitARITH_Reg_Imm(x86.CMP_r_i32, EAX, 0);
            asm.emitCJUMP_Short(x86.JNE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            asm.emitShort_Reg(x86.PUSH_r, ECX);
        } else if (f.getName() == addressOf || f.getName() == address32 ||
                   f.getName() == asObject || f.getName() == asReferenceType ||
                   f.getName() == to32BitValue) {
            asm.emit1(x86.NOP);
        } else if (f.getName() == stringRep) {
            jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljwutil/strings/Strings;");
            jq_StaticMethod sm = k.getOrCreateStaticMethod("hex8", "(I)Ljava/lang/String;");
            emitCallRelative(sm);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        } else if (f.getName() == getNull || f.getName() == min) {
            asm.emitPUSH_i(0);
        } else if (f.getName() == max) {
            asm.emitPUSH_i(Integer.MAX_VALUE);
        } else if (f.getName() == size) {
            asm.emitPUSH_i(4);
        } else if (f.getName() == logSize) {
            asm.emitPUSH_i(2);
        } else if (f.getName() == pageAlign) {
            asm.emitPUSH_i(12);
        } else if (f.getName() == getBasePointer) {
            asm.emitShort_Reg(x86.PUSH_r, EBP);
        } else if (f.getName() == getStackPointer) {
            asm.emitShort_Reg(x86.PUSH_r, ESP);
        } else if (f.getName() == alloca) {
            asm.emit2_Mem(x86.NEG_m32, 0, ESP);
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emitARITH_Reg_Reg(x86.ADD_r_r32, EAX, ESP);
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ESP, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        } else if (f.getName() == atomicAdd) {
            asm.emitShort_Reg(x86.POP_r, EBX); // value
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emitARITH_Reg_Mem(x86.ADD_m_r32, EBX, 0, EAX);
        } else if (f.getName() == atomicSub) {
            asm.emitShort_Reg(x86.POP_r, EBX); // value
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emitARITH_Reg_Mem(x86.SUB_m_r32, EBX, 0, EAX);
        } else if (f.getName() == atomicCas4) {
            asm.emitShort_Reg(x86.POP_r, EBX); // after
            asm.emitShort_Reg(x86.POP_r, EAX); // before
            asm.emitShort_Reg(x86.POP_r, ECX); // address
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emit3_Reg_Mem(x86.CMPXCHG_32, EBX, 0, ECX);
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        } else if (f.getName() == atomicCas8) {
            // untested.
            asm.emitShort_Reg(x86.POP_r, EBX); // lo after
            asm.emitShort_Reg(x86.POP_r, ECX); // hi after
            asm.emitShort_Reg(x86.POP_r, EAX); // lo before
            asm.emitShort_Reg(x86.POP_r, EDX); // hi before
            asm.emitShort_Reg(x86.POP_r, EDI); // address
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emit3_Reg_Mem(x86.CMPXCHG8B, EAX, 0, EDI);
            asm.emitShort_Reg(x86.PUSH_r, EDX); // hi result
            asm.emitShort_Reg(x86.PUSH_r, EAX); // lo result
        } else if (f.getName() == atomicAnd) {
            asm.emitShort_Reg(x86.POP_r, EBX); // value
            asm.emitShort_Reg(x86.POP_r, EAX); // address
            if (jq.SMP) asm.emitprefix(x86.PREFIX_LOCK);
            asm.emitARITH_Reg_Mem(x86.AND_m_r32, EBX, 0, EAX);
        } else if (f.getName() == Utf8.get("<init>")) {
            INVOKEhelper(INVOKE_SPECIAL, f);
        } else {
            Assert.UNREACHABLE(f.toString());
        }
    }
    
    private void gen_unsafe(jq_Method f) {
        if (TraceBytecodes) {
            emitPushAddressOf(SystemInterface.toCString(i_start+": UNSAFE "+f));
            emitCallMemory(SystemInterface._debugwriteln);
        }
        if ((f == Unsafe._floatToIntBits) || (f == Unsafe._intBitsToFloat) ||
            (f == Unsafe._doubleToLongBits) || (f == Unsafe._longBitsToDouble)) {
            asm.emit1(x86.NOP);
            /*
        } else if (f == Unsafe._getTypeOf) {
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emit2_Reg_Mem(x86.MOV_r_m32, EBX, VTABLE_OFFSET, EAX);
            asm.emit2_Mem(x86.PUSH_m, 0, EBX);
            */
        } else if (f == Unsafe._popFP32) {
            asm.emit2_Reg_Mem(x86.LEA, ESP, -4, ESP);
            asm.emit2_Mem(x86.FSTP_m32, 0, ESP);
        } else if (f == Unsafe._popFP64) {
            asm.emit2_Reg_Mem(x86.LEA, ESP, -8, ESP);
            asm.emit2_Mem(x86.FSTP_m64, 0, ESP);
        } else if (f == Unsafe._pushFP32) {
            asm.emit2_Mem(x86.FLD_m32, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 4, ESP);
        } else if (f == Unsafe._pushFP64) {
            asm.emit2_Mem(x86.FLD_m64, 0, ESP);
            asm.emit2_Reg_Mem(x86.LEA, ESP, 8, ESP);
        } else if (f == Unsafe._EAX) {
            asm.emitShort_Reg(x86.PUSH_r, EAX);
        } else if ((f == Unsafe._pushArg) || (f == Unsafe._pushArgA)) {
            asm.emit1(x86.NOP);
        } else if (f == Unsafe._invoke) {
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emit2_Reg(x86.CALL_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EDX); // hi
            asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
        } else if (f == Unsafe._invokeA) {
            asm.emitShort_Reg(x86.POP_r, EAX);
            asm.emit2_Reg(x86.CALL_r, EAX);
            asm.emitShort_Reg(x86.PUSH_r, EAX); // lo
        } else if (f == Unsafe._getThreadBlock) {
            asm.emitprefix(THREAD_BLOCK_PREFIX);
            asm.emit2_Mem(x86.PUSH_m, THREAD_BLOCK_OFFSET);
        } else if (f == Unsafe._setThreadBlock) {
            asm.emitprefix(THREAD_BLOCK_PREFIX);
            asm.emit2_Mem(x86.POP_m, THREAD_BLOCK_OFFSET);
        } else if (f == Unsafe._longJump) {
            asm.emitShort_Reg(x86.POP_r, EAX); // eax
            asm.emitShort_Reg(x86.POP_r, EBX); // sp
            asm.emitShort_Reg(x86.POP_r, EBP); // fp
            asm.emitShort_Reg(x86.POP_r, ECX); // ip
            asm.emit2_Reg_Reg(x86.MOV_r_r32, ESP, EBX);
            asm.emit2_Reg(x86.JMP_r, ECX);
        } else if (f == Unsafe._isEQ) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JNE, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            asm.emitShort_Reg(x86.PUSH_r, ECX);
        } else if (f == Unsafe._isGE) {
            asm.emitShort_Reg_Imm(x86.MOV_r_i32, ECX, 0);
            asm.emitCJUMP_Short(x86.JL, (byte)0);
            int cloc = asm.getCurrentOffset();
            asm.emitShort_Reg(x86.INC_r32, ECX);
            asm.patch1(cloc-1, (byte)(asm.getCurrentOffset()-cloc));
            asm.emitShort_Reg(x86.PUSH_r, ECX);
        } else {
            System.err.println(f.toString());
            Assert.UNREACHABLE();
        }
    }

}
