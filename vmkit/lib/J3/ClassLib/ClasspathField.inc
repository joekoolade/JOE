//===- ClasspathVMField.cpp - GNU classpath java/lang/reflect/Field -------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "Classpath.h"
#include "ClasspathReflect.h"
#include "JavaClass.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

using namespace j3;

extern "C" {


JNIEXPORT jint JNICALL Java_java_lang_reflect_Field_getModifiersInternal(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field) {
  jint res = 0;

  llvm_gcroot(Field, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  JavaField* field = JavaObjectField::getInternalField(Field);
  res = field->access;

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Field_getType(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field) {

  JavaObject* res = 0;

  llvm_gcroot(Field, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  JnjvmClassLoader* loader = cl->classLoader;
  UserCommonClass* fieldCl = field->getSignature()->assocClass(loader);
  res = fieldCl->getClassDelegatee(vm);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jint JNICALL Java_java_lang_reflect_Field_getInt(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jint res = 0;

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  const Typedef* type = field->getSignature();
  bool stat =  isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;

    if (prim->isInt()) {
      if (stat) {
        res = (sint32)field->getStaticInt32Field();
      } else {
        res = (sint32)field->getInstanceInt32Field(obj);
      }
    } else if (prim->isChar()) {
      if (stat) {
        res = (uint32)field->getStaticInt16Field();
      } else {
        res = (uint32)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isByte()) {
      if (stat) {
        res = (sint32)field->getStaticInt8Field();
      } else {
        res = (sint32)field->getInstanceInt8Field(obj);
      }
    } else if (prim->isShort()) {
      if (stat) {
        res = (sint32)field->getStaticInt16Field();
      } else {
        res = (sint32)field->getInstanceInt16Field(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
      vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;

}

JNIEXPORT jlong JNICALL Java_java_lang_reflect_Field_getLong(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jlong res = 0;

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;

    if (prim->isInt()) {
      if (stat) {
        res = (sint64)field->getStaticInt32Field();
      } else {
        res = (sint64)field->getInstanceInt32Field(obj);
      }
    } else if (prim->isChar()) {
      if (stat) {
        res = (uint64)field->getStaticInt16Field();
      } else {
        res = (uint64)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isByte()) {
      if (stat) {
        res = (sint64)field->getStaticInt8Field();
      } else {
        res = (sint64)field->getInstanceInt8Field(obj);
      }
    } else if (prim->isShort()) {
      if (stat) {
        res = (sint64)field->getStaticInt16Field();
      } else {
        res = (sint64)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isLong()) {
      if (stat) {
        res = (sint64)field->getStaticLongField();
      } else {
        res = (sint64)field->getInstanceLongField(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jboolean JNICALL Java_java_lang_reflect_Field_getBoolean(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jboolean res = 0;

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat =  isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isBool()) {
      if (stat) {
        res = (uint8)field->getStaticInt8Field();
      } else {
        res = (uint8)field->getInstanceInt8Field(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;

}

JNIEXPORT jfloat JNICALL Java_java_lang_reflect_Field_getFloat(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jfloat res = 0;
  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isByte()) {
      if (stat) {
        res = (jfloat)field->getStaticInt8Field();
      } else {
        res = (jfloat)field->getInstanceInt8Field(obj);
      }
    } else if (prim->isInt()) {
      if (stat) {
        res = (jfloat)field->getStaticInt32Field();
      } else {
        res = (jfloat)field->getInstanceInt32Field(obj);
      }
    } else if (prim->isShort()) {
      if (stat) {
        res = (jfloat)field->getStaticInt16Field();
      } else {
        res = (jfloat)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isLong()) {
      if (stat) {
        res = (jfloat)field->getStaticLongField();
      } else {
        res = (jfloat)field->getInstanceLongField(obj);
      }
    } else if (prim->isChar()) {
      // Cast to uint32 because char is unsigned.
      if (stat) {
        res = (jfloat)(uint32)field->getStaticInt16Field();
      } else {
        res = (jfloat)(uint32)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        res = (jfloat)field->getStaticFloatField();
      } else {
        res = (jfloat)field->getInstanceFloatField(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jbyte JNICALL Java_java_lang_reflect_Field_getByte(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jbyte res = 0;
  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isByte()) {
      if (stat) {
        res = (sint8)field->getStaticInt8Field();
      } else {
        res = (sint8)field->getInstanceInt8Field(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jchar JNICALL Java_java_lang_reflect_Field_getChar(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jchar res = 0;
  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isChar()) {
      if (stat) {
        res = (uint16)field->getStaticInt16Field();
      } else {
        res = (uint16)field->getInstanceInt16Field(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;

}

JNIEXPORT jshort JNICALL Java_java_lang_reflect_Field_getShort(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {


  jshort res = 0;
  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isShort()) {
      if (stat) {
        res = (sint16)field->getStaticInt16Field();
      } else {
        res = (sint16)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isByte()) {
      if (stat) {
        res = (sint16)field->getStaticInt8Field();
      } else {
        res = (sint16)field->getInstanceInt8Field(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jdouble JNICALL Java_java_lang_reflect_Field_getDouble(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {

  jdouble res = 0;
  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isByte()) {
      if (stat) {
        res = (jdouble)(sint64)field->getStaticInt8Field();
      } else {
        res = (jdouble)(sint64)field->getInstanceInt8Field(obj);
      }
    } else if (prim->isInt()) {
      if (stat) {
        res = (jdouble)(sint64)field->getStaticInt32Field();
      } else {
        res = (jdouble)(sint64)field->getInstanceInt32Field(obj);
      }
    } else if (prim->isShort()) {
      if (stat) {
        res = (jdouble)(sint64)field->getStaticInt16Field();
      } else {
        res = (jdouble)(sint64)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isLong()) {
      if (stat) {
        res = (jdouble)(sint64)field->getStaticLongField();
      } else {
        res = (jdouble)(sint64)field->getInstanceLongField(obj);
      }
    } else if (prim->isChar()) {
      if (stat) {
        res = (jdouble)(uint64)field->getStaticInt16Field();
      } else {
        res = (jdouble)(uint64)field->getInstanceInt16Field(obj);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        res = (jdouble)field->getStaticFloatField();
      } else {
        res = (jdouble)field->getInstanceFloatField(obj);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        res = (jdouble)field->getStaticDoubleField();
      } else {
        res = (jdouble)field->getInstanceDoubleField(obj);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_reflect_Field_get(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj) {


  JavaObject* res = 0;
  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isBool()) {
      uint8 val =  stat ? field->getStaticInt8Field() :
                          field->getInstanceInt8Field(obj);
      res = vm->upcalls->boolClass->doNew(vm);
      vm->upcalls->boolValue->setInstanceInt8Field(res, val);
    } else if (prim->isByte()) {
      sint8 val =  stat ? field->getStaticInt8Field() :
                          field->getInstanceInt8Field(obj);
      res = vm->upcalls->byteClass->doNew(vm);
      vm->upcalls->byteValue->setInstanceInt8Field(res, val);
    } else if (prim->isChar()) {
      uint16 val =  stat ? field->getStaticInt16Field() :
                           field->getInstanceInt16Field(obj);
      res = vm->upcalls->charClass->doNew(vm);
      vm->upcalls->charValue->setInstanceInt16Field(res, val);
    } else if (prim->isShort()) {
      sint16 val =  stat ? field->getStaticInt16Field() :
                           field->getInstanceInt16Field(obj);
      res = vm->upcalls->shortClass->doNew(vm);
      vm->upcalls->shortValue->setInstanceInt16Field(res, val);
    } else if (prim->isInt()) {
      sint64 val =  stat ? field->getStaticInt32Field() :
                           field->getInstanceInt32Field(obj);
      res = vm->upcalls->intClass->doNew(vm);
      vm->upcalls->intValue->setInstanceInt32Field(res, val);
    } else if (prim->isLong()) {
      sint64 val =  stat ? field->getStaticLongField() :
                           field->getInstanceLongField(obj);
      res = vm->upcalls->longClass->doNew(vm);
      vm->upcalls->longValue->setInstanceLongField(res, val);
    } else if (prim->isFloat()) {
      float val =  stat ? field->getStaticFloatField() :
                          field->getInstanceFloatField(obj);
      res = vm->upcalls->floatClass->doNew(vm);
      vm->upcalls->floatValue->setInstanceFloatField(res, val);
    } else if (prim->isDouble()) {
      double val =  stat ? field->getStaticDoubleField() :
                           field->getInstanceDoubleField(obj);
      res = vm->upcalls->doubleClass->doNew(vm);
      vm->upcalls->doubleValue->setInstanceDoubleField(res, val);
    }
  } else {
    if (stat) {
      res =  field->getStaticObjectField();
    } else {
      res =  field->getInstanceObjectField(obj);
    }
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_set(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, JavaObject* val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);
  llvm_gcroot(val, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  jvalue buf;
  bool stat = isStatic(field->access);


  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  JavaObject::decapsulePrimitive(val, vm, &buf, type);


  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isBool()) {
      if (stat) {
        field->setStaticInt8Field(buf.z);
      } else {
        field->setInstanceInt8Field(obj, buf.z);
      }
    } else if (prim->isByte()) {
      if (stat) {
        field->setStaticInt8Field(buf.b);
      } else {
        field->setInstanceInt8Field(obj, buf.b);
      }
    } else if (prim->isChar()) {
      if (stat) {
        field->setStaticInt16Field(buf.c);
      } else {
        field->setInstanceInt16Field(obj, buf.c);
      }
    } else if (prim->isShort()) {
      if (stat) {
        field->setStaticInt16Field(buf.s);
      } else {
        field->setInstanceInt16Field(obj, buf.s);
      }
    } else if (prim->isInt()) {
      if (stat) {
        field->setStaticInt32Field(buf.i);
      } else {
        field->setInstanceInt32Field(obj, buf.i);
      }
    } else if (prim->isLong()) {
      if (stat) {
        field->setStaticLongField(buf.j);
      } else {
        field->setInstanceLongField(obj, buf.j);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField(buf.f);
      } else {
        field->setInstanceFloatField(obj, buf.f);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField(buf.d);
      } else {
        field->setInstanceDoubleField(obj, buf.d);
      }
    }
  } else {
    if (stat) {
      field->setStaticObjectField(val);
    } else {
      field->setInstanceObjectField(obj, val);
    }
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setBoolean(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jboolean val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isBool()) {
      if (stat) {
        field->setStaticInt8Field((uint8)val);
      } else {
        field->setInstanceInt8Field(obj, (uint8)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION

}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setByte(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jbyte val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isByte()) {
      if (stat) {
        field->setStaticInt8Field((sint8)val);
      } else {
        field->setInstanceInt8Field(obj, (sint8)val);
      }
    } else if (prim->isShort()) {
      if (stat) {
        field->setStaticInt16Field((sint16)val);
      } else {
        field->setInstanceInt16Field(obj, (sint16)val);
      }
    } else if (prim->isInt()) {
      if (stat) {
        field->setStaticInt32Field((sint32)val);
      } else {
        field->setInstanceInt32Field(obj, (sint32)val);
      }
    } else if (prim->isLong()) {
      if (stat) {
        field->setStaticLongField((sint64)val);
      } else {
        field->setInstanceLongField(obj, (sint64)val);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField((float)val);
      } else {
        field->setInstanceFloatField(obj, (float)val);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)val);
      } else {
        field->setInstanceDoubleField(obj, (double)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setChar(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jchar val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }
  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isChar()) {
      if (stat) {
        field->setStaticInt16Field((uint16)val);
      } else {
        field->setInstanceInt16Field(obj, (uint16)val);
      }
    } else if (prim->isInt()) {
      if (stat) {
        field->setStaticInt32Field((uint32)val);
      } else {
        field->setInstanceInt32Field(obj, (uint32)val);
      }
    } else if (prim->isLong()) {
      if (stat) {
        field->setStaticLongField((uint64)val);
      } else {
        field->setInstanceLongField(obj, (uint64)val);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField((float)(uint32)val);
      } else {
        field->setInstanceFloatField(obj, (float)(uint32)val);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)(uint64)val);
      } else {
        field->setInstanceDoubleField(obj, (double)(uint64)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setShort(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jshort val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isShort()) {
      if (stat) {
        field->setStaticInt16Field((sint16)val);
      } else {
        field->setInstanceInt16Field(obj, (sint16)val);
      }
    } else if (prim->isInt()) {
      if (stat) {
        field->setStaticInt32Field((sint32)val);
      } else {
        field->setInstanceInt32Field(obj, (sint32)val);
      }
    } else if (prim->isLong()) {
      if (stat) {
        field->setStaticLongField((sint64)val);
      } else {
      }
    } else if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField((float)val);
      } else {
        field->setInstanceFloatField(obj, (float)val);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)val);
      } else {
        field->setInstanceDoubleField(obj, (double)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setInt(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jint val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isInt()) {
      if (stat) {
        field->setStaticInt32Field((sint32)val);
      } else {
        field->setInstanceInt32Field(obj, (sint32)val);
      }
    } else if (prim->isLong()) {
      if (stat) {
        field->setStaticLongField((sint64)val);
      } else {
        field->setInstanceLongField(obj, (sint64)val);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField((float)val);
      } else {
        field->setInstanceFloatField(obj, (float)val);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)val);
      } else {
        field->setInstanceDoubleField(obj, (double)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setLong(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jlong val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isLong()) {
      if (stat) {
        field->setStaticLongField((sint64)val);
      } else {
        field->setInstanceLongField(obj, (sint64)val);
      }
    } else if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField((float)val);
      } else {
        field->setInstanceFloatField(obj, (float)val);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)val);
      } else {
        field->setInstanceDoubleField(obj, (double)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setFloat(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jfloat val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isFloat()) {
      if (stat) {
        field->setStaticFloatField((float)val);
      } else {
        field->setInstanceFloatField(obj, (float)val);
      }
    } else if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)val);
      } else {
        field->setInstanceDoubleField(obj, (double)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setDouble(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObjectField* Field, JavaObject* obj, jdouble val) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = JavaObjectField::getClass(Field);
  JavaField* field = JavaObjectField::getInternalField(Field);
  bool stat = isStatic(field->access);

  if (stat) {
    cl->initialiseClass(vm);
  } else {
    verifyNull(obj);
  }

  const Typedef* type = field->getSignature();
  if (type->isPrimitive()) {
    const PrimitiveTypedef* prim = (const PrimitiveTypedef*)type;
    if (prim->isDouble()) {
      if (stat) {
        field->setStaticDoubleField((double)val);
      } else {
        field->setInstanceDoubleField(obj, (double)val);
      }
    } else {
      vm->illegalArgumentException("wrong type");
    }
  } else {
    vm->illegalArgumentException("wrong type");
  }

  END_NATIVE_EXCEPTION
}

// Never throws.
JNIEXPORT jlong JNICALL Java_sun_misc_Unsafe_objectFieldOffset(
#ifdef NATIVE_JNI
JNIEnv *env,
#endif
JavaObject* Unsafe, JavaObjectField* Field) {

  llvm_gcroot(Field, 0);
  llvm_gcroot(Unsafe, 0);

  JavaField* field = JavaObjectField::getInternalField(Field);
  return (jlong)field->ptrOffset;
}

}
