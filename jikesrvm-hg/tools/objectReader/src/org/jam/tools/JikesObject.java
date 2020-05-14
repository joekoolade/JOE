package org.jam.tools;

import java.util.Iterator;

public class JikesObject 
extends JObject
{
    private TIB tib;
    protected RVMClass type;
    
    public JikesObject(MemoryReader memory, int address)
    {
        super(memory, address);
        if(address!=0)
        {
            tib = new TIB(memory, getWord(JAVA_HEADER_OFFSET));
            type = tib.getType();
        }
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
    public void print(String[] args)
    {
        if(isNull()) 
        {
            System.out.println("NULL");
            return;
        }
        long value=0;
        System.out.println(type.getClassName());
        if(type.isArray())
        {
            int size = getWord(ARRAY_LENGTH_OFFSET);
            int index=0;
            if(args.length >= 3) size = CommandProcessor.parseNumber(args[2]);
            if(args.length == 4) index = CommandProcessor.parseNumber(args[3]);
            ArrayType arrayType = type.getArrayType();
            for(; index < size; index++)
            {
                switch(arrayType)
                {
                case BYTE:
                case BOOLEAN:
                    value = getByte(index) & 0xFF;
                    break;
                case SHORT:
                case CHAR:
                    value = getShort(index<<1) & 0xFFFF;
                    break;
                case INTEGER:
                case FLOAT:
                case OBJECT:
                    value = getInt(index<<2) & 0xFFFFFFFF;
                    break;
                case DOUBLE:
                case LONG:
                    value = getLong(index<<3);
                }
                System.out.println(Long.toHexString(value) + " ("+value+")");
            }
        }
        else
        {
            boolean hasField = (args.length==3);
            Iterator<RVMField> fields = getFields();
            while(fields.hasNext())
            {
                RVMField aField = fields.next();
                String name = aField.getName();
                if(hasField)
                {
                    if(name.equals(args[2]) == false) continue;
                }
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
                System.out.println(name + " = " + Long.toHexString(value));
            }

        }
    }
    
    public int getField(String fieldName)
    {
        int value = 0;
        RVMField field = type.getField(fieldName);
        if(field==null) 
        {
            value = 0;
        }
        else
        {
            value = getWord(field.getOffset());
        }
        return value;
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
    
    boolean isString()
    {
        return type.getClassName().equals("java.lang.String");
    }
}
