//===--- JavaConstantPool.h - Java constant pool definition ---------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_CONSTANT_POOL_H
#define JNJVM_JAVA_CONSTANT_POOL_H

#include "mvm/Allocator.h"
#include "types.h"

#include "UTF8.h"

namespace j3 {

class Class;
class CommonClass;
class JavaField;
class JavaMethod;
class JavaString;
class Reader;
class Signdef;
class Typedef;

/// JavaConstantPool - This class represents a Java constant pool, a place where
/// a Java class makes external references such as classes and methods and
/// stores constants such as integers or UTF8s.
class JavaConstantPool : public mvm::PermanentObject {
public:
  
  /// classDef - The owning class of this constant pool.
  ///
  Class*  classDef;
  
  /// ctpSize - The number of entries in the constant pool.
  ///
  uint32 ctpSize;

  /// ctpType - The types of the constant pool entries.
  ///
  uint8*  ctpType;

  /// ctpDef - The values of the constant pool entries: may be constants or
  /// references to other entries.
  ///
  sint32* ctpDef;
  
  /// ctpRes - Objects resolved dynamically, e.g. UTF8s, classes, methods,
  /// fields, string pointers.
  ///
  void**  ctpRes;
  
  /// operator new - Redefine the operator to allocate the arrays of a
  /// constant pool inline.
  void* operator new(size_t sz, mvm::BumpPtrAllocator& allocator,
                     uint32 ctpSize);

  /// CtpReaderClass - Reads a class entry.
  static uint32 CtpReaderClass(JavaConstantPool* ctp, Reader& reader,
                               uint32 index);
  
  /// CtpReaderInteger - Reads an integer entry.
  static uint32 CtpReaderInteger(JavaConstantPool* ctp, Reader& reader,
                                 uint32 index);
  
  /// CtpReaderFloat - Reads a float entry.
  static uint32 CtpReaderFloat(JavaConstantPool* ctp, Reader& reader,
                               uint32 index);
  
  /// CtpReaderClass - Reads an UTF8 entry.
  static uint32 CtpReaderUTF8(JavaConstantPool* ctp, Reader& reader,
                              uint32 index);
  
  /// CtpReaderNameAndType - Reads a name/signature entry.
  static uint32 CtpReaderNameAndType(JavaConstantPool* ctp, Reader& reader,
                                     uint32 index);
  
  /// CtpReaderFieldref - Reads a field entry.
  static uint32 CtpReaderFieldref(JavaConstantPool* ctp, Reader& reader,
                                  uint32 index);
  
  /// CtpReaderString - Reads a string entry.
  static uint32 CtpReaderString(JavaConstantPool* ctp, Reader& reader,
                                uint32 index);
  
  /// CtpReaderMethodref - Reads a method entry.
  static uint32 CtpReaderMethodref(JavaConstantPool* ctp, Reader& reader,
                                   uint32 index);
  
  /// CtpReaderInterfaceMethodref - Reads a method of an interface entry.
  static uint32 CtpReaderInterfaceMethodref(JavaConstantPool* ctp,
                                            Reader& reader,
                                            uint32 index);
   
  /// CtpReaderLong - Reads a long entry.
  static uint32 CtpReaderLong(JavaConstantPool* ctp, Reader& reader,
                              uint32 index);
  
  /// CtpReaderClass - Reads a double entry.
  static uint32 CtpReaderDouble(JavaConstantPool* ctp, Reader& reader,
                                uint32 index);

  static const uint32 ConstantUTF8;
  static const uint32 ConstantInteger;
  static const uint32 ConstantFloat;
  static const uint32 ConstantLong;
  static const uint32 ConstantDouble;
  static const uint32 ConstantClass;
  static const uint32 ConstantString;
  static const uint32 ConstantFieldref;
  static const uint32 ConstantMethodref;
  static const uint32 ConstantInterfaceMethodref;
  static const uint32 ConstantNameAndType;

  typedef uint32 (*ctpReader)(JavaConstantPool*, Reader&, uint32);

  /// funcsReader - Array of CtpReader* functions.
  ///
  static ctpReader funcsReader[16];

  /// isAStaticCall - Is the name/type at the given entry a reference to a
  /// static method?
  bool isAStaticCall(uint32 index) {
    return (ctpType[index] & 0x80) != 0;    
  }
  
  /// markAsStaticCall - Set the name/type entry as a reference to a static
  /// method.
  void markAsStaticCall(uint32 index) {
    ctpType[index] |= 0x80;
  }

  /// typeAt - Get the constant pool type of the given entry.
  ///
  uint8 typeAt(uint32 index) {
    return ctpType[index] & 0x7F;    
  }
  
  /// UTF8At - Get the UTF8 at the given entry.
  ///
  const UTF8* UTF8At(uint32 entry);
  
  /// UTF8At - Get the UTF8 referenced from this string entry.
  ///
  const UTF8* UTF8AtForString(uint32 entry) {
    return UTF8At(ctpDef[entry]);
  }

  /// FloatAt - Get the float at the given entry.
  ///
  float FloatAt(uint32 entry);

  /// IntegerAt - Get the int at the given entry.
  ///
  sint32 IntegerAt(uint32 entry);

  /// LongAt - Get the long at the given entry.
  ///
  sint64 LongAt(uint32 entry);

  /// DoubleAt - Get the double at the given entry.
  ///
  double DoubleAt(uint32 entry);

  /// isClassLoaded - Is the class at the given entry already loaded?
  ///
  CommonClass* isClassLoaded(uint32 index);

  /// resolveClassName - Get the name of the class referenced and returns
  /// it.
  ///
  const UTF8* resolveClassName(uint32 index);
 
  /// resolveNameAndType - Resolve the name/type at the given index,
  /// and returns the type.
  Typedef* resolveNameAndType(uint32 index);

  /// resolveNameAndSign - Resolve the name/sign at the given index,
  /// and returns the signature.
  Signdef* resolveNameAndSign(uint32 index);

  /// infoOfInterfaceOrVirtualMethod - Get the signature of the method
  /// referenced at the given entry.
  Signdef* infoOfInterfaceOrVirtualMethod(uint32 index, const UTF8*& name);

  /// infoOfStaticOrSpecialMethod - Get the JavaMethod of a non-virtual
  /// method. Return null if not loaded yet.
  ///
  JavaMethod* infoOfStaticOrSpecialMethod(uint32 index, uint32 access,
                                          Signdef* sign);
  
  
  /// nameOfStaticOrSpecialMethod - Get the name and the signature
  /// of a non-virtual method.
  ///
  void nameOfStaticOrSpecialMethod(uint32 index, const UTF8*& cl, 
                                   const UTF8*& name, Signdef*& sign);
  
  /// getClassIndexFromMethod - Get the entry of the class that owns
  /// the referenced method.
  ///
  uint32 getClassIndexFromMethod(uint32 index);

  /// getMethodClassIfLoaded - Returns the class of the given method if
  /// loaded, null if not.
  CommonClass* getMethodClassIfLoaded(uint32 index);
 
  /// infoOfField - Get the Typedef representation of the field referenced
  /// at the given entry. This does not involve any class loading.
  ///
  Typedef* infoOfField(uint32 index);
  
  /// infoOfMethod - Get the signature of the method referenced at the given
  /// entry and try to find the method. This does not involve any class
  /// loading.
  ///
  void infoOfMethod(uint32 index, uint32 access, CommonClass*& cl,
                    JavaMethod*& meth); 
 
  /// lookupField - Lookup the field at the given entry.
  ///
  JavaField* lookupField(uint32 index, bool stat);
  
  /// resolveString - Get the string referenced at the given
  /// index from the UTF8.
  JavaString* resolveString(const UTF8* utf8, uint16 index);
  
  /// resolveMethod - Resolve the class and the signature of the method. May
  /// perform class loading. This function is called just in time, ie when
  /// the method call is actually made and not yet resolved.
  ///
  void resolveMethod(uint32 index, CommonClass*& cl,
                     const UTF8*& utf8, Signdef*& sign);
  
  /// resolveField - Resolve the class and signature of the field. May
  /// perform class loading. This function is called just in time, ie when
  /// the field is accessed and not yet resolved.
  ///
  void resolveField(uint32 index, CommonClass*& cl, const UTF8*& utf8,
                    Typedef*& sign);
  
  /// loadClass - Loads the class and returns it. This is called just in time, 
  /// ie when the class will be used and not yet resolved, and also for
  /// loading exceptions when JITting catch clauses.
  ///
  CommonClass* loadClass(uint32 index, bool resolve = true);

  /// JavaConstantPool - Reads the bytecode of the class to get
  /// the initial types and constants definitions.
  ///
  JavaConstantPool(Class*, Reader& reader, uint32 ctpSize);

  /// ~JavaConstantPool - Delete the constant pool.
  ///
  ~JavaConstantPool() {}
};

} // end namespace j3

#endif
