package org.jam.tools;

import java.util.Iterator;

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
    
    public Iterator<RVMField> getFields()
    {
        return type.getFieldIter();
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
        long value=0;
        System.out.println(type.getClassName());
        Iterator<RVMField> fields = getFields();
        while(fields.hasNext())
        {
            RVMField aField = fields.next();
            String name = aField.getName();
            int offset = aField.getOffset();
            switch(aField.getSize())
            {
            case 1:
                value = getByte(offset) & 0xFFL;
                break;
            case 2:
                value = getShort(offset) & 0xFFFFL;
                break;
            case 4:
                value = getInt(offset) & 0xFFFFFFFFL;
                break;
            case 8:
                value = getLong(offset);
                break;
             default:
                 System.out.println("Unknown size: "+aField.getSize());
            }
            //System.out.println(name + "("+offset + ") = " + Integer.toHexString(value));
            System.out.println(name + " = " + Long.toHexString(value));

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
