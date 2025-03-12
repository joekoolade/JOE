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
package org.jikesrvm.runtime;

import org.jikesrvm.compilers.common.CodeArray;
import org.jikesrvm.architecture.AbstractRegisters;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.objectmodel.TIB;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.Intrinsic;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;
import org.vmmagic.unboxed.WordArray;

/**
 * Magic methods for accessing raw machine memory, registers, and
 * operating system calls.
 *
 * <p> These are "inline assembler functions" that cannot be implemented in
 * Java code. Their names are recognized by RVM's compilers
 * and cause inline machine code to be generated instead of
 * actual method calls.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Intrinsic
public final class Magic {

  //---------------------------------------//
  // Register and Psuedo-Register Access.  //
  //---------------------------------------//

  /** @return contents of "stack frame pointer" register. */
  public static Address getFramePointer() {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /** @return contents of "JTOC" register. */
  public static Address getTocPointer() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return BootRecord.the_boot_record.tocRegister;
  }

  /** @return contents of "JTOC" register */
  public static Address getJTOC() {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /** @return contents of "thread" register. */
  public static RVMThread getThreadRegister() {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Sets contents of "thread" register.
   * @param p new contents of the thread register
   */
  public static void setThreadRegister(RVMThread p) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /** @return contents of ESI, as a RVMThread. NOTE: IA-specific */
  public static RVMThread getESIAsThread() {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Sets contents of ESI to hold a reference to a thread object. NOTE: IA-specific.
   * @param p the thread
   */
  public static void setESIAsThread(RVMThread p) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Reads contents of hardware time base registers.
   * <p>
   * Note:     we think that 1 "tick" == 4 "machine cycles", but this seems to be
   *           undocumented and may vary across processor implementations.
   * @return number of ticks (epoch undefined)
   */
  public static long getTimeBase() {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  //---------------------------------------//
  //       Stackframe Manipulation         //
  //---------------------------------------//

  /**
   * Get fp for parent frame
   * @param fp frame pointer for child frame
   * @return the frame pointer of the parent frame
   */
  public static Address getCallerFramePointer(Address fp) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Set fp for parent frame
   * @param fp frame pointer for child frame
   * @param newCallerFP new value for caller frame pointer
   */
  public static void setCallerFramePointer(Address fp, Address newCallerFP) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * @param fp its frame pointer
   * @return the compiled Method ID for the frame
   */
  public static int getCompiledMethodID(Address fp) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Sets the Compiled Method ID for a frame.
   * @param fp its frame pointer
   * @param newCMID a new cmid for the frame
   */
  public static void setCompiledMethodID(Address fp, int newCMID) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * @param fp its frame pointer.
   * @return next instruction address for a frame
   */
  public static Address getNextInstructionAddress(Address fp) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * @param fp its frame pointer
   * @return location containing return address for a frame
   */
  public static Address getReturnAddressLocation(Address fp) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get return address for a frame in a case where the frame is
   * known not to be a trampoline frame.
   *
   * @param fp its frame pointer
   * @return the return address
   */
  @Uninterruptible
  public static Address getReturnAddressUnchecked(Address fp) {
    Address ip = getReturnAddressLocation(fp).loadAddress();
    if (VM.VerifyAssertions) VM._assert(!RVMThread.isTrampolineIP(ip));
    return ip;
  }

  /**
   * Get return address for a frame in the current thread
   *
   * @param fp its frame pointer
   * @return the return address
   */
  @Uninterruptible
  public static Address getReturnAddress(Address fp) {
    return getReturnAddress(fp, RVMThread.getCurrentThread());
  }

  /**
   * Get return address for a frame in a specific thread
   *
   * @param fp its frame pointer
   * @param thread the thread whose stack is being examined
   * @return the return address
   */
  @Uninterruptible
  public static Address getReturnAddress(Address fp, RVMThread thread) {
    Address ip = getReturnAddressLocation(fp).loadAddress();
    if (RVMThread.isTrampolineIP(ip))
      return thread.getTrampolineHijackedReturnAddress();
    else
      return ip;
  }

  /**
   * Sets return address for a frame.
   * @param fp its frame pointer
   * @param v the new return address
   */
  @Uninterruptible
  public static void setReturnAddress(Address fp, Address v) {
    getReturnAddressLocation(fp).store(v);
  }

  //---------------------------------------//
  //           Memory Access.              //
  //---------------------------------------//

  /**
   * Get unsigned byte at arbitrary (byte) offset from object. The
   * most significant 24bits of the result will be 0.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the byte at the location
   */
  public static byte getUnsignedByteAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get byte at arbitrary (byte) offset from object. The most
   * significant 24bits of the result will be the same as the most
   * significant bit in the byte.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the byte at the location
   */
  public static byte getByteAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get char at arbitrary (byte) offset from object. The most
   * significant 16bits will be 0.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the char at the location
   */
  public static char getCharAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return (char) -1;
  }

  /**
   * Get short at arbitrary (byte) offset from object. The most
   * significant 16bits will be the same as the most significant bit
   * in the short.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the short at the location
   */
  public static short getShortAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return (short) -1;
  }

  /**
   * Get int at arbitrary (byte) offset from object.
   * Use getIntAtOffset(obj, ofs) instead of getMemoryInt(objectAsAddress(obj)+ofs)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the int at the location
   */
  public static int getIntAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get long at arbitrary (byte) offset from object.
   * Use getlongAtOffset(obj, ofs) instead of two getIntAtOffset

   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the long at the location
   */
  public static long getLongAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get float at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the float at the location
   */
  public static float getFloatAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get double at arbitrary (byte) offset from object.
   * Use getDoubleAtOffset(obj, ofs) instead of two getIntAtOffset
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the double at the location
   */
  public static double getDoubleAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get Object at arbitrary (byte) offset from object.
   * Use getObjectAtOffset(obj, ofs) instead of
   * addressAsObject(getMemoryAddress(objectAsAddress(obj)+ofs))
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the object at the location
   */
  public static Object getObjectAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Object at arbitrary (byte) offset from object.
   * Use getObjectAtOffset(obj, ofs) instead of
   * addressAsObject(getMemoryAddress(objectAsAddress(obj)+ofs))
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param locationMetadata metadata about the location for the compilers
   * @return the object at the computed address
   */
  public static Object getObjectAtOffset(Object object, Offset offset, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Word at arbitrary (byte) offset from object.
   * Use getWordAtOffset(obj, ofs) instead of getMemoryWord(objectAsAddress(obj)+ofs)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the word at the computed address
   */
  public static Word getWordAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return Word.max();
  }

  /**
   * Get Word at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param locationMetadata metadata about the location for the compilers
   * @return the Word at the computed address
   */
  public static Word getWordAtOffset(Object object, Offset offset, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Address at arbitrary (byte) offset from object.
   * Use getAddressAtOffset(obj, ofs) instead of getMemoryWord(objectAsAddress(obj)+ofs)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the Address at the computed address
   */
  public static Address getAddressAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Address at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param locationMetadata metadata about the location for the compilers
   * @return the Address at the computed address
   */
  public static Address getAddressAtOffset(Object object, Offset offset, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Extent at arbitrary (byte) offset from object.
   * Use getExtentAtOffset(obj, ofs) instead of getMemoryWord(objectAsAddress(obj)+ofs)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the Extent at the computed address
   */
  public static Extent getExtentAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Extent at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param locationMetadata metadata about the location for the compilers
   * @return the Extent at the computed address
   */
  public static Extent getExtentAtOffset(Object object, Offset offset, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Offset at arbitrary (byte) offset from object.
   * Use getOffsetAtOffset(obj, ofs) instead of getMemoryWord(objectAsAddress(obj)+ofs)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the Offset at the computed address
   */
  public static Offset getOffsetAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get Offset at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param locationMetadata metadata about the location for the compilers
   * @return the Offset at the computed address
   */
  public static Offset getOffsetAtOffset(Object object, Offset offset, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get TIB at arbitrary (byte) offset from object.
   * Use getTIBAtOffset(obj, ofs) instead of
   * (TIB])addressAsObject(getMemoryAddr(objectAsAddress(obj)+ofs))
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the TIB at the computed address
   */
  public static TIB getTIBAtOffset(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Set boolean at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setBooleanAtOffset(Object object, Offset offset, boolean newvalue) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set boolean at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setBooleanAtOffset(Object object, Offset offset, boolean newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set byte at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setByteAtOffset(Object object, Offset offset, byte newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set byte at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setByteAtOffset(Object object, Offset offset, byte newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set char at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setCharAtOffset(Object object, Offset offset, char newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set char at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setCharAtOffset(Object object, Offset offset, char newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set short at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setShortAtOffset(Object object, Offset offset, short newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set short at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setShortAtOffset(Object object, Offset offset, short newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set int at arbitrary (byte) offset from object.
   * Use setIntAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setIntAtOffset(Object object, Offset offset, int newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set int at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setIntAtOffset(Object object, Offset offset, int newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set long at arbitrary (byte) offset from object.
   * Use setlongAtOffset(obj, ofs) instead of two setIntAtOffset
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setLongAtOffset(Object object, Offset offset, long newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set long at arbitrary (byte) offset from object. Use setlongAtOffset(obj,
   * ofs) instead of two setIntAtOffset
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setLongAtOffset(Object object, Offset offset, long newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set float at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setFloatAtOffset(Object object, Offset offset, float newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set float at arbitrary (byte) offset from object.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setFloatAtOffset(Object object, Offset offset, float newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set double at arbitrary (byte) offset from object.
   * Use setDoubleAtOffset(obj, ofs) instead of two setIntAtOffset
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setDoubleAtOffset(Object object, Offset offset, double newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set double at arbitrary (byte) offset from object. Use
   * setDoubleAtOffset(obj, ofs) instead of two setIntAtOffset
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setDoubleAtOffset(Object object, Offset offset, double newvalue, int locationMetadata) {
    if (VM.VerifyAssertions)
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Word at arbitrary (byte) offset from object.
   * Use setWordAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setWordAtOffset(Object object, Offset offset, Word newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Word at arbitrary (byte) offset from object.
   * Use setWordAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setWordAtOffset(Object object, Offset offset, Word newvalue, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Address at arbitrary (byte) offset from object.
   * Use setAddressAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setAddressAtOffset(Object object, Offset offset, Address newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Address at arbitrary (byte) offset from object.
   * Use setAddressAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setAddressAtOffset(Object object, Offset offset, Address newvalue, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Extent at arbitrary (byte) offset from object.
   * Use setExtentAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setExtentAtOffset(Object object, Offset offset, Extent newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Extent at arbitrary (byte) offset from object.
   * Use setExtenttOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setExtentAtOffset(Object object, Offset offset, Extent newvalue, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Offset at arbitrary (byte) offset from object.
   * Use setOffsetAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setOffsetAtOffset(Object object, Offset offset, Offset newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Offset at arbitrary (byte) offset from object.
   * Use setOffsetAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, new)
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setOffsetAtOffset(Object object, Offset offset, Offset newvalue, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Object at arbitrary (byte) offset from object.
   * Use setObjectAtOffset(obj, ofs, new) instead of setMemoryWord(objectAsAddress(obj)+ofs, objectAsAddress(new))
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   */
  public static void setObjectAtOffset(Object object, Offset offset, Object newvalue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Set Object at arbitrary (byte) offset from object
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param newvalue the value to write to the computed address
   * @param locationMetadata metadata about the location for the compilers
   */
  public static void setObjectAtOffset(Object object, Offset offset, Object newvalue, int locationMetadata) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }


  //---------------------------------------//
  //    Atomic Memory Access Primitives.   //
  //---------------------------------------//

  /**
   * Gets contents of (object + offset) and begin conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the int that was read out from the computed address
   */
  public static int prepareInt(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Get contents of (object + offset) and begin conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the object that was read out from the computed address
   */
  public static Object prepareObject(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get contents of (object + offset) and begin conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the Address that was read out from the computed address
   */
  public static Address prepareAddress(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return Address.max();
  }

  /**
   * Get contents of (object + offset) and begin conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the Word that was read out from the computed address
   */
  public static Word prepareWord(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return Word.max();
  }

  /**
   * Get contents of (object + offset) and begin conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @return the long that was read out from the computed address
   */
  @Uninterruptible
  public static long prepareLong(Object object, Offset offset) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Sets the memory at (object + offset) to newValue if its contents are oldValue.
   * Must be paired with a preceding prepare (which returned the oldValue).
   * Ends conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param oldValue the value that is expected to be at the computed address
   * @param newValue the value that is supposed to be written to the computed
   *  address
   * @return {@code true} if successful, {@code false} if not
   */
  public static boolean attemptInt(Object object, Offset offset, int oldValue, int newValue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return false;
  }

  /**
   * Sets the memory at (object + offset) to newValue if its contents are oldValue.
   * Must be paired with a preceding prepare (which returned the oldValue)
   * Ends conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param oldValue the value that is expected to be at the computed address
   * @param newValue the value that is supposed to be written to the computed
   *  address
   * @return {@code true} if successful, {@code false} if not
   */
  public static boolean attemptObject(Object object, Offset offset, Object oldValue, Object newValue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return false;
  }

  /**
   * Sets the memory at (object + offset) to newValue if its contents are oldValue.
   * Must be paired with a preceding prepare (which returned the oldValue)
   * Ends conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param oldValue the value that is expected to be at the computed address
   * @param newValue the value that is supposed to be written to the computed
   *  address
   * @return {@code true} if successful, {@code false} if not
   */
  public static boolean attemptAddress(Object object, Offset offset, Address oldValue, Address newValue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return false;
  }

  /**
   * Sets the memory at (object + offset) to newValue if its contents are oldValue.
   * Must be paired with a preceding prepare (which returned the oldValue)
   * Ends conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param oldValue the value that is expected to be at the computed address
   * @param newValue the value that is supposed to be written to the computed
   *  address
   * @return {@code true} if successful, {@code false} if not
   */
  public static boolean attemptWord(Object object, Offset offset, Word oldValue, Word newValue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return false;
  }

  /**
   * Sets the memory at (object + offset) to newValue if its contents are oldValue.
   * Must be paired with a preceding prepare (which returned the oldValue)
   * Ends conditional critical section.
   *
   * @param object the object serving as start address
   * @param offset the offset from the object
   * @param oldValue the value that is expected to be at the computed address
   * @param newValue the value that is supposed to be written to the computed
   *  address
   * @return {@code true} if successful, {@code false} if not
   */
  public static boolean attemptLong(Object object, Offset offset, long oldValue,
      long newValue) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return false;
  }

  //---------------------------------------//
  //             Type Conversion.          //
  //---------------------------------------//

  @Entrypoint
  private static ObjectAddressRemapper objectAddressRemapper;

  /**
   * Specify how to handle "objectAsAddress" and "addressAsObject" casts.
   * Used by debugger and boot image writer.
   *
   * @param x the remapper ot use
   */
  public static void setObjectAddressRemapper(ObjectAddressRemapper x) {
    objectAddressRemapper = x;
  }

  /**
   * Cast bits.
   * Note: the returned integer is only valid until next garbage collection
   *   cycle (thereafter the referenced object might have moved and
   *   its address changed)
   * @param <T> the object's type
   * @param object object reference
   * @return object reference as bits
   */
  public static <T> Address objectAsAddress(T object) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    }

    if (objectAddressRemapper == null) {
      return Address.zero();                 // tool isn't interested in remapping
    }

    return objectAddressRemapper.objectAsAddress(object);
  }

  /**
   * Certain objects aren't replicated in the boot image to save space.
   * @param <T> the object's type
   * @param object to intern
   * @return interned object
   */
  public static <T> T bootImageIntern(T object) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    }

    if (objectAddressRemapper == null) {
      return object;                 // tool isn't interested in remapping
    }

    return objectAddressRemapper.intern(object);
  }

  /**
   * Certain objects aren't replicated in the boot image to save space.
   * @param object to intern
   * @return interned object
   */
  public static int bootImageIdentityHashCode(Object object) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    }

    if (objectAddressRemapper == null) {
      // shouldn't create identity hash codes when we cannot record the effect, ignore if we're running a tool
      if (VM.VerifyAssertions) VM._assert(VM.runningTool || VM.writingImage);
      return System.identityHashCode(object);
    }

    return objectAddressRemapper.identityHashCode(object);
  }

  /**
   * Cast bits.
   * @param address object reference as bits
   * @return object reference
   */
  public static Object addressAsObject(Address address) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }

    if (objectAddressRemapper == null) {
      return null;               // tool isn't interested in remapping
    }

    return objectAddressRemapper.addressAsObject(address);
  }

  /**
   * Cast bits of code array into an object
   * Note: for use by Statics when assigning slots to static method pointers
   * @param code the code array to convert
   * @return object reference
   */
  public static Object codeArrayAsObject(CodeArray code) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    }

    return code;
  }

  /**
   * Cast bits of tib into an object
   * Note: for use by Statics when assigning slots
   * @param tib the tib to convert
   * @return object reference
   */
  public static Object tibAsObject(TIB tib) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    }

    return tib;
  }

  /**
   * Cast bits.
   * @param address object array reference as bits
   * @return object array reference
   */
  public static TIB addressAsTIB(Address address) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return null;
  }

  /**
   * Cast object.
   * @param object object reference
   * @return object reference as type (no checking on cast)
   */
  public static RVMType objectAsType(Object object) {
    if (VM.VerifyAssertions && VM.runningVM) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler

    return (RVMType)object;
  }

  /**
   * Cast object.
   * Note:     for use by GC to avoid checkcast during GC
   * @param object object reference
   * @return object reference as thread (no checking on cast)
   */
  public static RVMThread objectAsThread(Object object) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }
  /**
   * Cast bits.
   * @param number A floating point number
   * @return   <code>number</code> as bits
   */
  public static int floatAsIntBits(float number) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Cast bits.
   * @param number as bits
   * @return <code>number</code> as a <code>float</code>
   */
  public static float intBitsAsFloat(int number) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Cast bits.
   * @param number as double
   * @return number as bits
   */
  public static long doubleAsLongBits(double number) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Cast bits.
   * @param number as bits
   * @return number as double
   */
  public static double longBitsAsDouble(long number) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * Recast.
   * Note:     for use by GC to avoid checkcast during GC
   * @param byte_array an address
   * @return byte array (byte[])  object reference
   */
  public static byte[] addressAsByteArray(Address byte_array) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Cast object.
   * Note:     for use in dynamic type checking (avoid dynamic type checking in impl. of dynamic type checking)
   * @param object an object that must be a short array
   * @return short array (short[])  object reference
   */
  public static short[] objectAsShortArray(Object object) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return (short[]) object;
  }

  /**
   * Cast object.
   * Note:     for use in dynamic type checking (avoid dynamic type checking in impl. of dynamic type checking)
   * @param object an object that must be an int array
   * @return int array (int[])  object reference
   */
  public static int[] objectAsIntArray(Object object) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return (int[]) object;
  }

  //---------------------------------------//
  //          Object Header Access.        //
  //---------------------------------------//

  /**
   * Get an object's type.
   * @param object object reference
   * @return object type
   */
  public static RVMType getObjectType(Object object) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    return null;
  }

  /**
   * Get an array's length.
   * @param object object reference
   * @return array length (number of elements)
   */
  public static int getArrayLength(Object object) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED); // call site should have been hijacked by magic in compiler
    return -1;
  }

  //---------------------------------------//
  // Method Invocation And Stack Trickery. //
  //---------------------------------------//

  /**
   * Saves current thread state.  Stores the values in the hardware registers
   * into a Registers object.
   * <p>
   * We used to use this to implement thread switching, but we have a
   * threadSwitch magic now that does both of these in a single step as that
   * is less error-prone.  saveThreadState is now only used in the
   * implementation of athrow (RuntimeEntrypoints.athrow).
   * <p>
   * The following registers are saved:
   * <ul>
   *  <li>nonvolatile fpr registers
   *  <li>nonvolatile gpr registers
   *  <li>FRAME_POINTER register
   *  <li>THREAD_ID     "register"
   * </ul>
   * @param registers place to save register values
   */
  // PNT: make a version of this that implicitly uses contextRegisters.
  public static void saveThreadState(AbstractRegisters registers) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }
  /**
   * Switch threads.
   * The following registers are saved/restored
   * <ul>
   *  <li>nonvolatile fpr registers
   *  <li>nonvolatile gpr registers
   *  <li>FRAME_POINTER register
   *  <li>THREAD_ID     "register"
   * </ul>   *
   * @param currentThread thread that is currently running
   * @param restoreRegs   registers from which we should restore
   *                      the saved hardware state of another thread.
   */
  public static void threadSwitch(RVMThread currentThread, AbstractRegisters restoreRegs) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Resume execution with specified thread exception state.
   * <p>
   * Restores virtually all registers (details vary by architecture).
   * But, the following are _NOT_ restored
   * <ul>
   *  <li>JTOC_POINTER
   *  <li>THREAD_REGISTER
   * </ul>
   * does not return (execution resumes at new IP)
   * @param registers register values to be used
   */
  public static void restoreHardwareExceptionState(AbstractRegisters registers) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Return to caller of current method, resuming execution on a new stack
   * that's a copy of the original.
   * @param fp value to place into FRAME_POINTER register
   */
  public static void returnToNewStack(Address fp) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Transfer execution to target of a dynamic bridge method.
   * <p>
   * The following registers are restored:  non-volatiles, volatiles
   * <p>
   * Note: this method must only be called from a DynamicBridge method because it
   * never returns (target method returns to caller of dynamic bridge method)
   * @param instructions target method
   */
  public static void dynamicBridgeTo(CodeArray instructions) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * Call &lt;clinit&gt; method with no argument list
   * @param clinit the code of the class initializer
   * @throws Exception all expections from invocation of the class initializer
   *  are passed along
   */
  public static void invokeClassInitializer(CodeArray clinit) throws Exception {
    // Since the real method passes exceptions up. Constructor might throw an arbitrary exception.
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    throw new Exception("UNREACHED");
  }

  /**
   * @param code the method's code
   * @param gprs parameters passed in the general purpose registers
   * @param fprs parameters passed in the floating point registers
   * @param fprmeta meta-data (e.g. flags) about the floating point registers
   * @param spills parameters passed on the stack
   */
  public static void invokeMethodReturningVoid(CodeArray code, WordArray gprs, double[] fprs, byte[] fprmeta, WordArray spills) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
  }

  /**
   * @param code the method's code
   * @param gprs parameters passed in the general purpose registers
   * @param fprs parameters passed in the floating point registers
   * @param fprmeta meta-data (e.g. flags) about the floating point registers
   * @param spills parameters passed on the stack
   * @return the return value of the called method
   */
  public static int invokeMethodReturningInt(CodeArray code, WordArray gprs, double[] fprs, byte[] fprmeta, WordArray spills) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * @param code the method's code
   * @param gprs parameters passed in the general purpose registers
   * @param fprs parameters passed in the floating point registers
   * @param fprmeta meta-data (e.g. flags) about the floating point registers
   * @param spills parameters passed on the stack
   * @return the return value of the called method
   */
  public static long invokeMethodReturningLong(CodeArray code, WordArray gprs, double[] fprs, byte[] fprmeta, WordArray spills) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * @param code the method's code
   * @param gprs parameters passed in the general purpose registers
   * @param fprs parameters passed in the floating point registers
   * @param fprmeta meta-data (e.g. flags) about the floating point registers
   * @param spills parameters passed on the stack
   * @return the return value of the called method
   */
  public static float invokeMethodReturningFloat(CodeArray code, WordArray gprs, double[] fprs, byte[] fprmeta, WordArray spills) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * @param code the method's code
   * @param gprs parameters passed in the general purpose registers
   * @param fprs parameters passed in the floating point registers
   * @param fprmeta meta-data (e.g. flags) about the floating point registers
   * @param spills parameters passed on the stack
   * @return the return value of the called method
   */
  public static double invokeMethodReturningDouble(CodeArray code, WordArray gprs, double[] fprs, byte[] fprmeta, WordArray spills) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return -1;
  }

  /**
   * @param code the method's code
   * @param gprs parameters passed in the general purpose registers
   * @param fprs parameters passed in the floating point registers
   * @param fprmeta meta-data (e.g. flags) about the floating point registers
   * @param spills parameters passed on the stack
   * @return the return value of the called method
   */
  public static Object invokeMethodReturningObject(CodeArray code, WordArray gprs, double[] fprs, byte[] fprmeta, WordArray spills) {
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    return null;
  }

  //---------------------------------------//
  //            Memory Fences.             //
  //---------------------------------------//

  /**
   * Emits a strong memory fence, used to enforce StoreLoad in the JMM. A StoreLoad
   * barrier ensures that the data that was stored by the instruction before the
   * barrier is visible to all load instructions after the barrier.
   * <p>
   * Note: A StoreLoad barrier includes all other barriers on all platforms
   * that we currently support (IA32 and PPC).
   */
  public static void fence() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
  }

  /**
   * Emits an instruction that provides both a LoadLoad and a LoadStore barrier.
   * A LoadLoad barrier ensures that the data accessed by all load instructions
   * before the barrier is loaded before any data from load instructions after
   * the barrier.
   * A LoadStore barrier ensures that the data accessed by all load instructions
   * before the barrier is loaded before any data store instructions after the barrier
   * are completed.
   * <p>
   * We don't provide separate methods for LoadStore and LoadLoad barriers because
   * the appropriate instructions for IA32 and PPC provide both barriers.
   */
  public static void combinedLoadBarrier() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
  }

  /**
   * Emits a StoreStore barrier. A StoreStore barrier ensures that the data
   * that was stored by the instruction before the barrier is visible to all
   * subsequent store instructions.
   */
  public static void storeStoreBarrier() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
  }

  //---------------------------------------//
  //            Cache Management.          //
  //---------------------------------------//

  /**** NOTE: all per-address operations now live in vmmagic.Address *****/

  /**
   * Wait for all preceeding instructions to complete and discard any
   * prefetched instructions on this processor.
   */
  public static void synchronizeInstructionCache() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
  }

  //---------------------------------------//
  //           Misc.                       //
  //---------------------------------------//

  /**
   * On IA32, emit a PAUSE instruction, to optimize spin-wait loops.
   */
  public static void pause() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
  }

    /**
     * emit a HLT instruction
     */
    public static void halt()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }

  /**
   * A hardware SQRT instruction.
   * <p>
   * Note: this method may only be if the current configuration supports
   * hardware floating point, i.e. if
   * {@link org.jikesrvm.Configuration#BuildForHwFsqrt} is true.
   *
   * @param value the value whose square root will be computed
   * @return the SQRT of the given value
   */
  public static float sqrt(float value) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return -1.0f; // which should upset them even if assertions aren't enabled ...
  }

  /**
   * A hardware SQRT instruction.
   * <p>
   * Note: this method may only be if the current configuration supports
   * hardware floating point, i.e. if
   * {@link org.jikesrvm.Configuration#BuildForHwFsqrt} is true.
   *
   * @param value the value whose square root will be computed
   * @return the SQRT of the given value
   */
  public static double sqrt(double value) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return -1.0d; // which should upset them even if assertions aren't enabled ...
  }

  /**
   * An illegal instruction. Useful for testing purposes but not needed
   * for running the VM.
   */
  public static void illegalInstruction() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
  }

  //---------------------------------------//
  //    Methods which are evaluated at     //
  //    compile-time when instructions     //
  //    for magic methods are generated.   //
  //---------------------------------------//

  /**
   * How deeply inlined is this method (0 means no inlining).
   * @return depth of inlining
   */
  public static int getInlineDepth() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return 0;
  }

  /**
   * @return the compiler opt level, {@code -1} means baseline
   */
  public static int getCompilerLevel() {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return Integer.MIN_VALUE;
  }

    /**
     * Save processor registers on the stack
     */
    public static void saveContext()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }
  /**
   * Is the specified parameter constant (due to either inlining or specialization).
   * Count starts at zero and includes the 'this' parameter for instance methods.
   *
   * @param index the index for the parameter as described above
   * @return whether the specified parameter is constant
   */
  public static boolean isConstantParameter(int index) {
    if (VM.VerifyAssertions && VM.runningVM) {
      VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
    }
    return false;
  }

    /**
     * Restore processor registers from the stack
     */
    public static void restoreContext()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }
  /**
   * Returns the size of the stack frame for the method.
   * <p>
   * This method forces the opt compiler to allocate a stack frame.
   *
   * @return the size of the stack frame for the method.
   */
  public static int getFrameSize() {
    return -1;
  }

    /**
     * Restore processor registers from the stack
     */
    public static void restoreThreadContextNoErrCode()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }

    /**
     * Restore processor registers from the stack
     */
    public static void restoreThreadContextErrCode()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }

    /**
     * @param stack
     */
    public static void switchStack(Address stack)
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }
    
    /**
     * Set the IDT register
     * @param lidtDescriptor
     */
    public static void setIdt(Address lidtDescriptor)
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }

    /**
     * 
     */
    @Uninterruptible
    public static void yield()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }
    
    public static void startThread(Address ip, Address sp)
    {
        /*
         * Push ip onto the sp; switch to sp; and return
         */
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }
    
    public static void enableInterrupts()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }
    
    public static void disableInterrupts()
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }

    public static void setCS(int segment)
    {
        if (VM.runningVM && VM.VerifyAssertions)
        {
            VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
        }
    }

    /**
     * @param a
     * @return
     */
    public static double dceil(double a)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
      return 0.0;
    }
    
    public static void cpuId(int val, int[] registers)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
    }
    
    public static long readMSR(int msrRegister)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
      return 0;
    }
    
    public static void writeMSR(int msrRegister, long value)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
    }
    
    public static short byteSwap(short value)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
      return 0;
    }
    public static int byteSwap(int value)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
      return 0;
    }
    public static long byteSwap(long value)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
      return 0;
    }
    public static void throwException(Throwable exc)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
    }
    public static void throwExceptionNoErrCode(Throwable exc)
    {
      if (VM.runningVM && VM.VerifyAssertions)
      {
          VM._assert(VM.NOT_REACHED);  // call site should have been hijacked by magic in compiler
      }
    }
}
