/*
 * Created on May 21, 2004
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
public final class VTable {
  private final static int CLASS=0;
  private final static int GC_DESC=4;
  private final static int METHODS=8;
  private final static int SIZEOF_METHODS=4;
  private final static int SIZE=8;
  
  final static int allocate(int methodCnt) {
    return Heap.allocate((methodCnt*SIZEOF_METHODS) + SIZE);
  }
  
  final static int getMethod(int vtable, int index) {
    return Memory.readWord(vtable+METHODS+index*SIZEOF_METHODS);
  }
  
  final static void setMethod(int vtable, int index, int method) {
    Memory.writeWord(vtable+METHODS+index*SIZEOF_METHODS, method);
  }
  
  final static int getClass(int vtable) {
    return Memory.readWord(vtable);
  }
  
  final static int getGCDesc(int vtable) {
    return Memory.readWord(vtable+GC_DESC);
  }

  /**
   * @param vtable
   * @param cl
   */
  final static void setClass(int vtable, int cl) {
    Memory.writeWord(vtable, cl);
  }
  
  final static void setGCDesc(int vtable, int desc) {
    Memory.writeWord(vtable+GC_DESC, desc);
  }
}
