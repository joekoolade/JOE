/**
 * 
 */
package org.jam.system;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.Untraced;

/**
 * @author Joe Kulig
 * Copyright 2021
 *
 */
@NonMoving
public class Trace
{
    public static int MAX_EVENTS = 256;    // must be a power of 2
    private static int head;
    @Untraced
    private static Event events[];
    
    public static void init()
    {
        int i;
        events = new Event[MAX_EVENTS];
        for(i=0; i < MAX_EVENTS; i++)
        {
            events[i] = new Event(0);
        }
        head = 0;
    }
    
    private static void next()
    {
        head = (head+1) & (MAX_EVENTS - 1);
 
    }
    public static void add(Event event)
    {
        events[head] = event;
        next();
    }
    
    /**
     * Add trace event for beginning of irq
     * @param irqNum
     */
    @Uninterruptible
    public static void irqStart(int irqNum)
    {
//        events[head].setTime();
//        events[head].irqType();
//        events[head].setParameter(0, 1);
//        events[head].setParameter(1, irqNum);
//        events[head].setParameter(2, Magic.objectAsAddress(Magic.getThreadRegister()).toLong());
//        next();
    }
    /**
     * Add trace event for end of irq
     * @param irqNum
     */
    @Uninterruptible
    public static void irqEnd(int irqNum)
    {
//        events[head].setTime();
//        events[head].irqType();
//        events[head].setParameter(0, 2);
//        events[head].setParameter(1, irqNum);
//        events[head].setParameter(2, Magic.objectAsAddress(Magic.getThreadRegister()).toLong());
//        next();
    }
    /**
     * Add trace event for scheduling event
     */
    public static void schedule()
    {
        events[head].setTime();
        events[head].scheduleType();
        events[head].setParameter(0, Magic.objectAsAddress(Magic.getThreadRegister()).toLong());
        next();
    }
    
    /**
     * Add a software trace event
     * @param params sw event parameters
     */
    public static void sw(long[] params)
    {
        Magic.disableInterrupts();
        events[head].setTime();
        events[head].swType();
        events[head].setParameter(params);
        next();
        Magic.enableInterrupts();
    }
    
    public static void printLog()
    {
        int i = 0;
        int eventIndex = (head + 1) & (MAX_EVENTS-1);
        for( ; i < MAX_EVENTS; i++)
        {
            VM.sysWriteln(events[eventIndex].toString());
            eventIndex = (eventIndex + 1) & (MAX_EVENTS-1);
//            i = (i+1) & (MAX_EVENTS - 1);
        }
    }
}
