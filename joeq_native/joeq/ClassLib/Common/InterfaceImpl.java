// InterfaceImpl.java, created Wed Dec 11 11:59:03 2002 by mcmartin
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.lang.reflect.Constructor;
import joeq.Bootstrap.MethodInvocation;
import joeq.Bootstrap.SinglePassBootImage;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.Class.jq_Reference.jq_NullType;
import joeq.ClassLib.ClassLibInterface;
import joeq.Compiler.Quad.BytecodeToQuad.jq_ReturnAddressType;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.ObjectTraverser;
import joeq.Runtime.Reflection;
import joeq.Runtime.TypeCheck;
import joeq.Scheduler.jq_Thread;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * InterfaceImpl
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: InterfaceImpl.java,v 1.21 2004/09/30 03:35:37 joewhaley Exp $
 */
public abstract class InterfaceImpl implements Interface {

    /** Creates new Interface */
    public InterfaceImpl() {}

    public java.util.Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && (desc.toString().startsWith("Ljava/") ||
                                                    desc.toString().startsWith("Lsun/misc/"))) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/Common/"+desc.toString().substring(1));
            return java.util.Collections.singleton(u).iterator();
        }
        return java.util.Collections.EMPTY_SET.iterator();
    }
    
    public abstract ObjectTraverser getObjectTraverser();
    
    public static class CommonObjectTraverser extends ObjectTraverser {
        //public static CommonObjectTraverser INSTANCE = new CommonObjectTraverser();
        protected CommonObjectTraverser() { }
        protected static final java.util.Set nullStaticFields = new java.util.HashSet();
        protected static final java.util.Set nullInstanceFields = new java.util.HashSet();
        protected static final java.util.Map mappedObjects = new java.util.HashMap();
        public static /*final*/ boolean IGNORE_THREAD_LOCALS = true;
        public void initialize() {
            //nullStaticFields.add(Unsafe._remapper_object);
            nullStaticFields.add(CodeAddress._FACTORY);
            nullStaticFields.add(HeapAddress._FACTORY);
            nullStaticFields.add(StackAddress._FACTORY);
            nullStaticFields.add(SinglePassBootImage._DEFAULT);
            nullStaticFields.add(Reflection._obj_trav);
            Reflection.registerNullStaticFields(nullStaticFields);
            nullStaticFields.add(joeq.Allocator.DefaultCodeAllocator._default_allocator);
            jq_Class k = PrimordialClassLoader.getJavaLangSystem();
            nullStaticFields.add(k.getOrCreateStaticField("in", "Ljava/io/InputStream;"));
            nullStaticFields.add(k.getOrCreateStaticField("out", "Ljava/io/PrintStream;"));
            nullStaticFields.add(k.getOrCreateStaticField("err", "Ljava/io/PrintStream;"));
            nullStaticFields.add(k.getOrCreateStaticField("props", "Ljava/util/Properties;"));
            k = PrimordialClassLoader.getJavaLangClassLoader();
            nullStaticFields.add(k.getOrCreateStaticField("loadedLibraryNames",
                                                          "Ljava/util/Vector;"));
            nullStaticFields.add(k.getOrCreateStaticField("systemNativeLibraries",
                                                          "Ljava/util/Vector;"));
            nullStaticFields.add(k.getOrCreateStaticField("nativeLibraryContext",
                                                          "Ljava/util/Stack;"));
            k = PrimordialClassLoader.getJavaLangRefFinalizer();
            // todo: static fields in "java.lang.ref.Finalizer" need help.
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Main/jq;");
            nullStaticFields.add(k.getOrCreateStaticField("on_vm_startup",
                                                          "Ljava/util/List;"));
            k = PrimordialClassLoader.getJavaLangClass();
            // TODO: signers
            nullInstanceFields.add(k.getOrCreateInstanceField("signers", "[Ljava/lang/Object;"));
            // TODO: protection_domain
            nullInstanceFields.add(k.getOrCreateInstanceField("protection_domain", "Ljava/security/ProtectionDomain;"));
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/zip/ZipFile;");
            nullInstanceFields.add(k.getOrCreateInstanceField("raf", "Ljava/io/RandomAccessFile;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("entries", "Ljava/util/Hashtable;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("cenpos", "J"));
            nullInstanceFields.add(k.getOrCreateInstanceField("pos", "J"));
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/PrimordialClassLoader$ZipFileElement;");
            nullInstanceFields.add(k.getOrCreateInstanceField("entries", "Ljava/util/Map;"));
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/PrimordialClassLoader$PathElement;");
            nullInstanceFields.add(k.getOrCreateInstanceField("entries", "Ljava/util/Set;"));
        }

        public java.lang.Object mapStaticField(jq_StaticField f) {
            if (nullStaticFields.contains(f)) {
                return null;
            }
            return NO_OBJECT;
        }
        public java.lang.Object mapInstanceField(java.lang.Object o,
                                                 jq_InstanceField f) {
            if (nullInstanceFields.contains(f)) {
                return null;
            }
            jq_Class c = f.getDeclaringClass();
            String fieldName = f.getName().toString();
            if (c == PrimordialClassLoader.getJavaLangClass()) {
                if (fieldName.equals("jq_type"))
                    return Reflection.getJQType((java.lang.Class) o);
            } else if (c == jq_Type._class) {
                if (o == jq_NullType.NULL_TYPE)
                    return null;
                if (o == jq_ReturnAddressType.INSTANCE)
                    return null;
                if (!joeq.Class.jq_Class.USE_CLASS_OBJECT_FIELD &&
                    fieldName.equals("class_object"))
                    return Reflection.getJDKType((jq_Type) o);
            } else if (c == PrimordialClassLoader.getJavaLangReflectField()) {
                if (fieldName.equals("jq_field")) {
                    Object r = Reflection.getJQMember((java.lang.reflect.Field) o);
                    return r;
                }
            } else if (c == PrimordialClassLoader.getJavaLangReflectMethod()) {
                if (fieldName.equals("jq_method"))
                    return Reflection.getJQMember((java.lang.reflect.Method) o);
            } else if (
                c == PrimordialClassLoader.getJavaLangReflectConstructor()) {
                if (fieldName.equals("jq_init"))
                    return Reflection.getJQMember((Constructor) o);
            } else if (!joeq.Class.jq_Member.USE_MEMBER_OBJECT_FIELD &&
                       (c == PrimordialClassLoader.loader.getBSType("Ljoeq/Class/jq_Member;"))) {
                if (fieldName.equals("member_object")) {
                    // reflection returns different objects every time!
                    // cache one and use it from now on.
                    Object o2 = mappedObjects.get(o);
                    if (o2 != null)
                        return o2;
                    mappedObjects.put(o, o2 = Reflection.getJDKMember((jq_Member) o));
                    return o2;
                }
            } else if (c == PrimordialClassLoader.getJavaLangThread()) {
                if (fieldName.equals("jq_thread")) {
                    Object o2 = mappedObjects.get(o);
                    if (o2 != null)
                        return o2;
                    mappedObjects.put(o, o2 = new jq_Thread((Thread) o));
                    return o2;
                }
                /***
                if (fieldName.equals("threadLocals"))
                    return java.util.Collections.EMPTY_MAP;
                if (fieldName.equals("inheritableThreadLocals"))
                    return java.util.Collections.EMPTY_MAP;
                ***/
            } else if (c == PrimordialClassLoader.getJavaLangClassLoader()) {
                if (o == PrimordialClassLoader.loader) {
                    if (fieldName.equals("parent"))
                        return null;
                    if (fieldName.equals("desc2type"))
                        return getInstanceFieldValue_reflection(o, lookupField(PrimordialClassLoader.class, "bs_desc2type"));
                } else if (fieldName.equals("desc2type")) {
                    Object o2 = mappedObjects.get(o);
                    if (o2 != null)
                        return o2;
                    Class c2 = Reflection.getJDKType(c);
                    Vector classes = (Vector) getInstanceFieldValue_reflection(o, lookupField(c2, "classes"));
                    HashMap desc2type = new HashMap();
                    Iterator i = classes.iterator();
                    while (i.hasNext()) {
                        Class c3 = (Class) i.next();
                        jq_Type t = Reflection.getJQType(c3);
                        desc2type.put(t.getDesc(), t);
                    }
                    mappedObjects.put(o, desc2type);
                    return desc2type;
                }
            } else if (c == PrimordialClassLoader.getJavaLangRefFinalizer()) {
                // null out all fields of finalizer objects.
                return null;
            } else if (TypeCheck.isAssignable(c, PrimordialClassLoader.getJavaLangThrowable())) {
                if (fieldName.equals("backtrace")) {
                    // sun jvm crashes when using reflection on java.lang.Throwable.backtrace
                    return null;
                    /*
                    Object o2 = mapped_objects.get(o);
                    if (o2 != null) return o2;
                    mapped_objects.put(o, o2 = new int[0]);
                    return o2;
                    */
                }
            } else if (c == PrimordialClassLoader.loader.getBSType("Ljava/util/zip/Inflater;")) {
                // Inflater objects are reinitialized on VM startup.
                return null;
            }

            return NO_OBJECT;
        }
        
        public java.lang.Object mapValue(java.lang.Object o) {
            if (o == ClassLoader.getSystemClassLoader()) {
                return PrimordialClassLoader.loader;
            }
            else
            if (o instanceof java.util.zip.ZipFile) {
                Object o2 = mappedObjects.get(o);
                if (o2 != null) return o;
                mappedObjects.put(o, o);
                String name = ((java.util.zip.ZipFile)o).getName();
            
                // we need to reopen the ZipFile on VM startup
                if (jq.on_vm_startup != null) {
                    Object[] args = { o, name };
                    jq_Method zip_open = ClassLibInterface._class.getOrCreateStaticMethod("init_zipfile_static", "(Ljava/util/zip/ZipFile;Ljava/lang/String;)V");
                    MethodInvocation mi = new MethodInvocation(zip_open, args);
                    jq.on_vm_startup.add(mi);
                    System.out.println("Reopening zip file on joeq startup: "+name);
                }
            }
            else
            if (o instanceof java.util.zip.Inflater) {
                Object o2 = mappedObjects.get(o);
                if (o2 != null) return o;
                mappedObjects.put(o, o);
                boolean nowrap = false; // TODO: how do we know?
                
                // we need to reinitialize the Inflater on VM startup
                if (jq.on_vm_startup != null) {
                    Object[] args = { o, Convert.getBoolean(nowrap) };
                    jq_Method zip_open = ClassLibInterface._class.getOrCreateStaticMethod("init_inflater_static", "(Ljava/util/zip/Inflater;Z)V");
                    MethodInvocation mi = new MethodInvocation(zip_open, args);
                    jq.on_vm_startup.add(mi);
                    System.out.println("Reinitializing inflater on joeq startup: "+o);
                }
            }
            return o;
        }
    }
    
    public java.lang.Class createNewClass(joeq.Class.jq_Type f) {
        Assert._assert(jq.RunningNative);
        return joeq.ClassLib.Common.java.lang.Class.createNewClass(f);
    }
    
    public java.lang.reflect.Constructor createNewConstructor(joeq.Class.jq_Initializer f) {
        Assert._assert(jq.RunningNative);
        return joeq.ClassLib.Common.java.lang.reflect.Constructor.createNewConstructor(f);
    }
    
    public void initNewConstructor(java.lang.reflect.Constructor dis, joeq.Class.jq_Initializer f) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = dis;
        joeq.ClassLib.Common.java.lang.reflect.Constructor.initNewConstructor((joeq.ClassLib.Common.java.lang.reflect.Constructor)o, f);
    }
    
    public java.lang.reflect.Field createNewField(joeq.Class.jq_Field f) {
        Assert._assert(jq.RunningNative);
        return joeq.ClassLib.Common.java.lang.reflect.Field.createNewField(f);
    }
    
    public void initNewField(java.lang.reflect.Field dis, joeq.Class.jq_Field f) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = dis;
        joeq.ClassLib.Common.java.lang.reflect.Field.initNewField((joeq.ClassLib.Common.java.lang.reflect.Field)o, f);
    }
    
    public java.lang.reflect.Method createNewMethod(joeq.Class.jq_Method f) {
        Assert._assert(jq.RunningNative);
        return joeq.ClassLib.Common.java.lang.reflect.Method.createNewMethod(f);
    }
    
    public void initNewMethod(java.lang.reflect.Method dis, joeq.Class.jq_Method f) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = dis;
        joeq.ClassLib.Common.java.lang.reflect.Method.initNewMethod((joeq.ClassLib.Common.java.lang.reflect.Method)o, f);
    }
    
    public joeq.Class.jq_Field getJQField(java.lang.reflect.Field f) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = f;
        return ((joeq.ClassLib.Common.java.lang.reflect.Field)o).jq_field;
    }
    
    public joeq.Class.jq_Initializer getJQInitializer(java.lang.reflect.Constructor f) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = f;
        return ((joeq.ClassLib.Common.java.lang.reflect.Constructor)o).jq_init;
    }
    
    public joeq.Class.jq_Method getJQMethod(java.lang.reflect.Method f) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = f;
        return ((joeq.ClassLib.Common.java.lang.reflect.Method)o).jq_method;
    }
    
    public joeq.Class.jq_Type getJQType(java.lang.Class k) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = k;
        return ((joeq.ClassLib.Common.java.lang.Class)o).jq_type;
    }
    
    public joeq.Class.jq_Type getOrCreateType(java.lang.ClassLoader cl, joeq.UTF.Utf8 desc) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = cl;
        return ((joeq.ClassLib.Common.java.lang.ClassLoader)o).getOrCreateType(desc);
    }
    
    public void unloadType(java.lang.ClassLoader cl, joeq.Class.jq_Type t) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = cl;
        ((joeq.ClassLib.Common.java.lang.ClassLoader)o).unloadType(t);
    }
    
    public void init_zipfile(java.util.zip.ZipFile dis, java.lang.String name) throws java.io.IOException {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = dis;
        ((joeq.ClassLib.Common.java.util.zip.ZipFile)o).__init__(name);
    }
    
    public void init_inflater(java.util.zip.Inflater dis, boolean nowrap) throws java.io.IOException {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = dis;
        ((joeq.ClassLib.Common.java.util.zip.Inflater)o).__init__(nowrap);
    }
    
    public void initializeSystemClass() throws java.lang.Throwable {
        Assert._assert(jq.RunningNative);
        joeq.ClassLib.Common.java.lang.System.initializeSystemClass();
    }
    
    public joeq.Scheduler.jq_Thread getJQThread(java.lang.Thread t) {
        Assert._assert(jq.RunningNative);
        java.lang.Object o = t;
        return ((joeq.ClassLib.Common.java.lang.Thread)o).jq_thread;
    }    

    /*
    public void open(java.io.RandomAccessFile dis, java.lang.String name, boolean writeable) throws java.io.FileNotFoundException {
        jq.Assert(jq.RunningNative);
        java.lang.Object o = dis;
        ((joeq.ClassLib.Common.java.io.RandomAccessFile)o).open(name, writeable);
    }
    */
    
}
