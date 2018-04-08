package org.jam.net;

import org.jam.driver.net.NetworkQueue;
import org.jam.driver.net.Packet;
import org.jam.net.ethernet.Ethernet;

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
                wait();
                Packet packet = rxQueue.get();
                if(Ethernet.isArp(packet))
                {
                    
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void put(Packet packet)
    {
        rxQueue.put(packet);
        notify();
    }
}
