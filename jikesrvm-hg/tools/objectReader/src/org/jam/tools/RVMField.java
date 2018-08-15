package org.jam.tools;

public class RVMField extends JObject
{
    private static final int MEMBER_REF_OFFSET = 8;
    private static final int MODIFIER_OFFSET = 0x10;
    private static final int SIZE_OFFSET = 0x12;
    private static final int REF_OFFSET = 0x13;
    private static final int OFFSET_OFFSET = 0x14;
    private static final boolean DEBUG = false;
    
    final private MemberReference member;
    final private int modifier;
    final private int offset;
    final private byte size;
    final private byte reference;
    
    public RVMField(MemoryReader memory, int address)
    {
        super(memory, address);
        member = new MemberReference(memory, getInt(MEMBER_REF_OFFSET));
        modifier = getShort(MODIFIER_OFFSET);
        offset = getInt(OFFSET_OFFSET);
        size = getByte(SIZE_OFFSET);
        reference = getByte(REF_OFFSET);
        if(DEBUG) System.out.println("modifier/offset: "+Integer.toHexString(modifier)+"/"+Integer.toHexString(offset)+"/"+size+"/"+reference+"@"+Integer.toHexString(getAddress()));
    }
    
    public String getName()
    {
        return member.getName();
    }
    
    public String getType()
    {
        return member.getTypeName();
    }
    
    public int getOffset()
    {
        return offset;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public boolean isReference()
    {
        return reference != 0;
    }
}
