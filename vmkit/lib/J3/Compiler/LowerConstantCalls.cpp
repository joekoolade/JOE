//===----- LowerConstantCalls.cpp - Changes arrayLength calls  --------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/GlobalVariable.h"
#include "llvm/Pass.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/Support/CallSite.h"
#include "llvm/Support/Compiler.h"
#include "llvm/Support/Debug.h"

#include "JavaClass.h"
#include "j3/JavaLLVMCompiler.h"
#include "j3/J3Intrinsics.h"

using namespace llvm;

namespace j3 {

class LowerConstantCalls : public FunctionPass {
public:
  static char ID;
  JavaLLVMCompiler* TheCompiler;
  LowerConstantCalls(JavaLLVMCompiler* Compiler) : FunctionPass(ID),
    TheCompiler(Compiler) { }

  const char* getPassName() const { return "Lower Java calls"; }

  virtual bool runOnFunction(Function &F);
private:
};
char LowerConstantCalls::ID = 0;

static Value* getTCM(J3Intrinsics* intrinsics, Value* Arg, Instruction* CI) {
  Value* GEP[2] = { intrinsics->constantZero,
                    intrinsics->OffsetTaskClassMirrorInClassConstant };
  Value* TCMArray = GetElementPtrInst::Create(Arg, GEP, "", CI);
  
  Value* GEP2[2] = { intrinsics->constantZero, intrinsics->constantZero };

  Value* TCM = GetElementPtrInst::Create(TCMArray, GEP2, "", CI);
  return TCM;

}

static Value* getDelegatee(J3Intrinsics* intrinsics, Value* Arg, Instruction* CI) {
  Value* GEP[2] = { intrinsics->constantZero,
                    intrinsics->constantZero };
  Value* TCMArray = GetElementPtrInst::Create(Arg, GEP, "", CI);
  
  Value* GEP2[2] = { intrinsics->constantZero, intrinsics->constantZero };

  Value* TCM = GetElementPtrInst::Create(TCMArray, GEP2, "", CI);
  return new LoadInst(TCM, "", CI);

}

bool LowerConstantCalls::runOnFunction(Function& F) {
  LLVMContext* Context = &F.getContext();
  bool Changed = false;
  J3Intrinsics* intrinsics = TheCompiler->getIntrinsics();
  JavaMethod* meth = TheCompiler->getJavaMethod(F);
  assert(meth && "Method not registered");
  for (Function::iterator BI = F.begin(), BE = F.end(); BI != BE; BI++) { 
    BasicBlock *Cur = BI; 
    for (BasicBlock::iterator II = Cur->begin(), IE = Cur->end(); II != IE;) {
      Instruction *I = II;
      II++;

      if (ICmpInst* Cmp = dyn_cast<ICmpInst>(I)) {
        if (Cmp->getOperand(1) == intrinsics->JavaObjectNullConstant) {
          Value* Arg = Cmp->getOperand(0);
          if (isVirtual(meth->access) && Arg == F.arg_begin()) {
            Changed = true;
            Cmp->replaceAllUsesWith(ConstantInt::getFalse(*Context));
            Cmp->eraseFromParent();
            break;
          }
         
          Instruction* InsArg = dyn_cast<Instruction>(Arg); 
          if (InsArg != NULL &&
              (InsArg->getOpcode() == Instruction::Call ||
               InsArg->getOpcode() == Instruction::Invoke)) { 
            CallSite Ca(Arg);
            if (Ca.getCalledValue() == intrinsics->AllocateFunction) {
              Changed = true;
              Cmp->replaceAllUsesWith(ConstantInt::getFalse(*Context));
              Cmp->eraseFromParent();
              break;
            }
          }
        }
      }
     
      // Remove useless Alloca's, usually used for stacks or temporary values.
      // The optimizers may have rendered them useless.
      if (AllocaInst* AI = dyn_cast<AllocaInst>(I)) {
        bool ToDelete = true;
        for (Value::use_iterator UI = AI->use_begin(), UE = AI->use_end();
             UI != UE; ++UI) {
          if (dyn_cast<StoreInst>(*UI)) continue;
          if (BitCastInst* BI = dyn_cast<BitCastInst>(*UI)) {
            if (BI->hasOneUse()) {
              Instruction* use = dyn_cast<Instruction>(*(BI->use_begin()));
              if (use != NULL &&
                  (use->getOpcode() == Instruction::Call ||
                   use->getOpcode() == Instruction::Invoke)) { 
                CallSite Call(use);
                if (Call.getCalledFunction() == intrinsics->llvm_gc_gcroot) {
                  continue;
                }
              }
            }
          }
          
          ToDelete = false;
          break;
        }
        
        if (ToDelete) {
          Changed = true;
          for (Value::use_iterator UI = AI->use_begin(), UE = AI->use_end();
               UI != UE;) {
            Value* Temp = *UI;
            ++UI;
            if (StoreInst* SI = dyn_cast<StoreInst>(Temp)) {
              if (dyn_cast<Instruction>(II) == SI) ++II;
              SI->eraseFromParent();
            } else if (BitCastInst* BI = dyn_cast<BitCastInst>(Temp)) {
              CallSite Call(*(BI->use_begin()));
              Instruction* CI = Call.getInstruction();
              if (dyn_cast<Instruction>(II) == CI) ++II;
              CI->eraseFromParent();
              if (dyn_cast<Instruction>(II) == BI) ++II;
              BI->eraseFromParent();
            }
          }
          AI->eraseFromParent();
        }
        continue;
      }

      if ((I->getOpcode() == Instruction::Call ||
           I->getOpcode() == Instruction::Invoke)) { 
        Instruction* CI = I;
        CallSite Call(I);
        Value* V = Call.getCalledValue();
        if (V == intrinsics->ArrayLengthFunction) {
          Changed = true;
          Value* val = Call.getArgument(0); // get the array
          Value* array = new BitCastInst(val, intrinsics->JavaArrayType,
                                         "", CI);
          Value* args[2] = { intrinsics->constantZero, 
                             intrinsics->JavaArraySizeOffsetConstant };
          Value* ptr = GetElementPtrInst::Create(array, args, "", CI);
          Value* load = new LoadInst(ptr, "", CI);
          load = new PtrToIntInst(load, Type::getInt32Ty(*Context), "", CI);
          CI->replaceAllUsesWith(load);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetVTFunction) {
          Changed = true;
          Value* val = Call.getArgument(0); // get the object
          Value* indexes[2] = { intrinsics->constantZero, intrinsics->constantZero };
          Value* VTPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* VT = new LoadInst(VTPtr, "", CI);
          CI->replaceAllUsesWith(VT);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetIMTFunction) {
          Changed = true;
          Value* val = Call.getArgument(0); // get the VT
          Value* indexes[2] = { intrinsics->constantZero,
                                intrinsics->OffsetIMTInVTConstant };
          Value* IMTPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* IMT = new LoadInst(IMTPtr, "", CI);
          IMT = new BitCastInst(IMT, CI->getType(), "", CI);
          CI->replaceAllUsesWith(IMT);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetClassFunction) {
          Changed = true;
          Value* val = Call.getArgument(0); // get the object
          Value* args2[2] = { intrinsics->constantZero,
                              intrinsics->JavaObjectVTOffsetConstant };
          Value* VTPtr = GetElementPtrInst::Create(val, args2, "", CI);
          Value* VT = new LoadInst(VTPtr, "", CI);
          Value* args3[2] = { intrinsics->constantZero,
                              intrinsics->OffsetClassInVTConstant };

          Value* clPtr = GetElementPtrInst::Create(VT, args3, "", CI);
          Value* cl = new LoadInst(clPtr, "", CI);
          cl = new BitCastInst(cl, intrinsics->JavaCommonClassType, "", CI);

          CI->replaceAllUsesWith(cl);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetVTFromClassFunction) {
          Changed = true;
          
          Value* val = Call.getArgument(0);
          Value* indexes[3] = { intrinsics->constantZero, 
                                intrinsics->constantZero, 
                                intrinsics->OffsetVTInClassConstant };
          Value* VTPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* VT = new LoadInst(VTPtr, "", CI);
          CI->replaceAllUsesWith(VT);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetVTFromCommonClassFunction) {
          Changed = true;
          
          Value* val = Call.getArgument(0);
          Value* indexes[2] = { intrinsics->constantZero, 
                                intrinsics->OffsetVTInClassConstant };
          Value* VTPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* VT = new LoadInst(VTPtr, "", CI);
          CI->replaceAllUsesWith(VT);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetVTFromClassArrayFunction) {
          Changed = true;
          
          Value* val = Call.getArgument(0);
          Value* indexes[3] = { intrinsics->constantZero,
                                intrinsics->constantZero,
                                intrinsics->OffsetVTInClassConstant };
          Value* VTPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* VT = new LoadInst(VTPtr, "", CI);
          CI->replaceAllUsesWith(VT);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetBaseClassVTFromVTFunction) {
          Changed = true;
          
          Value* val = Call.getArgument(0);
          Value* indexes[2] = { intrinsics->constantZero,
                                intrinsics->OffsetBaseClassVTInVTConstant };
          Value* VTPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* VT = new LoadInst(VTPtr, "", CI);
          VT = new BitCastInst(VT, intrinsics->VTType, "", CI);
          CI->replaceAllUsesWith(VT);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetObjectSizeFromClassFunction) {
          Changed = true;
          
          Value* val = Call.getArgument(0); 
          Value* indexes[2] = { intrinsics->constantZero, 
                                intrinsics->OffsetObjectSizeInClassConstant };
          Value* SizePtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* Size = new LoadInst(SizePtr, "", CI);
          CI->replaceAllUsesWith(Size);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetDepthFunction) {
          Changed = true;
          Value* val = Call.getArgument(0); 
          Value* indexes[2] = { intrinsics->constantZero,
                                intrinsics->OffsetDepthInVTConstant };
          Value* DepthPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Value* Depth = new LoadInst(DepthPtr, "", CI);
          Depth = new PtrToIntInst(Depth, Type::getInt32Ty(*Context), "", CI);
          CI->replaceAllUsesWith(Depth);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetDisplayFunction) {
          Changed = true;
          Value* val = Call.getArgument(0);
          Value* indexes[2] = { intrinsics->constantZero,
                                intrinsics->OffsetDisplayInVTConstant };
          Value* DisplayPtr = GetElementPtrInst::Create(val, indexes, "", CI);
          Type* Ty = PointerType::getUnqual(intrinsics->VTType);
          DisplayPtr = new BitCastInst(DisplayPtr, Ty, "", CI);
          CI->replaceAllUsesWith(DisplayPtr);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetVTInDisplayFunction) {
          Changed = true;
          Value* val = Call.getArgument(0);
          Value* depth = Call.getArgument(1);
          Value* ClassPtr = GetElementPtrInst::Create(val, depth, "", CI);
          Value* Class = new LoadInst(ClassPtr, "", CI);
          CI->replaceAllUsesWith(Class);
          CI->eraseFromParent();
        } else if (V == intrinsics->GetClassDelegateeFunction) {
          Changed = true;
          BasicBlock* NBB = II->getParent()->splitBasicBlock(II);
          I->getParent()->getTerminator()->eraseFromParent();
          Value* Del = getDelegatee(intrinsics, Call.getArgument(0), CI);
          Value* cmp = new ICmpInst(CI, ICmpInst::ICMP_EQ, Del, 
                                    intrinsics->JavaObjectNullConstant, "");
          
          BasicBlock* NoDelegatee = BasicBlock::Create(*Context, "No delegatee", &F);
          BasicBlock* DelegateeOK = BasicBlock::Create(*Context, "Delegatee OK", &F);
          BranchInst::Create(NoDelegatee, DelegateeOK, cmp, CI);
          PHINode* phi = PHINode::Create(intrinsics->JavaObjectType, 2, "", DelegateeOK);
          phi->addIncoming(Del, CI->getParent());
          
          Instruction* Res = CallInst::Create(intrinsics->RuntimeDelegateeFunction,
                                              Call.getArgument(0), "", NoDelegatee);
          Res->setDebugLoc(CI->getDebugLoc());
          BranchInst::Create(DelegateeOK, NoDelegatee);
          phi->addIncoming(Res, NoDelegatee);

          CI->replaceAllUsesWith(phi);
          CI->eraseFromParent();
          BranchInst::Create(NBB, DelegateeOK);
          break;
         
        } else if (V == intrinsics->InitialisationCheckFunction) {
          Changed = true;
          
          BasicBlock* NBB = 0;
          if (CI->getParent()->getTerminator() != CI) {
            NBB = II->getParent()->splitBasicBlock(II);
            CI->getParent()->getTerminator()->eraseFromParent();
          } else {
            InvokeInst* Invoke = dyn_cast<InvokeInst>(CI);
            assert(Invoke && "Last instruction is not an invoke");
            NBB = Invoke->getNormalDest();
          }
         
          Value* Cl = Call.getArgument(0); 
          Value* TCM = getTCM(intrinsics, Call.getArgument(0), CI);
          Value* GEP[2] = 
            { intrinsics->constantZero,
              intrinsics->OffsetInitializedInTaskClassMirrorConstant };
          Value* StatusPtr = GetElementPtrInst::Create(TCM, GEP, "", CI);
          
          Value* test = new LoadInst(StatusPtr, "", CI);
          
          BasicBlock* trueCl = BasicBlock::Create(*Context, "Initialized", &F);
          BasicBlock* falseCl = BasicBlock::Create(*Context, "Uninitialized", &F);
          PHINode* node = llvm::PHINode::Create(intrinsics->JavaClassType, 2, "", trueCl);
          node->addIncoming(Cl, CI->getParent());
          BranchInst::Create(trueCl, falseCl, test, CI);
  
          
          Instruction* res = 0;
          if (InvokeInst* Invoke = dyn_cast<InvokeInst>(CI)) {
            Value* Args[1] = { Cl };
            BasicBlock* UI = Invoke->getUnwindDest();

            res = InvokeInst::Create(intrinsics->InitialiseClassFunction,
                                     trueCl, UI, Args, "", falseCl);

            // For some reason, an LLVM pass may add PHI nodes to the
            // exception destination.
            BasicBlock::iterator Temp = UI->getInstList().begin();
            while (PHINode* PHI = dyn_cast<PHINode>(Temp)) {
              Value* Val = PHI->getIncomingValueForBlock(CI->getParent());
              PHI->removeIncomingValue(CI->getParent(), false);
              PHI->addIncoming(Val, falseCl);
              Temp++;
            }
            
            // And here we set the phi nodes of the normal dest of the Invoke
            // instruction. The phi nodes have now the trueCl as basic block.
            Temp = NBB->getInstList().begin();
            while (PHINode* PHI = dyn_cast<PHINode>(Temp)) {
              Value* Val = PHI->getIncomingValueForBlock(CI->getParent());
              PHI->removeIncomingValue(CI->getParent(), false);
              PHI->addIncoming(Val, trueCl);
              Temp++;
            }

          } else {
            res = CallInst::Create(intrinsics->InitialiseClassFunction,
                                   Cl, "", falseCl);
            BranchInst::Create(trueCl, falseCl);
          }
          res->setDebugLoc(CI->getDebugLoc());
          
          node->addIncoming(res, falseCl);


          CI->replaceAllUsesWith(node);
          CI->eraseFromParent();
          BranchInst::Create(NBB, trueCl);
          break;
        } else if (V == intrinsics->GetConstantPoolAtFunction) {
          Function* resolver = dyn_cast<Function>(Call.getArgument(0));
          assert(resolver && "Wrong use of GetConstantPoolAt");
          Type* returnType = resolver->getReturnType();
          Value* CTP = Call.getArgument(1);
          Value* Index = Call.getArgument(3);
          Changed = true;
          BasicBlock* NBB = 0;
          if (CI->getParent()->getTerminator() != CI) {
            NBB = II->getParent()->splitBasicBlock(II);
            CI->getParent()->getTerminator()->eraseFromParent();
          } else {
            InvokeInst* Invoke = dyn_cast<InvokeInst>(CI);
            assert(Invoke && "Last instruction is not an invoke");
            NBB = Invoke->getNormalDest();
          }
          
          Value* indexes = Index;
          Value* arg1 = GetElementPtrInst::Create(CTP, indexes, "", CI);
          arg1 = new LoadInst(arg1, "", false, CI);
          Value* test = new ICmpInst(CI, ICmpInst::ICMP_EQ, arg1,
                                     intrinsics->constantPtrNull, "");
 
          BasicBlock* trueCl = BasicBlock::Create(*Context, "Ctp OK", &F);
          BasicBlock* falseCl = BasicBlock::Create(*Context, "Ctp Not OK", &F);
          PHINode* node = llvm::PHINode::Create(returnType, 2, "", trueCl);
          node->addIncoming(arg1, CI->getParent());
          BranchInst::Create(falseCl, trueCl, test, CI);
  
          std::vector<Value*> Args;
          unsigned ArgSize = Call.arg_size(), i = 1;
          while (++i < ArgSize) {
            Args.push_back(Call.getArgument(i));
          }
          
          Instruction* res = 0;
          if (InvokeInst* Invoke = dyn_cast<InvokeInst>(CI)) {
            BasicBlock* UI = Invoke->getUnwindDest();
            res = InvokeInst::Create(resolver, trueCl, UI, Args, "", falseCl);

            // For some reason, an LLVM pass may add PHI nodes to the
            // exception destination.
            BasicBlock::iterator Temp = UI->getInstList().begin();
            while (PHINode* PHI = dyn_cast<PHINode>(Temp)) {
              Value* Val = PHI->getIncomingValueForBlock(CI->getParent());
              PHI->removeIncomingValue(CI->getParent(), false);
              PHI->addIncoming(Val, falseCl);
              Temp++;
            }

            // And here we set the phi nodes of the normal dest of the Invoke
            // instruction. The phi nodes have now the trueCl as basic block.
            Temp = NBB->getInstList().begin();
            while (PHINode* PHI = dyn_cast<PHINode>(Temp)) {
              Value* Val = PHI->getIncomingValueForBlock(CI->getParent());
              PHI->removeIncomingValue(CI->getParent(), false);
              PHI->addIncoming(Val, trueCl);
              Temp++;
            }

          } else {
            res = CallInst::Create(resolver, Args, "", falseCl);
            BranchInst::Create(trueCl, falseCl);
          }
          
          res->setDebugLoc(CI->getDebugLoc());
          node->addIncoming(res, falseCl);

          CI->replaceAllUsesWith(node);
          CI->eraseFromParent();
          BranchInst::Create(NBB, trueCl);
          break;
        } else if (V == intrinsics->GetArrayClassFunction) {
          Type* Ty = PointerType::getUnqual(intrinsics->VTType);
          Constant* nullValue = Constant::getNullValue(Ty);
          // Check if we have already proceed this call.
          if (Call.getArgument(2) == nullValue) { 
            BasicBlock* NBB = II->getParent()->splitBasicBlock(II);
            I->getParent()->getTerminator()->eraseFromParent();

            Constant* init = Constant::getNullValue(intrinsics->VTType);
            GlobalVariable* GV = 
              new GlobalVariable(*(F.getParent()), intrinsics->VTType,
                                 false, GlobalValue::ExternalLinkage,
                                 init, "");

            Value* LoadedGV = new LoadInst(GV, "", CI);
            Value* cmp = new ICmpInst(CI, ICmpInst::ICMP_EQ, LoadedGV, init,
                                      "");

            BasicBlock* OKBlock = BasicBlock::Create(*Context, "", &F);
            BasicBlock* NotOKBlock = BasicBlock::Create(*Context, "", &F);
            PHINode* node = PHINode::Create(intrinsics->VTType, 2, "",
                                            OKBlock);
            node->addIncoming(LoadedGV, CI->getParent());

            BranchInst::Create(NotOKBlock, OKBlock, cmp, CI);

            Value* args[3] = { Call.getArgument(0), Call.getArgument(1), GV };
            Instruction* res = CallInst::Create(intrinsics->GetArrayClassFunction, args,
                                                "", NotOKBlock);
            res->setDebugLoc(CI->getDebugLoc());
            BranchInst::Create(OKBlock, NotOKBlock);
            node->addIncoming(res, NotOKBlock);
            
            CI->replaceAllUsesWith(node);
            CI->eraseFromParent();
            BranchInst::Create(NBB, OKBlock);
            Changed = true;
            break;
          }
        } else if (V == intrinsics->ForceInitialisationCheckFunction ||
                   V == intrinsics->ForceLoadedCheckFunction ) {
          Changed = true;
          CI->eraseFromParent();
        } else if (V == intrinsics->GetFinalInt8FieldFunction ||
                   V == intrinsics->GetFinalInt16FieldFunction ||
                   V == intrinsics->GetFinalInt32FieldFunction ||
                   V == intrinsics->GetFinalLongFieldFunction ||
                   V == intrinsics->GetFinalFloatFieldFunction ||
                   V == intrinsics->GetFinalDoubleFieldFunction) {
          Changed = true;
          Value* val = Call.getArgument(0);
          Value* res = new LoadInst(val, "", CI);
          CI->replaceAllUsesWith(res);
          CI->eraseFromParent();
        } else if (V == intrinsics->IsAssignableFromFunction) {
          Changed = true;
          Value* VT1 = Call.getArgument(0);
          Value* VT2 = Call.getArgument(1);
          
          BasicBlock* EndBlock = II->getParent()->splitBasicBlock(II);
          I->getParent()->getTerminator()->eraseFromParent();
          
          BasicBlock* CurEndBlock = BasicBlock::Create(*Context, "", &F);
          BasicBlock* FailedBlock = BasicBlock::Create(*Context, "", &F);
          PHINode* node = PHINode::Create(Type::getInt1Ty(*Context), 2, "", CurEndBlock);

          ConstantInt* CC = ConstantInt::get(Type::getInt32Ty(*Context),
              JavaVirtualTable::getOffsetIndex());
          Value* indices[2] = { intrinsics->constantZero, CC };
          Value* Offset = GetElementPtrInst::Create(VT2, indices, "", CI);
          Offset = new LoadInst(Offset, "", false, CI);
          Offset = new PtrToIntInst(Offset, Type::getInt32Ty(*Context), "", CI);
          indices[1] = Offset;
          Value* CurVT = GetElementPtrInst::Create(VT1, indices, "", CI);
          CurVT = new LoadInst(CurVT, "", false, CI);
          CurVT = new BitCastInst(CurVT, intrinsics->VTType, "", CI);
             
          Instruction* res =
            new ICmpInst(CI, ICmpInst::ICMP_EQ, CurVT, VT2, "");

          node->addIncoming(ConstantInt::getTrue(*Context), CI->getParent());
          BranchInst::Create(CurEndBlock, FailedBlock, res, CI);

          Value* Args[2] = { VT1, VT2 };
          res = CallInst::Create(intrinsics->IsSecondaryClassFunction, Args,
                                 "", FailedBlock);
          res->setDebugLoc(CI->getDebugLoc());
         
          node->addIncoming(res, FailedBlock);
          BranchInst::Create(CurEndBlock, FailedBlock);

          // Branch to the next block.
          BranchInst::Create(EndBlock, CurEndBlock);
          
          // We can now replace the previous instruction.
          CI->replaceAllUsesWith(node);
          CI->eraseFromParent();
          
          // Reanalyse the current block.
          break;

        } else if (V == intrinsics->IsSecondaryClassFunction) {
          Changed = true;
          Value* VT1 = Call.getArgument(0);
          Value* VT2 = Call.getArgument(1);
            
          BasicBlock* EndBlock = II->getParent()->splitBasicBlock(II);
          I->getParent()->getTerminator()->eraseFromParent();


          BasicBlock* Preheader = BasicBlock::Create(*Context, "preheader", &F);
          BasicBlock* BB4 = BasicBlock::Create(*Context, "BB4", &F);
          BasicBlock* BB5 = BasicBlock::Create(*Context, "BB5", &F);
          BasicBlock* BB6 = BasicBlock::Create(*Context, "BB6", &F);
          BasicBlock* BB7 = BasicBlock::Create(*Context, "BB7", &F);
          BasicBlock* BB9 = BasicBlock::Create(*Context, "BB9", &F);
          Type* Ty = PointerType::getUnqual(intrinsics->VTType);
          
          PHINode* resFwd = PHINode::Create(Type::getInt32Ty(*Context), 2, "", BB7);
   
          // This corresponds to:
          //    if (VT1.cache == VT2 || VT1 == VT2) goto end with true;
          //    else goto headerLoop;
          ConstantInt* cacheIndex = 
            ConstantInt::get(Type::getInt32Ty(*Context), JavaVirtualTable::getCacheIndex());
          Value* indices[2] = { intrinsics->constantZero, cacheIndex };
          Instruction* CachePtr = GetElementPtrInst::Create(VT1, indices, "", CI);
          CachePtr = new BitCastInst(CachePtr, Ty, "", CI);
          Value* Cache = new LoadInst(CachePtr, "", false, CI);
          ICmpInst* cmp1 = new ICmpInst(CI, ICmpInst::ICMP_EQ, Cache, VT2, "");
          ICmpInst* cmp2 = new ICmpInst(CI, ICmpInst::ICMP_EQ, VT1, VT2, "");
          BinaryOperator* Or = BinaryOperator::Create(Instruction::Or, cmp1,
                                                      cmp2, "", CI);
          BranchInst::Create(BB9, Preheader, Or, CI);
    
          // First test failed. Go into the loop. The Preheader looks like this:
          // headerLoop:
          //    types = VT1->secondaryTypes;
          //    size = VT1->nbSecondaryTypes;
          //    i = 0;
          //    goto test;
          ConstantInt* sizeIndex = ConstantInt::get(Type::getInt32Ty(*Context), 
              JavaVirtualTable::getNumSecondaryTypesIndex());
          indices[1] = sizeIndex;
          Instruction* Size = GetElementPtrInst::Create(VT1, indices, "", Preheader);
          Size = new LoadInst(Size, "", false, Preheader);
          Size = new PtrToIntInst(Size, Type::getInt32Ty(*Context), "", Preheader);
    
          ConstantInt* secondaryTypesIndex = ConstantInt::get(Type::getInt32Ty(*Context), 
              JavaVirtualTable::getSecondaryTypesIndex());
          indices[1] = secondaryTypesIndex;
          Instruction* secondaryTypes = 
            GetElementPtrInst::Create(VT1, indices, "", Preheader);
          secondaryTypes = new LoadInst(secondaryTypes, "", false, Preheader);
          secondaryTypes = new BitCastInst(secondaryTypes, Ty, "", Preheader);
          BranchInst::Create(BB7, Preheader);
    
          // Here is the test if the current secondary type is VT2.
          // test:
          //   CurVT = types[i];
          //   if (CurVT == VT2) goto update cache;
          //   est goto inc;
          Instruction* CurVT = GetElementPtrInst::Create(secondaryTypes, resFwd,
                                                         "", BB4);
          CurVT = new LoadInst(CurVT, "", false, BB4);
          cmp1 = new ICmpInst(*BB4, ICmpInst::ICMP_EQ, CurVT, VT2, "");
          BranchInst::Create(BB5, BB6, cmp1, BB4);
    
          // Increment i if the previous test failed
          // inc:
          //    ++i;
          //    goto endLoopTest;
          BinaryOperator* IndVar = 
            BinaryOperator::CreateAdd(resFwd, intrinsics->constantOne, "", BB6);
          BranchInst::Create(BB7, BB6);
    
          // Verify that we haven't reached the end of the loop:
          // endLoopTest:
          //    if (i < size) goto test
          //    else goto end with false
          resFwd->addIncoming(intrinsics->constantZero, Preheader);
          resFwd->addIncoming(IndVar, BB6);
    
          cmp1 = new ICmpInst(*BB7, ICmpInst::ICMP_SGT, Size, resFwd, "");
          BranchInst::Create(BB4, BB9, cmp1, BB7);
   
          // Update the cache if the result is found.
          // updateCache:
          //    VT1->cache = result
          //    goto end with true
          new StoreInst(VT2, CachePtr, false, BB5);
          BranchInst::Create(BB9, BB5);

          // Final block, that gets the result.
          PHINode* node = PHINode::Create(Type::getInt1Ty(*Context), 3, "", BB9);
          node->addIncoming(ConstantInt::getTrue(*Context), CI->getParent());
          node->addIncoming(ConstantInt::getFalse(*Context), BB7);
          node->addIncoming(ConstantInt::getTrue(*Context), BB5);
    
          // Don't forget to jump to the next block.
          BranchInst::Create(EndBlock, BB9);
   
          // We can now replace the previous instruction
          CI->replaceAllUsesWith(node);
          CI->eraseFromParent();

          // And reanalyse the current block.
          break;
        }
      }
    }
  }
  return Changed;
}


FunctionPass* createLowerConstantCallsPass(JavaLLVMCompiler* Compiler) {
  return new LowerConstantCalls(Compiler);
}

}
