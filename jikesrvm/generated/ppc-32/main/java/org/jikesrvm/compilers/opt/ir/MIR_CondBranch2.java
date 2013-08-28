
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
 * The MIR_CondBranch2 InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_CondBranch2 extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_CondBranch2.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_CondBranch2 or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_CondBranch2.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_CondBranch2 or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_CondBranch2_format;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static RegisterOperand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Value from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static RegisterOperand getClearValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Value in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Value the operand to store
   */
  public static void setValue(Instruction i, RegisterOperand Value) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Cond1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond1
   */
  public static PowerPCConditionOperand getCond1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (PowerPCConditionOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Cond1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond1
   */
  public static PowerPCConditionOperand getClearCond1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (PowerPCConditionOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Cond1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond1 the operand to store
   */
  public static void setCond1(Instruction i, PowerPCConditionOperand Cond1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    i.putOperand(1, Cond1);
  }
  /**
   * Return the index of the operand called Cond1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Cond1
   *         in the argument instruction
   */
  public static int indexOfCond1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Cond1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Cond1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasCond1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Target1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target1
   */
  public static BranchOperand getTarget1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchOperand) i.getOperand(2);
  }
  /**
   * Get the operand called Target1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target1
   */
  public static BranchOperand getClearTarget1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchOperand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Target1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Target1 the operand to store
   */
  public static void setTarget1(Instruction i, BranchOperand Target1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    i.putOperand(2, Target1);
  }
  /**
   * Return the index of the operand called Target1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Target1
   *         in the argument instruction
   */
  public static int indexOfTarget1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Target1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Target1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTarget1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called BranchProfile1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile1
   */
  public static BranchProfileOperand getBranchProfile1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchProfileOperand) i.getOperand(3);
  }
  /**
   * Get the operand called BranchProfile1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile1
   */
  public static BranchProfileOperand getClearBranchProfile1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchProfileOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called BranchProfile1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param BranchProfile1 the operand to store
   */
  public static void setBranchProfile1(Instruction i, BranchProfileOperand BranchProfile1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    i.putOperand(3, BranchProfile1);
  }
  /**
   * Return the index of the operand called BranchProfile1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called BranchProfile1
   *         in the argument instruction
   */
  public static int indexOfBranchProfile1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return 3;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named BranchProfile1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named BranchProfile1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasBranchProfile1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called Cond2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond2
   */
  public static PowerPCConditionOperand getCond2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (PowerPCConditionOperand) i.getOperand(4);
  }
  /**
   * Get the operand called Cond2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond2
   */
  public static PowerPCConditionOperand getClearCond2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (PowerPCConditionOperand) i.getClearOperand(4);
  }
  /**
   * Set the operand called Cond2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond2 the operand to store
   */
  public static void setCond2(Instruction i, PowerPCConditionOperand Cond2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    i.putOperand(4, Cond2);
  }
  /**
   * Return the index of the operand called Cond2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Cond2
   *         in the argument instruction
   */
  public static int indexOfCond2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Cond2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Cond2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasCond2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(4) != null;
  }

  /**
   * Get the operand called Target2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target2
   */
  public static BranchOperand getTarget2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchOperand) i.getOperand(5);
  }
  /**
   * Get the operand called Target2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Target2
   */
  public static BranchOperand getClearTarget2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchOperand) i.getClearOperand(5);
  }
  /**
   * Set the operand called Target2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Target2 the operand to store
   */
  public static void setTarget2(Instruction i, BranchOperand Target2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    i.putOperand(5, Target2);
  }
  /**
   * Return the index of the operand called Target2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Target2
   *         in the argument instruction
   */
  public static int indexOfTarget2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return 5;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Target2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Target2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTarget2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(5) != null;
  }

  /**
   * Get the operand called BranchProfile2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile2
   */
  public static BranchProfileOperand getBranchProfile2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchProfileOperand) i.getOperand(6);
  }
  /**
   * Get the operand called BranchProfile2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called BranchProfile2
   */
  public static BranchProfileOperand getClearBranchProfile2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return (BranchProfileOperand) i.getClearOperand(6);
  }
  /**
   * Set the operand called BranchProfile2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param BranchProfile2 the operand to store
   */
  public static void setBranchProfile2(Instruction i, BranchProfileOperand BranchProfile2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    i.putOperand(6, BranchProfile2);
  }
  /**
   * Return the index of the operand called BranchProfile2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called BranchProfile2
   *         in the argument instruction
   */
  public static int indexOfBranchProfile2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return 6;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named BranchProfile2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named BranchProfile2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasBranchProfile2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondBranch2");
    return i.getOperand(6) != null;
  }


  /**
   * Create an instruction of the MIR_CondBranch2 instruction format.
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Cond1 the instruction's Cond1 operand
   * @param Target1 the instruction's Target1 operand
   * @param BranchProfile1 the instruction's BranchProfile1 operand
   * @param Cond2 the instruction's Cond2 operand
   * @param Target2 the instruction's Target2 operand
   * @param BranchProfile2 the instruction's BranchProfile2 operand
   * @return the newly created MIR_CondBranch2 instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond1
                   , BranchOperand Target1
                   , BranchProfileOperand BranchProfile1
                   , PowerPCConditionOperand Cond2
                   , BranchOperand Target2
                   , BranchProfileOperand BranchProfile2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondBranch2");
    Instruction i = new Instruction(o, 7);
    i.putOperand(0, Value);
    i.putOperand(1, Cond1);
    i.putOperand(2, Target1);
    i.putOperand(3, BranchProfile1);
    i.putOperand(4, Cond2);
    i.putOperand(5, Target2);
    i.putOperand(6, BranchProfile2);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CondBranch2 instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Cond1 the instruction's Cond1 operand
   * @param Target1 the instruction's Target1 operand
   * @param BranchProfile1 the instruction's BranchProfile1 operand
   * @param Cond2 the instruction's Cond2 operand
   * @param Target2 the instruction's Target2 operand
   * @param BranchProfile2 the instruction's BranchProfile2 operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond1
                   , BranchOperand Target1
                   , BranchProfileOperand BranchProfile1
                   , PowerPCConditionOperand Cond2
                   , BranchOperand Target2
                   , BranchProfileOperand BranchProfile2
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondBranch2");
    i.resizeNumberOfOperands(7);

    i.operator = o;
    i.putOperand(0, Value);
    i.putOperand(1, Cond1);
    i.putOperand(2, Target1);
    i.putOperand(3, BranchProfile1);
    i.putOperand(4, Cond2);
    i.putOperand(5, Target2);
    i.putOperand(6, BranchProfile2);
    return i;
  }
}

