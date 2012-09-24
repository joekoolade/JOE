//===--------- JavaJITCompiler.cpp - Support for JIT compiling -------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/DerivedTypes.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/LLVMContext.h"
#include "llvm/Module.h"
#include "llvm/Analysis/DebugInfo.h"
#include "llvm/CodeGen/GCStrategy.h"
#include <llvm/CodeGen/JITCodeEmitter.h>
#include "llvm/CodeGen/MachineFunction.h"
#include "llvm/ExecutionEngine/ExecutionEngine.h"
#include "llvm/Support/CommandLine.h"
#include "llvm/Support/ManagedStatic.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Target/TargetData.h"
#include <../lib/ExecutionEngine/JIT/JIT.h>

#include "MvmGC.h"
#include "mvm/VirtualMachine.h"

#include "JavaClass.h"
#include "JavaConstantPool.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "Jnjvm.h"

#include "j3/JavaJITCompiler.h"
#include "j3/J3Intrinsics.h"

using namespace j3;
using namespace llvm;

void JavaJITListener::NotifyFunctionEmitted(const Function &F,
                                     void *Code, size_t Size,
                                     const EmittedFunctionDetails &Details) {

  assert(F.hasGC());
  if (TheCompiler->GCInfo == NULL) {
    TheCompiler->GCInfo = Details.MF->getGMI();
  }
  assert(TheCompiler->GCInfo == Details.MF->getGMI());
}


Constant* JavaJITCompiler::getNativeClass(CommonClass* classDef) {
  Type* Ty = classDef->isClass() ? JavaIntrinsics.JavaClassType :
                                               JavaIntrinsics.JavaCommonClassType;
  
  ConstantInt* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                     uint64_t(classDef));
  return ConstantExpr::getIntToPtr(CI, Ty);
}

Constant* JavaJITCompiler::getResolvedConstantPool(JavaConstantPool* ctp) {
  void* ptr = ctp->ctpRes;
  assert(ptr && "No constant pool found");
  ConstantInt* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                     uint64_t(ptr));
  return ConstantExpr::getIntToPtr(CI, JavaIntrinsics.ResolvedConstantPoolType);
}

Constant* JavaJITCompiler::getMethodInClass(JavaMethod* meth) {
  ConstantInt* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                     (int64_t)meth);
  return ConstantExpr::getIntToPtr(CI, JavaIntrinsics.JavaMethodType);
}

Constant* JavaJITCompiler::getString(JavaString* str) {
  llvm_gcroot(str, 0);
  fprintf(stderr, "Should not be here\n");
  abort();
}

Constant* JavaJITCompiler::getStringPtr(JavaString** str) {
  assert(str && "No string given");
  Type* Ty = PointerType::getUnqual(JavaIntrinsics.JavaObjectType);
  ConstantInt* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                     uint64(str));
  return ConstantExpr::getIntToPtr(CI, Ty);
}

Constant* JavaJITCompiler::getJavaClass(CommonClass* cl) {
  fprintf(stderr, "Should not be here\n");
  abort();
}

Constant* JavaJITCompiler::getJavaClassPtr(CommonClass* cl) {
  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaObject* const* obj = cl->getClassDelegateePtr(vm);
  assert(obj && "Delegatee not created");
  Constant* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                  uint64(obj));
  Type* Ty = PointerType::getUnqual(JavaIntrinsics.JavaObjectType);
  return ConstantExpr::getIntToPtr(CI, Ty);
}

JavaObject* JavaJITCompiler::getFinalObject(llvm::Value* obj) {
  // obj can not encode direclty an object.
  return NULL;
}

Constant* JavaJITCompiler::getFinalObject(JavaObject* obj, CommonClass* cl) {
  llvm_gcroot(obj, 0);
  return NULL;
}

Constant* JavaJITCompiler::getStaticInstance(Class* classDef) {
  void* obj = classDef->getStaticInstance();
  if (!obj) {
    classDef->acquire();
    obj = classDef->getStaticInstance();
    if (!obj) {
      // Allocate now so that compiled code can reference it.
      obj = classDef->allocateStaticInstance(JavaThread::get()->getJVM());
    }
    classDef->release();
  }
  Constant* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                  (uint64_t(obj)));
  return ConstantExpr::getIntToPtr(CI, JavaIntrinsics.ptrType);
}

Constant* JavaJITCompiler::getVirtualTable(JavaVirtualTable* VT) {
  if (VT->cl->isClass()) {
    LLVMClassInfo* LCI = getClassInfo(VT->cl->asClass());
    LCI->getVirtualType();
  }
  
  ConstantInt* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                     uint64_t(VT));
  return ConstantExpr::getIntToPtr(CI, JavaIntrinsics.VTType);
}

Constant* JavaJITCompiler::getNativeFunction(JavaMethod* meth, void* ptr) {
  LLVMSignatureInfo* LSI = getSignatureInfo(meth->getSignature());
  Type* valPtrType = LSI->getNativePtrType();
  
  assert(ptr && "No native function given");

  Constant* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
                                  uint64_t(ptr));
  return ConstantExpr::getIntToPtr(CI, valPtrType);
}

JavaJITCompiler::JavaJITCompiler(const std::string &ModuleID) :
  JavaLLVMCompiler(ModuleID), listener(this) {

  EmitFunctionName = false;
  GCInfo = NULL;

  executionEngine = ExecutionEngine::createJIT(TheModule, 0,
                                               0, llvm::CodeGenOpt::Default, false);
  executionEngine->RegisterJITEventListener(&listener);
  TheTargetData = executionEngine->getTargetData();
  TheModule->setDataLayout(TheTargetData->getStringRepresentation());
  TheModule->setTargetTriple(mvm::MvmModule::getHostTriple());
  JavaIntrinsics.init(TheModule);
  initialiseAssessorInfo();  

  addJavaPasses();

  // Set the pointer to methods that will be inlined, so that these methods
  // do not get compiled by the JIT.
  executionEngine->updateGlobalMapping(
      JavaIntrinsics.AllocateFunction, (void*)(word_t)gcmalloc);
  executionEngine->updateGlobalMapping(
      JavaIntrinsics.ArrayWriteBarrierFunction, (void*)(word_t)arrayWriteBarrier);
  executionEngine->updateGlobalMapping(
      JavaIntrinsics.FieldWriteBarrierFunction, (void*)(word_t)fieldWriteBarrier);
  executionEngine->updateGlobalMapping(
      JavaIntrinsics.NonHeapWriteBarrierFunction, (void*)(word_t)nonHeapWriteBarrier);
}

JavaJITCompiler::~JavaJITCompiler() {
  executionEngine->removeModule(TheModule);
  delete executionEngine;
  // ~JavaLLVMCompiler will delete the module.
}

void JavaJITCompiler::makeVT(Class* cl) { 
  JavaVirtualTable* VT = cl->virtualVT; 
  assert(VT && "No VT was allocated!");
    
  if (VT->init) {
    // The VT has already been filled by the AOT compiler so there
    // is nothing left to do!
    return;
  }
 
  Class* current = cl;
  word_t* functions = VT->getFunctions();
  while (current != NULL) {
    // Fill the virtual table with function pointers.
    for (uint32 i = 0; i < current->nbVirtualMethods; ++i) {
      JavaMethod& meth = current->virtualMethods[i];
      if (meth.offset != 0 || current->super != NULL) {
        functions[meth.offset] = getPointerOrStub(meth, JavaMethod::Virtual);
      }
    }
    current = current->super;
  }
}

extern "C" void ThrowUnfoundInterface() {
  JavaThread *th = JavaThread::get();

  BEGIN_NATIVE_EXCEPTION(1);

  // Lookup the caller of this class.
  mvm::StackWalker Walker(th);
  mvm::FrameInfo* FI = Walker.get();
  assert(FI->Metadata != NULL && "Wrong stack trace");
  JavaMethod* meth = (JavaMethod*)FI->Metadata;

  // Lookup the method info in the constant pool of the caller.
  uint16 ctpIndex = meth->lookupCtpIndex(FI);
  assert(ctpIndex && "No constant pool index");
  JavaConstantPool* ctpInfo = meth->classDef->getConstantPool();
  CommonClass* ctpCl = 0;
  const UTF8* utf8 = 0;
  Signdef* sign = 0;
  ctpInfo->resolveMethod(ctpIndex, ctpCl, utf8, sign);

  JavaThread::get()->getJVM()->abstractMethodError(ctpCl, utf8);

  END_NATIVE_EXCEPTION
}

void JavaJITCompiler::makeIMT(Class* cl) {
  InterfaceMethodTable* IMT = cl->virtualVT->IMT;
  if (!IMT) return;
 
  std::set<JavaMethod*> contents[InterfaceMethodTable::NumIndexes];
  cl->fillIMT(contents);

  
  for (uint32_t i = 0; i < InterfaceMethodTable::NumIndexes; ++i) {
    std::set<JavaMethod*>& atIndex = contents[i];
    uint32_t size = atIndex.size();
    if (size == 1) {
      JavaMethod* Imeth = *(atIndex.begin());
      JavaMethod* meth = cl->lookupMethodDontThrow(Imeth->name,
                                                   Imeth->type,
                                                   false, true, 0);
      if (meth) {
        IMT->contents[i] = getPointerOrStub(*meth, JavaMethod::Interface);
      } else {
        IMT->contents[i] = (word_t)ThrowUnfoundInterface;
      }
    } else if (size > 1) {
      std::vector<JavaMethod*> methods;
      bool SameMethod = true;
      JavaMethod* OldMethod = 0;
      
      for (std::set<JavaMethod*>::iterator it = atIndex.begin(),
           et = atIndex.end(); it != et; ++it) {
        JavaMethod* Imeth = *it;
        JavaMethod* Cmeth = cl->lookupMethodDontThrow(Imeth->name, Imeth->type,
                                                      false, true, 0);
       
        if (OldMethod && OldMethod != Cmeth) SameMethod = false;
        else OldMethod = Cmeth;
       
        methods.push_back(Cmeth);
      }

      if (SameMethod) {
        if (methods[0]) {
          IMT->contents[i] = getPointerOrStub(*(methods[0]),
                                              JavaMethod::Interface);
        } else {
          IMT->contents[i] = (word_t)ThrowUnfoundInterface;
        }
      } else {

        // Add one to have a NULL-terminated table.
        uint32_t length = (2 * size + 1) * sizeof(word_t);
      
        word_t* table = (word_t*)
          cl->classLoader->allocator.Allocate(length, "IMT");
      
        IMT->contents[i] = (word_t)table | 1;

        uint32_t j = 0;
        std::set<JavaMethod*>::iterator Interf = atIndex.begin();
        for (std::vector<JavaMethod*>::iterator it = methods.begin(),
             et = methods.end(); it != et; ++it, j += 2, ++Interf) {
          JavaMethod* Imeth = *Interf;
          JavaMethod* Cmeth = *it;
          assert(Imeth != NULL);
          assert(j < 2 * size - 1);
          table[j] = (word_t)Imeth;
          if (Cmeth) {
            table[j + 1] = getPointerOrStub(*Cmeth, JavaMethod::Interface);
          } else {
            table[j + 1] = (word_t)ThrowUnfoundInterface;
          }
        }
        assert(Interf == atIndex.end());
      }
    }
  }
}

void JavaJITCompiler::setMethod(Function* func, void* ptr, const char* name) {
  func->setLinkage(GlobalValue::ExternalLinkage);
  func->setName(name);
  assert(ptr && "No value given");
  executionEngine->updateGlobalMapping(func, ptr);
}

void* JavaJITCompiler::materializeFunction(JavaMethod* meth, Class* customizeFor) {
  mvm::MvmModule::protectIR();
  Function* func = parseFunction(meth, customizeFor);
  void* res = executionEngine->getPointerToGlobal(func);

  if (!func->isDeclaration()) {
    llvm::GCFunctionInfo& GFI = GCInfo->getFunctionInfo(*func);
  
    Jnjvm* vm = JavaThread::get()->getJVM();
    mvm::MvmModule::addToVM(vm, &GFI, (JIT*)executionEngine, allocator, meth);

    // Now that it's compiled, we don't need the IR anymore
    func->deleteBody();
  }
  mvm::MvmModule::unprotectIR();
  if (customizeFor == NULL || !getMethodInfo(meth)->isCustomizable) {
    meth->code = res;
  }
  return res;
}

void* JavaJITCompiler::GenerateStub(llvm::Function* F) {
  mvm::MvmModule::protectIR();
  void* res = executionEngine->getPointerToGlobal(F);
 
  // If the stub was already generated through an equivalent signature,
  // The body has been deleted, so the function just becomes a declaration.
  if (!F->isDeclaration()) {
    llvm::GCFunctionInfo& GFI = GCInfo->getFunctionInfo(*F);
  
    Jnjvm* vm = JavaThread::get()->getJVM();
    mvm::MvmModule::addToVM(vm, &GFI, (JIT*)executionEngine, allocator, NULL);
  
    // Now that it's compiled, we don't need the IR anymore
    F->deleteBody();
  }
  mvm::MvmModule::unprotectIR();
  return res;
}

// Helper function to run an executable with a JIT
extern "C" int StartJnjvmWithJIT(int argc, char** argv, char* mainClass) {
  llvm::llvm_shutdown_obj X; 
   
  mvm::MvmModule::initialise(argc, argv);
  mvm::Collector::initialise(argc, argv);
 
  mvm::ThreadAllocator allocator;
  char** newArgv = (char**)allocator.Allocate((argc + 1) * sizeof(char*));
  memcpy(newArgv + 1, argv, argc * sizeof(void*));
  newArgv[0] = newArgv[1];
  newArgv[1] = mainClass;

  mvm::BumpPtrAllocator Allocator;
  JavaJITCompiler* Comp = JavaJITCompiler::CreateCompiler("JITModule");
  JnjvmBootstrapLoader* loader = new(Allocator, "Bootstrap loader")
    JnjvmBootstrapLoader(Allocator, Comp, true);
  Jnjvm* vm = new(Allocator, "VM") Jnjvm(Allocator, NULL, loader);
  vm->runApplication(argc + 1, newArgv);
  vm->waitForExit();
  
  return 0;
}

word_t JavaJ3LazyJITCompiler::getPointerOrStub(JavaMethod& meth,
                                                  int side) {
  return meth.getSignature()->getVirtualCallStub();
}

Value* JavaJ3LazyJITCompiler::addCallback(Class* cl, uint16 index,
                                          Signdef* sign, bool stat,
                                          llvm::BasicBlock* insert) {
  LLVMSignatureInfo* LSI = getSignatureInfo(sign);
  // Set the stub in the constant pool.
  JavaConstantPool* ctpInfo = cl->ctpInfo;
  word_t stub = stat ? sign->getStaticCallStub() : sign->getSpecialCallStub();
  if (!ctpInfo->ctpRes[index]) {
    // Do a compare and swap, so that we do not overwrtie what a stub might
    // have just updated.
    word_t val = (word_t)
      __sync_val_compare_and_swap(&(ctpInfo->ctpRes[index]), NULL, (void*)stub);
    // If there is something in the the constant pool that is not NULL nor
    // the stub, then it's the method.
    if (val != 0 && val != stub) {
      return ConstantExpr::getIntToPtr(
          ConstantInt::get(Type::getInt64Ty(insert->getContext()), val),
          stat ? LSI->getStaticPtrType() : LSI->getVirtualPtrType());
    }
  }
  // Load the constant pool.
  Value* CTP = getResolvedConstantPool(ctpInfo);
  Value* Index = ConstantInt::get(Type::getInt32Ty(insert->getContext()),
                                  index);
  Value* func = GetElementPtrInst::Create(CTP, Index, "", insert);
  func = new LoadInst(func, "", false, insert);
  // Bitcast it to the LLVM function.
  func = new BitCastInst(func, stat ? LSI->getStaticPtrType() :
                                      LSI->getVirtualPtrType(),
                         "", insert);
  return func;
}

bool JavaJ3LazyJITCompiler::needsCallback(JavaMethod* meth,
                                          Class* customizeFor,
                                          bool* needsInit) {
  *needsInit = true;
  return (meth == NULL ||
          getMethod(meth, customizeFor)->hasExternalWeakLinkage());
}

JavaJ3LazyJITCompiler::JavaJ3LazyJITCompiler(const std::string& ModuleID)
    : JavaJITCompiler(ModuleID) {}


JavaJITCompiler* JavaJITCompiler::CreateCompiler(const std::string& ModuleID) {
  return new JavaJ3LazyJITCompiler(ModuleID);
}
