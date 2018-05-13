package org.jam.net;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jam.net.inet4.InetAddress;
import org.junit.Before;
import org.junit.Test;

public class RouteTest {
    Method toNetmaskMethod;
    Method toPrefixMethod;
    Field routeTableField;
    
    @Before
    public void setUp() throws Exception
    {
    		makeToNetmaskMethodAccessible();
    		makeToPrefixMethodAccessible();
    		makeRouteTablePublic();
    		clearRouteTable();
    }

    @SuppressWarnings("unchecked")
    private void clearRouteTable() throws IllegalAccessException
    {
        ((ArrayList<Route>)routeTableField.get(null)).clear();
    }

    private void makeRouteTablePublic() throws NoSuchFieldException
    {
        routeTableField = Route.class.getDeclaredField("routeTable");
        routeTableField.setAccessible(true);
    }

    private void makeToPrefixMethodAccessible() throws NoSuchMethodException
    {
        toPrefixMethod = Route.class.getDeclaredMethod("toPrefix", int.class);
        toPrefixMethod.setAccessible(true);
    }

    private void makeToNetmaskMethodAccessible() throws NoSuchMethodException
    {
        toNetmaskMethod = Route.class.getDeclaredMethod("toNetmask", int.class);
        toNetmaskMethod.setAccessible(true);
    }

    @Test   
    public void testToPrefix() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
    		Route route = new Route();
    		int actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFFF);
    		assertEquals(32, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFFE);
    		assertEquals(31, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFF00);
    		assertEquals(24, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFF0000);
    		assertEquals(16, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFF000000);
    		assertEquals(8, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFFC);
    		assertEquals(30, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFF8);
    		assertEquals(29, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFF0);
    		assertEquals(28, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFE0);
    		assertEquals(27, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFFC0);
    		assertEquals(26, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFF80);
    		assertEquals(25, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFE00);
    		assertEquals(23, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFFC00);
    		assertEquals(22, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFF800);
    		assertEquals(21, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFF000);
    		assertEquals(20, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFE000);
    		assertEquals(19, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFFC000);
    		assertEquals(18, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFF8000);
    		assertEquals(17, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFE0000);
    		assertEquals(15, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFFC0000);
    		assertEquals(14, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFF80000);
    		assertEquals(13, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFF00000);
    		assertEquals(12, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFE00000);
    		assertEquals(11, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFFC00000);
    		assertEquals(10, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFF800000);
    		assertEquals(9, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFE000000);
    		assertEquals(7, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xFC000000);
    		assertEquals(6, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xF8000000);
    		assertEquals(5, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xF0000000);
    		assertEquals(4, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xE0000000);
    		assertEquals(3, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0xC0000000);
    		assertEquals(2, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0x80000000);
    		assertEquals(1, actual);
    		actual = (Integer)toPrefixMethod.invoke(route, 0);
    		assertEquals(0, actual);
    		try
    		{
        		actual = (Integer)toPrefixMethod.invoke(route, 33);
        		fail("Inavlid netmask");
    		}
    		catch(InvocationTargetException e)
    		{
    			assertTrue(true);
    		}
    }
    
    @Test
    public void testToNetmask() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
    		Route route = new Route();
    		int actual = (Integer)toNetmaskMethod.invoke(route, 32);
    		assertEquals(0xFFFFFFFF, actual);
    		actual = (Integer)toNetmaskMethod.invoke(route, 31);
    		assertEquals(0xFFFFFFFE, actual);
    		actual = (Integer)toNetmaskMethod.invoke(route, 1);
    		assertEquals(0x80000000, actual);
    		actual = (Integer)toNetmaskMethod.invoke(route, 0);
    		assertEquals(0, actual);
    		try
    		{
    			toNetmaskMethod.invoke(route, -1);
    			fail("No exception thrown");
    		}
    		catch(InvocationTargetException e)
    		{
    			assertTrue(true);
    		}
    		try
    		{
    			toNetmaskMethod.invoke(route, 33);
    			fail("No exception thrown");
    		}
    		catch(InvocationTargetException e)
    		{
    			assertTrue(true);
    		}
    }
    @Test
    public void testAddRouteInetAddressInetAddressIntNetworkInterface()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testAddRouteWithPrefix() throws IllegalArgumentException, IllegalAccessException
    {
        Route.addRoute(30, null, null, null);
        Route.addRoute(32, null, null, null);
        Route.addRoute(31, null, null, null);
        Route.addRoute(20, null, null, null);
        ArrayList<Route> routeTable = (ArrayList<Route>)routeTableField.get(null);
        assertEquals(4, routeTable.size());
        assertEquals(32, routeTable.get(0).getPrefix());
        assertEquals(31, routeTable.get(1).getPrefix());
        assertEquals(30, routeTable.get(2).getPrefix());
        assertEquals(20, routeTable.get(3).getPrefix());
    }

    @Test
    public void testFind() throws UnknownHostException
    {
        InetAddress gw = new InetAddress("10.0.2.2");
        // Just mock this class
        NetworkInterface netIf = mock(NetworkInterface.class);
        // Add default route: *  -> 10.0.2.2 netIf
        Route.addRoute(0, InetAddress.DEFAULT, gw, netIf);
        // route 10.0.0.1
        InetAddress testAddress = new InetAddress("10.0.0.1");
        Route route = Route.find(testAddress);
        assertEquals(netIf, route.getNetworkIf());
        assertEquals(0, route.getNetmask());
        assertEquals(0, route.getPrefix());
        assertEquals(gw, route.getGateway());
        
        testAddress = new InetAddress("200.0.10.10");
        route = Route.find(testAddress);
        assertEquals(netIf, route.getNetworkIf());
        assertEquals(0, route.getNetmask());
        assertEquals(0, route.getPrefix());
        assertEquals(gw, route.getGateway());
    }

}
