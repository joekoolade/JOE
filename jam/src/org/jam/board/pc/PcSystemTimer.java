/*
 * Created on Oct 14, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.board.pc;

import org.jam.cpu.intel.IrqHandler;
import org.vmmagic.pragma.InterruptHandler;

/**
 * @author joe
 *
 */
public class PcSystemTimer
implements IrqHandler {

    public I82c54 timer;
    public long tick;                                        // in milliseconds
    public int  sourceFreq     = 1193180;                    // i82c54 source frequency is 1.193180 Mhz
    public int  ticksPerSecond = 1000;
    public int  counterDivisor = sourceFreq / ticksPerSecond;
    public int  overflow;                                    // in nanoseconds
    /*
     * how many ticks to wait to reschedule
     */
    public int  scheduleTick   = 20;

    public PcSystemTimer()
    {
        /*
         * Set the time for the PC system timer. Default is an interrupt every 1.193 ms.
         */
        timer = new I82c54();
        timer.counter0(I82c54.MODE2, counterDivisor);
    }

    public final long getTime()
    {
        return tick;
    }

    /*
     * timer interrupt handler.
     * 
     * context array is builtin into the stack.
     */
    public void handler()
    {
        tick++;
        overflow += 193180;
        if (overflow >= 1000000)
        {
            tick++;
            overflow -= 1000000;
        }
        // Platform.slavePic.eoi();
        // Platform.masterPic.eoi();

        /*
         * skip scheduling if no current thread
         */
        // if(Scheduler.currentThread==null) return;

        /*
         * Process the sleep queue
         */
        // Thread.processSleepQueue();

        /*
         * todo: thread scheduling routine call. every 10-100ms.
         */
        if (tick % scheduleTick == 0)
        {
            // Scheduler.currentThread.saveInterruptContext(context);
            // Thread.scheduler.schedule();
        }

        // if(Thread.newThread) {
        // Thread.newThread = false;
        // Scheduler.currentThread.kernelResume();
        // }
    }
}
