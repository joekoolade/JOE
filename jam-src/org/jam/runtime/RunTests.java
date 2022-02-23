package org.jam.runtime;

public class RunTests implements Runnable
{
    private String[] tests;

    public RunTests(String tests[])
    {
        this.tests = tests;
    }
    
    public void run()
    {
        System.out.println("Testing MODE");
        
        int i = 0;
        for (i = 0; i < tests.length; i++) 
        {
            String args[] = new String[1];
            args[0] = tests[i];
            System.out.println("Running " + tests[i]);
            MainThread program = new MainThread(args);
            program.run();
        }
    }
}
