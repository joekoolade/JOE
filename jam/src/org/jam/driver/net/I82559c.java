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
  public static int CFG_RX_FIFO_LIMIT            = 8;         // byte 1
  public static int CFG_TX_FIFO_LIMIT            = 0<<4;      // byte 1
  public static int CFG_ADAPTIVE_IFS             = 0;         // byte 2
  public static int CFG_MWI_ENABLE               = 1;         // byte 3
  public static int CFG_TYPE_ENABLE              = 0<<1;      // byte 3
  public static int CFG_READ_ALIGN_ENABLE        = 0<<2;      // byte 3
  public static int CFG_TERM_WRITE_CACHE_LINE    = 0<<3;      // byte 3
  public static int CFG_RX_DMA_MAX_COUNT         = 0;         // byte 4
  public static int CFG_TX_DMA_MAX_COUNT         = 0;         // byte 5
  public static int CFG_DMA_MAX_COUNT_ENABLE     = 0<<7;      // byte 5
  public static int CFG_LATE_SCB_UPDATE          = 0;         // byte 6
  public static int CFG_DIRECT_RX_DMA            = 1<<1;      // byte 6
  public static int CFG_TCO_STATS                = 1<<2;      // byte 6
  public static int CFG_CI_INTR                  = 0<<3;      // byte 6
  public static int CFG_STANDARD_TCB             = 1<<4;      // byte 6
  public static int CFG_STANDARD_STAT_COUNTER    = 1<<5;      // byte 6
  public static int CFG_RX_SAVE_OVERRUNS         = 0<<6;      // byte 6
  public static int CFG_RX_SAVE_BAD_FRAMES       = 0<<7;      // byte 6
  public static int CFG_RX_DISCARD_SHORT_FRAMES  = 1;         // byte 7
  public static int CFG_TX_UNDERRUN_RETRY        = 3<<1;      // byte 7
  public static int CFG_RX_EXTENDED_RFD          = 0;         // byte 7
  public static int CFG_TX_TWO_FRAMES_IN_FIFO    = 0<<6;      // byte 7
  public static int CFG_TX_DYNAMIC_TDB           = 0<<7;      // byte 7
  public static int CFG_MII_MODE                 = 1;         // byte 8
  public static int CFG_CSMA_DISABLED            = 0<<7;      // byte 8
  public static int CFG_RX_TCPUDP_CHECKSUM       = 0;         // byte 9
  public static int CFG_VLAN_ARP_TCO             = 0<<4;      // byte 9
  public static int CFG_LINK_STATUS_WAKE         = 0<<5;      // byte 9
  public static int CFG_ARP_WAKE                 = 0<<6;      // byte 9
  public static int CFG_MCMATCH_WAKE             = 0<<7;      // byte 9
  public static int CFG_RESERVED10               = 6;          // byte 10
  public static int CFG_NO_SOURCE_ADDR_INSERTION = 1<<3;      // byte 10. 1=no
  public static int CFG_PREAMBLE_LENGTH          = 2<<4;      // byte 10. 2=7bits
  public static int CFG_LOOPBACK                 = 0<<6;      // byte 10
  public static int CFG_LINEAR_PRIORITY          = 0;         // byte 11
  public static int CFG_LINEAR_PRIORITY_MODE     = 1;         // byte 12
  public static int CFG_IFS                      = 6<<4;      // byte 12
  public static int CFG_IP_ADDR_LO               = 0;         // byte 13
  public static int CFG_IP_ADDR_HI               = 0;         // byte 14
  public static int CFG_CRS_OR_CDT               = 1<<7;      // byte 15. 1=both
  public static int CFG_CRC16                    = 0<<6;      // byte 15. 0=crc 32, 1=crc 16
  public static int CFG_IGNORE_UL                = 0<<4;      // byte 15. 0=consider U/L bit
  public static int CFG_WAIT_AFTER_WIN           = 0<<2;      // byte 15. 0=disabled
  public static int CFG_BROADCAST_DISABLED       = 0<<1;      // byte 15. 0=receive broadcasts
  public static int CFG_PROMICUOUS_MODE          = 0;         // byte 15. 0=disabled
  public static int CFG_FC_DELAY_LO              = 0;         // byte 16
  public static int CFG_FC_DELAY_HI              = 0;         // byte 17
  public static int CFG_LONG_RECV_OK             = 0<<3;      // byte 18. 0=disabled
  public static int CFG_RCV_CRC_TRANSFER         = 0<<2;      // byte 18. 0=don't transfer crc
  public static int CFG_PADDING_ENABLED          = 1<<1;      // byte 18. 1=padding bytes are added
  public static int CFG_STRIPPING_ENABLED        = 1;         // byte 18. 1=enabled
  public static int CFG_PRIORITY_FC_THRESHOLD    = 7<<4;      // byte 18. 7=disabled
  public static int CFG_FD_PIN_ENABLE            = 1<<7;         // byte 19. 1=enabled
  public static int CFG_FD_FORCE                 = 0<<6;         // byte 19. 0=off
  public static int CFG_REJECT_FC                = 0<<5;         // byte 19. 0=off
  public static int CFG_FD_RESTART_FC            = 0<<4;         // byte 19. 0=off
  public static int CFG_FD_RESTOP_FC             = 0<<3;         // byte 19. 0=off
  public static int CFG_FD_XMIT_FC_DISABLE       = 0<<2;         // byte 19. 0=enabled
  public static int CFG_MP_WAKEUP_DISABLE        = 0<<1;         // byte 19. 0=enabled
  public static int CFG_IA_WAKEUP_ENABLE         = 0;         // byte 19. 0=enabled
  public static int CFG_IA_MULTIPLE              = 0<<6;         // byte 20. 0=disabled
  public static int CFG_PRIORITY_FC_LOC          = 1<<5;         // byte 20. 1=byte 31
  public static int CFG_MULTICAST                = 0<<3;         // byte 21. 0=disabled  
  
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

  private final int phyAddress;
  private int phyId;
  
  private ReceiveFrameDescriptor rfds[];
  private RuState running;
  private int rfdToClean;  // next buffer that needs to be processed
  private int rfdToUse;    // next buffer to allocate
  
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
    // TODO Auto-generated method stub
    
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
    scbPointer(Magic.objectAsAddress(cmdBlock).toInt());
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
    int cmdBlock[] = new int[8+4];
    Address cmdPtr = Magic.objectAsAddress(cmdBlock);
    if((cmdPtr.toInt() & 0xF) != 0)
    {
      cmdPtr = Address.fromIntZeroExtend((cmdPtr.toInt() + 16) & ~0xF);
    }
    Address configurePtr = Magic.objectAsAddress(cmdPtr).plus(8);
    
    configurePtr.store((byte)CFG_BYTE_COUNT); 
    configurePtr.store((byte)(CFG_TX_FIFO_LIMIT|CFG_RX_FIFO_LIMIT), Offset.zero().plus(1));
    configurePtr.store((byte)CFG_ADAPTIVE_IFS, Offset.zero().plus(2));
    configurePtr.store((byte)(CFG_TERM_WRITE_CACHE_LINE|CFG_READ_ALIGN_ENABLE|CFG_TYPE_ENABLE|CFG_MWI_ENABLE), Offset.zero().plus(3));
    configurePtr.store((byte)CFG_RX_DMA_MAX_COUNT, Offset.zero().plus(4));
    configurePtr.store((byte)(CFG_DMA_MAX_COUNT_ENABLE|CFG_TX_DMA_MAX_COUNT), Offset.zero().plus(5));
    configurePtr.store((byte)(CFG_RX_SAVE_BAD_FRAMES|CFG_RX_SAVE_OVERRUNS|CFG_STANDARD_STAT_COUNTER|CFG_STANDARD_TCB|CFG_CI_INTR|CFG_TCO_STATS|CFG_DIRECT_RX_DMA|CFG_LATE_SCB_UPDATE), Offset.zero().plus(6));
    configurePtr.store((byte)(CFG_TX_DYNAMIC_TDB|CFG_TX_TWO_FRAMES_IN_FIFO|CFG_TX_UNDERRUN_RETRY|CFG_RX_DISCARD_SHORT_FRAMES), Offset.zero().plus(7));
    configurePtr.store((byte)(CFG_CSMA_DISABLED|CFG_MII_MODE), Offset.zero().plus(8));
    configurePtr.store((byte)(CFG_MCMATCH_WAKE|CFG_ARP_WAKE|CFG_LINK_STATUS_WAKE|CFG_VLAN_ARP_TCO|CFG_RX_TCPUDP_CHECKSUM), Offset.zero().plus(9));
    configurePtr.store((byte)(CFG_NO_SOURCE_ADDR_INSERTION|CFG_PREAMBLE_LENGTH|CFG_LOOPBACK|CFG_RESERVED10), Offset.zero().plus(10));
    configurePtr.store((byte)CFG_LINEAR_PRIORITY, Offset.zero().plus(11));
    configurePtr.store((byte)(CFG_IFS|CFG_LINEAR_PRIORITY_MODE), Offset.zero().plus(12));
    configurePtr.store((byte)CFG_IP_ADDR_LO, Offset.zero().plus(13));
    configurePtr.store((byte)CFG_IP_ADDR_HI, Offset.zero().plus(14));
    configurePtr.store((byte)(CFG_CRS_OR_CDT|CFG_CRC16|CFG_IGNORE_UL|CFG_WAIT_AFTER_WIN|CFG_BROADCAST_DISABLED|CFG_PROMICUOUS_MODE|0x48), Offset.zero().plus(15));
    configurePtr.store((byte)CFG_FC_DELAY_LO, Offset.zero().plus(16));
    configurePtr.store((byte)CFG_FC_DELAY_HI, Offset.zero().plus(17));
    configurePtr.store((byte)(CFG_PRIORITY_FC_THRESHOLD|CFG_LONG_RECV_OK|CFG_RCV_CRC_TRANSFER|CFG_PADDING_ENABLED|CFG_STRIPPING_ENABLED|0x80), Offset.zero().plus(18));
    configurePtr.store((byte)(CFG_FD_PIN_ENABLE|CFG_FD_FORCE|CFG_REJECT_FC), Offset.zero().plus(19));
    configurePtr.store((byte)(CFG_IA_MULTIPLE|CFG_PRIORITY_FC_LOC|0x1F), Offset.zero().plus(20));
    configurePtr.store((byte)CFG_MULTICAST|0x5, Offset.zero().plus(21));
    
//    cmdBlock[0] = CB_EL|CB_CONFIGURE;
//    cmdBlock[1] = 0;
    cmdPtr.store(CB_EL|CB_CONFIGURE);
    cmdPtr.store(0, Offset.zero().plus(4));
    
    for(int i=0; i<CFG_BYTE_COUNT; i++)
    {
      VM.sysWrite("config ", i); VM.sysWriteln(" = ", VM.intAsHexString(configurePtr.loadByte(Offset.zero().plus(i))));
    }
    scbWait();
    scbPointer(Magic.objectAsAddress(cmdPtr).toInt());
    scbCommand(CucCommand.START);
    while((cmdPtr.loadInt() & CB_C) == 0)
    {
      Tsc.udelay(100);
    }
    if((cmdPtr.loadInt() & CB_OK) == 0)
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
    scbPointer(0);
    scbCommand(RucCommand.LOAD_BASE);
  }

  /**
   * 
   */
  private void cucLoadBase()
  {
    scbWait();
    scbPointer(0);
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
  private void scbPointer(int pointer)
  {
    csr.store(pointer, SCB_GENERAL_PTR);
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
