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
package org.jam.util;

import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Uninterruptible;

/**
 * This class implements a priority queue using the standard
 * (balanced partially-ordered tree, i.e., "heap") algorithm.
 * Smaller priority objects are in the front of the queue.
 */
public class PriorityQueue {

  /**
   * the queue, we use elements 1..queue.length
   */
  private PriorityQueueNode[] queue;

  /**
   * the number of elements actually in the queue
   */
  private int numElements = 0;

  public PriorityQueue() {
    queue = new PriorityQueueNode[RVMThread.MAX_THREADS>>2];

    // We don't use element #0
    for (int i = 1; i < queue.length; i++) {
      queue[i] = new PriorityQueueNode();
    }
  }

  /**
   * Determines number of elements in the queue
   * @return number of elements in the queue
   */
  public final int numElements() {
    return numElements;
  }

  /**
   * Checks if the queue is empty
   * @return is the queue empty?
   */
  public final boolean isEmpty() {
    return numElements == 0;
  }

  /**
   * Starting at the position passed, swap with parent until heap condition
   * is satisfied, i.e., bubble up
   * @param startingElement the position to start at
   */
  @Uninterruptible
  private void reheapify(int startingElement) {
    int current = startingElement;
    int parent = numElements / 2;
    // keep checking parents that violate the magic condition
    while (parent > 0 && queue[parent].priority >= queue[current].priority) {
      //        System.out.println("Parent: "+ parent +", Current: "+ current);
      //        System.out.println("Contents before: "+ this);
      // exchange parrent and current values
      PriorityQueueNode tmp = queue[parent];
      queue[parent] = queue[current];
      queue[current] = tmp;

      //        System.out.println("Contents after: "+ this);
      // go up 1 level
      current = parent;
      parent = parent / 2;
    }
  }

  /**
   * Insert the object passed with the priority value passed
   * @param _priority  the priority of the inserted object
   * @param _data the object to insert
   */
  @Uninterruptible
  public void insert(long _priority, Object _data) {
    numElements++;
    if (numElements == queue.length) {
    	VM.sysWriteln("priority queue resize: ", numElements);
      PriorityQueueNode[] tmp = new PriorityQueueNode[(int) (queue.length * 1.5)];
      System.arraycopy(queue, 0, tmp, 0, queue.length);
      for (int i = queue.length; i < tmp.length; i++) {
        tmp[i] = new PriorityQueueNode();
      }
      queue = tmp;
    }

    queue[numElements].data = _data;
    queue[numElements].priority = _priority;

    // re-heapify
    reheapify(numElements);
  }

  /**
   * Remove and return the front (minimum) object
   * @return the front (minimum) object or null if the queue is empty.
   */
  @Uninterruptible
  public Object deleteMin() {
    if (isEmpty()) return null;

    Object returnValue = queue[1].data;
    // move the "last" element to the root and reheapify by pushing it down
    queue[1].priority = queue[numElements].priority;
    queue[1].data = queue[numElements].data;
    numElements--;
    // reheapify!!!
    int current = 1;

    // The children live at 2*current and  2*current+1
    int child1 = 2 * current;
    while (child1 <= numElements) {
      int child2 = 2 * current + 1;

      // find the smaller of the two children
      int smaller;
      if (child2 <= numElements && queue[child2].priority <= queue[child1].priority) {
        smaller = child2;
      } else {
        smaller = child1;
      }

      if (queue[smaller].priority > queue[current].priority) {
        break;
      } else {
        // exchange parrent and current values
        PriorityQueueNode tmp = queue[smaller];
        queue[smaller] = queue[current];
        queue[current] = tmp;

        // go down 1 level
        current = smaller;
        child1 = 2 * current;
      }
    }
    return returnValue;
  }

  @Uninterruptible
  public final Object remove(long time)
  {
      Object result = null;
      
      /*
       * Loop thru the queue to find a matching key
       */
      int index=1;
      PriorityQueueNode node;
      for(; index <= numElements; index++)
      {
          node = queue[index];
          if(node.priority == time)
          {
              result = node.data;
              /*
               * Shift up
               */
              shiftUp(index);
              numElements--;
              reheapify(1);
              break;
          }
              
      }
      return result;
  }
  
  @Uninterruptible
  public final Object remove(Object thread)
  {
      Object result = null;
      
      /*
       * Loop thru the queue to find a matching key
       */
      int index=1;
      PriorityQueueNode node;
      for(; index <= numElements; index++)
      {
          node = queue[index];
          if(node.data == thread)
          {
              result = node.data;
              /*
               * Shift up
               */
              shiftUp(index);
              numElements--;
              reheapify(1);
              break;
          }
              
      }
      return result;
  }
  
  @Uninterruptible
  private void shiftUp(int index)
  {
      while(index < numElements)
      {
          queue[index].priority = queue[index+1].priority;
          queue[index].data = queue[index+1].data;
          index++;
      }
  }

  /**
   *  Return the priority of front object without removing it
   *  @return the priority of the front object
   */
  public final long rootValue() {
    if (VM.VerifyAssertions) VM._assert(!isEmpty());

    return queue[1].priority;
  }

  /**
   *  Prints the contents of the queue
   *  @return the queue contents
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(" --> ");
    sb.append("Dumping Queue with ");
    sb.append(numElements);
    sb.append(" elements:\n");
    if (numElements >= 1) sb.append("\t");

    for (int i = 1; i <= numElements; i++) {
      sb.append(queue[i].toString());
      if (i < numElements) sb.append("\n\t");
    }
    return sb.toString();
  }

  /**
   * A local class that holds the nodes of the priority tree
   */
  private static class PriorityQueueNode {

    /**
     * the value to compare on, larger is better
     */
    public long priority;

    /**
     * the associated data
     */
    public Object data;

    @Override
    public String toString() {
      return data + " ... [" + priority + "]";
    }
  }
}

