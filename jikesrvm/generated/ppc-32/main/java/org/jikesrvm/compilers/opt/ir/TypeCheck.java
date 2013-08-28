
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
 * The TypeCheck InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class TypeCheck extends InstructionFormat {
  /**
   * InstructionFormat identification method for TypeCheck.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is TypeCheck or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for TypeCheck.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is TypeCheck or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == TypeCheck_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Type from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Type
   */
  public static TypeOperand getType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
    return (TypeOperand) i.getOperand(2);
  }
  /**
   * Get the operand called Type from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Type
   */
  public static TypeOperand getClearType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
    return (TypeOperand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Type in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Type the operand to store
   */
  public static void setType(Instruction i, TypeOperand Type) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
    i.putOperand(2, Type);
  }
  /**
   * Return the index of the operand called Type
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Type
   *         in the argument instruction
   */
  public static int indexOfType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Type?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Type or <code>false</code>
   *         if it does not.
   */
  public static boolean hasType(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "TypeCheck");
    return i.getOperand(3) != null;
  }


  /**
   * Create an instruction of the TypeCheck instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Ref the instruction's Ref operand
   * @param Type the instruction's Type operand
   * @param Guard the instruction's Guard operand
   * @return the newly created TypeCheck instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , Operand Ref
                   , TypeOperand Type
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "TypeCheck");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Ref);
    i.putOperand(2, Type);
    i.putOperand(3, Guard);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * TypeCheck instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Ref the instruction's Ref operand
   * @param Type the instruction's Type operand
   * @param Guard the instruction's Guard operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , Operand Ref
                   , TypeOperand Type
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "TypeCheck");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Ref);
    i.putOperand(2, Type);
    i.putOperand(3, Guard);
    return i;
  }
  /**
   * Create an instruction of the TypeCheck instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Ref the instruction's Ref operand
   * @param Type the instruction's Type operand
   * @return the newly created TypeCheck instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , Operand Ref
                   , TypeOperand Type
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "TypeCheck");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Result);
    i.putOperand(1, Ref);
    i.putOperand(2, Type);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * TypeCheck instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Ref the instruction's Ref operand
   * @param Type the instruction's Type operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , Operand Ref
                   , TypeOperand Type
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "TypeCheck");
    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Ref);
    i.putOperand(2, Type);
    i.putOperand(3, null);
    return i;
  }
}

