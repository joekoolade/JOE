package org.jam.tools;

import org.jikesrvm.objectmodel.JavaHeaderConstants;
import org.jikesrvm.objectmodel.TIBLayoutConstants;

/*
 * Has stuff common to all jikes objects
 */
public class JObject 
implements TIBLayoutConstants, JavaHeaderConstants
{
    private MemoryReader memory;
    private int address;
    public static int ARRAY_LENGTH_OFFSET = -ARRAY_LENGTH_BYTES;
    public static int JAVA_HEADER_OFFSET = (ARRAY_LENGTH_OFFSET - JAVA_HEADER_BYTES);
    
    public JObject(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
    }

    public int getInt(int offset)
    {
        return memory.read(address+offset);
    }

    public byte[] getByteArray(int offset)
    {
        int arraySize = getInt(offset+ARRAY_LENGTH_OFFSET);
        byte[] byteArray = new byte[arraySize];
        for(int index=0; index < arraySize; index++)
        {
            byteArray[index] = getByte(index);
        }
        return byteArray;
    }

    public byte getByte(int index)
    {
        return memory.readByte(address+index);
    }
    
    public short getShort(int index)
    {
        return (short)memory.readChar(address+index);
    }
    
    public long getLong(int index)
    {
        return memory.readLong(address+index);
    }
    public int getAddress()
    {
        return address;
    }
    
    protected MemoryReader getMemory()
    {
        return memory;
    }
    
    public boolean isNull()
    {
        return address == 0;
    }
}
