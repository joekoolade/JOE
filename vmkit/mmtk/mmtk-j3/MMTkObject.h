//===----------- MMTkObject.h - Internal object type for MMTk  ------------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef MMTK_OBJECT_H
#define MMTK_OBJECT_H

#include <stdint.h>
#include "MutatorThread.h"
#include "MvmGC.h"

namespace mmtk {

struct MMTkObject;

struct MMTkClass {
  MMTkObject* delegatee;
};

struct MMTkVirtualTable : public VirtualTable {
  MMTkClass* cl;
};

struct MMTkObject {
  MMTkVirtualTable* virtualTable;
  word_t header;
};

struct MMTkArray : public MMTkObject {
  word_t size;
  uint16_t elements[1];
};

struct MMTkObjectArray : public MMTkObject {
  word_t size;
  MMTkObject* elements[1];
};

struct MMTkString : public MMTkObject {
  MMTkArray* value;
  int32_t count;
  int32_t cachedHashCode;
  int32_t offset;
};

struct MMTkLock : public MMTkObject {
  uint32_t state;
  MMTkString* name;
};

struct MMTkActivePlan : public MMTkObject {
  mvm::MutatorThread* current;
};

struct MMTkReferenceProcessor : public MMTkObject {
  MMTkObject* semantics;
  int32_t ordinal;
};

} // namespace mmtk

#endif // MMTK_OBJECT_H
