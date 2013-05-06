/*
 * ClassLoader.h
 *
 *  Created on: Apr 1, 2013
 *      Author: Joe Kulig
 * 
 * Copyright, (C) 2013 Joe Kulig
 */

#ifndef CLASSLOADER_H_
#define CLASSLOADER_H_

#include <map>

namespace j3 {

class Signdef;
class Typedef;
class Class;
class ClassMap;
class ClassBytes;
class ClassArray;
class CommonClass;
class ClassPrimitive;
class JavaCompiler;
class UTF8Map;
class TypeMap;
class SignMap;
class JavaString;
class UTF8;

class ClassLoader {
private:
	/// javaTypes - Tables of Typedef defined by this class loader.
	///
	static TypeMap* javaTypes;

	/// javaSignatures - Tables of Signdef defined by this class loader.
	///
	static SignMap* javaSignatures;

	/// hashUTF8 - Tables of UTF8s defined by this class loader.
	///
	static UTF8Map* hashUTF8;
	static JavaCompiler* compiler;
public:
	///
	/// classes - The classes this class loader has loaded.
	///
	static ClassMap* classes;

	/// primitiveMap - Map of primitive classes, hashed by id.
	static std::map<const char, ClassPrimitive*> primitiveMap;

	static void init();
	static JavaCompiler* getCompiler() {
		return compiler;
	}

	static ClassArray* getArrayClass(int) {
	    return NULL;
	}

	static void setCompiler(JavaCompiler *newCompiler) {
		compiler = newCompiler;
	}
/// constructType - Hashes a Typedef, an internal representation of a class
/// still not loaded.
///
static Typedef* constructType(const UTF8 * name);

/// constructSign - Hashes a Signdef, a method signature.
///
static Signdef* constructSign(const UTF8 * name);

static ClassPrimitive* getPrimitiveClass(char id);

/// internalConstructType - Hashes a Typedef, an internal representation of
/// a class still not loaded.
///
static Typedef* internalConstructType(const UTF8 * name);

/// asciizConstructUTF8 - Hashes an UTF8 created from the given asciiz.
static const UTF8* asciizConstructUTF8(const char* asciiz);

static JavaString** UTF8ToStr(const UTF8* utf8);

/// readerConstructUTF8 - Hashes an UTF8 created from the given Unicode
/// buffer.
///
static const UTF8* readerConstructUTF8(const uint16_t* buf, uint32_t size);

/// internalLoad - Load the class with the given name.
///
static Class* internalLoad(const UTF8* utf8, bool doResolve, JavaString* strName);

static Class* loadName(const UTF8* name);

static const UTF8* lookupComponentName(const UTF8* name, UTF8* holder, bool& prim);

/// lookupClass - Finds the class of the given name in the class loader's
/// table.
///
static CommonClass* lookupClass(const UTF8* utf8);

/// lookupClassOrArray - Finds the class of the given name in the class
/// loader's table. If the class has not been loaded, and if it's an
/// array whose base class is loaded, then this function loads the array class
/// and returns it.
///
static CommonClass* lookupClassOrArray(const UTF8* utf8);

/// constructArray - Hashes a runtime representation of a class with
/// the given name.
///
static ClassArray* constructArray(const UTF8* name);
static ClassArray* constructArray(const UTF8* name, CommonClass* base);
static const UTF8* constructArrayName(int, const UTF8*);

static CommonClass* loadBaseClass(const UTF8* name, uint32 start, uint32 len);

/// constructClass - Hashes a runtime representation of a class with
/// the given name.
///
static Class* constructClass(const UTF8* name, ClassBytes* bytes);

static const UTF8* lookupOrCreateReader(const uint16 *, uint32);
static const UTF8* lookupOrCreateAsciiz(const char *buf);

static UTF8* floatToRawIntBits;
static UTF8* intBitsToFloat;
static UTF8* doubleToRawLongBits;
static UTF8* longBitsToDouble;
static UTF8* initName;
static UTF8* clinitName;
static UTF8* sqrt;
static UTF8* sin;
static UTF8* cos;
static UTF8* tan;
static UTF8* asin;
static UTF8* acos;
static UTF8* atan;
static UTF8* atan2;
static UTF8* exp;
static UTF8* log;
static UTF8* pow;
static UTF8* ceil;
static UTF8* floor;
static UTF8* rint;
static UTF8* cbrt;
static UTF8* cosh;
static UTF8* expm1;
static UTF8* hypot;
static UTF8* log10;
static UTF8* log1p;
static UTF8* sinh;
static UTF8* tanh;
static UTF8* abs;

static UTF8* stackWalkerName;
static UTF8* mathName;
static UTF8* VMFloatName;
static UTF8* VMDoubleName;

static ClassPrimitive* OfByte;
static ClassPrimitive* OfChar;
static ClassPrimitive* OfInt;
static ClassPrimitive* OfShort;
static ClassPrimitive* OfBool;
static ClassPrimitive* OfLong;
static ClassPrimitive* OfFloat;
static ClassPrimitive* OfDouble;
static ClassPrimitive* OfVoid;
static Class* OfObject;
static ClassArray* ArrayOfObject;
static Class* newString;
static Class* newClass;
static Class* newThrowable;
};
}


#endif /* CLASSLOADER_H_ */
