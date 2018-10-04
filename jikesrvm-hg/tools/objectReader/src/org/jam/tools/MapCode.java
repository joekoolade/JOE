package org.jam.tools;

public class MapCode
extends MapCommon
{

    private String parameters;
    private String fullDefiningType;    // class the method is declared in
    private String keyDefiningType;
    private String name;                // name of method
    private Integer codeAddress;

    public MapCode(String[] rvmMapLine)
    {
        super(Integer.parseInt(rvmMapLine[0]), Long.decode(rvmMapLine[1]).intValue());
        parseDetail(rvmMapLine[4]);
        codeAddress = Integer.decode(rvmMapLine[3]);
    }

    public MapCode(Integer address, String details)
    {
        super(0, 0);
        parseDetail(details);
        codeAddress = address;
    }
    
    private void parseDetail(String details)
    {
        String[] tokens = details.substring(2).split("[<>\\. ]+");
        parameters = getClass(tokens[3]);
        fullDefiningType = getClass(tokens[1]);
        keyDefiningType = fullDefiningType.substring(fullDefiningType.lastIndexOf('.')+1);
        name = tokens[2];
    }

    private String getClass(String typeString)
    {
        int index=0;
        
        if(typeString.charAt(index)!='L') return typeString;
        return typeString.substring(index+1, typeString.lastIndexOf(';')).replace('/', '.');
    }

    public Integer getAddress()
    {
        return codeAddress;
    }

    public String getName()
    {
        return name;
    }
    
    public String getFullTypeName()
    {
        return fullDefiningType;
    }

    public String getTypeName()
    {
        return keyDefiningType;
    }
    
    public String getParameters()
    {
        return parameters;
    }
}
