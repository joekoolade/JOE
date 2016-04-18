/**
 * Created on Apr 14, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class IdleThread extends SystemThread {

    /**
     * 
     */
    public IdleThread()
    {
        super("IdleThread");
    }
    
    /* 
     * This is the idling loop when there is nothing else
     * to be done
     */
    @Override
    public void run()
    {
        VM.sysWriteln("Starting the Idle Thread");
        /*
         * For now just pause because we will be doing is spinning until a process becomes available.
         * Later on may want to do some power saving stuff.
         */
        while(true)
        {
            Magic.pause();
        }

    }

}
