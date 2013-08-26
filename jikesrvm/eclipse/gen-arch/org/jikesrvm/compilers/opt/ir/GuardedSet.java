
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
 * The GuardedSet InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class GuardedSet extends InstructionFormat {
  /**
   * InstructionFormat identification method for GuardedSet.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is GuardedSet or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for GuardedSet.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is GuardedSet or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == GuardedSet_format;
  }

  /**
   * Get the operand called Ref from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Ref
   */
  public static Operand getRef(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Val from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val
   */
  public static Operand getVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Val from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Val
   */
  public static Operand getClearVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return (Operand) i.getClearOperand(1);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    i.putOperand(1, Val);
  }
  /**
   * Return the index of the operand called Val
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Val
   *         in the argument instruction
   */
  public static int indexOfVal(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return 1;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Guard from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Guard from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getClearGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return (Operand) i.getClearOperand(2);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    i.putOperand(2, Guard);
  }
  /**
   * Return the index of the operand called Guard
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Guard
   *         in the argument instruction
   */
  public static int indexOfGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return 2;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "GuardedSet");
    return i.getOperand(2) != null;
  }


  /**
   * Create an instruction of the GuardedSet instruction format.
   * @param o the instruction's operator
   * @param Ref the instruction's Ref operand
   * @param Val the instruction's Val operand
   * @param Guard the instruction's Guard operand
   * @return the newly created GuardedSet instruction
   */
  public static Instruction create(Operator o
                   , Operand Ref
                   , Operand Val
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "GuardedSet");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Ref);
    i.putOperand(1, Val);
    i.putOperand(2, Guard);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * GuardedSet instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Ref the instruction's Ref operand
   * @param Val the instruction's Val operand
   * @param Guard the instruction's Guard operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Ref
                   , Operand Val
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "GuardedSet");
    i.operator = o;
    i.putOperand(0, Ref);
    i.putOperand(1, Val);
    i.putOperand(2, Guard);
    return i;
  }
}

