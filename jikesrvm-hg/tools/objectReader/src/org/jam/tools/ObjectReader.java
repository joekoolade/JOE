package org.jam.tools;

public class ObjectReader
{

    private static ObjectReader reader;
    private MemoryReader memory;
    private RvmMap map;
    private int jtoc;
    private int stack;
    private int bootThread;
    private int tibType;
    private String workingDirectory;
    
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
        CommandProcessor cp = new CommandProcessor(reader);
        cp.run();
    }

    private void initialize()
    {
        jtoc = memory.read(BOOT_PARAMETERS_ADDRESS);
        stack = memory.read(BOOT_PARAMETERS_ADDRESS+4);
        bootThread = memory.read(BOOT_PARAMETERS_ADDRESS+8);
    }

    public void dumpObject(int address)
    {
        JikesObject obj = new JikesObject(memory, address);
        obj.print();
    }
    public void dumpObject(MapConstants mapConstants)
    {
        long value = memory.read(jtoc + mapConstants.getOffset()) & 0xFFFFFFFF;
        System.out.println("0x"+Long.toHexString(value)+" ("+value+")");
    }

    public void dumpObject(MapField mapField)
    {
        if(mapField.isPrimitive() || mapField.isUnboxed())
        {
            long value = memory.read(jtoc + mapField.getOffset()) & 0xFFFFFFFF;
            System.out.println("0x"+Long.toHexString(value)+" ("+value+")");
        }
        else
        {     
            Long address = mapField.getIntValue();
            JikesObject obj = new JikesObject(memory, address.intValue());
            obj.print();
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
    
    public short readShort(int address)
    {
        return memory.readShort(address);
    }
    
    public byte readByte(int address)
    {
        return memory.readByte(address);
    }
}
