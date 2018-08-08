package org.jam.tools;

public class TIB 
extends JObject
{
    private RVMClass rvmClass;
    private int size;

    public TIB(MemoryReader memory, int address)
    {
        super(memory, address);
        size = getInt(ARRAY_LENGTH_OFFSET);
        rvmClass = new RVMClass(memory, getInt(0));
    }

    public int getSize()
    {
        return size;
    }
    
    public RVMClass getType()
    {
        return rvmClass;
    }
}
    