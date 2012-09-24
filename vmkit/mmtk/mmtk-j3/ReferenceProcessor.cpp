//===-------- ReferenceProcessor.cpp --------------------------------------===//
//===-------- Implementation of the Selected class  -----------------------===//
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

namespace mmtk {

extern "C" void Java_org_j3_mmtk_ReferenceProcessor_scan__Lorg_mmtk_plan_TraceLocal_2Z (MMTkReferenceProcessor* RP, word_t TL, uint8_t nursery) {
  mvm::Thread* th = mvm::Thread::get();
  uint32_t val = RP->ordinal;

  if (val == 0) {
    th->MyVM->scanSoftReferencesQueue(TL);
  } else if (val == 1) {
    th->MyVM->scanWeakReferencesQueue(TL);
  } else {
    assert(val == 2);
    th->MyVM->scanPhantomReferencesQueue(TL);
  }
}

extern "C" void Java_org_j3_mmtk_ReferenceProcessor_forward__Lorg_mmtk_plan_TraceLocal_2Z (MMTkReferenceProcessor* RP, word_t TL, uint8_t nursery) { UNIMPLEMENTED(); }
extern "C" void Java_org_j3_mmtk_ReferenceProcessor_clear__ (MMTkReferenceProcessor* RP) { UNIMPLEMENTED(); }
extern "C" void Java_org_j3_mmtk_ReferenceProcessor_countWaitingReferences__ (MMTkReferenceProcessor* RP) { UNIMPLEMENTED(); }

}
