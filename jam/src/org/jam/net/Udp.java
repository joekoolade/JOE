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
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.jam.driver.net.Packet;

/**
 * @author Joe Kulig
 *
 */
/**
 * @author Joe Kulig
 *
 */
public class Udp {
	InetSocketAddress localAddress;
	InetSocketAddress remoteAddress;
	int ttl;
	private Connection connection;
	private int pseudoHeaderSum;
	private int offset;
	private InetPacket packet;
	
  /**
   * @param inetSocketAddress
   */
  public void bind(InetSocketAddress inetSocketAddress) throws SocketException, IOException
  {
	  localAddress = inetSocketAddress;
  }

  /**
   * @param inetSocketAddress
   * @param i
   */
  public void connect(InetSocketAddress inetSocketAddress, int i)
  {
    remoteAddress = inetSocketAddress;
    // check if address is routable
    // Create a new connection
  }

  /**
   * @param ttl
   */
  public void setTimeToLive(int ttl)
  {
	  this.ttl = ttl;
  }

  /**
   * @return
   */
  public int getTimeToLive()
  {
    return ttl;
  }

  /**
   * @return
   */
  public InetSocketAddress getLocalAddress() throws IOException
  {
    return localAddress;
  }

  /**
   * @param packet
 * @throws IOException 
   */
  public void send(DatagramPacket packet) throws IOException
  {
	  /*
	   * Get a new connection
	   */
	  if(connection == null)
	  {
		  connection = new Connection(localAddress, remoteAddress, IpProto.UDP);
		  computePseudoHeaderSum();
	  }
	  if(packet.getLength() > 0xFFFF)
	  {
		  throw new IOException("Packet too big");
	  }
	  
  }

  /**
   * The is the 16 bit sum of the source, destination, and protocol
   * values
   */
  private void computePseudoHeaderSum() {
	  // Sum up the source IP address
	  byte[] inetAddressBytes = localAddress.getAddress().getAddress();
	  pseudoHeaderSum = (inetAddressBytes[0]<<8) + inetAddressBytes[1];
	  pseudoHeaderSum += (inetAddressBytes[2]<<8) + inetAddressBytes[3];
	  // Add in the destination IP address
	  inetAddressBytes = remoteAddress.getAddress().getAddress();
	  pseudoHeaderSum = (inetAddressBytes[0]<<8) + inetAddressBytes[1];
	  pseudoHeaderSum += (inetAddressBytes[2]<<8) + inetAddressBytes[3];
	  // Add in the protocol
	  pseudoHeaderSum += IpProto.UDP.ordinal();
	  // Add any carry overs
	  int carryOver = (pseudoHeaderSum>>16) & 0xFFFF;
	  pseudoHeaderSum += carryOver;
  }

  private int computeChecksum()
  {
	  int csum = 0;
	  
	  return csum;
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
