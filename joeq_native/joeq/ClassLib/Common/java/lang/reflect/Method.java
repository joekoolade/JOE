// Method.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang.reflect;

import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.ClassLib.ClassLibInterface;
import joeq.ClassLib.Common.ClassUtils;
import joeq.Main.jq;
import joeq.Memory.HeapAddress;
import joeq.Runtime.Reflection;
import joeq.Runtime.TypeCheck;
import joeq.UTF.Utf8;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * Method
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Method.java,v 1.17 2004/09/30 03:35:35 joewhaley Exp $
 */
public class Method extends AccessibleObject {

    // additional instance field.
    public final jq_Method jq_method;
    
    private java.lang.Class clazz;
    private java.lang.String name;
    private java.lang.Class[] parameterTypes;
    private java.lang.Class returnType;
    private java.lang.Class[] exceptionTypes;
    private int modifiers;
    private int slot;
    
    private Method(jq_Method m) {
        this.jq_method = m;
    }
    private Method(java.lang.Class clazz,
                   java.lang.String name,
                   java.lang.Class[] parameterTypes,
                   java.lang.Class returnType,
                   java.lang.Class[] exceptionTypes,
                   int modifiers,
                   int slot) {
        this.clazz = clazz;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.exceptionTypes = exceptionTypes;
        this.modifiers = modifiers;
        this.slot = slot;
        
        jq_Class k = (jq_Class) ClassLibInterface.DEFAULT.getJQType(clazz);
        StringBuffer desc = new StringBuffer();
        desc.append('(');
        for (int i=0; i<parameterTypes.length; ++i) {
            desc.append(Reflection.getJQType(parameterTypes[i]).getDesc().toString());
        }
        desc.append(')');
        desc.append(Reflection.getJQType(returnType).getDesc().toString());
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get(name), Utf8.get(desc.toString()));
        nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(k, nd);
        jq_Method m = (jq_Method) k.getDeclaredMember(nd);
        if (m == null) {
            if (java.lang.reflect.Modifier.isStatic(modifiers))
                m = k.getOrCreateStaticMethod(nd);
            else
                m = k.getOrCreateInstanceMethod(nd);
        }
        this.jq_method = m;
    }
    
    public java.lang.Object invoke(java.lang.Object obj,
                                   java.lang.Object[] initargs)
        throws java.lang.InstantiationException, java.lang.IllegalAccessException,
               java.lang.IllegalArgumentException, java.lang.reflect.InvocationTargetException
    {
        jq_Method jq_m = this.jq_method;
        jq_Class k = jq_m.getDeclaringClass();
        if (!jq_m.isStatic()) {
            jq_Reference t = jq_Reference.getTypeOf(obj);
            if (!TypeCheck.isAssignable(t, k))
                throw new java.lang.IllegalArgumentException(t+" is not assignable to "+k);
        }
        if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_m, 2);
        int offset;
        if (jq_m.isStatic()) {
            obj = null; offset = 0;
        } else {
            offset = 1;
        }
        jq_Type[] argtypes = jq_m.getParamTypes();
        int nargs = initargs==null ? 0 : initargs.length; 
        if (nargs != argtypes.length-offset) 
            throw new java.lang.IllegalArgumentException(); 
        if (jq_m.isStatic()) {
            k.cls_initialize();
        } else {
            jq_Reference t = jq_Reference.getTypeOf(obj);
            jq_m = t.getVirtualMethod(jq_m.getNameAndDesc());
            if (jq_m == null || jq_m.isAbstract())
                throw new java.lang.AbstractMethodError();
        }
        jq_Type retType = jq_m.getReturnType();
        if (retType.isReferenceType())
            return ((HeapAddress) Reflection.invokeA(jq_m, obj, initargs)).asObject();
        long result = Reflection.invoke(jq_m, obj, initargs);
        if (retType == jq_Primitive.VOID) return null;
        if (retType == jq_Primitive.INT) return new Integer((int)result);
        if (retType == jq_Primitive.LONG) return new Long(result);
        if (retType == jq_Primitive.FLOAT) return new Float(Float.intBitsToFloat((int)result));
        if (retType == jq_Primitive.DOUBLE) return new Double(Double.longBitsToDouble(result));
        if (retType == jq_Primitive.BOOLEAN) return Convert.getBoolean((int)result!=0);
        if (retType == jq_Primitive.BYTE) return new Byte((byte)result);
        if (retType == jq_Primitive.SHORT) return new Short((short)result);
        if (retType == jq_Primitive.CHAR) return new Character((char)result);
        Assert.UNREACHABLE(); return null;
    }
    // additional methods.
    // ONLY TO BE CALLED BY jq_Member CONSTRUCTOR!!!
    public static java.lang.reflect.Method createNewMethod(jq_Method jq_method) {
        Object o = new Method(jq_method);
        return (java.lang.reflect.Method)o;
    }
    
    public static void initNewMethod(Method o, jq_Method jq_method) {
        if (!jq.RunningNative) return;
        java.lang.String name = jq_method.getName().toString();
        o.name = name;
        java.lang.Class clazz = jq_method.getDeclaringClass().getJavaLangClassObject();
        Assert._assert(clazz != null);
        o.clazz = clazz;
        java.lang.Class returnType = jq_method.getReturnType().getJavaLangClassObject();
        Assert._assert(returnType != null);
        o.returnType = returnType;
        jq_Type[] paramTypes = jq_method.getParamTypes();
        int offset;
        if (jq_method instanceof jq_InstanceMethod)
            offset = 1;
        else
            offset = 0;
        java.lang.Class[] parameterTypes = new java.lang.Class[paramTypes.length-offset];
        for (int i=offset; i<paramTypes.length; ++i) {
            parameterTypes[i-offset] = Reflection.getJDKType(paramTypes[i]);
            Assert._assert(parameterTypes[i-offset] != null);
        }
        o.parameterTypes = parameterTypes;
        // TODO: exception types
        java.lang.Class[] exceptionTypes = new java.lang.Class[0];
        o.exceptionTypes = exceptionTypes;
        int modifiers = jq_method.getAccessFlags();
        o.modifiers = modifiers;
    }
}
