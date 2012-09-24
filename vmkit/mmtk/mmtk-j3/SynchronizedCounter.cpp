//===-------- SynchronizedCounter.cpp -------------------------------------===//
//===-------- Implementation of the SynchronizedCounter class  ------------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MMTkObject.h"
#include "debug.h"

namespace mmtk {

extern "C" void Java_org_j3_mmtk_SynchronizedCounter_reset__ (MMTkObject* self) { UNIMPLEMENTED(); }
extern "C" void Java_org_j3_mmtk_SynchronizedCounter_increment__ (MMTkObject* self) { UNIMPLEMENTED(); }

} // end namespace mmtk
