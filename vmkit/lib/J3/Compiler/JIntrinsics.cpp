//===------------- JIntrinsics.cpp - Intrinsics for J3 -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/DerivedTypes.h"
#include "llvm/LLVMContext.h"
#include "llvm/Module.h"
#include "llvm/Type.h";

#include "j3/JIT.h"

#include "JavaAccess.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaTypes.h"
#include "JavaObject.h"

#include "j3/JIntrinsics.h"
#include "j3/LLVMInfo.h"

using namespace j3;
using namespace llvm;

//namespace j3 {
//  namespace llvm_runtime {
//  //fixme
////    #include "LLVMRuntime.inc"
//  }
//}

void JIntrinsics::createJavaClass() {
	javaClass = StructType::create(*Context, "JavaClass");
	// Temporary for now
	/*
	 * 	+0	access
	 * 	+4	super
	 * 	+8	field count
	 * 	+12	interface count
	 * 	+16	method count
	 * 	+20	static field count
	 * 	+24	static method count
	 * 	+28 attribute count
	 * 	+32 methods array *
	 * 	+36 interface array *
	 * 	+40 attributes array *
	 * 	+44 static field array *
	 * 	+48 static method *
	 */
	std::vector<Type*> javaClassFields;
	// access field
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// super
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// field count
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// interface count
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// method count
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// static field count
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// static method count
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// attribute count
	javaClassFields.push_back(IntegerType::getInt32Ty(*Context));
	// method array pointer
	javaClassFields.push_back(VTType);
	// interface array pointer
	javaClassFields.push_back(VTType);
	// attributes array pointer
	javaClassFields.push_back(VTType);
	// static field array pointer
	javaClassFields.push_back(VTType);
	// static method array pointer
	javaClassFields.push_back(VTType);
	javaClass->setBody(javaClassFields, false);
	JavaClassType = PointerType::getUnqual(javaClass);

}
void JIntrinsics::createJavaObjectType() {
	// All java objects have:
	// +0		Hashcode
	// +4		Lock
	// +8		JavaClass *
	// +12		instance fields
	// %JavaObject = type { i32, i32, %JavaClass*, i32, [0 x i32] }
	javaObject = StructType::create(*Context, "JavaObject");
	std::vector<Type*> javaObjectFields;
	javaObjectFields.push_back(IntegerType::getInt32Ty(*Context));
	javaObjectFields.push_back(IntegerType::getInt32Ty(*Context));
	javaObject->setBody(javaObjectFields, false);
	JavaObjectType = PointerType::getUnqual(javaObject);
}

void JIntrinsics::createVirtualTable() {
	// Virtual table is type { [ 0 x i32 (...)* ] }
	// function returning an integer
	FunctionType *func = FunctionType::get(Type::getInt32Ty(*Context), true);
	// pointer to a function
	PointerType *funcPtr = PointerType::getUnqual(func);
	// An aray of function pointers
	VTType = PointerType::getUnqual(ArrayType::get(funcPtr, 0));
}

void JIntrinsics::createJavaArray() {
	// type { %JavaObject, i32 }
	StructType *javaArray = StructType::create(*Context, "JavaArray");
	std::vector<Type*> javaArrayFields;
	javaArrayFields.push_back(javaObject);
	javaArrayFields.push_pack(Type::getInt32Ty(*Context));
	javaArray->setBody(javaArrayFields, false);
	JavaArrayType = PointerType::getUnqual(javaArray);
}

void JIntrinsics::createJavaClassPrimitive() {
	// type { JavaClass, i32 }
	StructType *javaClassPrim = StructType::create(*Context, "JavaClassPrimitive");
	std::vector<Type*> javaClassPrimFields;
	javaClassPrimFields.push_back(javaClass);
	// log2 size of the primitive
	javaClassPrimFields.push_back(Type::getInt32Ty(*Context));
	javaClassPrim->setBody(javaClassPrimFields, false);
	JavaClassPrimitiveType = PointerType::getUnqual(javaClassPrim);
}

void JIntrinsics::createJavaClassArray() {
	// type { JavaClass, JavaClass * }
	StructType* javaClassArray = StructType::create(*Context, "JavaClassArray");
	std::vector<Type*> javaClassArrayFields;
	javaClassArrayFields.push_back(javaClass);
	javaClassArrayFields.push_back(JavaClassType);
	javaClassArray->setBody(javaClassArrayFields, false);
	JavaClassArrayType = PointerType::getUnqual(javaClassArray);

}
void JIntrinsics::initTypes() {
	createVirtualTable();
	createJavaClass();
	createJavaObjectType();
	createJavaArray();
	createJavaClassPrimitive();
}
void JIntrinsics::init(llvm::Module* module) {
  BaseIntrinsics::init(module);

  Context = &module->getContext();
  initTypes();

  ResolvedConstantPoolType = ptrPtrType;
 
  
  ClassBytesType =
    PointerType::getUnqual(module->getTypeByName("ClassBytes"));
  JavaConstantPoolType =
    PointerType::getUnqual(module->getTypeByName("JavaConstantPool"));
  
  JavaArrayUInt8Type =
    PointerType::getUnqual(module->getTypeByName("ArrayUInt8"));
  JavaArraySInt8Type =
    PointerType::getUnqual(module->getTypeByName("ArraySInt8"));
  JavaArrayUInt16Type =
    PointerType::getUnqual(module->getTypeByName("ArrayUInt16"));
  JavaArraySInt16Type =
    PointerType::getUnqual(module->getTypeByName("ArraySInt16"));
  JavaArrayUInt32Type =
    PointerType::getUnqual(module->getTypeByName("ArrayUInt32"));
  JavaArraySInt32Type =
    PointerType::getUnqual(module->getTypeByName("ArraySInt32"));
  JavaArrayLongType =
    PointerType::getUnqual(module->getTypeByName("ArrayLong"));
  JavaArrayFloatType =
    PointerType::getUnqual(module->getTypeByName("ArrayFloat"));
  JavaArrayDoubleType =
    PointerType::getUnqual(module->getTypeByName("ArrayDouble"));
  JavaArrayObjectType =
    PointerType::getUnqual(module->getTypeByName("ArrayObject"));

  JavaFieldType =
    PointerType::getUnqual(module->getTypeByName("JavaField"));
  JavaMethodType =
    PointerType::getUnqual(module->getTypeByName("JavaMethod"));
  UTF8Type =
    PointerType::getUnqual(module->getTypeByName("UTF8"));
  AttributType =
    PointerType::getUnqual(module->getTypeByName("Attribut"));
  JavaThreadType =
    PointerType::getUnqual(module->getTypeByName("JavaThread"));
  MutatorThreadType =
    PointerType::getUnqual(module->getTypeByName("MutatorThread"));
  
  J3DenseMapType =
    PointerType::getUnqual(module->getTypeByName("J3DenseMap"));
  
  
  JavaObjectNullConstant =
    Constant::getNullValue(JIntrinsics::JavaObjectType);
  MaxArraySizeConstant = ConstantInt::get(Type::getInt32Ty(*Context),
                                          JavaArray::MaxArraySize);
  JavaArraySizeConstant = ConstantInt::get(Type::getInt32Ty(*Context),
                                          /*&sizeof(JavaObject)*/ 8 + sizeof(ssize_t));
  
  
  JavaArrayElementsOffsetConstant = constantTwo;
  JavaArraySizeOffsetConstant = constantOne;
  JavaObjectLockOffsetConstant = constantOne;
  JavaObjectVTOffsetConstant = constantZero;

  OffsetClassInVTConstant =
    ConstantInt::get(Type::getInt32Ty(*Context),
                     JavaVirtualTable::getClassIndex());
  OffsetDepthInVTConstant =
    ConstantInt::get(Type::getInt32Ty(*Context),
                     JavaVirtualTable::getDepthIndex());
  OffsetDisplayInVTConstant =
    ConstantInt::get(Type::getInt32Ty(*Context),
                     JavaVirtualTable::getDisplayIndex());
  OffsetBaseClassVTInVTConstant =
    ConstantInt::get(Type::getInt32Ty(*Context),
                     JavaVirtualTable::getBaseClassIndex());
  OffsetIMTInVTConstant =
    ConstantInt::get(Type::getInt32Ty(*Context),
                     JavaVirtualTable::getIMTIndex());
  
  OffsetAccessInCommonClassConstant = constantOne;
  IsArrayConstant = ConstantInt::get(Type::getInt16Ty(*Context),
                                     JNJVM_ARRAY);
  
  IsPrimitiveConstant = ConstantInt::get(Type::getInt16Ty(*Context),
                                         JNJVM_PRIMITIVE);
 
  OffsetBaseClassInArrayClassConstant = constantOne;
  OffsetLogSizeInPrimitiveClassConstant = constantOne;

  OffsetObjectSizeInClassConstant = constantOne;
  OffsetVTInClassConstant = ConstantInt::get(Type::getInt32Ty(*Context), 7);
  OffsetTaskClassMirrorInClassConstant = constantThree;
  OffsetStaticInstanceInTaskClassMirrorConstant = constantThree;
  OffsetStatusInTaskClassMirrorConstant = constantZero;
  OffsetInitializedInTaskClassMirrorConstant = constantOne;
  
  OffsetIsolateIDInThreadConstant =         ConstantInt::get(Type::getInt32Ty(*Context), 1);
  OffsetVMInThreadConstant =                ConstantInt::get(Type::getInt32Ty(*Context), 2);
  OffsetDoYieldInThreadConstant =           ConstantInt::get(Type::getInt32Ty(*Context), 4);
	OffsetThreadInMutatorThreadConstant =     ConstantInt::get(Type::getInt32Ty(*Context), 0);
  OffsetJNIInJavaThreadConstant =           ConstantInt::get(Type::getInt32Ty(*Context), 1);
  OffsetJavaExceptionInJavaThreadConstant = ConstantInt::get(Type::getInt32Ty(*Context), 2);
  

  InterfaceLookupFunction = module->getFunction("j3InterfaceLookup");
  MultiCallNewFunction = module->getFunction("j3MultiCallNew");
  ForceLoadedCheckFunction = module->getFunction("forceLoadedCheck");
  InitialisationCheckFunction = module->getFunction("initialisationCheck");
  ForceInitialisationCheckFunction = 
    module->getFunction("forceInitialisationCheck");
  InitialiseClassFunction = module->getFunction("j3RuntimeInitialiseClass");
  
  GetConstantPoolAtFunction = module->getFunction("getConstantPoolAt");
  ArrayLengthFunction = module->getFunction("arrayLength");
  GetVTFunction = module->getFunction("getVT");
  GetIMTFunction = module->getFunction("getIMT");
  GetClassFunction = module->getFunction("getClass");
  ClassLookupFunction = module->getFunction("j3ClassLookup");
  GetVTFromClassFunction = module->getFunction("getVTFromClass");
  GetVTFromClassArrayFunction = module->getFunction("getVTFromClassArray");
  GetVTFromCommonClassFunction = module->getFunction("getVTFromCommonClass");
  GetBaseClassVTFromVTFunction = module->getFunction("getBaseClassVTFromVT");
  GetObjectSizeFromClassFunction = 
    module->getFunction("getObjectSizeFromClass");
 
  GetClassDelegateeFunction = module->getFunction("getClassDelegatee");
  RuntimeDelegateeFunction = module->getFunction("j3RuntimeDelegatee");
  IsAssignableFromFunction = module->getFunction("isAssignableFrom");
  IsSecondaryClassFunction = module->getFunction("isSecondaryClass");
  GetDepthFunction = module->getFunction("getDepth");
  GetStaticInstanceFunction = module->getFunction("getStaticInstance");
  GetDisplayFunction = module->getFunction("getDisplay");
  GetVTInDisplayFunction = module->getFunction("getVTInDisplay");
  AquireObjectFunction = module->getFunction("j3JavaObjectAquire");
  ReleaseObjectFunction = module->getFunction("j3JavaObjectRelease");

  VirtualFieldLookupFunction = module->getFunction("j3VirtualFieldLookup");
  StaticFieldLookupFunction = module->getFunction("j3StaticFieldLookup");
  StringLookupFunction = module->getFunction("j3StringLookup");
  StartJNIFunction = module->getFunction("j3StartJNI");
  EndJNIFunction = module->getFunction("j3EndJNI");
  
  ResolveVirtualStubFunction = module->getFunction("j3ResolveVirtualStub");
  ResolveStaticStubFunction = module->getFunction("j3ResolveStaticStub");
  ResolveSpecialStubFunction = module->getFunction("j3ResolveSpecialStub");
  ResolveInterfaceFunction = module->getFunction("j3ResolveInterface");
  
  NullPointerExceptionFunction =
    module->getFunction("j3NullPointerException");
  ClassCastExceptionFunction = module->getFunction("j3ClassCastException");
  IndexOutOfBoundsExceptionFunction = 
    module->getFunction("j3IndexOutOfBoundsException");
  NegativeArraySizeExceptionFunction = 
    module->getFunction("j3NegativeArraySizeException");
  OutOfMemoryErrorFunction = module->getFunction("j3OutOfMemoryError");
  StackOverflowErrorFunction = module->getFunction("j3StackOverflowError");
  ArrayStoreExceptionFunction = module->getFunction("j3ArrayStoreException");
  ArithmeticExceptionFunction = module->getFunction("j3ArithmeticException");

  PrintExecutionFunction = module->getFunction("j3PrintExecution");
  PrintMethodStartFunction = module->getFunction("j3PrintMethodStart");
  PrintMethodEndFunction = module->getFunction("j3PrintMethodEnd");

  ThrowExceptionFunction = module->getFunction("j3ThrowException");

  GetArrayClassFunction = module->getFunction("j3GetArrayClass");
 
  GetFinalInt8FieldFunction = module->getFunction("getFinalInt8Field");
  GetFinalInt16FieldFunction = module->getFunction("getFinalInt16Field");
  GetFinalInt32FieldFunction = module->getFunction("getFinalInt32Field");
  GetFinalLongFieldFunction = module->getFunction("getFinalLongField");
  GetFinalFloatFieldFunction = module->getFunction("getFinalFloatField");
  GetFinalDoubleFieldFunction = module->getFunction("getFinalDoubleField");

  VirtualLookupFunction = module->getFunction("j3VirtualTableLookup");

  GetLockFunction = module->getFunction("getLock");
  ThrowExceptionFromJITFunction =
    module->getFunction("j3ThrowExceptionFromJIT"); 
}
