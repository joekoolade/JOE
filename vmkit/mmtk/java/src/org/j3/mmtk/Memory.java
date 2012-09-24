//===-------------------------- Memory.java -------------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.mmtk.plan.Plan;
import org.mmtk.policy.ImmortalSpace;
import org.mmtk.utility.Constants;
import org.mmtk.utility.heap.VMRequest;

import org.j3.runtime.VM;
import org.jikesrvm.Magic;
import org.jikesrvm.SizeConstants;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

@Uninterruptible
public final class Memory extends org.mmtk.vm.Memory {

  protected native final Address getHeapStartConstant();
  protected native final Address getHeapEndConstant();
  protected native final Address getAvailableStartConstant();
  protected native final Address getAvailableEndConstant();
  protected final byte getLogBytesInAddressConstant() { return (byte)SizeConstants.LOG_BYTES_IN_ADDRESS; }
  protected final byte getLogBytesInWordConstant() { return (byte)SizeConstants.LOG_BYTES_IN_WORD; }
  protected final byte getLogBytesInPageConstant() { return (byte)SizeConstants.LOG_BYTES_IN_PAGE; }
  protected final byte getLogMinAlignmentConstant() { return (byte)SizeConstants.LOG_BYTES_IN_INT;}
  protected final int getMaxBytesPaddingConstant() { return (byte)SizeConstants.BYTES_IN_DOUBLE; }
  protected final int getAlignmentValueConstant() { return 0xdeadbeef; }

  /* On Intel we align code to 16 bytes as recommended in the optimization manual */
  protected final byte getMaxAlignmentShiftConstant() { 
    return (byte)((VM.BuildForIA32 ? 1 : 0) + SizeConstants.LOG_BYTES_IN_LONG - SizeConstants.LOG_BYTES_IN_INT);
  }

  private static ImmortalSpace bootSpace;

  /**
   * Return the space associated with/reserved for the VM.
   */
  @Interruptible
  public final ImmortalSpace getVMSpace() {
    if (bootSpace == null) {
      bootSpace = new ImmortalSpace("boot", Plan.DEFAULT_POLL_FREQUENCY, VMRequest.create());
    }
    return bootSpace;
  }

  /** Global preparation for a collection. */
  public final void globalPrepareVMSpace() { bootSpace.prepare(); }

  /** Per-collector preparation for a collection. */
  public final void collectorPrepareVMSpace() {}

  /** Per-collector post-collection work. */
  public final void collectorReleaseVMSpace() {}

  /** Global post-collection work. */
  public final void globalReleaseVMSpace() { bootSpace.release(); }

  /**
   * Sets the range of addresses associated with a heap.
   *
   * @param id the heap identifier
   * @param start the address of the start of the heap
   * @param end the address of the end of the heap
   */
  public final void setHeapRange(int id, Address start, Address end) {}

 /**
   * Demand zero mmaps an area of virtual memory.
   *
   * @param start the address of the start of the area to be mapped
   * @param size the size, in bytes, of the area to be mapped
   * @return 0 if successful, otherwise the system errno
   */
  public native final int dzmmap(Address start, int size);

  /**
   * Protects access to an area of virtual memory.
   *
   * @param start the address of the start of the area to be mapped
   * @param size the size, in bytes, of the area to be mapped
   * @return <code>true</code> if successful, otherwise
   * <code>false</code>
   */
  public native final boolean mprotect(Address start, int size);

  /**
   * Allows access to an area of virtual memory.
   *
   * @param start the address of the start of the area to be mapped
   * @param size the size, in bytes, of the area to be mapped
   * @return <code>true</code> if successful, otherwise
   * <code>false</code>
   */
  public native final boolean munprotect(Address start, int size);

  /**
   * Zero a region of memory.
   * @param start Start of address range (inclusive)
   * @param len Length in bytes of range to zero
   * Returned: nothing
   */
  public native final void zero(Address start, Extent len);

  /**
   * Zero a range of pages of memory.
   * @param start Start of address range (must be a page address)
   * @param len Length in bytes of range (must be multiple of page size)
   */
  public native final void zeroPages(Address start, int len);

  /**
   * Logs the contents of an address and the surrounding memory to the
   * error output.
   *
   * @param start the address of the memory to be dumped
   * @param beforeBytes the number of bytes before the address to be
   * included
   * @param afterBytes the number of bytes after the address to be
   * included
   */
  public native final void dumpMemory(Address start, int beforeBytes,
                                      int afterBytes);

  /*
   * Utilities from the VM class
   */

  @Inline
  public final void sync() {
    Magic.sync();
  }

  @Inline
  public final void isync() {
    Magic.isync();
  }
}
