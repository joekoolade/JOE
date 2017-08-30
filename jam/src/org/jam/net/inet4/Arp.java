/**
 * Created on Jul 22, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.inet4;

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
  private final static int SIZE = 28;
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
   * Default constructor creates 
   */
  public Arp(EthernetAddr senderEther, InetAddress sendIp, InetAddress targetIp)
  {
    hwType = HT_ETHERNET;
    protocolType = Ethernet.PROTO_IP;
    senderHa = senderEther.asArray();
    senderPa = sendIp.asArray();
    targetPa = targetIp.asArray();
    hwAddressLen = 6;
    protoAddrLen = 4;
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
  
  public Packet getPacket()
  {
    int packetIndex = 0;
    byte data[] = new byte[SIZE];
    Address dataAddr = Magic.objectAsAddress(data);
    dataAddr.store(ByteOrder.hostToNetwork(hwType));
    dataAddr.store(ByteOrder.hostToNetwork(protocolType), Offset.zero().plus(2));
    dataAddr.store(hwAddressLen, Offset.zero().plus(4));
    dataAddr.store(protoAddrLen, Offset.zero().plus(5));
    dataAddr.store(ByteOrder.hostToNetwork(opCode), Offset.zero().plus(6));
    for(packetIndex=0; packetIndex<senderHa.length; packetIndex++)
    {
      dataAddr.store(senderHa[packetIndex], Offset.zero().plus(8+packetIndex));
    }
    for(packetIndex=0; packetIndex<senderPa.length; packetIndex++)
    {
      dataAddr.store(senderPa[packetIndex], Offset.zero().plus(14+packetIndex));
    }
    for(packetIndex=0; packetIndex<targetPa.length; packetIndex++)
    {
      dataAddr.store(targetPa[packetIndex], Offset.zero().plus(24+packetIndex));
    }

    return new Packet(data);
  }
}
