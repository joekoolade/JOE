/**
 * Created on Dec 27, 2017
 *
 * Copyright (C) Joe Kulig, 2017 All rights reserved.
 */
package org.jam.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jam.driver.net.Packet;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
/**
 * @author Joe Kulig
 *
 */
public class Udp {
    private static final Offset DESTINATION_PORT = Offset.fromIntSignExtend(2);
    private static final Offset LENGTH = Offset.fromIntSignExtend(4);
    private static final Offset CHECKSUM = Offset.fromIntSignExtend(6);

    private static final boolean DEBUG_PSEUDOHEADER = true;
    private static InetConnections connectionTable = new InetConnections();
    
    InetSocketAddress localAddress;
    InetSocketAddress remoteAddress;
    int ttl;
    private Connection connection;
    private int pseudoHeaderSum;
    private int offset;
    private InetPacket packet;
    private Ip ip;
    private short packetChecksum;
    private boolean disableCheckSum = false;
    private boolean pseudoSum = false;
    
    public Udp()
    {
        ip = new Ip();
    }

    /**
     * @param inetSocketAddress
     */
    public void bind(InetSocketAddress inetSocketAddress) throws SocketException, IOException
    {
        localAddress = inetSocketAddress;
        // put it in the connection table
        connectionTable.add(inetSocketAddress, connection);
    }

    /**
     * @param inetSocketAddress
     * @param i
     */
    public void connect(InetSocketAddress inetSocketAddress, int i)
    {
        remoteAddress = inetSocketAddress;
        if (localAddress == null)
        {
            localAddress = new InetSocketAddress(10000);
        }
        connectionTable.add(localAddress, connection);
        // check if address is routable
        // Create a new connection
    }

    /**
     * @param ttl
     */
    public void setTimeToLive(int ttl)
    {
        this.ttl = ttl;
    }

    /**
     * @return
     */
    public int getTimeToLive()
    {
        return ttl;
    }

    /**
     * @return
     */
    public InetSocketAddress getLocalAddress() throws IOException
    {
        return localAddress;
    }

    /**
     * @param packet
     * @throws IOException
     */
    public void send(DatagramPacket packet) throws IOException
    {
        /*
         * Get a new connection
         */
        VM.sysWriteln("udp send0");
        if (connection == null)
        {
            initConnection(packet);
        }
        VM.sysWriteln("udp length: " + packet.getLength());
        if (packet.getLength() > 0xFFFF)
        {
            throw new IOException("Packet too big");
        }
        VM.sysWriteln("get new inet packet");
        this.packet = new InetPacket(packet, connection);
        this.packet.setHeadroom(8);
        VM.sysWriteln("New inet packet");
        send();
    }

    private void send()
    {
        Address udpPacket = packet.getPacketAddress();
        // Setup the udp packet header
        // source port
        udpPacket.store(ByteOrder.hostToNetwork((short) localAddress.getPort()));
        // desination port
        udpPacket.store(ByteOrder.hostToNetwork((short) remoteAddress.getPort()), DESTINATION_PORT);
        // packet length
        udpPacket.store(ByteOrder.hostToNetwork((short) packet.getSize()), LENGTH);
        // packet checksum
        udpPacket.store(0, CHECKSUM);
        computeChecksum();
        udpPacket.store(ByteOrder.hostToNetwork(packetChecksum), CHECKSUM);
        // send it on for IP processing
        System.out.println("private send "+packet.getOffset()+" "+packet.getSize());
        packet.setHeadroom(20);
        VM.hexDump(packet.getArray(),0,packet.getBufferSize());
        ip.send(packet);
    }

    private void initConnection(DatagramPacket packet) throws NoRouteToHostException
    {
        VM.sysWriteln("udp initConnection");
        if(packet != null)
        {
            if(remoteAddress==null)
            {
                remoteAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
            }
        }
        connection = new Connection(localAddress, remoteAddress, IpProto.UDP);
    }

    /**
     * The is the 16 bit sum of the source, destination, and protocol values
     */
    private void computePseudoHeaderSum()
    {
        // Sum up the source IP address
        int val = connection.getLocalInet();
        pseudoHeaderSum = ((val>>16) & 0xFFFF) + (val & 0xFFFF);
        // Add in the destination IP address
        val = connection.getRemoteInet();
        pseudoHeaderSum += ((val>>16) & 0xFFFF) + (val & 0xFFFF);
        // Add in the protocol
        pseudoHeaderSum += IpProto.UDP.protocol();
        // add in the size
        pseudoHeaderSum += packet.getSize();
        // Add the carry over
        val = (pseudoHeaderSum >> 16) & 0xFFFF;
        pseudoHeaderSum += val;
        if (DEBUG_PSEUDOHEADER) System.out.println("computePseudoHeaderSum "+Integer.toHexString(pseudoHeaderSum));
    }

    private void computeChecksum()
    {
        int csum = 0;

        if(disableCheckSum)
        {
            packetChecksum = 0;
            return;
        }
        if(!pseudoSum)
        {
            computePseudoHeaderSum();
            pseudoSum = true;
        }
        Address data = packet.getPacketAddress();
        csum = pseudoHeaderSum;
        for (int words = packet.getSize() >> 1; words > 0; words--)
        {
            int val = ByteOrder.hostToNetwork(data.loadShort());
            csum += (val & 0xFFFF);
            data = data.plus(2);
        }
        if ((packet.getSize() & 0x1) != 0)
        {
            csum += (ByteOrder.hostToNetwork(data.loadShort()) & 0x00FF);
        }
        // Add the carry overs
        csum = (csum >> 16) + (csum & 0xFFFF);
        packetChecksum = (short) ~csum;
    }

    /**
     * @param packet
     * @return
     */
    public SocketAddress receive(DatagramPacket packet) throws SocketTimeoutException, InterruptedIOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param optionId
     * @param value
     */
    public void setMulticastInterface(int optionId, InetAddress value)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param optionId
     * @param value
     */
    public void setOption(int optionId, Object value)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param optionId
     * @return
     */
    public Object getMulticastInterface(int optionId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param optionId
     * @return
     */
    public Object getOption(int optionId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     */
    public void close() throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param addr
     */
    public void join(InetAddress addr)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param addr
     */
    public void leave(InetAddress addr)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param address
     * @param netIf
     */
    public void joinGroup(InetSocketAddress address, NetworkInterface netIf)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param address
     * @param netIf
     */
    public void leaveGroup(InetSocketAddress address, NetworkInterface netIf)
    {
        // TODO Auto-generated method stub

    }

    /**
     * 
     */
    public void shutdownInput()
    {
        // TODO Auto-generated method stub

    }

    /**
     * 
     */
    public void shutdownOutput()
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param data
     */
    public void sendUrgentData(int data)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param queuelen
     */
    public void listen(int queuelen)
    {
        // TODO Auto-generated method stub

    }

    public void setBlocking(boolean blocking)
    {
        // TODO Auto-generated method stub

    }

    public void disconnect()
    {
        // TODO Auto-generated method stub

    }

    public InetSocketAddress getPeerAddress()
    {
        // TODO Auto-generated method stub
        return remoteAddress;
    }

    public int write(ByteBuffer src) throws IOException
    {
        VM.sysWriteln("udp write");
//        disableCheckSum = true;
        if (connection == null)
        {
            try
            {
                initConnection(null);
            }
            catch (NoRouteToHostException e)
            {
                throw new IOException("No Route");
            }
        }
        if (src.capacity() > 0xFFFF)
        {
            throw new IOException("Packet too big");
        }
        packet = new InetPacket(src, connection, 8);
        send();
        return 0;
    }

    public long writeGathering(ByteBuffer[] srcs, int offset2, int length2)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int read(ByteBuffer dst)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public long readScattering(ByteBuffer[] dsts, int offset2, int length2)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public SocketAddress receive(ByteBuffer dst)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int send(ByteBuffer src, InetSocketAddress dst)
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
