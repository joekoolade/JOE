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
 * THIS FILE IS MACHINE GENERATED. DO NOT EDIT.
 * The input files are:
 *  Operators.template
 *  OperatorList.dat
 */

package org.jikesrvm.compilers.opt.ir;

/**
 * Operators and opcodes that are common to all architectures
 *
 * @see Operator
 */
public abstract class Operators {

  /* Architecture independent operator codes */

  /** Opcode identifier for GET_CAUGHT_EXCEPTION instructions */
  public static final char GET_CAUGHT_EXCEPTION_opcode = (char)0;
  /** Opcode identifier for SET_CAUGHT_EXCEPTION instructions */
  public static final char SET_CAUGHT_EXCEPTION_opcode = (char)1;
  /** Opcode identifier for NEW instructions */
  public static final char NEW_opcode = (char)2;
  /** Opcode identifier for NEW_UNRESOLVED instructions */
  public static final char NEW_UNRESOLVED_opcode = (char)3;
  /** Opcode identifier for NEWARRAY instructions */
  public static final char NEWARRAY_opcode = (char)4;
  /** Opcode identifier for NEWARRAY_UNRESOLVED instructions */
  public static final char NEWARRAY_UNRESOLVED_opcode = (char)5;
  /** Opcode identifier for ATHROW instructions */
  public static final char ATHROW_opcode = (char)6;
  /** Opcode identifier for CHECKCAST instructions */
  public static final char CHECKCAST_opcode = (char)7;
  /** Opcode identifier for CHECKCAST_NOTNULL instructions */
  public static final char CHECKCAST_NOTNULL_opcode = (char)8;
  /** Opcode identifier for CHECKCAST_UNRESOLVED instructions */
  public static final char CHECKCAST_UNRESOLVED_opcode = (char)9;
  /** Opcode identifier for MUST_IMPLEMENT_INTERFACE instructions */
  public static final char MUST_IMPLEMENT_INTERFACE_opcode = (char)10;
  /** Opcode identifier for INSTANCEOF instructions */
  public static final char INSTANCEOF_opcode = (char)11;
  /** Opcode identifier for INSTANCEOF_NOTNULL instructions */
  public static final char INSTANCEOF_NOTNULL_opcode = (char)12;
  /** Opcode identifier for INSTANCEOF_UNRESOLVED instructions */
  public static final char INSTANCEOF_UNRESOLVED_opcode = (char)13;
  /** Opcode identifier for MONITORENTER instructions */
  public static final char MONITORENTER_opcode = (char)14;
  /** Opcode identifier for MONITOREXIT instructions */
  public static final char MONITOREXIT_opcode = (char)15;
  /** Opcode identifier for NEWOBJMULTIARRAY instructions */
  public static final char NEWOBJMULTIARRAY_opcode = (char)16;
  /** Opcode identifier for GETSTATIC instructions */
  public static final char GETSTATIC_opcode = (char)17;
  /** Opcode identifier for PUTSTATIC instructions */
  public static final char PUTSTATIC_opcode = (char)18;
  /** Opcode identifier for GETFIELD instructions */
  public static final char GETFIELD_opcode = (char)19;
  /** Opcode identifier for PUTFIELD instructions */
  public static final char PUTFIELD_opcode = (char)20;
  /** Opcode identifier for INT_ZERO_CHECK instructions */
  public static final char INT_ZERO_CHECK_opcode = (char)21;
  /** Opcode identifier for LONG_ZERO_CHECK instructions */
  public static final char LONG_ZERO_CHECK_opcode = (char)22;
  /** Opcode identifier for BOUNDS_CHECK instructions */
  public static final char BOUNDS_CHECK_opcode = (char)23;
  /** Opcode identifier for OBJARRAY_STORE_CHECK instructions */
  public static final char OBJARRAY_STORE_CHECK_opcode = (char)24;
  /** Opcode identifier for OBJARRAY_STORE_CHECK_NOTNULL instructions */
  public static final char OBJARRAY_STORE_CHECK_NOTNULL_opcode = (char)25;
  /** Opcode identifier for IG_PATCH_POINT instructions */
  public static final char IG_PATCH_POINT_opcode = (char)26;
  /** Opcode identifier for IG_CLASS_TEST instructions */
  public static final char IG_CLASS_TEST_opcode = (char)27;
  /** Opcode identifier for IG_METHOD_TEST instructions */
  public static final char IG_METHOD_TEST_opcode = (char)28;
  /** Opcode identifier for TABLESWITCH instructions */
  public static final char TABLESWITCH_opcode = (char)29;
  /** Opcode identifier for LOOKUPSWITCH instructions */
  public static final char LOOKUPSWITCH_opcode = (char)30;
  /** Opcode identifier for INT_ALOAD instructions */
  public static final char INT_ALOAD_opcode = (char)31;
  /** Opcode identifier for LONG_ALOAD instructions */
  public static final char LONG_ALOAD_opcode = (char)32;
  /** Opcode identifier for FLOAT_ALOAD instructions */
  public static final char FLOAT_ALOAD_opcode = (char)33;
  /** Opcode identifier for DOUBLE_ALOAD instructions */
  public static final char DOUBLE_ALOAD_opcode = (char)34;
  /** Opcode identifier for REF_ALOAD instructions */
  public static final char REF_ALOAD_opcode = (char)35;
  /** Opcode identifier for UBYTE_ALOAD instructions */
  public static final char UBYTE_ALOAD_opcode = (char)36;
  /** Opcode identifier for BYTE_ALOAD instructions */
  public static final char BYTE_ALOAD_opcode = (char)37;
  /** Opcode identifier for USHORT_ALOAD instructions */
  public static final char USHORT_ALOAD_opcode = (char)38;
  /** Opcode identifier for SHORT_ALOAD instructions */
  public static final char SHORT_ALOAD_opcode = (char)39;
  /** Opcode identifier for INT_ASTORE instructions */
  public static final char INT_ASTORE_opcode = (char)40;
  /** Opcode identifier for LONG_ASTORE instructions */
  public static final char LONG_ASTORE_opcode = (char)41;
  /** Opcode identifier for FLOAT_ASTORE instructions */
  public static final char FLOAT_ASTORE_opcode = (char)42;
  /** Opcode identifier for DOUBLE_ASTORE instructions */
  public static final char DOUBLE_ASTORE_opcode = (char)43;
  /** Opcode identifier for REF_ASTORE instructions */
  public static final char REF_ASTORE_opcode = (char)44;
  /** Opcode identifier for BYTE_ASTORE instructions */
  public static final char BYTE_ASTORE_opcode = (char)45;
  /** Opcode identifier for SHORT_ASTORE instructions */
  public static final char SHORT_ASTORE_opcode = (char)46;
  /** Opcode identifier for INT_IFCMP instructions */
  public static final char INT_IFCMP_opcode = (char)47;
  /** Opcode identifier for INT_IFCMP2 instructions */
  public static final char INT_IFCMP2_opcode = (char)48;
  /** Opcode identifier for LONG_IFCMP instructions */
  public static final char LONG_IFCMP_opcode = (char)49;
  /** Opcode identifier for FLOAT_IFCMP instructions */
  public static final char FLOAT_IFCMP_opcode = (char)50;
  /** Opcode identifier for DOUBLE_IFCMP instructions */
  public static final char DOUBLE_IFCMP_opcode = (char)51;
  /** Opcode identifier for REF_IFCMP instructions */
  public static final char REF_IFCMP_opcode = (char)52;
  /** Opcode identifier for LABEL instructions */
  public static final char LABEL_opcode = (char)53;
  /** Opcode identifier for BBEND instructions */
  public static final char BBEND_opcode = (char)54;
  /** Opcode identifier for UNINT_BEGIN instructions */
  public static final char UNINT_BEGIN_opcode = (char)55;
  /** Opcode identifier for UNINT_END instructions */
  public static final char UNINT_END_opcode = (char)56;
  /** Opcode identifier for FENCE instructions */
  public static final char FENCE_opcode = (char)57;
  /** Opcode identifier for READ_CEILING instructions */
  public static final char READ_CEILING_opcode = (char)58;
  /** Opcode identifier for WRITE_FLOOR instructions */
  public static final char WRITE_FLOOR_opcode = (char)59;
  /** Opcode identifier for PHI instructions */
  public static final char PHI_opcode = (char)60;
  /** Opcode identifier for SPLIT instructions */
  public static final char SPLIT_opcode = (char)61;
  /** Opcode identifier for PI instructions */
  public static final char PI_opcode = (char)62;
  /** Opcode identifier for NOP instructions */
  public static final char NOP_opcode = (char)63;
  /** Opcode identifier for INT_MOVE instructions */
  public static final char INT_MOVE_opcode = (char)64;
  /** Opcode identifier for LONG_MOVE instructions */
  public static final char LONG_MOVE_opcode = (char)65;
  /** Opcode identifier for FLOAT_MOVE instructions */
  public static final char FLOAT_MOVE_opcode = (char)66;
  /** Opcode identifier for DOUBLE_MOVE instructions */
  public static final char DOUBLE_MOVE_opcode = (char)67;
  /** Opcode identifier for REF_MOVE instructions */
  public static final char REF_MOVE_opcode = (char)68;
  /** Opcode identifier for GUARD_MOVE instructions */
  public static final char GUARD_MOVE_opcode = (char)69;
  /** Opcode identifier for INT_COND_MOVE instructions */
  public static final char INT_COND_MOVE_opcode = (char)70;
  /** Opcode identifier for LONG_COND_MOVE instructions */
  public static final char LONG_COND_MOVE_opcode = (char)71;
  /** Opcode identifier for FLOAT_COND_MOVE instructions */
  public static final char FLOAT_COND_MOVE_opcode = (char)72;
  /** Opcode identifier for DOUBLE_COND_MOVE instructions */
  public static final char DOUBLE_COND_MOVE_opcode = (char)73;
  /** Opcode identifier for REF_COND_MOVE instructions */
  public static final char REF_COND_MOVE_opcode = (char)74;
  /** Opcode identifier for GUARD_COND_MOVE instructions */
  public static final char GUARD_COND_MOVE_opcode = (char)75;
  /** Opcode identifier for GUARD_COMBINE instructions */
  public static final char GUARD_COMBINE_opcode = (char)76;
  /** Opcode identifier for REF_ADD instructions */
  public static final char REF_ADD_opcode = (char)77;
  /** Opcode identifier for INT_ADD instructions */
  public static final char INT_ADD_opcode = (char)78;
  /** Opcode identifier for LONG_ADD instructions */
  public static final char LONG_ADD_opcode = (char)79;
  /** Opcode identifier for FLOAT_ADD instructions */
  public static final char FLOAT_ADD_opcode = (char)80;
  /** Opcode identifier for DOUBLE_ADD instructions */
  public static final char DOUBLE_ADD_opcode = (char)81;
  /** Opcode identifier for REF_SUB instructions */
  public static final char REF_SUB_opcode = (char)82;
  /** Opcode identifier for INT_SUB instructions */
  public static final char INT_SUB_opcode = (char)83;
  /** Opcode identifier for LONG_SUB instructions */
  public static final char LONG_SUB_opcode = (char)84;
  /** Opcode identifier for FLOAT_SUB instructions */
  public static final char FLOAT_SUB_opcode = (char)85;
  /** Opcode identifier for DOUBLE_SUB instructions */
  public static final char DOUBLE_SUB_opcode = (char)86;
  /** Opcode identifier for INT_MUL instructions */
  public static final char INT_MUL_opcode = (char)87;
  /** Opcode identifier for LONG_MUL instructions */
  public static final char LONG_MUL_opcode = (char)88;
  /** Opcode identifier for FLOAT_MUL instructions */
  public static final char FLOAT_MUL_opcode = (char)89;
  /** Opcode identifier for DOUBLE_MUL instructions */
  public static final char DOUBLE_MUL_opcode = (char)90;
  /** Opcode identifier for INT_DIV instructions */
  public static final char INT_DIV_opcode = (char)91;
  /** Opcode identifier for LONG_DIV instructions */
  public static final char LONG_DIV_opcode = (char)92;
  /** Opcode identifier for FLOAT_DIV instructions */
  public static final char FLOAT_DIV_opcode = (char)93;
  /** Opcode identifier for DOUBLE_DIV instructions */
  public static final char DOUBLE_DIV_opcode = (char)94;
  /** Opcode identifier for INT_REM instructions */
  public static final char INT_REM_opcode = (char)95;
  /** Opcode identifier for LONG_REM instructions */
  public static final char LONG_REM_opcode = (char)96;
  /** Opcode identifier for FLOAT_REM instructions */
  public static final char FLOAT_REM_opcode = (char)97;
  /** Opcode identifier for DOUBLE_REM instructions */
  public static final char DOUBLE_REM_opcode = (char)98;
  /** Opcode identifier for REF_NEG instructions */
  public static final char REF_NEG_opcode = (char)99;
  /** Opcode identifier for INT_NEG instructions */
  public static final char INT_NEG_opcode = (char)100;
  /** Opcode identifier for LONG_NEG instructions */
  public static final char LONG_NEG_opcode = (char)101;
  /** Opcode identifier for FLOAT_NEG instructions */
  public static final char FLOAT_NEG_opcode = (char)102;
  /** Opcode identifier for DOUBLE_NEG instructions */
  public static final char DOUBLE_NEG_opcode = (char)103;
  /** Opcode identifier for FLOAT_SQRT instructions */
  public static final char FLOAT_SQRT_opcode = (char)104;
  /** Opcode identifier for DOUBLE_SQRT instructions */
  public static final char DOUBLE_SQRT_opcode = (char)105;
  /** Opcode identifier for REF_SHL instructions */
  public static final char REF_SHL_opcode = (char)106;
  /** Opcode identifier for INT_SHL instructions */
  public static final char INT_SHL_opcode = (char)107;
  /** Opcode identifier for LONG_SHL instructions */
  public static final char LONG_SHL_opcode = (char)108;
  /** Opcode identifier for REF_SHR instructions */
  public static final char REF_SHR_opcode = (char)109;
  /** Opcode identifier for INT_SHR instructions */
  public static final char INT_SHR_opcode = (char)110;
  /** Opcode identifier for LONG_SHR instructions */
  public static final char LONG_SHR_opcode = (char)111;
  /** Opcode identifier for REF_USHR instructions */
  public static final char REF_USHR_opcode = (char)112;
  /** Opcode identifier for INT_USHR instructions */
  public static final char INT_USHR_opcode = (char)113;
  /** Opcode identifier for LONG_USHR instructions */
  public static final char LONG_USHR_opcode = (char)114;
  /** Opcode identifier for REF_AND instructions */
  public static final char REF_AND_opcode = (char)115;
  /** Opcode identifier for INT_AND instructions */
  public static final char INT_AND_opcode = (char)116;
  /** Opcode identifier for LONG_AND instructions */
  public static final char LONG_AND_opcode = (char)117;
  /** Opcode identifier for REF_OR instructions */
  public static final char REF_OR_opcode = (char)118;
  /** Opcode identifier for INT_OR instructions */
  public static final char INT_OR_opcode = (char)119;
  /** Opcode identifier for LONG_OR instructions */
  public static final char LONG_OR_opcode = (char)120;
  /** Opcode identifier for REF_XOR instructions */
  public static final char REF_XOR_opcode = (char)121;
  /** Opcode identifier for INT_XOR instructions */
  public static final char INT_XOR_opcode = (char)122;
  /** Opcode identifier for REF_NOT instructions */
  public static final char REF_NOT_opcode = (char)123;
  /** Opcode identifier for INT_NOT instructions */
  public static final char INT_NOT_opcode = (char)124;
  /** Opcode identifier for LONG_NOT instructions */
  public static final char LONG_NOT_opcode = (char)125;
  /** Opcode identifier for LONG_XOR instructions */
  public static final char LONG_XOR_opcode = (char)126;
  /** Opcode identifier for INT_2ADDRSigExt instructions */
  public static final char INT_2ADDRSigExt_opcode = (char)127;
  /** Opcode identifier for INT_2ADDRZerExt instructions */
  public static final char INT_2ADDRZerExt_opcode = (char)128;
  /** Opcode identifier for LONG_2ADDR instructions */
  public static final char LONG_2ADDR_opcode = (char)129;
  /** Opcode identifier for ADDR_2INT instructions */
  public static final char ADDR_2INT_opcode = (char)130;
  /** Opcode identifier for ADDR_2LONG instructions */
  public static final char ADDR_2LONG_opcode = (char)131;
  /** Opcode identifier for INT_2LONG instructions */
  public static final char INT_2LONG_opcode = (char)132;
  /** Opcode identifier for INT_2FLOAT instructions */
  public static final char INT_2FLOAT_opcode = (char)133;
  /** Opcode identifier for INT_2DOUBLE instructions */
  public static final char INT_2DOUBLE_opcode = (char)134;
  /** Opcode identifier for LONG_2INT instructions */
  public static final char LONG_2INT_opcode = (char)135;
  /** Opcode identifier for LONG_2FLOAT instructions */
  public static final char LONG_2FLOAT_opcode = (char)136;
  /** Opcode identifier for LONG_2DOUBLE instructions */
  public static final char LONG_2DOUBLE_opcode = (char)137;
  /** Opcode identifier for FLOAT_2INT instructions */
  public static final char FLOAT_2INT_opcode = (char)138;
  /** Opcode identifier for FLOAT_2LONG instructions */
  public static final char FLOAT_2LONG_opcode = (char)139;
  /** Opcode identifier for FLOAT_2DOUBLE instructions */
  public static final char FLOAT_2DOUBLE_opcode = (char)140;
  /** Opcode identifier for DOUBLE_2INT instructions */
  public static final char DOUBLE_2INT_opcode = (char)141;
  /** Opcode identifier for DOUBLE_2LONG instructions */
  public static final char DOUBLE_2LONG_opcode = (char)142;
  /** Opcode identifier for DOUBLE_2FLOAT instructions */
  public static final char DOUBLE_2FLOAT_opcode = (char)143;
  /** Opcode identifier for INT_2BYTE instructions */
  public static final char INT_2BYTE_opcode = (char)144;
  /** Opcode identifier for INT_2USHORT instructions */
  public static final char INT_2USHORT_opcode = (char)145;
  /** Opcode identifier for INT_2SHORT instructions */
  public static final char INT_2SHORT_opcode = (char)146;
  /** Opcode identifier for LONG_CMP instructions */
  public static final char LONG_CMP_opcode = (char)147;
  /** Opcode identifier for FLOAT_CMPL instructions */
  public static final char FLOAT_CMPL_opcode = (char)148;
  /** Opcode identifier for FLOAT_CMPG instructions */
  public static final char FLOAT_CMPG_opcode = (char)149;
  /** Opcode identifier for DOUBLE_CMPL instructions */
  public static final char DOUBLE_CMPL_opcode = (char)150;
  /** Opcode identifier for DOUBLE_CMPG instructions */
  public static final char DOUBLE_CMPG_opcode = (char)151;
  /** Opcode identifier for RETURN instructions */
  public static final char RETURN_opcode = (char)152;
  /** Opcode identifier for NULL_CHECK instructions */
  public static final char NULL_CHECK_opcode = (char)153;
  /** Opcode identifier for GOTO instructions */
  public static final char GOTO_opcode = (char)154;
  /** Opcode identifier for BOOLEAN_NOT instructions */
  public static final char BOOLEAN_NOT_opcode = (char)155;
  /** Opcode identifier for BOOLEAN_CMP_INT instructions */
  public static final char BOOLEAN_CMP_INT_opcode = (char)156;
  /** Opcode identifier for BOOLEAN_CMP_ADDR instructions */
  public static final char BOOLEAN_CMP_ADDR_opcode = (char)157;
  /** Opcode identifier for BOOLEAN_CMP_LONG instructions */
  public static final char BOOLEAN_CMP_LONG_opcode = (char)158;
  /** Opcode identifier for BOOLEAN_CMP_FLOAT instructions */
  public static final char BOOLEAN_CMP_FLOAT_opcode = (char)159;
  /** Opcode identifier for BOOLEAN_CMP_DOUBLE instructions */
  public static final char BOOLEAN_CMP_DOUBLE_opcode = (char)160;
  /** Opcode identifier for BYTE_LOAD instructions */
  public static final char BYTE_LOAD_opcode = (char)161;
  /** Opcode identifier for UBYTE_LOAD instructions */
  public static final char UBYTE_LOAD_opcode = (char)162;
  /** Opcode identifier for SHORT_LOAD instructions */
  public static final char SHORT_LOAD_opcode = (char)163;
  /** Opcode identifier for USHORT_LOAD instructions */
  public static final char USHORT_LOAD_opcode = (char)164;
  /** Opcode identifier for REF_LOAD instructions */
  public static final char REF_LOAD_opcode = (char)165;
  /** Opcode identifier for REF_STORE instructions */
  public static final char REF_STORE_opcode = (char)166;
  /** Opcode identifier for INT_LOAD instructions */
  public static final char INT_LOAD_opcode = (char)167;
  /** Opcode identifier for LONG_LOAD instructions */
  public static final char LONG_LOAD_opcode = (char)168;
  /** Opcode identifier for FLOAT_LOAD instructions */
  public static final char FLOAT_LOAD_opcode = (char)169;
  /** Opcode identifier for DOUBLE_LOAD instructions */
  public static final char DOUBLE_LOAD_opcode = (char)170;
  /** Opcode identifier for BYTE_STORE instructions */
  public static final char BYTE_STORE_opcode = (char)171;
  /** Opcode identifier for SHORT_STORE instructions */
  public static final char SHORT_STORE_opcode = (char)172;
  /** Opcode identifier for INT_STORE instructions */
  public static final char INT_STORE_opcode = (char)173;
  /** Opcode identifier for LONG_STORE instructions */
  public static final char LONG_STORE_opcode = (char)174;
  /** Opcode identifier for FLOAT_STORE instructions */
  public static final char FLOAT_STORE_opcode = (char)175;
  /** Opcode identifier for DOUBLE_STORE instructions */
  public static final char DOUBLE_STORE_opcode = (char)176;
  /** Opcode identifier for PREPARE_INT instructions */
  public static final char PREPARE_INT_opcode = (char)177;
  /** Opcode identifier for PREPARE_ADDR instructions */
  public static final char PREPARE_ADDR_opcode = (char)178;
  /** Opcode identifier for PREPARE_LONG instructions */
  public static final char PREPARE_LONG_opcode = (char)179;
  /** Opcode identifier for ATTEMPT_INT instructions */
  public static final char ATTEMPT_INT_opcode = (char)180;
  /** Opcode identifier for ATTEMPT_ADDR instructions */
  public static final char ATTEMPT_ADDR_opcode = (char)181;
  /** Opcode identifier for ATTEMPT_LONG instructions */
  public static final char ATTEMPT_LONG_opcode = (char)182;
  /** Opcode identifier for CALL instructions */
  public static final char CALL_opcode = (char)183;
  /** Opcode identifier for SYSCALL instructions */
  public static final char SYSCALL_opcode = (char)184;
  /** Opcode identifier for YIELDPOINT_PROLOGUE instructions */
  public static final char YIELDPOINT_PROLOGUE_opcode = (char)185;
  /** Opcode identifier for YIELDPOINT_EPILOGUE instructions */
  public static final char YIELDPOINT_EPILOGUE_opcode = (char)186;
  /** Opcode identifier for YIELDPOINT_BACKEDGE instructions */
  public static final char YIELDPOINT_BACKEDGE_opcode = (char)187;
  /** Opcode identifier for YIELDPOINT_OSR instructions */
  public static final char YIELDPOINT_OSR_opcode = (char)188;
  /** Opcode identifier for OSR_BARRIER instructions */
  public static final char OSR_BARRIER_opcode = (char)189;
  /** Opcode identifier for IR_PROLOGUE instructions */
  public static final char IR_PROLOGUE_opcode = (char)190;
  /** Opcode identifier for RESOLVE instructions */
  public static final char RESOLVE_opcode = (char)191;
  /** Opcode identifier for RESOLVE_MEMBER instructions */
  public static final char RESOLVE_MEMBER_opcode = (char)192;
  /** Opcode identifier for GET_TIME_BASE instructions */
  public static final char GET_TIME_BASE_opcode = (char)193;
  /** Opcode identifier for INSTRUMENTED_EVENT_COUNTER instructions */
  public static final char INSTRUMENTED_EVENT_COUNTER_opcode = (char)194;
  /** Opcode identifier for TRAP_IF instructions */
  public static final char TRAP_IF_opcode = (char)195;
  /** Opcode identifier for TRAP instructions */
  public static final char TRAP_opcode = (char)196;
  /** Opcode identifier for ILLEGAL_INSTRUCTION instructions */
  public static final char ILLEGAL_INSTRUCTION_opcode = (char)197;
  /** Opcode identifier for FLOAT_AS_INT_BITS instructions */
  public static final char FLOAT_AS_INT_BITS_opcode = (char)198;
  /** Opcode identifier for INT_BITS_AS_FLOAT instructions */
  public static final char INT_BITS_AS_FLOAT_opcode = (char)199;
  /** Opcode identifier for DOUBLE_AS_LONG_BITS instructions */
  public static final char DOUBLE_AS_LONG_BITS_opcode = (char)200;
  /** Opcode identifier for LONG_BITS_AS_DOUBLE instructions */
  public static final char LONG_BITS_AS_DOUBLE_opcode = (char)201;
  /** Opcode identifier for ARRAYLENGTH instructions */
  public static final char ARRAYLENGTH_opcode = (char)202;
  /** Opcode identifier for FRAMESIZE instructions */
  public static final char FRAMESIZE_opcode = (char)203;
  /** Opcode identifier for GET_OBJ_TIB instructions */
  public static final char GET_OBJ_TIB_opcode = (char)204;
  /** Opcode identifier for GET_CLASS_TIB instructions */
  public static final char GET_CLASS_TIB_opcode = (char)205;
  /** Opcode identifier for GET_TYPE_FROM_TIB instructions */
  public static final char GET_TYPE_FROM_TIB_opcode = (char)206;
  /** Opcode identifier for GET_SUPERCLASS_IDS_FROM_TIB instructions */
  public static final char GET_SUPERCLASS_IDS_FROM_TIB_opcode = (char)207;
  /** Opcode identifier for GET_DOES_IMPLEMENT_FROM_TIB instructions */
  public static final char GET_DOES_IMPLEMENT_FROM_TIB_opcode = (char)208;
  /** Opcode identifier for GET_ARRAY_ELEMENT_TIB_FROM_TIB instructions */
  public static final char GET_ARRAY_ELEMENT_TIB_FROM_TIB_opcode = (char)209;
  /** Opcode identifier for LOWTABLESWITCH instructions */
  public static final char LOWTABLESWITCH_opcode = (char)210;
  /** Opcode identifier for ADDRESS_CONSTANT instructions */
  public static final char ADDRESS_CONSTANT_opcode = (char)211;
  /** Opcode identifier for INT_CONSTANT instructions */
  public static final char INT_CONSTANT_opcode = (char)212;
  /** Opcode identifier for LONG_CONSTANT instructions */
  public static final char LONG_CONSTANT_opcode = (char)213;
  /** Opcode identifier for REGISTER instructions */
  public static final char REGISTER_opcode = (char)214;
  /** Opcode identifier for OTHER_OPERAND instructions */
  public static final char OTHER_OPERAND_opcode = (char)215;
  /** Opcode identifier for NULL instructions */
  public static final char NULL_opcode = (char)216;
  /** Opcode identifier for BRANCH_TARGET instructions */
  public static final char BRANCH_TARGET_opcode = (char)217;
  /** Opcode identifier for ARCH_INDEPENDENT_END instructions */
  public static final char ARCH_INDEPENDENT_END_opcode = (char)218;

  /* Architecture independent operators */

  /** Operator for GET_CAUGHT_EXCEPTION instructions */
  public static final Operator GET_CAUGHT_EXCEPTION = Operator.lookupOpcode(0);
  /** Operator for SET_CAUGHT_EXCEPTION instructions */
  public static final Operator SET_CAUGHT_EXCEPTION = Operator.lookupOpcode(1);
  /** Operator for NEW instructions */
  public static final Operator NEW = Operator.lookupOpcode(2);
  /** Operator for NEW_UNRESOLVED instructions */
  public static final Operator NEW_UNRESOLVED = Operator.lookupOpcode(3);
  /** Operator for NEWARRAY instructions */
  public static final Operator NEWARRAY = Operator.lookupOpcode(4);
  /** Operator for NEWARRAY_UNRESOLVED instructions */
  public static final Operator NEWARRAY_UNRESOLVED = Operator.lookupOpcode(5);
  /** Operator for ATHROW instructions */
  public static final Operator ATHROW = Operator.lookupOpcode(6);
  /** Operator for CHECKCAST instructions */
  public static final Operator CHECKCAST = Operator.lookupOpcode(7);
  /** Operator for CHECKCAST_NOTNULL instructions */
  public static final Operator CHECKCAST_NOTNULL = Operator.lookupOpcode(8);
  /** Operator for CHECKCAST_UNRESOLVED instructions */
  public static final Operator CHECKCAST_UNRESOLVED = Operator.lookupOpcode(9);
  /** Operator for MUST_IMPLEMENT_INTERFACE instructions */
  public static final Operator MUST_IMPLEMENT_INTERFACE = Operator.lookupOpcode(10);
  /** Operator for INSTANCEOF instructions */
  public static final Operator INSTANCEOF = Operator.lookupOpcode(11);
  /** Operator for INSTANCEOF_NOTNULL instructions */
  public static final Operator INSTANCEOF_NOTNULL = Operator.lookupOpcode(12);
  /** Operator for INSTANCEOF_UNRESOLVED instructions */
  public static final Operator INSTANCEOF_UNRESOLVED = Operator.lookupOpcode(13);
  /** Operator for MONITORENTER instructions */
  public static final Operator MONITORENTER = Operator.lookupOpcode(14);
  /** Operator for MONITOREXIT instructions */
  public static final Operator MONITOREXIT = Operator.lookupOpcode(15);
  /** Operator for NEWOBJMULTIARRAY instructions */
  public static final Operator NEWOBJMULTIARRAY = Operator.lookupOpcode(16);
  /** Operator for GETSTATIC instructions */
  public static final Operator GETSTATIC = Operator.lookupOpcode(17);
  /** Operator for PUTSTATIC instructions */
  public static final Operator PUTSTATIC = Operator.lookupOpcode(18);
  /** Operator for GETFIELD instructions */
  public static final Operator GETFIELD = Operator.lookupOpcode(19);
  /** Operator for PUTFIELD instructions */
  public static final Operator PUTFIELD = Operator.lookupOpcode(20);
  /** Operator for INT_ZERO_CHECK instructions */
  public static final Operator INT_ZERO_CHECK = Operator.lookupOpcode(21);
  /** Operator for LONG_ZERO_CHECK instructions */
  public static final Operator LONG_ZERO_CHECK = Operator.lookupOpcode(22);
  /** Operator for BOUNDS_CHECK instructions */
  public static final Operator BOUNDS_CHECK = Operator.lookupOpcode(23);
  /** Operator for OBJARRAY_STORE_CHECK instructions */
  public static final Operator OBJARRAY_STORE_CHECK = Operator.lookupOpcode(24);
  /** Operator for OBJARRAY_STORE_CHECK_NOTNULL instructions */
  public static final Operator OBJARRAY_STORE_CHECK_NOTNULL = Operator.lookupOpcode(25);
  /** Operator for IG_PATCH_POINT instructions */
  public static final Operator IG_PATCH_POINT = Operator.lookupOpcode(26);
  /** Operator for IG_CLASS_TEST instructions */
  public static final Operator IG_CLASS_TEST = Operator.lookupOpcode(27);
  /** Operator for IG_METHOD_TEST instructions */
  public static final Operator IG_METHOD_TEST = Operator.lookupOpcode(28);
  /** Operator for TABLESWITCH instructions */
  public static final Operator TABLESWITCH = Operator.lookupOpcode(29);
  /** Operator for LOOKUPSWITCH instructions */
  public static final Operator LOOKUPSWITCH = Operator.lookupOpcode(30);
  /** Operator for INT_ALOAD instructions */
  public static final Operator INT_ALOAD = Operator.lookupOpcode(31);
  /** Operator for LONG_ALOAD instructions */
  public static final Operator LONG_ALOAD = Operator.lookupOpcode(32);
  /** Operator for FLOAT_ALOAD instructions */
  public static final Operator FLOAT_ALOAD = Operator.lookupOpcode(33);
  /** Operator for DOUBLE_ALOAD instructions */
  public static final Operator DOUBLE_ALOAD = Operator.lookupOpcode(34);
  /** Operator for REF_ALOAD instructions */
  public static final Operator REF_ALOAD = Operator.lookupOpcode(35);
  /** Operator for UBYTE_ALOAD instructions */
  public static final Operator UBYTE_ALOAD = Operator.lookupOpcode(36);
  /** Operator for BYTE_ALOAD instructions */
  public static final Operator BYTE_ALOAD = Operator.lookupOpcode(37);
  /** Operator for USHORT_ALOAD instructions */
  public static final Operator USHORT_ALOAD = Operator.lookupOpcode(38);
  /** Operator for SHORT_ALOAD instructions */
  public static final Operator SHORT_ALOAD = Operator.lookupOpcode(39);
  /** Operator for INT_ASTORE instructions */
  public static final Operator INT_ASTORE = Operator.lookupOpcode(40);
  /** Operator for LONG_ASTORE instructions */
  public static final Operator LONG_ASTORE = Operator.lookupOpcode(41);
  /** Operator for FLOAT_ASTORE instructions */
  public static final Operator FLOAT_ASTORE = Operator.lookupOpcode(42);
  /** Operator for DOUBLE_ASTORE instructions */
  public static final Operator DOUBLE_ASTORE = Operator.lookupOpcode(43);
  /** Operator for REF_ASTORE instructions */
  public static final Operator REF_ASTORE = Operator.lookupOpcode(44);
  /** Operator for BYTE_ASTORE instructions */
  public static final Operator BYTE_ASTORE = Operator.lookupOpcode(45);
  /** Operator for SHORT_ASTORE instructions */
  public static final Operator SHORT_ASTORE = Operator.lookupOpcode(46);
  /** Operator for INT_IFCMP instructions */
  public static final Operator INT_IFCMP = Operator.lookupOpcode(47);
  /** Operator for INT_IFCMP2 instructions */
  public static final Operator INT_IFCMP2 = Operator.lookupOpcode(48);
  /** Operator for LONG_IFCMP instructions */
  public static final Operator LONG_IFCMP = Operator.lookupOpcode(49);
  /** Operator for FLOAT_IFCMP instructions */
  public static final Operator FLOAT_IFCMP = Operator.lookupOpcode(50);
  /** Operator for DOUBLE_IFCMP instructions */
  public static final Operator DOUBLE_IFCMP = Operator.lookupOpcode(51);
  /** Operator for REF_IFCMP instructions */
  public static final Operator REF_IFCMP = Operator.lookupOpcode(52);
  /** Operator for LABEL instructions */
  public static final Operator LABEL = Operator.lookupOpcode(53);
  /** Operator for BBEND instructions */
  public static final Operator BBEND = Operator.lookupOpcode(54);
  /** Operator for UNINT_BEGIN instructions */
  public static final Operator UNINT_BEGIN = Operator.lookupOpcode(55);
  /** Operator for UNINT_END instructions */
  public static final Operator UNINT_END = Operator.lookupOpcode(56);
  /** Operator for FENCE instructions */
  public static final Operator FENCE = Operator.lookupOpcode(57);
  /** Operator for READ_CEILING instructions */
  public static final Operator READ_CEILING = Operator.lookupOpcode(58);
  /** Operator for WRITE_FLOOR instructions */
  public static final Operator WRITE_FLOOR = Operator.lookupOpcode(59);
  /** Operator for PHI instructions */
  public static final Operator PHI = Operator.lookupOpcode(60);
  /** Operator for SPLIT instructions */
  public static final Operator SPLIT = Operator.lookupOpcode(61);
  /** Operator for PI instructions */
  public static final Operator PI = Operator.lookupOpcode(62);
  /** Operator for NOP instructions */
  public static final Operator NOP = Operator.lookupOpcode(63);
  /** Operator for INT_MOVE instructions */
  public static final Operator INT_MOVE = Operator.lookupOpcode(64);
  /** Operator for LONG_MOVE instructions */
  public static final Operator LONG_MOVE = Operator.lookupOpcode(65);
  /** Operator for FLOAT_MOVE instructions */
  public static final Operator FLOAT_MOVE = Operator.lookupOpcode(66);
  /** Operator for DOUBLE_MOVE instructions */
  public static final Operator DOUBLE_MOVE = Operator.lookupOpcode(67);
  /** Operator for REF_MOVE instructions */
  public static final Operator REF_MOVE = Operator.lookupOpcode(68);
  /** Operator for GUARD_MOVE instructions */
  public static final Operator GUARD_MOVE = Operator.lookupOpcode(69);
  /** Operator for INT_COND_MOVE instructions */
  public static final Operator INT_COND_MOVE = Operator.lookupOpcode(70);
  /** Operator for LONG_COND_MOVE instructions */
  public static final Operator LONG_COND_MOVE = Operator.lookupOpcode(71);
  /** Operator for FLOAT_COND_MOVE instructions */
  public static final Operator FLOAT_COND_MOVE = Operator.lookupOpcode(72);
  /** Operator for DOUBLE_COND_MOVE instructions */
  public static final Operator DOUBLE_COND_MOVE = Operator.lookupOpcode(73);
  /** Operator for REF_COND_MOVE instructions */
  public static final Operator REF_COND_MOVE = Operator.lookupOpcode(74);
  /** Operator for GUARD_COND_MOVE instructions */
  public static final Operator GUARD_COND_MOVE = Operator.lookupOpcode(75);
  /** Operator for GUARD_COMBINE instructions */
  public static final Operator GUARD_COMBINE = Operator.lookupOpcode(76);
  /** Operator for REF_ADD instructions */
  public static final Operator REF_ADD = Operator.lookupOpcode(77);
  /** Operator for INT_ADD instructions */
  public static final Operator INT_ADD = Operator.lookupOpcode(78);
  /** Operator for LONG_ADD instructions */
  public static final Operator LONG_ADD = Operator.lookupOpcode(79);
  /** Operator for FLOAT_ADD instructions */
  public static final Operator FLOAT_ADD = Operator.lookupOpcode(80);
  /** Operator for DOUBLE_ADD instructions */
  public static final Operator DOUBLE_ADD = Operator.lookupOpcode(81);
  /** Operator for REF_SUB instructions */
  public static final Operator REF_SUB = Operator.lookupOpcode(82);
  /** Operator for INT_SUB instructions */
  public static final Operator INT_SUB = Operator.lookupOpcode(83);
  /** Operator for LONG_SUB instructions */
  public static final Operator LONG_SUB = Operator.lookupOpcode(84);
  /** Operator for FLOAT_SUB instructions */
  public static final Operator FLOAT_SUB = Operator.lookupOpcode(85);
  /** Operator for DOUBLE_SUB instructions */
  public static final Operator DOUBLE_SUB = Operator.lookupOpcode(86);
  /** Operator for INT_MUL instructions */
  public static final Operator INT_MUL = Operator.lookupOpcode(87);
  /** Operator for LONG_MUL instructions */
  public static final Operator LONG_MUL = Operator.lookupOpcode(88);
  /** Operator for FLOAT_MUL instructions */
  public static final Operator FLOAT_MUL = Operator.lookupOpcode(89);
  /** Operator for DOUBLE_MUL instructions */
  public static final Operator DOUBLE_MUL = Operator.lookupOpcode(90);
  /** Operator for INT_DIV instructions */
  public static final Operator INT_DIV = Operator.lookupOpcode(91);
  /** Operator for LONG_DIV instructions */
  public static final Operator LONG_DIV = Operator.lookupOpcode(92);
  /** Operator for FLOAT_DIV instructions */
  public static final Operator FLOAT_DIV = Operator.lookupOpcode(93);
  /** Operator for DOUBLE_DIV instructions */
  public static final Operator DOUBLE_DIV = Operator.lookupOpcode(94);
  /** Operator for INT_REM instructions */
  public static final Operator INT_REM = Operator.lookupOpcode(95);
  /** Operator for LONG_REM instructions */
  public static final Operator LONG_REM = Operator.lookupOpcode(96);
  /** Operator for FLOAT_REM instructions */
  public static final Operator FLOAT_REM = Operator.lookupOpcode(97);
  /** Operator for DOUBLE_REM instructions */
  public static final Operator DOUBLE_REM = Operator.lookupOpcode(98);
  /** Operator for REF_NEG instructions */
  public static final Operator REF_NEG = Operator.lookupOpcode(99);
  /** Operator for INT_NEG instructions */
  public static final Operator INT_NEG = Operator.lookupOpcode(100);
  /** Operator for LONG_NEG instructions */
  public static final Operator LONG_NEG = Operator.lookupOpcode(101);
  /** Operator for FLOAT_NEG instructions */
  public static final Operator FLOAT_NEG = Operator.lookupOpcode(102);
  /** Operator for DOUBLE_NEG instructions */
  public static final Operator DOUBLE_NEG = Operator.lookupOpcode(103);
  /** Operator for FLOAT_SQRT instructions */
  public static final Operator FLOAT_SQRT = Operator.lookupOpcode(104);
  /** Operator for DOUBLE_SQRT instructions */
  public static final Operator DOUBLE_SQRT = Operator.lookupOpcode(105);
  /** Operator for REF_SHL instructions */
  public static final Operator REF_SHL = Operator.lookupOpcode(106);
  /** Operator for INT_SHL instructions */
  public static final Operator INT_SHL = Operator.lookupOpcode(107);
  /** Operator for LONG_SHL instructions */
  public static final Operator LONG_SHL = Operator.lookupOpcode(108);
  /** Operator for REF_SHR instructions */
  public static final Operator REF_SHR = Operator.lookupOpcode(109);
  /** Operator for INT_SHR instructions */
  public static final Operator INT_SHR = Operator.lookupOpcode(110);
  /** Operator for LONG_SHR instructions */
  public static final Operator LONG_SHR = Operator.lookupOpcode(111);
  /** Operator for REF_USHR instructions */
  public static final Operator REF_USHR = Operator.lookupOpcode(112);
  /** Operator for INT_USHR instructions */
  public static final Operator INT_USHR = Operator.lookupOpcode(113);
  /** Operator for LONG_USHR instructions */
  public static final Operator LONG_USHR = Operator.lookupOpcode(114);
  /** Operator for REF_AND instructions */
  public static final Operator REF_AND = Operator.lookupOpcode(115);
  /** Operator for INT_AND instructions */
  public static final Operator INT_AND = Operator.lookupOpcode(116);
  /** Operator for LONG_AND instructions */
  public static final Operator LONG_AND = Operator.lookupOpcode(117);
  /** Operator for REF_OR instructions */
  public static final Operator REF_OR = Operator.lookupOpcode(118);
  /** Operator for INT_OR instructions */
  public static final Operator INT_OR = Operator.lookupOpcode(119);
  /** Operator for LONG_OR instructions */
  public static final Operator LONG_OR = Operator.lookupOpcode(120);
  /** Operator for REF_XOR instructions */
  public static final Operator REF_XOR = Operator.lookupOpcode(121);
  /** Operator for INT_XOR instructions */
  public static final Operator INT_XOR = Operator.lookupOpcode(122);
  /** Operator for REF_NOT instructions */
  public static final Operator REF_NOT = Operator.lookupOpcode(123);
  /** Operator for INT_NOT instructions */
  public static final Operator INT_NOT = Operator.lookupOpcode(124);
  /** Operator for LONG_NOT instructions */
  public static final Operator LONG_NOT = Operator.lookupOpcode(125);
  /** Operator for LONG_XOR instructions */
  public static final Operator LONG_XOR = Operator.lookupOpcode(126);
  /** Operator for INT_2ADDRSigExt instructions */
  public static final Operator INT_2ADDRSigExt = Operator.lookupOpcode(127);
  /** Operator for INT_2ADDRZerExt instructions */
  public static final Operator INT_2ADDRZerExt = Operator.lookupOpcode(128);
  /** Operator for LONG_2ADDR instructions */
  public static final Operator LONG_2ADDR = Operator.lookupOpcode(129);
  /** Operator for ADDR_2INT instructions */
  public static final Operator ADDR_2INT = Operator.lookupOpcode(130);
  /** Operator for ADDR_2LONG instructions */
  public static final Operator ADDR_2LONG = Operator.lookupOpcode(131);
  /** Operator for INT_2LONG instructions */
  public static final Operator INT_2LONG = Operator.lookupOpcode(132);
  /** Operator for INT_2FLOAT instructions */
  public static final Operator INT_2FLOAT = Operator.lookupOpcode(133);
  /** Operator for INT_2DOUBLE instructions */
  public static final Operator INT_2DOUBLE = Operator.lookupOpcode(134);
  /** Operator for LONG_2INT instructions */
  public static final Operator LONG_2INT = Operator.lookupOpcode(135);
  /** Operator for LONG_2FLOAT instructions */
  public static final Operator LONG_2FLOAT = Operator.lookupOpcode(136);
  /** Operator for LONG_2DOUBLE instructions */
  public static final Operator LONG_2DOUBLE = Operator.lookupOpcode(137);
  /** Operator for FLOAT_2INT instructions */
  public static final Operator FLOAT_2INT = Operator.lookupOpcode(138);
  /** Operator for FLOAT_2LONG instructions */
  public static final Operator FLOAT_2LONG = Operator.lookupOpcode(139);
  /** Operator for FLOAT_2DOUBLE instructions */
  public static final Operator FLOAT_2DOUBLE = Operator.lookupOpcode(140);
  /** Operator for DOUBLE_2INT instructions */
  public static final Operator DOUBLE_2INT = Operator.lookupOpcode(141);
  /** Operator for DOUBLE_2LONG instructions */
  public static final Operator DOUBLE_2LONG = Operator.lookupOpcode(142);
  /** Operator for DOUBLE_2FLOAT instructions */
  public static final Operator DOUBLE_2FLOAT = Operator.lookupOpcode(143);
  /** Operator for INT_2BYTE instructions */
  public static final Operator INT_2BYTE = Operator.lookupOpcode(144);
  /** Operator for INT_2USHORT instructions */
  public static final Operator INT_2USHORT = Operator.lookupOpcode(145);
  /** Operator for INT_2SHORT instructions */
  public static final Operator INT_2SHORT = Operator.lookupOpcode(146);
  /** Operator for LONG_CMP instructions */
  public static final Operator LONG_CMP = Operator.lookupOpcode(147);
  /** Operator for FLOAT_CMPL instructions */
  public static final Operator FLOAT_CMPL = Operator.lookupOpcode(148);
  /** Operator for FLOAT_CMPG instructions */
  public static final Operator FLOAT_CMPG = Operator.lookupOpcode(149);
  /** Operator for DOUBLE_CMPL instructions */
  public static final Operator DOUBLE_CMPL = Operator.lookupOpcode(150);
  /** Operator for DOUBLE_CMPG instructions */
  public static final Operator DOUBLE_CMPG = Operator.lookupOpcode(151);
  /** Operator for RETURN instructions */
  public static final Operator RETURN = Operator.lookupOpcode(152);
  /** Operator for NULL_CHECK instructions */
  public static final Operator NULL_CHECK = Operator.lookupOpcode(153);
  /** Operator for GOTO instructions */
  public static final Operator GOTO = Operator.lookupOpcode(154);
  /** Operator for BOOLEAN_NOT instructions */
  public static final Operator BOOLEAN_NOT = Operator.lookupOpcode(155);
  /** Operator for BOOLEAN_CMP_INT instructions */
  public static final Operator BOOLEAN_CMP_INT = Operator.lookupOpcode(156);
  /** Operator for BOOLEAN_CMP_ADDR instructions */
  public static final Operator BOOLEAN_CMP_ADDR = Operator.lookupOpcode(157);
  /** Operator for BOOLEAN_CMP_LONG instructions */
  public static final Operator BOOLEAN_CMP_LONG = Operator.lookupOpcode(158);
  /** Operator for BOOLEAN_CMP_FLOAT instructions */
  public static final Operator BOOLEAN_CMP_FLOAT = Operator.lookupOpcode(159);
  /** Operator for BOOLEAN_CMP_DOUBLE instructions */
  public static final Operator BOOLEAN_CMP_DOUBLE = Operator.lookupOpcode(160);
  /** Operator for BYTE_LOAD instructions */
  public static final Operator BYTE_LOAD = Operator.lookupOpcode(161);
  /** Operator for UBYTE_LOAD instructions */
  public static final Operator UBYTE_LOAD = Operator.lookupOpcode(162);
  /** Operator for SHORT_LOAD instructions */
  public static final Operator SHORT_LOAD = Operator.lookupOpcode(163);
  /** Operator for USHORT_LOAD instructions */
  public static final Operator USHORT_LOAD = Operator.lookupOpcode(164);
  /** Operator for REF_LOAD instructions */
  public static final Operator REF_LOAD = Operator.lookupOpcode(165);
  /** Operator for REF_STORE instructions */
  public static final Operator REF_STORE = Operator.lookupOpcode(166);
  /** Operator for INT_LOAD instructions */
  public static final Operator INT_LOAD = Operator.lookupOpcode(167);
  /** Operator for LONG_LOAD instructions */
  public static final Operator LONG_LOAD = Operator.lookupOpcode(168);
  /** Operator for FLOAT_LOAD instructions */
  public static final Operator FLOAT_LOAD = Operator.lookupOpcode(169);
  /** Operator for DOUBLE_LOAD instructions */
  public static final Operator DOUBLE_LOAD = Operator.lookupOpcode(170);
  /** Operator for BYTE_STORE instructions */
  public static final Operator BYTE_STORE = Operator.lookupOpcode(171);
  /** Operator for SHORT_STORE instructions */
  public static final Operator SHORT_STORE = Operator.lookupOpcode(172);
  /** Operator for INT_STORE instructions */
  public static final Operator INT_STORE = Operator.lookupOpcode(173);
  /** Operator for LONG_STORE instructions */
  public static final Operator LONG_STORE = Operator.lookupOpcode(174);
  /** Operator for FLOAT_STORE instructions */
  public static final Operator FLOAT_STORE = Operator.lookupOpcode(175);
  /** Operator for DOUBLE_STORE instructions */
  public static final Operator DOUBLE_STORE = Operator.lookupOpcode(176);
  /** Operator for PREPARE_INT instructions */
  public static final Operator PREPARE_INT = Operator.lookupOpcode(177);
  /** Operator for PREPARE_ADDR instructions */
  public static final Operator PREPARE_ADDR = Operator.lookupOpcode(178);
  /** Operator for PREPARE_LONG instructions */
  public static final Operator PREPARE_LONG = Operator.lookupOpcode(179);
  /** Operator for ATTEMPT_INT instructions */
  public static final Operator ATTEMPT_INT = Operator.lookupOpcode(180);
  /** Operator for ATTEMPT_ADDR instructions */
  public static final Operator ATTEMPT_ADDR = Operator.lookupOpcode(181);
  /** Operator for ATTEMPT_LONG instructions */
  public static final Operator ATTEMPT_LONG = Operator.lookupOpcode(182);
  /** Operator for CALL instructions */
  public static final Operator CALL = Operator.lookupOpcode(183);
  /** Operator for SYSCALL instructions */
  public static final Operator SYSCALL = Operator.lookupOpcode(184);
  /** Operator for YIELDPOINT_PROLOGUE instructions */
  public static final Operator YIELDPOINT_PROLOGUE = Operator.lookupOpcode(185);
  /** Operator for YIELDPOINT_EPILOGUE instructions */
  public static final Operator YIELDPOINT_EPILOGUE = Operator.lookupOpcode(186);
  /** Operator for YIELDPOINT_BACKEDGE instructions */
  public static final Operator YIELDPOINT_BACKEDGE = Operator.lookupOpcode(187);
  /** Operator for YIELDPOINT_OSR instructions */
  public static final Operator YIELDPOINT_OSR = Operator.lookupOpcode(188);
  /** Operator for OSR_BARRIER instructions */
  public static final Operator OSR_BARRIER = Operator.lookupOpcode(189);
  /** Operator for IR_PROLOGUE instructions */
  public static final Operator IR_PROLOGUE = Operator.lookupOpcode(190);
  /** Operator for RESOLVE instructions */
  public static final Operator RESOLVE = Operator.lookupOpcode(191);
  /** Operator for RESOLVE_MEMBER instructions */
  public static final Operator RESOLVE_MEMBER = Operator.lookupOpcode(192);
  /** Operator for GET_TIME_BASE instructions */
  public static final Operator GET_TIME_BASE = Operator.lookupOpcode(193);
  /** Operator for INSTRUMENTED_EVENT_COUNTER instructions */
  public static final Operator INSTRUMENTED_EVENT_COUNTER = Operator.lookupOpcode(194);
  /** Operator for TRAP_IF instructions */
  public static final Operator TRAP_IF = Operator.lookupOpcode(195);
  /** Operator for TRAP instructions */
  public static final Operator TRAP = Operator.lookupOpcode(196);
  /** Operator for ILLEGAL_INSTRUCTION instructions */
  public static final Operator ILLEGAL_INSTRUCTION = Operator.lookupOpcode(197);
  /** Operator for FLOAT_AS_INT_BITS instructions */
  public static final Operator FLOAT_AS_INT_BITS = Operator.lookupOpcode(198);
  /** Operator for INT_BITS_AS_FLOAT instructions */
  public static final Operator INT_BITS_AS_FLOAT = Operator.lookupOpcode(199);
  /** Operator for DOUBLE_AS_LONG_BITS instructions */
  public static final Operator DOUBLE_AS_LONG_BITS = Operator.lookupOpcode(200);
  /** Operator for LONG_BITS_AS_DOUBLE instructions */
  public static final Operator LONG_BITS_AS_DOUBLE = Operator.lookupOpcode(201);
  /** Operator for ARRAYLENGTH instructions */
  public static final Operator ARRAYLENGTH = Operator.lookupOpcode(202);
  /** Operator for FRAMESIZE instructions */
  public static final Operator FRAMESIZE = Operator.lookupOpcode(203);
  /** Operator for GET_OBJ_TIB instructions */
  public static final Operator GET_OBJ_TIB = Operator.lookupOpcode(204);
  /** Operator for GET_CLASS_TIB instructions */
  public static final Operator GET_CLASS_TIB = Operator.lookupOpcode(205);
  /** Operator for GET_TYPE_FROM_TIB instructions */
  public static final Operator GET_TYPE_FROM_TIB = Operator.lookupOpcode(206);
  /** Operator for GET_SUPERCLASS_IDS_FROM_TIB instructions */
  public static final Operator GET_SUPERCLASS_IDS_FROM_TIB = Operator.lookupOpcode(207);
  /** Operator for GET_DOES_IMPLEMENT_FROM_TIB instructions */
  public static final Operator GET_DOES_IMPLEMENT_FROM_TIB = Operator.lookupOpcode(208);
  /** Operator for GET_ARRAY_ELEMENT_TIB_FROM_TIB instructions */
  public static final Operator GET_ARRAY_ELEMENT_TIB_FROM_TIB = Operator.lookupOpcode(209);
  /** Operator for LOWTABLESWITCH instructions */
  public static final Operator LOWTABLESWITCH = Operator.lookupOpcode(210);
  /** Operator for ADDRESS_CONSTANT instructions */
  public static final Operator ADDRESS_CONSTANT = Operator.lookupOpcode(211);
  /** Operator for INT_CONSTANT instructions */
  public static final Operator INT_CONSTANT = Operator.lookupOpcode(212);
  /** Operator for LONG_CONSTANT instructions */
  public static final Operator LONG_CONSTANT = Operator.lookupOpcode(213);
  /** Operator for REGISTER instructions */
  public static final Operator REGISTER = Operator.lookupOpcode(214);
  /** Operator for OTHER_OPERAND instructions */
  public static final Operator OTHER_OPERAND = Operator.lookupOpcode(215);
  /** Operator for NULL instructions */
  public static final Operator NULL = Operator.lookupOpcode(216);
  /** Operator for BRANCH_TARGET instructions */
  public static final Operator BRANCH_TARGET = Operator.lookupOpcode(217);

}
