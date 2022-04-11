package org.jam.tools;

public class RVMField 
extends JObject
implements Comparable<RVMField>
{
    private static final int MEMBER_REF_OFFSET = 0x18;
    private static final int SIGNATURE_OFFSET = 0x20;
    private static final int MODIFIER_OFFSET = 0x28;
    private static final int SIZE_OFFSET = 0x2a;
    private static final int REF_OFFSET = 0x2b;
    private static final int OFFSET_OFFSET = 0x2c;
    private static final boolean DEBUG = false;
    
    final private MemberReference member;
    final private int modifier;
    final private int offset;
    final private byte size;
    final private byte reference;
    final private Atom signature;
    
    public RVMField(MemoryReader memory, int address)
    {
        super(memory, address);
        member = new MemberReference(memory, getWord(MEMBER_REF_OFFSET));
        modifier = getShort(MODIFIER_OFFSET);
        offset = getInt(OFFSET_OFFSET);
        size = getByte(SIZE_OFFSET);
        reference = getByte(REF_OFFSET);
        signature = new Atom(memory, getWord(SIGNATURE_OFFSET));
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
    public String getSignature()
    {
        return signature.getString();
    }
    public boolean isReference()
    {
        return reference != 0;
    }

    public int compareTo(RVMField aField)
    {
        return offset - aField.offset;
    }
    
    public boolean isString()
    {
        return member.isString();
    }
}
