//===------------- JavaTypes.cpp - Java primitives ------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <vector>

#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaCompiler.h"
#include "JavaTypes.h"

using namespace j3;

Signdef::Signdef(const UTF8* name, std::vector<Typedef*>& args, Typedef* ret) {
  
  arguments[0] = ret;
  Typedef** myArgs = &(arguments[1]);
  nbArguments = args.size();
  uint32 index = 0;
  for (std::vector<Typedef*>::iterator i = args.begin(), e = args.end();
       i != e; ++i) {
    myArgs[index++] = *i;
  }
  keyName = name;
  _virtualCallBuf = 0;
  _staticCallBuf = 0;
  _virtualCallAP = 0;
  _staticCallAP = 0;
  
}

ObjectTypedef::ObjectTypedef(const UTF8* name, UTF8Map* map) {
  keyName = name;
  pseudoAssocClassName = name->extract(map, 1, name->size - 1);
}

void Signdef::nativeName(char* ptr, const char* ext) const {
  ptr[0] = '_';
  ptr[1] = '_';
  ptr += 2;
  for (uint32_t i = 0; i < nbArguments; i++) {
    ptr[0] = arguments[i + 1]->getId();
    ++ptr;
  }
  ptr[0] = '_';
  ptr[1] = '_';
  ptr += 2;
  ptr[0] = arguments[0]->getId();
  ++ptr;

  assert(ext && "I need an extension");
  memcpy(ptr, ext, strlen(ext) + 1);
}

CommonClass* ArrayTypedef::assocClass() const {
	return NULL;
}

CommonClass* ArrayTypedef::findAssocClass() const {
	return NULL;
}

CommonClass* ObjectTypedef::assocClass() const {
	return NULL;
}

CommonClass* ObjectTypedef::findAssocClass() const {
	return NULL;
}

