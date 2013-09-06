// Field.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang.reflect;

import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.ClassLib.Common.ClassUtils;
import joeq.Main.jq;
import joeq.Runtime.Reflection;
import joeq.Runtime.TypeCheck;
import joeq.UTF.Utf8;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * Field
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Field.java,v 1.17 2004/10/08 08:43:02 joewhaley Exp $
 */
public class Field extends AccessibleObject {
    
    // additional instance field.
    public final jq_Field jq_field;
    
    private java.lang.Class clazz;
    private java.lang.String name;
    private java.lang.Class type;
    private int modifiers;
    private int slot;
    
    private Field(jq_Field f) {
        this.jq_field = f;
    }
    
    private Field(java.lang.Class clazz,
                  java.lang.String name,
                  java.lang.Class type,
                  int modifiers,
                  int slot) {
        this.clazz = clazz;
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.slot = slot;
        
        jq_Class c = (jq_Class) Reflection.getJQType(clazz);
        //if (c == null) return null;
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get(name), Reflection.getJQType(type).getDesc());
        nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(c, nd);
        jq_Field m = (jq_Field)c.getDeclaredMember(nd);
        if (m == null) {
            if (java.lang.reflect.Modifier.isStatic(modifiers))
                m = c.getOrCreateStaticField(nd);
            else
                m = c.getOrCreateInstanceField(nd);
        }
        this.jq_field = m;
    }
    
    // overridden implementations.
    public java.lang.Class getDeclaringClass() {
        jq_Field jq_f = this.jq_field;
        return jq_f.getDeclaringClass().getJavaLangClassObject();
    }
    public String getName() {
        jq_Field jq_f = this.jq_field;
        return jq_f.getName().toString();
    }
    public int getModifiers() {
        jq_Field jq_f = this.jq_field;
        return jq_f.getAccessFlags();
    }
    public Class getType() {
        jq_Field jq_f = this.jq_field;
        return jq_f.getType().getJavaLangClassObject();
    }
    public boolean equals(Object obj) {
        return this == obj;
    }
    
    // native method implementations.
    public java.lang.Object get(java.lang.Object obj)
        throws java.lang.IllegalArgumentException, java.lang.IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t.isReferenceType()) return Reflection.getstatic_A(sf);
            if (t == jq_Primitive.INT) return new Integer(Reflection.getstatic_I(sf));
            if (t == jq_Primitive.FLOAT) return new Float(Reflection.getstatic_F(sf));
            if (t == jq_Primitive.LONG) return new Long(Reflection.getstatic_L(sf));
            if (t == jq_Primitive.DOUBLE) return new Double(Reflection.getstatic_D(sf));
            if (t == jq_Primitive.BOOLEAN) return Convert.getBoolean(Reflection.getstatic_Z(sf));
            if (t == jq_Primitive.BYTE) return new Byte(Reflection.getstatic_B(sf));
            if (t == jq_Primitive.SHORT) return new Short(Reflection.getstatic_S(sf));
            if (t == jq_Primitive.CHAR) return new Character(Reflection.getstatic_C(sf));
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t.isReferenceType()) return Reflection.getfield_A(obj, f);
            if (t == jq_Primitive.INT) return new Integer(Reflection.getfield_I(obj, f));
            if (t == jq_Primitive.FLOAT) return new Float(Reflection.getfield_F(obj, f));
            if (t == jq_Primitive.LONG) return new Long(Reflection.getfield_L(obj, f));
            if (t == jq_Primitive.DOUBLE) return new Double(Reflection.getfield_D(obj, f));
            if (t == jq_Primitive.BOOLEAN) return Convert.getBoolean(Reflection.getfield_Z(obj, f));
            if (t == jq_Primitive.BYTE) return new Byte(Reflection.getfield_B(obj, f));
            if (t == jq_Primitive.SHORT) return new Short(Reflection.getfield_S(obj, f));
            if (t == jq_Primitive.CHAR) return new Character(Reflection.getfield_C(obj, f));
        }
        Assert.UNREACHABLE();
        return null;
    }

    public boolean getBoolean(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.BOOLEAN) return Reflection.getstatic_Z(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.BOOLEAN) return Reflection.getfield_Z(obj, f);
        }
        Assert.UNREACHABLE();
        return false;
    }
    
    public byte getByte(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.BYTE) return Reflection.getstatic_B(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.BYTE) return Reflection.getfield_B(obj, f);
        }
        Assert.UNREACHABLE();
        return (byte)0;
    }
    
    public char getChar(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.CHAR) return Reflection.getstatic_C(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.CHAR) return Reflection.getfield_C(obj, f);
        }
        Assert.UNREACHABLE();
        return (char)0;
    }
    
    // byte -> short
    public short getShort(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.SHORT) return Reflection.getstatic_S(sf);
            if (t == jq_Primitive.BYTE) return (short)Reflection.getstatic_B(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.SHORT) return Reflection.getfield_S(obj, f);
            if (t == jq_Primitive.BYTE) return (short)Reflection.getfield_B(obj, f);
        }
        Assert.UNREACHABLE();
        return (short)0;
    }
    
    // byte -> int
    // char -> int
    // short -> int
    public int getInt(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.INT) return Reflection.getstatic_I(sf);
            if (t == jq_Primitive.BYTE) return (int)Reflection.getstatic_B(sf);
            if (t == jq_Primitive.SHORT) return (int)Reflection.getstatic_S(sf);
            if (t == jq_Primitive.CHAR) return (int)Reflection.getstatic_C(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.INT) return Reflection.getfield_I(obj, f);
            if (t == jq_Primitive.BYTE) return (int)Reflection.getfield_B(obj, f);
            if (t == jq_Primitive.SHORT) return (int)Reflection.getfield_S(obj, f);
            if (t == jq_Primitive.CHAR) return (int)Reflection.getfield_C(obj, f);
        }
        Assert.UNREACHABLE();
        return 0;
    }
    
    // byte -> long
    // char -> long
    // short -> long
    // int -> long
    public long getLong(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.LONG) return Reflection.getstatic_L(sf);
            if (t == jq_Primitive.BYTE) return (long)Reflection.getstatic_B(sf);
            if (t == jq_Primitive.SHORT) return (long)Reflection.getstatic_S(sf);
            if (t == jq_Primitive.CHAR) return (long)Reflection.getstatic_C(sf);
            if (t == jq_Primitive.INT) return (long)Reflection.getstatic_I(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.LONG) return Reflection.getfield_L(obj, f);
            if (t == jq_Primitive.BYTE) return (long)Reflection.getfield_B(obj, f);
            if (t == jq_Primitive.SHORT) return (long)Reflection.getfield_S(obj, f);
            if (t == jq_Primitive.CHAR) return (long)Reflection.getfield_C(obj, f);
            if (t == jq_Primitive.INT) return (long)Reflection.getfield_I(obj, f);
        }
        Assert.UNREACHABLE();
        return 0L;
    }
    
    // byte -> float
    // char -> float
    // short -> float
    // int -> float
    // long -> float
    public float getFloat(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.FLOAT) return Reflection.getstatic_F(sf);
            if (t == jq_Primitive.BYTE) return (float)Reflection.getstatic_B(sf);
            if (t == jq_Primitive.SHORT) return (float)Reflection.getstatic_S(sf);
            if (t == jq_Primitive.CHAR) return (float)Reflection.getstatic_C(sf);
            if (t == jq_Primitive.INT) return (float)Reflection.getstatic_I(sf);
            if (t == jq_Primitive.LONG) return (float)Reflection.getstatic_L(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.FLOAT) return Reflection.getfield_F(obj, f);
            if (t == jq_Primitive.BYTE) return (float)Reflection.getfield_B(obj, f);
            if (t == jq_Primitive.SHORT) return (float)Reflection.getfield_S(obj, f);
            if (t == jq_Primitive.CHAR) return (float)Reflection.getfield_C(obj, f);
            if (t == jq_Primitive.INT) return (float)Reflection.getfield_I(obj, f);
            if (t == jq_Primitive.LONG) return (float)Reflection.getfield_L(obj, f);
        }
        Assert.UNREACHABLE();
        return 0F;
    }
    
    // byte -> double
    // char -> double
    // short -> double
    // int -> double
    // long -> double
    // float -> double
    public double getDouble(java.lang.Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.DOUBLE) return Reflection.getstatic_D(sf);
            if (t == jq_Primitive.BYTE) return (double)Reflection.getstatic_B(sf);
            if (t == jq_Primitive.SHORT) return (double)Reflection.getstatic_S(sf);
            if (t == jq_Primitive.CHAR) return (double)Reflection.getstatic_C(sf);
            if (t == jq_Primitive.INT) return (double)Reflection.getstatic_I(sf);
            if (t == jq_Primitive.LONG) return (double)Reflection.getstatic_L(sf);
            if (t == jq_Primitive.FLOAT) return (double)Reflection.getstatic_L(sf);
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.DOUBLE) return Reflection.getfield_F(obj, f);
            if (t == jq_Primitive.BYTE) return (double)Reflection.getfield_B(obj, f);
            if (t == jq_Primitive.SHORT) return (double)Reflection.getfield_S(obj, f);
            if (t == jq_Primitive.CHAR) return (double)Reflection.getfield_C(obj, f);
            if (t == jq_Primitive.INT) return (double)Reflection.getfield_I(obj, f);
            if (t == jq_Primitive.LONG) return (double)Reflection.getfield_L(obj, f);
            if (t == jq_Primitive.FLOAT) return (double)Reflection.getfield_L(obj, f);
        }
        Assert.UNREACHABLE();
        return 0F;
    }
    
    public void set(java.lang.Object obj, java.lang.Object value)
        throws java.lang.IllegalArgumentException, java.lang.IllegalAccessException
    {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t.isReferenceType()) Reflection.putstatic_A(sf, value);
            else if (t == jq_Primitive.INT) {
                int val = Convert.unwrapToInt(value);
                Reflection.putstatic_I(sf, val);
            }
            else if (t == jq_Primitive.FLOAT) {
                float val = Convert.unwrapToFloat(value);
                Reflection.putstatic_F(sf, val);
            }
            else if (t == jq_Primitive.LONG) {
                long val = Convert.unwrapToLong(value);
                Reflection.putstatic_L(sf, val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                double val = Convert.unwrapToDouble(value);
                Reflection.putstatic_D(sf, val);
            }
            else if (t == jq_Primitive.BOOLEAN) {
                boolean val = Convert.unwrapToBoolean(value);
                Reflection.putstatic_Z(sf, val);
            }
            else if (t == jq_Primitive.BYTE) {
                byte val = Convert.unwrapToByte(value);
                Reflection.putstatic_B(sf, val);
            }
            else if (t == jq_Primitive.SHORT) {
                short val = Convert.unwrapToShort(value);
                Reflection.putstatic_S(sf, val);
            }
            else if (t == jq_Primitive.CHAR) {
                char val = Convert.unwrapToChar(value);
                Reflection.putstatic_C(sf, val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t.isReferenceType()) {
                Reflection.getfield_A(obj, f);
            }
            else if (t == jq_Primitive.INT) {
                int val = Convert.unwrapToInt(value);
                Reflection.putfield_I(obj, f, val);
            }
            else if (t == jq_Primitive.FLOAT) {
                float val = Convert.unwrapToFloat(value);
                Reflection.putfield_F(obj, f, val);
            }
            else if (t == jq_Primitive.LONG) {
                long val = Convert.unwrapToLong(value);
                Reflection.putfield_L(obj, f, val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                double val = Convert.unwrapToDouble(value);
                Reflection.putfield_D(obj, f, val);
            }
            else if (t == jq_Primitive.BOOLEAN) {
                boolean val = Convert.unwrapToBoolean(value);
                Reflection.putfield_Z(obj, f, val);
            }
            else if (t == jq_Primitive.BYTE) {
                byte val = Convert.unwrapToByte(value);
                Reflection.putfield_B(obj, f, val);
            }
            else if (t == jq_Primitive.SHORT) {
                short val = Convert.unwrapToShort(value);
                Reflection.putfield_S(obj, f, val);
            }
            else if (t == jq_Primitive.CHAR) {
                char val = Convert.unwrapToChar(value);
                Reflection.putfield_C(obj, f, val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setBoolean(java.lang.Object obj, boolean val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.BOOLEAN) {
                Reflection.putstatic_Z(sf, val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.BOOLEAN) {
                Reflection.putfield_Z(obj, f, val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setByte(java.lang.Object obj, byte val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.BYTE) {
                Reflection.putstatic_B(sf, val);
            }
            else if (t == jq_Primitive.SHORT) {
                Reflection.putstatic_S(sf, (short)val);
            }
            else if (t == jq_Primitive.INT) {
                Reflection.putstatic_I(sf, (int)val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putstatic_L(sf, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putstatic_F(sf, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, (double)val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.BYTE) {
                Reflection.putfield_B(obj, f, val);
            }
            else if (t == jq_Primitive.SHORT) {
                Reflection.putfield_S(obj, f, (short)val);
            }
            else if (t == jq_Primitive.INT) {
                Reflection.putfield_I(obj, f, (int)val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putfield_L(obj, f, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putfield_F(obj, f, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, (double)val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setChar(java.lang.Object obj, char val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.CHAR) {
                Reflection.putstatic_C(sf, val);
            }
            else if (t == jq_Primitive.INT) {
                Reflection.putstatic_I(sf, (int)val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putstatic_L(sf, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putstatic_F(sf, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, (double)val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.CHAR) {
                Reflection.putfield_C(obj, f, val);
            }
            else if (t == jq_Primitive.INT) {
                Reflection.putfield_I(obj, f, (int)val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putfield_L(obj, f, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putfield_F(obj, f, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, (double)val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setShort(java.lang.Object obj, short val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.SHORT) {
                Reflection.putstatic_S(sf, val);
            }
            else if (t == jq_Primitive.INT) {
                Reflection.putstatic_I(sf, (int)val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putstatic_L(sf, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putstatic_F(sf, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, (double)val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.SHORT) {
                Reflection.putfield_S(obj, f, val);
            }
            else if (t == jq_Primitive.INT) {
                Reflection.putfield_I(obj, f, (int)val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putfield_L(obj, f, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putfield_F(obj, f, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, (double)val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setInt(java.lang.Object obj, int val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.INT) {
                Reflection.putstatic_I(sf, val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putstatic_L(sf, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putstatic_F(sf, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, (double)val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.INT) {
                Reflection.putfield_I(obj, f, val);
            }
            else if (t == jq_Primitive.LONG) {
                Reflection.putfield_L(obj, f, (long)val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putfield_F(obj, f, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, (double)val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setFloat(java.lang.Object obj, float val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.FLOAT) {
                Reflection.putstatic_F(sf, val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, (double)val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.FLOAT) {
                Reflection.putfield_F(obj, f, val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, (double)val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setLong(java.lang.Object obj, long val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.LONG) {
                Reflection.putstatic_L(sf, val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putstatic_F(sf, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, (double)val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.LONG) {
                Reflection.putfield_L(obj, f, val);
            }
            else if (t == jq_Primitive.FLOAT) {
                Reflection.putfield_F(obj, f, (float)val);
            }
            else if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, (double)val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    public void setDouble(java.lang.Object obj, double val)
        throws IllegalArgumentException, IllegalAccessException {
        jq_Field jq_f = this.jq_field;
        jq_Type t = jq_f.getType();
        if (jq_f.isStatic()) {
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_StaticField sf = (jq_StaticField)jq_f;
            if (t == jq_Primitive.DOUBLE) {
                Reflection.putstatic_D(sf, val);
            }
            else Assert.UNREACHABLE();
        } else {
            jq_Reference obj_t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(obj_t, jq_f.getDeclaringClass())) {
                throw new IllegalArgumentException();
            }
            if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_f, 2);
            if (!jq_f.isFinal()) {
                throw new IllegalAccessException();
            }
            jq_InstanceField f = (jq_InstanceField)jq_f;
            if (t == jq_Primitive.DOUBLE) {
                Reflection.putfield_D(obj, f, val);
            }
            else Assert.UNREACHABLE();
        }
    }
    
    // additional methods.
    // ONLY TO BE CALLED BY jq_Member CONSTRUCTOR!!!
    public static java.lang.reflect.Field createNewField(jq_Field jq_field) {
        Object o = new Field(jq_field);
        return (java.lang.reflect.Field)o;
    }
    
    public static void initNewField(Field o, jq_Field jq_field) {
        if (!jq.RunningNative) return;
        java.lang.String name = jq_field.getName().toString();
        o.name = name;
        java.lang.Class clazz = jq_field.getDeclaringClass().getJavaLangClassObject();
        Assert._assert(clazz != null);
        o.clazz = clazz;
        java.lang.Class type = Reflection.getJDKType(jq_field.getType());
        Assert._assert(type != null);
        o.type = type;
        int modifiers = jq_field.getAccessFlags();
        o.modifiers = modifiers;
    }
}
