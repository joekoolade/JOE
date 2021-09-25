/**
 * Created on Apr 14, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class IdleThread extends SystemThread
{
  private static int loop = 0;

  /**
     * 
     */
  public IdleThread()
  {
    super(MemoryManager.newStack(4096), "IdleThread");
    RVMThread.idleThread = this.rvmThread;
    VM.sysWriteln("idle thread: ", ObjectReference.fromObject(RVMThread.idleThread));
  }

  /*
   * This is the idling loop when there is nothing else to be done
   */
  @Override
  public void run()
  {
    VM.sysWriteln("Starting the Idle Thread");
    /*
     * For now just pause because we will be doing is spinning until a process
     * becomes available. Later on may want to do some power saving stuff.
     */
    int idling = 0;
    while (true)
    {
      // rvmThread.checkBlock();
      try
      {
        RVMThread.sleep(1000, 0);
      } catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      idling++;
      VM.sysWriteln("IDLING ... ");
    }

  }

  private void doSomething()
  {
    loop++;
    if ((loop % 100000) == 0)
    {
      VM.sysWrite("$%");
    }
  }
}
