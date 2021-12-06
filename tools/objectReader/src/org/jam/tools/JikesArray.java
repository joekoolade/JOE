package org.jam.tools;

public class JikesArray extends JikesObject {

    public JikesArray(MemoryReader memory, int address)
    {
        super(memory, address);
    }

    public int getSize()
    {
        return getWord(ARRAY_LENGTH_OFFSET);
    }
    
    public int get(int index)
    {
        return getWord(index<<3);
    }
}
