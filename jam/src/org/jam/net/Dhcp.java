package org.jam.net;

public class Dhcp
implements Runnable
{
    int mode;
    
    public Dhcp()
    {
        // TODO Auto-generated constructor stub
    }

    final public static void discover(NetworkInterface netInterface)
    {
        Dhcp dhcpClient =new Dhcp();
        Thread dhcpThread = new Thread(dhcpClient);
        dhcpThread.start();
    }

    public void run()
    {
        // TODO Auto-generated method stub
        
    }
}
