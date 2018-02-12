package org.jam.net;

import java.net.DatagramPacket;
import java.net.NetworkInterface;

import org.jam.driver.net.Packet;
import org.jikesrvm.classloader.RVMArray;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

public class InetPacket implements Packet {
	private int offset;
	private byte buffer[];
	private static int HEADER_SIZE = 44;		// size of the udp, ip, and ethernet headers
	private int packetSize;
	private Connection connection;
	private NetworkInterface netInterface;
	
	public InetPacket(DatagramPacket packet, Connection connection) {
		int bufferSize = packet.getLength()+HEADER_SIZE;
		byte[] srcBuffer = packet.getData();
		buffer = new byte[bufferSize];
		RVMArray.arraycopy(srcBuffer, packet.getOffset(), buffer, HEADER_SIZE, packet.getLength());
		offset = HEADER_SIZE;
		packetSize = packet.getLength();
		this.connection = connection;
	}

	public byte[] getArray() {
		return buffer;
	}

	/**
	 * Returns the address of a packet's header beginning
	 */
	public Address getAddress() {
		return Magic.objectAsAddress(buffer).plus(offset);
	}

	/*
	 * Returns the address of the packet's buffer
	 */
	public Address getBufferAddress()
	{
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
	}

	public byte getProtocol() {
		return connection.getProtocol();
	}

	public boolean needToFragment() {
		return false;
	}

	public int getLocalAddress() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRemoteAddress() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void send() {
		/*
		 * Need to find the route and the next hop to send the packet
		 */
	}

}
