package org.jam.tools;

public class MemberReference extends JObject
{

    private final static int TYPE_REF_OFFSET = -8;
    private final static int NAME_OFFSET = 0;
    private final static int DESCRIPTOR_OFFSET = 8;
    private static final boolean DEBUG = false;
    
    private final TypeRef type;
    private final Atom name;
    private final Atom descriptor;
    
    public MemberReference(MemoryReader memory, int address)
    {
        super(memory, address);
        type = new TypeRef(memory, getWord(TYPE_REF_OFFSET));
        name = new Atom(memory, getWord(NAME_OFFSET));
        descriptor = new Atom(memory, getWord(DESCRIPTOR_OFFSET));
        if(DEBUG) System.out.println(type.getTypeName()+"^"+name.getString()+"^"+descriptor.getString());
        
    }
    
    public String getName()
    {
        return name.getString();
    }
    
    public String getTypeName()
    {
        return type.getTypeName();
    }
}
