
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
package org.jikesrvm.compilers.opt.ir.ppc;

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

  /** Typecode for the MIR_Load InstructionFormat */
  public static final byte MIR_Load_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+0;
  /** Typecode for the MIR_LoadUpdate InstructionFormat */
  public static final byte MIR_LoadUpdate_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+1;
  /** Typecode for the MIR_Store InstructionFormat */
  public static final byte MIR_Store_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+2;
  /** Typecode for the MIR_StoreUpdate InstructionFormat */
  public static final byte MIR_StoreUpdate_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+3;
  /** Typecode for the MIR_CacheOp InstructionFormat */
  public static final byte MIR_CacheOp_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+4;
  /** Typecode for the MIR_Move InstructionFormat */
  public static final byte MIR_Move_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+5;
  /** Typecode for the MIR_Trap InstructionFormat */
  public static final byte MIR_Trap_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+6;
  /** Typecode for the MIR_DataInt InstructionFormat */
  public static final byte MIR_DataInt_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+7;
  /** Typecode for the MIR_DataLabel InstructionFormat */
  public static final byte MIR_DataLabel_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+8;
  /** Typecode for the MIR_Branch InstructionFormat */
  public static final byte MIR_Branch_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+9;
  /** Typecode for the MIR_CondBranch InstructionFormat */
  public static final byte MIR_CondBranch_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+10;
  /** Typecode for the MIR_CondBranch2 InstructionFormat */
  public static final byte MIR_CondBranch2_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+11;
  /** Typecode for the MIR_Call InstructionFormat */
  public static final byte MIR_Call_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+12;
  /** Typecode for the MIR_CondCall InstructionFormat */
  public static final byte MIR_CondCall_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+13;
  /** Typecode for the MIR_Return InstructionFormat */
  public static final byte MIR_Return_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+14;
  /** Typecode for the MIR_Empty InstructionFormat */
  public static final byte MIR_Empty_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+15;
  /** Typecode for the MIR_Nullary InstructionFormat */
  public static final byte MIR_Nullary_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+16;
  /** Typecode for the MIR_Unary InstructionFormat */
  public static final byte MIR_Unary_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+17;
  /** Typecode for the MIR_Binary InstructionFormat */
  public static final byte MIR_Binary_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+18;
  /** Typecode for the MIR_Condition InstructionFormat */
  public static final byte MIR_Condition_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+19;
  /** Typecode for the MIR_Ternary InstructionFormat */
  public static final byte MIR_Ternary_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+20;
  /** Typecode for the MIR_LowTableSwitch InstructionFormat */
  public static final byte MIR_LowTableSwitch_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+21;
  /** Typecode for the MIR_RotateAndMask InstructionFormat */
  public static final byte MIR_RotateAndMask_format = ARCH_INDEPENDENT_INSTR_FORMAT_END_format+22;

  /** Shared traits for operators of the MIR_Load InstructionFormat */
  public static final int MIR_Load_traits = Operator.none;
  /** Shared traits for operators of the MIR_LoadUpdate InstructionFormat */
  public static final int MIR_LoadUpdate_traits = Operator.none;
  /** Shared traits for operators of the MIR_Store InstructionFormat */
  public static final int MIR_Store_traits = Operator.none;
  /** Shared traits for operators of the MIR_StoreUpdate InstructionFormat */
  public static final int MIR_StoreUpdate_traits = Operator.none;
  /** Shared traits for operators of the MIR_CacheOp InstructionFormat */
  public static final int MIR_CacheOp_traits = Operator.none;
  /** Shared traits for operators of the MIR_Move InstructionFormat */
  public static final int MIR_Move_traits = Operator.none;
  /** Shared traits for operators of the MIR_Trap InstructionFormat */
  public static final int MIR_Trap_traits = Operator.none;
  /** Shared traits for operators of the MIR_DataInt InstructionFormat */
  public static final int MIR_DataInt_traits = Operator.none;
  /** Shared traits for operators of the MIR_DataLabel InstructionFormat */
  public static final int MIR_DataLabel_traits = Operator.none;
  /** Shared traits for operators of the MIR_Branch InstructionFormat */
  public static final int MIR_Branch_traits = Operator.none;
  /** Shared traits for operators of the MIR_CondBranch InstructionFormat */
  public static final int MIR_CondBranch_traits = Operator.none;
  /** Shared traits for operators of the MIR_CondBranch2 InstructionFormat */
  public static final int MIR_CondBranch2_traits = Operator.none;
  /** Shared traits for operators of the MIR_Call InstructionFormat */
  public static final int MIR_Call_traits = Operator.varUses;
  /** Shared traits for operators of the MIR_CondCall InstructionFormat */
  public static final int MIR_CondCall_traits = Operator.varUses;
  /** Shared traits for operators of the MIR_Return InstructionFormat */
  public static final int MIR_Return_traits = Operator.none;
  /** Shared traits for operators of the MIR_Empty InstructionFormat */
  public static final int MIR_Empty_traits = Operator.none;
  /** Shared traits for operators of the MIR_Nullary InstructionFormat */
  public static final int MIR_Nullary_traits = Operator.none;
  /** Shared traits for operators of the MIR_Unary InstructionFormat */
  public static final int MIR_Unary_traits = Operator.none;
  /** Shared traits for operators of the MIR_Binary InstructionFormat */
  public static final int MIR_Binary_traits = Operator.none;
  /** Shared traits for operators of the MIR_Condition InstructionFormat */
  public static final int MIR_Condition_traits = Operator.none;
  /** Shared traits for operators of the MIR_Ternary InstructionFormat */
  public static final int MIR_Ternary_traits = Operator.none;
  /** Shared traits for operators of the MIR_LowTableSwitch InstructionFormat */
  public static final int MIR_LowTableSwitch_traits = Operator.varUses;
  /** Shared traits for operators of the MIR_RotateAndMask InstructionFormat */
  public static final int MIR_RotateAndMask_traits = Operator.none;

}

