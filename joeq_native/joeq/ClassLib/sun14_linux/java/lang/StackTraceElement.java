// StackTraceElement.java, created Fri Aug 16 18:11:48 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_linux.java.lang;

/**
 * StackTraceElement
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: StackTraceElement.java,v 1.4 2004/03/09 06:26:54 jwhaley Exp $
 */
public final class StackTraceElement {
    private java.lang.String declaringClass;
    private java.lang.String methodName;
    private java.lang.String fileName;
    private int lineNumber;

    StackTraceElement(java.lang.String declaringClass,
                      java.lang.String methodName,
                      java.lang.String fileName,
                      int lineNumber) {
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
}
