//BytecodeVisitor.java, created Fri Jan 11 16:49:00 2002 by joewhaley
//Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
//Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.io.PrintStream;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Compiler.CompilationConstants;
import joeq.Compiler.CompilationState;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/*
* @author  John Whaley <jwhaley@alum.mit.edu>
* @version $Id: BytecodeVisitor.java,v 1.21 2004/09/22 22:17:34 joewhaley Exp $
*/
public class BytecodeVisitor implements jq_ClassFileConstants, CompilationConstants {

 protected final CompilationState state;
 protected final jq_Class clazz;
 protected final jq_Method method;
 protected final byte[] bcs;
 protected int i_start, i_end;
 protected boolean TRACE = false;
 protected PrintStream out = System.out;
 
 /** Creates new BytecodeVisitor */
 public BytecodeVisitor(jq_Method method) {
     this(CompilationState.DEFAULT, method);
 }
 
 /** Creates new BytecodeVisitor */
 public BytecodeVisitor(CompilationState state, jq_Method method) {
     this.state = state;
     this.clazz = method.getDeclaringClass();
     this.method = method;
     this.bcs = method.getBytecode();
     Assert._assert(this.bcs != null, "Method "+this.method+" has no bytecode!");
 }

 public void forwardTraversal() throws VerifyError {
     for (i_end=-1; ; ) {
         i_start = i_end+1;
         if (i_start >= bcs.length) break;
         this.visitBytecode();
     }
 }
 
 public jq_StaticField tryResolve(jq_StaticField m) {
     try {
         jq_StaticField m2 = (jq_StaticField) state.tryResolve(m);
         if (m != m2) updateMemberReference(m2, CONSTANT_ResolvedSFieldRef);
         return m2;
     } catch (Error e) {
         System.err.println("Method "+method+" bc index "+i_start+": Error when resolving "+m+": "+e);
         throw e;
     }
 }
 
 public jq_InstanceField tryResolve(jq_InstanceField m) {
     try {
         jq_InstanceField m2 = (jq_InstanceField) state.tryResolve(m);
         if (m != m2) updateMemberReference(m2, CONSTANT_ResolvedIFieldRef);
         return m2;
     } catch (Error e) {
         System.err.println("Method "+method+" bc index "+i_start+": Error when resolving "+m+": "+e);
         throw e;
     }
 }
 
 public jq_StaticMethod tryResolve(jq_StaticMethod m) {
     try {
         jq_StaticMethod m2 = (jq_StaticMethod) state.tryResolve(m);
         if (m != m2) updateMemberReference(m2, CONSTANT_ResolvedSMethodRef);
         return m2;
     } catch (Error e) {
         System.err.println("Method "+method+" bc index "+i_start+": Error when resolving "+m+": "+e);
         throw e;
     }
 }
 
 public jq_InstanceMethod tryResolve(jq_InstanceMethod m) {
     try {
         jq_InstanceMethod m2 = (jq_InstanceMethod) state.tryResolve(m);
         if (m != m2) updateMemberReference(m2, CONSTANT_ResolvedIMethodRef);
         return m2;
     } catch (Error e) {
         System.err.println("Method "+method+" bc index "+i_start+": Error when resolving "+m+": "+e);
         throw e;
     }
 }
 
 public jq_Member tryResolve(jq_Member m) {
     try {
         jq_Member m2 = state.tryResolve(m);
         if (m != m2) {
             byte tag = 0;
             if (m instanceof jq_InstanceField)
                tag = CONSTANT_ResolvedIFieldRef;
             else if (m instanceof jq_StaticField)
                 tag = CONSTANT_ResolvedSFieldRef;
             else if (m instanceof jq_InstanceMethod)
                 tag = CONSTANT_ResolvedIMethodRef;
             else if (m instanceof jq_StaticMethod)
                 tag = CONSTANT_ResolvedSMethodRef;
             else Assert.UNREACHABLE();
             updateMemberReference(m2, tag);
         }
         return m2;
     } catch (Error e) {
         System.err.println("Method "+method+" bc index "+i_start+": Error when resolving "+m+": "+e);
         throw e;
     }
 }
 
 public void updateCPIndex(char index) {
     int i_size = i_end - i_start;
     char op = (char)(bcs[i_start] & 0xff);
     switch (i_size) {
         case 1:
             // ldc
             Assert._assert(op == 0x12);
             Assert._assert(index <= 127);
             bcs[i_end] = (byte)index;
             break;
         case 2:
             // ldc_w, ldc2_w
             // getstatic, putstatic
             // getfield, putfield
             // invokevirtual, invokespecial, invokestatic
             // new, anewarray
             // checkcast, instanceof
             Assert._assert(op == 0x13 || op == 0x14 ||
                       op == 0xb2 || op == 0xb3 ||
                       op == 0xb4 || op == 0xb5 ||
                       op == 0xb6 || op == 0xb7 || op == 0xb8 ||
                       op == 0xbb || op == 0xbd ||
                       op == 0xc0 || op == 0xc1);
             bcs[i_end-1] = (byte)(index >> 8);
             bcs[i_end] = (byte)index;
             break;
         case 3:
             // multianewarray
             Assert._assert(op == 0xc5);
             bcs[i_end-2] = (byte)(index >> 8);
             bcs[i_end-1] = (byte)index;
             break;
         case 4:
             // invokeinterface
             Assert._assert(op == 0xb9);
             bcs[i_end-3] = (byte)(index >> 8);
             bcs[i_end-2] = (byte)index;
             break;
         default:
             Assert.UNREACHABLE(Strings.hex(op));
             return;
     }
 }
 
 public void updateMemberReference(jq_Member m, byte tag) {
     char index;
     int i_size = i_end - i_start;
     char op = (char)(bcs[i_start] & 0xff);
     switch (i_size) {
         case 1:
             // ldc
             Assert._assert(op == 0x12);
             --i_end; index = getUnsignedByte();
             break;
         case 2:
             // ldc_w, ldc2_w
             // getstatic, putstatic
             // getfield, putfield
             // invokevirtual, invokespecial, invokestatic
             // new, anewarray
             // checkcast, instanceof
             Assert._assert(op == 0x13 || op == 0x14 ||
                       op == 0xb2 || op == 0xb3 ||
                       op == 0xb4 || op == 0xb5 ||
                       op == 0xb6 || op == 0xb7 || op == 0xb8 ||
                       op == 0xbb || op == 0xbd ||
                       op == 0xc0 || op == 0xc1);
             i_end-=2; index = getUnsignedWord();
             break;
         case 3:
             // multianewarray
             Assert._assert(op == 0xc5);
             i_end-=3; index = getUnsignedWord(); getUnsignedByte();
             break;
         case 4:
             // invokeinterface
             Assert._assert(op == 0xb9);
             i_end-=4; index = getUnsignedWord(); getUnsignedByte(); getSignedByte();
             break;
         default:
             Assert.UNREACHABLE(Strings.hex(op));
             return;
     }
     clazz.getCP().set(index, m, tag);
 }
 
 public void visitBytecode() throws VerifyError {
     char bc = getUnsignedByte();
     switch (bc) {
         case 0x00: /* --- nop --- */ {
             this.visitNOP();
             break;
         }
         case 0x01: /* --- aconst_null --- */ {
             this.visitACONST(null);
             break;
         }
         case 0x02: /* --- iconst_m1 --- */
         case 0x03: /* --- iconst_0 --- */
         case 0x04: /* --- iconst_1 --- */
         case 0x05: /* --- iconst_2 --- */
         case 0x06: /* --- iconst_3 --- */
         case 0x07: /* --- iconst_4 --- */
         case 0x08: /* --- iconst_5 --- */ {
             this.visitICONST(bc-0x03);
             break;
         }
         case 0x09: /* --- lconst_0 --- */
         case 0x0a: /* --- lconst_1 --- */ {
             this.visitLCONST((long)(bc-0x09));
             break;
         }
         case 0x0b: /* --- fconst_0 --- */ {
             this.visitFCONST(0.f);
             break;
         }
         case 0x0c: /* --- fconst_1 --- */ {
             this.visitFCONST(1.f);
             break;
         }
         case 0x0d: /* --- fconst_2 --- */ {
             this.visitFCONST(2.f);
             break;
         }
         case 0x0e: /* --- dconst_0 --- */ {
             this.visitDCONST(0.);
             break;
         }
         case 0x0f: /* --- dconst_1 --- */ {
             this.visitDCONST(1.);
             break;
         }
         case 0x10: /* --- bipush --- */ {
             byte v = getSignedByte();
             this.visitICONST(v);
             break;
         }
         case 0x11: /* --- sipush --- */ {
             short v = getSignedWord();
             this.visitICONST(v);
             break;
         }
         case 0x12: /* --- ldc --- */ {
             char index = getUnsignedByte();
             byte tt = clazz.getCPtag(index);
             if (tt == CONSTANT_Integer)
                 this.visitICONST(clazz.getCPasInt(index).intValue());
             else if (tt == CONSTANT_Float)
                 this.visitFCONST(clazz.getCPasFloat(index).floatValue());
             else if (tt == CONSTANT_String || tt == CONSTANT_ResolvedClass)
                 this.visitACONST(clazz.getCPasObjectConstant(index));
             else
                 throw new VerifyError("bad ldc tag: "+tt);
             break;
         }
         case 0x13: /* --- ldc_w --- */ {
             char index = getUnsignedWord();
             byte tt = clazz.getCPtag(index);
             if (tt == CONSTANT_Integer)
                 this.visitICONST(clazz.getCPasInt(index).intValue());
             else if (tt == CONSTANT_Float)
                 this.visitFCONST(clazz.getCPasFloat(index).floatValue());
             else if (tt == CONSTANT_String || tt == CONSTANT_ResolvedClass)
                 this.visitACONST(clazz.getCPasObjectConstant(index));
             else
                 throw new VerifyError("bad ldc_w tag: "+tt);
             break;
         }
         case 0x14: /* --- ldc2_w --- */ {
             char index = getUnsignedWord();
             byte tt = clazz.getCPtag(index);
             if (tt == CONSTANT_Long)
                 this.visitLCONST(clazz.getCPasLong(index).longValue());
             else if (tt == CONSTANT_Double)
                 this.visitDCONST(clazz.getCPasDouble(index).doubleValue());
             else
                 throw new VerifyError();
             break;
         }
         case 0x15: /* --- iload --- */ {
             char index = getUnsignedByte();
             this.visitILOAD(index);
             break;
         }
         case 0x17: /* --- fload --- */ {
             char index = getUnsignedByte();
             this.visitFLOAD(index);
             break;
         }
         case 0x19: /* --- aload --- */ {
             char index = getUnsignedByte();
             this.visitALOAD(index);
             break;
         }
         case 0x16: /* --- lload --- */ {
             char index = getUnsignedByte();
             this.visitLLOAD(index);
             break;
         }
         case 0x18: /* --- dload --- */ {
             char index = getUnsignedByte();
             this.visitDLOAD(index);
             break;
         }
         case 0x1a: /* --- iload_0 --- */
         case 0x1b: /* --- iload_1 --- */
         case 0x1c: /* --- iload_2 --- */
         case 0x1d: /* --- iload_3 --- */ {
             int index = bc-0x1a;
             this.visitILOAD(index);
             break;
         }
         case 0x22: /* --- fload_0 --- */
         case 0x23: /* --- fload_1 --- */
         case 0x24: /* --- fload_2 --- */
         case 0x25: /* --- fload_3 --- */ {
             int index = bc-0x22;
             this.visitFLOAD(index);
             break;
         }
         case 0x2a: /* --- aload_0 --- */
         case 0x2b: /* --- aload_1 --- */
         case 0x2c: /* --- aload_2 --- */
         case 0x2d: /* --- aload_3 --- */ {
             int index = bc-0x2a;
             this.visitALOAD(index);
             break;
         }
         case 0x1e: /* --- lload_0 --- */
         case 0x1f: /* --- lload_1 --- */
         case 0x20: /* --- lload_2 --- */
         case 0x21: /* --- lload_3 --- */ {
             int index = bc-0x1e;
             this.visitLLOAD(index);
             break;
         }
         case 0x26: /* --- dload_0 --- */
         case 0x27: /* --- dload_1 --- */
         case 0x28: /* --- dload_2 --- */
         case 0x29: /* --- dload_3 --- */ {
             int index = bc-0x26;
             this.visitDLOAD(index);
             break;
         }
         case 0x2e: /* --- iaload --- */ {
             this.visitPEI();
             this.visitIALOAD();
             break;
         }
         case 0x30: /* --- faload --- */ {
             this.visitPEI();
             this.visitFALOAD();
             break;
         }
         case 0x32: /* --- aaload --- */ {
             this.visitPEI();
             this.visitAALOAD();
             break;
         }
         case 0x2f: /* --- laload --- */ {
             this.visitPEI();
             this.visitLALOAD();
             break;
         }
         case 0x31: /* --- daload --- */ {
             this.visitPEI();
             this.visitDALOAD();
             break;
         }
         case 0x33: /* --- baload --- */ {
             this.visitPEI();
             this.visitBALOAD();
             break;
         }
         case 0x34: /* --- caload --- */ {
             this.visitPEI();
             this.visitCALOAD();
             break;
         }
         case 0x35: /* --- saload --- */ {
             this.visitPEI();
             this.visitSALOAD();
             break;
         }
         case 0x36: /* --- istore --- */ {
             char index = getUnsignedByte();
             this.visitISTORE(index);
             break;
         }
         case 0x38: /* --- fstore --- */ {
             char index = getUnsignedByte();
             this.visitFSTORE(index);
             break;
         }
         case 0x3a: /* --- astore --- */ {
             char index = getUnsignedByte();
             this.visitASTORE(index);
             break;
         }
         case 0x37: /* --- lstore --- */ {
             char index = getUnsignedByte();
             this.visitLSTORE(index);
             break;
         }
         case 0x39: /* --- dstore --- */ {
             char index = getUnsignedByte();
             this.visitDSTORE(index);
             break;
         }
         case 0x3b: /* --- istore_0 --- */
         case 0x3c: /* --- istore_1 --- */
         case 0x3d: /* --- istore_2 --- */
         case 0x3e: /* --- istore_3 --- */ {
             int index = bc-0x3b;
             this.visitISTORE(index);
             break;
         }
         case 0x43: /* --- fstore_0 --- */
         case 0x44: /* --- fstore_1 --- */
         case 0x45: /* --- fstore_2 --- */
         case 0x46: /* --- fstore_3 --- */ {
             int index = bc-0x43;
             this.visitFSTORE(index);
             break;
         }
         case 0x4b: /* --- astore_0 --- */
         case 0x4c: /* --- astore_1 --- */
         case 0x4d: /* --- astore_2 --- */
         case 0x4e: /* --- astore_3 --- */ {
             int index = bc-0x4b;
             this.visitASTORE(index);
             break;
         }
         case 0x3f: /* --- lstore_0 --- */
         case 0x40: /* --- lstore_1 --- */
         case 0x41: /* --- lstore_2 --- */
         case 0x42: /* --- lstore_3 --- */ {
             int index = bc-0x3f;
             this.visitLSTORE(index);
             break;
         }
         case 0x47: /* --- dstore_0 --- */
         case 0x48: /* --- dstore_1 --- */
         case 0x49: /* --- dstore_2 --- */
         case 0x4a: /* --- dstore_3 --- */ {
             int index = bc-0x47;
             this.visitDSTORE(index);
             break;
         }
         case 0x4f: /* --- iastore --- */ {
             this.visitPEI();
             this.visitIASTORE();
             break;
         }
         case 0x51: /* --- fastore --- */ {
             this.visitPEI();
             this.visitFASTORE();
             break;
         }
         case 0x50: /* --- lastore --- */ {
             this.visitPEI();
             this.visitLASTORE();
             break;
         }
         case 0x52: /* --- dastore --- */ {
             this.visitPEI();
             this.visitDASTORE();
             break;
         }
         case 0x53: /* --- aastore --- */ {
             this.visitPEI();
             this.visitAASTORE();
             break;
         }
         case 0x54: /* --- bastore --- */ {
             this.visitPEI();
             this.visitBASTORE();
             break;
         }
         case 0x55: /* --- castore --- */ {
             this.visitPEI();
             this.visitCASTORE();
             break;
         }
         case 0x56: /* --- sastore --- */ {
             this.visitPEI();
             this.visitSASTORE();
             break;
         }
         case 0x57: /* --- pop --- */ {
             this.visitPOP();
             break;
         }
         case 0x58: /* --- pop2 --- */ {
             this.visitPOP2();
             break;
         }
         case 0x59: /* --- dup --- */ {
             this.visitDUP();
             break;
         }
         case 0x5a: /* --- dup_x1 --- */ {
             this.visitDUP_x1();
             break;
         }
         case 0x5b: /* --- dup_x2 --- */ {
             this.visitDUP_x2();
             break;
         }
         case 0x5c: /* --- dup2 --- */ {
             this.visitDUP2();
             break;
         }
         case 0x5d: /* --- dup2_x1 --- */ {
             this.visitDUP2_x1();
             break;
         }
         case 0x5e: /* --- dup2_x2 --- */ {
             this.visitDUP2_x2();
             break;
         }
         case 0x5f: /* --- swap --- */ {
             this.visitSWAP();
             break;
         }
         case 0x60: /* --- iadd --- */ {
             this.visitIBINOP(BINOP_ADD);
             break;
         }
         case 0x61: /* --- ladd --- */ {
             this.visitLBINOP(BINOP_ADD);
             break;
         }
         case 0x62: /* --- fadd --- */ {
             this.visitFBINOP(BINOP_ADD);
             break;
         }
         case 0x63: /* --- dadd --- */ {
             this.visitDBINOP(BINOP_ADD);
             break;
         }
         case 0x64: /* --- isub --- */ {
             this.visitIBINOP(BINOP_SUB);
             break;
         }
         case 0x65: /* --- lsub --- */ {
             this.visitLBINOP(BINOP_SUB);
             break;
         }
         case 0x66: /* --- fsub --- */ {
             this.visitFBINOP(BINOP_SUB);
             break;
         }
         case 0x67: /* --- dsub ---; */ {
             this.visitDBINOP(BINOP_SUB);
             break;
         }
         case 0x68: /* --- imul --- */ {
             this.visitIBINOP(BINOP_MUL);
             break;
         }
         case 0x69: /* --- lmul --- */ {
             this.visitLBINOP(BINOP_MUL);
             break;
         }
         case 0x6a: /* --- fmul --- */ {
             this.visitFBINOP(BINOP_MUL);
             break;
         }
         case 0x6b: /* --- dmul --- */ {
             this.visitDBINOP(BINOP_MUL);
             break;
         }
         case 0x6c: /* --- idiv --- */ {
             this.visitPEI();
             this.visitIBINOP(BINOP_DIV);
             break;
         }
         case 0x6d: /* --- ldiv --- */ {
             this.visitPEI();
             this.visitLBINOP(BINOP_DIV);
             break;
         }
         case 0x6e: /* --- fdiv --- */ {
             this.visitFBINOP(BINOP_DIV);
             break;
         }
         case 0x6f: /* --- ddiv --- */ {
             this.visitDBINOP(BINOP_DIV);
             break;
         }
         case 0x70: /* --- irem --- */ {
             this.visitPEI();
             this.visitIBINOP(BINOP_REM);
             break;
         }
         case 0x71: /* --- lrem --- */ {
             this.visitPEI();
             this.visitLBINOP(BINOP_REM);
             break;
         }
         case 0x72: /* --- frem --- */ {
             this.visitFBINOP(BINOP_REM);
             break;
         }
         case 0x73: /* --- drem --- */ {
             this.visitDBINOP(BINOP_REM);
             break;
         }
         case 0x74: /* --- ineg --- */ {
             this.visitIUNOP(UNOP_NEG);
             break;
         }
         case 0x75: /* --- lneg --- */ {
             this.visitLUNOP(UNOP_NEG);
             break;
         }
         case 0x76: /* --- fneg --- */ {
             this.visitFUNOP(UNOP_NEG);
             break;
         }
         case 0x77: /* --- dneg --- */ {
             this.visitDUNOP(UNOP_NEG);
             break;
         }
         case 0x78: /* --- ishl --- */ {
             this.visitISHIFT(SHIFT_LEFT);
             break;
         }
         case 0x79: /* --- lshl --- */ {
             this.visitLSHIFT(SHIFT_LEFT);
             break;
         }
         case 0x7a: /* --- ishr --- */ {
             this.visitISHIFT(SHIFT_RIGHT);
             break;
         }
         case 0x7b: /* --- lshr --- */ {
             this.visitLSHIFT(SHIFT_RIGHT);
             break;
         }
         case 0x7c: /* --- iushr --- */ {
             this.visitISHIFT(SHIFT_URIGHT);
             break;
         }
         case 0x7d: /* --- lushr --- */ {
             this.visitLSHIFT(SHIFT_URIGHT);
             break;
         }
         case 0x7e: /* --- iand --- */ {
             this.visitIBINOP(BINOP_AND);
             break;
         }
         case 0x7f: /* --- land --- */ {
             this.visitLBINOP(BINOP_AND);
             break;
         }
         case 0x80: /* --- ior --- */ {
             this.visitIBINOP(BINOP_OR);
             break;
         }
         case 0x81: /* --- lor --- */ {
             this.visitLBINOP(BINOP_OR);
             break;
         }
         case 0x82: /* --- ixor --- */ {
             this.visitIBINOP(BINOP_XOR);
             break;
         }
         case 0x83: /* --- lxor --- */ {
             this.visitLBINOP(BINOP_XOR);
             break;
         }
         case 0x84: /* --- iinc --- */ {
             char index = getUnsignedByte();
             byte v = getSignedByte();
             this.visitIINC(index, v);
             break;
         }
         case 0x85: /* --- i2l --- */ {
             this.visitI2L();
             break;
         }
         case 0x86: /* --- i2f --- */ {
             this.visitI2F();
             break;
         }
         case 0x87: /* --- i2d --- */ {
             this.visitI2D();
             break;
         }
         case 0x88: /* --- l2i --- */ {
             this.visitL2I();
             break;
         }
         case 0x89: /* --- l2f --- */ {
             this.visitL2F();
             break;
         }
         case 0x8a: /* --- l2d --- */ {
             this.visitL2D();
             break;
         }
         case 0x8b: /* --- f2i --- */ {
             this.visitF2I();
             break;
         }
         case 0x8c: /* --- f2l --- */ {
             this.visitF2L();
             break;
         }
         case 0x8d: /* --- f2d --- */ {
             this.visitF2D();
             break;
         }
         case 0x8e: /* --- d2i --- */ {
             this.visitD2I();
             break;
         }
         case 0x8f: /* --- d2l --- */ {
             this.visitD2L();
             break;
         }
         case 0x90: /* --- d2f --- */ {
             this.visitD2F();
             break;
         }
         case 0x91: /* --- i2b --- */ {
             this.visitI2B();
             break;
         }
         case 0x92: /* --- i2c --- */ {
             this.visitI2C();
             break;
         }
         case 0x93: /* --- i2s --- */ {
             this.visitI2S();
             break;
         }
         case 0x94: /* --- lcmp --- */ {
             this.visitLCMP2();
             break;
         }
         case 0x95: /* --- fcmpl --- */ {
             this.visitFCMP2(CMP_L);
             break;
         }
         case 0x96: /* --- fcmpg --- */ {
             this.visitFCMP2(CMP_G);
             break;
         }
         case 0x97: /* --- dcmpl --- */ {
             this.visitDCMP2(CMP_L);
             break;
         }
         case 0x98: /* --- dcmpg --- */ {
             this.visitDCMP2(CMP_G);
             break;
         }
         case 0x99: /* --- ifeq --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIF(CMP_EQ, target);
             break;
         }
         case 0xc6: /* --- ifnull --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFREF(CMP_EQ, target);
             break;
         }
         case 0x9a: /* --- ifne --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIF(CMP_NE, target);
             break;
         }
         case 0xc7: /* --- ifnonnull --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFREF(CMP_NE, target);
             break;
         }
         case 0x9b: /* --- iflt --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIF(CMP_LT, target);
             break;
         }
         case 0x9c: /* --- ifge --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIF(CMP_GE, target);
             break;
         }
         case 0x9d: /* --- ifgt --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIF(CMP_GT, target);
             break;
         }
         case 0x9e: /* --- ifle --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIF(CMP_LE, target);
             break;
         }
         case 0x9f: /* --- if_icmpeq --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFCMP(CMP_EQ, target);
             break;
         }
         case 0xa5: /* --- if_acmpeq --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFREFCMP(CMP_EQ, target);
             break;
         }
         case 0xa0: /* --- if_icmpne --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFCMP(CMP_NE, target);
             break;
         }
         case 0xa6: /* --- if_acmpne --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFREFCMP(CMP_NE, target);
             break;
         }
         case 0xa1: /* --- if_icmplt --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFCMP(CMP_LT, target);
             break;
         }
         case 0xa2: /* --- if_icmpge --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFCMP(CMP_GE, target);
             break;
         }
         case 0xa3: /* --- if_icmpgt --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFCMP(CMP_GT, target);
             break;
         }
         case 0xa4: /* --- if_icmple --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitIFCMP(CMP_LE, target);
             break;
         }
         case 0xa7: /* --- goto --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitGOTO(target);
             break;
         }
         case 0xa8: /* --- jsr --- */ {
             int offset = getSignedWord();
             int target = i_start + offset;
             this.visitJSR(target);
             break;
         }
         case 0xa9: /* --- ret --- */ {
             char index = getUnsignedByte();
             this.visitRET(index);
             break;
         }
         case 0xaa: /* --- tableswitch --- */ {
             int align = (i_end+1) & 3;
             if (align != 0) i_end += 4-align; // padding
             int offset = getSignedDWord();
             int target = i_start + offset;
             int low = getSignedDWord();
             int high = getSignedDWord();
             int count = high-low+1;
             int[] targets = new int[count];
             for (int i=0; i<count; ++i) {
                 offset = getSignedDWord();
                 targets[i] = i_start + offset;
             }
             this.visitTABLESWITCH(target, low, high, targets);
             break;
         }
         case 0xab: /* --- lookupswitch --- */ {
             int align = (i_end+1) & 3;
             if (align != 0) i_end += 4-align; // padding
             int doffset = getSignedDWord();
             int dtarget = i_start + doffset;
             int npairs = getSignedDWord();
             int[] values = new int[npairs];
             int[] targets = new int[npairs];
             for (int i=0; i<npairs; ++i) {
                 values[i] = getSignedDWord();
                 int offset = getSignedDWord();
                 targets[i] = i_start + offset;
             }
             this.visitLOOKUPSWITCH(dtarget, values, targets);
             break;
         }
         case 0xac: /* --- ireturn --- */ {
             this.visitIRETURN();
             break;
         }
         case 0xae: /* --- freturn --- */ {
             this.visitFRETURN();
             break;
         }
         case 0xb0: /* --- areturn --- */ {
             this.visitARETURN();
             break;
         }
         case 0xad: /* --- lreturn --- */ {
             this.visitLRETURN();
             break;
         }
         case 0xaf: /* --- dreturn --- */ {
             this.visitDRETURN();
             break;
         }
         case 0xb1: /* --- return --- */ {
             this.visitVRETURN();
             break;
         }
         case 0xb2: /* --- getstatic --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_StaticField f = clazz.getCPasStaticField(cpi);
             jq_Type t = f.getType();
             if (t.isReferenceType())
                 this.visitAGETSTATIC(f);
             else if (t == jq_Primitive.INT)
                 this.visitIGETSTATIC(f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFGETSTATIC(f);
             else if (t == jq_Primitive.LONG)
                 this.visitLGETSTATIC(f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDGETSTATIC(f);
             else if (t == jq_Primitive.BOOLEAN)
                 this.visitZGETSTATIC(f);
             else if (t == jq_Primitive.BYTE)
                 this.visitBGETSTATIC(f);
             else if (t == jq_Primitive.CHAR)
                 this.visitCGETSTATIC(f);
             else if (t == jq_Primitive.SHORT)
                 this.visitSGETSTATIC(f);
             else
                 Assert.UNREACHABLE();
             break;
         }
         case 0xb3: /* --- putstatic --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_StaticField f = clazz.getCPasStaticField(cpi);
             jq_Type t = f.getType();
             if (t.isReferenceType())
                 this.visitAPUTSTATIC(f);
             else if (t == jq_Primitive.INT)
                 this.visitIPUTSTATIC(f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFPUTSTATIC(f);
             else if (t == jq_Primitive.LONG)
                 this.visitLPUTSTATIC(f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDPUTSTATIC(f);
             else if (t == jq_Primitive.BOOLEAN)
                 this.visitZPUTSTATIC(f);
             else if (t == jq_Primitive.BYTE)
                 this.visitBPUTSTATIC(f);
             else if (t == jq_Primitive.CHAR)
                 this.visitCPUTSTATIC(f);
             else if (t == jq_Primitive.SHORT)
                 this.visitSPUTSTATIC(f);
             else
                 Assert.UNREACHABLE();
             break;
         }
         case 0xb4: /* --- getfield --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_InstanceField f = clazz.getCPasInstanceField(cpi);
             jq_Type t = f.getType();
             if (t.isReferenceType())
                 this.visitAGETFIELD(f);
             else if (t == jq_Primitive.INT)
                 this.visitIGETFIELD(f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFGETFIELD(f);
             else if (t == jq_Primitive.LONG)
                 this.visitLGETFIELD(f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDGETFIELD(f);
             else if (t == jq_Primitive.BYTE)
                 this.visitBGETFIELD(f);
             else if (t == jq_Primitive.CHAR)
                 this.visitCGETFIELD(f);
             else if (t == jq_Primitive.SHORT)
                 this.visitSGETFIELD(f);
             else if (t == jq_Primitive.BOOLEAN)
                 this.visitZGETFIELD(f);
             else
                 Assert.UNREACHABLE();
             break;
         }
         case 0xb5: /* --- putfield --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_InstanceField f = clazz.getCPasInstanceField(cpi);
             jq_Type t = f.getType();
             if (t.isReferenceType())
                 this.visitAPUTFIELD(f);
             else if (t == jq_Primitive.INT)
                 this.visitIPUTFIELD(f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFPUTFIELD(f);
             else if (t == jq_Primitive.LONG)
                 this.visitLPUTFIELD(f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDPUTFIELD(f);
             else if (t == jq_Primitive.BYTE)
                 this.visitBPUTFIELD(f);
             else if (t == jq_Primitive.CHAR)
                 this.visitCPUTFIELD(f);
             else if (t == jq_Primitive.SHORT)
                 this.visitSPUTFIELD(f);
             else if (t == jq_Primitive.BOOLEAN)
                 this.visitZPUTFIELD(f);
             else
                 Assert.UNREACHABLE();
             break;
         }
         case 0xb6: /* --- invokevirtual --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_InstanceMethod f = clazz.getCPasInstanceMethod(cpi);
             jq_Type t = f.getReturnType();
             if (t == jq_Primitive.VOID)
                 this.visitVINVOKE(INVOKE_VIRTUAL, f);
             else if (t.isReferenceType())
                 this.visitAINVOKE(INVOKE_VIRTUAL, f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFINVOKE(INVOKE_VIRTUAL, f);
             else if (t == jq_Primitive.LONG)
                 this.visitLINVOKE(INVOKE_VIRTUAL, f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDINVOKE(INVOKE_VIRTUAL, f);
             else
                 this.visitIINVOKE(INVOKE_VIRTUAL, f);
             break;
         }
         case 0xb7: /* --- invokespecial --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_InstanceMethod f = clazz.getCPasInstanceMethod(cpi);
             jq_Type t = f.getReturnType();
             if (t == jq_Primitive.VOID)
                 this.visitVINVOKE(INVOKE_SPECIAL, f);
             else if (t.isReferenceType())
                 this.visitAINVOKE(INVOKE_SPECIAL, f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFINVOKE(INVOKE_SPECIAL, f);
             else if (t == jq_Primitive.LONG)
                 this.visitLINVOKE(INVOKE_SPECIAL, f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDINVOKE(INVOKE_SPECIAL, f);
             else
                 this.visitIINVOKE(INVOKE_SPECIAL, f);
             break;
         }
         case 0xb8: /* --- invokestatic --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_StaticMethod f = clazz.getCPasStaticMethod(cpi);
             jq_Type t = f.getReturnType();
             if (t == jq_Primitive.VOID)
                 this.visitVINVOKE(INVOKE_STATIC, f);
             else if (t.isReferenceType())
                 this.visitAINVOKE(INVOKE_STATIC, f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFINVOKE(INVOKE_STATIC, f);
             else if (t == jq_Primitive.LONG)
                 this.visitLINVOKE(INVOKE_STATIC, f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDINVOKE(INVOKE_STATIC, f);
             else
                 this.visitIINVOKE(INVOKE_STATIC, f);
             break;
         }
         case 0xb9: /* --- invokeinterface --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_InstanceMethod f = clazz.getCPasInstanceMethod(cpi);
             getUnsignedByte(); // nargs
             getSignedByte(); // superfluous 0
             jq_Type t = f.getReturnType();
             if (t == jq_Primitive.VOID)
                 this.visitVINVOKE(INVOKE_INTERFACE, f);
             else if (t.isReferenceType())
                 this.visitAINVOKE(INVOKE_INTERFACE, f);
             else if (t == jq_Primitive.FLOAT)
                 this.visitFINVOKE(INVOKE_INTERFACE, f);
             else if (t == jq_Primitive.LONG)
                 this.visitLINVOKE(INVOKE_INTERFACE, f);
             else if (t == jq_Primitive.DOUBLE)
                 this.visitDINVOKE(INVOKE_INTERFACE, f);
             else
                 this.visitIINVOKE(INVOKE_INTERFACE, f);
             break;
         }
         case 0xba: /* --- unused --- */ {
             throw new VerifyError();
         }
         case 0xbb: /* --- new --- */ {
             char cpi = getUnsignedWord();
             jq_Type f = clazz.getCPasType(cpi);
             this.visitNEW(f);
             break;
         }
         case 0xbc: /* --- newarray --- */ {
             this.visitPEI();
             byte atype = getSignedByte();
             jq_Array array = jq_Array.getPrimitiveArrayType(atype);
             array.load(); array.prepare();
             this.visitNEWARRAY(array);
             break;
         }
         case 0xbd: /* --- anewarray --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_Type element = clazz.getCPasType(cpi);
             jq_Array array = element.getArrayTypeForElementType();
             array.load(); array.prepare();
             this.visitNEWARRAY(array);
             break;
         }
         case 0xbe: /* --- arraylength --- */ {
             this.visitPEI();
             this.visitARRAYLENGTH();
             break;
         }
         case 0xbf: /* --- athrow --- */ {
             this.visitPEI();
             this.visitATHROW();
             break;
         }
         case 0xc0: /* --- checkcast --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_Type f = clazz.getCPasType(cpi);
             this.visitCHECKCAST(f);
             break;
         }
         case 0xc1: /* --- instanceof --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             jq_Type f = clazz.getCPasType(cpi);
             this.visitINSTANCEOF(f);
             break;
         }
         case 0xc2: /* --- monitorenter ---  */ {
             this.visitPEI();
             this.visitMONITOR(MONITOR_ENTER);
             break;
         }
         case 0xc3: /* --- monitorexit --- */ {
             this.visitPEI();
             this.visitMONITOR(MONITOR_EXIT);
             break;
         }
         case 0xc4: /* --- wide --- */ {
             char widecode = getUnsignedByte();
             char index = getUnsignedWord();
             switch (widecode) {
                 case 0x15: /* --- wide iload --- */ {
                     this.visitILOAD(index);
                     break;
                 }
                 case 0x17: /* --- wide fload --- */ {
                     this.visitFLOAD(index);
                     break;
                 }
                 case 0x19: /* --- wide aload --- */ {
                     this.visitALOAD(index);
                     break;
                 }
                 case 0x16: /* --- wide lload --- */ {
                     this.visitLLOAD(index);
                     break;
                 }
                 case 0x18: /* --- wide dload --- */ {
                     this.visitDLOAD(index);
                     break;
                 }
                 case 0x36: /* --- wide istore --- */ {
                     this.visitISTORE(index);
                     break;
                 }
                 case 0x38: /* --- wide fstore --- */ {
                     this.visitFSTORE(index);
                     break;
                 }
                 case 0x3a: /* --- wide astore --- */ {
                     this.visitASTORE(index);
                     break;
                 }
                 case 0x37: /* --- wide lstore --- */ {
                     this.visitLSTORE(index);
                     break;
                 }
                 case 0x39: /* --- wide dstore --- */ {
                     this.visitDSTORE(index);
                     break;
                 }
                 case 0x84: /* --- wide iinc --- */ {
                     short v = getSignedWord();
                     this.visitIINC(index, v);
                     break;
                 }
                 case 0x9a: /* --- wide ret --- */ {
                     this.visitRET(index);
                     break;
                 }
                 default:
                     throw new VerifyError();
             }
             break;
         }
         case 0xc5: /* --- multianewarray --- */ {
             this.visitPEI();
             char cpi = getUnsignedWord();
             char dim = getUnsignedByte();
             jq_Type array = clazz.getCPasType(cpi);
             array.load(); array.prepare();
             this.visitMULTINEWARRAY(array, dim);
             break;
         }
         case 0xc8: /* --- goto_w --- */ {
             int offset = getSignedDWord();
             int target = i_start + offset;
             this.visitGOTO(target);
             break;
         }
         case 0xc9: /* --- jsr_w --- */ {
             int offset = getSignedDWord();
             int target = i_start + offset;
             this.visitJSR(target);
             break;
         }
         default:
             throw new VerifyError();
     }
 }

 public static final byte BINOP_ADD = 0;
 public static final byte BINOP_SUB = 1;
 public static final byte BINOP_MUL = 2;
 public static final byte BINOP_DIV = 3;
 public static final byte BINOP_REM = 4;
 public static final byte BINOP_AND = 5;
 public static final byte BINOP_OR = 6;
 public static final byte BINOP_XOR = 7;
 public static final String binopnames[] = {"ADD","SUB","MUL","DIV","REM","AND","OR","XOR"};

 public static final byte UNOP_NEG = 0;
 public static final String unopnames[] = {"NEG"};

 public static final byte SHIFT_LEFT = 0;
 public static final byte SHIFT_RIGHT = 1;
 public static final byte SHIFT_URIGHT = 2;
 public static final String shiftopnames[] = {"LEFT", "RIGHT", "URIGHT"};
 
 public static final byte CMP_L = 0;
 public static final byte CMP_G = 1;
 public static final String fcmpopnames[] = {"L", "G"};
 
 public static final byte CMP_EQ = 0;
 public static final byte CMP_NE = 1;
 public static final byte CMP_LT = 2;
 public static final byte CMP_GE = 3;
 public static final byte CMP_LE = 4;
 public static final byte CMP_GT = 5;
 public static final byte CMP_AE = 6;
 public static final byte CMP_UNCOND = 7;
 public static final String cmpopnames[] = {"EQ", "NE", "LT", "GE", "LE", "GT", "AE", "UNCOND"};
 
 public static final byte INVOKE_VIRTUAL = 0;
 public static final byte INVOKE_STATIC = 1;
 public static final byte INVOKE_SPECIAL = 2;
 public static final byte INVOKE_INTERFACE = 3;
 public static final String invokeopnames[] = {"VIRTUAL", "STATIC", "SPECIAL", "INTERFACE"};
 
 public static final byte MONITOR_ENTER = 0;
 public static final byte MONITOR_EXIT = 1;
 public static final String monitoropnames[] = {"ENTER", "EXIT"};
 
 // visitor methods
 public void visitPEI() {
     if (TRACE) out.println(this+": "+i_start+" is a PEI");
 }

 public void visitNOP() {
     if (TRACE) out.println(this+": "+i_start+" NOP");
 }
 public void visitACONST(Object s) {
     if (TRACE) out.println(this+": "+i_start+" ACONST \""+s+"\"");
 }
 public void visitICONST(int c) {
     if (TRACE) out.println(this+": "+i_start+" ICONST "+c);
 }
 public void visitLCONST(long c) {
     if (TRACE) out.println(this+": "+i_start+" LCONST "+c);
 }
 public void visitFCONST(float c) {
     if (TRACE) out.println(this+": "+i_start+" FCONST "+c);
 }
 public void visitDCONST(double c) {
     if (TRACE) out.println(this+": "+i_start+" DCONST "+c);
 }
 public void visitILOAD(int i) {
     if (TRACE) out.println(this+": "+i_start+" ILOAD "+i);
 }
 public void visitLLOAD(int i) {
     if (TRACE) out.println(this+": "+i_start+" LLOAD "+i);
 }
 public void visitFLOAD(int i) {
     if (TRACE) out.println(this+": "+i_start+" FLOAD "+i);
 }
 public void visitDLOAD(int i) {
     if (TRACE) out.println(this+": "+i_start+" DLOAD "+i);
 }
 public void visitALOAD(int i) {
     if (TRACE) out.println(this+": "+i_start+" ALOAD "+i);
 }
 public void visitISTORE(int i) {
     if (TRACE) out.println(this+": "+i_start+" ISTORE "+i);
 }
 public void visitLSTORE(int i) {
     if (TRACE) out.println(this+": "+i_start+" LSTORE "+i);
 }
 public void visitFSTORE(int i) {
     if (TRACE) out.println(this+": "+i_start+" FSTORE "+i);
 }
 public void visitDSTORE(int i) {
     if (TRACE) out.println(this+": "+i_start+" DSTORE "+i);
 }
 public void visitASTORE(int i) {
     if (TRACE) out.println(this+": "+i_start+" ASTORE "+i);
 }
 public void visitIALOAD() {
     if (TRACE) out.println(this+": "+i_start+" IALOAD");
 }
 public void visitLALOAD() {
     if (TRACE) out.println(this+": "+i_start+" LALOAD");
 }
 public void visitFALOAD() {
     if (TRACE) out.println(this+": "+i_start+" FALOAD");
 }
 public void visitDALOAD() {
     if (TRACE) out.println(this+": "+i_start+" DALOAD");
 }
 public void visitAALOAD() {
     if (TRACE) out.println(this+": "+i_start+" AALOAD");
 }
 public void visitBALOAD() {
     if (TRACE) out.println(this+": "+i_start+" BALOAD");
 }
 public void visitCALOAD() {
     if (TRACE) out.println(this+": "+i_start+" CALOAD");
 }
 public void visitSALOAD() {
     if (TRACE) out.println(this+": "+i_start+" SALOAD");
 }
 public void visitIASTORE() {
     if (TRACE) out.println(this+": "+i_start+" IASTORE");
 }
 public void visitLASTORE() {
     if (TRACE) out.println(this+": "+i_start+" LASTORE");
 }
 public void visitFASTORE() {
     if (TRACE) out.println(this+": "+i_start+" FASTORE");
 }
 public void visitDASTORE() {
     if (TRACE) out.println(this+": "+i_start+" DASTORE");
 }
 public void visitAASTORE() {
     if (TRACE) out.println(this+": "+i_start+" AASTORE");
 }
 public void visitBASTORE() {
     if (TRACE) out.println(this+": "+i_start+" BASTORE");
 }
 public void visitCASTORE() {
     if (TRACE) out.println(this+": "+i_start+" CASTORE");
 }
 public void visitSASTORE() {
     if (TRACE) out.println(this+": "+i_start+" SASTORE");
 }
 public void visitPOP() {
     if (TRACE) out.println(this+": "+i_start+" POP");
 }
 public void visitPOP2() {
     if (TRACE) out.println(this+": "+i_start+" POP2");
 }
 public void visitDUP() {
     if (TRACE) out.println(this+": "+i_start+" DUP");
 }
 public void visitDUP_x1() {
     if (TRACE) out.println(this+": "+i_start+" DUP_x1");
 }
 public void visitDUP_x2() {
     if (TRACE) out.println(this+": "+i_start+" DUP_x2");
 }
 public void visitDUP2() {
     if (TRACE) out.println(this+": "+i_start+" DUP2");
 }
 public void visitDUP2_x1() {
     if (TRACE) out.println(this+": "+i_start+" DUP2_x1");
 }
 public void visitDUP2_x2() {
     if (TRACE) out.println(this+": "+i_start+" DUP2_x2");
 }
 public void visitSWAP() {
     if (TRACE) out.println(this+": "+i_start+" SWAP");
 }
 public void visitIBINOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" I"+binopnames[op]);
 }
 public void visitLBINOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" L"+binopnames[op]);
 }
 public void visitFBINOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" F"+binopnames[op]);
 }
 public void visitDBINOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" D"+binopnames[op]);
 }
 public void visitIUNOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" I"+binopnames[op]);
 }
 public void visitLUNOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" L"+binopnames[op]);
 }
 public void visitFUNOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" F"+binopnames[op]);
 }
 public void visitDUNOP(byte op) {
     if (TRACE) out.println(this+": "+i_start+" D"+binopnames[op]);
 }
 public void visitISHIFT(byte op) {
     if (TRACE) out.println(this+": "+i_start+" ISHIFT "+shiftopnames[op]);
 }
 public void visitLSHIFT(byte op) {
     if (TRACE) out.println(this+": "+i_start+" LSHIFT "+shiftopnames[op]);
 }
 public void visitIINC(int i, int v) {
     if (TRACE) out.println(this+": "+i_start+" IINC "+i+" "+v);
 }
 public void visitI2L() {
     if (TRACE) out.println(this+": "+i_start+" I2L");
 }
 public void visitI2F() {
     if (TRACE) out.println(this+": "+i_start+" I2F");
 }
 public void visitI2D() {
     if (TRACE) out.println(this+": "+i_start+" I2D");
 }
 public void visitL2I() {
     if (TRACE) out.println(this+": "+i_start+" L2I");
 }
 public void visitL2F() {
     if (TRACE) out.println(this+": "+i_start+" L2F");
 }
 public void visitL2D() {
     if (TRACE) out.println(this+": "+i_start+" L2D");
 }
 public void visitF2I() {
     if (TRACE) out.println(this+": "+i_start+" F2I");
 }
 public void visitF2L() {
     if (TRACE) out.println(this+": "+i_start+" F2L");
 }
 public void visitF2D() {
     if (TRACE) out.println(this+": "+i_start+" F2D");
 }
 public void visitD2I() {
     if (TRACE) out.println(this+": "+i_start+" D2I");
 }
 public void visitD2L() {
     if (TRACE) out.println(this+": "+i_start+" D2L");
 }
 public void visitD2F() {
     if (TRACE) out.println(this+": "+i_start+" D2F");
 }
 public void visitI2B() {
     if (TRACE) out.println(this+": "+i_start+" I2B");
 }
 public void visitI2C() {
     if (TRACE) out.println(this+": "+i_start+" I2C");
 }
 public void visitI2S() {
     if (TRACE) out.println(this+": "+i_start+" I2S");
 }
 public void visitLCMP2() {
     if (TRACE) out.println(this+": "+i_start+" LCMP2");
 }
 public void visitFCMP2(byte op) {
     if (TRACE) out.println(this+": "+i_start+" FCMP2 "+fcmpopnames[op]);
 }
 public void visitDCMP2(byte op) {
     if (TRACE) out.println(this+": "+i_start+" DCMP2 "+fcmpopnames[op]);
 }
 public void visitIF(byte op, int target) {
     if (TRACE) out.println(this+": "+i_start+" IF"+cmpopnames[op]+" "+target);
 }
 public void visitIFREF(byte op, int target) {
     if (TRACE) out.println(this+": "+i_start+" IFREF"+cmpopnames[op]+" "+target);
 }
 public void visitIFCMP(byte op, int target) {
     if (TRACE) out.println(this+": "+i_start+" IFCMP"+cmpopnames[op]+" "+target);
 }
 public void visitIFREFCMP(byte op, int target) {
     if (TRACE) out.println(this+": "+i_start+" IFREFCMP"+cmpopnames[op]+" "+target);
 }
 public void visitGOTO(int target) {
     if (TRACE) out.println(this+": "+i_start+" GOTO "+target);
 }
 public void visitJSR(int target) {
     if (TRACE) out.println(this+": "+i_start+" JSR "+target);
 }
 public void visitRET(int i) {
     if (TRACE) out.println(this+": "+i_start+" RET "+i);
 }
 public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
     if (TRACE) out.println(this+": "+i_start+" TABLESWITCH("+low+".."+high+",def:"+default_target+")");
 }
 public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
     Assert._assert(values.length == targets.length);
     if (TRACE) out.println(this+": "+i_start+" LOOKUPSWITCH("+values.length+" entries,def:"+default_target+")");
 }
 public void visitIRETURN() {
     if (TRACE) out.println(this+": "+i_start+" IRETURN");
 }
 public void visitLRETURN() {
     if (TRACE) out.println(this+": "+i_start+" LRETURN");
 }
 public void visitFRETURN() {
     if (TRACE) out.println(this+": "+i_start+" FRETURN");
 }
 public void visitDRETURN() {
     if (TRACE) out.println(this+": "+i_start+" DRETURN");
 }
 public void visitARETURN() {
     if (TRACE) out.println(this+": "+i_start+" ARETURN");
 }
 public void visitVRETURN() {
     if (TRACE) out.println(this+": "+i_start+" VRETURN");
 }
 public void visitIGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" IGETSTATIC "+f);
 }
 public void visitLGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" LGETSTATIC "+f);
 }
 public void visitFGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" FGETSTATIC "+f);
 }
 public void visitDGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" DGETSTATIC "+f);
 }
 public void visitAGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" AGETSTATIC "+f);
 }
 public void visitZGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" ZGETSTATIC "+f);
 }
 public void visitBGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" BGETSTATIC "+f);
 }
 public void visitCGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" CGETSTATIC "+f);
 }
 public void visitSGETSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" SGETSTATIC "+f);
 }
 public void visitIPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" IPUTSTATIC "+f);
 }
 public void visitLPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" LPUTSTATIC "+f);
 }
 public void visitFPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" FPUTSTATIC "+f);
 }
 public void visitDPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" DPUTSTATIC "+f);
 }
 public void visitAPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" APUTSTATIC "+f);
 }
 public void visitZPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" ZPUTSTATIC "+f);
 }
 public void visitBPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" BPUTSTATIC "+f);
 }
 public void visitCPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" CPUTSTATIC "+f);
 }
 public void visitSPUTSTATIC(jq_StaticField f) {
     if (TRACE) out.println(this+": "+i_start+" SPUTSTATIC "+f);
 }
 public void visitIGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" IGETFIELD "+f);
 }
 public void visitLGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" LGETFIELD "+f);
 }
 public void visitFGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" FGETFIELD "+f);
 }
 public void visitDGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" DGETFIELD "+f);
 }
 public void visitAGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" AGETFIELD "+f);
 }
 public void visitBGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" BGETFIELD "+f);
 }
 public void visitCGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" CGETFIELD "+f);
 }
 public void visitSGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" SGETFIELD "+f);
 }
 public void visitZGETFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" ZGETFIELD "+f);
 }
 public void visitIPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" IPUTFIELD "+f);
 }
 public void visitLPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" LPUTFIELD "+f);
 }
 public void visitFPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" FPUTFIELD "+f);
 }
 public void visitDPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" DPUTFIELD "+f);
 }
 public void visitAPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" APUTFIELD "+f);
 }
 public void visitBPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" BPUTFIELD "+f);
 }
 public void visitCPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" CPUTFIELD "+f);
 }
 public void visitSPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" SPUTFIELD "+f);
 }
 public void visitZPUTFIELD(jq_InstanceField f) {
     if (TRACE) out.println(this+": "+i_start+" ZPUTFIELD "+f);
 }
 public void visitIINVOKE(byte op, jq_Method f) {
     if (TRACE) out.println(this+": "+i_start+" IINVOKE"+invokeopnames[op]+" "+f);
 }
 public void visitLINVOKE(byte op, jq_Method f) {
     if (TRACE) out.println(this+": "+i_start+" LINVOKE"+invokeopnames[op]+" "+f);
 }
 public void visitFINVOKE(byte op, jq_Method f) {
     if (TRACE) out.println(this+": "+i_start+" FINVOKE"+invokeopnames[op]+" "+f);
 }
 public void visitDINVOKE(byte op, jq_Method f) {
     if (TRACE) out.println(this+": "+i_start+" DINVOKE"+invokeopnames[op]+" "+f);
 }
 public void visitAINVOKE(byte op, jq_Method f) {
     if (TRACE) out.println(this+": "+i_start+" AINVOKE"+invokeopnames[op]+" "+f);
 }
 public void visitVINVOKE(byte op, jq_Method f) {
     if (TRACE) out.println(this+": "+i_start+" VINVOKE"+invokeopnames[op]+" "+f);
 }
 public void visitNEW(jq_Type f) {
     if (TRACE) out.println(this+": "+i_start+" NEW "+f);
 }
 public void visitNEWARRAY(jq_Array f) {
     if (TRACE) out.println(this+": "+i_start+" NEWARRAY "+f);
 }
 public void visitCHECKCAST(jq_Type f) {
     if (TRACE) out.println(this+": "+i_start+" CHECKCAST "+f);
 }
 public void visitINSTANCEOF(jq_Type f) {
     if (TRACE) out.println(this+": "+i_start+" INSTANCEOF "+f);
 }
 public void visitARRAYLENGTH() {
     if (TRACE) out.println(this+": "+i_start+" ARRAYLENGTH");
 }
 public void visitATHROW() {
     if (TRACE) out.println(this+": "+i_start+" ATHROW");
 }
 public void visitMONITOR(byte op) {
     if (TRACE) out.println(this+": "+i_start+" MONITOR "+monitoropnames[op]);
 }
 public void visitMULTINEWARRAY(jq_Type f, char dim) {
     if (TRACE) out.println(this+": "+i_start+" MULTINEWARRAY "+f+" (dim:"+(int)dim+")");
 }
 
 // utility methods
 private final byte getSignedByte() { return bcs[++i_end]; }
 private final char getUnsignedByte() { return (char)(bcs[++i_end] & 0xFF); }
 private final short getSignedWord() {
     int i = bcs[++i_end] << 8;
     i |= bcs[++i_end] & 0xFF;
     return (short)i;
 }
 private final char getUnsignedWord() {
     int i = (bcs[++i_end] & 0xFF) << 8;
     i |= (bcs[++i_end] & 0xFF);
     return (char)i;
 }
 private final int getSignedDWord() {
     int i = bcs[++i_end] << 24;
     i |= (bcs[++i_end] & 0xFF) << 16;
     i |= (bcs[++i_end] & 0xFF) << 8;
     i |= (bcs[++i_end] & 0xFF);
     return i;
 }
 
}

/*
class BytecodeVisitorTemplate extends BytecodeVisitor {

 BytecodeVisitorTemplate(jq_Method method) {
     super(method);
 }
 
 public void forwardTraversal() throws VerifyError {
     if (TRACE) out.println(this+": Starting traversal.");
     super.forwardTraversal();
     if (TRACE) out.println(this+": Finished traversal.");
 }
 
 public void visitBytecode() throws VerifyError {
     if (TRACE) out.println(this+": Visiting bytecode at "+i_start);
     super.visitBytecode();
     if (TRACE) out.println(this+": Finished visiting bytecode at "+i_start+" to "+i_end);
 }
 
 public void visitPEI() {
     super.visitPEI();
 }
 public void visitNOP() {
     super.visitNOP();
 }
 public void visitACONST(Object s) {
     super.visitACONST(s);
 }
 public void visitICONST(int c) {
     super.visitICONST(c);
 }
 public void visitLCONST(long c) {
     super.visitLCONST(c);
 }
 public void visitFCONST(float c) {
     super.visitFCONST(c);
 }
 public void visitDCONST(double c) {
     super.visitDCONST(c);
 }
 public void visitILOAD(int i) {
     super.visitILOAD(i);
 }
 public void visitLLOAD(int i) {
     super.visitLLOAD(i);
 }
 public void visitFLOAD(int i) {
     super.visitFLOAD(i);
 }
 public void visitDLOAD(int i) {
     super.visitDLOAD(i);
 }
 public void visitALOAD(int i) {
     super.visitALOAD(i);
 }
 public void visitISTORE(int i) {
     super.visitISTORE(i);
 }
 public void visitLSTORE(int i) {
     super.visitLSTORE(i);
 }
 public void visitFSTORE(int i) {
     super.visitFSTORE(i);
 }
 public void visitDSTORE(int i) {
     super.visitDSTORE(i);
 }
 public void visitASTORE(int i) {
     super.visitASTORE(i);
 }
 public void visitIALOAD() {
     super.visitIALOAD();
 }
 public void visitLALOAD() {
     super.visitLALOAD();
 }
 public void visitFALOAD() {
     super.visitFALOAD();
 }
 public void visitDALOAD() {
     super.visitDALOAD();
 }
 public void visitAALOAD() {
     super.visitAALOAD();
 }
 public void visitBALOAD() {
     super.visitBALOAD();
 }
 public void visitCALOAD() {
     super.visitCALOAD();
 }
 public void visitSALOAD() {
     super.visitSALOAD();
 }
 public void visitIASTORE() {
     super.visitIASTORE();
 }
 public void visitLASTORE() {
     super.visitLASTORE();
 }
 public void visitFASTORE() {
     super.visitFASTORE();
 }
 public void visitDASTORE() {
     super.visitDASTORE();
 }
 public void visitAASTORE() {
     super.visitAASTORE();
 }
 public void visitBASTORE() {
     super.visitBASTORE();
 }
 public void visitCASTORE() {
     super.visitCASTORE();
 }
 public void visitSASTORE() {
     super.visitSASTORE();
 }
 public void visitPOP() {
     super.visitPOP();
 }
 public void visitPOP2() {
     super.visitPOP2();
 }
 public void visitDUP() {
     super.visitDUP();
 }
 public void visitDUP_x1() {
     super.visitDUP_x1();
 }
 public void visitDUP_x2() {
     super.visitDUP_x2();
 }
 public void visitDUP2() {
     super.visitDUP2();
 }
 public void visitDUP2_x1() {
     super.visitDUP2_x1();
 }
 public void visitDUP2_x2() {
     super.visitDUP2_x2();
 }
 public void visitSWAP() {
     super.visitSWAP();
 }
 public void visitIBINOP(byte op) {
     super.visitIBINOP(op);
 }
 public void visitLBINOP(byte op) {
     super.visitLBINOP(op);
 }
 public void visitFBINOP(byte op) {
     super.visitFBINOP(op);
 }
 public void visitDBINOP(byte op) {
     super.visitDBINOP(op);
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
 }
 public void visitLSHIFT(byte op) {
     super.visitLSHIFT(op);
 }
 public void visitIINC(int i, int v) {
     super.visitIINC(i, v);
 }
 public void visitI2L() {
     super.visitI2L();
 }
 public void visitI2F() {
     super.visitI2F();
 }
 public void visitI2D() {
     super.visitI2D();
 }
 public void visitL2I() {
     super.visitL2I();
 }
 public void visitL2F() {
     super.visitL2F();
 }
 public void visitL2D() {
     super.visitL2D();
 }
 public void visitF2I() {
     super.visitF2I();
 }
 public void visitF2L() {
     super.visitF2L();
 }
 public void visitF2D() {
     super.visitF2D();
 }
 public void visitD2I() {
     super.visitD2I();
 }
 public void visitD2L() {
     super.visitD2L();
 }
 public void visitD2F() {
     super.visitD2F();
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
 }
 public void visitFCMP2(byte op) {
     super.visitFCMP2(op);
 }
 public void visitDCMP2(byte op) {
     super.visitDCMP2(op);
 }
 public void visitIF(byte op, int target) {
     super.visitIF(op, target);
 }
 public void visitIFREF(byte op, int target) {
     super.visitIFREF(op, target);
 }
 public void visitIFCMP(byte op, int target) {
     super.visitIFCMP(op, target);
 }
 public void visitIFREFCMP(byte op, int target) {
     super.visitIFREFCMP(op, target);
 }
 public void visitGOTO(int target) {
     super.visitGOTO(target);
 }
 public void visitJSR(int target) {
     super.visitJSR(target);
 }
 public void visitRET(int i) {
     super.visitRET(i);
 }
 public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
     super.visitTABLESWITCH(default_target, low, high, targets);
 }
 public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
     super.visitLOOKUPSWITCH(default_target, values, targets);
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
 }
 public void visitLGETSTATIC(jq_StaticField f) {
     super.visitLGETSTATIC(f);
 }
 public void visitFGETSTATIC(jq_StaticField f) {
     super.visitFGETSTATIC(f);
 }
 public void visitDGETSTATIC(jq_StaticField f) {
     super.visitDGETSTATIC(f);
 }
 public void visitAGETSTATIC(jq_StaticField f) {
     super.visitAGETSTATIC(f);
 }
 public void visitZGETSTATIC(jq_StaticField f) {
     super.visitZGETSTATIC(f);
 }
 public void visitBGETSTATIC(jq_StaticField f) {
     super.visitBGETSTATIC(f);
 }
 public void visitCGETSTATIC(jq_StaticField f) {
     super.visitCGETSTATIC(f);
 }
 public void visitSGETSTATIC(jq_StaticField f) {
     super.visitSGETSTATIC(f);
 }
 public void visitIPUTSTATIC(jq_StaticField f) {
     super.visitIPUTSTATIC(f);
 }
 public void visitLPUTSTATIC(jq_StaticField f) {
     super.visitLPUTSTATIC(f);
 }
 public void visitFPUTSTATIC(jq_StaticField f) {
     super.visitFPUTSTATIC(f);
 }
 public void visitDPUTSTATIC(jq_StaticField f) {
     super.visitDPUTSTATIC(f);
 }
 public void visitAPUTSTATIC(jq_StaticField f) {
     super.visitAPUTSTATIC(f);
 }
 public void visitZPUTSTATIC(jq_StaticField f) {
     super.visitZPUTSTATIC(f);
 }
 public void visitBPUTSTATIC(jq_StaticField f) {
     super.visitBPUTSTATIC(f);
 }
 public void visitCPUTSTATIC(jq_StaticField f) {
     super.visitCPUTSTATIC(f);
 }
 public void visitSPUTSTATIC(jq_StaticField f) {
     super.visitSPUTSTATIC(f);
 }
 public void visitIGETFIELD(jq_InstanceField f) {
     super.visitIGETFIELD(f);
 }
 public void visitLGETFIELD(jq_InstanceField f) {
     super.visitLGETFIELD(f);
 }
 public void visitFGETFIELD(jq_InstanceField f) {
     super.visitFGETFIELD(f);
 }
 public void visitDGETFIELD(jq_InstanceField f) {
     super.visitDGETFIELD(f);
 }
 public void visitAGETFIELD(jq_InstanceField f) {
     super.visitAGETFIELD(f);
 }
 public void visitBGETFIELD(jq_InstanceField f) {
     super.visitBGETFIELD(f);
 }
 public void visitCGETFIELD(jq_InstanceField f) {
     super.visitCGETFIELD(f);
 }
 public void visitSGETFIELD(jq_InstanceField f) {
     super.visitSGETFIELD(f);
 }
 public void visitZGETFIELD(jq_InstanceField f) {
     super.visitZGETFIELD(f);
 }
 public void visitIPUTFIELD(jq_InstanceField f) {
     super.visitIPUTFIELD(f);
 }
 public void visitLPUTFIELD(jq_InstanceField f) {
     super.visitLPUTFIELD(f);
 }
 public void visitFPUTFIELD(jq_InstanceField f) {
     super.visitFPUTFIELD(f);
 }
 public void visitDPUTFIELD(jq_InstanceField f) {
     super.visitDPUTFIELD(f);
 }
 public void visitAPUTFIELD(jq_InstanceField f) {
     super.visitAPUTFIELD(f);
 }
 public void visitBPUTFIELD(jq_InstanceField f) {
     super.visitBPUTFIELD(f);
 }
 public void visitCPUTFIELD(jq_InstanceField f) {
     super.visitCPUTFIELD(f);
 }
 public void visitSPUTFIELD(jq_InstanceField f) {
     super.visitSPUTFIELD(f);
 }
 public void visitZPUTFIELD(jq_InstanceField f) {
     super.visitZPUTFIELD(f);
 }
 public void visitIINVOKE(byte op, jq_Method f) {
     super.visitIINVOKE(op, f);
 }
 public void visitLINVOKE(byte op, jq_Method f) {
     super.visitLINVOKE(op, f);
 }
 public void visitFINVOKE(byte op, jq_Method f) {
     super.visitFINVOKE(op, f);
 }
 public void visitDINVOKE(byte op, jq_Method f) {
     super.visitDINVOKE(op, f);
 }
 public void visitAINVOKE(byte op, jq_Method f) {
     super.visitAINVOKE(op, f);
 }
 public void visitVINVOKE(byte op, jq_Method f) {
     super.visitVINVOKE(op, f);
 }
 public void visitNEW(jq_Type f) {
     super.visitNEW(f);
 }
 public void visitNEWARRAY(jq_Array f) {
     super.visitNEWARRAY(f);
 }
 public void visitCHECKCAST(jq_Type f) {
     super.visitCHECKCAST(f);
 }
 public void visitINSTANCEOF(jq_Type f) {
     super.visitINSTANCEOF(f);
 }
 public void visitARRAYLENGTH() {
     super.visitARRAYLENGTH();
 }
 public void visitATHROW() {
     super.visitATHROW();
 }
 public void visitMONITOR(byte op) {
     super.visitMONITOR(op);
 }
 public void visitMULTINEWARRAY(jq_Type f, char dim) {
     super.visitMULTINEWARRAY(f, dim);
 }

}
*/
