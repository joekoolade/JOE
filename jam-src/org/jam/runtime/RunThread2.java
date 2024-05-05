package org.jam.runtime;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.Reflection;
import org.jikesrvm.runtime.RuntimeEntrypoints;
import org.vmmagic.pragma.Entrypoint;

/**
 * The class is used by startup to load and run threads as part of the 
 * JOE boot up
 * 
 * @author Joe Kulig
 *
 */
public class RunThread2
implements Runnable
{
    private static final boolean DEBUG = true;
    private String threadClassName;
    private String[] args;
    private MainThread mainThread;
    
    /**
     * The thread class to load and  run
     * @param threadClass
     */
    public RunThread2(String threadClass)
    {
        this.threadClassName = threadClass;
        if(DEBUG) System.out.println("RunThread class: "+threadClassName);
    }
    
    public void setArgs(String args[])
    {
        this.args = args;
    }
    
    @Entrypoint
    public void run()
    {
        if(DEBUG) System.out.println("RunThread.run() starting");
       
        Class cls=null;
        try {
            cls = Class.forName(threadClassName);
        } catch (ClassNotFoundException e) {
          if (DEBUG) System.out.println("Class load failed!");
          e.printStackTrace();
          return;
        }
        if (DEBUG) System.out.println("loaded");
        
        Object obj;
        try
        {
            obj = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        if(DEBUG) System.out.println("got a new object");
        Runnable r=(Runnable)obj;
        r.run();
        if(DEBUG) VM.sysWriteln("Starting "+threadClassName);
    }
}
