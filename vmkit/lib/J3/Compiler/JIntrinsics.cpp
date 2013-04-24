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
#include "llvm/Type.h"

#include "j3/JIT.h"

#include "JavaAccess.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaTypes.h"
#include "JavaObject.h"

#include "j3/JIntrinsics.h"
#include "j3/LLVMInfo.h"

#include <vector>

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
	// %JavaObject = type { i32, i32, %JavaClass* }
	javaObject = StructType::create(*Context, "JavaObject");
	std::vector<Type*> javaObjectFields;
	javaObjectFields.push_back(IntegerType::getInt32Ty(*Context));
	javaObjectFields.push_back(IntegerType::getInt32Ty(*Context));
	javaObjectFields.push_back(JavaClassType);
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
	javaArrayFields.push_back(Type::getInt32Ty(*Context));
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

void JIntrinsics::createArrayUInt8() {
	// type { JavaObject, i32, [ 0 x i8 ] }
	StructType* javaUint8Array = StructType::create(*Context, "ArrayUInt8");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt8Ty(*Context), 0);
	fields.push_back(array);
	javaUint8Array->setBody(fields, false);
	JavaArrayUInt8Type = PointerType::getUnqual(javaUint8Array);
}

void JIntrinsics::createArraySInt8() {
	// type { JavaObject, i32, [ 0 x i8 ] }
	StructType* type = StructType::create(*Context, "ArraySInt8");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt8Ty(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArraySInt8Type = PointerType::getUnqual(type);
}

void JIntrinsics::createArrayUInt16() {
	// type { JavaObject, i32, [ 0 x i16 ] }
	StructType* type = StructType::create(*Context, "ArrayUInt16");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt16Ty(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArrayUInt16Type = PointerType::getUnqual(type);
}

void JIntrinsics::createArraySInt16() {
	// type { JavaObject, i32, [ 0 x i16 ] }
	StructType* type = StructType::create(*Context, "ArraySInt16");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt16Ty(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArraySInt16Type = PointerType::getUnqual(type);
}

void JIntrinsics::createArrayUInt32() {
	// type { JavaObject, i32, [ 0 x i32 ] }
	StructType* type = StructType::create(*Context, "ArrayUInt32");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt32Ty(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArrayUInt32Type = PointerType::getUnqual(type);
}

void JIntrinsics::createArraySInt32() {
	// type { JavaObject, i32, [ 0 x i32 ] }
	StructType* type = StructType::create(*Context, "ArraySInt32");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt32Ty(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArraySInt32Type = PointerType::getUnqual(type);
}

void JIntrinsics::createArrayLong() {
	// type { JavaObject, i32, [ 0 x i64 ] }
	StructType* type = StructType::create(*Context, "ArrayLong");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getInt64Ty(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArrayLongType = PointerType::getUnqual(type);
}

void JIntrinsics::createArrayFloat() {
	// type { JavaObject, i32, [ 0 x float ] }
	StructType* type = StructType::create(*Context, "ArrayFloat");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getFloatTy(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArrayFloatType = PointerType::getUnqual(type);
}

void JIntrinsics::createArrayDouble() {
	// type { JavaObject, i32, [ 0 x double ] }
	StructType* type = StructType::create(*Context, "ArrayDouble");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(Type::getDoubleTy(*Context), 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArrayDoubleType = PointerType::getUnqual(type);
}

void JIntrinsics::createArrayObject() {
	// type { JavaObject, i32, [ 0 x double ] }
	StructType* type = StructType::create(*Context, "ArrayObject");
	std::vector<Type*> fields;
	fields.push_back(javaObject);
	fields.push_back(Type::getInt32Ty(*Context));
	ArrayType* array = ArrayType::get(JavaObjectType, 0);
	fields.push_back(array);
	type->setBody(fields, false);
	JavaArrayObjectType = PointerType::getUnqual(type);
}

void JIntrinsics::initTypes() {
	createVirtualTable();
	createJavaClass();
	createJavaObjectType();
	createJavaArray();
	createJavaClassPrimitive();
	createArrayUInt8();
	createArraySInt8();
	createArrayUInt16();
	createArraySInt16();
	createArrayUInt32();
	createArraySInt32();
	createArrayLong();
	createArrayFloat();
	createArrayDouble();
	createArrayObject();
}

void JIntrinsics::init(llvm::Module* module) {
  BaseIntrinsics::init(module);

  Context = &module->getContext();
  initTypes();

  ResolvedConstantPoolType = ptrPtrType;
 

  JavaFieldType =
    PointerType::getUnqual(module->getTypeByName("JavaField"));
  JavaMethodType =
    PointerType::getUnqual(module->getTypeByName("JavaMethod"));
  UTF8Type =
    PointerType::getUnqual(module->getTypeByName("UTF8"));
  AttributType =
    PointerType::getUnqual(module->getTypeByName("Attribut"));
  
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
