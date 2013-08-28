/*
 * Created on May 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package baremetal.runtime;

import baremetal.kernel.Heap;
import baremetal.kernel.Memory;

/**
 * @author joe
 * 
 * Access and manipulate the GCJ Utf8Const structure
 */
public class Utf8 {
  /*
   * a utf string layout is: hash length data
   * 
   * length does not include the null terminator data consists of characters
   * ending with '\0'
   */
  private final static int HASH = 0;
  private final static int LENGTH = 2;
  private final static int DATA = 4;

  public static int utfStart;
  public static int utfEnd;

  public final static int getLength(int utf) {
    return Memory.read16(utf + LENGTH);
  }

  public final static int getHash(int utf) {
    return Memory.read16(utf);
  }

  public final static void setHash(int utf, int hash) {
    Memory.write16(utf, hash);
  }

  public final static void setLength(int utf, int size) {
    Memory.write16(utf + LENGTH, size);
  }

  /*
   * returns address of first character
   */
  public final static int getData(int utf) {
    return utf + DATA;
  }

  /*
   * Get an 8 bit char. Assumes that utf points to ascii data/characters
   */
  public final static int getChar(int utf, int index) {
    return Memory.read8(utf + DATA + index);
  }

  public final static int getUtf8(int utf, int index) {
    return 0;
  }

  /**
   * Deter
   */
  public final static boolean isUtf(java.lang.String str) {
    int addr = Memory.getAddress(str);
    return (addr >= utfStart) && (addr < utfEnd);
  }

  public final static int makeString(char[] str, int size) {
    int utf = Heap.allocate(4 + size);
    int hash = 0;
    for (int i = 0; i < size; i++) {
      Memory.writeByte(utf + DATA + i, str[i]);
      hash = (hash * 31) + str[i];
    }
    setHash(utf, hash & 0xffff);
    setLength(utf, size);
    return utf;

  }

  public final static java.lang.String toString(Object str) {
    return toString(Memory.getAddress(str));
  }

  public final native static java.lang.String toString(int str);

  /**
   * @param anObject
   * @return
   */
  public final static boolean isUtf(Object anObject) {
    int addr = Memory.getAddress(anObject);
    return (addr >= utfStart) && (addr < utfEnd);
  }

  public final static void convertUtfs() {
    int utf0 = 0;
    int negative = 0;
    for (int utf = utfStart; utf < utfEnd;) {
      java.lang.String name = new java.lang.String(utf);
      int len = getLength(utf);
      if(len<0 || len>15000)
        utf = 1;
      Memory.write32(utf, Memory.getAddress(name));
      utf += DATA + len;
      /*
       * Another problem presents itself. BM rewrites baremetal.vm.VMLongArray
       * to [J. Need to advance to the next utf. So search for the null, \0!
       */
      if (Memory.read8(utf) != 0) {
        utf++;
        while (Memory.read8(utf) != 0)
          utf++;
        if ((utf & 1) != 0)
          utf++;
        else
          utf += 2;
      } else {
        if ((len & 1) != 0)
          utf += 1;
        else
          utf += 2;
      }
    }
    /*
     * HACK:
     * 
     * After erodata is the following:
     *  0018dfc2 A _erodata
        0018dfe0 R _fp_hw
        0018dfe4 R _IO_stdin_used
        0018e5a4 r _Utf4163
        0018e876 r _Utf4165
        0018eb48 r _Utf4166
        0018ecf8 r _Utf4168
        0018eea6 r _Utf1809
        0018f134 r _Utf1810
        0018f428 r _Utf1811
        0018f6d0 r _Utf1812
        0018f918 r _Utf1813
        001910d2 r _Utf1814

     * We want to pick up the last 10 utfs.
     */
    utf0 = ((utfEnd+32) & ~15) + 1476;
    // utf4163
    java.lang.String name = new java.lang.String(utf0);
    int len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    
    // utf4165
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf4166
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf4168
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf1809
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf1810
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf1811
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf1812
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf1813
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
    utf0 += DATA + len;
    if ((len & 1) != 0)
      utf0 += 1;
    else
      utf0 += 2;
    // utf1814
    name = new java.lang.String(utf0);
    len = getLength(utf0);
    Memory.write32(utf0, Memory.getAddress(name));
  }
}
