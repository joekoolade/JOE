package org.jikesrvm.classloader;

public class RVMArray extends RVMType {
    public RVMType getInnermostElementType() { return null; }
    public RVMType getElementType() { return null; }
    public int getLogElementSize() { return 0; }
    public int getInstanceSize(int i) { return 0; }
    public static void arraycopy(float[] s, int sp, float[] d, int dp, int l) {}
    public static void arraycopy(double[] s, int sp, double[] d, int dp, int l) {}
    public static void arraycopy(long[] s, int sp, long[] d, int dp, int l) {}
    public static void arraycopy(int[] s, int sp, int[] d, int dp, int l) {}
    public static void arraycopy(short[] s, int sp, short[] d, int dp, int l) {}
    public static void arraycopy(char[] s, int sp, char[] d, int dp, int l) {}
    public static void arraycopy(byte[] s, int sp, byte[] d, int dp, int l) {}
    public static void arraycopy(boolean[] s, int sp, boolean[] d, int dp, int l) {}
    public static void arraycopy(Object[] s, int sp, Object[] d, int dp, int l) {}
}
