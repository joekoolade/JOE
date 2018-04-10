package org.jam.net;

public enum EtherType
{
    IPV4((short)0x800),
    ARP((short)0x806),
    RARP((short)0x8035),
    IPV6((short)0x86DD);
    
    public short type;
    
    EtherType(short type)
    {
        this.type = type;
    }
}
