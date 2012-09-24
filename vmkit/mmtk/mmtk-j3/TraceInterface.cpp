//===-- TraceInterface.cpp - Implementation of the TraceInterface class  --===//
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

extern "C" bool Java_org_j3_mmtk_TraceInterface_gcEnabled__ (MMTkObject* TI) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_adjustSlotOffset__ZLorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Address_2 (
    MMTkObject* TI, bool scalar, word_t src, word_t slot) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_skipOwnFramesAndDump__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* TI, word_t typeRef) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_updateDeathTime__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* TI, word_t obj) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_setDeathTime__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_Word_2 (
    MMTkObject* TI, word_t ref, word_t time) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_setLink__Lorg_vmmagic_unboxed_ObjectReference_2Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* TI, word_t ref, word_t link) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_updateTime__Lorg_vmmagic_unboxed_Word_2 (
    MMTkObject* TI, word_t obj) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_getOID__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* TI, word_t ref) { UNIMPLEMENTED(); }

extern "C" word_t Java_org_j3_mmtk_TraceInterface_getDeathTime__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* TI, word_t ref) { UNIMPLEMENTED(); }

extern "C" word_t Java_org_j3_mmtk_TraceInterface_getLink__Lorg_vmmagic_unboxed_ObjectReference_2 (
    MMTkObject* TI, word_t ref) { UNIMPLEMENTED(); }

extern "C" word_t Java_org_j3_mmtk_TraceInterface_getBootImageLink__ (
    MMTkObject* TI) { UNIMPLEMENTED(); }

extern "C" word_t Java_org_j3_mmtk_TraceInterface_getOID__ (
    MMTkObject* TI) { UNIMPLEMENTED(); }

extern "C" void Java_org_j3_mmtk_TraceInterface_setOID__Lorg_vmmagic_unboxed_Word_2 (
    MMTkObject* TI, word_t oid) { UNIMPLEMENTED(); }

extern "C" sint32 Java_org_j3_mmtk_TraceInterface_getHeaderSize__ (MMTkObject* TI) { UNIMPLEMENTED(); }
extern "C" sint32 Java_org_j3_mmtk_TraceInterface_getHeaderEndOffset__ (MMTkObject* TI) { UNIMPLEMENTED(); }

} // namespace mmtk
