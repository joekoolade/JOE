package org.jam.java.net;

import java.util.Set;

public class VMNetworkInterface {
    public String name;
    public Set addresses;
    public static VMNetworkInterface[] getVMInterfaces() { return null; }
    static public boolean isUp(String s) { return false; }
    static public boolean isPointToPoint(String s) { return false; }
    static public boolean isLoopback(String s) { return false; }
    static public boolean supportsMulticast(String s) { return false; }
    
}
