// CalculateSize.java, created Mar 21, 2004 1:26:40 AM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Iterator;
import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;
import joeq.Runtime.Reflection;

/**
 * CalculateSize
 * 
 * @author jwhaley
 * @version $Id: CalculateSize.java,v 1.4 2004/09/22 22:17:26 joewhaley Exp $
 */
public class CalculateSize extends QuadVisitor.EmptyVisitor implements ControlFlowGraphVisitor, BasicBlockVisitor {

    static final int ARRAYLIST_OVERHEAD = 7 * 4;
    static final int HASHMAP_OVERHEAD = 7 * 4;
    
    int cfgSize;
    int quadSize;
    int rfSize;
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.ControlFlowGraphVisitor#visitCFG(joeq.Compiler.Quad.ControlFlowGraph)
     */
    public void visitCFG(ControlFlowGraph cfg) {
        jq_Class c = (jq_Class) Reflection.getTypeOf(cfg);
        c.prepare();
        cfgSize += c.getInstanceSize();
        List ehs = cfg.getExceptionHandlers();
        cfgSize += ARRAYLIST_OVERHEAD;
        cfgSize += 4 * ehs.size();
        for (Iterator i = ehs.iterator(); i.hasNext(); ) {
            ExceptionHandler eh = (ExceptionHandler) i.next();
            c = (jq_Class) Reflection.getTypeOf(eh);
            c.prepare();
            cfgSize += c.getInstanceSize();
            List bbs = eh.getHandledBasicBlocks();
            cfgSize += ARRAYLIST_OVERHEAD + 4*bbs.size();
        }
        if (cfg.jsr_map != null) {
            cfgSize += HASHMAP_OVERHEAD;
            cfgSize += cfg.jsr_map.size() * 24; // 24 for each Entry
            cfgSize += cfg.jsr_map.size() * 100; // for Entry[] table;
        }
        RegisterFactory rf = cfg.getRegisterFactory();
        c = (jq_Class) Reflection.getTypeOf(rf);
        c.prepare();
        rfSize += c.getInstanceSize();
        rfSize += ARRAYLIST_OVERHEAD;
        rfSize += 4 * rf.size();
        rfSize += HASHMAP_OVERHEAD;
        rfSize += rf.numberOfLocalRegisters() * 24; // 24 for each Entry
        rfSize += rf.numberOfLocalRegisters() * 100; // for Entry[] table;
        rfSize += HASHMAP_OVERHEAD;
        rfSize += rf.numberOfStackRegisters() * 24; // 24 for each Entry
        rfSize += rf.numberOfStackRegisters() * 100; // for Entry[] table;
        cfg.visitBasicBlocks(this);
        System.out.println(cfg.getMethod()+" "+toString());
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.BasicBlockVisitor#visitBasicBlock(joeq.Compiler.Quad.BasicBlock)
     */
    public void visitBasicBlock(BasicBlock bb) {
        jq_Class c = (jq_Class) Reflection.getTypeOf(bb);
        c.prepare();
        cfgSize += c.getInstanceSize();
        cfgSize += ARRAYLIST_OVERHEAD;
        cfgSize += 4 * bb.getNumberOfPredecessors();
        cfgSize += ARRAYLIST_OVERHEAD;
        cfgSize += 4 * bb.getNumberOfSuccessors();
        cfgSize += ARRAYLIST_OVERHEAD;
        cfgSize += 4 * bb.size();
        // todo: sharing of exception handler lists?
        ExceptionHandlerList ehl = bb.getExceptionHandlers();
        while (ehl != null) {
            c = (jq_Class) Reflection.getTypeOf(ehl);
            c.prepare();
            cfgSize += c.getInstanceSize();
            ExceptionHandler eh = ehl.getHandler();
            if (eh != null) {
                c = (jq_Class) Reflection.getTypeOf(eh);
                c.prepare();
                cfgSize += c.getInstanceSize();
                cfgSize += ARRAYLIST_OVERHEAD;
                cfgSize += 4 * eh.getHandledBasicBlocks().size();
            }
            ehl = ehl.getParent();
        }
        bb.visitQuads(this);
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.QuadVisitor#visitQuad(joeq.Compiler.Quad.Quad)
     */
    public void visitQuad(Quad obj) {
        super.visitQuad(obj);
        jq_Class c = (jq_Class) Reflection.getTypeOf(obj);
        c.prepare();
        quadSize += c.getInstanceSize();
        for (Iterator i = obj.getAllOperands().iterator(); i.hasNext(); ) {
            Operand o = (Operand) i.next();
            c = (jq_Class) Reflection.getTypeOf(o);
            c.prepare();
            quadSize += c.getInstanceSize();
        }
    }

    public String toString() {
        return cfgSize+" + "+quadSize+" + "+rfSize+" = "+(cfgSize+quadSize+rfSize);
    }
    
    public static void main(String[] args) {
        CalculateSize cs = new CalculateSize();
        for (int i = 0; i < args.length; ++i) {
            jq_Class c = (jq_Class) Helper.load(args[0]);
            Helper.runPass(c, (ControlFlowGraphVisitor)cs);
        }
        System.out.println(cs.toString());
    }
    
}
