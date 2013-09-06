// ExceptionDeliverer.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import joeq.Allocator.CodeAllocator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Method;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_TryCatch;
import joeq.Debugger.OnlineDebugger;
import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;
import joeq.Scheduler.jq_Thread;
import joeq.UTF.Utf8;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ExceptionDeliverer.java,v 1.25 2004/09/30 03:35:35 joewhaley Exp $
 */
public abstract class ExceptionDeliverer {

    public static /*final*/ boolean TRACE = false;
    
    public static void abstractMethodError() throws AbstractMethodError {
        SystemInterface.debugwriteln("Unimplemented abstract method!");
        throw new AbstractMethodError();
    }
    
    public static void nativeMethodError() throws LinkageError {
        SystemInterface.debugwriteln("Unimplemented native method!");
        throw new LinkageError();
    }
    
    public static void athrow(Throwable k) {
        CodeAddress ip = (CodeAddress) StackAddress.getBasePointer().offset(StackAddress.size()).peek();
        StackAddress fp = (StackAddress) StackAddress.getBasePointer().peek();
        ExceptionDeliverer.deliverToCurrentThread(k, ip, fp);
        Assert.UNREACHABLE();
    }
    
    public static void trap_handler(int code) {
        switch (code) {
            case 0: throw new NullPointerException();
            case 1: throw new ArrayIndexOutOfBoundsException();
            case 2: throw new ArithmeticException();
            case 3: throw new StackOverflowError();
            default: throw new InternalError("unknown hardware exception type: "+code);
        }
    }
    public static void debug_trap_handler(int code) {
        boolean pass = OnlineDebugger.debuggerEntryPoint();
        if (pass) {
            SystemInterface.debugwriteln(">>> Passing on exception code "+code);
            trap_handler(code);
            Assert.UNREACHABLE();
        }
    }
    
    public abstract void deliverToStackFrame(jq_CompiledCode cc, Throwable x, jq_TryCatch tc, CodeAddress ip, StackAddress fp);
    public abstract Object getThisPointer(jq_CompiledCode cc, CodeAddress ip, StackAddress fp);
    
    public static void deliverToCurrentThread(Throwable x, CodeAddress ip, StackAddress fp) {
        jq_Thread t = Unsafe.getThreadBlock();
        Assert._assert(t != null && !t.is_delivering_exception);
        t.is_delivering_exception = true;
        
        jq_Class x_type = (jq_Class) jq_Reference.getTypeOf(x);
        if (TRACE) SystemInterface.debugwriteln("Delivering exception of type "+x_type+" to ip="+ip.stringRep()+" fp="+fp.stringRep());
        for (;;) {
            jq_CompiledCode cc = CodeAllocator.getCodeContaining(ip);
            if (TRACE) SystemInterface.debugwriteln("Checking compiled code "+cc);
            if ((cc == null) || (fp.isNull())) {
                // reached the top!
                System.out.println("Exception in thread \""+Unsafe.getThreadBlock()+"\" "+x);
                x.printStackTrace(System.out);
                t.is_delivering_exception = false;
                SystemInterface.die(-1);
                Assert.UNREACHABLE();
                return;
            } else {
                jq_TryCatch tc = cc.findCatchBlock(ip, x_type);
                if (tc != null) {
                    CodeAddress address = (CodeAddress) cc.getStart().offset(tc.getHandlerEntry());
                    
                    // go to this catch block!
                    if (TRACE) SystemInterface.debugwriteln("Jumping to catch block at "+address.stringRep());
                    t.is_delivering_exception = false;
                    cc.deliverException(tc, fp, x);
                    Assert.UNREACHABLE();
                    return;
                }
                if (cc.getMethod() != null && cc.getMethod().isSynchronized()) {
                    // need to perform monitorexit here.
                    Object o;
                    if (cc.getMethod().isStatic()) {
                        o = Reflection.getJDKType(cc.getMethod().getDeclaringClass());
                        if (TRACE) SystemInterface.debugwriteln("Performing monitorexit on static method "+cc.getMethod()+": object "+o);
                    } else {
                        o = cc.getThisPointer(ip, fp);
                        if (TRACE) SystemInterface.debugwriteln("Performing monitorexit on instance method "+cc.getMethod()+": object "+o.getClass()+"@"+Strings.hex(System.identityHashCode(o)));
                    }
                    Monitor.monitorexit(o);
                }
                ip = (CodeAddress) fp.offset(StackAddress.size()).peek();
                fp = (StackAddress) fp.peek();
            }
        }
    }
    
    public static void printStackTrace(Object backtrace, java.io.PrintWriter pw) {
        StackFrame sf = (StackFrame)backtrace;
        while (sf.next != null) {
            CodeAddress ip = sf.ip;
            jq_CompiledCode cc = CodeAllocator.getCodeContaining(ip);
            String s;
            if (cc != null) {
                jq_Method m = cc.getMethod();
                int code_offset = ip.difference(cc.getStart());
                if (m != null) {
                    Utf8 sourcefile = m.getDeclaringClass().getSourceFile();
                    int bc_index = cc.getBytecodeIndex(ip);
                    int line_num = m.getLineNumber(bc_index);
                    s = "\tat "+m+" ("+sourcefile+":"+line_num+" bc:"+bc_index+" off:"+Strings.hex(code_offset)+")";
                } else {
                    s = "\tat <unknown cc> (start:"+cc.getStart().stringRep()+" off:"+Strings.hex(code_offset)+")";
                }
            } else {
                s = "\tat <unknown addr> (ip:"+ip.stringRep()+")";
            }
            pw.println(s.toCharArray());
            sf = sf.next;
        }
    }

    public static void printStackTrace(Object backtrace, java.io.PrintStream pw) {
        StackFrame sf = (StackFrame)backtrace;
        while (sf.next != null) {
            CodeAddress ip = sf.ip;
            jq_CompiledCode cc = CodeAllocator.getCodeContaining(ip);
            String s;
            if (cc != null) {
                jq_Method m = cc.getMethod();
                int code_offset = ip.difference(cc.getStart());
                if (m != null) {
                    Utf8 sourcefile = m.getDeclaringClass().getSourceFile();
                    int bc_index = cc.getBytecodeIndex(ip);
                    int line_num = m.getLineNumber(bc_index);
                    s = "\tat "+m+" ("+sourcefile+":"+line_num+" bc:"+bc_index+" off:"+Strings.hex(code_offset)+")";
                } else {
                    s = "\tat <unknown cc> (start:"+cc.getStart().stringRep()+" off:"+Strings.hex(code_offset)+")";
                }
            } else {
                s = "\tat <unknown addr> (ip:"+ip.stringRep()+")";
            }
            pw.println(s.toCharArray());
            sf = sf.next;
        }
    }
    
    public static void printStackTrace(Object backtrace) {
        StackFrame sf = (StackFrame)backtrace;
        while (sf.next != null) {
            CodeAddress ip = sf.ip;
            jq_CompiledCode cc = CodeAllocator.getCodeContaining(ip);
            String s;
            if (cc != null) {
                jq_Method m = cc.getMethod();
                int code_offset = ip.difference(cc.getStart());
                if (m != null) {
                    Utf8 sourcefile = m.getDeclaringClass().getSourceFile();
                    int bc_index = cc.getBytecodeIndex(ip);
                    int line_num = m.getLineNumber(bc_index);
                    s = "\tat "+m+" ("+sourcefile+":"+line_num+" bc:"+bc_index+" off:"+Strings.hex(code_offset)+")";
                } else {
                    s = "\tat <unknown cc> (start:"+cc.getStart().stringRep()+" off:"+Strings.hex(code_offset)+")";
                }
            } else {
                s = "\tat <unknown addr> (ip:"+ip.stringRep()+")";
            }
            SystemInterface.debugwriteln(s);
            sf = sf.next;
        }
    }
    
    public static Object getStackTrace() {
        // stack traces are a linked list.
        CodeAddress ip = (CodeAddress) StackAddress.getBasePointer().offset(StackAddress.size()).peek();
        StackAddress fp = (StackAddress) StackAddress.getBasePointer().peek();
        StackFrame sf = new StackFrame(fp, ip);
        sf.fillInStackTrace();
        return sf;
    }
    
    public static class StackFrame {
        protected StackAddress fp; // location of this stack frame
        protected CodeAddress ip;  // ip address
        protected StackFrame next; // next frame in linked list
        
        public StackFrame(StackAddress fp, CodeAddress ip) {
            this.fp = fp; this.ip = ip;
        }
        
        public void fillInStackTrace() {
            StackFrame dis = this;
            while (!dis.fp.isNull()) {
                CodeAddress ip2 = (CodeAddress) dis.fp.offset(StackAddress.size()).peek();
                StackAddress fp2 = (StackAddress) dis.fp.peek();
                dis.next = new StackFrame(fp2, ip2);
                dis = dis.next;
            }
        }

        public int getSize() {
            StackFrame p = this; int s = 0;
            while (p != null) {
                p = p.next; ++s;
            }
            return s;
        }
        public StackFrame getNext() { return next; }
        public StackAddress getFP() { return fp; }
        public CodeAddress getIP() { return ip; }
        
        public String toString() {
            return CodeAllocator.getCodeContaining(ip)+" ip="+ip.stringRep()+" fp="+fp.stringRep();
        }
    }
    
    public static final jq_Class _class;
    public static final jq_StaticMethod _athrow;
    public static final jq_StaticMethod _trap_handler;
    public static final jq_StaticMethod _debug_trap_handler;
    public static final jq_StaticMethod _abstractMethodError;
    public static final jq_StaticMethod _nativeMethodError;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Runtime/ExceptionDeliverer;");
        _athrow = _class.getOrCreateStaticMethod("athrow", "(Ljava/lang/Throwable;)V");
        _trap_handler = _class.getOrCreateStaticMethod("trap_handler", "(I)V");
        _debug_trap_handler = _class.getOrCreateStaticMethod("debug_trap_handler", "(I)V");
        _abstractMethodError = _class.getOrCreateStaticMethod("abstractMethodError", "()V");
        _nativeMethodError = _class.getOrCreateStaticMethod("nativeMethodError", "()V");
    }
}
