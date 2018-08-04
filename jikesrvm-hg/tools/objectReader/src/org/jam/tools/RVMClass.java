package org.jam.tools;

public class RVMClass {
    private static final int TYPE_REF_OFFSET = 8;
    private MemoryReader memory;
    private int address;
    private TypeRef typeRef;
    
    public RVMClass(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
        
        typeRef = new TypeRef(memory, address+TYPE_REF_OFFSET);
    }
}
