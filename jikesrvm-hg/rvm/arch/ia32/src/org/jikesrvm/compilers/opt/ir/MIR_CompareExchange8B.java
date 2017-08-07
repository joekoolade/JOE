
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
 * The MIR_CompareExchange8B InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_CompareExchange8B extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_CompareExchange8B.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_CompareExchange8B or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_CompareExchange8B.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_CompareExchange8B or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_CompareExchange8B_format;
  }

  /**
   * Get the operand called OldValueHigh from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValueHigh
   */
  public static RegisterOperand getOldValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called OldValueHigh from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValueHigh
   */
  public static RegisterOperand getClearOldValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called OldValueHigh in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param OldValueHigh the operand to store
   */
  public static void setOldValueHigh(Instruction i, RegisterOperand OldValueHigh) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    i.putOperand(0, OldValueHigh);
  }
  /**
   * Return the index of the operand called OldValueHigh
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called OldValueHigh
   *         in the argument instruction
   */
  public static int indexOfOldValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named OldValueHigh?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named OldValueHigh or <code>false</code>
   *         if it does not.
   */
  public static boolean hasOldValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called OldValueLow from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValueLow
   */
  public static RegisterOperand getOldValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getOperand(1);
  }
  /**
   * Get the operand called OldValueLow from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValueLow
   */
  public static RegisterOperand getClearOldValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called OldValueLow in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param OldValueLow the operand to store
   */
  public static void setOldValueLow(Instruction i, RegisterOperand OldValueLow) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    i.putOperand(1, OldValueLow);
  }
  /**
   * Return the index of the operand called OldValueLow
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called OldValueLow
   *         in the argument instruction
   */
  public static int indexOfOldValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named OldValueLow?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named OldValueLow or <code>false</code>
   *         if it does not.
   */
  public static boolean hasOldValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called MemAddr from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MemAddr
   */
  public static MemoryOperand getMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (MemoryOperand) i.getOperand(2);
  }
  /**
   * Get the operand called MemAddr from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MemAddr
   */
  public static MemoryOperand getClearMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (MemoryOperand) i.getClearOperand(2);
  }
  /**
   * Set the operand called MemAddr in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param MemAddr the operand to store
   */
  public static void setMemAddr(Instruction i, MemoryOperand MemAddr) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    i.putOperand(2, MemAddr);
  }
  /**
   * Return the index of the operand called MemAddr
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called MemAddr
   *         in the argument instruction
   */
  public static int indexOfMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named MemAddr?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named MemAddr or <code>false</code>
   *         if it does not.
   */
  public static boolean hasMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called NewValueHigh from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValueHigh
   */
  public static RegisterOperand getNewValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getOperand(3);
  }
  /**
   * Get the operand called NewValueHigh from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValueHigh
   */
  public static RegisterOperand getClearNewValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called NewValueHigh in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param NewValueHigh the operand to store
   */
  public static void setNewValueHigh(Instruction i, RegisterOperand NewValueHigh) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    i.putOperand(3, NewValueHigh);
  }
  /**
   * Return the index of the operand called NewValueHigh
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called NewValueHigh
   *         in the argument instruction
   */
  public static int indexOfNewValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return 3;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named NewValueHigh?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named NewValueHigh or <code>false</code>
   *         if it does not.
   */
  public static boolean hasNewValueHigh(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called NewValueLow from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValueLow
   */
  public static RegisterOperand getNewValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getOperand(4);
  }
  /**
   * Get the operand called NewValueLow from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValueLow
   */
  public static RegisterOperand getClearNewValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return (RegisterOperand) i.getClearOperand(4);
  }
  /**
   * Set the operand called NewValueLow in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param NewValueLow the operand to store
   */
  public static void setNewValueLow(Instruction i, RegisterOperand NewValueLow) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    i.putOperand(4, NewValueLow);
  }
  /**
   * Return the index of the operand called NewValueLow
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called NewValueLow
   *         in the argument instruction
   */
  public static int indexOfNewValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named NewValueLow?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named NewValueLow or <code>false</code>
   *         if it does not.
   */
  public static boolean hasNewValueLow(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange8B");
    return i.getOperand(4) != null;
  }


  /**
   * Create an instruction of the MIR_CompareExchange8B instruction format.
   * @param o the instruction's operator
   * @param OldValueHigh the instruction's OldValueHigh operand
   * @param OldValueLow the instruction's OldValueLow operand
   * @param MemAddr the instruction's MemAddr operand
   * @param NewValueHigh the instruction's NewValueHigh operand
   * @param NewValueLow the instruction's NewValueLow operand
   * @return the newly created MIR_CompareExchange8B instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand OldValueHigh
                   , RegisterOperand OldValueLow
                   , MemoryOperand MemAddr
                   , RegisterOperand NewValueHigh
                   , RegisterOperand NewValueLow
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CompareExchange8B");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, OldValueHigh);
    i.putOperand(1, OldValueLow);
    i.putOperand(2, MemAddr);
    i.putOperand(3, NewValueHigh);
    i.putOperand(4, NewValueLow);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CompareExchange8B instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param OldValueHigh the instruction's OldValueHigh operand
   * @param OldValueLow the instruction's OldValueLow operand
   * @param MemAddr the instruction's MemAddr operand
   * @param NewValueHigh the instruction's NewValueHigh operand
   * @param NewValueLow the instruction's NewValueLow operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand OldValueHigh
                   , RegisterOperand OldValueLow
                   , MemoryOperand MemAddr
                   , RegisterOperand NewValueHigh
                   , RegisterOperand NewValueLow
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CompareExchange8B");
    i.operator = o;
    i.putOperand(0, OldValueHigh);
    i.putOperand(1, OldValueLow);
    i.putOperand(2, MemAddr);
    i.putOperand(3, NewValueHigh);
    i.putOperand(4, NewValueLow);
    return i;
  }
}

