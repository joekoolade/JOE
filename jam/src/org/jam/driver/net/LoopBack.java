package org.jam.driver.net;

import org.jam.net.NetworkInterface;
import org.jam.net.Route;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.InetAddress;
import org.jam.net.inet4.SendPacket;

public class LoopBack implements NetworkInterface {

	final static byte[] DEFAULT_ADDRESS = { 0x7f, 0, 0, 1 };
	final static int DEFAULT_NETMASK = 0xFF000000;
	
	private int netmask;
	private InetAddress inetAddress;
	private int mtu;
	
	public LoopBack()
	{
		this(DEFAULT_ADDRESS, DEFAULT_NETMASK);
	}
	
	public LoopBack(byte[] address, int netmask) {
		this.netmask = netmask;
		inetAddress = new InetAddress(address);
		/*
		 * Setup a route
		 */
		Route.addRoute(this);
	}
	@Override
	public InetAddress getInetAddress() {
		return inetAddress;
	}

	@Override
	public int getNetMask() {
		return netmask;
	}

	@Override
	public void send(SendPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(EthernetAddr destination, Packet packet, short protocol) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMtu() {
		return mtu;
	}

	@Override
	public void setMtu(int mtu) {
		this.mtu = mtu;
	}

	@Override
	public void setNetMask(int mask) {
		netmask = mask;
	}

	@Override
	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	@Override
	public EthernetAddr arp(InetAddress inet) {
		return null;
	}

	@Override
	public EthernetAddr getEthernetAddress() {
		return null;
	}

	@Override
	public void setEthernetAddress(EthernetAddr macAddress) {

	}

}
