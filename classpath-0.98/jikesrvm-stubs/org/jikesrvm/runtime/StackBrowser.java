package org.jikesrvm.runtime;

import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMMethod;

public class StackBrowser {
    public void init() { }
    public boolean hasMoreFrames() { return false; }
    public void up() { }
    public RVMClass getCurrentClass() { return null; }
    public RVMMethod getMethod() { return null; }

}
