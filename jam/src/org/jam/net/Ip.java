package org.jam.net;

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

public class Ip {
	private final static int VERSION = 4;
	private final static int HEADER_LEN = 5;
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
		Address ipHeader = packet.getAddress();
		ipHeader.store((byte)((VERSION<<4) | HEADER_LEN));
		ipHeader.store(tos, TOS_FIELD);
		ipHeader.store(ttl, TTL_FIELD);
		ipHeader.store(packet.getProtocol(), PROTOCOL_FIELD);
		ipHeader.store(ByteOrder.hostToNetwork((short)packet.getSize()));
	}

}
