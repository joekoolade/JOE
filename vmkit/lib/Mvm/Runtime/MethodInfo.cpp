//===------- MethodInfo.cpp - Runtime information for methods -------------===//
//
//                        The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/ADT/DenseMap.h"
#include "llvm/ADT/DenseSet.h"

#include "mvm/Allocator.h"
#include "mvm/MethodInfo.h"
#include "mvm/VirtualMachine.h"
#include "MvmGC.h"

#include <dlfcn.h>

namespace mvm {

void MethodInfoHelper::scan(word_t closure, FrameInfo* FI, word_t ip, word_t addr) {
  //word_t spaddr = (word_t)addr + FI->FrameSize + sizeof(void*);
  word_t spaddr = System::GetCallerOfAddress(addr);
  for (uint16 i = 0; i < FI->NumLiveOffsets; ++i) {
    word_t obj = *(word_t*)(spaddr + FI->LiveOffsets[i]);    
    // Verify that obj does not come from a JSR bytecode.
    if (!(obj & 1)) {
      Collector::scanObject((void**)(spaddr + FI->LiveOffsets[i]), closure);
    }
  }
}

void MethodInfoHelper::print(word_t ip, word_t addr) {
  Dl_info info;
  int res = dladdr((void*)ip, &info);
  if (res != 0 && info.dli_sname != NULL) {
    fprintf(stderr, "; %p (%p) in %s\n",  (void*)ip, (void*)addr, info.dli_sname);
  } else {
    fprintf(stderr, "; %p in Unknown method\n", (void*)ip);
  }
}


FunctionMap::FunctionMap(BumpPtrAllocator& allocator, CompiledFrames** allFrames) {
  if (allFrames == NULL) return;
  Functions.resize(32000); // Make sure the cache is big enough.
  int i = 0;
  CompiledFrames* compiledFrames = NULL;
  while ((compiledFrames = allFrames[i++]) != NULL) {
    Frames* currentFrames = compiledFrames->frames();
    for (uint32_t j = 0; j < compiledFrames->NumCompiledFrames; j++) {
      FrameIterator iterator(*currentFrames);
      FrameInfo* frame = NULL;
      while (iterator.hasNext()) {
        frame = iterator.next();
        assert(frame->ReturnAddress);
        addFrameInfoNoLock(frame->ReturnAddress, frame);
      }
      if (frame != NULL) {
        currentFrames = reinterpret_cast<Frames*>(
            reinterpret_cast<word_t>(frame) + MethodInfoHelper::FrameInfoSize(frame->NumLiveOffsets));
      } else {
        currentFrames = reinterpret_cast<Frames*>(System::WordAlignUp(
            reinterpret_cast<word_t>(currentFrames) + sizeof(Frames)));
      }
    }
  }
}

// Create a dummy FrameInfo, so that methods don't have to null check.
static FrameInfo emptyInfo;

FrameInfo* FunctionMap::IPToFrameInfo(word_t ip) {
  FunctionMapLock.acquire();
  llvm::DenseMap<word_t, FrameInfo*>::iterator I = Functions.find(ip);
  FrameInfo* res = NULL;
  if (I != Functions.end()) {
    res = I->second;
  } else {
    assert(emptyInfo.Metadata == NULL);
    assert(emptyInfo.NumLiveOffsets == 0);
    res = &emptyInfo;
  }
  FunctionMapLock.release();
  return res;
}


void FunctionMap::addFrameInfo(word_t ip, FrameInfo* meth) {
  FunctionMapLock.acquire();
  addFrameInfoNoLock(ip, meth);
  FunctionMapLock.release();
}

}
