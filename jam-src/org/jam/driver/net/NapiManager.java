/**
 * Created on Nov 14, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

import org.jam.util.Iterator;

import org.jam.util.LinkedList;
import org.jikesrvm.VM;

/**
 * @author Joe Kulig
 *
 *         Network interface will register themselves to this manager
 *         which will periodically poll them for packets
 */
public class NapiManager implements Runnable {
  final static class NapiEntry {
    NapiInterface netIf;
    int           timer;

    public NapiEntry(NapiInterface netIf, int timer)
    {
      this.netIf = netIf;
      this.timer = timer;
    }
  }

  private static final boolean DEBUG = false;

  private static LinkedList<NapiEntry> networkInterfaces = new LinkedList<NapiEntry>();

  /**
   * @param i82559c
   */
  public static void addInterface(NapiInterface networkCard)
  {
    if (networkInterfaces.isEmpty())
    {
      /*
       * This is the easy one. Just add it as is. No adjustments need to be made.
       */
      networkInterfaces.add(new NapiEntry(networkCard, networkCard.schedule()));
      if(DEBUG) VM.sysWriteln("napimgr.addInterface: first", networkCard.schedule());
    }
    else
    {
      NapiEntry previous = null;
      Iterator<NapiEntry> iter = networkInterfaces.iterator();
      int slot = 0;
      int timer = 0;
      while (iter.hasNext())
      {
        NapiEntry entry = iter.next();
        if (networkCard.schedule() <= entry.netIf.schedule())
        {
          // calculate the timer for the network card using the preceding entry
          if (previous != null)
          {
            timer = networkCard.schedule() - previous.netIf.schedule();
          }
          else
          {
            // netif is being added to the beginning
            timer = networkCard.schedule();
          }
          // adjust timer for current entry
          entry.timer -= timer;
          // insert before current entry
          if(DEBUG) 
          {
            VM.sysWrite("napimgr.addInterface: ", slot);
            VM.sysWriteln(" ", timer);
          }
          networkInterfaces.add(slot, new NapiEntry(networkCard, timer));
          return;
        }
        else
        {
          previous = entry;
        }
        slot++;
      }
      /*
       * netif is being added to the end
       */
      timer = networkCard.schedule() - previous.netIf.schedule();
      if(DEBUG) VM.sysWriteln("napimgr.addInterface: last", timer);
      networkInterfaces.add(new NapiEntry(networkCard, timer));
    }
  }

  public static void remove(NapiInterface netInterface)
  {
    networkInterfaces.remove(netInterface);
  }

  /**
   * Startup the NAPI timer thread
   */
  public static void boot()
  {

  }

  private int interfaceSlot;

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    interfaceSlot = 0;
    int debugTimer=0;
    NapiEntry networkInterface = null;
    VM.sysWriteln("Starting NAPI manager");
    while (true)
    {
      try
      {
        /*
         * Sleep if the queue is empty
         */
        if (networkInterfaces.isEmpty())
        {
          interfaceSlot = 0;
          Thread.sleep(1000);
        }
        else
        {
          networkInterface = networkInterfaces.get(interfaceSlot);
          Thread.sleep(networkInterface.timer);
          networkInterface.netIf.poll();
          if(DEBUG && ((++debugTimer%100)==0)) VM.sysWrite('*');
          int size = networkInterfaces.size();
          interfaceSlot++;
          interfaceSlot = interfaceSlot % size;
        }
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
