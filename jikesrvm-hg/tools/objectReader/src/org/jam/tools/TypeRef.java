package org.jam.tools;

public class TypeRef {

    private static final int ID_OFFSET = 8;
    private MemoryReader memory;
    private int address;
    private JObject name;
    private int id;
    
    public TypeRef(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
        name = new Atom(memory, address);
        id = memory.read(address+ID_OFFSET);
    }

}
