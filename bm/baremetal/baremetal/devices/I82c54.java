/*
 * Created on Sep 3, 2004
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
public class I82c54 {
  int counter0;
  int counter1;
  int counter2;
  int control;
  
  
  public final static int MODE0 = 0x00;
  public final static int MODE1 = 0x02;
  public final static int MODE2 = 0x04;
  public final static int MODE3 = 0x06;
  public final static int MODE4 = 0x08;
  public final static int MODE5 = 0x0a;
  
  public I82c54(int c0, int c1, int c2, int ctrl) {
    counter0 = c0;
    counter1 = c1;
    counter2 = c2;
    control = ctrl;
  }
  
  public void control(int v) {
    Memory.ioWrite8(control, v);
  }
  public int control() {
    return Memory.ioRead8(control);
  }
  
  /*
   * Selects which counter mode will be programmed.
   */
  public void counter0(int mode, int count) {
    /*
     * Write control, lsb, msb
     */
    Memory.ioWrite8(control, 0x30|mode);
    Memory.ioWrite8(counter0, count&0xff);
    Memory.ioWrite8(counter0, (count&0xff00)>>8);
  }
  
  public void counter1(int mode, int count) {
    /*
     * Write control, lsb, msb
     */
    Memory.ioWrite8(control, 0x70|mode);
    Memory.ioWrite8(counter1, count&0xff);
    Memory.ioWrite8(counter1, (count&0xff00)>>8);
    
  }
  public void counter2(int mode, int count) {
    /*
     * Write control, lsb, msb
     */
    Memory.ioWrite8(control, 0xb0|mode);
    Memory.ioWrite8(counter2, count&0xff);
    Memory.ioWrite8(counter2, (count&0xff00)>>8);
    
  }
  
  public int counter0() {
    Memory.ioWrite8(control, 0);
    int count = Memory.ioRead8(counter0);
    count |= Memory.ioRead8(counter0)<<8;
    return count;
  }

  public int counter1() {
    Memory.ioWrite8(control, 0x40);
    int count = Memory.ioRead8(counter1);
    count |= Memory.ioRead8(counter1)<<8;
    return count;
  }
  
  public int counter2() {
    Memory.ioWrite8(control, 0x80);
    int count = Memory.ioRead8(counter2);
    count |= Memory.ioRead8(counter2)<<8;
    return count;
  }
}
