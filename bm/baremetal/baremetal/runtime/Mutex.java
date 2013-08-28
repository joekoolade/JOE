/*
 * Created on Jan 12, 2005
 *
 * Copyright (C) Joe Kulig, 2005
 * All rights reserved.
 * 
 */
package baremetal.runtime;

import baremetal.kernel.Scheduler;
import baremetal.vm.Thread;

/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Mutex {
  private int count;
  private Thread holder;
  private Thread waitQueue;

  public void acquire() {
    if(Scheduler.currentThread == null)
      return;
    if(holder==Scheduler.currentThread) {
      count++;
      return;
    }
    while(count>0)
      queue();
    count++;
    holder=Scheduler.currentThread;
  }
  public void release() {
    if(Scheduler.currentThread == null)
      return;
    count--;
    if(count==0)
      wakeup();
    holder = null;
  }
  private void wakeup() {
    Thread aThread = waitQueue;
    while(aThread!=null) {
      Scheduler.schedule(aThread);
      aThread = aThread.next;
    }
  }
  private void queue() {
    Thread t = Scheduler.currentThread;
    t.scheduleMutex();
    /*
     * First one in the queue
     */
    if(waitQueue==null) {
      waitQueue = t;
      t.next = null;
    } else {
      /*
       * Append to the end of the queue
       */
      Thread s = waitQueue;
      while(s.next!=null)
        s = s.next;
      s.next = t;
      t.next = null;
    }
  }
}
