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
 *  /Users/joe/git/JOE/rvm/src-generated/opt-ir/ia32/OperatorList.dat
 */

package org.jikesrvm.compilers.opt.ir.ia32;

import org.jikesrvm.compilers.opt.ir.Operator;
import org.jikesrvm.compilers.opt.ir.Operators;

/**
 * Provides architectural literal operators.
 *
 * @see Operator
 */
public final class ArchOperators extends Operators {

  /* Architecture dependent operator codes */

  /** Opcode identifier for MATERIALIZE_FP_CONSTANT instructions */
  public static final char MATERIALIZE_FP_CONSTANT_opcode =
    (char)(0 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for ROUND_TO_ZERO instructions */
  public static final char ROUND_TO_ZERO_opcode =
    (char)(1 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for CLEAR_FLOATING_POINT_STATE instructions */
  public static final char CLEAR_FLOATING_POINT_STATE_opcode =
    (char)(2 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PREFETCH instructions */
  public static final char PREFETCH_opcode =
    (char)(3 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for PAUSE instructions */
  public static final char PAUSE_opcode =
    (char)(4 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FP_ADD instructions */
  public static final char FP_ADD_opcode =
    (char)(5 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FP_SUB instructions */
  public static final char FP_SUB_opcode =
    (char)(6 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FP_MUL instructions */
  public static final char FP_MUL_opcode =
    (char)(7 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FP_DIV instructions */
  public static final char FP_DIV_opcode =
    (char)(8 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FP_NEG instructions */
  public static final char FP_NEG_opcode =
    (char)(9 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FP_REM instructions */
  public static final char FP_REM_opcode =
    (char)(10 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for INT_2FP instructions */
  public static final char INT_2FP_opcode =
    (char)(11 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for LONG_2FP instructions */
  public static final char LONG_2FP_opcode =
    (char)(12 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for CMP_CMOV instructions */
  public static final char CMP_CMOV_opcode =
    (char)(13 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FCMP_CMOV instructions */
  public static final char FCMP_CMOV_opcode =
    (char)(14 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for LCMP_CMOV instructions */
  public static final char LCMP_CMOV_opcode =
    (char)(15 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for CMP_FCMOV instructions */
  public static final char CMP_FCMOV_opcode =
    (char)(16 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for FCMP_FCMOV instructions */
  public static final char FCMP_FCMOV_opcode =
    (char)(17 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for CALL_SAVE_VOLATILE instructions */
  public static final char CALL_SAVE_VOLATILE_opcode =
    (char)(18 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for MIR_START instructions */
  public static final char MIR_START_opcode =
    (char)(19 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for REQUIRE_ESP instructions */
  public static final char REQUIRE_ESP_opcode =
    (char)(20 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for ADVISE_ESP instructions */
  public static final char ADVISE_ESP_opcode =
    (char)(21 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for MIR_LOWTABLESWITCH instructions */
  public static final char MIR_LOWTABLESWITCH_opcode =
    (char)(22 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_METHODSTART instructions */
  public static final char IA32_METHODSTART_opcode =
    (char)(23 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FCLEAR instructions */
  public static final char IA32_FCLEAR_opcode =
    (char)(24 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DUMMY_DEF instructions */
  public static final char DUMMY_DEF_opcode =
    (char)(25 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for DUMMY_USE instructions */
  public static final char DUMMY_USE_opcode =
    (char)(26 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IMMQ_MOV instructions */
  public static final char IMMQ_MOV_opcode =
    (char)(27 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FMOV_ENDING_LIVE_RANGE instructions */
  public static final char IA32_FMOV_ENDING_LIVE_RANGE_opcode =
    (char)(28 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FMOV instructions */
  public static final char IA32_FMOV_opcode =
    (char)(29 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_TRAPIF instructions */
  public static final char IA32_TRAPIF_opcode =
    (char)(30 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_OFFSET instructions */
  public static final char IA32_OFFSET_opcode =
    (char)(31 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_LOCK_CMPXCHG instructions */
  public static final char IA32_LOCK_CMPXCHG_opcode =
    (char)(32 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_LOCK_CMPXCHG8B instructions */
  public static final char IA32_LOCK_CMPXCHG8B_opcode =
    (char)(33 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ADC instructions */
  public static final char IA32_ADC_opcode =
    (char)(34 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ADD instructions */
  public static final char IA32_ADD_opcode =
    (char)(35 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_AND instructions */
  public static final char IA32_AND_opcode =
    (char)(36 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_BSWAP instructions */
  public static final char IA32_BSWAP_opcode =
    (char)(37 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_BT instructions */
  public static final char IA32_BT_opcode =
    (char)(38 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_BTC instructions */
  public static final char IA32_BTC_opcode =
    (char)(39 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_BTR instructions */
  public static final char IA32_BTR_opcode =
    (char)(40 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_BTS instructions */
  public static final char IA32_BTS_opcode =
    (char)(41 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SYSCALL instructions */
  public static final char IA32_SYSCALL_opcode =
    (char)(42 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CALL instructions */
  public static final char IA32_CALL_opcode =
    (char)(43 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CDQ instructions */
  public static final char IA32_CDQ_opcode =
    (char)(44 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CDO instructions */
  public static final char IA32_CDO_opcode =
    (char)(45 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CDQE instructions */
  public static final char IA32_CDQE_opcode =
    (char)(46 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMOV instructions */
  public static final char IA32_CMOV_opcode =
    (char)(47 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMP instructions */
  public static final char IA32_CMP_opcode =
    (char)(48 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPXCHG instructions */
  public static final char IA32_CMPXCHG_opcode =
    (char)(49 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPXCHG8B instructions */
  public static final char IA32_CMPXCHG8B_opcode =
    (char)(50 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_DEC instructions */
  public static final char IA32_DEC_opcode =
    (char)(51 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_DIV instructions */
  public static final char IA32_DIV_opcode =
    (char)(52 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FADD instructions */
  public static final char IA32_FADD_opcode =
    (char)(53 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FADDP instructions */
  public static final char IA32_FADDP_opcode =
    (char)(54 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FCHS instructions */
  public static final char IA32_FCHS_opcode =
    (char)(55 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FCMOV instructions */
  public static final char IA32_FCMOV_opcode =
    (char)(56 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FCOMI instructions */
  public static final char IA32_FCOMI_opcode =
    (char)(57 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FCOMIP instructions */
  public static final char IA32_FCOMIP_opcode =
    (char)(58 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FDIV instructions */
  public static final char IA32_FDIV_opcode =
    (char)(59 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FDIVP instructions */
  public static final char IA32_FDIVP_opcode =
    (char)(60 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FDIVR instructions */
  public static final char IA32_FDIVR_opcode =
    (char)(61 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FDIVRP instructions */
  public static final char IA32_FDIVRP_opcode =
    (char)(62 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FEXAM instructions */
  public static final char IA32_FEXAM_opcode =
    (char)(63 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FXCH instructions */
  public static final char IA32_FXCH_opcode =
    (char)(64 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FFREE instructions */
  public static final char IA32_FFREE_opcode =
    (char)(65 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FFREEP instructions */
  public static final char IA32_FFREEP_opcode =
    (char)(66 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FIADD instructions */
  public static final char IA32_FIADD_opcode =
    (char)(67 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FIDIV instructions */
  public static final char IA32_FIDIV_opcode =
    (char)(68 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FIDIVR instructions */
  public static final char IA32_FIDIVR_opcode =
    (char)(69 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FILD instructions */
  public static final char IA32_FILD_opcode =
    (char)(70 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FIMUL instructions */
  public static final char IA32_FIMUL_opcode =
    (char)(71 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FINIT instructions */
  public static final char IA32_FINIT_opcode =
    (char)(72 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FIST instructions */
  public static final char IA32_FIST_opcode =
    (char)(73 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FISTP instructions */
  public static final char IA32_FISTP_opcode =
    (char)(74 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FISUB instructions */
  public static final char IA32_FISUB_opcode =
    (char)(75 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FISUBR instructions */
  public static final char IA32_FISUBR_opcode =
    (char)(76 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLD instructions */
  public static final char IA32_FLD_opcode =
    (char)(77 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDCW instructions */
  public static final char IA32_FLDCW_opcode =
    (char)(78 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLD1 instructions */
  public static final char IA32_FLD1_opcode =
    (char)(79 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDL2T instructions */
  public static final char IA32_FLDL2T_opcode =
    (char)(80 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDL2E instructions */
  public static final char IA32_FLDL2E_opcode =
    (char)(81 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDPI instructions */
  public static final char IA32_FLDPI_opcode =
    (char)(82 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDLG2 instructions */
  public static final char IA32_FLDLG2_opcode =
    (char)(83 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDLN2 instructions */
  public static final char IA32_FLDLN2_opcode =
    (char)(84 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FLDZ instructions */
  public static final char IA32_FLDZ_opcode =
    (char)(85 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FMUL instructions */
  public static final char IA32_FMUL_opcode =
    (char)(86 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FMULP instructions */
  public static final char IA32_FMULP_opcode =
    (char)(87 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FNSTCW instructions */
  public static final char IA32_FNSTCW_opcode =
    (char)(88 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FNSTSW instructions */
  public static final char IA32_FNSTSW_opcode =
    (char)(89 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FNINIT instructions */
  public static final char IA32_FNINIT_opcode =
    (char)(90 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FNSAVE instructions */
  public static final char IA32_FNSAVE_opcode =
    (char)(91 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FPREM instructions */
  public static final char IA32_FPREM_opcode =
    (char)(92 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FRSTOR instructions */
  public static final char IA32_FRSTOR_opcode =
    (char)(93 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FST instructions */
  public static final char IA32_FST_opcode =
    (char)(94 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSTCW instructions */
  public static final char IA32_FSTCW_opcode =
    (char)(95 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSTSW instructions */
  public static final char IA32_FSTSW_opcode =
    (char)(96 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSTP instructions */
  public static final char IA32_FSTP_opcode =
    (char)(97 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSUB instructions */
  public static final char IA32_FSUB_opcode =
    (char)(98 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSUBP instructions */
  public static final char IA32_FSUBP_opcode =
    (char)(99 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSUBR instructions */
  public static final char IA32_FSUBR_opcode =
    (char)(100 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FSUBRP instructions */
  public static final char IA32_FSUBRP_opcode =
    (char)(101 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FUCOMI instructions */
  public static final char IA32_FUCOMI_opcode =
    (char)(102 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_FUCOMIP instructions */
  public static final char IA32_FUCOMIP_opcode =
    (char)(103 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_IDIV instructions */
  public static final char IA32_IDIV_opcode =
    (char)(104 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_IMUL1 instructions */
  public static final char IA32_IMUL1_opcode =
    (char)(105 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_IMUL2 instructions */
  public static final char IA32_IMUL2_opcode =
    (char)(106 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_INC instructions */
  public static final char IA32_INC_opcode =
    (char)(107 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_INT instructions */
  public static final char IA32_INT_opcode =
    (char)(108 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_JCC instructions */
  public static final char IA32_JCC_opcode =
    (char)(109 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_JCC2 instructions */
  public static final char IA32_JCC2_opcode =
    (char)(110 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_JMP instructions */
  public static final char IA32_JMP_opcode =
    (char)(111 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_LEA instructions */
  public static final char IA32_LEA_opcode =
    (char)(112 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_LOCK instructions */
  public static final char IA32_LOCK_opcode =
    (char)(113 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOV instructions */
  public static final char IA32_MOV_opcode =
    (char)(114 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVZX__B instructions */
  public static final char IA32_MOVZX__B_opcode =
    (char)(115 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSX__B instructions */
  public static final char IA32_MOVSX__B_opcode =
    (char)(116 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVZX__W instructions */
  public static final char IA32_MOVZX__W_opcode =
    (char)(117 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSX__W instructions */
  public static final char IA32_MOVSX__W_opcode =
    (char)(118 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVZXQ__B instructions */
  public static final char IA32_MOVZXQ__B_opcode =
    (char)(119 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSXQ__B instructions */
  public static final char IA32_MOVSXQ__B_opcode =
    (char)(120 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVZXQ__W instructions */
  public static final char IA32_MOVZXQ__W_opcode =
    (char)(121 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSXQ__W instructions */
  public static final char IA32_MOVSXQ__W_opcode =
    (char)(122 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSXDQ instructions */
  public static final char IA32_MOVSXDQ_opcode =
    (char)(123 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MUL instructions */
  public static final char IA32_MUL_opcode =
    (char)(124 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_NEG instructions */
  public static final char IA32_NEG_opcode =
    (char)(125 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_NOT instructions */
  public static final char IA32_NOT_opcode =
    (char)(126 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_OR instructions */
  public static final char IA32_OR_opcode =
    (char)(127 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MFENCE instructions */
  public static final char IA32_MFENCE_opcode =
    (char)(128 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_PAUSE instructions */
  public static final char IA32_PAUSE_opcode =
    (char)(129 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_UD2 instructions */
  public static final char IA32_UD2_opcode =
    (char)(130 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_PREFETCHNTA instructions */
  public static final char IA32_PREFETCHNTA_opcode =
    (char)(131 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_POP instructions */
  public static final char IA32_POP_opcode =
    (char)(132 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_PUSH instructions */
  public static final char IA32_PUSH_opcode =
    (char)(133 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_RCL instructions */
  public static final char IA32_RCL_opcode =
    (char)(134 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_RCR instructions */
  public static final char IA32_RCR_opcode =
    (char)(135 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ROL instructions */
  public static final char IA32_ROL_opcode =
    (char)(136 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ROR instructions */
  public static final char IA32_ROR_opcode =
    (char)(137 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_RET instructions */
  public static final char IA32_RET_opcode =
    (char)(138 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SAL instructions */
  public static final char IA32_SAL_opcode =
    (char)(139 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SAR instructions */
  public static final char IA32_SAR_opcode =
    (char)(140 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SHL instructions */
  public static final char IA32_SHL_opcode =
    (char)(141 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SHR instructions */
  public static final char IA32_SHR_opcode =
    (char)(142 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SBB instructions */
  public static final char IA32_SBB_opcode =
    (char)(143 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SET__B instructions */
  public static final char IA32_SET__B_opcode =
    (char)(144 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SHLD instructions */
  public static final char IA32_SHLD_opcode =
    (char)(145 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SHRD instructions */
  public static final char IA32_SHRD_opcode =
    (char)(146 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SUB instructions */
  public static final char IA32_SUB_opcode =
    (char)(147 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_TEST instructions */
  public static final char IA32_TEST_opcode =
    (char)(148 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_XOR instructions */
  public static final char IA32_XOR_opcode =
    (char)(149 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_RDTSC instructions */
  public static final char IA32_RDTSC_opcode =
    (char)(150 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ADDSS instructions */
  public static final char IA32_ADDSS_opcode =
    (char)(151 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SUBSS instructions */
  public static final char IA32_SUBSS_opcode =
    (char)(152 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MULSS instructions */
  public static final char IA32_MULSS_opcode =
    (char)(153 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_DIVSS instructions */
  public static final char IA32_DIVSS_opcode =
    (char)(154 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ADDSD instructions */
  public static final char IA32_ADDSD_opcode =
    (char)(155 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SUBSD instructions */
  public static final char IA32_SUBSD_opcode =
    (char)(156 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MULSD instructions */
  public static final char IA32_MULSD_opcode =
    (char)(157 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_DIVSD instructions */
  public static final char IA32_DIVSD_opcode =
    (char)(158 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ANDPS instructions */
  public static final char IA32_ANDPS_opcode =
    (char)(159 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ANDPD instructions */
  public static final char IA32_ANDPD_opcode =
    (char)(160 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ANDNPS instructions */
  public static final char IA32_ANDNPS_opcode =
    (char)(161 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ANDNPD instructions */
  public static final char IA32_ANDNPD_opcode =
    (char)(162 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ORPS instructions */
  public static final char IA32_ORPS_opcode =
    (char)(163 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_ORPD instructions */
  public static final char IA32_ORPD_opcode =
    (char)(164 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_XORPS instructions */
  public static final char IA32_XORPS_opcode =
    (char)(165 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_XORPD instructions */
  public static final char IA32_XORPD_opcode =
    (char)(166 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_UCOMISS instructions */
  public static final char IA32_UCOMISS_opcode =
    (char)(167 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_UCOMISD instructions */
  public static final char IA32_UCOMISD_opcode =
    (char)(168 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPEQSS instructions */
  public static final char IA32_CMPEQSS_opcode =
    (char)(169 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPLTSS instructions */
  public static final char IA32_CMPLTSS_opcode =
    (char)(170 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPLESS instructions */
  public static final char IA32_CMPLESS_opcode =
    (char)(171 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPUNORDSS instructions */
  public static final char IA32_CMPUNORDSS_opcode =
    (char)(172 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPNESS instructions */
  public static final char IA32_CMPNESS_opcode =
    (char)(173 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPNLTSS instructions */
  public static final char IA32_CMPNLTSS_opcode =
    (char)(174 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPNLESS instructions */
  public static final char IA32_CMPNLESS_opcode =
    (char)(175 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPORDSS instructions */
  public static final char IA32_CMPORDSS_opcode =
    (char)(176 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPEQSD instructions */
  public static final char IA32_CMPEQSD_opcode =
    (char)(177 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPLTSD instructions */
  public static final char IA32_CMPLTSD_opcode =
    (char)(178 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPLESD instructions */
  public static final char IA32_CMPLESD_opcode =
    (char)(179 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPUNORDSD instructions */
  public static final char IA32_CMPUNORDSD_opcode =
    (char)(180 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPNESD instructions */
  public static final char IA32_CMPNESD_opcode =
    (char)(181 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPNLTSD instructions */
  public static final char IA32_CMPNLTSD_opcode =
    (char)(182 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPNLESD instructions */
  public static final char IA32_CMPNLESD_opcode =
    (char)(183 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CMPORDSD instructions */
  public static final char IA32_CMPORDSD_opcode =
    (char)(184 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVAPD instructions */
  public static final char IA32_MOVAPD_opcode =
    (char)(185 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVAPS instructions */
  public static final char IA32_MOVAPS_opcode =
    (char)(186 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVLPD instructions */
  public static final char IA32_MOVLPD_opcode =
    (char)(187 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVLPS instructions */
  public static final char IA32_MOVLPS_opcode =
    (char)(188 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSS instructions */
  public static final char IA32_MOVSS_opcode =
    (char)(189 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVSD instructions */
  public static final char IA32_MOVSD_opcode =
    (char)(190 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVD instructions */
  public static final char IA32_MOVD_opcode =
    (char)(191 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_MOVQ instructions */
  public static final char IA32_MOVQ_opcode =
    (char)(192 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_PSLLQ instructions */
  public static final char IA32_PSLLQ_opcode =
    (char)(193 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_PSRLQ instructions */
  public static final char IA32_PSRLQ_opcode =
    (char)(194 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SQRTSS instructions */
  public static final char IA32_SQRTSS_opcode =
    (char)(195 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_SQRTSD instructions */
  public static final char IA32_SQRTSD_opcode =
    (char)(196 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSI2SS instructions */
  public static final char IA32_CVTSI2SS_opcode =
    (char)(197 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSS2SD instructions */
  public static final char IA32_CVTSS2SD_opcode =
    (char)(198 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSS2SI instructions */
  public static final char IA32_CVTSS2SI_opcode =
    (char)(199 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTTSS2SI instructions */
  public static final char IA32_CVTTSS2SI_opcode =
    (char)(200 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSI2SD instructions */
  public static final char IA32_CVTSI2SD_opcode =
    (char)(201 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSD2SS instructions */
  public static final char IA32_CVTSD2SS_opcode =
    (char)(202 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSD2SI instructions */
  public static final char IA32_CVTSD2SI_opcode =
    (char)(203 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTTSD2SI instructions */
  public static final char IA32_CVTTSD2SI_opcode =
    (char)(204 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSI2SDQ instructions */
  public static final char IA32_CVTSI2SDQ_opcode =
    (char)(205 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTSD2SIQ instructions */
  public static final char IA32_CVTSD2SIQ_opcode =
    (char)(206 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for IA32_CVTTSD2SIQ instructions */
  public static final char IA32_CVTTSD2SIQ_opcode =
    (char)(207 + ARCH_INDEPENDENT_END_opcode);
  /** Opcode identifier for MIR_END instructions */
  public static final char MIR_END_opcode =
    (char)(208 + ARCH_INDEPENDENT_END_opcode);

  /* Architecture dependent operators */

  /** Operator for MATERIALIZE_FP_CONSTANT instructions */
  public static final Operator MATERIALIZE_FP_CONSTANT =
    Operator.lookupOpcode(0+ARCH_INDEPENDENT_END_opcode);
  /** Operator for ROUND_TO_ZERO instructions */
  public static final Operator ROUND_TO_ZERO =
    Operator.lookupOpcode(1+ARCH_INDEPENDENT_END_opcode);
  /** Operator for CLEAR_FLOATING_POINT_STATE instructions */
  public static final Operator CLEAR_FLOATING_POINT_STATE =
    Operator.lookupOpcode(2+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PREFETCH instructions */
  public static final Operator PREFETCH =
    Operator.lookupOpcode(3+ARCH_INDEPENDENT_END_opcode);
  /** Operator for PAUSE instructions */
  public static final Operator PAUSE =
    Operator.lookupOpcode(4+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FP_ADD instructions */
  public static final Operator FP_ADD =
    Operator.lookupOpcode(5+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FP_SUB instructions */
  public static final Operator FP_SUB =
    Operator.lookupOpcode(6+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FP_MUL instructions */
  public static final Operator FP_MUL =
    Operator.lookupOpcode(7+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FP_DIV instructions */
  public static final Operator FP_DIV =
    Operator.lookupOpcode(8+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FP_NEG instructions */
  public static final Operator FP_NEG =
    Operator.lookupOpcode(9+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FP_REM instructions */
  public static final Operator FP_REM =
    Operator.lookupOpcode(10+ARCH_INDEPENDENT_END_opcode);
  /** Operator for INT_2FP instructions */
  public static final Operator INT_2FP =
    Operator.lookupOpcode(11+ARCH_INDEPENDENT_END_opcode);
  /** Operator for LONG_2FP instructions */
  public static final Operator LONG_2FP =
    Operator.lookupOpcode(12+ARCH_INDEPENDENT_END_opcode);
  /** Operator for CMP_CMOV instructions */
  public static final Operator CMP_CMOV =
    Operator.lookupOpcode(13+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FCMP_CMOV instructions */
  public static final Operator FCMP_CMOV =
    Operator.lookupOpcode(14+ARCH_INDEPENDENT_END_opcode);
  /** Operator for LCMP_CMOV instructions */
  public static final Operator LCMP_CMOV =
    Operator.lookupOpcode(15+ARCH_INDEPENDENT_END_opcode);
  /** Operator for CMP_FCMOV instructions */
  public static final Operator CMP_FCMOV =
    Operator.lookupOpcode(16+ARCH_INDEPENDENT_END_opcode);
  /** Operator for FCMP_FCMOV instructions */
  public static final Operator FCMP_FCMOV =
    Operator.lookupOpcode(17+ARCH_INDEPENDENT_END_opcode);
  /** Operator for CALL_SAVE_VOLATILE instructions */
  public static final Operator CALL_SAVE_VOLATILE =
    Operator.lookupOpcode(18+ARCH_INDEPENDENT_END_opcode);
  /** Operator for MIR_START instructions */
  public static final Operator MIR_START =
    Operator.lookupOpcode(19+ARCH_INDEPENDENT_END_opcode);
  /** Operator for REQUIRE_ESP instructions */
  public static final Operator REQUIRE_ESP =
    Operator.lookupOpcode(20+ARCH_INDEPENDENT_END_opcode);
  /** Operator for ADVISE_ESP instructions */
  public static final Operator ADVISE_ESP =
    Operator.lookupOpcode(21+ARCH_INDEPENDENT_END_opcode);
  /** Operator for MIR_LOWTABLESWITCH instructions */
  public static final Operator MIR_LOWTABLESWITCH =
    Operator.lookupOpcode(22+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_METHODSTART instructions */
  public static final Operator IA32_METHODSTART =
    Operator.lookupOpcode(23+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FCLEAR instructions */
  public static final Operator IA32_FCLEAR =
    Operator.lookupOpcode(24+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DUMMY_DEF instructions */
  public static final Operator DUMMY_DEF =
    Operator.lookupOpcode(25+ARCH_INDEPENDENT_END_opcode);
  /** Operator for DUMMY_USE instructions */
  public static final Operator DUMMY_USE =
    Operator.lookupOpcode(26+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IMMQ_MOV instructions */
  public static final Operator IMMQ_MOV =
    Operator.lookupOpcode(27+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FMOV_ENDING_LIVE_RANGE instructions */
  public static final Operator IA32_FMOV_ENDING_LIVE_RANGE =
    Operator.lookupOpcode(28+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FMOV instructions */
  public static final Operator IA32_FMOV =
    Operator.lookupOpcode(29+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_TRAPIF instructions */
  public static final Operator IA32_TRAPIF =
    Operator.lookupOpcode(30+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_OFFSET instructions */
  public static final Operator IA32_OFFSET =
    Operator.lookupOpcode(31+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_LOCK_CMPXCHG instructions */
  public static final Operator IA32_LOCK_CMPXCHG =
    Operator.lookupOpcode(32+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_LOCK_CMPXCHG8B instructions */
  public static final Operator IA32_LOCK_CMPXCHG8B =
    Operator.lookupOpcode(33+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ADC instructions */
  public static final Operator IA32_ADC =
    Operator.lookupOpcode(34+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ADD instructions */
  public static final Operator IA32_ADD =
    Operator.lookupOpcode(35+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_AND instructions */
  public static final Operator IA32_AND =
    Operator.lookupOpcode(36+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_BSWAP instructions */
  public static final Operator IA32_BSWAP =
    Operator.lookupOpcode(37+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_BT instructions */
  public static final Operator IA32_BT =
    Operator.lookupOpcode(38+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_BTC instructions */
  public static final Operator IA32_BTC =
    Operator.lookupOpcode(39+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_BTR instructions */
  public static final Operator IA32_BTR =
    Operator.lookupOpcode(40+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_BTS instructions */
  public static final Operator IA32_BTS =
    Operator.lookupOpcode(41+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SYSCALL instructions */
  public static final Operator IA32_SYSCALL =
    Operator.lookupOpcode(42+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CALL instructions */
  public static final Operator IA32_CALL =
    Operator.lookupOpcode(43+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CDQ instructions */
  public static final Operator IA32_CDQ =
    Operator.lookupOpcode(44+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CDO instructions */
  public static final Operator IA32_CDO =
    Operator.lookupOpcode(45+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CDQE instructions */
  public static final Operator IA32_CDQE =
    Operator.lookupOpcode(46+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMOV instructions */
  public static final Operator IA32_CMOV =
    Operator.lookupOpcode(47+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMP instructions */
  public static final Operator IA32_CMP =
    Operator.lookupOpcode(48+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPXCHG instructions */
  public static final Operator IA32_CMPXCHG =
    Operator.lookupOpcode(49+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPXCHG8B instructions */
  public static final Operator IA32_CMPXCHG8B =
    Operator.lookupOpcode(50+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_DEC instructions */
  public static final Operator IA32_DEC =
    Operator.lookupOpcode(51+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_DIV instructions */
  public static final Operator IA32_DIV =
    Operator.lookupOpcode(52+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FADD instructions */
  public static final Operator IA32_FADD =
    Operator.lookupOpcode(53+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FADDP instructions */
  public static final Operator IA32_FADDP =
    Operator.lookupOpcode(54+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FCHS instructions */
  public static final Operator IA32_FCHS =
    Operator.lookupOpcode(55+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FCMOV instructions */
  public static final Operator IA32_FCMOV =
    Operator.lookupOpcode(56+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FCOMI instructions */
  public static final Operator IA32_FCOMI =
    Operator.lookupOpcode(57+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FCOMIP instructions */
  public static final Operator IA32_FCOMIP =
    Operator.lookupOpcode(58+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FDIV instructions */
  public static final Operator IA32_FDIV =
    Operator.lookupOpcode(59+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FDIVP instructions */
  public static final Operator IA32_FDIVP =
    Operator.lookupOpcode(60+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FDIVR instructions */
  public static final Operator IA32_FDIVR =
    Operator.lookupOpcode(61+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FDIVRP instructions */
  public static final Operator IA32_FDIVRP =
    Operator.lookupOpcode(62+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FEXAM instructions */
  public static final Operator IA32_FEXAM =
    Operator.lookupOpcode(63+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FXCH instructions */
  public static final Operator IA32_FXCH =
    Operator.lookupOpcode(64+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FFREE instructions */
  public static final Operator IA32_FFREE =
    Operator.lookupOpcode(65+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FFREEP instructions */
  public static final Operator IA32_FFREEP =
    Operator.lookupOpcode(66+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FIADD instructions */
  public static final Operator IA32_FIADD =
    Operator.lookupOpcode(67+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FIDIV instructions */
  public static final Operator IA32_FIDIV =
    Operator.lookupOpcode(68+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FIDIVR instructions */
  public static final Operator IA32_FIDIVR =
    Operator.lookupOpcode(69+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FILD instructions */
  public static final Operator IA32_FILD =
    Operator.lookupOpcode(70+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FIMUL instructions */
  public static final Operator IA32_FIMUL =
    Operator.lookupOpcode(71+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FINIT instructions */
  public static final Operator IA32_FINIT =
    Operator.lookupOpcode(72+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FIST instructions */
  public static final Operator IA32_FIST =
    Operator.lookupOpcode(73+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FISTP instructions */
  public static final Operator IA32_FISTP =
    Operator.lookupOpcode(74+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FISUB instructions */
  public static final Operator IA32_FISUB =
    Operator.lookupOpcode(75+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FISUBR instructions */
  public static final Operator IA32_FISUBR =
    Operator.lookupOpcode(76+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLD instructions */
  public static final Operator IA32_FLD =
    Operator.lookupOpcode(77+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDCW instructions */
  public static final Operator IA32_FLDCW =
    Operator.lookupOpcode(78+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLD1 instructions */
  public static final Operator IA32_FLD1 =
    Operator.lookupOpcode(79+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDL2T instructions */
  public static final Operator IA32_FLDL2T =
    Operator.lookupOpcode(80+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDL2E instructions */
  public static final Operator IA32_FLDL2E =
    Operator.lookupOpcode(81+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDPI instructions */
  public static final Operator IA32_FLDPI =
    Operator.lookupOpcode(82+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDLG2 instructions */
  public static final Operator IA32_FLDLG2 =
    Operator.lookupOpcode(83+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDLN2 instructions */
  public static final Operator IA32_FLDLN2 =
    Operator.lookupOpcode(84+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FLDZ instructions */
  public static final Operator IA32_FLDZ =
    Operator.lookupOpcode(85+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FMUL instructions */
  public static final Operator IA32_FMUL =
    Operator.lookupOpcode(86+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FMULP instructions */
  public static final Operator IA32_FMULP =
    Operator.lookupOpcode(87+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FNSTCW instructions */
  public static final Operator IA32_FNSTCW =
    Operator.lookupOpcode(88+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FNSTSW instructions */
  public static final Operator IA32_FNSTSW =
    Operator.lookupOpcode(89+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FNINIT instructions */
  public static final Operator IA32_FNINIT =
    Operator.lookupOpcode(90+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FNSAVE instructions */
  public static final Operator IA32_FNSAVE =
    Operator.lookupOpcode(91+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FPREM instructions */
  public static final Operator IA32_FPREM =
    Operator.lookupOpcode(92+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FRSTOR instructions */
  public static final Operator IA32_FRSTOR =
    Operator.lookupOpcode(93+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FST instructions */
  public static final Operator IA32_FST =
    Operator.lookupOpcode(94+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSTCW instructions */
  public static final Operator IA32_FSTCW =
    Operator.lookupOpcode(95+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSTSW instructions */
  public static final Operator IA32_FSTSW =
    Operator.lookupOpcode(96+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSTP instructions */
  public static final Operator IA32_FSTP =
    Operator.lookupOpcode(97+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSUB instructions */
  public static final Operator IA32_FSUB =
    Operator.lookupOpcode(98+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSUBP instructions */
  public static final Operator IA32_FSUBP =
    Operator.lookupOpcode(99+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSUBR instructions */
  public static final Operator IA32_FSUBR =
    Operator.lookupOpcode(100+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FSUBRP instructions */
  public static final Operator IA32_FSUBRP =
    Operator.lookupOpcode(101+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FUCOMI instructions */
  public static final Operator IA32_FUCOMI =
    Operator.lookupOpcode(102+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_FUCOMIP instructions */
  public static final Operator IA32_FUCOMIP =
    Operator.lookupOpcode(103+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_IDIV instructions */
  public static final Operator IA32_IDIV =
    Operator.lookupOpcode(104+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_IMUL1 instructions */
  public static final Operator IA32_IMUL1 =
    Operator.lookupOpcode(105+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_IMUL2 instructions */
  public static final Operator IA32_IMUL2 =
    Operator.lookupOpcode(106+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_INC instructions */
  public static final Operator IA32_INC =
    Operator.lookupOpcode(107+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_INT instructions */
  public static final Operator IA32_INT =
    Operator.lookupOpcode(108+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_JCC instructions */
  public static final Operator IA32_JCC =
    Operator.lookupOpcode(109+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_JCC2 instructions */
  public static final Operator IA32_JCC2 =
    Operator.lookupOpcode(110+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_JMP instructions */
  public static final Operator IA32_JMP =
    Operator.lookupOpcode(111+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_LEA instructions */
  public static final Operator IA32_LEA =
    Operator.lookupOpcode(112+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_LOCK instructions */
  public static final Operator IA32_LOCK =
    Operator.lookupOpcode(113+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOV instructions */
  public static final Operator IA32_MOV =
    Operator.lookupOpcode(114+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVZX__B instructions */
  public static final Operator IA32_MOVZX__B =
    Operator.lookupOpcode(115+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSX__B instructions */
  public static final Operator IA32_MOVSX__B =
    Operator.lookupOpcode(116+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVZX__W instructions */
  public static final Operator IA32_MOVZX__W =
    Operator.lookupOpcode(117+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSX__W instructions */
  public static final Operator IA32_MOVSX__W =
    Operator.lookupOpcode(118+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVZXQ__B instructions */
  public static final Operator IA32_MOVZXQ__B =
    Operator.lookupOpcode(119+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSXQ__B instructions */
  public static final Operator IA32_MOVSXQ__B =
    Operator.lookupOpcode(120+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVZXQ__W instructions */
  public static final Operator IA32_MOVZXQ__W =
    Operator.lookupOpcode(121+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSXQ__W instructions */
  public static final Operator IA32_MOVSXQ__W =
    Operator.lookupOpcode(122+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSXDQ instructions */
  public static final Operator IA32_MOVSXDQ =
    Operator.lookupOpcode(123+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MUL instructions */
  public static final Operator IA32_MUL =
    Operator.lookupOpcode(124+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_NEG instructions */
  public static final Operator IA32_NEG =
    Operator.lookupOpcode(125+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_NOT instructions */
  public static final Operator IA32_NOT =
    Operator.lookupOpcode(126+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_OR instructions */
  public static final Operator IA32_OR =
    Operator.lookupOpcode(127+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MFENCE instructions */
  public static final Operator IA32_MFENCE =
    Operator.lookupOpcode(128+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_PAUSE instructions */
  public static final Operator IA32_PAUSE =
    Operator.lookupOpcode(129+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_UD2 instructions */
  public static final Operator IA32_UD2 =
    Operator.lookupOpcode(130+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_PREFETCHNTA instructions */
  public static final Operator IA32_PREFETCHNTA =
    Operator.lookupOpcode(131+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_POP instructions */
  public static final Operator IA32_POP =
    Operator.lookupOpcode(132+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_PUSH instructions */
  public static final Operator IA32_PUSH =
    Operator.lookupOpcode(133+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_RCL instructions */
  public static final Operator IA32_RCL =
    Operator.lookupOpcode(134+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_RCR instructions */
  public static final Operator IA32_RCR =
    Operator.lookupOpcode(135+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ROL instructions */
  public static final Operator IA32_ROL =
    Operator.lookupOpcode(136+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ROR instructions */
  public static final Operator IA32_ROR =
    Operator.lookupOpcode(137+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_RET instructions */
  public static final Operator IA32_RET =
    Operator.lookupOpcode(138+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SAL instructions */
  public static final Operator IA32_SAL =
    Operator.lookupOpcode(139+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SAR instructions */
  public static final Operator IA32_SAR =
    Operator.lookupOpcode(140+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SHL instructions */
  public static final Operator IA32_SHL =
    Operator.lookupOpcode(141+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SHR instructions */
  public static final Operator IA32_SHR =
    Operator.lookupOpcode(142+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SBB instructions */
  public static final Operator IA32_SBB =
    Operator.lookupOpcode(143+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SET__B instructions */
  public static final Operator IA32_SET__B =
    Operator.lookupOpcode(144+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SHLD instructions */
  public static final Operator IA32_SHLD =
    Operator.lookupOpcode(145+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SHRD instructions */
  public static final Operator IA32_SHRD =
    Operator.lookupOpcode(146+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SUB instructions */
  public static final Operator IA32_SUB =
    Operator.lookupOpcode(147+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_TEST instructions */
  public static final Operator IA32_TEST =
    Operator.lookupOpcode(148+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_XOR instructions */
  public static final Operator IA32_XOR =
    Operator.lookupOpcode(149+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_RDTSC instructions */
  public static final Operator IA32_RDTSC =
    Operator.lookupOpcode(150+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ADDSS instructions */
  public static final Operator IA32_ADDSS =
    Operator.lookupOpcode(151+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SUBSS instructions */
  public static final Operator IA32_SUBSS =
    Operator.lookupOpcode(152+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MULSS instructions */
  public static final Operator IA32_MULSS =
    Operator.lookupOpcode(153+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_DIVSS instructions */
  public static final Operator IA32_DIVSS =
    Operator.lookupOpcode(154+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ADDSD instructions */
  public static final Operator IA32_ADDSD =
    Operator.lookupOpcode(155+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SUBSD instructions */
  public static final Operator IA32_SUBSD =
    Operator.lookupOpcode(156+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MULSD instructions */
  public static final Operator IA32_MULSD =
    Operator.lookupOpcode(157+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_DIVSD instructions */
  public static final Operator IA32_DIVSD =
    Operator.lookupOpcode(158+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ANDPS instructions */
  public static final Operator IA32_ANDPS =
    Operator.lookupOpcode(159+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ANDPD instructions */
  public static final Operator IA32_ANDPD =
    Operator.lookupOpcode(160+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ANDNPS instructions */
  public static final Operator IA32_ANDNPS =
    Operator.lookupOpcode(161+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ANDNPD instructions */
  public static final Operator IA32_ANDNPD =
    Operator.lookupOpcode(162+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ORPS instructions */
  public static final Operator IA32_ORPS =
    Operator.lookupOpcode(163+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_ORPD instructions */
  public static final Operator IA32_ORPD =
    Operator.lookupOpcode(164+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_XORPS instructions */
  public static final Operator IA32_XORPS =
    Operator.lookupOpcode(165+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_XORPD instructions */
  public static final Operator IA32_XORPD =
    Operator.lookupOpcode(166+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_UCOMISS instructions */
  public static final Operator IA32_UCOMISS =
    Operator.lookupOpcode(167+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_UCOMISD instructions */
  public static final Operator IA32_UCOMISD =
    Operator.lookupOpcode(168+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPEQSS instructions */
  public static final Operator IA32_CMPEQSS =
    Operator.lookupOpcode(169+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPLTSS instructions */
  public static final Operator IA32_CMPLTSS =
    Operator.lookupOpcode(170+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPLESS instructions */
  public static final Operator IA32_CMPLESS =
    Operator.lookupOpcode(171+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPUNORDSS instructions */
  public static final Operator IA32_CMPUNORDSS =
    Operator.lookupOpcode(172+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPNESS instructions */
  public static final Operator IA32_CMPNESS =
    Operator.lookupOpcode(173+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPNLTSS instructions */
  public static final Operator IA32_CMPNLTSS =
    Operator.lookupOpcode(174+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPNLESS instructions */
  public static final Operator IA32_CMPNLESS =
    Operator.lookupOpcode(175+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPORDSS instructions */
  public static final Operator IA32_CMPORDSS =
    Operator.lookupOpcode(176+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPEQSD instructions */
  public static final Operator IA32_CMPEQSD =
    Operator.lookupOpcode(177+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPLTSD instructions */
  public static final Operator IA32_CMPLTSD =
    Operator.lookupOpcode(178+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPLESD instructions */
  public static final Operator IA32_CMPLESD =
    Operator.lookupOpcode(179+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPUNORDSD instructions */
  public static final Operator IA32_CMPUNORDSD =
    Operator.lookupOpcode(180+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPNESD instructions */
  public static final Operator IA32_CMPNESD =
    Operator.lookupOpcode(181+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPNLTSD instructions */
  public static final Operator IA32_CMPNLTSD =
    Operator.lookupOpcode(182+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPNLESD instructions */
  public static final Operator IA32_CMPNLESD =
    Operator.lookupOpcode(183+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CMPORDSD instructions */
  public static final Operator IA32_CMPORDSD =
    Operator.lookupOpcode(184+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVAPD instructions */
  public static final Operator IA32_MOVAPD =
    Operator.lookupOpcode(185+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVAPS instructions */
  public static final Operator IA32_MOVAPS =
    Operator.lookupOpcode(186+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVLPD instructions */
  public static final Operator IA32_MOVLPD =
    Operator.lookupOpcode(187+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVLPS instructions */
  public static final Operator IA32_MOVLPS =
    Operator.lookupOpcode(188+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSS instructions */
  public static final Operator IA32_MOVSS =
    Operator.lookupOpcode(189+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVSD instructions */
  public static final Operator IA32_MOVSD =
    Operator.lookupOpcode(190+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVD instructions */
  public static final Operator IA32_MOVD =
    Operator.lookupOpcode(191+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_MOVQ instructions */
  public static final Operator IA32_MOVQ =
    Operator.lookupOpcode(192+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_PSLLQ instructions */
  public static final Operator IA32_PSLLQ =
    Operator.lookupOpcode(193+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_PSRLQ instructions */
  public static final Operator IA32_PSRLQ =
    Operator.lookupOpcode(194+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SQRTSS instructions */
  public static final Operator IA32_SQRTSS =
    Operator.lookupOpcode(195+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_SQRTSD instructions */
  public static final Operator IA32_SQRTSD =
    Operator.lookupOpcode(196+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSI2SS instructions */
  public static final Operator IA32_CVTSI2SS =
    Operator.lookupOpcode(197+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSS2SD instructions */
  public static final Operator IA32_CVTSS2SD =
    Operator.lookupOpcode(198+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSS2SI instructions */
  public static final Operator IA32_CVTSS2SI =
    Operator.lookupOpcode(199+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTTSS2SI instructions */
  public static final Operator IA32_CVTTSS2SI =
    Operator.lookupOpcode(200+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSI2SD instructions */
  public static final Operator IA32_CVTSI2SD =
    Operator.lookupOpcode(201+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSD2SS instructions */
  public static final Operator IA32_CVTSD2SS =
    Operator.lookupOpcode(202+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSD2SI instructions */
  public static final Operator IA32_CVTSD2SI =
    Operator.lookupOpcode(203+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTTSD2SI instructions */
  public static final Operator IA32_CVTTSD2SI =
    Operator.lookupOpcode(204+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSI2SDQ instructions */
  public static final Operator IA32_CVTSI2SDQ =
    Operator.lookupOpcode(205+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTSD2SIQ instructions */
  public static final Operator IA32_CVTSD2SIQ =
    Operator.lookupOpcode(206+ARCH_INDEPENDENT_END_opcode);
  /** Operator for IA32_CVTTSD2SIQ instructions */
  public static final Operator IA32_CVTTSD2SIQ =
    Operator.lookupOpcode(207+ARCH_INDEPENDENT_END_opcode);
  /** Operator for MIR_END instructions */
  public static final Operator MIR_END =
    Operator.lookupOpcode(208+ARCH_INDEPENDENT_END_opcode);

}
