
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, CommonOperands.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir.ppc;

import org.vmmagic.pragma.Pure;

/**
 * Lookup used to see if an operator is a Result (which is RegisterOperand)
 */
public final class ResultCarrierLookup {
  /** Look up table */
  private static final int[] table = {
           -1    // Unassigned
           , 0    // Move
           , -1    // Return
           , -1    // Prologue
           , -1    // InstrumentedCounter
           , -1    // Empty
           , 0    // Nullary
           , 0    // New
           , 0    // NewArray
           , 0    // Multianewarray
           , -1    // Athrow
           , -1    // MonitorOp
           , -1    // CacheOp
           , -1    // NullCheck
           , -1    // ZeroCheck
           , -1    // BoundsCheck
           , -1    // StoreCheck
           , 0    // TypeCheck
           , 0    // InstanceOf
           , -1    // Trap
           , -1    // TrapIf
           , -1    // IfCmp
           , -1    // IfCmp2
           , -1    // InlineGuard
           , 0    // BooleanCmp
           , 0    // CondMove
           , -1    // Goto
           , -1    // Label
           , -1    // BBend
           , 0    // Unary
           , 0    // GuardedUnary
           , 0    // Binary
           , 0    // GuardedBinary
           , -1    // GuardedSet
           , 0    // ALoad
           , 0    // GetField
           , 0    // GetStatic
           , 0    // Load
           , -1    // AStore
           , -1    // PutField
           , -1    // PutStatic
           , -1    // Store
           , 0    // Prepare
           , 0    // Attempt
           , 0    // Call
           , -1    // TableSwitch
           , -1    // LookupSwitch
           , -1    // LowTableSwitch
           , -1    // Phi
           , -1    // OsrBarrier
           , -1    // OsrPoint
           , 0    // MIR_Load
           , 0    // MIR_LoadUpdate
           , -1    // MIR_Store
           , -1    // MIR_StoreUpdate
           , -1    // MIR_CacheOp
           , 0    // MIR_Move
           , -1    // MIR_Trap
           , -1    // MIR_DataInt
           , -1    // MIR_DataLabel
           , -1    // MIR_Branch
           , -1    // MIR_CondBranch
           , -1    // MIR_CondBranch2
           , 0    // MIR_Call
           , 0    // MIR_CondCall
           , -1    // MIR_Return
           , -1    // MIR_Empty
           , 0    // MIR_Nullary
           , 0    // MIR_Unary
           , 0    // MIR_Binary
           , -1    // MIR_Condition
           , 0    // MIR_Ternary
           , -1    // MIR_LowTableSwitch
           , 0    // MIR_RotateAndMask
        };

  /**
   * Perform table lookup
   * @param index the index to lookup
   * @return the index into the instruction operands that carries the Result
   *   or -1 if not carried
   */
  @Pure
  public static int lookup(int index) {
    return table[index];
  }
}

