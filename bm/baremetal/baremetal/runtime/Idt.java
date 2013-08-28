/*
 * Created on Jun 1, 2004
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
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Idt {

  static final int IINDEX=0;
  static final int ITABLE=4;
  static final int IDT_SIZE=8;
  final static int INITIAL_IOFFSETS_LEN=64;
  static final int LEN=2;
  static final int OFFSETS=0;
  
  static int nullIdt;
  
  static {
    nullIdt = Heap.allocate(IDT_SIZE);
    Memory.writeHalfWord(nullIdt+IINDEX, 0xffff);
    Memory.writeHalfWord(nullIdt+LEN, 0);
    Memory.writeWord(nullIdt+ITABLE, 0);
  }
  /**
   * @param idt
   * @param itable
   */
  final static void setItable(int idt, int itable) {
    Memory.writeWord(idt + ITABLE, itable);
  }
  
  final static void setLen(int idt, int size) {
    Memory.writeHalfWord(idt + LEN, size);
  }
  
  final static void setIindex(int idt, int index) {
    Memory.writeHalfWord(idt, index);
  }
  final static int getLen(int idt) {
    return Memory.read16(idt+LEN);
  }
  final static int getIindex(int idt) {
    return Memory.readHalfWord(idt);
  }
  final static int getIoffsets(int idt) {
    return Memory.readWord(idt);
  }
  
  final static void setIoffsets(int idt, int value) {
    Memory.writeWord(idt, value);
  }
  
  final static int getItable(int idt) {
    return Memory.readWord(idt+ITABLE);
  }
  
}
