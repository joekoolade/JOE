// ListWrapper.java, created Wed Mar  5  0:26:32 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Util.Templates;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ListWrapper.java,v 1.5 2004/03/09 21:56:18 jwhaley Exp $
 */
public abstract class ListWrapper {
        
    public static class BasicBlock extends java.util.AbstractList implements List.BasicBlock {
        private final java.util.List/*<joeq.Compiler.Quad.BasicBlock>*/ a;
        public BasicBlock(java.util.List/*<joeq.Compiler.Quad.BasicBlock>*/ c) { this.a = c; }
        public int size() { return a.size(); }
        public Object get(int index) { return a.get(index); }
        public joeq.Compiler.Quad.BasicBlock getBasicBlock(int index) { return (joeq.Compiler.Quad.BasicBlock)a.get(index); }
        public void add(int i, Object o) { a.add(i, o); }
        public Object set(int i, Object o) { return a.set(i, o); }
        public Object remove(int i) { return a.remove(i); }
        public ListIterator.BasicBlock basicBlockIterator() { return new Iterator(a.listIterator()); }
        public static class Iterator implements ListIterator.BasicBlock {
            private java.util.ListIterator/*<joeq.Compiler.Quad.BasicBlock>*/ i;
            public Iterator(java.util.ListIterator/*<joeq.Compiler.Quad.BasicBlock>*/ l) { this.i = l; }
            public boolean hasNext() { return i.hasNext(); }
            public boolean hasPrevious() { return i.hasPrevious(); }
            public int nextIndex() { return i.nextIndex(); }
            public int previousIndex() { return i.previousIndex(); }
            public Object next() { return i.next(); }
            public joeq.Compiler.Quad.BasicBlock nextBasicBlock() { return (joeq.Compiler.Quad.BasicBlock)i.next(); }
            public Object previous() { return i.previous(); }
            public joeq.Compiler.Quad.BasicBlock previousBasicBlock() { return (joeq.Compiler.Quad.BasicBlock)i.previous(); }
            public void remove() { i.remove(); }
            public void set(Object o) { i.set(o); }
            public void add(Object o) { i.add(o); }
        }
        public static class EmptyIterator implements ListIterator.BasicBlock {
            private EmptyIterator() {}
            public boolean hasNext() { return false; }
            public boolean hasPrevious() { return false; }
            public int nextIndex() { return 0; }
            public int previousIndex() { return -1; }
            public Object next() { throw new java.util.NoSuchElementException(); }
            public joeq.Compiler.Quad.BasicBlock nextBasicBlock() { throw new java.util.NoSuchElementException(); }
            public Object previous() { throw new java.util.NoSuchElementException(); }
            public joeq.Compiler.Quad.BasicBlock previousBasicBlock() { throw new java.util.NoSuchElementException(); }
            public void remove() { throw new java.lang.IllegalStateException(); }
            public void set(Object o) { throw new java.lang.IllegalStateException(); }
            public void add(Object o) { throw new java.lang.UnsupportedOperationException(); }
            public static EmptyIterator INSTANCE = new EmptyIterator();
        }
    }
        
    public static class Quad extends java.util.AbstractList implements List.Quad {
        private final java.util.List/*<joeq.Compiler.Quad.Quad>*/ a;
        public Quad(java.util.List/*<joeq.Compiler.Quad.Quad>*/ c) { this.a = c; }
        public int size() { return a.size(); }
        public Object get(int index) { return a.get(index); }
        public joeq.Compiler.Quad.Quad getQuad(int index) { return (joeq.Compiler.Quad.Quad)a.get(index); }
        public void add(int i, Object o) { a.add(i, o); }
        public Object set(int i, Object o) { return a.set(i, o); }
        public Object remove(int i) { return a.remove(i); }
        public ListIterator.Quad quadIterator() { return new Iterator(a.listIterator()); }
        public static class Iterator implements ListIterator.Quad {
            private java.util.ListIterator/*<joeq.Compiler.Quad.Quad>*/ i;
            public Iterator(java.util.ListIterator/*<joeq.Compiler.Quad.Quad>*/ l) { this.i = l; }
            public boolean hasNext() { return i.hasNext(); }
            public boolean hasPrevious() { return i.hasPrevious(); }
            public int nextIndex() { return i.nextIndex(); }
            public int previousIndex() { return i.previousIndex(); }
            public Object next() { return i.next(); }
            public joeq.Compiler.Quad.Quad nextQuad() { return (joeq.Compiler.Quad.Quad)i.next(); }
            public Object previous() { return i.previous(); }
            public joeq.Compiler.Quad.Quad previousQuad() { return (joeq.Compiler.Quad.Quad)i.previous(); }
            public void remove() { i.remove(); }
            public void set(Object o) { i.set(o); }
            public void add(Object o) { i.add(o); }
        }
        public static class EmptyIterator implements ListIterator.Quad {
            private EmptyIterator() {}
            public boolean hasNext() { return false; }
            public boolean hasPrevious() { return false; }
            public int nextIndex() { return 0; }
            public int previousIndex() { return -1; }
            public Object next() { throw new java.util.NoSuchElementException(); }
            public joeq.Compiler.Quad.Quad nextQuad() { throw new java.util.NoSuchElementException(); }
            public Object previous() { throw new java.util.NoSuchElementException(); }
            public joeq.Compiler.Quad.Quad previousQuad() { throw new java.util.NoSuchElementException(); }
            public void remove() { throw new java.lang.IllegalStateException(); }
            public void set(Object o) { throw new java.lang.IllegalStateException(); }
            public void add(Object o) { throw new java.lang.UnsupportedOperationException(); }
            public static EmptyIterator INSTANCE = new EmptyIterator();
        }
    }
        
    public static class ExceptionHandler extends java.util.AbstractList implements List.ExceptionHandler {
        private final java.util.List/*<joeq.Compiler.Quad.ExceptionHandler>*/ a;
        public ExceptionHandler(java.util.List/*<joeq.Compiler.Quad.ExceptionHandler>*/ c) { this.a = c; }
        public int size() { return a.size(); }
        public Object get(int index) { return a.get(index); }
        public joeq.Compiler.Quad.ExceptionHandler getExceptionHandler(int index) { return (joeq.Compiler.Quad.ExceptionHandler)a.get(index); }
        public void add(int i, Object o) { a.add(i, o); }
        public Object set(int i, Object o) { return a.set(i, o); }
        public Object remove(int i) { return a.remove(i); }
        public ListIterator.ExceptionHandler exceptionHandlerIterator() { return new Iterator(a.listIterator()); }
        public static class Iterator implements ListIterator.ExceptionHandler {
            private java.util.ListIterator/*<joeq.Compiler.Quad.ExceptionHandler>*/ i;
            public Iterator(java.util.ListIterator/*<joeq.Compiler.Quad.ExceptionHandler>*/ l) { this.i = l; }
            public boolean hasNext() { return i.hasNext(); }
            public boolean hasPrevious() { return i.hasPrevious(); }
            public int nextIndex() { return i.nextIndex(); }
            public int previousIndex() { return i.previousIndex(); }
            public Object next() { return i.next(); }
            public joeq.Compiler.Quad.ExceptionHandler nextExceptionHandler() { return (joeq.Compiler.Quad.ExceptionHandler)i.next(); }
            public Object previous() { return i.previous(); }
            public joeq.Compiler.Quad.ExceptionHandler previousExceptionHandler() { return (joeq.Compiler.Quad.ExceptionHandler)i.previous(); }
            public void remove() { i.remove(); }
            public void set(Object o) { i.set(o); }
            public void add(Object o) { i.add(o); }
        }
        public static class EmptyIterator implements ListIterator.ExceptionHandler {
            private EmptyIterator() {}
            public boolean hasNext() { return false; }
            public boolean hasPrevious() { return false; }
            public int nextIndex() { return 0; }
            public int previousIndex() { return -1; }
            public Object next() { throw new java.util.NoSuchElementException(); }
            public joeq.Compiler.Quad.ExceptionHandler nextExceptionHandler() { throw new java.util.NoSuchElementException(); }
            public Object previous() { throw new java.util.NoSuchElementException(); }
            public joeq.Compiler.Quad.ExceptionHandler previousExceptionHandler() { throw new java.util.NoSuchElementException(); }
            public void remove() { throw new java.lang.IllegalStateException(); }
            public void set(Object o) { throw new java.lang.IllegalStateException(); }
            public void add(Object o) { throw new java.lang.UnsupportedOperationException(); }
            public static EmptyIterator INSTANCE = new EmptyIterator();
        }
    }
}
