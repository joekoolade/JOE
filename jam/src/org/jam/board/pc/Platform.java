/**
 * Created on Mar 6, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.board.pc;

import org.jam.cpu.intel.Idt;
import org.jam.driver.serial.PcSerialPort;
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
    public static PcSerialPort serialPort;
    public static Scheduler scheduler;
    public static I8259A masterPic;
    public static I8259A slavePic;
    // Interrupt ports
    final private static int MASTERPICPORT = 0x20;
    final private static int SLAVEPICPORT = 0xA0;
    // Serial ports
    final private static int COM1 = 0x3F8;
    final private static int COM2 = 0x2F8;
    final private static int COM3 = 0x3E8;
    final private static int COM4 = 0x2E8;
    
    public static void init()
    {
        timer = new PcSystemTimer();
        serialPort = new PcSerialPort(COM1);
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
