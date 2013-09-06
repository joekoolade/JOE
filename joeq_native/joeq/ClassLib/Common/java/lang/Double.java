// Double.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.Runtime.Unsafe;

/**
 * Double
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Double.java,v 1.6 2004/03/09 21:57:34 jwhaley Exp $
 */
abstract class Double {

    // native method implementations.
    public static long doubleToLongBits(double value) {
        if (java.lang.Double.isNaN(value)) return 0x7ff8000000000000L;
        return Unsafe.doubleToLongBits(value);
    }
    public static long doubleToRawLongBits(double value) {
        return Unsafe.doubleToLongBits(value);
    }
    public static double longBitsToDouble(long bits) {
        return Unsafe.longBitsToDouble(bits);
    }
    
}
