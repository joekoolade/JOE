// Delegates.java, created Wed Dec 11 12:02:02 2002 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import joeq.Allocator.CodeAllocator;
import joeq.Allocator.DefaultHeapAllocator;
import joeq.Assembler.DirectBindCall;
import joeq.Bootstrap.BootstrapCodeAddress;
import joeq.Bootstrap.BootstrapHeapAddress;
import joeq.Compiler.CompilerInterface;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.ExceptionDeliverer;
import jwutil.collections.Filter;
import jwutil.collections.Pair;

/**
 * Delegates
 *
 * @author Michael Martin <mcmartin@stanford.edu>
 * @version $Id: Delegates.java,v 1.20 2004/09/30 03:35:38 joewhaley Exp $
 */
public class Delegates implements jq_ClassFileConstants {
    static class Field implements jq_Field.Delegate {
        public final boolean isCodeAddressType(jq_Field f) {
            return f.getType() == CodeAddress._class ||
                f.getType() == BootstrapCodeAddress._class;
        }
        public final boolean isHeapAddressType(jq_Field f) {
            return f.getType() == HeapAddress._class ||
                f.getType() == BootstrapHeapAddress._class;
        }
        public final boolean isStackAddressType(jq_Field f) {
            return f.getType() == StackAddress._class;
        }
    }
    static class Method implements jq_Method.Delegate {
        public final jq_CompiledCode compile_stub (jq_Method m) {
            return default_compiler.generate_compile_stub(m);
        }
        public final jq_CompiledCode compile (jq_Method m) {
            jq_CompiledCode default_compiled_version;
            //System.out.println("Compiling: "+m);
            if (m.isNative() && m.getBytecode() == null) {
                System.out.println("Unimplemented native method! "+m);
                if (ExceptionDeliverer._nativeMethodError.getState() < STATE_CLSINITIALIZED) {
                    jq_Class k = ExceptionDeliverer._class;
                    k.verify(); //k.prepare();
                    if (ExceptionDeliverer._nativeMethodError.getState() != STATE_PREPARED)
                        ExceptionDeliverer._nativeMethodError.prepare();
                    default_compiled_version = ExceptionDeliverer._nativeMethodError.compile();
                    //if (k != getDeclaringClass() && getDeclaringClass().getSuperclass() != null) { k.cls_initialize(); }
                } else {
                    default_compiled_version = ExceptionDeliverer._nativeMethodError.getDefaultCompiledVersion();
                }
            } else if (m.isAbstract()) {
                if (ExceptionDeliverer._abstractMethodError.getState() < STATE_CLSINITIALIZED) {
                    jq_Class k = ExceptionDeliverer._class;
                    k.verify(); //k.prepare();
                    //default_compiled_version = x86ReferenceLinker._abstractMethodError.getDefaultCompiledVersion();
                    if (ExceptionDeliverer._abstractMethodError.getState() != STATE_PREPARED)
                        ExceptionDeliverer._abstractMethodError.prepare();
                    default_compiled_version = ExceptionDeliverer._abstractMethodError.compile();
                    //if (k != getDeclaringClass() && getDeclaringClass().getSuperclass() != null) { k.cls_initialize(); }
                } else {
                    default_compiled_version = ExceptionDeliverer._abstractMethodError.getDefaultCompiledVersion();
                }
            } else {
                CompilerInterface compiler = getCompiler(m);
                default_compiled_version = compiler.compile(m);
                if (jq.RunningNative)
                    default_compiled_version.patchDirectBindCalls();
            }
            return default_compiled_version;
        }
    }
    public static CompilerInterface default_compiler;
    public static List compilers = new LinkedList();
    public static CompilerInterface getCompiler(String name) {
        try {
            Class c = Class.forName(name);
            return (CompilerInterface) c.newInstance();
        } catch (Exception x) {
            System.err.println("Error occurred while instantiating compiler "+name);
            x.printStackTrace();
            return null;
        }
    }
    public static void setDefaultCompiler(String name) {
        CompilerInterface c = getCompiler(name);
        if (c == null) c = getCompiler("joeq.Compiler.Reference.x86.x86ReferenceCompiler.Factory");
        default_compiler = c;
    }
    public static void registerCompiler(Filter f, CompilerInterface c) {
        compilers.add(0, new Pair(f, c));
    }
    public static CompilerInterface getCompiler(jq_Method m) {
        for (Iterator i=compilers.iterator(); i.hasNext(); ) {
            Pair p = (Pair) i.next();
            Filter f = (Filter) p.get(0);
            if (f.isElement(m))
                return (CompilerInterface) p.get(1);
        }
        return default_compiler;
    }
    
    static class CompiledCode implements jq_CompiledCode.Delegate {
        public void patchDirectBindCalls (Iterator i) {
            while (i.hasNext()) {
                DirectBindCall r = (DirectBindCall) i.next();
                r.patch();
            }
        }
        public void patchDirectBindCalls (Iterator i, jq_Method method, jq_CompiledCode cc) {
            while (i.hasNext()) {
                DirectBindCall r = (DirectBindCall) i.next();
                if (r.getTarget() == method) {
                    if (jq_CompiledCode.TRACE_REDIRECT) Debug.writeln("patching direct bind call in " + this + " at " + r.getSource().stringRep() + " to refer to " + cc);
                    r.patchTo(cc);
                }
            }
        }
        public Iterator getCompiledMethods() {
            return CodeAllocator.getCompiledMethods();
        }
        public final void deliverToStackFrame(Object ed, jq_CompiledCode t, Throwable x, jq_TryCatch tc, CodeAddress entry, StackAddress fp) {
            ((ExceptionDeliverer)ed).deliverToStackFrame(t, x, tc, entry, fp);
        }
        public final Object getThisPointer(Object ed, jq_CompiledCode t, CodeAddress ip, StackAddress fp) {
            return ((ExceptionDeliverer)ed).getThisPointer(t, ip, fp);
        }
    }
    
    static class Klass implements jq_Class.Delegate {
        public final Object newInstance(jq_Class c, int instance_size, Object vtable) {
            c.cls_initialize();
            return DefaultHeapAllocator.allocateObject(instance_size, vtable);
        }
    }
    static class Array implements jq_Array.Delegate {
        public final Object newInstance(jq_Array a, int length, Object vtable) {
            return DefaultHeapAllocator.allocateArray(length, a.getInstanceSize(length), vtable);
        }
    }
}
