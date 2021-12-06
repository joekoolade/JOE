/**
 * Created on Mar 15, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.MessageAddressRegister;
import org.jam.board.pc.MessageDataRegister;
import org.jam.board.pc.PciCapability;
import org.jam.board.pc.PciDevice;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;
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
  final Address msixTable;
  final Address pbaTable;
  
  private static final int MSIX_ENABLED  = 1<<15;
  private static final int FUNCTION_MASK = 1<<14;

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
    msixTable = device.getBar(tableBar).plus(tableOffset);
    pbaTable = device.getBar(pbaBar).plus(pbaOffset);
  }

  public void enableInterrupts()
  {
    short control = device.readConfig16(offset+2);
    control |= MSIX_ENABLED;
    enabled = true;
//    VM.sysWriteln("msix control: ", Integer.toHexString(control));
    device.writeConfig16(offset+2, control);
  }
  
  public void disableInterrupts()
  {
    short control = device.readConfig16(offset+2);
    control &= ~MSIX_ENABLED;
    enabled = true;
//    VM.sysWriteln("msix control: ", Integer.toHexString(control));
    device.writeConfig16(offset+2, control);
  }
  
  public short getControl()
  {
    return device.readConfig16(offset+2);
  }
  
  public String toString()
  {
    String str = (enabled?" ENABLED ":"") + (functionMasked?" MASKED ":"") + " size " + tableSize + " table " +
    tableBar + "/" + Integer.toHexString(tableOffset.toInt()) + " pba " + pbaBar + "/" + Integer.toHexString(pbaOffset.toInt());
    str += "\nMSIX: " + Integer.toHexString(msixTable.toInt()) + " PBA: " + Integer.toHexString(pbaTable.toInt());
    return str;
  }
  
  public void setMessageAddress(int entry, MessageAddressRegister messageAddress)
  {
    msixTable.store(messageAddress.toRegister(), Offset.zero().plus(entry<<4));
  }
  
  public void setUpperMessageAddress(int entry, int upperAddress)
  {
    msixTable.store(upperAddress, Offset.zero().plus((entry<<4) + 4));
  }
  
  public void setMessageData(int entry, MessageDataRegister messageData)
  {
    msixTable.store(messageData.toRegister(), Offset.zero().plus((entry<<4) + 8));
  }
  
  public void enableInterrupt(int entry)
  {
    msixTable.store(0, Offset.zero().plus((entry<<4) + 12));
  }

  public void disableInterrupt(int entry)
  {
    msixTable.store(1, Offset.zero().plus((entry<<4) + 12));
  }
}
