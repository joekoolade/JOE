package org.jam.net;

import org.jam.net.inet4.InetAddress;
import org.jam.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.ListIterator;

public class Route {
	NetworkInterface networkIf;
	InetAddress destination;
	InetAddress gateway;
	int netmask;
	int metric;
	int prefix;
	
	private static ArrayList<Route> routeTable = new ArrayList<Route>();
	
	public Route(InetAddress address) throws NoRouteToHostException 
	{
	}

	public Route(NetworkInterface net)
	{
	    networkIf = net;
	    destination = net.getInetAddress();
	    netmask = net.getNetMask();
	    prefix=toPrefix(netmask);
	}
	
	public Route(int prefix, InetAddress destination, InetAddress gateway, NetworkInterface net)
    {
	    this.prefix = prefix;
	    netmask = toNetmask(prefix);
	    this.destination = destination;
	    this.gateway = gateway;
	    this.networkIf = net;
    }

	public Route(InetAddress destination, int netmask, InetAddress gateway, NetworkInterface net)
	{
	    this.netmask = netmask;
	    prefix = toPrefix(netmask);
	    this.destination = destination;
	    this.gateway = gateway;
	    this.networkIf = net;
	}
	public Route() 
	{
	}

	/**
	 * Convert a cidr prefix to a netmask value
	 * @return netmask
	 */
	final private int toNetmask(int prefix)
	{
	    int netmask;
	    
	    if(prefix < 0 || prefix > 32) throw new RuntimeException("Invalid prefix " + prefix);
	    netmask = 0x80000000;
	    if(prefix==0) netmask=0;
	    else
	    {
	        netmask = (netmask>>(prefix-1));
	    }
	    return netmask;
	}
    /**
	 * Convert netmask to a cidr prefix
	 * @return prefix
	 */
    final private int toPrefix(int netmask)
    {
        int prefix=0;
        
        if(netmask==0xFFFFFFFF) prefix=32;
        else if(netmask==0) prefix=0;
        else if(netmask==0xFFFFFF00) prefix=24;
        else if(netmask==0xFFFF0000) prefix=16;
        else if(netmask==0xFF000000) prefix=8;
        else if(netmask==0xFFFFFFFE) prefix=31;
        else if(netmask==0xFFFFFFFC) prefix=30;
        else if(netmask==0xFFFFFFF8) prefix=29;
        else if(netmask==0xFFFFFFF0) prefix=28;
        else if(netmask==0xFFFFFFE0) prefix=27;
        else if(netmask==0xFFFFFFC0) prefix=26;
        else if(netmask==0xFFFFFF80) prefix=25;
        else if(netmask==0xFFFFFE00) prefix=23;
        else if(netmask==0xFFFFFC00) prefix=22;
        else if(netmask==0xFFFFF800) prefix=21;
        else if(netmask==0xFFFFF000) prefix=20;
        else if(netmask==0xFFFFE000) prefix=19;
        else if(netmask==0xFFFFC000) prefix=18;
        else if(netmask==0xFFFF8000) prefix=17;
        else if(netmask==0xFFFE0000) prefix=15;
        else if(netmask==0xFFFC0000) prefix=14;
        else if(netmask==0xFFF80000) prefix=13;
        else if(netmask==0xFFF00000) prefix=12;
        else if(netmask==0xFFE00000) prefix=11;
        else if(netmask==0xFFC00000) prefix=10;
        else if(netmask==0xFF800000) prefix=9;
        else if(netmask==0xFE000000) prefix=7;
        else if(netmask==0xFC000000) prefix=6;
        else if(netmask==0xF8000000) prefix=5;
        else if(netmask==0xF0000000) prefix=4;
        else if(netmask==0xE0000000) prefix=3;
        else if(netmask==0xC0000000) prefix=2;
        else if(netmask==0x80000000) prefix=1;
        else throw new RuntimeException("Invalid NETMASK! "+Integer.toHexString(netmask));
        
        return prefix;
    }

    public int getNetmask()
    {
        return netmask;
    }
    
    public int getPrefix()
    {
        return prefix;
    }
    /**
     * Add a route based on the network interface information
     * @param net the network interface
     */
    final public static void addRoute(NetworkInterface net)
    {
        Route newRoute = new Route(net);
        addRoute(newRoute);
    }
    
	final public static void addRoute(InetAddress destination, InetAddress gateway, int netmask, NetworkInterface net)
	{
	}
	
	final public static void addRoute(int prefix, InetAddress destination, InetAddress gateway, NetworkInterface net)
	{
        Route newRoute = new Route(prefix, destination, gateway, net);
        // if empty then just add it
        addRoute(newRoute);
	}

	private static void addRoute(Route newRoute) {
		if(routeTable.isEmpty()) routeTable.add(newRoute);
        else
        {
            ListIterator<Route> routeIter = routeTable.listIterator();
            while(routeIter.hasNext())
            {
                Route route = routeIter.next();
                if(newRoute.prefix > route.prefix)
                {
                		routeIter.previous();
                    routeIter.add(newRoute);
                    return;
                }
            }
            routeTable.add(newRoute);
        }
	}
	
    final public static void addRoute(InetAddress destination, InetAddress gateway, int netmask, int metric, NetworkInterface net)
    {
        
    }

    public static Route find(InetAddress address)
    {
        for(Route route: routeTable)
        {
            if(route.canRoute(address))
            {
                return route;
            }
        }
        throw new RuntimeException("No Route Found");
    }

    private boolean canRoute(InetAddress address)
    {
        
        return (address.inet4() & netmask) == destination.inet4();
    }
}
