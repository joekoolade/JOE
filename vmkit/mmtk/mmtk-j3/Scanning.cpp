//===-------- Scanning.cpp - Implementation of the Scanning class  --------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "debug.h"
#include "mvm/VirtualMachine.h"
#include "MMTkObject.h"
#include "MvmGC.h"

namespace mmtk {

extern "C" void Java_org_j3_mmtk_Scanning_computeThreadRoots__Lorg_mmtk_plan_TraceLocal_2 (MMTkObject* Scanning, MMTkObject* TL) {
  // When entering this function, all threads are waiting on the rendezvous to
  // finish.
  mvm::Thread* th = mvm::Thread::get();
  mvm::Thread* tcur = th;
  
  do {
    tcur->scanStack(reinterpret_cast<word_t>(TL));
    tcur = (mvm::Thread*)tcur->next();
  } while (tcur != th);
}

extern "C" void Java_org_j3_mmtk_Scanning_computeGlobalRoots__Lorg_mmtk_plan_TraceLocal_2 (MMTkObject* Scanning, MMTkObject* TL) { 
  mvm::Thread::get()->MyVM->tracer(reinterpret_cast<word_t>(TL));
  
	mvm::Thread* th = mvm::Thread::get();
  mvm::Thread* tcur = th;
  
  do {
    tcur->tracer(reinterpret_cast<word_t>(TL));
    tcur = (mvm::Thread*)tcur->next();
  } while (tcur != th);
}

extern "C" void Java_org_j3_mmtk_Scanning_computeStaticRoots__Lorg_mmtk_plan_TraceLocal_2 (MMTkObject* Scanning, MMTkObject* TL) {
  // Nothing to do.
}

extern "C" void Java_org_j3_mmtk_Scanning_resetThreadCounter__ (MMTkObject* Scanning) {
  // Nothing to do.
}

extern "C" void Java_org_j3_mmtk_Scanning_specializedScanObject__ILorg_mmtk_plan_TransitiveClosure_2Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* Scanning, uint32_t id, MMTkObject* TC, gc* obj) ALWAYS_INLINE;

extern "C" void Java_org_j3_mmtk_Scanning_specializedScanObject__ILorg_mmtk_plan_TransitiveClosure_2Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* Scanning, uint32_t id, MMTkObject* TC, gc* obj) {
  assert(obj && "No object to trace");
  assert(obj->getVirtualTable() && "No virtual table");
  assert(obj->getVirtualTable()->tracer && "No tracer in VT");
  obj->tracer(reinterpret_cast<word_t>(TC));
}

extern "C" void Java_org_j3_mmtk_Scanning_preCopyGCInstances__Lorg_mmtk_plan_TraceLocal_2 (MMTkObject* Scanning, MMTkObject* TL) {
  // Nothing to do, there are no GC objects on which the GC depends.
}

extern "C" void Java_org_j3_mmtk_Scanning_scanObject__Lorg_mmtk_plan_TransitiveClosure_2Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* Scanning, word_t TC, gc* obj) {
  assert(obj && "No object to trace");
  assert(obj->getVirtualTable() && "No virtual table");
  assert(obj->getVirtualTable()->tracer && "No tracer in VT");
  obj->tracer(TC);
}

extern "C" void Java_org_j3_mmtk_Scanning_precopyChildren__Lorg_mmtk_plan_TraceLocal_2Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* Scanning, MMTkObject TL, word_t ref) { UNIMPLEMENTED(); }

}
