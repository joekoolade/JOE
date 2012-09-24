//===-------------------- --- MMTk_Events.java ----------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.mmtk.policy.Space;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * Implementation of simple MMTK event generation hooks
 * to allow MMTk to generate TuningFork events.
 */
@Uninterruptible
public class MMTk_Events extends org.mmtk.vm.MMTk_Events {

  public native void tracePageAcquired(Space space, Address startAddress, int numPages);

  public native void tracePageReleased(Space space, Address startAddress, int numPages);

  public native void heapSizeChanged(Extent heapSize);
}
