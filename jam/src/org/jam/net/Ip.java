package org.jam.net;

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

public class Ip {
	private final static int VERSION = 4;
	private final static int HEADER_LEN = 5;
	private final static short DONT_FRAGMENT = 0x4000;
	private final static short MORE_FRAGMENTS = 0x2000;
	private final static Offset TOS_FIELD = Offset.fromIntSignExtend(1);
	private final static Offset LENGTH_FIELD = Offset.fromIntSignExtend(2);
	private final static Offset ID_FIELD = Offset.fromIntSignExtend(4);
	private final static Offset FRAGMENT_FIELD = Offset.fromIntSignExtend(6);
	private final static Offset TTL_FIELD = Offset.fromIntSignExtend(8);
	private final static Offset PROTOCOL_FIELD = Offset.fromIntSignExtend(9);
	private final static Offset CHECKSUM_FIELD = Offset.fromIntSignExtend(10);
	private final static Offset SRCADDR_FIELD = Offset.fromIntSignExtend(12);
	private final static Offset DSTADDR_FIELD = Offset.fromIntSignExtend(16);
	
	private byte tos = 0; // best effort
	private byte ttl = (byte)255; 
	
	public void send(InetPacket packet) {
	    System.out.println("IP send");
		Address ipHeader = packet.getPacketAddress();
		ipHeader.store((byte)((VERSION<<4) | HEADER_LEN));
		ipHeader.store(tos, TOS_FIELD);
		ipHeader.store(ttl, TTL_FIELD);
		ipHeader.store(packet.getProtocol(), PROTOCOL_FIELD);
		ipHeader.store(0, CHECKSUM_FIELD);
		ipHeader.store(packet.getLocalAddress(), SRCADDR_FIELD);
		ipHeader.store(packet.getRemoteAddress(), DSTADDR_FIELD);
		ipHeader.store(ByteOrder.hostToNetwork((short)packet.getSize()));
		if(packet.needToFragment())
		{
			fragmentPacket(packet);
		}
		else
		{
			ipHeader.store(DONT_FRAGMENT, FRAGMENT_FIELD);
		}
		short csum = checksum(packet);
		ipHeader.store(csum, CHECKSUM_FIELD);
		packet.send();
	}

	private short checksum(InetPacket packet) {
		int csum=0;
		
		/*
		 * Assumes a 20 byte IP header with no options
		 */
		Address headerAddress = packet.getPacketAddress();
		csum += headerAddress.loadShort();
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(2)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(4)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(6)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(8)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(10)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(12)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(14)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(16)) & 0xffff);
		csum += (headerAddress.loadShort(Offset.fromIntZeroExtend(18)) & 0xffff);
		// Add the carry overs
		csum = (csum >> 16) + (csum & 0xFFFF);

		return (short)~csum;
	}

	private void fragmentPacket(InetPacket packet) {
		throw new Error("IP fragmentation not implemented");
	}

}
