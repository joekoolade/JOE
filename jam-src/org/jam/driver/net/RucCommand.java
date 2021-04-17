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
public enum RucCommand {
  NOP(0),
  START(1),
  RESUME(2),
  RCV_DMA_REDIRECT(3),
  ABORT(4),
  LOAD_HDS(5),
  LOAD_BASE(6);
  
  final private byte command;
  
  private RucCommand(int value)
  {
    command = (byte)value;
  }
  
  public byte register()
  {
    return command;
  }
}
