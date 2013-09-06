// Interface.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun142_win32;

import java.util.Collections;
import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.ClassLib.ClassLibInterface;
import joeq.Runtime.ObjectTraverser;
import jwutil.collections.AppendIterator;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Interface.java,v 1.9 2004/09/30 03:35:33 joewhaley Exp $
 */
public class Interface extends joeq.ClassLib.sun14_win32.Interface {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && desc.toString().startsWith("Ljava/")) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/sun142_win32/"+desc.toString().substring(1));
            return new AppendIterator(super.getImplementationClassDescs(desc),
                                      Collections.singleton(u).iterator());
        }
        return super.getImplementationClassDescs(desc);
    }
    
    public ObjectTraverser getObjectTraverser() {
        return sun142_win32ObjectTraverser.INSTANCE;
    }
    
    public static class sun142_win32ObjectTraverser extends sun14_win32ObjectTraverser {
        public static sun142_win32ObjectTraverser INSTANCE = new sun142_win32ObjectTraverser();
        protected sun142_win32ObjectTraverser() {}
        public void initialize() {
            super.initialize();
            
            jq_Class k;
            try {
                k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/ClassLib/Common/java/util/zip/DeflaterHuffman;");
                k.load();
            } catch (NoClassDefFoundError _) {
                System.err.println("Error preloading DeflaterHuffman class");
            }
            
            // used during bootstrapping.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/ObjectInputStream$GetFieldImpl;");
            k.load();
            
            // jdk1.4.2_05 caches name string.
            k = (jq_Class) PrimordialClassLoader.getJavaLangClass();
            nullInstanceFields.add(k.getOrCreateInstanceField("name", "Ljava/lang/String;"));
            
            // 1.4.2 adds caches to Win32FileSystem, which we should not reflectively inspect.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/Win32FileSystem;");
            nullInstanceFields.add(k.getOrCreateInstanceField("cache", "Ljava/io/ExpiringCache;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("prefixCache", "Ljava/io/ExpiringCache;"));
            
            // reference these now, so that they are not added during bootimage write.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/ExpiringCache;");
            k.load();
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/ExpiringCache$Entry;");
        }
    }
}
