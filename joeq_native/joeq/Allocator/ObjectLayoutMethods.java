// ObjectLayoutMethods.java, created Mon Dec 16 22:24:22 2002 by mcmartin
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Allocator;

import joeq.Memory.HeapAddress;
import joeq.Runtime.Unsafe;

/** This interface contains utility functions for the joeq object layout.
 *  You can play with these constants to experiment with different object layouts.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ObjectLayoutMethods.java,v 1.4 2004/03/09 21:57:34 jwhaley Exp $
 */
public abstract class ObjectLayoutMethods {
    public static Object initializeObject(HeapAddress addr, Object vtable, int size) {
        addr = (HeapAddress) addr.offset(ObjectLayout.OBJ_HEADER_SIZE);
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        return addr.asObject();
    }
    
    public static Object initializeArray(HeapAddress addr, Object vtable, int length, int size) {
        addr = (HeapAddress) addr.offset(ObjectLayout.ARRAY_HEADER_SIZE);
        addr.offset(ObjectLayout.ARRAY_LENGTH_OFFSET).poke4(length);
        addr.offset(ObjectLayout.VTABLE_OFFSET).poke(HeapAddress.addressOf(vtable));
        return addr.asObject();
    }
    
    public static int getArrayLength(Object obj) {
        HeapAddress addr = HeapAddress.addressOf(obj);
        return addr.offset(ObjectLayout.ARRAY_LENGTH_OFFSET).peek4();
    }
    
    public static void setArrayLength(Object obj, int newLength) {
        HeapAddress addr = HeapAddress.addressOf(obj);
        addr.offset(ObjectLayout.ARRAY_LENGTH_OFFSET).poke4(newLength);
    }
    
    public static Object getVTable(Object obj) {
        HeapAddress addr = HeapAddress.addressOf(obj);
        return ((HeapAddress) addr.offset(ObjectLayout.VTABLE_OFFSET).peek()).asObject();
    }
    
    public static boolean testAndMark(Object obj, int markValue) {
        HeapAddress addr = (HeapAddress) HeapAddress.addressOf(obj).offset(ObjectLayout.STATUS_WORD_OFFSET);
        for (;;) {
            int oldValue = addr.peek4();
            int newValue = (oldValue & ~ObjectLayout.GC_BIT) | markValue;
            if (oldValue == newValue)
                return false;
            addr.atomicCas4(oldValue, newValue);
            if (Unsafe.isEQ())
                break;
        }
        return true;
    }
    
    public static boolean testMarkBit(Object obj, int markValue) {
        HeapAddress addr = (HeapAddress) HeapAddress.addressOf(obj).offset(ObjectLayout.STATUS_WORD_OFFSET);
        int value = addr.peek4();
        return (value & ObjectLayout.GC_BIT) == markValue;
    }
    
    public static void writeMarkBit(Object obj, int markValue) {
        HeapAddress addr = (HeapAddress) HeapAddress.addressOf(obj).offset(ObjectLayout.STATUS_WORD_OFFSET);
        int oldValue = addr.peek4();
        int newValue = (oldValue & ~ObjectLayout.GC_BIT) | markValue;
        addr.poke4(newValue);
    }
}
