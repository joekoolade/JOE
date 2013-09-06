// ExceptionHandlerList.java, created Fri Jan 18 19:33:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Collection;
import java.util.Iterator;
import joeq.Class.jq_Class;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;
import joeq.Util.Templates.ListWrapper;

/**
 * Holds a list of exception handlers that protect a basic block.
 * It includes a reference to a parent exception handler list, to handle nesting
 * of exception handlers.
 * These form a tree structure where each node has a pointer to its parent.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @see  ExceptionHandler
 * @see  ExceptionHandlerIterator
 * @version  $Id: ExceptionHandlerList.java,v 1.13 2004/09/22 22:17:26 joewhaley Exp $
 */

public class ExceptionHandlerList extends java.util.AbstractList implements List.ExceptionHandler {

    /** The exception handler. */
    private ExceptionHandler exception_handler;
    /** The parent exception handler set. */
    ExceptionHandlerList parent;
    
    ExceptionHandlerList() {}

    /** Creates new ExceptionHandlerList containing the given exception handler and no parent set.
     * @param exception_handler  exception handler to include in the set. */
    public ExceptionHandlerList(ExceptionHandler exception_handler) {
        this.exception_handler = exception_handler;
        this.parent = null;
    }
    /** Creates new ExceptionHandlerList containing the given exception handler and parent set.
     * @param exception_handler  exception handler to include in the set.
     * @param parent  the parent set of exception handlers. */
    public ExceptionHandlerList(ExceptionHandler exception_handler, ExceptionHandlerList parent) {
        this.exception_handler = exception_handler;
        this.parent = parent;
    }

    /** Return the handler in this set.  Doesn't include parent handlers.
     * @return  the handler in this set, without parent handlers. */    
    public ExceptionHandler getHandler() { return exception_handler; }
    public void setHandler(ExceptionHandler eh) { exception_handler = eh; }
    /** Return the parent set of exception handlers, or null if this set doesn't have a parent.
     * @return  the parent set of exception handlers, or null if this set doesn't have a parent. */
    public ExceptionHandlerList getParent() { return parent; }
    public void setParent(ExceptionHandlerList p) { parent = p; }
    
    /** Returns the first exception handler in the list that MUST catch an
     * exception of the given type, or null if there is no handler that must catch it.
     * @return  the handler that must catch the exception type, or null if there is no such handler
     */
    public ExceptionHandler mustCatch(jq_Class exType) {
        ExceptionHandlerList p = this;
        while (p != null) {
            if (p.getHandler().mustCatch(exType)) return p.getHandler();
            p = p.getParent();
        }
        return null;
    }
    
    /** Returns the list of exception handlers in this list that MAY catch the given exception type.
     * @return  the list of handlers that may catch the exception type
     */
    public List.ExceptionHandler mayCatch(jq_Class exType) {
        java.util.List list = new java.util.LinkedList();
        ExceptionHandlerList p = this;
        while (p != null) {
            if (p.getHandler().mustCatch(exType)) {
                list.add(p.getHandler()); break;
            }
            if (p.getHandler().mayCatch(exType)) list.add(p.getHandler());
            p = p.getParent();
        }
        return new ListWrapper.ExceptionHandler(list);
    }
    
    /** Returns the list of exception handlers in this list that MAY catch the given exception type.
     * @return  the list of handlers that may catch the exception type
     */
    public List.ExceptionHandler mayCatch(Collection exTypes) {
        java.util.List list = new java.util.LinkedList();
        java.util.List eTypes = new java.util.LinkedList(exTypes);
        ExceptionHandlerList p = this;
        outer:
        while (p != null) {
            for (Iterator i = eTypes.iterator(); i.hasNext(); ) {
                jq_Class exType = (jq_Class) i.next();
                if (p.getHandler().mustCatch(exType)) {
                    list.add(p.getHandler());
                    i.remove();
                    if (eTypes.isEmpty()) break outer;
                    else continue;
                }
                if (p.getHandler().mayCatch(exType)) {
                    list.add(p.getHandler());
                }
            }
            p = p.getParent();
        }
        return new ListWrapper.ExceptionHandler(list);
    }
    
    /** Return an iteration over the handlers in this set (and the handlers in parent sets).
     * Handlers are returned in the correct order (this set, followed by parent sets.)
     * @return  an iteration over the handlers in this set (and the handlers in parent sets) in correct order. */
    public ListIterator.ExceptionHandler exceptionHandlerIterator() {
        return new ExceptionHandlerIterator(this);
    }
    public java.util.Iterator iterator() { return exceptionHandlerIterator(); }
    public java.util.ListIterator listIterator() { return exceptionHandlerIterator(); }
    
    public joeq.Compiler.Quad.ExceptionHandler getExceptionHandler(int index) {
        if (index < 0) throw new IndexOutOfBoundsException();
        ExceptionHandlerList p = this;
        while (--index >= 0) {
            if (p == null) throw new IndexOutOfBoundsException();
            p = p.parent;
        }
        return p.exception_handler;
    }
    
    public Object get(int index) { return getExceptionHandler(index); }
    
    public int size() {
        int size = 0;
        ExceptionHandlerList p = this;
        while (p != null) {
            ++size;
            p = p.parent;
        }
        return size;
    }
    
    public static ExceptionHandlerList getEmptyList() { return EMPTY; }
    public static final ExceptionHandlerList EMPTY = new ExceptionHandlerList(null) {
        public int size() { return 0; }
        public ListIterator.ExceptionHandler exceptionHandlerIterator() { return ExceptionHandlerIterator.getEmptyIterator(); }
        public ExceptionHandler mustCatch(jq_Class exType) { return null; }
        public List.ExceptionHandler mayCatch(jq_Class exType) { return getEmptyList(); }
    };
    
}
