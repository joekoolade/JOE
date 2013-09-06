// HostedThreadUtils.java, created Mon Dec 16 18:57:13 2002 by mcmartin
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Scheduler;

import joeq.Class.jq_Class;
import joeq.Runtime.Reflection;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: HostedThreadUtils.java,v 1.5 2004/03/09 21:57:40 jwhaley Exp $
 */
public class HostedThreadUtils implements ThreadUtils.Delegate {
    public jq_Thread getJQThread(java.lang.Thread t) {
        jq_Class k = joeq.Class.PrimordialClassLoader.getJavaLangThread();
        joeq.Class.jq_InstanceField f = k.getOrCreateInstanceField("jq_thread", "Ljoeq/Scheduler/jq_Thread;");
        return (jq_Thread)Reflection.getfield_A(t, f);
    }    
}
