
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
 * The MIR_Binary InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_Binary extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_Binary.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_Binary or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_Binary.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_Binary or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_Binary_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Value1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value1
   */
  public static RegisterOperand getValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return (RegisterOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Value1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value1
   */
  public static RegisterOperand getClearValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return (RegisterOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Value1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value1 the operand to store
   */
  public static void setValue1(Instruction i, RegisterOperand Value1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    i.putOperand(1, Value1);
  }
  /**
   * Return the index of the operand called Value1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value1
   *         in the argument instruction
   */
  public static int indexOfValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Value1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Value1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Value2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value2
   */
  public static Operand getValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Value2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value2
   */
  public static Operand getClearValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Value2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value2 the operand to store
   */
  public static void setValue2(Instruction i, Operand Value2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    i.putOperand(2, Value2);
  }
  /**
   * Return the index of the operand called Value2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value2
   *         in the argument instruction
   */
  public static int indexOfValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Value2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Value2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Binary");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the MIR_Binary instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Value1 the instruction's Value1 operand
   * @param Value2 the instruction's Value2 operand
   * @return the newly created MIR_Binary instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Value1
                   , Operand Value2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Binary");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Value1);
    i.putOperand(2, Value2);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Binary instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Value1 the instruction's Value1 operand
   * @param Value2 the instruction's Value2 operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Value1
                   , Operand Value2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Binary");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Value1);
    i.putOperand(2, Value2);
    return i;
  }
}

