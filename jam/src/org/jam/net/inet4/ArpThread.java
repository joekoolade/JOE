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
    private LinkedList<Arp> arpReplies;
    
    public ArpThread(NetworkInterface networkInterface)
    {
        netIf = networkInterface;
        arpTable = new ArpTable();
        arpReplies = new LinkedList<Arp>();
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
        synchronized(arpReplies)
        {
            arpReplies.add(arpPacket);
            arpReplies.notify();
        }
    }
    
    public void request(InetAddress senderIp, InetAddress targetIp)
    {
        Arp arpRequest = new Arp(netIf.getEthernetAddress(), senderIp, targetIp);
        System.out.println("at: arp request");
        arpTable.addDevice(targetIp.inet4(), arpRequest);
        netIf.send(arpRequest);
        Arp arpReply = null;
        try
        {
            /*
             * Wait for the reply
             */
            synchronized(arpReplies)
            {
                arpReplies.wait();
                arpReply = arpReplies.getFirst();
            }
            System.out.println("request -> Got ARP reply");
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
