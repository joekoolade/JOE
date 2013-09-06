// ObjectTraverser.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import java.util.Iterator;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_StaticField;
import joeq.ClassLib.ClassLibInterface;
import joeq.Main.jq;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * ObjectTraverser
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ObjectTraverser.java,v 1.32 2004/09/22 22:17:43 joewhaley Exp $
 */
public abstract class ObjectTraverser {

    public abstract void initialize();
    public abstract Object mapStaticField(jq_StaticField f);
    public abstract Object mapInstanceField(Object o, jq_InstanceField f);
    public abstract Object mapValue(Object o);
    
    public static /*final*/ boolean TRACE = false;
    public static final PrintStream out = System.out;
    
    public static final java.lang.Object NO_OBJECT = new java.lang.Object();
    
    public Object getStaticFieldValue(jq_StaticField f) {
        if (jq.IsBootstrapping) {
            java.lang.Object result = this.mapStaticField(f);
            if (result != NO_OBJECT) return this.mapValue(result);
        }
        // get the value via real reflection.
        if (TRACE) out.println("Getting value of static field "+f+" via reflection");
        Field f2 = (Field) f.getJavaLangReflectMemberObject();
        if (f2 == null) {
            Class c = Reflection.getJDKType(f.getDeclaringClass());
            String fieldName = f.getName().toString();
            f2 = lookupField(c, fieldName);
            if (f2 == null) {
                out.println("Cannot find static field "+f);
                return null;
            }
        }
        return getStaticFieldValue_reflection(f2);
    }

    public static Field lookupField(Class c, String fieldName) {
        Field f2 = Reflection.getJDKField(c, fieldName);
        if (f2 == null) {
            jq_Class klass = (jq_Class)Reflection.getJQType(c);
            for (Iterator i = ClassLibInterface.DEFAULT.getImplementationClassDescs(klass.getDesc()); i.hasNext(); ) {
                Utf8 u = (Utf8)i.next();
                if (TRACE) out.println("Checking mirror class "+u);
                String s = u.toString();
                Assert._assert(s.charAt(0) == 'L');
                try {
                    c = Class.forName(s.substring(1, s.length()-1).replace('/', '.'));
                    f2 = Reflection.getJDKField(c, fieldName);
                    if (f2 != null) break;
                } catch (ClassNotFoundException x) {
                    if (TRACE) out.println("Mirror class "+s+" doesn't exist");
                }
            }
        }
        return f2;
    }

    public Object getStaticFieldValue_reflection(Field f2) {
        f2.setAccessible(true);
        Assert._assert((f2.getModifiers() & Modifier.STATIC) != 0);
        try {
            Object o = f2.get(null);
            if (jq.IsBootstrapping) o = this.mapValue(o);
            return o;
        } catch (IllegalAccessException x) {
            Assert.UNREACHABLE();
            return null;
        }
    }
    
    public Object getInstanceFieldValue(Object base, jq_InstanceField f) {
        if (jq.IsBootstrapping) {
            java.lang.Object result = this.mapInstanceField(base, f);
            if (result != NO_OBJECT) return this.mapValue(result);
        }
        // get the value via real reflection.
        if (TRACE) out.println("Getting value of instance field "+f+" via reflection");
        Field f2 = (Field) f.getJavaLangReflectMemberObject();
        if (f2 == null) {
            Class c = Reflection.getJDKType(f.getDeclaringClass());
            String fieldName = f.getName().toString();
            f2 = lookupField(c, fieldName);
            if (f2 == null) {
                out.println("Cannot find instance field "+f);
                return null;
            }
        }
        return getInstanceFieldValue_reflection(base, f2);
    }
    public Object getInstanceFieldValue_reflection(Object base, Field f2) {
        f2.setAccessible(true);
        Assert._assert((f2.getModifiers() & Modifier.STATIC) == 0);
        try {
            Object o = f2.get(base);
            if (jq.IsBootstrapping) o = this.mapValue(o);
            return o;
        } catch (IllegalAccessException x) {
            Assert.UNREACHABLE();
            return null;
        }
    }
    
    public void putStaticFieldValue(jq_StaticField f, Object o) {
        Field f2 = (Field) f.getJavaLangReflectMemberObject();
        if (f2 == null) {
            Class c = Reflection.getJDKType(f.getDeclaringClass());
            String fieldName = f.getName().toString();
            f2 = lookupField(c, fieldName);
        }
        putStaticFieldValue_reflection(f2, o);
    }
    public void putStaticFieldValue_reflection(Field f2, Object o) {
        if (TRACE) out.println("Setting value of static field "+f2+" via reflection");
        f2.setAccessible(true);
        Assert._assert((f2.getModifiers() & Modifier.STATIC) != 0);
        try {
            f2.set(null, o);
        } catch (IllegalAccessException x) {
            Assert.UNREACHABLE();
        }
    }
    
    public void putInstanceFieldValue(Object base, jq_InstanceField f, Object o) {
        Field f2 = (Field) f.getJavaLangReflectMemberObject();
        if (f2 == null) {
            Class c = Reflection.getJDKType(f.getDeclaringClass());
            String fieldName = f.getName().toString();
            f2 = lookupField(c, fieldName);
        }
        putInstanceFieldValue_reflection(base, f2, o);
    }
    public void putInstanceFieldValue_reflection(Object base, Field f2, Object o) {
        if (TRACE) out.println("Setting value of static field "+f2+" via reflection");
        f2.setAccessible(true);
        Assert._assert((f2.getModifiers() & Modifier.STATIC) == 0);
        try {
            f2.set(base, o);
        } catch (IllegalAccessException x) {
            Assert.UNREACHABLE();
        }
    }
}
