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

    private final static Offset PROTO_OFFSET = Offset.zero().plus(2);
    private static final Offset HLEN_OFFSET = Offset.zero().plus(4);
    private static final Offset PLEN_OFFSET = Offset.zero().plus(5);
    
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
        packetAddr.store(ByteOrder.hostToNetwork(protocolType), Offset.zero().plus(2));
        packetAddr.store(hwAddressLen, Offset.zero().plus(4));
        packetAddr.store(protoAddrLen, Offset.zero().plus(5));
        packetAddr.store(ByteOrder.hostToNetwork(opCode), Offset.zero().plus(6));
        for (packetIndex = 0; packetIndex < senderHa.length; packetIndex++)
        {
            packetAddr.store(senderHa[packetIndex], Offset.zero().plus(8 + packetIndex));
        }
        for (packetIndex = 0; packetIndex < senderPa.length; packetIndex++)
        {
            packetAddr.store(senderPa[packetIndex], Offset.zero().plus(14 + packetIndex));
        }
        for (packetIndex = 0; packetIndex < targetPa.length; packetIndex++)
        {
            packetAddr.store(targetPa[packetIndex], Offset.zero().plus(24 + packetIndex));
        }

        return packet;
    }

    public short getProto()
    {
        return EtherType.ARP.type;
    }

    /*
     * Processes arp as a reply packet
     */
    public void reply()
    {
        if(packet == null) return;
        Address arpPacket = packet.getPacketAddress();
        // hw type should be ethernet
        if(ByteOrder.networkToHost(arpPacket.loadShort()) != HT_ETHERNET) return;
        // protocol must be IPv4
        if(ByteOrder.networkToHost(arpPacket.loadShort(PROTO_OFFSET)) != EtherType.IPV4.type) return;
        // check hw length; must be 6
        if(arpPacket.loadByte(HLEN_OFFSET) != 6) return;
        // check protocol length; must be 4
        if(arpPacket.loadByte(PLEN_OFFSET) != 4) return;
        /*
         * Make sure it is for me
         */
        
    }

}
