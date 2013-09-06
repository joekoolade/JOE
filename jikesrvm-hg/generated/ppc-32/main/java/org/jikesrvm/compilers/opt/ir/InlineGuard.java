
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
 * The InlineGuard InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class InlineGuard extends InstructionFormat {
  /**
   * InstructionFormat identification method for InlineGuard.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is InlineGuard or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for InlineGuard.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is InlineGuard or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == InlineGuard_format;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (Operand) i.getOperand(0);
  }
  /**
   * Get the operand called Value from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getClearValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (Operand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Value in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value the operand to store
   */
  public static void setValue(Instruction i, Operand Value) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    i.putOperand(0, Value);
  }
  /**
   * Return the index of the operand called Value
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value
   *         in the argument instruction
   */
  public static int indexOfValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Value?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Value or <code>false</code>
   *         if it does not.
   */
  public static boolean hasValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Guard from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Guard from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getClearGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Guard in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Guard the operand to store
   */
  public static void setGuard(Instruction i, Operand Guard) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    i.putOperand(1, Guard);
  }
  /**
   * Return the index of the operand called Guard
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Guard
   *         in the argument instruction
   */
  public static int indexOfGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Guard?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Guard or <code>false</code>
   *         if it does not.
   */
  public static boolean hasGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Goal from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Goal
   */
  public static Operand getGoal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Goal from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Goal
   */
  public static Operand getClearGoal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Goal in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Goal the operand to store
   */
  public static void setGoal(Instruction i, Operand Goal) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    i.putOperand(2, Goal);
  }
  /**
   * Return the index of the operand called Goal
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Goal
   *         in the argument instruction
   */
  public static int indexOfGoal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Goal?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Goal or <code>false</code>
   *         if it does not.
   */
  public static boolean hasGoal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Target from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static BranchOperand getTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (BranchOperand) i.getOperand(3);
  }
  /**
   * Get the operand called Target from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static BranchOperand getClearTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (BranchOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called Target in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Target the operand to store
   */
  public static void setTarget(Instruction i, BranchOperand Target) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    i.putOperand(3, Target);
  }
  /**
   * Return the index of the operand called Target
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Target
   *         in the argument instruction
   */
  public static int indexOfTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return 3;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Target?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Target or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called BranchProfile from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile
   */
  public static BranchProfileOperand getBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (BranchProfileOperand) i.getOperand(4);
  }
  /**
   * Get the operand called BranchProfile from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile
   */
  public static BranchProfileOperand getClearBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return (BranchProfileOperand) i.getClearOperand(4);
  }
  /**
   * Set the operand called BranchProfile in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param BranchProfile the operand to store
   */
  public static void setBranchProfile(Instruction i, BranchProfileOperand BranchProfile) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    i.putOperand(4, BranchProfile);
  }
  /**
   * Return the index of the operand called BranchProfile
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called BranchProfile
   *         in the argument instruction
   */
  public static int indexOfBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named BranchProfile?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named BranchProfile or <code>false</code>
   *         if it does not.
   */
  public static boolean hasBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "InlineGuard");
    return i.getOperand(4) != null;
  }


  /**
   * Create an instruction of the InlineGuard instruction format.
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Guard the instruction's Guard operand
   * @param Goal the instruction's Goal operand
   * @param Target the instruction's Target operand
   * @param BranchProfile the instruction's BranchProfile operand
   * @return the newly created InlineGuard instruction
   */
  public static Instruction create(Operator o
                   , Operand Value
                   , Operand Guard
                   , Operand Goal
                   , BranchOperand Target
                   , BranchProfileOperand BranchProfile
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "InlineGuard");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Value);
    i.putOperand(1, Guard);
    i.putOperand(2, Goal);
    i.putOperand(3, Target);
    i.putOperand(4, BranchProfile);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * InlineGuard instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Guard the instruction's Guard operand
   * @param Goal the instruction's Goal operand
   * @param Target the instruction's Target operand
   * @param BranchProfile the instruction's BranchProfile operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Value
                   , Operand Guard
                   , Operand Goal
                   , BranchOperand Target
                   , BranchProfileOperand BranchProfile
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "InlineGuard");
    i.operator = o;
    i.putOperand(0, Value);
    i.putOperand(1, Guard);
    i.putOperand(2, Goal);
    i.putOperand(3, Target);
    i.putOperand(4, BranchProfile);
    return i;
  }
}

