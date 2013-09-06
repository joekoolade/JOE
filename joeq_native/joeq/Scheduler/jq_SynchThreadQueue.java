// jq_SynchThreadQueue.java, created Mon Apr  9  1:52:50 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Scheduler;

import joeq.Runtime.Unsafe;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_SynchThreadQueue.java,v 1.7 2004/09/30 03:35:30 joewhaley Exp $
 */
public class jq_SynchThreadQueue extends jq_ThreadQueue {

    //public synchronized boolean isEmpty() { return super.isEmpty(); }
    public void enqueue(jq_Thread t) {
        Assert._assert(Unsafe.getThreadBlock().isScheduler);
        synchronized (this) {
            super.enqueue(t);
        }
    }
    public synchronized void enqueueFront(jq_Thread t) {
        Assert._assert(Unsafe.getThreadBlock().isScheduler);
        synchronized (this) {
            super.enqueueFront(t);
        }
    }
    public synchronized jq_Thread dequeue() {
        Assert._assert(Unsafe.getThreadBlock().isScheduler);
        synchronized (this) {
            return super.dequeue();
        }
    }
    public synchronized boolean remove(jq_Thread t2) {
        Assert._assert(Unsafe.getThreadBlock().isScheduler);
        synchronized (this) {
            return super.remove(t2);
        }
    }

}
