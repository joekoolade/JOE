package org.jam.net;

import org.jam.driver.net.Packet;
import org.jam.net.ethernet.EthernetAddr;
import org.jam.net.inet4.InetAddress;
import org.jam.net.inet4.SendPacket;

public interface NetworkInterface {
    InetAddress getInetAddress();
    int getNetMask();
    void send(SendPacket packet);
    void send(EthernetAddr destination, Packet packet, short protocol);
    int getMtu();
    void setMtu(int mtu);
    void setNetMask(int mask);
    void setInetAddress(InetAddress inetAddress);
    EthernetAddr getEthernetAddress();
    void setEthernetAddress(EthernetAddr macAddress);
    void poll();
    /*
     * How much work should be done per poll
     */
    int work();
    /*
     * How often to schedule the poll
     */
    int schedule();
    void receive();
}
