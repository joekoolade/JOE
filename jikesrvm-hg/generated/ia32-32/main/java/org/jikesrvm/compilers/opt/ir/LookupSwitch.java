
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
 * The LookupSwitch InstructionFormat class.
 *
 * The header comment for {@link Instruction} contains
 * an explanation of the role of InstructionFormats in the
 * opt compiler's IR.
 */
@SuppressWarnings("unused")  // Machine generated code is never 100% clean
public final class LookupSwitch extends InstructionFormat {
  /**
   * InstructionFormat identification method for LookupSwitch.
   * @param i an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         instruction is LookupSwitch or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Instruction i) {
    return conforms(i.operator);
  }
  /**
   * InstructionFormat identification method for LookupSwitch.
   * @param o an instruction
   * @return <code>true</code> if the InstructionFormat of the argument
   *         operator is LookupSwitch or <code>false</code>
   *         if it is not.
   */
  public static boolean conforms(Operator o) {
    return o.format == LookupSwitch_format;
  }

  /**
   * Get the operand called Value from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Value
   */
  public static Operand getValue(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
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
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(0) != null;
  }

  /**
   * Get the operand called Unknown1 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Unknown1
   */
  public static Operand getUnknown1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (Operand) i.getOperand(1);
  }
  /**
   * Get the operand called Unknown1 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Unknown1
   */
  public static Operand getClearUnknown1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (Operand) i.getClearOperand(1);
  }
  /**
   * Set the operand called Unknown1 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Unknown1 the operand to store
   */
  public static void setUnknown1(Instruction i, Operand Unknown1) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(1, Unknown1);
  }
  /**
   * Return the index of the operand called Unknown1
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Unknown1
   *         in the argument instruction
   */
  public static int indexOfUnknown1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 1;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Unknown1?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Unknown1 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasUnknown1(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(1) != null;
  }

  /**
   * Get the operand called Unknown2 from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Unknown2
   */
  public static Operand getUnknown2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (Operand) i.getOperand(2);
  }
  /**
   * Get the operand called Unknown2 from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Unknown2
   */
  public static Operand getClearUnknown2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (Operand) i.getClearOperand(2);
  }
  /**
   * Set the operand called Unknown2 in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Unknown2 the operand to store
   */
  public static void setUnknown2(Instruction i, Operand Unknown2) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(2, Unknown2);
  }
  /**
   * Return the index of the operand called Unknown2
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Unknown2
   *         in the argument instruction
   */
  public static int indexOfUnknown2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 2;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Unknown2?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Unknown2 or <code>false</code>
   *         if it does not.
   */
  public static boolean hasUnknown2(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(2) != null;
  }

  /**
   * Get the operand called Default from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Default
   */
  public static BranchOperand getDefault(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchOperand) i.getOperand(3);
  }
  /**
   * Get the operand called Default from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called Default
   */
  public static BranchOperand getClearDefault(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchOperand) i.getClearOperand(3);
  }
  /**
   * Set the operand called Default in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param Default the operand to store
   */
  public static void setDefault(Instruction i, BranchOperand Default) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(3, Default);
  }
  /**
   * Return the index of the operand called Default
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called Default
   *         in the argument instruction
   */
  public static int indexOfDefault(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 3;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named Default?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named Default or <code>false</code>
   *         if it does not.
   */
  public static boolean hasDefault(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(3) != null;
  }

  /**
   * Get the operand called DefaultBranchProfile from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called DefaultBranchProfile
   */
  public static BranchProfileOperand getDefaultBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchProfileOperand) i.getOperand(4);
  }
  /**
   * Get the operand called DefaultBranchProfile from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @return the operand called DefaultBranchProfile
   */
  public static BranchProfileOperand getClearDefaultBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchProfileOperand) i.getClearOperand(4);
  }
  /**
   * Set the operand called DefaultBranchProfile in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param DefaultBranchProfile the operand to store
   */
  public static void setDefaultBranchProfile(Instruction i, BranchProfileOperand DefaultBranchProfile) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(4, DefaultBranchProfile);
  }
  /**
   * Return the index of the operand called DefaultBranchProfile
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the operand called DefaultBranchProfile
   *         in the argument instruction
   */
  public static int indexOfDefaultBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 4;
  }
  /**
   * Does the argument instruction have a non-null
   * operand named DefaultBranchProfile?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has an non-null
   *         operand named DefaultBranchProfile or <code>false</code>
   *         if it does not.
   */
  public static boolean hasDefaultBranchProfile(Instruction i) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(4) != null;
  }

  /**
   * Get the k'th operand called Match from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Match
   */
  public static IntConstantOperand getMatch(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (IntConstantOperand) i.getOperand(5+k*3+0);
  }
  /**
   * Get the k'th operand called Match from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Match
   */
  public static IntConstantOperand getClearMatch(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (IntConstantOperand) i.getClearOperand(5+k*3+0);
  }
  /**
   * Set the k'th operand called Match in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param k the index of the operand
   * @param o the operand to store
   */
  public static void setMatch(Instruction i, int k, IntConstantOperand o) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(5+k*3+0, o);
  }
  /**
   * Return the index of the k'th operand called Match
   * in the argument instruction.
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return the index of the k'th operand called Match
   *         in the argument instruction
   */
  public static int indexOfMatch(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 5+k*3+0;
  }
  /**
   * Does the argument instruction have a non-null
   * k'th operand named Match?
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return <code>true</code> if the instruction has an non-null
   *         k'th operand named Match or <code>false</code>
   *         if it does not.
   */
  public static boolean hasMatch(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(5+k*3+0) != null;
  }

  /**
   * Return the index of the first operand called Matches
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the first operand called Matches
   *         in the argument instruction
   */
  public static int indexOfMatches(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 5;
  }
  /**
   * Does the argument instruction have any Matches
   * operands?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has
   *         Matches operands or <code>false</code>
   *         if it does not.
   */
  public static boolean hasMatches(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getNumberOfOperands()-5 > 0 && i.getOperand(5) != null;
  }

  /**
   * How many variable-length operands called Matches
   * does the argument instruction have?
   * @param i the instruction to access
   * @return the number of Matches operands the instruction has
   */
  public static int getNumberOfMatches(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (i.getNumberOfOperands()-5)/3;
  }

  /**
   * Change the number of Matches operands that may be stored in
   * the argument instruction to numVarOps.
   * @param i the instruction to access
   * @param numVarOps the new number of variable operands called Matches
   *        that may be stored in the instruction
   */
  public static void resizeNumberOfMatches(Instruction i, int numVarOps)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
  if (5+numVarOps*3>MIN_OPERAND_ARRAY_LENGTH)
    i.resizeNumberOfOperands(5+numVarOps*3);
  else
    for (int j = 5+numVarOps*3; j < MIN_OPERAND_ARRAY_LENGTH; j++)
      i.putOperand(j, null);
  }
  /**
   * Get the k'th operand called Target from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Target
   */
  public static BranchOperand getTarget(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchOperand) i.getOperand(5+k*3+1);
  }
  /**
   * Get the k'th operand called Target from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called Target
   */
  public static BranchOperand getClearTarget(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchOperand) i.getClearOperand(5+k*3+1);
  }
  /**
   * Set the k'th operand called Target in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param k the index of the operand
   * @param o the operand to store
   */
  public static void setTarget(Instruction i, int k, BranchOperand o) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(5+k*3+1, o);
  }
  /**
   * Return the index of the k'th operand called Target
   * in the argument instruction.
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return the index of the k'th operand called Target
   *         in the argument instruction
   */
  public static int indexOfTarget(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 5+k*3+1;
  }
  /**
   * Does the argument instruction have a non-null
   * k'th operand named Target?
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return <code>true</code> if the instruction has an non-null
   *         k'th operand named Target or <code>false</code>
   *         if it does not.
   */
  public static boolean hasTarget(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(5+k*3+1) != null;
  }

  /**
   * Return the index of the first operand called Target
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the first operand called Target
   *         in the argument instruction
   */
  public static int indexOfTargets(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 6;
  }
  /**
   * Does the argument instruction have any operands
   * named Target?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has operands
   *         named Target or <code>false</code> if it does not.
   */
  public static boolean hasTargets(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getNumberOfOperands()-6 > 0 && i.getOperand(6) != null;
  }

  /**
   * How many variable-length operands called Targets
   * does the argument instruction have?
   * @param i the instruction to access
   * @return the number of operands called Targets the instruction has
   */
  public static int getNumberOfTargets(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (i.getNumberOfOperands()-5)/3;
  }

  /**
   * Change the number of Targets that may be stored in
   * the argument instruction to numVarOps.
   * @param i the instruction to access
   * @param numVarOps the new number of variable operands called Targets
   *        that may be stored in the instruction
   */
  public static void resizeNumberOfTargets(Instruction i, int numVarOps)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
  if (5+numVarOps*3>MIN_OPERAND_ARRAY_LENGTH)
    i.resizeNumberOfOperands(5+numVarOps*3);
  else
    for (int j = 5+numVarOps*3; j < MIN_OPERAND_ARRAY_LENGTH; j++)
      i.putOperand(j, null);
  }
  /**
   * Get the k'th operand called BranchProfile from the
   * argument instruction. Note that the returned operand
   * will still point to its containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called BranchProfile
   */
  public static BranchProfileOperand getBranchProfile(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchProfileOperand) i.getOperand(5+k*3+2);
  }
  /**
   * Get the k'th operand called BranchProfile from the argument
   * instruction clearing its instruction pointer. The returned
   * operand will not point to any containing instruction.
   * @param i the instruction to fetch the operand from
   * @param k the index of the operand
   * @return the k'th operand called BranchProfile
   */
  public static BranchProfileOperand getClearBranchProfile(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (BranchProfileOperand) i.getClearOperand(5+k*3+2);
  }
  /**
   * Set the k'th operand called BranchProfile in the argument
   * instruction to the argument operand. The operand will
   * now point to the argument instruction as its containing
   * instruction.
   * @param i the instruction in which to store the operand
   * @param k the index of the operand
   * @param o the operand to store
   */
  public static void setBranchProfile(Instruction i, int k, BranchProfileOperand o) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    i.putOperand(5+k*3+2, o);
  }
  /**
   * Return the index of the k'th operand called BranchProfile
   * in the argument instruction.
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return the index of the k'th operand called BranchProfile
   *         in the argument instruction
   */
  public static int indexOfBranchProfile(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 5+k*3+2;
  }
  /**
   * Does the argument instruction have a non-null
   * k'th operand named BranchProfile?
   * @param i the instruction to access.
   * @param k the index of the operand.
   * @return <code>true</code> if the instruction has an non-null
   *         k'th operand named BranchProfile or <code>false</code>
   *         if it does not.
   */
  public static boolean hasBranchProfile(Instruction i, int k) {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getOperand(5+k*3+2) != null;
  }

  /**
   * Return the index of the first operand called BranchProfile
   * in the argument instruction.
   * @param i the instruction to access.
   * @return the index of the first operand called BranchProfile
   *         in the argument instruction
   */
  public static int indexOfBranchProfiles(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return 7;
  }
  /**
   * Does the argument instruction have any operands
   * named BranchProfile?
   * @param i the instruction to access.
   * @return <code>true</code> if the instruction has operands
   *         named BranchProfile or <code>false</code> if it does not.
   */
  public static boolean hasBranchProfiles(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return i.getNumberOfOperands()-7 > 0 && i.getOperand(7) != null;
  }

  /**
   * How many variable-length operands called BranchProfiles
   * does the argument instruction have?
   * @param i the instruction to access
   * @return the number of operands called BranchProfiles the instruction has
   */
  public static int getNumberOfBranchProfiles(Instruction i)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
    return (i.getNumberOfOperands()-5)/3;
  }

  /**
   * Change the number of BranchProfiles that may be stored in
   * the argument instruction to numVarOps.
   * @param i the instruction to access
   * @param numVarOps the new number of variable operands called BranchProfiles
   *        that may be stored in the instruction
   */
  public static void resizeNumberOfBranchProfiles(Instruction i, int numVarOps)
  {
    if (Configuration.ExtremeAssertions && !conforms(i)) fail(i, "LookupSwitch");
  if (5+numVarOps*3>MIN_OPERAND_ARRAY_LENGTH)
    i.resizeNumberOfOperands(5+numVarOps*3);
  else
    for (int j = 5+numVarOps*3; j < MIN_OPERAND_ARRAY_LENGTH; j++)
      i.putOperand(j, null);
  }

  /**
   * Create an instruction of the LookupSwitch instruction format.
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Unknown1 the instruction's Unknown1 operand
   * @param Unknown2 the instruction's Unknown2 operand
   * @param Default the instruction's Default operand
   * @param DefaultBranchProfile the instruction's DefaultBranchProfile operand
   * @param numVarOps the number of variable length operands that
   *                 will be stored in the insruction.
   * @return the newly created LookupSwitch instruction
   */
  public static Instruction create(Operator o
                   , Operand Value
                   , Operand Unknown1
                   , Operand Unknown2
                   , BranchOperand Default
                   , BranchProfileOperand DefaultBranchProfile
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "LookupSwitch");
    Instruction i = new Instruction(o, Math.max(5+numVarOps*3, MIN_OPERAND_ARRAY_LENGTH));
    i.putOperand(0, Value);
    i.putOperand(1, Unknown1);
    i.putOperand(2, Unknown2);
    i.putOperand(3, Default);
    i.putOperand(4, DefaultBranchProfile);
    return i;
  }

  /**
   * Mutate the argument instruction into an instruction of the
   * LookupSwitch instruction format having the specified
   * operator and operands.
   * @param i the instruction to mutate
   * @param o the instruction's operator
   * @param Value the instruction's Value operand
   * @param Unknown1 the instruction's Unknown1 operand
   * @param Unknown2 the instruction's Unknown2 operand
   * @param Default the instruction's Default operand
   * @param DefaultBranchProfile the instruction's DefaultBranchProfile operand
   * @param numVarOps the number of variable length operands that
   *                  will be stored in the insruction.
   * @return the mutated instruction
   */
  public static Instruction mutate(Instruction i, Operator o
                   , Operand Value
                   , Operand Unknown1
                   , Operand Unknown2
                   , BranchOperand Default
                   , BranchProfileOperand DefaultBranchProfile
                   , int numVarOps
                )
  {
    if (Configuration.ExtremeAssertions && !conforms(o)) fail(o, "LookupSwitch");
    if (5+numVarOps*3>MIN_OPERAND_ARRAY_LENGTH)
      i.resizeNumberOfOperands(5+numVarOps*3);

    i.operator = o;
    i.putOperand(0, Value);
    i.putOperand(1, Unknown1);
    i.putOperand(2, Unknown2);
    i.putOperand(3, Default);
    i.putOperand(4, DefaultBranchProfile);
    return i;
  }
}

