package org.jam.net;

import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;

import org.jam.net.inet4.InetAddress;

public class Connection {
    private InetAddress local;
    private InetAddress remote;
    private IpProto protocol;
    private Route route;

    public Connection(InetSocketAddress localAddress, InetSocketAddress remoteAddress, IpProto proto) throws NoRouteToHostException
    {
        // look up the route
        remote = new InetAddress(remoteAddress.getAddress());
        System.out.println("remote addr " + remote);
        route = Route.find(remote);
        System.out.println("getting local address "+localAddress);
        byte[] addr = null;
        if (localAddress !=null)
        {
            local = new InetAddress(localAddress.getAddress());
        }
        else
        {
            local = route.getNetworkIf().getInetAddress();
        }
        addr = local.asArray();
        System.out.println("connection " + ((int) addr[0] & 0xff) + "." + ((int) addr[1] & 0xff) + "." + ((int) addr[2] & 0xff) + "."
                        + ((int) addr[3] & 0xff));
        System.out.println("got local address");
        protocol = proto;
    }

    public byte getProtocol()
    {
        return protocol.protocol();
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

    public Route getRoute()
    {
        return route;
    }

    public NetworkInterface getNetworkInterface()
    {
        return route.getNetworkIf();
    }
}
