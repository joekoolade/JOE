//===----------- JavaThread.h - Java thread description -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_THREAD_H
#define JNJVM_JAVA_THREAD_H

#include "mvm/Threads/Cond.h"
#include "mvm/Threads/Locks.h"
#include "mvm/Threads/ObjectLocks.h"
#include "mvm/Threads/Thread.h"

#include "MutatorThread.h"

#include "JavaObject.h"
#include "JNIReferences.h"

namespace j3 {

class Class;
class JavaMethod;
class JavaObject;
class Jnjvm;


#define BEGIN_NATIVE_EXCEPTION(level)
#define END_NATIVE_EXCEPTION

#define BEGIN_JNI_EXCEPTION \
  JavaThread* th = JavaThread::get(); \
  word_t SP = th->getLastSP(); \
  th->leaveUncooperativeCode(); \
  mvm::KnownFrame Frame; \
  th->startKnownFrame(Frame); \
  TRY {

#define END_JNI_EXCEPTION \
  } CATCH { \
    th->throwFromJNI(SP); \
  } END_CATCH;

#define RETURN_FROM_JNI(a) {\
  th->endKnownFrame(); \
  th->enterUncooperativeCode(SP); \
  return (a); } \

#define RETURN_VOID_FROM_JNI {\
  th->endKnownFrame(); \
  th->enterUncooperativeCode(SP); \
  return; } \


/// JavaThread - This class is the internal representation of a Java thread.
/// It maintains thread-specific information such as its state, the current
/// exception if there is one, the layout of the stack, etc.
///
class JavaThread : public mvm::MutatorThread {
public:
  
  /// jniEnv - The JNI environment of the thread.
  ///
  void* jniEnv;
  
  /// pendingException - The Java exception currently pending.
  ///
  JavaObject* pendingException;

  /// javaThread - The Java representation of this thread.
  ///
  JavaObject* javaThread;

  /// vmThread - The VMThread object of this thread.
  ///
  JavaObject* vmThread;

  mvm::LockingThread lockingThread;
  
  /// currentAddedReferences - Current number of added local references.
  ///
  uint32_t* currentAddedReferences;

  /// localJNIRefs - List of local JNI references.
  ///
  JNILocalReferences* localJNIRefs;


  JavaObject** pushJNIRef(JavaObject* obj) {
    llvm_gcroot(obj, 0);
    if (!obj) return 0;
   
    ++(*currentAddedReferences);
    return localJNIRefs->addJNIReference(this, obj);

  }

  /// tracer - Traces GC-objects pointed by this thread object.
  ///
  virtual void tracer(word_t closure);

  /// JavaThread - Empty constructor, used to get the VT.
  ///
  JavaThread() {
  }

  /// ~JavaThread - Delete any potential malloc'ed objects used by this thread.
  ///
  ~JavaThread();
  
  /// JavaThread - Creates a Java thread.
  ///
  JavaThread(Jnjvm* isolate);

  void initialise(JavaObject* thread, JavaObject* vmth);
  
  /// get - Get the current thread as a J3 object.
  ///
  static JavaThread* get() {
    return (JavaThread*)mvm::Thread::get();
  }

  /// getJVM - Get the Java VM in which this thread executes.
  ///
  Jnjvm* getJVM() {
    return (Jnjvm*)MyVM;
  }

  /// currentThread - Return the current thread as a Java object.
  ///
  JavaObject* currentThread() {
    return javaThread;
  }
 
  /// throwException - Throw the given exception in the current thread.
  ///
  void throwException(JavaObject* obj);

  /// throwPendingException - Throw a pending exception.
  ///
  void throwPendingException();
  
  /// getJavaException - Return the pending exception.
  ///
  JavaObject* getJavaException() {
    return pendingException;
  }

  /// throwFromJNI - Throw an exception after executing JNI code.
  ///
  void throwFromJNI(word_t SP) {
    endKnownFrame();
    enterUncooperativeCode(SP);
  }
  
  /// throwFromNative - Throw an exception after executing Native code.
  ///
  void throwFromNative() {
  }
  
  /// throwFromJava - Throw an exception after executing Java code.
  ///
  void throwFromJava() {
    throwPendingException();
  }

  /// startJava - Interesting, but actually does nothing :)
  void startJava() {}
  
  /// endJava - Interesting, but actually does nothing :)
  void endJava() {}

  /// startJNI - Record that we are entering native code.
  ///
  void startJNI();

  void endJNI();

  /// getCallingMethod - Get the Java method in the stack at the specified
  /// level.
  ///
  JavaMethod* getCallingMethodLevel(uint32 level);
  
  /// getCallingClassLevel - Get the Java method in the stack at the
  /// specified level.
  ///
  UserClass* getCallingClassLevel(uint32 level);
  
  /// getNonNullClassLoader - Get the first non-null class loader on the
  /// stack.
  ///
  JavaObject* getNonNullClassLoader();
    
  /// printJavaBacktrace - Prints the backtrace of this thread. Only prints
  /// the Java methods on the stack.
  ///
  void printJavaBacktrace();

  /// getJavaFrameContext - Fill the buffer with Java methods currently on
  /// the stack.
  ///
  uint32 getJavaFrameContext(void** buffer);
  
private:
  /// internalClearException - Clear the C++ and Java exceptions
  /// currently pending.
  ///
  virtual void internalClearException() {
    pendingException = NULL;
  }
};

} // end namespace j3

#endif
