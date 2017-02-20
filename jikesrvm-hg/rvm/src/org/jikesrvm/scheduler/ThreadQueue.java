/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.Untraced;
import org.jikesrvm.runtime.Magic;

/**
 * An unsynchronized queue data structure for Threads. The current uses are all
 * where there is some other lock that already protects the queue.
 */
@Uninterruptible
@NonMoving
public class ThreadQueue {
  protected static final boolean trace = false;

  @Untraced RVMThread head;
  @Untraced RVMThread tail;
  int size;
  
  public ThreadQueue() {  
    size = 0;
  }

  public boolean isEmpty() {
    return head == null;
  }

  public void enqueue(RVMThread t) {
    if (trace) {
      VM.sysWriteln("enqueueing ", t.getThreadSlot(), " onto ",
          Magic.objectAsAddress(this));
    }
    /*
     * If it is already queuedon, the don't schedule
     */
    if(t.queuedOn!=null)
    {
      VM.sysWrite("Already scheduled ", t.getThreadSlot());
      VM.sysWrite(" tail: ", Magic.objectAsAddress(tail)); VM.sysWrite(" head: ", Magic.objectAsAddress(head));
      VM.sysWrite(" next: ", Magic.objectAsAddress(t.next)); VM.sysWriteln(" prev: ", Magic.objectAsAddress(t.prev));
      return;
    }
    if (VM.VerifyAssertions)
      VM._assert(t.queuedOn == null);
    t.next = null;
    /*
     * Queue is empty?
     */
    if (tail == null) {
      /*
       * Yes, so set the head
       */
      head = t;
      t.prev = null;
    } else {
      tail.next = t;
      t.prev = tail;
    }
    // Set the tail
    tail = t;
    t.queuedOn = this;
    size++;
  }

  public RVMThread peek() {
    return head;
  }

  public RVMThread dequeue() {
    RVMThread result = head;
    if (result != null) {
      head = result.next;
      if (head == null) {
        tail = null;
        result.prev = null;
      }
      else
      {
        head.prev = null;
      }
      if (VM.VerifyAssertions)
      {
        VM._assert(result.queuedOn == this);
      }
      result.next = null;
      result.queuedOn = null;
      size--;
    }
    if (trace) {
      if (result == null) {
        VM.sysWriteln("dequeueing null from ", Magic.objectAsAddress(this));
      } else {
        VM.sysWriteln("dequeueing ", result.getThreadSlot(), " from ",
            Magic.objectAsAddress(this));
      }
    }
    return result;
  }

  /**
   * Private helper. Gets the next pointer of cur unless cur is {@code null}, in which
   * case it returns head.
   */
  private RVMThread getNext(RVMThread cur) {
    if (cur == null) {
      return head;
    } else {
      return cur.next;
    }
  }

  /**
   * Private helper. Sets the next pointer of cur to value unless cur is {@code null},
   * in which case it sets head. Also sets tail as appropriate.
   */
  private void setNext(RVMThread cur, RVMThread value) {
    if (cur == null) {
      if (tail == head) {
        tail = value;
      }
      head = value;
    } else {
      if (cur == tail) {
        tail = value;
      }
      cur.next = value;
    }
  }

  /**
   * Remove the given thread from the queue if the thread is still on the queue.
   * Does nothing (and returns in O(1)) if the thread is not on the queue. Also
   * does nothing (and returns in O(1)) if the thread is on a different queue.
   */
  public boolean remove(RVMThread t) {
    if (t.queuedOn != this || t.queuedOn == null)
      return false;
    if (trace) {
      VM.sysWriteln("removing ", t.getThreadSlot(), " from ",
          Magic.objectAsAddress(this));
    }
    
    /*
     * thread is only one on the queue
     */
    if(size==1)
    {
      head = null;
      tail = null;
      t.queuedOn = null;
      size--;
      return true;
    }
    else
    {
      if(t.prev==null)
      {
        head = t.next;
        head.prev = null;
        t.queuedOn = null;
        size--;
        return true;
      }
      if(t.next == null)
      {
        tail = t.prev;
        tail.next = null;
        t.queuedOn = null;
        size--;
        return true;
      }
      t.prev.next = t.next;
      t.next.prev = t.prev;
      t.queuedOn = null;
      size--;
      return true;
    }
    
//    VM.sysWriteln("Could not remove Thread #", t.getThreadSlot(),
//        " from queue!");
//    dump();
//    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
//    return false; // make javac happy
  }

  public boolean isQueued(RVMThread t) {
    return t.queuedOn == this;
  }

  public void dump() {
    boolean pastFirst = false;
    for (RVMThread t = head; t != null; t = t.next) {
      if (pastFirst) {
        VM.sysWrite(" ");
      }
      t.dump();
      pastFirst = true;
    }
    VM.sysWrite("\n");
    if (head != null) {
      VM.sysWriteln("head: ", head.getThreadSlot());
    } else {
      VM.sysWriteln("head: null");
    }
    if (tail != null) {
      VM.sysWriteln("tail: ", tail.getThreadSlot());
    } else {
      VM.sysWriteln("tail: null");
    }
  }
}
