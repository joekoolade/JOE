/**
 * Created on Mar 26, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class VirtUsed {
  final Address ring;
  final int size;
  private int lastUsedIndex;    // The next buffer that driver expects to see data in
  
  public static final short FLAG_NO_INTERRUPT = 0x01;
  private static final int ID_OFFSET   = 4;
  private static final int LEN_OFFSET  = 8;
  private static final int FLAG_OFFSET = 0;
  private static final int IDX_OFFSET  = 2;
  
  public VirtUsed(Address ring, int size)
  {
    this.ring = ring;
    this.size = size;
    lastUsedIndex = 0;
  }

  public int getNextBufferDescriptor()
  {
    return getId(lastUsedIndex);
  }
  
  public int getNextBufferLen()
  {
    return getLen(lastUsedIndex);
  }
  
  public final void next()
  {
    // Advance the lastUsedIndex to the next entry
    lastUsedIndex++;
    lastUsedIndex &= (size-1);
  }
  
  public int getId(int index)
  {
    return ring.loadInt(Offset.fromIntZeroExtend(index*8 + ID_OFFSET));
  }
  
  public void setId(int index, int id)
  {
    ring.store(id, Offset.fromIntZeroExtend(index*8 + ID_OFFSET));
  }

  public int getLen(int index)
  {
    return ring.loadInt(Offset.fromIntZeroExtend(index*8 + LEN_OFFSET));
  }
  
  public void setLen(int index, int id)
  {
    ring.store(id, Offset.fromIntZeroExtend(index*8 + LEN_OFFSET));
  }
  
  public void setFlags(short flags)
  {
    ring.store(flags);
  }
  
  public void noInterrupts()
  {
    setFlags(FLAG_NO_INTERRUPT);
  }
  public int getFlags()
  {
    int flags = ring.loadShort();
    return flags & 0xFFFF;
  }
  
  public int getIdx()
  {
    int idx = ring.loadShort(Offset.fromIntZeroExtend(IDX_OFFSET));
    return idx & 0xFFFF;
  }
  
  public void setIdx(short idx)
  {
    ring.store(idx, Offset.fromIntZeroExtend(IDX_OFFSET));
  }

  /**
   * @return
   */
  public boolean hasBuffer()
  {
    return lastUsedIndex != getIdx();
  }
  
  public boolean hasNoBuffers()
  {
    return lastUsedIndex == getIdx();
  }
}
