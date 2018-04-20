package org.jam.net;

import org.jam.driver.net.NetworkQueue;
import org.jam.driver.net.Packet;
import org.jam.net.ethernet.Ethernet;
import org.jam.net.inet4.Arp;
import org.jam.net.inet4.ArpPacket;
import org.jam.net.inet4.ArpThread;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RVMThread;

public class InetProtocolProcessor
implements Runnable
{
    private NetworkQueue rxQueue;
    private ArpThread arp;
    private static int QUEUE_SIZE = 256;
    
    public InetProtocolProcessor(ArpThread arp)
    {
        this.arp = arp;
        rxQueue = new NetworkQueue();
    }
    public void run()
    {
        processPackets();
    }

    private void processPackets()
    {
        System.out.println("INET protocol processor started");
        while(true)
        {
            try
            {
                /*
                 * Wait for packets to be added to queue
                 */
                synchronized (this)
                {
                    wait();
                }
                System.out.println("inetpp got rx");
                Packet packet = rxQueue.get();
                if(Ethernet.isArp(packet))
                {
                    System.out.println("inetpp arp packet");
                    packet.pull(Ethernet.HEADER_SIZE);
                    Arp arp = new Arp(packet);
                    this.arp.reply(arp);
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
        VM.sysWriteln("inetpp notify");
        RVMThread.nosyncNotify(this);
    }
}
