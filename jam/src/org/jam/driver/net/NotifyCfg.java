/**
 * Created on Mar 13, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciDevice;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class NotifyCfg extends VirtioPciCap {
  int offsetMultiplier;
  private Address notifyCfg;
  /**
   * 
   * @param device pci device
   * @param capPointer pointer to capability
   * @param capInfo first word of capability info
   */
  NotifyCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
    offsetMultiplier = device.readConfig32(offset+16);
    VM.sysWriteln("offset multiplier ", VM.intAsHexString(offsetMultiplier));
    notifyCfg = device.getBar(bar).plus(virtioCapOffset);
  }
  
  public void notify(int notifyOffset, short queueIndex)
  {
    notifyCfg.plus(notifyOffset * offsetMultiplier).store(queueIndex);
  }
}
