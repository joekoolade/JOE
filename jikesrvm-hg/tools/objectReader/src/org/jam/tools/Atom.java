package org.jam.tools;

public class Atom 
extends JObject
{
    private static final int ID_OFFSET = 4;
    private static final int VAL_OFFSET = 0;
    private static final int JTOC_OR_STR_OFFSET = -4;
    private int stringOrJtoc;
    private int id;
    private byte[]  value;
    
    public Atom(MemoryReader memory, int address)
    {
        super(memory, address);
        id = getInt(ID_OFFSET);
        value = getByteArray(VAL_OFFSET);
        stringOrJtoc = getInt(JTOC_OR_STR_OFFSET);
        // do range check on *(stringOrJtoc-4)
    }

}
