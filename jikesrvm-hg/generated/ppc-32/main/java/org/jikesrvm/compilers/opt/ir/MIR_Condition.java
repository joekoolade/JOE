
/*
 * THIS FILE IS MACHINE_GENERATED. DO NOT EDIT.
 * See InstructionFormats.template, InstructionFormatList.dat,
 * OperatorList.dat, etc.
 */

package org.jikesrvm.compilers.opt.ir;

import org.jikesrvm.Configuration;
import org.jikesrvm.compilers.opt.ir.operand.ppc.PowerPCConditionOperand;
import org.jikesrvm.compilers.opt.ir.operand.ppc.PowerPCTrapOperand;
import org.jikesrvm.compilers.opt.ir.operand.*;

/**
 * The MIR_Condition InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_Condition extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_Condition.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_Condition or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_Condition.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_Condition or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_Condition_format;
  }

  /**
   * Get the operand called ResultBit from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called ResultBit
   */
  public static IntConstantOperand getResultBit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return (IntConstantOperand) i.getOperand(0);
  }
  /**
   * Get the operand called ResultBit from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called ResultBit
   */
  public static IntConstantOperand getClearResultBit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return (IntConstantOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called ResultBit in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param ResultBit the operand to store
   */
  public static void setResultBit(Instruction i, IntConstantOperand ResultBit) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    i.putOperand(0, ResultBit);
  }
  /**
   * Return the index of the operand called ResultBit
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called ResultBit
   *         in the argument instruction
   */
  public static int indexOfResultBit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named ResultBit?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named ResultBit or <code>false</code>
   *         if it does not.
   */
  public static boolean hasResultBit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Value1Bit from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value1Bit
   */
  public static IntConstantOperand getValue1Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return (IntConstantOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Value1Bit from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value1Bit
   */
  public static IntConstantOperand getClearValue1Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return (IntConstantOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Value1Bit in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value1Bit the operand to store
   */
  public static void setValue1Bit(Instruction i, IntConstantOperand Value1Bit) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    i.putOperand(1, Value1Bit);
  }
  /**
   * Return the index of the operand called Value1Bit
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value1Bit
   *         in the argument instruction
   */
  public static int indexOfValue1Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Value1Bit?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Value1Bit or <code>false</code>
   *         if it does not.
   */
  public static boolean hasValue1Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Value2Bit from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value2Bit
   */
  public static IntConstantOperand getValue2Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return (IntConstantOperand) i.getOperand(2);
  }
  /**
   * Get the operand called Value2Bit from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value2Bit
   */
  public static IntConstantOperand getClearValue2Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return (IntConstantOperand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Value2Bit in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value2Bit the operand to store
   */
  public static void setValue2Bit(Instruction i, IntConstantOperand Value2Bit) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    i.putOperand(2, Value2Bit);
  }
  /**
   * Return the index of the operand called Value2Bit
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value2Bit
   *         in the argument instruction
   */
  public static int indexOfValue2Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Value2Bit?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Value2Bit or <code>false</code>
   *         if it does not.
   */
  public static boolean hasValue2Bit(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Condition");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the MIR_Condition instruction format.
   * @param o the instruction's operator
   * @param ResultBit the instruction's ResultBit operand
   * @param Value1Bit the instruction's Value1Bit operand
   * @param Value2Bit the instruction's Value2Bit operand
   * @return the newly created MIR_Condition instruction
   */
  public static Instruction create(Operator o
                   , IntConstantOperand ResultBit
                   , IntConstantOperand Value1Bit
                   , IntConstantOperand Value2Bit
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Condition");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, ResultBit);
    i.putOperand(1, Value1Bit);
    i.putOperand(2, Value2Bit);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Condition instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param ResultBit the instruction's ResultBit operand
   * @param Value1Bit the instruction's Value1Bit operand
   * @param Value2Bit the instruction's Value2Bit operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , IntConstantOperand ResultBit
                   , IntConstantOperand Value1Bit
                   , IntConstantOperand Value2Bit
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Condition");
    i.operator = o;
    i.putOperand(0, ResultBit);
    i.putOperand(1, Value1Bit);
    i.putOperand(2, Value2Bit);
    return i;
  }
}

