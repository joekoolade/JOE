
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
 * The CacheOp InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class CacheOp extends InstructionFormat {
  /**
   * InstructionFormat identification method for CacheOp.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is CacheOp or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for CacheOp.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is CacheOp or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == CacheOp_format;
  }

  /**
   * Get the operand called Ref from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Ref
   */
  public static Operand getRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CacheOp");
    return (Operand) i.getOperand(0);
  }
  /**
   * Get the operand called Ref from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Ref
   */
  public static Operand getClearRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CacheOp");
    return (Operand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Ref in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Ref the operand to store
   */
  public static void setRef(Instruction i, Operand Ref) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CacheOp");
    i.putOperand(0, Ref);
  }
  /**
   * Return the index of the operand called Ref
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Ref
   *         in the argument instruction
   */
  public static int indexOfRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CacheOp");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Ref?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Ref or <code>false</code>
   *         if it does not.
   */
  public static boolean hasRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "CacheOp");
    return i.getOperand(0) != null;
  }


  /**
   * Create an instruction of the CacheOp instruction format.
   * @param o the instruction's operator
   * @param Ref the instruction's Ref operand
   * @return the newly created CacheOp instruction
   */
  public static Instruction create(Operator o
                   , Operand Ref
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "CacheOp");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Ref);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * CacheOp instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Ref the instruction's Ref operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Ref
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "CacheOp");
    i.operator = o;
    i.putOperand(0, Ref);
    return i;
  }
}

