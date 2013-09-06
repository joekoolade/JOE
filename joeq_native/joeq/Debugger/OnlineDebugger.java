// OnlineDebugger.java, created Sat Feb 22 13:35:26 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Debugger;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import joeq.Allocator.CodeAllocator;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_LocalVarTableEntry;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Compiler.CompilationState;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Main.TraceFlags;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.Reflection;
import joeq.Runtime.StackCodeWalker;
import joeq.Runtime.SystemInterface;
import joeq.Scheduler.jq_NativeThread;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: OnlineDebugger.java,v 1.12 2004/09/30 03:35:36 joewhaley Exp $
 */
public class OnlineDebugger {

    public static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    static boolean inDebugger;

    public static boolean debuggerEntryPoint() {
        if (!jq.RunningNative) {
            new InternalError().printStackTrace();
            System.exit(-1);
        }
        if (inDebugger) {
            SystemInterface.debugwriteln("Recursively entering debugger!");
            StackAddress fp = StackAddress.getBasePointer();
            CodeAddress ip = (CodeAddress) fp.offset(4).peek();
            Debug.writeln("fp = ", fp);
            Debug.writeln("ip = ", ip);
            return false;
        }
        inDebugger = true;
        SystemInterface.debugwriteln(">>> Entering debugger.");
        StackAddress fp = StackAddress.getBasePointer();
        CodeAddress ip = (CodeAddress) fp.offset(4).peek();
        fp = (StackAddress) fp.peek();
        StackCodeWalker sw = new StackCodeWalker(ip, fp);
        int frameNum = 0;
        SystemInterface.debugwriteln("> "+frameNum+":"+sw.toString());
        
        //bsh.Interpreter i = null;
uphere:
        for (;;) {
            SystemInterface.debugwrite("db> ");
            String s = null;
            try {
                s = in.readLine();
            } catch (IOException _) { }
            if (s == null) {
                SystemInterface.debugwriteln(">>> Exiting debugger.");
                inDebugger = false;
                return true;
            }
            if (s.equals("")) {
                continue;
            }
            if (s.equals("c")) {
                SystemInterface.debugwriteln(">>> Continuing execution.");
                inDebugger = false;
                return true;
            }
            if (s.equals("s")) {
                SystemInterface.debugwriteln("Single-step not yet implemented.");
                continue;
            }
            if (s.equals("u")) {
                if (!sw.hasNext()) {
                    SystemInterface.debugwriteln("Reached top.");
                    continue;
                }
                sw.gotoNext(); ++frameNum;
                SystemInterface.debugwriteln("> "+frameNum+":"+sw.toString());
                continue;
            }
            if (s.equals("d")) {
                if (frameNum == 0) {
                    SystemInterface.debugwriteln("Reached top.");
                    continue;
                }
                StackCodeWalker sw2 = new StackCodeWalker(ip, fp);
                for (;;) {
                    if (!sw2.hasNext()) {
                        SystemInterface.debugwriteln("ERROR! Stack is corrupted.");
                        continue uphere;
                    }
                    if (sw2.getFP().peek().difference(sw.getFP()) == 0)
                        break;
                    sw2.gotoNext();
                }
                sw = sw2; --frameNum;
                SystemInterface.debugwriteln("> "+frameNum+":"+sw.toString());
                continue;
            }
            if (s.equals("bt")) {
                StackCodeWalker sw2 = new StackCodeWalker(ip, fp);
                int counter = 0;
                while (sw2.hasNext()) {
                    if (counter == frameNum) {
                        SystemInterface.debugwriteln("> "+counter+":"+sw2.toString());
                        if (sw2.getFP().difference(sw.getFP()) != 0) {
                            SystemInterface.debugwriteln("ERROR! Stack is corrupted. Expected "+sw+" but found "+sw2);
                        }
                    } else {
                        SystemInterface.debugwriteln("  "+counter+":"+sw2.toString());
                    }
                    sw2.gotoNext(); ++counter;
                }
                continue;
            }
            if (s.equals("t")) {
                jq_NativeThread.dumpAllThreads();
                continue;
            }
            if (s.equals("sf")) {
                printStackFrame(sw);
                continue;
            }
            if (s.equals("l")) {
                jq_Method m = sw.getMethod();
                if (m == null) {
                    SystemInterface.debugwriteln("Unknown method!");
                    continue;
                }
                if (m.getBytecode() == null) {
                    SystemInterface.debugwriteln("No bytecode for method "+m+"!");
                    continue;
                }
                int currentIndex = sw.getBCIndex();
                if (currentIndex < 0) currentIndex = 0;
                BytecodeLister bl = new BytecodeLister(m, 0, m.getBytecode().length, currentIndex);
                bl.forwardTraversal();
                continue;
            }
            if (s.equals("?")) {
                printUsage();
                continue;
            }
            if (s.startsWith("toString ")) {
                s = s.substring(9);
                if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
                int k = (int) Long.parseLong(s, 16);
                HeapAddress addr = HeapAddress.address32(k);
                Object o = addr.asObject();
                SystemInterface.debugwriteln(o.toString());
                continue;
            }
            if (s.startsWith("dumpObject ")) {
                s = s.substring(11);
                if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
                int k = (int) Long.parseLong(s, 16);
                dumpObject(k);
                continue;
            }
            
            String[] commands;
            StringTokenizer st = new StringTokenizer(s);
            int size = st.countTokens();
            commands = new String[size];
            for (int j = 0; j < size; ++j) {
                commands[j] = st.nextToken();
            }
            Assert._assert(!st.hasMoreTokens());
            int index2 = TraceFlags.setTraceFlag(commands, 0);
            if (0 != index2) {
                continue;
            }
            
            /*
            if (i == null) i = new bsh.Interpreter();
            try {
                Object result = i.eval(s);
                SystemInterface.debugwriteln("Result: "+result);
            } catch (bsh.EvalError x) {
                SystemInterface.debugwriteln("Evaluation error: "+x);
            }
            */
        }
    }

    public static void printUsage() {
        SystemInterface.debugwriteln("c: continue, s: step");
        SystemInterface.debugwriteln("l: list bytecode");
        SystemInterface.debugwriteln("dumpObject, toString");
        SystemInterface.debugwriteln("u: up, d: down, sf: stack frame, bt: back trace, t: thread dump");
    }
    
    public static void dumpObject(int k) {
        HeapAddress addr = HeapAddress.address32(k);
        Object o = addr.asObject();
        dumpObject(o);
    }
    
    public static void dumpObject(Object o) {
        HeapAddress addr = HeapAddress.addressOf(o);
        jq_Reference t = jq_Reference.getTypeOf(o);
        SystemInterface.debugwriteln(addr.stringRep()+" type "+t);
        if (t.isClassType()) {
            jq_Class c = (jq_Class) t;
            jq_InstanceField[] f = c.getInstanceFields();
            for (int j=0; j<f.length; ++j) {
                StringBuffer sb = new StringBuffer();
                sb.append(Strings.left(f[j].getName().toString(), 15));
                sb.append(':');
                if (f[j].getType().isReferenceType()) {
                    Address addr2 = addr.offset(f[j].getOffset()).peek();
                    sb.append(addr2.stringRep());
                } else {
                    sb.append(Reflection.getfield(o, f[j]));
                }
                SystemInterface.debugwriteln(sb.toString());
            }
        } else {
            jq_Array a = (jq_Array) t;
            int length = Reflection.arraylength(o);
            for (int j=0; j<length; ++j) {
                if (a.getElementType().isReferenceType()) {
                    SystemInterface.debugwriteln(j+": "+addr.offset(j*HeapAddress.size()).peek().stringRep());
                } else if (a.getElementType() == jq_Primitive.FLOAT) {
                    SystemInterface.debugwriteln(j+": "+Float.intBitsToFloat(addr.offset(j*4).peek4()));
                } else if (a.getElementType() == jq_Primitive.DOUBLE) {
                    SystemInterface.debugwriteln(j+": "+Double.longBitsToDouble(addr.offset(j*8).peek8()));
                } else if (a.getElementType().getReferenceSize() == 1) {
                    SystemInterface.debugwriteln(j+": "+addr.offset(j).peek1());
                } else if (a.getElementType().getReferenceSize() == 2) {
                    SystemInterface.debugwriteln(j+": "+addr.offset(j*2).peek2());
                } else if (a.getElementType().getReferenceSize() == 4) {
                    SystemInterface.debugwriteln(j+": "+addr.offset(j*4).peek4());
                } else if (a.getElementType().getReferenceSize() == 8) {
                    SystemInterface.debugwriteln(j+": "+addr.offset(j*8).peek8());
                }
            }
        }
    }
    
    public static void printStackFrame(StackCodeWalker sw) {
        StackAddress my_fp = sw.getFP();
        if (my_fp.isNull()) {
            SystemInterface.debugwriteln("Cannot dump this frame!"+Strings.lineSep);
            return;
        }
        CodeAddress my_ip = (CodeAddress) my_fp.offset(4).peek();
        StackAddress next_fp = (StackAddress) my_fp.peek();
        if (my_fp.isNull()) {
            SystemInterface.debugwriteln("Cannot dump this frame!"+Strings.lineSep);
            return;
        }
        jq_CompiledCode cc = CodeAllocator.getCodeContaining(my_ip);
        jq_Method m = null;
        if (cc != null) m = cc.getMethod();
        SystemInterface.debugwriteln("Stack frame for "+cc);
        if (m != null) {
            SystemInterface.debugwriteln(((int)m.getMaxLocals())+" locals, "+((int)m.getParamWords())+" param words");
        }
        // b0: next->| caller's saved FP  |
        // ac:       | caller's locals    |
        //           |        ...         |
        // 94:       | caller's opstack   |
        //           |        ...         |
        // 80:       | pushed params      |
        //           |        ...         |
        // 74:       | ret addr in caller |
        // 70:   my->| callee's FP (b0)   |
        StackAddress ptr = my_fp;
        int framesize = 0;
        if (m != null) framesize = (m.getParamWords()+1)*StackAddress.size();
        while (ptr.difference(next_fp) <= framesize) {
            if (ptr.difference(my_fp) == 0) {
                SystemInterface.debugwriteln("Callee FP:      "+ptr.stringRep()+" : "+ptr.peek().stringRep());
            } else if (ptr.difference(next_fp) == StackAddress.size()) {
                CodeAddress my_ip2 = (CodeAddress) ptr.peek();
                jq_CompiledCode cc2 = CodeAllocator.getCodeContaining(my_ip2);
                int code_offset = 0;
                if (cc2 != null)
                    code_offset = my_ip2.difference(cc2.getStart());
                SystemInterface.debugwriteln("Caller retaddr: "+ptr.stringRep()+" : "+ptr.peek().stringRep()+"  (offset "+Strings.hex(code_offset)+")");
            } else if (m != null && ptr.difference(next_fp) > 0) {
                int n = m.getParamWords() - ptr.difference(next_fp) / StackAddress.size() + 1;
                SystemInterface.debugwriteln(Strings.left("Incoming arg "+n+":", 16)+ptr.stringRep()+" : "+ptr.peek().stringRep());
            } else if (ptr.difference(my_fp) == StackAddress.size()) {
                int code_offset = 0;
                if (cc != null)
                    code_offset = my_ip.difference(cc.getStart());
                SystemInterface.debugwriteln("Return address: "+ptr.stringRep()+" : "+ptr.peek().stringRep()+"  (offset "+Strings.hex(code_offset)+")");
            } else if (next_fp.difference(ptr) == 0) {
                SystemInterface.debugwriteln("Caller FP:      "+ptr.stringRep()+" : "+ptr.peek().stringRep());
            } else if (m != null && next_fp.difference(ptr) <= StackAddress.size()*(m.getMaxLocals()-m.getParamWords())) {
                int n = next_fp.difference(ptr) / StackAddress.size() - 1;
                int offset = sw.getBCIndex();
                jq_LocalVarTableEntry e = m.getLocalVarTableEntry(offset, n);
                String nd = (e != null) ? e.getNameAndDesc().toString() : "";
                SystemInterface.debugwriteln(Strings.left("Local "+n+": ", 16)+ptr.stringRep()+" : "+ptr.peek().stringRep()+"\t"+nd);
            } else if (m != null && ptr.difference(my_fp) > StackAddress.size()) {
                int n = next_fp.difference(ptr) / StackAddress.size() - m.getMaxLocals() + m.getParamWords() - 2;
                SystemInterface.debugwriteln(Strings.left("Stack "+n+": ", 16)+ptr.stringRep()+" : "+ptr.peek().stringRep());
            } else {
                SystemInterface.debugwriteln("                "+ptr.stringRep()+" : "+ptr.peek().stringRep());
            }
            ptr = (StackAddress) ptr.offset(StackAddress.size());
        }
    }
    
    public static class BytecodeLister extends BytecodeVisitor {
        
        protected int i_loc;
        protected int i_stop;
        
        public BytecodeLister(jq_Method m, int start, int stop, int loc) {
            super(CompilationState.DEFAULT, m);
            this.i_end = start-1;
            this.i_start = start;
            this.i_stop = stop;
            this.i_loc = loc;
            this.out = System.err;
            this.TRACE = true;
        }
        public void forwardTraversal() throws VerifyError {
            for (i_end=-1; ; ) {
                i_start = i_end+1;
                if (i_start == i_loc) {
                    out.println("Current location:");
                }
                if (i_start >= i_stop) break;
                this.visitBytecode();
            }
        }
        public String toString() { return Strings.left(method.getName().toString(), 12); }
    }
    
}
