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

/**
 * Flags that specify the configuration of our virtual machine.
 *
 * Note: Changing any <code>final</code> flags requires that the whole vm
 *       be recompiled and rebuilt after their values are changed.
 */
public abstract class Configuration {

  //TODO: Split target specific configuration into separate file
  public static final org.jikesrvm.ia32.MachineSpecificIA.IA32 archHelper = org.jikesrvm.ia32.MachineSpecificIA.IA32.singleton;

  public static final boolean BuildForPowerPC = false;
  public static final boolean BuildForIA32 = !BuildForPowerPC;
  public static final boolean BuildForSSE2 = BuildForIA32 && true;
  public static final boolean BuildForSSE2Full = BuildForSSE2 && true;
  public static final boolean BuildForHwFsqrt = true && (false || VM.BuildForSSE2);

  public static final boolean BuildFor32Addr = true;
  public static final boolean BuildFor64Addr = !BuildFor32Addr;

  public static final boolean BuildForAix = false;
  public static final boolean BuildForLinux = true;
  public static final boolean BuildForSolaris = false; 
  public static final boolean BuildForOsx = !BuildForAix && !BuildForLinux && !BuildForSolaris;

  public static final boolean BuildForGnuClasspath = true;
  public static final boolean BuildForHarmony = false;

  public static final boolean LittleEndian = BuildForIA32;

  /* ABI selection.  Exactly one of these variables will be true in each build. */
  public static final boolean BuildForMachOABI = BuildForOsx;
  public static final boolean BuildForPowerOpenABI = BuildForAix || (BuildForLinux && BuildForPowerPC && BuildFor64Addr);
  public static final boolean BuildForSVR4ABI = !(BuildForPowerOpenABI || BuildForMachOABI);

  /** Are we using Classpath's portable native sync feature? */
  public static final boolean PortableNativeSync = true;

  /**
   * Can a dereference of a null pointer result in an access
   * to 'low' memory addresses that must be explicitly guarded because the
   * target OS doesn't allow us to read protect low memory?
   */
  public static final boolean ExplicitlyGuardLowMemory = BuildForAix;

 /** Assertion checking.
      <dl>
      <dt>false</dt>  <dd> no assertion checking at runtime</dd>
      <dt>true  </dt> <dd> execute assertion checks at runtime</dd>
      <dl>

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
  public static final boolean BuildForAdaptiveSystem = true;

  /** Is this an opt compiler build? */
  public static final boolean BuildForOptCompiler = true;

  /** build with Base boot image compiler? */
  public static final boolean BuildWithBaseBootImageCompiler = false;

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
  
  public static final String RVM_VERSION_STRING = "Jikes RVM 3.1.3+hg (r50c500c6d5b8ee2c30fc985d4e93d16ed14fb6f1)";
  public static final String RVM_CONFIGURATION = "development";

  /**
   * Alignment checking (for IA32 only; for debugging purposes only).
   * To enable, build with -Dconfig.alignment-checking=true.
   * Important: You'll also need to build without SSE (-Dtarget.arch.sse2=none) and
   * run Jikes with only one processor.
   */
  public static final boolean AlignmentChecking = false;
}
