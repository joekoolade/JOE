// Dominators.java, created Wed Jan 30 22:34:43 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;
import jwutil.graphs.Navigator;
import jwutil.graphs.Traversals;
import jwutil.math.BitString;
import jwutil.math.BitString.BitStringIterator;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Dominators.java,v 1.15 2004/09/22 22:17:27 joewhaley Exp $
 */
public class Dominators extends jq_MethodVisitor.EmptyVisitor implements BasicBlockVisitor {

    /** true = normal dominators.
     * false = post dominators.
     */
    public Dominators(boolean direction) {
        this.direction = direction;
    }
    public Dominators() {
        this(false);
    }

    public static final boolean TRACE = false;

    public final boolean direction;
    public BitString[] dominators;
    protected boolean change;
    protected ControlFlowGraph cfg;
    protected BasicBlock[] bbs;
    private BitString temp;
    
    public void visitMethod(jq_Method m) {
        if (m.getBytecode() == null) return;
        cfg = joeq.Compiler.Quad.CodeCache.getCode(m);
        bbs = new BasicBlock[cfg.getNumberOfBasicBlocks()];
        dominators = new BitString[cfg.getNumberOfBasicBlocks()];
        temp = new BitString(dominators.length);
        int offset = direction?1:0;
        dominators[offset] = new BitString(dominators.length);
        dominators[offset].setAll();
        dominators[1-offset] = new BitString(dominators.length);
        dominators[1-offset].set(1-offset);
        for (int i=2; i<dominators.length; ++i) {
            dominators[i] = new BitString(dominators.length);
            dominators[i].setAll();
        }
        List.BasicBlock rpo;
        if (direction)
            rpo = cfg.reversePostOrder(cfg.entry());
        else
            rpo = cfg.reversePostOrderOnReverseGraph(cfg.exit());
        for (;;) {
            if (TRACE) System.out.println("Iterating over "+rpo);
            change = false;
            ListIterator.BasicBlock rpo_i = rpo.basicBlockIterator();
            BasicBlock first = rpo_i.nextBasicBlock(); // skip first node.
            bbs[first.getID()] = first;
            while (rpo_i.hasNext()) {
                BasicBlock bb = rpo_i.nextBasicBlock();
                this.visitBasicBlock(bb);
            }
            if (!change) break;
        }
        /*for (int i=0; i<dominators.length; ++i) {
            System.out.println("Dom "+i+": "+dominators[i]);
        }*/
        //computeTree();
        
    }

    public void visitBasicBlock(BasicBlock bb) {
        if (TRACE) System.out.println("Visiting: "+bb);
        bbs[bb.getID()] = bb;
        temp.setAll();
        ListIterator.BasicBlock preds = direction?bb.getPredecessors().basicBlockIterator():
                                                  bb.getSuccessors().basicBlockIterator();
        while (preds.hasNext()) {
            BasicBlock pred = preds.nextBasicBlock();
            if (TRACE) System.out.println("Visiting pred: "+pred);
            temp.and(dominators[pred.getID()]);
        }
        if (direction) {
            if (bb.isExceptionHandlerEntry()) {
                Iterator it = cfg.getExceptionHandlersMatchingEntry(bb);
                while (it.hasNext()) {
                    ExceptionHandler eh = (ExceptionHandler)it.next();
                    preds = eh.getHandledBasicBlocks().basicBlockIterator();
                    while (preds.hasNext()) {
                        BasicBlock pred = preds.nextBasicBlock();
                        if (TRACE) System.out.println("Visiting ex pred: "+pred);
                        temp.and(dominators[pred.getID()]);
                    }
                }
            }
        } else {
            ListIterator.ExceptionHandler it = bb.getExceptionHandlers().exceptionHandlerIterator();
            while (it.hasNext()) {
                ExceptionHandler eh = (ExceptionHandler)it.next();
                BasicBlock pred = eh.getEntry();
                if (TRACE) System.out.println("Visiting ex pred: "+pred);
                temp.and(dominators[pred.getID()]);
            }
        }
        temp.set(bb.getID());
        if (!temp.equals(dominators[bb.getID()])) {
            if (TRACE) System.out.println("Changed!");
            //dominators[bb.getID()] <- temp
            dominators[bb.getID()].copyBits(temp);
            change = true;
        }
        //reset change to break the loop
        //else change = false; 
    }
    
    DominatorNode[] dominatorNodes;
    
    public DominatorNode getDominatorNode(BasicBlock bb) {
        return dominatorNodes[bb.getID()];
    }
    
    public BasicBlock getImmediateDominator(BasicBlock bb) {
        DominatorNode n = getDominatorNode(bb);
        return n.parent.getBasicBlock();
    }
    
    public DominatorNode computeTree() {
        // TODO: fix this. this algorithm sucks (n^4 or so)
        dominatorNodes = new DominatorNode[dominators.length];
        ArrayList list = new ArrayList();
        list.add(new ArrayList());
        for (int depth = 1; ; ++depth) {
            if (TRACE) System.out.println("depth: "+depth);
            ArrayList list2 = new ArrayList();
            boolean found = false;
            for (int i=0; i<dominators.length; ++i) {
                if (dominators[i].numberOfOnes() == depth) {
                    if (TRACE) System.out.println("bb"+i+" matches: "+dominators[i]);
                    found = true;
                    temp.copyBits(dominators[i]);
                    temp.clear(i);
                    DominatorNode parent = null;
                    Iterator it = ((ArrayList)list.get(depth-1)).iterator();
                    while (it.hasNext()) {
                        DominatorNode n = (DominatorNode)it.next();
                        if (temp.equals(dominators[n.getBasicBlock().getID()])) {
                            parent = n; break;
                        }
                    }
                    DominatorNode n0 = new DominatorNode(bbs[i], parent);
                    if (parent != null)
                        parent.addChild(n0);
                    dominatorNodes[i] = n0;
                    list2.add(n0);
                }
            }
            list.add(list2);
            if (!found) break;
        }
        DominatorNode root = (DominatorNode)((ArrayList)list.get(1)).get(0);
        return root;
    }
    
    public static class DominatorNode {
        public final BasicBlock bb;
        public final DominatorNode parent;
        public final ArrayList children;
        public BitString dominance_frontier;
        
        public DominatorNode(BasicBlock bb, DominatorNode parent) {
            this.bb = bb; this.parent = parent; this.children = new ArrayList();
        }
        
        public BasicBlock getBasicBlock() { return bb; }
        public DominatorNode getParent() { return parent; }
        public int getNumberOfChildren() { return children.size(); }
        public DominatorNode getChild(int i) { return (DominatorNode)children.get(i); }
        public java.util.List getChildren() { return children; }
        public void addChild(DominatorNode n) { children.add(n); }
        public String toString() { return bb.toString(); }
        public void dumpTree() {
            System.out.println("Node: "+toString());
            System.out.println("Children of :"+toString());
            Iterator i = children.iterator();
            while (i.hasNext()) {
                ((DominatorNode)i.next()).dumpTree();
            }
            System.out.println("End of children of :"+toString());
        }
    }
    
    public static final Navigator dom_nav = new Navigator() {

        public Collection next(Object node) {
            DominatorNode dn = (DominatorNode) node;
            return dn.children;
        }

        public Collection prev(Object node) {
            DominatorNode dn = (DominatorNode) node;
            if (dn.parent == null) return Collections.EMPTY_LIST;
            return Collections.singleton(dn.parent);
        }
        
    };
    
    public void calculateDominanceFrontier(DominatorNode tree) {
        // for each X in a bottom-up traversal of the dominator tree do
        for (Iterator x = Traversals.postOrder(dom_nav, tree).iterator(); x.hasNext();) {
            DominatorNode v = (DominatorNode) x.next();
            BasicBlock X = v.getBasicBlock();
            BitString DF = new BitString(dominatorNodes.length);
            v.dominance_frontier = DF;
            // for each Y in Succ(X) do
            for (Iterator y = X.getSuccessors().iterator(); y.hasNext();) {
                BasicBlock Y = (BasicBlock) y.next();
                // skip EXIT node
                if (Y.isExit())
                    continue;
                // if (idom(Y)!=X) then DF(X) <- DF(X) U Y
                if (getImmediateDominator(Y) != X)
                    DF.set(Y.getID());
            }
            //        for each Z in {idom(z) = X} do
            for (Iterator z = getDominatorNode(X).getChildren().iterator(); z.hasNext();) {
                DominatorNode zVertex = (DominatorNode) z.next();
                BasicBlock Z = zVertex.getBasicBlock();
                // for each Y in DF(Z) do
                for (BitStringIterator y = zVertex.dominance_frontier
                        .iterator(); y.hasNext();) {
                    int I = y.nextIndex();
                    BasicBlock Y = bbs[I];
                    // if (idom(Y)!=X) then DF(X) <- DF(X) U Y
                    if (getImmediateDominator(Y) != X)
                        DF.set(Y.getID());
                }
            }
        }
    }
    
    public boolean dominates(int b, BitString b2) {
        BitString b1 = (BitString) this.dominators[b].clone();
        b1.minus(b2);
        return b1.isZero();
    }
    
    public BitString getDominanceFrontier(BitString bits) {
        BitString result = new BitString(dominatorNodes.length);
        for (BitStringIterator y = bits.iterator(); y.hasNext();) {
            int I = y.nextIndex();
            DominatorNode dn = dominatorNodes[I];
            result.or(dn.dominance_frontier);
        }
        return result;
    }
    public BitString getIteratedDominanceFrontier(BitString S) {
        BitString DFi = getDominanceFrontier(S);
        for (;;) {
            BitString DFiplus1 = getDominanceFrontier(DFi);
            DFiplus1.or(DFi);
            if (DFi.equals(DFiplus1))
                break;
            DFi = DFiplus1;
        }
        return DFi;
    }
    
    public static void main(String[] args) {
        joeq.Main.HostedVM.initialize();
        
        joeq.Class.jq_Class c = (joeq.Class.jq_Class) joeq.Class.jq_Type.parseType(args[0]);
        c.load();
        Dominators dom = new Dominators();
        jq_Method[] ms = c.getDeclaredStaticMethods();
        for (int i=0; i<ms.length; ++i) {
            dom.visitMethod(ms[i]);
            DominatorNode n = dom.computeTree();
            n.dumpTree();
        }
    }
    
}
