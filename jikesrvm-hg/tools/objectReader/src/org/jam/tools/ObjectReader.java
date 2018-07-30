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
    private int jtoc;
    private int stack;
    private int bootThread;
    private int tibType;
    
    static final private int BOOT_PARAMETERS_ADDRESS = 0x001000E0;
    
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
        reader.initialize();
        reader.run();
    }

    private void initialize()
    {
        jtoc = memory.read(BOOT_PARAMETERS_ADDRESS);
        stack = memory.read(BOOT_PARAMETERS_ADDRESS+4);
        bootThread = memory.read(BOOT_PARAMETERS_ADDRESS+8);
        /*
         * Find the TIB object
         */
        int bootThreadTib = memory.read(bootThread-12);
        int bootThreadRVMClass = memory.read(bootThreadTib);
        tibType = memory.read(bootThreadTib-12);
        StringBuffer sb = new StringBuffer("Boot Parameters: ");
        sb.append(Integer.toHexString(jtoc)).append(' ').append(Integer.toHexString(stack));
        sb.append(' ').append(Integer.toHexString(bootThread));
        System.out.println(sb);
        sb.setLength(0);
        sb.append("boot thread tib ").append(Integer.toHexString(bootThreadTib));
        sb.append("\n tib type").append(Integer.toHexString(tibType));
        System.out.println(sb);
    }

    public boolean isTib(int address)
    {
        return address == tibType;
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
