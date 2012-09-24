//===---- ObjectModel.cpp - Implementation of the ObjectModel class  ------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "mvm/System.h"
#include "mvm/VirtualMachine.h"
#include "MMTkObject.h"
#include "debug.h"

namespace mmtk {

extern "C" word_t Java_org_j3_mmtk_ObjectModel_getArrayBaseOffset__ (MMTkObject* OM) {
  return sizeof(MMTkObject) + sizeof(ssize_t);
}

extern "C" word_t Java_org_j3_mmtk_ObjectModel_GC_1HEADER_1OFFSET__ (MMTkObject* OM) {
  return sizeof(void*);
}

extern "C" word_t Java_org_j3_mmtk_ObjectModel_readAvailableBitsWord__Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* OM, gc* obj) {
  return obj->header;
}

extern "C" void Java_org_j3_mmtk_ObjectModel_writeAvailableBitsWord__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Word_2 (
    MMTkObject* OM, gc* obj, word_t val) {
  obj->header = val;
}

extern "C" gc* Java_org_j3_mmtk_ObjectModel_objectStartRef__Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* OM, gc* obj) {
  return obj;
}

extern "C" gc* Java_org_j3_mmtk_ObjectModel_refToAddress__Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* OM, gc* obj) {
  return obj;
}

extern "C" uint8_t Java_org_j3_mmtk_ObjectModel_readAvailableByte__Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* OM, gc* obj) {
  return *mvm::System::GetLastBytePtr(reinterpret_cast<word_t>(obj));
}

extern "C" void Java_org_j3_mmtk_ObjectModel_writeAvailableByte__Lorg_vmmagic_unboxed_ObjectReference_2B (MMTkObject* OM, gc* obj, uint8_t val) {
  *mvm::System::GetLastBytePtr(reinterpret_cast<word_t>(obj)) = val;
}

extern "C" gc* Java_org_j3_mmtk_ObjectModel_getObjectFromStartAddress__Lorg_vmmagic_unboxed_Address_2 (MMTkObject* OM, gc* obj) {
  return obj;
}

extern "C" word_t Java_org_j3_mmtk_ObjectModel_prepareAvailableBits__Lorg_vmmagic_unboxed_ObjectReference_2 (MMTkObject* OM, gc* obj) {
  return obj->header;
}

extern "C" uint8_t
Java_org_j3_mmtk_ObjectModel_attemptAvailableBits__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Word_2Lorg_vmmagic_unboxed_Word_2(
    MMTkObject* OM, gc* obj, word_t oldValue, word_t newValue) { 
  word_t val = __sync_val_compare_and_swap(&(obj->header), oldValue, newValue);
  return (val == oldValue);
}

extern "C" void Java_org_j3_bindings_Bindings_memcpy__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2I(
    void* res, void* src, int size) ALWAYS_INLINE;

extern "C" void Java_org_j3_bindings_Bindings_memcpy__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2I(
    void* res, void* src, int size) {
  memcpy(res, src, size);
}

extern "C" word_t JnJVM_org_j3_bindings_Bindings_copy__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2II(
    gc* obj, VirtualTable* VT, int size, int allocator);

extern "C" word_t Java_org_j3_mmtk_ObjectModel_copy__Lorg_vmmagic_unboxed_ObjectReference_2I (
    MMTkObject* OM, gc* src, int allocator) ALWAYS_INLINE;

extern "C" word_t Java_org_j3_mmtk_ObjectModel_copy__Lorg_vmmagic_unboxed_ObjectReference_2I (
    MMTkObject* OM, gc* src, int allocator) {
  size_t size = mvm::Thread::get()->MyVM->getObjectSize(src);
  size = llvm::RoundUpToAlignment(size, sizeof(void*));
  word_t res = JnJVM_org_j3_bindings_Bindings_copy__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2II(
      src, src->getVirtualTable(), size, allocator);
  assert((((word_t*)res)[1] & ~mvm::GCBitMask) == (((word_t*)src)[1] & ~mvm::GCBitMask));
  return res;
}

extern "C" void Java_org_j3_mmtk_ObjectModel_copyTo__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2 (
    MMTkObject* OM, word_t from, word_t to, word_t region) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_getReferenceWhenCopiedTo__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2 (
    MMTkObject* OM, word_t from, word_t to) { UNIMPLEMENTED(); }

extern "C" word_t Java_org_j3_mmtk_ObjectModel_getObjectEndAddress__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, gc* object) {
  size_t size = mvm::Thread::get()->MyVM->getObjectSize(object);
  size = llvm::RoundUpToAlignment(size, sizeof(void*));
  return reinterpret_cast<word_t>(object) + size;
}

extern "C" void Java_org_j3_mmtk_ObjectModel_getSizeWhenCopied__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_getAlignWhenCopied__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_getAlignOffsetWhenCopied__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_getCurrentSize__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_getNextObject__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }


class FakeByteArray : public MMTkObject {
 public:
  void* operator new(size_t size, int length) {
    return new char[size + length];
  }

  FakeByteArray(const char* name) {
    length = strlen(name);
    for (uint32 i = 0; i < length; i++) {
      elements[i] = name[i];
    }
  }
 private:
  size_t length;
  uint8_t elements[1];
};

extern "C" FakeByteArray* Java_org_j3_mmtk_ObjectModel_getTypeDescriptor__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, gc* src) {
  const char* name = mvm::Thread::get()->MyVM->getObjectTypeName(src);
  // This code is only used for debugging on a fatal error. It is fine to
  // allocate in the C++ heap.
  return new (strlen(name)) FakeByteArray(name);
}

extern "C" void Java_org_j3_mmtk_ObjectModel_getArrayLength__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_isArray__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_isPrimitiveArray__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_isAcyclic__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_ObjectModel_dumpObject__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* OM, word_t object) { UNIMPLEMENTED(); }

}
