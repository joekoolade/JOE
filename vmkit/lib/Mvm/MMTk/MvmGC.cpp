//===----------- MvmGC.cpp - Garbage Collection Interface -----------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MvmGC.h"
#include "MutatorThread.h"
#include "mvm/VirtualMachine.h"

#include <set>

using namespace mvm;

static mvm::SpinLock lock;
std::set<gc*> __InternalSet__;
int Collector::verbose = 0;

extern "C" void* gcmalloc(uint32_t sz, void* _VT) {
  gc* res = 0;
  VirtualTable* VT = (VirtualTable*)_VT;
  sz = llvm::RoundUpToAlignment(sz, sizeof(void*));
  res = (gc*)malloc(sz);
  memset((void*)res, 0, sz);
  
  lock.acquire();
  __InternalSet__.insert(res);
  lock.release();
  
  res->setVirtualTable(VT);
  return res;
}

extern "C" void* gcmallocUnresolved(uint32_t sz, VirtualTable* VT) {
  gc* res = (gc*)gcmalloc(sz, VT);
  if (VT->hasDestructor())
    mvm::Thread::get()->MyVM->addFinalizationCandidate(res);
  return res;
}

extern "C" void addFinalizationCandidate(gc* obj) {
  mvm::Thread::get()->MyVM->addFinalizationCandidate(obj);
}

extern "C" void* AllocateMagicArray(int32_t sz, void* length) {
  gc* res = (gc*)malloc(sz);
  memset((void*)res, 0, sz);
  ((void**)res)[0] = length;
  return res;
}

void* Collector::begOf(gc* obj) {
  lock.acquire();
  std::set<gc*>::iterator I = __InternalSet__.find(obj);
  std::set<gc*>::iterator E = __InternalSet__.end();
  lock.release();
    
  if (I != E) return obj;
  return 0;
}

void MutatorThread::init(Thread* _th) {
  MutatorThread* th = (MutatorThread*)_th;
  th->realRoutine(_th);
}

bool Collector::isLive(gc* ptr, word_t closure) {
  abort();
  return false;
}

void Collector::scanObject(void** ptr, word_t closure) {
  abort();
}
 
void Collector::markAndTrace(void* source, void* ptr, word_t closure) {
  abort();
}
  
void Collector::markAndTraceRoot(void* ptr, word_t closure) {
  abort();
}

gc* Collector::retainForFinalize(gc* val, word_t closure) {
  abort();
  return NULL;
}
  
gc* Collector::retainReferent(gc* val, word_t closure) {
  abort();
  return NULL;
}
  
gc* Collector::getForwardedFinalizable(gc* val, word_t closure) {
  abort();
  return NULL;
}
  
gc* Collector::getForwardedReference(gc* val, word_t closure) {
  abort();
  return NULL;
}
  
gc* Collector::getForwardedReferent(gc* val, word_t closure) {
  abort();
  return NULL;
}

extern "C" void arrayWriteBarrier(void* ref, void** ptr, void* value) {
  *ptr = value;
}

extern "C" void fieldWriteBarrier(void* ref, void** ptr, void* value) {
  *ptr = value;
}

extern "C" void nonHeapWriteBarrier(void** ptr, void* value) {
  *ptr = value;
}


void Collector::objectReferenceWriteBarrier(gc* ref, gc** slot, gc* value) {
  *slot = value;
}

void Collector::objectReferenceArrayWriteBarrier(gc* ref, gc** slot, gc* value) {
  *slot = value;
}

void Collector::objectReferenceNonHeapWriteBarrier(gc** slot, gc* value) {
  *slot = value;
}

bool Collector::objectReferenceTryCASBarrier(gc*ref, gc** slot, gc* old, gc* value) {
  gc* res = __sync_val_compare_and_swap(slot, old, value);
  return (old == res);
}

void Collector::collect() {
  // Do nothing.
}

void Collector::initialise(int argc, char** argv) {
}

bool Collector::needsWriteBarrier() {
  return false;
}

bool Collector::needsNonHeapWriteBarrier() {
  return false;
}
