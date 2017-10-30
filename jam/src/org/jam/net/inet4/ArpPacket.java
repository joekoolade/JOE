/**
 * Created on Oct 4, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.inet4;

import org.jam.net.ethernet.Ethernet;

/**
 * @author Joe Kulig
 *
 */
public class ArpPacket extends Packet {
  private final static int SIZE = Arp.SIZE + Ethernet.HEADER_SIZE;
  
  public ArpPacket()
  {
    super(SIZE, Ethernet.HEADER_SIZE);
  }
}
