/*
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.reflect;

import java.lang.reflect.*;
import java.lang.JikesRVMSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.runtime.StackBrowser;

/** Common utility routines used by both java.lang and
    java.lang.reflect */

public class Reflection {

    /** Used to filter out fields and methods from certain classes from public
        view, where they are sensitive or they may contain VM-internal objects.
        These Maps are updated very rarely. Rather than synchronize on
        each access, we use copy-on-write */
    private static volatile Map<Class,String[]> fieldFilterMap;
    private static volatile Map<Class,String[]> methodFilterMap;

    private static final boolean DEBUG_GET_CALLER_CLASS = true;
    static {
        Map<Class,String[]> map = new HashMap<Class,String[]>();
        map.put(Reflection.class,
            new String[] {"fieldFilterMap", "methodFilterMap"});
        map.put(System.class, new String[] {"security"});
        fieldFilterMap = map;

        methodFilterMap = new HashMap<Class,String[]>();
    }
    
    public static Class<?> getCallerClass()
    {
    	return getCallerClass(0);
    }
    /** Returns the class of the method <code>realFramesToSkip</code>
        frames up the stack (zero-based), ignoring frames associated
        with java.lang.reflect.Method.invoke() and its implementation.
        The first frame is that associated with this method, so
        <code>getCallerClass(0)</code> returns the Class object for
        sun.reflect.Reflection. Frames associated with
        java.lang.reflect.Method.invoke() and its implementation are
        completely ignored and do not count toward the number of "real"
        frames skipped. */
    public static Class<?> getCallerClass(int i) {
        // TODO OPENJDK/ICEDTEA this implementation is rather messy. If we have to adjust this again,
        // we ought to write a better one, with a test case for all the cases.

        StackBrowser b = new StackBrowser();
        VM.disableGC();

        b.init();
//        int up = i;
//        VM.sysWriteln("up ", up);
//        while (up-- > 0)
//        {
//          b.up();
//        }
        b.up(); // skip sun.reflect.Reflection.getCallerClass (this call)
        b.up();
        /*
         * Skip Method.invoke and Constructor.newInstance, (if the caller was called by
         * reflection)
         */
        if (b.currentMethodIs_Java_Lang_Reflect_Method_InvokeMethod() || b.currentMethodIs_Java_Lang_Reflect_Constructor_NewInstance())
        {
            b.up();
        }
        /*
         * Work around OpenJDK's work around for Reflection.getCallerClass(..) in
         * java.lang.reflect.Method.invoke(..). The OpenJDK implementation of
         * getCallerClass assumes a fixed stack depth of 2. The Jikes RVM implementation
         * is different so we have to work around OpenJDK's work around
         */
        if (b.currentMethodIs_Java_Lang_Reflect_Method_GetCallerClass())
        {
            b.up();
        }
        
        /* Skip JNI if necessary */
        while (b.currentMethodIsPartOfJikesRVMJNIImplementation())
        {
            b.up();
        }
        
        /* Don't skip if we're already in the application */
        if (b.currentMethodIsInClassLibrary())
        {
            b.up(); // skip method that contains the call
        }
        RVMType ret = b.getCurrentClass();
        VM.enableGC();

        Class<?> clazz = ret.getClassForType();
        if (DEBUG_GET_CALLER_CLASS) {
          VM.sysWriteln("Returning caller class " + clazz + " for stack:");
//          RVMThread.dumpStack();
        }
        return clazz;
    }

    /** Retrieves the access flags written to the class file. For
        inner classes these flags may differ from those returned by
        Class.getModifiers(), which searches the InnerClasses
        attribute to find the source-level access flags. This is used
        instead of Class.getModifiers() for run-time access checks due
        to compatibility reasons; see 4471811. Only the values of the
        low 13 bits (i.e., a mask of 0x1FFF) are guaranteed to be
        valid. */
    private static int getClassAccessFlags(Class c) {
        RVMClass clazz = JikesRVMSupport.getTypeForClass(c).asClass();
        return clazz.getOriginalModifiers();
    }

    /** A quick "fast-path" check to try to avoid getCallerClass()
        calls. */
    public static boolean quickCheckMemberAccess(Class memberClass,
                                                 int modifiers)
    {
        return Modifier.isPublic(getClassAccessFlags(memberClass) & modifiers);
    }

    public static void ensureMemberAccess(Class currentClass,
                                          Class memberClass,
                                          Object target,
                                          int modifiers)
        throws IllegalAccessException
    {
        if (currentClass == null || memberClass == null) {
            throw new InternalError();
        }

        if (!verifyMemberAccess(currentClass, memberClass, target, modifiers)) {
            throw new IllegalAccessException("Class " + currentClass.getName() +
                                             " can not access a member of class " +
                                             memberClass.getName() +
                                             " with modifiers \"" +
                                             Modifier.toString(modifiers) +
                                             "\"");
        }
    }

    public static boolean verifyMemberAccess(Class currentClass,
                                             // Declaring class of field
                                             // or method
                                             Class  memberClass,
                                             // May be NULL in case of statics
                                             Object target,
                                             int    modifiers)
    {
        // Verify that currentClass can access a field, method, or
        // constructor of memberClass, where that member's access bits are
        // "modifiers".

        boolean gotIsSameClassPackage = false;
        boolean isSameClassPackage = false;

        if (currentClass == memberClass) {
            // Always succeeds
            return true;
        }

        if (!Modifier.isPublic(getClassAccessFlags(memberClass))) {
            isSameClassPackage = isSameClassPackage(currentClass, memberClass);
            gotIsSameClassPackage = true;
            if (!isSameClassPackage) {
                return false;
            }
        }

        // At this point we know that currentClass can access memberClass.

        if (Modifier.isPublic(modifiers)) {
            return true;
        }

        boolean successSoFar = false;

        if (Modifier.isProtected(modifiers)) {
            // See if currentClass is a subclass of memberClass
            if (isSubclassOf(currentClass, memberClass)) {
                successSoFar = true;
            }
        }

        if (!successSoFar && !Modifier.isPrivate(modifiers)) {
            if (!gotIsSameClassPackage) {
                isSameClassPackage = isSameClassPackage(currentClass,
                                                        memberClass);
                gotIsSameClassPackage = true;
            }

            if (isSameClassPackage) {
                successSoFar = true;
            }
        }

        if (!successSoFar) {
            return false;
        }

        if (Modifier.isProtected(modifiers)) {
            // Additional test for protected members: JLS 6.6.2
            Class targetClass = (target == null ? memberClass : target.getClass());
            if (targetClass != currentClass) {
                if (!gotIsSameClassPackage) {
                    isSameClassPackage = isSameClassPackage(currentClass, memberClass);
                    gotIsSameClassPackage = true;
                }
                if (!isSameClassPackage) {
                    if (!isSubclassOf(targetClass, currentClass)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isSameClassPackage(Class c1, Class c2) {
        return isSameClassPackage(c1.getClassLoader(), c1.getName(),
                                  c2.getClassLoader(), c2.getName());
    }

    /** Returns true if two classes are in the same package; classloader
        and classname information is enough to determine a class's package */
    private static boolean isSameClassPackage(ClassLoader loader1, String name1,
                                              ClassLoader loader2, String name2)
    {
        if (loader1 != loader2) {
            return false;
        } else {
            int lastDot1 = name1.lastIndexOf('.');
            int lastDot2 = name2.lastIndexOf('.');
            if ((lastDot1 == -1) || (lastDot2 == -1)) {
                // One of the two doesn't have a package.  Only return true
                // if the other one also doesn't have a package.
                return (lastDot1 == lastDot2);
            } else {
                int idx1 = 0;
                int idx2 = 0;

                // Skip over '['s
                if (name1.charAt(idx1) == '[') {
                    do {
                        idx1++;
                    } while (name1.charAt(idx1) == '[');
                    if (name1.charAt(idx1) != 'L') {
                        // Something is terribly wrong.  Shouldn't be here.
                        throw new InternalError("Illegal class name " + name1);
                    }
                }
                if (name2.charAt(idx2) == '[') {
                    do {
                        idx2++;
                    } while (name2.charAt(idx2) == '[');
                    if (name2.charAt(idx2) != 'L') {
                        // Something is terribly wrong.  Shouldn't be here.
                        throw new InternalError("Illegal class name " + name2);
                    }
                }

                // Check that package part is identical
                int length1 = lastDot1 - idx1;
                int length2 = lastDot2 - idx2;

                if (length1 != length2) {
                    return false;
                }
                return name1.regionMatches(false, idx1, name2, idx2, length1);
            }
        }
    }

    static boolean isSubclassOf(Class queryClass,
                                Class ofClass)
    {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }

    // fieldNames must contain only interned Strings
    public static synchronized void registerFieldsToFilter(Class containingClass,
                                              String ... fieldNames) {
        fieldFilterMap =
            registerFilter(fieldFilterMap, containingClass, fieldNames);
    }

    // methodNames must contain only interned Strings
    public static synchronized void registerMethodsToFilter(Class containingClass,
                                              String ... methodNames) {
        methodFilterMap =
            registerFilter(methodFilterMap, containingClass, methodNames);
    }

    private static Map<Class,String[]> registerFilter(Map<Class,String[]> map,
            Class containingClass, String ... names) {
        if (map.get(containingClass) != null) {
            throw new IllegalArgumentException
                            ("Filter already registered: " + containingClass);
        }
        map = new HashMap<Class,String[]>(map);
        map.put(containingClass, names);
        return map;
    }

    public static Field[] filterFields(Class containingClass,
                                       Field[] fields) {
        if (fieldFilterMap == null) {
            // Bootstrapping
            return fields;
        }
        return (Field[])filter(fields, fieldFilterMap.get(containingClass));
    }

    public static Method[] filterMethods(Class containingClass, Method[] methods) {
        if (methodFilterMap == null) {
            // Bootstrapping
            return methods;
        }
        return (Method[])filter(methods, methodFilterMap.get(containingClass));
    }

    private static Member[] filter(Member[] members, String[] filteredNames) {
        if ((filteredNames == null) || (members.length == 0)) {
            return members;
        }
        int numNewMembers = 0;
        for (Member member : members) {
            boolean shouldSkip = false;
            for (String filteredName : filteredNames) {
                if (member.getName() == filteredName) {
                    shouldSkip = true;
                    break;
                }
            }
            if (!shouldSkip) {
                ++numNewMembers;
            }
        }
        Member[] newMembers =
            (Member[])Array.newInstance(members[0].getClass(), numNewMembers);
        int destIdx = 0;
        for (Member member : members) {
            boolean shouldSkip = false;
            for (String filteredName : filteredNames) {
                if (member.getName() == filteredName) {
                    shouldSkip = true;
                    break;
                }
            }
            if (!shouldSkip) {
                newMembers[destIdx++] = member;
            }
        }
        return newMembers;
    }
}
