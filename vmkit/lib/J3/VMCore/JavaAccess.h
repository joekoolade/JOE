//===----------- JavaAccess.h - Java access description -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//
//
// This file defines macros and functions for knowing and checking the access
// type of Java class, fields or methods.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_ACCESS_H
#define JNJVM_JAVA_ACCESS_H

namespace j3 {

#define ACC_PUBLIC       0x0001
#define ACC_PRIVATE      0x0002
#define ACC_PROTECTED    0x0004
#define ACC_VIRTUAL      0x0000
#define ACC_STATIC       0x0008
#define ACC_FINAL        0x0010
#define ACC_SYNCHRONIZED 0x0020
#define ACC_SUPER        0x0020
#define ACC_VOLATILE     0x0040
#define ACC_TRANSIENT    0x0080
#define ACC_NATIVE       0x0100
#define ACC_INTERFACE    0x0200
#define ACC_ABSTRACT     0x0400
#define ACC_STRICT       0x0800
#define ACC_SYNTHETIC    0x1000
#define ACC_ENUM         0x4000

#define JNJVM_CLASS      0x10000
#define JNJVM_ARRAY      0x20000
#define JNJVM_PRIMITIVE  0x40000

#define MK_VERIFIER(name, flag)                   \
  inline bool name(unsigned int param) {          \
    return (flag & param) != 0;                   \
  }                                               \

MK_VERIFIER(isStatic,     ACC_STATIC)
MK_VERIFIER(isNative,     ACC_NATIVE)
MK_VERIFIER(isInterface,  ACC_INTERFACE)
MK_VERIFIER(isSynchro,    ACC_SYNCHRONIZED)
MK_VERIFIER(isPublic,     ACC_PUBLIC)
MK_VERIFIER(isPrivate,    ACC_PRIVATE)
MK_VERIFIER(isAbstract,   ACC_ABSTRACT)
MK_VERIFIER(isProtected,  ACC_PROTECTED)
MK_VERIFIER(isFinal,      ACC_FINAL)
MK_VERIFIER(isSuper,      ACC_SUPER)
MK_VERIFIER(isSynthetic,  ACC_SYNTHETIC)
MK_VERIFIER(isEnum,       ACC_ENUM)


inline bool isVirtual(unsigned int param) {
    return !(ACC_STATIC & param);
}

MK_VERIFIER(isClass,      JNJVM_CLASS)
MK_VERIFIER(isPrimitive,  JNJVM_PRIMITIVE)
MK_VERIFIER(isArray,      JNJVM_ARRAY)


#undef MK_VERIFIER

} // end namespace j3

#endif
