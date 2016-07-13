/*
 * Created on Oct 12, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.board.pc;

import org.vmmagic.unboxed.Address;

/**
 * @author joe
 *
 */
public class I8259A {
  /*
   * ICW1 initialization word
   *  bit 7-5 	= 0  only used in 80/85 mode
   *  bit 4   	= 1  ICW1 is being issued
   *  bit 3   	= 0  edge triggered mode
   *          	= 1  level triggered mode
   *  bit 2   	= 0  successive interrupt vectors use 8 bytes
   *          	= 1  successive interrupt vectors use 4 bytes
   *  bit 1   	= 0  cascade mode
   *          	= 1  single mode, no ICW3 needed
   *  bit 0   	= 0  no ICW4 needed
   *          	= 1  ICW4 needed
    ICW2:
     bit 7-3  	= address lines A0-A3 of base vector address for PIC
     bit 2-0  	= reserved
    ICW3:
     bit 7-0  	= 0  slave controller not attached to corresponding
            	   	 interrupt pin
       		  	= 1  slave controller attached to corresponding
            		 interrupt pin
    ICW4:
     bit 7-5 	= 0  reserved
     bit 4  	= 0  no special fully-nested mode
       			= 1  special fully-nested mode
     bit 3-2 	= 0x nonbuffered mode
       			= 10 buffered mode/slave
       			= 11 buffered mode/master
     bit 1   	= 0  normal EOI
       			= 1  Auto EOI
     bit 0   	= 0  8085 mode
       			= 1  8086/8088 mode

   */
  private byte icw1,icw2,icw3,icw4;
  private boolean isSlave;
  private Address cmd, data;
  
  final public static int ICW1_INIT  = 0x10;
  final public static int ICW1_LTIM  = 0x08;
  final public static int ICW1_ADI   = 0x04;
  final public static int ICW1_SNGL  = 0x02;
  final public static int ICW1_IC2   = 0x01;
  final public static int ICW2_BASE_ADDR_MASK = 0x7;
  final public static int ICW3_SLAVE = 0x1;
  final public static int ICW4_SFNM  = 0x10;
  final public static int ICW4_BUF   = 0x08;
  final public static int ICW4_MS    = 0x04;
  final public static int ICW4_AEOI  = 0x02;
  final public static int ICW4_8086MODE = 0x01;
  
  public static final byte SYSTEM_TIMER = 0x01;
  public static final byte SLAVE = 0x04;
  public static final byte COM2 = 0x08;
  public static final byte COM1 = 0x10;
  
  private Address delay;
  
  public I8259A(int port) {
    cmd = Address.fromIntZeroExtend(port);
    data = Address.fromIntZeroExtend(port+1);
    delay = Address.fromIntSignExtend(0x80);
    isSlave = false;
  }
  
  public I8259A(int port, boolean isSlave)
  {
      this(port);
      this.isSlave = isSlave;
  }
  public void slaveControllerIrq(byte irq) {
    if(isSlave)
      icw3 = irq;
    else
      icw3 = (byte)(1<<irq);
  }
  public void pcSetup() {
    icw1=ICW1_INIT|ICW1_IC2;
    /*
     * IRQ vector remapping
     * Master PIC is 0x20-0x27
     * Slave PIC is  0x28-0x2F
     */
    if(isSlave) {
      icw3=0x02; // slave id 2; cascade identity
      icw2=0x28;
    } else {
      icw3=0x04; // slave PIC is on IRQ2
      icw2=0x20;
    }
    icw4=ICW4_8086MODE;
    initialize();
    // disable all interrupts
    interruptMask(0xFF);
  }
  
  public void slave() {
    isSlave = true;
  }
  public void master() {
    isSlave = false;
  }
  public void initialize() {
    cmd.ioStore(icw1);
    data.ioStore(icw2);
    if((icw1&2)==0) {
        data.ioStore(icw3);
        if((icw1&1)!=0)
        {
          data.ioStore(icw4);
        }
    }
  }
  
  public void eoi() {
      cmd.ioStore((byte) 0x20);
  }
  
    /**
     * Read the interrupt request register (IRR).
     * This indicates which interrupts have raised
     * @return IRR value
     */
    public int interruptRequestRegister()
    {
        data.ioStore((byte) 0xa);
        return cmd.ioLoadByte();
    }

    /**
     * Read the in-service register (ISR)
     * This indicates which interrupt being service (sent to the CPU)
     * @return
     */
    public int interruptInServiceRegister()
    {
        data.ioStore((byte) 0xb);
        return cmd.ioLoadByte();
    }

    /**
     * Sets the interrupt mask
     * @param i
     */
    final public void interruptMask(int i)
    {
        data.ioStore(i);
    }
    
    /**
     * Enables an individual interrupt of the interrupt mask
     * @param irq
     */
    final public void setInterrupt(byte irq)
    {
        byte interruptMask;
        
        interruptMask = data.ioLoadByte();
        interruptMask &= ~irq;
        data.ioStore(interruptMask);
    }
}
