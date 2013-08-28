
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
 * The BBend InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class BBend extends InstructionFormat {
  /**
   * InstructionFormat identification method for BBend.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is BBend or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for BBend.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is BBend or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == BBend_format;
  }

  /**
   * Get the operand called Block from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Block
   */
  public static BasicBlockOperand getBlock(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BBend");
    return (BasicBlockOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Block from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Block
   */
  public static BasicBlockOperand getClearBlock(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BBend");
    return (BasicBlockOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Block in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Block the operand to store
   */
  public static void setBlock(Instruction i, BasicBlockOperand Block) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BBend");
    i.putOperand(0, Block);
  }
  /**
   * Return the index of the operand called Block
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Block
   *         in the argument instruction
   */
  public static int indexOfBlock(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BBend");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Block?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Block or <code>false</code>
   *         if it does not.
   */
  public static boolean hasBlock(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "BBend");
    return i.getOperand(0) != null;
  }


  /**
   * Create an instruction of the BBend instruction format.
   * @param o the instruction's operator
   * @param Block the instruction's Block operand
   * @return the newly created BBend instruction
   */
  public static Instruction create(Operator o
                   , BasicBlockOperand Block
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "BBend");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Block);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * BBend instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Block the instruction's Block operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , BasicBlockOperand Block
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "BBend");
    i.operator = o;
    i.putOperand(0, Block);
    return i;
  }
}

