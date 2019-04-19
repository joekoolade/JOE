package org.jam.net;

public class IpStats {
    private int headerError=0;
    private int checksumError=0;
    
    public void headerError()
    {
        headerError++;
    }

    public void checksumError()
    {
        checksumError++;
    }

}
