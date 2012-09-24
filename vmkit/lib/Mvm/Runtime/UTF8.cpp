//===------------- UTF8.cpp - Common UTF8 functions -----------------------===//
//
//                     The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "mvm/Allocator.h"
#include "mvm/UTF8.h"

namespace mvm {

extern "C" const UTF8 TombstoneKey(-1);
extern "C" const UTF8 EmptyKey(-1);


const UTF8* UTF8::extract(UTF8Map* map, uint32 start, uint32 end) const {
  uint32 len = end - start;
  ThreadAllocator allocator;
  uint16* buf = (uint16*)allocator.Allocate(sizeof(uint16) * len);

  for (uint32 i = 0; i < len; i++) {
    buf[i] = elements[i + start];
  }

  return map->lookupOrCreateReader(buf, len);
}


uint32 UTF8::readerHasher(const uint16* buf, sint32 size) {
  uint32 r0 = 0, r1 = 0;
  for (sint32 i = 0; i < size; i++) {
    uint16 c = buf[i];
    r0 += c;
    r1 ^= c;
  }
  return (r1 & 255) + ((r0 & 255) << 8);
}


const UTF8* UTF8Map::lookupOrCreateAsciiz(const char* asciiz) {
  sint32 size = strlen(asciiz);
  ThreadAllocator tempAllocator;
  uint16_t* data = reinterpret_cast<uint16_t*>(
      tempAllocator.Allocate(size * sizeof(uint16_t)));
  for (int i = 0; i < size; i++) {
    data[i] = asciiz[i];
  }
  return lookupOrCreateReader(data, size);
}


const UTF8* UTF8Map::lookupOrCreateReader(const uint16* buf, uint32 len) {
  sint32 size = (sint32)len;
  UTF8MapKey key(buf, size);
  lock.lock();

  const UTF8* res = map.lookup(key);
  if (res == NULL) {
    UTF8* tmp = new(allocator, size) UTF8(size);
    memcpy(tmp->elements, buf, len * sizeof(uint16));
    res = (const UTF8*)tmp;
    key.data = res->elements;
    map[key] = res;
  }
  
  lock.unlock();
  return res;
}


const UTF8* UTF8Map::lookupAsciiz(const char* asciiz) {
  sint32 size = strlen(asciiz);
  ThreadAllocator tempAllocator;
  uint16_t* data = reinterpret_cast<uint16_t*>(
      tempAllocator.Allocate(size * sizeof(uint16_t)));
  for (int i = 0; i < size; i++) {
    data[i] = asciiz[i];
  }
  return lookupReader(data, size);
}


const UTF8* UTF8Map::lookupReader(const uint16* buf, uint32 len) {
  sint32 size = (sint32)len;
  UTF8MapKey key(buf, size);
  lock.lock();
  const UTF8* res = map.lookup(key);
  lock.unlock();
  return res;
}

} // namespace mvm
