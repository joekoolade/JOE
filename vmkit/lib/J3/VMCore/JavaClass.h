//===-------- JavaClass.h - Java class representation -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_CLASS_H
#define JNJVM_JAVA_CLASS_H


#include "types.h"

#include "mvm/Allocator.h"
#include "mvm/MethodInfo.h"
#include "mvm/Threads/Cond.h"
#include "mvm/Threads/Locks.h"

#include "JavaAccess.h"
#include "JavaObject.h"
#include "JnjvmClassLoader.h"
#include "JnjvmConfig.h"

#include <cassert>
#include <set>

namespace j3 {

class ArrayObject;
class ArrayUInt8;
class ArrayUInt16;
class Class;
class ClassArray;
class ClassBytes;
class JavaArray;
class JavaConstantPool;
class JavaField;
class JavaMethod;
class JavaObject;
class JavaVirtualTable;
class Reader;
class Signdef;
class Typedef;


/// JavaState - List of states a Java class can have. A class is ready to be
/// used (i.e allocating instances of the class, calling methods of the class
/// and accessing static fields of the class) when it is in the ready state.
///
#define loaded 0       /// The .class file has been found.
#define resolving 1    /// The .class file is being resolved.
#define resolved 2     /// The class has been resolved.
#define vmjc 3         /// The class is defined in a shared library.
#define inClinit 4     /// The class is cliniting.
#define ready 5        /// The class is ready to be used.
#define erroneous 6    /// The class is in an erroneous state.


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
class Attribut : public mvm::PermanentObject {
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

/// TaskClassMirror - The isolate specific class information: the initialization
/// state and the static instance. In a non-isolate environment, there is only
/// one instance of a TaskClassMirror per Class.
class TaskClassMirror {
public:
  
  /// status - The state.
  ///
  uint8 status;

  /// initialized - Is the class initialized?
  bool initialized;

  /// staticInstance - Memory that holds the static variables of the class.
  ///
  void* staticInstance;
};

/// CommonClass - This class is the root class of all Java classes. It is
/// GC-allocated because CommonClasses have to be traceable. A java/lang/Class
/// object that stays in memory has a reference to the class. Same for
/// super or interfaces.
///
class CommonClass : public mvm::PermanentObject {

public:
  
//===----------------------------------------------------------------------===//
//
// If you want to add new fields or modify the types of fields, you must also
// change their LLVM representation in LLVMRuntime/runtime-*.ll, and their
// offsets in JnjvmModule.cpp.
//
//===----------------------------------------------------------------------===//
 
  /// delegatees - The java/lang/Class delegatee.
  ///
  JavaObject* delegatee[NR_ISOLATES];

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
   
  /// classLoader - The Jnjvm class loader that loaded the class.
  ///
  JnjvmClassLoader* classLoader;
  
  /// virtualVT - The virtual table of instances of this class.
  ///
  JavaVirtualTable* virtualVT;
  

//===----------------------------------------------------------------------===//
//
// End field declaration.
//
//===----------------------------------------------------------------------===//

  bool isSecondaryClass() {
    return virtualVT->offset == JavaVirtualTable::getCacheIndex();
  }

  // Assessor methods.
  uint32 getAccess() const      { return access & 0xFFFF; }
  Class** getInterfaces() const { return interfaces; }
  const UTF8* getName() const   { return name; }
  Class* getSuper() const       { return super; }
  
  /// isArray - Is the class an array class?
  ///
  bool isArray() const {
    return j3::isArray(access);
  }
  
  /// isPrimitive - Is the class a primitive class?
  ///
  bool isPrimitive() const {
    return j3::isPrimitive(access);
  }
  
  /// isInterface - Is the class an interface?
  ///
  bool isInterface() const {
    return j3::isInterface(access);
  }
  
  /// isClass - Is the class a real, instantiable class?
  ///
  bool isClass() const {
    return j3::isClass(access);
  }

  /// asClass - Returns the class as a user-defined class
  /// if it is not a primitive or an array.
  ///
  UserClass* asClass() {
    if (isClass()) return (UserClass*)this;
    return 0;
  }
  
  /// asClass - Returns the class as a user-defined class
  /// if it is not a primitive or an array.
  ///
  const UserClass* asClass() const {
    if (isClass()) return (const UserClass*)this;
    return 0;
  }
  
  /// asPrimitiveClass - Returns the class if it's a primitive class.
  ///
  UserClassPrimitive* asPrimitiveClass() {
    if (isPrimitive()) return (UserClassPrimitive*)this;
    return 0;
  }
  
  const UserClassPrimitive* asPrimitiveClass() const {
    if (isPrimitive()) return (const UserClassPrimitive*)this;
    return 0;
  }
  
  /// asArrayClass - Returns the class if it's an array class.
  ///
  UserClassArray* asArrayClass() {
    if (isArray()) return (UserClassArray*)this;
    return 0;
  }
  
  const UserClassArray* asArrayClass() const {
    if (isArray()) return (const UserClassArray*)this;
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

  /// getClassDelegatee - Return the java/lang/Class representation of this
  /// class.
  ///
  JavaObject* getClassDelegatee(Jnjvm* vm, JavaObject* pd = NULL);
  
  /// getClassDelegateePtr - Return a pointer on the java/lang/Class
  /// representation of this class. Used for JNI.
  ///
  JavaObject* const* getClassDelegateePtr(Jnjvm* vm, JavaObject* pd = NULL);
  
  /// CommonClass - Create a class with th given name.
  ///
  CommonClass(JnjvmClassLoader* loader, const UTF8* name);
  
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
  UserClassPrimitive* toPrimitive(Jnjvm* vm) const;
 
  /// getInternal - Return the class.
  ///
  CommonClass* getInternal() {
    return this;
  }
 
  /// setDelegatee - Set the java/lang/Class object of this class.
  ///
  JavaObject* setDelegatee(JavaObject* val);

  /// getDelegatee - Get the java/lang/Class object representing this class.
  ///
  JavaObject* getDelegatee() const {
    return delegatee[0];
  }
  
  /// getDelegatee - Get a pointer on the java/lang/Class object
  /// representing this class.
  ///
  JavaObject* const* getDelegateePtr() const {
    return delegatee;
  }

  /// resolvedImplClass - Return the internal representation of the
  /// java.lang.Class object. The class must be resolved.
  //
  static UserCommonClass* resolvedImplClass(Jnjvm* vm, JavaObject* delegatee,
                                            bool doClinit);
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
  ClassPrimitive(JnjvmClassLoader* loader, const UTF8* name, uint32 nb);

  /// byteIdToPrimitive - Get the primitive class from its byte representation,
  /// ie int for I.
  ///
  static UserClassPrimitive* byteIdToPrimitive(char id, Classpath* upcalls);
  
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

  /// IsolateInfo - Per isolate informations for static instances and
  /// initialization state.
  ///
  TaskClassMirror IsolateInfo[NR_ISOLATES];
   
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
  
  /// ownerClass - Who is initializing this class.
  ///
  mvm::Thread* ownerClass;
  
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

  /// getOwnerClass - Get the thread that is currently initializing the class.
  ///
  mvm::Thread* getOwnerClass() const {
    return ownerClass;
  }

  /// setOwnerClass - Set the thread that is currently initializing the class.
  ///
  void setOwnerClass(mvm::Thread* th) {
    ownerClass = th;
  }
 
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
  
  /// doNew - Allocates a Java object whose class is this class.
  ///
  JavaObject* doNew(Jnjvm* vm);
  
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
  void* allocateStaticInstance(Jnjvm* vm);
  
  /// Class - Create a class in the given virtual machine and with the given
  /// name.
  Class(JnjvmClassLoader* loader, const UTF8* name, ClassBytes* bytes);
  
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
  void initialiseClass(Jnjvm* vm);
  
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
  
  /// getCurrentTaskClassMirror - Get the class task mirror of the executing
  /// isolate.
  ///
  TaskClassMirror& getCurrentTaskClassMirror() {
    return IsolateInfo[0];
  }
  
  /// isReadyForCompilation - Can this class be inlined when JITing?
  ///
  bool isReadyForCompilation() {
    return isReady();
  }
  
  /// setResolved - Set the status of the class as resolved.
  ///
  void setResolved() {
    getCurrentTaskClassMirror().status = resolved;
  }
  
  /// setErroneous - Set the class as erroneous.
  ///
  void setErroneous() {
    getCurrentTaskClassMirror().status = erroneous;
  }
  
  /// setIsResolving - The class file is being resolved.
  ///
  void setIsResolving() {
    getCurrentTaskClassMirror().status = resolving;
  }
  
  /// getStaticInstance - Get the memory that holds static variables.
  ///
  void* getStaticInstance() {
    return getCurrentTaskClassMirror().staticInstance;
  }
  
  /// setStaticInstance - Set the memory that holds static variables.
  ///
  void setStaticInstance(void* val) {
    assert(getCurrentTaskClassMirror().staticInstance == NULL);
    getCurrentTaskClassMirror().staticInstance = val;
  }
  
  /// getInitializationState - Get the state of the class.
  ///
  uint8 getInitializationState() {
    return getCurrentTaskClassMirror().status;
  }

  /// setInitializationState - Set the state of the class.
  ///
  void setInitializationState(uint8 st) {
    TaskClassMirror& TCM = getCurrentTaskClassMirror();
    TCM.status = st;
    if (st == ready) TCM.initialized = true;
  }
  
  /// isReady - Has this class been initialized?
  ///
  bool isReady() {
    return getCurrentTaskClassMirror().status == ready;
  }
  
  /// isInitializing - Is the class currently being initialized?
  ///
  bool isInitializing() {
    return getCurrentTaskClassMirror().status >= inClinit;
  }
  
  /// isResolved - Has this class been resolved?
  ///
  bool isResolved() {
    uint8 stat = getCurrentTaskClassMirror().status;
    return (stat >= resolved && stat != erroneous);
  }
  
  /// isErroneous - Is the class in an erroneous state.
  ///
  bool isErroneous() {
    return getCurrentTaskClassMirror().status == erroneous;
  }

  /// isResolving - Is the class currently being resolved?
  ///
  bool isResolving() {
    return getCurrentTaskClassMirror().status == resolving;
  }

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

  /// doNew - Allocate a new array in the given vm.
  ///
  JavaObject* doNew(sint32 n, Jnjvm* vm);

  /// ClassArray - Construct a Java array class with the given name.
  ///
  ClassArray(JnjvmClassLoader* loader, const UTF8* name,
             UserCommonClass* baseClass);
  
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

/// JavaMethod - This class represents Java methods.
///
class JavaMethod : public mvm::PermanentObject {
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
  uint16 lookupLineNumber(mvm::FrameInfo* FI);
  
  /// lookupCtpIndex - Lookup the constant pool index pointed by the opcode
  /// related to the given frame info.
  ///
  uint16 lookupCtpIndex(mvm::FrameInfo* FI);
  
  /// getSignature - Get the signature of thes method, resolving it if
  /// necessary.
  ///
  Signdef* getSignature() {
    if(!_signature)
      _signature = classDef->classLoader->constructSign(type);
    return _signature;
  }
  
  /// toString - Return an array of chars, suitable for creating a string.
  ///
  ArrayUInt16* toString() const;
  
  /// jniConsFromMeth - Construct the JNI name of this method as if
  /// there is no other function in the class with the same name.
  ///
  void jniConsFromMeth(char* buf) const {
    jniConsFromMeth(buf, classDef->name, name, type, isSynthetic(access));
  }

  /// jniConsFromMethOverloaded - Construct the JNI name of this method
  /// as if its name is overloaded in the class.
  ///
  void jniConsFromMethOverloaded(char* buf) const {
    jniConsFromMethOverloaded(buf, classDef->name, name, type,
                              isSynthetic(access));
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
  ArrayObject* getParameterTypes(JnjvmClassLoader* loader);

  /// getExceptionTypes - Get the java.lang.Class of the exceptions of the
  /// method, with the given class loader.
  ///
  ArrayObject* getExceptionTypes(JnjvmClassLoader* loader);

  /// getReturnType - Get the java.lang.Class of the result of the method,
  /// with the given class loader.
  ///
  JavaObject* getReturnType(JnjvmClassLoader* loader);
  

//===----------------------------------------------------------------------===//
//
// Upcalls from JnJVM code to Java code. 
//
//===----------------------------------------------------------------------===//
  
  /// This class of methods takes a variable argument list.
  uint32 invokeIntSpecialAP(Jnjvm* vm, UserClass*, JavaObject* obj, va_list ap)
    __attribute__ ((noinline));
  float invokeFloatSpecialAP(Jnjvm* vm, UserClass*, JavaObject* obj, va_list ap)
    __attribute__ ((noinline));
  double invokeDoubleSpecialAP(Jnjvm* vm, UserClass*, JavaObject* obj,
                               va_list ap) __attribute__ ((noinline));
  sint64 invokeLongSpecialAP(Jnjvm* vm, UserClass*, JavaObject* obj, va_list ap)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectSpecialAP(Jnjvm* vm, UserClass*, JavaObject* obj,
                                        va_list ap) __attribute__ ((noinline));
  
  uint32 invokeIntVirtualAP(Jnjvm* vm, UserClass*, JavaObject* obj, va_list ap)
    __attribute__ ((noinline));
  float invokeFloatVirtualAP(Jnjvm* vm, UserClass*, JavaObject* obj, va_list ap)
    __attribute__ ((noinline));
  double invokeDoubleVirtualAP(Jnjvm* vm, UserClass*, JavaObject* obj,
                               va_list ap) __attribute__ ((noinline));
  sint64 invokeLongVirtualAP(Jnjvm* vm, UserClass*, JavaObject* obj, va_list ap)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectVirtualAP(Jnjvm* vm, UserClass*, JavaObject* obj,
                                        va_list ap) __attribute__ ((noinline));
  
  uint32 invokeIntStaticAP(Jnjvm* vm, UserClass*, va_list ap)
    __attribute__ ((noinline));
  float invokeFloatStaticAP(Jnjvm* vm, UserClass*, va_list ap)
    __attribute__ ((noinline));
  double invokeDoubleStaticAP(Jnjvm* vm, UserClass*, va_list ap)
    __attribute__ ((noinline));
  sint64 invokeLongStaticAP(Jnjvm* vm, UserClass*, va_list ap)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectStaticAP(Jnjvm* vm, UserClass*, va_list ap)
    __attribute__ ((noinline));

  /// This class of methods takes a buffer which contain the arguments of the
  /// call.
  uint32 invokeIntSpecialBuf(Jnjvm* vm, UserClass*, JavaObject* obj, void* buf)
    __attribute__ ((noinline));
  float invokeFloatSpecialBuf(Jnjvm* vm, UserClass*, JavaObject* obj, void* buf)
    __attribute__ ((noinline));
  double invokeDoubleSpecialBuf(Jnjvm* vm, UserClass*, JavaObject* obj,
                                void* buf) __attribute__ ((noinline));
  sint64 invokeLongSpecialBuf(Jnjvm* vm, UserClass*, JavaObject* obj, void* buf)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectSpecialBuf(Jnjvm* vm, UserClass*, JavaObject* obj,
                                         void* buf) __attribute__ ((noinline));
  
  uint32 invokeIntVirtualBuf(Jnjvm* vm, UserClass*, JavaObject* obj, void* buf)
    __attribute__ ((noinline));
  float invokeFloatVirtualBuf(Jnjvm* vm, UserClass*, JavaObject* obj, void* buf)
    __attribute__ ((noinline));
  double invokeDoubleVirtualBuf(Jnjvm* vm, UserClass*, JavaObject* obj,
                                void* buf) __attribute__ ((noinline));
  sint64 invokeLongVirtualBuf(Jnjvm* vm, UserClass*, JavaObject* obj, void* buf)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectVirtualBuf(Jnjvm* vm, UserClass*, JavaObject* obj,
                                         void* buf) __attribute__ ((noinline));
  
  uint32 invokeIntStaticBuf(Jnjvm* vm, UserClass*, void* buf)
    __attribute__ ((noinline));
  float invokeFloatStaticBuf(Jnjvm* vm, UserClass*, void* buf)
    __attribute__ ((noinline));
  double invokeDoubleStaticBuf(Jnjvm* vm, UserClass*, void* buf)
    __attribute__ ((noinline));
  sint64 invokeLongStaticBuf(Jnjvm* vm, UserClass*, void* buf)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectStaticBuf(Jnjvm* vm, UserClass*, void* buf)
    __attribute__ ((noinline));

  /// This class of methods is variadic.
  uint32 invokeIntSpecial(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  float invokeFloatSpecial(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  double invokeDoubleSpecial(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  sint64 invokeLongSpecial(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectSpecial(Jnjvm* vm, UserClass*, JavaObject* obj,
                                      ...) __attribute__ ((noinline));
  
  uint32 invokeIntVirtual(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  float invokeFloatVirtual(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  double invokeDoubleVirtual(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  sint64 invokeLongVirtual(Jnjvm* vm, UserClass*, JavaObject* obj, ...)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectVirtual(Jnjvm* vm, UserClass*, JavaObject* obj,
                                      ...) __attribute__ ((noinline));
  
  uint32 invokeIntStatic(Jnjvm* vm, UserClass*, ...)
    __attribute__ ((noinline));
  float invokeFloatStatic(Jnjvm* vm, UserClass*, ...)
    __attribute__ ((noinline));
  double invokeDoubleStatic(Jnjvm* vm, UserClass*, ...)
    __attribute__ ((noinline));
  sint64 invokeLongStatic(Jnjvm* vm, UserClass*, ...)
    __attribute__ ((noinline));
  JavaObject* invokeJavaObjectStatic(Jnjvm* vm, UserClass*, ...)
    __attribute__ ((noinline));
  
  #define JNI_NAME_PRE "Java_"
  #define JNI_NAME_PRE_LEN 5
  
};

/// JavaField - This class represents a Java field.
///
class JavaField  : public mvm::PermanentObject {
private:
  /// _signature - The signature of the field. Null if not resolved.
  ///
  Typedef* _signature;
  
  /// InitField - Set an initial value to the field.
  ///
  void InitStaticField(uint64 val);
  void InitStaticField(JavaObject* val);
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
  Typedef* getSignature() {
    if(!_signature)
      _signature = classDef->classLoader->constructType(type);
    return _signature;
  }

  /// InitStaticField - Init the value of the field in the given object. This is
  /// used for static fields which have a default value.
  ///
  void InitStaticField(Jnjvm* vm);

  /// lookupAttribut - Look up the attribut in the field's list of attributs.
  ///
  Attribut* lookupAttribut(const UTF8* key);

  JavaObject** getStaticObjectFieldPtr() {
    assert(classDef->getStaticInstance());
    return (JavaObject**)((uint64)classDef->getStaticInstance() + ptrOffset);
  }

  JavaObject** getInstanceObjectFieldPtr(JavaObject* obj) {
    llvm_gcroot(obj, 0);
    return (JavaObject**)((uint64)obj + ptrOffset);
  }

  /// getStatic*Field - Get a static field.
  ///
  #define GETSTATICFIELD(TYPE, TYPE_NAME)                                   \
  TYPE getStatic##TYPE_NAME##Field() {                                      \
    assert(classDef->isResolved());                                         \
    void* ptr = (void*)((uint64)classDef->getStaticInstance() + ptrOffset); \
    return ((TYPE*)ptr)[0];                                                 \
  }

  /// setStatic*Field - Set a field of an object.
  ///
  #define SETSTATICFIELD(TYPE, TYPE_NAME)                                   \
  void setStatic##TYPE_NAME##Field(TYPE val) {                              \
    assert(classDef->isResolved());                                         \
    void* ptr = (void*)((uint64)classDef->getStaticInstance() + ptrOffset); \
    ((TYPE*)ptr)[0] = val;                                                  \
  }

  /// getInstance*Field - Get an instance field.
  ///
  #define GETINSTANCEFIELD(TYPE, TYPE_NAME)                                 \
  TYPE getInstance##TYPE_NAME##Field(JavaObject* obj) {                     \
    llvm_gcroot(obj, 0);                                                    \
    assert(classDef->isResolved());                                         \
    void* ptr = (void*)((uint64)obj + ptrOffset);                           \
    return ((TYPE*)ptr)[0];                                                 \
  }                                                                         \

  /// setInstance*Field - Set an instance field.
  ///
  #define SETINSTANCEFIELD(TYPE, TYPE_NAME)                                 \
  void setInstance##TYPE_NAME##Field(JavaObject* obj, TYPE val) {           \
    llvm_gcroot(obj, 0);                                                    \
    assert(classDef->isResolved());                                         \
    void* ptr = (void*)((uint64)obj + ptrOffset);                           \
    ((TYPE*)ptr)[0] = val;                                                  \
  }

  #define MK_ASSESSORS(TYPE, TYPE_NAME)                                     \
    GETSTATICFIELD(TYPE, TYPE_NAME)                                         \
    SETSTATICFIELD(TYPE, TYPE_NAME)                                         \
    GETINSTANCEFIELD(TYPE, TYPE_NAME)                                       \
    SETINSTANCEFIELD(TYPE, TYPE_NAME)                                       \

  MK_ASSESSORS(float, Float);
  MK_ASSESSORS(double, Double);
  MK_ASSESSORS(uint8, Int8);
  MK_ASSESSORS(uint16, Int16);
  MK_ASSESSORS(uint32, Int32);
  MK_ASSESSORS(sint64, Long);

  JavaObject* getStaticObjectField() {
    assert(classDef->isResolved());
    void* ptr = (void*)((uint64)classDef->getStaticInstance() + ptrOffset);
    return ((JavaObject**)ptr)[0];
  }

  void setStaticObjectField(JavaObject* val);

  JavaObject* getInstanceObjectField(JavaObject* obj) {
    llvm_gcroot(obj, 0);
    assert(classDef->isResolved());
    void* ptr = (void*)((uint64)obj + ptrOffset);
    return ((JavaObject**)ptr)[0];
  }

  // This can't be inlined because of a linker bug.
  void setInstanceObjectField(JavaObject* obj, JavaObject* val);
  
  bool isReference() {
    uint16 val = type->elements[0];
    return (val == '[' || val == 'L');
  }
  
  bool isDouble() {
    return (type->elements[0] == 'D');
  }

  bool isLong() {
    return (type->elements[0] == 'J');
  }

  bool isInt() {
    return (type->elements[0] == 'I');
  }

  bool isFloat() {
    return (type->elements[0] == 'F');
  }

  bool isShort() {
    return (type->elements[0] == 'S');
  }

  bool isChar() {
    return (type->elements[0] == 'C');
  }

  bool isByte() {
    return (type->elements[0] == 'B');
  }

  bool isBoolean() {
    return (type->elements[0] == 'Z');
  }

};


} // end namespace j3

#endif
