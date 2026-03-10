
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
 * InstructionFormats that have a BranchProfile (which is BranchProfileOperand)
 */
public final class BranchProfileCarrier extends InstructionFormat {

  /**
   * Performs table lookup
   * @param index the index to lookup
   * @return the index into the instruction operands that carries the BranchProfile
   *   or -1 if not carried
   */
  public static int lookup(int index) {
    if (VM.BuildForIA32) {
      return org.jikesrvm.compilers.opt.ir.ia32.BranchProfileCarrierLookup.lookup(index);
    } else {
      if (VM.VerifyAssertions) VM._assert(VM.BuildForPowerPC);
      return org.jikesrvm.compilers.opt.ir.ppc.BranchProfileCarrierLookup.lookup(index);
    }
  }

  /**
   * Does the instruction belong to an instruction format that
   * has an operand called BranchProfile?
   * @param i the instruction to test
   * @return <code>true</code> if the instruction's instruction
   *         format has an operand called BranchProfile and
   *         <code>false</code> if it does not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator());
  }
  /**
   * Does the operator belong to an instruction format that
   * has an operand called BranchProfile?
   * @param o the operator to test
   * @return <code>true</code> if the instruction's instruction
   *         format has an operand called BranchProfile and
   *         <code>false</code> if it does not.
   */
  public static boolean conforms(Operator o) {
    return lookup(o.format) != -1;
  }

  /**
   * Get the operand called BranchProfile from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile
   */
  public static BranchProfileOperand getBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BranchProfileCarrier");
    int index = lookup(i.operator().format);
    return (BranchProfileOperand) i.getOperand(index);
  }
  /**
   * Get the operand called BranchProfile from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile
   */
  public static BranchProfileOperand getClearBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BranchProfileCarrier");
    int index = lookup(i.operator().format);
    return (BranchProfileOperand) i.getClearOperand(index);
  }
  /**
   * Set the operand called BranchProfile in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param op the operand to store
   */
  public static void setBranchProfile(Instruction i, BranchProfileOperand op) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BranchProfileCarrier");
    int index = lookup(i.operator().format);
    i.putOperand(index, op);
  }
  /**
   * Return the index of the operand called BranchProfile
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called BranchProfile
   *         in the argument instruction
   */
  public static int indexOfBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BranchProfileCarrier");
    return lookup(i.operator().format);
  }
  /**
   * Does the argument instruction have a non-null
   * operand named BranchProfile?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named BranchProfile or <code>false</code>
   *         if it does not.
   */
  public static boolean hasBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BranchProfileCarrier");
    int index = lookup(i.operator().format);
    return i.getOperand(index) != null;
  }
}
