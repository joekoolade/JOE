/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.compilers.baseline.ia32;

import static org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl.FIVE_SLOTS;
import static org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl.FOUR_SLOTS;
import static org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl.NO_SLOT;
import static org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl.ONE_SLOT;
import static org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl.THREE_SLOTS;
import static org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl.TWO_SLOTS;
import static org.jikesrvm.ia32.ArchConstants.SSE2_BASE;
import static org.jikesrvm.ia32.ArchConstants.SSE2_FULL;
import static org.jikesrvm.ia32.BaselineConstants.EBX_SAVE_OFFSET;
import static org.jikesrvm.ia32.BaselineConstants.EDI_SAVE_OFFSET;
import static org.jikesrvm.ia32.BaselineConstants.FPU_SAVE_OFFSET;
import static org.jikesrvm.ia32.BaselineConstants.LG_WORDSIZE;
import static org.jikesrvm.ia32.BaselineConstants.TR;
import static org.jikesrvm.ia32.BaselineConstants.S0;
import static org.jikesrvm.ia32.BaselineConstants.SP;
import static org.jikesrvm.ia32.BaselineConstants.T0;
import static org.jikesrvm.ia32.BaselineConstants.T0_SAVE_OFFSET;
import static org.jikesrvm.ia32.BaselineConstants.T1;
import static org.jikesrvm.ia32.BaselineConstants.T1_SAVE_OFFSET;
import static org.jikesrvm.ia32.BaselineConstants.WORDSIZE;
import static org.jikesrvm.ia32.BaselineConstants.XMM_SAVE_OFFSET;
import static org.jikesrvm.ia32.RegisterConstants.EAX;
import static org.jikesrvm.ia32.RegisterConstants.EBX;
import static org.jikesrvm.ia32.RegisterConstants.ECX;
import static org.jikesrvm.ia32.RegisterConstants.EDI;
import static org.jikesrvm.ia32.RegisterConstants.EDX;
import static org.jikesrvm.ia32.RegisterConstants.ESI;
import static org.jikesrvm.ia32.RegisterConstants.ESP;
import static org.jikesrvm.ia32.RegisterConstants.EBP;
import static org.jikesrvm.ia32.RegisterConstants.FP0;
import static org.jikesrvm.ia32.RegisterConstants.XMM0;
import static org.jikesrvm.ia32.RegisterConstants.XMM1;
import static org.jikesrvm.ia32.RegisterConstants.XMM2;
import static org.jikesrvm.ia32.RegisterConstants.XMM3;
import static org.jikesrvm.ia32.RegisterConstants.CS;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_FRAME_POINTER_OFFSET;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_METHOD_ID_OFFSET;
import static org.jikesrvm.ia32.StackframeLayoutConstants.STACKFRAME_RETURN_ADDRESS_OFFSET;
import static org.jikesrvm.objectmodel.TIBLayoutConstants.TIB_TYPE_INDEX;
import static org.jikesrvm.runtime.EntrypointHelper.getMethodReference;
import static org.jikesrvm.runtime.EntrypointHelper.getField;

import org.jikesrvm.VM;
import org.jikesrvm.ArchitectureSpecific.Assembler;
import org.jikesrvm.ArchitectureSpecific.CodeArray;
import org.jikesrvm.ArchitectureSpecific.Registers;
import org.jikesrvm.ArchitectureSpecific.StackframeLayoutConstants;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.MethodReference;
import org.jikesrvm.classloader.RVMArray;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.common.assembler.ForwardReference;
import org.jikesrvm.ia32.RegisterConstants.GPR;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.objectmodel.IMT;
import org.jikesrvm.objectmodel.JavaHeader;
import org.jikesrvm.objectmodel.ObjectModel;
import org.jikesrvm.objectmodel.TIB;
import org.jikesrvm.runtime.ArchEntrypoints;
import org.jikesrvm.runtime.EntrypointHelper;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.MagicNames;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.util.ImmutableEntryHashMapRVM;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.AddressArray;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ExtentArray;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.ObjectReferenceArray;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.OffsetArray;
import org.vmmagic.unboxed.Word;
import org.vmmagic.unboxed.WordArray;

/**
 * Create magic code
 */
final class BaselineMagic {
  /**
   * Map of method references to objects that will generate the necessary magic
   */
  private static final ImmutableEntryHashMapRVM<MethodReference,MagicGenerator> generators =
    new ImmutableEntryHashMapRVM<MethodReference,MagicGenerator>();

  /**
   * When casting or loading object references should the reference be checked
   * to see if it is an object reference first?
   */
  private static final boolean VALIDATE_OBJECT_REFERENCES = false;

  /**
   * If a bad reference is encountered should we halt the VM?
   */
  private static final boolean FAIL_ON_BAD_REFERENCES = true;

  /**
   * Entry point to generating magic
   * @param asm assembler to generate magic code into
   * @param m method reference
   * @param cm the method being compiled
   * @param sd the depth of the stack
   * @return {@code true} if magic was generated
   */
  static boolean generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
    MagicGenerator g = generators.get(m);
    if (g != null) {
      g.generateMagic(asm, m, cm, sd);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Flag to avoid recursive calls to check
   */
  private static volatile boolean inCheck = false;
  /**
   * Method called to check an object reference is valid
   * @param value
   */
  @SuppressWarnings("unused")
  @Uninterruptible
  private static void check(ObjectReference value) {
    if (!inCheck) {
      inCheck = true;
      if (!MemoryManager.validRef(value) && FAIL_ON_BAD_REFERENCES) {
        VM.sysFail("Bad object reference encountered");
      }
      inCheck = false;
    }
  }

  /**
   * Reference of method that checks a reference
   */
  private static final MethodReference checkMR =
    EntrypointHelper.getMethodReference(BaselineMagic.class,
        Atom.findOrCreateUnicodeAtom("check"), ObjectReference.class, void.class);

  /**
   * Parent of all magic generating classes
   */
  private abstract static class MagicGenerator {
    abstract void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd);
  }

  /**
   * Add a reference check to a magic generator
   */
  private static final class EarlyReferenceCheckDecorator extends MagicGenerator {
    private final Offset offset;
    private final MagicGenerator generator;
    /**
     * Construct decorator that will add reference checks
     * @param offset on stack of reference to check
     * @param generator the magic generator being decorated
     */
    EarlyReferenceCheckDecorator(Offset offset, MagicGenerator generator) {
      this.offset = offset;
      this.generator = generator;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Class<?> dc = cm.getDeclaringClass().getClassForType();
      if ((dc != JavaHeader.class) &&
          (dc != ObjectModel.class)
      ){
        if (checkMR.needsDynamicLink(cm)) {
            System.err.println("EarlyReferenceCheckDecorator: "+m.toString()+" "+cm.toString());
          BaselineCompilerImpl.emitDynamicLinkingSequence(asm, S0, checkMR, true);
          if (offset.NE(NO_SLOT)) {
            asm.emitMOV_Reg_RegDisp(T0, SP, offset);
          } else {
            asm.emitMOV_Reg_RegInd(T0, SP);
          }
          asm.emitPUSH_Reg(T0);
          asm.emitCALL_RegDisp(S0, Magic.getTocPointer().toWord().toOffset());
        } else {
          if (offset.NE(NO_SLOT)) {
            asm.emitMOV_Reg_RegDisp(T0, SP, offset);
          } else {
            asm.emitMOV_Reg_RegInd(T0, SP);
          }
          asm.emitPUSH_Reg(T0);
          asm.emitCALL_Abs(Magic.getTocPointer().plus(checkMR.peekResolvedMethod().getOffset()));
        }
      }
      generator.generateMagic(asm, m, cm, sd);
    }
  }

  /**
   * Add a reference check to a magic generator
   */
  private static final class LateReferenceCheckDecorator extends MagicGenerator {
    private final Offset offset;
    private final MagicGenerator generator;
    /**
     * Construct decorator that will add reference checks
     * @param offset on stack of reference to check
     * @param generator the magic generator being decorated
     */
    LateReferenceCheckDecorator(Offset offset, MagicGenerator generator) {
      this.offset = offset;
      this.generator = generator;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      generator.generateMagic(asm, m, cm, sd);
      Class<?> dc = cm.getDeclaringClass().getClassForType();
      if ((dc != JavaHeader.class) &&
          (dc != ObjectModel.class)
      ){
        if (checkMR.needsDynamicLink(cm)) {
            System.err.println("LateReferenceCheckDecorator: "+m.toString()+" "+cm.toString());
          BaselineCompilerImpl.emitDynamicLinkingSequence(asm, S0, checkMR, true);
          if (offset.NE(NO_SLOT)) {
            asm.emitMOV_Reg_RegDisp(T0, SP, offset);
          } else {
            asm.emitMOV_Reg_RegInd(T0, SP);
          }
          asm.emitPUSH_Reg(T0);
          asm.emitCALL_RegDisp(S0, Magic.getTocPointer().toWord().toOffset());
        } else {
          if (offset.NE(NO_SLOT)) {
            asm.emitMOV_Reg_RegDisp(T0, SP, offset);
          } else {
            asm.emitMOV_Reg_RegInd(T0, SP);
          }
          asm.emitPUSH_Reg(T0);
          asm.emitCALL_Abs(Magic.getTocPointer().plus(checkMR.peekResolvedMethod().getOffset()));
        }
      }
    }
  }

  /**
   * Load a 32bit quantity from an address
   */
  private static final class Load32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                      // address
      asm.emitPUSH_RegInd(T0);                  // pushes [T0+0]
    }
  }
  static {
    MagicGenerator g = new Load32();
    generators.put(getMethodReference(Address.class, MagicNames.loadAddress, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareAddress, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadWord, Word.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareWord, Word.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadInt, int.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareInt, int.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadFloat, float.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new LateReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(Address.class, MagicNames.prepareObjectReference, ObjectReference.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadObjectReference, ObjectReference.class), g);
  }

  private static final class IOLoad32 extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T1);	// IO address
		  asm.emitIN32();
		  asm.emitPUSH_Reg(T0);	// int value
	  }
  }
  
  static {
	  MagicGenerator g = new IOLoad32();
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadInt, int.class), g);
  }
  
  private static final class IOLoad32_offset extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(S0);	// offset
		  asm.emitPOP_Reg(T1);	// IO address
		  asm.emitADD_Reg_Reg(T1, S0);
		  asm.emitIN32();
		  asm.emitPUSH_Reg(T0);	// int value
	  }
  }
  
  static {
	  MagicGenerator g = new IOLoad32_offset();
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadInt, Offset.class, int.class), g);
  }
  
  private static final class IOLoad16 extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T1);	// IO address
		  asm.emitIN16();
		  asm.emitPUSH_Reg(T0);	// short value
	  }
  }
  
  static {
	  MagicGenerator g = new IOLoad16();
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadShort, short.class), g);
  }
  
  private static final class IOLoad16_offset extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(S0);	// offset
		  asm.emitPOP_Reg(T1);	// IO address
		  asm.emitADD_Reg_Reg(T1, S0);
		  asm.emitIN16();
		  asm.emitPUSH_Reg(T0);	// int value
	  }
  }
  
  static {
	  MagicGenerator g = new IOLoad16_offset();
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadShort, Offset.class, short.class), g);
  }
  
  private static final class IOLoad8 extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T1);	// IO address
		  asm.emitIN8();
		  asm.emitPUSH_Reg(T0);	// short value
	  }
  }
  
  static {
	  MagicGenerator g = new IOLoad8();
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadByte, byte.class), g);
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadChar, char.class), g);
  }
  
  private static final class IOLoad8_offset extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(S0);	// offset
		  asm.emitPOP_Reg(T1);	// IO address
		  asm.emitADD_Reg_Reg(T1, S0);
		  asm.emitIN8();
		  asm.emitPUSH_Reg(T0);	// byte value
	  }
  }
  
  static {
	  MagicGenerator g = new IOLoad8_offset();
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadByte, Offset.class, byte.class), g);
	  generators.put(getMethodReference(Address.class, MagicNames.ioLoadChar, Offset.class, char.class), g);
  }
  
 /**
   * Load a 32bit quantity from an address and offset parameter
   */
  private static final class Load32_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // object ref
      asm.emitPUSH_RegIdx(T0, S0, Assembler.BYTE, NO_SLOT); // pushes [T0+S0]
    }
  }
  static {
    MagicGenerator g = new Load32_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.loadAddress, Offset.class, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareAddress, Offset.class, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadWord, Offset.class, Word.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareWord, Offset.class, Word.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadInt, Offset.class, int.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareInt, Offset.class, int.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadFloat, Offset.class, float.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getIntAtOffset, Object.class, Offset.class, int.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getFloatAtOffset, Object.class, Offset.class, float.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getWordAtOffset, Object.class, Offset.class, Word.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getAddressAtOffset, Object.class, Offset.class, Address.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getExtentAtOffset, Object.class, Offset.class, Extent.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getOffsetAtOffset, Object.class, Offset.class, Offset.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.prepareInt, Object.class, Offset.class, int.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.prepareAddress, Object.class, Offset.class, Address.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.prepareWord, Object.class, Offset.class, Word.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new LateReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(Address.class, MagicNames.prepareObjectReference, Offset.class, ObjectReference.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadObjectReference, Offset.class, ObjectReference.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getObjectAtOffset, Object.class, Offset.class, Object.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getTIBAtOffset, Object.class, Offset.class, TIB.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.prepareObject, Object.class, Offset.class, Object.class), g);
  }

  /**
   * Load a 32bit quantity from an address and offset parameter
   */
  private static final class Magic_Load32_MD extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(S0);                  // discard meta-data
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // object ref
      asm.emitPUSH_RegIdx(T0, S0, Assembler.BYTE, NO_SLOT); // pushes [T0+S0]
    }
  }
  static {
    MagicGenerator g = new Magic_Load32_MD();
    generators.put(getMethodReference(Magic.class, MagicNames.getWordAtOffset, Object.class, Offset.class, int.class, Word.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getAddressAtOffset, Object.class, Offset.class, int.class, Address.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getExtentAtOffset, Object.class, Offset.class, int.class, Extent.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getOffsetAtOffset, Object.class, Offset.class, int.class, Offset.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new LateReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(Magic.class, MagicNames.getObjectAtOffset, Object.class, Offset.class, int.class, Object.class), g);
  }

  /**
   * Load a byte from an address
   */
  private static final class LoadByte extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVSX_Reg_RegInd_Byte(T0, T0);
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    // Load a byte
    MagicGenerator g = new LoadByte();
    generators.put(getMethodReference(Address.class, MagicNames.loadByte, byte.class), g);
  }

  /**
   * Load a byte from an address and offset parameter
   */
  private static final class LoadByte_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVSX_Reg_RegIdx_Byte(T0, T0, S0, Assembler.BYTE, NO_SLOT); // load and sign extend byte [T0+S0]
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new LoadByte_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.loadByte, Offset.class, byte.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getByteAtOffset, Object.class, Offset.class, byte.class), g);
  }

  /**
   * Load an unsigned byte from an address and offset parameter
   */
  private static final class LoadUnsignedByte_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVZX_Reg_RegIdx_Byte(T0, T0, S0, Assembler.BYTE, NO_SLOT); // load and sign extend byte [T0+S0]
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new LoadUnsignedByte_Offset();
    generators.put(getMethodReference(Magic.class, MagicNames.getUnsignedByteAtOffset, Object.class, Offset.class, byte.class), g);
  }

  /**
   * Load a short quantity from an address
   */
  private static final class LoadShort extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVSX_Reg_RegInd_Word(T0, T0);
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new LoadShort();
    generators.put(getMethodReference(Address.class, MagicNames.loadShort, short.class), g);
  }

  /**
   * Load a short quantity from an address plus offset
   */
  private static final class LoadShort_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVSX_Reg_RegIdx_Word(T0, T0, S0, Assembler.BYTE, NO_SLOT); // load and sign extend word [T0+S0]
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new LoadShort_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.loadShort, Offset.class, short.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getShortAtOffset, Object.class, Offset.class, short.class), g);
  }

  /**
   * Load a char from an address
   */
  private static final class LoadChar extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVZX_Reg_RegInd_Word(T0, T0);
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new LoadChar();
    generators.put(getMethodReference(Address.class, MagicNames.loadChar, char.class), g);
  }

  /**
   * Load a char from an address plus offset
   */
  private static final class LoadChar_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // base
      asm.emitMOVZX_Reg_RegIdx_Word(T0, T0, S0, Assembler.BYTE, NO_SLOT); // load and sign extend word [T0+S0]
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new LoadChar_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.loadChar, Offset.class, char.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getCharAtOffset, Object.class, Offset.class, char.class), g);
  }

  /**
   * Load a 64bit quantity from an address
   */
  private static final class Load64 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                  // base
      if (VM.BuildFor32Addr) {
        asm.emitPUSH_RegDisp(T0, ONE_SLOT); // pushes [T0+4]
        asm.emitPUSH_RegInd(T0);            // pushes [T0]
      } else {
        asm.emitPUSH_Reg(T0);               // create space
        asm.emitPUSH_RegInd(T0);            // pushes [T0]
      }
    }
  }
  static {
    MagicGenerator g = new Load64();
    generators.put(getMethodReference(Address.class, MagicNames.loadDouble, double.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadLong, long.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareLong, long.class), g);
  }

  /**
   * Load a 32bit quantity from an address plus offset
   */
  private static final class Load64_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Load at offset
      asm.emitPOP_Reg(S0);                  // offset
      asm.emitPOP_Reg(T0);                  // base
      if (VM.BuildFor32Addr) {
        asm.emitPUSH_RegIdx(T0, S0, Assembler.BYTE, ONE_SLOT); // pushes [T0+S0+4]
        asm.emitPUSH_RegIdx(T0, S0, Assembler.BYTE, NO_SLOT);  // pushes [T0+S0]
      } else {
        asm.emitPUSH_Reg(T0);                                  // create space
        asm.emitPUSH_RegIdx(T0, S0, Assembler.BYTE, NO_SLOT);  // pushes [T0+S0]
      }
    }
  }
  static {
    MagicGenerator g = new Load64_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.loadDouble, Offset.class, double.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.loadLong, Offset.class, long.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prepareLong, Offset.class, long.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getDoubleAtOffset, Object.class, Offset.class, double.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getLongAtOffset, Object.class, Offset.class, long.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.prepareLong, Object.class, Offset.class, long.class), g);
  }

  /**
   * Store a 32bit quantity to an IO space address
   */
  private static final class IOStore32 extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T0);		// int value
		  asm.emitPOP_Reg(T1);		// IO address
		  asm.emitOUT32();
	  }
  }
  
  static {
	  MagicGenerator g = new IOStore32();
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, int.class, void.class), g);
  }

  /**
   * Store a 32bit quantity to an IO space address + offset
   */
  private static final class IOStore32_offset extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T0);		// int value
		  asm.emitPOP_Reg(S0);		// offset
		  asm.emitPOP_Reg(T1);		// IO address
		  asm.emitADD_Reg_Reg(T1, S0);	// IO address + offset
		  asm.emitOUT32();
	  }
  }
  
  static {
	  MagicGenerator g = new IOStore32_offset();
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, Offset.class, int.class, void.class), g);
  }

  /**
   * Store a 16bit quantity to an IO space address
   */
  private static final class IOStore16 extends MagicGenerator 
  {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) 
	  {
		  asm.emitPOP_Reg(T0);		// short value
		  asm.emitPOP_Reg(T1);		// IO address
		  asm.emitOUT16();
	  }
  }
  
  static 
  {
	  MagicGenerator g = new IOStore16();
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, short.class, void.class), g);
  }
  
  /**
   * Store a 32bit quantity to an IO space address + offset
   */
  private static final class IOStore16_offset extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T0);		// short value
		  asm.emitPOP_Reg(S0);		// offset
		  asm.emitPOP_Reg(T1);		// IO address
		  asm.emitADD_Reg_Reg(T1, S0);	// IO address + offset
		  asm.emitOUT16();
	  }
  }
  
  static {
	  MagicGenerator g = new IOStore16_offset();
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, Offset.class, short.class, void.class), g);
  }

  private static final class IOStore8 extends MagicGenerator 
  {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
	  {
		  asm.emitPOP_Reg(T0); // byte value
		  asm.emitPOP_Reg(T1); // IO address
		  asm.emitOUT8();
	  }
  }
  
  static
  {
	  MagicGenerator g = new IOStore8();
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, byte.class, void.class), g);
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, char.class, void.class), g);
  }

  /**
   * Store a 32bit quantity to an IO space address + offset
   */
  private static final class IOStore8_offset extends MagicGenerator {
	  @Override
	  void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
		  asm.emitPOP_Reg(T0);		// byte value
		  asm.emitPOP_Reg(S0);		// offset
		  asm.emitPOP_Reg(T1);		// IO address
		  asm.emitADD_Reg_Reg(T1, S0);	// IO address + offset
		  asm.emitOUT16();
	  }
  }
  
  static {
	  MagicGenerator g = new IOStore8_offset();
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, Offset.class, byte.class, void.class), g);
	  generators.put(getMethodReference(Address.class, MagicNames.ioStore, Offset.class, char.class, void.class), g);
  }

  /**
   * Store a 32bit quantity to an address
   */
  private static final class Store32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // address
      asm.emitMOV_RegInd_Reg(S0, T0);         // [S0+0] <- T0
    }
  }
  static {
    MagicGenerator g = new Store32();
    generators.put(getMethodReference(Address.class, MagicNames.store, Address.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, Word.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, int.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, float.class, void.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(Address.class, MagicNames.store, ObjectReference.class, void.class), g);
  }

  /**
   * Store a 32bit quantity to an address plus offset
   */
  private static final class Store32_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Store at offset
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(T1);                   // address
      asm.emitMOV_RegIdx_Reg(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- T0
    }
  }
  static {
    MagicGenerator g = new Store32_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.store, Address.class, Offset.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, Word.class, Offset.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, int.class, Offset.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, float.class, Offset.class, void.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(ONE_SLOT, g);
    }
    generators.put(getMethodReference(Address.class, MagicNames.store, ObjectReference.class, Offset.class, void.class), g);
  }

  /**
   * Store a 32bit quantity to an address plus offset in the format used in
   * {@link Magic}
   */
  private static final class Magic_Store32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T1);                   // obj ref
      asm.emitMOV_RegIdx_Reg(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- T0
    }
  }
  static {
    MagicGenerator g = new Magic_Store32();
    generators.put(getMethodReference(Magic.class, MagicNames.setIntAtOffset, Object.class, Offset.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setFloatAtOffset, Object.class, Offset.class, float.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setWordAtOffset, Object.class, Offset.class, Word.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setAddressAtOffset, Object.class, Offset.class, Address.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setExtentAtOffset, Object.class, Offset.class, Extent.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setOffsetAtOffset, Object.class, Offset.class, Offset.class, void.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(Magic.class, MagicNames.setObjectAtOffset, Object.class, Offset.class, Object.class, void.class), g);
  }

  /**
   * Store a 32bit quantity to an address plus offset in the format used in
   * {@link Magic} with an additional meta-data argument
   */
  private static final class Magic_Store32_MD extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // discard meta-data
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T1);                   // obj ref
      asm.emitMOV_RegIdx_Reg(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- T0
    }
  }
  static {
    MagicGenerator g = new Magic_Store32_MD();
    generators.put(getMethodReference(Magic.class, MagicNames.setIntAtOffset, Object.class, Offset.class, int.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setWordAtOffset, Object.class, Offset.class, Word.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setAddressAtOffset, Object.class, Offset.class, Address.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setOffsetAtOffset, Object.class, Offset.class, Offset.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setExtentAtOffset, Object.class, Offset.class, Extent.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setFloatAtOffset, Object.class, Offset.class, float.class, int.class, void.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(ONE_SLOT, g);
    }
    generators.put(getMethodReference(Magic.class, MagicNames.setObjectAtOffset, Object.class, Offset.class, Object.class, int.class, void.class), g);
  }

  /**
   * Store a 8bit quantity to an address plus offset
   */
  private static final class Store8 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(T1);                   // base
      asm.emitMOV_RegInd_Reg_Byte(T1, T0);
    }
  }
  static {
    MagicGenerator g = new Store8();
    generators.put(getMethodReference(Address.class, MagicNames.store, byte.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, boolean.class, void.class), g);
  }

  /**
   * Store a 8bit quantity to an address plus offset
   */
  private static final class Store8_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Store at offset
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(T1);                   // base
      asm.emitMOV_RegIdx_Reg_Byte(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- (byte) T0
    }
  }
  static {
    MagicGenerator g = new Store8_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.store, byte.class, Offset.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, boolean.class, Offset.class, void.class), g);
  }

  /**
   * Store a 8bit quantity to an address plus offset in the format used in {@link Magic}
   */
  private static final class Magic_Store8 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T1);                   // obj ref
      asm.emitMOV_RegIdx_Reg_Byte(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- (byte) T0
    }
  }
  static {
    MagicGenerator g = new Magic_Store8();
    generators.put(getMethodReference(Magic.class, MagicNames.setBooleanAtOffset, Object.class, Offset.class, boolean.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setByteAtOffset, Object.class, Offset.class, byte.class, void.class), g);
  }

  /**
   * Store a 8bit quantity to an address plus offset in the format used in
   * {@link Magic} with an additional meta-data argument
   */
  private static final class Magic_Store8_MD extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // discard meta-data
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T1);                   // obj ref
      asm.emitMOV_RegIdx_Reg_Byte(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- (byte) T0
    }
  }
  static {
    MagicGenerator g = new Magic_Store8_MD();
    generators.put(getMethodReference(Magic.class, MagicNames.setBooleanAtOffset, Object.class, Offset.class, boolean.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setByteAtOffset, Object.class, Offset.class, byte.class, int.class, void.class), g);
  }

  /**
   * Store a 16bit quantity to an address
   */
  private static final class Store16 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(T1);                   // base
      asm.emitMOV_RegInd_Reg_Word(T1, T0);
    }
  }
  static {
    MagicGenerator g = new Store16();
    generators.put(getMethodReference(Address.class, MagicNames.store, short.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, char.class, void.class), g);
  }

  /**
   * Store a 16bit quantity to an address plus offset
   */
  private static final class Store16_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Store at offset
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(T1);                   // base
      asm.emitMOV_RegIdx_Reg_Word(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- (word) T0
    }
  }
  static {
    MagicGenerator g = new Store16_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.store, short.class, Offset.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, char.class, Offset.class, void.class), g);
  }

  /**
   * Store a 16 bit quantity to an address plus offset in the format used in {@link Magic}
   */
  private static final class Magic_Store16 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T1);                   // obj ref
      asm.emitMOV_RegIdx_Reg_Word(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- (word) T0
    }
  }
  static {
    MagicGenerator g = new Magic_Store16();
    generators.put(getMethodReference(Magic.class, MagicNames.setCharAtOffset, Object.class, Offset.class, char.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setShortAtOffset, Object.class, Offset.class, short.class, void.class), g);
  }

  /**
   * Store a 16bit quantity to an address plus offset in the format used in
   * {@link Magic} with an additional meta-data argument
   */
  private static final class Magic_Store16_MD extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // discard meta-data
      asm.emitPOP_Reg(T0);                   // value
      asm.emitPOP_Reg(S0);                   // offset
      asm.emitPOP_Reg(T1);                   // obj ref
      asm.emitMOV_RegIdx_Reg_Word(T1, S0, Assembler.BYTE, NO_SLOT, T0); // [T1+S0] <- (word) T0
    }
  }
  static {
    MagicGenerator g = new Magic_Store16_MD();
    generators.put(getMethodReference(Magic.class, MagicNames.setCharAtOffset, Object.class, Offset.class, char.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setShortAtOffset, Object.class, Offset.class, short.class, int.class, void.class), g);
  }

  /**
   * Store a 64bit quantity to an address
   */
  private static final class Store64 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // No offset
      if (VM.BuildFor32Addr) {
        asm.emitPOP_Reg(T0); // value low
        asm.emitPOP_Reg(T1); // value high
        asm.emitPOP_Reg(S0); // base
        asm.emitMOV_RegInd_Reg(S0, T0);            // value low
        asm.emitMOV_RegDisp_Reg(S0, ONE_SLOT, T1); // value high
      } else {
        asm.emitPOP_Reg(T0); // value
        asm.emitPOP_Reg(T1); // throw away slot
        asm.emitPOP_Reg(T1); // base
        asm.emitMOV_RegInd_Reg_Quad(T1, T0);
      }
    }
  }
  static {
    MagicGenerator g = new Store64();
    generators.put(getMethodReference(Address.class, MagicNames.store, long.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, double.class, void.class), g);
  }

  /**
   * Store a 64bit quantity to an address plus offset
   */
  private static final class Store64_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Store at offset
      if (VM.BuildFor32Addr) {
        asm.emitPOP_Reg(T0);                          // T0 = offset
        asm.emitADD_Reg_RegDisp(T0, SP, TWO_SLOTS); // T0 = base+offset
        asm.emitPOP_RegInd(T0);                       // [T0]   <- value low
        asm.emitPOP_RegDisp(T0, ONE_SLOT);            // [T0+4] <- value high
        asm.emitPOP_Reg(T0);                          // throw away slot
      } else {
        asm.emitPOP_Reg(T0);                               // offset
        asm.emitADD_Reg_RegDisp_Quad(T0, SP, TWO_SLOTS); // T0 = base+offset
        asm.emitPOP_RegInd(T0);                            // T0 <- value
        asm.emitPOP_Reg(T0);                               // throw away slot
        asm.emitPOP_Reg(T0);                               // throw away slot
      }
    }
  }
  static {
    MagicGenerator g = new Store64_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.store, long.class, Offset.class, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.store, double.class, Offset.class, void.class), g);
  }

  /**
   * Store a 64bit quantity to an address plus offset in the format used in
   * {@link Magic}
   */
  private static final class Magic_Store64 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      if (VM.BuildFor32Addr) {
        asm.emitPOP_Reg(T0);                       // value low
        asm.emitPOP_Reg(T1);                       // value high
        asm.emitPOP_Reg(S0);                       // S0 = offset
        asm.emitADD_Reg_RegInd(S0, SP);            // S0 = base+offset
        asm.emitMOV_RegInd_Reg(S0, T0);            // [S0] <- value low
        asm.emitPOP_Reg(T0);                       // throw away slot
        asm.emitMOV_RegDisp_Reg(S0, ONE_SLOT, T1); // [S0+4] <- value high
      } else {
        asm.emitPOP_Reg(T0);                       // value
        asm.emitPOP_Reg(T1);                       // throw away slot
        asm.emitPOP_Reg(T1);                       // T1 = offset
        asm.emitPOP_Reg(S0);                       // S0 = base
        asm.emitMOV_RegIdx_Reg_Quad(S0, T1, Assembler.BYTE, NO_SLOT, T0); // [base+offset] <- T0
      }
    }
  }
  static {
    MagicGenerator g = new Magic_Store64();
    generators.put(getMethodReference(Magic.class, MagicNames.setLongAtOffset, Object.class, Offset.class, long.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setDoubleAtOffset, Object.class, Offset.class, double.class, void.class), g);
  }

  /**
   * Store a 64bit quantity to an address plus offset in the format used in
   * {@link Magic} with an additional meta-data argument
   */
  private static final class Magic_Store64_MD extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                   // discard meta-data
      if (VM.BuildFor32Addr) {
        asm.emitPOP_Reg(T0);                       // value low
        asm.emitPOP_Reg(T1);                       // value high
        asm.emitPOP_Reg(S0);                       // S0 = offset
        asm.emitADD_Reg_RegInd(S0, SP);            // S0 = base+offset
        asm.emitMOV_RegInd_Reg(S0, T0);            // [S0] <- value low
        asm.emitPOP_Reg(T0);                       // throw away slot
        asm.emitMOV_RegDisp_Reg(S0, ONE_SLOT, T1); // [S0+4] <- value high
      } else {
        asm.emitPOP_Reg(T0);                       // value
        asm.emitPOP_Reg(T1);                       // throw away slot
        asm.emitPOP_Reg(T1);                       // T1 = offset
        asm.emitPOP_Reg(S0);                       // S0 = base
        asm.emitMOV_RegIdx_Reg_Quad(S0, T1, Assembler.BYTE, NO_SLOT, T0); // [base+offset] <- T0
      }
    }
  }
  static {
    MagicGenerator g = new Magic_Store64_MD();
    generators.put(getMethodReference(Magic.class, MagicNames.setLongAtOffset, Object.class, Offset.class, long.class, int.class, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.setDoubleAtOffset, Object.class, Offset.class, double.class, int.class, void.class), g);
  }

  /**
   * Compare and swap a 32bit value
   */
  private static final class Attempt32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T1);          // newVal
      asm.emitPOP_Reg(EAX);         // oldVal (EAX is implicit arg to LCMPX
      // No offset
      asm.emitMOV_Reg_RegInd(S0, SP);  // S0 = base
      asm.emitLockNextInstruction();
      asm.emitCMPXCHG_RegInd_Reg(S0, T1);   // atomic compare-and-exchange
      asm.emitMOV_RegInd_Imm(SP, 1);        // 'push' true (overwriting base)
      asm.emitBranchLikelyNextInstruction();
      ForwardReference fr = asm.forwardJcc(Assembler.EQ); // skip if compare fails
      asm.emitMOV_RegInd_Imm(SP, 0);        // 'push' false (overwriting base)
      fr.resolve(asm);
    }
  }
  static {
    MagicGenerator g = new Attempt32();
    generators.put(getMethodReference(Address.class, MagicNames.attempt, Address.class, Address.class, boolean.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.attempt, Word.class, Word.class, boolean.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.attempt, int.class, int.class, boolean.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(NO_SLOT, g);
      g = new EarlyReferenceCheckDecorator(ONE_SLOT, g);
    }
    generators.put(getMethodReference(Address.class, MagicNames.attempt, ObjectReference.class, ObjectReference.class, boolean.class), g);
  }

  /**
   * Compare and swap a 32bit value at an address plus offset
   */
  private static final class Attempt32_Offset extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // Offset passed
      asm.emitPOP_Reg(S0);        // S0 = offset
      asm.emitPOP_Reg(T1);          // newVal
      asm.emitPOP_Reg(EAX);         // oldVal (EAX is implicit arg to LCMPX
      asm.emitADD_Reg_RegInd(S0, SP);  // S0 += base
      asm.emitLockNextInstruction();
      asm.emitCMPXCHG_RegInd_Reg(S0, T1);   // atomic compare-and-exchange
      asm.emitMOV_RegInd_Imm(SP, 1);        // 'push' true (overwriting base)
      asm.emitBranchLikelyNextInstruction();
      ForwardReference fr = asm.forwardJcc(Assembler.EQ); // skip if compare fails
      asm.emitMOV_RegInd_Imm(SP, 0);        // 'push' false (overwriting base)
      fr.resolve(asm);
    }
  }
  static {
    MagicGenerator g = new Attempt32_Offset();
    generators.put(getMethodReference(Address.class, MagicNames.attempt, Address.class, Address.class, Offset.class, boolean.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.attempt, Word.class, Word.class, Offset.class, boolean.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.attempt, int.class, int.class, Offset.class, boolean.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(ONE_SLOT, g);
      g = new EarlyReferenceCheckDecorator(TWO_SLOTS, g);
    }
    generators.put(getMethodReference(Address.class, MagicNames.attempt, ObjectReference.class, ObjectReference.class, Offset.class, boolean.class), g);
  }

  /**
   * Compare and swap a 32bit value in the format used in {@link Magic}
   */
  private static final class Magic_Attempt32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // attempt gets called with four arguments: base, offset, oldVal, newVal
      // returns ([base+offset] == oldVal)
      // if ([base+offset] == oldVal) [base+offset] := newVal
      // (operation on memory is atomic)
      asm.emitPOP_Reg(T1);            // newVal
      asm.emitPOP_Reg(EAX);           // oldVal (EAX is implicit arg to LCMPXCNG
      asm.emitPOP_Reg(S0);            // S0 = offset
      asm.emitADD_Reg_RegInd(S0, SP);  // S0 += base
      asm.emitLockNextInstruction();
      asm.emitCMPXCHG_RegInd_Reg(S0, T1);   // atomic compare-and-exchange
      asm.emitMOV_RegInd_Imm(SP, 1);        // 'push' true (overwriting base)
      asm.emitBranchLikelyNextInstruction();
      ForwardReference fr = asm.forwardJcc(Assembler.EQ); // skip if compare fails
      asm.emitMOV_RegInd_Imm(SP, 0);        // 'push' false (overwriting base)
      fr.resolve(asm);
    }
  }
  static {
    MagicGenerator g = new Magic_Attempt32();
    generators.put(getMethodReference(Magic.class, MagicNames.attemptInt, Object.class, Offset.class, int.class, int.class, boolean.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.attemptAddress, Object.class, Offset.class, Address.class, Address.class, boolean.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.attemptWord, Object.class, Offset.class, Word.class, Word.class, boolean.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(NO_SLOT, g);
      g = new EarlyReferenceCheckDecorator(ONE_SLOT, g);
      g = new EarlyReferenceCheckDecorator(THREE_SLOTS, g);
    }
    generators.put(getMethodReference(Magic.class, MagicNames.attemptObject, Object.class, Offset.class, Object.class, Object.class, boolean.class), g);
  }

  /**
   * Compare and swap a 64bit value in the format used in {@link Magic}
   */
  private static final class Magic_Attempt64 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // attempt gets called with four arguments: base, offset, oldVal, newVal
      // returns ([base+offset] == oldVal)
      // if ([base+offset] == oldVal) [base+offset] := newVal
      // (operation on memory is atomic)
      //t1:t0 with s0:ebx
      asm.emitMOV_Reg_RegDisp(T1, SP, THREE_SLOTS);
      asm.emitMOV_Reg_RegDisp(T0, SP, TWO_SLOTS);     // T1:T0 (EDX:EAX) -> oldVal
      asm.emitMOV_RegDisp_Reg(SP, THREE_SLOTS, EBX);  // Save EBX
      asm.emitMOV_RegDisp_Reg(SP, TWO_SLOTS, ESI);    // Save ESI
      asm.emitMOV_Reg_RegInd(EBX, SP);
      asm.emitMOV_Reg_RegDisp(S0, SP, ONE_SLOT);      // S0:EBX (ECX:EBX) -> newVal
      asm.emitMOV_Reg_RegDisp(ESI, SP, FIVE_SLOTS);   // ESI := base
      asm.emitADD_Reg_RegDisp(ESI, SP, FOUR_SLOTS);   // ESI += offset
      asm.emitLockNextInstruction();
      asm.emitCMPXCHG8B_RegInd(ESI);                  // atomic compare-and-exchange
      ForwardReference fr1 = asm.forwardJcc(Assembler.NE); // skip if compare fails
      asm.emitMOV_RegDisp_Imm(SP, FIVE_SLOTS, 1);     // 'push' true (overwriting base)
      ForwardReference fr2 = asm.forwardJMP();     // skip if compare fails
      fr1.resolve(asm);
      asm.emitMOV_RegDisp_Imm(SP, FIVE_SLOTS, 0);     // 'push' false (overwriting base)
      fr2.resolve(asm);
      asm.emitMOV_Reg_RegDisp(EBX, SP, THREE_SLOTS);  // Restore EBX
      asm.emitMOV_Reg_RegDisp(ESI, SP, TWO_SLOTS);    // Restore ESI
      asm.emitADD_Reg_Imm(SP, WORDSIZE*5);            // adjust SP popping the 4 args (6 slots) and pushing the result
    }
  }
  static {
    MagicGenerator g = new Magic_Attempt64();
    generators.put(getMethodReference(Magic.class, MagicNames.attemptLong, Object.class, Offset.class, long.class, long.class, boolean.class), g);
  }

  /**
   * Prefetch from an address
   */
  private static final class Prefetch extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(EDI);
      asm.emitPREFETCHNTA_Reg(EDI);
    }
  }
  static {
    MagicGenerator g = new Prefetch();
    generators.put(getMethodReference(Address.class, MagicNames.prefetch, void.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.prefetchNTA, void.class), g);
  }

  /**
   * Get the type from an object
   */
  private static final class GetObjectType extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);                               // object ref
      BaselineCompilerImpl.baselineEmitLoadTIB(asm, S0, T0);
      asm.emitPUSH_RegDisp(S0, Offset.fromIntZeroExtend(TIB_TYPE_INDEX << LG_WORDSIZE)); // push RVMType slot of TIB
    }
  }
  static {
    MagicGenerator g = new GetObjectType();
    generators.put(getMethodReference(Magic.class, MagicNames.getObjectType, Object.class, RVMType.class), g);
  }

  /**
   * Perform no-operation
   */
  private static final class Nop extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
    }
  }
  static {
    MagicGenerator g = new Nop();
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordFromInt, int.class, type), g);
      if (VM.BuildFor32Addr) {
        generators.put(getMethodReference(type, MagicNames.wordFromIntSignExtend, int.class, type), g);
        generators.put(getMethodReference(type, MagicNames.wordFromIntZeroExtend, int.class, type), g);
      }
      generators.put(getMethodReference(type, MagicNames.wordToInt, int.class), g);
      if (type != Address.class)
        generators.put(getMethodReference(type, MagicNames.wordToAddress, Address.class), g);
      if (type != Extent.class)
        generators.put(getMethodReference(type, MagicNames.wordToExtent, Extent.class), g);
      if (type != Offset.class)
        generators.put(getMethodReference(type, MagicNames.wordToOffset, Offset.class), g);
      if (type != Word.class)
        generators.put(getMethodReference(type, MagicNames.wordToWord, Word.class), g);
    }
    generators.put(getMethodReference(Magic.class, MagicNames.floatAsIntBits, float.class, int.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.intBitsAsFloat, int.class, float.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.doubleAsLongBits, double.class, long.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.longBitsAsDouble, long.class, double.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.sync, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.isync, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.combinedLoadBarrier, void.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.storeStoreBarrier, void.class), g);
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(NO_SLOT, g);
    }
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordFromObject, Object.class, type), g);
      generators.put(getMethodReference(type, MagicNames.wordToObject, Object.class), g);
      generators.put(getMethodReference(type, MagicNames.wordToObjectReference, ObjectReference.class), g);
    }
    generators.put(getMethodReference(ObjectReference.class, MagicNames.wordFromObject, Object.class, ObjectReference.class), g);
    generators.put(getMethodReference(ObjectReference.class, MagicNames.wordToObject, Object.class), g);
    generators.put(getMethodReference(ObjectReference.class, MagicNames.wordToAddress, Address.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.codeArrayAsObject, CodeArray.class, Object.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.tibAsObject, TIB.class, Object.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.objectAsAddress, Object.class, Address.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.addressAsByteArray, Address.class, byte[].class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.addressAsObject, Address.class, Object.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.addressAsTIB, Address.class, TIB.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.objectAsType, Object.class, RVMType.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.objectAsShortArray, Object.class, short[].class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.objectAsIntArray, Object.class, int[].class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.objectAsThread, Object.class, RVMThread.class), g);
  }

  /**
   * Generate the MFENCE instruction.
   */
  private static final class MFence extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitMFENCE();
    }
  }
  static {
    generators.put(getMethodReference(Magic.class, MagicNames.fence, void.class), new MFence());
  }

  /**
   * Perform an operation to release a stack slot
   */
  private static final class FreeStackSlot extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      asm.emitPOP_Reg(T1);
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new FreeStackSlot();
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordFromLong, long.class, type), g);
    }
  }

  /**
   * Perform an operation to duplicate a stack slot
   */
  private static final class DuplicateStackSlot extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      asm.emitPUSH_Reg(T0);
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    if (VM.BuildFor64Addr) {
      MagicGenerator g = new DuplicateStackSlot();
      Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
      for (Class<?> type : unboxedTypes) {
        generators.put(getMethodReference(type, MagicNames.wordToLong, type, long.class), g);
      }
    }
  }

  /**
   * Zero high part of 64bits
   */
  private static final class QuadZeroExtend extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      asm.emitMOV_Reg_Reg(T0, T0);
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    if (VM.BuildFor64Addr) {
      MagicGenerator g = new QuadZeroExtend();
      Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
      for (Class<?> type : unboxedTypes) {
        generators.put(getMethodReference(type, MagicNames.wordFromIntZeroExtend, int.class, type), g);
      }
    }
  }

  /**
   * Sign extend 32bit int to 64bits
   */
  private static final class QuadSignExtend extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(EAX);
      asm.emitCDQE();
      asm.emitPUSH_Reg(EAX);
    }
  }
  static {
    if (VM.BuildFor64Addr) {
      MagicGenerator g = new QuadSignExtend();
      Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
      for (Class<?> type : unboxedTypes) {
        generators.put(getMethodReference(type, MagicNames.wordFromIntSignExtend, int.class, type), g);
      }
    }
  }

  /**
   * Generate an address constant
   */
  private static final class AddressConstant extends MagicGenerator {
    final int value;
    AddressConstant(int value) {
      this.value = value;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPUSH_Imm(value);
    }
  }
  static {
    MagicGenerator zero = new AddressConstant(0);
    MagicGenerator one = new AddressConstant(1);
    MagicGenerator max = new AddressConstant(-1);
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordZero, type), zero);
      generators.put(getMethodReference(type, MagicNames.wordOne, type), one);
      generators.put(getMethodReference(type, MagicNames.wordMax, type), max);
    }
    generators.put(getMethodReference(ObjectReference.class, MagicNames.wordNull, ObjectReference.class), zero);
    MagicGenerator g = new AddressConstant(Magic.getTocPointer().toInt());
    generators.put(getMethodReference(Magic.class, MagicNames.getJTOC, Address.class), g);
    generators.put(getMethodReference(Magic.class, MagicNames.getTocPointer, Address.class), g);
  }

  /**
   * Address comparison
   */
  private static final class AddressComparison extends MagicGenerator {
    final byte comparator;
    AddressComparison(byte comparator) {
      this.comparator = comparator;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(S0);
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitCMP_Reg_Reg(T0, S0);
      } else {
        asm.emitCMP_Reg_Reg_Quad(T0, S0);
      }
      ForwardReference fr1 = asm.forwardJcc(comparator);
      asm.emitPUSH_Imm(0);
      ForwardReference fr2 = asm.forwardJMP();
      fr1.resolve(asm);
      asm.emitPUSH_Imm(1);
      fr2.resolve(asm);
    }
  }
  static {
    MagicGenerator llt = new AddressComparison(Assembler.LLT);
    MagicGenerator lle = new AddressComparison(Assembler.LLE);
    MagicGenerator lgt = new AddressComparison(Assembler.LGT);
    MagicGenerator lge = new AddressComparison(Assembler.LGE);
    MagicGenerator eq = new AddressComparison(Assembler.EQ);
    MagicGenerator ne = new AddressComparison(Assembler.NE);
    // Unsigned unboxed types
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordLT, type, boolean.class), llt);
      generators.put(getMethodReference(type, MagicNames.wordLE, type, boolean.class), lle);
      generators.put(getMethodReference(type, MagicNames.wordGT, type, boolean.class), lgt);
      generators.put(getMethodReference(type, MagicNames.wordGE, type, boolean.class), lge);
      generators.put(getMethodReference(type, MagicNames.wordEQ, type, boolean.class), eq);
      generators.put(getMethodReference(type, MagicNames.wordNE, type, boolean.class), ne);
    }
    MagicGenerator lt = new AddressComparison(Assembler.LT);
    MagicGenerator le = new AddressComparison(Assembler.LE);
    MagicGenerator gt = new AddressComparison(Assembler.GT);
    MagicGenerator ge = new AddressComparison(Assembler.GE);
    // Signed unboxed types
    unboxedTypes = new Class<?>[]{Offset.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordsLT, type, boolean.class), lt);
      generators.put(getMethodReference(type, MagicNames.wordsLE, type, boolean.class), le);
      generators.put(getMethodReference(type, MagicNames.wordsGT, type, boolean.class), gt);
      generators.put(getMethodReference(type, MagicNames.wordsGE, type, boolean.class), ge);
      generators.put(getMethodReference(type, MagicNames.wordEQ, type, boolean.class), eq);
      generators.put(getMethodReference(type, MagicNames.wordNE, type, boolean.class), ne);
    }
  }

  /**
   * Is an address zero?
   */
  private static final class AddressComparison_isZero extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitTEST_Reg_Reg(T0, T0);
      } else {
        asm.emitTEST_Reg_Reg_Quad(T0, T0);
      }
      ForwardReference fr1 = asm.forwardJcc(Assembler.EQ);
      asm.emitPUSH_Imm(0);
      ForwardReference fr2 = asm.forwardJMP();
      fr1.resolve(asm);
      asm.emitPUSH_Imm(1);
      fr2.resolve(asm);
    }
  }
  static {
    MagicGenerator g = new AddressComparison_isZero();
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordIsZero, boolean.class), g);
    }
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new EarlyReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(ObjectReference.class, MagicNames.wordIsNull, boolean.class), g);
  }

  /**
   * Is an address max?
   */
  private static final class AddressComparison_isMax extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitCMP_Reg_Imm(T0, -1);
      } else {
        asm.emitCMP_Reg_Imm_Quad(T0, -1);
      }
      ForwardReference fr1 = asm.forwardJcc(Assembler.EQ);
      asm.emitPUSH_Imm(0);
      ForwardReference fr2 = asm.forwardJMP();
      fr1.resolve(asm);
      asm.emitPUSH_Imm(1);
      fr2.resolve(asm);
    }
  }
  static {
    MagicGenerator g = new AddressComparison_isMax();
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordIsMax, boolean.class), g);
    }
  }

  /**
   * Addition of words
   */
  private static final class WordPlus extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitADD_RegInd_Reg(SP, T0);
      } else {
        asm.emitADD_RegInd_Reg_Quad(SP, T0);
      }
    }
  }
  /**
   * Special case of 64bit addition to 32bit value
   */
  private static final class WordPlus32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(EAX);
      asm.emitCDQE();
      asm.emitADD_RegInd_Reg_Quad(SP, EAX);
    }
  }
  static {
    MagicGenerator g = new WordPlus();
    generators.put(getMethodReference(Address.class, MagicNames.wordPlus, Offset.class, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.wordPlus, Extent.class, Address.class), g);
    generators.put(getMethodReference(Extent.class, MagicNames.wordPlus, Extent.class, Extent.class), g);
    generators.put(getMethodReference(Word.class, MagicNames.wordPlus, Word.class, Word.class), g);
    generators.put(getMethodReference(Word.class, MagicNames.wordPlus, Offset.class, Word.class), g);
    generators.put(getMethodReference(Word.class, MagicNames.wordPlus, Extent.class, Word.class), g);
    if (VM.BuildFor64Addr) {
      g = new WordPlus32();
    }
    generators.put(getMethodReference(Address.class, MagicNames.wordPlus, int.class, Address.class), g);
    generators.put(getMethodReference(Extent.class, MagicNames.wordPlus, int.class, Extent.class), g);
    generators.put(getMethodReference(Offset.class, MagicNames.wordPlus, int.class, Offset.class), g);
  }

  /**
   * Subtraction of words
   */
  private static final class WordMinus extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitSUB_RegInd_Reg(SP, T0);
      } else {
        asm.emitSUB_RegInd_Reg_Quad(SP, T0);
      }
    }
  }
  /**
   * Special case of 64bit subtraction to 32bit value
   */
  private static final class WordMinus32 extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(EAX);
      asm.emitCDQE();
      asm.emitSUB_RegInd_Reg_Quad(SP, EAX);
    }
  }
  static {
    MagicGenerator g = new WordMinus();
    generators.put(getMethodReference(Address.class, MagicNames.wordMinus, Offset.class, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.wordMinus, Extent.class, Address.class), g);
    generators.put(getMethodReference(Address.class, MagicNames.wordDiff, Address.class, Offset.class), g);
    generators.put(getMethodReference(Extent.class, MagicNames.wordMinus, Extent.class, Extent.class), g);
    generators.put(getMethodReference(Offset.class, MagicNames.wordMinus, Offset.class, Offset.class), g);
    generators.put(getMethodReference(Word.class, MagicNames.wordMinus, Word.class, Word.class), g);
    generators.put(getMethodReference(Word.class, MagicNames.wordMinus, Offset.class, Word.class), g);
    generators.put(getMethodReference(Word.class, MagicNames.wordMinus, Extent.class, Word.class), g);
    if (VM.BuildFor64Addr) {
      g = new WordMinus32();
    }
    generators.put(getMethodReference(Address.class, MagicNames.wordMinus, int.class, Address.class), g);
    generators.put(getMethodReference(Extent.class, MagicNames.wordMinus, int.class, Extent.class), g);
    generators.put(getMethodReference(Offset.class, MagicNames.wordMinus, int.class, Offset.class), g);
  }

  /**
   * Logical and of words
   */
  private static final class WordAnd extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitAND_RegInd_Reg(SP, T0);
      } else {
        asm.emitAND_RegInd_Reg_Quad(SP, T0);
      }
    }
  }
  static {
    MagicGenerator g = new WordAnd();
    generators.put(getMethodReference(Word.class, MagicNames.wordAnd, Word.class, Word.class), g);
  }

  /**
   * Logical or of words
   */
  private static final class WordOr extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitOR_RegInd_Reg(SP, T0);
      } else {
        asm.emitOR_RegInd_Reg_Quad(SP, T0);
      }
    }
  }
  static {
    MagicGenerator g = new WordOr();
    generators.put(getMethodReference(Word.class, MagicNames.wordOr, Word.class, Word.class), g);
  }

  /**
   * Logical xor of words
   */
  private static final class WordXor extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitXOR_RegInd_Reg(SP, T0);
      } else {
        asm.emitXOR_RegInd_Reg_Quad(SP, T0);
      }
    }
  }
  static {
    MagicGenerator g = new WordXor();
    generators.put(getMethodReference(Word.class, MagicNames.wordXor, Word.class, Word.class), g);
  }

  /**
   * Logical left shift of words
   */
  private static final class WordLsh extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(ECX);
      if (VM.BuildFor32Addr) {
        asm.emitSHL_RegInd_Reg(SP, ECX);
      } else {
        asm.emitSHL_RegInd_Reg_Quad(SP, ECX);
      }
    }
  }
  static {
    MagicGenerator g = new WordLsh();
    generators.put(getMethodReference(Word.class, MagicNames.wordLsh, int.class, Word.class), g);
  }

  /**
   * Logical right shift of words
   */
  private static final class WordRshl extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(ECX);
      if (VM.BuildFor32Addr) {
        asm.emitSHR_RegInd_Reg(SP, ECX);
      } else {
        asm.emitSHR_RegInd_Reg_Quad(SP, ECX);
      }
    }
  }
  static {
    MagicGenerator g = new WordRshl();
    generators.put(getMethodReference(Word.class, MagicNames.wordRshl, int.class, Word.class), g);
  }

  /**
   * Arithmetic right shift of words
   */
  private static final class WordRsha extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(ECX);
      if (VM.BuildFor32Addr) {
        asm.emitSAR_RegInd_Reg(SP, ECX);
      } else {
        asm.emitSAR_RegInd_Reg_Quad(SP, ECX);
      }
    }
  }
  static {
    MagicGenerator g = new WordRsha();
    generators.put(getMethodReference(Word.class, MagicNames.wordRsha, int.class, Word.class), g);
  }

  /**
   * Logical not of word
   */
  private static final class WordNot extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      if (VM.BuildFor32Addr) {
        asm.emitNOT_RegInd(SP);
      } else {
        asm.emitNOT_RegInd_Quad(SP);
      }
    }
  }
  static {
    MagicGenerator g = new WordNot();
    generators.put(getMethodReference(Word.class, MagicNames.wordNot, Word.class), g);
  }

  /**
   * Convert word to long
   */
  private static final class WordToLong extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      if (VM.BuildFor32Addr) {
        asm.emitPUSH_Imm(0); // upper 32 bits
        asm.emitPUSH_Reg(T0); // lower 32 bits
      } else {
        asm.emitPUSH_Reg(T0); // adjust stack
        asm.emitPUSH_Reg(T0); // long value
      }
    }
  }
  static {
    MagicGenerator g = new WordToLong();
    Class<?>[] unboxedTypes = new Class<?>[]{Address.class, Extent.class, Offset.class, Word.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.wordToLong, long.class), g);
    }
  }

  /**
   * Set a register to a value from the stack
   */
  private static final class SetRegister extends MagicGenerator {
    private final GPR reg;
    SetRegister(GPR reg) {
      this.reg = reg;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(reg);
    }
  }
  static {
    generators.put(getMethodReference(Magic.class, MagicNames.setESIAsThread, RVMThread.class, void.class),
        new SetRegister(ESI));
    generators.put(getMethodReference(Magic.class, MagicNames.setThreadRegister, RVMThread.class, void.class),
        new SetRegister(TR));
  }

  /**
   * Put a register on to the stack
   */
  private static final class GetRegister extends MagicGenerator {
    private final GPR reg;
    GetRegister(GPR reg) {
      this.reg = reg;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPUSH_Reg(reg);
    }
  }
  static {
    generators.put(getMethodReference(Magic.class, MagicNames.getESIAsThread, RVMThread.class),
        new GetRegister(ESI));
    generators.put(getMethodReference(Magic.class, MagicNames.getThreadRegister, RVMThread.class),
        new GetRegister(TR));
  }

  /**
   * Reflective method dispatch
   */
  private static final class InvokeMethodReturningObject extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Offset offset = ArchEntrypoints.reflectiveMethodInvokerInstructionsField.getOffset();
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 5); // pass 5 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new InvokeMethodReturningObject();
    if (VALIDATE_OBJECT_REFERENCES) {
      g = new LateReferenceCheckDecorator(NO_SLOT, g);
    }
    generators.put(getMethodReference(Magic.class, MagicNames.invokeMethodReturningObject, CodeArray.class, WordArray.class, double[].class, byte[].class, WordArray.class, Object.class), g);
  }

  /**
   * Reflective method dispatch
   */
  private static final class InvokeMethodReturningVoid extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Offset offset = ArchEntrypoints.reflectiveMethodInvokerInstructionsField.getOffset();
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 5); // pass 5 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
    }
  }
  static {
    MagicGenerator g = new InvokeMethodReturningVoid();
    generators.put(getMethodReference(Magic.class, MagicNames.invokeMethodReturningVoid, CodeArray.class, WordArray.class, double[].class, byte[].class, WordArray.class, void.class), g);
  }

  /**
   * Reflective method dispatch
   */
  private static final class InvokeMethodReturningInt extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Offset offset = ArchEntrypoints.reflectiveMethodInvokerInstructionsField.getOffset();
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 5); // pass 5 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    MagicGenerator g = new InvokeMethodReturningInt();
    generators.put(getMethodReference(Magic.class, MagicNames.invokeMethodReturningInt, CodeArray.class, WordArray.class, double[].class, byte[].class, WordArray.class, int.class), g);
  }

  /**
   * Reflective method dispatch
   */
  private static final class InvokeMethodReturningLong extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Offset offset = ArchEntrypoints.reflectiveMethodInvokerInstructionsField.getOffset();
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 5); // pass 5 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
      asm.emitPUSH_Reg(T0); // high half
      asm.emitPUSH_Reg(T1); // low half
    }
  }
  static {
    MagicGenerator g = new InvokeMethodReturningLong();
    generators.put(getMethodReference(Magic.class, MagicNames.invokeMethodReturningLong, CodeArray.class, WordArray.class, double[].class, byte[].class, WordArray.class, long.class), g);
  }

  /**
   * Reflective method dispatch
   */
  private static final class InvokeMethodReturningFloat extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Offset offset = ArchEntrypoints.reflectiveMethodInvokerInstructionsField.getOffset();
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 5); // pass 5 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
      asm.emitPUSH_Reg(T0); // create space
      if (SSE2_FULL) {
        asm.emitMOVSS_RegInd_Reg(SP, XMM0);
      } else {
        asm.emitFSTP_RegInd_Reg(SP, FP0);
      }
    }
  }
  static {
    MagicGenerator g = new InvokeMethodReturningFloat();
    generators.put(getMethodReference(Magic.class, MagicNames.invokeMethodReturningFloat, CodeArray.class, WordArray.class, double[].class, byte[].class, WordArray.class, float.class), g);
  }

  /**
   * Reflective method dispatch
   */
  private static final class InvokeMethodReturningDouble extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Offset offset = ArchEntrypoints.reflectiveMethodInvokerInstructionsField.getOffset();
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 5); // pass 5 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
      asm.emitPUSH_Reg(T0); // create space
      asm.emitPUSH_Reg(T0);
      if (SSE2_FULL) {
        asm.emitMOVLPD_RegInd_Reg(SP, XMM0);
      } else {
        asm.emitFSTP_RegInd_Reg_Quad(SP, FP0);
      }
    }
  }
  static {
    MagicGenerator g = new InvokeMethodReturningDouble();
    generators.put(getMethodReference(Magic.class, MagicNames.invokeMethodReturningDouble, CodeArray.class, WordArray.class, double[].class, byte[].class, WordArray.class, double.class), g);
  }

  /**
   * Invoke an entry point taking values off of the stack
   */
  private static final class InvokeEntryPoint extends MagicGenerator {
    private final Offset offset;
    private final int args;
    InvokeEntryPoint(Offset offset, int args) {
      this.offset = offset;
      this.args = args;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      BaselineCompilerImpl.genParameterRegisterLoad(asm, args);
      asm.emitCALL_Abs(Magic.getTocPointer().plus(offset));
    }
  }
  static {
    generators.put(getMethodReference(Magic.class, MagicNames.saveThreadState, Registers.class, void.class),
        new InvokeEntryPoint(ArchEntrypoints.saveThreadStateInstructionsField.getOffset(), 1));
    generators.put(getMethodReference(Magic.class, MagicNames.threadSwitch, RVMThread.class, Registers.class, void.class),
        new InvokeEntryPoint(ArchEntrypoints.threadSwitchInstructionsField.getOffset(), 2));
    generators.put(getMethodReference(Magic.class, MagicNames.restoreHardwareExceptionState, Registers.class, void.class),
        new InvokeEntryPoint(ArchEntrypoints.restoreHardwareExceptionStateInstructionsField.getOffset(), 1));
  }

  /**
   * Perform dynamic bridge from linker to compiled code
   */
  private static final class DynamicBridgeTo extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      if (VM.VerifyAssertions) VM._assert(cm.getDeclaringClass().hasDynamicBridgeAnnotation());

      // save the branch address for later
      asm.emitPOP_Reg(S0);             // S0<-code address

      if (VM.BuildFor32Addr) {
        asm.emitADD_Reg_Imm(SP, sd.toInt() - WORDSIZE); // just popped WORDSIZE bytes above.
      } else {
        asm.emitADD_Reg_Imm_Quad(SP, sd.toInt() - WORDSIZE); // just popped WORDSIZE bytes above.
      }
      if (SSE2_FULL) {
        // TODO: Restore SSE2 Control word?
        asm.emitMOVQ_Reg_RegDisp(XMM0, SP, XMM_SAVE_OFFSET.plus(0));
        asm.emitMOVQ_Reg_RegDisp(XMM1, SP, XMM_SAVE_OFFSET.plus(8));
        asm.emitMOVQ_Reg_RegDisp(XMM2, SP, XMM_SAVE_OFFSET.plus(16));
        asm.emitMOVQ_Reg_RegDisp(XMM3, SP, XMM_SAVE_OFFSET.plus(24));
      } else {
        // restore FPU state
        asm.emitFRSTOR_RegDisp(SP, FPU_SAVE_OFFSET);
      }

      // restore GPRs
      if (VM.BuildFor32Addr) {
        asm.emitMOV_Reg_RegDisp(T0, SP, T0_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(T1, SP, T1_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EBX, SP, EBX_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EDI, SP, EDI_SAVE_OFFSET);
      } else {
        asm.emitMOV_Reg_RegDisp_Quad(T0, SP, T0_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp_Quad(T1, SP, T1_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp_Quad(EBX, SP, EBX_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp_Quad(EDI, SP, EDI_SAVE_OFFSET);
      }

      // pop frame
      asm.emitPOP_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset()); // FP<-previous FP

      // branch
      asm.emitJMP_Reg(S0);
    }
  }
  static {
    MagicGenerator g = new DynamicBridgeTo();
    generators.put(getMethodReference(Magic.class, MagicNames.dynamicBridgeTo, CodeArray.class, void.class), g);
  }

  /**
   * Exchange stacks
   */
  private static final class ReturnToNewStack extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      // SP gets frame pointer for new stack
      asm.emitPOP_Reg(SP);

      // restore nonvolatile registers
      if (VM.BuildFor32Addr) {
        asm.emitMOV_Reg_RegDisp(EDI, SP, EDI_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EBX, SP, EBX_SAVE_OFFSET);
      } else {
        asm.emitMOV_Reg_RegDisp_Quad(EDI, SP, EDI_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp_Quad(EBX, SP, EBX_SAVE_OFFSET);
      }
      // discard current stack frame
      asm.emitPOP_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset());

      // return to caller- pop parameters from stack
      int parameterWords = cm.getParameterWords() + (cm.isStatic() ? 0 : 1); // add 1 for this pointer
      asm.emitRET_Imm(parameterWords << LG_WORDSIZE);
    }
  }
  static {
    MagicGenerator g = new ReturnToNewStack();
    generators.put(getMethodReference(Magic.class, MagicNames.returnToNewStack, Address.class, void.class), g);
  }

  /**
   * Boot up calling of class initializers
   */
  private static final class InvokeClassInitializer extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(S0);
      asm.emitCALL_Reg(S0); // call address just popped
    }
  }
  static {
    MagicGenerator g = new InvokeClassInitializer();
    generators.put(getMethodReference(Magic.class, MagicNames.invokeClassInitializer, CodeArray.class, void.class), g);
  }

  /**
   * Get frame pointer on entry to method
   */
  private static final class GetFramePointer extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitLEA_Reg_RegDisp(S0, SP, sd);
      asm.emitPUSH_Reg(S0);
    }
  }
  static {
    MagicGenerator g = new GetFramePointer();
    generators.put(getMethodReference(Magic.class, MagicNames.getFramePointer, Address.class), g);
  }

  /**
   * Load an address from the stack and load the value at it plus a displacement
   */
  private static final class GetValueAtDisplacement extends MagicGenerator {
    final Offset disp;
    GetValueAtDisplacement(Offset disp) {
      this.disp = disp;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      asm.emitPUSH_RegDisp(T0, disp);
    }
  }
  static {
    generators.put(getMethodReference(Magic.class, MagicNames.getCallerFramePointer, Address.class, Address.class),
        new GetValueAtDisplacement(Offset.fromIntSignExtend(STACKFRAME_FRAME_POINTER_OFFSET)));
    generators.put(getMethodReference(Magic.class, MagicNames.getCompiledMethodID, Address.class, int.class),
        new GetValueAtDisplacement(Offset.fromIntSignExtend(STACKFRAME_METHOD_ID_OFFSET)));
    MagicGenerator g = new GetValueAtDisplacement(ObjectModel.getArrayLengthOffset());
    generators.put(getMethodReference(Magic.class, MagicNames.getArrayLength, Object.class, int.class), g);
    Class<?>[] unboxedTypes = new Class<?>[]{AddressArray.class, CodeArray.class, ExtentArray.class, IMT.class, ObjectReferenceArray.class, OffsetArray.class, TIB.class, WordArray.class};
    for (Class<?> type : unboxedTypes) {
      generators.put(getMethodReference(type, MagicNames.addressArrayLength, int.class), g);
    }
  }

  /**
   * Store a value to an address from the stack plus a displacement
   */
  private static final class SetValueAtDisplacement extends MagicGenerator {
    final Offset disp;
    SetValueAtDisplacement(Offset disp) {
      this.disp = disp;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);  // value
      asm.emitPOP_Reg(S0);  // fp
      if (VM.BuildFor32Addr) {
        asm.emitMOV_RegDisp_Reg(S0, disp, T0); // [S0+disp] <- T0
      } else {
        asm.emitMOV_RegDisp_Reg_Quad(S0, disp, T0); // [S0+disp] <- T0
      }
    }
  }
  static {
    generators.put(getMethodReference(Magic.class, MagicNames.setCallerFramePointer, Address.class, Address.class, void.class),
        new SetValueAtDisplacement(Offset.fromIntSignExtend(STACKFRAME_FRAME_POINTER_OFFSET)));
    generators.put(getMethodReference(Magic.class, MagicNames.setCompiledMethodID, Address.class, int.class, void.class),
        new SetValueAtDisplacement(Offset.fromIntSignExtend(STACKFRAME_METHOD_ID_OFFSET)));
  }

  /**
   * Create an array for a runtime table
   * @see org.jikesrvm.objectmodel.RuntimeTable
   */
  private static final class CreateArray extends MagicGenerator {
    private final RVMArray array;
    CreateArray(RVMArray array) {
      this.array = array;
    }
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      int width = array.getLogElementSize();
      Offset tibOffset = array.getTibOffset();
      int headerSize = ObjectModel.computeHeaderSize(array);
      int whichAllocator = MemoryManager.pickAllocator(array, cm);
      int site = MemoryManager.getAllocationSite(true);
      int align = ObjectModel.getAlignment(array);
      int offset = ObjectModel.getOffsetForAlignment(array, false);
      // count is already on stack- nothing required
      asm.emitPUSH_Imm(width);                 // logElementSize
      asm.emitPUSH_Imm(headerSize);            // headerSize
      asm.emitPUSH_Abs(Magic.getTocPointer().plus(tibOffset));   // tib
      asm.emitPUSH_Imm(whichAllocator);        // allocator
      asm.emitPUSH_Imm(align);
      asm.emitPUSH_Imm(offset);
      asm.emitPUSH_Imm(site);
      BaselineCompilerImpl.genParameterRegisterLoad(asm, 8);             // pass 8 parameter words
      asm.emitCALL_Abs(Magic.getTocPointer().plus(Entrypoints.resolvedNewArrayMethod.getOffset()));
      asm.emitPUSH_Reg(T0);
    }
  }
  static {
    Class<?>[] unboxedTypes = new Class<?>[] { AddressArray.class,
        CodeArray.class, ExtentArray.class, ObjectReferenceArray.class,
        OffsetArray.class, WordArray.class };
    for (Class<?> type : unboxedTypes) {
      MagicGenerator g = new CreateArray(TypeReference.findOrCreate(type).resolve().asArray());
      generators.put(getMethodReference(type, MagicNames.addressArrayCreate, int.class, type), g);
    }
  }

  /**
   * Get a 32bit element from a runtime table
   * @see org.jikesrvm.objectmodel.RuntimeTable#get(int)
   */
  private static final class Load32_Array extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);          // T0 is array index
      asm.emitPOP_Reg(S0);          // S0 is array ref
      BaselineCompilerImpl.genBoundsCheck(asm, T0, S0); // T0 is index, S0 is address of array
      // push [S0+T0<<2]
      asm.emitPUSH_RegIdx(S0, T0, Assembler.WORD, NO_SLOT);
    }
  }
  /**
   * Get a 64bit element from a runtime table
   * @see org.jikesrvm.objectmodel.RuntimeTable#get(int)
   */
  private static final class Load64_Array extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);          // T0 is array index
      asm.emitPOP_Reg(S0);          // S0 is array ref
      BaselineCompilerImpl.genBoundsCheck(asm, T0, S0); // T0 is index, S0 is address of array
      // push [S0+T0<<3]
      asm.emitPUSH_RegIdx(S0, T0, Assembler.LONG, NO_SLOT);
    }
  }
  static {
    MagicGenerator g = VM.BuildFor32Addr ? new Load32_Array() : new Load64_Array();
    Class<?>[] unboxedTypes = new Class<?>[] { AddressArray.class,
        ExtentArray.class, IMT.class,
        ObjectReferenceArray.class, OffsetArray.class,
        TIB.class, WordArray.class };
    Class<?>[] resultTypes = new Class<?>[] { Address.class, Extent.class,
        CodeArray.class, ObjectReference.class, Offset.class,
        Object.class, Word.class };
    for (int i=0; i < unboxedTypes.length; i++) {
      Class<?> type = unboxedTypes[i];
      Class<?> result = resultTypes[i];
      generators.put(getMethodReference(type, MagicNames.addressArrayGet, int.class, result), g);
    }
  }

  /**
   * Get a byte element from a runtime table
   */
  private static final class LoadByte_Array extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0); // T0 is array index
      asm.emitPOP_Reg(S0); // S0 is array ref
      BaselineCompilerImpl.genBoundsCheck(asm, T0, S0); // T0 is index, S0 is address of array
      // T1 = (int)[S0+T0<<1]
      asm.emitMOVSX_Reg_RegIdx_Byte(T1, S0, T0, Assembler.BYTE, NO_SLOT);
      asm.emitPUSH_Reg(T1);        // push byte onto stack
    }
  }
  static {
    MagicGenerator g = new LoadByte_Array();
    generators.put(getMethodReference(CodeArray.class, MagicNames.addressArrayGet, int.class, byte.class), g);
  }

  /**
   * Store a 32bit element to a runtime table
   * @see org.jikesrvm.objectmodel.RuntimeTable#set(int, Object)
   */
  private static final class Store32_Array extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Barriers.compileModifyCheck(asm, 8);
      asm.emitPOP_Reg(T1); // T1 is the value
      asm.emitPOP_Reg(T0); // T0 is array index
      asm.emitPOP_Reg(S0); // S0 is array ref
      BaselineCompilerImpl.genBoundsCheck(asm, T0, S0);            // T0 is index, S0 is address of array
      asm.emitMOV_RegIdx_Reg(S0, T0, Assembler.WORD, NO_SLOT, T1); // [S0 + T0<<2] <- T1
    }
  }
  /**
   * Store a 64bit element to a runtime table
   * @see org.jikesrvm.objectmodel.RuntimeTable#set(int, Object)
   */
  private static final class Store64_Array extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Barriers.compileModifyCheck(asm, 8);
      asm.emitPOP_Reg(T1); // T1 is the value
      asm.emitPOP_Reg(T0); // T0 is array index
      asm.emitPOP_Reg(S0); // S0 is array ref
      BaselineCompilerImpl.genBoundsCheck(asm, T0, S0);                 // T0 is index, S0 is address of array
      asm.emitMOV_RegIdx_Reg_Quad(S0, T0, Assembler.LONG, NO_SLOT, T1); // [S0 + T0<<2] <- T1
    }
  }
  static {
    MagicGenerator g = VM.BuildFor32Addr ? new Store32_Array() : new Store64_Array();
    Class<?>[] unboxedTypes = new Class<?>[] { AddressArray.class,
        ExtentArray.class, IMT.class,
        ObjectReferenceArray.class, OffsetArray.class,
        TIB.class, WordArray.class };
    Class<?>[] operandTypes = new Class<?>[] { Address.class, Extent.class,
        CodeArray.class, ObjectReference.class, Offset.class,
        Object.class, Word.class };
    for (int i=0; i < unboxedTypes.length; i++) {
      Class<?> type = unboxedTypes[i];
      Class<?> operand = operandTypes[i];
      generators.put(getMethodReference(type, MagicNames.addressArraySet, int.class, operand, void.class), g);
    }
  }

  /**
   * Set a 8bit in a runtime table
   */
  private static final class Store8_Array extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      Barriers.compileModifyCheck(asm, 8);
      asm.emitPOP_Reg(T1); // T1 is the value
      asm.emitPOP_Reg(T0); // T0 is array index
      asm.emitPOP_Reg(S0); // S0 is array ref
      BaselineCompilerImpl.genBoundsCheck(asm, T0, S0);                // T0 is index, S0 is address of array
      asm.emitMOV_RegIdx_Reg_Byte(S0, T0, Assembler.BYTE, NO_SLOT, T1); // [S0 + T0<<2] <- T1
    }
  }
  static {
    MagicGenerator g = new Store8_Array();
    generators.put(getMethodReference(CodeArray.class, MagicNames.addressArraySet, int.class, byte.class, void.class), g);
  }

  /**
   * Create address that holds return address
   */
  private static final class GetReturnAddressLocation extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      if (VM.BuildFor32Addr) {
        asm.emitADD_RegInd_Imm(SP, STACKFRAME_RETURN_ADDRESS_OFFSET);
      } else {
        asm.emitADD_RegInd_Imm_Quad(SP, STACKFRAME_RETURN_ADDRESS_OFFSET);
      }
    }
  }
  static {
    MagicGenerator g = new GetReturnAddressLocation();
    generators.put(getMethodReference(Magic.class, MagicNames.getReturnAddressLocation, Address.class, Address.class), g);
  }

  /**
   * Get a 64bit time base value (not accurate on certain multi-cores)
   */
  private static final class GetTimeBase extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitRDTSC();       // read timestamp counter instruction
      if(VM.buildFor32Addr())
      {
          asm.emitPUSH_Reg(EDX); // upper 32 bits
          asm.emitPUSH_Reg(EAX); // lower 32 bits
      }
      else
      {
          /*
           * Shift EDX over by 32 and OR into EAX
           */
          asm.emitSHL_Reg_Imm_Quad(EDX, 32);
          asm.emitOR_Reg_Reg_Quad(EAX, EDX);
          asm.emitPUSH_Reg(EAX);    // Just stack filler
          asm.emitPUSH_Reg(EAX);
      }
    }
  }
  static {
    MagicGenerator g = new GetTimeBase();
    generators.put(getMethodReference(Magic.class, MagicNames.getTimeBase, long.class), g);
  }

  /**
   * Pause hint that thread is contending for a lock
   */
  private static final class Pause extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPAUSE();
    }
  }
  static {
    MagicGenerator g = new Pause();
    generators.put(getMethodReference(Magic.class, MagicNames.pause, void.class), g);
  }

  /*
   * Halt the cpu
   */
  private static final class Halt extends MagicGenerator {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitHLT();
      }
  }
  static {
      MagicGenerator g = new Halt();
      generators.put(getMethodReference(Magic.class, MagicNames.halt, void.class), g);
  }
  
  /**
   * Floating point square root
   */
  private static final class Fsqrt extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      if (SSE2_BASE) {
        asm.emitSQRTSS_Reg_RegInd(XMM0, SP);            // XMM0 = sqrt(value)
        asm.emitMOVSS_RegInd_Reg(SP, XMM0);            // set result on stack
      } else {
        VM.sysFail("Hardware sqrt only available for SSE");
      }
    }
  }
  static {
    MagicGenerator g = new Fsqrt();
    generators.put(getMethodReference(Magic.class, MagicNames.sqrt, float.class, float.class), g);
  }

  /**
   * Double precision square root
   */
  private static final class Dsqrt extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      if (SSE2_BASE) {
        asm.emitSQRTSD_Reg_RegInd(XMM0, SP);            // XMM0 = sqrt(value)
        asm.emitMOVLPD_RegInd_Reg(SP, XMM0);            // set result on stack
      } else {
        VM.sysFail("Hardware sqrt only available for SSE");
      }
    }
  }
  static {
    MagicGenerator g = new Dsqrt();
    generators.put(getMethodReference(Magic.class, MagicNames.sqrt, double.class, double.class), g);
  }

  /**
   * double ceiling
   */
  private static final class Dceil extends MagicGenerator
  {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
    {
      // Store FPU control word at top of the stack
      Offset CWOFF = Offset.fromIntSignExtend(-WORDSIZE);
      asm.emitFLDCW_RegDisp(SP, CWOFF);
      asm.emitMOV_Reg_RegDisp(S0, SP, CWOFF);
      // Set FPU to round up
      asm.emitAND_RegDisp_Imm(SP, CWOFF, ~0xc00);
      asm.emitOR_RegDisp_Imm(SP, CWOFF, 0x800);
      asm.emitFSTCW_RegDisp(SP, CWOFF);
      // Load parameter to FPU stack
      asm.emitFLD_Reg_RegInd(FP0, SP);
      asm.emitFRNDINT(FP0);
      asm.emitFST_RegInd_Reg(SP, FP0);
      // restore the fpu cw
      asm.emitMOV_RegDisp_Reg(SP, CWOFF, S0);
      asm.emitFSTCW_RegDisp(SP, CWOFF);
    }
  }
  static
  {
    MagicGenerator g = new Dceil();
    generators.put(getMethodReference(Magic.class, MagicNames.dceil, double.class, double.class),  g);
  }
  /**
   * Return the current inlining depth (always 0 for baseline)
   */
  private static final class GetInlineDepth extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPUSH_Imm(0);
    }
  }
  static {
    MagicGenerator g = new GetInlineDepth();
    generators.put(getMethodReference(Magic.class, MagicNames.getInlineDepth, int.class), g);
  }

  /**
   * Is the requested parameter a constant? Always {@code false} for baseline.
   */
  private static final class IsConstantParameter extends MagicGenerator {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd) {
      asm.emitPOP_Reg(T0);
      asm.emitPUSH_Imm(0);
    }
  }
  static {
    MagicGenerator g = new IsConstantParameter();
    generators.put(getMethodReference(Magic.class, MagicNames.isConstantParameter, int.class, boolean.class), g);
  }
  
  /*
   * Save registers onto the stack
   */
  private static final class SaveContext extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          if(VM.BuildFor64Addr)
          {
              /*
               *   IA32e mode interrupt stack looks like this
               *   
               *   +----------------------
               *   |
               *   |
               *   |
               *   +----------------------
               *   | SS (stack segment)      +40
               *   +----------------------
               *   | RSP                     +32
               *   +----------------------
               *   | RFLAGS                  +24
               *   +----------------------
               *   | CS                      +16
               *   +----------------------
               *   | RIP                     +8
               *   +----------------------
               *   | Error Code               <---- RSP
               *   +----------------------
               */
              Offset gprsOffset = ArchEntrypoints.registersGPRsField.getOffset();
              Offset registersContext = Entrypoints.threadContextRegistersField.getOffset();
              /*
               * Store the stack pointer
               */
              asm.emitMOV_RegDisp_Reg_Quad(TR, ArchEntrypoints.stackPointerField.getOffset(), SP);
              /*
               * Push R14,15 onto the stack
               */
              asm.emitPUSH_Reg(GPR.R15);
              asm.emitPUSH_Reg(GPR.R14);
              // R15 = context registers
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, TR, registersContext);
              // R14 = gprs word array
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R14, GPR.R15, gprsOffset);
              /*
               * Save context int thread.contextRegisters
               */
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*0), EAX);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*1), ECX);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*2), EDX);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*3), EBX);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*4), ESP);  // save but don't restore off by 16
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*5), EBP);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*6), ESI);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*7), EDI);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*8), GPR.R8);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*9), GPR.R9);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*10), GPR.R10);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*11), GPR.R11);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*12), GPR.R12);
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*13), GPR.R13);
              // pop R14 from stack into thread context
              asm.emitPOP_RegDisp(GPR.R14, Offset.zero().plus(WORDSIZE*14)); 
              // pop R15 from stack into thread context
              asm.emitPOP_RegDisp(GPR.R14, Offset.zero().plus(WORDSIZE*15));
              // Push the stack pointer
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, Offset.zero().plus(WORDSIZE*4), ESP);

              /*
               * Store floating point/sse/xmm registers
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, ESI, Entrypoints.fxStateField.getOffset());
              asm.emitFXSAVE_Reg(EAX);
              asm.emitFINIT();
         
              /*
               * Save fp and ip into contextRegisters
               * 
               * eax has the fp
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, TR, ArchEntrypoints.framePointerField.getOffset());
              // save fp
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R14, ArchEntrypoints.registersFPField.getOffset(), EAX);
              // get there return address
//              asm.emitMOV_Reg_RegDisp_Quad(EAX, ESP, Offset.fromIntZeroExtend(8));
              asm.emitMOV_Reg_RegDisp_Quad(EAX, ESP, Offset.zero());
              // save it
              asm.emitMOV_RegDisp_Reg_Quad(GPR.R15, ArchEntrypoints.registersIPField.getOffset(), EAX);
              /*
               * Push the interrupted threads frame pointer
               */
              asm.emitPUSH_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset());        // store interrupted caller's frame pointer
              asm.emitMOV_RegDisp_Reg_Quad(TR, ArchEntrypoints.framePointerField.getOffset(), SP); // establish new frame
              /*
               * Push interrupt method id
               */
              asm.emitPUSH_Imm(StackframeLayoutConstants.INTERRUPT_METHOD_ID);
           }
          else
          {
              /*
               * Push the interrupted threads frame pointer
               */
              asm.emitPUSH_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset());        // store interrupted caller's frame pointer
              asm.emitMOV_RegDisp_Reg(TR, ArchEntrypoints.framePointerField.getOffset(), SP); // establish new frame
              asm.emitPUSH_Imm(StackframeLayoutConstants.INTERRUPT_METHOD_ID);
    
              /**
               * Save context onto the stack.
               * 
               * This will push the following on the stack
               * 
               *    Top Stack
               *    ...
               *    EAX
               *    ECX
               *    EDX
               *    EBX
               *    EBP
               *    EDI  <-- ESP
               */
              asm.emitPUSH_Reg(EAX);
              asm.emitPUSH_Reg(ECX);
              asm.emitPUSH_Reg(EDX);
              asm.emitPUSH_Reg(EBX);
              asm.emitPUSH_Reg(EBP);
              asm.emitPUSH_Reg(EDI);
    
              /*
               * Store floating point/sse/xmm registers
               */
              asm.emitMOV_Reg_RegDisp(EAX, ESI, Entrypoints.fxStateField.getOffset());
              asm.emitFXSAVE_Reg(EAX);
              asm.emitFINIT();
              /*
               * Store the stack pointer
               */
              asm.emitMOV_RegDisp_Reg(TR, ArchEntrypoints.stackPointerField.getOffset(), SP);
              /*
               * Save fp and ip into contextRegisters
               * 
               * eax has the fp
               */
              asm.emitMOV_Reg_RegDisp(EAX, TR, ArchEntrypoints.framePointerField.getOffset());
              // ebx has contextRegisters
              asm.emitMOV_Reg_RegDisp(EBX, TR, Entrypoints.threadContextRegistersField.getOffset());
              // save fp
              asm.emitMOV_RegDisp_Reg(EBX, ArchEntrypoints.registersFPField.getOffset(), EAX);
              // get there return address
              asm.emitMOV_Reg_RegDisp(EAX, SP, Offset.fromIntZeroExtend(32));
              // save it
              asm.emitMOV_RegDisp_Reg(EBX, ArchEntrypoints.registersIPField.getOffset(), EAX);
          }
      }
  }
  static
  {
      MagicGenerator g = new SaveContext();
      generators.put(getMethodReference(Magic.class, MagicNames.saveContext, void.class), g);
  }
  
//    private static final class RestoreContext extends MagicGenerator
//  {
//      @Override
//      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
//      {
//          if(VM.BuildFor64Addr)
//          {
//              /*
//               * restore floating point/sse/xmm registers
//               */
//              asm.emitMOV_Reg_RegDisp_Quad(EAX, ESI, Entrypoints.fxStateField.getOffset());
//              asm.emitFXRSTOR_Reg(EAX);
//              
//              Offset gprsOffset = ArchEntrypoints.registersGPRsField.getOffset();
//              Offset registersContext = Entrypoints.threadContextRegistersField.getOffset();
//              // R15 = context registers
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, TR, registersContext);
//              // R14 = gprs word array
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R14, GPR.R15, gprsOffset);
//              /*
//               * restore the frame pointer
//               */
//              asm.emitMOV_Reg_RegDisp_Quad(EAX, GPR.R15, ArchEntrypoints.registersFPField.getOffset());
//              asm.emitMOV_RegDisp_Reg_Quad(TR, ArchEntrypoints.framePointerField.getOffset(), EAX);
//              /*
//               * restore registers
//               */
//              asm.emitMOV_Reg_RegDisp_Quad(EAX, GPR.R14, gprsOffset.plus(WORDSIZE));
//              asm.emitMOV_Reg_RegDisp_Quad(ECX, GPR.R14, gprsOffset.plus(WORDSIZE*2));
//              asm.emitMOV_Reg_RegDisp_Quad(EDX, GPR.R14, gprsOffset.plus(WORDSIZE*3));
//              asm.emitMOV_Reg_RegDisp_Quad(EBX, GPR.R14, gprsOffset.plus(WORDSIZE*4));
//              asm.emitMOV_Reg_RegDisp_Quad(EBP, GPR.R14, gprsOffset.plus(WORDSIZE*5));
//              asm.emitMOV_Reg_RegDisp_Quad(EDI, GPR.R14, gprsOffset.plus(WORDSIZE*6));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R8, GPR.R14, gprsOffset.plus(WORDSIZE*7));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R9, GPR.R14, gprsOffset.plus(WORDSIZE*8));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R10, GPR.R14, gprsOffset.plus(WORDSIZE*9));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R11, GPR.R14, gprsOffset.plus(WORDSIZE*10));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R12, GPR.R14, gprsOffset.plus(WORDSIZE*11));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R13, GPR.R14, gprsOffset.plus(WORDSIZE*12));
//              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, GPR.R14, gprsOffset.plus(WORDSIZE*14));
//              /*
//               * r14 being used as pointer to register context
//               * push r14 onto the stack, then pop it into register to restore
//               */
//              asm.emitPUSH_RegDisp(GPR.R14, gprsOffset.plus(WORDSIZE*13));
//              asm.emitPOP_Reg(GPR.R14);
//              /*
//               * Need to pop the compiled method id
//               */
//              asm.emitADD_Reg_Imm(ESP, 0x8);
//          }
//          else
//          {
//              /*
//               * Should be at the interrupted stacks SP.
//               * 
//               * Switch to the interrupted stack by popping it into the SP
//               */
//    //          asm.emitPOP_Reg(GPR.ESP);
//              /*
//               * Need to pop the interrupt thread stack
//               */
//    //          asm.emitPOP_Reg(GPR.EAX);
//              /*
//               * restore floating point/sse/xmm registers
//               */
//              asm.emitMOV_Reg_RegDisp(EAX, ESI, Entrypoints.fxStateField.getOffset());
//              asm.emitFXRSTOR_Reg(EAX);
//    
//              /*
//               * Restore the stack
//               */
//              asm.emitMOV_Reg_RegDisp(SP, TR, ArchEntrypoints.stackPointerField.getOffset());
//              /*
//               * Now restore the interrrupted threads context
//               */
//              asm.emitPOP_Reg(EDI);
//              asm.emitPOP_Reg(EBP);
//              asm.emitPOP_Reg(EBX);
//              asm.emitPOP_Reg(EDX);
//              asm.emitPOP_Reg(ECX);
//              asm.emitPOP_Reg(EAX);
//              // pop the cmid
//              asm.emitADD_Reg_Imm(SP, 4);
//              // Restore previous frame pointer
//              asm.emitPOP_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset()); // discard frame
//          }
//      }
//  }
//  static
//  {
//      MagicGenerator g = new RestoreContext();
//      generators.put(getMethodReference(Magic.class, MagicNames.restoreContext, void.class), g);
//  }
  
  /*
   * restore context and stack from the thread
   */
  private static final class RestoreThreadContextErrCode extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          if(VM.BuildFor64Addr)
          {
              /*
               * restore floating point/sse/xmm registers
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, TR, Entrypoints.fxStateField.getOffset());
              asm.emitFXRSTOR_Reg(EAX);
              
              Offset gprsOffset = ArchEntrypoints.registersGPRsField.getOffset();
              Offset registersContext = Entrypoints.threadContextRegistersField.getOffset();
              // R15 = context registers
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, TR, registersContext);
              // R14 = gprs word array
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R14, GPR.R15, gprsOffset);
              /*
               * restore the frame pointer
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, GPR.R14, ArchEntrypoints.registersFPField.getOffset());
              asm.emitMOV_RegDisp_Reg_Quad(TR, ArchEntrypoints.framePointerField.getOffset(), EAX);
              /*
               * restore registers
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, GPR.R14, Offset.zero().plus(WORDSIZE*0));
              asm.emitMOV_Reg_RegDisp_Quad(ECX, GPR.R14, Offset.zero().plus(WORDSIZE*1));
              asm.emitMOV_Reg_RegDisp_Quad(EDX, GPR.R14, Offset.zero().plus(WORDSIZE*2));
              asm.emitMOV_Reg_RegDisp_Quad(EBX, GPR.R14, Offset.zero().plus(WORDSIZE*3));
              asm.emitMOV_Reg_RegDisp_Quad(ESP, GPR.R14, Offset.zero().plus(WORDSIZE*4));   // Now we are on the restored threads stack
              asm.emitMOV_Reg_RegDisp_Quad(EBP, GPR.R14, Offset.zero().plus(WORDSIZE*5));
              asm.emitMOV_Reg_RegDisp_Quad(EDI, GPR.R14, Offset.zero().plus(WORDSIZE*7));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R8, GPR.R14, Offset.zero().plus(WORDSIZE*8));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R9, GPR.R14, Offset.zero().plus(WORDSIZE*9));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R10, GPR.R14, Offset.zero().plus(WORDSIZE*10));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R11, GPR.R14, Offset.zero().plus(WORDSIZE*11));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R12, GPR.R14, Offset.zero().plus(WORDSIZE*12));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R13, GPR.R14, Offset.zero().plus(WORDSIZE*13));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, GPR.R14, Offset.zero().plus(WORDSIZE*15));
              /*
               * r14 being used as pointer to register context
               * push r14 onto the stack, then pop it into register to restore
               */
              asm.emitPUSH_RegDisp(GPR.R14, Offset.zero().plus(WORDSIZE*14));
              asm.emitPOP_Reg(GPR.R14);
              /*
               * Need to pop the error code
               */
              asm.emitADD_Reg_Imm(ESP, 0x8);
          }
          else
          {
              /*
               * restore the stack pointer from the RVMThread sp field
               */
              asm.emitMOV_Reg_RegDisp(SP, TR, ArchEntrypoints.stackPointerField.getOffset());
              /*
               * restore floating point/sse/xmm registers
               */
              asm.emitMOV_Reg_RegDisp(EAX, ESI, Entrypoints.fxStateField.getOffset());
              asm.emitFXRSTOR_Reg(EAX);
              /*
               * Now restore the interrrupted threads context
               */
              asm.emitPOP_Reg(EDI);
              asm.emitPOP_Reg(EBP);
              asm.emitPOP_Reg(EBX);
              asm.emitPOP_Reg(EDX);
              asm.emitPOP_Reg(ECX);
              asm.emitPOP_Reg(EAX);
              // pop the cmid
              asm.emitADD_Reg_Imm(SP, 4);
              // Restore previous frame pointer
              asm.emitPOP_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset()); // discard frame
          }
      }
  }
  static
  {
      MagicGenerator g = new RestoreThreadContextErrCode();
      generators.put(getMethodReference(Magic.class, MagicNames.restoreThreadContextErrCode, void.class), g);
  }

  /*
   * restore context and stack from the thread
   */
  private static final class RestoreThreadContextNoErrCode extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          if(VM.BuildFor64Addr)
          {
              /*
               * restore floating point/sse/xmm registers
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, TR, Entrypoints.fxStateField.getOffset());
              asm.emitFXRSTOR_Reg(EAX);
              
              Offset gprsOffset = ArchEntrypoints.registersGPRsField.getOffset();
              Offset registersContext = Entrypoints.threadContextRegistersField.getOffset();
              // R15 = context registers
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, TR, registersContext);
              // R14 = gprs word array
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R14, GPR.R15, gprsOffset);
              /*
               * restore the frame pointer
               */
//              asm.emitMOV_Reg_RegDisp_Quad(EAX, GPR.R14, ArchEntrypoints.registersFPField.getOffset());
//              asm.emitMOV_RegDisp_Reg_Quad(TR, ArchEntrypoints.framePointerField.getOffset(), EAX);
              /*
               * restore registers
               */
              asm.emitMOV_Reg_RegDisp_Quad(EAX, GPR.R14, Offset.zero().plus(WORDSIZE*0));
              asm.emitMOV_Reg_RegDisp_Quad(ECX, GPR.R14, Offset.zero().plus(WORDSIZE*1));
              asm.emitMOV_Reg_RegDisp_Quad(EDX, GPR.R14, Offset.zero().plus(WORDSIZE*2));
              asm.emitMOV_Reg_RegDisp_Quad(EBX, GPR.R14, Offset.zero().plus(WORDSIZE*3));
//              asm.emitMOV_Reg_RegDisp_Quad(ESP, GPR.R14, Offset.zero().plus(WORDSIZE*4));   // Now we are on the restored threads stack
              asm.emitMOV_Reg_RegDisp_Quad(EBP, GPR.R14, Offset.zero().plus(WORDSIZE*5));
              asm.emitMOV_Reg_RegDisp_Quad(EDI, GPR.R14, Offset.zero().plus(WORDSIZE*7));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R8, GPR.R14, Offset.zero().plus(WORDSIZE*8));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R9, GPR.R14, Offset.zero().plus(WORDSIZE*9));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R10, GPR.R14, Offset.zero().plus(WORDSIZE*10));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R11, GPR.R14, Offset.zero().plus(WORDSIZE*11));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R12, GPR.R14, Offset.zero().plus(WORDSIZE*12));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R13, GPR.R14, Offset.zero().plus(WORDSIZE*13));
              asm.emitMOV_Reg_RegDisp_Quad(GPR.R15, GPR.R14, Offset.zero().plus(WORDSIZE*15));
              /*
               * r14 being used as pointer to register context
               * push r14 onto the stack, then pop it into register to restore
               */
              asm.emitPUSH_RegDisp(GPR.R14, Offset.zero().plus(WORDSIZE*14));
              asm.emitPOP_Reg(GPR.R14);
              /*
               * Need to pop the CMID
               */
//              asm.emitADD_Reg_Imm(ESP, 0x8);
              /*
               * restore the stack pointer from the RVMThread sp field
               */
              asm.emitMOV_Reg_RegDisp_Quad(SP, TR, ArchEntrypoints.stackPointerField.getOffset());
          }
          else
          {
              /*
               * restore the stack pointer from the RVMThread sp field
               */
              asm.emitMOV_Reg_RegDisp(SP, TR, ArchEntrypoints.stackPointerField.getOffset());
              /*
               * restore floating point/sse/xmm registers
               */
              asm.emitMOV_Reg_RegDisp(EAX, ESI, Entrypoints.fxStateField.getOffset());
              asm.emitFXRSTOR_Reg(EAX);
              /*
               * Now restore the interrrupted threads context
               */
              asm.emitPOP_Reg(EDI);
              asm.emitPOP_Reg(EBP);
              asm.emitPOP_Reg(EBX);
              asm.emitPOP_Reg(EDX);
              asm.emitPOP_Reg(ECX);
              asm.emitPOP_Reg(EAX);
              // pop the cmid
              asm.emitADD_Reg_Imm(SP, 4);
              // Restore previous frame pointer
              asm.emitPOP_RegDisp(TR, ArchEntrypoints.framePointerField.getOffset()); // discard frame
          }
      }
  }
  static
  {
      MagicGenerator g = new RestoreThreadContextNoErrCode();
      generators.put(getMethodReference(Magic.class, MagicNames.restoreThreadContextNoErrCode, void.class), g);
  }
  
  /**
   * Set ESP to a new stack pointer and
   * save into thread's framePointer field
   */
  private static final class SwitchStack extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          // Get the current stack pointer
          //asm.emitLEA_Reg_RegDisp(S0, SP, sd);
          asm.emitMOV_Reg_Reg(S0, SP);
          // Pop new stack parameter into the ESP
          // The new stack parameter is still on the old stack
          // remember to pop when restoring
          asm.emitPOP_Reg(SP);
      }
  }
  
  /**
   * Add SwitchStack magic into the table
   */
  static
  {
      MagicGenerator g = new SwitchStack();
      generators.put(getMethodReference(Magic.class, MagicNames.switchStack, Address.class, void.class), g);
  }
  
  /**
   * Set the IDT register
   */
  private static final class SetIdt extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitPOP_Reg(S0);
          asm.emitLIDT(S0);
      }
  }
  /**
   * Add SetIdt magic into the table
   */
  static
  {
      MagicGenerator g = new SetIdt();
      generators.put(getMethodReference(Magic.class, MagicNames.setIdt, Address.class, void.class), g);
  }
  
  /**
   * Start a thread
   */
  private static final class StartThread extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitPOP_Reg(S0);  // thread instruction pointer
          asm.emitPOP_Reg(SP);  // switch to the new stack
          /*
           * Start executing at the new thread
           */
          asm.emitJMP_Reg(S0);
      }
  }
  /**
   * Add StartThread into the table
   */
  static
  {
      MagicGenerator g = new StartThread();
      generators.put(getMethodReference(Magic.class, MagicNames.startThread, Address.class, Address.class, void.class), g);
  }
  
  /**
   * Enable interrupts
   */
  private static final class EnableInterrupts extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitSTI();
      }
  }
  
  /**
   * Add EnableInterrupts into the table
   */
  static
  {
      MagicGenerator g = new EnableInterrupts();
      generators.put(getMethodReference(Magic.class, MagicNames.enableInterrupts, void.class), g);
  }

  /**
   * Disable interrupts
   */
  private static final class DisableInterrupts extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitCLI();
      }
  }
  
  /**
   * Add EnableInterrupts into the table
   */
  static
  {
      MagicGenerator g = new DisableInterrupts();
      generators.put(getMethodReference(Magic.class, MagicNames.disableInterrupts, void.class), g);
  }
  
  /**
   * Generate a yield interrupt
   */
  private static final class Yield extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitINT_Imm(48);
      }
  }
  
  /**
   * Add Yield into the table
   */
  static
  {
      MagicGenerator g = new Yield();
      generators.put(getMethodReference(Magic.class, MagicNames.yield, void.class), g);
  }
  
  /**
   * Generate a segment register move
   */
  private static final class SetCS extends MagicGenerator
  {
      @Override
      void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
      {
          asm.emitPOP_Reg(S0);
          asm.emitMOVSEG(CS, S0);
      }
  }
  
  static
  {
      MagicGenerator g = new SetCS();
      generators.put(getMethodReference(Magic.class, MagicNames.setCS, int.class, void.class), g);
  }
  
  /*
   * Generate a CPUID instruction
   */
  private static final class CpuId extends MagicGenerator
  {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
    {
      asm.emitMOV_Reg_RegDisp(EAX, ESP, ONE_SLOT);
      // int[] is at the top of the stack
      asm.emitCPUID();
      /*
       * Push EAX, EBX, ECX, and EDX into the array
       */
      asm.emitPOP_Reg(EBP);
      asm.emitMOV_RegDisp_Reg(EBP, Offset.zero(), EAX);
      asm.emitMOV_RegDisp_Reg(EBP, Offset.zero().plus(4), EBX);
      asm.emitMOV_RegDisp_Reg(EBP, Offset.zero().plus(8), ECX);
      asm.emitMOV_RegDisp_Reg(EBP, Offset.zero().plus(12), EDX);
      /*
       * Pop the int[] off
       */
      asm.emitPOP_Reg(EAX);
    }
  }
  
  static
  {
    MagicGenerator g = new CpuId();
    generators.put(getMethodReference(Magic.class, MagicNames.cpuId, int.class, int[].class, void.class), g);
  }
  
  /*
   * Generate a RDMSR instruction
   */
  private static final class RdMsr extends MagicGenerator {

    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
    {
      // load the MSR register
      asm.emitPOP_Reg(ECX);
      asm.emitRDMSR();
      // push the value onto the stack
      if(VM.BuildFor64Addr)
      {
          asm.emitSHL_Reg_Imm_Quad(EDX, 32);
          asm.emitOR_Reg_Reg_Quad(EAX, EDX);
          asm.emitPUSH_Reg(EDX);
          asm.emitPUSH_Reg(EAX);
      }
      else
      {
          asm.emitPUSH_Reg(EDX);
      }
    }
  }
  
  static
  {
    MagicGenerator g = new RdMsr();
    generators.put(getMethodReference(Magic.class, MagicNames.rdMsr, int.class, long.class), g);
  }
  
  /*
   * Generate a WRMSR instruction
   */
  final static private class WrMsr extends MagicGenerator
  {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
    {
      // load the value
      asm.emitPOP_Reg(EDX);
      asm.emitPOP_Reg(EAX);
      asm.emitPOP_Reg(ECX);
      asm.emitWRMSR();
    }
  }
  static
  {
    MagicGenerator g = new WrMsr();
    generators.put(getMethodReference(Magic.class, MagicNames.wrMsr, int.class, long.class, void.class), g);
  }
  
  /*
   * Generate an integer byte swap
   */
  final static private class ByteSwap32 extends MagicGenerator
  {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
    {
      asm.emitPOP_Reg(S0);
      asm.emitBSWAP_Reg(S0);
      asm.emitPUSH_Reg(S0);
    }
  }
  static
  {
    MagicGenerator g = new ByteSwap32();
    generators.put(getMethodReference(Magic.class, MagicNames.byteSwap, int.class, int.class), g);
  }
  
  /*
   * Generate an short byte swap
   */
  final static private class ByteSwap16 extends MagicGenerator
  {
    @Override
    void generateMagic(Assembler asm, MethodReference m, RVMMethod cm, Offset sd)
    {
      /*
       * This is the ECX but it contains a short in the CX
       */
      asm.emitPOP_Reg(S0);
      /*
       * This is an 8bit instruction so really 
       * the registers are CL, CH
       */
      asm.emitXCHG_Reg_Reg(S0, EBP);
      /*
       * The byte swapped result is contained the CX
       */
      asm.emitPUSH_Reg(S0);
    }
  }
  static
  {
    MagicGenerator g = new ByteSwap16();
    generators.put(getMethodReference(Magic.class, MagicNames.byteSwap, short.class, short.class), g);
  }
}
