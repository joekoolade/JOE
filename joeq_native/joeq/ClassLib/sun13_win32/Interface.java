// Interface.java, created Fri Jan 11 17:11:52 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun13_win32;

import java.util.Collections;
import java.util.Iterator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.ClassLib.ClassLibInterface;
import joeq.Runtime.ObjectTraverser;
import joeq.Runtime.SystemInterface;
import joeq.Scheduler.jq_NativeThread;
import joeq.UTF.Utf8;
import jwutil.collections.AppendIterator;

/**
 * Interface
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Interface.java,v 1.27 2004/09/30 03:35:37 joewhaley Exp $
 */
public final class Interface extends joeq.ClassLib.Common.InterfaceImpl {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && desc.toString().startsWith("Ljava/")) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/sun13_win32/"+desc.toString().substring(1));
            return new AppendIterator(super.getImplementationClassDescs(desc),
                                      Collections.singleton(u).iterator());
        }
        return super.getImplementationClassDescs(desc);
    }
    
    public ObjectTraverser getObjectTraverser() {
        return sun13_win32ObjectTraverser.INSTANCE;
    }
    
    public static class sun13_win32ObjectTraverser extends CommonObjectTraverser {
        public static sun13_win32ObjectTraverser INSTANCE = new sun13_win32ObjectTraverser();
        protected sun13_win32ObjectTraverser() {}
        public void initialize() {
            super.initialize();
            jq_NativeThread.USE_INTERRUPTER_THREAD = true;
        }
        public java.lang.Object mapInstanceField(java.lang.Object o, joeq.Class.jq_InstanceField f) {
            if (IGNORE_THREAD_LOCALS) {
                jq_Class c = f.getDeclaringClass();
                if (c == PrimordialClassLoader.getJavaLangThread()) {
                    String fieldName = f.getName().toString();
                    if (fieldName.equals("threadLocals") || fieldName.equals("inheritableThreadLocals")) {
                        // 1.3.1_05 and greater uses the 1.4 implementation of thread locals.
                        // see Sun BugParade id#4667411.
                        // we check the type of the field and return the appropriate object.
                        if (f.getType().getDesc() == Utf8.get("Ljava/util/Map;")) {
                            return java.util.Collections.EMPTY_MAP;
                        } else if (f.getType().getDesc() == Utf8.get("Ljava/lang/ThreadLocal$ThreadLocalMap;")) {
                            return null;
                        } else {
                            SystemInterface.debugwriteln("Unknown type for field "+f);
                            return null;
                        }
                    }
                }
            }
            return super.mapInstanceField(o, f);
        }
    }
    
}
