
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
 * The MIR_RDTSC InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_RDTSC extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_RDTSC.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_RDTSC or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_RDTSC.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_RDTSC or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_RDTSC_format;
  }

  /**
   * Get the operand called Dest1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Dest1
   */
  public static RegisterOperand getDest1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Dest1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Dest1
   */
  public static RegisterOperand getClearDest1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Dest1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Dest1 the operand to store
   */
  public static void setDest1(Instruction i, RegisterOperand Dest1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    i.putOperand(0, Dest1);
  }
  /**
   * Return the index of the operand called Dest1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Dest1
   *         in the argument instruction
   */
  public static int indexOfDest1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Dest1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Dest1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasDest1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Dest2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Dest2
   */
  public static RegisterOperand getDest2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return (RegisterOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Dest2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Dest2
   */
  public static RegisterOperand getClearDest2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return (RegisterOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Dest2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Dest2 the operand to store
   */
  public static void setDest2(Instruction i, RegisterOperand Dest2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    i.putOperand(1, Dest2);
  }
  /**
   * Return the index of the operand called Dest2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Dest2
   *         in the argument instruction
   */
  public static int indexOfDest2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Dest2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Dest2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasDest2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RDTSC");
    return i.getOperand(1) != null;
  }


  /**
   * Create an instruction of the MIR_RDTSC instruction format.
   * @param o the instruction's operator
   * @param Dest1 the instruction's Dest1 operand
   * @param Dest2 the instruction's Dest2 operand
   * @return the newly created MIR_RDTSC instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Dest1
                   , RegisterOperand Dest2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_RDTSC");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Dest1);
    i.putOperand(1, Dest2);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_RDTSC instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Dest1 the instruction's Dest1 operand
   * @param Dest2 the instruction's Dest2 operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Dest1
                   , RegisterOperand Dest2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_RDTSC");
    i.operator = o;
    i.putOperand(0, Dest1);
    i.putOperand(1, Dest2);
    return i;
  }
}

