/*
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;
import java.lang.reflect.*;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMField;

import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import java.lang.JikesRVMSupport;

/**
 * A reflection-based utility that enables atomic updates to
 * designated {@code volatile} reference fields of designated
 * classes.  This class is designed for use in atomic data structures
 * in which several reference fields of the same node are
 * independently subject to atomic updates. For example, a tree node
 * might be declared as
 *
 *  <pre> {@code
 * class Node {
 *   private volatile Node left, right;
 *
 *   private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater =
 *     AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
 *   private static AtomicReferenceFieldUpdater<Node, Node> rightUpdater =
 *     AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
 *
 *   Node getLeft() { return left;  }
 *   boolean compareAndSetLeft(Node expect, Node update) {
 *     return leftUpdater.compareAndSet(this, expect, update);
 *   }
 *   // ... and so on
 * }}</pre>
 *
 * <p>Note that the guarantees of the {@code compareAndSet}
 * method in this class are weaker than in other atomic classes.
 * Because this class cannot ensure that all uses of the field
 * are appropriate for purposes of atomic access, it can
 * guarantee atomicity only with respect to other invocations of
 * {@code compareAndSet} and {@code set} on the same updater.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <T> The type of the object holding the updatable field
 * @param <V> The type of the field
 */
public abstract class AtomicReferenceFieldUpdater<T, V> {

    /**
     * Creates and returns an updater for objects with the given field.
     * The Class arguments are needed to check that reflective types and
     * generic types match.
     *
     * @param tclass the class of the objects holding the field.
     * @param vclass the class of the field
     * @param fieldName the name of the field to be updated.
     * @return the updater
     * @throws IllegalArgumentException if the field is not a volatile reference type.
     * @throws RuntimeException with a nested reflection-based
     * exception if the class does not hold field or is the wrong type.
     */
    @CallerSensitive
    public static <U, W> AtomicReferenceFieldUpdater<U,W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName) {
        return new AtomicReferenceFieldUpdaterImpl<U,W>(tclass,
                                                        vclass,
                                                        fieldName,
                                                        Reflection.getCallerClass());
    }

    /**
     * Protected do-nothing constructor for use by subclasses.
     */
    protected AtomicReferenceFieldUpdater() {
    }

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given updated value if the current value {@code ==} the
     * expected value. This method is guaranteed to be atomic with respect to
     * other calls to {@code compareAndSet} and {@code set}, but not
     * necessarily with respect to other changes in the field.
     *
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public abstract boolean compareAndSet(T obj, V expect, V update);

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given updated value if the current value {@code ==} the
     * expected value. This method is guaranteed to be atomic with respect to
     * other calls to {@code compareAndSet} and {@code set}, but not
     * necessarily with respect to other changes in the field.
     *
     * <p>May <a href="package-summary.html#Spurious">fail spuriously</a>
     * and does not provide ordering guarantees, so is only rarely an
     * appropriate alternative to {@code compareAndSet}.
     *
     * @param obj An object whose field to conditionally set
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public abstract boolean weakCompareAndSet(T obj, V expect, V update);

    /**
     * Sets the field of the given object managed by this updater to the
     * given updated value. This operation is guaranteed to act as a volatile
     * store with respect to subsequent invocations of {@code compareAndSet}.
     *
     * @param obj An object whose field to set
     * @param newValue the new value
     */
    public abstract void set(T obj, V newValue);

    /**
     * Eventually sets the field of the given object managed by this
     * updater to the given updated value.
     *
     * @param obj An object whose field to set
     * @param newValue the new value
     * @since 1.6
     */
    public abstract void lazySet(T obj, V newValue);

    /**
     * Gets the current value held in the field of the given object managed
     * by this updater.
     *
     * @param obj An object whose field to get
     * @return the current value
     */
    public abstract V get(T obj);

    /**
     * Atomically sets the field of the given object managed by this updater
     * to the given value and returns the old value.
     *
     * @param obj An object whose field to get and set
     * @param newValue the new value
     * @return the previous value
     */
    public V getAndSet(T obj, V newValue) {
        for (;;) {
            V current = get(obj);
            if (compareAndSet(obj, current, newValue))
                return current;
        }
    }

    private static final class AtomicReferenceFieldUpdaterImpl<T,V>
        extends AtomicReferenceFieldUpdater<T,V> {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final long offset;
        private final Class<T> tclass;
        private final Class<V> vclass;
        private final Class cclass;

        /*
         * Internal type checks within all update methods contain
         * internal inlined optimizations checking for the common
         * cases where the class is final (in which case a simple
         * getClass comparison suffices) or is of type Object (in
         * which case no check is needed because all objects are
         * instances of Object). The Object case is handled simply by
         * setting vclass to null in constructor.  The targetCheck and
         * updateCheck methods are invoked when these faster
         * screenings fail.
         */

        AtomicReferenceFieldUpdaterImpl(Class<T> tclass,
                                        Class<V> vclass,
                                        String fieldName,
                                        Class<?> caller) {
            RVMField field = null;
            Class fieldClass = null;
            int modifiers = 0;
            try {
                RVMClass rvmTclass = (RVMClass)JikesRVMSupport.getTypeForClass(tclass);
                field = rvmTclass.findDeclaredField(Atom.findAsciiAtom(fieldName));
                modifiers = field.getModifiers();
                sun.reflect.misc.ReflectUtil.ensureMemberAccess(
                    caller, tclass, null, modifiers);
                sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
                fieldClass = field.getType().peekType().getClassForType();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if (vclass != fieldClass)
                throw new ClassCastException();
            if (vclass.isPrimitive())
                throw new IllegalArgumentException("Must be reference type");

            if (!Modifier.isVolatile(modifiers))
                throw new IllegalArgumentException("Must be volatile type");

            this.cclass = (Modifier.isProtected(modifiers) &&
                           caller != tclass) ? caller : null;
            this.tclass = tclass;
            if (vclass == Object.class)
                this.vclass = null;
            else
                this.vclass = vclass;
            offset = field.getOffset().toLong();
        }

        void targetCheck(T obj) {
            if (!tclass.isInstance(obj))
                throw new ClassCastException();
            if (cclass != null)
                ensureProtectedAccess(obj);
        }

        void updateCheck(T obj, V update) {
            if (!tclass.isInstance(obj) ||
                (update != null && vclass != null && !vclass.isInstance(update)))
                throw new ClassCastException();
            if (cclass != null)
                ensureProtectedAccess(obj);
        }

        public boolean compareAndSet(T obj, V expect, V update) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (update != null && vclass != null &&
                 vclass != update.getClass()))
                updateCheck(obj, update);
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }

        public boolean weakCompareAndSet(T obj, V expect, V update) {
            // same implementation as strong form for now
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (update != null && vclass != null &&
                 vclass != update.getClass()))
                updateCheck(obj, update);
            return unsafe.compareAndSwapObject(obj, offset, expect, update);
        }

        public void set(T obj, V newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (newValue != null && vclass != null &&
                 vclass != newValue.getClass()))
                updateCheck(obj, newValue);
            unsafe.putObjectVolatile(obj, offset, newValue);
        }

        public void lazySet(T obj, V newValue) {
            if (obj == null || obj.getClass() != tclass || cclass != null ||
                (newValue != null && vclass != null &&
                 vclass != newValue.getClass()))
                updateCheck(obj, newValue);
            unsafe.putOrderedObject(obj, offset, newValue);
        }

        public V get(T obj) {
            if (obj == null || obj.getClass() != tclass || cclass != null)
                targetCheck(obj);
            return (V)unsafe.getObjectVolatile(obj, offset);
        }

        private void ensureProtectedAccess(T obj) {
            if (cclass.isInstance(obj)) {
                return;
            }
            throw new RuntimeException(
                new IllegalAccessException("Class " +
                    cclass.getName() +
                    " can not access a protected member of class " +
                    tclass.getName() +
                    " using an instance of " +
                    obj.getClass().getName()
                )
            );
        }
    }
}
