//===------------- LLVMInfo.h - Compiler info for LLVM --------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef J3_LLVM_INFO_H
#define J3_LLVM_INFO_H

namespace llvm {
  class Constant;
  class Function;
  class FunctionType;
  class GCFunctionInfo;
  class MDNode;
  class Module;
  class Type;
  class Value;
}

namespace j3 {

class Class;
class JavaField;
class JavaLLVMCompiler;
class JavaMethod;
class Signdef;

class LLVMAssessorInfo {
public:
  llvm::Type* llvmType;
  llvm::Type* llvmTypePtr;
  uint8_t logSizeInBytesConstant;
};

class LLVMClassInfo : public mvm::PermanentObject {
  friend class JavaAOTCompiler;
  friend class JavaJITCompiler;
  friend class JavaLLVMCompiler;
private:
  /// Compiler - The compiler for this class info.
  JavaLLVMCompiler* Compiler;
  
  Class* classDef;
  
  /// virtualSizeLLVM - The LLVM constant size of instances of this class.
  llvm::Constant* virtualSizeConstant;

  /// virtualType - The LLVM type of instance of this class.
  llvm::Type * virtualType;

  /// staticType - The LLVM type of the static instance of this class.
  llvm::Type * staticType;

public:
  
  llvm::Value* getVirtualSize();
  llvm::Type* getVirtualType();
  llvm::Type* getStaticType();
  
  LLVMClassInfo(Class* cl, JavaLLVMCompiler* comp) :
    Compiler(comp),
    classDef(cl),
    virtualSizeConstant(0),
    virtualType(0),
    staticType(0) {}

  virtual void clear() {
    virtualType = 0;
    staticType = 0;
    virtualSizeConstant = 0;
  }
};

class LLVMMethodInfo : public mvm::PermanentObject {
private:
  /// Compiler - The compiler for this method info.
  JavaLLVMCompiler* Compiler;

  JavaMethod* methodDef;

  llvm::Function* methodFunction;
  llvm::Constant* offsetConstant;
  llvm::FunctionType* functionType;
  std::map<Class*, llvm::Function*> customizedVersions;
  
public:
  llvm::Function* getMethod(Class* customizeFor);
  llvm::Constant* getOffset();
  llvm::FunctionType* getFunctionType();
  bool isCustomizable;
    
  LLVMMethodInfo(JavaMethod* M, JavaLLVMCompiler* comp) :  Compiler(comp),
    methodDef(M), methodFunction(0), offsetConstant(0), functionType(0),
    isCustomizable(false) {}
 
  virtual void clear() {
    methodFunction = 0;
    offsetConstant = 0;
    functionType = 0;
    customizedVersions.clear();
    isCustomizable = false;
  }

  void setCustomizedVersion(Class* customizeFor, llvm::Function* F);

  friend class JavaAOTCompiler;
};


class LLVMFieldInfo : public mvm::PermanentObject {
private:
  /// Compiler - The compiler for this field info.
  JavaLLVMCompiler* Compiler;
  
  JavaField* fieldDef;
  
  llvm::Constant* offsetConstant;

public:
  llvm::Constant* getOffset();

  LLVMFieldInfo(JavaField* F, JavaLLVMCompiler* comp) :
    Compiler(comp),
    fieldDef(F), 
    offsetConstant(0) {}

  virtual void clear() {
    offsetConstant = 0;
  }
};

class LLVMSignatureInfo : public mvm::PermanentObject {
private:
  /// Compiler - The compiler for this signature info.
  JavaLLVMCompiler* Compiler;
  
  llvm::FunctionType* staticType;
  llvm::FunctionType* virtualType;
  llvm::FunctionType* nativeType;
  
  llvm::FunctionType* virtualBufType;
  llvm::FunctionType* staticBufType;

  llvm::PointerType* staticPtrType;
  llvm::PointerType* virtualPtrType;
  llvm::PointerType* nativePtrType;
  
  llvm::Function* virtualBufFunction;
  llvm::Function* virtualAPFunction;
  llvm::Function* staticBufFunction;
  llvm::Function* staticAPFunction;
  
  llvm::Function* staticStubFunction;
  llvm::Function* specialStubFunction;
  llvm::Function* virtualStubFunction;
  
  Signdef* signature;

  llvm::Function* createFunctionCallBuf(bool virt);
  llvm::Function* createFunctionCallAP(bool virt);
  llvm::Function* createFunctionStub(bool special, bool virt);
   
  
public:
  llvm::FunctionType* getVirtualType();
  llvm::FunctionType* getStaticType();
  llvm::FunctionType* getNativeType();
  llvm::FunctionType* getNativeStubType();

  llvm::FunctionType* getVirtualBufType();
  llvm::FunctionType* getStaticBufType();

  llvm::PointerType*  getStaticPtrType();
  llvm::PointerType*  getNativePtrType();
  llvm::PointerType*  getVirtualPtrType();
   
  llvm::Function* getVirtualBuf();
  llvm::Function* getVirtualAP();
  llvm::Function* getStaticBuf();
  llvm::Function* getStaticAP();
  
  llvm::Function* getStaticStub();
  llvm::Function* getSpecialStub();
  llvm::Function* getVirtualStub();
  
  LLVMSignatureInfo(Signdef* sign, JavaLLVMCompiler* comp) :
    Compiler(comp),
    staticType(0),
    virtualType(0),
    nativeType(0),
    virtualBufType(0),
    staticBufType(0),
    staticPtrType(0),
    virtualPtrType(0),
    nativePtrType(0),
    virtualBufFunction(0),
    virtualAPFunction(0),
    staticBufFunction(0),
    staticAPFunction(0),
    staticStubFunction(0),
    specialStubFunction(0),
    virtualStubFunction(0),
    signature(sign) {}

};

} // end namespace j3

#endif
