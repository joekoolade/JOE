/**
 * Created on Jul 22, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.inet4;

/**
 * @author Joe Kulig
 *
 */
public class InetAddress {
  private static final int BROADCAST_ADDR_INT = 0xFFFFFFFF;
  final private byte addrBytes[];
  private String string=null;
  final private int addrInt;
  
  /*
   * Create ip from an integer
   */
  public InetAddress(int addr)
  {
    addrInt = addr;
    addrBytes = new byte[4];
    addrBytes[0] = (byte) ((addr>>24) & 0xFF);
    addrBytes[1] = (byte) ((addr>>16) & 0xFF);
    addrBytes[2] = (byte) ((addr>>8) & 0xFF);
    addrBytes[3] = (byte) (addr & 0xFF);
  }
  
  /*
   * Create ip from four octets
   */
  public InetAddress(int octet1, int octet2, int octet3, int octet4)
  {
    addrBytes = new byte[4];
    addrBytes[0] = (byte) octet1;
    addrBytes[1] = (byte) octet2;
    addrBytes[2] = (byte) octet3;
    addrBytes[3] = (byte) octet4;
    addrInt = (octet1<<24) | (octet2<<16) | (octet3<<8) | octet4;
  }
  
  public boolean isBroadcast()
  {
    return addrInt == BROADCAST_ADDR_INT;
  }
  
  public byte[] asArray()
  {
    return addrBytes;
  }
  public String toString()
  {
    return string;
  }
}
