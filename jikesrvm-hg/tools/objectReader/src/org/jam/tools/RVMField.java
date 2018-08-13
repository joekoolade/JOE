package org.jam.tools;

public class RVMField extends JObject
{
    private static final int MEMBER_REF_OFFSET = 8;
    private MemberReference member;
    
    public RVMField(MemoryReader memory, int address)
    {
        super(memory, address);
        member = new MemberReference(memory, getInt(MEMBER_REF_OFFSET));
    }
}
