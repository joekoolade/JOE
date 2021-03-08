package org.jikesrvm;

import org.vmmagic.unboxed.Address;

public class VM {
    public static boolean fullyBooted  = false;
    public static boolean BuildFor32Addr = false;
    public static boolean VerifyAssertions = false;
    public static boolean runningVM = false;
    public static boolean BuildForOsx = false;
    public static boolean BuildForSolaris = false;
    public static boolean BuildForLinux = false;
    public static boolean BuildForHwFsqrt = false;
    public static boolean LittleEndian = false;
    
    public static void disableGC() {}
    public static void enableGC() {}
    public static void sysWriteln(String s) { }
    public static void sysWriteln() { }
    public static void sysFail(String s) { }
    public static void sysWrite(String s) { }
    public static void sysWrite(int s) { }
    public static void sysWriteln(String s, Address a) {}
    public static void _assert(boolean c) {}
    public static void sysExit(int i) {}
}
