// Thread.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_Thread;
import jwutil.util.Assert;

/**
 * Thread
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Thread.java,v 1.11 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class Thread {

    // additional fields
    public final jq_Thread jq_thread;
    
    private boolean daemon;
    
    private Thread(jq_Thread t) {
        this.jq_thread = t;
    }
    
    private native void checkAccess();
    private static synchronized native int nextThreadNum();
    private native void init(java.lang.ThreadGroup g, java.lang.Runnable target, java.lang.String name);
    
    // overridden constructors
    public Thread() {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        int n = nextThreadNum();
        this.init(null, null, "Thread-"+n);
        t.init();
    }
    public Thread(java.lang.Runnable target) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        int n = nextThreadNum();
        this.init(null, target, "Thread-"+n);
        t.init();
    }
    public Thread(java.lang.ThreadGroup group, java.lang.Runnable target) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        int n = nextThreadNum();
        this.init(group, target, "Thread-"+n);
        t.init();
    }
    public Thread(java.lang.String name) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        this.init(null, null, name);
        t.init();
    }
    public Thread(java.lang.ThreadGroup group, java.lang.String name) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        this.init(group, null, name);
        t.init();
    }
    public Thread(java.lang.Runnable target, java.lang.String name) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        this.init(null, target, name);
        t.init();
    }
    public Thread(java.lang.ThreadGroup group, java.lang.Runnable target, java.lang.String name) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        this.init(group, target, name);
        t.init();
    }
    
    // native method implementations
    private static void registerNatives() {}
    public static java.lang.Thread currentThread() { return Unsafe.getThreadBlock().getJavaLangThreadObject(); }
    public static void yield() { Unsafe.getThreadBlock().yield(); }
    public static void sleep(long millis) throws InterruptedException { Unsafe.getThreadBlock().sleep(millis); }
    public synchronized void start() {
        jq_Thread jq_thread = this.jq_thread;
        jq_thread.start();
    }
    private boolean isInterrupted(boolean ClearInterrupted) {
        jq_Thread jq_thread = this.jq_thread;
        return jq_thread.isInterrupted(ClearInterrupted);
    }
    public final boolean isAlive() {
        jq_Thread jq_thread = this.jq_thread;
        return jq_thread.isAlive();
    }
    public final void setDaemon(boolean b) {
        this.checkAccess();
        if (this.isAlive()) {
            throw new java.lang.IllegalThreadStateException();
        }
        this.daemon = b;
        jq_Thread jq_thread = this.jq_thread;
        jq_thread.setDaemon(b);
    }
    public int countStackFrames() {
        jq_Thread jq_thread = this.jq_thread;
        return jq_thread.countStackFrames();
    }
    private void setPriority0(int newPriority) {
        jq_Thread jq_thread = this.jq_thread;
        Assert._assert(newPriority >= 1);
        Assert._assert(newPriority <= 10);
        jq_thread.setPriority(newPriority - 1);
    }
    private void stop0(java.lang.Object o) {
        jq_Thread jq_thread = this.jq_thread;
        jq_thread.stop(o);
    }
    private void suspend0() {
        jq_Thread jq_thread = this.jq_thread;
        jq_thread.suspend();
    }
    private void resume0() {
        jq_Thread jq_thread = this.jq_thread;
        jq_thread.resume();
    }
    private void interrupt0() {
        jq_Thread jq_thread = this.jq_thread;
        jq_thread.interrupt();
    }
}
