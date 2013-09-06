// ExceptionHandlerList.java, created Fri Jan 11 17:28:36 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.Collections;

/**
 * List of exception handlers for a bytecode CFG.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ExceptionHandlerList.java,v 1.1 2005/05/28 11:14:27 joewhaley Exp $
 */
public class ExceptionHandlerList {

    final ExceptionHandler exception_handler;
    ExceptionHandlerList parent;
    
    ExceptionHandlerList(ExceptionHandler exception_handler, ExceptionHandlerList parent) {
        this.exception_handler = exception_handler;
        this.parent = parent;
    }
    
    public ExceptionHandler getHandler() { return exception_handler; }
    public ExceptionHandlerList getParent() { return parent; }

    public ExceptionHandlerIterator iterator() {
        return new ExceptionHandlerIterator(Collections.singletonList(exception_handler), parent);
    }
}
