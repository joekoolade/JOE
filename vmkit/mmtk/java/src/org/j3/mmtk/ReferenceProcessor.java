//===--------------------- ReferenceProcessor.java ------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


package org.j3.mmtk;

import org.mmtk.plan.Plan;
import org.mmtk.plan.TraceLocal;
import org.mmtk.utility.options.Options;

import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.*;

import org.j3.runtime.VM;

@Uninterruptible
public final class ReferenceProcessor extends org.mmtk.vm.ReferenceProcessor {

  /********************************************************************
   * Class fields
   */

  private static final ReferenceProcessor softReferenceProcessor =
    new ReferenceProcessor(Semantics.SOFT);
  private static final ReferenceProcessor weakReferenceProcessor =
    new ReferenceProcessor(Semantics.WEAK);
  private static final ReferenceProcessor phantomReferenceProcessor =
    new ReferenceProcessor(Semantics.PHANTOM);
  
  private final Semantics semantics;
  private int SemanticsNum;

  private ReferenceProcessor(Semantics semantics) {
    this.semantics = semantics;
    SemanticsNum = semantics.ordinal();
  }
  
  /** 
   * Factory method.
   * Creates an instance of the appropriate reference type processor.
   * @return the reference processor
   */
  @Interruptible
  public static ReferenceProcessor get(Semantics semantics) {
    switch(semantics) {
    case WEAK:    return weakReferenceProcessor;
    case SOFT:    return softReferenceProcessor;
    case PHANTOM: return phantomReferenceProcessor;
    default:
      VM._assert(false,"Unrecognized semantics");
      return null;
    }   
  }

  /**
   * Scan through all references and forward.
   *
   * Collectors like MarkCompact determine liveness and move objects
   * using separate traces.
   *
   * Currently ignores the nursery hint.
   *
   * TODO parallelise this code
   *
   * @param trace The trace
   * @param nursery Is this a nursery collection ?
   */
  @Override
  public native void forward(TraceLocal trace, boolean nursery);

  /**
   * Clear the contents of the table. This is called when reference types are
   * disabled to make it easier for VMs to change this setting at runtime.
   */
  @Override
  public native void clear();

  /**
   * Scan through the list of references. Calls ReferenceProcessor's
   * processReference method for each reference and builds a new
   * list of those references still active.
   *
   * Depending on the value of <code>nursery</code>, we will either
   * scan all references, or just those created since the last scan.
   *
   * TODO parallelise this code
   *
   * @param nursery Scan only the newly created references
   */
  @Override
  public native void scan(TraceLocal trace, boolean nursery);

  /***********************************************************************
   *
   * Statistics and debugging
   */

  public native int countWaitingReferences();
}
