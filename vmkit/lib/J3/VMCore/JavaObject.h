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

#include "mvm/Allocator.h"
#include "mvm/UTF8.h"
#include "mvm/Threads/Locks.h"
#include "mvm/Threads/Thread.h"
#include "MvmGC.h"

#include "types.h"

#include "JnjvmConfig.h"

union jvalue;

namespace j3 {

class JavaObject;
class JavaThread;
class Jnjvm;
class Typedef;
class UserCommonClass;

class InterfaceMethodTable : public mvm::PermanentObject {
public:
	static const uint32_t NumIndexes = 29;
	word_t contents[NumIndexes];

  static uint32_t getIndex(const mvm::UTF8* name, const mvm::UTF8* type) {
    return (name->hash() + type->hash()) % NumIndexes;
  }
};

/// JavaVirtualTable - This class is the virtual table of instances of
/// Java classes. Besides holding function pointers for virtual calls,
/// it contains a bunch of information useful for fast dynamic type checking.
/// These are placed here for fast access of information from a Java object
/// (that only points to the VT, not the class).
///
class JavaVirtualTable : public VirtualTable {
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

  /// operator new - Allocates a JavaVirtualTable with the given size. The
  /// size must contain the additional information for type checking, as well
  /// as the function pointers.
  ///
  void* operator new(size_t sz, mvm::BumpPtrAllocator& allocator,
                     uint32 nbMethods) {
    return allocator.Allocate(sizeof(word_t) * (nbMethods), "Virtual table");
  }

  /// JavaVirtualTable - Create JavaVirtualTable objects for classes, array
  /// classes and primitive classes.
  ///
  JavaVirtualTable(Class* C);
  JavaVirtualTable(ClassArray* C);
  JavaVirtualTable(ClassPrimitive* C);


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

};


/// JavaObject - This class represents a Java object.
///
class JavaObject : public gc {
private:
  
  /// waitIntern - internal wait on a monitor
  ///
  static void waitIntern(JavaObject* self, struct timeval *info, bool timed);
  
public:

  /// getClass - Returns the class of this object.
  ///
  static UserCommonClass* getClass(const JavaObject* self) {
    llvm_gcroot(self, 0);
    return ((JavaVirtualTable*)self->getVirtualTable())->cl;
  }
  
  /// instanceOf - Is this object's class of type the given class?
  ///
  static bool instanceOf(JavaObject* self, UserCommonClass* cl);

  /// wait - Java wait. Makes the current thread waiting on a monitor.
  ///
  static void wait(JavaObject* self);

  /// timedWait - Java timed wait. Makes the current thread waiting on a
  /// monitor for the given amount of time.
  ///
  static void timedWait(JavaObject* self, struct timeval &info);
  
  /// wait - Wait for specified ms and ns.  Wrapper for either wait() or
  /// timedWait, depending on duration specified.
  static void wait(JavaObject* self, int64_t ms, int32_t ns);

  /// notify - Java notify. Notifies a thread from the availability of the
  /// monitor.
  ///
  static void notify(JavaObject* self);
  
  /// notifyAll - Java notifyAll. Notifies all threads from the availability of
  /// the monitor.
  ///
  static void notifyAll(JavaObject* self);

  /// clone - Java clone. Creates a copy of this object.
  ///
  static JavaObject* clone(JavaObject* other);
 
  /// overflowThinLock - Notify that the thin lock has overflowed.
  ///
  static void overflowThinLock(JavaObject* self);

  /// acquire - Acquire the lock on this object.
  static void acquire(JavaObject* self);

  /// release - Release the lock on this object
  static void release(JavaObject* self);

  /// owner - Returns true if the current thread is the owner of this object's
  /// lock.
  static bool owner(JavaObject* self);

#ifdef SIGSEGV_THROW_NULL
  #define verifyNull(obj) {}
#else
  #define verifyNull(obj) \
    if (obj == NULL) JavaThread::get()->getJVM()->nullPointerException();
#endif
  
  /// decapsulePrimitive - Based on the signature argument, decapsule
  /// obj as a primitive and put it in the buffer.
  ///
  static void decapsulePrimitive(JavaObject* self, Jnjvm* vm, jvalue* buf,
                                 const Typedef* signature);

  static uint16_t hashCodeGenerator;

  /// hashCode - Return the hash code of this object.
  static uint32_t hashCode(JavaObject* self);
};


} // end namespace j3

#endif
