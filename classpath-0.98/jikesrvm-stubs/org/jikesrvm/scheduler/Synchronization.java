package org.jikesrvm.scheduler;

import org.vmmagic.unboxed.Offset;

public class Synchronization {
    public static boolean tryCompareAndSwap(Object base, Offset offset, int testValue, int newValue) { return false; }
    public static boolean tryCompareAndSwap(Object base, Offset offset, long testValue, int newValue) { return false; }
    public static boolean tryCompareAndSwap(Object base, Offset offset, long testValue, long newValue) { return false; }
    public static boolean tryCompareAndSwap(Object base, Offset offset, Object testValue, Object newValue) { return false; }
    public static void fetchAndStore(Object l, Offset o, int i) {}
    public static boolean testAndSet(Object o, Offset l, int i) { return false; }
}
