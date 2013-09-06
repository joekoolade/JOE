// Throwable.java, created Fri Aug 16 18:11:49 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_linux.java.lang;

import joeq.Allocator.CodeAllocator;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Method;
import joeq.Memory.CodeAddress;
import joeq.Runtime.ExceptionDeliverer;
import joeq.UTF.Utf8;

/**
 * Throwable
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Throwable.java,v 1.14 2004/05/07 08:11:01 joewhaley Exp $
 */
public abstract class Throwable {
    
    private java.lang.Object backtrace;
    
    // native method implementations
    private int getStackTraceDepth() {
        ExceptionDeliverer.StackFrame backtrace = (ExceptionDeliverer.StackFrame)this.backtrace;
        int i=-1;
        while (backtrace != null) { backtrace = backtrace.getNext(); ++i; }
        if (i == -1) i = 0;
        return i;
    }
    
    private StackTraceElement getStackTraceElement(int i) {
        ExceptionDeliverer.StackFrame backtrace = (ExceptionDeliverer.StackFrame)this.backtrace;
        while (--i >= 0) { backtrace = backtrace.getNext(); }
        java.lang.String declaringClass = "";
        java.lang.String methodName = "";
        java.lang.String fileName = null;
        int lineNumber = -2;
        CodeAddress ip = backtrace.getIP();
        jq_CompiledCode cc = CodeAllocator.getCodeContaining(ip);
        if (cc != null) {
            jq_Method m = cc.getMethod();
            if (m != null) {
                declaringClass = m.getDeclaringClass().getJDKName();
                methodName = m.getName().toString();
                //int code_offset = ip.difference(cc.getStart());
                Utf8 fn = m.getDeclaringClass().getSourceFile();
                if (fn != null) fileName = fn.toString();
                int bc_index = cc.getBytecodeIndex(ip);
                lineNumber = m.getLineNumber(bc_index);
            }
        }
        return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }

    public java.lang.Throwable fillInStackTrace() {
        this.backtrace = ExceptionDeliverer.getStackTrace();
        java.lang.Object o = this;
        return (java.lang.Throwable)o;
    }

    public java.lang.Object getBacktraceObject() { return this.backtrace; }
}
