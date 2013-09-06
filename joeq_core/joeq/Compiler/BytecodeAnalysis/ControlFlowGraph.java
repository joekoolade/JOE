// ControlFlowGraph.java, created Fri Jan 11 16:49:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import joeq.Class.jq_Method;
import joeq.Class.jq_TryCatchBC;
import jwutil.math.BitString;
import jwutil.math.BitString.BitStringIterator;
import jwutil.util.Assert;

/**
 * Control flow graph for a bytecode stream.  The data structure is immutable
 * and corresponds exactly to the underlying bytecodes.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ControlFlowGraph.java,v 1.20 2005/05/28 11:14:27 joewhaley Exp $
 */
public class ControlFlowGraph {

    public static final boolean TRACE = false;
    
    /** Array of basic blocks, ordered by their appearance in the bytecode. */
    private final BasicBlock[] basic_blocks;
    /** Array of exception handler entrypoints, ordered by their appearance
     * in the bytecode. */
    private final BasicBlock[] handler_entries;
    /** Map from basic blocks to their JSR info.
     * There is JSR info associated with the entry and exit blocks of each
     * JSR subroutine. */
    private Map/*<BasicBlock, JSRInfo>*/ jsr_info;
    
    /** Creates new ControlFlowGraph */
    private ControlFlowGraph(int n_bb, int n_handlers) {
        basic_blocks = new BasicBlock[n_bb];
        handler_entries = new BasicBlock[n_handlers];
    }

    /** Returns the entry basic block. */
    public BasicBlock getEntry() { return basic_blocks[0]; }
    /** Returns the exit basic block. */
    public BasicBlock getExit() { return basic_blocks[1]; }
    /** Returns the number of basic blocks. */
    public int getNumberOfBasicBlocks() { return basic_blocks.length; }
    /** Returns the basic block with the given number. */
    public BasicBlock getBasicBlock(int index) { return basic_blocks[index]; }

    /** Add info about a JSR subroutine.  The JSR subroutine has the given
     * entry and exit blocks and modifies the given locals.  This info is
     * associated with the entry and exit blocks.
     * 
     * @param entry  entry block
     * @param exit  exit block
     * @param locals  which locals are modified
     */
    public void addJSRInfo(BasicBlock entry, BasicBlock exit, boolean[] locals) {
        if (jsr_info == null) jsr_info = new HashMap();
        JSRInfo nfo = new JSRInfo(entry, exit, locals);
        jsr_info.put(entry, nfo);
        jsr_info.put(exit, nfo);
    }
    
    /** Returns the JSR info about the JSR subroutine with the given entry/exit
     * block, or null if there are none.
     */
    public JSRInfo getJSRInfo(BasicBlock b) {
        return jsr_info != null ? (JSRInfo)jsr_info.get(b) : null;
    }
    
    /** Returns the basic block that contains the given bytecode index.
     */
    public BasicBlock getBasicBlockByBytecodeIndex(int index) {
        // binary search
        int lo, hi, mid;
        lo = 2; hi = basic_blocks.length-1;
        for (;;) {
            mid = (lo + hi) >> 1;
            if (lo > hi) break;
            int mid_index = basic_blocks[mid].start;
            if (index < mid_index) {
                hi = mid-1;
            } else {
                lo = mid+1;
            }
        }
        BasicBlock bb = basic_blocks[mid];
        Assert._assert(bb.start == index);
        return bb;
    }
    
    /** Returns the basic blocks in reverse post-order.
     */
    public RPOBasicBlockIterator reversePostOrderIterator() {
        return new RPOBasicBlockIterator(basic_blocks, basic_blocks[0]);
    }
    
    /** Returns the basic blocks in reverse post-order starting from the given
     * block.
     */
    public RPOBasicBlockIterator reversePostOrderIterator(BasicBlock start_bb) {
        return new RPOBasicBlockIterator(basic_blocks, start_bb);
    }
    
    public interface BasicBlockIterator extends ListIterator {
        BasicBlock nextBB();
        BasicBlock previousBB();
    }
    
    public static class RPOBasicBlockIterator implements BasicBlockIterator {
        private BasicBlock[] rpo;
        private int index;
        
        RPOBasicBlockIterator(BasicBlock[] bbs, BasicBlock start_bb) {
            index = bbs.length;
            rpo = new BasicBlock[index];
            boolean[] visited = new boolean[index];
            visit(visited, start_bb);
            --index; // so we can use preincrement in nextBB
        }
        
        private void visit(boolean[] visited, BasicBlock b) {
            int n = b.getNumberOfSuccessors();
            for (int i=0; i<n; ++i) {
                BasicBlock b2 = b.getSuccessor(i);
                if (!visited[b2.id]) {
                    visited[b2.id] = true;
                    visit(visited, b2);
                }
            }
            ExceptionHandlerIterator ehi = b.getExceptionHandlers();
            while (ehi.hasNext()) {
                ExceptionHandler eh = ehi.nextEH();
                BasicBlock b2 = eh.getEntry();
                if (!visited[b2.id]) {
                    visited[b2.id] = true;
                    visit(visited, b2);
                }
            }
            rpo[--index] = b;
        }
        
        public boolean hasNext() { return index < rpo.length-1; }
        public BasicBlock nextBB() { return rpo[++index]; }
        public Object next() { return nextBB(); }
        public int nextIndex() { return index+1; }
        public boolean hasPrevious() { return index >= 0 && rpo[index] != null; }
        public BasicBlock previousBB() { return rpo[index--]; }
        public Object previous() { return previousBB(); }
        public int previousIndex() { return index; }
        public void remove() { throw new UnsupportedOperationException(); }
        public void add(Object o) { throw new UnsupportedOperationException(); }
        public void set(Object o) { throw new UnsupportedOperationException(); }
        public void jumpToEnd() { index = rpo.length-1; }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (int i=0; i<rpo.length; ++i) {
                sb.append(rpo[i]);
                if (i == index) sb.append(" * ");
                else sb.append("   ");
            }
            return sb.toString();
        }
    }
    
    /** Compute and return the control flow graph for the given method. */
    public static ControlFlowGraph computeCFG(jq_Method method) {
        // get basic block boundaries and branch locations
        InitialPass pass = new InitialPass(method);
        pass.forwardTraversal();
        
        byte[] bc = method.getBytecode();
        
        BitString basic_block_start = pass.getBasicBlockStart();
        BitString branch_locations = pass.getBranchLocations();
        
        if (!basic_block_start.get(bc.length)) {
            // execution falls off end!  legal iff the code is unreachable.
            basic_block_start.set(bc.length);
        }
        
        // add try ranges and exception handlers to basic block boundaries
        jq_TryCatchBC[] exs = method.getExceptionTable();
        for (int i=0; i<exs.length; ++i) {
            jq_TryCatchBC ex = exs[i];
            basic_block_start.set(ex.getStartPC());
            basic_block_start.set(ex.getEndPC());
            basic_block_start.set(ex.getHandlerPC());
        }
        
        if (TRACE) System.out.println("Number of bb: "+basic_block_start.numberOfOnes());
        if (TRACE) System.out.println("Basic block start: "+basic_block_start);
        if (TRACE) System.out.println("Branch locations : "+branch_locations);
        
        int n_bb = basic_block_start.numberOfOnes();
        n_bb += 1; // for entry node.
        int[] n_pred = new int[n_bb];
        ControlFlowGraph cfg = new ControlFlowGraph(n_bb, exs.length);
        cfg.basic_blocks[0] = new BasicBlock(0, -1);
        cfg.basic_blocks[1] = new BasicBlock(1, -1);
        int bb_i = 2; BasicBlock bb = null;
        cfg.basic_blocks[2] = bb = new BasicBlock(2, 0);
        if (TRACE) System.out.println("Created "+bb+" at bytecode 0");
        BitStringIterator it = basic_block_start.iterator();
        Assert._assert(it.hasNext());
        for (;;) {
            Assert._assert(it.hasNext());
            int bc_i = it.nextIndex();
            cfg.basic_blocks[bb_i-1].end = bc_i-1;
            if (TRACE) System.out.println("Ending basic block #"+(bb_i-1)+" at bytecode "+(bc_i-1));
            if (bc_i == bc.length) break;
            if (TRACE) System.out.println("Creating basic block #"+bb_i+" at bytecode "+bc_i);
            bb = cfg.basic_blocks[bb_i] = new BasicBlock(bb_i, bc_i);
            ++bb_i;
        }
        Assert._assert(!it.hasNext());
        Assert._assert(bb_i == n_bb);
        cfg.basic_blocks[0].end = -1;
        cfg.basic_blocks[0].successors = new BasicBlock[1];
        cfg.basic_blocks[0].successors[0] = cfg.basic_blocks[2];
        cfg.basic_blocks[0].predecessors = cfg.basic_blocks[1].successors = new BasicBlock[0];
        //cfg.basic_blocks[1].end = -1; // already set in the loop above.
        bb.end = bc.length-1;
        
        n_pred[2] = 1;
        it = branch_locations.iterator();
        bb_i = 2;
        BranchVisitor bv = new BranchVisitor(method, cfg, n_pred);
        while (it.hasNext()) {
            int bc_i = it.nextIndex();
            bb = cfg.basic_blocks[bb_i];
            if (TRACE) System.out.println("Next branch: bc "+bc_i);
            while (bc_i > bb.end) {
                // fallthrough basic block.
                bb.successors = new BasicBlock[1];
                BasicBlock next_bb = bb.successors[0] = cfg.basic_blocks[bb_i+1];
                ++n_pred[++bb_i];
                if (TRACE) System.out.println("Fallthrough "+bb+" to "+next_bb);
                bb = next_bb;
            }
            // branch is at the end of this basic block.
            if (TRACE) System.out.println("Visiting branch at bc "+bc_i+" in "+bb);
            bv.bb = bb;
            bv.setLocation(bc_i);
            bv.visitBytecode();
            ++bb_i;
        }
        if (bb_i != n_bb) {
            // special case: code falls off end.
            Assert._assert(bb_i == n_bb-1);
            if (TRACE) System.out.println("Code falls off end!");
            cfg.basic_blocks[bb_i].successors = new BasicBlock[0];
        }
        // allocate predecessor arrays.
        for (bb_i = 1; bb_i < n_bb; ++bb_i) {
            bb = cfg.basic_blocks[bb_i];
            bb.predecessors = new BasicBlock[n_pred[bb_i]];
            if (TRACE) System.out.println(bb+" has "+n_pred[bb_i]+" predecessors");
            Assert._assert(bb.successors != null);
            n_pred[bb_i] = -1;
        }
        // fill in predecessor arrays.
        for (bb_i = 0; bb_i < n_bb; ++bb_i) {
            bb = cfg.basic_blocks[bb_i];
            for (int ct = 0; ct < bb.successors.length; ++ct) {
                BasicBlock bb2 = bb.successors[ct];
                bb2.predecessors[++n_pred[bb2.id]] = bb;
            }
        }
        // add exception handlers.
        for (int i=exs.length-1; i>=0; --i) {
            jq_TryCatchBC ex = exs[i];
            bb = cfg.getBasicBlockByBytecodeIndex(ex.getStartPC());
            if (bb.start >= ex.getEndPC())
                throw new VerifyError("Exception handler "+i+" "+ex+": start ("+(int)bb.start+") comes after end ("+(int)ex.getEndPC()+")");
            int numOfProtectedBlocks = (ex.getEndPC()==bc.length?n_bb:cfg.getBasicBlockByBytecodeIndex(ex.getEndPC()).id) - bb.id;
            BasicBlock handler_bb = cfg.getBasicBlockByBytecodeIndex(ex.getHandlerPC());
            ExceptionHandler eh = new ExceptionHandler(ex.getExceptionType(),
                                                       numOfProtectedBlocks,
                                                       handler_bb);
            ExceptionHandlerList ehs = new ExceptionHandlerList(eh, null);
            bb.addExceptionHandler_first(ehs);
            int start_id = bb.id;
            while (bb.getStart() < ex.getEndPC()) {
                eh.handledBlocks[bb.id - start_id] = bb;
                ehs = bb.addExceptionHandler(ehs);
                bb = cfg.basic_blocks[bb.id+1];
            }
        }
        // add ret edges, if necessary.
        if (pass.nJsrs > 0) {
            // LiveRefAnalysis can calculate ret edges for us.
            LiveRefAnalysis lra = new LiveRefAnalysis(method);
            lra.compute(cfg);
        }
        return cfg;
    }

    /** Visitor to perform the initial pass over the bytecode. */
    public static class InitialPass extends BytecodeVisitor {
        
        private BitString basic_block_start;
        private BitString branch_locations;
        private int nJsrs, nRets, nExits;

        InitialPass(jq_Method method) {
            super(method);
            this.basic_block_start = new BitString(bcs.length+1);
            this.branch_locations = new BitString(bcs.length);
            this.basic_block_start.set(0);
            //this.TRACE = true;
        }
        
        public String toString() {
            return "CFG1/"+method.getName();
        }
        
        public BitString getBasicBlockStart() {
            return this.basic_block_start;
        }
        public BitString getBranchLocations() {
            return this.branch_locations;
        }
        public int getNumberOfExits() { return nExits; }
        
        private void addBranch(int target) {
            basic_block_start.set(target);
            endBB();
        }
        private void endBB() {
            branch_locations.set(i_start);
            basic_block_start.set(i_end+1);
        }
        
        public void visitJSR(int target) {
            super.visitJSR(target);
            ++nJsrs; addBranch(target);
        }
        public void visitGOTO(int target) {
            super.visitGOTO(target);
            addBranch(target);
        }
        public void visitIRETURN() {
            super.visitIRETURN();
            ++nExits;
            endBB();
        }
        public void visitLRETURN() {
            super.visitLRETURN();
            ++nExits;
            endBB();
        }
        public void visitFRETURN() {
            super.visitFRETURN();
            ++nExits;
            endBB();
        }
        public void visitDRETURN() {
            super.visitDRETURN();
            ++nExits;
            endBB();
        }
        public void visitARETURN() {
            super.visitARETURN();
            ++nExits;
            endBB();
        }
        public void visitVRETURN() {
            super.visitVRETURN();
            ++nExits;
            endBB();
        }
        public void visitATHROW() {
            super.visitATHROW();
            ++nExits;
            endBB();
        }
        public void visitRET(int i) {
            super.visitRET(i);
            ++nRets;
            endBB();
        }
        public void visitIF(byte op, int target) {
            super.visitIF(op, target);
            addBranch(target);
        }
        public void visitIFREF(byte op, int target) {
            super.visitIFREF(op, target);
            addBranch(target);
        }
        public void visitIFCMP(byte op, int target) {
            super.visitIFCMP(op, target);
            addBranch(target);
        }
        public void visitIFREFCMP(byte op, int target) {
            super.visitIFREFCMP(op, target);
            addBranch(target);
        }
        public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
            super.visitTABLESWITCH(default_target, low, high, targets);
            for (int i=0; i<targets.length; ++i) {
                basic_block_start.set(targets[i]);
            }
            addBranch(default_target);
        }
        public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
            super.visitLOOKUPSWITCH(default_target, values, targets);
            for (int i=0; i<targets.length; ++i) {
                basic_block_start.set(targets[i]);
            }
            addBranch(default_target);
        }
    }

    /** Visitor to link up each of the branches in the code. */
    static class BranchVisitor extends BytecodeVisitor {
        
        private final ControlFlowGraph cfg;
        private final int[] n_pred;
        BasicBlock bb;
        
        BranchVisitor(jq_Method m, ControlFlowGraph cfg, int[] n_pred) {
            super(m);
            this.cfg = cfg; this.n_pred = n_pred;
            //this.TRACE = true;
        }
        
        void setLocation(int index) {
            i_start = index; i_end = index-1;
        }
        
        public void visitJSR(int target) {
            super.visitJSR(target);
            BasicBlock target_bb = cfg.getBasicBlockByBytecodeIndex(target);
            ++n_pred[target_bb.id];
            bb.successors = new BasicBlock[1];
            bb.successors[0] = target_bb;
        }
        public void visitRET(int i) {
            super.visitRET(i);
            // RETs are handled later.
            bb.successors = new BasicBlock[0];
        }
        public void visitGOTO(int target) {
            super.visitGOTO(target);
            BasicBlock target_bb = cfg.getBasicBlockByBytecodeIndex(target);
            ++n_pred[target_bb.id];
            bb.successors = new BasicBlock[1];
            bb.successors[0] = target_bb;
        }
        private void RETURNhelper() {
            BasicBlock target_bb = cfg.basic_blocks[1];
            ++n_pred[target_bb.id];
            bb.successors = new BasicBlock[1];
            bb.successors[0] = target_bb;
        }
        public void visitIRETURN() {
            super.visitIRETURN();
            RETURNhelper();
        }
        public void visitLRETURN() {
            super.visitLRETURN();
            RETURNhelper();
        }
        public void visitFRETURN() {
            super.visitFRETURN();
            RETURNhelper();
        }
        public void visitDRETURN() {
            super.visitDRETURN();
            RETURNhelper();
        }
        public void visitARETURN() {
            super.visitARETURN();
            RETURNhelper();
        }
        public void visitVRETURN() {
            super.visitVRETURN();
            RETURNhelper();
        }
        public void visitATHROW() {
            super.visitATHROW();
            RETURNhelper();
        }
        private void CONDBRANCHhelper(int target) {
            int bb_i = bb.id;
            BasicBlock next_bb = cfg.basic_blocks[bb_i+1]; // fallthrough edge
            ++n_pred[next_bb.id];
            BasicBlock target_bb = cfg.getBasicBlockByBytecodeIndex(target);
            ++n_pred[target_bb.id];
            bb.successors = new BasicBlock[2];
            bb.successors[0] = next_bb;
            bb.successors[1] = target_bb;
        }
        public void visitIF(byte op, int target) {
            super.visitIF(op, target);
            CONDBRANCHhelper(target);
        }
        public void visitIFREF(byte op, int target) {
            super.visitIFREF(op, target);
            CONDBRANCHhelper(target);
        }
        public void visitIFCMP(byte op, int target) {
            super.visitIFCMP(op, target);
            CONDBRANCHhelper(target);
        }
        public void visitIFREFCMP(byte op, int target) {
            super.visitIFREFCMP(op, target);
            CONDBRANCHhelper(target);
        }
        public void visitTABLESWITCH(int default_target, int low, int high, int[] targets) {
            super.visitTABLESWITCH(default_target, low, high, targets);
            BasicBlock target_bb = cfg.getBasicBlockByBytecodeIndex(default_target);
            ++n_pred[target_bb.id];
            bb.successors = new BasicBlock[targets.length+1];
            bb.successors[0] = target_bb;
            for (int i=0; i<targets.length; ++i) {
                target_bb = cfg.getBasicBlockByBytecodeIndex(targets[i]);
                ++n_pred[target_bb.id];
                bb.successors[i+1] = target_bb;
            }
        }
        public void visitLOOKUPSWITCH(int default_target, int[] values, int[] targets) {
            super.visitLOOKUPSWITCH(default_target, values, targets);
            BasicBlock target_bb = cfg.getBasicBlockByBytecodeIndex(default_target);
            ++n_pred[target_bb.id];
            bb.successors = new BasicBlock[targets.length+1];
            bb.successors[0] = target_bb;
            for (int i=0; i<targets.length; ++i) {
                target_bb = cfg.getBasicBlockByBytecodeIndex(targets[i]);
                ++n_pred[target_bb.id];
                bb.successors[i+1] = target_bb;
            }
        }
    }
    
}
