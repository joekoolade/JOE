//===---------------------------- VM.java ---------------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


package org.j3.runtime;

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

public final class VM {

  private native static boolean verifyAssertions();
  private native static boolean buildForIA32();
  private native static boolean buildFor64Addr();

  public final static boolean NOT_REACHED = false;

  public final static boolean VerifyAssertions = verifyAssertions();
  public final static boolean runningVM = false;
  public final static boolean BuildForIA32 = buildForIA32();
  public final static boolean BuildFor64Addr = buildFor64Addr();


  public native static void sysFail(String a);
  public native static void sysExit(int a);
  public native static void _assert(boolean cond);
  public native static void _assert(boolean cond, String msg);
  public native static void sysWriteln(String a);
  public native static void sysWriteln();
  public native static void sysWrite(String a);
  public native static void sysWrite(int a);
  public native static void sysWrite(float a);
  public native static void sysWrite(Address a);
  public native static void sysWrite(Extent a);
}
