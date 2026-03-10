
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
package org.jikesrvm.compilers.opt.ir.ia32;

import org.jikesrvm.VM;
import org.jikesrvm.compilers.opt.OptimizingCompilerException;
import org.jikesrvm.compilers.opt.ir.operand.*;
import org.jikesrvm.compilers.opt.ir.InstructionFormat;
import org.jikesrvm.compilers.opt.ir.Operator;

/**
 * Abstract parent class of all InstructionFormat classes.<p>
 *
 * The header comment for {@link org.jikesrvm.compilers.opt.ir.Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.<p>
 *
 * NOTE: We currently only support a subset of cases of variable operands.
 * <ul>
 *  <li>instructions with 0+ defs, 0+ def/uses, 0+ uses, and a variable number of uses
 *  <li>instructions with 0+ defs and variable number of defs.
 * Variable number of def/uses and variable number of defs with non-zero
 * number of def/uses or uses are not supported (and will generate java code
 * for the instruction format that doesn't compile).  Fully general support would
 * be a pain in the butt and since it currently isn't required, we don't do it.
 * </ul>
 * <p>
 *
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See ArchInstructionFormats.template, InstructionFormatList.dat,
 * OperatorList.dat, etc
 */
public abstract class ArchInstructionFormat extends InstructionFormat {

  static {
    if (MIN_OPERAND_ARRAY_LENGTH != 5) {
      throw new Error("Disagreement between architecture and common instruction formats on minimum operands");
    }
  }

  /** Typecode for the MIR_LowTableSwitch InstructionFormat */
  public static final byte MIR_LowTableSwitch_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+0;
  /** Typecode for the MIR_Move InstructionFormat */
  public static final byte MIR_Move_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+1;
  /** Typecode for the MIR_CondMove InstructionFormat */
  public static final byte MIR_CondMove_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+2;
  /** Typecode for the MIR_Lea InstructionFormat */
  public static final byte MIR_Lea_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+3;
  /** Typecode for the MIR_BinaryAcc InstructionFormat */
  public static final byte MIR_BinaryAcc_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+4;
  /** Typecode for the MIR_Divide InstructionFormat */
  public static final byte MIR_Divide_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+5;
  /** Typecode for the MIR_Multiply InstructionFormat */
  public static final byte MIR_Multiply_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+6;
  /** Typecode for the MIR_ConvertDW2QW InstructionFormat */
  public static final byte MIR_ConvertDW2QW_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+7;
  /** Typecode for the MIR_UnaryAcc InstructionFormat */
  public static final byte MIR_UnaryAcc_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+8;
  /** Typecode for the MIR_Compare InstructionFormat */
  public static final byte MIR_Compare_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+9;
  /** Typecode for the MIR_CompareExchange InstructionFormat */
  public static final byte MIR_CompareExchange_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+10;
  /** Typecode for the MIR_CompareExchange8B InstructionFormat */
  public static final byte MIR_CompareExchange8B_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+11;
  /** Typecode for the MIR_Trap InstructionFormat */
  public static final byte MIR_Trap_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+12;
  /** Typecode for the MIR_TrapIf InstructionFormat */
  public static final byte MIR_TrapIf_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+13;
  /** Typecode for the MIR_Branch InstructionFormat */
  public static final byte MIR_Branch_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+14;
  /** Typecode for the MIR_CondBranch InstructionFormat */
  public static final byte MIR_CondBranch_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+15;
  /** Typecode for the MIR_CondBranch2 InstructionFormat */
  public static final byte MIR_CondBranch2_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+16;
  /** Typecode for the MIR_Call InstructionFormat */
  public static final byte MIR_Call_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+17;
  /** Typecode for the MIR_Empty InstructionFormat */
  public static final byte MIR_Empty_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+18;
  /** Typecode for the MIR_Return InstructionFormat */
  public static final byte MIR_Return_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+19;
  /** Typecode for the MIR_Set InstructionFormat */
  public static final byte MIR_Set_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+20;
  /** Typecode for the MIR_Test InstructionFormat */
  public static final byte MIR_Test_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+21;
  /** Typecode for the MIR_Nullary InstructionFormat */
  public static final byte MIR_Nullary_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+22;
  /** Typecode for the MIR_UnaryNoRes InstructionFormat */
  public static final byte MIR_UnaryNoRes_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+23;
  /** Typecode for the MIR_Unary InstructionFormat */
  public static final byte MIR_Unary_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+24;
  /** Typecode for the MIR_XChng InstructionFormat */
  public static final byte MIR_XChng_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+25;
  /** Typecode for the MIR_DoubleShift InstructionFormat */
  public static final byte MIR_DoubleShift_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+26;
  /** Typecode for the MIR_CaseLabel InstructionFormat */
  public static final byte MIR_CaseLabel_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+27;
  /** Typecode for the MIR_FSave InstructionFormat */
  public static final byte MIR_FSave_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+28;
  /** Typecode for the MIR_RDTSC InstructionFormat */
  public static final byte MIR_RDTSC_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+29;
  /** Typecode for the MIR_CacheOp InstructionFormat */
  public static final byte MIR_CacheOp_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+30;

  /** Shared traits for operators of the MIR_LowTableSwitch InstructionFormat */
  public static final int MIR_LowTableSwitch_traits = Operator.varUses;
  /** Shared traits for operators of the MIR_Move InstructionFormat */
  public static final int MIR_Move_traits = Operator.none;
  /** Shared traits for operators of the MIR_CondMove InstructionFormat */
  public static final int MIR_CondMove_traits = Operator.none;
  /** Shared traits for operators of the MIR_Lea InstructionFormat */
  public static final int MIR_Lea_traits = Operator.none;
  /** Shared traits for operators of the MIR_BinaryAcc InstructionFormat */
  public static final int MIR_BinaryAcc_traits = Operator.none;
  /** Shared traits for operators of the MIR_Divide InstructionFormat */
  public static final int MIR_Divide_traits = Operator.none;
  /** Shared traits for operators of the MIR_Multiply InstructionFormat */
  public static final int MIR_Multiply_traits = Operator.none;
  /** Shared traits for operators of the MIR_ConvertDW2QW InstructionFormat */
  public static final int MIR_ConvertDW2QW_traits = Operator.none;
  /** Shared traits for operators of the MIR_UnaryAcc InstructionFormat */
  public static final int MIR_UnaryAcc_traits = Operator.none;
  /** Shared traits for operators of the MIR_Compare InstructionFormat */
  public static final int MIR_Compare_traits = Operator.none;
  /** Shared traits for operators of the MIR_CompareExchange InstructionFormat */
  public static final int MIR_CompareExchange_traits = Operator.none;
  /** Shared traits for operators of the MIR_CompareExchange8B InstructionFormat */
  public static final int MIR_CompareExchange8B_traits = Operator.none;
  /** Shared traits for operators of the MIR_Trap InstructionFormat */
  public static final int MIR_Trap_traits = Operator.none;
  /** Shared traits for operators of the MIR_TrapIf InstructionFormat */
  public static final int MIR_TrapIf_traits = Operator.none;
  /** Shared traits for operators of the MIR_Branch InstructionFormat */
  public static final int MIR_Branch_traits = Operator.none;
  /** Shared traits for operators of the MIR_CondBranch InstructionFormat */
  public static final int MIR_CondBranch_traits = Operator.none;
  /** Shared traits for operators of the MIR_CondBranch2 InstructionFormat */
  public static final int MIR_CondBranch2_traits = Operator.none;
  /** Shared traits for operators of the MIR_Call InstructionFormat */
  public static final int MIR_Call_traits = Operator.varUses;
  /** Shared traits for operators of the MIR_Empty InstructionFormat */
  public static final int MIR_Empty_traits = Operator.none;
  /** Shared traits for operators of the MIR_Return InstructionFormat */
  public static final int MIR_Return_traits = Operator.none;
  /** Shared traits for operators of the MIR_Set InstructionFormat */
  public static final int MIR_Set_traits = Operator.none;
  /** Shared traits for operators of the MIR_Test InstructionFormat */
  public static final int MIR_Test_traits = Operator.none;
  /** Shared traits for operators of the MIR_Nullary InstructionFormat */
  public static final int MIR_Nullary_traits = Operator.none;
  /** Shared traits for operators of the MIR_UnaryNoRes InstructionFormat */
  public static final int MIR_UnaryNoRes_traits = Operator.none;
  /** Shared traits for operators of the MIR_Unary InstructionFormat */
  public static final int MIR_Unary_traits = Operator.none;
  /** Shared traits for operators of the MIR_XChng InstructionFormat */
  public static final int MIR_XChng_traits = Operator.none;
  /** Shared traits for operators of the MIR_DoubleShift InstructionFormat */
  public static final int MIR_DoubleShift_traits = Operator.none;
  /** Shared traits for operators of the MIR_CaseLabel InstructionFormat */
  public static final int MIR_CaseLabel_traits = Operator.none;
  /** Shared traits for operators of the MIR_FSave InstructionFormat */
  public static final int MIR_FSave_traits = Operator.none;
  /** Shared traits for operators of the MIR_RDTSC InstructionFormat */
  public static final int MIR_RDTSC_traits = Operator.none;
  /** Shared traits for operators of the MIR_CacheOp InstructionFormat */
  public static final int MIR_CacheOp_traits = Operator.none;

}

