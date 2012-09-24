//===-------- JavaUpcalls.cpp - Upcalls to Java entities ------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "ClasspathReflect.h"
#include "JavaAccess.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "ReferenceQueue.h"

#define COMPILE_METHODS(cl) \
  for (CommonClass::method_iterator i = cl->virtualMethods.begin(), \
            e = cl->virtualMethods.end(); i!= e; ++i) { \
    i->second->compiledPtr(); \
  } \
  \
  for (CommonClass::method_iterator i = cl->staticMethods.begin(), \
            e = cl->staticMethods.end(); i!= e; ++i) { \
    i->second->compiledPtr(); \
  }


using namespace j3;

Class*      Classpath::newThread;
Class*      Classpath::newVMThread;
JavaField*  Classpath::assocThread;
JavaField*  Classpath::vmdataVMThread;
JavaMethod* Classpath::finaliseCreateInitialThread;
JavaMethod* Classpath::initVMThread;
JavaMethod* Classpath::initThread;
JavaMethod* Classpath::groupAddThread;
JavaField*  Classpath::threadName;
JavaField*  Classpath::groupName;
JavaMethod* Classpath::initGroup;
JavaField*  Classpath::priority;
JavaField*  Classpath::daemon;
JavaField*  Classpath::group;
JavaField*  Classpath::running;
Class*      Classpath::threadGroup;
JavaField*  Classpath::rootGroup;
JavaField*  Classpath::vmThread;
JavaMethod* Classpath::getUncaughtExceptionHandler;
JavaMethod* Classpath::uncaughtException;
Class*      Classpath::inheritableThreadLocal;

JavaMethod* Classpath::runVMThread;
JavaMethod* Classpath::setContextClassLoader;
JavaMethod* Classpath::getSystemClassLoader;
Class*      Classpath::newString;
Class*      Classpath::newClass;
Class*      Classpath::newThrowable;
Class*      Classpath::newException;
JavaMethod* Classpath::initClass;
JavaMethod* Classpath::initClassWithProtectionDomain;
JavaField*  Classpath::vmdataClass;
JavaMethod* Classpath::setProperty;
JavaMethod* Classpath::initString;
JavaMethod* Classpath::getCallingClassLoader;
JavaMethod* Classpath::initConstructor;
Class*      Classpath::newConstructor;
ClassArray* Classpath::constructorArrayClass;
ClassArray* Classpath::constructorArrayAnnotation;
JavaField*  Classpath::constructorSlot;
JavaMethod* Classpath::initMethod;
JavaMethod* Classpath::initField;
Class*      Classpath::newField;
Class*      Classpath::newMethod;
ClassArray* Classpath::methodArrayClass;
ClassArray* Classpath::fieldArrayClass;
JavaField*  Classpath::methodSlot;
JavaField*  Classpath::fieldSlot;
ClassArray* Classpath::classArrayClass;
JavaMethod* Classpath::loadInClassLoader;
JavaMethod* Classpath::initVMThrowable;
JavaField*  Classpath::vmDataVMThrowable;
Class*      Classpath::newVMThrowable;
JavaField*  Classpath::bufferAddress;
JavaField*  Classpath::dataPointer32;
JavaField*  Classpath::dataPointer64;
Class*      Classpath::newPointer32;
Class*      Classpath::newPointer64;
Class*      Classpath::newDirectByteBuffer;
JavaField*  Classpath::vmdataClassLoader;
JavaMethod* Classpath::InitDirectByteBuffer;
Class*      Classpath::newClassLoader;


JavaField*  Classpath::boolValue;
JavaField*  Classpath::byteValue;
JavaField*  Classpath::shortValue;
JavaField*  Classpath::charValue;
JavaField*  Classpath::intValue;
JavaField*  Classpath::longValue;
JavaField*  Classpath::floatValue;
JavaField*  Classpath::doubleValue;

Class*      Classpath::newStackTraceElement;
ClassArray* Classpath::stackTraceArray;
JavaMethod* Classpath::initStackTraceElement;

Class* Classpath::voidClass;
Class* Classpath::boolClass;
Class* Classpath::byteClass;
Class* Classpath::shortClass;
Class* Classpath::charClass;
Class* Classpath::intClass;
Class* Classpath::floatClass;
Class* Classpath::doubleClass;
Class* Classpath::longClass;

Class* Classpath::vmStackWalker;

Class* Classpath::InvocationTargetException;
Class* Classpath::ArrayStoreException;
Class* Classpath::ClassCastException;
Class* Classpath::IllegalMonitorStateException;
Class* Classpath::IllegalArgumentException;
Class* Classpath::InterruptedException;
Class* Classpath::IndexOutOfBoundsException;
Class* Classpath::ArrayIndexOutOfBoundsException;
Class* Classpath::NegativeArraySizeException;
Class* Classpath::NullPointerException;
Class* Classpath::SecurityException;
Class* Classpath::ClassFormatError;
Class* Classpath::ClassCircularityError;
Class* Classpath::NoClassDefFoundError;
Class* Classpath::UnsupportedClassVersionError;
Class* Classpath::NoSuchFieldError;
Class* Classpath::NoSuchMethodError;
Class* Classpath::InstantiationError;
Class* Classpath::InstantiationException;
Class* Classpath::IllegalAccessError;
Class* Classpath::IllegalAccessException;
Class* Classpath::VerifyError;
Class* Classpath::ExceptionInInitializerError;
Class* Classpath::LinkageError;
Class* Classpath::AbstractMethodError;
Class* Classpath::UnsatisfiedLinkError;
Class* Classpath::InternalError;
Class* Classpath::OutOfMemoryError;
Class* Classpath::StackOverflowError;
Class* Classpath::UnknownError;
Class* Classpath::ClassNotFoundException;
Class* Classpath::ArithmeticException;

JavaMethod* Classpath::InitInvocationTargetException;
JavaMethod* Classpath::InitArrayStoreException;
JavaMethod* Classpath::InitClassCastException;
JavaMethod* Classpath::InitIllegalMonitorStateException;
JavaMethod* Classpath::InitIllegalArgumentException;
JavaMethod* Classpath::InitInterruptedException;
JavaMethod* Classpath::InitIndexOutOfBoundsException;
JavaMethod* Classpath::InitArrayIndexOutOfBoundsException;
JavaMethod* Classpath::InitNegativeArraySizeException;
JavaMethod* Classpath::InitNullPointerException;
JavaMethod* Classpath::InitSecurityException;
JavaMethod* Classpath::InitClassFormatError;
JavaMethod* Classpath::InitClassCircularityError;
JavaMethod* Classpath::InitNoClassDefFoundError;
JavaMethod* Classpath::InitUnsupportedClassVersionError;
JavaMethod* Classpath::InitNoSuchFieldError;
JavaMethod* Classpath::InitNoSuchMethodError;
JavaMethod* Classpath::InitInstantiationError;
JavaMethod* Classpath::InitInstantiationException;
JavaMethod* Classpath::InitIllegalAccessError;
JavaMethod* Classpath::InitIllegalAccessException;
JavaMethod* Classpath::InitVerifyError;
JavaMethod* Classpath::InitExceptionInInitializerError;
JavaMethod* Classpath::InitLinkageError;
JavaMethod* Classpath::InitAbstractMethodError;
JavaMethod* Classpath::InitUnsatisfiedLinkError;
JavaMethod* Classpath::InitInternalError;
JavaMethod* Classpath::InitOutOfMemoryError;
JavaMethod* Classpath::InitStackOverflowError;
JavaMethod* Classpath::InitUnknownError;
JavaMethod* Classpath::InitClassNotFoundException;
JavaMethod* Classpath::InitArithmeticException;
JavaMethod* Classpath::InitObject;
JavaMethod* Classpath::FinalizeObject;
JavaMethod* Classpath::IntToString;

JavaMethod* Classpath::SystemArraycopy;
JavaMethod* Classpath::VMSystemArraycopy;
Class*      Classpath::SystemClass;
Class*      Classpath::EnumClass;

JavaMethod* Classpath::ErrorWithExcpNoClassDefFoundError;
JavaMethod* Classpath::ErrorWithExcpExceptionInInitializerError;
JavaMethod* Classpath::ErrorWithExcpInvocationTargetException;

ClassArray* Classpath::ArrayOfByte;
ClassArray* Classpath::ArrayOfChar;
ClassArray* Classpath::ArrayOfString;
ClassArray* Classpath::ArrayOfInt;
ClassArray* Classpath::ArrayOfShort;
ClassArray* Classpath::ArrayOfBool;
ClassArray* Classpath::ArrayOfLong;
ClassArray* Classpath::ArrayOfFloat;
ClassArray* Classpath::ArrayOfDouble;
ClassArray* Classpath::ArrayOfObject;

ClassPrimitive* Classpath::OfByte;
ClassPrimitive* Classpath::OfChar;
ClassPrimitive* Classpath::OfInt;
ClassPrimitive* Classpath::OfShort;
ClassPrimitive* Classpath::OfBool;
ClassPrimitive* Classpath::OfLong;
ClassPrimitive* Classpath::OfFloat;
ClassPrimitive* Classpath::OfDouble;
ClassPrimitive* Classpath::OfVoid;

Class* Classpath::OfObject;

JavaField* Classpath::methodClass;
JavaField* Classpath::fieldClass;
JavaField* Classpath::constructorClass;

JavaMethod* Classpath::EnqueueReference;
Class*      Classpath::newReference;

void Classpath::CreateJavaThread(Jnjvm* vm, JavaThread* myth,
                                 const char* thName, JavaObject* Group) {
  JavaObjectVMThread* vmth = NULL;
  JavaObject* th = NULL;
  JavaObject* name = NULL;
  llvm_gcroot(Group, 0);
  llvm_gcroot(vmth, 0);
  llvm_gcroot(th, 0);
  llvm_gcroot(name, 0);

  th = newThread->doNew(vm);
  myth->javaThread = th;
  vmth = (JavaObjectVMThread*)newVMThread->doNew(vm);
  name = vm->asciizToStr(thName);

  initThread->invokeIntSpecial(vm, newThread, th, &vmth, &name, 1, 0); 
  vmThread->setInstanceObjectField(th, vmth);
  assocThread->setInstanceObjectField(vmth, th);
  running->setInstanceInt8Field(vmth, (uint32)1);
  JavaObjectVMThread::setVmdata(vmth, myth);
  
  group->setInstanceObjectField(th, Group);
  groupAddThread->invokeIntSpecial(vm, threadGroup, Group, &th);
  
  finaliseCreateInitialThread->invokeIntStatic(vm, inheritableThreadLocal, &th);
}

void Classpath::InitializeThreading(Jnjvm* vm) {

  JavaObject* RG = 0;
  JavaObject* SystemGroup = 0;
  JavaObject* systemName = 0;
  llvm_gcroot(RG, 0);
  llvm_gcroot(SystemGroup, 0);
  llvm_gcroot(systemName, 0);

  // Resolve and initialize classes first.
  newThread->resolveClass();
  newThread->initialiseClass(vm);
  
  newVMThread->resolveClass();
  newVMThread->initialiseClass(vm);
  
  threadGroup->resolveClass();
  threadGroup->initialiseClass(vm);

  // Create the main thread
  RG = rootGroup->getStaticObjectField();
  assert(RG && "No root group");
  assert(vm->getMainThread() && "VM did not set its main thread");
  CreateJavaThread(vm, (JavaThread*)vm->getMainThread(), "main", RG);

  // Create the "system" group.
  SystemGroup = threadGroup->doNew(vm);
  initGroup->invokeIntSpecial(vm, threadGroup, SystemGroup);
  systemName = vm->asciizToStr("system");
  groupName->setInstanceObjectField(SystemGroup, systemName);

  // Create the finalizer thread.
  assert(vm->getFinalizerThread() && "VM did not set its finalizer thread");
  CreateJavaThread(vm, vm->getFinalizerThread(), "Finalizer", SystemGroup);
  
  // Create the enqueue thread.
  assert(vm->getReferenceThread() && "VM did not set its enqueue thread");
  CreateJavaThread(vm, vm->getReferenceThread(), "Reference", SystemGroup);
}

extern "C" void Java_java_lang_ref_WeakReference__0003Cinit_0003E__Ljava_lang_Object_2(
    JavaObjectReference* reference, JavaObject* referent) {
  llvm_gcroot(reference, 0);
  llvm_gcroot(referent, 0);

  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaObjectReference::init(reference, referent, 0);
  JavaThread::get()->getJVM()->getReferenceThread()->addWeakReference(reference);

  END_NATIVE_EXCEPTION

}

extern "C" void Java_java_lang_ref_WeakReference__0003Cinit_0003E__Ljava_lang_Object_2Ljava_lang_ref_ReferenceQueue_2(
    JavaObjectReference* reference,
    JavaObject* referent,
    JavaObject* queue) {
  llvm_gcroot(reference, 0);
  llvm_gcroot(referent, 0);
  llvm_gcroot(queue, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaObjectReference::init(reference, referent, queue);
  JavaThread::get()->getJVM()->getReferenceThread()->addWeakReference(reference);
  
  END_NATIVE_EXCEPTION

}

extern "C" void Java_java_lang_ref_SoftReference__0003Cinit_0003E__Ljava_lang_Object_2(
    JavaObjectReference* reference, JavaObject* referent) {
  llvm_gcroot(reference, 0);
  llvm_gcroot(referent, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaObjectReference::init(reference, referent, 0);
  JavaThread::get()->getJVM()->getReferenceThread()->addSoftReference(reference);
  
  END_NATIVE_EXCEPTION

}

extern "C" void Java_java_lang_ref_SoftReference__0003Cinit_0003E__Ljava_lang_Object_2Ljava_lang_ref_ReferenceQueue_2(
    JavaObjectReference* reference,
    JavaObject* referent,
    JavaObject* queue) {
  llvm_gcroot(reference, 0);
  llvm_gcroot(referent, 0);
  llvm_gcroot(queue, 0);
 
  BEGIN_NATIVE_EXCEPTION(0)

  JavaObjectReference::init(reference, referent, queue);
  JavaThread::get()->getJVM()->getReferenceThread()->addSoftReference(reference);
  
  END_NATIVE_EXCEPTION

}

extern "C" void Java_java_lang_ref_PhantomReference__0003Cinit_0003E__Ljava_lang_Object_2Ljava_lang_ref_ReferenceQueue_2(
    JavaObjectReference* reference,
    JavaObject* referent,
    JavaObject* queue) {
  llvm_gcroot(reference, 0);
  llvm_gcroot(referent, 0);
  llvm_gcroot(queue, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaObjectReference::init(reference, referent, queue);
  JavaThread::get()->getJVM()->getReferenceThread()->addPhantomReference(reference);

  END_NATIVE_EXCEPTION
}

extern "C" JavaString* Java_java_lang_VMString_intern__Ljava_lang_String_2(
    JavaString* obj) {
  const ArrayUInt16* array = 0;
  JavaString* res = 0;
  llvm_gcroot(obj, 0);
  llvm_gcroot(array, 0);
  llvm_gcroot(res, 0);
  // If the string is already interned, just return.
  if (obj->getVirtualTable() == JavaString::internStringVT) return obj;
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  array = JavaString::strToArray(obj, vm);
  res = vm->constructString(array);
  
  END_NATIVE_EXCEPTION

  return res;
}

extern "C" uint8 Java_java_lang_Class_isArray__(JavaObjectClass* klass) {
  llvm_gcroot(klass, 0);
  UserCommonClass* cl = 0;

  BEGIN_NATIVE_EXCEPTION(0)

  cl = JavaObjectClass::getClass(klass);
  
  END_NATIVE_EXCEPTION
  
  return (uint8)cl->isArray();
}

extern "C" JavaObject* Java_gnu_classpath_VMStackWalker_getCallingClass__() {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaThread* th = JavaThread::get();
  UserClass* cl = th->getCallingClassLevel(2);
  if (cl != NULL) res = cl->getClassDelegatee(th->getJVM());
  
  END_NATIVE_EXCEPTION

  return res;
}

extern "C" JavaObject* Java_gnu_classpath_VMStackWalker_getCallingClassLoader__() {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaThread* th = JavaThread::get();
  UserClass* cl = th->getCallingClassLevel(2);
  res = cl->classLoader->getJavaClassLoader();  
  
  END_NATIVE_EXCEPTION

  return res;
}

extern "C" JavaObject* Java_gnu_classpath_VMStackWalker_firstNonNullClassLoader__() {
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaThread* th = JavaThread::get();
  res = th->getNonNullClassLoader();
  
  END_NATIVE_EXCEPTION

  return res;
}

extern "C" JavaObject* Java_sun_reflect_Reflection_getCallerClass__I(uint32 index) {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  UserClass* cl = th->getCallingClassLevel(index);
  if (cl) res = cl->getClassDelegatee(vm);
  
  END_NATIVE_EXCEPTION

  return res;
}

extern "C" JavaObject* Java_java_lang_reflect_AccessibleObject_getAnnotation__Ljava_lang_Class_2(
    JavaObject* obj) {
  llvm_gcroot(obj, 0);
  return 0;
}

extern "C" JavaObject* Java_java_lang_reflect_AccessibleObject_getDeclaredAnnotations__() {
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClassArray* array = vm->upcalls->constructorArrayAnnotation;
  res = array->doNew(0, vm);

  END_NATIVE_EXCEPTION

  return res;
}

extern "C" void nativeJavaObjectClassTracer(
    JavaObjectClass* obj, word_t closure) {
  JavaObjectClass::staticTracer(obj, closure);
}

extern "C" void nativeJavaObjectFieldTracer(
    JavaObjectField* obj, word_t closure) {
  JavaObjectField::staticTracer(obj, closure);
}

extern "C" void nativeJavaObjectMethodTracer(
    JavaObjectMethod* obj, word_t closure) {
  JavaObjectMethod::staticTracer(obj, closure);
}

extern "C" void nativeJavaObjectConstructorTracer(
    JavaObjectConstructor* obj, word_t closure) {
  JavaObjectConstructor::staticTracer(obj, closure);
}

extern "C" void nativeJavaObjectVMThreadTracer(
    JavaObjectVMThread* obj, word_t closure) {
  JavaObjectVMThread::staticTracer(obj, closure);
}

extern "C" JavaString* Java_java_lang_VMSystem_getenv__Ljava_lang_String_2(JavaString* str) {
  JavaString* ret = 0;
  llvm_gcroot(str, 0);
  llvm_gcroot(ret, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)

  mvm::ThreadAllocator allocator;
  char* buf = JavaString::strToAsciiz(str, &allocator);
  char* res = getenv(buf);
  if (res) {
    Jnjvm* vm = JavaThread::get()->getJVM();
    ret = vm->asciizToStr(res);
  }
  
  END_NATIVE_EXCEPTION

  return ret;
}

void Classpath::initialiseClasspath(JnjvmClassLoader* loader) {

  newClassLoader = 
    UPCALL_CLASS(loader, "java/lang/ClassLoader");
  
  getSystemClassLoader =
    UPCALL_METHOD(loader, "java/lang/ClassLoader", "getSystemClassLoader",
                  "()Ljava/lang/ClassLoader;", ACC_STATIC);

  setContextClassLoader =
    UPCALL_METHOD(loader, "java/lang/Thread", "setContextClassLoader",
                  "(Ljava/lang/ClassLoader;)V", ACC_VIRTUAL);

  newString = 
    UPCALL_CLASS(loader, "java/lang/String");
  
  newClass =
    UPCALL_CLASS(loader, "java/lang/Class");
  
  newThrowable =
    UPCALL_CLASS(loader, "java/lang/Throwable");
  
  newException =
    UPCALL_CLASS(loader, "java/lang/Exception");

  newPointer32 = 
    UPCALL_CLASS(loader, "gnu/classpath/Pointer32");
  
  newPointer64 = 
    UPCALL_CLASS(loader, "gnu/classpath/Pointer64");
 
  newDirectByteBuffer =
    UPCALL_CLASS(loader, "java/nio/DirectByteBufferImpl$ReadWrite");

  InitDirectByteBuffer =
    UPCALL_METHOD(loader, "java/nio/DirectByteBufferImpl$ReadWrite", "<init>",
                  "(Ljava/lang/Object;Lgnu/classpath/Pointer;III)V",
                  ACC_VIRTUAL);

  initClass =
    UPCALL_METHOD(loader, "java/lang/Class", "<init>", "(Ljava/lang/Object;)V",
                  ACC_VIRTUAL);

  initClassWithProtectionDomain =
    UPCALL_METHOD(loader, "java/lang/Class", "<init>",
                  "(Ljava/lang/Object;Ljava/security/ProtectionDomain;)V",
                  ACC_VIRTUAL);

  vmdataClass =
    UPCALL_FIELD(loader, "java/lang/Class", "vmdata", "Ljava/lang/Object;",
                 ACC_VIRTUAL);
  
  setProperty = 
    UPCALL_METHOD(loader, "java/util/Properties", "setProperty",
                  "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
                  ACC_VIRTUAL);

  initString =
    UPCALL_METHOD(loader, "java/lang/String", "<init>", "([CIIZ)V", ACC_VIRTUAL);
  
  initConstructor =
    UPCALL_METHOD(loader, "java/lang/reflect/Constructor", "<init>",
                  "(Ljava/lang/Class;I)V", ACC_VIRTUAL);

  newConstructor =
    UPCALL_CLASS(loader, "java/lang/reflect/Constructor");

  constructorArrayClass =
    UPCALL_ARRAY_CLASS(loader, "java/lang/reflect/Constructor", 1);
  
  constructorArrayAnnotation =
    UPCALL_ARRAY_CLASS(loader, "java/lang/annotation/Annotation", 1);

  constructorSlot =
    UPCALL_FIELD(loader, "java/lang/reflect/Constructor", "slot", "I", ACC_VIRTUAL);
  
  initMethod =
    UPCALL_METHOD(loader, "java/lang/reflect/Method", "<init>",
                  "(Ljava/lang/Class;Ljava/lang/String;I)V", ACC_VIRTUAL);

  newMethod =
    UPCALL_CLASS(loader, "java/lang/reflect/Method");

  methodArrayClass =
    UPCALL_ARRAY_CLASS(loader, "java/lang/reflect/Method", 1);

  methodSlot =
    UPCALL_FIELD(loader, "java/lang/reflect/Method", "slot", "I", ACC_VIRTUAL);
  
  initField =
    UPCALL_METHOD(loader, "java/lang/reflect/Field", "<init>",
                  "(Ljava/lang/Class;Ljava/lang/String;I)V", ACC_VIRTUAL);

  newField =
    UPCALL_CLASS(loader, "java/lang/reflect/Field");

  fieldArrayClass =
    UPCALL_ARRAY_CLASS(loader, "java/lang/reflect/Field", 1);
  
  fieldSlot =
    UPCALL_FIELD(loader, "java/lang/reflect/Field", "slot", "I", ACC_VIRTUAL);
  
  
  classArrayClass =
    UPCALL_ARRAY_CLASS(loader, "java/lang/Class", 1);
  
  newVMThrowable =
    UPCALL_CLASS(loader, "java/lang/VMThrowable");
  
  initVMThrowable =
    UPCALL_METHOD(loader, "java/lang/VMThrowable", "<init>", "()V", ACC_VIRTUAL);

  vmDataVMThrowable =
    UPCALL_FIELD(loader, "java/lang/VMThrowable", "vmdata", "Ljava/lang/Object;",
                 ACC_VIRTUAL);

  bufferAddress =
    UPCALL_FIELD(loader, "java/nio/Buffer", "address", "Lgnu/classpath/Pointer;",
                 ACC_VIRTUAL);

  dataPointer32 =
    UPCALL_FIELD(loader, "gnu/classpath/Pointer32", "data", "I", ACC_VIRTUAL);
  
  dataPointer64 =
    UPCALL_FIELD(loader, "gnu/classpath/Pointer64", "data", "J", ACC_VIRTUAL);

  vmdataClassLoader =
    UPCALL_FIELD(loader, "java/lang/ClassLoader", "vmdata", "Ljava/lang/Object;",
                 ACC_VIRTUAL);
  
  newStackTraceElement =
    UPCALL_CLASS(loader, "java/lang/StackTraceElement");
  
  stackTraceArray =
    UPCALL_ARRAY_CLASS(loader, "java/lang/StackTraceElement", 1);

  initStackTraceElement =
    UPCALL_METHOD(loader,  "java/lang/StackTraceElement", "<init>",
                  "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Z)V",
                  ACC_VIRTUAL);

  boolValue =
    UPCALL_FIELD(loader, "java/lang/Boolean", "value", "Z", ACC_VIRTUAL);
  
  byteValue =
    UPCALL_FIELD(loader, "java/lang/Byte", "value", "B", ACC_VIRTUAL);

  shortValue =
    UPCALL_FIELD(loader, "java/lang/Short", "value", "S", ACC_VIRTUAL);

  charValue =
    UPCALL_FIELD(loader, "java/lang/Character", "value", "C", ACC_VIRTUAL);

  intValue =
    UPCALL_FIELD(loader, "java/lang/Integer", "value", "I", ACC_VIRTUAL);

  longValue =
    UPCALL_FIELD(loader, "java/lang/Long", "value", "J", ACC_VIRTUAL);

  floatValue =
    UPCALL_FIELD(loader, "java/lang/Float", "value", "F", ACC_VIRTUAL);

  doubleValue =
    UPCALL_FIELD(loader, "java/lang/Double", "value", "D", ACC_VIRTUAL);

  Classpath::voidClass =
    UPCALL_CLASS(loader, "java/lang/Void");
  
  Classpath::boolClass =
    UPCALL_CLASS(loader, "java/lang/Boolean");

  Classpath::byteClass =
    UPCALL_CLASS(loader, "java/lang/Byte");

  Classpath::shortClass =
    UPCALL_CLASS(loader, "java/lang/Short");

  Classpath::charClass =
    UPCALL_CLASS(loader, "java/lang/Character"); 

  Classpath::intClass =
    UPCALL_CLASS(loader, "java/lang/Integer");

  Classpath::floatClass =
    UPCALL_CLASS(loader, "java/lang/Float");

  Classpath::doubleClass =
    UPCALL_CLASS(loader, "java/lang/Double");

  Classpath::longClass =
    UPCALL_CLASS(loader, "java/lang/Long");
  
  Classpath::OfObject =
    UPCALL_CLASS(loader, "java/lang/Object");

  vmStackWalker =
    UPCALL_CLASS(loader, "gnu/classpath/VMStackWalker");

  loadInClassLoader =
    UPCALL_METHOD(loader, "java/lang/ClassLoader", "loadClass",
                  "(Ljava/lang/String;)Ljava/lang/Class;", ACC_VIRTUAL);

  JavaMethod* internString =
    UPCALL_METHOD(loader, "java/lang/VMString", "intern",
                  "(Ljava/lang/String;)Ljava/lang/String;", ACC_STATIC); 
  internString->setNative();
  
  JavaMethod* isArray =
    UPCALL_METHOD(loader, "java/lang/Class", "isArray", "()Z", ACC_VIRTUAL);
  isArray->setNative();

  // Make sure classes the JIT optimizes on are loaded.
  UPCALL_CLASS(loader, "java/lang/VMFloat");
  UPCALL_CLASS(loader, "java/lang/VMDouble");

  UPCALL_REFLECT_CLASS_EXCEPTION(loader, InvocationTargetException);
  UPCALL_CLASS_EXCEPTION(loader, ArrayStoreException);
  UPCALL_CLASS_EXCEPTION(loader, ClassCastException);
  UPCALL_CLASS_EXCEPTION(loader, IllegalMonitorStateException);
  UPCALL_CLASS_EXCEPTION(loader, IllegalArgumentException);
  UPCALL_CLASS_EXCEPTION(loader, InterruptedException);
  UPCALL_CLASS_EXCEPTION(loader, IndexOutOfBoundsException);
  UPCALL_CLASS_EXCEPTION(loader, ArrayIndexOutOfBoundsException);
  UPCALL_CLASS_EXCEPTION(loader, NegativeArraySizeException);
  UPCALL_CLASS_EXCEPTION(loader, NullPointerException);
  UPCALL_CLASS_EXCEPTION(loader, SecurityException);
  UPCALL_CLASS_EXCEPTION(loader, ClassFormatError);
  UPCALL_CLASS_EXCEPTION(loader, ClassCircularityError);
  UPCALL_CLASS_EXCEPTION(loader, NoClassDefFoundError);
  UPCALL_CLASS_EXCEPTION(loader, UnsupportedClassVersionError);
  UPCALL_CLASS_EXCEPTION(loader, NoSuchFieldError);
  UPCALL_CLASS_EXCEPTION(loader, NoSuchMethodError);
  UPCALL_CLASS_EXCEPTION(loader, InstantiationError);
  UPCALL_CLASS_EXCEPTION(loader, InstantiationException);
  UPCALL_CLASS_EXCEPTION(loader, IllegalAccessError);
  UPCALL_CLASS_EXCEPTION(loader, IllegalAccessException);
  UPCALL_CLASS_EXCEPTION(loader, VerifyError);
  UPCALL_CLASS_EXCEPTION(loader, ExceptionInInitializerError);
  UPCALL_CLASS_EXCEPTION(loader, LinkageError);
  UPCALL_CLASS_EXCEPTION(loader, AbstractMethodError);
  UPCALL_CLASS_EXCEPTION(loader, UnsatisfiedLinkError);
  UPCALL_CLASS_EXCEPTION(loader, InternalError);
  UPCALL_CLASS_EXCEPTION(loader, OutOfMemoryError);
  UPCALL_CLASS_EXCEPTION(loader, StackOverflowError);
  UPCALL_CLASS_EXCEPTION(loader, UnknownError);
  UPCALL_CLASS_EXCEPTION(loader, ClassNotFoundException);
  UPCALL_CLASS_EXCEPTION(loader, ArithmeticException);
  
  UPCALL_METHOD_EXCEPTION(loader, InvocationTargetException);
  UPCALL_METHOD_EXCEPTION(loader, ArrayStoreException);
  UPCALL_METHOD_EXCEPTION(loader, ClassCastException);
  UPCALL_METHOD_EXCEPTION(loader, IllegalMonitorStateException);
  UPCALL_METHOD_EXCEPTION(loader, IllegalArgumentException);
  UPCALL_METHOD_EXCEPTION(loader, InterruptedException);
  UPCALL_METHOD_EXCEPTION(loader, IndexOutOfBoundsException);
  UPCALL_METHOD_EXCEPTION(loader, ArrayIndexOutOfBoundsException);
  UPCALL_METHOD_EXCEPTION(loader, NegativeArraySizeException);
  UPCALL_METHOD_EXCEPTION(loader, NullPointerException);
  UPCALL_METHOD_EXCEPTION(loader, SecurityException);
  UPCALL_METHOD_EXCEPTION(loader, ClassFormatError);
  UPCALL_METHOD_EXCEPTION(loader, ClassCircularityError);
  UPCALL_METHOD_EXCEPTION(loader, NoClassDefFoundError);
  UPCALL_METHOD_EXCEPTION(loader, UnsupportedClassVersionError);
  UPCALL_METHOD_EXCEPTION(loader, NoSuchFieldError);
  UPCALL_METHOD_EXCEPTION(loader, NoSuchMethodError);
  UPCALL_METHOD_EXCEPTION(loader, InstantiationError);
  UPCALL_METHOD_EXCEPTION(loader, InstantiationException);
  UPCALL_METHOD_EXCEPTION(loader, IllegalAccessError);
  UPCALL_METHOD_EXCEPTION(loader, IllegalAccessException);
  UPCALL_METHOD_EXCEPTION(loader, VerifyError);
  UPCALL_METHOD_EXCEPTION(loader, ExceptionInInitializerError);
  UPCALL_METHOD_EXCEPTION(loader, LinkageError);
  UPCALL_METHOD_EXCEPTION(loader, AbstractMethodError);
  UPCALL_METHOD_EXCEPTION(loader, UnsatisfiedLinkError);
  UPCALL_METHOD_EXCEPTION(loader, InternalError);
  UPCALL_METHOD_EXCEPTION(loader, OutOfMemoryError);
  UPCALL_METHOD_EXCEPTION(loader, StackOverflowError);
  UPCALL_METHOD_EXCEPTION(loader, UnknownError);
  UPCALL_METHOD_EXCEPTION(loader, ClassNotFoundException);
  UPCALL_METHOD_EXCEPTION(loader, ArithmeticException);
  
  UPCALL_METHOD_WITH_EXCEPTION(loader, NoClassDefFoundError);
  UPCALL_METHOD_WITH_EXCEPTION(loader, ExceptionInInitializerError);
  UPCALL_METHOD_WITH_EXCEPTION(loader, InvocationTargetException);

  InitObject = UPCALL_METHOD(loader, "java/lang/Object", "<init>", "()V",
                             ACC_VIRTUAL);
  
  FinalizeObject = UPCALL_METHOD(loader, "java/lang/Object", "finalize", "()V",
                                 ACC_VIRTUAL);
  
  IntToString = UPCALL_METHOD(loader, "java/lang/Integer", "toString",
                              "(II)Ljava/lang/String;", ACC_STATIC);

  SystemArraycopy = UPCALL_METHOD(loader, "java/lang/System", "arraycopy",
                                  "(Ljava/lang/Object;ILjava/lang/Object;II)V",
                                  ACC_STATIC);
  
  VMSystemArraycopy = UPCALL_METHOD(loader, "java/lang/VMSystem", "arraycopy",
                                  "(Ljava/lang/Object;ILjava/lang/Object;II)V",
                                  ACC_STATIC);
  
  SystemClass = UPCALL_CLASS(loader, "java/lang/System");
  EnumClass = UPCALL_CLASS(loader, "java/lang/Enum");

  newThread = 
    UPCALL_CLASS(loader, "java/lang/Thread");
  
  newVMThread = 
    UPCALL_CLASS(loader, "java/lang/VMThread");
  
  assocThread = 
    UPCALL_FIELD(loader, "java/lang/VMThread", "thread", "Ljava/lang/Thread;",
                 ACC_VIRTUAL);
  
  vmdataVMThread = 
    UPCALL_FIELD(loader, "java/lang/VMThread", "vmdata", "Ljava/lang/Object;",
                 ACC_VIRTUAL);
  
  inheritableThreadLocal = 
    UPCALL_CLASS(loader, "java/lang/InheritableThreadLocal");

  finaliseCreateInitialThread = 
    UPCALL_METHOD(loader, "java/lang/InheritableThreadLocal", "newChildThread",
                  "(Ljava/lang/Thread;)V", ACC_STATIC);
  
  initThread = 
    UPCALL_METHOD(loader, "java/lang/Thread", "<init>",
                  "(Ljava/lang/VMThread;Ljava/lang/String;IZ)V", ACC_VIRTUAL);
  
  initVMThread = 
    UPCALL_METHOD(loader, "java/lang/VMThread", "<init>",
                  "(Ljava/lang/Thread;)V", ACC_VIRTUAL);
  
  runVMThread = 
    UPCALL_METHOD(loader, "java/lang/VMThread", "run", "()V", ACC_VIRTUAL);


  groupAddThread = 
    UPCALL_METHOD(loader, "java/lang/ThreadGroup", "addThread",
                  "(Ljava/lang/Thread;)V", ACC_VIRTUAL);
  
  initGroup = 
    UPCALL_METHOD(loader, "java/lang/ThreadGroup", "<init>",
                  "()V", ACC_VIRTUAL);
  
  groupName = 
    UPCALL_FIELD(loader, "java/lang/ThreadGroup", "name", "Ljava/lang/String;",
                 ACC_VIRTUAL);
  
  threadName = 
     UPCALL_FIELD(loader, "java/lang/Thread", "name", "Ljava/lang/String;",
                  ACC_VIRTUAL);
   

  priority = 
    UPCALL_FIELD(loader,  "java/lang/Thread", "priority", "I", ACC_VIRTUAL);

  daemon = 
    UPCALL_FIELD(loader, "java/lang/Thread", "daemon", "Z", ACC_VIRTUAL);

  group =
    UPCALL_FIELD(loader, "java/lang/Thread", "group",
                 "Ljava/lang/ThreadGroup;", ACC_VIRTUAL);
  
  running = 
    UPCALL_FIELD(loader, "java/lang/VMThread", "running", "Z", ACC_VIRTUAL);
  
  threadGroup = 
    UPCALL_CLASS(loader, "java/lang/ThreadGroup");
  
  rootGroup =
    UPCALL_FIELD(loader, "java/lang/ThreadGroup", "root",
                 "Ljava/lang/ThreadGroup;", ACC_STATIC);

  vmThread = 
    UPCALL_FIELD(loader, "java/lang/Thread", "vmThread",
                 "Ljava/lang/VMThread;", ACC_VIRTUAL);
  
  getUncaughtExceptionHandler = 
    UPCALL_METHOD(loader, "java/lang/Thread", "getUncaughtExceptionHandler",
                  "()Ljava/lang/Thread$UncaughtExceptionHandler;", ACC_VIRTUAL);
  
  uncaughtException = 
    UPCALL_METHOD(loader, "java/lang/Thread$UncaughtExceptionHandler",
                  "uncaughtException",
                  "(Ljava/lang/Thread;Ljava/lang/Throwable;)V", ACC_VIRTUAL);

  
  methodClass =
    UPCALL_FIELD(loader, "java/lang/reflect/Method", "declaringClass",
                 "Ljava/lang/Class;", ACC_VIRTUAL);
  
  fieldClass =
    UPCALL_FIELD(loader, "java/lang/reflect/Field", "declaringClass",
                 "Ljava/lang/Class;", ACC_VIRTUAL);
  
  constructorClass =
    UPCALL_FIELD(loader, "java/lang/reflect/Constructor", "clazz",
                 "Ljava/lang/Class;", ACC_VIRTUAL);

  loader->loadName(loader->asciizConstructUTF8("java/lang/String"), 
                                       true, false, NULL);

  loader->loadName(loader->asciizConstructUTF8("java/lang/Object"), 
                                       true, false, NULL);
  
  // Don't compile methods here, we still don't know where to allocate Java
  // strings.
  
  JavaMethod* getEnv =
    UPCALL_METHOD(loader, "java/lang/VMSystem", "getenv",
                  "(Ljava/lang/String;)Ljava/lang/String;", ACC_STATIC);
  getEnv->setNative();

  JavaMethod* getCallingClass =
    UPCALL_METHOD(loader, "gnu/classpath/VMStackWalker", "getCallingClass",
                  "()Ljava/lang/Class;", ACC_STATIC);
  getCallingClass->setNative();
  
  JavaMethod* getCallingClassLoader =
    UPCALL_METHOD(loader, "gnu/classpath/VMStackWalker", "getCallingClassLoader",
                  "()Ljava/lang/ClassLoader;", ACC_STATIC);
  getCallingClassLoader->setNative();
  
  JavaMethod* firstNonNullClassLoader =
    UPCALL_METHOD(loader, "gnu/classpath/VMStackWalker", "firstNonNullClassLoader",
                  "()Ljava/lang/ClassLoader;", ACC_STATIC);
  firstNonNullClassLoader->setNative();
  
  JavaMethod* getCallerClass =
    UPCALL_METHOD(loader, "sun/reflect/Reflection", "getCallerClass",
                  "(I)Ljava/lang/Class;", ACC_STATIC);
  getCallerClass->setNative();
  
  JavaMethod* postProperties =
    UPCALL_METHOD(loader, "gnu/classpath/VMSystemProperties", "postInit",
                  "(Ljava/util/Properties;)V", ACC_STATIC);
  postProperties->setNative();

  // Also implement these twos, implementation in GNU Classpath 0.97.2 is buggy.
  JavaMethod* getAnnotation =
    UPCALL_METHOD(loader, "java/lang/reflect/AccessibleObject", "getAnnotation",
                  "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;",
                  ACC_VIRTUAL);
  getAnnotation->setNative();
  
  JavaMethod* getAnnotations =
    UPCALL_METHOD(loader, "java/lang/reflect/AccessibleObject",
                  "getDeclaredAnnotations",
                  "()[Ljava/lang/annotation/Annotation;",
                  ACC_VIRTUAL);
  getAnnotations->setNative();
  
  JavaMethod* getBootPackages =
    UPCALL_METHOD(loader, "java/lang/VMClassLoader", "getBootPackages",
                  "()[Ljava/lang/String;", ACC_STATIC);
  getBootPackages->setNative();
  
  //===----------------------------------------------------------------------===//
  //
  // To make classes non GC-allocated, we have to bypass the tracer functions of
  // java.lang.Class, java.lang.reflect.Field, java.lang.reflect.Method and
  // java.lang.reflect.constructor. The new tracer functions trace the classloader
  // instead of the class/field/method.
  //
  //===----------------------------------------------------------------------===//
 
  newClass->getVirtualVT()->setNativeTracer(
      (word_t)nativeJavaObjectClassTracer,
       "nativeJavaObjectClassTracer");

  newConstructor->getVirtualVT()->setNativeTracer(
      (word_t)nativeJavaObjectConstructorTracer,
      "nativeJavaObjectConstructorTracer");

   newMethod->getVirtualVT()->setNativeTracer(
      (word_t)nativeJavaObjectMethodTracer,
      "nativeJavaObjectMethodTracer");

   newField->getVirtualVT()->setNativeTracer(
      (word_t)nativeJavaObjectFieldTracer,
      "nativeJavaObjectFieldTracer"); 
   
   newVMThread->getVirtualVT()->setNativeTracer(
      (word_t)nativeJavaObjectVMThreadTracer,
      "nativeJavaObjectVMThreadTracer");
 
  newReference = UPCALL_CLASS(loader, "java/lang/ref/Reference");
    
  EnqueueReference = 
    UPCALL_METHOD(loader, "java/lang/ref/Reference",  "enqueue", "()Z",
                  ACC_VIRTUAL);
 
  JavaMethod* initWeakReference =
    UPCALL_METHOD(loader, "java/lang/ref/WeakReference", "<init>",
                  "(Ljava/lang/Object;)V",
                  ACC_VIRTUAL);
  initWeakReference->setNative();
  
  initWeakReference =
    UPCALL_METHOD(loader, "java/lang/ref/WeakReference", "<init>",
                  "(Ljava/lang/Object;Ljava/lang/ref/ReferenceQueue;)V",
                  ACC_VIRTUAL);
  initWeakReference->setNative();
  
  JavaMethod* initSoftReference =
    UPCALL_METHOD(loader, "java/lang/ref/SoftReference", "<init>",
                  "(Ljava/lang/Object;)V",
                  ACC_VIRTUAL);
  initSoftReference->setNative();
  
  initSoftReference =
    UPCALL_METHOD(loader, "java/lang/ref/SoftReference", "<init>",
                  "(Ljava/lang/Object;Ljava/lang/ref/ReferenceQueue;)V",
                  ACC_VIRTUAL);
  initSoftReference->setNative();
  
  JavaMethod* initPhantomReference =
    UPCALL_METHOD(loader, "java/lang/ref/PhantomReference", "<init>",
                  "(Ljava/lang/Object;Ljava/lang/ref/ReferenceQueue;)V",
                  ACC_VIRTUAL);
  initPhantomReference->setNative();
}

void Classpath::InitializeSystem(Jnjvm * jvm) {
#define LOAD_CLASS(cl) \
  cl->resolveClass(); \
  cl->initialiseClass(jvm);
  LOAD_CLASS(newVMThread);
  LOAD_CLASS(newVMThrowable);
#undef LOAD_CLASS

}

#include "ClasspathConstructor.inc"
#include "Classpath.inc"
#include "ClasspathField.inc"
#include "ClasspathMethod.inc"
#include "ClasspathVMClass.inc"
#include "ClasspathVMClassLoader.inc"
#include "ClasspathVMObject.inc"
#include "ClasspathVMRuntime.inc"
#include "ClasspathVMStackWalker.inc"
#include "ClasspathVMSystem.inc"
#include "ClasspathVMSystemProperties.inc"
#include "ClasspathVMThread.inc"
#include "ClasspathVMThrowable.inc"
