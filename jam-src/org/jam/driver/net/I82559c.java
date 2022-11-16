/**
 * Created on Aug 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import static org.jam.driver.net.CucCommand.RESUME;
import static org.jam.driver.net.CucCommand.START;
import static org.jam.driver.net.RuState.SUSPENDED;

import org.jam.util.LinkedList;

import org.jam.board.pc.Pci;
import org.jam.board.pc.PciDevice;
import org.jam.cpu.intel.Tsc;
import org.jam.net.InetNexus;
import org.jam.net.NetworkInterface;
import org.jam.net.Route;
import org.jam.net.ethernet.Ethernet;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.ArpTable;
import org.jam.net.inet4.InetAddress;
import org.jam.net.inet4.SendPacket;
import org.jam.system.NoDeviceFoundException;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class I82559c 
implements NetworkInterface, BufferFree 
{
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
  
  // RU status
  private static final int       SCB_STAT_RNR                 = 1<<4;
  
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
  private static final int       RFD_COUNT         = 16;
  private static final int       WAIT_SCB_TIMEOUT  = 20000;        // 100ms wait
  private static final int       WAIT_SCB_FAST     = 20;           // Try 20 iterations first before delay
  
  private static final boolean DEBUG_CONFIG = false;
  private static final boolean DEBUG_RX = true;
  private static final int CBD_COUNT = 256;
  private static final boolean DEBUG_ACKS = false;
  private static final int NAPI_WORK = 16;
  private static final int NAPI_SCHEDULE = 10;  // in milliseconds

  private final int phyAddress;
  private int phyId;
  
  private ReceiveFrameDescriptor rfds[];
  private RuState running;
  private int rfdToClean;  // next buffer that needs to be processed
  private int rfdToUse;    // next buffer to allocate
  private LinkedList<ReceiveFrameDescriptor> rfdFreeList;
  private CommandBlockDescriptor cbdToUse;
  private CommandBlockDescriptor cbdToSend;
  private int cbdAvailable;
  private CommandBlockDescriptor cbdToClean;
  private CucCommand cucCommand;
  private boolean txCleaned=false;
  
  private NetworkQueue txQueue;
  
  private static final RuntimeException freePacketException = new RuntimeException("i82559c:free()");
private static final boolean DEBUG_TX = true;
  
/*
 * I82559C parameters
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| BYTE | D7       | D6       | D5          | D4       | D3          | D2         | D1        | D0          |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 0    | 0        | 0        | byte count                                                                  |
+------+----------+----------+-----------------------------------------------------------------------------+
| 1    | 0        | Transmit FIFO Limit               | Receive FIFO Limit                                 |
+------+----------+-----------------------------------+----------------------------------------------------+
| 2    | Adaptive Interframe Spacing                                                                       |
+------+---------------------------------------------------------------------------------------------------+
| 3    | 0        | 0        | 0           | 0        | Term Write  | Read AI    | Type      | MWI         |
|      |          |          |             |          | on CL       | Enable     | Enable    | Enable      |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 4    | 0        | Receive DMA Minimum Byte Count                                                         |
+------+----------+----------------------------------------------------------------------------------------+
| 5    | DMBC     | Transmit DMA Maximum Byte Count                                                        |
|      | Enable   |                                                                                        |
+------+----------+----------------------------------------------------------------------------------------+
| 6    | Save Bad | Discard  | Ext. Stat.  | Extended | CI          | TCO        | 1         | 0           |
|      | Frames   | Overruns | Count       | TxCB     | Interrupt   | Statistics |           |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 7    | Dynamic  | 2 Frames | 0           | 0        | 0           | Underrun Retry         | Discard     |
|      | TBD      | in FIFO  |             |          |             |                        | Short       |
|      |          |          |             |          |             |                        | Receive     |
+------+----------+----------+-------------+----------+-------------+------------------------+-------------+
| 8    | CSMA     | 0        | 0           | 0        | 0           | 0          | 0         | 1           |
|      | Disable  |          |             |          |             |            |           |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 9    | 0        | 0        | Link        | VLAN     | 0           | 0          | 0         | TCP/UDP     |
|      |          |          | Wake-up     | TCO      |             |            |           | Checksum    |
|      |          |          | Enable      |          |             |            |           |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 10   | Loopback            | Preamble Length        | NSAI        | 1          | 1         | 0           |
+------+---------------------+------------------------+-------------+------------+-----------+-------------+
| 11   | 0        | 0        | 0           | 0        | 0           | 0          | 0         | 0           |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 12   | Interframe Spacing                           | 0           | 0          | 0         | 1           |
+------+----------------------------------------------+-------------+------------+-----------+-------------+
| 13   | IP Address Low                                                                                    |
+------+---------------------------------------------------------------------------------------------------+
| 14   | IP Address High                                                                                   |
+------+---------------------------------------------------------------------------------------------------+
| 15   | CRS and  | 1        | CRC16       | Ignore   | 1           | Wait after | Broadcast | Promiscuous |
|      | CDT      |          |             | U/L      |             | Win        | Disable   |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 16   | FC Delay Least Significant Byte                                                                   |
+------+---------------------------------------------------------------------------------------------------+
| 17   | FC Delay Most Significant Byte                                                                    |
+------+---------------------------------------------------------------------------------------------------+
| 18   | 1        | Priority FC Threshold             | Long Recv   | Receive    | Padding   | Stripping   |
|      |          |                                   | OK          | CRC        | Enable    | Enable      |
|      |          |                                   |             | Transfer   |           |             |
+------+----------+-----------------------------------+-------------+------------+-----------+-------------+
| 19   | Auto     | Force    | Reject FC   | Receive  | Receive     | Transmit   | Magic     | Reserved    |
|      | FDX      | FDX      |             | FC       | FC          | FC         | Packet    |             |
|      |          |          |             | Location | Restop      |            | Wakeup    |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 20   | 0        | Multiple | Priority FC | 1        | 1           | 1          | 1         | 1           |
|      |          | IA       | Location    |          |             |            |           |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
| 21   | 0        | 0        | 0           | 0        | Multicast   | 1          | 0         | 1           |
|      |          |          |             |          | All         |            |           |             |
+------+----------+----------+-------------+----------+-------------+------------+-----------+-------------+
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
  private EthernetAddr macAddress;
  private CommandBlockDescriptor[] cbds;
  private boolean transmitting;
  private int napiWork=NAPI_WORK;
  private int napiSchedule=NAPI_SCHEDULE;
  
  // Statistics
  private int statsBuffersCleaned=0;
  private int statsFreeListEmpty=0;
  private int statsBufferFilled=0;
  private int statsStopPointMoved=0;
  
  private long txPackets=0;
  private long txBytes=0;
private InetAddress ipAddress;
private int netmask;
  
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
    rfdFreeList = new LinkedList<ReceiveFrameDescriptor>();
    transmitting = false;
    txQueue = new NetworkQueue();
//    arpTable = new ArpTable();
    setNetworkInterface(this);
    Route.addRoute(InetAddress.HOST, InetAddress.HOST, 0xffffffff, this);
  }
  
  private void setNetworkInterface(I82559c i82559c) {
	// TODO Auto-generated method stub
	
}

public I82559c(InetAddress inet, int netmask) throws NoDeviceFoundException
  {
      this();
      ipAddress = inet;
      this.netmask = netmask;
      Route.addRoute(ipAddress, InetAddress.HOST, 0xffffffff, this);
      Route.addRoute(new InetAddress(ipAddress.inet4()&netmask), InetAddress.HOST, netmask, this);
      VM.sysWriteln("i82559c done");
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
    initEthernetMac();
    VM.sysWrite("Ethernet Address: ", VM.intAsHexString(eeprom[0]&0xFFFF));
    VM.sysWrite(":", VM.intAsHexString(eeprom[1]&0xFFFF));
    VM.sysWriteln(":", VM.intAsHexString(eeprom[2]&0xFFFF));
    up();
  }

  /**
   * Store the device's mac address
   */
  private void initEthernetMac()
  {
    macAddress = new EthernetAddr((byte)eeprom[0], (byte)(eeprom[0]>>8), 
                                  (byte)eeprom[1], (byte)(eeprom[1]>>8), 
                                  (byte)eeprom[2], (byte)(eeprom[2]>>8));
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

  private void interrupt()
  {
    byte status = scbStatus();
    if(status == 0 || status == 0xFF)
    {
      return;
    }
    if((status & SCB_STAT_RNR) != 0)
    {
      running = SUSPENDED;
    }
    // todo: Schedule driver polling to process packets
  }
  
  public int work()
  {
    return napiWork;
  }
  
  public int schedule()
  {
    return napiSchedule;
  }
  public void poll()
  {
    rxClean();
    txClean();
  }
  /**
   * Return used tx blocks
   */
  private void txClean()
  {
      CommandBlockDescriptor toClean;
      for(toClean=cbdToClean; toClean.isComplete();)
      {
          if(DEBUG_TX) VM.sysWriteln("Cleaning "+VM.addressAsHexString(Magic.objectAsAddress(toClean)));
          if(toClean.hasBuffer())
          {
              txPackets++;
              txBytes += toClean.transmitBytes();
              toClean.cleanCbd();
              txCleaned = true;
          }
          toClean = cbdToClean = toClean.next();
      }
      
      if(txCleaned)
      {
          
      }
  }

  /**
   * Start the chip and the timer
   */
  private void startReceiver()
  {
    if(running != RuState.SUSPENDED) return;
    
    scbWait();
    scbPointer(rfds[rfdToUse].getAddress());
    scbCommand(RucCommand.START);
    
    // Start the timer
    NapiManager.addInterface(this);
  }

  private void xmitFrame(Packet packet)
  {
    CommandBlockDescriptor cbd = getCommandBlock();
    cbd.configureTransmitPacket(packet);
    execute(cbd);
    VM.sysWriteln("xmit packet done!");
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
   * Executes a command block. Returns true if command executed, false otherwise
   * @param cmd
   * @return true = command executed; false = command did not execute
   */
  private boolean execCommand(CommandBlockDescriptor cmd)
  {
    /**
     * see if it is working on another command. Return true if 
     * it could not 
     */
    if(!scbWait())
    {
      return false;
    }
    /*
     * Set the SCB pointer if the command is START
     */
    if(cucCommand == START)
    {
      scbPointer(cmd);
    }
    scbCommand(cucCommand);
    while(cmd.notComplete())
    {
      Tsc.udelay(100);
    }
    if(cmd.notOk())
    {
      VM.sysWriteln("Configure status: ", cmd.getSatus());
    }
    return true;
  }

  /**
   * @param cbd
   */
  private void execute(CommandBlockDescriptor cbd)
  {
    // set SUSPEND on the new command
    cbd.suspend();
    // unset SUSPEND on the previous command
    cbd.previous().unsetSuspend();
    while(moreToSend())
    {
      if(execCommand(cbdToSend))
      {
        cucCommand = RESUME;
        cbdToSend = cbdToSend.next();
      }
      else
      {
        VM.sysWriteln("Command did not execute!");
      }
    }
  }

  /**
   * @return
   */
  private boolean moreToSend()
  {
    return cbdToSend != cbdToUse;
  }

  /**
   * Returns a command block descriptor; maintains command block count and next
   * available command block
   * @return an available command block descriptor 
   */
  private CommandBlockDescriptor getCommandBlock()
  {
    CommandBlockDescriptor newCbd = cbdToUse;
    cbdToUse = cbdToUse.next();
    cbdAvailable--;
    VM.sysWrite("Get cmd block: ", Magic.objectAsAddress(newCbd)); 
    VM.sysWrite(" ", VM.intAsHexString(newCbd.getScbPointer()));
    VM.sysWriteln(" ", cbdAvailable);
    return newCbd;
  }

  /**
   * 
   */
  private void setupIaAddress()
  {
    CommandBlockDescriptor cbd = getCommandBlock();
    cbd.configureMacAddress(eeprom);
    execute(cbd);
  }

  private void configure()
  {
    CommandBlockDescriptor cbd = getCommandBlock();
    cbd.configureParameters(i82559c_parameters);
    execute(cbd);
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
  @SuppressWarnings("unused")
  private byte scbAck()
  {
    byte status = scbStatus();
    if(DEBUG_ACKS && ((status & 0xFF) != 0))
    {
      VM.sysWriteln("ACKing ", VM.intAsHexString(status&0xFF));
    }
    scbStatus(status);
    return status;
  }
  /**
   * @param status
   */
  private void scbStatus(byte status)
  {
    csr.store(status, SCB_STATUS);
  }

  /**
   * Return the SCB status
   */
  private byte scbStatus()
  {
    return csr.loadByte(SCB_STATUS);
  }

  /**
   * Waits for previous command to be accepted
   * @return false if timeout occurred waiting for command, true if command accepted
   */
  private boolean scbWait()
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
      VM.sysWriteln("SCB wait timeout! ", scbCommand());
      return false;
    }
    return true;
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
   * @return
   */
  private byte scbCommand()
  {
    return csr.loadByte(SCB_CMD);
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
   * @param cmd
   */
  private final void scbPointer(CommandBlockDescriptor cmd)
  {
    csr.store(cmd.getScbPointer(), SCB_GENERAL_PTR);
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
    CommandBlockDescriptor cbd=null, cbdPrevious;
    
    /*
     * Allocate
     */
    cbdPrevious = cbdToUse = cbdToSend = cbdToClean = new CommandBlockDescriptor();
    int commandBlockIndex;
    for(commandBlockIndex=0; commandBlockIndex < CBD_COUNT-1; commandBlockIndex++)
    {
      cbd = new CommandBlockDescriptor();
      cbdPrevious.next(cbd);
      cbdPrevious.link(cbd);
      cbd.previous(cbdPrevious);
      cbdPrevious = cbd;
    }
    cbd.next(cbdToUse);
    cbd.link(cbdToUse);
    cbdToUse.previous(cbd);
    cbdAvailable = CBD_COUNT;
    cucCommand = START;
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
    rfds[0] = new ReceiveFrameDescriptor(this);
    for(bufferNumber=1; bufferNumber < RFD_COUNT; bufferNumber++)
    {
      // Create a linked list of RFDS
      rfds[bufferNumber] = new ReceiveFrameDescriptor(this);
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
   /*
    * Setup the free list
    */
   for(bufferNumber=0; bufferNumber<RFD_COUNT; bufferNumber++)
   {
     rfdFreeList.add(new ReceiveFrameDescriptor(this));
   }
   VM.sysWriteln("rfd 0: ", rfds[0].toString());
  }

  /**
   * Process any buffers that have received packets
   */
  final private void rxProcessBuffer()
  {
    ReceiveFrameDescriptor rfd = rfds[rfdToClean];
    int actualSize = rfd.actualSize();
    
    if(DEBUG_RX) 
    { 
      VM.sysWrite("rxProcess: "); 
      VM.sysWriteln(rfd.toString()); 
      rfd.dump();
    }
    
//    rxQueue.put(rfd.packet());
    InetNexus.put(rfd.packet());
//    rfds[rfdToClean] = null;
//    free(rfd);
  }
  
  public void receive()
  {
      rxClean();
  }
  
  final private int advanceRfdIndex(int index, int step)
  {
    return (index+step) & (RFD_COUNT-1);
  }
  
  final private int advanceRfdIndex(int index)
  {
    return advanceRfdIndex(index, 1);
  }
  
  /**
   * Fill a new rx buffer
   */
  final private void rxFillBuffers()
  {
    /*
     * fill it only when it is null
     */
    for( ;rfds[rfdToUse]==null; )
    {
      if(rfds[rfdToUse] == null)
      {
        if(rfdFreeList.isEmpty())
        {
          VM.sysWriteln("RFD free list empty!");
          statsFreeListEmpty++;
          return;
        }
        ReceiveFrameDescriptor nuBuffer = rfdFreeList.remove();
        rfds[advanceRfdIndex(rfdToUse, -1)].link(nuBuffer);
        nuBuffer.link(rfds[advanceRfdIndex(rfdToUse)]);
        rfds[rfdToUse] = nuBuffer;
        nuBuffer.reset();
        if(DEBUG_RX) { VM.sysWrite("rxFill: "); VM.sysWriteln(nuBuffer.toString()); }
        statsBufferFilled++;
      }
      rfdToUse = advanceRfdIndex(rfdToUse);
    }
  }
  /**
   * find RFDS to clean
   */
  private void rxClean()
  {
    boolean restartRequired = false;
    boolean printstats =false;
    /*
     * Keep processing buffers until one that is not complete
     */
    for(; ; )
    {
      if(rfds[rfdToClean].notComplete())
      {
        break;
      }
      rxProcessBuffer();
      statsBuffersCleaned++;
      rfdToClean = advanceRfdIndex(rfdToClean);
      printstats = true;
    }
    
    int oldStoppingPoint = advanceRfdIndex(rfdToUse, -2);
    rxFillBuffers();
    int newStoppingPoint = advanceRfdIndex(rfdToUse, -2);
    
    /*
     * See if we need to set an new stopping point
     */
    if(oldStoppingPoint != newStoppingPoint)
    {
      /* Set the el-bit on the buffer that is before the last buffer.
       * This lets us update the next pointer on the last buffer
       * without worrying about hardware touching it.
       * We set the size to 0 to prevent hardware from touching this
       * buffer.
       * When the hardware hits the before last buffer with el-bit
       * and size of 0, it will RNR interrupt, the RUS will go into
       * the No Resources state.  It will not complete nor write to
       * this buffer. 
       */
      /*
       * Set the new stop point
       */
      rfds[newStoppingPoint].setStopPoint();
      /*
       * Clear the old stopping point
       */
      rfds[oldStoppingPoint].resetStopPoint();
      statsStopPointMoved++;
      
    }
    if(running == SUSPENDED)
    {
      restartRequired = true;
    }
    if(printstats && DEBUG_RX)
    {
      VM.sysWrite(" rxClean: ", rfdToClean);
      VM.sysWrite(" ", rfdToUse);
      VM.sysWrite(" ", oldStoppingPoint);
      VM.sysWriteln(" ", newStoppingPoint);
    }
  }
  
  public void transmitFrame(Packet packet)
  {
    if(transmitting)
    {
      txQueue.put(packet);
    }
    else
    {
      xmitFrame(packet);
    }
  }
  /**
   * public interface for transmitting a packet
   * @param frame
   */
  public void transmit(Ethernet packet)
  {
    // queue packet for transmission
    transmitting = false;
    transmitFrame(packet.getPacket());
    transmitting =false;
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
    scbStatus();
  }

  /**
   * @return
   */
  public EthernetAddr getEthernetAddress()
  {
    return macAddress;
  }

  public void setEthernetAddress(EthernetAddr macAddress)
  {
      this.macAddress = macAddress;
  }
  /* (non-Javadoc)
   * @see org.jam.driver.net.BufferFree#free(org.jam.net.inet4.Packet)
   * 
   * The parameter class must be a ReceiveFrameDescriptor
   */
  public void free(Packet packet)
  {
    if(!(packet instanceof ReceiveFrameDescriptor))
    {
      throw freePacketException;
    }
    rfdFreeList.add((ReceiveFrameDescriptor) packet);
  }
  
  final public void printStats()
  {
    VM.sysWrite("cleaned  ", statsBuffersCleaned);
    VM.sysWrite(" filled ", statsBufferFilled);
    VM.sysWrite(" moved ", statsStopPointMoved);
    VM.sysWriteln(" empty ", statsFreeListEmpty);
  }

    public void send(EthernetAddr destinationMac, Packet packet, short proto)
    {
        System.out.println("eepro100 send 1");
        Ethernet frame = new Ethernet(destinationMac, packet, proto);
        // Set the src address
        frame.setSource(macAddress);
        transmit(frame);
    }

    public void send(SendPacket packet)
    {
        System.out.println("eepro100 send");
        Ethernet frame = new Ethernet(EthernetAddr.BROADCAST_ADDRESS, packet.getPacket(), packet.getProto());
        // Set the src address
        frame.setSource(macAddress);
        transmit(frame);
    }

	@Override
	public InetAddress getInetAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNetMask() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMtu() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMtu(int mtu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNetMask(int mask) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInetAddress(InetAddress inetAddress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EthernetAddr arp(InetAddress inet) {
		// TODO Auto-generated method stub
		return null;
	}
}
