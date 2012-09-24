//===-------- Selected.cpp - Implementation of the Selected class  --------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MutatorThread.h"
#include "MMTkObject.h"

namespace mmtk {

extern "C" MMTkObject* Java_org_j3_config_Selected_00024Mutator_get__() {
  return (MMTkObject*)mvm::MutatorThread::get()->MutatorContext;
}

}
