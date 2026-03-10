
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, CommonOperands.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir.ppc;

import org.vmmagic.pragma.Pure;

/**
 * Lookup used to see if an operator is a BranchProfile (which is BranchProfileOperand)
 */
public final class BranchProfileCarrierLookup {
  /** Look up table */
  private static final int[] table = {
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
           , -1    // NullCheck
           , -1    // ZeroCheck
           , -1    // BoundsCheck
           , -1    // StoreCheck
           , -1    // TypeCheck
           , -1    // InstanceOf
           , -1    // Trap
           , -1    // TrapIf
           , 5    // IfCmp
           , -1    // IfCmp2
           , 4    // InlineGuard
           , 4    // BooleanCmp
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
           , -1    // MIR_Load
           , -1    // MIR_LoadUpdate
           , -1    // MIR_Store
           , -1    // MIR_StoreUpdate
           , -1    // MIR_CacheOp
           , -1    // MIR_Move
           , -1    // MIR_Trap
           , -1    // MIR_DataInt
           , -1    // MIR_DataLabel
           , -1    // MIR_Branch
           , 3    // MIR_CondBranch
           , -1    // MIR_CondBranch2
           , -1    // MIR_Call
           , -1    // MIR_CondCall
           , -1    // MIR_Return
           , -1    // MIR_Empty
           , -1    // MIR_Nullary
           , -1    // MIR_Unary
           , -1    // MIR_Binary
           , -1    // MIR_Condition
           , -1    // MIR_Ternary
           , -1    // MIR_LowTableSwitch
           , -1    // MIR_RotateAndMask
        };

  /**
   * Perform table lookup
   * @param index the index to lookup
   * @return the index into the instruction operands that carries the BranchProfile
   *   or -1 if not carried
   */
  @Pure
  public static int lookup(int index) {
    return table[index];
  }
}
