//===------ ClasspathVMObject.cpp - GNU classpath java/lang/VMObject ------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "types.h"

#include "Classpath.h"
#include "Jnjvm.h"

using namespace j3;

extern "C" {

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMObject_clone(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* src) {
  
  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  llvm_gcroot(src, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  res = JavaObject::clone(src);

  END_NATIVE_EXCEPTION

  return res;
} 

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMObject_getClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* obj) {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  res = JavaObject::getClass(obj)->getClassDelegatee(vm);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT void JNICALL Java_java_lang_VMObject_notifyAll(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* obj) {

  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaObject::notifyAll(obj);

  END_NATIVE_EXCEPTION
}


JNIEXPORT void JNICALL Java_java_lang_VMObject_wait(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* obj, jlong ms, jint ns) {

  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaObject::wait(obj, ms, ns);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_VMObject_notify(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* obj) {

  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaObject::notify(obj);

  END_NATIVE_EXCEPTION
}

}
