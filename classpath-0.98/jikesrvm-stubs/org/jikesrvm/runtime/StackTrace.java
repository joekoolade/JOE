package org.jikesrvm.runtime;

import org.jikesrvm.runtime.StackTrace.Element;

public class StackTrace {
    public static class Element { 
        public int getLineNumber() { return 0; }
        public String getClassName() { return null; }
        public boolean isNative() { return false; }
        public String getFileName() { return null; }
        public String getMethodName() { return null; }
        public Class getElementClass() { return null; }
    }
    public Element[] getStackTrace(Throwable cause) { return null; }

}
