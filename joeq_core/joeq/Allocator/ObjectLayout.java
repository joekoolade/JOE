// ObjectLayout.java, created Mon Feb  5 23:23:19 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

/** This interface contains constants that define the joeq object layout.
 *  You can play with these constants to experiment with different object layouts.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ObjectLayout.java,v 1.11 2004/03/09 04:03:57 jwhaley Exp $
 */
public abstract class ObjectLayout {

    /**** OFFSETS ****/
    
    /** Offset of array length word, in bytes. */
    public static final int ARRAY_LENGTH_OFFSET = -12;
    /** Offset of status word, in bytes. */
    public static final int STATUS_WORD_OFFSET = -8;
    /** Offset of vtable, in bytes. */
    public static final int VTABLE_OFFSET = -4;
    /** Offset of array element 0, in bytes. */
    public static final int ARRAY_ELEMENT_OFFSET = 0;

    
    /**** HEADER SIZES ****/
    
    /** Size of (non-array) object header, in bytes. */
    public static final int OBJ_HEADER_SIZE = 8;
    /** Size of array header, in bytes. */
    public static final int ARRAY_HEADER_SIZE = 12;

    
    /**** STATUS BITS ****/
    
    /** Object has been hashed.  If it moves, we need to store the old address. */
    public static final int HASHED       = 0x00000001;
    /** Object has been hashed and later moved.  The hash code is stored just past the object. */
    public static final int HASHED_MOVED = 0x00000002;
    /** Bit in object header for use by GC. */
    public static final int GC_BIT       = 0x00000004;
    /** Mask for status flags. */
    public static final int STATUS_FLAGS_MASK = 0x00000007;

    
    /**** LOCKING ****/
    
    /** Bit location of thread id in the status word. */
    public static final int THREAD_ID_SHIFT   = 9;
    /** Mask of the thread id in the status word. */
    public static final int THREAD_ID_MASK    = 0x7FFFFE00;
    /** Mask of the lock count in the status word. */
    public static final int LOCK_COUNT_MASK   = 0x000001F0;
    /** Value to add to status word to increment lock count by one. */
    public static final int LOCK_COUNT_INC    = 0x00000010;
    /** Bit location of lock count in the status word. */
    public static final int LOCK_COUNT_SHIFT  = 4;
    /** Lock has been expanded.
     *  Masking out this value and the status flags mask gives the address of the expanded lock structure. */
    public static final int LOCK_EXPANDED     = 0x80000000;
   
}
