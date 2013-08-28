
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
 * The MIR_Return InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_Return extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_Return.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_Return or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_Return.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_Return or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_Return_format;
  }

  /**
   * Get the operand called PopBytes from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called PopBytes
   */
  public static IntConstantOperand getPopBytes(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
    return (IntConstantOperand) i.getOperand(0);
  }
  /**
   * Get the operand called PopBytes from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called PopBytes
   */
  public static IntConstantOperand getClearPopBytes(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
    return (IntConstantOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called PopBytes in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param PopBytes the operand to store
   */
  public static void setPopBytes(Instruction i, IntConstantOperand PopBytes) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
    i.putOperand(0, PopBytes);
  }
  /**
   * Return the index of the operand called PopBytes
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called PopBytes
   *         in the argument instruction
   */
  public static int indexOfPopBytes(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named PopBytes?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named PopBytes or <code>false</code>
   *         if it does not.
   */
  public static boolean hasPopBytes(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Return");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the MIR_Return instruction format.
   * @param o the instruction's operator
   * @param PopBytes the instruction's PopBytes operand
   * @param Val the instruction's Val operand
   * @param Val2 the instruction's Val2 operand
   * @return the newly created MIR_Return instruction
   */
  public static Instruction create(Operator o
                   , IntConstantOperand PopBytes
                   , Operand Val
                   , Operand Val2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Return");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, PopBytes);
    i.putOperand(1, Val);
    i.putOperand(2, Val2);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Return instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param PopBytes the instruction's PopBytes operand
   * @param Val the instruction's Val operand
   * @param Val2 the instruction's Val2 operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , IntConstantOperand PopBytes
                   , Operand Val
                   , Operand Val2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Return");
    i.operator = o;
    i.putOperand(0, PopBytes);
    i.putOperand(1, Val);
    i.putOperand(2, Val2);
    return i;
  }
}

