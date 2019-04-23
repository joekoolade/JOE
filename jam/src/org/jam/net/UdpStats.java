package org.jam.net;

public class UdpStats {
    private int noPorts=0;
    private int inErrors;
    
    public void noPort()
    {
        noPorts++;
    }
    
    public void inError()
    {
        inErrors++;
    }
}
