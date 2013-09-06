/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
/*
 * NOTE: Operator.java is mechanically generated from
 * Operator.template using the operator definitions in
 * OperatorList.dat and ppcOperatorList.dat
 *
 * DO NOT MANUALLY EDIT THE JAVA FILE.
 */

package org.jikesrvm.compilers.opt.ir;

import org.jikesrvm.*;
import org.jikesrvm.ArchitectureSpecificOpt.PhysicalDefUse;
import org.jikesrvm.compilers.opt.*;
import org.jikesrvm.compilers.opt.instrsched.OperatorClass;
import org.jikesrvm.compilers.opt.util.Bits;

/**
 * An Operator represents the operator of an {@link Instruction}.
 * For each operator in the IR, we create exactly one Operator instance
 * to represent it. These instances are all stored in static fields
 * of {@link Operators}. Since only one instance is created for each
 * semantic operator, they can be compared using <code>==</code>.
 * <p>
 * A common coding practive is to implement the {@link Operators}
 * interface to be able to reference the IR operators within a class
 * without having to prepend 'Operators.' everywhere.
 *
 * @see Operators
 * @see Instruction
 * @see OperatorNames
 */
public final class Operator {

  /**
   * The operators opcode.
   * This value serves as a unique id suitable for use in switches
   */
  public final char opcode;

  /**
   * Encoding of the operator's InstructionFormat.
   * This field is only meant to be directly referenced
   * from the mechanically generated InstructionFormat
   * classes defined in the instructionFormats package.
   * {@link Instruction} contains an explanation
   * of the role of InstructionFormats in the IR.
   */
  public final byte format;

  /**
   * encoding of operator traits (characteristics)
   */
  private final int traits;

  /**
   * How many operands of the operator are (pure) defs?
   */
  private final int numberDefs;

  /**
   * How many operands of the operator are both defs and uses?
   * Only non-zero on IA32, 390.
   */
  private final int numberDefUses;

  /**
   * How many operands of the operator are pure uses?
   * Only contains a valid value for non-variableLength operators
   */
  private final int numberUses;

  /**
   * Physical registers that are implicitly defined by the operator.
   */
  public final int implicitDefs;

  /**
   * Physical registers that are implicitly used by the operator.
   */
  public final int implicitUses;

  /**
   * Instruction template used by the assembler to
   * generate binary code.  Only valid on MIR operators.
   */
  public int instTemplate;

  /**
   * Operator Class of the operator; used for instruction scheduling.
   */
  OperatorClass opClass;

  /**
   * Sets the operator class.
   *
   * @param opClass operator class
   */
  public void setOpClass(OperatorClass opClass) {
    this.opClass = opClass;
  }

  /**
   * Gets the operator class.
   *
   * @return operator class
   */
  public OperatorClass getOpClass() {
    return opClass;
  }


  /**
   * Returns the string representation of this operator.
   *
   * @return the name of the operator
   */
  @Override
  public String toString() {
    return OperatorNames.toString(this);
  }

  /**
   * Returns the number of operands that are defs.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return number of operands that are pure defs
   */
  public int getNumberOfPureDefs() {
    if (VM.VerifyAssertions) VM._assert(!hasVarDefs());
    return numberDefs;
  }

  /**
   * Returns the number of operands that are pure defs
   * and are not in the variable-length part of the operand list.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return how many non-variable operands are pure defs
   */
  public int getNumberOfFixedPureDefs() {
    return numberDefs;
  }

  /**
   * Returns the number of operands that are pure uses
   * and are not in the variable-length part of the operand list.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return how many non-variable operands are pure uses
   */
  public int getNumberOfFixedPureUses() {
    return numberUses;
  }

  /**
   * Returns the number of operands that are defs
   * and uses.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return number of operands that are combined defs and uses
   */
  public int getNumberOfDefUses() {
    return numberDefUses;
  }

  /**
   * Returns the number of operands that are pure uses.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return number of operands that are pure uses
   */
  public int getNumberOfPureUses() {
    return numberUses;
  }

  /**
   * Returns the number of operands that are defs
   * (either pure defs or combined def/uses).
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return number of operands that are defs
   */
  public int getNumberOfDefs() {
    if (VM.VerifyAssertions) VM._assert(!hasVarDefs());
    return numberDefs + numberDefUses;
  }

  /**
   * Returns the number of operands that are uses
   * (either combined def/uses or pure uses).
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return how many operands are uses
   */
  public int getNumberOfUses() {
    if (VM.VerifyAssertions) VM._assert(!hasVarUses());
    return numberDefUses + numberUses;
  }

  /**
   * Returns the number of operands that are pure uses
   * and are not in the variable-length part of the operand list.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return how many non-variable operands are pure uses
   */
  public int getNumberOfPureFixedUses() {
    return numberUses;
  }

  /**
   * Returns the number of operands that are uses
   * (either combined use/defs or pure uses)
   * and are not in the variable-length part of the operand list.
   * By convention, operands are ordered in instructions
   * such that all defs are first, followed by all
   * combined defs/uses, followed by all pure uses.
   *
   * @return number of non-variable operands are uses
   */
  public int getNumberOfFixedUses() {
    return numberDefUses + numberUses;
  }

  /**
   * Returns the number of physical registers that are
   * implicitly defined by this operator.
   *
   * @return number of implicit defs
   */
  public int getNumberOfImplicitDefs() {
    return Integer.bitCount(implicitDefs);
  }

  /**
   * Returns the number of physical registers that are
   * implicitly used by this operator.
   *
   * @return number of implicit uses
   */
  public int getNumberOfImplicitUses() {
    return Integer.bitCount(implicitUses);
  }

  /*
   * The following are used to encode operator traits in OperatorList.dat.
   * Had to make a few of them public (yuck) to let us get at them
   * from InstructionFormat.java.
   */
  // operator has no interesting traits
  public static final int none         = 0x00000000;
  // operator is a simple move operation from one "register" to another
  private static final int move         = 0x00000001;
  // operator is an intraprocedural branch of some form
  private static final int branch       = 0x00000002;
  // operator is some kind of call (interprocedural branch)
  private static final int call         = 0x00000004;
  // modifer for branches/calls
  private static final int conditional  = 0x00000008;
  // modifier for branches/calls, mostly on MIR
  private static final int indirect     = 0x00000010;
  // an explicit load of a value from memory
  private static final int load         = 0x00000020;
  // operator is modeled as a load by memory system, mostly on MIR
  private static final int memAsLoad    = 0x00000040;
  // an explicit store of a value to memory
  private static final int store        = 0x00000080;
  // operator is modeled as a store by memory system, mostly on MIR
  private static final int memAsStore   = 0x00000100;
  // is an exception throw
  private static final int ethrow       = 0x00000200;
  // an immediate PEI (null_check, int_zero_check, but _not_ call);
  private static final int immedPEI     = 0x00000400;
  // operator is some kind of compare (val,val)-> cond
  private static final int compare      = 0x00000800;
  // an explicit memory allocation
  private static final int alloc        = 0x00001000;
  // a return instruction (interprocedural branch)
  private static final int ret          = 0x00002000;
  // operator has a variable number of uses
  public static final int varUses      = 0x00004000;
  // operator has a variable number of defs
  public static final int varDefs      = 0x00008000;
  // operator is a potential thread switch point for some reason
  // other than being a call/immedPEI
  private static final int tsp          = 0x00010000;
  // operator is an acquire (monitorenter/lock) HIR only
  private static final int acquire      = 0x00020000;
  // operator is a relase (monitorexit/unlock) HIR only
  private static final int release      = 0x00040000;
  // operator either directly or indirectly may casue dynamic linking
  private static final int dynLink      = 0x00080000;
  // operator is a yield point
  private static final int yieldPoint   = 0x00100000;
  // operator pops floating-point stack after performing defs
  private static final int fpPop        = 0x00200000;
  // operator pushs floating-point stack before performing defs
  private static final int fpPush       = 0x00400000;
  // operator is commutative
  private static final int commutative  = 0x00800000;

  /**
   * Does the operator represent a simple move (the value is unchanged)
   * from one "register" location to another "register" location?
   *
   * @return <code>true</code> if the operator is a simple move
   *         or <code>false</code> if it is not.
   */
  public boolean isMove() {
    return (traits & move) != 0;
  }

  /**
   * Is the operator an intraprocedural branch?
   *
   * @return <code>true</code> if the operator is am
   *         intraprocedural branch or <code>false</code> if it is not.
   */
  public boolean isBranch() {
    return (traits & branch) != 0;
  }

  /**
   * Is the operator a conditional intraprocedural branch?
   *
   * @return <code>true</code> if the operator is a conditoonal
   *         intraprocedural branch or <code>false</code> if it is not.
   */
  public boolean isConditionalBranch() {
    return (traits & (branch|conditional)) == (branch|conditional);
  }

  /**
   * Is the operator an unconditional intraprocedural branch?
   * We consider various forms of switches to be unconditional
   * intraprocedural branches, even though they are multi-way branches
   * and we may not no exactly which target will be taken.
   * This turns out to be the right thing to do, since some
   * arm of the switch will always be taken (unlike conditional branches).
   *
   * @return <code>true</code> if the operator is an unconditional
   *         intraprocedural branch or <code>false</code> if it is not.
   */
  public boolean isUnconditionalBranch() {
    return (traits & (branch|conditional)) == branch;
  }

  /**
   * Is the operator a direct intraprocedural branch?
   * In the HIR and LIR we consider switches to be direct branches,
   * because their targets are known precisely.
   *
   * @return <code>true</code> if the operator is a direct
   *         intraprocedural branch or <code>false</code> if it is not.
   */
  public boolean isDirectBranch() {
    return (traits & (branch|indirect)) == branch;
  }

  /**
   * Is the operator an indirect intraprocedural branch?
   *
   * @return <code>true</code> if the operator is an indirect
   *         interprocedural branch or <code>false</code> if it is not.
   */
  public boolean isIndirectBranch() {
    return (traits & (branch|indirect)) == (branch|indirect);
  }

  /**
   * Is the operator a call (one kind of interprocedural branch)?
   *
   * @return <code>true</code> if the operator is a call
   *         or <code>false</code> if it is not.
   */
  public boolean isCall() {
    return (traits & call) != 0;
  }

  /**
   * Is the operator a conditional call?
   * We only allow conditional calls in the MIR, since they
   * tend to only be directly implementable on some architecutres.
   *
   * @return <code>true</code> if the operator is a
   *         conditional call or <code>false</code> if it is not.
   */
  public boolean isConditionalCall() {
    return (traits & (call|conditional)) == (call|conditional);
  }

  /**
   * Is the operator an unconditional call?
   * Really only an interesting question in the MIR, since
   * it is by definition true for all HIR and LIR calls.
   *
   * @return <code>true</code> if the operator is an unconditional
   *         call or <code>false</code> if it is not.
   */
  public boolean isUnconditionalCall() {
    return (traits & (call|conditional)) == call;
  }

  /**
   * Is the operator a direct call?
   * Only interesting on the MIR.  In the HIR and LIR we pretend that
   * all calls are "direct" even though most of them aren't.
   *
   * @return <code>true</code> if the operator is a direct call
   *         or <code>false</code> if it is not.
   */
  public boolean isDirectCall() {
    return (traits & (call|indirect)) == call;
  }

  /**
   * Is the operator an indirect call?
   * Only interesting on the MIR.  In the HIR and LIR we pretend that
   * all calls are "direct" even though most of them aren't.
   *
   * @return <code>true</code> if the operator is an indirect call
   *         or <code>false</code> if it is not.
   */
  public boolean isIndirectCall() {
    return (traits & (call|indirect)) == (call|indirect);
  }

  /**
   * Is the operator an explicit load of a finite set of values from
   * a finite set of memory locations (load, load multiple, _not_ call)?
   *
   * @return <code>true</code> if the operator is an explicit load
   *         or <code>false</code> if it is not.
   */
  public boolean isExplicitLoad() {
    return (traits & load) != 0;
  }

  /**
   * Should the operator be treated as a load from some unknown location(s)
   * for the purposes of scheduling and/or modeling the memory subsystem?
   *
   * @return <code>true</code> if the operator is an implicit load
   *         or <code>false</code> if it is not.
   */
  public boolean isImplicitLoad() {
    return (traits & (load|memAsLoad|call)) != 0;
  }

  /**
   * Is the operator an explicit store of a finite set of values to
   * a finite set of memory locations (store, store multiple, _not_ call)?
   *
   * @return <code>true</code> if the operator is an explicit store
   *         or <code>false</code> if it is not.
   */
  public boolean isExplicitStore() {
    return (traits & store) != 0;
  }

  /**
   * Should the operator be treated as a store to some unknown location(s)
   * for the purposes of scheduling and/or modeling the memory subsystem?
   *
   * @return <code>true</code> if the operator is an implicit store
   *         or <code>false</code> if it is not.
   */
  public boolean isImplicitStore() {
    return (traits & (store|memAsStore|call)) != 0;
  }

  /**
   * Is the operator a throw of a Java exception?
   *
   * @return <code>true</code> if the operator is a throw
   *         or <code>false</code> if it is not.
   */
  public boolean isThrow() {
    return (traits & ethrow) != 0;
  }

  /**
   * Is the operator a PEI (Potentially Excepting Instruction)?
   *
   * @return <code>true</code> if the operator is a PEI
   *         or <code>false</code> if it is not.
   */
  public boolean isPEI() {
    return (traits & (ethrow|immedPEI)) != 0;
  }

  /**
   * Is the operator a potential GC point?
   *
   * @return <code>true</code> if the operator is a potential
   *         GC point or <code>false</code> if it is not.
   */
  public boolean isGCPoint() {
    return isPEI() || ((traits & (alloc|tsp)) != 0);
  }

  /**
   * is the operator a potential thread switch point?
   *
   * @return <code>true</code> if the operator is a potential
   *         threadswitch point or <code>false</code> if it is not.
   */
  public boolean isTSPoint() {
    return isGCPoint();
  }

  /**
   * Is the operator a compare (val,val) => condition?
   *
   * @return <code>true</code> if the operator is a compare
   *         or <code>false</code> if it is not.
   */
  public boolean isCompare() {
    return (traits & compare) != 0;
  }

  /**
   * Is the operator an actual memory allocation instruction
   * (NEW, NEWARRAY, etc)?
   *
   * @return <code>true</code> if the operator is an allocation
   *         or <code>false</code> if it is not.
   */
  public boolean isAllocation() {
    return (traits & alloc) != 0;
  }

  /**
   * Is the operator a return (interprocedural branch)?
   *
   * @return <code>true</code> if the operator is a return
   *         or <code>false</code> if it is not.
   */
  public boolean isReturn() {
    return (traits & ret) != 0;
  }

  /**
   * Can the operator have a variable number of uses?
   *
   * @return <code>true</code> if the operator has a variable number
   *         of uses or <code>false</code> if it does not.
   */
  public boolean hasVarUses() {
    return (traits & varUses) != 0;
  }

  /**
   * Can the operator have a variable number of uses?
   *
   * @return <code>true</code> if the operator has a variable number
   *         of uses or <code>false</code> if it does not.
   */
  public boolean hasVarDefs() {
    return (traits & varDefs) != 0;
  }

  /**
   * Can the operator have a variable number of uses or defs?
   *
   * @return <code>true</code> if the operator has a variable number
   *         of uses or defs or <code>false</code> if it does not.
   */
  public boolean hasVarUsesOrDefs() {
    return (traits & (varUses | varDefs)) != 0;
  }

  /**
   * Is the operator an acquire (monitorenter/lock)?
   *
   * @return <code>true</code> if the operator is an acquire
   *         or <code>false</code> if it is not.
   */
  public boolean isAcquire() {
    return (traits & acquire) != 0;
  }

  /**
   * Is the operator a release (monitorexit/unlock)?
   *
   * @return <code>true</code> if the operator is a release
   *         or <code>false</code> if it is not.
   */
  public boolean isRelease() {
    return (traits & release) != 0;
  }

  /**
   * Could the operator either directly or indirectly
   * cause dynamic class loading?
   *
   * @return <code>true</code> if the operator is a dynamic linking point
   *         or <code>false</code> if it is not.
   */
  public boolean isDynamicLinkingPoint() {
    return (traits & dynLink) != 0;
  }

  /**
   * Is the operator a yield point?
   *
   * @return <code>true</code> if the operator is a yield point
   *         or <code>false</code> if it is not.
   */
  public boolean isYieldPoint() {
    return (traits & yieldPoint) != 0;
  }

  /**
   * Does the operator pop the floating-point stack?
   *
   * @return <code>true</code> if the operator pops the floating-point
   * stack.
   *         or <code>false</code> if not.
   */
  public boolean isFpPop() {
    return (traits & fpPop) != 0;
  }

  /**
   * Does the operator push on the floating-point stack?
   *
   * @return <code>true</code> if the operator pushes on the floating-point
   * stack.
   *         or <code>false</code> if not.
   */
  public boolean isFpPush() {
    return (traits & fpPush) != 0;
  }

  /**
   * Is the operator commutative?
   *
   * @return <code>true</code> if the operator is commutative.
   *         or <code>false</code> if not.
   */
  public boolean isCommutative() {
    return (traits & commutative) != 0;
  }


  public static final Operator[] OperatorArray = {
     new Operator((char)0, InstructionFormat.Nullary_format,  //GET_CAUGHT_EXCEPTION
                      (none | InstructionFormat.Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)1, InstructionFormat.CacheOp_format,  //SET_CAUGHT_EXCEPTION
                      (none | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)2, InstructionFormat.New_format,  //NEW
                      (alloc | immedPEI | InstructionFormat.New_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)3, InstructionFormat.New_format,  //NEW_UNRESOLVED
                      (alloc | immedPEI | dynLink | InstructionFormat.New_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)4, InstructionFormat.NewArray_format,  //NEWARRAY
                      (alloc | immedPEI | InstructionFormat.NewArray_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)5, InstructionFormat.NewArray_format,  //NEWARRAY_UNRESOLVED
                      (alloc | immedPEI | dynLink | InstructionFormat.NewArray_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)6, InstructionFormat.Athrow_format,  //ATHROW
                      (ethrow | InstructionFormat.Athrow_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)7, InstructionFormat.TypeCheck_format,  //CHECKCAST
                      (immedPEI | InstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)8, InstructionFormat.TypeCheck_format,  //CHECKCAST_NOTNULL
                      (immedPEI | InstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)9, InstructionFormat.TypeCheck_format,  //CHECKCAST_UNRESOLVED
                      (immedPEI | dynLink | InstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)10, InstructionFormat.TypeCheck_format,  //MUST_IMPLEMENT_INTERFACE
                      (immedPEI | InstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)11, InstructionFormat.InstanceOf_format,  //INSTANCEOF
                      (none | InstructionFormat.InstanceOf_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)12, InstructionFormat.InstanceOf_format,  //INSTANCEOF_NOTNULL
                      (none | InstructionFormat.InstanceOf_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)13, InstructionFormat.InstanceOf_format,  //INSTANCEOF_UNRESOLVED
                      (immedPEI | dynLink | InstructionFormat.InstanceOf_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)14, InstructionFormat.MonitorOp_format,  //MONITORENTER
                      (memAsLoad | memAsStore | acquire | tsp | InstructionFormat.MonitorOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)15, InstructionFormat.MonitorOp_format,  //MONITOREXIT
                      (memAsLoad | memAsStore | release | tsp | immedPEI | InstructionFormat.MonitorOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)16, InstructionFormat.Multianewarray_format,  //NEWOBJMULTIARRAY
                      (alloc | immedPEI | dynLink | InstructionFormat.Multianewarray_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)17, InstructionFormat.GetStatic_format,  //GETSTATIC
                      (load | InstructionFormat.GetStatic_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)18, InstructionFormat.PutStatic_format,  //PUTSTATIC
                      (store | InstructionFormat.PutStatic_traits),
                      0, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)19, InstructionFormat.GetField_format,  //GETFIELD
                      (load | InstructionFormat.GetField_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)20, InstructionFormat.PutField_format,  //PUTFIELD
                      (store | InstructionFormat.PutField_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)21, InstructionFormat.ZeroCheck_format,  //INT_ZERO_CHECK
                      (immedPEI | InstructionFormat.ZeroCheck_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)22, InstructionFormat.ZeroCheck_format,  //LONG_ZERO_CHECK
                      (immedPEI | InstructionFormat.ZeroCheck_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)23, InstructionFormat.BoundsCheck_format,  //BOUNDS_CHECK
                      (immedPEI | InstructionFormat.BoundsCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)24, InstructionFormat.StoreCheck_format,  //OBJARRAY_STORE_CHECK
                      (immedPEI | InstructionFormat.StoreCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)25, InstructionFormat.StoreCheck_format,  //OBJARRAY_STORE_CHECK_NOTNULL
                      (immedPEI | InstructionFormat.StoreCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)26, InstructionFormat.InlineGuard_format,  //IG_PATCH_POINT
                      (branch | conditional | InstructionFormat.InlineGuard_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)27, InstructionFormat.InlineGuard_format,  //IG_CLASS_TEST
                      (branch | conditional | InstructionFormat.InlineGuard_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)28, InstructionFormat.InlineGuard_format,  //IG_METHOD_TEST
                      (branch | conditional | InstructionFormat.InlineGuard_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)29, InstructionFormat.TableSwitch_format,  //TABLESWITCH
                      (branch | InstructionFormat.TableSwitch_traits),
                      0, 0, 7,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)30, InstructionFormat.LookupSwitch_format,  //LOOKUPSWITCH
                      (branch | InstructionFormat.LookupSwitch_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)31, InstructionFormat.ALoad_format,  //INT_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)32, InstructionFormat.ALoad_format,  //LONG_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)33, InstructionFormat.ALoad_format,  //FLOAT_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)34, InstructionFormat.ALoad_format,  //DOUBLE_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)35, InstructionFormat.ALoad_format,  //REF_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)36, InstructionFormat.ALoad_format,  //UBYTE_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)37, InstructionFormat.ALoad_format,  //BYTE_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)38, InstructionFormat.ALoad_format,  //USHORT_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)39, InstructionFormat.ALoad_format,  //SHORT_ALOAD
                      (load | InstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)40, InstructionFormat.AStore_format,  //INT_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)41, InstructionFormat.AStore_format,  //LONG_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)42, InstructionFormat.AStore_format,  //FLOAT_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)43, InstructionFormat.AStore_format,  //DOUBLE_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)44, InstructionFormat.AStore_format,  //REF_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)45, InstructionFormat.AStore_format,  //BYTE_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)46, InstructionFormat.AStore_format,  //SHORT_ASTORE
                      (store | InstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)47, InstructionFormat.IfCmp_format,  //INT_IFCMP
                      (branch | conditional | InstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)48, InstructionFormat.IfCmp2_format,  //INT_IFCMP2
                      (branch | conditional | InstructionFormat.IfCmp2_traits),
                      1, 0, 8,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)49, InstructionFormat.IfCmp_format,  //LONG_IFCMP
                      (branch | conditional | InstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)50, InstructionFormat.IfCmp_format,  //FLOAT_IFCMP
                      (branch | conditional | InstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)51, InstructionFormat.IfCmp_format,  //DOUBLE_IFCMP
                      (branch | conditional | InstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)52, InstructionFormat.IfCmp_format,  //REF_IFCMP
                      (branch | conditional | InstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)53, InstructionFormat.Label_format,  //LABEL
                      (none | InstructionFormat.Label_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)54, InstructionFormat.BBend_format,  //BBEND
                      (none | InstructionFormat.BBend_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)55, InstructionFormat.Empty_format,  //UNINT_BEGIN
                      (none | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)56, InstructionFormat.Empty_format,  //UNINT_END
                      (none | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)57, InstructionFormat.Empty_format,  //FENCE
                      (memAsLoad | memAsStore | release | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)58, InstructionFormat.Empty_format,  //READ_CEILING
                      (memAsLoad | memAsStore | acquire | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)59, InstructionFormat.Empty_format,  //WRITE_FLOOR
                      (memAsLoad | memAsStore | release | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)60, InstructionFormat.Phi_format,  //PHI
                      (none | InstructionFormat.Phi_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)61, InstructionFormat.Unary_format,  //SPLIT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)62, InstructionFormat.GuardedUnary_format,  //PI
                      (none | InstructionFormat.GuardedUnary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)63, InstructionFormat.Empty_format,  //NOP
                      (none | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)64, InstructionFormat.Move_format,  //INT_MOVE
                      (move | InstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)65, InstructionFormat.Move_format,  //LONG_MOVE
                      (move | InstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)66, InstructionFormat.Move_format,  //FLOAT_MOVE
                      (move | InstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)67, InstructionFormat.Move_format,  //DOUBLE_MOVE
                      (move | InstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)68, InstructionFormat.Move_format,  //REF_MOVE
                      (move | InstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)69, InstructionFormat.Move_format,  //GUARD_MOVE
                      (move | InstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)70, InstructionFormat.CondMove_format,  //INT_COND_MOVE
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)71, InstructionFormat.CondMove_format,  //LONG_COND_MOVE
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)72, InstructionFormat.CondMove_format,  //FLOAT_COND_MOVE
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)73, InstructionFormat.CondMove_format,  //DOUBLE_COND_MOVE
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)74, InstructionFormat.CondMove_format,  //REF_COND_MOVE
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)75, InstructionFormat.CondMove_format,  //GUARD_COND_MOVE
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)76, InstructionFormat.Binary_format,  //GUARD_COMBINE
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)77, InstructionFormat.Binary_format,  //REF_ADD
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)78, InstructionFormat.Binary_format,  //INT_ADD
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)79, InstructionFormat.Binary_format,  //LONG_ADD
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)80, InstructionFormat.Binary_format,  //FLOAT_ADD
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)81, InstructionFormat.Binary_format,  //DOUBLE_ADD
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)82, InstructionFormat.Binary_format,  //REF_SUB
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)83, InstructionFormat.Binary_format,  //INT_SUB
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)84, InstructionFormat.Binary_format,  //LONG_SUB
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)85, InstructionFormat.Binary_format,  //FLOAT_SUB
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)86, InstructionFormat.Binary_format,  //DOUBLE_SUB
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)87, InstructionFormat.Binary_format,  //INT_MUL
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)88, InstructionFormat.Binary_format,  //LONG_MUL
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)89, InstructionFormat.Binary_format,  //FLOAT_MUL
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)90, InstructionFormat.Binary_format,  //DOUBLE_MUL
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)91, InstructionFormat.GuardedBinary_format,  //INT_DIV
                      (none | InstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)92, InstructionFormat.GuardedBinary_format,  //LONG_DIV
                      (none | InstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)93, InstructionFormat.Binary_format,  //FLOAT_DIV
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)94, InstructionFormat.Binary_format,  //DOUBLE_DIV
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)95, InstructionFormat.GuardedBinary_format,  //INT_REM
                      (none | InstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)96, InstructionFormat.GuardedBinary_format,  //LONG_REM
                      (none | InstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)97, InstructionFormat.Binary_format,  //FLOAT_REM
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new Operator((char)98, InstructionFormat.Binary_format,  //DOUBLE_REM
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new Operator((char)99, InstructionFormat.Unary_format,  //REF_NEG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)100, InstructionFormat.Unary_format,  //INT_NEG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)101, InstructionFormat.Unary_format,  //LONG_NEG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)102, InstructionFormat.Unary_format,  //FLOAT_NEG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)103, InstructionFormat.Unary_format,  //DOUBLE_NEG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)104, InstructionFormat.Unary_format,  //FLOAT_SQRT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)105, InstructionFormat.Unary_format,  //DOUBLE_SQRT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)106, InstructionFormat.Binary_format,  //REF_SHL
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)107, InstructionFormat.Binary_format,  //INT_SHL
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)108, InstructionFormat.Binary_format,  //LONG_SHL
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)109, InstructionFormat.Binary_format,  //REF_SHR
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)110, InstructionFormat.Binary_format,  //INT_SHR
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)111, InstructionFormat.Binary_format,  //LONG_SHR
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)112, InstructionFormat.Binary_format,  //REF_USHR
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)113, InstructionFormat.Binary_format,  //INT_USHR
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)114, InstructionFormat.Binary_format,  //LONG_USHR
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)115, InstructionFormat.Binary_format,  //REF_AND
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)116, InstructionFormat.Binary_format,  //INT_AND
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)117, InstructionFormat.Binary_format,  //LONG_AND
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)118, InstructionFormat.Binary_format,  //REF_OR
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)119, InstructionFormat.Binary_format,  //INT_OR
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)120, InstructionFormat.Binary_format,  //LONG_OR
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)121, InstructionFormat.Binary_format,  //REF_XOR
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)122, InstructionFormat.Binary_format,  //INT_XOR
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)123, InstructionFormat.Unary_format,  //REF_NOT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)124, InstructionFormat.Unary_format,  //INT_NOT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)125, InstructionFormat.Unary_format,  //LONG_NOT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)126, InstructionFormat.Binary_format,  //LONG_XOR
                      (commutative | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)127, InstructionFormat.Unary_format,  //INT_2ADDRSigExt
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)128, InstructionFormat.Unary_format,  //INT_2ADDRZerExt
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)129, InstructionFormat.Unary_format,  //LONG_2ADDR
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)130, InstructionFormat.Unary_format,  //ADDR_2INT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)131, InstructionFormat.Unary_format,  //ADDR_2LONG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)132, InstructionFormat.Unary_format,  //INT_2LONG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)133, InstructionFormat.Unary_format,  //INT_2FLOAT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new Operator((char)134, InstructionFormat.Unary_format,  //INT_2DOUBLE
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new Operator((char)135, InstructionFormat.Unary_format,  //LONG_2INT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)136, InstructionFormat.Unary_format,  //LONG_2FLOAT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)137, InstructionFormat.Unary_format,  //LONG_2DOUBLE
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)138, InstructionFormat.Unary_format,  //FLOAT_2INT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)139, InstructionFormat.Unary_format,  //FLOAT_2LONG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)140, InstructionFormat.Unary_format,  //FLOAT_2DOUBLE
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)141, InstructionFormat.Unary_format,  //DOUBLE_2INT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)142, InstructionFormat.Unary_format,  //DOUBLE_2LONG
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)143, InstructionFormat.Unary_format,  //DOUBLE_2FLOAT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)144, InstructionFormat.Unary_format,  //INT_2BYTE
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)145, InstructionFormat.Unary_format,  //INT_2USHORT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)146, InstructionFormat.Unary_format,  //INT_2SHORT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)147, InstructionFormat.Binary_format,  //LONG_CMP
                      (compare | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)148, InstructionFormat.Binary_format,  //FLOAT_CMPL
                      (compare | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)149, InstructionFormat.Binary_format,  //FLOAT_CMPG
                      (compare | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)150, InstructionFormat.Binary_format,  //DOUBLE_CMPL
                      (compare | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)151, InstructionFormat.Binary_format,  //DOUBLE_CMPG
                      (compare | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)152, InstructionFormat.Return_format,  //RETURN
                      (ret | InstructionFormat.Return_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)153, InstructionFormat.NullCheck_format,  //NULL_CHECK
                      (immedPEI | InstructionFormat.NullCheck_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)154, InstructionFormat.Goto_format,  //GOTO
                      (branch | InstructionFormat.Goto_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)155, InstructionFormat.Unary_format,  //BOOLEAN_NOT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)156, InstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_INT
                      (compare | InstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)157, InstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_ADDR
                      (compare | InstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)158, InstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_LONG
                      (compare | InstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)159, InstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_FLOAT
                      (compare | InstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)160, InstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_DOUBLE
                      (compare | InstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)161, InstructionFormat.Load_format,  //BYTE_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)162, InstructionFormat.Load_format,  //UBYTE_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)163, InstructionFormat.Load_format,  //SHORT_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)164, InstructionFormat.Load_format,  //USHORT_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)165, InstructionFormat.Load_format,  //REF_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)166, InstructionFormat.Store_format,  //REF_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)167, InstructionFormat.Load_format,  //INT_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)168, InstructionFormat.Load_format,  //LONG_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)169, InstructionFormat.Load_format,  //FLOAT_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)170, InstructionFormat.Load_format,  //DOUBLE_LOAD
                      (load | InstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)171, InstructionFormat.Store_format,  //BYTE_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)172, InstructionFormat.Store_format,  //SHORT_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)173, InstructionFormat.Store_format,  //INT_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)174, InstructionFormat.Store_format,  //LONG_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)175, InstructionFormat.Store_format,  //FLOAT_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)176, InstructionFormat.Store_format,  //DOUBLE_STORE
                      (store | InstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)177, InstructionFormat.Prepare_format,  //PREPARE_INT
                      (load | acquire | InstructionFormat.Prepare_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)178, InstructionFormat.Prepare_format,  //PREPARE_ADDR
                      (load | acquire | InstructionFormat.Prepare_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)179, InstructionFormat.Prepare_format,  //PREPARE_LONG
                      (load | acquire | InstructionFormat.Prepare_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)180, InstructionFormat.Attempt_format,  //ATTEMPT_INT
                      (load | store | compare | release | InstructionFormat.Attempt_traits),
                      1, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)181, InstructionFormat.Attempt_format,  //ATTEMPT_ADDR
                      (load | store | compare | release | InstructionFormat.Attempt_traits),
                      1, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)182, InstructionFormat.Attempt_format,  //ATTEMPT_LONG
                      (load | store | compare | release  | InstructionFormat.Attempt_traits),
                      1, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)183, InstructionFormat.Call_format,  //CALL
                      (call | memAsLoad | memAsStore | dynLink | immedPEI | InstructionFormat.Call_traits),
                      1, 0, 3,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new Operator((char)184, InstructionFormat.Call_format,  //SYSCALL
                      (call | memAsLoad | memAsStore | InstructionFormat.Call_traits),
                      1, 0, 3,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new Operator((char)185, InstructionFormat.Empty_format,  //YIELDPOINT_PROLOGUE
                      (tsp | yieldPoint | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)186, InstructionFormat.Empty_format,  //YIELDPOINT_EPILOGUE
                      (tsp | yieldPoint | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)187, InstructionFormat.Empty_format,  //YIELDPOINT_BACKEDGE
                      (tsp | yieldPoint | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)188, InstructionFormat.OsrPoint_format,  //YIELDPOINT_OSR
                      (tsp | yieldPoint | InstructionFormat.OsrPoint_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)189, InstructionFormat.OsrBarrier_format,  //OSR_BARRIER
                      (none | InstructionFormat.OsrBarrier_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)190, InstructionFormat.Prologue_format,  //IR_PROLOGUE
                      (immedPEI | InstructionFormat.Prologue_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)191, InstructionFormat.CacheOp_format,  //RESOLVE
                      (tsp | dynLink | immedPEI | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)192, InstructionFormat.Unary_format,  //RESOLVE_MEMBER
                      (tsp | dynLink | immedPEI | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)193, InstructionFormat.Nullary_format,  //GET_TIME_BASE
                      (none | InstructionFormat.Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)194, InstructionFormat.InstrumentedCounter_format,  //INSTRUMENTED_EVENT_COUNTER
                      (none | InstructionFormat.InstrumentedCounter_traits),
                      0, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)195, InstructionFormat.TrapIf_format,  //TRAP_IF
                      (immedPEI | InstructionFormat.TrapIf_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)196, InstructionFormat.Trap_format,  //TRAP
                      (immedPEI | InstructionFormat.Trap_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)197, InstructionFormat.Unary_format,  //FLOAT_AS_INT_BITS
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)198, InstructionFormat.Unary_format,  //INT_BITS_AS_FLOAT
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)199, InstructionFormat.Unary_format,  //DOUBLE_AS_LONG_BITS
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)200, InstructionFormat.Unary_format,  //LONG_BITS_AS_DOUBLE
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)201, InstructionFormat.GuardedUnary_format,  //ARRAYLENGTH
                      (none | InstructionFormat.GuardedUnary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)202, InstructionFormat.GuardedUnary_format,  //GET_OBJ_TIB
                      (none | InstructionFormat.GuardedUnary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)203, InstructionFormat.Unary_format,  //GET_CLASS_TIB
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)204, InstructionFormat.Unary_format,  //GET_TYPE_FROM_TIB
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)205, InstructionFormat.Unary_format,  //GET_SUPERCLASS_IDS_FROM_TIB
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)206, InstructionFormat.Unary_format,  //GET_DOES_IMPLEMENT_FROM_TIB
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)207, InstructionFormat.Unary_format,  //GET_ARRAY_ELEMENT_TIB_FROM_TIB
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)208, InstructionFormat.LowTableSwitch_format,  //LOWTABLESWITCH
                      (branch | InstructionFormat.LowTableSwitch_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
  //////////////////////////
  // END   Architecture Independent opcodes.
  // BEGIN Architecture Dependent opcodes & MIR.
  //////////////////////////
     new Operator((char)(0 + Operators.ARCH_INDEPENDENT_END_opcode),  //ADDRESS_CONSTANT
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(1 + Operators.ARCH_INDEPENDENT_END_opcode),  //INT_CONSTANT
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(2 + Operators.ARCH_INDEPENDENT_END_opcode),  //LONG_CONSTANT
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(3 + Operators.ARCH_INDEPENDENT_END_opcode),  //REGISTER
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(4 + Operators.ARCH_INDEPENDENT_END_opcode),  //OTHER_OPERAND
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(5 + Operators.ARCH_INDEPENDENT_END_opcode),  //NULL
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(6 + Operators.ARCH_INDEPENDENT_END_opcode),  //BRANCH_TARGET
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(7 + Operators.ARCH_INDEPENDENT_END_opcode),  //DCBF
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(8 + Operators.ARCH_INDEPENDENT_END_opcode),  //DCBST
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(9 + Operators.ARCH_INDEPENDENT_END_opcode),  //DCBT
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(10 + Operators.ARCH_INDEPENDENT_END_opcode),  //DCBTST
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(11 + Operators.ARCH_INDEPENDENT_END_opcode),  //DCBZ
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(12 + Operators.ARCH_INDEPENDENT_END_opcode),  //DCBZL
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(13 + Operators.ARCH_INDEPENDENT_END_opcode),  //ICBI
                      InstructionFormat.CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(14 + Operators.ARCH_INDEPENDENT_END_opcode),  //CALL_SAVE_VOLATILE
                      InstructionFormat.MIR_Call_format,
                      (call | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(15 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_START
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(16 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_LOWTABLESWITCH
                      InstructionFormat.MIR_LowTableSwitch_format,
                      (branch | InstructionFormat.MIR_LowTableSwitch_traits),
                      0, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(17 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DATA_INT
                      InstructionFormat.MIR_DataInt_format,
                      (none | InstructionFormat.MIR_DataInt_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(18 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DATA_LABEL
                      InstructionFormat.MIR_DataLabel_format,
                      (none | InstructionFormat.MIR_DataLabel_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(19 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 266<<1)),
     new Operator((char)(20 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 266<<1 | 1)),
     new Operator((char)(21 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDC
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 10<<1)),
     new Operator((char)(22 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDE
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskXER,
                      (31<<26 | 138<<1)),
     new Operator((char)(23 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDZE
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskXER,
                      (31<<26 | 202<<1)),
     new Operator((char)(24 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDME
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskXER,
                      (31<<26 | 234<<1)),
     new Operator((char)(25 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDIC
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (12<<26)),
     new Operator((char)(26 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDICr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (13<<26)),
     new Operator((char)(27 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBF
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 40<<1)),
     new Operator((char)(28 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 40<<1 | 1)),
     new Operator((char)(29 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFC
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 8<<1)),
     new Operator((char)(30 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFCr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 8<<1 | 1)),
     new Operator((char)(31 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFIC
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (8<<26)),
     new Operator((char)(32 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFE
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskXER,
                      (31<<26 | 136<<1)),
     new Operator((char)(33 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFZE
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskXER,
                      (31<<26 | 200<<1)),
     new Operator((char)(34 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SUBFME
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskXER,
                      (31<<26 | 232<<1)),
     new Operator((char)(35 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_AND
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 28<<1)),
     new Operator((char)(36 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ANDr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 28<<1 | 1)),
     new Operator((char)(37 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ANDIr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (28<<26)),
     new Operator((char)(38 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ANDISr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (29<<26)),
     new Operator((char)(39 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_NAND
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 476<<1)),
     new Operator((char)(40 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_NANDr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 476<<1 | 1)),
     new Operator((char)(41 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ANDC
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 60<<1)),
     new Operator((char)(42 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ANDCr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 60<<1 | 1)),
     new Operator((char)(43 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_OR
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 444<<1)),
     new Operator((char)(44 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ORr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 444<<1 | 1)),
     new Operator((char)(45 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MOVE
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (24<<26)),
     new Operator((char)(46 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ORI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (24<<26)),
     new Operator((char)(47 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ORIS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (25<<26)),
     new Operator((char)(48 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_NOR
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 124<<1)),
     new Operator((char)(49 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_NORr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 124<<1 | 1)),
     new Operator((char)(50 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ORC
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 412<<1)),
     new Operator((char)(51 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ORCr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 412<<1 | 1)),
     new Operator((char)(52 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_XOR
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 316<<1)),
     new Operator((char)(53 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_XORr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 316<<1 | 1)),
     new Operator((char)(54 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_XORI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (26<<26)),
     new Operator((char)(55 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_XORIS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (27<<26)),
     new Operator((char)(56 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_EQV
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 284<<1)),
     new Operator((char)(57 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_EQVr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 284<<1 | 1)),
     new Operator((char)(58 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_NEG
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 104<<1)),
     new Operator((char)(59 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_NEGr
                      InstructionFormat.MIR_Unary_format,
                      (compare | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 104<<1 | 1)),
     new Operator((char)(60 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CNTLZW
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 26<<1)),
     new Operator((char)(61 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_EXTSB
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 954<<1)),
     new Operator((char)(62 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_EXTSBr
                      InstructionFormat.MIR_Unary_format,
                      (compare | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 954<<1 | 1)),
     new Operator((char)(63 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_EXTSH
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 922<<1)),
     new Operator((char)(64 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_EXTSHr
                      InstructionFormat.MIR_Unary_format,
                      (compare | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 922<<1 | 1)),
     new Operator((char)(65 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SLW
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 24<<1)),
     new Operator((char)(66 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SLWr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 24<<1 | 1)),
     new Operator((char)(67 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SLWI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (21<<26)),
     new Operator((char)(68 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SLWIr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (21<<26 | 1)),
     new Operator((char)(69 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRW
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 536<<1)),
     new Operator((char)(70 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRWr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 536<<1 | 1)),
     new Operator((char)(71 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRWI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (21<<26 | 31<<1)),
     new Operator((char)(72 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRWIr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (21<<26 | 31<<1 | 1)),
     new Operator((char)(73 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRAW
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 792<<1)),
     new Operator((char)(74 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRAWr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 792<<1 | 1)),
     new Operator((char)(75 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRAWI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 824<<1)),
     new Operator((char)(76 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRAWIr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 824<<1 | 1)),
     new Operator((char)(77 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_RLWINM
                      InstructionFormat.MIR_RotateAndMask_format,
                      (none | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (21<<26)),
     new Operator((char)(78 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_RLWINMr
                      InstructionFormat.MIR_RotateAndMask_format,
                      (compare | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (21<<26 | 1)),
     new Operator((char)(79 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_RLWIMI
                      InstructionFormat.MIR_RotateAndMask_format,
                      (none | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (20<<26)),
     new Operator((char)(80 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_RLWIMIr
                      InstructionFormat.MIR_RotateAndMask_format,
                      (compare | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (20<<26 | 1)),
     new Operator((char)(81 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_RLWNM
                      InstructionFormat.MIR_RotateAndMask_format,
                      (none | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (23<<26)),
     new Operator((char)(82 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_RLWNMr
                      InstructionFormat.MIR_RotateAndMask_format,
                      (compare | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (23<<26)),
     new Operator((char)(83 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_B
                      InstructionFormat.MIR_Branch_format,
                      (branch | InstructionFormat.MIR_Branch_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (18<<26)),
     new Operator((char)(84 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BL
                      InstructionFormat.MIR_Call_format,
                      (call | dynLink | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC,
                      (18<<26 | 1)),
     new Operator((char)(85 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BL_SYS
                      InstructionFormat.MIR_Call_format,
                      (call | dynLink | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC,
                      (18<<26 | 1)),
     new Operator((char)(86 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BLR
                      InstructionFormat.MIR_Return_format,
                      (ret | InstructionFormat.MIR_Return_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskLR,
                      (19<<26 | 0x14<<21 | 16<<1)),
     new Operator((char)(87 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCTR
                      InstructionFormat.MIR_Branch_format,
                      (branch | indirect | InstructionFormat.MIR_Branch_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskCTR,
                      (19<<26 | 0x14<<21 | 528<<1)),
     new Operator((char)(88 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCTRL
                      InstructionFormat.MIR_Call_format,
                      (call | indirect | dynLink | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC_CTR,
                      (19<<26 | 0x14<<21 | 528<<1 | 1)),
     new Operator((char)(89 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCTRL_SYS
                      InstructionFormat.MIR_Call_format,
                      (call | indirect | dynLink | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC_CTR,
                      (19<<26 | 0x14<<21 | 528<<1 | 1)),
     new Operator((char)(90 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCLR
                      InstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | indirect | InstructionFormat.MIR_CondBranch_traits),
                      0, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskLR,
                      (19<<26 | 16<<1)),
     new Operator((char)(91 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BLRL
                      InstructionFormat.MIR_Call_format,
                      (call | indirect | dynLink | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC_LR,
                      (19<<26 | 0x14<<21 | 16<<1 | 1)),
     new Operator((char)(92 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCLRL
                      InstructionFormat.MIR_CondCall_format,
                      (call | conditional | indirect | dynLink | immedPEI | InstructionFormat.MIR_CondCall_traits),
                      2, 0, 3,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC_LR,
                      (19<<26 | 16<<1 | 1)),
     new Operator((char)(93 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BC
                      InstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | InstructionFormat.MIR_CondBranch_traits),
                      0, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (16<<26)),
     new Operator((char)(94 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCL
                      InstructionFormat.MIR_CondCall_format,
                      (call | conditional | dynLink | immedPEI | InstructionFormat.MIR_CondCall_traits),
                      2, 0, 3,
                      PhysicalDefUse.maskLR,
                      PhysicalDefUse.maskJTOC,
                      (16<<26 | 1)),
     new Operator((char)(95 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCOND
                      InstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | InstructionFormat.MIR_CondBranch_traits),
                      0, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (16<<26)),
     new Operator((char)(96 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCOND2
                      InstructionFormat.MIR_CondBranch2_format,
                      (branch | conditional | InstructionFormat.MIR_CondBranch2_traits),
                      0, 0, 7,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     new Operator((char)(97 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCCTR
                      InstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | indirect | InstructionFormat.MIR_CondBranch_traits),
                      0, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskCTR,
                      (19<<26) | (528<<1)),
     new Operator((char)(98 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_BCC
                      InstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | InstructionFormat.MIR_CondBranch_traits),
                      0, 0, 4,
                      PhysicalDefUse.maskCTR,
                      PhysicalDefUse.mask,
                      (16<<26)),
     new Operator((char)(99 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (14<<26)),
     new Operator((char)(100 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ADDIS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (15<<26)),
     new Operator((char)(101 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LDI
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (14<<26)),
     new Operator((char)(102 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LDIS
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (15<<26)),
     new Operator((char)(103 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CMP
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 0<<1)),
     new Operator((char)(104 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CMPI
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (11<<26)),
     new Operator((char)(105 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CMPL
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 32<<1)),
     new Operator((char)(106 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CMPLI
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (10<<26)),
     new Operator((char)(107 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CRAND
                      InstructionFormat.MIR_Condition_format,
                      (none | InstructionFormat.MIR_Condition_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (19<<26 | 257<<1)),
     new Operator((char)(108 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CRANDC
                      InstructionFormat.MIR_Condition_format,
                      (none | InstructionFormat.MIR_Condition_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (19<<26 | 129<<1)),
     new Operator((char)(109 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CROR
                      InstructionFormat.MIR_Condition_format,
                      (none | InstructionFormat.MIR_Condition_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (19<<26 | 449<<1)),
     new Operator((char)(110 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CRORC
                      InstructionFormat.MIR_Condition_format,
                      (none | InstructionFormat.MIR_Condition_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (19<<26 | 417<<1)),
     new Operator((char)(111 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMR
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 72<<1)),
     new Operator((char)(112 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FRSP
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 12<<1)),
     new Operator((char)(113 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FCTIW
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 14<<1)),
     new Operator((char)(114 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FCTIWZ
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 15<<1)),
     new Operator((char)(115 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FADD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 21<<1)),
     new Operator((char)(116 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FADDS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 21<<1)),
     new Operator((char)(117 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FSQRT
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 22<<1)),
     new Operator((char)(118 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FSQRTS
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 22<<1)),
     new Operator((char)(119 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FABS
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 264<<1)),
     new Operator((char)(120 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FCMPO
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 32<<1)),
     new Operator((char)(121 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FCMPU
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26)),
     new Operator((char)(122 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FDIV
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 18<<1)),
     new Operator((char)(123 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FDIVS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 18<<1)),
     new Operator((char)(124 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DIVW
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 491<<1)),
     new Operator((char)(125 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DIVWU
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 459<<1)),
     new Operator((char)(126 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMUL
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 25<<1)),
     new Operator((char)(127 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMULS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 25<<1)),
     new Operator((char)(128 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FSEL
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 23<<1)),
     new Operator((char)(129 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMADD
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 29<<1)),
     new Operator((char)(130 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMADDS
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 29<<1)),
     new Operator((char)(131 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMSUB
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 28<<1)),
     new Operator((char)(132 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FMSUBS
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 28<<1)),
     new Operator((char)(133 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FNMADD
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 31<<1)),
     new Operator((char)(134 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FNMADDS
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 31<<1)),
     new Operator((char)(135 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FNMSUB
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 30<<1)),
     new Operator((char)(136 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FNMSUBS
                      InstructionFormat.MIR_Ternary_format,
                      (none | InstructionFormat.MIR_Ternary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 30<<1)),
     new Operator((char)(137 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MULLI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (7<<26)),
     new Operator((char)(138 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MULLW
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 235<<1)),
     new Operator((char)(139 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MULHW
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 75<<1)),
     new Operator((char)(140 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MULHWU
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 11<<1)),
     new Operator((char)(141 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FNEG
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 40<<1)),
     new Operator((char)(142 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FSUB
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 20<<1)),
     new Operator((char)(143 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_FSUBS
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (59<<26 | 20<<1)),
     new Operator((char)(144 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LWZ
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (32<<26)),
     new Operator((char)(145 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LWZU
                      InstructionFormat.MIR_LoadUpdate_format,
                      (load | InstructionFormat.MIR_LoadUpdate_traits),
                      1, 1, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (33<<26)),
     new Operator((char)(146 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LWZUX
                      InstructionFormat.MIR_LoadUpdate_format,
                      (load | InstructionFormat.MIR_LoadUpdate_traits),
                      1, 1, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 55<<1)),
     new Operator((char)(147 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LWZX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 23<<1)),
     new Operator((char)(148 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LWARX
                      InstructionFormat.MIR_Load_format,
                      (memAsLoad | memAsStore | load | acquire | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 20<<1)),
     new Operator((char)(149 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LBZ
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (34<<26)),
     new Operator((char)(150 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LBZUX
                      InstructionFormat.MIR_LoadUpdate_format,
                      (load | InstructionFormat.MIR_LoadUpdate_traits),
                      1, 1, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 119<<1)),
     new Operator((char)(151 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LBZX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 87<<1)),
     new Operator((char)(152 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LHA
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (42<<26)),
     new Operator((char)(153 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LHAX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 343<<1)),
     new Operator((char)(154 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LHZ
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (40<<26)),
     new Operator((char)(155 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LHZX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 279<<1)),
     new Operator((char)(156 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LFD
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (50<<26)),
     new Operator((char)(157 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LFDX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 599<<1)),
     new Operator((char)(158 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LFS
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (48<<26)),
     new Operator((char)(159 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LFSX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 535<<1)),
     new Operator((char)(160 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LMW
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (46<<26)),
     new Operator((char)(161 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STW
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (36<<26)),
     new Operator((char)(162 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STWX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 151<<1)),
     new Operator((char)(163 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STWCXr
                      InstructionFormat.MIR_Store_format,
                      (memAsLoad | memAsStore | store | compare | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 150<<1 | 1)),
     new Operator((char)(164 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STWU
                      InstructionFormat.MIR_StoreUpdate_format,
                      (store | InstructionFormat.MIR_StoreUpdate_traits),
                      0, 1, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (37<<26)),
     new Operator((char)(165 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STB
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (38<<26)),
     new Operator((char)(166 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STBX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 215<<1)),
     new Operator((char)(167 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STH
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (44<<26)),
     new Operator((char)(168 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STHX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 407<<1)),
     new Operator((char)(169 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STFD
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (54<<26)),
     new Operator((char)(170 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STFDX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 727<<1)),
     new Operator((char)(171 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STFDU
                      InstructionFormat.MIR_StoreUpdate_format,
                      (store | InstructionFormat.MIR_StoreUpdate_traits),
                      0, 1, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 759<<1)),
     new Operator((char)(172 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STFS
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (52<<26)),
     new Operator((char)(173 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STFSX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 663<<1)),
     new Operator((char)(174 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STFSU
                      InstructionFormat.MIR_StoreUpdate_format,
                      (store | InstructionFormat.MIR_StoreUpdate_traits),
                      0, 1, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (53<<26)),
     new Operator((char)(175 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STMW
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (47<<26)),
     new Operator((char)(176 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_TW
                      InstructionFormat.MIR_Trap_format,
                      (immedPEI | dynLink | InstructionFormat.MIR_Trap_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 4<<1)),
     new Operator((char)(177 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_TWI
                      InstructionFormat.MIR_Trap_format,
                      (immedPEI | dynLink | InstructionFormat.MIR_Trap_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (3<<26)),
     new Operator((char)(178 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MFSPR
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 339<<1)),
     new Operator((char)(179 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MTSPR
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 467<<1)),
     new Operator((char)(180 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MFTB
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 392<<11 | 371<<1)),
     new Operator((char)(181 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_MFTBU
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 424<<11 | 371<<1)),
     new Operator((char)(182 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SYNC
                      InstructionFormat.MIR_Empty_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 1<<21 | 598<<1)),
     new Operator((char)(183 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ISYNC
                      InstructionFormat.MIR_Empty_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (19<<26 | 150<<1)),
     new Operator((char)(184 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DCBF
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 86<<1)),
     new Operator((char)(185 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DCBST
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 54<<1)),
     new Operator((char)(186 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DCBT
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 278<<1)),
     new Operator((char)(187 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DCBTST
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 246<<1)),
     new Operator((char)(188 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DCBZ
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 1014<<1)),
     new Operator((char)(189 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_DCBZL
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 1<<21 | 1014<<1)),
     new Operator((char)(190 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_ICBI
                      InstructionFormat.MIR_CacheOp_format,
                      (memAsLoad | memAsStore | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 982<<1)),
     new Operator((char)(191 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_EXTSW
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 986<<1)),
     new Operator((char)(192 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_EXTSWr
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 986<<1 | 1)),
     new Operator((char)(193 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_EXTZW
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26 | 0<<2)),
     new Operator((char)(194 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_RLDICL
                      InstructionFormat.MIR_RotateAndMask_format,
                      (none | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26)),
     new Operator((char)(195 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_RLDICR
                      InstructionFormat.MIR_RotateAndMask_format,
                      (none | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26 | 1<<2)),
     new Operator((char)(196 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SLD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 27<<1)),
     new Operator((char)(197 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SLDr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (31<<26 | 27<<1 | 1)),
     new Operator((char)(198 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SLDI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26 | 1<<2)),
     new Operator((char)(199 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 539<<1)),
     new Operator((char)(200 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRDr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 539<<1 | 1)),
     new Operator((char)(201 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRAD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 794<<1)),
     new Operator((char)(202 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRADr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 794<<1 | 1)),
     new Operator((char)(203 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRADI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 413<<2)),
     new Operator((char)(204 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRADIr
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskC0_XER,
                      PhysicalDefUse.mask,
                      (31<<26 | 413<<2 | 1)),
     new Operator((char)(205 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_SRDI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26 | 0<<2)),
     new Operator((char)(206 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_RLDIMI
                      InstructionFormat.MIR_RotateAndMask_format,
                      (none | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26 | 3<<2)),
     new Operator((char)(207 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_RLDIMIr
                      InstructionFormat.MIR_RotateAndMask_format,
                      (compare | InstructionFormat.MIR_RotateAndMask_traits),
                      1, 0, 5,
                      PhysicalDefUse.maskC0,
                      PhysicalDefUse.mask,
                      (30<<26 | 3<<2 | 1)),
     new Operator((char)(208 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_CMP
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 0<<1 | 1<<21)),
     new Operator((char)(209 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_CMPI
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (11<<26 | 1<<21)),
     new Operator((char)(210 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_CMPL
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 32<<1 | 1<<21)),
     new Operator((char)(211 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_CMPLI
                      InstructionFormat.MIR_Binary_format,
                      (compare | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (10<<26 | 1<<21)),
     new Operator((char)(212 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_FCFID
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      0),
     new Operator((char)(213 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_FCTIDZ
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (63<<26 | 815<<1)),
     new Operator((char)(214 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_DIVD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 489<<1)),
     new Operator((char)(215 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_MULLD
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 233<<1)),
     new Operator((char)(216 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_LD
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (58<<26)),
     new Operator((char)(217 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_LDX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 21<<1)),
     new Operator((char)(218 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_STD
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (62<<26)),
     new Operator((char)(219 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_STDX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 149<<1)),
     new Operator((char)(220 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_TD
                      InstructionFormat.MIR_Trap_format,
                      (immedPEI | dynLink | InstructionFormat.MIR_Trap_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 68<<1)),
     new Operator((char)(221 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_TDI
                      InstructionFormat.MIR_Trap_format,
                      (immedPEI | dynLink | InstructionFormat.MIR_Trap_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (2<<26)),
     new Operator((char)(222 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_CNTLZAddr
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 58<<1)),
     new Operator((char)(223 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRAAddrI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskXER,
                      PhysicalDefUse.mask,
                      (31<<26 | 413<<2)),
     new Operator((char)(224 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_SRAddrI
                      InstructionFormat.MIR_Binary_format,
                      (none | InstructionFormat.MIR_Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (30<<26 | 0<<2)),
     new Operator((char)(225 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_LWA
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (58<<26 | 2)),
     new Operator((char)(226 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LInt
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (58<<26 | 2)),
     new Operator((char)(227 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC64_LWAX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 341<<1)),
     new Operator((char)(228 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LIntX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 341<<1)),
     new Operator((char)(229 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LIntUX
                      InstructionFormat.MIR_LoadUpdate_format,
                      (load | InstructionFormat.MIR_LoadUpdate_traits),
                      1, 1, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 373<<1)),
     new Operator((char)(230 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LAddr
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (58<<26)),
     new Operator((char)(231 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LAddrX
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 21<<1)),
     new Operator((char)(232 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LAddrU
                      InstructionFormat.MIR_Load_format,
                      (load | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (58<<26 | 1)),
     new Operator((char)(233 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LAddrUX
                      InstructionFormat.MIR_LoadUpdate_format,
                      (load | InstructionFormat.MIR_LoadUpdate_traits),
                      1, 1, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 53<<1)),
     new Operator((char)(234 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_LAddrARX
                      InstructionFormat.MIR_Load_format,
                      (memAsLoad | memAsStore | load | acquire | InstructionFormat.MIR_Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 84<<1)),
     new Operator((char)(235 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STAddr
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (62<<26)),
     new Operator((char)(236 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STAddrX
                      InstructionFormat.MIR_Store_format,
                      (store | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 149<<1)),
     new Operator((char)(237 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STAddrU
                      InstructionFormat.MIR_StoreUpdate_format,
                      (store | InstructionFormat.MIR_StoreUpdate_traits),
                      0, 1, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (62<<26 | 1)),
     new Operator((char)(238 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STAddrUX
                      InstructionFormat.MIR_StoreUpdate_format,
                      (store | InstructionFormat.MIR_StoreUpdate_traits),
                      0, 1, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 181<<1)),
     new Operator((char)(239 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_STAddrCXr
                      InstructionFormat.MIR_Store_format,
                      (memAsLoad | memAsStore | store | compare | InstructionFormat.MIR_Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 214<<1 | 1)),
     new Operator((char)(240 + Operators.ARCH_INDEPENDENT_END_opcode),  //PPC_TAddr
                      InstructionFormat.MIR_Trap_format,
                      (immedPEI | dynLink | InstructionFormat.MIR_Trap_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      (31<<26 | 68<<1)),
     new Operator((char)(241 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_END
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask,
                      -1),
     null };

  // For HIR/LIR
  private Operator(char opcode, byte format, int traits,
                       int numDefs, int numDefUses, int numUses,
                       int iDefs, int iUses) {
    this.opcode       = opcode;
    this.format       = format;
    this.traits       = traits;
    this.numberDefs   = numDefs;
    this.numberDefUses= numDefUses;
    this.numberUses   = numUses;
    this.implicitDefs = iDefs;
    this.implicitUses = iUses;
  }

  // For MIR
  private Operator(char opcode, byte format, int traits,
                       int numDefs, int numDefUses, int numUses,
                       int iDefs, int iUses,
                       int iTemp) {
    this.opcode       = opcode;
    this.format       = format;
    this.traits       = traits;
    this.instTemplate = iTemp;
    this.numberDefs   = numDefs;
    this.numberDefUses= numDefUses;
    this.numberUses   = numUses;
    this.implicitDefs = iDefs;
    this.implicitUses = iUses;
  }
}
