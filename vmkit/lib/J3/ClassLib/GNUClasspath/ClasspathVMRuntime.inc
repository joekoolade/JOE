//===------ ClasspathVMRuntime.cpp - GNU classpath java/lang/VMRuntime ----===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "MvmGC.h"

#include "types.h"

#include "Classpath.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

#include <cstring>
#include <unistd.h>

using namespace j3;

extern "C" {


JNIEXPORT JavaObject* JNICALL Java_java_lang_VMRuntime_mapLibraryName(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaString* strLib) {
  
  JavaObject* res = 0;
  ArrayUInt16* array = 0;
  const ArrayUInt16* utf8Lib = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(array, 0);
  llvm_gcroot(utf8Lib, 0);
  llvm_gcroot(strLib, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();

  utf8Lib = JavaString::getValue(strLib);
  uint32 stLib = strLib->offset;
  sint32 lgLib = strLib->count;
  sint32 lgPre = vm->bootstrapLoader->prelib->size;
  sint32 lgPost = vm->bootstrapLoader->postlib->size;
  
  uint32 size = (uint32)(lgPre + lgLib + lgPost);
  array = (ArrayUInt16*)vm->upcalls->ArrayOfChar->doNew(size, vm);
  uint16* elements = ArrayUInt16::getElements(array);

  memmove(elements, vm->bootstrapLoader->prelib->elements,
          lgPre * sizeof(uint16));
  memmove(&(elements[lgPre]), ArrayUInt16::getElements(utf8Lib) + stLib,
          lgLib * sizeof(uint16));
  memmove(&(elements[lgPre + lgLib]), vm->bootstrapLoader->postlib->elements,
           lgPost * sizeof(uint16));
  
  res = vm->constructString(array);

  END_NATIVE_EXCEPTION

  return res;
  
}

typedef int (*onLoad_t)(const void**, void*);
extern "C" void  j3EndJNI(uint32** old);
extern "C" void  j3StartJNI(uint32* num, uint32** old, mvm::KnownFrame* Frame);

extern "C" void callOnLoad(void* res, JnjvmClassLoader* loader, Jnjvm* vm)
  __attribute__ ((noinline));

extern "C" int doCall(onLoad_t func, void* res, Jnjvm* vm)
  __attribute__ ((noinline));

extern "C" int doCall(onLoad_t func, void* res, Jnjvm* vm) {
  func(&vm->javavmEnv, res);
  return 1;
}

// Calls the JNI_OnLoad function of a dynamic library.
extern "C" void callOnLoad(void* res, JnjvmClassLoader* loader, Jnjvm* vm) {

  onLoad_t onLoad = (onLoad_t)loader->loadInLib("JNI_OnLoad", res);
  
  if (onLoad) {
    uint32 num = 0;
    uint32* old = 0;
    mvm::KnownFrame Frame;
    
    j3StartJNI(&num, &old, &Frame);
    doCall(onLoad, res, vm);
    j3EndJNI(&old);
  }
}

// Never throws.
JNIEXPORT jint JNICALL Java_java_lang_VMRuntime_nativeLoad(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaString* str,
JavaObject* javaLoader) {
  
  
  llvm_gcroot(str, 0);
  llvm_gcroot(javaLoader, 0);

  void* res = 0;
  
  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  JnjvmClassLoader* loader = 
    JnjvmClassLoader::getJnjvmLoaderFromJavaObject(javaLoader, vm);

  mvm::ThreadAllocator allocator;
  char* buf = JavaString::strToAsciiz(str, &allocator);
  
  res = loader->loadLib(buf);
 
  if (res) callOnLoad(res, loader, vm);

  END_NATIVE_EXCEPTION

  return res != 0;
}


JNIEXPORT void JNICALL Java_java_lang_VMRuntime_gc(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  BEGIN_NATIVE_EXCEPTION(0)
  
  mvm::Collector::collect();

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_VMRuntime_runFinalization(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  mvm::Collector::collect();
  // Sleep a bit.
  sleep(1);
  return;
}

JNIEXPORT void JNICALL Java_java_lang_VMRuntime_runFinalizationForExit(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  return;
}

JNIEXPORT void JNICALL Java_java_lang_VMRuntime_runFinalizersOnExit(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
uint8 value
) {
  return;
}

JNIEXPORT void JNICALL Java_java_lang_VMRuntime_exit(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
jint par1) {
  mvm::System::Exit(par1);
}

JNIEXPORT jlong Java_java_lang_VMRuntime_freeMemory(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  return (jlong)mvm::Collector::getFreeMemory();
}

JNIEXPORT jlong Java_java_lang_VMRuntime_totalMemory(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  return (jlong)mvm::Collector::getTotalMemory();
}

JNIEXPORT jlong Java_java_lang_VMRuntime_maxMemory(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  return (jlong)mvm::Collector::getMaxMemory();
}

JNIEXPORT jint Java_java_lang_VMRuntime_availableProcessors(){
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
  return mvm::System::GetNumberOfProcessors();
}
}

