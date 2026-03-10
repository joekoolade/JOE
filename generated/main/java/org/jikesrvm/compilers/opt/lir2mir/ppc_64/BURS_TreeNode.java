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
package org.jikesrvm.compilers.opt.lir2mir.ppc_64;

import static org.jikesrvm.compilers.opt.ir.Operators.*;
import static org.jikesrvm.compilers.opt.lir2mir.ppc_64.BURS_Definitions.*;

import org.jikesrvm.*;
import org.jikesrvm.compilers.opt.*;
import org.jikesrvm.compilers.opt.depgraph.DepGraphNode;
import org.jikesrvm.compilers.opt.ir.*;
import org.jikesrvm.compilers.opt.ir.operand.*;
import org.jikesrvm.compilers.opt.ir.operand.ppc.*;
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
  private char cost_rs;
  private char cost_rz;
  private char cost_rp;
  private char cost_any;
  private char cost_boolcmp;

  // rule for each non-terminal
  private int word0;
     // stm; word:0 offset:0, bits:7, 83 rules);
     // r; word:0 offset:7, bits:7, 123 rules);
     // czr; word:0 offset:14, bits:1, 1 rules);
     // rs; word:0 offset:15, bits:5, 19 rules);
     // rz; word:0 offset:20, bits:4, 10 rules);
     // rp; word:0 offset:24, bits:4, 15 rules);
     // any; word:0 offset:28, bits:3, 6 rules);
  private int word1;
     // boolcmp; word:1 offset:0, bits:4, 8 rules);

  public final char getCost(int goalNT) {
    switch(goalNT) {
    case stm_NT:    return cost_stm;
    case r_NT:    return cost_r;
    case czr_NT:    return cost_czr;
    case rs_NT:    return cost_rs;
    case rz_NT:    return cost_rz;
    case rp_NT:    return cost_rp;
    case any_NT:    return cost_any;
    default:       return cost_boolcmp;
    }
  }

  public final void setCost(int goalNT, char cost) {
    switch(goalNT) {
    case stm_NT:    cost_stm = cost; break;
    case r_NT:    cost_r = cost; break;
    case czr_NT:    cost_czr = cost; break;
    case rs_NT:    cost_rs = cost; break;
    case rz_NT:    cost_rz = cost; break;
    case rp_NT:    cost_rp = cost; break;
    case any_NT:    cost_any = cost; break;
    default:       cost_boolcmp = cost; break;
    }
  }

  @Override
  public final void initCost() {
    cost_stm = 
    cost_r = 
    cost_czr = 
    cost_rs = 
    cost_rz = 
    cost_rp = 
    cost_any = 
    cost_boolcmp = 
          0x7fff;
    word0 = 0;
    word1 = 0;

  }

  @Override
  public final void writePacked(int word, int mask, int shiftedValue) {

    switch(word) {
    case 0: word0 = (word0 & mask) | shiftedValue; break;
    case 1: word1 = (word1 & mask) | shiftedValue; break;

    default: OptimizingCompilerException.UNREACHABLE();
    }

  }

  @Override
  public final int readPacked(int word, int shift, int mask) {

    switch(word) {
    case 0: return (word0 >>> shift) & mask;
    case 1: return (word1 >>> shift) & mask;

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
      stateNT = readPacked(0, 0, 0x7F);
      break;
    case r_NT:
      stateNT = readPacked(0, 7, 0x7F);
      break;
    case czr_NT:
      stateNT = readPacked(0, 14, 0x1);
      break;
    case rs_NT:
      stateNT = readPacked(0, 15, 0x1F);
      break;
    case rz_NT:
      stateNT = readPacked(0, 20, 0xF);
      break;
    case rp_NT:
      stateNT = readPacked(0, 24, 0xF);
      break;
    case any_NT:
      stateNT = readPacked(0, 28, 0x7);
      break;
    default: // boolcmp_NT
      stateNT = readPacked(1, 0, 0xF);
      break;
    }
    return BURS_STATE.decode(goalNT, stateNT);
  }
}

