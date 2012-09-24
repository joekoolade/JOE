//===-- LowerJavaRT.cpp - Remove references to RT classes and functions  --===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/LLVMContext.h"
#include "llvm/Module.h"
#include "llvm/Pass.h"
#include "llvm/Support/CallSite.h"
#include "llvm/Support/Compiler.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/raw_ostream.h"

using namespace llvm;

namespace {

  class LowerJavaRT : public ModulePass {
  public:
    static char ID;
    LowerJavaRT() : ModulePass(ID) { }

    virtual bool runOnModule(Module &M);
  private:
  };
  char LowerJavaRT::ID = 0;
  static RegisterPass<LowerJavaRT> X("LowerJavaRT",
                                     "Remove references to RT");

bool LowerJavaRT::runOnModule(Module& M) {
  bool Changed = true;

  for (Module::iterator I = M.begin(), E = M.end(); I != E;) {
    Function& GV = *I;
    ++I;
    if (!strncmp(GV.getName().data(), "JnJVM_java", 10) ||
        !strncmp(GV.getName().data(), "java", 4)) {
      if (!strcmp(GV.getName().data(), "JnJVM_java_lang_String_charAt__I")) {
  	    Function* F = M.getFunction("MMTkCharAt");
        if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkCharAt", &M);
      	
        GV.replaceAllUsesWith(F);
      } else if (!strcmp(GV.getName().data(), "JnJVM_java_lang_Object_getClass__")) {
  	    Function* F = M.getFunction("MMTkGetClass");
	      if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkGetClass", &M);
      	GV.replaceAllUsesWith(F);
      } else if (!strcmp(GV.getName().data(), "JnJVM_java_lang_String_equals__Ljava_lang_Object_2")) {
  	    Function* F = M.getFunction("MMTkStringEquals");
	      if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkStringEquals", &M);
      	GV.replaceAllUsesWith(F);
      } else if (!strcmp(GV.getName().data(), "JnJVM_java_lang_String_length__")) {
  	    Function* F = M.getFunction("MMTkStringLength");
	      if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkStringLength", &M);
      	GV.replaceAllUsesWith(F);
      } else if (!strcmp(GV.getName().data(), "JnJVM_java_lang_String_indexOf__I")) {
  	    Function* F = M.getFunction("MMTkStringIndexOf");
	      if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkStringIndexOf", &M);
      	GV.replaceAllUsesWith(F);
      } else if (!strcmp(GV.getName().data(), "JnJVM_java_lang_String_substring__II")) {
  	    Function* F = M.getFunction("MMTkStringSubstringII");
	      if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkStringSubstringII", &M);
      	GV.replaceAllUsesWith(F);
      } else if (!strcmp(GV.getName().data(), "JnJVM_java_lang_String_substring__I")) {
  	    Function* F = M.getFunction("MMTkStringSubstringI");
	      if (!F) 
          F = Function::Create(GV.getFunctionType(),
                               GlobalValue::ExternalLinkage, "MMTkStringSubstringI", &M);
      	GV.replaceAllUsesWith(F);
      } else {
        GV.replaceAllUsesWith(Constant::getNullValue(GV.getType()));
      }
      GV.eraseFromParent();
    }
  }

  // Remove all references to magic methods.
  for (Module::iterator I = M.begin(), E = M.end(); I != E;) {
    Function& GV = *I;
    ++I;
    if (!strncmp(GV.getName().data(), "JnJVM_org_vmmagic", 17)) {
      GV.replaceAllUsesWith(Constant::getNullValue(GV.getType()));
      GV.eraseFromParent();
    }
  }

  for (Module::global_iterator I = M.global_begin(), E = M.global_end();
       I != E;) {
    GlobalValue& GV = *I;
    ++I;
    if (!strncmp(GV.getName().data(), "JnJVM_java", 10) ||
        !strncmp(GV.getName().data(), "java", 4) ||
        !strncmp(GV.getName().data(), "JnJVM_gnu", 9) ||
        !strncmp(GV.getName().data(), "_3", 2) || // Arrays
        !strncmp(GV.getName().data(), "gnu", 3)) {
      GV.replaceAllUsesWith(Constant::getNullValue(GV.getType()));
      GV.eraseFromParent();
    }
  }

  // Replace finalization calls with null.
  Function* F = M.getFunction("addFinalizationCandidate");
  F->replaceAllUsesWith(Constant::getNullValue(F->getType()));
  F->eraseFromParent();
 
  // Replace gcmalloc with the allocator of MMTk objects in VMKit
  F = M.getFunction("gcmalloc");
  Function* Ma = M.getFunction("AllocateMagicArray");

  Function* NewFunction = 
    Function::Create(F->getFunctionType(), GlobalValue::ExternalLinkage,
                     "MMTkMutatorAllocate", &M);

  F->replaceAllUsesWith(NewFunction);
  F->eraseFromParent();
  
  Ma->replaceAllUsesWith(NewFunction);
  Ma->eraseFromParent();

  // Finally, remove GC info from the methods. They must not have any
  // gcroot.
  for (Module::iterator I = M.begin(), E = M.end(); I != E; I++) {
    I->clearGC();
  }

  // Rename JavaObject type to avoid collisions when inlining
  // malloc and barriers.
  M.getTypeByName("JavaObject")->setName("MMTk.JavaObject");

  return Changed;
}

}
