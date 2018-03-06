package org.jam.net.inet4;

import java.util.concurrent.ConcurrentHashMap;

public class ArpTable {
    private ConcurrentHashMap<Integer, Arp> table;
    
    public ArpTable()
    {
        table = new ConcurrentHashMap<Integer, Arp>();
    }
    
    public boolean hasDevice(int inet)
    {
        return false;
    }
    
    public Arp findDevice(int inet)
    {
        return null;
    }
    
    public void addDevice(int inet, byte[] mac)
    {
        
    }
}
