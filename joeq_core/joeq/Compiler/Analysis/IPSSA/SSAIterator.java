/*
 * Created on Dec 4, 2003
 *
 * Define some typed iterators.
 */
package joeq.Compiler.Analysis.IPSSA;

import java.util.Iterator;
import jwutil.collections.UnmodifiableIterator;

/**
 * @author V.Benjamin Livshits
 * @version $Id: SSAIterator.java,v 1.8 2004/09/22 22:17:33 joewhaley Exp $
 */
public class SSAIterator {
    public static class DefinitionIterator extends UnmodifiableIterator {
        private Iterator _iter;
        
        public DefinitionIterator(Iterator iter) {
            this._iter = iter;
        }
        
        public boolean hasNext() {
            return _iter.hasNext();
        }
    
        public Object next() {
            return _iter.next();
        }
        
        public SSADefinition nextDefinition() {
            return (SSADefinition) _iter.next();
        }
    }
    
    public static class ValueIterator extends UnmodifiableIterator {
        private Iterator _iter;
        
        public ValueIterator(Iterator iter) {
            this._iter = iter;
        }
        
        public boolean hasNext() {
            return _iter.hasNext();
        }
    
        public Object next() {
            return _iter.next();
        }
        
        public SSAValue nextValue() {
            return (SSAValue) _iter.next();
        }
    }
    
    public static class BindingIterator extends UnmodifiableIterator {
        private Iterator _iter;
    
        public BindingIterator(Iterator iter) {
            this._iter = iter;
        }
    
        public boolean hasNext() {
            return _iter.hasNext();
        }

        public Object next() {
            return _iter.next();
        }
    
        public SSABinding nextBinding() {
            return (SSABinding) _iter.next();
        }
    }
}