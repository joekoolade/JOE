package org.jam.tools;

public class RVMClass 
extends JObject
{
    private static final int TYPE_REF_OFFSET = 4;
    private static final int INSTANCES_ARRAY_OFFSET = 0x68;
    private TypeRef typeRef;
    private IntArray fieldInstances;
    private static final boolean DEBUG = false;
    private final RVMField fields[];
    
    public RVMClass(MemoryReader memory, int address)
    {
        super(memory, address);
        typeRef = new TypeRef(memory, getInt(TYPE_REF_OFFSET));
        fieldInstances = new IntArray(memory, getInt(INSTANCES_ARRAY_OFFSET));
        fields = new RVMField[fieldInstances.size()];
        if(DEBUG) System.out.println("Instances: "+Integer.toHexString(fieldInstances.getAddress())+"/"+fieldInstances.size());
        processFieldInstances();
        
    }
    
    private void processFieldInstances()
    {
        int[] instanceArray = fieldInstances.array();
        for(int i=0; i < fieldInstances.size(); i++)
        {
            fields[i] = new RVMField(getMemory(), instanceArray[i]);
        }
        
        
    }

    public String getClassName()
    {
        return typeRef.getTypeName();
    }
    
    
}
