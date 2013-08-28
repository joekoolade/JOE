/*
 * Created on Jun 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package baremetal.runtime;

import baremetal.kernel.Memory;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Iface {
  static final int IFACES_SIZE=8;
  private static final int LIST=0;
  private static final int LEN=4;
  private static final int COUNT=6;
  static final int INITIAL_LEN=128;
  
  static final void setCount(int iface, int count) {
    Memory.writeHalfWord(iface + COUNT, count);
  }
  
  static final void setList(int iface, int list) {
    Memory.writeWord(iface + LIST, list);
  }
  
  static final int getLen(int iface) {
    return Memory.readHalfWord(iface + LEN);
  }
  
  static final int getCount(int iface) {
    return Memory.readHalfWord(iface + COUNT);
  }
  
  static final int getList(int iface) {
    return Memory.readWord(iface);
  }
  
  static final void setDefaultLen(int iface) {
    Memory.writeHalfWord(iface + LEN, INITIAL_LEN);
  }

  final static int getIfaceAt(int iface, int index) {
    int list=Memory.readWord(iface);
    return Memory.readWord(list + (index << 2));
  }
}
