/**
 * Created on Aug 26, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

/**
 * @author Joe Kulig
 *
 */
enum DeliveryMode {
  FIXED(0),
  LOWEST_PRIORITY(1),
  SMI(2),
  NMI(4),
  INIT(5),
  EXT_INT(7);
  
  final private int value;
  
  private DeliveryMode(int value)
  {
    this.value = value;
  }
  
  public int value()
  {
    return value;
  }
}
