/**
 * Created on Mar 22, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class VirtAvail {
  final Address ring;
  final int size;
  private int shadowIdx;
  
  public static final short FLAG_NO_INTERRUPT = 0x01;
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
    shadowIdx = 0;
  }

  public void setAvailable(short descTableBuffer)
  {
    ring.store(descTableBuffer, Offset.fromIntZeroExtend(shadowIdx * 2 + RING_OFFSET));
    shadowIdx++;
    shadowIdx &= (size-1);
    setIdx((short)shadowIdx);
  }
  
  public int getFreeBuffer()
  {
    return shadowIdx;
  }
  /**
   * Makes buffers from bufferStart to bufferEnd inclusively available
   * 
   * @param bufferStart
   * @param bufferEnd
   */
  public void setAvailable(short bufferStart, short bufferEnd)
  {
    for(int buffer=bufferStart; buffer <= bufferEnd; buffer++)
    {
      ring.store(buffer, Offset.fromIntZeroExtend(shadowIdx * 2 + RING_OFFSET));
      shadowIdx++;
      shadowIdx &= (size-1);
    }
    setIdx((short)shadowIdx);
    Magic.fence();
  }
  
  public int getAvail(int index)
  {
    int value = ring.loadShort(Offset.fromIntZeroExtend(RING_OFFSET + (index * 2)));
    return value & 0xFFFF;
  }
  
  public void noInterrupts()
  {
    setFlags(FLAG_NO_INTERRUPT);
  }
  
  public void setFlags(short flags)
  {
    ring.store(flags);
  }
  
  
  public short getFlags()
  {
    return ring.loadShort();
  }
  
  public short getIdx()
  {
    return ring.loadShort(Offset.fromIntZeroExtend(IDX_OFFSET));
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
