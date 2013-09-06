// ReflectionImpl.java, created Sat Dec 14  2:52:34 2002 by mcmartin
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import java.util.Set;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassInitializer;
import joeq.Class.jq_Field;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.ClassLib.ClassLibInterface;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.UTF.Utf8;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ReflectionImpl.java,v 1.15 2005/03/21 12:39:42 joewhaley Exp $
 */
public class ReflectionImpl implements Reflection.Delegate {

    public static boolean REPORT_JDK_ERRORS = false;
    
    public final jq_Reference getTypeOf(Object o) {
        if (jq.RunningNative) return jq_Reference.getTypeOf(o);
        return (jq_Reference) getJQType(o.getClass());
    }
    
    // Map between our jq_Type objects and JDK Class objects
    public final jq_Type getJQType(Class c) {
        if (jq.RunningNative) return ClassLibInterface.DEFAULT.getJQType(c);
        //if (c == null) return null;
        if (c.isPrimitive()) {
            if (c == Byte.TYPE) return jq_Primitive.BYTE;
            if (c == Character.TYPE) return jq_Primitive.CHAR;
            if (c == Double.TYPE) return jq_Primitive.DOUBLE;
            if (c == Float.TYPE) return jq_Primitive.FLOAT;
            if (c == Integer.TYPE) return jq_Primitive.INT;
            if (c == Long.TYPE) return jq_Primitive.LONG;
            if (c == Short.TYPE) return jq_Primitive.SHORT;
            if (c == Boolean.TYPE) return jq_Primitive.BOOLEAN;
            if (c == Void.TYPE) return jq_Primitive.VOID;
            Assert.UNREACHABLE(c.toString());
            return null;
        }
        String className = c.getName().replace('.','/');
        if (!className.startsWith("[")) className = "L"+className+";";
        className = joeq.ClassLib.ClassLibInterface.convertClassLibDesc(className);
        return PrimordialClassLoader.loader.getOrCreateBSType(className);
    }
    public final Class getJDKType(jq_Type c) {
        if (jq.RunningNative) return c.getJavaLangClassObject();
        //if (c == null) return null;
        if (c.getJavaLangClassObject() != null)
            return c.getJavaLangClassObject();
        if (c.isPrimitiveType()) 
            return getJDKType((jq_Primitive)c);
        else
            return getJDKType((jq_Reference)c);
    }
    public final Class getJDKType(jq_Primitive c) {
        if (jq.RunningNative) return c.getJavaLangClassObject();
        if (c.getJavaLangClassObject() != null)
            return c.getJavaLangClassObject();
        // cannot compare to jq_Primitive types here, as they may not
        // have been initialized yet.  so we compare descriptors instead.
        if (c.getDesc() == Utf8.BYTE_DESC) return Byte.TYPE;
        if (c.getDesc() == Utf8.CHAR_DESC) return Character.TYPE;
        if (c.getDesc() == Utf8.DOUBLE_DESC) return Double.TYPE;
        if (c.getDesc() == Utf8.FLOAT_DESC) return Float.TYPE;
        if (c.getDesc() == Utf8.INT_DESC) return Integer.TYPE;
        if (c.getDesc() == Utf8.LONG_DESC) return Long.TYPE;
        if (c.getDesc() == Utf8.SHORT_DESC) return Short.TYPE;
        if (c.getDesc() == Utf8.BOOLEAN_DESC) return Boolean.TYPE;
        if (c.getDesc() == Utf8.VOID_DESC) return Void.TYPE;
        Assert.UNREACHABLE(c.getName());
        return null;
    }
    public Class getJDKType(jq_Reference c) {
        if (jq.RunningNative) return c.getJavaLangClassObject();
        //if (c == null) return null;
        if (c.getJavaLangClassObject() != null)
            return c.getJavaLangClassObject();
        try {
            return Class.forName(c.getJDKName(), false, Reflection.class.getClassLoader());
            //return Class.forName(c.getJDKName(), false, c.getClassLoader());
        } catch (ClassNotFoundException x) {
            if (!c.getJDKName().startsWith("joeq.ClassLib") && !c.getJDKName().startsWith("L&"))
                SystemInterface.debugwriteln("Note: "+c.getJDKName()+" was not found in host jdk");
            return null;
        }
    }
    
    // Map between our jq_Member objects and JDK Member objects
    public final jq_Field getJQMember(Field f) {
        if (jq.RunningNative) return ClassLibInterface.DEFAULT.getJQField(f);
        jq_Class c = (jq_Class)getJQType(f.getDeclaringClass());
        //if (c == null) return null;
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get(f.getName()), getJQType(f.getType()).getDesc());
        nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(c, nd);
        jq_Field m = (jq_Field)c.getDeclaredMember(nd);
        if (m == null) {
            if (!Utf8.NO_NEW) {
                //SystemInterface.debugwriteln("Reference to jdk field "+f.toString()+" does not exist, creating "+c+"."+nd);
                if (Modifier.isStatic(f.getModifiers()))
                    m = c.getOrCreateStaticField(nd);
                else
                    m = c.getOrCreateInstanceField(nd);
            }
        }
        return m;
    }
    public final jq_Method getJQMember(Method f) {
        if (jq.RunningNative) return ClassLibInterface.DEFAULT.getJQMethod(f);
        jq_Class c = (jq_Class)getJQType(f.getDeclaringClass());
        //if (c == null) return null;
        StringBuffer desc = new StringBuffer();
        desc.append('(');
        Class[] param_types = f.getParameterTypes();
        for (int i=0; i<param_types.length; ++i) {
            desc.append(getJQType(param_types[i]).getDesc().toString());
        }
        desc.append(')');
        desc.append(getJQType(f.getReturnType()).getDesc().toString());
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get(f.getName()), Utf8.get(desc.toString()));
        nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(c, nd);
        jq_Method m = (jq_Method)c.getDeclaredMember(nd);
        if (m == null) {
            if (!Utf8.NO_NEW) {
                //SystemInterface.debugwriteln("Reference to jdk method "+f.toString()+" does not exist, creating "+c+"."+nd);
                if (Modifier.isStatic(f.getModifiers()))
                    m = c.getOrCreateStaticMethod(nd);
                else
                    m = c.getOrCreateInstanceMethod(nd);
            }
        }
        return m;
    }
    public final jq_Initializer getJQMember(Constructor f) {
        if (jq.RunningNative) return ClassLibInterface.DEFAULT.getJQInitializer(f);
        jq_Class c = (jq_Class)getJQType(f.getDeclaringClass());
        //if (c == null) return null;
        StringBuffer desc = new StringBuffer();
        desc.append('(');
        Class[] param_types = f.getParameterTypes();
        for (int i=0; i<param_types.length; ++i) {
            desc.append(getJQType(param_types[i]).getDesc().toString());
        }
        desc.append(")V");
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get("<init>"), Utf8.get(desc.toString()));
        nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(c, nd);
        jq_Initializer m = (jq_Initializer)c.getDeclaredMember(nd);
        if (m == null) {
            if (!Utf8.NO_NEW) {
                //SystemInterface.debugwriteln("Reference to jdk constructor "+f.toString()+" does not exist, creating "+c+"."+nd);
                m = (jq_Initializer)c.getOrCreateInstanceMethod(nd);
            }
        }
        return m;
    }
    public boolean USE_DECLARED_FIELDS_CACHE = true;
    private static java.util.HashMap declaredFieldsCache;
    public final Field getJDKField(Class c, String name) {
        Field[] fields = null;
        if (USE_DECLARED_FIELDS_CACHE) {
            if (declaredFieldsCache == null) declaredFieldsCache = new java.util.HashMap();
            else fields = (Field[])declaredFieldsCache.get(c);
            if (fields == null) {
                try {
                    fields = c.getDeclaredFields();
                } catch (NoClassDefFoundError x) {
                    if (REPORT_JDK_ERRORS) SystemInterface.debugwriteln("Note: "+c+" could not be loaded in host jdk");
                    return null;
                }
                declaredFieldsCache.put(c, fields);
            }
        } else {
            try {
                fields = c.getDeclaredFields();
            } catch (NoClassDefFoundError x) {
                if (REPORT_JDK_ERRORS) SystemInterface.debugwriteln("Note: "+c+" could not be loaded in host jdk");
                return null;
            }
        }
        for (int i=0; i<fields.length; ++i) {
            Field f2 = fields[i];
            if (f2.getName().equals(name)) {
                //f2.setAccessible(true);
                return f2;
            }
        }
        //jq.UNREACHABLE(c+"."+name);
        return null;
    }
    public boolean USE_DECLARED_METHODS_CACHE = true;
    private static java.util.HashMap declaredMethodsCache;
    public final Method getJDKMethod(Class c, String name, Class[] args) {
        Method[] methods = null;
        if (USE_DECLARED_METHODS_CACHE) {
            if (declaredMethodsCache == null) declaredMethodsCache = new java.util.HashMap();
            else methods = (Method[])declaredMethodsCache.get(c);
            if (methods == null) {
                try {
                    methods = c.getDeclaredMethods();
                } catch (NoClassDefFoundError x) {
                    if (REPORT_JDK_ERRORS) SystemInterface.debugwriteln("Note: "+c+" could not be loaded in host jdk");
                    return null;
                }
                declaredMethodsCache.put(c, methods);
            }
        } else {
            try {
                methods = c.getDeclaredMethods();
            } catch (NoClassDefFoundError x) {
                if (REPORT_JDK_ERRORS) SystemInterface.debugwriteln("Note: "+c+" could not be loaded in host jdk");
                return null;
            }
        }
uphere:
        for (int i=0; i<methods.length; ++i) {
            Method f2 = methods[i];
            if (f2.getName().equals(name)) {
                Class[] args2 = f2.getParameterTypes();
                if (args.length != args2.length) continue uphere;
                for (int j=0; j<args.length; ++j) {
                    if (!args[j].equals(args2[j])) continue uphere;
                }
                //f2.setAccessible(true);
                return f2;
            }
        }
        //jq.UNREACHABLE(c+"."+name+" "+args);
        return null;
    }
    public final Constructor getJDKConstructor(Class c, Class[] args) {
        Constructor[] consts;
        try {
            consts = c.getDeclaredConstructors();
        } catch (NoClassDefFoundError x) {
            if (REPORT_JDK_ERRORS) SystemInterface.debugwriteln("Note: "+c+" could not be loaded in host jdk");
            return null;
        }
uphere:
        for (int i=0; i<consts.length; ++i) {
            Constructor f2 = consts[i];
            Class[] args2 = f2.getParameterTypes();
            if (args.length != args2.length) continue uphere;
            for (int j=0; j<args.length; ++j) {
                if (!args[j].equals(args2[j])) continue uphere;
            }
            //f2.setAccessible(true);
            return f2;
        }
        //jq.UNREACHABLE(c+".<init> "+args);
        return null;
    }
    public final Member getJDKMember(jq_Member m) {
        if (jq.RunningNative) return m.getJavaLangReflectMemberObject();
        if (m.getJavaLangReflectMemberObject() != null)
            return m.getJavaLangReflectMemberObject();
        Class c = getJDKType(m.getDeclaringClass());
        if (m instanceof jq_Field) {
            Member ret = getJDKField(c, m.getName().toString());
            if (ret == null) {
                // TODO: a synthetic field, so there is no java.lang.reflect.Field object yet.
            }
            return ret;
        } else if (m instanceof jq_Initializer) {
            jq_Initializer m2 = (jq_Initializer)m;
            jq_Type[] param_types = m2.getParamTypes();
            int num_of_args = param_types.length-1; // -1 for this ptr
            Class[] args = new Class[num_of_args];
            for (int i=0; i<num_of_args; ++i) {
                args[i] = getJDKType(param_types[i+1]);
            }
            Member ret = getJDKConstructor(c, args);
            if (ret == null) {
                // TODO: a synthetic field, so there is no java.lang.reflect.Field object yet.
            }
            return ret;
        } else if (m instanceof jq_ClassInitializer) {
            return null; // <clinit> methods have no Method object
        } else {
            Assert._assert(m instanceof jq_Method);
            jq_Method m2 = (jq_Method)m;
            int offset = m2.isStatic()?0:1;
            jq_Type[] param_types = m2.getParamTypes();
            int num_of_args = param_types.length-offset;
            Class[] args = new Class[num_of_args];
            for (int i=0; i<num_of_args; ++i) {
                args[i] = getJDKType(param_types[i+offset]);
            }
            Member ret = getJDKMethod(c, m.getName().toString(), args);
            if (ret == null) {
                // TODO: a synthetic field, so there is no java.lang.reflect.Field object yet.
            }
            return ret;
        }
    }
    
    // reflective invocations.
    public void invokestatic_V(jq_StaticMethod m) throws Throwable {
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
        }
    }
    public int invokestatic_I(jq_StaticMethod m) throws Throwable {
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            return (int)Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
        } else {
            Assert.UNREACHABLE();
            return 0;
        }
    }
    public Object invokestatic_A(jq_StaticMethod m) throws Throwable {
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            return ((HeapAddress)Unsafe.invokeA(m.getDefaultCompiledVersion().getEntrypoint())).asObject();
        } else {
            Assert.UNREACHABLE();
            return null;
        }
    }
    public long invokestatic_J(jq_StaticMethod m) throws Throwable {
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            return Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
        } else {
            Assert.UNREACHABLE();
            return 0L;
        }
    }
    public void invokestatic_V(jq_StaticMethod m, Object arg1) throws Throwable {
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public void invokeinstance_V(jq_InstanceMethod m, Object dis) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public Object invokeinstance_A(jq_InstanceMethod m, Object dis) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            return ((HeapAddress)Unsafe.invokeA(m.getDefaultCompiledVersion().getEntrypoint())).asObject();
        } else {
            Assert.UNREACHABLE();
            return null;
        }
    }
    public void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public Object invokeinstance_A(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            return ((HeapAddress)Unsafe.invokeA(m.getDefaultCompiledVersion().getEntrypoint())).asObject();
        } else {
            Assert.UNREACHABLE();
            return null;
        }
    }
    public boolean invokeinstance_Z(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            return ((int)Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint())) != 0;
        } else {
            Assert.UNREACHABLE();
            return false;
        }
    }
    public void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            Unsafe.pushArgA(HeapAddress.addressOf(arg2));
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2, Object arg3) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            Unsafe.pushArgA(HeapAddress.addressOf(arg2));
            Unsafe.pushArgA(HeapAddress.addressOf(arg3));
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2, Object arg3, long arg4) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            Unsafe.pushArgA(HeapAddress.addressOf(arg2));
            Unsafe.pushArgA(HeapAddress.addressOf(arg3));
            Unsafe.pushArg((int)(arg4 >> 32)); // hi
            Unsafe.pushArg((int)arg4);         // lo
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, int arg2, long arg3, int arg4) throws Throwable {
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(dis), m.getDeclaringClass()));
        if (jq.RunningNative) {
            Assert._assert(m.getDeclaringClass().isClsInitRunning());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            Unsafe.pushArgA(HeapAddress.addressOf(arg1));
            Unsafe.pushArg(arg2);
            Unsafe.pushArg((int)(arg3 >> 32)); // hi
            Unsafe.pushArg((int)arg3);         // lo
            Unsafe.pushArg(arg4);
            Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
            return;
        } else {
            Assert.UNREACHABLE();
            return;
        }
    }
    public long invoke(jq_Method m, Object dis, Object[] args)
        throws IllegalArgumentException, InvocationTargetException
    {
        jq_Type[] params = m.getParamTypes();
        int offset;
        if (dis != null) {
            Assert._assert(!m.isStatic());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            offset = 1;
        } else {
            offset = 0;
        }
        if (args != null) {
            Assert._assert(params.length == args.length+offset);
            for (int i=0; i<args.length; ++i) {
                jq_Type c = params[i+offset];
                if (c.isAddressType()) {
                    Assert.TODO();
                } else if (c.isReferenceType()) {
                    if (args[i] != null && !TypeCheck.isAssignable(jq_Reference.getTypeOf(args[i]), c))
                        throw new IllegalArgumentException(args[i].getClass()+" is not assignable to "+c);
                    Unsafe.pushArgA(HeapAddress.addressOf(args[i]));
                } else {
                    if (c == jq_Primitive.BYTE) {
                        int v = (int)Convert.unwrapToByte(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.CHAR) {
                        int v = (int)Convert.unwrapToChar(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.DOUBLE) {
                        long v = Double.doubleToRawLongBits(Convert.unwrapToDouble(args[i]));
                        Unsafe.pushArg((int)(v >> 32)); // hi
                        Unsafe.pushArg((int)v);         // lo
                    } else if (c == jq_Primitive.FLOAT) {
                        int v = Float.floatToRawIntBits(Convert.unwrapToFloat(args[i]));
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.INT) {
                        int v = Convert.unwrapToInt(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.LONG) {
                        long v = Convert.unwrapToLong(args[i]);
                        Unsafe.pushArg((int)(v >> 32)); // hi
                        Unsafe.pushArg((int)v);         // lo
                    } else if (c == jq_Primitive.SHORT) {
                        int v = (int)Convert.unwrapToShort(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.BOOLEAN) {
                        int v = Convert.unwrapToBoolean(args[i])?1:0;
                        Unsafe.pushArg(v);
                    } else Assert.UNREACHABLE(c.toString());
                }
            }
        } else {
            Assert._assert(params.length == offset);
        }
        try {
            return Unsafe.invoke(m.getDefaultCompiledVersion().getEntrypoint());
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
    public Address invokeA(jq_Method m, Object dis, Object[] args)
        throws IllegalArgumentException, InvocationTargetException
    {
        jq_Type[] params = m.getParamTypes();
        int offset;
        if (dis != null) {
            Assert._assert(!m.isStatic());
            Unsafe.pushArgA(HeapAddress.addressOf(dis));
            offset = 1;
        } else {
            offset = 0;
        }
        if (args != null) {
            Assert._assert(params.length == args.length+offset);
            for (int i=0; i<args.length; ++i) {
                jq_Type c = params[i+offset];
                if (c.isAddressType()) {
                    Assert.TODO();
                } else if (c.isReferenceType()) {
                    if (args[i] != null && !TypeCheck.isAssignable(jq_Reference.getTypeOf(args[i]), c))
                        throw new IllegalArgumentException(args[i].getClass()+" is not assignable to "+c);
                    Unsafe.pushArgA(HeapAddress.addressOf(args[i]));
                } else {
                    if (c == jq_Primitive.BYTE) {
                        int v = (int)Convert.unwrapToByte(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.CHAR) {
                        int v = (int)Convert.unwrapToChar(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.DOUBLE) {
                        long v = Double.doubleToRawLongBits(Convert.unwrapToDouble(args[i]));
                        Unsafe.pushArg((int)(v >> 32)); // hi
                        Unsafe.pushArg((int)v);         // lo
                    } else if (c == jq_Primitive.FLOAT) {
                        int v = Float.floatToRawIntBits(Convert.unwrapToFloat(args[i]));
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.INT) {
                        int v = Convert.unwrapToInt(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.LONG) {
                        long v = Convert.unwrapToLong(args[i]);
                        Unsafe.pushArg((int)(v >> 32)); // hi
                        Unsafe.pushArg((int)v);         // lo
                    } else if (c == jq_Primitive.SHORT) {
                        int v = (int)Convert.unwrapToShort(args[i]);
                        Unsafe.pushArg(v);
                    } else if (c == jq_Primitive.BOOLEAN) {
                        int v = Convert.unwrapToBoolean(args[i])?1:0;
                        Unsafe.pushArg(v);
                    } else Assert.UNREACHABLE(c.toString());
                }
            }
        } else {
            Assert._assert(params.length == offset);
        }
        try {
            return Unsafe.invokeA(m.getDefaultCompiledVersion().getEntrypoint());
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }
    
    public int getfield_I(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.INT || f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0;
            return ((Integer)q).intValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return HeapAddress.addressOf(o).offset(f.getOffset()).peek4();
    }
    public long getfield_L(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.LONG || f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0L;
            return ((Long)q).longValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return HeapAddress.addressOf(o).offset(f.getOffset()).peek8();
    }
    public float getfield_F(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0f;
            return ((Float)q).floatValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return Float.intBitsToFloat(getfield_I(o, f));
    }
    public double getfield_D(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0.;
            return ((Double)q).doubleValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return Double.longBitsToDouble(getfield_L(o, f));
    }
    public Object getfield_A(Object o, jq_InstanceField f) {
        Assert._assert(f.getType().isReferenceType());
        Assert._assert(!f.getType().isAddressType());
        if (!jq.RunningNative) return Reflection.obj_trav.getInstanceFieldValue(o, f);
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return ((HeapAddress) HeapAddress.addressOf(o).offset(f.getOffset()).peek()).asObject();
    }
    public Address getfield_P(Object o, jq_InstanceField f) {
        Assert._assert(f.getType().isAddressType());
        if (!jq.RunningNative) return (Address)Reflection.obj_trav.getInstanceFieldValue(o, f);
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return HeapAddress.addressOf(o).offset(f.getOffset()).peek();
    }
    public byte getfield_B(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.BYTE);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0;
            return ((Byte)q).byteValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return HeapAddress.addressOf(o).offset(f.getOffset()).peek1();
    }
    public char getfield_C(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.CHAR);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0;
            return ((Character)q).charValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return (char)HeapAddress.addressOf(o).offset(f.getOffset()).peek4();
    }
    public short getfield_S(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.SHORT);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return 0;
            return ((Short)q).shortValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return HeapAddress.addressOf(o).offset(f.getOffset()).peek2();
    }
    public boolean getfield_Z(Object o, jq_InstanceField f) {
        Assert._assert(f.getType() == jq_Primitive.BOOLEAN);
        if (!jq.RunningNative) {
            Object q = Reflection.obj_trav.getInstanceFieldValue(o, f);
            if (q == null) return false;
            return ((Boolean)q).booleanValue();
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        return HeapAddress.addressOf(o).offset(f.getOffset()).peek1()!=0;
    }
    public Object getfield(Object o, jq_InstanceField f) {
        if (!jq.RunningNative) return Reflection.obj_trav.getInstanceFieldValue(o, f);
        jq_Type t = f.getType();
        if (t.isReferenceType()) return getfield_A(o, f);
        if (t == jq_Primitive.INT) return new Integer(getfield_I(o, f));
        if (t == jq_Primitive.FLOAT) return new Float(getfield_F(o, f));
        if (t == jq_Primitive.LONG) return new Long(getfield_L(o, f));
        if (t == jq_Primitive.DOUBLE) return new Double(getfield_D(o, f));
        if (t == jq_Primitive.BYTE) return new Byte(getfield_B(o, f));
        if (t == jq_Primitive.CHAR) return new Character(getfield_C(o, f));
        if (t == jq_Primitive.SHORT) return new Short(getfield_S(o, f));
        if (t == jq_Primitive.BOOLEAN) return Convert.getBoolean(getfield_Z(o, f));
        Assert.UNREACHABLE();
        return null;
    }
    public void putfield_I(Object o, jq_InstanceField f, int v) {
        Assert._assert(f.getType() == jq_Primitive.INT || f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Integer(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke4(v);
    }
    public void putfield_L(Object o, jq_InstanceField f, long v) {
        Assert._assert(f.getType() == jq_Primitive.LONG || f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Long(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke8(v);
    }
    public void putfield_F(Object o, jq_InstanceField f, float v) {
        Assert._assert(f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Float(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        putfield_I(o, f, Float.floatToRawIntBits(v));
    }
    public void putfield_D(Object o, jq_InstanceField f, double v) {
        Assert._assert(f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Double(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        putfield_L(o, f, Double.doubleToRawLongBits(v));
    }
    public void putfield_A(Object o, jq_InstanceField f, Object v) {
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, v);
            return;
        }
        Assert._assert(v == null || TypeCheck.isAssignable(jq_Reference.getTypeOf(v), f.getType()));
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke(HeapAddress.addressOf(v));
    }
    public void putfield_P(Object o, jq_InstanceField f, Address v) {
        Assert._assert(f.getType().isAddressType());
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, v);
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke(v);
    }
    public void putfield_B(Object o, jq_InstanceField f, byte v) {
        Assert._assert(f.getType() == jq_Primitive.BYTE);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Byte(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke1(v);
    }
    public void putfield_C(Object o, jq_InstanceField f, char v) {
        Assert._assert(f.getType() == jq_Primitive.CHAR);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Character(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke2((short)((v<<16)>>16));
    }
    public void putfield_S(Object o, jq_InstanceField f, short v) {
        Assert._assert(f.getType() == jq_Primitive.SHORT);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, new Short(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke2(v);
    }
    public void putfield_Z(Object o, jq_InstanceField f, boolean v) {
        Assert._assert(f.getType() == jq_Primitive.BOOLEAN);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putInstanceFieldValue(o, f, Convert.getBoolean(v));
            return;
        }
        Assert._assert(TypeCheck.isAssignable(jq_Reference.getTypeOf(o), f.getDeclaringClass()));
        HeapAddress.addressOf(o).offset(f.getOffset()).poke1(v?(byte)1:(byte)0);
    }
    
    public int getstatic_I(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.INT || f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0;
            return ((Integer)o).intValue();
        }
        return f.getAddress().peek4();
    }
    public long getstatic_L(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.LONG || f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0L;
            return ((Long)o).longValue();
        }
        return f.getAddress().peek8();
    }
    public float getstatic_F(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0L;
            return ((Float)o).floatValue();
        }
        return Float.intBitsToFloat(getstatic_I(f));
    }
    public double getstatic_D(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0L;
            return ((Double)o).doubleValue();
        }
        return Double.longBitsToDouble(getstatic_L(f));
    }
    public Object getstatic_A(jq_StaticField f) {
        Assert._assert(f.getType().isReferenceType() && !f.getType().isAddressType());
        if (!jq.RunningNative) return Reflection.obj_trav.getStaticFieldValue(f);
        return ((HeapAddress) f.getAddress().peek()).asObject();
    }
    public Address getstatic_P(jq_StaticField f) {
        Assert._assert(f.getType().isAddressType());
        if (!jq.RunningNative) {
            Address a = (Address)Reflection.obj_trav.getStaticFieldValue(f);
            //if (a == null) return HeapAddress.getNull();
            return a;
        }
        return f.getAddress().peek();
    }
    public boolean getstatic_Z(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.BOOLEAN);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return false;
            return ((Boolean)o).booleanValue();
        }
        return f.getAddress().peek4()!=0;
    }
    public byte getstatic_B(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.BYTE);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0;
            return ((Byte)o).byteValue();
        }
        return f.getAddress().peek1();
    }
    public short getstatic_S(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.SHORT);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0;
            return ((Short)o).shortValue();
        }
        return f.getAddress().peek2();
    }
    public char getstatic_C(jq_StaticField f) {
        Assert._assert(f.getType() == jq_Primitive.CHAR);
        if (!jq.RunningNative) {
            Object o = Reflection.obj_trav.getStaticFieldValue(f);
            if (o == null) return 0;
            return ((Character)o).charValue();
        }
        return (char)f.getAddress().peek4();
    }
    public void putstatic_I(jq_StaticField f, int v) {
        Assert._assert(f.getType() == jq_Primitive.INT);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Integer(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, v);
    }
    public void putstatic_L(jq_StaticField f, long v) {
        Assert._assert(f.getType() == jq_Primitive.LONG);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Long(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, v);
    }
    public void putstatic_F(jq_StaticField f, float v) {
        Assert._assert(f.getType() == jq_Primitive.FLOAT);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Float(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, v);
    }
    public void putstatic_D(jq_StaticField f, double v) {
        Assert._assert(f.getType() == jq_Primitive.DOUBLE);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Double(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, v);
    }
    public void putstatic_A(jq_StaticField f, Object v) {
        Assert._assert(v == null || TypeCheck.isAssignable(jq_Reference.getTypeOf(v), f.getType()));
        Assert._assert(!f.getType().isAddressType());
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, v);
            return;
        }
        f.getDeclaringClass().setStaticData(f, v);
    }
    public void putstatic_P(jq_StaticField f, Address v) {
        Assert._assert(f.getType().isAddressType());
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, v);
            return;
        }
        f.getDeclaringClass().setStaticData(f, v);
    }
    public void putstatic_Z(jq_StaticField f, boolean v) {
        Assert._assert(f.getType() == jq_Primitive.BOOLEAN);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, Convert.getBoolean(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, v?1:0);
    }
    public void putstatic_B(jq_StaticField f, byte v) {
        Assert._assert(f.getType() == jq_Primitive.BYTE);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Byte(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, (int)v);
    }
    public void putstatic_S(jq_StaticField f, short v) {
        Assert._assert(f.getType() == jq_Primitive.SHORT);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Short(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, (int)v);
    }
    public void putstatic_C(jq_StaticField f, char v) {
        Assert._assert(f.getType() == jq_Primitive.CHAR);
        if (!jq.RunningNative) {
            Reflection.obj_trav.putStaticFieldValue(f, new Character(v));
            return;
        }
        f.getDeclaringClass().setStaticData(f, (int)v);
    }
    
    public int arraylength(Object o) {
        Assert._assert(getTypeOf(o).isArrayType());
        if (!jq.RunningNative) return Array.getLength(o);
        return HeapAddress.addressOf(o).offset(joeq.Allocator.ObjectLayout.ARRAY_LENGTH_OFFSET).peek4();
    }
    public Object arrayload_A(Object[] o, int i) {
        if (!jq.RunningNative) return Reflection.obj_trav.mapValue(o[i]);
        return o[i];
    }
    public Address arrayload_R(Address[] o, int i) {
        return o[i];
    }

    public void registerNullStaticFields(Set s) {
        s.add(_declaredFieldsCache);
        s.add(_declaredMethodsCache);
    }

    public void initialize() {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/ReflectionImpl;");
        _declaredFieldsCache = _class.getOrCreateStaticField("declaredFieldsCache", "Ljava/util/HashMap;");
        _declaredMethodsCache = _class.getOrCreateStaticField("declaredMethodsCache", "Ljava/util/HashMap;");
    }
    
    public static jq_Class _class;
    public static jq_StaticField _declaredFieldsCache;
    public static jq_StaticField _declaredMethodsCache;
}
