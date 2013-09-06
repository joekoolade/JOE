// AtomicLong.java, created Aug 9, 2003 3:47:06 AM by John Whaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.sun.misc;

/**
 * AtomicLong
 * 
 * @author John Whaley
 * @version $Id: AtomicLong.java,v 1.2 2004/03/09 06:26:30 jwhaley Exp $
 */
abstract class AtomicLong {
    private static boolean VMSupportsCS8() {
        return false;
    }
}
