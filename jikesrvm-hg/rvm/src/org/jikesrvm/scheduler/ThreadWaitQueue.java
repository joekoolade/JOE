package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;

public class ThreadWaitQueue extends ThreadQueue {

    private static final boolean DEBUG = false;

    /**
     * Override the ThreadQueue enqueue. Need to remove
     * the check on queuedOn
     */
    public void enqueue(RVMThread t)
    {
        if (DEBUG)
        {
            VM.sysWriteln("enqueueing ", t.getThreadSlot(), " onto ", Magic.objectAsAddress(this));
        }
        t.next = null;
        /*
         * Queue is empty?
         */
        if (tail == null)
        {
            /*
             * Yes, so set the head
             */
            head = t;
            t.prev = null;
        }
        else
        {
            tail.next = t;
            t.prev = tail;
        }
        // Set the tail
        tail = t;
        t.queuedOn = this;
        size++;
    }
    
}
