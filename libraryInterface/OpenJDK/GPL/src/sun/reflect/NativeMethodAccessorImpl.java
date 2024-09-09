/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

import org.jikesrvm.VM;
import org.jikesrvm.classlibrary.ClassLibraryHelpers;
import org.jikesrvm.classlibrary.JavaLangReflectSupport;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.runtime.ReflectionBase;
import org.jikesrvm.runtime.Reflection;

/** Used only for the first few invocations of a Method; afterward,
    switches to bytecode-based implementation */

class NativeMethodAccessorImpl extends MethodAccessorImpl {
    private Method method;
    private DelegatingMethodAccessorImpl parent;
    private int numInvocations;

    NativeMethodAccessorImpl(Method method) {
        this.method = method;
    }

    public Object invoke(Object obj, Object[] args)
        throws IllegalArgumentException, InvocationTargetException
    {
        if (++numInvocations > ReflectionFactory.inflationThreshold()) {
            MethodAccessorImpl acc = (MethodAccessorImpl)
                new MethodAccessorGenerator().
                    generateMethod(method.getDeclaringClass(),
                                   method.getName(),
                                   method.getParameterTypes(),
                                   method.getReturnType(),
                                   method.getExceptionTypes(),
                                   method.getModifiers());
            parent.setDelegate(acc);
        }

        return invoke0(method, obj, args);
    }

    void setParent(DelegatingMethodAccessorImpl parent) {
        this.parent = parent;
    }

    private static Object invoke0(Method m, Object obj, Object[] args)
    {
        RVMClass type = JikesRVMSupport.getTypeForClass(m.getDeclaringClass()).asClass();
        if (VM.VerifyAssertions) VM._assert(type.isClassType());

        // Run the constructor on the it.
        RVMMethod rvmMethod = java.lang.reflect.JikesRVMSupport.getMethodOf(m);
        if (rvmMethod == null) {
          Class[] parameterTypes = m.getParameterTypes();
          StringBuilder descriptorBuilder = new StringBuilder();
          descriptorBuilder.append("(");
          for (Class parameterType : parameterTypes) {
            descriptorBuilder.append(java.lang.JikesRVMSupport.getTypeForClass(parameterType).getDescriptor().toString());
          }
          descriptorBuilder.append(")V");
          String descriptorAsString = descriptorBuilder.toString();
          Atom methodName = Atom.findOrCreateUnicodeAtom(m.getName());
          Atom methodDescriptor = Atom.findOrCreateUnicodeAtom(descriptorAsString);
          RVMMethod realMethod = type.findDeclaredMethod(methodName, methodDescriptor);
          if (VM.VerifyAssertions) VM._assert(realMethod != null);
          ClassLibraryHelpers.javaLangReflectMethod_rvmMethodField.setObjectValueUnchecked(m, realMethod);
          rvmMethod = java.lang.reflect.JikesRVMSupport.getMethodOf(m);
        }

        if (VM.VerifyAssertions) VM._assert(rvmMethod != null);

        RVMClass callerClass = RVMClass.getClassFromStackFrame(4);
        ReflectionBase invoker = (ReflectionBase) ClassLibraryHelpers.javaLangReflectMethod_invokerField.getObjectUnchecked(m);
        Object o = null;
        try
        {
            o = JavaLangReflectSupport.invoke(obj, args, rvmMethod, m, callerClass, invoker);
        } catch (IllegalAccessException | IllegalArgumentException | ExceptionInInitializerError
                | InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return o;
      }

}
