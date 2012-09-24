//===--- JavaConstantPool.cpp - Java constant pool definition ---------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#define JNJVM_LOAD 0

#include <cstdio>
#include <cstdlib>

#include "debug.h"

#include "JavaAccess.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaCompiler.h"
#include "JavaConstantPool.h"
#include "Jnjvm.h"
#include "JavaThread.h"
#include "JavaTypes.h"
#include "LockedMap.h"
#include "Reader.h"
 
using namespace j3;

const uint32 JavaConstantPool::ConstantUTF8 = 1;
const uint32 JavaConstantPool::ConstantInteger = 3;
const uint32 JavaConstantPool::ConstantFloat = 4;
const uint32 JavaConstantPool::ConstantLong = 5;
const uint32 JavaConstantPool::ConstantDouble = 6;
const uint32 JavaConstantPool::ConstantClass = 7;
const uint32 JavaConstantPool::ConstantString = 8;
const uint32 JavaConstantPool::ConstantFieldref = 9;
const uint32 JavaConstantPool::ConstantMethodref = 10;
const uint32 JavaConstantPool::ConstantInterfaceMethodref = 11;
const uint32 JavaConstantPool::ConstantNameAndType = 12;


static uint32 unimplemented(JavaConstantPool* ctp, Reader& reader,
                            uint32 index) {
  fprintf(stderr, "Unimplemented in constant pool: %d\n", index);
  abort();
  return 1;
}


uint32 JavaConstantPool::CtpReaderClass(JavaConstantPool* ctp, Reader& reader,
                                   uint32 index) {
  uint16 entry = reader.readU2();
  ctp->ctpDef[index] = entry;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <class>\t\tutf8 is at %d\n", e,
              entry);
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderInteger(JavaConstantPool* ctp, Reader& reader,
                                     uint32 index) {
  uint32 val = reader.readU4();
  ctp->ctpDef[index] = val;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <class>\tinteger: %d\n", e,
              val);
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderFloat(JavaConstantPool* ctp, Reader& reader,
                                   uint32 index) { 
  uint32 val = reader.readU4();
  ctp->ctpDef[index] = val;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <class>\tfloat: %d\n", e,
              val);
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderUTF8(JavaConstantPool* ctp, Reader& reader,
                                  uint32 index) { 
  ctp->ctpDef[index] = reader.cursor;
  uint16 len = reader.readU2();
  reader.cursor += len;
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderNameAndType(JavaConstantPool* ctp, Reader& reader,
                                         uint32 index) {
  uint32 entry = reader.readU4();
  ctp->ctpDef[index] = entry;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, 
              "; [%5d] <name/type>\tname is at %d, type is at %d\n", index,
              (entry >> 16), (entry & 0xffff));
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderFieldref(JavaConstantPool* ctp,
                                           Reader& reader, uint32 index) {

  uint32 entry = reader.readU4();
  ctp->ctpDef[index] = entry;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, 
              "; [%5d] <fieldref>\tclass is at %d, name/type is at %d\n", index,
              (entry >> 16), (entry & 0xffff));
  return 1;
}

uint32 JavaConstantPool::CtpReaderString(JavaConstantPool* ctp, Reader& reader,
                                         uint32 index) {
  uint16 entry = reader.readU2();
  ctp->ctpDef[index] = entry;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <string>\tutf8 is at %d\n",
              index, entry);
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderMethodref(JavaConstantPool* ctp,
                                            Reader& reader,
                                            uint32 index) {
  uint32 entry = reader.readU4();
  ctp->ctpDef[index] = entry;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, 
              "; [%5d] <methodref>\tclass is at %d, name/type is at %d\n",
              index, (entry >> 16), (entry & 0xffff));
  return 1;
}

uint32 JavaConstantPool::CtpReaderInterfaceMethodref(JavaConstantPool* ctp,
                                                     Reader& reader,
                                                     uint32 index) {
  uint32 entry = reader.readU4();
  ctp->ctpDef[index] = entry;
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, 
        "; [%5d] <Interface xmethodref>\tclass is at %d, name/type is at %d\n",
        index, (entry >> 16), (entry & 0xffff));
  return 1;
}
  
uint32 JavaConstantPool::CtpReaderLong(JavaConstantPool* ctp, Reader& reader,
                                       uint32 index) {
  ctp->ctpDef[index + 1] = reader.readU4();
  ctp->ctpDef[index] = reader.readU4();
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <long>%d %d\n", index,
              ctpDef[e], ctpDef[e + 1]);
  return 2;
}

uint32 JavaConstantPool::CtpReaderDouble(JavaConstantPool* ctp, Reader& reader,
                                         uint32 index) {
  ctp->ctpDef[index + 1] = reader.readU4();
  ctp->ctpDef[index] = reader.readU4();
  PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <double>%d %d\n", index,
              ctp->ctpDef[index], ctp->ctpDef[index + 1]);
  return 2;
}


void*
JavaConstantPool::operator new(size_t sz, mvm::BumpPtrAllocator& allocator,
                               uint32 ctpSize) {
  uint32 size = sz + ctpSize * (sizeof(void*) + sizeof(sint32) + sizeof(uint8));
  return allocator.Allocate(size, "Constant pool");
}

JavaConstantPool::JavaConstantPool(Class* cl, Reader& reader, uint32 size) {
  ctpSize = size;
  classDef = cl;
  
  ctpType  = (uint8*)((uint64)this + sizeof(JavaConstantPool));
  ctpDef   = (sint32*)((uint64)ctpType + ctpSize * sizeof(uint8));
  ctpRes   = (void**)((uint64)ctpDef + ctpSize * sizeof(sint32));

  memset(ctpType, 0, 
         ctpSize * (sizeof(uint8) + sizeof(sint32) + sizeof(void*)));

  uint32 cur = 1;
  while (cur < ctpSize) {
    uint8 curType = reader.readU1();
    ctpType[cur] = curType;
    cur += ((funcsReader[curType])(this, reader, cur));
  }
}

const UTF8* JavaConstantPool::UTF8At(uint32 entry) {
  if (!((entry > 0) && (entry < ctpSize) && typeAt(entry) == ConstantUTF8)) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  
  if (!ctpRes[entry]) {
    mvm::ThreadAllocator allocator;
    Reader reader(classDef->bytes, ctpDef[entry]);
    uint32 len = reader.readU2();
    uint16* buf = (uint16*)allocator.Allocate(len * sizeof(uint16));
    uint32 n = 0;
    uint32 i = 0;
  
    while (i < len) {
      uint32 cur = reader.readU1();
      if (cur & 0x80) {
        uint32 y = reader.readU1();
        if (cur & 0x20) {
          uint32 z = reader.readU1();
          cur = ((cur & 0x0F) << 12) +
                ((y & 0x3F) << 6) +
                (z & 0x3F);
          i += 3;
        } else {
          cur = ((cur & 0x1F) << 6) +
                (y & 0x3F);
          i += 2;
        }
      } else {
        ++i;
      }
      buf[n] = ((uint16)cur);
      ++n;
    }
  
    JnjvmClassLoader* loader = classDef->classLoader;
    const UTF8* utf8 = loader->hashUTF8->lookupOrCreateReader(buf, n);
    ctpRes[entry] = const_cast<UTF8*>(utf8);
  
    PRINT_DEBUG(JNJVM_LOAD, 3, COLOR_NORMAL, "; [%5d] <utf8>\t\t\"%s\"\n",
                entry, UTF8Buffer(utf8)->cString());

  }
  return (const UTF8*)ctpRes[entry];
}

float JavaConstantPool::FloatAt(uint32 entry) {
  if (!((entry > 0) && (entry < ctpSize) && typeAt(entry) == ConstantFloat)) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  return ((float*)ctpDef)[entry];
}

sint32 JavaConstantPool::IntegerAt(uint32 entry) {
  if (!((entry > 0) && (entry < ctpSize) && typeAt(entry) == ConstantInteger)) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  return ((sint32*)ctpDef)[entry];
}

sint64 JavaConstantPool::LongAt(uint32 entry) {
  if (!((entry > 0) && (entry < ctpSize) && typeAt(entry) == ConstantLong)) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  return Reader::readLong(ctpDef[entry], ctpDef[entry + 1]);
}

double JavaConstantPool::DoubleAt(uint32 entry) {
  if (!((entry > 0) && (entry < ctpSize) && typeAt(entry) == ConstantDouble)) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  return Reader::readDouble(ctpDef[entry], ctpDef[entry + 1]);
}

CommonClass* JavaConstantPool::isClassLoaded(uint32 entry) {
  if (!((entry > 0) && (entry < ctpSize) &&  typeAt(entry) == ConstantClass)) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }

  CommonClass* res = (CommonClass*)ctpRes[entry];
  if (res == NULL) {
    JnjvmClassLoader* loader = classDef->classLoader;
    const UTF8* name = UTF8At(ctpDef[entry]);
    res = loader->lookupClassOrArray(name);
    ctpRes[entry] = res;
  }
  return res;
}

const UTF8* JavaConstantPool::resolveClassName(uint32 index) {
  CommonClass* cl = isClassLoaded(index);
  if (cl) return cl->name;
  else return UTF8At(ctpDef[index]);
}

CommonClass* JavaConstantPool::loadClass(uint32 index, bool resolve) {
  CommonClass* temp = isClassLoaded(index);
  if (!temp) {
    JnjvmClassLoader* loader = classDef->classLoader;
    const UTF8* name = UTF8At(ctpDef[index]);
    if (name->elements[0] == I_TAB) {
      temp = loader->constructArray(name);
    } else {
      temp = loader->loadName(name, resolve, false, NULL);
    }
    ctpRes[index] = temp;
  } else if (resolve && temp->isClass()) {
    temp->asClass()->resolveClass();
  }
  return temp;
}

CommonClass* JavaConstantPool::getMethodClassIfLoaded(uint32 index) {
  CommonClass* temp = isClassLoaded(index);

  if (classDef->classLoader->getCompiler()->isStaticCompiling()) {
    if (temp == NULL) {
      temp = loadClass(index, true);
    } else if (temp->isClass()) {
      temp->asClass()->resolveClass();
    }
  }

  return temp;
}

Typedef* JavaConstantPool::resolveNameAndType(uint32 index) {
  void* res = ctpRes[index];
  if (!res) {
    if (typeAt(index) != ConstantNameAndType) {
      fprintf(stderr, "Malformed class %s\n",
              UTF8Buffer(classDef->name).cString());
      abort();
    }
    sint32 entry = ctpDef[index];
    const UTF8* type = UTF8At(entry & 0xFFFF);
    Typedef* sign = classDef->classLoader->constructType(type);
    ctpRes[index] = sign;
    return sign;
  }
  return (Typedef*)res;
}

Signdef* JavaConstantPool::resolveNameAndSign(uint32 index) {
  void* res = ctpRes[index];
  if (!res) {
    if (typeAt(index) != ConstantNameAndType) {
      fprintf(stderr, "Malformed class %s\n",
              UTF8Buffer(classDef->name).cString());
      abort();
    }
    sint32 entry = ctpDef[index];
    const UTF8* type = UTF8At(entry & 0xFFFF);
    Signdef* sign = classDef->classLoader->constructSign(type);
    ctpRes[index] = sign;
    return sign;
  }
  return (Signdef*)res;
}

Typedef* JavaConstantPool::infoOfField(uint32 index) {
  if (typeAt(index) != ConstantFieldref) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }

  return resolveNameAndType(ctpDef[index] & 0xFFFF);
}

void JavaConstantPool::infoOfMethod(uint32 index, uint32 access, 
                                    CommonClass*& cl, JavaMethod*& meth) {
  uint8 id = typeAt(index);
  if (id != ConstantMethodref && id != ConstantInterfaceMethodref) {
    fprintf(stderr, "Malformed class %s\n", UTF8Buffer(classDef->name).cString());
    abort();
  }
  
  Signdef* sign = resolveNameAndSign(ctpDef[index] & 0xFFFF);
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  const UTF8* utf8 = UTF8At(ctpDef[ntIndex] >> 16);
  cl = getMethodClassIfLoaded(entry >> 16);
  if (cl) {
    Class* lookup = cl->isArray() ? cl->super : cl->asClass();
    if (lookup->isResolved()) {
    
      // lookup the method
      meth = lookup->lookupMethodDontThrow(utf8, sign->keyName, isStatic(access),
                                         true, 0);
      // OK, this is rare, but the Java bytecode may do an invokevirtual on an
      // interface method. Lookup the method as if it was static.
      // The caller is responsible for taking any action if the method is
      // an interface method.
      if (!meth) {
        meth = lookup->lookupInterfaceMethodDontThrow(utf8, sign->keyName);
      }
    }
  }
}

uint32 JavaConstantPool::getClassIndexFromMethod(uint32 index) {
  sint32 entry = ctpDef[index];
  return (uint32)(entry >> 16);
}


void JavaConstantPool::nameOfStaticOrSpecialMethod(uint32 index, 
                                              const UTF8*& cl,
                                              const UTF8*& name,
                                              Signdef*& sign) {
  uint8 id = typeAt(index);
  if (id != ConstantMethodref && id != ConstantInterfaceMethodref) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  
  sign = resolveNameAndSign(ctpDef[index] & 0xFFFF);
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  name = UTF8At(ctpDef[ntIndex] >> 16);
  cl = resolveClassName(entry >> 16);
}

JavaMethod* JavaConstantPool::infoOfStaticOrSpecialMethod(uint32 index, 
                                                          uint32 access,
                                                          Signdef* sign) {
  uint8 id = typeAt(index);
  if (id != ConstantMethodref && id != ConstantInterfaceMethodref) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  const UTF8* utf8 = UTF8At(ctpDef[ntIndex] >> 16);
  CommonClass* cl = getMethodClassIfLoaded(entry >> 16);
  JavaMethod* meth = 0;
  if (cl) {
    Class* lookup = cl->isArray() ? cl->super : cl->asClass();
    if (lookup->isResolved()) {
      // lookup the method
      if (isStatic(access)) {
        meth = lookup->lookupMethodDontThrow(utf8, sign->keyName,
                                             true, true, 0);
      } else {
        meth = lookup->lookupSpecialMethodDontThrow(utf8, sign->keyName,
                                                    classDef);
      }
    }
  }
  
  return meth;
}


Signdef* JavaConstantPool::infoOfInterfaceOrVirtualMethod(uint32 index,
                                                          const UTF8*& name) {

  uint8 id = typeAt(index);
  if (id != ConstantMethodref && id != ConstantInterfaceMethodref) {
    fprintf(stderr, "Malformed class %s\n",
            UTF8Buffer(classDef->name).cString());
    abort();
  }
  
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  Signdef* sign = resolveNameAndSign(ntIndex); 
  name = UTF8At(ctpDef[ntIndex] >> 16);
  return sign;
}

void JavaConstantPool::resolveMethod(uint32 index, CommonClass*& cl,
                                     const UTF8*& utf8, Signdef*& sign) {
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  sign = (Signdef*)ctpRes[ntIndex];
  assert(sign && "No cached signature after JITting");
  utf8 = UTF8At(ctpDef[ntIndex] >> 16);
  cl = loadClass(entry >> 16);
  assert(cl && "No class after loadClass");
  assert((cl->isArray() || (cl->isClass() && cl->asClass()->isResolved())) &&
         "Class not resolved after loadClass");
}
  
void JavaConstantPool::resolveField(uint32 index, CommonClass*& cl,
                                    const UTF8*& utf8, Typedef*& sign) {
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  sign = (Typedef*)ctpRes[ntIndex];
  assert(sign && "No cached Typedef after JITting");
  utf8 = UTF8At(ctpDef[ntIndex] >> 16);
  cl = loadClass(entry >> 16);
  assert(cl && "No class after loadClass");
  assert((cl->isClass() && cl->asClass()->isResolved()) && 
         "Class not resolved after loadClass");
}

JavaField* JavaConstantPool::lookupField(uint32 index, bool stat) {
  sint32 entry = ctpDef[index];
  sint32 ntIndex = entry & 0xFFFF;
  Typedef* sign = (Typedef*)ctpRes[ntIndex];
  const UTF8* utf8 = UTF8At(ctpDef[ntIndex] >> 16);
  CommonClass* cl = getMethodClassIfLoaded(entry >> 16);
  if (cl) {
    Class* lookup = cl->isArray() ? cl->super : cl->asClass();
    if (lookup->isResolved()) {
      JavaField* field = lookup->lookupFieldDontThrow(utf8, sign->keyName, stat,
                                                      true, 0);
      // don't throw if no field, the exception will be thrown just in time  
      if (field) {
        if (!stat) {
          ctpRes[index] = (void*)field->ptrOffset;
        } else if (lookup->isReady()) {
          void* S = field->classDef->getStaticInstance();
          ctpRes[index] = (void*)((uint64)S + field->ptrOffset);
        }
      }
      return field;
    }
  } 
  return 0;
}

JavaString* JavaConstantPool::resolveString(const UTF8* utf8, uint16 index) {
  JavaString* str = NULL;
  llvm_gcroot(str, 0);
  Jnjvm* vm = JavaThread::get()->getJVM();
  str = vm->internalUTF8ToStr(utf8);
  return str;
}

JavaConstantPool::ctpReader JavaConstantPool::funcsReader[16] = {
  unimplemented,
  CtpReaderUTF8,
  unimplemented,
  CtpReaderInteger,
  CtpReaderFloat,
  CtpReaderLong,
  CtpReaderDouble,
  CtpReaderClass,
  CtpReaderString,
  CtpReaderFieldref,
  CtpReaderMethodref,
  CtpReaderInterfaceMethodref,
  CtpReaderNameAndType,
  unimplemented,
  unimplemented,
  unimplemented
};
