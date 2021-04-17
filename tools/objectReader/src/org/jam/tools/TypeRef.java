package org.jam.tools;

/**
 * Access jikesRVM TypeRef in memory
 * 
 * @author jkulig
 *
 */
public class TypeRef 
extends JObject 
{

    private static final int ID_OFFSET = 0x10;
    private static final int NAME_OFFSET = 0;
    private Atom name;
    private int id;
    
    public TypeRef(MemoryReader memory, int address)
    {
        super(memory, address);
        name = new Atom(memory, getWord(NAME_OFFSET));
        id = getInt(ID_OFFSET);
    }

    public String getTypeName()
    {
        return name.getString();
    }
    
    public boolean isArray()
    {
        return name.getByte(0)=='[';
    }
}
