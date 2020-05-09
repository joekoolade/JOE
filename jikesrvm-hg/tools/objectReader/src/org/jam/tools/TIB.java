package org.jam.tools;

public class TIB 
extends JObject
{
    private RVMClass rvmClass;
    private int size;

    public TIB(MemoryReader memory, int address)
    {
        super(memory, address);
        try
        {
            size = getWord(ARRAY_LENGTH_OFFSET);
            rvmClass = new RVMClass(memory, getInt(0));
        }
        catch (Exception e)
        {
            System.err.println("TIB exception: "+Integer.toHexString(address));
            throw new RuntimeException(e);
        }
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
    