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
package org.jikesrvm.compilers.opt.lir2mir.ia32_64;

import static org.jikesrvm.compilers.opt.ir.Operators.*;
import static org.jikesrvm.compilers.opt.lir2mir.ia32_64.BURS_Definitions.*;

import org.jikesrvm.*;
import org.jikesrvm.compilers.opt.*;
import org.jikesrvm.compilers.opt.depgraph.DepGraphNode;
import org.jikesrvm.compilers.opt.ir.*;
import org.jikesrvm.compilers.opt.ir.operand.*;
import org.jikesrvm.compilers.opt.ir.operand.ia32.*;
import org.jikesrvm.compilers.opt.lir2mir.AbstractBURS_TreeNode;

import org.vmmagic.pragma.Inline;

/**
 * An BURS_TreeNode is a node in a binary tree that is fed
 * as input to BURS.
 * <p>
 * Machine-generated, do not edit! If you need to make changes
 * to this file, edit BURS_TreeNode.template.
 *
 * @see org.jikesrvm.compilers.opt.lir2mir.BURS
 * @see BURS_STATE
 */
public class BURS_TreeNode extends AbstractBURS_TreeNode {

  /**
   * Constructor for interior node.
   *
   * @param n corresponding node from the dependence graph
   */
  public BURS_TreeNode(DepGraphNode n) {
    super(n);
  }

  /**
   * Constructor for leaf/auxiliary node.
   *
   * @param opcode the opcode to use for the node
   */
  public BURS_TreeNode(char opcode) {
    super(opcode);
  }

/**** remainder will be inserted by the BURS generator *********/

// program generated file, do not edit

  // cost for each non-terminal
  private char cost_stm;
  private char cost_r;
  private char cost_czr;
  private char cost_cz;
  private char cost_szpr;
  private char cost_szp;
  private char cost_riv;
  private char cost_rlv;
  private char cost_any;
  private char cost_load32;
  private char cost_uload8;
  private char cost_load8_16_32;
  private char cost_load16_32;
  private char cost_load16;
  private char cost_address1scaledreg;
  private char cost_address1reg;
  private char cost_address;
  private char cost_bittest;
  private char cost_boolcmp;
  private char cost_load64;
  private char cost_load8;
  private char cost_sload16;
  private char cost_uload16;
  private char cost_sload8;
  private char cost_load8_16_32_64;
  private char cost_float_load;
  private char cost_double_load;

  // rule for each non-terminal
  private int word0;
     // stm; word:0 offset:0, bits:8, 225 rules);
     // r; word:0 offset:8, bits:9, 260 rules);
     // czr; word:0 offset:17, bits:4, 15 rules);
     // cz; word:0 offset:21, bits:2, 2 rules);
     // szpr; word:0 offset:23, bits:6, 43 rules);
  private int word1;
     // szp; word:1 offset:0, bits:4, 8 rules);
     // riv; word:1 offset:4, bits:2, 2 rules);
     // rlv; word:1 offset:6, bits:2, 2 rules);
     // any; word:1 offset:8, bits:3, 5 rules);
     // load32; word:1 offset:11, bits:3, 7 rules);
     // uload8; word:1 offset:14, bits:3, 4 rules);
     // load8_16_32; word:1 offset:17, bits:2, 2 rules);
     // load16_32; word:1 offset:19, bits:2, 2 rules);
     // load16; word:1 offset:21, bits:2, 3 rules);
     // address1scaledreg; word:1 offset:23, bits:3, 5 rules);
     // address1reg; word:1 offset:26, bits:3, 6 rules);
  private int word2;
     // address; word:2 offset:0, bits:4, 13 rules);
     // bittest; word:2 offset:4, bits:4, 10 rules);
     // boolcmp; word:2 offset:8, bits:4, 13 rules);
     // load64; word:2 offset:12, bits:3, 5 rules);
     // load8; word:2 offset:15, bits:2, 3 rules);
     // sload16; word:2 offset:17, bits:3, 4 rules);
     // uload16; word:2 offset:20, bits:3, 4 rules);
     // sload8; word:2 offset:23, bits:2, 3 rules);
     // load8_16_32_64; word:2 offset:25, bits:2, 2 rules);
     // float_load; word:2 offset:27, bits:3, 5 rules);
  private int word3;
     // double_load; word:3 offset:0, bits:3, 6 rules);

  public final char getCost(int goalNT) {
    switch(goalNT) {
    case stm_NT:    return cost_stm;
    case r_NT:    return cost_r;
    case czr_NT:    return cost_czr;
    case cz_NT:    return cost_cz;
    case szpr_NT:    return cost_szpr;
    case szp_NT:    return cost_szp;
    case riv_NT:    return cost_riv;
    case rlv_NT:    return cost_rlv;
    case any_NT:    return cost_any;
    case load32_NT:    return cost_load32;
    case uload8_NT:    return cost_uload8;
    case load8_16_32_NT:    return cost_load8_16_32;
    case load16_32_NT:    return cost_load16_32;
    case load16_NT:    return cost_load16;
    case address1scaledreg_NT:    return cost_address1scaledreg;
    case address1reg_NT:    return cost_address1reg;
    case address_NT:    return cost_address;
    case bittest_NT:    return cost_bittest;
    case boolcmp_NT:    return cost_boolcmp;
    case load64_NT:    return cost_load64;
    case load8_NT:    return cost_load8;
    case sload16_NT:    return cost_sload16;
    case uload16_NT:    return cost_uload16;
    case sload8_NT:    return cost_sload8;
    case load8_16_32_64_NT:    return cost_load8_16_32_64;
    case float_load_NT:    return cost_float_load;
    default:       return cost_double_load;
    }
  }

  public final void setCost(int goalNT, char cost) {
    switch(goalNT) {
    case stm_NT:    cost_stm = cost; break;
    case r_NT:    cost_r = cost; break;
    case czr_NT:    cost_czr = cost; break;
    case cz_NT:    cost_cz = cost; break;
    case szpr_NT:    cost_szpr = cost; break;
    case szp_NT:    cost_szp = cost; break;
    case riv_NT:    cost_riv = cost; break;
    case rlv_NT:    cost_rlv = cost; break;
    case any_NT:    cost_any = cost; break;
    case load32_NT:    cost_load32 = cost; break;
    case uload8_NT:    cost_uload8 = cost; break;
    case load8_16_32_NT:    cost_load8_16_32 = cost; break;
    case load16_32_NT:    cost_load16_32 = cost; break;
    case load16_NT:    cost_load16 = cost; break;
    case address1scaledreg_NT:    cost_address1scaledreg = cost; break;
    case address1reg_NT:    cost_address1reg = cost; break;
    case address_NT:    cost_address = cost; break;
    case bittest_NT:    cost_bittest = cost; break;
    case boolcmp_NT:    cost_boolcmp = cost; break;
    case load64_NT:    cost_load64 = cost; break;
    case load8_NT:    cost_load8 = cost; break;
    case sload16_NT:    cost_sload16 = cost; break;
    case uload16_NT:    cost_uload16 = cost; break;
    case sload8_NT:    cost_sload8 = cost; break;
    case load8_16_32_64_NT:    cost_load8_16_32_64 = cost; break;
    case float_load_NT:    cost_float_load = cost; break;
    default:       cost_double_load = cost; break;
    }
  }

  @Override
  public final void initCost() {
    cost_stm = 
    cost_r = 
    cost_czr = 
    cost_cz = 
    cost_szpr = 
    cost_szp = 
    cost_riv = 
    cost_rlv = 
    cost_any = 
    cost_load32 = 
    cost_uload8 = 
    cost_load8_16_32 = 
    cost_load16_32 = 
    cost_load16 = 
    cost_address1scaledreg = 
    cost_address1reg = 
    cost_address = 
    cost_bittest = 
    cost_boolcmp = 
    cost_load64 = 
    cost_load8 = 
    cost_sload16 = 
    cost_uload16 = 
    cost_sload8 = 
    cost_load8_16_32_64 = 
    cost_float_load = 
    cost_double_load = 
          0x7fff;
    word0 = 0;
    word1 = 0;
    word2 = 0;
    word3 = 0;

  }

  @Override
  public final void writePacked(int word, int mask, int shiftedValue) {

    switch(word) {
    case 0: word0 = (word0 & mask) | shiftedValue; break;
    case 1: word1 = (word1 & mask) | shiftedValue; break;
    case 2: word2 = (word2 & mask) | shiftedValue; break;
    case 3: word3 = (word3 & mask) | shiftedValue; break;

    default: OptimizingCompilerException.UNREACHABLE();
    }

  }

  @Override
  public final int readPacked(int word, int shift, int mask) {

    switch(word) {
    case 0: return (word0 >>> shift) & mask;
    case 1: return (word1 >>> shift) & mask;
    case 2: return (word2 >>> shift) & mask;
    case 3: return (word3 >>> shift) & mask;

    default: OptimizingCompilerException.UNREACHABLE(); return -1;
    }

  }

  /**
   * Get the BURS rule number associated with this tree node for a given non-terminal
   *
   * @param goalNT the non-terminal we want to know the rule for (e.g. stm_NT)
   * @return the rule number
   */
  @Inline
  public final int rule(int goalNT) {
    int stateNT;
    switch(goalNT) {
    case stm_NT:
      stateNT = readPacked(0, 0, 0xFF);
      break;
    case r_NT:
      stateNT = readPacked(0, 8, 0x1FF);
      break;
    case czr_NT:
      stateNT = readPacked(0, 17, 0xF);
      break;
    case cz_NT:
      stateNT = readPacked(0, 21, 0x3);
      break;
    case szpr_NT:
      stateNT = readPacked(0, 23, 0x3F);
      break;
    case szp_NT:
      stateNT = readPacked(1, 0, 0xF);
      break;
    case riv_NT:
      stateNT = readPacked(1, 4, 0x3);
      break;
    case rlv_NT:
      stateNT = readPacked(1, 6, 0x3);
      break;
    case any_NT:
      stateNT = readPacked(1, 8, 0x7);
      break;
    case load32_NT:
      stateNT = readPacked(1, 11, 0x7);
      break;
    case uload8_NT:
      stateNT = readPacked(1, 14, 0x7);
      break;
    case load8_16_32_NT:
      stateNT = readPacked(1, 17, 0x3);
      break;
    case load16_32_NT:
      stateNT = readPacked(1, 19, 0x3);
      break;
    case load16_NT:
      stateNT = readPacked(1, 21, 0x3);
      break;
    case address1scaledreg_NT:
      stateNT = readPacked(1, 23, 0x7);
      break;
    case address1reg_NT:
      stateNT = readPacked(1, 26, 0x7);
      break;
    case address_NT:
      stateNT = readPacked(2, 0, 0xF);
      break;
    case bittest_NT:
      stateNT = readPacked(2, 4, 0xF);
      break;
    case boolcmp_NT:
      stateNT = readPacked(2, 8, 0xF);
      break;
    case load64_NT:
      stateNT = readPacked(2, 12, 0x7);
      break;
    case load8_NT:
      stateNT = readPacked(2, 15, 0x3);
      break;
    case sload16_NT:
      stateNT = readPacked(2, 17, 0x7);
      break;
    case uload16_NT:
      stateNT = readPacked(2, 20, 0x7);
      break;
    case sload8_NT:
      stateNT = readPacked(2, 23, 0x3);
      break;
    case load8_16_32_64_NT:
      stateNT = readPacked(2, 25, 0x3);
      break;
    case float_load_NT:
      stateNT = readPacked(2, 27, 0x7);
      break;
    default: // double_load_NT
      stateNT = readPacked(3, 0, 0x7);
      break;
    }
    return BURS_STATE.decode(goalNT, stateNT);
  }
}

