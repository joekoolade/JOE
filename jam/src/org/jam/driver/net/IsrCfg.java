/**
 * Created on Mar 15, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciDevice;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class IsrCfg extends VirtioPciCap {
  final Address isr;
  final private static int QUEUE_INTERRUPT = 0x01;
  final private static int DEV_CONFIG_INTERRUPT = 0x02;
  
  /**
   * @param device
   * @param capPointer
   * @param capInfo
   */
  public IsrCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
    isr = Address.fromIntSignExtend(device.getBar(bar) + offset);
  }

  public int getStatus()
  {
    return isr.loadInt();
  }
  
  static public boolean isQueueInterrupt(int status)
  {
    return (status & QUEUE_INTERRUPT) != 0;
  }
  
  static public boolean isDeviceConfigurationInterrupt(int status)
  {
    return (status & DEV_CONFIG_INTERRUPT) != 0;
  }
}
