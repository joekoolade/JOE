package org.jam.driver.net;

import org.jam.net.NetworkInterface;
import org.jam.net.inet4.Arp;
import org.jam.net.inet4.ArpTable;
import org.jam.net.inet4.InetAddress;

public class InetNetworkInterface
{

    protected InetAddress ipAddress;
    protected int netmask;
    int mtu;
    protected ArpTable arpTable;
    private NetworkInterface networkInterface;
    
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

    public void arp(InetAddress inet)
    {
        /*
         * The easy case. arp table has the mac address
         * then just return
         */
        if(arpTable.hasInet(inet.inet4()))
        {
            System.out.println("arp have inet: " + inet);
            return;
        }
        /*
         * Lets generate the ARP request
         */
        System.out.println("Create arp request: "+inet);
        Arp arpRequest = Arp.request(networkInterface.getEthernetAddress(), ipAddress, inet);
        System.out.println("Send arp request");
        networkInterface.send(arpRequest);
    }
    protected void setNetworkInterface(NetworkInterface networkInterface)
    {
        this.networkInterface = networkInterface;
    }
}
