package org.jam.net;

import org.jam.driver.net.Packet;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.InetAddress;
import org.jam.net.inet4.SendPacket;

public interface NetworkInterface {
    InetAddress getInetAddress();
    int getNetMask();
    void send(SendPacket packet);
    int getMtu();
    void setMtu(int mtu);
    void setNetMask(int mask);
    void setInetAddress(InetAddress inetAddress);
    void arp(InetAddress inet);
    EthernetAddr getEthernetAddress();
    void setEthernetAddress(EthernetAddr macAddress);
}
