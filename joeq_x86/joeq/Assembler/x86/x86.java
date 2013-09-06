// x86.java, created Mon Feb  5 23:23:19 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Assembler.x86;

import joeq.Allocator.CodeAllocator;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * x86
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: x86.java,v 1.11 2004/09/30 03:37:06 joewhaley Exp $
 */
public class x86 implements x86Constants {

    int opcode;
    int length;   // in bytes
    int pairing;  // for traditional Pentium cores
    int microops; // for Pentium Pro cores
    String desc;  // description

    public static final x86 AAA = _op(0x37, 1, -1, -1, "AAA");
    public static final x86 AAD = _op(0xD50A, 2, -1, -1, "AAD");
    public static final x86 AAM = _op(0xD40A, 2, -1, -1, "AAM");
    public static final x86 AAS = _op(0x3F, 1, -1, -1, "AAS");
    public static final x86 ADC_ra_i8 = _op(0x14, 1, PU, 2, "ADC_ra_i8");
    public static final x86 ADC_ra_i32 = _op(0x15, 1, PU, 2, "ADC_ra_i32");
    public static final x86 ADC_r_i8 = _op(0x8010, 2, PU, 2, "ADC_r_i8");
    public static final x86 ADC_r_i32 = _op(0x8110, 2, PU, 2, "ADC_r_i32");
    public static final x86 ADC_r_r8 = _op(0x1200, 2, PU, 2, "ADC_r_r8");
    public static final x86 ADC_r_r32 = _op(0x1300, 2, PU, 2, "ADC_r_r32");
    public static final x86 ADC_r_m8 = _op(0x1200, 2, PU, 3, "ADC_r_m8");
    public static final x86 ADC_r_m32 = _op(0x1300, 2, PU, 3, "ADC_r_m32");
    public static final x86 ADC_m_i8 = _op(0x8010, 2, PU, 4, "ADC_m_i8");
    public static final x86 ADC_m_i32 = _op(0x8110, 2, PU, 4, "ADC_m_i32");
    public static final x86 ADC_m_r8 = _op(0x1000, 2, PU, 4, "ADC_m_r8");
    public static final x86 ADC_m_r32 = _op(0x1100, 2, PU, 4, "ADC_m_r32");
    public static final x86 ADD_ra_i8 = _op(0x04, 1, UV, 1, "ADD_ra_i8");
    public static final x86 ADD_ra_i32 = _op(0x05, 1, UV, 1, "ADD_ra_i32");
    public static final x86 ADD_r_i8 = _op(0x8000, 2, UV, 1, "ADD_r_i8");
    public static final x86 ADD_r_i32 = _op(0x8100, 2, UV, 1, "ADD_r_i32");
    public static final x86 ADD_r_r8 = _op(0x0200, 2, UV, 1, "ADD_r_r8");
    public static final x86 ADD_r_r32 = _op(0x0300, 2, UV, 1, "ADD_r_r32");
    public static final x86 ADD_r_m8 = _op(0x0200, 2, UV, 2, "ADD_r_m8");
    public static final x86 ADD_r_m32 = _op(0x0300, 2, UV, 2, "ADD_r_m32");
    public static final x86 ADD_m_i8 = _op(0x8000, 2, UV, 4, "ADD_m_i8");
    public static final x86 ADD_m_i32 = _op(0x8100, 2, UV, 4, "ADD_m_i32");
    public static final x86 ADD_m_r8 = _op(0x0100, 2, UV, 4, "ADD_m_r8");
    public static final x86 ADD_m_r32 = _op(0x0100, 2, UV, 4, "ADD_m_r32");
    public static final x86 AND_ra_i8 = _op(0x24, 1, UV, 1, "AND_ra_i8");
    public static final x86 AND_ra_i32 = _op(0x25, 1, UV, 1, "AND_ra_i32");
    public static final x86 AND_r_i8 = _op(0x8020, 2, UV, 1, "AND_r_i8");
    public static final x86 AND_r_i32 = _op(0x8120, 2, UV, 1, "AND_r_i32");
    public static final x86 AND_r_r8 = _op(0x2200, 2, UV, 1, "AND_r_r8");
    public static final x86 AND_r_r32 = _op(0x2300, 2, UV, 1, "AND_r_r32");
    public static final x86 AND_r_m8 = _op(0x2200, 2, UV, 2, "AND_r_m8");
    public static final x86 AND_r_m32 = _op(0x2300, 2, UV, 2, "AND_r_m32");
    public static final x86 AND_m_i8 = _op(0x8020, 2, UV, 4, "AND_m_i8");
    public static final x86 AND_m_i32 = _op(0x8120, 2, UV, 4, "AND_m_i32");
    public static final x86 AND_m_r8 = _op(0x2000, 2, UV, 4, "AND_m_r8");
    public static final x86 AND_m_r32 = _op(0x2100, 2, UV, 4, "AND_m_r32");
    public static final x86 ARPL = _op(0x6300, 2, -1, -1, "ARPL");
    public static final x86 BOUND = _op(0x6200, 2, -1, -1, "BOUND");
    public static final x86 BSF = _op(0x0FBC, 2, -1, -1, "BSF");
    public static final x86 BSR = _op(0x0FBD, 2, -1, -1, "BSR");
    public static final x86 BSWAP = _op(0x0FC8, 2, -1, -1, "BSWAP");
    public static final x86 BT_r_i = _op(0x0FBA20, 3, -1, -1, "BT_r_i");
    public static final x86 BT_r_r = _op(0x0FA3, 2, -1, -1, "BT_r_r");
    public static final x86 BT_m_i = _op(0x0FBA20, 3, -1, -1, "BT_m_i");
    public static final x86 BT_m_r = _op(0x0FA3, 2, -1, -1, "BT_m_r");
    public static final x86 BTC_r_i = _op(0x0FBA38, 3, -1, -1, "BTC_r_i");
    public static final x86 BTC_r_r = _op(0x0FBB, 2, -1, -1, "BTC_r_r");
    public static final x86 BTC_m_i = _op(0x0FBA38, 3, -1, -1, "BTC_m_i");
    public static final x86 BTC_m_r = _op(0x0FBB, 2, -1, -1, "BTC_m_r");
    public static final x86 BTR_r_i = _op(0x0FBA30, 3, -1, -1, "BTR_r_i");
    public static final x86 BTR_r_r = _op(0x0FB3, 2, -1, -1, "BTR_r_r");
    public static final x86 BTR_m_i = _op(0x0FBA30, 3, -1, -1, "BTR_m_i");
    public static final x86 BTR_m_r = _op(0x0FB3, 2, -1, -1, "BTR_m_r");
    public static final x86 BTS_r_i = _op(0x0FBA28, 3, -1, -1, "BTS_r_i");
    public static final x86 BTS_r_r = _op(0x0FAB, 2, -1, -1, "BTS_r_r");
    public static final x86 BTS_m_i = _op(0x0FBA28, 3, -1, -1, "BTS_m_i");
    public static final x86 BTS_m_r = _op(0x0FAB, 2, -1, -1, "BTS_m_r");
    public static final x86 CALL_rel32 = _op(0xE8, 1, -1, -1, "CALL_rel32");
    public static final x86 CALL_r = _op(0xFF10, 2, -1, -1, "CALL_r");
    public static final x86 CALL_abs = _op(0x9A, 1, -1, -1, "CALL_abs");
    public static final x86 CALL_m = _op(0xFF10, 2, -1, -1, "CALL_m");
    public static final x86 CBW = _op(0x98, 1, -1, -1, "CBW");
    public static final x86 CLC = _op(0xF8, 1, -1, -1, "CLC");
    public static final x86 CLI = _op(0xFA, 1, -1, -1, "CLI");
    public static final x86 CLTS = _op(0x0F06, 2, -1, -1, "CLTS");
    public static final x86 CMC = _op(0xF5, 1, -1, -1, "CMC");
    public static final x86 CMOVAr_r = _op(0x0F4700, 3, -1, -1, "CMOVAr_r");
    public static final x86 CMOVAr_m = _op(0x0F4700, 3, -1, -1, "CMOVAr_m");
    public static final x86 CMOVAEr_r = _op(0x0F4300, 3, -1, -1, "CMOVAEr_r");
    public static final x86 CMOVAEr_m = _op(0x0F4300, 3, -1, -1, "CMOVAEr_m");
    public static final x86 CMOVBr_r = _op(0x0F4200, 3, -1, -1, "CMOVBr_r");
    public static final x86 CMOVBr_m = _op(0x0F4200, 3, -1, -1, "CMOVBr_m");
    public static final x86 CMOVBEr_r = _op(0x0F4600, 3, -1, -1, "CMOVBEr_r");
    public static final x86 CMOVBEr_m = _op(0x0F4600, 3, -1, -1, "CMOVBEr_m");
    public static final x86 CMOVEr_r = _op(0x0F4400, 3, -1, -1, "CMOVEr_r");
    public static final x86 CMOVEr_m = _op(0x0F4400, 3, -1, -1, "CMOVEr_m");
    public static final x86 CMOVGr_r = _op(0x0F4F00, 3, -1, -1, "CMOVGr_r");
    public static final x86 CMOVGr_m = _op(0x0F4F00, 3, -1, -1, "CMOVGr_m");
    public static final x86 CMOVGEr_r = _op(0x0F4D00, 3, -1, -1, "CMOVGEr_r");
    public static final x86 CMOVGEr_m = _op(0x0F4D00, 3, -1, -1, "CMOVGEr_m");
    public static final x86 CMOVLr_r = _op(0x0F4C00, 3, -1, -1, "CMOVLr_r");
    public static final x86 CMOVLr_m = _op(0x0F4C00, 3, -1, -1, "CMOVLr_m");
    public static final x86 CMOVLEr_r = _op(0x0F4E00, 3, -1, -1, "CMOVLEr_r");
    public static final x86 CMOVLEr_m = _op(0x0F4E00, 3, -1, -1, "CMOVLEr_m");
    public static final x86 CMOVNEr_r = _op(0x0F4500, 3, -1, -1, "CMOVNEr_r");
    public static final x86 CMOVNEr_m = _op(0x0F4500, 3, -1, -1, "CMOVNEr_m");
    public static final x86 CMOVNOr_r = _op(0x0F4100, 3, -1, -1, "CMOVNOr_r");
    public static final x86 CMOVNOr_m = _op(0x0F4100, 3, -1, -1, "CMOVNOr_m");
    public static final x86 CMOVNPr_r = _op(0x0F4B00, 3, -1, -1, "CMOVNPr_r");
    public static final x86 CMOVNPr_m = _op(0x0F4B00, 3, -1, -1, "CMOVNPr_m");
    public static final x86 CMOVNSr_r = _op(0x0F4900, 3, -1, -1, "CMOVNSr_r");
    public static final x86 CMOVNSr_m = _op(0x0F4900, 3, -1, -1, "CMOVNSr_m");
    public static final x86 CMOVOr_r = _op(0x0F4000, 3, -1, -1, "CMOVOr_r");
    public static final x86 CMOVOr_m = _op(0x0F4000, 3, -1, -1, "CMOVOr_m");
    public static final x86 CMOVPr_r = _op(0x0F4A00, 3, -1, -1, "CMOVPr_r");
    public static final x86 CMOVPr_m = _op(0x0F4A00, 3, -1, -1, "CMOVPr_m");
    public static final x86 CMOVSr_r = _op(0x0F4800, 3, -1, -1, "CMOVSr_r");
    public static final x86 CMOVSr_m = _op(0x0F4800, 3, -1, -1, "CMOVSr_m");
    public static final x86 CMP_ra_i8 = _op(0x3C, 1, UV, 1, "CMP_ra_i8");
    public static final x86 CMP_ra_i32 = _op(0x3D, 1, UV, 1, "CMP_ra_i32");
    public static final x86 CMP_r_i8 = _op(0x8038, 2, UV, 1, "CMP_r_i8");
    public static final x86 CMP_r_i32 = _op(0x8138, 2, UV, 1, "CMP_r_i32");
    public static final x86 CMP_r_r8 = _op(0x3A00, 2, UV, 1, "CMP_r_r8");
    public static final x86 CMP_r_r32 = _op(0x3B00, 2, UV, 1, "CMP_r_r32");
    public static final x86 CMP_r_m8 = _op(0x3A00, 2, UV, 2, "CMP_r_m8");
    public static final x86 CMP_r_m32 = _op(0x3B00, 2, UV, 2, "CMP_r_m32");
    public static final x86 CMP_m_i8 = _op(0x8038, 2, UV, 2, "CMP_m_i8");
    public static final x86 CMP_m_i32 = _op(0x8138, 2, UV, 2, "CMP_m_i32");
    public static final x86 CMP_m_r8 = _op(0x3800, 2, UV, 2, "CMP_m_r8");
    public static final x86 CMP_m_r32 = _op(0x3900, 2, UV, 2, "CMP_m_r32");
    public static final x86 CMPSB = _op(0xA6, 1, -1, -1, "CMPSB");
    public static final x86 CMPSD = _op(0xA7, 1, -1, -1, "CMPSD");
    public static final x86 CMPXCHG_8 = _op(0x0FB000, 3, -1, -1, "CMPXCHG_8");
    public static final x86 CMPXCHG_32 = _op(0x0FB100, 3, -1, -1, "CMPXCHG_32");
    public static final x86 CMPXCHG8B = _op(0x0FC708, 3, -1, -1, "CMPXCHG8B");
    public static final x86 CPUID = _op(0x0FA2, 2, -1, -1, "CPUID");
    public static final x86 CWD = _op(0x99, 1, -1, -1, "CWD");
    public static final x86 DAA = _op(0x27, 1, -1, -1, "DAA");
    public static final x86 DAS = _op(0x2F, 1, -1, -1, "DAS");
    public static final x86 DEC_r8 = _op(0xFE08, 2, -1, -1, "DEC_r8");
    public static final x86 DEC_r32 = _op(0x48, 1, -1, -1, "DEC_r32");
    public static final x86 DEC_m8 = _op(0xFE08, 2, -1, -1, "DEC_m8");
    public static final x86 DEC_m32 = _op(0xFF08, 2, -1, -1, "DEC_m32");
    public static final x86 DIV_r8 = _op(0xF630, 2, -1, -1, "DIV_r8");
    public static final x86 DIV_r32 = _op(0xF730, 2, -1, -1, "DIV_r32");
    public static final x86 DIV_m8 = _op(0xF630, 2, -1, -1, "DIV_m8");
    public static final x86 DIV_m32 = _op(0xF730, 2, -1, -1, "DIV_m32");
    public static final x86 ENTER = _op(0xC8, 1, -1, -1, "ENTER");
    public static final x86 EMMS = _op(0x0F77, 2, -1, -1, "EMMS");
    public static final x86 F2XM1 = _op(0xD9F0, 2, -1, -1, "F2XM1");
    public static final x86 FABS = _op(0xD9E1, 2, -1, -1, "FABS");
    public static final x86 FADD_m32 = _op(0xD800, 2, -1, -1, "FADD_m32");
    public static final x86 FADD_m64 = _op(0xDC00, 2, -1, -1, "FADD_m64");
    public static final x86 FADD_s0_si = _op(0xD8C0, 2, -1, -1, "FADD_s0_si");
    public static final x86 FADD_si_s0 = _op(0xDCC0, 2, -1, -1, "FADD_si_s0");
    public static final x86 FADDP_si_s0 = _op(0xDEC0, 2, -1, -1, "FADDP_si_s0");
    public static final x86 FIADD_m16 = _op(0xDE00, 2, -1, -1, "FIADD_m16");
    public static final x86 FIADD_m32 = _op(0xDA00, 2, -1, -1, "FIADD_m32");
    public static final x86 FBLD = _op(0xDF20, 2, -1, -1, "FBLD");
    public static final x86 FBSTP = _op(0xDF30, 2, -1, -1, "FBSTP");
    public static final x86 FCHS = _op(0xD9E0, 2, -1, -1, "FCHS");
    public static final x86 FCLEX = _op(0x9BDBE2, 3, -1, -1, "FCLEX");
    public static final x86 FNCLEX = _op(0xDBE2, 2, -1, -1, "FNCLEX");
    public static final x86 FCMOVB = _op(0xDAC0, 2, -1, -1, "FCMOVB");
    public static final x86 FCMOVE = _op(0xDAC8, 2, -1, -1, "FCMOVE");
    public static final x86 FCMOVBE = _op(0xDAD0, 2, -1, -1, "FCMOVBE");
    public static final x86 FCMOVU = _op(0xDAD8, 2, -1, -1, "FCMOVU");
    public static final x86 FCMOVNB = _op(0xDBC0, 2, -1, -1, "FCMOVNB");
    public static final x86 FCMOVNE = _op(0xDBC8, 2, -1, -1, "FCMOVNE");
    public static final x86 FCMOVNBE = _op(0xDBD0, 2, -1, -1, "FCMOVNBE");
    public static final x86 FCMOVNU = _op(0xDBD8, 2, -1, -1, "FCMOVNU");
    public static final x86 FCOM_m32 = _op(0xD810, 2, -1, -1, "FCOM_m32");
    public static final x86 FCOM_m64 = _op(0xDC10, 2, -1, -1, "FCOM_m64");
    public static final x86 FCOM_si = _op(0xD8D0, 2, -1, -1, "FCOM_si");
    public static final x86 FCOMP_m32 = _op(0xD818, 2, -1, -1, "FCOMP_m32");
    public static final x86 FCOMP_m64 = _op(0xDC18, 2, -1, -1, "FCOMP_m64");
    public static final x86 FCOMP_si = _op(0xD8D8, 2, -1, -1, "FCOMP_si");
    public static final x86 FCOMPP = _op(0xDED9, 2, -1, -1, "FCOMPP");
    public static final x86 FCOMI = _op(0xDBF0, 2, -1, -1, "FCOMI");
    public static final x86 FCOMIP = _op(0xDFF0, 2, -1, -1, "FCOMIP");
    public static final x86 FUCOMI = _op(0xDBE8, 2, -1, -1, "FUCOMI");
    public static final x86 FUCOMIP = _op(0xDFE8, 2, -1, -1, "FUCOMIP");
    public static final x86 FCOS = _op(0xD9FF, 2, -1, -1, "FCOS");
    public static final x86 FDECSTP = _op(0xD9F6, 2, -1, -1, "FDECSTP");
    public static final x86 FDIV_m32 = _op(0xD830, 2, -1, -1, "FDIV_m32");
    public static final x86 FDIV_m64 = _op(0xDC30, 2, -1, -1, "FDIV_m64");
    public static final x86 FDIV_s0_si = _op(0xD8F0, 2, -1, -1, "FDIV_s0_si");
    public static final x86 FDIV_si_s0 = _op(0xDCF8, 2, -1, -1, "FDIV_si_s0");
    public static final x86 FDIVP_si_s0 = _op(0xDEF8, 2, -1, -1, "FDIVP_si_s0");
    public static final x86 FIDIV_m16 = _op(0xDE30, 2, -1, -1, "FIDIV_m16");
    public static final x86 FIDIV_m32 = _op(0xDA30, 2, -1, -1, "FIDIV_m32");
    public static final x86 FDIVR_m32 = _op(0xD838, 2, -1, -1, "FDIVR_m32");
    public static final x86 FDIVR_m64 = _op(0xDC38, 2, -1, -1, "FDIVR_m64");
    public static final x86 FDIVR_s0_si = _op(0xD8F8, 2, -1, -1, "FDIVR_s0_si");
    public static final x86 FDIVR_si_s0 = _op(0xDCF0, 2, -1, -1, "FDIVR_si_s0");
    public static final x86 FDIVRP_si_s0 = _op(0xDEF0, 2, -1, -1, "FDIVRP_si_s0");
    public static final x86 FIDIVR_m16 = _op(0xDE38, 2, -1, -1, "FIDIVR_m16");
    public static final x86 FIDIVR_m32 = _op(0xDA38, 2, -1, -1, "FIDIVR_m32");
    public static final x86 FFREE = _op(0xDDC0, 2, -1, -1, "FFREE");
    public static final x86 FICOM_m16 = _op(0xDE10, 2, -1, -1, "FICOM_m16");
    public static final x86 FICOM_m32 = _op(0xDA10, 2, -1, -1, "FICOM_m32");
    public static final x86 FICOMP_m16 = _op(0xDE18, 2, -1, -1, "FICOMP_m16");
    public static final x86 FICOMP_m32 = _op(0xDA18, 2, -1, -1, "FICOMP_m32");
    public static final x86 FILD_m16 = _op(0xDF00, 2, -1, -1, "FILD_m16");
    public static final x86 FILD_m32 = _op(0xDB00, 2, -1, -1, "FILD_m32");
    public static final x86 FILD_m64 = _op(0xDF28, 2, -1, -1, "FILD_m64");
    public static final x86 FINCSTP = _op(0xD9F7, 2, -1, -1, "FINCSTP");
    public static final x86 FINIT = _op(0x9BDBE3, 3, -1, -1, "FINIT");
    public static final x86 FNINIT = _op(0xDBE3, 2, -1, -1, "FNINIT");
    public static final x86 FIST_m16 = _op(0xDF10, 2, -1, -1, "FIST_m16");
    public static final x86 FIST_m32 = _op(0xDB10, 2, -1, -1, "FIST_m32");
    public static final x86 FISTP_m16 = _op(0xDF18, 2, -1, -1, "FISTP_m16");
    public static final x86 FISTP_m32 = _op(0xDB18, 2, -1, -1, "FISTP_m32");
    public static final x86 FISTP_m64 = _op(0xDF38, 2, -1, -1, "FISTP_m64");
    public static final x86 FLD_m32 = _op(0xD900, 2, -1, -1, "FLD_m32");
    public static final x86 FLD_m64 = _op(0xDD00, 2, -1, -1, "FLD_m64");
    public static final x86 FLD_m80 = _op(0xDB28, 2, -1, -1, "FLD_m80");
    public static final x86 FLD1 = _op(0xD9E8, 2, -1, -1, "FLD1");
    public static final x86 FLDL2T = _op(0xD9E9, 2, -1, -1, "FLDL2T");
    public static final x86 FLDL2E = _op(0xD9EA, 2, -1, -1, "FLDL2E");
    public static final x86 FLDPI = _op(0xD9EB, 2, -1, -1, "FLDPI");
    public static final x86 FLDLG2 = _op(0xD9EC, 2, -1, -1, "FLDLG2");
    public static final x86 FLDLN2 = _op(0xD9ED, 2, -1, -1, "FLDLN2");
    public static final x86 FLDZ = _op(0xD9EE, 2, -1, -1, "FLDZ");
    public static final x86 FLDCW = _op(0xD928, 2, -1, -1, "FLDCW");
    public static final x86 FLDENV = _op(0xD920, 2, -1, -1, "FLDENV");
    public static final x86 FMUL_m32 = _op(0xD808, 2, -1, -1, "FMUL_m32");
    public static final x86 FMUL_m64 = _op(0xDC08, 2, -1, -1, "FMUL_m64");
    public static final x86 FMUL_s0_si = _op(0xD8C8, 2, -1, -1, "FMUL_s0_si");
    public static final x86 FMUL_si_s0 = _op(0xDCC8, 2, -1, -1, "FMUL_si_s0");
    public static final x86 FMULP_si_s0 = _op(0xDEC8, 2, -1, -1, "FMULP_si_s0");
    public static final x86 FIMUL_m16 = _op(0xDE08, 2, -1, -1, "FIMUL_m16");
    public static final x86 FIMUL_m32 = _op(0xDA08, 2, -1, -1, "FIMUL_m32");
    public static final x86 FNOP = _op(0xD9D0, 2, -1, -1, "FNOP");
    public static final x86 FPATAN = _op(0xD9F3, 2, -1, -1, "FPATAN");
    public static final x86 FPREM = _op(0xD9F8, 2, -1, -1, "FPREM");
    public static final x86 FPREM1 = _op(0xD9F5, 2, -1, -1, "FPREM1");
    public static final x86 FPTAN = _op(0xD9F2, 2, -1, -1, "FPTAN");
    public static final x86 FRNDINT = _op(0xD9FC, 2, -1, -1, "FRNDINT");
    public static final x86 FRSTOR = _op(0xDD20, 2, -1, -1, "FRSTOR");
    public static final x86 FSAVE = _op(0x9BDD30, 3, -1, -1, "FSAVE");
    public static final x86 FNSAVE = _op(0xDD30, 2, -1, -1, "FNSAVE");
    public static final x86 FSCALE = _op(0xD9FD, 2, -1, -1, "FSCALE");
    public static final x86 FSIN = _op(0xD9FE, 2, -1, -1, "FSIN");
    public static final x86 FSINCOS = _op(0xD9FB, 2, -1, -1, "FSINCOS");
    public static final x86 FSQRT = _op(0xD9FA, 2, -1, -1, "FSQRT");
    public static final x86 FST_m32 = _op(0xD910, 2, -1, -1, "FST_m32");
    public static final x86 FST_m64 = _op(0xDD10, 2, -1, -1, "FST_m64");
    public static final x86 FST_si = _op(0xDDD0, 2, -1, -1, "FST_si");
    public static final x86 FSTP_m32 = _op(0xD918, 2, -1, -1, "FSTP_m32");
    public static final x86 FSTP_m64 = _op(0xDD18, 2, -1, -1, "FSTP_m64");
    public static final x86 FSTP_m80 = _op(0xDB38, 2, -1, -1, "FSTP_m80");
    public static final x86 FSTP_si = _op(0xDDD8, 2, -1, -1, "FSTP_si");
    public static final x86 FSTCW = _op(0x9BD938, 3, -1, -1, "FSTCW");
    public static final x86 FNSTCW = _op(0xD938, 2, -1, -1, "FNSTCW");
    public static final x86 FSTENV = _op(0x9BD930, 3, -1, -1, "FSTENV");
    public static final x86 FNSTENV = _op(0xD930, 2, -1, -1, "FNSTENV");
    public static final x86 FSTSW_m = _op(0x9BDD38, 3, -1, -1, "FSTSW_m");
    public static final x86 FSTSW_ax = _op(0x9BDFE0, 3, -1, -1, "FSTSW_ax");
    public static final x86 FNSTSW_m = _op(0xDD38, 2, -1, -1, "FNSTSW_m");
    public static final x86 FNSTSW_ax = _op(0xDFE0, 2, -1, -1, "FNSTSW_ax");
    public static final x86 FSUB_m32 = _op(0xD820, 2, -1, -1, "FSUB_m32");
    public static final x86 FSUB_m64 = _op(0xDC20, 2, -1, -1, "FSUB_m64");
    public static final x86 FSUB_s0_si = _op(0xD8E0, 2, -1, -1, "FSUB_s0_si");
    public static final x86 FSUB_si_s0 = _op(0xDCE8, 2, -1, -1, "FSUB_si_s0");
    public static final x86 FSUBP_si_s0 = _op(0xDEE8, 2, -1, -1, "FSUBP_si_s0");
    public static final x86 FISUB_m16 = _op(0xDE20, 2, -1, -1, "FISUB_m16");
    public static final x86 FISUB_m32 = _op(0xDA20, 2, -1, -1, "FISUB_m32");
    public static final x86 FSUBR_m32 = _op(0xD828, 2, -1, -1, "FSUBR_m32");
    public static final x86 FSUBR_m64 = _op(0xDC28, 2, -1, -1, "FSUBR_m64");
    public static final x86 FSUBR_s0_si = _op(0xD8E8, 2, -1, -1, "FSUBR_s0_si");
    public static final x86 FSUBR_si_s0 = _op(0xDCE0, 2, -1, -1, "FSUBR_si_s0");
    public static final x86 FSUBRP_si_s0 = _op(0xDEE0, 2, -1, -1, "FSUBRP_si_s0");
    public static final x86 FISUBR_m16 = _op(0xDE28, 2, -1, -1, "FISUBR_m16");
    public static final x86 FISUBR_m32 = _op(0xDA28, 2, -1, -1, "FISUBR_m32");
    public static final x86 FTST = _op(0xD9E4, 2, -1, -1, "FTST");
    public static final x86 FUCOM_si = _op(0xDDE0, 2, -1, -1, "FUCOM_si");
    public static final x86 FUCOMP_si = _op(0xDDE8, 2, -1, -1, "FUCOMP_si");
    public static final x86 FUCOMPP = _op(0xDAE9, 2, -1, -1, "FUCOMPP");
    public static final x86 FXAM = _op(0xD9E5, 2, -1, -1, "FXAM");
    public static final x86 FXCH_si = _op(0xD9C8, 2, -1, -1, "FXCH_si");
    public static final x86 FXTRACT = _op(0xD9F4, 2, -1, -1, "FXTRACT");
    public static final x86 FYL2X = _op(0xD9F1, 2, -1, -1, "FYL2X");
    public static final x86 FYL2XP1 = _op(0xD9F9, 2, -1, -1, "FYL2XP1");
    public static final x86 HLT = _op(0xF4, 1, -1, -1, "HLT");
    public static final x86 IDIV_r8 = _op(0xF638, 2, -1, -1, "IDIV_r8");
    public static final x86 IDIV_r32 = _op(0xF738, 2, -1, -1, "IDIV_r32");
    public static final x86 IDIV_m8 = _op(0xF638, 2, -1, -1, "IDIV_m8");
    public static final x86 IDIV_m32 = _op(0xF738, 2, -1, -1, "IDIV_m32");
    public static final x86 IMUL_rda_r8 = _op(0xF628, 2, -1, -1, "IMUL_rda_r8");
    public static final x86 IMUL_rda_r32 = _op(0xF728, 2, -1, -1, "IMUL_rda_r32");
    public static final x86 IMUL_rda_m8 = _op(0xF628, 2, -1, -1, "IMUL_rda_m8");
    public static final x86 IMUL_rda_m32 = _op(0xF728, 2, -1, -1, "IMUL_rda_m32");
    public static final x86 IMUL_r_i8 = _op(0x6B00, 2, -1, -1, "IMUL_r_i8");
    public static final x86 IMUL_r_i32 = _op(0x6900, 2, -1, -1, "IMUL_r_i32");
    public static final x86 IMUL_r_r32 = _op(0x0FAF00, 3, -1, -1, "IMUL_r_r32");
    public static final x86 IMUL_r_r32_i8 = _op(0x6B00, 2, -1, -1, "IMUL_r_r32_i8");
    public static final x86 IMUL_r_r32_i32 = _op(0x6900, 2, -1, -1, "IMUL_r_r32_i32");
    public static final x86 IMUL_r_m32 = _op(0x0FAF00, 3, -1, -1, "IMUL_r_m32");
    public static final x86 IMUL_r_m32_i8 = _op(0x6B00, 2, -1, -1, "IMUL_r_m32_i8");
    public static final x86 IMUL_r_m32_i32 = _op(0x6900, 2, -1, -1, "IMUL_r_m32_i32");
    public static final x86 IN_imm8 = _op(0xE4, 1, -1, -1, "IN_imm8");
    public static final x86 IN_ra8 = _op(0xEC, 1, -1, -1, "IN_ra8");
    public static final x86 IN_ra32 = _op(0xED, 1, -1, -1, "IN_ra32");
    public static final x86 INC_r8 = _op(0xFE00, 2, -1, -1, "INC_r8");
    public static final x86 INC_m8 = _op(0xFE00, 2, -1, -1, "INC_m8");
    public static final x86 INC_m32 = _op(0xFF00, 2, -1, -1, "INC_m32");
    public static final x86 INC_r32 = _op(0x40, 1, -1, -1, "INC_r32");
    public static final x86 INS_m8_rd = _op(0x6C, 1, -1, -1, "INS_m8_rd");
    public static final x86 INS_m32_rd = _op(0x6D, 1, -1, -1, "INS_m32_rd");
    public static final x86 INT_3 = _op(0xCC, 1, -1, -1, "INT_3");
    public static final x86 INT_i8 = _op(0xCD, 1, -1, -1, "INT_i8");
    public static final x86 INTO = _op(0xCE, 1, -1, -1, "INTO");
    public static final x86 INVD = _op(0x0F08, 2, -1, -1, "INVD");
    public static final x86 INVLPG = _op(0x0F0138, 3, -1, -1, "INVLPG");
    public static final x86 IRET = _op(0xCF, 1, -1, -1, "IRET");
    public static final x86 JO = _op(0x00, 1, -1, -1, "JO");
    public static final x86 JNO = _op(0x01, 1, -1, -1, "JNO");
    public static final x86 JB = _op(0x02, 1, -1, -1, "JB");
    public static final x86 JAE = _op(0x03, 1, -1, -1, "JAE");
    public static final x86 JE = _op(0x04, 1, -1, -1, "JE");
    public static final x86 JNE = _op(0x05, 1, -1, -1, "JNE");
    public static final x86 JBE = _op(0x06, 1, -1, -1, "JBE");
    public static final x86 JA = _op(0x07, 1, -1, -1, "JA");
    public static final x86 JS = _op(0x08, 1, -1, -1, "JS");
    public static final x86 JNS = _op(0x09, 1, -1, -1, "JNS");
    public static final x86 JP = _op(0x0A, 1, -1, -1, "JP");
    public static final x86 JNP = _op(0x0B, 1, -1, -1, "JNP");
    public static final x86 JL = _op(0x0C, 1, -1, -1, "JL");
    public static final x86 JGE = _op(0x0D, 1, -1, -1, "JGE");
    public static final x86 JLE = _op(0x0E, 1, -1, -1, "JLE");
    public static final x86 JG = _op(0x0F, 1, -1, -1, "JG");
    public static final x86 JCXZ_8 = _op(0xE3, 1, -1, -1, "JCXZ_8");
    public static final x86 JMP = _op(0xE0, 1, -1, -1, "JMP");
    public static final x86 JMP_r = _op(0xFF20, 2, -1, -1, "JMP_r");
    public static final x86 JMP_abs = _op(0xEA, 1, -1, -1, "JMP_abs");
    public static final x86 JMP_m = _op(0xFF20, 2, -1, -1, "JMP_m");
    public static final x86 LAHF = _op(0x9F, 1, -1, -1, "LAHF");
    public static final x86 LAR_r_r = _op(0x0F0200, 3, -1, -1, "LAR_r_r");
    public static final x86 LAR_r_m = _op(0x0F0200, 3, -1, -1, "LAR_r_m");
    public static final x86 LDS = _op(0xC500, 2, -1, -1, "LDS");
    public static final x86 LSS = _op(0x0FB200, 3, -1, -1, "LSS");
    public static final x86 LES = _op(0xC400, 2, -1, -1, "LES");
    public static final x86 LFS = _op(0x0FB400, 3, -1, -1, "LFS");
    public static final x86 LGS = _op(0x0FB500, 3, -1, -1, "LGS");
    public static final x86 LEA = _op(0x8D00, 2, -1, -1, "LEA");
    public static final x86 LEAVE = _op(0xC9, 1, -1, -1, "LEAVE");
    public static final x86 LGDT = _op(0x0F0110, 3, -1, -1, "LGDT");
    public static final x86 LIDT = _op(0x0F0118, 3, -1, -1, "LIDT");
    public static final x86 LLDT = _op(0x0F0010, 3, -1, -1, "LLDT");
    public static final x86 LMSW = _op(0x0F0130, 3, -1, -1, "LMSW");
    public static final x86 LOCK = _op(0xF0, 1, -1, -1, "LOCK");
    public static final x86 LODS_m8 = _op(0xAC, 1, -1, -1, "LODS_m8");
    public static final x86 LODS_m32 = _op(0xAD, 1, -1, -1, "LODS_m32");
    public static final x86 LOOP = _op(0xE2, 1, -1, -1, "LOOP");
    public static final x86 LOOPE = _op(0xE1, 1, -1, -1, "LOOPE");
    public static final x86 LOOPNE = _op(0xE0, 1, -1, -1, "LOOPNE");
    public static final x86 LSL_r_r = _op(0x0F0300, 3, -1, -1, "LSL_r_r");
    public static final x86 LSL_r_m = _op(0x0F0300, 3, -1, -1, "LSL_r_m");
    public static final x86 LTR_r = _op(0x0F0018, 3, -1, -1, "LTR_r");
    public static final x86 LTR_m = _op(0x0F0018, 3, -1, -1, "LTR_m");
    public static final x86 MOV_r_m8 = _op(0x8A00, 2, -1, -1, "MOV_r_m8");
    public static final x86 MOV_r_m32 = _op(0x8B00, 2, -1, -1, "MOV_r_m32");
    public static final x86 MOV_r_r8 = _op(0x8A00, 2, -1, -1, "MOV_r_r8");
    public static final x86 MOV_r_r32 = _op(0x8B00, 2, -1, -1, "MOV_r_r32");
    public static final x86 MOV_m_r8 = _op(0x8800, 2, -1, -1, "MOV_m_r8");
    public static final x86 MOV_m_r32 = _op(0x8900, 2, -1, -1, "MOV_m_r32");
    public static final x86 MOV_r_sr = _op(0x8C00, 2, -1, -1, "MOV_r_sr");
    public static final x86 MOV_m_sr = _op(0x8C00, 2, -1, -1, "MOV_m_sr");
    public static final x86 MOV_sr_r = _op(0x8E00, 2, -1, -1, "MOV_sr_r");
    public static final x86 MOV_sr_m = _op(0x8E00, 2, -1, -1, "MOV_sr_m");
    public static final x86 MOV_ra_mo8 = _op(0xA0, 1, -1, -1, "MOV_ra_mo8");
    public static final x86 MOV_ra_mo32 = _op(0xA1, 1, -1, -1, "MOV_ra_mo32");
    public static final x86 MOV_mo8_ra = _op(0xA2, 1, -1, -1, "MOV_mo8_ra");
    public static final x86 MOV_mo32_ra = _op(0xA3, 1, -1, -1, "MOV_mo32_ra");
    public static final x86 MOV_r_i8 = _op(0xB0, 1, -1, -1, "MOV_r_i8");
    public static final x86 MOV_r_i32 = _op(0xB8, 1, -1, -1, "MOV_r_i32");
    public static final x86 MOV_m_i8 = _op(0xC600, 2, -1, -1, "MOV_m_i8");
    public static final x86 MOV_m_i32 = _op(0xC700, 2, -1, -1, "MOV_m_i32");
    public static final x86 MOV_cr_r = _op(0x0F2200, 3, -1, -1, "MOV_cr_r");
    public static final x86 MOV_r_cr = _op(0x0F2000, 3, -1, -1, "MOV_r_cr");
    public static final x86 MOV_r_dr = _op(0x0F2100, 3, -1, -1, "MOV_r_dr");
    public static final x86 MOV_dr_r = _op(0x0F2300, 3, -1, -1, "MOV_dr_r");
    public static final x86 MOVD_mm_r = _op(0x0F6E00, 3, -1, -1, "MOVD_mm_r");
    public static final x86 MOVD_mm_m = _op(0x0F6E00, 3, -1, -1, "MOVD_mm_m");
    public static final x86 MOVD_r_mm = _op(0x0F7E00, 3, -1, -1, "MOVD_r_mm");
    public static final x86 MOVD_m_mm = _op(0x0F7E00, 3, -1, -1, "MOVD_m_mm");
    public static final x86 MOVQ_mm_mm = _op(0x0F6F00, 3, -1, -1, "MOVQ_mm_mm");
    public static final x86 MOVQ_mm_m = _op(0x0F6F00, 3, -1, -1, "MOVQ_mm_m");
    public static final x86 MOVQ_m_mm = _op(0x0F7F00, 3, -1, -1, "MOVQ_m_mm");
    public static final x86 MOVS_8 = _op(0xA4, 1, -1, -1, "MOVS_8");
    public static final x86 MOVS_32 = _op(0xA5, 1, -1, -1, "MOVS_32");
    public static final x86 MOVSX_r_r8 = _op(0x0FBE00, 3, -1, -1, "MOVSX_r_r8");
    public static final x86 MOVSX_r_m8 = _op(0x0FBE00, 3, -1, -1, "MOVSX_r_m8");
    public static final x86 MOVSX_r_r16 = _op(0x0FBF00, 3, -1, -1, "MOVSX_r_r16");
    public static final x86 MOVSX_r_m16 = _op(0x0FBF00, 3, -1, -1, "MOVSX_r_m16");
    public static final x86 MOVZX_r_r8 = _op(0x0FB600, 3, -1, -1, "MOVZX_r_r8");
    public static final x86 MOVZX_r_m8 = _op(0x0FB600, 3, -1, -1, "MOVZX_r_m8");
    public static final x86 MOVZX_r_r16 = _op(0x0FB700, 3, -1, -1, "MOVZX_r_r16");
    public static final x86 MOVZX_r_m16 = _op(0x0FB700, 3, -1, -1, "MOVZX_r_m16");
    public static final x86 MUL_rda_r8 = _op(0xF620, 2, -1, -1, "MUL_rda_r8");
    public static final x86 MUL_rda_r32 = _op(0xF720, 2, -1, -1, "MUL_rda_r32");
    public static final x86 MUL_rda_m8 = _op(0xF620, 2, -1, -1, "MUL_rda_m8");
    public static final x86 MUL_rda_m32 = _op(0xF720, 2, -1, -1, "MUL_rda_m32");
    public static final x86 NEG_r8 = _op(0xF618, 2, -1, -1, "NEG_r8");
    public static final x86 NEG_m8 = _op(0xF618, 2, -1, -1, "NEG_m8");
    public static final x86 NEG_r32 = _op(0xF718, 2, -1, -1, "NEG_r32");
    public static final x86 NEG_m32 = _op(0xF718, 2, -1, -1, "NEG_m32");
    public static final x86 NOP = _op(0x90, 1, -1, -1, "NOP");
    public static final x86 NOT_r8 = _op(0xF610, 2, -1, -1, "NOT_r8");
    public static final x86 NOT_m8 = _op(0xF610, 2, -1, -1, "NOT_m8");
    public static final x86 NOT_r32 = _op(0xF710, 2, -1, -1, "NOT_r32");
    public static final x86 NOT_m32 = _op(0xF710, 2, -1, -1, "NOT_m32");
    public static final x86 OR_ra_i8 = _op(0x0C, 1, UV, 1, "OR_ra_i8");
    public static final x86 OR_ra_i32 = _op(0x0D, 1, UV, 1, "OR_ra_i32");
    public static final x86 OR_r_i8 = _op(0x8008, 2, UV, 1, "OR_r_i8");
    public static final x86 OR_r_i32 = _op(0x8108, 2, UV, 1, "OR_r_i32");
    public static final x86 OR_m_i8 = _op(0x8008, 2, UV, 4, "OR_m_i8");
    public static final x86 OR_m_i32 = _op(0x8108, 2, UV, 4, "OR_m_i32");
    public static final x86 OR_r_m8 = _op(0x0A00, 2, UV, 2, "OR_r_m8");
    public static final x86 OR_r_m32 = _op(0x0B00, 2, UV, 2, "OR_r_m32");
    public static final x86 OR_m_r8 = _op(0x0800, 2, UV, 4, "OR_m_r8");
    public static final x86 OR_m_r32 = _op(0x0900, 2, UV, 4, "OR_m_r32");
    public static final x86 OR_r_r8 = _op(0x0A00, 2, UV, 1, "OR_r_r8");
    public static final x86 OR_r_r32 = _op(0x0B00, 2, UV, 1, "OR_r_r32");
    public static final x86 OUT_i8_ra8 = _op(0xE6, 1, -1, -1, "OUT_i8_ra8");
    public static final x86 OUT_i8_ra32 = _op(0xE7, 1, -1, -1, "OUT_i8_ra32");
    public static final x86 OUT_rd_ra8 = _op(0xEE, 1, -1, -1, "OUT_rd_ra8");
    public static final x86 OUT_rd_ra32 = _op(0xEF, 1, -1, -1, "OUT_rd_ra32");
    public static final x86 OUTS_rd_m8 = _op(0x6E, 1, -1, -1, "OUTS_rd_m8");
    public static final x86 OUTS_rd_m32 = _op(0x6F, 1, -1, -1, "OUTS_rd_m32");
    public static final x86 PACKSSWB_mm_mm = _op(0x0F6300, 3, -1, -1, "PACKSSWB_mm_mm");
    public static final x86 PACKSSWB_mm_m = _op(0x0F6300, 3, -1, -1, "PACKSSWB_mm_m");
    public static final x86 PACKSSDW_mm_mm = _op(0x0F6B00, 3, -1, -1, "PACKSSDW_mm_mm");
    public static final x86 PACKSSDW_mm_m = _op(0x0F6B00, 3, -1, -1, "PACKSSDW_mm_m");
    public static final x86 PACKUSWB_mm_mm = _op(0x0F6700, 3, -1, -1, "PACKUSWB_mm_mm");
    public static final x86 PACKUSWB_mm_m = _op(0x0F6700, 3, -1, -1, "PACKUSWB_mm_m");
    public static final x86 PADDB_mm_mm = _op(0x0FFC00, 3, -1, -1, "PADDB_mm_mm");
    public static final x86 PADDW_mm_mm = _op(0x0FFD00, 3, -1, -1, "PADDW_mm_mm");
    public static final x86 PADDD_mm_mm = _op(0x0FFE00, 3, -1, -1, "PADDD_mm_mm");
    public static final x86 PADDB_mm_m = _op(0x0FFC00, 3, -1, -1, "PADDB_mm_m");
    public static final x86 PADDW_mm_m = _op(0x0FFD00, 3, -1, -1, "PADDW_mm_m");
    public static final x86 PADDD_mm_m = _op(0x0FFE00, 3, -1, -1, "PADDD_mm_m");
    public static final x86 PADDSB_mm_mm = _op(0x0FEC00, 3, -1, -1, "PADDSB_mm_mm");
    public static final x86 PADDSW_mm_mm = _op(0x0FED00, 3, -1, -1, "PADDSW_mm_mm");
    public static final x86 PADDSB_mm_m = _op(0x0FEC00, 3, -1, -1, "PADDSB_mm_m");
    public static final x86 PADDSW_mm_m = _op(0x0FED00, 3, -1, -1, "PADDSW_mm_m");
    public static final x86 PADDUSB_mm_mm = _op(0x0FDC00, 3, -1, -1, "PADDUSB_mm_mm");
    public static final x86 PADDUSW_mm_mm = _op(0x0FDD00, 3, -1, -1, "PADDUSW_mm_mm");
    public static final x86 PADDUSB_mm_m = _op(0x0FDC00, 3, -1, -1, "PADDUSB_mm_m");
    public static final x86 PADDUSW_mm_m = _op(0x0FDD00, 3, -1, -1, "PADDUSW_mm_m");
    public static final x86 PAND_mm_mm = _op(0x0FDB00, 3, -1, -1, "PAND_mm_mm");
    public static final x86 PAND_mm_m = _op(0x0FDB00, 3, -1, -1, "PAND_mm_m");
    public static final x86 PANDN_mm_mm = _op(0x0FDF00, 3, -1, -1, "PANDN_mm_mm");
    public static final x86 PANDN_mm_m = _op(0x0FDF00, 3, -1, -1, "PANDN_mm_m");
    public static final x86 PCMPEQB_mm_mm = _op(0x0F7400, 3, -1, -1, "PCMPEQB_mm_mm");
    public static final x86 PCMPEQW_mm_mm = _op(0x0F7500, 3, -1, -1, "PCMPEQW_mm_mm");
    public static final x86 PCMPEQD_mm_mm = _op(0x0F7600, 3, -1, -1, "PCMPEQD_mm_mm");
    public static final x86 PCMPEQB_mm_m = _op(0x0F7400, 3, -1, -1, "PCMPEQB_mm_m");
    public static final x86 PCMPEQW_mm_m = _op(0x0F7500, 3, -1, -1, "PCMPEQW_mm_m");
    public static final x86 PCMPEQD_mm_m = _op(0x0F7600, 3, -1, -1, "PCMPEQD_mm_m");
    public static final x86 PCMPGTB_mm_mm = _op(0x0F6400, 3, -1, -1, "PCMPGTB_mm_mm");
    public static final x86 PCMPGTW_mm_mm = _op(0x0F6500, 3, -1, -1, "PCMPGTW_mm_mm");
    public static final x86 PCMPGTD_mm_mm = _op(0x0F6600, 3, -1, -1, "PCMPGTD_mm_mm");
    public static final x86 PCMPGTB_mm_m = _op(0x0F6400, 3, -1, -1, "PCMPGTB_mm_m");
    public static final x86 PCMPGTW_mm_m = _op(0x0F6500, 3, -1, -1, "PCMPGTW_mm_m");
    public static final x86 PCMPGTD_mm_m = _op(0x0F6600, 3, -1, -1, "PCMPGTD_mm_m");
    public static final x86 PMADDWD_mm_mm = _op(0x0FF500, 3, -1, -1, "PMADDWD_mm_mm");
    public static final x86 PMADDWD_mm_m = _op(0x0FF500, 3, -1, -1, "PMADDWD_mm_m");
    public static final x86 PMULHW_mm_mm = _op(0x0FE500, 3, -1, -1, "PMULHW_mm_mm");
    public static final x86 PMULHW_mm_m = _op(0x0FE500, 3, -1, -1, "PMULHW_mm_m");
    public static final x86 PMULLW_mm_mm = _op(0x0FD500, 3, -1, -1, "PMULLW_mm_mm");
    public static final x86 PMULLW_mm_m = _op(0x0FD500, 3, -1, -1, "PMULLW_mm_m");
    public static final x86 POP_r = _op(0x58, 1, UV, 2, "POP_r");
    public static final x86 POP_m = _op(0x8F00, 2, NP, COMPLEX, "POP_m");
    public static final x86 POP_ds = _op(0x1F, 1, -1, -1, "POP_ds");
    public static final x86 POP_es = _op(0x07, 1, -1, -1, "POP_es");
    public static final x86 POP_ss = _op(0x17, 1, -1, -1, "POP_ss");
    public static final x86 POP_fs = _op(0x0FA1, 2, -1, -1, "POP_fs");
    public static final x86 POP_gs = _op(0x0FA9, 2, -1, -1, "POP_gs");
    public static final x86 POPA = _op(0x61, 1, -1, -1, "POPA");
    public static final x86 POPF = _op(0x9D, 1, -1, -1, "POPF");
    public static final x86 POR_mm_mm = _op(0x0FEB00, 3, -1, -1, "POR_mm_mm");
    public static final x86 POR_mm_m = _op(0x0FEB00, 3, -1, -1, "POR_mm_m");
    public static final x86 PSLLW_mm_mm = _op(0x0FF100, 3, -1, -1, "PSLLW_mm_mm");
    public static final x86 PSLLD_mm_mm = _op(0x0FF200, 3, -1, -1, "PSLLD_mm_mm");
    public static final x86 PSLLQ_mm_mm = _op(0x0FF300, 3, -1, -1, "PSLLQ_mm_mm");
    public static final x86 PSLLW_mm_m = _op(0x0FF100, 3, -1, -1, "PSLLW_mm_m");
    public static final x86 PSLLD_mm_m = _op(0x0FF200, 3, -1, -1, "PSLLD_mm_m");
    public static final x86 PSLLQ_mm_m = _op(0x0FF300, 3, -1, -1, "PSLLQ_mm_m");
    public static final x86 PSLLW_mm_i = _op(0x0F7130, 3, -1, -1, "PSLLW_mm_i");
    public static final x86 PSLLD_mm_i = _op(0x0F7230, 3, -1, -1, "PSLLD_mm_i");
    public static final x86 PSLLQ_mm_i = _op(0x0F7330, 3, -1, -1, "PSLLQ_mm_i");
    public static final x86 PSRAW_mm_mm = _op(0x0FE100, 3, -1, -1, "PSRAW_mm_mm");
    public static final x86 PSRAW_mm_m = _op(0x0FE100, 3, -1, -1, "PSRAW_mm_m");
    public static final x86 PSRAD_mm_mm = _op(0x0FE200, 3, -1, -1, "PSRAD_mm_mm");
    public static final x86 PSRAD_mm_m = _op(0x0FE200, 3, -1, -1, "PSRAD_mm_m");
    public static final x86 PSRAW_mm_i = _op(0x0F7120, 3, -1, -1, "PSRAW_mm_i");
    public static final x86 PSRAD_mm_i = _op(0x0F7220, 3, -1, -1, "PSRAD_mm_i");
    public static final x86 PSRLW_mm_mm = _op(0x0FD100, 3, -1, -1, "PSRLW_mm_mm");
    public static final x86 PSRLW_mm_m = _op(0x0FD100, 3, -1, -1, "PSRLW_mm_m");
    public static final x86 PSRLD_mm_mm = _op(0x0FD200, 3, -1, -1, "PSRLD_mm_mm");
    public static final x86 PSRLD_mm_m = _op(0x0FD200, 3, -1, -1, "PSRLD_mm_m");
    public static final x86 PSRLQ_mm_mm = _op(0x0FD300, 3, -1, -1, "PSRLQ_mm_mm");
    public static final x86 PSRLQ_mm_m = _op(0x0FD300, 3, -1, -1, "PSRLQ_mm_m");
    public static final x86 PSRLW_mm_i = _op(0x0F7110, 3, -1, -1, "PSRLW_mm_i");
    public static final x86 PSRLD_mm_i = _op(0x0F7210, 3, -1, -1, "PSRLD_mm_i");
    public static final x86 PSRLQ_mm_i = _op(0x0F7310, 3, -1, -1, "PSRLQ_mm_i");
    public static final x86 PSUBB_mm_mm = _op(0x0FF800, 3, -1, -1, "PSUBB_mm_mm");
    public static final x86 PSUBB_mm_m = _op(0x0FF800, 3, -1, -1, "PSUBB_mm_m");
    public static final x86 PSUBW_mm_mm = _op(0x0FF900, 3, -1, -1, "PSUBW_mm_mm");
    public static final x86 PSUBW_mm_m = _op(0x0FF900, 3, -1, -1, "PSUBW_mm_m");
    public static final x86 PSUBD_mm_mm = _op(0x0FFA00, 3, -1, -1, "PSUBD_mm_mm");
    public static final x86 PSUBD_mm_m = _op(0x0FFA00, 3, -1, -1, "PSUBD_mm_m");
    public static final x86 PSUBSB_mm_mm = _op(0x0FE800, 3, -1, -1, "PSUBSB_mm_mm");
    public static final x86 PSUBSB_mm_m = _op(0x0FE800, 3, -1, -1, "PSUBSB_mm_m");
    public static final x86 PSUBSW_mm_mm = _op(0x0FE900, 3, -1, -1, "PSUBSW_mm_mm");
    public static final x86 PSUBSW_mm_m = _op(0x0FE900, 3, -1, -1, "PSUBSW_mm_m");
    public static final x86 PSUBUSB_mm_mm = _op(0x0FD800, 3, -1, -1, "PSUBUSB_mm_mm");
    public static final x86 PSUBUSB_mm_m = _op(0x0FD800, 3, -1, -1, "PSUBUSB_mm_m");
    public static final x86 PSUBUSW_mm_mm = _op(0x0FD900, 3, -1, -1, "PSUBUSW_mm_mm");
    public static final x86 PSUBUSW_mm_m = _op(0x0FD900, 3, -1, -1, "PSUBUSW_mm_m");
    public static final x86 PUNPCKHBW_mm_mm = _op(0x0F6800, 3, -1, -1, "PUNPCKHBW_mm_mm");
    public static final x86 PUNPCKHWD_mm_mm = _op(0x0F6900, 3, -1, -1, "PUNPCKHWD_mm_mm");
    public static final x86 PUNPCKHDQ_mm_mm = _op(0x0F6A00, 3, -1, -1, "PUNPCKHDQ_mm_mm");
    public static final x86 PUNPCKHBW_mm_m = _op(0x0F6800, 3, -1, -1, "PUNPCKHBW_mm_m");
    public static final x86 PUNPCKHWD_mm_m = _op(0x0F6900, 3, -1, -1, "PUNPCKHWD_mm_m");
    public static final x86 PUNPCKHDQ_mm_m = _op(0x0F6A00, 3, -1, -1, "PUNPCKHDQ_mm_m");
    public static final x86 PUNPCKLBW_mm_mm = _op(0x0F6000, 3, -1, -1, "PUNPCKLBW_mm_mm");
    public static final x86 PUNPCKLWD_mm_mm = _op(0x0F6100, 3, -1, -1, "PUNPCKLWD_mm_mm");
    public static final x86 PUNPCKLDQ_mm_mm = _op(0x0F6200, 3, -1, -1, "PUNPCKLDQ_mm_mm");
    public static final x86 PUNPCKLBW_mm_m = _op(0x0F6000, 3, -1, -1, "PUNPCKLBW_mm_m");
    public static final x86 PUNPCKLWD_mm_m = _op(0x0F6100, 3, -1, -1, "PUNPCKLWD_mm_m");
    public static final x86 PUNPCKLDQ_mm_m = _op(0x0F6200, 3, -1, -1, "PUNPCKLDQ_mm_m");
    public static final x86 PUSH_i8 = _op(0x6A, 1, -1, -1, "PUSH_i8");
    public static final x86 PUSH_i32 = _op(0x68, 1, -1, -1, "PUSH_i32");
    public static final x86 PUSH_r = _op(0x50, 1, UV, 3, "PUSH_r");
    public static final x86 PUSH_m = _op(0xFF30, 2, NP, 4, "PUSH_m");
    public static final x86 PUSH_cs = _op(0x0E, 1, -1, -1, "PUSH_cs");
    public static final x86 PUSH_ss = _op(0x16, 1, -1, -1, "PUSH_ss");
    public static final x86 PUSH_ds = _op(0x1E, 1, -1, -1, "PUSH_ds");
    public static final x86 PUSH_es = _op(0x06, 1, -1, -1, "PUSH_es");
    public static final x86 PUSH_fs = _op(0x0FA0, 2, -1, -1, "PUSH_fs");
    public static final x86 PUSH_gs = _op(0x0FA8, 2, -1, -1, "PUSH_gs");
    public static final x86 PUSHA = _op(0x60, 1, -1, -1, "PUSHA");
    public static final x86 PUSHF = _op(0x9C, 1, -1, -1, "PUSHF");
    public static final x86 PXOR_mm_mm = _op(0x0FEF00, 3, -1, -1, "PXOR_mm_mm");
    public static final x86 PXOR_mm_m = _op(0x0FEF00, 3, -1, -1, "PXOR_mm_m");
    public static final x86 ROL_r8_1 = _op(0xD000, 2, PU, 1, "ROL_r8_1");
    public static final x86 ROL_m8_1 = _op(0xD000, 2, PU, 4, "ROL_m8_1");
    public static final x86 ROL_r8_rc = _op(0xD200, 2, NP, 1, "ROL_r8_rc");
    public static final x86 ROL_m8_rc = _op(0xD200, 2, NP, 4, "ROL_m8_rc");
    public static final x86 ROL_r8_i = _op(0xC000, 2, PU, 1, "ROL_r8_i");
    public static final x86 ROL_m8_i = _op(0xC000, 2, PU, 4, "ROL_m8_i");
    public static final x86 ROR_r8_1 = _op(0xD008, 2, PU, 1, "ROR_r8_1");
    public static final x86 ROR_m8_1 = _op(0xD008, 2, PU, 4, "ROR_m8_1");
    public static final x86 ROR_r8_rc = _op(0xD208, 2, NP, 1, "ROR_r8_rc");
    public static final x86 ROR_m8_rc = _op(0xD208, 2, NP, 4, "ROR_m8_rc");
    public static final x86 ROR_r8_i = _op(0xC008, 2, PU, 1, "ROR_r8_i");
    public static final x86 ROR_m8_i = _op(0xC008, 2, PU, 4, "ROR_m8_i");
    public static final x86 RCL_r8_1 = _op(0xD010, 2, PU, 2, "RCL_r8_1");
    public static final x86 RCL_m8_1 = _op(0xD010, 2, PU, 4, "RCL_m8_1");
    public static final x86 RCL_r8_rc = _op(0xD210, 2, NP, COMPLEX, "RCL_r8_rc");
    public static final x86 RCL_m8_rc = _op(0xD210, 2, NP, COMPLEX, "RCL_m8_rc");
    public static final x86 RCL_r8_i = _op(0xC010, 2, PU, COMPLEX, "RCL_r8_i");
    public static final x86 RCL_m8_i = _op(0xC010, 2, PU, COMPLEX, "RCL_m8_i");
    public static final x86 RCR_r8_1 = _op(0xD018, 2, PU, 2, "RCR_r8_1");
    public static final x86 RCR_m8_1 = _op(0xD018, 2, PU, 4, "RCR_m8_1");
    public static final x86 RCR_r8_rc = _op(0xD218, 2, NP, COMPLEX, "RCR_r8_rc");
    public static final x86 RCR_m8_rc = _op(0xD218, 2, NP, COMPLEX, "RCR_m8_rc");
    public static final x86 RCR_r8_i = _op(0xC018, 2, PU, COMPLEX, "RCR_r8_i");
    public static final x86 RCR_m8_i = _op(0xC018, 2, PU, COMPLEX, "RCR_m8_i");
    public static final x86 ROL_r32_1 = _op(0xD100, 2, PU, 1, "ROL_r32_1");
    public static final x86 ROL_m32_1 = _op(0xD100, 2, PU, 4, "ROL_m32_1");
    public static final x86 ROL_r32_rc = _op(0xD300, 2, NP, 1, "ROL_r32_rc");
    public static final x86 ROL_m32_rc = _op(0xD300, 2, NP, 4, "ROL_m32_rc");
    public static final x86 ROL_r32_i = _op(0xC100, 2, PU, 1, "ROL_r32_i");
    public static final x86 ROL_m32_i = _op(0xC100, 2, PU, 4, "ROL_m32_i");
    public static final x86 ROR_r32_1 = _op(0xD108, 2, PU, 1, "ROR_r32_1");
    public static final x86 ROR_m32_1 = _op(0xD108, 2, PU, 4, "ROR_m32_1");
    public static final x86 ROR_r32_rc = _op(0xD308, 2, NP, 1, "ROR_r32_rc");
    public static final x86 ROR_m32_rc = _op(0xD308, 2, NP, 4, "ROR_m32_rc");
    public static final x86 ROR_r32_i = _op(0xC108, 2, PU, 1, "ROR_r32_i");
    public static final x86 ROR_m32_i = _op(0xC108, 2, PU, 4, "ROR_m32_i");
    public static final x86 RCL_r32_1 = _op(0xD110, 2, PU, 2, "RCL_r32_1");
    public static final x86 RCL_m32_1 = _op(0xD110, 2, PU, 4, "RCL_m32_1");
    public static final x86 RCL_r32_rc = _op(0xD310, 2, NP, COMPLEX, "RCL_r32_rc");
    public static final x86 RCL_m32_rc = _op(0xD310, 2, NP, COMPLEX, "RCL_m32_rc");
    public static final x86 RCL_r32_i = _op(0xC110, 2, PU, COMPLEX, "RCL_r32_i");
    public static final x86 RCL_m32_i = _op(0xC110, 2, PU, COMPLEX, "RCL_m32_i");
    public static final x86 RCR_r32_1 = _op(0xD118, 2, PU, 2, "RCR_r32_1");
    public static final x86 RCR_m32_1 = _op(0xD118, 2, PU, 4, "RCR_m32_1");
    public static final x86 RCR_r32_rc = _op(0xD318, 2, NP, COMPLEX, "RCR_r32_rc");
    public static final x86 RCR_m32_rc = _op(0xD318, 2, NP, COMPLEX, "RCR_m32_rc");
    public static final x86 RCR_r32_i = _op(0xC118, 2, PU, COMPLEX, "RCR_r32_i");
    public static final x86 RCR_m32_i = _op(0xC118, 2, PU, COMPLEX, "RCR_m32_i");
    public static final x86 RDMSR = _op(0x0F32, 2, -1, -1, "RDMSR");
    public static final x86 RDPMC = _op(0x0F33, 2, -1, -1, "RDPMC");
    public static final x86 RDTSC = _op(0x0F31, 2, -1, -1, "RDTSC");
    public static final x86 RET = _op(0xC3, 1, -1, -1, "RET");
    public static final x86 RET_far = _op(0xCB, 1, -1, -1, "RET_far");
    public static final x86 RET_i = _op(0xC2, 1, -1, -1, "RET_i");
    public static final x86 RET_i_far = _op(0xCA, 1, -1, -1, "RET_i_far");
    public static final x86 RSM = _op(0x0FAA, 2, -1, -1, "RSM");
    public static final x86 SAHF = _op(0x9E, 1, -1, -1, "SAHF");
    public static final x86 SHL_r8_1 = _op(0xD020, 2, PU, 1, "SHL_r8_1");
    public static final x86 SHL_m8_1 = _op(0xD020, 2, PU, 4, "SHL_m8_1");
    public static final x86 SHL_r8_rc = _op(0xD220, 2, NP, 1, "SHL_r8_rc");
    public static final x86 SHL_m8_rc = _op(0xD220, 2, NP, 4, "SHL_m8_rc");
    public static final x86 SHL_r8_i = _op(0xC020, 2, PU, 1, "SHL_r8_i");
    public static final x86 SHL_m8_i = _op(0xC020, 2, PU, 4, "SHL_m8_i");
    public static final x86 SHR_r8_1 = _op(0xD028, 2, PU, 1, "SHR_r8_1");
    public static final x86 SHR_m8_1 = _op(0xD028, 2, PU, 4, "SHR_m8_1");
    public static final x86 SHR_r8_rc = _op(0xD228, 2, NP, 1, "SHR_r8_rc");
    public static final x86 SHR_m8_rc = _op(0xD228, 2, NP, 4, "SHR_m8_rc");
    public static final x86 SHR_r8_i = _op(0xC028, 2, PU, 1, "SHR_r8_i");
    public static final x86 SHR_m8_i = _op(0xC028, 2, PU, 4, "SHR_m8_i");
    public static final x86 SAR_r8_1 = _op(0xD038, 2, PU, 1, "SAR_r8_1");
    public static final x86 SAR_m8_1 = _op(0xD038, 2, PU, 4, "SAR_m8_1");
    public static final x86 SAR_r8_rc = _op(0xD238, 2, NP, 1, "SAR_r8_rc");
    public static final x86 SAR_m8_rc = _op(0xD238, 2, NP, 4, "SAR_m8_rc");
    public static final x86 SAR_r8_i = _op(0xC038, 2, PU, 1, "SAR_r8_i");
    public static final x86 SAR_m8_i = _op(0xC038, 2, PU, 4, "SAR_m8_i");
    public static final x86 SHL_r32_1 = _op(0xD120, 2, PU, 1, "SHL_r32_1");
    public static final x86 SHL_m32_1 = _op(0xD120, 2, PU, 4, "SHL_m32_1");
    public static final x86 SHL_r32_rc = _op(0xD320, 2, NP, 1, "SHL_r32_rc");
    public static final x86 SHL_m32_rc = _op(0xD320, 2, NP, 4, "SHL_m32_rc");
    public static final x86 SHL_r32_i = _op(0xC120, 2, PU, 1, "SHL_r32_i");
    public static final x86 SHL_m32_i = _op(0xC120, 2, PU, 4, "SHL_m32_i");
    public static final x86 SHR_r32_1 = _op(0xD128, 2, PU, 1, "SHR_r32_1");
    public static final x86 SHR_m32_1 = _op(0xD128, 2, PU, 4, "SHR_m32_1");
    public static final x86 SHR_r32_rc = _op(0xD328, 2, NP, 1, "SHR_r32_rc");
    public static final x86 SHR_m32_rc = _op(0xD328, 2, NP, 4, "SHR_m32_rc");
    public static final x86 SHR_r32_i = _op(0xC128, 2, PU, 1, "SHR_r32_i");
    public static final x86 SHR_m32_i = _op(0xC128, 2, PU, 4, "SHR_m32_i");
    public static final x86 SAR_r32_1 = _op(0xD138, 2, PU, 1, "SAR_r32_1");
    public static final x86 SAR_m32_1 = _op(0xD138, 2, PU, 4, "SAR_m32_1");
    public static final x86 SAR_r32_rc = _op(0xD338, 2, NP, 1, "SAR_r32_rc");
    public static final x86 SAR_m32_rc = _op(0xD338, 2, NP, 4, "SAR_m32_rc");
    public static final x86 SAR_r32_i = _op(0xC138, 2, PU, 1, "SAR_r32_i");
    public static final x86 SAR_m32_i = _op(0xC138, 2, PU, 4, "SAR_m32_i");
    public static final x86 SBB_ra_i8 = _op(0x1C, 1, PU, 2, "SBB_ra_i8");
    public static final x86 SBB_ra_i32 = _op(0x1D, 1, PU, 2, "SBB_ra_i32");
    public static final x86 SBB_r_i8 = _op(0x8018, 2, PU, 2, "SBB_r_i8");
    public static final x86 SBB_m_i8 = _op(0x8018, 2, PU, 4, "SBB_m_i8");
    public static final x86 SBB_r_i32 = _op(0x8118, 2, PU, 2, "SBB_r_i32");
    public static final x86 SBB_m_i32 = _op(0x8118, 2, PU, 4, "SBB_m_i32");
    public static final x86 SBB_r_m8 = _op(0x1A00, 2, PU, 3, "SBB_r_m8");
    public static final x86 SBB_m_r8 = _op(0x1800, 2, PU, 4, "SBB_m_r8");
    public static final x86 SBB_r_r8 = _op(0x1A00, 2, PU, 2, "SBB_r_r8");
    public static final x86 SBB_r_m32 = _op(0x1B00, 2, PU, 3, "SBB_r_m32");
    public static final x86 SBB_m_r32 = _op(0x1900, 2, PU, 4, "SBB_m_r32");
    public static final x86 SBB_r_r32 = _op(0x1B00, 2, PU, 2, "SBB_r_r32");
    public static final x86 SCAS_m8 = _op(0xAE, 1, -1, -1, "SCAS_m8");
    public static final x86 SCAS_m32 = _op(0xAF, 1, -1, -1, "SCAS_m32");
    public static final x86 SETO_r = _op(0x0F90, 2, -1, -1, "SETO_r");
    public static final x86 SETNO_r = _op(0x0F91, 2, -1, -1, "SETNO_r");
    public static final x86 SETB_r = _op(0x0F92, 2, -1, -1, "SETB_r");
    public static final x86 SETAE_r = _op(0x0F93, 2, -1, -1, "SETAE_r");
    public static final x86 SETE_r = _op(0x0F94, 2, -1, -1, "SETE_r");
    public static final x86 SETNE_r = _op(0x0F95, 2, -1, -1, "SETNE_r");
    public static final x86 SETNA_r = _op(0x0F96, 2, -1, -1, "SETNA_r");
    public static final x86 SETNBE_r = _op(0x0F97, 2, -1, -1, "SETNBE_r");
    public static final x86 SETS_r = _op(0x0F98, 2, -1, -1, "SETS_r");
    public static final x86 SETNS_r = _op(0x0F99, 2, -1, -1, "SETNS_r");
    public static final x86 SETP_r = _op(0x0F9A, 2, -1, -1, "SETP_r");
    public static final x86 SETNP_r = _op(0x0F9B, 2, -1, -1, "SETNP_r");
    public static final x86 SETL_r = _op(0x0F9C, 2, -1, -1, "SETL_r");
    public static final x86 SETGE_r = _op(0x0F9D, 2, -1, -1, "SETGE_r");
    public static final x86 SETLE_r = _op(0x0F9E, 2, -1, -1, "SETLE_r");
    public static final x86 SETG_r = _op(0x0F9F, 2, -1, -1, "SETG_r");
    public static final x86 SETO_m = _op(0x0F90, 2, -1, -1, "SETO_m");
    public static final x86 SETNO_m = _op(0x0F91, 2, -1, -1, "SETNO_m");
    public static final x86 SETB_m = _op(0x0F92, 2, -1, -1, "SETB_m");
    public static final x86 SETAE_m = _op(0x0F93, 2, -1, -1, "SETAE_m");
    public static final x86 SETE_m = _op(0x0F94, 2, -1, -1, "SETE_m");
    public static final x86 SETNE_m = _op(0x0F95, 2, -1, -1, "SETNE_m");
    public static final x86 SETNA_m = _op(0x0F96, 2, -1, -1, "SETNA_m");
    public static final x86 SETNBE_m = _op(0x0F97, 2, -1, -1, "SETNBE_m");
    public static final x86 SETS_m = _op(0x0F98, 2, -1, -1, "SETS_m");
    public static final x86 SETNS_m = _op(0x0F99, 2, -1, -1, "SETNS_m");
    public static final x86 SETP_m = _op(0x0F9A, 2, -1, -1, "SETP_m");
    public static final x86 SETNP_m = _op(0x0F9B, 2, -1, -1, "SETNP_m");
    public static final x86 SETL_m = _op(0x0F9C, 2, -1, -1, "SETL_m");
    public static final x86 SETGE_m = _op(0x0F9D, 2, -1, -1, "SETGE_m");
    public static final x86 SETLE_m = _op(0x0F9E, 2, -1, -1, "SETLE_m");
    public static final x86 SETG_m = _op(0x0F9F, 2, -1, -1, "SETG_m");
    public static final x86 SGDT = _op(0x0F0100, 3, -1, -1, "SGDT");
    public static final x86 SIDT = _op(0x0F0108, 3, -1, -1, "SIDT");
    public static final x86 SHLD_r_r_i = _op(0x0FA400, 3, -1, -1, "SHLD_r_r_i");
    public static final x86 SHLD_r_r_rc = _op(0x0FA500, 3, -1, -1, "SHLD_r_r_rc");
    public static final x86 SHLD_m_r_i = _op(0x0FA400, 3, -1, -1, "SHLD_m_r_i");
    public static final x86 SHLD_m_r_rc = _op(0x0FA500, 3, -1, -1, "SHLD_m_r_rc");
    public static final x86 SHRD_r_r_i = _op(0x0FAC00, 3, -1, -1, "SHRD_r_r_i");
    public static final x86 SHRD_r_r_rc = _op(0x0FAD00, 3, -1, -1, "SHRD_r_r_rc");
    public static final x86 SHRD_m_r_i = _op(0x0FAC00, 3, -1, -1, "SHRD_m_r_i");
    public static final x86 SHRD_m_r_rc = _op(0x0FAD00, 3, -1, -1, "SHRD_m_r_rc");
    public static final x86 SLDT = _op(0x0F0000, 3, -1, -1, "SLDT");
    public static final x86 SMSW = _op(0x0F0120, 3, -1, -1, "SMSW");
    public static final x86 STC = _op(0xF9, 1, -1, -1, "STC");
    public static final x86 STD = _op(0xFD, 1, -1, -1, "STD");
    public static final x86 STI = _op(0xFB, 1, -1, -1, "STI");
    public static final x86 STOS_m8 = _op(0xAA, 1, -1, -1, "STOS_m8");
    public static final x86 STOS_m32 = _op(0xAB, 1, -1, -1, "STOS_m32");
    public static final x86 STR_r = _op(0x0F0080, 3, -1, -1, "STR_r");
    public static final x86 STR_m = _op(0x0F0080, 3, -1, -1, "STR_m");
    public static final x86 SUB_ra_i8 = _op(0x2C, 1, UV, 1, "SUB_ra_i8");
    public static final x86 SUB_r_i8 = _op(0x8028, 2, UV, 1, "SUB_r_i8");
    public static final x86 SUB_m_i8 = _op(0x8028, 2, UV, 4, "SUB_m_i8");
    public static final x86 SUB_r_m8 = _op(0x2A00, 2, UV, 2, "SUB_r_m8");
    public static final x86 SUB_m_r8 = _op(0x2800, 2, UV, 4, "SUB_m_r8");
    public static final x86 SUB_r_r8 = _op(0x2A00, 2, UV, 1, "SUB_r_r8");
    public static final x86 SUB_ra_i32 = _op(0x2D, 1, UV, 1, "SUB_ra_i32");
    public static final x86 SUB_r_i32 = _op(0x8128, 2, UV, 1, "SUB_r_i32");
    public static final x86 SUB_m_i32 = _op(0x8128, 2, UV, 4, "SUB_m_i32");
    public static final x86 SUB_r_m32 = _op(0x2B00, 2, UV, 2, "SUB_r_m32");
    public static final x86 SUB_m_r32 = _op(0x2900, 2, UV, 4, "SUB_m_r32");
    public static final x86 SUB_r_r32 = _op(0x2B00, 2, UV, 1, "SUB_r_r32");
    public static final x86 TEST_ra_i8 = _op(0xA8, 1, -1, -1, "TEST_ra_i8");
    public static final x86 TEST_ra_i32 = _op(0xA9, 1, -1, -1, "TEST_ra_i32");
    public static final x86 TEST_r_i8 = _op(0xF600, 2, -1, -1, "TEST_r_i8");
    public static final x86 TEST_r_i32 = _op(0xF700, 2, -1, -1, "TEST_r_i32");
    public static final x86 TEST_m_i8 = _op(0xF600, 2, -1, -1, "TEST_m_i8");
    public static final x86 TEST_m_i32 = _op(0xF700, 2, -1, -1, "TEST_m_i32");
    public static final x86 TEST_r_r8 = _op(0x8400, 2, -1, -1, "TEST_r_r8");
    public static final x86 TEST_r_r32 = _op(0x8500, 2, -1, -1, "TEST_r_r32");
    public static final x86 TEST_m_r8 = _op(0x8400, 2, -1, -1, "TEST_m_r8");
    public static final x86 TEST_m_r32 = _op(0x8500, 2, -1, -1, "TEST_m_r32");
    public static final x86 UD2 = _op(0x0F0B, 2, -1, -1, "UD2");
    public static final x86 VERR_r = _op(0x0F0020, 3, -1, -1, "VERR_r");
    public static final x86 VERR_m = _op(0x0F0020, 3, -1, -1, "VERR_m");
    public static final x86 VERW_r = _op(0x0F0028, 3, -1, -1, "VERW_r");
    public static final x86 VERW_m = _op(0x0F0028, 3, -1, -1, "VERW_m");
    public static final x86 WAIT = _op(0x9B, 1, -1, -1, "WAIT");
    public static final x86 WBINVD = _op(0x0F09, 2, -1, -1, "WBINVD");
    public static final x86 WRMSR = _op(0x0F30, 2, -1, -1, "WRMSR");
    public static final x86 XADD_r_r8 = _op(0x0FC000, 3, -1, -1, "XADD_r_r8");
    public static final x86 XADD_r_r32 = _op(0x0FC100, 3, -1, -1, "XADD_r_r32");
    public static final x86 XADD_m_r8 = _op(0x0FC000, 3, -1, -1, "XADD_m_r8");
    public static final x86 XADD_m_r32 = _op(0x0FC100, 3, -1, -1, "XADD_m_r32");
    public static final x86 XCHG_ra_r = _op(0x90, 1, -1, -1, "XCHG_ra_r");
    public static final x86 XCHG_r_r8 = _op(0x8600, 2, -1, -1, "XCHG_r_r8");
    public static final x86 XCHG_m_r8 = _op(0x8600, 2, -1, -1, "XCHG_m_r8");
    public static final x86 XCHG_r_r32 = _op(0x8700, 2, -1, -1, "XCHG_r_r32");
    public static final x86 XCHG_m_r32 = _op(0x8700, 2, -1, -1, "XCHG_m_r32");
    public static final x86 XLAT = _op(0xD7, 1, -1, -1, "XLAT");
    public static final x86 XOR_ra_i8 = _op(0x34, 1, UV, 1, "XOR_ra_i8");
    public static final x86 XOR_r_i8 = _op(0x8030, 2, UV, 1, "XOR_r_i8");
    public static final x86 XOR_m_i8 = _op(0x8030, 2, UV, 4, "XOR_m_i8");
    public static final x86 XOR_r_m8 = _op(0x3200, 2, UV, 2, "XOR_r_m8");
    public static final x86 XOR_m_r8 = _op(0x3000, 2, UV, 4, "XOR_m_r8");
    public static final x86 XOR_r_r8 = _op(0x3200, 2, UV, 1, "XOR_r_r8");
    public static final x86 XOR_ra_i32 = _op(0x3500, 2, UV, 1, "XOR_ra_i32");
    public static final x86 XOR_r_i32 = _op(0x8130, 2, UV, 1, "XOR_r_i32");
    public static final x86 XOR_m_i32 = _op(0x8130, 2, UV, 4, "XOR_m_i32");
    public static final x86 XOR_r_m32 = _op(0x3300, 2, UV, 2, "XOR_r_m32");
    public static final x86 XOR_m_r32 = _op(0x3100, 2, UV, 4, "XOR_m_r32");
    public static final x86 XOR_r_r32 = _op(0x3300, 2, UV, 1, "XOR_r_r32");

    public static final byte PREFIX_LOCK = (byte)0xf0;
    public static final byte PREFIX_REPNE = (byte)0xf2;
    public static final byte PREFIX_REP = (byte)0xf3;
    public static final byte PREFIX_REPE = (byte)0xf3;
    public static final byte PREFIX_16BIT = 0x66;
    public static final byte PREFIX_CS = 0x2e;
    public static final byte PREFIX_SS = 0x36;
    public static final byte PREFIX_DS = 0x3e;
    public static final byte PREFIX_ES = 0x26;
    public static final byte PREFIX_FS = 0x64;
    public static final byte PREFIX_GS = 0x65;
    public static final byte PREFIX_16BIT_ADDR = 0x67;
    public static final byte BRHINT_NOT_TAKEN = 0x2e;
    public static final byte BRHINT_TAKEN = 0x3e;

    public static final int s_Short_Reg() { return 1; }
    final int emitShort_Reg(CodeAllocator.x86CodeBuffer mc, int reg) {
        Assert._assert(length == 1);
        Assert._assert((opcode & 0x07) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null)
            mc.add1((byte)(opcode+reg));
        if (x86Assembler.TRACE) dbg(mc, 1, desc, RegToString(reg));
        return 1;
    }
    public static final int s_Short_Reg_Imm8() { return 2; }
    final int emitShort_Reg_Imm8(CodeAllocator.x86CodeBuffer mc, int reg, int imm) {
        Assert._assert(length == 1);
        Assert._assert((opcode & 0x07) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add1((byte)(opcode+reg));
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, RegToString(reg), Imm8ToString(imm));
        return 2;
    }
    public static final int s_Short_Reg_Imm32() { return 5; }
    final int emitShort_Reg_Imm32(CodeAllocator.x86CodeBuffer mc, int reg, int imm) {
        Assert._assert(length == 1);
        Assert._assert((opcode & 0x07) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add1((byte)(opcode+reg));
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, RegToString(reg), Imm32ToString(imm));
        return 5;
    }
    public static final int s_1() { return 1; }
    final int emit1(CodeAllocator.x86CodeBuffer mc) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)opcode);
        }
        if (x86Assembler.TRACE) dbg(mc, 1, desc);
        return 1;
    }
    public static final int s_CJump_Short() { return 2; }
    final int emitCJump_Short(CodeAllocator.x86CodeBuffer mc, byte offset) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)(opcode | CJUMP_SHORT));
            mc.add1((byte)(offset & 0xFF));
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, Strings.hex(mc.getCurrentOffset()+offset)+" (offset "+Strings.shex(offset)+")");
        return 2;
    }
    public static final int s_Jump_Short() { return 2; }
    final int emitJump_Short(CodeAllocator.x86CodeBuffer mc, byte offset) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)(opcode | JUMP_SHORT));
            mc.add1((byte)(offset & 0xFF));
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, Strings.hex(mc.getCurrentOffset()+offset)+" (offset "+Strings.shex(offset)+")");
        return 2;
    }
    public static final int s_1_Imm8() { return 2; }
    final int emit1_Imm8(CodeAllocator.x86CodeBuffer mc, int imm) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)opcode);
            mc.add1((byte)(imm & 0xFF));
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, Imm8ToString(imm));
        return 2;
    }
    public static final int s_1_Imm16() { return 3; }
    final int emit1_Imm16(CodeAllocator.x86CodeBuffer mc, char imm) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)opcode);
            mc.add2_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, Imm16ToString(imm));
        return 3;
    }
    public static final int s_1_Imm32() { return 5; }
    final int emit1_Imm32(CodeAllocator.x86CodeBuffer mc, int imm) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)opcode);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, Imm32ToString(imm));
        return 5;
    }
    public static final int s_Jump_Near() { return 5; }
    final int emitJump_Near(CodeAllocator.x86CodeBuffer mc, int offset) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)(opcode | JUMP_NEAR));
            mc.add4_endian(offset);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, (mc.getCurrentOffset()+offset)+" (offset "+offset+")");
        return 5;
    }
    public static final int s_Call_Near() { return 5; }
    final int emitCall_Near(CodeAllocator.x86CodeBuffer mc, int offset) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)(opcode));
            mc.add4_endian(offset);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, (mc.getCurrentOffset()+offset)+" (offset "+offset+")");
        return 5;
    }
    public static final int s_RA_1_Imm32() { return 5; }
    final int emit1_RA_Imm32(CodeAllocator.x86CodeBuffer mc, int imm) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add1((byte)(opcode | RA));
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, "EAX", Imm32ToString(imm));
        return 5;
    }
    public static final int s_2() { return 2; }
    final int emit2(CodeAllocator.x86CodeBuffer mc) {
        Assert._assert(length == 2);
        if (mc != null) {
            mc.add2(opcode);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc);
        return 2;
    }
    public static final int s_2_FPReg() { return 2; }
    final int emit2_FPReg(CodeAllocator.x86CodeBuffer mc, int r) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0x7) == 0);
        Assert._assert(r >= 0 && r <= 7);
        if (mc != null) {
            mc.add2(opcode + r);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, FPRegToString(r));
        return 2;
    }
    public static final int s_2_Imm32() { return 6; }
    final int emit2_Imm32(CodeAllocator.x86CodeBuffer mc, int imm) {
        Assert._assert(length == 2);
        if (mc != null) {
            mc.add2(opcode);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, Imm32ToString(imm));
        return 6;
    }
    public static final int s_CJump_Near() { return 6; }
    final int emitCJump_Near(CodeAllocator.x86CodeBuffer mc, int offset) {
        Assert._assert(length == 1);
        if (mc != null) {
            mc.add2(opcode | CJUMP_NEAR);
            mc.add4_endian(offset);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, (mc.getCurrentOffset()+offset)+" (offset "+offset+")");
        return 6;
    }
    public static final int s_2_Reg() { return 2; }
    final int emit2_Reg(CodeAllocator.x86CodeBuffer mc, int reg) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_REG | reg);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, RegToString(reg));
        return 2;
    }
    public static final int s_2_Once_Reg() { return 2; }
    final int emit2_Once_Reg(CodeAllocator.x86CodeBuffer mc, int reg) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_REG | reg);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, RegToString(reg), "1");
        return 2;
    }
    public static final int s_2_Reg_SEImm8() { return 3; }
    final int emit2_Reg_SEImm8(CodeAllocator.x86CodeBuffer mc, int reg, byte imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_REG | reg);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, RegToString(reg), SEImm8ToString(imm));
        return 3;
    }
    public static final int s_2_Reg_Imm8() { return 3; }
    final int emit2_Reg_Imm8(CodeAllocator.x86CodeBuffer mc, int reg, int imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_REG | reg);
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, RegToString(reg), Imm8ToString(imm));
        return 3;
    }
    public static final int s_2_Reg_Imm32() { return 6; }
    final int emit2_Reg_Imm32(CodeAllocator.x86CodeBuffer mc, int reg, int imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(reg >= EAX && reg <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_REG | reg);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, RegToString(reg), Imm32ToString(imm));
        return 6;
    }
    public static final int s_2_Reg_Reg() { return 2; }
    final int emit2_Reg_Reg(CodeAllocator.x86CodeBuffer mc, int toreg, int fromreg) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(toreg >= EAX && toreg <= EDI);
        Assert._assert(fromreg >= EAX && fromreg <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_REG | (toreg << 3) | fromreg);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, RegToString(toreg), RegToString(fromreg));
        return 2;
    }
    public static final int s_2_Reg_EA() { return 2; }
    final int emit2_Reg_EA(CodeAllocator.x86CodeBuffer mc, int r1, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | (r1 << 3) | base);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, RegToString(r1), EAToString(base));
        return 2;
    }
    public static final int s_2_Reg_DISP8() { return 3; }
    final int emit2_Reg_DISP8(CodeAllocator.x86CodeBuffer mc, int r1, byte off, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | (r1 << 3) | base);
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, RegToString(r1), DISP8ToString(off, base));
        return 3;
    }
    public static final int s_2_Reg_DISP8_SEImm8() { return 4; }
    final int emit2_Reg_DISP8_SEImm8(CodeAllocator.x86CodeBuffer mc, int r1, byte off, int base, byte imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_DISP8 | (r1 << 3) | base);
            mc.add1(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, RegToString(r1), DISP8ToString(off, base), SEImm8ToString(imm));
        return 4;
    }
    public static final int s_2_Reg_DISP8_Imm8() { return 4; }
    final int emit2_Reg_DISP8_Imm8(CodeAllocator.x86CodeBuffer mc, int r1, byte off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | (r1 << 3) | base);
            mc.add1(off);
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, RegToString(r1), DISP8ToString(off, base), Imm8ToString(imm));
        return 4;
    }
    public static final int s_2_Reg_DISP8_Imm32() { return 7; }
    final int emit2_Reg_DISP8_Imm32(CodeAllocator.x86CodeBuffer mc, int r1, byte off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | (r1 << 3) | base);
            mc.add1(off);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, RegToString(r1), DISP8ToString(off, base), Imm32ToString(imm));
        return 7;
    }
    public static final int s_2_Reg_SIB_EA() { return 3; }
    final int emit2_Reg_SIB_EA(CodeAllocator.x86CodeBuffer mc, int r1, int base, int ind, int scale) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | (r1 << 3) | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, RegToString(r1), EA_SIBToString(base, ind, scale));
        return 3;
    }
    public static final int s_2_Reg_SIB_DISP8() { return 4; }
    final int emit2_Reg_SIB_DISP8(CodeAllocator.x86CodeBuffer mc, int r1, int base, int ind, int scale, byte off) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | (r1 << 3) | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, RegToString(r1), DISP8_SIBToString(base, ind, scale, off));
        return 4;
    }
    public static final int s_2_Reg_DISP32() { return 6; }
    final int emit2_Reg_DISP32(CodeAllocator.x86CodeBuffer mc, int r1, int off, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | (r1 << 3) | base);
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, RegToString(r1), DISP32ToString(off, base));
        return 6;
    }
    public static final int s_2_Reg_DISP32_SEImm8() { return 7; }
    final int emit2_Reg_DISP32_SEImm8(CodeAllocator.x86CodeBuffer mc, int r1, int off, int base, byte imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_DISP32 | (r1 << 3) | base);
            mc.add4_endian(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, RegToString(r1), DISP32ToString(off, base), SEImm8ToString(imm));
        return 7;
    }
    public static final int s_2_Reg_DISP32_Imm8() { return 7; }
    final int emit2_Reg_DISP32_Imm8(CodeAllocator.x86CodeBuffer mc, int r1, int off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | (r1 << 3) | base);
            mc.add4_endian(off);
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, RegToString(r1), DISP32ToString(off, base), Imm8ToString(imm));
        return 7;
    }
    public static final int s_2_Reg_DISP32_Imm32() { return 10; }
    final int emit2_Reg_DISP32_Imm32(CodeAllocator.x86CodeBuffer mc, int r1, int off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | (r1 << 3) | base);
            mc.add4_endian(off);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 10, desc, RegToString(r1), DISP32ToString(off, base), Imm32ToString(imm));
        return 10;
    }
    public static final int s_2_Reg_SIB_DISP32() { return 7; }
    final int emit2_Reg_SIB_DISP32(CodeAllocator.x86CodeBuffer mc, int r1, int base, int ind, int scale, int off) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | (r1 << 3) | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, RegToString(r1), DISP32_SIBToString(base, ind, scale, off));
        return 7;
    }
    public static final int s_2_Reg_Abs32() { return 6; }
    final int emit2_Reg_Abs32(CodeAllocator.x86CodeBuffer mc, int r1, int addr) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | (r1 << 3) | EBP);
            mc.add4_endian(addr);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, RegToString(r1), Abs32ToString(addr));
        return 6;
    }
    public static final int s_2_EA() { return 2; }
    final int emit2_EA(CodeAllocator.x86CodeBuffer mc, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | base);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, EAToString(base));
        return 2;
    }
    public static final int s_2_Once_EA() { return 2; }
    final int emit2_Once_EA(CodeAllocator.x86CodeBuffer mc, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_EA | base);
        }
        if (x86Assembler.TRACE) dbg(mc, 2, desc, EAToString(base), "1");
        return 2;
    }
    public static final int s_2_EA_SEImm8() { return 3; }
    final int emit2_EA_SEImm8(CodeAllocator.x86CodeBuffer mc, int base, byte imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_EA | base);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, EAToString(base), SEImm8ToString(imm));
        return 3;
    }
    public static final int s_2_EA_Imm8() { return 3; }
    final int emit2_EA_Imm8(CodeAllocator.x86CodeBuffer mc, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | base);
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, EAToString(base), Imm8ToString(imm));
        return 3;
    }
    public static final int s_2_EA_Imm32() { return 6; }
    final int emit2_EA_Imm32(CodeAllocator.x86CodeBuffer mc, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | base);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, EAToString(base), Imm32ToString(imm));
        return 6;
    }
    public static final int s_2_SIB_EA() { return 3; }
    final int emit2_SIB_EA(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, EA_SIBToString(base, ind, scale));
        return 3;
    }
    public static final int s_2_Once_SIB_EA() { return 3; }
    final int emit2_Once_SIB_EA(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_EA | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, EA_SIBToString(base, ind, scale), "1");
        return 3;
    }
    public static final int s_2_SIB_EA_SEImm8() { return 4; }
    final int emit2_SIB_EA_SEImm8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, byte imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_EA | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, EA_SIBToString(base, ind, scale), SEImm8ToString(imm));
        return 4;
    }
    public static final int s_2_SIB_EA_Imm8() { return 4; }
    final int emit2_SIB_EA_Imm8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, EA_SIBToString(base, ind, scale), Imm8ToString(imm));
        return 4;
    }
    public static final int s_2_SIB_EA_Imm32() { return 7; }
    final int emit2_SIB_EA_Imm32(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, EA_SIBToString(base, ind, scale), Imm32ToString(imm));
        return 7;
    }
    public static final int s_2_DISP8() { return 3; }
    final int emit2_DISP8(CodeAllocator.x86CodeBuffer mc, byte off, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | base);
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, DISP8ToString(off, base));
        return 3;
    }
    public static final int s_2_Once_DISP8() { return 3; }
    final int emit2_Once_DISP8(CodeAllocator.x86CodeBuffer mc, byte off, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_DISP8 | base);
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, DISP8ToString(off, base), "1");
        return 3;
    }
    public static final int s_2_DISP8_SEImm8() { return 4; }
    final int emit2_DISP8_SEImm8(CodeAllocator.x86CodeBuffer mc, byte off, int base, byte imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_DISP8 | base);
            mc.add1(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, DISP8ToString(off, base), SEImm8ToString(imm));
        return 4;
    }
    public static final int s_2_DISP8_Imm8() { return 4; }
    final int emit2_DISP8_Imm8(CodeAllocator.x86CodeBuffer mc, byte off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | base);
            mc.add1(off);
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, DISP8ToString(off, base), Imm8ToString(imm));
        return 4;
    }
    public static final int s_2_DISP8_Imm32() { return 7; }
    final int emit2_DISP8_Imm32(CodeAllocator.x86CodeBuffer mc, byte off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | base);
            mc.add1(off);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, DISP8ToString(off, base), Imm32ToString(imm));
        return 7;
    }
    public static final int s_2_SIB_DISP8() { return 4; }
    final int emit2_SIB_DISP8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, byte off) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, DISP8_SIBToString(base, ind, scale, off));
        return 4;
    }
    public static final int s_2_Once_SIB_DISP8() { return 4; }
    final int emit2_Once_SIB_DISP8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, byte off) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_DISP8 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, DISP8_SIBToString(base, ind, scale, off), "1");
        return 4;
    }
    public static final int s_2_SIB_DISP8_Imm8() { return 5; }
    final int emit2_SIB_DISP8_Imm8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, byte off, byte imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, DISP8_SIBToString(base, ind, scale, off), Imm8ToString(imm));
        return 5;
    }
    public static final int s_2_SIB_DISP8_SEImm8() { return 5; }
    final int emit2_SIB_DISP8_SEImm8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, byte off, byte imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_DISP8 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, DISP8_SIBToString(base, ind, scale, off), SEImm8ToString(imm));
        return 5;
    }
    public static final int s_2_SIB_DISP8_Imm32() { return 8; }
    final int emit2_SIB_DISP8_Imm32(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, byte off, int imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP8 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 8, desc, DISP8_SIBToString(base, ind, scale, off), Imm32ToString(imm));
        return 8;
    }
    public static final int s_2_DISP32() { return 6; }
    final int emit2_DISP32(CodeAllocator.x86CodeBuffer mc, int off, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | base);
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, DISP32ToString(off, base));
        return 6;
    }
    public static final int s_2_Once_DISP32() { return 6; }
    final int emit2_Once_DISP32(CodeAllocator.x86CodeBuffer mc, int off, int base) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_DISP32 | base);
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, DISP32ToString(off, base), "1");
        return 6;
    }
    public static final int s_2_DISP32_SEImm8() { return 7; }
    final int emit2_DISP32_SEImm8(CodeAllocator.x86CodeBuffer mc, int off, int base, byte imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_DISP32 | base);
            mc.add4_endian(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, DISP32ToString(off, base), SEImm8ToString(imm));
        return 7;
    }
    public static final int s_2_DISP32_Imm8() { return 7; }
    final int emit2_DISP32_Imm8(CodeAllocator.x86CodeBuffer mc, int off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | base);
            mc.add4_endian(off);
            mc.add1((byte)imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, DISP32ToString(off, base), Imm8ToString(imm));
        return 7;
    }
    public static final int s_2_DISP32_Imm32() { return 10; }
    final int emit2_DISP32_Imm32(CodeAllocator.x86CodeBuffer mc, int off, int base, int imm) {
        Assert._assert(length == 2);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xC7) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | base);
            mc.add4_endian(off);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 10, desc, DISP32ToString(off, base), Imm32ToString(imm));
        return 10;
    }
    public static final int s_2_SIB_DISP32() { return 7; }
    final int emit2_SIB_DISP32(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int off) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, DISP32_SIBToString(base, ind, scale, off));
        return 7;
    }
    public static final int s_2_Once_SIB_DISP32() { return 7; }
    final int emit2_Once_SIB_DISP32(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int off) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | SHIFT_ONCE | MOD_DISP32 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, DISP32_SIBToString(base, ind, scale, off), "1");
        return 7;
    }
    public static final int s_2_SIB_DISP32_SEImm8() { return 8; }
    final int emit2_SIB_DISP32_SEImm8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int off, byte imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | SEIMM8 | MOD_DISP32 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 8, desc, DISP32_SIBToString(base, ind, scale, off), SEImm8ToString(imm));
        return 8;
    }
    public static final int s_2_SIB_DISP32_Imm8() { return 8; }
    final int emit2_SIB_DISP32_Imm8(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int off, byte imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
            mc.add1(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 8, desc, DISP32_SIBToString(base, ind, scale, off), Imm8ToString(imm));
        return 8;
    }
    public static final int s_2_SIB_DISP32_Imm32() { return 11; }
    final int emit2_SIB_DISP32_Imm32(CodeAllocator.x86CodeBuffer mc, int base, int ind, int scale, int off, int imm) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(base >= EAX && base <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        if (mc != null) {
            mc.add2(opcode | MOD_DISP32 | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
            mc.add4_endian(imm);
        }
        if (x86Assembler.TRACE) dbg(mc, 11, desc, DISP32_SIBToString(base, ind, scale, off), Imm32ToString(imm));
        return 11;
    }
    public static final int s_2_Abs32() { return 6; }
    final int emit2_Abs32(CodeAllocator.x86CodeBuffer mc, int addr) {
        Assert._assert(length == 2);
        Assert._assert((opcode & 0xC7) == 0);
        if (mc != null) {
            mc.add2(opcode | MOD_EA | EBP);
            mc.add4_endian(addr);
        }
        if (x86Assembler.TRACE) dbg(mc, 6, desc, Abs32ToString(addr));
        return 6;
    }
    public static final int s_3_Reg_Reg() { return 3; }
    final int emit3_Reg_Reg(CodeAllocator.x86CodeBuffer mc, int r1, int r2) {
        Assert._assert(length == 3);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(r2 >= EAX && r2 <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_REG | (r1 << 3) | r2);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, RegToString(r1), RegToString(r2));
        return 3;
    }
    public static final int s_3_Reg_EA() { return 3; }
    final int emit3_Reg_EA(CodeAllocator.x86CodeBuffer mc, int r1, int base) {
        Assert._assert(length == 3);
        Assert._assert(base != ESP);
        Assert._assert(base != EBP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_EA | (r1 << 3) | base);
        }
        if (x86Assembler.TRACE) dbg(mc, 3, desc, RegToString(r1), EAToString(base));
        return 3;
    }
    public static final int s_3_Reg_DISP8() { return 4; }
    final int emit3_Reg_DISP8(CodeAllocator.x86CodeBuffer mc, int r1, byte off, int base) {
        Assert._assert(length == 3);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_DISP8 | (r1 << 3) | base);
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, RegToString(r1), DISP8ToString(off, base));
        return 4;
    }
    public static final int s_3_Reg_DISP32() { return 7; }
    final int emit3_Reg_DISP32(CodeAllocator.x86CodeBuffer mc, int r1, int off, int base) {
        Assert._assert(length == 3);
        Assert._assert(base != ESP);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_DISP32 | (r1 << 3) | base);
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, RegToString(r1), DISP32ToString(off, base));
        return 7;
    }
    public static final int s_3_Reg_SIB_EA() { return 4; }
    final int emit3_Reg_SIB_EA(CodeAllocator.x86CodeBuffer mc, int r1, int base, int ind, int scale) {
        Assert._assert(length == 3);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_EA | (r1 << 3) | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
        }
        if (x86Assembler.TRACE) dbg(mc, 4, desc, RegToString(r1), EA_SIBToString(base, ind, scale));
        return 4;
    }
    public static final int s_3_Reg_SIB_DISP8() { return 5; }
    final int emit3_Reg_SIB_DISP8(CodeAllocator.x86CodeBuffer mc, int r1, int base, int ind, int scale, byte off) {
        Assert._assert(length == 3);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_DISP8 | (r1 << 3) | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add1(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 5, desc, RegToString(r1), DISP8_SIBToString(base, ind, scale, off));
        return 5;
    }
    public static final int s_3_Reg_SIB_DISP32() { return 8; }
    final int emit3_Reg_SIB_DISP32(CodeAllocator.x86CodeBuffer mc, int r1, int base, int ind, int scale, int off) {
        Assert._assert(length == 3);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        Assert._assert(ind >= EAX && ind <= EDI);
        Assert._assert(base >= EAX && base <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_DISP32 | (r1 << 3) | RM_SIB);
            mc.add1((byte)(scale | (ind << 3) | base));
            mc.add4_endian(off);
        }
        if (x86Assembler.TRACE) dbg(mc, 8, desc, RegToString(r1), DISP32_SIBToString(base, ind, scale, off));
        return 8;
    }
    public static final int s_3_Reg_Abs32() { return 7; }
    final int emit3_Reg_Abs32(CodeAllocator.x86CodeBuffer mc, int r1, int addr) {
        Assert._assert(length == 3);
        Assert._assert((opcode & 0xFF) == 0);
        Assert._assert(r1 >= EAX && r1 <= EDI);
        if (mc != null) {
            mc.add3(opcode | MOD_EA | (r1 << 3) | EBP);
            mc.add4_endian(addr);
        }
        if (x86Assembler.TRACE) dbg(mc, 7, desc, RegToString(r1), Abs32ToString(addr));
        return 7;
    }

    static String RegToString(int reg) {
        switch (reg) {
        case EAX: return "EAX";
        case EBX: return "EBX";
        case ECX: return "ECX";
        case EDX: return "EDX";
        case ESP: return "ESP";
        case EBP: return "EBP";
        case ESI: return "ESI";
        case EDI: return "EDI";
        default: throw new InternalError();
        }
    }

    static String FPRegToString(int r) {
        return "ST("+r+")";
    }
    
    static String EAToString(int base) {
        Assert._assert(base != EBP);
        Assert._assert(base != ESP);
        return "("+RegToString(base)+")";
    }

    static String DISP8ToString(byte off, int base) {
        Assert._assert(base != ESP);
        return off+"("+RegToString(base)+")";
    }

    static String DISP32ToString(int off, int base) {
        Assert._assert(base != ESP);
        return off+"("+RegToString(base)+")";
    }

    static String EA_SIBToString(int base, int ind, int scale) {
        if (ind == ESP)
            return "("+RegToString(base)+")";
        else
            return "("+RegToString(base)+"+("+RegToString(ind)+ScaleToString(scale)+") )";
    }

    static String DISP8_SIBToString(int base, int ind, int scale, byte off) {
        if (ind == ESP)
            return "("+RegToString(base)+"+"+off+")";
        else
            return "("+RegToString(base)+"+("+RegToString(ind)+ScaleToString(scale)+"+"+off+") )";
    }

    static String DISP32_SIBToString(int base, int ind, int scale, int off) {
        if (ind == ESP)
            return "("+RegToString(base)+"+"+off+")";
        else
            return "("+RegToString(base)+"+("+RegToString(ind)+ScaleToString(scale)+"+"+off+") )";
    }

    static String ScaleToString(int scale) {
        switch (scale) {
        case SCALE_1: return "*1";
        case SCALE_2: return "*2";
        case SCALE_4: return "*4";
        case SCALE_8: return "*8";
        default: throw new InternalError();
        }
    }

    static String SEImm8ToString(byte imm) {
        return "seimm8:"+imm;
    }

    static String Imm8ToString(int imm) {
        return "imm8:"+imm;
    }

    static String Imm16ToString(char imm) {
        return "imm16:"+(int)imm;
    }
    
    static String Imm32ToString(int imm) {
        return "imm32:"+imm;
    }

    static String Abs32ToString(int imm) {
        return "abs32:"+imm;
    }

    StringBuffer _dbg(CodeAllocator.x86CodeBuffer mc, int length, String opc) {
        StringBuffer s = new StringBuffer();
        if (mc != null) {
            int n = mc.getCurrentOffset();
            s.append(Strings.hex(n - length));
            s.append('\t');
            while (length > 0) {
                int b = ((int)mc.get1(n-length)) & 0xFF;
                --length;
                String str = Integer.toHexString(b);
                if (b <= 0x0f) s.append('0');
                s.append(str);
            }
        }
        s.append('\t');
        s.append(opc);
        return s;
    }

    void dbg(CodeAllocator.x86CodeBuffer mc, int length, String opc) {
        StringBuffer s = _dbg(mc, length, opc);
        System.out.println(s.toString());
    }

    void dbg(CodeAllocator.x86CodeBuffer mc, int length, String opc, String p1) {
        StringBuffer s = _dbg(mc, length, opc);
        s.append('\t'); s.append(p1);
        System.out.println(s.toString());
    }

    void dbg(CodeAllocator.x86CodeBuffer mc, int length, String opc, String p1, String p2) {
        StringBuffer s = _dbg(mc, length, opc);
        s.append('\t'); s.append(p1);
        s.append('\t'); s.append(p2);
        System.out.println(s.toString());
    }

    void dbg(CodeAllocator.x86CodeBuffer mc, int length, String opc, String p1, String p2, String p3) {
        StringBuffer s = _dbg(mc, length, opc);
        s.append('\t'); s.append(p1);
        s.append('\t'); s.append(p2);
        s.append('\t'); s.append(p3);
        System.out.println(s.toString());
    }

    private static x86 _op(int opcode,
                           int length,
                           int pairing,
                           int uops,
                           String desc) {
        x86 o = new x86();
        o.opcode = opcode;
        o.length = length;
        o.pairing = pairing;
        o.microops = uops;
        o.desc = desc;
        return o;
    }

    private static x86 _sop(int opcode,
                            int length,
                            int pairing,
                            int uops,
                            String desc) {
        x86 o = new x86();
        o.opcode = opcode;
        o.length = length;
        o.pairing = pairing;
        o.microops = uops;
        o.desc = desc;
        return o;
    }

}
