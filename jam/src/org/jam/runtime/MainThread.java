/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jam.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jikesrvm.Callbacks;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.runtime.Reflection;
import org.vmmagic.pragma.Entrypoint;

/**
 * Thread in which user's "main" program runs.
 */
public final class MainThread extends Thread {
  private final String[] args;
  private RVMMethod mainMethod;
  protected boolean launched = false;

  private static final boolean dbg = true;

  /**
   * Create "main" thread.
   * @param args {@code args[0]}: name of class containing "main" method;
   *  {@code args[1..N]}: parameters to pass to "main" method
   */
  public MainThread(String[] args) {
    super("MainThread");
    setDaemon(false); // NB otherwise we inherit the boot threads daemon status
    this.args = args;
    if (dbg) {
      VM.sysWriteln("MainThread(args.length == ", args.length, "): constructor done");
    }
  }

  RVMMethod getMainMethod() {
    return mainMethod;
  }

  /**
   * Run "main" thread.
   * <p>
   * This code could be made a little shorter by relying on Reflection
   * to do the classloading and compilation.  We intentionally do it here
   * to give us a chance to provide error messages that are specific to
   * not being able to find the main class the user wants to run.
   * This may be a little silly, since it results in code duplication
   * just to provide debug messages in a place where very little is actually
   * likely to go wrong, but there you have it....
   */
  @Override
  @Entrypoint
  public void run() {
    launched = true;

    if (dbg) VM.sysWriteln("MainThread.run() starting ");

    // Set up application class loader
    ClassLoader cl = RVMClassLoader.getApplicationClassLoader();
    setContextClassLoader(cl);

    if (dbg) VM.sysWrite("[MainThread.run() loading class to run... ");
    // find method to run
    // load class specified by args[0]
    RVMClass cls = null;
    try {
      Atom mainAtom = Atom.findOrCreateUnicodeAtom(args[0].replace('.', '/'));
      TypeReference mainClass = TypeReference.findOrCreate(cl, mainAtom.descriptorFromClassName());
      cls = mainClass.resolve().asClass();
      cls.resolve();
      cls.instantiate();
      cls.initialize();
    } catch (NoClassDefFoundError e) {
      if (dbg) VM.sysWrite("failed.]");
      // no such class
      VM.sysWrite(e + "\n");
      return;
    }
    if (dbg) VM.sysWriteln("loaded.]");

    // find "main" method
    //
    mainMethod = cls.findMainMethod();
    if (mainMethod == null) {
      // no such method
      VM.sysWrite(cls + " doesn't have a \"public static void main(String[])\" method to execute\n");
      return;
    }

    if (dbg) VM.sysWrite("[MainThread.run() making arg list... ");
    // create "main" argument list
    //
    String[] mainArgs = new String[args.length - 1];
    for (int i = 0, n = mainArgs.length; i < n; ++i) {
      mainArgs[i] = args[i + 1];
    }
    if (dbg) VM.sysWriteln("made.]");

    if (dbg) VM.sysWrite("[MainThread.run() compiling main(String[])... ");
    mainMethod.compile();
    if (dbg) VM.sysWriteln("compiled.]");

    // Notify other clients that the startup is complete.
    //
    Callbacks.notifyStartup();

    if (dbg) VM.sysWriteln("[MainThread.run() invoking \"main\" method... ");
    // invoke "main" method with argument list
    Reflection.invoke(mainMethod, null, null, new Object[]{mainArgs}, true);
    if (dbg) VM.sysWriteln("  MainThread.run(): \"main\" method completed.]");
  }
}
