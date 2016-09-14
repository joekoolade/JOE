/*
 * Created on Sep 3, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.board.pc;

import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 * 
 */
public class I82c54 {
  Address counter0;
  Address counter1;
  Address counter2;
  Address control;
  
  final private static int SC0 = 0;			// select counter 0
  final private static int SC1 = 0x40;		// select counter 1
  final private static int SC2 = 0x80;		// select counter 2
  final private static int RBC = 0xc0;		// read back command
  
  final private static int CLC  = 0;				// counter latch command
  final private static int RWL  = 0x10;				// read/write LSB
  final private static int RWM	= 0x20;				// read/write MSB
  final private static int RWLM = 0x30;				// read/write LSB,MSB
  
  final private static int BCD = 1;					// BCD counter
  
  /*
   * Interrupt on a terminal count; event counting
   */
  public final static int MODE0 = 0x00;
  /*
   * Hardware retriggerable one shot
   */
  public final static int MODE1 = 0x02;
  /*
   * rate generator
   */
  public final static int MODE2 = 0x04;
  /*
   * square wave mode
   */
  public final static int MODE3 = 0x06;
  /*
   * software triggered mode
   */
  public final static int MODE4 = 0x08;
  /*
   * hardware triggered strobe
   */
  public final static int MODE5 = 0x0a;
  
  public I82c54() {
      /*
       * Set up hardware IO addresses
       */
    counter0 = Address.fromIntZeroExtend(0x40);
    counter1 = Address.fromIntZeroExtend(0x41);
    counter2 = Address.fromIntZeroExtend(0x42);
    control = Address.fromIntZeroExtend(0x43);
  }
  
  public void control(byte v) {
    control.ioStore(v);
  }
  public byte control() {
    return control.ioLoadByte();
  }
  
  /*
   * Selects which counter mode will be programmed.
   */
  public void counter0(int mode, int count) {
    /*
     * Write control, lsb, msb
     */
    control.ioStore(SC0|RWLM|mode);
    counter0.ioStore(count&0xff);
    counter0.ioStore((count&0xff00)>>8);
  }
  
  public void counter1(int mode, int count) {
    /*
     * Write control, lsb, msb
     */
	control.ioStore(SC1|RWLM|mode);
    counter1.ioStore(count&0xff);
    counter1.ioStore((count&0xff00)>>8);
    
  }
  public void counter2(int mode, int count) {
    /*
     * Write control, lsb, msb
     */
	control.ioStore(SC2|RWLM|mode);
    counter2.ioStore(count&0xff);
    counter2.ioStore((count&0xff00)>>8);
    
  }
  
  public int counter0() {
    control.ioStore(CLC|SC0);;
    int count = counter0.ioLoadByte();
    count &= 0xff;
    count |= (counter0.ioLoadByte() & 0xff) << 8;
    return count;
  }

  public int counter1() {
    control.ioStore(CLC|SC1);;
    int count = counter1.ioLoadByte();
    count &= 0xff;
    count |= (counter1.ioLoadByte() & 0xff) << 8;
    return count;
  }
  
  public int counter2() {
    control.ioStore(CLC|SC2);;
    int count = counter2.ioLoadByte();
    count &= 0xff;
    count |= (counter2.ioLoadByte() & 0xff) << 8;
    return count;
  }
}
