package org.jam.tools;

/*
 * Has stuff common to all jikes objects
 */
public class JObject {
    private MemoryReader memory;
    private int address;

    public JObject(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
    }

    public int getInt(int offset)
    {
        return memory.read(offset);
    }

    public byte[] getByteArray(int offset)
    {
        return null;
    }
}
