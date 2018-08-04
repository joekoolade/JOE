package org.jam.tools;

public class JikesObject {
    private MemoryReader memory;
    private int address;
    private TIB tib;
    
    public JikesObject(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
        tib = new TIB(memory, address-12);
    }
}
