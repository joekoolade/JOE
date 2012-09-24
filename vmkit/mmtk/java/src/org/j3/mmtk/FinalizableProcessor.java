//===---------------------- FinalizableProcessor.java ---------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.*;

import org.j3.runtime.VM;
import org.j3.config.Selected;
import org.mmtk.plan.TraceLocal;

/**
 * This class manages the processing of finalizable objects.
 */

@Uninterruptible
public final class FinalizableProcessor extends org.mmtk.vm.FinalizableProcessor {

  /********************************************************************
   * Class fields
   */

  /** The FinalizableProcessor singleton */
  private static final FinalizableProcessor finalizableProcessor = new FinalizableProcessor();

  public static FinalizableProcessor getProcessor() {
    return finalizableProcessor;
  }

  public native void clear();

  /**
   * Scan through all entries in the table and forward.
   *
   * Currently ignores the nursery hint.
   *
   * TODO parallelise this code?
   *
   * @param trace The trace
   * @param nursery Is this a nursery collection ?
   */
  @Override
  public native void forward(TraceLocal trace, boolean nursery);

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
  @UninterruptibleNoWarn
  public native void scan(TraceLocal trace, boolean nursery);

}
