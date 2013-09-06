// SimpleAllocator.java, created Mon Feb  5 23:23:19 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import java.lang.reflect.Array;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.StackCodeWalker;
import joeq.Runtime.SystemInterface;
import jwutil.util.Assert;

/**
 * SimpleAllocator is a simple version of a heap allocator.
 * It is basically a bump-pointer allocator with a free list.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: SimpleAllocator.java,v 1.46 2005/01/21 07:13:15 joewhaley Exp $
 */
public class SimpleAllocator extends HeapAllocator {

    public static boolean NO_GC = true;
    
    public static boolean TRACE_ALLOC = false;
    public static boolean TRACE_FREELIST = false;
    public static boolean TRACE_GC = false;
    
    /**
     * Size of blocks allocated from the OS.
     */
    public static final int BLOCK_SIZE = 4194304;

    /**
     * Maximum memory, in bytes, to be allocated from the OS.
     */
    public static /*final*/ int MAX_MEMORY = 67108864;

    /**
     * Threshold for direct OS allocation.  When an array overflows the current block
     * and is larger than this size, it is allocated directly from the OS.
     */
    public static final int LARGE_THRESHOLD = 262144;

    /**
     * Pointers to the start, current, and end of the heap.
     */
    private HeapAddress heapFirst, heapCurrent, heapEnd;

    /**
     * Pointer to the start of the free list.
     */
    private HeapAddress firstFree;
    
    /**
     * Pointer to the start and current of the large object list.
     */
    private HeapAddress firstLarge, currLarge;
    
    /**
     * Simple work queue for GC.
     */
    private AddressQueue gcWorkQueue = new CircularAddressQueue();
    
    /**
     * The current state of the GC bit.  Flips back and forth on every GC.
     */
    private boolean flip;
    
    /**
     * The number of GC's that have occurred.
     */
    private int numOfGC;
    
    /**
     * Are we currently doing a GC?  For debugging purposes.
     */
    private boolean inGC;

    /**
     * Are we currently doing an allocation?  For debugging purposes.
     */
    private boolean inAlloc;
    
    /**
     * Smallest object size allowed in free list.
     */
    public static final int MIN_SIZE = Math.max(ObjectLayout.OBJ_HEADER_SIZE, 8);
    
    /**
     * Allocate a new block of memory from the OS and return a pointer to it,
     * throwing an OutOfMemoryError if we cannot allocate it.
     * 
     * @return  pointer to new block of memory
     * @throws OutOfMemoryError  if cannot allocate from OS
     */
    static final HeapAddress allocNewBlock() throws OutOfMemoryError {
        HeapAddress block = (HeapAddress) SystemInterface.syscalloc(BLOCK_SIZE);
        if (block.isNull()) {
            HeapAllocator.outOfMemory();
        }
        return block;
    }
    
    /**
     * Given a block, return its end address.
     * 
     * @param block  memory block
     * @return  end address
     */
    static final HeapAddress getBlockEnd(HeapAddress block) {
        // At end of memory block:
        //  - one word for pointer to start of next memory block.
        return (HeapAddress) block.offset(BLOCK_SIZE - HeapAddress.size());
    }
    
    /**
     * Given a block, return its size in bytes.
     * 
     * @param block  memory block
     * @return  size in bytes
     */
    static final int getBlockSize(HeapAddress block) {
        return BLOCK_SIZE;
    }
    
    /**
     * Get the next pointer of a block.
     * 
     * @param block  memory block
     * @return  pointer to next block
     */
    static final HeapAddress getBlockNext(HeapAddress block) {
        return (HeapAddress) block.offset(BLOCK_SIZE - HeapAddress.size()).peek();
    }
    
    /**
     * Set the next pointer of a block.
     * 
     * @param block  memory block
     * @param next  new value of pointer
     */
    static final void setBlockNext(HeapAddress block, HeapAddress next) {
        block.offset(BLOCK_SIZE - HeapAddress.size()).poke(next);
    }
    
    /**
     * Get the size in bytes of a free list entry.
     * 
     * @param free  free list entry
     * @return  size in bytes
     */
    static final int getFreeSize(HeapAddress free) {
        return free.offset(HeapAddress.size()).peek4();
    }
    
    /**
     * Set the size in bytes of a free list entry.
     * 
     * @param free  free list entry
     * @param size  size in bytes
     */
    static final void setFreeSize(HeapAddress free, int size) {
        Assert._assert(size >= HeapAddress.size()*2);
        free.offset(HeapAddress.size()).poke4(size);
        if (true) {
            int size2 = size - HeapAddress.size()*2;
            SystemInterface.mem_set(free.offset(HeapAddress.size()*2), (byte)0xbd, size2);
        }
    }
    
    /**
     * Given a free list entry, return its end address.
     * 
     * @param free  free list entry
     * @return  end address
     */
    static final HeapAddress getFreeEnd(HeapAddress free) {
        int size = getFreeSize(free);
        return (HeapAddress) free.offset(size);
    }
    
    /**
     * Get the next pointer of a free list entry.
     * 
     * @param free  free list entry
     * @return  pointer to next free list entry
     */
    static final HeapAddress getFreeNext(HeapAddress free) {
        return (HeapAddress) free.peek();
    }
    
    /**
     * Set the next pointer of a free list entry.
     * 
     * @param free  free list entry
     * @param next  new value of pointer
     */
    static final void setFreeNext(HeapAddress free, HeapAddress next) {
        free.poke(next);
    }
    
    /**
     * Get the next pointer of a large object entry.
     * 
     * @param large  large object entry
     * @return  pointer to next block
     */
    static final HeapAddress getLargeNext(HeapAddress large) {
        int size = getLargeSize(large);
        return (HeapAddress) large.offset(size).peek();
    }
    
    /**
     * Set the next pointer of a large object entry.
     * 
     * @param large  large object entry
     * @param next  new value of pointer
     */
    static final void setLargeNext(HeapAddress large, HeapAddress next) {
        int size = getLargeSize(large);
        large.offset(size).poke(next);
    }
    
    /**
     * Return the object contained in a large object entry.
     * 
     * @param large  large object entry
     * @return  object
     */
    static final Object getLargeObject(HeapAddress large) {
        Object o = ((HeapAddress) large.offset(ObjectLayout.ARRAY_HEADER_SIZE)).asObject();
        return o;
    }
    
    /**
     * Get the size in bytes of a large object entry.
     * 
     * @param large  large object entry
     * @return  size in bytes
     */
    static final int getLargeSize(HeapAddress large) {
        Object o = ((HeapAddress) large.offset(ObjectLayout.ARRAY_HEADER_SIZE)).asObject();
        int size = getObjectSize(o);
        return size;
    }
    
    /**
     * Perform initialization for this allocator.  This will be called before any other methods.
     * This allocates an initial block of memory from the OS and sets up relevant pointers.
     *
     * @throws OutOfMemoryError if there is not enough memory for initialization
     */
    public void init() throws OutOfMemoryError {
        Assert._assert(!inGC);
        Assert._assert(!inAlloc);
        heapCurrent = heapFirst = allocNewBlock();
        heapEnd = getBlockEnd(heapCurrent);
        firstFree = firstLarge = currLarge = HeapAddress.getNull();
    }

    private void addToFreeList(HeapAddress addr, HeapAddress end) {
        int size = end.difference(addr);
        if (size >= MIN_SIZE) {
            // Add remainder of this block to the free list.
            setFreeSize(addr, size);
            HeapAddress curr_p = firstFree;
            HeapAddress prev_p = HeapAddress.getNull();
            if (TRACE_FREELIST) {
                Debug.write("Adding free block ", addr);
                Debug.write("-", end);
                Debug.writeln(" size ", size);
            }
            for (;;) {
                if (TRACE_FREELIST) Debug.writeln("Checking ", curr_p);
                if (curr_p.isNull() || addr.difference(curr_p) < 0) {
                    if (prev_p.isNull()) {
                        firstFree = addr;
                        if (TRACE_FREELIST) Debug.writeln("New head of free list ", firstFree);
                    } else {
                        setFreeNext(prev_p, addr);
                        if (TRACE_FREELIST) Debug.writeln("Inserting after ", prev_p);
                    }
                    setFreeNext(addr, curr_p);
                    break;
                }
                HeapAddress next_p = getFreeNext(curr_p);
                prev_p = curr_p;
                curr_p = next_p;
            }
        }
    }
    
    /**
     * Allocates a new block of memory from the OS, sets the current block to
     * point to it, and makes the new block the current block.
     *
     * @throws OutOfMemoryError if there is not enough memory for initialization
     */
    private void allocateNewBlock() {
        if (TRACE_ALLOC) Debug.writeln("Allocating new memory block.");
        if (totalMemory() >= MAX_MEMORY) {
            HeapAllocator.outOfMemory();
        }
        // Add remainder of this block to the free list.
        addToFreeList(heapCurrent, heapEnd);
        
        // Allocate new block.
        heapCurrent = allocNewBlock();
        HeapAddress lastBlock = (HeapAddress) heapEnd.offset(HeapAddress.size()-BLOCK_SIZE);
        setBlockNext(lastBlock, heapCurrent);
        heapEnd = getBlockEnd(heapCurrent);
    }

    /**
     * Returns the number of bytes available in the free list.
     * 
     * @return bytes available in the free list
     */
    int getFreeListBytes() {
        int freeListMem = 0;
        HeapAddress p = firstFree;
        while (!p.isNull()) {
            freeListMem += getFreeSize(p);
            p = getFreeNext(p);
        }
        return freeListMem;
    }
    
    /**
     * Returns the number of bytes allocated in the large object list.
     * 
     * @return bytes allocated for large objects
     */
    int getLargeBytes() {
        int largeMem = 0;
        HeapAddress p = firstLarge;
        while (!p.isNull()) {
            largeMem += getLargeSize(p);
            p = getLargeNext(p);
        }
        return largeMem;
    }
    
    /**
     * Returns an estimate of the amount of free memory available.
     *
     * @return bytes of free memory
     */
    public int freeMemory() {
        return getFreeListBytes() + heapEnd.difference(heapCurrent);
    }

    /**
     * Returns an estimate of the total memory allocated (both used and unused).
     *
     * @return bytes of memory allocated
     */
    public int totalMemory() {
        int total = 0;
        HeapAddress ptr = heapFirst;
        while (!ptr.isNull()) {
            total += getBlockSize(ptr);
            ptr = getBlockNext(ptr);
        }
        total += getLargeBytes();
        return total;
    }

    /**
     * Allocate an object with the default alignment.
     * If the object cannot be allocated due to lack of memory, throws OutOfMemoryError.
     *
     * @param size size of object to allocate (including object header), in bytes
     * @param vtable vtable pointer for new object
     * @return new uninitialized object
     * @throws OutOfMemoryError if there is insufficient memory to perform the operation
     */
    public Object allocateObject(int size, Object vtable) throws OutOfMemoryError {
        if (TRACE_ALLOC || inAlloc || inGC) {
            if (inAlloc) {
                Debug.writeln("BUG! Trying to allocate during another allocation!");
                inAlloc = false; StackCodeWalker.stackDump(CodeAddress.getNull(), StackAddress.getBasePointer());
            }
            if (inGC) {
                Debug.writeln("BUG! Trying to allocate during GC!");
            }
            Debug.write("Allocating object of size ", size);
            jq_Type type = (jq_Type) ((HeapAddress) HeapAddress.addressOf(vtable).peek()).asObject();
            Debug.write(" type ");
            Debug.write(type.getDesc());
            Debug.writeln(" vtable ", HeapAddress.addressOf(vtable));
        }
        inAlloc = true;
        if (size < ObjectLayout.OBJ_HEADER_SIZE) {
            // size overflow! become minus!
            inAlloc = false;
            HeapAllocator.outOfMemory();
        }
        //jq.Assert((size & 0x3) == 0);
        size = (size + 3) & ~3; // align size
        HeapAddress addr = (HeapAddress) heapCurrent.offset(ObjectLayout.OBJ_HEADER_SIZE);
        heapCurrent = (HeapAddress) heapCurrent.offset(size);
        if (heapEnd.difference(heapCurrent) < 0) {
            // not enough space (rare path)
            heapCurrent = (HeapAddress) heapCurrent.offset(-size);
            if (TRACE_ALLOC) Debug.writeln("Not enough free space: ", heapEnd.difference(heapCurrent));
            // try to allocate the object from the free list.
            Object o = allocObjectFromFreeList(size, vtable);
            if (o != null) {
                inAlloc = false;
                return o;
            }
            // not enough space on free list, allocate another memory block.
            allocateNewBlock();
            addr = (HeapAddress) heapCurrent.offset(ObjectLayout.OBJ_HEADER_SIZE);
            heapCurrent = (HeapAddress) heapCurrent.offset(size);
            Assert._assert(heapEnd.difference(heapCurrent) >= 0);
        } else {
            if (TRACE_ALLOC) Debug.writeln("Fast path object allocation: ", addr);
        }
        // fast path
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        if (flip) addr.offset(ObjectLayout.STATUS_WORD_OFFSET).poke4(ObjectLayout.GC_BIT);
        inAlloc = false;
        return addr.asObject();
    }

    /**
     * Try to allocate a region of memory from the free list.
     * Returns a pointer to the allocated memory, or null if there is no
     * block large enough.
     * 
     * @param size  size to allocate in bytes
     * @return  pointer to allocated memory or null
     */
    private HeapAddress allocFromFreeList(int size) {
        // Search free list to find if there is an area that will fit the object.
        HeapAddress prev_p = HeapAddress.getNull();
        HeapAddress curr_p = firstFree;
        if (TRACE_FREELIST) Debug.writeln("Searching free list for block of size: ", size);
        while (!curr_p.isNull()) {
            if (TRACE_FREELIST) Debug.writeln("Looking at block ", curr_p);
            HeapAddress next_p = getFreeNext(curr_p);
            int areaSize = getFreeSize(curr_p);
            if (TRACE_FREELIST) Debug.writeln("Block size ", areaSize);
            if (areaSize >= size) {
                // This area fits!
                if (TRACE_FREELIST) Debug.writeln("Block fits, zeroing ", curr_p);
                // Zero out the memory.
                SystemInterface.mem_set(curr_p, (byte) 0, areaSize);
                // Fix up free list.
                int newSize = areaSize - size;
                if (TRACE_FREELIST) Debug.writeln("New size of block: ", newSize);
                HeapAddress new_next_p;
                if (newSize >= MIN_SIZE) {
                    // Still some space left here.
                    Assert._assert(newSize >= HeapAddress.size() * 2);
                    new_next_p = (HeapAddress) curr_p.offset(size);
                    setFreeNext(new_next_p, next_p);
                    setFreeSize(new_next_p, newSize);
                    if (TRACE_FREELIST) Debug.writeln("Block shrunk, now ", new_next_p);
                } else {
                    // Remainder is too small, skip it.
                    new_next_p = next_p;
                    if (TRACE_FREELIST) Debug.writeln("Result too small, new next ", new_next_p);
                }
                if (prev_p.isNull()) {
                    // New start of free list.
                    firstFree = new_next_p;
                    if (TRACE_FREELIST) Debug.writeln("New start of free list: ", firstFree);
                } else {
                    // Patch previous in free list to point to new location.
                    setFreeNext(prev_p, new_next_p);
                    if (TRACE_FREELIST) Debug.writeln("Inserted after ", prev_p);
                }
                return curr_p;
            }
            prev_p = curr_p;
            curr_p = next_p;
        }
        // Nothing in the free list is big enough!
        if (TRACE_FREELIST) Debug.writeln("Nothing in free list is big enough.");
        return HeapAddress.getNull();
    }
    
    /**
     * Try to allocate an object from the free list.  If there is not enough space, do a
     * garbage collection and try again.  If there is still not enough space, return null.
     * 
     * @param size  size of object in bytes
     * @param vtable  vtable for object
     * @return  allocated object, or null 
     */
    private Object allocObjectFromFreeList(int size, Object vtable) {
        HeapAddress addr = allocFromFreeList(size);
        if (addr.isNull()) {
            // Not enough space in free list, try a GC.
            collect();
            addr = allocFromFreeList(size);
            if (addr.isNull()) {
                // need to allocate new block of memory.
                return null;
            }
        }
        addr = (HeapAddress) addr.offset(ObjectLayout.OBJ_HEADER_SIZE);
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        if (flip) addr.offset(ObjectLayout.STATUS_WORD_OFFSET).poke4(ObjectLayout.GC_BIT);
        return addr.asObject();
    }
    
    /**
     * Try to allocate an array from the free list.  If there is not enough space, do a
     * garbage collection and try again.  If there is still not enough space, return null.
     * 
     * @param length  number of elements in array
     * @param size  size of array in bytes
     * @param vtable  vtable for array
     * @return  allocated array, or null 
     */
    private Object allocArrayFromFreeList(int length, int size, Object vtable) {
        HeapAddress addr = allocFromFreeList(size);
        if (addr.isNull()) {
            // Not enough space in free list, try a GC.
            collect();
            addr = allocFromFreeList(size);
            if (addr.isNull()) {
                // need to allocate new block of memory.
                return null;
            }
        }
        addr = (HeapAddress) addr.offset(ObjectLayout.ARRAY_HEADER_SIZE);
        addr.offset(ObjectLayout.ARRAY_LENGTH_OFFSET).poke4(length);
        if (flip) addr.offset(ObjectLayout.STATUS_WORD_OFFSET).poke4(ObjectLayout.GC_BIT);
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        return addr.asObject();
    }
    
    /**
     * Try to allocate a large array.
     * 
     * @param length
     * @param size
     * @param vtable
     * @return  new array object
     * @throws OutOfMemoryError
     */
    private Object allocLargeArray(int length, int size, Object vtable) throws OutOfMemoryError {
        HeapAddress addr = (HeapAddress) SystemInterface.syscalloc(size + HeapAddress.size());
        if (addr.isNull())
            outOfMemory();
        if (firstLarge.isNull()) {
            firstLarge = currLarge = addr;
        } else {
            setLargeNext(currLarge, addr);
            currLarge = addr;
        }
        addr = (HeapAddress) addr.offset(ObjectLayout.ARRAY_HEADER_SIZE);
        addr.offset(ObjectLayout.ARRAY_LENGTH_OFFSET).poke4(length);
        if (flip) addr.offset(ObjectLayout.STATUS_WORD_OFFSET).poke4(ObjectLayout.GC_BIT);
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        return addr.asObject();
    }
    
    public static final int getObjectSize(Object o) {
        jq_Reference t = jq_Reference.getTypeOf(o);
        int size;
        if (t.isArrayType()) {
            jq_Array a = (jq_Array) t;
            int length = Array.getLength(o);
            size = a.getInstanceSize(length);
        } else {
            jq_Class c = (jq_Class) t;
            size = c.getInstanceSize();
        }
        size = (size + 3) & ~3; // align size
        return size;
    }
    
    /**
     * Allocate an object such that the first field is 8-byte aligned.
     * If the object cannot be allocated due to lack of memory, throws OutOfMemoryError.
     *
     * @param size size of object to allocate (including object header), in bytes
     * @param vtable vtable pointer for new object
     * @return new uninitialized object
     * @throws OutOfMemoryError if there is insufficient memory to perform the operation
     */
    public Object allocateObjectAlign8(int size, Object vtable) throws OutOfMemoryError {
        heapCurrent = (HeapAddress) heapCurrent.offset(ObjectLayout.OBJ_HEADER_SIZE).align(3).offset(-ObjectLayout.OBJ_HEADER_SIZE);
        return allocateObject(size, vtable);
    }

    /**
     * Allocate an array with the default alignment.
     * If length is negative, throws NegativeArraySizeException.
     * If the array cannot be allocated due to lack of memory, throws OutOfMemoryError.
     *
     * @param length length of new array
     * @param size size of array to allocate (including array header), in bytes
     * @param vtable vtable pointer for new array
     * @return new array
     * @throws NegativeArraySizeException if length is negative
     * @throws OutOfMemoryError if there is insufficient memory to perform the operation
     */
    public Object allocateArray(int length, int size, Object vtable) throws OutOfMemoryError, NegativeArraySizeException {
        if (length < 0) throw new NegativeArraySizeException(length + " < 0");
        
        if (TRACE_ALLOC || inAlloc || inGC) {
            if (inAlloc) {
                Debug.writeln("BUG! Trying to allocate during another allocation!");
                inAlloc = false; StackCodeWalker.stackDump(CodeAddress.getNull(), StackAddress.getBasePointer());
            }
            if (inGC) {
                Debug.writeln("BUG! Trying to allocate during GC!");
            }
            Debug.write("Allocating array of size ", size);
            jq_Type type = (jq_Type) ((HeapAddress) HeapAddress.addressOf(vtable).peek()).asObject();
            Debug.write(" type ");
            Debug.write(type.getDesc());
            Debug.write(" length ", length);
            Debug.writeln(" vtable ", HeapAddress.addressOf(vtable));
        }
        
        inAlloc = true;
        if (size < ObjectLayout.ARRAY_HEADER_SIZE) {
            // size overflow!
            inAlloc = false;
            HeapAllocator.outOfMemory();
        }
        size = (size + 3) & ~3; // align size
        HeapAddress addr = (HeapAddress) heapCurrent.offset(ObjectLayout.ARRAY_HEADER_SIZE);
        heapCurrent = (HeapAddress) heapCurrent.offset(size);
        if (heapEnd.difference(heapCurrent) < 0) {
            // not enough space (rare path)
            heapCurrent = (HeapAddress) heapCurrent.offset(-size);
            if (size > LARGE_THRESHOLD) {
                // special large-object allocation
                Object o = allocLargeArray(length, size, vtable);
                inAlloc = false;
                return o;
            } else {
                Object o = allocArrayFromFreeList(length, size, vtable);
                if (o != null) {
                    inAlloc = false;
                    return o;
                }
                // not enough space on free list, allocate another memory block.
                allocateNewBlock();
                addr = (HeapAddress) heapCurrent.offset(ObjectLayout.ARRAY_HEADER_SIZE);
                heapCurrent = (HeapAddress) heapCurrent.offset(size);
                Assert._assert(heapEnd.difference(heapCurrent) >= 0);
            }
        } else {
            if (TRACE_ALLOC) Debug.writeln("Fast path array allocation: ", addr);
        }
        // fast path
        addr.offset(ObjectLayout.ARRAY_LENGTH_OFFSET).poke4(length);
        if (flip) addr.offset(ObjectLayout.STATUS_WORD_OFFSET).poke4(ObjectLayout.GC_BIT);
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        inAlloc = false;
        return addr.asObject();
    }

    /**
     * Allocate an array such that the elements are 8-byte aligned.
     * If length is negative, throws NegativeArraySizeException.
     * If the array cannot be allocated due to lack of memory, throws OutOfMemoryError.
     *
     * @param length length of new array
     * @param size size of array to allocate (including array header), in bytes
     * @param vtable vtable pointer for new array
     * @return new array
     * @throws NegativeArraySizeException if length is negative
     * @throws OutOfMemoryError if there is insufficient memory to perform the operation
     */
    public Object allocateArrayAlign8(int length, int size, Object vtable) throws OutOfMemoryError, NegativeArraySizeException {
        heapCurrent = (HeapAddress) heapCurrent.offset(ObjectLayout.ARRAY_HEADER_SIZE).align(3).offset(-ObjectLayout.ARRAY_HEADER_SIZE);
        return allocateArray(length, size, vtable);
    }

    /* (non-Javadoc)
     * @see joeq.Allocator.HeapAllocator#collect()
     */
    public void collect() {
        if (NO_GC) {
            return;
        }
        if (inGC) {
            if (TRACE_GC) Debug.writeln("BUG! Recursively calling GC!");
            //allocateNewBlock();
            return;
        }
        inGC = true;
        ++numOfGC;
        Debug.write("Starting GC #", numOfGC);
        Debug.write(" total ", totalMemory() / 1024, "K");
        Debug.writeln(" free ", freeMemory() / 1024, "K");
        flip = !flip;
        SemiConservative.collect();
        Debug.write("Finished GC #", numOfGC);
        Debug.write(" total ", totalMemory() / 1024, "K");
        Debug.writeln(" free ", freeMemory() / 1024, "K");
        inGC = false;
    }
    
    public void sweep() {
        updateFreeList();
        updateLargeObjectList();
    }

    void scanGCQueue() {
        for (;;) {
            HeapAddress o = (HeapAddress) gcWorkQueue.pull();
            if (TRACE_GC) Debug.writeln("Pulled object from queue: ", HeapAddress.addressOf(o));
            if (o.isNull()) break;
            scanObject(o.asObject());
        }
    }
    
    void updateLargeObjectList() {
        HeapAddress prev_p = HeapAddress.getNull();
        HeapAddress curr_p = firstLarge;
        while (!curr_p.isNull()) {
            Object o = getLargeObject(curr_p);
            HeapAddress next_p = getLargeNext(curr_p);
            int status = HeapAddress.addressOf(o).offset(ObjectLayout.STATUS_WORD_OFFSET).peek4();
            if ((status & ObjectLayout.GC_BIT) == 0) {
                if (prev_p.isNull()) {
                    firstLarge = next_p;
                } else {
                    setLargeNext(prev_p, next_p);
                }
                if (curr_p.difference(currLarge) == 0) {
                    currLarge = prev_p;
                }
            }
            prev_p = curr_p;
            curr_p = next_p;
        }
    }
    
    void updateFreeList() {
        if (TRACE_FREELIST) Debug.writeln("Updating free list.");
        // Scan forward through heap, finding unmarked objects and merging free spaces.
        HeapAddress currBlock = heapFirst;
        while (!currBlock.isNull()) {
            HeapAddress currBlockEnd = getBlockEnd(currBlock);
            HeapAddress p = currBlock;
            HeapAddress currFree, prevFree;
            prevFree = HeapAddress.getNull();
            currFree = firstFree;
            // Seek to the right place in the free list.
            while (!currFree.isNull() && currFree.difference(p) <= 0) {
                prevFree = currFree;
                currFree = getFreeNext(currFree);
            }
            
            if (TRACE_FREELIST) {
                Debug.write("Visiting block ", currBlock);
                Debug.write("-", currBlockEnd);
                Debug.write(" free list ptr ", prevFree);
                Debug.writeln(",", currFree);
            }
            
            boolean lastWasFree = false;
            
            // Walk over current block.
            outer:
            while (currBlockEnd.difference(p) > 0) {
                
                if (TRACE_FREELIST) Debug.write("ptr=", p);
                
                if (p.difference(currFree) == 0) {
                    if (TRACE_FREELIST) Debug.write(" on free list, ");
                    p = getFreeEnd(currFree);
                    if (lastWasFree) {
                        HeapAddress nextFree = getFreeNext(currFree);
                        // Extend size of previous free area.
                        int newSize = p.difference(prevFree);
                        setFreeSize(prevFree, newSize);
                        currFree = nextFree;
                        setFreeNext(prevFree, currFree);
                        if (TRACE_FREELIST) {
                            Debug.write("prev free area extended to size ", newSize);
                            Debug.writeln(", next free=", currFree);
                        }
                    } else {
                        // Skip over known free chunk.
                        prevFree = currFree;
                        currFree = getFreeNext(currFree);
                        if (TRACE_FREELIST) Debug.writeln(" on free list. Next free=", currFree);
                    }
                    lastWasFree = true;
                    continue;
                }
                
                HeapAddress lastEnd = p;
                HeapAddress obj = HeapAddress.getNull();
                // Scan forward to find next object reference.
                while (currBlockEnd.difference(p) > 0) {
                    HeapAddress p2 = (HeapAddress) p.offset(ObjectLayout.ARRAY_HEADER_SIZE);
                    boolean b2 = isValidArray(p2);
                    if (b2) {
                        obj = p2;
                        if (TRACE_FREELIST) Debug.write(" array ", obj);
                        break;
                    }
                    HeapAddress p1 = (HeapAddress) p.offset(ObjectLayout.OBJ_HEADER_SIZE);
                    boolean b1 = isValidObject(p1);
                    if (b1) {
                        obj = p1;
                        if (TRACE_FREELIST) Debug.write(" object ", obj);
                        break;
                    }
                    p = (HeapAddress) p.offset(HeapAddress.size());
                }
                
                if (TRACE_FREELIST) {
                    Debug.write(" ");
                }
                HeapAddress next_p;
                boolean isFree;
                if (!obj.isNull()) {
                    Object o = obj.asObject();
                    if (TRACE_FREELIST) Debug.write(jq_Reference.getTypeOf(o).getDesc());
                    int size = getObjectSize(o);
                    //if (TRACE_FREELIST) Debug.write(" size ", size);
                    next_p = (HeapAddress) p.offset(size).align(2);
                    isFree = getGCBit(o) != flip;
                } else {
                    next_p = p;
                    isFree = !getBlockNext(currBlock).isNull();
                    if (TRACE_FREELIST) Debug.write("<end of block>");
                }
                
                if (TRACE_FREELIST) Debug.writeln(isFree?" free":" notfree");
                
                if (isFree) {
                    if (lastWasFree) {
                        // Just extend size of this free area.
                        int newSize = next_p.difference(prevFree);
                        setFreeSize(prevFree, newSize);
                        if (TRACE_FREELIST) {
                            Debug.write("Free area ", prevFree);
                            Debug.writeln(" extended to size ", newSize);
                        }
                    } else {
                        int newSize = next_p.difference(lastEnd);
                        if (newSize >= HeapAddress.size() * 2) {
                            // Insert into free list.
                            setFreeNext(lastEnd, currFree);
                            setFreeSize(lastEnd, newSize);
                            if (TRACE_FREELIST) {
                                Debug.write("Inserted free area ", prevFree);
                                Debug.write(", ", lastEnd);
                                Debug.write(", ", currFree);
                                Debug.writeln(" size ", newSize);
                            }
                            if (prevFree.isNull()) {
                                firstFree = lastEnd;
                            } else {
                                setFreeNext(prevFree, lastEnd);
                            }
                            prevFree = lastEnd;
                        } else {
                            if (TRACE_FREELIST) {
                                Debug.writeln("Free area too small, skipping: ", newSize);
                            }
                        }
                    }
                }
                lastWasFree = isFree;
                
                p = next_p;
            }
            currBlock = (HeapAddress) getBlockNext(currBlock);
        }
    }
    
    void scanObject(Object obj) {
        jq_Reference type = jq_Reference.getTypeOf(obj);
        if (type.isClassType()) {
            if (TRACE_GC) Debug.writeln("Scanning object ", HeapAddress.addressOf(obj));
            int[] referenceOffsets = ((jq_Class)type).getReferenceOffsets();
            for (int i = 0, n = referenceOffsets.length; i < n; i++) {
                HeapAddress objRef = HeapAddress.addressOf(obj);
                if (TRACE_GC) Debug.writeln("Scanning offset ", referenceOffsets[i]);
                DefaultHeapAllocator.processObjectReference((HeapAddress) objRef.offset(referenceOffsets[i]));
            }
        } else {
            if (TRACE_GC) Debug.writeln("Scanning array ", HeapAddress.addressOf(obj));
            jq_Type elementType = ((jq_Array)type).getElementType();
            if (elementType.isReferenceType() && !elementType.isAddressType()) {
                int num_elements = Array.getLength(obj);
                int numBytes = num_elements * HeapAddress.size();
                HeapAddress objRef = HeapAddress.addressOf(obj);
                HeapAddress location = (HeapAddress) objRef.offset(ObjectLayout.ARRAY_ELEMENT_OFFSET);
                HeapAddress end = (HeapAddress) location.offset(numBytes);
                while (location.difference(end) < 0) {
                    if (TRACE_GC) Debug.writeln("Scanning address ", location);
                    DefaultHeapAllocator.processObjectReference(location);
                    location = (HeapAddress) location.offset(HeapAddress.size());
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.HeapAllocator#processObjectReference(joeq.Memory.HeapAddress)
     */
    public void processObjectReference(Address a) {
        if (TRACE_GC) Debug.writeln("Processing object reference at ", a);
        HeapAddress a2 = (HeapAddress) a.peek();
        if (a2.isNull()) {
            return;
        }
        if (getGCBit(a2.asObject()) == flip) {
            return;
        }
        setGCBit(a2.asObject(), flip);
        gcWorkQueue.push(a2);
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.HeapAllocator#processPossibleObjectReference(joeq.Memory.Address)
     */
    public void processPossibleObjectReference(Address a) {
        if (TRACE_GC) Debug.writeln("Processing possible object reference at ", a);
        HeapAddress a2 = (HeapAddress) a.peek();
        if (!isValidObject(a2, 1)) {
            if (TRACE_GC) Debug.writeln("Not a valid object, skipping: ", a2);
            return;
        }
        if (getGCBit(a2.asObject()) == flip) {
            return;
        }
        // TODO: don't set GC bit for possible objects, use a separate data structure instead.
        setGCBit(a2.asObject(), flip);
        gcWorkQueue.push(a2);
    }
    
    /* (non-Javadoc)
     * @see joeq.Allocator.HeapAllocator#isInHeap(joeq.Memory.Address)
     */
    public boolean isInHeap(Address a) {
        HeapAddress p = heapFirst;
        while (!p.isNull()) {
            int diff = a.difference(p);
            if (diff >= 0 && diff < BLOCK_SIZE - 2 * HeapAddress.size())
                return true;
            p = (HeapAddress) p.offset(BLOCK_SIZE - HeapAddress.size()).peek();
        }
        return false;
    }
    
    public static final jq_Class _class;
    public static final jq_InstanceMethod _allocateObject;
    public static final jq_InstanceMethod _allocateObjectAlign8;
    public static final jq_InstanceMethod _allocateArray;
    public static final jq_InstanceMethod _allocateArrayAlign8;

    static {
        _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Allocator/SimpleAllocator;");
        _allocateObject = _class.getOrCreateInstanceMethod("allocateObject", "(ILjava/lang/Object;)Ljava/lang/Object;");
        _allocateObjectAlign8 = _class.getOrCreateInstanceMethod("allocateObjectAlign8", "(ILjava/lang/Object;)Ljava/lang/Object;");
        _allocateArray = _class.getOrCreateInstanceMethod("allocateArray", "(IILjava/lang/Object;)Ljava/lang/Object;");
        _allocateArrayAlign8 = _class.getOrCreateInstanceMethod("allocateArrayAlign8", "(IILjava/lang/Object;)Ljava/lang/Object;");
    }

}
