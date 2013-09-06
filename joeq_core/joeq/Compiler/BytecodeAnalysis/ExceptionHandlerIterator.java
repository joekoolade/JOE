// ExceptionHandlerIterator.java, created Fri Jan 11 16:49:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import jwutil.collections.AppendListIterator;

/**
 * Iterator for exception handlers in a bytecode CFG.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ExceptionHandlerIterator.java,v 1.9 2005/05/28 11:14:27 joewhaley Exp $
 */
public class ExceptionHandlerIterator implements ListIterator {

    private final AppendListIterator iterator;
    
    /** Creates new ExceptionHandlerIterator */
    public ExceptionHandlerIterator(List exs, ExceptionHandlerList parent) {
        ListIterator l2 = parent==null?null:parent.iterator();
        iterator = new AppendListIterator(exs.listIterator(), l2);
    }
    private ExceptionHandlerIterator() {
        iterator = null;
    }
    
    public boolean hasPrevious() { return iterator.hasPrevious(); }
    public boolean hasNext() { return iterator.hasNext(); }
    public Object previous() { return iterator.previous(); }
    public Object next() { return iterator.next(); }
    public int previousIndex() { return iterator.previousIndex(); }
    public int nextIndex() { return iterator.nextIndex(); }
    public void remove() { iterator.remove(); }
    public void set(Object o) { iterator.set(o); }
    public void add(Object o) { iterator.add(o); }
    
    public ExceptionHandler prevEH() { return (ExceptionHandler)previous(); }
    public ExceptionHandler nextEH() { return (ExceptionHandler)next(); }
    
    public static ExceptionHandlerIterator nullIterator() {
        return new ExceptionHandlerIterator() {
            public boolean hasPrevious() { return false; }
            public boolean hasNext() { return false; }
            public Object previous() { throw new NoSuchElementException(); }
            public Object next() { throw new NoSuchElementException(); }
            public int previousIndex() { return -1; }
            public int nextIndex() { return 0; }
            public void remove() { throw new IllegalStateException(); }
            public void set(Object o) { throw new IllegalStateException(); }
            public void add(Object o) { throw new UnsupportedOperationException(); }
            public ExceptionHandler prevEH() { throw new NoSuchElementException(); }
            public ExceptionHandler nextEH() { throw new NoSuchElementException(); }
        };
    }
}
