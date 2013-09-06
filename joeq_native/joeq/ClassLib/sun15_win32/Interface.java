// Interface.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun15_win32;

import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_StaticField;
import joeq.ClassLib.ClassLibInterface;
import joeq.Main.jq;
import joeq.Runtime.ObjectTraverser;
import jwutil.collections.AppendIterator;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Interface.java,v 1.7 2004/12/10 19:04:43 joewhaley Exp $
 */
public class Interface extends joeq.ClassLib.sun142_win32.Interface {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB &&
                (desc.toString().startsWith("Ljava/") ||
                 desc.toString().startsWith("Lsun/misc/"))) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/sun15_win32/"+desc.toString().substring(1));
            return new AppendIterator(super.getImplementationClassDescs(desc),
                                      java.util.Collections.singleton(u).iterator());
        }
        return super.getImplementationClassDescs(desc);
    }
    
    public ObjectTraverser getObjectTraverser() {
        return sun15_win32ObjectTraverser.INSTANCE;
    }
    
    public static class sun15_win32ObjectTraverser extends sun142_win32ObjectTraverser {
        public static sun15_win32ObjectTraverser INSTANCE = new sun15_win32ObjectTraverser();
        protected sun15_win32ObjectTraverser() {}
        public void initialize() {
            super.initialize();
            
            jq_Class k;

            // jdk1.5 caches name string.
            k = (jq_Class) PrimordialClassLoader.getJavaLangClass();
            nullInstanceFields.add(k.getOrCreateInstanceField("name", "Ljava/lang/String;"));
            
            // jdk1.5 caches field accessors.
            k = (jq_Class) PrimordialClassLoader.getJavaLangReflectField();
            nullInstanceFields.add(k.getOrCreateInstanceField("fieldAccessor", "Lsun/reflect/FieldAccessor;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("overrideFieldAccessor", "Lsun/reflect/FieldAccessor;"));
            
            // crashes on reflective access.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Lsun/reflect/UnsafeStaticFieldAccessorImpl;");
            nullInstanceFields.add(k.getOrCreateInstanceField("base", "Ljava/lang/Object;"));

            // leads to sun.security.rsa junk
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Lsun/security/jca/Providers;");
            nullStaticFields.add(k.getOrCreateStaticField("threadLists", "Ljava/lang/ThreadLocal;"));
            nullStaticFields.add(k.getOrCreateStaticField("providerList", "Lsun/security/jca/ProviderList;"));

            // leads to sun.security.provider.certpath junk
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Lsun/net/www/protocol/jar/JarFileFactory;");
            nullStaticFields.add(k.getOrCreateStaticField("fileCache", "Ljava/util/HashMap;"));
            nullStaticFields.add(k.getOrCreateStaticField("urlCache", "Ljava/util/HashMap;"));
            
            // we need to reinitialize in/out/err on startup.
            if (jq.on_vm_startup != null) {
                Object[] args = { } ;
                k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/FileDescriptor;");
                joeq.Class.jq_Method init_fd = k.getOrCreateStaticMethod("init", "()V");
                joeq.Bootstrap.MethodInvocation mi = new joeq.Bootstrap.MethodInvocation(init_fd, args);
                jq.on_vm_startup.add(mi);
                System.out.println("Added call to reinitialize in/out/err file descriptors on joeq startup: "+mi);
            }
        }
        
        /*
        public java.lang.Object mapInstanceField(java.lang.Object o, joeq.Class.jq_InstanceField f) {
            if (o instanceof FileDescriptor) {
                String fieldName = f.getName().toString();
                if (fieldName.equals("fd")) {
                    if (o == FileDescriptor.in) return Integer.valueOf(0);
                    if (o == FileDescriptor.out) return Integer.valueOf(1);
                    if (o == FileDescriptor.err) return Integer.valueOf(2);
                    System.out.println("File descriptor will be stale: "+o);
                }
            }
            return super.mapInstanceField(o, f);
        }
        */
        
        public static final jq_StaticField valueOffsetField;
        
        static {
            jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/concurrent/atomic/AtomicLong;");
            valueOffsetField = k.getOrCreateStaticField("valueOffset", "J");
        }
        
        public java.lang.Object mapStaticField(jq_StaticField f) {
            // valueOffset is the offset of the "value" field in AtomicLong
            if (f == valueOffsetField) {
                jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/concurrent/atomic/AtomicLong;");
                k.prepare();
                int offset = ((jq_InstanceField) k.getDeclaredMember("value", "J")).getOffset();
                return new Long(offset);
            }
            return super.mapStaticField(f);
        }
    }
}
