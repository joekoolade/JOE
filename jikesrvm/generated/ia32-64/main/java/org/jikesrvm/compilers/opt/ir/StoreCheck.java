
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
 * The StoreCheck InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class StoreCheck extends InstructionFormat {
  /**
   * InstructionFormat identification method for StoreCheck.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is StoreCheck or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for StoreCheck.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is StoreCheck or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == StoreCheck_format;
  }

  /**
   * Get the operand called GuardResult from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called GuardResult
   */
  public static RegisterOperand getGuardResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Ref from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Ref
   */
  public static Operand getRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Ref from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Ref
   */
  public static Operand getClearRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return (Operand) i.getClearOperand(1);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    i.putOperand(1, Ref);
  }
  /**
   * Return the index of the operand called Ref
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Ref
   *         in the argument instruction
   */
  public static int indexOfRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Val from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val
   */
  public static Operand getVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Val from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val
   */
  public static Operand getClearVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Val in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Val the operand to store
   */
  public static void setVal(Instruction i, Operand Val) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    i.putOperand(2, Val);
  }
  /**
   * Return the index of the operand called Val
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val
   *         in the argument instruction
   */
  public static int indexOfVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Val?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Val or <code>false</code>
   *         if it does not.
   */
  public static boolean hasVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Guard from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return (Operand) i.getOperand(3);
  }
  /**
   * Get the operand called Guard from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getClearGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return (Operand) i.getClearOperand(3);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    i.putOperand(3, Guard);
  }
  /**
   * Return the index of the operand called Guard
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Guard
   *         in the argument instruction
   */
  public static int indexOfGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return 3;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "StoreCheck");
    return i.getOperand(3) != null;
  }


  /**
   * Create an instruction of the StoreCheck instruction format.
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param Ref the instruction's Ref operand
   * @param Val the instruction's Val operand
   * @param Guard the instruction's Guard operand
   * @return the newly created StoreCheck instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand GuardResult
                   , Operand Ref
                   , Operand Val
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "StoreCheck");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, GuardResult);
    i.putOperand(1, Ref);
    i.putOperand(2, Val);
    i.putOperand(3, Guard);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * StoreCheck instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param GuardResult the instruction's GuardResult operand
   * @param Ref the instruction's Ref operand
   * @param Val the instruction's Val operand
   * @param Guard the instruction's Guard operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand GuardResult
                   , Operand Ref
                   , Operand Val
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "StoreCheck");
    i.operator = o;
    i.putOperand(0, GuardResult);
    i.putOperand(1, Ref);
    i.putOperand(2, Val);
    i.putOperand(3, Guard);
    return i;
  }
}

