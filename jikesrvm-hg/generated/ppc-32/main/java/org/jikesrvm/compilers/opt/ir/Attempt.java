
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
 * The Attempt InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class Attempt extends InstructionFormat {
  /**
   * InstructionFormat identification method for Attempt.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is Attempt or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for Attempt.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is Attempt or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == Attempt_format;
  }

  /**
   * Get the operand called Result from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Result
   */
  public static RegisterOperand getResult(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Address from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Address
   */
  public static Operand getAddress(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Address from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Address
   */
  public static Operand getClearAddress(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Address in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Address the operand to store
   */
  public static void setAddress(Instruction i, Operand Address) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    i.putOperand(1, Address);
  }
  /**
   * Return the index of the operand called Address
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Address
   *         in the argument instruction
   */
  public static int indexOfAddress(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Address?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Address or <code>false</code>
   *         if it does not.
   */
  public static boolean hasAddress(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Offset from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Offset
   */
  public static Operand getOffset(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Offset from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Offset
   */
  public static Operand getClearOffset(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Offset in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Offset the operand to store
   */
  public static void setOffset(Instruction i, Operand Offset) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    i.putOperand(2, Offset);
  }
  /**
   * Return the index of the operand called Offset
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Offset
   *         in the argument instruction
   */
  public static int indexOfOffset(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Offset?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Offset or <code>false</code>
   *         if it does not.
   */
  public static boolean hasOffset(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called OldValue from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValue
   */
  public static Operand getOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getOperand(3);
  }
  /**
   * Get the operand called OldValue from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called OldValue
   */
  public static Operand getClearOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getClearOperand(3);
  }
  /**
   * Set the operand called OldValue in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param OldValue the operand to store
   */
  public static void setOldValue(Instruction i, Operand OldValue) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    i.putOperand(3, OldValue);
  }
  /**
   * Return the index of the operand called OldValue
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called OldValue
   *         in the argument instruction
   */
  public static int indexOfOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return 3;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named OldValue?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named OldValue or <code>false</code>
   *         if it does not.
   */
  public static boolean hasOldValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called NewValue from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValue
   */
  public static Operand getNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getOperand(4);
  }
  /**
   * Get the operand called NewValue from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called NewValue
   */
  public static Operand getClearNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getClearOperand(4);
  }
  /**
   * Set the operand called NewValue in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param NewValue the operand to store
   */
  public static void setNewValue(Instruction i, Operand NewValue) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    i.putOperand(4, NewValue);
  }
  /**
   * Return the index of the operand called NewValue
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called NewValue
   *         in the argument instruction
   */
  public static int indexOfNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named NewValue?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named NewValue or <code>false</code>
   *         if it does not.
   */
  public static boolean hasNewValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(4) != null;
  }

  /**
   * Get the operand called Location from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Location
   */
  public static LocationOperand getLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (LocationOperand) i.getOperand(5);
  }
  /**
   * Get the operand called Location from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Location
   */
  public static LocationOperand getClearLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (LocationOperand) i.getClearOperand(5);
  }
  /**
   * Set the operand called Location in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Location the operand to store
   */
  public static void setLocation(Instruction i, LocationOperand Location) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    i.putOperand(5, Location);
  }
  /**
   * Return the index of the operand called Location
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Location
   *         in the argument instruction
   */
  public static int indexOfLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return 5;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Location?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Location or <code>false</code>
   *         if it does not.
   */
  public static boolean hasLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(5) != null;
  }

  /**
   * Get the operand called Guard from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getOperand(6);
  }
  /**
   * Get the operand called Guard from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getClearGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return (Operand) i.getClearOperand(6);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    i.putOperand(6, Guard);
  }
  /**
   * Return the index of the operand called Guard
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Guard
   *         in the argument instruction
   */
  public static int indexOfGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return 6;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "Attempt");
    return i.getOperand(6) != null;
  }


  /**
   * Create an instruction of the Attempt instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Address the instruction's Address operand
   * @param Offset the instruction's Offset operand
   * @param OldValue the instruction's OldValue operand
   * @param NewValue the instruction's NewValue operand
   * @param Location the instruction's Location operand
   * @param Guard the instruction's Guard operand
   * @return the newly created Attempt instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , Operand Address
                   , Operand Offset
                   , Operand OldValue
                   , Operand NewValue
                   , LocationOperand Location
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Attempt");
    Instruction i = new Instruction(o, 7);
    i.putOperand(0, Result);
    i.putOperand(1, Address);
    i.putOperand(2, Offset);
    i.putOperand(3, OldValue);
    i.putOperand(4, NewValue);
    i.putOperand(5, Location);
    i.putOperand(6, Guard);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Attempt instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Address the instruction's Address operand
   * @param Offset the instruction's Offset operand
   * @param OldValue the instruction's OldValue operand
   * @param NewValue the instruction's NewValue operand
   * @param Location the instruction's Location operand
   * @param Guard the instruction's Guard operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , Operand Address
                   , Operand Offset
                   , Operand OldValue
                   , Operand NewValue
                   , LocationOperand Location
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Attempt");
    i.resizeNumberOfOperands(7);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Address);
    i.putOperand(2, Offset);
    i.putOperand(3, OldValue);
    i.putOperand(4, NewValue);
    i.putOperand(5, Location);
    i.putOperand(6, Guard);
    return i;
  }
  /**
   * Create an instruction of the Attempt instruction format.
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Address the instruction's Address operand
   * @param Offset the instruction's Offset operand
   * @param OldValue the instruction's OldValue operand
   * @param NewValue the instruction's NewValue operand
   * @param Location the instruction's Location operand
   * @return the newly created Attempt instruction
   */
  public static Instruction create(Operator o
                   , RegisterOperand Result
                   , Operand Address
                   , Operand Offset
                   , Operand OldValue
                   , Operand NewValue
                   , LocationOperand Location
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Attempt");
    Instruction i = new Instruction(o, 7);
    i.putOperand(0, Result);
    i.putOperand(1, Address);
    i.putOperand(2, Offset);
    i.putOperand(3, OldValue);
    i.putOperand(4, NewValue);
    i.putOperand(5, Location);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * Attempt instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Result the instruction's Result operand
   * @param Address the instruction's Address operand
   * @param Offset the instruction's Offset operand
   * @param OldValue the instruction's OldValue operand
   * @param NewValue the instruction's NewValue operand
   * @param Location the instruction's Location operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , RegisterOperand Result
                   , Operand Address
                   , Operand Offset
                   , Operand OldValue
                   , Operand NewValue
                   , LocationOperand Location
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "Attempt");
    i.resizeNumberOfOperands(7);

    i.operator = o;
    i.putOperand(0, Result);
    i.putOperand(1, Address);
    i.putOperand(2, Offset);
    i.putOperand(3, OldValue);
    i.putOperand(4, NewValue);
    i.putOperand(5, Location);
    i.putOperand(6, null);
    return i;
  }
}

