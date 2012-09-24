//===----------------------- TraceInterface.java --------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//



package org.j3.mmtk;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

/**
 * Class that supports scanning Objects or Arrays for references
 * during tracing, handling those references, and computing death times
 */
@Uninterruptible
public class TraceInterface extends org.mmtk.vm.TraceInterface {


  /***********************************************************************
   *
   * Public Methods
   */

  /**
   * Returns if the VM is ready for a garbage collection.
   *
   * @return True if the VM is ready for GC, false otherwise.
   */
  public native boolean gcEnabled();

  /**
   * This adjusts the offset into an object to reflect what it would look like
   * if the fields were laid out in memory space immediately after the object
   * pointer.
   *
   * @param isScalar If this is a pointer store to a scalar object
   * @param src The address of the source object
   * @param slot The address within <code>src</code> into which
   * the update will be stored
   * @return The easy to understand offset of the slot
   */
  public native Offset adjustSlotOffset(boolean isScalar,
                                        ObjectReference src,
                                        Address slot);

  /**
   * This skips over the frames added by the tracing algorithm, outputs
   * information identifying the method the containts the "new" call triggering
   * the allocation, and returns the address of the first non-trace, non-alloc
   * stack frame.
   *
   *@param typeRef The type reference (tib) of the object just allocated
   * @return The frame pointer address for the method that allocated the object
   */
  @Interruptible
  public native Address skipOwnFramesAndDump(ObjectReference typeRef);

  /***********************************************************************
   *
   * Wrapper methods
   */
  public native void updateDeathTime(ObjectReference obj);
  public native void setDeathTime(ObjectReference ref, Word time_);
  public native void setLink(ObjectReference ref, ObjectReference link);
  public native void updateTime(Word time_);
  public native Word getOID(ObjectReference ref);
  public native Word getDeathTime(ObjectReference ref);
  public native ObjectReference getLink(ObjectReference ref);
  public native Address getBootImageLink();
  public native Word getOID();
  public native void setOID(Word oid);
  public native int getHeaderSize();
  public native int getHeaderEndOffset();
}
