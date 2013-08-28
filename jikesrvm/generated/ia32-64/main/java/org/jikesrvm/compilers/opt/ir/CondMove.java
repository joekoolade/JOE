
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
 * The CondMove InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class CondMove extends InstructionFormat {
  /**
   * InstructionFormat identification method for CondMove.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is CondMove or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for CondMove.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is CondMove or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == CondMove_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Val1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val1
   */
  public static Operand getVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Val1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val1
   */
  public static Operand getClearVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Val1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Val1 the operand to store
   */
  public static void setVal1(Instruction i, Operand Val1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    i.putOperand(1, Val1);
  }
  /**
   * Return the index of the operand called Val1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val1
   *         in the argument instruction
   */
  public static int indexOfVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Val1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Val1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Val2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val2
   */
  public static Operand getVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Val2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val2
   */
  public static Operand getClearVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Val2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Val2 the operand to store
   */
  public static void setVal2(Instruction i, Operand Val2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    i.putOperand(2, Val2);
  }
  /**
   * Return the index of the operand called Val2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val2
   *         in the argument instruction
   */
  public static int indexOfVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Val2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Val2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Cond from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static ConditionOperand getCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (ConditionOperand) i.getOperand(3);
  }
  /**
   * Get the operand called Cond from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static ConditionOperand getClearCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (ConditionOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called Cond in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond the operand to store
   */
  public static void setCond(Instruction i, ConditionOperand Cond) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    i.putOperand(3, Cond);
  }
  /**
   * Return the index of the operand called Cond
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Cond
   *         in the argument instruction
   */
  public static int indexOfCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return 3;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called TrueValue from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called TrueValue
   */
  public static Operand getTrueValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getOperand(4);
  }
  /**
   * Get the operand called TrueValue from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called TrueValue
   */
  public static Operand getClearTrueValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getClearOperand(4);
  }
  /**
   * Set the operand called TrueValue in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param TrueValue the operand to store
   */
  public static void setTrueValue(Instruction i, Operand TrueValue) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    i.putOperand(4, TrueValue);
  }
  /**
   * Return the index of the operand called TrueValue
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called TrueValue
   *         in the argument instruction
   */
  public static int indexOfTrueValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named TrueValue?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named TrueValue or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTrueValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return i.getOperand(4) != null;
  }

  /**
   * Get the operand called FalseValue from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called FalseValue
   */
  public static Operand getFalseValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getOperand(5);
  }
  /**
   * Get the operand called FalseValue from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called FalseValue
   */
  public static Operand getClearFalseValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return (Operand) i.getClearOperand(5);
  }
  /**
   * Set the operand called FalseValue in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param FalseValue the operand to store
   */
  public static void setFalseValue(Instruction i, Operand FalseValue) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    i.putOperand(5, FalseValue);
  }
  /**
   * Return the index of the operand called FalseValue
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called FalseValue
   *         in the argument instruction
   */
  public static int indexOfFalseValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return 5;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named FalseValue?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named FalseValue or <code>false</code>
   *         if it does not.
   */
  public static boolean hasFalseValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CondMove");
    return i.getOperand(5) != null;
  }


  /**
   * Create an instruction of the CondMove instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Val1 the instruction's Val1 operand
   * @param Val2 the instruction's Val2 operand
   * @param Cond the instruction's Cond operand
   * @param TrueValue the instruction's TrueValue operand
   * @param FalseValue the instruction's FalseValue operand
   * @return the newly created CondMove instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , Operand Val1
                   , Operand Val2
                   , ConditionOperand Cond
                   , Operand TrueValue
                   , Operand FalseValue
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "CondMove");
    Instruction i = new Instruction(o, 6);
    i.putOperand(0, Result);
    i.putOperand(1, Val1);
    i.putOperand(2, Val2);
    i.putOperand(3, Cond);
    i.putOperand(4, TrueValue);
    i.putOperand(5, FalseValue);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * CondMove instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Val1 the instruction's Val1 operand
   * @param Val2 the instruction's Val2 operand
   * @param Cond the instruction's Cond operand
   * @param TrueValue the instruction's TrueValue operand
   * @param FalseValue the instruction's FalseValue operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , Operand Val1
                   , Operand Val2
                   , ConditionOperand Cond
                   , Operand TrueValue
                   , Operand FalseValue
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "CondMove");
    i.resizeNumberOfOperands(6);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Val1);
    i.putOperand(2, Val2);
    i.putOperand(3, Cond);
    i.putOperand(4, TrueValue);
    i.putOperand(5, FalseValue);
    return i;
  }
}

