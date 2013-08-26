
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
 * The NewArray InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class NewArray extends InstructionFormat {
  /**
   * InstructionFormat identification method for NewArray.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is NewArray or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for NewArray.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is NewArray or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == NewArray_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Size from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Size
   */
  public static Operand getSize(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Size from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Size
   */
  public static Operand getClearSize(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Size in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Size the operand to store
   */
  public static void setSize(Instruction i, Operand Size) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
    i.putOperand(2, Size);
  }
  /**
   * Return the index of the operand called Size
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Size
   *         in the argument instruction
   */
  public static int indexOfSize(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Size?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Size or <code>false</code>
   *         if it does not.
   */
  public static boolean hasSize(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "NewArray");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the NewArray instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Type the instruction's Type operand
   * @param Size the instruction's Size operand
   * @return the newly created NewArray instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , TypeOperand Type
                   , Operand Size
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "NewArray");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Type);
    i.putOperand(2, Size);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * NewArray instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Type the instruction's Type operand
   * @param Size the instruction's Size operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , TypeOperand Type
                   , Operand Size
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "NewArray");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Type);
    i.putOperand(2, Size);
    return i;
  }
}

