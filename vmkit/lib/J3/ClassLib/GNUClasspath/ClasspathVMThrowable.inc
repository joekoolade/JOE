//===- ClasspathVMClassLoader.cpp - GNU classpath java/lang/VMClassLoader -===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "types.h"

#include "Classpath.h"
#include "JavaAccess.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaConstantPool.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "Reader.h"

using namespace j3;

extern "C" {

JavaObject* internalFillInStackTrace(JavaObject* throwable) {
  
  JavaObject* vmThrowable = 0;
  ArrayPtr* result = 0;
  llvm_gcroot(throwable, 0);
  llvm_gcroot(vmThrowable, 0);
  llvm_gcroot(result, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
 
  uint32 length = th->getFrameContextLength();

  if (sizeof(void*) == 4) {
    ClassArray* cl = vm->upcalls->ArrayOfInt;
    result = (ArrayPtr*) cl->doNew(length, vm);
  } else {
    ClassArray* cl = vm->upcalls->ArrayOfLong;
    result = (ArrayPtr*) cl->doNew(length, vm);
  }
  
  // Don't call th->getFrameContext because it is not GC-safe.
  mvm::StackWalker Walker(th);
  uint32_t i = 0;

  while (intptr_t ip = *Walker) {
    ArrayPtr::setElement(result, ip, i);
    ++i;
    ++Walker;
  }

  // Set the tempory data in the new VMThrowable object.
  vmThrowable = vm->upcalls->newVMThrowable->doNew(vm);
  vm->upcalls->vmDataVMThrowable->setInstanceObjectField(vmThrowable, result);
  return vmThrowable;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMThrowable_fillInStackTrace(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* throwable) {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(throwable, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  res = internalFillInStackTrace(throwable);

  END_NATIVE_EXCEPTION

  return res;
}


JavaObject* consStackElement(mvm::FrameInfo* FI, intptr_t ip) {

  JavaString* methodName = 0;
  JavaString* className = 0;
  JavaString* sourceName = 0;
  JavaObject* res = 0;
  llvm_gcroot(methodName, 0);
  llvm_gcroot(className, 0);
  llvm_gcroot(sourceName, 0);
  llvm_gcroot(res, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaMethod* meth = (JavaMethod*)FI->Metadata;
  methodName = vm->internalUTF8ToStr(meth->name);
  Class* cl = meth->classDef;
  className = JavaString::internalToJava(cl->name, vm);
  
  Attribut* sourceAtt = cl->lookupAttribut(Attribut::sourceFileAttribut);
  
  // We don't have the bytes if the class was vmjc'ed.
  if (sourceAtt && cl->getBytes()) {
    Reader reader(sourceAtt, cl->bytes);
    uint16 index = reader.readU2();
    sourceName = vm->internalUTF8ToStr(cl->getConstantPool()->UTF8At(index));
  }

  bool native = isNative(meth->access);
  uint16 lineNumber = meth->lookupLineNumber(FI);

  UserClass* newS = vm->upcalls->newStackTraceElement;
  res = newS->doNew(vm);
  vm->upcalls->initStackTraceElement->invokeIntSpecial(vm, newS, res,
                                                       &sourceName,
                                                       lineNumber,
                                                       &className,
                                                       &methodName, native);
  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMThrowable_getStackTrace(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* vmthrow, JavaObject* throwable) {

  ArrayObject* result = NULL;
  JavaObject* stack = NULL;
  llvm_gcroot(vmthrow, 0);
  llvm_gcroot(throwable, 0);
  llvm_gcroot(result, 0);
  llvm_gcroot(stack, 0);

  BEGIN_NATIVE_EXCEPTION(0)
  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaField* field = vm->upcalls->vmDataVMThrowable;
  stack = field->getInstanceObjectField(vmthrow);
  
  // remove the VMThrowable.fillInStackTrace method and the last method
  // on the stack.
  sint32 index = 2;;
  while (index != JavaArray::getSize(stack)) {
    mvm::FrameInfo* FI = vm->IPToFrameInfo(ArrayPtr::getElement((ArrayPtr*)stack, index));
    if (FI->Metadata == NULL) ++index;
    else {
      JavaMethod* meth = (JavaMethod*)FI->Metadata;
      assert(meth && "Wrong stack trace");
      if (meth->classDef->isAssignableFrom(vm->upcalls->newThrowable)) {
        ++index;
      } else break;
    }
  }
  
  sint32 size = 0;
  sint32 cur = index;
  while (cur < JavaArray::getSize(stack)) {
    mvm::FrameInfo* FI = vm->IPToFrameInfo(ArrayPtr::getElement((ArrayPtr*)stack, cur));
    ++cur;
    if (FI->Metadata != NULL) ++size;
  }

  result = (ArrayObject*)
    vm->upcalls->stackTraceArray->doNew(size, vm);
  
  cur = 0;
  for (sint32 i = index; i < JavaArray::getSize(stack); ++i) {
    mvm::FrameInfo* FI = vm->IPToFrameInfo(ArrayPtr::getElement((ArrayPtr*)stack, i));
    if (FI->Metadata != NULL) {
      ArrayObject::setElement(result, consStackElement(FI, ArrayPtr::getElement((ArrayPtr*)stack, i)), cur);
      cur++;
    }
  }
  
  END_NATIVE_EXCEPTION

  return result;
}

}
