package org.jam.net;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class RouteTest {
    
    @Before
    public void setUp() throws Exception
    {
    }

    @Test   
    public void testToPrefix()
    {
    }
    
    @Test
    public void testToNetmask()
    {
        Route route = new Route(32, null, null, null);
        assertEquals(0xFFFFFFFF, route.getNetmask());
        route = new Route(31, null, null, null);
        assertEquals(0xFFFFFFFE, route.getNetmask());
        route = new Route(1, null, null, null);
        assertEquals(0x80000000, route.getNetmask());
        route = new Route(0, null, null, null);
        assertEquals(0, route.getNetmask());
    }
    @Test
    public void testAddRouteInetAddressInetAddressIntNetworkInterface()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testAddRouteIntInetAddressInetAddressNetworkInterface()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testFind()
    {
        fail("Not yet implemented");
    }

}
