/**
 * Created on Mar 8, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.mmtk.plan.Plan;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class RoundRobin
implements Scheduler {
    private final static int STACK_SIZE = 256;
    private ThreadQueue runQueue;
    private int idling = 0;
    final private static boolean trace = false;
	private static final boolean traceNext = false;
    
    public RoundRobin()
    {
        runQueue = new ThreadQueue();
    }
    /* 
     * Current thread must be scheduled before calling calling
     * 
     */
    public void nextThread()
    {
        RVMThread nextThread;
        
        RVMThread currentThread = Magic.getThreadRegister();
        nextThread = runQueue.dequeue();
        if(nextThread == null)
        {
            nextThread = RVMThread.idleThread;
        }
        /*
         * Setup to restore from new thread
         */
        if(traceNext)
        {
	        VM.sysWrite("nextThread: ",  currentThread.threadSlot);
	        VM.sysWrite("->", nextThread.threadSlot);
        }
        // VM.sysWrite("next thread sp: ", nextThread.getStackPointer());
        // VM.sysWriteln(" current sp: ", currentThread.getStackPointer());
        // VM.sysWriteln("gpr ",
        // Magic.objectAsAddress(nextThread.contextRegisters.gprs));
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
		if (thread.isOnQueue()) 
		{
			if(trace) VM.sysWrite('Q');
			return;
		}
        /*
         * See if we are in a garbage collection. If so
         * then put the thread onto the gcWait queue
         */
//        if(Plan.gcInProgress() && thread.isCollectorThread()==false && thread.ignoreHandshakesAndGC()==false)
//        {
//            RVMThread.gcWait.enqueue(thread);
//        }
        /*
         * See if thread is already queued somewhere else
         * like a monitor
         */
        else if(!thread.isTerminated() && thread.isRunnable())
        {
        	if(trace) VM.sysWrite('A');
        	runQueue.enqueue(thread);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jikesrvm.scheduler.Scheduler#getHandlerStack()
     */
    public Address getHandlerStack()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.jikesrvm.scheduler.Scheduler#noRunnableThreads()
     */
    public boolean noRunnableThreads()
    {
        return runQueue.isEmpty();
    }

}
