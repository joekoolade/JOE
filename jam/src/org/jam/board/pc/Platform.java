/**
 * Created on Mar 6, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.board.pc;

import org.jam.cpu.intel.Idt;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RoundRobin;
import org.jikesrvm.scheduler.Scheduler;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Joe Kulig
 *
 */
public class Platform {
    public static PcSystemTimer timer;
    public static Scheduler scheduler;
    public static I8259A masterPic;
    public static I8259A slavePic;
    final private static int MASTERPICPORT = 0x20;
    final private static int SLAVEPICPORT = 0xA0;
    
    public static void init()
    {
        timer = new PcSystemTimer();
        VM.sysWriteln("Timer: ", ObjectReference.fromObject(timer));
        scheduler = new RoundRobin();
        Idt.init();
        masterPic = new I8259A(MASTERPICPORT);
        masterPic.pcSetup();
        masterPic.interruptMask((byte)0xFE);
        slavePic = new I8259A(SLAVEPICPORT, true);
        slavePic.pcSetup();
        slavePic.interruptMask((byte)0xFF);
    }
}
