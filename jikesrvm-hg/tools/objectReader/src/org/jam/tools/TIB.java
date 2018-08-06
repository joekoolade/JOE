package org.jam.tools;

import org.jikesrvm.objectmodel.JavaHeaderConstants;
import org.jikesrvm.objectmodel.TIBLayoutConstants;

public class TIB 
extends JObject
{
    private RVMClass rvmClass;
    private int size;

    public TIB(MemoryReader memory, int address)
    {
        super(memory, address);
        size = getInt(ARRAY_LENGTH_OFFSET.toInt());
        rvmClass = new RVMClass(memory, address);
    }

    public int getSize()
    {
        return size;
    }
    
    public RVMClass getType()
    {
        return rvmClass;
    }
}
    