package org.jam.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.jam.tools.ArrayType;

public class RVMClass 
extends JObject
{
    private static final int TYPE_REF_OFFSET = 4;
    private static final int DIMENSION_OFFSET = 0x24;
    private static final int INSTANCES_ARRAY_OFFSET = 0x68;
    private TypeRef typeRef;
    private IntArray fieldInstances;
    private static final boolean DEBUG = false;
    private String typeName;
    private TreeSet<RVMField> fieldSet;
    private Map<String, RVMField> fieldMap;
    private int dimension;
    
    public RVMClass(MemoryReader memory, int address)
    {
        super(memory, address);
        typeRef = new TypeRef(memory, getInt(TYPE_REF_OFFSET));
        typeName = typeRef.getTypeName();
        dimension = getInt(DIMENSION_OFFSET);
        if(dimension > 0)
        {
            /*
             * See if type is like [[[Ljava/lang/Object;
             */
            if(typeName.charAt(dimension) == 'L')
            {
                /*
                 * Process the object descriptor
                 * 
                 * Isolate the class name
                 */
                String objDescName = typeName.substring(dimension+1, typeName.length()-1).replace('/', '.');
                /*
                 * append class name to array descriptor
                 */
                typeName = typeName.substring(0, dimension).concat(objDescName);
            }
        }
        else
        {
            fieldInstances = new IntArray(memory, getInt(INSTANCES_ARRAY_OFFSET));
            fieldSet = new TreeSet<RVMField>();
            fieldMap = new HashMap<String, RVMField>();
            if (DEBUG) System.out.println("Instances: " + Integer.toHexString(fieldInstances.getAddress()) + "/" + fieldInstances.size());
            processFieldInstances();
            // remove type descriptors and replace / with .
            typeName=typeName.substring(1, typeName.length()-1).replace('/', '.');
        }
    }
    
    private void processFieldInstances()
    {
        int[] instanceArray = fieldInstances.array();
        for(int i=0; i < fieldInstances.size(); i++)
        {
            RVMField aField = new RVMField(getMemory(), instanceArray[i]);
            fieldSet.add(aField);
            fieldMap.put(aField.getName(), aField);
        }
    }

    public String getClassName()
    {
        return typeName;
    }
    
    public boolean isArray()
    {
        return dimension > 0;
    }
    
    public ArrayType getArrayType()
    {
        ArrayType arrayType = null;
        
        char arrayDescriptor = typeName.charAt(dimension);
        if(arrayDescriptor == 'B')
        {
            arrayType = ArrayType.BYTE;
        }
        else if(arrayDescriptor == 'C')
        {
            arrayType = ArrayType.CHAR;
        }
        else if(arrayDescriptor == 'S')
        {
            arrayType = ArrayType.SHORT;
        }
        else if(arrayDescriptor == 'I')
        {
            arrayType = ArrayType.INTEGER;
        }
        else if(arrayDescriptor == 'J')
        {
            arrayType = ArrayType.LONG;
        }
        else if(arrayDescriptor == 'F')
        {
            arrayType = ArrayType.FLOAT;
        }
        else if(arrayDescriptor == 'D')
        {
            arrayType = ArrayType.DOUBLE;
        }
        else if(arrayDescriptor == 'Z')
        {
            arrayType = ArrayType.BOOLEAN;
        }
        else
        {
            arrayType = ArrayType.OBJECT;
        }
        return arrayType;
    }
    
    public Iterator<RVMField> getFieldIter()
    {
        return fieldSet.iterator();
    }
    
    public RVMField getField(String fieldName)
    {
        return fieldMap.get(fieldName);
    }
}
