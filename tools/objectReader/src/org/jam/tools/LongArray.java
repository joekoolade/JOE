package org.jam.tools;

public class LongArray extends JObject
{
    final private long[] value;
    private int size;
    
    public LongArray(MemoryReader memory, int address)
    {
        super(memory, address);
        size = getWord(ARRAY_LENGTH_OFFSET);
        value = new long[size];
        for(int i=0; i < size; i++)
        {
            value[i] = getLong(i<<3);
        }
    }

    public long[] array()
    {
        return value;
    }
    
    public int size()
    {
        return size;
    }
}
