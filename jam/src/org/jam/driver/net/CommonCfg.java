/**
 * Created on Mar 14, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.PciDevice;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class CommonCfg extends VirtioPciCap {
  private static final Offset DEVICE_FEATURE_OFFSET        = Offset.fromIntSignExtend(4);
  private static final Offset DRIVER_FEATURE_SELECT_OFFSET = Offset.fromIntSignExtend(8);
  private static final Offset DRIVER_FEATURE_OFFSET        = Offset.fromIntSignExtend(0x0C);
  private static final Offset MSIX_CONFIG_OFFSET           = Offset.fromIntSignExtend(0x10);
  private static final Offset NUM_QUEUES_OFFSET            = Offset.fromIntSignExtend(0x12);
  private static final Offset DEVICE_STATUS_OFFSET         = Offset.fromIntSignExtend(0x14);
  private static final Offset CONFIG_GENERATION_OFFSET     = Offset.fromIntSignExtend(0x15);
  private static final Offset QUEUE_SELECT_OFFSET          = Offset.fromIntSignExtend(0x16);
  private static final Offset QUEUE_SIZE_OFFSET            = Offset.fromIntSignExtend(0x18);
  private static final Offset QUEUE_MSIX_VECTOR_OFFSET     = Offset.fromIntSignExtend(0x1A);
  private static final Offset QUEUE_ENABLE_OFFSET          = Offset.fromIntSignExtend(0x1C);
  private static final Offset QUEUE_NOTIFY_OFFSET          = Offset.fromIntSignExtend(0x1E);
  private static final Offset QUEUE_DESC_OFFSET            = Offset.fromIntSignExtend(0x20);
  private static final Offset QUEUE_AVAIL_OFFSET           = Offset.fromIntSignExtend(0x28);
  private static final Offset QUEUE_USED_OFFSET            = Offset.fromIntSignExtend(0x30);

  /*
   * Deivce staus fields
   */
  private static final byte   ACKNOWLEDGE                  = 0x01;
  private static final byte   DRIVER                       = 0x02;
  private static final byte   DRIVER_OK                    = 0x04;
  private static final byte   FEATURES_OK                  = 0x08;
  private static final byte   DEVICE_NEEDS_RESET           = 0x40;
  private static final byte   FAILED                       = (byte) 0x80;
  
  final PciDevice pci;
  final Address commonCfg;
  /**
   * @param device
   * @param capPointer
   * @param capInfo 
   */
  public CommonCfg(PciDevice device, int capPointer, int capInfo)
  {
    super(device, capPointer);
    pci = device;
    commonCfg = Address.fromIntZeroExtend((pci.getBar(bar) & 0xFFFFFFF0)+offset);
    VM.sysWriteln("commoncfg: ", commonCfg);
  }
  
  public int getDeviceFeatureSelect()
  {
    return commonCfg.loadInt();
  }
  
  public void setDeviceFeatureSelect(int val)
  {
    commonCfg.store(val);
  }
  
  public int getDeviceFeature()
  {
    return commonCfg.loadInt(DEVICE_FEATURE_OFFSET);
  }
  
  public int getDriverFeatureSelect()
  {
    return commonCfg.loadInt(DRIVER_FEATURE_SELECT_OFFSET);
  }
  
  public void setDriverFeatureSelect(int val)
  {
    commonCfg.store(val, DRIVER_FEATURE_SELECT_OFFSET);
  }
  
  public int getDriverFeature()
  {
    return commonCfg.loadInt(DRIVER_FEATURE_OFFSET);
  }
  
  public void setDriverFeature(int value)
  {
    commonCfg.store(value, DRIVER_FEATURE_OFFSET);
  }
  
  public short getMsiXconfig()
  {
    return commonCfg.loadShort(MSIX_CONFIG_OFFSET);
  }
  
  public void setMsiXconfig(short value)
  {
    commonCfg.store(value, MSIX_CONFIG_OFFSET);
  }
  
  public short getNumQueues()
  {
    return commonCfg.loadShort(NUM_QUEUES_OFFSET);
  }
  
  public byte getDeviceStatus()
  {
    return commonCfg.loadByte(DEVICE_STATUS_OFFSET);
  }
  
  public void setDeviceStatus(byte value)
  {
    commonCfg.store(value, DEVICE_STATUS_OFFSET);
  }
  
  public byte getConfigGeneration()
  {
    return commonCfg.loadByte(CONFIG_GENERATION_OFFSET);
  }

  public void setQueueSelect(int queueIndex)
  {
    commonCfg.store(queueIndex, QUEUE_SELECT_OFFSET);
  }
  
  public int getQueueSelect()
  {
    int queueIndex = commonCfg.loadShort(QUEUE_SELECT_OFFSET);
    return queueIndex & 0xFFFF;
  }
  
  public int getQueueSize()
  {
    int size = commonCfg.loadShort(QUEUE_SIZE_OFFSET);
    return size & 0xFFFF;
  }
  
  public void resetDevice()
  {
    setDeviceStatus((byte) 0);
  }
  
  /*
   * Tell device you notice it
   */
  public void acknowledge()
  {
    byte value = ACKNOWLEDGE;
    setDeviceStatus(value);
  }
  
  /*
   * Tell device you can drive it
   */
  public void driver()
  {
    byte status = getDeviceStatus();
    status |= DRIVER;
    setDeviceStatus(status);
  }
  
  public void featuresOK()
  {
    byte status = getDeviceStatus();
    status |= FEATURES_OK;
    setDeviceStatus(status);
  }
  
  public boolean areFeaturesOk()
  {
    return (getDeviceStatus() & FEATURES_OK) != 0;
  }
}
