// SemiConservative.java, created Aug 3, 2004 4:18:21 AM by joewhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_NativeThread;
import joeq.Scheduler.jq_RegisterState;
import joeq.Scheduler.jq_Thread;
import joeq.Scheduler.jq_ThreadQueue;

/**
 * SemiConservative
 * 
 * @author John Whaley
 * @version $Id: SemiConservative.java,v 1.9 2004/08/09 08:22:27 joewhaley Exp $
 */
public abstract class SemiConservative {
    
    public static void collect() {
        if (true) Debug.writeln("Starting collection.");
        
        jq_Thread t = Unsafe.getThreadBlock();
        t.disableThreadSwitch();
        
        jq_NativeThread.suspendAllThreads();
        
        if (true) Debug.writeln("Threads suspended.");
        SimpleAllocator s = (SimpleAllocator) DefaultHeapAllocator.def();
        if (true) Debug.writeln("--> Marking roots.");
        scanRoots();
        if (true) Debug.writeln("--> Marking queue.");
        s.scanGCQueue();
        if (true) Debug.writeln("--> Sweeping.");
        s.sweep();
        
        if (true) Debug.writeln("Resuming threads.");
        jq_NativeThread.resumeAllThreads();
        
        t.enableThreadSwitch();
    }
    
    public static void scanRoots() {
        scanStatics();
        scanAllThreads();
    }
    
    /**
     * Scan static variables for object references.
     */
    public static void scanStatics() {
        // todo: other classloaders?
        jq_Type[] types = PrimordialClassLoader.loader.getAllTypes();
        int num = PrimordialClassLoader.loader.getNumTypes();
        for (int i = 0; i < num; ++i) {
            Object o = types[i];
            if (o instanceof jq_Class) {
                jq_Class c = (jq_Class) o;
                if (c.isSFInitialized()) {
                    jq_StaticField[] sfs = c.getDeclaredStaticFields();
                    for (int j=0; j<sfs.length; ++j) {
                        jq_StaticField sf = sfs[j];
                        jq_Type t = sf.getType();
                        if (t.isReferenceType() && !t.isAddressType()) {
                            if (SimpleAllocator.TRACE_GC) {
                                Debug.write(sf.getDeclaringClass().getDesc());
                                Debug.write(" ");
                                Debug.writeln(sf.getName());
                            }
                            HeapAddress a = sf.getAddress();
                            DefaultHeapAllocator.processObjectReference(a);
                        }
                    }
                }
            }
        }
    }
    
    public static void scanAllThreads() {
        if (jq_NativeThread.allNativeThreadsInitialized()) {
            for (int i = 0; i < jq_NativeThread.native_threads.length; ++i) {
                jq_NativeThread nt = jq_NativeThread.native_threads[i];
                if (SimpleAllocator.TRACE_GC) Debug.writeln("Scanning native thread ", i);
                scanQueuedThreads(nt);
                //addObject(nt, b);
            }
        } else {
            jq_NativeThread nt = Unsafe.getThreadBlock().getNativeThread();
            if (SimpleAllocator.TRACE_GC) Debug.writeln("Scanning initial native thread");
            scanQueuedThreads(nt);
        }
        scanCurrentThreadStack(3);
    }
    
    public static void scanQueuedThreads(jq_NativeThread nt) {
        for (int i = 0; i < jq_NativeThread.NUM_OF_QUEUES; ++i) {
            if (SimpleAllocator.TRACE_GC) Debug.writeln("Scanning thread queue ", i);
            scanThreadQueue(nt.getReadyQueue(i));
        }
        if (SimpleAllocator.TRACE_GC) Debug.writeln("Scanning idle queue");
        scanThreadQueue(nt.getIdleQueue());
        if (SimpleAllocator.TRACE_GC) Debug.writeln("Scanning transfer queue");
        scanThreadQueue(nt.getTransferQueue());
    }
    
    public static void scanThreadQueue(jq_ThreadQueue q) {
        jq_Thread t = q.peek();
        while (t != null) {
            scanThreadStack(t);
            //addObject(t);
            t = t.getNext();
        }
    }
    
    public static void scanCurrentThreadStack(int skip) {
        if (SimpleAllocator.TRACE_GC) Debug.writeln("Scanning current thread stack");
        StackAddress fp = StackAddress.getBasePointer();
        StackAddress sp = StackAddress.getStackPointer();
        CodeAddress ip = (CodeAddress) fp.offset(HeapAddress.size()).peek();
        while (!fp.isNull()) {
            if (SimpleAllocator.TRACE_GC) {
                Debug.write("Scanning stack frame fp=", fp);
                Debug.write(" sp=", sp);
                Debug.writeln(" ip=", ip);
            }
            if (--skip < 0) {
                while (fp.difference(sp) > 0) {
                    if (SimpleAllocator.TRACE_GC) {
                        Debug.write("sp: ", sp);
                        Debug.writeln("  ", sp.peek());
                    }
                    addConservativeAddress(sp);
                    sp = (StackAddress) sp.offset(HeapAddress.size());
                }
            } else {
                if (SimpleAllocator.TRACE_GC) Debug.writeln("Skipping this frame.");
            }
            ip = (CodeAddress) fp.offset(HeapAddress.size()).peek();
            sp = fp;
            fp = (StackAddress) fp.peek();
        }
    }
    
    public static void scanThreadStack(jq_Thread t) {
        jq_RegisterState s = t.getRegisterState();
        StackAddress fp = s.getEbp();
        CodeAddress ip = s.getEip();
        StackAddress sp = s.getEsp();
        while (!fp.isNull()) {
            while (fp.difference(sp) > 0) {
                addConservativeAddress(sp);
                sp = (StackAddress) sp.offset(HeapAddress.size());
            }
            ip = (CodeAddress) fp.offset(HeapAddress.size()).peek();
            sp = fp;
            fp = (StackAddress) fp.peek();
        }
    }
    
    public static void addConservativeAddress(Address a) {
        DefaultHeapAllocator.processPossibleObjectReference(a);
    }
    
}
