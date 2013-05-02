//===---------------- JIT.cc - Initialize the JIT -------------------------===//
//
//                     The Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <llvm/CallingConv.h>
#include <llvm/Constants.h>
#include <llvm/DerivedTypes.h>
#include <llvm/Function.h>
#include <llvm/GlobalValue.h>
#include <llvm/Instructions.h>
#include <llvm/LinkAllPasses.h>
#include <llvm/LLVMContext.h>
#include <llvm/Module.h>
#include <llvm/PassManager.h>
#include <llvm/Type.h>
#include <llvm/Analysis/DebugInfo.h>
#include <llvm/Analysis/LoopPass.h>
#include <llvm/Analysis/Verifier.h>
#include <llvm/Assembly/Parser.h>
#include <llvm/CodeGen/GCStrategy.h>
#include <llvm/CodeGen/JITCodeEmitter.h>
#include <llvm/Config/config.h>
#include <llvm/ExecutionEngine/ExecutionEngine.h>
#include "llvm/ExecutionEngine/JITEventListener.h"
#include "llvm/Support/CommandLine.h"
#include <llvm/Support/Debug.h>
#include <llvm/Support/IRReader.h>
#include <llvm/Support/MutexGuard.h>
#include <llvm/Support/PassNameParser.h>
#include <llvm/Support/SourceMgr.h>
#include <llvm/Support/TargetSelect.h>
#include <llvm/Target/TargetData.h>
#include <llvm/Target/TargetMachine.h>
#include <llvm/Target/TargetOptions.h>

#include "j3/JIT.h"

#include <dlfcn.h>
#include <sys/mman.h>
#include <vector>

using namespace j3;
using namespace llvm;

//namespace mvm {
//  namespace llvm_runtime {
//    #include "LLVMRuntime.inc"
//  }
//  void linkVmkitGC();
//}

const char* JeiModule::getHostTriple() {
#ifdef LLVM_HOSTTRIPLE
  return LLVM_HOSTTRIPLE;
#else
  return LLVM_DEFAULT_TARGET_TRIPLE;
#endif
}

const int kWordSize = sizeof(word_t);
const int kWordSizeLog2 = kWordSize == 4 ? 2 : 3;

cl::opt<bool>
StandardCompileOpts("std-compile-opts", 
                   cl::desc("Include the standard compile time optimizations"));

static cl::opt<bool>
DisableOptimizations("disable-opt",
                     cl::desc("Do not run any optimization passes"));

// The OptimizationList is automatically populated with registered Passes by the
// PassNameParser.
//
static llvm::cl::list<const llvm::PassInfo*, bool, llvm::PassNameParser>
PassList(llvm::cl::desc("Optimizations available:"));

void JeiModule::initialise(int argc, char** argv) {
  // llvm_start_multithreaded();

  // Initialize passes
  PassRegistry &Registry = *PassRegistry::getPassRegistry();
  initializeCore(Registry);
  initializeScalarOpts(Registry);
  initializeIPO(Registry);
  initializeAnalysis(Registry);
  initializeIPA(Registry);
  initializeTransformUtils(Registry);
  initializeInstCombine(Registry);
  initializeInstrumentation(Registry);
  initializeTarget(Registry);
  InitializeNativeTarget(); 
  
  // NoFramePointerElim = true;
  DisablePrettyStackTrace = true;

  static const char* kPrefix = "-X:llvm:";
  static const int kPrefixLength = strlen(kPrefix);
  int count = 0;

  int i = 1;
  while (i < argc && argv[i][0] == '-') {
    if (!strncmp(argv[i], kPrefix, kPrefixLength)) {
      count++;
    }
    i++;
  }

  const char** llvm_argv = reinterpret_cast<const char**>(new const char*[(count + 3)]);
  int arrayIndex = 0;
  llvm_argv[arrayIndex++] = argv[0];

  if (count > 0) {
    i = 1;
    while (i < argc && argv[i][0] == '-') {
      if (!strncmp(argv[i], kPrefix, kPrefixLength)) {
        argv[i][kPrefixLength - 1] = '-';
        llvm_argv[arrayIndex++] = argv[i] + kPrefixLength - 1;
      }
      i++;
    }
  } else {
    StandardCompileOpts = true;
  }
  // Disable branch fold for accurate line numbers.
  llvm_argv[arrayIndex++] = "-disable-branch-fold";
 
  cl::ParseCommandLineOptions(arrayIndex, const_cast<char**>(llvm_argv));
}


void JeiModule::runPasses(llvm::Function* func,
                          llvm::FunctionPassManager* pm) {
  pm->run(*func);
}

static void addPass(FunctionPassManager *PM, Pass *P) {
  // Add the pass to the pass manager...
  PM->add(P);
}

// This is equivalent to:
// opt -simplifycfg -mem2reg -instcombine -jump-threading -simplifycfg
//     -scalarrepl -instcombine -condprop -simplifycfg -predsimplify 
//     -reassociate -licm -loop-unswitch -indvars -loop-deletion -loop-unroll 
//     -instcombine -gvn -sccp -simplifycfg -instcombine -condprop -dse -adce 
//     -simplifycfg
//
static void AddStandardCompilePasses(FunctionPassManager* PM) { 
   
  addPass(PM, createCFGSimplificationPass()); // Clean up disgusting code
  addPass(PM, createPromoteMemoryToRegisterPass());// Kill useless allocas
  
  addPass(PM, createInstructionCombiningPass()); // Cleanup for scalarrepl.
  addPass(PM, createScalarReplAggregatesPass()); // Break up aggregate allocas
  addPass(PM, createInstructionCombiningPass()); // Cleanup for scalarrepl.
  addPass(PM, createJumpThreadingPass());        // Thread jumps.
  addPass(PM, createCFGSimplificationPass());    // Merge & remove BBs
  addPass(PM, createInstructionCombiningPass()); // Combine silly seq's
  
  addPass(PM, createCFGSimplificationPass());    // Merge & remove BBs
  addPass(PM, createReassociatePass());          // Reassociate expressions
  addPass(PM, createLoopRotatePass());           // Rotate loops.
  addPass(PM, createLICMPass());                 // Hoist loop invariants
  addPass(PM, createLoopUnswitchPass());         // Unswitch loops.
  addPass(PM, createInstructionCombiningPass()); 
  addPass(PM, createIndVarSimplifyPass());       // Canonicalize indvars
  addPass(PM, createLoopDeletionPass());         // Delete dead loops
  addPass(PM, createLoopUnrollPass());           // Unroll small loops*/
  addPass(PM, createInstructionCombiningPass()); // Clean up after the unroller
  addPass(PM, createGVNPass());                  // Remove redundancies
  addPass(PM, createMemCpyOptPass());             // Remove memcpy / form memset  
  addPass(PM, createSCCPPass());                 // Constant prop with SCCP

  // Run instcombine after redundancy elimination to exploit opportunities
  // opened up by them.
  addPass(PM, createInstructionCombiningPass());
  addPass(PM, createJumpThreadingPass());         // Thread jumps
  addPass(PM, createDeadStoreEliminationPass());  // Delete dead stores
  addPass(PM, createAggressiveDCEPass());         // Delete dead instructions
  addPass(PM, createCFGSimplificationPass());     // Merge & remove BBs
}

namespace j3 {
  llvm::FunctionPass* createInlineMallocPass();
}

void JeiModule::addCommandLinePasses(FunctionPassManager* PM) {
  addPass(PM, createVerifierPass());        // Verify that input is correct

  addPass(PM, createCFGSimplificationPass()); // Clean up disgusting code
  addPass(PM, createInlineMallocPass());

  if (DisableOptimizations) {
    PM->doInitialization();
    return;
  }
 
  bool addedStandardCompileOpts = false;
  // Create a new optimization pass for each one specified on the command line
  for (unsigned i = 0; i < PassList.size(); ++i) {
    // Check to see if -std-compile-opts was specified before this option.  If
    // so, handle it.
    if (StandardCompileOpts && 
        !addedStandardCompileOpts &&
        StandardCompileOpts.getPosition() < PassList.getPosition(i)) {
      AddStandardCompilePasses(PM);
      addedStandardCompileOpts = true;
    }
      
    const PassInfo *PassInf = PassList[i];
    Pass *P = 0;
    if (PassInf->getNormalCtor())
      P = PassInf->getNormalCtor()();
    else
      errs() << "cannot create pass: "
           << PassInf->getPassName() << "\n";
    if (P) {
        bool isModulePass = (P->getPassKind() == PT_Module);
        if (isModulePass) 
          errs() << "vmkit does not support module pass: "
             << PassInf->getPassName() << "\n";
        else addPass(PM, P);
    }
  }
    
  // If -std-compile-opts was specified at the end of the pass list, add them.
  if (StandardCompileOpts && !addedStandardCompileOpts) {
    AddStandardCompilePasses(PM);
  }

  PM->doInitialization();
}

extern "C" void MMTk_InlineMethods(llvm::Module* module);

void BaseIntrinsics::init(llvm::Module* module) {

  LLVMContext& Context = module->getContext();

  // MMTk_InlineMethods(module);
  // fixme
  // llvm_runtime::makeLLVMModuleContents(module);

  // Type declaration
  ptrType = PointerType::getUnqual(Type::getInt8Ty(Context));
  ptr32Type = PointerType::getUnqual(Type::getInt32Ty(Context));
  ptrPtrType = PointerType::getUnqual(ptrType);
  pointerSizeType = module->getPointerSize() == Module::Pointer32 ?
    Type::getInt32Ty(Context) : Type::getInt64Ty(Context);

  // Constant declaration
  constantLongMinusOne = ConstantInt::get(Type::getInt64Ty(Context), (uint64_t)-1);
  constantLongZero = ConstantInt::get(Type::getInt64Ty(Context), 0);
  constantLongOne = ConstantInt::get(Type::getInt64Ty(Context), 1);
  constantZero = ConstantInt::get(Type::getInt32Ty(Context), 0);
  constantInt8Zero = ConstantInt::get(Type::getInt8Ty(Context), 0);
  constantOne = ConstantInt::get(Type::getInt32Ty(Context), 1);
  constantTwo = ConstantInt::get(Type::getInt32Ty(Context), 2);
  constantThree = ConstantInt::get(Type::getInt32Ty(Context), 3);
  constantFour = ConstantInt::get(Type::getInt32Ty(Context), 4);
  constantFive = ConstantInt::get(Type::getInt32Ty(Context), 5);
  constantSix = ConstantInt::get(Type::getInt32Ty(Context), 6);
  constantSeven = ConstantInt::get(Type::getInt32Ty(Context), 7);
  constantEight = ConstantInt::get(Type::getInt32Ty(Context), 8);
  constantMinusOne = ConstantInt::get(Type::getInt32Ty(Context), (uint64_t)-1);
  constantMinInt = ConstantInt::get(Type::getInt32Ty(Context), MinInt);
  constantMaxInt = ConstantInt::get(Type::getInt32Ty(Context), MaxInt);
  constantMinLong = ConstantInt::get(Type::getInt64Ty(Context), MinLong);
  constantMaxLong = ConstantInt::get(Type::getInt64Ty(Context), MaxLong);
  constantFloatZero = ConstantFP::get(Type::getFloatTy(Context), 0.0f);
  constantFloatOne = ConstantFP::get(Type::getFloatTy(Context), 1.0f);
  constantFloatTwo = ConstantFP::get(Type::getFloatTy(Context), 2.0f);
  constantDoubleZero = ConstantFP::get(Type::getDoubleTy(Context), 0.0);
  constantDoubleOne = ConstantFP::get(Type::getDoubleTy(Context), 1.0);
  constantMaxIntFloat = ConstantFP::get(Type::getFloatTy(Context), MaxIntFloat);
  constantMinIntFloat = ConstantFP::get(Type::getFloatTy(Context), MinIntFloat);
  constantMinLongFloat = ConstantFP::get(Type::getFloatTy(Context), MinLongFloat);
  constantMinLongDouble = ConstantFP::get(Type::getDoubleTy(Context), MinLongDouble);
  constantMaxLongFloat = ConstantFP::get(Type::getFloatTy(Context), MaxLongFloat);
  constantMaxIntDouble = ConstantFP::get(Type::getDoubleTy(Context), MaxIntDouble);
  constantMinIntDouble = ConstantFP::get(Type::getDoubleTy(Context), MinIntDouble);
  constantMaxLongDouble = ConstantFP::get(Type::getDoubleTy(Context), MaxLongDouble);
  constantMaxLongDouble = ConstantFP::get(Type::getDoubleTy(Context), MaxLongDouble);
  constantFloatInfinity = ConstantFP::get(Type::getFloatTy(Context), MaxFloat);
  constantFloatMinusInfinity = ConstantFP::get(Type::getFloatTy(Context), MinFloat);
  constantDoubleInfinity = ConstantFP::get(Type::getDoubleTy(Context), MaxDouble);
  constantDoubleMinusInfinity = ConstantFP::get(Type::getDoubleTy(Context), MinDouble);
  constantDoubleMinusZero = ConstantFP::get(Type::getDoubleTy(Context), -0.0);
  constantFloatMinusZero = ConstantFP::get(Type::getFloatTy(Context), -0.0f);
  // constantThreadIDMask = ConstantInt::get(pointerSizeType, mvm::System::GetThreadIDMask());
  constantPtrOne = ConstantInt::get(pointerSizeType, 1);
  constantPtrZero = ConstantInt::get(pointerSizeType, 0);

  constantPtrNull = Constant::getNullValue(ptrType); 
  constantPtrLogSize = ConstantInt::get(Type::getInt32Ty(Context), kWordSizeLog2);
  arrayPtrType = PointerType::getUnqual(ArrayType::get(Type::getInt8Ty(Context), 0));
  
  printFloatLLVM = module->getFunction("printFloat");
  printDoubleLLVM = module->getFunction("printDouble");
  printLongLLVM = module->getFunction("printLong");
  printIntLLVM = module->getFunction("printInt");
  printObjectLLVM = module->getFunction("printObject");

  func_llvm_sqrt_f64 = module->getFunction("llvm.sqrt.f64");
  func_llvm_sin_f64 = module->getFunction("llvm.sin.f64");
  func_llvm_cos_f64 = module->getFunction("llvm.cos.f64");
  
  func_llvm_tan_f64 = module->getFunction("tan");
  func_llvm_asin_f64 = module->getFunction("asin");
  func_llvm_acos_f64 = module->getFunction("acos");
  func_llvm_atan_f64 = module->getFunction("atan");
  func_llvm_exp_f64 = module->getFunction("exp");
  func_llvm_log_f64 = module->getFunction("log");
  func_llvm_ceil_f64 = module->getFunction("ceil");
  func_llvm_floor_f64 = module->getFunction("floor");
  func_llvm_cbrt_f64 = module->getFunction("cbrt");
  func_llvm_cosh_f64 = module->getFunction("cosh");
  func_llvm_expm1_f64 = module->getFunction("expm1");
  func_llvm_log10_f64 = module->getFunction("log10");
  func_llvm_log1p_f64 = module->getFunction("log1p");
  func_llvm_sinh_f64 = module->getFunction("sinh");
  func_llvm_tanh_f64 = module->getFunction("tanh");
  func_llvm_fabs_f64 = module->getFunction("fabs");
  func_llvm_rint_f64 = module->getFunction("rint");
    
  func_llvm_hypot_f64 = module->getFunction("hypot");
  func_llvm_pow_f64 = module->getFunction("pow");
  func_llvm_atan2_f64 = module->getFunction("atan2");
    
  func_llvm_fabs_f32 = module->getFunction("fabsf");

  llvm_memcpy_i32 = module->getFunction("llvm.memcpy.i32");
  llvm_memset_i32 = module->getFunction("llvm.memset.i32");
  // Set up function declaration for llvm.frameaddress
  std::vector<Type*> params;
  params.push_back(Type::getInt32Ty(Context));
  FunctionType* func = FunctionType::get(Type::getInt8PtrTy(Context, 0), params, false);
  llvm_frameaddress = Function::Create(func, GlobalValue::ExternalLinkage, "llvm.frameaddress", module); // module->getFunction("llvm.frameaddress");
  llvm_gc_gcroot = module->getFunction("llvm.gcroot");

  unconditionalSafePoint = module->getFunction("unconditionalSafePoint");
  conditionalSafePoint = module->getFunction("conditionalSafePoint");
  AllocateUnresolvedFunction = module->getFunction("gcmallocUnresolved");
  AddFinalizationCandidate = module->getFunction("addFinalizationCandidate");

  ArrayWriteBarrierFunction = module->getFunction("arrayWriteBarrier");
  FieldWriteBarrierFunction = module->getFunction("fieldWriteBarrier");
  NonHeapWriteBarrierFunction = module->getFunction("nonHeapWriteBarrier");
  AllocateFunction = module->getFunction("gcmalloc");

  SetjmpFunction = module->getFunction("_setjmp");
  RegisterSetjmpFunction = module->getFunction("registerSetjmp");
  UnregisterSetjmpFunction = module->getFunction("unregisterSetjmp");
}
