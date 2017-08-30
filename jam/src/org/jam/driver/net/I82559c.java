/**
 * Created on Aug 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.board.pc.Pci;
import org.jam.board.pc.PciDevice;
import org.jam.cpu.intel.Tsc;
import org.jam.system.NoDeviceFoundException;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class I82559c {
  final PciDevice pci;
  final Address csr;
  private int eepromSize;
  private short[] eeprom;
  
  private final static Offset SCB_CMD = Offset.fromIntZeroExtend(2);
  private final static Offset SCB_IRQ = Offset.fromIntZeroExtend(3);
  private final static Offset SCB_GENERAL_PTR = Offset.fromIntZeroExtend(4);
  private final static Offset PORT = Offset.fromIntZeroExtend(8);
  private final static Offset EEPROM = Offset.fromIntZeroExtend(14);
  private final static Offset MDI = Offset.fromIntZeroExtend(16);
  private final static Offset RX_DMA_BYTE_COUNT = Offset.fromIntZeroExtend(20);

  // eeprom control signals
  private static final int EESK = 1;
  private static final int EECS = 2;
  private static final int EEDI = 4;
  private static final int EEDO = 8;
  
  // eeprom operations
  private static final int EE_READ_OP = 6;
  
  // number of eeprom address bits
  private int eepromAddrLength;
  
  public I82559c() throws NoDeviceFoundException
  {
    pci = Pci.find((short)0x8086, (short)0x1229);
    if(pci == null)
    {
      throw new NoDeviceFoundException("I82559c");
    }
    VM.sysWriteln(pci.toString());
    VM.sysWrite("status ", Integer.toHexString(pci.getStatus()));
    VM.sysWriteln(" command ", Integer.toHexString(pci.getCommand()));
    csr = pci.getBar(0);
    eeprom = new short[256];
    eepromAddrLength = 8;
    eepromSize = 0;
  }
  
  private void scbIrq(ScbIrqMasks mask)
  {
    csr.store((byte)mask.ordinal(), SCB_IRQ);
  }
  
  
  private void portCmd(E100PortCmd cmd)
  {
    csr.store(cmd.value(), PORT);
  }
  
  private void portCmd(E100PortCmd cmd, Address address)
  {
    
  }
  public void boot()
  {
    hwReset();
    eepromLoad();
    VM.sysWrite("Ethernet Address: ", VM.intAsHexString(eeprom[0]&0xFFFF));
    VM.sysWrite(":", VM.intAsHexString(eeprom[1]&0xFFFF));
    VM.sysWriteln(":", VM.intAsHexString(eeprom[2]&0xFFFF));
    phyInit();
    up();
  }

  /**
   * 
   */
  private void up()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  private void phyInit()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * Write control bits to EEPROM register
   * @param control control signals
   */
  private void eepromRegister(short control)
  {
    csr.store(control, EEPROM);
  }
  
  /**
   * Read EEPROM register
   * @return control signals
   */
  private short eepromRegister()
  {
    return csr.loadShort(EEPROM);
    
  }
  
  private short eepromRead(int address)
  {
    short value=0;
    int cmdAddr;
    int index;
    short eeCtl;
    
    cmdAddr = ((EE_READ_OP << eepromAddrLength) | address) << 16;
//    VM.sysWriteln("cmaddr: ", VM.intAsHexString(cmdAddr));
    eepromRegister((short)(EECS|EESK));
    writeFlush();
    Tsc.udelay(4);
    
    for(index=31; index >= 0; index--)
    {
      eeCtl = (cmdAddr & (1 << index)) != 0 ? (short)(EECS | EEDI) : (short)EECS;
//      VM.sysWriteln("1: ", eeCtl);
      eepromRegister(eeCtl);
      writeFlush();
      Tsc.udelay(4);
      
      eepromRegister((short)(eeCtl | EESK));
      writeFlush();
      Tsc.udelay(4);
      
      eeCtl = eepromRegister();
//      VM.sysWriteln("2: ", eeCtl);
      if((eeCtl & EEDO) == 0 && index > 16 && eepromSize==0)
      {
        eepromAddrLength -= (index-16);
        eepromSize = 1<<eepromAddrLength;
        VM.sysWrite("address len: ", eepromAddrLength); VM.sysWriteln(" ", eepromSize); 
        index=17;
      }
      value = (short)((value << 1) | ((eeCtl & EEDO) != 0 ? 1 : 0));
    }
    
    eepromRegister((short)0);
    writeFlush();
    Tsc.udelay(4);
    return value;
  }
  /**
   * Load eeprom into an array
   */
  private void eepromLoad()
  {
    short checksum = 0;
    // Do initial read to get the size
    eepromRead(0);
    
    /*
     * Store eeprom into the array
     */
    for(int addr=0; addr < eepromSize; addr++)
    {
      eeprom[addr] = eepromRead(addr);
      checksum += eeprom[addr];
    }
    
    if(checksum != (short)0xBABA)
    {
      VM.sysWriteln("E100 eeprom checksum err! expected 0xBABA got 0x", VM.intAsHexString(checksum));
    }
  }

  /**
   * 
   */
  private void hwReset()
  {
   portCmd(E100PortCmd.SELECTIVE_RESET);
   writeFlush();
   Tsc.udelay(20);
   
   portCmd(E100PortCmd.SOFTWARE_RESET);
   writeFlush();
   Tsc.udelay(20);
   
   disableInterrupt();
  }
  
  private void disableInterrupt()
  {
    scbIrq(ScbIrqMasks.MASK_ALL);
    writeFlush();
  }

  /**
   * Flushes previous PCI writes
   */
  private void writeFlush()
  {
    // Just a benign status read
    scbStatus();
  }

  /**
   * Return the SCB status
   */
  private short scbStatus()
  {
    return csr.loadShort();
  }
}
