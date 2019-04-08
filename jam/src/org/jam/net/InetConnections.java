package org.jam.net;

import java.net.InetSocketAddress;
import java.util.HashMap;

import org.jam.net.inet4.InetAddress;

/**
 * Keeps try of ipv4 connections
 * @author joe
 *
 */
public class InetConnections
{
    private HashMap<Long, Connection> table;
    
    public InetConnections()
    {
        table = new HashMap<Long, Connection>();
    }
    
    public final void add(InetSocketAddress addr)
    {
        
    }
    
    public final void remove(Connection conn)
    {
        
    }
    
    public final void add(InetAddress inetAddr, int port)
    {
        
    }
    
    public final void remove(InetAddress inetAddr, int port)
    {
        
    }
    
    public final Connection find(InetAddress inetAddr, int port)
    {
        Connection conn = null;
        
        return conn;
    }
}
