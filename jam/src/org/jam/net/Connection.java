package org.jam.net;

import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;

import org.jam.net.inet4.InetAddress;

public class Connection {
	private InetAddress local;
	private InetAddress remote;
	private IpProto protocol;
	private Route route;
	
	public Connection(InetSocketAddress localAddress, InetSocketAddress remoteAddress, IpProto proto) throws NoRouteToHostException {
		// look up the route
	    remote = new InetAddress(remoteAddress.getAddress());
		route = Route.find(remote);
		local = new InetAddress(localAddress.getAddress());
		protocol = proto;
	}

	public byte getProtocol() {
		return (byte)protocol.ordinal();
	}

}
