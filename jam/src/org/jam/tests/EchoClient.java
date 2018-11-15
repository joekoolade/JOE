package org.jam.tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public final class EchoClient
implements Runnable
{
    private DatagramSocket socket;
    final private int SIZE = 128;
    
    public EchoClient() throws SocketException, UnknownHostException
    {
        socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("10.0.2.2"), 7);
    }
    public void run()
    {
        System.out.println("DNS options");
        System.setProperty("dnsjava.options", "verbose,verbosemsg");
        System.setProperty("dns.server", "8.8.8.8");
        try
        {
            System.out.println("DNS requests");
            java.net.InetAddress addr = org.xbill.DNS.Address.getByName("google.com");
            System.out.println("request "+addr);
            addr = org.xbill.DNS.Address.getByName("viasat.com");
            System.out.println("request "+addr);
        }
        catch (UnknownHostException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("Echo Client running");
        byte[] data = new byte[SIZE];
        for(int i=0; i < data.length; i++)
        {
            data[i] = (byte)'0';
        }
        DatagramPacket packet = new DatagramPacket(data, data.length, socket.getInetAddress(), socket.getPort());
        try
        {
            socket.send(packet);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
