//===--------- JNIReferences.cpp - Management of JNI references -----------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNI_REFERENCES_H
#define JNI_REFERENCES_H

#include "mvm/Allocator.h"

namespace j3 {

class JavaObject;
class JavaThread;

#define MAXIMUM_REFERENCES 100

class JNILocalReferences {
  friend class JavaThread;

private:
  JNILocalReferences* prev;
  uint32_t length;
  JavaObject* localReferences[MAXIMUM_REFERENCES];

public:
  
  JNILocalReferences() {
    prev = 0;
    length = 0;
  }

  JavaObject** addJNIReference(JavaThread* th, JavaObject* obj);

  void removeJNIReferences(JavaThread* th, uint32_t num);

};

class JNIGlobalReferences {
  friend class Jnjvm;

private:
  JNIGlobalReferences* next;
  JNIGlobalReferences* prev;
  uint32_t length;
  uint32_t count;
  JavaObject* globalReferences[MAXIMUM_REFERENCES];


public:
  JNIGlobalReferences() {
    next = 0;
    prev = 0;
    length = 0;
    count = 0;
  }

  JavaObject** addJNIReference(JavaObject* obj) {
    llvm_gcroot(obj, 0);
    if (length == MAXIMUM_REFERENCES) {
      if (!next) {
        next = new JNIGlobalReferences();
        next->prev = this;
      }
      return next->addJNIReference(obj);
    } else {
      ++count;
      globalReferences[length] = obj;
      return &globalReferences[length++];
    }
  }

  void removeJNIReference(JavaObject** obj) {
    if (((word_t)obj >= (word_t)globalReferences) &&
        ((word_t)obj) < (word_t)(globalReferences + MAXIMUM_REFERENCES)) {
      *obj = NULL;
      --count;
    } else {
      assert(next && "No global reference located there");
      next->removeJNIReference(obj);
    }
  }
};

}

#endif
