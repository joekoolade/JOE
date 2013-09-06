// NullDelegates.java, created Wed Dec 11 12:02:02 2002 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Iterator;
import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;
import jwutil.util.Assert;

/**
 * NullDelegates
 *
 * @author Michael Martin <mcmartin@stanford.edu>
 * @version $Id: NullDelegates.java,v 1.14 2004/09/22 22:17:29 joewhaley Exp $
 */
abstract class NullDelegates {
    static class Field implements jq_Field.Delegate {
        public final boolean isCodeAddressType(jq_Field f) { return false; }
        public final boolean isHeapAddressType(jq_Field f) { return false; }
        public final boolean isStackAddressType(jq_Field f) { return false; }
    }

    static class Method implements jq_Method.Delegate {
        public final jq_CompiledCode compile_stub (jq_Method m) {
            return null;
        }
        public final jq_CompiledCode compile (jq_Method m) {
            return null;
        }
    }

    static class CompiledCode implements jq_CompiledCode.Delegate {
        public final void patchDirectBindCalls (Iterator i) { }
        public final void patchDirectBindCalls (Iterator i, jq_Method m, jq_CompiledCode cc) { }
        public final Iterator getCompiledMethods () { 
            return new java.util.LinkedList().iterator();
        }
        public final void deliverToStackFrame(Object ed, jq_CompiledCode t, Throwable x, jq_TryCatch tc, CodeAddress entry, StackAddress fp) { }
        public final Object getThisPointer(Object ed, jq_CompiledCode t, CodeAddress ip, StackAddress fp) { return null; }
    }

    static class Klass implements jq_Class.Delegate {
        public final Object newInstance(jq_Class c, int instance_size, Object vtable) {
            try {
                return Class.forName(c.getName()).newInstance();
            } catch (Exception e) { return null; }
        }
    }

    static class Array implements jq_Array.Delegate {
        public final Object newInstance(jq_Array a, int length, Object vtable) {
            Assert.UNREACHABLE("Can't create new arrays!");
            return null;
        }
    }
}
