
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, InstructionFormatList.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir;

import org.jikesrvm.Configuration;
import org.jikesrvm.compilers.opt.ir.operand.ia32.IA32ConditionOperand; //NOPMD
import org.jikesrvm.compilers.opt.ir.operand.*;

/**
 * The Goto InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class Goto extends InstructionFormat {
  /**
   * InstructionFormat identification method for Goto.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is Goto or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for Goto.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is Goto or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == Goto_format;
  }

  /**
   * Get the operand called Target from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static BranchOperand getTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Goto");
    return (BranchOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Target from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static BranchOperand getClearTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Goto");
    return (BranchOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Target in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Target the operand to store
   */
  public static void setTarget(Instruction i, BranchOperand Target) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Goto");
    i.putOperand(0, Target);
  }
  /**
   * Return the index of the operand called Target
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Target
   *         in the argument instruction
   */
  public static int indexOfTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Goto");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Target?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Target or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Goto");
    return i.getOperand(0) != null;
  }


  /**
   * Create an instruction of the Goto instruction format.
   * @param o the instruction's operator
   * @param Target the instruction's Target operand
   * @return the newly created Goto instruction
   */
  public static Instruction create(Operator o
                   , BranchOperand Target
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Goto");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Target);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Goto instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Target the instruction's Target operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , BranchOperand Target
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Goto");
    i.operator = o;
    i.putOperand(0, Target);
    return i;
  }
}

