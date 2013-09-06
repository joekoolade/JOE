// ModRefAnalysis.java, created Thu Feb 14 18:09:11 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Class.jq_StaticField;
import jwutil.strings.Strings;

/**
 * Simple mod/ref analysis.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ModRefAnalysis.java,v 1.14 2005/05/28 11:14:27 joewhaley Exp $
 */
public class ModRefAnalysis extends BytecodeVisitor {

    public static final boolean INTRA_CLASS = true;
    public static final boolean ALWAYS_TRACE = false;

    public static class ModRefVisitor extends jq_MethodVisitor.EmptyVisitor {
        public void visitMethod(jq_Method m) {
            m.getDeclaringClass().load();
            if (m.getBytecode() == null) return;
            ModRefAnalysis s = new ModRefAnalysis(m);
            results.put(m, s);
            s.forwardTraversal();
        }
    }
    
    public static Map results = new HashMap();

    protected Set mod = new HashSet();
    protected Set ref = new HashSet();
    
    /** Creates new ModRefAnalysis */
    public ModRefAnalysis(jq_Method m) {
        super(m);
        this.TRACE = ALWAYS_TRACE;
    }

    public Set getMod() { return mod; }
    public Set getRef() { return ref; }
    
    public void visitIGETSTATIC(jq_StaticField f) {
        super.visitIGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitLGETSTATIC(jq_StaticField f) {
        super.visitLGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitFGETSTATIC(jq_StaticField f) {
        super.visitFGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitDGETSTATIC(jq_StaticField f) {
        super.visitDGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitAGETSTATIC(jq_StaticField f) {
        super.visitAGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitZGETSTATIC(jq_StaticField f) {
        super.visitZGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitBGETSTATIC(jq_StaticField f) {
        super.visitBGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitCGETSTATIC(jq_StaticField f) {
        super.visitCGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitSGETSTATIC(jq_StaticField f) {
        super.visitSGETSTATIC(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitIPUTSTATIC(jq_StaticField f) {
        super.visitIPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitLPUTSTATIC(jq_StaticField f) {
        super.visitLPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitFPUTSTATIC(jq_StaticField f) {
        super.visitFPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitDPUTSTATIC(jq_StaticField f) {
        super.visitDPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitAPUTSTATIC(jq_StaticField f) {
        super.visitAPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitZPUTSTATIC(jq_StaticField f) {
        super.visitZPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitBPUTSTATIC(jq_StaticField f) {
        super.visitBPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitCPUTSTATIC(jq_StaticField f) {
        super.visitCPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitSPUTSTATIC(jq_StaticField f) {
        super.visitSPUTSTATIC(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitIGETFIELD(jq_InstanceField f) {
        super.visitIGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitLGETFIELD(jq_InstanceField f) {
        super.visitLGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitFGETFIELD(jq_InstanceField f) {
        super.visitFGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitDGETFIELD(jq_InstanceField f) {
        super.visitDGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitAGETFIELD(jq_InstanceField f) {
        super.visitAGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitBGETFIELD(jq_InstanceField f) {
        super.visitBGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitCGETFIELD(jq_InstanceField f) {
        super.visitCGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitSGETFIELD(jq_InstanceField f) {
        super.visitSGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitZGETFIELD(jq_InstanceField f) {
        super.visitZGETFIELD(f);
        f = tryResolve(f);
        ref.add(f);
    }
    public void visitIPUTFIELD(jq_InstanceField f) {
        super.visitIPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitLPUTFIELD(jq_InstanceField f) {
        super.visitLPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitFPUTFIELD(jq_InstanceField f) {
        super.visitFPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitDPUTFIELD(jq_InstanceField f) {
        super.visitDPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitAPUTFIELD(jq_InstanceField f) {
        super.visitAPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitBPUTFIELD(jq_InstanceField f) {
        super.visitBPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitCPUTFIELD(jq_InstanceField f) {
        super.visitCPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitSPUTFIELD(jq_InstanceField f) {
        super.visitSPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    public void visitZPUTFIELD(jq_InstanceField f) {
        super.visitZPUTFIELD(f);
        f = tryResolve(f);
        mod.add(f);
    }
    protected void handleInvoke(jq_Method target) {
        ModRefAnalysis s = (ModRefAnalysis)results.get(target);
        if (s == null) {
            if (INTRA_CLASS) {
                if (target.getDeclaringClass() != this.method.getDeclaringClass())
                    return;
            }
            target.getDeclaringClass().load();
            if (target.getBytecode() == null) return;
            s = new ModRefAnalysis(target);
            results.put(target, s);
            s.forwardTraversal();
        }
        mod.addAll(s.mod);
        ref.addAll(s.ref);
    }
    protected void invokeHelper(byte op, jq_Method f) {
        f = (jq_Method) tryResolve(f);
        Iterator i = CallTargets.getTargets(this.method.getDeclaringClass(), f, op, true).iterator();
        while (i.hasNext()) {
            jq_Method m = (jq_Method)i.next();
            handleInvoke(m);
        }
    }
    public void visitIINVOKE(byte op, jq_Method f) {
        super.visitIINVOKE(op, f);
        invokeHelper(op, f);
    }
    public void visitLINVOKE(byte op, jq_Method f) {
        super.visitLINVOKE(op, f);
        invokeHelper(op, f);
    }
    public void visitFINVOKE(byte op, jq_Method f) {
        super.visitFINVOKE(op, f);
        invokeHelper(op, f);
    }
    public void visitDINVOKE(byte op, jq_Method f) {
        super.visitDINVOKE(op, f);
        invokeHelper(op, f);
    }
    public void visitAINVOKE(byte op, jq_Method f) {
        super.visitAINVOKE(op, f);
        invokeHelper(op, f);
    }
    public void visitVINVOKE(byte op, jq_Method f) {
        super.visitVINVOKE(op, f);
        invokeHelper(op, f);
    }
    
    public String toString() {
        return "Mod: "+mod+Strings.lineSep+"Ref: "+ref;
    }
}
