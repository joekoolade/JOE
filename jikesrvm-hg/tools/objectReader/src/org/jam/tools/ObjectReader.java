package org.jam.tools;

public class ObjectReader
{

    String memoryDump;
    
    class Options
    {
        
    }
    public static void main(String[] args)
    {
       processArgs(args);

    }

    private static void processArgs(String[] args)
    {
       if(args.length != 1)
       {
           System.out.println("Need memory dump!");
           usage();
           System.exit(1);
       }
        
    }

    private static void usage()
    {
        System.out.println("usage: ObjectReader <memory dump file>");
    }

    public ObjectReader(String file)
    {
        memoryDump = file;
    }
}
