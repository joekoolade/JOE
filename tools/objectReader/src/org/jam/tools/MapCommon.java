package org.jam.tools;

public class MapCommon {
    final private int slot;
    final private int offset;
    
    protected MapCommon(int slot, int offset)
    {
        this.slot = slot;
        this.offset = offset;
    }
    
    protected int getSlot()
    {
        return slot;
    }
    
    protected int getOffset()
    {
        return offset;
    }
}
