/**
 * Created on Mar 13, 2017
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
public class NotifyCfg extends VirtioPciCap {
  int offsetMultiplier;
  /**
   * 
   * @param device pci device
   * @param capPointer pointer to capability
   * @param capInfo first word of capability info
   */
  NotifyCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
    offsetMultiplier = device.readConfig32(capPointer+16);
  }
}
