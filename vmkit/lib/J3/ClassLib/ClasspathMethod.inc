//===- ClasspathMethod.cpp ------------------------------------------------===//
//===------------- GNU classpath java/lang/reflect/Method -----------------===//
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
#include "JavaTypes.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "JnjvmClassLoader.h"

using namespace j3;

extern "C" {

JNIEXPORT jint JNICALL Java_java_lang_reflect_Method_getModifiersInternal(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectMethod* Meth) {

  jint res = 0;
  llvm_gcroot(Meth, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaMethod* meth = JavaObjectMethod::getInternalMethod(Meth);
  res = meth->access;

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Method_getReturnType(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectMethod* Meth) {

  JavaObject* res = 0;
  llvm_gcroot(Meth, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  UserClass* cl = JavaObjectMethod::getClass(Meth);
  JavaMethod* meth = JavaObjectMethod::getInternalMethod(Meth);
  JnjvmClassLoader* loader = cl->classLoader;
  res = meth->getReturnType(loader);

  END_NATIVE_EXCEPTION

  return res;
}


JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Method_getParameterTypes(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectMethod* Meth) {

  JavaObject* res = 0;
  llvm_gcroot(Meth, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  UserClass* cl = JavaObjectMethod::getClass(Meth);
  JavaMethod* meth = JavaObjectMethod::getInternalMethod(Meth);
  JnjvmClassLoader* loader = cl->classLoader;

  res = meth->getParameterTypes(loader);

  END_NATIVE_EXCEPTION

  return res;
}

JavaObject* proceedMethod(JavaObjectMethod* Meth, JavaObject* obj,
                          ArrayObject* args, JavaObject* Cl, jint index)
  __attribute__((noinline));

JavaObject* proceedMethod(JavaObjectMethod* Meth, JavaObject* obj,
                          ArrayObject* args, JavaObject* Cl, jint index) {

  JavaObject* res = 0;
  JavaObject* exc = 0;

  llvm_gcroot(res, 0);
  llvm_gcroot(Meth, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(args, 0);
  llvm_gcroot(Cl, 0);
  llvm_gcroot(exc, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();

  JavaMethod* meth = JavaObjectMethod::getInternalMethod(Meth);

  sint32 nbArgs = args ? ArrayObject::getSize(args) : 0;
  Signdef* sign = meth->getSignature();
  sint32 size = sign->nbArguments;

  mvm::ThreadAllocator allocator;
  jvalue* buf = size ?
      (jvalue*)allocator.Allocate(size * sizeof(jvalue)) : NULL;

  if (nbArgs == size) {
    UserCommonClass* _cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
    UserClass* cl = (UserClass*)_cl;

    if (isVirtual(meth->access)) {
      verifyNull(obj);
      UserCommonClass* objCl = JavaObject::getClass(obj);
      if (!(objCl->isAssignableFrom(cl))) {
        vm->illegalArgumentException("<this> is not a valid type");
      }

      if (isInterface(cl->access)) {
        cl->initialiseClass(vm);
        UserClass* methodCl = 0;
        UserClass* lookup = objCl->isArray() ? objCl->super : objCl->asClass();
        meth = lookup->lookupMethod(meth->name, meth->type, false, true,
                                    &methodCl);
      }
    } else {
      cl->initialiseClass(vm);
    }

    JavaObject** ptr = ArrayObject::getElements(args);
    Typedef* const* arguments = sign->getArgumentsType();
    for (sint32 i = 0; i < size; ++i) {
      JavaObject::decapsulePrimitive(ptr[i], vm, &buf[i], arguments[i]);
      if (!arguments[i]->isPrimitive()) {
        buf[i].l = reinterpret_cast<jobject>(&ptr[i]);
      }
    }

    JavaThread* th = JavaThread::get();

#define RUN_METH(TYPE, VAR)                                                    \
    TRY {                                                                      \
      if (isVirtual(meth->access)) {                                           \
        if (isPublic(meth->access) && !isFinal(meth->access) &&                \
            !isFinal(meth->classDef->access)) {                                \
          VAR = meth->invoke##TYPE##VirtualBuf(vm, cl, obj, buf);              \
        } else {                                                               \
          VAR = meth->invoke##TYPE##SpecialBuf(vm, cl, obj, buf);              \
        }                                                                      \
      } else {                                                                 \
        VAR = meth->invoke##TYPE##StaticBuf(vm, cl, buf);                      \
      }                                                                        \
    } CATCH {                                                                  \
      exc = th->getJavaException();                                            \
    } END_CATCH;                                                               \
    if (exc) {                                                                 \
      th->clearException();                                                    \
      th->getJVM()->invocationTargetException(exc);                            \
      return NULL;                                                             \
    }

    Typedef* retType = sign->getReturnType();
    if (retType->isPrimitive()) {
      PrimitiveTypedef* prim = (PrimitiveTypedef*)retType;
      if (prim->isVoid()) {
        res = 0;
        uint32 val = 0;
        RUN_METH(Int, val);
      } else if (prim->isBool()) {
        uint32 val = 0;
        RUN_METH(Int, val);
        res = vm->upcalls->boolClass->doNew(vm);
        vm->upcalls->boolValue->setInstanceInt8Field(res, val);
      } else if (prim->isByte()) {
        uint32 val = 0;
        RUN_METH(Int, val);
        res = vm->upcalls->byteClass->doNew(vm);
        vm->upcalls->byteValue->setInstanceInt8Field(res, val);
      } else if (prim->isChar()) {
        uint32 val = 0;
        RUN_METH(Int, val);
        res = vm->upcalls->charClass->doNew(vm);
        vm->upcalls->charValue->setInstanceInt16Field(res, val);
      } else if (prim->isShort()) {
        uint32 val = 0;
        RUN_METH(Int, val);
        res = vm->upcalls->shortClass->doNew(vm);
        vm->upcalls->shortValue->setInstanceInt16Field(res, val);
      } else if (prim->isInt()) {
        uint32 val = 0;
        RUN_METH(Int, val);
        res = vm->upcalls->intClass->doNew(vm);
        vm->upcalls->intValue->setInstanceInt32Field(res, val);
      } else if (prim->isLong()) {
        sint64 val = 0;
        RUN_METH(Long, val);
        res = vm->upcalls->longClass->doNew(vm);
        vm->upcalls->longValue->setInstanceLongField(res, val);
      } else if (prim->isFloat()) {
        float val = 0;
        RUN_METH(Float, val);
        res = vm->upcalls->floatClass->doNew(vm);
        vm->upcalls->floatValue->setInstanceFloatField(res, val);
      } else if (prim->isDouble()) {
        double val = 0;
        RUN_METH(Double, val);
        res = vm->upcalls->doubleClass->doNew(vm);
        vm->upcalls->doubleValue->setInstanceDoubleField(res, val);
      }
    } else {
      RUN_METH(JavaObject, res);
    }
  } else {
    vm->illegalArgumentException("wrong number of arguments");
    return NULL;
  }

  return res;
}

#undef RUN_METH

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Method_invokeNative(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectMethod* Meth, JavaObject* obj, ArrayObject* args, JavaObject* Cl, jint index) {

  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(Meth, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(args, 0);
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  res = proceedMethod(Meth, obj, args, Cl, index);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT ArrayObject* JNICALL Java_java_lang_reflect_Method_getExceptionTypes(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectMethod* Meth) {

  ArrayObject* res = 0;
  llvm_gcroot(Meth, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(Meth);
  UserClass* cl = JavaObjectMethod::getClass(Meth);
  JavaMethod* meth = JavaObjectMethod::getInternalMethod(Meth);
  JnjvmClassLoader* loader = cl->classLoader;
  res = meth->getExceptionTypes(loader);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Method_getSignature(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectMethod* Meth) {

  JavaObject* result = 0;
  llvm_gcroot(Meth, 0);
  llvm_gcroot(result, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(Meth);
  JavaMethod* meth = JavaObjectMethod::getInternalMethod(Meth);
  Jnjvm* vm = JavaThread::get()->getJVM();
  result = vm->internalUTF8ToStr(meth->type);

  END_NATIVE_EXCEPTION

  return result;
}

}
