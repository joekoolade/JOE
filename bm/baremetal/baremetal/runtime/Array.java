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
 * Methods for manipulating arrays
 * 
 */
public final class Array {
  public final static void set32(int table, int pos, int value) {
    Memory.writeWord(table+(pos*4), value);
  }
  
  public final static void set16(int table, int pos, int value) {
    Memory.writeHalfWord(table+(pos*2), value);
  }
  
  public final static void set8(int table, int pos, int value) {
    Memory.writeByte(table+pos, value);
  }
  
  public final static int get32(int table, int pos) {
    return Memory.readWord(table+(pos*4));
  }
  
  public final static int get16(int table, int pos) {
    return Memory.readHalfWord(table+(pos*2));
  }

  public final static int get8(int table, int pos) {
    return Memory.readByte(table+pos);
  }

}
