package org.jam.tools;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class ObjectReader
{

    private static ObjectReader reader;
    private MemoryReader memory;
    private FileChannel memoryChannel;
    
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
        reader.run();
    }

    private static void usage()
    {
        System.out.println("usage: ObjectReader <memory dump file>");
    }

    public ObjectReader(String file)
    {
        memory = new MemoryReader(file);
    }
    
    public void run()
    {
        System.out.println(Integer.toHexString(memory.read(0x100008)));
    }
}
