package org.jam.tools;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class RVMClass 
extends JObject
{
    private static final int TYPE_REF_OFFSET = 4;
    private static final int INSTANCES_ARRAY_OFFSET = 0x68;
    private TypeRef typeRef;
    private IntArray fieldInstances;
    private static final boolean DEBUG = false;
    private String typeName;
    private TreeSet<RVMField> fieldSet;
    
    public RVMClass(MemoryReader memory, int address)
    {
        super(memory, address);
        typeRef = new TypeRef(memory, getInt(TYPE_REF_OFFSET));
        fieldInstances = new IntArray(memory, getInt(INSTANCES_ARRAY_OFFSET));
        fieldSet = new TreeSet<RVMField>();
        if(DEBUG) System.out.println("Instances: "+Integer.toHexString(fieldInstances.getAddress())+"/"+fieldInstances.size());
        processFieldInstances();
        typeName = typeRef.getTypeName();
        // remove type descriptors and replace / with .
        typeName=typeName.substring(1, typeName.length()-1).replace('/', '.');
        System.out.println(typeName);
    }
    
    private void processFieldInstances()
    {
        int[] instanceArray = fieldInstances.array();
        for(int i=0; i < fieldInstances.size(); i++)
        {
            RVMField aField = new RVMField(getMemory(), instanceArray[i]);
            fieldSet.add(aField);
        }
    }

    public String getClassName()
    {
        return typeName;
    }
    
    public Iterator<RVMField> getFieldIter()
    {
        return fieldSet.iterator();
    }
}
