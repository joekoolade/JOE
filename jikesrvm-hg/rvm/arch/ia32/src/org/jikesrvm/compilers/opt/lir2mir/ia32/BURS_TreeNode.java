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
package org.jikesrvm.compilers.opt.lir2mir.ia32;

import org.jikesrvm.*;
import org.jikesrvm.compilers.opt.*;
import org.jikesrvm.compilers.opt.depgraph.DepGraphNode;
import org.jikesrvm.compilers.opt.ir.*;
import org.jikesrvm.compilers.opt.ir.operand.*;
import org.jikesrvm.compilers.opt.ir.operand.ia32.*;

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
	public char cost_cz;
	public char cost_szpr;
	public char cost_szp;
	public char cost_riv;
	public char cost_rlv;
	public char cost_any;
	public char cost_sload8;
	public char cost_uload8;
	public char cost_load8;
	public char cost_sload16;
	public char cost_uload16;
	public char cost_load16;
	public char cost_load32;
	public char cost_load16_32;
	public char cost_load8_16_32;
	public char cost_load64;
	public char cost_address1scaledreg;
	public char cost_address1reg;
	public char cost_address;
	public char cost_boolcmp;
	public char cost_bittest;
	public char cost_float_load;
	public char cost_double_load;

	// rule for each non-terminal
	public int word0;
	   // stm; word:0 offset:0, bits:8, 142 rules);
	   // r; word:0 offset:8, bits:8, 222 rules);
	   // czr; word:0 offset:16, bits:3, 7 rules);
	   // cz; word:0 offset:19, bits:2, 2 rules);
	   // szpr; word:0 offset:21, bits:5, 22 rules);
	   // szp; word:0 offset:26, bits:3, 5 rules);
	   // riv; word:0 offset:29, bits:2, 2 rules);
	public int word1;
	   // rlv; word:1 offset:0, bits:2, 2 rules);
	   // any; word:1 offset:2, bits:3, 5 rules);
	   // sload8; word:1 offset:5, bits:2, 3 rules);
	   // uload8; word:1 offset:7, bits:3, 4 rules);
	   // load8; word:1 offset:10, bits:2, 3 rules);
	   // sload16; word:1 offset:12, bits:2, 3 rules);
	   // uload16; word:1 offset:14, bits:2, 3 rules);
	   // load16; word:1 offset:16, bits:2, 3 rules);
	   // load32; word:1 offset:18, bits:3, 6 rules);
	   // load16_32; word:1 offset:21, bits:2, 2 rules);
	   // load8_16_32; word:1 offset:23, bits:2, 2 rules);
	   // load64; word:1 offset:25, bits:3, 4 rules);
	   // address1scaledreg; word:1 offset:28, bits:3, 4 rules);
	public int word2;
	   // address1reg; word:2 offset:0, bits:3, 4 rules);
	   // address; word:2 offset:3, bits:4, 8 rules);
	   // boolcmp; word:2 offset:7, bits:4, 10 rules);
	   // bittest; word:2 offset:11, bits:4, 10 rules);
	   // float_load; word:2 offset:15, bits:3, 4 rules);
	   // double_load; word:2 offset:18, bits:3, 4 rules);

	public char getCost(int goalNT) {
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
		case sload8_NT:    return cost_sload8;
		case uload8_NT:    return cost_uload8;
		case load8_NT:    return cost_load8;
		case sload16_NT:    return cost_sload16;
		case uload16_NT:    return cost_uload16;
		case load16_NT:    return cost_load16;
		case load32_NT:    return cost_load32;
		case load16_32_NT:    return cost_load16_32;
		case load8_16_32_NT:    return cost_load8_16_32;
		case load64_NT:    return cost_load64;
		case address1scaledreg_NT:    return cost_address1scaledreg;
		case address1reg_NT:    return cost_address1reg;
		case address_NT:    return cost_address;
		case boolcmp_NT:    return cost_boolcmp;
		case bittest_NT:    return cost_bittest;
		case float_load_NT:    return cost_float_load;
		default:       return cost_double_load;
		}
	}

	public void initCost() {
		cost_stm = 
		cost_r = 
		cost_czr = 
		cost_cz = 
		cost_szpr = 
		cost_szp = 
		cost_riv = 
		cost_rlv = 
		cost_any = 
		cost_sload8 = 
		cost_uload8 = 
		cost_load8 = 
		cost_sload16 = 
		cost_uload16 = 
		cost_load16 = 
		cost_load32 = 
		cost_load16_32 = 
		cost_load8_16_32 = 
		cost_load64 = 
		cost_address1scaledreg = 
		cost_address1reg = 
		cost_address = 
		cost_boolcmp = 
		cost_bittest = 
		cost_float_load = 
		cost_double_load = 
		      0x7fff;
		word0 = 0;
		word1 = 0;
		word2 = 0;

	}

	public int rule(int goalNT) {
		int statement = 0;
		switch(goalNT) {
		case stm_NT:  statement= (word0 & 0xFF); break;// stm
		case r_NT:  statement= ((word0 >>> 8) & 0xFF); break;// r
		case czr_NT:  statement= ((word0 >>> 16) & 0x7); break;// czr
		case cz_NT:  statement= ((word0 >>> 19) & 0x3); break;// cz
		case szpr_NT:  statement= ((word0 >>> 21) & 0x1F); break;// szpr
		case szp_NT:  statement= ((word0 >>> 26) & 0x7); break;// szp
		case riv_NT:  statement= ((word0 >>> 29) & 0x3); break;// riv
		case rlv_NT:  statement= (word1 & 0x3); break;// rlv
		case any_NT:  statement= ((word1 >>> 2) & 0x7); break;// any
		case sload8_NT:  statement= ((word1 >>> 5) & 0x3); break;// sload8
		case uload8_NT:  statement= ((word1 >>> 7) & 0x7); break;// uload8
		case load8_NT:  statement= ((word1 >>> 10) & 0x3); break;// load8
		case sload16_NT:  statement= ((word1 >>> 12) & 0x3); break;// sload16
		case uload16_NT:  statement= ((word1 >>> 14) & 0x3); break;// uload16
		case load16_NT:  statement= ((word1 >>> 16) & 0x3); break;// load16
		case load32_NT:  statement= ((word1 >>> 18) & 0x7); break;// load32
		case load16_32_NT:  statement= ((word1 >>> 21) & 0x3); break;// load16_32
		case load8_16_32_NT:  statement= ((word1 >>> 23) & 0x3); break;// load8_16_32
		case load64_NT:  statement= ((word1 >>> 25) & 0x7); break;// load64
		case address1scaledreg_NT:  statement= ((word1 >>> 28) & 0x7); break;// address1scaledreg
		case address1reg_NT:  statement= (word2 & 0x7); break;// address1reg
		case address_NT:  statement= ((word2 >>> 3) & 0xF); break;// address
		case boolcmp_NT:  statement= ((word2 >>> 7) & 0xF); break;// boolcmp
		case bittest_NT:  statement= ((word2 >>> 11) & 0xF); break;// bittest
		case float_load_NT:  statement= ((word2 >>> 15) & 0x7); break;// float_load
		default:     statement= ((word2 >>> 18) & 0x7); break;// double_load
		}
		return BURS_STATE.decode[goalNT][statement];
	}
}

