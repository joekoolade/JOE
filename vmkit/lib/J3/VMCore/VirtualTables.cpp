//===------ VirtualTables.cpp - Virtual methods for J3 objects ------------===//
//
//                          The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//
// 
// This file contains GC specific tracing functions.
//
// The file is divided into four parts:
// (1) Declaration of internal GC classes.
// (2) Tracing Java objects: regular object, native array, object array.
// (3) Tracing a class loader, which involves tracing the Java objects
//     referenced by classes.
// (4) Tracing the roots of a program: the JVM and the threads.
//
// Additionnaly, all write of GC objets in J3 data structures must go through
// a write barrier.
//
//===----------------------------------------------------------------------===//

#include "ClasspathReflect.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "JnjvmClassLoader.h"
#include "LockedMap.h"
#include "ReferenceQueue.h"
#include "VMStaticInstance.h"
#include "Zip.h"

using namespace j3;

//===----------------------------------------------------------------------===//
// List of classes that will be GC-allocated. One should try to keep this
// list as minimal as possible, and a GC class must be defined only if
// absolutely necessary. If there is an easy way to avoid it, do it! Only
// Java classes should be GC classes.
// Having many GC classes gives more work to the GC for the scanning phase
// and for the relocation phase (for copying collectors).
//
// In J3, there is only one primary internal gc object, the class loader.
// We decided that this was the best solution because
// otherwise it would involve hacks on the java.lang.Classloader class.
// Therefore, we create a new GC class with a finalize method that will
// delete the internal class loader when the Java object class loader is
// not reachable anymore. This also relies on the java.lang.Classloader class
// referencing an object of type VMClassLoader (this is the case in GNU
// Classpath with the vmdata field).
// In addition, to handle support for sun.misc.Unsafe, we have a similar
// second clsas VMStaticInstance that wraps static instances for use
// in staticFieldBase and traces the owning ClassLoader to make sure
// the underlying instance and class don't get GC'd improperly.
//===----------------------------------------------------------------------===//

VirtualTable VMClassLoader::VT((word_t)VMClassLoader::staticDestructor,
                               (word_t)VMClassLoader::staticDestructor,
                               (word_t)VMClassLoader::staticTracer);

VirtualTable VMStaticInstance::VT((word_t)VMStaticInstance::staticDestructor,
                                  (word_t)VMStaticInstance::staticDestructor,
                                  (word_t)VMStaticInstance::staticTracer);

//===----------------------------------------------------------------------===//
// Trace methods for Java objects. There are four types of objects:
// (1) java.lang.Object and primitive arrays: no need to trace anything.
// (2) Object whose class is not an array: needs to trace the classloader, and
//     all the virtual fields.
// (3) Object whose class is an array of objects: needs to trace the class
//     loader and all elements in the array.
// (4) Objects that extend java.lang.ref.Reference: must trace the class loader
//     and all the fields except the referent.
//===----------------------------------------------------------------------===//

/// Scanning java.lang.Object and primitive arrays.
extern "C" void JavaObjectTracer(JavaObject* obj, word_t closure) {
}

/// Method for scanning regular objects.
extern "C" void RegularObjectTracer(JavaObject* obj, word_t closure) {
  Class* cl = JavaObject::getClass(obj)->asClass();
  assert(cl && "Not a class in regular tracer");
  mvm::Collector::markAndTraceRoot(
      cl->classLoader->getJavaClassLoaderPtr(), closure);

  while (cl->super != 0) {
    for (uint32 i = 0; i < cl->nbVirtualFields; ++i) {
      JavaField& field = cl->virtualFields[i];
      if (field.isReference()) {
        JavaObject** ptr = field.getInstanceObjectFieldPtr(obj);
        mvm::Collector::markAndTrace(obj, ptr, closure);
      }
    }
    cl = cl->super;
  }
}

/// Method for scanning an array whose elements are JavaObjects. This method is
/// called for all non-native Java arrays.
extern "C" void ArrayObjectTracer(ArrayObject* obj, word_t closure) {
  CommonClass* cl = JavaObject::getClass(obj);
  assert(cl && "No class");
  mvm::Collector::markAndTraceRoot(
      cl->classLoader->getJavaClassLoaderPtr(), closure);
  

  for (sint32 i = 0; i < ArrayObject::getSize(obj); i++) {
    if (ArrayObject::getElement(obj, i) != NULL) {
      mvm::Collector::markAndTrace(
          obj, ArrayObject::getElements(obj) + i, closure);
    }
  } 
}

/// Method for scanning Java java.lang.ref.Reference objects.
extern "C" void ReferenceObjectTracer(
    JavaObjectReference* obj, word_t closure) {
  Class* cl = JavaObject::getClass(obj)->asClass();
  assert(cl && "Not a class in reference tracer");
  mvm::Collector::markAndTraceRoot(
      cl->classLoader->getJavaClassLoaderPtr(), closure);

  bool found = false;
  while (cl->super != 0) {
    for (uint32 i = 0; i < cl->nbVirtualFields; ++i) {
      JavaField& field = cl->virtualFields[i];
      if (field.isReference()) {
        JavaObject** ptr = field.getInstanceObjectFieldPtr(obj);
        if (ptr != JavaObjectReference::getReferentPtr(obj)) {
          mvm::Collector::markAndTrace(obj, ptr, closure);
        } else {
          found = true;
        }
      }
    }
    cl = cl->super;
  }
  assert(found && "No referent in a reference");
}

//===----------------------------------------------------------------------===//
// Support for scanning Java objects referenced by classes. All classes must
// trace:
// (1) The classloader of the parents (super and interfaces) as well as its
//     own class loader.
// (2) The delegatee object (java.lang.Class) if it exists.
//
// Additionaly, non-primitive and non-array classes must trace:
// (3) The static instance.
//===----------------------------------------------------------------------===//

void CommonClass::tracer(word_t closure) {
  
  if (super != NULL && super->classLoader != NULL) {
    JavaObject** Obj = super->classLoader->getJavaClassLoaderPtr();
    if (*Obj != NULL) mvm::Collector::markAndTraceRoot(Obj, closure);
  
    for (uint32 i = 0; i < nbInterfaces; ++i) {
      if (interfaces[i]->classLoader) {
        JavaObject** Obj = interfaces[i]->classLoader->getJavaClassLoaderPtr();
        if (*Obj != NULL) mvm::Collector::markAndTraceRoot(Obj, closure);
      }
    }
  }

  if (classLoader != NULL) {
    mvm::Collector::markAndTraceRoot(
        classLoader->getJavaClassLoaderPtr(), closure);
  }

  for (uint32 i = 0; i < NR_ISOLATES; ++i) {
    if (delegatee[i] != NULL) {
      mvm::Collector::markAndTraceRoot(delegatee + i, closure);
    }
  }
}

void Class::tracer(word_t closure) {
  CommonClass::tracer(closure);
  
  for (uint32 i = 0; i < NR_ISOLATES; ++i) {
    TaskClassMirror &M = IsolateInfo[i];
    if (M.staticInstance != NULL) {
      for (uint32 i = 0; i < nbStaticFields; ++i) {
        JavaField& field = staticFields[i];
        if (field.isReference()) {
          JavaObject** ptr = field.getStaticObjectFieldPtr();
          mvm::Collector::markAndTraceRoot(ptr, closure);
        }
      }
    }
  }
}

//===----------------------------------------------------------------------===//
// Support for scanning a classloader. A classloader must trace:
// (1) All the classes it has loaded (located in the classmap).
// (2) All the class it has initiated loading and therefore references (located
//     in the classmap).
// (3) All the strings referenced in class files.
//
// The class loader does not need to trace its java.lang.Classloader Java object
// because if we end up here, this means that the Java object is already being
// scanned. Only the Java object traces the class loader.
//
// Additionaly, the bootstrap loader must trace:
// (4) The delegatees of native array classes. Since these classes are not in
//     the class map and they are not GC-allocated, we must trace the objects
//     referenced by the delegatees.
//===----------------------------------------------------------------------===//

void JnjvmClassLoader::tracer(word_t closure) {
  
  for (ClassMap::iterator i = classes->map.begin(), e = classes->map.end();
       i!= e; ++i) {
    CommonClass* cl = i->second;
    if (cl->isClass()) cl->asClass()->tracer(closure);
    else cl->tracer(closure);
  }
  
  StringList* end = strings;
  while (end != NULL) {
    for (uint32 i = 0; i < end->length; ++i) {
      JavaString** obj = end->strings + i;
      mvm::Collector::markAndTraceRoot(obj, closure);
    }
    end = end->prev;
  }
  
  mvm::Collector::markAndTraceRoot(&javaLoader, closure);
}

void JnjvmBootstrapLoader::tracer(word_t closure) {
 
  JnjvmClassLoader::tracer(closure);
  upcalls->OfVoid->tracer(closure);
  upcalls->OfBool->tracer(closure);
  upcalls->OfByte->tracer(closure);
  upcalls->OfChar->tracer(closure);
  upcalls->OfShort->tracer(closure);
  upcalls->OfInt->tracer(closure);
  upcalls->OfFloat->tracer(closure);
  upcalls->OfLong->tracer(closure);
  upcalls->OfDouble->tracer(closure);
}

//===----------------------------------------------------------------------===//
// Support for scanning the roots of a program: JVM and threads. The JVM
// must trace:
// (1) The bootstrap class loader: where core classes live.
// (2) The applicative class loader: the JVM may be the ony one referencing it.
// (3) Global references from JNI.
//
// The threads must trace:
// (1) Their stack (already done by the GC in the case of GCMmap2 or Boehm)
// (2) Their pending exception if there is one.
// (3) The java.lang.Thread delegate.
//===----------------------------------------------------------------------===//


void Jnjvm::tracer(word_t closure) {
  // (1) Trace the bootrap loader.
  bootstrapLoader->tracer(closure);
  
  // (2) Trace the application class loader.
  if (appClassLoader != NULL) {
    mvm::Collector::markAndTraceRoot(
        appClassLoader->getJavaClassLoaderPtr(), closure);
  }
  
  // (3) Trace JNI global references.
  JNIGlobalReferences* start = &globalRefs;
  while (start != NULL) {
    for (uint32 i = 0; i < start->length; ++i) {
      JavaObject** obj = start->globalReferences + i;
      mvm::Collector::markAndTraceRoot(obj, closure);
    }
    start = start->next;
  }
  
  // (4) Trace the interned strings.
  for (StringMap::iterator i = hashStr.map.begin(), e = hashStr.map.end();
       i!= e; ++i) {
    JavaString** str = &(i->second);
    mvm::Collector::markAndTraceRoot(str, closure);
    ArrayUInt16** key = const_cast<ArrayUInt16**>(&(i->first));
    mvm::Collector::markAndTraceRoot(key, closure);
  }

  // (5) Trace the finalization queue.
  for (uint32 i = 0; i < finalizerThread->CurrentFinalizedIndex; ++i) {
    mvm::Collector::markAndTraceRoot(finalizerThread->ToBeFinalized + i, closure);
  }
  
  // (6) Trace the reference queue
  for (uint32 i = 0; i < referenceThread->ToEnqueueIndex; ++i) {
    mvm::Collector::markAndTraceRoot(referenceThread->ToEnqueue + i, closure);
  }
 
  // (7) Trace the locks and their associated object.
  uint32 i = 0;
  for (; i < mvm::LockSystem::GlobalSize; i++) {
    mvm::FatLock** array = lockSystem.LockTable[i];
    if (array == NULL) break;
    uint32 j = 0;
    for (; j < mvm::LockSystem::IndexSize; j++) {
      if (array[j] == NULL) break;
      mvm::FatLock* lock = array[j];
      mvm::Collector::markAndTraceRoot(lock->getAssociatedObjectPtr(), closure);
    }
    for (j = j + 1; j < mvm::LockSystem::IndexSize; j++) {
      assert(array[j] == NULL);
    }
  }
  for (i = i + 1; i < mvm::LockSystem::GlobalSize; i++) {
    assert(lockSystem.LockTable[i] == NULL);
  }
}

void JavaThread::tracer(word_t closure) {
  mvm::Collector::markAndTraceRoot(&pendingException, closure);
  mvm::Collector::markAndTraceRoot(&javaThread, closure);
  mvm::Collector::markAndTraceRoot(&vmThread, closure);
  
  JNILocalReferences* end = localJNIRefs;
  while (end != NULL) {
    for (uint32 i = 0; i < end->length; ++i) {
      JavaObject** obj = end->localReferences + i;
      mvm::Collector::markAndTraceRoot(obj, closure);
    }
    end = end->prev;
  }
}
