/**
 * Created on Aug 29, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

/**
 * @author Joe Kulig
 *
 */
public enum E100PortCmd {
  SOFTWARE_RESET(0),
  SELF_TEST(1),
  SELECTIVE_RESET(2),
  DUMP(3),
  DUMP_WAKEUP(7);
  
  final private int value;

  /**
   * 
   */
  private E100PortCmd(int value)
  {
    this.value = value;
  }
  
  public int value()
  {
    return value;
  }
}
