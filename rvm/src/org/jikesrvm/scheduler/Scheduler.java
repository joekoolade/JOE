/**
 * Created on Mar 8, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public interface Scheduler {
    public RVMThread nextThread();
    public void addThread(RVMThread thread);
    public Address getHandlerStack();
    public void schedule();
    /**
     * @return
     */
    public boolean noRunnableThreads();
}
