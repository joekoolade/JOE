/**
 * Created on Dec 27, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @author Joe Kulig
 *
 */
/**
 * @author Joe Kulig
 *
 */
public class Udp {

  
  /**
   * @param inetSocketAddress
   */
  public void bind(InetSocketAddress inetSocketAddress) throws SocketException, IOException
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param inetSocketAddress
   * @param i
   */
  public void connect(InetSocketAddress inetSocketAddress, int i)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param ttl
   */
  public void setTimeToLive(int ttl)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @return
   */
  public int getTimeToLive()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @return
   */
  public InetSocketAddress getLocalAddress() throws IOException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param packet
   */
  public void send(DatagramPacket packet) throws InterruptedIOException
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param packet
   * @return
   */
  public SocketAddress receive(DatagramPacket packet) throws SocketTimeoutException,InterruptedIOException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param optionId
   * @param value
   */
  public void setMulticastInterface(int optionId, InetAddress value)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param optionId
   * @param value
   */
  public void setOption(int optionId, Object value)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param optionId
   * @return
   */
  public Object getMulticastInterface(int optionId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param optionId
   * @return
   */
  public Object getOption(int optionId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * 
   */
  public void close() throws IOException
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param addr
   */
  public void join(InetAddress addr)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param addr
   */
  public void leave(InetAddress addr)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param address
   * @param netIf
   */
  public void joinGroup(InetSocketAddress address, NetworkInterface netIf)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param address
   * @param netIf
   */
  public void leaveGroup(InetSocketAddress address, NetworkInterface netIf)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  public void shutdownInput()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  public void shutdownOutput()
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param data
   */
  public void sendUrgentData(int data)
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param queuelen
   */
  public void listen(int queuelen)
  {
    // TODO Auto-generated method stub
    
  }

}
