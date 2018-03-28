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
		System.out.println("getting local address ");
		byte[] addr = localAddress.getAddress().getAddress();
		System.out.println("connection " + ((int)addr[0]&0xff) + "." + ((int)addr[1]&0xff) + "." + ((int)addr[2]&0xff)+ "." + ((int)addr[3]&0xff));
		local = new InetAddress(localAddress.getAddress());
		System.out.println("got local address");
		protocol = proto;
	}

	public byte getProtocol() {
		return (byte)protocol.ordinal();
	}

    public InetAddress getLocal()
    {
        return local;
    }

    public InetAddress getRemote()
    {
        return remote;
    }

    public int getLocalInet()
    {
        return local.inet4();
    }

    public int getRemoteInet()
    {
        return remote.inet4();
    }



}
