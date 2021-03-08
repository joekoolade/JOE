package org.jikesrvm.mm.mminterface;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.vmmagic.unboxed.Extent;

import java.lang.ref.PhantomReference;

public class MemoryManager {
    public static void addWeakReference(WeakReference<?> obj, Object referent) {}
    public static void addSoftReference(SoftReference<?> obj, Object referent) {}
    public static void addPhantomReference(PhantomReference<?> obj, Object referent) {}
    public static boolean willNeverMove(byte[] b) { return false; }
    public static void gc() {}
    public static Extent maxMemory() { return null; }
    public static Extent totalMemory() { return null; }
    public static Extent freeMemory() { return null; }
}
