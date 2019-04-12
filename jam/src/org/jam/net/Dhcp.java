package org.jam.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Random;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPPacket;
import org.jam.net.inet4.InetAddress;

public class Dhcp
implements Runnable
{
    int mode;
    int xid;
    final private static int DISCOVER_MODE = 1;
    private Random r;
    private NetworkInterface netInterface;
    private DatagramSocket socket;
    
    public Dhcp(NetworkInterface netInterface)
    {
        r = new Random();
        try
        {
            socket = new DatagramSocket();
            socket.bind(new InetSocketAddress(DHCPConstants.BOOTP_REPLY_PORT));
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.netInterface = netInterface;
    }

    final public static void discover(NetworkInterface netInterface)
    {
        Dhcp dhcpClient =new Dhcp(netInterface);
        dhcpClient.discoverMode();
        Thread dhcpThread = new Thread(dhcpClient);
        dhcpThread.setName("Dhcp Discover Client");
        dhcpThread.start();
    }

    private void discoverMode()
    {
        mode = DISCOVER_MODE;
    }

    public void run()
    {
        System.out.println("Dhcp.run");
        if(mode == DISCOVER_MODE)
        {
            runDiscover();
        }
        
    }

    final private void init()
    {
        System.out.println("Dhcp.init");
        // Send a DISCOVER message
        DHCPPacket discoverPacket = new DHCPPacket();
        xid = r.nextInt();
        discoverPacket.setXid(xid);
        discoverPacket.setDHCPMessageType(DHCPConstants.DHCPDISCOVER);
        discoverPacket.setOp(DHCPConstants.BOOTREQUEST);
        discoverPacket.setPort(DHCPConstants.BOOTP_REQUEST_PORT);
        discoverPacket.setAddress(DHCPConstants.INADDR_BROADCAST);
        discoverPacket.setChaddr(netInterface.getEthernetAddress().asArray());
        discoverPacket.setBroadcastFlag();
        byte[] packetBuffer = discoverPacket.serialize();
        DatagramPacket udpPacket = new DatagramPacket(packetBuffer, packetBuffer.length, 
                        discoverPacket.getAddress(), discoverPacket.getPort());
        try
        {
            System.out.println("Dhcp.init##");
            socket.send(udpPacket);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Dhcp.init###");
        selecting();
    }
    
    private void selecting()
    {
        // TODO Auto-generated method stub
        
    }

    private void runDiscover()
    {
        init();
    }
}
