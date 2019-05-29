/**
 * Created on Apr 4, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jam.board.pc.I82c54;
import org.jam.board.pc.Platform;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class Apic {
  
  // Hardcode address for now
  final protected static Address registers = Address.fromIntSignExtend(0xFEE00000);
  final protected static Offset ID = Offset.fromIntSignExtend(0x20);
  final protected static Offset VERSION = Offset.fromIntSignExtend(0x30);
  final protected static Offset TPR = Offset.fromIntSignExtend(0x80);
  final protected static Offset APR = Offset.fromIntSignExtend(0x90);
  final protected static Offset PPR = Offset.fromIntSignExtend(0xA0);
  final protected static Offset EOI = Offset.fromIntSignExtend(0xB0);
  final protected static Offset RRD = Offset.fromIntSignExtend(0xC0);
  final protected static Offset LDR = Offset.fromIntSignExtend(0xD0);
  final protected static Offset DFR = Offset.fromIntSignExtend(0xE0);
  final protected static Offset SPVR = Offset.fromIntSignExtend(0xF0);
  final protected static Offset ISR = Offset.fromIntSignExtend(0x100);
  final protected static Offset TMR = Offset.fromIntSignExtend(0x180);
  final protected static Offset IRR = Offset.fromIntSignExtend(0x200);
  final protected static Offset ESR = Offset.fromIntSignExtend(0x280);
  final protected static Offset CMCI = Offset.fromIntSignExtend(0x2F0);
  final protected static Offset ICR0 = Offset.fromIntSignExtend(0x300);
  final protected static Offset ICR1 = Offset.fromIntSignExtend(0x310);
  final protected static Offset TIMER = Offset.fromIntSignExtend(0x320);
  final protected static Offset TSR = Offset.fromIntSignExtend(0x330);
  final protected static Offset PMCR = Offset.fromIntSignExtend(0x340);
  final protected static Offset LINT0 = Offset.fromIntSignExtend(0x350);
  final protected static Offset LINT1 = Offset.fromIntSignExtend(0x360);
  final protected static Offset ERROR = Offset.fromIntSignExtend(0x370);
  final protected static Offset TIMERICR = Offset.fromIntSignExtend(0x380);
  final protected static Offset TIMERCCR = Offset.fromIntSignExtend(0x390);
  final protected static Offset TIMERDCR = Offset.fromIntSignExtend(0x3E0);
  private static final int SUPRESS_EIO_BROADCASTS = 1<<24;
  private static final int APIC_SW_ENABLE = 1<<8;
  
  public Apic()
  {
    if(!CpuId.hasAPIC)
    {
      throw new RuntimeException("No APIC on the processor!");
    } 
    if(!MSR.apicIsEnabled())
    {
      throw new RuntimeException("APIC is not enabled!");
    }
  }
  
  public void boot()
  {
    setSpuriousVector(15);
    setFlatModel();
    setLogicalDestination(1);
    //setTaskPriority(0);
  }
  public int getVersion()
  {
    int value = registers.loadInt(VERSION);
    return value & 0xFF;
  }
  
  public int getMaxLvtEntries()
  {
    int value = registers.loadInt(VERSION);
    return (value >> 16) & 0xFF;
  }
  
  public boolean canSuppressEOIBroadcasts()
  {
    int value = registers.loadInt(VERSION);
    return (value & SUPRESS_EIO_BROADCASTS) != 0;
  }
  
  public int getId()
  {
    int value = registers.loadInt(ID);
    return (value >> 24) & 0xFF;
  }
  
  protected int getTimerVector()
  {
    return registers.loadInt(TIMER);
  }
  
  protected void setTimerVector(int timerVectorValue)
  {
    registers.store(timerVectorValue, TIMER);
  }
  
  /**
   * Return contents of the APIC Timer divide configuration register
   * @return dcr register contents
   */
  protected int getTimerDcr()
  {
    return registers.loadInt(TIMERDCR);
  }
  
  /**
   * Set the APIC Timer divide configuration register
   * @param dcrValue new DCR value
   */
  protected void setTimerDcr(int dcrValue)
  {
    registers.store(dcrValue, TIMERDCR);
  }
  
  protected void setTimerIcr(int icrValue)
  {
    registers.store(icrValue, TIMERICR);
  }
  
  protected int getTimerCcr()
  {
    return registers.loadInt(TIMERCCR);
  }
  
  public void setTaskPriority(int priority)
  {
    registers.store(priority, TPR);
  }
  
  public int getTaskPriority()
  {
    return registers.loadInt(TPR);
  }
  
  public void setSpuriousVector(int vector)
  {
    int registerValue = registers.loadInt(SPVR);
    registerValue &= ~0xFF;
    registerValue |= vector;
    registers.store(registerValue, SPVR);
  }
  
  public void setLogicalDestination(int logicalId)
  {
    logicalId <<= 24;
    registers.store(logicalId, LDR);
  }
  
  public void setFlatModel()
  {
    registers.store(0xFFFFFFFF, DFR);
  }
  /**
   * Enable the local apic
   */
  public void enable()
  {
    int registerValue = registers.loadInt(SPVR);
    registerValue |= APIC_SW_ENABLE;
    registers.store(registerValue, SPVR);
  }
  
  /**
   * Disable the local apic
   */
  public void disable()
  {
    int registerValue = registers.loadInt(SPVR);
    registerValue &= ~APIC_SW_ENABLE;
    registers.store(registerValue, SPVR);
  }
  
  public void eoi()
  {
    registers.store(0, EOI);
  }
  
  public String toString()
  {
    String apicString = "APIC ver: " + getVersion() + " lvts: " + getMaxLvtEntries() + " id: " + getId();
    if(canSuppressEOIBroadcasts())
    {
      apicString += " EIO SUPRESSION";
    }
    return apicString;
  }
}
