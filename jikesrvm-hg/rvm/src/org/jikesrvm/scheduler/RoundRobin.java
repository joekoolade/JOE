/**
 * Created on Mar 8, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class RoundRobin
implements Scheduler {
    private int[] stack;
    private Address stackTop;
    private final static int STACK_SIZE = 256;
    private ThreadQueue runQueue;
    
    public RoundRobin()
    {
        stack = new int[STACK_SIZE];
        /*
         * Put in the sentinel
         */
        stack[STACK_SIZE-1] = 0;    // IP = 0
        stack[STACK_SIZE-2] = 0;    // FP = 0
        stack[STACK_SIZE-3] = 0;    // cmid = 0
        
        stackTop = Magic.objectAsAddress(stack[0]).plus((STACK_SIZE-4)<<2);
        VM.sysWriteln("roundrobin stack top: ", stackTop);
        runQueue = new ThreadQueue();
    }
    /* (non-Javadoc)
     * @see org.jikesrvm.scheduler.Scheduler#scheduleThread()
     */
    @Override
    public void nextThread()
    {
        RVMThread nextThread;
        
        if(runQueue.peek() == null)
        {
            nextThread = RVMThread.idleThread;
        }
        else
        {
            RVMThread currentThread = Magic.getThreadRegister();
            /*
             * Put previous back on the run queue
             * Unless it is the idle thread which is never runnable
             */
            if(!currentThread.isIdleThread())
            {
                runQueue.enqueue(currentThread);
            }
            /*
             * Get the next runnable candidate
             */
            nextThread = runQueue.dequeue();
        }
        /*
         * Setup to restore from new thread
         */
        VM.sysWriteln("next thread sp: ", nextThread.getStackPointer());
        /*
         * Set the thread register
         */
        Magic.setThreadRegister(nextThread);
    }

    /**
     * Puts thread onto the run queue
     * @param thread the thread to put on the run queue
     */
    @Override
    public void addThread(RVMThread thread)
    {
        runQueue.enqueue(thread);
    }
    
    /* (non-Javadoc)
     * @see org.jikesrvm.scheduler.Scheduler#getHandlerStack()
     */
    @Override
    public Address getHandlerStack()
    {
        // TODO Auto-generated method stub
        return stackTop;
    }

}
