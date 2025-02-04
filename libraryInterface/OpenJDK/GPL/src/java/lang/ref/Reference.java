/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.ref;

import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_OBJECT_GETFIELD_BARRIER;
import static org.jikesrvm.mm.mminterface.Barriers.NEEDS_OBJECT_PUTFIELD_BARRIER;

import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.mm.mminterface.Barriers;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.LightMonitor;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.ReferenceFieldsVary;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;

import sun.misc.Cleaner;


/**
 * Abstract base class for reference objects.  This class defines the
 * operations common to all reference objects.  Because reference objects are
 * implemented in close cooperation with the garbage collector, this class may
 * not be subclassed directly.
 *
 * @author   Mark Reinhold
 * @since    1.2
 */
@ReferenceFieldsVary
public abstract class Reference<T> {

    /* A Reference instance is in one of four possible internal states:
     *
     *     Active: Subject to special treatment by the garbage collector.  Some
     *     time after the collector detects that the reachability of the
     *     referent has changed to the appropriate state, it changes the
     *     instance's state to either Pending or Inactive, depending upon
     *     whether or not the instance was registered with a queue when it was
     *     created.  In the former case it also adds the instance to the
     *     pending-Reference list.  Newly-created instances are Active.
     *
     *     Pending: An element of the pending-Reference list, waiting to be
     *     enqueued by the Reference-handler thread.  Unregistered instances
     *     are never in this state.
     *
     *     Enqueued: An element of the queue with which the instance was
     *     registered when it was created.  When an instance is removed from
     *     its ReferenceQueue, it is made Inactive.  Unregistered instances are
     *     never in this state.
     *
     *     Inactive: Nothing more to do.  Once an instance becomes Inactive its
     *     state will never change again.
     *
     * The state is encoded in the queue and next fields as follows:
     *
     *     Active: queue = ReferenceQueue with which instance is registered, or
     *     ReferenceQueue.NULL if it was not registered with a queue; next =
     *     null.
     *
     *     Pending: queue = ReferenceQueue with which instance is registered;
     *     next = Following instance in queue, or this if at end of list.
     *
     *     Enqueued: queue = ReferenceQueue.ENQUEUED; next = Following instance
     *     in queue, or this if at end of list.
     *
     *     Inactive: queue = ReferenceQueue.NULL; next = this.
     *
     * With this scheme the collector need only examine the next field in order
     * to determine whether a Reference instance requires special treatment: If
     * the next field is null then the instance is active; if it is non-null,
     * then the collector should treat the instance normally.
     *
     * To ensure that concurrent collector can discover active Reference
     * objects without interfering with application threads that may apply
     * the enqueue() method to those objects, collectors should link
     * discovered objects through the discovered field.
     */

  /**
   * The underlying object.  This field is a Address so it will not
   * be automatically kept alive by the garbage collector.<p>
   *
   * Set and maintained by the ReferenceProcessor class.
   */
    @Entrypoint
    private Address _referent;

    ReferenceQueue<? super T> queue;

    Reference next;
    transient private Reference<T> discovered;  /* used by VM */


    /* Object used to synchronize with the garbage collector.  The collector
     * must acquire this lock at the beginning of each collection cycle.  It is
     * therefore critical that any code holding this lock complete as quickly
     * as possible, allocate no new objects, and avoid calling user code.
     */
    static private class Lock { };
    private static Lock lock = new Lock();
    // added for Jikes RVM to get away from using synchronized on Reference
    LightMonitor instanceLock = new LightMonitor();


    /* List of References waiting to be enqueued.  The collector adds
     * References to this list, while the Reference-handler thread removes
     * them.  This list is protected by the above lock object.
     */
    private static Reference pending = null;

    /* High-priority thread to enqueue pending References
     */
    private static class ReferenceHandler extends Thread {

        ReferenceHandler(ThreadGroup g, String name) {
            super(g, name);
        }

        @Override
        public void run() {
            for (;;) {

                Reference r;
                synchronized (lock) {
                    if (pending != null) {
                        r = pending;
                        Reference rn = r.next;
                        pending = (rn == r) ? null : rn;
                        r.next = r;
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException x) { }
                        continue;
                    }
                }

                // Fast path for cleaners
                if (r instanceof Cleaner) {
                    ((Cleaner)r).clean();
                    continue;
                }

                ReferenceQueue q = r.queue;
                if (q != ReferenceQueue.NULL) q.enqueue(r);
            }
        }
    }

//    static {
//        ThreadGroup tg = Thread.currentThread().getThreadGroup();
//        for (ThreadGroup tgn = tg;
//             tgn != null;
//             tg = tgn, tgn = tg.getParent());
//        Thread handler = new ReferenceHandler(tg, "Reference Handler");
//        /* If there were a special system-only priority greater than
//         * MAX_PRIORITY, it would be used here
//         */
//        handler.setPriority(Thread.MAX_PRIORITY);
//        handler.setDaemon(true);
//        handler.start();
//    }


    /* -- Referent accessor and setters -- */

    /**
     * Returns this reference object's referent.  If this reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @return   The object to which this reference refers, or
     *           <code>null</code> if this reference object has been cleared
     */
    public T get() {
        // Jikes RVM: changed to use get internal
        return (T) getInternal();
    }

    // new method added for Jikes RVM
    @Uninterruptible
    public Object getInternal() {
      // implementation copied from Jikes RVM implementation for Gnu Classpath
      if (RVMType.JavaLangRefReferenceReferenceField.madeTraced()) {
        if (NEEDS_OBJECT_GETFIELD_BARRIER) {
          return Barriers.objectFieldRead(this, RVMType.JavaLangRefReferenceReferenceField.getOffset(), RVMType.JavaLangRefReferenceReferenceField.getId());
        } else {
          return Magic.getObjectAtOffset(this, RVMType.JavaLangRefReferenceReferenceField.getOffset(), RVMType.JavaLangRefReferenceReferenceField.getId());
        }
      } else {
        Address tmp = _referent;
        if (tmp.isZero()) {
          return null;
        } else {
          Object ref = Magic.addressAsObject(tmp);

          if (Barriers.NEEDS_JAVA_LANG_REFERENCE_READ_BARRIER) {
            ref = Barriers.javaLangReferenceReadBarrier(ref);
          }
          return ref;
        }
      }
    }

    /**
     * Clears this reference object.  Invoking this method will not cause this
     * object to be enqueued.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * clears references it does so directly, without invoking this method.
     */
    public void clear() {
      // implementation copied from Jikes RVM implementation for Gnu Classpath
      if (RVMType.JavaLangRefReferenceReferenceField.madeTraced()) {
        if (NEEDS_OBJECT_PUTFIELD_BARRIER) {
          Barriers.objectFieldWrite(this, null, RVMType.JavaLangRefReferenceReferenceField.getOffset(), RVMType.JavaLangRefReferenceReferenceField.getId());
        } else {
          Magic.setObjectAtOffset(this, RVMType.JavaLangRefReferenceReferenceField.getOffset(), null, RVMType.JavaLangRefReferenceReferenceField.getId());
        }
      }
    }


    /* -- Queue operations -- */

    /**
     * Tells whether or not this reference object has been enqueued, either by
     * the program or by the garbage collector.  If this reference object was
     * not registered with a queue when it was created, then this method will
     * always return <code>false</code>.
     *
     * @return   <code>true</code> if and only if this reference object has
     *           been enqueued
     */
    public boolean isEnqueued() {
        /* In terms of the internal states, this predicate actually tests
           whether the instance is either Pending or Enqueued */
        synchronized (this) {
            return (this.queue != ReferenceQueue.NULL) && (this.next != null);
        }
    }

    /**
     * Adds this reference object to the queue with which it is registered,
     * if any.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * enqueues references it does so directly, without invoking this method.
     *
     * @return   <code>true</code> if this reference object was successfully
     *           enqueued; <code>false</code> if it was already enqueued or if
     *           it was not registered with a queue when it was created
     */
    public boolean enqueue() {
        return this.queue.enqueue(this);
    }

    // new method added for Jikes RVM
    @Uninterruptible
    public boolean enqueueInternal() {
      return this.queue.enqueueInternal(this);
  }

    /* -- Constructors -- */

    Reference(T referent) {
        this(referent, null);
    }

    Reference(T referent, ReferenceQueue<? super T> queue) {
        // Jikes RVM: don't save referent here
        this.queue = (queue == null) ? ReferenceQueue.NULL : queue;
    }

}
