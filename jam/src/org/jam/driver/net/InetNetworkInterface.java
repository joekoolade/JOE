package org.jam.driver.net;

import org.jam.net.Dhcp;
import java.net.UnknownHostException;

import org.jam.board.pc.Platform;
import org.jam.net.InetProtocolProcessor;
import org.jam.net.NetworkInterface;
import org.jam.net.Route;
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
         * The easiest case. The address is broadcast or host
         */
        if(inet.isBroadcast() || inet.isHost())
        {
            return EthernetAddr.BROADCAST_ADDRESS;
        }
        /*
         * The easy case. arp table has the mac address
         * then just return
         */
        if(!arpTable.hasInet(inet.inet4()))
        {
            Route route = Route.find(inet);
            // ARP for the address or the gateway
            if((ipAddress.inet4() & getNetMask()) != (inet.inet4() & getNetMask()))
            {
                inet = route.getGateway();
            }
            System.out.println("Create arp request: "+inet);
            arp.request(ipAddress, inet);
        }
        /*
         * If this point is reached then the 
         * arp entry should be present
         */
        System.out.println("arp.findDevice");
        return arp.findDevice(inet.inet4());        
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
        arpThread.setName("ARP Thread");
        inet4 = new InetProtocolProcessor(arp);
        /*
         * Setup the route
         */
//        try
//        {
//            Route.addRoute(0, new InetAddress("0.0.0.0"), new InetAddress("10.0.2.2"), Platform.net);
//        } catch (UnknownHostException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        inetThread = new Thread(inet4);
        inetThread.setName("INET Thread");
        arpThread.start();
        inetThread.start();
        Dhcp.discover(networkInterface);
    }
}
