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
  
  public static final int FLAG_NO_INTERRUPT = 0x01;
  private static final int ID_OFFSET   = 4;
  private static final int LEN_OFFSET  = 8;
  private static final int FLAG_OFFSET = 0;
  private static final int IDX_OFFSET  = 2;
  
  public VirtUsed(Address ring, int size)
  {
    this.ring = ring;
    this.size = size;
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
  
}
