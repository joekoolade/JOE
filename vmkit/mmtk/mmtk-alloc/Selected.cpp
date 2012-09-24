//===-------- Selected.cpp - Implementation of the Selected class  --------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MutatorThread.h"
#include "MvmGC.h"
#include "../mmtk-j3/MMTkObject.h"

#include "mvm/VirtualMachine.h"

#include <sys/mman.h>
#include <set>

#include "debug.h"

using namespace mvm;

int Collector::verbose = 0;
extern "C" void Java_org_j3_mmtk_Collection_triggerCollection__I(word_t, int32_t) ALWAYS_INLINE;

extern "C" word_t JnJVM_org_j3_bindings_Bindings_allocateMutator__I(int32_t) ALWAYS_INLINE;
extern "C" void JnJVM_org_j3_bindings_Bindings_freeMutator__Lorg_mmtk_plan_MutatorContext_2(word_t) ALWAYS_INLINE;
extern "C" void JnJVM_org_j3_bindings_Bindings_boot__Lorg_vmmagic_unboxed_Extent_2Lorg_vmmagic_unboxed_Extent_2_3Ljava_lang_String_2(word_t, word_t, mmtk::MMTkObjectArray*) ALWAYS_INLINE;

extern "C" void JnJVM_org_j3_bindings_Bindings_processEdge__Lorg_mmtk_plan_TransitiveClosure_2Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2(
    word_t closure, void* source, void* slot) ALWAYS_INLINE;

extern "C" void JnJVM_org_j3_bindings_Bindings_reportDelayedRootEdge__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_Address_2(
    word_t TraceLocal, void** slot) ALWAYS_INLINE;
extern "C" void JnJVM_org_j3_bindings_Bindings_processRootEdge__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_Address_2Z(
    word_t TraceLocal, void* slot, uint8_t untraced) ALWAYS_INLINE;
extern "C" gc* JnJVM_org_j3_bindings_Bindings_retainForFinalize__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(
    word_t TraceLocal, void* obj) ALWAYS_INLINE;
extern "C" gc* JnJVM_org_j3_bindings_Bindings_retainReferent__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(
    word_t TraceLocal, void* obj) ALWAYS_INLINE;
extern "C" gc* JnJVM_org_j3_bindings_Bindings_getForwardedReference__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(
    word_t TraceLocal, void* obj) ALWAYS_INLINE;
extern "C" gc* JnJVM_org_j3_bindings_Bindings_getForwardedReferent__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(
    word_t TraceLocal, void* obj) ALWAYS_INLINE;
extern "C" gc* JnJVM_org_j3_bindings_Bindings_getForwardedFinalizable__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(
    word_t TraceLocal, void* obj) ALWAYS_INLINE;
extern "C" uint8_t JnJVM_org_j3_bindings_Bindings_isLive__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(
    word_t TraceLocal, void* obj) ALWAYS_INLINE;
  
extern "C" uint8_t JnJVM_org_j3_bindings_Bindings_writeBarrierCAS__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2(gc* ref, gc** slot, gc* old, gc* value) ALWAYS_INLINE;
  
extern "C" void JnJVM_org_j3_bindings_Bindings_arrayWriteBarrier__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2(gc* ref, gc** ptr, gc* value) ALWAYS_INLINE;

extern "C" void JnJVM_org_j3_bindings_Bindings_fieldWriteBarrier__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2(gc* ref, gc** ptr, gc* value) ALWAYS_INLINE;
  
extern "C" void JnJVM_org_j3_bindings_Bindings_nonHeapWriteBarrier__Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2(gc** ptr, gc* value) ALWAYS_INLINE;

extern "C" void* JnJVM_org_j3_bindings_Bindings_gcmalloc__ILorg_vmmagic_unboxed_ObjectReference_2(
    int sz, void* VT) ALWAYS_INLINE;

extern "C" void* gcmalloc(uint32_t sz, void* VT) {
  sz = llvm::RoundUpToAlignment(sz, sizeof(void*));
  return (gc*)JnJVM_org_j3_bindings_Bindings_gcmalloc__ILorg_vmmagic_unboxed_ObjectReference_2(sz, VT);
}

extern "C" void addFinalizationCandidate(gc* obj) __attribute__((always_inline));

extern "C" void addFinalizationCandidate(gc* obj) {
  llvm_gcroot(obj, 0);
  mvm::Thread::get()->MyVM->addFinalizationCandidate(obj);
}

extern "C" void* gcmallocUnresolved(uint32_t sz, VirtualTable* VT) {
  gc* res = 0;
  llvm_gcroot(res, 0);
  res = (gc*)gcmalloc(sz, VT);
  if (VT->hasDestructor()) addFinalizationCandidate(res);
  return res;
}

extern "C" void arrayWriteBarrier(void* ref, void** ptr, void* value) {
  JnJVM_org_j3_bindings_Bindings_arrayWriteBarrier__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2(
      (gc*)ref, (gc**)ptr, (gc*)value);
  if (mvm::Thread::get()->doYield) mvm::Collector::collect();
}

extern "C" void fieldWriteBarrier(void* ref, void** ptr, void* value) {
  JnJVM_org_j3_bindings_Bindings_fieldWriteBarrier__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2(
      (gc*)ref, (gc**)ptr, (gc*)value);
  if (mvm::Thread::get()->doYield) mvm::Collector::collect();
}

extern "C" void nonHeapWriteBarrier(void** ptr, void* value) {
  JnJVM_org_j3_bindings_Bindings_nonHeapWriteBarrier__Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2((gc**)ptr, (gc*)value);
  if (mvm::Thread::get()->doYield) mvm::Collector::collect();
}

void MutatorThread::init(Thread* _th) {
  MutatorThread* th = (MutatorThread*)_th;
  th->MutatorContext =
    JnJVM_org_j3_bindings_Bindings_allocateMutator__I((int32_t)_th->getThreadID());
  th->realRoutine(_th);
  word_t context = th->MutatorContext;
  th->MutatorContext = 0;
  JnJVM_org_j3_bindings_Bindings_freeMutator__Lorg_mmtk_plan_MutatorContext_2(context);
}

bool Collector::isLive(gc* ptr, word_t closure) {
  return JnJVM_org_j3_bindings_Bindings_isLive__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(closure, ptr);
}

void Collector::scanObject(void** ptr, word_t closure) {
  if ((*ptr) != NULL) {
    assert(((gc*)(*ptr))->getVirtualTable());
  }
  JnJVM_org_j3_bindings_Bindings_reportDelayedRootEdge__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_Address_2(closure, ptr);
}
 
void Collector::markAndTrace(void* source, void* ptr, word_t closure) {
  void** ptr_ = (void**)ptr;
  if ((*ptr_) != NULL) {
    assert(((gc*)(*ptr_))->getVirtualTable());
  }
  if ((*(void**)ptr) != NULL) assert(((gc*)(*(void**)ptr))->getVirtualTable());
  JnJVM_org_j3_bindings_Bindings_processEdge__Lorg_mmtk_plan_TransitiveClosure_2Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2(closure, source, ptr);
}
  
void Collector::markAndTraceRoot(void* ptr, word_t closure) {
  void** ptr_ = (void**)ptr;
  if ((*ptr_) != NULL) {
    assert(((gc*)(*ptr_))->getVirtualTable());
  }
  JnJVM_org_j3_bindings_Bindings_processRootEdge__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_Address_2Z(closure, ptr, true);
}

gc* Collector::retainForFinalize(gc* val, word_t closure) {
  return JnJVM_org_j3_bindings_Bindings_retainForFinalize__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(closure, val);
}
  
gc* Collector::retainReferent(gc* val, word_t closure) {
  return JnJVM_org_j3_bindings_Bindings_retainReferent__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(closure, val);
}
  
gc* Collector::getForwardedFinalizable(gc* val, word_t closure) {
  return JnJVM_org_j3_bindings_Bindings_getForwardedFinalizable__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(closure, val);
}
  
gc* Collector::getForwardedReference(gc* val, word_t closure) {
  return JnJVM_org_j3_bindings_Bindings_getForwardedReference__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(closure, val);
}
  
gc* Collector::getForwardedReferent(gc* val, word_t closure) {
  return JnJVM_org_j3_bindings_Bindings_getForwardedReferent__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2(closure, val);
}

void Collector::collect() {
  Java_org_j3_mmtk_Collection_triggerCollection__I(NULL, 2);
}
  
static const char* kPrefix = "-X:gc:";
static const int kPrefixLength = strlen(kPrefix);

void Collector::initialise(int argc, char** argv) {
  int i = 1;
  int count = 0;
  ThreadAllocator allocator;
  mmtk::MMTkObjectArray* arguments = NULL;
  while (i < argc && argv[i][0] == '-') {
    if (!strncmp(argv[i], kPrefix, kPrefixLength)) {
      count++;
    }
    i++;
  }

  if (count > 0) {
    arguments = reinterpret_cast<mmtk::MMTkObjectArray*>(
        malloc(sizeof(mmtk::MMTkObjectArray) + count * sizeof(mmtk::MMTkString*)));
    arguments->size = count;
    i = 1;
    int arrayIndex = 0;
    while (i < argc && argv[i][0] == '-') {
      if (!strncmp(argv[i], kPrefix, kPrefixLength)) {
        int size = strlen(argv[i]) - kPrefixLength;
        mmtk::MMTkArray* array = reinterpret_cast<mmtk::MMTkArray*>(
            allocator.Allocate(sizeof(mmtk::MMTkArray) + size * sizeof(uint16_t)));
        array->size = size;
        for (uint32_t j = 0; j < array->size; j++) {
          array->elements[j] = argv[i][j + kPrefixLength];
        }
        mmtk::MMTkString* str = reinterpret_cast<mmtk::MMTkString*>(
            allocator.Allocate(sizeof(mmtk::MMTkString)));
        str->value = array;
        str->count = array->size;
        str->offset = 0;
        arguments->elements[arrayIndex++] = str;
      }
      i++;
    }
    assert(arrayIndex == count);
  }

  JnJVM_org_j3_bindings_Bindings_boot__Lorg_vmmagic_unboxed_Extent_2Lorg_vmmagic_unboxed_Extent_2_3Ljava_lang_String_2(20 * 1024 * 1024, 100 * 1024 * 1024, arguments);
}

extern "C" void* MMTkMutatorAllocate(uint32_t size, VirtualTable* VT) {
  void* val = MutatorThread::get()->Allocator.Allocate(size);
  ((void**)val)[0] = VT;
  return val;
}

void Collector::objectReferenceWriteBarrier(gc* ref, gc** slot, gc* value) {
  fieldWriteBarrier((void*)ref, (void**)slot, (void*)value);
}

void Collector::objectReferenceArrayWriteBarrier(gc* ref, gc** slot, gc* value) {
  arrayWriteBarrier((void*)ref, (void**)slot, (void*)value);
}

void Collector::objectReferenceNonHeapWriteBarrier(gc** slot, gc* value) {
  nonHeapWriteBarrier((void**)slot, (void*)value);
}

bool Collector::objectReferenceTryCASBarrier(gc* ref, gc** slot, gc* old, gc* value) {
  bool res = JnJVM_org_j3_bindings_Bindings_writeBarrierCAS__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2(ref, slot, old, value);
  if (mvm::Thread::get()->doYield) mvm::Collector::collect();
  return res;
}

extern "C" uint8_t JnJVM_org_j3_bindings_Bindings_needsWriteBarrier__() ALWAYS_INLINE;
extern "C" uint8_t JnJVM_org_j3_bindings_Bindings_needsNonHeapWriteBarrier__() ALWAYS_INLINE;

bool Collector::needsWriteBarrier() {
  return JnJVM_org_j3_bindings_Bindings_needsWriteBarrier__();
}

bool Collector::needsNonHeapWriteBarrier() {
  return JnJVM_org_j3_bindings_Bindings_needsNonHeapWriteBarrier__();
}

//TODO: Remove these.
std::set<gc*> __InternalSet__;
void* Collector::begOf(gc* obj) {
  abort();
}
