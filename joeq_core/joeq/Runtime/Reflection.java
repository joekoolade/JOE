// Reflection.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.UTF.Utf8;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Reflection.java,v 1.46 2005/04/29 07:38:59 joewhaley Exp $
 */
public abstract class Reflection {

    public static ObjectTraverser obj_trav;
    public static boolean USE_DECLARED_FIELDS_CACHE = true;

    public static final jq_Reference getTypeOf(Object o) {
        return _delegate.getTypeOf(o);
    }
    public static final jq_Type getJQType(Class c) {
        return _delegate.getJQType(c);
    }
    public static final Class getJDKType(jq_Type c) {
        return _delegate.getJDKType(c);
    }
    public static final Class getJDKType(jq_Primitive c) {
        return _delegate.getJDKType(c);
    }
    public static Class getJDKType(jq_Reference c) {
        return _delegate.getJDKType(c);
    }
    public static final jq_Field getJQMember(Field f) {
        return _delegate.getJQMember(f);
    }
    public static final jq_Method getJQMember(Method f) {
        return _delegate.getJQMember(f);
    }
    public static final jq_Initializer getJQMember(Constructor f) {
        return _delegate.getJQMember(f);
    }
    public static final Field getJDKField(Class c, String name) {
        return _delegate.getJDKField(c, name);
    }
    public static final Method getJDKMethod(Class c, String name, Class[] args) {
        return _delegate.getJDKMethod(c, name, args);
    }
    public static final Constructor getJDKConstructor(Class c, Class[] args) {
        return _delegate.getJDKConstructor(c, args);
    }
    public static final Member getJDKMember(jq_Member m) {
        return _delegate.getJDKMember(m);
    }

    /**
     * Utility function to extract the argument types from a method descriptor.
     * 
     * @param desc
     * @return array of argument types
     */
    public static Class[] getArgTypesFromDesc(Utf8 desc) {
        Utf8.MethodDescriptorIterator i = desc.getParamDescriptors();
        // count them up
        int num = 0;
        while (i.hasNext()) { i.nextUtf8(); ++num; }
        // get them for real
        Class[] param_types = new Class[num];
        i = desc.getParamDescriptors();
        for (int j=0; j<num; ++j) {
            Utf8 pd = i.nextUtf8();
            jq_Type t = PrimordialClassLoader.loader.getOrCreateBSType(pd);
            param_types[j] = getJDKType(t);
        }
        //Utf8 rd = i.getReturnDescriptor();
        return param_types;
    }
    
    /* Reflective invocations.  Unreachable unless running native. */
    public static void invokestatic_V(jq_StaticMethod m) throws Throwable {
        _delegate.invokestatic_V(m);
    }
    public static int invokestatic_I(jq_StaticMethod m) throws Throwable {
        return _delegate.invokestatic_I(m);
    }
    public static Object invokestatic_A(jq_StaticMethod m) throws Throwable {
        return _delegate.invokestatic_A(m);
    }
    public static long invokestatic_J(jq_StaticMethod m) throws Throwable {
        return _delegate.invokestatic_J(m);
    }
    public static void invokestatic_V(jq_StaticMethod m, Object arg1) throws Throwable {
        _delegate.invokestatic_V(m, arg1);
    }
    public static void invokeinstance_V(jq_InstanceMethod m, Object dis) throws Throwable {
        _delegate.invokeinstance_V(m, dis);
    }
    public static Object invokeinstance_A(jq_InstanceMethod m, Object dis) throws Throwable {
        return _delegate.invokeinstance_A(m, dis);
    }
    public static void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable {
        _delegate.invokeinstance_V(m, dis, arg1);
    }
    public static Object invokeinstance_A(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable {
        return _delegate.invokeinstance_A(m, dis, arg1);
    }
    public static boolean invokeinstance_Z(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable {
        return _delegate.invokeinstance_Z(m, dis, arg1);
    }
    public static void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2) throws Throwable {
        _delegate.invokeinstance_V(m, dis, arg1, arg2);
    }
    public static void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2, Object arg3) throws Throwable {
        _delegate.invokeinstance_V(m, dis, arg1, arg2, arg3);
    }
    public static void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2, Object arg3, long arg4) throws Throwable {
        _delegate.invokeinstance_V(m, dis, arg1, arg2, arg3, arg4);
    }
    public static void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, int arg2, long arg3, int arg4) throws Throwable {
        _delegate.invokeinstance_V(m, dis, arg1, arg2, arg3, arg4);
    }
    public static long invoke(jq_Method m, Object dis, Object[] args)
        throws IllegalArgumentException, InvocationTargetException
    {
        return _delegate.invoke(m, dis, args);
    }
    public static Address invokeA(jq_Method m, Object dis, Object[] args) 
        throws IllegalArgumentException, InvocationTargetException
    {
        return _delegate.invokeA(m, dis, args);
    }


    public static int getfield_I(Object o, jq_InstanceField f) {
        return _delegate.getfield_I(o, f);
    }
    public static long getfield_L(Object o, jq_InstanceField f) {
        return _delegate.getfield_L(o, f);
    }
    public static float getfield_F(Object o, jq_InstanceField f) {
        return _delegate.getfield_F(o, f);
    }
    public static double getfield_D(Object o, jq_InstanceField f) {
        return _delegate.getfield_D(o, f);
    }
    public static Object getfield_A(Object o, jq_InstanceField f) {
        return _delegate.getfield_A(o, f);
    }
    public static Address getfield_P(Object o, jq_InstanceField f) {
        return _delegate.getfield_P(o, f);
    }
    public static byte getfield_B(Object o, jq_InstanceField f) {
        return _delegate.getfield_B(o, f);
    }
    public static char getfield_C(Object o, jq_InstanceField f) {
        return _delegate.getfield_C(o, f);
    }
    public static short getfield_S(Object o, jq_InstanceField f) {
        return _delegate.getfield_S(o, f);
    }
    public static boolean getfield_Z(Object o, jq_InstanceField f) {
        return _delegate.getfield_Z(o, f);
    }
    public static Object getfield(Object o, jq_InstanceField f) {
        return _delegate.getfield(o, f);
    }
    public static void putfield_I(Object o, jq_InstanceField f, int v) {
        _delegate.putfield_I(o, f, v);
    }
    public static void putfield_L(Object o, jq_InstanceField f, long v) {
        _delegate.putfield_L(o, f, v);
    }
    public static void putfield_F(Object o, jq_InstanceField f, float v) {
        _delegate.putfield_F(o, f, v);
    }
    public static void putfield_D(Object o, jq_InstanceField f, double v) {
        _delegate.putfield_D(o, f, v);
    }
    public static void putfield_A(Object o, jq_InstanceField f, Object v) {
        _delegate.putfield_A(o, f, v);
    }
    public static void putfield_P(Object o, jq_InstanceField f, Address v) {
        _delegate.putfield_P(o, f, v);
    }
    public static void putfield_B(Object o, jq_InstanceField f, byte v) {
        _delegate.putfield_B(o, f, v);
    }
    public static void putfield_C(Object o, jq_InstanceField f, char v) {
        _delegate.putfield_C(o, f, v);
    }
    public static void putfield_S(Object o, jq_InstanceField f, short v) {
        _delegate.putfield_S(o, f, v);
    }
    public static void putfield_Z(Object o, jq_InstanceField f, boolean v) {
        _delegate.putfield_Z(o, f, v);
    }
    public static int getstatic_I(jq_StaticField f) {
        return _delegate.getstatic_I(f);
    }
    public static long getstatic_L(jq_StaticField f) {
        return _delegate.getstatic_L(f);
    }
    public static float getstatic_F(jq_StaticField f) {
        return _delegate.getstatic_F(f);
    }
    public static double getstatic_D(jq_StaticField f) {
        return _delegate.getstatic_D(f);
    }
    public static Object getstatic_A(jq_StaticField f) {
        return _delegate.getstatic_A(f);
    }
    public static Address getstatic_P(jq_StaticField f) {
        return _delegate.getstatic_P(f);
    }
    public static boolean getstatic_Z(jq_StaticField f) {
        return _delegate.getstatic_Z(f);
    }
    public static byte getstatic_B(jq_StaticField f) {
        return _delegate.getstatic_B(f);
    }
    public static short getstatic_S(jq_StaticField f) {
        return _delegate.getstatic_S(f);
    }
    public static char getstatic_C(jq_StaticField f) {
        return _delegate.getstatic_C(f);
    }
    public static void putstatic_I(jq_StaticField f, int v) {
        _delegate.putstatic_I(f, v);
    }
    public static void putstatic_L(jq_StaticField f, long v) {
        _delegate.putstatic_L(f, v);
    }
    public static void putstatic_F(jq_StaticField f, float v) {
        _delegate.putstatic_F(f, v);
    }
    public static void putstatic_D(jq_StaticField f, double v) {
        _delegate.putstatic_D(f, v);
    }
    public static void putstatic_A(jq_StaticField f, Object v) {
        _delegate.putstatic_A(f, v);
    }
    public static void putstatic_P(jq_StaticField f, Address v) {
        _delegate.putstatic_P(f, v);
    }
    public static void putstatic_Z(jq_StaticField f, boolean v) {
        _delegate.putstatic_Z(f, v);
    }
    public static void putstatic_B(jq_StaticField f, byte v) {
        _delegate.putstatic_B(f, v);
    }
    public static void putstatic_S(jq_StaticField f, short v) {
        _delegate.putstatic_S(f, v);
    }
    public static void putstatic_C(jq_StaticField f, char v) {
        _delegate.putstatic_C(f, v);
    }
    public static int arraylength(Object o) {
        return _delegate.arraylength(o);
    }
    public static Object arrayload_A(Object[] o, int i) {
        return _delegate.arrayload_A(o, i);
    }
    public static Address arrayload_R(Address[] o, int i) {
        return _delegate.arrayload_R(o, i);
    }

    public static void registerNullStaticFields (Set s) {
        _delegate.registerNullStaticFields(s);
    }

    public static jq_Class _class; 
    public static jq_StaticField _obj_trav; 

    static interface Delegate {
        jq_Reference getTypeOf(Object o);
        jq_Type getJQType(Class c);
        Class getJDKType(jq_Type c);
        Class getJDKType(jq_Primitive c);
        Class getJDKType(jq_Reference c);
        jq_Field getJQMember(Field f);
        jq_Method getJQMember(Method f);
        jq_Initializer getJQMember(Constructor f);
        Field getJDKField(Class c, String name);
        Method getJDKMethod(Class c, String name, Class[] args);
        Constructor getJDKConstructor(Class c, Class[] args);
        Member getJDKMember(jq_Member m);
        void invokestatic_V(jq_StaticMethod m) throws Throwable;
        int invokestatic_I(jq_StaticMethod m) throws Throwable;
        Object invokestatic_A(jq_StaticMethod m) throws Throwable;
        long invokestatic_J(jq_StaticMethod m) throws Throwable;
        void invokestatic_V(jq_StaticMethod m, Object arg1) throws Throwable;
        void invokeinstance_V(jq_InstanceMethod m, Object dis) throws Throwable;
        Object invokeinstance_A(jq_InstanceMethod m, Object dis) throws Throwable;
        void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable;
        Object invokeinstance_A(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable;
        boolean invokeinstance_Z(jq_InstanceMethod m, Object dis, Object arg1) throws Throwable;
        void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2) throws Throwable;
        void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2, Object arg3) throws Throwable;
        void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, Object arg2, Object arg3, long arg4) throws Throwable;
        void invokeinstance_V(jq_InstanceMethod m, Object dis, Object arg1, int arg2, long arg3, int arg4) throws Throwable;
        long invoke(jq_Method m, Object dis, Object[] args) throws IllegalArgumentException, InvocationTargetException;
        Address invokeA(jq_Method m, Object dis, Object[] args) throws IllegalArgumentException, InvocationTargetException;
        int getfield_I(Object o, jq_InstanceField f);
        long getfield_L(Object o, jq_InstanceField f);
        float getfield_F(Object o, jq_InstanceField f);
        double getfield_D(Object o, jq_InstanceField f);
        Object getfield_A(Object o, jq_InstanceField f);
        Address getfield_P(Object o, jq_InstanceField f);
        byte getfield_B(Object o, jq_InstanceField f);
        char getfield_C(Object o, jq_InstanceField f);
        short getfield_S(Object o, jq_InstanceField f);
        boolean getfield_Z(Object o, jq_InstanceField f);
        Object getfield(Object o, jq_InstanceField f);
        void putfield_I(Object o, jq_InstanceField f, int v);
        void putfield_L(Object o, jq_InstanceField f, long v);
        void putfield_F(Object o, jq_InstanceField f, float v);
        void putfield_D(Object o, jq_InstanceField f, double v);
        void putfield_A(Object o, jq_InstanceField f, Object v);
        void putfield_P(Object o, jq_InstanceField f, Address v);
        void putfield_B(Object o, jq_InstanceField f, byte v);
        void putfield_C(Object o, jq_InstanceField f, char v);
        void putfield_S(Object o, jq_InstanceField f, short v);
        void putfield_Z(Object o, jq_InstanceField f, boolean v);
        int getstatic_I(jq_StaticField f);
        long getstatic_L(jq_StaticField f);
        float getstatic_F(jq_StaticField f);
        double getstatic_D(jq_StaticField f);
        Object getstatic_A(jq_StaticField f);
        Address getstatic_P(jq_StaticField f);
        boolean getstatic_Z(jq_StaticField f);
        byte getstatic_B(jq_StaticField f);
        short getstatic_S(jq_StaticField f);
        char getstatic_C(jq_StaticField f);
        void putstatic_I(jq_StaticField f, int v);
        void putstatic_L(jq_StaticField f, long v);
        void putstatic_F(jq_StaticField f, float v);
        void putstatic_D(jq_StaticField f, double v);
        void putstatic_A(jq_StaticField f, Object v);
        void putstatic_P(jq_StaticField f, Address v);
        void putstatic_Z(jq_StaticField f, boolean v);
        void putstatic_B(jq_StaticField f, byte v);
        void putstatic_S(jq_StaticField f, short v);
        void putstatic_C(jq_StaticField f, char v);
        int arraylength(Object o);
        Object arrayload_A(Object[] o, int i);
        Address arrayload_R(Address[] o, int i);
        void registerNullStaticFields(Set h);
        void initialize();
    }

    private static Delegate _delegate;
    static {
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Runtime.ReflectionImpl");
        }
        if (_delegate == null) {
            _delegate = new joeq.Runtime.BasicReflectionImpl();
        }

        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/Reflection;");
        _obj_trav = _class.getOrCreateStaticField("obj_trav", "Ljoeq/Runtime/ObjectTraverser;");
        _delegate.initialize();
    }

    private static Delegate attemptDelegate(String s) {
        String type = "reflection delegate";
        try {
            Class c = Class.forName(s);
            return (Delegate)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            //System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            //System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            //System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }
}
