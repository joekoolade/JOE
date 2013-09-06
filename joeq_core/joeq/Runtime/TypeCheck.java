// TypeCheck.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import java.util.Collection;
import java.util.Stack;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Compiler.CompilationConstants;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import jwutil.util.Assert;

/**
 * Implements Java type checking.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: TypeCheck.java,v 1.25 2004/09/22 22:17:43 joewhaley Exp $
 */
public abstract class TypeCheck implements jq_ClassFileConstants, CompilationConstants {
    
    public static final boolean VerifyAssertions = true;
    
    /** Performs a checkcast operation. */
    public static Object checkcast(Object k, jq_Type t) {
        if (k != null) {
            jq_Type t2 = jq_Reference.getTypeOf(k);
            if (!TypeCheck.isAssignable(t2, t)) 
                throw new ClassCastException(t2+" cannot be cast into "+t);
        }
        return k;
    }
    
    /** Performs an instanceof operation. */
    public static boolean instance_of(Object k, jq_Type t) {
        if (k == null)
            return false;
        jq_Type t2 = jq_Reference.getTypeOf(k);
        if (!TypeCheck.isAssignable(t2, t)) 
            return false;
        return true;
    }
    
    /** Performs an arrayStoreCheck operation. */
    public static void arrayStoreCheck(HeapAddress value, Object[] arrayref) 
    throws ArrayStoreException {
        if (value.isNull()) return;
        jq_Array a = (jq_Array)jq_Reference.getTypeOf(arrayref);
        jq_Type t2 = a.getElementType();
        if (t2.isAddressType()) return;
        Object v = value.asObject();
        jq_Type t = jq_Reference.getTypeOf(v);
        if (!isAssignable(t, t2))
            throw new ArrayStoreException(t+" into array "+a);
    }
    
    /**
     * Returns true if "T = S;" would be legal. (T is same or supertype of S)
     * From the algorithm in vm spec under "checkcast"
     * 
     * @param S subtype
     * @param T type
     * @return true iff "T = S;" would be legal.
     */
    public static boolean isAssignable(jq_Type S, jq_Type T) {
        if (S == T)
            return true;
        S.prepare(); T.prepare();
        return S.isSubtypeOf(T);
    }
    
    /**
     * Uses (old) graph traversal algorithm for type check.
     * 
     * @param S subtype
     * @param T type
     * @return true iff "T = S;" would be legal.
     */
    public static boolean isAssignable_graph(jq_Type S, jq_Type T) {
        if (S == T)
            return true;
        jq_Type s2 = S, t2 = T;
        if (false) {
            if (t2 == Address._class)
                return s2.isAddressType();
            if (t2.isAddressType() || s2.isAddressType())
                return false;
        }
        while (t2.isArrayType()) {
            if (!s2.isArrayType()) {
                return false;
            }
            s2 = ((jq_Array)s2).getElementType(); s2.prepare();
            t2 = ((jq_Array)t2).getElementType(); t2.prepare();
        }
        if (s2.isPrimitiveType() || t2.isPrimitiveType()) {
            return false;
        }
        // t2 is a class
        boolean is_t2_loaded = t2.isLoaded();
        t2.load();
        if (s2.isArrayType()) {
            ((jq_Array)s2).chkState(STATE_PREPARED);
            if (((jq_Class)t2).isInterface()) {
                //s2.prepare();
                return ((jq_Array)s2).implementsInterface((jq_Class)t2);
            }
            return t2 == PrimordialClassLoader.getJavaLangObject();
        }
        // both are classes
        ((jq_Class)s2).chkState(STATE_PREPARED);
        if (((jq_Class)t2).isInterface()) {
            /*
            if (((jq_Class)s2).isInterface()) {
                if (!is_t2_loaded) return false;
                return isSuperclassOf((jq_Class)t2, (jq_Class)s2);
            }
            */
            return ((jq_Class)s2).implementsInterface((jq_Class)t2);
        }
        // t2 is not an interface
        if (!is_t2_loaded) return false;
        return isSuperclassOf((jq_Class)t2, (jq_Class)s2, true) == YES;
    }
    
    /** Returns YES iff t1 is a superclass of t2. */
    public static byte isSuperclassOf(jq_Class t1, jq_Class t2, boolean loadClasses) {
        // doesn't do equality test.
        for (;;) {
            if (!t2.isLoaded() && !loadClasses) return MAYBE;
            t2.load();
            t2 = t2.getSuperclass();
            if (t2 == null) return NO;
            if (t1 == t2) return YES;
        }
    }
    
    /** Returns YES iff "T = S;" would be legal. (T is same or supertype of S) */
    public static byte isAssignable_noload(jq_Type S, jq_Type T) {
        if (S == jq_Reference.jq_NullType.NULL_TYPE) {
            if (T.isReferenceType()) return YES;
            else return NO;
        }
        if (T == jq_Reference.jq_NullType.NULL_TYPE) return NO;
        if (T == S) return YES;
        if (T.isIntLike() && S.isIntLike()) return YES;
        if (T == PrimordialClassLoader.getJavaLangObject() && S.isReferenceType()) return YES;
        if (!T.isPrepared() || !S.isPrepared()) return MAYBE;
        if (T.isArrayType()) {
            jq_Type elemType = ((jq_Array) T).getInnermostElementType();
            if (!elemType.isPrepared()) return MAYBE;
        }
        if (S.isArrayType()) {
            jq_Type elemType = ((jq_Array) S).getInnermostElementType();
            if (!elemType.isPrepared()) return MAYBE;
        }
        if (S.isSubtypeOf(T)) return YES;
        else return NO;
    }
    
    /** Returns YES iff T declares one of the given interfaces. */
    public static byte declaresInterface(jq_Class T, Collection interfaces, boolean loadClasses) {
        if (VerifyAssertions)
            Assert._assert(T.isLoaded());
        jq_Class[] klass_interfaces = T.getDeclaredInterfaces();
        for (int i=0; i<klass_interfaces.length; ++i) {
            if (!loadClasses && !klass_interfaces[i].isLoaded()) return MAYBE;
            if (interfaces.contains(klass_interfaces[i])) return YES;
        }
        return NO;
    }
    
    /** Returns YES iff T implements the given interface. */
    public static byte implementsInterface_noload(jq_Class klass, jq_Class inter) {
        byte res = NO; jq_Class k = klass;
        if (!klass.isLoaded()) return MAYBE;
        for (;;) {
            if (k.getDeclaredInterface(inter.getDesc()) == inter) return YES;
            k = k.getSuperclass();
            if (k == null) break;
            if (!k.isLoaded()) {
                res = MAYBE; break;
            }
        }
        jq_Class[] interfaces = klass.getDeclaredInterfaces();
        for (int i=0; i<interfaces.length; ++i) {
            jq_Class k2 = interfaces[i];
            byte res2 = implementsInterface_noload(k2, inter);
            if (res2 == YES) return YES;
            if (res2 == MAYBE) res = MAYBE;
        }
        return res;
    }
    
    public static jq_Type findCommonSuperclass(jq_Type t1, jq_Type t2, boolean load) {
        if (t1 == t2) return t1;
        if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
            if (t1.isIntLike() && t2.isIntLike()) {
                if (t1 == jq_Primitive.INT || t2 == jq_Primitive.INT) return jq_Primitive.INT;
                if (t1 == jq_Primitive.CHAR) {
                    if (t2 == jq_Primitive.SHORT) return jq_Primitive.INT;
                    return jq_Primitive.CHAR;
                }
                if (t2 == jq_Primitive.CHAR) return jq_Primitive.CHAR;
                if (t1 == jq_Primitive.SHORT) {
                    if (t2 == jq_Primitive.CHAR) return jq_Primitive.INT;
                    return jq_Primitive.SHORT;
                }
                if (t2 == jq_Primitive.SHORT) return jq_Primitive.SHORT;
                if (t1 == jq_Primitive.BYTE || t2 == jq_Primitive.BYTE) return jq_Primitive.BYTE;
                if (t1 == jq_Primitive.BOOLEAN || t2 == jq_Primitive.BOOLEAN) return jq_Primitive.BOOLEAN;
            }
            return null;
        }
        if (!t1.isReferenceType() || !t2.isReferenceType()) return null;
        if (t1 == jq_Reference.jq_NullType.NULL_TYPE) return t2;
        if (t2 == jq_Reference.jq_NullType.NULL_TYPE) return t1;
        int dim = 0;
        while (t1.isArrayType() && t2.isArrayType()) {
            ++dim;
            t1 = ((jq_Array)t1).getElementType();
            t2 = ((jq_Array)t2).getElementType();
        }
        if (t1.isPrimitiveType() || t2.isPrimitiveType()) {
            jq_Reference result = PrimordialClassLoader.getJavaLangObject();
            --dim;
            while (--dim >= 0) result = result.getArrayTypeForElementType();
            return result;
        }
        if (!t1.isClassType() || !t2.isClassType()) {
            jq_Reference result = PrimordialClassLoader.getJavaLangObject();
            while (--dim >= 0) result = result.getArrayTypeForElementType();
            return result;
        }
        jq_Class c1 = (jq_Class)t1;
        jq_Class c2 = (jq_Class)t2;
        Stack s1 = new Stack();
        do {
            if (!c1.isLoaded()) {
                if (load) c1.load();
                else c1 = PrimordialClassLoader.getJavaLangObject();
            }
            s1.push(c1);
            if (c1.isLoaded()) c1 = c1.getSuperclass();
            else break;
        } while (c1 != null);
        Stack s2 = new Stack();
        do {
            if (!c2.isLoaded()) {
                if (load) c2.load();
                else c2 = PrimordialClassLoader.getJavaLangObject();
            }
            s2.push(c2);
            if (c2.isLoaded()) c2 = c2.getSuperclass();
            else break;
        } while (c2 != null);
        jq_Class result = PrimordialClassLoader.getJavaLangObject();
        while (!s1.empty() && !s2.empty()) {
            jq_Class temp = (jq_Class)s1.pop();
            if (temp == s2.pop()) result = temp;
            else break;
        }
        jq_Reference result2 = result;
        while (--dim >= 0) result2 = result2.getArrayTypeForElementType();
        return result2;
    }
    
    public static final jq_StaticMethod _checkcast;
    public static final jq_StaticMethod _instance_of;
    public static final jq_StaticMethod _arrayStoreCheck;
    static {
        jq_Class k = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/TypeCheck;");
        _checkcast = k.getOrCreateStaticMethod("checkcast", "(Ljava/lang/Object;Ljoeq/Class/jq_Type;)Ljava/lang/Object;");
        _instance_of = k.getOrCreateStaticMethod("instance_of", "(Ljava/lang/Object;Ljoeq/Class/jq_Type;)Z");
        _arrayStoreCheck = k.getOrCreateStaticMethod("arrayStoreCheck", "(Ljoeq/Memory/HeapAddress;[Ljava/lang/Object;)V");
    }
}
