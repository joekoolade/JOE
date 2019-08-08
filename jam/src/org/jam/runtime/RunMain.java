package org.jam.runtime;

import org.vmmagic.pragma.Entrypoint;

public class RunMain 
implements Run
{
    private static final boolean DEBUG = true;
    private String threadClassName;
    private String[] args;

    public RunMain(String threadClass, String args[])
    {
        this.threadClassName = threadClass;
        this.args = args;
        String[] mainArgs = new String[args.length + 1];
        args[0] = threadClass;
        int index = 1;
        for(; index < args.length; index++)
        {
            mainArgs[index] = args[index-1];
        }
        this.args = mainArgs;
    }

    public RunMain(String[] args)
    {
        this.args = args;
        threadClassName = args[0];
    }
    public void setArgs(String args[])
    {
        this.args = args;
    }
    
    @Entrypoint
    public void run()
    {
        
    }
}
