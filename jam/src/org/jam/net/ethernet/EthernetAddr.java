/**
 * Created on Jul 25, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.ethernet;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class EthernetAddr {
  private final int ETH_LEN = 6;
  private final byte addr[];
  public final static EthernetAddr BROADCAST_ADDRESS = new EthernetAddr((byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff);

  public EthernetAddr(byte addr[])
  {
    this.addr = addr;
  }
  
  public EthernetAddr(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5)
  {
    addr = new byte[ETH_LEN];
    addr[0] = b0;
    addr[1] = b1;
    addr[2] = b2;
    addr[3] = b3;
    addr[4] = b4;
    addr[5] = b5;
  }
  
  public Address asAddress()
  {
    return Magic.objectAsAddress(addr);
  }
  
  public byte[]  asArray()
  {
    return addr;
  }
}
