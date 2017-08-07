package java.lang.reflect;

import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.runtime.ReflectionBase;

public class VMCommonLibrarySupport {
	static Class<?>[] typesToClasses(TypeReference[] types) {
		return null;
	}
	
	static Object construct(RVMMethod constructor, Constructor<?> cons, Object[] args, RVMClass accessingClass, ReflectionBase invoker) {
		return null;
	}

	static Object invoke(Object receiver, Object[] args, RVMMethod m, Method m0, RVMClass c, ReflectionBase ref) {
		return null;
	}
}
