package org.jam.net.inet4;

import java.util.LinkedList;

import org.jam.driver.net.InetNetworkInterface;
import org.jam.net.NetworkInterface;

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
    private NetworkInterface netIf;
    private LinkedList<Arp> arpRequests;
    
    public ArpThread(NetworkInterface networkInterface)
    {
        netIf = networkInterface;
        arpTable = new ArpTable();
        arpRequests = new LinkedList<Arp>(); // make map sendingip, arprequest
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
        synchronized(arpRequests)
        {
            arpRequests.add(arpPacket);
            arpRequests.notify();
        }
    }
    
    public void request(InetAddress senderIp, InetAddress targetIp)
    {
        Arp arpRequest = new Arp(netIf.getEthernetAddress(), senderIp, targetIp);
        System.out.println("at: arp request");
        netIf.send(arpRequest);
        Arp arpReply = null;
        try
        {
            /*
             * Wait for the reply
             */
            synchronized(arpRequests)
            {
                arpRequests.wait();
                arpReply = arpRequests.getFirst();
            }
            System.out.println("request -> Got ARP reply");
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
}
