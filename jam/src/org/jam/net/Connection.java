package org.jam.net;

import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;

public class Connection {
	private InetSocketAddress local;
	private InetSocketAddress remote;
	private IpProto protocol;
	private Route route;
	
	public Connection(InetSocketAddress localAddress, InetSocketAddress remoteAddress, IpProto udp) throws NoRouteToHostException {
		// look up the route
		route = new Route(remoteAddress.getAddress());
		local = localAddress;
		remote = remoteAddress;
		protocol = udp;
	}

}
