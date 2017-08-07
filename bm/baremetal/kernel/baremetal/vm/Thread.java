/*
 * Created on Jul 13, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.vm;

import baremetal.kernel.Memory;
import baremetal.kernel.Scheduler;
import baremetal.platform.Console;
import baremetal.platform.Platform;
import baremetal.runtime.Class;

import java.lang.System;

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Thread {
  /*
   * Context layout starting from 0:
   * 
   * edi,esi,ebp,esp,ebx,edx,ecx,eax
   * error code (sometimes)
   * return address
   * code segment
   * eflags
   * 
   */
  int[] context;
  int[] stack;
  private final static int CONTEXT_SIZE = 16;
  
  /*
   * Java thread object
   */
  java.lang.Thread javaThread;
  Runnable runnable;
  
  /*
   * Class containing main method
   */
  java.lang.Class mainClass;
  boolean bootThread;
  
  public Thread next;
  public Thread back;
  
  static Thread sleepQueue;
  
  /*
   * Thread state
   */
  int state;
  
  static final int NULLTHREAD=-1;
  static final int INITIALIZING=0;
  static final int RUNNING=1;
  static final int WAITING=2;
  static final int ZOMBIE=3;
  
  public static boolean newThread;
  public int stackContext;
  public int interruptContext;
  
  /*
   * time remaining before being taken off 
   * the sleep queue
   */
  long sleep;
  
  static int INITIAL_STACK = 6*1024;
  // static java.lang.Thread currentThread;
  public static Scheduler scheduler = new Scheduler();
  
  Thread(java.lang.Thread thread, Runnable r) {
    javaThread = thread;
    runnable = r;
    bootThread = false;
    context = new int[16];
  }
  
  public Thread() {
    bootThread = true;
    context = new int[16];
  }
  
  public void runMain(java.lang.Class c) {
    
  }
  
  public void run() {
    javaThread.run();
  }
  
  /**
   * @return
   */
  public static java.lang.Thread currentThread() {
    // TODO Auto-generated method stub
    return Scheduler.currentThread.javaThread;
  }

  /**
   * @param thread
   */
  public static void destroy(java.lang.Thread thread) {
    // TODO Auto-generated method stub
    
  }

  /*
   * Save interrupt context
   */
  public void saveInterruptContext(int[] c) {
    for(int i=0; i<c.length; i++)
      context[i] = c[i];
    interruptContext = Memory.getAddress(c);
    stackContext = 0;
  }
  
  /*
   * Saves stack context
   */
  public void saveStackContext() {
    stackContext = frameAddress2();
    interruptContext = 0;
  }
  
  public native static int frameAddress2();
  /*
   * Restore interrupt context
   */
  public void restoreContext(int[] c) {
    for(int i=0; i<c.length; i++)
      c[i] = context[i];
  }
  /*
   * Called when thread is returns from run
   */
  void exit() {
    System.out.println("Thread exiting");
  }
  
  final static void systemExit() {
    System.out.println("System exit");
    Platform.cpu.halt();
  }
  /**
   * @param obj
   * @return
   */
  public static boolean holdsLock(Object obj) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @param thread
   */
  public static void interrupt(java.lang.Thread thread) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param thread
   * @param ms
   * @param ns
   */
  public static void join(java.lang.Thread thread, long ms, int ns) {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  public static void resume() {
    if(Scheduler.currentThread.stackContext!=0)
      Scheduler.currentThread.resumeFromStack();
    else
      Scheduler.currentThread.resumeFromInterrupt();
    
  }

  public void kernelResume() {
    if(stackContext!=0) {
      /*
       * stack before:
       * 
       * return address
       * ebp0             <- stackContext
       * 
       * stack after:
       * 
       * eflags
       * code segment
       * return address
       * ebp0             <- stackContext
       */
      // return address
      int value = Memory.read32(stackContext+4);
      Memory.write32(stackContext-4, value);
      // ebp0
      value = Memory.read32(stackContext);
      Memory.write32(stackContext-8, value);
      // code segment
      Memory.write32(stackContext, 8);
      // eflags
      Memory.write32(stackContext+4, 0x200);
      kernelReturnFromStack(stackContext-8);
    } else
      returnFromInterrupt(interruptContext);
    
  }

  public void resumeFromStack() {
    returnFromStack(stackContext);
    // should not get here
  }
  
  public native static void returnFromStack(int stack);
  public native static void kernelReturnFromStack(int stack);
  public native static void returnFromInterrupt(int stack);
  
  public void resumeFromInterrupt() {
    returnFromInterrupt(interruptContext);
    // should not get here
  }
  /**
   * 
   */
  public static void finish() {
    // TODO Auto-generated method stub
    
  }

  /**
   * 
   */
  public static void yield() {
    Scheduler.currentThread.saveStackContext();
    System.out.println("Yielding processor");
    scheduler.schedule();
    if(newThread) {
      newThread = false;
      resume();
    }
  }

  public static void processSleepQueue() {
    if(sleepQueue.next==null) return;
    
    Thread sleeper = sleepQueue.next;
    sleeper.sleep =- 1;
    if(sleeper.sleep<=0) {
      sleepQueue.next = sleeper.next;
      Scheduler.schedule(sleeper);
      System.out.println("Waking up!");
    }
  }
  /**
   * @param timeout time to sleep in milliseconds
   * @param nanos number of nanoseconds to sleep
   * 
   * sleep time = timeout + nanos
   * 
   */
  public static void sleep(long timeout, int nanos) {
    /*
     * ignoring nanos for now; timer does not have
     * the resolution
     */
    Thread t = Scheduler.currentThread;
    t.sleep = timeout;
    /*
     * Put thread onto the sleep queue. Queue is ordered
     * by next thread to wakeup from sleep in ascending
     * order.
     */
    if(sleepQueue.next == null) {
      /*
       * first thread on the queue
       */
      sleepQueue.next = t;
      t.next = null;
    } else {
      Thread prevThread=sleepQueue;
      Thread nextThread=sleepQueue.next;
      while (true) {
        if (timeout > nextThread.sleep) {
          timeout-=nextThread.sleep;
          nextThread=nextThread.next;
        } else {
          nextThread.sleep-=timeout;
          /*
           * insert before next thread
           */
          prevThread.next=t;
          t.next=nextThread;
        }
      }
    }
    yield();
  }

  public void scheduleMutex() {
    scheduler.mutexSchedule();
  }
  
  /**
   * @param thread
   */
  public static void start(java.lang.Thread thread, Runnable runnable) {
    Thread newThread = new Thread(thread, runnable);
    newThread.initialize();
    Scheduler.schedule(newThread);
  }

  public static void bootThread(java.lang.Class main) {
    sleepQueue.next = null;
    Thread t = new Thread();
    t.javaThread = null;
    Scheduler.currentThread = t;
    t.javaThread = new java.lang.Thread("Main");
    t.context = new int[16];
    t.stack = new int[INITIAL_STACK];
    int mainMethod = Class.getMethodCode(main, "main");
    int tosPtr = Memory.getAddress(t.stack)+12+INITIAL_STACK*4;
    /*
     * todo: see if this can be done statically.
     */
    int exitMethod = Class.getMethodCode(Thread.class, "systemExit");
    /*
     * Stack setup of main thread:
     * 
     * <EOS>
     * args[]
     * Thread.exit();
     * mainMethod
     * 
     */
    int stack = INITIAL_STACK-1;
    // args[] 
    t.stack[stack--] = 0;
    // Thread.exit() 
    t.stack[stack--] = exitMethod;
    // main method
    t.stack[stack--] = mainMethod;
    /*
     * todo: schedule this thread!
     */
    Console.write("Starting Main!\n");
    startThread(tosPtr-12);
    // Better not return
    throw new RuntimeException("bootThread: should not be here!");
  }
  
  static native void startThread(int stack);
  
  private void initialize() {
    stack = new int[INITIAL_STACK];
    int runMethod;
    int exitMethod;
    int threadObj;
    if(runnable!=null) {
      runMethod = Class.lookupInterfaceMethodIdx(Class.getClass(runnable), Runnable.class, 1);
      exitMethod = Class.getMethodCode(Thread.class, "exit");
      threadObj = Memory.getAddress(runnable);
    } else {
      runMethod = Class.getMethodCode(Class.getClass(javaThread), "run");
      exitMethod = 0;
      threadObj = Memory.getAddress(this);
    }
    int top = INITIAL_STACK-1;
    int stackPtr = Memory.getAddress(stack)+(2+INITIAL_STACK)*4;
    stack[top] = threadObj;
    stack[top-1] = 0;
    stack[top-2] = 0;
    stack[top-3] = threadObj;
    stack[top-4] = exitMethod;
//    stack[top-5] = stackPtr-8;
    stack[top-5] = 0x200; // eflags
    stack[top-6] = 0x8; // CS
    stack[top-7] = runMethod; // eip
    stack[top-8] = 0; // eax
    stack[top-9] = 0; // ecx
    stack[top-10] = 0; // edx
    stack[top-11] = 0; // ebx
    stack[top-12] = stackPtr-16; // esp0
    stack[top-13] = stackPtr-20; // ebp
    stack[top-14] = 0; // esi
    interruptContext = stackPtr-72; 
  }
  /**
   * @param e
   */
  public static void stop(Throwable e) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param thread
   * @param newPriority
   */
  public static void setPriority(java.lang.Thread thread, int newPriority) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param thread
   */
  public static void initialize(java.lang.Thread thread, Runnable r) {
    Thread t = new Thread(thread, r);
    
  }

  /**
   * @param thread
   */
  public static void suspend(java.lang.Thread thread) {
    // TODO Auto-generated method stub
    
  }

}
