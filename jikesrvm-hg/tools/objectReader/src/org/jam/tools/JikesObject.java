package org.jam.tools;

public class JikesObject 
extends JObject
{
    private TIB tib;
    private RVMClass type;
    
    public JikesObject(MemoryReader memory, int address)
    {
        super(memory, address);
        tib = new TIB(memory, getInt(JAVA_HEADER_OFFSET));
        type = tib.getType();
    }
    
    public int getTIBAddress()
    {
        return tib.getAddress();
    }
    
    /**
     * Get the class layout of the object
     * @return
     */
    public RVMClass getType()
    {
        return type;
    }
}
