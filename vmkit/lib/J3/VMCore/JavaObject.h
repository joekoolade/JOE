//===----------- JavaObject.h - Java object definition -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_OBJECT_H
#define JNJVM_JAVA_OBJECT_H

#include "UTF8.h"

#include "types.h"
#include <sys/time.h>

union jvalue;
extern "C" void EmptyDestructor();

typedef uint32_t word_t;

namespace j3 {

class JavaObject;
class JavaThread;
class Typedef;
class CommonClass;
class Class;
class ClassArray;
class ClassPrimitive;

class InterfaceMethodTable {
public:
	static const uint32_t NumIndexes = 29;
	word_t contents[NumIndexes];

  static uint32_t getIndex(const UTF8* name, const UTF8* type) {
    return (name->hash() + type->hash()) % NumIndexes;
  }
};

/// JavaVirtualTable - This class is the virtual table of instances of
/// Java classes. Besides holding function pointers for virtual calls,
/// it contains a bunch of information useful for fast dynamic type checking.
/// These are placed here for fast access of information from a Java object
/// (that only points to the VT, not the class).
///
class JavaVirtualTable {
public:

  /// cl - The class which defined this virtual table.
  ///
  CommonClass* cl;

  /// depth - The super hierarchy depth of the class.
  ///
  size_t depth;

  /// offset - Offset in the virtual table where this virtual
  /// table may be pointed. The offset is the cache if the class
  /// is an interface or depth is too big, or an offset in the display.
  ///
  size_t offset;

  /// cache - The cached result for better type checks on secondary types.
  ///
  JavaVirtualTable* cache;

  /// display - Array of super classes.
  ///
  JavaVirtualTable* display[8];

  /// nbSecondaryTypes - The length of the secondary type list.
  ///
  size_t nbSecondaryTypes;

  /// secondaryTypes - The list of secondary types of this type. These
  /// are the interface and all the supers whose depth is too big.
  ///
  JavaVirtualTable** secondaryTypes;

  /// baseClassVT - Holds the base class VT of an array. Used for AASTORE
  /// checks.
  ///
  JavaVirtualTable* baseClassVT;

  /// IMT - The interface method table.
  ///
  InterfaceMethodTable* IMT;

  /// Java methods for the virtual table functions.
  word_t init;
  word_t equals;
  word_t hashCode;
  word_t toString;
  word_t clone;
  word_t getClass;
  word_t notify;
  word_t notifyAll;
  word_t waitIndefinitely;
  word_t waitMs;
  word_t waitMsNs;
  word_t virtualMethods[1];

  /// JavaVirtualTable - Create JavaVirtualTable objects for classes, array
  /// classes and primitive classes.
  ///
  JavaVirtualTable(Class* C);
  JavaVirtualTable(ClassArray* C);
  JavaVirtualTable(ClassPrimitive* C);


  static uint32_t numberOfBaseFunctions() {
    return 4;
  }

  static uint32_t numberOfSpecializedTracers() {
    return 1;
  }

  /// getFirstJavaMethod - Get the byte offset of the first Java method
  /// (<init>).
  ///
  word_t* getFirstJavaMethod() {
    return &init;
  }
  
  /// getFirstJavaMethodIndex - Get the word offset of the first Java method.
  ///
  static uint32_t getFirstJavaMethodIndex() {
    return numberOfBaseFunctions() + 16;
  }
   
  /// getBaseSize - Get the size of the java.lang.Object virtual table.
  ///
  static uint32_t getBaseSize() {
    return numberOfBaseFunctions() + 27;
  }
  
  /// getNumJavaMethods - Get the number of methods of the java.lang.Object
  /// class.
  ///
  static uint32_t getNumJavaMethods() {
    return 11;
  }

  /// getDisplayLength - Get the length of the display (primary type) array.
  ///
  static uint32_t getDisplayLength() {
    return 8;
  }
  
  /// getClassIndex - Get the word offset of the class.
  ///
  static uint32_t getClassIndex() {
    return numberOfBaseFunctions();
  }
  
  /// getDepthIndex - Get the word offset of the depth.
  ///
  static uint32_t getDepthIndex() {
    return numberOfBaseFunctions() + 1;
  }
  
  /// getOffsetIndex - Get the word offset of the type cache.
  ///
  static uint32_t getOffsetIndex() {
    return numberOfBaseFunctions() + 2;
  }
  
  /// getCacheIndex - Get the word offset of the type cache.
  ///
  static uint32_t getCacheIndex() {
    return numberOfBaseFunctions() + 3;
  }
  
  /// getDisplayIndex - Get the word offset of the display.
  ///
  static uint32_t getDisplayIndex() {
    return numberOfBaseFunctions() + 4;
  }
  
  /// getNumSecondaryTypesIndex - Get the word offset of the number of
  /// secondary types.
  ///
  static uint32_t getNumSecondaryTypesIndex() {
    return numberOfBaseFunctions() + 12;
  }
  
  /// getSecondaryTypesIndex - Get the word offset of the secondary types
  /// list.
  ///
  static uint32_t getSecondaryTypesIndex() {
    return numberOfBaseFunctions() + 13;
  }
  
  /// getBaseClassIndex - Get the word offset of the base class.
  ///
  static uint32_t getBaseClassIndex() {
    return numberOfBaseFunctions() + 14;
  }
   
  /// getIMTIndex - Get the word offset of the IMT.
  ///
  static uint32_t getIMTIndex() {
    return numberOfBaseFunctions() + 15;
  }
   
  /// isSubtypeOf - Returns true if the given VT is a subtype of the this
  /// VT.
  ///
  bool isSubtypeOf(JavaVirtualTable* VT);

  /// setNativeTracer - Set the tracer of this virtual table as a method
  /// defined by JnJVM.
  ///
  void setNativeTracer(word_t tracer, const char* name);
  
  /// setNativeDestructor - Set the destructor of this virtual table as a method
  /// defined by JnJVM.
  ///
  void setNativeDestructor(word_t tracer, const char* name);
public:
 word_t destructor;
 word_t operatorDelete;
 word_t tracer;
 word_t specializedTracers[1];

 word_t* getFunctions() {
   return &destructor;
 }

// VirtualTable(word_t d, word_t o, word_t t) {
//   destructor = d;
//   operatorDelete = o;
//   tracer = t;
// }
//
// VirtualTable() {
//   destructor = reinterpret_cast<word_t>(EmptyDestructor);
// }

 bool hasDestructor() {
   return destructor != reinterpret_cast<word_t>(EmptyDestructor);
 }

 static void emptyTracer(void*) {}
};


} // end namespace j3

#endif
