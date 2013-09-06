
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
 * The MIR_RotateAndMask InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class MIR_RotateAndMask extends InstructionFormat {
  /**
   * InstructionFormat identification method for MIR_RotateAndMask.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is MIR_RotateAndMask or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for MIR_RotateAndMask.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is MIR_RotateAndMask or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == MIR_RotateAndMask_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Source from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Source
   */
  public static RegisterOperand getSource(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (RegisterOperand) i.getOperand(1);
  }
  /**
   * Get the operand called Source from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Source
   */
  public static RegisterOperand getClearSource(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (RegisterOperand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Source in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Source the operand to store
   */
  public static void setSource(Instruction i, RegisterOperand Source) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    i.putOperand(1, Source);
  }
  /**
   * Return the index of the operand called Source
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Source
   *         in the argument instruction
   */
  public static int indexOfSource(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Source?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Source or <code>false</code>
   *         if it does not.
   */
  public static boolean hasSource(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Shift from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Shift
   */
  public static Operand getShift(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (Operand) i.getOperand(3);
  }
  /**
   * Get the operand called Shift from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Shift
   */
  public static Operand getClearShift(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (Operand) i.getClearOperand(3);
  }
  /**
   * Set the operand called Shift in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Shift the operand to store
   */
  public static void setShift(Instruction i, Operand Shift) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    i.putOperand(3, Shift);
  }
  /**
   * Return the index of the operand called Shift
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Shift
   *         in the argument instruction
   */
  public static int indexOfShift(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return 3;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Shift?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Shift or <code>false</code>
   *         if it does not.
   */
  public static boolean hasShift(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called MaskBegin from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MaskBegin
   */
  public static IntConstantOperand getMaskBegin(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (IntConstantOperand) i.getOperand(4);
  }
  /**
   * Get the operand called MaskBegin from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MaskBegin
   */
  public static IntConstantOperand getClearMaskBegin(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (IntConstantOperand) i.getClearOperand(4);
  }
  /**
   * Set the operand called MaskBegin in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param MaskBegin the operand to store
   */
  public static void setMaskBegin(Instruction i, IntConstantOperand MaskBegin) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    i.putOperand(4, MaskBegin);
  }
  /**
   * Return the index of the operand called MaskBegin
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called MaskBegin
   *         in the argument instruction
   */
  public static int indexOfMaskBegin(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named MaskBegin?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named MaskBegin or <code>false</code>
   *         if it does not.
   */
  public static boolean hasMaskBegin(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return i.getOperand(4) != null;
  }

  /**
   * Get the operand called MaskEnd from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MaskEnd
   */
  public static IntConstantOperand getMaskEnd(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (IntConstantOperand) i.getOperand(5);
  }
  /**
   * Get the operand called MaskEnd from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called MaskEnd
   */
  public static IntConstantOperand getClearMaskEnd(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return (IntConstantOperand) i.getClearOperand(5);
  }
  /**
   * Set the operand called MaskEnd in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param MaskEnd the operand to store
   */
  public static void setMaskEnd(Instruction i, IntConstantOperand MaskEnd) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    i.putOperand(5, MaskEnd);
  }
  /**
   * Return the index of the operand called MaskEnd
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called MaskEnd
   *         in the argument instruction
   */
  public static int indexOfMaskEnd(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return 5;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named MaskEnd?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named MaskEnd or <code>false</code>
   *         if it does not.
   */
  public static boolean hasMaskEnd(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "MIR_RotateAndMask");
    return i.getOperand(5) != null;
  }


  /**
   * Create an instruction of the MIR_RotateAndMask instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Source the instruction's Source operand
   * @param Value the instruction's Value operand
   * @param Shift the instruction's Shift operand
   * @param MaskBegin the instruction's MaskBegin operand
   * @param MaskEnd the instruction's MaskEnd operand
   * @return the newly created MIR_RotateAndMask instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Source
                   , RegisterOperand Value
                   , Operand Shift
                   , IntConstantOperand MaskBegin
                   , IntConstantOperand MaskEnd
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_RotateAndMask");
    Instruction i = new Instruction(o, 6);
    i.putOperand(0, Result);
    i.putOperand(1, Source);
    i.putOperand(2, Value);
    i.putOperand(3, Shift);
    i.putOperand(4, MaskBegin);
    i.putOperand(5, MaskEnd);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_RotateAndMask instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Source the instruction's Source operand
   * @param Value the instruction's Value operand
   * @param Shift the instruction's Shift operand
   * @param MaskBegin the instruction's MaskBegin operand
   * @param MaskEnd the instruction's MaskEnd operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Source
                   , RegisterOperand Value
                   , Operand Shift
                   , IntConstantOperand MaskBegin
                   , IntConstantOperand MaskEnd
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_RotateAndMask");
    i.resizeNumberOfOperands(6);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Source);
    i.putOperand(2, Value);
    i.putOperand(3, Shift);
    i.putOperand(4, MaskBegin);
    i.putOperand(5, MaskEnd);
    return i;
  }
  /**
   * Create an instruction of the MIR_RotateAndMask instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Value the instruction's Value operand
   * @param Shift the instruction's Shift operand
   * @param MaskBegin the instruction's MaskBegin operand
   * @param MaskEnd the instruction's MaskEnd operand
   * @return the newly created MIR_RotateAndMask instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , RegisterOperand Value
                   , Operand Shift
                   , IntConstantOperand MaskBegin
                   , IntConstantOperand MaskEnd
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_RotateAndMask");
    Instruction i = new Instruction(o, 6);
    i.putOperand(0, Result);
    i.putOperand(2, Value);
    i.putOperand(3, Shift);
    i.putOperand(4, MaskBegin);
    i.putOperand(5, MaskEnd);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * MIR_RotateAndMask instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Value the instruction's Value operand
   * @param Shift the instruction's Shift operand
   * @param MaskBegin the instruction's MaskBegin operand
   * @param MaskEnd the instruction's MaskEnd operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , RegisterOperand Value
                   , Operand Shift
                   , IntConstantOperand MaskBegin
                   , IntConstantOperand MaskEnd
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "MIR_RotateAndMask");
    i.resizeNumberOfOperands(6);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, null);
    i.putOperand(2, Value);
    i.putOperand(3, Shift);
    i.putOperand(4, MaskBegin);
    i.putOperand(5, MaskEnd);
    return i;
  }
}



