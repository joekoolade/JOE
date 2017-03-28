/**
 * Created on Mar 15, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciDevice;

/**
 * @author Joe Kulig
 *
 */
public class PciCfg extends VirtioPciCap {
  int data;
  /**
   * @param device
   * @param capPointer
   * @param capInfo
   */
  public PciCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
    data = device.readConfig32(capPointer+16);
  }

  public String toString()
  {
    return super.toString() + "\ndata " + Integer.toHexString(data);
  }
}
