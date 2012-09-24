//===---ReferenceQueue.h - Implementation of soft/weak/phantom references--===//
//
//                            The VMKit project
//
// This file is distributed under the University of Pierre et Marie Curie 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef J3_REFERENCE_QUEUE_H
#define J3_REFERENCE_QUEUE_H

#include "mvm/Threads/Locks.h"

#include "JavaThread.h"

// Same values than JikesRVM
#define INITIAL_QUEUE_SIZE 256
#define GROW_FACTOR 2

namespace j3 {

class ReferenceThread;
class Jnjvm;

class ReferenceQueue {
private:
  gc** References;
  uint32 QueueLength;
  uint32 CurrentIndex;
  mvm::SpinLock QueueLock;
  uint8_t semantics;

  gc* processReference(gc*, ReferenceThread*, word_t closure);
public:

  static const uint8_t WEAK = 1;
  static const uint8_t SOFT = 2;
  static const uint8_t PHANTOM = 3;


  ReferenceQueue(uint8_t s) {
    References = new gc*[INITIAL_QUEUE_SIZE];
    memset(References, 0, INITIAL_QUEUE_SIZE * sizeof(gc*));
    QueueLength = INITIAL_QUEUE_SIZE;
    CurrentIndex = 0;
    semantics = s;
  }

  ~ReferenceQueue() {
    delete[] References;
  }
 
  void addReference(gc* ref) {
    llvm_gcroot(ref, 0);
    QueueLock.acquire();
    if (CurrentIndex >= QueueLength) {
      uint32 newLength = QueueLength * GROW_FACTOR;
      gc** newQueue = new gc*[newLength];
      if (!newQueue) {
        fprintf(stderr, "I don't know how to handle reference overflow yet!\n");
        abort();
      }
      memset(newQueue, 0, newLength * sizeof(gc*));
      for (uint32 i = 0; i < QueueLength; ++i) newQueue[i] = References[i];
      delete[] References;
      References = newQueue;
      QueueLength = newLength;
    }
    References[CurrentIndex++] = ref;
    QueueLock.release();
  }
  
  void acquire() {
    QueueLock.acquire();
  }

  void release() {
    QueueLock.release();
  }

  void scan(ReferenceThread* thread, word_t closure);
};

class ReferenceThread : public JavaThread {
public:
  /// WeakReferencesQueue - The queue of weak references.
  ///
  ReferenceQueue WeakReferencesQueue;

  /// SoftReferencesQueue - The queue of soft references.
  ///
  ReferenceQueue SoftReferencesQueue;

  /// PhantomReferencesQueue - The queue of phantom references.
  ///
  ReferenceQueue PhantomReferencesQueue;

  gc** ToEnqueue;
  uint32 ToEnqueueLength;
  uint32 ToEnqueueIndex;
  
  /// ToEnqueueLock - A lock to protect access to the queue.
  ///
  mvm::LockNormal EnqueueLock;
  mvm::Cond EnqueueCond;
  mvm::SpinLock ToEnqueueLock;

  void addToEnqueue(gc* obj);

  static void enqueueStart(ReferenceThread*);

  /// addWeakReference - Add a weak reference to the queue.
  ///
  void addWeakReference(gc* ref) {
    llvm_gcroot(ref, 0);
    WeakReferencesQueue.addReference(ref);
  }
  
  /// addSoftReference - Add a weak reference to the queue.
  ///
  void addSoftReference(gc* ref) {
    llvm_gcroot(ref, 0);
    SoftReferencesQueue.addReference(ref);
  }
  
  /// addPhantomReference - Add a weak reference to the queue.
  ///
  void addPhantomReference(gc* ref) {
    llvm_gcroot(ref, 0);
    PhantomReferencesQueue.addReference(ref);
  }

  ReferenceThread(Jnjvm* vm);

  ~ReferenceThread() {
    delete[] ToEnqueue;
  }
};

class FinalizerThread : public JavaThread {
public:
    /// FinalizationQueueLock - A lock to protect access to the queue.
  ///
  mvm::SpinLock FinalizationQueueLock;

  /// finalizationQueue - A list of allocated objets that contain a finalize
  /// method.
  ///
  gc** FinalizationQueue;

  /// CurrentIndex - Current index in the queue of finalizable objects.
  ///
  uint32 CurrentIndex;

  /// QueueLength - Current length of the queue of finalizable objects.
  ///
  uint32 QueueLength;

  /// growFinalizationQueue - Grow the queue of finalizable objects.
  ///
  void growFinalizationQueue();
  
  /// ToBeFinalized - List of objects that are scheduled to be finalized.
  ///
  gc** ToBeFinalized;
  
  /// ToBeFinalizedLength - Current length of the queue of objects scheduled
  /// for finalization.
  ///
  uint32 ToBeFinalizedLength;

  /// CurrentFinalizedIndex - The current index in the ToBeFinalized queue
  /// that will be sceduled for finalization.
  ///
  uint32 CurrentFinalizedIndex;
  
  /// growToBeFinalizedQueue - Grow the queue of the to-be finalized objects.
  ///
  void growToBeFinalizedQueue();
  
  /// finalizationCond - Condition variable to wake up finalization threads.
  ///
  mvm::Cond FinalizationCond;

  /// finalizationLock - Lock for the condition variable.
  ///
  mvm::LockNormal FinalizationLock;

  static void finalizerStart(FinalizerThread*);

  /// addFinalizationCandidate - Add an object to the queue of objects with
  /// a finalization method.
  ///
  void addFinalizationCandidate(gc*);

  /// scanFinalizationQueue - Scan objets with a finalized method and schedule
  /// them for finalization if they are not live.
  ///
  void scanFinalizationQueue(word_t closure);

  FinalizerThread(Jnjvm* vm);

  ~FinalizerThread() {
    delete[] FinalizationQueue;
    delete[] ToBeFinalized;
  }
};

} // namespace j3

#endif  //J3_REFERENCE_QUEUE_H
