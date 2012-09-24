//===------------------ JIT.h - JIT facilities ----------------------------===//
//
//                     The Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_JIT_H
#define MVM_JIT_H

#include <cfloat>
#include <cmath>
#include <string>

#include "types.h"

#include "llvm/Target/TargetMachine.h"

#include "mvm/MethodInfo.h"
#include "mvm/GC/GC.h"

namespace llvm {
  class Constant;
  class ConstantFP;
  class ConstantInt;
  class ExecutionEngine;
  class Function;
  class FunctionPassManager;
  class GCFunctionInfo;
  class GCStrategy;
  class JIT;
  class Module;
  class PointerType;
  class TargetData;
  class TargetMachine;
  class Type;
}

namespace mvm {

class LockRecursive;

const double MaxDouble = +INFINITY; //1.0 / 0.0;
const double MinDouble = -INFINITY;//-1.0 / 0.0;
const double MaxLongDouble =  9223372036854775807.0;
const double MinLongDouble = -9223372036854775808.0;
const double MaxIntDouble = 2147483647.0;
const double MinIntDouble = -2147483648.0;
const uint64 MaxLong = 9223372036854775807LL;
const uint64 MinLong = -9223372036854775808ULL;
const uint32 MaxInt =  2147483647;
const uint32 MinInt =  -2147483648U;
const float MaxFloat = +INFINITY; //(float)(((float)1.0) / (float)0.0);
const float MinFloat = -INFINITY; //(float)(((float)-1.0) / (float)0.0);
const float MaxLongFloat = (float)9223372036854775807.0;
const float MinLongFloat = (float)-9223372036854775808.0;
const float MaxIntFloat = (float)2147483647.0;
const float MinIntFloat = (float)-2147483648.0;
const float NaNFloat = NAN; //(float)(((float)0.0) / (float)0.0);
const double NaNDouble = NAN; //0.0 / 0.0;

class BaseIntrinsics {

public:
  
  void init(llvm::Module*);
 
  llvm::Function* printFloatLLVM;
  llvm::Function* printDoubleLLVM;
  llvm::Function* printLongLLVM;
  llvm::Function* printIntLLVM;
  llvm::Function* printObjectLLVM;

  llvm::Function* func_llvm_fabs_f32;
  llvm::Function* func_llvm_fabs_f64;
  llvm::Function* func_llvm_sqrt_f64;
  llvm::Function* func_llvm_sin_f64;
  llvm::Function* func_llvm_cos_f64;
  llvm::Function* func_llvm_tan_f64;
  llvm::Function* func_llvm_asin_f64;
  llvm::Function* func_llvm_acos_f64;
  llvm::Function* func_llvm_atan_f64;
  llvm::Function* func_llvm_atan2_f64;
  llvm::Function* func_llvm_exp_f64;
  llvm::Function* func_llvm_log_f64;
  llvm::Function* func_llvm_pow_f64;
  llvm::Function* func_llvm_ceil_f64;
  llvm::Function* func_llvm_floor_f64;
  llvm::Function* func_llvm_rint_f64;
  llvm::Function* func_llvm_cbrt_f64;
  llvm::Function* func_llvm_cosh_f64;
  llvm::Function* func_llvm_expm1_f64;
  llvm::Function* func_llvm_hypot_f64;
  llvm::Function* func_llvm_log10_f64;
  llvm::Function* func_llvm_log1p_f64;
  llvm::Function* func_llvm_sinh_f64;
  llvm::Function* func_llvm_tanh_f64;

  llvm::Function* llvm_memcpy_i32;
  llvm::Function* llvm_memset_i32;
  llvm::Function* llvm_frameaddress;
  llvm::Function* llvm_gc_gcroot;

  llvm::Function* conditionalSafePoint;
  llvm::Function* unconditionalSafePoint;
  llvm::Function* AllocateFunction;
  llvm::Function* AllocateUnresolvedFunction;
  llvm::Function* AddFinalizationCandidate;
  llvm::Function* ArrayWriteBarrierFunction;
  llvm::Function* FieldWriteBarrierFunction;
  llvm::Function* NonHeapWriteBarrierFunction;

  llvm::Function* SetjmpFunction;
  llvm::Function* RegisterSetjmpFunction;
  llvm::Function* UnregisterSetjmpFunction;

  llvm::Constant* constantInt8Zero;
  llvm::Constant* constantZero;
  llvm::Constant* constantOne;
  llvm::Constant* constantTwo;
  llvm::Constant* constantThree;
  llvm::Constant* constantFour;
  llvm::Constant* constantFive;
  llvm::Constant* constantSix;
  llvm::Constant* constantSeven;
  llvm::Constant* constantEight;
  llvm::Constant* constantMinusOne;
  llvm::Constant* constantLongMinusOne;
  llvm::Constant* constantLongZero;
  llvm::Constant* constantLongOne;
  llvm::Constant* constantMinInt;
  llvm::Constant* constantMaxInt;
  llvm::Constant* constantMinLong;
  llvm::Constant* constantMaxLong;
  llvm::Constant* constantFloatZero;
  llvm::Constant* constantFloatOne;
  llvm::Constant* constantFloatTwo;
  llvm::Constant* constantDoubleZero;
  llvm::Constant* constantDoubleOne;
  llvm::Constant* constantMaxIntFloat;
  llvm::Constant* constantMinIntFloat;
  llvm::Constant* constantMinLongFloat;
  llvm::Constant* constantMinLongDouble;
  llvm::Constant* constantMaxLongFloat;
  llvm::Constant* constantMaxIntDouble;
  llvm::Constant* constantMinIntDouble;
  llvm::Constant* constantMaxLongDouble;
  llvm::Constant* constantDoubleInfinity;
  llvm::Constant* constantDoubleMinusInfinity;
  llvm::Constant* constantFloatInfinity;
  llvm::Constant* constantFloatMinusInfinity;
  llvm::Constant* constantFloatMinusZero;
  llvm::Constant* constantDoubleMinusZero;
  llvm::Constant* constantPtrNull;
  llvm::Constant* constantPtrLogSize;
  llvm::Constant* constantThreadIDMask;
  llvm::Constant* constantStackOverflowMask;
  llvm::Constant* constantFatMask;
  llvm::Constant* constantPtrOne;
  llvm::Constant* constantPtrZero;
  
  llvm::PointerType* ptrType;
  llvm::PointerType* ptr32Type;
  llvm::PointerType* ptrPtrType;
  llvm::Type* arrayPtrType;
  llvm::Type* pointerSizeType;
};


class MvmModule {
public:
   static mvm::LockRecursive protectEngine;

   static void runPasses(llvm::Function* func, llvm::FunctionPassManager*);
   static void initialise(int argc, char** argv);

   static Frames* addToVM(VirtualMachine* VM,
                          llvm::GCFunctionInfo* GFI,
                          llvm::JIT* jit,
                          mvm::BumpPtrAllocator& allocator,
                          void* meta);

   static int disassemble(unsigned int* addr);
  
   static void protectIR();
   static void unprotectIR();

   static void addCommandLinePasses(llvm::FunctionPassManager* PM);

   static const char* getHostTriple();
};

} // end namespace mvm

#endif // MVM_JIT_H
