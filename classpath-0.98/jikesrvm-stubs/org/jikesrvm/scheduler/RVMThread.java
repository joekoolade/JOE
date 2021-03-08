package org.jikesrvm.scheduler;

public class RVMThread
{
    public static int availableProcessors = 0;
    public RVMThread(Thread t, long l, String s, boolean b, int p) { }
    public static void notifyAll(Object o) { }
    public static void wait(Object o) { }
    public static void wait(Object o, long millis) { }
    public static void notify(Object o) {}
    public static RVMThread getCurrentThread() { return null; }
    public boolean isCollectorThread() { return false; }
    public static void dumpStack() { }
    public void start() { }
    public void setName(String s) { }
    public String getName() { return null; }
    public Thread getJavaLangThread() { return null; }
    public boolean holdsLock(Object obj) { return false; } 
    public boolean isDaemonThread() { return false; }
    public int getPriority() { return 0; }
    public void setPriority(int p) { }
    public Thread.State getState() { return null; }
    public void join(long l, int i) { }
    public static void yieldNoHandshake() { }
    public static void sleep(long l, int i) { }
    public boolean isInterrupted() { return false; }
    public void interrupt() { }
    public void suspend() { }
    public void stop(Throwable t) { }
    public int countStackFrames() { return 0; }
    public void clearInterrupted() { } 
    public void resume() {}
    public void unpark() {}
    public void park(boolean b, long l) {}
    
}