// Interface.java, created Wed Feb  4 12:10:06 PST 2004
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.pa;

import java.util.Iterator;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_StaticField;
import joeq.ClassLib.ClassLibInterface;
import joeq.Runtime.ObjectTraverser;
import jwutil.util.Assert;

/*
 * Classes we replace for pointer analysis purposes ('pa')
 * If we can model the effect of a native method from the java.* hierarchy
 * in Java code, we can add an implementation whose bytecode is then
 * analyzed.
 *
 * @author  Godmar Back <gback@stanford.edu>
 * @version $Id: Interface.java,v 1.6 2004/09/30 03:35:31 joewhaley Exp $
 */
public final class Interface extends joeq.ClassLib.Common.InterfaceImpl {

    /** Creates new Interface */
    public Interface() {}

    public Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        if (ClassLibInterface.USE_JOEQ_CLASSLIB && desc.toString().startsWith("Ljava/")) {
            joeq.UTF.Utf8 u = joeq.UTF.Utf8.get("Ljoeq/ClassLib/pa/"+desc.toString().substring(1));
            return java.util.Collections.singleton(u).iterator();
        }
        return java.util.Collections.EMPTY_SET.iterator();
    }

    public ObjectTraverser getObjectTraverser() {
        return new ObjectTraverser() {
            public void initialize() { }
            public Object mapStaticField(jq_StaticField f) { Assert.UNREACHABLE(); return null; }
            public Object mapInstanceField(Object o, jq_InstanceField f) { Assert.UNREACHABLE(); return null; }
            public Object mapValue(Object o) { Assert.UNREACHABLE(); return null; }
        };
    }
}
