/**
 * Created on Jul 24, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import java.util.Iterator;
import java.util.LinkedList;

import org.jam.net.inet4.CleanPacket;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class PacketBuffer implements Packet
{

    private byte        buffer[];
    private int         length;
    private Address     bufferAddr;
    private int         offset;
    private int         headroom;
    private CleanPacket cleaner;

    /*
     * Used to prepend and append other packets
     */
    private LinkedList<Packet> packetList;

    /*
     * Create packet from an array
     */
    public PacketBuffer(byte[] buffer, int offset)
    {
        this.buffer = buffer;
        length = buffer.length;
        this.offset = offset;
        headroom = offset;
        bufferAddr = Magic.objectAsAddress(buffer);
    }

    public PacketBuffer(byte[] buffer)
    {
        this(buffer, 0);
    }

    public PacketBuffer(int size)
    {
        this(size, 0);
    }

    /**
     * Create a packet that starts at offset
     * 
     * @param size
     * @param offset
     */
    public PacketBuffer(int size, int offset)
    {
        this.offset = offset;
        headroom = offset;
        buffer = new byte[size];
        length = size;
        bufferAddr = Magic.objectAsAddress(buffer);
    }

    public byte[] getArray()
    {
        return buffer;
    }

    /**
     * Gets current addres of the packet
     * 
     * @return packet address
     */
    public Address getPacketAddress()
    {
        return bufferAddr.plus(offset);
    }

    public int getOffset()
    {
        return offset;
    }

    public int getSize()
    {
        return length;
    }

    public void append(Packet packet)
    {
        packetList.add(packet);
        length += packet.getSize();
    }

    /**
     * Returns space before current buffer
     * 
     * @param size
     *            space before the current buffer
     * @return address pointer
     */
    public Address prepend(int size)
    {
        return null;
    }

    public void prepend(Packet packet)
    {
        packetList.addFirst(packet);
        length += packet.getSize();
    }

    /**
     * Creates a new contiguous buffer that contains all packet data. This is
     * suitable for a network device to transmit.
     * 
     * @return a contiguous array of packet data
     */
    public byte[] transmit()
    {
        /*
         * Only one packet to transmit
         */
        if (packetList.size() == 1)
            return buffer;

        /*
         * The below will iterate through all packets and copy their data into one big
         * buffer. The ordering of the packets in packetList is the order that the data
         * should be sent/transmitted.
         */
        byte data[] = new byte[length];
        int srcIndex, dstIndex = 0;
        Iterator<Packet> packetIter = packetList.iterator();
        while (packetIter.hasNext())
        {
            /*
             * Get packet
             */
            Packet packet = packetIter.next();
            byte packetData[] = packet.getArray();
            for (srcIndex = 0; srcIndex < packetData.length; srcIndex++, dstIndex++)
            {
                data[dstIndex] = packetData[srcIndex];
            }
        }
        return data;
    }

    /**
     * Sets the offset by size bytes
     * 
     * @param size
     */
    public void setHeadroom(int size)
    {
        if (size > offset)
        {
            throw new RuntimeException("Not enought headroom");
        }
        offset -= size;
    }

    public void setCleaner(CleanPacket cleaner)
    {
        this.cleaner = cleaner;
    }

    public void free()
    {

    }

    public int getBufferSize()
    {
        return buffer.length;
    }

    public Address getAddress()
    {
        // TODO Auto-generated method stub
        return bufferAddr;
    }
}
