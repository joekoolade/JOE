package org.jikesrvm.classloader;

public class RVMClass extends RVMType {
//	public final Class<?> getClassForType() { return null;	}
	
	public static RVMClass getClassFromStackFrame(int skip) { return null; }
	public int getModifiers() { return 0; }
	public boolean isInterface() { return false; }
	public RVMClass getSuperClass() { return null; }
	public boolean isAnnotation() { return false; }
	public boolean isSynthetic() { return false; }
	public boolean isAnonymousClass() { return false; }
	public boolean isLocalClass() { return false; }
    public boolean isMemberClass() { return false; }
    public Atom getSignature() { return null; }
    public TypeReference getDeclaringClass() { return null; }
    public TypeReference[] getDeclaredClasses() { return null; }
    public boolean isPublic() { return false; }
    public static ClassLoader getClassLoaderFromStackFrame(int skip) { return null; }
    public RVMClass[] getDeclaredInterfaces() { return null; }
    public TypeReference getEnclosingClass() { return null; }
    public boolean isEnum() { return false; }
    public RVMMethod[] getConstructorMethods() { return null; }
    public boolean isInitialized() { return false; }
    public boolean isAbstract() { return false; }
    public RVMMethod[] getDeclaredMethods() { return null; }
    public RVMField[] getDeclaredFields() { return null; }
    public RVMField findDeclaredField(Atom fieldName) { return null; }
    public int getInstanceSize() { return 0; }
    public RVMMethod getClassInitializerMethod() { return null; }
    public String getPackageName() { return null; }
    public boolean isResolved() { return false; }
    public RVMMethod findVirtualMethod(Atom a, Atom b) { return null; }
    public static RVMType defineClassInternal(String n, byte[] a, int i, int j, ClassLoader l) { return null; }

}
