// AppendIterator.java, created Wed Mar  5  0:26:27 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Util.Collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: AppendIterator.java,v 1.2 2003/05/12 10:05:21 joewhaley Exp $
 */
public class AppendIterator implements Iterator {

    private final Iterator iterator1;
    private final Iterator iterator2;
    private boolean which;
    
    /** Creates new AppendIterator */
    public AppendIterator(Iterator iter1, Iterator iter2) {
        if (iter1 == null) {
            iterator1 = iter2; iterator2 = null;
        } else {
            iterator1 = iter1; iterator2 = iter2;
        }
        which = false;
    }

    public Object next() {
        if (which) {
            return iterator2.next();
        } else if (iterator1.hasNext()) {
            return iterator1.next();
        } else if (iterator2 != null) {
            which = true; return iterator2.next();
        } else throw new NoSuchElementException();
    }
    
    public boolean hasNext() {
        if (which || ((iterator2 != null) && !iterator1.hasNext())) {
            return iterator2.hasNext();
        } else {
            return iterator1.hasNext();
        }
    }
    
    public void remove() {
        if (!which) iterator1.remove();
        else iterator2.remove();
    }
    
}
