// ControlDependence.java, created Wed Jan 30 22:31:58 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Iterator;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Compiler.Quad.Operand.AConstOperand;
import joeq.Compiler.Quad.Operand.ConditionOperand;
import joeq.Compiler.Quad.Operand.FieldOperand;
import joeq.Compiler.Quad.Operand.IConstOperand;
import joeq.Compiler.Quad.Operator.Getfield;
import joeq.Compiler.Quad.Operator.IntIfCmp;
import joeq.Compiler.Quad.Operator.Return;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ControlDependence.java,v 1.10 2004/09/22 22:17:26 joewhaley Exp $
 */
public class ControlDependence extends jq_MethodVisitor.EmptyVisitor {

    jq_Method current_method;
    
    public void visitMethod(jq_Method m) {
        if (m.getBytecode() == null) return;
        System.out.println("Visiting method "+m);
        current_method = m;
        ControlFlowGraph cfg = joeq.Compiler.Quad.CodeCache.getCode(m);
        Dominators dom = new Dominators(false);
        dom.visitMethod(m);
        Dominators.DominatorNode root = dom.computeTree();
        
        // find blocks that end in a throw statement.
        ListIterator.BasicBlock li = cfg.exit().getPredecessors().basicBlockIterator();
        while (li.hasNext()) {
            BasicBlock bb = li.nextBasicBlock();
            Quad q = bb.getLastQuad();
            if (q == null) {
                System.out.println("NOTE! empty basic block "+bb+" goes to exit");
                continue;
            }
            if (q.getOperator() == Return.THROW_A.INSTANCE) {
                System.out.println("Found throw statement in "+bb+"! "+q);
                // find control dependence
                Dominators.DominatorNode dn = getDomNode(root, bb);
                findControlDependence(dn, root);
            }
        }
    }

    static Dominators.DominatorNode getDomNode(Dominators.DominatorNode p, BasicBlock bb) {
        if (p.getBasicBlock() == bb) return p;
        Iterator i = p.getChildren().iterator();
        while (i.hasNext()) {
            Dominators.DominatorNode x = getDomNode((Dominators.DominatorNode)i.next(), bb);
            if (x != null) return x;
        }
        return null;
    }

    void findControlDependence(Dominators.DominatorNode p, Dominators.DominatorNode root) {
        Iterator i = p.getChildren().iterator();
        if (!i.hasNext()) {
            if (p.getBasicBlock().isEntry()) {
                System.out.println("Post-dominates entry "+p);
                return;
            }
            //System.out.println("Reached leaf node: "+p);
            List.BasicBlock preds = p.getBasicBlock().getPredecessors();
            //System.out.println("Predecessors: "+preds);
            ListIterator.BasicBlock li = preds.basicBlockIterator();
            while (li.hasNext()) {
                BasicBlock pred = li.nextBasicBlock();
                Quad q = pred.getLastQuad();
                //System.out.println("last quad of "+pred+" is "+q);
                if (pred.size() < 2) break;
                Quad q2 = pred.getQuad(pred.size()-2);
                //System.out.println("next-to-last quad is "+q2);
                if (q.getOperator() instanceof IntIfCmp) {
                    Operand src2 = IntIfCmp.getSrc2(q);
                    ConditionOperand cond = IntIfCmp.getCond(q);
                    if ((q.getOperator() == IntIfCmp.IFCMP_A.INSTANCE) &&
                       (src2 instanceof AConstOperand) &&
                       ((AConstOperand)src2).getValue() == null) {
                        if (q2.getOperator() instanceof Getfield) {
                            FieldOperand fo = Getfield.getField(q2);
                            System.out.println("depends on "+cond+" comparison between field "+fo+" and const null");
                            add(current_method);
                            findControlDependence(getDomNode(root, pred), root);
                        }
                    } else if ((q.getOperator() == IntIfCmp.IFCMP_I.INSTANCE) &&
                             (src2 instanceof IConstOperand)) {
                        if (q2.getOperator() instanceof Getfield) {
                            FieldOperand fo = Getfield.getField(q2);
                            System.out.println("depends on "+cond+" comparison between field "+fo+" and const "+((IConstOperand)src2).getValue());
                            add(current_method);
                            findControlDependence(getDomNode(root, pred), root);
                        }
                    }
                } 
            }
        } else {
            while (i.hasNext()) {
                Dominators.DominatorNode x = (Dominators.DominatorNode)i.next();
                findControlDependence(x, root);
            }
        }
    }
    
    public java.util.HashSet found_classes = new java.util.HashSet();
    public java.util.HashSet found_methods = new java.util.HashSet();
    
    void add(jq_Method x) {
        found_methods.add(x);
        found_classes.add(x.getDeclaringClass());
    }
    
    public String toString() { return "found "+found_methods.size()+" methods in "+found_classes.size()+" classes"; }
}
