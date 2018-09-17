package org.jam.tools;

public class MapTib
extends MapCommon
{
    final private int address;
    final private String name;
    
    public MapTib(String[] rvmMapLine)
    {
        super(Integer.parseInt(rvmMapLine[0]), Long.decode(rvmMapLine[1]).intValue());
        address = Integer.decode(rvmMapLine[3]);
        name = rvmMapLine[4];
    }

    public int getAddress()
    {
        return address;
    }

    public String getName()
    {
        return name;
    }

}
