/**
 * Created on Apr 5, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class RTC {
  private final static Address registerPort = Address.fromIntSignExtend(0x70);
  private final static Address dataPort = Address.fromIntSignExtend(0x71);
  
  private final static byte CURRENT_SEC_REG = 0x00;
  private final static byte ALARM_SEC_REG = 0x01;
  private final static byte CURRENT_MIN_REG = 0x02;
  private final static byte ALARM_MIN_REG = 0x03;
  private final static byte CURRENT_HOUR_REG = 0x04;
  private final static byte ALARM_HOUR_REG = 0x05;
  private final static byte DAY_OF_WEEK_REG = 0x06;
  private final static byte DAY_OF_MONTH_REG = 0x07;
  private final static byte MONTH_REG = 0x08;
  private final static byte YEAR_REG = 0x09;
  private final static byte STATUSA_REG = 0x0A;
  private final static byte STATUSB_REG = 0x0B;
  private final static byte STATUSC_REG = 0x0C;
  private final static byte STATUSD_REG = 0x0D;
  
  /*
   * Status register A flags
   */
  private final static byte SRA_UIP = (byte)(1<<7);
  private final static byte SRA_DIV_SHIFT = 4;
  private final static byte RATE_256HZ0 = 1;
  private final static byte RATE_128HZ0 = 2;
  private final static byte RATE_8P192KHZ = 3;
  private final static byte RATE_4P096KHZ = 4;
  private final static byte RATE_2P048KHZ = 5;
  private final static byte RATE_1P024KHZ = 6;
  private final static byte RATE_512HZ = 7;
  private final static byte RATE_256HZ = 8;
  private final static byte RATE_128HZ = 9;
  private final static byte RATE_64HZ = 10;
  private final static byte RATE_32HZ = 11;
  private final static byte RATE_16HZ = 12;
  private final static byte RATE_8HZ = 13;
  private final static byte RATE_4HZ = 14;
  private final static byte RATE_2HZ = 15;
  
  /*
   * Status register B flags
   */
  private final static byte SRB_HALT = (byte) (1<<7);
  private final static byte SRB_EPI = 1<<6;
  private final static byte SRB_EAI = 1<<5;
  private final static byte SRB_EUI = 1<<4;
  private final static byte SRB_SQWE = 1<<3;
  private final static byte SRB_BCDFMT = 1<<2;
  private final static byte SRB_24HR = 1<<1;
  private final static byte SRB_EDST = 1<<0;
  
  /*
   * Status register C flags
   */
  private final static byte SRC_IRF = (byte) (1<<7);
  private final static byte SRC_PIF = 1<<6;
  private final static byte SRC_AIF = 1<<5;
  private final static byte SRC_UIF = 1<<4;
  
  /*
   * Status register D flags
   */
  private final static byte SRD_RTCPWR = (byte) (1<<7);
  
  /*
   * Write byte to RTC register port, 0x70
   */
  final static void writeRegisterPort(byte register)
  {
    registerPort.ioStore(register);
  }
  
  /*
   * Read byte from RTC data port 0x71
   */
  final static byte readDataPort()
  {
    return dataPort.ioLoadByte();
  }
  
  /*
   * Write byte to RTC data port 0x71
   */
  final static void writeDataPort(byte value)
  {
    dataPort.ioStore(value);
  }
  
  public static final void setRate(byte rate)
  {
    writeRegisterPort(STATUSA_REG);
    byte registerValue = readDataPort();
    registerValue &= ~0xF;
    registerValue |= rate;
    writeRegisterPort(STATUSA_REG);
    writeDataPort(registerValue);
  }
  
  /**
   * Returns true if the PF bit in status register C is set
   * @return true if PF bit is set
   */
  public final static boolean hasPeriodicInterrupt()
  {
    writeRegisterPort(STATUSC_REG);
    byte registerValue = readDataPort();
    return (registerValue & SRC_PIF) == SRC_PIF;
  }

  /**
   * 
   */
  public static void rate8Hz()
  {
    setRate(RATE_8HZ);
  }
  
  public static void enableSWE()
  {
    writeRegisterPort(STATUSB_REG);
    byte registerValue = readDataPort();
    //registerValue |= 
  }

  public final static boolean isUpdating()
  {
    writeRegisterPort(STATUSA_REG);
    return (readDataPort() & SRA_UIP) != 0;
  }
  /**
   * @return
   */
  public static byte getSecond()
  {
    while(isUpdating())
    {
    }
    writeRegisterPort(CURRENT_SEC_REG);    
    return readDataPort();
  }
}
