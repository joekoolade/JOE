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
package org.jikesrvm.compilers.opt.lir2mir.ppc;

import org.jikesrvm.*;
import org.jikesrvm.compilers.opt.*;
import org.jikesrvm.compilers.opt.depgraph.DepGraphNode;
import org.jikesrvm.compilers.opt.ir.*;
import org.jikesrvm.compilers.opt.ir.operand.*;
import org.jikesrvm.compilers.opt.ir.operand.ppc.*;

/**
 * An BURS_TreeNode is a node in a binary tree that is fed
 * as input to BURS.
 * Machine-generated, do not edit.
 *
 * @see org.jikesrvm.compilers.opt.lir2mir.BURS
 * @see BURS_STATE
 */
public class BURS_TreeNode implements BURS_Definitions {

  public ArchitectureSpecificOpt.BURS_TreeNode child1;
  public ArchitectureSpecificOpt.BURS_TreeNode child2;

  /**
   * Dependence graph node corresponding to
   * interior node in BURS tree (set to null for
   * leaf node or for OTHER_OPERAND node).
   */
  public final DepGraphNode dg_node;

  /**
   * Opcode of instruction
   */
  private final char opcode;

  /**
   * nonterminal > 0 ==> this tree node is the
   * root of a "supernode"; the value of nonterminal
   *  identifies the matching non-terminal
   * nonterminal = 0 ==> this tree node is NOT the
   * root of a "supernode".
   */
  private byte nonterminal;

  /**
   * <pre>
   * trrr rrrr
   * t = tree root
   * r = num of registers used
   * </pre>
   */
  private byte treeroot_registersused;

  public final char getOpcode() {
    return opcode;
  }

  public final int getNonTerminal() {
     return (int)nonterminal & 0xFF;
  }

  public final void setNonTerminal(int nonterminal) {
     if (VM.VerifyAssertions) VM._assert(nonterminal <= 0xff);
     this.nonterminal = (byte)nonterminal;
  }

  public final boolean isTreeRoot() {
     return (treeroot_registersused & 0x80 ) != 0;
  }

  public final void setTreeRoot() {
     treeroot_registersused |= 0x80;
  }

  public final void setNumRegisters(int r) {
    treeroot_registersused = (byte)((treeroot_registersused & 0x80) | (r & 0x7f));
  }
  public final int numRegisters() {
    return treeroot_registersused & 0x7f;
  }

  public final Instruction getInstruction() {
     return dg_node._instr;
  }

  /**
   * Constructor for interior node.
   */
  public BURS_TreeNode(DepGraphNode n) {
    Instruction instr = n._instr;
    dg_node = n;
    opcode = instr.getOpcode();
  }

  /**
   * Constructor for leaf/auxiliary node.
   */
  public BURS_TreeNode(char Opcode) {
    dg_node = null;
    opcode = Opcode;
  }

  @Override
  public String toString() {
    String node;
    node = OperatorNames.operatorName[getOpcode()];
    return node;
  }

  public final boolean isSuperNodeRoot() {
    return (getNonTerminal() > 0 );
  }

  public final boolean isREGISTERNode() {
    return getOpcode() == Operators.REGISTER_opcode;
  }

/**** remainder will be inserted by the BURS generator *********/

// program generated file, do not edit

	// cost for each non-terminal
	public char cost_stm;
	public char cost_r;
	public char cost_czr;
	public char cost_rs;
	public char cost_rz;
	public char cost_rp;
	public char cost_any;
	public char cost_boolcmp;

	// rule for each non-terminal
	public int word0;
	   // stm; word:0 offset:0, bits:7, 82 rules);
	   // r; word:0 offset:7, bits:7, 122 rules);
	   // czr; word:0 offset:14, bits:1, 1 rules);
	   // rs; word:0 offset:15, bits:5, 19 rules);
	   // rz; word:0 offset:20, bits:4, 10 rules);
	   // rp; word:0 offset:24, bits:4, 15 rules);
	   // any; word:0 offset:28, bits:3, 6 rules);
	public int word1;
	   // boolcmp; word:1 offset:0, bits:4, 8 rules);

	public char getCost(int goalNT) {
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

	public void initCost() {
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

	public int rule(int goalNT) {
		int statement = 0;
		switch(goalNT) {
		case stm_NT:  statement= (word0 & 0x7F); break;// stm
		case r_NT:  statement= ((word0 >>> 7) & 0x7F); break;// r
		case czr_NT:  statement= ((word0 >>> 14) & 0x1); break;// czr
		case rs_NT:  statement= ((word0 >>> 15) & 0x1F); break;// rs
		case rz_NT:  statement= ((word0 >>> 20) & 0xF); break;// rz
		case rp_NT:  statement= ((word0 >>> 24) & 0xF); break;// rp
		case any_NT:  statement= ((word0 >>> 28) & 0x7); break;// any
		default:     statement= (word1 & 0xF); break;// boolcmp
		}
		return BURS_STATE.decode[goalNT][statement];
	}
}

