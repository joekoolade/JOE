// BytecodeInterpreter.java, created Fri Aug 16 16:04:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Interpreter;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_TryCatchBC;
import joeq.Class.jq_Type;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Runtime.Reflection;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BytecodeInterpreter.java,v 1.18 2005/04/29 07:38:59 joewhaley Exp $
 */
public abstract class BytecodeInterpreter {

    public static /*final*/ boolean ALWAYS_TRACE = System.getProperty("interpreter.trace") != null;
    
    /** Creates new Interpreter */
    public BytecodeInterpreter(VMInterface vm, State istate) {
        this.vm = vm; this.istate = istate;
    }

    // create an Interpreter.State and call invokeMethod(m, istate)
    public abstract Object invokeMethod(jq_Method m) throws Throwable;
    public abstract Object invokeUnsafeMethod(jq_Method m) throws Throwable;
    
    protected State istate;
    protected final VMInterface vm;

    public abstract static class State {
        public abstract void push_I(int v);
        public abstract void push_L(long v);
        public abstract void push_F(float v);
        public abstract void push_D(double v);
        public abstract void push_A(Object v);
        public abstract void push(Object v);
        public abstract int pop_I();
        public abstract long pop_L();
        public abstract float pop_F();
        public abstract double pop_D();
        public abstract Object pop_A();
        public abstract Object pop();
        public abstract void popAll();
        public abstract Object peek_A(int depth);
        public abstract void setLocal_I(int i, int v);
        public abstract void setLocal_L(int i, long v);
        public abstract void setLocal_F(int i, float v);
        public abstract void setLocal_D(int i, double v);
        public abstract void setLocal_A(int i, Object v);
        public abstract int getLocal_I(int i);
        public abstract long getLocal_L(int i);
        public abstract float getLocal_F(int i);
        public abstract double getLocal_D(int i);
        public abstract Object getLocal_A(int i);
        public abstract void return_I(int v);
        public abstract void return_L(long v);
        public abstract void return_F(float v);
        public abstract void return_D(double v);
        public abstract void return_A(Object v);
        public abstract void return_V();
        public abstract int getReturnVal_I();
        public abstract long getReturnVal_L();
        public abstract float getReturnVal_F();
        public abstract double getReturnVal_D();
        public abstract Object getReturnVal_A();
    }
    
    public abstract static class VMInterface {
        public abstract Object new_obj(jq_Type t);
        public abstract Object new_array(jq_Type t, int length);
        public abstract Object checkcast(Object o, jq_Type t);
        public abstract boolean instance_of(Object o, jq_Type t);
        public abstract int arraylength(Object o);
        public abstract void monitorenter(Object o, MethodInterpreter v);
        public abstract void monitorexit(Object o);
        public abstract Object multinewarray(int[] dims, jq_Type t);
    }

    public static class WrappedException extends RuntimeException {
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3977582476543866419L;
        Throwable t;
        WrappedException(Throwable t) { this.t = t; }
        public String toString() { return "WrappedException: "+t; }
    }
    
    class MethodInterpreter extends BytecodeVisitor {
        
        MethodInterpreter(jq_Method method) {
            super(method);
            i_end = -1;
            String s = method.getDeclaringClass().getName().toString();
            int i = s.lastIndexOf('.');
            name = s.substring(i+1)+"/"+method.getName();
            TRACE = ALWAYS_TRACE;
            out = System.err;
        }

        final String name;
        public String toString() {
            return name;
        }
        
        // Workaround for javac bug -> cannot access protected members of inner classes.
        boolean getTraceFlag() { return TRACE; }
        java.io.PrintStream getTraceOut() { return out; }
        
        public void forwardTraversal() throws VerifyError, WrappedException {
            if (this.TRACE) this.out.println(this+": Starting traversal.");
            for (;;) {
                i_start = i_end+1;
                if (i_start >= bcs.length) break;
                try {
                    super.visitBytecode();
                } catch (WrappedException ix) {
                    if (this.TRACE) this.out.println(this+": Exception thrown! "+ix.t);
                    handleException(ix.t);
                } catch (Throwable x) {
                    if (this.TRACE) this.out.println(this+": RuntimeException/Error thrown! "+x);
                    handleException(x);
                }
            }
            if (this.TRACE) this.out.println(this+": Finished traversal.");
        }

        public void continueForwardTraversal() throws VerifyError, WrappedException, ReflectiveInterpreter.MonitorExit {
            for (;;) {
                i_start = i_end+1;
                if (i_start >= bcs.length) break;
                try {
                    super.visitBytecode();
                } catch (ReflectiveInterpreter.MonitorExit x) {
                    throw x;
                } catch (WrappedException ix) {
                    if (this.TRACE) this.out.println(this+": Exception thrown! "+ix.t);
                    handleException(ix.t);
                } catch (Throwable x) {
                    if (this.TRACE) this.out.println(this+": RuntimeException/Error thrown! "+x);
                    handleException(x);
                }
            }
        }
        
        public void visitBytecode() throws WrappedException {
            try {
                super.visitBytecode();
            } catch (WrappedException ix) {
                if (this.TRACE) this.out.println(this+": Exception thrown! "+ix.t);
                handleException(ix.t);
            } catch (Throwable x) {
                if (this.TRACE) this.out.println(this+": RuntimeException/Error thrown! "+x);
                handleException(x);
            }
        }
        
        private void handleException(Throwable x) throws WrappedException {
            jq_Class t = (jq_Class)jq_Reference.getTypeOf(x);
            t.prepare();
            jq_TryCatchBC[] tc = method.getExceptionTable();
            for (int i=0; i<tc.length; ++i) {
                if (tc[i].catches(i_start, t)) {
                    istate.popAll(); istate.push_A(x);
                    branchTo(tc[i].getHandlerPC());
                    if (this.TRACE) this.out.println(this+": Branching to exception handler "+tc[i]);
                    return;
                }
            }
            if (this.TRACE) this.out.println(this+": Uncaught exception, exiting method.");
            throw new WrappedException(x);
        }

        protected void branchTo(int target) {
            i_end = target-1;
        }
        
        public void visitNOP() {
            super.visitNOP();
        }
        public void visitACONST(Object s) {
            super.visitACONST(s);
            istate.push_A(s);
        }
        public void visitICONST(int c) {
            super.visitICONST(c);
            istate.push_I(c);
        }
        public void visitLCONST(long c) {
            super.visitLCONST(c);
            istate.push_L(c);
        }
        public void visitFCONST(float c) {
            super.visitFCONST(c);
            istate.push_F(c);
        }
        public void visitDCONST(double c) {
            super.visitDCONST(c);
            istate.push_D(c);
        }
        public void visitILOAD(int i) {
            super.visitILOAD(i);
            istate.push_I(istate.getLocal_I(i));
        }
        public void visitLLOAD(int i) {
            super.visitLLOAD(i);
            istate.push_L(istate.getLocal_L(i));
        }
        public void visitFLOAD(int i) {
            super.visitFLOAD(i);
            istate.push_F(istate.getLocal_F(i));
        }
        public void visitDLOAD(int i) {
            super.visitDLOAD(i);
            istate.push_D(istate.getLocal_D(i));
        }
        public void visitALOAD(int i) {
            super.visitALOAD(i);
            istate.push_A(istate.getLocal_A(i));
        }
        public void visitISTORE(int i) {
            super.visitISTORE(i);
            istate.setLocal_I(i, istate.pop_I());
        }
        public void visitLSTORE(int i) {
            super.visitLSTORE(i);
            istate.setLocal_L(i, istate.pop_L());
        }
        public void visitFSTORE(int i) {
            super.visitFSTORE(i);
            istate.setLocal_F(i, istate.pop_F());
        }
        public void visitDSTORE(int i) {
            super.visitDSTORE(i);
            istate.setLocal_D(i, istate.pop_D());
        }
        public void visitASTORE(int i) {
            super.visitASTORE(i);
            istate.setLocal_A(i, istate.pop_A());
        }
        public void visitIALOAD() {
            super.visitIALOAD();
            int index = istate.pop_I();
            int[] array = (int[])istate.pop_A();
            istate.push_I(array[index]);
        }
        public void visitLALOAD() {
            super.visitLALOAD();
            int index = istate.pop_I();
            long[] array = (long[])istate.pop_A();
            istate.push_L(array[index]);
        }
        public void visitFALOAD() {
            super.visitFALOAD();
            int index = istate.pop_I();
            float[] array = (float[])istate.pop_A();
            istate.push_F(array[index]);
        }
        public void visitDALOAD() {
            super.visitDALOAD();
            int index = istate.pop_I();
            double[] array = (double[])istate.pop_A();
            istate.push_D(array[index]);
        }
        public void visitAALOAD() {
            super.visitAALOAD();
            int index = istate.pop_I();
            Object[] array = (Object[])istate.pop_A();
            istate.push_A(array[index]);
        }
        public void visitBALOAD() {
            super.visitBALOAD();
            int index = istate.pop_I();
            Object array = (Object)istate.pop_A();
            int val;
            try {
                if (array.getClass() == Class.forName("[Z")) val = ((boolean[])array)[index]?1:0;
                else val = ((byte[])array)[index];
            } catch (ClassNotFoundException x) { Assert.UNREACHABLE(); return; }
            istate.push_I(val);
        }
        public void visitCALOAD() {
            super.visitCALOAD();
            int index = istate.pop_I();
            char[] array = (char[])istate.pop_A();
            istate.push_I(array[index]);
        }
        public void visitSALOAD() {
            super.visitSALOAD();
            int index = istate.pop_I();
            short[] array = (short[])istate.pop_A();
            istate.push_I(array[index]);
        }
        public void visitIASTORE() {
            super.visitIASTORE();
            int val = istate.pop_I();
            int index = istate.pop_I();
            int[] array = (int[])istate.pop_A();
            array[index] = val;
        }
        public void visitLASTORE() {
            super.visitLASTORE();
            long val = istate.pop_L();
            int index = istate.pop_I();
            long[] array = (long[])istate.pop_A();
            array[index] = val;
        }
        public void visitFASTORE() {
            super.visitFASTORE();
            float val = istate.pop_F();
            int index = istate.pop_I();
            float[] array = (float[])istate.pop_A();
            array[index] = val;
        }
        public void visitDASTORE() {
            super.visitDASTORE();
            double val = istate.pop_D();
            int index = istate.pop_I();
            double[] array = (double[])istate.pop_A();
            array[index] = val;
        }
        public void visitAASTORE() {
            super.visitAASTORE();
            Object val = istate.pop_A();
            int index = istate.pop_I();
            Object[] array = (Object[])istate.pop_A();
            array[index] = val;
        }
        public void visitBASTORE() {
            super.visitBASTORE();
            int val = istate.pop_I();
            int index = istate.pop_I();
            Object array = (Object)istate.pop_A();
            try {
                if (array.getClass() == Class.forName("[Z")) ((boolean[])array)[index] = val!=0;
                else ((byte[])array)[index] = (byte)val;
            } catch (ClassNotFoundException x) { Assert.UNREACHABLE(); }
        }
        public void visitCASTORE() {
            super.visitCASTORE();
            int val = istate.pop_I();
            int index = istate.pop_I();
            char[] array = (char[])istate.pop_A();
            array[index] = (char)val;
        }
        public void visitSASTORE() {
            super.visitSASTORE();
            int val = istate.pop_I();
            int index = istate.pop_I();
            short[] array = (short[])istate.pop_A();
            array[index] = (short)val;
        }
        public void visitPOP() {
            super.visitPOP();
            istate.pop();
        }
        public void visitPOP2() {
            super.visitPOP2();
            istate.pop();
            istate.pop();
        }
        public void visitDUP() {
            super.visitDUP();
            Object o = istate.pop();
            istate.push(o);
            istate.push(o);
        }
        public void visitDUP_x1() {
            super.visitDUP_x1();
            Object o1 = istate.pop();
            Object o2 = istate.pop();
            istate.push(o1);
            istate.push(o2);
            istate.push(o1);
        }
        public void visitDUP_x2() {
            super.visitDUP_x2();
            Object o1 = istate.pop();
            Object o2 = istate.pop();
            Object o3 = istate.pop();
            istate.push(o1);
            istate.push(o3);
            istate.push(o2);
            istate.push(o1);
        }
        public void visitDUP2() {
            super.visitDUP2();
            Object o1 = istate.pop();
            Object o2 = istate.pop();
            istate.push(o2);
            istate.push(o1);
            istate.push(o2);
            istate.push(o1);
        }
        public void visitDUP2_x1() {
            super.visitDUP2_x1();
            Object o1 = istate.pop();
            Object o2 = istate.pop();
            Object o3 = istate.pop();
            istate.push(o2);
            istate.push(o1);
            istate.push(o3);
            istate.push(o2);
            istate.push(o1);
        }
        public void visitDUP2_x2() {
            super.visitDUP2_x2();
            Object o1 = istate.pop();
            Object o2 = istate.pop();
            Object o3 = istate.pop();
            Object o4 = istate.pop();
            istate.push(o2);
            istate.push(o1);
            istate.push(o4);
            istate.push(o3);
            istate.push(o2);
            istate.push(o1);
        }
        public void visitSWAP() {
            super.visitSWAP();
            Object o1 = istate.pop();
            Object o2 = istate.pop();
            istate.push(o1);
            istate.push(o2);
        }
        public void visitIBINOP(byte op) {
            super.visitIBINOP(op);
            int v1 = istate.pop_I();
            int v2 = istate.pop_I();
            switch(op) {
                case BINOP_ADD:
                    istate.push_I(v2+v1);
                    break;
                case BINOP_SUB:
                    istate.push_I(v2-v1);
                    break;
                case BINOP_MUL:
                    istate.push_I(v2*v1);
                    break;
                case BINOP_DIV:
                    istate.push_I(v2/v1);
                    break;
                case BINOP_REM:
                    istate.push_I(v2%v1);
                    break;
                case BINOP_AND:
                    istate.push_I(v2&v1);
                    break;
                case BINOP_OR:
                    istate.push_I(v2|v1);
                    break;
                case BINOP_XOR:
                    istate.push_I(v2^v1);
                    break;
                default:
                    Assert.UNREACHABLE();
            }
        }
        public void visitLBINOP(byte op) {
            super.visitLBINOP(op);
            long v1 = istate.pop_L();
            long v2 = istate.pop_L();
            switch(op) {
                case BINOP_ADD:
                    istate.push_L(v2+v1);
                    break;
                case BINOP_SUB:
                    istate.push_L(v2-v1);
                    break;
                case BINOP_MUL:
                    istate.push_L(v2*v1);
                    break;
                case BINOP_DIV:
                    istate.push_L(v2/v1);
                    break;
                case BINOP_REM:
                    istate.push_L(v2%v1);
                    break;
                case BINOP_AND:
                    istate.push_L(v2&v1);
                    break;
                case BINOP_OR:
                    istate.push_L(v2|v1);
                    break;
                case BINOP_XOR:
                    istate.push_L(v2^v1);
                    break;
                default:
                    Assert.UNREACHABLE();
            }
        }
        public void visitFBINOP(byte op) {
            super.visitFBINOP(op);
            float v1 = istate.pop_F();
            float v2 = istate.pop_F();
            switch(op) {
                case BINOP_ADD:
                    istate.push_F(v2+v1);
                    break;
                case BINOP_SUB:
                    istate.push_F(v2-v1);
                    break;
                case BINOP_MUL:
                    istate.push_F(v2*v1);
                    break;
                case BINOP_DIV:
                    istate.push_F(v2/v1);
                    break;
                case BINOP_REM:
                    istate.push_F(v2%v1);
                    break;
                default:
                    Assert.UNREACHABLE();
            }
        }
        public void visitDBINOP(byte op) {
            super.visitDBINOP(op);
            double v1 = istate.pop_D();
            double v2 = istate.pop_D();
            switch(op) {
                case BINOP_ADD:
                    istate.push_D(v2+v1);
                    break;
                case BINOP_SUB:
                    istate.push_D(v2-v1);
                    break;
                case BINOP_MUL:
                    istate.push_D(v2*v1);
                    break;
                case BINOP_DIV:
                    istate.push_D(v2/v1);
                    break;
                case BINOP_REM:
                    istate.push_D(v2%v1);
                    break;
                default:
                    Assert.UNREACHABLE();
            }
        }
        public void visitIUNOP(byte op) {
            super.visitIUNOP(op);
            Assert._assert(op == UNOP_NEG);
            istate.push_I(-istate.pop_I());
        }
        public void visitLUNOP(byte op) {
            super.visitLUNOP(op);
            Assert._assert(op == UNOP_NEG);
            istate.push_L(-istate.pop_L());
        }
        public void visitFUNOP(byte op) {
            super.visitFUNOP(op);
            Assert._assert(op == UNOP_NEG);
            istate.push_F(-istate.pop_F());
        }
        public void visitDUNOP(byte op) {
            super.visitDUNOP(op);
            Assert._assert(op == UNOP_NEG);
            istate.push_D(-istate.pop_D());
        }
        public void visitISHIFT(byte op) {
            super.visitISHIFT(op);
            int v1 = istate.pop_I();
            int v2 = istate.pop_I();
            switch(op) {
                case SHIFT_LEFT:
                    istate.push_I(v2 << v1);
                    break;
                case SHIFT_RIGHT:
                    istate.push_I(v2 >> v1);
                    break;
                case SHIFT_URIGHT:
                    istate.push_I(v2 >>> v1);
                    break;
                default:
                    Assert.UNREACHABLE();
            }
        }
        public void visitLSHIFT(byte op) {
            super.visitLSHIFT(op);
            int v1 = istate.pop_I();
            long v2 = istate.pop_L();
            switch(op) {
                case SHIFT_LEFT:
                    istate.push_L(v2 << v1);
                    break;
                case SHIFT_RIGHT:
                    istate.push_L(v2 >> v1);
                    break;
                case SHIFT_URIGHT:
                    istate.push_L(v2 >>> v1);
                    break;
                default:
                    Assert.UNREACHABLE();
            }
        }
        public void visitIINC(int i, int v) {
            super.visitIINC(i, v);
            istate.setLocal_I(i, istate.getLocal_I(i)+v);
        }
        public void visitI2L() {
            super.visitI2L();
            istate.push_L((long)istate.pop_I());
        }
        public void visitI2F() {
            super.visitI2F();
            istate.push_F((float)istate.pop_I());
        }
        public void visitI2D() {
            super.visitI2D();
            istate.push_D((double)istate.pop_I());
        }
        public void visitL2I() {
            super.visitL2I();
            istate.push_I((int)istate.pop_L());
        }
        public void visitL2F() {
            super.visitL2F();
            istate.push_F((float)istate.pop_L());
        }
        public void visitL2D() {
            super.visitL2D();
            istate.push_D((double)istate.pop_L());
        }
        public void visitF2I() {
            super.visitF2I();
            istate.push_I((int)istate.pop_F());
        }
        public void visitF2L() {
            super.visitF2L();
            istate.push_L((long)istate.pop_F());
        }
        public void visitF2D() {
            super.visitF2D();
            istate.push_D((double)istate.pop_F());
        }
        public void visitD2I() {
            super.visitD2I();
            istate.push_I((int)istate.pop_D());
        }
        public void visitD2L() {
            super.visitD2L();
            istate.push_L((long)istate.pop_D());
        }
        public void visitD2F() {
            super.visitD2F();
            istate.push_F((float)istate.pop_D());
        }
        public void visitI2B() {
            super.visitI2B();
            istate.push_I((byte)istate.pop_I());
        }
        public void visitI2C() {
            super.visitI2C();
            istate.push_I((char)istate.pop_I());
        }
        public void visitI2S() {
            super.visitI2S();
            istate.push_I((short)istate.pop_I());
        }
        public void visitLCMP2() {
            super.visitLCMP2();
            long v1 = istate.pop_L();
            long v2 = istate.pop_L();
            istate.push_I((v2>v1)?1:((v2==v1)?0:-1));
        }
        public void visitFCMP2(byte op) {
            super.visitFCMP2(op);
            float v1 = istate.pop_F();
            float v2 = istate.pop_F();
            int val;
            if (op == CMP_L)
                val = ((v2>v1)?1:((v2==v1)?0:-1));
            else
                val = ((v2<v1)?-1:((v2==v1)?0:1));
            istate.push_I(val);
        }
        public void visitDCMP2(byte op) {
            super.visitDCMP2(op);
            double v1 = istate.pop_D();
            double v2 = istate.pop_D();
            int val;
            if (op == CMP_L)
                val = ((v2>v1)?1:((v2==v1)?0:-1));
            else
                val = ((v2<v1)?-1:((v2==v1)?0:1));
            istate.push_I(val);
        }
        public void visitIF(byte op, int target) {
            super.visitIF(op, target);
            int v = istate.pop_I();
            switch(op) {
                case CMP_EQ: if (v==0) branchTo(target); break;
                case CMP_NE: if (v!=0) branchTo(target); break;
                case CMP_LT: if (v<0) branchTo(target); break;
                case CMP_GE: if (v>=0) branchTo(target); break;
                case CMP_LE: if (v<=0) branchTo(target); break;
                case CMP_GT: if (v>0) branchTo(target); break;
                default: Assert.UNREACHABLE();
            }
        }
        public void visitIFREF(byte op, int target) {
            super.visitIFREF(op, target);
            Object v = istate.pop_A();
            switch(op) {
                case CMP_EQ: if (v==null) branchTo(target); break;
                case CMP_NE: if (v!=null) branchTo(target); break;
                default: Assert.UNREACHABLE();
            }
        }
        public void visitIFCMP(byte op, int target) {
            super.visitIFCMP(op, target);
            int v1 = istate.pop_I();
            int v2 = istate.pop_I();
            switch(op) {
                case CMP_EQ: if (v2==v1) branchTo(target); break;
                case CMP_NE: if (v2!=v1) branchTo(target); break;
                case CMP_LT: if (v2<v1) branchTo(target); break;
                case CMP_GE: if (v2>=v1) branchTo(target); break;
                case CMP_LE: if (v2<=v1) branchTo(target); break;
                case CMP_GT: if (v2>v1) branchTo(target); break;
                default: Assert.UNREACHABLE();
            }
        }
        public void visitIFREFCMP(byte op, int target) {
            super.visitIFREFCMP(op, target);
            Object v1 = istate.pop_A();
            Object v2 = istate.pop_A();
            switch(op) {
                case CMP_EQ: if (v2==v1) branchTo(target); break;
                case CMP_NE: if (v2!=v1) branchTo(target); break;
                default: Assert.UNREACHABLE();
            }
        }
        public void visitGOTO(int target) {
            super.visitGOTO(target);
            branchTo(target);
        }
        public void visitJSR(int target) {
            super.visitJSR(target);
            istate.push_I(i_end+1);
            branchTo(target);
        }
        public void visitRET(int i) {
            super.visitRET(i);
            branchTo(istate.getLocal_I(i));
        }
        public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
            super.visitTABLESWITCH(default_target, low, high, targets);
            int v = istate.pop_I();
            if ((v < low) || (v > high)) branchTo(default_target);
            else branchTo(targets[v-low]);
        }
        public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
            super.visitLOOKUPSWITCH(default_target, values, targets);
            int v = istate.pop_I();
            for (int i=0; i<values.length; ++i) {
                if (v == values[i]) {
                    branchTo(targets[i]);
                    return;
                }
            }
            branchTo(default_target);
        }
        public void visitIRETURN() {
            super.visitIRETURN();
            istate.return_I(istate.pop_I());
            i_end = bcs.length;
        }
        public void visitLRETURN() {
            super.visitLRETURN();
            istate.return_L(istate.pop_L());
            i_end = bcs.length;
        }
        public void visitFRETURN() {
            super.visitFRETURN();
            istate.return_F(istate.pop_F());
            i_end = bcs.length;
        }
        public void visitDRETURN() {
            super.visitDRETURN();
            istate.return_D(istate.pop_D());
            i_end = bcs.length;
        }
        public void visitARETURN() {
            super.visitARETURN();
            istate.return_A(istate.pop_A());
            i_end = bcs.length;
        }
        public void visitVRETURN() {
            super.visitVRETURN();
            istate.return_V();
            i_end = bcs.length;
        }
        public void visitIGETSTATIC(jq_StaticField f) {
            super.visitIGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_I(Reflection.getstatic_I(f));
        }
        public void visitLGETSTATIC(jq_StaticField f) {
            super.visitLGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_L(Reflection.getstatic_L(f));
        }
        public void visitFGETSTATIC(jq_StaticField f) {
            super.visitFGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_F(Reflection.getstatic_F(f));
        }
        public void visitDGETSTATIC(jq_StaticField f) {
            super.visitDGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_D(Reflection.getstatic_D(f));
        }
        public void visitAGETSTATIC(jq_StaticField f) {
            super.visitAGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_A(Reflection.getstatic_A(f));
        }
        public void visitZGETSTATIC(jq_StaticField f) {
            super.visitZGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_I(Reflection.getstatic_Z(f)?1:0);
        }
        public void visitBGETSTATIC(jq_StaticField f) {
            super.visitBGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_I(Reflection.getstatic_B(f));
        }
        public void visitCGETSTATIC(jq_StaticField f) {
            super.visitCGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_I(Reflection.getstatic_C(f));
        }
        public void visitSGETSTATIC(jq_StaticField f) {
            super.visitSGETSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            istate.push_I(Reflection.getstatic_S(f));
        }
        public void visitIPUTSTATIC(jq_StaticField f) {
            super.visitIPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_I(f, istate.pop_I());
        }
        public void visitLPUTSTATIC(jq_StaticField f) {
            super.visitLPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_L(f, istate.pop_L());
        }
        public void visitFPUTSTATIC(jq_StaticField f) {
            super.visitFPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_F(f, istate.pop_F());
        }
        public void visitDPUTSTATIC(jq_StaticField f) {
            super.visitDPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_D(f, istate.pop_D());
        }
        public void visitAPUTSTATIC(jq_StaticField f) {
            super.visitAPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_A(f, istate.pop_A());
        }
        public void visitZPUTSTATIC(jq_StaticField f) {
            super.visitZPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_Z(f, istate.pop_I()!=0);
        }
        public void visitBPUTSTATIC(jq_StaticField f) {
            super.visitBPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_B(f, (byte)istate.pop_I());
        }
        public void visitCPUTSTATIC(jq_StaticField f) {
            super.visitCPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_C(f, (char)istate.pop_I());
        }
        public void visitSPUTSTATIC(jq_StaticField f) {
            super.visitSPUTSTATIC(f);
            f = tryResolve(f);
            f.getDeclaringClass().cls_initialize();
            Reflection.putstatic_S(f, (short)istate.pop_I());
        }
        public void visitIGETFIELD(jq_InstanceField f) {
            super.visitIGETFIELD(f);
            f = tryResolve(f);
            istate.push_I(Reflection.getfield_I(istate.pop_A(), f));
        }
        public void visitLGETFIELD(jq_InstanceField f) {
            super.visitLGETFIELD(f);
            f = tryResolve(f);
            istate.push_L(Reflection.getfield_L(istate.pop_A(), f));
        }
        public void visitFGETFIELD(jq_InstanceField f) {
            super.visitFGETFIELD(f);
            f = tryResolve(f);
            istate.push_F(Reflection.getfield_F(istate.pop_A(), f));
        }
        public void visitDGETFIELD(jq_InstanceField f) {
            super.visitDGETFIELD(f);
            f = tryResolve(f);
            istate.push_D(Reflection.getfield_D(istate.pop_A(), f));
        }
        public void visitAGETFIELD(jq_InstanceField f) {
            super.visitAGETFIELD(f);
            f = tryResolve(f);
            istate.push_A(Reflection.getfield_A(istate.pop_A(), f));
        }
        public void visitBGETFIELD(jq_InstanceField f) {
            super.visitBGETFIELD(f);
            f = tryResolve(f);
            istate.push_I(Reflection.getfield_B(istate.pop_A(), f));
        }
        public void visitCGETFIELD(jq_InstanceField f) {
            super.visitCGETFIELD(f);
            f = tryResolve(f);
            istate.push_I(Reflection.getfield_C(istate.pop_A(), f));
        }
        public void visitSGETFIELD(jq_InstanceField f) {
            super.visitSGETFIELD(f);
            f = tryResolve(f);
            istate.push_I(Reflection.getfield_S(istate.pop_A(), f));
        }
        public void visitZGETFIELD(jq_InstanceField f) {
            super.visitZGETFIELD(f);
            f = tryResolve(f);
            istate.push_I(Reflection.getfield_Z(istate.pop_A(), f)?1:0);
        }
        public void visitIPUTFIELD(jq_InstanceField f) {
            super.visitIPUTFIELD(f);
            f = tryResolve(f);
            int v = istate.pop_I();
            Reflection.putfield_I(istate.pop_A(), f, v);
        }
        public void visitLPUTFIELD(jq_InstanceField f) {
            super.visitLPUTFIELD(f);
            f = tryResolve(f);
            long v = istate.pop_L();
            Reflection.putfield_L(istate.pop_A(), f, v);
        }
        public void visitFPUTFIELD(jq_InstanceField f) {
            super.visitFPUTFIELD(f);
            f = tryResolve(f);
            float v = istate.pop_F();
            Reflection.putfield_F(istate.pop_A(), f, v);
        }
        public void visitDPUTFIELD(jq_InstanceField f) {
            super.visitDPUTFIELD(f);
            f = tryResolve(f);
            double v = istate.pop_D();
            Reflection.putfield_D(istate.pop_A(), f, v);
        }
        public void visitAPUTFIELD(jq_InstanceField f) {
            super.visitAPUTFIELD(f);
            f = tryResolve(f);
            Object v = istate.pop_A();
            Reflection.putfield_A(istate.pop_A(), f, v);
        }
        public void visitBPUTFIELD(jq_InstanceField f) {
            super.visitBPUTFIELD(f);
            f = tryResolve(f);
            byte v = (byte)istate.pop_I();
            Reflection.putfield_B(istate.pop_A(), f, v);
        }
        public void visitCPUTFIELD(jq_InstanceField f) {
            super.visitCPUTFIELD(f);
            f = tryResolve(f);
            char v = (char)istate.pop_I();
            Reflection.putfield_C(istate.pop_A(), f, v);
        }
        public void visitSPUTFIELD(jq_InstanceField f) {
            super.visitSPUTFIELD(f);
            f = tryResolve(f);
            short v = (short)istate.pop_I();
            Reflection.putfield_S(istate.pop_A(), f, v);
        }
        public void visitZPUTFIELD(jq_InstanceField f) {
            super.visitZPUTFIELD(f);
            f = tryResolve(f);
            boolean v = istate.pop_I()!=0;
            Reflection.putfield_Z(istate.pop_A(), f, v);
        }
        protected Object INVOKEhelper(byte op, jq_Method f) {
            f = (jq_Method) tryResolve(f);
            jq_Class k = f.getDeclaringClass();
            k.cls_initialize();
            jq_Class _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/Unsafe;");
            if (k == _class || k.isAddressType()) {
                try {
                    // redirect call
                    return invokeUnsafeMethod(f);
                } catch (Throwable t) {
                    if (this.TRACE) this.out.println(this+": "+f+" threw "+t);
                    throw new WrappedException(t);
                }
            }
            if (op == INVOKE_SPECIAL) {
                f = jq_Class.getInvokespecialTarget(clazz, (jq_InstanceMethod)f);
            } else if (op != INVOKE_STATIC) {
                Object o = istate.peek_A(f.getParamWords()-1);
                jq_Reference t = jq_Reference.getTypeOf(o);
                t.cls_initialize();
                if (op == INVOKE_INTERFACE) {
                    if (!t.implementsInterface(f.getDeclaringClass()))
                        throw new IncompatibleClassChangeError();
                    if (t.isArrayType()) t = PrimordialClassLoader.getJavaLangObject();
                } else {
                    Assert._assert(op == INVOKE_VIRTUAL);
                }
                jq_Method f2 = f;
                f = t.getVirtualMethod(f.getNameAndDesc());
                if (this.TRACE) this.out.println(this+": virtual method target "+f);
                if (f == null)
                    throw new AbstractMethodError("no such method "+f2.toString()+" in type "+t);
                if (f.isAbstract())
                    throw new AbstractMethodError("method "+f2.toString()+" on type "+t+" is abstract");
            } else {
                // static call
            }
            try {
                return invokeMethod(f);
            } catch (Throwable t) {
                if (this.TRACE) this.out.println(this+": "+f+" threw "+t);
                throw new WrappedException(t);
            }
        }
        public void visitIINVOKE(byte op, jq_Method f) {
            super.visitIINVOKE(op, f);
            istate.push_I(((Integer)INVOKEhelper(op, f)).intValue());
        }
        public void visitLINVOKE(byte op, jq_Method f) {
            super.visitLINVOKE(op, f);
            istate.push_L(((Long)INVOKEhelper(op, f)).longValue());
        }
        public void visitFINVOKE(byte op, jq_Method f) {
            super.visitFINVOKE(op, f);
            istate.push_F(((Float)INVOKEhelper(op, f)).floatValue());
        }
        public void visitDINVOKE(byte op, jq_Method f) {
            super.visitDINVOKE(op, f);
            istate.push_D(((Double)INVOKEhelper(op, f)).doubleValue());
        }
        public void visitAINVOKE(byte op, jq_Method f) {
            super.visitAINVOKE(op, f);
            istate.push_A(INVOKEhelper(op, f));
        }
        public void visitVINVOKE(byte op, jq_Method f) {
            super.visitVINVOKE(op, f);
            INVOKEhelper(op, f);
        }
        public void visitNEW(jq_Type f) {
            super.visitNEW(f);
            istate.push_A(vm.new_obj(f));
        }
        public void visitNEWARRAY(jq_Array f) {
            super.visitNEWARRAY(f);
            istate.push_A(vm.new_array(f, istate.pop_I()));
        }
        public void visitCHECKCAST(jq_Type f) {
            super.visitCHECKCAST(f);
            istate.push_A(vm.checkcast(istate.pop_A(), f));
        }
        public void visitINSTANCEOF(jq_Type f) {
            super.visitINSTANCEOF(f);
            istate.push_I(vm.instance_of(istate.pop_A(), f)?1:0);
        }
        public void visitARRAYLENGTH() {
            super.visitARRAYLENGTH();
            istate.push_I(vm.arraylength(istate.pop_A()));
        }
        public void visitATHROW() {
            super.visitATHROW();
            throw new WrappedException((Throwable)istate.pop_A());
        }
        public void visitMONITOR(byte op) {
            super.visitMONITOR(op);
            Object v = istate.pop_A();
            if (op == MONITOR_ENTER) vm.monitorenter(v, this);
            else vm.monitorexit(v);
        }
        public void visitMULTINEWARRAY(jq_Type f, char dim) {
            super.visitMULTINEWARRAY(f, dim);
            int[] dims = new int[dim];
            //for (int i=0; i<dim; ++i) f = ((jq_Array)f).getElementType();
            for (int i=0; i<dim; ++i) dims[dim-i-1] = istate.pop_I();
            istate.push_A(vm.multinewarray(dims, f));
        }
    }
    
}
