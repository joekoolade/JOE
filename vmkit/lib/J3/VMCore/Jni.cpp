//===---------------- Jni.cpp - Jni interface for J3 ----------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <jni.h>

#include "mvm/System.h"

#include "ClasspathReflect.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "Reader.h"

using namespace j3;

#define NYI() do { \
  fprintf(stderr, "%s: Implement me!\n", __FUNCTION__); \
} while(0)

Jnjvm* myVM(JNIEnv* env) {
  return JavaThread::get()->getJVM();
}

UserClass* getClassFromStaticMethod(Jnjvm* vm, JavaMethod* meth,
                                    JavaObject* clazz) {
  llvm_gcroot(clazz, 0);
  return meth->classDef;
}

static UserClass* getClassFromVirtualMethod(Jnjvm* vm, 
                                            JavaMethod* meth,
                                            UserCommonClass* cl) {
  return meth->classDef;
}

extern "C" const struct JNIInvokeInterface_ JNI_JavaVMTable;
extern "C" struct JNINativeInterface_ JNI_JNIEnvTable;

jint GetVersion(JNIEnv *env) {
  return JNI_VERSION_1_4;
}


jclass DefineClass(JNIEnv *env, const char *name, jobject _loader,
				   const jbyte *buf, jsize len) {
  BEGIN_JNI_EXCEPTION

  JavaObject * loader = _loader ? *(JavaObject**)_loader : 0;
  llvm_gcroot(loader, 0);

  jclass res;

  Jnjvm* vm = JavaThread::get()->getJVM();
  JnjvmClassLoader* JCL =
    JnjvmClassLoader::getJnjvmLoaderFromJavaObject(loader, vm);

  ClassBytes * bytes = new (JCL->allocator, len) ClassBytes(len);
  memcpy(bytes->elements,buf,len);
  const UTF8* utfName = JCL->asciizConstructUTF8(name);
  UserClass *cl = JCL->constructClass(utfName, bytes);

  if (cl) res = (jclass)cl->getClassDelegateePtr(vm);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION

  return 0;
}


jclass FindClass(JNIEnv *env, const char *asciiz) {
  
  BEGIN_JNI_EXCEPTION

  JnjvmClassLoader* loader = 0;
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  UserClass* currentClass = th->getCallingClassLevel(0);
  if (currentClass) loader = currentClass->classLoader;
  else loader = vm->appClassLoader;

  UserCommonClass* cl = loader->loadClassFromAsciiz(asciiz, true, true);
  if (cl && cl->asClass()) {
    assert(cl->asClass()->isResolved());
    cl->asClass()->initialiseClass(vm);
  }
  jclass res = (jclass)cl->getClassDelegateePtr(vm);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}
  

jmethodID FromReflectedMethod(JNIEnv *env, jobject method) {
  
  BEGIN_JNI_EXCEPTION
 
  // Local object references. 
  JavaObject* meth = *(JavaObject**)method;
  llvm_gcroot(meth, 0);

  Jnjvm* vm = myVM(env);
  Classpath* upcalls = vm->upcalls;
  UserCommonClass* cl = JavaObject::getClass(meth);
  if (cl == upcalls->newConstructor)  {
    jmethodID res = (jmethodID)JavaObjectMethod::getInternalMethod((JavaObjectMethod*)meth);
    RETURN_FROM_JNI(res);
  } else if (cl == upcalls->newMethod) {
    jmethodID res = (jmethodID)JavaObjectConstructor::getInternalMethod((JavaObjectConstructor*)meth);
    RETURN_FROM_JNI(res);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jclass GetSuperclass(JNIEnv *env, jclass sub) {
  BEGIN_JNI_EXCEPTION

  jclass res = 0;
  JavaObject* Cl = *(JavaObject**)sub;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(res, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  if (!cl->isInterface() && cl->getSuper() != NULL) {
    res = (jclass)cl->getSuper()->getClassDelegateePtr(vm);
  }
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION

  RETURN_FROM_JNI(0);
}
  
 
jboolean IsAssignableFrom(JNIEnv *env, jclass _sub, jclass _sup) {
  
  BEGIN_JNI_EXCEPTION
 
  // Local object references.
  JavaObject* sub = *(JavaObject**)_sub;
  JavaObject* sup = *(JavaObject**)_sup;
  llvm_gcroot(sub, 0);
  llvm_gcroot(sup, 0);
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl2 = 
    UserCommonClass::resolvedImplClass(vm, sup, false);
  UserCommonClass* cl1 = 
    UserCommonClass::resolvedImplClass(vm, sub, false);

  jboolean res = (jboolean)cl1->isAssignableFrom(cl2);
  RETURN_FROM_JNI(res);
  
  END_JNI_EXCEPTION

  RETURN_FROM_JNI(false);
}


jint Throw(JNIEnv *env, jthrowable obj) {
  BEGIN_JNI_EXCEPTION
  JavaThread::get()->pendingException = *(JavaObject**)obj;

  RETURN_FROM_JNI(0);

  END_JNI_EXCEPTION

  RETURN_FROM_JNI(-1);
}


jint ThrowNew(JNIEnv* env, jclass _Cl, const char *msg) {
  
  BEGIN_JNI_EXCEPTION
 
  verifyNull(_Cl);
  // Local object references.
  JavaObject* Cl = *(JavaObject**)_Cl;
  JavaObject* res = 0;
  JavaString* str = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(res, 0);
  llvm_gcroot(str, 0);
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, true);
  if (!cl->isClass()) RETURN_FROM_JNI(0);

  UserClass* realCl = cl->asClass();
  res = realCl->doNew(vm);
  JavaMethod* init = realCl->lookupMethod(vm->bootstrapLoader->initName,
                                          vm->bootstrapLoader->initExceptionSig,
                                          false, true, 0);
  str = vm->asciizToStr(msg);
  init->invokeIntSpecial(vm, realCl, res, &str);
  th->pendingException = res;
  
  RETURN_FROM_JNI(0);
  
  END_JNI_EXCEPTION

  RETURN_FROM_JNI(-1);
}


jthrowable ExceptionOccurred(JNIEnv *env) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* obj = JavaThread::get()->pendingException;
  llvm_gcroot(obj, 0);
  if (obj == NULL) RETURN_FROM_JNI(NULL);
  jthrowable res = (jthrowable)th->pushJNIRef(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION

  RETURN_FROM_JNI(0);
}


void ExceptionDescribe(JNIEnv *env) {
  NYI();
  abort();
}


void ExceptionClear(JNIEnv *env) {
  NYI();
  abort();
}


void FatalError(JNIEnv *env, const char *msg) {
  NYI();
  abort();
}


jint PushLocalFrame(JNIEnv* env, jint capacity) {
  NYI();
  abort();
  return 0;
}

jobject PopLocalFrame(JNIEnv* env, jobject result) {
  NYI();
  abort();
  return 0;
}


void DeleteLocalRef(JNIEnv *env, jobject localRef) {
}


jboolean IsSameObject(JNIEnv *env, jobject ref1, jobject ref2) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaObject* Ref1 = ref1 ? *(JavaObject**)ref1 : NULL;
  JavaObject* Ref2 = ref2 ? *(JavaObject**)ref2 : NULL;
  llvm_gcroot(Ref1, 0);
  llvm_gcroot(Ref2, 0);

  RETURN_FROM_JNI(Ref1 == Ref2);
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(false);
}


jobject NewLocalRef(JNIEnv *env, jobject ref) {
  NYI();
  abort();
  return 0;
}


jint EnsureLocalCapacity(JNIEnv* env, jint capacity) {
  // Assume we have capacity
  return 0;
}


jobject AllocObject(JNIEnv *env, jclass _clazz) {
  
  BEGIN_JNI_EXCEPTION
 
  // Local object references.  
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();

  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);
  if (!cl->isClass()) RETURN_FROM_JNI(0);

  // Store local reference
  res = cl->asClass()->doNew(vm);
 
  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject NewObject(JNIEnv *env, jclass _clazz, jmethodID methodID, ...) {
  BEGIN_JNI_EXCEPTION
  
 
  // Local object references
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);
  if (!cl->isClass()) RETURN_FROM_JNI(0);
  
  // Store local reference
  res = cl->asClass()->doNew(vm);

  va_list ap;
  va_start(ap, methodID);
  meth->invokeIntSpecialAP(vm, cl->asClass(), res, ap);
  va_end(ap);
 
  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject NewObjectV(JNIEnv* env, jclass _clazz, jmethodID methodID,
                   va_list args) {
  BEGIN_JNI_EXCEPTION

  // Local object references
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  JavaMethod* meth = (JavaMethod*)methodID;
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);

  // Store local reference
  res = cl->asClass()->doNew(vm);

  meth->invokeIntSpecialAP(vm, cl->asClass(), res, args);

  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}

jobject NewObjectA(JNIEnv* env, jclass _clazz, jmethodID methodID,
                   const jvalue *args) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  JavaMethod* meth = (JavaMethod*)methodID;
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);
  
  // Store local reference
  res = cl->asClass()->doNew(vm);

  meth->invokeIntSpecialBuf(vm, cl->asClass(), res, (void*)args);

  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jclass GetObjectClass(JNIEnv *env, jobject _obj) {
  
  BEGIN_JNI_EXCEPTION

  // Local object references
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  
  // Store local reference
  jclass res = (jclass)JavaObject::getClass(obj)->getClassDelegateePtr(vm);
  RETURN_FROM_JNI(res);
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean IsInstanceOf(JNIEnv *env, jobject _obj, jclass clazz) {
  bool res = false;
  JavaObject * obj = 0;
  JavaObject * Cl = 0;
  llvm_gcroot(Cl, 0);
  llvm_gcroot(obj, 0);

  BEGIN_JNI_EXCEPTION

  obj = *(JavaObject**)_obj;
  Cl = *(JavaObject**)clazz;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, Cl, false);
  res = JavaObject::instanceOf(obj, cl);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION

  return JNI_FALSE;
}


jfieldID FromReflectedField(JNIEnv* env, jobject field) {
  NYI();
  abort();
  return 0;
}


jobject ToReflectedMethod(JNIEnv* env, jclass cls, jmethodID methodID,
                               jboolean isStatic) {
  NYI();
  abort();
  return 0;
}


jobject ToReflectedField(JNIEnv* env, jclass cls, jfieldID fieldID,
			 jboolean isStatic) {
  NYI();
  abort();
  return 0;
}


jmethodID GetMethodID(JNIEnv* env, jclass _clazz, const char *aname,
		                  const char *atype) {
  
  BEGIN_JNI_EXCEPTION
 
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);

  UserClass* realCl = cl->isClass() ? cl->asClass() : cl->super;

  const UTF8* name = cl->classLoader->hashUTF8->lookupAsciiz(aname);
  if (name) {
    const UTF8* type = cl->classLoader->hashUTF8->lookupAsciiz(atype);
    if (type) {
      JavaMethod* meth = realCl->lookupMethod(name, type, false, true, 0);
      RETURN_FROM_JNI((jmethodID)meth);
    }
  }

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject CallObjectMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {

  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  JavaObject* res = 0;
  llvm_gcroot(obj, 0);
  llvm_gcroot(res, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  
  res = meth->invokeJavaObjectVirtualAP(vm, cl, obj, ap);
  va_end(ap);

  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject CallObjectMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                          va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
 
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  JavaObject* res = 0;
  llvm_gcroot(obj, 0);
  llvm_gcroot(res, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  
  // Store local reference.
  res = meth->invokeJavaObjectVirtualAP(vm, cl, obj, args);
  
  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jobject CallObjectMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                          const jvalue * args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  JavaObject* res = 0;
  llvm_gcroot(obj, 0);
  llvm_gcroot(res, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  res = meth->invokeJavaObjectVirtualBuf(vm, cl, obj, (void*)args);

  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jboolean CallBooleanMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {

  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.  
  JavaObject* self = *(JavaObject**)_obj;
  llvm_gcroot(self, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl =
      getClassFromVirtualMethod(vm, meth, JavaObject::getClass(self));
  
  uint32 res = meth->invokeIntVirtualAP(vm, cl, self, ap);
  va_end(ap);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean CallBooleanMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                            va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jboolean res = (jboolean)meth->invokeIntVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jboolean CallBooleanMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                            const jvalue * args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jboolean res = (jboolean)meth->invokeIntVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jbyte CallByteMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}

jbyte CallByteMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                      va_list args) {
  
  BEGIN_JNI_EXCEPTION
 
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jbyte res = (jbyte)meth->invokeIntVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jbyte CallByteMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                      const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jbyte res = (jbyte)meth->invokeIntVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jchar CallCharMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jchar CallCharMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                      va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
 
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jchar res = (jchar)meth->invokeIntVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jchar CallCharMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                      const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jchar res = (jchar)meth->invokeIntVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jshort CallShortMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jshort CallShortMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                        va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jshort res = (jshort)meth->invokeIntVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jshort CallShortMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                        const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jshort res = (jshort)meth->invokeIntVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}



jint CallIntMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {

  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  
  uint32 res = meth->invokeIntVirtualAP(vm, cl, obj, ap);
  va_end(ap);
  
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jint CallIntMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                    va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  
  jint res = (jint)meth->invokeIntVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jint CallIntMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                    const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jint res = (jint)meth->invokeIntVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}



jlong CallLongMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jlong CallLongMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                      va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jlong res = (jlong)meth->invokeLongVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jlong CallLongMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                      const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jlong res = (jlong)meth->invokeLongVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}



jfloat CallFloatMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jfloat res = meth->invokeFloatVirtualAP(vm, cl, obj, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION;
  RETURN_FROM_JNI(0.0);
}


jfloat CallFloatMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                        va_list args) {
  
  BEGIN_JNI_EXCEPTION
 
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jfloat res = (jfloat)meth->invokeFloatVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0.0f);
}


jfloat CallFloatMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                        const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jfloat res = (jfloat)meth->invokeFloatVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0.0f);
}



jdouble CallDoubleMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jdouble res = meth->invokeDoubleVirtualAP(vm, cl, obj, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0.0);
}


jdouble CallDoubleMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                          va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  jdouble res = (jdouble)meth->invokeDoubleVirtualAP(vm, cl, obj, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0.0);

}


jdouble CallDoubleMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                          const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj)); 
  jdouble res = (jdouble)meth->invokeDoubleVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0.0);
}



void CallVoidMethod(JNIEnv *env, jobject _obj, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  va_list ap;
  va_start(ap, methodID);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  meth->invokeIntVirtualAP(vm, cl, obj, ap);
  va_end(ap);

  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void CallVoidMethodV(JNIEnv *env, jobject _obj, jmethodID methodID,
                     va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  meth->invokeIntVirtualAP(vm, cl, obj, args);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void CallVoidMethodA(JNIEnv *env, jobject _obj, jmethodID methodID,
                     const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  verifyNull(_obj);

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  
  meth->invokeIntVirtualBuf(vm, cl, obj, (void*)args);

  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}



jobject CallNonvirtualObjectMethod(JNIEnv *env, jobject obj, jclass clazz,
                                   jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jobject CallNonvirtualObjectMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                    jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jobject CallNonvirtualObjectMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                    jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jboolean CallNonvirtualBooleanMethod(JNIEnv *env, jobject obj, jclass clazz,
                                     jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jboolean CallNonvirtualBooleanMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                      jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jboolean CallNonvirtualBooleanMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                      jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}


jbyte CallNonvirtualByteMethod(JNIEnv *env, jobject obj, jclass clazz,
                               jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jbyte CallNonvirtualByteMethodV (JNIEnv *env, jobject obj, jclass clazz,
                                 jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jbyte CallNonvirtualByteMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jchar CallNonvirtualCharMethod(JNIEnv *env, jobject obj, jclass clazz,
                               jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jchar CallNonvirtualCharMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jchar CallNonvirtualCharMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jshort CallNonvirtualShortMethod(JNIEnv *env, jobject obj, jclass clazz,
                                 jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jshort CallNonvirtualShortMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                  jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jshort CallNonvirtualShortMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                  jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jint CallNonvirtualIntMethod(JNIEnv *env, jobject obj, jclass clazz,
                             jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jint CallNonvirtualIntMethodV(JNIEnv *env, jobject obj, jclass clazz,
                              jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jint CallNonvirtualIntMethodA(JNIEnv *env, jobject obj, jclass clazz,
                              jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jlong CallNonvirtualLongMethod(JNIEnv *env, jobject obj, jclass clazz,
                               jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jlong CallNonvirtualLongMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jlong CallNonvirtualLongMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jfloat CallNonvirtualFloatMethod(JNIEnv *env, jobject obj, jclass clazz,
                                 jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jfloat CallNonvirtualFloatMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                  jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jfloat CallNonvirtualFloatMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                  jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



jdouble CallNonvirtualDoubleMethod(JNIEnv *env, jobject obj, jclass clazz,
                                   jmethodID methodID, ...) {
  NYI();
  abort();
  return 0;
}


jdouble CallNonvirtualDoubleMethodV(JNIEnv *env, jobject obj, jclass clazz,
                                    jmethodID methodID, va_list args) {
  NYI();
  abort();
  return 0;
}


jdouble CallNonvirtualDoubleMethodA(JNIEnv *env, jobject obj, jclass clazz,
                                    jmethodID methodID, const jvalue *args) {
  NYI();
  abort();
  return 0;
}



void CallNonvirtualVoidMethod(JNIEnv *env, jobject _obj, jclass clazz,
                              jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  verifyNull(_obj);
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromVirtualMethod(vm, meth, JavaObject::getClass(obj));
  meth->invokeIntSpecialAP(vm, cl, obj, ap);
  va_end(ap);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void CallNonvirtualVoidMethodV(JNIEnv *env, jobject obj, jclass clazz,
                               jmethodID methodID, va_list args) {
  NYI();
  abort();
}


void CallNonvirtualVoidMethodA(JNIEnv *env, jobject obj, jclass clazz,
                               jmethodID methodID, const jvalue * args) {
  NYI();
  abort();
}


jfieldID GetFieldID(JNIEnv *env, jclass _clazz, const char *aname,
		    const char *sig)  {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);

  if (cl->isClass()) {
    const UTF8* name = cl->classLoader->hashUTF8->lookupAsciiz(aname);
    if (name) {
      const UTF8* type = cl->classLoader->hashUTF8->lookupAsciiz(sig);
      if (type) {
        JavaField* field = cl->asClass()->lookupField(name, type, false, true,
                                                      0);
        RETURN_FROM_JNI((jfieldID)field);
      }
    }
  }
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);

}


jobject GetObjectField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  JavaObject* res = 0;
  llvm_gcroot(obj, 0);
  llvm_gcroot(res, 0);

  JavaField* field = (JavaField*)fieldID;

  // Store local reference.
  res = field->getInstanceObjectField(obj);

  JavaThread* th = JavaThread::get();
  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean GetBooleanField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  uint8 res = (uint8)field->getInstanceInt8Field(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyte GetByteField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  sint8 res = (sint8)field->getInstanceInt8Field(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jchar GetCharField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  uint16 res = (uint16)field->getInstanceInt16Field(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshort GetShortField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  sint16 res = (sint16)field->getInstanceInt16Field(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jint GetIntField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  sint32 res = (sint32)field->getInstanceInt32Field(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jlong GetLongField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  sint64 res = (sint64)field->getInstanceLongField(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jfloat GetFloatField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  jfloat res = (jfloat)field->getInstanceFloatField(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jdouble GetDoubleField(JNIEnv *env, jobject _obj, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  jdouble res = (jdouble)field->getInstanceDoubleField(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void SetObjectField(JNIEnv *env, jobject _obj, jfieldID fieldID, jobject _value) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  JavaObject* value = *(JavaObject**)_value;
  llvm_gcroot(obj, 0);
  llvm_gcroot(value, 0);

  JavaField* field = (JavaField*)fieldID;
  field->setInstanceObjectField(obj, value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetBooleanField(JNIEnv *env, jobject _obj, jfieldID fieldID,
                     jboolean value) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  field->setInstanceInt8Field(obj, (uint8)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetByteField(JNIEnv *env, jobject _obj, jfieldID fieldID, jbyte value) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  field->setInstanceInt8Field(obj, (uint8)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION

  RETURN_VOID_FROM_JNI;
}


void SetCharField(JNIEnv *env, jobject _obj, jfieldID fieldID, jchar value) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);

  JavaField* field = (JavaField*)fieldID;
  field->setInstanceInt16Field(obj, (uint16)value);
  
  RETURN_VOID_FROM_JNI;
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetShortField(JNIEnv *env, jobject _obj, jfieldID fieldID, jshort value) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  field->setInstanceInt16Field(obj, (sint16)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetIntField(JNIEnv *env, jobject _obj, jfieldID fieldID, jint value) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  field->setInstanceInt32Field(obj, (sint32)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetLongField(JNIEnv *env, jobject _obj, jfieldID fieldID, jlong value) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  field->setInstanceLongField(obj, (sint64)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetFloatField(JNIEnv *env, jobject _obj, jfieldID fieldID, jfloat value) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  field->setInstanceFloatField(obj, (float)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetDoubleField(JNIEnv *env, jobject _obj, jfieldID fieldID, jdouble value) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* obj = *(JavaObject**)_obj;
  llvm_gcroot(obj, 0);
  
  JavaField* field = (JavaField*)fieldID;
  field->setInstanceDoubleField(obj, (float)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


jmethodID GetStaticMethodID(JNIEnv *env, jclass _clazz, const char *aname,
			    const char *atype) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);

  if (cl->isClass()) {
    const UTF8* name = cl->classLoader->hashUTF8->lookupAsciiz(aname);
    if (name) {
      const UTF8* type = cl->classLoader->hashUTF8->lookupAsciiz(atype);
      if (type) {
        JavaMethod* meth = cl->asClass()->lookupMethod(name, type, true, true,
                                                       0);
        RETURN_FROM_JNI((jmethodID)meth);
      }
    }
  }

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject CallStaticObjectMethod(JNIEnv *env, jclass _clazz, jmethodID methodID,
                               ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
 
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);


  JavaMethod* meth = (JavaMethod*)methodID;
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  
  // Store local reference.
  res = meth->invokeJavaObjectStaticAP(vm, cl, ap);
  va_end(ap);
  
  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject CallStaticObjectMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                     va_list args) {
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  
  // Store local reference.
  res = meth->invokeJavaObjectStaticAP(vm, cl, args);

  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject CallStaticObjectMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  JavaObject* res = 0;
  llvm_gcroot(clazz, 0);
  llvm_gcroot(res, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 

  res = meth->invokeJavaObjectStaticBuf(vm, cl, (void*)args);

  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean CallStaticBooleanMethod(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                 ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  uint32 res = meth->invokeIntStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean CallStaticBooleanMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                  va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jboolean res = (jboolean)meth->invokeIntStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean CallStaticBooleanMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                  const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jboolean res = (jboolean) meth->invokeIntStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyte CallStaticByteMethod(JNIEnv *env, jclass _clazz, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  va_list ap;
  va_start(ap, methodID);
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jbyte res = (jbyte) meth->invokeIntStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyte CallStaticByteMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                            va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jbyte res = (jbyte)meth->invokeIntStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyte CallStaticByteMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                            const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jbyte res = (jbyte) meth->invokeIntStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jchar CallStaticCharMethod(JNIEnv *env, jclass _clazz, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jchar res = (jchar) meth->invokeIntStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jchar CallStaticCharMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                            va_list args) {
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jchar res = (jchar)meth->invokeIntStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jchar CallStaticCharMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                            const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jchar res = (jchar) meth->invokeIntStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshort CallStaticShortMethod(JNIEnv *env, jclass _clazz, jmethodID methodID,
                             ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jshort res = (jshort) meth->invokeIntStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshort CallStaticShortMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                              va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jshort res = (jshort)meth->invokeIntStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshort CallStaticShortMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                              const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jshort res = (jshort) meth->invokeIntStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jint CallStaticIntMethod(JNIEnv *env, jclass _clazz, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jint res = (jint) meth->invokeIntStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jint CallStaticIntMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                          va_list args) {
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jint res = (jint)meth->invokeIntStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jint CallStaticIntMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                          const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jint res = (jint) meth->invokeIntStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jlong CallStaticLongMethod(JNIEnv *env, jclass _clazz, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jlong res = (jlong) meth->invokeLongStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jlong CallStaticLongMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
			    va_list args) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jlong res = (jlong)meth->invokeLongStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0);
}


jlong CallStaticLongMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                            const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jlong res = (jlong) meth->invokeLongStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}



jfloat CallStaticFloatMethod(JNIEnv *env, jclass _clazz, jmethodID methodID,
                             ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jfloat res = (jfloat) meth->invokeFloatStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0.0f);
}


jfloat CallStaticFloatMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                              va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jfloat res = (jfloat)meth->invokeFloatStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0.0f);
}


jfloat CallStaticFloatMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                              const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jfloat res = (jfloat) meth->invokeFloatStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jdouble CallStaticDoubleMethod(JNIEnv *env, jclass _clazz, jmethodID methodID,
                               ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jdouble res = (jdouble) meth->invokeDoubleStaticAP(vm, cl, ap);
  va_end(ap);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0.0);
}


jdouble CallStaticDoubleMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                va_list args) {
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  jdouble res = (jdouble)meth->invokeDoubleStaticAP(vm, cl, args);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  
  RETURN_FROM_JNI(0.0);
}


jdouble CallStaticDoubleMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                                const jvalue *args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  jdouble res = (jdouble) meth->invokeDoubleStaticBuf(vm, cl, (void*)args);

  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void CallStaticVoidMethod(JNIEnv *env, jclass _clazz, jmethodID methodID, ...) {
  
  BEGIN_JNI_EXCEPTION
  
  va_list ap;
  va_start(ap, methodID);
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);

  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  meth->invokeIntStaticAP(vm, cl, ap);
  va_end(ap);

  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void CallStaticVoidMethodV(JNIEnv *env, jclass _clazz, jmethodID methodID,
                           va_list args) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz);
  meth->invokeIntStaticAP(vm, cl, args);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void CallStaticVoidMethodA(JNIEnv *env, jclass _clazz, jmethodID methodID,
                           const jvalue * args) {
  BEGIN_JNI_EXCEPTION

  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  JavaMethod* meth = (JavaMethod*)methodID;
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClass* cl = getClassFromStaticMethod(vm, meth, clazz); 
  meth->invokeIntStaticBuf(vm, cl, (void*)args);

  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


jfieldID GetStaticFieldID(JNIEnv *env, jclass _clazz, const char *aname,
                          const char *sig) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* clazz = *(JavaObject**)_clazz;
  llvm_gcroot(clazz, 0);
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass* cl = UserCommonClass::resolvedImplClass(vm, clazz, true);
  
  if (cl->isClass()) {
    const UTF8* name = cl->classLoader->hashUTF8->lookupAsciiz(aname);
    if (name) {
      const UTF8* type = cl->classLoader->hashUTF8->lookupAsciiz(sig);
      if (type) {
        JavaField* field = cl->asClass()->lookupField(name, type, true, true,
                                                      0);
        RETURN_FROM_JNI((jfieldID)field);
      }
    }
  }

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject GetStaticObjectField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* obj = 0;
  llvm_gcroot(obj, 0);

  JavaThread* th = JavaThread::get();
  JavaField* field = (JavaField*)fieldID;
  obj = field->getStaticObjectField();
  jobject res = (jobject)th->pushJNIRef(obj);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean GetStaticBooleanField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  jboolean res = (jboolean)field->getStaticInt8Field();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyte GetStaticByteField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  jbyte res = (jbyte)field->getStaticInt8Field();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jchar GetStaticCharField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  JavaField* field = (JavaField*)fieldID;
  jchar res = (jchar)field->getStaticInt16Field();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshort GetStaticShortField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  JavaField* field = (JavaField*)fieldID;
  jshort res = (jshort)field->getStaticInt16Field();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jint GetStaticIntField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  JavaField* field = (JavaField*)fieldID;
  jint res = (jint)field->getStaticInt32Field();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jlong GetStaticLongField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  jlong res = (jlong)field->getStaticLongField();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jfloat GetStaticFloatField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  jfloat res = (jfloat)field->getStaticFloatField();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jdouble GetStaticDoubleField(JNIEnv *env, jclass _clazz, jfieldID fieldID) {

  BEGIN_JNI_EXCEPTION

  JavaField* field = (JavaField*)fieldID;
  jdouble res = (jdouble)field->getStaticDoubleField();
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void SetStaticObjectField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                          jobject _value) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* value = *(JavaObject**)_value;
  llvm_gcroot(value, 0);

  JavaField* field = (JavaField*)fieldID;
  field->setStaticObjectField(value);
  
  RETURN_VOID_FROM_JNI;
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticBooleanField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                           jboolean value) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticInt8Field((uint8)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticByteField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                        jbyte value) {

  BEGIN_JNI_EXCEPTION

  JavaField* field = (JavaField*)fieldID;
  field->setStaticInt8Field((sint8)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticCharField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                        jchar value) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticInt16Field((uint16)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticShortField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                         jshort value) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticInt16Field((sint16)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticIntField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                       jint value) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticInt32Field((sint32)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticLongField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                        jlong value) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticLongField((sint64)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticFloatField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                         jfloat value) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticFloatField((float)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetStaticDoubleField(JNIEnv *env, jclass _clazz, jfieldID fieldID,
                          jdouble value) {

  BEGIN_JNI_EXCEPTION
  
  JavaField* field = (JavaField*)fieldID;
  field->setStaticDoubleField((double)value);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION

  RETURN_VOID_FROM_JNI;
}


jstring NewString(JNIEnv *env, const jchar *buf, jsize len) {
  BEGIN_JNI_EXCEPTION

  JavaString* obj = NULL;
  ArrayUInt16* tmp = NULL;
  llvm_gcroot(obj, 0);
  llvm_gcroot(tmp, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();

  tmp = (ArrayUInt16*)vm->upcalls->ArrayOfChar->doNew(len, vm);

  for (sint32 i = 0; i < len; i++) {
    ArrayUInt16::setElement(tmp, buf[i], i);
  }

  obj = vm->constructString(tmp);
  assert(obj->count == len);
  jstring ret = (jstring)th->pushJNIRef(obj);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jsize GetStringLength(JNIEnv *env, jstring _str) {
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaString* str = *(JavaString**)_str;
  llvm_gcroot(str, 0);

  RETURN_FROM_JNI(str->count);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


const jchar *GetStringChars(JNIEnv *env, jstring str, jboolean *isCopy) {
  NYI();
  abort();
  return 0;
}


void ReleaseStringChars(JNIEnv *env, jstring str, const jchar *chars) {
  NYI();
  abort();
}


jstring NewStringUTF(JNIEnv *env, const char *bytes) {

  BEGIN_JNI_EXCEPTION

  JavaObject* obj = NULL;
  llvm_gcroot(obj, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  obj = vm->asciizToStr(bytes);
  jstring ret = (jstring)th->pushJNIRef(obj);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jsize GetStringUTFLength (JNIEnv *env, jstring string) {
  BEGIN_JNI_EXCEPTION
  JavaThread* th = JavaThread::get();

  JavaString * s = *(JavaString**)string;
  llvm_gcroot(s, 0);
  RETURN_FROM_JNI(s->count);
  END_JNI_EXCEPTION

  RETURN_FROM_JNI(0)
}


const char *GetStringUTFChars(JNIEnv *env, jstring _string, jboolean *isCopy) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaString* string = *(JavaString**)_string;
  llvm_gcroot(string, 0);

  if (isCopy != 0) (*isCopy) = true;
  const char* res = JavaString::strToAsciiz(string);
  RETURN_FROM_JNI(res);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void ReleaseStringUTFChars(JNIEnv *env, jstring _string, const char *utf) {
  delete[] utf;
}


jsize GetArrayLength(JNIEnv *env, jarray _array) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* array = *(JavaObject**)_array;
  llvm_gcroot(array, 0);

  RETURN_FROM_JNI(JavaArray::getSize(array));

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobjectArray NewObjectArray(JNIEnv *env, jsize length, jclass _elementClass,
                            jobject _initialElement) {
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  JavaObject* elementClass = *(JavaObject**)_elementClass;
  JavaObject* initialElement = _initialElement ? 
    *(JavaObject**)_initialElement : 0;
  ArrayObject* res = 0;
  llvm_gcroot(elementClass, 0);
  llvm_gcroot(initialElement, 0);
  llvm_gcroot(res, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();

  if (length < 0) vm->negativeArraySizeException(length);
  
  UserCommonClass* base =
    UserCommonClass::resolvedImplClass(vm, elementClass, true);
  JnjvmClassLoader* loader = base->classLoader;
  const UTF8* name = base->getName();
  const UTF8* arrayName = loader->constructArrayName(1, name);
  UserClassArray* array = loader->constructArray(arrayName, base);
  res = (ArrayObject*)array->doNew(length, vm);
  
  if (initialElement) {
    for (sint32 i = 0; i < length; ++i) {
      ArrayObject::setElement(res, initialElement, i);
    }
  }
  
  jobjectArray ret = (jobjectArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jobject GetObjectArrayElement(JNIEnv *env, jobjectArray _array, jsize index) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArrayObject* array = *(ArrayObject**)_array;
  JavaObject* res = 0;
  llvm_gcroot(array, 0);
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  
  if (index >= ArrayObject::getSize(array)) {
    vm->indexOutOfBounds(array, index);
  }
  
  // Store local refererence.
  res = ArrayObject::getElement(array, index);
  
  jobject ret = (jobject)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);
  
  END_JNI_EXCEPTION

  RETURN_FROM_JNI(0);
}


void SetObjectArrayElement(JNIEnv *env, jobjectArray _array, jsize index,
                           jobject _val) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArrayObject* array = *(ArrayObject**)_array;
  JavaObject* val = *(JavaObject**)_val;
  llvm_gcroot(array, 0);
  llvm_gcroot(val, 0);

  if (index >= ArrayObject::getSize(array)) {
    JavaThread::get()->getJVM()->indexOutOfBounds(array, index);
  }
  
  // Store global reference.
  ArrayObject::setElement(array, val, index);
  
  RETURN_VOID_FROM_JNI;

  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


jbooleanArray NewBooleanArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfBool->doNew(len, vm);
  jbooleanArray ret = (jbooleanArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);


  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyteArray NewByteArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaObject* res = NULL;
  llvm_gcroot(res, 0);

  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfByte->doNew(len, vm);
  jbyteArray ret = (jbyteArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jcharArray NewCharArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfChar->doNew(len, vm);
  jcharArray ret = (jcharArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshortArray NewShortArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfShort->doNew(len, vm);
  jshortArray ret = (jshortArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jintArray NewIntArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfInt->doNew(len, vm);
  jintArray ret = (jintArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jlongArray NewLongArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfLong->doNew(len, vm);
  jlongArray ret = (jlongArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jfloatArray NewFloatArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfFloat->doNew(len, vm);
  jfloatArray ret = (jfloatArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jdoubleArray NewDoubleArray(JNIEnv *env, jsize len) {
  
  BEGIN_JNI_EXCEPTION

  JavaObject* res = NULL;
  llvm_gcroot(res, 0);
  
  JavaThread* th = JavaThread::get();
  Jnjvm* vm = th->getJVM();
  res = vm->upcalls->ArrayOfDouble->doNew(len, vm);
  jdoubleArray ret = (jdoubleArray)th->pushJNIRef(res);
  RETURN_FROM_JNI(ret);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jboolean* GetBooleanArrayElements(JNIEnv *env, jbooleanArray _array,
				                          jboolean *isCopy) {
  
  BEGIN_JNI_EXCEPTION
 
  // Local object references.
  ArrayUInt8* array = *(ArrayUInt8**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  sint32 len = ArrayUInt8::getSize(array) * sizeof(uint8);
  void* buffer = malloc(len);
  memcpy(buffer, ArrayUInt8::getElements(array), len);

  RETURN_FROM_JNI((jboolean*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jbyte *GetByteArrayElements(JNIEnv *env, jbyteArray _array, jboolean *isCopy) {

  BEGIN_JNI_EXCEPTION

  // Local object references.
  ArraySInt8* array = *(ArraySInt8**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  sint32 len = ArraySInt8::getSize(array) * sizeof(uint8);
  void* buffer = malloc(len);
  memcpy(buffer, ArraySInt8::getElements(array), len);

  RETURN_FROM_JNI((jbyte*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jchar *GetCharArrayElements(JNIEnv *env, jcharArray _array, jboolean *isCopy) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArrayUInt16* array = *(ArrayUInt16**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  sint32 len = ArrayUInt16::getSize(array) * sizeof(uint16);
  void* buffer = malloc(len);
  memcpy(buffer, ArrayUInt16::getElements(array), len);

  RETURN_FROM_JNI((jchar*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jshort *GetShortArrayElements(JNIEnv *env, jshortArray _array,
                              jboolean *isCopy) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArraySInt16* array = *(ArraySInt16**)_array;
  llvm_gcroot(array, 0);
  
  if (isCopy) (*isCopy) = true;

  sint32 len = ArraySInt16::getSize(array) * sizeof(sint16);
  void* buffer = malloc(len);
  memcpy(buffer, ArraySInt16::getElements(array), len);

  RETURN_FROM_JNI((jshort*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jint *GetIntArrayElements(JNIEnv *env, jintArray _array, jboolean *isCopy) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArraySInt32* array = *(ArraySInt32**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  sint32 len = ArraySInt32::getSize(array) * sizeof(sint32);
  void* buffer = malloc(len);
  memcpy(buffer, ArraySInt32::getElements(array), len);

  RETURN_FROM_JNI((jint*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jlong *GetLongArrayElements(JNIEnv *env, jlongArray _array, jboolean *isCopy) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArrayLong* array = *(ArrayLong**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  sint32 len = ArrayLong::getSize(array) * sizeof(sint64);
  void* buffer = malloc(len);
  memcpy(buffer, ArrayLong::getElements(array), len);

  RETURN_FROM_JNI((jlong*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jfloat *GetFloatArrayElements(JNIEnv *env, jfloatArray _array,
                              jboolean *isCopy) {

  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArrayFloat* array = *(ArrayFloat**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  sint32 len = ArrayFloat::getSize(array) * sizeof(float);
  void* buffer = malloc(len);
  memcpy(buffer, ArrayFloat::getElements(array), len);

  RETURN_FROM_JNI((jfloat*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


jdouble *GetDoubleArrayElements(JNIEnv *env, jdoubleArray _array,
				jboolean *isCopy) {
  
  BEGIN_JNI_EXCEPTION
  
  // Local object references.
  ArrayDouble* array = *(ArrayDouble**)_array;
  llvm_gcroot(array, 0);
  
  if (isCopy) (*isCopy) = true;

  sint32 len = ArrayDouble::getSize(array) * sizeof(double);
  void* buffer = malloc(len);
  memcpy(buffer, ArrayDouble::getElements(array), len);

  RETURN_FROM_JNI((jdouble*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void ReleaseBooleanArrayElements(JNIEnv *env, jbooleanArray _array,
				 jboolean *elems, jint mode) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayUInt8* array = *(ArrayUInt8**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArrayUInt8::getSize(array);
    memcpy(ArrayUInt8::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseByteArrayElements(JNIEnv *env, jbyteArray _array, jbyte *elems,
			      jint mode) {
  
  BEGIN_JNI_EXCEPTION

  ArraySInt16* array = *(ArraySInt16**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArraySInt16::getSize(array);
    memcpy(ArraySInt16::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseCharArrayElements(JNIEnv *env, jcharArray _array, jchar *elems,
			      jint mode) {
  
  BEGIN_JNI_EXCEPTION

  ArrayUInt16* array = *(ArrayUInt16**)_array;
  llvm_gcroot(array, 0);

  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArrayUInt16::getSize(array) << 1;
    memcpy(ArrayUInt16::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseShortArrayElements(JNIEnv *env, jshortArray _array, jshort *elems,
			       jint mode) {
  
  BEGIN_JNI_EXCEPTION

  ArraySInt16* array = *(ArraySInt16**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArraySInt16::getSize(array) << 1;
    memcpy(ArraySInt16::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseIntArrayElements(JNIEnv *env, jintArray _array, jint *elems,
			     jint mode) {
  
  BEGIN_JNI_EXCEPTION
    
  ArraySInt32* array = *(ArraySInt32**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArraySInt32::getSize(array) << 2;
    memcpy(ArraySInt32::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseLongArrayElements(JNIEnv *env, jlongArray _array, jlong *elems,
			      jint mode) {
  
  BEGIN_JNI_EXCEPTION
    
  ArrayLong* array = *(ArrayLong**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArrayLong::getSize(array) << 3;
    memcpy(ArrayLong::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseFloatArrayElements(JNIEnv *env, jfloatArray _array, jfloat *elems,
			       jint mode) {
  BEGIN_JNI_EXCEPTION
    
  ArrayFloat* array = *(ArrayFloat**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArrayFloat::getSize(array) << 2;
    memcpy(ArrayFloat::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void ReleaseDoubleArrayElements(JNIEnv *env, jdoubleArray _array,
				jdouble *elems, jint mode) {
  
  BEGIN_JNI_EXCEPTION
    
  ArrayDouble* array = *(ArrayDouble**)_array;
  llvm_gcroot(array, 0);
  
  if (mode == JNI_ABORT) {
    free(elems);
  } else {
    sint32 len = ArrayDouble::getSize(array) << 3;
    memcpy(ArrayDouble::getElements(array), elems, len);

    if (mode == 0) free(elems);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start,
			   jsize len, jboolean *buf) {
  BEGIN_JNI_EXCEPTION
  
  ArrayUInt8* Array = *(ArrayUInt8**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArrayUInt8::getElements(Array) + start, len * sizeof(uint8));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len,
			jbyte *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArraySInt8* Array = *(ArraySInt8**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArraySInt8::getElements(Array) + start, len * sizeof(uint8));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len,
			jchar *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayUInt16* Array = *(ArrayUInt16**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArrayUInt16::getElements(Array) + start, len * sizeof(uint16));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start,
			 jsize len, jshort *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArraySInt16* Array = *(ArraySInt16**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArraySInt16::getElements(Array) + start, len * sizeof(sint16));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len,
		       jint *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArraySInt32* Array = *(ArraySInt32**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArraySInt32::getElements(Array) + start, len * sizeof(sint32));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len,
		        jlong *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayLong* Array = *(ArrayLong**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArrayLong::getElements(Array) + start, len * sizeof(sint64));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start,
			 jsize len, jfloat *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayFloat* Array = *(ArrayFloat**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArrayFloat::getElements(Array) + start, len * sizeof(float));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start,
			  jsize len, jdouble *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayDouble* Array = *(ArrayDouble**)array;
  llvm_gcroot(Array, 0);
  memcpy(buf, ArrayDouble::getElements(Array) + start, len * sizeof(double));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start,
			   jsize len, const jboolean *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayUInt8* Array = *(ArrayUInt8**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArrayUInt8::getElements(Array) + start, buf, len * sizeof(uint8));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len,
			                  const jbyte *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArraySInt8* Array = *(ArraySInt8**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArraySInt8::getElements(Array) + start, buf, len * sizeof(sint8));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len,
			                  const jchar *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayUInt16* Array = *(ArrayUInt16**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArrayUInt16::getElements(Array) + start, buf, len * sizeof(uint16));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start,
			                   jsize len, const jshort *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArraySInt16* Array = *(ArraySInt16**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArraySInt16::getElements(Array) + start, buf, len * sizeof(sint16));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len,
		                   const jint *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArraySInt32* Array = *(ArraySInt32**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArraySInt32::getElements(Array) + start, buf, len * sizeof(sint32));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetLongArrayRegion(JNIEnv* env, jlongArray array, jsize start, jsize len,
			                  const jlong *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayLong* Array = *(ArrayLong**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArrayLong::getElements(Array) + start, buf, len * sizeof(sint64));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start,
			                   jsize len, const jfloat *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayFloat* Array = *(ArrayFloat**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArrayFloat::getElements(Array) + start, buf, len * sizeof(float));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


void SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start,
			                    jsize len, const jdouble *buf) {
  
  BEGIN_JNI_EXCEPTION
  
  ArrayDouble* Array = *(ArrayDouble**)array;
  llvm_gcroot(Array, 0);
  memcpy(ArrayDouble::getElements(Array) + start, buf, len * sizeof(double));
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


jint RegisterNatives(JNIEnv *env, jclass _clazz, const JNINativeMethod *methods,
		     jint nMethods) {
  BEGIN_JNI_EXCEPTION

  JavaObject * clazz = 0;
  llvm_gcroot(clazz, 0);
  clazz = *(JavaObject**)_clazz;

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass * Cl = UserCommonClass::resolvedImplClass(vm, clazz, false);
  // TODO: Don't assert, throw exceptions!
  assert(Cl);
  UserClass * cl = Cl->asClass();

  for(int i = 0; i < nMethods; ++i)
  {
    const UTF8* name = cl->classLoader->hashUTF8->lookupAsciiz(methods[i].name);
    const UTF8* sign = cl->classLoader->hashUTF8->lookupAsciiz(methods[i].signature);
    assert(name);
    assert(sign);

    JavaMethod * meth = cl->lookupMethodDontThrow(name, sign, true, true, 0);
    if (!meth) meth = cl->lookupMethodDontThrow(name, sign, false, true, 0);

    // TODO: Don't assert, throw exceptions!
    assert(meth);
    assert(isNative(meth->access));

    cl->classLoader->registerNative(meth,(word_t)methods[i].fnPtr);
  }

  RETURN_FROM_JNI(0)

  END_JNI_EXCEPTION

  RETURN_FROM_JNI(0)
}


jint UnregisterNatives(JNIEnv *env, jclass clazz) {
  NYI();
  abort();
  return 0;
}

jint MonitorEnter(JNIEnv *env, jobject _obj) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaObject* Obj = *(JavaObject**)_obj;
  llvm_gcroot(Obj, 0);
  
  if (Obj != NULL) {
    JavaObject::acquire(Obj);
    RETURN_FROM_JNI(0);
  } else {
    RETURN_FROM_JNI(-1);
  }


  END_JNI_EXCEPTION
  RETURN_FROM_JNI(-1);
}


jint MonitorExit(JNIEnv *env, jobject _obj) {

  BEGIN_JNI_EXCEPTION

  JavaObject* Obj = *(JavaObject**)_obj;
  llvm_gcroot(Obj, 0);
 
  if (Obj != NULL) {

    if (!JavaObject::owner(Obj)) {
      JavaThread::get()->getJVM()->illegalMonitorStateException(Obj);    
    }
  
    JavaObject::release(Obj);
    RETURN_FROM_JNI(0);
  } else {
    RETURN_FROM_JNI(-1);
  }

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(-1);
}


jint GetJavaVM(JNIEnv *env, JavaVM **vm) {
  BEGIN_JNI_EXCEPTION
  Jnjvm* myvm = JavaThread::get()->getJVM();
  (*vm) = (JavaVM*)(void*)(&(myvm->javavmEnv));
  RETURN_FROM_JNI(0);
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void GetStringRegion(JNIEnv* env, jstring str, jsize start, jsize len,
                     jchar *buf) {
  BEGIN_JNI_EXCEPTION

  JavaString * s = *(JavaString**)str;
  llvm_gcroot(s, 0);
  UserClass * cl = JavaObject::getClass(s)->asClass();
  const UTF8 * utf = JavaString::javaToInternal(s, cl->classLoader->hashUTF8);

  ssize_t end = start+len;
  if (end > utf->size) {
    assert(0 && "Throw string out of bounds exception here!");
  }

  Jnjvm* vm = JavaThread::get()->getJVM();
  UTF8Map* map = vm->bootstrapLoader->hashUTF8;

  const UTF8 * result = utf->extract(map, start, start + len);
  assert(result->size == len);
  for(sint32 i = 0; i < len; ++i)
    buf[i] = result->elements[i];

  RETURN_VOID_FROM_JNI;
  END_JNI_EXCEPTION
  RETURN_VOID_FROM_JNI;
}


void GetStringUTFRegion(JNIEnv* env, jstring str, jsize start, jsize len,
                        char *buf) {
  BEGIN_JNI_EXCEPTION

  JavaString * s = *(JavaString**)str;
  llvm_gcroot(s, 0);

  int end = start+len;
  if (end > s->count) {
    assert(0 && "Throw string out of bounds exception here!");
  }

  char * internalStr = JavaString::strToAsciiz(s);
  assert((int)strlen(internalStr) == len);
  memcpy(buf, internalStr, len + 1);

  RETURN_VOID_FROM_JNI;
  END_JNI_EXCEPTION
  RETURN_VOID_FROM_JNI;
}


void *GetPrimitiveArrayCritical(JNIEnv *env, jarray _array, jboolean *isCopy) {
  BEGIN_JNI_EXCEPTION
  
  JavaObject* array = *(JavaObject**)_array;
  llvm_gcroot(array, 0);

  if (isCopy) (*isCopy) = true;

  UserClassArray* cl = JavaObject::getClass(array)->asArrayClass();
  uint32 logSize = cl->baseClass()->asPrimitiveClass()->logSize;
  sint32 len = JavaArray::getSize(array) << logSize;
  void* buffer = malloc(len);
  memcpy(buffer, JavaArray::getElements(array), len);

  RETURN_FROM_JNI((jchar*)buffer);

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void ReleasePrimitiveArrayCritical(JNIEnv *env, jarray _array, void *carray,
				   jint mode) {
  
  BEGIN_JNI_EXCEPTION
  
  JavaObject* array = *(JavaObject**)_array;
  llvm_gcroot(array, 0);

  if (mode == JNI_ABORT) {
    free(carray);
  } else {
    UserClassArray* cl = JavaObject::getClass(array)->asArrayClass();
    uint32 logSize = cl->baseClass()->asPrimitiveClass()->logSize;
    sint32 len = JavaArray::getSize(array) << logSize;
    memcpy(JavaArray::getElements(array), carray, len);

    if (mode == 0) free(carray);
  }
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


const jchar *GetStringCritical(JNIEnv *env, jstring string, jboolean *isCopy) {
  NYI();
  abort();
  return 0;
}


void ReleaseStringCritical(JNIEnv *env, jstring string, const jchar *cstring) {
  NYI();
  abort();
}


jweak NewWeakGlobalRef(JNIEnv* env, jobject obj) {
  NYI();
  abort();
  return 0;
}


void DeleteWeakGlobalRef(JNIEnv* env, jweak ref) {
  NYI();
  abort();
}


jobject NewGlobalRef(JNIEnv* env, jobject obj) {
  
  BEGIN_JNI_EXCEPTION
    
  JavaObject* Obj = NULL;
  llvm_gcroot(Obj, 0);
  
  // Local object references.
  if (obj) {
    Obj = *(JavaObject**)obj;
    llvm_gcroot(Obj, 0);

    Jnjvm* vm = JavaThread::get()->getJVM();


    vm->globalRefsLock.lock();
    JavaObject** res = vm->globalRefs.addJNIReference(Obj);
    vm->globalRefsLock.unlock();

    RETURN_FROM_JNI((jobject)res);
  } else {
    RETURN_FROM_JNI(0);
  }
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}


void DeleteGlobalRef(JNIEnv* env, jobject globalRef) {
  
  BEGIN_JNI_EXCEPTION
  
  Jnjvm* vm = myVM(env);
  vm->globalRefsLock.lock();
  vm->globalRefs.removeJNIReference((JavaObject**)globalRef);
  vm->globalRefsLock.unlock();
  
  END_JNI_EXCEPTION
  
  RETURN_VOID_FROM_JNI;
}


jboolean ExceptionCheck(JNIEnv *env) {
  BEGIN_JNI_EXCEPTION
  
  if (JavaThread::get()->pendingException) {
    RETURN_FROM_JNI(JNI_TRUE);
  } else {
    RETURN_FROM_JNI(JNI_FALSE);
  }
  
  END_JNI_EXCEPTION
  RETURN_FROM_JNI(false);
}




jlong GetDirectBufferCapacity(JNIEnv* env, jobject buf) {
  NYI();
  abort();
  return 0;
}

jobjectRefType GetObjectRefType(JNIEnv* env, jobject obj) {
  NYI();
  abort();
  return (jobjectRefType)0;
}



jint DestroyJavaVM(JavaVM *vm) {
  NYI();
  abort();
  return 0;
}


jint AttachCurrentThread(JavaVM *vm, void **env, void *thr_args) {
  NYI();
  abort();
  return 0;
}


jint DetachCurrentThread(JavaVM *vm) {
  NYI();
  abort();
  return 0;
}


jint GetEnv(JavaVM *vm, void **env, jint version) {

  BEGIN_JNI_EXCEPTION

  JavaThread* _th = JavaThread::get();
  JavaObject* obj = _th->currentThread();
  llvm_gcroot(obj, 0);

  Jnjvm* myvm = _th->getJVM();
  if (obj != 0) {
    (*env) = &(myvm->jniEnv);
    RETURN_FROM_JNI(JNI_OK);
  } else {
    (*env) = 0;
    RETURN_FROM_JNI(JNI_EDETACHED);
  }

  END_JNI_EXCEPTION
  RETURN_FROM_JNI(0);
}



jint AttachCurrentThreadAsDaemon(JavaVM *vm, void **par1, void *par2) {
  NYI();
  abort();
  return 0;
}

// Pull in implementation-specific JNI methods
#ifdef USE_OPENJDK
#include "JniOpenJDK.inc"
#else
#include "JniClasspath.inc"
#endif

const struct JNIInvokeInterface_ JNI_JavaVMTable = {
	NULL,
	NULL,
	NULL,

	DestroyJavaVM,
	AttachCurrentThread,
	DetachCurrentThread,
	GetEnv,
	AttachCurrentThreadAsDaemon
};


struct JNINativeInterface_ JNI_JNIEnvTable = {
	NULL,
	NULL,
	NULL,
	NULL,    
	&GetVersion,

	&DefineClass,
	&FindClass,
	&FromReflectedMethod,
	&FromReflectedField,
	&ToReflectedMethod,
	&GetSuperclass,
	&IsAssignableFrom,
	&ToReflectedField,

	&Throw,
	&ThrowNew,
	&ExceptionOccurred,
	&ExceptionDescribe,
	&ExceptionClear,
	&FatalError,
	&PushLocalFrame,
	&PopLocalFrame,

	&NewGlobalRef,
	&DeleteGlobalRef,
	&DeleteLocalRef,
	&IsSameObject,
	&NewLocalRef,
	&EnsureLocalCapacity,

	&AllocObject,
	&NewObject,
	&NewObjectV,
	&NewObjectA,

	&GetObjectClass,
	&IsInstanceOf,

	&GetMethodID,

	&CallObjectMethod,
	&CallObjectMethodV,
	&CallObjectMethodA,
	&CallBooleanMethod,
	&CallBooleanMethodV,
	&CallBooleanMethodA,
	&CallByteMethod,
	&CallByteMethodV,
	&CallByteMethodA,
	&CallCharMethod,
	&CallCharMethodV,
	&CallCharMethodA,
	&CallShortMethod,
	&CallShortMethodV,
	&CallShortMethodA,
	&CallIntMethod,
	&CallIntMethodV,
	&CallIntMethodA,
	&CallLongMethod,
	&CallLongMethodV,
	&CallLongMethodA,
	&CallFloatMethod,
	&CallFloatMethodV,
	&CallFloatMethodA,
	&CallDoubleMethod,
	&CallDoubleMethodV,
	&CallDoubleMethodA,
	&CallVoidMethod,
	&CallVoidMethodV,
	&CallVoidMethodA,

	&CallNonvirtualObjectMethod,
	&CallNonvirtualObjectMethodV,
	&CallNonvirtualObjectMethodA,
	&CallNonvirtualBooleanMethod,
	&CallNonvirtualBooleanMethodV,
	&CallNonvirtualBooleanMethodA,
	&CallNonvirtualByteMethod,
	&CallNonvirtualByteMethodV,
	&CallNonvirtualByteMethodA,
	&CallNonvirtualCharMethod,
	&CallNonvirtualCharMethodV,
	&CallNonvirtualCharMethodA,
	&CallNonvirtualShortMethod,
	&CallNonvirtualShortMethodV,
	&CallNonvirtualShortMethodA,
	&CallNonvirtualIntMethod,
	&CallNonvirtualIntMethodV,
	&CallNonvirtualIntMethodA,
	&CallNonvirtualLongMethod,
	&CallNonvirtualLongMethodV,
	&CallNonvirtualLongMethodA,
	&CallNonvirtualFloatMethod,
	&CallNonvirtualFloatMethodV,
	&CallNonvirtualFloatMethodA,
	&CallNonvirtualDoubleMethod,
	&CallNonvirtualDoubleMethodV,
	&CallNonvirtualDoubleMethodA,
	&CallNonvirtualVoidMethod,
	&CallNonvirtualVoidMethodV,
	&CallNonvirtualVoidMethodA,

	&GetFieldID,

	&GetObjectField,
	&GetBooleanField,
	&GetByteField,
	&GetCharField,
	&GetShortField,
	&GetIntField,
	&GetLongField,
	&GetFloatField,
	&GetDoubleField,
	&SetObjectField,
	&SetBooleanField,
	&SetByteField,
	&SetCharField,
	&SetShortField,
	&SetIntField,
	&SetLongField,
	&SetFloatField,
	&SetDoubleField,

	&GetStaticMethodID,

	&CallStaticObjectMethod,
	&CallStaticObjectMethodV,
	&CallStaticObjectMethodA,
	&CallStaticBooleanMethod,
	&CallStaticBooleanMethodV,
	&CallStaticBooleanMethodA,
	&CallStaticByteMethod,
	&CallStaticByteMethodV,
	&CallStaticByteMethodA,
	&CallStaticCharMethod,
	&CallStaticCharMethodV,
	&CallStaticCharMethodA,
	&CallStaticShortMethod,
	&CallStaticShortMethodV,
	&CallStaticShortMethodA,
	&CallStaticIntMethod,
	&CallStaticIntMethodV,
	&CallStaticIntMethodA,
	&CallStaticLongMethod,
	&CallStaticLongMethodV,
	&CallStaticLongMethodA,
	&CallStaticFloatMethod,
	&CallStaticFloatMethodV,
	&CallStaticFloatMethodA,
	&CallStaticDoubleMethod,
	&CallStaticDoubleMethodV,
	&CallStaticDoubleMethodA,
	&CallStaticVoidMethod,
	&CallStaticVoidMethodV,
	&CallStaticVoidMethodA,

	&GetStaticFieldID,

	&GetStaticObjectField,
	&GetStaticBooleanField,
	&GetStaticByteField,
	&GetStaticCharField,
	&GetStaticShortField,
	&GetStaticIntField,
	&GetStaticLongField,
	&GetStaticFloatField,
	&GetStaticDoubleField,
	&SetStaticObjectField,
	&SetStaticBooleanField,
	&SetStaticByteField,
	&SetStaticCharField,
	&SetStaticShortField,
	&SetStaticIntField,
	&SetStaticLongField,
	&SetStaticFloatField,
	&SetStaticDoubleField,

	&NewString,
	&GetStringLength,
	&GetStringChars,
	&ReleaseStringChars,

	&NewStringUTF,
	&GetStringUTFLength,
	&GetStringUTFChars,
	&ReleaseStringUTFChars,

	&GetArrayLength,

	&NewObjectArray,
	&GetObjectArrayElement,
	&SetObjectArrayElement,

	&NewBooleanArray,
	&NewByteArray,
	&NewCharArray,
	&NewShortArray,
	&NewIntArray,
	&NewLongArray,
	&NewFloatArray,
	&NewDoubleArray,

	&GetBooleanArrayElements,
	&GetByteArrayElements,
	&GetCharArrayElements,
	&GetShortArrayElements,
	&GetIntArrayElements,
	&GetLongArrayElements,
	&GetFloatArrayElements,
	&GetDoubleArrayElements,

	&ReleaseBooleanArrayElements,
	&ReleaseByteArrayElements,
	&ReleaseCharArrayElements,
	&ReleaseShortArrayElements,
	&ReleaseIntArrayElements,
	&ReleaseLongArrayElements,
	&ReleaseFloatArrayElements,
	&ReleaseDoubleArrayElements,

	&GetBooleanArrayRegion,
	&GetByteArrayRegion,
	&GetCharArrayRegion,
	&GetShortArrayRegion,
	&GetIntArrayRegion,
	&GetLongArrayRegion,
	&GetFloatArrayRegion,
	&GetDoubleArrayRegion,
	&SetBooleanArrayRegion,
	&SetByteArrayRegion,
	&SetCharArrayRegion,
	&SetShortArrayRegion,
	&SetIntArrayRegion,
	&SetLongArrayRegion,
	&SetFloatArrayRegion,
	&SetDoubleArrayRegion,

	&RegisterNatives,
	&UnregisterNatives,

	&MonitorEnter,
	&MonitorExit,

	&GetJavaVM,

	/* new JNI 1.2 functions */

	&GetStringRegion,
	&GetStringUTFRegion,

	&GetPrimitiveArrayCritical,
	&ReleasePrimitiveArrayCritical,

	&GetStringCritical,
	&ReleaseStringCritical,

	&NewWeakGlobalRef,
	&DeleteWeakGlobalRef,

	&ExceptionCheck,

	/* new JNI 1.4 functions */

	&NewDirectByteBuffer,
	&GetDirectBufferAddress,
	&GetDirectBufferCapacity,

  /* ---- JNI 1.6 functions ---- */
  &GetObjectRefType
};
