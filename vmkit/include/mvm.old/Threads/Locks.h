//===------------------ Locks.h - Thread locks ----------------------------===//
//
//                     The Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_LOCKS_H
#define MVM_LOCKS_H

#include <pthread.h>
#include <cassert>
#include <cstdio>

#include "mvm/Threads/Thread.h"

extern "C" void __llvm_gcroot(void**, void*) __attribute__((nothrow));
#define llvm_gcroot(a, b) __llvm_gcroot((void**)&a, b)

class gc;

namespace mvm {

class Cond;
class FatLock;
class LockNormal;
class LockRecursive;
class Thread;

/// Lock - This class is an abstract class for declaring recursive and normal
/// locks.
///
class Lock {
  friend class Cond;
  
private:
  virtual void unsafeLock(int n) = 0;
  virtual int unsafeUnlock() = 0;

protected:
  /// owner - Which thread is currently holding the lock?
  ///
  mvm::Thread* owner;

  /// internalLock - The lock implementation of the platform.
  ///
  pthread_mutex_t internalLock;
  
public:

  /// Lock - Creates a lock, recursive if rec is true.
  ///
  Lock();
  
  /// ~Lock - Give it a home.
  ///
  virtual ~Lock();
  
  /// lock - Acquire the lock.
  ///
  virtual void lock() __attribute__ ((noinline)) = 0;

  /// unlock - Release the lock.
  ///
  virtual void unlock() = 0;

  /// selfOwner - Is the current thread holding the lock?
  ///
  bool selfOwner();

  /// getOwner - Get the thread that is holding the lock.
  ///
  mvm::Thread* getOwner();
  
};

/// LockNormal - A non-recursive lock.
class LockNormal : public Lock {
  friend class Cond;
private:
  virtual void unsafeLock(int n) {
    owner = mvm::Thread::get();
  }
  
  virtual int unsafeUnlock() {
    owner = 0;
    return 0;
  }
public:
  LockNormal() : Lock() {}

  virtual void lock() __attribute__ ((noinline));
  virtual void unlock();

};

/// LockRecursive - A recursive lock.
class LockRecursive : public Lock {
  friend class Cond;
private:
  
  /// n - Number of times the lock has been locked.
  ///
  int n;

  virtual void unsafeLock(int a) {
    n = a;
    owner = mvm::Thread::get();
  }
  
  virtual int unsafeUnlock() {
    int ret = n;
    n = 0;
    owner = 0;
    return ret;
  }

public:
  LockRecursive() : Lock() { n = 0; }
  
  virtual void lock() __attribute__ ((noinline));
  virtual void unlock();
  virtual int tryLock();

  /// recursionCount - Get the number of times the lock has been locked.
  ///
  int recursionCount() { return n; }

  /// unlockAll - Unlock the lock, releasing it the number of times it is held.
  /// Return the number of times the lock has been locked.
  ///
  int unlockAll();

  /// lockAll - Acquire the lock count times.
  ///
  void lockAll(int count) __attribute__ ((noinline));
};

/// SpinLock - This class implements a spin lock. A spin lock is OK to use
/// when it is held during short period of times. It is CPU expensive
/// otherwise.
class SpinLock {
public:

  /// locked - Is the spin lock locked?
  ///
  uint32 locked;
  
  /// SpinLock - Initialize the lock as not being held.
  ///
  SpinLock() { locked = 0; }


  /// acquire - Acquire the spin lock, doing an active loop.
  ///
  void acquire() {
    for (uint32 count = 0; count < 1000; ++count) {
      uint32 res = __sync_val_compare_and_swap(&locked, 0, 1);
      if (!res) return;
    }
    
    while (__sync_val_compare_and_swap(&locked, 0, 1))
      mvm::Thread::yield();
  }

  void lock() { acquire(); }

  /// release - Release the spin lock. This must be called by the thread
  /// holding it.
  ///
  void release() { locked = 0; }
  
  void unlock() { release(); }
};


} // end namespace mvm

#endif // MVM_LOCKS_H
