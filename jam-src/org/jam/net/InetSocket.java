package org.jam.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;

public class InetSocket
{
    final StandardProtocolFamily family = StandardProtocolFamily.INET;
    
    public InetSocket()
    {
    }

    public <T> T getSocketOption(SocketOption<T> name)
    {
        return null;
    }

    public int receive(long l, int rem, boolean connected)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public InetSocketAddress localAddress()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int send(boolean preferIPv6, long l, int rem, InetAddress address, int port)
    throws PortUnreachableException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int read(ByteBuffer buf, int i)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public long read(ByteBuffer[] dsts, int offset, int length)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int write(ByteBuffer buf, int i)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public long write(ByteBuffer[] srcs, int offset, int length)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void bind(InetAddress address, int port)
    {
        // TODO Auto-generated method stub
        
    }

    public int connect(InetAddress address, int port)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void disconnect()
    {
        // TODO Auto-generated method stub
        
    }
    
    
}
