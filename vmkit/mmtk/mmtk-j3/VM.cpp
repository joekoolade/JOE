//===-------------- VM.cpp - Implementation of the VM class  --------------===//
//
//                              The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "debug.h"
#include "MMTkObject.h"

namespace mmtk {

extern "C" void Java_org_j3_runtime_VM_sysWrite__Lorg_vmmagic_unboxed_Extent_2 (void* e) {
  fprintf(stderr, "%p", e);
}

extern "C" void Java_org_j3_runtime_VM_sysWrite__Lorg_vmmagic_unboxed_Address_2 (void* a) {
  fprintf(stderr, "%p", a);
}

extern "C" void Java_org_j3_runtime_VM_sysWrite__F (float f) {
  fprintf(stderr, "%f", f);
}

extern "C" void Java_org_j3_runtime_VM_sysWrite__I (int i) {
  fprintf(stderr, "%d", i);
}

extern "C" void Java_org_j3_runtime_VM_sysWrite__Ljava_lang_String_2 (MMTkString* msg) {
  for (int i = 0; i < msg->count; i++) {
    fprintf(stderr, "%c", msg->value->elements[i + msg->offset]);
  }
}

extern "C" void Java_org_j3_runtime_VM_sysWriteln__Ljava_lang_String_2 (MMTkString* msg) {
  for (int i = 0; i < msg->count; i++) {
    fprintf(stderr, "%c", msg->value->elements[i + msg->offset]);
  }
  fprintf(stderr, "\n");
}

extern "C" void Java_org_j3_runtime_VM_sysWriteln__ () {
  fprintf(stderr, "\n");
}

extern "C" void Java_org_j3_runtime_VM__1assert__ZLjava_lang_String_2 (bool cond, MMTkString* msg) {
  ABORT();
}

extern "C" void Java_org_j3_runtime_VM_sysExit__I (int i) {
  ABORT();
}

extern "C" void Java_org_j3_runtime_VM_sysFail__Ljava_lang_String_2 (MMTkString* msg) {
  // Just call abort because gcmalloc calls this function. If it were to
  // call printf, MMTkInline.inc could not be JIT-compiled.
  abort();
}

extern "C" void Java_org_j3_runtime_VM__1assert__Z (uint8_t cond) {
  ASSERT(cond);
}

extern "C" bool Java_org_j3_runtime_VM_buildFor64Addr__ () { 
  return mvm::kWordSize == 8;
}

extern "C" bool Java_org_j3_runtime_VM_buildForIA32__ () { 
#if ARCH_X86
  return true;
#else
  return false;
#endif
}

extern "C" bool Java_org_j3_runtime_VM_verifyAssertions__ () {
  // Note that DEBUG is defined in make ENABLE_OPTIMIZED=1.
  // You must provide DISABLE_ASSERTIONS=1 to not have DEBUG defined.
  // To generate MMTkInline.inc, this function returns false.
#if 0
  return true;
#else
  return false;
#endif
}

} // namespace mmtk
