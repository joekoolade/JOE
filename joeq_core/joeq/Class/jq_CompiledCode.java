// jq_CompiledCode.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Iterator;
import java.util.List;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_CompiledCode.java,v 1.30 2004/09/22 22:17:28 joewhaley Exp $
 */
public class jq_CompiledCode implements Comparable {

    public static /*final*/ boolean TRACE = false;
    public static /*final*/ boolean TRACE_REDIRECT = false;

    protected final CodeAddress entrypoint;
    protected final jq_Method method;
    protected final CodeAddress start;
    protected final int length;
    protected final jq_TryCatch[] handlers;
    protected final jq_BytecodeMap bcm;
    protected final Object /* ExceptionDeliverer */ ed;
    protected final int stackframesize;
    protected final List code_reloc, data_reloc;

    public jq_CompiledCode(jq_Method method,
                           CodeAddress start,
                           int length,
                           CodeAddress entrypoint,
                           jq_TryCatch[] handlers,
                           jq_BytecodeMap bcm,
                           Object /*ExceptionDeliverer*/ ed,
                           int stackframesize,
                           List code_reloc,
                           List data_reloc) {
        this.method = method;
        this.entrypoint = entrypoint;
        this.start = start;
        this.length = length;
        this.handlers = handlers;
        this.bcm = bcm;
        this.ed = ed;
        this.stackframesize = stackframesize;
        this.code_reloc = code_reloc;
        this.data_reloc = data_reloc;
    }

    public jq_Method getMethod() {
        return method;
    }

    public CodeAddress getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public CodeAddress getEntrypoint() {
        return entrypoint;
    }

    public int getStackFrameSize() {
        return stackframesize;
    }
    
    public jq_TryCatch findCatchBlock(CodeAddress ip, jq_Class extype) {
        int offset = ip.difference(start);
        if (handlers == null) {
            if (TRACE) Debug.writeln("no handlers in " + this);
            return null;
        }
        for (int i = 0; i < handlers.length; ++i) {
            jq_TryCatch tc = handlers[i];
            if (TRACE) Debug.writeln("checking handler: " + tc);
            if (tc.catches(offset, extype))
                return tc;
            if (TRACE) Debug.writeln("does not catch");
        }
        if (TRACE) Debug.writeln("no appropriate handler found in " + this);
        return null;
    }

    public void deliverException(jq_TryCatch tc, StackAddress fp, Throwable x) {
        Assert._assert(ed != null);
        CodeAddress entry = (CodeAddress) start.offset(tc.getHandlerEntry());  
        _delegate.deliverToStackFrame(ed, this, x, tc, entry, fp);
    }

    public Object getThisPointer(CodeAddress ip, StackAddress fp) {
        Assert._assert(ed != null);
        return _delegate.getThisPointer(ed, this, ip, fp);
    }

    public int getBytecodeIndex(CodeAddress ip) {
        if (bcm == null) return -1;
        return bcm.getBytecodeIndex(ip.difference(start));
    }

    /** Rewrite the entrypoint to branch to the given compiled code. */
    public void redirect(jq_CompiledCode that) {
        CodeAddress newEntrypoint = that.getEntrypoint();
        if (TRACE_REDIRECT) Debug.writeln("redirecting " + this + " to point to " + that);
        if (entrypoint.difference(start.offset(5)) >= 0) {
            if (TRACE_REDIRECT) Debug.writeln("redirecting via trampoline");
            // both should start with "push EBP"
            Assert._assert(entrypoint.peek1() == newEntrypoint.peek1());
            // put target address (just after push EBP)
            entrypoint.offset(-4).poke4(newEntrypoint.difference(entrypoint) + 1);
            // put jump instruction
            entrypoint.offset(-5).poke1((byte) 0xE9); // JMP
            // put backward branch to jump instruction
            entrypoint.offset(1).poke2((short) 0xF8EB); // JMP
        } else {
            if (TRACE_REDIRECT) Debug.writeln("redirecting by rewriting targets");
            Iterator it = _delegate.getCompiledMethods();
            while (it.hasNext()) {
                jq_CompiledCode cc = (jq_CompiledCode) it.next();
                cc.patchDirectBindCalls(this.method, that);
            }
        }
    }

    public String toString() {
        return method + " address: (" + start.stringRep() + "-" + start.offset(length).stringRep() + ")";
    }

    public boolean contains(CodeAddress address) {
        return address.difference(start) >= 0 && address.difference(start.offset(length)) < 0;
    }

    static interface Delegate {
        void patchDirectBindCalls(Iterator i);
        void patchDirectBindCalls(Iterator i, jq_Method method, jq_CompiledCode cc);
        Iterator getCompiledMethods();
        void deliverToStackFrame(Object ed, jq_CompiledCode t, Throwable x, jq_TryCatch tc, CodeAddress entry, StackAddress fp);
        Object getThisPointer(Object ed, jq_CompiledCode t, CodeAddress ip, StackAddress fp);
    }
    
    private static Delegate _delegate;

    public void patchDirectBindCalls() {
        Assert._assert(jq.RunningNative);
        if (code_reloc != null) {
            Iterator i = code_reloc.iterator();
            _delegate.patchDirectBindCalls(i);
        }
    }

    public void patchDirectBindCalls(jq_Method method, jq_CompiledCode cc) {
        Assert._assert(jq.RunningNative);
        if (code_reloc != null) {
            Iterator i = code_reloc.iterator();
            _delegate.patchDirectBindCalls(i, method, cc);
        }
    }

    public int compareTo(jq_CompiledCode that) {
        if (this == that) return 0;
        if (this.start.difference(that.start) < 0) return -1;
        if (this.start.difference(that.start.offset(that.length)) < 0) {
            Assert.UNREACHABLE(this + " overlaps " + that);
        }
        return 1;
    }

    public int compareTo(java.lang.Object o) {
        if (o instanceof jq_CompiledCode)
            return compareTo((jq_CompiledCode) o);
        else
            return -((Comparable)o).compareTo(this);
    }

    public boolean equals(Object o) {
        if (o instanceof jq_CompiledCode)
            return this == o;
        else
            return o.equals(this);
    }

    /**
     * NOTE that this violates the contract of hashCode when comparing against InstructionPointer objects!
     */
    public int hashCode() {
        return super.hashCode();
    }

    public static final jq_InstanceField _entrypoint;

    static {
        jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_CompiledCode;");
        _entrypoint = k.getOrCreateInstanceField("entrypoint", "Ljoeq/Memory/CodeAddress;");
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Class.Delegates$CompiledCode");
        }
        if (_delegate == null) {
            _delegate = new NullDelegates.CompiledCode();
        }
    }

    private static Delegate attemptDelegate(String s) {
        String type = "compiled code delegate";
        try {
            Class c = Class.forName(s);
            return (Delegate)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }
}
