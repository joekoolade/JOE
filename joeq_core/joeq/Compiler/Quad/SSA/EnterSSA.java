// EnterSSA.java, created Mar 21, 2004 7:49:47 PM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad.SSA;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Type;
import joeq.Class.jq_Reference.jq_NullType;
import joeq.Compiler.Dataflow.LivenessAnalysis;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.Dominators;
import joeq.Compiler.Quad.ExceptionHandler;
import joeq.Compiler.Quad.ExceptionHandlerList;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.BytecodeToQuad.jq_ReturnAddressType;
import joeq.Compiler.Quad.Dominators.DominatorNode;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.Phi;
import joeq.Compiler.Quad.Operator.Special;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Runtime.TypeCheck;
import jwutil.collections.GenericMultiMap;
import jwutil.collections.MultiMap;
import jwutil.collections.Pair;
import jwutil.math.BitString;
import jwutil.math.BitString.BitStringIterator;
import jwutil.util.Assert;

/**
 * Transform IR into SSA form.
 * Adapted from some code I found somewhere on the net...
 * 
 * @author jwhaley
 * @version $Id: EnterSSA.java,v 1.8 2004/09/22 22:17:47 joewhaley Exp $
 */
public class EnterSSA implements ControlFlowGraphVisitor { 

    static boolean DEBUG = false;
    static boolean PRINT_SSA = false;
    private ControlFlowGraph ir;
    private LivenessAnalysis live;
    /**
     * The set of scalar phi functions inserted
     */
    private Set scalarPhis = new HashSet();
    /**
     * For each basic block, the number of predecessors that have been
     * processed.
     */
    private int[] numPredProcessed;
    
    BasicBlock[] basic_blocks;
    Dominators dominators;
    
    public void visitCFG(ControlFlowGraph ir) {
        this.ir = ir;
        this.basic_blocks = new BasicBlock[ir.getNumberOfBasicBlocks()];
        for (Iterator i = ir.reversePostOrderIterator(); i.hasNext(); ) {
            BasicBlock bb = (BasicBlock) i.next();
            this.basic_blocks[bb.getID()] = bb;
        }
        this.dominators = new Dominators(true);
        dominators.visitMethod(ir.getMethod());
        DominatorNode n = dominators.computeTree();
        dominators.calculateDominanceFrontier(n);
        this.link_registers = new HashMap();
        this.register_uses = new GenericMultiMap();
        prepare();
        patchPEIgeneratedValues();
        computeSSA(ir);
    }
    
    /**
     * Perform some calculations to prepare for SSA construction.
     */
    private void prepare() {
        live = LivenessAnalysis.solve(ir);
    }
    
    /**
     * Work around some problems with PEI-generated values and handlers.
     * Namely, if a PEI has a return value, rename the result register before
     * and after the PEI in order to reflect the fact that the PEI may not
     * actually assign the result register.
     */
    private void patchPEIgeneratedValues() {
        // this only applies if there are exception handlers
        if (ir.getExceptionHandlers().isEmpty())
            return;
        Set needed = new HashSet(4);
        Iterator blocks = ir.reversePostOrderIterator();
        while (blocks.hasNext()) {
            BasicBlock block = (BasicBlock) blocks.next();
            ExceptionHandlerList ehl = block.getExceptionHandlers();
            if (!ehl.isEmpty()) {
                Quad pei = block.getLastQuad();
                if (pei != null && !pei.getThrownExceptions().isEmpty() && Special.getOp1(pei) instanceof RegisterOperand) {
                    boolean copyNeeded = false;
                    RegisterOperand v = (RegisterOperand) Special.getOp1(pei);
                    Register orig = v.getRegister();
                    Iterator out = ehl.mayCatch(pei.getThrownExceptions()).iterator();
                    while (out.hasNext()) {
                        ExceptionHandler eh = (ExceptionHandler) out.next();
                        BasicBlock exp = eh.getEntry();
                        if (live.isLiveAtIn(exp, orig)) {
                            copyNeeded = true;
                            break;
                        }
                    }
                    if (copyNeeded) {
                        boolean copyRequested = false;
                        Iterator out2 = ehl.mayCatch(pei.getThrownExceptions()).iterator();
                        while (out2.hasNext()) {
                            ExceptionHandler eh = (ExceptionHandler) out2.next();
                            BasicBlock exp = eh.getEntry();
                            needed.add(new Pair(exp, v));
                        }
                    }
                }
            }
        }
        // having determine where copies should be inserted, now insert them.
        Iterator copies = needed.iterator();
        while (copies.hasNext()) {
            Pair copy = (Pair) copies.next();
            BasicBlock inBlock = (BasicBlock) copy.left;
            RegisterOperand registerOp = (RegisterOperand) copy.right;
            jq_Type type = registerOp.getType();
            Register register = registerOp.getRegister();
            Register temp = ir.getRegisterFactory().makeReg(register);
            inBlock.addQuad(0, Move.create(ir.getNewQuadID(), register, temp, type));
            live.setLiveAtIn(inBlock, temp);
            Iterator outBlocks = inBlock.getPredecessors().iterator();
            while (outBlocks.hasNext()) {
                BasicBlock outBlock = (BasicBlock) outBlocks.next();
                Quad x = Move.create(ir.getNewQuadID(), temp, register, type);
                outBlock.addAtEnd(ir, x);
                live.setKilledAtIn(outBlock, temp);
            }
        }
    }
    
    /**
     * Calculate SSA form for an IR. This routine holds the guts of the
     * transformation.
     */
    private void computeSSA(ControlFlowGraph ir) {
        // reset Array SSA information
        if (DEBUG)
            System.out.println("Computing register lists...");
        // 1. re-compute the flow-insensitive isSSA flag for each register
        markSSARegisterFlags(ir);
        // 3. walk through the IR, and set up BitVectors representing the defs
        //    for each symbolic register (more efficient than using register
        //  lists)
        if (DEBUG)
            System.out.println("Find defs for each register...");
        BitString[] defSets = getDefSets();
        // 4. Insert phi functions for scalars
        if (DEBUG)
            System.out.println("Insert phi functions...");
        insertPhiFunctions(ir, defSets);
        // 5. Insert heap variables into the Array SSA form
        if (DEBUG)
            System.out.println("Before renaming...");
        if (DEBUG)
            System.out.println(ir.fullDump());
        if (DEBUG)
            System.out.println("Renaming...");
        renameSymbolicRegisters();
        if (DEBUG)
            System.out.println("SSA done.");
        if (PRINT_SSA)
            System.out.println(ir.fullDump());
    }
    
    /**
     * Calculate the set of blocks that contain defs for each symbolic register
     * in an IR. <em> Note: </em> This routine skips registers marked already
     * having a single static definition, physical registers, and guard
     * registers.
     * 
     * @return an array of BitVectors, where element <em>i</em> represents
     *         the basic blocks that contain defs for symbolic register <em>i</em>
     */
    private BitString[] getDefSets() {
        int nBlocks = ir.getNumberOfBasicBlocks();
        BitString[] result = new BitString[ir.getRegisterFactory().size()];
        for (int i = 0; i < result.length; i++)
            result[i] = new BitString(nBlocks);
        // loop over each basic block
        for (Iterator e = ir.reversePostOrderIterator(); e.hasNext();) {
            BasicBlock bb = (BasicBlock) e.next();
            if (DEBUG) System.out.println("Visiting "+bb);
            int bbNumber = bb.getID();
            // visit each instruction in the basic block
            for (Iterator ie = bb.iterator(); ie.hasNext(); ) {
                Quad s = (Quad) ie.next();
                // record each def in the instruction
                // skip SSA defs
                for (Iterator j = s.getDefinedRegisters().iterator(); j.hasNext(); ) {
                    RegisterOperand operand = (RegisterOperand) j.next();
                    if (operand.getRegister().isSSA()) continue;
                    if (operand.getRegister().isPhysical()) continue;
                    if (operand.getRegister().isGuard()) continue;
                    int reg = operand.getRegister().getNumber();
                    result[reg].set(bbNumber);
                }
            }
        }
        return result;
    }
    
    /**
     * Insert the necessary phi functions into an IR.
     * <p>
     * Algorithm:
     * <p>
     * For register r, let S be the set of all blocks that contain defs of r.
     * Let D be the iterated dominance frontier of S. Each block in D needs a
     * phi-function for r.
     * 
     * <p>
     * Special Java case: if node N dominates all defs of r, then N does not
     * need a phi-function for r
     */
    private void insertPhiFunctions(ControlFlowGraph ir, BitString[] defs) {
        for (Iterator it = ir.getRegisterFactory().iterator(); it.hasNext(); ) {
            Register reg = (Register) it.next();
            int r = reg.getNumber();
            if (reg.isSSA()) continue;
            if (reg.isPhysical()) continue;
            if (reg.isGuard()) continue;
            if (DEBUG)
                System.out.println("Inserting phis for register " + r);
            if (DEBUG)
                System.out.println("Start iterated frontier...");
            BitString needsPhi = dominators.getIteratedDominanceFrontier(defs[r]);
            if (DEBUG)
                System.out.println("Iterated frontier = "+needsPhi);
            removePhisThatDominateAllDefs(needsPhi, ir, defs[r]);
            if (DEBUG)
                System.out.println("Done.");
            for (BitStringIterator i = needsPhi.iterator(); i.hasNext(); ) {
                int b = i.nextIndex();
                BasicBlock bb = basic_blocks[b];
                if (live.isLiveAtIn(bb, reg)) {
                    if (DEBUG)
                        System.out.println("Inserting phi at "+bb);
                    insertPhi(bb, reg);
                } else {
                    if (DEBUG)
                        System.out.println(reg+" not live at "+bb);
                }
            }
        }
    }
    
    /**
     * If node N dominates all defs of a register r, then N does not need a phi
     * function for r; this function removes such nodes N from a Bit Set.
     * 
     * @param needsPhi
     *                representation of set of nodes that need phi functions
     *                for a register r
     * @param defs
     *                set of nodes that define register r
     */
    private void removePhisThatDominateAllDefs(BitString needsPhi, ControlFlowGraph ir, BitString defs) {
        for (BitStringIterator i = needsPhi.iterator(); i.hasNext(); ) {
            int index = i.nextIndex();
            if (dominators.dominates(index, defs)) {
                if (DEBUG)
                    System.out.println(index+" dominates all defs, so phi is not needed.");
                needsPhi.clear(index);
            }
        }
    }
    
    /**
     * Insert a phi function for a symbolic register at the head of a basic
     * block.
     * 
     * @param bb
     *                the basic block
     * @param r
     *                the symbolic register that needs a phi function
     */
    private void insertPhi(BasicBlock bb, Register r) {
        Quad s = makePhiInstruction(r, bb);
        bb.addQuad(0, s);
        scalarPhis.add(s);
    }
    
    /**
     * Create a phi-function instruction
     * 
     * @param r
     *                the symbolic register
     * @param bb
     *                the basic block holding the new phi function
     * @return the instruction r = PHI null,null,..,null
     */
    private Quad makePhiInstruction(Register r, BasicBlock bb) {
        int n = bb.getNumberOfPredecessors();
        Iterator in = bb.getPredecessors().iterator();
        jq_Type type = null;
        Quad s = Phi.create(ir.getNewQuadID(), Phi.PHI.INSTANCE, new RegisterOperand(r, type), n);
        for (int i = 0; i < n; i++) {
            RegisterOperand junk = new RegisterOperand(r, type);
            Phi.setSrc(s, i, junk);
            BasicBlock pred = (BasicBlock) in.next();
            Phi.setPred(s, i, pred);
        }
        //s.position = ir.gc.inlineSequence;
        //s.bcIndex = SSA_SYNTH_BCI;
        return s;
    }
    
    /**
     * Rename the symbolic registers so that each register has only one
     * definition.
     */
    private void renameSymbolicRegisters() {
        int n = ir.getRegisterFactory().size();
        Stack[] S = new Stack[n];
        for (int i = 0; i < S.length; i++) {
            S[i] = new Stack();
        }
        // populate the Stacks with initial names for
        // each parameter, and push "null" for other symbolic registers
        jq_Type[] paramTypes = ir.getMethod().getParamTypes();
        for (int i = 0, j = 0; i < paramTypes.length; ++i, ++j) {
            jq_Type t = paramTypes[i];
            Register r = ir.getRegisterFactory().getOrCreateLocal(j, t);
            if (DEBUG) System.out.println("Param "+i+" local "+j+" type "+t+" register "+r);
            if (t.getReferenceSize() == 8) ++j;
            S[r.getNumber()].push(new RegisterOperand(r, t));
        }
        for (int i = 0; i < S.length; i++) {
            // If a register's name is "null", that means the
            // register has not yet been defined.
            if (S[i].isEmpty()) S[i].push(null);
        }
        BasicBlock entry = ir.entry();
        numPredProcessed = new int[ir.getNumberOfBasicBlocks()];
        search(entry, S);
        markSSARegisterFlags(ir);
        rectifyPhiTypes();
    }
    
    MultiMap register_uses;
    
    Map link_registers;
    
    /**
     * 
     * @param X
     *                basic block to search dominator tree from
     * @param S
     *                stack of names for each register
     */
    private void search(BasicBlock X, Stack[] S) {
        if (DEBUG)
            System.out.println("SEARCH " + X);
        for (Iterator ie = X.iterator(); ie.hasNext(); ) {
            Quad A = (Quad) ie.next();
            if (!(A.getOperator() instanceof Phi)) {
                // replace each use
                for (Iterator u = A.getUsedRegisters().iterator(); u.hasNext(); ) {
                    RegisterOperand rop = (RegisterOperand) u.next();
                    Register r1 = rop.getRegister();
                    if (r1.isSSA()) continue;
                    if (r1.isPhysical()) continue;
                    if (r1.isGuard()) continue;
                    RegisterOperand r2 = (RegisterOperand) S[r1.getNumber()].peek();
                    if (DEBUG)
                        System.out.println("REPLACE NORMAL USE " + r1
                                + " with " + r2);
                    if (r2 != null) {
                        rop.setRegister(r2.getRegister());
                        register_uses.add(rop.getRegister(), rop);
                    }
                }
            }
            // replace each def
            for (Iterator d = A.getDefinedRegisters().iterator(); d.hasNext(); ) {
                RegisterOperand rop = (RegisterOperand) d.next();
                Register r1 = rop.getRegister();
                if (r1.isSSA()) continue;
                if (r1.isPhysical()) continue;
                if (r1.isGuard()) continue;
                Register r2 = ir.getRegisterFactory().makeReg(r1);
                if (DEBUG)
                    System.out.println("PUSH " + r2 + " FOR " + r1 + " BECAUSE " + A);
                S[r1.getNumber()].push(new RegisterOperand(r2, rop.getType()));
                rop.setRegister(r2);
                link_registers.put(r2, r1);
            }
        } // end of first loop
        if (DEBUG)
            System.out.println("SEARCH (second loop) " + X);
        for (Iterator y = X.getSuccessors().iterator(); y.hasNext();) {
            BasicBlock Y = (BasicBlock) y.next();
            if (DEBUG)
                System.out.println(" Successor: " + Y);
            int j = numPredProcessed[Y.getID()]++;
            if (Y.isExit())
                continue;
            Iterator ss = Y.iterator();
            if (!ss.hasNext()) continue;
            // replace use USE in each PHI instruction
            if (DEBUG)
                System.out.println(" Predecessor: " + j);
            while (ss.hasNext()) {
                Quad s = (Quad) ss.next();
                if (!(s.getOperator() instanceof Phi)) break;
                Operand val = Phi.getSrc(s, j);
                if (val instanceof RegisterOperand) {
                    Register r1 = ((RegisterOperand) Phi.getSrc(s, j)).getRegister();
                    // ignore registers already marked SSA by a previous pass
                    if (!r1.isSSA()) {
                        RegisterOperand r2 = (RegisterOperand) S[r1.getNumber()].peek();
                        if (r2 == null) {
                            // in this case, the register is never defined along
                            // this particular control flow path into the basic
                            // block.
                            Phi.setSrc(s, j, null);
                        } else {
                            RegisterOperand rop = (RegisterOperand) r2.copy();
                            Phi.setSrc(s, j, rop);
                            register_uses.add(rop.getRegister(), rop);
                        }
                        Phi.setPred(s, j, X);
                        if (DEBUG) System.out.println("Set "+j+": "+s);
                    }
                }
            }
        } // end of second loop
        if (DEBUG)
            System.out.println("SEARCH (third loop) " + X);
        for (Iterator c = dominators.getDominatorNode(X).getChildren().iterator(); c.hasNext(); ) {
            DominatorNode v = (DominatorNode) c.next();
            if (DEBUG) System.out.println("   Dominator Node " + v);
            search(v.getBasicBlock(), S);
        } // end of third loop
        if (DEBUG)
            System.out.println("SEARCH (fourth loop) " + X);
        for (Iterator a = X.iterator(); a.hasNext(); ) {
            Quad A = (Quad) a.next();
            // loop over each def
            for (Iterator d = A.getDefinedRegisters().iterator(); d.hasNext(); ) {
                RegisterOperand newOp = (RegisterOperand) d.next();
                Register newReg = newOp.getRegister();
                if (newReg.isSSA()) continue;
                if (newReg.isPhysical()) continue;
                if (newReg.isGuard()) continue;
                Register r1 = (Register) link_registers.get(newReg);
                S[r1.getNumber()].pop();
                if (DEBUG)
                    System.out.println("POP " + r1);
            }
        } // end of fourth loop
        if (DEBUG)
            System.out.println("FINISHED SEARCH " + X);
    }
    
    /**
     * Compute type information for operands in each phi instruction.
     */
    private void rectifyPhiTypes() {
        if (DEBUG)
            System.out.println("Rectify phi types.");
        removeAllUnreachablePhis(scalarPhis);
        while (!scalarPhis.isEmpty()) {
            boolean didSomething = false;
            for (Iterator i = scalarPhis.iterator(); i.hasNext(); ) {
                Quad phi = (Quad) i.next();
                if (DEBUG)
                    System.out.println("PHI: " + phi);
                jq_Type meet = meetPhiType(phi);
                if (DEBUG)
                    System.out.println("MEET: " + meet);
                if (meet != null) {
                    didSomething = true;
                    i.remove();
                    RegisterOperand result = (RegisterOperand) Phi.getDest(phi);
                    result.setType(meet);
                    for (Iterator e = register_uses.getValues(result.getRegister()).iterator(); e.hasNext();) {
                        RegisterOperand rop = (RegisterOperand) e.next();
                        rop.setType(meet);
                    }
                }
            }
            if (!didSomething) {
                // iteration has bottomed out.
                return;
            }
        }
    }
    
    /**
     * Remove all phis that are unreachable
     */
    private void removeAllUnreachablePhis(Set scalarPhis) {
        boolean iterateAgain = false;
        do {
            iterateAgain = false;
    outer : for (Iterator i = scalarPhis.iterator(); i.hasNext();) {
                Quad phi = (Quad) i.next();
                for (int j = 0; j < Phi.getSrcs(phi).length(); j++) {
                    Operand op = Phi.getSrc(phi, j);
                    if (op != null) {
                        continue outer;
                    }
                }
                RegisterOperand result = Phi.getDest(phi);
                i.remove();
                Register resultReg = result.getRegister();
                Collection values = register_uses.getValues(resultReg);
                for (Iterator e = values.iterator(); e.hasNext(); ) {
                    RegisterOperand use = (RegisterOperand) e.next();
                    Quad s = use.getQuad();
                    if (s.getOperator() instanceof Phi) {
                        for (int k = 0; k < Phi.getSrcs(phi).length(); k++) {
                            Operand op = Phi.getSrc(phi, k);
                            if (op != null && op.isSimilar(result)) {
                                Phi.setSrc(phi, k, null);
                                if (DEBUG) System.out.println("Set src "+k+": "+phi);
                                iterateAgain = true;
                            }
                        }
                    }
                }
            }
        } while (iterateAgain);
    }
    
    /**
     * Return the meet of the types on the rhs of a phi instruction
     */
    private jq_Type meetPhiType(Quad s) {
        jq_Type result = null;
        for (int i = 0; i < Phi.getSrcs(s).length(); i++) {
            Operand val = Phi.getSrc(s, i);
            if (val == null) continue;
            jq_Type t = ((RegisterOperand)val).getType();
            if (t == jq_NullType.NULL_TYPE) {
                continue;
            }
            if (result == null) {
                result = t;
                continue;
            }
            if (t == null) {
                continue;
            }
            if (result == jq_ReturnAddressType.INSTANCE ||
                    t == jq_ReturnAddressType.INSTANCE) {
                // TODO.
                continue;
            }
            jq_Type meet = TypeCheck.findCommonSuperclass(result, t, false);
            if (meet == null) {
                if ((result.isIntLike() && (t.isReferenceType() || t.getReferenceSize() == 4))
                        || ((result.isReferenceType() || result.getReferenceSize() == 4) && t.isIntLike())) {
                    meet = jq_Primitive.INT;
                } else if (result.isReferenceType() && t.getReferenceSize() == 4) {
                    meet = t;
                } else if (result.getReferenceSize() == 4 && t.isReferenceType()) {
                    meet = result;
                }
            }
            if (meet == null) {
                Assert.UNREACHABLE(result + " and " + t + " meet to null");
            }
            result = meet;
        }
        return result;
    }
    
    public static void markSSARegisterFlags(ControlFlowGraph cfg) {
        Set defined = new HashSet();
        for (Iterator i = cfg.getRegisterFactory().iterator(); i.hasNext(); ) {
            Register r = (Register) i.next();
            r.setSSA();
        }
        for (QuadIterator i = new QuadIterator(cfg); i.hasNext(); ) {
            Quad q = i.nextQuad();
            for (Iterator j = q.getDefinedRegisters().iterator(); j.hasNext(); ) {
                RegisterOperand rop = (RegisterOperand) j.next();
                Register r = rop.getRegister();
                boolean change = defined.add(r);
                if (!change) r.clearSSA();
            }
        }
        if (DEBUG) {
            System.out.println("Defined registers: "+defined);
            System.out.print("SSA registers: ");
            for (Iterator i = cfg.getRegisterFactory().iterator(); i.hasNext(); ) {
                Register r = (Register) i.next();
                if (r.isSSA()) System.out.print(" "+r);
            }
            System.out.println();
            System.out.print("Non-SSA registers: ");
            for (Iterator i = cfg.getRegisterFactory().iterator(); i.hasNext(); ) {
                Register r = (Register) i.next();
                if (!r.isSSA()) System.out.print(" "+r);
            }
            System.out.println();
        }
    }
}
