// ProgramLocation.java, created Sun Sep  1 17:38:25 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_FakeInstanceMethod;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_LineNumberBC;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Compiler.BytecodeAnalysis.Bytecodes;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.UTF.Utf8;
import jwutil.io.ByteSequence;
import jwutil.io.Textualizable;
import jwutil.io.Textualizer;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * This class provides a general mechanism to describe a location in the code,
 * independent of IR type.  It combines a method and a location within that
 * method.  This is useful for interprocedural analysis, among other things.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ProgramLocation.java,v 1.28 2006/05/02 22:01:25 mcmartin Exp $
 */
public abstract class ProgramLocation implements Textualizable {
    public static final boolean GIVE_SIGNATURES = !System.getProperty("pa.signaturesinlocs", "no").equals("no");
    /** The method of this location. **/
    protected final jq_Method m;
    
    protected ProgramLocation(jq_Method m) {
        this.m = m;
    }
    
    public jq_Class getContainingClass() {
        return m.getDeclaringClass();
    }
    
    public jq_Method getMethod() {
        return m;
    }
    
    public Utf8 getSourceFile() {
        return getContainingClass().getSourceFile();
    }
    
    public int getLineNumber() {
        return m.getLineNumber(getBytecodeIndex());
    }

    /** Print a location as it would appear in an exception stacktrace. */
    public String toStringLong() {
        return getContainingClass()+"."+getMethod().getName()+'('+getSourceFile()+':'+getLineNumber()+')';
    }
    
    public abstract int getID();
    public abstract int getBytecodeIndex();
    public abstract jq_Type getResultType();
    
    public abstract void write(Textualizer t) throws IOException;
    public void writeEdges(Textualizer t) throws IOException {}
    public void addEdge(String edgeName, Textualizable t) {}
    
    public abstract boolean isCall();
    public abstract jq_Method getTargetMethod();
    public abstract jq_Method resolveTargetMethod();
    public abstract jq_Type[] getParamTypes();
    public abstract jq_Type getReturnType();
    public abstract boolean isSingleTarget();
    public abstract boolean isInterfaceCall();
    public abstract byte getInvocationType();

    public String getEmacsName() {
        Utf8 source = getSourceFile();
        if (source != null) {
            int lineno = getLineNumber();
            return source+":"+lineno;
        } else {
            String className = getContainingClass().getJDKName();
            String method = m.getNameAndDesc().toString();
            int id = getID();
            return className+":"+method+":"+id;
        }
    }
    
    public static class QuadProgramLocation extends ProgramLocation {
        private final Quad q;
        public QuadProgramLocation(jq_Method m, Quad q) {
            super(m);
            this.q = q;
        }
        
        public Quad getQuad() {
            return q;
        }
        
        public int getID() {
            return q.getID();
        }
        
        public int getBytecodeIndex() {
            Map map = CodeCache.getBCMap((jq_Method) super.m);
            if (map == null) return -1;
            Integer i = (Integer) map.get(q);
            if (i == null) return -1;
            return i.intValue();
        }
        
        public jq_Type getResultType() {
            return q.getDefinedRegisters().getRegisterOperand(0).getType();
        }
        
        public boolean isCall() {
            return q.getOperator() instanceof Invoke;
        }
        
        public jq_Method getTargetMethod() {
            Assert._assert(isCall());
            return Invoke.getMethod(q).getMethod();
        }
        
        public jq_Method resolveTargetMethod() {
            Assert._assert(isCall());
            Invoke.getMethod(q).resolve();
            return Invoke.getMethod(q).getMethod();
        }

        public jq_Type[] getParamTypes() {
            Assert._assert(isCall());
            jq_Type[] t = Invoke.getMethod(q).getMethod().getParamTypes();
            if (t.length != Invoke.getParamList(q).length()) {
                t = new jq_Type[Invoke.getParamList(q).length()];
                for (int i=0; i<t.length; ++i) {
                    t[i] = Invoke.getParamList(q).get(i).getType();
                }
            }
            return t;
        }
        
        public jq_Type getReturnType() {
            Assert._assert(isCall());
            return Invoke.getMethod(q).getMethod().getReturnType();
        }
        
        public boolean isSingleTarget() {
            if (isInterfaceCall()) return false;
            if (!((Invoke) q.getOperator()).isVirtual()) return true;
            Object trg = Invoke.getMethod(q).getMethod();
            Assert._assert(trg instanceof jq_InstanceMethod, "Unexpected " + trg + " of type " + trg.getClass());
            jq_InstanceMethod target = (jq_InstanceMethod) trg;
            target.getDeclaringClass().load();
            if (target.getDeclaringClass().isFinal()) return true;
            target.getDeclaringClass().prepare();
            if (!target.isLoaded()) {
                target = target.resolve1();
                if (!target.isLoaded()) {
                    // bad target method!
                    return false;
                }
                Invoke.getMethod(q).setMethod(target);
            }
            if (target.isFinal()) return true;
            if (!target.isVirtual()) return true;
            return false;
        }
        
        public boolean isInterfaceCall() {
            return q.getOperator() instanceof Invoke.InvokeInterface;
        }
        
        public int hashCode() {
            return (q==null)?-1:q.hashCode();
        }
        public boolean equals(QuadProgramLocation that) {
            return this.q == that.q;
        }
        public boolean equals(Object o) {
            if (o instanceof QuadProgramLocation)
                return equals((QuadProgramLocation)o);
            return false;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.m.getDeclaringClass().getName());
            sb.append('.');
            sb.append(super.m.getName());
            sb.append("() quad ");
            sb.append((q==null)?-1:q.getID());
            if (q.getOperator() instanceof Invoke) {
                sb.append(" => ");
                if (GIVE_SIGNATURES) {
                    sb.append(Invoke.getMethod(q).getMethod().getNameAndDesc());
                } else {                    
                    sb.append(Invoke.getMethod(q).getMethod().getName());
                    sb.append("()");
                }
                if (isSingleTarget())
                    sb.append("*");
            }
//            sb.append(" [");
//            sb.append(getEmacsName());
//            sb.append("]");
            
            return sb.toString();
        }
        
        public byte getInvocationType() {
            if (q.getOperator() instanceof Invoke.InvokeVirtual) {
                return BytecodeVisitor.INVOKE_VIRTUAL;
            } else if (q.getOperator() instanceof Invoke.InvokeStatic) {
                jq_Method target = Invoke.getMethod(q).getMethod();
                if (target instanceof jq_InstanceMethod)
                    return BytecodeVisitor.INVOKE_SPECIAL;
                else
                    return BytecodeVisitor.INVOKE_STATIC;
            } else {
                Assert._assert(q.getOperator() instanceof Invoke.InvokeInterface);
                return BytecodeVisitor.INVOKE_INTERFACE;
            }
        }
        
        /*
        public CallTargets getCallTargets() {
            if (!(q.getOperator() instanceof Invoke)) return null;
            jq_Method target = Invoke.getMethod(q).getMethod();
            byte type = getInvocationType();
            return CallTargets.getTargets(target.getDeclaringClass(), target, type, true);
        }
        
        public CallTargets getCallTargets(AndersenReference klass, boolean exact) {
            if (!(q.getOperator() instanceof Invoke)) return null;
            jq_Method target = Invoke.getMethod(q).getMethod();
            byte type = getInvocationType();
            return CallTargets.getTargets(target.getDeclaringClass(), target, type, (jq_Reference)klass, exact, true);
        }
        
        public CallTargets getCallTargets(java.util.Set receiverTypes, boolean exact) {
            if (!(q.getOperator() instanceof Invoke)) return null;
            jq_Method target = Invoke.getMethod(q).getMethod();
            byte type = getInvocationType();
            return CallTargets.getTargets(target.getDeclaringClass(), target, type, receiverTypes, exact, true);
        }
        */
        
        public void write(Textualizer t) throws IOException {
            t.writeString("quad "+q.getID()+" ");
            t.writeObject(m);
        }
    }
    
    public static class BCProgramLocation extends ProgramLocation {
        final int bcIndex;
        
        public BCProgramLocation(jq_Method m, int bcIndex) {
            super(m);
            this.bcIndex = bcIndex;
        }
        
        public int getID() {
            return bcIndex;
        }
        
        public int getBytecodeIndex() {
            return bcIndex;
        }
        
        public byte getBytecode() {
            byte[] bc = ((jq_Method) super.m).getBytecode();
            return bc[bcIndex];
        }
        
        public jq_Type getResultType() {
            ByteSequence bs = new ByteSequence(m.getBytecode(), bcIndex, 8);
            try {
                Bytecodes.Instruction i = Bytecodes.Instruction.readInstruction(getContainingClass().getCP(), bs);
                if (!(i instanceof Bytecodes.TypedInstruction)) return null;
                return ((Bytecodes.TypedInstruction)i).getType();
            } catch (IOException x) {
                Assert.UNREACHABLE();
                return null;
            }
        }
        
        public boolean isCall() {
            switch (getBytecode()) {
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    return true;
                default:
                    return false;
            }
        }
        
        public jq_Method getTargetMethod() {
            jq_Class clazz = ((jq_Method) super.m).getDeclaringClass();
            byte[] bc = ((jq_Method) super.m).getBytecode();
            char cpi = Convert.twoBytesToChar(bc, bcIndex+1);
            switch (bc[bcIndex]) {
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                    return clazz.getCPasInstanceMethod(cpi);
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                    return clazz.getCPasStaticMethod(cpi);
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    return joeq.Runtime.Arrays._multinewarray;
                default:
                    return null;
            }
        }
        
        
        public jq_Method resolveTargetMethod() {
            jq_Method m = getTargetMethod();
            m = (jq_Method) m.resolve();
            return m;
        }
        
        public jq_Type[] getParamTypes() {
            return getTargetMethod().getParamTypes();
        }
        
        public jq_Type getReturnType() {
            return getTargetMethod().getReturnType();
        }
        
        public boolean isSingleTarget() {
            switch (getBytecode()) {
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    return true;
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                default:
                    return false;
            }
        }
        
        public boolean isInterfaceCall() {
            return getBytecode() == jq_ClassFileConstants.jbc_INVOKEINTERFACE;
        }

        public int hashCode() {
            return super.m.hashCode() ^ bcIndex;
        }
        public boolean equals(BCProgramLocation that) {
            return this.bcIndex == that.bcIndex && super.m == that.m;
        }
        public boolean equals(Object o) {
            if (o instanceof BCProgramLocation)
                return equals((BCProgramLocation) o);
            return false;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer(super.m.getDeclaringClass().getName());
            sb.append('.');
            if (GIVE_SIGNATURES) {
                sb.append(super.m.getNameAndDesc());
            } else {
                sb.append(super.m.getName()).append("()");                
            }                
            sb.append(" @ ").append(bcIndex);
            return sb.toString();
        }
        
        public byte getInvocationType() {
            switch (getBytecode()) {
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                    return BytecodeVisitor.INVOKE_VIRTUAL;
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                    return BytecodeVisitor.INVOKE_SPECIAL;
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                    return BytecodeVisitor.INVOKE_INTERFACE;
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    return BytecodeVisitor.INVOKE_STATIC;
                default:
                    return -1;
            }
        }
        /*
        public CallTargets getCallTargets() {
            jq_Class clazz = ((jq_Method) super.m).getDeclaringClass();
            byte[] bc = ((jq_Method) super.m).getBytecode();
            if (bc == null || bcIndex < 0 || bcIndex+2 >= bc.length) return null;
            char cpi = jwutil.util.Convert.twoBytesToChar(bc, bcIndex+1);
            byte type;
            jq_Method method;
            switch (bc[bcIndex]) {
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                    type = BytecodeVisitor.INVOKE_VIRTUAL;
                    // fallthrough
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                    type = BytecodeVisitor.INVOKE_SPECIAL;
                    // fallthrough
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                    method = clazz.getCPasInstanceMethod(cpi);
                    type = BytecodeVisitor.INVOKE_INTERFACE;
                    break;
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                    method = clazz.getCPasStaticMethod(cpi);
                    type = BytecodeVisitor.INVOKE_STATIC;
                    break;
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    method = joeq.Runtime.Arrays._multinewarray;
                    type = BytecodeVisitor.INVOKE_STATIC;
                    break;
                default:
                    return null;
            }
            return CallTargets.getTargets(clazz, method, type, true);
        }
        public CallTargets getCallTargets(AndersenReference klass, boolean exact) {
            jq_Class clazz = ((jq_Method) super.m).getDeclaringClass();
            byte[] bc = ((jq_Method) super.m).getBytecode();
            if (bc == null || bcIndex < 0 || bcIndex+2 >= bc.length) return null;
            char cpi = jwutil.util.Convert.twoBytesToChar(bc, bcIndex+1);
            byte type;
            jq_Method method;
            switch (bc[bcIndex]) {
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                    type = BytecodeVisitor.INVOKE_VIRTUAL;
                    // fallthrough
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                    type = BytecodeVisitor.INVOKE_SPECIAL;
                    // fallthrough
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                    method = clazz.getCPasInstanceMethod(cpi);
                    type = BytecodeVisitor.INVOKE_INTERFACE;
                    break;
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                    method = clazz.getCPasStaticMethod(cpi);
                    type = BytecodeVisitor.INVOKE_STATIC;
                    break;
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    method = joeq.Runtime.Arrays._multinewarray;
                    type = BytecodeVisitor.INVOKE_STATIC;
                    break;
                default:
                    return null;
            }
            return CallTargets.getTargets(clazz, method, type, (jq_Reference) klass, exact, true);
        }
        public CallTargets getCallTargets(java.util.Set receiverTypes, boolean exact) {
            jq_Class clazz = ((jq_Method) super.m).getDeclaringClass();
            byte[] bc = ((jq_Method) super.m).getBytecode();
            if (bc == null || bcIndex < 0 || bcIndex+2 >= bc.length) return null;
            char cpi = jwutil.util.Convert.twoBytesToChar(bc, bcIndex+1);
            byte type;
            jq_Method method;
            switch (bc[bcIndex]) {
                case (byte) jq_ClassFileConstants.jbc_INVOKEVIRTUAL:
                    type = BytecodeVisitor.INVOKE_VIRTUAL;
                    // fallthrough
                case (byte) jq_ClassFileConstants.jbc_INVOKESPECIAL:
                    type = BytecodeVisitor.INVOKE_SPECIAL;
                    // fallthrough
                case (byte) jq_ClassFileConstants.jbc_INVOKEINTERFACE:
                    method = clazz.getCPasInstanceMethod(cpi);
                    type = BytecodeVisitor.INVOKE_INTERFACE;
                    break;
                case (byte) jq_ClassFileConstants.jbc_INVOKESTATIC:
                    method = clazz.getCPasStaticMethod(cpi);
                    type = BytecodeVisitor.INVOKE_STATIC;
                    break;
                case (byte) jq_ClassFileConstants.jbc_MULTIANEWARRAY:
                    method = joeq.Runtime.Arrays._multinewarray;
                    type = BytecodeVisitor.INVOKE_STATIC;
                    break;
                default:
                    return null;
            }
            return CallTargets.getTargets(clazz, method, type, receiverTypes, exact, true);
        }
        */
        
        public void write(Textualizer t) throws IOException {
            t.writeString("bc "+bcIndex+" ");
            t.writeObject(m);
        }
        
    }
    
    public static class FakeProgramLocation extends ProgramLocation {
        String label;

        public FakeProgramLocation(jq_Method m, String label) {
            super(m);
            this.label = label;
        }

        public void write(Textualizer t) throws IOException {
            t.writeString("fake "+label.replace(' ', '_') + " ");
            t.writeObject(m);
        }

        public String toString() {
            String s = super.m.getDeclaringClass().getName()+"."+super.m.getName()+"() '"+label+"'";
            return s;
        }

        public int getID() { return -1; }
        public int getBytecodeIndex() { return -1; }
        public jq_Type getResultType() { return null; }
        public boolean isCall() { return false; }
        public jq_Method getTargetMethod() { return null; }
        public jq_Method resolveTargetMethod() { return null; }
        public jq_Type[] getParamTypes() { return null; }
        public jq_Type getReturnType() { return null; }
        public boolean isSingleTarget() { return false; }
        public boolean isInterfaceCall() { return false; }
        public byte getInvocationType() { return -1; }
    }
    
    public static class PlaceholderParameterProgramLocation extends ProgramLocation {
        String locationLabel;

        public PlaceholderParameterProgramLocation(jq_Method m, String locationLabel) {
            super(m);
            this.locationLabel = locationLabel;
        }
        
        public PlaceholderParameterProgramLocation(PlaceholderParameterProgramLocation that, String postfix) {
            this(that.getMethod(), that.getLocationLabel() + postfix);
        }

        public String getLocationLabel() {
            return locationLabel;
        }

        public void write(Textualizer t) throws IOException {
            t.writeString("placeholder "+locationLabel.replace(' ', '_') + " ");
            t.writeObject(m);
        }

        public String toString() {
            String s = locationLabel + " of " + super.m.getDeclaringClass().getName()+"."+super.m.getName()+"()";
            return s;
        }

        public int getID() { return -1; }
        public int getBytecodeIndex() { return -1; }
        public jq_Type getResultType() { return null; }
        public boolean isCall() { return false; }
        public jq_Method getTargetMethod() { return null; }
        public jq_Method resolveTargetMethod() { return null; }
        public jq_Type[] getParamTypes() { return null; }
        public jq_Type getReturnType() { return null; }
        public boolean isSingleTarget() { return false; }
        public boolean isInterfaceCall() { return false; }
        public byte getInvocationType() { return -1; }
        public String getEmacsName() {
            Utf8 source = getSourceFile();
            if (source == null) {
                return m + ":"+m.getLineNumber(0) + locationLabel;
            }else{
                return source+":"+m.getLineNumber(0) + locationLabel;    
            }
        }
    }

    public static ProgramLocation read(StringTokenizer st) {
        String s = st.nextToken();
        if (s.equals("null"))
            return null;
        if (s.equals("fake")) {
            String label = st.nextToken().replace('_', ' ');
            jq_Method m = (jq_Method)jq_FakeInstanceMethod.read(st);
            if (m == null) return null;
            return new FakeProgramLocation(m, label);
        }
        int id = Integer.parseInt(st.nextToken());
        jq_Method m = (jq_Method) jq_Method.read(st);
        if (m == null) return null;
        if (s.equals("bc")) {
            return new BCProgramLocation(m, id);
        }
        if (s.equals("quad")) {
            if (m.getBytecode() == null) return null;
            ControlFlowGraph cfg = CodeCache.getCode(m);
            for (QuadIterator i = new QuadIterator(cfg); i.hasNext(); ) {
                Quad q = i.nextQuad();
                if (q.getID() == id) return new QuadProgramLocation(m, q);
            }
        }
        return null;
    }
    
    public static ProgramLocation getLoadLocation(jq_Class klass, int lineNum) {
        if (true)
            return getBCProgramLocation(klass, lineNum, Bytecodes.LoadInstruction.class, 0);
        else {
            ProgramLocation pl;
            pl = getQuadProgramLocation(klass, lineNum, Operator.ALoad.ALOAD_A.class, 0);
            if (pl != null) return pl;
            pl = getQuadProgramLocation(klass, lineNum, Operator.ALoad.ALOAD_P.class, 0);
            if (pl != null) return pl;
            pl = getQuadProgramLocation(klass, lineNum, Operator.Getfield.GETFIELD_A.class, 0);
            if (pl != null) return pl;
            return getQuadProgramLocation(klass, lineNum, Operator.Getfield.GETFIELD_P.class, 0);
        }
    }
    
    public static ProgramLocation getAllocLocation(jq_Class klass, int lineNum) {
        if (true)
            return getBCProgramLocation(klass, lineNum, Bytecodes.AllocationInstruction.class, 0);
        else {
            ProgramLocation pl;
            pl = getQuadProgramLocation(klass, lineNum, Operator.New.class, 0);
            if (pl != null) return pl;
            return getQuadProgramLocation(klass, lineNum, Operator.NewArray.class, 0);
        }
    }
    
    public static ProgramLocation getConstLocation(jq_Class klass, int lineNum) {
        if (true) {
            ProgramLocation pl = getBCProgramLocation(klass, lineNum, Bytecodes.LDC.class, 0);
            if (pl != null) return pl;
            return getBCProgramLocation(klass, lineNum, Bytecodes.LDC2_W.class, 0);
        } else {
            return getQuadProgramLocation(klass, lineNum, Operator.Move.class, 0);
        }
    }
    
    public static ProgramLocation getInvokeLocation(jq_Class klass, int lineNum) {
        if (true)
            return getBCProgramLocation(klass, lineNum, Bytecodes.InvokeInstruction.class, 0);
        else {
            return getQuadProgramLocation(klass, lineNum, Operator.Invoke.class, 0);
        }
    }
    
    public static ProgramLocation getBCProgramLocation(jq_Class klass, int lineNum, Class instructionType, int k) {
        klass.load();
        jq_Method m = klass.getMethodContainingLine((char) lineNum);
        if (m == null) return null;
        jq_LineNumberBC[] ln = m.getLineNumberTable();
        if (ln == null) return null;
        int i = 0;
        for ( ; i<ln.length; ++i) {
            if (ln[i].getLineNum() == lineNum) break;
        }
        if (i == ln.length) return null;
        int loIndex = ln[i].getStartPC();
        int hiIndex = m.getBytecode().length;
        if (i < ln.length-1) hiIndex = ln[i+1].getStartPC();
        ByteSequence bs = new ByteSequence(m.getBytecode(), loIndex, hiIndex-loIndex);
        try {
            while (bs.available() > 0) {
                int off = bs.getIndex();
                Bytecodes.Instruction in = Bytecodes.Instruction.readInstruction(klass.getCP(), bs);
                if (instructionType.isInstance(in)) {
                    if (k == 0)
                        return new BCProgramLocation(m, off);
                    --k;
                }
            }
        } catch (IOException x) {
            Assert.UNREACHABLE();
        }
        return null;
    }
    
    public static ProgramLocation getQuadProgramLocation(jq_Class klass, int lineNum, Class instructionType, int k) {
        klass.load();
        jq_Method m = klass.getMethodContainingLine((char) lineNum);
        if (m == null) return null;
        jq_LineNumberBC[] ln = m.getLineNumberTable();
        if (ln == null) return null;
        int i = 0;
        for ( ; i<ln.length; ++i) {
            if (ln[i].getLineNum() == lineNum) break;
        }
        if (i == ln.length) return null;
        int loIndex = ln[i].getStartPC();
        int hiIndex = m.getBytecode().length;
        if (i < ln.length-1) hiIndex = ln[i+1].getStartPC();
        Map bc_map = CodeCache.getBCMap(m);
        for (Iterator j = bc_map.entrySet().iterator(); j.hasNext(); ) {
            Map.Entry e = (Map.Entry) j.next();
            Quad q = (Quad) e.getKey();
            if (!instructionType.isInstance(q.getOperator()))
                continue;
            int index = ((Integer) e.getValue()).intValue();
            if (index >= loIndex && index < hiIndex)
                return new QuadProgramLocation(m, q);
        }
        return null;
    }
}
