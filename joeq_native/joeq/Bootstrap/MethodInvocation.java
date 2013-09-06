// MethodInvocation.java, created Sun Mar 11  2:21:10 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import java.lang.reflect.InvocationTargetException;
import joeq.Class.jq_Method;
import joeq.Runtime.Reflection;

/**
 * MethodInvocation
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: MethodInvocation.java,v 1.9 2004/09/30 03:35:29 joewhaley Exp $
 */
public class MethodInvocation {

    jq_Method method;
    Object[] args;
    
    public MethodInvocation(jq_Method m, Object[] a) {
        this.method = m;
        this.args = a;
    }

    public long invoke() throws Throwable {
        try {
            return Reflection.invoke(method, null, args);
        } catch (InvocationTargetException x) {
            throw x.getTargetException();
        }
    }
    
    public String toString() {
        return "method "+method;
    }
}
