// JoeqVM.java, created Sat Dec 14  2:52:34 2002 by mcmartin
// Copyright (C) 2001-3 jwhaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.Iterator;
import joeq.Allocator.SimpleAllocator;
import joeq.Bootstrap.MethodInvocation;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_StaticMethod;
import joeq.ClassLib.ClassLibInterface;
import joeq.Compiler.CompilationState;
import joeq.Compiler.CompilationState.DynamicCompilation;
import joeq.Runtime.Debug;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_InterrupterThread;
import joeq.Scheduler.jq_MainThread;
import joeq.Scheduler.jq_NativeThread;
import joeq.Scheduler.jq_Thread;
import joeq.UTF.Utf8;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: JoeqVM.java,v 1.19 2004/09/30 03:35:36 joewhaley Exp $
 */
public abstract class JoeqVM {
    public static void boot() throws Throwable {
        try {
            // initialize the thread data structures, allocators, etc.
            jq_NativeThread.initInitialNativeThread();

            // init the ctrl-break handler thread.
            jq_NativeThread.initBreakThread();

            // initialize dynamic compiler
            CompilationState.DEFAULT = new DynamicCompilation();
            
            // call java.lang.System.initializeSystemClass()
            ClassLibInterface.DEFAULT.initializeSystemClass();
            
        } catch (Throwable x) {
            SystemInterface.debugwriteln("Exception occurred during virtual machine initialization");
            SystemInterface.debugwriteln("Exception: " + x);
            if (System.err != null) x.printStackTrace(System.err);
            return;
        }
        
        if (jq.on_vm_startup != null) {
            Iterator it = jq.on_vm_startup.iterator();
            while (it.hasNext()) {
                MethodInvocation mi = (MethodInvocation) it.next();
                try {
                    mi.invoke();
                } catch (Throwable x) {
                    SystemInterface.debugwriteln("Exception occurred while initializing the virtual machine");
                    SystemInterface.debugwriteln(x.toString());
                    x.printStackTrace(System.err);
                    //return;
                }
            }
        }
        
        Debug.writeln("Joeq system initialized.");

        int numOfArgs = SystemInterface.main_argc();
        String[] args = new String[numOfArgs];
        for (int i = 0; i < numOfArgs; ++i) {
            int len = SystemInterface.main_argv_length(i);
            byte[] b = new byte[len];
            SystemInterface.main_argv(i, b);
            args[i] = new String(b);
        }
        String classpath = ".";
        int i = 0;
        for (; ;) {
            if (i == args.length) {
                printUsage();
                return;
            }
            if (args[i].equals("-cp") || args[i].equals("-classpath")) { // class path
                classpath = args[++i];
                ++i;
                // update classpath here.
                if (classpath != null) {
                    Iterator it = PrimordialClassLoader.classpaths(classpath);
                    while (it.hasNext()) {
                        String s = (String) it.next();
                        PrimordialClassLoader.loader.addToClasspath(s);
                    }
                }
                continue;
            }
            if (args[i].equals("-nt") || args[i].equals("-native_threads")) { // number of native threads
                jq.NumOfNativeThreads = Integer.parseInt(args[++i]);
                ++i;
                continue;
            }
            if (args[i].startsWith("-D")) { // system property
                String key;
                String value;
                int equals = args[i].indexOf('=');
                if (equals != -1) {
                    key = args[i].substring(2, equals);
                    value = args[i].substring(equals+1);
                } else {
                    key = args[i].substring(2);
                    value = "";
                }
                System.setProperty(key, value);
                ++i;
                continue;
            }
            if (args[i].startsWith("-mx")) { // max memory
                String amt = args[i].substring(3);
                int mult = 1;
                if (amt.endsWith("m") || amt.endsWith("M")) {
                    mult = 1048576;
                    amt = amt.substring(0, amt.length()-1);
                } else if (amt.endsWith("k") || amt.endsWith("K")) {
                    mult = 1024;
                    amt = amt.substring(0, amt.length()-1);
                }
                int size = mult * Integer.parseInt(amt);
                //size = HeapAddress.align(size, 20);
                SimpleAllocator.MAX_MEMORY = size;
                ++i;
                continue;
            }
            // todo: other command line switches to change VM behavior.
            int j = TraceFlags.setTraceFlag(args, i);
            if (i != j) {
                i = j;
                continue;
            }
            break;
        }

        handleSystemProperties();
        
        jq_Thread tb = Unsafe.getThreadBlock();
        jq_NativeThread nt = tb.getNativeThread();
        jq_NativeThread.initNativeThreads(nt, jq.NumOfNativeThreads);

        // Here we start method replacement of classes whose name were given as arguments to -replace on the cmd line.
        if (joeq.Class.jq_Class.TRACE_REPLACE_CLASS) SystemInterface.debugwriteln(Strings.lineSep+"STARTING REPLACEMENT of classes: " + joeq.Class.jq_Class.classToReplace);
        for (Iterator it = joeq.Class.jq_Class.classToReplace.iterator(); it.hasNext();) {
            String newCName = (String) it.next();
            PrimordialClassLoader.loader.replaceClass(newCName);
        }
        if (joeq.Class.jq_Class.TRACE_REPLACE_CLASS) SystemInterface.debugwriteln(Strings.lineSep+"DONE with Classes Replacement!");

        String className = args[i];
        jq_Class main_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("L" + className.replace('.', '/') + ";");
        main_class.load();
        jq_StaticMethod main_method = main_class.getStaticMethod(new jq_NameAndDesc(Utf8.get("main"), Utf8.get("([Ljava/lang/String;)V")));
        if (main_method == null) {
            System.err.println("Class " + className + " does not contain a main method!");
            return;
        }
        if (!main_method.isPublic()) {
            System.err.println("Method " + main_method + " is not public!");
            return;
        }
        main_class.cls_initialize();
        String[] main_args = new String[args.length - i - 1];
        System.arraycopy(args, i + 1, main_args, 0, main_args.length);

        //jq_CompiledCode main_cc = main_method.getDefaultCompiledVersion();
        //Reflection.invokestatic_V(main_method, main_args);
        jq_MainThread mt = new jq_MainThread(main_method, main_args);
        mt.start();
        jq_NativeThread.startNativeThreads();
        nt.nativeThreadEntry();
        Assert.UNREACHABLE();
    }

    public static void handleSystemProperties() {
        String s;
        
        s = System.getProperty("scheduler.quanta");
        if (s != null) {
            try {
                jq_InterrupterThread.QUANTA = Integer.parseInt(s);
                Debug.writeln("Scheduler quanta is ", jq_InterrupterThread.QUANTA);
            } catch (NumberFormatException x) {
                Debug.writeln("Bad scheduler.quanta, ignoring.");
            }
        }
        
        s = System.getProperty("scheduler.transfer");
        if (s != null) {
            try {
                jq_NativeThread.TRANSFER_THRESHOLD = Float.parseFloat(s);
                Debug.write("Scheduler transfer threshold is ");
                System.err.println(jq_NativeThread.TRANSFER_THRESHOLD);
            } catch (NumberFormatException x) {
                Debug.writeln("Bad scheduler.transfer, ignoring.");
            }
        }
        
        s = System.getProperty("scheduler.stack");
        if (s != null) {
            try {
                jq_Thread.INITIAL_STACK_SIZE = Integer.parseInt(s);
                Debug.writeln("Thread stack size is ", jq_Thread.INITIAL_STACK_SIZE);
            } catch (NumberFormatException x) {
                Debug.writeln("Bad scheduler.stack, ignoring.");
            }
        }
    }
    
    public static void printUsage() {
        System.out.println("Usage: joeq <classname> <parameters>");
    }
}
