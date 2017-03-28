/**
 * Created on Mar 15, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciCapability;
import org.jam.board.pc.PciDevice;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class MsiXCap extends PciCapability {

  boolean enabled;
  boolean functionMasked;
  final int tableSize;
  final Offset tableOffset;
  final int tableBar;
  final Offset pbaOffset;
  final int pbaBar;
  
  private static final int MSIX_ENABLED  = 0x8000;
  private static final int FUNCTION_MASK = 0x4000;

  /**
   * @param pci
   * @param capField
   * @param capPointer
   */
  public MsiXCap(PciDevice device, int capField, int capPointer)
  {
    super(device, capPointer);
    int value = (capField >> 16) & 0xFFFF;
    enabled = (value & MSIX_ENABLED) == MSIX_ENABLED;
    functionMasked = (value & FUNCTION_MASK) == FUNCTION_MASK;
    tableSize = (value & 0x7FF) + 1;
    value = device.readConfig32(capPointer+4);
    tableBar = value & 0x7;
    tableOffset = Offset.fromIntZeroExtend((value >> 3) & 0x1FFFFFFF);
    value = device.readConfig32(capPointer+8);
    pbaBar = value & 0x7;
    pbaOffset = Offset.fromIntZeroExtend((value >> 3) & 0x1FFFFFFF);
  }

  public void enableInterrupts()
  {
    short control = device.readConfig16(offset+2);
    control |= MSIX_ENABLED;
    device.writeConfig16(offset+2, control);
  }
  
  public String toString()
  {
    return (enabled?" ENABLED ":"") + (functionMasked?" MASKED ":"") + " size " + tableSize + " table " +
    tableBar + "/" + Integer.toHexString(tableOffset.toInt()) + " pba " + pbaBar + "/" + Integer.toHexString(pbaOffset.toInt());
  }
}
