/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */

/*
 * THIS FILE IS MACHINE GENERATED. DO NOT EDIT.
 * The input files are:
 *  ArchOperators.template
 *  /Users/joe/git/JOE/rvm/src-generated/opt-ir/ppc/OperatorList.dat
 */

package org.jikesrvm.compilers.opt.ir.ppc;

import org.jikesrvm.compilers.opt.ir.Operator;
import org.jikesrvm.compilers.opt.ir.Operators;

/**
 * Provides architectural literal operators.
 *
 * @see Operator
 */
public final class ArchOperators extends Operators {

  /* Architecture dependent operator codes */

  /** Opcode identifier for DCBF instructions */
  public static final char DCBF_opcode =
    (char)(0 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DCBST instructions */
  public static final char DCBST_opcode =
    (char)(1 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DCBT instructions */
  public static final char DCBT_opcode =
    (char)(2 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DCBTST instructions */
  public static final char DCBTST_opcode =
    (char)(3 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DCBZ instructions */
  public static final char DCBZ_opcode =
    (char)(4 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DCBZL instructions */
  public static final char DCBZL_opcode =
    (char)(5 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for ICBI instructions */
  public static final char ICBI_opcode =
    (char)(6 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for CALL_SAVE_VOLATILE instructions */
  public static final char CALL_SAVE_VOLATILE_opcode =
    (char)(7 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for MIR_START instructions */
  public static final char MIR_START_opcode =
    (char)(8 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for MIR_LOWTABLESWITCH instructions */
  public static final char MIR_LOWTABLESWITCH_opcode =
    (char)(9 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DATA_INT instructions */
  public static final char PPC_DATA_INT_opcode =
    (char)(10 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DATA_LABEL instructions */
  public static final char PPC_DATA_LABEL_opcode =
    (char)(11 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADD instructions */
  public static final char PPC_ADD_opcode =
    (char)(12 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDr instructions */
  public static final char PPC_ADDr_opcode =
    (char)(13 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDC instructions */
  public static final char PPC_ADDC_opcode =
    (char)(14 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDE instructions */
  public static final char PPC_ADDE_opcode =
    (char)(15 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDZE instructions */
  public static final char PPC_ADDZE_opcode =
    (char)(16 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDME instructions */
  public static final char PPC_ADDME_opcode =
    (char)(17 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDIC instructions */
  public static final char PPC_ADDIC_opcode =
    (char)(18 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDICr instructions */
  public static final char PPC_ADDICr_opcode =
    (char)(19 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBF instructions */
  public static final char PPC_SUBF_opcode =
    (char)(20 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFr instructions */
  public static final char PPC_SUBFr_opcode =
    (char)(21 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFC instructions */
  public static final char PPC_SUBFC_opcode =
    (char)(22 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFCr instructions */
  public static final char PPC_SUBFCr_opcode =
    (char)(23 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFIC instructions */
  public static final char PPC_SUBFIC_opcode =
    (char)(24 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFE instructions */
  public static final char PPC_SUBFE_opcode =
    (char)(25 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFZE instructions */
  public static final char PPC_SUBFZE_opcode =
    (char)(26 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SUBFME instructions */
  public static final char PPC_SUBFME_opcode =
    (char)(27 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_AND instructions */
  public static final char PPC_AND_opcode =
    (char)(28 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ANDr instructions */
  public static final char PPC_ANDr_opcode =
    (char)(29 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ANDIr instructions */
  public static final char PPC_ANDIr_opcode =
    (char)(30 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ANDISr instructions */
  public static final char PPC_ANDISr_opcode =
    (char)(31 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_NAND instructions */
  public static final char PPC_NAND_opcode =
    (char)(32 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_NANDr instructions */
  public static final char PPC_NANDr_opcode =
    (char)(33 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ANDC instructions */
  public static final char PPC_ANDC_opcode =
    (char)(34 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ANDCr instructions */
  public static final char PPC_ANDCr_opcode =
    (char)(35 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_OR instructions */
  public static final char PPC_OR_opcode =
    (char)(36 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ORr instructions */
  public static final char PPC_ORr_opcode =
    (char)(37 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MOVE instructions */
  public static final char PPC_MOVE_opcode =
    (char)(38 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ORI instructions */
  public static final char PPC_ORI_opcode =
    (char)(39 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ORIS instructions */
  public static final char PPC_ORIS_opcode =
    (char)(40 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_NOR instructions */
  public static final char PPC_NOR_opcode =
    (char)(41 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_NORr instructions */
  public static final char PPC_NORr_opcode =
    (char)(42 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ORC instructions */
  public static final char PPC_ORC_opcode =
    (char)(43 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ORCr instructions */
  public static final char PPC_ORCr_opcode =
    (char)(44 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_XOR instructions */
  public static final char PPC_XOR_opcode =
    (char)(45 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_XORr instructions */
  public static final char PPC_XORr_opcode =
    (char)(46 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_XORI instructions */
  public static final char PPC_XORI_opcode =
    (char)(47 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_XORIS instructions */
  public static final char PPC_XORIS_opcode =
    (char)(48 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_EQV instructions */
  public static final char PPC_EQV_opcode =
    (char)(49 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_EQVr instructions */
  public static final char PPC_EQVr_opcode =
    (char)(50 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_NEG instructions */
  public static final char PPC_NEG_opcode =
    (char)(51 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_NEGr instructions */
  public static final char PPC_NEGr_opcode =
    (char)(52 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CNTLZW instructions */
  public static final char PPC_CNTLZW_opcode =
    (char)(53 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_EXTSB instructions */
  public static final char PPC_EXTSB_opcode =
    (char)(54 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_EXTSBr instructions */
  public static final char PPC_EXTSBr_opcode =
    (char)(55 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_EXTSH instructions */
  public static final char PPC_EXTSH_opcode =
    (char)(56 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_EXTSHr instructions */
  public static final char PPC_EXTSHr_opcode =
    (char)(57 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SLW instructions */
  public static final char PPC_SLW_opcode =
    (char)(58 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SLWr instructions */
  public static final char PPC_SLWr_opcode =
    (char)(59 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SLWI instructions */
  public static final char PPC_SLWI_opcode =
    (char)(60 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SLWIr instructions */
  public static final char PPC_SLWIr_opcode =
    (char)(61 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRW instructions */
  public static final char PPC_SRW_opcode =
    (char)(62 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRWr instructions */
  public static final char PPC_SRWr_opcode =
    (char)(63 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRWI instructions */
  public static final char PPC_SRWI_opcode =
    (char)(64 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRWIr instructions */
  public static final char PPC_SRWIr_opcode =
    (char)(65 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRAW instructions */
  public static final char PPC_SRAW_opcode =
    (char)(66 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRAWr instructions */
  public static final char PPC_SRAWr_opcode =
    (char)(67 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRAWI instructions */
  public static final char PPC_SRAWI_opcode =
    (char)(68 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRAWIr instructions */
  public static final char PPC_SRAWIr_opcode =
    (char)(69 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_RLWINM instructions */
  public static final char PPC_RLWINM_opcode =
    (char)(70 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_RLWINMr instructions */
  public static final char PPC_RLWINMr_opcode =
    (char)(71 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_RLWIMI instructions */
  public static final char PPC_RLWIMI_opcode =
    (char)(72 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_RLWIMIr instructions */
  public static final char PPC_RLWIMIr_opcode =
    (char)(73 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_RLWNM instructions */
  public static final char PPC_RLWNM_opcode =
    (char)(74 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_RLWNMr instructions */
  public static final char PPC_RLWNMr_opcode =
    (char)(75 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_B instructions */
  public static final char PPC_B_opcode =
    (char)(76 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BL instructions */
  public static final char PPC_BL_opcode =
    (char)(77 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BL_SYS instructions */
  public static final char PPC_BL_SYS_opcode =
    (char)(78 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BLR instructions */
  public static final char PPC_BLR_opcode =
    (char)(79 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCTR instructions */
  public static final char PPC_BCTR_opcode =
    (char)(80 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCTRL instructions */
  public static final char PPC_BCTRL_opcode =
    (char)(81 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCTRL_SYS instructions */
  public static final char PPC_BCTRL_SYS_opcode =
    (char)(82 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCLR instructions */
  public static final char PPC_BCLR_opcode =
    (char)(83 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BLRL instructions */
  public static final char PPC_BLRL_opcode =
    (char)(84 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCLRL instructions */
  public static final char PPC_BCLRL_opcode =
    (char)(85 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BC instructions */
  public static final char PPC_BC_opcode =
    (char)(86 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCL instructions */
  public static final char PPC_BCL_opcode =
    (char)(87 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCOND instructions */
  public static final char PPC_BCOND_opcode =
    (char)(88 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCOND2 instructions */
  public static final char PPC_BCOND2_opcode =
    (char)(89 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCCTR instructions */
  public static final char PPC_BCCTR_opcode =
    (char)(90 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_BCC instructions */
  public static final char PPC_BCC_opcode =
    (char)(91 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDI instructions */
  public static final char PPC_ADDI_opcode =
    (char)(92 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ADDIS instructions */
  public static final char PPC_ADDIS_opcode =
    (char)(93 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LDI instructions */
  public static final char PPC_LDI_opcode =
    (char)(94 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LDIS instructions */
  public static final char PPC_LDIS_opcode =
    (char)(95 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CMP instructions */
  public static final char PPC_CMP_opcode =
    (char)(96 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CMPI instructions */
  public static final char PPC_CMPI_opcode =
    (char)(97 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CMPL instructions */
  public static final char PPC_CMPL_opcode =
    (char)(98 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CMPLI instructions */
  public static final char PPC_CMPLI_opcode =
    (char)(99 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CRAND instructions */
  public static final char PPC_CRAND_opcode =
    (char)(100 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CRANDC instructions */
  public static final char PPC_CRANDC_opcode =
    (char)(101 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CROR instructions */
  public static final char PPC_CROR_opcode =
    (char)(102 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CRORC instructions */
  public static final char PPC_CRORC_opcode =
    (char)(103 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMR instructions */
  public static final char PPC_FMR_opcode =
    (char)(104 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FRSP instructions */
  public static final char PPC_FRSP_opcode =
    (char)(105 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FCTIW instructions */
  public static final char PPC_FCTIW_opcode =
    (char)(106 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FCTIWZ instructions */
  public static final char PPC_FCTIWZ_opcode =
    (char)(107 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FADD instructions */
  public static final char PPC_FADD_opcode =
    (char)(108 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FADDS instructions */
  public static final char PPC_FADDS_opcode =
    (char)(109 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FSQRT instructions */
  public static final char PPC_FSQRT_opcode =
    (char)(110 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FSQRTS instructions */
  public static final char PPC_FSQRTS_opcode =
    (char)(111 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FABS instructions */
  public static final char PPC_FABS_opcode =
    (char)(112 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FCMPO instructions */
  public static final char PPC_FCMPO_opcode =
    (char)(113 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FCMPU instructions */
  public static final char PPC_FCMPU_opcode =
    (char)(114 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FDIV instructions */
  public static final char PPC_FDIV_opcode =
    (char)(115 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FDIVS instructions */
  public static final char PPC_FDIVS_opcode =
    (char)(116 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DIVW instructions */
  public static final char PPC_DIVW_opcode =
    (char)(117 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DIVWU instructions */
  public static final char PPC_DIVWU_opcode =
    (char)(118 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMUL instructions */
  public static final char PPC_FMUL_opcode =
    (char)(119 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMULS instructions */
  public static final char PPC_FMULS_opcode =
    (char)(120 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FSEL instructions */
  public static final char PPC_FSEL_opcode =
    (char)(121 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMADD instructions */
  public static final char PPC_FMADD_opcode =
    (char)(122 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMADDS instructions */
  public static final char PPC_FMADDS_opcode =
    (char)(123 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMSUB instructions */
  public static final char PPC_FMSUB_opcode =
    (char)(124 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FMSUBS instructions */
  public static final char PPC_FMSUBS_opcode =
    (char)(125 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FNMADD instructions */
  public static final char PPC_FNMADD_opcode =
    (char)(126 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FNMADDS instructions */
  public static final char PPC_FNMADDS_opcode =
    (char)(127 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FNMSUB instructions */
  public static final char PPC_FNMSUB_opcode =
    (char)(128 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FNMSUBS instructions */
  public static final char PPC_FNMSUBS_opcode =
    (char)(129 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MULLI instructions */
  public static final char PPC_MULLI_opcode =
    (char)(130 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MULLW instructions */
  public static final char PPC_MULLW_opcode =
    (char)(131 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MULHW instructions */
  public static final char PPC_MULHW_opcode =
    (char)(132 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MULHWU instructions */
  public static final char PPC_MULHWU_opcode =
    (char)(133 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FNEG instructions */
  public static final char PPC_FNEG_opcode =
    (char)(134 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FSUB instructions */
  public static final char PPC_FSUB_opcode =
    (char)(135 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_FSUBS instructions */
  public static final char PPC_FSUBS_opcode =
    (char)(136 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LWZ instructions */
  public static final char PPC_LWZ_opcode =
    (char)(137 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LWZU instructions */
  public static final char PPC_LWZU_opcode =
    (char)(138 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LWZUX instructions */
  public static final char PPC_LWZUX_opcode =
    (char)(139 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LWZX instructions */
  public static final char PPC_LWZX_opcode =
    (char)(140 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LWARX instructions */
  public static final char PPC_LWARX_opcode =
    (char)(141 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LBZ instructions */
  public static final char PPC_LBZ_opcode =
    (char)(142 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LBZUX instructions */
  public static final char PPC_LBZUX_opcode =
    (char)(143 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LBZX instructions */
  public static final char PPC_LBZX_opcode =
    (char)(144 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LHA instructions */
  public static final char PPC_LHA_opcode =
    (char)(145 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LHAX instructions */
  public static final char PPC_LHAX_opcode =
    (char)(146 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LHZ instructions */
  public static final char PPC_LHZ_opcode =
    (char)(147 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LHZX instructions */
  public static final char PPC_LHZX_opcode =
    (char)(148 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LFD instructions */
  public static final char PPC_LFD_opcode =
    (char)(149 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LFDX instructions */
  public static final char PPC_LFDX_opcode =
    (char)(150 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LFS instructions */
  public static final char PPC_LFS_opcode =
    (char)(151 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LFSX instructions */
  public static final char PPC_LFSX_opcode =
    (char)(152 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LMW instructions */
  public static final char PPC_LMW_opcode =
    (char)(153 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STW instructions */
  public static final char PPC_STW_opcode =
    (char)(154 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STWX instructions */
  public static final char PPC_STWX_opcode =
    (char)(155 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STWCXr instructions */
  public static final char PPC_STWCXr_opcode =
    (char)(156 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STWU instructions */
  public static final char PPC_STWU_opcode =
    (char)(157 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STB instructions */
  public static final char PPC_STB_opcode =
    (char)(158 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STBX instructions */
  public static final char PPC_STBX_opcode =
    (char)(159 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STH instructions */
  public static final char PPC_STH_opcode =
    (char)(160 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STHX instructions */
  public static final char PPC_STHX_opcode =
    (char)(161 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STFD instructions */
  public static final char PPC_STFD_opcode =
    (char)(162 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STFDX instructions */
  public static final char PPC_STFDX_opcode =
    (char)(163 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STFDU instructions */
  public static final char PPC_STFDU_opcode =
    (char)(164 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STFS instructions */
  public static final char PPC_STFS_opcode =
    (char)(165 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STFSX instructions */
  public static final char PPC_STFSX_opcode =
    (char)(166 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STFSU instructions */
  public static final char PPC_STFSU_opcode =
    (char)(167 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STMW instructions */
  public static final char PPC_STMW_opcode =
    (char)(168 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_TW instructions */
  public static final char PPC_TW_opcode =
    (char)(169 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_TWI instructions */
  public static final char PPC_TWI_opcode =
    (char)(170 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MFSPR instructions */
  public static final char PPC_MFSPR_opcode =
    (char)(171 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MTSPR instructions */
  public static final char PPC_MTSPR_opcode =
    (char)(172 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MFTB instructions */
  public static final char PPC_MFTB_opcode =
    (char)(173 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_MFTBU instructions */
  public static final char PPC_MFTBU_opcode =
    (char)(174 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_HWSYNC instructions */
  public static final char PPC_HWSYNC_opcode =
    (char)(175 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SYNC instructions */
  public static final char PPC_SYNC_opcode =
    (char)(176 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ISYNC instructions */
  public static final char PPC_ISYNC_opcode =
    (char)(177 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DCBF instructions */
  public static final char PPC_DCBF_opcode =
    (char)(178 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DCBST instructions */
  public static final char PPC_DCBST_opcode =
    (char)(179 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DCBT instructions */
  public static final char PPC_DCBT_opcode =
    (char)(180 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DCBTST instructions */
  public static final char PPC_DCBTST_opcode =
    (char)(181 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DCBZ instructions */
  public static final char PPC_DCBZ_opcode =
    (char)(182 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_DCBZL instructions */
  public static final char PPC_DCBZL_opcode =
    (char)(183 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ICBI instructions */
  public static final char PPC_ICBI_opcode =
    (char)(184 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_EXTSW instructions */
  public static final char PPC64_EXTSW_opcode =
    (char)(185 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_EXTSWr instructions */
  public static final char PPC64_EXTSWr_opcode =
    (char)(186 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_EXTZW instructions */
  public static final char PPC64_EXTZW_opcode =
    (char)(187 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_RLDICL instructions */
  public static final char PPC64_RLDICL_opcode =
    (char)(188 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_RLDICR instructions */
  public static final char PPC64_RLDICR_opcode =
    (char)(189 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SLD instructions */
  public static final char PPC64_SLD_opcode =
    (char)(190 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SLDr instructions */
  public static final char PPC64_SLDr_opcode =
    (char)(191 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SLDI instructions */
  public static final char PPC64_SLDI_opcode =
    (char)(192 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRD instructions */
  public static final char PPC64_SRD_opcode =
    (char)(193 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRDr instructions */
  public static final char PPC64_SRDr_opcode =
    (char)(194 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRAD instructions */
  public static final char PPC64_SRAD_opcode =
    (char)(195 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRADr instructions */
  public static final char PPC64_SRADr_opcode =
    (char)(196 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRADI instructions */
  public static final char PPC64_SRADI_opcode =
    (char)(197 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRADIr instructions */
  public static final char PPC64_SRADIr_opcode =
    (char)(198 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_SRDI instructions */
  public static final char PPC64_SRDI_opcode =
    (char)(199 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_RLDIMI instructions */
  public static final char PPC64_RLDIMI_opcode =
    (char)(200 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_RLDIMIr instructions */
  public static final char PPC64_RLDIMIr_opcode =
    (char)(201 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_CMP instructions */
  public static final char PPC64_CMP_opcode =
    (char)(202 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_CMPI instructions */
  public static final char PPC64_CMPI_opcode =
    (char)(203 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_CMPL instructions */
  public static final char PPC64_CMPL_opcode =
    (char)(204 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_CMPLI instructions */
  public static final char PPC64_CMPLI_opcode =
    (char)(205 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_FCFID instructions */
  public static final char PPC64_FCFID_opcode =
    (char)(206 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_FCTIDZ instructions */
  public static final char PPC64_FCTIDZ_opcode =
    (char)(207 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_DIVD instructions */
  public static final char PPC64_DIVD_opcode =
    (char)(208 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_MULLD instructions */
  public static final char PPC64_MULLD_opcode =
    (char)(209 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_LD instructions */
  public static final char PPC64_LD_opcode =
    (char)(210 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_LDX instructions */
  public static final char PPC64_LDX_opcode =
    (char)(211 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_STD instructions */
  public static final char PPC64_STD_opcode =
    (char)(212 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_STDX instructions */
  public static final char PPC64_STDX_opcode =
    (char)(213 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_TD instructions */
  public static final char PPC64_TD_opcode =
    (char)(214 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_TDI instructions */
  public static final char PPC64_TDI_opcode =
    (char)(215 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_CNTLZAddr instructions */
  public static final char PPC_CNTLZAddr_opcode =
    (char)(216 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRAAddrI instructions */
  public static final char PPC_SRAAddrI_opcode =
    (char)(217 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_SRAddrI instructions */
  public static final char PPC_SRAddrI_opcode =
    (char)(218 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_LWA instructions */
  public static final char PPC64_LWA_opcode =
    (char)(219 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LInt instructions */
  public static final char PPC_LInt_opcode =
    (char)(220 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC64_LWAX instructions */
  public static final char PPC64_LWAX_opcode =
    (char)(221 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LIntX instructions */
  public static final char PPC_LIntX_opcode =
    (char)(222 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LIntUX instructions */
  public static final char PPC_LIntUX_opcode =
    (char)(223 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LAddr instructions */
  public static final char PPC_LAddr_opcode =
    (char)(224 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LAddrX instructions */
  public static final char PPC_LAddrX_opcode =
    (char)(225 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LAddrU instructions */
  public static final char PPC_LAddrU_opcode =
    (char)(226 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LAddrUX instructions */
  public static final char PPC_LAddrUX_opcode =
    (char)(227 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_LAddrARX instructions */
  public static final char PPC_LAddrARX_opcode =
    (char)(228 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STAddr instructions */
  public static final char PPC_STAddr_opcode =
    (char)(229 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STAddrX instructions */
  public static final char PPC_STAddrX_opcode =
    (char)(230 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STAddrU instructions */
  public static final char PPC_STAddrU_opcode =
    (char)(231 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STAddrUX instructions */
  public static final char PPC_STAddrUX_opcode =
    (char)(232 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_STAddrCXr instructions */
  public static final char PPC_STAddrCXr_opcode =
    (char)(233 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_TAddr instructions */
  public static final char PPC_TAddr_opcode =
    (char)(234 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PPC_ILLEGAL_INSTRUCTION instructions */
  public static final char PPC_ILLEGAL_INSTRUCTION_opcode =
    (char)(235 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for MIR_END instructions */
  public static final char MIR_END_opcode =
    (char)(236 + ARCH_INDEPENDENT_END_opcode);

  /* Architecture dependent operators */

  /** Operator for DCBF instructions */
  public static final Operator DCBF =
    Operator.lookupOpcode(0+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DCBST instructions */
  public static final Operator DCBST =
    Operator.lookupOpcode(1+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DCBT instructions */
  public static final Operator DCBT =
    Operator.lookupOpcode(2+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DCBTST instructions */
  public static final Operator DCBTST =
    Operator.lookupOpcode(3+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DCBZ instructions */
  public static final Operator DCBZ =
    Operator.lookupOpcode(4+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DCBZL instructions */
  public static final Operator DCBZL =
    Operator.lookupOpcode(5+ARCH_INDEPENDENT_END_opcode);
  /** Operator for ICBI instructions */
  public static final Operator ICBI =
    Operator.lookupOpcode(6+ARCH_INDEPENDENT_END_opcode);
  /** Operator for CALL_SAVE_VOLATILE instructions */
  public static final Operator CALL_SAVE_VOLATILE =
    Operator.lookupOpcode(7+ARCH_INDEPENDENT_END_opcode);
  /** Operator for MIR_START instructions */
  public static final Operator MIR_START =
    Operator.lookupOpcode(8+ARCH_INDEPENDENT_END_opcode);
  /** Operator for MIR_LOWTABLESWITCH instructions */
  public static final Operator MIR_LOWTABLESWITCH =
    Operator.lookupOpcode(9+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DATA_INT instructions */
  public static final Operator PPC_DATA_INT =
    Operator.lookupOpcode(10+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DATA_LABEL instructions */
  public static final Operator PPC_DATA_LABEL =
    Operator.lookupOpcode(11+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADD instructions */
  public static final Operator PPC_ADD =
    Operator.lookupOpcode(12+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDr instructions */
  public static final Operator PPC_ADDr =
    Operator.lookupOpcode(13+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDC instructions */
  public static final Operator PPC_ADDC =
    Operator.lookupOpcode(14+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDE instructions */
  public static final Operator PPC_ADDE =
    Operator.lookupOpcode(15+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDZE instructions */
  public static final Operator PPC_ADDZE =
    Operator.lookupOpcode(16+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDME instructions */
  public static final Operator PPC_ADDME =
    Operator.lookupOpcode(17+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDIC instructions */
  public static final Operator PPC_ADDIC =
    Operator.lookupOpcode(18+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDICr instructions */
  public static final Operator PPC_ADDICr =
    Operator.lookupOpcode(19+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBF instructions */
  public static final Operator PPC_SUBF =
    Operator.lookupOpcode(20+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFr instructions */
  public static final Operator PPC_SUBFr =
    Operator.lookupOpcode(21+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFC instructions */
  public static final Operator PPC_SUBFC =
    Operator.lookupOpcode(22+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFCr instructions */
  public static final Operator PPC_SUBFCr =
    Operator.lookupOpcode(23+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFIC instructions */
  public static final Operator PPC_SUBFIC =
    Operator.lookupOpcode(24+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFE instructions */
  public static final Operator PPC_SUBFE =
    Operator.lookupOpcode(25+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFZE instructions */
  public static final Operator PPC_SUBFZE =
    Operator.lookupOpcode(26+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SUBFME instructions */
  public static final Operator PPC_SUBFME =
    Operator.lookupOpcode(27+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_AND instructions */
  public static final Operator PPC_AND =
    Operator.lookupOpcode(28+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ANDr instructions */
  public static final Operator PPC_ANDr =
    Operator.lookupOpcode(29+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ANDIr instructions */
  public static final Operator PPC_ANDIr =
    Operator.lookupOpcode(30+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ANDISr instructions */
  public static final Operator PPC_ANDISr =
    Operator.lookupOpcode(31+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_NAND instructions */
  public static final Operator PPC_NAND =
    Operator.lookupOpcode(32+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_NANDr instructions */
  public static final Operator PPC_NANDr =
    Operator.lookupOpcode(33+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ANDC instructions */
  public static final Operator PPC_ANDC =
    Operator.lookupOpcode(34+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ANDCr instructions */
  public static final Operator PPC_ANDCr =
    Operator.lookupOpcode(35+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_OR instructions */
  public static final Operator PPC_OR =
    Operator.lookupOpcode(36+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ORr instructions */
  public static final Operator PPC_ORr =
    Operator.lookupOpcode(37+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MOVE instructions */
  public static final Operator PPC_MOVE =
    Operator.lookupOpcode(38+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ORI instructions */
  public static final Operator PPC_ORI =
    Operator.lookupOpcode(39+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ORIS instructions */
  public static final Operator PPC_ORIS =
    Operator.lookupOpcode(40+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_NOR instructions */
  public static final Operator PPC_NOR =
    Operator.lookupOpcode(41+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_NORr instructions */
  public static final Operator PPC_NORr =
    Operator.lookupOpcode(42+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ORC instructions */
  public static final Operator PPC_ORC =
    Operator.lookupOpcode(43+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ORCr instructions */
  public static final Operator PPC_ORCr =
    Operator.lookupOpcode(44+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_XOR instructions */
  public static final Operator PPC_XOR =
    Operator.lookupOpcode(45+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_XORr instructions */
  public static final Operator PPC_XORr =
    Operator.lookupOpcode(46+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_XORI instructions */
  public static final Operator PPC_XORI =
    Operator.lookupOpcode(47+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_XORIS instructions */
  public static final Operator PPC_XORIS =
    Operator.lookupOpcode(48+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_EQV instructions */
  public static final Operator PPC_EQV =
    Operator.lookupOpcode(49+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_EQVr instructions */
  public static final Operator PPC_EQVr =
    Operator.lookupOpcode(50+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_NEG instructions */
  public static final Operator PPC_NEG =
    Operator.lookupOpcode(51+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_NEGr instructions */
  public static final Operator PPC_NEGr =
    Operator.lookupOpcode(52+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CNTLZW instructions */
  public static final Operator PPC_CNTLZW =
    Operator.lookupOpcode(53+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_EXTSB instructions */
  public static final Operator PPC_EXTSB =
    Operator.lookupOpcode(54+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_EXTSBr instructions */
  public static final Operator PPC_EXTSBr =
    Operator.lookupOpcode(55+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_EXTSH instructions */
  public static final Operator PPC_EXTSH =
    Operator.lookupOpcode(56+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_EXTSHr instructions */
  public static final Operator PPC_EXTSHr =
    Operator.lookupOpcode(57+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SLW instructions */
  public static final Operator PPC_SLW =
    Operator.lookupOpcode(58+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SLWr instructions */
  public static final Operator PPC_SLWr =
    Operator.lookupOpcode(59+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SLWI instructions */
  public static final Operator PPC_SLWI =
    Operator.lookupOpcode(60+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SLWIr instructions */
  public static final Operator PPC_SLWIr =
    Operator.lookupOpcode(61+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRW instructions */
  public static final Operator PPC_SRW =
    Operator.lookupOpcode(62+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRWr instructions */
  public static final Operator PPC_SRWr =
    Operator.lookupOpcode(63+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRWI instructions */
  public static final Operator PPC_SRWI =
    Operator.lookupOpcode(64+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRWIr instructions */
  public static final Operator PPC_SRWIr =
    Operator.lookupOpcode(65+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRAW instructions */
  public static final Operator PPC_SRAW =
    Operator.lookupOpcode(66+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRAWr instructions */
  public static final Operator PPC_SRAWr =
    Operator.lookupOpcode(67+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRAWI instructions */
  public static final Operator PPC_SRAWI =
    Operator.lookupOpcode(68+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRAWIr instructions */
  public static final Operator PPC_SRAWIr =
    Operator.lookupOpcode(69+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_RLWINM instructions */
  public static final Operator PPC_RLWINM =
    Operator.lookupOpcode(70+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_RLWINMr instructions */
  public static final Operator PPC_RLWINMr =
    Operator.lookupOpcode(71+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_RLWIMI instructions */
  public static final Operator PPC_RLWIMI =
    Operator.lookupOpcode(72+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_RLWIMIr instructions */
  public static final Operator PPC_RLWIMIr =
    Operator.lookupOpcode(73+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_RLWNM instructions */
  public static final Operator PPC_RLWNM =
    Operator.lookupOpcode(74+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_RLWNMr instructions */
  public static final Operator PPC_RLWNMr =
    Operator.lookupOpcode(75+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_B instructions */
  public static final Operator PPC_B =
    Operator.lookupOpcode(76+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BL instructions */
  public static final Operator PPC_BL =
    Operator.lookupOpcode(77+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BL_SYS instructions */
  public static final Operator PPC_BL_SYS =
    Operator.lookupOpcode(78+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BLR instructions */
  public static final Operator PPC_BLR =
    Operator.lookupOpcode(79+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCTR instructions */
  public static final Operator PPC_BCTR =
    Operator.lookupOpcode(80+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCTRL instructions */
  public static final Operator PPC_BCTRL =
    Operator.lookupOpcode(81+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCTRL_SYS instructions */
  public static final Operator PPC_BCTRL_SYS =
    Operator.lookupOpcode(82+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCLR instructions */
  public static final Operator PPC_BCLR =
    Operator.lookupOpcode(83+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BLRL instructions */
  public static final Operator PPC_BLRL =
    Operator.lookupOpcode(84+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCLRL instructions */
  public static final Operator PPC_BCLRL =
    Operator.lookupOpcode(85+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BC instructions */
  public static final Operator PPC_BC =
    Operator.lookupOpcode(86+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCL instructions */
  public static final Operator PPC_BCL =
    Operator.lookupOpcode(87+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCOND instructions */
  public static final Operator PPC_BCOND =
    Operator.lookupOpcode(88+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCOND2 instructions */
  public static final Operator PPC_BCOND2 =
    Operator.lookupOpcode(89+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCCTR instructions */
  public static final Operator PPC_BCCTR =
    Operator.lookupOpcode(90+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_BCC instructions */
  public static final Operator PPC_BCC =
    Operator.lookupOpcode(91+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDI instructions */
  public static final Operator PPC_ADDI =
    Operator.lookupOpcode(92+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ADDIS instructions */
  public static final Operator PPC_ADDIS =
    Operator.lookupOpcode(93+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LDI instructions */
  public static final Operator PPC_LDI =
    Operator.lookupOpcode(94+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LDIS instructions */
  public static final Operator PPC_LDIS =
    Operator.lookupOpcode(95+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CMP instructions */
  public static final Operator PPC_CMP =
    Operator.lookupOpcode(96+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CMPI instructions */
  public static final Operator PPC_CMPI =
    Operator.lookupOpcode(97+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CMPL instructions */
  public static final Operator PPC_CMPL =
    Operator.lookupOpcode(98+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CMPLI instructions */
  public static final Operator PPC_CMPLI =
    Operator.lookupOpcode(99+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CRAND instructions */
  public static final Operator PPC_CRAND =
    Operator.lookupOpcode(100+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CRANDC instructions */
  public static final Operator PPC_CRANDC =
    Operator.lookupOpcode(101+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CROR instructions */
  public static final Operator PPC_CROR =
    Operator.lookupOpcode(102+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CRORC instructions */
  public static final Operator PPC_CRORC =
    Operator.lookupOpcode(103+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMR instructions */
  public static final Operator PPC_FMR =
    Operator.lookupOpcode(104+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FRSP instructions */
  public static final Operator PPC_FRSP =
    Operator.lookupOpcode(105+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FCTIW instructions */
  public static final Operator PPC_FCTIW =
    Operator.lookupOpcode(106+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FCTIWZ instructions */
  public static final Operator PPC_FCTIWZ =
    Operator.lookupOpcode(107+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FADD instructions */
  public static final Operator PPC_FADD =
    Operator.lookupOpcode(108+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FADDS instructions */
  public static final Operator PPC_FADDS =
    Operator.lookupOpcode(109+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FSQRT instructions */
  public static final Operator PPC_FSQRT =
    Operator.lookupOpcode(110+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FSQRTS instructions */
  public static final Operator PPC_FSQRTS =
    Operator.lookupOpcode(111+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FABS instructions */
  public static final Operator PPC_FABS =
    Operator.lookupOpcode(112+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FCMPO instructions */
  public static final Operator PPC_FCMPO =
    Operator.lookupOpcode(113+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FCMPU instructions */
  public static final Operator PPC_FCMPU =
    Operator.lookupOpcode(114+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FDIV instructions */
  public static final Operator PPC_FDIV =
    Operator.lookupOpcode(115+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FDIVS instructions */
  public static final Operator PPC_FDIVS =
    Operator.lookupOpcode(116+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DIVW instructions */
  public static final Operator PPC_DIVW =
    Operator.lookupOpcode(117+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DIVWU instructions */
  public static final Operator PPC_DIVWU =
    Operator.lookupOpcode(118+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMUL instructions */
  public static final Operator PPC_FMUL =
    Operator.lookupOpcode(119+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMULS instructions */
  public static final Operator PPC_FMULS =
    Operator.lookupOpcode(120+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FSEL instructions */
  public static final Operator PPC_FSEL =
    Operator.lookupOpcode(121+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMADD instructions */
  public static final Operator PPC_FMADD =
    Operator.lookupOpcode(122+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMADDS instructions */
  public static final Operator PPC_FMADDS =
    Operator.lookupOpcode(123+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMSUB instructions */
  public static final Operator PPC_FMSUB =
    Operator.lookupOpcode(124+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FMSUBS instructions */
  public static final Operator PPC_FMSUBS =
    Operator.lookupOpcode(125+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FNMADD instructions */
  public static final Operator PPC_FNMADD =
    Operator.lookupOpcode(126+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FNMADDS instructions */
  public static final Operator PPC_FNMADDS =
    Operator.lookupOpcode(127+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FNMSUB instructions */
  public static final Operator PPC_FNMSUB =
    Operator.lookupOpcode(128+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FNMSUBS instructions */
  public static final Operator PPC_FNMSUBS =
    Operator.lookupOpcode(129+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MULLI instructions */
  public static final Operator PPC_MULLI =
    Operator.lookupOpcode(130+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MULLW instructions */
  public static final Operator PPC_MULLW =
    Operator.lookupOpcode(131+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MULHW instructions */
  public static final Operator PPC_MULHW =
    Operator.lookupOpcode(132+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MULHWU instructions */
  public static final Operator PPC_MULHWU =
    Operator.lookupOpcode(133+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FNEG instructions */
  public static final Operator PPC_FNEG =
    Operator.lookupOpcode(134+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FSUB instructions */
  public static final Operator PPC_FSUB =
    Operator.lookupOpcode(135+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_FSUBS instructions */
  public static final Operator PPC_FSUBS =
    Operator.lookupOpcode(136+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LWZ instructions */
  public static final Operator PPC_LWZ =
    Operator.lookupOpcode(137+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LWZU instructions */
  public static final Operator PPC_LWZU =
    Operator.lookupOpcode(138+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LWZUX instructions */
  public static final Operator PPC_LWZUX =
    Operator.lookupOpcode(139+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LWZX instructions */
  public static final Operator PPC_LWZX =
    Operator.lookupOpcode(140+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LWARX instructions */
  public static final Operator PPC_LWARX =
    Operator.lookupOpcode(141+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LBZ instructions */
  public static final Operator PPC_LBZ =
    Operator.lookupOpcode(142+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LBZUX instructions */
  public static final Operator PPC_LBZUX =
    Operator.lookupOpcode(143+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LBZX instructions */
  public static final Operator PPC_LBZX =
    Operator.lookupOpcode(144+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LHA instructions */
  public static final Operator PPC_LHA =
    Operator.lookupOpcode(145+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LHAX instructions */
  public static final Operator PPC_LHAX =
    Operator.lookupOpcode(146+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LHZ instructions */
  public static final Operator PPC_LHZ =
    Operator.lookupOpcode(147+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LHZX instructions */
  public static final Operator PPC_LHZX =
    Operator.lookupOpcode(148+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LFD instructions */
  public static final Operator PPC_LFD =
    Operator.lookupOpcode(149+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LFDX instructions */
  public static final Operator PPC_LFDX =
    Operator.lookupOpcode(150+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LFS instructions */
  public static final Operator PPC_LFS =
    Operator.lookupOpcode(151+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LFSX instructions */
  public static final Operator PPC_LFSX =
    Operator.lookupOpcode(152+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LMW instructions */
  public static final Operator PPC_LMW =
    Operator.lookupOpcode(153+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STW instructions */
  public static final Operator PPC_STW =
    Operator.lookupOpcode(154+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STWX instructions */
  public static final Operator PPC_STWX =
    Operator.lookupOpcode(155+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STWCXr instructions */
  public static final Operator PPC_STWCXr =
    Operator.lookupOpcode(156+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STWU instructions */
  public static final Operator PPC_STWU =
    Operator.lookupOpcode(157+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STB instructions */
  public static final Operator PPC_STB =
    Operator.lookupOpcode(158+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STBX instructions */
  public static final Operator PPC_STBX =
    Operator.lookupOpcode(159+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STH instructions */
  public static final Operator PPC_STH =
    Operator.lookupOpcode(160+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STHX instructions */
  public static final Operator PPC_STHX =
    Operator.lookupOpcode(161+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STFD instructions */
  public static final Operator PPC_STFD =
    Operator.lookupOpcode(162+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STFDX instructions */
  public static final Operator PPC_STFDX =
    Operator.lookupOpcode(163+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STFDU instructions */
  public static final Operator PPC_STFDU =
    Operator.lookupOpcode(164+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STFS instructions */
  public static final Operator PPC_STFS =
    Operator.lookupOpcode(165+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STFSX instructions */
  public static final Operator PPC_STFSX =
    Operator.lookupOpcode(166+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STFSU instructions */
  public static final Operator PPC_STFSU =
    Operator.lookupOpcode(167+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STMW instructions */
  public static final Operator PPC_STMW =
    Operator.lookupOpcode(168+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_TW instructions */
  public static final Operator PPC_TW =
    Operator.lookupOpcode(169+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_TWI instructions */
  public static final Operator PPC_TWI =
    Operator.lookupOpcode(170+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MFSPR instructions */
  public static final Operator PPC_MFSPR =
    Operator.lookupOpcode(171+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MTSPR instructions */
  public static final Operator PPC_MTSPR =
    Operator.lookupOpcode(172+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MFTB instructions */
  public static final Operator PPC_MFTB =
    Operator.lookupOpcode(173+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_MFTBU instructions */
  public static final Operator PPC_MFTBU =
    Operator.lookupOpcode(174+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_HWSYNC instructions */
  public static final Operator PPC_HWSYNC =
    Operator.lookupOpcode(175+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SYNC instructions */
  public static final Operator PPC_SYNC =
    Operator.lookupOpcode(176+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ISYNC instructions */
  public static final Operator PPC_ISYNC =
    Operator.lookupOpcode(177+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DCBF instructions */
  public static final Operator PPC_DCBF =
    Operator.lookupOpcode(178+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DCBST instructions */
  public static final Operator PPC_DCBST =
    Operator.lookupOpcode(179+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DCBT instructions */
  public static final Operator PPC_DCBT =
    Operator.lookupOpcode(180+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DCBTST instructions */
  public static final Operator PPC_DCBTST =
    Operator.lookupOpcode(181+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DCBZ instructions */
  public static final Operator PPC_DCBZ =
    Operator.lookupOpcode(182+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_DCBZL instructions */
  public static final Operator PPC_DCBZL =
    Operator.lookupOpcode(183+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ICBI instructions */
  public static final Operator PPC_ICBI =
    Operator.lookupOpcode(184+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_EXTSW instructions */
  public static final Operator PPC64_EXTSW =
    Operator.lookupOpcode(185+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_EXTSWr instructions */
  public static final Operator PPC64_EXTSWr =
    Operator.lookupOpcode(186+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_EXTZW instructions */
  public static final Operator PPC64_EXTZW =
    Operator.lookupOpcode(187+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_RLDICL instructions */
  public static final Operator PPC64_RLDICL =
    Operator.lookupOpcode(188+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_RLDICR instructions */
  public static final Operator PPC64_RLDICR =
    Operator.lookupOpcode(189+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SLD instructions */
  public static final Operator PPC64_SLD =
    Operator.lookupOpcode(190+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SLDr instructions */
  public static final Operator PPC64_SLDr =
    Operator.lookupOpcode(191+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SLDI instructions */
  public static final Operator PPC64_SLDI =
    Operator.lookupOpcode(192+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRD instructions */
  public static final Operator PPC64_SRD =
    Operator.lookupOpcode(193+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRDr instructions */
  public static final Operator PPC64_SRDr =
    Operator.lookupOpcode(194+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRAD instructions */
  public static final Operator PPC64_SRAD =
    Operator.lookupOpcode(195+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRADr instructions */
  public static final Operator PPC64_SRADr =
    Operator.lookupOpcode(196+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRADI instructions */
  public static final Operator PPC64_SRADI =
    Operator.lookupOpcode(197+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRADIr instructions */
  public static final Operator PPC64_SRADIr =
    Operator.lookupOpcode(198+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_SRDI instructions */
  public static final Operator PPC64_SRDI =
    Operator.lookupOpcode(199+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_RLDIMI instructions */
  public static final Operator PPC64_RLDIMI =
    Operator.lookupOpcode(200+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_RLDIMIr instructions */
  public static final Operator PPC64_RLDIMIr =
    Operator.lookupOpcode(201+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_CMP instructions */
  public static final Operator PPC64_CMP =
    Operator.lookupOpcode(202+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_CMPI instructions */
  public static final Operator PPC64_CMPI =
    Operator.lookupOpcode(203+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_CMPL instructions */
  public static final Operator PPC64_CMPL =
    Operator.lookupOpcode(204+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_CMPLI instructions */
  public static final Operator PPC64_CMPLI =
    Operator.lookupOpcode(205+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_FCFID instructions */
  public static final Operator PPC64_FCFID =
    Operator.lookupOpcode(206+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_FCTIDZ instructions */
  public static final Operator PPC64_FCTIDZ =
    Operator.lookupOpcode(207+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_DIVD instructions */
  public static final Operator PPC64_DIVD =
    Operator.lookupOpcode(208+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_MULLD instructions */
  public static final Operator PPC64_MULLD =
    Operator.lookupOpcode(209+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_LD instructions */
  public static final Operator PPC64_LD =
    Operator.lookupOpcode(210+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_LDX instructions */
  public static final Operator PPC64_LDX =
    Operator.lookupOpcode(211+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_STD instructions */
  public static final Operator PPC64_STD =
    Operator.lookupOpcode(212+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_STDX instructions */
  public static final Operator PPC64_STDX =
    Operator.lookupOpcode(213+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_TD instructions */
  public static final Operator PPC64_TD =
    Operator.lookupOpcode(214+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_TDI instructions */
  public static final Operator PPC64_TDI =
    Operator.lookupOpcode(215+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_CNTLZAddr instructions */
  public static final Operator PPC_CNTLZAddr =
    Operator.lookupOpcode(216+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRAAddrI instructions */
  public static final Operator PPC_SRAAddrI =
    Operator.lookupOpcode(217+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_SRAddrI instructions */
  public static final Operator PPC_SRAddrI =
    Operator.lookupOpcode(218+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_LWA instructions */
  public static final Operator PPC64_LWA =
    Operator.lookupOpcode(219+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LInt instructions */
  public static final Operator PPC_LInt =
    Operator.lookupOpcode(220+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC64_LWAX instructions */
  public static final Operator PPC64_LWAX =
    Operator.lookupOpcode(221+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LIntX instructions */
  public static final Operator PPC_LIntX =
    Operator.lookupOpcode(222+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LIntUX instructions */
  public static final Operator PPC_LIntUX =
    Operator.lookupOpcode(223+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LAddr instructions */
  public static final Operator PPC_LAddr =
    Operator.lookupOpcode(224+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LAddrX instructions */
  public static final Operator PPC_LAddrX =
    Operator.lookupOpcode(225+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LAddrU instructions */
  public static final Operator PPC_LAddrU =
    Operator.lookupOpcode(226+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LAddrUX instructions */
  public static final Operator PPC_LAddrUX =
    Operator.lookupOpcode(227+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_LAddrARX instructions */
  public static final Operator PPC_LAddrARX =
    Operator.lookupOpcode(228+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STAddr instructions */
  public static final Operator PPC_STAddr =
    Operator.lookupOpcode(229+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STAddrX instructions */
  public static final Operator PPC_STAddrX =
    Operator.lookupOpcode(230+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STAddrU instructions */
  public static final Operator PPC_STAddrU =
    Operator.lookupOpcode(231+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STAddrUX instructions */
  public static final Operator PPC_STAddrUX =
    Operator.lookupOpcode(232+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_STAddrCXr instructions */
  public static final Operator PPC_STAddrCXr =
    Operator.lookupOpcode(233+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_TAddr instructions */
  public static final Operator PPC_TAddr =
    Operator.lookupOpcode(234+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PPC_ILLEGAL_INSTRUCTION instructions */
  public static final Operator PPC_ILLEGAL_INSTRUCTION =
    Operator.lookupOpcode(235+ARCH_INDEPENDENT_END_opcode);
  /** Operator for MIR_END instructions */
  public static final Operator MIR_END =
    Operator.lookupOpcode(236+ARCH_INDEPENDENT_END_opcode);

}
