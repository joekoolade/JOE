/**
 * Created on Sep 2, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

/**
 * @author Joe Kulig
 *
 */
public enum CucCommand {
  NOP(0),
  START(1),
  RESUME(2),
  LOAD_DUMP_ADDRESS(4),
  DUMP_STATS_COUNTERS(5),
  LOAD_BASE(6),
  DUMP_RESET_STAT_COUNTERS(7),
  STATIC_RESUME(10);
  
  private final byte command;
  
  private CucCommand(int value)
  {
    command = (byte)(value<<4);
  }
  
  public byte register()
  {
    return command;
  }
}
