/**
 * Created on Mar 6, 2016
 *
 * Copyright (C) Joe Kulig, 2016 All rights reserved.
 */
package org.jam.board.pc;

import org.jam.cpu.intel.Apic;
import org.jam.cpu.intel.ApicTimer;
import org.jam.cpu.intel.CpuId;
import org.jam.cpu.intel.Idt;
import org.jam.cpu.intel.Tsc;
import org.jam.driver.net.I82559c;
import org.jam.driver.net.LoopBack;
import org.jam.driver.serial.PcSerialPort;
import org.jam.input.HotKey;
import org.jam.interfaces.Timer;
import org.jam.net.InetNexus;
import org.jam.net.inet4.InetAddress;
import org.jam.system.NoDeviceFoundException;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RoundRobin;
import org.jikesrvm.scheduler.Scheduler;
import org.vmmagic.pragma.NonMoving;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class Platform {
    // public static PcSystemTimer timer;
    public static PcSystemTimer pit;
    public static PcSerialPort serialPort;
    public static Scheduler scheduler;
    public static I8259A masterPic;
    public static I8259A slavePic;
    public static I82559c net;
    public static QemuIoApic ioApic;
    public static ApicTimer apicTimer;
    public static Apic apic;
    public static Timer timer;
    public static LoopBack localHost;
    public static I8042 kbd;
    public static HotKey hotKeys;
    
    // Interrupt ports
    final private static int MASTERPICPORT = 0x20;
    final private static int SLAVEPICPORT = 0xA0;
    // Serial ports
    final private static int COM1 = 0x3F8;
    final private static int COM2 = 0x2F8;
    final private static int COM3 = 0x3E8;
    final private static int COM4 = 0x2E8;

    public final static void initTimers()
    {
      I82c54.init();
      Tsc.calibrate(50);
    }

    public final static void initScheduler()
    {
        VM.sysWriteln("scheduler");
        scheduler = new RoundRobin();
    }

    public static void boot()
    {
        CpuId.boot();
        CpuId.print();
        Pci.boot();
        Pci.enumeratePci();
        apicTimer = new ApicTimer();
        apicTimer.calibrate();
        VM.sysWriteln(apicTimer.toString());
        // Tsc.rtcCalibrate();
        serialPort = new PcSerialPort(COM1);
        // VM.sysWriteln("Timer: ", ObjectReference.fromObject(timer));
        VM.sysWriteln("idt");
        Idt.init();
        VM.sysWriteln("apic");
        apic = new Apic();
        apic.boot();
        VM.sysWriteln("ioapic");
        ioApic = new QemuIoApic();
        ioApic.boot();
        VM.sysWrite(ioApic.toString());
        try
        {
            InetAddress inet = new InetAddress("10.0.2.15");
            net = new I82559c(inet, 0xffffff00);
        }
        catch (NoDeviceFoundException e)
        {
            // TODO Auto-generated catch block
            VM.sysWriteln("I8225c device found!");
        }
        net.boot();
        timer = new PcSystemTimer();
        localHost = new LoopBack();
        InetNexus.boot();
        InetNexus.setArpInterface(net);
        
        kbd = new I8042();
        kbd.init();
        
        hotKeys = new HotKey(kbd);
    }
}
