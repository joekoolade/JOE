//===----- JavaMetaJIT.cpp - Caling Java methods from native code ---------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <cstdarg>
#include <cstring>
#include <jni.h>

#include "debug.h"

#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

using namespace j3;


#define readArgs(buf, signature, ap, jni) { \
  jvalue* buffer = (jvalue*)buf; \
  Typedef* const* arguments = signature->getArgumentsType(); \
  for (uint32 i = 0; i < signature->nbArguments; ++i) { \
    const Typedef* type = arguments[i];\
    if (type->isPrimitive()) {\
      const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;\
      if (prim->isLong()) {\
        buffer[i].j = va_arg(ap, sint64);\
      } else if (prim->isInt()){ \
        buffer[i].i = va_arg(ap, sint32);\
      } else if (prim->isChar()) { \
        buffer[i].c = va_arg(ap, uint32);\
      } else if (prim->isShort()) { \
        buffer[i].s = va_arg(ap, sint32);\
      } else if (prim->isByte()) { \
        buffer[i].b = va_arg(ap, sint32);\
      } else if (prim->isBool()) { \
        buffer[i].z = va_arg(ap, uint32);\
      } else if (prim->isFloat()) {\
        buffer[i].f = (float)va_arg(ap, double);\
      } else if (prim->isDouble()) {\
        buffer[i].d = va_arg(ap, double);\
      } else {\
        fprintf(stderr, "Can't happen");\
        abort();\
      }\
    } else{\
      buffer[i].l = reinterpret_cast<jobject>(va_arg(ap, JavaObject**));\
    }\
  }\
}
  
#define DO_TRY
#define DO_CATCH if (th->pendingException) { th->throwFromJava(); }

//===----------------------------------------------------------------------===//
// We do not need to have special care on the GC-pointers in the buffer
// manipulated in these functions because the objects in the buffer are
// addressed and never stored directly.
//===----------------------------------------------------------------------===//

#if 1 // VA_ARGS do not work on all platforms for LLVM.
#define INVOKE_AP(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
\
TYPE JavaMethod::invoke##TYPE_NAME##VirtualAP(Jnjvm* vm, UserClass* cl, JavaObject* obj, va_list ap) { \
  llvm_gcroot(obj, 0); \
  verifyNull(obj); \
  Signdef* sign = getSignature(); \
  mvm::ThreadAllocator allocator; \
  jvalue* buf = (jvalue*)allocator.Allocate(sign->nbArguments * sizeof(jvalue)); \
  readArgs(buf, sign, ap, jni); \
  return invoke##TYPE_NAME##VirtualBuf(vm, cl, obj, buf); \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##SpecialAP(Jnjvm* vm, UserClass* cl, JavaObject* obj, va_list ap) {\
  llvm_gcroot(obj, 0); \
  verifyNull(obj); \
  Signdef* sign = getSignature(); \
  mvm::ThreadAllocator allocator; \
  jvalue* buf = (jvalue*)allocator.Allocate(sign->nbArguments * sizeof(jvalue)); \
  readArgs(buf, sign, ap, jni); \
  return invoke##TYPE_NAME##SpecialBuf(vm, cl, obj, buf); \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##StaticAP(Jnjvm* vm, UserClass* cl, va_list ap) {\
  Signdef* sign = getSignature(); \
  mvm::ThreadAllocator allocator; \
  jvalue* buf = (jvalue*)allocator.Allocate(sign->nbArguments * sizeof(jvalue)); \
  readArgs(buf, sign, ap, jni); \
  return invoke##TYPE_NAME##StaticBuf(vm, cl, buf); \
}

#else

#define INVOKE_AP(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
TYPE JavaMethod::invoke##TYPE_NAME##VirtualAP(Jnjvm* vm, UserClass* cl, JavaObject* obj, va_list ap) { \
  llvm_gcroot(obj, 0); \
  verifyNull(obj); \
  UserClass* objCl = JavaObject::getClass(obj)->isArray() ? JavaObject::getClass(obj)->super : JavaObject::getClass(obj)->asClass(); \
  if (objCl == classDef || isFinal(access)) { \
    meth = this; \
  } else { \
    meth = objCl->lookupMethodDontThrow(name, type, false, true, &cl); \
  } \
  assert(meth && "No method found"); \
  void* func = meth->compiledPtr(); \
  Signdef* sign = getSignature(); \
  FUNC_TYPE_VIRTUAL_AP call = (FUNC_TYPE_VIRTUAL_AP)sign->getVirtualCallAP(); \
  JavaThread* th = JavaThread::get(); \
  th->startJava(); \
  TYPE res = 0; \
  DO_TRY \
  res = call(cl->getConstantPool(), func, obj, ap);\
  DO_CATCH \
  th->endJava(); \
  return res; \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##SpecialAP(Jnjvm* vm, UserClass* cl, JavaObject* obj, va_list ap) {\
  llvm_gcroot(obj, 0); \
  verifyNull(obj);\
  void* func = this->compiledPtr();\
  Signdef* sign = getSignature(); \
  FUNC_TYPE_VIRTUAL_AP call = (FUNC_TYPE_VIRTUAL_AP)sign->getVirtualCallAP(); \
  JavaThread* th = JavaThread::get(); \
  th->startJava(); \
  TYPE res = 0; \
  DO_TRY \
  res = call(cl->getConstantPool(), func, obj, ap);\
  DO_CATCH \
  th->endJava(); \
  return res; \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##StaticAP(Jnjvm* vm, UserClass* cl, va_list ap) {\
  if (!cl->isReady()) { \
    cl->resolveClass(); \
    cl->initialiseClass(vm); \
  } \
  \
  void* func = this->compiledPtr();\
  Signdef* sign = getSignature(); \
  FUNC_TYPE_STATIC_AP call = (FUNC_TYPE_STATIC_AP)sign->getStaticCallAP(); \
  JavaThread* th = JavaThread::get(); \
  th->startJava(); \
  TYPE res = 0; \
  DO_TRY \
  res = call(cl->getConstantPool(), func, ap);\
  DO_CATCH \
  th->endJava(); \
  return res; \
}

#endif

#define INVOKE_BUF(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
TYPE JavaMethod::invoke##TYPE_NAME##VirtualBuf(Jnjvm* vm, UserClass* cl, JavaObject* obj, void* buf) {\
  llvm_gcroot(obj, 0); \
  verifyNull(obj);\
  Signdef* sign = getSignature(); \
  UserClass* objCl = JavaObject::getClass(obj)->isArray() ? JavaObject::getClass(obj)->super : JavaObject::getClass(obj)->asClass(); \
  JavaMethod* meth = NULL; \
  if (objCl == classDef || isFinal(access)) { \
    meth = this; \
  } else { \
    meth = objCl->lookupMethodDontThrow(name, type, false, true, &cl); \
  } \
  assert(meth && "No method found"); \
  assert(objCl->isAssignableFrom(meth->classDef) && "Wrong type"); \
  void* func = meth->compiledPtr(); \
  FUNC_TYPE_VIRTUAL_BUF call = (FUNC_TYPE_VIRTUAL_BUF)sign->getVirtualCallBuf(); \
  JavaThread* th = JavaThread::get(); \
  th->startJava(); \
  TYPE res = 0; \
  DO_TRY \
  res = call(cl->getConstantPool(), func, obj, buf);\
  DO_CATCH \
  th->endJava(); \
  return res; \
}\
TYPE JavaMethod::invoke##TYPE_NAME##SpecialBuf(Jnjvm* vm, UserClass* cl, JavaObject* obj, void* buf) {\
  llvm_gcroot(obj, 0); \
  verifyNull(obj);\
  void* func = this->compiledPtr();\
  Signdef* sign = getSignature(); \
  FUNC_TYPE_VIRTUAL_BUF call = (FUNC_TYPE_VIRTUAL_BUF)sign->getVirtualCallBuf(); \
  JavaThread* th = JavaThread::get(); \
  th->startJava(); \
  TYPE res = 0; \
  DO_TRY \
  res = call(cl->getConstantPool(), func, obj, buf);\
  DO_CATCH \
  th->endJava(); \
  return res; \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##StaticBuf(Jnjvm* vm, UserClass* cl, void* buf) {\
  if (!cl->isReady()) { \
    cl->resolveClass(); \
    cl->initialiseClass(vm); \
  } \
  \
  void* func = this->compiledPtr();\
  Signdef* sign = getSignature(); \
  FUNC_TYPE_STATIC_BUF call = (FUNC_TYPE_STATIC_BUF)sign->getStaticCallBuf(); \
  JavaThread* th = JavaThread::get(); \
  th->startJava(); \
  TYPE res = 0; \
  DO_TRY \
  res = call(cl->getConstantPool(), func, buf);\
  DO_CATCH \
  th->endJava(); \
  return res; \
}\

#define INVOKE_VA(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
TYPE JavaMethod::invoke##TYPE_NAME##Virtual(Jnjvm* vm, UserClass* cl, JavaObject* obj, ...) { \
  llvm_gcroot(obj, 0); \
  va_list ap;\
  va_start(ap, obj);\
  TYPE res = invoke##TYPE_NAME##VirtualAP(vm, cl, obj, ap);\
  va_end(ap); \
  return res; \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##Special(Jnjvm* vm, UserClass* cl, JavaObject* obj, ...) {\
  llvm_gcroot(obj, 0); \
  va_list ap;\
  va_start(ap, obj);\
  TYPE res = invoke##TYPE_NAME##SpecialAP(vm, cl, obj, ap);\
  va_end(ap); \
  return res; \
}\
\
TYPE JavaMethod::invoke##TYPE_NAME##Static(Jnjvm* vm, UserClass* cl, ...) {\
  va_list ap;\
  va_start(ap, cl);\
  TYPE res = invoke##TYPE_NAME##StaticAP(vm, cl, ap);\
  va_end(ap); \
  return res; \
}\

#define INVOKE(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
  INVOKE_AP(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
  INVOKE_BUF(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF) \
  INVOKE_VA(TYPE, TYPE_NAME, FUNC_TYPE_VIRTUAL_AP, FUNC_TYPE_STATIC_AP, FUNC_TYPE_VIRTUAL_BUF, FUNC_TYPE_STATIC_BUF)

typedef uint32 (*uint32_virtual_ap)(UserConstantPool*, void*, JavaObject*, va_list);
typedef sint64 (*sint64_virtual_ap)(UserConstantPool*, void*, JavaObject*, va_list);
typedef float  (*float_virtual_ap)(UserConstantPool*, void*, JavaObject*, va_list);
typedef double (*double_virtual_ap)(UserConstantPool*, void*, JavaObject*, va_list);
typedef JavaObject* (*object_virtual_ap)(UserConstantPool*, void*, JavaObject*, va_list);

typedef uint32 (*uint32_static_ap)(UserConstantPool*, void*, va_list);
typedef sint64 (*sint64_static_ap)(UserConstantPool*, void*, va_list);
typedef float  (*float_static_ap)(UserConstantPool*, void*, va_list);
typedef double (*double_static_ap)(UserConstantPool*, void*, va_list);
typedef JavaObject* (*object_static_ap)(UserConstantPool*, void*, va_list);

typedef uint32 (*uint32_virtual_buf)(UserConstantPool*, void*, JavaObject*, void*);
typedef sint64 (*sint64_virtual_buf)(UserConstantPool*, void*, JavaObject*, void*);
typedef float  (*float_virtual_buf)(UserConstantPool*, void*, JavaObject*, void*);
typedef double (*double_virtual_buf)(UserConstantPool*, void*, JavaObject*, void*);
typedef JavaObject* (*object_virtual_buf)(UserConstantPool*, void*, JavaObject*, void*);

typedef uint32 (*uint32_static_buf)(UserConstantPool*, void*, void*);
typedef sint64 (*sint64_static_buf)(UserConstantPool*, void*, void*);
typedef float  (*float_static_buf)(UserConstantPool*, void*, void*);
typedef double (*double_static_buf)(UserConstantPool*, void*, void*);
typedef JavaObject* (*object_static_buf)(UserConstantPool*, void*, void*);

INVOKE(uint32, Int, uint32_virtual_ap, uint32_static_ap, uint32_virtual_buf, uint32_static_buf)
INVOKE(sint64, Long, sint64_virtual_ap, sint64_static_ap, sint64_virtual_buf, sint64_static_buf)
INVOKE(float,  Float, float_virtual_ap,  float_static_ap,  float_virtual_buf,  float_static_buf)
INVOKE(double, Double, double_virtual_ap, double_static_ap, double_virtual_buf, double_static_buf)
INVOKE(JavaObject*, JavaObject, object_virtual_ap, object_static_ap, object_virtual_buf, object_static_buf)
