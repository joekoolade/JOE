package org.jam.net.inet4;

import java.util.HashMap;
import java.util.LinkedList;

import org.jam.net.NetworkInterface;
import org.jam.net.Route;
import org.jam.net.ethernet.EthernetAddr;

/**
 * 
 * @author Joe Kulig
 * created April 9, 2018
 * Copyright 2018, All Rights Reserved
 *
 * The class will run as a thread and handle ARP requests
 */
public class ArpThread 
implements Runnable
{
    private ArpTable arpTable;
    private HashMap<Integer, Arp> arpRequests;
    private NetworkInterface nif;
    static private HashMap<NetworkInterface, ArpThread> netifArpTable;
    
    public ArpThread()
    {
        arpTable = new ArpTable();
        arpRequests = new HashMap<Integer, Arp>(); // make map sendingip, arprequest
    }

    public void run()
    {
        System.out.println("Arp Thread running!");
        while(true)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void reply(Arp arpPacket)
    {
        System.out.println("received reply packet");
        /*
         * Need to sync up on the original request packet. This will get
         * the request() restarted.
         */
        Arp request = arpRequests.get(arpPacket.targetInet());
        // Replace the request with the reply
        arpRequests.put(arpPacket.targetInet(), arpPacket);
        synchronized(request)
        {
            request.notify();
        }
    }
    
    public void setInterface(NetworkInterface netIf)
    {
    	nif = netIf;
    	if(netifArpTable==null) netifArpTable = new HashMap<>();
    	netifArpTable.put(netIf, this);
    }
    
    public void request(InetAddress senderIp, InetAddress targetIp, NetworkInterface netIf)
    {
        Arp arpRequest = new Arp(netIf.getEthernetAddress(), senderIp, targetIp);
        System.out.println("at: arp request");
        netIf.send(arpRequest);
        Arp arpReply = null;
        try
        {
            /*
             * Wait for the reply from reply()
             */
            synchronized(arpRequest)
            {
                /*
                 * the reply needs the request so it can send the notify
                 */
                arpRequests.put(senderIp.inet4(), arpRequest);
                arpRequest.wait();
            }
            System.out.println("request -> Got ARP reply");
            /*
             * get the reply
             */
            arpReply = arpRequests.get(senderIp.inet4());
            /*
             * Verify the packet
             */
            if(!arpReply.verifyEthernet() || !arpReply.verifyIpv4()) 
            {
                // ignore packet
                System.out.println("ARP is not ethernet or inet");
                return;
            }
            /*
             * See if it is for me
             */
            if(arpReply.targetInet() != netIf.getInetAddress().inet4() || arpReply.senderInet() != targetIp.inet4())
            {
                // ignore packet
                System.out.println("ARP is not for me");
            }
            /*
             * Put it into the arp table
             */
            arpTable.addDevice(arpReply.senderInet(), arpReply.senderMac());
        }
        catch (IllegalMonitorStateException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
    }

    public static EthernetAddr arp(NetworkInterface netif, InetAddress inet)
    {
    	ArpThread arp = netifArpTable.get(netif);
    	return arp.arp(inet);
    }
    
    public EthernetAddr arp( InetAddress inet)
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
            if((inet.inet4() & nif.getNetMask()) != (inet.inet4() & nif.getNetMask()))
            {
                inet = route.getGateway();
            }
            System.out.println("Create arp request: "+inet);
            request(nif.getInetAddress(), inet, nif);
        }
        /*
         * If this point is reached then the 
         * arp entry should be present
         */
        System.out.println("arp.findDevice");
        return arpTable.findDevice(inet.inet4());        
    }

    public EthernetAddr findDevice(int inet)
    {
        return arpTable.findDevice(inet);
    }
}
