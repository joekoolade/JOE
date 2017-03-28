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
public abstract class PciCapability {
  public final static int POWER_MANAGEMENT_INTERFACE = 0x01;
  public final static int AGP                        = 0x02;
  public final static int VPD                        = 0x03;
  public final static int SLOT_ID                    = 0x04;
  public final static int MSI                        = 0x05;
  public final static int HOT_SWAP                   = 0x06;
  public final static int PCIX                       = 0x07;
  public final static int HYPER_TRANSPORT            = 0x08;
  public final static int VENDOR_SPECIFIC            = 0x09;
  public final static int DEBUG_PORT                 = 0x0A;
  public final static int CPCI_CTRL                  = 0x0B;
  public final static int HOT_PLUG                   = 0x0C;
  public final static int PCI_BRIDGE_SUBSYS_VID      = 0x0D;
  public final static int AGP8X                      = 0x0E;
  public final static int SECURE_DEVICE              = 0x0F;
  public final static int PCI_EXPRESS                = 0x10;
  public final static int MSIX                       = 0x11;

  final int id;
  protected final int offset;
  protected final PciDevice device;
  
  public PciCapability(PciDevice device, int id, int offset)
  {
    this.id = id;
    this.offset = offset;
    this.device = device;
  }

  /**
   * @param device
   * @param capPointer
   */
  public PciCapability(PciDevice device, int capPointer)
  {
    int val = device.readConfig32(capPointer);
    id = val & 0xFF;
    offset = (val >> 8) & 0xFF;
    this.device = device;
  }

  /**
   * @return
   */
  public boolean isVendorSpecific()
  {
    return id == VENDOR_SPECIFIC;
  }
  
  public int getOffset()
  {
    return offset;
  }

  /**
   * @param i
   * @return
   */
  public static boolean isVendorSpecific(int id)
  {
    return id == VENDOR_SPECIFIC;
  }

  /**
   * @param id
   * @return
   */
  public static boolean isMsiX(int id)
  {
    // TODO Auto-generated method stub
    return id == MSIX;
  }
  
  public static boolean isMsi(int id)
  {
    return id == MSI;
  }
}
