// B2QUnsafeHandler.java, created Mon Dec 23 23:00:34 2002 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operator.Special;
import joeq.Compiler.Quad.Operator.Unary;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_Thread;
import jwutil.util.Assert;

/*
 * @author  Michael Martin <mcmartin@stanford.edu>
 * @version $Id: B2QUnsafeHandler.java,v 1.9 2004/12/10 19:02:26 joewhaley Exp $
 */
class B2QUnsafeHandler implements BytecodeToQuad.UnsafeHelper {
    public boolean isUnsafe(jq_Method m) {
        return m.getDeclaringClass() == Unsafe._class;
    }
    public boolean endsBB(jq_Method m) {
        return m == Unsafe._longJump;
    }
    public boolean handleMethod(BytecodeToQuad b2q, ControlFlowGraph quad_cfg, BytecodeToQuad.AbstractState current_state, jq_Method m, Operator.Invoke oper) {
        Quad q;
        if (m == Unsafe._floatToIntBits) {
            Operand op = current_state.pop_F();
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.INT);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.FLOAT_2INTBITS.INSTANCE, res, op);
            current_state.push_I(res.copy());
        } else if (m == Unsafe._intBitsToFloat) {
            Operand op = current_state.pop_I();
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.FLOAT);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.INTBITS_2FLOAT.INSTANCE, res, op);
            current_state.push_F(res.copy());
        } else if (m == Unsafe._doubleToLongBits) {
            Operand op = current_state.pop_D();
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.LONG);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.DOUBLE_2LONGBITS.INSTANCE, res, op);
            current_state.push_L(res.copy());
        } else if (m == Unsafe._longBitsToDouble) {
            Operand op = current_state.pop_L();
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.DOUBLE);
            q = Unary.create(quad_cfg.getNewQuadID(), Unary.LONGBITS_2DOUBLE.INSTANCE, res, op);
            current_state.push_D(res.copy());
        } else if (m == Unsafe._getThreadBlock) {
            RegisterOperand res = b2q.getStackRegister(jq_Thread._class);
            q = Special.create(quad_cfg.getNewQuadID(), Special.GET_THREAD_BLOCK.INSTANCE, res);
            current_state.push_A(res.copy());
        } else if (m == Unsafe._setThreadBlock) {
            Operand loc = current_state.pop_A();
            q = Special.create(quad_cfg.getNewQuadID(), Special.SET_THREAD_BLOCK.INSTANCE, loc);
        } else if (m == Unsafe._longJump) {
            Operand eax = current_state.pop_I();
            Operand sp = current_state.pop(StackAddress._class);
            Operand fp = current_state.pop(StackAddress._class);
            Operand ip = current_state.pop(CodeAddress._class);
            q = Special.create(quad_cfg.getNewQuadID(), Special.LONG_JUMP.INSTANCE, ip, fp, sp, eax);
        } else if (m == Unsafe._popFP32) {
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.FLOAT);
            q = Special.create(quad_cfg.getNewQuadID(), Special.POP_FP32.INSTANCE, res);
            current_state.push_F(res.copy());
        } else if (m == Unsafe._popFP64) {
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.DOUBLE);
            q = Special.create(quad_cfg.getNewQuadID(), Special.POP_FP64.INSTANCE, res);
            current_state.push_D(res.copy());
        } else if (m == Unsafe._pushFP32) {
            Operand val = current_state.pop_F();
            q = Special.create(quad_cfg.getNewQuadID(), Special.PUSH_FP32.INSTANCE, val);
        } else if (m == Unsafe._pushFP64) {
            Operand val = current_state.pop_D();
            q = Special.create(quad_cfg.getNewQuadID(), Special.PUSH_FP64.INSTANCE, val);
        } else if (m == Unsafe._EAX) {
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.INT);
            q = Special.create(quad_cfg.getNewQuadID(), Special.GET_EAX.INSTANCE, res);
            current_state.push_I(res.copy());
        } else if (m == Unsafe._pushArg) {
            Operand val = current_state.pop_I();
            q = Special.create(quad_cfg.getNewQuadID(), Special.PUSHARG_I.INSTANCE, val);
        } else if (m == Unsafe._pushArgA) {
            Operand val = current_state.pop_P();
            q = Special.create(quad_cfg.getNewQuadID(), Special.PUSHARG_P.INSTANCE, val);
        } else if (m == Unsafe._invoke) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.LONG);
            q = Special.create(quad_cfg.getNewQuadID(), Special.INVOKE_L.INSTANCE, res, loc);
            current_state.push_L(res.copy());
        } else if (m == Unsafe._invokeA) {
            Operand loc = current_state.pop_P();
            RegisterOperand res = b2q.getStackRegister(Address._class);
            q = Special.create(quad_cfg.getNewQuadID(), Special.INVOKE_P.INSTANCE, res, loc);
            current_state.push_P(res.copy());
        } else if (m == Unsafe._isEQ) {
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.BOOLEAN);
            q = Special.create(quad_cfg.getNewQuadID(), Special.ISEQ.INSTANCE, res);
            current_state.push_I(res.copy());
        } else if (m == Unsafe._isGE) {
            RegisterOperand res = b2q.getStackRegister(jq_Primitive.BOOLEAN);
            q = Special.create(quad_cfg.getNewQuadID(), Special.ISGE.INSTANCE, res);
            current_state.push_I(res.copy());
        } else {
            System.err.println(m.toString());
            Assert.UNREACHABLE();
            return false;
        }
        b2q.appendQuad(q);
        return true;
    }
}
