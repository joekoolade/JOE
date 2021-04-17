/**
 * Created on Mar 7, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

import java.util.ArrayList;
import java.util.Iterator;

import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class PciDevice {
  final short deviceId;
  final short vendorId;
  int bar[];
  final int classCode;
  final int subClassCode;
  final int ifCode;
  final int bus;
  final int slot;
  final int function;
  final int headerType;
  final int revisionId;
  final int subsystemId;
  final int subsystemVendorId;
  final int interruptPin;
  final int interruptLine;
  final int capabilitiesPtr;
  final boolean hasCapabilityList;
  ArrayList<PciCapability> caps;
  
  public PciDevice(int bus, int slot, int function, short deviceId, short vendorId)
  {
    this.deviceId = deviceId;
    this.vendorId = vendorId;
    this.bus = bus;
    this.slot = slot;
    this.function = function;
    bar = new int[6];
    classCode = Pci.getClass(bus, slot, function);
    subClassCode = Pci.getSubClass(bus, slot, function);
    ifCode = Pci.getIfCode(bus, slot, function);
    headerType = Pci.getHeaderType(bus, slot, function);
    revisionId = Pci.getRevisionId(bus, slot, function);
    subsystemId = Pci.getSubSystemId(bus, slot, function);
    subsystemVendorId = Pci.getSubSystemVendorId(bus, slot, function);
    interruptPin = Pci.getInterruptPin(bus, slot, function);
    interruptLine = Pci.getInterruptLine(bus, slot, function);
    capabilitiesPtr = Pci.getCapabilityPointer(bus, slot, function);
    bar[0] = Pci.getBar0(bus, slot, function);
    bar[1] = Pci.getBar1(bus, slot, function);
    bar[2] = Pci.getBar2(bus, slot, function);
    bar[3] = Pci.getBar3(bus, slot, function);
    bar[4] = Pci.getBar4(bus, slot, function);
    bar[5] = Pci.getBar5(bus, slot, function);
    int status = Pci.getStatus(bus, slot, function);
    hasCapabilityList = (status & Pci.CAPLIST) != 0;
//    if((status & Pci.CAPLIST) != 0)
//    {
//      caps = new ArrayList<PciCapability>();
//      int capLocation = Pci.getCapabilityPointer(bus, slot, function);
//      for( ; capLocation != 0; )
//      {
//        VM.sysWrite("CAP ", capLocation); VM.sysWriteln(" ", capabilityId(capLocation));
//        caps.add(new PciCapability(capabilityId(capLocation), capLocation));
//        capLocation = capNextPointer(capLocation);
//      }
//    }
  }
  
  public int status()
  {
    return Pci.getStatus(bus, slot, function);
  }
  public int readConfig32(int offset)
  {
    return Pci.pciConfigRead32(bus, slot, function, offset);
  }
  
  public int getCapabilityPointer()
  {
    return capabilitiesPtr;
  }
  
  /**
   * Returns the capability ID
   * @param capability location
   * @return capability ID
   */
  public int capabilityId(int capOffset)
  {
    return Pci.pciConfigRead32(bus, slot, function, capOffset) & 0xFF;
  }
  
  public int capNextPointer(int capOffset)
  {
    int val = Pci.pciConfigRead32(bus, slot, function, capOffset);
    return (val >> 8) & 0xFF;
  }

  /**
   * @return
   */
  public Iterator<PciCapability> getCapsIter()
  {
    return caps.iterator();
  }

  /**
   * @return
   */
  public int getCapabilityOffset()
  {
    return capabilitiesPtr;
  }
  
  public boolean hasCapabilities()
  {
    return hasCapabilityList;
  }
  
  public String toString()
  {
    String str = "bar0: " + Integer.toHexString(bar[0]) + "\n";
    str = str + "bar1: " + Integer.toHexString(bar[1]) + "\n";
    str = str + "bar2: " + Integer.toHexString(bar[2]) + "\n";
    str = str + "bar3: " + Integer.toHexString(bar[3]) + "\n";
    str = str + "bar4: " + Integer.toHexString(bar[4]) + "\n";
    str = str + "bar5: " + Integer.toHexString(bar[5]) + "\n";
    str = str + "Interrupt pin " + interruptPin + " line " + interruptLine + "\n";
    str = str + "class " + classCode + " subclass " + subClassCode +  " Interface " + ifCode + "\n";
    str = str + "Subsystem Vendor " + Integer.toHexString(subsystemVendorId) + " id " + Integer.toHexString(subsystemId);
    return str;
  }

  public Address getBar(int barIndex)
  {
    if(barIndex < 0 || barIndex > 5)
    {
      VM.sysFail("Bad Bar Index:" + barIndex);
    }
    return Address.fromIntZeroExtend(bar[barIndex]&0xFFFFFFF0);
  }

  /**
   * @param offset
   * @return
   */
  public short readConfig16(int offset)
  {
    return Pci.pciConfigRead16(bus, slot, function, offset);
  }

  public short getCommand()
  {
    return Pci.getCommand(bus, slot, function);
  }
  
  public short getStatus()
  {
    return Pci.getStatus(bus, slot, function);
  }
  /**
   * @param i
   */
  public void writeConfig16(int offset, short value)
  {
    Pci.pciConfigWrite16(bus, slot, function, offset, value);
  }
  
  /**
   * Make device a bus master
   */
  public void busMaster()
  {
    short cmd = Pci.getCommand(bus, slot, function);
    cmd |= Pci.CMD_BUS_MASTER;
    Pci.setCommand(bus, slot, function, cmd);
  }
  public void disableInterrupt()
  {
    short cmd = Pci.getCommand(bus, slot, function);
    cmd |= Pci.CMD_DISABLE_INT;
    Pci.setCommand(bus, slot, function, cmd);
  }
  public boolean isEthernetController()
  {
    return (classCode==2) && (subClassCode==0);
  }
}
