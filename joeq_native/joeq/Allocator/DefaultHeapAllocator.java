// DefaultHeapAllocator.java, created Mon Apr  9  1:01:31 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_StaticMethod;
import joeq.Memory.Address;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_NativeThread;

/**
 * Provides access functions to the default heap allocator for the current thread.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: DefaultHeapAllocator.java,v 1.15 2004/08/07 10:00:39 joewhaley Exp $
 */
public abstract class DefaultHeapAllocator {

    /**
     * The default heap allocator for the current thread.
     * 
     * @return default heap allocator for the current thread
     */
    public static final HeapAllocator def() {
        return Unsafe.getThreadBlock().getNativeThread().getHeapAllocator();
    }
    
    /**
     * Initialize the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final void init() throws OutOfMemoryError {
        def().init();
    }
    
    /**
     * Allocate an object with the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final Object allocateObject(int size, Object vtable) throws OutOfMemoryError {
        Unsafe.getThreadBlock().disableThreadSwitch();
        Object o = def().allocateObject(size, vtable);
        Unsafe.getThreadBlock().enableThreadSwitch();
        return o;
    }
    
    /**
     * Allocate an aligned object with the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final Object allocateObjectAlign8(int size, Object vtable) throws OutOfMemoryError {
        Unsafe.getThreadBlock().disableThreadSwitch();
        Object o = def().allocateObjectAlign8(size, vtable);
        Unsafe.getThreadBlock().enableThreadSwitch();
        return o;
    }
    
    /**
     * Allocate an array with the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final Object allocateArray(int length, int size, Object vtable)
    throws OutOfMemoryError, NegativeArraySizeException {
        Unsafe.getThreadBlock().disableThreadSwitch();
        Object o = def().allocateArray(length, size, vtable);
        Unsafe.getThreadBlock().enableThreadSwitch();
        return o;
    }
    
    /**
     * Allocate an aligned array with the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final Object allocateArrayAlign8(int length, int size, Object vtable)
    throws OutOfMemoryError, NegativeArraySizeException {
        Unsafe.getThreadBlock().disableThreadSwitch();
        Object o = def().allocateArrayAlign8(length, size, vtable);
        Unsafe.getThreadBlock().enableThreadSwitch();
        return o;
    }
    
    /**
     * Return the amount of free memory in the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final int freeMemory() { return def().freeMemory(); }
    
    /**
     * Return the total amount of memory us the default heap allocator for the current thread.
     * 
     * @throws OutOfMemoryError
     */
    public static final int totalMemory() { return def().totalMemory(); }

    public static final void collect() {
        Unsafe.getThreadBlock().disableThreadSwitch();
        def().collect();
        Unsafe.getThreadBlock().enableThreadSwitch();
    }
    
    public static final boolean isValidHeapAddress(Address a) {
        if (HeapAllocator.isInDataSegment(a)) return true;
        if (jq_NativeThread.allNativeThreadsInitialized()) {
            for (int i = 0; i < jq_NativeThread.native_threads.length; ++i) {
                jq_NativeThread nt = jq_NativeThread.native_threads[i];
                if (nt.getHeapAllocator().isInHeap(a)) return true;
            }
        } else {
            jq_NativeThread nt = Unsafe.getThreadBlock().getNativeThread();
            if (nt.getHeapAllocator().isInHeap(a)) return true;
        }
        return false;
    }
    
    public static final void processObjectReference(Address a) {
        def().processObjectReference(a);
    }

    public static final void processPossibleObjectReference(Address a) {
        def().processPossibleObjectReference(a);
    }
    
    public static final jq_StaticMethod _allocateObject;
    public static final jq_StaticMethod _allocateObjectAlign8;
    public static final jq_StaticMethod _allocateArray;
    public static final jq_StaticMethod _allocateArrayAlign8;
    static {
        jq_Class k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Allocator/DefaultHeapAllocator;");
        _allocateObject = k.getOrCreateStaticMethod("allocateObject", "(ILjava/lang/Object;)Ljava/lang/Object;");
        _allocateObjectAlign8 = k.getOrCreateStaticMethod("allocateObjectAlign8", "(ILjava/lang/Object;)Ljava/lang/Object;");
        _allocateArray = k.getOrCreateStaticMethod("allocateArray", "(IILjava/lang/Object;)Ljava/lang/Object;");
        _allocateArrayAlign8 = k.getOrCreateStaticMethod("allocateArrayAlign8", "(IILjava/lang/Object;)Ljava/lang/Object;");
    }
}
