/*
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.dhcp4java.examples;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPPacket;



/**
 * A simple DHCP sniffer.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPSniffer {
    private DHCPSniffer() {
    	throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(DHCPConstants.BOOTP_REQUEST_PORT);

            while (true) {
                DatagramPacket pac = new DatagramPacket(new byte[1500], 1500);
                DHCPPacket     dhcp;

                socket.receive(pac);
                dhcp = DHCPPacket.getPacket(pac);
                System.out.println(dhcp.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
