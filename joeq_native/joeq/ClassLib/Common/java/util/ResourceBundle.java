// ResourceBundle.java, created Thu Jul  4  4:50:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.util;

import joeq.Class.jq_CompiledCode;
import joeq.Memory.StackAddress;
import joeq.Runtime.Reflection;
import joeq.Runtime.StackCodeWalker;
import jwutil.util.Assert;

/**
 * ResourceBundle
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ResourceBundle.java,v 1.13 2004/09/30 03:35:34 joewhaley Exp $
 */
abstract class ResourceBundle {

    private static Class[] getClassContext() {
        StackCodeWalker sw = new StackCodeWalker(null, StackAddress.getBasePointer());
        sw.gotoNext();
        int i;
        for (i=0; sw.hasNext(); ++i, sw.gotoNext()) ;
        Class[] classes = new Class[i];
        sw = new StackCodeWalker(null, StackAddress.getBasePointer());
        sw.gotoNext();
        for (i=0; sw.hasNext(); ++i, sw.gotoNext()) {
            jq_CompiledCode cc = sw.getCode();
            if (cc == null) classes[i] = null;
            else classes[i] = Reflection.getJDKType(cc.getMethod().getDeclaringClass());
        }
        Assert._assert(i == classes.length);
        return classes;
    }

}
