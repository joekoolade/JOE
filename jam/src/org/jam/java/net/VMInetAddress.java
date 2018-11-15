/* VMInetAddress.java -- Class to model an Internet address
   Copyright (C) 2005  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package org.jam.java.net;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbill.DNS.Address;

public class VMInetAddress implements Serializable
{
    /**
     * This method looks up the hostname of the local machine we are on. If the
     * actual hostname cannot be determined, then the value "localhost" will be
     * used. This native method wrappers the "gethostname" function.
     *
     * @return The local hostname.
     */
    public static String getLocalHostname()
    {
        return null;
    }

    /**
     * Returns the value of the special address INADDR_ANY
     */
    public static byte[] lookupInaddrAny() throws UnknownHostException
    {
        return null;
    }

    /**
     * This method returns the hostname for a given IP address. It will throw an
     * UnknownHostException if the hostname cannot be determined.
     *
     * @param ip
     *            The IP address as a byte array
     *
     * @return The hostname
     *
     * @exception UnknownHostException
     *                If the reverse lookup fails
     */
    public static String getHostByAddr(byte[] ip) throws UnknownHostException
    {
        InetAddress addr = InetAddress.getByAddress(ip);
        return Address.getHostName(addr);
    }

    /**
     * Returns a list of all IP addresses for a given hostname. Will throw an
     * UnknownHostException if the hostname cannot be resolved.
     */
    public static byte[][] getHostByName(String hostname) throws UnknownHostException
    {
        InetAddress ipAddrs[] = Address.getAllByName(hostname);
        byte[][] byteAddrs = new byte[ipAddrs.length][];
        int index = 0;
        for(; index < ipAddrs.length; index++)
        {
            byteAddrs[index] = ipAddrs[index].getAddress();
        }
        return byteAddrs;
    }

    /**
     * Return the IP address represented by a literal address. Will return null if
     * the literal address is not valid.
     *
     * @param address
     *            the name of the host
     *
     * @return The IP address as a byte array
     */
    public static byte[] aton(String address)
    {
        int octetParts[] = new int[4];
        int partIndex = 0;
        int value = 0;
        int base = 10;

        for (char c : address.toCharArray())
        {
            if (Character.isDigit(c))
            {
                value = (value * base) + (c - '0');
                continue;
            }
            if (c == '.')
            {
                if (partIndex > 3 || value > 0xFF)
                {
                    return null;
                }
                octetParts[partIndex++] = value;
                value = 0;
            }
        }
        if (value > 255)
        {
            return null;
        }
        octetParts[partIndex] = value;
        byte[] inet = new byte[4];
        inet[0] = (byte) octetParts[0];
        inet[1] = (byte) octetParts[1];
        inet[2] = (byte) octetParts[2];
        inet[3] = (byte) octetParts[3];
        return inet;
    }
}
