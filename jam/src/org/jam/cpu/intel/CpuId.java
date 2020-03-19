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
  private static int extendedFamilyId;
  private static int extendedModelId;
  private static int processorType;
  private static int familyId;
  private static int model;
  private static int steppingId;
  private static int brandIndex;
  private static int cflushLineSize;
  private static int maxApicIds;
  private static int apicId;
  public static boolean hasFPU;
  public static boolean hasVME;
  public static boolean hasDE;
  public static boolean hasPSE;
  public static boolean hasTSC;
  public static boolean hasMSR;
  public static boolean hasPAE;
  public static boolean hasMCE;
  public static boolean hasCX8;
  public static boolean hasAPIC;
  public static boolean hasSEP;
  public static boolean hasMTRR;
  public static boolean hasPGE;
  public static boolean hasMCA;
  public static boolean hasCMOV;
  public static boolean hasPAT;
  public static boolean hasPSE36;
  public static boolean hasPSN;
  public static boolean hasCLFSH;
  public static boolean hasDS;
  public static boolean hasACPI;
  public static boolean hasMMX;
  public static boolean hasFXSR;
  public static boolean hasSSE;
  public static boolean hasSSE2;
  public static boolean hasSS;
  public static boolean hasHTT;
  public static boolean hasTM;
  public static boolean hasPBE;
  public static boolean hasSSE3;
  public static boolean hasPCLMUQDQ;
  public static boolean hasDTES64;
  public static boolean hasMONITOR;
  public static boolean hasDSCPL;
  public static boolean hasVMX;
  public static boolean hasSMX;
  public static boolean hasEIST;
  public static boolean hasTM2;
  public static boolean hasSSSE3;
  public static boolean hasCNXTID;
  public static boolean hasSDBG;
  public static boolean hasFMA;
  public static boolean hasCMPXCHG16B;
  public static boolean hasXTPR;
  public static boolean hasPDCM;
  public static boolean hasPCID;
  public static boolean hasDCA;
  public static boolean hasSSE41;
  public static boolean hasSSE42;
  public static boolean hasX2APIC;
  public static boolean hasMOVBE;
  public static boolean hasPOPCNT;
  public static boolean hasTSCDEADLINE;
  public static boolean hasAESNI;
  public static boolean hasOSXSAVE;
  public static boolean hasXSAVE;
  public static boolean hasAVX;
  public static boolean hasF16C;
  public static boolean hasDRAND;
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
        VM.sysWrite("Vendor ID: "); VM.sysWrite(vendorId, 12); VM.sysWriteln();
      }
    }
    static void cpuId1()
    {
      Magic.cpuId(1, regs);
      if(trace)
      {
        VM.sysWrite("CPUID 1: ", VM.intAsHexString(regs[0]));
        VM.sysWrite(" ", VM.intAsHexString(regs[1]));
        VM.sysWrite(" ", VM.intAsHexString(regs[2]));
        VM.sysWriteln(" ", VM.intAsHexString(regs[3]));
      }
      
      // EAX
      extendedFamilyId = (regs[0] >> 20) & 0xFF;
      extendedModelId = (regs[0] >> 16) & 0xF;
      processorType = (regs[0] >> 12) & 0x3;
      familyId = (regs[0] >> 8) & 0xF;
      model = (regs[0] >> 4) & 0xF;
      steppingId = regs[0] & 0xF;
      
      // EBX
      brandIndex = regs[1] & 0xFF;
      cflushLineSize = ((regs[1] >> 8) & 0xFF) * 8;
      maxApicIds = (regs[1] >> 16) & 0xFF;
      apicId = (regs[1] >> 24) & 0xFF;
      
      // ECX
      hasSSE3 = (regs[2] & 0x00000001) != 0;
      hasPCLMUQDQ = (regs[2] & 0x00000002) != 0;
      hasDTES64 = (regs[2] & 0x00000004) != 0;
      hasMONITOR = (regs[2] & 0x00000008) != 0;
      hasDSCPL = (regs[2] & 0x00000010) != 0;
      hasVMX = (regs[2] & 0x00000020) != 0;
      hasSMX = (regs[2] & 0x00000040) != 0;
      hasEIST = (regs[2] & 0x00000080) != 0;
      hasTM2 = (regs[2] & 0x00000100) != 0;
      hasSSSE3 = (regs[2] & 0x00000200) != 0;
      hasCNXTID = (regs[2] & 0x00000400) != 0;
      hasSDBG = (regs[2] & 0x00000800) != 0;
      hasFMA = (regs[2] & 0x00001000) != 0;
      hasCMPXCHG16B = (regs[2] & 0x00002000) != 0;
      hasXTPR = (regs[2] & 0x00004000) != 0;
      hasPDCM = (regs[2] & 0x00008000) != 0;
      hasPCID = (regs[2] & 0x00020000) != 0;
      hasDCA = (regs[2] & 0x00040000) != 0;
      hasSSE41 = (regs[2] & 0x00080000) != 0;
      hasSSE42 = (regs[2] & 0x00100000) != 0;
      hasX2APIC = (regs[2] & 0x00200000) != 0;
      hasMOVBE = (regs[2] & 0x00400000) != 0;
      hasPOPCNT = (regs[2] & 0x00800000) != 0;
      hasTSCDEADLINE = (regs[2] & 0x01000000) != 0;
      hasAESNI = (regs[2] & 0x02000000) != 0;
      hasXSAVE = (regs[2] & 0x04000000) != 0;
      hasOSXSAVE = (regs[2] & 0x08000000) != 0;
      hasAVX = (regs[2] & 0x10000000) != 0;
      hasF16C = (regs[2] & 0x20000000) != 0;
      hasDRAND = (regs[2] & 0x40000000) != 0;
      
      // EDX
      hasFPU = (regs[3] & 0x00000001) != 0;
      hasVME = (regs[3] & 0x00000002) != 0;
      hasDE  = (regs[3] & 0x00000004) != 0;
      hasPSE = (regs[3] & 0x00000008) != 0;
      hasTSC = (regs[3] & 0x00000010) != 0;
      hasMSR = (regs[3] & 0x00000020) != 0;
      hasPAE = (regs[3] & 0x00000040) != 0;
      hasMCE = (regs[3] & 0x00000080) != 0;
      hasCX8 = (regs[3] & 0x00000100) != 0;
      hasAPIC = (regs[3] & 0x00000200) != 0;
      hasSEP = (regs[3] & 0x00000800) != 0;
      hasMTRR = (regs[3] & 0x00001000) != 0;
      hasPGE = (regs[3] & 0x00002000) != 0;
      hasMCA = (regs[3] & 0x00004000) != 0;
      hasCMOV = (regs[3] & 0x00008000) != 0;
      hasPAT = (regs[3] & 0x00010000) != 0;
      hasPSE36 = (regs[3] & 0x00020000) != 0;
      hasPSN = (regs[3] & 0x00040000) != 0;
      hasCLFSH = (regs[3] & 0x00080000) != 0;
      hasDS = (regs[3] & 0x00200000) != 0;
      hasACPI = (regs[3] & 0x00400000) != 0;
      hasMMX = (regs[3] & 0x00800000) != 0;
      hasFXSR = (regs[3] & 0x01000000) != 0;
      hasSSE = (regs[3] & 0x02000000) != 0;
      hasSSE2 = (regs[3] & 0x04000000) != 0;
      hasSS = (regs[3] & 0x08000000) != 0;
      hasHTT = (regs[3] & 0x10000000) != 0;
      hasTM = (regs[3] & 0x20000000) != 0;
      hasPBE = (regs[3] & 0x80000000) != 0;
    }
    
    static void extendedCpuId0()
    {
      Magic.cpuId(0x80000000, regs);
      if(trace)
      {
        VM.sysWriteln("Extened CPUID 0: ", VM.intAsHexString(regs[0]));
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
        VM.sysWrite("Processor Brand: "); VM.sysWrite(processorBrand, 48); VM.sysWriteln();
      }
    }
    
    public static void print()
    {
      VM.sysWriteln("CPUID:");
      if(hasFPU) VM.sysWrite("FPU ");
      if(hasVME) VM.sysWrite("VME ");
      if(hasDE) VM.sysWrite("DE ");
      if(hasPSE) VM.sysWrite("PSE ");
      if(hasTSC) VM.sysWrite("TSC ");
      if(hasMSR) VM.sysWrite("MSR ");
      if(hasPAE) VM.sysWrite("PAE ");
      if(hasMCE) VM.sysWrite("MCE ");
      if(hasCX8) VM.sysWrite("CX8 ");
      if(hasAPIC) VM.sysWrite("APIC ");
      if(hasSEP) VM.sysWrite("SEP ");
      if(hasMTRR) VM.sysWrite("MTRR ");
      if(hasPGE) VM.sysWrite("PGE ");
      if(hasMCA) VM.sysWrite("MCA ");
      if(hasCMOV) VM.sysWrite("CMOV ");
      if(hasPAT) VM.sysWrite("PAT ");
      if(hasPSE36) VM.sysWrite("PSE36 " );
      if(hasPSN) VM.sysWrite("PSN ");
      if(hasCLFSH) VM.sysWrite("CLFSH ");
      if(hasDS) VM.sysWrite("DS ");
      if(hasACPI) VM.sysWrite("ACPI ");
      if(hasMMX) VM.sysWrite("MMX ");
      if(hasFXSR) VM.sysWrite("FXSR ");
      if(hasSSE) VM.sysWrite("SSE ");
      if(hasSSE2) VM.sysWrite("SSE2 ");
      if(hasSS) VM.sysWrite("SS ");
      if(hasHTT) VM.sysWrite("HTT ");
      if(hasTM) VM.sysWrite("TM ");
      if(hasPBE) VM.sysWrite("PBE ");
      if(hasSSE3) VM.sysWrite("SSE3 ");
      if(hasPCLMUQDQ) VM.sysWrite("PCLMUQDQ ");
      if(hasDTES64) VM.sysWrite("DTES64 ");
      if(hasMONITOR) VM.sysWrite("MONITOR ");
      if(hasDSCPL) VM.sysWrite("DSCPL ");
      if(hasVMX) VM.sysWrite("VMX ");
      if(hasSMX) VM.sysWrite("SMX ");
      if(hasEIST) VM.sysWrite("EIST ");
      if(hasTM2) VM.sysWrite("TM2 ");
      if(hasSSSE3) VM.sysWrite("SSSE3 ");
      if(hasCNXTID) VM.sysWrite("CNXTID ");
      if(hasSDBG) VM.sysWrite("SDBG ");
      if(hasFMA) VM.sysWrite("FMA ");
      if(hasCMPXCHG16B) VM.sysWrite("CMPXCHG16B ");
      if(hasXTPR) VM.sysWrite("XTPR ");
      if(hasPDCM) VM.sysWrite("PDCM ");
      if(hasPCID) VM.sysWrite("PCID ");
      if(hasDCA) VM.sysWrite("DCA ");
      if(hasSSE41) VM.sysWrite("SSE41 ");
      if(hasSSE42) VM.sysWrite("SSE42 ");
      if(hasX2APIC) VM.sysWrite("X2APIC ");
      if(hasMOVBE) VM.sysWrite("MOVBE ");
      if(hasPOPCNT) VM.sysWrite("POPCNT ");
      if(hasTSCDEADLINE) VM.sysWrite("TSCDEADLINE ");
      if(hasAESNI) VM.sysWrite("AESNI ");
      if(hasOSXSAVE) VM.sysWrite("OSXSAVE ");
      if(hasXSAVE) VM.sysWrite("XSAVE ");
      if(hasAVX) VM.sysWrite("AVX ");
      if(hasF16C) VM.sysWrite("F16C ");
      if(hasDRAND) VM.sysWrite("DRAND ");
      VM.sysWriteln();
    }
}
