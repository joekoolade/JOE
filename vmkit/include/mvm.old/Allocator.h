//===----------- Allocator.h - A memory allocator  ------------------------===//
//
//                        The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_ALLOCATOR_H
#define MVM_ALLOCATOR_H

#include "llvm/Support/Allocator.h"
#include "mvm/Threads/Locks.h"

#include <cstring>

class VirtualTable;

namespace mvm {

class BumpPtrAllocator {
private:
  SpinLock TheLock;
  llvm::BumpPtrAllocator Allocator;
public:
  void* Allocate(size_t sz, const char* name) {
    TheLock.acquire();
    void* res = Allocator.Allocate(sz, sizeof(void*));
    TheLock.release();
    memset(res, 0, sz);
    return res;
  }

  void Deallocate(void* obj) {}

};

class ThreadAllocator {
private:
  llvm::BumpPtrAllocator Allocator;
public:
  void* Allocate(size_t sz) {
    void* res = Allocator.Allocate(sz, sizeof(void*));
    memset(res, 0, sz);
    return res;
  }

  void Deallocate(void* obj) {}

};

class PermanentObject {
public:
  void* operator new(size_t sz, BumpPtrAllocator& allocator,
                     const char* name) {
    return allocator.Allocate(sz, name);
  }
  
  void operator delete(void* ptr) {
    free(ptr);
  }

  void* operator new [](size_t sz, BumpPtrAllocator& allocator,
                        const char* name) {
    return allocator.Allocate(sz, name);
  }
  
  void* operator new[](size_t sz) {
    return malloc(sz);
  }
  
  void operator delete[](void* ptr) {
    return free(ptr);
  }
};

} // end namespace mvm

#endif // MVM_ALLOCATOR_H
