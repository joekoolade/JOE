// Interface.java, created Fri Jan 11 17:07:15 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.ibm13_linux;

import java.util.Collections;
import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.ClassLib.ClassLibInterface;
import joeq.Main.jq;
import joeq.Runtime.ObjectTraverser;
import jwutil.collections.AppendIterator;

/**
 * Interface
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Interface.java,v 1.30 2004/09/30 03:35:37 joewhaley Exp $
 */
public final class Interface extends joeq.ClassLib.Common.InterfaceImpl {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && (desc.toString().startsWith("Ljava/") ||
                                                    desc.toString().startsWith("Lcom/ibm/jvm/"))) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/ibm13_linux/"+desc.toString().substring(1));
            return new AppendIterator(super.getImplementationClassDescs(desc),
                                      Collections.singleton(u).iterator());
        }
        return super.getImplementationClassDescs(desc);
    }
    
    public ObjectTraverser getObjectTraverser() {
        return ibm13_linuxObjectTraverser.INSTANCE;
    }
    
    public static class ibm13_linuxObjectTraverser extends CommonObjectTraverser {
        public static ibm13_linuxObjectTraverser INSTANCE = new ibm13_linuxObjectTraverser();
        protected ibm13_linuxObjectTraverser() {}
        public void initialize() {
            super.initialize();
            
            // access the ISO-8859-1 character encoding, as it is used during bootstrapping
            try {
                new String(new byte[0], 0, 0, "ISO-8859-1");
            } catch (java.io.UnsupportedEncodingException x) {}
            PrimordialClassLoader.loader.getOrCreateBSType("Lsun/io/CharToByteISO8859_1;");
    
            jq_Class k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/net/URLClassLoader$ClassFinder;");
            nullInstanceFields.add(k.getOrCreateInstanceField("name", "Ljava/lang/String;"));
            
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Lsun/misc/Launcher;");
            nullStaticFields.add(k.getOrCreateStaticField("launcher", "Lsun/misc/Launcher;"));
            //k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/net/URLClassLoader;");
            //nullStaticFields.add(k.getOrCreateStaticField("extLoader", "Ljava/net/URLClassLoader;"));
            k = PrimordialClassLoader.getJavaLangString();
            nullStaticFields.add(k.getOrCreateStaticField("btcConverter", "Ljava/lang/ThreadLocal;"));
            nullStaticFields.add(k.getOrCreateStaticField("ctbConverter", "Ljava/lang/ThreadLocal;"));
            k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/zip/ZipFile;");
            nullStaticFields.add(k.getOrCreateStaticField("inflaters", "Ljava/util/Vector;"));
            
            // we need to reinitialize the inflaters array on startup.
            if (jq.on_vm_startup != null) {
                Object[] args = { } ;
                joeq.Class.jq_Method init_inflaters = k.getOrCreateStaticMethod("init_inflaters", "()V");
                joeq.Bootstrap.MethodInvocation mi = new joeq.Bootstrap.MethodInvocation(init_inflaters, args);
                jq.on_vm_startup.add(mi);
                System.out.println("Added call to reinitialize java.util.zip.ZipFile.inflaters field on joeq startup: "+mi);
            }
        }
        
        public java.lang.Object mapInstanceField(java.lang.Object o, joeq.Class.jq_InstanceField f) {
            if (IGNORE_THREAD_LOCALS) {
                jq_Class c = f.getDeclaringClass();
                if (c == PrimordialClassLoader.getJavaLangThread()) {
                    String fieldName = f.getName().toString();
                    if (fieldName.equals("threadLocals"))
                        return java.util.Collections.EMPTY_MAP;
                    if (fieldName.equals("inheritableThreadLocals"))
                        return java.util.Collections.EMPTY_MAP;
                }
            }
            return super.mapInstanceField(o, f);
        }
    }
}
