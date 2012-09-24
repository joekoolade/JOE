//===----------- Sigsegv.cc - Sigsegv default handling --------------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#include "mvm/VirtualMachine.h"
#include "mvm/Threads/Thread.h"

#include <csignal>
#include <cstdio>

using namespace mvm;

#if defined(__MACH__) && defined(__i386__)
#include "ucontext.h"
#endif

void sigsegvHandler(int n, siginfo_t *_info, void *context) {
  word_t addr = (word_t)_info->si_addr;
#if defined(__i386__)
  struct frame {
    struct frame *caller;
    void         *ip;
  };
  
  /* my frame */
  struct frame *fp;
  /* get it */
  asm ("mov %%ebp, %0" : "=&r"(fp));
  /* my caller */
  struct frame *caller = fp->caller; 
  /* preserve my caller if I return from the handler */
  void *caller_ip = caller->ip; 

#if defined(__MACH__)
  //.gregs[REG_EIP]; /* just like it's on the stack.. */
  caller->ip = (void *)((ucontext_t*)context)->uc_mcontext->__ss.__eip;
#else
  /* just like it's on the stack... */
  caller->ip = (void *)((ucontext_t*)context)->uc_mcontext.gregs[REG_EIP]; 
#endif
#endif

  mvm::Thread* th = mvm::Thread::get();
  if (addr > (word_t)th->getThreadID() && addr < (word_t)th->baseSP) {
    fprintf(stderr, "Stack overflow in VM code or in JNI code. If it is from\n"
                    "the VM, it is either from the JIT, the GC or the runtime."
                    "\nThis has to be fixed in the VM: VMKit makes sure that\n"
                    "the bottom of the stack is always available when entering"
                    "\nthe VM.\n");
  } else {
    fprintf(stderr, "Thread %p received a SIGSEGV: either the VM code or an external\n"
                    "native method is bogus. Aborting...\n", (void*)th);
  }
  th->printBacktrace();
  abort();
  
#if defined(__i386__)
  caller->ip = caller_ip; /* restore the caller ip */
#endif
}
