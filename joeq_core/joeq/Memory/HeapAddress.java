// HeapAddress.java, created Wed Sep 18  1:22:46 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Memory;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: HeapAddress.java,v 1.12 2004/03/23 11:48:23 joewhaley Exp $
 */
public class HeapAddress extends Address {

    public static HeapAddressFactory FACTORY;

    public abstract static class HeapAddressFactory {
        public abstract int size();

        public abstract int logSize();
        
        public abstract int pageAlign();

        public abstract HeapAddress getNull();

        public abstract HeapAddress addressOf(Object o);

        public abstract HeapAddress address32(int val);
    }

    public static final int size() {
        return FACTORY.size();
    }

    public static final int logSize() {
        return FACTORY.logSize();
    }
    
    public static final int pageAlign() {
        return FACTORY.pageAlign();
    }
    
    public static final HeapAddress getNull() {
        return FACTORY.getNull();
    }

    public static final HeapAddress addressOf(Object o) {
        return FACTORY.addressOf(o);
    }

    public static final HeapAddress address32(int val) {
        return FACTORY.address32(val);
    }

    public native Object asObject();

    public native jq_Reference asReferenceType();

    public native void atomicAdd(int v);

    public native void atomicSub(int v);

    public native void atomicAnd(int v);

    public native int atomicCas4(int before, int after);

    public native long atomicCas8(long before, long after);

    public native Address peek();

    public native byte peek1();

    public native short peek2();

    public native int peek4();

    public native long peek8();

    public native void poke(Address v);

    public native void poke1(byte v);

    public native void poke2(short v);

    public native void poke4(int v);

    public native void poke8(long v);

    public native Address offset(int offset);

    public native Address align(int shift);

    public native int difference(Address v);

    public native boolean isNull();

    public native int to32BitValue();

    public native String stringRep();

    public static final jq_Class _class;
    public static final jq_StaticField _FACTORY;

    static {
        _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Memory/HeapAddress;");
        _FACTORY = _class.getOrCreateStaticField("FACTORY", "Ljoeq/Memory/HeapAddress$HeapAddressFactory;");
    }
}
