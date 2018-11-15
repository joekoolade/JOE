/**
 * Created on Oct 5, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.utilities;

/**
 * @author Joe Kulig
 *
 */
public class HexChar {
  private final static char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                           'A', 'B', 'C', 'D', 'E', 'F'
  };
  
  /**
   * @param i
   * @return
   */
  public static char getChar(int i)
  {
    return hexChars[i];
  }

}
