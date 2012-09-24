//===----------- Lock.cpp - Implementation of the Lock class  -------------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "debug.h"

#include "MMTkObject.h"

namespace mmtk {

extern "C" void Java_org_j3_mmtk_Lock_acquire__(MMTkLock* l) NO_INLINE;
extern "C" void Java_org_j3_mmtk_Lock_acquire__(MMTkLock* l) {
  for (uint32 count = 0; count < 1000; ++count) {
    uint32 res = __sync_val_compare_and_swap(&(l->state), 0, 1); 
    if (!res) return;
  }   
    
  while (__sync_val_compare_and_swap(&(l->state), 0, 1)) {
    sched_yield();
  }
}

extern "C" void Java_org_j3_mmtk_Lock_release__(MMTkLock* l) {
  l->state = 0;
}


extern "C" void Java_org_j3_mmtk_Lock_check__I (MMTkLock* l, int i) {
  UNIMPLEMENTED();
}

}
