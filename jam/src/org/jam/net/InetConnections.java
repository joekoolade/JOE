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
    
    public final void add(InetSocketAddress addr, Connection connection)
    {
        byte[] inet = addr.getAddress().getAddress();
        long key = ((long)addr.getPort() << 32) | (inet[0] << 24) | (inet[1] << 16) | (inet[2] << 8) | inet[3];
        System.out.println("InetConnections add "+addr);
        table.put(key, connection);
    }
    
    public final void remove(Connection conn)
    {
        
    }
    
    public final void add(InetAddress inetAddr, int port, Connection connection)
    {
        
    }
    
    public final void remove(InetAddress inetAddr, int port, Connection connection)
    {
        
    }
    
    public final Connection find(InetAddress inetAddr, int port)
    {
        Connection conn = null;
        
        long key = ((long)port<<32)|inetAddr.inet4();
        conn = table.get(key);
        /*
         * Try any-address, 0.0.0.0
         */
        if(conn == null)
        {
            key = port<<32;
            conn = table.get(key);
        }
        if(conn != null) System.out.println("Connection found!");
        System.out.println("key "+Long.toHexString(key));
        return conn;
    }
}
