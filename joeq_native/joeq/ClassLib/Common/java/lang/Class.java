// Class.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_ClassInitializer;
import joeq.Class.jq_Field;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.ClassLib.Common.ClassUtils;
import joeq.Runtime.Reflection;
import joeq.Runtime.TypeCheck;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * Class
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Class.java,v 1.17 2004/09/30 03:35:32 joewhaley Exp $
 */
public class Class {
    
    // additional instance fields.
    public final jq_Type jq_type;
    private java.lang.Object[] signers;
    private java.security.ProtectionDomain protection_domain;
    
    private Class(jq_Type t) {
        this.jq_type = t;
        //joeq.Runtime.SystemInterface.debugmsg("Created new Class object for "+t.getName());
    }
    
    // native method implementations.
    private static void registerNatives() { }
    private static Class forName0(java.lang.String name, boolean initialize,
                                  ClassLoader loader)
        throws java.lang.ClassNotFoundException
    {
        Class k = loader.loadClass(name);
        if (initialize) {
            jq_Type t = k.jq_type;
            Assert._assert(t.isLoaded());
            t.cls_initialize();
        }
        return k;
    }

    private java.lang.Object newInstance0()
        throws java.lang.InstantiationException, java.lang.IllegalAccessException
    {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType())
            throw new java.lang.InstantiationException(jq_type.getDesc()+" is not a class type");
        jq_Class jq_class = (jq_Class)jq_type;
        jq_class.load();
        if (jq_class.isAbstract())
            throw new java.lang.InstantiationException("cannot instantiate abstract "+this);
        jq_Initializer i = jq_class.getInitializer(new jq_NameAndDesc(Utf8.get("<init>"), Utf8.get("()V")));
        if (i == null)
            throw new java.lang.InstantiationException("no empty arg initializer in "+this);
        ClassUtils.checkCallerAccess(i, 3);
        jq_class.cls_initialize(); 
        java.lang.Object o = jq_class.newInstance();
        try {
            Reflection.invokeinstance_V(i, o);
        } catch (java.lang.Error x) {
            throw x;
        } catch (java.lang.Throwable x) {
            throw new java.lang.ExceptionInInitializerError(x);
        }
        return o;
    }

    public boolean isInstance(java.lang.Object obj) {
        if (obj == null) return false;
        jq_Reference t = jq_Reference.getTypeOf(obj);
        jq_Type jq_type = this.jq_type;
        jq_type.prepare();
        return TypeCheck.isAssignable(t, jq_type);
    }
    
    public boolean isAssignableFrom(Class cls) {
        jq_Type jq_type = this.jq_type;
        jq_type.prepare();
        jq_Type cls_jq_type = cls.jq_type;
        cls_jq_type.prepare();
        return TypeCheck.isAssignable(cls_jq_type, jq_type);
    }
    
    public boolean isInterface() {
        jq_Type jq_type = this.jq_type;
        jq_type.load();
        return jq_type.isClassType() && ((jq_Class)jq_type).isInterface();
    }
    
    public boolean isArray() {
        jq_Type jq_type = this.jq_type;
        return jq_type.isArrayType();
    }
    
    public boolean isPrimitive() {
        jq_Type jq_type = this.jq_type;
        return jq_type.isPrimitiveType();
    }
    
    public java.lang.String getName() {
        jq_Type jq_type = this.jq_type;
        if (jq_type.isArrayType()) return jq_type.getDesc().toString().replace('/','.');
        else return jq_type.getName().toString();
    }
    
    java.lang.ClassLoader getClassLoader0() {
        jq_Type jq_type = this.jq_type;
        return (java.lang.ClassLoader)jq_type.getClassLoader();
    }
    
    public java.lang.Class getSuperclass() {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType()) return null;
        jq_Class k = (jq_Class)jq_type;
        k.prepare();
        if (k.getSuperclass() == null) return null;
        return Reflection.getJDKType(k.getSuperclass());
    }
    
    public java.lang.Class[] getInterfaces() {
        jq_Type jq_type = this.jq_type;
        if (jq_type.isPrimitiveType()) return new java.lang.Class[0];
        jq_Class[] ins;
        jq_type.load();
        if (jq_type.isArrayType()) ins = jq_Array.array_interfaces;
        else ins = ((jq_Class)jq_type).getDeclaredInterfaces();
        java.lang.Class[] c = new java.lang.Class[ins.length];
        for (int i=0; i<ins.length; ++i) {
            c[i] = Reflection.getJDKType(ins[i]);
        }
        return c;
    }
    
    public java.lang.Class getComponentType() {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isArrayType()) return null;
        return Reflection.getJDKType(((jq_Array)jq_type).getElementType());
    }
    
    public int getModifiers() {
        jq_Type jq_type = this.jq_type;
        jq_type.load();
        if (jq_type.isPrimitiveType()) return jq_ClassFileConstants.ACC_PUBLIC | jq_ClassFileConstants.ACC_FINAL;
        if (jq_type.isArrayType()) return Reflection.getJDKType(((jq_Array)jq_type).getElementType()).getModifiers() | jq_ClassFileConstants.ACC_FINAL & ~jq_ClassFileConstants.ACC_INTERFACE;
        return (int)((jq_Class)jq_type).getAccessFlags();
    }

    public java.lang.Object[] getSigners() {
        // TODO: correct handling of signers
        return this.signers;
    }
    void setSigners(java.lang.Object[] signers) {
        // TODO: correct handling of signers
        this.signers = signers;
    }
    
    public java.lang.Class getDeclaringClass() {
        // TODO: handling of declaring class
        return null;
    }
    
    private java.security.ProtectionDomain getProtectionDomain0() {
        // TODO: correct handling of ProtectionDomain
        return this.protection_domain;
    }
    void setProtectionDomain0(java.security.ProtectionDomain pd) {
        // TODO: correct handling of ProtectionDomain
        this.protection_domain = pd;
    }

    static java.lang.Class getPrimitiveClass(java.lang.String name) {
        if (name.equals("int")) return Reflection.getJDKType(jq_Primitive.INT);
        if (name.equals("float")) return Reflection.getJDKType(jq_Primitive.FLOAT);
        if (name.equals("long")) return Reflection.getJDKType(jq_Primitive.LONG);
        if (name.equals("double")) return Reflection.getJDKType(jq_Primitive.DOUBLE);
        if (name.equals("boolean")) return Reflection.getJDKType(jq_Primitive.BOOLEAN);
        if (name.equals("byte")) return Reflection.getJDKType(jq_Primitive.BYTE);
        if (name.equals("char")) return Reflection.getJDKType(jq_Primitive.CHAR);
        if (name.equals("short")) return Reflection.getJDKType(jq_Primitive.SHORT);
        if (name.equals("void")) return Reflection.getJDKType(jq_Primitive.VOID);
        throw new java.lang.InternalError("no such primitive type: "+name);
    }
    
    private java.lang.reflect.Field[] getFields0(int which) {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType()) return new java.lang.reflect.Field[0];
        jq_Class c = (jq_Class)jq_type;
        c.load();
        if (which == java.lang.reflect.Member.DECLARED) {
            jq_StaticField[] sfs = c.getDeclaredStaticFields();
            jq_InstanceField[] ifs = c.getDeclaredInstanceFields();
            int size = sfs.length + ifs.length;
            java.lang.reflect.Field[] fs = new java.lang.reflect.Field[size];
            for (int j=0; j<sfs.length; ++j) {
                fs[j] = (java.lang.reflect.Field)sfs[j].getJavaLangReflectMemberObject();
            }
            for (int j=0; j<ifs.length; ++j) {
                fs[sfs.length+j] = (java.lang.reflect.Field)ifs[j].getJavaLangReflectMemberObject();
            }
            return fs;
        } else {
            Assert._assert(which == java.lang.reflect.Member.PUBLIC);
            c.prepare();
            int size = 0;
            jq_StaticField[] sfs = c.getStaticFields();
            jq_InstanceField[] ifs = c.getInstanceFields();
            for (int j=0; j<sfs.length; ++j) {
                if (sfs[j].isPublic()) ++size;
            }
            for (int j=0; j<ifs.length; ++j) {
                if (ifs[j].isPublic()) ++size;
            }
            java.lang.reflect.Field[] fs = new java.lang.reflect.Field[size];
            int current = -1;
            for (int j=0; j<sfs.length; ++j) {
                if (sfs[j].isPublic())
                    fs[++current] = (java.lang.reflect.Field)sfs[j].getJavaLangReflectMemberObject();
            }
            for (int j=0; j<ifs.length; ++j) {
                if (ifs[j].isPublic())
                    fs[++current] = (java.lang.reflect.Field)ifs[j].getJavaLangReflectMemberObject();
            }
            Assert._assert(current+1 == fs.length);
            return fs;
        }
    }
    private java.lang.reflect.Method[] getMethods0(int which) {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType()) return new java.lang.reflect.Method[0];
        jq_Class c = (jq_Class)jq_type;
        c.load();
        if (which == java.lang.reflect.Member.DECLARED) {
            jq_StaticMethod[] sfs = c.getDeclaredStaticMethods();
            jq_InstanceMethod[] ifs = c.getDeclaredInstanceMethods();
            int size = sfs.length + ifs.length;
            for (int j=0; j<sfs.length; ++j) {
                if (sfs[j] instanceof jq_ClassInitializer) --size;
            }
            for (int j=0; j<ifs.length; ++j) {
                if (ifs[j] instanceof jq_Initializer) --size;
            }
            java.lang.reflect.Method[] fs = new java.lang.reflect.Method[size];
            int k = -1;
            for (int j=0; j<sfs.length; ++j) {
                if (sfs[j] instanceof jq_ClassInitializer) continue;
                fs[++k] = (java.lang.reflect.Method)sfs[j].getJavaLangReflectMemberObject();
            }
            for (int j=0; j<ifs.length; ++j) {
                if (ifs[j] instanceof jq_Initializer) continue;
                fs[++k] = (java.lang.reflect.Method)ifs[j].getJavaLangReflectMemberObject();
            }
            Assert._assert(k == fs.length-1);
            return fs;
        } else {
            Assert._assert(which == java.lang.reflect.Member.PUBLIC);
            c.prepare();
            int size = 0;
            jq_StaticMethod[] sfs = c.getStaticMethods();
            jq_InstanceMethod[] ifs = c.getVirtualMethods();
            for (int j=0; j<sfs.length; ++j) {
                if (sfs[j] instanceof jq_ClassInitializer) continue;
                if (sfs[j].isPublic()) ++size;
            }
            for (int j=0; j<ifs.length; ++j) {
                if (ifs[j] instanceof jq_Initializer) continue;
                if (ifs[j].isPublic()) ++size;
            }
            java.lang.reflect.Method[] fs = new java.lang.reflect.Method[size];
            int k = -1;
            for (int j=0; j<sfs.length; ++j) {
                if (sfs[j] instanceof jq_ClassInitializer) continue;
                if (sfs[j].isPublic())
                    fs[++k] = (java.lang.reflect.Method)sfs[j].getJavaLangReflectMemberObject();
            }
            for (int j=0; j<ifs.length; ++j) {
                if (ifs[j] instanceof jq_Initializer) continue;
                if (ifs[j].isPublic())
                    fs[++k] = (java.lang.reflect.Method)ifs[j].getJavaLangReflectMemberObject();
            }
            Assert._assert(k == fs.length-1);
            return fs;
        }
    }
    private java.lang.reflect.Constructor[] getConstructors0(int which) {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType()) return new java.lang.reflect.Constructor[0];
        jq_Class c = (jq_Class)jq_type;
        c.load();
        jq_InstanceMethod[] ifs = c.getDeclaredInstanceMethods();
        int size = 0;
        for (int j=0; j<ifs.length; ++j) {
            if (which == java.lang.reflect.Member.PUBLIC && !ifs[j].isPublic())
                continue;
            if (ifs[j] instanceof jq_Initializer) ++size;
        }
        java.lang.reflect.Constructor[] fs = new java.lang.reflect.Constructor[size];
        
        int k = -1;
        for (int j=0; j<ifs.length; ++j) {
            if (which == java.lang.reflect.Member.PUBLIC && !ifs[j].isPublic())
                continue;
            if (ifs[j] instanceof jq_Initializer) {
                fs[++k] = (java.lang.reflect.Constructor)ifs[j].getJavaLangReflectMemberObject();
            }
        }
        Assert._assert(k == fs.length-1);
        return fs;
    }
    private java.lang.reflect.Field getField0(java.lang.String name, int which)
        throws java.lang.NoSuchFieldException
    {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType())
            throw new java.lang.NoSuchFieldException(jq_type.getJDKDesc());
        jq_Class c = (jq_Class)jq_type;
        c.load();
        Utf8 utf8name = Utf8.get(name);
        jq_Field[] sms;
        if (which == java.lang.reflect.Member.DECLARED) {
            sms = c.getDeclaredStaticFields();
        } else {
            Assert._assert(which == java.lang.reflect.Member.PUBLIC);
            sms = c.getStaticFields();
        }
        for (int i=0; i<sms.length; ++i) {
            if (sms[i].getName() == utf8name) {
                if (which == java.lang.reflect.Member.PUBLIC && !sms[i].isPublic())
                    continue;
                return (java.lang.reflect.Field)sms[i].getJavaLangReflectMemberObject();
            }
        }
        if (which == java.lang.reflect.Member.DECLARED) {
            sms = c.getDeclaredInstanceFields();
        } else {
            Assert._assert(which == java.lang.reflect.Member.PUBLIC);
            c.prepare();
            sms = c.getInstanceFields();
        }
        for (int i=0; i<sms.length; ++i) {
            if (sms[i].getName() == utf8name) {
                if (which == java.lang.reflect.Member.PUBLIC && !sms[i].isPublic())
                    continue;
                return (java.lang.reflect.Field)sms[i].getJavaLangReflectMemberObject();
            }
        }
        throw new java.lang.NoSuchFieldException(c.getJDKName()+"."+name);
    }
    private java.lang.reflect.Method getMethod0(java.lang.String name, java.lang.Class[] parameterTypes, int which)
        throws java.lang.NoSuchMethodException
    {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType())
            throw new java.lang.NoSuchMethodException(jq_type.getJDKDesc());
        if (name.equals("<init>") || name.equals("<clinit>"))
            throw new java.lang.NoSuchMethodException(name+" is an illegal method name");
        jq_Class c = (jq_Class)jq_type;
        java.lang.StringBuffer sb = new java.lang.StringBuffer();
        sb.append('(');
        for (int i=0; i<parameterTypes.length; ++i) {
            sb.append(Reflection.getJQType(parameterTypes[i]).getDesc().toString());
        }
        sb.append(')');
        java.lang.String args = sb.toString();
        c.load();
        Utf8 utf8name = Utf8.get(name);
        jq_Method[] sms;
        if (which == java.lang.reflect.Member.DECLARED) {
            sms = c.getDeclaredStaticMethods();
        } else {
            Assert._assert(which == java.lang.reflect.Member.PUBLIC);
            sms = c.getStaticMethods();
        }
        for (int i=0; i<sms.length; ++i) {
            if (sms[i].getName() == utf8name) {
                if (which == java.lang.reflect.Member.PUBLIC && !sms[i].isPublic())
                    continue;
                if (sms[i].getDesc().toString().startsWith(args))
                    return (java.lang.reflect.Method)sms[i].getJavaLangReflectMemberObject();
            }
        }
        if (which == java.lang.reflect.Member.DECLARED) {
            sms = c.getDeclaredInstanceMethods();
        } else {
            Assert._assert(which == java.lang.reflect.Member.PUBLIC);
            c.prepare();
            sms = c.getVirtualMethods();
        }
        for (int i=0; i<sms.length; ++i) {
            if (sms[i].getName() == utf8name) {
                if (which == java.lang.reflect.Member.PUBLIC && !sms[i].isPublic())
                    continue;
                if (sms[i].getDesc().toString().startsWith(args))
                    return (java.lang.reflect.Method)sms[i].getJavaLangReflectMemberObject();
            }
        }
        throw new java.lang.NoSuchMethodException(c.getJDKName()+"."+name+args);
    }
    private java.lang.reflect.Constructor getConstructor0(java.lang.Class[] parameterTypes, int which)
        throws java.lang.NoSuchMethodException
    {
        jq_Type jq_type = this.jq_type;
        if (!jq_type.isClassType())
            throw new java.lang.NoSuchMethodException(jq_type.getJDKDesc());
        jq_Class c = (jq_Class)jq_type;
        Utf8 utf8desc = buildMethodDescriptor(parameterTypes, null);
        jq_NameAndDesc nd = new jq_NameAndDesc(Utf8.get("<init>"), utf8desc);
        jq_Initializer i = c.getInitializer(nd);
        if (i == null || (which == java.lang.reflect.Member.PUBLIC && !i.isPublic()))
            throw new java.lang.NoSuchMethodException(c.getJDKName()+utf8desc);
        return (java.lang.reflect.Constructor)i.getJavaLangReflectMemberObject();
    }
    private java.lang.Class[] getDeclaredClasses0() {
        // TODO: handle declared classes
        return new java.lang.Class[0];
    }
    
    // additional methods.
    // ONLY TO BE CALLED BY jq_Class CONSTRUCTOR!!!
    public static java.lang.Class createNewClass(jq_Type jq_type) {
        java.lang.Object o = new Class(jq_type);
        return (java.lang.Class)o;
    }
    
    public static jq_Type getJQType(java.lang.Class f) {
        java.lang.Object o = f;
        return ((Class)o).jq_type;
    }
    
    public static Utf8 buildMethodDescriptor(java.lang.Class[] args, java.lang.Class returnType) {
        java.lang.StringBuffer sb = new java.lang.StringBuffer();
        sb.append('(');
        for (int i=0; i<args.length; ++i) {
            sb.append(Reflection.getJQType(args[i]).getDesc().toString());
        }
        sb.append(')');
        if (returnType == null) sb.append('V');
        else sb.append(Reflection.getJQType(returnType).getDesc().toString());
        return Utf8.get(sb.toString());
    }
}
