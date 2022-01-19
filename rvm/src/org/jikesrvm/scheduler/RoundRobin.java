/**
 * Created on Mar 8, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class RoundRobin implements Scheduler {
  private ThreadQueue runQueue;
  final private static boolean trace = false;
  private static final boolean traceNext = false;

  public RoundRobin() {
    runQueue = new ThreadQueue("runQueue");
  }

  public void schedule()
  {
    RVMThread currentThread = RVMThread.getCurrentThread();
    if(!currentThread.isOnQueue() && currentThread.isRunnable())
    {
      addThread(currentThread);
    }
    RVMThread nextThread = runQueue.dequeue();
    while(nextThread == null)
    {
//      VM.sysWrite("halt!");
      Magic.halt();
      nextThread = runQueue.dequeue();
    }

    Magic.threadSwitch(currentThread, nextThread.getContextRegisters());
    /*
     * nextThread is running here
     */
  }
  
  /*
   * Current thread must be scheduled before calling calling
   * 
   */
  public RVMThread nextThread() {
    RVMThread nextThread;

    nextThread = runQueue.dequeue();
//    if (nextThread == null) {
//      nextThread = RVMThread.idleThread;
//    }
    /*
     * Setup to restore from new thread
     */
    if (traceNext && nextThread != null) {
      VM.sysWrite("nextThread: ", nextThread.threadSlot);
      VM.sysWriteln(" ", nextThread.getName());
    }
    // VM.sysWrite("next thread sp: ", nextThread.getStackPointer());
    // VM.sysWriteln(" current sp: ", currentThread.getStackPointer());
    // VM.sysWriteln("gpr ",
    // Magic.objectAsAddress(nextThread.contextRegisters.gprs));
    /*
     * Set the thread register
     */
    return nextThread;
  }

  /**
   * Puts thread onto the run queue
   * 
   * @param thread the thread to put on the run queue
   */
  public void addThread(RVMThread thread) {
    if (thread.isOnQueue()) {
      if (trace) {
        VM.sysWrite("|Q", thread.threadSlot);
        VM.sysWrite(" on ", thread.getThreadQueueName());
      }
      return;
    }
    /*
     * See if we are in a garbage collection. If so then put the thread onto the
     * gcWait queue
     */
//        if(Plan.gcInProgress() && thread.isCollectorThread()==false && thread.ignoreHandshakesAndGC()==false)
//        {
//            RVMThread.gcWait.enqueue(thread);
//        }
    /*
     * See if thread is already queued somewhere else like a monitor
     */
    else if (!thread.isTerminated()) {
      if (trace)
        VM.sysWrite("|A", thread.threadSlot);
      runQueue.enqueue(thread);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jikesrvm.scheduler.Scheduler#getHandlerStack()
   */
  public Address getHandlerStack() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jikesrvm.scheduler.Scheduler#noRunnableThreads()
   */
  public boolean noRunnableThreads() {
    return runQueue.isEmpty();
  }

}
