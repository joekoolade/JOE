/**
 * Created on Jul 22, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.inet4;

import org.jam.driver.net.PacketBuffer;
import org.jam.net.ByteOrder;
import org.jam.net.ethernet.Ethernet;
import org.jam.net.ethernet.EthernetAddr;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class Arp {
  public final static int SIZE = 28;
  private final static short HT_ETHERNET = 1;

  private final static short OP_REQUEST  = 1;
  private final static short OP_REPLY    = 2;
  
  private byte senderHa[];
  private short hwType;
  private short protocolType;
  private short opCode;
  private byte senderPa[];
  private byte targetHa[];
  private byte targetPa[];
  private byte hwAddressLen;
  private byte protoAddrLen;
  
  /**
   * Create an ARP request
   */
  public Arp(EthernetAddr senderEther, InetAddress sendIp, InetAddress targetIp)
  {
    hwType = HT_ETHERNET;
    protocolType = Ethernet.PROTO_IP;
    opCode = OP_REQUEST;
    senderHa = senderEther.asArray();
    senderPa = sendIp.asArray();
    targetPa = targetIp.asArray();
    hwAddressLen = 6;
    protoAddrLen = 4;
  }
  
  /**
   * Create an ARP reply
   * @param request
   * @param senderHa
   */
  public Arp(Arp request, Ethernet senderHa)
  {
    
  }
  public Arp(short hwType, short protocolType)
  {
    this.hwType = hwType;
    this.protocolType = protocolType;
  }
  public void request()
  {
    opCode = OP_REQUEST;
  }
  
  public PacketBuffer getPacket()
  {
    int packetIndex = 0;
    ArpPacket packet = new ArpPacket();
    Address packetAddr = packet.getAddress();
    packetAddr.store(ByteOrder.hostToNetwork(hwType));
    packetAddr.store(ByteOrder.hostToNetwork(protocolType), Offset.zero().plus(2));
    packetAddr.store(hwAddressLen, Offset.zero().plus(4));
    packetAddr.store(protoAddrLen, Offset.zero().plus(5));
    packetAddr.store(ByteOrder.hostToNetwork(opCode), Offset.zero().plus(6));
    for(packetIndex=0; packetIndex<senderHa.length; packetIndex++)
    {
      packetAddr.store(senderHa[packetIndex], Offset.zero().plus(8+packetIndex));
    }
    for(packetIndex=0; packetIndex<senderPa.length; packetIndex++)
    {
      packetAddr.store(senderPa[packetIndex], Offset.zero().plus(14+packetIndex));
    }
    for(packetIndex=0; packetIndex<targetPa.length; packetIndex++)
    {
      packetAddr.store(targetPa[packetIndex], Offset.zero().plus(24+packetIndex));
    }

    return packet;
  }

public static void hasDevice(int remoteAddress) {
	// TODO Auto-generated method stub
	
}
}
