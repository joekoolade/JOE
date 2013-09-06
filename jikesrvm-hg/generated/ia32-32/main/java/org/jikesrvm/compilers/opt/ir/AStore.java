
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
 * The AStore InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class AStore extends InstructionFormat {
  /**
   * InstructionFormat identification method for AStore.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is AStore or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for AStore.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is AStore or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == AStore_format;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Array from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Array
   */
  public static Operand getArray(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Array from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Array
   */
  public static Operand getClearArray(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Array in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Array the operand to store
   */
  public static void setArray(Instruction i, Operand Array) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    i.putOperand(1, Array);
  }
  /**
   * Return the index of the operand called Array
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Array
   *         in the argument instruction
   */
  public static int indexOfArray(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Array?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Array or <code>false</code>
   *         if it does not.
   */
  public static boolean hasArray(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Index from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Index
   */
  public static Operand getIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Index from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Index
   */
  public static Operand getClearIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Index in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Index the operand to store
   */
  public static void setIndex(Instruction i, Operand Index) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    i.putOperand(2, Index);
  }
  /**
   * Return the index of the operand called Index
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Index
   *         in the argument instruction
   */
  public static int indexOfIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Index?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Index or <code>false</code>
   *         if it does not.
   */
  public static boolean hasIndex(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Location from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Location
   */
  public static LocationOperand getLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (LocationOperand) i.getOperand(3);
  }
  /**
   * Get the operand called Location from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Location
   */
  public static LocationOperand getClearLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (LocationOperand) i.getClearOperand(3);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    i.putOperand(3, Location);
  }
  /**
   * Return the index of the operand called Location
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Location
   *         in the argument instruction
   */
  public static int indexOfLocation(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return 3;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called Guard from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (Operand) i.getOperand(4);
  }
  /**
   * Get the operand called Guard from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Guard
   */
  public static Operand getClearGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return (Operand) i.getClearOperand(4);
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    i.putOperand(4, Guard);
  }
  /**
   * Return the index of the operand called Guard
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Guard
   *         in the argument instruction
   */
  public static int indexOfGuard(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return 4;
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "AStore");
    return i.getOperand(4) != null;
  }


  /**
   * Create an instruction of the AStore instruction format.
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Array the instruction's Array operand
   * @param Index the instruction's Index operand
   * @param Location the instruction's Location operand
   * @param Guard the instruction's Guard operand
   * @return the newly created AStore instruction
   */
  public static Instruction create(Operator o
                   , Operand Value
                   , Operand Array
                   , Operand Index
                   , LocationOperand Location
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "AStore");
    Instruction i = new Instruction(o, 5);
    i.putOperand(0, Value);
    i.putOperand(1, Array);
    i.putOperand(2, Index);
    i.putOperand(3, Location);
    i.putOperand(4, Guard);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * AStore instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Array the instruction's Array operand
   * @param Index the instruction's Index operand
   * @param Location the instruction's Location operand
   * @param Guard the instruction's Guard operand
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Value
                   , Operand Array
                   , Operand Index
                   , LocationOperand Location
                   , Operand Guard
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "AStore");
    i.operator = o;
    i.putOperand(0, Value);
    i.putOperand(1, Array);
    i.putOperand(2, Index);
    i.putOperand(3, Location);
    i.putOperand(4, Guard);
    return i;
  }
}

