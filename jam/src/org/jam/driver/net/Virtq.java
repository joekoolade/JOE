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
  
  public Virtq(int size)
  {
    int ALIGNMENT = (16+4+2);
    int space = (26 * size) + 12 + ALIGNMENT;
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
    align = (align + 9 + (size * 2)) & ~0x4; 
    virtUsed = Address.fromIntZeroExtend(align);
    usedTable = new VirtUsed(virtUsed, size);
  }
}
