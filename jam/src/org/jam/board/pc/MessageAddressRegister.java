/**
 * Created on Aug 24, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

/**
 * @author Joe Kulig
 *
 */
public class MessageAddressRegister {

  final private int destinationId;
  final private RedirectionHint redirectionHint;
  final private DestinationMode destinationMode;
  
  private MessageAddressRegister(RedirectionHint hint, DestinationMode mode, int destination)
  {
    redirectionHint = hint;
    destinationMode = mode;
    destinationId = destination;
  }
  
  public static MessageAddressRegister logicalDestination(int destination)
  {
    return new MessageAddressRegister(RedirectionHint.ON, DestinationMode.LOGICAL, destination);
  }
  
  public static MessageAddressRegister physicalDestination(int destination)
  {
    return new MessageAddressRegister(RedirectionHint.ON, DestinationMode.PHYSICAL, destination);
  }
  
  public int toRegister()
  {
    int registerValue = 0xFEE00000;
    
    registerValue |= (destinationId<<12) | (redirectionHint.ordinal()<<3) | (destinationMode.ordinal()<<2);
    return registerValue;
  }
}
