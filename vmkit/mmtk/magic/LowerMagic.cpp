//===----- LowerConstantCalls.cpp - Changes arrayLength calls  --------------===//
//
//                               JnJVM
//
// This file is distributed under the University of Illinois Open Source 
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/Function.h"
#include "llvm/Instructions.h"
#include "llvm/LLVMContext.h"
#include "llvm/Module.h"
#include "llvm/Pass.h"
#include "llvm/Support/CallSite.h"
#include "llvm/Support/Compiler.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/raw_ostream.h"

#include <cstdio>

using namespace llvm;

namespace vmmagic {

  class LowerMagic : public FunctionPass {
  public:
    static char ID;
    LowerMagic() : FunctionPass(ID) { }

    virtual bool runOnFunction(Function &F);
  private:
  };
  char LowerMagic::ID = 0;
  static RegisterPass<LowerMagic> X("LowerMagic",
                                    "Lower magic calls");

static const char* AddressClass = "JnJVM_org_vmmagic_unboxed_Address_";
static const char* AddressZeroMethod = 0;
static const char* AddressIsZeroMethod;
static const char* AddressMaxMethod;
//static const char* AddressIsMaxMethod;
//static const char* AddressFromIntSignExtendMethod;
static const char* AddressFromIntZeroExtendMethod;
//static const char* AddressFromLongMethod;
static const char* AddressToObjectReferenceMethod;
static const char* AddressToIntMethod;
static const char* AddressToLongMethod;
static const char* AddressToWordMethod;
static const char* AddressPlusIntMethod;
static const char* AddressPlusOffsetMethod;
static const char* AddressPlusExtentMethod;
static const char* AddressMinusIntMethod;
//static const char* AddressMinusOffsetMethod;
static const char* AddressMinusExtentMethod;
static const char* AddressDiffMethod;
static const char* AddressLTMethod;
static const char* AddressLEMethod;
static const char* AddressGTMethod;
static const char* AddressGEMethod;
static const char* AddressEQMethod;
static const char* AddressNEMethod;
//static const char* AddressPrefetchMethod;
static const char* AddressLoadObjectReferenceMethod;
static const char* AddressLoadObjectReferenceAtOffsetMethod;
static const char* AddressLoadByteMethod;
static const char* AddressLoadByteAtOffsetMethod;
//static const char* AddressLoadCharMethod;
//static const char* AddressLoadCharAtOffsetMethod;
static const char* AddressLoadShortMethod;
static const char* AddressLoadShortAtOffsetMethod;
//static const char* AddressLoadFloatMethod;
//static const char* AddressLoadFloatAtOffsetMethod;
static const char* AddressLoadIntMethod;
static const char* AddressLoadIntAtOffsetMethod;
//static const char* AddressLoadLongMethod;
//static const char* AddressLoadLongAtOffsetMethod;
//static const char* AddressLoadDoubleMethod;
//static const char* AddressLoadDoubleAtOffsetMethod;
static const char* AddressLoadAddressMethod;
static const char* AddressLoadAddressAtOffsetMethod;
static const char* AddressLoadWordMethod;
static const char* AddressLoadWordAtOffsetMethod;
static const char* AddressStoreObjectReferenceMethod;
//static const char* AddressStoreObjectReferenceAtOffsetMethod;
static const char* AddressStoreAddressMethod;
static const char* AddressStoreAddressAtOffsetMethod;
//static const char* AddressStoreFloatMethod;
//static const char* AddressStoreFloatAtOffsetMethod;
static const char* AddressStoreWordMethod;
static const char* AddressStoreWordAtOffsetMethod;
static const char* AddressStoreByteMethod;
static const char* AddressStoreByteAtOffsetMethod;
static const char* AddressStoreIntMethod;
//static const char* AddressStoreIntAtOffsetMethod;
//static const char* AddressStoreDoubleMethod;
//static const char* AddressStoreDoubleAtOffsetMethod;
//static const char* AddressStoreLongMethod;
//static const char* AddressStoreLongAtOffsetMethod;
//static const char* AddressStoreCharMethod;
//static const char* AddressStoreCharAtOffsetMethod;
static const char* AddressStoreShortMethod;
static const char* AddressStoreShortAtOffsetMethod;
static const char* AddressPrepareWordMethod;
static const char* AddressPrepareWordAtOffsetMethod;
//static const char* AddressPrepareObjectReferenceMethod;
//static const char* AddressPrepareObjectReferenceAtOffsetMethod;
//static const char* AddressPrepareAddressMethod;
//static const char* AddressPrepareAddressAtOffsetMethod;
//static const char* AddressPrepareIntMethod;
//static const char* AddressPrepareIntAtOffsetMethod;
//static const char* AddressAttemptIntMethod;
//static const char* AddressAttemptIntAtOffsetMethod;
static const char* AddressAttemptWordMethod;
static const char* AddressAttemptWordAtOffsetMethod;
//static const char* AddressAttemptObjectReferenceMethod;
//static const char* AddressAttemptObjectReferenceAtOffsetMethod;
//static const char* AddressAttemptAddressMethod;
//static const char* AddressAttemptAddressAtOffsetMethod;

static const char* ExtentClass = "JnJVM_org_vmmagic_unboxed_Extent_";
static const char* ExtentToWordMethod = 0;
static const char* ExtentFromIntSignExtendMethod;
static const char* ExtentFromIntZeroExtendMethod;
static const char* ExtentZeroMethod;
static const char* ExtentOneMethod;
static const char* ExtentMaxMethod;
static const char* ExtentToIntMethod;
static const char* ExtentToLongMethod;
static const char* ExtentPlusIntMethod;
static const char* ExtentPlusExtentMethod;
static const char* ExtentMinusIntMethod;
static const char* ExtentMinusExtentMethod;
static const char* ExtentLTMethod;
//static const char* ExtentLEMethod;
static const char* ExtentGTMethod;
//static const char* ExtentGEMethod;
static const char* ExtentEQMethod;
static const char* ExtentNEMethod;

static const char* ObjectReferenceClass = 
  "JnJVM_org_vmmagic_unboxed_ObjectReference_";
static const char* ObjectReferenceFromObjectMethod = 0;
static const char* ObjectReferenceNullReferenceMethod;
static const char* ObjectReferenceToObjectMethod;
static const char* ObjectReferenceToAddressMethod;
static const char* ObjectReferenceIsNullMethod;

static const char* OffsetClass = "JnJVM_org_vmmagic_unboxed_Offset_";
static const char* OffsetFromIntSignExtendMethod = 0;
static const char* OffsetFromIntZeroExtendMethod;
static const char* OffsetZeroMethod;
//static const char* OffsetMaxMethod;
static const char* OffsetToIntMethod;
static const char* OffsetToLongMethod;
static const char* OffsetToWordMethod;
static const char* OffsetPlusIntMethod;
static const char* OffsetMinusIntMethod;
static const char* OffsetMinusOffsetMethod;
static const char* OffsetEQMethod;
//static const char* OffsetNEMethod;
static const char* OffsetSLTMethod;
static const char* OffsetSLEMethod;
static const char* OffsetSGTMethod;
static const char* OffsetSGEMethod;
static const char* OffsetIsZeroMethod;
//static const char* OffsetIsMaxMethod;

static const char* WordClass = "JnJVM_org_vmmagic_unboxed_Word_";
static const char* WordFromIntSignExtendMethod = 0;
static const char* WordFromIntZeroExtendMethod;
//static const char* WordFromLongMethod;
static const char* WordZeroMethod;
static const char* WordOneMethod;
static const char* WordMaxMethod;
static const char* WordToIntMethod;
static const char* WordToLongMethod;
static const char* WordToAddressMethod;
static const char* WordToOffsetMethod;
static const char* WordToExtentMethod;
static const char* WordPlusWordMethod;
//static const char* WordPlusOffsetMethod;
//static const char* WordPlusExtentMethod;
static const char* WordMinusWordMethod;
//static const char* WordMinusOffsetMethod;
static const char* WordMinusExtentMethod;
static const char* WordIsZeroMethod;
//static const char* WordIsMaxMethod;
static const char* WordLTMethod;
static const char* WordLEMethod;
static const char* WordGTMethod;
static const char* WordGEMethod;
static const char* WordEQMethod;
static const char* WordNEMethod;
static const char* WordAndMethod;
static const char* WordOrMethod;
static const char* WordNotMethod;
static const char* WordXorMethod;
static const char* WordLshMethod;
static const char* WordRshlMethod;
//static const char* WordRshaMethod;

static const char* AddressArrayClass = "JnJVM_org_vmmagic_unboxed_AddressArray_";
static const char* ExtentArrayClass = "JnJVM_org_vmmagic_unboxed_ExtentArray_";
static const char* ObjectReferenceArrayClass = "JnJVM_org_vmmagic_unboxed_ObjectReferenceArray_";
static const char* OffsetArrayClass = "JnJVM_org_vmmagic_unboxed_OffsetArray_";
static const char* WordArrayClass = "JnJVM_org_vmmagic_unboxed_WordArray_";

static const char* AddressArrayCreateMethod = "JnJVM_org_vmmagic_unboxed_AddressArray_create__I";
static const char* ExtentArrayCreateMethod = "JnJVM_org_vmmagic_unboxed_ExtentArray_create__I";
static const char* ObjectReferenceArrayCreateMethod = "JnJVM_org_vmmagic_unboxed_ObjectReferenceArray_create__I";
static const char* OffsetArrayCreateMethod = "JnJVM_org_vmmagic_unboxed_OffsetArray_create__I";
static const char* WordArrayCreateMethod = "JnJVM_org_vmmagic_unboxed_WordArray_create__I";

static const char* AddressArrayGetMethod = "JnJVM_org_vmmagic_unboxed_AddressArray_get__I";
static const char* ExtentArrayGetMethod = "JnJVM_org_vmmagic_unboxed_ExtentArray_get__I";
static const char* ObjectReferenceArrayGetMethod = "JnJVM_org_vmmagic_unboxed_ObjectReferenceArray_get__I";
static const char* OffsetArrayGetMethod = "JnJVM_org_vmmagic_unboxed_OffsetArray_get__I";
static const char* WordArrayGetMethod = "JnJVM_org_vmmagic_unboxed_WordArray_get__I";

static const char* AddressArraySetMethod = "JnJVM_org_vmmagic_unboxed_AddressArray_set__ILorg_vmmagic_unboxed_Address_2";
static const char* ExtentArraySetMethod = "JnJVM_org_vmmagic_unboxed_ExtentArray_set__ILorg_vmmagic_unboxed_Extent_2";
static const char* ObjectReferenceArraySetMethod = "JnJVM_org_vmmagic_unboxed_ObjectReferenceArray_set__ILorg_vmmagic_unboxed_ObjectReference_2";
static const char* OffsetArraySetMethod = "JnJVM_org_vmmagic_unboxed_OffsetArray_set__ILorg_vmmagic_unboxed_Offset_2";
static const char* WordArraySetMethod = "JnJVM_org_vmmagic_unboxed_WordArray_set__ILorg_vmmagic_unboxed_Word_2";

static const char* AddressArrayLengthMethod = "JnJVM_org_vmmagic_unboxed_AddressArray_lenght__";
static const char* ExtentArrayLengthMethod = "JnJVM_org_vmmagic_unboxed_ExtentArray_length__";
static const char* ObjectReferenceArrayLengthMethod = "JnJVM_org_vmmagic_unboxed_ObjectReferenceArray_length__";
static const char* OffsetArrayLengthMethod = "JnJVM_org_vmmagic_unboxed_OffsetArray_length__";
static const char* WordArrayLengthMethod = "JnJVM_org_vmmagic_unboxed_WordArray_length__";



static void initialiseFunctions(Module* M) {
  if (!AddressZeroMethod) {
    AddressZeroMethod = "JnJVM_org_vmmagic_unboxed_Address_zero__";
    AddressMaxMethod = "JnJVM_org_vmmagic_unboxed_Address_max__";
    AddressStoreObjectReferenceMethod = "JnJVM_org_vmmagic_unboxed_Address_store__Lorg_vmmagic_unboxed_ObjectReference_2";
    AddressLoadObjectReferenceMethod = "JnJVM_org_vmmagic_unboxed_Address_loadObjectReference__";
    AddressLoadAddressMethod = "JnJVM_org_vmmagic_unboxed_Address_loadAddress__";
    AddressLoadWordMethod = "JnJVM_org_vmmagic_unboxed_Address_loadWord__";
    AddressDiffMethod = "JnJVM_org_vmmagic_unboxed_Address_diff__Lorg_vmmagic_unboxed_Address_2";
    AddressPlusOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_plus__Lorg_vmmagic_unboxed_Offset_2";
    AddressStoreAddressMethod = "JnJVM_org_vmmagic_unboxed_Address_store__Lorg_vmmagic_unboxed_Address_2";
    AddressPlusIntMethod = "JnJVM_org_vmmagic_unboxed_Address_plus__I";
    AddressLTMethod = "JnJVM_org_vmmagic_unboxed_Address_LT__Lorg_vmmagic_unboxed_Address_2";
    AddressGEMethod = "JnJVM_org_vmmagic_unboxed_Address_GE__Lorg_vmmagic_unboxed_Address_2";
    AddressStoreWordMethod = "JnJVM_org_vmmagic_unboxed_Address_store__Lorg_vmmagic_unboxed_Word_2";
    AddressToObjectReferenceMethod = "JnJVM_org_vmmagic_unboxed_Address_toObjectReference__";
    AddressToWordMethod = "JnJVM_org_vmmagic_unboxed_Address_toWord__";
    AddressPrepareWordMethod = "JnJVM_org_vmmagic_unboxed_Address_prepareWord__";
    AddressAttemptWordAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_attempt__Lorg_vmmagic_unboxed_Word_2Lorg_vmmagic_unboxed_Word_2Lorg_vmmagic_unboxed_Offset_2";
    AddressPrepareWordAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_prepareWord__Lorg_vmmagic_unboxed_Offset_2";
    AddressLoadWordAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_loadWord__Lorg_vmmagic_unboxed_Offset_2";
    AddressStoreWordAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_store__Lorg_vmmagic_unboxed_Word_2Lorg_vmmagic_unboxed_Offset_2";
    AddressPlusExtentMethod = "JnJVM_org_vmmagic_unboxed_Address_plus__Lorg_vmmagic_unboxed_Extent_2";
    AddressIsZeroMethod = "JnJVM_org_vmmagic_unboxed_Address_isZero__";
    AddressStoreAddressAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_store__Lorg_vmmagic_unboxed_Address_2Lorg_vmmagic_unboxed_Offset_2";
    AddressGTMethod = "JnJVM_org_vmmagic_unboxed_Address_GT__Lorg_vmmagic_unboxed_Address_2";
    AddressLoadAddressAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_loadAddress__Lorg_vmmagic_unboxed_Offset_2";
    AddressEQMethod = "JnJVM_org_vmmagic_unboxed_Address_EQ__Lorg_vmmagic_unboxed_Address_2";
    AddressLoadObjectReferenceAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_loadObjectReference__Lorg_vmmagic_unboxed_Offset_2";
    AddressLEMethod = "JnJVM_org_vmmagic_unboxed_Address_LE__Lorg_vmmagic_unboxed_Address_2";
    AddressAttemptWordMethod = "JnJVM_org_vmmagic_unboxed_Address_attempt__Lorg_vmmagic_unboxed_Word_2Lorg_vmmagic_unboxed_Word_2";
    AddressNEMethod = "JnJVM_org_vmmagic_unboxed_Address_NE__Lorg_vmmagic_unboxed_Address_2";
    AddressToLongMethod = "JnJVM_org_vmmagic_unboxed_Address_toLong__";
    AddressMinusExtentMethod = "JnJVM_org_vmmagic_unboxed_Address_minus__Lorg_vmmagic_unboxed_Extent_2";
    AddressLoadShortAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_loadShort__Lorg_vmmagic_unboxed_Offset_2";
    AddressStoreShortAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_store__SLorg_vmmagic_unboxed_Offset_2";
    AddressLoadShortMethod = "JnJVM_org_vmmagic_unboxed_Address_loadShort__";
    AddressStoreShortMethod = "JnJVM_org_vmmagic_unboxed_Address_store__S";
    AddressLoadByteMethod = "JnJVM_org_vmmagic_unboxed_Address_loadByte__";
    AddressLoadIntMethod = "JnJVM_org_vmmagic_unboxed_Address_loadInt__";
    AddressStoreIntMethod = "JnJVM_org_vmmagic_unboxed_Address_store__I";
    AddressStoreByteMethod = "JnJVM_org_vmmagic_unboxed_Address_store__B";
    AddressLoadByteAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_loadByte__Lorg_vmmagic_unboxed_Offset_2";
    AddressMinusIntMethod = "JnJVM_org_vmmagic_unboxed_Address_minus__I";
    AddressLoadIntAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_loadInt__Lorg_vmmagic_unboxed_Offset_2";
    AddressStoreByteAtOffsetMethod = "JnJVM_org_vmmagic_unboxed_Address_store__BLorg_vmmagic_unboxed_Offset_2";
    AddressFromIntZeroExtendMethod = "JnJVM_org_vmmagic_unboxed_Address_fromIntZeroExtend__I";
    AddressToIntMethod = "JnJVM_org_vmmagic_unboxed_Address_toInt__";
    
    ExtentToWordMethod = "JnJVM_org_vmmagic_unboxed_Extent_toWord__";
    ExtentMinusExtentMethod = "JnJVM_org_vmmagic_unboxed_Extent_minus__Lorg_vmmagic_unboxed_Extent_2";
    ExtentPlusExtentMethod = "JnJVM_org_vmmagic_unboxed_Extent_plus__Lorg_vmmagic_unboxed_Extent_2";
    ExtentPlusIntMethod = "JnJVM_org_vmmagic_unboxed_Extent_plus__I";
    ExtentMinusIntMethod = "JnJVM_org_vmmagic_unboxed_Extent_minus__I";
    ExtentFromIntZeroExtendMethod = "JnJVM_org_vmmagic_unboxed_Extent_fromIntZeroExtend__I";
    ExtentFromIntSignExtendMethod = "JnJVM_org_vmmagic_unboxed_Extent_fromIntSignExtend__I";
    ExtentOneMethod = "JnJVM_org_vmmagic_unboxed_Extent_one__";
    ExtentNEMethod = "JnJVM_org_vmmagic_unboxed_Extent_NE__Lorg_vmmagic_unboxed_Extent_2";
    ExtentZeroMethod = "JnJVM_org_vmmagic_unboxed_Extent_zero__";
    ExtentToLongMethod = "JnJVM_org_vmmagic_unboxed_Extent_toLong__";
    ExtentToIntMethod = "JnJVM_org_vmmagic_unboxed_Extent_toInt__";
    ExtentEQMethod = "JnJVM_org_vmmagic_unboxed_Extent_EQ__Lorg_vmmagic_unboxed_Extent_2";
    ExtentGTMethod = "JnJVM_org_vmmagic_unboxed_Extent_GT__Lorg_vmmagic_unboxed_Extent_2";
    ExtentLTMethod = "JnJVM_org_vmmagic_unboxed_Extent_LT__Lorg_vmmagic_unboxed_Extent_2";
    ExtentMaxMethod = "JnJVM_org_vmmagic_unboxed_Extent_max__";

    ObjectReferenceFromObjectMethod = "JnJVM_org_vmmagic_unboxed_ObjectReference_fromObject__Ljava_lang_Object_2";
    ObjectReferenceToObjectMethod = "JnJVM_org_vmmagic_unboxed_ObjectReference_toObject__";
    ObjectReferenceNullReferenceMethod = "JnJVM_org_vmmagic_unboxed_ObjectReference_nullReference__";
    ObjectReferenceToAddressMethod = "JnJVM_org_vmmagic_unboxed_ObjectReference_toAddress__";
    ObjectReferenceIsNullMethod = "JnJVM_org_vmmagic_unboxed_ObjectReference_isNull__";

    WordOrMethod = "JnJVM_org_vmmagic_unboxed_Word_or__Lorg_vmmagic_unboxed_Word_2";
    WordRshlMethod = "JnJVM_org_vmmagic_unboxed_Word_rshl__I";
    WordToIntMethod = "JnJVM_org_vmmagic_unboxed_Word_toInt__";
    WordNotMethod = "JnJVM_org_vmmagic_unboxed_Word_not__";
    WordZeroMethod = "JnJVM_org_vmmagic_unboxed_Word_zero__";
    WordOneMethod = "JnJVM_org_vmmagic_unboxed_Word_one__";
    WordAndMethod = "JnJVM_org_vmmagic_unboxed_Word_and__Lorg_vmmagic_unboxed_Word_2";
    WordToAddressMethod = "JnJVM_org_vmmagic_unboxed_Word_toAddress__";
    WordLshMethod = "JnJVM_org_vmmagic_unboxed_Word_lsh__I";
    WordMinusWordMethod = "JnJVM_org_vmmagic_unboxed_Word_minus__Lorg_vmmagic_unboxed_Word_2";
    WordLTMethod = "JnJVM_org_vmmagic_unboxed_Word_LT__Lorg_vmmagic_unboxed_Word_2";
    WordPlusWordMethod = "JnJVM_org_vmmagic_unboxed_Word_plus__Lorg_vmmagic_unboxed_Word_2";
    WordLEMethod = "JnJVM_org_vmmagic_unboxed_Word_LE__Lorg_vmmagic_unboxed_Word_2";
    WordGEMethod = "JnJVM_org_vmmagic_unboxed_Word_GE__Lorg_vmmagic_unboxed_Word_2";
    WordEQMethod = "JnJVM_org_vmmagic_unboxed_Word_EQ__Lorg_vmmagic_unboxed_Word_2";
    WordNEMethod = "JnJVM_org_vmmagic_unboxed_Word_NE__Lorg_vmmagic_unboxed_Word_2";
    WordFromIntSignExtendMethod = "JnJVM_org_vmmagic_unboxed_Word_fromIntSignExtend__I";
    WordIsZeroMethod = "JnJVM_org_vmmagic_unboxed_Word_isZero__";
    WordXorMethod = "JnJVM_org_vmmagic_unboxed_Word_xor__Lorg_vmmagic_unboxed_Word_2";
    WordFromIntZeroExtendMethod = "JnJVM_org_vmmagic_unboxed_Word_fromIntZeroExtend__I";
    WordToExtentMethod = "JnJVM_org_vmmagic_unboxed_Word_toExtent__";
    WordMinusExtentMethod = "JnJVM_org_vmmagic_unboxed_Word_minus__Lorg_vmmagic_unboxed_Extent_2";
    WordToLongMethod = "JnJVM_org_vmmagic_unboxed_Word_toLong__";
    WordMaxMethod = "JnJVM_org_vmmagic_unboxed_Word_max__";
    WordToOffsetMethod = "JnJVM_org_vmmagic_unboxed_Word_toOffset__";
    WordGTMethod = "JnJVM_org_vmmagic_unboxed_Word_GT__Lorg_vmmagic_unboxed_Word_2";


    OffsetSLTMethod = "JnJVM_org_vmmagic_unboxed_Offset_sLT__Lorg_vmmagic_unboxed_Offset_2";
    OffsetFromIntSignExtendMethod = "JnJVM_org_vmmagic_unboxed_Offset_fromIntSignExtend__I";
    OffsetSGTMethod = "JnJVM_org_vmmagic_unboxed_Offset_sGT__Lorg_vmmagic_unboxed_Offset_2";
    OffsetPlusIntMethod = "JnJVM_org_vmmagic_unboxed_Offset_plus__I";
    OffsetZeroMethod = "JnJVM_org_vmmagic_unboxed_Offset_zero__";
    OffsetToWordMethod = "JnJVM_org_vmmagic_unboxed_Offset_toWord__";
    OffsetFromIntZeroExtendMethod = "JnJVM_org_vmmagic_unboxed_Offset_fromIntZeroExtend__I";
    OffsetSGEMethod = "JnJVM_org_vmmagic_unboxed_Offset_sGE__Lorg_vmmagic_unboxed_Offset_2";
    OffsetToIntMethod = "JnJVM_org_vmmagic_unboxed_Offset_toInt__";
    OffsetToLongMethod = "JnJVM_org_vmmagic_unboxed_Offset_toLong__";
    OffsetIsZeroMethod = "JnJVM_org_vmmagic_unboxed_Offset_isZero__";
    OffsetMinusIntMethod = "JnJVM_org_vmmagic_unboxed_Offset_minus__I";
    OffsetSLEMethod = "JnJVM_org_vmmagic_unboxed_Offset_sLE__Lorg_vmmagic_unboxed_Offset_2";
    OffsetEQMethod = "JnJVM_org_vmmagic_unboxed_Offset_EQ__Lorg_vmmagic_unboxed_Offset_2";
    OffsetMinusOffsetMethod = "JnJVM_org_vmmagic_unboxed_Offset_minus__Lorg_vmmagic_unboxed_Offset_2";
  }
}


static bool removePotentialNullCheck(BasicBlock* Cur, Value* Obj) {
  BasicBlock* BB = Cur->getUniquePredecessor();
  LLVMContext& Context = Cur->getParent()->getContext();
  if (BB) {
    Instruction* T = BB->getTerminator();
    if (dyn_cast<BranchInst>(T) && T != BB->begin()) {
      BasicBlock::iterator BIE = BB->end();
      --BIE; // Terminator
      --BIE; // Null test
      if (ICmpInst* IE = dyn_cast<ICmpInst>(BIE)) {
        if (IE->getPredicate() == ICmpInst::ICMP_EQ &&
            IE->getOperand(0) == Obj &&
            IE->getOperand(1) == Constant::getNullValue(Obj->getType())) {
          BIE->replaceAllUsesWith(ConstantInt::getFalse(Context));
          BIE->eraseFromParent();
          return true;
        }
      }
    }
  }
  return false;
}

bool LowerMagic::runOnFunction(Function& F) {
  Module* globalModule = F.getParent();
  LLVMContext& Context = globalModule->getContext();
  bool Changed = false;
  llvm::Type* pointerSizeType = 
    globalModule->getPointerSize() == llvm::Module::Pointer32 ?
      Type::getInt32Ty(Context) : Type::getInt64Ty(Context);
 
   Constant* constantPtrLogSize = 
    ConstantInt::get(Type::getInt32Ty(Context), sizeof(void*) == 8 ? 3 : 2);

  llvm::Type* ptrType = PointerType::getUnqual(Type::getInt8Ty(Context));
  llvm::Type* ptrSizeType = PointerType::getUnqual(pointerSizeType);


  initialiseFunctions(globalModule);

  Function* MMalloc = globalModule->getFunction("AllocateMagicArray");
  if (!MMalloc) {
    std::vector<Type*>FuncTyArgs;
    FuncTyArgs.push_back(Type::getInt32Ty(Context));
    FuncTyArgs.push_back(ptrType);
    FunctionType* FuncTy = FunctionType::get(ptrType, FuncTyArgs, false);


    MMalloc = Function::Create(FuncTy, GlobalValue::ExternalLinkage, "AllocateMagicArray",
                               globalModule);
  }


  for (Function::iterator BI = F.begin(), BE = F.end(); BI != BE; BI++) { 
    BasicBlock *Cur = BI; 
    for (BasicBlock::iterator II = Cur->begin(), IE = Cur->end(); II != IE;) {
      Instruction *I = II;
      II++;
      if (I->getOpcode() != Instruction::Call &&
          I->getOpcode() != Instruction::Invoke) {
        continue;
      }

      CallSite Call(I);
      Instruction* CI = I;
        Value* V = Call.getCalledValue();
        if (Function* FCur = dyn_cast<Function>(V)) {
          const char* name = FCur->getName().data();
          unsigned len = FCur->getName().size();
          if (len > strlen(AddressClass) && 
              !memcmp(AddressClass, name, strlen(AddressClass))) {
            
            Changed = true;
            // Remove the null check
            if (Call.arg_begin() != Call.arg_end()) {
              removePotentialNullCheck(Cur, Call.getArgument(0));
            }

            if (!strcmp(FCur->getName().data(), AddressZeroMethod)) {
              Constant* N = Constant::getNullValue(FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressMaxMethod)) {
              ConstantInt* M = ConstantInt::get(Type::getInt64Ty(Context), (uint64_t)-1);
              Constant* N = ConstantExpr::getIntToPtr(M, FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressStoreObjectReferenceMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreAddressMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreShortMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreByteMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreIntMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreWordMethod)) {
              Value* Addr = Call.getArgument(0);
              Value* Obj = Call.getArgument(1);
              Type* Ty = PointerType::getUnqual(Obj->getType());
              Addr = new BitCastInst(Addr, Ty, "", CI);
              new StoreInst(Obj, Addr, CI);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressLoadObjectReferenceMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadAddressMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadWordMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadShortMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadByteMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadIntMethod) ||
                       !strcmp(FCur->getName().data(), AddressPrepareWordMethod)) {
              Value* Addr = Call.getArgument(0);
              Type* Ty = PointerType::getUnqual(FCur->getReturnType());
              Addr = new BitCastInst(Addr, Ty, "", CI);
              Value* LD = new LoadInst(Addr, "", CI);
              CI->replaceAllUsesWith(LD);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressDiffMethod) ||
                       !strcmp(FCur->getName().data(), AddressMinusExtentMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressPlusOffsetMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressPlusIntMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressMinusIntMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressLTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_ULT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressGTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_UGT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressEQMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressNEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_NE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressLEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_ULE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressGEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_UGE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressToObjectReferenceMethod) ||
                       !strcmp(FCur->getName().data(), AddressToWordMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new BitCastInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressAttemptWordAtOffsetMethod)) {
              Value* Ptr = Call.getArgument(0);
              Value* Old = Call.getArgument(1);
              Value* Val = Call.getArgument(2);
              Value* Offset = Call.getArgument(3);

              Ptr = new PtrToIntInst(Ptr, pointerSizeType, "", CI);
              Offset = new PtrToIntInst(Offset, pointerSizeType, "", CI);
              Ptr = BinaryOperator::CreateAdd(Ptr, Offset, "", CI);
              Type* Ty = PointerType::getUnqual(pointerSizeType);
              Ptr = new IntToPtrInst(Ptr, Ty, "", CI);
              Old = new PtrToIntInst(Old, pointerSizeType, "", CI);
              Val = new PtrToIntInst(Val, pointerSizeType, "", CI);
              
              Value* res = new AtomicCmpXchgInst(
                  Ptr, Old, Val, SequentiallyConsistent, CrossThread, CI);
              res = new ICmpInst(CI, ICmpInst::ICMP_EQ, res, Old, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);

              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressAttemptWordMethod)) {
              Value* Ptr = Call.getArgument(0);
              Value* Old = Call.getArgument(1);
              Value* Val = Call.getArgument(2);

              Type* Ty = PointerType::getUnqual(pointerSizeType);
              Ptr = new BitCastInst(Ptr, Ty, "", CI);
              Old = new PtrToIntInst(Old, pointerSizeType, "", CI);
              Val = new PtrToIntInst(Val, pointerSizeType, "", CI);
              
              Value* res = new AtomicCmpXchgInst(
                  Ptr, Old, Val, SequentiallyConsistent, CrossThread, CI);
              res = new ICmpInst(CI, ICmpInst::ICMP_EQ, res, Old, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);

              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressPrepareWordAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadWordAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadAddressAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadObjectReferenceAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadByteAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadIntAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressLoadShortAtOffsetMethod)) {
              Value* Ptr = Call.getArgument(0);
              Value* Offset = Call.getArgument(1);

              Ptr = new PtrToIntInst(Ptr, pointerSizeType, "", CI);
              Offset = new PtrToIntInst(Offset, pointerSizeType, "", CI);
              Ptr = BinaryOperator::CreateAdd(Ptr, Offset, "", CI);
              Type* Ty = PointerType::getUnqual(FCur->getReturnType());
              Ptr = new IntToPtrInst(Ptr, Ty, "", CI);
              Value* res = new LoadInst(Ptr, "", CI);

              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressStoreWordAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreAddressAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreByteAtOffsetMethod) ||
                       !strcmp(FCur->getName().data(), AddressStoreShortAtOffsetMethod)) {
              Value* Ptr = Call.getArgument(0);
              Value* Val = Call.getArgument(1);
              Value* Offset = Call.getArgument(2);

              Ptr = new PtrToIntInst(Ptr, pointerSizeType, "", CI);
              Offset = new PtrToIntInst(Offset, pointerSizeType, "", CI);
              Ptr = BinaryOperator::CreateAdd(Ptr, Offset, "", CI);
              Type* Ty = PointerType::getUnqual(Val->getType());
              Ptr = new IntToPtrInst(Ptr, Ty, "", CI);
              new StoreInst(Val, Ptr, CI);

              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressPlusExtentMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressIsZeroMethod)) {
              Value* Val = Call.getArgument(0);
              Constant* N = Constant::getNullValue(Val->getType());
              Value* Res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val, N, "");
              Res = new ZExtInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressToLongMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt64Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressFromIntZeroExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new ZExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), AddressToIntMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt32Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else {
              fprintf(stderr, "Implement me %s\n", name);
              abort();
            }

          } else if (len > strlen(ExtentClass) && 
              !memcmp(ExtentClass, name, strlen(ExtentClass))) {
            
            Changed = true;
            // Remove the null check
            if (Call.arg_begin() != Call.arg_end()) {
              removePotentialNullCheck(Cur, Call.getArgument(0));
            }

            if (!strcmp(FCur->getName().data(), ExtentToWordMethod)) {
              CI->replaceAllUsesWith(Call.getArgument(0));
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentMinusExtentMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentPlusExtentMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentPlusIntMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentMinusIntMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentFromIntZeroExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new ZExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentFromIntSignExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new SExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentOneMethod)) {
              Constant* N = ConstantInt::get(pointerSizeType, 1);
              N = ConstantExpr::getIntToPtr(N, FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentNEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_NE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentEQMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentGTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_UGT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentLTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_ULT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentZeroMethod)) {
              Constant* N = Constant::getNullValue(FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentToLongMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt64Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentToIntMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt32Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ExtentMaxMethod)) {
              ConstantInt* M = ConstantInt::get(Type::getInt64Ty(Context), (uint64_t)-1);
              Constant* N = ConstantExpr::getIntToPtr(M, FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else {
              fprintf(stderr, "Implement me %s\n", name);
              abort();
            }
          } else if (len > strlen(OffsetClass) && 
              !memcmp(OffsetClass, name, strlen(OffsetClass))) {
            
            Changed = true;
            // Remove the null check
            if (Call.arg_begin() != Call.arg_end()) {
              removePotentialNullCheck(Cur, Call.getArgument(0));
            }
            
            if (!strcmp(FCur->getName().data(), OffsetSLTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_SLT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetToWordMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new BitCastInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetZeroMethod)) {
              Constant* N = Constant::getNullValue(FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetSGTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_SGT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetSGEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_SGE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetSLEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_SLE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetEQMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetFromIntSignExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new SExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetFromIntZeroExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new ZExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetPlusIntMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetToIntMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt32Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetToLongMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt64Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetIsZeroMethod)) {
              Value* Val = Call.getArgument(0);
              Constant* N = Constant::getNullValue(Val->getType());
              Value* Res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val, N, "");
              Res = new ZExtInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetMinusIntMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), OffsetMinusOffsetMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else {
              fprintf(stderr, "Implement me %s\n", name);
              abort();
            }
          } else if (len > strlen(ObjectReferenceClass) && 
            !memcmp(ObjectReferenceClass, name, strlen(ObjectReferenceClass))) {
           
            Changed = true;
            // Remove the null check
            if (Call.arg_begin() != Call.arg_end()) {
              removePotentialNullCheck(Cur, Call.getArgument(0));
            }

            if (!strcmp(FCur->getName().data(), ObjectReferenceNullReferenceMethod)) {
              Constant* N = Constant::getNullValue(FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ObjectReferenceFromObjectMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new BitCastInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ObjectReferenceToAddressMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new BitCastInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ObjectReferenceToObjectMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new BitCastInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), ObjectReferenceIsNullMethod)) {
              Value* Val = Call.getArgument(0);
              Constant* N = Constant::getNullValue(Val->getType());
              Value* Res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val, N, "");
              Res = new ZExtInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else {
              fprintf(stderr, "Implement me %s\n", name);
              abort();
            }
          } else if (len > strlen(WordClass) && 
              !memcmp(WordClass, name, strlen(WordClass))) {
           
            Changed = true;
            // Remove the null check
            if (Call.arg_begin() != Call.arg_end()) {
              removePotentialNullCheck(Cur, Call.getArgument(0));
            }
             
            if (!strcmp(FCur->getName().data(), WordOrMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* Res = BinaryOperator::CreateOr(Val1, Val2, "", CI);
              Res = new IntToPtrInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordAndMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* Res = BinaryOperator::CreateAnd(Val1, Val2, "", CI);
              Res = new IntToPtrInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordXorMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* Res = BinaryOperator::CreateXor(Val1, Val2, "", CI);
              Res = new IntToPtrInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordRshlMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* Res = BinaryOperator::CreateLShr(Val1, Val2, "", CI);
              Res = new IntToPtrInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordLshMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              if (Val2->getType() != pointerSizeType)
                Val2 = new ZExtInst(Val2, pointerSizeType, "", CI);
              Value* Res = BinaryOperator::CreateShl(Val1, Val2, "", CI);
              Res = new IntToPtrInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordToIntMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt32Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordNotMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, pointerSizeType, "", CI);
              Constant* M1 = ConstantInt::get(pointerSizeType, -1);
              Value* Res = BinaryOperator::CreateXor(Val, M1, "", CI);
              Res = new IntToPtrInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordZeroMethod)) {
              Constant* N = Constant::getNullValue(FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordOneMethod)) {
              Constant* N = ConstantInt::get(pointerSizeType, 1);
              N = ConstantExpr::getIntToPtr(N, FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordToAddressMethod) ||
                       !strcmp(FCur->getName().data(), WordToOffsetMethod) ||
                       !strcmp(FCur->getName().data(), WordToExtentMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new BitCastInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordMinusWordMethod) ||
                       !strcmp(FCur->getName().data(), WordMinusExtentMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateSub(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordPlusWordMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = BinaryOperator::CreateAdd(Val1, Val2, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordLTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_ULT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordLEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_ULE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordGEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_UGE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordEQMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordGTMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_UGT, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordNEMethod)) {
              Value* Val1 = Call.getArgument(0);
              Value* Val2 = Call.getArgument(1);
              Val1 = new PtrToIntInst(Val1, pointerSizeType, "", CI);
              Val2 = new PtrToIntInst(Val2, pointerSizeType, "", CI);
              Value* res = new ICmpInst(CI, ICmpInst::ICMP_NE, Val1, Val2, "");
              res = new ZExtInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordFromIntSignExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new SExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordFromIntZeroExtendMethod)) {
              Value* Val = Call.getArgument(0);
              if (pointerSizeType != Type::getInt32Ty(Context))
                Val = new ZExtInst(Val, pointerSizeType, "", CI);
              Val = new IntToPtrInst(Val, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordIsZeroMethod)) {
              Value* Val = Call.getArgument(0);
              Constant* N = Constant::getNullValue(Val->getType());
              Value* Res = new ICmpInst(CI, ICmpInst::ICMP_EQ, Val, N, "");
              Res = new ZExtInst(Res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(Res);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordToLongMethod)) {
              Value* Val = Call.getArgument(0);
              Val = new PtrToIntInst(Val, Type::getInt64Ty(Context), "", CI);
              CI->replaceAllUsesWith(Val);
              CI->eraseFromParent();
            } else if (!strcmp(FCur->getName().data(), WordMaxMethod)) {
              ConstantInt* M = ConstantInt::get(Type::getInt64Ty(Context), (uint64_t)-1);
              Constant* N = ConstantExpr::getIntToPtr(M, FCur->getReturnType());
              CI->replaceAllUsesWith(N);
              CI->eraseFromParent();
            } else {
              fprintf(stderr, "Implement me %s\n", name);
              abort();
            }
          } else if (
              (len > strlen(AddressArrayClass) && 
               !memcmp(AddressArrayClass, name, strlen(AddressArrayClass))) ||
              (len > strlen(OffsetArrayClass) && 
               !memcmp(OffsetArrayClass, name, strlen(OffsetArrayClass))) ||
              (len > strlen(WordArrayClass) && 
               !memcmp(WordArrayClass, name, strlen(WordArrayClass))) ||
              (len > strlen(ObjectReferenceArrayClass) && 
               !memcmp(ObjectReferenceArrayClass, name, strlen(ObjectReferenceArrayClass))) ||
              (len > strlen(ExtentArrayClass) && 
               !memcmp(ExtentArrayClass, name, strlen(ExtentArrayClass)))) {
            Changed = true;
            
            if (!strcmp(FCur->getName().data(), AddressArrayCreateMethod) ||
                !strcmp(FCur->getName().data(), OffsetArrayCreateMethod) ||
                !strcmp(FCur->getName().data(), WordArrayCreateMethod) ||
                !strcmp(FCur->getName().data(), ExtentArrayCreateMethod) ||
                !strcmp(FCur->getName().data(), ObjectReferenceArrayCreateMethod)) {
              Value* Val = Call.getArgument(0);
              ConstantInt* One = ConstantInt::get(Type::getInt32Ty(Context), (uint64_t)1);
              Value* Length = BinaryOperator::CreateAdd(Val, One, "", CI);
              Length = BinaryOperator::CreateShl(Length, constantPtrLogSize, "", CI);
              Val = new IntToPtrInst(Val, ptrType, "", CI);
              Value* args[2] = { Length, Val };
              Value* res = CallInst::Create(MMalloc, args, "", CI);
              res = new BitCastInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent();

            } else if (!strcmp(FCur->getName().data(), AddressArrayGetMethod) ||
                !strcmp(FCur->getName().data(), OffsetArrayGetMethod) ||
                !strcmp(FCur->getName().data(), WordArrayGetMethod) ||
                !strcmp(FCur->getName().data(), ExtentArrayGetMethod) ||
                !strcmp(FCur->getName().data(), ObjectReferenceArrayGetMethod)) {
              
              Value* Array = Call.getArgument(0);
              Value* Index = Call.getArgument(1);
              ConstantInt* One = ConstantInt::get(Type::getInt32Ty(Context), (uint64_t)1);
              Index = BinaryOperator::CreateAdd(Index, One, "", CI);
              Array = new BitCastInst(Array, ptrSizeType, "", CI);
              Value* res = GetElementPtrInst::Create(Array, Index, "", CI);
              res = new LoadInst(res, "", CI);
              res = new IntToPtrInst(res, FCur->getReturnType(), "", CI);
              CI->replaceAllUsesWith(res);
              CI->eraseFromParent(); 
            
            } else if (!strcmp(FCur->getName().data(), AddressArraySetMethod) ||
                !strcmp(FCur->getName().data(), OffsetArraySetMethod) ||
                !strcmp(FCur->getName().data(), WordArraySetMethod) ||
                !strcmp(FCur->getName().data(), ExtentArraySetMethod) ||
                !strcmp(FCur->getName().data(), ObjectReferenceArraySetMethod)) {
              
              Value* Array = Call.getArgument(0);
              Value* Index = Call.getArgument(1);
              Value* Element = Call.getArgument(2);
              ConstantInt* One = ConstantInt::get(Type::getInt32Ty(Context), (uint64_t)1);
              
              Index = BinaryOperator::CreateAdd(Index, One, "", CI);
              Array = new BitCastInst(Array, ptrSizeType, "", CI);
              Value* ptr = GetElementPtrInst::Create(Array, Index, "", CI);
              Element = new PtrToIntInst(Element, pointerSizeType, "", CI);
              new StoreInst(Element, ptr, CI);
              CI->eraseFromParent(); 
            
            } else if (!strcmp(FCur->getName().data(), AddressArrayLengthMethod) ||
                !strcmp(FCur->getName().data(), OffsetArrayLengthMethod) ||
                !strcmp(FCur->getName().data(), WordArrayLengthMethod) ||
                !strcmp(FCur->getName().data(), ExtentArrayLengthMethod) ||
                !strcmp(FCur->getName().data(), ObjectReferenceArrayLengthMethod)) {
              
              Value* Array = Call.getArgument(0);
              Array = new BitCastInst(Array, ptrSizeType, "", CI);
              Value* Length = new LoadInst(Array, "", CI);
              if (Length->getType() != Type::getInt32Ty(Context)) {
                Length = new TruncInst(Length, Type::getInt32Ty(Context), "", CI);
              }
              CI->replaceAllUsesWith(Length);
              CI->eraseFromParent(); 
            }
          }
        }
    }
  }
  return Changed;
}


FunctionPass* createLowerMagicPass() {
  return new LowerMagic();
}

}
