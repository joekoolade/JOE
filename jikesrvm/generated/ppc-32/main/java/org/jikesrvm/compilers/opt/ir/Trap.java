
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
 * The Trap InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class Trap extends InstructionFormat {
  /**
   * InstructionFormat identification method for Trap.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is Trap or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for Trap.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is Trap or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == Trap_format;
  }

  /**
   * Get the operand called GuardResult from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called GuardResult from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getClearGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called GuardResult in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param GuardResult the operand to store
   */
  public static void setGuardResult(Instruction i, RegisterOperand GuardResult) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    i.putOperand(0, GuardResult);
  }
  /**
   * Return the index of the operand called GuardResult
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called GuardResult
   *         in the argument instruction
   */
  public static int indexOfGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named GuardResult?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named GuardResult or <code>false</code>
   *         if it does not.
   */
  public static boolean hasGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called TCode from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called TCode
   */
  public static TrapCodeOperand getTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return (TrapCodeOperand) i.getOperand(1);
  }
  /**
   * Get the operand called TCode from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called TCode
   */
  public static TrapCodeOperand getClearTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return (TrapCodeOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called TCode in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param TCode the operand to store
   */
  public static void setTCode(Instruction i, TrapCodeOperand TCode) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    i.putOperand(1, TCode);
  }
  /**
   * Return the index of the operand called TCode
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called TCode
   *         in the argument instruction
   */
  public static int indexOfTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named TCode?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named TCode or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Trap");
    return i.getOperand(1) != null;
  }


  /**
   * Create an instruction of the Trap instruction format.
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param TCode the instruction's TCode operand
   * @return the newly created Trap instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand GuardResult
                   , TrapCodeOperand TCode
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Trap");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, GuardResult);
    i.putOperand(1, TCode);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Trap instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param TCode the instruction's TCode operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand GuardResult
                   , TrapCodeOperand TCode
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Trap");
    i.operator = o;
    i.putOperand(0, GuardResult);
    i.putOperand(1, TCode);
    return i;
  }
}

