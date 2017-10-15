/**
 * Created on Jul 24, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.ethernet;

import org.jam.net.ByteOrder;
import org.jam.net.inet4.Packet;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 * Implements the ethernet framing. Takes data and creates an ethernet packet that 
 * can be transmitted by a ethernet device
 */
public class Ethernet {
  public final static short  PROTO_IP            = 0x800;
  public final static short  PROTO_ARP           = 0x806;
  private final static int   MAX_PAYLOAD         = 1500;
  private final static int   MIN_PAYLOAD         = 46;
  public final static int    FRAME_LENGTH        = 1518;   // does not include FCS
  public final static int    FCS_LENGTH          = 4;
  private final static int   MIN_ETHERNET_PACKET = 64;
  public final static int    HEADER_SIZE         = 14;
  
  final private EthernetAddr src;
  final private EthernetAddr dst;
  final private short type;
  private byte[] frame;
 
  public Ethernet(EthernetAddr src, EthernetAddr dst, short type, byte payload[])
  {
    this.src = src;
    this.dst = dst;
    this.type = type;
    send(payload);
  }
  
  public void send(byte payload[])
  {
    int srcIndex, targetIndex=0;
    if(payload.length < MIN_PAYLOAD)
    {
      frame = new byte[MIN_ETHERNET_PACKET];
    }
    else
    {
      frame = new byte[payload.length + 18];
    }
    
    // Copy destination address
    byte[] ethAddress = dst.asArray();
    for(srcIndex=0, targetIndex=0; srcIndex < ethAddress.length; srcIndex++, targetIndex++)
    {
      frame[targetIndex] = ethAddress[srcIndex];
    }
    
    // copy source address
    ethAddress = src.asArray();
    for(srcIndex=0, targetIndex=6; srcIndex < ethAddress.length; srcIndex++, targetIndex++)
    {
      frame[targetIndex] = ethAddress[srcIndex];
    }
    
    // copy ethernet type
    Address dataAddr = Magic.objectAsAddress(frame);
    dataAddr.store(ByteOrder.hostToNetwork(type), Offset.zero().plus(12));
    
    // copy the data/payload
    for(srcIndex=0, targetIndex=14; srcIndex < payload.length; srcIndex++, targetIndex++)
    {
      frame[targetIndex] = payload[srcIndex];
    }
    
    // zero out the rest
    for( ; targetIndex < frame.length; targetIndex++)
    {
      frame[targetIndex] = 0;
    }
  }
  
  public byte[] getFrame()
  {
    return frame;
  }
}
