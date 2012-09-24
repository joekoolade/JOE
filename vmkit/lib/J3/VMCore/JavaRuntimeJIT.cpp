//===-------------------- JavaRuntimeJIT.cpp ------------------------------===//
//=== ---- Runtime functions called by code compiled by the JIT -----------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "ClasspathReflect.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaConstantPool.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

#include "j3/OpcodeNames.def"

#include <cstdarg>

using namespace j3;

extern "C" void* j3InterfaceLookup(UserClass* caller, uint32 index) {

  void* res = 0;

  UserConstantPool* ctpInfo = caller->getConstantPool();
  if (ctpInfo->ctpRes[index]) {
    res = ctpInfo->ctpRes[index];
  } else {
    UserCommonClass* cl = 0;
    const UTF8* utf8 = 0;
    Signdef* sign = 0;
  
    ctpInfo->resolveMethod(index, cl, utf8, sign);
    assert(cl->isClass() && isInterface(cl->access) && "Wrong type of method");
    res = cl->asClass()->lookupInterfaceMethod(utf8, sign->keyName);
    
    ctpInfo->ctpRes[index] = (void*)res;
  }
  return res;
}

// Throws if the field is not found.
extern "C" void* j3VirtualFieldLookup(UserClass* caller, uint32 index) {
  
  void* res = 0;

  UserConstantPool* ctpInfo = caller->getConstantPool();
  if (ctpInfo->ctpRes[index]) {
    res = ctpInfo->ctpRes[index];
  } else {
  
    UserCommonClass* cl = 0;
    const UTF8* utf8 = 0;
    Typedef* sign = 0;
  
    ctpInfo->resolveField(index, cl, utf8, sign);
 
    UserClass* lookup = cl->isArray() ? cl->super : cl->asClass();
    JavaField* field = lookup->lookupField(utf8, sign->keyName, false, true, 0);
  
    ctpInfo->ctpRes[index] = (void*)field->ptrOffset;
  
    res = (void*)field->ptrOffset;
  }

  return res;
}

// Throws if the field or its class is not found.
extern "C" void* j3StaticFieldLookup(UserClass* caller, uint32 index) {
  
  void* res = 0;
  
  UserConstantPool* ctpInfo = caller->getConstantPool();
  
  if (ctpInfo->ctpRes[index]) {
    res = ctpInfo->ctpRes[index];
  } else {
  
    UserCommonClass* cl = 0;
    UserClass* fieldCl = 0;
    const UTF8* utf8 = 0;
    Typedef* sign = 0;
  
    ctpInfo->resolveField(index, cl, utf8, sign);
 
    assert(cl->asClass() && "Lookup a field of something not an array");
    JavaField* field = cl->asClass()->lookupField(utf8, sign->keyName, true,
                                                  true, &fieldCl);
    
    fieldCl->initialiseClass(JavaThread::get()->getJVM());
    void* obj = ((UserClass*)fieldCl)->getStaticInstance();
  
    assert(obj && "No static instance in static field lookup");
  
    void* ptr = (void*)((uint64)obj + field->ptrOffset);
    ctpInfo->ctpRes[index] = ptr;
   
    res = ptr;
  }

  return res;
}

// Throws if the method is not found.
extern "C" uint32 j3VirtualTableLookup(UserClass* caller, uint32 index,
                                       uint32* offset, JavaObject* obj) {
  llvm_gcroot(obj, 0);
  uint32 res = 0;
  
  UserCommonClass* cl = 0;
  const UTF8* utf8 = 0;
  Signdef* sign = 0;
  
  caller->getConstantPool()->resolveMethod(index, cl, utf8, sign);
  UserClass* lookup = cl->isArray() ? cl->super : cl->asClass();
  JavaMethod* dmeth = lookup->lookupMethodDontThrow(utf8, sign->keyName, false,
                                                    true, 0);
  if (!dmeth) {
    assert((JavaObject::getClass(obj)->isClass() && 
            JavaObject::getClass(obj)->asClass()->isInitializing()) &&
           "Class not ready in a virtual lookup.");
    // Arg, the bytecode is buggy! Perform the lookup on the object class
    // and do not update offset.
    lookup = JavaObject::getClass(obj)->isArray() ?
      JavaObject::getClass(obj)->super : 
      JavaObject::getClass(obj)->asClass();
    dmeth = lookup->lookupMethod(utf8, sign->keyName, false, true, 0);
  } else {
    *offset = dmeth->offset;
  }

  assert(dmeth->classDef->isInitializing() && 
         "Class not ready in a virtual lookup.");

  res = dmeth->offset;

  return res;
}

// Throws if the class is not found.
extern "C" void* j3ClassLookup(UserClass* caller, uint32 index) { 
  
  void* res = 0;
  
  UserConstantPool* ctpInfo = caller->getConstantPool();
  UserCommonClass* cl = ctpInfo->loadClass(index);
  // We can not initialize here, because bytecodes such as CHECKCAST
  // or classes used in catch clauses do not trigger class initialization.
  // This is really sad, because we need to insert class initialization checks
  // in the LLVM code.
  assert(cl && "No cl after class lookup");
  res = (void*)cl;
 
  // Create the array class, in case we come from a ANEWARRAY.
  if (cl->isClass() && !cl->virtualVT->baseClassVT) { 
    const UTF8* arrayName =
      cl->classLoader->constructArrayName(1, cl->getName());
    cl->virtualVT->baseClassVT =
      cl->classLoader->constructArray(arrayName)->virtualVT;
  }

  return res;
}

// Calls Java code.
// Throws if initializing the class throws an exception.
extern "C" UserCommonClass* j3RuntimeInitialiseClass(UserClass* cl) {
  cl->resolveClass();
  cl->initialiseClass(JavaThread::get()->getJVM());
  return cl;
}

// Calls Java code.
extern "C" JavaObject* j3RuntimeDelegatee(UserCommonClass* cl) {
  return cl->getClassDelegatee(JavaThread::get()->getJVM());
}

// Throws if one of the dimension is negative.
JavaObject* multiCallNewIntern(UserClassArray* cl, uint32 len,
                               sint32* dims, Jnjvm* vm) {
  assert(len > 0 && "Negative size given by VMKit");
 
  JavaObject* _res = cl->doNew(dims[0], vm);
  ArrayObject* res = NULL;
  JavaObject* temp = NULL;
  llvm_gcroot(_res, 0);
  llvm_gcroot(res, 0);
  llvm_gcroot(temp, 0);

  if (len > 1) {
    res = (ArrayObject*)_res;
    UserCommonClass* _base = cl->baseClass();
    assert(_base->isArray() && "Base class not an array");
    UserClassArray* base = (UserClassArray*)_base;
    if (dims[0] > 0) {
      for (sint32 i = 0; i < dims[0]; ++i) {
        temp = multiCallNewIntern(base, (len - 1), &dims[1], vm);
        ArrayObject::setElement(res, temp, i);
      }
    } else {
      for (uint32 i = 1; i < len; ++i) {
        sint32 p = dims[i];
        if (p < 0) {
          JavaThread::get()->getJVM()->negativeArraySizeException(p);
        }
      }
    }
  }
  return _res;
}

// Throws if one of the dimension is negative.
extern "C" JavaObject* j3MultiCallNew(UserClassArray* cl, uint32 len, ...) {
  JavaObject* res = 0;
  llvm_gcroot(res, 0);

  va_list ap;
  va_start(ap, len);
  mvm::ThreadAllocator allocator;
  sint32* dims = (sint32*)allocator.Allocate(sizeof(sint32) * len);
  for (uint32 i = 0; i < len; ++i){
    dims[i] = va_arg(ap, int);
  }
  Jnjvm* vm = JavaThread::get()->getJVM();
  res = multiCallNewIntern(cl, len, dims, vm);

  return res;
}

// Throws if the class can not be resolved.
extern "C" JavaVirtualTable* j3GetArrayClass(UserClass* caller,
                                             uint32 index,
                                             JavaVirtualTable** VT) {
  JavaVirtualTable* res = 0;
  assert(VT && "Incorrect call to j3GetArrayClass");
  
  UserConstantPool* ctpInfo = caller->getConstantPool();
  UserCommonClass* cl = ctpInfo->loadClass(index);
  
  JnjvmClassLoader* JCL = cl->classLoader;
  if (cl->asClass()) cl->asClass()->resolveClass();
  const UTF8* arrayName = JCL->constructArrayName(1, cl->getName());
  
  res = JCL->constructArray(arrayName)->virtualVT;
  *VT = res;

  return res;
}

// Does not call Java code. Can not yield a GC.
extern "C" void j3EndJNI(uint32** oldLRN) {
  JavaThread* th = JavaThread::get();
  
  // We're going back to Java
  th->endJNI();
  
  // Update the number of references.
  th->currentAddedReferences = *oldLRN;
}

extern "C" word_t j3StartJNI(uint32* localReferencesNumber,
                               uint32** oldLocalReferencesNumber,
                               mvm::KnownFrame* Frame) 
  __attribute__((noinline));

// Never throws. Does not call Java code. Can not yield a GC. May join a GC.
extern "C" word_t j3StartJNI(uint32* localReferencesNumber,
                               uint32** oldLocalReferencesNumber,
                               mvm::KnownFrame* Frame) {
  
  JavaThread* th = JavaThread::get();
 
  *oldLocalReferencesNumber = th->currentAddedReferences;
  th->currentAddedReferences = localReferencesNumber;
  th->startJNI();
  th->startUnknownFrame(*Frame);
  th->enterUncooperativeCode();
  assert(th->getLastSP() == th->lastKnownFrame->currentFP);

  return Frame->currentFP;
}

// Never throws.
extern "C" void j3JavaObjectAquire(JavaObject* obj) {
  llvm_gcroot(obj, 0);
  JavaObject::acquire(obj);
}

// Never throws.
extern "C" void j3JavaObjectRelease(JavaObject* obj) {
  llvm_gcroot(obj, 0);
  JavaObject::release(obj);
}

extern "C" void j3ThrowException(JavaObject* obj) {
  llvm_gcroot(obj, 0);
  JavaThread::get()->throwException(obj);
  UNREACHABLE();
}

extern "C" JavaObject* j3NullPointerException() {
  return JavaThread::get()->getJVM()->CreateNullPointerException();
}

extern "C" JavaObject* j3NegativeArraySizeException(sint32 val) {
  return JavaThread::get()->getJVM()->CreateNegativeArraySizeException();
}

extern "C" JavaObject* j3OutOfMemoryError(sint32 val) {
  return JavaThread::get()->getJVM()->CreateOutOfMemoryError();
}

extern "C" JavaObject* j3StackOverflowError() {
  return JavaThread::get()->getJVM()->CreateStackOverflowError();
}

extern "C" JavaObject* j3ArithmeticException() {
  return JavaThread::get()->getJVM()->CreateArithmeticException();
}

extern "C" JavaObject* j3ClassCastException(JavaObject* obj,
                                            UserCommonClass* cl) {
  llvm_gcroot(obj, 0);  
  return JavaThread::get()->getJVM()->CreateClassCastException(obj, cl);
}

extern "C" JavaObject* j3IndexOutOfBoundsException(JavaObject* obj,
                                                   sint32 index) {
  llvm_gcroot(obj, 0);
  return JavaThread::get()->getJVM()->CreateIndexOutOfBoundsException(index);
}

extern "C" JavaObject* j3ArrayStoreException(JavaVirtualTable* VT,
                                             JavaVirtualTable* VT2) {
  return JavaThread::get()->getJVM()->CreateArrayStoreException(VT);
}

// Create an exception then throws it.
extern "C" void j3ThrowExceptionFromJIT() {
  JavaObject *exc = 0;
  llvm_gcroot(exc, 0);
  JavaThread *th = JavaThread::get();
  JavaMethod* meth = th->getCallingMethodLevel(0);
  exc = th->getJVM()->CreateUnsatisfiedLinkError(meth);
  j3ThrowException(exc);
}

extern "C" void* j3StringLookup(UserClass* cl, uint32 index) {
  
  JavaString** str = 0;
  UserConstantPool* ctpInfo = cl->getConstantPool();
  const UTF8* utf8 = ctpInfo->UTF8At(ctpInfo->ctpDef[index]);
  str = cl->classLoader->UTF8ToStr(utf8);
  ctpInfo->ctpRes[index] = str;
  return (void*)str;
}

extern "C" void* j3ResolveVirtualStub(JavaObject* obj) {
  llvm_gcroot(obj, 0);
  JavaThread *th = JavaThread::get();
  UserCommonClass* cl = JavaObject::getClass(obj);
  void* result = NULL;
  
  // Lookup the caller of this class.
  mvm::StackWalker Walker(th);
  ++Walker; // Remove the stub.
  mvm::FrameInfo* FI = Walker.get();
  assert(FI->Metadata != NULL && "Wrong stack trace");
  JavaMethod* meth = (JavaMethod*)FI->Metadata;

  // Lookup the method info in the constant pool of the caller.
  uint16 ctpIndex = meth->lookupCtpIndex(FI);
  assert(ctpIndex && "No constant pool index");
  JavaConstantPool* ctpInfo = meth->classDef->getConstantPool();
  CommonClass* ctpCl = 0;
  const UTF8* utf8 = 0;
  Signdef* sign = 0;
  JavaMethod* origMeth = 0;
  ctpInfo->infoOfMethod(ctpIndex, ACC_VIRTUAL, ctpCl, origMeth);

  ctpInfo->resolveMethod(ctpIndex, ctpCl, utf8, sign);
  assert(cl->isAssignableFrom(ctpCl) && "Wrong call object");
  UserClass* lookup = cl->isArray() ? cl->super : cl->asClass();
  JavaMethod* Virt = lookup->lookupMethod(utf8, sign->keyName, false, true, 0);

  if (isAbstract(Virt->access)) {
    JavaThread::get()->getJVM()->abstractMethodError(Virt->classDef, Virt->name);
  }

  // Compile the found method.
  result = Virt->compiledPtr(lookup);

  // Update the virtual table.
  assert(lookup->isResolved() && "Class not resolved");
  assert(lookup->isInitializing() && "Class not ready");
  assert(lookup->virtualVT && "Class has no VT");
  assert(lookup->virtualTableSize > Virt->offset && 
         "The method's offset is greater than the virtual table size");
  ((void**)obj->getVirtualTable())[Virt->offset] = result;
  
  if (isInterface(origMeth->classDef->access)) {
    InterfaceMethodTable* IMT = cl->virtualVT->IMT;
    uint32_t index = InterfaceMethodTable::getIndex(Virt->name, Virt->type);
    if ((IMT->contents[index] & 1) == 0) {
      IMT->contents[index] = (word_t)result;
    } else { 
      JavaMethod* Imeth = 
        ctpCl->asClass()->lookupInterfaceMethodDontThrow(utf8, sign->keyName);
      assert(Imeth && "Method not in hierarchy?");
      word_t* table = (word_t*)(IMT->contents[index] & ~1);
      uint32 i = 0;
      while (table[i] != (word_t)Imeth) { i += 2; }
      table[i + 1] = (word_t)result;
    }
  }

  return result;
}

extern "C" void* j3ResolveStaticStub() {
  JavaThread *th = JavaThread::get();
  void* result = NULL;

  // Lookup the caller of this class.
  mvm::StackWalker Walker(th);
  ++Walker; // Remove the stub.
  mvm::FrameInfo* FI = Walker.get();
  assert(FI->Metadata != NULL && "Wrong stack trace");
  JavaMethod* caller = (JavaMethod*)FI->Metadata;

  // Lookup the method info in the constant pool of the caller.
  uint16 ctpIndex = caller->lookupCtpIndex(FI);
  assert(ctpIndex && "No constant pool index");
  JavaConstantPool* ctpInfo = caller->classDef->getConstantPool();
  CommonClass* cl = 0;
  const UTF8* utf8 = 0;
  Signdef* sign = 0;

  ctpInfo->resolveMethod(ctpIndex, cl, utf8, sign);
  UserClass* lookup = cl->isArray() ? cl->super : cl->asClass();
  assert(lookup->isInitializing() && "Class not ready");
  JavaMethod* callee = lookup->lookupMethod(utf8, sign->keyName, true, true, 0);

  // Compile the found method.
  result = callee->compiledPtr();
    
  // Update the entry in the constant pool.
  ctpInfo->ctpRes[ctpIndex] = result;

  return result;
}

extern "C" void* j3ResolveSpecialStub() {
  JavaThread *th = JavaThread::get();
  void* result = NULL;

  // Lookup the caller of this class.
  mvm::StackWalker Walker(th);
  ++Walker; // Remove the stub.
  mvm::FrameInfo* FI = Walker.get();
  assert(FI->Metadata != NULL && "Wrong stack trace");
  JavaMethod* caller = (JavaMethod*)FI->Metadata;

  // Lookup the method info in the constant pool of the caller.
  uint16 ctpIndex = caller->lookupCtpIndex(FI);
  assert(ctpIndex && "No constant pool index");
  JavaConstantPool* ctpInfo = caller->classDef->getConstantPool();
  CommonClass* cl = 0;
  const UTF8* utf8 = 0;
  Signdef* sign = 0;

  ctpInfo->resolveMethod(ctpIndex, cl, utf8, sign);
  UserClass* lookup = cl->isArray() ? cl->super : cl->asClass();
  assert(lookup->isInitializing() && "Class not ready");
  JavaMethod* callee =
    lookup->lookupSpecialMethodDontThrow(utf8, sign->keyName, caller->classDef);
  
  if (!callee) {
    th->getJVM()->noSuchMethodError(lookup, utf8);
  }
  if (isAbstract(callee->access)) {
    JavaThread::get()->getJVM()->abstractMethodError(callee->classDef, callee->name);
  }

  // Compile the found method.
  result = callee->compiledPtr();
    
  // Update the entry in the constant pool.
  ctpInfo->ctpRes[ctpIndex] = result;

  return result;
}

// Does not throw an exception.
extern "C" void* j3ResolveInterface(JavaObject* obj, JavaMethod* meth, uint32_t index) {
  word_t result = NULL;
  InterfaceMethodTable* IMT = JavaObject::getClass(obj)->virtualVT->IMT;
  if ((IMT->contents[index] & 1) == 0) {
    result = IMT->contents[index];
  } else {
    word_t* table = (word_t*)(IMT->contents[index] & ~1);
    uint32 i = 0;
    while (table[i] != (word_t)meth && table[i] != 0) { i += 2; }
    assert(table[i] != 0);
    result = table[i + 1];
  }
  // TODO(ngeoffray): This code is too performance critical to get asserts.
  // Ideally, it would be inlined by the compiler, so this method is
  // only for debugging.
  //
  // assert(JavaObject::instanceOf(obj, meth->classDef));
  // assert(meth->classDef->isInterface() ||
  //    (meth->classDef == meth->classDef->classLoader->bootstrapLoader->upcalls->OfObject));
  // assert(index == InterfaceMethodTable::getIndex(meth->name, meth->type));
  // assert((result != 0) && "Bad IMT");
  return (void*)result;
}

extern "C" void j3PrintMethodStart(JavaMethod* meth) {
  fprintf(stderr, "[%p] executing %s.%s\n", (void*)mvm::Thread::get(),
          UTF8Buffer(meth->classDef->name).cString(),
          UTF8Buffer(meth->name).cString());
}

extern "C" void j3PrintMethodEnd(JavaMethod* meth) {
  fprintf(stderr, "[%p] return from %s.%s\n", (void*)mvm::Thread::get(),
          UTF8Buffer(meth->classDef->name).cString(),
          UTF8Buffer(meth->name).cString());
}

extern "C" void j3PrintExecution(uint32 opcode, uint32 index,
                                    JavaMethod* meth) {
  fprintf(stderr, "[%p] executing %s.%s %s at %d\n", (void*)mvm::Thread::get(),
         UTF8Buffer(meth->classDef->name).cString(),
         UTF8Buffer(meth->name).cString(),
         OpcodeNames[opcode], index);
}
