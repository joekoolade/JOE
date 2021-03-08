package org.jikesrvm.classloader;

import java.lang.annotation.Annotation;

import org.jikesrvm.runtime.ReflectionBase;

public class RVMMethod extends RVMMember {

	public ReflectionBase getInvoker() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeReference[] getExceptionTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeReference[] getParameterTypes() {
		return null;
	}
	
	public Object getAnnotationDefault() {
		return null;
	}
	
	public final TypeReference getReturnType() {
		return null;
	}
	public final boolean isClassInitializer() { return false; }
	public final boolean isObjectInitializer() { return false; }
	public void compile() {}
	public boolean isNative() { return false; }
	public boolean isAbstract() { return false; }
	public boolean isStatic() { return false; }
	public Atom getDescriptor() { return null; }
}
