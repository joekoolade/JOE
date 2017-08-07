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

import org.vmmagic.pragma.*;

public class Selected {
  public static final String name = "org.mmtk.plan.semispace.SS";
  @Uninterruptible
  public static final class Plan extends org.mmtk.plan.semispace.SS
  {
    private static final Plan plan = new Plan();

    @Inline
    public static Plan get() { return plan; }
  }

  @Uninterruptible
  public static final class Collector extends org.mmtk.plan.semispace.SSCollector
  {
  }

  @Uninterruptible
  public static final class Constraints extends org.mmtk.plan.semispace.SSConstraints
  {
    private static final Constraints constraints = new Constraints();

    @Inline
    public static Constraints get() { return constraints; }
  }

  @Uninterruptible
  public static class Mutator extends org.mmtk.plan.semispace.SSMutator
  {
    @Inline
    public final RVMThread getThread() { return (RVMThread) this; }
    @Inline
    public static Mutator get() { return RVMThread.getCurrentThread(); }
  }
}
