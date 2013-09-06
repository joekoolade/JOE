// String.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.UTF.Utf8;

/**
 * String
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: String.java,v 1.5 2004/03/09 06:26:22 jwhaley Exp $
 */
public abstract class String {

    public java.lang.String intern() {
        // note: this relies on the caching of String objects in Utf8 class
        java.lang.Object o = this;
        return Utf8.get((java.lang.String)o).toString();
    }

}
