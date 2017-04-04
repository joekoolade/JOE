/**
 * Created on Mar 10, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciCapability;
import org.jam.board.pc.PciDevice;
import org.jikesrvm.VM;

/**
 * @author Joe Kulig
 *
 */
abstract public class VirtioPciCap extends PciCapability {
  int type;
  int bar;
  int offset;
  int length;
  int capLength;
  
  final static int COMMON_CFG = 1;
  final static int NOTIFY_CFG = 2;
  final static int ISR_CFG    = 3;
  final static int DEVICE_CFG = 4;
  final static int PCI_CFG    = 5;
  
  /**
   * @param device
   * @param capPointer
   */
  public VirtioPciCap(PciDevice device, int capPointer)
  {
    super(device, capPointer);
    int val = device.readConfig32(capPointer);
    capLength = (val >> 16) & 0xFF;
    type = (val >> 24) & 0xFF;
    bar = device.readConfig32(capPointer+4) & 0xFF;
    offset = device.readConfig32(capPointer+8);
    length = device.readConfig32(capPointer+12);
  }

  public String toString()
  {
    String str = "virtio cap type " + type + " caplen " + capLength + " bar " + bar +  " " + offset + "/" + length;
    return str;
  }
  
  static public VirtioPciCap createCap(PciDevice device, int capInfo, int capPointer)
  {
    VirtioPciCap cap=null;
    
    int type = (capInfo >> 24) & 0xFF;
    // VM.sysWrite("createCap ", type);  VM.sysWriteln(" ", VM.intAsHexString(capInfo));
    if(type == NOTIFY_CFG)
    {
      return new NotifyCfg(device, capPointer, capInfo);
    }
    else if(type == COMMON_CFG)
    {
      return new CommonCfg(device, capPointer, capInfo);
    }
    else if(type == ISR_CFG)
    {
      return new IsrCfg(device, capPointer, capInfo);
    }
    else if(type == DEVICE_CFG && device.isEthernetController())
    {
      return new NetDeviceCfg(device, capPointer, capInfo);
    }
    else if(type == PCI_CFG)
    {
      return new PciCfg(device, capPointer, capInfo);
    }
    return cap;
  }
}
