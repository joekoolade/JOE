package org.jam.driver.net;

import org.jam.net.NetworkInterface;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.InetAddress;
import org.jam.net.inet4.SendPacket;

public class LocalInetInterface
implements NetworkInterface, BufferFree
{
    private InetAddress addr;
    private int mtu;
    private int netMask;
    final static int DEFAULT_LOCAL_MTU = 16384;
    final static int DEFAULT_IP_ADDRESS = 0x7f000001;
    final static int DEFAULT_NETMASK = 0xff000000;
    
    public LocalInetInterface()
    {
        addr = new InetAddress(DEFAULT_IP_ADDRESS);
        mtu = DEFAULT_LOCAL_MTU;
        netMask = DEFAULT_NETMASK;
    }
    
    public void free(Packet packet)
    {
    }

    public InetAddress getInetAddress()
    {
        return addr;
    }

    public int getNetMask()
    {
        return netMask;
    }

    public void send(SendPacket packet)
    {
    }

    public void send(EthernetAddr destination, Packet packet, short protocol)
    {
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
        netMask = mask;
    }

    public void setInetAddress(InetAddress inetAddress)
    {
        addr = inetAddress;
    }

    public EthernetAddr arp(InetAddress inet)
    {
        return null;
    }

    public EthernetAddr getEthernetAddress()
    {
        return null;
    }

    public void setEthernetAddress(EthernetAddr macAddress)
    {
    }

    public void poll()
    {
    }

    public int work()
    {
        return 0;
    }

    public int schedule()
    {
        return 0;
    }

    public void receive()
    {
    }

}
