package org.jikesrvm.classloader;

import java.lang.annotation.Annotation;

public class AnnotatedElement {
    public final Annotation[] getDeclaredAnnotations() { return null; }
    public final Annotation[][] getDeclaredParameterAnnotations() { return null; }
    public final Annotation[] getAnnotations() { return null; }
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) { return null; }
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return false; }
}
