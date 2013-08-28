
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
 * The MIR_CompareExchange InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_CompareExchange extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_CompareExchange.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_CompareExchange or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_CompareExchange.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_CompareExchange or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_CompareExchange_format;
  }

  /**
   * Get the operand called OldValue from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValue
   */
  public static RegisterOperand getOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called OldValue from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValue
   */
  public static RegisterOperand getClearOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called OldValue in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param OldValue the operand to store
   */
  public static void setOldValue(Instruction i, RegisterOperand OldValue) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    i.putOperand(0, OldValue);
  }
  /**
   * Return the index of the operand called OldValue
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called OldValue
   *         in the argument instruction
   */
  public static int indexOfOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named OldValue?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named OldValue or <code>false</code>
   *         if it does not.
   */
  public static boolean hasOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called MemAddr from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MemAddr
   */
  public static MemoryOperand getMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return (MemoryOperand) i.getOperand(1);
  }
  /**
   * Get the operand called MemAddr from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MemAddr
   */
  public static MemoryOperand getClearMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return (MemoryOperand) i.getClearOperand(1);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    i.putOperand(1, MemAddr);
  }
  /**
   * Return the index of the operand called MemAddr
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called MemAddr
   *         in the argument instruction
   */
  public static int indexOfMemAddr(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called NewValue from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValue
   */
  public static RegisterOperand getNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return (RegisterOperand) i.getOperand(2);
  }
  /**
   * Get the operand called NewValue from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValue
   */
  public static RegisterOperand getClearNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return (RegisterOperand) i.getClearOperand(2);
  }
  /**
   * Set the operand called NewValue in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param NewValue the operand to store
   */
  public static void setNewValue(Instruction i, RegisterOperand NewValue) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    i.putOperand(2, NewValue);
  }
  /**
   * Return the index of the operand called NewValue
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called NewValue
   *         in the argument instruction
   */
  public static int indexOfNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named NewValue?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named NewValue or <code>false</code>
   *         if it does not.
   */
  public static boolean hasNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CompareExchange");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the MIR_CompareExchange instruction format.
   * @param o the instruction's operator
   * @param OldValue the instruction's OldValue operand
   * @param MemAddr the instruction's MemAddr operand
   * @param NewValue the instruction's NewValue operand
   * @return the newly created MIR_CompareExchange instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand OldValue
                   , MemoryOperand MemAddr
                   , RegisterOperand NewValue
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CompareExchange");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, OldValue);
    i.putOperand(1, MemAddr);
    i.putOperand(2, NewValue);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CompareExchange instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param OldValue the instruction's OldValue operand
   * @param MemAddr the instruction's MemAddr operand
   * @param NewValue the instruction's NewValue operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand OldValue
                   , MemoryOperand MemAddr
                   , RegisterOperand NewValue
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CompareExchange");
    i.operator = o;
    i.putOperand(0, OldValue);
    i.putOperand(1, MemAddr);
    i.putOperand(2, NewValue);
    return i;
  }
}

