// Thread.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_win32.java.lang;

import joeq.Scheduler.jq_Thread;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Thread.java,v 1.7 2004/03/09 06:26:55 jwhaley Exp $
 */
public abstract class Thread {

    public final jq_Thread jq_thread;
    
    private void init(java.lang.ThreadGroup g, java.lang.Runnable target, java.lang.String name) {
        this.init(g, target, name, 0L);
    }
    private native void init(java.lang.ThreadGroup g, java.lang.Runnable target, java.lang.String name, long stackSize);
    
    public Thread(java.lang.ThreadGroup group, java.lang.Runnable target, java.lang.String name, long stackSize) {
        java.lang.Object o = this;
        jq_Thread t = new jq_Thread((java.lang.Thread)o);
        this.jq_thread = t;
        this.init(group, target, name, stackSize);
        t.init();
    }
    
}
