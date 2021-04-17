/**
 * Created on Mar 9, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

/**
 * @author Joe Kulig
 *
 */
public enum PciCapId {
  POWER_MANAGEMENT_INTERFACE(1),
  AGP(2),
  VPD(3),
  SLOT_ID(4),
  MSI(5),
  HOT_SWAP(6),
  PCIX(7),
  HYPER_TRANSPORT(8),
  VENDOR_SPECIFIC(9),
  DEBUG_PORT(0x0A),
  CPCI_CTRL(0x0B),
  HOT_PLUG(0x0C),
  PCI_BRIDGE_SUBSYS_VID(0x0D),
  AGP8X(0x0E),
  SECURE_DEVICE(0x0F),
  PCI_EXPRESS(0x10),
  MSIX(0x11),
  UNKNOWN(0xFF);
  
  private int id;
  
  private PciCapId(int v)
  {
    id = v;
  }
  
  public int value()
  {
    return id;
  }
  
  static public PciCapId valueOf(int enumValue)
  {
    for(PciCapId c: PciCapId.values())
    {
      if(c.id == enumValue)
      {
        return c;
      }
    }
    return UNKNOWN;
  }
}
