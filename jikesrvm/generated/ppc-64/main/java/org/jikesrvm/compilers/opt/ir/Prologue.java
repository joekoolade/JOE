
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
 * The Prologue InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class Prologue extends InstructionFormat {
  /**
   * InstructionFormat identification method for Prologue.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is Prologue or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for Prologue.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is Prologue or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == Prologue_format;
  }

  /**
   * Get the k'th operand called Formal from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Formal
   */
  public static RegisterOperand getFormal(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return (RegisterOperand) i.getOperand(0+k);
  }
  /**
   * Get the k'th operand called Formal from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Formal
   */
  public static RegisterOperand getClearFormal(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return (RegisterOperand) i.getClearOperand(0+k);
  }
  /**
   * Set the k'th operand called Formal in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param k the index of the operand
   * @param o the operand to store
   */
  public static void setFormal(Instruction i, int k, RegisterOperand o) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    i.putOperand(0+k, o);
  }
  /**
   * Return the index of the k'th operand called Formal
   * in the argument instruction.
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return the index of the k'th operand called Formal
   *         in the argument instruction
   */
  public static int indexOfFormal(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return 0+k;
  }
  /**
   * Does the argument instruction have a non-null
   * k'th operand named Formal?
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return <code>true</code> if the instruction has an non-null
   *         k'th operand named Formal or <code>false</code>
   *         if it does not.
   */
  public static boolean hasFormal(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return i.getOperand(0+k) != null;
  }

  /**
   * Return the index of the first operand called Formal
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the first operand called Formal
   *         in the argument instruction
   */
  public static int indexOfFormals(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return 0;
  }
  /**
   * Does the argument instruction have any operands
   * named Formal?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has operands
   *         named Formal or <code>false</code> if it does not.
   */
  public static boolean hasFormals(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return i.getNumberOfOperands()-0 > 0 && i.getOperand(0) != null;
  }

  /**
   * How many variable-length operands called Formals
   * does the argument instruction have?
   * @param i the instruction to access
   * @return the number of operands called Formals the instruction has
   */
  public static int getNumberOfFormals(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
    return i.getNumberOfOperands()-0;
  }

  /**
   * Change the number of Formals that may be stored in
   * the argument instruction to numVarOps.
   * @param i the instruction to access
   * @param numVarOps the new number of variable operands called Formals
   *        that may be stored in the instruction
   */
  public static void resizeNumberOfFormals(Instruction i, int numVarOps)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Prologue");
  if (0+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
    i.resizeNumberOfOperands(0+numVarOps);
  else
    for (int j = 0+numVarOps; j < MIN_OPERAND_ARRAY_LENGTH; j++)
      i.putOperand(j, null);
  }

  /**
   * Create an instruction of the Prologue instruction format.
   * @param o the instruction's operator
   * @param numVarOps the number of variable length operands that
   *                 will be stored in the insruction.
   * @return the newly created Prologue instruction
   */
  public static Instruction create(Operator o
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Prologue");
    Instruction i = new Instruction(o, Math.max(0+numVarOps, MIN_OPERAND_ARRAY_LENGTH));
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Prologue instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param numVarOps the number of variable length operands that
   *                  will be stored in the insruction.
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Prologue");
    if (0+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
      i.resizeNumberOfOperands(0+numVarOps);

    i.operator = o;
    return i;
  }
}

