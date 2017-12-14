/**
 * Created on Nov 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public interface Packet {
  byte[] getArray();
  Address getAddress();
  int getOffset();
  int getSize();
  void append(Packet packet);
  Address prepend(int size);
  void prepend(Packet packet);
  void setHeadroom(int size);
}
