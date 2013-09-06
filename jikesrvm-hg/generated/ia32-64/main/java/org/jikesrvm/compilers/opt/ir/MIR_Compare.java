
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
 * The MIR_Compare InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_Compare extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_Compare.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_Compare or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_Compare.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_Compare or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_Compare_format;
  }

  /**
   * Get the operand called Val1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val1
   */
  public static Operand getVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return (Operand) i.getOperand(0);
  }
  /**
   * Get the operand called Val1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val1
   */
  public static Operand getClearVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return (Operand) i.getClearOperand(0);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    i.putOperand(0, Val1);
  }
  /**
   * Return the index of the operand called Val1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val1
   *         in the argument instruction
   */
  public static int indexOfVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return 0;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Val2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val2
   */
  public static Operand getVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Val2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val2
   */
  public static Operand getClearVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return (Operand) i.getClearOperand(1);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    i.putOperand(1, Val2);
  }
  /**
   * Return the index of the operand called Val2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val2
   *         in the argument instruction
   */
  public static int indexOfVal2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Compare");
    return i.getOperand(1) != null;
  }


  /**
   * Create an instruction of the MIR_Compare instruction format.
   * @param o the instruction's operator
   * @param Val1 the instruction's Val1 operand
   * @param Val2 the instruction's Val2 operand
   * @return the newly created MIR_Compare instruction
   */
  public static Instruction create(Operator o
                   , Operand Val1
                   , Operand Val2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Compare");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Val1);
    i.putOperand(1, Val2);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Compare instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Val1 the instruction's Val1 operand
   * @param Val2 the instruction's Val2 operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Val1
                   , Operand Val2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Compare");
    i.operator = o;
    i.putOperand(0, Val1);
    i.putOperand(1, Val2);
    return i;
  }
}

