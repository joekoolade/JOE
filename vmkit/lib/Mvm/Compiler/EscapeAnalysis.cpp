//===------EscapeAnalysis.cpp - Simple LLVM escape analysis ---------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "llvm/Constants.h"
#include "llvm/Function.h"
#include "llvm/GlobalVariable.h"
#include "llvm/Module.h"
#include "llvm/Pass.h"
#include "llvm/Instructions.h"
#include "llvm/Analysis/LoopInfo.h"
#include "llvm/Support/CallSite.h"
#include "llvm/Support/Compiler.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/raw_ostream.h"

#include <cstddef>
#include <map>

#include "mvm/GC/GC.h"

using namespace llvm;

namespace {

  class EscapeAnalysis : public FunctionPass {
  public:
    static char ID;
    uint64_t pageSize;
    EscapeAnalysis() : FunctionPass(ID) {
      pageSize = getpagesize();
    }

    virtual void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.addRequired<LoopInfo>();
    }

    virtual bool runOnFunction(Function &F);

  private:
    bool processMalloc(Instruction* I, Value* Size, Value* VT, Loop* CurLoop);
  };

  char EscapeAnalysis::ID = 0;
  RegisterPass<EscapeAnalysis> X("EscapeAnalysis", "Escape Analysis Pass");

bool EscapeAnalysis::runOnFunction(Function& F) {
  bool Changed = false;
  Function* Allocator = F.getParent()->getFunction("gcmalloc");
  if (!Allocator) return Changed;

  LoopInfo* LI = &getAnalysis<LoopInfo>();

  for (Function::iterator BI = F.begin(), BE = F.end(); BI != BE; BI++) { 
    BasicBlock *Cur = BI;
   
    // Get the parent loop if there is one. If the allocation happens in a loop
    // we must make sure that the allocated value is not used outside of
    // the loop. If the allocation does not escape and it is only used inside
    // the loop, we will hoist the allocation in the pre-header of the loop.
    Loop* CurLoop = LI->getLoopFor(Cur);
    if (CurLoop) {
      Loop* NextLoop = CurLoop->getParentLoop();
      while (NextLoop) {
        CurLoop = NextLoop;
        NextLoop = CurLoop->getParentLoop();
      }
    }

    for (BasicBlock::iterator II = Cur->begin(), IE = Cur->end(); II != IE;) {
      Instruction *I = II;
      II++;
      if (I->getOpcode() != Instruction::Call &&
          I->getOpcode() != Instruction::Invoke) {
        continue;
      }
      CallSite Call(I);
      if (Call.getCalledValue() == Allocator) {
        if (CurLoop) {
          bool escapesLoop = false;
          for (Value::use_iterator U = I->use_begin(), E = I->use_end();
               U != E; ++U) {
            if (Instruction* II = dyn_cast<Instruction>(*U)) {
              BasicBlock* BBU = II->getParent();
              if (!CurLoop->contains(BBU)) {
                escapesLoop = true;
                break;
              }
            }
          }

          if (escapesLoop) continue;
        }

        if (CallInst *CI = dyn_cast<CallInst>(I)) {
          Changed |= processMalloc(CI, CI->getArgOperand(0), CI->getArgOperand(1),
                                   CurLoop);
        } else if (InvokeInst *CI = dyn_cast<InvokeInst>(I)) {
          Changed |= processMalloc(CI, CI->getArgOperand(0), CI->getArgOperand(1),
                                   CurLoop);
        }
      }
    }
  }
  return Changed;
}




static bool escapes(Value* Ins, std::map<Instruction*, bool>& visited) {
  for (Value::use_iterator I = Ins->use_begin(), E = Ins->use_end(); 
       I != E; ++I) {
    if (Instruction* II = dyn_cast<Instruction>(*I)) {
      if (II->getOpcode() == Instruction::Call || 
          II->getOpcode() == Instruction::Invoke) {
        
        CallSite CS(II);
        if (!CS.onlyReadsMemory()) return true;
        
        CallSite::arg_iterator B = CS.arg_begin(), E = CS.arg_end();
        for (CallSite::arg_iterator A = B; A != E; ++A) {
          if (A->get() == Ins && 
              !CS.paramHasAttr(A - B + 1, Attribute::NoCapture)) {
            return true;
          }
        }
       
        // We must also consider the value returned by the function.
        if (II->getType() == Ins->getType()) {
          if (escapes(II, visited)) return true;
        }

      } else if (dyn_cast<BitCastInst>(II)) {
        if (escapes(II, visited)) return true;
      } else if (StoreInst* SI = dyn_cast<StoreInst>(II)) {
        if (AllocaInst * AI = dyn_cast<AllocaInst>(SI->getOperand(1))) {
          if (!visited[AI]) {
            visited[AI] = true;
            if (escapes(AI, visited)) return true;
          }
        } else if (SI->getOperand(0) == Ins) {
          return true;
        }
      } else if (dyn_cast<LoadInst>(II)) {
        if (isa<PointerType>(II->getType())) {
          if (escapes(II, visited)) return true; // allocas
        }
      } else if (dyn_cast<GetElementPtrInst>(II)) {
        if (escapes(II, visited)) return true;
      } else if (dyn_cast<ReturnInst>(II)) {
        return true;
      } else if (dyn_cast<PHINode>(II)) {
        if (!visited[II]) {
          visited[II] = true;
          if (escapes(II, visited)) return true;
        }
      }
    } else {
      return true;
    }
  }
  return false;
}

bool EscapeAnalysis::processMalloc(Instruction* I, Value* Size, Value* VT,
                                   Loop* CurLoop) {
  Instruction* Alloc = I;
  LLVMContext& Context = Alloc->getParent()->getContext();

  ConstantInt* CI = dyn_cast<ConstantInt>(Size);
  bool hasFinalizer = true;
  
  if (CI) {
    if (ConstantExpr* CE = dyn_cast<ConstantExpr>(VT)) {
      if (ConstantInt* C = dyn_cast<ConstantInt>(CE->getOperand(0))) {
        VirtualTable* Table = (VirtualTable*)C->getZExtValue();
        hasFinalizer = (((void**)Table)[0] != 0);
      } else {
        GlobalVariable* GV = dyn_cast<GlobalVariable>(CE->getOperand(0));
        if (GV->hasInitializer()) {
          Constant* Init = GV->getInitializer();
          if (ConstantArray* CA = dyn_cast<ConstantArray>(Init)) {
            Constant* V = CA->getOperand(0);
            hasFinalizer = !V->isNullValue();
          }
        }
      }
    }
  } else {
    return false;
  }

  // The object does not have a finalizer and is never used. Remove the
  // allocation as it will not have side effects.
  if (!hasFinalizer && !Alloc->getNumUses()) {
    DEBUG(errs() << "Escape analysis removes instruction " << *Alloc << ": ");
    Alloc->eraseFromParent();
    return true;
  }
  
  uint64_t NSize = CI->getZExtValue();
  // If the class has a finalize method, do not stack allocate the object.
  if (NSize < pageSize && !hasFinalizer) {
    std::map<Instruction*, bool> visited;
    bool esc = escapes(Alloc, visited);
    if (!esc) {

      if (CurLoop) {
        // The object does not escape and is only used in the loop where it
        // is allocated. We hoist the allocation in the pre-header so that
        // we don't end up with tons of allocations on the stack.
        BasicBlock* BB = CurLoop->getLoopPreheader();
        assert(BB && "No Preheader!");
        DEBUG(errs() << "Escape analysis hoisting to " << BB->getNameStr());
        DEBUG(errs() << ": ");
        DEBUG(errs() << *Alloc);
        Alloc->removeFromParent();
        BB->getInstList().insert(BB->getTerminator(), Alloc);
      }

      AllocaInst* AI = new AllocaInst(Type::getInt8Ty(Context), Size, "",
                                      Alloc);
      BitCastInst* BI = new BitCastInst(AI, Alloc->getType(), "", Alloc);
      DEBUG(errs() << "escape");
      DEBUG(errs() << Alloc->getParent()->getParent()->getNameStr() << "\n");
      Alloc->replaceAllUsesWith(BI);
      // If it's an invoke, replace the invoke with a direct branch.
      if (InvokeInst *CI = dyn_cast<InvokeInst>(Alloc)) {
        BranchInst::Create(CI->getNormalDest(), Alloc);
      }
      Alloc->eraseFromParent();
      return true;
    }
  }
  return false;
}
}

namespace mvm {
FunctionPass* createEscapeAnalysisPass() {
  return new EscapeAnalysis();
}

}
