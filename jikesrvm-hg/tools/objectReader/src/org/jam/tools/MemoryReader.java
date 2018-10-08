package org.jam.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class MemoryReader
{
    private MappedByteBuffer mappedBuffer;
    private FileChannel memoryChannel;

    public MemoryReader(String file)
    {
        RandomAccessFile memoryDump;
        try
        {
            memoryDump = new RandomAccessFile(file, "r");
            memoryChannel = memoryDump.getChannel();
            mappedBuffer = memoryChannel.map(MapMode.READ_ONLY, 0, memoryDump.length());
            mappedBuffer.order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public int read(int address)
    {
        return mappedBuffer.getInt(address);
    }
    
    public char readChar(int address)
    {
        return mappedBuffer.getChar(address);
    }
    
    public long readLong(int address)
    {
        return mappedBuffer.getLong(address);
    }
    
    public byte readByte(int address)
    {
        return mappedBuffer.get(address);
    }

    public short readShort(int address)
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
