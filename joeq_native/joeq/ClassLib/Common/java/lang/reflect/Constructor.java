// Constructor.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang.reflect;

import joeq.Class.jq_Class;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Type;
import joeq.ClassLib.ClassLibInterface;
import joeq.ClassLib.Common.ClassUtils;
import joeq.Main.jq;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * Constructor
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Constructor.java,v 1.14 2004/09/30 03:35:35 joewhaley Exp $
 */
public class Constructor extends AccessibleObject {

    // additional instance field.
    public final jq_Initializer jq_init;
    
    private java.lang.Class clazz;
    private java.lang.Class[] parameterTypes;
    private java.lang.Class[] exceptionTypes;
    private int modifiers;
    private int slot;
    
    private Constructor() {
        Assert.UNREACHABLE();
        this.jq_init = null;
    }
    
    private Constructor(jq_Initializer i) {
        this.jq_init = i;
    }
    private Constructor(java.lang.Class clazz,
                        java.lang.Class[] parameterTypes,
                        java.lang.Class[] exceptionTypes,
                        int modifiers,
                        int slot) {
        this.clazz = clazz;
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = exceptionTypes;
        this.modifiers = modifiers;
        this.slot = slot;
        jq_Class k = (jq_Class) ClassLibInterface.DEFAULT.getJQType(clazz);
        
        StringBuffer desc = new StringBuffer();
        desc.append('(');
        for (int i=0; i<parameterTypes.length; ++i) {
            desc.append(Reflection.getJQType(parameterTypes[i]).getDesc().toString());
        }
        desc.append(")V");
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get("<init>"), Utf8.get(desc.toString()));
        nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(k, nd);
        jq_Initializer init = (jq_Initializer) k.getDeclaredMember(nd);
        if (init == null) {
            init = (jq_Initializer) k.getOrCreateInstanceMethod(nd);
        }
        this.jq_init = init;
    }
    
    public java.lang.Object newInstance(java.lang.Object[] initargs)
        throws java.lang.InstantiationException, java.lang.IllegalAccessException,
               java.lang.IllegalArgumentException, java.lang.reflect.InvocationTargetException
    {
        jq_Initializer jq_i = this.jq_init;
        jq_Class k = jq_i.getDeclaringClass();
        if (k.isAbstract()) throw new InstantiationException();
        if (!this.isAccessible()) ClassUtils.checkCallerAccess(jq_i, 2);
        jq_Type[] argtypes = jq_i.getParamTypes();
        int nargs = initargs == null ? 0 : initargs.length;
        if (nargs != argtypes.length-1)
            throw new java.lang.IllegalArgumentException("Constructor takes "+(argtypes.length-1)+" arguments, but "+nargs+" arguments passed in");
        Object o = k.newInstance();
        Reflection.invoke(jq_i, o, initargs);
        return o;
    }
    
    // additional methods.
    // ONLY TO BE CALLED BY jq_Member CONSTRUCTOR!!!
    public static java.lang.reflect.Constructor createNewConstructor(jq_Initializer jq_init) {
        Object o = new Constructor(jq_init);
        return (java.lang.reflect.Constructor)o;
    }
    
    public static void initNewConstructor(Constructor o, jq_Initializer jq_init) {
        if (!jq.RunningNative) return;
        Assert._assert(jq_init == o.jq_init);
        java.lang.Class clazz = jq_init.getDeclaringClass().getJavaLangClassObject();
        o.clazz = clazz;
        jq_Type[] paramTypes = jq_init.getParamTypes();
        java.lang.Class[] parameterTypes = new java.lang.Class[paramTypes.length-1];
        for (int i=1; i<paramTypes.length; ++i) {
            parameterTypes[i-1] = Reflection.getJDKType(paramTypes[i]);
        }
        o.parameterTypes = parameterTypes;
        // TODO: exception types
        java.lang.Class[] exceptionTypes = new java.lang.Class[0];
        o.exceptionTypes = exceptionTypes;
        int modifiers = jq_init.getAccessFlags();
        o.modifiers = modifiers;
    }
}
