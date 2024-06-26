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
package org.jikesrvm.ia32;

import static org.jikesrvm.compilers.common.assembler.ia32.AssemblerConstants.EQ;
import static org.jikesrvm.compilers.common.assembler.ia32.AssemblerConstants.NE;
import static org.jikesrvm.ia32.ArchConstants.SSE2_FULL;
import static org.jikesrvm.ia32.BaselineConstants.LG_WORDSIZE;
import static org.jikesrvm.ia32.BaselineConstants.S0;
import static org.jikesrvm.ia32.BaselineConstants.SP;
import static org.jikesrvm.ia32.BaselineConstants.T0;
import static org.jikesrvm.ia32.BaselineConstants.T1;
import static org.jikesrvm.ia32.BaselineConstants.TR;
import static org.jikesrvm.ia32.BaselineConstants.WORDSIZE;
import static org.jikesrvm.ia32.RegisterConstants.ALL_GPRS;
import static org.jikesrvm.ia32.RegisterConstants.EAX;
import static org.jikesrvm.ia32.RegisterConstants.EBP;
import static org.jikesrvm.ia32.RegisterConstants.EBX;
import static org.jikesrvm.ia32.RegisterConstants.ECX;
import static org.jikesrvm.ia32.RegisterConstants.EDI;
import static org.jikesrvm.ia32.RegisterConstants.EDX;
import static org.jikesrvm.ia32.RegisterConstants.ESI;
import static org.jikesrvm.ia32.RegisterConstants.FP0;
import static org.jikesrvm.ia32.RegisterConstants.JTOC_REGISTER;
import static org.jikesrvm.ia32.RegisterConstants.NONVOLATILE_GPRS;
import static org.jikesrvm.ia32.RegisterConstants.NUM_GPRS;
import static org.jikesrvm.ia32.RegisterConstants.NUM_NONVOLATILE_FPRS;
import static org.jikesrvm.ia32.RegisterConstants.NUM_NONVOLATILE_GPRS;
import static org.jikesrvm.ia32.RegisterConstants.NUM_PARAMETER_FPRS;
import static org.jikesrvm.ia32.RegisterConstants.NUM_PARAMETER_GPRS;
import static org.jikesrvm.ia32.RegisterConstants.THREAD_REGISTER;
import static org.jikesrvm.ia32.RegisterConstants.XMM0;
import static org.jikesrvm.ia32.RegisterConstants.XMM1;
import static org.jikesrvm.ia32.RegisterConstants.XMM2;
import static org.jikesrvm.ia32.RegisterConstants.XMM3;
import static org.jikesrvm.ia32.StackframeLayoutConstants.INVISIBLE_METHOD_ID;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_BODY_OFFSET;
import static org.jikesrvm.objectmodel.JavaHeaderConstants.ARRAY_LENGTH_BYTES;
import static org.jikesrvm.runtime.JavaSizeConstants.BYTES_IN_DOUBLE;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.RVMField;
import org.jikesrvm.compilers.common.CodeArray;
import org.jikesrvm.compilers.common.assembler.ForwardReference;
import org.jikesrvm.compilers.common.assembler.ia32.Assembler;
import org.jikesrvm.ia32.RegisterConstants.GPR;
import org.jikesrvm.objectmodel.ObjectModel;
import org.jikesrvm.runtime.ArchEntrypoints;
import org.jikesrvm.runtime.EntrypointHelper;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.unboxed.Offset;

/**
 * A place to put hand written machine code typically invoked by Magic
 * methods.
 *
 * <p>Hand coding of small inline instruction sequences is typically handled by
 * each compiler's implementation of Magic methods.
 * A few Magic methods are so complex that their implementations require
 * many instructions.  But our compilers do not inline
 * arbitrary amounts of machine code. We therefore write such code blocks
 * here, out of line.
 *
 * <p>These code blocks can be shared by all compilers. They can be branched to
 * via a jtoc offset (obtained from Entrypoints.XXXInstructionsField).
 */
public abstract class OutOfLineMachineCode {
  //-----------//
  // interface //
  //-----------//

  public static void init() {
    generatePcThunkInstructions();
    reflectiveMethodInvokerInstructions = generateReflectiveMethodInvokerInstructions();
    saveThreadStateInstructions = generateSaveThreadStateInstructions();
    threadSwitchInstructions = generateThreadSwitchInstructions();
    RVMThread.stackTrampolineBridgeInstructions = generateStackTrampolineBridgeInstructions();
    restoreHardwareExceptionStateInstructions = generateRestoreHardwareExceptionStateInstructions();
  }

  //----------------//
  // implementation //
  //----------------//

  public static final RVMField[] pcThunkInstructionsField = new RVMField[8];

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkEAXInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkEBXInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkECXInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkEDXInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkEBPInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkESIInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  // Accessed via field array above
  @Entrypoint
  private static CodeArray pcThunkEDIInstructions;

  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  @Entrypoint
  private static CodeArray reflectiveMethodInvokerInstructions;
  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  @Entrypoint
  private static CodeArray saveThreadStateInstructions;
  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  @Entrypoint
  private static CodeArray threadSwitchInstructions;
  @SuppressWarnings({"unused", "UnusedDeclaration", "FieldCanBeLocal"})
  @Entrypoint
  private static CodeArray restoreHardwareExceptionStateInstructions;

  private static final Offset PARAMS_FP_OFFSET = Offset.fromIntSignExtend(WORDSIZE * 2);
  private static final Offset FPRMETA_FP_OFFSET = Offset.fromIntSignExtend(WORDSIZE * 3);
  private static final Offset FPRS_FP_OFFSET = Offset.fromIntSignExtend(WORDSIZE * 4);
  private static final Offset GPRS_FP_OFFSET = Offset.fromIntSignExtend(WORDSIZE * 5);
  private static final Offset CODE_FP_OFFSET = Offset.fromIntSignExtend(WORDSIZE * 6);

  /**
   * Machine code to get the address of the instruction after the call to this
   * method
   */
  private static void generatePcThunkInstructions() {
    Assembler asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(EAX, SP);
    asm.emitRET();
    pcThunkEAXInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[EAX.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkEAXInstructions", CodeArray.class);

    asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(EBX, SP);
    asm.emitRET();
    pcThunkEBXInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[EBX.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkEBXInstructions", CodeArray.class);

    asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(ECX, SP);
    asm.emitRET();
    pcThunkECXInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[ECX.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkECXInstructions", CodeArray.class);

    asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(EDX, SP);
    asm.emitRET();
    pcThunkEDXInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[EDX.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkEDXInstructions", CodeArray.class);

    // NB a PC thunk into ESP isn't allowed

    asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(EBP, SP);
    asm.emitRET();
    pcThunkEBPInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[EBP.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkEBPInstructions", CodeArray.class);

    asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(ESI, SP);
    asm.emitRET();
    pcThunkESIInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[ESI.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkESIInstructions", CodeArray.class);

    asm = new Assembler(0);
    asm.emitMOV_Reg_RegInd(EDI, SP);
    asm.emitRET();
    pcThunkEDIInstructions = asm.getMachineCodes();
    pcThunkInstructionsField[EDI.value()] =
       EntrypointHelper.getField(OutOfLineMachineCode.class,
         "pcThunkEDIInstructions", CodeArray.class);
  }

  /**
   * Machine code for reflective method invocation.
   * <pre>
   * VM compiled with NUM_PARAMETERS_GPRS == 0
   *   Registers taken at runtime:
   *     none
   *   Stack taken at runtime:
   *     hi-mem
   *         address of method entrypoint to be called
   *         address of gpr registers to be loaded
   *         address of fpr registers to be loaded
   *         address of parameters area in calling frame
   *         return address
   *     low-mem
   *
   * VM compiled with NUM_PARAMETERS_GPRS == 1
   *   T0 == address of method entrypoint to be called
   *   Stack taken at runtime:
   *     hi-mem
   *         space ???
   *         address of gpr registers to be loaded
   *         address of fpr registers to be loaded
   *         address of parameters area in calling frame
   *         return address
   *     low-mem
   *
   * VM compiled with NUM_PARAMETERS_GPRS == 2
   *   T0 == address of method entrypoint to be called
   *   T1 == address of gpr registers to be loaded
   *   Stack taken at runtime:
   *     hi-mem
   *         space ???
   *         space ???
   *         address of fpr registers to be loaded
   *         address of parameters area in calling frame
   *         return address
   *     low-mem
   *
   * Registers returned at runtime:
   *   standard return value conventions used
   *
   * Side effects at runtime:
   *   artificial stackframe created and destroyed
   *   volatile, and scratch registers destroyed
   * </pre>
   *
   * @return instructions for the reflective method invoker
   */
  private static CodeArray generateReflectiveMethodInvokerInstructions() {
    Assembler asm = new Assembler(100);
    int gprs;
    Offset fpOffset = ArchEntrypoints.framePointerField.getOffset();
    GPR T = T0;
    gprs = NUM_PARAMETER_GPRS;
    // we have exactly 5 paramaters, offset 0 from SP is the return address the
    // parameters are at offsets 5 to 1
    Offset offset = Offset.fromIntZeroExtend(5 << LG_WORDSIZE);
    // Write at most 2 parameters from registers in the stack. This is
    // logically equivalent to ParamaterRegisterUnload in the compiler
    if (gprs > 0) {
      gprs--;
      if (VM.BuildFor32Addr) {
        asm.emitMOV_RegDisp_Reg(SP, offset, T);
      } else {
        asm.emitMOV_RegDisp_Reg_Quad(SP, offset, T);
      }
      T = T1;
      offset = offset.minus(WORDSIZE);
    }
    if (gprs > 0) {
      if (VM.BuildFor32Addr) {
        asm.emitMOV_RegDisp_Reg(SP, offset, T);
      } else {
        asm.emitMOV_RegDisp_Reg_Quad(SP, offset, T);
      }
    }

    /* available registers S0, T0, T1 */

    /* push a new frame */
    asm.emitPUSH_RegDisp(TR, fpOffset); // link this frame with next
    if (VM.BuildFor32Addr) {
      asm.emitMOV_RegDisp_Reg(THREAD_REGISTER, fpOffset, SP);  // establish base of new frame
      asm.emitPUSH_Imm(INVISIBLE_METHOD_ID);
      asm.emitADD_Reg_Imm(SP, STACKFRAME_BODY_OFFSET.toInt());
    } else {
      asm.emitMOV_RegDisp_Reg_Quad(THREAD_REGISTER, fpOffset, SP);  // establish base of new frame
      asm.emitPUSH_Imm(INVISIBLE_METHOD_ID);
      asm.emitADD_Reg_Imm_Quad(SP, STACKFRAME_BODY_OFFSET.toInt());
    }

    /* write parameters on stack
     * move data from memory addressed by Paramaters array, the fourth
     * parameter to this, into the stack.
     * SP target address
     * S0 source address
     * T1 length
     * T0 scratch
     */
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, THREAD_REGISTER, fpOffset);
      asm.emitMOV_Reg_RegDisp(S0, S0, PARAMS_FP_OFFSET); // S0 <- Parameters
      asm.emitMOV_Reg_RegDisp(T1, S0, ObjectModel.getArrayLengthOffset());       // T1 <- Parameters.length()
      asm.emitCMP_Reg_Imm(T1, 0);                        // length == 0 ?
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, THREAD_REGISTER, fpOffset);
      asm.emitMOV_Reg_RegDisp_Quad(S0, S0, PARAMS_FP_OFFSET);// S0 <- Parameters
      if (ARRAY_LENGTH_BYTES == 4) {
        asm.emitMOV_Reg_RegDisp(T1, S0, ObjectModel.getArrayLengthOffset());     // T1 <- Parameters.length()
        asm.emitCMP_Reg_Imm(T1, 0);                      // length == 0 ?
      } else {
        asm.emitMOV_Reg_RegDisp_Quad(T1, S0, ObjectModel.getArrayLengthOffset()); // T1 <- Parameters.length()
        asm.emitCMP_Reg_Imm_Quad(T1, 0);                 // length == 0 ?
      }
    }

    int parameterLoopLabel = asm.getMachineCodeIndex();
    ForwardReference fr1 = asm.forwardJcc(EQ); // done? --> branch to end
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegInd(T0, S0);                  // T0 <- Paramaters[i]
    } else {
      asm.emitMOV_Reg_RegInd_Quad(T0, S0);             // T0 <- Paramaters[i]
    }
    asm.emitPUSH_Reg(T0);                              // mem[j++] <- Parameters[i]
    if (VM.BuildFor32Addr) {
      asm.emitADD_Reg_Imm(S0, WORDSIZE);               // i++
    } else {
      asm.emitADD_Reg_Imm_Quad(S0, WORDSIZE);          // i++
    }
    if (ARRAY_LENGTH_BYTES == 4) {
      asm.emitADD_Reg_Imm(T1, -1);                     // length--
    } else {
      asm.emitADD_Reg_Imm_Quad(T1, -1);                // length--
    }
    asm.emitJMP_Imm(parameterLoopLabel);

    fr1.resolve(asm);                                   // end of the loop

    if (SSE2_FULL) {
      /* write fprs onto fprs registers */
      if (VM.BuildFor32Addr) {
        asm.emitMOV_Reg_RegDisp(S0, THREAD_REGISTER, fpOffset);
        asm.emitMOV_Reg_RegDisp(T0, S0, FPRS_FP_OFFSET);    // T0 <- FPRs
        asm.emitMOV_Reg_RegDisp(T1, T0, ObjectModel.getArrayLengthOffset()); // T1 <- FPRs.length()
        asm.emitMOV_Reg_RegDisp(S0, S0, FPRMETA_FP_OFFSET); // S0 <- FPRmeta
      } else {
        asm.emitMOV_Reg_RegDisp_Quad(S0, THREAD_REGISTER, fpOffset);
        asm.emitMOV_Reg_RegDisp_Quad(T0, S0, FPRS_FP_OFFSET);    // T0 <- FPRs
        if (ARRAY_LENGTH_BYTES == 4) {
          asm.emitMOV_Reg_RegDisp(T1, T0, ObjectModel.getArrayLengthOffset());      // T1 <- FPRs.length()
        } else {
          asm.emitMOV_Reg_RegDisp_Quad(T1, T0, ObjectModel.getArrayLengthOffset()); // T1 <- FPRs.length()
        }
        asm.emitMOV_Reg_RegDisp_Quad(S0, S0, FPRMETA_FP_OFFSET); // S0 <- FPRmeta
      }

      if (VM.VerifyAssertions) VM._assert(NUM_PARAMETER_FPRS <= 4);

      ForwardReference fr_next;

      asm.emitCMP_Reg_Imm(T1, 0);                         // length == 0 ?
      ForwardReference fpr_r1 = asm.forwardJcc(EQ);
      asm.emitMOVSD_Reg_RegInd(XMM0, T0);
      asm.emitCMP_RegInd_Imm_Byte(S0, 0);
      fr_next = asm.forwardJcc(NE);
      asm.emitCVTSD2SS_Reg_Reg(XMM0, XMM0);
      fr_next.resolve(asm);

      asm.emitSUB_Reg_Imm(T1, 1);                         // length == 0 ?
      ForwardReference fpr_r2 = asm.forwardJcc(EQ);
      asm.emitMOVSD_Reg_RegDisp(XMM1, T0, Offset.fromIntZeroExtend(BYTES_IN_DOUBLE));
      asm.emitCMP_RegDisp_Imm_Byte(S0, Offset.fromIntZeroExtend(1), 0);
      fr_next = asm.forwardJcc(NE);
      asm.emitCVTSD2SS_Reg_Reg(XMM1, XMM1);
      fr_next.resolve(asm);

      asm.emitSUB_Reg_Imm(T1, 1);                         // length == 0 ?
      ForwardReference fpr_r3 = asm.forwardJcc(EQ);
      asm.emitMOVSD_Reg_RegDisp(XMM2, T0, Offset.fromIntZeroExtend(BYTES_IN_DOUBLE * 2));
      asm.emitCMP_RegDisp_Imm_Byte(S0, Offset.fromIntZeroExtend(2), 0);
      fr_next = asm.forwardJcc(NE);
      asm.emitCVTSD2SS_Reg_Reg(XMM2, XMM2);
      fr_next.resolve(asm);

      asm.emitSUB_Reg_Imm(T1, 1);                         // length == 0 ?
      ForwardReference fpr_r4 = asm.forwardJcc(EQ);
      asm.emitMOVSD_Reg_RegDisp(XMM3, T0, Offset.fromIntZeroExtend(BYTES_IN_DOUBLE * 3));
      asm.emitCMP_RegDisp_Imm_Byte(S0, Offset.fromIntZeroExtend(3), 0);
      fr_next = asm.forwardJcc(NE);
      asm.emitCVTSD2SS_Reg_Reg(XMM3, XMM3);
      fr_next.resolve(asm);

      fpr_r1.resolve(asm);
      fpr_r2.resolve(asm);
      fpr_r3.resolve(asm);
      fpr_r4.resolve(asm);

    } else {
      if (VM.VerifyAssertions) VM._assert(VM.BuildFor32Addr);
      /* write fprs onto fprs registers */
      if (VM.BuildFor32Addr) {
        asm.emitMOV_Reg_RegDisp(S0, THREAD_REGISTER, fpOffset);
      } else {
        asm.emitMOV_Reg_RegDisp_Quad(S0, THREAD_REGISTER, fpOffset);
      }
      asm.emitMOV_Reg_RegDisp(S0, S0, FPRS_FP_OFFSET);   // S0 <- FPRs
      asm.emitMOV_Reg_RegDisp(T1, S0, ObjectModel.getArrayLengthOffset());    // T1 <- FPRs.length()
      asm.emitSHL_Reg_Imm(T1, LG_WORDSIZE + 1);         // length in bytes
      asm.emitADD_Reg_Reg(S0, T1);                       // S0 <- last FPR + 8
      asm.emitCMP_Reg_Imm(T1, 0);                        // length == 0 ?

      int fprsLoopLabel = asm.getMachineCodeIndex();
      ForwardReference fr2 = asm.forwardJcc(EQ);   // done? --> branch to end
      asm.emitSUB_Reg_Imm(S0, 2 * WORDSIZE);            // i--
      asm.emitFLD_Reg_RegInd_Quad(FP0, S0);              // frp[fpr_sp++] <-FPRs[i]
      asm.emitSUB_Reg_Imm(T1, 2 * WORDSIZE);              // length--
      asm.emitJMP_Imm(fprsLoopLabel);

      fr2.resolve(asm);                                   // end of the loop
    }

    /* write gprs: S0 = Base address of GPRs[], T1 = GPRs.length */
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, THREAD_REGISTER, fpOffset);
      asm.emitMOV_Reg_RegDisp(S0, S0, GPRS_FP_OFFSET);   // S0 <- GPRs
      asm.emitMOV_Reg_RegDisp(T1, S0, ObjectModel.getArrayLengthOffset());    // T1 <- GPRs.length()
      asm.emitCMP_Reg_Imm(T1, 0);                        // length == 0 ?
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, THREAD_REGISTER, fpOffset);
      asm.emitMOV_Reg_RegDisp_Quad(S0, S0, GPRS_FP_OFFSET);   // S0 <- GPRs
      if (ARRAY_LENGTH_BYTES == 4) {
        asm.emitMOV_Reg_RegDisp(T1, S0, ObjectModel.getArrayLengthOffset());    // T1 <- GPRs.length()
        asm.emitCMP_Reg_Imm(T1, 0);                        // length == 0 ?
      } else {
        asm.emitMOV_Reg_RegDisp_Quad(T1, S0, ObjectModel.getArrayLengthOffset());    // T1 <- GPRs.length()
        asm.emitCMP_Reg_Imm_Quad(T1, 0);                        // length == 0 ?
      }
    }
    ForwardReference fr3 = asm.forwardJcc(EQ);   // result 0 --> branch to end
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegInd(T0, S0);                    // T0 <- GPRs[0]
      asm.emitADD_Reg_Imm(S0, WORDSIZE);                 // S0 += WORDSIZE
      asm.emitADD_Reg_Imm(T1, -1);                       // T1--
    } else {
      asm.emitMOV_Reg_RegInd_Quad(T0, S0);                    // T0 <- GPRs[0]
      asm.emitADD_Reg_Imm_Quad(S0, WORDSIZE);                 // S0 += WORDSIZE
      asm.emitADD_Reg_Imm_Quad(T1, -1);                       // T1--
    }

    ForwardReference fr4 = asm.forwardJcc(EQ);   // result 0 --> branch to end
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegInd(T1, S0);                    // T1 <- GPRs[1]
    } else {
      asm.emitMOV_Reg_RegInd_Quad(T1, S0);                    // T1 <- GPRs[1]
    }
    fr3.resolve(asm);
    fr4.resolve(asm);

    /* branch to method.  On a good day we might even be back */
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, THREAD_REGISTER, fpOffset);
      asm.emitMOV_Reg_RegDisp(S0, S0, CODE_FP_OFFSET);   // S0 <- code
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, THREAD_REGISTER, fpOffset);
      asm.emitMOV_Reg_RegDisp_Quad(S0, S0, CODE_FP_OFFSET);   // S0 <- code
    }
    asm.emitCALL_Reg(S0);                              // go there
    // T0/T1 have returned value

    /* and get out */
    // NOTE: RVM callee has popped the params, so we can simply
    //       add back in the initial SP to FP delta to get SP to be a framepointer again!
    if (VM.BuildFor32Addr) {
      asm.emitADD_Reg_Imm(SP, -STACKFRAME_BODY_OFFSET.toInt() + WORDSIZE);
    } else {
      asm.emitADD_Reg_Imm_Quad(SP, -STACKFRAME_BODY_OFFSET.toInt() + WORDSIZE);
    }
    asm.emitPOP_RegDisp(TR, fpOffset);

    asm.emitRET_Imm(5 << LG_WORDSIZE);                  // again, exactly 5 parameters

    return asm.getMachineCodes();
  }

  /**
   * Machine code to implement "Magic.saveThreadState()".
   * <pre>
   *  Registers taken at runtime:
   *    T0 == address of Registers object
   *
   *  Registers returned at runtime:
   *    none
   *
   *  Side effects at runtime:
   *    S0, T1 destroyed
   *    Thread state stored into Registers object
   * </pre>
   *
   * @return instructions for saving the thread state
   */
  private static CodeArray generateSaveThreadStateInstructions() {
    if (VM.VerifyAssertions) {
      VM._assert(NUM_NONVOLATILE_FPRS == 0); // assuming no NV FPRs (otherwise would have to save them here)
    }
    Assembler asm = new Assembler(0);
    Offset ipOffset = ArchEntrypoints.registersIPField.getOffset();
    Offset fpOffset = ArchEntrypoints.registersFPField.getOffset();
    Offset gprsOffset = ArchEntrypoints.registersGPRsField.getOffset();
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, TR, ArchEntrypoints.framePointerField.getOffset());
      asm.emitMOV_RegDisp_Reg(T0, fpOffset, S0);      // registers.fp := pr.framePointer
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, TR, ArchEntrypoints.framePointerField.getOffset());
      asm.emitMOV_RegDisp_Reg_Quad(T0, fpOffset, S0); // registers.fp := pr.framePointer
    }
    asm.emitPOP_Reg(T1);                              // T1 := return address (target of final jmp)
    if (VM.BuildFor32Addr) {
      asm.emitMOV_RegDisp_Reg(T0, ipOffset, T1);        // registers.ip := return address
    } else {
      asm.emitMOV_RegDisp_Reg_Quad(T0, ipOffset, T1); // registers.ip := return address
    }
    asm.emitPOP_Reg(S0);                              // throw away space for registers parameter (in T0)
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, T0, gprsOffset);    // S0 := registers.gprs[]
      asm.emitMOV_RegDisp_Reg(S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE), SP); // registers.gprs[#SP] := SP
      for (int i = 0; i < NUM_NONVOLATILE_GPRS; i++) {
        asm.emitMOV_RegDisp_Reg(S0,
                                Offset.fromIntZeroExtend(NONVOLATILE_GPRS[i].value() << LG_WORDSIZE),
                                NONVOLATILE_GPRS[i]); // registers.gprs[i] := i'th register
      }
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, T0, gprsOffset); // S0 := registers.gprs[]
      asm.emitMOV_RegDisp_Reg_Quad(S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE), SP); // registers.gprs[#SP] := SP
      for (int i = 0; i < NUM_NONVOLATILE_GPRS; i++) {
        asm.emitMOV_RegDisp_Reg_Quad(S0,
                                     Offset.fromIntZeroExtend(NONVOLATILE_GPRS[i].value() << LG_WORDSIZE),
                                     NONVOLATILE_GPRS[i]); // registers.gprs[i] := i'th register
      }
    }
    asm.emitJMP_Reg(T1);                      // return to return address
    return asm.getMachineCodes();
  }

  /**
   * Machine code to perform a stack trampoline bridge for
   * implementing a return barrier.
   * <p>
   * This code is used to hijack a return and bridge to some
   * other method (which implements the return barrier) before
   * falling back to the intended return address.
   * <p>
   * The key here is to preserve register and stack state so that
   * the caller is oblivious of the detour that occurred during
   * the return.
   *
   * @return instructions for the stack trampoline bridge
   */
  private static CodeArray generateStackTrampolineBridgeInstructions() {
    if (VM.VerifyAssertions) {
      VM._assert(NUM_NONVOLATILE_FPRS == 0); // assuming no NV FPRs (otherwise would have to save them here)
    }

    Assembler asm = new Assembler(0);

    /* push the hijacked return address (which is held in thread-local state) */
    asm.emitPUSH_RegDisp(TR, ArchEntrypoints.hijackedReturnAddressField.getOffset());

    /* push the GPRs and fp */
    for (int i = 0; i < NUM_GPRS; i++) {
      asm.emitPUSH_Reg(ALL_GPRS[i]);
    }
    asm.emitPUSH_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset());

    /* call the handler */
    asm.generateJTOCcall(Entrypoints.returnBarrierMethod.getOffset());

    /* pop the fp and GPRs */
    asm.emitPOP_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset());
    for (int i = NUM_GPRS - 1; i >= 0; i--) {
      asm.emitPOP_Reg(ALL_GPRS[i]);
    }

    /* pop the hijacked return address and return */
    asm.emitRET();

    return asm.getMachineCodes();
  }


  /**
   * Machine code to implement "Magic.threadSwitch()".
   * <pre>
   * NOTE: Currently not functional for PNT: left as a guide for possible reimplementation.
   *
   *  Parameters taken at runtime:
   *    T0 == address of Thread object for the current thread
   *    T1 == address of Registers object for the new thread
   *
   *  Registers returned at runtime:
   *    none
   *
   *  Side effects at runtime:
   *    sets current Thread's beingDispatched field to false
   *    saves current Thread's nonvolatile hardware state in its Registers object
   *    restores new thread's Registers nonvolatile hardware state.
   *    execution resumes at address specificed by restored thread's Registers ip field
   * </pre>
   *
   * @return instructions for doing a thread switch
   */
  private static CodeArray generateThreadSwitchInstructions() {
    if (VM.VerifyAssertions) {
      VM._assert(NUM_NONVOLATILE_FPRS == 0); // assuming no NV FPRs (otherwise would have to save them here)
    }
    Assembler asm = new Assembler(0);
    Offset ipOffset = ArchEntrypoints.registersIPField.getOffset();
    Offset fpOffset = ArchEntrypoints.registersFPField.getOffset();
    Offset gprsOffset = ArchEntrypoints.registersGPRsField.getOffset();
    Offset regsOffset = Entrypoints.threadContextRegistersField.getOffset();

    // Disable interrupts
//    asm.emitCLI();
    
    // (1) Save hardware state of thread we are switching off of.
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, T0, regsOffset);      // S0 = T0.contextRegisters
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, T0, regsOffset); // S0 = T0.contextRegisters
    }
    asm.emitPOP_RegDisp(S0, ipOffset);                  // T0.contextRegisters.ip = returnAddress
    asm.emitPUSH_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset()); // push TR.framePointer
    asm.emitPOP_RegDisp(S0, fpOffset);                  // T0.contextRegisters.fp = pushed framepointer
    if (VM.BuildFor32Addr) {
      asm.emitADD_Reg_Imm(SP, 2 * WORDSIZE);                // discard 2 words of parameters (T0, T1)
      asm.emitMOV_Reg_RegDisp(S0, S0, gprsOffset);       // S0 = T0.contextRegisters.gprs;
      asm.emitMOV_RegDisp_Reg(S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE), SP); // T0.contextRegisters.gprs[#SP] := SP
      for (int i = 0; i < NUM_NONVOLATILE_GPRS; i++) {
        // T0.contextRegisters.gprs[i] := i'th register
        asm.emitMOV_RegDisp_Reg(S0,
                                Offset.fromIntZeroExtend(NONVOLATILE_GPRS[i].value() << LG_WORDSIZE),
                                NONVOLATILE_GPRS[i]);
      }
      // Save the Thread register, ESI
      asm.emitMOV_RegDisp_Reg(S0, Offset.fromIntZeroExtend(ESI.value() << LG_WORDSIZE), ESI);
    } else {
      asm.emitADD_Reg_Imm_Quad(SP, 2 * WORDSIZE);                // discard 2 words of parameters (T0, T1)
      asm.emitMOV_Reg_RegDisp_Quad(S0, S0, gprsOffset);  // S0 = T0.contextRegisters.gprs;
      asm.emitMOV_RegDisp_Reg_Quad(S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE), SP); // T0.contextRegisters.gprs[#SP] := SP
      for (int i = 0; i < NUM_NONVOLATILE_GPRS; i++) {
        // T0.contextRegisters.gprs[i] := i'th register
        asm.emitMOV_RegDisp_Reg_Quad(S0,
                                     Offset.fromIntZeroExtend(NONVOLATILE_GPRS[i].value() << LG_WORDSIZE),
                                     NONVOLATILE_GPRS[i]);
      }
      // Save the Thread register, ESI
      asm.emitMOV_RegDisp_Reg_Quad(S0, Offset.fromIntZeroExtend(ESI.value() << LG_WORDSIZE), ESI);
    }

    // (2) Set currentThread.beingDispatched to false
    // PNT: don't have this field anymore
    //asm.emitMOV_RegDisp_Imm_Byte(T0,
    //                             Entrypoints.beingDispatchedField.getOffset(),
    //                             0); // previous thread's stack is nolonger in use, so it can now be dispatched on any virtual processor

    // (3) Restore hardware state of thread we are switching to.
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, T1, fpOffset);        // S0 := restoreRegs.fp
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, T1, fpOffset);   // S0 := restoreRegs.fp
    }
    // TR.framePointer = restoreRegs.fp
    if (VM.BuildFor32Addr) {
      asm.emitMOV_RegDisp_Reg(THREAD_REGISTER, ArchEntrypoints.framePointerField.getOffset(), S0);
      asm.emitMOV_Reg_RegDisp(S0, T1, gprsOffset);      // S0 := restoreRegs.gprs[]
      asm.emitMOV_Reg_RegDisp(SP, S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE)); // SP := restoreRegs.gprs[#SP]
      for (int i = 0; i < NUM_NONVOLATILE_GPRS; i++) {
        // i'th register := restoreRegs.gprs[i]
        asm.emitMOV_Reg_RegDisp(NONVOLATILE_GPRS[i],
                                S0,
                                Offset.fromIntZeroExtend(NONVOLATILE_GPRS[i].value() <<
                                                         LG_WORDSIZE));
      }
      // Restore the Thread ID, ESI
      asm.emitMOV_Reg_RegDisp(ESI, S0, Offset.fromIntZeroExtend(ESI.value() << LG_WORDSIZE));
    } else {
      asm.emitMOV_RegDisp_Reg_Quad(THREAD_REGISTER, ArchEntrypoints.framePointerField.getOffset(), S0);
      asm.emitMOV_Reg_RegDisp_Quad(S0, T1, gprsOffset); // S0 := restoreRegs.gprs[]
      asm.emitMOV_Reg_RegDisp_Quad(SP, S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE)); // SP := restoreRegs.gprs[#SP]
      for (int i = 0; i < NUM_NONVOLATILE_GPRS; i++) {
        // i'th register := restoreRegs.gprs[i]
        asm.emitMOV_Reg_RegDisp_Quad(NONVOLATILE_GPRS[i],
                                     S0,
                                     Offset.fromIntZeroExtend(NONVOLATILE_GPRS[i].value() << LG_WORDSIZE));
      }
      asm.emitMOV_Reg_RegDisp_Quad(ESI, S0, Offset.fromIntZeroExtend(ESI.value() << LG_WORDSIZE));
   }
    
    // Turn on interrupts
//    asm.emitSTI();
    
    asm.emitJMP_RegDisp(T1, ipOffset);            // return to (save) return address
    return asm.getMachineCodes();
  }

  /**
   * Machine code to implement "Magic.restoreHardwareExceptionState()".
   * <pre>
   *  Registers taken at runtime:
   *    T0 == address of Registers object
   *
   *  Registers returned at runtime:
   *    none
   *
   *  Side effects at runtime:
   *    all registers are restored except THREAD_REGISTER and EFLAGS;
   *    execution resumes at "registers.ip"
   * </pre>
   *
   * @return instructions to restore the hardware exception state
   */
  private static CodeArray generateRestoreHardwareExceptionStateInstructions() {
    Assembler asm = new Assembler(0);

    // Set TR.framePointer to be registers.fp
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, T0, ArchEntrypoints.registersFPField.getOffset());
      asm.emitMOV_RegDisp_Reg(THREAD_REGISTER, ArchEntrypoints.framePointerField.getOffset(), S0);
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, T0, ArchEntrypoints.registersFPField.getOffset());
      asm.emitMOV_RegDisp_Reg_Quad(THREAD_REGISTER, ArchEntrypoints.framePointerField.getOffset(), S0);
    }

    // Restore SP
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, T0, ArchEntrypoints.registersGPRsField.getOffset());
      asm.emitMOV_Reg_RegDisp(SP, S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE));
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, T0, ArchEntrypoints.registersGPRsField.getOffset());
      asm.emitMOV_Reg_RegDisp_Quad(SP, S0, Offset.fromIntZeroExtend(SP.value() << LG_WORDSIZE));
    }

    // Push registers.ip to stack (now that SP has been restored)
    asm.emitPUSH_RegDisp(T0, ArchEntrypoints.registersIPField.getOffset());

    // Restore the GPRs except for S0, TR, SP and JTOC
    // (restored above and then modified by pushing registers.ip!)
    Offset off = Offset.zero();
    for (byte i = 0; i < NUM_GPRS; i++, off = off.plus(WORDSIZE)) {
      if (i != S0.value() && i != ESI.value() && i != SP.value() &&
          (JTOC_REGISTER == null || i != JTOC_REGISTER.value())) {
        if (VM.BuildFor32Addr) {
          asm.emitMOV_Reg_RegDisp(GPR.lookup(i), S0, off);
        } else {
          asm.emitMOV_Reg_RegDisp_Quad(GPR.lookup(i), S0, off);
        }
      }
    }

    // Restore S0
    if (VM.BuildFor32Addr) {
      asm.emitMOV_Reg_RegDisp(S0, S0, Offset.fromIntZeroExtend(S0.value() << LG_WORDSIZE));
    } else {
      asm.emitMOV_Reg_RegDisp_Quad(S0, S0, Offset.fromIntZeroExtend(S0.value() << LG_WORDSIZE));
    }

    // Return to registers.ip (popping stack)
    asm.emitRET();
    return asm.getMachineCodes();
  }
}
