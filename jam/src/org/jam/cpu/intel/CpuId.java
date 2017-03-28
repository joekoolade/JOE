/**
 * Created on Feb 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;

/**
 * @author Joe Kulig
 *
 */
public class CpuId {
  public static int maxCpuId;
  public static int extendedMaxCpuId;
  public static char[] vendorId = new char[12];
  public static char[] processorBrand = new char[48];
  static int[] regs = new int[4];
  public static final boolean trace = true;
  
  public static void boot()
  {
    cpuId0();
    cpuId1();
    extendedCpuId0();
    extendedCpuId2();
    extendedCpuId3();
    extendedCpuId4();
  }

    static void cpuId0()
    {
      Magic.cpuId(0, regs);
      maxCpuId = regs[0];
      vendorId[0] = (char)(regs[1] & 0xff);
      vendorId[1] = (char)((regs[1] >> 8) & 0xff);
      vendorId[2] = (char)((regs[1] >> 16) & 0xff);
      vendorId[3] = (char)((regs[1] >> 24) & 0xff);
      vendorId[4] = (char)(regs[3] & 0xff);
      vendorId[5] = (char)((regs[3] >> 8) & 0xff);
      vendorId[6] = (char)((regs[3] >> 16) & 0xff);
      vendorId[7] = (char)((regs[3] >> 24) & 0xff);
      vendorId[8] = (char)(regs[2] & 0xff);
      vendorId[9] = (char)((regs[2] >> 8) & 0xff);
      vendorId[10] = (char)((regs[2] >> 16) & 0xff);
      vendorId[11] = (char)((regs[2] >> 24) & 0xff);
      if(trace)
      {
        VM.sysWriteln("Max CPUID: ", maxCpuId);
        VM.sysWriteln("Vendor ID: ", new String(vendorId));
      }
    }
    static void cpuId1()
    {
      Magic.cpuId(1, regs);
      if(trace)
      {
        VM.sysWrite("CPUID 1: ", regs[0]);
        VM.sysWrite(" ", regs[1]);
        VM.sysWrite(" ", regs[2]);
        VM.sysWriteln(" ", regs[3]);
      }
    }
    static void extendedCpuId0()
    {
      Magic.cpuId(0x80000000, regs);
      if(trace)
      {
        VM.sysWriteln("Extened CPUID 0: ", regs[0]);
      }
    }
    
    static void extendedCpuId2()
    {
      Magic.cpuId(0x80000002, regs);
      processorBrand[0] = (char)(regs[0] & 0xff);
      processorBrand[1] = (char)((regs[0] >> 8) & 0xff);
      processorBrand[2] = (char)((regs[0] >> 16) & 0xff);
      processorBrand[3] = (char)((regs[0] >> 24) & 0xff);
      processorBrand[4] = (char)(regs[1] & 0xff);
      processorBrand[5] = (char)((regs[1] >> 8) & 0xff);
      processorBrand[6] = (char)((regs[1] >> 16) & 0xff);
      processorBrand[7] = (char)((regs[1] >> 24) & 0xff);
      processorBrand[8] = (char)(regs[2] & 0xff);
      processorBrand[9] = (char)((regs[2] >> 8) & 0xff);
      processorBrand[10] = (char)((regs[2] >> 16) & 0xff);
      processorBrand[11] = (char)((regs[2] >> 24) & 0xff);
      processorBrand[12] = (char)(regs[3] & 0xff);
      processorBrand[13] = (char)((regs[3] >> 8) & 0xff);
      processorBrand[14] = (char)((regs[3] >> 16) & 0xff);
      processorBrand[15] = (char)((regs[3] >> 24) & 0xff);
    }
    static void extendedCpuId3()
    {
      Magic.cpuId(0x80000003, regs);
      processorBrand[16] = (char)(regs[0] & 0xff);
      processorBrand[17] = (char)((regs[0] >> 8) & 0xff);
      processorBrand[18] = (char)((regs[0] >> 16) & 0xff);
      processorBrand[19] = (char)((regs[0] >> 24) & 0xff);
      processorBrand[20] = (char)(regs[1] & 0xff);
      processorBrand[21] = (char)((regs[1] >> 8) & 0xff);
      processorBrand[22] = (char)((regs[1] >> 16) & 0xff);
      processorBrand[23] = (char)((regs[1] >> 24) & 0xff);
      processorBrand[24] = (char)(regs[2] & 0xff);
      processorBrand[25] = (char)((regs[2] >> 8) & 0xff);
      processorBrand[26] = (char)((regs[2] >> 16) & 0xff);
      processorBrand[27] = (char)((regs[2] >> 24) & 0xff);
      processorBrand[28] = (char)(regs[3] & 0xff);
      processorBrand[29] = (char)((regs[3] >> 8) & 0xff);
      processorBrand[30] = (char)((regs[3] >> 16) & 0xff);
      processorBrand[31] = (char)((regs[3] >> 24) & 0xff);
    }
    static void extendedCpuId4()
    {
      Magic.cpuId(0x80000004, regs);
      processorBrand[32] = (char)(regs[0] & 0xff);
      processorBrand[33] = (char)((regs[0] >> 8) & 0xff);
      processorBrand[34] = (char)((regs[0] >> 16) & 0xff);
      processorBrand[35] = (char)((regs[0] >> 24) & 0xff);
      processorBrand[36] = (char)(regs[1] & 0xff);
      processorBrand[37] = (char)((regs[1] >> 8) & 0xff);
      processorBrand[38] = (char)((regs[1] >> 16) & 0xff);
      processorBrand[39] = (char)((regs[1] >> 24) & 0xff);
      processorBrand[40] = (char)(regs[2] & 0xff);
      processorBrand[41] = (char)((regs[2] >> 8) & 0xff);
      processorBrand[42] = (char)((regs[2] >> 16) & 0xff);
      processorBrand[43] = (char)((regs[2] >> 24) & 0xff);
      processorBrand[44] = (char)(regs[3] & 0xff);
      processorBrand[45] = (char)((regs[3] >> 8) & 0xff);
      processorBrand[46] = (char)((regs[3] >> 16) & 0xff);
      processorBrand[47] = (char)((regs[3] >> 24) & 0xff);
      if(trace)
      {
        VM.sysWriteln("Processor Brand: ", new String(processorBrand));
      }
    }
}
