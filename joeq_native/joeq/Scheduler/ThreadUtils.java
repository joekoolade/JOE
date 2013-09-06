// ThreadUtils.java, created Mon Dec 16 18:57:13 2002 by mcmartin
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Scheduler;

import joeq.Main.jq;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ThreadUtils.java,v 1.6 2004/03/09 06:26:56 jwhaley Exp $
 */
public abstract class ThreadUtils {
    public static jq_Thread getJQThread(java.lang.Thread t) {
        return _delegate.getJQThread(t);
    }
    static interface Delegate {
        jq_Thread getJQThread(java.lang.Thread t);
    }

    private static Delegate _delegate;
    static {
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Scheduler.FullThreadUtils");
        }
        if (_delegate == null) {
            _delegate = new joeq.Scheduler.HostedThreadUtils();
        }
    }

    private static Delegate attemptDelegate(String s) {
        String type = "thread util delegate";
        try {
            Class c = Class.forName(s);
            return (Delegate)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }

}
