//===------------ ClassContext.inc -- Implement GetClassContext -----------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

ArrayObject* GetClassContext() {
  ArrayObject* result = NULL;
  JavaObject* delegatee = NULL;
  llvm_gcroot(result, 0);
  llvm_gcroot(delegatee, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  uint32 length = th->getFrameContextLength();

  mvm::ThreadAllocator allocator;
  uintptr_t* buffer = (uintptr_t*)allocator.Allocate(length * sizeof(uintptr_t));

  uint32 finalSize = th->getJavaFrameContext((void**)buffer);

  result = (ArrayObject*)
    vm->upcalls->classArrayClass->doNew(finalSize, vm);

  for (uint32 i = 0; i != finalSize; ++i) {
    JavaMethod* meth = ((JavaMethod**)buffer)[i];
    assert(meth && "Wrong stack trace");
    delegatee = meth->classDef->getClassDelegatee(vm);;
    ArrayObject::setElement(result, delegatee, i);
  }

  return result;
}
