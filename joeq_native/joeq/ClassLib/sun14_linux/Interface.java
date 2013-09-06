// Interface.java, created Fri Aug 16 18:11:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_linux;

import java.util.Collections;
import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.ClassLib.ClassLibInterface;
import joeq.Runtime.ObjectTraverser;
import joeq.Scheduler.jq_NativeThread;
import jwutil.collections.AppendIterator;

/**
 * Interface
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Interface.java,v 1.24 2004/09/30 03:35:38 joewhaley Exp $
 */
public class Interface extends joeq.ClassLib.Common.InterfaceImpl {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && desc.toString().startsWith("Ljava/")) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/sun14_linux/"+desc.toString().substring(1));
            return new AppendIterator(super.getImplementationClassDescs(desc),
                                      Collections.singleton(u).iterator());
        }
        return super.getImplementationClassDescs(desc);
    }

    public ObjectTraverser getObjectTraverser() {
        return sun14_linuxObjectTraverser.INSTANCE;
    }
    
    public static class sun14_linuxObjectTraverser extends CommonObjectTraverser {
        public static sun14_linuxObjectTraverser INSTANCE = new sun14_linuxObjectTraverser();
        protected sun14_linuxObjectTraverser() {}
        public void initialize() {
            super.initialize();
            
            jq_Class k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Lsun/misc/Unsafe;");
            nullStaticFields.add(k.getOrCreateStaticField("theUnsafe", "Lsun/misc/Unsafe;"));
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Lsun/reflect/UnsafeFieldAccessorImpl;");
            nullStaticFields.add(k.getOrCreateStaticField("unsafe", "Lsun/misc/Unsafe;"));
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/nio/charset/Charset;");
            nullStaticFields.add(k.getOrCreateStaticField("cache", "[Ljava/lang/Object;"));

            k = PrimordialClassLoader.getJavaLangClass();
            nullInstanceFields.add(k.getOrCreateInstanceField("declaredFields", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("publicFields", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("declaredMethods", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("publicMethods", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("declaredConstructors", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("publicConstructors", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("declaredPublicFields", "Ljava/lang/ref/SoftReference;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("declaredPublicMethods", "Ljava/lang/ref/SoftReference;"));
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/reflect/Field;");
            nullInstanceFields.add(k.getOrCreateInstanceField("fieldAccessor", "Lsun/reflect/FieldAccessor;"));
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/reflect/Method;");
            nullInstanceFields.add(k.getOrCreateInstanceField("methodAccessor", "Lsun/reflect/MethodAccessor;"));
            k = (joeq.Class.jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/reflect/Constructor;");
            nullInstanceFields.add(k.getOrCreateInstanceField("constructorAccessor", "Lsun/reflect/ConstructorAccessor;"));
            
            if (IGNORE_THREAD_LOCALS) {
                // for some reason, thread local gets created during bootstrapping. (SoftReference)
                // for now, just kill all thread locals.
                k = (joeq.Class.jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Thread;");
                nullInstanceFields.add(k.getOrCreateInstanceField("threadLocals", "Ljava/lang/ThreadLocal$ThreadLocalMap;"));
                nullInstanceFields.add(k.getOrCreateInstanceField("inheritableThreadLocals", "Ljava/lang/ThreadLocal$ThreadLocalMap;"));
            }
            
            jq_NativeThread.USE_INTERRUPTER_THREAD = false;
            
            // access the ISO-8859-1 character encoding, as it is used during bootstrapping
            //PrimordialClassLoader.loader.getOrCreateBSType("Lsun/nio/cs/ISO_8859_1;");
        }
        
        public java.lang.Object mapInstanceField(java.lang.Object o, joeq.Class.jq_InstanceField f) {
            jq_Class c = f.getDeclaringClass();
            if (c == PrimordialClassLoader.loader.getBSType("Lsun/nio/cs/AbstractCharsetProvider;")) {
                String fieldName = f.getName().toString();
                if (fieldName.equals("cache")) {
                    Object o2 = mappedObjects.get(o);
                    if (o2 != null)
                        return o2;
                    o2 = new java.util.TreeMap(java.lang.String.CASE_INSENSITIVE_ORDER);
                    mappedObjects.put(o, o2);
                    return o2;
                }
            }
            return super.mapInstanceField(o, f);
        }
    }
    
}
