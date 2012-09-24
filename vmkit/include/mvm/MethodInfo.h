//===---------- MethodInfo.h - Meta information for methods ---------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MVM_METHODINFO_H
#define MVM_METHODINFO_H

#include "mvm/Allocator.h"
#include "mvm/System.h"
#include "mvm/GC/GC.h"

namespace mvm {

class FrameInfo {
public:
  void* Metadata;
  word_t ReturnAddress;
  uint16_t SourceIndex;
  uint16_t FrameSize;
  uint16_t NumLiveOffsets;
  int16_t LiveOffsets[1];
};
 
class MethodInfoHelper {
public:
  static void print(word_t ip, word_t addr);

  static void scan(word_t closure, FrameInfo* FI, word_t ip, word_t addr);
  
  static uint32_t FrameInfoSize(uint32_t NumOffsets) {
    uint32_t FrameInfoSize = sizeof(FrameInfo) + (NumOffsets - 1) * sizeof(int16_t);
    FrameInfoSize = System::WordAlignUp(FrameInfoSize);
    return FrameInfoSize;
  }
};


class Frames {
public:
  uint32_t NumDescriptors;
  FrameInfo* frames() const {
    return reinterpret_cast<FrameInfo*>(
        reinterpret_cast<word_t>(this) + kWordSize);
  }

  void* operator new(size_t sz, mvm::BumpPtrAllocator& allocator, uint32_t NumDescriptors, uint32_t NumOffsets) {
    Frames* res = reinterpret_cast<Frames*>(
        allocator.Allocate(kWordSize + NumDescriptors * MethodInfoHelper::FrameInfoSize(NumOffsets), "Frames"));
    assert(System::IsWordAligned(reinterpret_cast<word_t>(res)));
    return res;
  }
};

class CompiledFrames {
public:
  uint32_t NumCompiledFrames;
  Frames* frames() const {
    return reinterpret_cast<Frames*>(
        reinterpret_cast<word_t>(this) + kWordSize);
  }
};

class FrameIterator {
public:
  const Frames& frames;
  uint32 currentFrameNumber;
  FrameInfo* currentFrame;

  FrameIterator(const Frames& f)
      : frames(f), currentFrameNumber(0) , currentFrame(f.frames()) {
  }

  bool hasNext() {
    return currentFrameNumber < frames.NumDescriptors;
  }

  void advance(int NumLiveOffsets) {
    ++currentFrameNumber;
    if (!hasNext()) return;
    word_t ptr =
      reinterpret_cast<word_t>(currentFrame) + MethodInfoHelper::FrameInfoSize(NumLiveOffsets);
    currentFrame = reinterpret_cast<FrameInfo*>(ptr);
  }

  FrameInfo* next() {
    assert(hasNext());
    FrameInfo* result = currentFrame;
    advance(currentFrame->NumLiveOffsets);
    return result;
  }
};

} // end namespace mvm
#endif // MVM_METHODINFO_H
