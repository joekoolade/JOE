//===------- VmkitGC.cpp - GC for JIT-generated functions -----------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "llvm/CodeGen/GCs.h"
#include "llvm/CodeGen/GCStrategy.h"
#include "llvm/Support/Compiler.h"

#include "mvm/JIT.h"

using namespace llvm;

namespace {
  class VmkitGC : public GCStrategy {
  public:
    VmkitGC();
  };
}

namespace mvm {
  void linkVmkitGC() { }
}

static GCRegistry::Add<VmkitGC>
X("vmkit", "VMKit GC for JIT-generated functions");

VmkitGC::VmkitGC() {
  NeededSafePoints = 1 << GC::PostCall;
}
