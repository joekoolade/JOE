//===----- JavaAOTCompiler.cpp - Support for Ahead of Time Compiler --------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//
#include <cassert>

#include "llvm/BasicBlock.h"
#include "llvm/Constants.h"
#include "llvm/Instructions.h"
#include "llvm/LLVMContext.h"
#include "llvm/Module.h"
#include "llvm/PassManager.h"
#include "llvm/Support/CodeGen.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/TargetRegistry.h"
#include "llvm/Target/TargetData.h"

#include "UTF8.h"

#include "j3/JIntrinsics.h"
#include "j3/JavaAOTCompiler.h"
#include "j3/JavaJITCompiler.h"

#include "JavaArray.h"
#include "JavaConstantPool.h"
#include "JavaString.h"
#include "JavaTypes.h"
#include "ClassLoader.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "Reader.h"
#include "Zip.h"
#include "JMap.h"

#include <cstdio>
// for stat, S_IFMT and S_IFDIR
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
// for PATH_MAX
#include <limits.h>
// for realpath()
#include <stdlib.h>
#include <iostream>

using namespace j3;
using namespace llvm;

const char* JavaAOTCompiler::dirSeparator = "/";
const char* JavaAOTCompiler::envSeparator = ":";
const UTF8* JavaCompiler::InlinePragma;
const UTF8* JavaCompiler::NoInlinePragma;

bool JavaAOTCompiler::isCompiling(const CommonClass* cl) const {
	return true;
}

void JavaAOTCompiler::AddInitializerToClass(GlobalVariable* varGV,
		CommonClass* classDef) {
	if (classDef->isClass() && isCompiling(classDef)) {
		Constant* C = CreateConstantFromClass(classDef->asClass());
		varGV->setInitializer(C);
	} else if (classDef->isArray()) {
		Constant* C = CreateConstantFromClassArray(classDef->asArrayClass());
		varGV->setInitializer(C);
	} else if (classDef->isPrimitive() && compileRT) {
		Constant* C = CreateConstantFromClassPrimitive(
				classDef->asPrimitiveClass());
		varGV->setInitializer(C);
	}
}

/**
 * Checks if class has a global variable assigned to it. If not then one will be
 * assigend to it.
 */
GlobalVariable* JavaAOTCompiler::getNativeClass(CommonClass* classDef) {

	if (classDef->isClass()) {

		native_class_iterator End = nativeClasses.end();
		native_class_iterator I = nativeClasses.find(classDef);
		if (I == End) {
			llvm::Type* Ty = NULL;

			if (classDef->isArray()) {
				Ty = JavaIntrinsics.JavaClassArrayType->getContainedType(0);
			} else if (classDef->isPrimitive()) {
				Ty = JavaIntrinsics.JavaClassPrimitiveType->getContainedType(0);
			} else {
				Ty = JavaIntrinsics.JavaClassType->getContainedType(0);
			}

			GlobalVariable* varGV = new GlobalVariable(*getLLVMModule(), Ty,
					false, GlobalValue::ExternalLinkage, 0,
					UTF8Buffer(classDef->name).toCompileName()->cString());

			nativeClasses.insert(std::make_pair(classDef, varGV));

			if (!precompile || classDef->isPrimitive()) {
				AddInitializerToClass(varGV, classDef);
			}

			return varGV;
		} else {
			return I->second;
		}
	} else if (classDef->isArray()) {
		array_class_iterator End = arrayClasses.end();
		array_class_iterator I = arrayClasses.find(classDef->asArrayClass());
		if (I == End) {
			llvm::Type* Ty = JavaIntrinsics.JavaClassArrayType;
			Module& Mod = *getLLVMModule();

			GlobalVariable* varGV = new GlobalVariable(Mod, Ty, false,
					GlobalValue::InternalLinkage, Constant::getNullValue(Ty),
					UTF8Buffer(classDef->name).toCompileName()->cString());

			arrayClasses.insert(
					std::make_pair(classDef->asArrayClass(), varGV));
			return varGV;
		} else {
			return I->second;
		}
	} else {
		assert(0 && "Implement me");
	}
	return 0;
}

Constant* JavaAOTCompiler::getNativeFunction(JavaMethod* meth, void* ptr) {
	llvm::Constant* varGV = 0;
	native_function_iterator End = nativeFunctions.end();
	native_function_iterator I = nativeFunctions.find(meth);
	if (I == End) {

		LLVMSignatureInfo* LSI = getSignatureInfo(meth->getSignature());
		llvm::Type* valPtrType = LSI->getNativePtrType();

		Module& Mod = *getLLVMModule();
		varGV = new GlobalVariable(Mod, valPtrType, false,
				GlobalValue::InternalLinkage,
				Constant::getNullValue(valPtrType), "");

		nativeFunctions.insert(std::make_pair(meth, varGV));
		return varGV;
	} else {
		return I->second;
	}
}
Constant* JavaAOTCompiler::getResolvedConstantPool(JavaConstantPool* ctp) {
    return NULL;
}
Constant* JavaAOTCompiler::getMethodInClass(JavaMethod* meth) {
	Class* cl = meth->classDef;
	Constant* MOffset = 0;
	Constant* Array = 0;

	for (uint32 i = 0; i < cl->nbVirtualMethods + cl->nbStaticMethods; ++i) {
		if (&cl->virtualMethods[i] == meth) {
			MOffset = ConstantInt::get(Type::getInt32Ty(getLLVMContext()), i);
			break;
		}
	}
	assert(MOffset && "No offset for method");

	method_iterator SI = virtualMethods.find(cl);
	if (SI != virtualMethods.end()) {
		Array = SI->second;
		assert(Array && "No array in class");
	} else {
		std::string name(UTF8Buffer(cl->name).toCompileName()->cString());
		name += "_VirtualMethods";
		Module& Mod = *getLLVMModule();
		Type* ATy = ArrayType::get(
				JavaIntrinsics.JavaMethodType->getContainedType(0),
				cl->nbVirtualMethods + cl->nbStaticMethods);

		Array = new GlobalVariable(Mod, ATy, false,
				GlobalValue::ExternalLinkage, 0, name);
		virtualMethods.insert(std::make_pair(cl, Array));
	}

	Constant* GEPs[2] = { getIntrinsics()->constantZero, MOffset };
	return ConstantExpr::getGetElementPtr(Array, GEPs, 2);
}

Constant* JavaAOTCompiler::getString(JavaString* str) {
	string_iterator SI = strings.find(str);
	if (SI != strings.end()) {
		return SI->second;
	} else {
		assert(str && "No string given");
		LLVMClassInfo* LCI = 0; //getClassInfo(JavaObject::getClass(str)->asClass());
		llvm::Type* Ty = LCI->getVirtualType();
		Module& Mod = *getLLVMModule();

		const char* name = JavaString::strToAsciiz(str);
		GlobalVariable* varGV = new GlobalVariable(Mod, Ty->getContainedType(0),
				false, GlobalValue::ExternalLinkage, 0, "str");
		delete[] name;
		Constant* res = ConstantExpr::getCast(Instruction::BitCast, varGV,
				JavaIntrinsics.JavaObjectType);
		strings.insert(std::make_pair(str, res));
		Constant* C = CreateConstantFromJavaString(str);
		varGV->setInitializer(C);
		return res;
	}
}

Constant* JavaAOTCompiler::getClassBytes(const UTF8* className, ClassBytes* bytes) {
	class_bytes_iterator CI = classBytes.find(bytes);
	if (CI != classBytes.end()) {
		return CI->second;
	}

	std::vector<Type*> Elemts;
	ArrayType* ATy = ArrayType::get(Type::getInt8Ty(getLLVMContext()),
			bytes->size);
	Elemts.push_back(Type::getInt32Ty(getLLVMContext()));
	Elemts.push_back(ATy);
	StructType* STy = StructType::get(getLLVMContext(), Elemts);

	std::string name(UTF8Buffer(className).toCompileName("_bytes")->cString());
	Constant* C = emitClassBytes ? CreateConstantFromClassBytes(bytes) : NULL;
	GlobalVariable* varGV = new GlobalVariable(*getLLVMModule(), STy, false,
			GlobalValue::ExternalLinkage, C, name);
	classBytes[bytes] = varGV;
	return varGV;
}

Constant* JavaAOTCompiler::getStringPtr(JavaString** str) {
	fprintf(stderr, "Implement me");
	abort();
}

Constant* JavaAOTCompiler::getJavaClass(CommonClass* cl) {
	return Constant::getNullValue(JavaIntrinsics.JavaObjectType);
}

Constant* JavaAOTCompiler::getJavaClassPtr(CommonClass* cl) {
	// Make sure it's emitted.
	getJavaClass(cl);

	Constant* Cl = getNativeClass(cl);
	Cl = ConstantExpr::getBitCast(Cl, JavaIntrinsics.JavaClassType);

	Constant* GEP[2] = { getIntrinsics()->constantZero,
			getIntrinsics()->constantZero };

	Constant* TCMArray = ConstantExpr::getGetElementPtr(Cl, GEP, 2);

	Constant* GEP2[2] = { getIntrinsics()->constantZero,
			getIntrinsics()->constantZero };

	Constant* Ptr = ConstantExpr::getGetElementPtr(TCMArray, GEP2, 2);
	return Ptr;
}

JavaObject* JavaAOTCompiler::getFinalObject(llvm::Value* obj) {
	if (Constant* CI = dyn_cast<Constant>(obj)) {
		reverse_final_object_iterator End = reverseFinalObjects.end();
		reverse_final_object_iterator I = reverseFinalObjects.find(CI);
		if (I != End)
			return I->second;
	}

	return 0;
}

//Constant* JavaAOTCompiler::HandleMagic(CommonClass* objCl) {
//
//	static const UTF8* AddressArray = asciizConstructUTF8(
//			"org/vmmagic/unboxed/AddressArray");
//	static const UTF8* WordArray = asciizConstructUTF8(
//			"org/vmmagic/unboxed/WordArray");
//	static const UTF8* ExtentArray = asciizConstructUTF8(
//			"org/vmmagic/unboxed/ExtentArray");
//	static const UTF8* ObjectReferenceArray = asciizConstructUTF8(
//			"org/vmmagic/unboxed/ObjectReferenceArray");
//	static const UTF8* OffsetArray = asciizConstructUTF8(
//			"org/vmmagic/unboxed/OffsetArray");
//	const UTF8* name = objCl->name;
//
//	if (name->equals(AddressArray) || name->equals(WordArray)
//			|| name->equals(ExtentArray) || name->equals(ObjectReferenceArray)
//			|| name->equals(OffsetArray)) {
//
//		word_t* realObj = (word_t*) obj;
//		word_t size = realObj[0];
//
//		ArrayType* ATy = ArrayType::get(JavaIntrinsics.JavaObjectType,
//				size + 1);
//
//		std::vector<Constant*> Vals;
//		for (uint32 i = 0; i < size + 1; ++i) {
//			Constant* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
//					uint64_t(realObj[i]));
//			CI = ConstantExpr::getIntToPtr(CI, JavaIntrinsics.JavaObjectType);
//			Vals.push_back(CI);
//		}
//
//		Constant* CA = ConstantArray::get(ATy, Vals);
//
//		GlobalVariable* varGV = new GlobalVariable(*getLLVMModule(),
//				CA->getType(), false, GlobalValue::InternalLinkage, CA, "");
//
//		return ConstantExpr::getBitCast(varGV, JavaIntrinsics.JavaObjectType);
//
//	} else {
//		Constant* CI = ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
//				uint64_t(obj));
//		CI = ConstantExpr::getIntToPtr(CI, JavaIntrinsics.JavaObjectType);
//		return CI;
//	}
//}

/*
Constant* JavaAOTCompiler::getFinalObject(CommonClass* objCl) {
	assert(!useCooperativeGC());
	llvm::GlobalVariable* varGV = 0;
	final_object_iterator End = finalObjects.end();
	final_object_iterator I = finalObjects.find(obj);
	if (I == End) {

//    if (mvm::Collector::begOf(obj)) {
		if (0) {
			Type* Ty = 0;
			CommonClass* cl = JavaObject::getClass(obj);

			if (cl->isArray()) {
				Classpath* upcalls = cl->classLoader->bootstrapLoader->upcalls;
				CommonClass* subClass = cl->asArrayClass()->baseClass();
				if (subClass->isPrimitive()) {
					if (subClass == upcalls->OfBool) {
						Ty = Type::getInt8Ty(getLLVMContext());
					} else if (subClass == upcalls->OfByte) {
						Ty = Type::getInt8Ty(getLLVMContext());
					} else if (subClass == upcalls->OfShort) {
						Ty = Type::getInt16Ty(getLLVMContext());
					} else if (subClass == upcalls->OfChar) {
						Ty = Type::getInt16Ty(getLLVMContext());
					} else if (subClass == upcalls->OfInt) {
						Ty = Type::getInt32Ty(getLLVMContext());
					} else if (subClass == upcalls->OfFloat) {
						Ty = Type::getFloatTy(getLLVMContext());
					} else if (subClass == upcalls->OfLong) {
						Ty = Type::getInt64Ty(getLLVMContext());
					} else if (subClass == upcalls->OfDouble) {
						Ty = Type::getDoubleTy(getLLVMContext());
					} else {
						abort();
					}
				} else {
					Ty = JavaIntrinsics.JavaObjectType;
				}

				std::vector<Type*> Elemts;
				ArrayType* ATy = ArrayType::get(Ty, JavaArray::getSize(obj));
				Elemts.push_back(
						JavaIntrinsics.JavaObjectType->getContainedType(0));
				Elemts.push_back(JavaIntrinsics.pointerSizeType);
				Elemts.push_back(ATy);
				Ty = StructType::get(getLLVMModule()->getContext(), Elemts);

			} else {
				LLVMClassInfo* LCI = getClassInfo(cl->asClass());
				Ty = LCI->getVirtualType()->getContainedType(0);
			}

			Module& Mod = *getLLVMModule();
			// Set as External, so that inlining MMTk code works.
			varGV = new GlobalVariable(Mod, Ty, false,
					GlobalValue::ExternalLinkage, 0, "finalObject");

			Constant* C = ConstantExpr::getBitCast(varGV,
					JavaIntrinsics.JavaObjectType);

			finalObjects.insert(std::make_pair(obj, C));
			reverseFinalObjects.insert(std::make_pair(C, obj));

			varGV->setInitializer(CreateConstantFromJavaObject(obj));
			return C;
		} else {
			Constant* CI = HandleMagic(obj, objCl);
			finalObjects.insert(std::make_pair(obj, CI));
			return CI;
		}
	} else {
		return I->second;
	}
}
*/

Constant* JavaAOTCompiler::CreateConstantFromStaticInstance(Class* cl) {
	LLVMClassInfo* LCI = getClassInfo(cl);
	Type* Ty = LCI->getStaticType();
	StructType* STy = dyn_cast<StructType>(Ty->getContainedType(0));

	std::vector<Constant*> Elts;

	for (uint32 i = 0; i < cl->nbStaticFields; ++i) {
		JavaField& field = cl->staticFields[i];
		const Typedef* type = field.getSignature();
		LLVMAssessorInfo& LAI = getTypedefInfo(type);
		Type* Ty = LAI.llvmType;

		Attribut* attribut = field.lookupAttribut(Attribut::constantAttribut);

		if (attribut == NULL) {
			std::cerr << "NULL attribute!";
			abort();
//			if ((cl->getStaticInstance() != NULL) && !useCooperativeGC()) {
//				if (type->isPrimitive()) {
//					const PrimitiveTypedef* prim =
//							(const PrimitiveTypedef*) type;
//					if (prim->isBool() || prim->isByte()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt8Ty(getLLVMContext()),
//								field.getStaticInt8Field());
//						Elts.push_back(CI);
//					} else if (prim->isShort() || prim->isChar()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt16Ty(getLLVMContext()),
//								field.getStaticInt16Field());
//						Elts.push_back(CI);
//					} else if (prim->isInt()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt32Ty(getLLVMContext()),
//								field.getStaticInt32Field());
//						Elts.push_back(CI);
//					} else if (prim->isLong()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt64Ty(getLLVMContext()),
//								field.getStaticLongField());
//						Elts.push_back(CI);
//					} else if (prim->isFloat()) {
//						Constant* CF = ConstantFP::get(
//								Type::getFloatTy(getLLVMContext()),
//								field.getStaticFloatField());
//						Elts.push_back(CF);
//					} else if (prim->isDouble()) {
//						Constant* CF = ConstantFP::get(
//								Type::getDoubleTy(getLLVMContext()),
//								field.getStaticDoubleField());
//						Elts.push_back(CF);
//					} else {
//						abort();
//					}
//				} else {
//					JavaObject* val = field.getStaticObjectField();
//					if (val) {
//						JnjvmClassLoader* JCL = cl->classLoader;
//						CommonClass* FieldCl = field.getSignature()->assocClass(
//								JCL);
//						Constant* CO = getFinalObject(val, FieldCl);
//						Elts.push_back(CO);
//					} else {
//						Elts.push_back(Constant::getNullValue(Ty));
//					}
//				}
//			} else {
//				Elts.push_back(Constant::getNullValue(Ty));
//			}
		} else {
			Reader reader(attribut, cl->bytes);
			JavaConstantPool * ctpInfo = cl->ctpInfo;
			uint16 idx = reader.readU2();
			if (type->isPrimitive()) {
				if (Ty == Type::getInt64Ty(getLLVMContext())) {
					Elts.push_back(
							ConstantInt::get(Ty,
									(uint64) ctpInfo->LongAt(idx)));
				} else if (Ty == Type::getDoubleTy(getLLVMContext())) {
					Elts.push_back(ConstantFP::get(Ty, ctpInfo->DoubleAt(idx)));
				} else if (Ty == Type::getFloatTy(getLLVMContext())) {
					Elts.push_back(ConstantFP::get(Ty, ctpInfo->FloatAt(idx)));
				} else {
					Elts.push_back(
							ConstantInt::get(Ty,
									(uint64) ctpInfo->IntegerAt(idx)));
				}
			} else if (type->isReference()) {
				const UTF8* utf8 = ctpInfo->UTF8At(ctpInfo->ctpDef[idx]);
				JavaString* obj = ctpInfo->resolveString(utf8, idx);
				Constant* C = getString(obj);
				C = ConstantExpr::getBitCast(C, JavaIntrinsics.JavaObjectType);
				Elts.push_back(C);
			} else {
				fprintf(stderr, "Implement me");
				abort();
			}
		}
	}

	return ConstantStruct::get(STy, Elts);
}

Constant* JavaAOTCompiler::getStaticInstance(Class* classDef) {
	static_instance_iterator End = staticInstances.end();
	static_instance_iterator I = staticInstances.find(classDef);
	if (I == End) {

		LLVMClassInfo* LCI = getClassInfo(classDef);
		Type* Ty = LCI->getStaticType();
		Ty = Ty->getContainedType(0);
		std::string name(UTF8Buffer(classDef->name).toCompileName()->cString());
		name += "_static";
		Module& Mod = *getLLVMModule();
		GlobalVariable* varGV = new GlobalVariable(Mod, Ty, false,
				GlobalValue::ExternalLinkage, 0, name);

		Constant* res = ConstantExpr::getCast(Instruction::BitCast, varGV,
				JavaIntrinsics.ptrType);
		staticInstances.insert(std::make_pair(classDef, res));

		if (isCompiling(classDef)) {
			Constant* C = CreateConstantFromStaticInstance(classDef);
			varGV->setInitializer(C);
		}

		return res;
	} else {
		return I->second;
	}
}

Constant* JavaAOTCompiler::getVirtualTable(JavaVirtualTable* VT) {
	CommonClass* classDef = VT->cl;
	uint32 size = 0;
	if (classDef->isClass()) {
		LLVMClassInfo* LCI = getClassInfo(classDef->asClass());
		LCI->getVirtualType();
		size = classDef->asClass()->virtualTableSize;
	} else {
		size = JavaVirtualTable::getBaseSize();
	}
	llvm::Constant* res = 0;
	virtual_table_iterator End = virtualTables.end();
	virtual_table_iterator I = virtualTables.find(VT);
	if (I == End) {

		ArrayType* ATy = dyn_cast<ArrayType>(
				JavaIntrinsics.VTType->getContainedType(0));
		PointerType* PTy = dyn_cast<PointerType>(ATy->getContainedType(0));
		ATy = ArrayType::get(PTy, size);
		std::string name(UTF8Buffer(classDef->name).toCompileName()->cString());
		name += "_VT";
		// Do not set a virtual table as a constant, because the runtime may
		// modify it.
		Module& Mod = *getLLVMModule();
		GlobalVariable* varGV = new GlobalVariable(Mod, ATy, false,
				GlobalValue::ExternalLinkage, 0, name);

		res = ConstantExpr::getCast(Instruction::BitCast, varGV,
				JavaIntrinsics.VTType);
		virtualTables.insert(std::make_pair(VT, res));

		if ((precompile && classDef->isPrimitive())
				|| (!precompile && (isCompiling(classDef) || assumeCompiled))) {
			varGV->setInitializer(CreateConstantFromVT(VT));
		}

		return res;
	} else {
		return I->second;
	}
}

Constant* JavaAOTCompiler::CreateConstantForBaseObject(CommonClass* cl) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.JavaObjectType->getContainedType(0));

	std::vector<Constant*> Elmts;

	// VT
	Elmts.push_back(getVirtualTable(cl->virtualVT));

	// lock
	Constant* L = ConstantInt::get(Type::getInt64Ty(getLLVMContext()), 0);
	Elmts.push_back(ConstantExpr::getIntToPtr(L, JavaIntrinsics.ptrType));

	return ConstantStruct::get(STy, Elmts);
}

//Constant* JavaAOTCompiler::CreateConstantFromJavaObject(JavaObject* obj) {
//	assert(!useCooperativeGC());
//	CommonClass* cl = JavaObject::getClass(obj);
//
//	if (cl->isArray()) {
//		Classpath* upcalls = cl->classLoader->bootstrapLoader->upcalls;
//		CommonClass* subClass = cl->asArrayClass()->baseClass();
//		if (subClass->isPrimitive()) {
//			if (subClass == upcalls->OfBool) {
//				return CreateConstantFromIntArray<ArrayUInt8>((ArrayUInt8*) obj,
//						Type::getInt8Ty(getLLVMContext()));
//			} else if (subClass == upcalls->OfByte) {
//				return CreateConstantFromIntArray<ArraySInt8>((ArraySInt8*) obj,
//						Type::getInt8Ty(getLLVMContext()));
//			} else if (subClass == upcalls->OfShort) {
//				return CreateConstantFromIntArray<ArraySInt16>(
//						(ArraySInt16*) obj, Type::getInt16Ty(getLLVMContext()));
//			} else if (subClass == upcalls->OfChar) {
//				return CreateConstantFromIntArray<ArrayUInt16>(
//						(ArrayUInt16*) obj, Type::getInt16Ty(getLLVMContext()));
//			} else if (subClass == upcalls->OfInt) {
//				return CreateConstantFromIntArray<ArraySInt32>(
//						(ArraySInt32*) obj, Type::getInt32Ty(getLLVMContext()));
//			} else if (subClass == upcalls->OfFloat) {
//				return CreateConstantFromFPArray<ArrayFloat>((ArrayFloat*) obj,
//						Type::getFloatTy(getLLVMContext()));
//			} else if (subClass == upcalls->OfLong) {
//				return CreateConstantFromIntArray<ArrayLong>((ArrayLong*) obj,
//						Type::getInt64Ty(getLLVMContext()));
//			} else if (subClass == upcalls->OfDouble) {
//				return CreateConstantFromFPArray<ArrayDouble>(
//						(ArrayDouble*) obj, Type::getDoubleTy(getLLVMContext()));
//			} else {
//				abort();
//			}
//		} else {
//			return CreateConstantFromObjectArray((ArrayObject*) obj);
//		}
//	} else {
//
//		std::vector<Constant*> Elmts;
//
//		// JavaObject
//		Constant* CurConstant = CreateConstantForBaseObject(
//				JavaObject::getClass(obj));
//
//		for (uint32 j = 1; j <= cl->virtualVT->depth; ++j) {
//			std::vector<Constant*> TempElts;
//			Elmts.push_back(CurConstant);
//			TempElts.push_back(CurConstant);
//			Class* curCl = cl->virtualVT->display[j]->cl->asClass();
//			LLVMClassInfo* LCI = getClassInfo(curCl);
//			StructType* STy = dyn_cast<StructType>(
//					LCI->getVirtualType()->getContainedType(0));
//
//			for (uint32 i = 0; i < curCl->nbVirtualFields; ++i) {
//				JavaField& field = curCl->virtualFields[i];
//				const Typedef* type = field.getSignature();
//				if (type->isPrimitive()) {
//					const PrimitiveTypedef* prim =
//							(const PrimitiveTypedef*) type;
//					if (prim->isBool() || prim->isByte()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt8Ty(getLLVMContext()),
//								field.getInstanceInt8Field(obj));
//						TempElts.push_back(CI);
//					} else if (prim->isShort() || prim->isChar()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt16Ty(getLLVMContext()),
//								field.getInstanceInt16Field(obj));
//						TempElts.push_back(CI);
//					} else if (prim->isInt()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt32Ty(getLLVMContext()),
//								field.getInstanceInt32Field(obj));
//						TempElts.push_back(CI);
//					} else if (prim->isLong()) {
//						ConstantInt* CI = ConstantInt::get(
//								Type::getInt64Ty(getLLVMContext()),
//								field.getInstanceLongField(obj));
//						TempElts.push_back(CI);
//					} else if (prim->isFloat()) {
//						Constant* CF = ConstantFP::get(
//								Type::getFloatTy(getLLVMContext()),
//								field.getInstanceFloatField(obj));
//						TempElts.push_back(CF);
//					} else if (prim->isDouble()) {
//						Constant* CF = ConstantFP::get(
//								Type::getDoubleTy(getLLVMContext()),
//								field.getInstanceDoubleField(obj));
//						TempElts.push_back(CF);
//					} else {
//						abort();
//					}
//				} else {
//					JavaObject* val = field.getInstanceObjectField(obj);
//					if (val) {
//						JnjvmClassLoader* JCL = cl->classLoader;
//						CommonClass* FieldCl = field.getSignature()->assocClass(
//								JCL);
//						Constant* C = getFinalObject(val, FieldCl);
//						TempElts.push_back(C);
//					} else {
//						llvm::Type* Ty = JavaIntrinsics.JavaObjectType;
//						TempElts.push_back(Constant::getNullValue(Ty));
//					}
//				}
//			}
//			CurConstant = ConstantStruct::get(STy, TempElts);
//		}
//
//		return CurConstant;
//	}
//}

Constant* JavaAOTCompiler::CreateConstantFromJavaString(JavaString* str) {
	Class* cl = 0; // JavaObject::getClass(str)->asClass();
	LLVMClassInfo* LCI = getClassInfo(cl);
	StructType* STy = dyn_cast<StructType>(
			LCI->getVirtualType()->getContainedType(0));

	std::vector<Constant*> Elmts;

	Elmts.push_back(CreateConstantForBaseObject(cl));

	Constant* Array = CreateConstantFromIntArray<ArrayUInt16>(
			JavaString::getValue(str), Type::getInt16Ty(getLLVMContext()));

	Module& Mod = *getLLVMModule();
	GlobalVariable* varGV = new GlobalVariable(Mod, Array->getType(), false,
			GlobalValue::InternalLinkage, Array, "");

	Array = ConstantExpr::getBitCast(varGV, JavaIntrinsics.JavaObjectType);

	Elmts.push_back(Array);
	Elmts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()), str->count));
	Elmts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					str->cachedHashCode));
	Elmts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()), str->offset));

	return ConstantStruct::get(STy, Elmts);
}

Constant* JavaAOTCompiler::CreateConstantFromAttribut(Attribut& attribut) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.AttributType->getContainedType(0));

	std::vector<Constant*> Elmts;

	// name
	Elmts.push_back(getUTF8(attribut.name));

	// start
	Elmts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					attribut.start));

	// nbb
	Elmts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()), attribut.nbb));

	return ConstantStruct::get(STy, Elmts);
}

Constant* JavaAOTCompiler::CreateConstantFromCommonClass(CommonClass* cl) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.JavaClassType->getContainedType(0));
	Module& Mod = *getLLVMModule();

	llvm::Type* TempTy = NULL;

	std::vector<Constant*> CommonClassElts;
	std::vector<Constant*> TempElmts;

	// delegatee
	ArrayType* ATy = dyn_cast<ArrayType>(STy->getContainedType(0));
	assert(ATy && "Malformed type");

	Constant* TCM[1] = { getJavaClass(cl) };
	CommonClassElts.push_back(ConstantArray::get(ATy, TCM));

	// access
	CommonClassElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()), cl->access));

	// interfaces
	if (cl->nbInterfaces) {
		for (uint32 i = 0; i < cl->nbInterfaces; ++i) {
			TempElmts.push_back(getNativeClass(cl->interfaces[i]));
		}

		ATy = ArrayType::get(JavaIntrinsics.JavaClassType, cl->nbInterfaces);
		Constant* interfaces = ConstantArray::get(ATy, TempElmts);
		interfaces = new GlobalVariable(Mod, ATy, true,
				GlobalValue::InternalLinkage, interfaces, "");
		interfaces = ConstantExpr::getCast(Instruction::BitCast, interfaces,
				PointerType::getUnqual(JavaIntrinsics.JavaClassType));

		CommonClassElts.push_back(interfaces);
	} else {
		Type* Ty = PointerType::getUnqual(JavaIntrinsics.JavaClassType);
		CommonClassElts.push_back(Constant::getNullValue(Ty));
	}

	// nbInterfaces
	CommonClassElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					cl->nbInterfaces));

	// name
	CommonClassElts.push_back(getUTF8(cl->name));

	// super
	if (cl->super) {
		CommonClassElts.push_back(getNativeClass(cl->super));
	} else {
		TempTy = JavaIntrinsics.JavaClassType;
		CommonClassElts.push_back(Constant::getNullValue(TempTy));
	}

	CommonClassElts.push_back(Constant::getNullValue(JavaIntrinsics.ptrType));

	// virtualTable
	if (cl->virtualVT) {
		CommonClassElts.push_back(getVirtualTable(cl->virtualVT));
	} else {
		TempTy = JavaIntrinsics.VTType;
		CommonClassElts.push_back(Constant::getNullValue(TempTy));
	}
	return ConstantStruct::get(STy, CommonClassElts);
}

Constant* JavaAOTCompiler::CreateConstantFromJavaField(JavaField& field) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.JavaFieldType->getContainedType(0));

	std::vector<Constant*> FieldElts;
	std::vector<Constant*> TempElts;

	// signature
	FieldElts.push_back(Constant::getNullValue(JavaIntrinsics.ptrType));

	// access
	FieldElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()), field.access));

	// name
	FieldElts.push_back(getUTF8(field.name));

	// type
	FieldElts.push_back(getUTF8(field.type));

	// attributs
	if (field.nbAttributs) {
		llvm::Type* AttrTy = JavaIntrinsics.AttributType->getContainedType(0);
		ArrayType* ATy = ArrayType::get(AttrTy, field.nbAttributs);
		for (uint32 i = 0; i < field.nbAttributs; ++i) {
			TempElts.push_back(CreateConstantFromAttribut(field.attributs[i]));
		}

		Constant* attributs = ConstantArray::get(ATy, TempElts);
		TempElts.clear();
		attributs = new GlobalVariable(*getLLVMModule(), ATy, true,
				GlobalValue::InternalLinkage, attributs, "");
		attributs = ConstantExpr::getCast(Instruction::BitCast, attributs,
				JavaIntrinsics.AttributType);

		FieldElts.push_back(attributs);
	} else {
		FieldElts.push_back(
				Constant::getNullValue(JavaIntrinsics.AttributType));
	}

	// nbAttributs
	FieldElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					field.nbAttributs));

	// classDef
	FieldElts.push_back(getNativeClass(field.classDef));

	// ptrOffset
	FieldElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					field.ptrOffset));

	// num
	FieldElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()), field.num));

	return ConstantStruct::get(STy, FieldElts);
}

Constant* JavaAOTCompiler::CreateConstantFromJavaMethod(JavaMethod& method) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.JavaMethodType->getContainedType(0));
	Module& Mod = *getLLVMModule();

	std::vector<Constant*> MethodElts;
	std::vector<Constant*> TempElts;

	// signature
	MethodElts.push_back(Constant::getNullValue(JavaIntrinsics.ptrType));

	// access
	MethodElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					method.access));

	// attributs
	if (method.nbAttributs) {
		llvm::Type* AttrTy = JavaIntrinsics.AttributType->getContainedType(0);
		ArrayType* ATy = ArrayType::get(AttrTy, method.nbAttributs);
		for (uint32 i = 0; i < method.nbAttributs; ++i) {
			TempElts.push_back(CreateConstantFromAttribut(method.attributs[i]));
		}

		Constant* attributs = ConstantArray::get(ATy, TempElts);
		TempElts.clear();
		attributs = new GlobalVariable(Mod, ATy, true,
				GlobalValue::InternalLinkage, attributs, "");
		attributs = ConstantExpr::getCast(Instruction::BitCast, attributs,
				JavaIntrinsics.AttributType);

		MethodElts.push_back(attributs);
	} else {
		MethodElts.push_back(
				Constant::getNullValue(JavaIntrinsics.AttributType));
	}

	// nbAttributs
	MethodElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					method.nbAttributs));

	// classDef
	MethodElts.push_back(getNativeClass(method.classDef));

	// name
	MethodElts.push_back(getUTF8(method.name));

	// type
	MethodElts.push_back(getUTF8(method.type));

	// canBeInlined
	MethodElts.push_back(
			ConstantInt::get(Type::getInt8Ty(getLLVMContext()),
					method.isCustomizable));

	// code
	if (getMethodInfo(&method)->methodFunction == NULL) {
		MethodElts.push_back(Constant::getNullValue(JavaIntrinsics.ptrType));
	} else {
		assert(!isAbstract(method.access));
		Function* func = getMethod(&method, NULL);
		MethodElts.push_back(
				ConstantExpr::getCast(Instruction::BitCast, func,
						JavaIntrinsics.ptrType));
	}

	// offset
	MethodElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					method.offset));

	return ConstantStruct::get(STy, MethodElts);
}

Constant* JavaAOTCompiler::CreateConstantFromClassPrimitive(
		ClassPrimitive* cl) {
	llvm::Type* JCPTy = JavaIntrinsics.JavaClassPrimitiveType->getContainedType(
			0);
	StructType* STy = dyn_cast<StructType>(JCPTy);

	std::vector<Constant*> ClassElts;

	// common class
	ClassElts.push_back(CreateConstantFromCommonClass(cl));

	// primSize
	ClassElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()), cl->logSize));

	return ConstantStruct::get(STy, ClassElts);
}

Constant* JavaAOTCompiler::CreateConstantFromClassArray(ClassArray* cl) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.JavaClassArrayType->getContainedType(0));

	std::vector<Constant*> ClassElts;
	Constant* ClGEPs[2] = { getIntrinsics()->constantZero,
			getIntrinsics()->constantZero };

	// common class
	ClassElts.push_back(CreateConstantFromCommonClass(cl));

	// baseClass
	Constant* Cl = getNativeClass(cl->baseClass());
	if (Cl->getType() != JavaIntrinsics.JavaClassType)
		Cl = ConstantExpr::getGetElementPtr(Cl, ClGEPs, 2);

	ClassElts.push_back(Cl);

	return ConstantStruct::get(STy, ClassElts);
}

Constant* JavaAOTCompiler::CreateConstantFromClassMap(
		const MvmDenseMap<const UTF8*, CommonClass*>& map) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.J3DenseMapType->getContainedType(0));
	Module& Mod = *getLLVMModule();

	std::vector<Constant*> elements;
	elements.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					map.NumBuckets));

	Constant* buckets;
	if (map.NumBuckets > 0) {
		std::vector<Constant*> TempElts;
		ArrayType* ATy = ArrayType::get(JavaIntrinsics.ptrType,
				map.NumBuckets * 2);

		for (uint32 i = 0; i < map.NumBuckets; ++i) {
			MvmPair<const UTF8*, CommonClass*> pair = map.Buckets[i];
			if (pair.first == &TombstoneKey) {
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast,
								UTF8TombstoneGV, JavaIntrinsics.ptrType));
				TempElts.push_back(
						Constant::getNullValue(JavaIntrinsics.ptrType));
			} else if (pair.first == &EmptyKey) {
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast, UTF8EmptyGV,
								JavaIntrinsics.ptrType));
				TempElts.push_back(
						Constant::getNullValue(JavaIntrinsics.ptrType));
			} else {
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast,
								getUTF8(pair.first), JavaIntrinsics.ptrType));
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast,
								getNativeClass(pair.second),
								JavaIntrinsics.ptrType));
			}
		}

		buckets = ConstantArray::get(ATy, TempElts);

		GlobalVariable* gv = new GlobalVariable(Mod, ATy, false,
				GlobalValue::InternalLinkage, buckets, "");
		buckets = ConstantExpr::getCast(Instruction::BitCast, gv,
				JavaIntrinsics.ptrType);
	} else {
		buckets = Constant::getNullValue(JavaIntrinsics.ptrType);
	}

	elements.push_back(buckets);
	elements.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					map.NumEntries));
	elements.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					map.NumTombstones));
	elements.push_back(ConstantInt::get(Type::getInt1Ty(getLLVMContext()), 1));

	return new GlobalVariable(Mod, STy, false, GlobalValue::ExternalLinkage,
			ConstantStruct::get(STy, elements), "ClassMap");
}

Constant* JavaAOTCompiler::CreateConstantFromUTF8Map(
		const MvmDenseSet<UTF8MapKey, const UTF8*>& set) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.J3DenseMapType->getContainedType(0));
	Module& Mod = *getLLVMModule();

	std::vector<Constant*> elements;
	elements.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					set.NumBuckets));

	Constant* buckets;
	if (set.NumBuckets > 0) {
		std::vector<Constant*> TempElts;
		ArrayType* ATy = ArrayType::get(JavaIntrinsics.ptrType, set.NumBuckets);

		for (uint32 i = 0; i < set.NumBuckets; ++i) {
			const UTF8* utf8 = set.Buckets[i];
			if (utf8 == &EmptyKey) {
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast, UTF8EmptyGV,
								JavaIntrinsics.ptrType));
			} else if (utf8 == &TombstoneKey) {
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast,
								UTF8TombstoneGV, JavaIntrinsics.ptrType));
			} else {
				TempElts.push_back(
						ConstantExpr::getCast(Instruction::BitCast,
								getUTF8(utf8), JavaIntrinsics.ptrType));
			}
		}

		buckets = ConstantArray::get(ATy, TempElts);

		GlobalVariable* gv = new GlobalVariable(Mod, ATy, false,
				GlobalValue::InternalLinkage, buckets, "");
		buckets = ConstantExpr::getCast(Instruction::BitCast, gv,
				JavaIntrinsics.ptrType);
	} else {
		buckets = Constant::getNullValue(JavaIntrinsics.ptrType);
	}

	elements.push_back(buckets);
	elements.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					set.NumEntries));
	elements.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					set.NumTombstones));
	elements.push_back(ConstantInt::get(Type::getInt1Ty(getLLVMContext()), 1));

	return new GlobalVariable(Mod, STy, false, GlobalValue::ExternalLinkage,
			ConstantStruct::get(STy, elements), "UTF8Map");
}

Constant* JavaAOTCompiler::CreateConstantFromClass(Class* cl) {
	StructType* STy = dyn_cast<StructType>(
			JavaIntrinsics.JavaClassType->getContainedType(0));
	Module& Mod = *getLLVMModule();

	std::vector<Constant*> ClassElts;
	std::vector<Constant*> TempElts;

	// common class
	ClassElts.push_back(CreateConstantFromCommonClass(cl));

	// virtualSize
	ClassElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					cl->virtualSize));

	// alginment
	ClassElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					cl->alignment));

	// IsolateInfo
	ArrayType* ATy = dyn_cast<ArrayType>(STy->getContainedType(3));
	assert(ATy && "Malformed type");

	StructType* TCMTy = dyn_cast<StructType>(ATy->getContainedType(0));
	assert(TCMTy && "Malformed type");

//	TempElts.push_back(
//			ConstantInt::get(Type::getInt8Ty(getLLVMContext()),
//					cl->getInitializationState()));
	TempElts.push_back(
			ConstantInt::get(Type::getInt1Ty(getLLVMContext()),
					cl->isReady() ? 1 : 0));
	TempElts.push_back(getStaticInstance(cl));
	Constant* CStr[1] = { ConstantStruct::get(TCMTy, TempElts) };
	TempElts.clear();
	ClassElts.push_back(ConstantArray::get(ATy, CStr));

	if (cl->nbVirtualFields + cl->nbStaticFields) {
		ATy = ArrayType::get(JavaIntrinsics.JavaFieldType->getContainedType(0),
				cl->nbVirtualFields + cl->nbStaticFields);
	}

	// virtualFields
	if (cl->nbVirtualFields) {
		for (uint32 i = 0; i < cl->nbVirtualFields; ++i) {
			TempElts.push_back(
					CreateConstantFromJavaField(cl->virtualFields[i]));
		}
	}

	// staticFields
	if (cl->nbStaticFields) {
		for (uint32 i = 0; i < cl->nbStaticFields; ++i) {
			TempElts.push_back(
					CreateConstantFromJavaField(cl->staticFields[i]));
		}
	}

	Constant* fields = 0;
	if (cl->nbStaticFields + cl->nbVirtualFields) {
		fields = ConstantArray::get(ATy, TempElts);
		TempElts.clear();
		fields = new GlobalVariable(Mod, ATy, false,
				GlobalValue::InternalLinkage, fields, "");
		fields = ConstantExpr::getCast(Instruction::BitCast, fields,
				JavaIntrinsics.JavaFieldType);
	} else {
		fields = Constant::getNullValue(JavaIntrinsics.JavaFieldType);
	}

	// virtualFields
	ClassElts.push_back(fields);

	ConstantInt* nbVirtualFields = ConstantInt::get(
			Type::getInt16Ty(getLLVMContext()), cl->nbVirtualFields);
	// nbVirtualFields
	ClassElts.push_back(nbVirtualFields);

	// staticFields
	ClassElts.push_back(
			ConstantExpr::getGetElementPtr(fields, nbVirtualFields));

	// nbStaticFields
	ClassElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					cl->nbStaticFields));

	// virtualMethods
	if (cl->nbVirtualMethods + cl->nbStaticMethods) {
		ATy = ArrayType::get(JavaIntrinsics.JavaMethodType->getContainedType(0),
				cl->nbVirtualMethods + cl->nbStaticMethods);
	}

	if (cl->nbVirtualMethods) {
		for (uint32 i = 0; i < cl->nbVirtualMethods; ++i) {
			TempElts.push_back(
					CreateConstantFromJavaMethod(cl->virtualMethods[i]));
		}
	}

	if (cl->nbStaticMethods) {
		for (uint32 i = 0; i < cl->nbStaticMethods; ++i) {
			TempElts.push_back(
					CreateConstantFromJavaMethod(cl->staticMethods[i]));
		}
	}

	Constant* methods = 0;
	if (cl->nbVirtualMethods + cl->nbStaticMethods) {
		methods = ConstantArray::get(ATy, TempElts);
		TempElts.clear();

		GlobalVariable* GV = NULL;
		method_iterator SI = virtualMethods.find(cl);
		if (SI != virtualMethods.end()) {
			GV = dyn_cast<GlobalVariable>(SI->second);
			GV->setInitializer(methods);
		} else {
			std::string name(UTF8Buffer(cl->name).toCompileName()->cString());
			name += "_VirtualMethods";
			GV = new GlobalVariable(Mod, ATy, false,
					GlobalValue::ExternalLinkage, methods, name);
			virtualMethods.insert(std::make_pair(cl, GV));
		}
		methods = ConstantExpr::getCast(Instruction::BitCast, GV,
				JavaIntrinsics.JavaMethodType);
	} else {
		methods = Constant::getNullValue(JavaIntrinsics.JavaMethodType);
	}

	// virtualMethods
	ClassElts.push_back(methods);

	ConstantInt* nbVirtualMethods = ConstantInt::get(
			Type::getInt16Ty(getLLVMContext()), cl->nbVirtualMethods);
	// nbVirtualMethods
	ClassElts.push_back(nbVirtualMethods);

	// staticMethods
	ClassElts.push_back(
			ConstantExpr::getGetElementPtr(methods, nbVirtualMethods));

	// nbStaticMethods
	ClassElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					cl->nbStaticMethods));

	// ownerClass
	ClassElts.push_back(Constant::getNullValue(JavaIntrinsics.ptrType));

	// ctpInfo
//	Constant* ctpInfo = CreateConstantFromJavaConstantPool(cl->ctpInfo);
//	Constant* varGV = new GlobalVariable(*getLLVMModule(), ctpInfo->getType(),
//			false, GlobalValue::InternalLinkage, ctpInfo, "");
//	ClassElts.push_back(varGV);

	// attributs
	if (cl->nbAttributs) {
		ATy = ArrayType::get(JavaIntrinsics.AttributType->getContainedType(0),
				cl->nbAttributs);

		for (uint32 i = 0; i < cl->nbAttributs; ++i) {
			TempElts.push_back(CreateConstantFromAttribut(cl->attributs[i]));
		}

		Constant* attributs = ConstantArray::get(ATy, TempElts);
		TempElts.clear();
		attributs = new GlobalVariable(*getLLVMModule(), ATy, true,
				GlobalValue::InternalLinkage, attributs, "");
		attributs = ConstantExpr::getCast(Instruction::BitCast, attributs,
				JavaIntrinsics.AttributType);
		ClassElts.push_back(attributs);
	} else {
		ClassElts.push_back(
				Constant::getNullValue(JavaIntrinsics.AttributType));
	}

	// nbAttributs
	ClassElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					cl->nbAttributs));

	// innerClasses
	if (cl->nbInnerClasses) {
		for (uint32 i = 0; i < cl->nbInnerClasses; ++i) {
			TempElts.push_back(getNativeClass(cl->innerClasses[i]));
		}

		llvm::Type* TempTy = JavaIntrinsics.JavaClassType;
		ATy = ArrayType::get(TempTy, cl->nbInnerClasses);
		Constant* innerClasses = ConstantArray::get(ATy, TempElts);
		innerClasses = new GlobalVariable(*getLLVMModule(), ATy, true,
				GlobalValue::InternalLinkage, innerClasses, "");
		innerClasses = ConstantExpr::getCast(Instruction::BitCast, innerClasses,
				PointerType::getUnqual(TempTy));

		ClassElts.push_back(innerClasses);
	} else {
		Type* Ty = PointerType::getUnqual(JavaIntrinsics.JavaClassType);
		ClassElts.push_back(Constant::getNullValue(Ty));
	}

	// nbInnerClasses
	ClassElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					cl->nbInnerClasses));

	// outerClass
	if (cl->outerClass) {
		ClassElts.push_back(getNativeClass(cl->outerClass));
	} else {
		ClassElts.push_back(
				Constant::getNullValue(JavaIntrinsics.JavaClassType));
	}

	// innerAccess
	ClassElts.push_back(
			ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
					cl->innerAccess));

	// innerOuterResolved
	ClassElts.push_back(
			ConstantInt::get(Type::getInt8Ty(getLLVMContext()),
					cl->innerOuterResolved));

	// isAnonymous
	ClassElts.push_back(
			ConstantInt::get(Type::getInt8Ty(getLLVMContext()),
					cl->isAnonymous));

	// virtualTableSize
	ClassElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					cl->virtualTableSize));

	// staticSize
	ClassElts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()),
					cl->staticSize));

	return ConstantStruct::get(STy, ClassElts);
}

Constant* JavaAOTCompiler::CreateConstantFromClassBytes(ClassBytes* bytes) {
	std::vector<Type*> Elemts;
	ArrayType* ATy = ArrayType::get(Type::getInt8Ty(getLLVMContext()),
			bytes->size);
	Elemts.push_back(Type::getInt32Ty(getLLVMContext()));
	Elemts.push_back(ATy);

	StructType* STy = StructType::get(getLLVMContext(), Elemts);

	std::vector<Constant*> Cts;
	Cts.push_back(
			ConstantInt::get(Type::getInt32Ty(getLLVMContext()), bytes->size));

	std::vector<Constant*> Vals;
	for (uint32 i = 0; i < bytes->size; ++i) {
		Vals.push_back(
				ConstantInt::get(Type::getInt8Ty(getLLVMContext()),
						bytes->elements[i]));
	}

	Cts.push_back(ConstantArray::get(ATy, Vals));

	return ConstantStruct::get(STy, Cts);
}

template<typename T>
Constant* JavaAOTCompiler::CreateConstantFromIntArray(const T* val, Type* Ty) {
	std::vector<Type*> Elemts;
	ArrayType* ATy = ArrayType::get(Ty, T::getSize(val));
	Elemts.push_back(JavaIntrinsics.JavaObjectType->getContainedType(0));
	Elemts.push_back(JavaIntrinsics.pointerSizeType);

	Elemts.push_back(ATy);

	StructType* STy = StructType::get(getLLVMModule()->getContext(), Elemts);

	std::vector<Constant*> Cts;
	// fixme
	// Cts.push_back(CreateConstantForBaseObject(JavaObject::getClass(val)));
	Cts.push_back(
			ConstantInt::get(JavaIntrinsics.pointerSizeType, T::getSize(val)));

	std::vector<Constant*> Vals;
	for (sint32 i = 0; i < T::getSize(val); ++i) {
		Vals.push_back(ConstantInt::get(Ty, (uint64) T::getElement(val, i)));
	}

	Cts.push_back(ConstantArray::get(ATy, Vals));

	return ConstantStruct::get(STy, Cts);
}

template<typename T>
Constant* JavaAOTCompiler::CreateConstantFromFPArray(const T* val, Type* Ty) {
		std::vector<Type*> Elemts;
	ArrayType* ATy = ArrayType::get(Ty, T::getSize(val));
	Elemts.push_back(JavaIntrinsics.JavaObjectType->getContainedType(0));
	Elemts.push_back(JavaIntrinsics.pointerSizeType);

	Elemts.push_back(ATy);

	StructType* STy = StructType::get(getLLVMModule()->getContext(), Elemts);

	std::vector<Constant*> Cts;
	// fixme
	// Cts.push_back(CreateConstantForBaseObject(JavaObject::getClass(val)));
	Cts.push_back(
			ConstantInt::get(JavaIntrinsics.pointerSizeType, T::getSize(val)));

	std::vector<Constant*> Vals;
	for (sint32 i = 0; i < T::getSize(val); ++i) {
		Vals.push_back(ConstantFP::get(Ty, (double) T::getElement(val, i)));
	}

	Cts.push_back(ConstantArray::get(ATy, Vals));

	return ConstantStruct::get(STy, Cts);
}

Constant* JavaAOTCompiler::CreateConstantFromObjectArray(
		const ArrayObject* val) {
	std::vector<Type*> Elemts;
	llvm::Type* Ty = JavaIntrinsics.JavaObjectType;
	ArrayType* ATy = ArrayType::get(Ty, ArrayObject::getSize(val));
	Elemts.push_back(JavaIntrinsics.JavaObjectType->getContainedType(0));
	Elemts.push_back(JavaIntrinsics.pointerSizeType);

	Elemts.push_back(ATy);

	StructType* STy = StructType::get(getLLVMModule()->getContext(), Elemts);

	std::vector<Constant*> Cts;
	// fixme
	//Cts.push_back(CreateConstantForBaseObject(JavaObject::getClass(val)));
	Cts.push_back(
			ConstantInt::get(JavaIntrinsics.pointerSizeType,
					ArrayObject::getSize(val)));

	std::vector<Constant*> Vals;
	for (sint32 i = 0; i < ArrayObject::getSize(val); ++i) {
		if (ArrayObject::getElement(val, i)) {
			// fixme
			//			Vals.push_back(
//					getFinalObject(ArrayObject::getElement(val, i),
//							JavaObject::getClass(val)->asArrayClass()->baseClass()));
		} else {
			Vals.push_back(
					Constant::getNullValue(JavaIntrinsics.JavaObjectType));
		}
	}

	Cts.push_back(ConstantArray::get(ATy, Vals));

	return ConstantStruct::get(STy, Cts);
}

Constant* JavaAOTCompiler::CreateConstantFromUTF8(const UTF8* val) {
	std::vector<Type*> Elemts;
	ArrayType* ATy = ArrayType::get(Type::getInt16Ty(getLLVMContext()),
			val->size);
	Elemts.push_back(JavaIntrinsics.pointerSizeType);

	Elemts.push_back(ATy);

	StructType* STy = StructType::get(getLLVMModule()->getContext(), Elemts);

	std::vector<Constant*> Cts;
	Cts.push_back(ConstantInt::get(JavaIntrinsics.pointerSizeType, val->size));

	std::vector<Constant*> Vals;
	for (sint32 i = 0; i < val->size; ++i) {
		Vals.push_back(
				ConstantInt::get(Type::getInt16Ty(getLLVMContext()),
						val->elements[i]));
	}

	Cts.push_back(ConstantArray::get(ATy, Vals));

	return ConstantStruct::get(STy, Cts);

}

Constant* JavaAOTCompiler::getUTF8(const UTF8* val) {
	utf8_iterator End = utf8s.end();
	utf8_iterator I = utf8s.find(val);
	if (I == End) {
		Constant* C = CreateConstantFromUTF8(val);
		Module& Mod = *getLLVMModule();
		GlobalVariable* varGV = new GlobalVariable(Mod, C->getType(), true,
				GlobalValue::InternalLinkage, C, "");

		Constant* res = ConstantExpr::getCast(Instruction::BitCast, varGV,
				JavaIntrinsics.UTF8Type);
		utf8s.insert(std::make_pair(val, res));

		return res;
	} else {
		return I->second;
	}
}

Function* JavaAOTCompiler::getMethodOrStub(JavaMethod* meth,
		Class* customizeFor) {
	assert(!isStatic(meth->access));
	LLVMMethodInfo* LMI = getMethodInfo(meth);
	LLVMSignatureInfo* LSI = getSignatureInfo(meth->getSignature());
	if (precompile) {
		if (customizeFor != NULL) {
			if (LMI->isCustomizable) {
				if (LMI->customizedVersions[customizeFor] != NULL) {
					// We have a customized version, get it.
					return getMethod(meth, customizeFor);
				} else {
					// No customized version, even if there is an uncustomized version,
					// we return the stub, so that we get a customized version at
					// runtime.
					return LSI->getVirtualStub();
				}
			} else {
				// If we have created a method for it, we know the method is not customizable,
				// and we can use the 'general' method.
				if (LMI->methodFunction) {
					return getMethod(meth, NULL);
				} else {
					// Otherwise, no method has been created for it.
					return LSI->getVirtualStub();
				}
			}
		} else {
			// If we have created a method for it, take it.
			if (LMI->methodFunction) {
				return getMethod(meth, NULL);
			} else {
				// Otherwise, no method has been created for it.
				return LSI->getVirtualStub();
			}
		}
	} else {
		// We're not precompiling, get the method.
		return getMethod(meth, NULL);
	}
}

Function* JavaAOTCompiler::getMethod(JavaMethod* meth, Class* customizeFor) {
	Function* func = getMethodInfo(meth)->getMethod(customizeFor);
	if (func->hasExternalWeakLinkage()) {
		toCompile.push_back(std::make_pair(meth, customizeFor));
	}
	return func;
}

Constant* JavaAOTCompiler::CreateConstantFromVT(JavaVirtualTable* VT) {
	CommonClass* classDef = VT->cl;
	uint32 size =
			classDef->isClass() ?
					classDef->asClass()->virtualTableSize :
					JavaVirtualTable::getBaseSize();
	JavaVirtualTable* RealVT =
			classDef->isClass() ? VT : ClassArray::SuperArray->virtualVT;

	Class* maybeCustomize = classDef->isClass() ? classDef->asClass() : NULL;

	ArrayType* ATy = dyn_cast<ArrayType>(
			JavaIntrinsics.VTType->getContainedType(0));
	PointerType* PTy = dyn_cast<PointerType>(ATy->getContainedType(0));
	ATy = ArrayType::get(PTy, size);

	ConstantPointerNull* N = ConstantPointerNull::get(PTy);
	std::vector<Constant*> Elemts;

	// Destructor
	Function* Finalizer = NULL;
	if (VT->hasDestructor()) {
		JavaMethod* meth = (JavaMethod*) (RealVT->destructor);
		Finalizer = getMethodOrStub(meth, maybeCustomize);
	} else {
		Finalizer = EmptyDestructorFunction;
	}

	Elemts.push_back(
			ConstantExpr::getCast(Instruction::BitCast, Finalizer, PTy));

	// Delete
	Elemts.push_back(N);

	// Tracer
	Function* Tracer = 0;
	// fixme
//	if (classDef->isArray()) {
//		if (classDef->asArrayClass()->baseClass()->isPrimitive()) {
//			Tracer = JavaObjectTracer;
//		} else {
//			Tracer = ArrayObjectTracer;
//		}
//	} else if (classDef->isClass()) {
//		if (classDef->isAssignableFrom(
//				classDef->classLoader->bootstrapLoader->upcalls->newReference)) {
//			Tracer = ReferenceObjectTracer;
//		} else {
//			Tracer = RegularObjectTracer;
//		}
//	}

//	Elemts.push_back(
//			(Tracer) ?
//					ConstantExpr::getCast(Instruction::BitCast, Tracer, PTy) :
//					N);

	// fixme
//	for (uint32_t i = 0; i < VirtualTable::numberOfSpecializedTracers(); i++) {
//		// Push null for now.
//		Elemts.push_back(N);
//	}

	// Class
	Elemts.push_back(
			ConstantExpr::getCast(Instruction::BitCast,
					getNativeClass(classDef), PTy));

	// depth
	Elemts.push_back(
			ConstantExpr::getIntToPtr(
					ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
							VT->depth), PTy));

	// offset
	Elemts.push_back(
			ConstantExpr::getIntToPtr(
					ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
							VT->offset), PTy));

	// cache
	Elemts.push_back(N);

	// display
	for (uint32 i = 0; i < JavaVirtualTable::getDisplayLength(); ++i) {
		if (VT->display[i]) {
			Constant* Temp = getVirtualTable(VT->display[i]);
			Temp = ConstantExpr::getBitCast(Temp, PTy);
			Elemts.push_back(Temp);
		} else {
			Elemts.push_back(Constant::getNullValue(PTy));
		}
	}

	// nbSecondaryTypes
	Elemts.push_back(
			ConstantExpr::getIntToPtr(
					ConstantInt::get(Type::getInt64Ty(getLLVMContext()),
							VT->nbSecondaryTypes), PTy));

	// secondaryTypes
	ArrayType* DTy = ArrayType::get(JavaIntrinsics.VTType,
			VT->nbSecondaryTypes);

	std::vector<Constant*> TempElmts;
	for (uint32 i = 0; i < VT->nbSecondaryTypes; ++i) {
		assert(VT->secondaryTypes[i] && "No secondary type");
		Constant* Cl = getVirtualTable(VT->secondaryTypes[i]);
		TempElmts.push_back(Cl);
	}
	Constant* display = ConstantArray::get(DTy, TempElmts);
	TempElmts.clear();

	display = new GlobalVariable(*getLLVMModule(), DTy, true,
			GlobalValue::InternalLinkage, display, "");

	display = ConstantExpr::getCast(Instruction::BitCast, display, PTy);

	Elemts.push_back(display);

	// baseClassVT
	if (VT->baseClassVT) {
		Constant* Temp = getVirtualTable(VT->baseClassVT);
		Temp = ConstantExpr::getBitCast(Temp, PTy);
		Elemts.push_back(Temp);
	} else {
		Elemts.push_back(Constant::getNullValue(PTy));
	}

	// IMT
	if (!VT->IMT) {
		Elemts.push_back(Constant::getNullValue(PTy));
	} else {
		// TODO: add a null element at the end to diagnose errors.
		Class* cl = classDef->asClass();
		assert(cl && "Not a class");
		std::set<JavaMethod*> contents[InterfaceMethodTable::NumIndexes];
		classDef->asClass()->fillIMT(contents);

		ArrayType* ATy = dyn_cast<ArrayType>(
				JavaIntrinsics.VTType->getContainedType(0));
		PointerType* PTy = dyn_cast<PointerType>(ATy->getContainedType(0));
		ATy = ArrayType::get(PTy, InterfaceMethodTable::NumIndexes);

		ConstantPointerNull* N = ConstantPointerNull::get(PTy);
		std::vector<Constant*> IElemts;

		for (uint32_t i = 0; i < InterfaceMethodTable::NumIndexes; ++i) {
			std::set<JavaMethod*>& atIndex = contents[i];
			uint32_t size = atIndex.size();
			if (size == 1) {
				JavaMethod* Imeth = *(atIndex.begin());
				JavaMethod* meth = cl->lookupMethodDontThrow(Imeth->name,
						Imeth->type, false, true, 0);
				assert(meth && "No method found");
				Function* func = getMethodOrStub(meth, maybeCustomize);
				IElemts.push_back(ConstantExpr::getBitCast(func, PTy));
			} else if (size > 1) {
				std::vector<JavaMethod*> methods;
				bool SameMethod = true;
				JavaMethod* OldMethod = 0;

				for (std::set<JavaMethod*>::iterator it = atIndex.begin(), et =
						atIndex.end(); it != et; ++it) {
					JavaMethod* Imeth = *it;
					JavaMethod* Cmeth = cl->lookupMethodDontThrow(Imeth->name,
							Imeth->type, false, true, 0);

					if (OldMethod && OldMethod != Cmeth)
						SameMethod = false;
					else
						OldMethod = Cmeth;

					methods.push_back(Cmeth);
				}

				if (SameMethod) {
					assert(methods[0] && "No method found");
					Function* func = getMethodOrStub(methods[0],
							maybeCustomize);
					IElemts.push_back(ConstantExpr::getBitCast(func, PTy));
				} else {

					uint32_t length = 2 * size;

					ArrayType* ATy = dyn_cast<ArrayType>(
							JavaIntrinsics.VTType->getContainedType(0));
					ATy = ArrayType::get(PTy, length);
					std::vector<Constant*> InternalElemts;

					std::set<JavaMethod*>::iterator Interf = atIndex.begin();
					for (std::vector<JavaMethod*>::iterator it =
							methods.begin(), et = methods.end(); it != et;
							++it, ++Interf) {
						JavaMethod* Imeth = *Interf;
						JavaMethod* Cmeth = *it;
						assert(Cmeth && "No method found");

						Function* func = getMethodOrStub(Cmeth, maybeCustomize);
						InternalElemts.push_back(
								ConstantExpr::getBitCast(
										getMethodInClass(Imeth), PTy));
						InternalElemts.push_back(
								ConstantExpr::getBitCast(func, PTy));
					}
					Constant* Array = ConstantArray::get(ATy, InternalElemts);

					GlobalVariable* GV = new GlobalVariable(*getLLVMModule(),
							ATy, false, GlobalValue::InternalLinkage, Array,
							"");

					Constant* CI = ConstantExpr::getPtrToInt(GV,
							JavaIntrinsics.pointerSizeType);
					CI = ConstantExpr::getAdd(CI,
							ConstantExpr::getIntegerCast(
									JavaIntrinsics.constantOne,
									JavaIntrinsics.pointerSizeType, false));
					CI = ConstantExpr::getIntToPtr(CI, PTy);
					IElemts.push_back(CI);
				}
			} else {
				IElemts.push_back(N);
			}
		}

		Constant* Array = ConstantArray::get(ATy, IElemts);
		GlobalVariable* GV = new GlobalVariable(*getLLVMModule(), ATy, false,
				GlobalValue::InternalLinkage, Array, "");
		Elemts.push_back(ConstantExpr::getBitCast(GV, PTy));
	}

	// methods
	for (uint32 i = JavaVirtualTable::getFirstJavaMethodIndex(); i < size;
			++i) {
		JavaMethod* meth = ((JavaMethod**) RealVT)[i];
		// Primitive classes don't have methods--abstract or otherwise.
		// (But we do have placeholders for j.l.Object methods in their VTs,
		// so just emit NULL's here)
		if (classDef->isPrimitive() || isAbstract(meth->access)) {
			Elemts.push_back(Constant::getNullValue(PTy));
		} else {
			Function* F = getMethodOrStub(meth, maybeCustomize);
			Elemts.push_back(
					ConstantExpr::getCast(Instruction::BitCast, F, PTy));
		}
	}

	Constant* Array = ConstantArray::get(ATy, Elemts);

	return Array;
}

const char *
JavaAOTCompiler::getHostTriple() {
	return "i686-pc-cygwin";
}

JavaAOTCompiler::JavaAOTCompiler(const std::string& ModuleID) :
		JavaLLVMCompiler(ModuleID) {

	std::string Error;

	bootClasspathEnv = NULL;
	Triple theTriple("i686-pc-cygwin");
	TargetRegistry::printRegisteredTargetsForVersion();
	const Target* TheTarget(TargetRegistry::lookupTarget(theTriple.getTriple(), Error));
	if(TheTarget == NULL) {
		printf("Target not found!\n");
		exit(-1);
	}
	StringRef triple = StringRef("i686-pc-cygwin");
	StringRef cpu="x86";
	StringRef features = "";
	Reloc::Model relocModel = Reloc::PIC_;
	CodeModel::Model codeModel = CodeModel::Default;
	CodeGenOpt::Level optLevel = CodeGenOpt::Default;
	TargetOptions Options;
	Options.LessPreciseFPMADOption = false;
	Options.PrintMachineCode = false;
	Options.NoFramePointerElim = false;
	Options.NoFramePointerElimNonLeaf = false;
	Options.NoExcessFPPrecision = false;
	Options.UnsafeFPMath = false;
	Options.NoInfsFPMath = false;
	Options.NoNaNsFPMath = false;
	Options.HonorSignDependentRoundingFPMathOption = false;
	Options.UseSoftFloat = false;
//	  if (FloatABIForCalls != FloatABI::Default)
	Options.FloatABIType = FloatABI::Default;
	Options.NoZerosInBSS = false;
	Options.GuaranteedTailCallOpt = false;
	Options.DisableTailCalls = false;
	Options.StackAlignmentOverride = 0;
	Options.RealignStack = true;
	Options.DisableJumpTables = false;
	Options.TrapFuncName = "";
	Options.PositionIndependentExecutable = true;
	Options.EnableSegmentedStacks = false;

	const TargetMachine *TM = TheTarget->createTargetMachine(
			theTriple.getTriple(), cpu, features, Options, relocModel,
			codeModel, optLevel);
	TheTargetData = TM->getTargetData();
	TheModule->setDataLayout(TheTargetData->getStringRepresentation());
	TheModule->setTargetTriple(TM->getTargetTriple());
	JavaIntrinsics.init(TheModule);
	initialiseAssessorInfo();

	generateStubs = true;
	assumeCompiled = false;
	compileRT = false;
	precompile = true;
	emitClassBytes = false;

	std::vector<llvm::Type*> llvmArgs;
	FunctionType* FTy = FunctionType::get(Type::getVoidTy(getLLVMContext()),
			llvmArgs, false);
	Callback = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"staticCallback", getLLVMModule());

	llvmArgs.clear();
	llvmArgs.push_back(JavaIntrinsics.JavaMethodType);

	FTy = FunctionType::get(JavaIntrinsics.ptrType, llvmArgs, false);

	NativeLoader = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"vmjcNativeLoader", getLLVMModule());

	llvmArgs.clear();
	FTy = FunctionType::get(Type::getVoidTy(getLLVMContext()), llvmArgs, false);
	ObjectPrinter = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"printJavaObject", getLLVMModule());

	ArrayObjectTracer = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"ArrayObjectTracer", getLLVMModule());

	RegularObjectTracer = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"RegularObjectTracer", getLLVMModule());

	JavaObjectTracer = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"JavaObjectTracer", getLLVMModule());

	ReferenceObjectTracer = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"ReferenceObjectTracer", getLLVMModule());

	EmptyDestructorFunction = Function::Create(FTy,
			GlobalValue::ExternalLinkage, "EmptyDestructor", getLLVMModule());

	UTF8EmptyGV = new GlobalVariable(*getLLVMModule(),
			JavaIntrinsics.UTF8Type->getContainedType(0), false,
			GlobalValue::ExternalLinkage, NULL, "EmptyKey");

	UTF8TombstoneGV = new GlobalVariable(*getLLVMModule(),
			JavaIntrinsics.UTF8Type->getContainedType(0), false,
			GlobalValue::ExternalLinkage, NULL, "TombstoneKey");
}

void JavaAOTCompiler::printStats() {
	fprintf(stdout,
			"----------------- Info from the module -----------------\n");
	fprintf(stdout, "Number of native classes            : %llu\n",
			(unsigned long long int) nativeClasses.size());
	fprintf(stdout, "Number of Java classes              : %llu\n",
			(unsigned long long int) javaClasses.size());
	fprintf(stdout, "Number of external array classes    : %llu\n",
			(unsigned long long int) arrayClasses.size());
	fprintf(stdout, "Number of virtual tables            : %llu\n",
			(unsigned long long int) virtualTables.size());
	fprintf(stdout, "Number of static instances          : %llu\n",
			(unsigned long long int) staticInstances.size());
	fprintf(stdout, "Number of constant pools            : %llu\n",
			(unsigned long long int) resolvedConstantPools.size());
	fprintf(stdout, "Number of strings                   : %llu\n",
			(unsigned long long int) strings.size());
	fprintf(stdout, "Number of native functions          : %llu\n",
			(unsigned long long int) nativeFunctions.size());
	fprintf(stdout,
			"----------------- Total size in .data ------------------\n");
	uint64 size = 0;
	Module* Mod = getLLVMModule();
	for (Module::const_global_iterator i = Mod->global_begin(), e =
			Mod->global_end(); i != e; ++i) {
		size += TheTargetData->getTypeAllocSize(i->getType());
	}
	fprintf(stdout, "%lluB\n", (unsigned long long int) size);
}

void JavaAOTCompiler::CreateStaticInitializer() {

	std::vector<llvm::Type*> llvmArgs;
	llvmArgs.push_back(JavaIntrinsics.ptrType); // class loader
	llvmArgs.push_back(JavaIntrinsics.JavaClassType); // cl
	FunctionType* FTy = FunctionType::get(Type::getVoidTy(getLLVMContext()),
			llvmArgs, false);

	Function* AddClass = Function::Create(FTy, GlobalValue::ExternalLinkage,
			"vmjcAddPreCompiledClass", getLLVMModule());

	llvmArgs.clear();
	llvmArgs.push_back(JavaIntrinsics.ptrType); // class loader.
	FTy = FunctionType::get(Type::getVoidTy(getLLVMContext()), llvmArgs, false);

	StaticInitializer = Function::Create(FTy, GlobalValue::InternalLinkage,
			"Init", getLLVMModule());

	llvmArgs.clear();
	// class loader
	llvmArgs.push_back(JavaIntrinsics.ptrType);
	// array ptr
	llvmArgs.push_back(
			PointerType::getUnqual(JavaIntrinsics.JavaClassArrayType));
	// name
	llvmArgs.push_back(JavaIntrinsics.UTF8Type);
	FTy = FunctionType::get(Type::getVoidTy(getLLVMContext()), llvmArgs, false);

	Function* GetClassArray = Function::Create(FTy,
			GlobalValue::ExternalLinkage, "vmjcGetClassArray", getLLVMModule());

	BasicBlock* currentBlock = BasicBlock::Create(getLLVMContext(), "enter",
			StaticInitializer);
	Function::arg_iterator loader = StaticInitializer->arg_begin();

	Value* Args[3];
	// If we have defined some strings.
	if (strings.begin() != strings.end()) {
		llvmArgs.clear();
		llvmArgs.push_back(JavaIntrinsics.ptrType); // class loader
		llvmArgs.push_back(strings.begin()->second->getType()); // val
		FTy = FunctionType::get(Type::getVoidTy(getLLVMContext()), llvmArgs,
				false);

		Function* AddString = Function::Create(FTy,
				GlobalValue::ExternalLinkage, "vmjcAddString", getLLVMModule());

		for (string_iterator i = strings.begin(), e = strings.end(); i != e;
				++i) {
			Args[0] = loader;
			Args[1] = i->second;
			CallInst::Create(AddString, ArrayRef<Value*>(Args, 2), "",
					currentBlock);
		}
	}

	for (native_class_iterator i = nativeClasses.begin(), e =
			nativeClasses.end(); i != e; ++i) {
		if (isCompiling(i->first)) {
			Args[0] = loader;
			Args[1] = ConstantExpr::getBitCast(i->second,
					JavaIntrinsics.JavaClassType);
			CallInst::Create(AddClass, ArrayRef<Value*>(Args, 2), "",
					currentBlock);
		}
	}

	for (array_class_iterator i = arrayClasses.begin(), e = arrayClasses.end();
			i != e; ++i) {
		Args[0] = loader;
		Args[1] = i->second;
		Args[2] = getUTF8(i->first->name);
		CallInst::Create(GetClassArray, ArrayRef<Value*>(Args, 3), "",
				currentBlock);
	}

	ReturnInst::Create(getLLVMContext(), currentBlock);
}

void JavaAOTCompiler::makeVT(Class* cl) {
	JavaVirtualTable* VT = cl->virtualVT;

	if (cl->super) {
		// Copy the super VT into the current VT.
		uint32 size = cl->super->virtualTableSize
				- JavaVirtualTable::getFirstJavaMethodIndex();
		memcpy(VT->getFirstJavaMethod(),
				cl->super->virtualVT->getFirstJavaMethod(),
				size * sizeof(word_t));
		VT->destructor = cl->super->virtualVT->destructor;
	}

	for (uint32 i = 0; i < cl->nbVirtualMethods; ++i) {
		JavaMethod& meth = cl->virtualMethods[i];
		((void**) VT)[meth.offset] = &meth;
	}

//	if (!cl->super)
//		VT->destructor = reinterpret_cast<word_t>(EmptyDestructor);
}

void JavaAOTCompiler::makeIMT(Class* cl) {
}

void JavaAOTCompiler::setMethod(Function* func, void* ptr, const char* name) {
	func->setName(name);
	func->setLinkage(GlobalValue::ExternalLinkage);
}

void JavaAOTCompiler::compileClass(Class* cl) {

	// Make sure the class is emitted.
	getNativeClass(cl);

	for (uint32 i = 0; i < cl->nbVirtualMethods; ++i) {
		JavaMethod& meth = cl->virtualMethods[i];
		if (!isAbstract(meth.access))
			parseFunction(&meth, cl);
		if (generateStubs)
			compileAllStubs(meth.getSignature());
	}

	for (uint32 i = 0; i < cl->nbStaticMethods; ++i) {
		JavaMethod& meth = cl->staticMethods[i];
		if (!isAbstract(meth.access))
			parseFunction(&meth, NULL);
		if (generateStubs)
			compileAllStubs(meth.getSignature());
	}
}

void JavaAOTCompiler::extractFiles(ClassBytes* bytes, ClassLoader* loader) {
	ZipArchive archive(bytes);

	char* realName = new char[4096];
	for (ZipArchive::table_iterator i = archive.filetable.begin(), e =
			archive.filetable.end(); i != e; ++i) {
		ZipFile* file = i->second;

		char* name = file->filename;
		uint32 size = strlen(name);
		if (size > 6 && !strcmp(&(name[size - 6]), ".class")) {
			memcpy(realName, name, size);
			realName[size - 6] = 0;
			const UTF8* utf8 = loader->asciizConstructUTF8(realName);
			ClassBytes *zippedClass = new ClassBytes(file->ucsize);
			archive.readFile(zippedClass, file);
			Class* cl = loader->loadName(utf8, zippedClass);
			assert(cl && "Class not created");
			classes.push_back(cl);
		} else if (size > 4
				&& (!strcmp(&name[size - 4], ".jar")
						|| !strcmp(&name[size - 4], ".zip"))) {
			ClassBytes* res = new ClassBytes(file->ucsize);
			int ok = archive.readFile(res, file);
			if (!ok)
				return;

			extractFiles(res, loader);
		}
	}
}

static const char* name;

extern "C" void UnreachableMagicMMTk() {
	std::cerr << "UREACHABLE!";
}

void JavaAOTCompiler::analyseClasspathEnv(const char* str) {
	ClassBytes* bytes = NULL;
	if (str != 0) {
		unsigned int len = strlen(str);
		char* buf = new char[len + 1];
		const char* cur = str;
		int top = 0;
		char c = 1;
		while (c != 0) {
			while (((c = cur[top]) != 0) && c != envSeparator[0]) {
				top++;
			}
			if (top != 0) {
				memcpy(buf, cur, top);
				buf[top] = 0;
				char* rp = new char[PATH_MAX];
				memset(rp, 0, PATH_MAX);
				rp = realpath(buf, rp);
				if (rp && rp[PATH_MAX - 1] == 0 && strlen(rp) != 0) {
					struct stat st;
					stat(rp, &st);
					if ((st.st_mode & S_IFMT) == S_IFDIR) {
						unsigned int len = strlen(rp);
						char* temp = new char[len + 2];
						memcpy(temp, rp, len);
						temp[len] = dirSeparator[0];
						temp[len + 1] = 0;
						bootClasspath.push_back(temp);
					} else {
						bytes = Reader::openFile(rp);
						if (bytes) {
							ZipArchive *archive = new ZipArchive(bytes);
							if (archive) {
								bootArchives.push_back(archive);
							}
						}
					}
				}
			}
			cur = cur + top + 1;
			top = 0;
		}
	}
}

void JavaAOTCompiler::mainCompilerStart() {

    // setup bootstrap loader
	ClassLoader* loader = new ClassLoader();

	addJavaPasses();

	analyseClasspathEnv(bootClasspathEnv);
	uint32 size = strlen(name);
	if (size > 4
			&& (!strcmp(&name[size - 4], ".jar")
					|| !strcmp(&name[size - 4], ".zip"))) {
		analyseClasspathEnv(name);
	}


#if 0
	JavaJITCompiler* Comp = NULL;
	if (!clinits->empty()) {
		Comp = JavaJITCompiler::CreateCompiler("JIT");
		Comp->EmitFunctionName = true;
//    if (!M->useCooperativeGC()) {
//      Comp->disableCooperativeGC();
//    }
//    bootstrapLoader->setCompiler(Comp);
		analyseClasspathEnv(vm->classpath);
	} else {
		bootstrapLoader->analyseClasspathEnv(vm->classpath);
		bootstrapLoader->upcalls->initialiseClasspath(bootstrapLoader);
	}
#endif

	if (size > 4
		&& (!strcmp(&name[size - 4], ".jar")
		|| !strcmp(&name[size - 4], ".zip"))) {

		ClassBytes* bytes = Reader::openFile(name);

		if (!bytes) {
			fprintf(stderr, "Can't find zip file.\n");
			// goto end;
			return;
		}

		extractFiles(bytes, loader);

		// First resolve everyone so that there can not be unknown references in
		// constant pools.
		for (std::vector<Class*>::iterator i = classes.begin(), e =
				classes.end(); i != e; ++i) {
			Class* cl = *i;
			cl->resolveClass();

			for (uint32 i = 0; i < cl->nbVirtualMethods; ++i) {
				if (!isAbstract(cl->virtualMethods[i].access)) {
					getMethod(&cl->virtualMethods[i], NULL);
				}
			}

			for (uint32 i = 0; i < cl->nbStaticMethods; ++i) {
				getMethod(&cl->staticMethods[i], NULL);
			}
		}

		if (!clinits->empty()) {

			// First, if we have the magic classes available, make sure we
			// compile them so that we can call them directly and let
			// LowerMagic lower them later on.
			for (std::vector<Class*>::iterator ii = classes.begin(), ee =
					classes.end(); ii != ee; ++ii) {
				Class* cl = *ii;
				static const std::string magic = "org/vmmagic";
				static void* ptr = (void*) (word_t) UnreachableMagicMMTk;
				if (!strncmp(UTF8Buffer(cl->name).cString(), magic.c_str(),
						magic.length() - 1)) {
					for (uint32 i = 0; i < cl->nbVirtualMethods; ++i) {
						if (!isAbstract(cl->virtualMethods[i].access)) {
							Function* F = getMethod(&cl->virtualMethods[i],
									NULL);
							setMethod(F, ptr, F->getName().data());
							cl->virtualMethods[i].compiledPtr();
							// Set native so that we don't try to inline it.
							cl->virtualMethods[i].setNative();
						}
					}

					for (uint32 i = 0; i < cl->nbStaticMethods; ++i) {
						Function* F = getMethod(&cl->staticMethods[i], NULL);
						setMethod(F, ptr, F->getName().data());
						cl->staticMethods[i].compiledPtr();
						// Set native so that we don't try to inline it.
						cl->staticMethods[i].setNative();
					}
				}
			}

			// Initialize all classes given with with the -with-clinit
			// command line argument.
			for (std::vector<std::string>::iterator i = clinits->begin(), e =
					clinits->end(); i != e; ++i) {
				Class* cl = NULL;
//				TRY{
					if (i->at(i->length() - 1) == '*') {
						for (std::vector<Class*>::iterator ii = classes.begin(),
								ee = classes.end(); ii != ee; ++ii) {
							cl = *ii;
//              if (!strncmp(UTF8Buffer(cl->name).cString(), i->c_str(),
//                           i->length() - 1)) {
//                cl->initialiseClass(vm);
//              }
						}
					} else {
						const UTF8* name = loader->asciizConstructUTF8(i->c_str());
						CommonClass* cls = loader->lookupClass(name);
						if (cls && cls->isClass()) {
							cl = cls->asClass();
							// cl->initialiseClass(vm);
						} else {
							fprintf(stderr, "Class %s does not exist or is an array class.\n",
									i->c_str());
						}
					}
//				}CATCH {
//					fprintf(stderr, "Error when initializing %s\n",
//							UTF8Buffer(cl->name).cString());
//					abort();
//				}END_CATCH;
			}
			loader->setCompiler(this);
		}

		// Finally, compile all classes.
		for (std::vector<Class*>::iterator i = classes.begin(), e = classes.end();
				i != e; ++i) {
			compileClass(*i);
		}
	} else {
		char* realName = new char[size + 1];
		if (size > 6 && !strcmp(&name[size - 6], ".class")) {
			memcpy(realName, name, size - 6);
			realName[size - 6] = 0;
		} else {
			memcpy(realName, name, size + 1);
		}

		const UTF8* utf8 = loader->asciizConstructUTF8(realName);
		ClassBytes* bytes = Reader::openFile(name);
		Class* cl = loader->constructClass(utf8, bytes);
#if 0
		if (!clinits->empty()) {
			vm->loadBootstrap();
			cl->initialiseClass(vm);
			bootstrapLoader->setCompiler(M);
		}
		cl->setOwnerClass(JavaThread::get());
#endif
		cl->resolveInnerOuterClasses();
		for (uint32 i = 0; i < cl->nbInnerClasses; ++i) {
			compileClass(cl->innerClasses[i]);
		}
		compileClass(cl);
	}

	if (compileRT) {
		// fixme
		// Make sure that if we compile RT, the native classes are emitted.
//    getNativeClass(bootstrapLoader->upcalls->OfVoid);
//    getNativeClass(bootstrapLoader->upcalls->OfBool);
//    getNativeClass(bootstrapLoader->upcalls->OfByte);
//    getNativeClass(bootstrapLoader->upcalls->OfChar);
//    getNativeClass(bootstrapLoader->upcalls->OfShort);
//    getNativeClass(bootstrapLoader->upcalls->OfInt);
//    getNativeClass(bootstrapLoader->upcalls->OfFloat);
//    getNativeClass(bootstrapLoader->upcalls->OfLong);
//    getNativeClass(bootstrapLoader->upcalls->OfDouble);
	}

	CreateStaticInitializer();

#if 0
	end:

	vm->threadSystem.leave();
#endif
}

void JavaAOTCompiler::compileFile(const char* n) {
	name = n;
	mainCompilerStart();
}

void JavaAOTCompiler::generateClassBytes(ClassLoader* loader) {
	emitClassBytes = true;
	// Add the bootstrap classes to the image.
	for (std::vector<ZipArchive*>::iterator i = bootArchives.begin(),
			e = bootArchives.end(); i != e; ++i) {
		ZipArchive* archive = *i;
		for (ZipArchive::table_iterator zi = archive->filetable.begin(), ze =
				archive->filetable.end(); zi != ze; zi++) {
			// Remove the '.class'.
			const char* name = zi->first;
			std::string str(name, strlen(name) - strlen(".class"));
			ClassBytes* bytes = Reader::openZip(archive, name);
			getClassBytes(loader->asciizConstructUTF8(str.c_str()), bytes);
		}
	}
}

/// compileAllStubs - Compile all the native -> Java stubs. 
/// TODO: Once LLVM supports va_arg, enable AP.
///
void JavaAOTCompiler::compileAllStubs(Signdef* sign) {
//	sign->getStaticCallBuf();
	// getStaticCallAP();
//	sign->getVirtualCallBuf();
	// getVirtualCallAP();
}

void JavaAOTCompiler::generateMain(const char* name, bool jit) {

	// Type Definitions
	std::vector<Type*> FuncArgs;
	FuncArgs.push_back(Type::getInt32Ty(getLLVMContext()));
	FuncArgs.push_back(PointerType::getUnqual(JavaIntrinsics.ptrType));

	FunctionType* FuncTy = FunctionType::get(Type::getInt32Ty(getLLVMContext()),
			FuncArgs, false);

	Function* MainFunc = Function::Create(FuncTy, GlobalValue::ExternalLinkage,
			"main", TheModule);
	BasicBlock* currentBlock = BasicBlock::Create(getLLVMContext(), "enter",
			MainFunc);

	GlobalVariable* GvarArrayStr = new GlobalVariable(*TheModule,
			ArrayType::get(Type::getInt8Ty(getLLVMContext()), strlen(name) + 1),
			true, GlobalValue::InternalLinkage, 0, "mainClass");

	// fixme
	Constant* NameArray = 0; // ConstantArray::get(getLLVMContext(), name, true);
	GvarArrayStr->setInitializer(NameArray);
	Value* Indices[2] = { JavaIntrinsics.constantZero,
			JavaIntrinsics.constantZero };
	Value* ArgName = ConstantExpr::getGetElementPtr(GvarArrayStr, Indices, 2);

	Function::arg_iterator FuncVals = MainFunc->arg_begin();
	Value* Argc = FuncVals++;
	Value* Argv = FuncVals++;
	Value* Args[3] = { Argc, Argv, ArgName };

	FuncArgs.push_back(Args[2]->getType());

	FuncTy = FunctionType::get(Type::getInt32Ty(getLLVMContext()), FuncArgs,
			false);

	Function* CalledFunc = Function::Create(FuncTy,
			GlobalValue::ExternalLinkage,
			jit ? "StartJnjvmWithJIT" : "StartJnjvmWithoutJIT", TheModule);

	Value* res = CallInst::Create(CalledFunc, ArrayRef<Value*>(Args, 3), "", currentBlock);
	ReturnInst::Create(getLLVMContext(), res, currentBlock);

}

CommonClass* JavaAOTCompiler::getUniqueBaseClass(CommonClass* cl) {
	assert(0 && "Implement me");
}

Constant* JavaAOTCompiler::getFinalObject(JavaObject* obj, CommonClass* objCl) {
	assert(0 && "Implement me");
}
