/**
 * Created on Mar 15, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciDevice;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
abstract public class DeviceCfg extends VirtioPciCap {
  final Address deviceCfg;
  
  /**
   * @param device
   * @param capPointer
   * @param capInfo
   */
  public DeviceCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
    deviceCfg = device.getBar(bar).plus(virtioCapOffset);
  }

  public byte getByte(int offset)
  {
    return deviceCfg.loadByte(Offset.fromIntSignExtend(offset));
  }
  
  public short getShort(int offset)
  {
    return deviceCfg.loadShort(Offset.fromIntSignExtend(offset));
  }
  
  public int getInt(int offset)
  {
    return deviceCfg.loadInt(Offset.fromIntSignExtend(offset));
  }
}
