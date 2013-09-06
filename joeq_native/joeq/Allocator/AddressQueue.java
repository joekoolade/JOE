// AddressQueue.java, created Aug 5, 2004 12:02:14 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import joeq.Memory.Address;

/**
 * AddressQueue
 * 
 * @author John Whaley
 * @version $Id: AddressQueue.java,v 1.1 2004/08/05 18:33:30 joewhaley Exp $
 */
public interface AddressQueue {
    /**
     * Free the memory associated with this reference queue.
     */
    void free();

    /**
     * Grows the queue to the specified size in words.
     * 
     * @param words
     */
    void growQueue(int words);

    /**
     * Return the number of elements in this queue.
     * 
     * @return number of elements in this queue
     */
    int size();

    /**
     * Return the amount of free space (in words) in this queue.
     * 
     * @return amount of free space (in words) in this queue
     */
    int space();

    /**
     * Add the given address to the reference queue.
     * 
     * @param a  address to add
     */
    void push(Address a);

    /**
     * Returns and removes an address from this queue, or returns
     * null if the queue is empty.
     * 
     * @return address, or null if queue is empty
     */
    Address pull();
    
    /**
     * Returns the first address from this queue, or null if
     * the queue is empty.
     * 
     * @return address, or null if queue is empty
     */
    Address peek();
}
