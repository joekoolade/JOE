package org.jam.runtime;

import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.runtime.Reflection;

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
        
        // Set up application class loader
        ClassLoader cl = RVMClassLoader.getApplicationClassLoader();
        System.out.println("got class loader");
        Thread.currentThread().setContextClassLoader(cl);
        Atom xthreadAtom = Atom.findOrCreateUnicodeAtom("test/org/jikesrvm/basic/core/threads/XThread");
        Atom initAtom = Atom.findOrCreateAsciiAtom("init");
        TypeReference xthreadClass = TypeReference.findOrCreate(cl, xthreadAtom.descriptorFromClassName());
        System.out.println("Got xthread class");
        RVMMethod initMethod = xthreadClass.resolve().asClass().findDeclaredMethod(initAtom);
        System.out.println("Got xthread.init()");
        int i = 0;
        for (i = 0; i < tests.length; i++) 
        {
            String args[] = new String[1];
            args[0] = tests[i];
            System.out.println("Running " + tests[i]);
            MainThread program = new MainThread(args);
            Reflection.invoke(initMethod, null, null, null, true);
            program.run();
        }
    }
}
