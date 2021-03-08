package org.jikesrvm.runtime;

import org.jikesrvm.classloader.RVMType;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

public class Magic {
    static public Object addressAsObject(Address a) { return null; }
    static public Address objectAsAddress(Object a) { return null; }
    static public void storeStoreBarrier() {}
    static public void fence() {}
    static public void setIntAtOffset(Object o, Offset o1, int i) {}
    static public void setLongAtOffset(Object o, Offset o1, long i) {}
    static public void setObjectAtOffset(Object o, Offset o1, Object i) {}
    static public Object getObjectAtOffset(Object o, Offset o0, int i) { return null; }
    static public Object getObjectAtOffset(Object o, Offset o0) { return null; }
    public static void setObjectAtOffset(Object object, Offset offset, Object newvalue, int locationMetadata) {}
    public static int getIntAtOffset(Object o, Offset o0) { return 0; }
    public static long getLongAtOffset(Object o, Offset o0) { return 0; }
    public static void combinedLoadBarrier() {}
    public static int floatAsIntBits(float f) { return 0; }
    public static float intBitsAsFloat(int b) { return 0.0f; }
    public static double sqrt(double a) { return 0.0; }
    public static RVMType getObjectType(Object o) { return null; }
    public static double dceil(double d) { return 0.0; }
    public static long doubleAsLongBits(double d) { return 0; }
    public static double longBitsAsDouble(long l) { return 0.0; }
}
