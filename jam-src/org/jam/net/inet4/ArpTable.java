package org.jam.net.inet4;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jam.net.ethernet.EthernetAddr;

public class ArpTable {
    private HashMap<Integer, EthernetAddr> table;
    
    public ArpTable()
    {
        table = new HashMap<Integer, EthernetAddr>();
    }
    
    public boolean hasDevice(int inet)
    {
        return hasInet(inet);
    }
    
    public EthernetAddr findDevice(int inet)
    {
        System.out.println("Finding arp dev "+Integer.toHexString(inet));
        return table.get(inet);
    }
    
    public boolean hasInet(int inet)
    {
        return table.containsKey(inet);
    }

    public void addDevice(int senderInet, byte[] senderMac)
    {
        EthernetAddr mac = new EthernetAddr(senderMac);
        table.put(senderInet, mac);
        System.out.println("arptable added " + Integer.toHexString(senderInet));
    }
}
