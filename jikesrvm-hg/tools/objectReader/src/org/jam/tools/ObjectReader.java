package org.jam.tools;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ObjectReader
{

    private static ObjectReader reader;
    private byte[] memory;
    
    public static void main(String[] args)
    {
        // process arguments
        if(args.length != 1)
        {
            System.out.println("Need memory dump!");
            usage();
            System.exit(1);
        }
        reader = new ObjectReader(args[0]);
    }

    private static void usage()
    {
        System.out.println("usage: ObjectReader <memory dump file>");
    }

    public ObjectReader(String file)
    {
        
        try
        {
            RandomAccessFile memoryDump = new RandomAccessFile(file, "r");
            memory = new byte[(int) memoryDump.length()];
            
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }
}
