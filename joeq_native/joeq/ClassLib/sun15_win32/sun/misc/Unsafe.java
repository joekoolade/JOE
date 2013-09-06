// Unsafe.java, created Tue Dec 10 14:02:37 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun15_win32.sun.misc;

import joeq.Memory.HeapAddress;

/**
 * Unsafe
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Unsafe.java,v 1.2 2004/03/09 06:26:55 jwhaley Exp $
 */
public final class Unsafe {

    public boolean compareAndSwapLong(java.lang.Object o, long off, long a, long b) {
        HeapAddress p = (HeapAddress) HeapAddress.addressOf(o).offset((int)off);
        // todo: atomic cas8
        if (p.peek8() == a) {
            p.poke8(b);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean compareAndSwapInt(java.lang.Object o, long off, int a, int b) {
        HeapAddress p = (HeapAddress) HeapAddress.addressOf(o).offset((int)off);
        // todo: atomic cas4
        if (p.peek4() == a) {
            p.poke4(b);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean compareAndSwapObject(java.lang.Object o, long off, java.lang.Object a, java.lang.Object b) {
        HeapAddress p = (HeapAddress) HeapAddress.addressOf(o).offset((int)off);
        // todo: atomic cas4
        if (p.peek() == HeapAddress.addressOf(a)) {
            p.poke(HeapAddress.addressOf(b));
            return true;
        } else {
            return false;
        }
    }
}
