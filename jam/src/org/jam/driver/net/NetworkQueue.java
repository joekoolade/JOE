/**
 * Created on Nov 5, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;

/**
 * @author Joe Kulig
 *
 */
public class NetworkQueue {
  // size must be a factor of 2
  final static int SIZE=256;
  Packet queue[];
  int head,tail;
  
  public NetworkQueue()
  {
    this(SIZE);
  }
  /**
   * @param size
   */
  public NetworkQueue(int size)
  {
    queue = new Packet[size];
    head = 0;
    tail = 0;
  }
  final public void put(Packet packet)
  {
    // check for overflow
    if(full())
    {
      VM.sysWrite("Network Queue full ", Magic.objectAsAddress(this)); VM.sysWrite(" head ", head);
      VM.sysWrite(" tail ", tail);
      return;
    }
    queue[head] = packet;
    head = nextHead();
  }
  final public Packet get()
  {
    if(empty())
    {
      return null;
    }
    Packet packet = queue[tail];
    tail = nextTail();
    return packet;
  }
  final private int nextTail()
  {
    return (tail+1) & (SIZE-1);
  }
  final private int nextHead()
  {
    return (head+1) & (SIZE-1);
  }
  final private boolean empty()
  {
    return head==tail;
  }
  final private boolean full()
  {
    return nextHead() == tail;
  }
}
