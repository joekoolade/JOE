package org.jikesrvm.classloader;

public class RVMType extends AnnotatedElement {
    public static final RVMClass JavaLangCloneableType = null;
    public static final RVMClass JavaIoSerializableType = null;
    public static final RVMField JavaLangRefReferenceReferenceField = null;
    public static final Primitive VoidType = null;
    public static final Primitive BooleanType = null;
    public static final Primitive ByteType = null;
    public static final Primitive ShortType = null;
    public static final Primitive IntType = null;
    public static final Primitive LongType = null;
    public static final Primitive FloatType = null;
    public static final Primitive DoubleType = null;
    public static final Primitive CharType = null;
    public static final RVMClass JavaLangObjectType = null;

    public final Class<?> getClassForType() { return null; }
    public boolean getDesiredAssertionStatus() { return false; }
    public boolean isClassType() { return false; }
    public final RVMClass asClass() { return null; }
    public boolean isArrayType() { return false; }
    public final RVMArray asArray() { return null; }
    public final ClassLoader getClassLoader() { return null; }
    public boolean isAssignableFrom(RVMType type) { return false; }
    public boolean isPrimitiveType() { return false; }
    public boolean isUnboxedType() { return false; }
    public boolean isInitialized() { return false; }
    public void resolve() { }
    public void instantiate() { }
    public void initialize() { }
    public RVMMethod[] getVirtualMethods() { return null; }
    public RVMMethod[] getStaticMethods() { return null; }
    public RVMField[] getStaticFields() { return null; }
    public RVMField[] getInstanceFields() { return null; }
    public RVMArray getArrayTypeForElementType() { return null; }
    public boolean isDoubleType() { return false; }
    public boolean isFloatType() { return false; }
    public boolean isLongType() { return false; }
    public boolean isIntType() { return false; }
    public boolean isCharType() { return false; }
    public boolean isShortType() { return false; }
    public boolean isByteType() { return false; }
    public boolean isBooleanType() { return false; }
    public boolean isVoidType() { return false; }
}
