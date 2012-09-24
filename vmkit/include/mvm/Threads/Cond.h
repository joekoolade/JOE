//===---------------- Cond.h - Threads conditions -------------------------===//
//
//                     The Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_COND_H
#define MVM_COND_H

#include <cstdlib>
#include <pthread.h>

namespace mvm {

class Lock;

class Cond {
  pthread_cond_t internalCond;
public:
  
  Cond();
  ~Cond();
  void broadcast(void) __attribute__ ((noinline));
  void wait(Lock *l) __attribute__ ((noinline));
  int timedWait(Lock *l, timeval *tv) __attribute__ ((noinline));
  void signal(void) __attribute__ ((noinline));
};

} // end namespace mvm

#endif // MVM_COND_H
