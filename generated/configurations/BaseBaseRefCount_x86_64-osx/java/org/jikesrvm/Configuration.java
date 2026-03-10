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
package org.jikesrvm;

import org.jikesrvm.runtime.CommandLineArgs;

/**
 * Flags that specify the configuration of our virtual machine.
 *
 * Note: Changing any <code>final</code> flags requires that the whole vm
 *       be recompiled and rebuilt after their values are changed.
 */
public abstract class Configuration {

  public static final boolean BuildForPowerPC = false;
  public static final boolean BuildForIA32 = !BuildForPowerPC;
  public static final boolean BuildForSSE2 = BuildForIA32 && (true || VM.BuildFor64Addr);
  public static final boolean BuildForSSE2Full = BuildForSSE2 && (true || VM.BuildFor64Addr);
  public static final boolean BuildForHwFsqrt = true && (false || VM.BuildForSSE2);
  /**
   * Does using Magic.attempt* mean that the effects of a {@code StoreLoad} barrier will happen? This is the case on
   * IA32 because attempts use {@code cmpxchg*} which has the effect of a full memory fence.
   */
  public static final boolean MagicAttemptImpliesStoreLoadBarrier = !BuildForPowerPC;

  public static final boolean BuildFor32Addr = false;
  public static final boolean BuildFor64Addr = !BuildFor32Addr;

  public static final boolean BuildForLinux = false;
  public static final boolean BuildForSolaris = false; 
  public static final boolean BuildForOsx = true;

  public static final boolean BuildForGnuClasspath = false;
  public static final boolean BuildForOpenJDK = true;

  public static final boolean LittleEndian = true;

  /* ABI selection for PowerPC.  Exactly one of these variables will be true in each build. */
  public static final boolean BuildForPower64ELF_ABI = BuildForLinux && BuildForPowerPC && BuildFor64Addr;
  public static final boolean BuildForSVR4ABI = !BuildForPower64ELF_ABI;

  /** Are we using Classpath's portable native sync feature? */
  public static final boolean PortableNativeSync = false;

 /** Assertion checking.
      <dl>
      <dt>false</dt>  <dd> no assertion checking at runtime</dd>
      <dt>true  </dt> <dd> execute assertion checks at runtime</dd>
      </dl>

      Note: code your assertion checks as
      <pre>
        if (VM.VerifyAssertions)
          VM._assert(xxx);
      </pre>
  */
  public static final boolean VerifyAssertions = true;
  public static final boolean ExtremeAssertions = false;

  /**
   * If set, verify that Uninterruptible methods actually cannot be
   * interrupted.
   */
  public static final boolean VerifyUnint = VerifyAssertions;

  /** If set, ignore the supression pragma and print all warning messages. */
  public static final boolean ParanoidVerifyUnint = false;

  /** Is this an adaptive build? */
  public static final boolean BuildForAdaptiveSystem = false;

  /** Is this an opt compiler build? */
  public static final boolean BuildForOptCompiler = false;

  /** build with Base boot image compiler? */
  public static final boolean BuildWithBaseBootImageCompiler = true;

  /** allow bootimage writer to build oversized images? */
  public static final boolean AllowOversizedImages = false;

  // Interface method dispatch strategy.
  // We either use IMT's (Alpern, Cocchi, Fink, Grove, and Lieber OOPSLA 2001)
  // or searched ITables. See also the research archive for the variants on these
  // two schemes that were evaluated in the OOPSLA 2001 paper.
  public static final boolean BuildForIMTInterfaceInvocation = true;
  public static final boolean BuildForITableInterfaceInvocation = !BuildForIMTInterfaceInvocation;

  /** Epilogue yieldpoints increase sampling accuracy for adaptive
      recompilation.  In particular, they are key for large, leaf, loop-free
      methods.  */
  public static final boolean UseEpilogueYieldPoints = BuildForAdaptiveSystem;

  /** NUmber of allocations between gc's during stress testing. Set to 0 to disable. */
  public static final int StressGCAllocationInterval = 0;
  public static final boolean ForceFrequentGC = 0 != StressGCAllocationInterval;

  public static final boolean BuildWithGCTrace = false;
  public static final boolean BuildWithGCSpy = false;

  public static final String RVM_VERSION_STRING = "Jikes RVM 3.1.4+git (rd6a4d76753aa6ba856699cbdf91226535e1133d6)";
  public static final String RVM_CONFIGURATION = "BaseBaseRefCount";

  public static final String OPENJDK_LIB_ARCH = "amd64";

  /**
   * Alignment checking (for IA32 only; for debugging purposes only).
   * To enable, build with -Dconfig.alignment-checking=true.
   * Important: You'll also need to build without SSE (-Dtarget.arch.sse2=none) and
   * run Jikes with only one processor.
   */
  public static final boolean AlignmentChecking = false;

  /**
   * Sets properties for the Jikes RVM Junit test runner.
   * <p>
   * In order to skip certain unit tests based on the configuration of the VM
   * that the test runner is running in, we need to be able to do basic
   * introspection of Jikes RVM specific features. That is implemented by
   * setting properties that the test runner can then query. This approach
   * avoids linking with Jikes RVM code (which would make the test runner
   * fragile and annoying to compile).
   */
  public static void setupPropertiesForUnitTesting() {
    String targetArch;
    if (BuildForIA32) {
      targetArch = "ia32";
    } else if (BuildForPowerPC) {
      targetArch = "ppc";
    } else {
      // Can't use assertions because that would require importing VM, which
      // is a subclass of this class
      throw new InternalError("Unknown architecture");
    }
    System.setProperty("jikesrvm.target.arch", targetArch);

    if (BuildForOptCompiler) {
      System.setProperty("jikesrvm.include.opt", "true");
    }

    String addressingMode = BuildFor32Addr ? "32-bit" : "64-bit";
    System.setProperty("jikesrvm.addressing.mode", addressingMode);
  }
}
