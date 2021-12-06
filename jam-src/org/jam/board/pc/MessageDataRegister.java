/**
 * Created on Aug 26, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.board.pc;

/**
 * @author Joe Kulig
 *
 */
/**
 *  Intel message data register, 10.11.2
 *
 */
public class MessageDataRegister {
  final private int vector;
  final private TriggerMode triggerMode;
  final private Level level;
  final private DeliveryMode delivery;

  private MessageDataRegister(TriggerMode trigger, DeliveryMode delivery, Level level, int vector)
  {
    triggerMode = trigger;
    this.level = level;
    this.vector = vector;
    this.delivery = delivery;
  }
  
  public static MessageDataRegister fixedEdgeVector(int vector)
  {
    return new MessageDataRegister(TriggerMode.EDGE, DeliveryMode.FIXED, Level.DEASSERT, vector);
  }
  
  public static MessageDataRegister lowPriorityEdgeVector(int vector)
  {
    return new MessageDataRegister(TriggerMode.EDGE, DeliveryMode.LOWEST_PRIORITY, Level.DEASSERT, vector);
  }
  
  public int toRegister()
  {
    int registerValue=0;
    registerValue = (triggerMode.ordinal()<<15) | (level.ordinal()<<14) | (delivery.value()<<8) | vector;
    return registerValue;
  }
}
