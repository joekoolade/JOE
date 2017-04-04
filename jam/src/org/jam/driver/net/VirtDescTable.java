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
public class VirtDescTable {
  final private Address table;
  final private int size;
  final private int numFree;
  
  static final private Offset ADDR  = Offset.fromIntZeroExtend(0);
  static final private Offset LEN   = Offset.fromIntZeroExtend(8);
  static final private Offset FLAGS = Offset.fromIntZeroExtend(12);
  static final private Offset NEXT  = Offset.fromIntZeroExtend(14);
  
  static final public int    FLAG_NEXT     = 0x01;
  static final public int    FLAG_WRITE    = 0x02;
  static final public int    FLAG_INDIRECT = 0x04;

  static final private int ENTRY_SIZE = 16;

  /**
   * @param virtDescTable
   * @param size
   */
  public VirtDescTable(Address table, int size)
  {
    this.size = size;
    this.table = table;
    numFree = size;
  }

  public int getSize()
  {
    return size;
  }
  
  public Address getTableAddress()
  {
    return table;
  }
  /**
   * Set the buffer of a virt desc table entry
   * @param index virt desc entry
   * @param buffer pointer to a buffer
   */
  public void setAddress(int index, Address buffer)
  {
    table.store(buffer, ADDR.plus(index*ENTRY_SIZE));
  }
  
  /**
   * Get buffer address of a virt desc table entry
   * @param index
   * @return
   */
  public Address getAddress(int index)
  {
    return table.loadAddress(ADDR.plus(index*ENTRY_SIZE));
  }
  
  /**
   * Get the length of a virt desc entry
   * @param index
   * @return
   */
  public int getLen(int index)
  {
    return table.loadInt(LEN.plus(index*ENTRY_SIZE));
  }
  
  /**
   * Set the buffer length of a virt desc table entry
   * @param index
   * @param len
   */
  public void setLen(int index, int len)
  {
    table.store(len, LEN.plus(index*ENTRY_SIZE));
  }
  
  public void setFlags(int index, short flags)
  {
    table.store(flags, FLAGS.plus(index*ENTRY_SIZE));
  }
  
  public void makeWriteable(int index)
  {
    int flags = getFlags(index);
    flags |= FLAG_WRITE;
    setFlags(index, (short)flags);
  }
  public int getFlags(int index)
  {
    int flag = table.loadShort(FLAGS.plus(index*ENTRY_SIZE));
    return flag & 0xFFFF;
  }
  
  public void setNext(int index, short next)
  {
    table.store(next, NEXT.plus(index*ENTRY_SIZE));
    int flags = getFlags(index);
    flags |= FLAG_NEXT;
    setFlags(index, (short)flags);
  }
  
  public int getNext(int index)
  {
    int next = table.loadShort(NEXT.plus(index*ENTRY_SIZE));
    return next & 0xFFFF;
  }
  
  public void allocate(int index, int size, boolean write)
  {
    byte buffer[] = new byte[size];
    setAddress(index, Magic.objectAsAddress(buffer));
    setLen(index, buffer.length);
    if(write)
    {
      makeWriteable(index);
    }
  }
}
