/**
 * Created on Apr 30, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class IMCR {
  private static Address port22 = Address.fromIntSignExtend(0x22);
  private static Address port23 = Address.fromIntSignExtend(0x23);
  
  /**
   * Routes PIC IRQ signals to the IO APIC
   */
  public static void enableIRQS()
  {
    port22.ioStore(0x70);
    port23.ioStore(0x01);
  }
  
  public static void disableIRQS()
  {
    port22.ioStore(0x70);
    port23.ioStore(0x00);
  }
}
