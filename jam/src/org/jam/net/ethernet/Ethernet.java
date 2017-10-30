/**
 * Created on Jul 24, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.ethernet;

import org.jam.net.ByteOrder;
import org.jam.net.inet4.Arp;
import org.jam.net.inet4.Packet;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 * Implements the ethernet framing. Takes data and creates an ethernet packet that 
 * can be transmitted by a ethernet device
 */
public class Ethernet {
  public final static short  PROTO_IP            = 0x800;
  public final static short  PROTO_ARP           = 0x806;
  private final static int   MAX_PAYLOAD         = 1500;
  private final static int   MIN_PAYLOAD         = 46;
  public final static int    FRAME_LENGTH        = 1518;   // does not include FCS
  public final static int    FCS_LENGTH          = 4;
  private final static int   MIN_ETHERNET_PACKET = 64;
  public final static int    HEADER_SIZE         = 14;
  private static final Offset typeOffset = Offset.zero().plus(12);
  private static final short IP4_PROTO = 0x800;
  private static final short ARP_PROTO = 0x806;
  
  private byte[] frame;
  private byte[] packetArray;
  private Address packetAddress;
  private Packet packet;
 
  /*
   * Ethernet frame
   * | Destination | Source | Type | Payload | FCS |
   */
  public Ethernet(EthernetAddr dst, Arp arpPacket)
  {
    // Copy destination address
    int srcIndex, targetIndex=0;
    packet = arpPacket.getPacket();
    packet.setHeadroom(HEADER_SIZE);
    packetArray = packet.getArray();
    packetAddress = Magic.objectAsAddress(packetArray);
    byte[] ethAddress = dst.asArray();
    for(srcIndex=0, targetIndex=0; srcIndex < ethAddress.length; srcIndex++, targetIndex++)
    {
      packetArray[targetIndex] = ethAddress[srcIndex];
    }
    // ARP type
    packetAddress.store(ByteOrder.hostToNetwork(ARP_PROTO), Offset.zero().plus(12));
    VM.sysWriteln("ethernet packetaddr ", packetAddress);
  }
  
  public byte[] getFrame()
  {
    return frame;
  }

  /**
   * @return
   */
  public Packet getPacket()
  {
    return packet;
  }
}
