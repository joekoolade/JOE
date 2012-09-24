//===- ClasspathConstructor.cpp -------------------------------------------===//
//===----------- GNU classpath java/lang/reflect/Constructor --------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "types.h"

#include "Classpath.h"
#include "ClasspathReflect.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "JnjvmClassLoader.h"

using namespace j3;

extern "C" {

JNIEXPORT jobject JNICALL Java_java_lang_reflect_Constructor_getParameterTypes(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectConstructor* cons) {

  JavaObject* res = 0;

  llvm_gcroot(cons, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(cons);
  UserClass* cl = JavaObjectConstructor::getClass(cons);
  JavaMethod* meth = JavaObjectConstructor::getInternalMethod(cons);
  JnjvmClassLoader* loader = cl->classLoader;

  res = meth->getParameterTypes(loader);
  END_NATIVE_EXCEPTION

  return (jobject)res;
}

JNIEXPORT jint JNICALL Java_java_lang_reflect_Constructor_getModifiersInternal(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectConstructor* cons) {

  llvm_gcroot(cons, 0);
  jint res = 0;

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(cons);
  JavaMethod* meth = JavaObjectConstructor::getInternalMethod(cons);
  res = meth->access;

  END_NATIVE_EXCEPTION

  return res;
}

JavaObject* proceedConstructor(JavaObjectConstructor* cons,
                               ArrayObject* args,
                               JavaObject* Clazz, jint index)
  __attribute__ ((noinline));

JavaObject* proceedConstructor(JavaObjectConstructor* cons,
                               ArrayObject* args,
                               JavaObject* Clazz, jint index) {
  JavaObject* res = 0;
  JavaObject* excp = 0;

  llvm_gcroot(cons, 0);
  llvm_gcroot(args, 0);
  llvm_gcroot(Clazz, 0);
  llvm_gcroot(res, 0);
  llvm_gcroot(excp, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaMethod* meth = JavaObjectConstructor::getInternalMethod(cons);
  UserClass* cl = JavaObjectConstructor::getClass(cons);
  sint32 nbArgs = args ? ArrayObject::getSize(args) : 0;
  Signdef* sign = meth->getSignature();
  sint32 size = sign->nbArguments;

  if (isAbstract(cl->access)) vm->instantiationException(cl);

  mvm::ThreadAllocator allocator;
  // Allocate a buffer to store the arguments.
  jvalue* buf = size ?
      (jvalue*)allocator.Allocate(size * sizeof(jvalue)) : NULL;

  if (nbArgs == size) {
    UserCommonClass* _cl = UserCommonClass::resolvedImplClass(vm, Clazz, false);
    UserClass* cl = _cl->asClass();
    if (cl) {
      cl->initialiseClass(vm);
      res = cl->doNew(vm);
      JavaObject** ptr = (JavaObject**)ArrayObject::getElements(args);

      Typedef* const* arguments = sign->getArgumentsType();
      // Store the arguments, unboxing primitives if necessary.
      for (sint32 i = 0; i < size; ++i) {
        JavaObject::decapsulePrimitive(ptr[i], vm, &buf[i], arguments[i]);
        if (!arguments[i]->isPrimitive()) {
          buf[i].l = reinterpret_cast<jobject>(&ptr[i]);
        }
      }

      JavaThread* th = JavaThread::get();
      TRY {
        meth->invokeIntSpecialBuf(vm, cl, res, buf);
      } CATCH {
        excp = th->getJavaException();
      } END_CATCH;
      if (excp) {
        if (JavaObject::getClass(excp)->isAssignableFrom(vm->upcalls->newException)) {
          th->clearException();
          // If it's an exception, we encapsule it in an
          // invocationTargetException
          vm->invocationTargetException(excp);
        } else {
          // If it's an error, throw it again.
          th->throwPendingException();
        }
        return NULL;
      }
    } else {
      vm->illegalArgumentException("class is not a regular class");
      return NULL;
    }
  } else {
    vm->illegalArgumentException("wrong number of arguments");
    return NULL;
  }

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Constructor_constructNative(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectConstructor* cons, ArrayObject* args, JavaObject* Clazz, jint index) {

  JavaObject* res = 0;

  llvm_gcroot(res, 0);
  llvm_gcroot(cons, 0);
  llvm_gcroot(args, 0);
  llvm_gcroot(Clazz, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  res = proceedConstructor(cons, args, Clazz, index);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT
ArrayObject* JNICALL Java_java_lang_reflect_Constructor_getExceptionTypes(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectConstructor* cons) {

  ArrayObject* res = 0;

  llvm_gcroot(res, 0);
  llvm_gcroot(cons, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(cons);
  UserClass* cl = JavaObjectConstructor::getClass(cons);
  JavaMethod* meth = JavaObjectConstructor::getInternalMethod(cons);
  JnjvmClassLoader* loader = cl->classLoader;

  res = (ArrayObject*)meth->getExceptionTypes(loader);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Constructor_getSignature(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectConstructor* Meth) {

  JavaObject* result = 0;

  llvm_gcroot(result, 0);
  llvm_gcroot(Meth, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(Meth);
  JavaMethod* meth = JavaObjectConstructor::getInternalMethod(Meth);
  Jnjvm* vm = JavaThread::get()->getJVM();
  result = vm->internalUTF8ToStr(meth->type);

  END_NATIVE_EXCEPTION

  return result;
}


}
