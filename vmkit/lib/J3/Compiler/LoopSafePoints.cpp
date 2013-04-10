//===------- LoopSafePoints.cpp - Add safe points in loop headers ---------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "llvm/Module.h"
#include "llvm/Analysis/LoopPass.h"
#include "llvm/Support/CallSite.h"
#include "llvm/Support/Compiler.h"

using namespace llvm;

namespace {

  class LoopSafePoints : public LoopPass {
  public:
    static char ID;
    
    LoopSafePoints() : LoopPass(ID) {}

    virtual bool runOnLoop(Loop* L, LPPassManager& LPM);

    virtual void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.addRequired<LoopInfo>();
    }


  private:
    void insertSafePoint(BasicBlock* BB, Function* SafeFunction,
                         Value* YieldPtr, Loop* L, LoopInfo* LI);
  };

  char LoopSafePoints::ID = 0;

void LoopSafePoints::insertSafePoint(BasicBlock* BB, Function* SafeFunction,
                                     Value* YieldPtr, Loop* L, LoopInfo* LI) {
  Instruction* I = BB->getFirstNonPHI();
  BasicBlock* NBB = BB->splitBasicBlock(I);
  L->addBasicBlockToLoop(NBB, LI->getBase());

  NBB = NBB->getSinglePredecessor();
  I = NBB->getTerminator();
  BasicBlock* SU = (static_cast<BranchInst*>(I))->getSuccessor(0);
  I->eraseFromParent();
  
  Value* Ld = new LoadInst(YieldPtr, "", NBB);
  BasicBlock* yield = BasicBlock::Create(SafeFunction->getContext(), "",
                                         BB->getParent());
  
  BranchInst::Create(yield, SU, Ld, NBB);

  CallInst::Create(SafeFunction, "", yield);
  BranchInst::Create(SU, yield);

  L->addBasicBlockToLoop(yield, LI->getBase());
}


bool LoopSafePoints::runOnLoop(Loop* L, LPPassManager& LPM) {

  LoopInfo* LI = &getAnalysis<LoopInfo>();
  BasicBlock* Header = L->getHeader();
  Function *F = Header->getParent();  
  Function* SafeFunction =
    F->getParent()->getFunction("conditionalSafePoint");
  if (!SafeFunction) return false;

  Value* YieldPtr = 0;
  
  // Lookup the yield pointer.
  for (Function::iterator BI = F->begin(), BE = F->end(); BI != BE; BI++) { 
    BasicBlock *Cur = BI;

    for (BasicBlock::iterator II = Cur->begin(), IE = Cur->end(); II != IE;) {
      Instruction *I = II;
      II++;
      if (I->getOpcode() != Instruction::Call &&
          I->getOpcode() != Instruction::Invoke) {
        continue;
      }

      CallSite Call(I);
      if (Call.getCalledValue() == SafeFunction) {
        if (BasicBlock* Incoming = Cur->getSinglePredecessor()) {
          if (BranchInst* T = dyn_cast<BranchInst>(Incoming->getTerminator())) {
            if (LoadInst* LI = dyn_cast<LoadInst>(T->getCondition())) {
              YieldPtr = LI->getPointerOperand();
              break;
            }
          }
        }
      }
    }
    if (YieldPtr) break;
  }

  if (!YieldPtr) return false;

  insertSafePoint(Header, SafeFunction, YieldPtr, L, LI);
  return true;
}

}


namespace mvm {

LoopPass* createLoopSafePointsPass() {
  return new LoopSafePoints();
}

}
