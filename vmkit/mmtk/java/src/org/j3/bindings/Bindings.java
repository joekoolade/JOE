//===-------------------------- Bindings.java -----------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.bindings;

import org.j3.config.Selected;
import org.j3.options.OptionSet;
import org.mmtk.plan.MutatorContext;
import org.mmtk.plan.Plan;
import org.mmtk.plan.TraceLocal;
import org.mmtk.plan.TransitiveClosure;
import org.mmtk.utility.heap.HeapGrowthManager;
import org.mmtk.utility.Constants;
import org.mmtk.utility.Log;
import org.mmtk.utility.options.Options;
import org.mmtk.vm.Collection;
import org.mmtk.vm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;

import org.vmutil.options.AddressOption;
import org.vmutil.options.BooleanOption;
import org.vmutil.options.EnumOption;
import org.vmutil.options.FloatOption;
import org.vmutil.options.IntOption;
import org.vmutil.options.MicrosecondsOption;
import org.vmutil.options.Option;
import org.vmutil.options.PagesOption;
import org.vmutil.options.StringOption;


public final class Bindings {
  @Inline
  private static Address gcmalloc(int size, ObjectReference virtualTable) {
    Selected.Mutator mutator = Selected.Mutator.get();
    int allocator = mutator.checkAllocator(size, 0, 0);
    Address res = mutator.alloc(size, 0, 0, allocator, 0);
    res.store(virtualTable);
    mutator.postAlloc(res.toObjectReference(), virtualTable, size, allocator);
    return res;
  }

  @Inline
  private static boolean isLive(TraceLocal closure, ObjectReference obj) {
    return closure.isLive(obj);
  }

  @Inline
  private static void reportDelayedRootEdge(TraceLocal closure, Address obj) {
    closure.reportDelayedRootEdge(obj);
  }

  @Inline
  private static void processRootEdge(TraceLocal closure, Address obj, boolean traced) {
    closure.processRootEdge(obj, traced);
  }

  @Inline
  private static ObjectReference retainForFinalize(TraceLocal closure, ObjectReference obj) {
    return closure.retainForFinalize(obj);
  }

  @Inline
  private static ObjectReference retainReferent(TraceLocal closure, ObjectReference obj) {
    return closure.retainReferent(obj);
  }

  @Inline
  private static ObjectReference getForwardedReference(TraceLocal closure, ObjectReference obj) {
    return closure.getForwardedReference(obj);
  }

  @Inline
  private static ObjectReference getForwardedReferent(TraceLocal closure, ObjectReference obj) {
    return closure.getForwardedReferent(obj);
  }

  @Inline
  private static ObjectReference getForwardedFinalizable(TraceLocal closure, ObjectReference obj) {
    return closure.getForwardedFinalizable(obj);
  }

  @Inline
  private static void processEdge(TransitiveClosure closure, ObjectReference source, Address slot) {
    closure.processEdge(source, slot);
  }

  @Inline
  private static MutatorContext allocateMutator(int id) {
    Selected.Mutator mutator = new Selected.Mutator();
    mutator.initMutator(id);
    return mutator;
  }

  @Inline
  private static void freeMutator(MutatorContext mutator) {
    mutator.deinitMutator();
  }

  @Inline
  private static void boot(Extent minSize, Extent maxSize, String[] arguments) {
    if (arguments != null) {
      OptionSet.parseOptions(arguments);
    }
    HeapGrowthManager.boot(minSize, maxSize);
    Plan plan = Selected.Plan.get();
    plan.boot();
    plan.postBoot();
    plan.fullyBooted();
  }

  @Inline
  private static Address copy(ObjectReference from,
                              ObjectReference virtualTable,
                              int size,
                              int allocator) {
    Selected.Collector plan = Selected.Collector.get();
    allocator = plan.copyCheckAllocator(from, size, 0, allocator);
    Address to = plan.allocCopy(from, size, 0, 0, allocator);
    memcpy(to.toObjectReference(), from, size);
    plan.postCopy(to.toObjectReference(), virtualTable, size, allocator);
    return to;
  }

  @Inline
  private static native void memcpy(
      ObjectReference to, ObjectReference from, int size);

  @Inline
  private static void arrayWriteBarrier(ObjectReference ref, Address slot, ObjectReference value) {
    if (Selected.Constraints.get().needsObjectReferenceWriteBarrier()) {
      Selected.Mutator mutator = Selected.Mutator.get();
      mutator.objectReferenceWrite(ref, slot, value, slot.toWord(), slot.toWord(), Constants.ARRAY_ELEMENT);
    } else {
      slot.store(value);
    }
  }
  
  @Inline
  private static void fieldWriteBarrier(ObjectReference ref, Address slot, ObjectReference value) {
    if (Selected.Constraints.get().needsObjectReferenceWriteBarrier()) {
      Selected.Mutator mutator = Selected.Mutator.get();
      mutator.objectReferenceWrite(ref, slot, value, slot.toWord(), slot.toWord(), Constants.INSTANCE_FIELD);
    } else {
      slot.store(value);
    }
  }
  
  @Inline
  private static void nonHeapWriteBarrier(Address slot, ObjectReference value) {
    if (Selected.Constraints.get().needsObjectReferenceNonHeapWriteBarrier()) {
      Selected.Mutator mutator = Selected.Mutator.get();
      mutator.objectReferenceNonHeapWrite(slot, value, slot.toWord(), slot.toWord());
    } else {
      slot.store(value);
    }
  }
  
  @Inline
  private static boolean writeBarrierCAS(ObjectReference src, Address slot, ObjectReference old, ObjectReference value) {
    if (Selected.Constraints.get().needsObjectReferenceWriteBarrier()) {
      Selected.Mutator mutator = Selected.Mutator.get();
      return mutator.objectReferenceTryCompareAndSwap(src, slot, old, value, slot.toWord(), slot.toWord(), Constants.INSTANCE_FIELD);
    } else {
      return slot.attempt(old.toAddress().toWord(), value.toAddress().toWord());
    }
  }

  @Inline
  private static boolean needsWriteBarrier() {
    return Selected.Constraints.get().needsObjectReferenceWriteBarrier();
  }

  @Inline
  private static boolean needsNonHeapWriteBarrier() {
    return Selected.Constraints.get().needsObjectReferenceNonHeapWriteBarrier();
  }

  @Inline
  private static void collect(int why) {
    boolean userTriggered = why == Collection.EXTERNAL_GC_TRIGGER;
    boolean internalPhaseTriggered = why == Collection.INTERNAL_PHASE_GC_TRIGGER;

    if (Options.verbose.getValue() == 1 || Options.verbose.getValue() == 2) {
      if (userTriggered) {
        Log.write("[Forced GC]");
      } else if (internalPhaseTriggered) {
        Log.write("[Phase GC]");
      }
    }

    Plan.setCollectionTriggered();
    Plan.setCollectionTrigger(why);

    long startTime = VM.statistics.nanoTime();
    Selected.Collector.staticCollect();
    long elapsedTime = VM.statistics.nanoTime() - startTime;

    HeapGrowthManager.recordGCTime(((double)elapsedTime) / 1000000);

    if (Selected.Plan.get().lastCollectionFullHeap() && !internalPhaseTriggered) {
      if (Options.variableSizeHeap.getValue() && !userTriggered) {
        // Don't consider changing the heap size if gc was forced by System.gc()
        HeapGrowthManager.considerHeapSize();
      }
      HeapGrowthManager.reset();
    }

    Plan.collectionComplete();
  }
}
