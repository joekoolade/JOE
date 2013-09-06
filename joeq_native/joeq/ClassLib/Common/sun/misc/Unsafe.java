// Unsafe.java, created Tue Dec 10 14:02:37 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.sun.misc;

import joeq.ClassLib.ClassLibInterface;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;

/**
 * Unsafe
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Unsafe.java,v 1.7 2004/08/07 07:30:50 joewhaley Exp $
 */
public final class Unsafe {

    public java.lang.Object getObject(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return ((HeapAddress)a.offset(x).peek()).asObject();
    }
    public java.lang.Object getObject(java.lang.Object o, long x) {
        return getObject(o, (int) x);
    }
    
    public void putObject(java.lang.Object o1, int x, java.lang.Object v) {
        HeapAddress a = HeapAddress.addressOf(o1);
        a.offset(x).poke(HeapAddress.addressOf(v));
    }
    public void putObject(java.lang.Object o1, long x, java.lang.Object v) {
        putObject(o1, (int) x, v);
    }
    
    public boolean getBoolean(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return a.offset(x).peek1() != (byte)0;
    }
    public boolean getBoolean(java.lang.Object o, long x) {
        return getBoolean(o, (int) x);
    }
    
    public void putBoolean(java.lang.Object o, int x, boolean v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke1(v?(byte)1:(byte)0);
    }
    public void putBoolean(java.lang.Object o1, long x, boolean v) {
        putBoolean(o1, (int) x, v);
    }
    
    public byte getByte(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return a.offset(x).peek1();
    }
    public byte getByte(java.lang.Object o, long x) {
        return getByte(o, (int) x);
    }
    
    public void putByte(java.lang.Object o, int x, byte v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke1(v);
    }
    public void putByte(java.lang.Object o1, long x, byte v) {
        putByte(o1, (int) x, v);
    }
    
    public short getShort(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return a.offset(x).peek2();
    }
    public short getShort(java.lang.Object o, long x) {
        return getShort(o, (int) x);
    }
    
    public void putShort(java.lang.Object o, int x, short v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke2(v);
    }
    public void putShort(java.lang.Object o1, long x, short v) {
        putShort(o1, (int) x, v);
    }
    
    public char getChar(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return (char) a.offset(x).peek2();
    }
    public char getChar(java.lang.Object o, long x) {
        return getChar(o, (int) x);
    }
    
    public void putChar(java.lang.Object o, int x, char v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke2((short) v);
    }
    public void putChar(java.lang.Object o1, long x, char v) {
        putChar(o1, (int) x, v);
    }
    
    public int getInt(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return a.offset(x).peek4();
    }
    public int getInt(java.lang.Object o, long x) {
        return getInt(o, (int) x);
    }
    
    public void putInt(java.lang.Object o, int x, int v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke4(v);
    }
    public void putInt(java.lang.Object o1, long x, int v) {
        putInt(o1, (int) x, v);
    }
    
    public long getLong(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return a.offset(x).peek8();
    }
    public long getLong(java.lang.Object o, long x) {
        return getLong(o, (int) x);
    }
    
    public void putLong(java.lang.Object o, int x, long v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke8(v);
    }
    public void putLong(java.lang.Object o1, long x, long v) {
        putLong(o1, (int) x, v);
    }
    
    public float getFloat(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return Float.intBitsToFloat(a.offset(x).peek4());
    }
    public float getFloat(java.lang.Object o, long x) {
        return getFloat(o, (int) x);
    }
    
    public void putFloat(java.lang.Object o, int x, float v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke4(Float.floatToRawIntBits(v));
    }
    public void putFloat(java.lang.Object o1, long x, float v) {
        putFloat(o1, (int) x, v);
    }
    
    public double getDouble(java.lang.Object o, int x) {
        HeapAddress a = HeapAddress.addressOf(o);
        return Double.longBitsToDouble(a.offset(x).peek8());
    }
    public double getDouble(java.lang.Object o, long x) {
        return getDouble(o, (int) x);
    }
    
    public void putDouble(java.lang.Object o, int x, double v) {
        HeapAddress a = HeapAddress.addressOf(o);
        a.offset(x).poke8(Double.doubleToRawLongBits(v));
    }
    public void putDouble(java.lang.Object o1, long x, double v) {
        putDouble(o1, (int) x, v);
    }
    
    public byte getByte(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return a.peek1();
    }
    
    public void putByte(long addr, byte v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke1(v);
    }
    
    public short getShort(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return a.peek2();
    }
    
    public void putShort(long addr, short v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke2(v);
    }
    
    public char getChar(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return (char) a.peek2();
    }
    
    public void putChar(long addr, char v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke2((short)v);
    }
    
    public int getInt(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return a.peek4();
    }
    
    public void putInt(long addr, int v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke4(v);
    }
    
    public long getLong(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return a.peek8();
    }
    
    public void putLong(long addr, long v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke8(v);
    }
    
    public float getFloat(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return Float.intBitsToFloat(a.peek4());
    }
    
    public void putFloat(long addr, float v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke4(Float.floatToRawIntBits(v));
    }
    
    public double getDouble(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return Double.longBitsToDouble(a.peek8());
    }
    
    public void putDouble(long addr, double v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        a.poke8(Double.doubleToRawLongBits(v));
    }
    
    public long getAddress(long addr) {
        HeapAddress a = HeapAddress.address32((int) addr);
        return (long) a.peek().to32BitValue();
    }
    
    public void putAddress(long addr, long v) {
        HeapAddress a = HeapAddress.address32((int) addr);
        HeapAddress b = HeapAddress.address32((int) v);
        a.poke(b);
    }
    
    public long allocateMemory(long v) {
        return SystemInterface.syscalloc((int) v).to32BitValue();
    }
    
    //public long reallocateMemory(long addr, long size) {
    
    public void setMemory(long to, long size, byte b) {
        HeapAddress a = HeapAddress.address32((int) to);
        SystemInterface.mem_set(a, b, (int) size);
    }
    
    public void copyMemory(long to, long from, long size) {
        HeapAddress a = HeapAddress.address32((int) to);
        HeapAddress b = HeapAddress.address32((int) from);
        SystemInterface.mem_cpy(a, b, (int) size);
    }
    
    public void freeMemory(long v) {
        HeapAddress a = HeapAddress.address32((int) v);
        SystemInterface.sysfree(a);
    }
    
    public long objectFieldOffset(java.lang.reflect.Field field) {
        return (long) fieldOffset(field);
    }
    public int fieldOffset(java.lang.reflect.Field field) {
        jq_Field f = (jq_Field) ClassLibInterface.DEFAULT.getJQField(field);
        jq_Class c = f.getDeclaringClass();
        c.load(); c.verify(); c.prepare();
        if (f instanceof jq_InstanceField) {
            return ((jq_InstanceField)f).getOffset();
        } else {
            HeapAddress a = ((jq_StaticField)f).getAddress();
            HeapAddress b = HeapAddress.addressOf(c.getStaticData());
            return b.difference(a);
        }
    }
    
    public java.lang.Object staticFieldBase(java.lang.Class k) {
        jq_Type t = ClassLibInterface.DEFAULT.getJQType(k);
        if (t instanceof jq_Class) {
            jq_Class c = (jq_Class) t;
            return c.getStaticData();
        }
        return null;
    }
    
    public void ensureClassInitialized(java.lang.Class k) {
        jq_Type t = ClassLibInterface.DEFAULT.getJQType(k);
        t.load(); t.verify(); t.prepare(); t.sf_initialize(); t.cls_initialize();
    }
    
    public int arrayBaseOffset(java.lang.Class k) {
        return 0;
    }
    
    public int arrayIndexScale(java.lang.Class k) {
        jq_Type t = ClassLibInterface.DEFAULT.getJQType(k);
        if (t instanceof jq_Array) {
            int width = ((jq_Array)t).getElementType().getReferenceSize();
            switch (width) {
            case 4: return 2;
            case 2: return 1;
            case 1: return 0;
            case 8: return 3;
            }
        }
        return -1;
    }
    
    public int addressSize() {
        return HeapAddress.size();
    }
    
    public int pageSize() {
        return 1 << HeapAddress.pageAlign();
    }
    
    public java.lang.Class defineClass(java.lang.String name, byte[] b, int off, int len, joeq.ClassLib.Common.java.lang.ClassLoader cl, java.security.ProtectionDomain pd) {
        return cl.defineClass0(name, b, off, len, pd);
    }
    
    public java.lang.Class defineClass(java.lang.String name, byte[] b, int off, int len, joeq.ClassLib.Common.java.lang.ClassLoader cl) {
        return cl.defineClass0(name, b, off, len, null);
    }
    
    public java.lang.Object allocateInstance(java.lang.Class k)
    throws java.lang.InstantiationException {
        jq_Type t = ClassLibInterface.DEFAULT.getJQType(k);
        if (t instanceof jq_Class) {
            jq_Class c = (jq_Class) t;
            return c.newInstance();
        }
        throw new java.lang.InstantiationException();
    }
    
    public void monitorEnter(java.lang.Object o) {
        joeq.Runtime.Monitor.monitorenter(o);
    }
    
    public void monitorExit(java.lang.Object o) {
        joeq.Runtime.Monitor.monitorexit(o);
    }
    
    public void throwException(java.lang.Throwable o) {
        joeq.Runtime.ExceptionDeliverer.athrow(o);
    }
}
