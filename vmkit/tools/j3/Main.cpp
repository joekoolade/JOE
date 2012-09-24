//===------------- Main.cpp - Simple execution of J3 ----------------------===//
//
//                          The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MvmGC.h"
#include "mvm/JIT.h"
#include "mvm/MethodInfo.h"
#include "mvm/VirtualMachine.h"
#include "mvm/Threads/Thread.h"

#include "j3/JavaJITCompiler.h"
#include "../../lib/J3/VMCore/JnjvmClassLoader.h"
#include "../../lib/J3/VMCore/Jnjvm.h"

#include "llvm/Support/CommandLine.h"
#include "llvm/Support/ManagedStatic.h"

using namespace j3;
using namespace mvm;

#include "FrametablesExterns.inc"

CompiledFrames* frametables[] = {
  #include "FrametablesSymbols.inc"
  NULL
};

int main(int argc, char **argv, char **envp) {
  llvm::llvm_shutdown_obj X;

  // Initialize base components.  
  MvmModule::initialise(argc, argv);
  Collector::initialise(argc, argv);
 
  // Create the allocator that will allocate the bootstrap loader and the JVM.
  mvm::BumpPtrAllocator Allocator;
  JavaJITCompiler* Comp = JavaJITCompiler::CreateCompiler("JITModule");
  JnjvmBootstrapLoader* loader = new(Allocator, "Bootstrap loader")
    JnjvmBootstrapLoader(Allocator, Comp, true);
  Jnjvm* vm = new(Allocator, "VM") Jnjvm(Allocator, frametables, loader);
 
  // Run the application. 
  vm->runApplication(argc, argv);
  vm->waitForExit();
  System::Exit(0);

  // Destroy everyone.
  // vm->~Jnjvm();
  // loader->~JnjvmBootstrapLoader();

  return 0;
}
