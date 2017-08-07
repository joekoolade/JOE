/**
 * Created on Mar 8, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import java.util.ArrayList;
import java.util.Iterator;

import org.jam.board.pc.Pci;
import org.jam.board.pc.PciCapability;
import org.jam.board.pc.PciDevice;
import org.jam.system.NoDeviceFoundException;
import org.jikesrvm.VM;

/**
 * @author Joe Kulig
 *
 *
 */
public class VirtioNet {
  final PciDevice pci;
  CommonCfg cfg;
  ArrayList<VirtioPciCap> caps;
  private MsiXCap msixCap;
  
  /*
   * Features
   */
  final private static int CSUM                = 0x00000001;
  final private static int GUEST_CSUM          = 0x00000002;
  final private static int CTRL_GUEST_OFFLOADS = 0x00000004;
  final private static int MAC                 = 0x00000020;
  final private static int GUEST_TSO4          = 0x00000080;
  final private static int GUEST_TSO6          = 0x00000100;
  final private static int GUEST_ECN           = 0x00000200;
  final private static int GUEST_UFO           = 0x00000400;
  final private static int HOST_TSO4           = 0x00000800;
  final private static int HOST_TSO6           = 0x00001000;
  final private static int HOST_ECN            = 0x00002000;
  final private static int HOST_UFO            = 0x00004000;
  final private static int MRG_RXBUF           = 0x00008000;
  final private static int STATUS              = 0x00010000;
  final private static int CTRL_VQ             = 0x00020000;
  final private static int CTRL_RX             = 0x00040000;
  final private static int CTRL_VLAN           = 0x00080000;
  final private static int CTRL_RX_EXTRA       = 0x00100000;
  final private static int GUEST_ANNOUNCE      = 0x00200000;
  final private static int MQ                  = 0x00400000;
  final private static int CTRL_MAC_ADDR       = 0x00800000;
  final private static int NOTIFY_ON_EMPTY     = 0x01000000;
  final private static int ANY_LAYOUT          = 0x08000000;
  final private static int RING_INDIRECT_DESC  = 0x10000000;
  final private static int RING_EVENT_IDX      = 0x20000000;
  private static final short RECEIVE_VIRTQ_INDEX = 0;
  private static final short TRANSMIT_VIRTQ_INDEX = 1;
  private static final short CONTROL_VIRTQ_INDEX = 2;
  
  private Virtq receiveVirtq;
  private short rxNotifyOffset;
  private Virtq transmitVirtq;
  private short txNotifyOffset;
  private Virtq controlVirtq;
  private short ctlNotifyOffset;
  private NetDeviceCfg deviceCfg;
  private NotifyCfg notifyCfg;
  
  public VirtioNet() throws NoDeviceFoundException
  {
    pci = Pci.find(0x1AF4, 0x1000);
    if(pci == null)
    {
      throw new NoDeviceFoundException("VirtioNet");
    }
    //pci.disableInterrupt();
    VM.sysWriteln(pci.toString());
    VM.sysWrite("status ", Integer.toHexString(pci.getStatus()));
    VM.sysWriteln(" command ", Integer.toHexString(pci.getCommand()));
    if(pci.hasCapabilities())
    {
      findCapabilities();
    }
  }
  
  private void findCapabilities()
  {
    int capPointer = pci.getCapabilityPointer();
    for( ; capPointer != 0; )
    {
      /*
       * Read first cap field
       */
      int capField = pci.readConfig32(capPointer);
      /*
       * Find VENDOR SPECIFIC
       */
      if(PciCapability.isVendorSpecific(capField & 0xFF))
      {
        VirtioPciCap cap = VirtioPciCap.createCap(pci, capField, capPointer);
        if (cap != null)
        {
          if(cap instanceof CommonCfg)
          {
            cfg = (CommonCfg) cap;
          }
          else if(cap instanceof NetDeviceCfg)
          {
            deviceCfg = (NetDeviceCfg) cap;
          }
          else if(cap instanceof NotifyCfg)
          {
            notifyCfg = (NotifyCfg) cap;
          }
          VM.sysWriteln(cap.toString());
        }
      }
      else if(PciCapability.isMsiX(capField & 0xFF))
      {
        VM.sysWriteln("CAP MSIX ", VM.intAsHexString(capField));
        msixCap = new MsiXCap(pci, capField, capPointer);
        msixCap.disableInterrupts();
        short control = msixCap.getControl();
        VM.sysWriteln("CAP MSIX ", VM.intAsHexString(control));
        VM.sysWriteln(msixCap.toString());
      }
      else if(PciCapability.isMsi(capField & 0xFF))
      {
        VM.sysWriteln("CAP MSI ", VM.intAsHexString(capField));
      }
      capPointer = pci.capNextPointer(capPointer);
    }
  }

  public void boot()
  {
    cfg.acknowledge();
    cfg.driver();
    negotiate();
    cfg.featuresOK();
    if(!cfg.areFeaturesOk())
    {
      VM.sysWriteln("Features not accepted! ", cfg.getDeviceStatus());
      System.exit(0);
    }
    VM.sysWriteln("Features have been accepted!");
    cfg.configMsixNoVector();
    queueSetup();
    cfg.driverOK();
    VM.sysWriteln("Device Status: ", deviceCfg.getStatus());
  }
  
  void negotiate()
  {
    cfg.setDriverFeatureSelect(0);
    cfg.setDriverFeature(MAC | STATUS | CTRL_VQ | CTRL_RX);
  }
  
  private void queueSetup()
  {
    int numOfQueues = cfg.getNumQueues();
    VM.sysWriteln("# of VQueues ", numOfQueues);
    for(short i=0; i < numOfQueues; i++)
    {
      cfg.setQueueSelect(i);
      int queueSize = cfg.getQueueSize();
      if(queueSize == 0)
      {
        continue;
      }
      VM.sysWrite("VQueue ", i); VM.sysWriteln(" size ", queueSize);
    }
    /*
     * Setup the rx virtq
     */
    cfg.setQueueSelect(RECEIVE_VIRTQ_INDEX);
    int queueSize = cfg.getQueueSize();
    receiveVirtq = new Virtq(queueSize);
    /*
     * Allocates the rx virtq descriptor table
     */
    receiveVirtq.allocate(true);
    receiveVirtq.availTable.noInterrupts();
    VM.sysWrite("RX virtq: "); VM.sysWriteln(receiveVirtq.toString());
    /*
     * Configure device with rx virtq
     */
    VM.sysWriteln("RX virtq size:", queueSize);
//    cfg.setQueueSize((short)queueSize);
    cfg.setDescQueue(RECEIVE_VIRTQ_INDEX, receiveVirtq.virtDescTable);
    cfg.setAvailQueue(RECEIVE_VIRTQ_INDEX, receiveVirtq.virtAvail);
    cfg.setUsedQueue(RECEIVE_VIRTQ_INDEX, receiveVirtq.virtUsed);
    cfg.queueMsixNoVector();
    rxNotifyOffset = cfg.getQueueNotifyOffset();
    cfg.displayQueues();
    notifyCfg.notify(rxNotifyOffset, RECEIVE_VIRTQ_INDEX);
    
    /*
     * Setup the tx virtq
     */
    cfg.setQueueSelect(TRANSMIT_VIRTQ_INDEX);
    queueSize = cfg.getQueueSize();
    VM.sysWriteln("TX virtq size:", queueSize);
    transmitVirtq = new Virtq(queueSize);
    transmitVirtq.allocate(false);
    cfg.setDescQueue(TRANSMIT_VIRTQ_INDEX, transmitVirtq.virtDescTable);
    cfg.setAvailQueue(TRANSMIT_VIRTQ_INDEX, transmitVirtq.virtAvail);
    cfg.setUsedQueue(TRANSMIT_VIRTQ_INDEX, transmitVirtq.virtUsed);
    cfg.queueMsixNoVector();
    cfg.displayQueues();

    /*
     * Setup the control virtq
     */
    cfg.setQueueSelect(CONTROL_VIRTQ_INDEX);
    queueSize = cfg.getQueueSize();
    VM.sysWriteln("CTL virtq size:", queueSize);
    controlVirtq = new Virtq(queueSize);
    controlVirtq.allocate(false);
    cfg.setDescQueue(CONTROL_VIRTQ_INDEX, controlVirtq.virtDescTable);
    cfg.setAvailQueue(CONTROL_VIRTQ_INDEX, controlVirtq.virtAvail);
    cfg.setUsedQueue(CONTROL_VIRTQ_INDEX, controlVirtq.virtUsed);
    cfg.queueMsixNoVector();
    cfg.displayQueues();

    cfg.enableQueue(RECEIVE_VIRTQ_INDEX);
    cfg.enableQueue(TRANSMIT_VIRTQ_INDEX);
    cfg.enableQueue(CONTROL_VIRTQ_INDEX);

  }
  
  /*
   * Simple receive. Allocates a buffer and waits for it
   * to receive data
   */
  public void receive()
  {
    receiveVirtq.initializeAvailableBuffers();
    while(true)
    {
      byte[] buffer = receiveVirtq.waitForBuffer();
      for(int i=0; i<4; i++)
      {
        for(int j=0; j<16; j++)
        {
          VM.sysWrite(" ", Integer.toHexString(buffer[i*16 + j]));
        }
        VM.sysWriteln();
      }
    }
  }
}
