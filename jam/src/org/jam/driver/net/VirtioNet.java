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
  private static final int RECVIEVE_VIRTQ_INDEX = 0;
  private static final int TRANSMIT_VIRTQ_INDEX = 1;
  private static final int CONTROL_VIRTQ_INDEX = 2;
  
  private Virtq receiveVirtq;
  private Virtq transmitVirtq;
  private Virtq controlVirtq;
  private NetDeviceCfg deviceCfg;
  
  public VirtioNet() throws NoDeviceFoundException
  {
    pci = Pci.find(0x1AF4, 0x1000);
    if(pci == null)
    {
      throw new NoDeviceFoundException("VirtioNet");
    }
    VM.sysWriteln(pci.toString());
    if(pci.hasCapabilities())
    {
      findCapabilities();
    }
//    caps = new ArrayList<VirtioPciCap>();
//    Iterator<PciCapability> capsIter = pci.getCapsIter();
//    while(capsIter.hasNext())
//    {
//      PciCapability cap = capsIter.next();
//      if(cap.isVendorSpecific())
//      {
//        VirtioPciCap virtioCap = createVirtioCap(cap);
//        caps.add(virtioCap);
//        VM.sysWriteln(virtioCap.toString());
//      }
//    }
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
          VM.sysWriteln(cap.toString());
        }
      }
      else if(PciCapability.isMsiX(capField & 0xFF))
      {
        VM.sysWriteln("CAP MSIX ", VM.intAsHexString(capField));
        msixCap = new MsiXCap(pci, capField, capPointer);
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
    queueSetup();
  }
  
  void negotiate()
  {
    cfg.setDriverFeatureSelect(0);
    cfg.setDriverFeature(MAC | STATUS | CTRL_VQ | CTRL_RX | CTRL_RX_EXTRA);
  }
  
  private void queueSetup()
  {
    int numOfQueues = cfg.getNumQueues();
    VM.sysWriteln("# of VQueues ", numOfQueues);
    for(int i=0; i < numOfQueues; i++)
    {
      cfg.setQueueSelect(i);
      int queueSize = cfg.getQueueSize();
      if(queueSize == 0)
      {
        continue;
      }
      VM.sysWrite("VQueue ", i); VM.sysWriteln(" size ", queueSize);
    }
    cfg.setQueueSelect(RECVIEVE_VIRTQ_INDEX);
    receiveVirtq = new Virtq(cfg.getQueueSize());
    receiveVirtq.allocate(true);
    cfg.setDescQueue(receiveVirtq.virtDescTable);
    cfg.setAvailQueue(receiveVirtq.virtAvail);
    cfg.setUsedQueue(receiveVirtq.virtUsed);
    cfg.setQueueSelect(TRANSMIT_VIRTQ_INDEX);
    transmitVirtq = new Virtq(cfg.getQueueSize());
    cfg.setQueueSelect(CONTROL_VIRTQ_INDEX);
    controlVirtq = new Virtq(cfg.getQueueSize());
  }
}
