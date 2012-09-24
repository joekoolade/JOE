//===--------- MutatorThread.h - Thread for GC ----------------------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#ifndef MVM_MUTATOR_THREAD_H
#define MVM_MUTATOR_THREAD_H

#include "mvm/Allocator.h"
#include "mvm/Threads/Thread.h"

namespace mvm {

class MutatorThread : public mvm::Thread {
public:
  MutatorThread() : mvm::Thread() {
    MutatorContext = 0;
    CollectionAttempts = 0;
  }
  mvm::ThreadAllocator Allocator;
  word_t MutatorContext;
  
  /// realRoutine - The function to invoke when the thread starts.
  ///
  void (*realRoutine)(mvm::Thread*);

  uint32_t CollectionAttempts;

  static void init(Thread* _th);

  static MutatorThread* get() {
    return (MutatorThread*)mvm::Thread::get();
  }

  virtual int start(void (*fct)(mvm::Thread*)) {
    realRoutine = fct;
    routine = init;
    return Thread::start(init);
  }
};

}

#endif
