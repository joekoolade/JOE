/*
 * Created on Oct 12, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.devices;

import baremetal.kernel.Memory;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class I8259A {
  /*
   * ICW1 initialization word
   *  bit 7-5 = 0  only used in 80/85 mode
   *  bit 4   = 1  ICW1 is being issued
   *  bit 3   = 0  edge triggered mode
   *          = 1  level triggered mode
   *  bit 2   = 0  successive interrupt vectors use 8 bytes
   *          = 1  successive interrupt vectors use 4 bytes
   *  bit 1   = 0  cascade mode
   *          = 1  single mode, no ICW3 needed
   *  bit 0   = 0  no ICW4 needed
   *          = 1  ICW4 needed
    ICW2:
     bit 7-3 = address lines A0-A3 of base vector address for PIC
     bit 2-0 = reserved
    ICW3:
     bit 7-0 = 0  slave controller not attached to corresponding
            interrupt pin
       = 1  slave controller attached to corresponding
            interrupt pin
    ICW4:
     bit 7-5 = 0  reserved
     bit 4   = 0  no special fully-nested mode
       = 1  special fully-nested mode
     bit 3-2 = 0x nonbuffered mode
       = 10 buffered mode/slave
       = 11 buffered mode/master
     bit 1   = 0  normal EOI
       = 1  Auto EOI
     bit 0   = 0  8085 mode
       = 1  8086/8088 mode

   */
  private int icw1,icw2,icw3,icw4;
  private boolean isSlave;
  private int port;
  
  public I8259A(int port) {
    this.port = port;
  }
  
  public void edgeTriggered() {
    icw1 &= ~0x8;
  }
  public void levelTriggered() {
    icw1 |= 0x8;
  }
  public void callAddressInterval4() {
    icw1 |= 0x4;
  }
  public void callAddressInterval8() {
    icw1 &= ~0x4;
  }
  public void cascadeMode() {
    icw1 &= ~0x2;
  }
  public void singleMode() {
    icw1 |= 0x2;
  }
  public void baseVectorAddress(int base) {
    icw2 = base & ~0x7;
  }
  public void slaveControllerIrq(int irq) {
    if(isSlave)
      icw3 = irq;
    else
      icw3 = 1<<irq;
  }
  public void nestedMode() {
    icw4 |= 0x10;
  }
  public void noNestedMode() {
    icw4 &= ~0x10;
  }
  public void nonBufferedMode() {
    icw4 &= ~0xc;
  }
  public void bufferedModeMaster() {
    icw4 |= 0xc;
  }
  public void bufferedModeSlave() {
    nonBufferedMode();
    icw4 |= 0x8;
  }
  public void autoEoi() {
    icw4 |= 2;
  }
  public void normalEoi() {
    icw4 &= ~2;
  }
  public void x86Mode() {
    icw4 |= 1;
  }
  public void mcs80Mode() {
    icw4 &= ~1;
  }
  public void pcSetup() {
    icw1=0x11;
    icw2=0x68;
    if(isSlave) {
      icw3=0x02;
      icw2=0x70;
    } else {
      icw3=0x04;
      icw2=0x68;
    }
    icw4=0x01;
    initialize();
  }
  
  public void slave() {
    isSlave = true;
  }
  public void master() {
    isSlave = false;
  }
  public void initialize() {
    Memory.ioWrite8(port, icw1);
    Memory.ioWrite8(port+1, icw2);
    if((icw1&2)==0) {
      Memory.ioWrite8(port+1, icw3);
      if((icw1&1)!=0)
        Memory.ioWrite8(port+1, icw4);
    }
  }
  
  public void eoi() {
    Memory.ioWrite8(port, 0x20);
  }
  
  public int interruptRequestRegister() {
    Memory.ioWrite8(port+1, 0xa);
    return Memory.ioRead8(port);
  }
  public int interruptInServiceRegister() {
    Memory.ioWrite8(port+1, 0xb);
    return Memory.ioRead8(port);
  }
  public void interruptMask(int mask) {
    Memory.ioWrite8(port+1, mask);
  }
}
