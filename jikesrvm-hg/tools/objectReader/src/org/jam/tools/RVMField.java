package org.jam.tools;

public class RVMField extends JObject
{
    private static final int MEMBER_REF_OFFSET = 8;
    private static final int MODIFIER_OFFSET = 0x10;
    private static final int OFFSET_OFFSET = 0x12;
    
    final private MemberReference member;
    final private int modifier;
    final private int offset;
    
    public RVMField(MemoryReader memory, int address)
    {
        super(memory, address);
        member = new MemberReference(memory, getInt(MEMBER_REF_OFFSET));
        modifier = getShort(MODIFIER_OFFSET);
        offset = getInt(OFFSET_OFFSET);
        System.out.println("modifier/offset: "+Integer.toHexString(modifier)+"/"+Integer.toHexString(offset));
    }
}
