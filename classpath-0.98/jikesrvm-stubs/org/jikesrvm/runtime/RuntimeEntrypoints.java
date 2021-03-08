package org.jikesrvm.runtime;

import org.jikesrvm.classloader.RVMArray;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.RVMType;

public class RuntimeEntrypoints {
    public static Object clone(Object obj) { return null; }
    public static void initializeClassForDynamicLink(RVMClass cls) { }
    public static Object resolvedNewScalar(RVMClass cls) { return null; }
    public static void athrow(Throwable t) {}
    public static void raiseArrayStoreException() {}
    public static boolean isAssignableWith(RVMClass c, RVMClass t) { return false; }
    public static boolean isAssignableWith(RVMType c, RVMType t) { return false; }
    public static Object buildMDAHelper(RVMMethod o, int[] a, int d, RVMArray at) { return null; }
    public static void raiseNullPointerException() {}
    public static Object resolvedNewArray(int l, RVMArray a) { return null; }
}
