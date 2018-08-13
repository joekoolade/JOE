package org.jam.tools;

public class RVMClass 
extends JObject
{
    private static final int TYPE_REF_OFFSET = 4;
    private static final int INSTANCES_ARRAY_OFFSET = 0x68;
    private TypeRef typeRef;
    private IntArray fieldInstances;
    private static final boolean DEBUG = false;
    
    public RVMClass(MemoryReader memory, int address)
    {
        super(memory, address);
        typeRef = new TypeRef(memory, getInt(TYPE_REF_OFFSET));
        fieldInstances = new IntArray(memory, getInt(INSTANCES_ARRAY_OFFSET));
        if(DEBUG) System.out.println("Instances: "+Integer.toHexString(fieldInstances.getAddress())+"/"+fieldInstances.size());
        processFieldInstances();
    }
    
    private void processFieldInstances()
    {
        
        
    }

    public String getClassName()
    {
        return typeRef.getTypeName();
    }
    
    
}
