package org.jikesrvm.runtime;

import org.jikesrvm.classloader.RVMMethod;

public class Reflection {
    public static boolean cacheInvokerInJavaLangReflect = false;
    public static Object invoke(RVMMethod method, ReflectionBase invoker, Object thisArg, Object[] otherArgs, boolean isNonvirtual) { return null; }
    public static boolean needsCheckArgs(ReflectionBase b) { return false; }
}
