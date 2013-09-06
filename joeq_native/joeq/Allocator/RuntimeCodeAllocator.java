// RuntimeCodeAllocator.java, created Tue Feb 27  2:53:11 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import java.util.List;
import joeq.Class.jq_BytecodeMap;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Method;
import joeq.Class.jq_TryCatch;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Runtime.ExceptionDeliverer;
import joeq.Runtime.SystemInterface;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * RuntimeCodeAllocator
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: RuntimeCodeAllocator.java,v 1.22 2004/09/30 03:35:30 joewhaley Exp $
 */
public class RuntimeCodeAllocator extends CodeAllocator {

    // Memory layout:
    //
    // start:   |   next ptr   |---->
    //          |   end ptr    |
    //          |.....data.....|
    // current: |.....free.....|
    // end:     | current ptr  |

    /** Size of blocks allocated from the OS.
     */
    public static final int BLOCK_SIZE = 131072;
    
    /** Pointers to the start, current, and end of the heap.
     */
    private CodeAddress heapStart, heapCurrent, heapEnd;
    
    /** Pointer to the first block.
     */
    private CodeAddress heapFirst;
    
    /** Max memory free in all allocated blocks.
     */
    private int maxFreePrevious;
    
    volatile boolean isGenerating = false;
    
    public void init()
    throws OutOfMemoryError {
        heapStart = heapFirst = (CodeAddress)SystemInterface.syscalloc(BLOCK_SIZE);
        if (heapStart.isNull())
            HeapAllocator.outOfMemory();
        heapStart.poke(null);
        heapEnd = (CodeAddress)heapStart.offset(BLOCK_SIZE - CodeAddress.size());
        heapStart.offset(CodeAddress.size()).poke(heapEnd);
        heapCurrent = (CodeAddress)heapStart.offset(CodeAddress.size() * 2);
        heapEnd.poke(heapCurrent);
    }
    
    /** Allocate a code buffer of the given estimated size, such that the given
     * offset will have the given alignment.
     * It is legal for code to exceed the estimated size, but the cost may be
     * high (i.e. it may require recopying of the buffer.)
     *
     * @param estimatedSize  estimated size, in bytes, of desired code buffer
     * @param offset  desired offset to align to
     * @param alignment  desired alignment, or 0 if don't care
     * @return  the new code buffer
     */
    public x86CodeBuffer getCodeBuffer(int estimatedSize,
                                       int offset,
                                       int alignment) {
        // should not be called recursively.
        Assert._assert(!isGenerating);
        if (TRACE) SystemInterface.debugwriteln("Code generation started: "+this);
        isGenerating = true;
        // align pointer
        CodeAddress entrypoint = (CodeAddress)heapCurrent.offset(offset);
        if (alignment > 0) entrypoint.align(alignment);
        if (entrypoint.offset(estimatedSize - offset).difference(heapEnd) <= 0) {
            return new Runtimex86CodeBuffer((CodeAddress)entrypoint.offset(-offset), heapEnd);
        }
        if (estimatedSize < maxFreePrevious) {
            // use a prior block's unused space.
            if (TRACE) SystemInterface.debugwriteln("Estimated size ("+Strings.hex(estimatedSize)+" fits within a prior block: maxfreeprev="+Strings.hex(maxFreePrevious));
            // start searching at the first block
            CodeAddress start_ptr = heapFirst;
            for (;;) {
                // start_ptr:   points to start of current block
                // end_ptr:     points to end of current block
                // current_ptr: current pointer for current block
                Assert._assert(!start_ptr.isNull());
                CodeAddress end_ptr = (CodeAddress)start_ptr.offset(CodeAddress.size()).peek();
                CodeAddress current_ptr = (CodeAddress)end_ptr.peek();
                if (end_ptr.difference(current_ptr) >= estimatedSize) {
                    return new Runtimex86CodeBuffer(current_ptr, end_ptr);
                }
                start_ptr = (CodeAddress)start_ptr.peek(); // go to the next block
            }
        }
        // allocate new block.
        allocateNewBlock(Math.max(estimatedSize, BLOCK_SIZE));
        return new Runtimex86CodeBuffer(heapCurrent, heapEnd);
    }
    
    private void allocateNewBlock(int blockSize)
    throws OutOfMemoryError {
        heapStart.offset(CodeAddress.size()).poke(heapCurrent);
        CodeAddress newBlock = (CodeAddress)SystemInterface.syscalloc(blockSize);
        if (newBlock.isNull())
            HeapAllocator.outOfMemory();
        heapStart.poke(newBlock);
        heapStart = newBlock;
        heapStart.poke(null);
        heapEnd = (CodeAddress)newBlock.offset(blockSize - CodeAddress.size());
        heapStart.offset(CodeAddress.size()).poke(heapEnd);
        heapCurrent = (CodeAddress)newBlock.offset(CodeAddress.size() * 2);
        heapEnd.poke(heapCurrent);
    }
    
    public void patchAbsolute(Address addr1, Address addr2) {
        addr1.poke(addr2);
    }
    public void patchRelativeOffset(CodeAddress code, CodeAddress target) {
        code.poke4(target.difference(code)-4);
    }
    
    public class Runtimex86CodeBuffer extends CodeAllocator.x86CodeBuffer {

        private CodeAddress startAddress;
        private CodeAddress entrypointAddress;
        private CodeAddress currentAddress;
        private CodeAddress endAddress;

        Runtimex86CodeBuffer(CodeAddress startAddress, CodeAddress endAddress) {
            this.startAddress = startAddress;
            this.endAddress = endAddress;
            this.currentAddress = (CodeAddress)startAddress.offset(-1);
        }
        
        public int getCurrentOffset() { return currentAddress.difference(startAddress) + 1; }
        public CodeAddress getStartAddress() { return startAddress; }
        public CodeAddress getCurrentAddress() { return (CodeAddress)currentAddress.offset(1); }
        
        public CodeAddress getStart() { return startAddress; }
        public CodeAddress getCurrent() { return (CodeAddress)currentAddress.offset(1); }
        public CodeAddress getEntry() { return entrypointAddress; }
        public CodeAddress getEnd() { return endAddress; }
        
        public void setEntrypoint() { this.entrypointAddress = getCurrent(); }
        
        public void checkSize(int size) {
            if (currentAddress.offset(size).difference(endAddress) < 0) return;
            // overflow!
            int newEstimatedSize = endAddress.difference(startAddress) << 1;
            allocateNewBlock(Math.max(BLOCK_SIZE, newEstimatedSize));
            Assert._assert(currentAddress.difference(startAddress)+size < heapEnd.difference(heapCurrent));
            SystemInterface.mem_cpy(heapCurrent, startAddress, currentAddress.difference(startAddress));
            if (!entrypointAddress.isNull())
                entrypointAddress = (CodeAddress)heapCurrent.offset(entrypointAddress.difference(startAddress));
            currentAddress = (CodeAddress)heapCurrent.offset(currentAddress.difference(startAddress));
            startAddress = heapCurrent;
            endAddress = heapEnd;
        }
        
        public void add1(byte i) {
            checkSize(1);
            currentAddress = (CodeAddress)currentAddress.offset(1);
            currentAddress.poke1(i);
        }
        public void add2_endian(int i) {
            checkSize(2);
            currentAddress.offset(1).poke2((short)i);
            currentAddress = (CodeAddress)currentAddress.offset(2);
        }
        public void add2(int i) {
            checkSize(2);
            currentAddress.offset(1).poke2(endian2(i));
            currentAddress = (CodeAddress)currentAddress.offset(2);
        }
        public void add3(int i) {
            checkSize(3);
            currentAddress.offset(1).poke1((byte)(i >> 16));
            currentAddress.offset(2).poke2(endian2(i));
            currentAddress = (CodeAddress)currentAddress.offset(3);
        }
        public void add4_endian(int i) {
            checkSize(4);
            currentAddress.offset(1).poke4(i);
            currentAddress = (CodeAddress)currentAddress.offset(4);
        }

        public byte get1(int k) {
            return startAddress.offset(k).peek1();
        }
        public int get4_endian(int k) {
            return startAddress.offset(k).peek4();
        }

        public void put1(int k, byte instr) {
            startAddress.offset(k).poke1(instr);
        }
        public void put4_endian(int k, int instr) {
            startAddress.offset(k).poke4(instr);
        }

        public void skip(int nbytes) {
            currentAddress = (CodeAddress)currentAddress.offset(nbytes);
        }
        
        public jq_CompiledCode allocateCodeBlock(jq_Method m, jq_TryCatch[] ex,
                                                 jq_BytecodeMap bcm, ExceptionDeliverer exd,
                                                 int stackframesize,
                                                 List code_relocs, List data_relocs) {
            Assert._assert(isGenerating);
            CodeAddress start = getStart();
            CodeAddress entrypoint = getEntry();
            CodeAddress current = getCurrent();
            CodeAddress end = getEnd();
            Assert._assert(current.difference(end) <= 0);
            if (end != heapEnd) {
                if (TRACE) SystemInterface.debugwriteln("Prior block, recalculating maxfreeprevious (was "+Strings.hex(maxFreePrevious)+")");
                // prior block
                end.poke(current);
                // recalculate max free previous
                maxFreePrevious = 0;
                CodeAddress start_ptr = heapFirst;
                while (!start_ptr.isNull()) {
                    CodeAddress end_ptr = (CodeAddress)start_ptr.offset(CodeAddress.size());
                    CodeAddress current_ptr = (CodeAddress)end_ptr.peek();
                    int temp = end_ptr.difference(current_ptr);
                    maxFreePrevious = Math.max(maxFreePrevious, temp);
                    start_ptr = (CodeAddress)start_ptr.peek();
                }
                if (TRACE) SystemInterface.debugwriteln("New maxfreeprevious: "+Strings.hex(maxFreePrevious));
            } else {
                // current block
                heapCurrent = current;
                heapEnd.poke(heapCurrent);
            }
            isGenerating = false;
            if (TRACE) SystemInterface.debugwriteln("Code generation completed: "+this);
            jq_CompiledCode cc = new jq_CompiledCode(m, start, current.difference(start), entrypoint, ex, bcm, exd, stackframesize, code_relocs, data_relocs);
            CodeAllocator.registerCode(cc);
            return cc;
        }
    }

    public static short endian2(int k) {
        return (short)(((k>>8)&0xFF) | (k<<8));
    }
}
