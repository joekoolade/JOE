/**
 * Created on Mar 21, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class Virtq {
  final Address virtDescTable;
  final Address virtAvail;
  final Address virtUsed;
  final byte buffer[];
  final VirtDescTable descTable;
  final VirtAvail availTable;
  final VirtUsed usedTable;
  final int size;
  
  private final static int MAX_BUFFER = 1526;
  
  public Virtq(int size)
  {
    int ALIGNMENT = (16+4+2);
    int space = (26 * size) + 12 + ALIGNMENT;
    this.size = size;
    buffer = new byte[space];
    /*
     * Align on a 16 byte boundary
     */
    int align = (Magic.objectAsAddress(buffer).toInt() + 15) & ~0xF;
    virtDescTable = Address.fromIntZeroExtend(align);
    descTable = new VirtDescTable(virtDescTable, size);
    /*
     * Setup virt avail area
     */
    align = align + (size * 16);
    virtAvail = Address.fromIntZeroExtend(align);
    availTable = new VirtAvail(virtAvail, size);
    
    /*
     * Align virt used on a 4 byte boundary
     */
    align = (align + 9 + (size * 2)) & ~0x3; 
    virtUsed = Address.fromIntZeroExtend(align);
    usedTable = new VirtUsed(virtUsed, size);
  }
  
  /**
   * Allocates buffers to the descriptor table.
   */
  public void allocate(boolean writeable)
  {
    int buffer;
    
    for(buffer=0; buffer < size; buffer++)
    {
      descTable.allocate(buffer, MAX_BUFFER, writeable);
    }
  }
  
  /**
   * Add buffers from descritpor table to the available queue
   */
  public void initializeAvailableBuffers()
  {
    availTable.setAvailable((short)0, (short)(size-1));
  }
  
  /*
   * Waits for a buffer to become available
   */
  public byte[] waitForBuffer()
  {
    byte[] buffer=null;
    
    while(usedTable.hasNoBuffers())
    {
      // Just spin until a buffer is received
    }
    int bufferDescriptor = usedTable.getNextBufferDescriptor();
    usedTable.next();
    return descTable.getBuffer(bufferDescriptor);
  }
  
  public String toString()
  {
    return Integer.toHexString(virtDescTable.toInt()) + "/"
    + Integer.toHexString(virtAvail.toInt()) + "/"
    + Integer.toHexString(virtUsed.toInt());
  }
}
