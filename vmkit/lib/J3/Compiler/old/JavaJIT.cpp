//===----------- JavaJIT.cpp - Java just in time compiler -----------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#define DEBUG 0
#define JNJVM_COMPILE 0
#define JNJVM_EXECUTE 0

#include <cstring>

#include <llvm/Constants.h>
#include <llvm/DerivedTypes.h>
#include <llvm/Function.h>
#include <llvm/Instructions.h>
#include <llvm/Module.h>
#include <llvm/Type.h>
#include <llvm/Analysis/DebugInfo.h>
#include "llvm/Analysis/DIBuilder.h"
#include <llvm/Support/CFG.h>

#include "mvm/JIT.h"

#include "debug.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaConstantPool.h"
#include "JavaObject.h"
#include "JavaJIT.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "Reader.h"

#include "j3/JavaLLVMCompiler.h"
#include "j3/J3Intrinsics.h"

using namespace j3;
using namespace llvm;

void JavaJIT::updateStackInfo(Opinfo& info) {
  if (stackSize()) {
    if (!info.stack.size()) {
      info.stack = stack;
    } else {
      int size = stack.size();
      info.stack.clear();
      for (int i = 0 ; i < size; i++) {
        info.stack.push_back(MetaInfo(stack[i].type, NOP));
      }
    }
  }
}

bool JavaJIT::needsInitialisationCheck(Class* cl) {
  if (cl->isReadyForCompilation() || 
      (!cl->isInterface() && compilingClass->isAssignableFrom(cl))) {
    return false;
  }

  if (!cl->needsInitialisationCheck()) {
    if (!cl->isReady()) {
      cl->setInitializationState(ready);
    }
    return false;
  }

  return true;
}

bool JavaJIT::canBeInlined(JavaMethod* meth, bool customizing) {
  if (inlineMethods[meth]) return false;
  if (isSynchro(meth->access)) return false;
  if (isNative(meth->access)) return false;

  Attribut* codeAtt = meth->lookupAttribut(Attribut::codeAttribut);
  if (codeAtt == NULL) return false;

  Reader reader(codeAtt, meth->classDef->bytes);
  /* uint16 maxStack = */ reader.readU2();
  /* uint16 maxLocals = */ reader.readU2();
  uint32 codeLen = reader.readU4();
  uint32 start = reader.cursor; 
  reader.seek(codeLen, Reader::SeekCur);
  uint16 handlers = reader.readU2();
  if (handlers != 0) return false;
  reader.cursor = start;

  JavaJIT jit(TheCompiler, meth, llvmFunction, customizing ? customizeFor : NULL);
  jit.inlineMethods = inlineMethods;
  jit.inlineMethods[meth] = true;
  if (!jit.analyzeForInlining(reader, codeLen)) return false;
  jit.inlineMethods[meth] = false;
  return true;
}

bool JavaJIT::isThisReference(int stackIndex) {
  return !overridesThis
      && (stack[stackIndex].bytecode == ALOAD_0)
      && !isStatic(compilingMethod->access);
}

void JavaJIT::invokeVirtual(uint16 index) {
  
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  CommonClass* cl = 0;
  JavaMethod* meth = 0;
  ctpInfo->infoOfMethod(index, ACC_VIRTUAL, cl, meth);
  bool canBeDirect = false;
  Value* val = NULL;  // The return from the method.
  const UTF8* name = 0;
  Signdef* signature = ctpInfo->infoOfInterfaceOrVirtualMethod(index, name);

  bool customized = false;
  bool thisReference =
    isThisReference(stackSize() - signature->getNumberOfSlots() - 1);
  if (thisReference) {
    assert(meth != NULL);
    isCustomizable = true;
    if (customizeFor != NULL) {
      meth = customizeFor->lookupMethodDontThrow(
          meth->name, meth->type, false, true, NULL);
      assert(meth);
      canBeDirect = true;
      customized = true;
      assert(!meth->classDef->isInterface());
      assert(!isAbstract(meth->access));
    }
  }
 
  if ((cl && isFinal(cl->access)) || 
      (meth && (isFinal(meth->access) || isPrivate(meth->access)))) {
    canBeDirect = true;
  }

  if (meth && isInterface(meth->classDef->access)) {
    // This can happen because we compute miranda methods before resolving
    // interfaces.
		return invokeInterface(index);
	}
 
 
  if (TheCompiler->isStaticCompiling()) {
    Value* obj = objectStack[stack.size() - signature->nbArguments - 1];
    JavaObject* source = TheCompiler->getFinalObject(obj);
    if (source) {
      canBeDirect = true;
      CommonClass* sourceClass = JavaObject::getClass(source);
      Class* lookup = sourceClass->isArray() ? sourceClass->super :
                                               sourceClass->asClass();
      meth = lookup->lookupMethodDontThrow(name, signature->keyName, false,
                                           true, 0);
    }
    CommonClass* unique = TheCompiler->getUniqueBaseClass(cl);
    if (unique) {
      canBeDirect = true;
      Class* lookup = unique->isArray() ? unique->super : unique->asClass();
      meth = lookup->lookupMethodDontThrow(name, signature->keyName, false,
                                           true, 0);
    }
  }
 
  Typedef* retTypedef = signature->getReturnType();
  std::vector<Value*> args; // size = [signature->nbIn + 3];
  LLVMSignatureInfo* LSI = TheCompiler->getSignatureInfo(signature);
  llvm::FunctionType* virtualType = LSI->getVirtualType();
  FunctionType::param_iterator it  = virtualType->param_end();
  llvm::Type* retType = virtualType->getReturnType();

  bool needsInit = false;
  if (canBeDirect && canBeInlined(meth, customized)) {
    makeArgs(it, index, args, signature->nbArguments + 1);
    if (!thisReference) JITVerifyNull(args[0]);
    val = invokeInline(meth, args, customized);
  } else if (canBeDirect &&
      !TheCompiler->needsCallback(meth, customized ? customizeFor : NULL, &needsInit)) {
    makeArgs(it, index, args, signature->nbArguments + 1);
    if (!thisReference) JITVerifyNull(args[0]);
    val = invoke(TheCompiler->getMethod(meth, customized ? customizeFor : NULL),
                 args, "", currentBlock);
  } else {

    BasicBlock* endBlock = 0;
    PHINode* node = 0;
    Value* indexes2[2];
    indexes2[0] = intrinsics->constantZero;
    bool nullChecked = false;

    if (meth) {
      LLVMMethodInfo* LMI = TheCompiler->getMethodInfo(meth);
      Constant* Offset = LMI->getOffset();
      indexes2[1] = Offset;
    } else {
      nullChecked = true;
      GlobalVariable* GV = new GlobalVariable(*llvmFunction->getParent(),
                                              Type::getInt32Ty(*llvmContext),
                                              false,
                                              GlobalValue::ExternalLinkage,
                                              intrinsics->constantZero, "");
    
      BasicBlock* resolveVirtual = createBasicBlock("resolveVirtual");
      BasicBlock* endResolveVirtual = createBasicBlock("endResolveVirtual");
      PHINode* node = PHINode::Create(Type::getInt32Ty(*llvmContext), 2, "",
                                      endResolveVirtual);

      Value* load = new LoadInst(GV, "", false, currentBlock);
      Value* test = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, load,
                                 intrinsics->constantZero, "");
      BranchInst::Create(resolveVirtual, endResolveVirtual, test, currentBlock);
      node->addIncoming(load, currentBlock);
      currentBlock = resolveVirtual;
      std::vector<Value*> Args;
      Args.push_back(TheCompiler->getNativeClass(compilingClass));
      Args.push_back(ConstantInt::get(Type::getInt32Ty(*llvmContext), index));
      Args.push_back(GV);
      Value* targetObject = getTarget(signature);
      targetObject = new LoadInst(targetObject, "", false, currentBlock);
      if (!thisReference) JITVerifyNull(targetObject);
      Args.push_back(targetObject);
      load = invoke(intrinsics->VirtualLookupFunction, Args, "", currentBlock);
      node->addIncoming(load, currentBlock);
      BranchInst::Create(endResolveVirtual, currentBlock);
      currentBlock = endResolveVirtual;

      indexes2[1] = node;
    }

    makeArgs(it, index, args, signature->nbArguments + 1);
    if (!nullChecked && !thisReference) JITVerifyNull(args[0]);
    Value* VT = CallInst::Create(intrinsics->GetVTFunction, args[0], "",
                                 currentBlock);
 
    Value* FuncPtr = GetElementPtrInst::Create(VT, indexes2, "", currentBlock);
    
    Value* Func = new LoadInst(FuncPtr, "", currentBlock);
  
    Func = new BitCastInst(Func, LSI->getVirtualPtrType(), "", currentBlock);
    val = invoke(Func, args, "", currentBlock);
  
    if (endBlock) {
      if (node) {
        node->addIncoming(val, currentBlock);
        val = node;
      }
      BranchInst::Create(endBlock, currentBlock);
      currentBlock = endBlock;
    }
  }

  if (retType != Type::getVoidTy(*llvmContext)) {
    if (retType == intrinsics->JavaObjectType) {
      JnjvmClassLoader* JCL = compilingClass->classLoader;
      push(val, false, signature->getReturnType()->findAssocClass(JCL));
    } else {
      push(val, retTypedef->isUnsigned());
      if (retType == Type::getDoubleTy(*llvmContext) || retType == Type::getInt64Ty(*llvmContext)) {
        push(intrinsics->constantZero, false);
      }
    }
  }
}

llvm::Value* JavaJIT::getMutatorThreadPtr() {
  Value* FrameAddr = CallInst::Create(intrinsics->llvm_frameaddress,
                                     	intrinsics->constantZero, "", currentBlock);
  Value* threadId = new PtrToIntInst(FrameAddr, intrinsics->pointerSizeType, "",
                              			 currentBlock);
  threadId = BinaryOperator::CreateAnd(threadId, intrinsics->constantThreadIDMask,
                                       "", currentBlock);
  threadId = new IntToPtrInst(threadId, intrinsics->MutatorThreadType, "", currentBlock);

  return threadId;
}

llvm::Value* JavaJIT::getJavaThreadPtr(llvm::Value* mutatorThreadPtr) {
  return new BitCastInst(mutatorThreadPtr, intrinsics->JavaThreadType, "", currentBlock);
}

llvm::Value* JavaJIT::getIsolateIDPtr(llvm::Value* mutatorThreadPtr) { 
	Value* GEP[3] = { intrinsics->constantZero,
										intrinsics->OffsetThreadInMutatorThreadConstant,
										intrinsics->OffsetIsolateIDInThreadConstant };
    
	return GetElementPtrInst::Create(mutatorThreadPtr, GEP, "", currentBlock);
}

llvm::Value* JavaJIT::getVMPtr(llvm::Value* mutatorThreadPtr) { 
	Value* GEP[3] = { intrinsics->constantZero,
										intrinsics->OffsetThreadInMutatorThreadConstant,
										intrinsics->OffsetVMInThreadConstant };
    
	return GetElementPtrInst::Create(mutatorThreadPtr, GEP, "", currentBlock);
}

llvm::Value* JavaJIT::getDoYieldPtr(llvm::Value* mutatorThreadPtr) {
	Value* GEP[3] = { intrinsics->constantZero,
										intrinsics->OffsetThreadInMutatorThreadConstant,
										intrinsics->OffsetDoYieldInThreadConstant };
    
	return GetElementPtrInst::Create(mutatorThreadPtr, GEP, "", currentBlock);
}

llvm::Value* JavaJIT::getJNIEnvPtr(llvm::Value* javaThreadPtr) { 
	Value* GEP[2] = { intrinsics->constantZero,
										intrinsics->OffsetJNIInJavaThreadConstant };
    
	return GetElementPtrInst::Create(javaThreadPtr, GEP, "", currentBlock);
}

llvm::Value* JavaJIT::getJavaExceptionPtr(llvm::Value* javaThreadPtr) { 
	Value* GEP[2] = { intrinsics->constantZero,
										intrinsics->OffsetJavaExceptionInJavaThreadConstant };
    
	return GetElementPtrInst::Create(javaThreadPtr, GEP, "", currentBlock);
}

static llvm::Function* GetNativeCallee(JavaLLVMCompiler* TheCompiler,
                                       JavaMethod* compilingMethod) {
  LLVMSignatureInfo* LSI =
    TheCompiler->getSignatureInfo(compilingMethod->getSignature());
  FunctionType* FTy = LSI->getNativeStubType();
  Function* callee = Function::Create(FTy,
                                      GlobalValue::ExternalLinkage,
                                      "",
                                      TheCompiler->getLLVMModule());
  std::vector<Value*> args;
  Function::arg_iterator i = callee->arg_begin();
  Value* nativeFunc = i;
  i++;
  for (Function::arg_iterator e = callee->arg_end(); i != e; i++) {
    args.push_back(i);
  }

  LLVMContext& llvmContext = TheCompiler->getLLVMContext();
  BasicBlock* BB = BasicBlock::Create(llvmContext, "", callee);
  Value* res = CallInst::Create(nativeFunc, args, "", BB);
  if (callee->getFunctionType()->getReturnType() != Type::getVoidTy(llvmContext)) {
    ReturnInst::Create(llvmContext, res, BB);
  } else {
    ReturnInst::Create(llvmContext, BB);
  }
  callee->setGC("vmkit");

  TheCompiler->GenerateStub(callee);
  return callee;
}

llvm::Function* JavaJIT::nativeCompile(word_t natPtr) {
  
  PRINT_DEBUG(JNJVM_COMPILE, 1, COLOR_NORMAL, "native compile %s.%s\n",
              UTF8Buffer(compilingClass->name).cString(),
              UTF8Buffer(compilingMethod->name).cString());
  
  bool stat = isStatic(compilingMethod->access);

  FunctionType *funcType = llvmFunction->getFunctionType();
  Type* returnType = funcType->getReturnType();
  
  bool j3 = false;
  
  const UTF8* jniConsClName = compilingClass->name;
  const UTF8* jniConsName = compilingMethod->name;
  const UTF8* jniConsType = compilingMethod->type;
  sint32 clen = jniConsClName->size;
  sint32 mnlen = jniConsName->size;
  sint32 mtlen = jniConsType->size;

  mvm::ThreadAllocator allocator;
  char* functionName = (char*)allocator.Allocate(
      3 + JNI_NAME_PRE_LEN + ((mnlen + clen + mtlen) << 3));
  
  if (!natPtr) {
    natPtr = compilingClass->classLoader->nativeLookup(compilingMethod, j3,
                                                       functionName);
  }
  
  if (!natPtr && !TheCompiler->isStaticCompiling()) {
    currentBlock = createBasicBlock("start");
    CallInst::Create(intrinsics->ThrowExceptionFromJITFunction, "", currentBlock);
    if (returnType != Type::getVoidTy(*llvmContext)) {
      ReturnInst::Create(*llvmContext, Constant::getNullValue(returnType), currentBlock);
    } else {
      ReturnInst::Create(*llvmContext, currentBlock);
    }
  
    PRINT_DEBUG(JNJVM_COMPILE, 1, COLOR_NORMAL, "end native compile %s.%s\n",
                UTF8Buffer(compilingClass->name).cString(),
                UTF8Buffer(compilingMethod->name).cString());
  
    return llvmFunction;
  }
  
  
  Function* func = llvmFunction;
  if (j3) {
    Function* callee = Function::Create(llvmFunction->getFunctionType(),
                                        GlobalValue::ExternalLinkage,
                                        functionName,
                                        llvmFunction->getParent());
    TheCompiler->setMethod(callee, (void*)natPtr, functionName);
    currentBlock = createBasicBlock("start");
    std::vector<Value*> args;
    for (Function::arg_iterator i = func->arg_begin(), e = func->arg_end();
         i != e;
         i++) {
      args.push_back(i);
    }
    Value* res = CallInst::Create(callee, args, "", currentBlock);
    if (returnType != Type::getVoidTy(*llvmContext)) {
      ReturnInst::Create(*llvmContext, res, currentBlock);
    } else {
      ReturnInst::Create(*llvmContext, currentBlock);
    }
    return llvmFunction;
  }

  currentExceptionBlock = endExceptionBlock = 0;
  currentBlock = createBasicBlock("start");
  endBlock = createBasicBlock("end block");
  
  if (returnType != Type::getVoidTy(*llvmContext)) {
    endNode = PHINode::Create(returnType, 0, "", endBlock);
  }
  
  // Allocate currentLocalIndexNumber pointer
  Value* temp = new AllocaInst(Type::getInt32Ty(*llvmContext), "",
                               currentBlock);
  new StoreInst(intrinsics->constantZero, temp, false, currentBlock);
  
  // Allocate oldCurrentLocalIndexNumber pointer
  Value* oldCLIN = new AllocaInst(PointerType::getUnqual(Type::getInt32Ty(*llvmContext)), "",
                                  currentBlock);
  
  Constant* sizeF = ConstantInt::get(Type::getInt32Ty(*llvmContext), sizeof(mvm::KnownFrame));
  Value* Frame = new AllocaInst(Type::getInt8Ty(*llvmContext), sizeF, "", currentBlock);
  
  uint32 nargs = func->arg_size() + 1 + (stat ? 1 : 0); 
  std::vector<Value*> nativeArgs;
  nativeArgs.push_back(NULL); // Will contain the callee
  
  
  Value* jniEnv = getJNIEnvPtr(getJavaThreadPtr(getMutatorThreadPtr()));
 
  jniEnv = new BitCastInst(jniEnv, intrinsics->ptrType, "", currentBlock);

  nativeArgs.push_back(jniEnv);

  uint32 index = 0;
  if (stat) {
    Value* cl = TheCompiler->getJavaClassPtr(compilingClass);
    nativeArgs.push_back(cl);
    index = 2;
  } else {
    index = 1;
  }
  for (Function::arg_iterator i = func->arg_begin(); 
       index < nargs; ++i, ++index) {
    
    if (i->getType() == intrinsics->JavaObjectType) {
      BasicBlock* BB = createBasicBlock("");
      BasicBlock* NotZero = createBasicBlock("");
      Type* Ty = PointerType::getUnqual(intrinsics->JavaObjectType);
      PHINode* node = PHINode::Create(Ty, 2, "", BB);

      Value* test = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, i,
                                 intrinsics->JavaObjectNullConstant, "");

      node->addIncoming(Constant::getNullValue(Ty), currentBlock);
      BranchInst::Create(BB, NotZero, test, currentBlock);

      currentBlock = NotZero;

      Instruction* temp = new AllocaInst(intrinsics->JavaObjectType, "",
                                         func->begin()->getTerminator());
      if (i == func->arg_begin() && !stat) {
        this->thisObject = temp;
      }
      
      if (TheCompiler->useCooperativeGC()) {
        Value* GCArgs[2] = { 
          new BitCastInst(temp, intrinsics->ptrPtrType, "",
                          func->begin()->getTerminator()),
          intrinsics->constantPtrNull
        };
        
        CallInst::Create(intrinsics->llvm_gc_gcroot, GCArgs, "",
                         func->begin()->getTerminator());
      }
      
      new StoreInst(i, temp, false, currentBlock);
      node->addIncoming(temp, currentBlock);
      BranchInst::Create(BB, currentBlock);

      currentBlock = BB;

      nativeArgs.push_back(node);
    } else {
      nativeArgs.push_back(i);
    }
  }
  
  
  Instruction* ResultObject = 0;
  if (returnType == intrinsics->JavaObjectType) {
    ResultObject = new AllocaInst(intrinsics->JavaObjectType, "",
                                  func->begin()->begin());
    
    if (TheCompiler->useCooperativeGC()) {
      
      Value* GCArgs[2] = { 
        new BitCastInst(ResultObject, intrinsics->ptrPtrType, "", currentBlock),
        intrinsics->constantPtrNull
      };
      
      CallInst::Create(intrinsics->llvm_gc_gcroot, GCArgs, "",
                       currentBlock);
    } else {
      new StoreInst(intrinsics->JavaObjectNullConstant, ResultObject, "",
                    currentBlock);
    }
  }
  
  Value* nativeFunc = TheCompiler->getNativeFunction(compilingMethod, (void*)natPtr);
  if (TheCompiler->isStaticCompiling()) {
    Value* Arg = TheCompiler->getMethodInClass(compilingMethod); 
    
    // If the global variable is null, then load it.
    BasicBlock* unloadedBlock = createBasicBlock("");
    BasicBlock* endBlock = createBasicBlock("");
    Value* test = new LoadInst(nativeFunc, "", currentBlock);
    Type* Ty = test->getType();
    PHINode* node = PHINode::Create(Ty, 2, "", endBlock);
    node->addIncoming(test, currentBlock);
    Value* cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, test,
                              Constant::getNullValue(Ty), "");
    BranchInst::Create(unloadedBlock, endBlock, cmp, currentBlock);
    currentBlock = unloadedBlock;

    Value* res = CallInst::Create(TheCompiler->NativeLoader, Arg, "",
                                  currentBlock);

    res = new BitCastInst(res, Ty, "", currentBlock);
    new StoreInst(res, nativeFunc, currentBlock);
    node->addIncoming(res, currentBlock);
    BranchInst::Create(endBlock, currentBlock);
    currentBlock = endBlock;
    nativeFunc = node;
  }
  nativeArgs[0] = nativeFunc;

  // Synchronize before saying we're entering native
  if (isSynchro(compilingMethod->access)) {
    nbHandlers = 1;
    beginSynchronize();
  }
  
  Value* Args4[3] = { temp, oldCLIN, Frame };

  CallInst::Create(intrinsics->StartJNIFunction, Args4, "", currentBlock);
  
  Function* callee = GetNativeCallee(TheCompiler, compilingMethod);
  Value* result = llvm::CallInst::Create(callee, nativeArgs, "", currentBlock);

  if (returnType == intrinsics->JavaObjectType) {
    Type* Ty = PointerType::getUnqual(intrinsics->JavaObjectType);
    Constant* C = Constant::getNullValue(Ty);
    Value* cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, result, C, "");
    BasicBlock* loadBlock = createBasicBlock("");

    endNode->addIncoming(intrinsics->JavaObjectNullConstant, currentBlock);
    BranchInst::Create(endBlock, loadBlock, cmp, currentBlock);

    currentBlock = loadBlock;
    result = new LoadInst(
        result, "", false, currentBlock);
    new StoreInst(result, ResultObject, "", currentBlock);
    endNode->addIncoming(result, currentBlock);

  } else if (returnType != Type::getVoidTy(*llvmContext)) {
    endNode->addIncoming(result, currentBlock);
  }
  
  BranchInst::Create(endBlock, currentBlock);


  currentBlock = endBlock; 
 
  Value* Args2[1] = { oldCLIN };

  CallInst::Create(intrinsics->EndJNIFunction, Args2, "", currentBlock);
  
  // Synchronize after leaving native.
  if (isSynchro(compilingMethod->access))
    endSynchronize();

  BasicBlock* ifNormal = createBasicBlock("");
  BasicBlock* ifException = createBasicBlock("");
  Value* javaExceptionPtr = getJavaExceptionPtr(getJavaThreadPtr(getMutatorThreadPtr()));
  Value* obj = new LoadInst(javaExceptionPtr, "", currentBlock);
  Value* test = new ICmpInst(*currentBlock, ICmpInst::ICMP_NE, obj, intrinsics->JavaObjectNullConstant, "");
  BranchInst::Create(ifException, ifNormal, test, currentBlock);

  currentBlock = ifException;
  // Clear exception.
  new StoreInst(intrinsics->JavaObjectNullConstant, javaExceptionPtr,
                currentBlock);
  CallInst::Create(intrinsics->ThrowExceptionFunction, obj, "", currentBlock);
  new UnreachableInst(*llvmContext, currentBlock);
  currentBlock = ifNormal;
  
  if (returnType != Type::getVoidTy(*llvmContext))
    ReturnInst::Create(*llvmContext, endNode, currentBlock);
  else
    ReturnInst::Create(*llvmContext, currentBlock);
  
  PRINT_DEBUG(JNJVM_COMPILE, 1, COLOR_NORMAL, "end native compile %s.%s\n",
              UTF8Buffer(compilingClass->name).cString(),
              UTF8Buffer(compilingMethod->name).cString());
  
  return llvmFunction;
}

void JavaJIT::monitorEnter(Value* obj) {
  std::vector<Value*> gep;
  gep.push_back(intrinsics->constantZero);
  gep.push_back(intrinsics->JavaObjectLockOffsetConstant);
  Value* lockPtr = GetElementPtrInst::Create(obj, gep, "", currentBlock);
  
  Value* lock = new LoadInst(lockPtr, "", currentBlock);
  lock = new PtrToIntInst(lock, intrinsics->pointerSizeType, "", currentBlock);
  Value* NonLockBitsMask = ConstantInt::get(intrinsics->pointerSizeType,
                                            mvm::ThinLock::NonLockBitsMask);

  lock = BinaryOperator::CreateAnd(lock, NonLockBitsMask, "", currentBlock);

  lockPtr = new BitCastInst(lockPtr, 
                            PointerType::getUnqual(intrinsics->pointerSizeType),
                            "", currentBlock);
  Value* threadId = getMutatorThreadPtr();
  threadId = new PtrToIntInst(threadId, intrinsics->pointerSizeType, "",
                              currentBlock);
  Value* newValMask = BinaryOperator::CreateOr(threadId, lock, "",
                                               currentBlock);

  // Do the atomic compare and swap.
  Value* atomic = new AtomicCmpXchgInst(
      lockPtr, lock, newValMask, SequentiallyConsistent, CrossThread,
      currentBlock);
  
  Value* cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, atomic,
                            lock, "");
  
  BasicBlock* OK = createBasicBlock("synchronize passed");
  BasicBlock* NotOK = createBasicBlock("synchronize did not pass");

  BranchInst::Create(OK, NotOK, cmp, currentBlock);

  // The atomic cas did not work.
  currentBlock = NotOK;
  CallInst::Create(intrinsics->AquireObjectFunction, obj, "", currentBlock);
  BranchInst::Create(OK, currentBlock);

  currentBlock = OK;
}

void JavaJIT::monitorExit(Value* obj) {
  std::vector<Value*> gep;
  gep.push_back(intrinsics->constantZero);
  gep.push_back(intrinsics->JavaObjectLockOffsetConstant);
  Value* lockPtr = GetElementPtrInst::Create(obj, gep, "", currentBlock);
  lockPtr = new BitCastInst(lockPtr, 
                            PointerType::getUnqual(intrinsics->pointerSizeType),
                            "", currentBlock);
  Value* lock = new LoadInst(lockPtr, "", currentBlock);
  Value* NonLockBitsMask = ConstantInt::get(
      intrinsics->pointerSizeType, mvm::ThinLock::NonLockBitsMask);

  Value* lockedMask = BinaryOperator::CreateAnd(
      lock, NonLockBitsMask, "", currentBlock);
  
  Value* threadId = getMutatorThreadPtr();
  threadId = new PtrToIntInst(threadId, intrinsics->pointerSizeType, "",
                              currentBlock);
  
  Value* oldValMask = BinaryOperator::CreateOr(threadId, lockedMask, "",
                                               currentBlock);

  std::vector<Value*> atomicArgs;
  atomicArgs.push_back(lockPtr);
  atomicArgs.push_back(oldValMask);
  atomicArgs.push_back(lockedMask);

  // Do the atomic compare and swap.
  Value* atomic = new AtomicCmpXchgInst(
      lockPtr, oldValMask, lockedMask, SequentiallyConsistent, CrossThread,
      currentBlock);
  
  Value* cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, atomic,
                            oldValMask, "");
  
  BasicBlock* OK = createBasicBlock("unsynchronize passed");
  BasicBlock* NotOK = createBasicBlock("unsynchronize did not pass");

  BranchInst::Create(OK, NotOK, cmp, currentBlock);

  // The atomic cas did not work.
  currentBlock = NotOK;
  CallInst::Create(intrinsics->ReleaseObjectFunction, obj, "", currentBlock);
  BranchInst::Create(OK, currentBlock);

  currentBlock = OK;
}

void JavaJIT::beginSynchronize() {
  Value* obj = 0;
  if (isVirtual(compilingMethod->access)) {
    assert(thisObject != NULL && "beginSynchronize without this");
    obj = new LoadInst(
        thisObject, "", false, currentBlock);
  } else {
    obj = TheCompiler->getJavaClassPtr(compilingClass);
    obj = new LoadInst(obj, "", false, currentBlock);
  }
  monitorEnter(obj);
}

void JavaJIT::endSynchronize() {
  Value* obj = 0;
  if (isVirtual(compilingMethod->access)) {
    assert(thisObject != NULL && "endSynchronize without this");
    obj = new LoadInst(
        thisObject, "", false, currentBlock);
  } else {
    obj = TheCompiler->getJavaClassPtr(compilingClass);
    obj = new LoadInst(obj, "", false, currentBlock);
  }
  monitorExit(obj);
}


static void removeUnusedLocals(std::vector<AllocaInst*>& locals) {
  for (std::vector<AllocaInst*>::iterator i = locals.begin(),
       e = locals.end(); i != e; ++i) {
    AllocaInst* temp = *i;
    unsigned uses = temp->getNumUses();
    if (!uses) {
      temp->eraseFromParent();
    } else if (uses == 1 && dyn_cast<StoreInst>(*(temp->use_begin()))) {
      dyn_cast<StoreInst>(*(temp->use_begin()))->eraseFromParent();
      temp->eraseFromParent();
    }
  }
}
  
static void removeUnusedObjects(std::vector<AllocaInst*>& objects,
                                J3Intrinsics* intrinsics, bool coop) {
  for (std::vector<AllocaInst*>::iterator i = objects.begin(),
       e = objects.end(); i != e; ++i) {
    AllocaInst* temp = *i;
    unsigned uses = temp->getNumUses();
    if (!uses) {
      temp->eraseFromParent();
    } else if (uses == 1 && dyn_cast<StoreInst>(*(temp->use_begin()))) {
      dyn_cast<StoreInst>(*(temp->use_begin()))->eraseFromParent();
      temp->eraseFromParent();
    } else {
      if (coop) {
        Instruction* I = new BitCastInst(temp, intrinsics->ptrPtrType, "");
        I->insertAfter(temp);
        Value* GCArgs[2] = { I, intrinsics->constantPtrNull };
        Instruction* C = CallInst::Create(intrinsics->llvm_gc_gcroot, GCArgs, "");
        C->insertAfter(I);
      }
    }
  }
}

Instruction* JavaJIT::inlineCompile(BasicBlock*& curBB,
                                    BasicBlock* endExBlock,
                                    std::vector<Value*>& args) {
  Attribut* codeAtt = compilingMethod->lookupAttribut(Attribut::codeAttribut);
  Reader reader(codeAtt, compilingClass->bytes);
  uint16 maxStack = reader.readU2();
  uint16 maxLocals = reader.readU2();
  uint32 codeLen = reader.readU4();
  uint32 start = reader.cursor; 
  reader.seek(codeLen, Reader::SeekCur);
  
  LLVMAssessorInfo& LAI = TheCompiler->getTypedefInfo(
      compilingMethod->getSignature()->getReturnType());
  Type* returnType = LAI.llvmType;

  endBlock = createBasicBlock("end");

  currentBlock = curBB;
  endExceptionBlock = endExBlock;

  opcodeInfos = new Opinfo[codeLen];
  memset(opcodeInfos, 0, codeLen * sizeof(Opinfo));
  for (uint32 i = 0; i < codeLen; ++i) {
    opcodeInfos[i].exceptionBlock = endExBlock;
  }
  
  BasicBlock* firstBB = llvmFunction->begin();
  
  if (firstBB->begin() != firstBB->end()) {
    Instruction* firstInstruction = firstBB->begin();

    for (int i = 0; i < maxLocals; i++) {
      intLocals.push_back(new AllocaInst(Type::getInt32Ty(*llvmContext), "", firstInstruction));
      new StoreInst(Constant::getNullValue(Type::getInt32Ty(*llvmContext)), intLocals.back(), false, firstInstruction);
      doubleLocals.push_back(new AllocaInst(Type::getDoubleTy(*llvmContext), "",
                                            firstInstruction));
      new StoreInst(Constant::getNullValue(Type::getDoubleTy(*llvmContext)), doubleLocals.back(), false, firstInstruction);
      longLocals.push_back(new AllocaInst(Type::getInt64Ty(*llvmContext), "", firstInstruction));
      new StoreInst(Constant::getNullValue(Type::getInt64Ty(*llvmContext)), longLocals.back(), false, firstInstruction);
      floatLocals.push_back(new AllocaInst(Type::getFloatTy(*llvmContext), "", firstInstruction));
      new StoreInst(Constant::getNullValue(Type::getFloatTy(*llvmContext)), floatLocals.back(), false, firstInstruction);
      objectLocals.push_back(new AllocaInst(intrinsics->JavaObjectType, "",
                                          firstInstruction));
     
      // The GCStrategy will already initialize the value.
      if (!TheCompiler->useCooperativeGC())
        new StoreInst(Constant::getNullValue(intrinsics->JavaObjectType), objectLocals.back(), false, firstInstruction);
    }
    for (int i = 0; i < maxStack; i++) {
      objectStack.push_back(new AllocaInst(intrinsics->JavaObjectType, "",
                                           firstInstruction));
      addHighLevelType(objectStack.back(), upcalls->OfObject);
      intStack.push_back(new AllocaInst(Type::getInt32Ty(*llvmContext), "", firstInstruction));
      doubleStack.push_back(new AllocaInst(Type::getDoubleTy(*llvmContext), "",
                                           firstInstruction));
      longStack.push_back(new AllocaInst(Type::getInt64Ty(*llvmContext), "", firstInstruction));
      floatStack.push_back(new AllocaInst(Type::getFloatTy(*llvmContext), "", firstInstruction));
    }

  } else {
    for (int i = 0; i < maxLocals; i++) {
      intLocals.push_back(new AllocaInst(Type::getInt32Ty(*llvmContext), "", firstBB));
      new StoreInst(Constant::getNullValue(Type::getInt32Ty(*llvmContext)), intLocals.back(), false, firstBB);
      doubleLocals.push_back(new AllocaInst(Type::getDoubleTy(*llvmContext), "", firstBB));
      new StoreInst(Constant::getNullValue(Type::getDoubleTy(*llvmContext)), doubleLocals.back(), false, firstBB);
      longLocals.push_back(new AllocaInst(Type::getInt64Ty(*llvmContext), "", firstBB));
      new StoreInst(Constant::getNullValue(Type::getInt64Ty(*llvmContext)), longLocals.back(), false, firstBB);
      floatLocals.push_back(new AllocaInst(Type::getFloatTy(*llvmContext), "", firstBB));
      new StoreInst(Constant::getNullValue(Type::getFloatTy(*llvmContext)), floatLocals.back(), false, firstBB);
      objectLocals.push_back(new AllocaInst(intrinsics->JavaObjectType, "",
                                            firstBB));
      // The GCStrategy will already initialize the value.
      if (!TheCompiler->useCooperativeGC())
        new StoreInst(Constant::getNullValue(intrinsics->JavaObjectType), objectLocals.back(), false, firstBB);
    }
    
    for (int i = 0; i < maxStack; i++) {
      objectStack.push_back(new AllocaInst(intrinsics->JavaObjectType, "",
                                           firstBB));
      addHighLevelType(objectStack.back(), upcalls->OfObject);
      intStack.push_back(new AllocaInst(Type::getInt32Ty(*llvmContext), "", firstBB));
      doubleStack.push_back(new AllocaInst(Type::getDoubleTy(*llvmContext), "", firstBB));
      longStack.push_back(new AllocaInst(Type::getInt64Ty(*llvmContext), "", firstBB));
      floatStack.push_back(new AllocaInst(Type::getFloatTy(*llvmContext), "", firstBB));
    }
  }

  uint32 index = 0;
  uint32 count = 0;
  uint32 max = args.size();

  Signdef* sign = compilingMethod->getSignature();
  Typedef* const* arguments = sign->getArgumentsType();
  uint32 type = 0;
  std::vector<Value*>::iterator i = args.begin(); 

  if (isVirtual(compilingMethod->access)) {
    Instruction* V = new StoreInst(*i, objectLocals[0], false, currentBlock);
    addHighLevelType(V, compilingClass);
    ++i;
    ++index;
    ++count;
    thisObject = objectLocals[0];
  }

  for (;count < max; ++i, ++index, ++count, ++type) {
    
    const Typedef* cur = arguments[type];
    Type* curType = (*i)->getType();

    if (curType == Type::getInt64Ty(*llvmContext)){
      new StoreInst(*i, longLocals[index], false, currentBlock);
      ++index;
    } else if (cur->isUnsigned()) {
      new StoreInst(new ZExtInst(*i, Type::getInt32Ty(*llvmContext), "", currentBlock),
                    intLocals[index], false, currentBlock);
    } else if (curType == Type::getInt8Ty(*llvmContext) || curType == Type::getInt16Ty(*llvmContext)) {
      new StoreInst(new SExtInst(*i, Type::getInt32Ty(*llvmContext), "", currentBlock),
                    intLocals[index], false, currentBlock);
    } else if (curType == Type::getInt32Ty(*llvmContext)) {
      new StoreInst(*i, intLocals[index], false, currentBlock);
    } else if (curType == Type::getDoubleTy(*llvmContext)) {
      new StoreInst(*i, doubleLocals[index], false, currentBlock);
      ++index;
    } else if (curType == Type::getFloatTy(*llvmContext)) {
      new StoreInst(*i, floatLocals[index], false, currentBlock);
    } else {
      Instruction* V = new StoreInst(*i, objectLocals[index], false, currentBlock);
      addHighLevelType(V, cur->findAssocClass(compilingClass->classLoader));
    }
  }
  
  nbHandlers = readExceptionTable(reader, codeLen);
  
  reader.cursor = start;
  exploreOpcodes(reader, codeLen);

  if (returnType != Type::getVoidTy(*llvmContext)) {
    endNode = PHINode::Create(returnType, 0, "", endBlock);
  }

  reader.cursor = start;
  compileOpcodes(reader, codeLen);
  
  PRINT_DEBUG(JNJVM_COMPILE, 1, COLOR_NORMAL,
              "--> end inline compiling %s.%s\n",
              UTF8Buffer(compilingClass->name).cString(),
              UTF8Buffer(compilingMethod->name).cString());

  curBB = endBlock;


  removeUnusedLocals(intLocals);
  removeUnusedLocals(doubleLocals);
  removeUnusedLocals(floatLocals);
  removeUnusedLocals(longLocals);
  removeUnusedLocals(intStack);
  removeUnusedLocals(doubleStack);
  removeUnusedLocals(floatStack);
  removeUnusedLocals(longStack);
  
  removeUnusedObjects(objectLocals, intrinsics, TheCompiler->useCooperativeGC());
  removeUnusedObjects(objectStack, intrinsics, TheCompiler->useCooperativeGC());


  delete[] opcodeInfos;
  return endNode;
}

llvm::Function* JavaJIT::javaCompile() {
  PRINT_DEBUG(JNJVM_COMPILE, 1, COLOR_NORMAL, "compiling %s.%s\n",
              UTF8Buffer(compilingClass->name).cString(),
              UTF8Buffer(compilingMethod->name).cString());

  DbgSubprogram = TheCompiler->getDebugFactory()->createFunction(
      DIDescriptor(), "", "", DIFile(), 0, DIType(), false, false);

  Attribut* codeAtt = compilingMethod->lookupAttribut(Attribut::codeAttribut);
  
  if (!codeAtt) {
    fprintf(stderr, "I haven't verified your class file and it's malformed:"
                    " no code attribute found for %s.%s!\n",
                    UTF8Buffer(compilingClass->name).cString(),
                    UTF8Buffer(compilingMethod->name).cString());
    abort();
  } 

  Reader reader(codeAtt, compilingClass->bytes);
  uint16 maxStack = reader.readU2();
  uint16 maxLocals = reader.readU2();
  uint32 codeLen = reader.readU4();
  uint32 start = reader.cursor;
  
  reader.seek(codeLen, Reader::SeekCur);

  FunctionType *funcType = llvmFunction->getFunctionType();
  Type* returnType = funcType->getReturnType();
  
  Function* func = llvmFunction;

  currentBlock = createBasicBlock("start");
  endExceptionBlock = createBasicBlock("endExceptionBlock");
  unifiedUnreachable = createBasicBlock("unifiedUnreachable");

  opcodeInfos = new Opinfo[codeLen];
  memset(opcodeInfos, 0, codeLen * sizeof(Opinfo));
  for (uint32 i = 0; i < codeLen; ++i) {
    opcodeInfos[i].exceptionBlock = endExceptionBlock;
  }

  Instruction* returnValue = NULL;
  if (returnType == intrinsics->JavaObjectType &&
      TheCompiler->useCooperativeGC()) {
    returnValue = new AllocaInst(intrinsics->JavaObjectType, "",
                                 currentBlock);
    Instruction* cast = 
        new BitCastInst(returnValue, intrinsics->ptrPtrType, "", currentBlock);
    Value* GCArgs[2] = { cast, intrinsics->constantPtrNull };
        
    CallInst::Create(intrinsics->llvm_gc_gcroot, GCArgs, "", currentBlock);
  }

  for (int i = 0; i < maxLocals; i++) {
    intLocals.push_back(new AllocaInst(Type::getInt32Ty(*llvmContext), "", currentBlock));
    new StoreInst(Constant::getNullValue(Type::getInt32Ty(*llvmContext)), intLocals.back(), false, currentBlock);
    doubleLocals.push_back(new AllocaInst(Type::getDoubleTy(*llvmContext), "", currentBlock));
    new StoreInst(Constant::getNullValue(Type::getDoubleTy(*llvmContext)), doubleLocals.back(), false, currentBlock);
    longLocals.push_back(new AllocaInst(Type::getInt64Ty(*llvmContext), "", currentBlock));
    new StoreInst(Constant::getNullValue(Type::getInt64Ty(*llvmContext)), longLocals.back(), false, currentBlock);
    floatLocals.push_back(new AllocaInst(Type::getFloatTy(*llvmContext), "", currentBlock));
    new StoreInst(Constant::getNullValue(Type::getFloatTy(*llvmContext)), floatLocals.back(), false, currentBlock);
    objectLocals.push_back(new AllocaInst(intrinsics->JavaObjectType, "",
                                          currentBlock));
    // The GCStrategy will already initialize the value.
    if (!TheCompiler->useCooperativeGC())
      new StoreInst(Constant::getNullValue(intrinsics->JavaObjectType), objectLocals.back(), false, currentBlock);
  }
  
  for (int i = 0; i < maxStack; i++) {
    objectStack.push_back(new AllocaInst(intrinsics->JavaObjectType, "",
                                         currentBlock));
    addHighLevelType(objectStack.back(), upcalls->OfObject);
    intStack.push_back(new AllocaInst(Type::getInt32Ty(*llvmContext), "", currentBlock));
    doubleStack.push_back(new AllocaInst(Type::getDoubleTy(*llvmContext), "", currentBlock));
    longStack.push_back(new AllocaInst(Type::getInt64Ty(*llvmContext), "", currentBlock));
    floatStack.push_back(new AllocaInst(Type::getFloatTy(*llvmContext), "", currentBlock));
  }
 
  uint32 index = 0;
  uint32 count = 0;
  uint32 max = func->arg_size();

  Function::arg_iterator i = func->arg_begin(); 
  Signdef* sign = compilingMethod->getSignature();
  Typedef* const* arguments = sign->getArgumentsType();
  uint32 type = 0;

  if (isVirtual(compilingMethod->access)) {
    Instruction* V = new StoreInst(i, objectLocals[0], false, currentBlock);
    addHighLevelType(V, compilingClass);
    ++i;
    ++index;
    ++count;
    thisObject = objectLocals[0];
  }

  for (;count < max; ++i, ++index, ++count, ++type) {
    
    const Typedef* cur = arguments[type];
    Type* curType = i->getType();

    if (curType == Type::getInt64Ty(*llvmContext)){
      new StoreInst(i, longLocals[index], false, currentBlock);
      ++index;
    } else if (cur->isUnsigned()) {
      new StoreInst(new ZExtInst(i, Type::getInt32Ty(*llvmContext), "", currentBlock),
                    intLocals[index], false, currentBlock);
    } else if (curType == Type::getInt8Ty(*llvmContext) || curType == Type::getInt16Ty(*llvmContext)) {
      new StoreInst(new SExtInst(i, Type::getInt32Ty(*llvmContext), "", currentBlock),
                    intLocals[index], false, currentBlock);
    } else if (curType == Type::getInt32Ty(*llvmContext)) {
      new StoreInst(i, intLocals[index], false, currentBlock);
    } else if (curType == Type::getDoubleTy(*llvmContext)) {
      new StoreInst(i, doubleLocals[index], false, currentBlock);
      ++index;
    } else if (curType == Type::getFloatTy(*llvmContext)) {
      new StoreInst(i, floatLocals[index], false, currentBlock);
    } else {
      Instruction* V = new StoreInst(i, objectLocals[index], false, currentBlock);
      addHighLevelType(V, cur->findAssocClass(compilingClass->classLoader));
    }
  }

  // Now that arguments have been setup, we can proceed with runtime calls.
#if JNJVM_EXECUTE > 0
    {
    Value* arg = TheCompiler->getMethodInClass(compilingMethod);

    llvm::CallInst::Create(intrinsics->PrintMethodStartFunction, arg, "",
                           currentBlock);
    }
#endif

  nbHandlers = readExceptionTable(reader, codeLen);
  if (nbHandlers != 0) {
    jmpBuffer = new AllocaInst(ArrayType::get(Type::getInt8Ty(*llvmContext), sizeof(mvm::ExceptionBuffer)), "", currentBlock);
    jmpBuffer = new BitCastInst(jmpBuffer, intrinsics->ptrType, "", currentBlock);
  }
  
  reader.cursor = start;
  exploreOpcodes(reader, codeLen);
 
  endBlock = createBasicBlock("end");

  if (returnType != Type::getVoidTy(*llvmContext)) {
    endNode = llvm::PHINode::Create(returnType, 0, "", endBlock);
  }
  
  if (isSynchro(compilingMethod->access)) {
    beginSynchronize();
  }
  
  if (TheCompiler->useCooperativeGC()) {
    Value* YieldPtr = getDoYieldPtr(getMutatorThreadPtr());

    Value* Yield = new LoadInst(YieldPtr, "", currentBlock);

    BasicBlock* continueBlock = createBasicBlock("After safe point");
    BasicBlock* yieldBlock = createBasicBlock("In safe point");
    BranchInst::Create(yieldBlock, continueBlock, Yield, currentBlock);

    currentBlock = yieldBlock;
    CallInst::Create(intrinsics->conditionalSafePoint, "", currentBlock);
    BranchInst::Create(continueBlock, currentBlock);

    currentBlock = continueBlock;
  }
  
  if (TheCompiler->hasExceptionsEnabled()) {
    // Variables have been allocated and the lock has been taken. Do the stack
    // check now: if there is an exception, we will go to the lock release code.
    currentExceptionBlock = opcodeInfos[0].exceptionBlock;
    Value* FrameAddr = CallInst::Create(intrinsics->llvm_frameaddress,
                                       	intrinsics->constantZero, "", currentBlock);
    FrameAddr = new PtrToIntInst(FrameAddr, intrinsics->pointerSizeType, "",
                                 currentBlock);
    Value* stackCheck = 
      BinaryOperator::CreateAnd(FrameAddr, intrinsics->constantStackOverflowMask,
                                "", currentBlock);

    stackCheck = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, stackCheck,
                              intrinsics->constantPtrZero, "");
    BasicBlock* stackOverflow = createBasicBlock("stack overflow");
    BasicBlock* noStackOverflow = createBasicBlock("no stack overflow");
    BranchInst::Create(stackOverflow, noStackOverflow, stackCheck,
                       currentBlock);
    currentBlock = stackOverflow;
    throwRuntimeException(intrinsics->StackOverflowErrorFunction, 0, 0);
    currentBlock = noStackOverflow;
  }

  reader.cursor = start;
  compileOpcodes(reader, codeLen);
  
  assert(stack.size() == 0 && "Stack not empty after compiling bytecode");
  // Fix a javac(?) bug where a method only throws an exception and does
  // not return.
  pred_iterator PI = pred_begin(endBlock);
  pred_iterator PE = pred_end(endBlock);
  if (PI == PE && returnType != Type::getVoidTy(*llvmContext)) {
    Instruction* I = currentBlock->getTerminator();
    
    if (isa<UnreachableInst>(I)) {
      I->eraseFromParent();
      BranchInst::Create(endBlock, currentBlock);
      endNode->addIncoming(Constant::getNullValue(returnType),
                           currentBlock);
    } else if (InvokeInst* II = dyn_cast<InvokeInst>(I)) {
      II->setNormalDest(endBlock);
      endNode->addIncoming(Constant::getNullValue(returnType),
                           currentBlock);
    }

  }
  currentBlock = endBlock;

  if (returnValue != NULL) {
    new StoreInst(endNode, returnValue, currentBlock);
  }
  
  if (isSynchro(compilingMethod->access)) {
    endSynchronize();
  }

#if JNJVM_EXECUTE > 0
    {
    Value* arg = TheCompiler->getMethodInClass(compilingMethod); 
    CallInst::Create(intrinsics->PrintMethodEndFunction, arg, "", currentBlock);
    }
#endif

  finishExceptions();
  
  PI = pred_begin(currentBlock);
  PE = pred_end(currentBlock);
  if (PI == PE) {
    currentBlock->eraseFromParent();
  } else {
    if (nbHandlers != 0) {
      BasicBlock* ifNormal = createBasicBlock("");
      BasicBlock* ifException = createBasicBlock("");
      Value* javaExceptionPtr = getJavaExceptionPtr(getJavaThreadPtr(getMutatorThreadPtr()));
      Value* obj = new LoadInst(javaExceptionPtr, "", currentBlock);
      Value* test = new ICmpInst(*currentBlock, ICmpInst::ICMP_NE, obj, intrinsics->JavaObjectNullConstant, "");
      BranchInst::Create(ifException, ifNormal, test, currentBlock);

      currentBlock = ifException;
      // Clear exception.
      new StoreInst(intrinsics->JavaObjectNullConstant, javaExceptionPtr,
                    currentBlock);
      CallInst::Create(intrinsics->ThrowExceptionFunction, obj, "", currentBlock);
      new UnreachableInst(*llvmContext, currentBlock);
      currentBlock = ifNormal;
    }

    if (returnType != Type::getVoidTy(*llvmContext)) {
      if (returnValue != NULL) {
        Value* obj = new LoadInst(
            returnValue, "", false, currentBlock);
        ReturnInst::Create(*llvmContext, obj, currentBlock);
      } else {
        ReturnInst::Create(*llvmContext, endNode, currentBlock);
      }
    } else {
      ReturnInst::Create(*llvmContext, currentBlock);
    }
  }

   
  removeUnusedLocals(intLocals);
  removeUnusedLocals(doubleLocals);
  removeUnusedLocals(floatLocals);
  removeUnusedLocals(longLocals);
  removeUnusedLocals(intStack);
  removeUnusedLocals(doubleStack);
  removeUnusedLocals(floatStack);
  removeUnusedLocals(longStack);
  
  removeUnusedObjects(objectLocals, intrinsics, TheCompiler->useCooperativeGC());
  removeUnusedObjects(objectStack, intrinsics, TheCompiler->useCooperativeGC());
 
  delete[] opcodeInfos;

  PRINT_DEBUG(JNJVM_COMPILE, 1, COLOR_NORMAL, "--> end compiling %s.%s\n",
              UTF8Buffer(compilingClass->name).cString(),
              UTF8Buffer(compilingMethod->name).cString());
   
  Attribut* annotationsAtt =
    compilingMethod->lookupAttribut(Attribut::annotationsAttribut);
  
  if (annotationsAtt) {
    Reader reader(annotationsAtt, compilingClass->bytes);
    AnnotationReader AR(reader, compilingClass);
    uint16 numAnnotations = reader.readU2();
    for (uint16 i = 0; i < numAnnotations; ++i) {
      AR.readAnnotation();
      const UTF8* name =
        compilingClass->ctpInfo->UTF8At(AR.AnnotationNameIndex);
      if (name->equals(TheCompiler->InlinePragma)) {
        llvmFunction->removeFnAttr(Attribute::NoInline);
        llvmFunction->addFnAttr(Attribute::AlwaysInline);
      } else if (name->equals(TheCompiler->NoInlinePragma)) {
        llvmFunction->addFnAttr(Attribute::NoInline);
      }
    }
  }
 
  return llvmFunction;
}

void JavaJIT::compareFP(Value* val1, Value* val2, Type* ty, bool l) {
  Value* one = intrinsics->constantOne;
  Value* zero = intrinsics->constantZero;
  Value* minus = intrinsics->constantMinusOne;

  Value* c = new FCmpInst(*currentBlock, FCmpInst::FCMP_UGT, val1, val2, "");
  Value* r = llvm::SelectInst::Create(c, one, zero, "", currentBlock);
  c = new FCmpInst(*currentBlock, FCmpInst::FCMP_ULT, val1, val2, "");
  r = llvm::SelectInst::Create(c, minus, r, "", currentBlock);
  c = new FCmpInst(*currentBlock, FCmpInst::FCMP_UNO, val1, val2, "");
  r = llvm::SelectInst::Create(c, l ? one : minus, r, "", currentBlock);

  push(r, false);

}

void JavaJIT::loadConstant(uint16 index) {
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  uint8 type = ctpInfo->typeAt(index);
  
  if (type == JavaConstantPool::ConstantString) {
    if (TheCompiler->isStaticCompiling() && !TheCompiler->useCooperativeGC()) {
      const UTF8* utf8 = ctpInfo->UTF8At(ctpInfo->ctpDef[index]);
      JavaString* str = *(compilingClass->classLoader->UTF8ToStr(utf8));
      Value* val = TheCompiler->getString(str);
      push(val, false, upcalls->newString);
    } else {
      JavaString** str = (JavaString**)ctpInfo->ctpRes[index];
      if ((str != NULL) && !TheCompiler->isStaticCompiling()) {
        Value* val = TheCompiler->getStringPtr(str);
        val = new LoadInst(val, "", currentBlock);
        push(val, false, upcalls->newString);
      } else {
        // Lookup the constant pool cache
        Type* Ty = PointerType::getUnqual(intrinsics->JavaObjectType);
        Value* val = getConstantPoolAt(index, intrinsics->StringLookupFunction,
                                       Ty, 0, false);
        val = new LoadInst(val, "", currentBlock);
        push(val, false, upcalls->newString);
      }
    }
  } else if (type == JavaConstantPool::ConstantLong) {
    push(ConstantInt::get(Type::getInt64Ty(*llvmContext), ctpInfo->LongAt(index)),
         false);
  } else if (type == JavaConstantPool::ConstantDouble) {
    push(ConstantFP::get(Type::getDoubleTy(*llvmContext), ctpInfo->DoubleAt(index)),
         false);
  } else if (type == JavaConstantPool::ConstantInteger) {
    push(ConstantInt::get(Type::getInt32Ty(*llvmContext), ctpInfo->IntegerAt(index)),
         false);
  } else if (type == JavaConstantPool::ConstantFloat) {
    push(ConstantFP::get(Type::getFloatTy(*llvmContext), ctpInfo->FloatAt(index)),
         false);
  } else if (type == JavaConstantPool::ConstantClass) {
    UserCommonClass* cl = 0;
    Value* res = getResolvedCommonClass(index, false, &cl);

    res = CallInst::Create(intrinsics->GetClassDelegateeFunction, res, "",
                           currentBlock);
    push(res, false, upcalls->newClass);
  } else {
    fprintf(stderr, "I haven't verified your class file and it's malformed:"
                    " unknown ldc %d in %s.%s!\n", type,
                    UTF8Buffer(compilingClass->name).cString(),
                    UTF8Buffer(compilingMethod->name).cString());
    abort();
  }
}

void JavaJIT::JITVerifyNull(Value* obj) {
  if (TheCompiler->hasExceptionsEnabled()) {
    Constant* zero = intrinsics->JavaObjectNullConstant;
    Value* test = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, obj, zero, "");

    BasicBlock* exit = createBasicBlock("verifyNullExit");
    BasicBlock* cont = createBasicBlock("verifyNullCont");

    BranchInst::Create(exit, cont, test, currentBlock);
    currentBlock = exit;
    throwRuntimeException(intrinsics->NullPointerExceptionFunction, 0, 0);
    currentBlock = cont;
  } 
}

Value* JavaJIT::verifyAndComputePtr(Value* obj, Value* index,
                                    Type* arrayType, bool doNullCheck) {
  if (doNullCheck) {
    JITVerifyNull(obj);
  }
  
  if (index->getType() != Type::getInt32Ty(*llvmContext)) {
    index = new SExtInst(index, Type::getInt32Ty(*llvmContext), "", currentBlock);
  }
  
  if (TheCompiler->hasExceptionsEnabled()) {
    Value* size = arraySize(obj);
    
    Value* cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_ULT, index, size,
                              "");

    BasicBlock* ifTrue =  createBasicBlock("true verifyAndComputePtr");
    BasicBlock* ifFalse = createBasicBlock("false verifyAndComputePtr");

    BranchInst::Create(ifTrue, ifFalse, cmp, currentBlock);
    
    currentBlock = ifFalse;
    Value* args[2] = { obj, index };
    throwRuntimeException(intrinsics->IndexOutOfBoundsExceptionFunction, args, 2);
    currentBlock = ifTrue;
  }
  
  Constant* zero = intrinsics->constantZero;
  Value* val = new BitCastInst(obj, arrayType, "", currentBlock);
  
  Value* indexes[3] = { zero, intrinsics->JavaArrayElementsOffsetConstant, index };
  Value* ptr = GetElementPtrInst::Create(val, indexes, "", currentBlock);

  return ptr;

}

void JavaJIT::makeArgs(FunctionType::param_iterator it,
                       uint32 index, std::vector<Value*>& Args, uint32 nb) {
  Args.reserve(nb + 2);
  mvm::ThreadAllocator threadAllocator;
  Value** args = (Value**)threadAllocator.Allocate(nb*sizeof(Value*));
  sint32 start = nb - 1;

  for (sint32 i = start; i >= 0; --i) {
    it--;
    if (*it == Type::getInt64Ty(*llvmContext)
        || *it == Type::getDoubleTy(*llvmContext)) {
      pop();
    }
    Value* tmp = pop();
    
    Type* type = *it;
    if (tmp->getType() != type) { // int8 or int16
      convertValue(tmp, type, currentBlock, false);
    }
    args[i] = tmp;

  }

  for (uint32 i = 0; i < nb; ++i) {
    Args.push_back(args[i]);
  }
}

Value* JavaJIT::getTarget(Signdef* signature) {
  int offset = 0;
  Typedef* const* arguments = signature->getArgumentsType();
  for (uint32 i = 0; i < signature->nbArguments; i++) {
    if (arguments[i]->isDouble() || arguments[i]->isLong()) {
      offset++;
    }
    offset++;
  }
  return objectStack[currentStackIndex - 1 - offset];
}

Instruction* JavaJIT::lowerMathOps(const UTF8* name, 
                                   std::vector<Value*>& args) {
  JnjvmBootstrapLoader* loader = compilingClass->classLoader->bootstrapLoader;
  if (name->equals(loader->abs)) {
    const Type* Ty = args[0]->getType();
    if (Ty == Type::getInt32Ty(*llvmContext)) {
      Constant* const_int32_9 = intrinsics->constantZero;
      Constant* const_int32_10 = intrinsics->constantMinusOne;
      BinaryOperator* int32_tmpneg = 
        BinaryOperator::Create(Instruction::Sub, const_int32_9, args[0],
                               "tmpneg", currentBlock);
      ICmpInst* int1_abscond = 
        new ICmpInst(*currentBlock, ICmpInst::ICMP_SGT, args[0], const_int32_10,
                     "abscond");
      return llvm::SelectInst::Create(int1_abscond, args[0], int32_tmpneg,
                                      "abs", currentBlock);
    } else if (Ty == Type::getInt64Ty(*llvmContext)) {
      Constant* const_int64_9 = intrinsics->constantLongZero;
      Constant* const_int64_10 = intrinsics->constantLongMinusOne;
      
      BinaryOperator* int64_tmpneg = 
        BinaryOperator::Create(Instruction::Sub, const_int64_9, args[0],
                               "tmpneg", currentBlock);

      ICmpInst* int1_abscond = new ICmpInst(*currentBlock, ICmpInst::ICMP_SGT,
                                            args[0], const_int64_10, "abscond");
      
      return llvm::SelectInst::Create(int1_abscond, args[0], int64_tmpneg,
                                      "abs", currentBlock);
    } else if (Ty == Type::getFloatTy(*llvmContext)) {
      return llvm::CallInst::Create(intrinsics->func_llvm_fabs_f32, args[0],
                                    "tmp1", currentBlock);
    } else if (Ty == Type::getDoubleTy(*llvmContext)) {
      return llvm::CallInst::Create(intrinsics->func_llvm_fabs_f64, args[0],
                                    "tmp1", currentBlock);
    }
  } else if (name->equals(loader->sqrt)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_sqrt_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->sin)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_sin_f64, args[0], 
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->cos)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_cos_f64, args[0], 
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->tan)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_tan_f64, args[0], 
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->asin)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_asin_f64, args[0], 
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->acos)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_acos_f64, args[0], 
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->atan)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_atan_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->atan2)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_atan2_f64, 
                                  args, "tmp1", currentBlock);
  } else if (name->equals(loader->exp)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_exp_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->log)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_log_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->pow)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_pow_f64, args,
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->ceil)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_ceil_f64, args[0], "tmp1",
                                  currentBlock);
  } else if (name->equals(loader->floor)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_floor_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->rint)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_rint_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->cbrt)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_cbrt_f64, args[0], "tmp1",
                                  currentBlock);
  } else if (name->equals(loader->cosh)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_cosh_f64, args[0], "tmp1",
                                  currentBlock);
  } else if (name->equals(loader->expm1)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_expm1_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->hypot)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_hypot_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->log10)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_log10_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->log1p)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_log1p_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->sinh)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_sinh_f64, args[0],
                                  "tmp1", currentBlock);
  } else if (name->equals(loader->tanh)) {
    return llvm::CallInst::Create(intrinsics->func_llvm_tanh_f64, args[0],
                                  "tmp1", currentBlock);
  }
  
  return 0;
}


Instruction* JavaJIT::lowerFloatOps(const UTF8* name, 
                                    std::vector<Value*>& args) {
  JnjvmBootstrapLoader* loader = compilingClass->classLoader->bootstrapLoader;
  if (name->equals(loader->floatToRawIntBits)) {
    return new BitCastInst(args[0], Type::getInt32Ty(*llvmContext), "", currentBlock);
  } else if (name->equals(loader->intBitsToFloat)) {
    return new BitCastInst(args[0], Type::getFloatTy(*llvmContext), "", currentBlock);
  }
  return NULL;
}

Instruction* JavaJIT::lowerDoubleOps(const UTF8* name, 
                                    std::vector<Value*>& args) {
  JnjvmBootstrapLoader* loader = compilingClass->classLoader->bootstrapLoader;
  if (name->equals(loader->doubleToRawLongBits)) {
    return new BitCastInst(args[0], Type::getInt64Ty(*llvmContext), "", currentBlock);
  } else if (name->equals(loader->longBitsToDouble)) {
    return new BitCastInst(args[0], Type::getDoubleTy(*llvmContext), "", currentBlock);
  }
  return NULL;
}


Instruction* JavaJIT::invokeInline(JavaMethod* meth, 
                                   std::vector<Value*>& args,
                                   bool customized) {
  JavaJIT jit(TheCompiler, meth, llvmFunction, customized ? customizeFor : NULL);
  jit.unifiedUnreachable = unifiedUnreachable;
  jit.inlineMethods = inlineMethods;
  jit.inlineMethods[meth] = true;
  jit.inlining = true;
  jit.DbgSubprogram = DbgSubprogram;
#if DEBUG
  static int inlineNb = 0;
  fprintf(stderr, "inline compile %d %s.%s%s from %s.%s (%d)\n", inlineNb++,
              UTF8Buffer(meth->classDef->name).cString(),
              UTF8Buffer(meth->name).cString(),
              UTF8Buffer(meth->getSignature()->keyName).cString(),
              UTF8Buffer(compilingClass->name).cString(),
              UTF8Buffer(compilingMethod->name).cString(),
              customized);
#endif
  
  Instruction* ret = jit.inlineCompile(currentBlock, 
                                       currentExceptionBlock, args);
  inlineMethods[meth] = false;
  return ret;
}

void JavaJIT::invokeSpecial(uint16 index) {
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  JavaMethod* meth = 0;
  Signdef* signature = 0;
  const UTF8* name = 0;
  const UTF8* cl = 0;

  ctpInfo->nameOfStaticOrSpecialMethod(index, cl, name, signature);
  LLVMSignatureInfo* LSI = TheCompiler->getSignatureInfo(signature);
  FunctionType* virtualType = LSI->getVirtualType();
  meth = ctpInfo->infoOfStaticOrSpecialMethod(index, ACC_VIRTUAL, signature);
  bool thisReference =
    isThisReference(stackSize() - signature->getNumberOfSlots() - 1);

  Value* func = 0;
  bool needsInit = false;
  if (TheCompiler->needsCallback(meth, NULL, &needsInit)) {
    if (needsInit) {
      // Make sure the class is loaded before materializing the method.
      uint32 clIndex = ctpInfo->getClassIndexFromMethod(index);
      UserCommonClass* cl = 0;
      Value* Cl = getResolvedCommonClass(clIndex, false, &cl);
      if (cl == NULL) {
        CallInst::Create(intrinsics->ForceLoadedCheckFunction, Cl, "",
                         currentBlock);
      }
    }
    func = TheCompiler->addCallback(compilingClass, index, signature, false,
                                    currentBlock);
  } else {
    func = TheCompiler->getMethod(meth, NULL);
  }

  std::vector<Value*> args;
  FunctionType::param_iterator it  = virtualType->param_end();
  makeArgs(it, index, args, signature->nbArguments + 1);
  if (!thisReference) JITVerifyNull(args[0]);
  
  if (meth == compilingClass->classLoader->bootstrapLoader->upcalls->InitObject) {
    return;
  }

  llvm::Instruction* val = 0;
  if (meth && canBeInlined(meth, false)) {
    val = invokeInline(meth, args, false);
  } else {
    val = invoke(func, args, "", currentBlock);
  }
  
  Type* retType = virtualType->getReturnType();
  if (retType != Type::getVoidTy(*llvmContext)) {
    if (retType == intrinsics->JavaObjectType) {
      JnjvmClassLoader* JCL = compilingClass->classLoader;
      push(val, false, signature->getReturnType()->findAssocClass(JCL));
    } else {
      push(val, signature->getReturnType()->isUnsigned());
      if (retType == Type::getDoubleTy(*llvmContext) ||
          retType == Type::getInt64Ty(*llvmContext)) {
        push(intrinsics->constantZero, false);
      }
    }
  }
}

void JavaJIT::invokeStatic(uint16 index) {
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  Signdef* signature = 0;
  const UTF8* name = 0;
  const UTF8* className = 0;
  ctpInfo->nameOfStaticOrSpecialMethod(index, className, name, signature);
  LLVMSignatureInfo* LSI = TheCompiler->getSignatureInfo(signature);
  FunctionType* staticType = LSI->getStaticType();
  ctpInfo->markAsStaticCall(index);
  JnjvmBootstrapLoader* loader = compilingClass->classLoader->bootstrapLoader;
  llvm::Instruction* val = 0;
  
  if (className->equals(loader->stackWalkerName)) {
    callsStackWalker = true;
  }

  JavaMethod* meth = ctpInfo->infoOfStaticOrSpecialMethod(index, ACC_STATIC,
                                                          signature);
    

  uint32 clIndex = ctpInfo->getClassIndexFromMethod(index);
  UserClass* cl = 0;
  Value* Cl = getResolvedClass(clIndex, true, true, &cl);
  if (!meth || (cl && needsInitialisationCheck(cl))) {
    CallInst::Create(intrinsics->ForceInitialisationCheckFunction, Cl, "",
                     currentBlock);
  }
  
  Value* func = 0;
  bool needsInit = false;
  if (TheCompiler->needsCallback(meth, NULL, &needsInit)) {
    func = TheCompiler->addCallback(compilingClass, index, signature,
                                    true, currentBlock);
  } else {
    func = TheCompiler->getMethod(meth, NULL);
  }

  std::vector<Value*> args; // size = [signature->nbIn + 2]; 
  FunctionType::param_iterator it  = staticType->param_end();
  makeArgs(it, index, args, signature->nbArguments);

  if (className->equals(loader->mathName)) {
    val = lowerMathOps(name, args);
  } else if (className->equals(loader->VMFloatName)) {
    val = lowerFloatOps(name, args);
  } else if (className->equals(loader->VMDoubleName)) {
    val = lowerDoubleOps(name, args);
  }
    
  if (val == NULL) {
    if (meth != NULL && canBeInlined(meth, false)) {
      val = invokeInline(meth, args, false);
    } else {
      val = invoke(func, args, "", currentBlock);
    }
  }

  Type* retType = staticType->getReturnType();
  if (retType != Type::getVoidTy(*llvmContext)) {
    if (retType == intrinsics->JavaObjectType) {
      JnjvmClassLoader* JCL = compilingClass->classLoader;
      push(val, false, signature->getReturnType()->findAssocClass(JCL));
    } else {
      push(val, signature->getReturnType()->isUnsigned());
      if (retType == Type::getDoubleTy(*llvmContext) ||
          retType == Type::getInt64Ty(*llvmContext)) {
        push(intrinsics->constantZero, false);
      }
    }
  }
}

Value* JavaJIT::getConstantPoolAt(uint32 index, Function* resolver,
                                  Type* returnType,
                                  Value* additionalArg, bool doThrow) {

// This makes unswitch loop very unhappy time-wise, but makes GVN happy
// number-wise. IMO, it's better to have this than Unswitch.
  JavaConstantPool* ctp = compilingClass->ctpInfo;
  Value* CTP = TheCompiler->getResolvedConstantPool(ctp);
  Value* Cl = TheCompiler->getNativeClass(compilingClass);

  std::vector<Value*> Args;
  Args.push_back(resolver);
  Args.push_back(CTP);
  Args.push_back(Cl);
  Args.push_back(ConstantInt::get(Type::getInt32Ty(*llvmContext), index));
  if (additionalArg) Args.push_back(additionalArg);

  Value* res = 0;
  if (doThrow) {
    res = invoke(intrinsics->GetConstantPoolAtFunction, Args, "",
                 currentBlock);
  } else {
    res = CallInst::Create(intrinsics->GetConstantPoolAtFunction, Args,
                           "", currentBlock);
  }
  
  Type* realType = 
    intrinsics->GetConstantPoolAtFunction->getReturnType();
  if (returnType == Type::getInt32Ty(*llvmContext)) {
    return new PtrToIntInst(res, Type::getInt32Ty(*llvmContext), "", currentBlock);
  } else if (returnType != realType) {
    return new BitCastInst(res, returnType, "", currentBlock);
  } 
  
  return res;
}

Value* JavaJIT::getResolvedCommonClass(uint16 index, bool doThrow,
                                       UserCommonClass** alreadyResolved) {
    
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  CommonClass* cl = ctpInfo->getMethodClassIfLoaded(index);
  Value* node = 0;
  if (cl && (!cl->isClass() || cl->asClass()->isResolved())) {
    if (alreadyResolved) *alreadyResolved = cl;
    node = TheCompiler->getNativeClass(cl);
    // Since we only allocate for array classes that we own and
    // ony primitive arrays are already allocated, verify that the class
    // array is not external.
    if (TheCompiler->isStaticCompiling() && cl->isArray() && 
        node->getType() != intrinsics->JavaClassArrayType) {
      node = new LoadInst(node, "", currentBlock);
    }
    if (node->getType() != intrinsics->JavaCommonClassType) {
      node = new BitCastInst(node, intrinsics->JavaCommonClassType, "",
                             currentBlock);
    }
  } else {
    node = getConstantPoolAt(index, intrinsics->ClassLookupFunction,
                             intrinsics->JavaCommonClassType, 0, doThrow);
  }
  
  return node;
}

Value* JavaJIT::getResolvedClass(uint16 index, bool clinit, bool doThrow,
                                 Class** alreadyResolved) {
    
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  Class* cl = (Class*)(ctpInfo->getMethodClassIfLoaded(index));
  Value* node = 0;
  bool needsInit = true;
  if (cl && cl->isResolved()) {
    if (alreadyResolved) (*alreadyResolved) = cl;
    node = TheCompiler->getNativeClass(cl);
    needsInit = needsInitialisationCheck(cl);
  } else {
    node = getConstantPoolAt(index, intrinsics->ClassLookupFunction,
                             intrinsics->JavaClassType, 0, doThrow);
  }
 

  if (clinit && needsInit) {
    if (node->getType() != intrinsics->JavaClassType) {
      node = new BitCastInst(node, intrinsics->JavaClassType, "", currentBlock);
    }
    return invoke(intrinsics->InitialisationCheckFunction, node, "",
                  currentBlock);
  } else {
    return node;
  }
}

void JavaJIT::invokeNew(uint16 index) {
  
  Class* cl = 0;
  Value* Cl = getResolvedClass(index, true, true, &cl);
          
  Value* VT = 0;
  Value* Size = 0;
  
  if (cl) {
    VT = TheCompiler->getVirtualTable(cl->virtualVT);
    LLVMClassInfo* LCI = TheCompiler->getClassInfo(cl);
    Size = LCI->getVirtualSize();
    
    bool needsCheck = needsInitialisationCheck(cl);
    if (needsCheck) {
      Cl = invoke(intrinsics->ForceInitialisationCheckFunction, Cl, "",
                  currentBlock);
    }

  } else {
    VT = CallInst::Create(intrinsics->GetVTFromClassFunction, Cl, "",
                          currentBlock);
    Size = CallInst::Create(intrinsics->GetObjectSizeFromClassFunction, Cl,
                            "", currentBlock);
  }
 
  VT = new BitCastInst(VT, intrinsics->ptrType, "", currentBlock);
  Instruction* val = invoke(cl ? intrinsics->AllocateFunction :
                           intrinsics->AllocateUnresolvedFunction,
                           Size, VT, "", currentBlock);

  addHighLevelType(val, cl ? cl : upcalls->OfObject);
  Instruction* res = new BitCastInst(val, intrinsics->JavaObjectType, "", currentBlock);
  push(res, false, cl ? cl : upcalls->OfObject);

  // Make sure to add the object to the finalization list after it has been
  // pushed.
  if (cl && cl->virtualVT->hasDestructor()) {
    CallInst::Create(intrinsics->AddFinalizationCandidate, val, "", currentBlock);
  }
}

Value* JavaJIT::ldResolved(uint16 index, bool stat, Value* object, 
                           Type* fieldTypePtr, bool thisReference) {
  JavaConstantPool* info = compilingClass->ctpInfo;
  
  JavaField* field = info->lookupField(index, stat);
  if (field && field->classDef->isResolved()) {
    LLVMClassInfo* LCI = TheCompiler->getClassInfo(field->classDef);
    LLVMFieldInfo* LFI = TheCompiler->getFieldInfo(field);
    Type* type = NULL;
    if (stat) {
      type = LCI->getStaticType();
      Value* Cl = TheCompiler->getNativeClass(field->classDef);
      bool needsCheck = needsInitialisationCheck(field->classDef);
      if (needsCheck) {
        Cl = invoke(intrinsics->InitialisationCheckFunction, Cl, "",
                    currentBlock);
        CallInst::Create(intrinsics->ForceInitialisationCheckFunction, Cl, "",
                         currentBlock);
      }

      object = TheCompiler->getStaticInstance(field->classDef);
    } else {
      object = new LoadInst(
          object, "", false, currentBlock);
      if (!thisReference) JITVerifyNull(object);
      type = LCI->getVirtualType();
    }
    
    Value* objectConvert = new BitCastInst(object, type, "", currentBlock);

    Value* args[2] = { intrinsics->constantZero, LFI->getOffset() };
    Value* ptr = llvm::GetElementPtrInst::Create(objectConvert, args, "",
                                                 currentBlock);
    return ptr;
  }

  Type* Pty = intrinsics->arrayPtrType;
  Constant* zero = intrinsics->constantZero;
    
  Function* func = stat ? intrinsics->StaticFieldLookupFunction :
                          intrinsics->VirtualFieldLookupFunction;
    
  Type* returnType = NULL;
  if (stat) {
    returnType = intrinsics->ptrType;
  } else {
    returnType = Type::getInt32Ty(*llvmContext);
  }

  Value* ptr = getConstantPoolAt(index, func, returnType, 0, true);
  if (!stat) {
    object = new LoadInst(
        object, "", false, currentBlock);
    if (!thisReference) JITVerifyNull(object);
    Value* tmp = new BitCastInst(object, Pty, "", currentBlock);
    Value* args[2] = { zero, ptr };
    ptr = GetElementPtrInst::Create(tmp, args, "", currentBlock);
  }
    
  return new BitCastInst(ptr, fieldTypePtr, "", currentBlock);
}

void JavaJIT::convertValue(Value*& val, Type* t1, BasicBlock* currentBlock,
                           bool usign) {
  Type* t2 = val->getType();
  if (t1 != t2) {
    if (t1->isIntegerTy() && t2->isIntegerTy()) {
      if (t2->getPrimitiveSizeInBits() < t1->getPrimitiveSizeInBits()) {
        if (usign) {
          val = new ZExtInst(val, t1, "", currentBlock);
        } else {
          val = new SExtInst(val, t1, "", currentBlock);
        }
      } else {
        val = new TruncInst(val, t1, "", currentBlock);
      }    
    } else if (t1->isFloatTy() && t2->isFloatTy()) {
      if (t2->getPrimitiveSizeInBits() < t1->getPrimitiveSizeInBits()) {
        val = new FPExtInst(val, t1, "", currentBlock);
      } else {
        val = new FPTruncInst(val, t1, "", currentBlock);
      }    
    } else if (isa<PointerType>(t1) && isa<PointerType>(t2)) {
      val = new BitCastInst(val, t1, "", currentBlock);
    }    
  }
}
 

void JavaJIT::setStaticField(uint16 index) {
  Typedef* sign = compilingClass->ctpInfo->infoOfField(index);
  LLVMAssessorInfo& LAI = TheCompiler->getTypedefInfo(sign);
  Type* type = LAI.llvmType;
   
  Value* ptr = ldResolved(index, true, NULL, LAI.llvmTypePtr);

  Value* val = pop(); 
  if (type == Type::getInt64Ty(*llvmContext) ||
      type == Type::getDoubleTy(*llvmContext)) {
    val = pop();
  }

  if (type != val->getType()) { // int1, int8, int16
    convertValue(val, type, currentBlock, false);
  }
  
  if (mvm::Collector::needsNonHeapWriteBarrier() && type == intrinsics->JavaObjectType) {
    ptr = new BitCastInst(ptr, intrinsics->ptrPtrType, "", currentBlock);
    val = new BitCastInst(val, intrinsics->ptrType, "", currentBlock);
    Value* args[2] = { ptr, val };
    CallInst::Create(intrinsics->NonHeapWriteBarrierFunction, args, "", currentBlock);
  } else {
    new StoreInst(val, ptr, false, currentBlock);
  }
}

void JavaJIT::getStaticField(uint16 index) {
  Typedef* sign = compilingClass->ctpInfo->infoOfField(index);
  LLVMAssessorInfo& LAI = TheCompiler->getTypedefInfo(sign);
  Type* type = LAI.llvmType;
  
  Value* ptr = ldResolved(index, true, NULL, LAI.llvmTypePtr);
  
  bool final = false;
  JnjvmBootstrapLoader* JBL = compilingClass->classLoader->bootstrapLoader;
  if (!compilingMethod->name->equals(JBL->clinitName)) {
    JavaField* field = compilingClass->ctpInfo->lookupField(index, true);
    if (field && field->classDef->isReady()) final = isFinal(field->access);
    if (final) {
      if (sign->isPrimitive()) {
        const PrimitiveTypedef* prim = (PrimitiveTypedef*)sign;
        if (prim->isInt()) {
          sint32 val = field->getStaticInt32Field();
          push(ConstantInt::get(Type::getInt32Ty(*llvmContext), val), false);
        } else if (prim->isByte()) {
          sint8 val = (sint8)field->getStaticInt8Field();
          push(ConstantInt::get(Type::getInt8Ty(*llvmContext), val), false);
        } else if (prim->isBool()) {
          uint8 val = (uint8)field->getStaticInt8Field();
          push(ConstantInt::get(Type::getInt8Ty(*llvmContext), val), true);
        } else if (prim->isShort()) {
          sint16 val = (sint16)field->getStaticInt16Field();
          push(ConstantInt::get(Type::getInt16Ty(*llvmContext), val), false);
        } else if (prim->isChar()) {
          uint16 val = (uint16)field->getStaticInt16Field();
          push(ConstantInt::get(Type::getInt16Ty(*llvmContext), val), true);
        } else if (prim->isLong()) {
          sint64 val = (sint64)field->getStaticLongField();
          push(ConstantInt::get(Type::getInt64Ty(*llvmContext), val), false);
        } else if (prim->isFloat()) {
          float val = (float)field->getStaticFloatField();
          push(ConstantFP::get(Type::getFloatTy(*llvmContext), val), false);
        } else if (prim->isDouble()) {
          double val = (double)field->getStaticDoubleField();
          push(ConstantFP::get(Type::getDoubleTy(*llvmContext), val), false);
        } else {
          abort();
        }
      } else {
        if (TheCompiler->isStaticCompiling() && !TheCompiler->useCooperativeGC()) {
          JavaObject* val = field->getStaticObjectField();
          JnjvmClassLoader* JCL = field->classDef->classLoader;
          Value* V = TheCompiler->getFinalObject(val, sign->assocClass(JCL));
          CommonClass* cl = mvm::Collector::begOf(val) ?
              JavaObject::getClass(val) : NULL;
          push(V, false, cl);
        } else {
          // Do not call getFinalObject, as the object may move in-between two
          // loads of this static.
          Value* V = new LoadInst(ptr, "", currentBlock);
          JnjvmClassLoader* JCL = compilingClass->classLoader;
          push(V, false, sign->findAssocClass(JCL));
        } 
      }
    }
  }

  if (!final) {
    JnjvmClassLoader* JCL = compilingClass->classLoader;
    CommonClass* cl = sign->findAssocClass(JCL);
    push(new LoadInst(ptr, "", currentBlock), sign->isUnsigned(), cl);
  }
  if (type == Type::getInt64Ty(*llvmContext) ||
      type == Type::getDoubleTy(*llvmContext)) {
    push(intrinsics->constantZero, false);
  }
}

void JavaJIT::setVirtualField(uint16 index) {
  Typedef* sign = compilingClass->ctpInfo->infoOfField(index);
  LLVMAssessorInfo& LAI = TheCompiler->getTypedefInfo(sign);
  Type* type = LAI.llvmType;
  int stackIndex = currentStackIndex - 2;
  if (type == Type::getInt64Ty(*llvmContext) ||
      type == Type::getDoubleTy(*llvmContext)) {
    stackIndex--;
  }
  Value* object = objectStack[stackIndex];
  bool thisReference = isThisReference(stackIndex);
  Value* ptr = ldResolved(index, false, object, LAI.llvmTypePtr, thisReference);

  Value* val = pop();
  if (type == Type::getInt64Ty(*llvmContext) ||
      type == Type::getDoubleTy(*llvmContext)) {
    val = pop();
  }
  pop(); // Pop the object
  
  if (type != val->getType()) { // int1, int8, int16
    convertValue(val, type, currentBlock, false);
  }
  
  if (mvm::Collector::needsWriteBarrier() && type == intrinsics->JavaObjectType) {
    ptr = new BitCastInst(ptr, intrinsics->ptrPtrType, "", currentBlock);
    val = new BitCastInst(val, intrinsics->ptrType, "", currentBlock);
    object = new LoadInst(object, "", false, currentBlock);
    object = new BitCastInst(object, intrinsics->ptrType, "", currentBlock);
    Value* args[3] = { object, ptr, val };
    CallInst::Create(intrinsics->FieldWriteBarrierFunction, args, "", currentBlock);
  } else {
    new StoreInst(val, ptr, false, currentBlock);
  }
}

void JavaJIT::getVirtualField(uint16 index) {
  Typedef* sign = compilingClass->ctpInfo->infoOfField(index);
  JnjvmClassLoader* JCL = compilingClass->classLoader;
  CommonClass* cl = sign->findAssocClass(JCL);
  
  LLVMAssessorInfo& LAI = TheCompiler->getTypedefInfo(sign);
  Type* type = LAI.llvmType;
  Value* obj = objectStack[currentStackIndex - 1];
  bool thisReference = isThisReference(currentStackIndex - 1);
  pop(); // Pop the object
  
  Value* ptr = ldResolved(index, false, obj, LAI.llvmTypePtr, thisReference);
  
  JnjvmBootstrapLoader* JBL = compilingClass->classLoader->bootstrapLoader;
  bool final = false;
  
  // In init methods, the fields have not been set yet.
  if (!compilingMethod->name->equals(JBL->initName)) {
    JavaField* field = compilingClass->ctpInfo->lookupField(index, false);
    if (field) {
      final = isFinal(field->access) && sign->isPrimitive();
    }
    if (final) {
      Function* F = 0;
      assert(sign->isPrimitive());
      const PrimitiveTypedef* prim = (PrimitiveTypedef*)sign;
      if (prim->isInt()) {
        F = intrinsics->GetFinalInt32FieldFunction;
      } else if (prim->isByte()) {
        F = intrinsics->GetFinalInt8FieldFunction;
      } else if (prim->isBool()) {
        F = intrinsics->GetFinalInt8FieldFunction;
      } else if (prim->isShort()) {
        F = intrinsics->GetFinalInt16FieldFunction;
      } else if (prim->isChar()) {
        F = intrinsics->GetFinalInt16FieldFunction;
      } else if (prim->isLong()) {
        F = intrinsics->GetFinalLongFieldFunction;
      } else if (prim->isFloat()) {
        F = intrinsics->GetFinalFloatFieldFunction;
      } else if (prim->isDouble()) {
        F = intrinsics->GetFinalDoubleFieldFunction;
      } else {
        abort();
      }
      push(CallInst::Create(F, ptr, "", currentBlock), sign->isUnsigned(), cl);
    }
  }
 
  if (!final) push(new LoadInst(ptr, "", currentBlock), sign->isUnsigned(), cl);
  if (type == Type::getInt64Ty(*llvmContext) ||
      type == Type::getDoubleTy(*llvmContext)) {
    push(intrinsics->constantZero, false);
  }
}


void JavaJIT::invokeInterface(uint16 index) {
  
  // Do the usual
  JavaConstantPool* ctpInfo = compilingClass->ctpInfo;
  const UTF8* name = 0;
  Signdef* signature = ctpInfo->infoOfInterfaceOrVirtualMethod(index, name);
  bool thisReference =
    isThisReference(stackSize() - signature->getNumberOfSlots() - 1);
  
  LLVMSignatureInfo* LSI = TheCompiler->getSignatureInfo(signature);
  FunctionType* virtualType = LSI->getVirtualType();
  PointerType* virtualPtrType = LSI->getVirtualPtrType();
 
  Type* retType = virtualType->getReturnType();
   
  CommonClass* cl = 0;
  JavaMethod* meth = 0;
  ctpInfo->infoOfMethod(index, ACC_VIRTUAL, cl, meth);
  Value* Meth = 0;

  if (meth) {
    Meth = TheCompiler->getMethodInClass(meth);
  } else {
    Meth = getConstantPoolAt(index, intrinsics->InterfaceLookupFunction,
                             intrinsics->JavaMethodType, 0, true);
  }

  uint32_t tableIndex = InterfaceMethodTable::getIndex(name, signature->keyName);
  Constant* Index = ConstantInt::get(Type::getInt32Ty(*llvmContext),
                                     tableIndex);
  Value* targetObject = getTarget(signature);
  targetObject = new LoadInst(
          targetObject, "", false, currentBlock);
  if (!thisReference) JITVerifyNull(targetObject);
  // TODO: The following code needs more testing.
#if 0
  BasicBlock* endBlock = createBasicBlock("end interface invoke");
  PHINode * node = PHINode::Create(virtualPtrType, "", endBlock);

  BasicBlock* label_bb = createBasicBlock("bb");
  BasicBlock* label_bb4 = createBasicBlock("bb4");
  BasicBlock* label_bb6 = createBasicBlock("bb6");
  BasicBlock* label_bb7 = createBasicBlock("bb7");
    
  // Block entry (label_entry)
  Value* VT = CallInst::Create(intrinsics->GetVTFunction, targetObject, "",
                               currentBlock);
  Value* IMT = CallInst::Create(intrinsics->GetIMTFunction, VT, "",
                                currentBlock);


  Value* indices[2] = { intrinsics->constantZero, Index };
  Instruction* ptr_18 = GetElementPtrInst::Create(IMT, indices, "",
                                                  currentBlock);
  Instruction* int32_19 = new LoadInst(ptr_18, "", false, currentBlock);
  int32_19 = new PtrToIntInst(int32_19, intrinsics->pointerSizeType, "",
                              currentBlock);
  Value* one = ConstantInt::get(intrinsics->pointerSizeType, 1);
  Value* zero = ConstantInt::get(intrinsics->pointerSizeType, 0);
  BinaryOperator* int32_20 = BinaryOperator::Create(Instruction::And, int32_19,
                                                    one, "", currentBlock);
  ICmpInst* int1_toBool = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ,
                                       int32_20, zero, "toBool");
  BranchInst::Create(label_bb, label_bb4, int1_toBool, currentBlock);
    
  // Block bb (label_bb)
  currentBlock = label_bb;
  CastInst* ptr_22 = new IntToPtrInst(int32_19, virtualPtrType, "", currentBlock);
  
  node->addIncoming(ptr_22, currentBlock);
  BranchInst::Create(endBlock, currentBlock);
    
  // Block bb4 (label_bb4)
  currentBlock = label_bb4;
  Constant* MinusTwo = ConstantInt::get(intrinsics->pointerSizeType, -2);
  BinaryOperator* int32_25 = BinaryOperator::Create(Instruction::And, int32_19,
                                                    MinusTwo, "", currentBlock);
  PointerType* Ty = PointerType::getUnqual(intrinsics->JavaMethodType);
  CastInst* ptr_26 = new IntToPtrInst(int32_25, Ty, "", currentBlock);
  LoadInst* int32_27 = new LoadInst(ptr_26, "", false, currentBlock);
  ICmpInst* int1_28 = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, int32_27,
                                   Meth, "");
  BranchInst::Create(label_bb6, label_bb7, int1_28, currentBlock);
    
  // Block bb6 (label_bb6)
  currentBlock = label_bb6;
  PHINode* ptr_table_0_lcssa = PHINode::Create(Ty, 2, "table.0.lcssa",
                                               currentBlock);
  ptr_table_0_lcssa->reserveOperandSpace(2);
  ptr_table_0_lcssa->addIncoming(ptr_26, label_bb4);
   
  GetElementPtrInst* ptr_31 = GetElementPtrInst::Create(ptr_table_0_lcssa,
                                                        intrinsics->constantOne, "",
                                                        currentBlock);

  LoadInst* int32_32 = new LoadInst(ptr_31, "", false, currentBlock);
  CastInst* ptr_33 = new BitCastInst(int32_32, virtualPtrType, "",
                                     currentBlock);
  node->addIncoming(ptr_33, currentBlock);

  BranchInst::Create(endBlock, currentBlock);
    
  // Block bb7 (label_bb7)
  currentBlock = label_bb7;
  PHINode* int32_indvar = PHINode::Create(Type::getInt32Ty(*llvmContext),
                                          "indvar", currentBlock);
  int32_indvar->reserveOperandSpace(2);
  int32_indvar->addIncoming(intrinsics->constantZero, label_bb4);
    
  BinaryOperator* int32_table_010_rec =
    BinaryOperator::Create(Instruction::Shl, int32_indvar, intrinsics->constantOne,
                           "table.010.rec", currentBlock);

  BinaryOperator* int32__rec =
    BinaryOperator::Create(Instruction::Add, int32_table_010_rec,
                           intrinsics->constantTwo, ".rec", currentBlock);
  GetElementPtrInst* ptr_37 = GetElementPtrInst::Create(ptr_26, int32__rec, "",
                                                        currentBlock);
  LoadInst* int32_38 = new LoadInst(ptr_37, "", false, currentBlock);
  ICmpInst* int1_39 = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, int32_38,
                                   Meth, "");
  BinaryOperator* int32_indvar_next =
    BinaryOperator::Create(Instruction::Add, int32_indvar, intrinsics->constantOne,
                           "indvar.next", currentBlock);
  BranchInst::Create(label_bb6, label_bb7, int1_39, currentBlock);
  
  int32_indvar->addIncoming(int32_indvar_next, currentBlock);
  ptr_table_0_lcssa->addIncoming(ptr_37, currentBlock);
      
  currentBlock = endBlock;
#else
  std::vector<Value*> Args;
  Args.push_back(targetObject);
  Args.push_back(Meth);
  Args.push_back(Index);
  Value* node = invoke(intrinsics->ResolveInterfaceFunction,
                       Args, "invokeinterface", currentBlock);
  node = new BitCastInst(node, virtualPtrType, "", currentBlock);
#endif

  std::vector<Value*> args; // size = [signature->nbIn + 3];
  FunctionType::param_iterator it  = virtualType->param_end();
  makeArgs(it, index, args, signature->nbArguments + 1);
  Value* ret = invoke(node, args, "", currentBlock);
  if (retType != Type::getVoidTy(*llvmContext)) {
    if (ret->getType() == intrinsics->JavaObjectType) {
      JnjvmClassLoader* JCL = compilingClass->classLoader;
      push(ret, false, signature->getReturnType()->findAssocClass(JCL));
    } else {
      push(ret, signature->getReturnType()->isUnsigned());
      if (retType == Type::getDoubleTy(*llvmContext) ||
          retType == Type::getInt64Ty(*llvmContext)) {
        push(intrinsics->constantZero, false);
      }
    }
  }
}

DebugLoc JavaJIT::CreateLocation() {
  DebugLoc DL = DebugLoc::get(currentBytecodeIndex, 0, DbgSubprogram);
  return DL;
}

Instruction* JavaJIT::invoke(Value *F, std::vector<llvm::Value*>& args,
                       const char* Name,
                       BasicBlock *InsertAtEnd) {
  assert(!inlining);
 
  BasicBlock* ifException = NULL;
  if (jmpBuffer != NULL) {
    BasicBlock* doCall = createBasicBlock("");
    ifException = createBasicBlock("");
    Instruction* check = CallInst::Create(intrinsics->SetjmpFunction, jmpBuffer, "", currentBlock);
    check = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, check, intrinsics->constantZero, "");
    BranchInst::Create(doCall, ifException, check, currentBlock);
    currentBlock = doCall;
    CallInst::Create(intrinsics->RegisterSetjmpFunction, jmpBuffer, "", currentBlock);
  }

  Instruction* res = CallInst::Create(F, args, Name,  currentBlock);
  DebugLoc DL = CreateLocation();
  res->setDebugLoc(DL);
  
  if (jmpBuffer != NULL) {
    CallInst::Create(intrinsics->UnregisterSetjmpFunction, jmpBuffer, "", currentBlock);
    BasicBlock* ifNormal = createBasicBlock("no exception block");
    BranchInst::Create(ifNormal, currentBlock);

    currentBlock = ifException;
    CallInst::Create(intrinsics->UnregisterSetjmpFunction, jmpBuffer, "", currentBlock);
 
    if (!currentExceptionBlock->empty()) {
      // Get the Java exception.
      Value* javaExceptionPtr = getJavaExceptionPtr(getJavaThreadPtr(getMutatorThreadPtr())); 
      Value* obj = new LoadInst(javaExceptionPtr, "", currentBlock);
      Instruction* insn = currentExceptionBlock->begin();
      PHINode* node = dyn_cast<PHINode>(insn);
      if (node) node->addIncoming(obj, currentBlock);
    } 
    BranchInst::Create(currentExceptionBlock, currentBlock);
    currentBlock = ifNormal; 
  }

  return res;
}

Instruction* JavaJIT::invoke(Value *F, Value* arg1, const char* Name,
                       BasicBlock *InsertAtEnd) {
  std::vector<Value*> args;
  args.push_back(arg1);
  return invoke(F, args, Name, InsertAtEnd);
}

Instruction* JavaJIT::invoke(Value *F, Value* arg1, Value* arg2,
                       const char* Name, BasicBlock *InsertAtEnd) {
  std::vector<Value*> args;
  args.push_back(arg1);
  args.push_back(arg2);
  return invoke(F, args, Name, InsertAtEnd);
}

Instruction* JavaJIT::invoke(Value *F, const char* Name,
                       BasicBlock *InsertAtEnd) {
  std::vector<Value*> args;
  return invoke(F, args, Name, InsertAtEnd);
}

void JavaJIT::throwException(Value* obj, bool checkNull) {
  if (checkNull) JITVerifyNull(obj);
  if (nbHandlers == 0) {
    CallInst::Create(intrinsics->ThrowExceptionFunction, obj, "", currentBlock);
    new UnreachableInst(*llvmContext, currentBlock);
  } else {
    Value* javaExceptionPtr = getJavaExceptionPtr(getJavaThreadPtr(getMutatorThreadPtr()));
    new StoreInst(obj, javaExceptionPtr, currentBlock);

    Instruction* insn = currentExceptionBlock->begin();
    PHINode* node = dyn_cast<PHINode>(insn);
    if (node) node->addIncoming(obj, currentBlock);
    BranchInst::Create(currentExceptionBlock, currentBlock);
  }
}

void JavaJIT::throwRuntimeException(llvm::Function* F, Value* arg1) {
  Value* args[1] = { arg1 };
  throwRuntimeException(F, args, 1);
}

void JavaJIT::throwRuntimeException(llvm::Function* F, Value** args, uint32 nbArgs) {
  Instruction* obj = CallInst::Create(F, ArrayRef<Value*>(args, nbArgs), "", currentBlock);
  DebugLoc DL = CreateLocation();
  obj->setDebugLoc(DL);
  throwException(obj, false);
}

/// Handler - This class represents an exception handler. It is only needed
/// when parsing the .class file in the JIT, therefore it is only defined
/// here. The readExceptionTable function is the only function that makes
/// use of this class.
struct Handler {
  
  /// startpc - The bytecode number that begins the try clause.
  uint32 startpc;

  /// endpc - The bytecode number that ends the try clause.
  uint32 endpc;

  /// handlerpc - The bytecode number where the handler code starts.
  uint32 handlerpc;

  /// catche - Index in the constant pool of the exception class.
  uint16 catche;

  /// catchClass - The class of the exception: it must always be loaded before
  /// reading the exception table so that we do not throw an exception
  /// when compiling.
  UserClass* catchClass;

  /// tester - The basic block that tests if the exception is handled by this
  /// handler. If the handler is not the first of a list of handlers with the
  /// same range, than this block is the catcher block. Otherwise, it is the
  /// destination of the catcher block and of the handlers that do not handler
  /// the exception.
  llvm::BasicBlock* tester;

  /// javaHandler - The Java code that handles the exception. At this point, we
  /// know we have caught and are handling the exception. The Java exception
  /// object is the PHI node that begins this block.
  llvm::BasicBlock* javaHandler;

};

unsigned JavaJIT::readExceptionTable(Reader& reader, uint32 codeLen) {

  // This function uses currentBlock to simplify things. We save the current
  // value of currentBlock to restore it at the end of the function
  BasicBlock* temp = currentBlock;
  
  sint16 nbe = reader.readU2();
  sint16 sync = isSynchro(compilingMethod->access) ? 1 : 0;
  nbe += sync;
 
  mvm::ThreadAllocator allocator;
  // Loop over all handlers in the bytecode to initialize their values.
  Handler* handlers =
      (Handler*)allocator.Allocate(sizeof(Handler) * (nbe - sync));
  for (uint16 i = 0; i < nbe - sync; ++i) {
    Handler* ex   = &handlers[i];
    ex->startpc   = reader.readU2();
    ex->endpc     = reader.readU2();
    ex->handlerpc = reader.readU2();

    ex->catche = reader.readU2();

    if (ex->catche) {
      UserClass* cl = 
        (UserClass*)(compilingClass->ctpInfo->isClassLoaded(ex->catche));
      // When loading the class, we made sure that all exception classes
      // were loaded, so cl must have a value.
      assert(cl && "exception class has not been loaded");
      ex->catchClass = cl;
    } else {
      ex->catchClass = Classpath::newThrowable;
    }
    
    ex->tester = createBasicBlock("testException");
    
    // PHI Node for the exception object
    PHINode::Create(intrinsics->JavaObjectType, 0, "", ex->tester);
    
    // Set the unwind destination of the instructions in the range of this
    // handler to the test block of the handler. If an instruction already has
    // a handler and thus is not the synchronize or regular end handler block,
    // leave it as-is.
    for (uint16 i = ex->startpc; i < ex->endpc; ++i) {
      if (opcodeInfos[i].exceptionBlock == endExceptionBlock) {
        opcodeInfos[i].exceptionBlock = ex->tester;
      }
    }

    // If the handler pc does not already have a block, create a new one.
    if (!(opcodeInfos[ex->handlerpc].newBlock)) {
      opcodeInfos[ex->handlerpc].newBlock = createBasicBlock("javaHandler");
    }
    
    // Set the Java handler for this exception.
    ex->javaHandler = opcodeInfos[ex->handlerpc].newBlock;
    opcodeInfos[ex->handlerpc].handler = true;
    
    if (ex->javaHandler->empty()) {
      PHINode::Create(intrinsics->JavaObjectType, 0, "", ex->javaHandler);
    }

  }

  // Loop over all handlers to implement their tester.
  for (sint16 i = 0; i < nbe - sync; ++i) {
    Handler* cur = &handlers[i];
    BasicBlock* bbNext = 0;
    PHINode* javaNode = 0;
    currentExceptionBlock = opcodeInfos[cur->handlerpc].exceptionBlock;

    // Look out where we go if we're not the handler for the exception.
    if (i + 1 != nbe - sync) {
      Handler* next = &handlers[i + 1];
      if (!(cur->startpc >= next->startpc && cur->endpc <= next->endpc)) {
        // If there is no handler to go to (either one that has the same range
        // or one that contains the range), then we jump to the end handler.
        bbNext = endExceptionBlock;
      } else {
        // If there's a handler to goto, we jump to its tester block and record
        // the exception PHI node to give our exception to the tester.
        bbNext = next->tester;
        javaNode = dyn_cast<PHINode>(bbNext->begin());
        assert(javaNode);
      }
    } else {
      // If there's no handler after us, we jump to the end handler.
      bbNext = endExceptionBlock;
    }

    currentBlock = cur->tester;
    
    assert(cur->catchClass && 
           "Class not loaded when reading the exception table");

    Value* VTVar = TheCompiler->getVirtualTable(cur->catchClass->virtualVT);

    // Get the Java exception.
    Value* obj = currentBlock->begin();
    
    Value* objVT = CallInst::Create(intrinsics->GetVTFunction, obj, "",
                                    currentBlock);

    uint32 depth = cur->catchClass->virtualVT->depth;
    Value* depthCl = ConstantInt::get(Type::getInt32Ty(*llvmContext), depth);
    Value* cmp = 0;

    if (depth >= JavaVirtualTable::getDisplayLength()) {
      Value* classArgs[2] = { objVT, VTVar };
          
      cmp = CallInst::Create(intrinsics->IsSecondaryClassFunction,
                             classArgs, "", currentBlock);

    } else {
     
      Value* inDisplay = CallInst::Create(intrinsics->GetDisplayFunction,
                                          objVT, "", currentBlock);
            
      Value* displayArgs[2] = { inDisplay, depthCl };
      Value* VTInDisplay = CallInst::Create(intrinsics->GetVTInDisplayFunction,
                                            displayArgs, "", currentBlock);
             
      cmp = new ICmpInst(*currentBlock, ICmpInst::ICMP_EQ, VTInDisplay, VTVar,
                         "");
    }
   
    // Add the Java exception in the phi node of the handler.
    Instruction* insn = cur->javaHandler->begin();
    PHINode* node = dyn_cast<PHINode>(insn);
    assert(node && "malformed exceptions");
    node->addIncoming(obj, currentBlock);
   
    // Add the Java exception in the phi node of the next block.
    if (javaNode)
      javaNode->addIncoming(obj, currentBlock);
 
    // If we are catching this exception, then jump to the Java Handler,
    // otherwise jump to our next handler.
    BranchInst::Create(cur->javaHandler, bbNext, cmp, currentBlock);

    currentBlock = cur->javaHandler;

    // First thing in the handler: clear the exception.
    Value* javaExceptionPtr = getJavaExceptionPtr(getJavaThreadPtr(getMutatorThreadPtr()));
    
    // Clear exception.
    new StoreInst(intrinsics->JavaObjectNullConstant, javaExceptionPtr,
                  currentBlock);
  }
 
  // Restore currentBlock.
  currentBlock = temp;
  return nbe;
}

void JavaJIT::finishExceptions() {
  pred_iterator PI = pred_begin(endExceptionBlock);
  pred_iterator PE = pred_end(endExceptionBlock);
  if (PI == PE) {
    endExceptionBlock->eraseFromParent();
  } else {
    if (endNode) {
      endNode->addIncoming(Constant::getNullValue(endNode->getType()),
                           endExceptionBlock);
    }
    BranchInst::Create(endBlock, endExceptionBlock);
  }
 

  PI = pred_begin(unifiedUnreachable);
  PE = pred_end(unifiedUnreachable);
  if (PI == PE) {
    unifiedUnreachable->eraseFromParent();
  } else {
    new UnreachableInst(*llvmContext, unifiedUnreachable);
  }
  
  for (Function::iterator BI = llvmFunction->begin(), BE = llvmFunction->end();
       BI != BE; BI++) {
    PI = pred_begin(BI);
    PE = pred_end(BI);
    if (PI == PE) {
      Instruction* insn = BI->begin();
      PHINode* node = dyn_cast<PHINode>(insn);
      if (node) {
        node->replaceAllUsesWith(Constant::getNullValue(node->getType()));
        node->eraseFromParent();
      }
    }
  }
}


#ifdef USE_OPENJDK
#include "JavaJITOpenJDK.inc"
#else
#include "JavaJITClasspath.inc"
#endif
