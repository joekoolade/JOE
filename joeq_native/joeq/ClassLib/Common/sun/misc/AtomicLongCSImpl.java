// AtomicLongCSImpl.java, created Aug 9, 2003 3:50:36 AM by John Whaley
// Copyright (C) 2003 John Whaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.sun.misc;

/**
 * AtomicLongCSImpl
 * 
 * @author John Whaley
 * @version $Id: AtomicLongCSImpl.java,v 1.3 2004/03/09 06:26:30 jwhaley Exp $
 */
abstract class AtomicLongCSImpl {
    private volatile long value;
    public boolean attemptUpdate(long a, long b) {
        // todo: atomic cas8
        if (value == a) {
            value = b;
            return true;
        } else {
            return false;
        }
    }
}
