/**
 * Created on Aug 2, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net;

import org.jikesrvm.runtime.Magic;

/**
 * @author Joe Kulig
 *
 */
public class ByteOrder {
  static public short networkToHost(short value)
  {
    return Magic.byteSwap(value);
  }
  static public int networkToHost(int value)
  {
    return Magic.byteSwap(value);
  }
  static public short hostToNetwork(short value)
  {
    return Magic.byteSwap(value);
  }
  static public int hostToNetwork(int value)
  {
    return Magic.byteSwap(value);
  }
}
