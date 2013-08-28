/*
 * Created on Oct 14, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.platform;

import baremetal.devices.I82c54;
import baremetal.kernel.IrqHandler;
import baremetal.kernel.Scheduler;
import baremetal.vm.Thread;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SystemTimer extends I82c54 
implements IrqHandler {
  public static long tick; // in milliseconds
  public static int sourceFreq = 1193180;
  public static int ticksPerSecond = 1000;
  public static int counterDivisor =  sourceFreq/ticksPerSecond;
  public static int overflow; // in nanoseconds
  /*
   * how many ticks to wait to reschedule
   */
  public static int scheduleTick = 20;
  
  public SystemTimer() {
    super(0x40, 0x41, 0x42, 0x43);
    /*
     * Set the time for the PC system timer.
     * Default is an interrupt every 1.193 ms.     
     */
    counter0(MODE2, counterDivisor);
  }

  public final long getTime() {
    return tick;
  }
  
  /*
   * timer interrupt handler.
   * 
   * context array is builtin into the stack.
   */
  public void handler(int[] context) {
    tick++;
    overflow += 193180;
    if(overflow>=1000000) {
      tick++;
      overflow -= 1000000;
    }
    Platform.slavePic.eoi();
    Platform.masterPic.eoi();
    
    /*
     * skip scheduling if no current thread
     */
    if(Scheduler.currentThread==null) return;
    
    /*
     * Process the sleep queue
     */
    Thread.processSleepQueue();

    /*
     * todo: thread scheduling routine call. every 10-100ms.
     */
    if(tick % scheduleTick == 0) {
      Scheduler.currentThread.saveInterruptContext(context);
      Thread.scheduler.schedule();
    }
    
    if(Thread.newThread) {
      Thread.newThread = false;
      Scheduler.currentThread.kernelResume();
    }
  }
}
