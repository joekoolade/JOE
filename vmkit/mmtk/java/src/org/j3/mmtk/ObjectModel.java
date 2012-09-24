//===------------------------ ObjectModel.java ----------------------------===//
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

@Uninterruptible
public final class ObjectModel extends org.mmtk.vm.ObjectModel {

  protected native Offset getArrayBaseOffset();

  /**
   * Copy an object using a plan's allocCopy to get space and install
   * the forwarding pointer.  On entry, <code>from</code> must have
   * been reserved for copying by the caller.  This method calls the
   * plan's <code>getStatusForCopy()</code> method to establish a new
   * status word for the copied object and <code>postCopy()</code> to
   * allow the plan to perform any post copy actions.
   *
   * @param from the address of the object to be copied
   * @return the address of the new object
   */
  @Inline
  public native ObjectReference copy(ObjectReference from, int allocator);

  /**
   * Copy an object to be pointer to by the to address. This is required
   * for delayed-copy collectors such as compacting collectors. During the
   * collection, MMTk reserves a region in the heap for an object as per
   * requirements found from ObjectModel and then asks ObjectModel to
   * determine what the object's reference will be post-copy.
   *
   * @param from the address of the object to be copied
   * @param to The target location.
   * @param region The start (or an address less than) the region that was reserved for this object.
   * @return Address The address past the end of the copied object
   */
  @Inline
  public native Address copyTo(ObjectReference from, ObjectReference to, Address region);

  /**
   * Return the reference that an object will be refered to after it is copied
   * to the specified region. Used in delayed-copy collectors such as compacting
   * collectors.
   *
   * @param from The object to be copied.
   * @param to The region to be copied to.
   * @return The resulting reference.
   */
  public native ObjectReference getReferenceWhenCopiedTo(ObjectReference from, Address to);

  /**
   * Gets a pointer to the address just past the end of the object.
   *
   * @param object The object.
   */
  public native Address getObjectEndAddress(ObjectReference object);

  /**
   * Return the size required to copy an object
   *
   * @param object The object whose size is to be queried
   * @return The size required to copy <code>obj</code>
   */
  public native int getSizeWhenCopied(ObjectReference object);

  /**
   * Return the alignment requirement for a copy of this object
   *
   * @param object The object whose size is to be queried
   * @return The alignment required for a copy of <code>obj</code>
   */
  public native int getAlignWhenCopied(ObjectReference object);

  /**
   * Return the alignment offset requirements for a copy of this object
   *
   * @param object The object whose size is to be queried
   * @return The alignment offset required for a copy of <code>obj</code>
   */
  public native int getAlignOffsetWhenCopied(ObjectReference object);

  /**
   * Return the size used by an object
   *
   * @param object The object whose size is to be queried
   * @return The size of <code>obj</code>
   */
  public native int getCurrentSize(ObjectReference object);

  /**
   * Return the next object in the heap under contiguous allocation
   */
  public native ObjectReference getNextObject(ObjectReference object);

  /**
   * Return an object reference from knowledge of the low order word
   */
  public native ObjectReference getObjectFromStartAddress(Address start);

  /**
   * Get the type descriptor for an object.
   *
   * @param ref address of the object
   * @return byte array with the type descriptor
   */
  public native byte [] getTypeDescriptor(ObjectReference ref);

  @Inline
  public native int getArrayLength(ObjectReference object);

  /**
   * Is the passed object an array?
   *
   * @param object address of the object
   */
  public native boolean isArray(ObjectReference object);

  /**
   * Is the passed object a primitive array?
   *
   * @param object address of the object
   */
  public native boolean isPrimitiveArray(ObjectReference object);

  /**
   * Attempts to set the bits available for memory manager use in an
   * object.  The attempt will only be successful if the current value
   * of the bits matches <code>oldVal</code>.  The comparison with the
   * current value and setting are atomic with respect to other
   * allocators.
   *
   * @param object the address of the object
   * @param oldVal the required current value of the bits
   * @param newVal the desired new value of the bits
   * @return <code>true</code> if the bits were set,
   * <code>false</code> otherwise
   */
  public native boolean attemptAvailableBits(ObjectReference object,
                                             Word oldVal, Word newVal);

  /**
   * Gets the value of bits available for memory manager use in an
   * object, in preparation for setting those bits.
   *
   * @param object the address of the object
   * @return the value of the bits
   */
  public native Word prepareAvailableBits(ObjectReference object);

  /**
   * Sets the byte available for memory manager use in an object.
   *
   * @param object the address of the object
   * @param val the new value of the byte
   */
  public native void writeAvailableByte(ObjectReference object, byte val);

  /**
   * Read the byte available for memory manager use in an object.
   *
   * @param object the address of the object
   * @return the value of the byte
   */
  public native byte readAvailableByte(ObjectReference object);

  /**
   * Sets the bits available for memory manager use in an object.
   *
   * @param object the address of the object
   * @param val the new value of the bits
   */
  public native void writeAvailableBitsWord(ObjectReference object, Word val);

  /**
   * Read the bits available for memory manager use in an object.
   *
   * @param object the address of the object
   * @return the value of the bits
   */
  public native Word readAvailableBitsWord(ObjectReference object);

  /**
   * Gets the offset of the memory management header from the object
   * reference address.  XXX The object model / memory manager
   * interface should be improved so that the memory manager does not
   * need to know this.
   *
   * @return the offset, relative the object reference address
   */
  /* AJG: Should this be a variable rather than method? */
  public native Offset GC_HEADER_OFFSET();

  /**
   * Returns the lowest address of the storage associated with an object.
   *
   * @param object the reference address of the object
   * @return the lowest address of the object
   */
  @Inline
  public native Address objectStartRef(ObjectReference object);

  /**
   * Returns an address guaranteed to be inside the storage assocatied
   * with and object.
   *
   * @param object the reference address of the object
   * @return an address inside the object
   */
  public native Address refToAddress(ObjectReference object);

  /**
   * Checks if a reference of the given type in another object is
   * inherently acyclic.
   *
   * @return <code>true</code> if a reference of the type is
   * inherently acyclic
   */
  @Inline
  public native boolean isAcyclic(ObjectReference typeRef);

  /**
   * Dump debugging information for an object.
   *
   * @param object The object whose information is to be dumped
   */
  public native void dumpObject(ObjectReference object);
}

