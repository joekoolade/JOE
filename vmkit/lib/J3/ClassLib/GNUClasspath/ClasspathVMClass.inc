//===---- ClasspathVMClass.cpp - GNU classpath java/lang/VMClass ----------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "types.h"

#include "Classpath.h"
#include "ClasspathReflect.h"
#include "JavaAccess.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaTypes.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"

using namespace j3;

extern "C" {

// Never throws
JNIEXPORT jboolean JNICALL Java_java_lang_VMClass_isArray(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObjectClass* klass) {

  llvm_gcroot(klass, 0);

  UserCommonClass* cl = JavaObjectClass::getClass(klass);

  return cl->isArray();
  
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_forName(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaString* str, 
jboolean clinit, 
JavaObject* loader) {

  JavaObject* res = 0;
  llvm_gcroot(loader, 0);
  llvm_gcroot(str, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  verifyNull(str);
  Jnjvm* vm = JavaThread::get()->getJVM();
  JnjvmClassLoader* JCL = 
    JnjvmClassLoader::getJnjvmLoaderFromJavaObject(loader, vm);
  UserCommonClass* cl = JCL->loadClassFromJavaString(str, true, false);

  if (cl != NULL && !cl->isPrimitive()) {
    if (clinit && cl->asClass()) {
      cl->asClass()->initialiseClass(vm);
    }
    res = cl->getClassDelegatee(vm);
  } else {
    vm->classNotFoundException(str);
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getDeclaredConstructors(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* Cl, 
jboolean publicOnly) {

  ArrayObject* ret = 0;
  JavaObject* tmp = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(ret, 0);
  llvm_gcroot(tmp, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);

  if (cl->isArray() || cl->isInterface() || cl->isPrimitive()) {
    ret = (ArrayObject*)vm->upcalls->constructorArrayClass->doNew(0, vm);
  } else {
    UserClass* realCl = cl->asClass();;
    JnjvmClassLoader* classLoader = cl->classLoader;
    uint32 size = 0;
    
    for (uint32 i = 0; i < realCl->nbVirtualMethods; ++i) {
      JavaMethod* meth = &realCl->virtualMethods[i];
      bool pub = isPublic(meth->access);
      if (meth->name->equals(classLoader->bootstrapLoader->initName) && 
          (!publicOnly || pub)) {
        ++size;
      }
    }
  
    ret = (ArrayObject*)vm->upcalls->constructorArrayClass->doNew(size, vm);

    sint32 index = 0;
    for (uint32 i = 0; i < realCl->nbVirtualMethods; ++i) {
      JavaMethod* meth = &realCl->virtualMethods[i];
      bool pub = isPublic(meth->access);
      if (meth->name->equals(classLoader->bootstrapLoader->initName) && 
          (!publicOnly || pub)) {
        UserClass* Cons = vm->upcalls->newConstructor;
        tmp = Cons->doNew(vm);
        vm->upcalls->initConstructor->invokeIntSpecial(vm, Cons, tmp, &Cl, i);
        ArrayObject::setElement(ret, tmp, index);
        index++;
      }
    }
  }

  END_NATIVE_EXCEPTION

  return ret;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getDeclaredMethods(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* Cl, 
jboolean publicOnly) {

  ArrayObject* ret = 0;
  JavaObject* tmp = 0;
  JavaString* str = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(ret, 0);
  llvm_gcroot(tmp, 0);
  llvm_gcroot(str, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  Classpath* upcalls = vm->upcalls;

  if (cl->isArray() || cl->isPrimitive()) {
    ret = (ArrayObject*)upcalls->methodArrayClass->doNew(0, vm);
  } else {
    UserClass* realCl = cl->asClass();
    JnjvmClassLoader* classLoader = cl->classLoader;
    uint32 size = 0;

    for (uint32 i = 0; i < realCl->nbVirtualMethods + realCl->nbStaticMethods;
         ++i) {
      JavaMethod* meth = &realCl->virtualMethods[i];
      bool pub = isPublic(meth->access);
      if (!(meth->name->equals(classLoader->bootstrapLoader->initName)) && 
          !(meth->name->equals(classLoader->bootstrapLoader->clinitName)) && 
          (!publicOnly || pub)) {
        ++size; 
      }
    }

    
    ret = (ArrayObject*)upcalls->methodArrayClass->doNew(size, vm);

    sint32 index = 0;
    for (uint32 i = 0; i < realCl->nbVirtualMethods + realCl->nbStaticMethods;
         ++i) {
      JavaMethod* meth = &realCl->virtualMethods[i];
      bool pub = isPublic(meth->access);
      if (!(meth->name->equals(classLoader->bootstrapLoader->initName)) && 
          !(meth->name->equals(classLoader->bootstrapLoader->clinitName)) && 
          (!publicOnly || pub)) {
        // TODO: check parameter types
        UserClass* Meth = vm->upcalls->newMethod;
        tmp = Meth->doNew(vm);
        str = vm->internalUTF8ToStr(meth->name);
        upcalls->initMethod->invokeIntSpecial(vm, Meth, tmp, &Cl, &str, i);
        ArrayObject::setElement(ret, tmp, index);
        index++;
      }
    }
  }

  END_NATIVE_EXCEPTION

  return ret;
}

JNIEXPORT jint JNICALL Java_java_lang_VMClass_getModifiers(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* Cl, 
jboolean ignore) {
  
  jint res = 0;
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  res = cl->getAccess();

  if (isEnum(res) && cl->getSuper() != vm->upcalls->EnumClass) {
    // javac may put that flag to inner classes of enum classes. 
    res &= ~ACC_ENUM;
  }

  END_NATIVE_EXCEPTION
  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getName(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {

  JavaObject* result = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(result, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  
  const UTF8* iname = cl->getName();
  result = JavaString::internalToJava(iname, vm);

  END_NATIVE_EXCEPTION

  return result;
}

JNIEXPORT jboolean JNICALL Java_java_lang_VMClass_isPrimitive(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {
 
  jboolean res = 0;
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  
  res = cl->isPrimitive();

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jboolean JNICALL Java_java_lang_VMClass_isInterface(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {

  jboolean res = 0;
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);

  res = cl->isInterface();

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getComponentType(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(Cl, 0);

  
  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);

  if (cl->isArray()) {
    UserCommonClass* bc = cl->asArrayClass()->baseClass();
    res = bc->getClassDelegatee(vm);
  } else {
    res = 0;
  }

  END_NATIVE_EXCEPTION
  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getClassLoader(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {

  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  res = cl->classLoader->getJavaClassLoader();

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jboolean JNICALL Java_java_lang_VMClass_isAssignableFrom(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl1, JavaObject* Cl2) {
  
  jboolean res = 0;
  llvm_gcroot(Cl1, 0);
  llvm_gcroot(Cl2, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  if (!Cl2) vm->nullPointerException();

  UserCommonClass* cl1 = UserCommonClass::resolvedImplClass(vm, Cl1, false);
  UserCommonClass* cl2 = UserCommonClass::resolvedImplClass(vm, Cl2, false);

  res = cl2->isAssignableFrom(cl1);

  END_NATIVE_EXCEPTION

  return res;

}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getSuperclass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {

  JavaObject* res = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  if (cl->isInterface()) res = 0;
  else {
    if (cl->getSuper()) res = cl->getSuper()->getClassDelegatee(vm);
    else res = 0;
  }

  END_NATIVE_EXCEPTION
  
  return res;
}

JNIEXPORT bool JNICALL Java_java_lang_VMClass_isInstance(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl, JavaObject* obj) {

  bool res = false;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(obj, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  res = JavaObject::instanceOf(obj, cl);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getDeclaredFields(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl, jboolean publicOnly) {

  ArrayObject* ret = 0;
  JavaObject* tmp = 0;
  JavaString* name = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(ret, 0);
  llvm_gcroot(tmp, 0);
  llvm_gcroot(name, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);

  if (!cl->isClass()) {
    ret = (ArrayObject*)vm->upcalls->fieldArrayClass->doNew(0, vm);
  } else {
    UserClass* realCl = cl->asClass(); 
    uint32 size = 0;
    for (uint32 i = 0; i < realCl->nbVirtualFields + realCl->nbStaticFields;
         ++i) {
      JavaField* field = &realCl->virtualFields[i];
      if (!publicOnly || isPublic(field->access)) {
        ++size;
      }
    }


    ret = (ArrayObject*)vm->upcalls->fieldArrayClass->doNew(size, vm);

    sint32 index = 0;
    for (uint32 i = 0; i < realCl->nbVirtualFields + realCl->nbStaticFields;
         ++i) {
      JavaField* field = &realCl->virtualFields[i];
      if (!publicOnly || isPublic(field->access)) {
        // TODO: check parameter types
        UserClass* Field = vm->upcalls->newField;
        tmp = Field->doNew(vm);
        name = vm->internalUTF8ToStr(field->name);
        vm->upcalls->initField->invokeIntSpecial(vm, Field, tmp, &Cl, &name, i);
        ArrayObject::setElement(ret, tmp, index);
        index++;
      }
    }
  }

  END_NATIVE_EXCEPTION

  return ret;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getInterfaces(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {

  ArrayObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  res = (ArrayObject*)vm->upcalls->classArrayClass->doNew(cl->nbInterfaces, vm);

  for (uint16 i = 0; i < cl->nbInterfaces; ++i) {
    UserClass* klass = cl->interfaces[i];
    ArrayObject::setElement(res, klass->getClassDelegatee(vm), i);
  }

  END_NATIVE_EXCEPTION

  return res;
}


JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getDeclaringClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {
  JavaObject* res = 0;

  llvm_gcroot(res, 0);
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false)->asClass();

  if (cl) {
    cl->resolveInnerOuterClasses();
    UserClass* outer = cl->getOuterClass();
    if (outer) {
      res = outer->getClassDelegatee(vm);
    }
  }

  END_NATIVE_EXCEPTION

  return res;

}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClass_getDeclaredClasses(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl, bool publicOnly) {

  ArrayObject* result = 0;
  llvm_gcroot(result, 0);
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false)->asClass();
  if (cl) {
    cl->resolveInnerOuterClasses();
    UserClassArray* array = vm->upcalls->constructorArrayClass;
    
    uint16 sizeArray = 0;

    if (publicOnly) {
      for (uint16 i = 0; i < cl->nbInnerClasses; ++i) {
        UserClass* klass = cl->innerClasses[i];
        if (isPublic(klass->innerAccess)) ++sizeArray;
      }
    } else {
      sizeArray = cl->nbInnerClasses;
    }

    result = (ArrayObject*)array->doNew(sizeArray, vm);
    int index = 0;
    for (uint16 i = 0; i < cl->nbInnerClasses; ++i) {
      UserClass* klass = cl->innerClasses[i];
      if (!publicOnly || isPublic(klass->innerAccess))
        ArrayObject::setElement(result, klass->getClassDelegatee(vm), index++); 
    }
    assert(index == sizeArray);
  }
  

  END_NATIVE_EXCEPTION

  return result;

}

// Only throws.
JNIEXPORT void JNICALL Java_java_lang_VMClass_throwException(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* throwable) {
  
  llvm_gcroot(throwable, 0);

  assert(throwable && "Using internal VM throw exception without exception");
  JavaThread::get()->throwException(throwable);
}

JNIEXPORT ArrayObject* Java_java_lang_VMClass_getDeclaredAnnotations(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {
  // TODO implement me
  
  ArrayObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(Cl, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClassArray* array = vm->upcalls->constructorArrayAnnotation;
  res = (ArrayObject*)array->doNew(0, vm);

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT jboolean Java_java_lang_VMClass_isAnonymousClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* Cl) {
 
  jboolean res = false;
  llvm_gcroot(Cl, 0);

  BEGIN_NATIVE_EXCEPTION(0)
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false)->asClass();

  if (cl) res = cl->isAnonymous;

  END_NATIVE_EXCEPTION

  return res;
}


}
