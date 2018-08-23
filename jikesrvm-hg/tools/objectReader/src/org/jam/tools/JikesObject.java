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
    
    public void printObject()
    {
        System.out.println(type.getClassName());
        
    }
    public int getTIBAddress()
    {
        return tib.getAddress();
    }
    
    public RVMField[] getFields()
    {
        return type.getFields();
    }
    /**
     * Get the class layout of the object
     * @return
     */
    public RVMClass getType()
    {
        return type;
    }
    
    /**
     * Pretty print the object
     */
    public void print()
    {
        System.out.println("Type: "+type.getClassName());
        RVMField[] fields = getFields();
        for(RVMField aField: fields)
        {
            String name = aField.getName();
            
        }
    }
    
    /**
     * hex dump the object
     */
    public void dump()
    {
        
    }
    
    /**
     * Return a string representation of the object
     */
    public String toString()
    {
        return null;
    }
}
