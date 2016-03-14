/**
 * Created on Mar 6, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.board.pc;

import org.jam.cpu.intel.Idt;
import org.jikesrvm.scheduler.RoundRobin;
import org.jikesrvm.scheduler.Scheduler;

/**
 * @author Joe Kulig
 *
 */
public class Platform {
    public static PcSystemTimer timer;
    public static Scheduler scheduler;
    
    public static void init()
    {
        timer = new PcSystemTimer();
        scheduler = new RoundRobin();
        Idt.init();
    }
}
