//===-------- VMStaticInstance.h - Java wrapper for a static instance------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_VMSTATICINSTANCE_H
#define JNJVM_VMSTATICINSTANCE_H

#include "ClasspathReflect.h"
#include "JavaObject.h"
#include "MvmGC.h"

namespace j3 {

/// VMStaticInstance - Used as a placeholder for a staticInstance, tracing to
/// the corresponding Class to ensure it doesn't get improperly GC'd.
/// This placeholder is used solely in getStaticFieldBase and the various
/// put/get methods in sun.misc.Unsafe, any other use is invalid.
/// Largely inspired by VMClassLoader.
///
class VMStaticInstance : public JavaObject {
private:

  /// OwningClass - The Class this is represents a static instance of.
  Class * OwningClass;

public:

  static VMStaticInstance* allocate(Class * Class) {
    VMStaticInstance* res = 0;
    llvm_gcroot(res, 0);
    llvm_gcroot(Class, 0);
    res = (VMStaticInstance*)gc::operator new(sizeof(VMStaticInstance), &VT);
    res->OwningClass = Class;

    return res;
  }

  /// VT - The VirtualTable for this GC-class.
  ///
  static VirtualTable VT;

  /// Is the object a VMStaticInstance object?
  ///
  static bool isVMStaticInstance(JavaObject* obj) {
    llvm_gcroot(obj, 0);
    return obj->getVirtualTable() == &VT;
  }

  /// ~VMStaticInstance - Nothing. Placeholder method
  /// to give the VirtualTable.
  ///
  static void staticDestructor(VMStaticInstance* obj) {
    llvm_gcroot(obj, 0);
    // Nothing to do here
  }

  /// staticTracer - Trace through to our Class
  ///
  static void staticTracer(VMStaticInstance* obj, word_t closure) {
    llvm_gcroot(obj, 0);
    assert(obj->OwningClass);
    obj->OwningClass->classLoader->tracer(closure);
  }

  /// getStaticInstance - Get the static instance contained in this object
  ///
  void * getStaticInstance() {
    assert(OwningClass);
    return OwningClass->getStaticInstance();
  }

};

}

#endif // JNJVM_VMSTATICINSTANCE_H
