package org.jam.system;

import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.Untraced;

@NonMoving
public class Event
{
    final private static int NUM_PARAMS = 4;
    private long time;
    @Untraced
    private long params[];
    private int type;

    private static int NONE = 0;
    private static int IRQ = 1;
    private static int SCHEDULE = 2;
    private static int SW = 3;
    
    public Event(int type)
    {
        params = new long[NUM_PARAMS];
        this.type = type;
    }
    
    @Uninterruptible
    public void setTime()
    {
        time = Magic.getTimeBase();
    }
    
    @Uninterruptible
    public void setParameter(int index, long val)
    {
        params[index] = val;
    }
    
    @Uninterruptible
    public long getParameter(int index)
    {
        return params[index];
    }
    
    @Uninterruptible
    public void setParameter(long[] params)
    {
        int i=0;
        for(i=0; i < NUM_PARAMS; i++)
        {
            this.params[i] = params[i];
        }
    }
    @Uninterruptible
    public void irqType()
    {
        type = IRQ;
    }
    
    @Uninterruptible
    public void scheduleType()
    {
        type = SCHEDULE;
    }
    
    @Uninterruptible
    public void swType()
    {
        type = SW;
    }
    
    public String toString()
    {
        return time + " " + type + " " + params[0] + " " + params[1] + " "
        + Long.toHexString(params[2]) + " " + params[3];
    }
}
