package org.jam.runtime;

import org.vmmagic.pragma.Entrypoint;

public class RunMain 
implements Run
{
    private static final boolean DEBUG = true;
    private String threadClassName;
    private String[] args;
    private final MainThread mainThread;
    
    public RunMain(String threadClass, String args[])
    {
        this.threadClassName = threadClass;
        if(args == null)
        {
            this.args = new String[1];
            this.args[0] = threadClass;
        }
        else
        {
            this.args = new String[args.length + 1];
            this.args[0] = threadClass;
            int index = 1;
            for(; index < args.length; index++)
            {
                this.args[index] = args[index-1];
            }
        }
        mainThread = new MainThread(this.args);
    }

    public RunMain(String threadClass)
    {
        this(threadClass, null);
    }
    public RunMain(String[] args)
    {
        this.args = args;
        threadClassName = args[0];
        mainThread = new MainThread(this.args);
    }

    @Entrypoint
    public void run()
    {
        mainThread.start();
    }
}
