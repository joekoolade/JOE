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
//OperatorClass.java
package org.jikesrvm.compilers.opt.instrsched;

import org.jikesrvm.*;
import org.jikesrvm.compilers.opt.ir.*;
import java.util.ArrayList;

/**
 * Generated from a template.
 * Consists of an operator class and information about resource usage
 * There is only one instance of each OperatorClass, which is stored
 * as a static final field in OperatorClass.  You can compare
 * OperatorClasses using ==.
 * Every Operator contains one of these.
 *
 * @see Operator
 * @see Operators
 */
public final class OperatorClass implements Operators {

   // debug level (0 = no debug)
   private static final int verbose = 0;

   private static void debug(String s) {
      System.err.println(s);
   }
   private static String SPACES = null;
   private static void debug(int depth, String s) {
      if (SPACES == null) SPACES = dup(7200, ' ');
      debug(SPACES.substring(0,depth*2)+s);
   }

   // Padding
   // For internal use only.
   private static final String ZEROS = dup(32, '0');
   private static String toBinaryPad32(int value) {
      String s = Integer.toBinaryString(value);
      return ZEROS.substring(s.length())+s;
   }

   // Returns a special resource type embodying all resources of a given class.
   // For internal use only.
   private static int all_units(int rclass) { return rclass | 0x80000000; }

   /**
    * Empty Resources Mask
    */
   static int NONE = 0;

   /**
    * All Resources Mask
    */
   static int ALL = 0;          // will be filled in

   // Generates an array of resource masks, and updating the static field
   // ALL to contain all of the masks.
   // For internal use only.
   private static int M = 1;    // current mask
   private static int[] genMasks(int number) {
      int[] rs = new int[number + 1];
      int rall = 0;
      for (int i = 0; i < number; i++) {
         if (VM.VerifyAssertions && M == 0)
            throw new InternalError("Exceeded 32 resources");
         //System.err.println("Scheduler: Resource "+M);
         rs[i] = M;
         ALL |= M;
         rall |= M;
         M <<= 1;
      }
      rs[number] = rall;
      return rs;
   }

   /**
    * Resource Masks
    */
   private static final int[][] resources = {
      genMasks(4),    // ISSUE
      genMasks(2),    // FXU
      genMasks(1),    // FPU
      genMasks(1),    // BRU
      genMasks(1),    // LDST
      genMasks(1),    // FXUC
      genMasks(1),    // CR
      genMasks(1),    // RESERVE
      null
   };

   /**
    * Total number of resources
    */
   static final int N = resources.length - 1;

   /**
    * Resource Names
    */
   private static final String[] resource_names = {
      "Issue slot",    // ISSUE
      "Fixed point unit",    // FXU
      "Floating point unit",    // FPU
      "Branch unit",    // BRU
      "Load/store unit",    // LDST
      "Complex fixed point unit",    // FXUC
      "Condition register",    // CR
      "Memory reservation",    // RESERVE
      null
   };

   /**
    * Resources
    */

   /**
    * Issue slot
    * The architecture contains 4 of them
    * Every "real" instruction must use at least one
    */
   static final int ISSUE = 0;
   // Combined instances of ISSUE
   static final int ISSUE_ALL = ISSUE | 0x80000000;

   /**
    * Fixed point unit
    * The architecture contains 2 of them
    * Used by fixed point arithmetic operations
    */
   static final int FXU = 1;
   // Combined instances of FXU
   static final int FXU_ALL = FXU | 0x80000000;

   /**
    * Floating point unit
    * The architecture contains 1 of them
    * Used by floating point arithmetic operations
    */
   static final int FPU = 2;
   // Combined instances of FPU
   static final int FPU_ALL = FPU | 0x80000000;

   /**
    * Branch unit
    * The architecture contains 1 of them
    * Used by branch and switch operations
    */
   static final int BRU = 3;
   // Combined instances of BRU
   static final int BRU_ALL = BRU | 0x80000000;

   /**
    * Load/store unit
    * The architecture contains 1 of them
    * Used by memory operations
    */
   static final int LDST = 4;
   // Combined instances of LDST
   static final int LDST_ALL = LDST | 0x80000000;

   /**
    * Complex fixed point unit
    * The architecture contains 1 of them
    * Used by various special fixed point operations
    */
   static final int FXUC = 5;
   // Combined instances of FXUC
   static final int FXUC_ALL = FXUC | 0x80000000;

   /**
    * Condition register
    * The architecture contains 1 of them
    * Stores flags and predicate bits
    */
   static final int CR = 6;
   // Combined instances of CR
   static final int CR_ALL = CR | 0x80000000;

   /**
    * Memory reservation
    * The architecture contains 1 of them
    * Used by all LWARX and STWCX.  NB: do we need a more fine-grain model?
    */
   static final int RESERVE = 7;
   // Combined instances of RESERVE
   static final int RESERVE_ALL = RESERVE | 0x80000000;


   /**
    * Id of the operator class
    */
   private int id = 0;

   /**
    * Maximum Latency of any instruction
    */
   private int maxlat = 0;

   /**
    * Returns the maximum latency of any instruction in the class.
    * Note: it is faster to simply check the field directly, if possible.
    */
   public int maxLatency() { return maxlat; }

   /**
    * Latencies to other classes
    */
   private final ArrayList<Integer> latencies;

   // Returns latency lookup in the hashtable for a given operator class.
   // For internal use only.
   private Object latObj(OperatorClass opclass) {
      int latsize = latencies.size();
      Object latObj = null;
      if (latsize > opclass.id) latObj = latencies.get(opclass.id);

      // walk through backwards, since any_insn (most general) is first
      ArrayList<OperatorClass> opcrc = opclass.rclasses;
      for (int i = opcrc.size(); latObj == null && i > 0; i--) {
         OperatorClass rc = opcrc.get(i - 1);
         if (latsize > rc.id) latObj = latencies.get(rc.id);
      }

      for (int i = rclasses.size(); latObj == null && i > 0; i--) {
         OperatorClass rc = rclasses.get(i - 1);
         latObj = rc.latObj(opclass);
      }

      return latObj;
   }

   /**
    * Sets the operator class (for hierarchy)
    *
    * @param opClass operator class
    */
   public void setOpClass(OperatorClass opClass) {
      rclasses.add(opClass);
   }

   /**
    * Returns the latency between instructions in this class and given class
    *
    * @param opclass destination operator class
    * @return latency to given operator class
    */
   public int latency(OperatorClass opclass) {
      return (Integer) latObj(opclass);
   }

   /**
    * Sets the latency between instructions in this class and given class
    *
    * @param opclass destination operator class
    * @param latency desired latency
    */
   public void setLatency(OperatorClass opclass, int latency) {
      int latencies_size = latencies.size();
      if (opclass.id < latencies_size) {
         latencies.set(opclass.id, latency);
      }
      else {
         for(; latencies_size < opclass.id; latencies_size++) {
            latencies.add(null);
         }
         latencies.add(latency);
      }
   }
   /**
    * Sets the latency between instructions in given class and this class
    *
    * @param opclass source operator class
    * @param latency desired latency
    */
   public void setRevLatency(OperatorClass opclass, int latency) {
      opclass.setLatency(this, latency);
   }

   /*
    * Operator Classes
    */

   // Global class embodying all operator classes.  For internal use only.
   private static final OperatorClass any_insn = new OperatorClass(0);

   // Global class embodying all operator classes using ISSUE.  For internal use only.
   private static final OperatorClass
   ISSUE_insn = new OperatorClass(0+1);

   // Global class embodying all operator classes using FXU.  For internal use only.
   private static final OperatorClass
   FXU_insn = new OperatorClass(1+1);

   // Global class embodying all operator classes using FPU.  For internal use only.
   private static final OperatorClass
   FPU_insn = new OperatorClass(2+1);

   // Global class embodying all operator classes using BRU.  For internal use only.
   private static final OperatorClass
   BRU_insn = new OperatorClass(3+1);

   // Global class embodying all operator classes using LDST.  For internal use only.
   private static final OperatorClass
   LDST_insn = new OperatorClass(4+1);

   // Global class embodying all operator classes using FXUC.  For internal use only.
   private static final OperatorClass
   FXUC_insn = new OperatorClass(5+1);

   // Global class embodying all operator classes using CR.  For internal use only.
   private static final OperatorClass
   CR_insn = new OperatorClass(6+1);

   // Global class embodying all operator classes using RESERVE.  For internal use only.
   private static final OperatorClass
   RESERVE_insn = new OperatorClass(7+1);


   /**
    * Map from resource to operator class representing that resource
    */
   private static OperatorClass[] res2class = {
      ISSUE_insn,
      FXU_insn,
      FPU_insn,
      BRU_insn,
      LDST_insn,
      FXUC_insn,
      CR_insn,
      RESERVE_insn,
      null
   };

   private static final OperatorClass
   pseudo_ops = new OperatorClass(
      0+N+1,
      new ResourceReservation[] {
      }
   );
   static {
      LABEL.setOpClass(pseudo_ops);
      BBEND.setOpClass(pseudo_ops);
      PPC_DATA_INT.setOpClass(pseudo_ops);
      PPC_DATA_LABEL.setOpClass(pseudo_ops);
      GUARD_MOVE.setOpClass(pseudo_ops);
      GUARD_COMBINE.setOpClass(pseudo_ops);
      UNINT_BEGIN.setOpClass(pseudo_ops);
      UNINT_END.setOpClass(pseudo_ops);
      IR_PROLOGUE.setOpClass(pseudo_ops);


      pseudo_ops.setLatency(any_insn, 0);

   }
   private static final OperatorClass
   branch_unconditional = new OperatorClass(
      1+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(BRU, 1),
      }
   );
   static {
      PPC_B.setOpClass(branch_unconditional);
      PPC_BLR.setOpClass(branch_unconditional);
      RETURN.setOpClass(branch_unconditional);


      branch_unconditional.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   call_insn = new OperatorClass(
      2+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXUC, 1),
         new ResourceReservation(BRU, 1),
         new ResourceReservation(LDST, 1),
      }
   );
   static {
      PPC_BL.setOpClass(call_insn);
      PPC_BCL.setOpClass(call_insn);
      PPC_BLRL.setOpClass(call_insn);
      PPC_BCLRL.setOpClass(call_insn);
      PPC_BCTRL.setOpClass(call_insn);
      CALL.setOpClass(call_insn);
      YIELDPOINT_PROLOGUE.setOpClass(call_insn);
      YIELDPOINT_EPILOGUE.setOpClass(call_insn);
      YIELDPOINT_BACKEDGE.setOpClass(call_insn);
      RESOLVE.setOpClass(call_insn);
      LOWTABLESWITCH.setOpClass(call_insn);


      call_insn.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   trap_insn = new OperatorClass(
      3+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 1),
      }
   );
   static {
      PPC_TW.setOpClass(trap_insn);
      PPC_TWI.setOpClass(trap_insn);
      NULL_CHECK.setOpClass(trap_insn);
      PPC_TAddr.setOpClass(trap_insn);


      trap_insn.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   branch_conditional = new OperatorClass(
      4+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(BRU, 1),
      }
   );
   static {
      PPC_BCOND.setOpClass(branch_conditional);
      PPC_BC.setOpClass(branch_conditional);
      PPC_BCLR.setOpClass(branch_conditional);
      PPC_BCOND2.setOpClass(branch_conditional);


      branch_conditional.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   branch_on_count = new OperatorClass(
      5+N+1,
      new ResourceReservation[] {
      }
   );
   static {
      PPC_BCTR.setOpClass(branch_on_count);
      PPC_BCCTR.setOpClass(branch_on_count);
      PPC_BCC.setOpClass(branch_on_count);


      branch_on_count.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   cr_logic = new OperatorClass(
      6+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(BRU, 1),
         new ResourceReservation(CR, 3),
      }
   );
   static {
      PPC_CRAND.setOpClass(cr_logic);
      PPC_CRANDC.setOpClass(cr_logic);
      PPC_CROR.setOpClass(cr_logic);
      PPC_CRORC.setOpClass(cr_logic);


      cr_logic.setLatency(any_insn, 3);

   }
   private static final OperatorClass
   fixed_compare = new OperatorClass(
      7+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 1),
      }
   );
   static {
      PPC_CMP.setOpClass(fixed_compare);
      PPC_CMPI.setOpClass(fixed_compare);
      PPC_CMPL.setOpClass(fixed_compare);
      PPC_CMPLI.setOpClass(fixed_compare);


      fixed_compare.setLatency(branch_conditional, 2);
      fixed_compare.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   float_compare = new OperatorClass(
      8+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 1),
      }
   );
   static {
      PPC_FCMPU.setOpClass(float_compare);
      PPC_FCMPO.setOpClass(float_compare);


      float_compare.setLatency(branch_conditional, 5);
      float_compare.setLatency(any_insn, 5);

   }
   private static final OperatorClass
   fxuc_serial = new OperatorClass(
      9+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXUC, 3),
      }
   );
   static {
      PPC_MFSPR.setOpClass(fxuc_serial);


      fxuc_serial.setLatency(any_insn, 3);

   }
   private static final OperatorClass
   sets_ctrlr = new OperatorClass(
      10+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXUC, 1),
      }
   );
   static {
      PPC_MTSPR.setOpClass(sets_ctrlr);


      sets_ctrlr.setLatency(branch_unconditional, 5);
      sets_ctrlr.setLatency(branch_conditional, 5);
      sets_ctrlr.setLatency(branch_on_count, 5);
      sets_ctrlr.setLatency(call_insn, 5);
      sets_ctrlr.setLatency(any_insn, 5);

   }
   private static final OperatorClass
   fixed_unit_604e = new OperatorClass(
      11+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 1),
      }
   );
   static {
      PPC_ADD.setOpClass(fixed_unit_604e);
      PPC_ADDI.setOpClass(fixed_unit_604e);
      PPC_ADDC.setOpClass(fixed_unit_604e);
      PPC_ADDIC.setOpClass(fixed_unit_604e);
      PPC_ADDIS.setOpClass(fixed_unit_604e);
      PPC_SUBF.setOpClass(fixed_unit_604e);
      PPC_SUBFC.setOpClass(fixed_unit_604e);
      PPC_SUBFIC.setOpClass(fixed_unit_604e);
      PPC_NEG.setOpClass(fixed_unit_604e);
      PPC_MOVE.setOpClass(fixed_unit_604e);
      INT_MOVE.setOpClass(fixed_unit_604e);
      PPC_LDI.setOpClass(fixed_unit_604e);
      PPC_LDIS.setOpClass(fixed_unit_604e);
      PPC_AND.setOpClass(fixed_unit_604e);
      PPC_ANDC.setOpClass(fixed_unit_604e);
      PPC_OR.setOpClass(fixed_unit_604e);
      PPC_ORC.setOpClass(fixed_unit_604e);
      PPC_ORI.setOpClass(fixed_unit_604e);
      PPC_ORIS.setOpClass(fixed_unit_604e);
      PPC_XOR.setOpClass(fixed_unit_604e);
      PPC_XORI.setOpClass(fixed_unit_604e);
      PPC_XORIS.setOpClass(fixed_unit_604e);
      PPC_NAND.setOpClass(fixed_unit_604e);
      PPC_NOR.setOpClass(fixed_unit_604e);
      PPC_SLW.setOpClass(fixed_unit_604e);
      PPC_SLWI.setOpClass(fixed_unit_604e);
      PPC_SRAW.setOpClass(fixed_unit_604e);
      PPC_SRAWI.setOpClass(fixed_unit_604e);
      PPC_SRW.setOpClass(fixed_unit_604e);
      PPC_SRWI.setOpClass(fixed_unit_604e);
      PPC_RLWIMI.setOpClass(fixed_unit_604e);
      PPC_RLWNM.setOpClass(fixed_unit_604e);
      PPC_RLWINM.setOpClass(fixed_unit_604e);
      PPC_EQV.setOpClass(fixed_unit_604e);
      PPC_CNTLZW.setOpClass(fixed_unit_604e);
      PPC_EXTSB.setOpClass(fixed_unit_604e);
      PPC_EXTSH.setOpClass(fixed_unit_604e);
      PPC_CNTLZAddr.setOpClass(fixed_unit_604e);
      PPC_SRAddrI.setOpClass(fixed_unit_604e);
      PPC_SRAAddrI.setOpClass(fixed_unit_604e);


      fixed_unit_604e.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   fixed_record_604e = new OperatorClass(
      12+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 1),
      }
   );
   static {
      PPC_ADDr.setOpClass(fixed_record_604e);
      PPC_ADDICr.setOpClass(fixed_record_604e);
      PPC_SUBFr.setOpClass(fixed_record_604e);
      PPC_SUBFCr.setOpClass(fixed_record_604e);
      PPC_NEGr.setOpClass(fixed_record_604e);
      PPC_ANDr.setOpClass(fixed_record_604e);
      PPC_ANDCr.setOpClass(fixed_record_604e);
      PPC_ANDIr.setOpClass(fixed_record_604e);
      PPC_ANDISr.setOpClass(fixed_record_604e);
      PPC_ORr.setOpClass(fixed_record_604e);
      PPC_ORCr.setOpClass(fixed_record_604e);
      PPC_XORr.setOpClass(fixed_record_604e);
      PPC_NANDr.setOpClass(fixed_record_604e);
      PPC_NORr.setOpClass(fixed_record_604e);
      PPC_SLWr.setOpClass(fixed_record_604e);
      PPC_SLWIr.setOpClass(fixed_record_604e);
      PPC_SRAWr.setOpClass(fixed_record_604e);
      PPC_SRAWIr.setOpClass(fixed_record_604e);
      PPC_SRWr.setOpClass(fixed_record_604e);
      PPC_SRWIr.setOpClass(fixed_record_604e);
      PPC_RLWIMIr.setOpClass(fixed_record_604e);
      PPC_RLWNMr.setOpClass(fixed_record_604e);
      PPC_RLWINMr.setOpClass(fixed_record_604e);
      PPC_EQVr.setOpClass(fixed_record_604e);
      PPC_EXTSBr.setOpClass(fixed_record_604e);
      PPC_EXTSHr.setOpClass(fixed_record_604e);


      fixed_record_604e.setLatency(branch_conditional, 2);
      fixed_record_604e.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   fixed_serial_604e = new OperatorClass(
      13+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 2),
         new ResourceReservation(CR, 5),
      }
   );
   static {
      PPC_ADDE.setOpClass(fixed_serial_604e);
      PPC_ADDME.setOpClass(fixed_serial_604e);
      PPC_ADDZE.setOpClass(fixed_serial_604e);
      PPC_SUBFE.setOpClass(fixed_serial_604e);
      PPC_SUBFZE.setOpClass(fixed_serial_604e);


      fixed_serial_604e.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   fixed_mul_604e = new OperatorClass(
      14+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 1),
      }
   );
   static {
      PPC_MULLW.setOpClass(fixed_mul_604e);
      PPC_MULLI.setOpClass(fixed_mul_604e);
      PPC_MULHW.setOpClass(fixed_mul_604e);
      PPC_MULHWU.setOpClass(fixed_mul_604e);


      fixed_mul_604e.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   fixed_div_604e = new OperatorClass(
      15+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FXU, 20),
      }
   );
   static {
      PPC_DIVW.setOpClass(fixed_div_604e);
      PPC_DIVWU.setOpClass(fixed_div_604e);


      fixed_div_604e.setLatency(any_insn, 20);

   }
   private static final OperatorClass
   float_move = new OperatorClass(
      16+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 1),
      }
   );
   static {
      PPC_FABS.setOpClass(float_move);


      float_move.setLatency(any_insn, 1);

   }
   private static final OperatorClass
   float_arith = new OperatorClass(
      17+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 1),
      }
   );
   static {
      PPC_FADD.setOpClass(float_arith);
      PPC_FADDS.setOpClass(float_arith);
      PPC_FSUB.setOpClass(float_arith);
      PPC_FSUBS.setOpClass(float_arith);
      PPC_FNEG.setOpClass(float_arith);
      PPC_FRSP.setOpClass(float_arith);
      PPC_FCTIW.setOpClass(float_arith);
      PPC_FCTIWZ.setOpClass(float_arith);
      PPC_FMR.setOpClass(float_arith);
      DOUBLE_MOVE.setOpClass(float_arith);


      float_arith.setLatency(FPU_insn, 3);
      float_arith.setLatency(any_insn, 3);

   }
   private static final OperatorClass
   float_sel = new OperatorClass(
      18+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 2),
      }
   );
   static {
      PPC_FSEL.setOpClass(float_sel);


      float_sel.setLatency(FPU_insn, 3);
      float_sel.setLatency(any_insn, 3);

   }
   private static final OperatorClass
   float_mul = new OperatorClass(
      19+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 1),
      }
   );
   static {
      PPC_FMUL.setOpClass(float_mul);
      PPC_FMULS.setOpClass(float_mul);


      float_mul.setLatency(FPU_insn, 3);
      float_mul.setLatency(any_insn, 3);

   }
   private static final OperatorClass
   float_madd_single = new OperatorClass(
      20+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 1),
      }
   );
   static {
      PPC_FMADDS.setOpClass(float_madd_single);
      PPC_FMSUBS.setOpClass(float_madd_single);
      PPC_FNMADDS.setOpClass(float_madd_single);
      PPC_FNMSUBS.setOpClass(float_madd_single);


      float_madd_single.setLatency(FPU_insn, 2);
      float_madd_single.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   float_madd_double = new OperatorClass(
      21+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 1),
      }
   );
   static {
      PPC_FMADD.setOpClass(float_madd_double);
      PPC_FMSUB.setOpClass(float_madd_double);
      PPC_FNMADD.setOpClass(float_madd_double);
      PPC_FNMSUB.setOpClass(float_madd_double);


      float_madd_double.setLatency(FPU_insn, 3);
      float_madd_double.setLatency(any_insn, 3);

   }
   private static final OperatorClass
   float_div = new OperatorClass(
      22+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(FPU, 19),
      }
   );
   static {
      PPC_FDIV.setOpClass(float_div);
      PPC_FDIVS.setOpClass(float_div);


      float_div.setLatency(FPU_insn, 22);
      float_div.setLatency(any_insn, 19);

   }
   private static final OperatorClass
   fixed_load = new OperatorClass(
      23+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 1),
      }
   );
   static {
      PPC_LWZ.setOpClass(fixed_load);
      PPC_LWZX.setOpClass(fixed_load);
      PPC_LWZU.setOpClass(fixed_load);
      PPC_LWZUX.setOpClass(fixed_load);
      PPC_LHZ.setOpClass(fixed_load);
      PPC_LHZX.setOpClass(fixed_load);
      PPC_LHA.setOpClass(fixed_load);
      PPC_LHAX.setOpClass(fixed_load);
      PPC_LBZ.setOpClass(fixed_load);
      PPC_LBZX.setOpClass(fixed_load);
      PPC_LBZUX.setOpClass(fixed_load);
      PPC_LInt.setOpClass(fixed_load);
      PPC_LIntX.setOpClass(fixed_load);
      PPC_LIntUX.setOpClass(fixed_load);
      PPC_LAddr.setOpClass(fixed_load);
      PPC_LAddrX.setOpClass(fixed_load);
      PPC_LAddrU.setOpClass(fixed_load);
      PPC_LAddrUX.setOpClass(fixed_load);


      fixed_load.setLatency(FXU_insn, 2);
      fixed_load.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   float_load = new OperatorClass(
      24+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 1),
      }
   );
   static {
      PPC_LFD.setOpClass(float_load);
      PPC_LFDX.setOpClass(float_load);
      PPC_LFS.setOpClass(float_load);
      PPC_LFSX.setOpClass(float_load);


      float_load.setLatency(FPU_insn, 2);
      float_load.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   fixed_load_multiple = new OperatorClass(
      25+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 16),
      }
   );
   static {
      PPC_LMW.setOpClass(fixed_load_multiple);


      fixed_load_multiple.setLatency(FXU_insn, 17);
      fixed_load_multiple.setLatency(any_insn, 17);

   }
   private static final OperatorClass
   fixed_load_reserve = new OperatorClass(
      26+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 1),
         new ResourceReservation(RESERVE, 1),
      }
   );
   static {
      PPC_LWARX.setOpClass(fixed_load_reserve);
      PPC_LAddrARX.setOpClass(fixed_load_reserve);


      fixed_load_reserve.setLatency(FXU_insn, 2);
      fixed_load_reserve.setLatency(any_insn, 2);

   }
   private static final OperatorClass
   fixed_store = new OperatorClass(
      27+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 1),
      }
   );
   static {
      PPC_STW.setOpClass(fixed_store);
      PPC_STWX.setOpClass(fixed_store);
      PPC_STWU.setOpClass(fixed_store);
      PPC_STH.setOpClass(fixed_store);
      PPC_STHX.setOpClass(fixed_store);
      PPC_STB.setOpClass(fixed_store);
      PPC_STBX.setOpClass(fixed_store);
      PPC_STAddr.setOpClass(fixed_store);
      PPC_STAddrX.setOpClass(fixed_store);
      PPC_STAddrU.setOpClass(fixed_store);
      PPC_STAddrUX.setOpClass(fixed_store);


      fixed_store.setLatency(fixed_load, 4);
      fixed_store.setLatency(float_load, 4);
      fixed_store.setLatency(fixed_load_multiple, 4);
      fixed_store.setLatency(fixed_load_reserve, 4);
      fixed_store.setLatency(any_insn, 1);

      fixed_store.setRevLatency(FXU_insn, 2);
   }
   private static final OperatorClass
   float_store = new OperatorClass(
      28+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 1),
      }
   );
   static {
      PPC_STFD.setOpClass(float_store);
      PPC_STFDX.setOpClass(float_store);
      PPC_STFDU.setOpClass(float_store);
      PPC_STFS.setOpClass(float_store);
      PPC_STFSX.setOpClass(float_store);
      PPC_STFSU.setOpClass(float_store);


      float_store.setLatency(float_load, 4);
      float_store.setLatency(fixed_load, 4);
      float_store.setLatency(fixed_load_multiple, 4);
      float_store.setLatency(fixed_load_reserve, 4);
      float_store.setLatency(any_insn, 1);

      float_store.setRevLatency(FPU_insn, 2);
   }
   private static final OperatorClass
   fixed_store_multiple = new OperatorClass(
      29+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 19),
      }
   );
   static {
      PPC_STMW.setOpClass(fixed_store_multiple);


      fixed_store_multiple.setLatency(fixed_load, 20);
      fixed_store_multiple.setLatency(float_load, 20);
      fixed_store_multiple.setLatency(fixed_load_multiple, 20);
      fixed_store_multiple.setLatency(fixed_load_reserve, 20);
      fixed_store_multiple.setLatency(any_insn, 1);

      fixed_store_multiple.setRevLatency(FXU_insn, 2);
   }
   private static final OperatorClass
   fixed_store_conditional = new OperatorClass(
      30+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE, 1),
         new ResourceReservation(LDST, 1),
         new ResourceReservation(RESERVE, 1),
      }
   );
   static {
      PPC_STWCXr.setOpClass(fixed_store_conditional);
      PPC_STAddrCXr.setOpClass(fixed_store_conditional);


      fixed_store_conditional.setLatency(fixed_load, 4);
      fixed_store_conditional.setLatency(float_load, 4);
      fixed_store_conditional.setLatency(fixed_load_multiple, 4);
      fixed_store_conditional.setLatency(fixed_load_reserve, 4);
      fixed_store_conditional.setLatency(any_insn, 1);

      fixed_store_conditional.setRevLatency(FXU_insn, 2);
   }
   private static final OperatorClass
   icache_sync = new OperatorClass(
      31+N+1,
      new ResourceReservation[] {
         new ResourceReservation(ISSUE_ALL, 50),
         new ResourceReservation(FXU_ALL, 50),
         new ResourceReservation(FPU_ALL, 50),
         new ResourceReservation(BRU_ALL, 50),
         new ResourceReservation(LDST_ALL, 50),
         new ResourceReservation(FXUC_ALL, 50),
         new ResourceReservation(CR_ALL, 50),
      }
   );
   static {
      PPC_ISYNC.setOpClass(icache_sync);
      PPC_ICBI.setOpClass(icache_sync);


      icache_sync.setLatency(any_insn, 50);

   }
   private static final OperatorClass
   dcache_sync = new OperatorClass(
      32+N+1,
      new ResourceReservation[] {
         new ResourceReservation(all_units(ISSUE), 50),
         new ResourceReservation(all_units(FXU), 50),
         new ResourceReservation(all_units(FPU), 50),
         new ResourceReservation(all_units(BRU), 50),
         new ResourceReservation(all_units(LDST), 50),
         new ResourceReservation(all_units(FXUC), 50),
         new ResourceReservation(all_units(CR), 50),
      }
   );
   static {
      PPC_SYNC.setOpClass(dcache_sync);
      PPC_DCBF.setOpClass(dcache_sync);
      PPC_DCBST.setOpClass(dcache_sync);
      PPC_DCBT.setOpClass(dcache_sync);
      PPC_DCBTST.setOpClass(dcache_sync);
      PPC_DCBZ.setOpClass(dcache_sync);
      PPC_DCBZL.setOpClass(dcache_sync);


      dcache_sync.setLatency(any_insn, 50);

   }

   /**
    * Resource Classes used by this Operator Class
    */
   final ArrayList<OperatorClass> rclasses;

   /**
    * Resource Usage Masks
    */
   int[][] masks;

   // For internal use only.
   private OperatorClass(int _id) {
      id = _id;
      rclasses = new ArrayList<OperatorClass>();
      latencies = new ArrayList<Integer>();
   }

   // For internal use only.
   private OperatorClass(int _id, ResourceReservation[] pat) {
      this(_id);
      allocateMasks(pat);
      if (verbose >= 2) debug(masks.length+" masks allocated for "+pat.length+
                              " requests");
      int[] assign = new int[pat.length];
      int comb = fillMasks(pat, assign, 0, 0, 0);
      if (false && comb != masks.length)
         throw new InternalError("Insufficient Resources");
   }

   /**
    * Returns the string representation of this operator class.
    */
   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer("Size=");
      sb.append(masks.length).append('\n');
     for (int[] mask : masks) {
       for (int v : mask)
         sb.append(toBinaryPad32(v)).append('\n');
       sb.append('\n');
     }
      return sb.toString();
   }

   // For internal use only.
   private void allocateMasks(ResourceReservation[] pat) {
      ResourceReservation.sort(pat);
      int maxlen = 0;
      int size = 1;
      ResourceReservation r = new ResourceReservation(-1, -1, -1000);
      int len = -1;
      OperatorClass[] rss = new OperatorClass[N];
      for (ResourceReservation p : pat) {
         rss[p.rclass()] = res2class[p.rclass()];
         boolean same = p.equals(r);
         if (!p.conflicts(r)) {
            r = p;
            if (r.isGlobal())
               len = 1;
            else
               len = resources[r.rclass()].length - 1;
         } else if (r.isGlobal()) {
            throw new InternalError("Insufficient Resources");
         } else {
            len--;
         }
         size *= len;
         if (same)
            size /= 2;
         if (p.start + p.duration > maxlen)
            maxlen = p.start + p.duration;
      }
      rclasses.add(any_insn);
      for (int i = 0; i < N; i++)
         if (rss[i] != null)
            rclasses.add(rss[i]);
      masks = new int[size][];
      for (int i = 0; i < size; i++)
         masks[i] = new int[maxlen];
   }

   // For internal debug use only.
   static int depth = 0;

   // For internal use only.
   private int fillMasks(ResourceReservation[] pat, int[] assign,
                               int all, int rrq, int comb) {
      if (rrq == pat.length) {
         for (int i = 0; i < masks[comb].length; i++)
            masks[comb][i] = 0;
         StringBuffer dbSB;
         if (verbose >= 1) dbSB = new StringBuffer();
         for (int i = 0; i < pat.length; i++) {
            ResourceReservation pi = pat[i];
            int rc = pi.rclass();
            int mask = resources[rc][assign[i]];
            if (verbose >= 1) dbSB.append(toBinaryPad32(mask)).append(" ");
            for (int j = 0; j < pi.duration; j++)
               masks[comb][pi.start + j] |= mask;
            if (maxlat < pi.duration)
               maxlat = pi.duration;
         }
         if (verbose >= 1) debug(dbSB.toString());
         return comb + 1;
      }
      int rc = pat[rrq].rclass();
      int start = 0;
      int end = resources[rc].length - 1;
      if (rrq != 0 && pat[rrq].equals(pat[rrq-1]))
         start = assign[rrq-1] + 1;
      boolean ignore = ((rrq != 0 && !pat[rrq].conflicts(pat[rrq-1])) ||
                        pat[rrq].isGlobal());
      if (pat[rrq].isGlobal()) {
         start = end;
         end++;
      }

      for (int i = start; i < end; i++)
         if (ignore || (resources[rc][i] & all) == 0) {
            if (verbose >= 2) debug(depth, rrq+": Res#"+rc+"; Trying "+i+
                                    "; reserved='"+toBinaryPad32(all)+"'");

            depth++;
            assign[rrq] = i;
            comb = fillMasks(pat, assign, all | resources[rc][i], rrq+1, comb);
            depth--;
         }

      return comb;
   }

   // Generates a string of a given length filled by a given character.
   // For internal use only.
   private static String dup(int len, char c) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < len; i++)
         sb.append(c);
      return sb.toString();
   }
}
