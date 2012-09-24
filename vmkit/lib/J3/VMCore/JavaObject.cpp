//===----------- JavaObject.cpp - Java object definition ------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "mvm/Threads/Locks.h"

#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

#include <jni.h>
#include "debug.h"

using namespace j3;

static const int hashCodeIncrement = mvm::GCBitMask + 1;
uint16_t JavaObject::hashCodeGenerator = hashCodeIncrement;
static const uint64_t HashMask = ((1 << mvm::HashBits) - 1) << mvm::GCBits;


/// hashCode - Return the hash code of this object.
uint32_t JavaObject::hashCode(JavaObject* self) {
  llvm_gcroot(self, 0);
  if (!mvm::MovesObject) return (uint32_t)(long)self;
  assert(HashMask != 0);
  assert(mvm::HashBits != 0);

  word_t header = self->header;
  word_t GCBits;
  GCBits = header & mvm::GCBitMask;
  word_t val = header & HashMask;
  if (val != 0) {
    return val ^ (word_t)getClass(self);
  }
  val = hashCodeGenerator;
  hashCodeGenerator += hashCodeIncrement;
  val = val & HashMask;
  if (val == 0) {
    // It is possible that in the same time, a thread is in this method and
    // gets the same hash code value than this thread. This is fine.
    val = hashCodeIncrement;
    hashCodeGenerator += hashCodeIncrement;
  }
  assert(val > mvm::GCBitMask);
  assert(val <= HashMask);

  do {
    header = self->header;
    if ((header & HashMask) != 0) break;
    word_t newHeader = header | val;
    assert((newHeader & ~HashMask) == header);
    __sync_val_compare_and_swap(&(self->header), header, newHeader);
  } while (true);

  assert((self->header & HashMask) != 0);
  assert(GCBits == (self->header & mvm::GCBitMask));
  return (self->header & HashMask) ^ (word_t)getClass(self);
}


void JavaObject::waitIntern(
    JavaObject* self, struct timeval* info, bool timed) {
  llvm_gcroot(self, 0);
  JavaThread* thread = JavaThread::get();
  mvm::LockSystem& table = thread->getJVM()->lockSystem;

  if (!owner(self)) {
    thread->getJVM()->illegalMonitorStateException(self);
    UNREACHABLE();
  }

  bool interrupted = thread->lockingThread.wait(self, table, info, timed);

  if (interrupted) {
    thread->getJVM()->interruptedException(self);
    UNREACHABLE();
  }
}

void JavaObject::wait(JavaObject* self) {
  llvm_gcroot(self, 0);
  waitIntern(self, NULL, false);
}

void JavaObject::timedWait(JavaObject* self, struct timeval& info) {
  llvm_gcroot(self, 0);
  waitIntern(self, &info, true);
}

void JavaObject::wait(JavaObject* self, int64_t ms, int32_t ns) {
  llvm_gcroot(self, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();

  if (ms < 0 || ns < 0) {
    vm->illegalArgumentException("Negative wait time specified");
  }

  uint32 sec = (uint32) (ms / 1000);
  uint32 usec = (ns / 1000) + 1000 * (ms % 1000);
  if (ns && !usec) usec = 1;
  if (sec || usec) {
    struct timeval t;
    t.tv_sec = sec;
    t.tv_usec = usec;
    JavaObject::timedWait(self, t);
  } else {
    JavaObject::wait(self);
  }
}

void JavaObject::notify(JavaObject* self) {
  llvm_gcroot(self, 0);
  JavaThread* thread = JavaThread::get();
  mvm::LockSystem& table = thread->getJVM()->lockSystem;

  if (!owner(self)) {
    thread->getJVM()->illegalMonitorStateException(self);
    UNREACHABLE();
  }
  thread->lockingThread.notify(self, table);
}

void JavaObject::notifyAll(JavaObject* self) {
  llvm_gcroot(self, 0);
  JavaThread* thread = JavaThread::get();
  mvm::LockSystem& table = thread->getJVM()->lockSystem;

  if (!owner(self)) {
    thread->getJVM()->illegalMonitorStateException(self);
    UNREACHABLE();
  }
  thread->lockingThread.notifyAll(self, table);
}

JavaObject* JavaObject::clone(JavaObject* src) {
  JavaObject* res = 0;
  JavaObject* tmp = 0;

  llvm_gcroot(src, 0);
  llvm_gcroot(res, 0);
  llvm_gcroot(tmp, 0);

  UserCommonClass* cl = JavaObject::getClass(src);
  Jnjvm* vm = JavaThread::get()->getJVM();

  // If this doesn't inherit the Cloneable interface, throw exception
  // TODO: Add support in both class libraries for the upcalls fields used here
  //if (!JavaObject::instanceOf(src, vm->upcalls->cloneableClass))
  //  vm->cloneNotSupportedException();

  if (cl->isArray()) {
    UserClassArray* array = cl->asArrayClass();
    int length = JavaArray::getSize(src);
    res = array->doNew(length, vm);
    UserCommonClass* base = array->baseClass();
    if (base->isPrimitive()) {
      int size = length << base->asPrimitiveClass()->logSize;
      memcpy((void*)((uintptr_t)res + sizeof(JavaObject) + sizeof(size_t)),
             (void*)((uintptr_t)src + sizeof(JavaObject) + sizeof(size_t)),
             size);
    } else {
      for (int i = 0; i < length; i++) {
        tmp = ArrayObject::getElement((ArrayObject*)src, i);
        ArrayObject::setElement((ArrayObject*)res, tmp, i);
      }
    }
  } else {
    assert(cl->isClass() && "Not a class!");
    res = cl->asClass()->doNew(vm);
    while (cl != NULL) {
      for (uint32 i = 0; i < cl->asClass()->nbVirtualFields; ++i) {
        JavaField& field = cl->asClass()->virtualFields[i];
        if (field.isReference()) {
          tmp = field.getInstanceObjectField(src);
          JavaObject** ptr = field.getInstanceObjectFieldPtr(res);
          mvm::Collector::objectReferenceWriteBarrier((gc*)res, (gc**)ptr, (gc*)tmp);
        } else if (field.isLong()) {
          field.setInstanceLongField(res, field.getInstanceLongField(src));
        } else if (field.isDouble()) {
          field.setInstanceDoubleField(res, field.getInstanceDoubleField(src));
        } else if (field.isInt()) {
          field.setInstanceInt32Field(res, field.getInstanceInt32Field(src));
        } else if (field.isFloat()) {
          field.setInstanceFloatField(res, field.getInstanceFloatField(src));
        } else if (field.isShort() || field.isChar()) {
          field.setInstanceInt16Field(res, field.getInstanceInt16Field(src));
        } else if (field.isByte() || field.isBoolean()) {
          field.setInstanceInt8Field(res, field.getInstanceInt8Field(src));
        } else {
          UNREACHABLE();
        }
      }
      cl = cl->super;
    }
  }

  return res;
}

void JavaObject::overflowThinLock(JavaObject* self) {
  llvm_gcroot(self, 0);
  mvm::ThinLock::overflowThinLock(self, JavaThread::get()->getJVM()->lockSystem);
}

void JavaObject::acquire(JavaObject* self) {
  llvm_gcroot(self, 0);
  mvm::ThinLock::acquire(self, JavaThread::get()->getJVM()->lockSystem);
}

void JavaObject::release(JavaObject* self) {
  llvm_gcroot(self, 0);
  mvm::ThinLock::release(self, JavaThread::get()->getJVM()->lockSystem);
}

bool JavaObject::owner(JavaObject* self) {
  llvm_gcroot(self, 0);
  return mvm::ThinLock::owner(self, JavaThread::get()->getJVM()->lockSystem);
}

void JavaObject::decapsulePrimitive(JavaObject* obj, Jnjvm *vm, jvalue* buf,
                                    const Typedef* signature) {

  llvm_gcroot(obj, 0);

  if (!signature->isPrimitive()) {
    if (obj && !(getClass(obj)->isOfTypeName(signature->getName()))) {
      vm->illegalArgumentException("wrong type argument");
    }
    return;
  } else if (obj == NULL) {
    vm->illegalArgumentException("");
  } else {
    UserCommonClass* cl = getClass(obj);
    UserClassPrimitive* value = cl->toPrimitive(vm);
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)signature;

    if (value == 0) {
      vm->illegalArgumentException("");
    }
    
    if (prim->isShort()) {
      if (value == vm->upcalls->OfShort) {
        (*buf).s = vm->upcalls->shortValue->getInstanceInt16Field(obj);
        return;
      } else if (value == vm->upcalls->OfByte) {
        (*buf).s = (sint16)vm->upcalls->byteValue->getInstanceInt8Field(obj);
        return;
      } else {
        vm->illegalArgumentException("");
      }
    } else if (prim->isByte()) {
      if (value == vm->upcalls->OfByte) {
        (*buf).b = vm->upcalls->byteValue->getInstanceInt8Field(obj);
        return;
      } else {
        vm->illegalArgumentException("");
      }
    } else if (prim->isBool()) {
      if (value == vm->upcalls->OfBool) {
        (*buf).z = vm->upcalls->boolValue->getInstanceInt8Field(obj);
        return;
      } else {
        vm->illegalArgumentException("");
      }
    } else if (prim->isInt()) {
      if (value == vm->upcalls->OfInt) {
        (*buf).i = vm->upcalls->intValue->getInstanceInt32Field(obj);
      } else if (value == vm->upcalls->OfByte) {
        (*buf).i = (sint32)vm->upcalls->byteValue->getInstanceInt8Field(obj);
      } else if (value == vm->upcalls->OfChar) {
        (*buf).i = (uint32)vm->upcalls->charValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfShort) {
        (*buf).i = (sint32)vm->upcalls->shortValue->getInstanceInt16Field(obj);
      } else {
        vm->illegalArgumentException("");
      }
      return;
    } else if (prim->isChar()) {
      if (value == vm->upcalls->OfChar) {
        (*buf).c = (uint16)vm->upcalls->charValue->getInstanceInt16Field(obj);
      } else {
        vm->illegalArgumentException("");
      }
      return;
    } else if (prim->isFloat()) {
      if (value == vm->upcalls->OfFloat) {
        (*buf).f = (float)vm->upcalls->floatValue->getInstanceFloatField(obj);
      } else if (value == vm->upcalls->OfByte) {
        (*buf).f = (float)(sint32)vm->upcalls->byteValue->getInstanceInt8Field(obj);
      } else if (value == vm->upcalls->OfChar) {
        (*buf).f = (float)(uint32)vm->upcalls->charValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfShort) {
        (*buf).f = (float)(sint32)vm->upcalls->shortValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfInt) {
        (*buf).f = (float)(sint32)vm->upcalls->intValue->getInstanceInt32Field(obj);
      } else if (value == vm->upcalls->OfLong) {
        (*buf).f = (float)vm->upcalls->longValue->getInstanceLongField(obj);
      } else {
        vm->illegalArgumentException("");
      }
      return;
    } else if (prim->isDouble()) {
      if (value == vm->upcalls->OfDouble) {
        (*buf).d = (double)vm->upcalls->doubleValue->getInstanceDoubleField(obj);
      } else if (value == vm->upcalls->OfFloat) {
        (*buf).d = (double)vm->upcalls->floatValue->getInstanceFloatField(obj);
      } else if (value == vm->upcalls->OfByte) {
        (*buf).d = (double)(sint64)vm->upcalls->byteValue->getInstanceInt8Field(obj);
      } else if (value == vm->upcalls->OfChar) {
        (*buf).d = (double)(uint64)vm->upcalls->charValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfShort) {
        (*buf).d = (double)(sint16)vm->upcalls->shortValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfInt) {
        (*buf).d = (double)(sint32)vm->upcalls->intValue->getInstanceInt32Field(obj);
      } else if (value == vm->upcalls->OfLong) {
        (*buf).d = (double)(sint64)vm->upcalls->longValue->getInstanceLongField(obj);
      } else {
        vm->illegalArgumentException("");
      }
      return;
    } else if (prim->isLong()) {
      if (value == vm->upcalls->OfByte) {
        (*buf).j = (sint64)vm->upcalls->byteValue->getInstanceInt8Field(obj);
      } else if (value == vm->upcalls->OfChar) {
        (*buf).j = (sint64)(uint64)vm->upcalls->charValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfShort) {
        (*buf).j = (sint64)vm->upcalls->shortValue->getInstanceInt16Field(obj);
      } else if (value == vm->upcalls->OfInt) {
        (*buf).j = (sint64)vm->upcalls->intValue->getInstanceInt32Field(obj);
      } else if (value == vm->upcalls->OfLong) {
        (*buf).j = (sint64)vm->upcalls->intValue->getInstanceLongField(obj);
      } else {
        vm->illegalArgumentException("");
      }
      return;
    }
  }
  // can not be here
  return;
}

bool JavaObject::instanceOf(JavaObject* self, UserCommonClass* cl) {
  llvm_gcroot(self, 0);
  if (self == NULL) return false;
  else return getClass(self)->isAssignableFrom(cl);
}
