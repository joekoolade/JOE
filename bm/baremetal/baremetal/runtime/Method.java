/*
 * Created on May 21, 2004
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
public final class Method {
  private final static int NAME=0;
  private final static int SIGNATURE=4;
  private final static int ACCESS_FLAGS=8;
  private final static int VTABLE_INDEX=10;
  private final static int CODE=12;
  private final static int THROWS=16;
  public final static int SIZEOF_METHOD=20;
  
  final static java.lang.String getName(int m) {
    // get the utf address
    m = Memory.readWord(m);
    // get the string
    return Utf8.toString(Memory.readWord(m));
  }
  
  final static java.lang.String getSignature(int m) {
    // get the utf address
    m = Memory.readWord(m+SIGNATURE);
    // get the string
    return Utf8.toString(Memory.readWord(m));
  }
  
  final static int getAccessFlags(int m) {
    return Memory.readHalfWord(m+ACCESS_FLAGS);
  }

  final static int getIndex(int m) {
    return Memory.readHalfWord(m+VTABLE_INDEX);
  }

  final static int getCode(int m) {
    return Memory.readWord(m+CODE);
  }
  
  final static int getThrows(int m) {
    return Memory.readWord(m+THROWS);
  }
  

}
