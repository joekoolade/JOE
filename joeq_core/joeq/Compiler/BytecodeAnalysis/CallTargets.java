// CallTargets.java, created Fri Jan 11 16:49:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticMethod;
import joeq.Compiler.CompilationConstants;
import joeq.Compiler.CompilationState;
import joeq.Runtime.TypeCheck;
import jwutil.collections.HashCodeComparator;
import jwutil.collections.SortedArraySet;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CallTargets.java,v 1.26 2004/09/22 22:17:41 joewhaley Exp $
 */
public abstract class CallTargets extends AbstractSet implements CompilationConstants {

    public static final boolean TRACE = false;
    public static final boolean VerifyAssertions = false;
    
    public static CallTargets getTargets(jq_Class callingClass,
                                         jq_Method method,
                                         byte type,
                                         Set possibleReceiverTypes,
                                         boolean exact,
                                         boolean loadClasses)
    {
        if (type == BytecodeVisitor.INVOKE_STATIC) return getStaticTargets((jq_StaticMethod)method);
        jq_InstanceMethod imethod = (jq_InstanceMethod)method;
        if (type == BytecodeVisitor.INVOKE_SPECIAL) return getSpecialTargets(callingClass, imethod, loadClasses);
        if (VerifyAssertions)
            Assert._assert(type == BytecodeVisitor.INVOKE_VIRTUAL || type == BytecodeVisitor.INVOKE_INTERFACE, ""+type);

        if (TRACE) System.out.println("Getting call targets of "+method+" type "+type+" receiver types "+possibleReceiverTypes+" exact="+exact);
        if (!exact) {
            // temporary hack, until we rewrite the code below to take into account
            // non-exact sets.
            Set c = new LinkedHashSet();
            Iterator i = possibleReceiverTypes.iterator();
            boolean complete = true;
            while (i.hasNext()) {
                CallTargets ct = getTargets(callingClass, method, type, (jq_Reference)i.next(), false, loadClasses);
                c.addAll(ct);
                if (!ct.isComplete()) complete = false;
            }
            return new MultipleCallTargets(c, complete);
        }
        
        Set c = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
        boolean complete = true;
        // HACK: some broken class files have invokevirtual on interface methods.
        if (type == BytecodeVisitor.INVOKE_VIRTUAL &&
            imethod.getDeclaringClass().isLoaded() &&
            imethod.getDeclaringClass().isInterface()) {
            type = BytecodeVisitor.INVOKE_INTERFACE;
        }

        if ((type == BytecodeVisitor.INVOKE_VIRTUAL) && imethod.getDeclaringClass().isPrepared()) {
            // fast search using vtable
            if (VerifyAssertions)
                Assert._assert(!imethod.getDeclaringClass().isInterface(), imethod.toString());
            int offset = imethod.getOffset() >>> 2;
            if (TRACE) System.out.println("Fast search using vtable offset "+offset+" for method "+imethod);
            if (VerifyAssertions)
                Assert._assert(offset >= 1);
            Iterator i = possibleReceiverTypes.iterator();
            while (i.hasNext()) {
                jq_Reference rtype = (jq_Reference)i.next();
                if (rtype.isClassType()) {
                    jq_Class rclass = (jq_Class)rtype;
                    //jq.Assert(!rclass.isLoaded() || !rclass.isAbstract());
                    if (!rclass.isPrepared()) {
                        jq_InstanceMethod target;
                        for (;;) {
                            if (TRACE) System.out.println("visiting "+rclass);
                            if (loadClasses) rclass.load();
                            if (!rclass.isLoaded()) {
                                if (TRACE) System.out.println(rclass+" isn't loaded: conservative.");
                                complete = false; // conservative.
                                break;
                            }
                            jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
                            if (mtarget instanceof jq_StaticMethod) break;
                            target = (jq_InstanceMethod)mtarget;
                            if (target != null) {
                                if (VerifyAssertions)
                                    Assert._assert(imethod.getNameAndDesc().equals(target.getNameAndDesc()), imethod+" != "+target);
                                if (!target.isAbstract())
                                    c.add(target);
                                break;
                            }
                            rclass = rclass.getSuperclass();
                            if (rclass == null) {
                                // method doesn't exist in this class or any of its superclasses.
                                break;
                            }
                        }
                        continue;
                    }
                    if (offset > rclass.getVirtualMethods().length)
                        continue;
                    jq_InstanceMethod target = rclass.getVirtualMethods()[offset-1];
                    if (!imethod.getNameAndDesc().equals(target.getNameAndDesc()))
                        continue;
                    if (VerifyAssertions)
                        Assert._assert(!target.isAbstract());
                    c.add(target);
                } else {
                    if (VerifyAssertions)
                        Assert._assert(rtype.isArrayType(), rtype.toString());
                    if (imethod.getDeclaringClass() != PrimordialClassLoader.getJavaLangObject())
                        continue;
                    if (VerifyAssertions)
                        Assert._assert(!imethod.isAbstract(), imethod.toString());
                    c.add(imethod);
                }
            }
        } else {
            // slow search.
            Iterator i = possibleReceiverTypes.iterator();
            while (i.hasNext()) {
                jq_Reference rtype = (jq_Reference)i.next();
                if (rtype.isClassType()) {
                    jq_Class rclass = (jq_Class)rtype;
                    jq_InstanceMethod target;
                    for (;;) {
                        if (TRACE) System.out.println("visiting "+rclass);
                        if (loadClasses) rclass.load();
                        if (!rclass.isLoaded()) {
                            complete = false; // conservative.
                            break;
                        }
                        jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
                        if (mtarget instanceof jq_StaticMethod) break;
                        target = (jq_InstanceMethod)mtarget;
                        if (target != null) {
                            if (VerifyAssertions)
                                Assert._assert(imethod.getNameAndDesc().equals(target.getNameAndDesc()), imethod+" != "+target);
                            if (!target.isAbstract())
                                c.add(target);
                            break;
                        }
                        rclass = rclass.getSuperclass();
                        if (rclass == null) {
                            // method doesn't exist in this class or any of its superclasses.
                            break;
                        }
                    }
                    continue;
                } else {
                    if (VerifyAssertions)
                        Assert._assert(rtype.isArrayType(), rtype.toString());
                    if (imethod.getDeclaringClass() != PrimordialClassLoader.getJavaLangObject())
                        continue;
                    if (VerifyAssertions)
                        Assert._assert(!imethod.isAbstract());
                    c.add(imethod);
                }
            }
        }
        if (TRACE) System.out.println("final result: "+c+" complete: "+complete);
        return new MultipleCallTargets(c, complete);
    }

    public static CallTargets getTargets(jq_Class callingClass,
                                         jq_Method method,
                                         byte type,
                                         jq_Reference receiverType,
                                         boolean exact,
                                         boolean loadClasses)
    {
        if (type == BytecodeVisitor.INVOKE_STATIC) return getStaticTargets((jq_StaticMethod)method);
        jq_InstanceMethod imethod = (jq_InstanceMethod)method;
        if (type == BytecodeVisitor.INVOKE_SPECIAL) return getSpecialTargets(callingClass, imethod, loadClasses);
        if (VerifyAssertions)
            Assert._assert(type == BytecodeVisitor.INVOKE_VIRTUAL || type == BytecodeVisitor.INVOKE_INTERFACE, ""+type);
            
        if (receiverType.isArrayType()) {
            if (imethod.getDeclaringClass() != PrimordialClassLoader.getJavaLangObject())
                return NoCallTarget.INSTANCE;
            return new SingleCallTarget(imethod, true);
        }
        
        jq_Class rclass = (jq_Class)receiverType;
        
        if (TRACE) System.out.println("Class "+rclass+" has "+rclass.getSubClasses().length+" subclasses");
        
        if (TypeCheck.isSuperclassOf(rclass, imethod.getDeclaringClass(), loadClasses) == YES) {
            // errr... rclass is a supertype of the method's class!
            receiverType = rclass = imethod.getDeclaringClass();
        }
        
        if (exact) {
            if (loadClasses) rclass.load();
            if (!rclass.isLoaded()) return NoCallTarget.INSTANCE;
            for (;;) {
                jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
                if (mtarget instanceof jq_StaticMethod) return NoCallTarget.INSTANCE;
                jq_InstanceMethod target = (jq_InstanceMethod)mtarget;
                if (target != null) return new SingleCallTarget(target, true);
                if (VerifyAssertions)
                    Assert._assert(rclass != imethod.getDeclaringClass(), rclass+" != "+imethod.getDeclaringClass());
                if (loadClasses) rclass.load();
                if (!rclass.isLoaded()) return NoCallTarget.INSTANCE;
                rclass = rclass.getSuperclass();
                if (rclass == null) {
                    // method doesn't exist in this class or any of its superclasses.
                    return NoCallTarget.INSTANCE;
                }
            }
        }

        // TEMPORARY HACK: sometimes casts from interface types are
        // lost in the type analysis, leading to virtual calls on interfaces.
        if (loadClasses) {
            rclass.load();
            if (rclass.isInterface() && type == BytecodeVisitor.INVOKE_VIRTUAL) {
                if (!imethod.getDeclaringClass().isInterface())
                    receiverType = rclass = imethod.getDeclaringClass();
            }
        }

        // TEMPORARY HACK: some class files are messed up and use
        // invokevirtual instead of invokeinterface.
        if (type == BytecodeVisitor.INVOKE_VIRTUAL &&
            imethod.getDeclaringClass().isLoaded() &&
            imethod.getDeclaringClass().isInterface()) {
            type = BytecodeVisitor.INVOKE_INTERFACE;
        }

        Set c = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
        boolean complete = true;
        if (type == BytecodeVisitor.INVOKE_VIRTUAL) {
            if (imethod.getDeclaringClass().isPrepared()) {
                // fast search.
                if (VerifyAssertions)
                    Assert._assert(!imethod.getDeclaringClass().isInterface());
                int offset = imethod.getOffset() >>> 2;
                if (VerifyAssertions)
                    Assert._assert(offset >= 1);
                if (!rclass.isPrepared()) {
                    for (;;) {
                        if (loadClasses) rclass.load();
                        if (!rclass.isLoaded()) {
                            complete = false; // conservative.
                            break;
                        }
                        jq_Method target = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
                        if (target != null) {
                            if (!target.isStatic() && !target.isAbstract())
                                c.add(target);
                            break;
                        }
                        if (VerifyAssertions)
                            Assert._assert(rclass != imethod.getDeclaringClass());
                        rclass = rclass.getSuperclass();
                        if (rclass == null) {
                            // method doesn't exist in this class or any of its superclasses.
                            break;
                        }
                    }
                }
                jq_InstanceMethod target;
                Stack subclass = new Stack();
                subclass.push(receiverType);
                while (!subclass.empty()) {
                    rclass = (jq_Class)subclass.pop();
                    if (loadClasses) rclass.load();
                    if (!rclass.isLoaded()) {
                        complete = false; // conservative.
                        continue;
                    }
                    if (TRACE) System.out.println("Class "+rclass+" has "+rclass.getSubClasses().length+" subclasses");
                    if (!rclass.isPrepared()) {
                        jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
                        if (mtarget instanceof jq_StaticMethod) continue;
                        target = (jq_InstanceMethod)mtarget;
                        if (TRACE) System.out.println("Class "+rclass+" target: "+target);
                        if ((target != null) && !target.isAbstract()) c.add(target);
                    } else {
                        if (offset > rclass.getVirtualMethods().length)
                            continue;
                        target = rclass.getVirtualMethods()[offset-1];
                        if (TRACE) System.out.println("Class "+rclass+" target: "+target);
                        if (!imethod.getNameAndDesc().equals(target.getNameAndDesc()))
                            continue;
                        if (!target.isAbstract()) {
                            if (TRACE) System.out.println("Target added to result: "+target);
                            c.add(target);
                        }
                    }
                    if (target != null) {
                        if (!target.isFinal() && !target.isPrivate()) {
                            if (!rclass.isFinal()) {
                                complete = false; // conservative.
                            }
                            jq_Class[] subclasses = rclass.getSubClasses();
                            for (int i=0; i<subclasses.length; ++i) {
                                subclass.push(subclasses[i]);
                            }
                        }
                    } else {
                        if (!rclass.isFinal()) {
                            complete = false; // conservative.
                        }
                        jq_Class[] subclasses = rclass.getSubClasses();
                        for (int i=0; i<subclasses.length; ++i) subclass.push(subclasses[i]);
                    }
                }
                if (TRACE) System.out.println("final result: "+c+" complete: "+complete);
                return new MultipleCallTargets(c, complete);
            }
        }
        
        if (type == BytecodeVisitor.INVOKE_INTERFACE) {
            if (loadClasses) rclass.load();
            if (!rclass.isLoaded() || ((jq_Class)rclass).isInterface()) {
                // not the true receiver type, or we don't know anything about the receiver type because
                // it isn't loaded, so we fall back to the case where we don't consider the receiver
                // (calls to java.lang.Object methods on interfaces should have been caught above
                //  because they are virtual calls, and java.lang.Object is always prepared)
                return getTargets(callingClass, method, type, loadClasses);
            }
        }
        
        // slow search, interface or virtual call.  instance method is not prepared.
        for (;;) {
            if (loadClasses) rclass.load();
            if (!rclass.isLoaded()) {
                complete = false; // conservative.
                break;
            }
            jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
            if (mtarget instanceof jq_StaticMethod) break;
            jq_InstanceMethod target = (jq_InstanceMethod)mtarget;
            if (target != null) {
                if (!target.isAbstract()) c.add(target);
                break;
            }
            // assert commented out- imethod might have the wrong 
            //jq.Assert(rclass != imethod.getDeclaringClass());
            rclass = rclass.getSuperclass();
            if (rclass == null) {
                // method doesn't exist in this class or any of its superclasses.
                break;
            }
        }
        Stack subclass = new Stack();
        subclass.push(receiverType);
        while (!subclass.empty()) {
            rclass = (jq_Class)subclass.pop();
            if (loadClasses) rclass.load();
            if (TRACE) System.out.println("Class "+rclass+" has "+rclass.getSubClasses().length+" subclasses");
            if (!rclass.isLoaded()) {
                complete = false; // conservative.
                continue;
            }
            jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
            if (mtarget instanceof jq_StaticMethod) continue;
            jq_InstanceMethod target = (jq_InstanceMethod)mtarget;
            if (target != null) {
                if (TRACE) System.out.println("Class "+rclass+" target: "+target);
                if (!target.isAbstract()) {
                    c.add(target);
                    if (TRACE) System.out.println("target added to result: "+target);
                }
                if (!target.isFinal() && !target.isPrivate()) {
                    if (!rclass.isFinal()) {
                        complete = false; // conservative.
                    }
                    jq_Class[] subclasses = rclass.getSubClasses();
                    for (int i=0; i<subclasses.length; ++i) subclass.push(subclasses[i]);
                }
            } else {
                if (!rclass.isFinal()) {
                    complete = false; // conservative.
                }
                jq_Class[] subclasses = rclass.getSubClasses();
                for (int i=0; i<subclasses.length; ++i) subclass.push(subclasses[i]);
            }
        }
        if (TRACE) System.out.println("final result: "+c+" complete: "+complete);
        return new MultipleCallTargets(c, complete);
    }

    public static void addAllSubclasses(jq_Class cl, Set s, boolean loadClasses) {
        Stack worklist = new Stack();
        for (;;) {
            s.add(cl);
            if (loadClasses) cl.load();
            if (cl.isLoaded()) {
                jq_Class[] subclasses = cl.getSubClasses();
                for (int i=0; i<subclasses.length; ++i) worklist.push(subclasses[i]);
            }
            if (worklist.empty()) break;
            cl = (jq_Class)worklist.pop();
        }
    }
    
    public static CallTargets getTargets(jq_Class callingClass,
                                         jq_Method method,
                                         byte type,
                                         boolean loadClasses)
    {
        if (type == BytecodeVisitor.INVOKE_STATIC) return getStaticTargets((jq_StaticMethod)method);
        jq_InstanceMethod imethod = (jq_InstanceMethod)method;
        if (type == BytecodeVisitor.INVOKE_SPECIAL) return getSpecialTargets(callingClass, imethod, loadClasses);
        if (type == BytecodeVisitor.INVOKE_VIRTUAL)
            return getTargets(callingClass, imethod, type, imethod.getDeclaringClass(), false, loadClasses);
        if (VerifyAssertions)
            Assert._assert(type == BytecodeVisitor.INVOKE_INTERFACE);
        if (VerifyAssertions)
            Assert._assert(!imethod.getDeclaringClass().isLoaded() ||
                      imethod.getDeclaringClass().isInterface());

        // find the set of equivalent interfaces
        jq_Class interf = method.getDeclaringClass();
        Set interfaces = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
        interfaces.add(interf);
        addAllSubclasses(interf, interfaces, loadClasses);
        boolean again;
        do {
            again = false;
            jq_Class rclass = PrimordialClassLoader.getJavaLangObject();
            Stack worklist = new Stack();
            worklist.push(rclass);
            while (!worklist.empty()) {
                rclass = (jq_Class)worklist.pop();
                if (loadClasses) rclass.load();
                if (!rclass.isLoaded()) continue;
                if (!rclass.isInterface()) continue;
                if (CompilationState.DEFAULT.declaresInterface(rclass, interfaces) != NO) {
                    again = true; // must repeat to catch any interfaces that implement this one
                    // add subtree from here.
                    addAllSubclasses(rclass, interfaces, loadClasses);
                } else {
                    jq_Class[] subclasses = rclass.getSubClasses();
                    for (int i=0; i<subclasses.length; ++i) worklist.push(subclasses[i]);
                }
            }
        } while (again);
        
        // find the set of classes that implement these interfaces.
        Stack worklist = new Stack();     // unchecked classes
        Stack implementers = new Stack(); // do/may implement
        jq_Class rclass = PrimordialClassLoader.getJavaLangObject();
        if (VerifyAssertions)
            Assert._assert(rclass.isLoaded()); // java.lang.Object had better be loaded!
        if (rclass.implementsInterface(interf)) implementers.push(rclass);
        else {
            worklist.push(rclass);
            while (!worklist.empty()) {
                rclass = (jq_Class)worklist.pop();
                if (loadClasses) rclass.load();
                if (!rclass.isLoaded()) continue;
                if (rclass.isInterface()) continue;
                if (CompilationState.DEFAULT.declaresInterface(rclass, interfaces) != NO) {
                    implementers.push(rclass);
                } else {
                    jq_Class[] subclasses = rclass.getSubClasses();
                    for (int i=0; i<subclasses.length; ++i) worklist.push(subclasses[i]);
                }
            }
        }
        
        LinkedHashSet c = new LinkedHashSet(); // use a HashSet because it is going to be large
        while (!implementers.empty()) {
            rclass = (jq_Class)implementers.pop();
            if (loadClasses) rclass.load();
            if (!rclass.isLoaded()) continue;
            if (VerifyAssertions)
                Assert._assert(!rclass.isInterface());
            jq_Method mtarget = (jq_Method)rclass.getDeclaredMember(imethod.getNameAndDesc());
            if (mtarget instanceof jq_StaticMethod) continue;
            jq_InstanceMethod target = (jq_InstanceMethod)mtarget;
            if (target != null) {
                if (!target.isAbstract()) c.add(target);
                if (target.isFinal() || target.isPrivate()) continue;
            }
            jq_Class[] subclasses = rclass.getSubClasses();
            for (int i=0; i<subclasses.length; ++i) implementers.push(subclasses[i]);
        }
        return new MultipleCallTargets(c, false);
    }
    
    public static SingleCallTarget getStaticTargets(jq_StaticMethod method)
    {
        // static method. the declaring class might not have been loaded.
        return new SingleCallTarget(method, true);
    }
    
    public static CallTargets getSpecialTargets(jq_Class callingClass,
                                                jq_InstanceMethod method,
                                                boolean loadClasses)
    {
        // special, non-virtual invocation.
        if (!method.getDeclaringClass().isLoaded()) {
            if (method.isInitializer()) return new SingleCallTarget(method, true);
            if (callingClass.isLoaded() && !callingClass.isSpecial())
                return new SingleCallTarget(method, true);
            if (!loadClasses) return NoCallTarget.INSTANCE;  // no idea!
            method.getDeclaringClass().load();
        }
        jq_InstanceMethod target = jq_Class.getInvokespecialTarget(callingClass, method);
        return new SingleCallTarget(target, true);
    }
    
    public abstract Iterator iterator();
    public abstract boolean isComplete();
    public abstract CallTargets union(CallTargets s);
    public abstract int size();
    
    public static class NoCallTarget extends CallTargets
    {
        public Iterator iterator() { return Collections.EMPTY_SET.iterator(); }
        public boolean isComplete() { return false; }
        public CallTargets union(CallTargets s) { return s; }
        public int size() { return 0; }
        public static final NoCallTarget INSTANCE = new NoCallTarget();
        private NoCallTarget() {}
        public String toString() { return "{}"; }
    }
    
    public static class SingleCallTarget extends CallTargets
    {
        final jq_Method method; final boolean complete;
        public SingleCallTarget(jq_Method m, boolean c) { method = m; complete = c; }
        public Iterator iterator() { return Collections.singleton(method).iterator(); }
        public boolean isComplete() { return complete; }
        public CallTargets union(CallTargets s) {
            if (s == NoCallTarget.INSTANCE) return this;
            Set result = SortedArraySet.FACTORY.makeSet(HashCodeComparator.INSTANCE);
            boolean is_complete = this.complete;
            result.add(this.method);
            if (!s.isComplete()) is_complete = false;
            for (Iterator i = s.iterator(); i.hasNext(); ) {
                result.add(i.next());
            }
            return new MultipleCallTargets(result, is_complete);
        }
        public int size() { return 1; }
        public String toString() {
            if (complete) return "{ "+method.toString()+" } (complete)";
            else return "{ "+method.toString()+" }";
        }
    }
    
    static class MultipleCallTargets extends CallTargets
    {
        final Set set; boolean complete;
        MultipleCallTargets(Set s, boolean c) { set = s; complete = c; }
        public Iterator iterator() { return set.iterator(); }
        public boolean isComplete() { return complete; }
        public CallTargets union(CallTargets s) {
            if (s == NoCallTarget.INSTANCE) return this;
            if (s instanceof SingleCallTarget) {
                SingleCallTarget sct = (SingleCallTarget)s;
                this.set.add(sct.method);
                if (!sct.isComplete()) this.complete = false;
            } else {
                if (VerifyAssertions)
                    Assert._assert(s instanceof MultipleCallTargets);
                this.set.addAll(((MultipleCallTargets)s).set);
            }
            return this;
        }
        public int size() { return set.size(); }
        public String toString() {
            if (complete) return set.toString()+" (complete)";
            else return set.toString();
        }
    }
    
}
