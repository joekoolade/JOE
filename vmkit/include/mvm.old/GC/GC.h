//===----------- GC.h - Garbage Collection Interface -----------------------===//
//
//                     The Micro Virtual Machine
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//


#ifndef MVM_GC_H
#define MVM_GC_H

#include <stdint.h>
#include "mvm/System.h"

class VirtualTable;

class gcRoot {
public:
  virtual           ~gcRoot() {}
  virtual void      tracer(word_t closure) {}
  word_t header;
  
  /// getVirtualTable - Returns the virtual table of this object.
  ///
  VirtualTable* getVirtualTable() const {
    return ((VirtualTable**)(this))[0];
  }
  
  /// setVirtualTable - Sets the virtual table of this object.
  ///
  void setVirtualTable(VirtualTable* VT) {
    ((VirtualTable**)(this))[0] = VT;
  }
};

namespace mvm {
  // TODO(ngeoffray): Make these two constants easily configurable. For now they
  // work for all our supported GCs.
  static const uint32_t GCBits = 8;
  static const bool MovesObject = true;

  static const uint32_t HashBits = 8;
  static const uint64_t GCBitMask = ((1 << GCBits) - 1);
}

#endif
