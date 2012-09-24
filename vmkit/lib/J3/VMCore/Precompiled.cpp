//===-------- Precompiled.cpp - Support for precompiled code --------------===//
//
//                          The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

// for dlopen and dlsym
#include <dlfcn.h> 
#include "mvm/MethodInfo.h"

#include "JavaClass.h"
#include "JavaUpcalls.h"
#include "JnjvmClassLoader.h"
#include "Jnjvm.h"
#include "LockedMap.h"

namespace j3 {

typedef void (*static_init_t)(JnjvmClassLoader*);

void JnjvmClassLoader::insertAllMethodsInVM(Jnjvm* vm) {
  UNIMPLEMENTED();
}


void JnjvmClassLoader::loadLibFromJar(Jnjvm* vm, const char* name,
                                      const char* file) {

  mvm::ThreadAllocator threadAllocator;
  char* soName = (char*)threadAllocator.Allocate(
      strlen(name) + strlen(DYLD_EXTENSION));
  const char* ptr = strrchr(name, '/');
  sprintf(soName, "%s%s", ptr ? ptr + 1 : name, DYLD_EXTENSION);
  void* handle = dlopen(soName, RTLD_LAZY | RTLD_LOCAL);
  if (handle) {
    Class* cl = (Class*)dlsym(handle, file);
    if (cl) {
      static_init_t init = (static_init_t)(word_t)cl->classLoader;
      assert(init && "Loaded the wrong library");
      init(this);
      insertAllMethodsInVM(vm);
    }
  }
}


void JnjvmClassLoader::loadLibFromFile(Jnjvm* vm, const char* name) {
  mvm::ThreadAllocator threadAllocator;
  assert(classes->map.size() == 0);
  char* soName = (char*)threadAllocator.Allocate(
      strlen(name) + strlen(DYLD_EXTENSION));
  sprintf(soName, "%s%s", name, DYLD_EXTENSION);
  void* handle = dlopen(soName, RTLD_LAZY | RTLD_LOCAL);
  if (handle) {
    Class* cl = (Class*)dlsym(handle, name);
    if (cl) {
      static_init_t init = (static_init_t)(word_t)cl->classLoader;
      init(this);
      insertAllMethodsInVM(vm);
    }
  }
}


Class* JnjvmClassLoader::loadClassFromSelf(Jnjvm* vm, const char* name) {
  assert(classes->map.size() == 0);
  Class* cl = (Class*)dlsym(SELF_HANDLE, name);
  if (cl) {
    static_init_t init = (static_init_t)(word_t)cl->classLoader;
    init(this);
    insertAllMethodsInVM(vm);
  }
  return cl;
}


// Extern "C" functions called by the vmjc static intializer.
extern "C" void vmjcAddPreCompiledClass(JnjvmClassLoader* JCL,
                                        CommonClass* cl) {
  cl->classLoader = JCL;
}


extern "C" void vmjcGetClassArray(JnjvmClassLoader* JCL, ClassArray** ptr,
                                  const UTF8* name) {
  *ptr = JCL->constructArray(name);
}

extern "C" word_t vmjcNativeLoader(JavaMethod* meth) {
  bool j3 = false;
  const UTF8* jniConsClName = meth->classDef->name;
  const UTF8* jniConsName = meth->name;
  const UTF8* jniConsType = meth->type;
  sint32 clen = jniConsClName->size;
  sint32 mnlen = jniConsName->size;
  sint32 mtlen = jniConsType->size;

  mvm::ThreadAllocator threadAllocator;
  char* buf = (char*)threadAllocator.Allocate(
      3 + JNI_NAME_PRE_LEN + 1 + ((mnlen + clen + mtlen) << 3));
  word_t res = meth->classDef->classLoader->nativeLookup(meth, j3, buf);
  assert(res && "Could not find required native method");
  return res;
}

extern "C" void staticCallback() {
  fprintf(stderr, "Implement me");
  abort();
}


bool Precompiled::Init(JnjvmBootstrapLoader* loader) {
  Class* javaLangObject = (Class*)dlsym(SELF_HANDLE, "java_lang_Object");
  void* nativeHandle = mvm::System::GetSelfHandle();
  if (javaLangObject == NULL) {
    void* handle = dlopen("libvmjc"DYLD_EXTENSION, RTLD_LAZY | RTLD_GLOBAL);
    if (handle != NULL) {
      nativeHandle = handle;
      javaLangObject = (Class*)dlsym(nativeHandle, "java_lang_Object");
    }
  }

  if (javaLangObject == NULL) {
    return false;
  }

  ClassArray::SuperArray = javaLangObject;
    
  // Get the native classes.
  Classpath* upcalls = loader->upcalls;
  upcalls->OfVoid = (ClassPrimitive*)dlsym(nativeHandle, "void");
  upcalls->OfBool = (ClassPrimitive*)dlsym(nativeHandle, "boolean");
  upcalls->OfByte = (ClassPrimitive*)dlsym(nativeHandle, "byte");
  upcalls->OfChar = (ClassPrimitive*)dlsym(nativeHandle, "char");
  upcalls->OfShort = (ClassPrimitive*)dlsym(nativeHandle, "short");
  upcalls->OfInt = (ClassPrimitive*)dlsym(nativeHandle, "int");
  upcalls->OfFloat = (ClassPrimitive*)dlsym(nativeHandle, "float");
  upcalls->OfLong = (ClassPrimitive*)dlsym(nativeHandle, "long");
  upcalls->OfDouble = (ClassPrimitive*)dlsym(nativeHandle, "double");
  upcalls->OfVoid->classLoader = loader;
  upcalls->OfBool->classLoader = loader;
  upcalls->OfByte->classLoader = loader;
  upcalls->OfChar->classLoader = loader;
  upcalls->OfShort->classLoader = loader;
  upcalls->OfInt->classLoader = loader;
  upcalls->OfFloat->classLoader = loader;
  upcalls->OfLong->classLoader = loader;
  upcalls->OfDouble->classLoader = loader;

  mvm::MvmDenseSet<mvm::UTF8MapKey, const UTF8*>* precompiledUTF8Map =
    reinterpret_cast<mvm::MvmDenseSet<mvm::UTF8MapKey, const UTF8*>*>(dlsym(nativeHandle, "UTF8Map"));
  loader->hashUTF8 = new (loader->allocator, "UTF8Map") UTF8Map(loader->allocator, precompiledUTF8Map);
  
  mvm::MvmDenseMap<const UTF8*, CommonClass*>* precompiledClassMap =
    reinterpret_cast<mvm::MvmDenseMap<const UTF8*, CommonClass*>*>(dlsym(nativeHandle, "ClassMap"));
  loader->classes = new (loader->allocator, "ClassMap") ClassMap(precompiledClassMap);

  for (ClassMap::iterator i = loader->getClasses()->map.begin(),
       e = loader->getClasses()->map.end(); i != e; i++) {
    i->second->classLoader = loader;
  }
 
  // Get the base object arrays after the init, because init puts arrays
  // in the class loader map.
  upcalls->ArrayOfString = 
    loader->constructArray(loader->asciizConstructUTF8("[Ljava/lang/String;"));

  upcalls->ArrayOfObject = 
    loader->constructArray(loader->asciizConstructUTF8("[Ljava/lang/Object;"));
  
  ClassArray::InterfacesArray = upcalls->ArrayOfObject->interfaces;

  return true;
}

}
