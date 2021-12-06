package org.jam.net;

public enum IpProto {
	ICMP(1),
	IGMP(2),
	IPV4(4),
	TCP(6),
	UDP(17),
	IPV6(41),
	GRE(47);
	
	int protocol;
	
	IpProto(int protocol)
	{
		this.protocol = protocol;
	}

    public byte protocol()
    {
        // TODO Auto-generated method stub
        return (byte)protocol;
    }
}
