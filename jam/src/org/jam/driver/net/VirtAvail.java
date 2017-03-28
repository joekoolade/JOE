/**
 * Created on Mar 22, 2017
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
public class VirtAvail {
  final Address ring;
  final int size;
  
  public static final int FLAG_NO_INTERRUPT = 0x01;
  private static final int RING_OFFSET = 4;
  private static final int FLAG_OFFSET = 0;
  private static final int IDX_OFFSET  = 2;
  
  /**
   * @param virtAvail
   * @param size
   */
  public VirtAvail(Address table, int size)
  {
    ring = table;
    this.size = size;
  }

  public void setRing(int index, short value)
  {
    ring.store(value, Offset.fromIntZeroExtend(index * 2 + RING_OFFSET));
  }
  
  public int getRing(int index)
  {
    int value = ring.loadShort(Offset.fromIntZeroExtend(RING_OFFSET + (index * 2)));
    return value & 0xFFFF;
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
  
  public int getUsedEvent()
  {
    int usedEvent = 0;
    return usedEvent;
  }
  
  public void setUsedEvent(int event)
  {
//    usedEvent = event;
  }
}
