
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, CommonOperands.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir;

import org.jikesrvm.Configuration;
import org.jikesrvm.compilers.opt.ir.operand.*;

/**
 * InstructionFormats that have a GuardResult (which is RegisterOperand)
 */
public final class GuardResultCarrier extends InstructionFormat {
  private static final int[] _index = {
           -1    // Unassigned
           , -1    // Move
           , -1    // Return
           , -1    // Prologue
           , -1    // InstrumentedCounter
           , -1    // Empty
           , -1    // Nullary
           , -1    // New
           , -1    // NewArray
           , -1    // Multianewarray
           , -1    // Athrow
           , -1    // MonitorOp
           , -1    // CacheOp
           , 0    // NullCheck
           , 0    // ZeroCheck
           , 0    // BoundsCheck
           , 0    // StoreCheck
           , -1    // TypeCheck
           , -1    // InstanceOf
           , 0    // Trap
           , 0    // TrapIf
           , 0    // IfCmp
           , 0    // IfCmp2
           , -1    // InlineGuard
           , -1    // BooleanCmp
           , -1    // CondMove
           , -1    // Goto
           , -1    // Label
           , -1    // BBend
           , -1    // Unary
           , -1    // GuardedUnary
           , -1    // Binary
           , -1    // GuardedBinary
           , -1    // GuardedSet
           , -1    // ALoad
           , -1    // GetField
           , -1    // GetStatic
           , -1    // Load
           , -1    // AStore
           , -1    // PutField
           , -1    // PutStatic
           , -1    // Store
           , -1    // Prepare
           , -1    // Attempt
           , -1    // Call
           , -1    // TableSwitch
           , -1    // LookupSwitch
           , -1    // LowTableSwitch
           , -1    // Phi
           , -1    // OsrBarrier
           , -1    // OsrPoint
           , -1    // MIR_LowTableSwitch
           , -1    // MIR_Move
           , -1    // MIR_CondMove
           , -1    // MIR_Lea
           , -1    // MIR_BinaryAcc
           , -1    // MIR_Divide
           , -1    // MIR_Multiply
           , -1    // MIR_ConvertDW2QW
           , -1    // MIR_UnaryAcc
           , -1    // MIR_Compare
           , -1    // MIR_CompareExchange
           , -1    // MIR_CompareExchange8B
           , 0    // MIR_Trap
           , 0    // MIR_TrapIf
           , -1    // MIR_Branch
           , -1    // MIR_CondBranch
           , -1    // MIR_CondBranch2
           , -1    // MIR_Call
           , -1    // MIR_Empty
           , -1    // MIR_Return
           , -1    // MIR_Set
           , -1    // MIR_Test
           , -1    // MIR_Nullary
           , -1    // MIR_UnaryNoRes
           , -1    // MIR_Unary
           , -1    // MIR_XChng
           , -1    // MIR_DoubleShift
           , -1    // MIR_CaseLabel
           , -1    // MIR_FSave
           , -1    // MIR_RDTSC
           , -1    // MIR_CacheOp
        };

  /**
   * Does the instruction belong to an instruction format that
   * has an operand called GuardResult?
   * @param i the instruction to test
   * @return <code>true</code> if the instruction's instruction
   *         format has an operand called GuardResult and
   *         <code>false</code> if it does not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * Does the operator belong to an instruction format that
   * has an operand called GuardResult?
   * @param o the operator to test
   * @return <code>true</code> if the instruction's instruction
   *         format has an operand called GuardResult and
   *         <code>false</code> if it does not.
   */
  public static boolean conforms(Operator o) {
    return _index[o.format] != -1;
  }

  /**
   * Get the operand called GuardResult from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardResultCarrier");
    int index = _index[i.operator.format];
    return (RegisterOperand) i.getOperand(index);
  }
  /**
   * Get the operand called GuardResult from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getClearGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardResultCarrier");
    int index = _index[i.operator.format];
    return (RegisterOperand) i.getClearOperand(index);
  }
  /**
   * Set the operand called GuardResult in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param op the operand to store
   */
  public static void setGuardResult(Instruction i, RegisterOperand op) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardResultCarrier");
    int index = _index[i.operator.format];
    i.putOperand(index, op);
  }
  /**
   * Return the index of the operand called GuardResult
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called GuardResult
   *         in the argument instruction
   */
  public static int indexOfGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardResultCarrier");
    return _index[i.operator.format];
  }
  /**
   * Does the argument instruction have a non-null
   * operand named GuardResult?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named GuardResult or <code>false</code>
   *         if it does not.
   */
  public static boolean hasGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardResultCarrier");
    int index = _index[i.operator.format];
    return i.getOperand(index) != null;
  }
}

