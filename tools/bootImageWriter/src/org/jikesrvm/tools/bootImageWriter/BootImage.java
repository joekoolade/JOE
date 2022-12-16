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
package org.jikesrvm.tools.bootImageWriter;

import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_CODE_END;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_CODE_SIZE;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_CODE_SIZE_LIMIT;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_CODE_START;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_DATA_SIZE;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_DATA_SIZE_LIMIT;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_DATA_START;
import static org.jikesrvm.HeapLayoutConstants.BOOT_IMAGE_RMAP_START;
import static org.jikesrvm.HeapLayoutConstants.MAX_BOOT_IMAGE_RMAP_SIZE;
import static org.jikesrvm.runtime.UnboxedSizeConstants.LOG_BYTES_IN_ADDRESS;
import static org.jikesrvm.runtime.UnboxedSizeConstants.LOG_BYTES_IN_WORD;
import static org.jikesrvm.tools.bootImageWriter.BootImageWriterMessages.fail;
import static org.jikesrvm.tools.bootImageWriter.BootImageWriterMessages.say;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;

import static joeq.Linker.ELF.ELFConstants.*;

import joeq.Linker.ELF.ELFConstants;
import joeq.Linker.ELF.ELFRandomAccessFile;
import joeq.Linker.ELF.ProgramHeader.LoadProgramHeader;
import joeq.Linker.ELF.Section;
import joeq.Linker.ELF.Section.SymTabSection;
import joeq.Linker.ELF.Section.StrTabSection;
import joeq.Linker.ELF.SymbolTableEntry;

import org.jikesrvm.VM;
import org.jikesrvm.compilers.common.CodeArray;
import org.jikesrvm.classloader.RVMArray;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMField;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.common.CompiledMethods;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.mm.mmtk.ScanBootImage;
import org.jikesrvm.objectmodel.BootImageInterface;
import org.jikesrvm.objectmodel.JavaHeader;
import org.jikesrvm.objectmodel.ObjectModel;
import org.jikesrvm.objectmodel.TIB;
import org.jikesrvm.runtime.Statics;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * Memory image of virtual machine that will be written to disk file and later
 * "booted".
 */
public class BootImage implements BootImageInterface {

  /**
   * Talk while we work?
   */
  private final boolean trace;

  /**
   * The data portion of the actual boot image
   */
  private final ByteBuffer bootImageData;

  /**
   * The code portion of the actual boot image
   */
  private final ByteBuffer bootImageCode;

  /**
   * The reference map for the boot image
   */
  private final byte[] referenceMap;
  private int referenceMapReferences = 0;
  private int referenceMapLimit = 0;
  private byte[] bootImageRMap;
  private int rMapSize = 0;

  /**
   * Offset of next free data word, in bytes
   */
  private Offset freeDataOffset = Offset.zero();

  /**
   * Offset of next free code word, in bytes
   */
  private Offset freeCodeOffset = Offset.zero();

  /**
   * Number of objects appearing in bootimage
   */
  private int numObjects;

  /**
   * Number of non-null object addresses appearing in bootimage
   */
  private int numAddresses;

  /**
   * Number of object addresses set to null because they referenced objects
   * that are not part of bootimage
   */
  private int numNulledReferences;

  /**
   * Data output file
   */
  private final RandomAccessFile dataOut;

  /**
   * Code output file
   */
  private final RandomAccessFile codeOut;


  /**
   * Code map file name
   */
  private final String imageCodeFileName;

  /**
   * Data map file name
   */
  private final String imageDataFileName;

  /**
   * Root map file name
   */
  private final String imageRMapFileName;

  /**
     * the elf image
     */
    private String jamoutFile;

    /**
     * Use mapped byte buffers? We need to truncate the byte buffer before writing
     * it to disk. This operation is supported on UNIX but not Windows.
   */
  private static final boolean mapByteBuffers = false;

    private SymTabSection symbolTable;
    private StrTabSection stringTable;

    /**
     * @param ltlEndian write words low-byte first?
     * @param t         turn tracing on?
     */
    BootImage(boolean ltlEndian, boolean t, String imageCodeFileName, String imageDataFileName, String imageRMapFileName, String elfImage) throws IOException
    {
        this.imageCodeFileName = imageCodeFileName;
        this.imageDataFileName = imageDataFileName;
        this.imageRMapFileName = imageRMapFileName;
        jamoutFile = elfImage;
        dataOut = new RandomAccessFile(imageDataFileName, "rw");
        dataOut.setLength(0);
        codeOut = new RandomAccessFile(imageCodeFileName, "rw");
        codeOut.setLength(0);
        if (mapByteBuffers)
        {
            bootImageData = dataOut.getChannel().map(MapMode.READ_WRITE, 0, BOOT_IMAGE_DATA_SIZE);
            bootImageCode = codeOut.getChannel().map(MapMode.READ_WRITE, 0, BOOT_IMAGE_CODE_SIZE);
        } else
        {
            bootImageData = ByteBuffer.allocate(BOOT_IMAGE_DATA_SIZE);
            bootImageCode = ByteBuffer.allocate(BOOT_IMAGE_CODE_SIZE);
        }
        System.out.println("BOOTIMAGE data="+BOOT_IMAGE_DATA_SIZE+" code="+BOOT_IMAGE_CODE_SIZE);
        ByteOrder endian = ltlEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        bootImageData.order(endian);
        bootImageCode.order(endian);
        referenceMap = new byte[BOOT_IMAGE_DATA_SIZE >> LOG_BYTES_IN_ADDRESS];
        trace = t;
        stringTable = new Section.StrTabSection(".strtab", Section.SHF_ALLOC, 0);
        symbolTable = new Section.SymTabSection(".symtab", Section.SHF_ALLOC, 0, stringTable);
    }

    public void writeElfFile(byte[] startupCode) throws IOException
    {
        RandomAccessFile execFile = new RandomAccessFile(jamoutFile, "rw");
        // truncate the file
        execFile.setLength(0);
        ELFRandomAccessFile elf = new ELFRandomAccessFile(ELFDATA2LSB, ET_EXEC, EM_386, 0x100000, execFile);

        /*
         * Setup the startup code
         * 
         * The program headers and section headers must be in sequence
         */
        LoadProgramHeader programHeader = new LoadProgramHeader(PF_X | PF_R | PF_W, 0x100000, 0x1000,
        startupCode.length, 0x8000);
        elf.addProgramHeader(programHeader);
        programHeader = new LoadProgramHeader(PF_X | PF_R | PF_W, BOOT_IMAGE_CODE_START.toInt(), 0x1000, getCodeSize(),
        BOOT_IMAGE_CODE_SIZE);
        elf.addProgramHeader(programHeader);
        programHeader = new LoadProgramHeader(PF_X | PF_R | PF_W, BOOT_IMAGE_DATA_START.toInt(), 0x1000, getDataSize(),
        BOOT_IMAGE_DATA_SIZE);
        elf.addProgramHeader(programHeader);
        programHeader = new LoadProgramHeader(PF_X | PF_R | PF_W, BOOT_IMAGE_RMAP_START.toInt(), 0x1000, getRMapSize(),
        MAX_BOOT_IMAGE_RMAP_SIZE);
        elf.addProgramHeader(programHeader);
        elf.addSection(Section.NullSection.INSTANCE);
        Section section = new Section.ProgBitsSectionImpl(".init",
        Section.SHF_ALLOC | Section.SHF_EXECINSTR | Section.SHF_WRITE, 0x100000, 0x1000, startupCode);
        elf.addSection(section);
        section = new Section.ProgBitsSectionImpl(".text",
        Section.SHF_ALLOC | Section.SHF_EXECINSTR | Section.SHF_WRITE, BOOT_IMAGE_CODE_START.toInt(), 0x1000,
        bootImageCode.array());
        elf.addSection(section);
        section = new Section.ProgBitsSectionImpl(".data", Section.SHF_ALLOC | Section.SHF_WRITE,
        BOOT_IMAGE_DATA_START.toInt(), 0x1000, bootImageData.array());
        elf.addSection(section);
        section = new Section.ProgBitsSectionImpl(".rodata", Section.SHF_ALLOC, BOOT_IMAGE_RMAP_START.toInt(), 0x1000,
        bootImageRMap);
        elf.addSection(section);
        elf.addSection(symbolTable);
        elf.addSection(stringTable);
        elf.write();
        execFile.close();
    }

    public void createSymbolTable()
    {
        for (int i = 0; i < CompiledMethods.numCompiledMethods(); ++i)
        {
            CompiledMethod compiledMethod = CompiledMethods.getCompiledMethodUnchecked(i);
            if (compiledMethod != null)
            {
                RVMMethod m = compiledMethod.getMethod();
                if (m != null && compiledMethod.isCompiled())
                {
                    CodeArray instructions = compiledMethod.getEntryCodeArray();
                    Address code = BootImageMap.getImageAddress(instructions.getBacking(), true);
                    String methodName = m.toString().replaceAll("\\< BootstrapCL\\, ", "");
                    methodName = methodName.replaceAll("\\; \\>", "");
                    symbolTable.addSymbol(new SymbolTableEntry(methodName, code.toInt(), 4, STB_GLOBAL, STT_FUNC, symbolTable));
                }
            }
        }
        byte type = 0; 
        // Reference JTOC fields
        // Restore previously unnecessary Statics data structures
        Statics.bootImageReportGeneration(MethodAddressMap.staticsJunk);
       for (int jtocSlot = Statics.middleOfTable, n = Statics.getHighestInUseSlot(); jtocSlot <= n; jtocSlot += Statics
        .getReferenceSlotSize())
        {
            Offset jtocOff = Statics.slotAsOffset(jtocSlot);
            Object obj = BootImageMap.getObject(MethodAddressMap.getIVal(jtocOff));
            Address address = (MethodAddressMap.getReferenceAddr(jtocOff, false));
            if (address.toInt() == 0)
                continue;
            String symbol=null, symbol0=null;
            RVMField field = BootImageWriter.getRvmStaticField(jtocOff);
            if (Statics.isReferenceLiteral(jtocSlot))
            {
                if (obj instanceof Class)
                {
                    symbol0 = symbol = obj.toString();
                    type = STT_OBJECT;
                }
                if (field != null)
                {
                    symbol0 = field.toString();
                    symbol = field.toString().replaceAll("\\< BootstrapCL\\, ", "");
                    symbol = symbol.replaceAll("\\; \\>", "");
                    type = STT_OBJECT;
                }
            } 
            else if (field != null)
            {
                // category = "field ";
                symbol0 = field.toString();
                symbol = field.toString().replaceAll("\\< BootstrapCL\\, ", "");
                symbol = symbol.replaceAll("\\; \\>", "");
                type = STT_OBJECT;
            } 
            else if (obj instanceof TIB)
            {
                // TIBs confuse the statics as their backing is written into the boot image
                // category = "tib ";
                RVMType rvmType = ((TIB) obj).getType();
                if(rvmType==null) continue;
                symbol0 = symbol = rvmType.toString();
                type = STT_OBJECT;
            } 
            else 
            {
                if (obj instanceof Class) {
                  symbol0 = symbol = obj.toString();
                  type = STT_OBJECT;
                } 
                else {
                  CompiledMethod m = MethodAddressMap.findMethodOfCode(obj);
                  if (m != null) {
                      symbol0 = m.getMethod().toString();
                    symbol = m.getMethod().toString().replaceAll("\\< BootstrapCL\\, ", "");;
                    symbol = symbol.replaceAll("\\; \\>", "");
                    type = STT_FUNC;
                  }
                  else continue;
                }
            }
            if(symbol == null) continue;
 //           say(symbol0 + ":" + symbol + ":" + Integer.toHexString(address.toInt()));
            symbolTable.addSymbol(new SymbolTableEntry(symbol, address.toInt(), 4, STB_GLOBAL, type, symbolTable));
        }
  }

  /**
   * Write boot image to disk.
   */
  public void write() throws IOException {
    if (trace) {
      say((numObjects / 1024)   + "k objects");
      say((numAddresses / 1024) + "k non-null object references");
      say(numNulledReferences + " references nulled because they are " +
          "non-jdk fields or point to non-bootimage objects");
      say(((Statics.getNumberOfReferenceSlots() + Statics.getNumberOfNumericSlots()) / 1024) + "k jtoc slots");
      say((getDataSize() / 1024) + "k data in image");
      say((getCodeSize() / 1024) + "k code in image");
      say("writing " + imageDataFileName);
    }
    if (!mapByteBuffers) {
      dataOut.write(bootImageData.array(), 0, getDataSize());
    } else {
      dataOut.getChannel().truncate(getDataSize());
    }
    dataOut.close();

    if (trace) {
      say("writing " + imageCodeFileName);
    }
    if (!mapByteBuffers) {
      codeOut.write(bootImageCode.array(), 0, getCodeSize());
    } else {
      codeOut.getChannel().truncate(getCodeSize());
    }
    codeOut.close();

    if (trace) {
      say("writing " + imageRMapFileName);
    }

    /* Now we generate a compressed reference map.  Typically we get 4 bits/address, but
       we'll create the in-memory array assuming worst case 1:1 compression.  Only the
       used portion of the array actually gets written into the image. */
    bootImageRMap = new byte[referenceMapReferences << LOG_BYTES_IN_WORD];
    rMapSize = ScanBootImage.encodeRMap(bootImageRMap, referenceMap, referenceMapLimit);
        File oldRmap = new File(imageRMapFileName);
        oldRmap.delete();
    FileOutputStream rmapOut = new FileOutputStream(imageRMapFileName);
    rmapOut.write(bootImageRMap, 0, rMapSize);
    rmapOut.flush();
    rmapOut.close();
    if (trace) {
      say("total refs: " + referenceMapReferences);
    }
    ScanBootImage.encodingStats();
  }

    public void writeMultiboot(byte[] startUpCode) throws IOException
    {
        RandomAccessFile jamOut = new RandomAccessFile(jamoutFile, "rw");
        // set file pointer to end
        jamOut.write(startUpCode);
        jamOut.write(bootImageData.array());
        jamOut.write(bootImageCode.array());
        jamOut.write(bootImageRMap);
        say("Create jam.out multiboot!");
        jamOut.close();
    }

  /**
   * Get image data size, in bytes.
   * @return image size
   */
  public int getDataSize() {
    return freeDataOffset.toInt();
  }

  /**
   * Get image code size, in bytes.
   * @return image size
   */
  public int getCodeSize() {
    return freeCodeOffset.toInt();
  }


  /**
   * return the size of the rmap
   */
    public int getRMapSize()
    {
        int size;
        if (rMapSize == 0)
        {
            System.out.println("rmap size references: " + referenceMapReferences);
            byte[] bootImageRMap0 = new byte[referenceMapReferences << LOG_BYTES_IN_WORD];
            size = ScanBootImage.encodeRMap(bootImageRMap0, referenceMap, referenceMapLimit);
            ScanBootImage.reset();
        } else
        {
            size = rMapSize;
        }
        return size;
  }

  /**
   * Allocate a scalar object.
   *
   * @param klass RVMClass object of scalar being allocated
   * @param needsIdentityHash needs an identity hash value
   * @param identityHashValue the value for the identity hash
   * @return address of object within bootimage
   */
  public Address allocateScalar(RVMClass klass, boolean needsIdentityHash, int identityHashValue) {
    numObjects++;
    BootImageWriter.logAllocation(klass, klass.getInstanceSize());
    return ObjectModel.allocateScalar(this, klass, needsIdentityHash, identityHashValue);
  }

  /**
   * Allocate an array object.
   *
   * @param array RVMArray object of array being allocated.
   * @param numElements number of elements
   * @param needsIdentityHash needs an identity hash value
   * @param identityHashValue the value for the identity hash
   * @param alignment special alignment value
   * @param alignCode Alignment-encoded value (AlignmentEncoding.ALIGN_CODE_NONE for none)
   * @return address of object within bootimage
   */
  public Address allocateArray(RVMArray array, int numElements, boolean needsIdentityHash, int identityHashValue, int alignCode) {
    numObjects++;
    BootImageWriter.logAllocation(array, array.getInstanceSize(numElements));
    return ObjectModel.allocateArray(this, array, numElements, needsIdentityHash, identityHashValue, alignCode);
  }

  /**
   * Allocate an array object.
   *
   * @param array RVMArray object of array being allocated.
   * @param numElements number of elements
   * @param needsIdentityHash needs an identity hash value
   * @param identityHashValue the value for the identity hash
   * @param align special alignment value
   * @param alignCode Alignment-encoded value (AlignmentEncoding.ALIGN_CODE_NONE for none)
   * @return address of object within bootimage
   */
  public Address allocateArray(RVMArray array, int numElements, boolean needsIdentityHash, int identityHashValue, int align, int alignCode) {
    numObjects++;
    BootImageWriter.logAllocation(array, array.getInstanceSize(numElements));
    return ObjectModel.allocateArray(this, array, numElements, needsIdentityHash, identityHashValue, align, alignCode);
  }

  /**
   * Allocate an array object.
   *
   * @param array RVMArray object of array being allocated.
   * @param numElements number of elements
   * @return address of object within bootimage
   */
  public Address allocateCode(RVMArray array, int numElements) {
    numObjects++;
    BootImageWriter.logAllocation(array, array.getInstanceSize(numElements));
    return ObjectModel.allocateCode(this, array, numElements);
  }

  @Override
  public Address allocateDataStorage(int size, int align, int offset) {
    size = roundAllocationSize(size);
    Offset unalignedOffset = freeDataOffset;
    freeDataOffset = MemoryManager.alignAllocation(freeDataOffset, align, offset);
    if (VM.ExtremeAssertions) {
      VM._assert(freeDataOffset.plus(offset).toWord().and(Word.fromIntSignExtend(align - 1)).isZero());
      VM._assert(freeDataOffset.toWord().and(Word.fromIntSignExtend(3)).isZero());
    }
    Offset lowAddr = freeDataOffset;
    freeDataOffset = freeDataOffset.plus(size);
    if (!VM.AllowOversizedImages && freeDataOffset.sGT(Offset.fromIntZeroExtend(BOOT_IMAGE_DATA_SIZE_LIMIT)))
      fail("bootimage full (need at least " + size + " more bytes for data). " +
           "To ignore this, add config.allowOversizedImage=true to the configuration you are using " +
           "or increase BOOT_IMAGE_DATA_SIZE_LIMIT in HeapLayoutConstants.template .");

    ObjectModel.fillAlignmentGap(this, BOOT_IMAGE_DATA_START.plus(unalignedOffset),
                                    lowAddr.minus(unalignedOffset).toWord().toExtent());
    return BOOT_IMAGE_DATA_START.plus(lowAddr);
  }

  /**
   * Round a size in bytes up to the next value of MIN_ALIGNMENT
   */
  private int roundAllocationSize(int size) {
    return size + ((-size) & ((1 << JavaHeader.LOG_MIN_ALIGNMENT) - 1));
  }

  @Override
  public Address allocateCodeStorage(int size, int align, int offset) {
    size = roundAllocationSize(size);
    Offset unalignedOffset = freeCodeOffset;
    freeCodeOffset = MemoryManager.alignAllocation(freeCodeOffset, align, offset);
    if (VM.ExtremeAssertions) {
      VM._assert(freeCodeOffset.plus(offset).toWord().and(Word.fromIntSignExtend(align - 1)).isZero());
      VM._assert(freeCodeOffset.toWord().and(Word.fromIntSignExtend(3)).isZero());
    }
    Offset lowAddr = freeCodeOffset;
    freeCodeOffset = freeCodeOffset.plus(size);
    if (!VM.AllowOversizedImages && freeCodeOffset.sGT(Offset.fromIntZeroExtend(BOOT_IMAGE_CODE_SIZE_LIMIT)))
      fail("bootimage full (need at least " + size + " more bytes for code). " +
          "To ignore this, add config.allowOversizedImage=true to the configuration you are using " +
          "or increase BOOT_IMAGE_CODE_SIZE_LIMIT in HeapLayoutConstants.template .");

    ObjectModel.fillAlignmentGap(this, BOOT_IMAGE_CODE_START.plus(unalignedOffset),
                                    lowAddr.minus(unalignedOffset).toWord().toExtent());

    return BOOT_IMAGE_CODE_START.plus(lowAddr);
  }

  /**
   * Reset the allocator as if no allocation had occured.  This is
   * useful to allow a "trial run", as is done to establish the offset
   * of the JTOC for the entry in the boot image record---so its
   * actual address can be computed early in the build process.
   */
  public void resetAllocator() {
    freeDataOffset = Offset.zero();
    freeCodeOffset = Offset.zero();
  }

  @Override
  public void setByte(Address address, int value) {
    int idx;
    ByteBuffer data;
    if (address.GE(BOOT_IMAGE_CODE_START) && address.LE(BOOT_IMAGE_CODE_END)) {
      idx = address.diff(BOOT_IMAGE_CODE_START).toInt();
      data = bootImageCode;
    } else {
      idx = address.diff(BOOT_IMAGE_DATA_START).toInt();
      data = bootImageData;
    }
    data.put(idx, (byte)value);
  }


  /**
   * Set a byte in the reference bytemap to indicate that there is an
   * address in the boot image at this offset.  This can be used for
   * relocatability and for fast boot image scanning at GC time.
   *
   * @param address The offset into the boot image which contains an
   * address.
   */
  private void markReferenceMap(Address address) {
    int referenceIndex = address.diff(BOOT_IMAGE_DATA_START).toInt() >> LOG_BYTES_IN_ADDRESS;
    if (referenceMap[referenceIndex] == 0) {
      referenceMap[referenceIndex] = 1;
      referenceMapReferences++;
      if (referenceIndex > referenceMapLimit) referenceMapLimit = referenceIndex;
    }
  }

  @Override
  public void setHalfWord(Address address, int value) {
    int idx = address.diff(BOOT_IMAGE_DATA_START).toInt();
    bootImageData.putChar(idx, (char)value);
  }

  @Override
  public void setFullWord(Address address, int value) {
    int idx;
    ByteBuffer data;
    if (address.GE(BOOT_IMAGE_CODE_START) && address.LE(BOOT_IMAGE_CODE_END)) {
      idx = address.diff(BOOT_IMAGE_CODE_START).toInt();
      data = bootImageCode;
    } else {
      idx = address.diff(BOOT_IMAGE_DATA_START).toInt();
      data = bootImageData;
    }
    data.putInt(idx, value);
  }

  @Override
  public void setAddressWord(Address address, Word value, boolean objField, boolean root) {
    if (VM.VerifyAssertions) VM._assert(!root || objField);
    if (objField) value = MemoryManager.bootTimeWriteBarrier(value);
    if (root) markReferenceMap(address);
    if (VM.BuildFor32Addr)
      setFullWord(address, value.toInt());
    else
      setDoubleWord(address, value.toLong());
    numAddresses++;
  }

  /**
   * Fill in 4/8 bytes of bootimage, as null object reference.
   *
   * @param address address of target
   * @param objField true if this word is an object field (as opposed
   * to a static, or TIB, or some other metadata)
   * @param root Does this slot contain a possible reference into the heap? (objField must also be true)
   * @param genuineNull true if the value is a genuine null and
   * shouldn't be counted as a blanked field
   */
  public void setNullAddressWord(Address address, boolean objField, boolean root, boolean genuineNull) {
    setAddressWord(address, Word.zero(), objField, root);
    if (!genuineNull)
      numNulledReferences += 1;
  }

  @Override
  public void setNullAddressWord(Address address, boolean objField, boolean root) {
    setNullAddressWord(address, objField, root, true);
  }

  @Override
  public void setDoubleWord(Address address, long value) {
    int idx;
    ByteBuffer data;
    if (address.GE(BOOT_IMAGE_CODE_START) && address.LE(BOOT_IMAGE_CODE_END)) {
      idx = address.diff(BOOT_IMAGE_CODE_START).toInt();
      data = bootImageCode;
    } else {
      idx = address.diff(BOOT_IMAGE_DATA_START).toInt();
      data = bootImageData;
    }
    data.putLong(idx, value);
  }

  /**
   * Keep track of how many references were set null because they pointed to
   * non-bootimage objects.
   */
  public void countNulledReference() {
    numNulledReferences += 1;
  }

    public String getCodeFileName()
    {
        return imageCodeFileName;
    }

    public String getDataFileName()
    {
        return imageDataFileName;
    }

    public String getRMapFileName()
    {
        return imageRMapFileName;
    }
}
