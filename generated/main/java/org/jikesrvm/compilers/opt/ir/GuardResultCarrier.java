
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, CommonOperands.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir;

import org.jikesrvm.Configuration;
import org.jikesrvm.VM;
import org.jikesrvm.compilers.opt.ir.operand.*;

/**
 * InstructionFormats that have a GuardResult (which is RegisterOperand)
 */
public final class GuardResultCarrier extends InstructionFormat {

  /**
   * Performs table lookup
   * @param index the index to lookup
   * @return the index into the instruction operands that carries the GuardResult
   *   or -1 if not carried
   */
  public static int lookup(int index) {
    if (VM.BuildForIA32) {
      return org.jikesrvm.compilers.opt.ir.ia32.GuardResultCarrierLookup.lookup(index);
    } else {
      if (VM.VerifyAssertions) VM._assert(VM.BuildForPowerPC);
      return org.jikesrvm.compilers.opt.ir.ppc.GuardResultCarrierLookup.lookup(index);
    }
  }

  /**
   * Does the instruction belong to an instruction format that
   * has an operand called GuardResult?
   * @param i the instruction to test
   * @return <code>true</code> if the instruction's instruction
   *         format has an operand called GuardResult and
   *         <code>false</code> if it does not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator());
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
    return lookup(o.format) != -1;
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
    int index = lookup(i.operator().format);
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
    int index = lookup(i.operator().format);
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
    int index = lookup(i.operator().format);
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
    return lookup(i.operator().format);
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
    int index = lookup(i.operator().format);
    return i.getOperand(index) != null;
  }
}

