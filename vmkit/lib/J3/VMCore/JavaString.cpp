//===-- JavaString.cpp - Internal correspondance with Java Strings --------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <cassert>

#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaString.h"

using namespace j3;

JavaVirtualTable* JavaString::internStringVT = 0;

JavaString* JavaString::stringDup(const ArrayUInt16 *const& _array) {
  // fixme
  JavaString* res = 0;
  return res;
}

char* JavaString::strToAsciiz(JavaString* self) {
  const ArrayUInt16* value = NULL;
  value = JavaString::getValue(self);
  char* buf = new char[self->count + 1]; 
  for (sint32 i = 0; i < self->count; ++i) {
    buf[i] = ArrayUInt16::getElement(value, i + self->offset);
  }
  buf[self->count] =  0; 
  return buf;
}

const ArrayUInt16* JavaString::strToArray(JavaString* self) {
  ArrayUInt16* array = NULL;
  const ArrayUInt16* value = NULL;
  value = JavaString::getValue(self);

  assert(getValue(self) && "String without an array?");
  if (self->offset || (self->count != ArrayUInt16::getSize(getValue(self)))) {
	  // fixme
//    array = (ArrayUInt16*)vm->upcalls->ArrayOfChar->doNew(self->count, vm);
    for (sint32 i = 0; i < self->count; i++) {
      ArrayUInt16::setElement(
          array, ArrayUInt16::getElement(value, i + self->offset), i);
    }
    return array;
  } else {
    return value;
  }
}

void JavaString::stringDestructor(JavaString* str) {
}

JavaString* JavaString::internalToJava(const UTF8* name) {
  
  ArrayUInt16* array = 0;

  // fixme
  // array = (ArrayUInt16*)vm->upcalls->ArrayOfChar->doNew(name->size, vm);
  
  for (sint32 i = 0; i < name->size; i++) {
    uint16 cur = name->elements[i];
    if (cur == '/') {
      ArrayUInt16::setElement(array, '.', i);
    } else {
      ArrayUInt16::setElement(array, cur, i);
    }
  }

  // fixme
  return (JavaString *)0;
}

const UTF8* JavaString::javaToInternal(const JavaString* self, UTF8Map* map) {
  const ArrayUInt16* value = NULL;
  value = JavaString::getValue(self);
 
  uint16* java = new uint16[self->count];

  for (sint32 i = 0; i < self->count; ++i) {
    uint16 cur = ArrayUInt16::getElement(value, self->offset + i);
    if (cur == '.') java[i] = '/';
    else java[i] = cur;
  }
  
  const UTF8* res = map->lookupOrCreateReader(java, self->count);
  return res;
}
