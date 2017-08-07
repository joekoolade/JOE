/*
 * Created on Sep 21, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.dwarf;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ArrayPtr {
  byte[] data;
  int ptr;
  
  public ArrayPtr(byte[] array, int ptr) {
    this.data = array;
    this.ptr = ptr;
  }
}
