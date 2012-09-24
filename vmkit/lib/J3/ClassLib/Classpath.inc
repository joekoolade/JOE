//===-------- Classpath.cpp - Configuration for classpath -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//



#include "Classpath.h"
#include "ClasspathReflect.h"
#include "JavaClass.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "Reader.h"
#include "VMStaticInstance.h"


using namespace j3;

extern "C" {

// Convert a 'base' JavaObject to its pointer representation.
// Handles our special VMStaticInstance wrapper.
static inline uint8 *baseToPtr(JavaObject *base) {
  if (VMStaticInstance::isVMStaticInstance(base))
    return (uint8*)((VMStaticInstance*)base)->getStaticInstance();
  else
    return (uint8*)base;
}

// Define hasClassInitializer because of a buggy implementation in Classpath.
JNIEXPORT bool JNICALL Java_java_io_VMObjectStreamClass_hasClassInitializer(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* Cl) {

  llvm_gcroot(Cl, 0);
  bool res = false;
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(Cl);
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, true);

  if (cl->isClass() &&
      cl->asClass()->lookupMethodDontThrow(vm->bootstrapLoader->clinitName,
                                           vm->bootstrapLoader->clinitType,
                                           true, false, 0))
  res = true;

  END_NATIVE_EXCEPTION

  return res;
}


// Redefine some VMObjectStreamClass functions because of a slow implementation
// in Classpath.

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setBooleanNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jboolean val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceInt8Field(obj, (uint8)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setByteNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jbyte val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceInt8Field(obj, (uint8)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setCharNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jchar val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceInt16Field((JavaObject*)obj, (uint16)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setShortNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jshort val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceInt16Field(obj, (sint16)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setIntNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jint val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceInt32Field(obj, (sint32)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setLongNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jlong val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceLongField(obj, (sint64)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setFloatNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jfloat val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceFloatField(obj, (float)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setDoubleNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, jdouble val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceDoubleField(obj, (double)val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_io_VMObjectStreamClass_setObjectNative(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectField* Field, JavaObject* obj, JavaObject* val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(val, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(obj);
  JavaField* field = JavaObjectField::getInternalField(Field);
  field->setInstanceObjectField(obj, val);

  END_NATIVE_EXCEPTION
}

JNIEXPORT JavaObject* JNICALL Java_java_io_VMObjectInputStream_allocateObject(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* target, JavaObject* constr, JavaObjectConstructor* cons) {

  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(target, 0);
  llvm_gcroot(constr, 0);
  llvm_gcroot(cons, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl =
    (UserClass*)UserCommonClass::resolvedImplClass(vm, target, true);
  res = cl->doNew(vm);
  JavaMethod* meth = JavaObjectConstructor::getInternalMethod(cons);
  meth->invokeIntSpecial(vm, cl, res);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_VMArray_createObjectArray(
#ifdef NATIVE_JNI
JNIEnv * env,
jclass thisClass,
#endif
JavaObject* arrayType, jint arrayLength) {

  JavaObject* res = 0;
  llvm_gcroot(arrayType, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* base =
    UserCommonClass::resolvedImplClass(vm, arrayType, true);
  JnjvmClassLoader* loader = base->classLoader;
  const UTF8* name = base->getName();
  // -1 because we're adding a new dimension in this method.
  const int kLimit = 255 - 1;
  const uint16* elements = name->elements;
  if (name->size > kLimit && elements[kLimit] == '[') {
    vm->illegalArgumentException("Too many dimensions for array");
  }
  const UTF8* arrayName = loader->constructArrayName(1, name);
  UserClassArray* array = loader->constructArray(arrayName, base);
  res = array->doNew(arrayLength, vm);

  END_NATIVE_EXCEPTION

  return res;
}


// Never throws.
JNIEXPORT
bool JNICALL Java_java_util_concurrent_atomic_AtomicLong_VMSupportsCS8(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  return false;
}

// Never throws.
JNIEXPORT bool JNICALL Java_sun_misc_Unsafe_compareAndSwapLong(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* unsafe, JavaObject* obj, jlong offset, jlong expect, jlong update) {

  llvm_gcroot(unsafe, 0);
  llvm_gcroot(obj, 0);
  jlong *ptr;
  jlong  value;

  ptr = (jlong *) (((uint8 *) obj) + offset);

  value = *ptr;

  if (value == expect) {
    *ptr = update;
    return true;
  } else {
    return false;
  }

}

// Never throws.
JNIEXPORT bool JNICALL Java_sun_misc_Unsafe_compareAndSwapInt(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* unsafe, JavaObject* obj, jlong offset, jint expect, jint update) {

  llvm_gcroot(unsafe, 0);
  llvm_gcroot(obj, 0);
  jint *ptr;

  ptr = (jint *) (((uint8 *) obj) + offset);

  return __sync_bool_compare_and_swap(ptr, expect, update);
}

// Never throws.
JNIEXPORT bool JNICALL Java_sun_misc_Unsafe_compareAndSwapObject(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* unsafe, JavaObject* obj, jlong offset, JavaObject* expect,
JavaObject* update) {
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(expect, 0);
  llvm_gcroot(update, 0);

  JavaObject** ptr = (JavaObject**) (((uint8 *) obj) + offset);

  return mvm::Collector::objectReferenceTryCASBarrier((gc*)obj, (gc**)ptr, (gc*)expect, (gc*)update);
}

// Never throws.
JNIEXPORT void JNICALL Java_sun_misc_Unsafe_putObjectVolatile(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* unsafe, JavaObject* obj, jlong offset, JavaObject* value) {
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(value, 0);

  JavaObject** ptr = (JavaObject**) (((uint8 *) obj) + offset);
  mvm::Collector::objectReferenceWriteBarrier((gc*)obj, (gc**)ptr, (gc*)value);
}

JNIEXPORT jlong JNICALL Java_sun_misc_Unsafe_allocateMemory(
JavaObject* unsafe, jlong size) {
  // TODO: Invalid size/OOM/etc handling!
  jlong res = 0;
  BEGIN_NATIVE_EXCEPTION(0)
  res = (jlong)malloc(size);
  END_NATIVE_EXCEPTION
  return res;
}

JNIEXPORT void JNICALL Java_sun_misc_Unsafe_freeMemory(
JavaObject* unsafe, jlong ptr) {
  // TODO: Exception handling...
  BEGIN_NATIVE_EXCEPTION(0)
  free((void*)ptr);
  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_sun_misc_Unsafe_putLong__JJ(
JavaObject* unsafe, jlong ptr, jlong value) {
  BEGIN_NATIVE_EXCEPTION(0)
  *(jlong*)ptr = value;
  END_NATIVE_EXCEPTION
}

JNIEXPORT jbyte JNICALL Java_sun_misc_Unsafe_getByte__J(
JavaObject* unsafe, jlong ptr) {
  jbyte res = 0;
  BEGIN_NATIVE_EXCEPTION(0)
  res =  *(jbyte*)ptr;
  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT void JNICALL Java_sun_misc_Unsafe_ensureClassInitialized(
JavaObject* unsafe, JavaObject* clazz) {
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(clazz, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();

  CommonClass * cl = JavaObject::getClass(clazz);
  assert(cl && cl->isClass());
  cl->asClass()->resolveClass();
  cl->asClass()->initialiseClass(vm);

  END_NATIVE_EXCEPTION;
}

JNIEXPORT jlong JNICALL Java_sun_misc_Unsafe_staticFieldOffset(
JavaObject* unsafe, JavaObjectField* _field) {
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(_field, 0);

  jlong res = 0;
  BEGIN_NATIVE_EXCEPTION(0)

  JavaField * field = JavaObjectField::getInternalField(_field);
  assert(field);

  res = field->ptrOffset;

  END_NATIVE_EXCEPTION;

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_sun_misc_Unsafe_staticFieldBase(
JavaObject* unsafe, JavaObjectField* _field) {
  JavaObject* res = 0;
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(_field, 0);
  llvm_gcroot(res, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  JavaField * field = JavaObjectField::getInternalField(_field);
  assert(field);
  field->classDef->initialiseClass(JavaThread::get()->getJVM());

  res = VMStaticInstance::allocate(field->classDef);

  END_NATIVE_EXCEPTION;

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_sun_misc_Unsafe_getObjectVolatile(
JavaObject* unsafe, JavaObject* base, jlong offset) {
  JavaObject * res = 0;
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(base, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)
  JavaObject** ptr = (JavaObject**) (baseToPtr(base) + offset);
  res = *ptr;
  END_NATIVE_EXCEPTION;

  return res;
}

JNIEXPORT jlong JNICALL Java_sun_misc_Unsafe_arrayBaseOffset(
JavaObject* unsafe, JavaObject* clazz) {
  // Array starts at beginning of object
  return 0;
}

JNIEXPORT jlong JNICALL Java_sun_misc_Unsafe_arrayIndexScale(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* unsafe, JavaObject* clazz) {
  // Return '0' if we don't support indexing this way.
  // (We might pack fields specially, etc)
  // TODO: Implement this for the array types we support this way
  return 0;
}

JNIEXPORT JavaObject* JNICALL Java_sun_misc_Unsafe_defineClass__Ljava_lang_String_2_3BIILjava_lang_ClassLoader_2Ljava_security_ProtectionDomain_2(
JavaObject* unsafe, JavaString *name, ArrayObject * bytesArr, jint off, jint len, JavaObject * loader, JavaObject * pd) {
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(name, 0);
  llvm_gcroot(bytesArr, 0);
  llvm_gcroot(loader, 0);
  llvm_gcroot(pd, 0);
  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  JnjvmClassLoader* JCL = NULL;
  JCL = JnjvmClassLoader::getJnjvmLoaderFromJavaObject(loader, vm);

  jint last = off + len;
  if (last < bytesArr->size) {
    assert(0 && "What exception to throw here?");
  }
  ClassBytes * bytes = new (JCL->allocator, len) ClassBytes(len);
  memcpy(bytes->elements, JavaArray::getElements(bytesArr)+off, len);
  const UTF8* utfName = JavaString::javaToInternal(name, JCL->hashUTF8);
  UserClass *cl = JCL->constructClass(utfName, bytes);

  if (cl) res = (JavaObject*)cl->getClassDelegatee(vm);

  END_NATIVE_EXCEPTION;

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_sun_misc_Unsafe_allocateInstance__Ljava_lang_Class_2(
JavaObject* unsafe, JavaObjectClass * clazz) {
  JavaObject* res = 0;
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();

  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);
  if (cl->isClass())
    res = cl->asClass()->doNew(vm);

  END_NATIVE_EXCEPTION;

  return res;
}


JNIEXPORT void JNICALL Java_sun_misc_Unsafe_throwException(
JavaObject* unsafe, JavaObject * obj) {
  llvm_gcroot(unsafe, 0);
  llvm_gcroot(obj, 0);

  JavaThread::get()->throwException(obj);
}

JNIEXPORT void JNICALL Java_sun_misc_Unsafe_registerNatives(JavaObject*) {
  // Nothing
}

// TODO: Add the Volatile variants
#define GET_PUT_OFFSET(Type,jtype,shorttype) \
JNIEXPORT jtype JNICALL Java_sun_misc_Unsafe_get ## Type ## __Ljava_lang_Object_2J( \
JavaObject* unsafe, JavaObject* base, jlong offset) { \
  jtype res = 0; \
  BEGIN_NATIVE_EXCEPTION(0) \
  jtype* ptr = (jtype*) (baseToPtr(base) + offset); \
  res = *ptr; \
  END_NATIVE_EXCEPTION \
 \
  return res; \
} \
 \
JNIEXPORT void JNICALL Java_sun_misc_Unsafe_put ## Type ## __Ljava_lang_Object_2J ## shorttype( \
JavaObject* unsafe, JavaObject* base, jlong offset, jtype val) { \
  BEGIN_NATIVE_EXCEPTION(0) \
  jtype* ptr = (jtype*) (baseToPtr(base) + offset); \
  *ptr = val; \
  END_NATIVE_EXCEPTION \
}

GET_PUT_OFFSET(Boolean,jboolean,Z)
GET_PUT_OFFSET(Byte,jbyte,B)
GET_PUT_OFFSET(Char,jchar,C)
GET_PUT_OFFSET(Short,jshort,S)
GET_PUT_OFFSET(Int,jint,I)
GET_PUT_OFFSET(Long,jlong,J)
GET_PUT_OFFSET(Float,jfloat,F)
GET_PUT_OFFSET(Double,jdouble,D)

}
