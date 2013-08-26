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
 * OperatorList.dat and ia32OperatorList.dat
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
                      PhysicalDefUse.mask),
     new Operator((char)(1 + Operators.ARCH_INDEPENDENT_END_opcode),  //INT_CONSTANT
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(2 + Operators.ARCH_INDEPENDENT_END_opcode),  //LONG_CONSTANT
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(3 + Operators.ARCH_INDEPENDENT_END_opcode),  //REGISTER
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(4 + Operators.ARCH_INDEPENDENT_END_opcode),  //OTHER_OPERAND
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(5 + Operators.ARCH_INDEPENDENT_END_opcode),  //NULL
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(6 + Operators.ARCH_INDEPENDENT_END_opcode),  //BRANCH_TARGET
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(7 + Operators.ARCH_INDEPENDENT_END_opcode),  //MATERIALIZE_FP_CONSTANT
                      InstructionFormat.Binary_format,
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(8 + Operators.ARCH_INDEPENDENT_END_opcode),  //GET_CURRENT_PROCESSOR
                      InstructionFormat.Nullary_format,
                      (none | InstructionFormat.Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(9 + Operators.ARCH_INDEPENDENT_END_opcode),  //ROUND_TO_ZERO
                      InstructionFormat.Empty_format,
                      (none | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(10 + Operators.ARCH_INDEPENDENT_END_opcode),  //CLEAR_FLOATING_POINT_STATE
                      InstructionFormat.Empty_format,
                      (none | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(11 + Operators.ARCH_INDEPENDENT_END_opcode),  //PREFETCH
                      InstructionFormat.CacheOp_format,
                      (none | InstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(12 + Operators.ARCH_INDEPENDENT_END_opcode),  //PAUSE
                      InstructionFormat.Empty_format,
                      (none | InstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(13 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_ADD
                      InstructionFormat.Binary_format,
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(14 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_SUB
                      InstructionFormat.Binary_format,
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(15 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_MUL
                      InstructionFormat.Binary_format,
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(16 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_DIV
                      InstructionFormat.Binary_format,
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(17 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_NEG
                      InstructionFormat.Unary_format,
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(18 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_REM
                      InstructionFormat.Binary_format,
                      (none | InstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(19 + Operators.ARCH_INDEPENDENT_END_opcode),  //INT_2FP
                      InstructionFormat.Unary_format,
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(20 + Operators.ARCH_INDEPENDENT_END_opcode),  //LONG_2FP
                      InstructionFormat.Unary_format,
                      (none | InstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(21 + Operators.ARCH_INDEPENDENT_END_opcode),  //CMP_CMOV
                      InstructionFormat.CondMove_format,
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(22 + Operators.ARCH_INDEPENDENT_END_opcode),  //FCMP_CMOV
                      InstructionFormat.CondMove_format,
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(23 + Operators.ARCH_INDEPENDENT_END_opcode),  //LCMP_CMOV
                      InstructionFormat.CondMove_format,
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(24 + Operators.ARCH_INDEPENDENT_END_opcode),  //CMP_FCMOV
                      InstructionFormat.CondMove_format,
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(25 + Operators.ARCH_INDEPENDENT_END_opcode),  //FCMP_FCMOV
                      InstructionFormat.CondMove_format,
                      (compare | InstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(26 + Operators.ARCH_INDEPENDENT_END_opcode),  //CALL_SAVE_VOLATILE
                      InstructionFormat.MIR_Call_format,
                      (call | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new Operator((char)(27 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_START
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(28 + Operators.ARCH_INDEPENDENT_END_opcode),  //REQUIRE_ESP
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(29 + Operators.ARCH_INDEPENDENT_END_opcode),  //ADVISE_ESP
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(30 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_LOWTABLESWITCH
                      InstructionFormat.MIR_LowTableSwitch_format,
                      (branch | InstructionFormat.MIR_LowTableSwitch_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(31 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_METHODSTART
                      InstructionFormat.MIR_Nullary_format,
                      (none | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(32 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCLEAR
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(33 + Operators.ARCH_INDEPENDENT_END_opcode),  //DUMMY_DEF
                      InstructionFormat.MIR_Nullary_format,
                      (none | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(34 + Operators.ARCH_INDEPENDENT_END_opcode),  //DUMMY_USE
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(35 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMOV_ENDING_LIVE_RANGE
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(36 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMOV
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(37 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_TRAPIF
                      InstructionFormat.MIR_TrapIf_format,
                      (immedPEI | InstructionFormat.MIR_TrapIf_traits),
                      1, 0, 4,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(38 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_OFFSET
                      InstructionFormat.MIR_CaseLabel_format,
                      (none | InstructionFormat.MIR_CaseLabel_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(39 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LOCK_CMPXCHG
                      InstructionFormat.MIR_CompareExchange_format,
                      (compare | InstructionFormat.MIR_CompareExchange_traits),
                      0, 2, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(40 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LOCK_CMPXCHG8B
                      InstructionFormat.MIR_CompareExchange8B_format,
                      (compare | InstructionFormat.MIR_CompareExchange8B_traits),
                      0, 3, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(41 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADC
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.maskCF),
     new Operator((char)(42 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(43 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_AND
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(44 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BSWAP
                      InstructionFormat.MIR_UnaryAcc_format,
                      (none | InstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(45 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BT
                      InstructionFormat.MIR_Test_format,
                      (none | InstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new Operator((char)(46 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BTC
                      InstructionFormat.MIR_Test_format,
                      (none | InstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new Operator((char)(47 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BTR
                      InstructionFormat.MIR_Test_format,
                      (none | InstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new Operator((char)(48 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BTS
                      InstructionFormat.MIR_Test_format,
                      (none | InstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new Operator((char)(49 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SYSCALL
                      InstructionFormat.MIR_Call_format,
                      (call | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new Operator((char)(50 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CALL
                      InstructionFormat.MIR_Call_format,
                      (call | immedPEI | InstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new Operator((char)(51 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CDQ
                      InstructionFormat.MIR_ConvertDW2QW_format,
                      (none | InstructionFormat.MIR_ConvertDW2QW_traits),
                      1, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(52 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CDO
                      InstructionFormat.MIR_ConvertDW2QW_format,
                      (none | InstructionFormat.MIR_ConvertDW2QW_traits),
                      1, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(53 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CDQE
                      InstructionFormat.MIR_ConvertDW2QW_format,
                      (none | InstructionFormat.MIR_ConvertDW2QW_traits),
                      1, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(54 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMOV
                      InstructionFormat.MIR_CondMove_format,
                      (none | InstructionFormat.MIR_CondMove_traits),
                      0, 1, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskCF_OF_PF_SF_ZF),
     new Operator((char)(55 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMP
                      InstructionFormat.MIR_Compare_format,
                      (compare | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(56 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPXCHG
                      InstructionFormat.MIR_CompareExchange_format,
                      (compare | InstructionFormat.MIR_CompareExchange_traits),
                      0, 2, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(57 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPXCHG8B
                      InstructionFormat.MIR_CompareExchange8B_format,
                      (compare | InstructionFormat.MIR_CompareExchange8B_traits),
                      0, 3, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(58 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DEC
                      InstructionFormat.MIR_UnaryAcc_format,
                      (none | InstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskAF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(59 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DIV
                      InstructionFormat.MIR_Divide_format,
                      (none | InstructionFormat.MIR_Divide_traits),
                      0, 2, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(60 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FADD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(61 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FADDP
                      InstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(62 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCHS
                      InstructionFormat.MIR_UnaryAcc_format,
                      (none | InstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(63 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCMOV
                      InstructionFormat.MIR_CondMove_format,
                      (none | InstructionFormat.MIR_CondMove_traits),
                      0, 1, 2,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.maskCF_PF_ZF),
     new Operator((char)(64 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCOMI
                      InstructionFormat.MIR_Compare_format,
                      (compare | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(65 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCOMIP
                      InstructionFormat.MIR_Compare_format,
                      (compare | fpPop | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(66 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIV
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(67 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIVP
                      InstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(68 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIVR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(69 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIVRP
                      InstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(70 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FEXAM
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(71 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FXCH
                      InstructionFormat.MIR_XChng_format,
                      (none | InstructionFormat.MIR_XChng_traits),
                      0, 2, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(72 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FFREE
                      InstructionFormat.MIR_Nullary_format,
                      (none | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(73 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIADD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(74 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIDIV
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(75 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIDIVR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(76 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FILD
                      InstructionFormat.MIR_Move_format,
                      (fpPush | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(77 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIMUL
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(78 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FINIT
                      InstructionFormat.MIR_Empty_format,
                      (none | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(79 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIST
                      InstructionFormat.MIR_Move_format,
                      (none | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(80 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FISTP
                      InstructionFormat.MIR_Move_format,
                      (fpPop | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(81 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FISUB
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(82 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FISUBR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(83 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLD
                      InstructionFormat.MIR_Move_format,
                      (fpPush | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(84 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDCW
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(85 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLD1
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(86 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDL2T
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(87 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDL2E
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(88 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDPI
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(89 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDLG2
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(90 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDLN2
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(91 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDZ
                      InstructionFormat.MIR_Nullary_format,
                      (fpPush | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(92 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMUL
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(93 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMULP
                      InstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(94 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNSTCW
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(95 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNINIT
                      InstructionFormat.MIR_Empty_format,
                      (none | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(96 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNSAVE
                      InstructionFormat.MIR_FSave_format,
                      (none | InstructionFormat.MIR_FSave_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(97 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FPREM
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(98 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FRSTOR
                      InstructionFormat.MIR_FSave_format,
                      (none | InstructionFormat.MIR_FSave_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(99 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FST
                      InstructionFormat.MIR_Move_format,
                      (none | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(100 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSTCW
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(101 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSTP
                      InstructionFormat.MIR_Move_format,
                      (fpPop | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(102 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUB
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(103 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUBP
                      InstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(104 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUBR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(105 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUBRP
                      InstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(106 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FUCOMI
                      InstructionFormat.MIR_Compare_format,
                      (compare | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(107 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FUCOMIP
                      InstructionFormat.MIR_Compare_format,
                      (compare | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(108 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_IDIV
                      InstructionFormat.MIR_Divide_format,
                      (none | InstructionFormat.MIR_Divide_traits),
                      0, 2, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(109 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_IMUL1
                      InstructionFormat.MIR_Multiply_format,
                      (none | InstructionFormat.MIR_Multiply_traits),
                      1, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(110 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_IMUL2
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(111 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_INC
                      InstructionFormat.MIR_UnaryAcc_format,
                      (none | InstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskAF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(112 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_INT
                      InstructionFormat.MIR_Trap_format,
                      (immedPEI | InstructionFormat.MIR_Trap_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(113 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_JCC
                      InstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | InstructionFormat.MIR_CondBranch_traits),
                      0, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF),
     new Operator((char)(114 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_JCC2
                      InstructionFormat.MIR_CondBranch2_format,
                      (branch | conditional | InstructionFormat.MIR_CondBranch2_traits),
                      0, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF),
     new Operator((char)(115 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_JMP
                      InstructionFormat.MIR_Branch_format,
                      (branch | InstructionFormat.MIR_Branch_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(116 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LEA
                      InstructionFormat.MIR_Lea_format,
                      (none | InstructionFormat.MIR_Lea_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(117 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LOCK
                      InstructionFormat.MIR_Empty_format,
                      (none | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(118 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOV
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(119 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZX__B
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(120 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSX__B
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(121 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZX__W
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(122 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSX__W
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(123 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZXQ__B
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(124 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSXQ__B
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(125 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZXQ__W
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(126 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSXQ__W
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(127 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MUL
                      InstructionFormat.MIR_Multiply_format,
                      (none | InstructionFormat.MIR_Multiply_traits),
                      1, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(128 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_NEG
                      InstructionFormat.MIR_UnaryAcc_format,
                      (none | InstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(129 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_NOT
                      InstructionFormat.MIR_UnaryAcc_format,
                      (none | InstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(130 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_OR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(131 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MFENCE
                      InstructionFormat.MIR_Empty_format,
                      (none | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(132 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PAUSE
                      InstructionFormat.MIR_Empty_format,
                      (none | InstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(133 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PREFETCHNTA
                      InstructionFormat.MIR_CacheOp_format,
                      (none | InstructionFormat.MIR_CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(134 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_POP
                      InstructionFormat.MIR_Nullary_format,
                      (none | InstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskESP,
                      PhysicalDefUse.maskESP),
     new Operator((char)(135 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PUSH
                      InstructionFormat.MIR_UnaryNoRes_format,
                      (none | InstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskESP,
                      PhysicalDefUse.maskESP),
     new Operator((char)(136 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RCL
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.maskCF),
     new Operator((char)(137 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RCR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.maskCF),
     new Operator((char)(138 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ROL
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.mask),
     new Operator((char)(139 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ROR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.mask),
     new Operator((char)(140 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RET
                      InstructionFormat.MIR_Return_format,
                      (ret | InstructionFormat.MIR_Return_traits),
                      0, 0, 3,
                      PhysicalDefUse.maskESP,
                      PhysicalDefUse.maskESP),
     new Operator((char)(141 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SAL
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(142 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SAR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(143 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHL
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(144 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(145 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SBB
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.maskCF),
     new Operator((char)(146 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SET__B
                      InstructionFormat.MIR_Set_format,
                      (none | InstructionFormat.MIR_Set_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF),
     new Operator((char)(147 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHLD
                      InstructionFormat.MIR_DoubleShift_format,
                      (none | InstructionFormat.MIR_DoubleShift_traits),
                      0, 1, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(148 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHRD
                      InstructionFormat.MIR_DoubleShift_format,
                      (none | InstructionFormat.MIR_DoubleShift_traits),
                      0, 1, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(149 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SUB
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(150 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_TEST
                      InstructionFormat.MIR_Test_format,
                      (none | InstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(151 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_XOR
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(152 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RDTSC
                      InstructionFormat.MIR_RDTSC_format,
                      (none | InstructionFormat.MIR_RDTSC_traits),
                      2, 0, 0,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.mask),
     new Operator((char)(153 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADDSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(154 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SUBSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(155 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MULSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(156 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DIVSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(157 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADDSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(158 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SUBSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(159 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MULSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(160 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DIVSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(161 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDPS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(162 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDPD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(163 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDNPS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(164 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDNPD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(165 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ORPS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(166 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ORPD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(167 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_XORPS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(168 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_XORPD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(169 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_UCOMISS
                      InstructionFormat.MIR_Compare_format,
                      (compare | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(170 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_UCOMISD
                      InstructionFormat.MIR_Compare_format,
                      (compare | InstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new Operator((char)(171 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPEQSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(172 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLTSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(173 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLESS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(174 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPUNORDSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(175 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNESS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(176 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLTSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(177 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLESS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(178 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPORDSS
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(179 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPEQSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(180 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLTSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(181 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLESD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(182 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPUNORDSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(183 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNESD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(184 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLTSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(185 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLESD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(186 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPORDSD
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(187 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVLPD
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(188 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVLPS
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(189 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSS
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(190 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSD
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(191 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVD
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(192 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVQ
                      InstructionFormat.MIR_Move_format,
                      (move | InstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(193 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PSLLQ
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(194 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PSRLQ
                      InstructionFormat.MIR_BinaryAcc_format,
                      (none | InstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(195 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SQRTSS
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(196 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SQRTSD
                      InstructionFormat.MIR_Unary_format,
                      (none | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new Operator((char)(197 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSI2SS
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(198 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSS2SD
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(199 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSS2SI
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(200 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTTSS2SI
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(201 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSI2SD
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(202 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSD2SS
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(203 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSD2SI
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(204 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTTSD2SI
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(205 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSI2SDQ
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(206 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSD2SIQ
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(207 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTTSD2SIQ
                      InstructionFormat.MIR_Unary_format,
                      (move | InstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new Operator((char)(208 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_END
                      InstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
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

}
