/*
 * Created on Sep 29, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.runtime;

import baremetal.kernel.Memory;

/**
 * @author Joe Kulig
 * 
 * Stores and manipulates an exception table entry
 *  
 */
public final class ExceptionTable {

  byte startEncoding;
  int start;
  byte ttypeEncoding;
  int ttype;
  byte callSiteEncoding;
  int actionTable;
  int actionTableSize;
  int actionTableEnd;
  int tablePtr;
  int entryPtr;

  // current entry settings
  int throwStartAddr;
  int throwLength;
  int launchPad;
  int action;
  int filter;
  int displacement;
  int catchClass;
  int throwEndAddr; // derived

  int[] buffer = new int[2];
  int[] buffer0 = new int[2];
  
  final byte PE_ABSPTR = 0x0;
  final byte PE_OMIT = (byte) 0xff;
  final byte PE_ULEB128 = 0x1;
  final byte PE_UDATA2 = 0x2;
  final byte PE_UDATA4 = 0x3;
  final byte PE_UDATA8 = 0x4;
  final byte PE_SLEB128 = 0x9;
  final byte PE_SDATA2 = 0xa;
  final byte PE_SDATA4 = 0xb;
  final byte PE_SDATA8 = 0xc;
  final byte PE_SIGNED = 0x8;

  public void load(int table, int functionPtr) {
    tablePtr = table;
    startEncoding = (byte) Memory.readByte(table++);
    //   if(startEncoding==PE_OMIT)
    // assuming that encoding is omitted
    start = functionPtr;
    ttypeEncoding = (byte) Memory.readByte(table++);

    buffer[0] = table;
    ttype = Utils.readUleb128(buffer);
    table = buffer[0];
    ttype += table;
    callSiteEncoding = (byte) Memory.readByte(table++);
    buffer[0] = table;
    actionTableSize = Utils.readUleb128(buffer);
    actionTable = buffer[0];
    entryPtr = buffer[0];
    actionTableEnd = actionTableSize + buffer[0];
  }

  public void iterate() {
    buffer[0] = entryPtr;
  }

  public boolean hasEntry() {
    return buffer[0] < actionTableEnd;
  }

  public boolean canCatch(int addr, Throwable throwObject) {
    if (addr < throwStartAddr || addr > throwEndAddr || filter <= 0)
      return false;
    while(true) {
      if(Class.isInstanceOf(catchClass, throwObject))
        return true;
      /*
       * goto the next catch
       */
      if(displacement==0)
        return false;
      action+=displacement;
      filter = Memory.readByte(actionTableEnd + action);
      displacement = Memory.readByte(actionTableEnd + action + 1);
      if((displacement&0x40)!=0)
        displacement|=0xffffff80;
      catchClass = Memory.read32(Memory.read32(ttype - (filter * 4)));
    }
  }

  /**
   * Loads and entry
   */
  public void nextEntry() {
    throwStartAddr = start + Utils.readUleb128(buffer);
    throwLength = Utils.readUleb128(buffer);
    throwEndAddr = throwStartAddr + throwLength;
    launchPad = start + Utils.readUleb128(buffer);
    action = Utils.readUleb128(buffer);
    if (action != 0) {
      /*
       * todo: may have make these sleb128 calls.
       * For now this is working.
       */
      filter = Memory.readByte(actionTableEnd + action - 1);
      displacement = Memory.readByte(actionTableEnd + action);
      if((displacement&0x40)!=0)
        displacement|=0xffffff80;
      catchClass = Memory.read32(Memory.read32(ttype - (filter * 4)));
    }
  }
}
