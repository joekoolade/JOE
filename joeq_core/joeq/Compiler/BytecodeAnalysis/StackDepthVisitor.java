// StackDepthVisitor.java, created Fri Jan 11 16:49:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.Stack;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Method;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import jwutil.util.Assert;

/**
 * A simple visitor that keeps track of the bytecode stack depth.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: StackDepthVisitor.java,v 1.11 2005/05/28 11:14:27 joewhaley Exp $
 */
public class StackDepthVisitor extends BytecodeVisitor {

    protected int currentStackDepth;
    protected final ControlFlowGraph cfg;
    
    /** Creates new StackDepthVisitor */
    public StackDepthVisitor(jq_Method method, ControlFlowGraph cfg) {
        super(method);
        this.cfg = cfg;
    }

    public void go() {
        // initialize all to -1
        int n = cfg.getNumberOfBasicBlocks();
        for (int i=0; i<n; ++i) {
            BasicBlock bb = cfg.getBasicBlock(i);
            bb.startingStackDepth = -1;
        }
        // start at the entry
        Stack w = new Stack();
        BasicBlock bb = cfg.getEntry();
        bb.startingStackDepth = 0;
        w.push(bb);
        while (!w.isEmpty()) {
            bb = (BasicBlock)w.pop();
            currentStackDepth = bb.startingStackDepth;
            visitBasicBlock(bb);
            for (int i=0; i<bb.getNumberOfSuccessors(); ++i) {
                BasicBlock bb2 = bb.getSuccessor(i);
                if (bb2.startingStackDepth == -1) {
                    bb2.startingStackDepth = currentStackDepth;
                    w.push(bb2);
                } else {
                    Assert._assert(bb2.startingStackDepth == currentStackDepth);
                }
            }
            ExceptionHandlerIterator ei = bb.getExceptionHandlers();
            while (ei.hasNext()) {
                ExceptionHandler e = ei.nextEH();
                BasicBlock bb2 = e.getEntry();
                if (bb2.startingStackDepth == -1) {
                    bb2.startingStackDepth = 1;
                    w.push(bb2);
                } else {
                    Assert._assert(bb2.startingStackDepth == 1);
                }
            }
        }
    }
    
    public void visitBasicBlock(BasicBlock bb) {
        if (TRACE) out.println("Visiting "+bb);
        for (i_end=bb.getStart()-1; ; ) {
            i_start = i_end+1;
            if (isEndOfBB(bb)) break;
            this.visitBytecode();
        }
    }
    
    private boolean isEndOfBB(BasicBlock bb) {
        return i_start > bb.getEnd();
    }
    
    public void visitACONST(Object s) {
        super.visitACONST(s);
        ++currentStackDepth;
    }
    public void visitICONST(int c) {
        super.visitICONST(c);
        ++currentStackDepth;
    }
    public void visitLCONST(long c) {
        super.visitLCONST(c);
        currentStackDepth+=2;
    }
    public void visitFCONST(float c) {
        super.visitFCONST(c);
        ++currentStackDepth;
    }
    public void visitDCONST(double c) {
        super.visitDCONST(c);
        currentStackDepth+=2;
    }
    public void visitILOAD(int i) {
        super.visitILOAD(i);
        ++currentStackDepth;
    }
    public void visitLLOAD(int i) {
        super.visitLLOAD(i);
        currentStackDepth+=2;
    }
    public void visitFLOAD(int i) {
        super.visitFLOAD(i);
        ++currentStackDepth;
    }
    public void visitDLOAD(int i) {
        super.visitDLOAD(i);
        currentStackDepth+=2;
    }
    public void visitALOAD(int i) {
        super.visitALOAD(i);
        ++currentStackDepth;
    }
    public void visitISTORE(int i) {
        super.visitISTORE(i);
        --currentStackDepth;
    }
    public void visitLSTORE(int i) {
        super.visitLSTORE(i);
        currentStackDepth-=2;
    }
    public void visitFSTORE(int i) {
        super.visitFSTORE(i);
        --currentStackDepth;
    }
    public void visitDSTORE(int i) {
        super.visitDSTORE(i);
        currentStackDepth-=2;
    }
    public void visitASTORE(int i) {
        super.visitASTORE(i);
        --currentStackDepth;
    }
    public void visitIALOAD() {
        super.visitIALOAD();
        --currentStackDepth;
    }
    public void visitFALOAD() {
        super.visitFALOAD();
        --currentStackDepth;
    }
    public void visitAALOAD() {
        super.visitAALOAD();
        --currentStackDepth;
    }
    public void visitBALOAD() {
        super.visitBALOAD();
        --currentStackDepth;
    }
    public void visitCALOAD() {
        super.visitCALOAD();
        --currentStackDepth;
    }
    public void visitSALOAD() {
        super.visitSALOAD();
        --currentStackDepth;
    }
    public void visitIASTORE() {
        super.visitIASTORE();
        currentStackDepth-=3;
    }
    public void visitLASTORE() {
        super.visitLASTORE();
        currentStackDepth-=4;
    }
    public void visitFASTORE() {
        super.visitFASTORE();
        currentStackDepth-=3;
    }
    public void visitDASTORE() {
        super.visitDASTORE();
        currentStackDepth-=4;
    }
    public void visitAASTORE() {
        super.visitAASTORE();
        currentStackDepth-=3;
    }
    public void visitBASTORE() {
        super.visitBASTORE();
        currentStackDepth-=3;
    }
    public void visitCASTORE() {
        super.visitCASTORE();
        currentStackDepth-=3;
    }
    public void visitSASTORE() {
        super.visitSASTORE();
        currentStackDepth-=3;
    }
    public void visitPOP() {
        super.visitPOP();
        --currentStackDepth;
    }
    public void visitPOP2() {
        super.visitPOP2();
        currentStackDepth-=2;
    }
    public void visitDUP() {
        super.visitDUP();
        ++currentStackDepth;
    }
    public void visitDUP_x1() {
        super.visitDUP_x1();
        ++currentStackDepth;
    }
    public void visitDUP_x2() {
        super.visitDUP_x2();
        ++currentStackDepth;
    }
    public void visitDUP2() {
        super.visitDUP2();
        currentStackDepth+=2;
    }
    public void visitDUP2_x1() {
        super.visitDUP2_x1();
        currentStackDepth+=2;
    }
    public void visitDUP2_x2() {
        super.visitDUP2_x2();
        currentStackDepth+=2;
    }
    public void visitIBINOP(byte op) {
        super.visitIBINOP(op);
        --currentStackDepth;
    }
    public void visitLBINOP(byte op) {
        super.visitLBINOP(op);
        currentStackDepth-=2;
    }
    public void visitFBINOP(byte op) {
        super.visitFBINOP(op);
        --currentStackDepth;
    }
    public void visitDBINOP(byte op) {
        super.visitDBINOP(op);
        currentStackDepth-=2;
    }
    public void visitISHIFT(byte op) {
        super.visitISHIFT(op);
        --currentStackDepth;
    }
    public void visitLSHIFT(byte op) {
        super.visitLSHIFT(op);
        --currentStackDepth;
    }
    public void visitI2L() {
        super.visitI2L();
        ++currentStackDepth;
    }
    public void visitI2D() {
        super.visitI2D();
        ++currentStackDepth;
    }
    public void visitL2I() {
        super.visitL2I();
        --currentStackDepth;
    }
    public void visitL2F() {
        super.visitL2F();
        --currentStackDepth;
    }
    public void visitF2L() {
        super.visitF2L();
        ++currentStackDepth;
    }
    public void visitF2D() {
        super.visitF2D();
        ++currentStackDepth;
    }
    public void visitD2I() {
        super.visitD2I();
        --currentStackDepth;
    }
    public void visitD2F() {
        super.visitD2F();
        --currentStackDepth;
    }
    public void visitLCMP2() {
        super.visitLCMP2();
        currentStackDepth-=3;
    }
    public void visitFCMP2(byte op) {
        super.visitFCMP2(op);
        --currentStackDepth;
    }
    public void visitDCMP2(byte op) {
        super.visitDCMP2(op);
        currentStackDepth-=3;
    }
    public void visitIF(byte op, int target) {
        super.visitIF(op, target);
        --currentStackDepth;
    }
    public void visitIFREF(byte op, int target) {
        super.visitIFREF(op, target);
        --currentStackDepth;
    }
    public void visitIFCMP(byte op, int target) {
        super.visitIFCMP(op, target);
        currentStackDepth-=2;
    }
    public void visitIFREFCMP(byte op, int target) {
        super.visitIFREFCMP(op, target);
        currentStackDepth-=2;
    }
    public void visitJSR(int target) {
        super.visitJSR(target);
        ++currentStackDepth;
    }
    public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
        super.visitTABLESWITCH(default_target, low, high, targets);
        --currentStackDepth;
    }
    public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
        super.visitLOOKUPSWITCH(default_target, values, targets);
        --currentStackDepth;
    }
    public void visitIRETURN() {
        super.visitIRETURN();
        currentStackDepth=0;
    }
    public void visitLRETURN() {
        super.visitLRETURN();
        currentStackDepth=0;
    }
    public void visitFRETURN() {
        super.visitFRETURN();
        currentStackDepth=0;
    }
    public void visitDRETURN() {
        super.visitDRETURN();
        currentStackDepth=0;
    }
    public void visitARETURN() {
        super.visitARETURN();
        currentStackDepth=0;
    }
    public void visitVRETURN() {
        super.visitVRETURN();
        currentStackDepth=0;
    }
    public void visitIGETSTATIC(jq_StaticField f) {
        super.visitIGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitLGETSTATIC(jq_StaticField f) {
        super.visitLGETSTATIC(f);
        currentStackDepth+=2;
    }
    public void visitFGETSTATIC(jq_StaticField f) {
        super.visitFGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitDGETSTATIC(jq_StaticField f) {
        super.visitDGETSTATIC(f);
        currentStackDepth+=2;
    }
    public void visitAGETSTATIC(jq_StaticField f) {
        super.visitAGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitZGETSTATIC(jq_StaticField f) {
        super.visitZGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitBGETSTATIC(jq_StaticField f) {
        super.visitBGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitCGETSTATIC(jq_StaticField f) {
        super.visitCGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitSGETSTATIC(jq_StaticField f) {
        super.visitSGETSTATIC(f);
        ++currentStackDepth;
    }
    public void visitIPUTSTATIC(jq_StaticField f) {
        super.visitIPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitLPUTSTATIC(jq_StaticField f) {
        super.visitLPUTSTATIC(f);
        currentStackDepth-=2;
    }
    public void visitFPUTSTATIC(jq_StaticField f) {
        super.visitFPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitDPUTSTATIC(jq_StaticField f) {
        super.visitDPUTSTATIC(f);
        currentStackDepth-=2;
    }
    public void visitAPUTSTATIC(jq_StaticField f) {
        super.visitAPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitZPUTSTATIC(jq_StaticField f) {
        super.visitZPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitBPUTSTATIC(jq_StaticField f) {
        super.visitBPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitCPUTSTATIC(jq_StaticField f) {
        super.visitCPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitSPUTSTATIC(jq_StaticField f) {
        super.visitSPUTSTATIC(f);
        --currentStackDepth;
    }
    public void visitLGETFIELD(jq_InstanceField f) {
        super.visitLGETFIELD(f);
        ++currentStackDepth;
    }
    public void visitDGETFIELD(jq_InstanceField f) {
        super.visitDGETFIELD(f);
        ++currentStackDepth;
    }
    public void visitIPUTFIELD(jq_InstanceField f) {
        super.visitIPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitLPUTFIELD(jq_InstanceField f) {
        super.visitLPUTFIELD(f);
        currentStackDepth-=3;
    }
    public void visitFPUTFIELD(jq_InstanceField f) {
        super.visitFPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitDPUTFIELD(jq_InstanceField f) {
        super.visitDPUTFIELD(f);
        currentStackDepth-=3;
    }
    public void visitAPUTFIELD(jq_InstanceField f) {
        super.visitAPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitBPUTFIELD(jq_InstanceField f) {
        super.visitBPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitCPUTFIELD(jq_InstanceField f) {
        super.visitCPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitSPUTFIELD(jq_InstanceField f) {
        super.visitSPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitZPUTFIELD(jq_InstanceField f) {
        super.visitZPUTFIELD(f);
        currentStackDepth-=2;
    }
    public void visitIINVOKE(byte op, jq_Method f) {
        super.visitIINVOKE(op, f);
        currentStackDepth-=f.getParamWords()-1;
    }
    public void visitLINVOKE(byte op, jq_Method f) {
        super.visitLINVOKE(op, f);
        currentStackDepth-=f.getParamWords()-2;
    }
    public void visitFINVOKE(byte op, jq_Method f) {
        super.visitFINVOKE(op, f);
        currentStackDepth-=f.getParamWords()-1;
    }
    public void visitDINVOKE(byte op, jq_Method f) {
        super.visitDINVOKE(op, f);
        currentStackDepth-=f.getParamWords()-2;
    }
    public void visitAINVOKE(byte op, jq_Method f) {
        super.visitAINVOKE(op, f);
        currentStackDepth-=f.getParamWords()-1;
    }
    public void visitVINVOKE(byte op, jq_Method f) {
        super.visitVINVOKE(op, f);
        currentStackDepth-=f.getParamWords();
    }
    public void visitNEW(jq_Type f) {
        super.visitNEW(f);
        ++currentStackDepth;
    }
    public void visitATHROW() {
        super.visitATHROW();
        currentStackDepth=0;
    }
    public void visitMONITOR(byte op) {
        super.visitMONITOR(op);
        --currentStackDepth;
    }
    public void visitMULTINEWARRAY(jq_Type f, char dim) {
        super.visitMULTINEWARRAY(f, dim);
        currentStackDepth-=dim-1;
    }

}
