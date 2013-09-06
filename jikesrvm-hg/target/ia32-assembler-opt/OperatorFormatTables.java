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
import java.util.*;

/**
 * Static tables of all low-level operators understood by the opt
 * compiler.  There is a table of all opcodes, and, for each opcode, a
 * mapping from operator to the instruction format that that operator
 * expects.
 *
 * @see InstructionFormatTables
 */
class OperatorFormatTables {

  /**
   * A mapping for each opcode to its expected instruction format
   */
  static private final Hashtable<String,String> formats = new Hashtable<String,String>();

  /**
   * The set of all low-level operators understood by the opt
   * compiler.
   */
  static private final Set<String> operators = new HashSet<String>();

  /**
   * For a given low-level opcode understood by the opt compiler,
   * return its expected instruction format.
   *
   * @param MIRopcode the opt operator being examined.
   * @return the correponding opt compiler instruction format.
   */
  static public String getFormat(String MIRopcode) {
    return formats.get( MIRopcode );
  }

  /**
   * Access all low-level opt operators the pot compiler understands.
   * @return all low-level opt operators the pot compiler
   * understands.
   */
  static public Iterator<String> getOpcodes() {
    return operators.iterator();
  }

  /**
   * initialize tables for IA32_METHODSTART
   */
  static {
    String key = "IA32_METHODSTART".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FCLEAR
   */
  static {
    String key = "IA32_FCLEAR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryNoRes");
  }

  /**
   * initialize tables for IA32_FMOV_ENDING_LIVE_RANGE
   */
  static {
    String key = "IA32_FMOV_ENDING_LIVE_RANGE".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FMOV
   */
  static {
    String key = "IA32_FMOV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_TRAPIF
   */
  static {
    String key = "IA32_TRAPIF".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_TrapIf");
  }

  /**
   * initialize tables for IA32_OFFSET
   */
  static {
    String key = "IA32_OFFSET".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CaseLabel");
  }

  /**
   * initialize tables for IA32_LOCK_CMPXCHG
   */
  static {
    String key = "IA32_LOCK_CMPXCHG".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CompareExchange");
  }

  /**
   * initialize tables for IA32_LOCK_CMPXCHG8B
   */
  static {
    String key = "IA32_LOCK_CMPXCHG8B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CompareExchange8B");
  }

  /**
   * initialize tables for IA32_ADC
   */
  static {
    String key = "IA32_ADC".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ADD
   */
  static {
    String key = "IA32_ADD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_AND
   */
  static {
    String key = "IA32_AND".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_BSWAP
   */
  static {
    String key = "IA32_BSWAP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryAcc");
  }

  /**
   * initialize tables for IA32_BT
   */
  static {
    String key = "IA32_BT".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Test");
  }

  /**
   * initialize tables for IA32_BTC
   */
  static {
    String key = "IA32_BTC".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Test");
  }

  /**
   * initialize tables for IA32_BTR
   */
  static {
    String key = "IA32_BTR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Test");
  }

  /**
   * initialize tables for IA32_BTS
   */
  static {
    String key = "IA32_BTS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Test");
  }

  /**
   * initialize tables for IA32_SYSCALL
   */
  static {
    String key = "IA32_SYSCALL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Call");
  }

  /**
   * initialize tables for IA32_CALL
   */
  static {
    String key = "IA32_CALL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Call");
  }

  /**
   * initialize tables for IA32_CDQ
   */
  static {
    String key = "IA32_CDQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_ConvertDW2QW");
  }

  /**
   * initialize tables for IA32_CDO
   */
  static {
    String key = "IA32_CDO".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_ConvertDW2QW");
  }

  /**
   * initialize tables for IA32_CDQE
   */
  static {
    String key = "IA32_CDQE".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_ConvertDW2QW");
  }

  /**
   * initialize tables for IA32_CMOV
   */
  static {
    String key = "IA32_CMOV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CondMove");
  }

  /**
   * initialize tables for IA32_CMP
   */
  static {
    String key = "IA32_CMP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_CMPXCHG
   */
  static {
    String key = "IA32_CMPXCHG".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CompareExchange");
  }

  /**
   * initialize tables for IA32_CMPXCHG8B
   */
  static {
    String key = "IA32_CMPXCHG8B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CompareExchange8B");
  }

  /**
   * initialize tables for IA32_DEC
   */
  static {
    String key = "IA32_DEC".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryAcc");
  }

  /**
   * initialize tables for IA32_DIV
   */
  static {
    String key = "IA32_DIV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Divide");
  }

  /**
   * initialize tables for IA32_FADD
   */
  static {
    String key = "IA32_FADD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FADDP
   */
  static {
    String key = "IA32_FADDP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FCHS
   */
  static {
    String key = "IA32_FCHS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryAcc");
  }

  /**
   * initialize tables for IA32_FCMOV
   */
  static {
    String key = "IA32_FCMOV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CondMove");
  }

  /**
   * initialize tables for IA32_FCOMI
   */
  static {
    String key = "IA32_FCOMI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_FCOMIP
   */
  static {
    String key = "IA32_FCOMIP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_FDIV
   */
  static {
    String key = "IA32_FDIV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FDIVP
   */
  static {
    String key = "IA32_FDIVP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FDIVR
   */
  static {
    String key = "IA32_FDIVR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FDIVRP
   */
  static {
    String key = "IA32_FDIVRP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FEXAM
   */
  static {
    String key = "IA32_FEXAM".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryNoRes");
  }

  /**
   * initialize tables for IA32_FXCH
   */
  static {
    String key = "IA32_FXCH".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_XChng");
  }

  /**
   * initialize tables for IA32_FFREE
   */
  static {
    String key = "IA32_FFREE".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FIADD
   */
  static {
    String key = "IA32_FIADD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FIDIV
   */
  static {
    String key = "IA32_FIDIV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FIDIVR
   */
  static {
    String key = "IA32_FIDIVR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FILD
   */
  static {
    String key = "IA32_FILD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FIMUL
   */
  static {
    String key = "IA32_FIMUL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FINIT
   */
  static {
    String key = "IA32_FINIT".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Empty");
  }

  /**
   * initialize tables for IA32_FIST
   */
  static {
    String key = "IA32_FIST".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FISTP
   */
  static {
    String key = "IA32_FISTP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FISUB
   */
  static {
    String key = "IA32_FISUB".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FISUBR
   */
  static {
    String key = "IA32_FISUBR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FLD
   */
  static {
    String key = "IA32_FLD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FLDCW
   */
  static {
    String key = "IA32_FLDCW".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryNoRes");
  }

  /**
   * initialize tables for IA32_FLD1
   */
  static {
    String key = "IA32_FLD1".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FLDL2T
   */
  static {
    String key = "IA32_FLDL2T".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FLDL2E
   */
  static {
    String key = "IA32_FLDL2E".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FLDPI
   */
  static {
    String key = "IA32_FLDPI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FLDLG2
   */
  static {
    String key = "IA32_FLDLG2".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FLDLN2
   */
  static {
    String key = "IA32_FLDLN2".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FLDZ
   */
  static {
    String key = "IA32_FLDZ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_FMUL
   */
  static {
    String key = "IA32_FMUL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FMULP
   */
  static {
    String key = "IA32_FMULP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FNSTCW
   */
  static {
    String key = "IA32_FNSTCW".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryNoRes");
  }

  /**
   * initialize tables for IA32_FNINIT
   */
  static {
    String key = "IA32_FNINIT".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Empty");
  }

  /**
   * initialize tables for IA32_FNSAVE
   */
  static {
    String key = "IA32_FNSAVE".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_FSave");
  }

  /**
   * initialize tables for IA32_FPREM
   */
  static {
    String key = "IA32_FPREM".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FRSTOR
   */
  static {
    String key = "IA32_FRSTOR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_FSave");
  }

  /**
   * initialize tables for IA32_FST
   */
  static {
    String key = "IA32_FST".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FSTCW
   */
  static {
    String key = "IA32_FSTCW".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryNoRes");
  }

  /**
   * initialize tables for IA32_FSTP
   */
  static {
    String key = "IA32_FSTP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_FSUB
   */
  static {
    String key = "IA32_FSUB".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FSUBP
   */
  static {
    String key = "IA32_FSUBP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FSUBR
   */
  static {
    String key = "IA32_FSUBR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FSUBRP
   */
  static {
    String key = "IA32_FSUBRP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_FUCOMI
   */
  static {
    String key = "IA32_FUCOMI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_FUCOMIP
   */
  static {
    String key = "IA32_FUCOMIP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_IDIV
   */
  static {
    String key = "IA32_IDIV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Divide");
  }

  /**
   * initialize tables for IA32_IMUL1
   */
  static {
    String key = "IA32_IMUL1".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Multiply");
  }

  /**
   * initialize tables for IA32_IMUL2
   */
  static {
    String key = "IA32_IMUL2".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_INC
   */
  static {
    String key = "IA32_INC".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryAcc");
  }

  /**
   * initialize tables for IA32_INT
   */
  static {
    String key = "IA32_INT".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Trap");
  }

  /**
   * initialize tables for IA32_JCC
   */
  static {
    String key = "IA32_JCC".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CondBranch");
  }

  /**
   * initialize tables for IA32_JCC2
   */
  static {
    String key = "IA32_JCC2".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CondBranch2");
  }

  /**
   * initialize tables for IA32_JMP
   */
  static {
    String key = "IA32_JMP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Branch");
  }

  /**
   * initialize tables for IA32_LEA
   */
  static {
    String key = "IA32_LEA".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Lea");
  }

  /**
   * initialize tables for IA32_LOCK
   */
  static {
    String key = "IA32_LOCK".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Empty");
  }

  /**
   * initialize tables for IA32_MOV
   */
  static {
    String key = "IA32_MOV".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_MOVZX__B
   */
  static {
    String key = "IA32_MOVZX__B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVSX__B
   */
  static {
    String key = "IA32_MOVSX__B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVZX__W
   */
  static {
    String key = "IA32_MOVZX__W".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVSX__W
   */
  static {
    String key = "IA32_MOVSX__W".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVZXQ__B
   */
  static {
    String key = "IA32_MOVZXQ__B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVSXQ__B
   */
  static {
    String key = "IA32_MOVSXQ__B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVZXQ__W
   */
  static {
    String key = "IA32_MOVZXQ__W".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MOVSXQ__W
   */
  static {
    String key = "IA32_MOVSXQ__W".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_MUL
   */
  static {
    String key = "IA32_MUL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Multiply");
  }

  /**
   * initialize tables for IA32_NEG
   */
  static {
    String key = "IA32_NEG".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryAcc");
  }

  /**
   * initialize tables for IA32_NOT
   */
  static {
    String key = "IA32_NOT".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryAcc");
  }

  /**
   * initialize tables for IA32_OR
   */
  static {
    String key = "IA32_OR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_MFENCE
   */
  static {
    String key = "IA32_MFENCE".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Empty");
  }

  /**
   * initialize tables for IA32_PAUSE
   */
  static {
    String key = "IA32_PAUSE".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Empty");
  }

  /**
   * initialize tables for IA32_PREFETCHNTA
   */
  static {
    String key = "IA32_PREFETCHNTA".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_CacheOp");
  }

  /**
   * initialize tables for IA32_POP
   */
  static {
    String key = "IA32_POP".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Nullary");
  }

  /**
   * initialize tables for IA32_PUSH
   */
  static {
    String key = "IA32_PUSH".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_UnaryNoRes");
  }

  /**
   * initialize tables for IA32_RCL
   */
  static {
    String key = "IA32_RCL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_RCR
   */
  static {
    String key = "IA32_RCR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ROL
   */
  static {
    String key = "IA32_ROL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ROR
   */
  static {
    String key = "IA32_ROR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_RET
   */
  static {
    String key = "IA32_RET".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Return");
  }

  /**
   * initialize tables for IA32_SAL
   */
  static {
    String key = "IA32_SAL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SAR
   */
  static {
    String key = "IA32_SAR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SHL
   */
  static {
    String key = "IA32_SHL".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SHR
   */
  static {
    String key = "IA32_SHR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SBB
   */
  static {
    String key = "IA32_SBB".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SET__B
   */
  static {
    String key = "IA32_SET__B".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Set");
  }

  /**
   * initialize tables for IA32_SHLD
   */
  static {
    String key = "IA32_SHLD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_DoubleShift");
  }

  /**
   * initialize tables for IA32_SHRD
   */
  static {
    String key = "IA32_SHRD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_DoubleShift");
  }

  /**
   * initialize tables for IA32_SUB
   */
  static {
    String key = "IA32_SUB".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_TEST
   */
  static {
    String key = "IA32_TEST".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Test");
  }

  /**
   * initialize tables for IA32_XOR
   */
  static {
    String key = "IA32_XOR".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_RDTSC
   */
  static {
    String key = "IA32_RDTSC".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_RDTSC");
  }

  /**
   * initialize tables for IA32_ADDSS
   */
  static {
    String key = "IA32_ADDSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SUBSS
   */
  static {
    String key = "IA32_SUBSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_MULSS
   */
  static {
    String key = "IA32_MULSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_DIVSS
   */
  static {
    String key = "IA32_DIVSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ADDSD
   */
  static {
    String key = "IA32_ADDSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SUBSD
   */
  static {
    String key = "IA32_SUBSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_MULSD
   */
  static {
    String key = "IA32_MULSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_DIVSD
   */
  static {
    String key = "IA32_DIVSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ANDPS
   */
  static {
    String key = "IA32_ANDPS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ANDPD
   */
  static {
    String key = "IA32_ANDPD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ANDNPS
   */
  static {
    String key = "IA32_ANDNPS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ANDNPD
   */
  static {
    String key = "IA32_ANDNPD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ORPS
   */
  static {
    String key = "IA32_ORPS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_ORPD
   */
  static {
    String key = "IA32_ORPD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_XORPS
   */
  static {
    String key = "IA32_XORPS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_XORPD
   */
  static {
    String key = "IA32_XORPD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_UCOMISS
   */
  static {
    String key = "IA32_UCOMISS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_UCOMISD
   */
  static {
    String key = "IA32_UCOMISD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Compare");
  }

  /**
   * initialize tables for IA32_CMPEQSS
   */
  static {
    String key = "IA32_CMPEQSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPLTSS
   */
  static {
    String key = "IA32_CMPLTSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPLESS
   */
  static {
    String key = "IA32_CMPLESS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPUNORDSS
   */
  static {
    String key = "IA32_CMPUNORDSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPNESS
   */
  static {
    String key = "IA32_CMPNESS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPNLTSS
   */
  static {
    String key = "IA32_CMPNLTSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPNLESS
   */
  static {
    String key = "IA32_CMPNLESS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPORDSS
   */
  static {
    String key = "IA32_CMPORDSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPEQSD
   */
  static {
    String key = "IA32_CMPEQSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPLTSD
   */
  static {
    String key = "IA32_CMPLTSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPLESD
   */
  static {
    String key = "IA32_CMPLESD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPUNORDSD
   */
  static {
    String key = "IA32_CMPUNORDSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPNESD
   */
  static {
    String key = "IA32_CMPNESD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPNLTSD
   */
  static {
    String key = "IA32_CMPNLTSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPNLESD
   */
  static {
    String key = "IA32_CMPNLESD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_CMPORDSD
   */
  static {
    String key = "IA32_CMPORDSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_MOVLPD
   */
  static {
    String key = "IA32_MOVLPD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_MOVLPS
   */
  static {
    String key = "IA32_MOVLPS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_MOVSS
   */
  static {
    String key = "IA32_MOVSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_MOVSD
   */
  static {
    String key = "IA32_MOVSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_MOVD
   */
  static {
    String key = "IA32_MOVD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_MOVQ
   */
  static {
    String key = "IA32_MOVQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Move");
  }

  /**
   * initialize tables for IA32_PSLLQ
   */
  static {
    String key = "IA32_PSLLQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_PSRLQ
   */
  static {
    String key = "IA32_PSRLQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_BinaryAcc");
  }

  /**
   * initialize tables for IA32_SQRTSS
   */
  static {
    String key = "IA32_SQRTSS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_SQRTSD
   */
  static {
    String key = "IA32_SQRTSD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSI2SS
   */
  static {
    String key = "IA32_CVTSI2SS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSS2SD
   */
  static {
    String key = "IA32_CVTSS2SD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSS2SI
   */
  static {
    String key = "IA32_CVTSS2SI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTTSS2SI
   */
  static {
    String key = "IA32_CVTTSS2SI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSI2SD
   */
  static {
    String key = "IA32_CVTSI2SD".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSD2SS
   */
  static {
    String key = "IA32_CVTSD2SS".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSD2SI
   */
  static {
    String key = "IA32_CVTSD2SI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTTSD2SI
   */
  static {
    String key = "IA32_CVTTSD2SI".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSI2SDQ
   */
  static {
    String key = "IA32_CVTSI2SDQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTSD2SIQ
   */
  static {
    String key = "IA32_CVTSD2SIQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

  /**
   * initialize tables for IA32_CVTTSD2SIQ
   */
  static {
    String key = "IA32_CVTTSD2SIQ".substring(5);
    operators.add( key );
    int suffix = key.indexOf( "__" );
    if (suffix != -1) key = key.substring(0, suffix);
    formats.put( key, "MIR_Unary");
  }

}
