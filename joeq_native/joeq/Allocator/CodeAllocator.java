// CodeAllocator.java, created Mon Feb  5 23:23:19 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_BytecodeMap;
import joeq.Class.jq_Class;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Method;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_TryCatch;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Runtime.ExceptionDeliverer;

/**
 * This class provides the abstract interface for code allocators.  A code
 * allocator handles the allocation and management of code buffers.
 *
 * It also provides static methods for keeping track of the compiled methods and
 * their address ranges.
 * 
 * It also includes an inner class that provides the interface for code buffers.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CodeAllocator.java,v 1.26 2004/09/30 03:35:30 joewhaley Exp $
 */
public abstract class CodeAllocator {
    
    /** Trace flag. */
    public static /*final*/ boolean TRACE = false;
    
    /**
     * Initialize this code allocator.  This method is always called before the
     * code allocator is actually used.
     */
    public abstract void init();
    
    /**
     * Allocate a code buffer of the given estimated size, such that the given
     * offset will have the given alignment.
     * It is legal for code to exceed the estimated size, but the cost may be
     * high (i.e. it may require recopying of the buffer.)
     *
     * @param estimatedSize  estimated size, in bytes, of desired code buffer
     * @param offset  desired offset to align to
     * @param alignment  desired alignment, or 0 if don't care
     * @return  the new code buffer
     */
    public abstract x86CodeBuffer getCodeBuffer(int estimatedSize,
                                                int offset,
                                                int alignment);
    
    /**
     * Patch the given address to refer to the other given address, in
     * absolute terms.  This is used to patch heap address references in the
     * code, and code references in the heap.
     *
     * @param addr1  address to patch
     * @param addr2  address to patch to
     */
    public abstract void patchAbsolute(Address addr1,
                                       Address addr2);
    
    /**
     * Patch the given code address to refer to the given code address, in
     * relative terms.  This is used to patch branch targets in the code.
     *
     * @param code  code address to patch
     * @param target  code address to patch to
     */
    public abstract void patchRelativeOffset(CodeAddress code,
                                             CodeAddress target);

    /**
     * This class provides the interface for x86 code buffers.
     * These code buffers are used to store generated x86 code.
     * After the code is generated, use the allocateCodeBlock method to obtain
     * a jq_CompiledCode object.
     */
    public abstract static class x86CodeBuffer {

        /**
         * Returns the current offset in this code buffer.
         * @return  current offset
         */
        public abstract int getCurrentOffset();
        
        /**
         * Returns the current address in this code buffer.
         * @return  current address
         */
        public abstract CodeAddress getStartAddress();
        
        /**
         * Returns the current address in this code buffer.
         * @return  current address
         */
        public abstract CodeAddress getCurrentAddress();

        /**
         * Sets the current address as the entrypoint to this code buffer.
         */
        public abstract void setEntrypoint();

        /**
         * Adds one byte to the end of this code buffer.  Offset/address
         * increase by 1.
         * @param i  the byte to add
         */
        public abstract void add1(byte i);
        
        /**
         * Adds two bytes (little-endian) to the end of this code buffer.
         * Offset/address increase by 2.
         * @param i  the little-endian value to add
         */
        public abstract void add2_endian(int i);
        
        /**
         * Adds two bytes (big-endian) to the end of this code buffer.
         * Offset/address increase by 2.
         * @param i  the big-endian value to add
         */
        public abstract void add2(int i);
        
        /**
         * Adds three bytes (big-endian) to the end of this code buffer.
         * Offset/address increase by 3.
         * @param i  the big-endian value to add
         */
        public abstract void add3(int i);
        
        /**
         * Adds four bytes (little-endian) to the end of this code buffer.
         * Offset/address increase by 4.
         * @param i  the little-endian value to add
         */
        public abstract void add4_endian(int i);
        
        /**
         * Gets the byte at the given offset in this code buffer.
         * 
         * @param k  offset of byte to return
         * @return  byte at given offset
         */
        public abstract byte get1(int k);
        
        /**
         * Gets the (little-endian) 4 bytes at the given offset in this
         * code buffer.
         * 
         * @param k  offset of little-endian 4 bytes to return
         * @return  little-endian 4 bytes at given offset
         */
        public abstract int get4_endian(int k);

        /**
         * Sets the byte at the given offset to the given value.
         * @param k  offset of byte to set
         * @param instr  value to set it to
         */
        public abstract void put1(int k, byte instr);
        
        /**
         * Sets the 4 bytes at the given offset to the given (little-endian)
         * value.
         * @param k  offset of 4 bytes to set
         * @param instr  little-endian value to set it to
         */
        public abstract void put4_endian(int k, int instr);
        
        public abstract void skip(int nbytes);
        
        /**
         * Uses the code in this buffer, along with the arguments, to create
         * a jq_CompiledCode object.  Call this method after you are done
         * generating code, and actually want to use it.
         * 
         * @param m  Java method of this code block, or null if none
         * @param ex  exception handler table, or null if none
         * @param bcm  bytecode map, or null if none
         * @param x  exception deliverer to use for this code, or null if none
         * @param stackframesize  size of stack frame in bytes
         * @param codeRelocs  list of code relocations for this code buffer, or
         *                     null if none
         * @param dataRelocs  list of data relocations for this code buffer, or
         *                     null if none
         * @return  a new jq_CompiledCode object for the code
         */
        public abstract jq_CompiledCode allocateCodeBlock(jq_Method m,
                                                          jq_TryCatch[] ex,
                                                          jq_BytecodeMap bcm,
                                                          ExceptionDeliverer x,
                                                          int stackframesize,
                                                          List codeRelocs,
                                                          List dataRelocs);
    }
    
    /** Map of compiled methods, sorted by address. */
    public static final SortedMap compiledMethods;
    
    /**
     * Address range of compiled code.  Code outside of this range cannot be
     * generated by us.
     */
    private static CodeAddress lowAddress, highAddress;
    static {
        compiledMethods = new TreeMap();
    }

    public static void initializeCompiledMethodMap() {
        lowAddress = (CodeAddress) CodeAddress.getNull().offset(0x7FFFFFFF);
        highAddress = CodeAddress.getNull();
        jq_CompiledCode cc = new jq_CompiledCode(null, highAddress, 0, highAddress,
                                                 null, null, null, 0, null, null);
        compiledMethods.put(cc, cc);
    }
    
    /**
     * Register the given compiled code, so lookups by address will return
     * this code.
     *
     * @param cc  compiled code to register
     */
    public static void registerCode(jq_CompiledCode cc) {
        if (TRACE) System.out.println("Registering code: " + cc);
        if (lowAddress == null || cc.getStart().difference(lowAddress) < 0)
            lowAddress = cc.getStart();
        if (highAddress == null || highAddress.difference(cc.getStart().offset(cc.getLength())) < 0)
            highAddress = (CodeAddress)cc.getStart().offset(cc.getLength());
        compiledMethods.put(cc, cc);
    }
    
    /**
     * Return the compiled code which contains the given code address.
     * Returns null if there is no registered code that contains the
     * given address.
     *
     * @param ip  code address to check
     * @return  compiled code containing given address, or null
     */
    public static jq_CompiledCode getCodeContaining(CodeAddress ip) {
        InstructionPointer iptr = new InstructionPointer(ip);
        return (jq_CompiledCode) compiledMethods.get(iptr);
    }
    
    /**
     * Returns the lowest address of any registered code.
     * @return  lowest address of any registered code.
     */
    public static CodeAddress getLowAddress() { return lowAddress; }
    /**
     * Returns the highest address of any registered code.
     * @return  highest address of any registered code.
     */
    public static CodeAddress getHighAddress() { return highAddress; }

    /**
     * Returns an iterator of the registered jq_CompiledCode objects, in
     * address order.
     * @return  iterator of jq_CompiledCode objects
     */
    public static Iterator/*<jq_CompiledCode>*/ getCompiledMethods() {
        Iterator i = compiledMethods.keySet().iterator();
        i.next(); // skip bogus compiled code
        return i;
    }
    
    /**
     * Returns the number of registered jq_CompiledCode objects.
     * @return  number of registered jq_CompiledCode objects
     */
    public static int getNumberOfCompiledMethods() {
        return compiledMethods.keySet().size() - 1;  // skip bogus compiled code
    }
    
    /**
     * An object of this class represents a code address.
     * It can be compared with a jq_CompiledCode object with compareTo and
     * equals.  They are equal if the InstructionPointer points within the
     * range of the compiled code; the InstructionPointer is less if it is
     * before the start address of the compiled code; the InstructionPointer
     * is less if it is after the end address of the compiled code.
     */
    public static class InstructionPointer implements Comparable {
        
        /** The (actual) address. */
        private final CodeAddress ip;
        
        /**
         * Create a new instruction pointer.
         * @param ip  instruction pointer value
         */
        public InstructionPointer(CodeAddress ip) { this.ip = ip; }
        
        /**
         * Extract the address of this instruction pointer.
         * @return  address of this instruction pointer
         */
        public CodeAddress getIP() { return ip; }
        
        /**
         * Compare this instruction pointer to a compiled code object.
         * @param that  compiled code to compare against
         * @return  -1 if this ip comes before the given code, 0 if it is
         *           inside the given code, 1 if it is after the given code
         */
        public int compareTo(jq_CompiledCode that) {
            CodeAddress ip = this.getIP();
            CodeAddress start = that.getStart();
            if (start.difference(ip) >= 0) return -1;
            if (start.offset(that.getLength()).difference(ip) < 0) return 1;
            return 0;
        }
        
        /**
         * Compare this instruction pointer to another instruction pointer.
         * @param that  instruction pointer to compare against
         * @return  -1 if this ip is before the given ip, 0 if it is equal
         *           to the given ip, 1 if it is after the given ip
         */
        public int compareTo(InstructionPointer that) {
            if (this.ip.difference(that.ip) < 0) return -1;
            if (this.ip.difference(that.ip) > 0) return 1;
            return 0;
        }
        
        /**
         * Compares this instruction pointer to the given object
         * (InstructionPointer or jq_CompiledCode)
         * @param that  object to compare to
         * @return  -1 if this is less than, 0 if this is equal, 1 if this
         *           is greater than
         */
        public int compareTo(java.lang.Object that) {
            if (that instanceof jq_CompiledCode)
                return compareTo((jq_CompiledCode) that);
            else
                return compareTo((InstructionPointer) that);
        }
        
        /**
         * Returns true if this instruction pointer refers to a location
         * within the given compiled code, false otherwise.
         * @param that  compiled code to compare to
         * @return  true if the instruction pointer is within, false otherwise
         */
        public boolean equals(jq_CompiledCode that) {
            CodeAddress ip = this.getIP();
            CodeAddress start = that.getStart();
            if (ip.difference(start) < 0) return false;
            if (ip.difference(start.offset(that.getLength())) > 0)
                return false;
            return true;
        }
        
        /**
         * Returns true if this instruction pointer refers to the same location
         * as the given instruction pointer, false otherwise.
         * @param that  instruction pointer to compare to
         * @return  true if the instruction pointers are equal, false otherwise
         */
        public boolean equals(InstructionPointer that) {
            return this.ip.difference(that.ip) == 0;
        }
        
        /**
         * Compares this instruction pointer with the given object
         * (InstructionPointer or jq_CompiledCode).
         * @param that  object to compare with
         * @return  true if these objects are equal, false otherwise
         */
        public boolean equals(Object that) {
            if (that instanceof jq_CompiledCode)
                return equals((jq_CompiledCode) that);
            else
                return equals((InstructionPointer) that);
        }
        
        /**
         * Returns the hash code of this instruction pointer.
         * This is a really bad implementation (just returns 0), and
         * should not be counted on.
         * @return  hash code
         */
        public int hashCode() { return 0; }
        
        public static final jq_InstanceField _ip;
        static {
            jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Allocator/CodeAllocator$InstructionPointer;");
            _ip = k.getOrCreateInstanceField("ip", "I");
        }
    }
    
    public static final jq_Class _class;
    public static final jq_StaticField _lowAddress;
    public static final jq_StaticField _highAddress;
    public static final jq_StaticField _compiledMethods;
    static {
        _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Allocator/CodeAllocator;");
        _lowAddress = _class.getOrCreateStaticField("lowAddress", "Ljoeq/Memory/CodeAddress;");
        _highAddress = _class.getOrCreateStaticField("highAddress", "Ljoeq/Memory/CodeAddress;");
        _compiledMethods = _class.getOrCreateStaticField("compiledMethods", "Ljava/util/SortedMap;");
    }
}
