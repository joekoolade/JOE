/**
 * Created on Apr 4, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.cpu.intel;

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
  final protected static Offset EIO = Offset.fromIntSignExtend(0xB0);
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
  
  
}
