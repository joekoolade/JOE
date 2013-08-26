
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
 * The MIR_Trap InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_Trap extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_Trap.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_Trap or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_Trap.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_Trap or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_Trap_format;
  }

  /**
   * Get the operand called GuardResult from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Cond from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static PowerPCTrapOperand getCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (PowerPCTrapOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Cond from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static PowerPCTrapOperand getClearCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (PowerPCTrapOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Cond in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond the operand to store
   */
  public static void setCond(Instruction i, PowerPCTrapOperand Cond) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    i.putOperand(1, Cond);
  }
  /**
   * Return the index of the operand called Cond
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Cond
   *         in the argument instruction
   */
  public static int indexOfCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Value1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value1
   */
  public static RegisterOperand getValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (RegisterOperand) i.getOperand(2);
  }
  /**
   * Get the operand called Value1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value1
   */
  public static RegisterOperand getClearValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (RegisterOperand) i.getClearOperand(2);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    i.putOperand(2, Value1);
  }
  /**
   * Return the index of the operand called Value1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value1
   *         in the argument instruction
   */
  public static int indexOfValue1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return 2;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Value2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value2
   */
  public static Operand getValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (Operand) i.getOperand(3);
  }
  /**
   * Get the operand called Value2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value2
   */
  public static Operand getClearValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (Operand) i.getClearOperand(3);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    i.putOperand(3, Value2);
  }
  /**
   * Return the index of the operand called Value2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value2
   *         in the argument instruction
   */
  public static int indexOfValue2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return 3;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called TCode from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called TCode
   */
  public static TrapCodeOperand getTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (TrapCodeOperand) i.getOperand(4);
  }
  /**
   * Get the operand called TCode from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called TCode
   */
  public static TrapCodeOperand getClearTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return (TrapCodeOperand) i.getClearOperand(4);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    i.putOperand(4, TCode);
  }
  /**
   * Return the index of the operand called TCode
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called TCode
   *         in the argument instruction
   */
  public static int indexOfTCode(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return 4;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_Trap");
    return i.getOperand(4) != null;
  }


  /**
   * Create an instruction of the MIR_Trap instruction format.
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param Cond the instruction's Cond operand
   * @param Value1 the instruction's Value1 operand
   * @param Value2 the instruction's Value2 operand
   * @param TCode the instruction's TCode operand
   * @return the newly created MIR_Trap instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand GuardResult
                   , PowerPCTrapOperand Cond
                   , RegisterOperand Value1
                   , Operand Value2
                   , TrapCodeOperand TCode
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Trap");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, GuardResult);
    i.putOperand(1, Cond);
    i.putOperand(2, Value1);
    i.putOperand(3, Value2);
    i.putOperand(4, TCode);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Trap instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param Cond the instruction's Cond operand
   * @param Value1 the instruction's Value1 operand
   * @param Value2 the instruction's Value2 operand
   * @param TCode the instruction's TCode operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand GuardResult
                   , PowerPCTrapOperand Cond
                   , RegisterOperand Value1
                   , Operand Value2
                   , TrapCodeOperand TCode
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Trap");
    i.operator = o;
    i.putOperand(0, GuardResult);
    i.putOperand(1, Cond);
    i.putOperand(2, Value1);
    i.putOperand(3, Value2);
    i.putOperand(4, TCode);
    return i;
  }
  /**
   * Create an instruction of the MIR_Trap instruction format.
   * @param o the instruction's operator
   * @param Cond the instruction's Cond operand
   * @param Value1 the instruction's Value1 operand
   * @param Value2 the instruction's Value2 operand
   * @param TCode the instruction's TCode operand
   * @return the newly created MIR_Trap instruction
   */
  public static Instruction create(Operator o
                   , PowerPCTrapOperand Cond
                   , RegisterOperand Value1
                   , Operand Value2
                   , TrapCodeOperand TCode
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Trap");
    Instruction i = new Instruction(o, 5);
    i.putOperand(1, Cond);
    i.putOperand(2, Value1);
    i.putOperand(3, Value2);
    i.putOperand(4, TCode);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_Trap instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Cond the instruction's Cond operand
   * @param Value1 the instruction's Value1 operand
   * @param Value2 the instruction's Value2 operand
   * @param TCode the instruction's TCode operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , PowerPCTrapOperand Cond
                   , RegisterOperand Value1
                   , Operand Value2
                   , TrapCodeOperand TCode
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_Trap");
    i.operator = o;
    i.putOperand(0, null);
    i.putOperand(1, Cond);
    i.putOperand(2, Value1);
    i.putOperand(3, Value2);
    i.putOperand(4, TCode);
    return i;
  }
}

