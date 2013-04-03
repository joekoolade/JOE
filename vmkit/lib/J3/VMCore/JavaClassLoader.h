/*
 * JavaClassLoader.h
 *
 *  Created on: Apr 1, 2013
 *      Author: Joe Kulig
 * 
 * Copyright, (C) 2013 Joe Kulig
 */

#ifndef JAVACLASSLOADER_H_
#define JAVACLASSLOADER_H_

namespace j3 {

class Signdef;
class Typedef;
class Class;
class ClassBytes;
class JavaCompiler;
class UTF8Map;

class JavaClassLoader {
private:
	/// javaTypes - Tables of Typedef defined by this class loader.
	///
	static TypeMap* javaTypes;

	/// javaSignatures - Tables of Signdef defined by this class loader.
	///
	static SignMap* javaSignatures;
	///
	/// classes - The classes this class loader has loaded.
	///
	static ClassMap* classes;
	/// primitiveMap - Map of primitive classes, hashed by id.
	static std::map<const char, ClassPrimitive*> primitiveMap;
	/// hashUTF8 - Tables of UTF8s defined by this class loader.
	///
	static UTF8Map* hashUTF8;
	static JavaCompiler* compiler;
public:
	static JavaCompiler* getCompiler() {
		return compiler;
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

/// readerConstructUTF8 - Hashes an UTF8 created from the given Unicode
/// buffer.
///
static const UTF8* readerConstructUTF8(const uint16* buf, uint32 size);

/// internalLoad - Load the class with the given name.
///
static Class* internalLoad(const UTF8* utf8, bool doResolve,
                                JavaString* strName);

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

static CommonClass* loadBaseClass(const UTF8* name, uint32 start, uint32 len);

/// constructClass - Hashes a runtime representation of a class with
/// the given name.
///
static Class* constructClass(const UTF8* name, ClassBytes* bytes);


};


#endif /* JAVACLASSLOADER_H_ */
