/*
 * ClassLoader.cpp
 *
 *  Created on: Apr 1, 2013
 *      Author: Joe Kulig
 * 
 * Copyright, (C) 2013 Joe Kulig
 */

#include <cassert>

#include "types.h"
#include "MvmDenseSet.h"
#include "ClassLoader.h"
#include "JavaClass.h"
#include "JavaCompiler.h"
#include "UTF8.h"
#include "JavaTypes.h"
#include "JMap.h"

#include <vector>
#include <stdio.h>

using namespace j3;

UTF8Map* ClassLoader::hashUTF8 = new UTF8Map();
ClassMap* ClassLoader::classes = new ClassMap();
TypeMap* ClassLoader::javaTypes = new TypeMap();
SignMap* ClassLoader::javaSignatures = new SignMap();

ClassPrimitive* ClassLoader::OfByte = new ClassPrimitive(ClassLoader::asciizConstructUTF8("byte"), 0);
ClassPrimitive* ClassLoader::OfChar = new ClassPrimitive(ClassLoader::asciizConstructUTF8("char"), 1);
ClassPrimitive* ClassLoader::OfInt = new ClassPrimitive(ClassLoader::asciizConstructUTF8("int"), 2);
ClassPrimitive* ClassLoader::OfShort = new ClassPrimitive(ClassLoader::asciizConstructUTF8("short"), 1);
ClassPrimitive* ClassLoader::OfBool = new ClassPrimitive(ClassLoader::asciizConstructUTF8("boolean"), 0);
ClassPrimitive* ClassLoader::OfLong = new ClassPrimitive(ClassLoader::asciizConstructUTF8("long"), 3);
ClassPrimitive* ClassLoader::OfFloat = new ClassPrimitive(ClassLoader::asciizConstructUTF8("float"), 2);
ClassPrimitive* ClassLoader::OfDouble = new ClassPrimitive(ClassLoader::asciizConstructUTF8("double"), 3);
ClassPrimitive* ClassLoader::OfVoid = new ClassPrimitive(ClassLoader::asciizConstructUTF8("void"), 0);

Class* ClassLoader::OfObject;
JavaCompiler* ClassLoader::compiler;
ClassArray* ClassLoader::ArrayOfObject;
Class* ClassLoader::newString;
Class* ClassLoader::newClass;
Class* ClassLoader::newThrowable;

UTF8* ClassLoader::floatToRawIntBits;
UTF8* ClassLoader::intBitsToFloat;
UTF8* ClassLoader::doubleToRawLongBits;
UTF8* ClassLoader::longBitsToDouble;
UTF8* ClassLoader::initName;
UTF8* ClassLoader::clinitName;
UTF8* ClassLoader::sqrt;
UTF8* ClassLoader::sin;
UTF8* ClassLoader::cos;
UTF8* ClassLoader::tan;
UTF8* ClassLoader::asin;
UTF8* ClassLoader::acos;
UTF8* ClassLoader::atan;
UTF8* ClassLoader::atan2;
UTF8* ClassLoader::exp;
UTF8* ClassLoader::log;
UTF8* ClassLoader::pow;
UTF8* ClassLoader::ceil;
UTF8* ClassLoader::floor;
UTF8* ClassLoader::rint;
UTF8* ClassLoader::cbrt;
UTF8* ClassLoader::cosh;
UTF8* ClassLoader::expm1;
UTF8* ClassLoader::hypot;
UTF8* ClassLoader::log10;
UTF8* ClassLoader::log1p;
UTF8* ClassLoader::sinh;
UTF8* ClassLoader::tanh;
UTF8* ClassLoader::abs;

const UTF8* Attribut::annotationsAttribut = ClassLoader::asciizConstructUTF8("RuntimeVisibleAnnotations");
const UTF8* Attribut::codeAttribut = ClassLoader::asciizConstructUTF8("Code");
const UTF8* Attribut::exceptionsAttribut = ClassLoader::asciizConstructUTF8("Exceptions");
const UTF8* Attribut::constantAttribut = ClassLoader::asciizConstructUTF8("ConstantValue");
const UTF8* Attribut::lineNumberTableAttribut = ClassLoader::asciizConstructUTF8("LineNumberTable");
const UTF8* Attribut::innerClassesAttribut = ClassLoader::asciizConstructUTF8("InnerClasses");
const UTF8* Attribut::sourceFileAttribut = ClassLoader::asciizConstructUTF8("SourceFile");

UTF8* ClassLoader::stackWalkerName;
UTF8* ClassLoader::mathName;
UTF8* ClassLoader::VMFloatName;
UTF8* ClassLoader::VMDoubleName;

std::map<const char, ClassPrimitive*> ClassLoader::primitiveMap;

void ClassLoader::init() {
	ClassLoader::primitiveMap[I_VOID] = ClassLoader::OfVoid;
	ClassLoader::primitiveMap[I_BOOL] = ClassLoader::OfBool;
	ClassLoader::primitiveMap[I_BYTE] = ClassLoader::OfByte;
	ClassLoader::primitiveMap[I_CHAR] = ClassLoader::OfChar;
	ClassLoader::primitiveMap[I_SHORT] = ClassLoader::OfShort;
	ClassLoader::primitiveMap[I_INT] = ClassLoader::OfInt;
	ClassLoader::primitiveMap[I_FLOAT] = ClassLoader::OfFloat;
	ClassLoader::primitiveMap[I_LONG] = ClassLoader::OfLong;
	ClassLoader::primitiveMap[I_DOUBLE] = ClassLoader::OfDouble;
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

ClassPrimitive* ClassLoader::getPrimitiveClass(char id) {
  return primitiveMap[id];
}

const UTF8* ClassLoader::asciizConstructUTF8(const char* asciiz) {
    return hashUTF8->lookupOrCreateAsciiz(asciiz);
}

const UTF8* ClassLoader::readerConstructUTF8(const uint16* buf,
        uint32 size) {
    return hashUTF8->lookupOrCreateReader(buf, size);
}

Typedef* ClassLoader::internalConstructType(const UTF8* name) {
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
      res = new PrimitiveTypedef(name, cl, unsign, cur);
  }
  return res;
}


Typedef* ClassLoader::constructType(const UTF8* name) {
  Typedef* res = javaTypes->map.lookup(name);
  if (res == 0) {
    res = internalConstructType(name);
    javaTypes->map[name] = res;
  }
  return res;
}

Signdef* ClassLoader::constructSign(const UTF8* name) {
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

const UTF8* ClassLoader::lookupComponentName(const UTF8* name,
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

// fixme: implement
CommonClass* ClassLoader::loadBaseClass(const UTF8* name, uint32 start, uint32 len) {
	assert(false && "implement me");
	return NULL;
}

CommonClass* ClassLoader::lookupClass(const UTF8* name) {
	CommonClass* cl = classes->map.lookup(name);
	return cl;
}

CommonClass* ClassLoader::lookupClassOrArray(const UTF8* name) {
	CommonClass* cl = lookupClass(name);
	if(cl) return cl;

	if(name->elements[0] == I_TAB) {
		bool prim = false;
		const UTF8* componentName = lookupComponentName(name, 0, prim);
		if(prim) return constructArray(name);
		if(componentName) {
			CommonClass* cl = lookupClass(componentName);
			if(cl) return constructArray(name);
		}
	}
	return NULL;
}

ClassArray* ClassLoader::constructArray(const UTF8* name) {
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

Class* ClassLoader::constructClass(const UTF8* name,
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

ClassArray* ClassLoader::constructArray(const UTF8* name, CommonClass* baseClass) {
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

const UTF8* ClassLoader::lookupOrCreateAsciiz(const char *buf) {
	return hashUTF8->lookupOrCreateAsciiz(buf);
}

const UTF8* ClassLoader::lookupOrCreateReader(const uint16 *buf, uint32 n) {
	return hashUTF8->lookupOrCreateReader(buf, n);
}

Class* ClassLoader::loadName(const UTF8* name) {
	// fixme; add loadName(utf8*) in JavaCompiler
	return (Class*) classes->map.lookup(name);;
}

// fixme: implement
JavaString** ClassLoader::UTF8ToStr(const UTF8* utf8) {
	assert(false && "implement me");
	return NULL;
}

// fixme: implement
const UTF8* ClassLoader::constructArrayName(int, const UTF8*) {
	assert(false && "implement me");
	return NULL;
}
