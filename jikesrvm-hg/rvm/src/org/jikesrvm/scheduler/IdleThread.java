/**
 * Created on Apr 14, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;

import static org.jikesrvm.ia32.StackframeLayoutConstants.STACK_SIZE_NORMAL;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Joe Kulig
 *
 */
@NonMoving
public class IdleThread extends SystemThread {

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
   * This is the idling loop when there is nothing else
   * to be done
   */
  @Override
  public void run()
  {
    VM.sysWriteln("Starting the Idle Thread");
    /*
     * For now just pause because we will be doing is spinning until a process becomes available.
     * Later on may want to do some power saving stuff.
     */
    while (true)
    {
      // rvmThread.checkBlock();
      Magic.pause();
    }

  }

}
