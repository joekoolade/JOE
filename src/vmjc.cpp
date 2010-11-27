//===----------------- vmjc.cpp - Java static compiler --------------------===//
//
//                           The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//
//
// This utility may be invoked in the following manner:
//  vmjc [options] x - Read Java bytecode from the x.class file, write llvm
//                     bytecode to the x.bc file.
//  Options:
//      --help   - Output information about command line switches
//
//===----------------------------------------------------------------------===//

#include "llvm/LinkAllPasses.h"
#include "llvm/LinkAllVMCore.h"
#include "llvm/Module.h"
#include "llvm/PassManager.h"
#include "llvm/Assembly/PrintModulePass.h"
#include "llvm/CodeGen/LinkAllCodegenComponents.h"
#include "llvm/Bitcode/ReaderWriter.h"
#include "llvm/ExecutionEngine/ExecutionEngine.h"
#include "llvm/Support/CommandLine.h"
#include "llvm/Support/ManagedStatic.h"
#include "llvm/Support/MemoryBuffer.h"
#include "llvm/Support/PassNameParser.h"
#include "llvm/Support/PluginLoader.h"
#include "llvm/Support/RegistryParser.h"
#include "llvm/Support/SystemUtils.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/System/Signals.h"
#include "llvm/Target/TargetData.h"
#include "llvm/Target/TargetMachine.h"


#include "MvmGC.h"
#include "mvm/JIT.h"
#include "mvm/Object.h"
#include "mvm/VirtualMachine.h"
#include "mvm/Threads/Thread.h"

#include "j3/JavaAOTCompiler.h"

#include "../../lib/J3/VMCore/JnjvmClassLoader.h"
#include "../../lib/J3/VMCore/Jnjvm.h"

#include <iostream>
#include <fstream>
#include <memory>
#include <string>

using namespace j3;
using namespace llvm;

static cl::opt<std::string>
InputFilename(cl::Positional, cl::desc("<input Java bytecode>"), cl::init("-"));

static cl::opt<std::string>
OutputFilename("o", cl::desc("Override output filename"),
               cl::value_desc("filename"));

static cl::opt<bool>
Force("f", cl::desc("Overwrite output files"));

static cl::opt<std::string>
MainClass("main", cl::desc("Specify main class"));

static cl::opt<bool>
WithJIT("with-jit", cl::desc("Generate main function with JIT support"));

static cl::opt<bool>
DisableOutput("disable-output", cl::desc("Disable output"), cl::init(false));

static cl::opt<std::string>
TargetTriple("mtriple", cl::desc("Override target triple for module"));

static cl::opt<bool>
DisableExceptions("disable-exceptions",
              cl::desc("Disable Java exceptions"));

static cl::opt<bool>
DisableCooperativeGC("disable-cooperativegc",
              cl::desc("Disable cooperative garbage collection"));


static cl::opt<bool>
DisableStubs("disable-stubs",
              cl::desc("Disable Java stubs"));

static cl::opt<bool>
AssumeCompiled("assume-compiled",
              cl::desc("Assume external Java classes are compiled"));

static cl::opt<bool> 
PrintStats("print-aot-stats", 
           cl::desc("Print stats by the AOT compiler"));


static cl::list<std::string> 
Properties("D", cl::desc("Set a property"), cl::Prefix, cl::ZeroOrMore);

static cl::list<std::string> 
WithClinit("with-clinit", cl::desc("Classes to clinit"), cl::ZeroOrMore,
           cl::CommaSeparated);

int main(int argc, char **argv) {
  llvm_shutdown_obj X;  // Call llvm_shutdown() on exit.
  try {
    cl::ParseCommandLineOptions(argc, argv, "vmkit .class -> .ll compiler\n");
    sys::PrintStackTraceOnErrorSignal();

    std::string ErrorMessage;

    
    if (InputFilename == "-") {
      cl::PrintHelpMessage();
      return 0;
    }
   
    // Disable cross-compiling for now.
    if (false) {
      Module* TheModule = new Module("bootstrap module",
                                     *(new llvm::LLVMContext()));
      if (!TargetTriple.empty())
        TheModule->setTargetTriple(TargetTriple);
      else
        TheModule->setTargetTriple(mvm::MvmModule::getHostTriple());

#if 0
      // explicitly specified an architecture to compile for.
      const Target *TheTarget = 0;
      if (!MArch.empty()) {
        for (TargetRegistry::iterator it = TargetRegistry::begin(),
             ie = TargetRegistry::end(); it != ie; ++it) {
          if (MArch == it->getName()) {
            TheTarget = &*it;
            break;
          }
        }

        if (!TheTarget) {
          errs() << argv[0] << ": error: invalid target '" << MArch << "'.\n";
          return 1;
        }
      } else {
        std::string Err;
        TheTarget =
          TargetRegistry::getClosestStaticTargetForModule(*TheModule, Err);
        if (TheTarget == 0) {
          errs() << argv[0] << ": error auto-selecting target for module '"
                 << Err << "'.  Please use the -march option to explicitly "
                 << "pick a target.\n";
          return 1;
        }
      }

      std::string FeaturesStr;
      std::auto_ptr<TargetMachine>
        target(TheTarget->createTargetMachine(*TheModule, FeaturesStr));
      assert(target.get() && "Could not allocate target machine!");
      TargetMachine &Target = *target.get();

      // Install information about target datalayout stuff into the module for
      // optimizer use.
      TheModule->setDataLayout(Target.getTargetData()->
                               getStringRepresentation());


      mvm::MvmModule::initialise(CodeGenOpt::Default, TheModule, &Target);
#endif
    } else {
      mvm::MvmModule::initialise();
    }

    JavaAOTCompiler* Comp = new JavaAOTCompiler("AOT");

    mvm::Collector::initialise();

    JnjvmClassLoader* JCL = mvm::VirtualMachine::initialiseJVM(Comp, false);

    if (DisableExceptions) Comp->disableExceptions();
    if (DisableStubs) Comp->generateStubs = false;
    if (AssumeCompiled) Comp->assumeCompiled = true;
    if (DisableCooperativeGC) Comp->disableCooperativeGC();
    
    mvm::BumpPtrAllocator A;
    Jnjvm* vm = new(A, "Bootstrap loader") Jnjvm(A, (JnjvmBootstrapLoader*)JCL);
  
    for (std::vector<std::string>::iterator i = Properties.begin(),
         e = Properties.end(); i != e; ++i) {

      char* key = new char [(*i).size()+1];
      strcpy(key, (*i).c_str());
      char* value = strchr(key, '=');
      if (!value) {
        delete[] key;
      } else {
        value[0] = 0;
        vm->addProperty(key, &value[1]);
      }
    }

    Comp->clinits = &WithClinit;
    Comp->compileFile(vm, InputFilename.c_str());

    if (!MainClass.empty()) {
      Comp->generateMain(MainClass.c_str(), WithJIT);
    }

    if (PrintStats)
      Comp->printStats();

    // Infer the output filename if needed.
    if (OutputFilename.empty()) {
      if (InputFilename == "-") {
        OutputFilename = "-";
      } else {
        std::string IFN = InputFilename;
        int Len = IFN.length();
        if (IFN[Len-3] == '.' && IFN[Len-2] == 'l' && IFN[Len-1] == 'l') {
          // Source ends in .ll
          OutputFilename = std::string(IFN.begin(), IFN.end()-3);
        } else {
          OutputFilename = IFN;   // Append a .bc to it
        }   
        OutputFilename += ".bc";
      }   
    }
  
    std::string ErrorInfo;
    std::auto_ptr<raw_ostream> Out 
    (new raw_fd_ostream(OutputFilename.c_str(), ErrorInfo,
                        raw_fd_ostream::F_Binary));
    if (!ErrorInfo.empty()) {
      errs() << ErrorInfo << '\n';
      return 1;
    }
  
  
    // Make sure that the Out file gets unlinked from the disk if we get a
    // SIGINT.
    if (OutputFilename != "-")
      sys::RemoveFileOnSignal(sys::Path(OutputFilename));

    if (!DisableOutput)
      if (Force || !CheckBitcodeOutputToConsole(*Out, true))
        WriteBitcodeToFile(Comp->getLLVMModule(), *Out);

    return 0;

  } catch (const std::string& msg) {
    errs() << argv[0] << ": " << msg << "\n";
  } catch (...) {
    errs() << argv[0] << ": Unexpected unknown exception occurred.\n";
  }
  return 1;
}

