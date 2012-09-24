//===---------- JavaCompiler.h - J3 interface for the compiler ------------===//
//
//                           The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JAVA_COMPILER_H
#define JAVA_COMPILER_H

#include <cstdio>
#include <cstdlib>
#include <string>
#include <dlfcn.h>

#include "mvm/GC/GC.h"
#include "mvm/Allocator.h"

namespace mvm {
  class UTF8;
}

namespace j3 {

class Class;
class CommonClass;
class JavaMethod;
class JavaVirtualTable;
class JnjvmClassLoader;
class Signdef;

class JavaCompiler {
public:
  
  mvm::BumpPtrAllocator allocator;

  virtual JavaCompiler* Create(const std::string&) {
    return this;
  }
  
  virtual void* materializeFunction(JavaMethod* meth, Class* customizeFor) {
    fprintf(stderr, "Materializing a function in an empty compiler");
    abort();
    return 0;
  }

  virtual bool isStaticCompiling() {
    return false;
  }

  virtual bool emitFunctionName() {
    return false;
  }

  virtual void resolveVirtualClass(Class* cl) {
    fprintf(stderr, "Resolving a class in an empty compiler");
    abort();
  }

  virtual void resolveStaticClass(Class* cl) {
    fprintf(stderr, "Resolving a class in an empty compiler");
    abort();
  }


  virtual void staticCallBuf(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }

  virtual void virtualCallBuf(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }

  virtual void staticCallAP(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }

  virtual void virtualCallAP(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }
  
  virtual void virtualCallStub(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }
  
  virtual void specialCallStub(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }
  
  virtual void staticCallStub(Signdef* sign) {
    fprintf(stderr, "Asking for a callback in an empty compiler");
    abort();
  }

  virtual ~JavaCompiler() {}

  virtual void* loadMethod(void* handle, const char* symbol) {
    return dlsym(handle, symbol);
  }

  static const mvm::UTF8* InlinePragma;
  static const mvm::UTF8* NoInlinePragma;

  virtual CommonClass* getUniqueBaseClass(CommonClass* cl) {
    return 0;
  }
};

}

#endif
