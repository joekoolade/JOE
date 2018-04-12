package org.jam.net.inet4;

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
    
    public ArpThread(NetworkInterface networkInterface)
    {
        netIf = networkInterface;
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
        
    }
    
    public void request(InetAddress senderIp, InetAddress targetIp)
    {
        Arp arpRequest = new Arp(netIf.getEthernetAddress(), senderIp, targetIp);
        arpTable.addDevice(targetIp.inet4(), arpRequest);
        netIf.send(arpRequest.getPacket());
        try
        {
            /*
             * Wait for the reply
             */
            arpRequest.wait();
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
