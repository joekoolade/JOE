package org.jam.net;

public class IpStats {
    private int headerError=0;
    private int checksumError=0;
    private int truncated=0;        // truncated packet; packet lenght < ip len
    
    public void headerError()
    {
        headerError++;
    }

    public void checksumError()
    {
        checksumError++;
    }

    public void truncated()
    {
        truncated++;
    }

}
