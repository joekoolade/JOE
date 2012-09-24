//===---==---- JavaLLVMCompiler.h - A LLVM Compiler for J3 ----------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef J3_LLVM_COMPILER_H
#define J3_LLVM_COMPILER_H

#include "j3/JavaCompiler.h"
#include "j3/J3Intrinsics.h"
#include "j3/LLVMInfo.h"

#include "llvm/ADT/DenseMap.h"
#include "llvm/LLVMContext.h"
#include "llvm/Module.h"

#include <map>

namespace llvm {
  class BasicBlock;
  class DIBuilder;
}

namespace j3 {

class CommonClass;
class Class;
class ClassArray;
class ClassPrimitive;
class JavaConstantPool;
class JavaField;
class JavaMethod;
class JavaObject;
class JavaString;
class JavaVirtualTable;
class Jnjvm;
class Typedef;
class Signdef;

class JavaLLVMCompiler : public JavaCompiler {
  friend class JavaAOTCompiler;
  friend class LLVMClassInfo;
  friend class LLVMMethodInfo;
  friend class LLVMSignatureInfo;

protected:
  llvm::Module* TheModule;
  llvm::DIBuilder* DebugFactory;  
  J3Intrinsics JavaIntrinsics;
  const llvm::TargetData* TheTargetData;

private:  
  bool enabledException;
  bool cooperativeGC;
  
  virtual void makeVT(Class* cl) = 0;
  virtual void makeIMT(Class* cl) = 0;
  
  std::map<const llvm::Function*, JavaMethod*> functions;
  typedef std::map<const llvm::Function*, JavaMethod*>::iterator function_iterator;
  
  std::map<JavaMethod*, LLVMMethodInfo*> method_infos;
  typedef std::map<JavaMethod*, LLVMMethodInfo*>::iterator method_info_iterator;
  
  std::map<JavaField*, LLVMFieldInfo*> field_infos;
  typedef std::map<JavaField*, LLVMFieldInfo*>::iterator field_info_iterator;
  
  std::map<Signdef*, LLVMSignatureInfo*> signature_infos;
  typedef std::map<Signdef*, LLVMSignatureInfo*>::iterator signature_info_iterator;
  
  std::map<Class*, LLVMClassInfo*> class_infos;
  typedef std::map<Class*, LLVMClassInfo*>::iterator class_info_iterator;

  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> virtualStubs;
  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> specialStubs;
  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> staticStubs;
  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> virtualBufs;
  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> staticBufs;
  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> virtualAPs;
  llvm::DenseMap<llvm::FunctionType*, llvm::Function*> staticAPs;

public:
  JavaLLVMCompiler(const std::string &ModuleID);
  
  virtual bool isStaticCompiling() = 0;
  virtual bool emitFunctionName() = 0;
  virtual void* GenerateStub(llvm::Function* F) = 0;
  void addJavaPasses();
  
  llvm::DIBuilder* getDebugFactory() {
    return DebugFactory;
  }

  llvm::Module* getLLVMModule() {
    return TheModule;
  }
  
  llvm::LLVMContext& getLLVMContext() {
    return TheModule->getContext();
  }

  J3Intrinsics* getIntrinsics() {
    return &JavaIntrinsics;
  }

  bool hasExceptionsEnabled() {
    return enabledException;
  }

  bool useCooperativeGC() {
    return cooperativeGC;
  }
  
  void disableExceptions() {
    enabledException = false;
  }
  
  void disableCooperativeGC() {
    cooperativeGC = false;
  }
 
  virtual JavaCompiler* Create(const std::string& ModuleID) = 0;
  
  virtual ~JavaLLVMCompiler();

  JavaMethod* getJavaMethod(const llvm::Function&);

  void resolveVirtualClass(Class* cl);
  void resolveStaticClass(Class* cl);
  virtual llvm::Function* getMethod(JavaMethod* meth, Class* customizeFor);

  void initialiseAssessorInfo();
  std::map<const char, LLVMAssessorInfo> AssessorInfo;
  LLVMAssessorInfo& getTypedefInfo(const Typedef* type);

  LLVMSignatureInfo* getSignatureInfo(Signdef* sign) {
    signature_info_iterator E = signature_infos.end();
    signature_info_iterator I = signature_infos.find(sign);
    if (I == E) {
      LLVMSignatureInfo* signInfo =
        new(allocator, "LLVMSignatureInfo") LLVMSignatureInfo(sign, this);
      signature_infos.insert(std::make_pair(sign, signInfo));
      return signInfo;
    } else {
      return I->second;
    }
  }
  
  LLVMFieldInfo* getFieldInfo(JavaField* field) {
    field_info_iterator E = field_infos.end();
    field_info_iterator I = field_infos.find(field);
    if (I == E) {
      LLVMFieldInfo* fieldInfo =
        new(allocator, "LLVMFieldInfo") LLVMFieldInfo(field, this);
      field_infos.insert(std::make_pair(field, fieldInfo));
      return fieldInfo;
    } else {
      return I->second;
    }
  }
  
  LLVMClassInfo* getClassInfo(Class* klass) {
    class_info_iterator E = class_infos.end();
    class_info_iterator I = class_infos.find(klass);
    if (I == E) {
      LLVMClassInfo* classInfo =
        new(allocator, "LLVMClassInfo") LLVMClassInfo(klass, this);
      class_infos.insert(std::make_pair(klass, classInfo));
      return classInfo;
    } else {
      return I->second;
    }
  }
  
  LLVMMethodInfo* getMethodInfo(JavaMethod* method) {
    method_info_iterator E = method_infos.end();
    method_info_iterator I = method_infos.find(method);
    if (I == E) {
      LLVMMethodInfo* methodInfo =
        new(allocator, "LLVMMethodInfo") LLVMMethodInfo(method, this);
      method_infos.insert(std::make_pair(method, methodInfo));
      return methodInfo;
    } else {
      return I->second;
    }
  }

  virtual llvm::Constant* getFinalObject(JavaObject* obj, CommonClass* cl) = 0;
  virtual JavaObject* getFinalObject(llvm::Value* C) = 0;
  virtual llvm::Constant* getNativeClass(CommonClass* cl) = 0;
  virtual llvm::Constant* getJavaClass(CommonClass* cl) = 0;
  virtual llvm::Constant* getJavaClassPtr(CommonClass* cl) = 0;
  virtual llvm::Constant* getStaticInstance(Class* cl) = 0;
  virtual llvm::Constant* getVirtualTable(JavaVirtualTable*) = 0;
  virtual llvm::Constant* getMethodInClass(JavaMethod* meth) = 0;
  
  virtual llvm::Constant* getString(JavaString* str) = 0;
  virtual llvm::Constant* getStringPtr(JavaString** str) = 0;
  virtual llvm::Constant* getResolvedConstantPool(JavaConstantPool* ctp) = 0;
  virtual llvm::Constant* getNativeFunction(JavaMethod* meth, void* natPtr) = 0;
  
  virtual void setMethod(llvm::Function* func, void* ptr, const char* name) = 0;
  
  virtual void* materializeFunction(JavaMethod* meth,
                                    Class* customizeFor) = 0;
  llvm::Function* parseFunction(JavaMethod* meth, Class* customizeFor);
   
  llvm::FunctionPassManager* JavaFunctionPasses;
  llvm::FunctionPassManager* J3FunctionPasses;
  llvm::FunctionPassManager* JavaNativeFunctionPasses;
  
  virtual bool needsCallback(JavaMethod* meth,
                             Class* customizeFor,
                             bool* needsInit) {
    *needsInit = true;
    return meth == NULL;
  }

  virtual llvm::Value* addCallback(Class* cl, uint16 index, Signdef* sign,
                                   bool stat, llvm::BasicBlock* insert) = 0;
  
  virtual void staticCallBuf(Signdef* sign) {
    getSignatureInfo(sign)->getStaticBuf();
  }

  virtual void virtualCallBuf(Signdef* sign) {
    getSignatureInfo(sign)->getVirtualBuf();
  }

  virtual void staticCallAP(Signdef* sign) {
    getSignatureInfo(sign)->getStaticAP();
  }

  virtual void virtualCallAP(Signdef* sign) {
    getSignatureInfo(sign)->getVirtualAP();
  }
  
  virtual void virtualCallStub(Signdef* sign) {
    getSignatureInfo(sign)->getVirtualStub();
  }
  
  virtual void specialCallStub(Signdef* sign) {
    getSignatureInfo(sign)->getSpecialStub();
  }
  
  virtual void staticCallStub(Signdef* sign) {
    getSignatureInfo(sign)->getStaticStub();
  }

  llvm::Function* NativeLoader;
};

} // end namespace j3

#endif
