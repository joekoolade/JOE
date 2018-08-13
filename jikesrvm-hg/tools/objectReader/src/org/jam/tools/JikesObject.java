package org.jam.tools;

public class JikesObject 
extends JObject
{
    private TIB tib;
    
    public JikesObject(MemoryReader memory, int address)
    {
        super(memory, address);
        tib = new TIB(memory, getInt(JAVA_HEADER_OFFSET));
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
        return tib.getType();
    }
}
