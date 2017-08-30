/**
 * Created on Jul 24, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.inet4;

import java.util.Iterator;
import java.util.LinkedList;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class Packet {

  private byte buffer[];
  private int length;
  private Address bufferAddr;
  
  /*
   * Used to prepend and append other packets
   */
  private LinkedList<Packet> packetList;
  
  /*
   * Create packet from an array
   */
  public Packet(byte[] buffer)
  {
    this.buffer = buffer;
    length = buffer.length;
    bufferAddr = Magic.objectAsAddress(buffer);
    initPacketList();
  }
  
  private final void initPacketList()
  {
    packetList = new LinkedList<Packet>();
    packetList.add(this);
  }
  
  public Packet(int length)
  {
    buffer = new byte[length];
    this.length = length;
    bufferAddr = Magic.objectAsAddress(buffer);
    initPacketList();
  }
  
  public byte[] getArray()
  {
    return buffer;
  }
  
  public Address getAddress()
  {
    return bufferAddr;
  }
  
  public int getSize()
  {
    return length;
  }
  
  public void append(Packet packet)
  {
    packetList.add(packet);
    length += packet.length;
  }
  
  public void prepend(Packet packet)
  {
    packetList.addFirst(packet);
    length += packet.length;
  }
  
  /**
   * Creates a new contiguous buffer that contains all packet data.
   * This is suitable for a network device to transmit.
   * @return a contiguous array of packet data
   */
  public byte[] transmit()
  {
    /*
     * Only one packet to transmit
     */
    if(packetList.size()==1) return buffer;
    
    /*
     * The below will iterate through all packets and copy
     * their data into one big buffer. The ordering of the packets in
     * packetList is the order that the data should be sent/transmitted.
     */
    byte data[] = new byte[length];
    int srcIndex, dstIndex=0;
    Iterator<Packet> packetIter = packetList.iterator();
    while(packetIter.hasNext())
    {
      /*
       * Get packet
       */
      Packet packet = packetIter.next();
      byte packetData[] = packet.getArray();
      for(srcIndex=0; srcIndex < packetData.length; srcIndex++, dstIndex++)
      {
        data[dstIndex] = packetData[srcIndex];
      }
    }
    return data;
  }
}
