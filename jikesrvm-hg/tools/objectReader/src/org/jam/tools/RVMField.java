package org.jam.tools;

public class RVMField extends JObject
{
    private static final int MEMBER_REF_OFFSET = 8;
    private static final int SIGNATURE_OFFSET = 12;
    final private MemberReference member;
    final private Atom signature;
    
    public RVMField(MemoryReader memory, int address)
    {
        super(memory, address);
        member = new MemberReference(memory, getInt(MEMBER_REF_OFFSET));
        signature = new Atom(memory, getInt(SIGNATURE_OFFSET));
    }
}
