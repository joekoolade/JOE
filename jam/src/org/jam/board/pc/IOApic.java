/**
 * Created on Mar 30, 2017
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
 * Class that can program the IO-APIC. Class is abstract to force the implementation for 
 * specific IO-APIC IRQ routings that are specific for motherboards.
 * 
 */
public abstract class IOApic {
  // Hard coding for now
  Address IOREGSEL;
  Address IOWIN;
  
  final private static int IOAPICID = 0x00;
  final private static int IOAPICVER = 0x01;
  final private static int IOAPICARB = 0x02;
  final private static int IOREDTBL = 0x10;
  private static final long LOGICAL_MODE = 1<<11;
  private static final long INTERRUPT_MASK = 1<<16;
  private static final long DELMOD_FIXED = 0<<8;
  private static final long DELMOD_LOWPRIORITY = 1<<8;
  private static final long TRIGGER_MODE = 1<<15;
  private static final long INTPOL_FLAG = 1<<13;
  private static final int DEFAULT_ADDRESS = 0xFEC00000;
  
  final private int id;
  final private int version;
  final private int maxRedirEntry;
  
  public IOApic(int baseAddress)
  {
    IOREGSEL = Address.fromIntZeroExtend(baseAddress);
    IOWIN = Address.fromIntZeroExtend(baseAddress+0x10);
    int val = ioApicId();
    id = (val >> 24) & 0xF;
    val = ioApicVer();
    maxRedirEntry = (val >> 16) & 0xFF;
    version = val & 0xFF;
  }
  
  public IOApic()
  {
    this(DEFAULT_ADDRESS);
  }
  
  protected abstract void boot();
  
  final private void ioRegSel(int register)
  {
    IOREGSEL.store(register);
  }
  
  final private int getIoWin()
  {
    return IOWIN.loadInt();
  }
  
  final private void setIoWin(int value)
  {
    IOWIN.store(value);
  }
  
  final public int ioApicId()
  {
    ioRegSel(IOAPICID);
    return getIoWin();
  }
  
  public final int ioApicVer()
  {
    ioRegSel(IOAPICVER);
    int val = getIoWin();
    return val;
  }
  
  public final void setIoRedTable(int tableEntry, long value)
  {
    int entryValue = (int)(value & 0xFFFFFFFF);
    tableEntry *= 2;
    ioRegSel(IOREDTBL + tableEntry);
    setIoWin(entryValue);
    entryValue = (int)((value >> 32) & 0xFFFFFFFF);
    ioRegSel(IOREDTBL + tableEntry + 1);
    setIoWin(entryValue);
  }
  
  public final long getIoRedTable(int tableEntry)
  {
    long value;
    
    tableEntry *= 2;
    ioRegSel(IOREDTBL + tableEntry);
    value = getIoWin();
    ioRegSel(IOREDTBL + tableEntry + 1);
    long entryValue = getIoWin();
    value |= (entryValue << 32);
    return value;
  }
  
  /**
   * Set sets the destination field for an I/O redirection table entry
   * with an APIC ID and the destination mode to physical
   * @param tableEntry IO redirection table entry
   * @param apicId APCI ID
   */
  public final void setPhysicalDestination(int tableEntry, int apicId)
  {
    long tableValue = getIoRedTable(tableEntry);
    // Set the destination field
    tableValue &= 0x00FFFFFFFFFFFFFFL;
    tableValue |= ((long)apicId << 56);
    // Set destination mode to physical
    tableValue &= ~LOGICAL_MODE;
    setIoRedTable(tableEntry, tableValue);
  }
  
  public final void setLogicalDestination(int tableEntry, int processorSet)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= 0x00FFFFFFFFFFFFFFL;
    tableValue |= ((long)processorSet << 56);
    tableValue |= LOGICAL_MODE;
    setIoRedTable(tableEntry, tableValue);
  }
  /**
   * Enable the table entry's interrupt
   * @param tableEntry IO redirection table entry
   */
  public final void maskInterrupt(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue |= INTERRUPT_MASK;
    setIoRedTable(tableEntry, tableValue);
  }
  
  /**
   * Disable the table entry's interrupt
   * @param tableEntry
   */
  public final void unmaskInterrupt(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= ~INTERRUPT_MASK;
    setIoRedTable(tableEntry, tableValue);
  }
  
  /**
   * Set the interrupt pin trigger to level sensitive
   * @param tableEntry
   */
  public final void levelTriggerMode(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue |= TRIGGER_MODE;
    setIoRedTable(tableEntry, tableValue);
  }
  
  /**
   * Set the interrupt pin trigger to edge sensitive.
   * @param tableEntry
   */
  public final void edgeTriggerMode(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= ~TRIGGER_MODE;
    setIoRedTable(tableEntry, tableValue);
  }
  
  public final void highActivePinPolarity(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= ~INTPOL_FLAG;
    setIoRedTable(tableEntry, tableValue);
  }

  public final void lowActivePinPolarity(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue |= INTPOL_FLAG;
    setIoRedTable(tableEntry, tableValue);
  }
  /**
   * Set the delivery mode for entry to fixed
   * @param tableEntry
   */
  public final void setFixedDelivery(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= ~(0x7<<8);
    tableValue |= DELMOD_FIXED;
    setIoRedTable(tableEntry, tableValue);
  }

  /**
   * Set the delivery mode for entry to lower priority
   * @param tableEntry
   */
  public final void setLowestPriorityDelivery(int tableEntry)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= ~(0x7<<8);
    tableValue |= DELMOD_LOWPRIORITY;
    setIoRedTable(tableEntry, tableValue);
  }

  public final void setInterruptVector(int tableEntry, int vector)
  {
    long tableValue = getIoRedTable(tableEntry);
    tableValue &= ~0xFFL;
    tableValue |= vector;
    setIoRedTable(tableEntry, tableValue);
  }
  
  public String toString()
  {
    String ioApicString = "IOAPIC id " + id + " ver " + version + " max " + maxRedirEntry + "\n";
    
//    for(int i=0; i <= maxRedirEntry; i++)
//    {
//      long entry = getIoRedTable(i);
//      ioApicString += i + " " + Long.toHexString(entry) + "\n";
//    }
    return ioApicString;
  }
}
