//===--------------------- LinkJavaRuntime.h ------------------------------===//
//=== ------------- Reference all runtime functions -----------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_LINK_JAVA_RUNTIME_H
#define JNJVM_LINK_JAVA_RUNTIME_H


namespace j3 {
  class JavaObject;
  class UserClass;
  class UserClassArray;
  class UserCommonClass;
  class UserConstantPool;
  class JavaVirtualTable;
  class JavaMethod;
  class Jnjvm;
}

namespace mvm {
  class KnownFrame;
}

using namespace j3;

extern "C" void* j3InterfaceLookup(UserClass* caller, uint32 index);
extern "C" void* j3VirtualFieldLookup(UserClass* caller, uint32 index);
extern "C" void* j3StaticFieldLookup(UserClass* caller, uint32 index);
extern "C" void* j3VirtualTableLookup(UserClass* caller, uint32 index, ...);
extern "C" void* j3StringLookup(UserClass* cl, uint32 index);
extern "C" void* j3ClassLookup(UserClass* caller, uint32 index);
extern "C" UserCommonClass* j3RuntimeInitialiseClass(UserClass* cl);
extern "C" JavaObject* j3RuntimeDelegatee(UserCommonClass* cl);
extern "C" JavaArray* j3MultiCallNew(UserClassArray* cl, uint32 len, ...);
extern "C" UserClassArray* j3GetArrayClass(UserCommonClass*,
                                              UserClassArray**);
extern "C" void j3EndJNI(uint32**);
extern "C" void* j3StartJNI(uint32*, uint32**, mvm::KnownFrame*);
extern "C" void j3JavaObjectAquire(JavaObject* obj);
extern "C" void j3JavaObjectRelease(JavaObject* obj);
extern "C" void j3ThrowException(JavaObject* obj);
extern "C" JavaObject* j3NullPointerException();
extern "C" JavaObject* j3NegativeArraySizeException(sint32 val);
extern "C" JavaObject* j3OutOfMemoryError(sint32 val);
extern "C" JavaObject* j3StackOverflowError();
extern "C" JavaObject* j3ArithmeticException();
extern "C" JavaObject* j3ClassCastException(JavaObject* obj,
                                               UserCommonClass* cl);
extern "C" JavaObject* j3IndexOutOfBoundsException(JavaObject* obj,
                                                      sint32 index);
extern "C" JavaObject* j3ArrayStoreException(JavaVirtualTable* VT);
extern "C" void j3ThrowExceptionFromJIT();
extern "C" void j3PrintMethodStart(JavaMethod* meth);
extern "C" void j3PrintMethodEnd(JavaMethod* meth);
extern "C" void j3PrintExecution(uint32 opcode, uint32 index,
                                    JavaMethod* meth);

namespace force_linker {
  struct ForceRuntimeLinking {
    ForceRuntimeLinking() {
      // We must reference the methods in such a way that compilers will not
      // delete it all as dead code, even with whole program optimization,
      // yet is effectively a NO-OP. As the compiler isn't smart enough
      // to know that getenv() never returns -1, this will do the job.
      if (std::getenv("bar") != (char*) -1) 
        return;
      
      (void) j3InterfaceLookup(0, 0);
      (void) j3VirtualFieldLookup(0, 0);
      (void) j3StaticFieldLookup(0, 0);
      (void) j3VirtualTableLookup(0, 0);
      (void) j3ClassLookup(0, 0);
      (void) j3RuntimeInitialiseClass(0);
      (void) j3RuntimeDelegatee(0);
      (void) j3MultiCallNew(0, 0);
      (void) j3GetArrayClass(0, 0);
      (void) j3EndJNI(0);
      (void) j3StartJNI(0, 0, 0);
      (void) j3JavaObjectAquire(0);
      (void) j3JavaObjectRelease(0);
      (void) j3ThrowException(0);
      (void) j3NullPointerException();
      (void) j3NegativeArraySizeException(0);
      (void) j3OutOfMemoryError(0);
      (void) j3StackOverflowError();
      (void) j3ArithmeticException();
      (void) j3ClassCastException(0, 0);
      (void) j3IndexOutOfBoundsException(0, 0);
      (void) j3ArrayStoreException(0);
      (void) j3ThrowExceptionFromJIT();
      (void) j3PrintMethodStart(0);
      (void) j3PrintMethodEnd(0);
      (void) j3PrintExecution(0, 0, 0);
      (void) j3StringLookup(0, 0);
    }
  } ForcePassLinking; // Force link by creating a global definition.
}
  


#endif //JNJVM_LINK_JAVA_RUNTIME_H
