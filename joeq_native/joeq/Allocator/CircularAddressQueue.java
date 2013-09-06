// CircularAddressQueue.java, created Aug 3, 2004 3:29:21 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.SystemInterface;
import jwutil.util.Assert;

/**
 * An implementation of an address queue that uses a circular buffer.
 * 
 * @author John Whaley
 * @version $Id: CircularAddressQueue.java,v 1.5 2004/09/30 03:35:30 joewhaley Exp $
 */
public class CircularAddressQueue implements AddressQueue {
    
    public static final boolean TRACE = false;
    
    /**
     * Size of block (in words) to allocate when we need more space
     * in a queue.
     */
    public static int QUEUE_WORDS = 262144;
    
    /**
     * Queue pointers.
     */
    HeapAddress queueStart, queueEnd;
    
    /**
     * Pointers to start and end of memory block.
     */
    HeapAddress blockStart, blockEnd;
    
    /**
     * Create a new CircularAddressQueue.
     * Does not allocate any memory yet.
     */
    public CircularAddressQueue() {
        super();
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#free()
     */
    public void free() {
        if (TRACE) Debug.writeln("Freeing work queue.");
        if (!blockStart.isNull()) {
            SystemInterface.sysfree(blockStart);
            blockStart = blockEnd = queueStart = queueEnd = HeapAddress.getNull();
        }
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#growQueue(int)
     */
    public void growQueue(int words) {
        // todo: use realloc here.
        if (true) Debug.writeln("Growing work queue to size ", words * HeapAddress.size());
        HeapAddress new_queue = (HeapAddress) SystemInterface.syscalloc(words * HeapAddress.size());
        if (TRACE) Debug.writeln("New Queue start: ", new_queue);
        if (new_queue.isNull())
            HeapAllocator.outOfMemory();
        if (TRACE) Debug.writeln("Queue start: ", queueStart);
        if (TRACE) Debug.writeln("Queue end: ", queueEnd);
        if (TRACE) Debug.writeln("Block start: ", blockStart);
        if (TRACE) Debug.writeln("Block end: ", blockEnd);
        int size = queueEnd.difference(queueStart);
        if (size > 0) {
            if (TRACE) Debug.writeln("Current size ", size);
            Assert._assert(words * HeapAddress.size() > size);
            SystemInterface.mem_cpy(new_queue, queueStart, size);
        } else {
            if (TRACE) Debug.writeln("Pointers are flipped");
            int size2 = blockEnd.difference(queueStart);
            if (TRACE) Debug.writeln("Size of first part: ", size2);
            SystemInterface.mem_cpy(new_queue, queueStart, size2);
            int size3 = queueEnd.difference(blockStart);
            if (TRACE) Debug.writeln("Size of second part: ", size3);
            SystemInterface.mem_cpy(new_queue.offset(size2), blockStart, size3);
            size = size2 + size3;
        }
        if (TRACE) Debug.writeln("Freeing old queue");
        SystemInterface.sysfree(blockStart);
        queueStart = blockStart = new_queue;
        blockEnd = (HeapAddress) blockStart.offset(words * HeapAddress.size());
        queueEnd = (HeapAddress) queueStart.offset(size);
        if (true) Debug.writeln("New Block end:", blockEnd);
        if (true) Debug.writeln("New Queue end:", queueEnd);
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#size()
     */
    public int size() {
        int size = queueEnd.difference(queueStart);
        if (size < 0) {
            size = blockEnd.difference(queueStart) +
                   queueEnd.difference(blockStart);
        }
        return size / HeapAddress.size();
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#space()
     */
    public int space() {
        int size = queueEnd.difference(queueStart);
        int space;
        if (size < 0) {
            space = -size;
        } else {
            space = blockEnd.difference(queueEnd) +
                    queueStart.difference(blockStart);
        }
        return space / HeapAddress.size();
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#push(joeq.Memory.Address)
     */
    public void push(Address a) {
        if (TRACE) Debug.writeln("Adding to queue: ", a);
        if (space() <= HeapAddress.size()) {
            // need a bigger work queue!
            int size = blockEnd.difference(blockStart);
            if (size == 0) size = QUEUE_WORDS;
            else size = (QUEUE_WORDS *= 2);
            growQueue(size);
        }
        if (TRACE) Debug.writeln("Adding at: ", queueEnd);
        queueEnd.poke(a);
        queueEnd = (HeapAddress) queueEnd.offset(HeapAddress.size());
        if (queueEnd.difference(blockEnd) == 0) {
            queueEnd = blockStart;
            if (TRACE) Debug.writeln("Queue end pointer wrapped around to: ", queueEnd);
        }
        Assert._assert(queueEnd.difference(queueStart) != 0);
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#pull()
     */
    public Address pull() {
        if (queueEnd.difference(queueStart) == 0) {
            return HeapAddress.getNull();
        }
        if (TRACE) Debug.writeln("Pulling from: ", queueStart);
        HeapAddress a = (HeapAddress) queueStart.peek();
        queueStart = (HeapAddress) queueStart.offset(HeapAddress.size());
        if (queueStart.difference(blockEnd) == 0) {
            queueStart = blockStart;
            if (TRACE) Debug.writeln("Queue start pointer wrapped around to: ", queueStart);
        }
        if (TRACE) Debug.writeln("Pulled from queue: ", a);
        return a;
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.AddressQueue#peek()
     */
    public Address peek() {
        if (queueEnd.difference(queueStart) == 0) {
            return HeapAddress.getNull();
        }
        Address a = (Address) queueStart.peek();
        if (TRACE) Debug.writeln("Peeked from queue: ", a);
        return a;
    }
}
