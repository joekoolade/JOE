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
	TypeMap* javaTypes;

	/// javaSignatures - Tables of Signdef defined by this class loader.
	///
	SignMap* javaSignatures;

	/// hashUTF8 - Tables of UTF8s defined by this class loader.
	///
	UTF8Map* hashUTF8;
	JavaCompiler* compiler;
public:
	///
	/// classes - The classes this class loader has loaded.
	///
	ClassMap* classes;

	/// primitiveMap - Map of primitive classes, hashed by id.
	std::map<const char, ClassPrimitive*> primitiveMap;

	void init();
	ClassLoader(JavaCompiler*);

	JavaCompiler* getCompiler() const {
		return compiler;
	}

	ClassArray* getArrayClass(int) {
	    return NULL;
	}

	void setCompiler(JavaCompiler *newCompiler) {
		compiler = newCompiler;
	}
/// constructType - Hashes a Typedef, an internal representation of a class
/// still not loaded.
///
Typedef* constructType(const UTF8 * name);

/// constructSign - Hashes a Signdef, a method signature.
///
Signdef* constructSign(const UTF8 * name);

ClassPrimitive* getPrimitiveClass(char id);

/// internalConstructType - Hashes a Typedef, an internal representation of
/// a class still not loaded.
///
Typedef* internalConstructType(const UTF8 * name);

/// asciizConstructUTF8 - Hashes an UTF8 created from the given asciiz.
const UTF8* asciizConstructUTF8(const char* asciiz);

JavaString** UTF8ToStr(const UTF8* utf8);

/// readerConstructUTF8 - Hashes an UTF8 created from the given Unicode
/// buffer.
///
const UTF8* readerConstructUTF8(const uint16_t* buf, uint32_t size);

/// internalLoad - Load the class with the given name.
///
Class* internalLoad(const UTF8* utf8, bool doResolve, JavaString* strName);

Class* loadName(const UTF8* name, ClassBytes* data);

/// loadName - Loads the class of the given name.
///
Class* loadName(const UTF8* name, bool doResolve, JavaString* strName);

const UTF8* lookupComponentName(const UTF8* name, UTF8* holder, bool& prim);

/// lookupClass - Finds the class of the given name in the class loader's
/// table.
///
CommonClass* lookupClass(const UTF8* utf8);

/// lookupClassOrArray - Finds the class of the given name in the class
/// loader's table. If the class has not been loaded, and if it's an
/// array whose base class is loaded, then this function loads the array class
/// and returns it.
///
CommonClass* lookupClassOrArray(const UTF8* utf8);

/// constructArray - Hashes a runtime representation of a class with
/// the given name.
///
ClassArray* constructArray(const UTF8* name);
ClassArray* constructArray(const UTF8* name, CommonClass* base);
const UTF8* constructArrayName(int, const UTF8*);

CommonClass* loadBaseClass(const UTF8* name, uint32 start, uint32 len);

/// constructClass - Hashes a runtime representation of a class with
/// the given name.
///
Class* constructClass(const UTF8* name, ClassBytes* bytes);

const UTF8* lookupOrCreateReader(const uint16 *, uint32);
const UTF8* lookupOrCreateAsciiz(const char *buf);

UTF8* floatToRawIntBits;
UTF8* intBitsToFloat;
UTF8* doubleToRawLongBits;
UTF8* longBitsToDouble;
UTF8* initName;
UTF8* clinitName;
UTF8* sqrt;
UTF8* sin;
UTF8* cos;
UTF8* tan;
UTF8* asin;
UTF8* acos;
UTF8* atan;
UTF8* atan2;
UTF8* exp;
UTF8* log;
UTF8* pow;
UTF8* ceil;
UTF8* floor;
UTF8* rint;
UTF8* cbrt;
UTF8* cosh;
UTF8* expm1;
UTF8* hypot;
UTF8* log10;
UTF8* log1p;
UTF8* sinh;
UTF8* tanh;
UTF8* abs;

UTF8* stackWalkerName;
UTF8* mathName;
UTF8* VMFloatName;
UTF8* VMDoubleName;

ClassPrimitive* OfByte;
ClassPrimitive* OfChar;
ClassPrimitive* OfInt;
ClassPrimitive* OfShort;
ClassPrimitive* OfBool;
ClassPrimitive* OfLong;
ClassPrimitive* OfFloat;
ClassPrimitive* OfDouble;
ClassPrimitive* OfVoid;
Class* OfObject;
ClassArray* ArrayOfObject;
Class* newString;
Class* newClass;
Class* newThrowable;
};
}


#endif /* CLASSLOADER_H_ */
