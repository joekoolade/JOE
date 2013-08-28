/*
 * Created on Oct 15, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.platform;

import baremetal.devices.I8259A;
import baremetal.devices.MC146818;
import baremetal.kernel.InterruptManager;
import baremetal.processor.Idt;
import baremetal.processor.Pentium;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Platform {
  public static SystemTimer systemTimer;
  public static I8259A masterPic;
  public static I8259A slavePic;
  public static MC146818 rtc;
  public static Pentium cpu;
  public static Idt vectorTable;
  
  public final static void init() {
    cpu = new Pentium();
    cpu.init();
    cpu.cli();
    systemTimer = new SystemTimer();
    InterruptManager.irq = systemTimer;
    vectorTable = new Idt();
    vectorTable.init();
    masterPic = new I8259A(0x20);
    slavePic = new I8259A(0xa0);
    masterPic.master();
    slavePic.slave();
    masterPic.pcSetup();
    slavePic.pcSetup();
    masterPic.interruptMask(0xfe);
    slavePic.interruptMask(0xff);
    cpu.sti();
    cpu.detectCpuFrequency();
  }
}
