
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
 * The Multianewarray InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class Multianewarray extends InstructionFormat {
  /**
   * InstructionFormat identification method for Multianewarray.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is Multianewarray or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for Multianewarray.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is Multianewarray or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == Multianewarray_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Result from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getClearResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Result in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Result the operand to store
   */
  public static void setResult(Instruction i, RegisterOperand Result) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Type from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Type
   */
  public static TypeOperand getType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return (TypeOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Type from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Type
   */
  public static TypeOperand getClearType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return (TypeOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Type in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Type the operand to store
   */
  public static void setType(Instruction i, TypeOperand Type) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    i.putOperand(1, Type);
  }
  /**
   * Return the index of the operand called Type
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Type
   *         in the argument instruction
   */
  public static int indexOfType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Type?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Type or <code>false</code>
   *         if it does not.
   */
  public static boolean hasType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return i.getOperand(1) != null;
  }

  /**
   * Get the k'th operand called Dimension from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Dimension
   */
  public static Operand getDimension(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return (Operand) i.getOperand(2+k);
  }
  /**
   * Get the k'th operand called Dimension from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Dimension
   */
  public static Operand getClearDimension(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return (Operand) i.getClearOperand(2+k);
  }
  /**
   * Set the k'th operand called Dimension in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param k the index of the operand
   * @param o the operand to store
   */
  public static void setDimension(Instruction i, int k, Operand o) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    i.putOperand(2+k, o);
  }
  /**
   * Return the index of the k'th operand called Dimension
   * in the argument instruction.
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return the index of the k'th operand called Dimension
   *         in the argument instruction
   */
  public static int indexOfDimension(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return 2+k;
  }
  /**
   * Does the argument instruction have a non-null
   * k'th operand named Dimension?
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return <code>true</code> if the instruction has an non-null
   *         k'th operand named Dimension or <code>false</code>
   *         if it does not.
   */
  public static boolean hasDimension(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return i.getOperand(2+k) != null;
  }

  /**
   * Return the index of the first operand called Dimension
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the first operand called Dimension
   *         in the argument instruction
   */
  public static int indexOfDimensions(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return 2;
  }
  /**
   * Does the argument instruction have any operands
   * named Dimension?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has operands
   *         named Dimension or <code>false</code> if it does not.
   */
  public static boolean hasDimensions(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return i.getNumberOfOperands()-2 > 0 && i.getOperand(2) != null;
  }

  /**
   * How many variable-length operands called Dimensions
   * does the argument instruction have?
   * @param i the instruction to access
   * @return the number of operands called Dimensions the instruction has
   */
  public static int getNumberOfDimensions(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
    return i.getNumberOfOperands()-2;
  }

  /**
   * Change the number of Dimensions that may be stored in
   * the argument instruction to numVarOps.
   * @param i the instruction to access
   * @param numVarOps the new number of variable operands called Dimensions
   *        that may be stored in the instruction
   */
  public static void resizeNumberOfDimensions(Instruction i, int numVarOps)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Multianewarray");
  if (2+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
    i.resizeNumberOfOperands(2+numVarOps);
  else
    for (int j = 2+numVarOps; j < MIN_OPERAND_ARRAY_LENGTH; j++)
      i.putOperand(j, null);
  }

  /**
   * Create an instruction of the Multianewarray instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Type the instruction's Type operand
   * @param numVarOps the number of variable length operands that
   *                 will be stored in the insruction.
   * @return the newly created Multianewarray instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , TypeOperand Type
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Multianewarray");
    Instruction i = new Instruction(o, Math.max(2+numVarOps, MIN_OPERAND_ARRAY_LENGTH));
    i.putOperand(0, Result);
    i.putOperand(1, Type);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Multianewarray instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Type the instruction's Type operand
   * @param numVarOps the number of variable length operands that
   *                  will be stored in the insruction.
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , TypeOperand Type
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Multianewarray");
    if (2+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
      i.resizeNumberOfOperands(2+numVarOps);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Type);
    return i;
  }
}

