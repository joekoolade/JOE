package org.jam.driver.net;

import org.jam.net.InetProtocolProcessor;
import org.jam.net.NetworkInterface;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.Arp;
import org.jam.net.inet4.ArpTable;
import org.jam.net.inet4.ArpThread;
import org.jam.net.inet4.InetAddress;

public class InetNetworkInterface
{

    protected InetAddress ipAddress;
    protected int netmask;
    int mtu;
    protected ArpTable arpTable;
    private NetworkInterface networkInterface;
    protected ArpThread arp;
    protected InetProtocolProcessor inet4;
    private Thread arpThread;
    private Thread inetThread;
    
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

    public EthernetAddr arp(InetAddress inet)
    {
        /*
         * The easy case. arp table has the mac address
         * then just return
         */
        if(!arpTable.hasInet(inet.inet4()))
        {
            System.out.println("Create arp request: "+inet);
            arp.request(ipAddress, inet);
        }
        /*
         * If this point is reached then the 
         * arp entry should be present
         */
        return arpTable.findDevice(inet.inet4());        
    }
    protected void setNetworkInterface(NetworkInterface networkInterface)
    {
        this.networkInterface = networkInterface;
    }
    
    public void inetBoot()
    {
        /*
         * Start the arp processing
         */
        arp = new ArpThread(networkInterface);
        arpThread = new Thread(arp);
        inet4 = new InetProtocolProcessor(arp);
        inetThread = new Thread(inet4);
        arpThread.start();
        inetThread.start();
    }
}
