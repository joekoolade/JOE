//===---------- Jnjvm.cpp - Java virtual machine description --------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#define JNJVM_LOAD 0

#include <cfloat>
#include <climits>
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>
#include "debug.h"

#include "mvm/Threads/Thread.h"
#include "MvmGC.h"

#include "ClasspathReflect.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaCompiler.h"
#include "JavaConstantPool.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "LinkJavaRuntime.h"
#include "LockedMap.h"
#include "Reader.h"
#include "ReferenceQueue.h"
#include "VMStaticInstance.h"
#include "Zip.h"

using namespace j3;

const char* Jnjvm::dirSeparator = "/";
const char* Jnjvm::envSeparator = ":";
const unsigned int Jnjvm::Magic = 0xcafebabe;

/// initialiseClass - Java class initialisation. Java specification §2.17.5.

void UserClass::initialiseClass(Jnjvm* vm) {
  JavaObject* exc = NULL;
  JavaObject* obj = NULL;
  llvm_gcroot(exc, 0);
  llvm_gcroot(obj, 0);
  
  // Primitives are initialized at boot time, arrays are initialized directly.
  
  // Assumes that the Class object has already been verified and prepared and
  // that the Class object contains state that can indicate one of four
  // situations:
  //
  //  * This Class object is verified and prepared but not initialized.
  //  * This Class object is being initialized by some particular thread T.
  //  * This Class object is fully initialized and ready for use.
  //  * This Class object is in an erroneous state, perhaps because the
  //    verification step failed or because initialization was attempted and
  //    failed.

  assert((isResolved() || getOwnerClass() || isReady() ||
         isErroneous()) && "Class in wrong state");
  
  if (getInitializationState() != ready) {
    
    // 1. Synchronize on the Class object that represents the class or 
    //    interface to be initialized. This involves waiting until the
    //    current thread can obtain the lock for that object
    //    (Java specification §8.13).
    acquire();
    JavaThread* self = JavaThread::get();

    if (getInitializationState() == inClinit) {
      // 2. If initialization by some other thread is in progress for the
      //    class or interface, then wait on this Class object (which 
      //    temporarily releases the lock). When the current thread awakens
      //    from the wait, repeat this step.
      if (getOwnerClass() != self) {
        while (getOwnerClass()) {
          waitClass();
        }
      } else {
        // 3. If initialization is in progress for the class or interface by
        //    the current thread, then this must be a recursive request for 
        //    initialization. Release the lock on the Class object and complete
        //    normally.
        release();
        return;
      }
    } 
    
    // 4. If the class or interface has already been initialized, then no 
    //    further action is required. Release the lock on the Class object
    //    and complete normally.
    if (getInitializationState() == ready) {
      release();
      return;
    }
    
    // 5. If the Class object is in an erroneous state, then initialization is
    //    not possible. Release the lock on the Class object and throw a
    //    NoClassDefFoundError.
    if (isErroneous()) {
      release();
      vm->noClassDefFoundError(name);
    }

    // 6. Otherwise, record the fact that initialization of the Class object is
    //    now in progress by the current thread and release the lock on the
    //    Class object.
    setOwnerClass(self);
    setInitializationState(inClinit);
    UserClass* cl = (UserClass*)this;
    
    // Single environment allocates the static instance during resolution, so
    // that compiled code can access it directly (with an initialization
    // check just before the access)
    if (!cl->getStaticInstance()) cl->allocateStaticInstance(vm);

    release();
  

    // 7. Next, if the Class object represents a class rather than an interface, 
    //    and the direct superclass of this class has not yet been initialized,
    //    then recursively perform this entire procedure for the uninitialized 
    //    superclass. If the initialization of the direct superclass completes 
    //    abruptly because of a thrown exception, then lock this Class object, 
    //    label it erroneous, notify all waiting threads, release the lock, 
    //    and complete abruptly, throwing the same exception that resulted from 
    //    the initializing the superclass.
    UserClass* super = getSuper();
    if (super) {
      TRY {
        super->initialiseClass(vm);
      } CATCH {
        acquire();
        setErroneous();
        setOwnerClass(0);
        broadcastClass();
        release();
      } END_CATCH;
      if (self->pendingException != NULL) {
        self->throwPendingException();
        return;
      }
    }
 
    // 8. Next, execute either the class variable initializers and static
    //    initializers of the class or the field initializers of the interface,
    //    in textual order, as though they were a single block, except that
    //    final static variables and fields of interfaces whose values are 
    //    compile-time constants are initialized first.
    
    PRINT_DEBUG(JNJVM_LOAD, 0, COLOR_NORMAL, "; ", 0);
    PRINT_DEBUG(JNJVM_LOAD, 0, LIGHT_GREEN, "clinit ", 0);
    PRINT_DEBUG(JNJVM_LOAD, 0, COLOR_NORMAL, "%s\n", mvm::PrintString(this).cString());

    JavaField* fields = cl->getStaticFields();
    for (uint32 i = 0; i < cl->nbStaticFields; ++i) {
      fields[i].InitStaticField(vm);
    }
  
    JavaMethod* meth = lookupMethodDontThrow(vm->bootstrapLoader->clinitName,
                                             vm->bootstrapLoader->clinitType,
                                             true, false, 0);

    if (meth) {
      TRY {
        meth->invokeIntStatic(vm, cl);
      } CATCH {
        exc = self->getJavaException();
        assert(exc && "no exception?");
        self->clearException();
      } END_CATCH;
    }

    // 9. If the execution of the initializers completes normally, then lock
    //    this Class object, label it fully initialized, notify all waiting 
    //    threads, release the lock, and complete this procedure normally.
    if (!exc) {
      acquire();
      setInitializationState(ready);
      setOwnerClass(0);
      broadcastClass();
      release();
      return;
    }
    
    // 10. Otherwise, the initializers must have completed abruptly by
    //     throwing some exception E. If the class of E is not Error or one
    //     of its subclasses, then create a new instance of the class 
    //     ExceptionInInitializerError, with E as the argument, and use this
    //     object in place of E in the following step. But if a new instance of
    //     ExceptionInInitializerError cannot be created because an
    //     OutOfMemoryError occurs, then instead use an OutOfMemoryError object
    //     in place of E in the following step.
    if (JavaObject::getClass(exc)->isAssignableFrom(vm->upcalls->newException)) {
      Classpath* upcalls = classLoader->bootstrapLoader->upcalls;
      UserClass* clExcp = upcalls->ExceptionInInitializerError;
      Jnjvm* vm = self->getJVM();
      obj = clExcp->doNew(vm);
      if (obj == NULL) {
        fprintf(stderr, "implement me");
        abort();
      }
      JavaMethod* init = upcalls->ErrorWithExcpExceptionInInitializerError;
      init->invokeIntSpecial(vm, clExcp, obj, &exc);
      exc = obj;
    } 

    // 11. Lock the Class object, label it erroneous, notify all waiting
    //     threads, release the lock, and complete this procedure abruptly
    //     with reason E or its replacement as determined in the previous step.
    acquire();
    setErroneous();
    setOwnerClass(0);
    broadcastClass();
    release();
    self->throwException(exc);
    return;
  }
}
      
void Jnjvm::errorWithExcp(UserClass* cl, JavaMethod* init,
                          const JavaObject* excp) {
  JavaObject* obj = NULL;
  llvm_gcroot(obj, 0);
  llvm_gcroot(excp, 0);

  obj = cl->doNew(this);
  init->invokeIntSpecial(this, cl, obj, &excp);
  JavaThread::get()->throwException(obj);
}

JavaObject* Jnjvm::CreateError(UserClass* cl, JavaMethod* init,
                               const char* asciiz) {
  JavaObject* obj = NULL;
  JavaString* str = NULL;
  llvm_gcroot(obj, 0);
  llvm_gcroot(str, 0);
  obj = cl->doNew(this);

  if (asciiz) str = asciizToStr(asciiz);

  init->invokeIntSpecial(this, cl, obj, &str);
  return obj;
}

JavaObject* Jnjvm::CreateError(UserClass* cl, JavaMethod* init,
                               JavaString* str) {
  JavaObject* obj = NULL;
  llvm_gcroot(str, 0);
  llvm_gcroot(obj, 0);
  obj = cl->doNew(this);
  init->invokeIntSpecial(this, cl, obj, &str);
  return obj;
}

void Jnjvm::error(UserClass* cl, JavaMethod* init, JavaString* str) {
  JavaObject* obj = 0;
  llvm_gcroot(obj, 0);
  llvm_gcroot(str, 0);
  obj = CreateError(cl, init, str);
  JavaThread::get()->throwException(obj);
}

void Jnjvm::arrayStoreException() {
  error(upcalls->ArrayStoreException,
        upcalls->InitArrayStoreException, (JavaString*)0);
}

void Jnjvm::indexOutOfBounds(const JavaObject* obj, sint32 entry) {
  JavaString* str = NULL;
  llvm_gcroot(obj, 0);
  llvm_gcroot(str, 0);
  str = (JavaString*)upcalls->IntToString->invokeJavaObjectStatic(
      this, upcalls->intClass, entry, 10);
  error(upcalls->ArrayIndexOutOfBoundsException,
        upcalls->InitArrayIndexOutOfBoundsException, str);
}

void Jnjvm::negativeArraySizeException(sint32 size) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = (JavaString*)
    upcalls->IntToString->invokeJavaObjectStatic(this, upcalls->intClass,
                                                 size, 10);
  error(upcalls->NegativeArraySizeException,
        upcalls->InitNegativeArraySizeException, str);
}

void Jnjvm::nullPointerException() {
  error(upcalls->NullPointerException,
        upcalls->InitNullPointerException, (JavaString*)0);
}

JavaObject* Jnjvm::CreateIndexOutOfBoundsException(sint32 entry) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = (JavaString*)
    upcalls->IntToString->invokeJavaObjectStatic(this, upcalls->intClass,
                                                 entry, 10);
  return CreateError(upcalls->ArrayIndexOutOfBoundsException,
                     upcalls->InitArrayIndexOutOfBoundsException, str);
}

JavaObject* Jnjvm::CreateNegativeArraySizeException() {
  return CreateError(upcalls->NegativeArraySizeException,
                     upcalls->InitNegativeArraySizeException,
                     (JavaString*)0);
}

JavaObject* Jnjvm::CreateUnsatisfiedLinkError(JavaMethod* meth) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = constructString(meth->toString());
  return CreateError(upcalls->UnsatisfiedLinkError,
                     upcalls->InitUnsatisfiedLinkError,
                     str);
}

JavaObject* Jnjvm::CreateArithmeticException() {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr("/ by zero");
  return CreateError(upcalls->ArithmeticException,
                     upcalls->InitArithmeticException, str);
}

JavaObject* Jnjvm::CreateNullPointerException() {
  return CreateError(upcalls->NullPointerException,
                     upcalls->InitNullPointerException,
                     (JavaString*)0);
}

JavaObject* Jnjvm::CreateOutOfMemoryError() {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr("Java heap space");
  return CreateError(upcalls->OutOfMemoryError,
                     upcalls->InitOutOfMemoryError, str);
}

JavaObject* Jnjvm::CreateStackOverflowError() {
  // Don't call init, or else we'll get a new stack overflow error.
  JavaObject* obj = NULL;
  llvm_gcroot(obj, 0);
  obj = upcalls->StackOverflowError->doNew(this);
  JavaObjectThrowable::fillInStackTrace((JavaObjectThrowable*)obj);
  return obj;
}

JavaObject* Jnjvm::CreateArrayStoreException(JavaVirtualTable* VT) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  if (VT != NULL) str = JavaString::internalToJava(VT->cl->name, this);
  return CreateError(upcalls->ArrayStoreException,
                     upcalls->InitArrayStoreException, str);
}

JavaObject* Jnjvm::CreateClassCastException(JavaObject* obj,
                                            UserCommonClass* cl) {
  llvm_gcroot(obj, 0);
  return CreateError(upcalls->ClassCastException,
                     upcalls->InitClassCastException,
                     (JavaString*)0);
}

JavaObject* Jnjvm::CreateLinkageError(const char* msg) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr(msg);
  return CreateError(upcalls->LinkageError,
                     upcalls->InitLinkageError, str);
}

void Jnjvm::illegalAccessException(const char* msg) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr(msg);
  error(upcalls->IllegalAccessException,
        upcalls->InitIllegalAccessException, str);
}

void Jnjvm::illegalMonitorStateException(const JavaObject* obj) {
  llvm_gcroot(obj, 0);
  error(upcalls->IllegalMonitorStateException,
        upcalls->InitIllegalMonitorStateException,
        (JavaString*)0);
}

void Jnjvm::interruptedException(const JavaObject* obj) {
  llvm_gcroot(obj, 0);
  error(upcalls->InterruptedException,
        upcalls->InitInterruptedException,
        (JavaString*)0);
}


void Jnjvm::initializerError(const JavaObject* excp) {
  llvm_gcroot(excp, 0);
  errorWithExcp(upcalls->ExceptionInInitializerError,
                upcalls->ErrorWithExcpExceptionInInitializerError,
                excp);
}

void Jnjvm::invocationTargetException(const JavaObject* excp) {
  llvm_gcroot(excp, 0);
  errorWithExcp(upcalls->InvocationTargetException,
                upcalls->ErrorWithExcpInvocationTargetException,
                excp);
}

void Jnjvm::outOfMemoryError() {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr("Java heap space");
  error(upcalls->OutOfMemoryError,
        upcalls->InitOutOfMemoryError, str);
}

void Jnjvm::illegalArgumentException(const char* msg) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr(msg);
  error(upcalls->IllegalArgumentException,
        upcalls->InitIllegalArgumentException, str);
}

void Jnjvm::classCastException(JavaObject* obj, UserCommonClass* cl) {
  llvm_gcroot(obj, 0);
  error(upcalls->ClassCastException,
        upcalls->InitClassCastException,
        (JavaString*)0);
}

void Jnjvm::noClassDefFoundError(JavaObject* obj) {
  llvm_gcroot(obj, 0);
  errorWithExcp(upcalls->NoClassDefFoundError,
        upcalls->ErrorWithExcpNoClassDefFoundError, 
        obj);
}

void Jnjvm::instantiationException(UserCommonClass* cl) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = internalUTF8ToStr(cl->name);
  error(upcalls->InstantiationException, upcalls->InitInstantiationException,
        str);
}

void Jnjvm::instantiationError(UserCommonClass* cl) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = internalUTF8ToStr(cl->name);
  error(upcalls->InstantiationError, upcalls->InitInstantiationError, str);
}
  

JavaString* CreateNoSuchMsg(CommonClass* cl, const UTF8* name,
                            Jnjvm* vm) {
  ArrayUInt16* msg = NULL;
  JavaString* str = NULL;
  llvm_gcroot(msg, 0);
  llvm_gcroot(str, 0);
  msg = (ArrayUInt16*)
    vm->upcalls->ArrayOfChar->doNew(19 + cl->name->size + name->size, vm);

  uint32 i = 0;


  ArrayUInt16::setElement(msg, 'u', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'b', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'e', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 't', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'f', i); i++;
  ArrayUInt16::setElement(msg, 'i', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;

  for (sint32 j = 0; j < name->size; ++j) {
    ArrayUInt16::setElement(msg, name->elements[j], i);
    i++;
  }

  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'i', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  
  for (sint32 j = 0; j < cl->name->size; ++j) {
    if (cl->name->elements[j] == '/') {
      ArrayUInt16::setElement(msg, '.', i);
      i++;
    } else {
      ArrayUInt16::setElement(msg, cl->name->elements[j], i);
      i++;
    }
  }

  str = vm->constructString(msg);

  return str;
}

void Jnjvm::noSuchFieldError(CommonClass* cl, const UTF8* name) { 
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = CreateNoSuchMsg(cl, name, this);
  error(upcalls->NoSuchFieldError,
        upcalls->InitNoSuchFieldError, str);
}

void Jnjvm::noSuchMethodError(CommonClass* cl, const UTF8* name) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = CreateNoSuchMsg(cl, name, this);
  error(upcalls->NoSuchMethodError,
        upcalls->InitNoSuchMethodError, str);
}

void Jnjvm::abstractMethodError(CommonClass* cl, const UTF8* name) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = CreateNoSuchMsg(cl, name, this);
  error(upcalls->AbstractMethodError,
        upcalls->InitAbstractMethodError, str);
}

JavaString* CreateUnableToLoad(const UTF8* name, Jnjvm* vm) {
  ArrayUInt16* msg = NULL;
  JavaString* str = NULL;
  llvm_gcroot(msg, 0);
  llvm_gcroot(str, 0);

  msg = (ArrayUInt16*)vm->upcalls->ArrayOfChar->doNew(15 + name->size, vm);
  uint32 i = 0;


  ArrayUInt16::setElement(msg, 'u', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'b', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'e', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 't', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;

  for (sint32 j = 0; j < name->size; ++j) {
    if (name->elements[j] == '/') {
      ArrayUInt16::setElement(msg, '.', i); i++;
    } else {
      ArrayUInt16::setElement(msg, name->elements[j], i); i++;
    }
  }

  str = vm->constructString(msg);

  return str;
}

JavaString* CreateUnableToLoad(JavaString* name, Jnjvm* vm) {
  JavaString* str = NULL;
  ArrayUInt16* msg = NULL;
  llvm_gcroot(msg, 0);
  llvm_gcroot(str, 0);

  msg = (ArrayUInt16*)vm->upcalls->ArrayOfChar->doNew(15 + name->count, vm);
  uint32 i = 0;

  ArrayUInt16::setElement(msg, 'u', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'b', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'e', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 't', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;

  for (sint32 j = name->offset; j < name->offset + name->count; ++j) {
    if (ArrayUInt16::getElement(JavaString::getValue(name), j) == '/') {
      ArrayUInt16::setElement(msg, '.', i); i++;
    } else {
      ArrayUInt16::setElement(msg, ArrayUInt16::getElement(JavaString::getValue(name), j), i); i++;
    }
  }

  str = vm->constructString(msg);

  return str;
}



void Jnjvm::noClassDefFoundError(const UTF8* name) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = CreateUnableToLoad(name, this);
  error(upcalls->NoClassDefFoundError,
        upcalls->InitNoClassDefFoundError, str);
}

void Jnjvm::classNotFoundException(JavaString* name) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = CreateUnableToLoad(name, this);
  error(upcalls->ClassNotFoundException,
        upcalls->InitClassNotFoundException, str);
}

void Jnjvm::noClassDefFoundError(UserClass* cl, const UTF8* name) {
  ArrayUInt16* msg = NULL;
  JavaString* str = NULL;
  llvm_gcroot(msg, 0);
  llvm_gcroot(str, 0);

  uint32 size = 35 + name->size + cl->name->size;
  msg = (ArrayUInt16*)upcalls->ArrayOfChar->doNew(size, this);
  uint32 i = 0;


  ArrayUInt16::setElement(msg, 't', i); i++;
  ArrayUInt16::setElement(msg, 'r', i); i++;
  ArrayUInt16::setElement(msg, 'y', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 't', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;

  for (sint32 j = 0; j < cl->name->size; ++j) {
    if (cl->name->elements[j] == '/') {
      ArrayUInt16::setElement(msg, '.', i); i++;
    } else {
      ArrayUInt16::setElement(msg, cl->name->elements[j], i); i++;
    }
  }
  
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'f', i); i++;
  ArrayUInt16::setElement(msg, 'o', i); i++;
  ArrayUInt16::setElement(msg, 'u', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'c', i); i++;
  ArrayUInt16::setElement(msg, 'l', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 's', i); i++;
  ArrayUInt16::setElement(msg, 's', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  ArrayUInt16::setElement(msg, 'n', i); i++;
  ArrayUInt16::setElement(msg, 'a', i); i++;
  ArrayUInt16::setElement(msg, 'm', i); i++;
  ArrayUInt16::setElement(msg, 'e', i); i++;
  ArrayUInt16::setElement(msg, 'd', i); i++;
  ArrayUInt16::setElement(msg, ' ', i); i++;
  
  for (sint32 j = 0; j < name->size; ++j) {
    if (name->elements[j] == '/') {
      ArrayUInt16::setElement(msg, '.', i); i++;
    } else {
      ArrayUInt16::setElement(msg, name->elements[j], i); i++;
    }
  }
 
  assert(i == size && "Array overflow");

  str = constructString(msg);
  error(upcalls->NoClassDefFoundError, upcalls->InitNoClassDefFoundError, str);
}


void Jnjvm::classFormatError(const char* msg) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  str = asciizToStr(msg);
  error(upcalls->ClassFormatError, upcalls->InitClassFormatError, str);
}

JavaString* Jnjvm::internalUTF8ToStr(const UTF8* utf8) {
  ArrayUInt16* tmp = NULL;
  llvm_gcroot(tmp, 0);
  uint32 size = utf8->size;
  tmp = (ArrayUInt16*)upcalls->ArrayOfChar->doNew(size, this);
  
  for (uint32 i = 0; i < size; i++) {
    ArrayUInt16::setElement(tmp, utf8->elements[i], i);
  }
  
  return hashStr.lookupOrCreate(const_cast<const ArrayUInt16*&>(tmp), this,
                                JavaString::stringDup);
}

JavaString* Jnjvm::constructString(const ArrayUInt16* array) { 
  JavaString* res = NULL;
  llvm_gcroot(array, 0);
  llvm_gcroot(res, 0);
  res = hashStr.lookupOrCreate(array, this, JavaString::stringDup);
  return res;
}

JavaString* Jnjvm::asciizToStr(const char* asciiz) {
  ArrayUInt16* var = NULL;
  llvm_gcroot(var, 0);
  assert(asciiz && "No asciiz given");
  var = asciizToArray(asciiz);
  return constructString(var);
}

void Jnjvm::addProperty(char* key, char* value) {
  postProperties.push_back(std::make_pair(key, value));
}

// Mimic what's happening in Classpath when creating a java.lang.Class object.
JavaObject* UserCommonClass::getClassDelegatee(Jnjvm* vm, JavaObject* pd) {
  JavaObjectClass* delegatee = 0;
  JavaObjectClass* base = 0;
  llvm_gcroot(pd, 0);
  llvm_gcroot(delegatee, 0);
  llvm_gcroot(base, 0);

  if (getDelegatee() == NULL) {
    UserClass* cl = vm->upcalls->newClass;
    delegatee = (JavaObjectClass*)cl->doNew(vm);
    JavaObjectClass::setClass(delegatee, this);
    if (pd == NULL && isArray()) {
      base = (JavaObjectClass*)
        asArrayClass()->baseClass()->getClassDelegatee(vm, pd);
      JavaObjectClass::setProtectionDomain(
        delegatee, JavaObjectClass::getProtectionDomain(base));
    } else {
      JavaObjectClass::setProtectionDomain(delegatee, pd);
    }
    setDelegatee(delegatee);
  }
  return getDelegatee();
}

JavaObject* const* UserCommonClass::getClassDelegateePtr(Jnjvm* vm, JavaObject* pd) {
  llvm_gcroot(pd, 0);
  // Make sure it's created.
  getClassDelegatee(vm, pd);
  return getDelegateePtr();
}

#define PATH_MANIFEST "META-INF/MANIFEST.MF"
#define MAIN_CLASS "Main-Class: "
#define MAIN_LOWER_CLASS "Main-class: "
#define PREMAIN_CLASS "Premain-Class: "
#define BOOT_CLASS_PATH "Boot-Class-Path: "
#define CAN_REDEFINE_CLASS_PATH "Can-Redefine-Classes: "

#define LENGTH_MAIN_CLASS 12
#define LENGTH_PREMAIN_CLASS 15
#define LENGTH_BOOT_CLASS_PATH 17

extern "C" struct JNINativeInterface JNI_JNIEnvTable;
extern "C" const struct JNIInvokeInterface JNI_JavaVMTable;

void ClArgumentsInfo::javaAgent(char* cur) {
  assert(0 && "implement me");
}

extern "C" int sys_strnstr(const char *haystack, const char *needle) {
  const char* res = strstr(haystack, needle);
  if (res) return res - haystack;
  else return -1; 
}


static char* findInformation(Jnjvm* vm, ClassBytes* manifest, const char* entry,
                             uint32 len) {
  sint32 index = sys_strnstr((char*)manifest->elements, entry);
  if (index != -1) {
    index += len;
    sint32 end = sys_strnstr((char*)manifest->elements + index, "\n");
    if (end == -1) end = manifest->size;
    else end += index;

    sint32 length = end - index - 1;
    char* name = (char*)vm->allocator.Allocate(length + 1, "class name");
    memcpy(name, manifest->elements + index, length);
    name[length] = 0;
    return name;
  } else {
    return 0;
  }
}

void ClArgumentsInfo::extractClassFromJar(Jnjvm* vm, int argc, char** argv, 
                                          int i) {
  ClassBytes* bytes = NULL;
  ClassBytes* res = NULL;
  jarFile = argv[i];

  vm->setClasspath(jarFile);
  
  bytes = Reader::openFile(vm->bootstrapLoader, jarFile);

  if (bytes == NULL) {
    printf("Unable to access jarfile %s\n", jarFile);
    return;
  }

  mvm::BumpPtrAllocator allocator;
  ZipArchive* archive = new(allocator, "TempZipArchive")
      ZipArchive(bytes, allocator);
  if (archive->getOfscd() != -1) {
    ZipFile* file = archive->getFile(PATH_MANIFEST);
    if (file != NULL) {
      res = new (allocator, file->ucsize) ClassBytes(file->ucsize);
      int ok = archive->readFile(res, file);
      if (ok) {
        char* mainClass = findInformation(vm, res, MAIN_CLASS,
                                          LENGTH_MAIN_CLASS);
        if (mainClass == NULL) {
          mainClass = findInformation(vm, res, MAIN_LOWER_CLASS,
                                      LENGTH_MAIN_CLASS);
        }
        if (mainClass != NULL) {
          className = mainClass;
        } else {
          printf("No Main-Class:  in Manifest of archive %s.\n", jarFile);
        }
      } else {
        printf("Can't extract Manifest file from archive %s\n", jarFile);
      }
    } else {
      printf("Can't find Manifest file in archive %s\n", jarFile);
    }
  } else {
    printf("Can't find archive %s\n", jarFile);
  }
}

void ClArgumentsInfo::nyi() {
  fprintf(stdout, "Not yet implemented\n");
}

void ClArgumentsInfo::printVersion() {
  fprintf(stdout, "J3 for Java 1.1 -- 1.5\n");
}

void ClArgumentsInfo::printInformation() {
  fprintf(stdout, 
  "Usage: j3 [-options] class [args...] (to execute a class)\n"
   "or  j3 [-options] -jar jarfile [args...]\n"
           "(to execute a jar file) where options include:\n"
    "-cp <class search path of directories and zip/jar files>\n"
    "-classpath <class search path of directories and zip/jar files>\n"
    "              A : separated list of directories, JAR archives,\n"
    "              and ZIP archives to search for class files.\n"
    "-D<name>=<value>\n"
    "              set a system property\n"
    "-verbose[:class|gc|jni]\n"
    "              enable verbose output\n"
    "-version      print product version and exit\n"
    "-version:<value>\n"
    "              require the specified version to run\n"
    "-showversion  print product version and continue\n"
    "-jre-restrict-search | -jre-no-restrict-search\n"
    "              include/exclude user private JREs in the version search\n"
    "-? -help      print this help message\n"
    "-X            print help on non-standard options\n"
    "-ea[:<packagename>...|:<classname>]\n"
    "-enableassertions[:<packagename>...|:<classname>]\n"
    "              enable assertions\n"
    "-da[:<packagename>...|:<classname>]\n"
    "-disableassertions[:<packagename>...|:<classname>]\n"
    "              disable assertions\n"
    "-esa | -enablesystemassertions\n"
    "              enable system assertions\n"
    "-dsa | -disablesystemassertions\n"
    "              disable system assertions\n"
    "-agentlib:<libname>[=<options>]\n"
    "              load native agent library <libname>, e.g. -agentlib:hprof\n"
    "                see also, -agentlib:jdwp=help and -agentlib:hprof=help\n"
    "-agentpath:<pathname>[=<options>]\n"
    "              load native agent library by full pathname\n"
    "-javaagent:<jarpath>[=<options>]\n"
    "       load Java programming language agent, see java.lang.instrument\n");
}

void ClArgumentsInfo::readArgs(Jnjvm* vm) {
  className = 0;
  appArgumentsPos = 0;
  sint32 i = 1;
  if (i == argc) printInformation();
  while (i < argc) {
    char* cur = argv[i];
    if (!(strcmp(cur, "-classpath"))) {
      ++i;
      if (i == argc) printInformation();
      else vm->setClasspath(argv[i]);
    } else if (!(strcmp(cur, "-cp"))) {
      ++i;
      if (i == argc) printInformation();
      else vm->setClasspath(argv[i]);
    } else if (!(strncmp(cur, "-D", 2))) {
      uint32 len = strlen(cur);
      if (len == 2) {
        printInformation();
      } else {
        char* key = &cur[2];
        char* value = strchr(key, '=');
        if (!value) {
          printInformation();
          return;
        } else {
          value[0] = 0;
          vm->addProperty(key, &value[1]);
        }
      }
    } else if (!(strncmp(cur, "-Xbootclasspath:", 16))) {
      uint32 len = strlen(cur);
      if (len == 16) {
        printInformation();
      } else {
        char* path = &cur[16];
        vm->bootstrapLoader->analyseClasspathEnv(path);
      }
    } else if (!(strcmp(cur, "-enableassertions"))) {
      nyi();
    } else if (!(strcmp(cur, "-ea"))) {
      nyi();
    } else if (!(strcmp(cur, "-disableassertions"))) {
      nyi();
    } else if (!(strcmp(cur, "-da"))) {
      nyi();
    } else if (!(strcmp(cur, "-enablesystemassertions"))) {
      nyi();
    } else if (!(strcmp(cur, "-esa"))) {
      nyi();
    } else if (!(strcmp(cur, "-disablesystemassertions"))) {
      nyi();
    } else if (!(strcmp(cur, "-dsa"))) {
      nyi();
    } else if (!(strcmp(cur, "-jar"))) {
      ++i;
      if (i == argc) {
        printInformation();
      } else {
        extractClassFromJar(vm, argc, argv, i);
        appArgumentsPos = i;
        return;
      }
    } else if (!(strcmp(cur, "-jre-restrict-research"))) {
      nyi();
    } else if (!(strcmp(cur, "-jre-no-restrict-research"))) {
      nyi();
    } else if (!(strcmp(cur, "-noclassgc"))) {
      nyi();
    } else if (!(strcmp(cur, "-ms"))) {
      nyi();
    } else if (!(strcmp(cur, "-mx"))) {
      nyi();
    } else if (!(strcmp(cur, "-ss"))) {
      nyi();
    } else if (!(strcmp(cur, "-verbose"))) {
      nyi();
    } else if (!(strcmp(cur, "-verbose:class"))) {
      nyi();
    } else if (!(strcmp(cur, "-verbosegc"))) {
      nyi();
    } else if (!(strcmp(cur, "-verbose:gc"))) {
      mvm::Collector::verbose = 1;
    } else if (!(strcmp(cur, "-verbose:jni"))) {
      nyi();
    } else if (!(strcmp(cur, "-version"))) {
      printVersion();
    } else if (!(strcmp(cur, "-showversion"))) {
      nyi();
    } else if (!(strcmp(cur, "-?"))) {
      printInformation();
    } else if (!(strcmp(cur, "-help"))) {
      printInformation();
    } else if (!(strcmp(cur, "-X"))) {
      nyi();
    } else if (!(strcmp(cur, "-agentlib"))) {
      nyi();
    } else if (!(strcmp(cur, "-agentpath"))) {
      nyi();
    } else if (cur[0] == '-') {
    } else if (!(strcmp(cur, "-javaagent"))) {
      javaAgent(cur);
    } else {
      className = cur;
      appArgumentsPos = i;
      return;
    }
    ++i;
  }
}


JnjvmClassLoader* Jnjvm::loadAppClassLoader() {
  JavaObject* loader = 0;
  llvm_gcroot(loader, 0);
  
  if (appClassLoader == NULL) {
    UserClass* cl = upcalls->newClassLoader;
    loader = upcalls->getSystemClassLoader->invokeJavaObjectStatic(this, cl);
    appClassLoader = JnjvmClassLoader::getJnjvmLoaderFromJavaObject(loader,
                                                                    this);
    if (argumentsInfo.jarFile) {
      appClassLoader->loadLibFromJar(this, argumentsInfo.jarFile,
                                     argumentsInfo.className);
    } else if (argumentsInfo.className) {
      appClassLoader->loadLibFromFile(this, argumentsInfo.className);
    }
  }
  return appClassLoader;
}

void Jnjvm::loadBootstrap() {
  JavaObject* obj = NULL;
  JavaObject* javaLoader = NULL;
  llvm_gcroot(obj, 0);
  llvm_gcroot(javaLoader, 0);
  JnjvmBootstrapLoader* loader = bootstrapLoader;
  
  // First create system threads.
  finalizerThread = new FinalizerThread(this);
  finalizerThread->start(
      (void (*)(mvm::Thread*))FinalizerThread::finalizerStart);
    
  referenceThread = new ReferenceThread(this);
  referenceThread->start(
      (void (*)(mvm::Thread*))ReferenceThread::enqueueStart);
  
  // Initialise the bootstrap class loader if it's not
  // done already.
  if (bootstrapLoader->upcalls->newString == NULL) {
    bootstrapLoader->upcalls->initialiseClasspath(bootstrapLoader);
  }
  
#define LOAD_CLASS(cl) \
  cl->resolveClass(); \
  cl->initialiseClass(this);
  
  // If a string belongs to the vm hashmap, we must remove it when
  // it's destroyed. So we define a new VT for strings that will be
  // placed in the hashmap. This VT will have its destructor set so
  // that the string is removed when deallocated.
  upcalls->newString->resolveClass();
  if (JavaString::internStringVT == NULL) {
    JavaVirtualTable* stringVT = upcalls->newString->getVirtualVT();
    uint32 size = upcalls->newString->virtualTableSize * sizeof(word_t);
    
    JavaString::internStringVT = 
      (JavaVirtualTable*)bootstrapLoader->allocator.Allocate(size, "String VT");

    memcpy(JavaString::internStringVT, stringVT, size);
    
    JavaString::internStringVT->destructor = 
      (word_t)JavaString::stringDestructor;

    // Tell the finalizer that this is a native destructor.
    JavaString::internStringVT->operatorDelete = 
      (word_t)JavaString::stringDestructor;
  }
  upcalls->newString->initialiseClass(this);

  // The initialization code of the classes initialized below may require
  // to get the Java thread, so we create the Java thread object first.
  upcalls->InitializeThreading(this);
  
  LOAD_CLASS(upcalls->newClass);
  LOAD_CLASS(upcalls->newConstructor);
  LOAD_CLASS(upcalls->newField);
  LOAD_CLASS(upcalls->newMethod);
  LOAD_CLASS(upcalls->newStackTraceElement);
  LOAD_CLASS(upcalls->boolClass);
  LOAD_CLASS(upcalls->byteClass);
  LOAD_CLASS(upcalls->charClass);
  LOAD_CLASS(upcalls->shortClass);
  LOAD_CLASS(upcalls->intClass);
  LOAD_CLASS(upcalls->longClass);
  LOAD_CLASS(upcalls->floatClass);
  LOAD_CLASS(upcalls->doubleClass);
  LOAD_CLASS(upcalls->InvocationTargetException);
  LOAD_CLASS(upcalls->ArrayStoreException);
  LOAD_CLASS(upcalls->ClassCastException);
  LOAD_CLASS(upcalls->IllegalMonitorStateException);
  LOAD_CLASS(upcalls->IllegalArgumentException);
  LOAD_CLASS(upcalls->InterruptedException);
  LOAD_CLASS(upcalls->IndexOutOfBoundsException);
  LOAD_CLASS(upcalls->ArrayIndexOutOfBoundsException);
  LOAD_CLASS(upcalls->NegativeArraySizeException);
  LOAD_CLASS(upcalls->NullPointerException);
  LOAD_CLASS(upcalls->SecurityException);
  LOAD_CLASS(upcalls->ClassFormatError);
  LOAD_CLASS(upcalls->ClassCircularityError);
  LOAD_CLASS(upcalls->NoClassDefFoundError);
  LOAD_CLASS(upcalls->UnsupportedClassVersionError);
  LOAD_CLASS(upcalls->NoSuchFieldError);
  LOAD_CLASS(upcalls->NoSuchMethodError);
  LOAD_CLASS(upcalls->InstantiationError);
  LOAD_CLASS(upcalls->IllegalAccessError);
  LOAD_CLASS(upcalls->IllegalAccessException);
  LOAD_CLASS(upcalls->VerifyError);
  LOAD_CLASS(upcalls->ExceptionInInitializerError);
  LOAD_CLASS(upcalls->LinkageError);
  LOAD_CLASS(upcalls->AbstractMethodError);
  LOAD_CLASS(upcalls->UnsatisfiedLinkError);
  LOAD_CLASS(upcalls->InternalError);
  LOAD_CLASS(upcalls->OutOfMemoryError);
  LOAD_CLASS(upcalls->StackOverflowError);
  LOAD_CLASS(upcalls->UnknownError);
  LOAD_CLASS(upcalls->ClassNotFoundException); 
  LOAD_CLASS(upcalls->ArithmeticException); 
  LOAD_CLASS(upcalls->InstantiationException);
  LOAD_CLASS(upcalls->SystemClass);
#undef LOAD_CLASS

  // Implementation-specific end-of-bootstrap initialization
  upcalls->InitializeSystem(this);

  loadAppClassLoader();
  obj = JavaThread::get()->currentThread();
  javaLoader = appClassLoader->getJavaClassLoader();

  upcalls->setContextClassLoader->invokeIntSpecial(this, upcalls->newThread,
                                                   obj, &javaLoader);
  // load and initialise math since it is responsible for dlopen'ing 
  // libjavalang.so and we are optimizing some math operations
  UserCommonClass* math = loader->loadName(loader->mathName, true, true, NULL);
  math->asClass()->initialiseClass(this);
}

void Jnjvm::executeClass(const char* className, ArrayObject* args) {
  JavaObject* exc = NULL;
  JavaObject* obj = NULL;
  JavaObject* handler = NULL;
  
  llvm_gcroot(args, 0);
  llvm_gcroot(exc, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(handler, 0);

  TRY {
    // First try to see if we are a self-contained executable.
    UserClass* cl = appClassLoader->loadClassFromSelf(this, className);
    
    // If not, load the class.
    if (cl == NULL) {
      const UTF8* name = appClassLoader->asciizConstructUTF8(className);
      cl = (UserClass*)appClassLoader->loadName(name, true, true, NULL);
    }
    
    cl->initialiseClass(this);
  
    const UTF8* funcSign = 
      appClassLoader->asciizConstructUTF8("([Ljava/lang/String;)V");
    const UTF8* funcName = appClassLoader->asciizConstructUTF8("main");
    JavaMethod* method = cl->lookupMethod(funcName, funcSign, true, true, 0);
    if (isPublic(method->access)) { 
      method->invokeIntStatic(this, method->classDef, &args);
    } else {
      fprintf(stderr, "Main method not public.\n");
      mvm::System::Exit(1);
    }
  } CATCH {
  } END_CATCH;

  exc = JavaThread::get()->pendingException;
  if (exc != NULL) {
    JavaThread* th = JavaThread::get();
    th->clearException();
    obj = th->currentThread();
    TRY {
      handler = upcalls->getUncaughtExceptionHandler->invokeJavaObjectVirtual(this, upcalls->newThread, obj);
      verifyNull(handler);
      upcalls->uncaughtException->invokeIntVirtual(this, upcalls->uncaughtException->classDef, handler, &obj, &exc);
    } CATCH {
      fprintf(stderr, "Exception in thread \"main\": "
                      "Can not print stack trace.\n");
    } END_CATCH;
    // Program failed. Exit with return code not 0.
    mvm::System::Exit(1);
  }
}

void Jnjvm::executePremain(const char* className, JavaString* args,
                             JavaObject* instrumenter) {
  llvm_gcroot(args, 0);
  llvm_gcroot(instrumenter, 0);
  TRY {
    const UTF8* name = appClassLoader->asciizConstructUTF8(className);
    UserClass* cl = (UserClass*)
        appClassLoader->loadName(name, true, true, NULL);
    cl->initialiseClass(this);
  
    const UTF8* funcSign = appClassLoader->asciizConstructUTF8(
      "(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V");
    const UTF8* funcName = appClassLoader->asciizConstructUTF8("premain");
    JavaMethod* method = cl->lookupMethod(funcName, funcSign, true, true, 0);
  
    method->invokeIntStatic(this, method->classDef, &args, &instrumenter);
  } IGNORE;
}

void Jnjvm::mainJavaStart(JavaThread* thread) {

  JavaString* str = NULL;
  JavaObject* instrumenter = NULL;
  ArrayObject* args = NULL;
  JavaObject* exc = NULL;

  llvm_gcroot(str, 0);
  llvm_gcroot(instrumenter, 0);
  llvm_gcroot(args, 0);
  llvm_gcroot(exc, 0);

  Jnjvm* vm = thread->getJVM();
  vm->argumentsInfo.readArgs(vm);
  if (vm->argumentsInfo.className == NULL) {
    vm->threadSystem.leave();
    return;
  }

  int pos = vm->argumentsInfo.appArgumentsPos;  
  vm->argumentsInfo.argv = vm->argumentsInfo.argv + pos - 1;
  vm->argumentsInfo.argc = vm->argumentsInfo.argc - pos + 1;

  vm->mainThread = thread;

  TRY {
    vm->loadBootstrap();
  } CATCH {
    exc = JavaThread::get()->pendingException;
  } END_CATCH;

  if (exc != NULL) {
    fprintf(stderr, "Exception %s while bootstrapping VM.\n",
        UTF8Buffer(JavaObject::getClass(exc)->name).cString());
  } else {
    ClArgumentsInfo& info = vm->argumentsInfo;
  
    if (info.agents.size()) {
      assert(0 && "implement me");
      instrumenter = 0;//createInstrumenter();
      for (std::vector< std::pair<char*, char*> >::iterator i = 
           info.agents.begin(), e = info.agents.end(); i!= e; ++i) {
        str = vm->asciizToStr(i->second);
        vm->executePremain(i->first, str, instrumenter);
      }
    }
    
    UserClassArray* array = vm->bootstrapLoader->upcalls->ArrayOfString;
    args = (ArrayObject*)array->doNew(info.argc - 2, vm);
    for (int i = 2; i < info.argc; ++i) {
      ArrayObject::setElement(args, (JavaObject*)vm->asciizToStr(info.argv[i]), i - 2);
    }

    vm->executeClass(info.className, args);
  }
  vm->threadSystem.leave();
}

void ThreadSystem::leave() {
  nonDaemonLock.lock();
  --nonDaemonThreads;
  if (nonDaemonThreads == 0) mvm::Thread::get()->MyVM->exit();
  nonDaemonLock.unlock();  
}

void ThreadSystem::enter() {
  nonDaemonLock.lock();
  ++nonDaemonThreads;
  nonDaemonLock.unlock();  
}

void Jnjvm::runApplication(int argc, char** argv) {
  argumentsInfo.argc = argc;
  argumentsInfo.argv = argv;
  mainThread = new JavaThread(this);
  mainThread->start((void (*)(mvm::Thread*))mainJavaStart);
}

Jnjvm::Jnjvm(mvm::BumpPtrAllocator& Alloc,
             mvm::CompiledFrames** frames,
             JnjvmBootstrapLoader* loader) : 
  VirtualMachine(Alloc, frames), lockSystem(Alloc) {

  classpath = getenv("CLASSPATH");
  if (classpath == NULL) classpath = ".";
  
  appClassLoader = NULL;
  jniEnv = &JNI_JNIEnvTable;
  javavmEnv = &JNI_JavaVMTable;
  
  bootstrapLoader = loader;
  upcalls = bootstrapLoader->upcalls;
  throwable = upcalls->newThrowable;
}

Jnjvm::~Jnjvm() {
}

ArrayUInt16* Jnjvm::asciizToArray(const char* asciiz) {
  ArrayUInt16* tmp = NULL;
  llvm_gcroot(tmp, 0);

  uint32 size = strlen(asciiz);
  tmp = (ArrayUInt16*)upcalls->ArrayOfChar->doNew(size, this);
  
  for (uint32 i = 0; i < size; i++) {
    ArrayUInt16::setElement(tmp, asciiz[i], i);
  }
  return tmp;
}

void Jnjvm::startCollection() {
  finalizerThread->FinalizationQueueLock.acquire();
  referenceThread->ToEnqueueLock.acquire();
  referenceThread->SoftReferencesQueue.acquire();
  referenceThread->WeakReferencesQueue.acquire();
  referenceThread->PhantomReferencesQueue.acquire();
}
  
void Jnjvm::endCollection() {
  finalizerThread->FinalizationQueueLock.release();
  referenceThread->ToEnqueueLock.release();
  referenceThread->SoftReferencesQueue.release();
  referenceThread->WeakReferencesQueue.release();
  referenceThread->PhantomReferencesQueue.release();
  finalizerThread->FinalizationCond.broadcast();
  referenceThread->EnqueueCond.broadcast();
}
  
void Jnjvm::scanWeakReferencesQueue(word_t closure) {
  referenceThread->WeakReferencesQueue.scan(referenceThread, closure);
}
  
void Jnjvm::scanSoftReferencesQueue(word_t closure) {
  referenceThread->SoftReferencesQueue.scan(referenceThread, closure);
}
  
void Jnjvm::scanPhantomReferencesQueue(word_t closure) {
  referenceThread->PhantomReferencesQueue.scan(referenceThread, closure);
}

void Jnjvm::scanFinalizationQueue(word_t closure) {
  finalizerThread->scanFinalizationQueue(closure);
}

void Jnjvm::addFinalizationCandidate(gc* object) {
  llvm_gcroot(object, 0);
  finalizerThread->addFinalizationCandidate(object);
}

size_t Jnjvm::getObjectSize(gc* object) {
  // TODO: because this is called during GC, there is no need to do
  // llvm_gcroot. For clarity, it may be useful to have a special type
  // in this case.
  size_t size = 0;
  JavaObject* src = (JavaObject*)object;
  if (VMClassLoader::isVMClassLoader(src)) {
    size = sizeof(VMClassLoader);
  } else if (VMStaticInstance::isVMStaticInstance(src)) {
    size = sizeof(VMStaticInstance);
  } else {
    CommonClass* cl = JavaObject::getClass(src);
    if (cl->isArray()) {
      UserClassArray* array = cl->asArrayClass();
      UserCommonClass* base = array->baseClass();
      uint32 logSize = base->isPrimitive() ? 
        base->asPrimitiveClass()->logSize : (sizeof(JavaObject*) == 8 ? 3 : 2); 

      size = sizeof(JavaObject) + sizeof(ssize_t) + 
                    (JavaArray::getSize(src) << logSize);
    } else {
      assert(cl->isClass() && "Not a class!");
      size = cl->asClass()->getVirtualSize();
    }
  }
  return size;
}

const char* Jnjvm::getObjectTypeName(gc* object) {
  JavaObject* src = (JavaObject*)object;
  if (VMClassLoader::isVMClassLoader(src)) {
    return "VMClassLoader";
  } else if (VMStaticInstance::isVMStaticInstance(src)) {
    return "VMStaticInstance";
  } else {
    CommonClass* cl = JavaObject::getClass(src);
    // This code is only used for debugging on a fatal error. It is fine to
    // allocate in the C++ heap.
    return (new UTF8Buffer(cl->name))->cString();
  }
}

// Helper function to run J3 without JIT.
extern "C" int StartJnjvmWithoutJIT(int argc, char** argv, char* mainClass) {
  mvm::Collector::initialise(argc, argv);
 
  mvm::ThreadAllocator allocator; 
  char** newArgv = (char**)allocator.Allocate((argc + 1) * sizeof(char*));
  memcpy(newArgv + 1, argv, argc * sizeof(char*));
  newArgv[0] = newArgv[1];
  newArgv[1] = mainClass;
 
  mvm::BumpPtrAllocator Allocator;
  JavaCompiler* Comp = new JavaCompiler();
  JnjvmBootstrapLoader* loader = new(Allocator, "Bootstrap loader")
    JnjvmBootstrapLoader(Allocator, Comp, true);
  Jnjvm* vm = new(Allocator, "VM") Jnjvm(Allocator, NULL, loader);

  vm->runApplication(argc + 1, newArgv);
  vm->waitForExit();
  
  return 0; 
}

void Jnjvm::printMethod(mvm::FrameInfo* FI, word_t ip, word_t addr) {
  if (FI->Metadata == NULL) {
    mvm::MethodInfoHelper::print(ip, addr);
    return;
  }
  JavaMethod* meth = (JavaMethod*)FI->Metadata;

  fprintf(stderr, "; %p (%p) in %s.%s (line %d, bytecode %d, code start %p)",
          (void*)ip,
          (void*)addr,
          UTF8Buffer(meth->classDef->name).cString(),
          UTF8Buffer(meth->name).cString(),
          meth->lookupLineNumber(FI),
          FI->SourceIndex, meth->code);

  fprintf(stderr, "\n");
}
