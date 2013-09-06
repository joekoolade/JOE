// BootstrapRootSet.java, created Wed Jun 26 12:27:23 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassInitializer;
import joeq.Class.jq_Field;
import joeq.Class.jq_FieldVisitor;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Class.jq_TypeVisitor;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.ExceptionDeliverer;
import joeq.Runtime.Reflection;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import jwutil.collections.IdentityHashCodeWrapper;
import jwutil.util.Assert;

/**
 * BootstrapRootSet
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BootstrapRootSet.java,v 1.34 2004/09/30 03:35:29 joewhaley Exp $
 */
public class BootstrapRootSet {

    public static /*final*/ boolean TRACE = false;
    public static final java.io.PrintStream out = System.out;
    
    protected final Set/*jq_Type*/ instantiatedTypes;
    protected final Set/*jq_Type*/ necessaryTypes;
    protected final Set/*jq_Field*/ necessaryFields;
    protected final Set/*jq_Method*/ necessaryMethods;
    
    protected final LinkedHashSet/*Object*/ visitedObjects;
    
    protected List/*jq_TypeVisitor*/ instantiatedTypesListeners;
    protected List/*jq_TypeVisitor*/ necessaryTypesListeners;
    protected List/*jq_FieldVisitor*/ necessaryFieldsListeners;
    protected List/*jq_MethodVisitor*/ necessaryMethodsListeners;
    
    public boolean AddAllFields;
    
    /** Creates new BootstrapRootSet */
    public BootstrapRootSet(boolean addall) {
        this.instantiatedTypes = new HashSet();
        this.necessaryTypes = new HashSet();
        this.necessaryFields = new HashSet();
        this.necessaryMethods = new HashSet();
        this.visitedObjects = new LinkedHashSet();
        this.AddAllFields = addall;
    }
    
    public Set/*jq_Type*/ getInstantiatedTypes() { return instantiatedTypes; }
    public Set/*jq_Type*/ getNecessaryTypes() { return necessaryTypes; }
    public Set/*jq_Field*/ getNecessaryFields() { return necessaryFields; }
    public Set/*jq_Method*/ getNecessaryMethods() { return necessaryMethods; }
    
    public void registerInstantiatedTypeListener(jq_TypeVisitor tv) {
        if (instantiatedTypesListeners == null)
            instantiatedTypesListeners = new LinkedList();
        instantiatedTypesListeners.add(tv);
    }
    public void unregisterInstantiatedTypeListener(jq_TypeVisitor tv) {
        instantiatedTypesListeners.remove(tv);
    }
    public void registerNecessaryTypeListener(jq_TypeVisitor tv) {
        if (necessaryTypesListeners == null)
            necessaryTypesListeners = new LinkedList();
        necessaryTypesListeners.add(tv);
    }
    public void unregisterNecessaryTypeListener(jq_TypeVisitor tv) {
        necessaryTypesListeners.remove(tv);
    }
    public void registerNecessaryFieldListener(jq_FieldVisitor tv) {
        if (necessaryFieldsListeners == null)
            necessaryFieldsListeners = new LinkedList();
        necessaryFieldsListeners.add(tv);
    }
    public void unregisterNecessaryFieldListener(jq_FieldVisitor tv) {
        necessaryFieldsListeners.remove(tv);
    }
    public void registerNecessaryMethodListener(jq_MethodVisitor tv) {
        if (necessaryMethodsListeners == null)
            necessaryMethodsListeners = new LinkedList();
        necessaryMethodsListeners.add(tv);
    }
    public void unregisterNecessaryMethodListener(jq_MethodVisitor tv) {
        necessaryMethodsListeners.remove(tv);
    }
    
    public boolean addInstantiatedType(jq_Type t) {
        Assert._assert(t != null);
        addNecessaryType(t);
        boolean b = instantiatedTypes.add(t);
        if (b) {
            if (TRACE) out.println("New instantiated type: "+t);
            if (instantiatedTypesListeners != null) {
                for (Iterator i=instantiatedTypesListeners.iterator(); i.hasNext(); ) {
                    jq_TypeVisitor tv = (jq_TypeVisitor)i.next();
                    t.accept(tv);
                }
            }
        }
        return b;
    }
    
    public jq_Type addNecessaryType(String desc) {
        String className = desc.substring(1, desc.length()-1).replace('/', '.');
        try {
            // attempt to load class in host VM first.
            Class.forName(className);
            jq_Type t = null;
            try {
                t = PrimordialClassLoader.loader.getOrCreateBSType(desc);
                t.load();
                addNecessaryType(t);
                return t;
            } catch (NoClassDefFoundError x) {
                System.out.println("Note: Cannot load class "+t+" present in host Jvm");
                PrimordialClassLoader.unloadType(PrimordialClassLoader.loader, t);
            }
        } catch (ClassNotFoundException x) { }
        return null;
    }
        
    public boolean addNecessaryType(jq_Type t) {
        if (t == null) return false;
        t.prepare();
        boolean b = necessaryTypes.add(t);
        if (b) {
            if (TRACE) out.println("New necessary type: "+t);
            if (necessaryTypesListeners != null) {
                for (Iterator i=necessaryTypesListeners.iterator(); i.hasNext(); ) {
                    jq_TypeVisitor tv = (jq_TypeVisitor)i.next();
                    t.accept(tv);
                }
            }
            if (t instanceof jq_Class) {
                jq_Class klass = (jq_Class)t;
                if (AddAllFields) {
                    jq_StaticField[] sfs = klass.getDeclaredStaticFields();
                    for (int i=0; i<sfs.length; ++i) {
                        addNecessaryField(sfs[i]);
                    }
                }
                // add superclass as necessary, as well.
                addNecessaryType(klass.getSuperclass());
            }
        }
        return b;
    }
    
    public jq_StaticField addNecessaryStaticField(jq_Class c, String name, String desc) {
        if (c == null) return null;
        jq_StaticField f = c.getOrCreateStaticField(name, desc);
        addNecessaryField(f);
        return f;
    }
    
    public jq_InstanceField addNecessaryInstanceField(jq_Class c, String name, String desc) {
        if (c == null) return null;
        jq_InstanceField f = c.getOrCreateInstanceField(name, desc);
        addNecessaryField(f);
        return f;
    }
        
    public boolean addNecessaryField(jq_Field t) {
        addNecessaryType(t.getDeclaringClass());
        boolean b = necessaryFields.add(t);
        if (b) {
            if (TRACE) out.println("New necessary field: "+t);
            if (necessaryFieldsListeners != null) {
                for (Iterator i=necessaryFieldsListeners.iterator(); i.hasNext(); ) {
                    jq_FieldVisitor tv = (jq_FieldVisitor)i.next();
                    t.accept(tv);
                }
            }
        }
        return b;
    }
    
    public jq_StaticMethod addNecessaryStaticMethod(jq_Class c, String name, String desc) {
        if (c == null) return null;
        jq_StaticMethod f = c.getOrCreateStaticMethod(name, desc);
        addNecessaryMethod(f);
        return f;
    }
    
    public jq_InstanceMethod addNecessaryInstanceMethod(jq_Class c, String name, String desc) {
        if (c == null) return null;
        jq_InstanceMethod f = c.getOrCreateInstanceMethod(name, desc);
        addNecessaryMethod(f);
        return f;
    }
        
    public boolean addNecessaryMethod(jq_Method t) {
        addNecessaryType(t.getDeclaringClass());
        boolean b = necessaryMethods.add(t);
        if (b) {
            if (TRACE) out.println("New necessary method: "+t);
            if (necessaryMethodsListeners != null) {
                for (Iterator i=necessaryMethodsListeners.iterator(); i.hasNext(); ) {
                    jq_MethodVisitor tv = (jq_MethodVisitor)i.next();
                    t.accept(tv);
                }
            }
        }
        return b;
    }
    
    public void addDefaultRoots() {
        jq_Class c;
        jq_StaticMethod s_m; jq_InstanceMethod i_m;
        
        // some internal vm data structures are necessary for correct execution
        // under just about any circumstances.
        addNecessaryType(jq_Class._class);
        addNecessaryType(jq_Primitive._class);
        addNecessaryType(jq_Array._class);
        addNecessaryType(jq_InstanceField._class);
        addNecessaryType(jq_StaticField._class);
        addNecessaryType(jq_InstanceMethod._class);
        addNecessaryType(jq_StaticMethod._class);
        addNecessaryType(jq_Initializer._class);
        addNecessaryType(jq_ClassInitializer._class);
        addNecessaryType(CodeAddress._class);
        addNecessaryType(HeapAddress._class);
        addNecessaryType(StackAddress._class);
        addNecessaryField(jq_Reference._vtable);
        
        // the bootstrap loader uses the static fields in the SystemInterface class.
        SystemInterface._class.load();
        jq_StaticField[] sfs = SystemInterface._class.getDeclaredStaticFields();
        for (int i=0; i<sfs.length; ++i) {
            addNecessaryField(sfs[i]);
        }
        // even if there are no calls to these Unsafe methods, we need their definitions
        // to stick around so that we can check against them.
        Unsafe._class.load();
        jq_StaticMethod[] sms = Unsafe._class.getDeclaredStaticMethods();
        for (int i=0; i<sms.length; ++i) {
            if (sms[i] instanceof jq_ClassInitializer) continue;
            addNecessaryMethod(sms[i]);
        }
        //addNecessaryField(Unsafe._remapper_object);

        // We need to be able to allocate objects and code.
        addNecessaryType(joeq.Allocator.SimpleAllocator._class);
        addNecessaryType(PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Allocator/RuntimeCodeAllocator;"));
        
        // setIn0, setOut0, and setErr0 use these fields, but the trimmer doesn't detect the uses.
        c = PrimordialClassLoader.getJavaLangSystem();
        addNecessaryStaticField(c, "in", "Ljava/io/InputStream;");
        addNecessaryStaticField(c, "out", "Ljava/io/PrintStream;");
        addNecessaryStaticField(c, "err", "Ljava/io/PrintStream;");
        
        // private method initializeSystemClass is called reflectively
        addNecessaryStaticMethod(c, "initializeSystemClass", "()V");
        
        // an instance of this class is created via reflection during VM initialization.
        c = (jq_Class)Reflection.getJQType(sun.io.CharToByteConverter.getDefault().getClass());
        addNecessaryInstanceMethod(c, "<init>", "()V");
        
        // an instance of this class is created via reflection during VM initialization.
        c = (jq_Class)Reflection.getJQType(sun.io.ByteToCharConverter.getDefault().getClass());
        addNecessaryInstanceMethod(c, "<init>", "()V");
        
        // the trap handler can be implicitly called from any bytecode than can trigger a hardware exception.
        s_m = ExceptionDeliverer._trap_handler;
        addNecessaryMethod(s_m);
        
        // we want the compiler to be able to run at run time, too.
        i_m = jq_Method._compile;
        addNecessaryMethod(i_m);
        
        // debugger is large, so compile it on demand.
        if (false) {
            s_m = ExceptionDeliverer._debug_trap_handler;
            addNecessaryMethod(s_m);
        }
        
        // entrypoint for new threads
        addNecessaryMethod(joeq.Scheduler.jq_NativeThread._nativeThreadEntry);
        // thread switch interrupt
        addNecessaryMethod(joeq.Scheduler.jq_NativeThread._threadSwitch);
        // ctrl-break handler
        addNecessaryMethod(joeq.Scheduler.jq_NativeThread._ctrl_break_handler);
        // entrypoint for interrupter thread
        addNecessaryMethod(joeq.Scheduler.jq_InterrupterThread._run);
        
        // dunno why this doesn't show up
        addNecessaryType(joeq.Assembler.Heap2HeapReference._class);
        
        try {
            // an instance of this class is created via reflection during VM initialization.
            c = (jq_Class)Reflection.getJQType(sun.io.ByteToCharConverter.getConverter("ISO-8859-1").getClass());
            addNecessaryInstanceMethod(c, "<init>", "()V");
            // an instance of this class is created via reflection during VM initialization.
            c = (jq_Class)Reflection.getJQType(sun.io.CharToByteConverter.getConverter("ISO-8859-1").getClass());
            addNecessaryInstanceMethod(c, "<init>", "()V");
        } catch (java.io.UnsupportedEncodingException x) { }

        // JDK1.4: an instance of this class is created via reflection during VM initialization.
        c = (jq_Class) addNecessaryType("Lsun/nio/cs/ISO_8859_1;");
        addNecessaryInstanceMethod(c, "<init>", "()V");
        
        addNecessaryType("Lsun/nio/cs/ISO_8859_1$Encoder;");
        addNecessaryType("Lsun/nio/cs/ISO_8859_1$Decoder;");
        addNecessaryType("Lsun/nio/cs/ISO_8859_1$1;");
        
        // for JDK1.4.2
        addNecessaryType("Lsun/net/www/protocol/jar/Handler;");
        addNecessaryType("Ljava/util/logging/LogManager$Cleaner;");
        
        // tracing in the compiler uses these
        //c = jq._class; c.prepare();
        //addToWorklist(jq._hex8);
        //addToWorklist(jq._hex16);
    }
    
    public boolean addObjectAndSubfields(Object o) {
        return addObjectAndSubfields(o, visitedObjects);
    }
    private boolean addObjectAndSubfields(Object o, LinkedHashSet objs) {
        if (o == null) return false;
        IdentityHashCodeWrapper a = IdentityHashCodeWrapper.create(o);
        if (visitedObjects.contains(a) || objs.contains(a))
            return false;
        objs.add(a);
        Class objType = o.getClass();
        jq_Reference jqType = (jq_Reference)Reflection.getJQType(objType);
        if (TRACE) out.println("Adding object of type "+jqType+": "+o);
        addInstantiatedType(jqType);
        /*
                addClassInitializer((jq_Class)jqType);
                addSuperclassVirtualMethods((jq_Class)jqType);
                addClassInterfaceImplementations((jq_Class)jqType);
         */
        if (jqType.isArrayType()) {
            jq_Type elemType = ((jq_Array)jqType).getElementType();
            if (elemType.isAddressType()) {
                // no need to visit.
            } else if (elemType.isReferenceType()) {
                int length = java.lang.reflect.Array.getLength(o);
                Object[] v = (Object[])o;
                if (TRACE) out.println("Visiting "+jqType+" of "+length+" elements");
                for (int k=0; k<length; ++k) {
                    Object o2 = Reflection.arrayload_A(v, k);
                    addObjectAndSubfields(o2, objs);
                }
            }
        } else {
            Assert._assert(jqType.isClassType());
            jq_Class clazz = (jq_Class)jqType;
            jq_InstanceField[] fields = clazz.getInstanceFields();
            for (int k=0; k<fields.length; ++k) {
                jq_InstanceField f = fields[k];
                if (!AddAllFields && !necessaryFields.contains(f))
                    continue;
                jq_Type ftype = f.getType();
                if (ftype.isAddressType()) {
                    // no need to visit.
                } else if (ftype.isReferenceType()) {
                    if (TRACE) out.println("Visiting field "+f);
                    Object o2 = Reflection.getfield_A(o, f);
                    addObjectAndSubfields(o2, objs);
                }
            }
        }
        return true;
    }
    
    public void addNecessarySubfieldsOfVisitedObjects() {
        if (AddAllFields) return;
        LinkedHashSet objs = visitedObjects;
        for (;;) {
            LinkedHashSet objs2 = new LinkedHashSet();
            boolean change = false;
            for (Iterator i = objs.iterator(); i.hasNext(); ) {
                Object o = ((IdentityHashCodeWrapper)i.next()).getObject();
                Class objType = o.getClass();
                jq_Reference jqType = (jq_Reference)Reflection.getJQType(objType);
                if (jqType.isArrayType()) continue;
                Assert._assert(jqType.isClassType());
                jq_Class clazz = (jq_Class)jqType;
                jq_InstanceField[] fields = clazz.getInstanceFields();
                for (int k=0; k<fields.length; ++k) {
                    jq_InstanceField f = fields[k];
                    if (!necessaryFields.contains(f))
                        continue;
                    jq_Type ftype = f.getType();
                    if (ftype.isAddressType()) {
                        // no need to visit.
                    } else if (ftype.isReferenceType()) {
                        if (TRACE) out.println("Visiting field "+f+" of object of type "+clazz);
                        Object o2 = Reflection.getfield_A(o, f);
                        if (addObjectAndSubfields(o2, objs2))
                            change = true;
                    }
                }
            }
            if (!change) break;
            if (TRACE) out.println("Objects added: "+objs2.size()+", iterating over those objects.");
            visitedObjects.addAll(objs2);
            objs = objs2;
        }
    }
    
    public void addAllInterfaceMethodImplementations(jq_InstanceMethod i_m) {
        addNecessaryMethod(i_m);
        jq_Class interf = i_m.getDeclaringClass();
        Assert._assert(interf.isInterface());
        Iterator i = necessaryTypes.iterator();
        while (i.hasNext()) {
            jq_Type t = (jq_Type)i.next();
            if (!t.isReferenceType()) continue;
            if (t.isAddressType()) continue;
            jq_Reference r = (jq_Reference)t;
            if (!r.implementsInterface(interf)) continue;
            jq_InstanceMethod m2 = r.getVirtualMethod(i_m.getNameAndDesc());
            if (m2 == null) {
                // error:
                if (TRACE) out.println("Error: class "+r+" does not implement interface method "+i_m);
                continue;
            }
            addNecessaryMethod(m2);
        }
    }
    
    public void addAllVirtualMethodImplementations(jq_InstanceMethod i_m) {
        addNecessaryMethod(i_m);
        addAllVirtualMethodImplementations(i_m.getDeclaringClass(), i_m);
    }
    
    public void addAllVirtualMethodImplementations(jq_Class c, jq_InstanceMethod i_m) {
        if (!i_m.isOverridden())
            return;
        jq_Class[] subclasses = c.getSubClasses();
        for (int i=0; i<subclasses.length; ++i) {
            jq_Class subclass = subclasses[i];
            subclass.prepare();
            jq_Method m2 = (jq_Method)subclass.getDeclaredMember(i_m.getNameAndDesc());
            if (m2 != null && !m2.isStatic()) {
                addNecessaryMethod(m2);
            }
            addAllVirtualMethodImplementations(subclass, i_m);
        }
    }

    // not thread safe.
    public void trimClass(jq_Class clazz) {
        Assert._assert(clazz.isPrepared());

        jq_Class super_class = clazz.getSuperclass();
        if (super_class != null)
            trimClass(super_class);

        //Set instantiatedTypes = getInstantiatedTypes();
        Set necessaryFields = getNecessaryFields();
        Set necessaryMethods = getNecessaryMethods();
        
        Iterator it = clazz.getMembers().iterator();
        while (it.hasNext()) {
            jq_Member m = (jq_Member)it.next();
            if (m instanceof jq_Field) {
                if (!necessaryFields.contains(m)) {
                    if (TRACE) out.println("Eliminating field: "+m);
                    it.remove();
                }
            } else {
                Assert._assert(m instanceof jq_Method);
                if (!necessaryMethods.contains(m)) {
                    if (TRACE) out.println("Eliminating method: "+m);
                    it.remove();
                }
            }
        }

        int n;
        n=0;
        jq_InstanceField[] declared_instance_fields = clazz.getDeclaredInstanceFields();
        for (int i=0; i<declared_instance_fields.length; ++i) {
            jq_InstanceField f = declared_instance_fields[i];
            f.unprepare();
            if (necessaryMethods.contains(f)) ++n;
        }
        jq_InstanceField[] ifs = new jq_InstanceField[n];
        for (int i=0, j=-1; j<n-1; ++i) {
            jq_InstanceField f = declared_instance_fields[i];
            if (necessaryFields.contains(f)) {
                ifs[++j] = f;
                ++jq_Class.NumOfIFieldsKept;
            } else {
                if (TRACE) out.println("Eliminating instance field: "+f);
                ++jq_Class.NumOfIFieldsEliminated;
            }
        }
        clazz.setDeclaredInstanceFields(ifs);
        
        jq_StaticField[] static_fields = clazz.getDeclaredStaticFields();
        int static_data_size=0;
        n=0; 
        for (int i=0; i<static_fields.length; ++i) {
            jq_StaticField f = static_fields[i];
            f.unprepare();
            if (necessaryFields.contains(f)) ++n;
        }
        jq_StaticField[] sfs = new jq_StaticField[n];
        for (int i=0, j=-1; j<n-1; ++i) {
            jq_StaticField f = static_fields[i];
            if (necessaryFields.contains(f)) {
                sfs[++j] = f;
                static_data_size += f.getWidth();
                ++jq_Class.NumOfSFieldsKept;
            }
            else {
                if (TRACE) out.println("Eliminating static field: "+f);
                ++jq_Class.NumOfSFieldsEliminated;
            }
        }
        clazz.setDeclaredStaticFields(sfs);

        n=0;
        jq_InstanceMethod[] declared_instance_methods = clazz.getDeclaredInstanceMethods();
        for (int i=0; i<declared_instance_methods.length; ++i) {
            jq_InstanceMethod f = declared_instance_methods[i];
            f.unprepare();
            f.clearOverrideFlags();
            if (necessaryMethods.contains(f)) ++n;
        }
        jq_InstanceMethod[] ims = new jq_InstanceMethod[n];
        for (int i=0, j=-1; j<n-1; ++i) {
            jq_InstanceMethod f = declared_instance_methods[i];
            if (necessaryMethods.contains(f)) {
                ims[++j] = f;
                ++jq_Class.NumOfIMethodsKept;
            } else {
                if (BootstrapRootSet.TRACE) BootstrapRootSet.out.println("Eliminating instance method: "+f);
                ++jq_Class.NumOfIMethodsEliminated;
            }
        }
        clazz.setDeclaredInstanceMethods(ims);
        
        n=0;
        jq_StaticMethod[] static_methods = clazz.getDeclaredStaticMethods();
        for (int i=0; i<static_methods.length; ++i) {
            jq_StaticMethod f = static_methods[i];
            f.unprepare();
            if (necessaryMethods.contains(f)) ++n;
        }
        jq_StaticMethod[] sms = new jq_StaticMethod[n];
        for (int i=0, j=-1; j<n-1; ++i) {
            jq_StaticMethod f = static_methods[i];
            if (necessaryMethods.contains(f)) {
                sms[++j] = f;
                ++jq_Class.NumOfSMethodsKept;
            } else {
                if (BootstrapRootSet.TRACE) BootstrapRootSet.out.println("Eliminating static method: "+f);
                ++jq_Class.NumOfSMethodsEliminated;
            }
        }
        clazz.setDeclaredStaticMethods(sms);
        
        /*
        n=0;
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class f = declared_interfaces[i];
            if (instantiatedTypes.contains(f)) ++n;
        }
        jq_Class[] is = new jq_Class[n];
        for (int i=0, j=-1; j<n-1; ++i) {
            jq_Class f = declared_interfaces[i];
            if (instantiatedTypes.contains(f))
                is[++j] = f;
            else
                if (trim.TRACE) trim.out.println("Eliminating interface: "+f);
        }
        declared_interfaces = is;
        */
        
        clazz.getCP().trim(necessaryFields, necessaryMethods);
        
        clazz.prepare();
    }
}
