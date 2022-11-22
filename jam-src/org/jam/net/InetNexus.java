package org.jam.net;

import org.jam.driver.net.NetworkQueue;
import org.jam.driver.net.Packet;
import org.jam.net.ethernet.Ethernet;
import org.jam.net.inet4.Arp;
import org.jam.net.inet4.ArpPacket;
import org.jam.net.inet4.ArpThread;
import org.jikesrvm.VM;
import org.jikesrvm.scheduler.RVMThread;

public class InetNexus
implements Runnable
{
    private static NetworkQueue rxQueue;
    private static ArpThread arp;
    private static int QUEUE_SIZE = 256;
    private static Ip ip;
	private static Thread arpThread;
	private static InetNexus nexus;
	private static Thread inetThread;
    
    public void run()
    {
        processPackets();
    }

	public static void boot() {
        rxQueue = new NetworkQueue();
        ip = new Ip();
		arp = new ArpThread();
		arpThread = new Thread(arp);
		arpThread.setName("ARP Thread");
		nexus = new InetNexus();
		inetThread = new Thread(nexus);
		inetThread.setName("INET Thread");
		arpThread.start();
		inetThread.start();
	}

    private void processPackets()
    {
        System.out.println("INET nexus started");
        while(true)
        {
            try
            {
                /*
                 * Wait for packets to be added to queue.
                 */
                synchronized (InetNexus.class)
                {
                	InetNexus.class.wait();
                }
                System.out.println("nexus got rx");
                Packet packet = rxQueue.get();
                if(Ethernet.isIPv4(packet))
                {
                    packet.pull(Ethernet.HEADER_SIZE);
                    ip.receive(new InetPacket(packet));
                }
                else if(packet.getPacketAddress().loadByte() >= 0x45)
                {
                	ip.receive(new InetPacket(packet));
                }
                else if(Ethernet.isArp(packet))
                {
                    System.out.println("nexus arp packet");
                    packet.pull(Ethernet.HEADER_SIZE);
                    Arp arp = new Arp(packet);
                    packet.free();
                    this.arp.reply(arp);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void put(Packet packet)
    {
        /*
         * Add packet to queue and notify processPackets(). 
         * This is called from an interrupt so there is no synchronization
         * on 'this'
         */
        rxQueue.put(packet);
        VM.sysWriteln("inetpp notify");
        RVMThread.nosyncNotify(InetNexus.class);
    }
    
    public static void receive(Packet packet)
    {
    	put(packet);
    }
}
