/**
 * Created on Mar 4, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class Pci {
  private static final int VENDOR_OFFSET    = 0;
  private static final int DEVICE_OFFSET    = 2;
  private static final int COMMAND_OFFSET   = 4;
  private static final int STATUS_OFFSET    = 6;
  private static final int REVID_OFFSET     = 8;
  private static final int CLASS_OFFSET     = 10;
  private static final int CACHESIZE_OFFSET = 12;
  private static final int HEADER_OFFSET    = 14;
  private static final int BAR0_OFFSET      = 16;
  private static final int BAR1_OFFSET      = 20;
  private static final int BAR2_OFFSET      = 24;
  private static final int BAR3_OFFSET      = 28;
  private static final int BAR4_OFFSET      = 32;
  private static final int BAR5_OFFSET      = 36;
  private static final int SUBVID_OFFSET    = 44;
  private static final int SUBSYSID_OFFSET  = 46;
  private static final int CAPPTR_OFFSET    = 52;
  private static final int INTR_OFFSET      = 60;
  
  static final int CAPLIST = 0x10;
  static final int MHZ66   = 0x20;
  static final int FASTCAP = 0x80;
  
  private static final int PCI_CONFIG_ENABLE = 0x80000000;
  private static final Address configAddress = Address.fromIntZeroExtend(0xcf8);
  private static final Address configData = Address.fromIntZeroExtend(0xcfc);
  private static final short NO_VENDOR_ID = (short)0xffff;
  
  private static final int MAX_PCI_DEVICES = 32;
  private static int deviceIndex = 0;
  private static PciDevice devices[];
  
  static public short pciConfigRead16(int bus, int slot, int function, int offset)
  {
    int data=0;
    int address = (bus<<16) | (slot<<11) | (function<<8) | (offset&0xFC) | PCI_CONFIG_ENABLE;
    
    configAddress.ioStore(address);
    data = configData.ioLoadInt();
    data = (data >> (offset & 2) * 8) & 0xffff;
    return (short)data;
  }
  
  static public int pciConfigRead32(int bus, int slot, int function, int offset)
  {
    int address = (bus<<16) | (slot<<11) | (function<<8) | (offset&0xFC) | PCI_CONFIG_ENABLE;
    configAddress.ioStore(address);
    return configData.ioLoadInt();
  }
  
  static public void boot()
  {
    devices = new PciDevice[MAX_PCI_DEVICES];
  }
  /*
   * Search for devices on pci bus
   */
  static public void enumeratePci()
  {
    int bus, slot;
    
    VM.sysWriteln("PCI Bus");
    for(bus=0; bus < 256; bus++)
    {
      for(slot = 0; slot < 32; slot++)
      {
        checkDevice(bus, slot);
      }
    }
  }
  
  static void checkDevice(int bus, int slot)
  {
    short vendorId = getVendorId(bus, slot, 0);
    if(vendorId == NO_VENDOR_ID)
    {
      return;
    }
    short deviceId = getDeviceId(bus, slot, 0);
    devices[deviceIndex] = new PciDevice(bus, slot, 0, deviceId, vendorId);
    deviceIndex++;
    byte devClass = getClass(bus, slot, 0);
    byte subClass = getSubClass(bus, slot, 0);
    VM.sysWrite("Bus ", bus);  VM.sysWrite(" Device ", slot);
    VM.sysWrite(" Vendor ID ", VM.intAsHexString(vendorId)); VM.sysWriteln(" Device ID ", VM.intAsHexString(deviceId));
    VM.sysWrite("Class ", devClass); VM.sysWriteln(" SubClass ", subClass);
  }
  
  static byte getSubClass(int bus, int device, int function)
  {
    short val=pciConfigRead16(bus, device, function, CLASS_OFFSET);;
    return (byte)(val & 0xFF);
  }
  
  static byte getClass(int bus, int device, int function)
  {
    int val=pciConfigRead16(bus, device, function, CLASS_OFFSET);
    return (byte)((val>>8) & 0xFF);
  }
  
  static short getVendorId(int bus, int device, int function)
  {
    short val=pciConfigRead16(bus, device, function, VENDOR_OFFSET);
    return val;
  }
  
  static short getDeviceId(int bus, int device, int function)
  {
    short val=pciConfigRead16(bus, device, function, DEVICE_OFFSET);
    return val;
  }
  
  static int getCapabilityPointer(int bus, int device, int function)
  {
    int val=pciConfigRead16(bus, device, function, CAPPTR_OFFSET);
    return val & 0xFF;
  }
  
  static int getIfCode(int bus, int slot, int function)
  {
    int val = pciConfigRead16(bus, slot, function, REVID_OFFSET);
    return (val >> 8) & 0xFF;
  }
  
  static int getRevisionId(int bus, int slot, int function)
  {
    int val = pciConfigRead16(bus, slot, function, REVID_OFFSET);
    return val & 0xFF;
  }
  
  static int getInterruptLine(int bus, int slot, int function)
  {
    int val = pciConfigRead16(bus, slot, function, INTR_OFFSET);
    return val & 0xFF;
  }
  
  static int getInterruptPin(int bus, int slot, int function)
  {
    int val = pciConfigRead16(bus, slot, function, INTR_OFFSET);
    return (val >> 8) & 0xFF;
  }
  
  static int getBar0(int bus, int slot, int function)
  {
    return pciConfigRead32(bus, slot, function, BAR0_OFFSET);
  }
  
  static int getBar1(int bus, int slot, int function)
  {
    return pciConfigRead32(bus, slot, function, BAR1_OFFSET);
  }
  
  static int getBar2(int bus, int slot, int function)
  {
    return pciConfigRead32(bus, slot, function, BAR2_OFFSET);
  }
  
  static int getBar3(int bus, int slot, int function)
  {
    return pciConfigRead32(bus, slot, function, BAR3_OFFSET);
  }
 
  static int getBar4(int bus, int slot, int function)
  {
    return pciConfigRead32(bus, slot, function, BAR4_OFFSET);
  }
  
  static int getBar5(int bus, int slot, int function)
  {
    return pciConfigRead32(bus, slot, function, BAR5_OFFSET);
  }
  
  final static int getStatus(int bus, int slot, int function)
  {
    return pciConfigRead16(bus, slot, function, STATUS_OFFSET);
  }
  
  final static int getCommand(int bus, int slot, int function)
  {
    return pciConfigRead16(bus, slot, function, COMMAND_OFFSET);
  }
  
  final static int getSubSystemId(int bus, int slot, int function)
  {
    return pciConfigRead16(bus, slot, function, SUBSYSID_OFFSET);
  }
  
  final static int getSubSystemVendorId(int bus, int slot, int function)
  {
    return pciConfigRead16(bus, slot, function, SUBVID_OFFSET);
  }
  
  final static int getHeaderType(int bus, int slot, int function)
  {
    int val = pciConfigRead16(bus, slot, function, HEADER_OFFSET);
    return val & 0xFF;
  }

  /**
   * Find pci device by vendor and device id
   * 
   * @param vendorId  pci vendor id
   * @param deviceId  pci device id
   * @return  PciDevice object if found otherwise NULL
   */
  public static PciDevice find(int vendorId, int deviceId)
  {
    for(int slot=0; slot < deviceIndex; slot++)
    {
      if(devices[slot].vendorId == vendorId && devices[slot].deviceId == deviceId)
      {
        return devices[slot];
      }
    }
    return null;
  }

  /**
   * @param bus
   * @param slot
   * @param function
   * @param offset
   * @param value
   */
  public static void pciConfigWrite16(int bus, int slot, int function, int offset, short value)
  {
    int address = (bus<<16) | (slot<<11) | (function<<8) | (offset&0xFC) | PCI_CONFIG_ENABLE;
    
    configAddress.ioStore(address);
    configData.ioStore(value);
  }

  /**
   * @param bus
   * @param slot
   * @param function
   * @param offset
   * @param value
   */
  public static void pciConfigWrite32(int bus, int slot, int function, int offset, int value)
  {
    int address = (bus<<16) | (slot<<11) | (function<<8) | (offset&0xFC) | PCI_CONFIG_ENABLE;
    
    configAddress.ioStore(address);
    configData.ioStore(value);
  }
}
