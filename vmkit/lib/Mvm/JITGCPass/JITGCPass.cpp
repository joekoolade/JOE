//===------ JITGCPass.cpp - Put GC information in functions compiled ---------//
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

using namespace llvm;

namespace {

  class JITGCPass : public ModulePass {
  public:
    static char ID;
    
    JITGCPass() : ModulePass(ID) {}

    virtual bool runOnModule(Module& M);

    /// getAnalysisUsage - We do not modify anything.
    virtual void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.setPreservesAll();
    } 

  };

  char JITGCPass::ID = 0;
  RegisterPass<JITGCPass> X("JITGCPass",
                      "Add GC information in files compiled with llvm-gcc");

bool JITGCPass::runOnModule(Module& M) {

  Function* F = M.getFunction("__llvm_gcroot");
  if (F) {
    Function *gcrootFun = Intrinsic::getDeclaration(&M, Intrinsic::gcroot);

    F->replaceAllUsesWith(gcrootFun);
    F->eraseFromParent();

    for (Value::use_iterator I = gcrootFun->use_begin(),
         E = gcrootFun->use_end(); I != E; ++I) {
      if (Instruction* II = dyn_cast<Instruction>(*I)) {
        Function* F = II->getParent()->getParent();
        if (!F->hasGC()) F->setGC("vmkit");
      }
    }

    return true;
  }

  return false;
}

}
