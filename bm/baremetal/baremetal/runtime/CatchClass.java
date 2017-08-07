/*
 * Created on Jul 2, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.runtime;

import baremetal.kernel.Memory;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final class CatchClass {
  final static int ADDRESS = 0;
  final static int NAME = 4;
  
  final static java.lang.String getClassName(int r) {
    return Utf8.toString(Memory.read32(r+NAME));
  }
  
  final static int getAddress(int r) {
    return Memory.read32(r);
  }
  
  final static void setClassName(int r, int name) {
    Memory.write32(r+NAME, name);
  }
  
  final static void setAddress(int r, int value) {
    Memory.write32(r, value);
  }
}
