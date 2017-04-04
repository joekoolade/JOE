/**
 * Created on Mar 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciDevice;

/**
 * @author Joe Kulig
 *
 */
public class NetDeviceCfg extends DeviceCfg {
  final byte macAddress[];
  
  /**
   * @param device
   * @param capPointer
   * @param capInfo
   */
  public NetDeviceCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer, capInfo);
    macAddress = new byte[6];
    for(int i=0; i < 6; i++)
    {
      macAddress[i] = getByte(i);
    }
  }

  public int getStatus()
  {
    int status = getShort(6);
    return status & 0xFFFF;
  }
  
  public boolean linkUp()
  {
    return (getShort(6) & 0x1) == 0x1;
  }
  
  public byte[] getMacAddress()
  {
    return macAddress;
  }
  
  public String toString()
  {
    return "MAC: " + Integer.toHexString((int)macAddress[0]&0xFF) + ":"+ Integer.toHexString((int)macAddress[1]&0xFF) + ":"
    + Integer.toHexString((int)macAddress[2]&0xFF) + ":"+ Integer.toHexString((int)macAddress[3]&0xFF) + ":"
    + Integer.toHexString((int)macAddress[4]&0xFF) + ":"+ Integer.toHexString((int)macAddress[5]&0xFF);
  }
}
