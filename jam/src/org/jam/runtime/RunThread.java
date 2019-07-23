package org.jam.runtime;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeReference;
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
          if(DEBUG) VM.sysWriteln("Get classname Atom");
          Atom mainAtom = Atom.findOrCreateUnicodeAtom(threadClassName);
          if(DEBUG) VM.sysWriteln("Find type reference");
          TypeReference threadClass = TypeReference.findOrCreate(cl, mainAtom.descriptorFromClassName());
          if(DEBUG) VM.sysWriteln("Resolve 1");
          cls = threadClass.resolve().asClass();
          if(DEBUG) VM.sysWriteln("Resolve 2");
          cls.resolve();
          if(DEBUG) VM.sysWriteln("Instantiate");
          cls.instantiate();
          if(DEBUG) VM.sysWriteln("Initialized");
          cls.initialize();
        } catch (NoClassDefFoundError e) {
          if (DEBUG) VM.sysWrite("failed.]");
          // no such class
          VM.sysWrite(e + "\n");
          return;
        }
        if (DEBUG) VM.sysWriteln("loaded.]");
        
        /*
         * Find the constructor
         * 
         * For now only expecting one, the default. <init>()V
         */
        RVMMethod[] constructors = cls.getConstructorMethods();
        if(DEBUG) VM.sysWriteln("Constructor "+constructors[0].getSignature()+"/"+constructors[0].getName()+"/"+constructors[0].getDescriptor());
        TypeReference params[] = constructors[0].getParameterTypes();
        Object obj = RuntimeEntrypoints.resolvedNewScalar(cls);
        Reflection.invoke(constructors[0], null, obj, null, true);
    }
}
