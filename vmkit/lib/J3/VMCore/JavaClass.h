//===-------- JavaClass.h - Java class representation -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JAVA_CLASS_H
#define JAVA_CLASS_H

#include "types.h"
#include "JavaAccess.h"

#include <stdarg.h>
#include <sys/time.h>

#include <cassert>

#include <set>

namespace j3 {

class ArrayObject;
class ArrayUInt8;
class ArrayUInt16;
class ClassBytes;
class JavaArray;
class JavaConstantPool;
class JavaField;
class JavaMethod;
class JavaVirtualTable;
class Reader;
class Typedef;
class Signdef;
class JavaClassLoader;
class UTF8;
class Class;
class ClassPrimitive;
class CommonClass;
class ClassArray;

/// JavaState - List of states a Java class can have. A class is ready to be
/// used (i.e allocating instances of the class, calling methods of the class
/// and accessing static fields of the class) when it is in the ready state.
///
//#define loaded 0       /// The .class file has been found.
//#define resolving 1    /// The .class file is being resolved.
//#define resolved 2     /// The class has been resolved.
//#define vmjc 3         /// The class is defined in a shared library.
//#define inClinit 4     /// The class is cliniting.
//#define ready 5        /// The class is ready to be used.
//#define erroneous 6    /// The class is in an erroneous state.
//

class AnnotationReader {
public:
  Reader& reader;
  Class* cl;
  uint16 AnnotationNameIndex;

  AnnotationReader(Reader& R, Class* C) : reader(R), cl(C),
    AnnotationNameIndex(0) {}
  void readAnnotation();
  void readElementValue();
};

/// Attribut - This class represents JVM attributes to Java class, methods and
/// fields located in the .class file.
///
class Attribut {
public:
  
  /// name - The name of the attribut. These are specified in the JVM book.
  /// Experimental attributes exist, but the JnJVM does nor parse them.
  ///
  const UTF8* name;

  /// start - The offset in the class of this attribut.
  ///
  uint32 start;

  /// nbb - The size of the attribut.
  ///
  uint32 nbb;

  /// Attribut - Create an attribut at the given length and offset.
  ///
  Attribut(const UTF8* name, uint32 length, uint32 offset);
  Attribut() {}

  /// codeAttribut - The "Code" JVM attribut. This is a method attribut for
  /// finding the bytecode of a method in the .class file.
  //
  static const UTF8* codeAttribut;
  
  /// annotationsAttribut - The "RuntimeVisibleAnnotations" JVM attribut.
  /// This is a method attribut for getting the runtime annotations.
  //
  static const UTF8* annotationsAttribut;

  /// exceptionsAttribut - The "Exceptions" attribut. This is a method
  /// attribut for finding the exception table of a method in the .class
  /// file.
  ///
  static const UTF8* exceptionsAttribut;

  /// constantAttribut - The "ConstantValue" attribut. This is a field attribut
  /// when the field has a static constant value.
  ///
  static const UTF8* constantAttribut;

  /// lineNumberTableAttribut - The "LineNumberTable" attribut. This is used
  /// for corresponding JVM bytecode to source line in the .java file.
  ///
  static const UTF8* lineNumberTableAttribut;

  /// innerClassAttribut - The "InnerClasses" attribut. This is a class attribut
  /// for knowing the inner/outer informations of a Java class.
  ///
  static const UTF8* innerClassesAttribut;

  /// sourceFileAttribut - The "SourceFile" attribut. This is a class attribut
  /// and gives the correspondance between a class and the name of its Java
  /// file.
  ///
  static const UTF8* sourceFileAttribut;
  
};

/// CommonClass - This class is the root class of all Java classes. It is
/// GC-allocated because CommonClasses have to be traceable. A java/lang/Class
/// object that stays in memory has a reference to the class. Same for
/// super or interfaces.
///
class CommonClass {

public:
  
//===----------------------------------------------------------------------===//
//
// If you want to add new fields or modify the types of fields, you must also
// change their LLVM representation in LLVMRuntime/runtime-*.ll, and their
// offsets in JnjvmModule.cpp.
//
//===----------------------------------------------------------------------===//
 
  /// access - {public, private, protected}.
  ///
  uint32 access;
  
  /// interfaces - The interfaces this class implements.
  ///
  Class** interfaces; 
  uint16 nbInterfaces;
  
  /// name - The name of the class.
  ///
  const UTF8* name;
   
  /// super - The parent of this class.
  ///
  Class * super;
   
  /// virtualVT - The virtual table of instances of this class.
  ///
  JavaVirtualTable* virtualVT;
  

//===----------------------------------------------------------------------===//
//
// End field declaration.
//
//===----------------------------------------------------------------------===//

  bool isSecondaryClass();

  // Assessor methods.
  uint32 getAccess() const      { return access & 0xFFFF; }
  Class** getInterfaces() const { return interfaces; }
  const UTF8* getName() const   { return name; }
  Class* getSuper() const       { return super; }
  
  /// isArray - Is the class an array class?
  ///
  inline bool isArray() const {
    return (access & 0x20000);
  }
  
  /// isPrimitive - Is the class a primitive class?
  ///
  inline bool isPrimitive() const {
    return (access & 0x40000);
  }
  
  /// isInterface - Is the class an interface?
  ///
  inline bool isInterface() const {
    return (access & 0x0200);
  }
  
  /// isClass - Is the class a real, instantiable class?
  ///
  inline bool isClass() const {
    return (access & 0x10000);
  }

  /// asClass - Returns the class as a user-defined class
  /// if it is not a primitive or an array.
  ///
  Class* asClass() {
    if (isClass()) return (Class*)this;
    return 0;
  }
  
  /// asClass - Returns the class as a user-defined class
  /// if it is not a primitive or an array.
  ///
  const Class* asClass() const {
    if (isClass()) return (const Class*)this;
    return 0;
  }
  
  /// asPrimitiveClass - Returns the class if it's a primitive class.
  ///
  ClassPrimitive* asPrimitiveClass() {
    if (isPrimitive()) return (ClassPrimitive*)this;
    return 0;
  }
  
  const ClassPrimitive* asPrimitiveClass() const {
    if (isPrimitive()) return (const ClassPrimitive*)this;
    return 0;
  }
  
  /// asArrayClass - Returns the class if it's an array class.
  ///
  ClassArray* asArrayClass() {
    if (isArray()) return (ClassArray*)this;
    return 0;
  }
  
  const ClassArray* asArrayClass() const {
    if (isArray()) return (const ClassArray*)this;
    return 0;
  }

  /// tracer - The tracer of this GC-allocated class.
  ///
  void tracer(word_t closure);
  
  /// inheritName - Does this class in its class hierarchy inherits
  /// the given name? Equality is on the name. This function does not take
  /// into account array classes.
  ///
  bool inheritName(const uint16* buf, uint32 len);

  /// isOfTypeName - Does this class inherits the given name? Equality is on
  /// the name. This function takes into account array classes.
  ///
  bool isOfTypeName(const UTF8* Tname);

  /// isAssignableFrom - Is this class assignable from the given class? The
  /// classes may be of any type.
  ///
  bool isAssignableFrom(CommonClass* cl);

  /// CommonClass - Create a class with th given name.
  ///
  CommonClass(const UTF8* name);
  
  /// ~CommonClass - Free memory used by this class, and remove it from
  /// metadata.
  ///
  ~CommonClass();

  /// setInterfaces - Set the interfaces of the class.
  ///
  void setInterfaces(Class** I) {
    interfaces = I;
  }
 
  /// toPrimitive - Returns the primitive class which represents
  /// this class, ie void for java/lang/Void.
  ///
  ClassPrimitive* toPrimitive() const;
 
  /// getInternal - Return the class.
  ///
  CommonClass* getInternal() {
    return this;
  }
 
};

/// ClassPrimitive - This class represents internal classes for primitive
/// types, e.g. java/lang/Integer.TYPE.
///
class ClassPrimitive : public CommonClass {
public:
  
  /// logSize - The log size of this class, eg 2 for int.
  ///
  uint32 logSize;
  
  
  /// ClassPrimitive - Constructs a primitive class. Only called at boot
  /// time.
  ///
  ClassPrimitive(const UTF8* name, uint32 nb);

  /// byteIdToPrimitive - Get the primitive class from its byte representation,
  /// ie int for I.
  ///
  static ClassPrimitive* byteIdToPrimitive(char id);
  
};


/// Class - This class is the representation of Java regular classes (i.e not
/// array or primitive). Theses classes have a constant pool.
///
class Class : public CommonClass {

public:
  
  /// virtualSize - The size of instances of this class.
  /// 
  uint32 virtualSize;

  /// aligment - Alignment of instances of this class.
  ///
  uint32 alignment;

  /// virtualFields - List of all the virtual fields defined in this class.
  /// This does not contain non-redefined super fields.
  JavaField* virtualFields;
  
  /// nbVirtualFields - The number of virtual fields.
  ///
  uint16 nbVirtualFields;

  /// staticFields - List of all the static fields defined in this class.
  ///
  JavaField* staticFields;

  /// nbStaticFields - The number of static fields.
  ///
  uint16 nbStaticFields;
  
  /// virtualMethods - List of all the virtual methods defined by this class.
  /// This does not contain non-redefined super methods.
  JavaMethod* virtualMethods;

  /// nbVirtualMethods - The number of virtual methods.
  ///
  uint16 nbVirtualMethods;
  
  /// staticMethods - List of all the static methods defined by this class.
  ///
  JavaMethod* staticMethods;

  /// nbStaticMethods - The number of static methods.
  ///
  uint16 nbStaticMethods;
  
  /// bytes - The .class file of this class.
  ///
  ClassBytes* bytes;

  /// ctpInfo - The constant pool info of this class.
  ///
  JavaConstantPool* ctpInfo;

  /// attributs - JVM attributes of this class.
  ///
  Attribut* attributs;

  /// nbAttributs - The number of attributes.
  ///
  uint16 nbAttributs;
  
  /// innerClasses - The inner classes of this class.
  ///
  Class** innerClasses;
  
  /// nbInnerClasses - The number of inner classes.
  ///
  uint16 nbInnerClasses;

  /// outerClass - The outer class, if this class is an inner class.
  ///
  Class* outerClass;
  
  /// innerAccess - The access of this class, if this class is an inner class.
  ///
  uint16 innerAccess;

  /// innerOuterResolved - Is the inner/outer resolution done?
  ///
  bool innerOuterResolved;
  
  // Are all classes in the constant pool resolved?
  bool resolved;

  /// isAnonymous - Is the class an anonymous class?
  ///
  bool isAnonymous;

  /// virtualTableSize - The size of the virtual table of this class.
  ///
  uint32 virtualTableSize;
  
  /// staticSize - The size of the static instance of this class.
  ///
  uint32 staticSize;

  /// getVirtualSize - Get the virtual size of instances of this class.
  ///
  uint32 getVirtualSize() const { return virtualSize; }
  
  /// getVirtualVT - Get the virtual VT of instances of this class.
  ///
  JavaVirtualTable* getVirtualVT() const { return virtualVT; }

  /// getOuterClass - Get the class that contains the definition of this class.
  ///
  Class* getOuterClass() const {
    return outerClass;
  }

  /// getInnterClasses - Get the classes that this class defines.
  ///
  Class** getInnerClasses() const {
    return innerClasses;
  }

  /// lookupMethodDontThrow - Lookup a method in the method map of this class.
  /// Do not throw if the method is not found.
  ///
  JavaMethod* lookupMethodDontThrow(const UTF8* name, const UTF8* type,
                                    bool isStatic, bool recurse, Class** cl);
  
  /// lookupInterfaceMethodDontThrow - Lookup a method in the interfaces of
  /// this class.
  /// Do not throw if the method is not found.
  ///
  JavaMethod* lookupInterfaceMethodDontThrow(const UTF8* name,
                                             const UTF8* type);
  
  /// lookupSpecialMethodDontThrow - Lookup a method following the
  /// invokespecial specification.
  /// Do not throw if the method is not found.
  ///
  JavaMethod* lookupSpecialMethodDontThrow(const UTF8* name,
                                           const UTF8* type,
                                           Class* current);
  
  /// lookupMethod - Lookup a method and throw an exception if not found.
  ///
  JavaMethod* lookupMethod(const UTF8* name, const UTF8* type, bool isStatic,
                           bool recurse, Class** cl);
  
  /// lookupInterfaceMethodDontThrow - Lookup a method in the interfaces of
  /// this class.
  /// Throws a MethodNotFoundError if the method can not ne found.
  ///
  JavaMethod* lookupInterfaceMethod(const UTF8* name, const UTF8* type);

  /// lookupFieldDontThrow - Lookup a field in the field map of this class. Do
  /// not throw if the field is not found.
  ///
  JavaField* lookupFieldDontThrow(const UTF8* name, const UTF8* type,
                                  bool isStatic, bool recurse,
                                  Class** definingClass);
  
  /// lookupField - Lookup a field and throw an exception if not found.
  ///
  JavaField* lookupField(const UTF8* name, const UTF8* type, bool isStatic,
                         bool recurse, Class** definingClass);
   
  /// Assessor methods.
  JavaField* getStaticFields() const    { return staticFields; }
  JavaField* getVirtualFields() const   { return virtualFields; }
  JavaMethod* getStaticMethods() const  { return staticMethods; }
  JavaMethod* getVirtualMethods() const { return virtualMethods; }

  
  /// setInnerAccess - Set the access flags of this inner class.
  ///
  void setInnerAccess(uint16 access) {
    innerAccess = access;
  }
   
  /// getStaticSize - Get the size of the static instance.
  ///
  uint32 getStaticSize() const {
    return staticSize;
  }
  
  /// tracer - Tracer function of instances of Class.
  ///
  void tracer(word_t closure);
  
  ~Class();
  Class();
  
  /// lookupAttribut - Look up a JVM attribut of this class.
  ///
  Attribut* lookupAttribut(const UTF8* key);
  
  /// allocateStaticInstance - Allocate the static instance of this class.
  ///
  void* allocateStaticInstance();
  
  /// Class - Create a class in the given virtual machine and with the given
  /// name.
  Class(const UTF8* name, ClassBytes* bytes);
  
  /// readParents - Reads the parents, i.e. super and interfaces, of the class.
  ///
  void readParents(Reader& reader);

  /// loadExceptions - Loads and resolves the exception classes used in catch 
  /// clauses of methods defined in this class.
  ///
  void loadExceptions();

  /// readAttributs - Reads the attributs of the class.
  ///
  Attribut* readAttributs(Reader& reader, uint16& size);

  /// readFields - Reads the fields of the class.
  ///
  void readFields(Reader& reader);

  /// readMethods - Reads the methods of the class.
  ///
  void readMethods(Reader& reader);
  
  /// readClass - Reads the class.
  ///
  void readClass();
 
  /// getConstantPool - Get the constant pool of the class.
  ///
  JavaConstantPool* getConstantPool() const {
    return ctpInfo;
  }

  /// getBytes - Get the bytes of the class file.
  ///
  ClassBytes* getBytes() const {
    return bytes;
  }
  
  /// resolveInnerOuterClasses - Resolve the inner/outer information.
  ///
  void resolveInnerOuterClasses();

  /// resolveClass - If the class has not been resolved yet, resolve it.
  ///
  void resolveClass();
  void resolveParents();

  /// initialiseClass - If the class has not been initialized yet,
  /// initialize it.
  ///
  void initialiseClass();
  
  /// acquire - Acquire this class lock.
  ///
  void acquire();
  
  /// release - Release this class lock.
  ///
  void release();

  /// waitClass - Wait for the class to be loaded/initialized/resolved.
  ///
  void waitClass();
  
  /// broadcastClass - Unblock threads that were waiting on the class being
  /// loaded/initialized/resolved.
  ///
  void broadcastClass();
  
  /// isReadyForCompilation - Can this class be inlined when JITing?
  ///
  bool isReadyForCompilation() {
    return isReady();
  }
  
  /// setResolved - Set the status of the class as resolved.
  ///
  void setResolved() {
    resolved = true;
  }
  
  /// isReady - Has this class been initialized?
  ///
  bool isReady() {
    return true;
  }
  
  bool isResolved() {
    return resolved;
  }

  // fixme
  // move to java types
  bool isStatic(int);
  bool isAbstract(int);
  bool isPublic(int);
  bool isNative(int);

  /// isNativeOverloaded - Is the method overloaded with a native function?
  ///
  bool isNativeOverloaded(JavaMethod* meth);

  /// needsInitialisationCheck - Does the method need an initialisation check?
  ///
  bool needsInitialisationCheck();

  /// fillIMT - Fill the vector with vectors of methods with the same IMT
  /// index.
  ///
  void fillIMT(std::set<JavaMethod*>* meths);

  /// makeVT - Create the virtual table of this class.
  ///
  void makeVT();

};

/// ClassArray - This class represents Java array classes.
///
class ClassArray : public CommonClass {

public:
  
  /// _baseClass - The base class of the array.
  ///
  CommonClass*  _baseClass;

  /// baseClass - Get the base class of this array class.
  ///
  CommonClass* baseClass() const {
    return _baseClass;
  }

  /// ClassArray - Construct a Java array class with the given name.
  ///
  ClassArray(const UTF8* name,
             CommonClass* baseClass);
  
  /// SuperArray - The super of class arrays. Namely java/lang/Object.
  ///
  static Class* SuperArray;

  /// InterfacesArray - The list of interfaces for array classes.
  ///
  static Class** InterfacesArray;

  /// initialiseVT - Initialise the primitive and reference array VT.
  /// super is the java/lang/Object class.
  ///
  static void initialiseVT(Class* javaLangObject);
  
};

class FrameInfo {
public:
  void* Metadata;
  word_t ReturnAddress;
  uint16_t SourceIndex;
  uint16_t FrameSize;
  uint16_t NumLiveOffsets;
  int16_t LiveOffsets[1];
};

/// JavaMethod - This class represents Java methods.
///
class JavaMethod {
private:

  /// _signature - The signature of this method. Null if not resolved.
  ///
  Signdef* _signature;

public:
  
  enum Type {
    Static,
    Special,
    Interface,
    Virtual
  };

  /// initialise - Create a new method.
  ///
  void initialise(Class* cl, const UTF8* name, const UTF8* type, uint16 access);
   
  /// compiledPtr - Return a pointer to the compiled code of this Java method,
  /// compiling it if necessary.
  ///
  void* compiledPtr(Class* customizeFor = NULL);

  /// setNative - Set the method as native.
  ///
  void setNative();
  
  /// JavaMethod - Delete the method as well as the cache enveloppes and
  /// attributes of the method.
  ///
  ~JavaMethod();

  /// access - Java access type of this method (e.g. private, public...).
  ///
  uint16 access;

  /// attributs - List of Java attributs of this method.
  ///
  Attribut* attributs;
  
  /// nbAttributs - The number of attributes.
  ///
  uint16 nbAttributs;

  /// classDef - The Java class where the method is defined.
  ///
  Class* classDef;

  /// name - The name of the method.
  ///
  const UTF8* name;

  /// type - The UTF8 signature of the method.
  ///
  const UTF8* type;

  /// isCustomizable - Can the method be customizable?
  ///
  bool isCustomizable;

  /// code - Pointer to the compiled code of this method.
  ///
  void* code;
 
  /// offset - The index of the method in the virtual table.
  ///
  uint32 offset;

  /// lookupAttribut - Look up an attribut in the method's attributs. Returns
  /// null if the attribut is not found.
  ///
  Attribut* lookupAttribut(const UTF8* key);

  /// lookupLineNumber - Find the line number based on the given frame info.
  ///
  uint16 lookupLineNumber(FrameInfo* FI);
  
  /// lookupCtpIndex - Lookup the constant pool index pointed by the opcode
  /// related to the given frame info.
  ///
  uint16 lookupCtpIndex(FrameInfo* FI);
  
  /// getSignature - Get the signature of thes method, resolving it if
  /// necessary.
  ///
  Signdef* getSignature();
  
  /// toString - Return an array of chars, suitable for creating a string.
  ///
  ArrayUInt16* toString() const;
  
  /// jniConsFromMeth - Construct the JNI name of this method as if
  /// there is no other function in the class with the same name.
  ///
  void jniConsFromMeth(char* buf) const {
    jniConsFromMeth(buf, classDef->name, name, type, access & 0x1000);
  }

  /// jniConsFromMethOverloaded - Construct the JNI name of this method
  /// as if its name is overloaded in the class.
  ///
  void jniConsFromMethOverloaded(char* buf) const {
    jniConsFromMethOverloaded(buf, classDef->name, name, type,
    		access & 0x1000);
  }
  
  /// jniConsFromMeth - Construct the non-overloaded JNI name with
  /// the given name and type.
  ///
  static void jniConsFromMeth(char* buf, const UTF8* clName, const UTF8* name,
                              const UTF8* sign, bool synthetic);

  /// jniConsFromMethOverloaded - Construct the overloaded JNI name with
  /// the given name and type.
  ///
  static void jniConsFromMethOverloaded(char* buf, const UTF8* clName,
                                        const UTF8* name, const UTF8* sign,
                                        bool synthetic);
  
  /// getParameterTypes - Get the java.lang.Class of the parameters of
  /// the method, with the given class loader.
  ///
  ArrayObject* getParameterTypes();

  /// getExceptionTypes - Get the java.lang.Class of the exceptions of the
  /// method, with the given class loader.
  ///
  ArrayObject* getExceptionTypes();

//===----------------------------------------------------------------------===//
//
// Upcalls from JnJVM code to Java code. 
//
//===----------------------------------------------------------------------===//
  
  
  #define JNI_NAME_PRE "Java_"
  #define JNI_NAME_PRE_LEN 5
  
};

/// JavaField - This class represents a Java field.
///
class JavaField {
private:
  /// _signature - The signature of the field. Null if not resolved.
  ///
  Typedef* _signature;
  
  /// InitField - Set an initial value to the field.
  ///
  void InitStaticField(uint64 val);
  void InitStaticField(double val);
  void InitStaticField(float val);
  void InitNullStaticField();

public:
  
  /// constructField - Create a new field.
  ///
  void initialise(Class* cl, const UTF8* name, const UTF8* type, uint16 access);

  /// ~JavaField - Destroy the field as well as its attributes.
  ///
  ~JavaField();

  /// access - The Java access type of this field (e.g. public, private).
  ///
  uint16 access;

  /// name - The name of the field.
  ///
  const UTF8* name;

  /// type - The UTF8 type name of the field.
  ///
  const UTF8* type;

  /// attributs - List of Java attributs for this field.
  ///
  Attribut* attributs;
  
  /// nbAttributs - The number of attributes.
  ///
  uint16 nbAttributs;

  /// classDef - The class where the field is defined.
  ///
  Class* classDef;

  /// ptrOffset - The offset of the field when the object containing
  /// the field is casted to an array of bytes.
  ///
  uint32 ptrOffset;
  
  /// num - The index of the field in the field list.
  ///
  uint16 num;
  
  /// getSignature - Get the signature of this field, resolving it if
  /// necessary.
  ///
  Typedef* getSignature();

  /// InitStaticField - Init the value of the field in the given object. This is
  /// used for static fields which have a default value.
  ///
  void InitStaticField();

  /// lookupAttribut - Look up the attribut in the field's list of attributs.
  ///
  Attribut* lookupAttribut(const UTF8* key);

  bool isReference();
  bool isDouble();
  bool isLong();
  bool isInt();
  bool isFloat();
  bool isShort();
  bool isChar();
  bool isByte();
  bool isBoolean();

};


} // end namespace j3

#endif
