//===-- RuntimeHelpers.cpp - Implement rt.jar functions needed by MMTk  --===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "MMTkObject.h"

namespace mmtk {

extern "C" uint16_t MMTkCharAt(MMTkString* str, uint32_t index) {
  return str->value->elements[index + str->offset];
}

extern "C" MMTkObject* MMTkGetClass(MMTkObject* obj) {
  return obj->virtualTable->cl->delegatee;
}

extern "C" uint8_t MMTkStringEquals(MMTkString* first, MMTkString* second) {
  if (first->count != second->count) return 0;
  for (int i = 0; i < first->count; i++) {
    if (first->value->elements[i + first->offset]
        != second->value->elements[i + second->offset]) {
      return 0;
    }
  }
  return 1;
}

extern "C" int32_t MMTkStringLength(MMTkString* obj) {
  return obj->count;
}

extern "C" int32_t MMTkStringIndexOf(MMTkString* obj, uint16_t c) {
  for (int i = 0; i < obj->count; i++) {
    if (obj->value->elements[i + obj->offset] == c) return i;
  }
  return -1;
}

extern "C" MMTkString* MMTkStringSubstringII(MMTkString* obj, int32_t start, int32_t end) {
  MMTkString* str = new MMTkString();
  str->value = obj->value;
  str->count = end - start;
  str->offset = start + obj->offset;
  return str;
}

extern "C" MMTkString* MMTkStringSubstringI(MMTkString* obj, int32_t start) {
  MMTkString* str = new MMTkString();
  str->value = obj->value;
  str->count = obj->count - start;
  str->offset = start + obj->offset;
  return str;
}

}
