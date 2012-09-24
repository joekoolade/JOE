//===---- StaticGCPass.cpp - Put GC information in functions compiled --------//
//===----------------------- with llvm-gcc --------------------------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "llvm/Intrinsics.h"
#include "llvm/Module.h"
#include "llvm/Pass.h"
#include "llvm/Support/raw_ostream.h"

#include <cstdio>

using namespace llvm;

namespace {

  class StaticGCPass : public ModulePass {
  public:
    static char ID;
    
    StaticGCPass() : ModulePass(ID) {}

    virtual bool runOnModule(Module& M);

    /// getAnalysisUsage - We do not modify anything.
    virtual void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.setPreservesAll();
    } 

  };

  char StaticGCPass::ID = 0;
  RegisterPass<StaticGCPass> X("StaticGCPass",
                      "Add GC information in files compiled with llvm-gcc");

bool StaticGCPass::runOnModule(Module& M) {

  Function* F = M.getFunction("__llvm_gcroot");
  Function *gcrootFun = Intrinsic::getDeclaration(&M, Intrinsic::gcroot);

  if (F) {
    F->replaceAllUsesWith(gcrootFun);
    F->eraseFromParent();
  }

  bool error = false;
  for (Value::use_iterator I = gcrootFun->use_begin(),
       E = gcrootFun->use_end(); I != E; ++I) {
    if (Instruction* II = dyn_cast<Instruction>(*I)) {
      Function* F = II->getParent()->getParent();
      if (!F->hasGC()) {
        F->setGC("vmkit");
      }
    }
  }

  for (Module::iterator I = M.begin(), E = M.end(); I != E; ++I) {
    if (I->hasGC() && I->hasInternalLinkage()) {
      error = true;
      fprintf(stderr, "Method %s has static linkage but uses gc_root. "
                      "Functions using gc_root should not have static linkage.\n",
                      I->getName().data());
    }
  }

  if (error) abort();

  return true;
}

}
