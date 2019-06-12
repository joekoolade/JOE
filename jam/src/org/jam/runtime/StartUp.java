package org.jam.runtime;

import java.util.ArrayList;

/**
 * This class is used to start up classes with threads and main()
 * @author joe
 *
 */
public class StartUp
{
    private enum Type
    {
        MAIN,
        THREAD
    }
    
    private String className;
    private Type type;
    
    private static ArrayList<StartUp> programs;

    private StartUp(String name, Type type)
    {
        className = name;
        this.type = type;
    }

    public final static void runMain(String clsName)
    {
        programs.add(new StartUp(clsName, Type.MAIN));
    }
    
    public final static void runThread(String clsName)
    {
        programs.add(new StartUp(clsName, Type.THREAD));
    }
    
    public final static void run()
    {
        
    }
}
