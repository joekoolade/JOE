/**
 * Created on Jul 22, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.net.inet4;

import java.net.UnknownHostException;

import org.jam.java.net.VMInetAddress;

/**
 * @author Joe Kulig
 *
 */
public class InetAddress
{
    private static final int        BROADCAST_ADDR = 0xFFFFFFFF;
    private static final int        HOST_ADDR      = 0;
    final private byte              addrBytes[];
    private String                  string         = null;
    final private int               addrInt;
    public static final InetAddress HOST           = new InetAddress(HOST_ADDR);
    public static final InetAddress BROADCAST      = new InetAddress(BROADCAST_ADDR);

    /*
     * Create ip from an integer
     */
    public InetAddress(int addr)
    {
        addrInt = addr;
        addrBytes = new byte[4];
        addrBytes[0] = (byte) ((addr >> 24) & 0xFF);
        addrBytes[1] = (byte) ((addr >> 16) & 0xFF);
        addrBytes[2] = (byte) ((addr >> 8) & 0xFF);
        addrBytes[3] = (byte) (addr & 0xFF);
    }

    public InetAddress(String inet4String) throws UnknownHostException
    {
        this(VMInetAddress.aton(inet4String));
        string = inet4String;
    }

    public InetAddress(java.net.InetAddress inetAddress)
    {
        this(inetAddress.getAddress());
    }

    /*
     * Create ip from four octets
     */
    public InetAddress(int octet1, int octet2, int octet3, int octet4)
    {
        addrBytes = new byte[4];
        addrBytes[0] = (byte) octet1;
        addrBytes[1] = (byte) octet2;
        addrBytes[2] = (byte) octet3;
        addrBytes[3] = (byte) octet4;
        addrInt = (octet1 << 24) | (octet2 << 16) | (octet3 << 8) | octet4;
    }

    public InetAddress(byte[] address)
    {
        addrBytes = address;
        addrInt = (addrBytes[0] << 24) | ((addrBytes[1] & 0xFF) << 16) | ((addrBytes[2] & 0xFF) << 8)
                | (addrBytes[3] & 0xFF);
    }

    public boolean isBroadcast()
    {
        return addrInt == BROADCAST_ADDR;
    }

    public boolean isHost()
    {
        return addrInt == HOST_ADDR;
    }

    final public byte[] asArray()
    {
        return addrBytes;
    }

    final public int inet4()
    {
        return addrInt;
    }

    public String toString()
    {
        if (string == null)
            string = ((int)addrBytes[0]&0xFF) + "." + ((int)addrBytes[1]&0xFF) + "." + ((int)addrBytes[2]&0xFF) + "." + ((int)addrBytes[3]&0xFF);
        return string;
    }
}
