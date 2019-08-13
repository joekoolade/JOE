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
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.Time;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.ThreadQueue;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.unboxed.Address;
import org.jam.interfaces.Timer;

/**
 * @author joe
 *
 */
@NonMoving
public class PcSystemTimer
implements Timer
{

    public I82c54 timer;
    public long tick;                                        // in milliseconds
    private static final int  sourceFreq     = 1193180;                    // i82c54 source frequency is 1.193180 Mhz
    private static final int  ticksPerSecond = 1000;
    public int  counterDivisor = sourceFreq / ticksPerSecond;
    public int  overflow;                                    // in nanoseconds
    public int BOLT = 10;   // schedule new process
    private int stack[];
    Address stackTop;
    private final static int STACK_SIZE = 512;
    private static final int TIMERTICKSPERNSECS = 1000000;
    private static final boolean trace = false;
    private TreeMap<Long, RVMThread> timerQueue;
    private ThreadQueue threadQueue;
    
    /*
     * how many ticks to wait to reschedule
     */
    public int  scheduleTick = 20;

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
        stack = MemoryManager.newNonMovingIntArray(STACK_SIZE); // new int[STACK_SIZE];
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
        stackTop = Magic.objectAsAddress(stack).plus((STACK_SIZE-4)<<2);
        timerQueue = new TreeMap<Long, RVMThread>();
        threadQueue = new ThreadQueue();
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

        if (RVMThread.bootThread.isTerminated()==false) return;
        checkTimers();
        schedule();
//        Platform.masterPic.eoi();
    }
    
    final private void schedule()
    {
        /*
         * If there are no threads waiting run then
         * just keep running the current one
         */
        if(Platform.scheduler.noRunnableThreads())
        {
            return;
        }
        /*
         * We took a timer tick and there are threads that are runnable.
         */
        RVMThread currentThread = Magic.getThreadRegister();
        
        /*
         * Current thread has had its time allotment so put it on queue 
         * and schedule a new thread
         */
        if(((int)tick % BOLT) == 0)
        {
            Platform.scheduler.addThread(currentThread);
            Platform.scheduler.nextThread();
        }
    }
    
    private void checkTimers()
    {
        if(timerQueue.isEmpty())
        {
            return;
        }
        Long timerExpiration = timerQueue.firstKey();
        
        if(Time.nanoTime() < timerExpiration)
        {
            return;
        }
        
        /*
         * Remove the thread from the timer and schedule it
         */
        if(trace) VM.sysWriteln("Timer expired! ", timerExpiration);
        RVMThread thread = timerQueue.remove(timerExpiration);
        threadQueue.remove(thread);
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
//        timerTicks = time_ns / TIMERTICKSPERNSECS;
        if(trace)
        {
//          VM.sysWriteln("timer ticks: ", timerTicks);
          VM.sysWriteln("time_ns: ", time_ns);
//          VM.sysWriteln("expire: ", timerTicks+tick);
        }
        /*
         * set expiration time and put on the queue
         */
//        timerQueue.put(timerTicks+tick, RVMThread.getCurrentThread());
        timerQueue.put(time_ns, RVMThread.getCurrentThread());
        threadQueue.enqueue(RVMThread.getCurrentThread());
        /*
         * give it up and schedule a new thread
         */
        Magic.yield();
    }
    
    /**
     * Remove timer from queue and return thread
     * @param timeKey
     * @return
     */
    public RVMThread removeTimer(long timeKey)
    {
      return timerQueue.remove(timeKey);
    }
    
    /**
     * Remove timer associated with thread
     */
    public void removeTimer(RVMThread thr)
    {
      
    }
}
