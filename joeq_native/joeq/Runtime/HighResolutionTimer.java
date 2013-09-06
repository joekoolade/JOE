// HighResolutionTimer.java, created Tue Dec 10 14:02:07 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import joeq.Bootstrap.MethodInvocation;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.SystemInterface.ExternalLink;
import joeq.Runtime.SystemInterface.Library;
import jwutil.util.Assert;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: HighResolutionTimer.java,v 1.13 2004/09/30 03:35:35 joewhaley Exp $
 */
public class HighResolutionTimer {
    private static boolean native_library_present;

    static {
        try {
            System.loadLibrary("timer");
            native_library_present = true;
        } catch (Throwable _) {}
    }
    public static native long ticks();
    public static final double TICKS_PER_SECOND = 2e9;
    public static void main(String[] args) throws Exception {
        for (int i=0; i<10; ++i) {
            System.out.println("Now: "+now());
        }
    }

    private static long counter_frequency = 0L;

    public static final void init() {
        if (QueryPerformanceFrequency != null) {
            counter_frequency = query_performance_frequency();
        }
    }

    public static final double now() {
        if (QueryPerformanceCounter != null) {
            if (counter_frequency == 0L) init();
            long l = query_performance_counter();
            return (double)l / counter_frequency;
        } else if (gettimeofday != null) {
            long l = get_time_of_day();
            int sec = (int) (l >> 32);
            int usec = (int) l;
            return ((double)sec) * 1000000 + usec;
        } else if (native_library_present) {
            return ticks() / TICKS_PER_SECOND;
        } else {
            return 0.;
        }
    }

    private static long query_performance_frequency() {
        try {
            CodeAddress a = QueryPerformanceFrequency.resolve();
            StackAddress b = StackAddress.alloca(8);
            Unsafe.pushArgA(b);
            Unsafe.getThreadBlock().disableThreadSwitch();
            byte rc = (byte) Unsafe.invoke(a);
            Unsafe.getThreadBlock().enableThreadSwitch();
            if (rc != 0) {
                // error occurred (?)
            }
            long v = b.peek8();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0L;
    }

    private static long query_performance_counter() {
        try {
            CodeAddress a = QueryPerformanceCounter.resolve();
            StackAddress b = StackAddress.alloca(8);
            Unsafe.pushArgA(b);
            Unsafe.getThreadBlock().disableThreadSwitch();
            byte rc = (byte) Unsafe.invoke(a);
            Unsafe.getThreadBlock().enableThreadSwitch();
            if (rc != 0) {
                // error occurred (?)
            }
            long v = b.peek8();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0L;
    }
    
    private static long get_time_of_day() {
        try {
            CodeAddress a = gettimeofday.resolve();
            StackAddress b = StackAddress.alloca(8);
            Unsafe.pushArgA(HeapAddress.getNull());
            Unsafe.pushArgA(b);
            Unsafe.getThreadBlock().disableThreadSwitch();
            int rc = (int) Unsafe.invoke(a);
            Unsafe.getThreadBlock().enableThreadSwitch();
            if (rc != 0) {
                // error occurred (?)
            }
            long v = b.peek8();
            return v;
        } catch (Throwable t) { Assert.UNREACHABLE(); }
        return 0L;
    }
    
    static ExternalLink QueryPerformanceFrequency;
    static ExternalLink QueryPerformanceCounter;
    static ExternalLink gettimeofday;
    
    static {
        if (jq.RunningNative) boot();
        else if (jq.on_vm_startup != null) {
            jq_Class c = (jq_Class) Reflection.getJQType(HighResolutionTimer.class);
            jq_Method m = c.getDeclaredStaticMethod(new jq_NameAndDesc("boot", "()V"));
            MethodInvocation mi = new MethodInvocation(m, null);
            jq.on_vm_startup.add(mi);
        }
    }
    
    public static void boot() {
        Library kernel32 = SystemInterface.registerLibrary("kernel32");
        Library c = SystemInterface.registerLibrary("libc.so.6");

        if (kernel32 != null) {
            QueryPerformanceFrequency = kernel32.resolve("QueryPerformanceFrequency");
            QueryPerformanceCounter = kernel32.resolve("QueryPerformanceCounter");
        } else {
            QueryPerformanceFrequency = QueryPerformanceCounter = null;
        }

        if (c != null) {
            gettimeofday = c.resolve("gettimeofday");
        } else {
            gettimeofday = null;
        }
    }
}
