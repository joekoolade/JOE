// Proxy.java, created Tue Dec 10 14:01:57 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang.reflect;

/**
 * Proxy
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Proxy.java,v 1.3 2004/03/09 06:26:25 jwhaley Exp $
 */
class Proxy {
    private static java.lang.Class defineClass0(joeq.ClassLib.Common.java.lang.ClassLoader cl, java.lang.String s, byte[] b, int i, int j) {
        return cl.defineClass0(s, b, i, j, null);
    }
}
