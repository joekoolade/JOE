/*
 * Created on Nov 5, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.system;

//import baremetal.vm.Thread;

/**
 * @author Joe Kulig
 *
 */
public final class Scheduler {
  
  public static Thread currentThread;
  private static Thread runList;
  private static Thread runListEnd;
  private static Thread waitList;
  private static Thread waitListEnd;
  private static Thread sleepList;
  private static Thread sleepListEnd;
  private static int threads;
  
  public static void schedule(Thread t) {
    if(runList==null) {
      runList = t;
      runListEnd = t;
    } else {
//      runListEnd.next = t;
//      t.back = runListEnd;
//      runListEnd = t;
//      t.next = null;
    }
    threads++;
  }
  
  public final boolean runListEmpty() {
    return threads==0;
  }
  
  public void schedule() {
    /*
     * Take the thread in front and move to the back.
     * Run the next thread.
     */
    if(runListEmpty()) {
//      Thread.newThread = false;
      return;
    }
    Thread previousThread = currentThread;
    currentThread = remove();
    schedule(previousThread);
//    Thread.newThread = (currentThread!=previousThread);
  }
  
  public void mutexSchedule() {
    currentThread = remove();
//    Thread.newThread = true;
  }
  private Thread remove() {
    Thread t;
    
    t = runList;
//    runList = t.next;
//    // null out threads linkage
//    t.next = null;
//    t.back = null;
    threads--;
    return t;
  }
}
