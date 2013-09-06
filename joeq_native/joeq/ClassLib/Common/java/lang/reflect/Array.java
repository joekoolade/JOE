// Array.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang.reflect;

import joeq.Allocator.ObjectLayout;
import joeq.Class.jq_Array;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.ClassLib.ClassLibInterface;
import joeq.Memory.HeapAddress;
import jwutil.util.Convert;

/**
 * Array
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Array.java,v 1.14 2004/09/30 03:35:35 joewhaley Exp $
 */
public abstract class Array {

    public static int getLength(Object array) throws IllegalArgumentException {
        if (!jq_Reference.getTypeOf(array).isArrayType())
            throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
        return HeapAddress.addressOf(array).offset(ObjectLayout.ARRAY_LENGTH_OFFSET).peek4();
    }
    public static Object get(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof Object[]) return ((Object[])array)[index];
        if (array instanceof int[]) return new Integer(((int[])array)[index]);
        if (array instanceof long[]) return new Long(((long[])array)[index]);
        if (array instanceof float[]) return new Float(((float[])array)[index]);
        if (array instanceof double[]) return new Double(((double[])array)[index]);
        if (array instanceof boolean[]) return Convert.getBoolean(((boolean[])array)[index]);
        if (array instanceof byte[]) return new Byte(((byte[])array)[index]);
        if (array instanceof short[]) return new Short(((short[])array)[index]);
        if (array instanceof char[]) return new Character(((char[])array)[index]);
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static boolean getBoolean(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof boolean[]) return ((boolean[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static byte getByte(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof byte[]) return ((byte[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static char getChar(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof char[]) return ((char[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static short getShort(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof short[]) return ((short[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static int getInt(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof int[]) return ((int[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static long getLong(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof long[]) return ((long[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static float getFloat(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof float[]) return ((float[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static double getDouble(Object array, int index) {
        if (array == null) throw new NullPointerException();
        if (array instanceof double[]) return ((double[])array)[index];
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void set(Object array, int index, Object value)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof Object[]) {
            ((Object[])array)[index] = value;
            return;
        }
        if (array instanceof boolean[]) {
            boolean v;
            if (value instanceof Boolean) v = ((Boolean)value).booleanValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((boolean[])array)[index] = v;
            return;
        }
        if (array instanceof byte[]) {
            byte v;
            if (value instanceof Byte) v = ((Byte)value).byteValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((byte[])array)[index] = v;
            return;
        }
        if (array instanceof short[]) {
            short v;
            if (value instanceof Short) v = ((Short)value).shortValue();
            else if (value instanceof Byte) v = (short)((Byte)value).byteValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((short[])array)[index] = v;
            return;
        }
        if (array instanceof char[]) {
            char v;
            if (value instanceof Character) v = ((Character)value).charValue();
            else if (value instanceof Byte) v = (char)((Byte)value).byteValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((char[])array)[index] = v;
            return;
        }
        if (array instanceof int[]) {
            int v;
            if (value instanceof Integer) v = ((Integer)value).intValue();
            else if (value instanceof Character) v = (int)((Character)value).charValue();
            else if (value instanceof Short) v = (int)((Short)value).shortValue();
            else if (value instanceof Byte) v = (int)((Byte)value).byteValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((int[])array)[index] = v;
            return;
        }
        if (array instanceof long[]) {
            long v;
            if (value instanceof Long) v = ((Long)value).longValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((long[])array)[index] = v;
            return;
        }
        if (array instanceof float[]) {
            float v;
            if (value instanceof Float) v = ((Float)value).floatValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((float[])array)[index] = v;
            return;
        }
        if (array instanceof double[]) {
            double v;
            if (value instanceof Double) v = ((Double)value).doubleValue();
            else throw new IllegalArgumentException("cannot store value of type "+jq_Reference.getTypeOf(value)+" into array of type "+jq_Reference.getTypeOf(array));
            ((double[])array)[index] = v;
            return;
        }
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setBoolean(Object array, int index, boolean z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof boolean[]) ((boolean[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setByte(Object array, int index, byte z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof byte[]) ((byte[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setChar(Object array, int index, char z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof char[]) ((char[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setShort(Object array, int index, short z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof short[]) ((short[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setInt(Object array, int index, int z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof int[]) ((int[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setLong(Object array, int index, long z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof long[]) ((long[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setFloat(Object array, int index, float z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof float[]) ((float[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    public static void setDouble(Object array, int index, double z)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array == null) throw new NullPointerException();
        if (array instanceof double[]) ((double[])array)[index] = z;
        throw new IllegalArgumentException(jq_Reference.getTypeOf(array).toString());
    }
    private static Object newArray(Class componentType, int length)
    throws NegativeArraySizeException {
        jq_Type t = ClassLibInterface.DEFAULT.getJQType(componentType);
        if (t == jq_Primitive.VOID)
            throw new IllegalArgumentException("cannot create a void array");
        jq_Array a = t.getArrayTypeForElementType();
        a.cls_initialize();
        return a.newInstance(length);
    }
    private static Object multiNewArray(Class componentType, int[] dimensions)
    throws IllegalArgumentException, NegativeArraySizeException {
        jq_Type a = ClassLibInterface.DEFAULT.getJQType(componentType);
        if (a == jq_Primitive.VOID)
            throw new IllegalArgumentException("cannot create a void array");
        if (dimensions.length == 0)
            throw new IllegalArgumentException("dimensions array is zero");
        for (int i=0; i<dimensions.length; ++i) {
            // check for dim < 0 here, because if a dim is zero, later dim's
            // are not checked by multinewarray_helper.
            if (dimensions[i] < 0)
                throw new NegativeArraySizeException("dim "+i+": "+dimensions[i]+" < 0");
            a = a.getArrayTypeForElementType();
            a.cls_initialize();
        }
        return joeq.Runtime.Arrays.multinewarray_helper(dimensions, 0, (jq_Array)a);
    }
    
}
