//===----- VmkitAOTGC.cpp - Support for Ahead of Time Compiler GC -------===//
//
//                            The Vmkit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "llvm/Constants.h"
#include "llvm/DerivedTypes.h"
#include "llvm/Type.h"
#include "llvm/CodeGen/GCs.h"
#include "llvm/CodeGen/GCStrategy.h"
#include "llvm/CodeGen/AsmPrinter.h"
#include "llvm/CodeGen/GCMetadataPrinter.h"
#include "llvm/Module.h"
#include "llvm/MC/MCAsmInfo.h"
#include "llvm/MC/MCContext.h"
#include "llvm/MC/MCSymbol.h"
#include "llvm/MC/MCStreamer.h"
#include "llvm/Target/Mangler.h"
#include "llvm/Target/TargetData.h"
#include "llvm/Target/TargetLoweringObjectFile.h"
#include "llvm/Target/TargetMachine.h"
#include "llvm/ADT/SmallString.h"
#include "llvm/Support/Compiler.h"
#include "llvm/Support/ErrorHandling.h"
#include "llvm/Support/FormattedStream.h"
#include "llvm/Support/raw_ostream.h"
#include <cctype>
#include <cstdio>

using namespace llvm;

namespace {
  class VmkitAOTGC : public GCStrategy {
  public:
    VmkitAOTGC();
  };
}

static GCRegistry::Add<VmkitAOTGC>
X("vmkit", "Vmkit GC for AOT-generated functions");

VmkitAOTGC::VmkitAOTGC() {
  NeededSafePoints = 1 << GC::PostCall;
  UsesMetadata = true;
}

namespace {

  class VmkitAOTGCMetadataPrinter : public GCMetadataPrinter {
  public:
    void beginAssembly(AsmPrinter &AP);
    void finishAssembly(AsmPrinter &AP);
  };

}

static GCMetadataPrinterRegistry::Add<VmkitAOTGCMetadataPrinter>
Y("vmkit", "Vmkit GC for AOT-generated functions");

void VmkitAOTGCMetadataPrinter::beginAssembly(AsmPrinter &AP) {
}

static bool isAcceptableChar(char C) {
  if ((C < 'a' || C > 'z') &&
      (C < 'A' || C > 'Z') &&
      (C < '0' || C > '9') &&
      C != '_' && C != '$' && C != '@') {
    return false;
  }
  return true;
}

static char HexDigit(int V) {
  return V < 10 ? V+'0' : V+'A'-10;
}

static void MangleLetter(SmallVectorImpl<char> &OutName, unsigned char C) {
  OutName.push_back('_');
  OutName.push_back(HexDigit(C >> 4));
  OutName.push_back(HexDigit(C & 15));
  OutName.push_back('_');
}


static void EmitVmkitGlobal(const Module &M, AsmPrinter &AP, const char *Id) {
  const std::string &MId = M.getModuleIdentifier();

  std::string SymName;
  SymName += "vmkit";
  size_t Letter = SymName.size();
  SymName += MId;
  SymName += "__";
  SymName += Id;

  // Capitalize the first letter of the module name.
  SymName[Letter] = toupper(SymName[Letter]);

  SmallString<128> TmpStr;
  AP.Mang->getNameWithPrefix(TmpStr, SymName);

  SmallString<128> FinalStr;
  for (unsigned i = 0, e = TmpStr.size(); i != e; ++i) {
    if (!isAcceptableChar(TmpStr[i])) {
      MangleLetter(FinalStr, TmpStr[i]);
    } else {
      FinalStr.push_back(TmpStr[i]);
    }
  }

  MCSymbol *Sym = AP.OutContext.GetOrCreateSymbol(FinalStr);

  AP.OutStreamer.EmitSymbolAttribute(Sym, MCSA_Global);
  AP.OutStreamer.EmitLabel(Sym);
}

static bool methodNameMatches(StringRef compiledName,
                              Constant* name,
                              Constant* type) {
  uint32_t size = compiledName.size();
  std::string str;

  for (uint32_t i = 0; i < name->getNumOperands(); ++i) {
    int16_t cur = cast<ConstantInt>(name->getOperand(i))->getZExtValue();
    if (cur == '/') {
      str += '_';
    } else if (cur == '_') {
      str += "_1";
    } else if (cur == '<') {
      str += "_0003C";
    } else if (cur == '>') {
      str += "_0003E";
    } else {
      str += (char)cur;
    }
  }

  for (uint32_t i = 0; i < type->getNumOperands(); ++i) {
    int16_t cur = cast<ConstantInt>(type->getOperand(i))->getZExtValue();
    if (cur == '(') {
      str += "__";
    } else if (cur == '/') {
      str += '_';
    } else if (cur == '_') {
      str += "_1";
    } else if (cur == '$') {
      str += "_00024";
    } else if (cur == ';') {
      str += "_2";
    } else if (cur == '[') {
      str += "_3";
    } else if (cur == ')') {
      break;
    } else {
      str += (char)cur;
    }
  }

  if (str.length() > size) return false;
  if (str.compare(compiledName) == 0) return true;

  str += 'S';

  if (str.compare(compiledName) == 0) return true;

  return false;
}

Constant* FindMetadata(const Function& F) {
  LLVMContext& context = F.getParent()->getContext();
  for (Value::const_use_iterator I = F.use_begin(), E = F.use_end(); I != E; ++I) {
    if (const Constant* C = dyn_cast<Constant>(*I)) {
      if (PointerType* PTy = dyn_cast<PointerType>(C->getType())) {
        if (isa<IntegerType>(PTy->getContainedType(0))) {
          // We have found the bitcast constant that casts the method in a i8*
          for (Value::const_use_iterator CI = C->use_begin(), CE = C->use_end(); CI != CE; ++CI) {
            if (StructType* STy = dyn_cast<StructType>((*CI)->getType())) {
              if (STy->getName().equals("JavaMethod")) {
                const Constant* Method = dyn_cast<Constant>(*CI);
                const Constant* Array = dyn_cast<Constant>(*((*CI)->use_begin()));
                Constant* VirtualMethods = dyn_cast<Constant>(const_cast<User*>((*(Array->use_begin()))));
                uint32_t index = 0;
                for (; index < Array->getNumOperands(); index++) {
                  if (Array->getOperand(index) == Method) break;
                }
                assert(index != Array->getNumOperands());
                Constant* GEPs[2] = { ConstantInt::get(Type::getInt32Ty(context), 0),
                                      ConstantInt::get(Type::getInt32Ty(context), index) };
                return ConstantExpr::getGetElementPtr(VirtualMethods, GEPs, 2);
              }
            }
          }
        }
      }
    }
  }

  StringRef name = F.getName();
  if (name.startswith("JnJVM")) {
    // Metadata for customized methods.
    std::string methods = name.substr(0, name.find("__"));
    std::string methodName = name.substr(methods.rfind('_') + 1);
    methodName = methodName.substr(0, methodName.rfind("__"));
    methods = methods.substr(6, methods.rfind('_') - 5);
    methods = methods + "VirtualMethods";
    Constant* VirtualMethods = cast<Constant>(F.getParent()->getNamedValue(methods));
    assert(VirtualMethods);
    Constant* MethodsArray = cast<Constant>(VirtualMethods->getOperand(0));
    for (uint32_t index = 0; index < MethodsArray->getNumOperands(); index++) {
      Constant* method = cast<Constant>(MethodsArray->getOperand(index));

      Constant* name = cast<ConstantExpr>(method->getOperand(5));
      name = cast<Constant>(name->getOperand(0));
      name = cast<Constant>(name->getOperand(0));
      name = cast<Constant>(name->getOperand(1));

      Constant* type = cast<ConstantExpr>(method->getOperand(6));
      type = cast<Constant>(type->getOperand(0));
      type = cast<Constant>(type->getOperand(0));
      type = cast<Constant>(type->getOperand(1));

      if (methodNameMatches(methodName, name, type)) {
        Constant* GEPs[2] = { ConstantInt::get(Type::getInt32Ty(context), 0),
                              ConstantInt::get(Type::getInt32Ty(context), index) };
        return ConstantExpr::getGetElementPtr(VirtualMethods, GEPs, 2);
      }
    }
    assert(0 && "Should have found a JavaMethod");
  }
  return NULL;
}

/// emitAssembly - Print the frametable. The ocaml frametable format is thus:
///
///   extern "C" struct align(sizeof(word_t)) {
///     uint32_t NumDescriptors;
///     struct align(sizeof(word_t)) {
///       void *ReturnAddress;
///       void *Metadata;
///       uint16_t BytecodeIndex; 
///       uint16_t FrameSize;
///       uint16_t NumLiveOffsets;
///       uint16_t LiveOffsets[NumLiveOffsets];
///     } Descriptors[NumDescriptors];
///   } vmkit${module}__frametable;
///
/// Note that this precludes programs from stack frames larger than 64K
/// (FrameSize and LiveOffsets would overflow). FrameTablePrinter will abort if
/// either condition is detected in a function which uses the GC.
///
void VmkitAOTGCMetadataPrinter::finishAssembly(AsmPrinter &AP) {
  unsigned IntPtrSize = AP.TM.getTargetData()->getPointerSize();

  AP.OutStreamer.SwitchSection(AP.getObjFileLowering().getDataSection());

  AP.EmitAlignment(IntPtrSize == 4 ? 2 : 3);
  EmitVmkitGlobal(getModule(), AP, "frametable");
  int NumMethodFrames = 0;
  for (iterator I = begin(), IE = end(); I != IE; ++I) {
    NumMethodFrames++;
  }
  AP.EmitInt32(NumMethodFrames);
  AP.EmitAlignment(IntPtrSize == 4 ? 2 : 3);

  for (iterator I = begin(), IE = end(); I != IE; ++I) {
    GCFunctionInfo &FI = **I;

    Constant* Metadata = FindMetadata(FI.getFunction());

    int NumDescriptors = 0;
    for (GCFunctionInfo::iterator J = FI.begin(), JE = FI.end(); J != JE; ++J) {
      NumDescriptors++;
    }
    if (NumDescriptors >= 1<<16) {
      // Very rude!
      report_fatal_error(" Too much descriptor for J3 AOT GC");
    }
    AP.EmitInt32(NumDescriptors);
    AP.EmitAlignment(IntPtrSize == 4 ? 2 : 3);

    uint64_t FrameSize = FI.getFrameSize();
    if (FrameSize >= 1<<16) {
      // Very rude!
      report_fatal_error("Function '" + FI.getFunction().getName() +
                         "' is too large for the Vmkit AOT GC! "
                         "Frame size " + Twine(FrameSize) + ">= 65536.\n"
                         "(" + Twine(uintptr_t(&FI)) + ")");
    }

    AP.OutStreamer.AddComment("live roots for " +
                              Twine(FI.getFunction().getName()));
    AP.OutStreamer.AddBlankLine();

    for (GCFunctionInfo::iterator J = FI.begin(), JE = FI.end(); J != JE; ++J) {
      size_t LiveCount = FI.live_size(J);
      if (LiveCount >= 1<<16) {
        // Very rude!
        report_fatal_error("Function '" + FI.getFunction().getName() +
                           "' is too large for the Vmkit AOT GC! "
                           "Live root count "+Twine(LiveCount)+" >= 65536.");
      }

      DebugLoc DL = J->Loc;
      uint32_t sourceIndex = DL.getLine();

      // Metadata
      if (Metadata != NULL) {
        AP.EmitGlobalConstant(Metadata);
      } else {
        AP.EmitInt32(0);
        if (IntPtrSize == 8) {
          AP.EmitInt32(0);
        }
      }

      // Return address
      AP.OutStreamer.EmitSymbolValue(J->Label, IntPtrSize, 0);
      AP.EmitInt16(sourceIndex);
      AP.EmitInt16(FrameSize);
      AP.EmitInt16(LiveCount);

      for (GCFunctionInfo::live_iterator K = FI.live_begin(J),
                                         KE = FI.live_end(J); K != KE; ++K) {
        if (K->StackOffset >= 1<<16) {
          // Very rude!
          report_fatal_error(
                 "GC root stack offset is outside of fixed stack frame and out "
                 "of range for ocaml GC!");
        }
        AP.EmitInt16(K->StackOffset);
      }

      AP.EmitAlignment(IntPtrSize == 4 ? 2 : 3);
    }
  }
}
