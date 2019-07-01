package org.jam.runtime;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.TypeReference;
import org.vmmagic.pragma.Entrypoint;

/**
 * The class is used by startup to load and run threads as part of the 
 * JOE boot up
 * 
 * @author Joe Kulig
 *
 */
public class RunThread
{
    private static final boolean DEBUG = true;
    private String threadClassName;
    private String[] args;

    /**
     * The thread class to load and  run
     * @param threadClass
     */
    public RunThread(String threadClass)
    {
        this.threadClassName = threadClass;
        if(DEBUG) VM.sysWriteln("RunThread class: ",threadClassName);
    }
    
    public void setArgs(String args[])
    {
        this.args = args;
    }
    
    @Entrypoint
    public void run()
    {
        if(DEBUG) VM.sysWriteln("RunThread.run() starting");
        // Set up application class loader
        ClassLoader cl = RVMClassLoader.getApplicationClassLoader();
//        setContextClassLoader(cl);
       
        RVMClass cls = null;
        try {
          Atom mainAtom = Atom.findOrCreateUnicodeAtom(threadClassName);
          TypeReference threadClass = TypeReference.findOrCreate(cl, mainAtom.descriptorFromClassName());
          cls = threadClass.resolve().asClass();
          cls.resolve();
          cls.instantiate();
          cls.initialize();
        } catch (NoClassDefFoundError e) {
          if (DEBUG) VM.sysWrite("failed.]");
          // no such class
          VM.sysWrite(e + "\n");
          return;
        }
        if (DEBUG) VM.sysWriteln("loaded.]");
        
        
    }
}
