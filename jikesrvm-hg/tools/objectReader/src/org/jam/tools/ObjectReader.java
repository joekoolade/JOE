package org.jam.tools;

public class ObjectReader
{

    private static ObjectReader reader;
    private MemoryReader memory;
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
        JikesObject bt = new JikesObject(memory, bootThread);
        /*
         * Find the TIB object
         */
        int bootThreadTib = bt.getTIBAddress();
        int bootThreadRVMClass = bt.getType().getAddress();
        int rvmClassTib = memory.read(bootThreadRVMClass-12);
        int rvmClass = memory.read(rvmClassTib);
        tibType = memory.read(bootThreadTib-12);
        StringBuffer sb = new StringBuffer("Boot Parameters: ");
        sb.append(Integer.toHexString(jtoc)).append(' ').append(Integer.toHexString(stack));
        sb.append(' ').append(Integer.toHexString(bootThread));
        System.out.println(sb);
        sb.setLength(0);
        sb.append("boot thread tib ").append(Integer.toHexString(bootThreadTib));
        sb.append("\nboot thread rvm class ").append(Integer.toHexString(bootThreadRVMClass));
        sb.append("\ntib type ").append(Integer.toHexString(tibType));
        sb.append("\nrvm class tib/class ").append(Integer.toHexString(rvmClassTib)).append('/').append(Integer.toHexString(rvmClass));
        System.out.println(sb);
        System.out.println();
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
}
