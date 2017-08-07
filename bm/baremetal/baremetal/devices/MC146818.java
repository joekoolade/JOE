/*
 * Created on Aug 31, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.devices;

import baremetal.kernel.Memory;
import baremetal.kernel.Utils;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MC146818 {
  /*
   * Registers
   */
  public final static int SECOND=0;
  public final static int ALARM_SECOND=1;
  public final static int MINUTE=2;
  public final static int ALARM_MINUTE=3;
  public final static int HOUR=4;
  public final static int ALARM_HOUR=5;
  public final static int DAY_OF_WEEK=6;
  public final static int DAY_OF_MONTH=7;
  public final static int MONTH=8;
  public final static int YEAR=9;
  public final static int STATUS_REG_A=10;
  public final static int STATUS_REG_B=11;
  public final static int STATUS_REG_C=12;
  public final static int STATUS_REG_D=13;
  
  /*
   * Status register A
   */
  public final static int UIP=0x80;   // Update in progress
  
  /*
   * Status register B
   */
  public final static int SET=0x80;
  public final static int PIE=0x40;
  public final static int AIE=0x20;
  public final static int UIE=0x10;
  public final static int SQWE=0x08;
  public final static int DM=0x04;
  public final static int MODE24=0x02;
  public final static int DSE=0x01;
  
  /*
   * Status register C
   */
  public final static int IRQF=0x80;
  public final static int PF=0x40;
  public final static int AF=0x20;
  public final static int UF=0x10;
  
  /*
   * Status register D
   */
  public final static int VRT=0x80;
  
  /*
   * Rate selections
   */
  public final static int RATE_NONE=0;
  public final static int RATE_256HZ=1;   // 3.90625ms
  public final static int RATE_128HZ=2;   // 7.8125ms
  public final static int RATE_8192HZ=3;  // 122.070us
  public final static int RATE_4096HZ=4;  // 244.141us
  public final static int RATE_2048HZ=5;  // 488.281us
  public final static int RATE_1024HZ=6;  // 976.5626us
  public final static int RATE_512HZ=7;   // 1.953125ms
  public final static int RATE1_256HZ=8;   // 3.90625ms
  public final static int RATE1_128HZ=9;   // 7.8125ms
  public final static int RATE_64HZ=10;   // 15.625ms
  public final static int RATE_32HZ=11;   // 31.25ms
  public final static int RATE_16HZ=12;   // 62.50ms
  public final static int RATE_8HZ=13;    // 125ms
  public final static int RATE_4HZ=14;    // 250ms
  public final static int RATE_2HZ=15;    // 500ms
  protected int addressPort;
  protected int dataPort;
  
  public MC146818(int addrPort, int dataPort) {
    addressPort = addrPort;
    this.dataPort = dataPort;
  }
  public int statusRegA() {
    Memory.ioWrite8(addressPort, STATUS_REG_A);
    Utils.ioDelay();
    return Memory.ioRead8(dataPort);
  }
  public void statusRegA(int val) {
    Memory.ioWrite8(addressPort, STATUS_REG_A);
    Utils.ioDelay();
    Memory.ioWrite8(dataPort, val);
  }
  public int statusRegB() {
    Memory.ioWrite8(addressPort, STATUS_REG_B);
    Utils.ioDelay();
    return Memory.ioRead8(dataPort);
  }
  public void statusRegB(int val) {
    Memory.ioWrite8(addressPort, STATUS_REG_B);
    Utils.ioDelay();
    Memory.ioWrite8(dataPort, val);
  }
  public int statusRegC() {
    Memory.ioWrite8(addressPort, STATUS_REG_C);
    Utils.ioDelay();
    return Memory.ioRead8(dataPort);
  }
  public int statusRegD() {
    Memory.ioWrite8(addressPort, STATUS_REG_D);
    Utils.ioDelay();
    return Memory.ioRead8(dataPort);
  }
  public void setRate(int rate) {
    int reg = statusRegA()&0xf0;
    reg |= (rate&0xf);
    Utils.ioDelay();
    statusRegA(reg);
  }
  public boolean hasPeriodicInt() {
    int val = statusRegC();
    return (val & PF) != 0;
  }
  public boolean hasAlarmInt() {
    int val = statusRegC();
    return (val & AF) != 0;
  }
  public boolean isUpdateEnded() {
    int val = statusRegC();
    return (val & UF) != 0;
  }
  
}
