package org.jam.tools;

public class RVMClass {
    private MemoryReader memory;
    private int address;
    
    public RVMClass(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
    }
}
