//===------------------------ Statistics.java -----------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.vmmagic.pragma.*;

@Uninterruptible
public final class Statistics extends org.mmtk.vm.Statistics {
  /**
   * Returns the number of collections that have occurred.
   *
   * @return The number of collections that have occurred.
   */
  @Uninterruptible
  public native int getCollectionCount();

  /**
   * Read nanoTime (high resolution, monotonically increasing clock).
   * Has same semantics as java.lang.System.nanoTime().
   */
  public native long nanoTime();

  /**
   * Read a cycle counter (high resolution, non-monotonic clock).
   * This method should be used with care as the cycle counters (especially on IA32 SMP machines)
   * are not a reliably time source.
   */
  public native long cycles();

  /**
   * Convert nanoseconds to milliseconds
   */
  public double nanosToMillis(long c) {
    return ((double)c)/1e6;
  }

  /**
   * Convert nanoseconds to seconds
   */
  public double nanosToSecs(long c) {
    return ((double)c)/1e9;
  }

  /**
   * Convert milliseconds to nanoseconds
   */
  public long millisToNanos(double t) {
    return (long)(t * 1e6);
  }

  /**
   * Convert seconds to nanoseconds
   */
  public long secsToNanos(double t) {
    return (long)(t * 1e9);
  }

  /**
   * Initialize performance events
   */
  @Interruptible
  public native void perfEventInit(String events);

  /**
   * Read a performance event
   */
  public native void perfEventRead(int id, long[] values);
}

