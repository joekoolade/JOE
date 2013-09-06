/*
 * Created on Sep 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPSSA.Utils;

import java.util.Iterator;
import jwutil.util.Assert;

/**
 * @author livshits
 * @version $Id: IteratorHelper.java,v 1.6 2004/09/22 22:17:25 joewhaley Exp $
 */
public class IteratorHelper {
    public static class SingleIterator implements Iterator {
        boolean _done;
        Object  _obj;
        
        public SingleIterator(Object obj){
            _done = false;
            _obj  = obj; 
        }
        public boolean hasNext() {
            return !_done;
        }

        public Object next() {
            Assert._assert(!_done);
            return _obj;
        }

        public void remove() {
            Assert._assert(false);            
        }    
    }
    
    public static class EmptyIterator implements Iterator {
        private EmptyIterator(){}
        public boolean hasNext() {return false;}
        public Object next() {
            Assert._assert(false);
            return null;
        }
        public void remove() {
            Assert._assert(false);            
        }
        
        public static class FACTORY {
            static EmptyIterator _sample = null;
            
            public static EmptyIterator get(){
                if(_sample == null){
                    _sample = new EmptyIterator();
                }
                
                return _sample;
            }
        }
    }
}
