
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, InstructionFormatList.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir;

import org.jikesrvm.VM;
import org.jikesrvm.compilers.opt.OptimizingCompilerException;
import org.jikesrvm.compilers.opt.ir.operand.*;

/**
 * Abstract parent class of all InstructionFormat classes.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
public abstract class InstructionFormat {

  /**
   * Make all operand arrays created via the InstructionFormat constructors
   * at least this big to reduce the chance of having to resize the array
   * if the instruction is mutated.
   */
  static final int MIN_OPERAND_ARRAY_LENGTH = 5;

  /** Typecode for the Unassigned InstructionFormat */
  public static final byte Unassigned_format = 0;
  /** Typecode for the Move InstructionFormat */
  public static final byte Move_format = 1;
  /** Typecode for the Return InstructionFormat */
  public static final byte Return_format = 2;
  /** Typecode for the Prologue InstructionFormat */
  public static final byte Prologue_format = 3;
  /** Typecode for the InstrumentedCounter InstructionFormat */
  public static final byte InstrumentedCounter_format = 4;
  /** Typecode for the Empty InstructionFormat */
  public static final byte Empty_format = 5;
  /** Typecode for the Nullary InstructionFormat */
  public static final byte Nullary_format = 6;
  /** Typecode for the New InstructionFormat */
  public static final byte New_format = 7;
  /** Typecode for the NewArray InstructionFormat */
  public static final byte NewArray_format = 8;
  /** Typecode for the Multianewarray InstructionFormat */
  public static final byte Multianewarray_format = 9;
  /** Typecode for the Athrow InstructionFormat */
  public static final byte Athrow_format = 10;
  /** Typecode for the MonitorOp InstructionFormat */
  public static final byte MonitorOp_format = 11;
  /** Typecode for the CacheOp InstructionFormat */
  public static final byte CacheOp_format = 12;
  /** Typecode for the NullCheck InstructionFormat */
  public static final byte NullCheck_format = 13;
  /** Typecode for the ZeroCheck InstructionFormat */
  public static final byte ZeroCheck_format = 14;
  /** Typecode for the BoundsCheck InstructionFormat */
  public static final byte BoundsCheck_format = 15;
  /** Typecode for the StoreCheck InstructionFormat */
  public static final byte StoreCheck_format = 16;
  /** Typecode for the TypeCheck InstructionFormat */
  public static final byte TypeCheck_format = 17;
  /** Typecode for the InstanceOf InstructionFormat */
  public static final byte InstanceOf_format = 18;
  /** Typecode for the Trap InstructionFormat */
  public static final byte Trap_format = 19;
  /** Typecode for the TrapIf InstructionFormat */
  public static final byte TrapIf_format = 20;
  /** Typecode for the IfCmp InstructionFormat */
  public static final byte IfCmp_format = 21;
  /** Typecode for the IfCmp2 InstructionFormat */
  public static final byte IfCmp2_format = 22;
  /** Typecode for the InlineGuard InstructionFormat */
  public static final byte InlineGuard_format = 23;
  /** Typecode for the BooleanCmp InstructionFormat */
  public static final byte BooleanCmp_format = 24;
  /** Typecode for the CondMove InstructionFormat */
  public static final byte CondMove_format = 25;
  /** Typecode for the Goto InstructionFormat */
  public static final byte Goto_format = 26;
  /** Typecode for the Label InstructionFormat */
  public static final byte Label_format = 27;
  /** Typecode for the BBend InstructionFormat */
  public static final byte BBend_format = 28;
  /** Typecode for the Unary InstructionFormat */
  public static final byte Unary_format = 29;
  /** Typecode for the GuardedUnary InstructionFormat */
  public static final byte GuardedUnary_format = 30;
  /** Typecode for the Binary InstructionFormat */
  public static final byte Binary_format = 31;
  /** Typecode for the GuardedBinary InstructionFormat */
  public static final byte GuardedBinary_format = 32;
  /** Typecode for the GuardedSet InstructionFormat */
  public static final byte GuardedSet_format = 33;
  /** Typecode for the ALoad InstructionFormat */
  public static final byte ALoad_format = 34;
  /** Typecode for the GetField InstructionFormat */
  public static final byte GetField_format = 35;
  /** Typecode for the GetStatic InstructionFormat */
  public static final byte GetStatic_format = 36;
  /** Typecode for the Load InstructionFormat */
  public static final byte Load_format = 37;
  /** Typecode for the AStore InstructionFormat */
  public static final byte AStore_format = 38;
  /** Typecode for the PutField InstructionFormat */
  public static final byte PutField_format = 39;
  /** Typecode for the PutStatic InstructionFormat */
  public static final byte PutStatic_format = 40;
  /** Typecode for the Store InstructionFormat */
  public static final byte Store_format = 41;
  /** Typecode for the Prepare InstructionFormat */
  public static final byte Prepare_format = 42;
  /** Typecode for the Attempt InstructionFormat */
  public static final byte Attempt_format = 43;
  /** Typecode for the Call InstructionFormat */
  public static final byte Call_format = 44;
  /** Typecode for the TableSwitch InstructionFormat */
  public static final byte TableSwitch_format = 45;
  /** Typecode for the LookupSwitch InstructionFormat */
  public static final byte LookupSwitch_format = 46;
  /** Typecode for the LowTableSwitch InstructionFormat */
  public static final byte LowTableSwitch_format = 47;
  /** Typecode for the Phi InstructionFormat */
  public static final byte Phi_format = 48;
  /** Typecode for the OsrBarrier InstructionFormat */
  public static final byte OsrBarrier_format = 49;
  /** Typecode for the OsrPoint InstructionFormat */
  public static final byte OsrPoint_format = 50;
  /** Typecode for the ARCH_INDEPENDENT_INSTR_FORMAT_END InstructionFormat */
  public static final byte ARCH_INDEPENDENT_INSTR_FORMAT_END_format = 51;
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

  /** Shared traits for operators of the Move InstructionFormat */
  public static final int Move_traits = Operator.none;
  /** Shared traits for operators of the Return InstructionFormat */
  public static final int Return_traits = Operator.none;
  /** Shared traits for operators of the Prologue InstructionFormat */
  public static final int Prologue_traits = Operator.varDefs;
  /** Shared traits for operators of the InstrumentedCounter InstructionFormat */
  public static final int InstrumentedCounter_traits = Operator.none;
  /** Shared traits for operators of the Empty InstructionFormat */
  public static final int Empty_traits = Operator.none;
  /** Shared traits for operators of the Nullary InstructionFormat */
  public static final int Nullary_traits = Operator.none;
  /** Shared traits for operators of the New InstructionFormat */
  public static final int New_traits = Operator.none;
  /** Shared traits for operators of the NewArray InstructionFormat */
  public static final int NewArray_traits = Operator.none;
  /** Shared traits for operators of the Multianewarray InstructionFormat */
  public static final int Multianewarray_traits = Operator.varUses;
  /** Shared traits for operators of the Athrow InstructionFormat */
  public static final int Athrow_traits = Operator.none;
  /** Shared traits for operators of the MonitorOp InstructionFormat */
  public static final int MonitorOp_traits = Operator.none;
  /** Shared traits for operators of the CacheOp InstructionFormat */
  public static final int CacheOp_traits = Operator.none;
  /** Shared traits for operators of the NullCheck InstructionFormat */
  public static final int NullCheck_traits = Operator.none;
  /** Shared traits for operators of the ZeroCheck InstructionFormat */
  public static final int ZeroCheck_traits = Operator.none;
  /** Shared traits for operators of the BoundsCheck InstructionFormat */
  public static final int BoundsCheck_traits = Operator.none;
  /** Shared traits for operators of the StoreCheck InstructionFormat */
  public static final int StoreCheck_traits = Operator.none;
  /** Shared traits for operators of the TypeCheck InstructionFormat */
  public static final int TypeCheck_traits = Operator.none;
  /** Shared traits for operators of the InstanceOf InstructionFormat */
  public static final int InstanceOf_traits = Operator.none;
  /** Shared traits for operators of the Trap InstructionFormat */
  public static final int Trap_traits = Operator.none;
  /** Shared traits for operators of the TrapIf InstructionFormat */
  public static final int TrapIf_traits = Operator.none;
  /** Shared traits for operators of the IfCmp InstructionFormat */
  public static final int IfCmp_traits = Operator.none;
  /** Shared traits for operators of the IfCmp2 InstructionFormat */
  public static final int IfCmp2_traits = Operator.none;
  /** Shared traits for operators of the InlineGuard InstructionFormat */
  public static final int InlineGuard_traits = Operator.none;
  /** Shared traits for operators of the BooleanCmp InstructionFormat */
  public static final int BooleanCmp_traits = Operator.none;
  /** Shared traits for operators of the CondMove InstructionFormat */
  public static final int CondMove_traits = Operator.none;
  /** Shared traits for operators of the Goto InstructionFormat */
  public static final int Goto_traits = Operator.none;
  /** Shared traits for operators of the Label InstructionFormat */
  public static final int Label_traits = Operator.none;
  /** Shared traits for operators of the BBend InstructionFormat */
  public static final int BBend_traits = Operator.none;
  /** Shared traits for operators of the Unary InstructionFormat */
  public static final int Unary_traits = Operator.none;
  /** Shared traits for operators of the GuardedUnary InstructionFormat */
  public static final int GuardedUnary_traits = Operator.none;
  /** Shared traits for operators of the Binary InstructionFormat */
  public static final int Binary_traits = Operator.none;
  /** Shared traits for operators of the GuardedBinary InstructionFormat */
  public static final int GuardedBinary_traits = Operator.none;
  /** Shared traits for operators of the GuardedSet InstructionFormat */
  public static final int GuardedSet_traits = Operator.none;
  /** Shared traits for operators of the ALoad InstructionFormat */
  public static final int ALoad_traits = Operator.none;
  /** Shared traits for operators of the GetField InstructionFormat */
  public static final int GetField_traits = Operator.none;
  /** Shared traits for operators of the GetStatic InstructionFormat */
  public static final int GetStatic_traits = Operator.none;
  /** Shared traits for operators of the Load InstructionFormat */
  public static final int Load_traits = Operator.none;
  /** Shared traits for operators of the AStore InstructionFormat */
  public static final int AStore_traits = Operator.none;
  /** Shared traits for operators of the PutField InstructionFormat */
  public static final int PutField_traits = Operator.none;
  /** Shared traits for operators of the PutStatic InstructionFormat */
  public static final int PutStatic_traits = Operator.none;
  /** Shared traits for operators of the Store InstructionFormat */
  public static final int Store_traits = Operator.none;
  /** Shared traits for operators of the Prepare InstructionFormat */
  public static final int Prepare_traits = Operator.none;
  /** Shared traits for operators of the Attempt InstructionFormat */
  public static final int Attempt_traits = Operator.none;
  /** Shared traits for operators of the Call InstructionFormat */
  public static final int Call_traits = Operator.varUses;
  /** Shared traits for operators of the TableSwitch InstructionFormat */
  public static final int TableSwitch_traits = Operator.varUses;
  /** Shared traits for operators of the LookupSwitch InstructionFormat */
  public static final int LookupSwitch_traits = Operator.varUses;
  /** Shared traits for operators of the LowTableSwitch InstructionFormat */
  public static final int LowTableSwitch_traits = Operator.varUses;
  /** Shared traits for operators of the Phi InstructionFormat */
  public static final int Phi_traits = Operator.varUses;
  /** Shared traits for operators of the OsrBarrier InstructionFormat */
  public static final int OsrBarrier_traits = Operator.varUses;
  /** Shared traits for operators of the OsrPoint InstructionFormat */
  public static final int OsrPoint_traits = Operator.varUses;
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

  /**
   * Called to generate a (possibly fatal) error message
   * when it is detected that an InstructionFormat method
   * was invoked on an instruction/operator that does not
   * conform to that format.
   * @param i the instruction that failed to conform to the
   *          expected format.
   * @param name the name of the instruction format that the
   *             instruction was expected to conform to.
   */
  protected static void fail(Instruction i, String name) {
      VM.sysWrite("Instruction "+i+" improperly accessed as "+name+"\n");
      throw new OptimizingCompilerException();
    }

  /**
   * Called to generate a (possibly fatal) error message
   * when it is detected that an InstructionFormat method
   * was invoked on an operator that does not
   * conform to that format.
   * @param op the operator that failed to conform to the
   *          expected format.
   * @param name the name of the instruction format that the
   *             operator was expected to conform to.
   */
  protected static void fail(Operator op, String name) {
      VM.sysWrite("Improper attempt to create/mutate as "+name+"\n");
      throw new OptimizingCompilerException();
    }
}

