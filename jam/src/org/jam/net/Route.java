package org.jam.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;

public class Route {
	NetworkInterface networkIf;
	InetAddress destination;
	InetAddress gateway;
	int netmask;
	
	public Route(InetAddress address) throws NoRouteToHostException {
	}

}
