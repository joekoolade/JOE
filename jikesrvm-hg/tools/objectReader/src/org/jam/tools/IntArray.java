package org.jam.tools;

public class IntArray extends JObject
{
    final private int[] value;
    private int size;
    
    public IntArray(MemoryReader memory, int address)
    {
        super(memory, address);
        size = getInt(ARRAY_LENGTH_OFFSET);
        value = new int[size];
        for(int i=0; i < size; i++)
        {
            value[i] = getByte(i);
        }
    }

    public int[] array()
    {
        return value;
    }
    
    public int size()
    {
        return size;
    }
}
