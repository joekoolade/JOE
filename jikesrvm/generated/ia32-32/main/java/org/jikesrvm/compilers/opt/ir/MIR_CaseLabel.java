
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
 * The MIR_CaseLabel InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_CaseLabel extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_CaseLabel.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_CaseLabel or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_CaseLabel.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_CaseLabel or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_CaseLabel_format;
  }

  /**
   * Get the operand called Index from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Index
   */
  public static IntConstantOperand getIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return (IntConstantOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Index from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Index
   */
  public static IntConstantOperand getClearIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return (IntConstantOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Index in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Index the operand to store
   */
  public static void setIndex(Instruction i, IntConstantOperand Index) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    i.putOperand(0, Index);
  }
  /**
   * Return the index of the operand called Index
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Index
   *         in the argument instruction
   */
  public static int indexOfIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Index?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Index or <code>false</code>
   *         if it does not.
   */
  public static boolean hasIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Target from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static Operand getTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Target from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static Operand getClearTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Target in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Target the operand to store
   */
  public static void setTarget(Instruction i, Operand Target) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    i.putOperand(1, Target);
  }
  /**
   * Return the index of the operand called Target
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Target
   *         in the argument instruction
   */
  public static int indexOfTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CaseLabel");
    return i.getOperand(1) != null;
  }


  /**
   * Create an instruction of the MIR_CaseLabel instruction format.
   * @param o the instruction's operator
   * @param Index the instruction's Index operand
   * @param Target the instruction's Target operand
   * @return the newly created MIR_CaseLabel instruction
   */
  public static Instruction create(Operator o
                   , IntConstantOperand Index
                   , Operand Target
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CaseLabel");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Index);
    i.putOperand(1, Target);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CaseLabel instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Index the instruction's Index operand
   * @param Target the instruction's Target operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , IntConstantOperand Index
                   , Operand Target
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CaseLabel");
    i.operator = o;
    i.putOperand(0, Index);
    i.putOperand(1, Target);
    return i;
  }
}

