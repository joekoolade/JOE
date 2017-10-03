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
import org.jikesrvm.runtime.Magic;
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
  
  // Configure command parameters
  public static int CFG_BYTE_COUNT               = 0x16;      // byte 0. 22 bytes in the configure command
  // Command bits
  private static final int       CB_EL                        = 1 << 31;
  private static final int       CB_S                         = 1 << 30;
  private static final int       CB_I                         = 1 << 29;
  // status bits
  private static final int       CB_C                         = 1 << 15;
  private static final int       CB_OK                        = 1 << 13;

  // CU commands
  private static final int       CB_NOP                       = 0;
  private static final int       CB_IA_SETUP                  = 1<<16;
  private static final int       CB_CONFIGURE                 = 2<<16;
  private static final int       CB_MC_SETUP                  = 3<<16;
  private static final int       CB_TRANSMIT                  = 4<<16;
  private static final int       CB_LOAD_UCODE                = 5<<16;
  private static final int       CB_DUMP                      = 6<<16;
  private static final int       CB_DIAGNOSE                  = 7<<16;
  
  private final static Offset    SCB_STATUS                   = Offset.fromIntZeroExtend(1);
  private final static Offset    SCB_CMD                      = Offset.fromIntZeroExtend(2);
  private final static Offset    SCB_IRQ                      = Offset.fromIntZeroExtend(3);
  private final static Offset    SCB_GENERAL_PTR              = Offset.fromIntZeroExtend(4);
  private final static Offset    PORT                         = Offset.fromIntZeroExtend(8);
  private final static Offset    EEPROM                       = Offset.fromIntZeroExtend(14);
  private final static Offset    MDI                          = Offset.fromIntZeroExtend(16);
  private final static Offset    RX_DMA_BYTE_COUNT            = Offset.fromIntZeroExtend(20);

  // eeprom control signals
  private static final int EESK = 1;
  private static final int EECS = 2;
  private static final int EEDI = 4;
  private static final int EEDO = 8;
  
  // eeprom operations
  private static final int EE_READ_OP = 6;
  
  // number of eeprom address bits
  private int eepromAddrLength;
  
  private static final short     MDI_READ          = 2;
  private static final short     MDI_WRITE         = 1;
  private static final int       MDI_READY         = (1 << 28);
  private static final int       RFD_COUNT         = 256;
  private static final int       WAIT_SCB_TIMEOUT  = 20000;        // 100ms wait
  private static final int       WAIT_SCB_FAST     = 20;           // Try 20 iterations first before delay
  
  private static final boolean DEBUG_CONFIG = false;
  private static final boolean DEBUG_RX = true;

  private final int phyAddress;
  private int phyId;
  
  private ReceiveFrameDescriptor rfds[];
  private RuState running;
  private int rfdToClean;  // next buffer that needs to be processed
  private int rfdToUse;    // next buffer to allocate
  
/*
 * I82559C parameters


 */
  
  private final byte i82559c_parameters[] = {
/* 0 */    (byte)22,   //  22 configuration bytes
/* 1 */    (byte)8,    //  tx/rx fifo limits
/* 2 */    (byte)0,    //  adaptive IFS
/* 3 */    (byte)1,    // mwi enabled
/* 4 */    (byte)0,    // recveive dma maximum byte count
/* 5 */    (byte)0,    // xmit dma max byte count,dma max count disabled
/* 6 */    (byte)0x36, // std stat counters, std txcb, CNA interrupt, TCO stats
/* 7 */    (byte)0x07, // 3 retrys, discard short frames
/* 8 */    (byte)1,    // MII mode
/* 9 */    (byte)0,
/* 10 */   (byte)0x26, // 7 byte preamble
/* 11 */   (byte)0,
/* 12 */   (byte)0x61, // 96bit IFS
/* 13 */   (byte)0,
/* 14 */   (byte)0,
/* 15 */   (byte)0xC8, // CRS and CDT
/* 16 */   (byte)0,
/* 17 */   (byte)0,
/* 18 */   (byte)0xF3, // padding/stripping enabled, priority FC disabled
/* 19 */   (byte)0x80, // FD pin enabled
/* 20 */   (byte)0x3F, // priority FC field byte 31
/* 21 */   (byte)0x05,
};
  
  public I82559c() throws NoDeviceFoundException
  {
    pci = Pci.find((short)0x8086, (short)0x1229);
    if(pci == null)
    {
      throw new NoDeviceFoundException("I82559c");
    }
    pci.busMaster();
    VM.sysWriteln(pci.toString());
    VM.sysWrite("status ", Integer.toHexString(pci.getStatus()));
    VM.sysWriteln(" command ", Integer.toHexString(pci.getCommand()));
    csr = pci.getBar(0);
    eeprom = new short[256];
    eepromAddrLength = 8;
    eepromSize = 0;
    phyAddress = 1;
    rfds = new ReceiveFrameDescriptor[RFD_COUNT];
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
    csr.store(cmd.value() | address.toInt(), PORT);
  }
  
  public void boot()
  {
    hwReset();
    eepromLoad();
    VM.sysWrite("Ethernet Address: ", VM.intAsHexString(eeprom[0]&0xFFFF));
    VM.sysWrite(":", VM.intAsHexString(eeprom[1]&0xFFFF));
    VM.sysWriteln(":", VM.intAsHexString(eeprom[2]&0xFFFF));
    up();
  }

  /**
   * 
   */
  private void up()
  {
    allocateReceiveFrames();
    allocateCommandBlocks();
    hwInit();
    startReceiver();
  }

  
  /**
   * 
   */
  private void startReceiver()
  {
    if(running != RuState.SUSPENDED) return;
    
    scbWait();
    scbPointer(rfds[rfdToUse].getAddress());
    scbCommand(RucCommand.START);
  }

  /**
   * 
   */
  private void hwInit()
  {
    hwReset();
    selfTest();
    phyInit();
    cucLoadBase();
    rucLoadBase();
    loadUcode();
    configure();
    setupIaAddress();
    cucDumpAddress();
    cucDumpReset();
  }

  /**
   * 
   */
  private void cucDumpReset()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  private void cucDumpAddress()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  private void setupIaAddress()
  {
    int cmdBlock[] = new int[4];
    Address configurePtr = Magic.objectAsAddress(cmdBlock).plus(8);
    configurePtr.store(eeprom[0]);
    configurePtr.store(eeprom[1], Offset.zero().plus(2));
    configurePtr.store(eeprom[2], Offset.zero().plus(4));

    cmdBlock[0] = CB_EL|CB_IA_SETUP;
    cmdBlock[1] = 0;
    
    for(int i=0; i<4; i++)
    {
      VM.sysWrite("ia setup ", i); VM.sysWriteln(" = ", VM.intAsHexString(cmdBlock[i]));
    }
    scbWait();
    scbPointer(Magic.objectAsAddress(cmdBlock));
    scbCommand(CucCommand.START);
    while((cmdBlock[0] & CB_C) == 0)
    {
      Tsc.udelay(100);
    }
    if((cmdBlock[0] & CB_OK) == 0)
    {
      VM.sysWriteln("Configure status: ", cmdBlock[0]);
    }
  }

  /**
   * 
   */
  private void configure()
  {
    int cmdBlock[] = new int[8];
    Address cmdPtr = Magic.objectAsAddress(cmdBlock);
    Address configurePtr = Magic.objectAsAddress(cmdPtr).plus(8);
    
    for(int i=0; i < CFG_BYTE_COUNT; i++)
    {
      configurePtr.store(i82559c_parameters[i], Offset.zero().plus(i));
    }
    cmdBlock[0] = CB_EL|CB_CONFIGURE;
    cmdBlock[1] = 0;
    
    if(DEBUG_CONFIG)
    {
      for(int i=0; i<CFG_BYTE_COUNT; i++)
      {
        VM.sysWrite("config ", i); VM.sysWriteln(" = ", VM.intAsHexString(configurePtr.loadByte(Offset.zero().plus(i))));
      }
    }
    scbWait();
    scbPointer(Magic.objectAsAddress(cmdBlock));
    scbCommand(CucCommand.START);
    while((cmdBlock[0] & CB_C) == 0)
    {
      Tsc.udelay(100);
    }
    if((cmdBlock[0] & CB_OK) == 0)
    {
      VM.sysWriteln("Configure status: ", cmdBlock[0]);
    }
  }

  /**
   * 
   */
  private void loadUcode()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  private void rucLoadBase()
  {
    scbWait();
    scbPointer(Address.zero());
    scbCommand(RucCommand.LOAD_BASE);
  }

  /**
   * 
   */
  private void cucLoadBase()
  {
    scbWait();
    scbPointer(Address.zero());
    scbCommand(CucCommand.LOAD_BASE);
  }

  
  /**
   * Acknowledge all interrupts
   */
  private void scbAck()
  {
    byte status = scbStatus();
    if((status & 0xFF) != 0)
    {
      VM.sysWriteln("ACKing ", VM.intAsHexString(status&0xFF));
    }
    scbStatus(status);
  }
  /**
   * @param status
   */
  private void scbStatus(byte status)
  {
    csr.store(status, SCB_STATUS);
  }

  /**
   * 
   */
  private void scbWait()
  {
    int iters;
    
    scbAck();
    for(iters=0; iters < WAIT_SCB_TIMEOUT; iters++)
    {
      if(scbCommand() == 0)
      {
        break;
      }
      if(iters > WAIT_SCB_FAST)
      {
        Tsc.udelay(5);
      }
    }
    if(iters == WAIT_SCB_TIMEOUT)
    {
      VM.sysWriteln("SCB loadbase timeout!");
      return;
    }
  }

  /**
   * @param command
   */
  private void scbCommand(CucCommand command)
  {
    csr.store(command.register(), SCB_CMD);
  }

  private void scbCommand(RucCommand command)
  {
    csr.store(command.register(), SCB_CMD);
  }

  /**
   * Writer pointer value into the SCB_POINTER register
   * @param pointer
   */
  private void scbPointer(Address pointer)
  {
    csr.store(pointer.toInt(), SCB_GENERAL_PTR);
  }

  /**
   * @return
   */
  private int scbCommand()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * 
   */
  private void selfTest()
  {
    int results[] = new int[8];  // need 16 byte alignment
    Address resultsAddress = Magic.objectAsAddress(results);
    int alignedResults = resultsAddress.toInt();
    /*
     * Align to a 16 byte address
     */
    if((alignedResults & 0xF) != 0)
    {
      alignedResults = (alignedResults + 4) & ~0xF;
    }
    VM.sysWriteln("results address: ", VM.intAsHexString(alignedResults));
    resultsAddress = Address.fromIntZeroExtend(alignedResults);
    resultsAddress.store(0);
    resultsAddress.store(0xFFFFFFFF, Offset.fromIntZeroExtend(4));
    portCmd(E100PortCmd.SELF_TEST, resultsAddress);
    writeFlush();
    Tsc.DEBUG = true;
    Tsc.udelay(1000*100);  // 100ms delay
    Tsc.DEBUG = false;
    // Check for a zero signature
    if(resultsAddress.loadInt() == 0)
    {
      VM.sysWriteln("Selftest signature failure: ", resultsAddress.loadInt());
    }
    // Check for a non-zero results
    if(resultsAddress.loadInt(Offset.fromIntZeroExtend(4)) != 0)
    {
      VM.sysWriteln("Selftest results failure: ", resultsAddress.loadInt(Offset.fromIntZeroExtend(4)));
    }
    disableInterrupt();
  }

  /**
   * 
   */
  private void allocateCommandBlocks()
  {
    
  }

  /**
   * Allocate receive descriptors
   */
  private void allocateReceiveFrames()
  {
    int bufferNumber;
    
    running = RuState.UNINITIALIZED;
    /*
     * Create a circular list of RFDS
     */
    rfds[0] = new ReceiveFrameDescriptor();
    for(bufferNumber=1; bufferNumber < RFD_COUNT; bufferNumber++)
    {
      // Create a linked list of RFDS
      rfds[bufferNumber] = new ReceiveFrameDescriptor();
      rfds[bufferNumber-1].link(rfds[bufferNumber]);
    }
    rfds[RFD_COUNT-1].link(rfds[0]);
    
    /* Set the EL bit on the buffer that is before the last buffer.
     * This lets us update the next pointer on the last buffer without
     * worrying about hardware touching it.
     * We set the size to 0 to prevent hardware from touching this buffer.
     * When the hardware hits the before last buffer with el-bit and size
     * of 0, it will RNR interrupt, the RU will go into the No Resources
     * state.  It will not complete nor write to this buffer. 
     */
   rfds[RFD_COUNT-2].endLink();
   rfds[RFD_COUNT-2].size(0);
   running = RuState.SUSPENDED;
   rfdToUse = rfdToClean = 0;
  }

  /**
   * Process any buffers that have received packets
   */
  private void rxProcessBuffer()
  {
    int actualSize = rfds[rfdToClean].actualSize();
    
    if(DEBUG_RX)
    {
      rfds[rfdToClean].dump();
    }
  }
  
  public void receive()
  {
    while(true)
    {
      rxClean();
      Tsc.udelay(10000);
    }
  }
  /**
   * find RFDS to clean
   */
  private void rxClean()
  {
    /*
     * Keep processing buffers until one that is not complete
     */
    for(; ; rfdToClean++)
    {
      if(rfds[rfdToClean].notComplete())
      {
        break;
      }
      rxProcessBuffer();
    }
  }
  /**
   * 
   */
  private void phyInit()
  {
    short bmcr, stat;
    
    bmcr = bmcr();
    stat = bmsr();
    if(bmcr==0xFFFF || (bmcr==0 && stat==0))
    {
      VM.sysWriteln("PHY is unavailabe! ", phyAddress);
      // Should throw an exception
      return;
    }
    VM.sysWrite("status: ", VM.intAsHexString(stat)); VM.sysWriteln(" ", VM.intAsHexString(bmcr));
    short idLo = physId1();
    short idHi = physId2();
    phyId = (int)idHi<<16 | (int)idLo & 0xFFFF;
    VM.sysWriteln("phy ID = ", VM.intAsHexString(phyId));
  }

  /**
   * @return
   */
  private short physId2()
  {
    return mdioRead(MdiRegister.PHY2);
  }

  /**
   * @return
   */
  private short physId1()
  {
    return mdioRead(MdiRegister.PHY1);
  }

  /**
   * @return
   */
  private short bmsr()
  {
    return mdioRead(MdiRegister.STATUS);
  }

  /**
   * @return
   */
  private short bmcr()
  {
    return mdioRead(MdiRegister.CONTROL);
  }

  /**
   * @param control
   * @return
   */
  private short mdioRead(MdiRegister register)
  {
    int ctrl = (MDI_READ<<26) | (phyAddress<<21) | (register.ordinal()<<16);
    ctrl &= ~(1<<29);
    csr.store(ctrl, MDI);
    int data = csr.loadInt(MDI);
    while((data & MDI_READY) == 0)
    {
      data = csr.loadInt(MDI);
    }
    return (short)data;
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
    //scbStatus();
  }

  /**
   * Return the SCB status
   */
  private byte scbStatus()
  {
    return csr.loadByte(SCB_STATUS);
  }
}
