//===--------------------------- Assert.java ------------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


package org.j3.mmtk;

import org.mmtk.policy.Space;
import org.vmmagic.pragma.*;
import org.j3.runtime.VM;

@Uninterruptible
public final class Assert extends org.mmtk.vm.Assert {
  
  /**
   * <code>true</code> if assertions should be verified
   */
  protected final boolean getVerifyAssertionsConstant() {
    return VM.VerifyAssertions;
  }

  /**
   * This method should be called whenever an error is encountered.
   *
   * @param str A string describing the error condition.
   */
  public final void error(String str) {
    Space.printUsagePages();
    Space.printUsageMB();
    fail(str);
  }

  /**
   * Logs a message and traceback, then exits.
   *
   * @param message the string to log
   */
  public final void fail(String message) {
    VM.sysFail(message);
  }

  @Uninterruptible
  public final void exit(int rc) {
    VM.sysExit(rc);
  }

  /**
   * Checks that the given condition is true.  If it is not, this
   * method does a traceback and exits. All calls to this method
   * must be guarded by <code>VM.VERIFY_ASSERTIONS</code>.
   *
   * @param cond the condition to be checked
   */
  @Inline(value=Inline.When.AllArgumentsAreConstant)
  public final void _assert(boolean cond) {
    if (!org.mmtk.vm.VM.VERIFY_ASSERTIONS)
      VM.sysFail("All assertions must be guarded by VM.VERIFY_ASSERTIONS: please check the failing assertion");
    VM._assert(cond);
  }

  /**
   * Checks that the given condition is true.  If it is not, this
   * method prints a message, does a traceback and exits. All calls
   * to this method must be guarded by <code>VM.VERIFY_ASSERTIONS</code>.
   *
   * @param cond the condition to be checked
   * @param message the message to print
   */
  @Inline(value=Inline.When.ArgumentsAreConstant, arguments={1})
  public final void _assert(boolean cond, String message) {
    if (!org.mmtk.vm.VM.VERIFY_ASSERTIONS)
      VM.sysFail("All assertions must be guarded by VM.VERIFY_ASSERTIONS: please check the failing assertion");
    if (!cond) VM.sysWriteln(message);
    VM._assert(cond);
  }

  public native final void dumpStack();

  /**
   * Checks if the virtual machine is running.  This value changes, so
   * the call-through to the VM must be a method. 
   *
   * @return <code>true</code> if the virtual machine is running
   */
  public final boolean runningVM() { return VM.runningVM; }

}
