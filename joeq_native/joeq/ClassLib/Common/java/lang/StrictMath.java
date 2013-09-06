// StrictMath.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.Support.JMath;

/**
 * StrictMath
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: StrictMath.java,v 1.6 2004/03/09 06:26:22 jwhaley Exp $
 */
abstract class StrictMath extends java.lang.Object {

    // native method implementations
    public static double sin(double a) { return JMath.sin(a); }
    public static double cos(double a) { return JMath.cos(a); }
    public static double tan(double a) { return JMath.tan(a); }
    public static double asin(double a) { return JMath.asin(a); }
    public static double acos(double a) { return JMath.acos(a); }
    public static double atan(double a) { return JMath.atan(a); }
    public static double exp(double a) { return JMath.exp(a); }
    public static double log(double a) { return JMath.log(a); }
    public static double sqrt(double a) { return JMath.sqrt(a); }
    public static double IEEEremainder(double f1, double f2) { return JMath.IEEEremainder(f1, f2); }
    public static double ceil(double a) { return JMath.ceil(a); }
    public static double floor(double a) { return JMath.floor(a); }
    public static double rint(double a) { return JMath.rint(a); }
    public static double atan2(double a, double b) { return JMath.atan2(a, b); }
    public static double pow(double a, double b) { return JMath.pow(a, b); }

}
