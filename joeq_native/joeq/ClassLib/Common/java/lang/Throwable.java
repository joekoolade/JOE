// Throwable.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.Runtime.ExceptionDeliverer;
import jwutil.util.Assert;

/**
 * Throwable
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Throwable.java,v 1.10 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class Throwable {
    
    private java.lang.Object backtrace;
    
    // native method implementations
    private void printStackTrace0(java.lang.Object s) {
        java.lang.Object backtrace = this.backtrace;
        if (s instanceof java.io.PrintWriter)
            ExceptionDeliverer.printStackTrace(backtrace, (java.io.PrintWriter)s);
        else if (s instanceof java.io.PrintStream)
            ExceptionDeliverer.printStackTrace(backtrace, (java.io.PrintStream)s);
        else
            Assert.UNREACHABLE();
    }
    
    public java.lang.Throwable fillInStackTrace() {
        this.backtrace = ExceptionDeliverer.getStackTrace();
        java.lang.Object o = this;
        return (java.lang.Throwable)o;
    }

    public java.lang.Object getBacktraceObject() { return this.backtrace; }
}
