
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
 * The MIR_CondCall InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_CondCall extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_CondCall.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_CondCall or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_CondCall.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_CondCall or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_CondCall_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (RegisterOperand) i.getOperand(0);
  }
  /**
   * Get the operand called Result from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getClearResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (RegisterOperand) i.getClearOperand(0);
  }
  /**
   * Set the operand called Result in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Result the operand to store
   */
  public static void setResult(Instruction i, RegisterOperand Result) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    i.putOperand(0, Result);
  }
  /**
   * Return the index of the operand called Result
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Result
   *         in the argument instruction
   */
  public static int indexOfResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return 0;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Result?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Result or <code>false</code>
   *         if it does not.
   */
  public static boolean hasResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Result2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result2
   */
  public static RegisterOperand getResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (RegisterOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Result2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result2
   */
  public static RegisterOperand getClearResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (RegisterOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Result2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Result2 the operand to store
   */
  public static void setResult2(Instruction i, RegisterOperand Result2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    i.putOperand(1, Result2);
  }
  /**
   * Return the index of the operand called Result2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Result2
   *         in the argument instruction
   */
  public static int indexOfResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Result2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Result2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasResult2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static RegisterOperand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (RegisterOperand) i.getOperand(2);
  }
  /**
   * Get the operand called Value from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static RegisterOperand getClearValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (RegisterOperand) i.getClearOperand(2);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    i.putOperand(2, Value);
  }
  /**
   * Return the index of the operand called Value
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Value
   *         in the argument instruction
   */
  public static int indexOfValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return 2;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Cond from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static PowerPCConditionOperand getCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (PowerPCConditionOperand) i.getOperand(3);
  }
  /**
   * Get the operand called Cond from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Cond
   */
  public static PowerPCConditionOperand getClearCond(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (PowerPCConditionOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called Cond in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Cond the operand to store
   */
  public static void setCond(Instruction i, PowerPCConditionOperand Cond) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getOperand(4) != null;
  }

  /**
   * Get the k'th operand called Param from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Param
   */
  public static Operand getParam(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (Operand) i.getOperand(5+k);
  }
  /**
   * Get the k'th operand called Param from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Param
   */
  public static Operand getClearParam(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return (Operand) i.getClearOperand(5+k);
  }
  /**
   * Set the k'th operand called Param in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param k the index of the operand
   * @param o the operand to store
   */
  public static void setParam(Instruction i, int k, Operand o) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    i.putOperand(5+k, o);
  }
  /**
   * Return the index of the k'th operand called Param
   * in the argument instruction.
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return the index of the k'th operand called Param
   *         in the argument instruction
   */
  public static int indexOfParam(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return 5+k;
  }
  /**
   * Does the argument instruction have a non-null
   * k'th operand named Param?
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return <code>true</code> if the instruction has an non-null
   *         k'th operand named Param or <code>false</code>
   *         if it does not.
   */
  public static boolean hasParam(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getOperand(5+k) != null;
  }

  /**
   * Return the index of the first operand called Param
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the first operand called Param
   *         in the argument instruction
   */
  public static int indexOfParams(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return 5;
  }
  /**
   * Does the argument instruction have any operands
   * named Param?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has operands
   *         named Param or <code>false</code> if it does not.
   */
  public static boolean hasParams(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getNumberOfOperands()-5 > 0 && i.getOperand(5) != null;
  }

  /**
   * How many variable-length operands called Params
   * does the argument instruction have?
   * @param i the instruction to access
   * @return the number of operands called Params the instruction has
   */
  public static int getNumberOfParams(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
    return i.getNumberOfOperands()-5;
  }

  /**
   * Change the number of Params that may be stored in
   * the argument instruction to numVarOps.
   * @param i the instruction to access
   * @param numVarOps the new number of variable operands called Params
   *        that may be stored in the instruction
   */
  public static void resizeNumberOfParams(Instruction i, int numVarOps)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_CondCall");
  if (5+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
    i.resizeNumberOfOperands(5+numVarOps);
  else
    for (int j = 5+numVarOps; j < MIN_OPERAND_ARRAY_LENGTH; j++)
      i.putOperand(j, null);
  }

  /**
   * Create an instruction of the MIR_CondCall instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @param Target the instruction's Target operand
   * @param numVarOps the number of variable length operands that
   *                 will be stored in the insruction.
   * @return the newly created MIR_CondCall instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                   , BranchOperand Target
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    Instruction i = new Instruction(o, Math.max(5+numVarOps, MIN_OPERAND_ARRAY_LENGTH));
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    i.putOperand(4, Target);
    return i;
  }
  /**
   * Create an instruction of the MIR_CondCall instruction format
   * with 0 variable arguments.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @param Target the instruction's Target operand
   * @return the newly created MIR_CondCall instruction
   */
  public static Instruction create0(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                   , BranchOperand Target
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    i.putOperand(4, Target);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CondCall instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @param Target the instruction's Target operand
   * @param numVarOps the number of variable length operands that
   *                  will be stored in the insruction.
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                   , BranchOperand Target
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    if (5+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
      i.resizeNumberOfOperands(5+numVarOps);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    i.putOperand(4, Target);
    return i;
  }
  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CondCall instruction format having the specified
   * operator, operands, and number of variable-length operands.
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @param Target the instruction's Target operand
   * @return the mutated instruction
   */
  public static Instruction mutate0(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                   , BranchOperand Target
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    i.putOperand(4, Target);
    return i;
  }
  /**
   * Create an instruction of the MIR_CondCall instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @param numVarOps the number of variable length operands that
   *                 will be stored in the insruction.
   * @return the newly created MIR_CondCall instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    Instruction i = new Instruction(o, Math.max(5+numVarOps, MIN_OPERAND_ARRAY_LENGTH));
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    return i;
  }
  /**
   * Create an instruction of the MIR_CondCall instruction format
   * with 0 variable arguments.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @return the newly created MIR_CondCall instruction
   */
  public static Instruction create0(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CondCall instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @param numVarOps the number of variable length operands that
   *                  will be stored in the insruction.
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    if (5+numVarOps>MIN_OPERAND_ARRAY_LENGTH)
      i.resizeNumberOfOperands(5+numVarOps);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    i.putOperand(4, null);
    return i;
  }
  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_CondCall instruction format having the specified
   * operator, operands, and number of variable-length operands.
   * @param Result the instruction's Result operand
   * @param Result2 the instruction's Result2 operand
   * @param Value the instruction's Value operand
   * @param Cond the instruction's Cond operand
   * @return the mutated instruction
   */
  public static Instruction mutate0(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Result2
                   , RegisterOperand Value
                   , PowerPCConditionOperand Cond
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_CondCall");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Result2);
    i.putOperand(2, Value);
    i.putOperand(3, Cond);
    i.putOperand(4, null);
    return i;
  }
}

