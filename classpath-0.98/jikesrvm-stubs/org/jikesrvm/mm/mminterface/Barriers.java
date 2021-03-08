package org.jikesrvm.mm.mminterface;

import org.vmmagic.unboxed.Offset;

public class Barriers {
    public static final boolean  NEEDS_OBJECT_GETFIELD_BARRIER     = false;
    public static final boolean  NEEDS_OBJECT_PUTFIELD_BARRIER     = false;
    public static final boolean NEEDS_JAVA_LANG_REFERENCE_READ_BARRIER = false;
    public static final boolean NEEDS_INT_PUTFIELD_BARRIER = false;
    public static final boolean NEEDS_LONG_PUTFIELD_BARRIER = false;
    public static void intFieldWrite(Object ref, int value, Offset offset, int locationMetadata) {}
    public static void longFieldWrite(Object ref, long value, Offset offset, int locationMetadata) {}
    public static void objectFieldWrite(Object ref, Object value, Offset offset, int locationMetadata) {}
    public static Object javaLangReferenceReadBarrier(Object obj) { return null; }
    public static Object objectFieldRead(Object ref, Offset offset, int locationMetadata) { return null; }
}
