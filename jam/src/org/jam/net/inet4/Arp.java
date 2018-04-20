/**
 * Created on Jul 22, 2017
 *
 * Copyright (C) Joe Kulig, 2017 All rights reserved.
 */
package org.jam.net.inet4;

import org.jam.driver.net.Packet;
import org.jam.driver.net.PacketBuffer;
import org.jam.net.ByteOrder;
import org.jam.net.EtherType;
import org.jam.net.NetworkInterface;
import org.jam.net.ethernet.Ethernet;
import org.jam.net.ethernet.EthernetAddr;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class Arp 
implements SendPacket 
{
    public final static int SIZE = 28;
    private final static short HT_ETHERNET = 1;

    private final static short OP_REQUEST = 1;
    private final static short OP_REPLY = 2;

    private final static Offset PROTO_OFFSET = Offset.fromIntSignExtend(2);
    private static final Offset HLEN_OFFSET = Offset.fromIntSignExtend(4);
    private static final Offset PLEN_OFFSET = Offset.fromIntSignExtend(5);
    private static final Offset OPCODE_OFFSET = Offset.fromIntSignExtend(6);
    private static final Offset SENDER_HW_OFFSET = Offset.fromIntSignExtend(8);
    private static final Offset SENDER_PROTO_OFFSET = Offset.fromIntSignExtend(14);
    private static final Offset TARGET_HW_OFFSET = Offset.fromIntSignExtend(18);
    private static final Offset TARGET_PROTO_OFFSET = Offset.fromIntSignExtend(24);
    
    private byte senderHa[];
    private short hwType;
    private short protocolType;
    private short opCode;
    private byte senderPa[];
    private byte targetHa[];
    private byte targetPa[];
    private byte hwAddressLen;
    private byte protoAddrLen;
    private Packet packet;
    private int senderInet;
    private int targetInet;
    
    /**
     * Create an ARP request
     */
    Arp(EthernetAddr senderEther, InetAddress sendIp, InetAddress targetIp)
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
     * 
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

    /*
     * Fills in the arp fields from the packet
     */
    public Arp(Packet packet)
    {
        this.packet = packet;
        Address packetAddr = packet.getPacketAddress();
        hwType = ByteOrder.networkToHost(packetAddr.loadShort());
        protocolType = ByteOrder.networkToHost(packetAddr.loadShort(PROTO_OFFSET));
        hwAddressLen = packetAddr.loadByte(HLEN_OFFSET);
        protoAddrLen = packetAddr.loadByte(PLEN_OFFSET);
        opCode = ByteOrder.networkToHost(packetAddr.loadShort(OPCODE_OFFSET));
        senderHa = new byte[6];
        targetHa = new byte[6];
        senderPa = new byte[4];
        targetPa = new byte[4];
        for(int addressIndex=0; addressIndex < 6; addressIndex++)
        {
            senderHa[addressIndex] = packetAddr.loadByte(SENDER_HW_OFFSET.plus(addressIndex));
            targetHa[addressIndex] = packetAddr.loadByte(TARGET_HW_OFFSET.plus(addressIndex));
        }
        for(int addressIndex=0; addressIndex < 4; addressIndex++)
        {
            senderPa[addressIndex] = packetAddr.loadByte(SENDER_PROTO_OFFSET.plus(addressIndex));
            targetPa[addressIndex] = packetAddr.loadByte(TARGET_PROTO_OFFSET.plus(addressIndex));
        }
        senderInet = ByteOrder.networkToHost(packetAddr.loadInt(SENDER_PROTO_OFFSET));
        targetInet = ByteOrder.networkToHost(packetAddr.loadInt(TARGET_PROTO_OFFSET));
    }

    public void request()
    {
        opCode = OP_REQUEST;
    }

    public PacketBuffer getPacket()
    {
        int packetIndex = 0;
        ArpPacket packet = new ArpPacket();
        Address packetAddr = packet.getPacketAddress();
        packetAddr.store(ByteOrder.hostToNetwork(hwType));
        packetAddr.store(ByteOrder.hostToNetwork(protocolType), PROTO_OFFSET);
        packetAddr.store(hwAddressLen, HLEN_OFFSET);
        packetAddr.store(protoAddrLen, PLEN_OFFSET);
        packetAddr.store(ByteOrder.hostToNetwork(opCode), OPCODE_OFFSET);
        for (packetIndex = 0; packetIndex < senderHa.length; packetIndex++)
        {
            packetAddr.store(senderHa[packetIndex], SENDER_HW_OFFSET.plus(packetIndex));
        }
        for (packetIndex = 0; packetIndex < senderPa.length; packetIndex++)
        {
            packetAddr.store(senderPa[packetIndex], SENDER_PROTO_OFFSET.plus(packetIndex));
        }
        for (packetIndex = 0; packetIndex < targetPa.length; packetIndex++)
        {
            packetAddr.store(targetPa[packetIndex], TARGET_PROTO_OFFSET.plus(packetIndex));
        }
        return packet;
    }

    /*
     * return true if hw type is ethernet and hw len is 6
     */
    public boolean verifyEthernet()
    {
        return ( hwType == HT_ETHERNET ) && ( hwAddressLen == 6 );
    }
    
    /*
     * return true if protocol type is IPv4 and protocol len is 4
     */
    public boolean verifyIpv4()
    {
        return (protocolType == EtherType.IPV4.type()) && (protoAddrLen==4);
    }
    
    /*
     * return sender's ipv4 address
     */
    public int senderInet()
    {
        return senderInet;
    }
    
    /*
     * return target's ipv4 address
     */
    public int targetInet()
    {
        return targetInet;
    }
    
    /*
     * return target's mac address
     */
    public byte[] targetMac()
    {
        return targetHa;
    }
    
    /*
     * return sender's mac address
     */
    public byte[] senderMac()
    {
        return senderHa;
    }
    public short getProto()
    {
        return EtherType.ARP.type();
    }
}
