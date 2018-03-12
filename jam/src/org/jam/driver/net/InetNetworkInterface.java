package org.jam.driver.net;

import org.jam.net.NetworkInterface;
import org.jam.net.inet4.ArpTable;
import org.jam.net.inet4.InetAddress;

public class InetNetworkInterface
{

    protected InetAddress ipAddress;
    protected int netmask;
    int mtu;
    protected ArpTable arpTable;
    private NetworkInterface networkInterface;
    
    public InetNetworkInterface(NetworkInterface networkInterface)
    {
        this.networkInterface = networkInterface;
    }
    public InetAddress getInetAddress()
    {
        return ipAddress;
    }

    public int getNetMask()
    {
        return netmask;
    }

    public int getMtu()
    {
        return mtu;
    }

    public void setMtu(int mtu)
    {
        this.mtu = mtu;
    }

    public void setNetMask(int mask)
    {
        netmask = mask;
    }

    public void setInetAddress(InetAddress inetAddress)
    {
        ipAddress = inetAddress;
    }

    public void arp(int inet)
    {
        /*
         * The easy case. arp table has the mac address
         * then just return
         */
        if(arpTable.hasInet(inet))
        {
            return;
        }
    }

}
