
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, CommonOperands.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir.ia32;

import org.vmmagic.pragma.Pure;

/**
 * Lookup used to see if an operator is a Location (which is LocationOperand)
 */
public final class LocationCarrierLookup {
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
           , -1    // IfCmp
           , -1    // IfCmp2
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
           , 3    // ALoad
           , 3    // GetField
           , 2    // GetStatic
           , 3    // Load
           , 3    // AStore
           , 3    // PutField
           , 2    // PutStatic
           , 3    // Store
           , 3    // Prepare
           , 5    // Attempt
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
           , -1    // MIR_Trap
           , -1    // MIR_TrapIf
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
   * Perform table lookup
   * @param index the index to lookup
   * @return the index into the instruction operands that carries the Location
   *   or -1 if not carried
   */
  @Pure
  public static int lookup(int index) {
    return table[index];
  }
}

