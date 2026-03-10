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
package org.jikesrvm.compilers.opt.lir2mir.ppc_32;

import static org.jikesrvm.compilers.opt.ir.Operators.*;
import static org.jikesrvm.compilers.opt.ir.ppc.ArchOperators.*;
import static org.jikesrvm.compilers.opt.lir2mir.ppc_32.BURS_Definitions.*;
import static org.jikesrvm.compilers.opt.ir.IRTools.*;

import org.jikesrvm.*;
import org.jikesrvm.classloader.*;
import org.jikesrvm.compilers.opt.ir.*;
import org.jikesrvm.compilers.opt.ir.ppc.*;
import org.jikesrvm.compilers.opt.ir.operand.*;
import org.jikesrvm.compilers.opt.ir.operand.ppc.*;
import org.jikesrvm.compilers.opt.lir2mir.ppc.BURS_Helpers;
import org.jikesrvm.compilers.opt.lir2mir.ppc_32.BURS_TreeNode;
import org.jikesrvm.compilers.opt.lir2mir.AbstractBURS_TreeNode;
import org.jikesrvm.compilers.opt.lir2mir.BURS;
import org.jikesrvm.compilers.opt.lir2mir.BURS_StateCoder;
import org.jikesrvm.compilers.opt.OptimizingCompilerException;
import org.jikesrvm.runtime.ArchEntrypoints;
import org.jikesrvm.util.Bits; //NOPMD

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Pure;

/**
 * Machine-specific instruction selection rules.  Program generated.
 *
 * Note: some of the functions have been taken and modified
 * from the file gen.c, from the LCC compiler.
 * See $RVM_ROOT/rvm/src-generated/opt-burs/jburg/COPYRIGHT file for copyright restrictions.
 *
 * @see BURS
 *
 * NOTE: Program generated file, do not edit!
 */
@SuppressWarnings("unused") // Machine generated code is hard to get perfect
public class BURS_STATE extends BURS_Helpers implements BURS_StateCoder {

   public BURS_STATE(BURS b) {
      super(b);
   }

/*****************************************************************/
/*                                                               */
/*  BURS TEMPLATE                                                */
/*                                                               */
/*****************************************************************/

   /**
    * Gets the state of a BURS node. This accessor is used by BURS.
    *
    * @param a the node
    *
    * @return the node's state
    */
   private static AbstractBURS_TreeNode STATE(AbstractBURS_TreeNode a) { return a; }

   /***********************************************************************
    *
    *   This file contains BURG utilities
    *
    *   Note: some of the functions have been taken and modified
    *    from the file gen.c, from the LCC compiler.
    *
    ************************************************************************/
   
   /**
    * Prints a debug message. No-op if debugging is disabled.
    *
    * @param p the BURS node
    * @param rule the rule
    * @param cost the rule's cost
    * @param bestcost the best cost seen so far
    */
   private static void trace(AbstractBURS_TreeNode p, int rule, int cost, int bestcost) {
     if (BURS.DEBUG) {
       VM.sysWriteln(p + " matched " + BURS_Debug.string[rule] + " with cost " +
                     cost + " vs. " + bestcost);
     }
   }

   /**
    * Dumps the whole tree starting at the given node. No-op if
    * debugging is disabled.
    *
    * @param p the node to start at
    */
   public static void dumpTree(AbstractBURS_TreeNode p) {
     if (BURS.DEBUG) {
       VM.sysWrite(dumpTree("\n",p,1));
     }
   }

   public static String dumpTree(String out, AbstractBURS_TreeNode p, int indent) {
     if (p == null) return out;
     StringBuilder result = new StringBuilder(out);
     for (int i=0; i<indent; i++)
       result.append("   ");
     result.append(p);
     result.append('\n');
     if (p.getChild1() != null) {
       indent++;
       result.append(dumpTree("",p.getChild1(),indent));
       if (p.getChild2() != null) {
         result.append(dumpTree("",p.getChild2(),indent));
       }
     }
     return result.toString();
   }

   /**
    * Dumps the cover of a tree, i.e. the rules
    * that cover the tree with a minimal cost. No-op if debugging is
    * disabled.
    *
    * @param p the tree's root
    * @param goalnt the non-terminal that is a goal. This is an external rule number
    * @param indent number of spaces to use for indentation
    */
   public static void dumpCover(AbstractBURS_TreeNode p, byte goalnt, int indent){
      if (BURS.DEBUG) {
      if (p == null) return;
      int rule = STATE(p).rule(goalnt);
      VM.sysWrite(STATE(p).getCost(goalnt)+"\t");
      for (int i = 0; i < indent; i++)
        VM.sysWrite(' ');
      VM.sysWrite(BURS_Debug.string[rule]+"\n");
      for (int i = 0; i < nts[rule].length; i++)
        dumpCover(kids(p,rule,i), nts[rule][i], indent + 1);
      }
   }

   // caution: MARK should be used in single threaded mode,
   @Inline
   public static void mark(AbstractBURS_TreeNode p, byte goalnt) {
     if (p == null) return;
     int rule = STATE(p).rule(goalnt);
     byte act = action(rule);
     if ((act & EMIT_INSTRUCTION) != 0) {
       p.setNonTerminal(goalnt);
     }
     if (rule == 0) {
       if (BURS.DEBUG) {
         VM.sysWrite("marking " + p + " with a goalnt of " + goalnt + " failed as the rule " + rule + " has no action");
       }
       throw new OptimizingCompilerException("BURS", "rule missing in ",
         p.getInstructionString(), dumpTree("", p, 1));
     }
     mark_kids(p,rule);
   }
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
//ir.brg

  /**
   * Generate from ir.template and assembled rules files.
   */
  /** Sorted rule number to unsorted rule number map */
  private static int[] unsortedErnMap = {
    0, /* 0 - no rule */
    1, /* 1 - stm: r */
    3, /* 2 - r: czr */
    4, /* 3 - r: rs */
    5, /* 4 - r: rz */
    6, /* 5 - rs: rp */
    7, /* 6 - rz: rp */
    9, /* 7 - any: r */
    2, /* 8 - r: REGISTER */
    8, /* 9 - any: NULL */
    10, /* 10 - any: ADDRESS_CONSTANT */
    11, /* 11 - any: INT_CONSTANT */
    12, /* 12 - any: LONG_CONSTANT */
    14, /* 13 - stm: RESOLVE */
    15, /* 14 - stm: IG_PATCH_POINT */
    16, /* 15 - stm: UNINT_BEGIN */
    17, /* 16 - stm: UNINT_END */
    18, /* 17 - stm: YIELDPOINT_PROLOGUE */
    19, /* 18 - stm: YIELDPOINT_EPILOGUE */
    20, /* 19 - stm: YIELDPOINT_BACKEDGE */
    21, /* 20 - r: FRAMESIZE */
    23, /* 21 - stm: NOP */
    24, /* 22 - r: GUARD_MOVE */
    25, /* 23 - r: GUARD_COMBINE */
    27, /* 24 - r: GET_CAUGHT_EXCEPTION */
    29, /* 25 - stm: FENCE */
    30, /* 26 - stm: WRITE_FLOOR */
    31, /* 27 - stm: READ_CEILING */
    39, /* 28 - stm: ILLEGAL_INSTRUCTION */
    40, /* 29 - stm: TRAP */
    138, /* 30 - rs: REF_MOVE(INT_CONSTANT) */
    139, /* 31 - rs: REF_MOVE(INT_CONSTANT) */
    140, /* 32 - rs: REF_MOVE(INT_CONSTANT) */
    207, /* 33 - stm: GOTO */
    208, /* 34 - stm: RETURN(NULL) */
    213, /* 35 - r: GET_TIME_BASE */
    220, /* 36 - stm: IR_PROLOGUE */
    239, /* 37 - r: LONG_MOVE(LONG_CONSTANT) */
    261, /* 38 - r: REF_MOVE(ADDRESS_CONSTANT) */
    262, /* 39 - r: REF_MOVE(ADDRESS_CONSTANT) */
    263, /* 40 - r: REF_MOVE(ADDRESS_CONSTANT) */
    13, /* 41 - any: OTHER_OPERAND(any,any) */
    41, /* 42 - stm: TRAP_IF(r,r) */
    46, /* 43 - r: BOOLEAN_CMP_INT(r,r) */
    48, /* 44 - boolcmp: BOOLEAN_CMP_INT(r,r) */
    50, /* 45 - r: BOOLEAN_CMP_ADDR(r,r) */
    52, /* 46 - boolcmp: BOOLEAN_CMP_ADDR(r,r) */
    62, /* 47 - r: REF_ADD(r,r) */
    65, /* 48 - r: REF_SUB(r,r) */
    68, /* 49 - r: INT_MUL(r,r) */
    69, /* 50 - r: INT_DIV(r,r) */
    71, /* 51 - r: INT_REM(r,r) */
    75, /* 52 - rz: INT_SHL(r,r) */
    78, /* 53 - rs: INT_SHR(r,r) */
    81, /* 54 - rz: INT_USHR(r,r) */
    85, /* 55 - r: REF_AND(r,r) */
    92, /* 56 - r: REF_OR(r,r) */
    96, /* 57 - r: REF_XOR(r,r) */
    102, /* 58 - r: FLOAT_ADD(r,r) */
    103, /* 59 - r: DOUBLE_ADD(r,r) */
    104, /* 60 - r: FLOAT_MUL(r,r) */
    105, /* 61 - r: DOUBLE_MUL(r,r) */
    106, /* 62 - r: FLOAT_SUB(r,r) */
    107, /* 63 - r: DOUBLE_SUB(r,r) */
    108, /* 64 - r: FLOAT_DIV(r,r) */
    109, /* 65 - r: DOUBLE_DIV(r,r) */
    144, /* 66 - rs: BYTE_LOAD(r,r) */
    148, /* 67 - rp: UBYTE_LOAD(r,r) */
    150, /* 68 - rs: SHORT_LOAD(r,r) */
    152, /* 69 - rp: USHORT_LOAD(r,r) */
    155, /* 70 - r: FLOAT_LOAD(r,r) */
    158, /* 71 - r: DOUBLE_LOAD(r,r) */
    161, /* 72 - rs: INT_LOAD(r,r) */
    184, /* 73 - stm: INT_IFCMP(r,r) */
    199, /* 74 - stm: INT_IFCMP2(r,r) */
    201, /* 75 - stm: FLOAT_IFCMP(r,r) */
    202, /* 76 - stm: DOUBLE_IFCMP(r,r) */
    203, /* 77 - stm: FLOAT_CMPL(r,r) */
    204, /* 78 - stm: FLOAT_CMPG(r,r) */
    205, /* 79 - stm: DOUBLE_CMPL(r,r) */
    206, /* 80 - stm: DOUBLE_CMPG(r,r) */
    210, /* 81 - r: CALL(r,any) */
    212, /* 82 - r: SYSCALL(r,any) */
    214, /* 83 - r: OTHER_OPERAND(r,r) */
    215, /* 84 - r: YIELDPOINT_OSR(any,any) */
    216, /* 85 - r: PREPARE_INT(r,r) */
    217, /* 86 - r: PREPARE_LONG(r,r) */
    218, /* 87 - r: ATTEMPT_INT(r,r) */
    219, /* 88 - r: ATTEMPT_LONG(r,r) */
    221, /* 89 - r: LONG_ADD(r,r) */
    222, /* 90 - r: LONG_SUB(r,r) */
    223, /* 91 - r: LONG_MUL(r,r) */
    225, /* 92 - r: LONG_SHL(r,r) */
    227, /* 93 - r: LONG_SHR(r,r) */
    229, /* 94 - r: LONG_USHR(r,r) */
    231, /* 95 - r: LONG_AND(r,r) */
    232, /* 96 - r: LONG_OR(r,r) */
    233, /* 97 - r: LONG_XOR(r,r) */
    241, /* 98 - stm: LONG_CMP(r,r) */
    242, /* 99 - stm: LONG_IFCMP(r,r) */
    266, /* 100 - r: LONG_LOAD(r,r) */
    270, /* 101 - r: PREPARE_ADDR(r,r) */
    271, /* 102 - r: ATTEMPT_ADDR(r,r) */
    22, /* 103 - stm: LOWTABLESWITCH(r) */
    26, /* 104 - stm: NULL_CHECK(r) */
    28, /* 105 - stm: SET_CAUGHT_EXCEPTION(r) */
    32, /* 106 - stm: DCBF(r) */
    33, /* 107 - stm: DCBST(r) */
    34, /* 108 - stm: DCBT(r) */
    35, /* 109 - stm: DCBTST(r) */
    36, /* 110 - stm: DCBZ(r) */
    37, /* 111 - stm: DCBZL(r) */
    38, /* 112 - stm: ICBI(r) */
    42, /* 113 - stm: TRAP_IF(r,INT_CONSTANT) */
    43, /* 114 - stm: TRAP_IF(r,LONG_CONSTANT) */
    44, /* 115 - r: BOOLEAN_NOT(r) */
    45, /* 116 - r: BOOLEAN_CMP_INT(r,INT_CONSTANT) */
    47, /* 117 - boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT) */
    49, /* 118 - r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT) */
    51, /* 119 - boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT) */
    53, /* 120 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    54, /* 121 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    55, /* 122 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    56, /* 123 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    57, /* 124 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    58, /* 125 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    59, /* 126 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    60, /* 127 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) */
    61, /* 128 - r: REF_ADD(r,INT_CONSTANT) */
    63, /* 129 - r: REF_ADD(r,REF_MOVE(INT_CONSTANT)) */
    64, /* 130 - r: REF_ADD(r,REF_MOVE(INT_CONSTANT)) */
    67, /* 131 - r: INT_MUL(r,INT_CONSTANT) */
    70, /* 132 - r: INT_DIV(r,REF_MOVE(INT_CONSTANT)) */
    72, /* 133 - r: INT_REM(r,REF_MOVE(INT_CONSTANT)) */
    73, /* 134 - r: REF_NEG(r) */
    74, /* 135 - rz: INT_SHL(r,INT_CONSTANT) */
    77, /* 136 - rs: INT_SHR(r,INT_CONSTANT) */
    80, /* 137 - rp: INT_USHR(r,INT_CONSTANT) */
    86, /* 138 - czr: REF_AND(r,INT_CONSTANT) */
    87, /* 139 - rp: REF_AND(r,INT_CONSTANT) */
    93, /* 140 - r: REF_OR(r,INT_CONSTANT) */
    97, /* 141 - r: REF_XOR(r,INT_CONSTANT) */
    98, /* 142 - r: REF_NOT(r) */
    110, /* 143 - r: FLOAT_NEG(r) */
    111, /* 144 - r: DOUBLE_NEG(r) */
    112, /* 145 - r: FLOAT_SQRT(r) */
    113, /* 146 - r: DOUBLE_SQRT(r) */
    126, /* 147 - rs: INT_2BYTE(r) */
    127, /* 148 - rp: INT_2USHORT(r) */
    128, /* 149 - rs: INT_2SHORT(r) */
    129, /* 150 - r: INT_2FLOAT(r) */
    130, /* 151 - r: INT_2DOUBLE(r) */
    131, /* 152 - r: FLOAT_2INT(r) */
    132, /* 153 - r: FLOAT_2DOUBLE(r) */
    133, /* 154 - r: DOUBLE_2INT(r) */
    134, /* 155 - r: DOUBLE_2FLOAT(r) */
    135, /* 156 - r: FLOAT_AS_INT_BITS(r) */
    136, /* 157 - r: INT_BITS_AS_FLOAT(r) */
    137, /* 158 - r: REF_MOVE(r) */
    141, /* 159 - r: FLOAT_MOVE(r) */
    142, /* 160 - r: DOUBLE_MOVE(r) */
    143, /* 161 - rs: BYTE_LOAD(r,INT_CONSTANT) */
    147, /* 162 - rp: UBYTE_LOAD(r,INT_CONSTANT) */
    149, /* 163 - rs: SHORT_LOAD(r,INT_CONSTANT) */
    151, /* 164 - rp: USHORT_LOAD(r,INT_CONSTANT) */
    153, /* 165 - r: FLOAT_LOAD(r,INT_CONSTANT) */
    154, /* 166 - r: FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) */
    156, /* 167 - r: DOUBLE_LOAD(r,INT_CONSTANT) */
    157, /* 168 - r: DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) */
    159, /* 169 - rs: INT_LOAD(r,INT_CONSTANT) */
    160, /* 170 - rs: INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) */
    185, /* 171 - stm: INT_IFCMP(r,INT_CONSTANT) */
    195, /* 172 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) */
    196, /* 173 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) */
    197, /* 174 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) */
    198, /* 175 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) */
    200, /* 176 - stm: INT_IFCMP2(r,INT_CONSTANT) */
    209, /* 177 - stm: RETURN(r) */
    224, /* 178 - r: LONG_NEG(r) */
    226, /* 179 - r: LONG_SHL(r,INT_CONSTANT) */
    228, /* 180 - r: LONG_SHR(r,INT_CONSTANT) */
    230, /* 181 - r: LONG_USHR(r,INT_CONSTANT) */
    234, /* 182 - r: LONG_NOT(r) */
    235, /* 183 - r: INT_2LONG(r) */
    236, /* 184 - r: LONG_2INT(r) */
    237, /* 185 - r: DOUBLE_AS_LONG_BITS(r) */
    238, /* 186 - r: LONG_BITS_AS_DOUBLE(r) */
    240, /* 187 - r: LONG_MOVE(r) */
    256, /* 188 - czr: REF_AND(r,REF_MOVE(INT_CONSTANT)) */
    257, /* 189 - r: REF_AND(r,REF_MOVE(INT_CONSTANT)) */
    258, /* 190 - r: REF_OR(r,REF_MOVE(INT_CONSTANT)) */
    259, /* 191 - r: REF_OR(r,REF_MOVE(INT_CONSTANT)) */
    260, /* 192 - r: REF_XOR(r,REF_MOVE(INT_CONSTANT)) */
    264, /* 193 - r: LONG_LOAD(r,INT_CONSTANT) */
    265, /* 194 - r: LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) */
    66, /* 195 - r: REF_SUB(INT_CONSTANT,r) */
    211, /* 196 - r: CALL(BRANCH_TARGET,any) */
    76, /* 197 - rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT) */
    79, /* 198 - rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT) */
    82, /* 199 - rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT) */
    83, /* 200 - rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT) */
    84, /* 201 - rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT) */
    90, /* 202 - rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT) */
    91, /* 203 - rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT)) */
    146, /* 204 - rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT) */
    163, /* 205 - rs: INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT) */
    186, /* 206 - stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT) */
    187, /* 207 - stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT) */
    191, /* 208 - stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT) */
    192, /* 209 - stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT) */
    193, /* 210 - stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT) */
    194, /* 211 - stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT) */
    245, /* 212 - stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT) */
    246, /* 213 - stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT) */
    251, /* 214 - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT) */
    252, /* 215 - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT) */
    253, /* 216 - stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT) */
    88, /* 217 - r: REF_AND(REF_NOT(r),REF_NOT(r)) */
    94, /* 218 - r: REF_OR(REF_NOT(r),REF_NOT(r)) */
    166, /* 219 - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT)) */
    170, /* 220 - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT)) */
    172, /* 221 - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT)) */
    89, /* 222 - r: REF_AND(r,REF_NOT(r)) */
    95, /* 223 - r: REF_OR(r,REF_NOT(r)) */
    164, /* 224 - stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) */
    168, /* 225 - stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) */
    174, /* 226 - stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) */
    175, /* 227 - stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) */
    178, /* 228 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) */
    179, /* 229 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) */
    181, /* 230 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) */
    182, /* 231 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) */
    267, /* 232 - stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) */
    268, /* 233 - stm: LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) */
    99, /* 234 - r: REF_NOT(REF_OR(r,r)) */
    100, /* 235 - r: REF_NOT(REF_AND(r,r)) */
    101, /* 236 - r: REF_NOT(REF_XOR(r,r)) */
    145, /* 237 - rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT) */
    162, /* 238 - rs: INT_LOAD(REF_ADD(r,r),INT_CONSTANT) */
    188, /* 239 - stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT) */
    189, /* 240 - stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT) */
    190, /* 241 - stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT) */
    243, /* 242 - stm: INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT) */
    244, /* 243 - stm: INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT) */
    247, /* 244 - stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT) */
    248, /* 245 - stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT) */
    249, /* 246 - stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT) */
    250, /* 247 - stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT) */
    114, /* 248 - r: FLOAT_ADD(FLOAT_MUL(r,r),r) */
    115, /* 249 - r: DOUBLE_ADD(DOUBLE_MUL(r,r),r) */
    118, /* 250 - r: FLOAT_SUB(FLOAT_MUL(r,r),r) */
    119, /* 251 - r: DOUBLE_SUB(DOUBLE_MUL(r,r),r) */
    116, /* 252 - r: FLOAT_ADD(r,FLOAT_MUL(r,r)) */
    117, /* 253 - r: DOUBLE_ADD(r,DOUBLE_MUL(r,r)) */
    165, /* 254 - stm: BYTE_STORE(r,OTHER_OPERAND(r,r)) */
    169, /* 255 - stm: SHORT_STORE(r,OTHER_OPERAND(r,r)) */
    176, /* 256 - stm: INT_STORE(r,OTHER_OPERAND(r,r)) */
    180, /* 257 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,r)) */
    183, /* 258 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r)) */
    269, /* 259 - stm: LONG_STORE(r,OTHER_OPERAND(r,r)) */
    120, /* 260 - r: FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r,r),r)) */
    121, /* 261 - r: DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r,r),r)) */
    124, /* 262 - r: FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r,r),r)) */
    125, /* 263 - r: DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r,r),r)) */
    122, /* 264 - r: FLOAT_NEG(FLOAT_ADD(r,FLOAT_MUL(r,r))) */
    123, /* 265 - r: DOUBLE_NEG(DOUBLE_ADD(r,DOUBLE_MUL(r,r))) */
    167, /* 266 - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r)) */
    171, /* 267 - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r)) */
    173, /* 268 - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r)) */
    177, /* 269 - stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)) */
    254, /* 270 - stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT) */
  };

  /** Ragged array for non-terminal leaves of r_NT,  */
  private static final byte[] nts_0 = { r_NT,  };
  /** Ragged array for non-terminal leaves of czr_NT,  */
  private static final byte[] nts_1 = { czr_NT,  };
  /** Ragged array for non-terminal leaves of rs_NT,  */
  private static final byte[] nts_2 = { rs_NT,  };
  /** Ragged array for non-terminal leaves of rz_NT,  */
  private static final byte[] nts_3 = { rz_NT,  };
  /** Ragged array for non-terminal leaves of rp_NT,  */
  private static final byte[] nts_4 = { rp_NT,  };
  /** Ragged array for non-terminal leaves of  */
  private static final byte[] nts_5 = {  };
  /** Ragged array for non-terminal leaves of any_NT, any_NT,  */
  private static final byte[] nts_6 = { any_NT, any_NT,  };
  /** Ragged array for non-terminal leaves of r_NT, r_NT,  */
  private static final byte[] nts_7 = { r_NT, r_NT,  };
  /** Ragged array for non-terminal leaves of r_NT, any_NT,  */
  private static final byte[] nts_8 = { r_NT, any_NT,  };
  /** Ragged array for non-terminal leaves of boolcmp_NT,  */
  private static final byte[] nts_9 = { boolcmp_NT,  };
  /** Ragged array for non-terminal leaves of any_NT,  */
  private static final byte[] nts_10 = { any_NT,  };
  /** Ragged array for non-terminal leaves of r_NT, r_NT, r_NT,  */
  private static final byte[] nts_11 = { r_NT, r_NT, r_NT,  };

  /** Map non-terminal to non-terminal leaves */
  private static final byte[][] nts = {
    null, /* 0 */
    nts_0,  // 1 - stm: r 
    nts_1,  // 2 - r: czr 
    nts_2,  // 3 - r: rs 
    nts_3,  // 4 - r: rz 
    nts_4,  // 5 - rs: rp 
    nts_4,  // 6 - rz: rp 
    nts_0,  // 7 - any: r 
    nts_5,  // 8 - r: REGISTER 
    nts_5,  // 9 - any: NULL 
    nts_5,  // 10 - any: ADDRESS_CONSTANT 
    nts_5,  // 11 - any: INT_CONSTANT 
    nts_5,  // 12 - any: LONG_CONSTANT 
    nts_5,  // 13 - stm: RESOLVE 
    nts_5,  // 14 - stm: IG_PATCH_POINT 
    nts_5,  // 15 - stm: UNINT_BEGIN 
    nts_5,  // 16 - stm: UNINT_END 
    nts_5,  // 17 - stm: YIELDPOINT_PROLOGUE 
    nts_5,  // 18 - stm: YIELDPOINT_EPILOGUE 
    nts_5,  // 19 - stm: YIELDPOINT_BACKEDGE 
    nts_5,  // 20 - r: FRAMESIZE 
    nts_5,  // 21 - stm: NOP 
    nts_5,  // 22 - r: GUARD_MOVE 
    nts_5,  // 23 - r: GUARD_COMBINE 
    nts_5,  // 24 - r: GET_CAUGHT_EXCEPTION 
    nts_5,  // 25 - stm: FENCE 
    nts_5,  // 26 - stm: WRITE_FLOOR 
    nts_5,  // 27 - stm: READ_CEILING 
    nts_5,  // 28 - stm: ILLEGAL_INSTRUCTION 
    nts_5,  // 29 - stm: TRAP 
    nts_5,  // 30 - rs: REF_MOVE(INT_CONSTANT) 
    nts_5,  // 31 - rs: REF_MOVE(INT_CONSTANT) 
    nts_5,  // 32 - rs: REF_MOVE(INT_CONSTANT) 
    nts_5,  // 33 - stm: GOTO 
    nts_5,  // 34 - stm: RETURN(NULL) 
    nts_5,  // 35 - r: GET_TIME_BASE 
    nts_5,  // 36 - stm: IR_PROLOGUE 
    nts_5,  // 37 - r: LONG_MOVE(LONG_CONSTANT) 
    nts_5,  // 38 - r: REF_MOVE(ADDRESS_CONSTANT) 
    nts_5,  // 39 - r: REF_MOVE(ADDRESS_CONSTANT) 
    nts_5,  // 40 - r: REF_MOVE(ADDRESS_CONSTANT) 
    nts_6,  // 41 - any: OTHER_OPERAND(any,any) 
    nts_7,  // 42 - stm: TRAP_IF(r,r) 
    nts_7,  // 43 - r: BOOLEAN_CMP_INT(r,r) 
    nts_7,  // 44 - boolcmp: BOOLEAN_CMP_INT(r,r) 
    nts_7,  // 45 - r: BOOLEAN_CMP_ADDR(r,r) 
    nts_7,  // 46 - boolcmp: BOOLEAN_CMP_ADDR(r,r) 
    nts_7,  // 47 - r: REF_ADD(r,r) 
    nts_7,  // 48 - r: REF_SUB(r,r) 
    nts_7,  // 49 - r: INT_MUL(r,r) 
    nts_7,  // 50 - r: INT_DIV(r,r) 
    nts_7,  // 51 - r: INT_REM(r,r) 
    nts_7,  // 52 - rz: INT_SHL(r,r) 
    nts_7,  // 53 - rs: INT_SHR(r,r) 
    nts_7,  // 54 - rz: INT_USHR(r,r) 
    nts_7,  // 55 - r: REF_AND(r,r) 
    nts_7,  // 56 - r: REF_OR(r,r) 
    nts_7,  // 57 - r: REF_XOR(r,r) 
    nts_7,  // 58 - r: FLOAT_ADD(r,r) 
    nts_7,  // 59 - r: DOUBLE_ADD(r,r) 
    nts_7,  // 60 - r: FLOAT_MUL(r,r) 
    nts_7,  // 61 - r: DOUBLE_MUL(r,r) 
    nts_7,  // 62 - r: FLOAT_SUB(r,r) 
    nts_7,  // 63 - r: DOUBLE_SUB(r,r) 
    nts_7,  // 64 - r: FLOAT_DIV(r,r) 
    nts_7,  // 65 - r: DOUBLE_DIV(r,r) 
    nts_7,  // 66 - rs: BYTE_LOAD(r,r) 
    nts_7,  // 67 - rp: UBYTE_LOAD(r,r) 
    nts_7,  // 68 - rs: SHORT_LOAD(r,r) 
    nts_7,  // 69 - rp: USHORT_LOAD(r,r) 
    nts_7,  // 70 - r: FLOAT_LOAD(r,r) 
    nts_7,  // 71 - r: DOUBLE_LOAD(r,r) 
    nts_7,  // 72 - rs: INT_LOAD(r,r) 
    nts_7,  // 73 - stm: INT_IFCMP(r,r) 
    nts_7,  // 74 - stm: INT_IFCMP2(r,r) 
    nts_7,  // 75 - stm: FLOAT_IFCMP(r,r) 
    nts_7,  // 76 - stm: DOUBLE_IFCMP(r,r) 
    nts_7,  // 77 - stm: FLOAT_CMPL(r,r) 
    nts_7,  // 78 - stm: FLOAT_CMPG(r,r) 
    nts_7,  // 79 - stm: DOUBLE_CMPL(r,r) 
    nts_7,  // 80 - stm: DOUBLE_CMPG(r,r) 
    nts_8,  // 81 - r: CALL(r,any) 
    nts_8,  // 82 - r: SYSCALL(r,any) 
    nts_7,  // 83 - r: OTHER_OPERAND(r,r) 
    nts_6,  // 84 - r: YIELDPOINT_OSR(any,any) 
    nts_7,  // 85 - r: PREPARE_INT(r,r) 
    nts_7,  // 86 - r: PREPARE_LONG(r,r) 
    nts_7,  // 87 - r: ATTEMPT_INT(r,r) 
    nts_7,  // 88 - r: ATTEMPT_LONG(r,r) 
    nts_7,  // 89 - r: LONG_ADD(r,r) 
    nts_7,  // 90 - r: LONG_SUB(r,r) 
    nts_7,  // 91 - r: LONG_MUL(r,r) 
    nts_7,  // 92 - r: LONG_SHL(r,r) 
    nts_7,  // 93 - r: LONG_SHR(r,r) 
    nts_7,  // 94 - r: LONG_USHR(r,r) 
    nts_7,  // 95 - r: LONG_AND(r,r) 
    nts_7,  // 96 - r: LONG_OR(r,r) 
    nts_7,  // 97 - r: LONG_XOR(r,r) 
    nts_7,  // 98 - stm: LONG_CMP(r,r) 
    nts_7,  // 99 - stm: LONG_IFCMP(r,r) 
    nts_7,  // 100 - r: LONG_LOAD(r,r) 
    nts_7,  // 101 - r: PREPARE_ADDR(r,r) 
    nts_7,  // 102 - r: ATTEMPT_ADDR(r,r) 
    nts_0,  // 103 - stm: LOWTABLESWITCH(r) 
    nts_0,  // 104 - stm: NULL_CHECK(r) 
    nts_0,  // 105 - stm: SET_CAUGHT_EXCEPTION(r) 
    nts_0,  // 106 - stm: DCBF(r) 
    nts_0,  // 107 - stm: DCBST(r) 
    nts_0,  // 108 - stm: DCBT(r) 
    nts_0,  // 109 - stm: DCBTST(r) 
    nts_0,  // 110 - stm: DCBZ(r) 
    nts_0,  // 111 - stm: DCBZL(r) 
    nts_0,  // 112 - stm: ICBI(r) 
    nts_0,  // 113 - stm: TRAP_IF(r,INT_CONSTANT) 
    nts_0,  // 114 - stm: TRAP_IF(r,LONG_CONSTANT) 
    nts_0,  // 115 - r: BOOLEAN_NOT(r) 
    nts_0,  // 116 - r: BOOLEAN_CMP_INT(r,INT_CONSTANT) 
    nts_0,  // 117 - boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT) 
    nts_0,  // 118 - r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT) 
    nts_0,  // 119 - boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT) 
    nts_9,  // 120 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 121 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 122 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 123 - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 124 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 125 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 126 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_9,  // 127 - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT) 
    nts_0,  // 128 - r: REF_ADD(r,INT_CONSTANT) 
    nts_0,  // 129 - r: REF_ADD(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 130 - r: REF_ADD(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 131 - r: INT_MUL(r,INT_CONSTANT) 
    nts_0,  // 132 - r: INT_DIV(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 133 - r: INT_REM(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 134 - r: REF_NEG(r) 
    nts_0,  // 135 - rz: INT_SHL(r,INT_CONSTANT) 
    nts_0,  // 136 - rs: INT_SHR(r,INT_CONSTANT) 
    nts_0,  // 137 - rp: INT_USHR(r,INT_CONSTANT) 
    nts_0,  // 138 - czr: REF_AND(r,INT_CONSTANT) 
    nts_0,  // 139 - rp: REF_AND(r,INT_CONSTANT) 
    nts_0,  // 140 - r: REF_OR(r,INT_CONSTANT) 
    nts_0,  // 141 - r: REF_XOR(r,INT_CONSTANT) 
    nts_0,  // 142 - r: REF_NOT(r) 
    nts_0,  // 143 - r: FLOAT_NEG(r) 
    nts_0,  // 144 - r: DOUBLE_NEG(r) 
    nts_0,  // 145 - r: FLOAT_SQRT(r) 
    nts_0,  // 146 - r: DOUBLE_SQRT(r) 
    nts_0,  // 147 - rs: INT_2BYTE(r) 
    nts_0,  // 148 - rp: INT_2USHORT(r) 
    nts_0,  // 149 - rs: INT_2SHORT(r) 
    nts_0,  // 150 - r: INT_2FLOAT(r) 
    nts_0,  // 151 - r: INT_2DOUBLE(r) 
    nts_0,  // 152 - r: FLOAT_2INT(r) 
    nts_0,  // 153 - r: FLOAT_2DOUBLE(r) 
    nts_0,  // 154 - r: DOUBLE_2INT(r) 
    nts_0,  // 155 - r: DOUBLE_2FLOAT(r) 
    nts_0,  // 156 - r: FLOAT_AS_INT_BITS(r) 
    nts_0,  // 157 - r: INT_BITS_AS_FLOAT(r) 
    nts_0,  // 158 - r: REF_MOVE(r) 
    nts_0,  // 159 - r: FLOAT_MOVE(r) 
    nts_0,  // 160 - r: DOUBLE_MOVE(r) 
    nts_0,  // 161 - rs: BYTE_LOAD(r,INT_CONSTANT) 
    nts_0,  // 162 - rp: UBYTE_LOAD(r,INT_CONSTANT) 
    nts_0,  // 163 - rs: SHORT_LOAD(r,INT_CONSTANT) 
    nts_0,  // 164 - rp: USHORT_LOAD(r,INT_CONSTANT) 
    nts_0,  // 165 - r: FLOAT_LOAD(r,INT_CONSTANT) 
    nts_0,  // 166 - r: FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) 
    nts_0,  // 167 - r: DOUBLE_LOAD(r,INT_CONSTANT) 
    nts_0,  // 168 - r: DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) 
    nts_0,  // 169 - rs: INT_LOAD(r,INT_CONSTANT) 
    nts_0,  // 170 - rs: INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) 
    nts_0,  // 171 - stm: INT_IFCMP(r,INT_CONSTANT) 
    nts_9,  // 172 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) 
    nts_9,  // 173 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) 
    nts_9,  // 174 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) 
    nts_9,  // 175 - stm: INT_IFCMP(boolcmp,INT_CONSTANT) 
    nts_0,  // 176 - stm: INT_IFCMP2(r,INT_CONSTANT) 
    nts_0,  // 177 - stm: RETURN(r) 
    nts_0,  // 178 - r: LONG_NEG(r) 
    nts_0,  // 179 - r: LONG_SHL(r,INT_CONSTANT) 
    nts_0,  // 180 - r: LONG_SHR(r,INT_CONSTANT) 
    nts_0,  // 181 - r: LONG_USHR(r,INT_CONSTANT) 
    nts_0,  // 182 - r: LONG_NOT(r) 
    nts_0,  // 183 - r: INT_2LONG(r) 
    nts_0,  // 184 - r: LONG_2INT(r) 
    nts_0,  // 185 - r: DOUBLE_AS_LONG_BITS(r) 
    nts_0,  // 186 - r: LONG_BITS_AS_DOUBLE(r) 
    nts_0,  // 187 - r: LONG_MOVE(r) 
    nts_0,  // 188 - czr: REF_AND(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 189 - r: REF_AND(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 190 - r: REF_OR(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 191 - r: REF_OR(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 192 - r: REF_XOR(r,REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 193 - r: LONG_LOAD(r,INT_CONSTANT) 
    nts_0,  // 194 - r: LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT)) 
    nts_0,  // 195 - r: REF_SUB(INT_CONSTANT,r) 
    nts_10, // 196 - r: CALL(BRANCH_TARGET,any) 
    nts_0,  // 197 - rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 198 - rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 199 - rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 200 - rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT) 
    nts_0,  // 201 - rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 202 - rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 203 - rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT)) 
    nts_0,  // 204 - rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 205 - rs: INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 206 - stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT) 
    nts_0,  // 207 - stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT) 
    nts_0,  // 208 - stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 209 - stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 210 - stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 211 - stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT) 
    nts_0,  // 212 - stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT) 
    nts_0,  // 213 - stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT) 
    nts_0,  // 214 - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT) 
    nts_0,  // 215 - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT) 
    nts_0,  // 216 - stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT) 
    nts_7,  // 217 - r: REF_AND(REF_NOT(r),REF_NOT(r)) 
    nts_7,  // 218 - r: REF_OR(REF_NOT(r),REF_NOT(r)) 
    nts_7,  // 219 - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 220 - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 221 - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 222 - r: REF_AND(r,REF_NOT(r)) 
    nts_7,  // 223 - r: REF_OR(r,REF_NOT(r)) 
    nts_7,  // 224 - stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 225 - stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 226 - stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 227 - stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) 
    nts_7,  // 228 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 229 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) 
    nts_7,  // 230 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 231 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) 
    nts_7,  // 232 - stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT)) 
    nts_7,  // 233 - stm: LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT))) 
    nts_7,  // 234 - r: REF_NOT(REF_OR(r,r)) 
    nts_7,  // 235 - r: REF_NOT(REF_AND(r,r)) 
    nts_7,  // 236 - r: REF_NOT(REF_XOR(r,r)) 
    nts_7,  // 237 - rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT) 
    nts_7,  // 238 - rs: INT_LOAD(REF_ADD(r,r),INT_CONSTANT) 
    nts_7,  // 239 - stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT) 
    nts_7,  // 240 - stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT) 
    nts_7,  // 241 - stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT) 
    nts_7,  // 242 - stm: INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT) 
    nts_7,  // 243 - stm: INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT) 
    nts_7,  // 244 - stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT) 
    nts_7,  // 245 - stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT) 
    nts_7,  // 246 - stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT) 
    nts_7,  // 247 - stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT) 
    nts_11, // 248 - r: FLOAT_ADD(FLOAT_MUL(r,r),r) 
    nts_11, // 249 - r: DOUBLE_ADD(DOUBLE_MUL(r,r),r) 
    nts_11, // 250 - r: FLOAT_SUB(FLOAT_MUL(r,r),r) 
    nts_11, // 251 - r: DOUBLE_SUB(DOUBLE_MUL(r,r),r) 
    nts_11, // 252 - r: FLOAT_ADD(r,FLOAT_MUL(r,r)) 
    nts_11, // 253 - r: DOUBLE_ADD(r,DOUBLE_MUL(r,r)) 
    nts_11, // 254 - stm: BYTE_STORE(r,OTHER_OPERAND(r,r)) 
    nts_11, // 255 - stm: SHORT_STORE(r,OTHER_OPERAND(r,r)) 
    nts_11, // 256 - stm: INT_STORE(r,OTHER_OPERAND(r,r)) 
    nts_11, // 257 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,r)) 
    nts_11, // 258 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r)) 
    nts_11, // 259 - stm: LONG_STORE(r,OTHER_OPERAND(r,r)) 
    nts_11, // 260 - r: FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r,r),r)) 
    nts_11, // 261 - r: DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r,r),r)) 
    nts_11, // 262 - r: FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r,r),r)) 
    nts_11, // 263 - r: DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r,r),r)) 
    nts_11, // 264 - r: FLOAT_NEG(FLOAT_ADD(r,FLOAT_MUL(r,r))) 
    nts_11, // 265 - r: DOUBLE_NEG(DOUBLE_ADD(r,DOUBLE_MUL(r,r))) 
    nts_11, // 266 - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r)) 
    nts_11, // 267 - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r)) 
    nts_11, // 268 - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r)) 
    nts_7,  // 269 - stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)) 
    nts_7,  // 270 - stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT) 
    nts_7,  // 271 - stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT) 
  };

  /* private static final byte arity[] = {
    0,  // 0 - GET_CAUGHT_EXCEPTION
    1,  // 1 - SET_CAUGHT_EXCEPTION
    -1, // 2 - NEW
    -1, // 3 - NEW_UNRESOLVED
    -1, // 4 - NEWARRAY
    -1, // 5 - NEWARRAY_UNRESOLVED
    -1, // 6 - ATHROW
    -1, // 7 - CHECKCAST
    -1, // 8 - CHECKCAST_NOTNULL
    -1, // 9 - CHECKCAST_UNRESOLVED
    -1, // 10 - MUST_IMPLEMENT_INTERFACE
    -1, // 11 - INSTANCEOF
    -1, // 12 - INSTANCEOF_NOTNULL
    -1, // 13 - INSTANCEOF_UNRESOLVED
    -1, // 14 - MONITORENTER
    -1, // 15 - MONITOREXIT
    -1, // 16 - NEWOBJMULTIARRAY
    -1, // 17 - GETSTATIC
    -1, // 18 - PUTSTATIC
    -1, // 19 - GETFIELD
    -1, // 20 - PUTFIELD
    -1, // 21 - INT_ZERO_CHECK
    -1, // 22 - LONG_ZERO_CHECK
    -1, // 23 - BOUNDS_CHECK
    -1, // 24 - OBJARRAY_STORE_CHECK
    -1, // 25 - OBJARRAY_STORE_CHECK_NOTNULL
    0,  // 26 - IG_PATCH_POINT
    -1, // 27 - IG_CLASS_TEST
    -1, // 28 - IG_METHOD_TEST
    -1, // 29 - TABLESWITCH
    -1, // 30 - LOOKUPSWITCH
    -1, // 31 - INT_ALOAD
    -1, // 32 - LONG_ALOAD
    -1, // 33 - FLOAT_ALOAD
    -1, // 34 - DOUBLE_ALOAD
    -1, // 35 - REF_ALOAD
    -1, // 36 - UBYTE_ALOAD
    -1, // 37 - BYTE_ALOAD
    -1, // 38 - USHORT_ALOAD
    -1, // 39 - SHORT_ALOAD
    -1, // 40 - INT_ASTORE
    -1, // 41 - LONG_ASTORE
    -1, // 42 - FLOAT_ASTORE
    -1, // 43 - DOUBLE_ASTORE
    -1, // 44 - REF_ASTORE
    -1, // 45 - BYTE_ASTORE
    -1, // 46 - SHORT_ASTORE
    2,  // 47 - INT_IFCMP
    2,  // 48 - INT_IFCMP2
    2,  // 49 - LONG_IFCMP
    2,  // 50 - FLOAT_IFCMP
    2,  // 51 - DOUBLE_IFCMP
    -1, // 52 - REF_IFCMP
    -1, // 53 - LABEL
    -1, // 54 - BBEND
    0,  // 55 - UNINT_BEGIN
    0,  // 56 - UNINT_END
    0,  // 57 - FENCE
    0,  // 58 - READ_CEILING
    0,  // 59 - WRITE_FLOOR
    -1, // 60 - PHI
    -1, // 61 - SPLIT
    -1, // 62 - PI
    0,  // 63 - NOP
    -1, // 64 - INT_MOVE
    1,  // 65 - LONG_MOVE
    1,  // 66 - FLOAT_MOVE
    1,  // 67 - DOUBLE_MOVE
    1,  // 68 - REF_MOVE
    0,  // 69 - GUARD_MOVE
    -1, // 70 - INT_COND_MOVE
    -1, // 71 - LONG_COND_MOVE
    -1, // 72 - FLOAT_COND_MOVE
    -1, // 73 - DOUBLE_COND_MOVE
    -1, // 74 - REF_COND_MOVE
    -1, // 75 - GUARD_COND_MOVE
    0,  // 76 - GUARD_COMBINE
    2,  // 77 - REF_ADD
    -1, // 78 - INT_ADD
    2,  // 79 - LONG_ADD
    2,  // 80 - FLOAT_ADD
    2,  // 81 - DOUBLE_ADD
    2,  // 82 - REF_SUB
    -1, // 83 - INT_SUB
    2,  // 84 - LONG_SUB
    2,  // 85 - FLOAT_SUB
    2,  // 86 - DOUBLE_SUB
    2,  // 87 - INT_MUL
    2,  // 88 - LONG_MUL
    2,  // 89 - FLOAT_MUL
    2,  // 90 - DOUBLE_MUL
    2,  // 91 - INT_DIV
    -1, // 92 - LONG_DIV
    2,  // 93 - FLOAT_DIV
    2,  // 94 - DOUBLE_DIV
    2,  // 95 - INT_REM
    -1, // 96 - LONG_REM
    -1, // 97 - FLOAT_REM
    -1, // 98 - DOUBLE_REM
    1,  // 99 - REF_NEG
    -1, // 100 - INT_NEG
    1,  // 101 - LONG_NEG
    1,  // 102 - FLOAT_NEG
    1,  // 103 - DOUBLE_NEG
    1,  // 104 - FLOAT_SQRT
    1,  // 105 - DOUBLE_SQRT
    -1, // 106 - REF_SHL
    2,  // 107 - INT_SHL
    2,  // 108 - LONG_SHL
    -1, // 109 - REF_SHR
    2,  // 110 - INT_SHR
    2,  // 111 - LONG_SHR
    -1, // 112 - REF_USHR
    2,  // 113 - INT_USHR
    2,  // 114 - LONG_USHR
    2,  // 115 - REF_AND
    -1, // 116 - INT_AND
    2,  // 117 - LONG_AND
    2,  // 118 - REF_OR
    -1, // 119 - INT_OR
    2,  // 120 - LONG_OR
    2,  // 121 - REF_XOR
    -1, // 122 - INT_XOR
    1,  // 123 - REF_NOT
    -1, // 124 - INT_NOT
    1,  // 125 - LONG_NOT
    2,  // 126 - LONG_XOR
    -1, // 127 - INT_2ADDRSigExt
    -1, // 128 - INT_2ADDRZerExt
    -1, // 129 - LONG_2ADDR
    -1, // 130 - ADDR_2INT
    -1, // 131 - ADDR_2LONG
    1,  // 132 - INT_2LONG
    1,  // 133 - INT_2FLOAT
    1,  // 134 - INT_2DOUBLE
    1,  // 135 - LONG_2INT
    -1, // 136 - LONG_2FLOAT
    -1, // 137 - LONG_2DOUBLE
    1,  // 138 - FLOAT_2INT
    -1, // 139 - FLOAT_2LONG
    1,  // 140 - FLOAT_2DOUBLE
    1,  // 141 - DOUBLE_2INT
    -1, // 142 - DOUBLE_2LONG
    1,  // 143 - DOUBLE_2FLOAT
    1,  // 144 - INT_2BYTE
    1,  // 145 - INT_2USHORT
    1,  // 146 - INT_2SHORT
    2,  // 147 - LONG_CMP
    2,  // 148 - FLOAT_CMPL
    2,  // 149 - FLOAT_CMPG
    2,  // 150 - DOUBLE_CMPL
    2,  // 151 - DOUBLE_CMPG
    1,  // 152 - RETURN
    1,  // 153 - NULL_CHECK
    0,  // 154 - GOTO
    1,  // 155 - BOOLEAN_NOT
    2,  // 156 - BOOLEAN_CMP_INT
    2,  // 157 - BOOLEAN_CMP_ADDR
    -1, // 158 - BOOLEAN_CMP_LONG
    -1, // 159 - BOOLEAN_CMP_FLOAT
    -1, // 160 - BOOLEAN_CMP_DOUBLE
    2,  // 161 - BYTE_LOAD
    2,  // 162 - UBYTE_LOAD
    2,  // 163 - SHORT_LOAD
    2,  // 164 - USHORT_LOAD
    -1, // 165 - REF_LOAD
    -1, // 166 - REF_STORE
    2,  // 167 - INT_LOAD
    2,  // 168 - LONG_LOAD
    2,  // 169 - FLOAT_LOAD
    2,  // 170 - DOUBLE_LOAD
    2,  // 171 - BYTE_STORE
    2,  // 172 - SHORT_STORE
    2,  // 173 - INT_STORE
    2,  // 174 - LONG_STORE
    2,  // 175 - FLOAT_STORE
    2,  // 176 - DOUBLE_STORE
    2,  // 177 - PREPARE_INT
    2,  // 178 - PREPARE_ADDR
    2,  // 179 - PREPARE_LONG
    2,  // 180 - ATTEMPT_INT
    2,  // 181 - ATTEMPT_ADDR
    2,  // 182 - ATTEMPT_LONG
    2,  // 183 - CALL
    2,  // 184 - SYSCALL
    0,  // 185 - YIELDPOINT_PROLOGUE
    0,  // 186 - YIELDPOINT_EPILOGUE
    0,  // 187 - YIELDPOINT_BACKEDGE
    2,  // 188 - YIELDPOINT_OSR
    -1, // 189 - OSR_BARRIER
    0,  // 190 - IR_PROLOGUE
    0,  // 191 - RESOLVE
    -1, // 192 - RESOLVE_MEMBER
    0,  // 193 - GET_TIME_BASE
    -1, // 194 - INSTRUMENTED_EVENT_COUNTER
    2,  // 195 - TRAP_IF
    0,  // 196 - TRAP
    0,  // 197 - ILLEGAL_INSTRUCTION
    1,  // 198 - FLOAT_AS_INT_BITS
    1,  // 199 - INT_BITS_AS_FLOAT
    1,  // 200 - DOUBLE_AS_LONG_BITS
    1,  // 201 - LONG_BITS_AS_DOUBLE
    -1, // 202 - ARRAYLENGTH
    0,  // 203 - FRAMESIZE
    -1, // 204 - GET_OBJ_TIB
    -1, // 205 - GET_CLASS_TIB
    -1, // 206 - GET_TYPE_FROM_TIB
    -1, // 207 - GET_SUPERCLASS_IDS_FROM_TIB
    -1, // 208 - GET_DOES_IMPLEMENT_FROM_TIB
    -1, // 209 - GET_ARRAY_ELEMENT_TIB_FROM_TIB
    1,  // 210 - LOWTABLESWITCH
    0,  // 211 - ADDRESS_CONSTANT
    0,  // 212 - INT_CONSTANT
    0,  // 213 - LONG_CONSTANT
    0,  // 214 - REGISTER
    2,  // 215 - OTHER_OPERAND
    0,  // 216 - NULL
    0,  // 217 - BRANCH_TARGET
    1,  // 218 - DCBF
    1,  // 219 - DCBST
    1,  // 220 - DCBT
    1,  // 221 - DCBTST
    1,  // 222 - DCBZ
    1,  // 223 - DCBZL
    1,  // 224 - ICBI
    -1, // 225 - CALL_SAVE_VOLATILE
    -1, // 226 - MIR_START
    -1, // 227 - MIR_LOWTABLESWITCH
    -1, // 228 - PPC_DATA_INT
    -1, // 229 - PPC_DATA_LABEL
    -1, // 230 - PPC_ADD
    -1, // 231 - PPC_ADDr
    -1, // 232 - PPC_ADDC
    -1, // 233 - PPC_ADDE
    -1, // 234 - PPC_ADDZE
    -1, // 235 - PPC_ADDME
    -1, // 236 - PPC_ADDIC
    -1, // 237 - PPC_ADDICr
    -1, // 238 - PPC_SUBF
    -1, // 239 - PPC_SUBFr
    -1, // 240 - PPC_SUBFC
    -1, // 241 - PPC_SUBFCr
    -1, // 242 - PPC_SUBFIC
    -1, // 243 - PPC_SUBFE
    -1, // 244 - PPC_SUBFZE
    -1, // 245 - PPC_SUBFME
    -1, // 246 - PPC_AND
    -1, // 247 - PPC_ANDr
    -1, // 248 - PPC_ANDIr
    -1, // 249 - PPC_ANDISr
    -1, // 250 - PPC_NAND
    -1, // 251 - PPC_NANDr
    -1, // 252 - PPC_ANDC
    -1, // 253 - PPC_ANDCr
    -1, // 254 - PPC_OR
    -1, // 255 - PPC_ORr
    -1, // 256 - PPC_MOVE
    -1, // 257 - PPC_ORI
    -1, // 258 - PPC_ORIS
    -1, // 259 - PPC_NOR
    -1, // 260 - PPC_NORr
    -1, // 261 - PPC_ORC
    -1, // 262 - PPC_ORCr
    -1, // 263 - PPC_XOR
    -1, // 264 - PPC_XORr
    -1, // 265 - PPC_XORI
    -1, // 266 - PPC_XORIS
    -1, // 267 - PPC_EQV
    -1, // 268 - PPC_EQVr
    -1, // 269 - PPC_NEG
    -1, // 270 - PPC_NEGr
    -1, // 271 - PPC_CNTLZW
    -1, // 272 - PPC_EXTSB
    -1, // 273 - PPC_EXTSBr
    -1, // 274 - PPC_EXTSH
    -1, // 275 - PPC_EXTSHr
    -1, // 276 - PPC_SLW
    -1, // 277 - PPC_SLWr
    -1, // 278 - PPC_SLWI
    -1, // 279 - PPC_SLWIr
    -1, // 280 - PPC_SRW
    -1, // 281 - PPC_SRWr
    -1, // 282 - PPC_SRWI
    -1, // 283 - PPC_SRWIr
    -1, // 284 - PPC_SRAW
    -1, // 285 - PPC_SRAWr
    -1, // 286 - PPC_SRAWI
    -1, // 287 - PPC_SRAWIr
    -1, // 288 - PPC_RLWINM
    -1, // 289 - PPC_RLWINMr
    -1, // 290 - PPC_RLWIMI
    -1, // 291 - PPC_RLWIMIr
    -1, // 292 - PPC_RLWNM
    -1, // 293 - PPC_RLWNMr
    -1, // 294 - PPC_B
    -1, // 295 - PPC_BL
    -1, // 296 - PPC_BL_SYS
    -1, // 297 - PPC_BLR
    -1, // 298 - PPC_BCTR
    -1, // 299 - PPC_BCTRL
    -1, // 300 - PPC_BCTRL_SYS
    -1, // 301 - PPC_BCLR
    -1, // 302 - PPC_BLRL
    -1, // 303 - PPC_BCLRL
    -1, // 304 - PPC_BC
    -1, // 305 - PPC_BCL
    -1, // 306 - PPC_BCOND
    -1, // 307 - PPC_BCOND2
    -1, // 308 - PPC_BCCTR
    -1, // 309 - PPC_BCC
    -1, // 310 - PPC_ADDI
    -1, // 311 - PPC_ADDIS
    -1, // 312 - PPC_LDI
    -1, // 313 - PPC_LDIS
    -1, // 314 - PPC_CMP
    -1, // 315 - PPC_CMPI
    -1, // 316 - PPC_CMPL
    -1, // 317 - PPC_CMPLI
    -1, // 318 - PPC_CRAND
    -1, // 319 - PPC_CRANDC
    -1, // 320 - PPC_CROR
    -1, // 321 - PPC_CRORC
    -1, // 322 - PPC_FMR
    -1, // 323 - PPC_FRSP
    -1, // 324 - PPC_FCTIW
    -1, // 325 - PPC_FCTIWZ
    -1, // 326 - PPC_FADD
    -1, // 327 - PPC_FADDS
    -1, // 328 - PPC_FSQRT
    -1, // 329 - PPC_FSQRTS
    -1, // 330 - PPC_FABS
    -1, // 331 - PPC_FCMPO
    -1, // 332 - PPC_FCMPU
    -1, // 333 - PPC_FDIV
    -1, // 334 - PPC_FDIVS
    -1, // 335 - PPC_DIVW
    -1, // 336 - PPC_DIVWU
    -1, // 337 - PPC_FMUL
    -1, // 338 - PPC_FMULS
    -1, // 339 - PPC_FSEL
    -1, // 340 - PPC_FMADD
    -1, // 341 - PPC_FMADDS
    -1, // 342 - PPC_FMSUB
    -1, // 343 - PPC_FMSUBS
    -1, // 344 - PPC_FNMADD
    -1, // 345 - PPC_FNMADDS
    -1, // 346 - PPC_FNMSUB
    -1, // 347 - PPC_FNMSUBS
    -1, // 348 - PPC_MULLI
    -1, // 349 - PPC_MULLW
    -1, // 350 - PPC_MULHW
    -1, // 351 - PPC_MULHWU
    -1, // 352 - PPC_FNEG
    -1, // 353 - PPC_FSUB
    -1, // 354 - PPC_FSUBS
    -1, // 355 - PPC_LWZ
    -1, // 356 - PPC_LWZU
    -1, // 357 - PPC_LWZUX
    -1, // 358 - PPC_LWZX
    -1, // 359 - PPC_LWARX
    -1, // 360 - PPC_LBZ
    -1, // 361 - PPC_LBZUX
    -1, // 362 - PPC_LBZX
    -1, // 363 - PPC_LHA
    -1, // 364 - PPC_LHAX
    -1, // 365 - PPC_LHZ
    -1, // 366 - PPC_LHZX
    -1, // 367 - PPC_LFD
    -1, // 368 - PPC_LFDX
    -1, // 369 - PPC_LFS
    -1, // 370 - PPC_LFSX
    -1, // 371 - PPC_LMW
    -1, // 372 - PPC_STW
    -1, // 373 - PPC_STWX
    -1, // 374 - PPC_STWCXr
    -1, // 375 - PPC_STWU
    -1, // 376 - PPC_STB
    -1, // 377 - PPC_STBX
    -1, // 378 - PPC_STH
    -1, // 379 - PPC_STHX
    -1, // 380 - PPC_STFD
    -1, // 381 - PPC_STFDX
    -1, // 382 - PPC_STFDU
    -1, // 383 - PPC_STFS
    -1, // 384 - PPC_STFSX
    -1, // 385 - PPC_STFSU
    -1, // 386 - PPC_STMW
    -1, // 387 - PPC_TW
    -1, // 388 - PPC_TWI
    -1, // 389 - PPC_MFSPR
    -1, // 390 - PPC_MTSPR
    -1, // 391 - PPC_MFTB
    -1, // 392 - PPC_MFTBU
    -1, // 393 - PPC_HWSYNC
    -1, // 394 - PPC_SYNC
    -1, // 395 - PPC_ISYNC
    -1, // 396 - PPC_DCBF
    -1, // 397 - PPC_DCBST
    -1, // 398 - PPC_DCBT
    -1, // 399 - PPC_DCBTST
    -1, // 400 - PPC_DCBZ
    -1, // 401 - PPC_DCBZL
    -1, // 402 - PPC_ICBI
    -1, // 403 - PPC64_EXTSW
    -1, // 404 - PPC64_EXTSWr
    -1, // 405 - PPC64_EXTZW
    -1, // 406 - PPC64_RLDICL
    -1, // 407 - PPC64_RLDICR
    -1, // 408 - PPC64_SLD
    -1, // 409 - PPC64_SLDr
    -1, // 410 - PPC64_SLDI
    -1, // 411 - PPC64_SRD
    -1, // 412 - PPC64_SRDr
    -1, // 413 - PPC64_SRAD
    -1, // 414 - PPC64_SRADr
    -1, // 415 - PPC64_SRADI
    -1, // 416 - PPC64_SRADIr
    -1, // 417 - PPC64_SRDI
    -1, // 418 - PPC64_RLDIMI
    -1, // 419 - PPC64_RLDIMIr
    -1, // 420 - PPC64_CMP
    -1, // 421 - PPC64_CMPI
    -1, // 422 - PPC64_CMPL
    -1, // 423 - PPC64_CMPLI
    -1, // 424 - PPC64_FCFID
    -1, // 425 - PPC64_FCTIDZ
    -1, // 426 - PPC64_DIVD
    -1, // 427 - PPC64_MULLD
    -1, // 428 - PPC64_LD
    -1, // 429 - PPC64_LDX
    -1, // 430 - PPC64_STD
    -1, // 431 - PPC64_STDX
    -1, // 432 - PPC64_TD
    -1, // 433 - PPC64_TDI
    -1, // 434 - PPC_CNTLZAddr
    -1, // 435 - PPC_SRAAddrI
    -1, // 436 - PPC_SRAddrI
    -1, // 437 - PPC64_LWA
    -1, // 438 - PPC_LInt
    -1, // 439 - PPC64_LWAX
    -1, // 440 - PPC_LIntX
    -1, // 441 - PPC_LIntUX
    -1, // 442 - PPC_LAddr
    -1, // 443 - PPC_LAddrX
    -1, // 444 - PPC_LAddrU
    -1, // 445 - PPC_LAddrUX
    -1, // 446 - PPC_LAddrARX
    -1, // 447 - PPC_STAddr
    -1, // 448 - PPC_STAddrX
    -1, // 449 - PPC_STAddrU
    -1, // 450 - PPC_STAddrUX
    -1, // 451 - PPC_STAddrCXr
    -1, // 452 - PPC_TAddr
    -1, // 453 - PPC_ILLEGAL_INSTRUCTION
    -1, // 454 - MIR_END
  };*/

  /**
   * Decoding table. Translate the target non-terminal and minimal cost covering state encoding
   * non-terminal into the rule that produces the non-terminal.
   * The first index is the non-terminal that we wish to produce.
   * The second index is the state non-terminal associated with covering a tree
   * with minimal cost and is computed by jburg based on the non-terminal to be produced.
   * The value in the array is the rule number
   */
  private static final char[][] decode = {
    null, // [0][0]
    { // stm_NT
      0, // [1][0]
      1, // [1][1] - stm: r
      13, // [1][2] - stm: RESOLVE
      14, // [1][3] - stm: IG_PATCH_POINT
      15, // [1][4] - stm: UNINT_BEGIN
      16, // [1][5] - stm: UNINT_END
      17, // [1][6] - stm: YIELDPOINT_PROLOGUE
      18, // [1][7] - stm: YIELDPOINT_EPILOGUE
      19, // [1][8] - stm: YIELDPOINT_BACKEDGE
      103, // [1][9] - stm: LOWTABLESWITCH(r)
      21, // [1][10] - stm: NOP
      104, // [1][11] - stm: NULL_CHECK(r)
      105, // [1][12] - stm: SET_CAUGHT_EXCEPTION(r)
      25, // [1][13] - stm: FENCE
      26, // [1][14] - stm: WRITE_FLOOR
      27, // [1][15] - stm: READ_CEILING
      106, // [1][16] - stm: DCBF(r)
      107, // [1][17] - stm: DCBST(r)
      108, // [1][18] - stm: DCBT(r)
      109, // [1][19] - stm: DCBTST(r)
      110, // [1][20] - stm: DCBZ(r)
      111, // [1][21] - stm: DCBZL(r)
      112, // [1][22] - stm: ICBI(r)
      28, // [1][23] - stm: ILLEGAL_INSTRUCTION
      29, // [1][24] - stm: TRAP
      42, // [1][25] - stm: TRAP_IF(r,r)
      113, // [1][26] - stm: TRAP_IF(r,INT_CONSTANT)
      114, // [1][27] - stm: TRAP_IF(r,LONG_CONSTANT)
      224, // [1][28] - stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      254, // [1][29] - stm: BYTE_STORE(r,OTHER_OPERAND(r,r))
      219, // [1][30] - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT))
      266, // [1][31] - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r))
      225, // [1][32] - stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      255, // [1][33] - stm: SHORT_STORE(r,OTHER_OPERAND(r,r))
      220, // [1][34] - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
      267, // [1][35] - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r))
      221, // [1][36] - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
      268, // [1][37] - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r))
      226, // [1][38] - stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      227, // [1][39] - stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      256, // [1][40] - stm: INT_STORE(r,OTHER_OPERAND(r,r))
      269, // [1][41] - stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT))
      228, // [1][42] - stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      229, // [1][43] - stm: FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      257, // [1][44] - stm: FLOAT_STORE(r,OTHER_OPERAND(r,r))
      230, // [1][45] - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      231, // [1][46] - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      258, // [1][47] - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r))
      73, // [1][48] - stm: INT_IFCMP(r,r)
      171, // [1][49] - stm: INT_IFCMP(r,INT_CONSTANT)
      206, // [1][50] - stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT)
      207, // [1][51] - stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT)
      239, // [1][52] - stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT)
      240, // [1][53] - stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT)
      241, // [1][54] - stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT)
      208, // [1][55] - stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      209, // [1][56] - stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
      210, // [1][57] - stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT)
      211, // [1][58] - stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      172, // [1][59] - stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      173, // [1][60] - stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      174, // [1][61] - stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      175, // [1][62] - stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      74, // [1][63] - stm: INT_IFCMP2(r,r)
      176, // [1][64] - stm: INT_IFCMP2(r,INT_CONSTANT)
      75, // [1][65] - stm: FLOAT_IFCMP(r,r)
      76, // [1][66] - stm: DOUBLE_IFCMP(r,r)
      77, // [1][67] - stm: FLOAT_CMPL(r,r)
      78, // [1][68] - stm: FLOAT_CMPG(r,r)
      79, // [1][69] - stm: DOUBLE_CMPL(r,r)
      80, // [1][70] - stm: DOUBLE_CMPG(r,r)
      33, // [1][71] - stm: GOTO
      34, // [1][72] - stm: RETURN(NULL)
      177, // [1][73] - stm: RETURN(r)
      36, // [1][74] - stm: IR_PROLOGUE
      98, // [1][75] - stm: LONG_CMP(r,r)
      99, // [1][76] - stm: LONG_IFCMP(r,r)
      242, // [1][77] - stm: INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT)
      243, // [1][78] - stm: INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT)
      212, // [1][79] - stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT)
      213, // [1][80] - stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT)
      244, // [1][81] - stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT)
      245, // [1][82] - stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT)
      246, // [1][83] - stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT)
      247, // [1][84] - stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT)
      214, // [1][85] - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      215, // [1][86] - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      216, // [1][87] - stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
      270, // [1][88] - stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT)
      271, // [1][89] - stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT)
      232, // [1][90] - stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      233, // [1][91] - stm: LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      259, // [1][92] - stm: LONG_STORE(r,OTHER_OPERAND(r,r))
    },
    { // r_NT
      0, // [2][0]
      8, // [2][1] - r: REGISTER
      2, // [2][2] - r: czr
      3, // [2][3] - r: rs
      4, // [2][4] - r: rz
      20, // [2][5] - r: FRAMESIZE
      22, // [2][6] - r: GUARD_MOVE
      23, // [2][7] - r: GUARD_COMBINE
      24, // [2][8] - r: GET_CAUGHT_EXCEPTION
      115, // [2][9] - r: BOOLEAN_NOT(r)
      116, // [2][10] - r: BOOLEAN_CMP_INT(r,INT_CONSTANT)
      43, // [2][11] - r: BOOLEAN_CMP_INT(r,r)
      118, // [2][12] - r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
      45, // [2][13] - r: BOOLEAN_CMP_ADDR(r,r)
      124, // [2][14] - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      125, // [2][15] - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      126, // [2][16] - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      127, // [2][17] - r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      128, // [2][18] - r: REF_ADD(r,INT_CONSTANT)
      47, // [2][19] - r: REF_ADD(r,r)
      129, // [2][20] - r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
      130, // [2][21] - r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
      48, // [2][22] - r: REF_SUB(r,r)
      195, // [2][23] - r: REF_SUB(INT_CONSTANT,r)
      131, // [2][24] - r: INT_MUL(r,INT_CONSTANT)
      49, // [2][25] - r: INT_MUL(r,r)
      50, // [2][26] - r: INT_DIV(r,r)
      132, // [2][27] - r: INT_DIV(r,REF_MOVE(INT_CONSTANT))
      51, // [2][28] - r: INT_REM(r,r)
      133, // [2][29] - r: INT_REM(r,REF_MOVE(INT_CONSTANT))
      134, // [2][30] - r: REF_NEG(r)
      55, // [2][31] - r: REF_AND(r,r)
      217, // [2][32] - r: REF_AND(REF_NOT(r),REF_NOT(r))
      222, // [2][33] - r: REF_AND(r,REF_NOT(r))
      56, // [2][34] - r: REF_OR(r,r)
      140, // [2][35] - r: REF_OR(r,INT_CONSTANT)
      218, // [2][36] - r: REF_OR(REF_NOT(r),REF_NOT(r))
      223, // [2][37] - r: REF_OR(r,REF_NOT(r))
      57, // [2][38] - r: REF_XOR(r,r)
      141, // [2][39] - r: REF_XOR(r,INT_CONSTANT)
      142, // [2][40] - r: REF_NOT(r)
      234, // [2][41] - r: REF_NOT(REF_OR(r,r))
      235, // [2][42] - r: REF_NOT(REF_AND(r,r))
      236, // [2][43] - r: REF_NOT(REF_XOR(r,r))
      58, // [2][44] - r: FLOAT_ADD(r,r)
      59, // [2][45] - r: DOUBLE_ADD(r,r)
      60, // [2][46] - r: FLOAT_MUL(r,r)
      61, // [2][47] - r: DOUBLE_MUL(r,r)
      62, // [2][48] - r: FLOAT_SUB(r,r)
      63, // [2][49] - r: DOUBLE_SUB(r,r)
      64, // [2][50] - r: FLOAT_DIV(r,r)
      65, // [2][51] - r: DOUBLE_DIV(r,r)
      143, // [2][52] - r: FLOAT_NEG(r)
      144, // [2][53] - r: DOUBLE_NEG(r)
      145, // [2][54] - r: FLOAT_SQRT(r)
      146, // [2][55] - r: DOUBLE_SQRT(r)
      248, // [2][56] - r: FLOAT_ADD(FLOAT_MUL(r,r),r)
      249, // [2][57] - r: DOUBLE_ADD(DOUBLE_MUL(r,r),r)
      252, // [2][58] - r: FLOAT_ADD(r,FLOAT_MUL(r,r))
      253, // [2][59] - r: DOUBLE_ADD(r,DOUBLE_MUL(r,r))
      250, // [2][60] - r: FLOAT_SUB(FLOAT_MUL(r,r),r)
      251, // [2][61] - r: DOUBLE_SUB(DOUBLE_MUL(r,r),r)
      260, // [2][62] - r: FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r,r),r))
      261, // [2][63] - r: DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r,r),r))
      264, // [2][64] - r: FLOAT_NEG(FLOAT_ADD(r,FLOAT_MUL(r,r)))
      265, // [2][65] - r: DOUBLE_NEG(DOUBLE_ADD(r,DOUBLE_MUL(r,r)))
      262, // [2][66] - r: FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r,r),r))
      263, // [2][67] - r: DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r,r),r))
      150, // [2][68] - r: INT_2FLOAT(r)
      151, // [2][69] - r: INT_2DOUBLE(r)
      152, // [2][70] - r: FLOAT_2INT(r)
      153, // [2][71] - r: FLOAT_2DOUBLE(r)
      154, // [2][72] - r: DOUBLE_2INT(r)
      155, // [2][73] - r: DOUBLE_2FLOAT(r)
      156, // [2][74] - r: FLOAT_AS_INT_BITS(r)
      157, // [2][75] - r: INT_BITS_AS_FLOAT(r)
      158, // [2][76] - r: REF_MOVE(r)
      159, // [2][77] - r: FLOAT_MOVE(r)
      160, // [2][78] - r: DOUBLE_MOVE(r)
      165, // [2][79] - r: FLOAT_LOAD(r,INT_CONSTANT)
      166, // [2][80] - r: FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      70, // [2][81] - r: FLOAT_LOAD(r,r)
      167, // [2][82] - r: DOUBLE_LOAD(r,INT_CONSTANT)
      168, // [2][83] - r: DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      71, // [2][84] - r: DOUBLE_LOAD(r,r)
      81, // [2][85] - r: CALL(r,any)
      196, // [2][86] - r: CALL(BRANCH_TARGET,any)
      82, // [2][87] - r: SYSCALL(r,any)
      35, // [2][88] - r: GET_TIME_BASE
      83, // [2][89] - r: OTHER_OPERAND(r,r)
      84, // [2][90] - r: YIELDPOINT_OSR(any,any)
      85, // [2][91] - r: PREPARE_INT(r,r)
      86, // [2][92] - r: PREPARE_LONG(r,r)
      87, // [2][93] - r: ATTEMPT_INT(r,r)
      88, // [2][94] - r: ATTEMPT_LONG(r,r)
      89, // [2][95] - r: LONG_ADD(r,r)
      90, // [2][96] - r: LONG_SUB(r,r)
      91, // [2][97] - r: LONG_MUL(r,r)
      178, // [2][98] - r: LONG_NEG(r)
      92, // [2][99] - r: LONG_SHL(r,r)
      179, // [2][100] - r: LONG_SHL(r,INT_CONSTANT)
      93, // [2][101] - r: LONG_SHR(r,r)
      180, // [2][102] - r: LONG_SHR(r,INT_CONSTANT)
      94, // [2][103] - r: LONG_USHR(r,r)
      181, // [2][104] - r: LONG_USHR(r,INT_CONSTANT)
      95, // [2][105] - r: LONG_AND(r,r)
      96, // [2][106] - r: LONG_OR(r,r)
      97, // [2][107] - r: LONG_XOR(r,r)
      182, // [2][108] - r: LONG_NOT(r)
      183, // [2][109] - r: INT_2LONG(r)
      184, // [2][110] - r: LONG_2INT(r)
      185, // [2][111] - r: DOUBLE_AS_LONG_BITS(r)
      186, // [2][112] - r: LONG_BITS_AS_DOUBLE(r)
      37, // [2][113] - r: LONG_MOVE(LONG_CONSTANT)
      187, // [2][114] - r: LONG_MOVE(r)
      189, // [2][115] - r: REF_AND(r,REF_MOVE(INT_CONSTANT))
      190, // [2][116] - r: REF_OR(r,REF_MOVE(INT_CONSTANT))
      191, // [2][117] - r: REF_OR(r,REF_MOVE(INT_CONSTANT))
      192, // [2][118] - r: REF_XOR(r,REF_MOVE(INT_CONSTANT))
      38, // [2][119] - r: REF_MOVE(ADDRESS_CONSTANT)
      39, // [2][120] - r: REF_MOVE(ADDRESS_CONSTANT)
      40, // [2][121] - r: REF_MOVE(ADDRESS_CONSTANT)
      193, // [2][122] - r: LONG_LOAD(r,INT_CONSTANT)
      194, // [2][123] - r: LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      100, // [2][124] - r: LONG_LOAD(r,r)
      101, // [2][125] - r: PREPARE_ADDR(r,r)
      102, // [2][126] - r: ATTEMPT_ADDR(r,r)
    },
    { // czr_NT
      0, // [3][0]
      138, // [3][1] - czr: REF_AND(r,INT_CONSTANT)
      188, // [3][2] - czr: REF_AND(r,REF_MOVE(INT_CONSTANT))
    },
    { // rs_NT
      0, // [4][0]
      5, // [4][1] - rs: rp
      136, // [4][2] - rs: INT_SHR(r,INT_CONSTANT)
      53, // [4][3] - rs: INT_SHR(r,r)
      147, // [4][4] - rs: INT_2BYTE(r)
      149, // [4][5] - rs: INT_2SHORT(r)
      30, // [4][6] - rs: REF_MOVE(INT_CONSTANT)
      31, // [4][7] - rs: REF_MOVE(INT_CONSTANT)
      32, // [4][8] - rs: REF_MOVE(INT_CONSTANT)
      161, // [4][9] - rs: BYTE_LOAD(r,INT_CONSTANT)
      66, // [4][10] - rs: BYTE_LOAD(r,r)
      163, // [4][11] - rs: SHORT_LOAD(r,INT_CONSTANT)
      68, // [4][12] - rs: SHORT_LOAD(r,r)
      169, // [4][13] - rs: INT_LOAD(r,INT_CONSTANT)
      170, // [4][14] - rs: INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      72, // [4][15] - rs: INT_LOAD(r,r)
      238, // [4][16] - rs: INT_LOAD(REF_ADD(r,r),INT_CONSTANT)
      205, // [4][17] - rs: INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
    },
    { // rz_NT
      0, // [5][0]
      6, // [5][1] - rz: rp
      135, // [5][2] - rz: INT_SHL(r,INT_CONSTANT)
      52, // [5][3] - rz: INT_SHL(r,r)
      197, // [5][4] - rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      54, // [5][5] - rz: INT_USHR(r,r)
    },
    { // rp_NT
      0, // [6][0]
      198, // [6][1] - rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      137, // [6][2] - rp: INT_USHR(r,INT_CONSTANT)
      199, // [6][3] - rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      200, // [6][4] - rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      201, // [6][5] - rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
      139, // [6][6] - rp: REF_AND(r,INT_CONSTANT)
      202, // [6][7] - rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      203, // [6][8] - rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT))
      148, // [6][9] - rp: INT_2USHORT(r)
      237, // [6][10] - rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT)
      204, // [6][11] - rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT)
      162, // [6][12] - rp: UBYTE_LOAD(r,INT_CONSTANT)
      67, // [6][13] - rp: UBYTE_LOAD(r,r)
      164, // [6][14] - rp: USHORT_LOAD(r,INT_CONSTANT)
      69, // [6][15] - rp: USHORT_LOAD(r,r)
    },
    { // any_NT
      0, // [7][0]
      9, // [7][1] - any: NULL
      7, // [7][2] - any: r
      10, // [7][3] - any: ADDRESS_CONSTANT
      11, // [7][4] - any: INT_CONSTANT
      12, // [7][5] - any: LONG_CONSTANT
      41, // [7][6] - any: OTHER_OPERAND(any,any)
    },
    { // boolcmp_NT
      0, // [8][0]
      117, // [8][1] - boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT)
      44, // [8][2] - boolcmp: BOOLEAN_CMP_INT(r,r)
      119, // [8][3] - boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
      46, // [8][4] - boolcmp: BOOLEAN_CMP_ADDR(r,r)
      120, // [8][5] - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      121, // [8][6] - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      122, // [8][7] - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      123, // [8][8] - boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    },
  };

  /**
   * Create closure for r
   * @param p the node
   * @param c the cost
   */
  @Inline
  private static void closure_r(AbstractBURS_TreeNode p, int c) {
    if(BURS.DEBUG) trace(p, 7, c + 0, p.getCost(7) /* any */);
    if (c < p.getCost(7) /* any */) {
      p.setCost(7 /* any */, (char)(c));
      p.writePacked(0, 0x8FFFFFFF, 0x20000000); // p.any = 2
    }
    if(BURS.DEBUG) trace(p, 1, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x1); // p.stm = 1
    }
  }

  /**
   * Create closure for czr
   * @param p the node
   * @param c the cost
   */
  @Inline
  private static void closure_czr(AbstractBURS_TreeNode p, int c) {
    if(BURS.DEBUG) trace(p, 2, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x100); // p.r = 2
      closure_r(p, c);
    }
  }

  /**
   * Create closure for rs
   * @param p the node
   * @param c the cost
   */
  @Inline
  private static void closure_rs(AbstractBURS_TreeNode p, int c) {
    if(BURS.DEBUG) trace(p, 3, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x180); // p.r = 3
      closure_r(p, c);
    }
  }

  /**
   * Create closure for rz
   * @param p the node
   * @param c the cost
   */
  @Inline
  private static void closure_rz(AbstractBURS_TreeNode p, int c) {
    if(BURS.DEBUG) trace(p, 4, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x200); // p.r = 4
      closure_r(p, c);
    }
  }

  /**
   * Create closure for rp
   * @param p the node
   * @param c the cost
   */
  @Inline
  private static void closure_rp(AbstractBURS_TreeNode p, int c) {
    if(BURS.DEBUG) trace(p, 6, c + 0, p.getCost(5) /* rz */);
    if (c < p.getCost(5) /* rz */) {
      p.setCost(5 /* rz */, (char)(c));
      p.writePacked(0, 0xFF1FFFFF, 0x200000); // p.rz = 1
      closure_rz(p, c);
    }
    if(BURS.DEBUG) trace(p, 5, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0x10000); // p.rs = 1
      closure_rs(p, c);
    }
  }

  /**
  /** Recursively labels the tree/
   * @param p node to label
   */
  public static void label(AbstractBURS_TreeNode p) {
    switch (p.getOpcode()) {
    case GET_CAUGHT_EXCEPTION_opcode:
      label_GET_CAUGHT_EXCEPTION(p);
      break;
    case SET_CAUGHT_EXCEPTION_opcode:
      label_SET_CAUGHT_EXCEPTION(p);
      break;
    case IG_PATCH_POINT_opcode:
      label_IG_PATCH_POINT(p);
      break;
    case INT_IFCMP_opcode:
      label_INT_IFCMP(p);
      break;
    case INT_IFCMP2_opcode:
      label_INT_IFCMP2(p);
      break;
    case LONG_IFCMP_opcode:
      label_LONG_IFCMP(p);
      break;
    case FLOAT_IFCMP_opcode:
      label_FLOAT_IFCMP(p);
      break;
    case DOUBLE_IFCMP_opcode:
      label_DOUBLE_IFCMP(p);
      break;
    case UNINT_BEGIN_opcode:
      label_UNINT_BEGIN(p);
      break;
    case UNINT_END_opcode:
      label_UNINT_END(p);
      break;
    case FENCE_opcode:
      label_FENCE(p);
      break;
    case READ_CEILING_opcode:
      label_READ_CEILING(p);
      break;
    case WRITE_FLOOR_opcode:
      label_WRITE_FLOOR(p);
      break;
    case NOP_opcode:
      label_NOP(p);
      break;
    case LONG_MOVE_opcode:
      label_LONG_MOVE(p);
      break;
    case FLOAT_MOVE_opcode:
      label_FLOAT_MOVE(p);
      break;
    case DOUBLE_MOVE_opcode:
      label_DOUBLE_MOVE(p);
      break;
    case REF_MOVE_opcode:
      label_REF_MOVE(p);
      break;
    case GUARD_MOVE_opcode:
      label_GUARD_MOVE(p);
      break;
    case GUARD_COMBINE_opcode:
      label_GUARD_COMBINE(p);
      break;
    case REF_ADD_opcode:
      label_REF_ADD(p);
      break;
    case LONG_ADD_opcode:
      label_LONG_ADD(p);
      break;
    case FLOAT_ADD_opcode:
      label_FLOAT_ADD(p);
      break;
    case DOUBLE_ADD_opcode:
      label_DOUBLE_ADD(p);
      break;
    case REF_SUB_opcode:
      label_REF_SUB(p);
      break;
    case LONG_SUB_opcode:
      label_LONG_SUB(p);
      break;
    case FLOAT_SUB_opcode:
      label_FLOAT_SUB(p);
      break;
    case DOUBLE_SUB_opcode:
      label_DOUBLE_SUB(p);
      break;
    case INT_MUL_opcode:
      label_INT_MUL(p);
      break;
    case LONG_MUL_opcode:
      label_LONG_MUL(p);
      break;
    case FLOAT_MUL_opcode:
      label_FLOAT_MUL(p);
      break;
    case DOUBLE_MUL_opcode:
      label_DOUBLE_MUL(p);
      break;
    case INT_DIV_opcode:
      label_INT_DIV(p);
      break;
    case FLOAT_DIV_opcode:
      label_FLOAT_DIV(p);
      break;
    case DOUBLE_DIV_opcode:
      label_DOUBLE_DIV(p);
      break;
    case INT_REM_opcode:
      label_INT_REM(p);
      break;
    case REF_NEG_opcode:
      label_REF_NEG(p);
      break;
    case LONG_NEG_opcode:
      label_LONG_NEG(p);
      break;
    case FLOAT_NEG_opcode:
      label_FLOAT_NEG(p);
      break;
    case DOUBLE_NEG_opcode:
      label_DOUBLE_NEG(p);
      break;
    case FLOAT_SQRT_opcode:
      label_FLOAT_SQRT(p);
      break;
    case DOUBLE_SQRT_opcode:
      label_DOUBLE_SQRT(p);
      break;
    case INT_SHL_opcode:
      label_INT_SHL(p);
      break;
    case LONG_SHL_opcode:
      label_LONG_SHL(p);
      break;
    case INT_SHR_opcode:
      label_INT_SHR(p);
      break;
    case LONG_SHR_opcode:
      label_LONG_SHR(p);
      break;
    case INT_USHR_opcode:
      label_INT_USHR(p);
      break;
    case LONG_USHR_opcode:
      label_LONG_USHR(p);
      break;
    case REF_AND_opcode:
      label_REF_AND(p);
      break;
    case LONG_AND_opcode:
      label_LONG_AND(p);
      break;
    case REF_OR_opcode:
      label_REF_OR(p);
      break;
    case LONG_OR_opcode:
      label_LONG_OR(p);
      break;
    case REF_XOR_opcode:
      label_REF_XOR(p);
      break;
    case REF_NOT_opcode:
      label_REF_NOT(p);
      break;
    case LONG_NOT_opcode:
      label_LONG_NOT(p);
      break;
    case LONG_XOR_opcode:
      label_LONG_XOR(p);
      break;
    case INT_2LONG_opcode:
      label_INT_2LONG(p);
      break;
    case INT_2FLOAT_opcode:
      label_INT_2FLOAT(p);
      break;
    case INT_2DOUBLE_opcode:
      label_INT_2DOUBLE(p);
      break;
    case LONG_2INT_opcode:
      label_LONG_2INT(p);
      break;
    case FLOAT_2INT_opcode:
      label_FLOAT_2INT(p);
      break;
    case FLOAT_2DOUBLE_opcode:
      label_FLOAT_2DOUBLE(p);
      break;
    case DOUBLE_2INT_opcode:
      label_DOUBLE_2INT(p);
      break;
    case DOUBLE_2FLOAT_opcode:
      label_DOUBLE_2FLOAT(p);
      break;
    case INT_2BYTE_opcode:
      label_INT_2BYTE(p);
      break;
    case INT_2USHORT_opcode:
      label_INT_2USHORT(p);
      break;
    case INT_2SHORT_opcode:
      label_INT_2SHORT(p);
      break;
    case LONG_CMP_opcode:
      label_LONG_CMP(p);
      break;
    case FLOAT_CMPL_opcode:
      label_FLOAT_CMPL(p);
      break;
    case FLOAT_CMPG_opcode:
      label_FLOAT_CMPG(p);
      break;
    case DOUBLE_CMPL_opcode:
      label_DOUBLE_CMPL(p);
      break;
    case DOUBLE_CMPG_opcode:
      label_DOUBLE_CMPG(p);
      break;
    case RETURN_opcode:
      label_RETURN(p);
      break;
    case NULL_CHECK_opcode:
      label_NULL_CHECK(p);
      break;
    case GOTO_opcode:
      label_GOTO(p);
      break;
    case BOOLEAN_NOT_opcode:
      label_BOOLEAN_NOT(p);
      break;
    case BOOLEAN_CMP_INT_opcode:
      label_BOOLEAN_CMP_INT(p);
      break;
    case BOOLEAN_CMP_ADDR_opcode:
      label_BOOLEAN_CMP_ADDR(p);
      break;
    case BYTE_LOAD_opcode:
      label_BYTE_LOAD(p);
      break;
    case UBYTE_LOAD_opcode:
      label_UBYTE_LOAD(p);
      break;
    case SHORT_LOAD_opcode:
      label_SHORT_LOAD(p);
      break;
    case USHORT_LOAD_opcode:
      label_USHORT_LOAD(p);
      break;
    case INT_LOAD_opcode:
      label_INT_LOAD(p);
      break;
    case LONG_LOAD_opcode:
      label_LONG_LOAD(p);
      break;
    case FLOAT_LOAD_opcode:
      label_FLOAT_LOAD(p);
      break;
    case DOUBLE_LOAD_opcode:
      label_DOUBLE_LOAD(p);
      break;
    case BYTE_STORE_opcode:
      label_BYTE_STORE(p);
      break;
    case SHORT_STORE_opcode:
      label_SHORT_STORE(p);
      break;
    case INT_STORE_opcode:
      label_INT_STORE(p);
      break;
    case LONG_STORE_opcode:
      label_LONG_STORE(p);
      break;
    case FLOAT_STORE_opcode:
      label_FLOAT_STORE(p);
      break;
    case DOUBLE_STORE_opcode:
      label_DOUBLE_STORE(p);
      break;
    case PREPARE_INT_opcode:
      label_PREPARE_INT(p);
      break;
    case PREPARE_ADDR_opcode:
      label_PREPARE_ADDR(p);
      break;
    case PREPARE_LONG_opcode:
      label_PREPARE_LONG(p);
      break;
    case ATTEMPT_INT_opcode:
      label_ATTEMPT_INT(p);
      break;
    case ATTEMPT_ADDR_opcode:
      label_ATTEMPT_ADDR(p);
      break;
    case ATTEMPT_LONG_opcode:
      label_ATTEMPT_LONG(p);
      break;
    case CALL_opcode:
      label_CALL(p);
      break;
    case SYSCALL_opcode:
      label_SYSCALL(p);
      break;
    case YIELDPOINT_PROLOGUE_opcode:
      label_YIELDPOINT_PROLOGUE(p);
      break;
    case YIELDPOINT_EPILOGUE_opcode:
      label_YIELDPOINT_EPILOGUE(p);
      break;
    case YIELDPOINT_BACKEDGE_opcode:
      label_YIELDPOINT_BACKEDGE(p);
      break;
    case YIELDPOINT_OSR_opcode:
      label_YIELDPOINT_OSR(p);
      break;
    case IR_PROLOGUE_opcode:
      label_IR_PROLOGUE(p);
      break;
    case RESOLVE_opcode:
      label_RESOLVE(p);
      break;
    case GET_TIME_BASE_opcode:
      label_GET_TIME_BASE(p);
      break;
    case TRAP_IF_opcode:
      label_TRAP_IF(p);
      break;
    case TRAP_opcode:
      label_TRAP(p);
      break;
    case ILLEGAL_INSTRUCTION_opcode:
      label_ILLEGAL_INSTRUCTION(p);
      break;
    case FLOAT_AS_INT_BITS_opcode:
      label_FLOAT_AS_INT_BITS(p);
      break;
    case INT_BITS_AS_FLOAT_opcode:
      label_INT_BITS_AS_FLOAT(p);
      break;
    case DOUBLE_AS_LONG_BITS_opcode:
      label_DOUBLE_AS_LONG_BITS(p);
      break;
    case LONG_BITS_AS_DOUBLE_opcode:
      label_LONG_BITS_AS_DOUBLE(p);
      break;
    case FRAMESIZE_opcode:
      label_FRAMESIZE(p);
      break;
    case LOWTABLESWITCH_opcode:
      label_LOWTABLESWITCH(p);
      break;
    case ADDRESS_CONSTANT_opcode:
      label_ADDRESS_CONSTANT(p);
      break;
    case INT_CONSTANT_opcode:
      label_INT_CONSTANT(p);
      break;
    case LONG_CONSTANT_opcode:
      label_LONG_CONSTANT(p);
      break;
    case REGISTER_opcode:
      label_REGISTER(p);
      break;
    case OTHER_OPERAND_opcode:
      label_OTHER_OPERAND(p);
      break;
    case NULL_opcode:
      label_NULL(p);
      break;
    case BRANCH_TARGET_opcode:
      label_BRANCH_TARGET(p);
      break;
    case DCBF_opcode:
      label_DCBF(p);
      break;
    case DCBST_opcode:
      label_DCBST(p);
      break;
    case DCBT_opcode:
      label_DCBT(p);
      break;
    case DCBTST_opcode:
      label_DCBTST(p);
      break;
    case DCBZ_opcode:
      label_DCBZ(p);
      break;
    case DCBZL_opcode:
      label_DCBZL(p);
      break;
    case ICBI_opcode:
      label_ICBI(p);
      break;
    default:
      throw new OptimizingCompilerException("BURS","terminal not in grammar:",
        p.toString());
    }
  }

  /**
   * Labels GET_CAUGHT_EXCEPTION tree node
   * @param p node to label
   */
  private static void label_GET_CAUGHT_EXCEPTION(AbstractBURS_TreeNode p) {
    p.initCost();
    // r: GET_CAUGHT_EXCEPTION
    if(BURS.DEBUG) trace(p, 24, 11 + 0, p.getCost(2) /* r */);
    if (11 < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(11));
      p.writePacked(0, 0xFFFFC07F, 0x400); // p.r = 8
      closure_r(p, 11);
    }
  }

  /**
   * Labels SET_CAUGHT_EXCEPTION tree node
   * @param p node to label
   */
  private static void label_SET_CAUGHT_EXCEPTION(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: SET_CAUGHT_EXCEPTION(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 105, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0xC); // p.stm = 12
    }
  }

  /**
   * Labels IG_PATCH_POINT tree node
   * @param p node to label
   */
  private static void label_IG_PATCH_POINT(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: IG_PATCH_POINT
    if(BURS.DEBUG) trace(p, 14, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x3); // p.stm = 3
    }
  }

  /**
   * Labels INT_IFCMP tree node
   * @param p node to label
   */
  private static void label_INT_IFCMP(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: INT_IFCMP(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 73, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x30); // p.stm = 48
    }
    if ( // stm: INT_IFCMP(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 171, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x31); // p.stm = 49
      }
    }
    if ( // stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT)
      lchild.getOpcode() == INT_2BYTE_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 206, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x32); // p.stm = 50
      }
    }
    if ( // stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT)
      lchild.getOpcode() == INT_2SHORT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 207, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x33); // p.stm = 51
      }
    }
    if ( // stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT)
      lchild.getOpcode() == INT_USHR_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 239, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x34); // p.stm = 52
      }
    }
    if ( // stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT)
      lchild.getOpcode() == INT_SHL_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 240, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x35); // p.stm = 53
      }
    }
    if ( // stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT)
      lchild.getOpcode() == INT_SHR_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 241, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x36); // p.stm = 54
      }
    }
    if ( // stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == INT_USHR_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 208, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x37); // p.stm = 55
      }
    }
    if ( // stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == INT_SHL_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 209, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x38); // p.stm = 56
      }
    }
    if ( // stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == INT_SHR_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 210, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x39); // p.stm = 57
      }
    }
    if ( // stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 211, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x3A); // p.stm = 58
      }
    }
    if ( // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 0 && IfCmp.getCond(P(p)).isNOT_EQUAL()?20:INFINITE);
      if(BURS.DEBUG) trace(p, 172, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x3B); // p.stm = 59
      }
    }
    if ( // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 0 && IfCmp.getCond(P(p)).isEQUAL()?20:INFINITE);
      if(BURS.DEBUG) trace(p, 173, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x3C); // p.stm = 60
      }
    }
    if ( // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 1 && IfCmp.getCond(P(p)).isEQUAL()?20:INFINITE);
      if(BURS.DEBUG) trace(p, 174, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x3D); // p.stm = 61
      }
    }
    if ( // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 1 && (IfCmp.getCond(P(p)).isNOT_EQUAL())?26:INFINITE);
      if(BURS.DEBUG) trace(p, 175, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x3E); // p.stm = 62
      }
    }
    if ( // stm: INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT)
      lchild.getOpcode() == ATTEMPT_INT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 242, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x4D); // p.stm = 77
      }
    }
    if ( // stm: INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT)
      lchild.getOpcode() == ATTEMPT_ADDR_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 243, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x4E); // p.stm = 78
      }
    }
    if ( // stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT)
      lchild.getOpcode() == REF_NEG_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 212, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x4F); // p.stm = 79
      }
    }
    if ( // stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT)
      lchild.getOpcode() == REF_NOT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 213, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x50); // p.stm = 80
      }
    }
    if ( // stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT)
      lchild.getOpcode() == REF_ADD_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 244, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x51); // p.stm = 81
      }
    }
    if ( // stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 245, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x52); // p.stm = 82
      }
    }
    if ( // stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT)
      lchild.getOpcode() == REF_OR_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 246, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x53); // p.stm = 83
      }
    }
    if ( // stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT)
      lchild.getOpcode() == REF_XOR_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 247, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x54); // p.stm = 84
      }
    }
    if ( // stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      lchild.getChild2().getChild1().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))&&U16(IV(Move.getVal(PLR(p))))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 214, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x55); // p.stm = 85
      }
    }
    if ( // stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      lchild.getChild2().getChild1().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))&&MASK(IV(Move.getVal(PLR(p))))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 215, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x56); // p.stm = 86
      }
    }
    if ( // stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == REF_ADD_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 216, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x57); // p.stm = 87
      }
    }
    if ( // stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == REF_NOT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2().getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 270, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x58); // p.stm = 88
      }
    }
    if ( // stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT)
      lchild.getOpcode() == REF_OR_opcode && 
      lchild.getChild2().getOpcode() == REF_NOT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2().getChild1()).getCost(2 /* r */) + ((!IfCmp.getCond(P(p)).isUNSIGNED())&&ZERO(IfCmp.getVal2(P(p)))?20:INFINITE);
      if(BURS.DEBUG) trace(p, 271, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x59); // p.stm = 89
      }
    }
  }

  /**
   * Labels INT_IFCMP2 tree node
   * @param p node to label
   */
  private static void label_INT_IFCMP2(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: INT_IFCMP2(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 74, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x3F); // p.stm = 63
    }
    if ( // stm: INT_IFCMP2(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 176, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x40); // p.stm = 64
      }
    }
  }

  /**
   * Labels LONG_IFCMP tree node
   * @param p node to label
   */
  private static void label_LONG_IFCMP(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: LONG_IFCMP(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 30;
    if(BURS.DEBUG) trace(p, 99, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x4C); // p.stm = 76
    }
  }

  /**
   * Labels FLOAT_IFCMP tree node
   * @param p node to label
   */
  private static void label_FLOAT_IFCMP(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: FLOAT_IFCMP(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 75, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x41); // p.stm = 65
    }
  }

  /**
   * Labels DOUBLE_IFCMP tree node
   * @param p node to label
   */
  private static void label_DOUBLE_IFCMP(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: DOUBLE_IFCMP(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 76, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x42); // p.stm = 66
    }
  }

  /**
   * Labels UNINT_BEGIN tree node
   * @param p node to label
   */
  private static void label_UNINT_BEGIN(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: UNINT_BEGIN
    if(BURS.DEBUG) trace(p, 15, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x4); // p.stm = 4
    }
  }

  /**
   * Labels UNINT_END tree node
   * @param p node to label
   */
  private static void label_UNINT_END(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: UNINT_END
    if(BURS.DEBUG) trace(p, 16, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x5); // p.stm = 5
    }
  }

  /**
   * Labels FENCE tree node
   * @param p node to label
   */
  private static void label_FENCE(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: FENCE
    if(BURS.DEBUG) trace(p, 25, 11 + 0, p.getCost(1) /* stm */);
    if (11 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(11));
      p.writePacked(0, 0xFFFFFF80, 0xD); // p.stm = 13
    }
  }

  /**
   * Labels READ_CEILING tree node
   * @param p node to label
   */
  private static void label_READ_CEILING(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: READ_CEILING
    if(BURS.DEBUG) trace(p, 27, 11 + 0, p.getCost(1) /* stm */);
    if (11 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(11));
      p.writePacked(0, 0xFFFFFF80, 0xF); // p.stm = 15
    }
  }

  /**
   * Labels WRITE_FLOOR tree node
   * @param p node to label
   */
  private static void label_WRITE_FLOOR(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: WRITE_FLOOR
    if(BURS.DEBUG) trace(p, 26, 11 + 0, p.getCost(1) /* stm */);
    if (11 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(11));
      p.writePacked(0, 0xFFFFFF80, 0xE); // p.stm = 14
    }
  }

  /**
   * Labels NOP tree node
   * @param p node to label
   */
  private static void label_NOP(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: NOP
    if(BURS.DEBUG) trace(p, 21, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0xA); // p.stm = 10
    }
  }

  /**
   * Labels LONG_MOVE tree node
   * @param p node to label
   */
  private static void label_LONG_MOVE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    if ( // r: LONG_MOVE(LONG_CONSTANT)
      lchild.getOpcode() == LONG_CONSTANT_opcode  
    ) {
      c = 40;
      if(BURS.DEBUG) trace(p, 37, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3880); // p.r = 113
        closure_r(p, c);
      }
    }
    // r: LONG_MOVE(r)
    c = STATE(lchild).getCost(2 /* r */) + 22;
    if(BURS.DEBUG) trace(p, 187, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3900); // p.r = 114
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_MOVE tree node
   * @param p node to label
   */
  private static void label_FLOAT_MOVE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: FLOAT_MOVE(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 159, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2680); // p.r = 77
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_MOVE tree node
   * @param p node to label
   */
  private static void label_DOUBLE_MOVE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: DOUBLE_MOVE(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 160, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2700); // p.r = 78
      closure_r(p, c);
    }
  }

  /**
   * Labels REF_MOVE tree node
   * @param p node to label
   */
  private static void label_REF_MOVE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: REF_MOVE(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 158, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2600); // p.r = 76
      closure_r(p, c);
    }
    if ( // rs: REF_MOVE(INT_CONSTANT)
      lchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = (SI16(IV(Move.getVal(P(p))))?11:INFINITE);
      if(BURS.DEBUG) trace(p, 30, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x60000); // p.rs = 6
        closure_rs(p, c);
      }
    }
    if ( // rs: REF_MOVE(INT_CONSTANT)
      lchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = (U16(IV(Move.getVal(P(p))))?11:INFINITE);
      if(BURS.DEBUG) trace(p, 31, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x70000); // p.rs = 7
        closure_rs(p, c);
      }
    }
    if ( // rs: REF_MOVE(INT_CONSTANT)
      lchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = 22;
      if(BURS.DEBUG) trace(p, 32, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x80000); // p.rs = 8
        closure_rs(p, c);
      }
    }
    if ( // r: REF_MOVE(ADDRESS_CONSTANT)
      lchild.getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = (SI16(AV(Move.getVal(P(p))))?11:INFINITE);
      if(BURS.DEBUG) trace(p, 38, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3B80); // p.r = 119
        closure_r(p, c);
      }
    }
    if ( // r: REF_MOVE(ADDRESS_CONSTANT)
      lchild.getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = (U16(AV(Move.getVal(P(p))))?11:INFINITE);
      if(BURS.DEBUG) trace(p, 39, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3C00); // p.r = 120
        closure_r(p, c);
      }
    }
    if ( // r: REF_MOVE(ADDRESS_CONSTANT)
      lchild.getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = 22;
      if(BURS.DEBUG) trace(p, 40, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3C80); // p.r = 121
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels GUARD_MOVE tree node
   * @param p node to label
   */
  private static void label_GUARD_MOVE(AbstractBURS_TreeNode p) {
    p.initCost();
    // r: GUARD_MOVE
    if(BURS.DEBUG) trace(p, 22, 11 + 0, p.getCost(2) /* r */);
    if (11 < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(11));
      p.writePacked(0, 0xFFFFC07F, 0x300); // p.r = 6
      closure_r(p, 11);
    }
  }

  /**
   * Labels GUARD_COMBINE tree node
   * @param p node to label
   */
  private static void label_GUARD_COMBINE(AbstractBURS_TreeNode p) {
    p.initCost();
    // r: GUARD_COMBINE
    if(BURS.DEBUG) trace(p, 23, 11 + 0, p.getCost(2) /* r */);
    if (11 < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(11));
      p.writePacked(0, 0xFFFFC07F, 0x380); // p.r = 7
      closure_r(p, 11);
    }
  }

  /**
   * Labels REF_ADD tree node
   * @param p node to label
   */
  private static void label_REF_ADD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: REF_ADD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 128, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x900); // p.r = 18
        closure_r(p, c);
      }
    }
    // r: REF_ADD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 47, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x980); // p.r = 19
      closure_r(p, c);
    }
    if ( // r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 129, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0xA00); // p.r = 20
        closure_r(p, c);
      }
    }
    if ( // r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + (U16(IV(Move.getVal(PR(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 130, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0xA80); // p.r = 21
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels LONG_ADD tree node
   * @param p node to label
   */
  private static void label_LONG_ADD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_ADD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 89, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2F80); // p.r = 95
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_ADD tree node
   * @param p node to label
   */
  private static void label_FLOAT_ADD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: FLOAT_ADD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 58, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1600); // p.r = 44
      closure_r(p, c);
    }
    if ( // r: FLOAT_ADD(FLOAT_MUL(r,r),r)
      lchild.getOpcode() == FLOAT_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 248, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1C00); // p.r = 56
        closure_r(p, c);
      }
    }
    if ( // r: FLOAT_ADD(r,FLOAT_MUL(r,r))
      rchild.getOpcode() == FLOAT_MUL_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PR(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 252, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1D00); // p.r = 58
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels DOUBLE_ADD tree node
   * @param p node to label
   */
  private static void label_DOUBLE_ADD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: DOUBLE_ADD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 59, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1680); // p.r = 45
      closure_r(p, c);
    }
    if ( // r: DOUBLE_ADD(DOUBLE_MUL(r,r),r)
      lchild.getOpcode() == DOUBLE_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 249, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1C80); // p.r = 57
        closure_r(p, c);
      }
    }
    if ( // r: DOUBLE_ADD(r,DOUBLE_MUL(r,r))
      rchild.getOpcode() == DOUBLE_MUL_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PR(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 253, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1D80); // p.r = 59
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels REF_SUB tree node
   * @param p node to label
   */
  private static void label_REF_SUB(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: REF_SUB(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 48, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0xB00); // p.r = 22
      closure_r(p, c);
    }
    if ( // r: REF_SUB(INT_CONSTANT,r)
      lchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(rchild).getCost(2 /* r */) + (SI16(IV(Binary.getVal1(P(p))))?11:INFINITE);
      if(BURS.DEBUG) trace(p, 195, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0xB80); // p.r = 23
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels LONG_SUB tree node
   * @param p node to label
   */
  private static void label_LONG_SUB(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_SUB(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 90, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3000); // p.r = 96
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_SUB tree node
   * @param p node to label
   */
  private static void label_FLOAT_SUB(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: FLOAT_SUB(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 62, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1800); // p.r = 48
      closure_r(p, c);
    }
    if ( // r: FLOAT_SUB(FLOAT_MUL(r,r),r)
      lchild.getOpcode() == FLOAT_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 250, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1E00); // p.r = 60
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels DOUBLE_SUB tree node
   * @param p node to label
   */
  private static void label_DOUBLE_SUB(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: DOUBLE_SUB(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 63, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1880); // p.r = 49
      closure_r(p, c);
    }
    if ( // r: DOUBLE_SUB(DOUBLE_MUL(r,r),r)
      lchild.getOpcode() == DOUBLE_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 251, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1E80); // p.r = 61
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels INT_MUL tree node
   * @param p node to label
   */
  private static void label_INT_MUL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: INT_MUL(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 131, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0xC00); // p.r = 24
        closure_r(p, c);
      }
    }
    // r: INT_MUL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 49, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0xC80); // p.r = 25
      closure_r(p, c);
    }
  }

  /**
   * Labels LONG_MUL tree node
   * @param p node to label
   */
  private static void label_LONG_MUL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_MUL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 91, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3080); // p.r = 97
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_MUL tree node
   * @param p node to label
   */
  private static void label_FLOAT_MUL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: FLOAT_MUL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 60, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1700); // p.r = 46
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_MUL tree node
   * @param p node to label
   */
  private static void label_DOUBLE_MUL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: DOUBLE_MUL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 61, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1780); // p.r = 47
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_DIV tree node
   * @param p node to label
   */
  private static void label_INT_DIV(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: INT_DIV(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 50, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0xD00); // p.r = 26
      closure_r(p, c);
    }
    if ( // r: INT_DIV(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 132, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0xD80); // p.r = 27
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels FLOAT_DIV tree node
   * @param p node to label
   */
  private static void label_FLOAT_DIV(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: FLOAT_DIV(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 64, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1900); // p.r = 50
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_DIV tree node
   * @param p node to label
   */
  private static void label_DOUBLE_DIV(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: DOUBLE_DIV(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 65, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1980); // p.r = 51
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_REM tree node
   * @param p node to label
   */
  private static void label_INT_REM(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: INT_REM(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 51, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0xE00); // p.r = 28
      closure_r(p, c);
    }
    if ( // r: INT_REM(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 133, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0xE80); // p.r = 29
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels REF_NEG tree node
   * @param p node to label
   */
  private static void label_REF_NEG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: REF_NEG(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 134, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0xF00); // p.r = 30
      closure_r(p, c);
    }
  }

  /**
   * Labels LONG_NEG tree node
   * @param p node to label
   */
  private static void label_LONG_NEG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: LONG_NEG(r)
    c = STATE(lchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 178, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3100); // p.r = 98
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_NEG tree node
   * @param p node to label
   */
  private static void label_FLOAT_NEG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: FLOAT_NEG(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 143, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1A00); // p.r = 52
      closure_r(p, c);
    }
    if ( // r: FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r,r),r))
      lchild.getOpcode() == FLOAT_ADD_opcode && 
      lchild.getChild1().getOpcode() == FLOAT_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1().getChild1()).getCost(2 /* r */) + STATE(lchild.getChild1().getChild2()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p),PLL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 260, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1F00); // p.r = 62
        closure_r(p, c);
      }
    }
    if ( // r: FLOAT_NEG(FLOAT_ADD(r,FLOAT_MUL(r,r)))
      lchild.getOpcode() == FLOAT_ADD_opcode && 
      lchild.getChild2().getOpcode() == FLOAT_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2().getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2().getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p),PLR(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 264, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2000); // p.r = 64
        closure_r(p, c);
      }
    }
    if ( // r: FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r,r),r))
      lchild.getOpcode() == FLOAT_SUB_opcode && 
      lchild.getChild1().getOpcode() == FLOAT_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1().getChild1()).getCost(2 /* r */) + STATE(lchild.getChild1().getChild2()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p),PLL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 262, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2100); // p.r = 66
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels DOUBLE_NEG tree node
   * @param p node to label
   */
  private static void label_DOUBLE_NEG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: DOUBLE_NEG(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 144, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1A80); // p.r = 53
      closure_r(p, c);
    }
    if ( // r: DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r,r),r))
      lchild.getOpcode() == DOUBLE_ADD_opcode && 
      lchild.getChild1().getOpcode() == DOUBLE_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1().getChild1()).getCost(2 /* r */) + STATE(lchild.getChild1().getChild2()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p),PLL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 261, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1F80); // p.r = 63
        closure_r(p, c);
      }
    }
    if ( // r: DOUBLE_NEG(DOUBLE_ADD(r,DOUBLE_MUL(r,r)))
      lchild.getOpcode() == DOUBLE_ADD_opcode && 
      lchild.getChild2().getOpcode() == DOUBLE_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2().getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2().getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p),PLR(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 265, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2080); // p.r = 65
        closure_r(p, c);
      }
    }
    if ( // r: DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r,r),r))
      lchild.getOpcode() == DOUBLE_SUB_opcode && 
      lchild.getChild1().getOpcode() == DOUBLE_MUL_opcode  
    ) {
      c = STATE(lchild.getChild1().getChild1()).getCost(2 /* r */) + STATE(lchild.getChild1().getChild2()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + (IR.strictFP(P(p),PL(p),PLL(p))?INFINITE:10);
      if(BURS.DEBUG) trace(p, 263, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2180); // p.r = 67
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels FLOAT_SQRT tree node
   * @param p node to label
   */
  private static void label_FLOAT_SQRT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: FLOAT_SQRT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 145, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1B00); // p.r = 54
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_SQRT tree node
   * @param p node to label
   */
  private static void label_DOUBLE_SQRT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: DOUBLE_SQRT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 146, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1B80); // p.r = 55
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_SHL tree node
   * @param p node to label
   */
  private static void label_INT_SHL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rz: INT_SHL(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 135, c + 0, p.getCost(5) /* rz */);
      if (c < p.getCost(5) /* rz */) {
        p.setCost(5 /* rz */, (char)(c));
        p.writePacked(0, 0xFF1FFFFF, 0x400000); // p.rz = 2
        closure_rz(p, c);
      }
    }
    // rz: INT_SHL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 52, c + 0, p.getCost(5) /* rz */);
    if (c < p.getCost(5) /* rz */) {
      p.setCost(5 /* rz */, (char)(c));
      p.writePacked(0, 0xFF1FFFFF, 0x600000); // p.rz = 3
      closure_rz(p, c);
    }
    if ( // rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == INT_USHR_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 197, c + 0, p.getCost(5) /* rz */);
      if (c < p.getCost(5) /* rz */) {
        p.setCost(5 /* rz */, (char)(c));
        p.writePacked(0, 0xFF1FFFFF, 0x800000); // p.rz = 4
        closure_rz(p, c);
      }
    }
  }

  /**
   * Labels LONG_SHL tree node
   * @param p node to label
   */
  private static void label_LONG_SHL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_SHL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 92, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3180); // p.r = 99
      closure_r(p, c);
    }
    if ( // r: LONG_SHL(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 179, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3200); // p.r = 100
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels INT_SHR tree node
   * @param p node to label
   */
  private static void label_INT_SHR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rs: INT_SHR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 136, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x20000); // p.rs = 2
        closure_rs(p, c);
      }
    }
    // rs: INT_SHR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 53, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0x30000); // p.rs = 3
      closure_rs(p, c);
    }
    if ( // rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (POSITIVE_MASK(IV(Binary.getVal2(PL(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 198, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x1000000); // p.rp = 1
        closure_rp(p, c);
      }
    }
  }

  /**
   * Labels LONG_SHR tree node
   * @param p node to label
   */
  private static void label_LONG_SHR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_SHR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 93, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3280); // p.r = 101
      closure_r(p, c);
    }
    if ( // r: LONG_SHR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 180, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3300); // p.r = 102
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels INT_USHR tree node
   * @param p node to label
   */
  private static void label_INT_USHR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rp: INT_USHR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 137, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x2000000); // p.rp = 2
        closure_rp(p, c);
      }
    }
    // rz: INT_USHR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 54, c + 0, p.getCost(5) /* rz */);
    if (c < p.getCost(5) /* rz */) {
      p.setCost(5 /* rz */, (char)(c));
      p.writePacked(0, 0xFF1FFFFF, 0xA00000); // p.rz = 5
      closure_rz(p, c);
    }
    if ( // rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (POSITIVE_MASK(IV(Binary.getVal2(PL(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 199, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x3000000); // p.rp = 3
        closure_rp(p, c);
      }
    }
    if ( // rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      lchild.getOpcode() == REF_AND_opcode && 
      lchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      lchild.getChild2().getChild1().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (POSITIVE_MASK(IV(Move.getVal(PLR(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 200, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x4000000); // p.rp = 4
        closure_rp(p, c);
      }
    }
    if ( // rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == INT_SHL_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 201, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x5000000); // p.rp = 5
        closure_rp(p, c);
      }
    }
  }

  /**
   * Labels LONG_USHR tree node
   * @param p node to label
   */
  private static void label_LONG_USHR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_USHR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 94, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3380); // p.r = 103
      closure_r(p, c);
    }
    if ( // r: LONG_USHR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 181, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3400); // p.r = 104
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels REF_AND tree node
   * @param p node to label
   */
  private static void label_REF_AND(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: REF_AND(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 55, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0xF80); // p.r = 31
      closure_r(p, c);
    }
    if ( // czr: REF_AND(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 138, c + 0, p.getCost(3) /* czr */);
      if (c < p.getCost(3) /* czr */) {
        p.setCost(3 /* czr */, (char)(c));
        p.writePacked(0, 0xFFFF3FFF, 0x4000); // p.czr = 1
        closure_czr(p, c);
      }
    }
    if ( // rp: REF_AND(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + (MASK(IV(Binary.getVal2(P(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 139, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x6000000); // p.rp = 6
        closure_rp(p, c);
      }
    }
    if ( // r: REF_AND(REF_NOT(r),REF_NOT(r))
      lchild.getOpcode() == REF_NOT_opcode && 
      rchild.getOpcode() == REF_NOT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 217, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1000); // p.r = 32
        closure_r(p, c);
      }
    }
    if ( // r: REF_AND(r,REF_NOT(r))
      rchild.getOpcode() == REF_NOT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 222, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1080); // p.r = 33
        closure_r(p, c);
      }
    }
    if ( // rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == INT_USHR_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (POSITIVE_MASK(IV(Binary.getVal2(P(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 202, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x7000000); // p.rp = 7
        closure_rp(p, c);
      }
    }
    if ( // rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT))
      lchild.getOpcode() == INT_USHR_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (POSITIVE_MASK(IV(Move.getVal(PR(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 203, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0x8000000); // p.rp = 8
        closure_rp(p, c);
      }
    }
    if ( // rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT)
      lchild.getOpcode() == BYTE_LOAD_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + (VR(p) == 0xff ? 10 : INFINITE);
      if(BURS.DEBUG) trace(p, 237, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0xA000000); // p.rp = 10
        closure_rp(p, c);
      }
    }
    if ( // rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == BYTE_LOAD_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (VR(p) == 0xff ? 10 : INFINITE);
      if(BURS.DEBUG) trace(p, 204, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0xB000000); // p.rp = 11
        closure_rp(p, c);
      }
    }
    if ( // czr: REF_AND(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + (U16(IV(Move.getVal(PR(p))))?11:INFINITE);
      if(BURS.DEBUG) trace(p, 188, c + 0, p.getCost(3) /* czr */);
      if (c < p.getCost(3) /* czr */) {
        p.setCost(3 /* czr */, (char)(c));
        p.writePacked(0, 0xFFFF3FFF, 0x8000); // p.czr = 2
        closure_czr(p, c);
      }
    }
    if ( // r: REF_AND(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + (MASK(IV(Move.getVal(PR(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 189, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3980); // p.r = 115
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels LONG_AND tree node
   * @param p node to label
   */
  private static void label_LONG_AND(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_AND(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 95, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3480); // p.r = 105
      closure_r(p, c);
    }
  }

  /**
   * Labels REF_OR tree node
   * @param p node to label
   */
  private static void label_REF_OR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: REF_OR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 56, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1100); // p.r = 34
      closure_r(p, c);
    }
    if ( // r: REF_OR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 140, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1180); // p.r = 35
        closure_r(p, c);
      }
    }
    if ( // r: REF_OR(REF_NOT(r),REF_NOT(r))
      lchild.getOpcode() == REF_NOT_opcode && 
      rchild.getOpcode() == REF_NOT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 218, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1200); // p.r = 36
        closure_r(p, c);
      }
    }
    if ( // r: REF_OR(r,REF_NOT(r))
      rchild.getOpcode() == REF_NOT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 223, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1280); // p.r = 37
        closure_r(p, c);
      }
    }
    if ( // r: REF_OR(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + (U16(IV(Move.getVal(PR(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 190, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3A00); // p.r = 116
        closure_r(p, c);
      }
    }
    if ( // r: REF_OR(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 20;
      if(BURS.DEBUG) trace(p, 191, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3A80); // p.r = 117
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels LONG_OR tree node
   * @param p node to label
   */
  private static void label_LONG_OR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_OR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 96, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3500); // p.r = 106
      closure_r(p, c);
    }
  }

  /**
   * Labels REF_XOR tree node
   * @param p node to label
   */
  private static void label_REF_XOR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: REF_XOR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 57, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1300); // p.r = 38
      closure_r(p, c);
    }
    if ( // r: REF_XOR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 141, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1380); // p.r = 39
        closure_r(p, c);
      }
    }
    if ( // r: REF_XOR(r,REF_MOVE(INT_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + (U16(IV(Move.getVal(PR(p))))?10:INFINITE);
      if(BURS.DEBUG) trace(p, 192, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3B00); // p.r = 118
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels REF_NOT tree node
   * @param p node to label
   */
  private static void label_REF_NOT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: REF_NOT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 142, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x1400); // p.r = 40
      closure_r(p, c);
    }
    if ( // r: REF_NOT(REF_OR(r,r))
      lchild.getOpcode() == REF_OR_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 234, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1480); // p.r = 41
        closure_r(p, c);
      }
    }
    if ( // r: REF_NOT(REF_AND(r,r))
      lchild.getOpcode() == REF_AND_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 235, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1500); // p.r = 42
        closure_r(p, c);
      }
    }
    if ( // r: REF_NOT(REF_XOR(r,r))
      lchild.getOpcode() == REF_XOR_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 236, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x1580); // p.r = 43
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels LONG_NOT tree node
   * @param p node to label
   */
  private static void label_LONG_NOT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: LONG_NOT(r)
    c = STATE(lchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 182, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3600); // p.r = 108
      closure_r(p, c);
    }
  }

  /**
   * Labels LONG_XOR tree node
   * @param p node to label
   */
  private static void label_LONG_XOR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: LONG_XOR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 97, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3580); // p.r = 107
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_2LONG tree node
   * @param p node to label
   */
  private static void label_INT_2LONG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: INT_2LONG(r)
    c = STATE(lchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 183, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3680); // p.r = 109
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_2FLOAT tree node
   * @param p node to label
   */
  private static void label_INT_2FLOAT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: INT_2FLOAT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 150, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2200); // p.r = 68
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_2DOUBLE tree node
   * @param p node to label
   */
  private static void label_INT_2DOUBLE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: INT_2DOUBLE(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 151, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2280); // p.r = 69
      closure_r(p, c);
    }
  }

  /**
   * Labels LONG_2INT tree node
   * @param p node to label
   */
  private static void label_LONG_2INT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: LONG_2INT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 184, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3700); // p.r = 110
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_2INT tree node
   * @param p node to label
   */
  private static void label_FLOAT_2INT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: FLOAT_2INT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 152, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2300); // p.r = 70
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_2DOUBLE tree node
   * @param p node to label
   */
  private static void label_FLOAT_2DOUBLE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: FLOAT_2DOUBLE(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 153, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2380); // p.r = 71
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_2INT tree node
   * @param p node to label
   */
  private static void label_DOUBLE_2INT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: DOUBLE_2INT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 154, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2400); // p.r = 72
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_2FLOAT tree node
   * @param p node to label
   */
  private static void label_DOUBLE_2FLOAT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: DOUBLE_2FLOAT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 155, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2480); // p.r = 73
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_2BYTE tree node
   * @param p node to label
   */
  private static void label_INT_2BYTE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // rs: INT_2BYTE(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 147, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0x40000); // p.rs = 4
      closure_rs(p, c);
    }
  }

  /**
   * Labels INT_2USHORT tree node
   * @param p node to label
   */
  private static void label_INT_2USHORT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // rp: INT_2USHORT(r)
    c = STATE(lchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 148, c + 0, p.getCost(6) /* rp */);
    if (c < p.getCost(6) /* rp */) {
      p.setCost(6 /* rp */, (char)(c));
      p.writePacked(0, 0xF0FFFFFF, 0x9000000); // p.rp = 9
      closure_rp(p, c);
    }
  }

  /**
   * Labels INT_2SHORT tree node
   * @param p node to label
   */
  private static void label_INT_2SHORT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // rs: INT_2SHORT(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 149, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0x50000); // p.rs = 5
      closure_rs(p, c);
    }
  }

  /**
   * Labels LONG_CMP tree node
   * @param p node to label
   */
  private static void label_LONG_CMP(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: LONG_CMP(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 98, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x4B); // p.stm = 75
    }
  }

  /**
   * Labels FLOAT_CMPL tree node
   * @param p node to label
   */
  private static void label_FLOAT_CMPL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: FLOAT_CMPL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 77, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x43); // p.stm = 67
    }
  }

  /**
   * Labels FLOAT_CMPG tree node
   * @param p node to label
   */
  private static void label_FLOAT_CMPG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: FLOAT_CMPG(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 78, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x44); // p.stm = 68
    }
  }

  /**
   * Labels DOUBLE_CMPL tree node
   * @param p node to label
   */
  private static void label_DOUBLE_CMPL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: DOUBLE_CMPL(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 79, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x45); // p.stm = 69
    }
  }

  /**
   * Labels DOUBLE_CMPG tree node
   * @param p node to label
   */
  private static void label_DOUBLE_CMPG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: DOUBLE_CMPG(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 80, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x46); // p.stm = 70
    }
  }

  /**
   * Labels RETURN tree node
   * @param p node to label
   */
  private static void label_RETURN(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    if ( // stm: RETURN(NULL)
      lchild.getOpcode() == NULL_opcode  
    ) {
      c = 10;
      if(BURS.DEBUG) trace(p, 34, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x48); // p.stm = 72
      }
    }
    // stm: RETURN(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 177, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x49); // p.stm = 73
    }
  }

  /**
   * Labels NULL_CHECK tree node
   * @param p node to label
   */
  private static void label_NULL_CHECK(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: NULL_CHECK(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 104, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0xB); // p.stm = 11
    }
  }

  /**
   * Labels GOTO tree node
   * @param p node to label
   */
  private static void label_GOTO(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: GOTO
    if(BURS.DEBUG) trace(p, 33, 11 + 0, p.getCost(1) /* stm */);
    if (11 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(11));
      p.writePacked(0, 0xFFFFFF80, 0x47); // p.stm = 71
    }
  }

  /**
   * Labels BOOLEAN_NOT tree node
   * @param p node to label
   */
  private static void label_BOOLEAN_NOT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: BOOLEAN_NOT(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 115, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x480); // p.r = 9
      closure_r(p, c);
    }
  }

  /**
   * Labels BOOLEAN_CMP_INT tree node
   * @param p node to label
   */
  private static void label_BOOLEAN_CMP_INT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: BOOLEAN_CMP_INT(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 116, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x500); // p.r = 10
        closure_r(p, c);
      }
    }
    // r: BOOLEAN_CMP_INT(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 43, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x580); // p.r = 11
      closure_r(p, c);
    }
    if ( // boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 0;
      if(BURS.DEBUG) trace(p, 117, c + 0, p.getCost(8) /* boolcmp */);
      if (c < p.getCost(8) /* boolcmp */) {
        p.setCost(8 /* boolcmp */, (char)(c));
        p.writePacked(1, 0xFFFFFFF0, 0x1); // p.boolcmp = 1
      }
    }
    // boolcmp: BOOLEAN_CMP_INT(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 0;
    if(BURS.DEBUG) trace(p, 44, c + 0, p.getCost(8) /* boolcmp */);
    if (c < p.getCost(8) /* boolcmp */) {
      p.setCost(8 /* boolcmp */, (char)(c));
      p.writePacked(1, 0xFFFFFFF0, 0x2); // p.boolcmp = 2
    }
    if ( // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 1 && BooleanCmp.getCond(P(p)).isEQUAL()?0:INFINITE);
      if(BURS.DEBUG) trace(p, 120, c + 0, p.getCost(8) /* boolcmp */);
      if (c < p.getCost(8) /* boolcmp */) {
        p.setCost(8 /* boolcmp */, (char)(c));
        p.writePacked(1, 0xFFFFFFF0, 0x5); // p.boolcmp = 5
      }
    }
    if ( // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 1 && BooleanCmp.getCond(P(p)).isNOT_EQUAL()?0:INFINITE);
      if(BURS.DEBUG) trace(p, 121, c + 0, p.getCost(8) /* boolcmp */);
      if (c < p.getCost(8) /* boolcmp */) {
        p.setCost(8 /* boolcmp */, (char)(c));
        p.writePacked(1, 0xFFFFFFF0, 0x6); // p.boolcmp = 6
      }
    }
    if ( // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 0 && BooleanCmp.getCond(P(p)).isNOT_EQUAL()?0:INFINITE);
      if(BURS.DEBUG) trace(p, 122, c + 0, p.getCost(8) /* boolcmp */);
      if (c < p.getCost(8) /* boolcmp */) {
        p.setCost(8 /* boolcmp */, (char)(c));
        p.writePacked(1, 0xFFFFFFF0, 0x7); // p.boolcmp = 7
      }
    }
    if ( // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 0 && BooleanCmp.getCond(P(p)).isEQUAL()?0:INFINITE);
      if(BURS.DEBUG) trace(p, 123, c + 0, p.getCost(8) /* boolcmp */);
      if (c < p.getCost(8) /* boolcmp */) {
        p.setCost(8 /* boolcmp */, (char)(c));
        p.writePacked(1, 0xFFFFFFF0, 0x8); // p.boolcmp = 8
      }
    }
    if ( // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 1 && BooleanCmp.getCond(P(p)).isEQUAL()?10:INFINITE);
      if(BURS.DEBUG) trace(p, 124, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x700); // p.r = 14
        closure_r(p, c);
      }
    }
    if ( // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 1 && BooleanCmp.getCond(P(p)).isNOT_EQUAL()?10:INFINITE);
      if(BURS.DEBUG) trace(p, 125, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x780); // p.r = 15
        closure_r(p, c);
      }
    }
    if ( // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 0 && BooleanCmp.getCond(P(p)).isNOT_EQUAL()?10:INFINITE);
      if(BURS.DEBUG) trace(p, 126, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x800); // p.r = 16
        closure_r(p, c);
      }
    }
    if ( // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(8 /* boolcmp */) + (VR(p) == 0 && BooleanCmp.getCond(P(p)).isEQUAL()?10:INFINITE);
      if(BURS.DEBUG) trace(p, 127, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x880); // p.r = 17
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels BOOLEAN_CMP_ADDR tree node
   * @param p node to label
   */
  private static void label_BOOLEAN_CMP_ADDR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 118, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x600); // p.r = 12
        closure_r(p, c);
      }
    }
    // r: BOOLEAN_CMP_ADDR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 45, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x680); // p.r = 13
      closure_r(p, c);
    }
    if ( // boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 0;
      if(BURS.DEBUG) trace(p, 119, c + 0, p.getCost(8) /* boolcmp */);
      if (c < p.getCost(8) /* boolcmp */) {
        p.setCost(8 /* boolcmp */, (char)(c));
        p.writePacked(1, 0xFFFFFFF0, 0x3); // p.boolcmp = 3
      }
    }
    // boolcmp: BOOLEAN_CMP_ADDR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 0;
    if(BURS.DEBUG) trace(p, 46, c + 0, p.getCost(8) /* boolcmp */);
    if (c < p.getCost(8) /* boolcmp */) {
      p.setCost(8 /* boolcmp */, (char)(c));
      p.writePacked(1, 0xFFFFFFF0, 0x4); // p.boolcmp = 4
    }
  }

  /**
   * Labels BYTE_LOAD tree node
   * @param p node to label
   */
  private static void label_BYTE_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rs: BYTE_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 22;
      if(BURS.DEBUG) trace(p, 161, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x90000); // p.rs = 9
        closure_rs(p, c);
      }
    }
    // rs: BYTE_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 22;
    if(BURS.DEBUG) trace(p, 66, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0xA0000); // p.rs = 10
      closure_rs(p, c);
    }
  }

  /**
   * Labels UBYTE_LOAD tree node
   * @param p node to label
   */
  private static void label_UBYTE_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rp: UBYTE_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 162, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0xC000000); // p.rp = 12
        closure_rp(p, c);
      }
    }
    // rp: UBYTE_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 67, c + 0, p.getCost(6) /* rp */);
    if (c < p.getCost(6) /* rp */) {
      p.setCost(6 /* rp */, (char)(c));
      p.writePacked(0, 0xF0FFFFFF, 0xD000000); // p.rp = 13
      closure_rp(p, c);
    }
  }

  /**
   * Labels SHORT_LOAD tree node
   * @param p node to label
   */
  private static void label_SHORT_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rs: SHORT_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 163, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0xB0000); // p.rs = 11
        closure_rs(p, c);
      }
    }
    // rs: SHORT_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 68, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0xC0000); // p.rs = 12
      closure_rs(p, c);
    }
  }

  /**
   * Labels USHORT_LOAD tree node
   * @param p node to label
   */
  private static void label_USHORT_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rp: USHORT_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 164, c + 0, p.getCost(6) /* rp */);
      if (c < p.getCost(6) /* rp */) {
        p.setCost(6 /* rp */, (char)(c));
        p.writePacked(0, 0xF0FFFFFF, 0xE000000); // p.rp = 14
        closure_rp(p, c);
      }
    }
    // rp: USHORT_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 69, c + 0, p.getCost(6) /* rp */);
    if (c < p.getCost(6) /* rp */) {
      p.setCost(6 /* rp */, (char)(c));
      p.writePacked(0, 0xF0FFFFFF, 0xF000000); // p.rp = 15
      closure_rp(p, c);
    }
  }

  /**
   * Labels INT_LOAD tree node
   * @param p node to label
   */
  private static void label_INT_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // rs: INT_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 169, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0xD0000); // p.rs = 13
        closure_rs(p, c);
      }
    }
    if ( // rs: INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + FITS(Move.getVal(PR(p)),32,22);
      if(BURS.DEBUG) trace(p, 170, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0xE0000); // p.rs = 14
        closure_rs(p, c);
      }
    }
    // rs: INT_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 72, c + 0, p.getCost(4) /* rs */);
    if (c < p.getCost(4) /* rs */) {
      p.setCost(4 /* rs */, (char)(c));
      p.writePacked(0, 0xFFE0FFFF, 0xF0000); // p.rs = 15
      closure_rs(p, c);
    }
    if ( // rs: INT_LOAD(REF_ADD(r,r),INT_CONSTANT)
      lchild.getOpcode() == REF_ADD_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(lchild.getChild2()).getCost(2 /* r */) + isZERO(VR(p), 11);
      if(BURS.DEBUG) trace(p, 238, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x100000); // p.rs = 16
        closure_rs(p, c);
      }
    }
    if ( // rs: INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
      lchild.getOpcode() == REF_ADD_opcode && 
      lchild.getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + (SI16(VR(p)+VLR(p)) ? 14 : INFINITE);
      if(BURS.DEBUG) trace(p, 205, c + 0, p.getCost(4) /* rs */);
      if (c < p.getCost(4) /* rs */) {
        p.setCost(4 /* rs */, (char)(c));
        p.writePacked(0, 0xFFE0FFFF, 0x110000); // p.rs = 17
        closure_rs(p, c);
      }
    }
  }

  /**
   * Labels LONG_LOAD tree node
   * @param p node to label
   */
  private static void label_LONG_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: LONG_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 21;
      if(BURS.DEBUG) trace(p, 193, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3D00); // p.r = 122
        closure_r(p, c);
      }
    }
    if ( // r: LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 21;
      if(BURS.DEBUG) trace(p, 194, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x3D80); // p.r = 123
        closure_r(p, c);
      }
    }
    // r: LONG_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 21;
    if(BURS.DEBUG) trace(p, 100, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3E00); // p.r = 124
      closure_r(p, c);
    }
  }

  /**
   * Labels FLOAT_LOAD tree node
   * @param p node to label
   */
  private static void label_FLOAT_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: FLOAT_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 165, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2780); // p.r = 79
        closure_r(p, c);
      }
    }
    if ( // r: FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + FITS(Move.getVal(PR(p)),32,22);
      if(BURS.DEBUG) trace(p, 166, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2800); // p.r = 80
        closure_r(p, c);
      }
    }
    // r: FLOAT_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 70, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2880); // p.r = 81
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_LOAD tree node
   * @param p node to label
   */
  private static void label_DOUBLE_LOAD(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // r: DOUBLE_LOAD(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 167, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2900); // p.r = 82
        closure_r(p, c);
      }
    }
    if ( // r: DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      rchild.getOpcode() == REF_MOVE_opcode && 
      rchild.getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + FITS(Move.getVal(PR(p)),32,22);
      if(BURS.DEBUG) trace(p, 168, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2980); // p.r = 83
        closure_r(p, c);
      }
    }
    // r: DOUBLE_LOAD(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 71, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2A00); // p.r = 84
      closure_r(p, c);
    }
  }

  /**
   * Labels BYTE_STORE tree node
   * @param p node to label
   */
  private static void label_BYTE_STORE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 224, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x1C); // p.stm = 28
      }
    }
    if ( // stm: BYTE_STORE(r,OTHER_OPERAND(r,r))
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 254, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x1D); // p.stm = 29
      }
    }
    if ( // stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT))
      lchild.getOpcode() == INT_2BYTE_opcode && 
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 219, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x1E); // p.stm = 30
      }
    }
    if ( // stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r))
      lchild.getOpcode() == INT_2BYTE_opcode && 
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 266, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x1F); // p.stm = 31
      }
    }
  }

  /**
   * Labels SHORT_STORE tree node
   * @param p node to label
   */
  private static void label_SHORT_STORE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 225, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x20); // p.stm = 32
      }
    }
    if ( // stm: SHORT_STORE(r,OTHER_OPERAND(r,r))
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 255, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x21); // p.stm = 33
      }
    }
    if ( // stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
      lchild.getOpcode() == INT_2SHORT_opcode && 
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 220, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x22); // p.stm = 34
      }
    }
    if ( // stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r))
      lchild.getOpcode() == INT_2SHORT_opcode && 
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 267, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x23); // p.stm = 35
      }
    }
    if ( // stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
      lchild.getOpcode() == INT_2USHORT_opcode && 
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 221, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x24); // p.stm = 36
      }
    }
    if ( // stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r))
      lchild.getOpcode() == INT_2USHORT_opcode && 
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 268, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x25); // p.stm = 37
      }
    }
  }

  /**
   * Labels INT_STORE tree node
   * @param p node to label
   */
  private static void label_INT_STORE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 226, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x26); // p.stm = 38
      }
    }
    if ( // stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      rchild.getChild2().getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + FITS(Move.getVal(PRR(p)),32,22);
      if(BURS.DEBUG) trace(p, 227, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x27); // p.stm = 39
      }
    }
    if ( // stm: INT_STORE(r,OTHER_OPERAND(r,r))
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 256, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x28); // p.stm = 40
      }
    }
    if ( // stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild1().getOpcode() == REF_ADD_opcode && 
      rchild.getChild1().getChild2().getOpcode() == INT_CONSTANT_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1().getChild1()).getCost(2 /* r */) + (SI16(VRR(p)+VRLR(p))?14:INFINITE);
      if(BURS.DEBUG) trace(p, 269, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x29); // p.stm = 41
      }
    }
  }

  /**
   * Labels LONG_STORE tree node
   * @param p node to label
   */
  private static void label_LONG_STORE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 22;
      if(BURS.DEBUG) trace(p, 232, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x5A); // p.stm = 90
      }
    }
    if ( // stm: LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      rchild.getChild2().getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 22;
      if(BURS.DEBUG) trace(p, 233, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x5B); // p.stm = 91
      }
    }
    if ( // stm: LONG_STORE(r,OTHER_OPERAND(r,r))
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 22;
      if(BURS.DEBUG) trace(p, 259, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x5C); // p.stm = 92
      }
    }
  }

  /**
   * Labels FLOAT_STORE tree node
   * @param p node to label
   */
  private static void label_FLOAT_STORE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 228, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x2A); // p.stm = 42
      }
    }
    if ( // stm: FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      rchild.getChild2().getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + FITS(Move.getVal(PRR(p)),32,22);
      if(BURS.DEBUG) trace(p, 229, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x2B); // p.stm = 43
      }
    }
    if ( // stm: FLOAT_STORE(r,OTHER_OPERAND(r,r))
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 257, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x2C); // p.stm = 44
      }
    }
  }

  /**
   * Labels DOUBLE_STORE tree node
   * @param p node to label
   */
  private static void label_DOUBLE_STORE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    if ( // stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 230, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x2D); // p.stm = 45
      }
    }
    if ( // stm: DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      rchild.getOpcode() == OTHER_OPERAND_opcode && 
      rchild.getChild2().getOpcode() == REF_MOVE_opcode && 
      rchild.getChild2().getChild1().getOpcode() == ADDRESS_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + FITS(Move.getVal(PRR(p)),32,22);
      if(BURS.DEBUG) trace(p, 231, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x2E); // p.stm = 46
      }
    }
    if ( // stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r))
      rchild.getOpcode() == OTHER_OPERAND_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + STATE(rchild.getChild1()).getCost(2 /* r */) + STATE(rchild.getChild2()).getCost(2 /* r */) + 11;
      if(BURS.DEBUG) trace(p, 258, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x2F); // p.stm = 47
      }
    }
  }

  /**
   * Labels PREPARE_INT tree node
   * @param p node to label
   */
  private static void label_PREPARE_INT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: PREPARE_INT(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 85, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2D80); // p.r = 91
      closure_r(p, c);
    }
  }

  /**
   * Labels PREPARE_ADDR tree node
   * @param p node to label
   */
  private static void label_PREPARE_ADDR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: PREPARE_ADDR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 101, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3E80); // p.r = 125
      closure_r(p, c);
    }
  }

  /**
   * Labels PREPARE_LONG tree node
   * @param p node to label
   */
  private static void label_PREPARE_LONG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: PREPARE_LONG(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 86, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2E00); // p.r = 92
      closure_r(p, c);
    }
  }

  /**
   * Labels ATTEMPT_INT tree node
   * @param p node to label
   */
  private static void label_ATTEMPT_INT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: ATTEMPT_INT(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 87, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2E80); // p.r = 93
      closure_r(p, c);
    }
  }

  /**
   * Labels ATTEMPT_ADDR tree node
   * @param p node to label
   */
  private static void label_ATTEMPT_ADDR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: ATTEMPT_ADDR(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 102, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3F00); // p.r = 126
      closure_r(p, c);
    }
  }

  /**
   * Labels ATTEMPT_LONG tree node
   * @param p node to label
   */
  private static void label_ATTEMPT_LONG(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: ATTEMPT_LONG(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 88, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2F00); // p.r = 94
      closure_r(p, c);
    }
  }

  /**
   * Labels CALL tree node
   * @param p node to label
   */
  private static void label_CALL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: CALL(r,any)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(7 /* any */) + 10;
    if(BURS.DEBUG) trace(p, 81, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2A80); // p.r = 85
      closure_r(p, c);
    }
    if ( // r: CALL(BRANCH_TARGET,any)
      lchild.getOpcode() == BRANCH_TARGET_opcode  
    ) {
      c = STATE(rchild).getCost(7 /* any */) + 10;
      if(BURS.DEBUG) trace(p, 196, c + 0, p.getCost(2) /* r */);
      if (c < p.getCost(2) /* r */) {
        p.setCost(2 /* r */, (char)(c));
        p.writePacked(0, 0xFFFFC07F, 0x2B00); // p.r = 86
        closure_r(p, c);
      }
    }
  }

  /**
   * Labels SYSCALL tree node
   * @param p node to label
   */
  private static void label_SYSCALL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: SYSCALL(r,any)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(7 /* any */) + 10;
    if(BURS.DEBUG) trace(p, 82, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2B80); // p.r = 87
      closure_r(p, c);
    }
  }

  /**
   * Labels YIELDPOINT_PROLOGUE tree node
   * @param p node to label
   */
  private static void label_YIELDPOINT_PROLOGUE(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: YIELDPOINT_PROLOGUE
    if(BURS.DEBUG) trace(p, 17, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x6); // p.stm = 6
    }
  }

  /**
   * Labels YIELDPOINT_EPILOGUE tree node
   * @param p node to label
   */
  private static void label_YIELDPOINT_EPILOGUE(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: YIELDPOINT_EPILOGUE
    if(BURS.DEBUG) trace(p, 18, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x7); // p.stm = 7
    }
  }

  /**
   * Labels YIELDPOINT_BACKEDGE tree node
   * @param p node to label
   */
  private static void label_YIELDPOINT_BACKEDGE(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: YIELDPOINT_BACKEDGE
    if(BURS.DEBUG) trace(p, 19, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x8); // p.stm = 8
    }
  }

  /**
   * Labels YIELDPOINT_OSR tree node
   * @param p node to label
   */
  private static void label_YIELDPOINT_OSR(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // r: YIELDPOINT_OSR(any,any)
    c = STATE(lchild).getCost(7 /* any */) + STATE(rchild).getCost(7 /* any */) + 11;
    if(BURS.DEBUG) trace(p, 84, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2D00); // p.r = 90
      closure_r(p, c);
    }
  }

  /**
   * Labels IR_PROLOGUE tree node
   * @param p node to label
   */
  private static void label_IR_PROLOGUE(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: IR_PROLOGUE
    if(BURS.DEBUG) trace(p, 36, 11 + 0, p.getCost(1) /* stm */);
    if (11 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(11));
      p.writePacked(0, 0xFFFFFF80, 0x4A); // p.stm = 74
    }
  }

  /**
   * Labels RESOLVE tree node
   * @param p node to label
   */
  private static void label_RESOLVE(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: RESOLVE
    if(BURS.DEBUG) trace(p, 13, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x2); // p.stm = 2
    }
  }

  /**
   * Labels GET_TIME_BASE tree node
   * @param p node to label
   */
  private static void label_GET_TIME_BASE(AbstractBURS_TreeNode p) {
    p.initCost();
    // r: GET_TIME_BASE
    if(BURS.DEBUG) trace(p, 35, 11 + 0, p.getCost(2) /* r */);
    if (11 < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(11));
      p.writePacked(0, 0xFFFFC07F, 0x2C00); // p.r = 88
      closure_r(p, 11);
    }
  }

  /**
   * Labels TRAP_IF tree node
   * @param p node to label
   */
  private static void label_TRAP_IF(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // stm: TRAP_IF(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 42, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x19); // p.stm = 25
    }
    if ( // stm: TRAP_IF(r,INT_CONSTANT)
      rchild.getOpcode() == INT_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 113, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x1A); // p.stm = 26
      }
    }
    if ( // stm: TRAP_IF(r,LONG_CONSTANT)
      rchild.getOpcode() == LONG_CONSTANT_opcode  
    ) {
      c = STATE(lchild).getCost(2 /* r */) + 10;
      if(BURS.DEBUG) trace(p, 114, c + 0, p.getCost(1) /* stm */);
      if (c < p.getCost(1) /* stm */) {
        p.setCost(1 /* stm */, (char)(c));
        p.writePacked(0, 0xFFFFFF80, 0x1B); // p.stm = 27
      }
    }
  }

  /**
   * Labels TRAP tree node
   * @param p node to label
   */
  private static void label_TRAP(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: TRAP
    if(BURS.DEBUG) trace(p, 29, 10 + 0, p.getCost(1) /* stm */);
    if (10 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(10));
      p.writePacked(0, 0xFFFFFF80, 0x18); // p.stm = 24
    }
  }

  /**
   * Labels ILLEGAL_INSTRUCTION tree node
   * @param p node to label
   */
  private static void label_ILLEGAL_INSTRUCTION(AbstractBURS_TreeNode p) {
    p.initCost();
    // stm: ILLEGAL_INSTRUCTION
    if(BURS.DEBUG) trace(p, 28, 11 + 0, p.getCost(1) /* stm */);
    if (11 < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(11));
      p.writePacked(0, 0xFFFFFF80, 0x17); // p.stm = 23
    }
  }

  /**
   * Labels FLOAT_AS_INT_BITS tree node
   * @param p node to label
   */
  private static void label_FLOAT_AS_INT_BITS(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: FLOAT_AS_INT_BITS(r)
    c = STATE(lchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 156, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2500); // p.r = 74
      closure_r(p, c);
    }
  }

  /**
   * Labels INT_BITS_AS_FLOAT tree node
   * @param p node to label
   */
  private static void label_INT_BITS_AS_FLOAT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: INT_BITS_AS_FLOAT(r)
    c = STATE(lchild).getCost(2 /* r */) + 20;
    if(BURS.DEBUG) trace(p, 157, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2580); // p.r = 75
      closure_r(p, c);
    }
  }

  /**
   * Labels DOUBLE_AS_LONG_BITS tree node
   * @param p node to label
   */
  private static void label_DOUBLE_AS_LONG_BITS(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: DOUBLE_AS_LONG_BITS(r)
    c = STATE(lchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 185, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3780); // p.r = 111
      closure_r(p, c);
    }
  }

  /**
   * Labels LONG_BITS_AS_DOUBLE tree node
   * @param p node to label
   */
  private static void label_LONG_BITS_AS_DOUBLE(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // r: LONG_BITS_AS_DOUBLE(r)
    c = STATE(lchild).getCost(2 /* r */) + 40;
    if(BURS.DEBUG) trace(p, 186, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x3800); // p.r = 112
      closure_r(p, c);
    }
  }

  /**
   * Labels FRAMESIZE tree node
   * @param p node to label
   */
  private static void label_FRAMESIZE(AbstractBURS_TreeNode p) {
    p.initCost();
    // r: FRAMESIZE
    if(BURS.DEBUG) trace(p, 20, 10 + 0, p.getCost(2) /* r */);
    if (10 < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(10));
      p.writePacked(0, 0xFFFFC07F, 0x280); // p.r = 5
      closure_r(p, 10);
    }
  }

  /**
   * Labels LOWTABLESWITCH tree node
   * @param p node to label
   */
  private static void label_LOWTABLESWITCH(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: LOWTABLESWITCH(r)
    c = STATE(lchild).getCost(2 /* r */) + 10;
    if(BURS.DEBUG) trace(p, 103, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x9); // p.stm = 9
    }
  }

  /**
   * Labels ADDRESS_CONSTANT tree node
   * @param p node to label
   */
  private static void label_ADDRESS_CONSTANT(AbstractBURS_TreeNode p) {
    p.initCost();
    // any: ADDRESS_CONSTANT
    if(BURS.DEBUG) trace(p, 10, 0 + 0, p.getCost(7) /* any */);
    if (0 < p.getCost(7) /* any */) {
      p.setCost(7 /* any */, (char)(0));
      p.writePacked(0, 0x8FFFFFFF, 0x30000000); // p.any = 3
    }
  }

  /**
   * Labels INT_CONSTANT tree node
   * @param p node to label
   */
  private static void label_INT_CONSTANT(AbstractBURS_TreeNode p) {
    p.initCost();
    // any: INT_CONSTANT
    if(BURS.DEBUG) trace(p, 11, 0 + 0, p.getCost(7) /* any */);
    if (0 < p.getCost(7) /* any */) {
      p.setCost(7 /* any */, (char)(0));
      p.writePacked(0, 0x8FFFFFFF, 0x40000000); // p.any = 4
    }
  }

  /**
   * Labels LONG_CONSTANT tree node
   * @param p node to label
   */
  private static void label_LONG_CONSTANT(AbstractBURS_TreeNode p) {
    p.initCost();
    // any: LONG_CONSTANT
    if(BURS.DEBUG) trace(p, 12, 0 + 0, p.getCost(7) /* any */);
    if (0 < p.getCost(7) /* any */) {
      p.setCost(7 /* any */, (char)(0));
      p.writePacked(0, 0x8FFFFFFF, 0x50000000); // p.any = 5
    }
  }

  /**
   * Labels REGISTER tree node
   * @param p node to label
   */
  private static void label_REGISTER(AbstractBURS_TreeNode p) {
    p.initCost();
    // r: REGISTER
    if(BURS.DEBUG) trace(p, 8, 0 + 0, p.getCost(2) /* r */);
    if (0 < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(0));
      p.writePacked(0, 0xFFFFC07F, 0x80); // p.r = 1
      closure_r(p, 0);
    }
  }

  /**
   * Labels OTHER_OPERAND tree node
   * @param p node to label
   */
  private static void label_OTHER_OPERAND(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild, rchild;
    lchild = p.getChild1();
    rchild = p.getChild2();
    label(lchild);
    label(rchild);
    int c;
    // any: OTHER_OPERAND(any,any)
    c = STATE(lchild).getCost(7 /* any */) + STATE(rchild).getCost(7 /* any */) + 0;
    if(BURS.DEBUG) trace(p, 41, c + 0, p.getCost(7) /* any */);
    if (c < p.getCost(7) /* any */) {
      p.setCost(7 /* any */, (char)(c));
      p.writePacked(0, 0x8FFFFFFF, 0x60000000); // p.any = 6
    }
    // r: OTHER_OPERAND(r,r)
    c = STATE(lchild).getCost(2 /* r */) + STATE(rchild).getCost(2 /* r */) + 0;
    if(BURS.DEBUG) trace(p, 83, c + 0, p.getCost(2) /* r */);
    if (c < p.getCost(2) /* r */) {
      p.setCost(2 /* r */, (char)(c));
      p.writePacked(0, 0xFFFFC07F, 0x2C80); // p.r = 89
      closure_r(p, c);
    }
  }

  /**
   * Labels NULL tree node
   * @param p node to label
   */
  private static void label_NULL(AbstractBURS_TreeNode p) {
    p.initCost();
    // any: NULL
    if(BURS.DEBUG) trace(p, 9, 0 + 0, p.getCost(7) /* any */);
    if (0 < p.getCost(7) /* any */) {
      p.setCost(7 /* any */, (char)(0));
      p.writePacked(0, 0x8FFFFFFF, 0x10000000); // p.any = 1
    }
  }

  /**
   * Labels BRANCH_TARGET tree node
   * @param p node to label
   */
  private static void label_BRANCH_TARGET(AbstractBURS_TreeNode p) {
    p.initCost();
  }

  /**
   * Labels DCBF tree node
   * @param p node to label
   */
  private static void label_DCBF(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: DCBF(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 106, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x10); // p.stm = 16
    }
  }

  /**
   * Labels DCBST tree node
   * @param p node to label
   */
  private static void label_DCBST(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: DCBST(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 107, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x11); // p.stm = 17
    }
  }

  /**
   * Labels DCBT tree node
   * @param p node to label
   */
  private static void label_DCBT(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: DCBT(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 108, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x12); // p.stm = 18
    }
  }

  /**
   * Labels DCBTST tree node
   * @param p node to label
   */
  private static void label_DCBTST(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: DCBTST(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 109, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x13); // p.stm = 19
    }
  }

  /**
   * Labels DCBZ tree node
   * @param p node to label
   */
  private static void label_DCBZ(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: DCBZ(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 110, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x14); // p.stm = 20
    }
  }

  /**
   * Labels DCBZL tree node
   * @param p node to label
   */
  private static void label_DCBZL(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: DCBZL(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 111, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x15); // p.stm = 21
    }
  }

  /**
   * Labels ICBI tree node
   * @param p node to label
   */
  private static void label_ICBI(AbstractBURS_TreeNode p) {
    p.initCost();
    AbstractBURS_TreeNode lchild;
    lchild = p.getChild1();
    label(lchild);
    int c;
    // stm: ICBI(r)
    c = STATE(lchild).getCost(2 /* r */) + 11;
    if(BURS.DEBUG) trace(p, 112, c + 0, p.getCost(1) /* stm */);
    if (c < p.getCost(1) /* stm */) {
      p.setCost(1 /* stm */, (char)(c));
      p.writePacked(0, 0xFFFFFF80, 0x16); // p.stm = 22
    }
  }

  /**
   * Give leaf child corresponding to external rule and child number.
   * e.g. .
   *
   * @param p tree node to get child for
   * @param eruleno external rule number
   * @param kidnumber the child to return
   * @return the requested child
   */
  private static AbstractBURS_TreeNode kids(AbstractBURS_TreeNode p, int eruleno, int kidnumber)  { 
    if (BURS.DEBUG) {
      switch (eruleno) {
      case 7: // any: r
      case 6: // rz: rp
      case 5: // rs: rp
      case 4: // r: rz
      case 3: // r: rs
      case 2: // r: czr
      case 1: // stm: r
        if (kidnumber == 0) {
          return p;
        }
        break;
      case 40: // r: REF_MOVE(ADDRESS_CONSTANT)
      case 39: // r: REF_MOVE(ADDRESS_CONSTANT)
      case 38: // r: REF_MOVE(ADDRESS_CONSTANT)
      case 37: // r: LONG_MOVE(LONG_CONSTANT)
      case 36: // stm: IR_PROLOGUE
      case 35: // r: GET_TIME_BASE
      case 34: // stm: RETURN(NULL)
      case 33: // stm: GOTO
      case 32: // rs: REF_MOVE(INT_CONSTANT)
      case 31: // rs: REF_MOVE(INT_CONSTANT)
      case 30: // rs: REF_MOVE(INT_CONSTANT)
      case 29: // stm: TRAP
      case 28: // stm: ILLEGAL_INSTRUCTION
      case 27: // stm: READ_CEILING
      case 26: // stm: WRITE_FLOOR
      case 25: // stm: FENCE
      case 24: // r: GET_CAUGHT_EXCEPTION
      case 23: // r: GUARD_COMBINE
      case 22: // r: GUARD_MOVE
      case 21: // stm: NOP
      case 20: // r: FRAMESIZE
      case 19: // stm: YIELDPOINT_BACKEDGE
      case 18: // stm: YIELDPOINT_EPILOGUE
      case 17: // stm: YIELDPOINT_PROLOGUE
      case 16: // stm: UNINT_END
      case 15: // stm: UNINT_BEGIN
      case 14: // stm: IG_PATCH_POINT
      case 13: // stm: RESOLVE
      case 12: // any: LONG_CONSTANT
      case 11: // any: INT_CONSTANT
      case 10: // any: ADDRESS_CONSTANT
      case 9: // any: NULL
      case 8: // r: REGISTER
        break;
      case 102: // r: ATTEMPT_ADDR(r,r)
      case 101: // r: PREPARE_ADDR(r,r)
      case 100: // r: LONG_LOAD(r,r)
      case 99: // stm: LONG_IFCMP(r,r)
      case 98: // stm: LONG_CMP(r,r)
      case 97: // r: LONG_XOR(r,r)
      case 96: // r: LONG_OR(r,r)
      case 95: // r: LONG_AND(r,r)
      case 94: // r: LONG_USHR(r,r)
      case 93: // r: LONG_SHR(r,r)
      case 92: // r: LONG_SHL(r,r)
      case 91: // r: LONG_MUL(r,r)
      case 90: // r: LONG_SUB(r,r)
      case 89: // r: LONG_ADD(r,r)
      case 88: // r: ATTEMPT_LONG(r,r)
      case 87: // r: ATTEMPT_INT(r,r)
      case 86: // r: PREPARE_LONG(r,r)
      case 85: // r: PREPARE_INT(r,r)
      case 84: // r: YIELDPOINT_OSR(any,any)
      case 83: // r: OTHER_OPERAND(r,r)
      case 82: // r: SYSCALL(r,any)
      case 81: // r: CALL(r,any)
      case 80: // stm: DOUBLE_CMPG(r,r)
      case 79: // stm: DOUBLE_CMPL(r,r)
      case 78: // stm: FLOAT_CMPG(r,r)
      case 77: // stm: FLOAT_CMPL(r,r)
      case 76: // stm: DOUBLE_IFCMP(r,r)
      case 75: // stm: FLOAT_IFCMP(r,r)
      case 74: // stm: INT_IFCMP2(r,r)
      case 73: // stm: INT_IFCMP(r,r)
      case 72: // rs: INT_LOAD(r,r)
      case 71: // r: DOUBLE_LOAD(r,r)
      case 70: // r: FLOAT_LOAD(r,r)
      case 69: // rp: USHORT_LOAD(r,r)
      case 68: // rs: SHORT_LOAD(r,r)
      case 67: // rp: UBYTE_LOAD(r,r)
      case 66: // rs: BYTE_LOAD(r,r)
      case 65: // r: DOUBLE_DIV(r,r)
      case 64: // r: FLOAT_DIV(r,r)
      case 63: // r: DOUBLE_SUB(r,r)
      case 62: // r: FLOAT_SUB(r,r)
      case 61: // r: DOUBLE_MUL(r,r)
      case 60: // r: FLOAT_MUL(r,r)
      case 59: // r: DOUBLE_ADD(r,r)
      case 58: // r: FLOAT_ADD(r,r)
      case 57: // r: REF_XOR(r,r)
      case 56: // r: REF_OR(r,r)
      case 55: // r: REF_AND(r,r)
      case 54: // rz: INT_USHR(r,r)
      case 53: // rs: INT_SHR(r,r)
      case 52: // rz: INT_SHL(r,r)
      case 51: // r: INT_REM(r,r)
      case 50: // r: INT_DIV(r,r)
      case 49: // r: INT_MUL(r,r)
      case 48: // r: REF_SUB(r,r)
      case 47: // r: REF_ADD(r,r)
      case 46: // boolcmp: BOOLEAN_CMP_ADDR(r,r)
      case 45: // r: BOOLEAN_CMP_ADDR(r,r)
      case 44: // boolcmp: BOOLEAN_CMP_INT(r,r)
      case 43: // r: BOOLEAN_CMP_INT(r,r)
      case 42: // stm: TRAP_IF(r,r)
      case 41: // any: OTHER_OPERAND(any,any)
        if (kidnumber == 0) {
          return p.getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild2();
        }
        break;
      case 194: // r: LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      case 193: // r: LONG_LOAD(r,INT_CONSTANT)
      case 192: // r: REF_XOR(r,REF_MOVE(INT_CONSTANT))
      case 191: // r: REF_OR(r,REF_MOVE(INT_CONSTANT))
      case 190: // r: REF_OR(r,REF_MOVE(INT_CONSTANT))
      case 189: // r: REF_AND(r,REF_MOVE(INT_CONSTANT))
      case 188: // czr: REF_AND(r,REF_MOVE(INT_CONSTANT))
      case 187: // r: LONG_MOVE(r)
      case 186: // r: LONG_BITS_AS_DOUBLE(r)
      case 185: // r: DOUBLE_AS_LONG_BITS(r)
      case 184: // r: LONG_2INT(r)
      case 183: // r: INT_2LONG(r)
      case 182: // r: LONG_NOT(r)
      case 181: // r: LONG_USHR(r,INT_CONSTANT)
      case 180: // r: LONG_SHR(r,INT_CONSTANT)
      case 179: // r: LONG_SHL(r,INT_CONSTANT)
      case 178: // r: LONG_NEG(r)
      case 177: // stm: RETURN(r)
      case 176: // stm: INT_IFCMP2(r,INT_CONSTANT)
      case 175: // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      case 174: // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      case 173: // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      case 172: // stm: INT_IFCMP(boolcmp,INT_CONSTANT)
      case 171: // stm: INT_IFCMP(r,INT_CONSTANT)
      case 170: // rs: INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      case 169: // rs: INT_LOAD(r,INT_CONSTANT)
      case 168: // r: DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      case 167: // r: DOUBLE_LOAD(r,INT_CONSTANT)
      case 166: // r: FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
      case 165: // r: FLOAT_LOAD(r,INT_CONSTANT)
      case 164: // rp: USHORT_LOAD(r,INT_CONSTANT)
      case 163: // rs: SHORT_LOAD(r,INT_CONSTANT)
      case 162: // rp: UBYTE_LOAD(r,INT_CONSTANT)
      case 161: // rs: BYTE_LOAD(r,INT_CONSTANT)
      case 160: // r: DOUBLE_MOVE(r)
      case 159: // r: FLOAT_MOVE(r)
      case 158: // r: REF_MOVE(r)
      case 157: // r: INT_BITS_AS_FLOAT(r)
      case 156: // r: FLOAT_AS_INT_BITS(r)
      case 155: // r: DOUBLE_2FLOAT(r)
      case 154: // r: DOUBLE_2INT(r)
      case 153: // r: FLOAT_2DOUBLE(r)
      case 152: // r: FLOAT_2INT(r)
      case 151: // r: INT_2DOUBLE(r)
      case 150: // r: INT_2FLOAT(r)
      case 149: // rs: INT_2SHORT(r)
      case 148: // rp: INT_2USHORT(r)
      case 147: // rs: INT_2BYTE(r)
      case 146: // r: DOUBLE_SQRT(r)
      case 145: // r: FLOAT_SQRT(r)
      case 144: // r: DOUBLE_NEG(r)
      case 143: // r: FLOAT_NEG(r)
      case 142: // r: REF_NOT(r)
      case 141: // r: REF_XOR(r,INT_CONSTANT)
      case 140: // r: REF_OR(r,INT_CONSTANT)
      case 139: // rp: REF_AND(r,INT_CONSTANT)
      case 138: // czr: REF_AND(r,INT_CONSTANT)
      case 137: // rp: INT_USHR(r,INT_CONSTANT)
      case 136: // rs: INT_SHR(r,INT_CONSTANT)
      case 135: // rz: INT_SHL(r,INT_CONSTANT)
      case 134: // r: REF_NEG(r)
      case 133: // r: INT_REM(r,REF_MOVE(INT_CONSTANT))
      case 132: // r: INT_DIV(r,REF_MOVE(INT_CONSTANT))
      case 131: // r: INT_MUL(r,INT_CONSTANT)
      case 130: // r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
      case 129: // r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
      case 128: // r: REF_ADD(r,INT_CONSTANT)
      case 127: // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 126: // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 125: // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 124: // r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 123: // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 122: // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 121: // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 120: // boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
      case 119: // boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
      case 118: // r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
      case 117: // boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT)
      case 116: // r: BOOLEAN_CMP_INT(r,INT_CONSTANT)
      case 115: // r: BOOLEAN_NOT(r)
      case 114: // stm: TRAP_IF(r,LONG_CONSTANT)
      case 113: // stm: TRAP_IF(r,INT_CONSTANT)
      case 112: // stm: ICBI(r)
      case 111: // stm: DCBZL(r)
      case 110: // stm: DCBZ(r)
      case 109: // stm: DCBTST(r)
      case 108: // stm: DCBT(r)
      case 107: // stm: DCBST(r)
      case 106: // stm: DCBF(r)
      case 105: // stm: SET_CAUGHT_EXCEPTION(r)
      case 104: // stm: NULL_CHECK(r)
      case 103: // stm: LOWTABLESWITCH(r)
        if (kidnumber == 0) {
          return p.getChild1();
        }
        break;
      case 196: // r: CALL(BRANCH_TARGET,any)
      case 195: // r: REF_SUB(INT_CONSTANT,r)
        if (kidnumber == 0) {
          return p.getChild2();
        }
        break;
      case 216: // stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
      case 215: // stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      case 214: // stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      case 213: // stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT)
      case 212: // stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT)
      case 211: // stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      case 210: // stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT)
      case 209: // stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
      case 208: // stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      case 207: // stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT)
      case 206: // stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT)
      case 205: // rs: INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
      case 204: // rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT)
      case 203: // rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT))
      case 202: // rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
      case 201: // rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
      case 200: // rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
      case 199: // rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      case 198: // rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
      case 197: // rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        break;
      case 221: // stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
      case 220: // stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
      case 219: // stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT))
      case 218: // r: REF_OR(REF_NOT(r),REF_NOT(r))
      case 217: // r: REF_AND(REF_NOT(r),REF_NOT(r))
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild2().getChild1();
        }
        break;
      case 233: // stm: LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      case 232: // stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      case 231: // stm: DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      case 230: // stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      case 229: // stm: FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      case 228: // stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      case 227: // stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
      case 226: // stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      case 225: // stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      case 224: // stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
      case 223: // r: REF_OR(r,REF_NOT(r))
      case 222: // r: REF_AND(r,REF_NOT(r))
        if (kidnumber == 0) {
          return p.getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild2().getChild1();
        }
        break;
      case 247: // stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT)
      case 246: // stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT)
      case 245: // stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT)
      case 244: // stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT)
      case 243: // stm: INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT)
      case 242: // stm: INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT)
      case 241: // stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT)
      case 240: // stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT)
      case 239: // stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT)
      case 238: // rs: INT_LOAD(REF_ADD(r,r),INT_CONSTANT)
      case 237: // rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT)
      case 236: // r: REF_NOT(REF_XOR(r,r))
      case 235: // r: REF_NOT(REF_AND(r,r))
      case 234: // r: REF_NOT(REF_OR(r,r))
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild1().getChild2();
        }
        break;
      case 251: // r: DOUBLE_SUB(DOUBLE_MUL(r,r),r)
      case 250: // r: FLOAT_SUB(FLOAT_MUL(r,r),r)
      case 249: // r: DOUBLE_ADD(DOUBLE_MUL(r,r),r)
      case 248: // r: FLOAT_ADD(FLOAT_MUL(r,r),r)
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild1().getChild2();
        }
        if (kidnumber == 2) {
          return p.getChild2();
        }
        break;
      case 259: // stm: LONG_STORE(r,OTHER_OPERAND(r,r))
      case 258: // stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r))
      case 257: // stm: FLOAT_STORE(r,OTHER_OPERAND(r,r))
      case 256: // stm: INT_STORE(r,OTHER_OPERAND(r,r))
      case 255: // stm: SHORT_STORE(r,OTHER_OPERAND(r,r))
      case 254: // stm: BYTE_STORE(r,OTHER_OPERAND(r,r))
      case 253: // r: DOUBLE_ADD(r,DOUBLE_MUL(r,r))
      case 252: // r: FLOAT_ADD(r,FLOAT_MUL(r,r))
        if (kidnumber == 0) {
          return p.getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild2().getChild1();
        }
        if (kidnumber == 2) {
          return p.getChild2().getChild2();
        }
        break;
      case 263: // r: DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r,r),r))
      case 262: // r: FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r,r),r))
      case 261: // r: DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r,r),r))
      case 260: // r: FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r,r),r))
        if (kidnumber == 0) {
          return p.getChild1().getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild1().getChild1().getChild2();
        }
        if (kidnumber == 2) {
          return p.getChild1().getChild2();
        }
        break;
      case 265: // r: DOUBLE_NEG(DOUBLE_ADD(r,DOUBLE_MUL(r,r)))
      case 264: // r: FLOAT_NEG(FLOAT_ADD(r,FLOAT_MUL(r,r)))
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild1().getChild2().getChild1();
        }
        if (kidnumber == 2) {
          return p.getChild1().getChild2().getChild2();
        }
        break;
      case 268: // stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r))
      case 267: // stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r))
      case 266: // stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r))
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild2().getChild1();
        }
        if (kidnumber == 2) {
          return p.getChild2().getChild2();
        }
        break;
      case 269: // stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT))
        if (kidnumber == 0) {
          return p.getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild2().getChild1().getChild1();
        }
        break;
      case 271: // stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT)
      case 270: // stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT)
        if (kidnumber == 0) {
          return p.getChild1().getChild1();
        }
        if (kidnumber == 1) {
          return p.getChild1().getChild2().getChild1();
        }
        break;
      }
      throw new OptimizingCompilerException("BURS","Bad rule number ",
        Integer.toString(eruleno));
    } else {
      return null;
    }
  }

  /**
   * @param p node whose kids will be marked
   * @param eruleno rule number
   */
  private static void mark_kids(AbstractBURS_TreeNode p, int eruleno)
  {
    byte[] ntsrule = nts[eruleno];
    // 7: any: r
    // 6: rz: rp
    // 5: rs: rp
    // 4: r: rz
    // 3: r: rs
    // 2: r: czr
    // 1: stm: r
    if (eruleno <= 7) {
      if (VM.VerifyAssertions) VM._assert(eruleno > 0);
      mark(p, ntsrule[0]);
    }
    // 40: r: REF_MOVE(ADDRESS_CONSTANT)
    // 39: r: REF_MOVE(ADDRESS_CONSTANT)
    // 38: r: REF_MOVE(ADDRESS_CONSTANT)
    // 37: r: LONG_MOVE(LONG_CONSTANT)
    // 36: stm: IR_PROLOGUE
    // 35: r: GET_TIME_BASE
    // 34: stm: RETURN(NULL)
    // 33: stm: GOTO
    // 32: rs: REF_MOVE(INT_CONSTANT)
    // 31: rs: REF_MOVE(INT_CONSTANT)
    // 30: rs: REF_MOVE(INT_CONSTANT)
    // 29: stm: TRAP
    // 28: stm: ILLEGAL_INSTRUCTION
    // 27: stm: READ_CEILING
    // 26: stm: WRITE_FLOOR
    // 25: stm: FENCE
    // 24: r: GET_CAUGHT_EXCEPTION
    // 23: r: GUARD_COMBINE
    // 22: r: GUARD_MOVE
    // 21: stm: NOP
    // 20: r: FRAMESIZE
    // 19: stm: YIELDPOINT_BACKEDGE
    // 18: stm: YIELDPOINT_EPILOGUE
    // 17: stm: YIELDPOINT_PROLOGUE
    // 16: stm: UNINT_END
    // 15: stm: UNINT_BEGIN
    // 14: stm: IG_PATCH_POINT
    // 13: stm: RESOLVE
    // 12: any: LONG_CONSTANT
    // 11: any: INT_CONSTANT
    // 10: any: ADDRESS_CONSTANT
    // 9: any: NULL
    // 8: r: REGISTER
    else if (eruleno <= 40) {
    }
    // 102: r: ATTEMPT_ADDR(r,r)
    // 101: r: PREPARE_ADDR(r,r)
    // 100: r: LONG_LOAD(r,r)
    // 99: stm: LONG_IFCMP(r,r)
    // 98: stm: LONG_CMP(r,r)
    // 97: r: LONG_XOR(r,r)
    // 96: r: LONG_OR(r,r)
    // 95: r: LONG_AND(r,r)
    // 94: r: LONG_USHR(r,r)
    // 93: r: LONG_SHR(r,r)
    // 92: r: LONG_SHL(r,r)
    // 91: r: LONG_MUL(r,r)
    // 90: r: LONG_SUB(r,r)
    // 89: r: LONG_ADD(r,r)
    // 88: r: ATTEMPT_LONG(r,r)
    // 87: r: ATTEMPT_INT(r,r)
    // 86: r: PREPARE_LONG(r,r)
    // 85: r: PREPARE_INT(r,r)
    // 84: r: YIELDPOINT_OSR(any,any)
    // 83: r: OTHER_OPERAND(r,r)
    // 82: r: SYSCALL(r,any)
    // 81: r: CALL(r,any)
    // 80: stm: DOUBLE_CMPG(r,r)
    // 79: stm: DOUBLE_CMPL(r,r)
    // 78: stm: FLOAT_CMPG(r,r)
    // 77: stm: FLOAT_CMPL(r,r)
    // 76: stm: DOUBLE_IFCMP(r,r)
    // 75: stm: FLOAT_IFCMP(r,r)
    // 74: stm: INT_IFCMP2(r,r)
    // 73: stm: INT_IFCMP(r,r)
    // 72: rs: INT_LOAD(r,r)
    // 71: r: DOUBLE_LOAD(r,r)
    // 70: r: FLOAT_LOAD(r,r)
    // 69: rp: USHORT_LOAD(r,r)
    // 68: rs: SHORT_LOAD(r,r)
    // 67: rp: UBYTE_LOAD(r,r)
    // 66: rs: BYTE_LOAD(r,r)
    // 65: r: DOUBLE_DIV(r,r)
    // 64: r: FLOAT_DIV(r,r)
    // 63: r: DOUBLE_SUB(r,r)
    // 62: r: FLOAT_SUB(r,r)
    // 61: r: DOUBLE_MUL(r,r)
    // 60: r: FLOAT_MUL(r,r)
    // 59: r: DOUBLE_ADD(r,r)
    // 58: r: FLOAT_ADD(r,r)
    // 57: r: REF_XOR(r,r)
    // 56: r: REF_OR(r,r)
    // 55: r: REF_AND(r,r)
    // 54: rz: INT_USHR(r,r)
    // 53: rs: INT_SHR(r,r)
    // 52: rz: INT_SHL(r,r)
    // 51: r: INT_REM(r,r)
    // 50: r: INT_DIV(r,r)
    // 49: r: INT_MUL(r,r)
    // 48: r: REF_SUB(r,r)
    // 47: r: REF_ADD(r,r)
    // 46: boolcmp: BOOLEAN_CMP_ADDR(r,r)
    // 45: r: BOOLEAN_CMP_ADDR(r,r)
    // 44: boolcmp: BOOLEAN_CMP_INT(r,r)
    // 43: r: BOOLEAN_CMP_INT(r,r)
    // 42: stm: TRAP_IF(r,r)
    // 41: any: OTHER_OPERAND(any,any)
    else if (eruleno <= 102) {
      mark(p.getChild1(), ntsrule[0]);
      mark(p.getChild2(), ntsrule[1]);
    }
    // 194: r: LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    // 193: r: LONG_LOAD(r,INT_CONSTANT)
    // 192: r: REF_XOR(r,REF_MOVE(INT_CONSTANT))
    // 191: r: REF_OR(r,REF_MOVE(INT_CONSTANT))
    // 190: r: REF_OR(r,REF_MOVE(INT_CONSTANT))
    // 189: r: REF_AND(r,REF_MOVE(INT_CONSTANT))
    // 188: czr: REF_AND(r,REF_MOVE(INT_CONSTANT))
    // 187: r: LONG_MOVE(r)
    // 186: r: LONG_BITS_AS_DOUBLE(r)
    // 185: r: DOUBLE_AS_LONG_BITS(r)
    // 184: r: LONG_2INT(r)
    // 183: r: INT_2LONG(r)
    // 182: r: LONG_NOT(r)
    // 181: r: LONG_USHR(r,INT_CONSTANT)
    // 180: r: LONG_SHR(r,INT_CONSTANT)
    // 179: r: LONG_SHL(r,INT_CONSTANT)
    // 178: r: LONG_NEG(r)
    // 177: stm: RETURN(r)
    // 176: stm: INT_IFCMP2(r,INT_CONSTANT)
    // 175: stm: INT_IFCMP(boolcmp,INT_CONSTANT)
    // 174: stm: INT_IFCMP(boolcmp,INT_CONSTANT)
    // 173: stm: INT_IFCMP(boolcmp,INT_CONSTANT)
    // 172: stm: INT_IFCMP(boolcmp,INT_CONSTANT)
    // 171: stm: INT_IFCMP(r,INT_CONSTANT)
    // 170: rs: INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    // 169: rs: INT_LOAD(r,INT_CONSTANT)
    // 168: r: DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    // 167: r: DOUBLE_LOAD(r,INT_CONSTANT)
    // 166: r: FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    // 165: r: FLOAT_LOAD(r,INT_CONSTANT)
    // 164: rp: USHORT_LOAD(r,INT_CONSTANT)
    // 163: rs: SHORT_LOAD(r,INT_CONSTANT)
    // 162: rp: UBYTE_LOAD(r,INT_CONSTANT)
    // 161: rs: BYTE_LOAD(r,INT_CONSTANT)
    // 160: r: DOUBLE_MOVE(r)
    // 159: r: FLOAT_MOVE(r)
    // 158: r: REF_MOVE(r)
    // 157: r: INT_BITS_AS_FLOAT(r)
    // 156: r: FLOAT_AS_INT_BITS(r)
    // 155: r: DOUBLE_2FLOAT(r)
    // 154: r: DOUBLE_2INT(r)
    // 153: r: FLOAT_2DOUBLE(r)
    // 152: r: FLOAT_2INT(r)
    // 151: r: INT_2DOUBLE(r)
    // 150: r: INT_2FLOAT(r)
    // 149: rs: INT_2SHORT(r)
    // 148: rp: INT_2USHORT(r)
    // 147: rs: INT_2BYTE(r)
    // 146: r: DOUBLE_SQRT(r)
    // 145: r: FLOAT_SQRT(r)
    // 144: r: DOUBLE_NEG(r)
    // 143: r: FLOAT_NEG(r)
    // 142: r: REF_NOT(r)
    // 141: r: REF_XOR(r,INT_CONSTANT)
    // 140: r: REF_OR(r,INT_CONSTANT)
    // 139: rp: REF_AND(r,INT_CONSTANT)
    // 138: czr: REF_AND(r,INT_CONSTANT)
    // 137: rp: INT_USHR(r,INT_CONSTANT)
    // 136: rs: INT_SHR(r,INT_CONSTANT)
    // 135: rz: INT_SHL(r,INT_CONSTANT)
    // 134: r: REF_NEG(r)
    // 133: r: INT_REM(r,REF_MOVE(INT_CONSTANT))
    // 132: r: INT_DIV(r,REF_MOVE(INT_CONSTANT))
    // 131: r: INT_MUL(r,INT_CONSTANT)
    // 130: r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
    // 129: r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
    // 128: r: REF_ADD(r,INT_CONSTANT)
    // 127: r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 126: r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 125: r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 124: r: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 123: boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 122: boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 121: boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 120: boolcmp: BOOLEAN_CMP_INT(boolcmp,INT_CONSTANT)
    // 119: boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
    // 118: r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
    // 117: boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT)
    // 116: r: BOOLEAN_CMP_INT(r,INT_CONSTANT)
    // 115: r: BOOLEAN_NOT(r)
    // 114: stm: TRAP_IF(r,LONG_CONSTANT)
    // 113: stm: TRAP_IF(r,INT_CONSTANT)
    // 112: stm: ICBI(r)
    // 111: stm: DCBZL(r)
    // 110: stm: DCBZ(r)
    // 109: stm: DCBTST(r)
    // 108: stm: DCBT(r)
    // 107: stm: DCBST(r)
    // 106: stm: DCBF(r)
    // 105: stm: SET_CAUGHT_EXCEPTION(r)
    // 104: stm: NULL_CHECK(r)
    // 103: stm: LOWTABLESWITCH(r)
    else if (eruleno <= 194) {
      mark(p.getChild1(), ntsrule[0]);
    }
    // 196: r: CALL(BRANCH_TARGET,any)
    // 195: r: REF_SUB(INT_CONSTANT,r)
    else if (eruleno <= 196) {
      mark(p.getChild2(), ntsrule[0]);
    }
    // 216: stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
    // 215: stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
    // 214: stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
    // 213: stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT)
    // 212: stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT)
    // 211: stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
    // 210: stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT)
    // 209: stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
    // 208: stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
    // 207: stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT)
    // 206: stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT)
    // 205: rs: INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
    // 204: rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT)
    // 203: rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT))
    // 202: rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
    // 201: rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
    // 200: rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
    // 199: rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
    // 198: rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
    // 197: rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
    else if (eruleno <= 216) {
      mark(p.getChild1().getChild1(), ntsrule[0]);
    }
    // 221: stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
    // 220: stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
    // 219: stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT))
    // 218: r: REF_OR(REF_NOT(r),REF_NOT(r))
    // 217: r: REF_AND(REF_NOT(r),REF_NOT(r))
    else if (eruleno <= 221) {
      mark(p.getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild2().getChild1(), ntsrule[1]);
    }
    // 233: stm: LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    // 232: stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    // 231: stm: DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    // 230: stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    // 229: stm: FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    // 228: stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    // 227: stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    // 226: stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    // 225: stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    // 224: stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    // 223: r: REF_OR(r,REF_NOT(r))
    // 222: r: REF_AND(r,REF_NOT(r))
    else if (eruleno <= 233) {
      mark(p.getChild1(), ntsrule[0]);
      mark(p.getChild2().getChild1(), ntsrule[1]);
    }
    // 247: stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT)
    // 246: stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT)
    // 245: stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT)
    // 244: stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT)
    // 243: stm: INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT)
    // 242: stm: INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT)
    // 241: stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT)
    // 240: stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT)
    // 239: stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT)
    // 238: rs: INT_LOAD(REF_ADD(r,r),INT_CONSTANT)
    // 237: rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT)
    // 236: r: REF_NOT(REF_XOR(r,r))
    // 235: r: REF_NOT(REF_AND(r,r))
    // 234: r: REF_NOT(REF_OR(r,r))
    else if (eruleno <= 247) {
      mark(p.getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild1().getChild2(), ntsrule[1]);
    }
    // 251: r: DOUBLE_SUB(DOUBLE_MUL(r,r),r)
    // 250: r: FLOAT_SUB(FLOAT_MUL(r,r),r)
    // 249: r: DOUBLE_ADD(DOUBLE_MUL(r,r),r)
    // 248: r: FLOAT_ADD(FLOAT_MUL(r,r),r)
    else if (eruleno <= 251) {
      mark(p.getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild1().getChild2(), ntsrule[1]);
      mark(p.getChild2(), ntsrule[2]);
    }
    // 259: stm: LONG_STORE(r,OTHER_OPERAND(r,r))
    // 258: stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r))
    // 257: stm: FLOAT_STORE(r,OTHER_OPERAND(r,r))
    // 256: stm: INT_STORE(r,OTHER_OPERAND(r,r))
    // 255: stm: SHORT_STORE(r,OTHER_OPERAND(r,r))
    // 254: stm: BYTE_STORE(r,OTHER_OPERAND(r,r))
    // 253: r: DOUBLE_ADD(r,DOUBLE_MUL(r,r))
    // 252: r: FLOAT_ADD(r,FLOAT_MUL(r,r))
    else if (eruleno <= 259) {
      mark(p.getChild1(), ntsrule[0]);
      mark(p.getChild2().getChild1(), ntsrule[1]);
      mark(p.getChild2().getChild2(), ntsrule[2]);
    }
    // 263: r: DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r,r),r))
    // 262: r: FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r,r),r))
    // 261: r: DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r,r),r))
    // 260: r: FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r,r),r))
    else if (eruleno <= 263) {
      mark(p.getChild1().getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild1().getChild1().getChild2(), ntsrule[1]);
      mark(p.getChild1().getChild2(), ntsrule[2]);
    }
    // 265: r: DOUBLE_NEG(DOUBLE_ADD(r,DOUBLE_MUL(r,r)))
    // 264: r: FLOAT_NEG(FLOAT_ADD(r,FLOAT_MUL(r,r)))
    else if (eruleno <= 265) {
      mark(p.getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild1().getChild2().getChild1(), ntsrule[1]);
      mark(p.getChild1().getChild2().getChild2(), ntsrule[2]);
    }
    // 268: stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r))
    // 267: stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r))
    // 266: stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r))
    else if (eruleno <= 268) {
      mark(p.getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild2().getChild1(), ntsrule[1]);
      mark(p.getChild2().getChild2(), ntsrule[2]);
    }
    // 269: stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT))
    else if (eruleno <= 269) {
      mark(p.getChild1(), ntsrule[0]);
      mark(p.getChild2().getChild1().getChild1(), ntsrule[1]);
    }
    // 271: stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT)
    // 270: stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT)
    else {
      if (VM.VerifyAssertions) VM._assert(eruleno <= 271);
      mark(p.getChild1().getChild1(), ntsrule[0]);
      mark(p.getChild1().getChild2().getChild1(), ntsrule[1]);
    }
  }

  /**
   * For each BURS rule (the number of which provides the index) give its flags byte
   */
  private static final byte[] action={
    0,
    NOFLAGS, // 1 - stm: r
    NOFLAGS, // 2 - r: REGISTER
    NOFLAGS, // 3 - r: czr
    NOFLAGS, // 4 - r: rs
    NOFLAGS, // 5 - r: rz
    NOFLAGS, // 6 - rs: rp
    NOFLAGS, // 7 - rz: rp
    NOFLAGS, // 8 - any: NULL
    NOFLAGS, // 9 - any: r
    NOFLAGS, // 10 - any: ADDRESS_CONSTANT
    NOFLAGS, // 11 - any: INT_CONSTANT
    NOFLAGS, // 12 - any: LONG_CONSTANT
    NOFLAGS, // 13 - any: OTHER_OPERAND(any, any)
    EMIT_INSTRUCTION, // 14 - stm: RESOLVE
    EMIT_INSTRUCTION, // 15 - stm: IG_PATCH_POINT
    EMIT_INSTRUCTION, // 16 - stm: UNINT_BEGIN
    EMIT_INSTRUCTION, // 17 - stm: UNINT_END
    EMIT_INSTRUCTION, // 18 - stm: YIELDPOINT_PROLOGUE
    EMIT_INSTRUCTION, // 19 - stm: YIELDPOINT_EPILOGUE
    EMIT_INSTRUCTION, // 20 - stm: YIELDPOINT_BACKEDGE
    EMIT_INSTRUCTION, // 21 - r: FRAMESIZE
    EMIT_INSTRUCTION, // 22 - stm: LOWTABLESWITCH(r)
    NOFLAGS, // 23 - stm: NOP
    EMIT_INSTRUCTION, // 24 - r: GUARD_MOVE
    EMIT_INSTRUCTION, // 25 - r: GUARD_COMBINE
    EMIT_INSTRUCTION, // 26 - stm: NULL_CHECK(r)
    EMIT_INSTRUCTION, // 27 - r: GET_CAUGHT_EXCEPTION
    EMIT_INSTRUCTION, // 28 - stm: SET_CAUGHT_EXCEPTION(r)
    EMIT_INSTRUCTION, // 29 - stm: FENCE
    EMIT_INSTRUCTION, // 30 - stm: WRITE_FLOOR
    EMIT_INSTRUCTION, // 31 - stm: READ_CEILING
    EMIT_INSTRUCTION, // 32 - stm: DCBF(r)
    EMIT_INSTRUCTION, // 33 - stm: DCBST(r)
    EMIT_INSTRUCTION, // 34 - stm: DCBT(r)
    EMIT_INSTRUCTION, // 35 - stm: DCBTST(r)
    EMIT_INSTRUCTION, // 36 - stm: DCBZ(r)
    EMIT_INSTRUCTION, // 37 - stm: DCBZL(r)
    EMIT_INSTRUCTION, // 38 - stm: ICBI(r)
    EMIT_INSTRUCTION, // 39 - stm:   ILLEGAL_INSTRUCTION
    EMIT_INSTRUCTION, // 40 - stm: TRAP
    EMIT_INSTRUCTION, // 41 - stm: TRAP_IF(r,r)
    EMIT_INSTRUCTION, // 42 - stm: TRAP_IF(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 43 - stm: TRAP_IF(r,LONG_CONSTANT)
    EMIT_INSTRUCTION, // 44 - r: BOOLEAN_NOT(r)
    EMIT_INSTRUCTION, // 45 - r: BOOLEAN_CMP_INT(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 46 - r: BOOLEAN_CMP_INT(r,r)
    EMIT_INSTRUCTION, // 47 - boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 48 - boolcmp: BOOLEAN_CMP_INT(r,r)
    EMIT_INSTRUCTION, // 49 - r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 50 - r: BOOLEAN_CMP_ADDR(r,r)
    EMIT_INSTRUCTION, // 51 - boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 52 - boolcmp: BOOLEAN_CMP_ADDR(r,r)
    NOFLAGS, // 53 - boolcmp: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 54 - boolcmp: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    NOFLAGS, // 55 - boolcmp: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 56 - boolcmp: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 57 - r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 58 - r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 59 - r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 60 - r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 61 - r: REF_ADD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 62 - r: REF_ADD(r,r)
    EMIT_INSTRUCTION, // 63 - r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 64 - r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 65 - r: REF_SUB(r,r)
    EMIT_INSTRUCTION, // 66 - r: REF_SUB(INT_CONSTANT,r)
    EMIT_INSTRUCTION, // 67 - r: INT_MUL(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 68 - r: INT_MUL(r,r)
    EMIT_INSTRUCTION, // 69 - r: INT_DIV(r,r)
    EMIT_INSTRUCTION, // 70 - r: INT_DIV(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 71 - r: INT_REM(r,r)
    EMIT_INSTRUCTION, // 72 - r: INT_REM(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 73 - r: REF_NEG(r)
    EMIT_INSTRUCTION, // 74 - rz: INT_SHL(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 75 - rz: INT_SHL(r,r)
    EMIT_INSTRUCTION, // 76 - rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 77 - rs: INT_SHR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 78 - rs: INT_SHR(r,r)
    EMIT_INSTRUCTION, // 79 - rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 80 - rp: INT_USHR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 81 - rz: INT_USHR(r,r)
    EMIT_INSTRUCTION, // 82 - rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 83 - rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
    EMIT_INSTRUCTION, // 84 - rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 85 - r: REF_AND(r,r)
    EMIT_INSTRUCTION, // 86 - czr: REF_AND(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 87 - rp: REF_AND(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 88 - r: REF_AND(REF_NOT(r),REF_NOT(r))
    EMIT_INSTRUCTION, // 89 - r: REF_AND(r,REF_NOT(r))
    EMIT_INSTRUCTION, // 90 - rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 91 - rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 92 - r: REF_OR(r,r)
    EMIT_INSTRUCTION, // 93 - r: REF_OR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 94 - r: REF_OR(REF_NOT(r),REF_NOT(r))
    EMIT_INSTRUCTION, // 95 - r: REF_OR(r,REF_NOT(r))
    EMIT_INSTRUCTION, // 96 - r: REF_XOR(r,r)
    EMIT_INSTRUCTION, // 97 - r: REF_XOR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 98 - r: REF_NOT(r)
    EMIT_INSTRUCTION, // 99 - r: REF_NOT(REF_OR(r,r))
    EMIT_INSTRUCTION, // 100 - r: REF_NOT(REF_AND(r,r))
    EMIT_INSTRUCTION, // 101 - r: REF_NOT(REF_XOR(r,r))
    EMIT_INSTRUCTION, // 102 - r: FLOAT_ADD(r,r)
    EMIT_INSTRUCTION, // 103 - r: DOUBLE_ADD(r,r)
    EMIT_INSTRUCTION, // 104 - r: FLOAT_MUL(r,r)
    EMIT_INSTRUCTION, // 105 - r: DOUBLE_MUL(r,r)
    EMIT_INSTRUCTION, // 106 - r: FLOAT_SUB(r,r)
    EMIT_INSTRUCTION, // 107 - r: DOUBLE_SUB(r,r)
    EMIT_INSTRUCTION, // 108 - r: FLOAT_DIV(r,r)
    EMIT_INSTRUCTION, // 109 - r: DOUBLE_DIV(r,r)
    EMIT_INSTRUCTION, // 110 - r: FLOAT_NEG(r)
    EMIT_INSTRUCTION, // 111 - r: DOUBLE_NEG(r)
    EMIT_INSTRUCTION, // 112 - r: FLOAT_SQRT(r)
    EMIT_INSTRUCTION, // 113 - r: DOUBLE_SQRT(r)
    EMIT_INSTRUCTION, // 114 - r:  FLOAT_ADD(FLOAT_MUL(r, r), r)
    EMIT_INSTRUCTION, // 115 - r:  DOUBLE_ADD(DOUBLE_MUL(r, r), r)
    EMIT_INSTRUCTION, // 116 - r: FLOAT_ADD(r, FLOAT_MUL(r,r))
    EMIT_INSTRUCTION, // 117 - r: DOUBLE_ADD(r, DOUBLE_MUL(r,r))
    EMIT_INSTRUCTION, // 118 - r:  FLOAT_SUB(FLOAT_MUL(r, r), r)
    EMIT_INSTRUCTION, // 119 - r:  DOUBLE_SUB(DOUBLE_MUL(r, r), r)
    EMIT_INSTRUCTION, // 120 - r:  FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r, r), r))
    EMIT_INSTRUCTION, // 121 - r:  DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r, r), r))
    EMIT_INSTRUCTION, // 122 - r: FLOAT_NEG(FLOAT_ADD(r, FLOAT_MUL(r,r)))
    EMIT_INSTRUCTION, // 123 - r: DOUBLE_NEG(DOUBLE_ADD(r, DOUBLE_MUL(r,r)))
    EMIT_INSTRUCTION, // 124 - r:  FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r, r), r))
    EMIT_INSTRUCTION, // 125 - r:  DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r, r), r))
    EMIT_INSTRUCTION, // 126 - rs: INT_2BYTE(r)
    EMIT_INSTRUCTION, // 127 - rp: INT_2USHORT(r)
    EMIT_INSTRUCTION, // 128 - rs: INT_2SHORT(r)
    EMIT_INSTRUCTION, // 129 - r: INT_2FLOAT(r)
    EMIT_INSTRUCTION, // 130 - r: INT_2DOUBLE(r)
    EMIT_INSTRUCTION, // 131 - r: FLOAT_2INT(r)
    EMIT_INSTRUCTION, // 132 - r: FLOAT_2DOUBLE(r)
    EMIT_INSTRUCTION, // 133 - r: DOUBLE_2INT(r)
    EMIT_INSTRUCTION, // 134 - r: DOUBLE_2FLOAT(r)
    EMIT_INSTRUCTION, // 135 - r: FLOAT_AS_INT_BITS(r)
    EMIT_INSTRUCTION, // 136 - r: INT_BITS_AS_FLOAT(r)
    EMIT_INSTRUCTION, // 137 - r: REF_MOVE(r)
    EMIT_INSTRUCTION, // 138 - rs: REF_MOVE(INT_CONSTANT)
    EMIT_INSTRUCTION, // 139 - rs: REF_MOVE(INT_CONSTANT)
    EMIT_INSTRUCTION, // 140 - rs: REF_MOVE(INT_CONSTANT)
    EMIT_INSTRUCTION, // 141 - r: FLOAT_MOVE(r)
    EMIT_INSTRUCTION, // 142 - r: DOUBLE_MOVE(r)
    EMIT_INSTRUCTION, // 143 - rs: BYTE_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 144 - rs: BYTE_LOAD(r,r)
    EMIT_INSTRUCTION, // 145 - rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 146 - rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 147 - rp: UBYTE_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 148 - rp: UBYTE_LOAD(r,r)
    EMIT_INSTRUCTION, // 149 - rs: SHORT_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 150 - rs: SHORT_LOAD(r,r)
    EMIT_INSTRUCTION, // 151 - rp: USHORT_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 152 - rp: USHORT_LOAD(r,r)
    EMIT_INSTRUCTION, // 153 - r: FLOAT_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 154 - r:      FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    EMIT_INSTRUCTION, // 155 - r: FLOAT_LOAD(r,r)
    EMIT_INSTRUCTION, // 156 - r: DOUBLE_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 157 - r:      DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    EMIT_INSTRUCTION, // 158 - r: DOUBLE_LOAD(r,r)
    EMIT_INSTRUCTION, // 159 - rs:      INT_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 160 - rs:      INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    EMIT_INSTRUCTION, // 161 - rs:      INT_LOAD(r,r)
    EMIT_INSTRUCTION, // 162 - rs:      INT_LOAD(REF_ADD(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 163 - rs:      INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 164 - stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 165 - stm: BYTE_STORE(r,OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 166 - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 167 - stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 168 - stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 169 - stm: SHORT_STORE(r,OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 170 - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 171 - stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 172 - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 173 - stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 174 - stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 175 - stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    EMIT_INSTRUCTION, // 176 - stm: INT_STORE(r,OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 177 - stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT))
    EMIT_INSTRUCTION, // 178 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 179 - stm:      FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    EMIT_INSTRUCTION, // 180 - stm: FLOAT_STORE(r,OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 181 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 182 - stm:      DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    EMIT_INSTRUCTION, // 183 - stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 184 - stm: INT_IFCMP(r,r)
    EMIT_INSTRUCTION, // 185 - stm: INT_IFCMP(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 186 - stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 187 - stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 188 - stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 189 - stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 190 - stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 191 - stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 192 - stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 193 - stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 194 - stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 195 - stm: INT_IFCMP(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 196 - stm: INT_IFCMP(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 197 - stm: INT_IFCMP(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 198 - stm: INT_IFCMP(boolcmp, INT_CONSTANT)
    EMIT_INSTRUCTION, // 199 - stm: INT_IFCMP2(r,r)
    EMIT_INSTRUCTION, // 200 - stm: INT_IFCMP2(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 201 - stm:   FLOAT_IFCMP(r,r)
    EMIT_INSTRUCTION, // 202 - stm:   DOUBLE_IFCMP(r,r)
    EMIT_INSTRUCTION, // 203 - stm: FLOAT_CMPL(r,r)
    EMIT_INSTRUCTION, // 204 - stm: FLOAT_CMPG(r,r)
    EMIT_INSTRUCTION, // 205 - stm: DOUBLE_CMPL(r,r)
    EMIT_INSTRUCTION, // 206 - stm: DOUBLE_CMPG(r,r)
    EMIT_INSTRUCTION, // 207 - stm: GOTO
    EMIT_INSTRUCTION, // 208 - stm: RETURN(NULL)
    EMIT_INSTRUCTION, // 209 - stm: RETURN(r)
    EMIT_INSTRUCTION, // 210 - r: CALL(r,any)
    EMIT_INSTRUCTION, // 211 - r: CALL(BRANCH_TARGET,any)
    EMIT_INSTRUCTION, // 212 - r: SYSCALL(r,any)
    EMIT_INSTRUCTION, // 213 - r: GET_TIME_BASE
    NOFLAGS, // 214 - r: OTHER_OPERAND(r,r)
    EMIT_INSTRUCTION, // 215 - r:  YIELDPOINT_OSR(any, any)
    EMIT_INSTRUCTION, // 216 - r:      PREPARE_INT(r, r)
    EMIT_INSTRUCTION, // 217 - r:      PREPARE_LONG(r, r)
    EMIT_INSTRUCTION, // 218 - r:      ATTEMPT_INT(r, r)
    EMIT_INSTRUCTION, // 219 - r:      ATTEMPT_LONG(r, r)
    EMIT_INSTRUCTION, // 220 - stm: IR_PROLOGUE
    EMIT_INSTRUCTION, // 221 - r: LONG_ADD(r,r)
    EMIT_INSTRUCTION, // 222 - r: LONG_SUB(r,r)
    EMIT_INSTRUCTION, // 223 - r: LONG_MUL(r,r)
    EMIT_INSTRUCTION, // 224 - r: LONG_NEG(r)
    EMIT_INSTRUCTION, // 225 - r: LONG_SHL(r,r)
    EMIT_INSTRUCTION, // 226 - r: LONG_SHL(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 227 - r: LONG_SHR(r,r)
    EMIT_INSTRUCTION, // 228 - r: LONG_SHR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 229 - r: LONG_USHR(r,r)
    EMIT_INSTRUCTION, // 230 - r: LONG_USHR(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 231 - r: LONG_AND(r,r)
    EMIT_INSTRUCTION, // 232 - r: LONG_OR(r,r)
    EMIT_INSTRUCTION, // 233 - r: LONG_XOR(r,r)
    EMIT_INSTRUCTION, // 234 - r: LONG_NOT(r)
    EMIT_INSTRUCTION, // 235 - r: INT_2LONG(r)
    EMIT_INSTRUCTION, // 236 - r: LONG_2INT(r)
    EMIT_INSTRUCTION, // 237 - r: DOUBLE_AS_LONG_BITS(r)
    EMIT_INSTRUCTION, // 238 - r: LONG_BITS_AS_DOUBLE(r)
    EMIT_INSTRUCTION, // 239 - r: LONG_MOVE(LONG_CONSTANT)
    EMIT_INSTRUCTION, // 240 - r: LONG_MOVE(r)
    EMIT_INSTRUCTION, // 241 - stm: LONG_CMP(r,r)
    EMIT_INSTRUCTION, // 242 - stm: LONG_IFCMP(r,r)
    EMIT_INSTRUCTION, // 243 - stm:    INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 244 - stm:    INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 245 - stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 246 - stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 247 - stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 248 - stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 249 - stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 250 - stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT)
    EMIT_INSTRUCTION, // 251 - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
    EMIT_INSTRUCTION, // 252 - stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
    EMIT_INSTRUCTION, // 253 - stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
    EMIT_INSTRUCTION, // 254 - stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT)
    EMIT_INSTRUCTION, // 255 - stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT)
    EMIT_INSTRUCTION, // 256 - czr: REF_AND(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 257 - r: REF_AND(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 258 - r: REF_OR(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 259 - r: REF_OR(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 260 - r: REF_XOR(r,REF_MOVE(INT_CONSTANT))
    EMIT_INSTRUCTION, // 261 - r: REF_MOVE(ADDRESS_CONSTANT)
    EMIT_INSTRUCTION, // 262 - r: REF_MOVE(ADDRESS_CONSTANT)
    EMIT_INSTRUCTION, // 263 - r: REF_MOVE(ADDRESS_CONSTANT)
    EMIT_INSTRUCTION, // 264 - r: LONG_LOAD(r,INT_CONSTANT)
    EMIT_INSTRUCTION, // 265 - r:      LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
    EMIT_INSTRUCTION, // 266 - r: LONG_LOAD(r,r)
    EMIT_INSTRUCTION, // 267 - stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
    EMIT_INSTRUCTION, // 268 - stm:      LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
    EMIT_INSTRUCTION, // 269 - stm: LONG_STORE(r,OTHER_OPERAND(r,r))
    EMIT_INSTRUCTION, // 270 - r:      PREPARE_ADDR(r, r)
    EMIT_INSTRUCTION, // 271 - r:      ATTEMPT_ADDR(r, r)
  };

  /**
   * Gets the action flags (such as EMIT_INSTRUCTION) associated with the given
   * rule number.
   *
   * @param ruleno the rule number we want the action flags for
   * @return the action byte for the rule
   */
  @Pure
  public static byte action(int ruleno) {
    return action[unsortedErnMap[ruleno]];
  }

  /**
   * Decode the target non-terminal and minimal cost covering statement
   * into the rule that produces the non-terminal
   *
   * @param goalnt the non-terminal that we wish to produce.
   * @param stateNT the state encoding the non-terminals associated associated
   *        with covering a tree with minimal cost (computed by at compile time
   *        by jburg).
   * @return the rule number
   */
   @Pure
   public static char decode(int goalnt, int stateNT) {
     return decode[goalnt][stateNT];
   }


  /**
   * Emit code for rule number 14:
   * stm: RESOLVE
   * @param p BURS node to apply the rule to
   */
  private void code14(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 15:
   * stm: IG_PATCH_POINT
   * @param p BURS node to apply the rule to
   */
  private void code15(AbstractBURS_TreeNode p) {
    EMIT(InlineGuard.mutate(P(p), IG_PATCH_POINT, null, null, null, InlineGuard.getTarget(P(p)), InlineGuard.getBranchProfile(P(p))));
  }

  /**
   * Emit code for rule number 16:
   * stm: UNINT_BEGIN
   * @param p BURS node to apply the rule to
   */
  private void code16(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 17:
   * stm: UNINT_END
   * @param p BURS node to apply the rule to
   */
  private void code17(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 18:
   * stm: YIELDPOINT_PROLOGUE
   * @param p BURS node to apply the rule to
   */
  private void code18(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 19:
   * stm: YIELDPOINT_EPILOGUE
   * @param p BURS node to apply the rule to
   */
  private void code19(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 20:
   * stm: YIELDPOINT_BACKEDGE
   * @param p BURS node to apply the rule to
   */
  private void code20(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 21:
   * r: FRAMESIZE
   * @param p BURS node to apply the rule to
   */
  private void code21(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_LDI, Nullary.getClearResult(P(p)), new UnknownConstantOperand()));
  }

  /**
   * Emit code for rule number 22:
   * stm: LOWTABLESWITCH(r)
   * @param p BURS node to apply the rule to
   */
  private void code22(AbstractBURS_TreeNode p) {
    LOWTABLESWITCH(P(p));
  }

  /**
   * Emit code for rule number 24:
   * r: GUARD_MOVE
   * @param p BURS node to apply the rule to
   */
  private void code24(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 25:
   * r: GUARD_COMBINE
   * @param p BURS node to apply the rule to
   */
  private void code25(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 26:
   * stm: NULL_CHECK(r)
   * @param p BURS node to apply the rule to
   */
  private void code26(AbstractBURS_TreeNode p) {
    EMIT(P(p));
  }

  /**
   * Emit code for rule number 27:
   * r: GET_CAUGHT_EXCEPTION
   * @param p BURS node to apply the rule to
   */
  private void code27(AbstractBURS_TreeNode p) {
    GET_EXCEPTION_OBJECT(P(p));
  }

  /**
   * Emit code for rule number 28:
   * stm: SET_CAUGHT_EXCEPTION(r)
   * @param p BURS node to apply the rule to
   */
  private void code28(AbstractBURS_TreeNode p) {
    SET_EXCEPTION_OBJECT(P(p));
  }

  /**
   * Emit code for rule number 29:
   * stm: FENCE
   * @param p BURS node to apply the rule to
   */
  private void code29(AbstractBURS_TreeNode p) {
    EMIT(MIR_Empty.mutate(P(p), PPC_HWSYNC));
  }

  /**
   * Emit code for rule number 30:
   * stm: WRITE_FLOOR
   * @param p BURS node to apply the rule to
   */
  private void code30(AbstractBURS_TreeNode p) {
    EMIT(MIR_Empty.mutate(P(p), PPC_SYNC));
  }

  /**
   * Emit code for rule number 31:
   * stm: READ_CEILING
   * @param p BURS node to apply the rule to
   */
  private void code31(AbstractBURS_TreeNode p) {
    EMIT(MIR_Empty.mutate(P(p), PPC_ISYNC));
  }

  /**
   * Emit code for rule number 32:
   * stm: DCBF(r)
   * @param p BURS node to apply the rule to
   */
  private void code32(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_DCBF, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 33:
   * stm: DCBST(r)
   * @param p BURS node to apply the rule to
   */
  private void code33(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_DCBST, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 34:
   * stm: DCBT(r)
   * @param p BURS node to apply the rule to
   */
  private void code34(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_DCBT, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 35:
   * stm: DCBTST(r)
   * @param p BURS node to apply the rule to
   */
  private void code35(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_DCBTST, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 36:
   * stm: DCBZ(r)
   * @param p BURS node to apply the rule to
   */
  private void code36(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_DCBZ, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 37:
   * stm: DCBZL(r)
   * @param p BURS node to apply the rule to
   */
  private void code37(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_DCBZL, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 38:
   * stm: ICBI(r)
   * @param p BURS node to apply the rule to
   */
  private void code38(AbstractBURS_TreeNode p) {
    EMIT(MIR_CacheOp.mutate(P(p), PPC_ICBI, I(0), R(CacheOp.getRef(P(p)))));
  }

  /**
   * Emit code for rule number 39:
   * stm:   ILLEGAL_INSTRUCTION
   * @param p BURS node to apply the rule to
   */
  private void code39(AbstractBURS_TreeNode p) {
    EMIT(MIR_Empty.mutate(P(p), PPC_ILLEGAL_INSTRUCTION));
  }

  /**
   * Emit code for rule number 40:
   * stm: TRAP
   * @param p BURS node to apply the rule to
   */
  private void code40(AbstractBURS_TreeNode p) {
    TRAP(P(p));
  }

  /**
   * Emit code for rule number 41:
   * stm: TRAP_IF(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code41(AbstractBURS_TreeNode p) {
    TRAP_IF(P(p));
  }

  /**
   * Emit code for rule number 42:
   * stm: TRAP_IF(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code42(AbstractBURS_TreeNode p) {
    TRAP_IF_IMM(P(p), false);
  }

  /**
   * Emit code for rule number 43:
   * stm: TRAP_IF(r,LONG_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code43(AbstractBURS_TreeNode p) {
    TRAP_IF_IMM(P(p), true);
  }

  /**
   * Emit code for rule number 44:
   * r: BOOLEAN_NOT(r)
   * @param p BURS node to apply the rule to
   */
  private void code44(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_XORI, Unary.getResult(P(p)), R(Unary.getVal(P(p))), IC(1)));
  }

  /**
   * Emit code for rule number 45:
   * r: BOOLEAN_CMP_INT(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code45(AbstractBURS_TreeNode p) {
    BOOLEAN_CMP_INT_IMM(BooleanCmp.getClearResult(P(p)), BooleanCmp.getClearCond(P(p)), R(BooleanCmp.getClearVal1(P(p))), IC(BooleanCmp.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 46:
   * r: BOOLEAN_CMP_INT(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code46(AbstractBURS_TreeNode p) {
    BOOLEAN_CMP_INT(BooleanCmp.getClearResult(P(p)), BooleanCmp.getClearCond(P(p)), R(BooleanCmp.getClearVal1(P(p))), R(BooleanCmp.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 47:
   * boolcmp: BOOLEAN_CMP_INT(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code47(AbstractBURS_TreeNode p) {
    PUSH_BOOLCMP(BooleanCmp.getClearCond(P(p)), BooleanCmp.getClearVal1(P(p)), BooleanCmp.getClearVal2(P(p)), false);
  }

  /**
   * Emit code for rule number 48:
   * boolcmp: BOOLEAN_CMP_INT(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code48(AbstractBURS_TreeNode p) {
    PUSH_BOOLCMP(BooleanCmp.getClearCond(P(p)), BooleanCmp.getClearVal1(P(p)), BooleanCmp.getClearVal2(P(p)), false);
  }

  /**
   * Emit code for rule number 49:
   * r: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code49(AbstractBURS_TreeNode p) {
    BOOLEAN_CMP_ADDR_IMM(BooleanCmp.getClearResult(P(p)), BooleanCmp.getClearCond(P(p)), R(BooleanCmp.getClearVal1(P(p))), IC(BooleanCmp.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 50:
   * r: BOOLEAN_CMP_ADDR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code50(AbstractBURS_TreeNode p) {
    BOOLEAN_CMP_ADDR(BooleanCmp.getClearResult(P(p)), BooleanCmp.getClearCond(P(p)), R(BooleanCmp.getClearVal1(P(p))), R(BooleanCmp.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 51:
   * boolcmp: BOOLEAN_CMP_ADDR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code51(AbstractBURS_TreeNode p) {
    PUSH_BOOLCMP(BooleanCmp.getClearCond(P(p)), BooleanCmp.getClearVal1(P(p)), BooleanCmp.getClearVal2(P(p)), true);
  }

  /**
   * Emit code for rule number 52:
   * boolcmp: BOOLEAN_CMP_ADDR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code52(AbstractBURS_TreeNode p) {
    PUSH_BOOLCMP(BooleanCmp.getClearCond(P(p)), BooleanCmp.getClearVal1(P(p)), BooleanCmp.getClearVal2(P(p)), true);
  }

  /**
   * Emit code for rule number 54:
   * boolcmp: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code54(AbstractBURS_TreeNode p) {
    FLIP_BOOLCMP(); // invert condition
  }

  /**
   * Emit code for rule number 56:
   * boolcmp: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code56(AbstractBURS_TreeNode p) {
    FLIP_BOOLCMP(); // invert condition
  }

  /**
   * Emit code for rule number 57:
   * r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code57(AbstractBURS_TreeNode p) {
    EMIT_PUSHED_BOOLCMP(BooleanCmp.getClearResult(P(p)));
  }

  /**
   * Emit code for rule number 58:
   * r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code58(AbstractBURS_TreeNode p) {
    FLIP_BOOLCMP(); EMIT_PUSHED_BOOLCMP(BooleanCmp.getClearResult(P(p)));
  }

  /**
   * Emit code for rule number 59:
   * r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code59(AbstractBURS_TreeNode p) {
    EMIT_PUSHED_BOOLCMP(BooleanCmp.getClearResult(P(p)));
  }

  /**
   * Emit code for rule number 60:
   * r: BOOLEAN_CMP_INT(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code60(AbstractBURS_TreeNode p) {
    FLIP_BOOLCMP(); EMIT_PUSHED_BOOLCMP(BooleanCmp.getClearResult(P(p)));
  }

  /**
   * Emit code for rule number 61:
   * r: REF_ADD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code61(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ADDI, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 62:
   * r: REF_ADD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code62(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ADD, Binary.getResult(P(p)),                 
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 63:
   * r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code63(AbstractBURS_TreeNode p) {
    {                                                                             
   int val = IV(Move.getVal(PR(p)));                                      
   EMIT(MIR_Binary.create(PPC_ADDI, Move.getResult(PR(p)).copyRO(),                    
                          R(Binary.getClearVal1(P(p))), CAL16(val)));              
   EMIT(MIR_Binary.mutate(P(p), PPC_ADDIS, Binary.getResult(P(p)),            
                          Move.getClearResult(PR(p)), CAU16(val)));       
}
  }

  /**
   * Emit code for rule number 64:
   * r: REF_ADD(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code64(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ADDIS, Binary.getResult(P(p)),               
                       R(Binary.getVal1(P(p))), CAU16(IV(Move.getVal(PR(p))))));
  }

  /**
   * Emit code for rule number 65:
   * r: REF_SUB(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code65(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SUBF, Binary.getResult(P(p)),                
                       R(Binary.getVal2(P(p))), Binary.getVal1(P(p))));
  }

  /**
   * Emit code for rule number 66:
   * r: REF_SUB(INT_CONSTANT,r)
   * @param p BURS node to apply the rule to
   */
  private void code66(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SUBFIC, Binary.getResult(P(p)),              
                       R(Binary.getVal2(P(p))), Binary.getVal1(P(p))));
  }

  /**
   * Emit code for rule number 67:
   * r: INT_MUL(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code67(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_MULLI, Binary.getResult(P(p)),               
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 68:
   * r: INT_MUL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code68(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_MULLW, Binary.getResult(P(p)),               
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 69:
   * r: INT_DIV(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code69(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_DIVW, GuardedBinary.getResult(P(p)),  
                       R(GuardedBinary.getVal1(P(p))), GuardedBinary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 70:
   * r: INT_DIV(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code70(AbstractBURS_TreeNode p) {
    INT_DIV_IMM(P(p), GuardedBinary.getClearResult(P(p)), R(GuardedBinary.getClearVal1(P(p))), 
                  Move.getClearResult(PR(p)), IC(Move.getClearVal(PR(p))));
  }

  /**
   * Emit code for rule number 71:
   * r: INT_REM(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code71(AbstractBURS_TreeNode p) {
    INT_REM(P(p), GuardedBinary.getClearResult(P(p)), R(GuardedBinary.getClearVal1(P(p))), R(GuardedBinary.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 72:
   * r: INT_REM(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code72(AbstractBURS_TreeNode p) {
    INT_REM_IMM(P(p), GuardedBinary.getClearResult(P(p)), R(GuardedBinary.getClearVal1(P(p))), 
            Move.getClearResult(PR(p)), IC(Move.getClearVal(PR(p))));
  }

  /**
   * Emit code for rule number 73:
   * r: REF_NEG(r)
   * @param p BURS node to apply the rule to
   */
  private void code73(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_NEG, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 74:
   * rz: INT_SHL(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code74(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SLWI, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 75:
   * rz: INT_SHL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code75(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SLW, Binary.getResult(P(p)), 
                       R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 76:
   * rz: INT_SHL(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code76(AbstractBURS_TreeNode p) {
    USHR_SHL(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(PL(p))), IC(Binary.getClearVal2(PL(p))),      
               IC(Binary.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 77:
   * rs: INT_SHR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code77(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SRAWI, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 78:
   * rs: INT_SHR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code78(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SRAW, Binary.getResult(P(p)), 
                       R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 79:
   * rp: INT_SHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code79(AbstractBURS_TreeNode p) {
    AND_USHR(P(p), Binary.getClearResult(P(p)),                      
                     R(Binary.getClearVal1(PL(p))), IC(Binary.getClearVal2(PL(p))),      
                     IC(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 80:
   * rp: INT_USHR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code80(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SRWI, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 81:
   * rz: INT_USHR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code81(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_SRW, Binary.getResult(P(p)), 
                       R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 82:
   * rp: INT_USHR(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code82(AbstractBURS_TreeNode p) {
    AND_USHR(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(PL(p))), 
         IC(Binary.getClearVal2(PL(p))), IC(Binary.getClearVal2(P(p))));
  }

  /**
   * Emit code for rule number 83:
   * rp: INT_USHR(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code83(AbstractBURS_TreeNode p) {
    AND_USHR(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(PL(p))), 
         IC(Move.getClearVal(PLR(p))), IC(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 84:
   * rp: INT_USHR(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code84(AbstractBURS_TreeNode p) {
    SHL_USHR(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(PL(p))), 
         IC(Binary.getClearVal2(PL(p))), IC(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 85:
   * r: REF_AND(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code85(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_AND, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 86:
   * czr: REF_AND(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code86(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ANDIr, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), IC(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 87:
   * rp: REF_AND(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code87(AbstractBURS_TreeNode p) {
    {                                                                             
   int mask = IV(Binary.getVal2(P(p)));                                   
   EMIT(MIR_RotateAndMask.create(PPC_RLWINM, Binary.getClearResult(P(p)),          
                                 R(Binary.getClearVal1(P(p))), IC(0),               
                                 IC(MaskBegin(mask)), IC(MaskEnd(mask))));                        
}
  }

  /**
   * Emit code for rule number 88:
   * r: REF_AND(REF_NOT(r),REF_NOT(r))
   * @param p BURS node to apply the rule to
   */
  private void code88(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_NOR, Binary.getResult(P(p)),                 
                       R(Unary.getVal(PL(p))), Unary.getVal(PR(p))));
  }

  /**
   * Emit code for rule number 89:
   * r: REF_AND(r,REF_NOT(r))
   * @param p BURS node to apply the rule to
   */
  private void code89(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ANDC, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Unary.getClearVal(PR(p))));
  }

  /**
   * Emit code for rule number 90:
   * rp: REF_AND(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code90(AbstractBURS_TreeNode p) {
    USHR_AND(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(PL(p))), 
               IC(Binary.getClearVal2(P(p))), IC(Binary.getClearVal2(PL(p))));
  }

  /**
   * Emit code for rule number 91:
   * rp: REF_AND(INT_USHR(r,INT_CONSTANT),REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code91(AbstractBURS_TreeNode p) {
    USHR_AND(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(PL(p))), 
               IC(Move.getClearVal(PR(p))), IC(Binary.getClearVal2(PL(p))));
  }

  /**
   * Emit code for rule number 92:
   * r: REF_OR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code92(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_OR, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 93:
   * r: REF_OR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code93(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ORI, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 94:
   * r: REF_OR(REF_NOT(r),REF_NOT(r))
   * @param p BURS node to apply the rule to
   */
  private void code94(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_NAND, Binary.getResult(P(p)), R(Unary.getVal(PL(p))), Unary.getClearVal(PR(p))));
  }

  /**
   * Emit code for rule number 95:
   * r: REF_OR(r,REF_NOT(r))
   * @param p BURS node to apply the rule to
   */
  private void code95(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ORC, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Unary.getClearVal(PR(p))));
  }

  /**
   * Emit code for rule number 96:
   * r: REF_XOR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code96(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_XOR, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 97:
   * r: REF_XOR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code97(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_XORI, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 98:
   * r: REF_NOT(r)
   * @param p BURS node to apply the rule to
   */
  private void code98(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_NOR, Unary.getResult(P(p)), R(Unary.getVal(P(p))), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 99:
   * r: REF_NOT(REF_OR(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code99(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_NOR, Unary.getResult(P(p)), R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p))));
  }

  /**
   * Emit code for rule number 100:
   * r: REF_NOT(REF_AND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code100(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_NAND, Unary.getResult(P(p)), R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p))));
  }

  /**
   * Emit code for rule number 101:
   * r: REF_NOT(REF_XOR(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code101(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_EQV, Unary.getResult(P(p)), R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p))));
  }

  /**
   * Emit code for rule number 102:
   * r: FLOAT_ADD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code102(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FADDS, Binary.getResult(P(p)), 
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 103:
   * r: DOUBLE_ADD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code103(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FADD, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 104:
   * r: FLOAT_MUL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code104(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FMULS, Binary.getResult(P(p)),               
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 105:
   * r: DOUBLE_MUL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code105(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FMUL, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 106:
   * r: FLOAT_SUB(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code106(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FSUBS, Binary.getResult(P(p)),               
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 107:
   * r: DOUBLE_SUB(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code107(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FSUB, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 108:
   * r: FLOAT_DIV(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code108(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FDIVS, Binary.getResult(P(p)),               
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 109:
   * r: DOUBLE_DIV(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code109(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_FDIV, Binary.getResult(P(p)),                
                       R(Binary.getVal1(P(p))), Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 110:
   * r: FLOAT_NEG(r)
   * @param p BURS node to apply the rule to
   */
  private void code110(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_FNEG, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 111:
   * r: DOUBLE_NEG(r)
   * @param p BURS node to apply the rule to
   */
  private void code111(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_FNEG, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 112:
   * r: FLOAT_SQRT(r)
   * @param p BURS node to apply the rule to
   */
  private void code112(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_FSQRTS, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 113:
   * r: DOUBLE_SQRT(r)
   * @param p BURS node to apply the rule to
   */
  private void code113(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_FSQRT, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 114:
   * r:  FLOAT_ADD(FLOAT_MUL(r, r), r)
   * @param p BURS node to apply the rule to
   */
  private void code114(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FMADDS, Binary.getResult(P(p)),             
                        R(Binary.getClearVal1(PL(p))), R(Binary.getClearVal2(PL(p))),   
                        R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 115:
   * r:  DOUBLE_ADD(DOUBLE_MUL(r, r), r)
   * @param p BURS node to apply the rule to
   */
  private void code115(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FMADD, Binary.getResult(P(p)),              
                        R(Binary.getClearVal1(PL(p))), R(Binary.getClearVal2(PL(p))),   
                        R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 116:
   * r: FLOAT_ADD(r, FLOAT_MUL(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code116(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FMADDS, Binary.getResult(P(p)),             
                        R(Binary.getClearVal1(PR(p))), R(Binary.getClearVal2(PR(p))),   
                        R(Binary.getVal1(P(p)))));
  }

  /**
   * Emit code for rule number 117:
   * r: DOUBLE_ADD(r, DOUBLE_MUL(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code117(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FMADD, Binary.getResult(P(p)),              
                        R(Binary.getClearVal1(PR(p))), R(Binary.getClearVal2(PR(p))),   
                        R(Binary.getVal1(P(p)))));
  }

  /**
   * Emit code for rule number 118:
   * r:  FLOAT_SUB(FLOAT_MUL(r, r), r)
   * @param p BURS node to apply the rule to
   */
  private void code118(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FMSUBS, Binary.getResult(P(p)),             
                        R(Binary.getClearVal1(PL(p))), R(Binary.getClearVal2(PL(p))),   
                        R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 119:
   * r:  DOUBLE_SUB(DOUBLE_MUL(r, r), r)
   * @param p BURS node to apply the rule to
   */
  private void code119(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FMSUB, Binary.getResult(P(p)),              
                        R(Binary.getClearVal1(PL(p))), R(Binary.getClearVal2(PL(p))),   
                        R(Binary.getVal2(P(p)))));
  }

  /**
   * Emit code for rule number 120:
   * r:  FLOAT_NEG(FLOAT_ADD(FLOAT_MUL(r, r), r))
   * @param p BURS node to apply the rule to
   */
  private void code120(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FNMADDS, Unary.getResult(P(p)),             
                        R(Binary.getClearVal1(PLL(p))), R(Binary.getClearVal2(PLL(p))), 
                        R(Binary.getVal2(PL(p)))));
  }

  /**
   * Emit code for rule number 121:
   * r:  DOUBLE_NEG(DOUBLE_ADD(DOUBLE_MUL(r, r), r))
   * @param p BURS node to apply the rule to
   */
  private void code121(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FNMADD, Unary.getResult(P(p)),              
                        R(Binary.getClearVal1(PLL(p))), R(Binary.getClearVal2(PLL(p))), 
                        R(Binary.getClearVal2(PL(p)))));
  }

  /**
   * Emit code for rule number 122:
   * r: FLOAT_NEG(FLOAT_ADD(r, FLOAT_MUL(r,r)))
   * @param p BURS node to apply the rule to
   */
  private void code122(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FNMADDS, Unary.getResult(P(p)),             
                        R(Binary.getClearVal1(PLR(p))), R(Binary.getClearVal2(PLR(p))), 
                        R(Binary.getClearVal1(PL(p)))));
  }

  /**
   * Emit code for rule number 123:
   * r: DOUBLE_NEG(DOUBLE_ADD(r, DOUBLE_MUL(r,r)))
   * @param p BURS node to apply the rule to
   */
  private void code123(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FNMADD, Unary.getResult(P(p)),             
                        R(Binary.getClearVal1(PLR(p))), R(Binary.getClearVal2(PLR(p))),
                        R(Binary.getClearVal1(PL(p)))));
  }

  /**
   * Emit code for rule number 124:
   * r:  FLOAT_NEG(FLOAT_SUB(FLOAT_MUL(r, r), r))
   * @param p BURS node to apply the rule to
   */
  private void code124(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FNMSUBS, Unary.getResult(P(p)),             
                        R(Binary.getClearVal1(PLL(p))), R(Binary.getClearVal2(PLL(p))), 
                        R(Binary.getClearVal2(PL(p)))));
  }

  /**
   * Emit code for rule number 125:
   * r:  DOUBLE_NEG(DOUBLE_SUB(DOUBLE_MUL(r, r), r))
   * @param p BURS node to apply the rule to
   */
  private void code125(AbstractBURS_TreeNode p) {
    EMIT(MIR_Ternary.mutate(P(p), PPC_FNMSUB, Unary.getResult(P(p)),              
                        R(Binary.getClearVal1(PLL(p))), R(Binary.getClearVal2(PLL(p))), 
                        R(Binary.getClearVal2(PL(p)))));
  }

  /**
   * Emit code for rule number 126:
   * rs: INT_2BYTE(r)
   * @param p BURS node to apply the rule to
   */
  private void code126(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_EXTSB, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 127:
   * rp: INT_2USHORT(r)
   * @param p BURS node to apply the rule to
   */
  private void code127(AbstractBURS_TreeNode p) {
    EMIT(MIR_RotateAndMask.create(PPC_RLWINM, Unary.getClearResult(P(p)), null,        
                              R(Unary.getClearVal(P(p))), IC(0), IC(16), IC(31)));
  }

  /**
   * Emit code for rule number 128:
   * rs: INT_2SHORT(r)
   * @param p BURS node to apply the rule to
   */
  private void code128(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_EXTSH, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 129:
   * r: INT_2FLOAT(r)
   * @param p BURS node to apply the rule to
   */
  private void code129(AbstractBURS_TreeNode p) {
    INT_2DOUBLE(P(p), Unary.getResult(P(p)), R(Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 130:
   * r: INT_2DOUBLE(r)
   * @param p BURS node to apply the rule to
   */
  private void code130(AbstractBURS_TreeNode p) {
    INT_2DOUBLE(P(p), Unary.getResult(P(p)), R(Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 131:
   * r: FLOAT_2INT(r)
   * @param p BURS node to apply the rule to
   */
  private void code131(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 132:
   * r: FLOAT_2DOUBLE(r)
   * @param p BURS node to apply the rule to
   */
  private void code132(AbstractBURS_TreeNode p) {
    EMIT(MIR_Move.mutate(P(p), PPC_FMR, Unary.getResult(P(p)), R(Unary.getVal(P(p)))));
  }

  /**
   * Emit code for rule number 133:
   * r: DOUBLE_2INT(r)
   * @param p BURS node to apply the rule to
   */
  private void code133(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansionLeave
  }

  /**
   * Emit code for rule number 134:
   * r: DOUBLE_2FLOAT(r)
   * @param p BURS node to apply the rule to
   */
  private void code134(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_FRSP, Unary.getResult(P(p)), Unary.getVal(P(p))));
  }

  /**
   * Emit code for rule number 135:
   * r: FLOAT_AS_INT_BITS(r)
   * @param p BURS node to apply the rule to
   */
  private void code135(AbstractBURS_TreeNode p) {
    FPR2GPR_32(P(p));
  }

  /**
   * Emit code for rule number 136:
   * r: INT_BITS_AS_FLOAT(r)
   * @param p BURS node to apply the rule to
   */
  private void code136(AbstractBURS_TreeNode p) {
    GPR2FPR_32(P(p));
  }

  /**
   * Emit code for rule number 137:
   * r: REF_MOVE(r)
   * @param p BURS node to apply the rule to
   */
  private void code137(AbstractBURS_TreeNode p) {
    EMIT(MIR_Move.mutate(P(p), PPC_MOVE, Move.getResult(P(p)), R(Move.getVal(P(p)))));
  }

  /**
   * Emit code for rule number 138:
   * rs: REF_MOVE(INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code138(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_LDI, Move.getResult(P(p)), Move.getVal(P(p))));
  }

  /**
   * Emit code for rule number 139:
   * rs: REF_MOVE(INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code139(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_LDIS, Move.getResult(P(p)), SRI(IV(Move.getVal(P(p))), 16)));
  }

  /**
   * Emit code for rule number 140:
   * rs: REF_MOVE(INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code140(AbstractBURS_TreeNode p) {
    {                                                                             
   int one = IV(Move.getVal(P(p)));                                       
   EMIT(MIR_Unary.create(PPC_LDIS, Move.getResult(P(p)).copyRO(), CAU16(one)));        
   EMIT(MIR_Binary.mutate(P(p), PPC_ADDI, Move.getResult(P(p)).copyRO(),      
                          Move.getClearResult(P(p)), CAL16(one)));        
}
  }

  /**
   * Emit code for rule number 141:
   * r: FLOAT_MOVE(r)
   * @param p BURS node to apply the rule to
   */
  private void code141(AbstractBURS_TreeNode p) {
    EMIT(MIR_Move.mutate(P(p), PPC_FMR, Move.getResult(P(p)), R(Move.getVal(P(p)))));
  }

  /**
   * Emit code for rule number 142:
   * r: DOUBLE_MOVE(r)
   * @param p BURS node to apply the rule to
   */
  private void code142(AbstractBURS_TreeNode p) {
    EMIT(MIR_Move.mutate(P(p), PPC_FMR, Move.getResult(P(p)), R(Move.getVal(P(p)))));
  }

  /**
   * Emit code for rule number 143:
   * rs: BYTE_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code143(AbstractBURS_TreeNode p) {
    BYTE_LOAD(P(p), PPC_LBZ, Load.getClearResult(P(p)), R(Load.getClearAddress(P(p))), Load.getClearOffset(P(p)), 
          Load.getClearLocation(P(p)), Load.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 144:
   * rs: BYTE_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code144(AbstractBURS_TreeNode p) {
    BYTE_LOAD(P(p), PPC_LBZX, Load.getClearResult(P(p)), R(Load.getClearAddress(P(p))), Load.getClearOffset(P(p)), 
          Load.getClearLocation(P(p)), Load.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 145:
   * rp: REF_AND(BYTE_LOAD(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code145(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(PL(p), PPC_LBZX, Binary.getClearResult(P(p)), R(Load.getAddress(PL(p))), 
                     Load.getOffset(PL(p)), Load.getLocation(PL(p)), 
                     Load.getGuard(PL(p))));
  }

  /**
   * Emit code for rule number 146:
   * rp: REF_AND(BYTE_LOAD(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code146(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(PL(p), PPC_LBZ, Binary.getClearResult(P(p)), R(Load.getAddress(PL(p))), 
                     Load.getOffset(PL(p)), Load.getLocation(PL(p)), 
                     Load.getGuard(PL(p))));
  }

  /**
   * Emit code for rule number 147:
   * rp: UBYTE_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code147(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LBZ, Load.getResult(P(p)),            
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 148:
   * rp: UBYTE_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code148(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LBZX, Load.getResult(P(p)),           
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 149:
   * rs: SHORT_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code149(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LHA, Load.getResult(P(p)),            
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 150:
   * rs: SHORT_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code150(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LHAX, Load.getResult(P(p)),           
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 151:
   * rp: USHORT_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code151(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LHZ, Load.getResult(P(p)),            
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 152:
   * rp: USHORT_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code152(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LHZX, Load.getResult(P(p)),           
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 153:
   * r: FLOAT_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code153(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LFS, Load.getResult(P(p)),            
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 154:
   * r:      FLOAT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code154(AbstractBURS_TreeNode p) {
    {                                                                             
   Address val = AV(Move.getVal(PR(p)));                                      
   EMIT(MIR_Binary.create(PPC_ADDIS, Move.getResult(PR(p)).copyRO(),                   
                          R(Load.getClearAddress(P(p))), CAU16(val)));             
   EMIT(MIR_Load.mutate(P(p), PPC_LFS, Load.getResult(P(p)),         
                        Move.getClearResult(PR(p)), CAL16(val),  
                        Load.getLocation(P(p)),                      
                        Load.getGuard(P(p))));                      
}
  }

  /**
   * Emit code for rule number 155:
   * r: FLOAT_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code155(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LFSX, Load.getResult(P(p)),           
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 156:
   * r: DOUBLE_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code156(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LFD, Load.getResult(P(p)),            
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 157:
   * r:      DOUBLE_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code157(AbstractBURS_TreeNode p) {
    {                                                                             
   Address val = AV(Move.getVal(PR(p)));                                      
   EMIT(MIR_Binary.create(PPC_ADDIS, Move.getResult(PR(p)).copyRO(),                   
                          R(Load.getClearAddress(P(p))), CAU16(val)));             
   EMIT(MIR_Load.mutate(P(p), PPC_LFD, Load.getResult(P(p)),         
                        Move.getClearResult(PR(p)), CAL16(val),  
                        Load.getLocation(P(p)),                      
                        Load.getGuard(P(p))));                      
}
  }

  /**
   * Emit code for rule number 158:
   * r: DOUBLE_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code158(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LFDX, Load.getResult(P(p)),           
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 159:
   * rs:      INT_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code159(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LInt, Load.getResult(P(p)),            
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 160:
   * rs:      INT_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code160(AbstractBURS_TreeNode p) {
    {                                                                             
   Address val = AV(Move.getVal(PR(p)));                                      
   EMIT(MIR_Binary.create(PPC_ADDIS, Move.getResult(PR(p)).copyRO(),                   
                          R(Load.getClearAddress(P(p))), CAU16(val)));             
   EMIT(MIR_Load.mutate(P(p), PPC_LInt, Load.getResult(P(p)),         
                        Move.getClearResult(PR(p)), CAL16(val),           
                        Load.getLocation(P(p)),                      
                        Load.getGuard(P(p))));                      
}
  }

  /**
   * Emit code for rule number 161:
   * rs:      INT_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code161(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LIntX, Load.getResult(P(p)),           
                     R(Load.getAddress(P(p))), Load.getOffset(P(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 162:
   * rs:      INT_LOAD(REF_ADD(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code162(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LIntX, Load.getResult(P(p)),           
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 163:
   * rs:      INT_LOAD(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code163(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LInt, Load.getResult(P(p)),            
                     R(Binary.getClearVal1(PL(p))), IC(VR(p)+VLR(p)), 
                     Load.getLocation(P(p)), Load.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 164:
   * stm: BYTE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code164(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STB, R(Store.getValue(P(p))),        
                      R(Store.getAddress(P(p))),                     
                      Store.getOffset(P(p)),                         
                      Store.getLocation(P(p)),                       
                      Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 165:
   * stm: BYTE_STORE(r,OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code165(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STBX, R(Store.getValue(P(p))),       
                      R(Store.getAddress(P(p))),                     
                      Store.getOffset(P(p)),                         
                      Store.getLocation(P(p)),                       
                      Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 166:
   * stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code166(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STB, R(Unary.getClearVal(PL(p))),    
                      R(Store.getAddress(P(p))),                     
                      Store.getOffset(P(p)),                         
                      Store.getLocation(P(p)),                       
                      Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 167:
   * stm: BYTE_STORE(INT_2BYTE(r),OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code167(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STBX, R(Unary.getClearVal(PL(p))),   
                      R(Store.getAddress(P(p))),                     
                      Store.getOffset(P(p)),                         
                      Store.getLocation(P(p)),                       
                      Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 168:
   * stm: SHORT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code168(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STH, R(Store.getValue(P(p))),        
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 169:
   * stm: SHORT_STORE(r,OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code169(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STHX, R(Store.getValue(P(p))),       
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 170:
   * stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code170(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STH, R(Unary.getClearVal(PL(p))),         
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 171:
   * stm: SHORT_STORE(INT_2SHORT(r),OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code171(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STHX, R(Unary.getClearVal(PL(p))),        
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 172:
   * stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code172(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STH, R(Unary.getClearVal(PL(p))),         
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 173:
   * stm: SHORT_STORE(INT_2USHORT(r),OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code173(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STHX, R(Unary.getClearVal(PL(p))),        
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 174:
   * stm: INT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code174(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STW, R(Store.getValue(P(p))),        
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 175:
   * stm: INT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
   * @param p BURS node to apply the rule to
   */
  private void code175(AbstractBURS_TreeNode p) {
    {                                                                             
   Address val = AV(Move.getVal(PRR(p)));                                     
   EMIT(MIR_Binary.create(PPC_ADDIS, Move.getResult(PRR(p)).copyRO(),                  
                          R(Store.getClearAddress(P(p))), CAU16(val)));            
   EMIT(MIR_Store.mutate(P(p), PPC_STW, R(Store.getValue(P(p))),     
                                  Move.getClearResult(PRR(p)), CAL16(val),
                           Store.getLocation(P(p)),                    
                                  Store.getGuard(P(p))));                    
}
  }

  /**
   * Emit code for rule number 176:
   * stm: INT_STORE(r,OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code176(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STWX, R(Store.getValue(P(p))),       
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 177:
   * stm: INT_STORE(r,OTHER_OPERAND(REF_ADD(r,INT_CONSTANT),INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code177(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STW, R(Store.getValue(P(p))),        
                               R(Binary.getClearVal1(PRL(p))),                     
                               IC(VRR(p) + VRLR(p)), 
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 178:
   * stm: FLOAT_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code178(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STFS, R(Store.getValue(P(p))),       
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 179:
   * stm:      FLOAT_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
   * @param p BURS node to apply the rule to
   */
  private void code179(AbstractBURS_TreeNode p) {
    {                                                                             
   Address val = AV(Move.getVal(PRR(p)));                                     
   EMIT(MIR_Binary.create(PPC_ADDIS, Move.getResult(PRR(p)).copyRO(),          
                          R(Store.getAddress(P(p))), CAU16(val)));            
   EMIT(MIR_Store.mutate(P(p), PPC_STFS, R(Store.getValue(P(p))),    
                                  Move.getClearResult(PRR(p)), CAL16(val),
                                  Store.getLocation(P(p)),                    
                                  Store.getGuard(P(p))));                    
}
  }

  /**
   * Emit code for rule number 180:
   * stm: FLOAT_STORE(r,OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code180(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STFSX, R(Store.getValue(P(p))),      
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 181:
   * stm: DOUBLE_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code181(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STFD, R(Store.getValue(P(p))),       
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 182:
   * stm:      DOUBLE_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
   * @param p BURS node to apply the rule to
   */
  private void code182(AbstractBURS_TreeNode p) {
    {                                                                             
   Address val = AV(Move.getVal(PRR(p)));                                     
   EMIT(MIR_Binary.create(PPC_ADDIS, Move.getResult(PRR(p)).copyRO(),         
                          R(Store.getAddress(P(p))), CAU16(val)));            
   EMIT(MIR_Store.mutate(P(p), PPC_STFD, R(Store.getValue(P(p))),    
                                  Move.getClearResult(PRR(p)), CAL16(val),
                                  Store.getLocation(P(p)),                    
                                  Store.getGuard(P(p))));                    
}
  }

  /**
   * Emit code for rule number 183:
   * stm: DOUBLE_STORE(r,OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code183(AbstractBURS_TreeNode p) {
    EMIT(MIR_Store.mutate(P(p), PPC_STFDX, R(Store.getValue(P(p))),      
                               R(Store.getAddress(P(p))),                     
                               Store.getOffset(P(p)),                         
                               Store.getLocation(P(p)),                       
                               Store.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 184:
   * stm: INT_IFCMP(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code184(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP(P(p), R(IfCmp.getClearVal1(P(p))), IfCmp.getClearVal2(P(p)), IfCmp.getCond(P(p)), false);
  }

  /**
   * Emit code for rule number 185:
   * stm: INT_IFCMP(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code185(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP(P(p), R(IfCmp.getClearVal1(P(p))), IfCmp.getClearVal2(P(p)), IfCmp.getCond(P(p)), true);
  }

  /**
   * Emit code for rule number 186:
   * stm: INT_IFCMP(INT_2BYTE(r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code186(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_EXTSBr, Unary.getClearResult(PL(p)),         
                     Unary.getClearVal(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 187:
   * stm: INT_IFCMP(INT_2SHORT(r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code187(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_EXTSHr, Unary.getClearResult(PL(p)),         
                     Unary.getClearVal(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 188:
   * stm: INT_IFCMP(INT_USHR(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code188(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_SRWr, Binary.getClearResult(PL(p)),          
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 189:
   * stm: INT_IFCMP(INT_SHL(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code189(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_SLWr, Binary.getClearResult(PL(p)),          
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 190:
   * stm: INT_IFCMP(INT_SHR(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code190(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_SRAWr, Binary.getClearResult(PL(p)),         
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 191:
   * stm: INT_IFCMP(INT_USHR(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code191(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_SRWIr, Binary.getClearResult(PL(p)),         
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 192:
   * stm: INT_IFCMP(INT_SHL(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code192(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_SLWIr, Binary.getClearResult(PL(p)),         
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 193:
   * stm: INT_IFCMP(INT_SHR(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code193(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_SRAWIr, Binary.getClearResult(PL(p)),        
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 194:
   * stm: INT_IFCMP(REF_AND(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code194(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP_ZERO(P(p), PPC_ANDIr, Binary.getClearResult(PL(p)),         
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 195:
   * stm: INT_IFCMP(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code195(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
EMIT_BOOLCMP_BRANCH(IfCmp.getClearTarget(P(p)), IfCmp.getClearBranchProfile(P(p)));
  }

  /**
   * Emit code for rule number 196:
   * stm: INT_IFCMP(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code196(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
FLIP_BOOLCMP(); EMIT_BOOLCMP_BRANCH(IfCmp.getClearTarget(P(p)), IfCmp.getClearBranchProfile(P(p)));
  }

  /**
   * Emit code for rule number 197:
   * stm: INT_IFCMP(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code197(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
EMIT_BOOLCMP_BRANCH(IfCmp.getClearTarget(P(p)), IfCmp.getClearBranchProfile(P(p)));
  }

  /**
   * Emit code for rule number 198:
   * stm: INT_IFCMP(boolcmp, INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code198(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
FLIP_BOOLCMP(); EMIT_BOOLCMP_BRANCH(IfCmp.getClearTarget(P(p)), IfCmp.getClearBranchProfile(P(p)));
  }

  /**
   * Emit code for rule number 199:
   * stm: INT_IFCMP2(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code199(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp2.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP2(P(p), R(IfCmp2.getClearVal1(P(p))), IfCmp2.getClearVal2(P(p)), IfCmp2.getClearCond1(P(p)), IfCmp2.getClearCond2(P(p)), false);
  }

  /**
   * Emit code for rule number 200:
   * stm: INT_IFCMP2(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code200(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp2.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
CMP2(P(p), R(IfCmp2.getClearVal1(P(p))), IfCmp2.getClearVal2(P(p)), IfCmp2.getClearCond1(P(p)), IfCmp2.getClearCond2(P(p)), true);
  }

  /**
   * Emit code for rule number 201:
   * stm:   FLOAT_IFCMP(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code201(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
DOUBLE_IFCMP(P(p), R(IfCmp.getClearVal1(P(p))), IfCmp.getClearVal2(P(p)));
  }

  /**
   * Emit code for rule number 202:
   * stm:   DOUBLE_IFCMP(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code202(AbstractBURS_TreeNode p) {
    EMIT(CPOS(P(p), Move.create(GUARD_MOVE, IfCmp.getClearGuardResult(P(p)), new TrueGuardOperand()))); 
DOUBLE_IFCMP(P(p), R(IfCmp.getClearVal1(P(p))), IfCmp.getClearVal2(P(p)));
  }

  /**
   * Emit code for rule number 203:
   * stm: FLOAT_CMPL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code203(AbstractBURS_TreeNode p) {
    EMIT (P(p)); //  Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 204:
   * stm: FLOAT_CMPG(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code204(AbstractBURS_TreeNode p) {
    EMIT (P(p)); //  Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 205:
   * stm: DOUBLE_CMPL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code205(AbstractBURS_TreeNode p) {
    EMIT (P(p)); //  Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 206:
   * stm: DOUBLE_CMPG(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code206(AbstractBURS_TreeNode p) {
    EMIT (P(p)); //  Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 207:
   * stm: GOTO
   * @param p BURS node to apply the rule to
   */
  private void code207(AbstractBURS_TreeNode p) {
    EMIT(MIR_Branch.mutate(P(p), PPC_B, Goto.getTarget(P(p))));
  }

  /**
   * Emit code for rule number 208:
   * stm: RETURN(NULL)
   * @param p BURS node to apply the rule to
   */
  private void code208(AbstractBURS_TreeNode p) {
    RETURN(P(p), null);
  }

  /**
   * Emit code for rule number 209:
   * stm: RETURN(r)
   * @param p BURS node to apply the rule to
   */
  private void code209(AbstractBURS_TreeNode p) {
    RETURN(P(p), Return.getVal(P(p)));
  }

  /**
   * Emit code for rule number 210:
   * r: CALL(r,any)
   * @param p BURS node to apply the rule to
   */
  private void code210(AbstractBURS_TreeNode p) {
    CALL(P(p));
  }

  /**
   * Emit code for rule number 211:
   * r: CALL(BRANCH_TARGET,any)
   * @param p BURS node to apply the rule to
   */
  private void code211(AbstractBURS_TreeNode p) {
    CALL(P(p));
  }

  /**
   * Emit code for rule number 212:
   * r: SYSCALL(r,any)
   * @param p BURS node to apply the rule to
   */
  private void code212(AbstractBURS_TreeNode p) {
    SYSCALL(P(p));
  }

  /**
   * Emit code for rule number 213:
   * r: GET_TIME_BASE
   * @param p BURS node to apply the rule to
   */
  private void code213(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 215:
   * r:  YIELDPOINT_OSR(any, any)
   * @param p BURS node to apply the rule to
   */
  private void code215(AbstractBURS_TreeNode p) {
    OSR(burs, P(p));
  }

  /**
   * Emit code for rule number 216:
   * r:      PREPARE_INT(r, r)
   * @param p BURS node to apply the rule to
   */
  private void code216(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LWARX, Prepare.getResult(P(p)),   
                     R(Prepare.getAddress(P(p))), Prepare.getOffset(P(p)),           
                     Prepare.getLocation(P(p)),                  
                     Prepare.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 217:
   * r:      PREPARE_LONG(r, r)
   * @param p BURS node to apply the rule to
   */
  private void code217(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LAddrARX, Prepare.getResult(P(p)),   
                     R(Prepare.getAddress(P(p))), Prepare.getOffset(P(p)),           
                     Prepare.getLocation(P(p)),                  
                     Prepare.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 218:
   * r:      ATTEMPT_INT(r, r)
   * @param p BURS node to apply the rule to
   */
  private void code218(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 219:
   * r:      ATTEMPT_LONG(r, r)
   * @param p BURS node to apply the rule to
   */
  private void code219(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 220:
   * stm: IR_PROLOGUE
   * @param p BURS node to apply the rule to
   */
  private void code220(AbstractBURS_TreeNode p) {
    PROLOGUE(P(p));
  }

  /**
   * Emit code for rule number 221:
   * r: LONG_ADD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code221(AbstractBURS_TreeNode p) {
    LONG_ADD(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 222:
   * r: LONG_SUB(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code222(AbstractBURS_TreeNode p) {
    LONG_SUB(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 223:
   * r: LONG_MUL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code223(AbstractBURS_TreeNode p) {
    LONG_MUL(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 224:
   * r: LONG_NEG(r)
   * @param p BURS node to apply the rule to
   */
  private void code224(AbstractBURS_TreeNode p) {
    LONG_NEG(P(p), Unary.getClearResult(P(p)), R(Unary.getClearVal(P(p))));
  }

  /**
   * Emit code for rule number 225:
   * r: LONG_SHL(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code225(AbstractBURS_TreeNode p) {
    LONG_SHL(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 226:
   * r: LONG_SHL(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code226(AbstractBURS_TreeNode p) {
    LONG_SHL_IMM(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), IC(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 227:
   * r: LONG_SHR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code227(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 228:
   * r: LONG_SHR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code228(AbstractBURS_TreeNode p) {
    LONG_SHR_IMM(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), IC(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 229:
   * r: LONG_USHR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code229(AbstractBURS_TreeNode p) {
    LONG_USHR(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 230:
   * r: LONG_USHR(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code230(AbstractBURS_TreeNode p) {
    LONG_USHR_IMM(P(p), Binary.getResult(P(p)), R(Binary.getVal1(P(p))), IC(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 231:
   * r: LONG_AND(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code231(AbstractBURS_TreeNode p) {
    LONG_LOG(P(p), PPC_AND, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 232:
   * r: LONG_OR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code232(AbstractBURS_TreeNode p) {
    LONG_LOG(P(p), PPC_OR, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 233:
   * r: LONG_XOR(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code233(AbstractBURS_TreeNode p) {
    LONG_LOG(P(p), PPC_XOR, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), R(Binary.getVal2(P(p))));
  }

  /**
   * Emit code for rule number 234:
   * r: LONG_NOT(r)
   * @param p BURS node to apply the rule to
   */
  private void code234(AbstractBURS_TreeNode p) {
    LONG_NOT(P(p), Unary.getClearResult(P(p)), R(Unary.getClearVal(P(p))));
  }

  /**
   * Emit code for rule number 235:
   * r: INT_2LONG(r)
   * @param p BURS node to apply the rule to
   */
  private void code235(AbstractBURS_TreeNode p) {
    INT_2LONG(P(p), Unary.getClearResult(P(p)), R(Unary.getClearVal(P(p))));
  }

  /**
   * Emit code for rule number 236:
   * r: LONG_2INT(r)
   * @param p BURS node to apply the rule to
   */
  private void code236(AbstractBURS_TreeNode p) {
    LONG_2INT(P(p), Unary.getClearResult(P(p)), R(Unary.getClearVal(P(p))));
  }

  /**
   * Emit code for rule number 237:
   * r: DOUBLE_AS_LONG_BITS(r)
   * @param p BURS node to apply the rule to
   */
  private void code237(AbstractBURS_TreeNode p) {
    FPR2GPR_64(P(p));
  }

  /**
   * Emit code for rule number 238:
   * r: LONG_BITS_AS_DOUBLE(r)
   * @param p BURS node to apply the rule to
   */
  private void code238(AbstractBURS_TreeNode p) {
    GPR2FPR_64(P(p));
  }

  /**
   * Emit code for rule number 239:
   * r: LONG_MOVE(LONG_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code239(AbstractBURS_TreeNode p) {
    LONG_CONSTANT(P(p), Move.getResult(P(p)), LC(Move.getVal(P(p))));
  }

  /**
   * Emit code for rule number 240:
   * r: LONG_MOVE(r)
   * @param p BURS node to apply the rule to
   */
  private void code240(AbstractBURS_TreeNode p) {
    LONG_MOVE(P(p), Move.getResult(P(p)), R(Move.getVal(P(p))));
  }

  /**
   * Emit code for rule number 241:
   * stm: LONG_CMP(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code241(AbstractBURS_TreeNode p) {
    EMIT (P(p)); //  Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 242:
   * stm: LONG_IFCMP(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code242(AbstractBURS_TreeNode p) {
    EMIT(P(p)); //  Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code for rule number 243:
   * stm:    INT_IFCMP(ATTEMPT_INT(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code243(AbstractBURS_TreeNode p) {
    {                                                                             
   ConditionOperand c = IfCmp.getCond(P(p)).flipCode();                   
   EMIT(MIR_Store.create(PPC_STWCXr, R(Attempt.getClearNewValue(PL(p))), 
                                  R(Attempt.getClearAddress(PL(p))), Attempt.getClearOffset(PL(p)),           
                                  Attempt.getClearLocation(PL(p)),                
                                  Attempt.getClearGuard(PL(p))));                
   EMIT(MIR_CondBranch.mutate(P(p), PPC_BCOND, CR(0),                         
                              new PowerPCConditionOperand(c),             
                              IfCmp.getTarget(P(p)),                         
                              IfCmp.getBranchProfile(P(p))));                 
}
  }

  /**
   * Emit code for rule number 244:
   * stm:    INT_IFCMP(ATTEMPT_ADDR(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code244(AbstractBURS_TreeNode p) {
    {                                                                             
   ConditionOperand c = IfCmp.getCond(P(p)).flipCode();                   
   EMIT(MIR_Store.create(PPC_STWCXr, R(Attempt.getClearNewValue(PL(p))), 
                                  R(Attempt.getClearAddress(PL(p))), Attempt.getClearOffset(PL(p)),           
                                  Attempt.getClearLocation(PL(p)),                
                                  Attempt.getClearGuard(PL(p))));                
   EMIT(MIR_CondBranch.mutate(P(p), PPC_BCOND, CR(0),                         
                              new PowerPCConditionOperand(c),             
                              IfCmp.getTarget(P(p)),                         
                              IfCmp.getBranchProfile(P(p))));                 
}
  }

  /**
   * Emit code for rule number 245:
   * stm: INT_IFCMP(REF_NEG(r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code245(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_NEGr, Unary.getClearResult(PL(p)),           
                     Unary.getClearVal(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 246:
   * stm: INT_IFCMP(REF_NOT(r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code246(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_XORr, Unary.getClearResult(PL(p)),           
                     R(Unary.getClearVal(PL(p))), Unary.getClearVal(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 247:
   * stm: INT_IFCMP(REF_ADD(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code247(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ADDr, Binary.getClearResult(PL(p)),          
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 248:
   * stm: INT_IFCMP(REF_AND(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code248(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ANDr, Binary.getClearResult(PL(p)),          
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 249:
   * stm: INT_IFCMP(REF_OR(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code249(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ORr, Binary.getClearResult(PL(p)),           
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 250:
   * stm: INT_IFCMP(REF_XOR(r,r),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code250(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_XORr, Binary.getClearResult(PL(p)),          
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 251:
   * stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code251(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ANDISr, Binary.getClearResult(PL(p)),        
                     R(Binary.getClearVal1(PL(p))),                               
                     SRI(IV(Move.getClearVal(PLR(p))), 16), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 252:
   * stm: INT_IFCMP(REF_AND(r,REF_MOVE(INT_CONSTANT)),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code252(AbstractBURS_TreeNode p) {
    CMP_ZERO_AND_MASK(P(p), Binary.getClearResult(PL(p)),           
                              R(Binary.getClearVal1(PL(p))),                      
                              IC(Move.getClearVal(PLR(p))), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 253:
   * stm: INT_IFCMP(REF_ADD(r,INT_CONSTANT),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code253(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ADDICr, Binary.getClearResult(PL(p)),        
                     R(Binary.getClearVal1(PL(p))), Binary.getClearVal2(PL(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 254:
   * stm: INT_IFCMP(REF_AND(r,REF_NOT(r)),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code254(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ANDCr, Binary.getClearResult(PL(p)),         
                     R(Binary.getClearVal1(PL(p))), Unary.getClearVal(PLR(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 255:
   * stm: INT_IFCMP(REF_OR(r,REF_NOT(r)),INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code255(AbstractBURS_TreeNode p) {
    CMP_ZERO(P(p), PPC_ORCr, Binary.getClearResult(PL(p)),          
                     R(Binary.getClearVal1(PL(p))), Unary.getClearVal(PLR(p)), IfCmp.getCond(P(p)));
  }

  /**
   * Emit code for rule number 256:
   * czr: REF_AND(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code256(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ANDISr, Binary.getResult(P(p)),              
                       R(Binary.getVal1(P(p))), IC(Bits.PPCMaskUpper16(VRL(p)))));
  }

  /**
   * Emit code for rule number 257:
   * r: REF_AND(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code257(AbstractBURS_TreeNode p) {
    AND_MASK(P(p), Binary.getClearResult(P(p)), R(Binary.getClearVal1(P(p))), IC(Move.getClearVal(PR(p))));
  }

  /**
   * Emit code for rule number 258:
   * r: REF_OR(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code258(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_ORIS, Binary.getResult(P(p)), R(Binary.getVal1(P(p))), SRI(IV(Move.getVal(PR(p))), 16)));
  }

  /**
   * Emit code for rule number 259:
   * r: REF_OR(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code259(AbstractBURS_TreeNode p) {
    {                                                                             
   int c = IV(Move.getVal(PR(p)));                                        
   EMIT(MIR_Binary.create(PPC_ORI, Binary.getResult(P(p)).copyRO(),           
                          R(Binary.getClearVal1(P(p))), ANDI(c, 0xffff)));         
   EMIT(MIR_Binary.mutate(P(p), PPC_ORIS, Binary.getResult(P(p)).copyRO(),    
                          Binary.getClearResult(P(p)), SRI(c, 16)));      
}
  }

  /**
   * Emit code for rule number 260:
   * r: REF_XOR(r,REF_MOVE(INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code260(AbstractBURS_TreeNode p) {
    EMIT(MIR_Binary.mutate(P(p), PPC_XORIS, Binary.getResult(P(p)), 
                       R(Binary.getVal1(P(p))), SRI(IV(Move.getVal(PR(p))), 16)));
  }

  /**
   * Emit code for rule number 261:
   * r: REF_MOVE(ADDRESS_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code261(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_LDI, Move.getResult(P(p)), CAL16(AV(Move.getVal(P(p))))));
  }

  /**
   * Emit code for rule number 262:
   * r: REF_MOVE(ADDRESS_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code262(AbstractBURS_TreeNode p) {
    EMIT(MIR_Unary.mutate(P(p), PPC_LDIS, Move.getResult(P(p)), CAU16(AV(Move.getVal(P(p))))));
  }

  /**
   * Emit code for rule number 263:
   * r: REF_MOVE(ADDRESS_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code263(AbstractBURS_TreeNode p) {
    {                                                                             
   EMIT(MIR_Unary.create(PPC_LDIS, Move.getResult(P(p)).copyRO(), CAU16(AV(Move.getVal(P(p))))));   
   EMIT(MIR_Binary.mutate(P(p), PPC_ADDI, Move.getResult(P(p)).copyRO(),                   
                          Move.getClearResult(P(p)), CAL16(AV(Move.getVal(P(p))))));   
}
  }

  /**
   * Emit code for rule number 264:
   * r: LONG_LOAD(r,INT_CONSTANT)
   * @param p BURS node to apply the rule to
   */
  private void code264(AbstractBURS_TreeNode p) {
    LONG_LOAD_addi(P(p), Load.getClearResult(P(p)),                  
                           R(Load.getClearAddress(P(p))), IC(Load.getClearOffset(P(p))), 
                           Load.getClearLocation(P(p)), Load.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 265:
   * r:      LONG_LOAD(r,REF_MOVE(ADDRESS_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code265(AbstractBURS_TreeNode p) {
    LONG_LOAD_addis(P(p), Load.getClearResult(P(p)),                 
                            R(Load.getClearAddress(P(p))),                         
                            R(Load.getClearOffset(P(p))), AC(Move.getClearVal(PR(p))),   
                            Load.getClearLocation(P(p)), Load.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 266:
   * r: LONG_LOAD(r,r)
   * @param p BURS node to apply the rule to
   */
  private void code266(AbstractBURS_TreeNode p) {
    LONG_LOAD_addx(P(p), Load.getClearResult(P(p)),                  
                           R(Load.getClearAddress(P(p))), R(Load.getClearOffset(P(p))), 
                           Load.getClearLocation(P(p)), Load.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 267:
   * stm: LONG_STORE(r,OTHER_OPERAND(r,INT_CONSTANT))
   * @param p BURS node to apply the rule to
   */
  private void code267(AbstractBURS_TreeNode p) {
    LONG_STORE_addi(P(p), R(Store.getClearValue(P(p))),              
                            R(Store.getClearAddress(P(p))),                        
                            IC(Store.getClearOffset(P(p))),                         
                            Store.getClearLocation(P(p)),                          
                            Store.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 268:
   * stm:      LONG_STORE(r,OTHER_OPERAND(r,REF_MOVE(ADDRESS_CONSTANT)))
   * @param p BURS node to apply the rule to
   */
  private void code268(AbstractBURS_TreeNode p) {
    LONG_STORE_addis(P(p), R(Store.getClearValue(P(p))),             
                             R(Store.getClearAddress(P(p))),                       
                             R(Store.getClearOffset(P(p))),                        
                             AC(Move.getClearVal(PRR(p))),                          
                             Store.getClearLocation(P(p)),                         
                             Store.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 269:
   * stm: LONG_STORE(r,OTHER_OPERAND(r,r))
   * @param p BURS node to apply the rule to
   */
  private void code269(AbstractBURS_TreeNode p) {
    LONG_STORE_addx(P(p), R(Store.getClearValue(P(p))),              
                            R(Store.getClearAddress(P(p))),                        
                            R(Store.getClearOffset(P(p))),                         
                            Store.getClearLocation(P(p)),                          
                            Store.getClearGuard(P(p)));
  }

  /**
   * Emit code for rule number 270:
   * r:      PREPARE_ADDR(r, r)
   * @param p BURS node to apply the rule to
   */
  private void code270(AbstractBURS_TreeNode p) {
    EMIT(MIR_Load.mutate(P(p), PPC_LWARX, Prepare.getResult(P(p)),   
                              R(Prepare.getAddress(P(p))), Prepare.getOffset(P(p)),           
                              Prepare.getLocation(P(p)),                  
                              Prepare.getGuard(P(p))));
  }

  /**
   * Emit code for rule number 271:
   * r:      ATTEMPT_ADDR(r, r)
   * @param p BURS node to apply the rule to
   */
  private void code271(AbstractBURS_TreeNode p) {
    EMIT(P(p));  // Leave for ComplexLIR2MIRExpansion
  }

  /**
   * Emit code using given rule number
   *
   * @param p the tree that's being emitted
   * @param n the non-terminal goal of that tree
   * @param ruleno the rule that will generate the tree
   */
    @Override
  public void code(AbstractBURS_TreeNode p, int  n, int ruleno) {
    switch(unsortedErnMap[ruleno]) {
    case 14: code14(p); break;
    case 15: code15(p); break;
    case 16: code16(p); break;
    case 17: code17(p); break;
    case 18: code18(p); break;
    case 19: code19(p); break;
    case 20: code20(p); break;
    case 21: code21(p); break;
    case 22: code22(p); break;
    case 24: code24(p); break;
    case 25: code25(p); break;
    case 26: code26(p); break;
    case 27: code27(p); break;
    case 28: code28(p); break;
    case 29: code29(p); break;
    case 30: code30(p); break;
    case 31: code31(p); break;
    case 32: code32(p); break;
    case 33: code33(p); break;
    case 34: code34(p); break;
    case 35: code35(p); break;
    case 36: code36(p); break;
    case 37: code37(p); break;
    case 38: code38(p); break;
    case 39: code39(p); break;
    case 40: code40(p); break;
    case 41: code41(p); break;
    case 42: code42(p); break;
    case 43: code43(p); break;
    case 44: code44(p); break;
    case 45: code45(p); break;
    case 46: code46(p); break;
    case 47: code47(p); break;
    case 48: code48(p); break;
    case 49: code49(p); break;
    case 50: code50(p); break;
    case 51: code51(p); break;
    case 52: code52(p); break;
    case 54: code54(p); break;
    case 56: code56(p); break;
    case 57: code57(p); break;
    case 58: code58(p); break;
    case 59: code59(p); break;
    case 60: code60(p); break;
    case 61: code61(p); break;
    case 62: code62(p); break;
    case 63: code63(p); break;
    case 64: code64(p); break;
    case 65: code65(p); break;
    case 66: code66(p); break;
    case 67: code67(p); break;
    case 68: code68(p); break;
    case 69: code69(p); break;
    case 70: code70(p); break;
    case 71: code71(p); break;
    case 72: code72(p); break;
    case 73: code73(p); break;
    case 74: code74(p); break;
    case 75: code75(p); break;
    case 76: code76(p); break;
    case 77: code77(p); break;
    case 78: code78(p); break;
    case 79: code79(p); break;
    case 80: code80(p); break;
    case 81: code81(p); break;
    case 82: code82(p); break;
    case 83: code83(p); break;
    case 84: code84(p); break;
    case 85: code85(p); break;
    case 86: code86(p); break;
    case 87: code87(p); break;
    case 88: code88(p); break;
    case 89: code89(p); break;
    case 90: code90(p); break;
    case 91: code91(p); break;
    case 92: code92(p); break;
    case 93: code93(p); break;
    case 94: code94(p); break;
    case 95: code95(p); break;
    case 96: code96(p); break;
    case 97: code97(p); break;
    case 98: code98(p); break;
    case 99: code99(p); break;
    case 100: code100(p); break;
    case 101: code101(p); break;
    case 102: code102(p); break;
    case 103: code103(p); break;
    case 104: code104(p); break;
    case 105: code105(p); break;
    case 106: code106(p); break;
    case 107: code107(p); break;
    case 108: code108(p); break;
    case 109: code109(p); break;
    case 110: code110(p); break;
    case 111: code111(p); break;
    case 112: code112(p); break;
    case 113: code113(p); break;
    case 114: code114(p); break;
    case 115: code115(p); break;
    case 116: code116(p); break;
    case 117: code117(p); break;
    case 118: code118(p); break;
    case 119: code119(p); break;
    case 120: code120(p); break;
    case 121: code121(p); break;
    case 122: code122(p); break;
    case 123: code123(p); break;
    case 124: code124(p); break;
    case 125: code125(p); break;
    case 126: code126(p); break;
    case 127: code127(p); break;
    case 128: code128(p); break;
    case 129: code129(p); break;
    case 130: code130(p); break;
    case 131: code131(p); break;
    case 132: code132(p); break;
    case 133: code133(p); break;
    case 134: code134(p); break;
    case 135: code135(p); break;
    case 136: code136(p); break;
    case 137: code137(p); break;
    case 138: code138(p); break;
    case 139: code139(p); break;
    case 140: code140(p); break;
    case 141: code141(p); break;
    case 142: code142(p); break;
    case 143: code143(p); break;
    case 144: code144(p); break;
    case 145: code145(p); break;
    case 146: code146(p); break;
    case 147: code147(p); break;
    case 148: code148(p); break;
    case 149: code149(p); break;
    case 150: code150(p); break;
    case 151: code151(p); break;
    case 152: code152(p); break;
    case 153: code153(p); break;
    case 154: code154(p); break;
    case 155: code155(p); break;
    case 156: code156(p); break;
    case 157: code157(p); break;
    case 158: code158(p); break;
    case 159: code159(p); break;
    case 160: code160(p); break;
    case 161: code161(p); break;
    case 162: code162(p); break;
    case 163: code163(p); break;
    case 164: code164(p); break;
    case 165: code165(p); break;
    case 166: code166(p); break;
    case 167: code167(p); break;
    case 168: code168(p); break;
    case 169: code169(p); break;
    case 170: code170(p); break;
    case 171: code171(p); break;
    case 172: code172(p); break;
    case 173: code173(p); break;
    case 174: code174(p); break;
    case 175: code175(p); break;
    case 176: code176(p); break;
    case 177: code177(p); break;
    case 178: code178(p); break;
    case 179: code179(p); break;
    case 180: code180(p); break;
    case 181: code181(p); break;
    case 182: code182(p); break;
    case 183: code183(p); break;
    case 184: code184(p); break;
    case 185: code185(p); break;
    case 186: code186(p); break;
    case 187: code187(p); break;
    case 188: code188(p); break;
    case 189: code189(p); break;
    case 190: code190(p); break;
    case 191: code191(p); break;
    case 192: code192(p); break;
    case 193: code193(p); break;
    case 194: code194(p); break;
    case 195: code195(p); break;
    case 196: code196(p); break;
    case 197: code197(p); break;
    case 198: code198(p); break;
    case 199: code199(p); break;
    case 200: code200(p); break;
    case 201: code201(p); break;
    case 202: code202(p); break;
    case 203: code203(p); break;
    case 204: code204(p); break;
    case 205: code205(p); break;
    case 206: code206(p); break;
    case 207: code207(p); break;
    case 208: code208(p); break;
    case 209: code209(p); break;
    case 210: code210(p); break;
    case 211: code211(p); break;
    case 212: code212(p); break;
    case 213: code213(p); break;
    case 215: code215(p); break;
    case 216: code216(p); break;
    case 217: code217(p); break;
    case 218: code218(p); break;
    case 219: code219(p); break;
    case 220: code220(p); break;
    case 221: code221(p); break;
    case 222: code222(p); break;
    case 223: code223(p); break;
    case 224: code224(p); break;
    case 225: code225(p); break;
    case 226: code226(p); break;
    case 227: code227(p); break;
    case 228: code228(p); break;
    case 229: code229(p); break;
    case 230: code230(p); break;
    case 231: code231(p); break;
    case 232: code232(p); break;
    case 233: code233(p); break;
    case 234: code234(p); break;
    case 235: code235(p); break;
    case 236: code236(p); break;
    case 237: code237(p); break;
    case 238: code238(p); break;
    case 239: code239(p); break;
    case 240: code240(p); break;
    case 241: code241(p); break;
    case 242: code242(p); break;
    case 243: code243(p); break;
    case 244: code244(p); break;
    case 245: code245(p); break;
    case 246: code246(p); break;
    case 247: code247(p); break;
    case 248: code248(p); break;
    case 249: code249(p); break;
    case 250: code250(p); break;
    case 251: code251(p); break;
    case 252: code252(p); break;
    case 253: code253(p); break;
    case 254: code254(p); break;
    case 255: code255(p); break;
    case 256: code256(p); break;
    case 257: code257(p); break;
    case 258: code258(p); break;
    case 259: code259(p); break;
    case 260: code260(p); break;
    case 261: code261(p); break;
    case 262: code262(p); break;
    case 263: code263(p); break;
    case 264: code264(p); break;
    case 265: code265(p); break;
    case 266: code266(p); break;
    case 267: code267(p); break;
    case 268: code268(p); break;
    case 269: code269(p); break;
    case 270: code270(p); break;
    case 271: code271(p); break;
    default:
      throw new OptimizingCompilerException("BURS", "rule " + ruleno + " without emit code:",
        BURS_Debug.string[unsortedErnMap[ruleno]]);
    }
  }
}
