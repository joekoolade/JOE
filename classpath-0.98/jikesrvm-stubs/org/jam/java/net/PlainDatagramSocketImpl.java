package org.jam.java.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

import org.jam.net.Udp;

public class PlainDatagramSocketImpl extends DatagramSocketImpl {

    public PlainDatagramSocketImpl(DatagramSocket datagramSocket) throws IOException
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object getOption(int optID) throws SocketException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void create() throws SocketException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void bind(int lport, InetAddress laddr) throws SocketException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void send(DatagramPacket p) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected int peek(InetAddress i) throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int peekData(DatagramPacket p) throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void receive(DatagramPacket p) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void setTTL(byte ttl) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected byte getTTL() throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void setTimeToLive(int ttl) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected int getTimeToLive() throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void join(InetAddress inetaddr) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void leave(InetAddress inetaddr) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void close()
    {
        // TODO Auto-generated method stub
        
    }

    protected Udp getChannel() { return null; }

}
