// Interface.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun142_linux;

import java.util.Collections;
import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.ClassLib.ClassLibInterface;
import joeq.Runtime.ObjectTraverser;
import jwutil.collections.AppendIterator;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Interface.java,v 1.9 2004/09/30 03:35:37 joewhaley Exp $
 */
public class Interface extends joeq.ClassLib.sun14_linux.Interface {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && desc.toString().startsWith("Ljava/")) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/sun142_linux/"+desc.toString().substring(1));
            return new AppendIterator(super.getImplementationClassDescs(desc),
                                      Collections.singleton(u).iterator());
        }
        return super.getImplementationClassDescs(desc);
    }
    
    public ObjectTraverser getObjectTraverser() {
        return sun142_linuxObjectTraverser.INSTANCE;
    }
    
    public static class sun142_linuxObjectTraverser extends sun14_linuxObjectTraverser {
        public static sun142_linuxObjectTraverser INSTANCE = new sun142_linuxObjectTraverser();
        protected sun142_linuxObjectTraverser() {}
        public void initialize() {
            super.initialize();
            
            jq_Class k;
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/ClassLib/Common/java/util/zip/DeflaterHuffman;");
            k.load();
            
            // used during bootstrapping.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/ObjectInputStream$GetFieldImpl;");
            k.load();
            
            // 1.4.2 adds caches to UnixFileSystem, which we should not reflectively inspect.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/UnixFileSystem;");
            nullInstanceFields.add(k.getOrCreateInstanceField("cache", "Ljava/io/ExpiringCache;"));
            nullInstanceFields.add(k.getOrCreateInstanceField("javaHomePrefixCache", "Ljava/io/ExpiringCache;"));
            
            // reference these now, so that they are not added during bootimage write.
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/ExpiringCache;");
            k.load();
            k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/ExpiringCache$Entry;");
        }
    }
}
