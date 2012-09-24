//===---------------- ActivePlan.java - Plan for J3 -----------------------===//
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
import org.mmtk.plan.PlanConstraints;
import org.mmtk.utility.Log;

import org.j3.config.Selected;

import org.vmmagic.pragma.*;

public final class ActivePlan extends org.mmtk.vm.ActivePlan {

  Object currentThread = null;

  /** @return The active Plan instance. */
  @Inline
  public Plan global() {
    return Selected.Plan.get();
  }

  /** @return The active PlanConstraints instance. */
  @Inline
  public PlanConstraints constraints() {
    return Selected.Constraints.get();
  }

  /** @return The number of registered CollectorContext instances. */
  @Inline
  public native int collectorCount();

  /** @return The active CollectorContext instance. */
  @Inline
  public CollectorContext collector() {
    return Selected.Collector.get();
  }

  /** @return The active MutatorContext instance. */
  @Inline
  public MutatorContext mutator() {
    return Selected.Mutator.get();
  }

  /** @return The log for the active thread */
  public Log log() {
    return Selected.Mutator.get().getLog();
  }

  /** Reset the mutator iterator */
  public native void resetMutatorIterator();

  /**
   * Return the next <code>MutatorContext</code> in a
   * synchronized iteration of all mutators.
   *
   * @return The next <code>MutatorContext</code> in a
   *  synchronized iteration of all mutators, or
   *  <code>null</code> when all mutators have been done.
   */
  public native MutatorContext getNextMutator();
}
