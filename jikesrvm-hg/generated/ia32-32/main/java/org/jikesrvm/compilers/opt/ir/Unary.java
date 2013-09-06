
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
 * The Unary InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class Unary extends InstructionFormat {
  /**
   * InstructionFormat identification method for Unary.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is Unary or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for Unary.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is Unary or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == Unary_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Val from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val
   */
  public static Operand getVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Val from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val
   */
  public static Operand getClearVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Val in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Val the operand to store
   */
  public static void setVal(Instruction i, Operand Val) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
    i.putOperand(1, Val);
  }
  /**
   * Return the index of the operand called Val
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val
   *         in the argument instruction
   */
  public static int indexOfVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Val?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Val or <code>false</code>
   *         if it does not.
   */
  public static boolean hasVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Unary");
    return i.getOperand(1) != null;
  }


  /**
   * Create an instruction of the Unary instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Val the instruction's Val operand
   * @return the newly created Unary instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , Operand Val
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Unary");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Val);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Unary instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Val the instruction's Val operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , Operand Val
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Unary");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Val);
    return i;
  }
}

