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

import static org.jikesrvm.ia32.BaselineConstants.WORDSIZE;
import static org.jikesrvm.ia32.RegisterConstants.ESP;
import static org.jikesrvm.ia32.StackframeLayoutConstants.INVISIBLE_METHOD_ID;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_BODY_OFFSET;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_HEADER_SIZE;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_SENTINEL_FP;
import static org.jikesrvm.ia32.StackframeLayoutConstants.THREAD_START_METHOD_ID;
import static org.jikesrvm.runtime.UnboxedSizeConstants.BYTES_IN_ADDRESS;

import org.jikesrvm.VM;
import org.jikesrvm.architecture.AbstractRegisters;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * The machine state comprising a thread's execution context, used both for
 * thread context switching and for software/hardware exception
 * reporting/delivery.
 */
@Uninterruptible
@NonMoving
public final class Registers extends AbstractRegisters {
  /** Frame pointer */
  @Entrypoint
  public Address fp;

  private static final int CS = 8;

  @Override
  public void clear() {
    fp = Address.zero();
    super.clear();
  }

  /**
   * @return framepointer for the deepest stackframe
   */
  @Override
  public Address getInnermostFramePointer() {
    return fp;
  }

  /**
   * @return next instruction address for the deepest stackframe
   */
  @Override
  public Address getInnermostInstructionAddress() {
    return ip;
  }

  /**
   * Updates the machine state as if the stackframe were unwound.
   */
  @Override
  public void unwindStackFrame() {
    ip = Magic.getReturnAddress(fp, RVMThread.getCurrentThread());
    fp = Magic.getCallerFramePointer(fp);
  }

  /**
   * Sets ip &amp; fp. used to control the stack frame at which a scan of
   * the stack during GC will start, for ex., the top java frame for
   * a thread that is blocked in native code during GC.
   *
   * @param newip the new instruction pointer
   * @param newfp the new frame pointer
   */
  @Override
  public void setInnermost(Address newip, Address newfp) {
    ip = newip;
    fp = newfp;
  }

  /**
   * set ip and fp values to those of the caller. used just prior to entering
   * sigwait to set fp &amp; ip so that GC will scan the threads stack
   * starting at the frame of the method that called sigwait.
   */
  public void setInnermost() {
    Address current_fp = Magic.getFramePointer();
    ip = Magic.getReturnAddress(current_fp);
    fp = Magic.getCallerFramePointer(current_fp);
  }

  /**
   * The following method initializes a thread stack as if
   * "startoff" method had been called by an empty baseline-compiled
   *  "sentinel" frame with one local variable
   *
   * @param ip The instruction pointer for the "startoff" method
   * @param sp The base of the stack
   */
  @Override
  @Uninterruptible
  public void initializeStack(Address ip, Address sp) {
    Address fp;
    VM.sysWrite("sp0: ", sp);
    VM.sysWriteln(" ip: ", ip);
    // sp = sp.minus(STACKFRAME_HEADER_SIZE); // last word of header
    // fp = sp.minus(SizeConstants.BYTES_IN_ADDRESS + STACKFRAME_BODY_OFFSET);
    // Magic.setCallerFramePointer(fp, STACKFRAME_SENTINEL_FP);
    // Magic.setCompiledMethodID(fp, INVISIBLE_METHOD_ID);
    // VM.sysWrite("sp1: ", sp); VM.sysWriteln(" fp: ", fp);
    /*
     * Setup the interrupt return and registers
     * 
     * Top of Stack 0 IP0; sentinel IP STACKFRAME_SENTINEL_FP FP0; sentinel FP
     * INVISIBLE_METHOD_ID cmd id0; invisible method id 0x200 code segment IP FP
     * THREAD_START_METHOD_ID
     */
    if (VM.buildFor32Addr())
    {
      sp.store(0, Offset.zero()); // IP0
      sp.store(STACKFRAME_SENTINEL_FP, Offset.zero().minus(4)); // FP0
      sp.store(INVISIBLE_METHOD_ID, Offset.zero().minus(8)); // cmd id0; invisible method id
      sp.store(0x200, Offset.zero().minus(12)); // EFLAGS
      sp.store(8, Offset.zero().minus(16)); // CS
      sp.store(ip, Offset.zero().minus(20)); // IP
      sp.store(sp.minus(8), Offset.zero().minus(24)); // FP
      sp.store(THREAD_START_METHOD_ID, Offset.zero().minus(28)); // thread start method id
      this.ip = ip;
      this.fp = sp.minus(24);
      // set the sp
      getGPRs().set(ESP.value(), sp.minus(52).toWord());
    } else
    {
      /*
       * Need align stack on 16-byte boundary
       */
      // sp = Address.fromIntZeroExtend(sp.plus(15).toInt() & ~0xF).plus(8);
      sp.store(0, Offset.zero()); // IP0
      sp.store(STACKFRAME_SENTINEL_FP, Offset.zero().minus(WORDSIZE)); // FP0
      sp.store(INVISIBLE_METHOD_ID, Offset.zero().minus(WORDSIZE * 2)); // cmd id0; invisible method id
      sp.store(0, Offset.zero().minus(WORDSIZE * 3)); // Stack selector, NULL
      sp.store(sp.minus(WORDSIZE * 9).toInt(), Offset.zero().minus(WORDSIZE * 4)); // Stack selector, NULL
      sp.store(0x200, Offset.zero().minus(WORDSIZE * 5)); // RFLAGS
      sp.store(CS, Offset.zero().minus(WORDSIZE * 6)); // code selector
      sp.store(ip, Offset.zero().minus(WORDSIZE * 7)); // RIP
      sp.store(0, Offset.zero().minus(WORDSIZE * 8)); // err code 0
      sp.store(THREAD_START_METHOD_ID, Offset.zero().minus(WORDSIZE * 9)); // thread start method id
      this.ip = ip;
      this.fp = sp.minus(WORDSIZE * 8);
      // set the sp
      getGPRs().set(ESP.value(), sp.minus(WORDSIZE * 7).toWord());
    }
    VM.sysWriteln("fp: ", this.fp);

//    sp = sp.minus(STACKFRAME_HEADER_SIZE);                   // last word of header
//    fp = sp.minus(BYTES_IN_ADDRESS).minus(STACKFRAME_BODY_OFFSET);
//    Magic.setCallerFramePointer(fp, STACKFRAME_SENTINEL_FP);
//    Magic.setCompiledMethodID(fp, INVISIBLE_METHOD_ID);
//
//    sp = sp.minus(BYTES_IN_ADDRESS);                                 // allow for one local
//    getGPRs().set(ESP.value(), sp.toWord());
//
//    this.fp = fp;
//    this.ip = ip;
  }

  /**
   * A thread's stack has been moved or resized.
   * Adjust the ESP register to reflect new position.
   *
   * @param delta The displacement to be applied
   * @param traceAdjustments Log all adjustments to stderr if true
   */
  @Uninterruptible
  @Override
  public void adjustESP(Offset delta, boolean traceAdjustments) {
    Word old = getGPRs().get(ESP.value());
    getGPRs().set(ESP.value(), old.plus(delta));
    if (traceAdjustments) {
      VM.sysWrite(" esp =");
      VM.sysWrite(getGPRs().get(ESP.value()));
    }
  }

  @Override
  public void dump() {
    super.dump();
    VM.sysWriteln("fp = ",fp);
  }
}

