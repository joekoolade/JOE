/**
 * Created on Mar 6, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.board.pc;

import org.jam.cpu.intel.Apic;
import org.jam.cpu.intel.ApicTimer;
import org.jam.cpu.intel.CpuId;
import org.jam.cpu.intel.Idt;
import org.jam.cpu.intel.Tsc;
import org.jam.driver.net.VirtioNet;
import org.jam.driver.serial.PcSerialPort;
import org.jam.interfaces.Timer;
import org.jam.system.NoDeviceFoundException;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RoundRobin;
import org.jikesrvm.scheduler.Scheduler;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class Platform {
    //public static PcSystemTimer timer;
    public static PcSystemTimer pit;
    public static PcSerialPort serialPort;
    public static Scheduler scheduler;
    public static I8259A masterPic;
    public static I8259A slavePic;
    public static VirtioNet net;
    public static IOApic ioApic;
    public static ApicTimer apicTimer;
    public static Timer timer;
    // Interrupt ports
    final private static int MASTERPICPORT = 0x20;
    final private static int SLAVEPICPORT = 0xA0;
    // Serial ports
    final private static int COM1 = 0x3F8;
    final private static int COM2 = 0x2F8;
    final private static int COM3 = 0x3E8;
    final private static int COM4 = 0x2E8;
    
    public static void boot()
    {
      CpuId.boot();
      CpuId.print();
      Pci.boot();
      Pci.enumeratePci();
      pit = new PcSystemTimer();
      timer = pit;
      new PcSystemTimer();
      Tsc.calibrate(50);
      apicTimer = new ApicTimer();
      apicTimer.calibrate();
//      Tsc.rtcCalibrate();
      serialPort = new PcSerialPort(COM1);
//      VM.sysWriteln("Timer: ", ObjectReference.fromObject(timer));
      scheduler = new RoundRobin();
      Idt.init();
      ioApic = new IOApic();
      VM.sysWrite(ioApic.toString());
      masterPic = new I8259A(MASTERPICPORT);
      masterPic.pcSetup();
      slavePic = new I8259A(SLAVEPICPORT, true);
      slavePic.pcSetup();
      try
      {
        net = new VirtioNet();
      }
      catch (NoDeviceFoundException e)
      {
        // TODO Auto-generated catch block
        VM.sysWriteln("No VirtioNet device found!");
      }
      net.boot();
    }
}
