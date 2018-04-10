package org.jam.net;

import org.jam.driver.net.NetworkQueue;
import org.jam.driver.net.Packet;
import org.jam.net.ethernet.Ethernet;
import org.jam.net.inet4.Arp;
import org.jam.net.inet4.ArpPacket;

public class InetProtocolProcessor
implements Runnable
{
    private NetworkQueue rxQueue;
    
    public void run()
    {
        
    }

    private void processPackets()
    {
        while(true)
        {
            try
            {
                /*
                 * Wait for packets to be added to queue
                 */
                wait();
                Packet packet = rxQueue.get();
                if(Ethernet.isArp(packet))
                {
                    packet.pull(Ethernet.HEADER_SIZE);
                    Arp arp = new Arp(packet);
                    arp.reply();
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void put(Packet packet)
    {
        /*
         * Add packet to queue and notify
         */
        rxQueue.put(packet);
        notify();
    }
}
