// Thread.java, created Thu Sep  5 10:48:53 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.apple13_osx.java.lang;

import joeq.Scheduler.jq_Thread;

/**
 * Thread
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Thread.java,v 1.4 2004/03/09 06:26:33 jwhaley Exp $
 */
public abstract class Thread {

    public final jq_Thread jq_thread;

    private void init(java.lang.ThreadGroup g, java.lang.Runnable target, java.lang.String name) {
        this.init(g, target, name, true);
    }
    private native void init(java.lang.ThreadGroup g, java.lang.Runnable target, java.lang.String name, boolean setpriority);
    private static synchronized native int nextThreadNum();

    private Thread(java.lang.ThreadGroup group, java.lang.Runnable target, boolean set_priority) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        java.lang.String name = "Thread-" + nextThreadNum();
        this.init(group, target, name, false);
        t.init();
    }

    private Thread(java.lang.ThreadGroup group, java.lang.String name, boolean set_priority) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        this.init(group, null, name, false);
        t.init();
    }
    
}
