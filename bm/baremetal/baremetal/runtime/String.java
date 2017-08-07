/*
 * Created on May 13, 2004
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
public class String {

  public final static int length(java.lang.String s) {
    int s0 = Memory.getAddress(s);
    return Utf8.getLength(s0);
  }

  /**
   * @param string
   * @return
   */
  public static char[] toCharArray(java.lang.String s) {
    int addr = Memory.getAddress(s);
    int count = Utf8.getLength(addr);
    char[] copy=new char[count];
    for(int i=0; i<count; i++)
      copy[i] = (char)Utf8.getChar(addr, i);

    return copy;
  }

  public static char[] toCharArray(int addr) {
    int count = Utf8.getLength(addr);
    char[] copy=new char[count];
    for(int i=0; i<count; i++)
      copy[i] = (char)Utf8.getChar(addr, i);

    return copy;
  }
  /**
   * @param string
   * @param srcBegin
   * @param srcEnd
   * @param dst
   * @param dstBegin
   * @return
   */
  public static void getBytes(java.lang.String string, int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
    // TODO Auto-generated method stub
  }

}
