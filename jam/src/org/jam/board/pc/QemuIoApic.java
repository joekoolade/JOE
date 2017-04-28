/**
 * Created on Apr 26, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

/**
 * @author Joe Kulig
 * 
 * The class will route the IRQ in the below manner
 * 
 * +-----+---------------+-----------------------+
 * | IRQ | IO-APIC Pin   | Interrupt Destination |
 * +-----+---------------+-----------------------+
 * | 0   | 2             | 0x5f                  |
 * +-----+---------------+-----------------------+
 * | 1   | 1             | disabled              |
 * +-----+---------------+-----------------------+
 * | 2   | -             | disabled              |
 * +-----+---------------+-----------------------+
 * | 3   | 3             | 0x58                  |
 * +-----+---------------+-----------------------+
 * | 4   | 4             | 0x57                  |
 * +-----+---------------+-----------------------+
 * | 5   | 5             | disabled              |
 * +-----+---------------+-----------------------+
 * | 6   | 6             | disabled              |
 * +-----+---------------+-----------------------+
 * | 7   | 7             | disabled              |
 * +-----+---------------+-----------------------+
 * | 8   | 8             | disabled              |
 * +-----+---------------+-----------------------+
 * | 9   | 9             | disabled              |
 * +-----+---------------+-----------------------+
 * | 10  | 10            | disabled              |
 * +-----+---------------+-----------------------+
 * | 12  | 12            | disabled              |
 * +-----+---------------+-----------------------+
 * | 13  | 13            | disabled              |
 * +-----+---------------+-----------------------+
 * | 14  | 14            | disabled              |
 * +-----+---------------+-----------------------+
 * | 15  | 15            | disabled              |
 * +-----+---------------+-----------------------+
 */

public class QemuIoApic extends IOApic {

  public QemuIoApic()
  {
    
  }

  /* (non-Javadoc)
   * @see org.jam.board.pc.IOApic#boot()
   */
  @Override
  protected void boot()
  {
    // PIT interrupt
    setLogicalDestination(2, 0xFF);
    setInterruptVector(2, 0x5F);
    // COM2
//    setLogicalDestination(3, 0xFF);
//    setInterruptVector(3, 0x58);
    // COM1
    setLogicalDestination(3, 0xFF);
    setLogicalDestination(4, 0x57);
  }
}
