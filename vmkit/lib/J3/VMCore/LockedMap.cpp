//===------- LockedMap.cpp - Implementation of the UTF8 map ---------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <map>

#include "JavaArray.h"
#include "JavaString.h"
#include "LockedMap.h"

namespace j3 {

bool ltarray16::operator()(const ArrayUInt16* s1, const ArrayUInt16* s2) const {
  llvm_gcroot(s1, 0);
  llvm_gcroot(s2, 0);
  if (ArrayUInt16::getSize(s1) < ArrayUInt16::getSize(s2)) return true;
  else if (ArrayUInt16::getSize(s1) > ArrayUInt16::getSize(s2)) return false;
  else return memcmp((const char*)ArrayUInt16::getElements(s1),
                     (const char*)ArrayUInt16::getElements(s2),
                     ArrayUInt16::getSize(s1) * sizeof(uint16)) < 0;
}

void StringMap::insert(JavaString* str) {
  const ArrayUInt16* array = NULL;
  llvm_gcroot(str, 0);
  llvm_gcroot(array, 0);
  array = JavaString::getValue(str);
  StringMap::iterator it = map.insert(std::make_pair(array, str)).first;
  assert(map.find(array)->second == str);
  assert(map.find(array)->first == array);
  assert(&(map.find(array)->second) == &(it->second));
  assert(&(map.find(array)->first) == &(it->first));
}

}
