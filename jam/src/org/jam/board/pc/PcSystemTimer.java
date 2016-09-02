/*
 * Created on Oct 14, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.board.pc;

import java.util.TreeMap;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.unboxed.Address;

/**
 * @author joe
 *
 */
public class PcSystemTimer
{

    public I82c54 timer;
    public long tick;                                        // in milliseconds
    public static final int  sourceFreq     = 1193180;                    // i82c54 source frequency is 1.193180 Mhz
    public static final int  ticksPerSecond = 1000;
    public int  counterDivisor = sourceFreq / ticksPerSecond;
    public int  overflow;                                    // in nanoseconds
    
    private int stack[];
    Address stackTop;
    private final static int STACK_SIZE = 512;
    private static final int TICKSPERNSECS = 1000000;
    private TreeMap<Long, RVMThread> timerQueue;
    
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
        timerQueue = new TreeMap<Long, RVMThread>();
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

        checkTimers();
        schedule();
        Platform.masterPic.eoi();
    }
    
    final private void schedule()
    {
        // do nothing for now
    }
    
    private void checkTimers()
    {
        if(timerQueue.isEmpty())
        {
            return;
        }
        Long timerExpiration = timerQueue.firstKey();
        
        if(tick < timerExpiration)
        {
            return;
        }
        
        /*
         * Remove the thread from the timer and schedule it
         */
        VM.sysWriteln("Timer expired!");
        RVMThread thread = timerQueue.remove(timerExpiration);
        VM.sysWriteln("Running thread: ", Magic.objectAsAddress(thread));
        Platform.scheduler.addThread(thread);
    }
    
    /**
     * Set timer for a thread
     * @param time_ns time to sleep in nanoseconds.
     */
    public void startTimer(long time_ns)
    {
        long timerTicks;
        
        /*
         * convert to ticks (milliseconds)
         */
        timerTicks = time_ns / TICKSPERNSECS;
        /*
         * set expiration time and put on the queue
         */
        timerQueue.put(timerTicks+tick, RVMThread.getCurrentThread());
        /*
         * schedule a new thread
         */
        Platform.scheduler.nextThread();
        /*
         * give it up
         */
        Magic.yield();
    }
}
