package org.jam.tools;

public class MemberReference extends JObject
{

    private final static int TYPE_REF_OFFSET = -4;
    private final static int NAME_OFFSET = 0;
    private final static int DESCRIPTOR_OFFSET = 4;
    
    public MemberReference(MemoryReader memory, int address)
    {
        super(memory, address);
    }
    
}
