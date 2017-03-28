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
public class DeviceCfg extends VirtioPciCap {

  /**
   * @param device
   * @param capPointer
   * @param capInfo
   */
  public DeviceCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
  }

}
