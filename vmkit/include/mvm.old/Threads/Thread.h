//===---------------- Threads.h - Micro-vm threads ------------------------===//
//
//                        The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_THREAD_H
#define MVM_THREAD_H

#include <cassert>
#include <cstdio>
#include <stdlib.h>

#include "debug.h"
#include "types.h"

#include "mvm/System.h"


namespace mvm {

class FrameInfo;
class VirtualMachine;

/// CircularBase - This class represents a circular list. Classes that extend
/// this class automatically place their instances in a circular list.
///
class CircularBase {
  /// _next - The next object in the list.
  ///
  CircularBase  *_next;

  /// _prev - The previous object in the list.
  ///
  CircularBase  *_prev;
public:
  
  /// ~CircularBase - Give the class a home.
  ///
  virtual ~CircularBase() {}

  /// next - Get the next object in the list.
  ///
  inline CircularBase *next() { return _next; }

  /// prev - Get the previous object in the list.
  ///
  inline CircularBase *prev() { return _prev; }

  /// next - Set the next object in the list.
  ///
  inline void next(CircularBase *n) { _next = n; }

  /// prev - Set the previous object in the list.
  ///
  inline void prev(CircularBase *p) { _prev = p; }

  /// CricularBase - Creates the object as a single element in the list.
  ///
  inline CircularBase() { alone(); }

  /// CircularBase - Creates the object and place it in the given list.
  ///
  inline explicit CircularBase(CircularBase *p) { append(p); }

  /// remove - Remove the object from its list.
  ///
  inline void remove() {
    _prev->_next = _next; 
    _next->_prev = _prev;
    alone();
  }

  /// append - Add the object in the list.
  ///
  inline void append(CircularBase *p) { 
    _prev = p;
    _next = p->_next;
    _next->_prev = this;
    _prev->_next = this;
  }

  /// alone - Set the object as being part of a new empty list.
  ///
  inline void alone() { _prev = _next = this; }

  /// print - Print the list for debug purposes.
  void print() {
    CircularBase* temp = this;
    do {
      fprintf(stderr, "%p -> ", (void*)temp);
      temp = temp->next();
    } while (temp != this);
    fprintf(stderr, "\n");
  }
};


class KnownFrame {
public:
  word_t currentFP;
  word_t currentIP;
  KnownFrame* previousFrame;
};


class ExceptionBuffer;

/// Thread - This class is the base of custom virtual machines' Thread classes.
/// It provides static functions to manage threads. An instance of this class
/// contains all thread-specific informations.
class Thread : public CircularBase {
public:
  Thread() {
    lastExceptionBuffer = 0;
    lastKnownFrame = 0;
  }

  /// yield - Yield the processor to another thread.
  ///
  static void yield(void);
  
  /// kill - Kill the thread with the given pid by sending it a signal.
  ///
  static int kill(void* tid, int signo);
  
  /// kill - Kill the given thread by sending it a signal.
  ///
  int kill(int signo);
  
  /// exit - Exit the current thread.
  ///
  static void exit(int value);
  
  /// start - Start the execution of a thread.
  ///
  virtual int start(void (*fct)(mvm::Thread*));
  
  uint64_t getThreadID() {
    return (uint64_t)this;
  }
 
public:

  /// IsolateID - The Isolate ID of the thread's VM.
  size_t IsolateID;

  /// MyVM - The VM attached to this Thread.
  VirtualMachine* MyVM;

  /// baseSP - The base stack pointer.
  ///
  word_t baseSP;
 
  /// doYield - Flag to tell the thread to yield for GC reasons.
  ///
  bool doYield;

  /// inRV - Flag to tell that the thread is being part of a rendezvous.
  ///
  bool inRV;

  /// joinedRV - Flag to tell that the thread has joined a rendezvous.
  ///
  bool joinedRV;

  /// get - Get the thread specific data of the current thread.
  ///
  static Thread* get() {
    return (Thread*)(System::GetCallerAddress() & System::GetThreadIDMask());
  }
  
private:
  
  /// lastSP - If the thread is running native code that can not be
  /// interrupted, lastSP is not null and contains the value of the
  /// stack pointer before entering native.
  ///
  word_t lastSP;
 
  /// internalThreadID - The implementation specific thread id.
  ///
  void* internalThreadID;
  
  /// internalThreadStart - The implementation sepcific thread starter
  /// function.
  ///
  static void internalThreadStart(mvm::Thread* th);

  /// internalClearException - Clear any pending exception.
  ///
  virtual void internalClearException() {}

public:
 
  /// tracer - Does nothing. Used for child classes which may defined
  /// a tracer.
  ///
  virtual void tracer(word_t closure) {}
  void scanStack(word_t closure);
  
  word_t getLastSP() { return lastSP; }
  void  setLastSP(word_t V) { lastSP = V; }
  
  void joinRVBeforeEnter();
  void joinRVAfterLeave(word_t savedSP);

  void enterUncooperativeCode(uint16_t level = 0) __attribute__ ((noinline));
  void enterUncooperativeCode(word_t SP);
  void leaveUncooperativeCode();
  word_t waitOnSP();


  /// clearException - Clear any pending exception of the current thread.
  void clearException() {
    internalClearException();
  }

  bool isMvmThread() {
    if (!baseAddr) return false;
    else return (((word_t)this) & System::GetMvmThreadMask()) == baseAddr;
  }

  /// baseAddr - The base address for all threads.
  static word_t baseAddr;

  /// OverflowMask - Apply this mask to implement overflow checks. For
  /// efficiency, we lower the available size of the stack: it can never go
  /// under 0xC0000
  ///
  static const uint64_t StackOverflowMask = 0xC0000;

  /// stackOverflow - Returns if there is a stack overflow in Java land.
  ///
  bool stackOverflow() {
    return (System::GetCallerAddress() & StackOverflowMask) == 0;
  }

  /// operator new - Allocate the Thread object as well as the stack for this
  /// Thread. The thread object is inlined in the stack.
  ///
  void* operator new(size_t sz);
  void operator delete(void* th) { UNREACHABLE(); }
  
  /// releaseThread - Free the stack so that another thread can use it.
  ///
  static void releaseThread(mvm::Thread* th);

  /// routine - The function to invoke when the thread starts.
  ///
  void (*routine)(mvm::Thread*);
 
  /// printBacktrace - Print the backtrace.
  ///
  void printBacktrace();
 
  /// getFrameContext - Fill the buffer with frames currently on the stack.
  ///
  void getFrameContext(word_t* buffer);
  
  /// getFrameContextLength - Get the length of the frame context.
  ///
  uint32_t getFrameContextLength();

  /// lastKnownFrame - The last frame that we know of, before resuming to JNI.
  ///
  KnownFrame* lastKnownFrame;
  
  /// lastExceptionBuffer - The last exception buffer on this thread's stack.
  ///
  ExceptionBuffer* lastExceptionBuffer;

  void internalThrowException();

  void startKnownFrame(KnownFrame& F) __attribute__ ((noinline));
  void endKnownFrame();
  void startUnknownFrame(KnownFrame& F) __attribute__ ((noinline));
  void endUnknownFrame();
};

class ExceptionBuffer {
public:
  ExceptionBuffer() {
    init();
  }

  void init() {
    Thread* th = Thread::get();
    previousBuffer = th->lastExceptionBuffer;
    th->lastExceptionBuffer = this;
  }

  ~ExceptionBuffer() {
    remove();
  }

  void remove() {
    Thread* th = Thread::get();
    assert(th->lastExceptionBuffer == this && "Wrong exception buffer");
    th->lastExceptionBuffer = previousBuffer;
  }

  jmp_buf buffer;
  ExceptionBuffer* previousBuffer;
};

/// StackWalker - This class walks the stack of threads, returning a FrameInfo
/// object at each iteration.
///
class StackWalker {
public:
  word_t addr;
  word_t ip;
  KnownFrame* frame;
  mvm::Thread* thread;

  StackWalker(mvm::Thread* th) __attribute__ ((noinline));
  void operator++();
  word_t operator*();
  FrameInfo* get();

};


} // end namespace mvm
#endif // MVM_THREAD_H
