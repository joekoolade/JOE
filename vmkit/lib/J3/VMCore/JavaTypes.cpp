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

UserCommonClass* ArrayTypedef::assocClass(JnjvmClassLoader* loader) const {
  return loader->constructArray(keyName);
}

UserCommonClass* ObjectTypedef::assocClass(JnjvmClassLoader* loader) const {
  return loader->loadName(pseudoAssocClassName, false, true, NULL);
}

UserCommonClass* ObjectTypedef::findAssocClass(JnjvmClassLoader* loader) const {
  return loader->lookupClassOrArray(pseudoAssocClassName);
}

UserCommonClass* ArrayTypedef::findAssocClass(JnjvmClassLoader* loader) const {
  return loader->lookupClassOrArray(keyName);
}

Signdef::Signdef(const UTF8* name, JnjvmClassLoader* loader,
                 std::vector<Typedef*>& args, Typedef* ret) {
  
  arguments[0] = ret;
  Typedef** myArgs = &(arguments[1]);
  nbArguments = args.size();
  uint32 index = 0;
  for (std::vector<Typedef*>::iterator i = args.begin(), e = args.end();
       i != e; ++i) {
    myArgs[index++] = *i;
  }
  initialLoader = loader;
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

word_t Signdef::staticCallBuf() {
  if (!_staticCallBuf) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "static_buf");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) {
      _staticCallBuf = res;
    } else {
      initialLoader->getCompiler()->staticCallBuf(this);
    }
  }
  return _staticCallBuf;
}

word_t Signdef::virtualCallBuf() {
  if (!_virtualCallBuf) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "virtual_buf");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) { 
      _virtualCallBuf = res;
    } else {
      initialLoader->getCompiler()->virtualCallBuf(this);
    }
  }
  return _virtualCallBuf;
}

word_t Signdef::staticCallAP() {
  if (!_staticCallAP) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "static_ap");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) {
      _staticCallAP = res;
    } else {
      initialLoader->getCompiler()->staticCallAP(this);
    }
  }
  return _staticCallAP;
}

word_t Signdef::virtualCallAP() {
  if (!_virtualCallAP) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "virtual_ap");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) {
      _virtualCallAP = res;
    } else {
      initialLoader->getCompiler()->virtualCallAP(this);
    }
  }
  return _virtualCallAP;
}

word_t Signdef::virtualCallStub() {
  if (!_virtualCallAP) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "virtual_stub");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) {
      _virtualCallStub = res;
    } else {
      initialLoader->getCompiler()->virtualCallStub(this);
    }
  }
  return _virtualCallStub;
}

word_t Signdef::specialCallStub() {
  if (!_specialCallStub) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "special_stub");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) {
      _specialCallStub = res;
    } else {
      initialLoader->getCompiler()->specialCallStub(this);
    }
  }
  return _specialCallStub;
}

word_t Signdef::staticCallStub() {
  if (!_staticCallStub) {
    mvm::ThreadAllocator allocator;
    char* buf = (char*)allocator.Allocate((keyName->size << 1) + 1 + 11);
    nativeName(buf, "static_stub");
    bool unused = false;
    word_t res = initialLoader->loadInLib(buf, unused);
    if (res) {
      _staticCallStub = res;
    } else {
      initialLoader->getCompiler()->staticCallStub(this);
    }
  }
  return _staticCallStub;
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
