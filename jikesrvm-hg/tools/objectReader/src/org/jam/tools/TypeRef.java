package org.jam.tools;

public class TypeRef 
extends JObject 
{

    private static final int ID_OFFSET = 8;
    private Atom name;
    private int id;
    
    public TypeRef(MemoryReader memory, int address)
    {
        super(memory, address);
        name = new Atom(memory, getInt(0));
        id = getInt(ID_OFFSET);
    }

}
