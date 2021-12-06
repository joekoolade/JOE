/**
 * Created on Nov 28, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

/**
 * @author Joe Kulig
 *
 */
public interface BufferFree {
  void free(Packet packet);
}
