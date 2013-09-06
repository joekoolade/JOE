// BootstrapHeapAddress.java, created Wed Sep 18  1:22:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * BootstrapHeapAddress
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BootstrapHeapAddress.java,v 1.12 2004/09/30 03:35:29 joewhaley Exp $
 */
public class BootstrapHeapAddress extends HeapAddress implements BootstrapAddress {

    public static BootstrapHeapAddressFactory FACTORY = new BootstrapHeapAddressFactory(SinglePassBootImage.DEFAULT);
    
    public static class BootstrapHeapAddressFactory extends HeapAddressFactory {
        SinglePassBootImage bi;
        public BootstrapHeapAddressFactory(SinglePassBootImage bi) {
            Assert._assert(bi != null);
            this.bi = bi;
        }
        public int size() { return 4; }
        public int logSize() { return 2; }
        public int pageAlign() { return 12; }
        public HeapAddress getNull() { return NULL; }
        public HeapAddress addressOf(Object o) {
            //if (o == null) return NULL;
            return bi.getOrAllocateObject(o);
        }
        public HeapAddress address32(int v) {
            return new BootstrapHeapAddress(v);
        }
        public static final BootstrapHeapAddress NULL = new BootstrapHeapAddress(0);
    }
    
    public final int value;
    
    public BootstrapHeapAddress(int value) { this.value = value; }
    
    public Address peek() { Assert.UNREACHABLE(); return null; }
    public byte    peek1() { Assert.UNREACHABLE(); return 0; }
    public short   peek2() { Assert.UNREACHABLE(); return 0; }
    public int     peek4() { Assert.UNREACHABLE(); return 0; }
    public long    peek8() { Assert.UNREACHABLE(); return 0; }
    
    public void poke(Address v) { Assert.UNREACHABLE(); }
    public void poke1(byte v) { Assert.UNREACHABLE(); }
    public void poke2(short v) { Assert.UNREACHABLE(); }
    public void poke4(int v) { Assert.UNREACHABLE(); }
    public void poke8(long v) { Assert.UNREACHABLE(); }
    
    public Address offset(int offset) { return new BootstrapHeapAddress(value+offset); }
    public Address align(int shift) {
        int mask = (1 << shift) - 1;
        return new BootstrapHeapAddress((value+mask)&~mask);
    }
    public int difference(Address v) { return this.value - v.to32BitValue(); }
    public boolean isNull() { return value == 0; }
    
    public int to32BitValue() { return value; }
    public String stringRep() { return Strings.hex8(value); }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Bootstrap/BootstrapHeapAddress;");
    }
}
