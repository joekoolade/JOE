
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
 * The IfCmp InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class IfCmp extends InstructionFormat {
  /**
   * InstructionFormat identification method for IfCmp.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is IfCmp or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for IfCmp.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is IfCmp or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == IfCmp_format;
  }

  /**
   * Get the operand called GuardResult from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Val1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val1
   */
  public static Operand getVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Val1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val1
   */
  public static Operand getClearVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (Operand) i.getClearOperand(1);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    i.putOperand(1, Val1);
  }
  /**
   * Return the index of the operand called Val1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val1
   *         in the argument instruction
   */
  public static int indexOfVal1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Cond from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static ConditionOperand getCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (ConditionOperand) i.getOperand(3);
  }
  /**
   * Get the operand called Cond from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static ConditionOperand getClearCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (ConditionOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called Cond in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond the operand to store
   */
  public static void setCond(Instruction i, ConditionOperand Cond) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    i.putOperand(3, Cond);
  }
  /**
   * Return the index of the operand called Cond
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Cond
   *         in the argument instruction
   */
  public static int indexOfCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return 3;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called Target from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static BranchOperand getTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (BranchOperand) i.getOperand(4);
  }
  /**
   * Get the operand called Target from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target
   */
  public static BranchOperand getClearTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (BranchOperand) i.getClearOperand(4);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    i.putOperand(4, Target);
  }
  /**
   * Return the index of the operand called Target
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Target
   *         in the argument instruction
   */
  public static int indexOfTarget(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return 4;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return i.getOperand(4) != null;
  }

  /**
   * Get the operand called BranchProfile from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile
   */
  public static BranchProfileOperand getBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (BranchProfileOperand) i.getOperand(5);
  }
  /**
   * Get the operand called BranchProfile from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile
   */
  public static BranchProfileOperand getClearBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return (BranchProfileOperand) i.getClearOperand(5);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    i.putOperand(5, BranchProfile);
  }
  /**
   * Return the index of the operand called BranchProfile
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called BranchProfile
   *         in the argument instruction
   */
  public static int indexOfBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return 5;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "IfCmp");
    return i.getOperand(5) != null;
  }


  /**
   * Create an instruction of the IfCmp instruction format.
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param Val1 the instruction's Val1 operand
   * @param Val2 the instruction's Val2 operand
   * @param Cond the instruction's Cond operand
   * @param Target the instruction's Target operand
   * @param BranchProfile the instruction's BranchProfile operand
   * @return the newly created IfCmp instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand GuardResult
                   , Operand Val1
                   , Operand Val2
                   , ConditionOperand Cond
                   , BranchOperand Target
                   , BranchProfileOperand BranchProfile
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "IfCmp");
    Instruction i = new Instruction(o, 6);
    i.putOperand(0, GuardResult);
    i.putOperand(1, Val1);
    i.putOperand(2, Val2);
    i.putOperand(3, Cond);
    i.putOperand(4, Target);
    i.putOperand(5, BranchProfile);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * IfCmp instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param Val1 the instruction's Val1 operand
   * @param Val2 the instruction's Val2 operand
   * @param Cond the instruction's Cond operand
   * @param Target the instruction's Target operand
   * @param BranchProfile the instruction's BranchProfile operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand GuardResult
                   , Operand Val1
                   , Operand Val2
                   , ConditionOperand Cond
                   , BranchOperand Target
                   , BranchProfileOperand BranchProfile
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "IfCmp");
    i.resizeNumberOfOperands(6);

    i.operator = o;
    i.putOperand(0, GuardResult);
    i.putOperand(1, Val1);
    i.putOperand(2, Val2);
    i.putOperand(3, Cond);
    i.putOperand(4, Target);
    i.putOperand(5, BranchProfile);
    return i;
  }
}

