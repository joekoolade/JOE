// Unsafe.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_StaticMethod;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;
import joeq.Scheduler.jq_Thread;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Unsafe.java,v 1.14 2004/03/09 22:36:58 jwhaley Exp $
 */
public abstract class Unsafe {

    public static final native int floatToIntBits(float i);
    public static final native float intBitsToFloat(int i);
    public static final native long doubleToLongBits(double i);
    public static final native double longBitsToDouble(long i);
    public static final native void pushArg(int arg);
    public static final native void pushArgA(Address arg);
    public static final native float popFP32();
    public static final native double popFP64();
    public static final native void pushFP32(float v);
    public static final native void pushFP64(double v);
    public static final native long invoke(CodeAddress address) throws Throwable;
    public static final native Address invokeA(CodeAddress address) throws Throwable;
    public static final native int EAX();
    public static final native jq_Thread getThreadBlock();
    public static final native void setThreadBlock(jq_Thread t);
    public static final native void longJump(CodeAddress ip, StackAddress fp, StackAddress sp, int eax);
    //public static final jq_Reference getTypeOf(Object o) { return remapper_object.getTypeOf(o); }
    /*
    public static final int atomicCas4(int address, int before, int after) {
        int val = peek(address);
        if (val == before) { remapper_object.poke4(address, after); return after; }
        return val;
    }
    */
    public static final native boolean isEQ();
    public static final native boolean isGE();

    public static final jq_Class _class;
    public static final jq_StaticMethod _floatToIntBits;
    public static final jq_StaticMethod _intBitsToFloat;
    public static final jq_StaticMethod _doubleToLongBits;
    public static final jq_StaticMethod _longBitsToDouble;
    public static final jq_StaticMethod _pushArg;
    public static final jq_StaticMethod _pushArgA;
    public static final jq_StaticMethod _popFP32;
    public static final jq_StaticMethod _popFP64;
    public static final jq_StaticMethod _pushFP32;
    public static final jq_StaticMethod _pushFP64;
    public static final jq_StaticMethod _invoke;
    public static final jq_StaticMethod _invokeA;
    public static final jq_StaticMethod _EAX;
    public static final jq_StaticMethod _getThreadBlock;
    public static final jq_StaticMethod _setThreadBlock;
    public static final jq_StaticMethod _longJump;
    public static final jq_StaticMethod _isEQ;
    public static final jq_StaticMethod _isGE;
    
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/Unsafe;");
        _floatToIntBits = _class.getOrCreateStaticMethod("floatToIntBits", "(F)I");
        _intBitsToFloat = _class.getOrCreateStaticMethod("intBitsToFloat", "(I)F");
        _doubleToLongBits = _class.getOrCreateStaticMethod("doubleToLongBits", "(D)J");
        _longBitsToDouble = _class.getOrCreateStaticMethod("longBitsToDouble", "(J)D");
        _pushArg = _class.getOrCreateStaticMethod("pushArg", "(I)V");
        _pushArgA = _class.getOrCreateStaticMethod("pushArgA", "(Ljoeq/Memory/Address;)V");
        _popFP32 = _class.getOrCreateStaticMethod("popFP32", "()F");
        _popFP64 = _class.getOrCreateStaticMethod("popFP64", "()D");
        _pushFP32 = _class.getOrCreateStaticMethod("pushFP32", "(F)V");
        _pushFP64 = _class.getOrCreateStaticMethod("pushFP64", "(D)V");
        _invoke = _class.getOrCreateStaticMethod("invoke", "(Ljoeq/Memory/CodeAddress;)J");
        _invokeA = _class.getOrCreateStaticMethod("invokeA", "(Ljoeq/Memory/CodeAddress;)Ljoeq/Memory/Address;");
        _EAX = _class.getOrCreateStaticMethod("EAX", "()I");
        _getThreadBlock = _class.getOrCreateStaticMethod("getThreadBlock", "()Ljoeq/Scheduler/jq_Thread;");
        _setThreadBlock = _class.getOrCreateStaticMethod("setThreadBlock", "(Ljoeq/Scheduler/jq_Thread;)V");
        _longJump = _class.getOrCreateStaticMethod("longJump", "(Ljoeq/Memory/CodeAddress;Ljoeq/Memory/StackAddress;Ljoeq/Memory/StackAddress;I)V");
        _isEQ = _class.getOrCreateStaticMethod("isEQ", "()Z");
        _isGE = _class.getOrCreateStaticMethod("isGE", "()Z");
    }
    
}
