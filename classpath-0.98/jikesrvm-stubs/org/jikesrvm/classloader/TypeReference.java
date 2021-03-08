package org.jikesrvm.classloader;

public class TypeReference {
	public RVMClass resolve() { return null; }
    public static TypeReference findOrCreate(String tn) { return null; }
    public static synchronized TypeReference findOrCreate(ClassLoader cl, Atom tn) throws IllegalArgumentException { return null; }
    public RVMType peekType() { return null; }
    public boolean isBooleanType() { return false; }
    public boolean isFloatType() { return false; }
    public boolean isDoubleType() { return false; }
    public boolean isByteType() { return false; }
    public boolean isLongType() { return false; }
    public boolean isIntType() { return false; }
    public boolean isShortType() { return false; }
    public boolean isCharType() { return false; }
}
