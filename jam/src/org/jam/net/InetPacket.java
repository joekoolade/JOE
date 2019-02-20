package org.jam.net;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import org.jam.driver.net.Packet;
import org.jam.driver.net.PacketBuffer;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.Arp;
import org.jikesrvm.classloader.RVMArray;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

public class InetPacket implements Packet {
	private int offset;
	private byte buffer[];
	private static int UDP_HEADROOM = 42;		// size of the udp, ip, and ethernet headers
	private static int TCP_HEADROOM = 54;
	private int packetSize;
	private Connection connection;
	private NetworkInterface netInterface;
	
	public InetPacket(DatagramPacket packet, Connection connection) {
		int bufferSize = packet.getLength()+UDP_HEADROOM;
		byte[] srcBuffer = packet.getData();
		buffer = new byte[bufferSize];
		System.out.println("InetPacket "+packet.getOffset());
		RVMArray.arraycopy(srcBuffer, packet.getOffset(), buffer, UDP_HEADROOM, packet.getLength());
		offset = UDP_HEADROOM-8;
		packetSize = packet.getLength();
		this.connection = connection;
		netInterface = connection.getNetworkInterface();
	}

	public InetPacket(ByteBuffer src, Connection connection, int headroom)
    {
        int bufferSize = src.capacity()+UDP_HEADROOM;
        byte[] srcBuffer = src.array();
        buffer = new byte[bufferSize];
        RVMArray.arraycopy(srcBuffer, 0, buffer, UDP_HEADROOM, src.capacity());
        System.out.println("InetPacket1 "+bufferSize);
        offset = UDP_HEADROOM - headroom;
        packetSize = src.capacity() + headroom;
        this.connection = connection;
        netInterface = connection.getNetworkInterface();
    }

    public byte[] getArray() {
		return buffer;
	}

	/**
	 * Returns the address of a packet's header beginning
	 */
	public Address getAddress() {
		return Magic.objectAsAddress(buffer);
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * returns data size of packet
	 */
	public int getSize() {
		return packetSize;
	}

	public void append(Packet packet) {
		// TODO Auto-generated method stub

	}

	public Address prepend(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	public void prepend(Packet packet) {
		// TODO Auto-generated method stub

	}

	public void setHeadroom(int size) {
		if(size > offset)
		{
			throw new RuntimeException("No Headroom");
		}
		offset -= size;
		packetSize += size;
	}

	public byte getProtocol() {
		return connection.getProtocol();
	}

	public boolean needToFragment() {
		return false;
	}

	public int getLocalAddress() {
		return connection.getLocalInet();
	}

	public int getRemoteAddress() {
		return connection.getRemoteInet();
	}

	public void send() {
		/*
		 * At this point the packet has a route. We just need
		 * to get the mac  address of the destination
		 */
	    System.out.println("Inet send");
		EthernetAddr destinationMac = netInterface.arp(connection.getRemote());
		System.out.println("Arp done");
		netInterface.send(destinationMac, this, EtherType.IPV4.type());
	}

    public Address getPacketAddress()
    {
        return Magic.objectAsAddress(buffer).plus(offset);
    }

    public int getBufferSize()
    {
        return buffer.length;
    }

    public void pull(int size)
    {
       offset += size;
    }

}
