//===--- ctlock.cc - Common threads implementation of locks ---------------===//
//
//                     The Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <cassert>

#include "mvm/Threads/Cond.h"
#include "mvm/Threads/Locks.h"
#include "mvm/Threads/Thread.h"
#include "mvm/VirtualMachine.h"
#include "MvmGC.h"
#include <cerrno>
#include <sys/time.h>
#include <pthread.h>


namespace mvm {

Lock::Lock() {
  pthread_mutexattr_t attr;

  // Initialize the mutex attributes
  int errorcode = pthread_mutexattr_init(&attr);
  assert(errorcode == 0); 

  // Initialize the mutex as a recursive mutex, if requested, or normal
  // otherwise.
  int kind = PTHREAD_MUTEX_NORMAL;
  errorcode = pthread_mutexattr_settype(&attr, kind);
  assert(errorcode == 0); 

#if !defined(__FreeBSD__) && !defined(__OpenBSD__) && !defined(__NetBSD__) && \
    !defined(__DragonFly__)
  // Make it a process local mutex
  errorcode = pthread_mutexattr_setpshared(&attr, PTHREAD_PROCESS_PRIVATE);
#endif

  // Initialize the mutex
  errorcode = pthread_mutex_init(&internalLock, &attr);
  assert(errorcode == 0); 

  // Destroy the attributes
  errorcode = pthread_mutexattr_destroy(&attr);
  assert(errorcode == 0);

  owner = 0;
}

Lock::~Lock() {
  pthread_mutex_destroy((pthread_mutex_t*)&internalLock);
}

bool Lock::selfOwner() {
  return owner == mvm::Thread::get();
}

mvm::Thread* Lock::getOwner() {
  return owner;
}

void LockNormal::lock() {
  Thread* th = Thread::get();
  th->enterUncooperativeCode();
  pthread_mutex_lock((pthread_mutex_t*)&internalLock);
  th->leaveUncooperativeCode();
  owner = th;
}

void LockNormal::unlock() {
  assert(selfOwner() && "Not owner when unlocking");
  owner = 0;
  pthread_mutex_unlock((pthread_mutex_t*)&internalLock);
}

void LockRecursive::lock() {
  if (!selfOwner()) {
    Thread* th = Thread::get();
    th->enterUncooperativeCode();
    pthread_mutex_lock((pthread_mutex_t*)&internalLock);
    th->leaveUncooperativeCode();
    owner = th;
  }
  ++n;
}

int LockRecursive::tryLock() {
  int res = 0;
  if (!selfOwner()) {
    res = pthread_mutex_trylock((pthread_mutex_t*)&internalLock);
    owner = mvm::Thread::get();
  }
  ++n;
  return res;
}

void LockRecursive::unlock() {
  assert(selfOwner() && "Not owner when unlocking");
  --n;
  if (n == 0) {
    owner = 0;
    pthread_mutex_unlock((pthread_mutex_t*)&internalLock);
  }
}

int LockRecursive::unlockAll() {
  assert(selfOwner() && "Not owner when unlocking all");
  int res = n;
  n = 0;
  owner = 0;
  pthread_mutex_unlock((pthread_mutex_t*)&internalLock);
  return res;
}

void LockRecursive::lockAll(int count) {
  if (selfOwner()) {
    n += count;
  } else {
    Thread* th = Thread::get();
    th->enterUncooperativeCode();
    pthread_mutex_lock((pthread_mutex_t*)&internalLock);
    th->leaveUncooperativeCode();
    owner = th;
    n = count;
  }
}

Cond::Cond() {
  int errorcode;
  errorcode = pthread_cond_init((pthread_cond_t*)&internalCond, NULL);
  assert(errorcode == 0); 
}

Cond::~Cond() {
  pthread_cond_destroy((pthread_cond_t*)&internalCond);
}

void Cond::broadcast() {
  pthread_cond_broadcast((pthread_cond_t*)&internalCond);
}

void Cond::wait(Lock* l) {
  assert(l->selfOwner());
  int n = l->unsafeUnlock();
  int res;
  Thread* th = Thread::get();
  th->enterUncooperativeCode();
  res = pthread_cond_wait((pthread_cond_t*)&internalCond,
													(pthread_mutex_t*)&(l->internalLock));
  th->leaveUncooperativeCode();

  assert(!res && "Error on wait");
  l->unsafeLock(n);
}

void Cond::signal() {
  pthread_cond_signal((pthread_cond_t*)&internalCond);
}

#define BILLION 1000000000
int Cond::timedWait(Lock* l, struct timeval *ref) { 
  struct timespec timeout; 
  struct timeval now;
  gettimeofday(&now, NULL); 
  timeout.tv_sec = now.tv_sec + ref->tv_sec; 
  timeout.tv_nsec = (now.tv_usec + ref->tv_usec) * 1000;
  if (timeout.tv_nsec > BILLION) {
    timeout.tv_sec++;
    timeout.tv_nsec -= BILLION;
  }
  
  assert(l->selfOwner());
  int n = l->unsafeUnlock();
  
  Thread* th = Thread::get();
  th->enterUncooperativeCode();
  int res = pthread_cond_timedwait((pthread_cond_t*)&internalCond, 
                                   (pthread_mutex_t*)&(l->internalLock),
                                   &timeout);
  th->leaveUncooperativeCode();
  
  assert((!res || res == ETIMEDOUT) && "Error on timed wait");
  l->unsafeLock(n);

  return res;
}

}
