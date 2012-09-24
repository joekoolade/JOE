//===--ReferenceQueue.cpp - Implementation of soft/weak/phantom references-===//
//
//                            The VMKit project
//
// This file is distributed under the University of Pierre et Marie Curie 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "ClasspathReflect.h"
#include "JavaClass.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "ReferenceQueue.h"

using namespace j3;

ReferenceThread::ReferenceThread(Jnjvm* vm) : JavaThread(vm),
    WeakReferencesQueue(ReferenceQueue::WEAK),
    SoftReferencesQueue(ReferenceQueue::SOFT), 
    PhantomReferencesQueue(ReferenceQueue::PHANTOM) {

  ToEnqueue = new gc*[INITIAL_QUEUE_SIZE];
  ToEnqueueLength = INITIAL_QUEUE_SIZE;
  ToEnqueueIndex = 0;
}


bool enqueueReference(gc* _obj) {
  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaObject* obj = (JavaObject*)_obj;
  llvm_gcroot(obj, 0);
  JavaMethod* meth = vm->upcalls->EnqueueReference;
  UserClass* cl = JavaObject::getClass(obj)->asClass();
  return (bool)meth->invokeIntSpecialBuf(vm, cl, obj, 0);
}

void invokeEnqueue(gc* res) {
  llvm_gcroot(res, 0);
  TRY {
    enqueueReference(res);
  } IGNORE;
  mvm::Thread::get()->clearException();
}

void ReferenceThread::enqueueStart(ReferenceThread* th) {
  gc* res = NULL;
  llvm_gcroot(res, 0);

  while (true) {
    th->EnqueueLock.lock();
    while (th->ToEnqueueIndex == 0) {
      th->EnqueueCond.wait(&th->EnqueueLock);
    }
    th->EnqueueLock.unlock();

    while (true) {
      th->ToEnqueueLock.acquire();
      if (th->ToEnqueueIndex != 0) {
        res = th->ToEnqueue[th->ToEnqueueIndex - 1];
        --th->ToEnqueueIndex;
      }
      th->ToEnqueueLock.release();
      if (!res) break;

      invokeEnqueue(res);
      res = NULL;
    }
  }
}


void ReferenceThread::addToEnqueue(gc* obj) {
  llvm_gcroot(obj, 0);
  if (ToEnqueueIndex >= ToEnqueueLength) {
    uint32 newLength = ToEnqueueLength * GROW_FACTOR;
    gc** newQueue = new gc*[newLength];
    if (!newQueue) {
      fprintf(stderr, "I don't know how to handle reference overflow yet!\n");
      abort();
    }   
    for (uint32 i = 0; i < ToEnqueueLength; ++i) {
      newQueue[i] = ToEnqueue[i];
    }   
    delete[] ToEnqueue;
    ToEnqueue = newQueue;
    ToEnqueueLength = newLength;
  }
  ToEnqueue[ToEnqueueIndex++] = obj;
}

gc** getReferentPtr(gc* _obj) {
  JavaObjectReference* obj = (JavaObjectReference*)_obj;
  llvm_gcroot(obj, 0);
  return (gc**)JavaObjectReference::getReferentPtr(obj);
}

void setReferent(gc* _obj, gc* val) {
  JavaObjectReference* obj = (JavaObjectReference*)_obj;
  llvm_gcroot(obj, 0);
  llvm_gcroot(val, 0);
  JavaObjectReference::setReferent(obj, (JavaObject*)val);
}
 
void clearReferent(gc* _obj) {
  JavaObjectReference* obj = (JavaObjectReference*)_obj;
  llvm_gcroot(obj, 0);
  JavaObjectReference::setReferent(obj, NULL);
}

gc* ReferenceQueue::processReference(gc* reference, ReferenceThread* th, word_t closure) {
  if (!mvm::Collector::isLive(reference, closure)) {
    clearReferent(reference);
    return NULL;
  }

  gc* referent = *(getReferentPtr(reference));

  if (!referent) {
    return NULL;
  }

  if (semantics == SOFT) {
    // TODO: are we are out of memory? Consider that we always are for now.
    if (false) {
      mvm::Collector::retainReferent(referent, closure);
    }
  } else if (semantics == PHANTOM) {
    // Nothing to do.
  }

  gc* newReference =
      mvm::Collector::getForwardedReference(reference, closure);
  if (mvm::Collector::isLive(referent, closure)) {
    gc* newReferent = mvm::Collector::getForwardedReferent(referent, closure);
    setReferent(newReference, newReferent);
    return newReference;
  } else {
    clearReferent(newReference);
    th->addToEnqueue(newReference);
    return NULL;
  }
}


void ReferenceQueue::scan(ReferenceThread* th, word_t closure) {
  uint32 NewIndex = 0;

  for (uint32 i = 0; i < CurrentIndex; ++i) {
    gc* obj = References[i];
    gc* res = processReference(obj, th, closure);
    if (res) References[NewIndex++] = res;
  }

  CurrentIndex = NewIndex;
}


FinalizerThread::FinalizerThread(Jnjvm* vm) : JavaThread(vm) {
  FinalizationQueue = new gc*[INITIAL_QUEUE_SIZE];
  QueueLength = INITIAL_QUEUE_SIZE;
  CurrentIndex = 0;

  ToBeFinalized = new gc*[INITIAL_QUEUE_SIZE];
  ToBeFinalizedLength = INITIAL_QUEUE_SIZE;
  CurrentFinalizedIndex = 0;
}

void FinalizerThread::growFinalizationQueue() {
  if (CurrentIndex >= QueueLength) {
    uint32 newLength = QueueLength * GROW_FACTOR;
    gc** newQueue = new gc*[newLength];
    if (!newQueue) {
      fprintf(stderr, "I don't know how to handle finalizer overflows yet!\n");
      abort();
    }
    for (uint32 i = 0; i < QueueLength; ++i) newQueue[i] = FinalizationQueue[i];
    delete[] FinalizationQueue;
    FinalizationQueue = newQueue;
    QueueLength = newLength;
  }
}

void FinalizerThread::growToBeFinalizedQueue() {
  if (CurrentFinalizedIndex >= ToBeFinalizedLength) {
    uint32 newLength = ToBeFinalizedLength * GROW_FACTOR;
    gc** newQueue = new gc*[newLength];
    if (!newQueue) {
      fprintf(stderr, "I don't know how to handle finalizer overflows yet!\n");
      abort();
    }
    for (uint32 i = 0; i < ToBeFinalizedLength; ++i) newQueue[i] = ToBeFinalized[i];
    delete[] ToBeFinalized;
    ToBeFinalized = newQueue;
    ToBeFinalizedLength = newLength;
  }
}


void FinalizerThread::addFinalizationCandidate(gc* obj) {
  llvm_gcroot(obj, 0);
  FinalizationQueueLock.acquire();
 
  if (CurrentIndex >= QueueLength) {
    growFinalizationQueue();
  }
  
  FinalizationQueue[CurrentIndex++] = obj;
  FinalizationQueueLock.release();
}
  

void FinalizerThread::scanFinalizationQueue(word_t closure) {
  uint32 NewIndex = 0;
  for (uint32 i = 0; i < CurrentIndex; ++i) {
    gc* obj = FinalizationQueue[i];

    if (!mvm::Collector::isLive(obj, closure)) {
      obj = mvm::Collector::retainForFinalize(FinalizationQueue[i], closure);
      
      if (CurrentFinalizedIndex >= ToBeFinalizedLength)
        growToBeFinalizedQueue();
      
      /* Add to object table */
      ToBeFinalized[CurrentFinalizedIndex++] = obj;
    } else {
      FinalizationQueue[NewIndex++] =
        mvm::Collector::getForwardedFinalizable(obj, closure);
    }
  }
  CurrentIndex = NewIndex;
}

typedef void (*destructor_t)(void*);

void invokeFinalizer(gc* _obj) {
  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaObject* obj = (JavaObject*)_obj;
  llvm_gcroot(obj, 0);
  JavaMethod* meth = vm->upcalls->FinalizeObject;
  UserClass* cl = JavaObject::getClass(obj)->asClass();
  meth->invokeIntVirtualBuf(vm, cl, obj, 0);
}

void invokeFinalize(gc* res) {
  llvm_gcroot(res, 0);
  TRY {
    invokeFinalizer(res);
  } IGNORE;
  mvm::Thread::get()->clearException();
}

void FinalizerThread::finalizerStart(FinalizerThread* th) {
  gc* res = NULL;
  llvm_gcroot(res, 0);

  while (true) {
    th->FinalizationLock.lock();
    while (th->CurrentFinalizedIndex == 0) {
      th->FinalizationCond.wait(&th->FinalizationLock);
    }
    th->FinalizationLock.unlock();

    while (true) {
      th->FinalizationQueueLock.acquire();
      if (th->CurrentFinalizedIndex != 0) {
        res = th->ToBeFinalized[th->CurrentFinalizedIndex - 1];
        --th->CurrentFinalizedIndex;
      }
      th->FinalizationQueueLock.release();
      if (!res) break;

      VirtualTable* VT = res->getVirtualTable();
      if (VT->operatorDelete) {
        destructor_t dest = (destructor_t)VT->destructor;
        dest(res);
      } else {
        invokeFinalize(res);
      }
      res = NULL;
    }
  }
}
