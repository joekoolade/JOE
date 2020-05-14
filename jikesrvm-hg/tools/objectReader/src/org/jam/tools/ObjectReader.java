package org.jam.tools;

import java.util.List;

public class ObjectReader
implements SizeConstants
{

    private static ObjectReader reader;
    private MemoryReader memory;
    private RvmMap map;
    private int jtoc;
    private int stack;
    private int bootThread;
    private int tibType;
    private String workingDirectory;
    
    int THREAD_START_METHOD_ID = -3;  // thread starup frame
    static final private int BOOT_PARAMETERS_ADDRESS = 0x001000E0;
    private static CommandProcessor cp;
    
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
        cp = new CommandProcessor(reader);
        cp.run();
    }

    private void initialize()
    {
        jtoc = memory.read(BOOT_PARAMETERS_ADDRESS);
        stack = memory.read(BOOT_PARAMETERS_ADDRESS+4);
        bootThread = memory.read(BOOT_PARAMETERS_ADDRESS+4);
    }

    public void dumpObject(int address)
    {
        JikesObject obj = new JikesObject(memory, address);
        obj.print(cp.getArgs());
    }
    public void dumpObject(MapConstants mapConstants)
    {
        long value = memory.read(jtoc + mapConstants.getOffset()) & 0xFFFFFFFF;
        System.out.println("0x"+Long.toHexString(value)+" ("+value+")");
    }

    /*
     * Prints out JTOC field
     */
    public void dumpObject(MapField mapField)
    {
        long value = memory.read(jtoc + mapField.getOffset()) & 0xFFFFFFFF;
        if(mapField.isPrimitive() || mapField.isUnboxed())
        {
            System.out.println("0x"+Long.toHexString(value)+" ("+value+")");
        }
        else
        {     
            JikesObject obj = new JikesObject(memory, (int) value);
            obj.print(cp.getArgs());
        }
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
        workingDirectory = file.substring(0, file.lastIndexOf('/')+1);
        System.out.println("Working Directory "+workingDirectory);
        memory = new MemoryReader(file);
        map = new RvmMap(workingDirectory);
        map.process();
    }
    
    public void run()
    {
        System.out.println(Integer.toHexString(memory.read(0x100008)));
    }
    
    /**
     * Scan a stack
     * @param stack
     */
    public void scan(byte[] stack)
    {
        
    }
    
    public void scan(int address, int size)
    {
        
    }
    
    public void scanJtoc()
    {
        
    }

    public RvmMap getMap()
    {
        return map;
    }
    
    public int readInt(int address)
    {
        return memory.read(address);
    }
    
    public long readLong(int address)
    {
        return memory.readLong(address);
    }
    
    public short readShort(int address)
    {
        return memory.readShort(address);
    }
    
    public byte readByte(int address)
    {
        return memory.readByte(address);
    }

    public String getString(int address)
    {
        String str=null;
        JikesObject stringObj = new JikesObject(memory, address);
        if(stringObj.isString()==false)
        {
            return Integer.toHexString(address);
        }
        int charArray = stringObj.getField("value");
        JikesObject carray = new JikesObject(memory, charArray);
        str = new String(carray.getCharArray(0));
        return str;
    }
    /**
     * Format an integer into the specified radix, zero-filled.
     *
     * @param i The integer to format.
     * @param radix The radix to encode to.
     * @param len The target length of the string. The string is
     *   zero-padded to this length, but may be longer.
     * @return The formatted integer.
     */
    public static StringBuilder formatInt(int i, int len)
    {
      String s = Integer.toHexString(i);
      StringBuilder buf = new StringBuilder();
      for (int j = 0; j < len - s.length(); j++)
        buf.append("0");
      buf.append(s);
      return buf;
    }

    /**
     * Print out contents of address 
     * @param address
     * @param size
     */
    public void dump(int address, int size)
    {
        StringBuilder prefix;
        int index = 0;
        int columns = 0;
        for (; index < size; index += 4)
        {
            prefix = formatInt(address + index * 4, 8);
            prefix.append(": ");
            if (size - index < 4)
                columns = size - index;
            else
                columns = 4;
            for (int nextColumn = 0; nextColumn < columns; nextColumn++)
            {
                int value = readInt(address + nextColumn * 4 + index * 4);
                prefix.append(formatInt(value, 8)).append(' ');
            }
            /*
             * print out any characters
             */
            for (int j = 0; j < columns * 4; j++)
            {
                byte aByte = readByte(address + index * 4 + j);
                if ((aByte & 0xFF) < 0x20 || (aByte & 0xFF) > 0x7E)
                    prefix.append('.');
                else
                    prefix.append((char) (aByte & 0xFF));
            }
            System.out.println(prefix.toString());
        }

    }

    /**
     * Format an long into the specified radix, zero-filled.
     *
     * @param i The integer to format.
     * @param radix The radix to encode to.
     * @param len The target length of the string. The string is
     *   zero-padded to this length, but may be longer.
     * @return The formatted integer.
     */
    public static StringBuilder formatLong(long i, int len)
    {
      String s = Long.toHexString(i);
      StringBuilder buf = new StringBuilder();
      for (int j = 0; j < len - s.length(); j++)
        buf.append("0");
      buf.append(s);
      return buf;
    }

    /**
     * Print out contents of address 
     * @param address
     * @param size
     */
    public void dump_long(int address, int size)
    {
        StringBuilder prefix;
        int index = 0;
        int columns = 0;
        for (; index < size; index += 2)
        {
            prefix = formatInt(address + index * 8, 8);
            prefix.append(": ");
            if (size - index < 2)
                columns = size - index;
            else
                columns = 2;
            for (int nextColumn = 0; nextColumn < columns; nextColumn++)
            {
                long value = readLong(address + nextColumn * 8 + index * 8);
                prefix.append(formatLong(value, 16)).append(' ');
            }
            /*
             * print out any characters
             */
            for (int j = 0; j < columns * 8; j++)
            {
                byte aByte = readByte(address + index * 8 + j);
                if ((aByte & 0xFF) < 0x20 || (aByte & 0xFF) > 0x7E)
                    prefix.append('.');
                else
                    prefix.append((char) (aByte & 0xFF));
            }
            System.out.println(prefix.toString());
        }

    }

    public void printCodePoint(int address)
    {
        if(address <= 0x100000) return;
        MapCode code = map.getCode(address);
        if(code == null)
        {
            System.out.println(Integer.toHexString(address)+": NULL");
        }
        else
        {
            System.out.println(code.getTypeName()+"."+code.getName()+code.getParameters());
        }
    }

    public void backTrace(int address)
    {
        long stackPointer = reader.readLong(address);
        long ipAddress;
        for(; stackPointer > 0 && (int)stackPointer != THREAD_START_METHOD_ID; )
        {
            ipAddress = reader.readLong((int)stackPointer+8);
            MapCode code = map.getCode((int)ipAddress);
            System.out.print(formatInt((int)stackPointer, 8)+": ");
            if(code == null)
            {
                System.out.println(formatInt((int)ipAddress,8));
            }
            else
            {
                System.out.println(code.getTypeName()+"."+code.getName()+code.getParameters());
            }
            stackPointer = reader.readLong((int)stackPointer);
        }
    }
    
    public void dumpThread(int threadAddress)
    {
        int fp=0, sp=0, threadIdx=-1, slot=-1, nameAddress=0;
        String name="NULL";
        try
        {
            // get the thread stack
            JikesObject thread = new JikesObject(memory, threadAddress);
            fp = thread.getField("framePointer");
            sp = thread.getField("sp");
            threadIdx = thread.getField("threadIdx");
            slot = thread.getField("threadSlot");
            nameAddress = thread.getField("name");
            name = getString(nameAddress);
            JikesObject contextRegisters = new JikesObject(memory, thread.getField("contextRegisters"));
            int gprs = contextRegisters.getField("gprs");
            System.out.println("Thread " + name + " index "+threadIdx+" slot "+slot);
            System.out.println("fp "+Integer.toHexString(fp) + " sp "+Integer.toHexString(sp));
            int fp0 = contextRegisters.getField("fp");
            int ip0 = contextRegisters.getField("ip");
            System.out.println("ctxt fp "+Integer.toHexString(fp0) + " ip " + Integer.toHexString(ip0) + " gprs " + Integer.toHexString(gprs));
            // dump the stack
            dump_long(sp, 20);
            
            printCodePoint((int)readLong(fp+8));
            // backtrace
            backTrace(fp);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("OOPS! Something is wrong with this thread!");
            System.out.println("Thread id: "+Integer.toHexString(threadAddress));
            System.out.println("fp: "+Integer.toHexString(fp));
            System.out.println("sp: "+Integer.toHexString(sp));
            System.out.println("Thread idx: "+threadIdx);
            System.out.println("Slot: "+slot);
            System.out.println("Name Address: "+Integer.toHexString(nameAddress));
            System.out.println("Name: "+name);
        }
    }
    
    public void dumpAllThreads()
    {
        /*
         * Get the threadBySlot[] field
         */
        MapField threadSlotField = map.getField("RVMThread.threadBySlot").get(0);
        // Create the jikes object
        Long address = threadSlotField.getIntValue();
        JikesArray threads = new JikesArray(memory, address.intValue());
        if(threads.getType().isArray() == false)
        {
            System.out.println("threadBySlot is not an array!");
            return;
        }
        /*
         * Loop throught the array and dump thread information:
         * stack, backtrace, and 
         */
        int length = threads.getSize();
        int threadIndex = 0;
        for(threadIndex=0; threadIndex < length; threadIndex++)
        {
            int threadAddress = threads.get(threadIndex);
            if(threadAddress == 0) continue;
            dumpThread(threadAddress);
        }
    }
}
