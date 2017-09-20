/**
 * Created on Aug 30, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.net.ethernet.Ethernet;

/**
 * @author Joe Kulig
 *
 */
public class ReceiveFrameDescriptor {
  private static final int EL              = (1 << 31);
  private static final int SUSPEND         = (1 << 30);
  private static final int HEADER          = (1 << 20);
  private static final int SIMPLIFIED_MODE = (1 << 19);
  private static final int COMPLETE        = (1 << 15);
  private static final int OK              = (1 << 13);
  private static final int EOF             = (1 << 15);
  private static final int FINISHED        = (1 << 14);
  private static final int RFD_SIZE        = 16;
  
  private byte buffer[];
  
  public ReceiveFrameDescriptor(int bufferSize)
  {
    buffer = new byte[bufferSize];
  }
  
  public ReceiveFrameDescriptor()
  {
    this(Ethernet.FRAME_LENGTH + Ethernet.FCS_LENGTH + RFD_SIZE);
  }
  
  /**
   * Set the link address field
   * @param linkAddress
   */
  public void setLinkAddress()
  {
    
  }

  /**
   * @param receiveFrameDescriptor
   */
  public void link(ReceiveFrameDescriptor receiveFrameDescriptor)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  public void endLink()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param i
   */
  public void size(int i)
  {
    // TODO Auto-generated method stub
    
  }
}
