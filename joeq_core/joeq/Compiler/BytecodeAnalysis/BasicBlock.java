// BasicBlock.java, created Fri Jan 11 16:49:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import jwutil.util.Assert;

/**
 * A basic block in terms of bytecode indices.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BasicBlock.java,v 1.10 2005/05/28 11:14:27 joewhaley Exp $
 */
public class BasicBlock {

    /**
     * ID number.
     */
    public final int id;
    
    /**
     * Start index of basic block.
     */
    final int start;
    
    /**
     * End index of basic block.
     */
    int end;
    
    /**
     * Predecessors of this basic block.
     */
    BasicBlock[] predecessors;
    
    /**
     * Successors of this basic block.
     */
    BasicBlock[] successors;
    
    /**
     * Set of exception handlers for this basic block.
     */
    ExceptionHandlerList exception_handler_set;

    /**
     * Starting stack depth of this basic block.
     */
    int startingStackDepth;
    
    /**
     * Whether this basic block ends in a ret.
     */
    boolean isSubroutineRet;
    
    /**
     * Construct a new basic block.  Only to be called by ControlFlowGraph.
     * 
     * @param id
     * @param start
     */
    BasicBlock(int id, int start) {
        this.id = id; this.start = start;
    }
    
    public int getStart() { return start; }
    public int getEnd() { return end; }
    
    public int getNumberOfPredecessors() { return predecessors.length; }
    public int getNumberOfSuccessors() { return successors.length; }
    public BasicBlock getPredecessor(int i) { return predecessors[i]; }
    public BasicBlock getSuccessor(int i) { return successors[i]; }
    public boolean isSubroutineRet() { return isSubroutineRet; }
    void setSubroutineRet(ControlFlowGraph cfg, BasicBlock jsub_bb) {
        isSubroutineRet = true;
        Assert._assert(this.successors.length == 0);
        this.successors = new BasicBlock[jsub_bb.predecessors.length];
        for (int i=0; i<this.successors.length; ++i) {
            int ret_target_index = jsub_bb.predecessors[i].id + 1;
            Assert._assert(ret_target_index < cfg.getNumberOfBasicBlocks());
            BasicBlock ret_target = cfg.getBasicBlock(ret_target_index);
            this.successors[i] = ret_target;
            BasicBlock[] new_pred = new BasicBlock[ret_target.predecessors.length+1];
            if (ret_target.predecessors.length != 0) {
                System.arraycopy(ret_target.predecessors, 0, new_pred, 0, ret_target.predecessors.length);
            }
            new_pred[ret_target.predecessors.length] = this;
            ret_target.predecessors = new_pred;
        }
    }
    
    public ExceptionHandlerIterator getExceptionHandlers() {
        if (exception_handler_set == null) return ExceptionHandlerIterator.nullIterator();
        return exception_handler_set.iterator();
    }
    
    void addExceptionHandler_first(ExceptionHandlerList eh) {
        Assert._assert(eh.parent == null);
        eh.parent = this.exception_handler_set;
        this.exception_handler_set = eh;
    }
    ExceptionHandlerList addExceptionHandler(ExceptionHandlerList eh) {
        if (eh.parent == this.exception_handler_set)
            return this.exception_handler_set = eh;
        else
            return this.exception_handler_set = new ExceptionHandlerList(eh.getHandler(), this.exception_handler_set);
    }

    public String toString() { return "BB"+id+" ("+start+"-"+end+")"; }
}
