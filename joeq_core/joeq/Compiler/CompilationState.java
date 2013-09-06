// CompilationState.java, created Oct 4, 2003 11:09:20 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.Operator.CheckCast;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Main.jq;
import joeq.Runtime.TypeCheck;
import joeq.UTF.Utf8;
import joeq.Util.Templates.List;
import joeq.Util.Templates.UnmodifiableList;

/**
 * CompilationState
 * 
 * @author John Whaley
 * @version $Id: CompilationState.java,v 1.9 2004/09/22 22:17:46 joewhaley Exp $
 */
public abstract class CompilationState implements CompilationConstants {
    
    /** Default compilation state object.
     * This is here temporarily until we remove all static calls in the compiler. */
    public static CompilationState DEFAULT;
    
    public static final boolean VerifyAssertions = true;
    
    public abstract boolean needsDynamicLink(jq_Method method, jq_Member member);
    public abstract boolean needsDynamicLink(jq_Method method, jq_Type type);
    
    public abstract jq_Member tryResolve(jq_Member m);
    public abstract jq_Member resolve(jq_Member m);
    
    public abstract byte isSubtype(jq_Type t1, jq_Type t2);
    public abstract jq_Type findCommonSuperclass(jq_Type t1, jq_Type t2);
    
    public abstract byte declaresInterface(jq_Class klass, Collection interfaces);
    public abstract byte implementsInterface(jq_Class klass, jq_Class inter);
    
    public abstract jq_Type getOrCreateType(Utf8 desc);

    public List.jq_Class getThrownExceptions(Quad q) {
        return q.getOperator().getThrownExceptions();
    }
    
    static {
        if (jq.nullVM) {
            DEFAULT = new StaticCompilation();
        } else if (jq.IsBootstrapping) {
            DEFAULT = new BootstrapCompilation();
        } else {
            DEFAULT = new DynamicCompilation();
        }
    }
    
    public static class StaticCompilation extends CompilationState {

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#needsDynamicLink(Class.jq_Method, Class.jq_Member)
         */
        public boolean needsDynamicLink(jq_Method method, jq_Member member) {
            if (member.isPrepared()) return false;
            member.getDeclaringClass().prepare();
            return !member.isPrepared();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#needsDynamicLink(Class.jq_Method, Class.jq_Type)
         */
        public boolean needsDynamicLink(jq_Method method, jq_Type type) {
            type.prepare();
            return !type.isPrepared();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#tryResolve(Class.jq_Member)
         */
        public jq_Member tryResolve(jq_Member m) {
            try {
                m = m.resolve();
            } catch (Error _) { }
            return m;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#resolve(Class.jq_Member)
         */
        public jq_Member resolve(jq_Member m) {
            return m.resolve();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#isSubtype(Class.jq_Type, Class.jq_Type)
         */
        public byte isSubtype(jq_Type t1, jq_Type t2) {
            t1.prepare(); t2.prepare();
            return t1.isSubtypeOf(t2) ? YES : NO;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#findCommonSuperclass(Class.jq_Type, Class.jq_Type)
         */
        public jq_Type findCommonSuperclass(jq_Type t1, jq_Type t2) {
            return TypeCheck.findCommonSuperclass(t1, t2, true);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#declaresInterface(Class.jq_Class, java.util.Collection)
         */
        public byte declaresInterface(jq_Class klass, Collection interfaces) {
            return TypeCheck.declaresInterface(klass, interfaces, true);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#implementsInterface(Class.jq_Class, Class.jq_Class)
         */
        public byte implementsInterface(jq_Class klass, jq_Class inter) {
            klass.prepare();
            inter.prepare();
            return klass.isSubtypeOf(inter) ? YES : NO;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#getOrCreateType(UTF.Utf8)
         */
        public jq_Type getOrCreateType(Utf8 desc) {
            return PrimordialClassLoader.loader.getOrCreateBSType(desc);
        }

        /** Assume jq_Method.getThrownExceptions() returns correct information */
        public static boolean ASSUME_CORRECT_EXCEPTIONS = true;

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#getThrownExceptions(Quad)
         */
        public List.jq_Class getThrownExceptions(Quad q) {
            if (q.getOperator() == CheckCast.CHECKCAST.INSTANCE) {
                return new UnmodifiableList.jq_Class(PrimordialClassLoader
                        .getJavaLangClassCastException());
            }
            if (ASSUME_CORRECT_EXCEPTIONS && q.getOperator() instanceof Invoke) {
                jq_Method m = (jq_Method) resolve(Invoke.getMethod(q)
                        .getMethod());
                UnmodifiableList.jq_Class exclist;
                exclist = (UnmodifiableList.jq_Class) cachedThrownExcListByMethod
                        .get(m);
                if (exclist != null)
                    return exclist;
                /*
                 * Exception lists reflect exactly what was given in the
                 * 'throws' clause at compile time. These lists can (and do)
                 * contain redundant declarations of various kinds of Errors
                 * and RuntimeExceptions. It is possible to have entries that
                 * are superclasses of other entries in this list. We always
                 * add Error and RuntimeException because every method can
                 * throw these. See also VM Spec Section 4.7.4
                 */
                jq_Class[] exc = m.getThrownExceptionsTable();
                int exclistLength = defaultThrowables.length;
                if (exc != null)
                    exclistLength += exc.length;
                jq_Class[] tlist = new jq_Class[exclistLength];
                System.arraycopy(defaultThrowables, 0, tlist, 0,
                        defaultThrowables.length);
                if (exc != null)
                    System.arraycopy(exc, 0, tlist, defaultThrowables.length,
                            exc.length);
                // potential for memory savings here: could sort and intern
                // identical exclists
                cachedThrownExcListByMethod.put(m,
                        exclist = new UnmodifiableList.jq_Class(tlist));
                return exclist;
            }
            return super.getThrownExceptions(q);
        }
        private static HashMap/* <jq_Method, UnmodifiableList.jq_Class> */cachedThrownExcListByMethod = new HashMap();
        private static final jq_Class[] defaultThrowables = new jq_Class[]{
                PrimordialClassLoader.getJavaLangRuntimeException(),
                PrimordialClassLoader.getJavaLangError()};
    }

    public static class BootstrapCompilation extends CompilationState {

        Set/*<jq_Type>*/ boot_types;

        public void setBootTypes(Set boot_types) {
            this.boot_types = boot_types;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#needsDynamicLink(Class.jq_Method, Class.jq_Member)
         */
        public boolean needsDynamicLink(jq_Method method, jq_Member member) {
            if (member.isPrepared()) return false;
            return boot_types == null ||
                   !boot_types.contains(member.getDeclaringClass());
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#needsDynamicLink(Class.jq_Method, Class.jq_Type)
         */
        public boolean needsDynamicLink(jq_Method method, jq_Type type) {
            if (type.isPrepared()) return false;
            return boot_types == null ||
                   !boot_types.contains(type);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#tryResolve(Class.jq_Member)
         */
        public jq_Member tryResolve(jq_Member m) {
            try {
                m = m.resolve();
            } catch (Error _) { }
            return m;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#resolve(Class.jq_Member)
         */
        public jq_Member resolve(jq_Member m) {
            return m.resolve();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#isSubtype(Class.jq_Type, Class.jq_Type)
         */
        public byte isSubtype(jq_Type t1, jq_Type t2) {
            return TypeCheck.isAssignable_noload(t1, t2);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#findCommonSuperclass(Class.jq_Type, Class.jq_Type)
         */
        public jq_Type findCommonSuperclass(jq_Type t1, jq_Type t2) {
            return TypeCheck.findCommonSuperclass(t1, t2, false);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#declaresInterface(Class.jq_Class, java.util.Collection)
         */
        public byte declaresInterface(jq_Class klass, Collection interfaces) {
            return TypeCheck.declaresInterface(klass, interfaces, false);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#implementsInterface(Class.jq_Class, Class.jq_Class)
         */
        public byte implementsInterface(jq_Class klass, jq_Class inter) {
            return TypeCheck.implementsInterface_noload(klass, inter);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#getOrCreateType(UTF.Utf8)
         */
        public jq_Type getOrCreateType(Utf8 desc) {
            return PrimordialClassLoader.loader.getOrCreateBSType(desc);
        }
        
    }
    
    public static class DynamicCompilation extends CompilationState {

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#needsDynamicLink(Class.jq_Method, Class.jq_Member)
         */
        public boolean needsDynamicLink(jq_Method method, jq_Member member) {
            if (member.isStatic() &&
                method.getDeclaringClass() != member.getDeclaringClass() &&
                !member.getDeclaringClass().isClsInitialized())
                return true;
            if (member instanceof jq_Method)
                return !member.isInitialized();
            return !member.isPrepared();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#needsDynamicLink(Class.jq_Method, Class.jq_Type)
         */
        public boolean needsDynamicLink(jq_Method method, jq_Type type) {
            return method.getDeclaringClass() != type &&
                   !type.isClsInitialized();
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#tryResolve(Class.jq_Member)
         */
        public jq_Member tryResolve(jq_Member m) {
            if (m.getDeclaringClass().isPrepared()) {
                try {
                    m = m.resolve();
                } catch (Error _) { }
            }
            return m;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#resolve(Class.jq_Member)
         */
        public jq_Member resolve(jq_Member m) {
            try {
                m = m.resolve();
            } catch (Error _) { }
            return m;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#isSubtype(Class.jq_Type, Class.jq_Type)
         */
        public byte isSubtype(jq_Type t1, jq_Type t2) {
            return TypeCheck.isAssignable_noload(t1, t2);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#findCommonSuperclass(Class.jq_Type, Class.jq_Type)
         */
        public jq_Type findCommonSuperclass(jq_Type t1, jq_Type t2) {
            return TypeCheck.findCommonSuperclass(t1, t2, false);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#declaresInterface(Class.jq_Class, java.util.Collection)
         */
        public byte declaresInterface(jq_Class klass, Collection interfaces) {
            return TypeCheck.declaresInterface(klass, interfaces, false);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#implementsInterface(Class.jq_Class, Class.jq_Class)
         */
        public byte implementsInterface(jq_Class klass, jq_Class inter) {
            return TypeCheck.implementsInterface_noload(klass, inter);
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.CompilationState#getOrCreateType(UTF.Utf8)
         */
        public jq_Type getOrCreateType(Utf8 desc) {
            return PrimordialClassLoader.loader.getOrCreateBSType(desc);
        }
    }
    
}
