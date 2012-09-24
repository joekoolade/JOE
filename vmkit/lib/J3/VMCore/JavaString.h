//===--- JavaString.h - Internal correspondance with Java Strings ---------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_STRING_H
#define JNJVM_JAVA_STRING_H

#include "types.h"

#include "JavaObject.h"
#include "UTF8.h"

namespace j3 {

class ArrayUInt16;
class Jnjvm;

class JavaString : public JavaObject {
 private:
  const ArrayUInt16* value;
 public:
#ifndef USE_OPENJDK
  // Classpath fields
  sint32 count;
  sint32 cachedHashCode;
  sint32 offset;
#else
  // OpenJDK fields
  sint32 offset;
  sint32 count;
  sint32 cachedHashCode;
#endif

  static void setValue(JavaString* self, const ArrayUInt16* array) {
    llvm_gcroot(self, 0);
    llvm_gcroot(array, 0);
    mvm::Collector::objectReferenceWriteBarrier(
        (gc*)self, (gc**)&(self->value), (gc*)array);
  }
  static const ArrayUInt16* getValue(const JavaString* self) {
    llvm_gcroot(self, 0);
    return self->value;
  }
  
  static JavaString* stringDup(const ArrayUInt16 *const & array, Jnjvm* vm);

  /// internalToJava - Creates a copy of the UTF8 at its given offset and size
  /// with all its '.' replaced by '/'. The JVM bytecode reference classes in
  /// packages with the '.' as the separating character. The JVM language uses
  /// the '/' character. Returns a Java String.
  ///
  static JavaString* internalToJava(const UTF8* utf8, Jnjvm* vm);

  static void stringDestructor(JavaString*);
  static char* strToAsciiz(JavaString* self);
  static char* strToAsciiz(JavaString* self, mvm::ThreadAllocator* allocator);
  static const ArrayUInt16* strToArray(JavaString* self, Jnjvm* vm);

  /// javaToInternal - Replaces all '/' into '.'.
  static const UTF8* javaToInternal(const JavaString* self, UTF8Map* map);

  static JavaVirtualTable* internStringVT;
};

} // end namespace j3

#endif
