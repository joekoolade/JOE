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
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.Untraced;

/**
 * An unsynchronized queue data structure for Threads. The current uses are all
 * where there is some other lock that already protects the queue.
 */
@Uninterruptible
@NonMoving
public class ThreadQueue {
  protected static final boolean trace = false;
  private static final boolean trace0 = false;  // traces remove()
  String name;
  
  @Untraced RVMThread head;
  @Untraced RVMThread tail;
  int size;
  
  public ThreadQueue(String name) {
      size = 0;
	  this.name = name;
  }

  public boolean isEmpty() {
    return head == null;
  }

  public void enqueue(RVMThread t) {
    if (trace) {
      VM.sysWriteln("enqueue:", t.getThreadSlot(), " onto ",
          name);
    }
    /*
     * If it is already queuedon, the don't schedule
     */
    if (t.queuedOn != null)
    {
      if (trace)
      {
        VM.sysWrite("Already scheduled ", t.getThreadSlot());
        VM.sysWrite("on ", t.queuedOn.name);
        VM.sysWrite(" tail: ", tail.getName());
        VM.sysWrite(" head: ", head.getName());
        VM.sysWrite(" next: ", t.next.getName());
        VM.sysWriteln(" prev: ", t.prev.getName());
      }
      VM.sysWriteln(t.queuedOn.name);
      VM.sysFail("Queued on");
      return;
    }
    if (VM.VerifyAssertions)
    {
//        if(trace) VM.sysWriteln("queueOn ", Magic.objectAsAddress(t.queuedOn));
        VM._assert(t.queuedOn == null);
    }
    if(head == null)
    {
        head = t;
        tail = t;
        t.next = null;
        t.prev = null;
        size = 1;
    }
    else
    {
        t.prev = tail;
        tail.next = t;
        t.next = null;
        tail = t;
        size++;
    }
    t.queuedOn = this;
//    t.next = null;
//    if (tail == null) {
//      head = t;
//    } else {
//      tail.next = t;
//    }
//    tail = t;
//    t.queuedOn = this;
//    size++;
  }

  public RVMThread peek() {
    return head;
  }

  public RVMThread dequeue()
  {
      RVMThread result = head;
      if (result != null)
      {
          size -= 1;
          head = head.next;
          if (head == null)
          {
              tail = null;
              result.prev = null;
              result.next = null;
              size = 0;
          }
          else
          {
              head.prev = null;
              result.next = null;
              result.prev = null;
          }
          if (VM.VerifyAssertions) 
          {
              if(result.queuedOn != this) VM.sysWriteln("queuedOn: ", result.queuedOn.name);
              VM._assert(result.queuedOn == this);
          }
          result.queuedOn = null;
      }
      if (trace)
      {
          if (result == null)
          {
              if (tail != null) VM.sysWriteln("dequeue: tail NOT NULL ", tail.getName());
              VM.sysWriteln("dequeue: no threads on ", name);
          }
          else
          {
              VM.sysWriteln("dequeueing ", result.getThreadSlot(), " from ", name);
          }
      }
      if (head==null && tail==null && size > 0)
      {
          VM.sysWriteln("queue size > 0: ", size);
          VM._assert(false);
      }
      return result;
  }

  /**
   * @param cur a thread
   * @return the next pointer of cur unless cur is {@code null}, in which
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
   * Sets the next pointer of cur to value unless cur is {@code null},
   * in which case it sets head. Also sets tail as appropriate.
   *
   * @param cur a thread
   * @param value a value for the next pointer of the given thread
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
   * Removes the given thread from the queue if the thread is still on the queue.
   * Does nothing (and returns in O(1)) if the thread is not on the queue. Also
   * does nothing (and returns in O(1)) if the thread is on a different queue.
   *
   * @param t thread to remove from the queue
   * @return whether the given thread was removed from the queue
   */
  public boolean remove(RVMThread t) {
    if (t.queuedOn != this || t.queuedOn == null)
      return false;
    if (trace0) {
      VM.sysWriteln("removing ", t.getThreadSlot(), " from ",
          name);
    }
    for (RVMThread cur = null; cur != tail; cur = getNext(cur)) {
      if (getNext(cur) == t) {
        if (trace0) {
          VM.sysWriteln("found!  before:");
          dump();
        }
        setNext(cur, t.next);
        if (tail == t) {
          tail = cur;
        }
        if (trace0) {
          VM.sysWriteln("after:");
          dump();
        }
        t.next = null;
        t.queuedOn = null;
        size--;
        return true;
      }
    }
    VM.sysWriteln("Could not remove Thread #", t.getThreadSlot(),
        " from queue!");
    dump();
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
    return false;
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
    VM.sysWriteln();
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
