/*
 * Created on Oct 14, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.board.pc;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author joe
 *
 */
public class PcSystemTimer
{

    public I82c54 timer;
    public long tick;                                        // in milliseconds
    public int  sourceFreq     = 1193180;                    // i82c54 source frequency is 1.193180 Mhz
    public int  ticksPerSecond = 1000;
    public int  counterDivisor = sourceFreq / ticksPerSecond;
    public int  overflow;                                    // in nanoseconds
    
    private int stack[];
    Address stackTop;
    private final static int STACK_SIZE = 512;
    
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
        /*
         * Allocate irq handler stack
         */
        stack = new int[STACK_SIZE];
        /*
         * Put in the sentinel
         */
        stack[STACK_SIZE-1] = 0;    // IP = 0
        stack[STACK_SIZE-2] = 0;    // FP = 0
        stack[STACK_SIZE-3] = 0;    // cmid = 0
        
        /*
         * On a stack switch, the new stack is popped so need to count for this
         * in the stackTop field. This space will contain the interrupted thread's
         * stack pointer.
         */
        stackTop = Magic.objectAsAddress(stack[0]).plus((STACK_SIZE-4)<<2);
    }

    public final long getTime()
    {
        return tick;
    }

    public Address getHandlerStack()
    {
        
        return stackTop;
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

         Platform.masterPic.eoi();
    }
}
