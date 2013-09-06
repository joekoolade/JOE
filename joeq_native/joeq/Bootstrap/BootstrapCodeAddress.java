// BootstrapCodeAddress.java, created Wed Sep 18  1:22:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import jwutil.strings.Strings;

/**
 * BootstrapCodeAddress
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BootstrapCodeAddress.java,v 1.10 2004/09/30 03:35:29 joewhaley Exp $
 */
public class BootstrapCodeAddress extends CodeAddress implements BootstrapAddress {

    public static BootstrapCodeAddressFactory FACTORY = new BootstrapCodeAddressFactory(BootstrapCodeAllocator.DEFAULT);
    
    public static class BootstrapCodeAddressFactory extends CodeAddressFactory {
        final BootstrapCodeAllocator bca;
        public BootstrapCodeAddressFactory(BootstrapCodeAllocator bca) {
            this.bca = bca;
        }
        public int size() { return 4; }
        public CodeAddress getNull() { return NULL; }
        public static final BootstrapCodeAddress NULL = new BootstrapCodeAddress(0);
    }
    
    public final int value;
    
    public BootstrapCodeAddress(int value) { this.value = value; }
    
    public Address peek() { return FACTORY.bca.peek(this); }
    public byte    peek1() { return FACTORY.bca.peek1(this); }
    public short   peek2() { return FACTORY.bca.peek2(this); }
    public int     peek4() { return FACTORY.bca.peek4(this); }
    public long    peek8() { return FACTORY.bca.peek8(this); }
    
    public void poke(Address v) { FACTORY.bca.poke(this, v); }
    public void poke1(byte v) { FACTORY.bca.poke1(this, v); }
    public void poke2(short v) { FACTORY.bca.poke2(this, v); }
    public void poke4(int v) { FACTORY.bca.poke4(this, v); }
    public void poke8(long v) { FACTORY.bca.poke8(this, v); }
    
    public Address offset(int offset) { return new BootstrapCodeAddress(value+offset); }
    public Address align(int shift) {
        int mask = (1 << shift) - 1;
        return new BootstrapCodeAddress((value+mask)&~mask);
    }
    public int difference(Address v) { return this.value - v.to32BitValue(); }
    public boolean isNull() { return value == 0; }
    
    public int to32BitValue() { return value; }
    public String stringRep() { return Strings.hex8(value); }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Bootstrap/BootstrapCodeAddress;");
    }
}
