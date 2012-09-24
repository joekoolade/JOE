//===-- JnjvmClassLoader.cpp - Jnjvm representation of a class loader ------===//
//
//                          The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <climits>
#include <cstdlib>

// for strrchr
#include <cstring>

// for dlopen and dlsym
#include <dlfcn.h> 

// for stat, S_IFMT and S_IFDIR
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "debug.h"
#include "mvm/Allocator.h"

#include "Classpath.h"
#include "ClasspathReflect.h"
#include "JavaClass.h"
#include "JavaCompiler.h"
#include "JavaConstantPool.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "JnjvmClassLoader.h"
#include "LockedMap.h"
#include "Reader.h"
#include "Zip.h"


using namespace j3;

typedef void (*static_init_t)(JnjvmClassLoader*);

const UTF8* JavaCompiler::InlinePragma = 0;
const UTF8* JavaCompiler::NoInlinePragma = 0;


JnjvmBootstrapLoader::JnjvmBootstrapLoader(mvm::BumpPtrAllocator& Alloc,
                                           JavaCompiler* Comp, 
                                           bool dlLoad) : 
    JnjvmClassLoader(Alloc) {
  
	TheCompiler = Comp;
  
  javaTypes = new(allocator, "TypeMap") TypeMap(); 
  javaSignatures = new(allocator, "SignMap") SignMap();
  strings = new(allocator, "StringList") StringList();
  
  bootClasspathEnv = ClasslibBootEnv;
  libClasspathEnv = ClasslibLibEnv;
   
  upcalls = new(allocator, "Classpath") Classpath();
  bootstrapLoader = this;
   
  // Try to find if we have a pre-compiled rt.jar
  bool bootLoaded = false;
  if (dlLoad) {
    bootLoaded = Precompiled::Init(this);
  }
 
  if (!bootLoaded) {
    classes = new(allocator, "ClassMap") ClassMap();
    hashUTF8 = new(allocator, "UTF8Map") UTF8Map(allocator);
    // Analyze the boot classpath, we know bootstrap classes are not in the
    // executable.
    analyseClasspathEnv(bootClasspathEnv);

    // Allocate the interfaces array for array classes, so that VT construction
    // can use it right away.
    ClassArray::InterfacesArray =
      (Class**)allocator.Allocate(2 * sizeof(UserClass*), "Interface array");

    // Create the primitive classes.
    upcalls->OfChar = UPCALL_PRIMITIVE_CLASS(this, "char", 1);
    upcalls->OfBool = UPCALL_PRIMITIVE_CLASS(this, "boolean", 0);
    upcalls->OfShort = UPCALL_PRIMITIVE_CLASS(this, "short", 1);
    upcalls->OfInt = UPCALL_PRIMITIVE_CLASS(this, "int", 2);
    upcalls->OfLong = UPCALL_PRIMITIVE_CLASS(this, "long", 3);
    upcalls->OfFloat = UPCALL_PRIMITIVE_CLASS(this, "float", 2);
    upcalls->OfDouble = UPCALL_PRIMITIVE_CLASS(this, "double", 3);
    upcalls->OfVoid = UPCALL_PRIMITIVE_CLASS(this, "void", 0);
    upcalls->OfByte = UPCALL_PRIMITIVE_CLASS(this, "byte", 0);
  }
  
  // Create the primitive arrays.
  upcalls->ArrayOfChar = constructArray(asciizConstructUTF8("[C"),
                                        upcalls->OfChar);

  upcalls->ArrayOfByte = constructArray(asciizConstructUTF8("[B"),
                                        upcalls->OfByte);
  
  upcalls->ArrayOfInt = constructArray(asciizConstructUTF8("[I"),
                                       upcalls->OfInt);
  
  upcalls->ArrayOfBool = constructArray(asciizConstructUTF8("[Z"),
                                        upcalls->OfBool);
  
  upcalls->ArrayOfLong = constructArray(asciizConstructUTF8("[J"),
                                        upcalls->OfLong);
  
  upcalls->ArrayOfFloat = constructArray(asciizConstructUTF8("[F"),
                                         upcalls->OfFloat);
  
  upcalls->ArrayOfDouble = constructArray(asciizConstructUTF8("[D"),
                                          upcalls->OfDouble);
  
  upcalls->ArrayOfShort = constructArray(asciizConstructUTF8("[S"),
                                         upcalls->OfShort);
  
  // Fill the maps.
  primitiveMap[I_VOID] = upcalls->OfVoid;
  primitiveMap[I_BOOL] = upcalls->OfBool;
  primitiveMap[I_BYTE] = upcalls->OfByte;
  primitiveMap[I_CHAR] = upcalls->OfChar;
  primitiveMap[I_SHORT] = upcalls->OfShort;
  primitiveMap[I_INT] = upcalls->OfInt;
  primitiveMap[I_FLOAT] = upcalls->OfFloat;
  primitiveMap[I_LONG] = upcalls->OfLong;
  primitiveMap[I_DOUBLE] = upcalls->OfDouble;

  arrayTable[JavaArray::T_BOOLEAN - 4] = upcalls->ArrayOfBool;
  arrayTable[JavaArray::T_BYTE - 4] = upcalls->ArrayOfByte;
  arrayTable[JavaArray::T_CHAR - 4] = upcalls->ArrayOfChar;
  arrayTable[JavaArray::T_SHORT - 4] = upcalls->ArrayOfShort;
  arrayTable[JavaArray::T_INT - 4] = upcalls->ArrayOfInt;
  arrayTable[JavaArray::T_FLOAT - 4] = upcalls->ArrayOfFloat;
  arrayTable[JavaArray::T_LONG - 4] = upcalls->ArrayOfLong;
  arrayTable[JavaArray::T_DOUBLE - 4] = upcalls->ArrayOfDouble;
  
  Attribut::annotationsAttribut =
    asciizConstructUTF8("RuntimeVisibleAnnotations");
  Attribut::codeAttribut = asciizConstructUTF8("Code");
  Attribut::exceptionsAttribut = asciizConstructUTF8("Exceptions");
  Attribut::constantAttribut = asciizConstructUTF8("ConstantValue");
  Attribut::lineNumberTableAttribut = asciizConstructUTF8("LineNumberTable");
  Attribut::innerClassesAttribut = asciizConstructUTF8("InnerClasses");
  Attribut::sourceFileAttribut = asciizConstructUTF8("SourceFile");
 
  JavaCompiler::InlinePragma =
    asciizConstructUTF8("Lorg/vmmagic/pragma/Inline;");
  JavaCompiler::NoInlinePragma =
    asciizConstructUTF8("Lorg/vmmagic/pragma/NoInline;");

  initName = asciizConstructUTF8("<init>");
  initExceptionSig = asciizConstructUTF8("(Ljava/lang/String;)V");
  clinitName = asciizConstructUTF8("<clinit>");
  clinitType = asciizConstructUTF8("()V");
  runName = asciizConstructUTF8("run");
  prelib = asciizConstructUTF8("lib");
  postlib = asciizConstructUTF8(mvm::System::GetDyLibExtension());
  mathName = asciizConstructUTF8("java/lang/Math");
  VMFloatName = asciizConstructUTF8("java/lang/VMFloat");
  VMDoubleName = asciizConstructUTF8("java/lang/VMDouble");
  stackWalkerName = asciizConstructUTF8("gnu/classpath/VMStackWalker");
  NoClassDefFoundError = asciizConstructUTF8("java/lang/NoClassDefFoundError");

#define DEF_UTF8(var) \
  var = asciizConstructUTF8(#var)
  
  DEF_UTF8(abs);
  DEF_UTF8(sqrt);
  DEF_UTF8(sin);
  DEF_UTF8(cos);
  DEF_UTF8(tan);
  DEF_UTF8(asin);
  DEF_UTF8(acos);
  DEF_UTF8(atan);
  DEF_UTF8(atan2);
  DEF_UTF8(exp);
  DEF_UTF8(log);
  DEF_UTF8(pow);
  DEF_UTF8(ceil);
  DEF_UTF8(floor);
  DEF_UTF8(rint);
  DEF_UTF8(cbrt);
  DEF_UTF8(cosh);
  DEF_UTF8(expm1);
  DEF_UTF8(hypot);
  DEF_UTF8(log10);
  DEF_UTF8(log1p);
  DEF_UTF8(sinh);
  DEF_UTF8(tanh);
  DEF_UTF8(finalize);
  DEF_UTF8(floatToRawIntBits);
  DEF_UTF8(doubleToRawLongBits);
  DEF_UTF8(intBitsToFloat);
  DEF_UTF8(longBitsToDouble);

#undef DEF_UTF8 
}

JnjvmClassLoader::JnjvmClassLoader(mvm::BumpPtrAllocator& Alloc,
                                   JnjvmClassLoader& JCL, JavaObject* loader,
                                   VMClassLoader* vmdata,
                                   Jnjvm* I) : allocator(Alloc) {
  llvm_gcroot(loader, 0);
  llvm_gcroot(vmdata, 0);
  bootstrapLoader = JCL.bootstrapLoader;
  TheCompiler = bootstrapLoader->getCompiler()->Create("Applicative loader");
  
  hashUTF8 = new(allocator, "UTF8Map") UTF8Map(allocator);
  classes = new(allocator, "ClassMap") ClassMap();
  javaTypes = new(allocator, "TypeMap") TypeMap();
  javaSignatures = new(allocator, "SignMap") SignMap();
  strings = new(allocator, "StringList") StringList();

  vmdata->JCL = this;
  mvm::Collector::objectReferenceNonHeapWriteBarrier(
      (gc**)&javaLoader, (gc*)loader);
  isolate = I;

  JavaMethod* meth = bootstrapLoader->upcalls->loadInClassLoader;
  loadClassMethod = 
    JavaObject::getClass(loader)->asClass()->lookupMethodDontThrow(
        meth->name, meth->type, false, true, &loadClass);
  assert(loadClass && "Loader does not have a loadClass function");
}

void JnjvmClassLoader::setCompiler(JavaCompiler* Comp) {
  // Set the new compiler.
  TheCompiler = Comp;
}

ClassBytes* JnjvmBootstrapLoader::openName(const UTF8* utf8) {
  ClassBytes* res = reinterpret_cast<ClassBytes*>(dlsym(mvm::System::GetSelfHandle(),
      UTF8Buffer(utf8).toCompileName("_bytes")->cString()));
  if (res != NULL) return res;

  mvm::ThreadAllocator threadAllocator;

  char* asciiz = (char*)threadAllocator.Allocate(utf8->size + 1);
  for (sint32 i = 0; i < utf8->size; ++i) 
    asciiz[i] = utf8->elements[i];
  asciiz[utf8->size] = 0;
  
  uint32 alen = utf8->size;
  
  for (std::vector<const char*>::iterator i = bootClasspath.begin(),
       e = bootClasspath.end(); i != e; ++i) {
    const char* str = *i;
    unsigned int strLen = strlen(str);
    char* buf = (char*)threadAllocator.Allocate(strLen + alen + 7);

    sprintf(buf, "%s%s.class", str, asciiz);
    res = Reader::openFile(this, buf);
    if (res != NULL) return res;
  }

  for (std::vector<ZipArchive*>::iterator i = bootArchives.begin(),
       e = bootArchives.end(); i != e; ++i) {
    
    ZipArchive* archive = *i;
    char* buf = (char*)threadAllocator.Allocate(alen + 7);
    sprintf(buf, "%s.class", asciiz);
    res = Reader::openZip(this, archive, buf);
    if (res != NULL) return res;
  }

  return NULL;
}


UserClass* JnjvmBootstrapLoader::internalLoad(const UTF8* name,
                                              bool doResolve,
                                              JavaString* strName) {
  ClassBytes* bytes = NULL;
  llvm_gcroot(strName, 0);

  UserCommonClass* cl = lookupClass(name);
  
  if (!cl) {
    bytes = openName(name);
    if (bytes != NULL) {
      cl = constructClass(name, bytes);
    }
  }
  
  if (cl) {
    assert(!cl->isArray());
    if (doResolve) cl->asClass()->resolveClass();
  }

  return (UserClass*)cl;
}

UserClass* JnjvmClassLoader::internalLoad(const UTF8* name, bool doResolve,
                                          JavaString* strName) {
  JavaObject* obj = 0;
  llvm_gcroot(strName, 0);
  llvm_gcroot(obj, 0);
  
  UserCommonClass* cl = lookupClass(name);
  
  if (!cl) {
    UserClass* forCtp = loadClass;
    if (strName == NULL) {
      strName = JavaString::internalToJava(name, isolate);
    }
    obj = loadClassMethod->invokeJavaObjectVirtual(isolate, forCtp, javaLoader,
                                                   &strName);
    cl = JavaObjectClass::getClass(((JavaObjectClass*)obj));
  }
  
  if (cl && doResolve && cl->isClass()) {
    cl->asClass()->resolveClass();
  }

  return (UserClass*)cl;
}

UserClass* JnjvmClassLoader::loadName(const UTF8* name, bool doResolve,
                                      bool doThrow, JavaString* strName) {
  
  llvm_gcroot(strName, 0);

  UserClass* cl = internalLoad(name, doResolve, strName);

  if (!cl && doThrow) {
    Jnjvm* vm = JavaThread::get()->getJVM();
    if (name->equals(bootstrapLoader->NoClassDefFoundError)) {
      fprintf(stderr, "Unable to load NoClassDefFoundError");
      abort();
    }
    if (TheCompiler->isStaticCompiling()) {
      fprintf(stderr, "Could not find %s, needed for static compiling\n",
              UTF8Buffer(name).cString());
      abort();
    }
    vm->noClassDefFoundError(name);
  }

  if (cl && cl->classLoader != this) {
    classes->lock.lock();
    ClassMap::iterator End = classes->map.end();
    ClassMap::iterator I = classes->map.find(cl->name);
    if (I == End)
      classes->map.insert(std::make_pair(cl->name, cl));
    classes->lock.unlock();
  }

  return cl;
}


const UTF8* JnjvmClassLoader::lookupComponentName(const UTF8* name,
                                                  UTF8* holder,
                                                  bool& prim) {
  uint32 len = name->size;
  uint32 start = 0;
  uint32 origLen = len;
  
  while (true) {
    --len;
    if (len == 0) {
      return 0;
    } else {
      ++start;
      if (name->elements[start] != I_TAB) {
        if (name->elements[start] == I_REF) {
          uint32 size = (uint32)name->size;
          if ((size == (start + 1)) || (size == (start + 2)) ||
              (name->elements[start + 1] == I_TAB) ||
              (name->elements[origLen - 1] != I_END_REF)) {
            return 0;
          } else {
            const uint16* buf = &(name->elements[start + 1]);
            uint32 bufLen = len - 2;
            const UTF8* componentName = hashUTF8->lookupReader(buf, bufLen);
            if (!componentName && holder) {
              holder->size = len - 2;
              for (uint32 i = 0; i < len - 2; ++i) {
                holder->elements[i] = name->elements[start + 1 + i];
              }
              componentName = holder;
            }
            return componentName;
          }
        } else {
          uint16 cur = name->elements[start];
          if ((cur == I_BOOL || cur == I_BYTE ||
               cur == I_CHAR || cur == I_SHORT ||
               cur == I_INT || cur == I_FLOAT || 
               cur == I_DOUBLE || cur == I_LONG)
              && ((uint32)name->size) == start + 1) {
            prim = true;
          }
          return 0;
        }
      }
    }
  }

  return 0;
}

UserCommonClass* JnjvmClassLoader::lookupClassOrArray(const UTF8* name) {
  UserCommonClass* temp = lookupClass(name);
  if (temp) return temp;

  if (this != bootstrapLoader) {
    temp = bootstrapLoader->lookupClassOrArray(name);
    if (temp) return temp;
  }
  
  
  if (name->elements[0] == I_TAB) {
    bool prim = false;
    const UTF8* componentName = lookupComponentName(name, 0, prim);
    if (prim) return constructArray(name);
    if (componentName) {
      UserCommonClass* temp = lookupClass(componentName);
      if (temp) return constructArray(name);
    }
  }

  return 0;
}

UserCommonClass* JnjvmClassLoader::loadClassFromUserUTF8(const UTF8* name,
                                                         bool doResolve,
                                                         bool doThrow,
                                                         JavaString* strName) {
  llvm_gcroot(strName, 0);
  
  if (name->size == 0) {
    return 0;
  } else if (name->elements[0] == I_TAB) {
    mvm::ThreadAllocator threadAllocator;
    bool prim = false;
    UTF8* holder = (UTF8*)threadAllocator.Allocate(
        sizeof(UTF8) + name->size * sizeof(uint16));
    if (!holder) return 0;
    
    const UTF8* componentName = lookupComponentName(name, holder, prim);
    if (prim) return constructArray(name);
    if (componentName) {
      UserCommonClass* temp = loadName(componentName, doResolve, doThrow, NULL);
      if (temp) return constructArray(name);
    }
  } else {
    return loadName(name, doResolve, doThrow, strName);
  }

  return NULL;
}

UserCommonClass* JnjvmClassLoader::loadClassFromAsciiz(const char* asciiz,
                                                       bool doResolve,
                                                       bool doThrow) {
  const UTF8* name = hashUTF8->lookupAsciiz(asciiz);
  mvm::ThreadAllocator threadAllocator;
  UserCommonClass* result = NULL;
  if (!name) name = bootstrapLoader->hashUTF8->lookupAsciiz(asciiz);
  if (!name) {
    uint32 size = strlen(asciiz);
    UTF8* temp = (UTF8*)threadAllocator.Allocate(
        sizeof(UTF8) + size * sizeof(uint16));
    temp->size = size;

    for (uint32 i = 0; i < size; ++i) {
      temp->elements[i] = asciiz[i];
    }
    name = temp;
  }
  
  result = lookupClass(name);
  if ((result == NULL) && (this != bootstrapLoader)) {
    result = bootstrapLoader->lookupClassOrArray(name);
    if (result != NULL) {
      if (result->isClass() && doResolve) {
        result->asClass()->resolveClass();
      }
      return result;
    }
  }
 
  return loadClassFromUserUTF8(name, doResolve, doThrow, NULL);
}


UserCommonClass* 
JnjvmClassLoader::loadClassFromJavaString(JavaString* str, bool doResolve,
                                          bool doThrow) {
  
  llvm_gcroot(str, 0);
  mvm::ThreadAllocator allocator; 
  UTF8* name = (UTF8*)allocator.Allocate(sizeof(UTF8) + str->count * sizeof(uint16));
 
  name->size = str->count;
  if (ArrayUInt16::getElement(JavaString::getValue(str), str->offset) != I_TAB) {
    for (sint32 i = 0; i < str->count; ++i) {
      uint16 cur = ArrayUInt16::getElement(JavaString::getValue(str), str->offset + i);
      if (cur == '.') name->elements[i] = '/';
      else if (cur == '/') {
        return 0;
      }
      else name->elements[i] = cur;
    }
  } else {
    for (sint32 i = 0; i < str->count; ++i) {
      uint16 cur = ArrayUInt16::getElement(JavaString::getValue(str), str->offset + i);
      if (cur == '.') {
        name->elements[i] = '/';
      } else if (cur == '/') {
        return 0;
      } else {
        name->elements[i] = cur;
      }
    }
  }
    
  UserCommonClass* cls = loadClassFromUserUTF8(name, doResolve, doThrow, str);
  return cls;
}

UserCommonClass* JnjvmClassLoader::lookupClassFromJavaString(JavaString* str) {
  
  const ArrayUInt16* value = NULL;
  llvm_gcroot(str, 0);
  llvm_gcroot(value, 0);
  value = JavaString::getValue(str);
  mvm::ThreadAllocator allocator; 
  
  UTF8* name = (UTF8*)allocator.Allocate(sizeof(UTF8) + str->count * sizeof(uint16));
  name->size = str->count;
  for (sint32 i = 0; i < str->count; ++i) {
    uint16 cur = ArrayUInt16::getElement(value, str->offset + i);
    if (cur == '.') name->elements[i] = '/';
    else name->elements[i] = cur;
  }
  UserCommonClass* cls = lookupClass(name);
  return cls;
}

UserCommonClass* JnjvmClassLoader::lookupClass(const UTF8* utf8) {
  classes->lock.lock();
  UserCommonClass* cl = classes->map.lookup(utf8);
  classes->lock.unlock();
  return cl;
}

UserCommonClass* JnjvmClassLoader::loadBaseClass(const UTF8* name,
                                                 uint32 start, uint32 len) {
  
  if (name->elements[start] == I_TAB) {
    UserCommonClass* baseClass = loadBaseClass(name, start + 1, len - 1);
    JnjvmClassLoader* loader = baseClass->classLoader;
    const UTF8* arrayName = name->extract(loader->hashUTF8, start, start + len);
    return loader->constructArray(arrayName, baseClass);
  } else if (name->elements[start] == I_REF) {
    const UTF8* componentName = name->extract(hashUTF8,
                                              start + 1, start + len - 1);
    UserCommonClass* cl = loadName(componentName, false, true, NULL);
    return cl;
  } else {
    Classpath* upcalls = bootstrapLoader->upcalls;
    UserClassPrimitive* prim = 
      UserClassPrimitive::byteIdToPrimitive(name->elements[start], upcalls);
    assert(prim && "No primitive found");
    return prim;
  }
}


UserClassArray* JnjvmClassLoader::constructArray(const UTF8* name) {
  ClassArray* res = (ClassArray*)lookupClass(name);
  if (res) return res;

  UserCommonClass* cl = loadBaseClass(name, 1, name->size - 1);
  assert(cl && "no base class for an array");
  JnjvmClassLoader* ld = cl->classLoader;
  res = ld->constructArray(name, cl);
  
  if (res && res->classLoader != this) {
    classes->lock.lock();
    ClassMap::iterator End = classes->map.end();
    ClassMap::iterator I = classes->map.find(res->name);
    if (I == End)
      classes->map.insert(std::make_pair(res->name, res));
    classes->lock.unlock();
  }
  return res;
}

UserClass* JnjvmClassLoader::constructClass(const UTF8* name,
                                            ClassBytes* bytes) {
  JavaObject* excp = NULL;
  llvm_gcroot(excp, 0);
  UserClass* res = NULL;
  lock.lock();
  classes->lock.lock();
  res = (UserClass*) classes->map.lookup(name);
  classes->lock.unlock();
  if (res == NULL) {
    TRY {
      const UTF8* internalName = readerConstructUTF8(name->elements, name->size);
      res = new(allocator, "Class") UserClass(this, internalName, bytes);
      res->readClass();
      res->makeVT();
      getCompiler()->resolveVirtualClass(res);
      getCompiler()->resolveStaticClass(res);
      classes->lock.lock();
      assert(res->getDelegatee() == NULL);
      assert(res->getStaticInstance() == NULL);
      assert(classes->map.lookup(internalName) == NULL);
      classes->map[internalName] = res;
      classes->lock.unlock();
    } CATCH {
      excp = JavaThread::get()->pendingException;
      JavaThread::get()->clearException();    
    } END_CATCH;
  }
  lock.unlock();
  if (excp != NULL) {
    JavaThread::get()->throwException(excp);
  }

  if (res->super == NULL) {
    // java.lang.Object just got created, initialise VTs of arrays.
    ClassArray::initialiseVT(res);
  }
  return res;
}

UserClassArray* JnjvmClassLoader::constructArray(const UTF8* name,
                                                 UserCommonClass* baseClass) {
  assert(baseClass && "constructing an array class without a base class");
  assert(baseClass->classLoader == this && 
         "constructing an array with wrong loader");
  UserClassArray* res = 0;
  classes->lock.lock();
  res = (UserClassArray*) classes->map.lookup(name);
  if (res == NULL) {
    const UTF8* internalName = readerConstructUTF8(name->elements, name->size);
    res = new(allocator, "Array class") UserClassArray(this, internalName,
                                                       baseClass);
    classes->map.insert(std::make_pair(internalName, res));
  }
  classes->lock.unlock();
  return res;
}

Typedef* JnjvmClassLoader::internalConstructType(const UTF8* name) {
  short int cur = name->elements[0];
  Typedef* res = 0;
  switch (cur) {
    case I_TAB :
      res = new(allocator, "ArrayTypedef") ArrayTypedef(name);
      break;
    case I_REF :
      res = new(allocator, "ObjectTypedef") ObjectTypedef(name, hashUTF8);
      break;
    default :
      UserClassPrimitive* cl = 
        bootstrapLoader->getPrimitiveClass((char)name->elements[0]);
      assert(cl && "No primitive");
      bool unsign = (cl == bootstrapLoader->upcalls->OfChar || 
                     cl == bootstrapLoader->upcalls->OfBool);
      res = new(allocator, "PrimitiveTypedef") PrimitiveTypedef(name, cl,
                                                                unsign, cur);
  }
  return res;
}


Typedef* JnjvmClassLoader::constructType(const UTF8* name) {
  javaTypes->lock.lock();
  Typedef* res = javaTypes->map.lookup(name);
  if (res == 0) {
    res = internalConstructType(name);
    javaTypes->map[name] = res;
  }
  javaTypes->lock.unlock();
  return res;
}

static void typeError(const UTF8* name, short int l) {
  if (l != 0) {
    fprintf(stderr, "wrong type %d in %s", l, UTF8Buffer(name).cString());
  } else {
    fprintf(stderr, "wrong type %s", UTF8Buffer(name).cString());
  }
  abort();
}


static bool analyseIntern(const UTF8* name, uint32 pos, uint32 meth,
                          uint32& ret) {
  short int cur = name->elements[pos];
  switch (cur) {
    case I_PARD :
      ret = pos + 1;
      return true;
    case I_BOOL :
      ret = pos + 1;
      return false;
    case I_BYTE :
      ret = pos + 1;
      return false;
    case I_CHAR :
      ret = pos + 1;
      return false;
    case I_SHORT :
      ret = pos + 1;
      return false;
    case I_INT :
      ret = pos + 1;
      return false;
    case I_FLOAT :
      ret = pos + 1;
      return false;
    case I_DOUBLE :
      ret = pos + 1;
      return false;
    case I_LONG :
      ret = pos + 1;
      return false;
    case I_VOID :
      ret = pos + 1;
      return false;
    case I_TAB :
      if (meth == 1) {
        pos++;
      } else {
        while (name->elements[++pos] == I_TAB) {}
        analyseIntern(name, pos, 1, pos);
      }
      ret = pos;
      return false;
    case I_REF :
      if (meth != 2) {
        while (name->elements[++pos] != I_END_REF) {}
      }
      ret = pos + 1;
      return false;
    default :
      typeError(name, cur);
  }
  return false;
}

Signdef* JnjvmClassLoader::constructSign(const UTF8* name) {
  javaSignatures->lock.lock();
  Signdef* res = javaSignatures->map.lookup(name);
  if (res == 0) {
    std::vector<Typedef*> buf;
    uint32 len = (uint32)name->size;
    uint32 pos = 1;
    uint32 pred = 0;

    while (pos < len) {
      pred = pos;
      bool end = analyseIntern(name, pos, 0, pos);
      if (end) break;
      else {
        buf.push_back(constructType(name->extract(hashUTF8, pred, pos)));
      } 
    }
  
    if (pos == len) {
      typeError(name, 0);
    }
  
    analyseIntern(name, pos, 0, pred);

    if (pred != len) {
      typeError(name, 0);
    }
    
    Typedef* ret = constructType(name->extract(hashUTF8, pos, pred));
    
    res = new(allocator, buf.size()) Signdef(name, this, buf, ret);

    javaSignatures->map[name] = res;
  }
  javaSignatures->lock.unlock();
  return res;
}


JnjvmClassLoader*
JnjvmClassLoader::getJnjvmLoaderFromJavaObject(JavaObject* loader, Jnjvm* vm) {
  
  VMClassLoader* vmdata = 0;
  
  llvm_gcroot(loader, 0);
  llvm_gcroot(vmdata, 0);
  
  if (loader == NULL) return vm->bootstrapLoader;
 
  JnjvmClassLoader* JCL = 0;
  Classpath* upcalls = vm->bootstrapLoader->upcalls;
  vmdata = 
    (VMClassLoader*)(upcalls->vmdataClassLoader->getInstanceObjectField(loader));
  
  // If the vmdata field isn't set yet, or it's set to something other than
  // a VMClassLoader (which happens in the OpenJDK port), then initialize
  // the field now.
  if (vmdata == NULL || !VMClassLoader::isVMClassLoader(vmdata)) {
    JavaObject::acquire(loader);
    vmdata = 
      (VMClassLoader*)(upcalls->vmdataClassLoader->getInstanceObjectField(loader));
    if (!vmdata || !VMClassLoader::isVMClassLoader(vmdata)) {
      vmdata = VMClassLoader::allocate();
      mvm::BumpPtrAllocator* A = new mvm::BumpPtrAllocator();
      JCL = new(*A, "Class loader") JnjvmClassLoader(*A, *vm->bootstrapLoader,
                                                     loader, vmdata, vm);
      upcalls->vmdataClassLoader->setInstanceObjectField(loader, (JavaObject*)vmdata);
    }
    JavaObject::release(loader);
  } else {
    JCL = vmdata->getClassLoader();
    assert(JCL->javaLoader == loader);
  }

  return JCL;
}

const UTF8* JnjvmClassLoader::asciizConstructUTF8(const char* asciiz) {
  return hashUTF8->lookupOrCreateAsciiz(asciiz);
}

const UTF8* JnjvmClassLoader::readerConstructUTF8(const uint16* buf,
                                                  uint32 size) {
  return hashUTF8->lookupOrCreateReader(buf, size);
}

JnjvmClassLoader::~JnjvmClassLoader() {

  if (isolate) {
    isolate->removeFrameInfos(TheCompiler);
  }

  if (classes) {
    classes->~ClassMap();
    allocator.Deallocate(classes);
  }

  if (hashUTF8) {
    hashUTF8->~UTF8Map();
    allocator.Deallocate(hashUTF8);
  }

  if (javaTypes) {
    javaTypes->~TypeMap();
    allocator.Deallocate(javaTypes);
  }

  if (javaSignatures) {
    javaSignatures->~SignMap();
    allocator.Deallocate(javaSignatures);
  }

  for (std::vector<void*>::iterator i = nativeLibs.begin(); 
       i < nativeLibs.end(); ++i) {
    dlclose(*i);
  }

  delete TheCompiler;

  // Don't delete the allocator. The caller of this method must
  // delete it after the current object is deleted.
}


JnjvmBootstrapLoader::~JnjvmBootstrapLoader() {
}

JavaString** JnjvmClassLoader::UTF8ToStr(const UTF8* val) {
  JavaString* res = NULL;
  llvm_gcroot(res, 0);
  res = isolate->internalUTF8ToStr(val);
  return strings->addString(this, res);
}

JavaString** JnjvmBootstrapLoader::UTF8ToStr(const UTF8* val) {
  JavaString* res = NULL;
  llvm_gcroot(res, 0);
  Jnjvm* vm = JavaThread::get()->getJVM();
  res = vm->internalUTF8ToStr(val);
  return strings->addString(this, res);
}

void JnjvmBootstrapLoader::analyseClasspathEnv(const char* str) {
  ClassBytes* bytes = NULL;
  mvm::ThreadAllocator threadAllocator;
  if (str != 0) {
    unsigned int len = strlen(str);
    char* buf = (char*)threadAllocator.Allocate((len + 1) * sizeof(char));
    const char* cur = str;
    int top = 0;
    char c = 1;
    while (c != 0) {
      while (((c = cur[top]) != 0) && c != Jnjvm::envSeparator[0]) {
        top++;
      }
      if (top != 0) {
        memcpy(buf, cur, top);
        buf[top] = 0;
        char* rp = (char*)threadAllocator.Allocate(PATH_MAX);
        memset(rp, 0, PATH_MAX);
        rp = realpath(buf, rp);
        if (rp && rp[PATH_MAX - 1] == 0 && strlen(rp) != 0) {
          struct stat st;
          stat(rp, &st);
          if ((st.st_mode & S_IFMT) == S_IFDIR) {
            unsigned int len = strlen(rp);
            char* temp = (char*)allocator.Allocate(len + 2, "Boot classpath");
            memcpy(temp, rp, len);
            temp[len] = Jnjvm::dirSeparator[0];
            temp[len + 1] = 0;
            bootClasspath.push_back(temp);
          } else {
            bytes = Reader::openFile(this, rp);
            if (bytes) {
              ZipArchive *archive = new(allocator, "ZipArchive")
                ZipArchive(bytes, allocator);
              if (archive) {
                bootArchives.push_back(archive);
              }
            }
          }
        } 
      }
      cur = cur + top + 1;
      top = 0;
    }
  }
}

// constructArrayName can allocate the UTF8 directly in the classloader
// memory because it is called by safe places, ie only valid names are
// created.
const UTF8* JnjvmClassLoader::constructArrayName(uint32 steps,
                                                 const UTF8* className) {
  uint32 len = className->size;
  uint32 pos = steps;
  bool isTab = (className->elements[0] == I_TAB ? true : false);
  uint32 n = steps + len + (isTab ? 0 : 2);
  mvm::ThreadAllocator allocator;
  uint16* buf = (uint16*)allocator.Allocate(n * sizeof(uint16));
    
  for (uint32 i = 0; i < steps; i++) {
    buf[i] = I_TAB;
  }

  if (!isTab) {
    ++pos;
    buf[steps] = I_REF;
  }

  for (uint32 i = 0; i < len; i++) {
    buf[pos + i] = className->elements[i];
  }

  if (!isTab) {
    buf[n - 1] = I_END_REF;
  }

  const UTF8* res = readerConstructUTF8(buf, n);
  return res;
}

word_t JnjvmClassLoader::loadInLib(const char* buf, bool& j3) {
  word_t res = (word_t)TheCompiler->loadMethod(mvm::System::GetSelfHandle(), buf);
  
  if (!res) {
    for (std::vector<void*>::iterator i = nativeLibs.begin(),
              e = nativeLibs.end(); i!= e; ++i) {
      res = (word_t)TheCompiler->loadMethod((*i), buf);
      if (res) break;
    }
  } else {
    j3 = true;
  }
  
  if (!res && this != bootstrapLoader)
    res = bootstrapLoader->loadInLib(buf, j3);

  return (word_t)res;
}

void* JnjvmClassLoader::loadLib(const char* buf) {
  void* handle = dlopen(buf, RTLD_LAZY | RTLD_LOCAL);
  if (handle) nativeLibs.push_back(handle);
  return handle;
}

word_t JnjvmClassLoader::loadInLib(const char* name, void* handle) {
  return (word_t)TheCompiler->loadMethod(handle, name);
}

word_t JnjvmClassLoader::nativeLookup(JavaMethod* meth, bool& j3,
                                        char* buf) {

  // Is this method defined via registerNatives()?
  // If so, use that definition.
  word_t res = getRegisteredNative(meth);
  if (res != 0) return res;

  // Otherwise, try to resolve the method with a symbol lookup
  // First as the base method
  meth->jniConsFromMeth(buf);
  res = loadInLib(buf, j3);
  if (!res) {
    // Failing that, try the overloaded symbol name
    meth->jniConsFromMethOverloaded(buf);
    res = loadInLib(buf, j3);
  }
  return res;
}


JavaString** StringList::addString(JnjvmClassLoader* JCL, JavaString* obj) {
  llvm_gcroot(obj, 0);
  if (length == MAXIMUM_STRINGS) {
    StringList* next = new(JCL->allocator, "StringList") StringList();
    next->prev = this;
    JCL->strings = next;
    return next->addString(JCL, obj);
  } else {
    JCL->lock.lock();
    mvm::Collector::objectReferenceNonHeapWriteBarrier(
        (gc**)&(strings[length]), (gc*)obj);
    JavaString** res = &strings[length++];
    JCL->lock.unlock();
    return res;
  }
}

void JnjvmClassLoader::registerNative(JavaMethod * meth, word_t fnPtr) {
  nativesLock.lock();
  // Don't support multiple levels of registerNatives
  assert(registeredNatives.find(meth) == registeredNatives.end());

  registeredNatives[meth] = fnPtr;
  nativesLock.unlock();
}

word_t JnjvmClassLoader::getRegisteredNative(const JavaMethod * meth) {
  nativesLock.lock();
  word_t res = registeredNatives[meth];
  nativesLock.unlock();
  return res;
}
