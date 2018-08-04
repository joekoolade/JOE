package org.jam.tools;

public class Atom 
extends JObject
{
    private static final int ID_OFFSET = 4;
    private static final int VAL_OFFSET = 0;
    private int stringOrOffset;
    private int id;
    private byte[]  value;
    
    public Atom(MemoryReader memory, int address)
    {
        super(memory, address);
        id = getInt(ID_OFFSET);
        value = getByteArray(VAL_OFFSET);
    }

}
