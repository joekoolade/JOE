//===------------- InlineMalloc.cpp - Inline allocations  -----------------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/GlobalVariable.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/Module.h"
#include "llvm/Pass.h"
#include "llvm/Support/CallSite.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Target/TargetData.h"
#include "llvm/Transforms/Utils/Cloning.h"

#include "mvm/JIT.h"

using namespace llvm;

namespace mvm {

  class InlineMalloc : public FunctionPass {
  public:
    static char ID;
    InlineMalloc() : FunctionPass(ID) {}
    virtual const char* getPassName() const {
      return "Inline malloc and barriers";
    }

    virtual bool runOnFunction(Function &F);
  private:
  };
  char InlineMalloc::ID = 0;


bool InlineMalloc::runOnFunction(Function& F) {
  Function* Malloc = F.getParent()->getFunction("gcmalloc");
  Function* FieldWriteBarrier = F.getParent()->getFunction("fieldWriteBarrier");
  Function* ArrayWriteBarrier = F.getParent()->getFunction("arrayWriteBarrier");
  Function* NonHeapWriteBarrier = F.getParent()->getFunction("nonHeapWriteBarrier");
  bool Changed = false;
  const TargetData *TD = getAnalysisIfAvailable<TargetData>();
  for (Function::iterator BI = F.begin(), BE = F.end(); BI != BE; BI++) { 
    BasicBlock *Cur = BI; 
    for (BasicBlock::iterator II = Cur->begin(), IE = Cur->end(); II != IE;) {
      Instruction *I = II;
      II++;
      if (I->getOpcode() != Instruction::Call &&
          I->getOpcode() != Instruction::Invoke) {
        continue;
      }
      CallSite Call(I);
      Function* Temp = Call.getCalledFunction();
      if (Temp == Malloc) {
        if (dyn_cast<Constant>(Call.getArgument(0))) {
          InlineFunctionInfo IFI(NULL, TD);
          Changed |= InlineFunction(Call, IFI);
          break;
        }
      } else if (Temp == FieldWriteBarrier ||
                 Temp == NonHeapWriteBarrier ||
                 Temp == ArrayWriteBarrier) {
        InlineFunctionInfo IFI(NULL, TD);
        Changed |= InlineFunction(Call, IFI);
        break;
      }
    }
  }
  return Changed;
}


FunctionPass* createInlineMallocPass() {
  return new InlineMalloc();
}

}
