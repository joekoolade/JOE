
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
 * The MIR_Multiply InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_Multiply extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_Multiply.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_Multiply or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_Multiply.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_Multiply or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_Multiply_format;
  }

  /**
   * Get the operand called Result1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result1
   */
  public static Operand getResult1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return (Operand) i.getOperand(0);
  }
  /**
   * Get the operand called Result1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result1
   */
  public static Operand getClearResult1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return (Operand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Result1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Result1 the operand to store
   */
  public static void setResult1(Instruction i, Operand Result1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    i.putOperand(0, Result1);
  }
  /**
   * Return the index of the operand called Result1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Result1
   *         in the argument instruction
   */
  public static int indexOfResult1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Result1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Result1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasResult1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Result2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result2
   */
  public static Operand getResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Result2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result2
   */
  public static Operand getClearResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Result2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Result2 the operand to store
   */
  public static void setResult2(Instruction i, Operand Result2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    i.putOperand(1, Result2);
  }
  /**
   * Return the index of the operand called Result2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Result2
   *         in the argument instruction
   */
  public static int indexOfResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Result2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Result2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Value from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getClearValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return (Operand) i.getClearOperand(2);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    i.putOperand(2, Value);
  }
  /**
   * Return the index of the operand called Value
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value
   *         in the argument instruction
   */
  public static int indexOfValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return 2;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Multiply");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the MIR_Multiply instruction format.
   * @param o the instruction's operator
   * @param Result1 the instruction's Result1 operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @return the newly created MIR_Multiply instruction
   */
  public static Instruction create(Operator o
                   , Operand Result1
                   , Operand Result2
                   , Operand Value
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Multiply");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result1);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Multiply instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result1 the instruction's Result1 operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Result1
                   , Operand Result2
                   , Operand Value
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Multiply");
    i.operator = o;
    i.putOperand(0, Result1);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    return i;
  }
}

