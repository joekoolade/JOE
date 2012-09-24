//===---------- JavaUpcalls.h - Upcalls to Java entities ------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_UPCALLS_H
#define JNJVM_JAVA_UPCALLS_H

#include "mvm/Allocator.h"

#include "JnjvmConfig.h"

#define UPCALL_CLASS(vm, name)                                                 \
  vm->loadName(vm->asciizConstructUTF8(name), true, false, NULL)

#define UPCALL_PRIMITIVE_CLASS(loader, name, nb)                               \
  new(loader->allocator, "Primitive class")                                    \
          UserClassPrimitive(loader, loader->asciizConstructUTF8(name), nb)    \

#define UPCALL_FIELD(vm, cl, name, type, acc)                                  \
  UPCALL_CLASS(vm, cl)->lookupFieldDontThrow(vm->asciizConstructUTF8(name),    \
                                             vm->asciizConstructUTF8(type),    \
                                             isStatic(acc), false, 0)

#define UPCALL_METHOD(vm, cl, name, type, acc)                                 \
  UPCALL_CLASS(vm, cl)->lookupMethodDontThrow(vm->asciizConstructUTF8(name),   \
                                              vm->asciizConstructUTF8(type),   \
                                              isStatic(acc), false, 0)

#define UPCALL_ARRAY_CLASS(loader, name, depth)                                \
  loader->constructArray(                                                      \
    loader->constructArrayName(depth, loader->asciizConstructUTF8(name)))       

#define UPCALL_CLASS_EXCEPTION(loader, name)                                   \
  name = UPCALL_CLASS(loader, "java/lang/"#name)                           

#define UPCALL_REFLECT_CLASS_EXCEPTION(loader, name)                           \
  name = UPCALL_CLASS(loader, "java/lang/reflect/"#name)                   

#define UPCALL_METHOD_EXCEPTION(loader, name) \
  Init##name = name->lookupMethodDontThrow(loader->asciizConstructUTF8("<init>"), \
                                           loader->asciizConstructUTF8("(Ljava/lang/String;)V"), \
                                           false, false, 0);

#define UPCALL_METHOD_WITH_EXCEPTION(loader, name) \
  ErrorWithExcp##name = name->lookupMethodDontThrow(loader->asciizConstructUTF8("<init>"), \
                                     loader->asciizConstructUTF8("(Ljava/lang/Throwable;)V"), \
                                     false, false, 0);

namespace j3 {

class Jnjvm;
class JavaField;
class JavaMethod;
class JavaObject;
class JavaThread;
class Class;
class ClassArray;
class JnjvmClassLoader;

class Classpath : public mvm::PermanentObject {
public: 
  ISOLATE_STATIC UserClass*  newClassLoader;
  ISOLATE_STATIC JavaMethod* getSystemClassLoader;
  ISOLATE_STATIC JavaMethod* setContextClassLoader;
  ISOLATE_STATIC UserClass* newString;
  ISOLATE_STATIC UserClass* newClass;
  ISOLATE_STATIC UserClass* newThrowable;
  ISOLATE_STATIC UserClass* newException;
  ISOLATE_STATIC JavaMethod* initClass;
  ISOLATE_STATIC JavaMethod* initClassWithProtectionDomain;
  ISOLATE_STATIC JavaField* vmdataClass;
  ISOLATE_STATIC JavaMethod* setProperty;
  ISOLATE_STATIC JavaMethod* initString;
  ISOLATE_STATIC JavaMethod* getCallingClassLoader;
  ISOLATE_STATIC JavaMethod* initConstructor;
  ISOLATE_STATIC UserClassArray* constructorArrayClass;
  ISOLATE_STATIC UserClassArray* constructorArrayAnnotation;
  ISOLATE_STATIC UserClass*      newConstructor;
  ISOLATE_STATIC JavaField*  constructorSlot;
  ISOLATE_STATIC JavaMethod* initMethod;
  ISOLATE_STATIC JavaMethod* initField;
  ISOLATE_STATIC UserClassArray* methodArrayClass;
  ISOLATE_STATIC UserClassArray* fieldArrayClass;
  ISOLATE_STATIC UserClass*      newMethod;
  ISOLATE_STATIC UserClass*      newField;
  ISOLATE_STATIC JavaField*  methodSlot;
  ISOLATE_STATIC JavaField*  fieldSlot;
  ISOLATE_STATIC UserClassArray* classArrayClass;
  ISOLATE_STATIC JavaMethod* loadInClassLoader;
  ISOLATE_STATIC JavaMethod* initVMThrowable;
  ISOLATE_STATIC JavaField*  vmDataVMThrowable;
  ISOLATE_STATIC UserClass*  newVMThrowable;
  ISOLATE_STATIC JavaField*  bufferAddress;
  ISOLATE_STATIC JavaField*  dataPointer32;
  ISOLATE_STATIC JavaField*  dataPointer64;
  ISOLATE_STATIC UserClass*  newPointer32;
  ISOLATE_STATIC UserClass*  newPointer64;
  ISOLATE_STATIC UserClass*  newDirectByteBuffer;
  ISOLATE_STATIC JavaMethod* InitDirectByteBuffer;
  ISOLATE_STATIC JavaField*  vmdataClassLoader;
  ISOLATE_STATIC UserClass*  enumClass;

  ISOLATE_STATIC JavaField* boolValue;
  ISOLATE_STATIC JavaField* byteValue;
  ISOLATE_STATIC JavaField* shortValue;
  ISOLATE_STATIC JavaField* charValue;
  ISOLATE_STATIC JavaField* intValue;
  ISOLATE_STATIC JavaField* longValue;
  ISOLATE_STATIC JavaField* floatValue;
  ISOLATE_STATIC JavaField* doubleValue;

  ISOLATE_STATIC UserClass* newStackTraceElement;
  ISOLATE_STATIC UserClassArray* stackTraceArray;
  ISOLATE_STATIC JavaMethod* initStackTraceElement;

  ISOLATE_STATIC void initialiseClasspath(JnjvmClassLoader* loader);
  
  ISOLATE_STATIC UserClass* voidClass;
  ISOLATE_STATIC UserClass* boolClass;
  ISOLATE_STATIC UserClass* byteClass;
  ISOLATE_STATIC UserClass* shortClass;
  ISOLATE_STATIC UserClass* charClass;
  ISOLATE_STATIC UserClass* intClass;
  ISOLATE_STATIC UserClass* floatClass;
  ISOLATE_STATIC UserClass* doubleClass;
  ISOLATE_STATIC UserClass* longClass;
  
  ISOLATE_STATIC UserClass* vmStackWalker;
  
  ISOLATE_STATIC UserClass* newThread;
  ISOLATE_STATIC UserClass* newVMThread;
  ISOLATE_STATIC JavaField* assocThread;
  ISOLATE_STATIC JavaField* vmdataVMThread;
  ISOLATE_STATIC JavaMethod* finaliseCreateInitialThread;
  ISOLATE_STATIC JavaMethod* initThread;
  ISOLATE_STATIC JavaMethod* initVMThread;
  ISOLATE_STATIC JavaMethod* runVMThread;
  ISOLATE_STATIC JavaMethod* groupAddThread;
  ISOLATE_STATIC JavaField* threadName;
  ISOLATE_STATIC JavaField* groupName;
  ISOLATE_STATIC JavaMethod* initGroup;
  ISOLATE_STATIC JavaField* priority;
  ISOLATE_STATIC JavaField* daemon;
  ISOLATE_STATIC JavaField* group;
  ISOLATE_STATIC JavaField* running;
  ISOLATE_STATIC UserClass* threadGroup;
  ISOLATE_STATIC JavaField* rootGroup;
  ISOLATE_STATIC JavaField* vmThread;
  ISOLATE_STATIC JavaMethod* getUncaughtExceptionHandler;
  ISOLATE_STATIC JavaMethod* uncaughtException;
  ISOLATE_STATIC UserClass*  inheritableThreadLocal;
  

  ISOLATE_STATIC UserClass* InvocationTargetException;
  ISOLATE_STATIC UserClass* ArrayStoreException;
  ISOLATE_STATIC UserClass* ClassCastException;
  ISOLATE_STATIC UserClass* IllegalMonitorStateException;
  ISOLATE_STATIC UserClass* IllegalArgumentException;
  ISOLATE_STATIC UserClass* InterruptedException;
  ISOLATE_STATIC UserClass* IndexOutOfBoundsException;
  ISOLATE_STATIC UserClass* ArrayIndexOutOfBoundsException;
  ISOLATE_STATIC UserClass* NegativeArraySizeException;
  ISOLATE_STATIC UserClass* NullPointerException;
  ISOLATE_STATIC UserClass* SecurityException;
  ISOLATE_STATIC UserClass* ClassFormatError;
  ISOLATE_STATIC UserClass* ClassCircularityError;
  ISOLATE_STATIC UserClass* NoClassDefFoundError;
  ISOLATE_STATIC UserClass* UnsupportedClassVersionError;
  ISOLATE_STATIC UserClass* NoSuchFieldError;
  ISOLATE_STATIC UserClass* NoSuchMethodError;
  ISOLATE_STATIC UserClass* InstantiationError;
  ISOLATE_STATIC UserClass* InstantiationException;
  ISOLATE_STATIC UserClass* IllegalAccessError;
  ISOLATE_STATIC UserClass* IllegalAccessException;
  ISOLATE_STATIC UserClass* VerifyError;
  ISOLATE_STATIC UserClass* ExceptionInInitializerError;
  ISOLATE_STATIC UserClass* LinkageError;
  ISOLATE_STATIC UserClass* AbstractMethodError;
  ISOLATE_STATIC UserClass* UnsatisfiedLinkError;
  ISOLATE_STATIC UserClass* InternalError;
  ISOLATE_STATIC UserClass* OutOfMemoryError;
  ISOLATE_STATIC UserClass* StackOverflowError;
  ISOLATE_STATIC UserClass* UnknownError;
  ISOLATE_STATIC UserClass* ClassNotFoundException;
  ISOLATE_STATIC UserClass* ArithmeticException;

  ISOLATE_STATIC JavaMethod* InitInvocationTargetException;
  ISOLATE_STATIC JavaMethod* InitArrayStoreException;
  ISOLATE_STATIC JavaMethod* InitClassCastException;
  ISOLATE_STATIC JavaMethod* InitIllegalMonitorStateException;
  ISOLATE_STATIC JavaMethod* InitIllegalArgumentException;
  ISOLATE_STATIC JavaMethod* InitInterruptedException;
  ISOLATE_STATIC JavaMethod* InitIndexOutOfBoundsException;
  ISOLATE_STATIC JavaMethod* InitArrayIndexOutOfBoundsException;
  ISOLATE_STATIC JavaMethod* InitNegativeArraySizeException;
  ISOLATE_STATIC JavaMethod* InitNullPointerException;
  ISOLATE_STATIC JavaMethod* InitSecurityException;
  ISOLATE_STATIC JavaMethod* InitClassFormatError;
  ISOLATE_STATIC JavaMethod* InitClassCircularityError;
  ISOLATE_STATIC JavaMethod* InitNoClassDefFoundError;
  ISOLATE_STATIC JavaMethod* InitUnsupportedClassVersionError;
  ISOLATE_STATIC JavaMethod* InitNoSuchFieldError;
  ISOLATE_STATIC JavaMethod* InitNoSuchMethodError;
  ISOLATE_STATIC JavaMethod* InitInstantiationError;
  ISOLATE_STATIC JavaMethod* InitInstantiationException;
  ISOLATE_STATIC JavaMethod* InitIllegalAccessError;
  ISOLATE_STATIC JavaMethod* InitIllegalAccessException;
  ISOLATE_STATIC JavaMethod* InitVerifyError;
  ISOLATE_STATIC JavaMethod* InitExceptionInInitializerError;
  ISOLATE_STATIC JavaMethod* InitLinkageError;
  ISOLATE_STATIC JavaMethod* InitAbstractMethodError;
  ISOLATE_STATIC JavaMethod* InitUnsatisfiedLinkError;
  ISOLATE_STATIC JavaMethod* InitInternalError;
  ISOLATE_STATIC JavaMethod* InitOutOfMemoryError;
  ISOLATE_STATIC JavaMethod* InitStackOverflowError;
  ISOLATE_STATIC JavaMethod* InitUnknownError;
  ISOLATE_STATIC JavaMethod* InitClassNotFoundException;
  ISOLATE_STATIC JavaMethod* InitArithmeticException;
  
  ISOLATE_STATIC JavaMethod* SystemArraycopy;
  ISOLATE_STATIC JavaMethod* VMSystemArraycopy;
  ISOLATE_STATIC Class*      SystemClass;
  
  ISOLATE_STATIC JavaMethod* IntToString;

  ISOLATE_STATIC JavaMethod* InitObject;
  ISOLATE_STATIC JavaMethod* FinalizeObject;

  ISOLATE_STATIC JavaMethod* ErrorWithExcpNoClassDefFoundError;
  ISOLATE_STATIC JavaMethod* ErrorWithExcpExceptionInInitializerError;
  ISOLATE_STATIC JavaMethod* ErrorWithExcpInvocationTargetException;
  
  

  ISOLATE_STATIC UserClassArray* ArrayOfByte;
  ISOLATE_STATIC UserClassArray* ArrayOfChar;
  ISOLATE_STATIC UserClassArray* ArrayOfInt;
  ISOLATE_STATIC UserClassArray* ArrayOfShort;
  ISOLATE_STATIC UserClassArray* ArrayOfBool;
  ISOLATE_STATIC UserClassArray* ArrayOfLong;
  ISOLATE_STATIC UserClassArray* ArrayOfFloat;
  ISOLATE_STATIC UserClassArray* ArrayOfDouble;
  ISOLATE_STATIC UserClassArray* ArrayOfObject;
  ISOLATE_STATIC UserClassArray* ArrayOfString;
  
  ISOLATE_STATIC UserClassPrimitive* OfByte;
  ISOLATE_STATIC UserClassPrimitive* OfChar;
  ISOLATE_STATIC UserClassPrimitive* OfInt;
  ISOLATE_STATIC UserClassPrimitive* OfShort;
  ISOLATE_STATIC UserClassPrimitive* OfBool;
  ISOLATE_STATIC UserClassPrimitive* OfLong;
  ISOLATE_STATIC UserClassPrimitive* OfFloat;
  ISOLATE_STATIC UserClassPrimitive* OfDouble;
  ISOLATE_STATIC UserClassPrimitive* OfVoid;

  ISOLATE_STATIC UserClass* OfObject;
  
  ISOLATE_STATIC JavaField* methodClass;
  ISOLATE_STATIC JavaField* fieldClass;
  ISOLATE_STATIC JavaField* constructorClass;
  
  ISOLATE_STATIC JavaMethod* EnqueueReference;
  ISOLATE_STATIC Class*      newReference;

  ISOLATE_STATIC UserClass*  EnumClass;

private:
  ISOLATE_STATIC void CreateJavaThread(Jnjvm* vm, JavaThread* myth,
                                       const char* name, JavaObject* Group);

public:
  ISOLATE_STATIC void InitializeThreading(Jnjvm* vm);
  ISOLATE_STATIC void InitializeSystem(Jnjvm* vm);
};


} // end namespace j3

#endif
