//===- ClasspathVMThread.cpp - GNU classpath java/lang/VMThread -----------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "types.h"

#include "Classpath.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

using namespace j3;

extern "C" {

// Never throws.
// Never calls Java code.
JNIEXPORT JavaObject* JNICALL Java_java_lang_VMThread_currentThread(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz
#endif
) {
  return JavaThread::get()->currentThread();
}

void start(JavaThread* thread) {

  JavaObjectVMThread* vmThread = NULL;
  JavaObject* javaThread = NULL;
  llvm_gcroot(vmThread, 0);
  llvm_gcroot(javaThread, 0);

  Jnjvm* vm = thread->getJVM();
  // Wait some time to let the creator initialise these fields.
  while ((thread->javaThread == NULL) || (thread->vmThread == NULL)) {
    mvm::Thread::yield();
  }

  // Ok, now that the thread is created we can set the the value of vmdata,
  // which is the JavaThread object.
  vmThread = (JavaObjectVMThread*)thread->vmThread;
  assert(vmThread && "Didn't fix the vmThread of a j3 thread");
  assert(vmThread->getVirtualTable());
  JavaObjectVMThread::setVmdata(vmThread, thread);
  
  UserClass* vmthClass = (UserClass*)JavaObject::getClass(vmThread); 
  
  javaThread = thread->javaThread;
  assert(javaThread && "Didn't fix the javaThread of a j3 thread");
  // If the thread is not a daemon, it is added to the list of threads to
  // wait until exit.
  bool isDaemon = vm->upcalls->daemon->getInstanceInt8Field(javaThread);

  if (!isDaemon) {
    vm->threadSystem.enter();
  }
 
  assert(vmThread->getVirtualTable());
  assert(javaThread->getVirtualTable());
  // Run the VMThread::run function
  vm->upcalls->runVMThread->invokeIntSpecial(vm, vmthClass, vmThread);
 
  // Remove the thread from the list.
  if (!isDaemon) {
    vm->threadSystem.leave();
  }
}

JNIEXPORT void JNICALL Java_java_lang_VMThread_start(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* vmThread, sint64 stackSize) {

  JavaObject* javaThread = 0;
  llvm_gcroot(vmThread, 0);
  llvm_gcroot(javaThread, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  
  // Classpath has set this field.
  javaThread = vm->upcalls->assocThread->getInstanceObjectField(vmThread);
  assert(javaThread && "VMThread with no Java equivalent");
 
  JavaThread* th = new JavaThread(vm);
  if (!th) vm->outOfMemoryError();
  th->start((void (*)(mvm::Thread*))start);
  // Now that the thread has been created, initialise its object fields.
  th->initialise(javaThread, vmThread);

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_VMThread_interrupt(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* vmthread) {

  llvm_gcroot(vmthread, 0);  

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaField* field = vm->upcalls->vmdataVMThread; 
  
  // It's possible that the thread to be interrupted has not finished
  // its initialization. Wait until the initialization is done.
  while (field->getInstanceObjectField(vmthread) == 0)
    mvm::Thread::yield();
  
  JavaThread* th = (JavaThread*)field->getInstanceObjectField(vmthread);
  th->lockingThread.interruptFlag = 1;
  mvm::FatLock* lock = th->lockingThread.waitsOn;

  // If the thread is blocked on a wait. We also verify nextWaiting in case
  // the thread has been notified.
  if (lock && th->lockingThread.nextWaiting) {
    th->lockingThread.state = mvm::LockingThread::StateInterrupted;
  
    // Make sure the thread is waiting.
    uint32 locked = 0;
    while (true) {
      locked = (lock->tryAcquire() == 0);
      if (locked || (lock->getOwner() != th && lock->getOwner() != 0))
        break;
      else mvm::Thread::yield();
    }
    
    // Interrupt the thread.
    th->lockingThread.varcond.signal();

    // Release the lock if we acquired it.
    if (locked) lock->release(lock->getAssociatedObject(), vm->lockSystem);
  }

  // Here we could also raise a signal for interrupting I/O
  
  END_NATIVE_EXCEPTION
}

// Never throws.
// Never calls Java code.
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_interrupted(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  JavaThread* th = JavaThread::get();
  uint32 interrupt = th->lockingThread.interruptFlag;
  th->lockingThread.interruptFlag = 0;
  return (jboolean)interrupt;
}

// Never throws.
// Never calls Java code.
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_isInterrupted(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* vmthread) {
  
  llvm_gcroot(vmthread, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();
  JavaField* field = vm->upcalls->vmdataVMThread;
  JavaThread* th = (JavaThread*)field->getInstanceObjectField(vmthread);
  return (jboolean)th->lockingThread.interruptFlag;
}

// Never throws.
// Never calls Java code.
JNIEXPORT void JNICALL Java_java_lang_VMThread_nativeSetPriority(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* vmthread, jint prio) {
  // Currently not implemented
  llvm_gcroot(vmthread, 0);
}

// Never throws.
// Never calls Java code.
JNIEXPORT void JNICALL Java_java_lang_VMThread_nativeStop(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* vmthread, JavaObject* exc) {
  // Currently not implemented
  llvm_gcroot(vmthread, 0);
  llvm_gcroot(exc, 0);
}

// Never throws.
// Never calls Java code.
JNIEXPORT void JNICALL Java_java_lang_VMThread_yield(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
) {
  mvm::Thread::yield();
}

}
