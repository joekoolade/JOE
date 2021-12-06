/**
 * Created on Apr 4, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jikesrvm.runtime.Magic;

/**
 * @author Joe Kulig
 *
 */
public class MSR {
  final private static int IA32_APIC_BASE = 0x01B;
  final private static long APIC_BSP = (1<<8);
  final private static long APIC_GLOBAL_ENABLE = (1<<11);
  
  final public static long readMsr(int msrRegister)
  {
    if(!CpuId.hasMSR)
    {
      throw new RuntimeException("No MSR on processor!");
    }
    return Magic.readMSR(msrRegister);
  }
  
  final public static void writeMsr(int msrRegister, long value)
  {
    if(!CpuId.hasMSR)
    {
      throw new RuntimeException("No MSR on processor!");
    }
    Magic.writeMSR(msrRegister, value);
  }
  
  final public static boolean apicIsEnabled()
  {
    return (readMsr(IA32_APIC_BASE) & APIC_GLOBAL_ENABLE) != 0;
  }
}
