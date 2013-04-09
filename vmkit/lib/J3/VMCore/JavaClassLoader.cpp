/*
 * JavaClassLoader.cpp
 *
 *  Created on: Apr 1, 2013
 *      Author: Joe Kulig
 * 
 * Copyright, (C) 2013 Joe Kulig
 */

#include "types.h"
#include "MvmDenseSet.h"
#include "JavaClassLoader.h"
#include "JavaClass.h"
#include "JavaCompiler.h"
#include "UTF8.h"
#include "JavaTypes.h"
#include "JMap.h"

#include <vector>
#include <stdio.h>
#include <cassert>

using namespace j3;

UTF8Map* JavaClassLoader::hashUTF8 = new UTF8Map();
ClassMap* JavaClassLoader::classes = new ClassMap();
TypeMap* JavaClassLoader::javaTypes = new TypeMap();
SignMap* JavaClassLoader::javaSignatures = new SignMap();

ClassPrimitive* JavaClassLoader::OfByte = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("byte"), 0);
ClassPrimitive* JavaClassLoader::OfChar = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("char"), 1);
ClassPrimitive* JavaClassLoader::OfInt = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("int"), 2);
ClassPrimitive* JavaClassLoader::OfShort = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("short"), 1);
ClassPrimitive* JavaClassLoader::OfBool = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("boolean"), 0);
ClassPrimitive* JavaClassLoader::OfLong = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("long"), 3);
ClassPrimitive* JavaClassLoader::OfFloat = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("float"), 2);
ClassPrimitive* JavaClassLoader::OfDouble = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("double"), 3);
ClassPrimitive* JavaClassLoader::OfVoid = new ClassPrimitive(JavaClassLoader::asciizConstructUTF8("void"), 0);

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

ClassPrimitive* JavaClassLoader::getPrimitiveClass(char id) {
  return primitiveMap[id];
}

const UTF8* JavaClassLoader::asciizConstructUTF8(const char* asciiz) {
    return hashUTF8->lookupOrCreateAsciiz(asciiz);
}

const UTF8* JavaClassLoader::readerConstructUTF8(const uint16* buf,
        uint32 size) {
    return hashUTF8->lookupOrCreateReader(buf, size);
}

Typedef* JavaClassLoader::internalConstructType(const UTF8* name) {
  short int cur = name->elements[0];
  Typedef* res = 0;
  switch (cur) {
    case I_TAB :
      res = new ArrayTypedef(name);
      break;
    case I_REF :
      res = new ObjectTypedef(name, hashUTF8);
      break;
    default :
      ClassPrimitive* cl =
        getPrimitiveClass((char)name->elements[0]);
      assert(cl && "No primitive");
      // fixme
      bool unsign = false;
//      bool unsign = (cl == bootstrapLoader->upcalls->OfChar ||
//                     cl == bootstrapLoader->upcalls->OfBool);
      res = new PrimitiveTypedef(name, cl,
                                                                unsign, cur);
  }
  return res;
}


Typedef* JavaClassLoader::constructType(const UTF8* name) {
  Typedef* res = javaTypes->map.lookup(name);
  if (res == 0) {
    res = internalConstructType(name);
    javaTypes->map[name] = res;
  }
  return res;
}

Signdef* JavaClassLoader::constructSign(const UTF8* name) {
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

    res = new Signdef(name, buf, ret);

    javaSignatures->map[name] = res;
  }
  return res;
}

ClassArray* JavaClassLoader::constructArray(const UTF8* name) {
  ClassArray* res = (ClassArray*)lookupClass(name);
  if (res) return res;

  CommonClass* cl = loadBaseClass(name, 1, name->size - 1);
  assert(cl && "no base class for an array");
  res = constructArray(name, cl);

  if (res) {
    ClassMap::iterator End = classes->map.end();
    ClassMap::iterator I = classes->map.find(res->name);
    if (I == End)
      classes->map.insert(std::make_pair(res->name, res));
  }
  return res;
}

Class* JavaClassLoader::constructClass(const UTF8* name,
                                            ClassBytes* bytes) {
  Class* res = NULL;
  res = (Class*) classes->map.lookup(name);
  if (res == NULL) {
      const UTF8* internalName = readerConstructUTF8(name->elements, name->size);
      res = new Class(internalName, bytes);
      res->readClass();
      res->makeVT();
      getCompiler()->resolveVirtualClass(res);
      getCompiler()->resolveStaticClass(res);
      assert(classes->map.lookup(internalName) == NULL);
      classes->map[internalName] = res;
  }
  if (res->super == NULL) {
    // java.lang.Object just got created, initialise VTs of arrays.
    ClassArray::initialiseVT(res);
    OfObject = res;
  }
  return res;
}

ClassArray* JavaClassLoader::constructArray(const UTF8* name, CommonClass* baseClass) {
  assert(baseClass && "constructing an array class without a base class");
  ClassArray* res = 0;
  res = (ClassArray*) classes->map.lookup(name);
  if (res == NULL) {
    const UTF8* internalName = readerConstructUTF8(name->elements, name->size);
    res = new ClassArray(internalName, baseClass);
    classes->map.insert(std::make_pair(internalName, res));
  }
  return res;
}

const UTF8* JavaClassLoader::lookupOrCreateAsciiz(const char *buf) {
	return hashUTF8->lookupOrCreateAsciiz(buf);
}

const UTF8* JavaClassLoader::lookupOrCreateReader(const uint16 *buf, uint32 n) {
	return hashUTF8->lookupOrCreateReader(buf, n);
}

Class* JavaClassLoader::loadName(const UTF8* name) {
	// fixme; add loadName(utf8*) in JavaCompiler
	return (Class*) classes->map.lookup(name);;
}

