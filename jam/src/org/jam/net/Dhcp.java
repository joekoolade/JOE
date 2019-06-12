package org.jam.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Random;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.jam.net.inet4.InetAddress;

public class Dhcp
implements Runnable
{
    int mode;
    int xid;
    final private static int DISCOVER_MODE = 1;
    private static final boolean DEBUG_TRACE = true;
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_OPTIONS = false;
    private Random r;
    private NetworkInterface netInterface;
    private DatagramSocket socket;
    private boolean collecting;
    private java.net.InetAddress subnetMask;
    private java.net.InetAddress[] routes;
    private java.net.InetAddress[] nameservers;
    private int leaseTime;
    private byte messageType;
    private java.net.InetAddress dhcpServer;
    private java.net.InetAddress ipRequest;
    
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
        discoverPacket.setOp(DHCPConstants.BOOTREQUEST);
        discoverPacket.setHtype(DHCPConstants.HTYPE_ETHER);
        discoverPacket.setDHCPMessageType(DHCPConstants.DHCPDISCOVER);
        discoverPacket.setPort(DHCPConstants.BOOTP_REQUEST_PORT);
        discoverPacket.setAddress(DHCPConstants.INADDR_BROADCAST);
        discoverPacket.setChaddr(netInterface.getEthernetAddress().asArray());
        discoverPacket.setBroadcastFlag();
        byte[] packetBuffer = discoverPacket.serialize();
        DatagramPacket udpPacket = new DatagramPacket(packetBuffer, packetBuffer.length, 
                        discoverPacket.getAddress(), discoverPacket.getPort());
        try
        {
            if(DEBUG_TRACE) System.out.println("Dhcp.init##");
            socket.send(udpPacket);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(DEBUG_TRACE) System.out.println("Dhcp.init###");
        selecting();
    }
    
    /**
     * Processes messages in the dhcp selecting state
     */
    private void selecting()
    {
        collecting = true;
        DatagramPacket response = new DatagramPacket(new byte[DHCPConstants.MAX_LEN], DHCPConstants.MAX_LEN);
        try
        {
            while(collecting)
            {
                if(DEBUG_TRACE) System.out.println("dhcp.selecting#");
                socket.receive(response);
                if(DEBUG_TRACE) System.out.println("dhcp.selecting##");
                collector(response);

                if(isOfferMessage())
                {
                    sendRequest();
                    requesting();
                }
            }
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Find DHCPOFFERs and select one; sets collecting to false when done.
     * @param response
     */
    private void collector(DatagramPacket response)
    {
        DHCPPacket packet = DHCPPacket.getPacket(response);
        if(packet.getDHCPMessageType() == DHCPConstants.DHCPOFFER)
        {
            /*
             * Discard non-matching xid messages
             */
            if(packet.getXid() != xid)
            {
                return;
            }
            /*
             * Look for the server identifier option
             */
            if(!packet.containsOption(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER))
            {
                return;
            }
            /*
             * Process the options
             */
            processOptions(packet);
            /*
             * Requested IP address
             */
            ipRequest = packet.getYiaddr();
        }
        
    }

    /**
     * Create and send a DHCPREQUEST packet
     */
    private void sendRequest()
    {
        // Send a DHCPREQUEST packet
        DHCPPacket request = getRequestPacket();
        byte[] packetBuffer = request.serialize();
        DatagramPacket udpPacket = new DatagramPacket(packetBuffer, packetBuffer.length, request.getAddress(), request.getPort());
        try
        {
            if(DEBUG_TRACE) System.out.println("dhcp.sendRequest1");
            socket.send(udpPacket);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        if(DEBUG_TRACE) System.out.println("dhcp.sendRequest2");
        
    }

    /**
     * processes messages in the dhcp requesting state
     */
    private void requesting()
    {
        DatagramPacket response = new DatagramPacket(new byte[DHCPConstants.MAX_LEN], DHCPConstants.MAX_LEN);
        while(true)
        {
            if(DEBUG_TRACE) System.out.println("dhcp.requesting1");
            try
            {
                socket.receive(response);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            DHCPPacket packet = DHCPPacket.getPacket(response);
            if(DEBUG_TRACE) System.out.println("dhcp.requesting2 "+packet.getDHCPMessageType());
            processOptions(packet);
        }
        //if(DEBUG_TRACE) System.out.println("dhcp.requesting3");
    }
    /**
     * @return 
     * 
     */
    private DHCPPacket getRequestPacket()
    {
        DHCPPacket request = new DHCPPacket();
        request.setXid(xid);
        request.setOp(DHCPConstants.BOOTREQUEST);
        request.setHtype(DHCPConstants.HTYPE_ETHER);
        request.setDHCPMessageType(DHCPConstants.DHCPREQUEST);
        request.setPort(DHCPConstants.BOOTP_REQUEST_PORT);
        request.setAddress(DHCPConstants.INADDR_BROADCAST);
        request.setChaddr(netInterface.getEthernetAddress().asArray());
        request.setBroadcastFlag();
        request.setOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, dhcpServer);
        request.setYiaddr(ipRequest);
        DHCPOption option = DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, ipRequest);
        request.setOption(option);
        
        return request;
    }

    private boolean isOfferMessage()
    {
        return messageType == DHCPConstants.DHCPOFFER;
    }

    private void processOptions(DHCPPacket packet)
    {
        DHCPOption options[] = packet.getOptionsArray();
        for(int i=0; i < options.length; i++)
        {
            if(DEBUG_OPTIONS) System.out.println("Option "+options[i].getCode());
            int optionCode = options[i].getCode();
            if(optionCode==DHCPConstants.DHO_SUBNET_MASK)
            {
                subnetMask = options[i].getValueAsInetAddr();
                System.out.println("Subnet "+subnetMask);
            }
            else if(optionCode == DHCPConstants.DHO_ROUTERS)
            {
                routes = options[i].getValueAsInetAddrs();
                for(int route=0; route<routes.length; route++)
                {
                    System.out.println("Route "+routes[route].toString());
                }
            }
            else if(optionCode == DHCPConstants.DHO_DOMAIN_NAME_SERVERS)
            {
                nameservers = options[i].getValueAsInetAddrs();
                for(int nameServerIndex=0; nameServerIndex < nameservers.length; nameServerIndex++)
                {
                    System.out.println("Nameserver "+nameservers[nameServerIndex]);
                }
            }
            else if(optionCode == DHCPConstants.DHO_DHCP_LEASE_TIME)
            {
                leaseTime = options[i].getValueAsInt();
                System.out.println("Lease time "+leaseTime);
            }
            else if(optionCode == DHCPConstants.DHO_DHCP_MESSAGE_TYPE)
            {
                messageType = options[i].getValueAsByte();
                System.out.println("Message type "+messageType);
            }
            else if(optionCode == DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER)
            {
                dhcpServer = options[i].getValueAsInetAddr();
                System.out.println("DHCP Server "+dhcpServer);
            }
        }
    }
    
    private void runDiscover()
    {
        init();
    }
}
