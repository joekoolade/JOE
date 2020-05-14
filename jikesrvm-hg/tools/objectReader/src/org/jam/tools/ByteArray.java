package org.jam.tools;

public class ByteArray extends JObject
{
    final private byte[] value;
    private int size;
    
    public ByteArray(MemoryReader memory, int address)
    {
        super(memory, address);
        size = getWord(ARRAY_LENGTH_OFFSET);
        value = new byte[size];
        for(int i=0; i < size; i++)
        {
            value[i] = getByte(i);
        }
    }

    public byte[] array()
    {
        return value;
    }
}
