package org.jam.tools;

public class RVMClass 
extends JObject
{
    private static final int TYPE_REF_OFFSET = 4;
    private TypeRef typeRef;
    
    public RVMClass(MemoryReader memory, int address)
    {
        super(memory, address);
        typeRef = new TypeRef(memory, address+TYPE_REF_OFFSET);
    }
}
