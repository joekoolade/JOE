/**
 * Created on Mar 30, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class IOApic {
  // Hard coding for now
  Address IOREGSEL = Address.fromIntSignExtend(0xFEC00000);
  Address IOWIN = Address.fromIntSignExtend(0xFEC00010);
  
  final private static int IOAPICID = 0x00;
  final private static int IOAPICVER = 0x01;
  final private static int IOAPICARB = 0x02;
  final private static int IOREDTBL = 0x10;
  
  final private int id;
  final private int version;
  final private int maxRedirEntry;
  
  public IOApic()
  {
    int val = ioApicId();
    id = (val >> 24) & 0xF;
    val = ioApicVer();
    maxRedirEntry = (val >> 16) & 0xFF;
    version = val & 0xFF;
  }
  
  final private void ioRegSel(int register)
  {
    IOREGSEL.store(register);
  }
  
  final private int getIoWin()
  {
    return IOWIN.loadInt();
  }
  
  final private void setIoWin(int value)
  {
    IOWIN.store(value);
  }
  
  final public int ioApicId()
  {
    ioRegSel(IOAPICID);
    return getIoWin();
  }
  
  public final int ioApicVer()
  {
    ioRegSel(IOAPICVER);
    int val = getIoWin();
    return val;
  }
  
  public final void setIoRedTable(int tableEntry, long value)
  {
    int entryValue = (int)(value & 0xFFFFFFFF);
    tableEntry *= 2;
    ioRegSel(IOREDTBL + tableEntry);
    setIoWin(entryValue);
    entryValue = (int)((value >> 32) & 0xFFFFFFFF);
    ioRegSel(IOREDTBL + tableEntry + 1);
    setIoWin(entryValue);
  }
  
  public final long getIoRedTable(int tableEntry)
  {
    long value;
    
    tableEntry *= 2;
    ioRegSel(IOREDTBL + tableEntry);
    value = getIoWin();
    ioRegSel(IOREDTBL + tableEntry + 1);
    long entryValue = getIoWin();
    value |= (entryValue << 32);
    return value;
  }
  
  public String toString()
  {
    String ioApicString = "IOAPIC id " + id + " ver " + version + " max " + maxRedirEntry + "\n";
    
    for(int i=0; i <= maxRedirEntry; i++)
    {
      long entry = getIoRedTable(i);
      ioApicString += i + " " + Long.toHexString(entry) + "\n";
    }
    return ioApicString;
  }
}
