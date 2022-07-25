package tests.java.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DatagramClientServer
{
    int SERVER_PORT = 20097;
    
    public static void main(String[] args) throws UnknownHostException, InterruptedException
    {
        DatagramClientServer dcs = new DatagramClientServer();
        dcs.run();
    }

    public void run() throws UnknownHostException, InterruptedException
    {
        Thread clientThread = new Thread(this.new Client());
        Thread serverThread = new Thread(this.new Server());
        
        serverThread.start();
        clientThread.start();
        
        clientThread.join();
        serverThread.join();
        System.out.println("done");
    }
    
    class Client implements Runnable
    {
        InetAddress addr;
        int port;
        String message;
        
        public Client() throws UnknownHostException
        {
            addr = InetAddress.getByName("127.0.0.1");
            port = SERVER_PORT;
            message = "I am sending this to the people of the world";
        }
        
        @Override
        public void run()
        {
            DatagramSocket socket;
            try
            {
                socket = new DatagramSocket();
            } catch (SocketException e)
            {
                System.out.println("client create failed");
                e.printStackTrace();
                return;
            }
            System.out.println("Sending: " + message);
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), addr, port);
            try
            {
                socket.send(packet);
            } catch (IOException e)
            {
                System.out.println("client send failed");
                e.printStackTrace();
            }
        }
        
    }
    
    class Server implements Runnable
    {
        InetAddress addr;

        public Server() throws UnknownHostException
        {
            addr = InetAddress.getByName("127.0.0.1");
        }

        @Override
        public void run()
        {
            DatagramSocket socket=null;
            
            try
            {
                socket = new DatagramSocket(null);
                System.out.println("bound: "+socket.isBound());
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(addr, SERVER_PORT));
            } catch (SocketException e)
            {
                System.out.println("server create failed");
                socket.close();
                e.printStackTrace();
                System.exit(0);
            }
            byte data[] = new byte[256];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try
            {
                socket.receive(packet);
            } catch (IOException e)
            {
                System.out.println("server i/o exception");
                e.printStackTrace();
            }
            System.out.println("Received: "+ new String(packet.getData()));
        }
    }
}
