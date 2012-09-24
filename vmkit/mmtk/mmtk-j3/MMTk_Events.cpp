//===----- MMTk_Events.cpp - Implementation of the MMTk_Events class  -----===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MMTkObject.h"

namespace mmtk {

extern "C" void Java_org_j3_mmtk_MMTk_1Events_tracePageAcquired__Lorg_mmtk_policy_Space_2Lorg_vmmagic_unboxed_Address_2I(
    MMTkObject* event, MMTkObject* space, word_t address, int numPages) {
#if 0
  fprintf(stderr, "Pages acquired by thread %p from space %p at %x (%d)\n", (void*)mvm::Thread::get(), (void*)space, address, numPages);
#endif
}

extern "C" void Java_org_j3_mmtk_MMTk_1Events_tracePageReleased__Lorg_mmtk_policy_Space_2Lorg_vmmagic_unboxed_Address_2I(
    MMTkObject* event, MMTkObject* space, word_t address, int numPages) {
#if 0
  fprintf(stderr, "Pages released by thread %p from space %p at %x (%d)\n", (void*)mvm::Thread::get(), (void*)space, address, numPages);
#endif
}

extern "C" void Java_org_j3_mmtk_MMTk_1Events_heapSizeChanged__Lorg_vmmagic_unboxed_Extent_2(
    MMTkObject* event, word_t heapSize) {
#if 0
  fprintf(stderr, "New heap size : %d\n", (int)heapSize);
#endif
}

}
