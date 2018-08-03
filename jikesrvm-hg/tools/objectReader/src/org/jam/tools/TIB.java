package org.jam.tools;

import org.jikesrvm.objectmodel.JavaHeaderConstants;
import org.jikesrvm.objectmodel.TIBLayoutConstants;

public class TIB 
implements TIBLayoutConstants, JavaHeaderConstants
{
    private MemoryReader memory;
    private int address;
    private RVMClass rvmClass;
    private int size;

    public TIB(MemoryReader memory, int address)
    {
        this.memory = memory;
        this.address = address;
        size = memory.read(address+ARRAY_LENGTH_OFFSET.toInt());
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
    