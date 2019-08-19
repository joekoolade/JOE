/**
 * Created on Mar 8, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
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
    private int idling = 0;
    
    public RoundRobin()
    {
        stack = MemoryManager.newNonMovingIntArray(STACK_SIZE);
        /*
         * Put in the sentinel
         */
        stack[STACK_SIZE-1] = 0;    // IP = 0
        stack[STACK_SIZE-2] = 0;    // FP = 0
        stack[STACK_SIZE-3] = 0;    // cmid = 0
        
        stackTop = Magic.objectAsAddress(stack).plus((STACK_SIZE-4)<<2);
        VM.sysWriteln("roundrobin stack top: ", stackTop);
        runQueue = new ThreadQueue();
    }
    /* 
     * Current thread must be scheduled before calling calling
     * 
     */
    public void nextThread()
    {
        RVMThread nextThread;
        
//        RVMThread currentThread = Magic.getThreadRegister();
        if(runQueue.peek() == null)
        {
            nextThread = RVMThread.idleThread;
            idling++;
        }
        else
        {
            /*
             * Get the next runnable candidate
             */
            nextThread = runQueue.dequeue();
            idling=0;
        }
        /*
         * Setup to restore from new thread
         */
//        VM.sysWrite(Magic.objectAsAddress(Magic.getThreadRegister()));
//        VM.sysWriteln("->", Magic.objectAsAddress(nextThread));
//        VM.sysWrite("next thread sp: ", nextThread.getStackPointer());
//        VM.sysWriteln(" current sp: ", currentThread.getStackPointer());
        /*
         * Set the thread register
         */
        Magic.setThreadRegister(nextThread);
    }

    /**
     * Puts thread onto the run queue
     * @param thread the thread to put on the run queue
     */
    public void addThread(RVMThread thread)
    {
        /*
         * See if thread is already queued somewhere else
         * like a monitor
         */
        if(thread.queuedOn==null && !thread.isTerminated())
        {
          runQueue.enqueue(thread);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jikesrvm.scheduler.Scheduler#getHandlerStack()
     */
    public Address getHandlerStack()
    {
        // TODO Auto-generated method stub
        return stackTop;
    }
    /* (non-Javadoc)
     * @see org.jikesrvm.scheduler.Scheduler#noRunnableThreads()
     */
    public boolean noRunnableThreads()
    {
        return runQueue.isEmpty();
    }

}
