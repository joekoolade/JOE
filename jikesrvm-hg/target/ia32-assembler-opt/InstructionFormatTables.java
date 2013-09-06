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

/**
 * Static tables describing the format of each low-level instruction
 * that opt compiler understands.  For each instruction, there are two
 * static arrays:
 * <UL>
 *  <LI> <format>ParameterNames is the list of names of operands
 *  <LI> <format>ParameterTypes is the list of types of operands
 * </UL>
 */
class InstructionFormatTables {


  /**
   * The parameter names of MIR_LowTableSwitch
   */
  static final String[] MIR_LowTableSwitchParameterNames = new String[]{
    "Index",
    "MethodStart",
  };

  /**
   * The parameter types of MIR_LowTableSwitch
   */
  static final String[] MIR_LowTableSwitchParameterTypes = new String[]{
    "RegisterOperand",
    "RegisterOperand",
  };


  /**
   * The parameter names of MIR_Move
   */
  static final String[] MIR_MoveParameterNames = new String[]{
    "Result",
    "Value",
  };

  /**
   * The parameter types of MIR_Move
   */
  static final String[] MIR_MoveParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_CondMove
   */
  static final String[] MIR_CondMoveParameterNames = new String[]{
    "Result",
    "Value",
    "Cond",
  };

  /**
   * The parameter types of MIR_CondMove
   */
  static final String[] MIR_CondMoveParameterTypes = new String[]{
    "Operand",
    "Operand",
    "IA32ConditionOperand",
  };


  /**
   * The parameter names of MIR_Lea
   */
  static final String[] MIR_LeaParameterNames = new String[]{
    "Result",
    "Value",
  };

  /**
   * The parameter types of MIR_Lea
   */
  static final String[] MIR_LeaParameterTypes = new String[]{
    "RegisterOperand",
    "MemoryOperand",
  };


  /**
   * The parameter names of MIR_BinaryAcc
   */
  static final String[] MIR_BinaryAccParameterNames = new String[]{
    "Result",
    "Value",
  };

  /**
   * The parameter types of MIR_BinaryAcc
   */
  static final String[] MIR_BinaryAccParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_Divide
   */
  static final String[] MIR_DivideParameterNames = new String[]{
    "Result1",
    "Result2",
    "Value",
    "Guard",
  };

  /**
   * The parameter types of MIR_Divide
   */
  static final String[] MIR_DivideParameterTypes = new String[]{
    "Operand",
    "Operand",
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_Multiply
   */
  static final String[] MIR_MultiplyParameterNames = new String[]{
    "Result1",
    "Result2",
    "Value",
  };

  /**
   * The parameter types of MIR_Multiply
   */
  static final String[] MIR_MultiplyParameterTypes = new String[]{
    "Operand",
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_ConvertDW2QW
   */
  static final String[] MIR_ConvertDW2QWParameterNames = new String[]{
    "Result1",
    "Result2",
  };

  /**
   * The parameter types of MIR_ConvertDW2QW
   */
  static final String[] MIR_ConvertDW2QWParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_UnaryAcc
   */
  static final String[] MIR_UnaryAccParameterNames = new String[]{
    "Result",
  };

  /**
   * The parameter types of MIR_UnaryAcc
   */
  static final String[] MIR_UnaryAccParameterTypes = new String[]{
    "Operand",
  };


  /**
   * The parameter names of MIR_Compare
   */
  static final String[] MIR_CompareParameterNames = new String[]{
    "Val1",
    "Val2",
  };

  /**
   * The parameter types of MIR_Compare
   */
  static final String[] MIR_CompareParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_CompareExchange
   */
  static final String[] MIR_CompareExchangeParameterNames = new String[]{
    "OldValue",
    "MemAddr",
    "NewValue",
  };

  /**
   * The parameter types of MIR_CompareExchange
   */
  static final String[] MIR_CompareExchangeParameterTypes = new String[]{
    "RegisterOperand",
    "MemoryOperand",
    "RegisterOperand",
  };


  /**
   * The parameter names of MIR_CompareExchange8B
   */
  static final String[] MIR_CompareExchange8BParameterNames = new String[]{
    "OldValueHigh",
    "OldValueLow",
    "MemAddr",
    "NewValueHigh",
    "NewValueLow",
  };

  /**
   * The parameter types of MIR_CompareExchange8B
   */
  static final String[] MIR_CompareExchange8BParameterTypes = new String[]{
    "RegisterOperand",
    "RegisterOperand",
    "MemoryOperand",
    "RegisterOperand",
    "RegisterOperand",
  };


  /**
   * The parameter names of MIR_Trap
   */
  static final String[] MIR_TrapParameterNames = new String[]{
    "GuardResult",
    "TrapCode",
  };

  /**
   * The parameter types of MIR_Trap
   */
  static final String[] MIR_TrapParameterTypes = new String[]{
    "RegisterOperand",
    "TrapCodeOperand",
  };


  /**
   * The parameter names of MIR_TrapIf
   */
  static final String[] MIR_TrapIfParameterNames = new String[]{
    "GuardResult",
    "Val1",
    "Val2",
    "Cond",
    "TrapCode",
  };

  /**
   * The parameter types of MIR_TrapIf
   */
  static final String[] MIR_TrapIfParameterTypes = new String[]{
    "RegisterOperand",
    "Operand",
    "Operand",
    "IA32ConditionOperand",
    "TrapCodeOperand",
  };


  /**
   * The parameter names of MIR_Branch
   */
  static final String[] MIR_BranchParameterNames = new String[]{
    "Target",
  };

  /**
   * The parameter types of MIR_Branch
   */
  static final String[] MIR_BranchParameterTypes = new String[]{
    "BranchOperand",
  };


  /**
   * The parameter names of MIR_CondBranch
   */
  static final String[] MIR_CondBranchParameterNames = new String[]{
    "Cond",
    "Target",
    "BranchProfile",
  };

  /**
   * The parameter types of MIR_CondBranch
   */
  static final String[] MIR_CondBranchParameterTypes = new String[]{
    "IA32ConditionOperand",
    "BranchOperand",
    "BranchProfileOperand",
  };


  /**
   * The parameter names of MIR_CondBranch2
   */
  static final String[] MIR_CondBranch2ParameterNames = new String[]{
    "Cond1",
    "Target1",
    "BranchProfile1",
    "Cond2",
    "Target2",
    "BranchProfile2",
  };

  /**
   * The parameter types of MIR_CondBranch2
   */
  static final String[] MIR_CondBranch2ParameterTypes = new String[]{
    "IA32ConditionOperand",
    "BranchOperand",
    "BranchProfileOperand",
    "IA32ConditionOperand",
    "BranchOperand",
    "BranchProfileOperand",
  };


  /**
   * The parameter names of MIR_Call
   */
  static final String[] MIR_CallParameterNames = new String[]{
    "Result",
    "Result2",
    "Target",
    "Method",
  };

  /**
   * The parameter types of MIR_Call
   */
  static final String[] MIR_CallParameterTypes = new String[]{
    "RegisterOperand",
    "RegisterOperand",
    "Operand",
    "MethodOperand",
  };


  /**
   * The parameter names of MIR_Empty
   */
  static final String[] MIR_EmptyParameterNames = new String[]{
  };

  /**
   * The parameter types of MIR_Empty
   */
  static final String[] MIR_EmptyParameterTypes = new String[]{
  };


  /**
   * The parameter names of MIR_Return
   */
  static final String[] MIR_ReturnParameterNames = new String[]{
    "PopBytes",
    "Val",
    "Val2",
  };

  /**
   * The parameter types of MIR_Return
   */
  static final String[] MIR_ReturnParameterTypes = new String[]{
    "IntConstantOperand",
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_Set
   */
  static final String[] MIR_SetParameterNames = new String[]{
    "Result",
    "Cond",
  };

  /**
   * The parameter types of MIR_Set
   */
  static final String[] MIR_SetParameterTypes = new String[]{
    "Operand",
    "IA32ConditionOperand",
  };


  /**
   * The parameter names of MIR_Test
   */
  static final String[] MIR_TestParameterNames = new String[]{
    "Val1",
    "Val2",
  };

  /**
   * The parameter types of MIR_Test
   */
  static final String[] MIR_TestParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_Nullary
   */
  static final String[] MIR_NullaryParameterNames = new String[]{
    "Result",
  };

  /**
   * The parameter types of MIR_Nullary
   */
  static final String[] MIR_NullaryParameterTypes = new String[]{
    "Operand",
  };


  /**
   * The parameter names of MIR_UnaryNoRes
   */
  static final String[] MIR_UnaryNoResParameterNames = new String[]{
    "Val",
  };

  /**
   * The parameter types of MIR_UnaryNoRes
   */
  static final String[] MIR_UnaryNoResParameterTypes = new String[]{
    "Operand",
  };


  /**
   * The parameter names of MIR_Unary
   */
  static final String[] MIR_UnaryParameterNames = new String[]{
    "Result",
    "Val",
  };

  /**
   * The parameter types of MIR_Unary
   */
  static final String[] MIR_UnaryParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_XChng
   */
  static final String[] MIR_XChngParameterNames = new String[]{
    "Val1",
    "Val2",
  };

  /**
   * The parameter types of MIR_XChng
   */
  static final String[] MIR_XChngParameterTypes = new String[]{
    "Operand",
    "Operand",
  };


  /**
   * The parameter names of MIR_DoubleShift
   */
  static final String[] MIR_DoubleShiftParameterNames = new String[]{
    "Result",
    "Source",
    "BitsToShift",
  };

  /**
   * The parameter types of MIR_DoubleShift
   */
  static final String[] MIR_DoubleShiftParameterTypes = new String[]{
    "Operand",
    "RegisterOperand",
    "Operand",
  };


  /**
   * The parameter names of MIR_CaseLabel
   */
  static final String[] MIR_CaseLabelParameterNames = new String[]{
    "Index",
    "Target",
  };

  /**
   * The parameter types of MIR_CaseLabel
   */
  static final String[] MIR_CaseLabelParameterTypes = new String[]{
    "IntConstantOperand",
    "Operand",
  };


  /**
   * The parameter names of MIR_FSave
   */
  static final String[] MIR_FSaveParameterNames = new String[]{
    "Destination",
  };

  /**
   * The parameter types of MIR_FSave
   */
  static final String[] MIR_FSaveParameterTypes = new String[]{
    "Operand",
  };


  /**
   * The parameter names of MIR_RDTSC
   */
  static final String[] MIR_RDTSCParameterNames = new String[]{
    "Dest1",
    "Dest2",
  };

  /**
   * The parameter types of MIR_RDTSC
   */
  static final String[] MIR_RDTSCParameterTypes = new String[]{
    "RegisterOperand",
    "RegisterOperand",
  };


  /**
   * The parameter names of MIR_CacheOp
   */
  static final String[] MIR_CacheOpParameterNames = new String[]{
    "Address",
  };

  /**
   * The parameter types of MIR_CacheOp
   */
  static final String[] MIR_CacheOpParameterTypes = new String[]{
    "Operand",
  };

}
