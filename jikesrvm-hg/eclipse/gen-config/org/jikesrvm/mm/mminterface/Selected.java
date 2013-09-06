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
package org.jikesrvm.mm.mminterface;

import org.jikesrvm.scheduler.RVMThread;

import org.mmtk.utility.Log;

import org.vmmagic.pragma.*;

public class Selected {
  public static final String name = "org.mmtk.plan.generational.immix.GenImmix";
  @Uninterruptible
  public static final class Plan extends org.mmtk.plan.generational.immix.GenImmix
  {
    private static final Plan plan = new Plan();

    @Inline
    public static Plan get() { return plan; }
  }

  @Uninterruptible
  public static final class Collector extends org.mmtk.plan.generational.immix.GenImmixCollector
  {
  }

  @Uninterruptible
  public static final class Constraints extends org.mmtk.plan.generational.immix.GenImmixConstraints
  {
    private static final Constraints constraints = new Constraints();

    @Inline
    public static Constraints get() { return constraints; }
  }

  @Uninterruptible
  public static class Mutator extends org.mmtk.plan.generational.immix.GenImmixMutator
  {
    @Inline
    public final RVMThread getThread() { return (RVMThread) this; }
    @Inline
    public static Mutator get() { return RVMThread.getCurrentThread(); }
  }
}
