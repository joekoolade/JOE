/**
 * Created on Oct 6, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class CommandBlockDescriptor {
  private final static int SIZE = 64;
  private static final boolean DEBUG_CONFIG = true;
  private byte[] buffer;
  private Address bufferAddr;
  private CommandBlockDescriptor next,previous;
  
  private static final byte NOP = 0;
  private static final byte INTERNET_ADDR_SETUP = 1;
  private static final byte CONFIGURE = 2;
  private static final byte MULTICAST_ADDR_SETUP = 3;
  private static final byte TRANSMIT = 4;
  private static final byte LOAD_UCODE = 5;
  private static final byte DUMP = 6;
  private static final byte DIAGNOSE = 7;
  
  private static final byte      INTERRUPT            = 0x20;
  private static final byte      SUSPEND              = 0x40;
  private static final byte      EL                   = (byte) 0x80;

  private static final byte      COMPLETE             = (byte) 0x80;
  private static final byte      OK                   = 0x20;
  
  private static final Offset LINK_OFFSET = Offset.zero().plus(4);
  
  public CommandBlockDescriptor()
  {
    buffer = new byte[SIZE];
    bufferAddr = Magic.objectAsAddress(buffer);
  }
  
  public CommandBlockDescriptor(int size)
  {
    buffer = new byte[size];
    bufferAddr = Magic.objectAsAddress(buffer);
  }
  /**
   * @param commandBlockDescriptor
   */
  public void link(CommandBlockDescriptor commandBlockDescriptor)
  {
    bufferAddr.store(commandBlockDescriptor.bufferAddr, LINK_OFFSET);
  }

  public void suspend()
  {
    buffer[3] |= SUSPEND;
  }
  
  public void unsetSuspend()
  {
    buffer[3] &= ~SUSPEND;
  }
  /**
   * @param configureParameters  
   */
  public void configureParameters(byte[] configureParameters)
  {
    for(int i=0; i < configureParameters.length; i++)
    {
      buffer[i+8] = configureParameters[i];
    }
    buffer[2] = CONFIGURE;
    
    if(DEBUG_CONFIG)
    {
      for(int i=0; i<configureParameters.length; i++)
      {
        VM.sysWrite("config ", i); VM.sysWriteln(" = ", VM.intAsHexString(buffer[i+8]));
      }
    }
  }

  /**
   * @return
   */
  public CommandBlockDescriptor previous()
  {
    return previous;
  }

  /**
   * Set the previous link with command block
   * @param cbd
   */
  public void previous(CommandBlockDescriptor cbd)
  {
    previous = cbd;
  }
  public CommandBlockDescriptor next()
  {
    return next;
  }

  /**
   * Set the next link with the command block
   * @param cbd
   */
  public void next(CommandBlockDescriptor cbd)
  {
    next = cbd;
  }

  /**
   * @return
   */
  public boolean notComplete()
  {
    return (buffer[1] & COMPLETE) == 0;
  }

  /**
   * @return
   */
  public boolean notOk()
  {
    return (buffer[1] & OK) == 0;
  }

  /**
   * @return
   */
  public short getSatus()
  {
    return bufferAddr.loadShort();
  }

  /**
   * @return integer value of command buffer address
   */
  public final int getScbPointer()
  {
    return Magic.objectAsAddress(buffer).toInt();
  }
}
