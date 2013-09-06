// BasicBlock.java, created Fri Jan 11 16:42:38 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Collection;
import java.util.Iterator;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.Ret;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;
import joeq.Util.Templates.ListWrapper;
import joeq.Util.Templates.UnmodifiableList;
import jwutil.collections.BackwardIterator;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * Represents a basic block in the quad intermediate representation.
 * Basic blocks are single-entry regions, but not necessarily single-exit regions
 * due to the fact that control flow may exit a basic block early due to a
 * run time exception.  That is to say, a potential exception point does not
 * end a basic block.
 *
 * Each basic block contains a list of quads, a list of predecessors, a list of
 * successors, and a list of exception handlers.  It also has an id number that
 * is unique within its control flow graph, and some flags.
 *
 * You should never create a basic block directly.  You should create one via a
 * ControlFlowGraph so that the id number is correct.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @see  Quad
 * @see  ControlFlowGraph
 * @see  ExceptionHandlerList
 * @version  $Id: BasicBlock.java,v 1.23 2006/02/25 05:49:42 livshits Exp $
 */

public class BasicBlock {

    /** Unique id number for this basic block. */
    private int id_number;
    /** List of instructions. */
    private final java.util.List/*<Quad>*/ instructions;
    /** List of successor basic blocks. */
    private final java.util.List/*<BasicBlock>*/ successors;
    /** List of predecessor basic blocks. */
    private final java.util.List/*<BasicBlock>*/ predecessors;
    
    /** List of exception handlers for this basic block. */
    private ExceptionHandlerList exception_handler_list;
    
    /** Flags for this basic block. */
    private int flags;
    
    /** Exception handler entry point. */
    private static final int EXCEPTION_HANDLER_ENTRY = 0x1;
    /** JSR subroutine entry point. */
    private static final int JSR_ENTRY = 0x2;
    /** This basic block ends in a 'ret'. */
    private static final int ENDS_IN_RET = 0x4;
    
    /** Creates new entry node. Only to be called by ControlFlowGraph. */
    static BasicBlock createStartNode() {
        return new BasicBlock();
    }
    /** Private constructor for the entry node. */
    private BasicBlock() {
        this.id_number = 0;
        this.instructions = null;
        this.predecessors = null;
        this.successors = new java.util.ArrayList(1);
        this.exception_handler_list = null;
    }
    /** Creates new exit node */
    static BasicBlock createEndNode(int numOfPredecessors) {
        return new BasicBlock(numOfPredecessors);
    }
    /** Private constructor for the exit node. */
    private BasicBlock(int numOfExits) {
        this.id_number = 1;
        this.instructions = null;
        this.successors = null;
        this.predecessors = new java.util.ArrayList(numOfExits);
        this.exception_handler_list = null;
    }
    /** Create new basic block with no exception handlers.
     * Only to be called by ControlFlowGraph. */
    static BasicBlock createBasicBlock(int id, int numOfPredecessors, int numOfSuccessors, int numOfInstructions) {
        return new BasicBlock(id, numOfPredecessors, numOfSuccessors, numOfInstructions, null);
    }
    /** Create new basic block with the given exception handlers.
     * Only to be called by ControlFlowGraph. */
    static BasicBlock createBasicBlock(int id, int numOfPredecessors, int numOfSuccessors, int numOfInstructions, ExceptionHandlerList ehs) {
        return new BasicBlock(id, numOfPredecessors, numOfSuccessors, numOfInstructions, ehs);
    }
    /** Private constructor for internal nodes. */
    private BasicBlock(int id, int numOfPredecessors, int numOfSuccessors, int numOfInstructions,
                       ExceptionHandlerList ehs) {
        this.id_number = id;
        this.predecessors = new java.util.ArrayList(numOfPredecessors);
        this.successors = new java.util.ArrayList(numOfSuccessors);
        this.instructions = new java.util.ArrayList(numOfInstructions);
        this.exception_handler_list = ehs;
    }

    /** Returns true if this is the entry basic block.
     * @return  true if this is the entry basic block. */
    public boolean isEntry() { return predecessors == null; }
    /** Returns true if this is the exit basic block.
     * @return  true if this is the exit basic block. */
    public boolean isExit() { return successors == null; }
    
    /** Returns an iterator over the quads in this basic block in forward order.
     * @return  an iterator over the quads in this basic block in forward order. */
    public ListIterator.Quad iterator() {
        if (instructions == null) return ListWrapper.Quad.EmptyIterator.INSTANCE;
        return new ListWrapper.Quad.Iterator(instructions.listIterator());
    }
    
    /** Returns an iterator over the quads in this basic block in backward order.
     * @return  an iterator over the quads in this basic block in backward order. */
    public ListIterator.Quad backwardIterator() {
        if (instructions == null) return ListWrapper.Quad.EmptyIterator.INSTANCE;
        return new ListWrapper.Quad.Iterator(new BackwardIterator(instructions.listIterator()));
    }

    /** Visit all of the quads in this basic block in forward order
     * with the given quad visitor.
     * @see  QuadVisitor
     * @param qv  QuadVisitor to visit the quads with. */
    public void visitQuads(QuadVisitor qv) {
        for (ListIterator.Quad i = iterator(); i.hasNext(); ) {
            Quad q = i.nextQuad();
            q.accept(qv);
        }
    }
    
    /** Visit all of the quads in this basic block in backward order
     * with the given quad visitor.
     * @see  QuadVisitor
     * @param qv  QuadVisitor to visit the quads with. */
    public void backwardVisitQuads(QuadVisitor qv) {
        for (ListIterator.Quad i = backwardIterator(); i.hasNext(); ) {
            Quad q = i.nextQuad();
            q.accept(qv);
        }
    }
    
    /** Returns the number of quads in this basic block.
     * @return  the number of quads in this basic block. */
    public int size() {
        if (instructions == null) return 0; // entry or exit block
        return instructions.size();
    }
    
    public Quad getQuad(int i) {
        return (Quad)instructions.get(i);
    }
    
    public Quad getLastQuad() {
        if (size() == 0) return null;
        return (Quad)instructions.get(instructions.size()-1);
    }

    public int getQuadIndex(Quad q) {
        return instructions == null ? -1 : instructions.indexOf(q);
    }
    
    public Quad removeQuad(int i) {
        return (Quad)instructions.remove(i);
    }
    
    public boolean removeQuad(Quad q) {
        return instructions.remove(q);
    }
    
    public void removeAllQuads() {
        instructions.clear();
    }
    
    /** Add a quad to this basic block at the given location.
     * Cannot add quads to the entry or exit basic blocks.
     * @param index  the index to add the quad
     * @param q  quad to add */
    public void addQuad(int index, Quad q) {
        Assert._assert(instructions != null, "Cannot add instructions to entry/exit basic block");
        instructions.add(index, q);
    }
    /** Append a quad to the end of this basic block.
     * Cannot add quads to the entry or exit basic blocks.
     * @param q  quad to add */
    public void appendQuad(Quad q) {
        Assert._assert(instructions != null, "Cannot add instructions to entry/exit basic block");
        instructions.add(q);
    }
    
    /**
     * Replace the quad at position pos.
     * */
    public void replaceQuad(int pos, Quad q) {
        Assert._assert(instructions != null, "Cannot add instructions to entry/exit basic block");
        instructions.set(pos, q);
    }
    
    /** Add a predecessor basic block to this basic block.
     * Cannot add predecessors to the entry basic block.
     * @param b  basic block to add as a predecessor */
    public void addPredecessor(BasicBlock b) {
        Assert._assert(predecessors != null, "Cannot add predecessor to entry basic block");
        predecessors.add(b);
    }
    /** Add a successor basic block to this basic block.
     * Cannot add successors to the exit basic block.
     * @param b  basic block to add as a successor */
    public void addSuccessor(BasicBlock b) {
        Assert._assert(successors != null, "Cannot add successor to exit basic block");
        successors.add(b);
    }
    
    public boolean removePredecessor(BasicBlock bb) {
        Assert._assert(predecessors != null, "Cannot remove predecessor from entry basic block");
        return predecessors.remove(bb);
    }
    public void removePredecessor(int i) {
        Assert._assert(predecessors != null, "Cannot remove predecessor from entry basic block");
        predecessors.remove(i);
    }
    public boolean removePredecessors(Collection bb) {
        Assert._assert(predecessors != null, "Cannot remove predecessor from entry basic block");
        return predecessors.removeAll(bb);
    }
    public boolean removeSuccessor(BasicBlock bb) {
        Assert._assert(successors != null, "Cannot remove successor from exit basic block");
        return successors.remove(bb);
    }
    public void removeSuccessor(int i) {
        Assert._assert(successors != null, "Cannot remove successor from exit basic block");
        successors.remove(i);
    }
    public void removeAllPredecessors() {
        Assert._assert(predecessors != null, "Cannot remove predecessors from entry basic block");
        predecessors.clear();
    }
    public void removeAllSuccessors() {
        Assert._assert(successors != null, "Cannot remove successors from exit basic block");
        successors.clear();
    }
        
    public int getNumberOfSuccessors() {
        if (successors == null) return 0;
        return successors.size();
    }

    public int getNumberOfPredecessors() {
        if (predecessors == null) return 0;
        return predecessors.size();
    }

    /** Returns the fallthrough successor to this basic block, if it exists.
     * If there is none, returns null.
     * @return  the fallthrough successor, or null if there is none. */
    public BasicBlock getFallthroughSuccessor() {
        if (successors == null) return null;
        return (BasicBlock)successors.get(0);
    }

    /** Returns the fallthrough predecessor to this basic block, if it exists.
     * If there is none, returns null.
     * @return  the fallthrough predecessor, or null if there is none. */
    public BasicBlock getFallthroughPredecessor() {
        if (predecessors == null) return null;
        return (BasicBlock)predecessors.get(0);
    }

    /** Returns a list of the successors of this basic block.
     * @return  a list of the successors of this basic block. */
    public List.BasicBlock getSuccessors() {
        if (successors == null) return UnmodifiableList.BasicBlock.getEmptyList();
        return new ListWrapper.BasicBlock(successors);
    }
    
    /** Returns an list of the predecessors of this basic block.
     * @return  an iterator of the predecessors of this basic block. */
    public List.BasicBlock getPredecessors() {
        if (predecessors == null) return UnmodifiableList.BasicBlock.getEmptyList();
        return new ListWrapper.BasicBlock(predecessors);
    }
    
    void addExceptionHandler_first(ExceptionHandlerList eh) {
        eh.getHandler().addHandledBasicBlock(this);
        Assert._assert(eh.parent == null);
        eh.parent = this.exception_handler_list;
        this.exception_handler_list = eh;
    }
    ExceptionHandlerList addExceptionHandler(ExceptionHandlerList eh) {
        eh.getHandler().addHandledBasicBlock(this);
        if (eh.parent == this.exception_handler_list)
            return this.exception_handler_list = eh;
        else
            return this.exception_handler_list = new ExceptionHandlerList(eh.getHandler(), this.exception_handler_list);
    }
    void setExceptionHandlerList(ExceptionHandlerList ehl) {
        this.exception_handler_list = ehl;
    }
    
    /** Returns the list of exception handlers that guard this basic block.
     * @see ExceptionHandlerList
     * @return  an iterator of the exception handlers that guard this basic block. */
    public ExceptionHandlerList getExceptionHandlers() {
        if (exception_handler_list == null) return ExceptionHandlerList.getEmptyList();
        return exception_handler_list;
    }

    /** Appends the list of exception handlers to the current list of
     * exception handlers. Doesn't append if it is already there.
     */
    public void appendExceptionHandlerList(ExceptionHandlerList list) {
        if (list == null || list.size() == 0) return;
        ExceptionHandlerList p = this.exception_handler_list;
        if (p == null) {
            this.exception_handler_list = list; return;
        }
        for (;;) {
            if (p == list) return;
            ExceptionHandlerList q = p.getParent();
            if (q == null) {
                p.setParent(list); return;
            }
            p = q;
        }
    }
    
    /** Returns the unique id number for this basic block.
     * @return  the unique id number for this basic block. */
    public int getID() { return id_number; }

    public List.BasicBlock getExceptionHandlerEntries() {
        if (exception_handler_list == null) return UnmodifiableList.BasicBlock.getEmptyList();
        java.util.ArrayList result = new java.util.ArrayList(exception_handler_list.size());
        for (ListIterator.ExceptionHandler i = exception_handler_list.exceptionHandlerIterator();
             i.hasNext(); ) {
            ExceptionHandler eh = i.nextExceptionHandler();
            result.add(eh.getEntry());
        }
        return new ListWrapper.BasicBlock(result);
    }
    
    /** Returns true if this basic block has been marked as an exception handler
     * entry point.  Returns false otherwise.
     * @return  if this basic block has been marked as an exception handler entry point. */
    public boolean isExceptionHandlerEntry() { return (flags & EXCEPTION_HANDLER_ENTRY) != 0; }
    /** Marks this basic block as an exception handler entry point.
     */
    public void setExceptionHandlerEntry() { flags |= EXCEPTION_HANDLER_ENTRY; }
    
    /** Returns true if this basic block has been marked as a JSR entry.
     * entry point.  Returns false otherwise.
     * @return  if this basic block has been marked as a JSR entry. */
    public boolean isJSREntry() { return (flags & JSR_ENTRY) != 0; }
    /** Marks this basic block as a JSR entry.
     */
    public void setJSREntry() { flags |= JSR_ENTRY; }
    
    public boolean endsInRet() {
        Quad last = getLastQuad();
        if (last == null) return false;
        return last.getOperator() == Ret.RET.INSTANCE;
    }
    
    public void appendQuadBeforeBranchOrPEI(Quad c) {
        java.util.ListIterator li = instructions.listIterator();
        while (li.hasNext()) li.next();
        while (li.hasPrevious()) {
            Quad q = (Quad) li.previous();
            if (q.getOperator() instanceof Operator.Branch) continue;
            if (!q.getThrownExceptions().isEmpty()) continue;
            break;
        }
        li.add(c);
    }
    
    /** Returns the name of this basic block.
     * @return  the name of this basic block. */
    public String toString() {
        if (isEntry()) {
            Assert._assert(getID() == 0);
            return "BB0 (ENTRY)";
        }
        if (isExit()) {
            Assert._assert(getID() == 1);
            return "BB1 (EXIT)";
        }
        return "BB"+getID();
    }
    
    public void addAtEnd(ControlFlowGraph ir, Quad c) {
        appendQuadBeforeBranchOrPEI(c);
        RegisterOperand aux = null;
        RegisterOperand lhs = Move.getDest(c);
        java.util.ListIterator li = this.instructions.listIterator();
        while (li.next() != c) ;
        while (li.hasNext()) {
            Quad i = (Quad) li.next();
            for (Iterator os = i.getUsedRegisters().iterator(); os.hasNext(); ) {
                Operand op = (Operand) os.next();
                if (lhs.isSimilar(op)) {
                    if (aux == null) {
                        aux = ir.getRegisterFactory().makeRegOp(lhs.getRegister(), lhs.getType());
                        Quad m = Move.create(ir.getNewQuadID(), aux.getRegister(), lhs.getRegister(), lhs.getType());
                        int index = instructions.indexOf(c);
                        instructions.add(index, m);
                    }
                    ((RegisterOperand)op).setRegister(aux.getRegister());
                }
            }
        }
    }
    /** Returns a String describing the name, predecessor, successor, exception
     * handlers, and quads of this basic block.
     * @return  a verbose string describing this basic block */    
    public String fullDump() {
        StringBuffer sb = new StringBuffer();
        sb.append(toString());
        sb.append("\t(in: ");
        ListIterator.BasicBlock bbi = getPredecessors().basicBlockIterator();
        if (!bbi.hasNext()) sb.append("<none>");
        else {
            sb.append(bbi.nextBasicBlock().toString());
            while (bbi.hasNext()) {
                sb.append(", ");
                sb.append(bbi.nextBasicBlock().toString());
            }
        }
        sb.append(", out: ");
        bbi = getSuccessors().basicBlockIterator();
        if (!bbi.hasNext()) sb.append("<none>");
        else {
            sb.append(bbi.nextBasicBlock().toString());
            while (bbi.hasNext()) {
                sb.append(", ");
                sb.append(bbi.nextBasicBlock().toString());
            }
        }
        sb.append(')');
        ListIterator.ExceptionHandler ehi = getExceptionHandlers().exceptionHandlerIterator();
        if (ehi.hasNext()) {
            sb.append(Strings.lineSep+"\texception handlers: ");
            sb.append(ehi.next().toString());
            while (ehi.hasNext()) {
                sb.append(", ");
                sb.append(ehi.next().toString());
            }
        }
        sb.append(Strings.lineSep);
        ListIterator.Quad qi = iterator();
        while (qi.hasNext()) {
            sb.append(qi.nextQuad().toString());
            sb.append(Strings.lineSep);
        }
        sb.append(Strings.lineSep);
        return sb.toString();
    }
    
}
