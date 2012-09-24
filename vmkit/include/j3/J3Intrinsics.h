//===-------------- J3Intrinsics.h - Intrinsics of J3 ---------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef J3_INTRINSICS_H
#define J3_INTRINSICS_H

#include "mvm/JIT.h"

namespace j3 {

class J3Intrinsics : public mvm::BaseIntrinsics {

public:
  void init(llvm::Module* M);

  llvm::Type* JavaArrayUInt8Type;
  llvm::Type* JavaArraySInt8Type;
  llvm::Type* JavaArrayUInt16Type;
  llvm::Type* JavaArraySInt16Type;
  llvm::Type* JavaArrayUInt32Type;
  llvm::Type* JavaArraySInt32Type;
  llvm::Type* JavaArrayLongType;
  llvm::Type* JavaArrayFloatType;
  llvm::Type* JavaArrayDoubleType;
  llvm::Type* JavaArrayObjectType;
  
  llvm::Type* VTType;
  llvm::Type* JavaObjectType;
  llvm::Type* JavaArrayType;
  llvm::Type* JavaCommonClassType;
  llvm::Type* JavaClassType;
  llvm::Type* JavaClassArrayType;
  llvm::Type* JavaClassPrimitiveType;
  llvm::Type* ClassBytesType;
  llvm::Type* JavaConstantPoolType;
  llvm::Type* ResolvedConstantPoolType;
  llvm::Type* UTF8Type;
  llvm::Type* JavaMethodType;
  llvm::Type* JavaFieldType;
  llvm::Type* AttributType;
  llvm::Type* JavaThreadType;
  llvm::Type* MutatorThreadType;
  llvm::Type* J3DenseMapType;
  
  llvm::Function* StartJNIFunction;
  llvm::Function* EndJNIFunction;
  llvm::Function* InterfaceLookupFunction;
  llvm::Function* VirtualFieldLookupFunction;
  llvm::Function* StaticFieldLookupFunction;
  llvm::Function* PrintExecutionFunction;
  llvm::Function* PrintMethodStartFunction;
  llvm::Function* PrintMethodEndFunction;
  llvm::Function* InitialiseClassFunction;
  llvm::Function* InitialisationCheckFunction;
  llvm::Function* ForceInitialisationCheckFunction;
  llvm::Function* ForceLoadedCheckFunction;
  llvm::Function* ClassLookupFunction;
  llvm::Function* StringLookupFunction;
  
  llvm::Function* ResolveVirtualStubFunction;
  llvm::Function* ResolveSpecialStubFunction;
  llvm::Function* ResolveStaticStubFunction;
  llvm::Function* ResolveInterfaceFunction;

  llvm::Function* VirtualLookupFunction;
  llvm::Function* IsAssignableFromFunction;
  llvm::Function* IsSecondaryClassFunction;
  llvm::Function* GetDepthFunction;
  llvm::Function* GetDisplayFunction;
  llvm::Function* GetVTInDisplayFunction;
  llvm::Function* GetStaticInstanceFunction;
  llvm::Function* AquireObjectFunction;
  llvm::Function* ReleaseObjectFunction;
  llvm::Function* GetConstantPoolAtFunction;
  llvm::Function* MultiCallNewFunction;
  llvm::Function* GetArrayClassFunction;

  llvm::Function* GetClassDelegateeFunction;
  llvm::Function* RuntimeDelegateeFunction;
  llvm::Function* ArrayLengthFunction;
  llvm::Function* GetVTFunction;
  llvm::Function* GetIMTFunction;
  llvm::Function* GetClassFunction;
  llvm::Function* GetVTFromClassFunction;
  llvm::Function* GetVTFromClassArrayFunction;
  llvm::Function* GetVTFromCommonClassFunction;
  llvm::Function* GetObjectSizeFromClassFunction;
  llvm::Function* GetBaseClassVTFromVTFunction;

  llvm::Function* GetLockFunction;
  
  llvm::Function* GetFinalInt8FieldFunction;
  llvm::Function* GetFinalInt16FieldFunction;
  llvm::Function* GetFinalInt32FieldFunction;
  llvm::Function* GetFinalLongFieldFunction;
  llvm::Function* GetFinalFloatFieldFunction;
  llvm::Function* GetFinalDoubleFieldFunction;
  
  llvm::Constant* JavaArraySizeOffsetConstant;
  llvm::Constant* JavaArrayElementsOffsetConstant;
  llvm::Constant* JavaObjectLockOffsetConstant;
  llvm::Constant* JavaObjectVTOffsetConstant;

  llvm::Constant* OffsetAccessInCommonClassConstant;
  llvm::Constant* IsArrayConstant;
  llvm::Constant* IsPrimitiveConstant;
  llvm::Constant* OffsetObjectSizeInClassConstant;
  llvm::Constant* OffsetVTInClassConstant;
  llvm::Constant* OffsetTaskClassMirrorInClassConstant;
  llvm::Constant* OffsetStaticInstanceInTaskClassMirrorConstant;
  llvm::Constant* OffsetInitializedInTaskClassMirrorConstant;
  llvm::Constant* OffsetStatusInTaskClassMirrorConstant;
  
  llvm::Constant* OffsetDoYieldInThreadConstant;
  llvm::Constant* OffsetIsolateIDInThreadConstant;
  llvm::Constant* OffsetVMInThreadConstant;
	llvm::Constant* OffsetThreadInMutatorThreadConstant;
  llvm::Constant* OffsetJNIInJavaThreadConstant;
  llvm::Constant* OffsetJavaExceptionInJavaThreadConstant;
  
  llvm::Constant* OffsetClassInVTConstant;
  llvm::Constant* OffsetDepthInVTConstant;
  llvm::Constant* OffsetDisplayInVTConstant;
  llvm::Constant* OffsetBaseClassVTInVTConstant;
  llvm::Constant* OffsetIMTInVTConstant;
  
  llvm::Constant* OffsetBaseClassInArrayClassConstant;
  llvm::Constant* OffsetLogSizeInPrimitiveClassConstant;
  
  llvm::Constant* ClassReadyConstant;

  llvm::Constant* JavaObjectNullConstant;
  llvm::Constant* MaxArraySizeConstant;
  llvm::Constant* JavaArraySizeConstant;

  llvm::Function* ThrowExceptionFunction;
  llvm::Function* NullPointerExceptionFunction;
  llvm::Function* IndexOutOfBoundsExceptionFunction;
  llvm::Function* ClassCastExceptionFunction;
  llvm::Function* OutOfMemoryErrorFunction;
  llvm::Function* StackOverflowErrorFunction;
  llvm::Function* NegativeArraySizeExceptionFunction;
  llvm::Function* ArrayStoreExceptionFunction;
  llvm::Function* ArithmeticExceptionFunction;
  llvm::Function* ThrowExceptionFromJITFunction;
};

}

#endif
