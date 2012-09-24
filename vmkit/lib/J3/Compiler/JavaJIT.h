//===----------- JavaJIT.h - Java just in time compiler -------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_JAVA_JIT_H
#define JNJVM_JAVA_JIT_H

#include <vector>
#include <map>

#include "llvm/BasicBlock.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/LLVMContext.h"
#include "llvm/Metadata.h"
#include "llvm/Type.h"
#include "llvm/Value.h"
#include "llvm/Support/DebugLoc.h"
#include "llvm/Analysis/DebugInfo.h"

#include "types.h"

#include "j3/JavaLLVMCompiler.h"

#include "JavaClass.h"
#include "JavaUpcalls.h"

namespace j3 {

class Class;
class JavaMethod;
class Reader;

struct MetaInfo {
  MetaInfo(CommonClass* t, uint8 b) : type(t), bytecode(b) {}
  CommonClass* type;
  uint8 bytecode;
};

/// Opinfo - This class gives for each opcode if it starts a new block and
/// its exception destination.
///
struct Opinfo {

  /// newBlock - If it is non-null, the block that the instruction starts.
  ///
  llvm::BasicBlock* newBlock;

  /// exceptionBlock - Never null, the exception destination of the
  /// instruction.
  ///
  llvm::BasicBlock* exceptionBlock;

  /// handler - If the instruction is the first instruction of a Java exception
  /// handler.
  ///
  bool handler;

  /// stack - The stack at this location if there is a new block
  ///
  std::vector<MetaInfo> stack;
};

/// JavaJIT - The compilation engine of J3. Parses the bycode and returns
/// its LLVM representation.
///
class JavaJIT {
public:
 
  /// JavaJIT - Default constructor.
  JavaJIT(JavaLLVMCompiler* C,
          JavaMethod* meth,
          llvm::Function* func,
          Class* customized) {
    compilingMethod = meth;
    compilingClass = meth->classDef;
    upcalls = compilingClass->classLoader->bootstrapLoader->upcalls;
    TheCompiler = C;
    intrinsics = TheCompiler->getIntrinsics();
    llvmFunction = func;
    llvmContext = &func->getContext();
    inlining = false;
    callsStackWalker = false;
    endNode = 0;
    currentStackIndex = 0;
    currentBytecodeIndex = 0;
    currentBytecode = 0;
    thisObject = NULL;
    customizeFor = customized;
    isCustomizable = false;
    overridesThis = false;
    nbHandlers = 0;
    jmpBuffer = NULL;
  }

  /// javaCompile - Compile the Java method.
  llvm::Function* javaCompile();
  
  /// nativeCompile - Compile the native method.
  llvm::Function* nativeCompile(word_t natPtr = 0);
  
  /// isCustomizable - Whether we found the method to be customizable.
  bool isCustomizable;

  // The number of handlers in that method.
  uint32_t nbHandlers;

private:
  /// Whether the method overrides 'this'.
  bool overridesThis;
  
  /// currentBytecode - The current bytecode being processed.
  uint16 currentBytecode;
  
  /// compilingClass - The class that is defining the method being compiled.
  Class* compilingClass;

  /// compilingMethod - The method being compiled.
  JavaMethod* compilingMethod;

  /// customizeFor - The class we're currently customizing this method for.
  Class* customizeFor;

  /// upcalls - Upcalls used tp type the stack and locals.
  Classpath* upcalls;

  /// llvmFunction - The LLVM representation of the method.
  llvm::Function* llvmFunction;

  /// llvmContext - The current LLVM context of compilation.
  llvm::LLVMContext* llvmContext;
  
  /// intrinsics - The LLVM intrinsics where lives the compiling LLVM function.
  J3Intrinsics* intrinsics;

  /// TheCompiler - The LLVM Java compiler.
  ///
  JavaLLVMCompiler* TheCompiler;
  
  /// locals - The locals of the method.
  std::vector<llvm::AllocaInst*> intLocals;
  std::vector<llvm::AllocaInst*> longLocals;
  std::vector<llvm::AllocaInst*> floatLocals;
  std::vector<llvm::AllocaInst*> doubleLocals;
  std::vector<llvm::AllocaInst*> objectLocals;

  /// endBlock - The block that returns.
  llvm::BasicBlock* endBlock;

  /// endNode - The result of the method.
  llvm::PHINode* endNode;

  llvm::Value* jmpBuffer;
  
  /// arraySize - Get the size of the array.
  llvm::Value* arraySize(llvm::Value* obj) {
    return llvm::CallInst::Create(intrinsics->ArrayLengthFunction, obj, "",
                                  currentBlock);
  }
  
  /// convertValue - Convert a value to a new type.
  void convertValue(llvm::Value*& val, llvm::Type* t1,
                    llvm::BasicBlock* currentBlock, bool usign);
 
  /// getMutatorThreadPtr - Emit code to get a pointer to the current MutatorThread.
	llvm::Value* getMutatorThreadPtr();

  /// getIsolateIDPtr - Emit code to get a pointer to IsolateID.
	llvm::Value* getIsolateIDPtr(llvm::Value* mutatorThreadPtr);

  /// getVMPtr - Emit code to get a pointer to MyVM.
	llvm::Value* getVMPtr(llvm::Value* mutatorThreadPtr);

  /// getDoYieldPtr - Emit code to get a pointer to doYield.
	llvm::Value* getDoYieldPtr(llvm::Value* mutatorThreadPtr);

  /// getJavaThreadPtr - Emit code to get a pointer to the current JavaThread.
	llvm::Value* getJavaThreadPtr(llvm::Value* mutatorThreadPtr);

  /// getJNIEnvPtr - Emit code to get a pointer to JNIEnv
	llvm::Value* getJNIEnvPtr(llvm::Value* javaThreadPtr);

  /// getJavaExceptionPtr - Emit code to get a pointer to the Java pending exception
	llvm::Value* getJavaExceptionPtr(llvm::Value* javaThreadPtr);

  bool needsInitialisationCheck(Class* cl);  

//===------------------------- Debugging support --------------------------===//
  
  llvm::MDNode* DbgSubprogram;
  
  /// currentBytecodeIndex - The current bytecode being processed.
  uint16 currentBytecodeIndex;
  
  /// CreateLocation - Create debug information for a call.
  llvm::DebugLoc CreateLocation();

//===--------------------------- Inline support ---------------------------===//

  /// inlineCompile - Parse the method and start its LLVM representation
  /// at curBB. endExBlock is the exception destination. args is the
  /// arguments of the method.
  llvm::Instruction* inlineCompile(llvm::BasicBlock*& curBB,
                                   llvm::BasicBlock* endExBlock,
                                   std::vector<llvm::Value*>& args);


  /// inlineMethods - Methods that are currently being inlined. The JIT
  /// uses this map to not inline a method currently bein inlined.
  std::map<JavaMethod*, bool> inlineMethods;

  /// inlining - Are we JITting a method inline?
  bool inlining;
  
  /// canBeInlined - Can this method's body be inlined?
  bool canBeInlined(JavaMethod* meth, bool customizing);

  /// callsStackWalker - Is the method calling a stack walker method? If it is,
  /// then this method can not be inlined.
  bool callsStackWalker;

  bool analyzeForInlining(Reader& reader, uint32_t codeLength);
  bool canInlineLoadConstant(uint16 index);
  bool isThisReference(int staciIndex);

//===------------------------- Bytecode parsing ---------------------------===//

  /// compileOpcodes - Parse the bytecode and create LLVM instructions.
  void compileOpcodes(Reader& reader, uint32 codeLength);

  /// exploreOpcodes - Parse the bytecode and create the basic blocks.
  void exploreOpcodes(Reader& reader, uint32 codeLength);
  
  /// readExceptionTable - Read the exception table in the bytecode. Prepare
  /// exception destination for all Java instructions and set the exception
  /// object to handler blocks.
  unsigned readExceptionTable(Reader& reader, uint32 codeLength);

  /// loadConstant - Load a constant from the _ldc bytecode.
  void loadConstant(uint16 index);

//===------------------------- Runtime exceptions -------------------------===//
  
  /// JITVerifyNull - Insert a null pointer check in the LLVM code.
  void JITVerifyNull(llvm::Value* obj);
  
  
  /// verifyAndComputePtr - Computes the address in the array. If out of bounds
  /// throw an exception.
  llvm::Value* verifyAndComputePtr(llvm::Value* obj, llvm::Value* index,
                                   llvm::Type* arrayType,
                                   bool doNullCheck = true);

  /// compareFP - Do float comparisons.
  void compareFP(llvm::Value*, llvm::Value*, llvm::Type*, bool l);
  

//===------------------------- Stack manipulation -------------------------===//

  /// stack - The compiler stack.
  std::vector<MetaInfo> stack;
  uint32 currentStackIndex;
  std::vector<llvm::AllocaInst*> objectStack;
  std::vector<llvm::AllocaInst*> intStack;
  std::vector<llvm::AllocaInst*> longStack;
  std::vector<llvm::AllocaInst*> floatStack;
  std::vector<llvm::AllocaInst*> doubleStack;

  /// push - Push a new value in the stack.
  void push(llvm::Value* val, bool unsign, CommonClass* cl = 0) {
    llvm::Type* type = val->getType();
    if (unsign) {
      val = new llvm::ZExtInst(val, llvm::Type::getInt32Ty(*llvmContext), "", currentBlock);
      new llvm::StoreInst(val, intStack[currentStackIndex++], false,
                          currentBlock);
      stack.push_back(MetaInfo(upcalls->OfInt, currentBytecode));
    } else if (type == llvm::Type::getInt8Ty(*llvmContext) ||
               type == llvm::Type::getInt16Ty(*llvmContext)) {
      val = new llvm::SExtInst(val, llvm::Type::getInt32Ty(*llvmContext), "",
                               currentBlock);
      new llvm::StoreInst(val, intStack[currentStackIndex++], false,
                          currentBlock);
      stack.push_back(MetaInfo(upcalls->OfInt, currentBytecode));
    } else if (type == llvm::Type::getInt32Ty(*llvmContext)) {
      new llvm::StoreInst(val, intStack[currentStackIndex++], false,
                          currentBlock);
      stack.push_back(MetaInfo(upcalls->OfInt, currentBytecode));
    } else if (type == llvm::Type::getInt64Ty(*llvmContext)) {
      new llvm::StoreInst(val, longStack[currentStackIndex++], false,
                          currentBlock);
      stack.push_back(MetaInfo(upcalls->OfLong, currentBytecode));
    } else if (type == llvm::Type::getFloatTy(*llvmContext)) {
      new llvm::StoreInst(val, floatStack[currentStackIndex++], false,
                          currentBlock);
      stack.push_back(MetaInfo(upcalls->OfFloat, currentBytecode));
    } else if (type == llvm::Type::getDoubleTy(*llvmContext)) {
      new llvm::StoreInst(val, doubleStack[currentStackIndex++], false,
                          currentBlock);
      stack.push_back(MetaInfo(upcalls->OfDouble, currentBytecode));
    } else {
      assert(type == intrinsics->JavaObjectType && "Can't handle this type");
      llvm::Instruction* V = new 
        llvm::StoreInst(val, objectStack[currentStackIndex++], false,
                        currentBlock);
      stack.push_back(MetaInfo(cl ? cl : upcalls->OfObject, currentBytecode));
      addHighLevelType(V, topTypeInfo());
      if (llvm::Instruction* I = llvm::dyn_cast<llvm::Instruction>(val)) {
        addHighLevelType(I, topTypeInfo());
      }
    }
  }

  void addHighLevelType(llvm::Instruction* V, CommonClass* cl) {
    // Enable this when it will be actually used.
#if 0
    llvm::Value* A[1] = 
      { TheCompiler->getNativeClass(cl ? cl : upcalls->OfObject) };
    llvm::MDNode* Node = llvm::MDNode::get(*llvmContext, A, 1);
    V->setMetadata(intrinsics->MetadataTypeKind, Node);
#endif
  }


  /// pop - Pop a value from the stack and return it.
  llvm::Value* pop() {
    llvm::Value* res = top();
    --currentStackIndex;
    stack.pop_back();
    return res;
  }

  /// top - Return the value on top of the stack.
  llvm::Value* top() {
    CommonClass* cl = stack.back().type;
    if (cl == upcalls->OfInt) {
      return new llvm::LoadInst(intStack[currentStackIndex - 1], "", false,
                                currentBlock);
    } else if (cl == upcalls->OfFloat) {
      return new llvm::LoadInst(floatStack[currentStackIndex - 1], "", false,
                                currentBlock);
    } else if (cl == upcalls->OfDouble) {
      return new llvm::LoadInst(doubleStack[currentStackIndex - 1], "", false,
                                currentBlock);
    } else if (cl == upcalls->OfLong) {
      return new llvm::LoadInst(longStack[currentStackIndex - 1], "", false,
                                currentBlock);
    } else {
      return new llvm::LoadInst(objectStack[currentStackIndex - 1], "",
                                false, currentBlock);
    }
  }
  
  /// topTypeInfo - Return the type of the value on top of the stack.
  CommonClass* topTypeInfo() {
    return stack.back().type;
  }

  MetaInfo topInfo() {
    return stack.back();
  }
 
  /// stackSize - Return the size of the stack.
  uint32 stackSize() {
    return stack.size();    
  }
  
  /// popAsInt - Pop a value from the stack and returns it as a Java
  /// int, ie signed int32.
  llvm::Value* popAsInt() {
    return pop();
  }

//===------------------------- Exception support --------------------------===//
  
  /// jsrs - The list of jsrs (jump subroutine) instructions.
  std::vector<llvm::BasicBlock*> jsrs;

  /// endExceptionBlock - The initial exception block where each handler goes
  /// if it does not handle the exception.
  llvm::BasicBlock* endExceptionBlock;

  /// currentExceptionBlock - The exception block of the current instruction.
  llvm::BasicBlock* currentExceptionBlock;

  /// unifiedUnreachable - When an exception is thrown, the code after is
  /// unreachable. All invokes that only throw an exception have the
  /// unifiedUnreachable block as their normal destination.
  llvm::BasicBlock* unifiedUnreachable;

  /// throwException - Emit code to throw an exception.
  void throwRuntimeException(llvm::Function* F, llvm::Value** args,
                      uint32 nbArgs);
  void throwRuntimeException(llvm::Function* F, llvm::Value* arg1);
  void throwException(llvm::Value* obj, bool checkNull = true);

  /// finishExceptions - Emit code to unwind the current function if an
  /// exception is thrown.
  void finishExceptions();

//===--------------------------- Control flow  ----------------------------===//

  /// opcodeInfos - The informations for each instruction.
  Opinfo* opcodeInfos;

  /// currentBlock - The current block of the JIT.
  llvm::BasicBlock* currentBlock;

  /// createBasicBlock - Create a new basic block.
  llvm::BasicBlock* createBasicBlock(const char* name = "") {
    return llvm::BasicBlock::Create(*llvmContext, name, llvmFunction);
  }

  void updateStackInfo(Opinfo& info);
 
  /// branch - Branch based on a boolean value.
  void branch(llvm::Value* test, llvm::BasicBlock* ifTrue, 
              llvm::BasicBlock* ifFalse, llvm::BasicBlock* insert,
              Opinfo& info) {
    updateStackInfo(info);
    llvm::BranchInst::Create(ifTrue, ifFalse, test, insert);
  }

  /// branch - Branch to a new block.
  void branch(Opinfo& info, llvm::BasicBlock* insert) {
    updateStackInfo(info);
    llvm::BranchInst::Create(info.newBlock, insert);
  }
  
//===-------------------------- Synchronization  --------------------------===//
  
  llvm::Value* thisObject;

  /// beginSynchronize - Emit synchronization code to acquire the instance
  /// or the class.
  void beginSynchronize();

  /// endSynchronize - Emit synchronization code to release the instance or the
  /// class.
  void endSynchronize();

  /// monitorEnter - Emit synchronization code to acquire the lock of the value.
  void monitorEnter(llvm::Value* obj);
  
  /// monitorExit - Emit synchronization code to release the lock of the value.
  void monitorExit(llvm::Value* obj);

//===----------------------- Java field accesses  -------------------------===//

  /// getStaticField - Emit code to get the static field declared at the given
  /// index in the constant pool.
  void getStaticField(uint16 index);

  /// setStaticField - Emit code to set a value to the static field declared
  /// at the given index in the constant pool.
  void setStaticField(uint16 index);

  /// getVirtualField - Emit code to get the virtual field declared at the given
  /// index in the constant pool.
  void getVirtualField(uint16 index);

  /// setVirtualField - Emit code to set a value to the virtual field declared
  /// at the given index in the constant pool.
  void setVirtualField(uint16 index);

  /// ldResolved - Emit code to get a pointer to a field.
  llvm::Value* ldResolved(uint16 index, bool stat, llvm::Value* object,
                          llvm::Type* fieldTypePtr, bool thisReference = false);

//===--------------------- Constant pool accesses  ------------------------===//
 
  /// getResolvedCommonClass - Emit code to get a resolved common class. If the
  /// constant pool already links to the class, the class is emitted directly.
  /// Otherwise the JIT installs a resolver which will be called at runtime.
  llvm::Value* getResolvedCommonClass(uint16 index, bool doThrow,
                                      UserCommonClass** alreadyResolved);
  
  /// getResolvedCommonClass - Similar to getResolvedCommonClass, but the type
  /// of the returned value is Class.
  llvm::Value* getResolvedClass(uint16 index, bool clinit, bool doThrow,
                                UserClass** alreadyResolved);
  
  /// getConstantPoolAt - Return the value at the given index of the constant
  /// pool. The generated code invokes the resolver if the constant pool
  /// contains no value at the index.
  llvm::Value* getConstantPoolAt(uint32 index, llvm::Function* resolver,
                                 llvm::Type* returnType,
                                 llvm::Value* addArg, bool doThrow = true);

//===----------------------- Java method calls  ---------------------------===//
  
  /// makeArgs - Insert the arguments of a method in the vector. The arguments
  /// are popped from the compilation stack.
  void makeArgs(llvm::FunctionType::param_iterator it,
                uint32 index, std::vector<llvm::Value*>& result, uint32 nb);

  /// getTarget - Get the target object for invocation.
  llvm::Value* getTarget(Signdef* signature);

  /// invokeVirtual - Invoke a Java virtual method.
  void invokeVirtual(uint16 index);

  /// invokeInterface - Invoke a Java interface method.
  void invokeInterface(uint16 index);

  /// invokeSpecial - Invoke an instance Java method directly.
  void invokeSpecial(uint16 index);

  /// invokeStatic - Invoke a static Java method.
  void invokeStatic(uint16 index);

  /// invokeNew - Allocate a new object.
  void invokeNew(uint16 index);

  /// invokeInline - Instead of calling the method, inline it.
  llvm::Instruction* invokeInline(JavaMethod* meth, 
                                  std::vector<llvm::Value*>& args,
                                  bool customized);

  /// lowerMathOps - Map Java Math operations to LLVM intrinsics.
  llvm::Instruction* lowerMathOps(const UTF8* name, 
                                  std::vector<llvm::Value*>& args);
  llvm::Instruction* lowerFloatOps(const UTF8* name, 
                                   std::vector<llvm::Value*>& args);
  llvm::Instruction* lowerDoubleOps(const UTF8* name, 
                                    std::vector<llvm::Value*>& args);
 
  /// lowerArraycopy - Create a fast path for System.arraycopy.
  void lowerArraycopy(std::vector<llvm::Value*>& args);

  /// invoke - invoke the LLVM method of a Java method.
  llvm::Instruction* invoke(llvm::Value *F, std::vector<llvm::Value*>&args,
                            const char* Name,
                            llvm::BasicBlock *InsertAtEnd);
  llvm::Instruction* invoke(llvm::Value *F, llvm::Value *Actual1,
                            llvm::Value *Actual2, const char* Name,
                            llvm::BasicBlock *InsertAtEnd);
  llvm::Instruction* invoke(llvm::Value *F, llvm::Value *Actual1,
                            const char* Name, llvm::BasicBlock *InsertAtEnd);
  llvm::Instruction* invoke(llvm::Value *F, const char* Name,
                            llvm::BasicBlock *InsertAtEnd);
  
//===--------------------- Yield point support  ---------------------------===//

  void checkYieldPoint();
};

enum Opcode {
      NOP = 0x00,
      ACONST_NULL = 0x01,
      ICONST_M1 = 0x02,
      ICONST_0 = 0x03,
      ICONST_1 = 0x04,
      ICONST_2 = 0x05,
      ICONST_3 = 0x06,
      ICONST_4 = 0x07,
      ICONST_5 = 0x08,
      LCONST_0 = 0x09,
      LCONST_1 = 0x0A,
      FCONST_0 = 0x0B,
      FCONST_1 = 0x0C,
      FCONST_2 = 0x0D,
      DCONST_0 = 0x0E,
      DCONST_1 = 0x0F,
      BIPUSH = 0x10,
      SIPUSH = 0x11,
      LDC = 0x12,
      LDC_W = 0x13,
      LDC2_W = 0x14,
      ILOAD = 0x15,
      LLOAD = 0x16,
      FLOAD = 0x17,
      DLOAD = 0x18,
      ALOAD = 0x19,
      ILOAD_0 = 0x1A,
      ILOAD_1 = 0x1B,
      ILOAD_2 = 0x1C,
      ILOAD_3 = 0x1D,
      LLOAD_0 = 0x1E,
      LLOAD_1 = 0x1F,
      LLOAD_2 = 0x20,
      LLOAD_3 = 0x21,
      FLOAD_0 = 0x22,
      FLOAD_1 = 0x23,
      FLOAD_2 = 0x24,
      FLOAD_3 = 0x25,
      DLOAD_0 = 0x26,
      DLOAD_1 = 0x27,
      DLOAD_2 = 0x28,
      DLOAD_3 = 0x29,
      ALOAD_0 = 0x2A,
      ALOAD_1 = 0x2B,
      ALOAD_2 = 0x2C,
      ALOAD_3 = 0x2D,
      IALOAD = 0x2E,
      LALOAD = 0x2F,
      FALOAD = 0x30,
      DALOAD = 0x31,
      AALOAD = 0x32,
      BALOAD = 0x33,
      CALOAD = 0x34,
      SALOAD = 0x35,
      ISTORE = 0x36,
      LSTORE = 0x37,
      FSTORE = 0x38,
      DSTORE = 0x39,
      ASTORE = 0x3A,
      ISTORE_0 = 0x3B,
      ISTORE_1 = 0x3C,
      ISTORE_2 = 0x3D,
      ISTORE_3 = 0x3E,
      LSTORE_0 = 0x3F,
      LSTORE_1 = 0x40,
      LSTORE_2 = 0x41,
      LSTORE_3 = 0x42,
      FSTORE_0 = 0x43,
      FSTORE_1 = 0x44,
      FSTORE_2 = 0x45,
      FSTORE_3 = 0x46,
      DSTORE_0 = 0x47,
      DSTORE_1 = 0x48,
      DSTORE_2 = 0x49,
      DSTORE_3 = 0x4A,
      ASTORE_0 = 0x4B,
      ASTORE_1 = 0x4C,
      ASTORE_2 = 0x4D,
      ASTORE_3 = 0x4E,
      IASTORE = 0x4F,
      LASTORE = 0x50,
      FASTORE = 0x51,
      DASTORE = 0x52,
      AASTORE = 0x53,
      BASTORE = 0x54,
      CASTORE = 0x55,
      SASTORE = 0x56,
      POP = 0x57,
      POP2 = 0x58,
      DUP = 0x59,
      DUP_X1 = 0x5A,
      DUP_X2 = 0x5B,
      DUP2 = 0x5C,
      DUP2_X1 = 0x5D,
      DUP2_X2 = 0x5E,
      SWAP = 0x5F,
      IADD = 0x60,
      LADD = 0x61,
      FADD = 0x62,
      DADD = 0x63,
      ISUB = 0x64,
      LSUB = 0x65,
      FSUB = 0x66,
      DSUB = 0x67,
      IMUL = 0x68,
      LMUL = 0x69,
      FMUL = 0x6A,
      DMUL = 0x6B,
      IDIV = 0x6C,
      LDIV = 0x6D,
      FDIV = 0x6E,
      DDIV = 0x6F,
      IREM = 0x70,
      LREM = 0x71,
      FREM = 0x72,
      DREM = 0x73,
      INEG = 0x74,
      LNEG = 0x75,
      FNEG = 0x76,
      DNEG = 0x77,
      ISHL = 0x78,
      LSHL = 0x79,
      ISHR = 0x7A,
      LSHR = 0x7B,
      IUSHR = 0x7C,
      LUSHR = 0x7D,
      IAND = 0x7E,
      LAND = 0x7F,
      IOR = 0x80,
      LOR = 0x81,
      IXOR = 0x82,
      LXOR = 0x83,
      IINC = 0x84,
      I2L = 0x85,
      I2F = 0x86,
      I2D = 0x87,
      L2I = 0x88,
      L2F = 0x89,
      L2D = 0x8A,
      F2I = 0x8B,
      F2L = 0x8C,
      F2D = 0x8D,
      D2I = 0x8E,
      D2L = 0x8F,
      D2F = 0x90,
      I2B = 0x91,
      I2C = 0x92,
      I2S = 0x93,
      LCMP = 0x94,
      FCMPL = 0x95,
      FCMPG = 0x96,
      DCMPL = 0x97,
      DCMPG = 0x98,
      IFEQ = 0x99,
      IFNE = 0x9A,
      IFLT = 0x9B,
      IFGE = 0x9C,
      IFGT = 0x9D,
      IFLE = 0x9E,
      IF_ICMPEQ = 0x9F,
      IF_ICMPNE = 0xA0,
      IF_ICMPLT = 0xA1,
      IF_ICMPGE = 0xA2,
      IF_ICMPGT = 0xA3,
      IF_ICMPLE = 0xA4,
      IF_ACMPEQ = 0xA5,
      IF_ACMPNE = 0xA6,
      GOTO = 0xA7,
      JSR = 0xA8,
      RET = 0xA9,
      TABLESWITCH = 0xAA,
      LOOKUPSWITCH = 0xAB,
      IRETURN = 0xAC,
      LRETURN = 0xAD,
      FRETURN = 0xAE,
      DRETURN = 0xAF,
      ARETURN = 0xB0,
      RETURN = 0xB1,
      GETSTATIC = 0xB2,
      PUTSTATIC = 0xB3,
      GETFIELD = 0xB4,
      PUTFIELD = 0xB5,
      INVOKEVIRTUAL = 0xB6,
      INVOKESPECIAL = 0xB7,
      INVOKESTATIC = 0xB8,
      INVOKEINTERFACE = 0xB9,
      UNUSED = 0xBA,
      NEW = 0xBB,
      NEWARRAY = 0xBC,
      ANEWARRAY = 0xBD,
      ARRAYLENGTH = 0xBE,
      ATHROW = 0xBF,
      CHECKCAST = 0xC0,
      INSTANCEOF = 0xC1,
      MONITORENTER = 0xC2,
      MONITOREXIT = 0xC3,
      WIDE = 0xC4,
      MULTIANEWARRAY = 0xC5,
      IFNULL = 0xC6,
      IFNONNULL = 0xC7,
      GOTO_W = 0xC8,
      JSR_W = 0xC9,
      BREAKPOINT = 0xCA,
      IMPDEP1 = 0xFE,
      IMPDEP2 = 0xFF
};

} // end namespace j3

#endif
