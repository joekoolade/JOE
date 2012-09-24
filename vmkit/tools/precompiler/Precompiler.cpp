//===------------------------ Precompiler.cpp -----------------------------===//
// Run a program and emit code for classes loaded by the bootstrap loader -===//
//
//                           The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Support/CommandLine.h"
#include "llvm/Support/ManagedStatic.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/Path.h"
#include "llvm/Bitcode/ReaderWriter.h"
#include "llvm/Support/Signals.h"

#include "MvmGC.h"
#include "mvm/JIT.h"
#include "mvm/MethodInfo.h"
#include "mvm/VirtualMachine.h"
#include "mvm/Threads/Thread.h"

#include "j3/JavaAOTCompiler.h"
#include "j3/JavaJITCompiler.h"
#include "../../lib/J3/VMCore/JavaThread.h"
#include "../../lib/J3/VMCore/JnjvmClassLoader.h"
#include "../../lib/J3/VMCore/Jnjvm.h"

#include <string>

using namespace j3;
using namespace mvm;

#include "FrametablesExterns.inc"

CompiledFrames* frametables[] = {
  #include "FrametablesSymbols.inc"
  NULL
};


static void mainCompilerLoaderStart(JavaThread* th) {
  Jnjvm* vm = th->getJVM();
  JnjvmBootstrapLoader* bootstrapLoader = vm->bootstrapLoader;
  JavaAOTCompiler* AOT = new JavaAOTCompiler("AOT");
  AOT->compileClassLoader(bootstrapLoader);
  AOT->printStats();
  vm->exit(); 
}

int main(int argc, char **argv, char **envp) {
  llvm::llvm_shutdown_obj X;
  bool EmitClassBytes = false;
  static const char* EmitClassBytesStr = "-emit-class-bytes";
  for (int i = 0; i < argc; i++) {
    if (!strncmp(argv[i], EmitClassBytesStr, strlen(EmitClassBytesStr))) {
      EmitClassBytes = true;
      break;
    }
  }

  std::string OutputFilename;

  // Initialize base components.  
  MvmModule::initialise(argc, argv);
  Collector::initialise(argc, argv);
  
  // Create the allocator that will allocate the bootstrap loader and the JVM.
  mvm::BumpPtrAllocator Allocator;
  JavaAOTCompiler* AOT;
  if (EmitClassBytes) {
    AOT = new JavaAOTCompiler("AOT");
    OutputFilename = "classes.bc";
    JnjvmBootstrapLoader* loader = new(Allocator, "Bootstrap loader")
      JnjvmBootstrapLoader(Allocator, AOT, true);
    AOT->generateClassBytes(loader);
  } else {
    OutputFilename = "generated.bc";
    JavaJITCompiler* JIT = JavaJITCompiler::CreateCompiler("JIT");
    JnjvmBootstrapLoader* loader = new(Allocator, "Bootstrap loader")
      JnjvmBootstrapLoader(Allocator, JIT, true);
    Jnjvm* vm = new(Allocator, "VM") Jnjvm(Allocator, frametables, loader);
 
    // Run the application. 
    vm->runApplication(argc, argv);
    vm->waitForExit();

    // Now AOT Compile all compiled methods.
    vm->doExit = false;
    JavaThread* th = new JavaThread(vm);
    vm->setMainThread(th);
    th->start((void (*)(mvm::Thread*))mainCompilerLoaderStart);
    vm->waitForExit();

    AOT = (JavaAOTCompiler*)loader->getCompiler();
  }


  // Emit the bytecode in file.
  std::string ErrorInfo;
  std::auto_ptr<llvm::raw_ostream> Out 
    (new llvm::raw_fd_ostream(OutputFilename.c_str(), ErrorInfo,
                        llvm::raw_fd_ostream::F_Binary));
  if (!ErrorInfo.empty()) {
    llvm::errs() << ErrorInfo << '\n';
    return 1;
  }
   
  // Make sure that the Out file gets unlinked from the disk if we get a
  // SIGINT.
  llvm::sys::RemoveFileOnSignal(llvm::sys::Path(OutputFilename));
  
  llvm::WriteBitcodeToFile(AOT->getLLVMModule(), *Out);

  return 0;
}

// Because we don't want inlined methods to show up in the result
// precompiled code, provide this method in order to link.
extern "C" void MMTk_InlineMethods(llvm::Module* module) {
}
