package org.jam.net;

import org.jam.driver.net.Packet;
import org.jam.net.inet4.InetAddress;

public interface NetworkInterface {
    InetAddress getInetAddress();
    int getNetMask();
    void send(Packet packet);
    int getMtu();
    void setMtu(int mtu);
    void setNetMask(int mask);
    void setInetAddress(InetAddress inetAddress);
}
