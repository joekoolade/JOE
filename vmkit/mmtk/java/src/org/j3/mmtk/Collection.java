//===------------------------- Collection.java ----------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.mmtk.plan.Plan;
import org.mmtk.plan.CollectorContext;
import org.mmtk.plan.MutatorContext;
import org.mmtk.utility.options.Options;

import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.pragma.Unpreemptible;
import org.vmmagic.unboxed.Address;

@Uninterruptible
public final class Collection extends org.mmtk.vm.Collection {

  /**
   * Triggers a collection.
   *
   * @param why the reason why a collection was triggered.  0 to
   * <code>TRIGGER_REASONS - 1</code>.
   */
  @Unpreemptible("Becoming another thread interrupts the current thread, avoid preemption in the process")
  public native final void triggerCollection(int why);

  /**
   * Joins a collection.
   */
  @Unpreemptible("Becoming another thread interrupts the current thread, avoid preemption in the process")
  public native final void joinCollection();

  /**
   * The maximum number collection attempts across threads.
   */
  public native int maximumCollectionAttempt();

  /**
   * Report that the the physical allocation has succeeded.
   */
  public native void reportAllocationSuccess();

  /**
   * Report that a physical allocation has failed.
   */
  public native void reportPhysicalAllocationFailed();

  /**
   * Does the VM consider this an emergency allocation, where the normal
   * heap size rules can be ignored.
   */
  public native boolean isEmergencyAllocation();

  /**
   * Trigger an asynchronous collection, checking for memory
   * exhaustion first.
   */
  @Unpreemptible("Becoming another thread interrupts the current thread, avoid preemption in the process")
  public native final void triggerAsyncCollection(int why);

  /**
   * Determine whether a collection cycle has fully completed (this is
   * used to ensure a GC is not in the process of completing, to
   * avoid, for example, an async GC being triggered on the switch
   * from GC to mutator thread before all GC threads have switched.
   *
   * @return True if GC is not in progress.
   */
  @Uninterruptible
  public native final boolean noThreadsInGC();

  /**
   * Prepare a mutator for a collection.
   *
   * @param m the mutator to prepare
   */
  public native final void prepareMutator(MutatorContext m);

  /**
   * Prepare a collector for a collection.
   *
   * @param c the collector to prepare
   */
  public native final void prepareCollector(CollectorContext c);

  /**
   * Rendezvous with all other processors, returning the rank
   * (that is, the order this processor arrived at the barrier).
   */
  public native final int rendezvous(int where);

  // REVIEW: what are the semantics of this method in a concurrent collector?
  /** @return The number of active collector threads */
  public native final int activeGCThreads();

  /**
   * @return The ordinal ID of the running collector thread w.r.t.
   * the set of active collector threads (zero based)
   */
  public native final int activeGCThreadOrdinal();

  /**
   * Request each mutator flush remembered sets. This method
   * will trigger the flush and then yield until all processors have
   * flushed.
   */
  @UninterruptibleNoWarn("This method is really unpreemptible, since it involves blocking")
  public native void requestMutatorFlush();

}

