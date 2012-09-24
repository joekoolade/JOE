//===---------- ctthread.cc - Thread implementation for VMKit -------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "debug.h"

#include "MvmGC.h"
#include "mvm/MethodInfo.h"
#include "mvm/VirtualMachine.h"
#include "mvm/Threads/Cond.h"
#include "mvm/Threads/Locks.h"
#include "mvm/Threads/Thread.h"

#include <cassert>
#include <cstdio>
#include <errno.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sched.h>
#include <signal.h>
#include <unistd.h>

using namespace mvm;

int Thread::kill(void* tid, int signo) {
  return pthread_kill((pthread_t)tid, signo);
}

int Thread::kill(int signo) {
  return pthread_kill((pthread_t)internalThreadID, signo);
}

void Thread::exit(int value) {
  pthread_exit((void*)value);
}

void Thread::yield(void) {
  Thread* th = mvm::Thread::get();
  if (th->isMvmThread()) {
    if (th->doYield && !th->inRV) {
      th->MyVM->rendezvous.join();
    }
  }
  sched_yield();
}

void Thread::joinRVBeforeEnter() {
  MyVM->rendezvous.joinBeforeUncooperative(); 
}

void Thread::joinRVAfterLeave(word_t savedSP) {
  MyVM->rendezvous.joinAfterUncooperative(savedSP); 
}

void Thread::startKnownFrame(KnownFrame& F) {
  // Get the caller of this function
  word_t cur = System::GetCallerAddress();
  F.previousFrame = lastKnownFrame;
  F.currentFP = cur;
  // This is used as a marker.
  F.currentIP = 0;
  lastKnownFrame = &F;
}

void Thread::endKnownFrame() {
  assert(lastKnownFrame->currentIP == 0);
  lastKnownFrame = lastKnownFrame->previousFrame;
}

void Thread::startUnknownFrame(KnownFrame& F) {
  // Get the caller of this function
  word_t cur = System::GetCallerAddress();
  // Get the caller of the caller.
  cur = System::GetCallerOfAddress(cur);
  F.previousFrame = lastKnownFrame;
  F.currentFP = cur;
  F.currentIP = System::GetIPFromCallerAddress(cur);
  lastKnownFrame = &F;
}

void Thread::endUnknownFrame() {
  assert(lastKnownFrame->currentIP != 0);
  lastKnownFrame = lastKnownFrame->previousFrame;
}

void Thread::internalThrowException() {
  LONGJMP(lastExceptionBuffer->buffer, 1);
}

void Thread::printBacktrace() {
  StackWalker Walker(this);

  while (FrameInfo* FI = Walker.get()) {
    MyVM->printMethod(FI, Walker.ip, Walker.addr);
    ++Walker;
  }
}

void Thread::getFrameContext(word_t* buffer) {
  mvm::StackWalker Walker(this);
  uint32_t i = 0;

  while (word_t ip = *Walker) {
    buffer[i++] = ip;
    ++Walker;
  }
}

uint32_t Thread::getFrameContextLength() {
  mvm::StackWalker Walker(this);
  uint32_t i = 0;

  while (*Walker) {
    ++i;
    ++Walker;
  }
  return i;
}

FrameInfo* StackWalker::get() {
  if (addr == thread->baseSP) return 0;
  ip = System::GetIPFromCallerAddress(addr);
  return thread->MyVM->IPToFrameInfo(ip);
}

word_t StackWalker::operator*() {
  if (addr == thread->baseSP) return 0;
  ip = System::GetIPFromCallerAddress(addr);
  return ip;
}

void StackWalker::operator++() {
  if (addr != thread->baseSP) {
    assert((addr < thread->baseSP) && "Corrupted stack");
    assert((addr < System::GetCallerOfAddress(addr)) && "Corrupted stack");
    if ((frame != NULL) && (addr == frame->currentFP)) {
      assert(frame->currentIP == 0);
      frame = frame->previousFrame;
      assert(frame != NULL);
      assert(frame->currentIP != 0);
      addr = frame->currentFP;
      frame = frame->previousFrame;
    } else {
      addr = System::GetCallerOfAddress(addr);
    }
  }
}

StackWalker::StackWalker(mvm::Thread* th) {
  thread = th;
  frame = th->lastKnownFrame;
  if (mvm::Thread::get() == th) {
    addr = System::GetCallerAddress();
    addr = System::GetCallerOfAddress(addr);
  } else {
    addr = th->waitOnSP();
    if (frame) {
      assert(frame->currentFP >= addr);
    }
    if (frame && (addr == frame->currentFP)) {
      frame = frame->previousFrame;
      assert((frame == NULL) || (frame->currentIP == 0));
    }
  }
  assert(addr && "No address to start with");
}


void Thread::scanStack(word_t closure) {
  StackWalker Walker(this);
  while (FrameInfo* MI = Walker.get()) {
    MethodInfoHelper::scan(closure, MI, Walker.ip, Walker.addr);
    ++Walker;
  }
}

void Thread::enterUncooperativeCode(uint16_t level) {
  if (isMvmThread()) {
    if (!inRV) {
      assert(!lastSP && "SP already set when entering uncooperative code");
      // Get the caller.
      word_t temp = System::GetCallerAddress();
      // Make sure to at least get the caller of the caller.
      ++level;
      while (level--) temp = System::GetCallerOfAddress(temp);
      // The cas is not necessary, but it does a memory barrier.
      __sync_bool_compare_and_swap(&lastSP, 0, temp);
      if (doYield) joinRVBeforeEnter();
      assert(lastSP && "No last SP when entering uncooperative code");
    }
  }
}

void Thread::enterUncooperativeCode(word_t SP) {
  if (isMvmThread()) {
    if (!inRV) {
      assert(!lastSP && "SP already set when entering uncooperative code");
      // The cas is not necessary, but it does a memory barrier.
      __sync_bool_compare_and_swap(&lastSP, 0, SP);
      if (doYield) joinRVBeforeEnter();
      assert(lastSP && "No last SP when entering uncooperative code");
    }
  }
}

void Thread::leaveUncooperativeCode() {
  if (isMvmThread()) {
    if (!inRV) {
      assert(lastSP && "No last SP when leaving uncooperative code");
      word_t savedSP = lastSP;
      // The cas is not necessary, but it does a memory barrier.
      __sync_bool_compare_and_swap(&lastSP, lastSP, 0);
      // A rendezvous has just been initiated, join it.
      if (doYield) joinRVAfterLeave(savedSP);
      assert(!lastSP && "SP has a value after leaving uncooperative code");
    }
  }
}

word_t Thread::waitOnSP() {
  // First see if we can get lastSP directly.
  word_t sp = lastSP;
  if (sp) return sp;
  
  // Then loop a fixed number of iterations to get lastSP.
  for (uint32 count = 0; count < 1000; ++count) {
    sp = lastSP;
    if (sp) return sp;
  }
  
  // Finally, yield until lastSP is not set.
  while ((sp = lastSP) == 0) mvm::Thread::yield();

  assert(sp != 0 && "Still no sp");
  return sp;
}


word_t Thread::baseAddr = 0;

// These could be set at runtime.
#define STACK_SIZE 0x100000
#define NR_THREADS 255

/// StackThreadManager - This class allocates all stacks for threads. Because
/// we want fast access to thread local data, and can not rely on platform
/// dependent thread local storage (eg pthread keys are inefficient, tls is
/// specific to Linux), we put thread local data at the bottom of the 
/// stack. A simple mask computes the thread local data , based on the current
/// stack pointer.
//
/// The stacks are allocated at boot time. They must all be in the memory range
/// 0x?0000000 and Ox(?+1)0000000, so that the thread local data can be computed
/// and threads have a unique ID.
///
class StackThreadManager {
public:
  word_t baseAddr;
  uint32 allocPtr;
  uint32 used[NR_THREADS];
  LockNormal stackLock;

  StackThreadManager() {
    baseAddr = 0;
    word_t ptr = kThreadStart;

    uint32 flags = MAP_PRIVATE | MAP_ANON | MAP_FIXED;
    baseAddr = (word_t)mmap((void*)ptr, STACK_SIZE * NR_THREADS, 
                               PROT_READ | PROT_WRITE, flags, -1, 0);

    if (baseAddr == (word_t) MAP_FAILED) {
      fprintf(stderr, "Can not allocate thread memory\n");
      abort();
    }
 
    // Protect the page after the first page. The first page contains thread
    // specific data. The second page has no access rights to catch stack
    // overflows.
    uint32 pagesize = getpagesize();
    for (uint32 i = 0; i < NR_THREADS; ++i) {
      word_t addr = baseAddr + (i * STACK_SIZE) + pagesize;
      mprotect((void*)addr, pagesize, PROT_NONE);
    }

    memset((void*)used, 0, NR_THREADS * sizeof(uint32));
    allocPtr = 0;
    mvm::Thread::baseAddr = baseAddr;
  }

  word_t allocate() {
    stackLock.lock();
    uint32 myIndex = 0;
    do {
      if (!used[myIndex]) {
        used[myIndex] = 1;
        break;
      }
      ++myIndex;
    } while (myIndex != NR_THREADS);
  
    stackLock.unlock();
    
    if (myIndex != NR_THREADS)
      return baseAddr + myIndex * STACK_SIZE;

    return 0;
  }

};


/// Static allocate a stack manager. In the future, this should be virtual
/// machine specific.
StackThreadManager TheStackManager;

/// internalThreadStart - The initial function called by a thread. Sets some
/// thread specific data, registers the thread to the GC and calls the
/// given routine of th.
///
void Thread::internalThreadStart(mvm::Thread* th) {
  th->baseSP  = System::GetCallerAddress();

  assert(th->MyVM && "VM not set in a thread");
  th->MyVM->rendezvous.addThread(th);
  th->routine(th);
  th->MyVM->removeThread(th);
}



/// start - Called by the creator of the thread to run the new thread.
int Thread::start(void (*fct)(mvm::Thread*)) {
  pthread_attr_t attributs;
  pthread_attr_init(&attributs);
  pthread_attr_setstack(&attributs, this, STACK_SIZE);
  routine = fct;
  // Make sure to add it in the list of threads before leaving this function:
  // the garbage collector wants to trace this thread.
  MyVM->addThread(this);
  int res = pthread_create((pthread_t*)(void*)(&internalThreadID), &attributs,
                           (void* (*)(void *))internalThreadStart, this);
  pthread_attr_destroy(&attributs);
  return res;
}


/// operator new - Get a stack from the stack manager. The Thread object
/// will be placed in the first page at the bottom of the stack. Hence
/// Thread objects can not exceed a page.
void* Thread::operator new(size_t sz) {
  assert(sz < (size_t)getpagesize() && "Thread local data too big");
  void* res = (void*)TheStackManager.allocate();
  // Make sure the thread information is cleared.
  if (res != NULL) memset(res, 0, sz);
  return res;
}

/// releaseThread - Remove the stack of the thread from the list of stacks
/// in use.
void Thread::releaseThread(mvm::Thread* th) {
  // It seems like the pthread implementation in Linux is clearing with NULL
  // the stack of the thread. So we have to get the thread id before
  // calling pthread_join.
  void* thread_id = th->internalThreadID;
  if (thread_id != NULL) {
    // Wait for the thread to die.
    pthread_join((pthread_t)thread_id, NULL);
  }
  word_t index = ((word_t)th & System::GetThreadIDMask());
  index = (index & ~TheStackManager.baseAddr) >> 20;
  TheStackManager.used[index] = 0;
}
