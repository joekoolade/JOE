package org.jam.java.net;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;
import java.net.InetAddress;

import org.jam.net.Connection;
import org.jam.net.InetConnections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
public class InetConnectionsTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testInetConnections()
    {
        InetConnections conn = new InetConnections();
        assertNotNull(Whitebox.getInternalState(conn, HashMap.class));
    }

    @Test
    public void testAddInetSocketAddressConnection() throws IllegalArgumentException, UnknownHostException
    {
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("1.1.1.1"), 64);
        InetConnections inetConnection = new InetConnections();
        Connection udpConnection = createMock(Connection.class);
        inetConnection.add(addr, udpConnection);
        HashMap<Long,Connection> map = (HashMap<Long, Connection>)Whitebox.getInternalState(inetConnection, HashMap.class);
        Set<Long> keys = map.keySet();
        assertEquals(1, keys.size());
        Long[] longs = keys.toArray(new Long[0]);
        assertEquals(0x4001010101L, longs[0].longValue());
        
        org.jam.net.inet4.InetAddress inetAddr = new org.jam.net.inet4.InetAddress(0x01010101);
        assertEquals(udpConnection, inetConnection.find(inetAddr, 64));
    }

    @Test
    public void testRemoveConnection()
    {
        InetConnections inetConnection = new InetConnections();
        inetConnection.remove(null);
    }

    @Test
    public void testAddInetAddressIntConnection()
    {
        InetConnections inetConnection = new InetConnections();
        inetConnection.add(null, 0, null);
    }

    @Test
    public void testRemoveInetAddressIntConnection()
    {
        InetConnections inetConnection = new InetConnections();
        inetConnection.remove(null, 0, null);
    }

}
