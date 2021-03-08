package org.jikesrvm.classloader;

public class RVMField extends RVMMember {
    public TypeReference getType() { return null; }
    public boolean madeTraced() { return false; }
    public void setDoubleValueUnchecked(Object o,double d) {}
    public void setFloatValueUnchecked(Object o, float f) {}
    public void setLongValueUnchecked(Object o, long l) {}
    public void setIntValueUnchecked(Object o, int i) {}
    public void setShortValueUnchecked(Object o, short i) {}
    public void setCharValueUnchecked(Object o, char i) {}
    public void setByteValueUnchecked(Object o, byte i) {}
    public void setBooleanValueUnchecked(Object o, boolean i) {}
    public void setObjectValueUnchecked(Object o, Object i) {}
    public boolean isStatic() { return false; }
    public boolean getBooleanValueUnchecked(Object o) { return false; }
    public float getFloatValueUnchecked(Object o) { return 0.0f; }
    public double getDoubleValueUnchecked(Object o) { return 0.0; }
    public byte getByteValueUnchecked(Object o) { return 0; }
    public long getLongValueUnchecked(Object o) { return 0; }
    public short getShortValueUnchecked(Object o) { return 0; }
    public char getCharValueUnchecked(Object o) { return 0; }
    public int getIntValueUnchecked(Object o) { return 0; }
    public Object getObjectValueUnchecked(Object o) { return null; }
    public boolean isReferenceType() { return false; }
    public boolean isFinal() { return false; }
}
