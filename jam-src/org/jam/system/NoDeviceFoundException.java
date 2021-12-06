/**
 * Created on Mar 8, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.system;

/**
 * @author Joe Kulig
 *
 */
public class NoDeviceFoundException extends Exception {
  public NoDeviceFoundException()
  {
    super();
  }
  public NoDeviceFoundException(String message)
  {
    super(message);
  }
}
