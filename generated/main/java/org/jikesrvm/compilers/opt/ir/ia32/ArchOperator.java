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
 * NOTE: ArchOperator.java is mechanically generated from
 * ArchOperator.template using the operator definitions in
 * OperatorList.dat and ia32OperatorList.dat
 *
 * DO NOT MANUALLY EDIT THE JAVA FILE.
 */

package org.jikesrvm.compilers.opt.ir.ia32;

import org.jikesrvm.*;
import org.jikesrvm.compilers.opt.ir.InstructionFormat;
import org.jikesrvm.compilers.opt.ir.Operator;
import org.jikesrvm.compilers.opt.ir.Operators;
import org.jikesrvm.compilers.opt.*;
import org.jikesrvm.util.Bits;
import org.vmmagic.pragma.Pure;

/**
 * The implementation of operator that is specific to a particular
 * architecture.
 */
public final class ArchOperator extends Operator {

  /** Array holding all singleton operators */
  private static final Operator[] OperatorArray = {
     new ArchOperator((char)0, ArchInstructionFormat.Nullary_format,  //GET_CAUGHT_EXCEPTION
                      (none | ArchInstructionFormat.Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)1, ArchInstructionFormat.CacheOp_format,  //SET_CAUGHT_EXCEPTION
                      (none | ArchInstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)2, ArchInstructionFormat.New_format,  //NEW
                      (alloc | immedPEI | ArchInstructionFormat.New_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)3, ArchInstructionFormat.New_format,  //NEW_UNRESOLVED
                      (alloc | immedPEI | dynLink | ArchInstructionFormat.New_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)4, ArchInstructionFormat.NewArray_format,  //NEWARRAY
                      (alloc | immedPEI | ArchInstructionFormat.NewArray_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)5, ArchInstructionFormat.NewArray_format,  //NEWARRAY_UNRESOLVED
                      (alloc | immedPEI | dynLink | ArchInstructionFormat.NewArray_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)6, ArchInstructionFormat.Athrow_format,  //ATHROW
                      (ethrow | ArchInstructionFormat.Athrow_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)7, ArchInstructionFormat.TypeCheck_format,  //CHECKCAST
                      (immedPEI | ArchInstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)8, ArchInstructionFormat.TypeCheck_format,  //CHECKCAST_NOTNULL
                      (immedPEI | ArchInstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)9, ArchInstructionFormat.TypeCheck_format,  //CHECKCAST_UNRESOLVED
                      (immedPEI | dynLink | ArchInstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)10, ArchInstructionFormat.TypeCheck_format,  //MUST_IMPLEMENT_INTERFACE
                      (immedPEI | ArchInstructionFormat.TypeCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)11, ArchInstructionFormat.InstanceOf_format,  //INSTANCEOF
                      (none | ArchInstructionFormat.InstanceOf_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)12, ArchInstructionFormat.InstanceOf_format,  //INSTANCEOF_NOTNULL
                      (none | ArchInstructionFormat.InstanceOf_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)13, ArchInstructionFormat.InstanceOf_format,  //INSTANCEOF_UNRESOLVED
                      (immedPEI | dynLink | ArchInstructionFormat.InstanceOf_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)14, ArchInstructionFormat.MonitorOp_format,  //MONITORENTER
                      (memAsLoad | memAsStore | acquire | tsp | ArchInstructionFormat.MonitorOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)15, ArchInstructionFormat.MonitorOp_format,  //MONITOREXIT
                      (memAsLoad | memAsStore | release | tsp | immedPEI | ArchInstructionFormat.MonitorOp_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)16, ArchInstructionFormat.Multianewarray_format,  //NEWOBJMULTIARRAY
                      (alloc | immedPEI | dynLink | ArchInstructionFormat.Multianewarray_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)17, ArchInstructionFormat.GetStatic_format,  //GETSTATIC
                      (load | ArchInstructionFormat.GetStatic_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)18, ArchInstructionFormat.PutStatic_format,  //PUTSTATIC
                      (store | ArchInstructionFormat.PutStatic_traits),
                      0, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)19, ArchInstructionFormat.GetField_format,  //GETFIELD
                      (load | ArchInstructionFormat.GetField_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)20, ArchInstructionFormat.PutField_format,  //PUTFIELD
                      (store | ArchInstructionFormat.PutField_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)21, ArchInstructionFormat.ZeroCheck_format,  //INT_ZERO_CHECK
                      (immedPEI | ArchInstructionFormat.ZeroCheck_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)22, ArchInstructionFormat.ZeroCheck_format,  //LONG_ZERO_CHECK
                      (immedPEI | ArchInstructionFormat.ZeroCheck_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)23, ArchInstructionFormat.BoundsCheck_format,  //BOUNDS_CHECK
                      (immedPEI | ArchInstructionFormat.BoundsCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)24, ArchInstructionFormat.StoreCheck_format,  //OBJARRAY_STORE_CHECK
                      (immedPEI | ArchInstructionFormat.StoreCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)25, ArchInstructionFormat.StoreCheck_format,  //OBJARRAY_STORE_CHECK_NOTNULL
                      (immedPEI | ArchInstructionFormat.StoreCheck_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)26, ArchInstructionFormat.InlineGuard_format,  //IG_PATCH_POINT
                      (branch | conditional | ArchInstructionFormat.InlineGuard_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)27, ArchInstructionFormat.InlineGuard_format,  //IG_CLASS_TEST
                      (branch | conditional | ArchInstructionFormat.InlineGuard_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)28, ArchInstructionFormat.InlineGuard_format,  //IG_METHOD_TEST
                      (branch | conditional | ArchInstructionFormat.InlineGuard_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)29, ArchInstructionFormat.TableSwitch_format,  //TABLESWITCH
                      (branch | ArchInstructionFormat.TableSwitch_traits),
                      0, 0, 7,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)30, ArchInstructionFormat.LookupSwitch_format,  //LOOKUPSWITCH
                      (branch | ArchInstructionFormat.LookupSwitch_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)31, ArchInstructionFormat.ALoad_format,  //INT_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)32, ArchInstructionFormat.ALoad_format,  //LONG_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)33, ArchInstructionFormat.ALoad_format,  //FLOAT_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)34, ArchInstructionFormat.ALoad_format,  //DOUBLE_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)35, ArchInstructionFormat.ALoad_format,  //REF_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)36, ArchInstructionFormat.ALoad_format,  //UBYTE_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)37, ArchInstructionFormat.ALoad_format,  //BYTE_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)38, ArchInstructionFormat.ALoad_format,  //USHORT_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)39, ArchInstructionFormat.ALoad_format,  //SHORT_ALOAD
                      (load | ArchInstructionFormat.ALoad_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)40, ArchInstructionFormat.AStore_format,  //INT_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)41, ArchInstructionFormat.AStore_format,  //LONG_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)42, ArchInstructionFormat.AStore_format,  //FLOAT_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)43, ArchInstructionFormat.AStore_format,  //DOUBLE_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)44, ArchInstructionFormat.AStore_format,  //REF_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)45, ArchInstructionFormat.AStore_format,  //BYTE_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)46, ArchInstructionFormat.AStore_format,  //SHORT_ASTORE
                      (store | ArchInstructionFormat.AStore_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)47, ArchInstructionFormat.IfCmp_format,  //INT_IFCMP
                      (branch | conditional | ArchInstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)48, ArchInstructionFormat.IfCmp2_format,  //INT_IFCMP2
                      (branch | conditional | ArchInstructionFormat.IfCmp2_traits),
                      1, 0, 8,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)49, ArchInstructionFormat.IfCmp_format,  //LONG_IFCMP
                      (branch | conditional | ArchInstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)50, ArchInstructionFormat.IfCmp_format,  //FLOAT_IFCMP
                      (branch | conditional | ArchInstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)51, ArchInstructionFormat.IfCmp_format,  //DOUBLE_IFCMP
                      (branch | conditional | ArchInstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)52, ArchInstructionFormat.IfCmp_format,  //REF_IFCMP
                      (branch | conditional | ArchInstructionFormat.IfCmp_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)53, ArchInstructionFormat.Label_format,  //LABEL
                      (none | ArchInstructionFormat.Label_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)54, ArchInstructionFormat.BBend_format,  //BBEND
                      (none | ArchInstructionFormat.BBend_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)55, ArchInstructionFormat.Empty_format,  //UNINT_BEGIN
                      (none | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)56, ArchInstructionFormat.Empty_format,  //UNINT_END
                      (none | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)57, ArchInstructionFormat.Empty_format,  //FENCE
                      (memAsLoad | memAsStore | release | acquire | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)58, ArchInstructionFormat.Empty_format,  //READ_CEILING
                      (memAsLoad | memAsStore | acquire | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)59, ArchInstructionFormat.Empty_format,  //WRITE_FLOOR
                      (memAsLoad | memAsStore | release | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)60, ArchInstructionFormat.Phi_format,  //PHI
                      (none | ArchInstructionFormat.Phi_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)61, ArchInstructionFormat.Unary_format,  //SPLIT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)62, ArchInstructionFormat.GuardedUnary_format,  //PI
                      (none | ArchInstructionFormat.GuardedUnary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)63, ArchInstructionFormat.Empty_format,  //NOP
                      (none | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)64, ArchInstructionFormat.Move_format,  //INT_MOVE
                      (move | ArchInstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)65, ArchInstructionFormat.Move_format,  //LONG_MOVE
                      (move | ArchInstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)66, ArchInstructionFormat.Move_format,  //FLOAT_MOVE
                      (move | ArchInstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)67, ArchInstructionFormat.Move_format,  //DOUBLE_MOVE
                      (move | ArchInstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)68, ArchInstructionFormat.Move_format,  //REF_MOVE
                      (move | ArchInstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)69, ArchInstructionFormat.Move_format,  //GUARD_MOVE
                      (move | ArchInstructionFormat.Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)70, ArchInstructionFormat.CondMove_format,  //INT_COND_MOVE
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)71, ArchInstructionFormat.CondMove_format,  //LONG_COND_MOVE
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)72, ArchInstructionFormat.CondMove_format,  //FLOAT_COND_MOVE
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)73, ArchInstructionFormat.CondMove_format,  //DOUBLE_COND_MOVE
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)74, ArchInstructionFormat.CondMove_format,  //REF_COND_MOVE
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)75, ArchInstructionFormat.CondMove_format,  //GUARD_COND_MOVE
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)76, ArchInstructionFormat.Binary_format,  //GUARD_COMBINE
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)77, ArchInstructionFormat.Binary_format,  //REF_ADD
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)78, ArchInstructionFormat.Binary_format,  //INT_ADD
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)79, ArchInstructionFormat.Binary_format,  //LONG_ADD
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)80, ArchInstructionFormat.Binary_format,  //FLOAT_ADD
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)81, ArchInstructionFormat.Binary_format,  //DOUBLE_ADD
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)82, ArchInstructionFormat.Binary_format,  //REF_SUB
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)83, ArchInstructionFormat.Binary_format,  //INT_SUB
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)84, ArchInstructionFormat.Binary_format,  //LONG_SUB
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)85, ArchInstructionFormat.Binary_format,  //FLOAT_SUB
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)86, ArchInstructionFormat.Binary_format,  //DOUBLE_SUB
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)87, ArchInstructionFormat.Binary_format,  //INT_MUL
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)88, ArchInstructionFormat.Binary_format,  //LONG_MUL
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)89, ArchInstructionFormat.Binary_format,  //FLOAT_MUL
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)90, ArchInstructionFormat.Binary_format,  //DOUBLE_MUL
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)91, ArchInstructionFormat.GuardedBinary_format,  //INT_DIV
                      (none | ArchInstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)92, ArchInstructionFormat.GuardedBinary_format,  //LONG_DIV
                      (none | ArchInstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)93, ArchInstructionFormat.Binary_format,  //FLOAT_DIV
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)94, ArchInstructionFormat.Binary_format,  //DOUBLE_DIV
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)95, ArchInstructionFormat.GuardedBinary_format,  //INT_REM
                      (none | ArchInstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)96, ArchInstructionFormat.GuardedBinary_format,  //LONG_REM
                      (none | ArchInstructionFormat.GuardedBinary_traits),
                      1, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)97, ArchInstructionFormat.Binary_format,  //FLOAT_REM
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new ArchOperator((char)98, ArchInstructionFormat.Binary_format,  //DOUBLE_REM
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new ArchOperator((char)99, ArchInstructionFormat.Unary_format,  //REF_NEG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)100, ArchInstructionFormat.Unary_format,  //INT_NEG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)101, ArchInstructionFormat.Unary_format,  //LONG_NEG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)102, ArchInstructionFormat.Unary_format,  //FLOAT_NEG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)103, ArchInstructionFormat.Unary_format,  //DOUBLE_NEG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)104, ArchInstructionFormat.Unary_format,  //FLOAT_SQRT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)105, ArchInstructionFormat.Unary_format,  //DOUBLE_SQRT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)106, ArchInstructionFormat.Binary_format,  //REF_SHL
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)107, ArchInstructionFormat.Binary_format,  //INT_SHL
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)108, ArchInstructionFormat.Binary_format,  //LONG_SHL
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)109, ArchInstructionFormat.Binary_format,  //REF_SHR
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)110, ArchInstructionFormat.Binary_format,  //INT_SHR
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)111, ArchInstructionFormat.Binary_format,  //LONG_SHR
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)112, ArchInstructionFormat.Binary_format,  //REF_USHR
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)113, ArchInstructionFormat.Binary_format,  //INT_USHR
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)114, ArchInstructionFormat.Binary_format,  //LONG_USHR
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)115, ArchInstructionFormat.Binary_format,  //REF_AND
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)116, ArchInstructionFormat.Binary_format,  //INT_AND
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)117, ArchInstructionFormat.Binary_format,  //LONG_AND
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)118, ArchInstructionFormat.Binary_format,  //REF_OR
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)119, ArchInstructionFormat.Binary_format,  //INT_OR
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)120, ArchInstructionFormat.Binary_format,  //LONG_OR
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)121, ArchInstructionFormat.Binary_format,  //REF_XOR
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)122, ArchInstructionFormat.Binary_format,  //INT_XOR
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)123, ArchInstructionFormat.Unary_format,  //REF_NOT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)124, ArchInstructionFormat.Unary_format,  //INT_NOT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)125, ArchInstructionFormat.Unary_format,  //LONG_NOT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)126, ArchInstructionFormat.Binary_format,  //LONG_XOR
                      (commutative | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)127, ArchInstructionFormat.Unary_format,  //INT_2ADDRSigExt
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)128, ArchInstructionFormat.Unary_format,  //INT_2ADDRZerExt
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)129, ArchInstructionFormat.Unary_format,  //LONG_2ADDR
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)130, ArchInstructionFormat.Unary_format,  //ADDR_2INT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)131, ArchInstructionFormat.Unary_format,  //ADDR_2LONG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)132, ArchInstructionFormat.Unary_format,  //INT_2LONG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)133, ArchInstructionFormat.Unary_format,  //INT_2FLOAT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new ArchOperator((char)134, ArchInstructionFormat.Unary_format,  //INT_2DOUBLE
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskIEEEMagicUses,
                      PhysicalDefUse.mask),
     new ArchOperator((char)135, ArchInstructionFormat.Unary_format,  //LONG_2INT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)136, ArchInstructionFormat.Unary_format,  //LONG_2FLOAT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)137, ArchInstructionFormat.Unary_format,  //LONG_2DOUBLE
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)138, ArchInstructionFormat.Unary_format,  //FLOAT_2INT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)139, ArchInstructionFormat.Unary_format,  //FLOAT_2LONG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)140, ArchInstructionFormat.Unary_format,  //FLOAT_2DOUBLE
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)141, ArchInstructionFormat.Unary_format,  //DOUBLE_2INT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)142, ArchInstructionFormat.Unary_format,  //DOUBLE_2LONG
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)143, ArchInstructionFormat.Unary_format,  //DOUBLE_2FLOAT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)144, ArchInstructionFormat.Unary_format,  //INT_2BYTE
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)145, ArchInstructionFormat.Unary_format,  //INT_2USHORT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)146, ArchInstructionFormat.Unary_format,  //INT_2SHORT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)147, ArchInstructionFormat.Binary_format,  //LONG_CMP
                      (compare | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)148, ArchInstructionFormat.Binary_format,  //FLOAT_CMPL
                      (compare | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)149, ArchInstructionFormat.Binary_format,  //FLOAT_CMPG
                      (compare | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)150, ArchInstructionFormat.Binary_format,  //DOUBLE_CMPL
                      (compare | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)151, ArchInstructionFormat.Binary_format,  //DOUBLE_CMPG
                      (compare | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)152, ArchInstructionFormat.Return_format,  //RETURN
                      (ret | ArchInstructionFormat.Return_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)153, ArchInstructionFormat.NullCheck_format,  //NULL_CHECK
                      (immedPEI | ArchInstructionFormat.NullCheck_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)154, ArchInstructionFormat.Goto_format,  //GOTO
                      (branch | ArchInstructionFormat.Goto_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)155, ArchInstructionFormat.Unary_format,  //BOOLEAN_NOT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)156, ArchInstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_INT
                      (compare | ArchInstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)157, ArchInstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_ADDR
                      (compare | ArchInstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)158, ArchInstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_LONG
                      (compare | ArchInstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)159, ArchInstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_FLOAT
                      (compare | ArchInstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)160, ArchInstructionFormat.BooleanCmp_format,  //BOOLEAN_CMP_DOUBLE
                      (compare | ArchInstructionFormat.BooleanCmp_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)161, ArchInstructionFormat.Load_format,  //BYTE_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)162, ArchInstructionFormat.Load_format,  //UBYTE_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)163, ArchInstructionFormat.Load_format,  //SHORT_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)164, ArchInstructionFormat.Load_format,  //USHORT_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)165, ArchInstructionFormat.Load_format,  //REF_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)166, ArchInstructionFormat.Store_format,  //REF_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)167, ArchInstructionFormat.Load_format,  //INT_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)168, ArchInstructionFormat.Load_format,  //LONG_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)169, ArchInstructionFormat.Load_format,  //FLOAT_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)170, ArchInstructionFormat.Load_format,  //DOUBLE_LOAD
                      (load | ArchInstructionFormat.Load_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)171, ArchInstructionFormat.Store_format,  //BYTE_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)172, ArchInstructionFormat.Store_format,  //SHORT_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)173, ArchInstructionFormat.Store_format,  //INT_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)174, ArchInstructionFormat.Store_format,  //LONG_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)175, ArchInstructionFormat.Store_format,  //FLOAT_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)176, ArchInstructionFormat.Store_format,  //DOUBLE_STORE
                      (store | ArchInstructionFormat.Store_traits),
                      0, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)177, ArchInstructionFormat.Prepare_format,  //PREPARE_INT
                      (load | acquire | ArchInstructionFormat.Prepare_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)178, ArchInstructionFormat.Prepare_format,  //PREPARE_ADDR
                      (load | acquire | ArchInstructionFormat.Prepare_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)179, ArchInstructionFormat.Prepare_format,  //PREPARE_LONG
                      (load | acquire | ArchInstructionFormat.Prepare_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)180, ArchInstructionFormat.Attempt_format,  //ATTEMPT_INT
                      (load | store | compare | release | ArchInstructionFormat.Attempt_traits),
                      1, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)181, ArchInstructionFormat.Attempt_format,  //ATTEMPT_ADDR
                      (load | store | compare | release | ArchInstructionFormat.Attempt_traits),
                      1, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)182, ArchInstructionFormat.Attempt_format,  //ATTEMPT_LONG
                      (load | store | compare | release  | ArchInstructionFormat.Attempt_traits),
                      1, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)183, ArchInstructionFormat.Call_format,  //CALL
                      (call | memAsLoad | memAsStore | dynLink | immedPEI | ArchInstructionFormat.Call_traits),
                      1, 0, 3,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new ArchOperator((char)184, ArchInstructionFormat.Call_format,  //SYSCALL
                      (call | memAsLoad | memAsStore | ArchInstructionFormat.Call_traits),
                      1, 0, 3,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new ArchOperator((char)185, ArchInstructionFormat.Empty_format,  //YIELDPOINT_PROLOGUE
                      (tsp | yieldPoint | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)186, ArchInstructionFormat.Empty_format,  //YIELDPOINT_EPILOGUE
                      (tsp | yieldPoint | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)187, ArchInstructionFormat.Empty_format,  //YIELDPOINT_BACKEDGE
                      (tsp | yieldPoint | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)188, ArchInstructionFormat.OsrPoint_format,  //YIELDPOINT_OSR
                      (tsp | yieldPoint | ArchInstructionFormat.OsrPoint_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)189, ArchInstructionFormat.OsrBarrier_format,  //OSR_BARRIER
                      (none | ArchInstructionFormat.OsrBarrier_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)190, ArchInstructionFormat.Prologue_format,  //IR_PROLOGUE
                      (immedPEI | ArchInstructionFormat.Prologue_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)191, ArchInstructionFormat.CacheOp_format,  //RESOLVE
                      (tsp | dynLink | immedPEI | ArchInstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)192, ArchInstructionFormat.Unary_format,  //RESOLVE_MEMBER
                      (tsp | dynLink | immedPEI | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)193, ArchInstructionFormat.Nullary_format,  //GET_TIME_BASE
                      (none | ArchInstructionFormat.Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)194, ArchInstructionFormat.InstrumentedCounter_format,  //INSTRUMENTED_EVENT_COUNTER
                      (none | ArchInstructionFormat.InstrumentedCounter_traits),
                      0, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)195, ArchInstructionFormat.TrapIf_format,  //TRAP_IF
                      (immedPEI | ArchInstructionFormat.TrapIf_traits),
                      1, 0, 4,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)196, ArchInstructionFormat.Trap_format,  //TRAP
                      (immedPEI | ArchInstructionFormat.Trap_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)197, ArchInstructionFormat.Empty_format,  //ILLEGAL_INSTRUCTION
                      (immedPEI | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)198, ArchInstructionFormat.Unary_format,  //FLOAT_AS_INT_BITS
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)199, ArchInstructionFormat.Unary_format,  //INT_BITS_AS_FLOAT
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)200, ArchInstructionFormat.Unary_format,  //DOUBLE_AS_LONG_BITS
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)201, ArchInstructionFormat.Unary_format,  //LONG_BITS_AS_DOUBLE
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)202, ArchInstructionFormat.GuardedUnary_format,  //ARRAYLENGTH
                      (none | ArchInstructionFormat.GuardedUnary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)203, ArchInstructionFormat.Nullary_format,  //FRAMESIZE
                      (none | ArchInstructionFormat.Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)204, ArchInstructionFormat.GuardedUnary_format,  //GET_OBJ_TIB
                      (none | ArchInstructionFormat.GuardedUnary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)205, ArchInstructionFormat.Unary_format,  //GET_CLASS_TIB
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)206, ArchInstructionFormat.Unary_format,  //GET_TYPE_FROM_TIB
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)207, ArchInstructionFormat.Unary_format,  //GET_SUPERCLASS_IDS_FROM_TIB
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)208, ArchInstructionFormat.Unary_format,  //GET_DOES_IMPLEMENT_FROM_TIB
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)209, ArchInstructionFormat.Unary_format,  //GET_ARRAY_ELEMENT_TIB_FROM_TIB
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)210, ArchInstructionFormat.LowTableSwitch_format,  //LOWTABLESWITCH
                      (branch | ArchInstructionFormat.LowTableSwitch_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)211, ArchInstructionFormat.Unassigned_format,  //ADDRESS_CONSTANT
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)212, ArchInstructionFormat.Unassigned_format,  //INT_CONSTANT
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)213, ArchInstructionFormat.Unassigned_format,  //LONG_CONSTANT
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)214, ArchInstructionFormat.Unassigned_format,  //REGISTER
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)215, ArchInstructionFormat.Unassigned_format,  //OTHER_OPERAND
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)216, ArchInstructionFormat.Unassigned_format,  //NULL
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)217, ArchInstructionFormat.Unassigned_format,  //BRANCH_TARGET
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
  //////////////////////////
  // END   Architecture Independent opcodes.
  // BEGIN Architecture Dependent opcodes & MIR.
  //////////////////////////
     new ArchOperator((char)(0 + Operators.ARCH_INDEPENDENT_END_opcode),  //MATERIALIZE_FP_CONSTANT
                      ArchInstructionFormat.Binary_format,
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(1 + Operators.ARCH_INDEPENDENT_END_opcode),  //ROUND_TO_ZERO
                      ArchInstructionFormat.Empty_format,
                      (none | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(2 + Operators.ARCH_INDEPENDENT_END_opcode),  //CLEAR_FLOATING_POINT_STATE
                      ArchInstructionFormat.Empty_format,
                      (none | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(3 + Operators.ARCH_INDEPENDENT_END_opcode),  //PREFETCH
                      ArchInstructionFormat.CacheOp_format,
                      (none | ArchInstructionFormat.CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(4 + Operators.ARCH_INDEPENDENT_END_opcode),  //PAUSE
                      ArchInstructionFormat.Empty_format,
                      (none | ArchInstructionFormat.Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(5 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_ADD
                      ArchInstructionFormat.Binary_format,
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(6 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_SUB
                      ArchInstructionFormat.Binary_format,
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(7 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_MUL
                      ArchInstructionFormat.Binary_format,
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(8 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_DIV
                      ArchInstructionFormat.Binary_format,
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(9 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_NEG
                      ArchInstructionFormat.Unary_format,
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(10 + Operators.ARCH_INDEPENDENT_END_opcode),  //FP_REM
                      ArchInstructionFormat.Binary_format,
                      (none | ArchInstructionFormat.Binary_traits),
                      1, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(11 + Operators.ARCH_INDEPENDENT_END_opcode),  //INT_2FP
                      ArchInstructionFormat.Unary_format,
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(12 + Operators.ARCH_INDEPENDENT_END_opcode),  //LONG_2FP
                      ArchInstructionFormat.Unary_format,
                      (none | ArchInstructionFormat.Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(13 + Operators.ARCH_INDEPENDENT_END_opcode),  //CMP_CMOV
                      ArchInstructionFormat.CondMove_format,
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(14 + Operators.ARCH_INDEPENDENT_END_opcode),  //FCMP_CMOV
                      ArchInstructionFormat.CondMove_format,
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(15 + Operators.ARCH_INDEPENDENT_END_opcode),  //LCMP_CMOV
                      ArchInstructionFormat.CondMove_format,
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(16 + Operators.ARCH_INDEPENDENT_END_opcode),  //CMP_FCMOV
                      ArchInstructionFormat.CondMove_format,
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(17 + Operators.ARCH_INDEPENDENT_END_opcode),  //FCMP_FCMOV
                      ArchInstructionFormat.CondMove_format,
                      (compare | ArchInstructionFormat.CondMove_traits),
                      1, 0, 5,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(18 + Operators.ARCH_INDEPENDENT_END_opcode),  //CALL_SAVE_VOLATILE
                      ArchInstructionFormat.MIR_Call_format,
                      (call | immedPEI | ArchInstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new ArchOperator((char)(19 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_START
                      ArchInstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(20 + Operators.ARCH_INDEPENDENT_END_opcode),  //REQUIRE_ESP
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(21 + Operators.ARCH_INDEPENDENT_END_opcode),  //ADVISE_ESP
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(22 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_LOWTABLESWITCH
                      ArchInstructionFormat.MIR_LowTableSwitch_format,
                      (branch | ArchInstructionFormat.MIR_LowTableSwitch_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(23 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_METHODSTART
                      ArchInstructionFormat.MIR_Nullary_format,
                      (none | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(24 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCLEAR
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(25 + Operators.ARCH_INDEPENDENT_END_opcode),  //DUMMY_DEF
                      ArchInstructionFormat.MIR_Nullary_format,
                      (none | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(26 + Operators.ARCH_INDEPENDENT_END_opcode),  //DUMMY_USE
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(27 + Operators.ARCH_INDEPENDENT_END_opcode),  //IMMQ_MOV
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(28 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMOV_ENDING_LIVE_RANGE
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(29 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMOV
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(30 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_TRAPIF
                      ArchInstructionFormat.MIR_TrapIf_format,
                      (immedPEI | ArchInstructionFormat.MIR_TrapIf_traits),
                      1, 0, 4,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(31 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_OFFSET
                      ArchInstructionFormat.MIR_CaseLabel_format,
                      (none | ArchInstructionFormat.MIR_CaseLabel_traits),
                      0, 0, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(32 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LOCK_CMPXCHG
                      ArchInstructionFormat.MIR_CompareExchange_format,
                      (compare | ArchInstructionFormat.MIR_CompareExchange_traits),
                      0, 2, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(33 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LOCK_CMPXCHG8B
                      ArchInstructionFormat.MIR_CompareExchange8B_format,
                      (compare | ArchInstructionFormat.MIR_CompareExchange8B_traits),
                      0, 3, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(34 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADC
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.maskCF),
     new ArchOperator((char)(35 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(36 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_AND
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(37 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BSWAP
                      ArchInstructionFormat.MIR_UnaryAcc_format,
                      (none | ArchInstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(38 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BT
                      ArchInstructionFormat.MIR_Test_format,
                      (none | ArchInstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(39 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BTC
                      ArchInstructionFormat.MIR_Test_format,
                      (none | ArchInstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(40 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BTR
                      ArchInstructionFormat.MIR_Test_format,
                      (none | ArchInstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(41 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_BTS
                      ArchInstructionFormat.MIR_Test_format,
                      (none | ArchInstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(42 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SYSCALL
                      ArchInstructionFormat.MIR_Call_format,
                      (call | ArchInstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new ArchOperator((char)(43 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CALL
                      ArchInstructionFormat.MIR_Call_format,
                      (call | immedPEI | ArchInstructionFormat.MIR_Call_traits),
                      2, 0, 2,
                      PhysicalDefUse.maskcallDefs,
                      PhysicalDefUse.maskcallUses),
     new ArchOperator((char)(44 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CDQ
                      ArchInstructionFormat.MIR_ConvertDW2QW_format,
                      (none | ArchInstructionFormat.MIR_ConvertDW2QW_traits),
                      1, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(45 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CDO
                      ArchInstructionFormat.MIR_ConvertDW2QW_format,
                      (none | ArchInstructionFormat.MIR_ConvertDW2QW_traits),
                      1, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(46 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CDQE
                      ArchInstructionFormat.MIR_ConvertDW2QW_format,
                      (none | ArchInstructionFormat.MIR_ConvertDW2QW_traits),
                      1, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(47 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMOV
                      ArchInstructionFormat.MIR_CondMove_format,
                      (none | ArchInstructionFormat.MIR_CondMove_traits),
                      0, 1, 2,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskCF_OF_PF_SF_ZF),
     new ArchOperator((char)(48 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMP
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(49 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPXCHG
                      ArchInstructionFormat.MIR_CompareExchange_format,
                      (compare | ArchInstructionFormat.MIR_CompareExchange_traits),
                      0, 2, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(50 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPXCHG8B
                      ArchInstructionFormat.MIR_CompareExchange8B_format,
                      (compare | ArchInstructionFormat.MIR_CompareExchange8B_traits),
                      0, 3, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(51 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DEC
                      ArchInstructionFormat.MIR_UnaryAcc_format,
                      (none | ArchInstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskAF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(52 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DIV
                      ArchInstructionFormat.MIR_Divide_format,
                      (none | ArchInstructionFormat.MIR_Divide_traits),
                      0, 2, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(53 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FADD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(54 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FADDP
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(55 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCHS
                      ArchInstructionFormat.MIR_UnaryAcc_format,
                      (none | ArchInstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(56 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCMOV
                      ArchInstructionFormat.MIR_CondMove_format,
                      (none | ArchInstructionFormat.MIR_CondMove_traits),
                      0, 1, 2,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.maskCF_PF_ZF),
     new ArchOperator((char)(57 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCOMI
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(58 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FCOMIP
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | fpPop | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(59 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIV
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(60 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIVP
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(61 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIVR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(62 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FDIVRP
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(63 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FEXAM
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(64 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FXCH
                      ArchInstructionFormat.MIR_XChng_format,
                      (none | ArchInstructionFormat.MIR_XChng_traits),
                      0, 2, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(65 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FFREE
                      ArchInstructionFormat.MIR_Nullary_format,
                      (none | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(66 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FFREEP
                      ArchInstructionFormat.MIR_Nullary_format,
                      (none | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(67 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIADD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(68 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIDIV
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(69 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIDIVR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(70 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FILD
                      ArchInstructionFormat.MIR_Move_format,
                      (fpPush | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(71 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIMUL
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(72 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FINIT
                      ArchInstructionFormat.MIR_Empty_format,
                      (none | ArchInstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(73 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FIST
                      ArchInstructionFormat.MIR_Move_format,
                      (none | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(74 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FISTP
                      ArchInstructionFormat.MIR_Move_format,
                      (fpPop | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(75 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FISUB
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(76 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FISUBR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(77 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLD
                      ArchInstructionFormat.MIR_Move_format,
                      (fpPush | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(78 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDCW
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(79 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLD1
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(80 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDL2T
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(81 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDL2E
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(82 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDPI
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(83 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDLG2
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(84 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDLN2
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(85 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FLDZ
                      ArchInstructionFormat.MIR_Nullary_format,
                      (fpPush | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(86 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMUL
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(87 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FMULP
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(88 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNSTCW
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(89 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNSTSW
                      ArchInstructionFormat.MIR_Unary_format,
                      (none | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.maskC0_C1_C2_C3),
     new ArchOperator((char)(90 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNINIT
                      ArchInstructionFormat.MIR_Empty_format,
                      (none | ArchInstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(91 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FNSAVE
                      ArchInstructionFormat.MIR_FSave_format,
                      (none | ArchInstructionFormat.MIR_FSave_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(92 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FPREM
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(93 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FRSTOR
                      ArchInstructionFormat.MIR_FSave_format,
                      (none | ArchInstructionFormat.MIR_FSave_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(94 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FST
                      ArchInstructionFormat.MIR_Move_format,
                      (none | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(95 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSTCW
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(96 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSTSW
                      ArchInstructionFormat.MIR_Unary_format,
                      (none | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.maskC0_C1_C2_C3),
     new ArchOperator((char)(97 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSTP
                      ArchInstructionFormat.MIR_Move_format,
                      (fpPop | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(98 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUB
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(99 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUBP
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(100 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUBR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(101 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FSUBRP
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (fpPop | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(102 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FUCOMI
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(103 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_FUCOMIP
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(104 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_IDIV
                      ArchInstructionFormat.MIR_Divide_format,
                      (none | ArchInstructionFormat.MIR_Divide_traits),
                      0, 2, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(105 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_IMUL1
                      ArchInstructionFormat.MIR_Multiply_format,
                      (none | ArchInstructionFormat.MIR_Multiply_traits),
                      1, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(106 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_IMUL2
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(107 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_INC
                      ArchInstructionFormat.MIR_UnaryAcc_format,
                      (none | ArchInstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskAF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(108 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_INT
                      ArchInstructionFormat.MIR_Trap_format,
                      (immedPEI | ArchInstructionFormat.MIR_Trap_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(109 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_JCC
                      ArchInstructionFormat.MIR_CondBranch_format,
                      (branch | conditional | ArchInstructionFormat.MIR_CondBranch_traits),
                      0, 0, 3,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF),
     new ArchOperator((char)(110 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_JCC2
                      ArchInstructionFormat.MIR_CondBranch2_format,
                      (branch | conditional | ArchInstructionFormat.MIR_CondBranch2_traits),
                      0, 0, 6,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF),
     new ArchOperator((char)(111 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_JMP
                      ArchInstructionFormat.MIR_Branch_format,
                      (branch | ArchInstructionFormat.MIR_Branch_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(112 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LEA
                      ArchInstructionFormat.MIR_Lea_format,
                      (none | ArchInstructionFormat.MIR_Lea_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(113 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_LOCK
                      ArchInstructionFormat.MIR_Empty_format,
                      (none | ArchInstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(114 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOV
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(115 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZX__B
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(116 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSX__B
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(117 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZX__W
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(118 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSX__W
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(119 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZXQ__B
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(120 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSXQ__B
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(121 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVZXQ__W
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(122 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSXQ__W
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(123 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSXDQ
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(124 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MUL
                      ArchInstructionFormat.MIR_Multiply_format,
                      (none | ArchInstructionFormat.MIR_Multiply_traits),
                      1, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(125 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_NEG
                      ArchInstructionFormat.MIR_UnaryAcc_format,
                      (none | ArchInstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(126 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_NOT
                      ArchInstructionFormat.MIR_UnaryAcc_format,
                      (none | ArchInstructionFormat.MIR_UnaryAcc_traits),
                      0, 1, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(127 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_OR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(128 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MFENCE
                      ArchInstructionFormat.MIR_Empty_format,
                      (none | ArchInstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(129 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PAUSE
                      ArchInstructionFormat.MIR_Empty_format,
                      (none | ArchInstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(130 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_UD2
                      ArchInstructionFormat.MIR_Empty_format,
                      (none | ArchInstructionFormat.MIR_Empty_traits),
                      0, 0, 0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(131 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PREFETCHNTA
                      ArchInstructionFormat.MIR_CacheOp_format,
                      (none | ArchInstructionFormat.MIR_CacheOp_traits),
                      0, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(132 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_POP
                      ArchInstructionFormat.MIR_Nullary_format,
                      (none | ArchInstructionFormat.MIR_Nullary_traits),
                      1, 0, 0,
                      PhysicalDefUse.maskESP,
                      PhysicalDefUse.maskESP),
     new ArchOperator((char)(133 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PUSH
                      ArchInstructionFormat.MIR_UnaryNoRes_format,
                      (none | ArchInstructionFormat.MIR_UnaryNoRes_traits),
                      0, 0, 1,
                      PhysicalDefUse.maskESP,
                      PhysicalDefUse.maskESP),
     new ArchOperator((char)(134 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RCL
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.maskCF),
     new ArchOperator((char)(135 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RCR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.maskCF),
     new ArchOperator((char)(136 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ROL
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(137 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ROR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(138 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RET
                      ArchInstructionFormat.MIR_Return_format,
                      (ret | ArchInstructionFormat.MIR_Return_traits),
                      0, 0, 3,
                      PhysicalDefUse.maskESP,
                      PhysicalDefUse.maskESP),
     new ArchOperator((char)(139 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SAL
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(140 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SAR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(141 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHL
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(142 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(143 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SBB
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.maskCF),
     new ArchOperator((char)(144 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SET__B
                      ArchInstructionFormat.MIR_Set_format,
                      (none | ArchInstructionFormat.MIR_Set_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF),
     new ArchOperator((char)(145 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHLD
                      ArchInstructionFormat.MIR_DoubleShift_format,
                      (none | ArchInstructionFormat.MIR_DoubleShift_traits),
                      0, 1, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(146 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SHRD
                      ArchInstructionFormat.MIR_DoubleShift_format,
                      (none | ArchInstructionFormat.MIR_DoubleShift_traits),
                      0, 1, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(147 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SUB
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(148 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_TEST
                      ArchInstructionFormat.MIR_Test_format,
                      (none | ArchInstructionFormat.MIR_Test_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(149 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_XOR
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.maskAF_CF_OF_PF_SF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(150 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_RDTSC
                      ArchInstructionFormat.MIR_RDTSC_format,
                      (none | ArchInstructionFormat.MIR_RDTSC_traits),
                      2, 0, 0,
                      PhysicalDefUse.maskCF_OF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(151 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADDSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(152 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SUBSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(153 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MULSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(154 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DIVSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(155 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ADDSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(156 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SUBSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(157 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MULSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(158 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_DIVSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(159 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDPS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(160 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDPD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(161 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDNPS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(162 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ANDNPD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(163 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ORPS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(164 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_ORPD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(165 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_XORPS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(166 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_XORPD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(167 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_UCOMISS
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(168 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_UCOMISD
                      ArchInstructionFormat.MIR_Compare_format,
                      (compare | ArchInstructionFormat.MIR_Compare_traits),
                      0, 0, 2,
                      PhysicalDefUse.maskCF_PF_ZF,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(169 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPEQSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(170 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLTSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(171 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLESS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(172 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPUNORDSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(173 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNESS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(174 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLTSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(175 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLESS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(176 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPORDSS
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(177 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPEQSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(178 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLTSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(179 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPLESD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(180 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPUNORDSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(181 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNESD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(182 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLTSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(183 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPNLESD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(184 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CMPORDSD
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(185 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVAPD
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(186 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVAPS
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(187 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVLPD
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(188 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVLPS
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(189 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSS
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(190 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVSD
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(191 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVD
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(192 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_MOVQ
                      ArchInstructionFormat.MIR_Move_format,
                      (move | ArchInstructionFormat.MIR_Move_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(193 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PSLLQ
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(194 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_PSRLQ
                      ArchInstructionFormat.MIR_BinaryAcc_format,
                      (none | ArchInstructionFormat.MIR_BinaryAcc_traits),
                      0, 1, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(195 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SQRTSS
                      ArchInstructionFormat.MIR_Unary_format,
                      (none | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(196 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_SQRTSD
                      ArchInstructionFormat.MIR_Unary_format,
                      (none | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.maskC0_C1_C2_C3,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(197 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSI2SS
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(198 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSS2SD
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(199 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSS2SI
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(200 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTTSS2SI
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(201 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSI2SD
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(202 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSD2SS
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(203 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSD2SI
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(204 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTTSD2SI
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(205 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSI2SDQ
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(206 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTSD2SIQ
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(207 + Operators.ARCH_INDEPENDENT_END_opcode),  //IA32_CVTTSD2SIQ
                      ArchInstructionFormat.MIR_Unary_format,
                      (move | ArchInstructionFormat.MIR_Unary_traits),
                      1, 0, 1,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     new ArchOperator((char)(208 + Operators.ARCH_INDEPENDENT_END_opcode),  //MIR_END
                      ArchInstructionFormat.Unassigned_format,
                      (none),
                      0,0,0,
                      PhysicalDefUse.mask,
                      PhysicalDefUse.mask),
     null };

 
  @Pure
  public static Operator lookupOpcode(int opcode) {
    return OperatorArray[opcode];
  }

  /** @return array that holds all operators for this architecture */
  public static Operator[] operatorArray() {
    return OperatorArray;
  }

  /**
   * Instruction template used by the assembler to
   * generate binary code.  Only valid on MIR operators.
   */
  @Override
  public int instTemplate() {
    org.jikesrvm.compilers.opt.OptimizingCompilerException.UNREACHABLE();
    return 0;
  }

  /* Constructor for HIR/LIR */
  private ArchOperator(char opcode, byte format, int traits,
                       int numDefs, int numDefUses, int numUses,
                       int iDefs, int iUses) {
    super(opcode, format, traits, numDefs, numDefUses, numUses, iDefs, iUses);
  }

}
