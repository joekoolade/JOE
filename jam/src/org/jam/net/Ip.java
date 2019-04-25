package org.jam.net;

import org.jam.driver.net.Packet;
import org.jikesrvm.VM;
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
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_RX = true;
	
    private IpStats stats;
	private byte tos = 0; // best effort
	private byte ttl = (byte)255; 
	
	public void send(InetPacket packet) {
	    VM.sysWriteln("IP send ", packet.getOffset());
		Address ipHeader = packet.getPacketAddress();
		int vhlen = (VERSION<<4) | HEADER_LEN;
		ipHeader.store((byte)vhlen);
		ipHeader.store(tos, TOS_FIELD);
		ipHeader.store(ttl, TTL_FIELD);
		ipHeader.store(packet.getProtocol(), PROTOCOL_FIELD);
		ipHeader.store(0, CHECKSUM_FIELD);
		ipHeader.store(ByteOrder.hostToNetwork(packet.getLocalAddress()), SRCADDR_FIELD);
		ipHeader.store(ByteOrder.hostToNetwork(packet.getRemoteAddress()), DSTADDR_FIELD);
		ipHeader.store(ByteOrder.hostToNetwork((short)packet.getSize()),LENGTH_FIELD);
		if(packet.needToFragment())
		{
			fragmentPacket(packet);
		}
		else
		{
			ipHeader.store(ByteOrder.hostToNetwork(DONT_FRAGMENT), FRAGMENT_FIELD);
		}
		short csum = checksum(packet);
		ipHeader.store(csum, CHECKSUM_FIELD);
        if (DEBUG) VM.hexDump(packet.getArray(),0,packet.getBufferSize());
		packet.send();
	}

	final public void receive(InetPacket packet)
	{
	    if(DEBUG_RX) System.out.println("ip.receive"); 
	    Address ipHeader = packet.getAddress();
	    byte vhl = ipHeader.loadByte();
	    int headerLength = vhl & 0xF;
	    if((vhl>>4) != 4 || headerLength < 5)
        {
            // drop packet
	        System.out.println("dropped vhl "+Integer.toHexString(vhl));
	        stats.headerError();
	        return;
        }
	    int csum = checksum(packet);
        if(DEBUG_RX) System.out.println("ip.receive csum "+Integer.toHexString(csum)+" "+Integer.toHexString(ipHeader.loadShort(CHECKSUM_FIELD)&0xFFFF));
	    if(csum !=0)
	    {
	        System.out.println("csum failure");
	        stats.checksumError();
	    }
	    int len = ByteOrder.networkToHost(ipHeader.loadShort(LENGTH_FIELD)) & 0xFFFF;
	    if(packet.getSize() < len)
	    {
	        stats.truncated();
	    }
	    else if(len < headerLength * 4)
	    {
	        stats.headerError();
	    }
	    if((ByteOrder.networkToHost(ipHeader.loadShort(FRAGMENT_FIELD)) & MORE_FRAGMENTS) !=0)
	    {
	        processFragment(packet);
	    }
        if(DEBUG_RX) System.out.println("ip.receive checks done");
        int sourceAddress = ByteOrder.networkToHost(ipHeader.loadInt(SRCADDR_FIELD));
        int destinationAddress = ByteOrder.networkToHost(ipHeader.loadInt(DSTADDR_FIELD));
        int protocol = ipHeader.loadByte(PROTOCOL_FIELD) & 0xFF;
        if(protocol==IpProto.UDP.protocol())
        {
            // udp receive
            packet.pull(headerLength*4);
            Udp.receive(packet, sourceAddress, destinationAddress);
        }
        if(DEBUG_RX) System.out.println("ip.receive done");
	}
	private void processFragment(InetPacket packet)
    {
        // TODO Auto-generated method stub
        
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
