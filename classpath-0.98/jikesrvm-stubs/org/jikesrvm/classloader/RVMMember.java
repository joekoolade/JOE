package org.jikesrvm.classloader;

import org.vmmagic.unboxed.Offset;

public class RVMMember extends AnnotatedElement {
    public final boolean isPublic() { return false; }
    public  Atom getName() { return null; }
    public final RVMClass getDeclaringClass() { return null; }
    public int getModifiers() { return 0; }
    public final Atom getSignature() { return null; }
    public final Offset getOffset() { return null; }
    public final int getId() { return 0; }
    public boolean isProtected() { return false; }
    public boolean isPrivate() { return false; }
}
