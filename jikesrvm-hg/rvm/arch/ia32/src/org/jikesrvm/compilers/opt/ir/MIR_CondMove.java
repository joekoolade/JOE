
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
 * The MIR_CondMove InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_CondMove extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_CondMove.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_CondMove or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_CondMove.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_CondMove or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_CondMove_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static Operand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return (Operand) i.getOperand(0);
  }
  /**
   * Get the operand called Result from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static Operand getClearResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return (Operand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Result in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Result the operand to store
   */
  public static void setResult(Instruction i, Operand Result) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    i.putOperand(0, Result);
  }
  /**
   * Return the index of the operand called Result
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Result
   *         in the argument instruction
   */
  public static int indexOfResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Result?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Result or <code>false</code>
   *         if it does not.
   */
  public static boolean hasResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Value from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getClearValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Value in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value the operand to store
   */
  public static void setValue(Instruction i, Operand Value) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    i.putOperand(1, Value);
  }
  /**
   * Return the index of the operand called Value
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value
   *         in the argument instruction
   */
  public static int indexOfValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Value?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Value or <code>false</code>
   *         if it does not.
   */
  public static boolean hasValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Cond from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static IA32ConditionOperand getCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return (IA32ConditionOperand) i.getOperand(2);
  }
  /**
   * Get the operand called Cond from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static IA32ConditionOperand getClearCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return (IA32ConditionOperand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Cond in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond the operand to store
   */
  public static void setCond(Instruction i, IA32ConditionOperand Cond) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    i.putOperand(2, Cond);
  }
  /**
   * Return the index of the operand called Cond
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Cond
   *         in the argument instruction
   */
  public static int indexOfCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Cond?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Cond or <code>false</code>
   *         if it does not.
   */
  public static boolean hasCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondMove");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the MIR_CondMove instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @return the newly created MIR_CondMove instruction
   */
  public static Instruction create(Operator o
                   , Operand Result
                   , Operand Value
                   , IA32ConditionOperand Cond
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondMove");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Value);
    i.putOperand(2, Cond);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CondMove instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Result
                   , Operand Value
                   , IA32ConditionOperand Cond
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondMove");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Value);
    i.putOperand(2, Cond);
    return i;
  }
}

