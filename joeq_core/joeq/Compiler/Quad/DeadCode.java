// DeadCode.java, created Tue Jun  4 15:58:53 2002 by joewhaley
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

/**
 * @author Michael Martin <mcmartin@stanford.edu>
 * @version $Id: DeadCode.java,v 1.7 2005/05/05 19:37:36 joewhaley Exp $
 */
public class DeadCode extends DataflowFramework.EmptyAnalysis {
    static class TraceFact implements DataflowFramework.Fact {
        boolean _val;
        public TraceFact(boolean t) { _val = t; }

        public DataflowFramework.Fact deepCopy() { return new TraceFact(_val); }
        public DataflowFramework.Fact meetWith(DataflowFramework.Fact f) {
            TraceFact other = (TraceFact) f;
            _val = _val || other._val;
            return this;
        }
        public boolean equals(Object o) {
            if (o instanceof TraceFact) {
                return ((TraceFact) o)._val == _val;
            }
            return false;
        }
        public int hashCode() {
            return _val ? 1 : 0;
        }
    }

    public void preprocess(ControlFlowGraph cfg) {
        _fc.setInitial(new TraceFact(true));
        _fc.setFinal(new TraceFact(false));
        QuadIterator qi = new QuadIterator(cfg);
        while (qi.hasNext()) {
            Quad q = qi.nextQuad();
            _fc.setPre(q, new TraceFact(false));
            _fc.setPost(q, new TraceFact(false));
        }
    }

    public boolean transfer(Quad q) {
        DataflowFramework.Fact older = _fc.getPost(q).deepCopy();
        DataflowFramework.Fact newer = _fc.getPre(q).deepCopy();
        _fc.setPost(q, newer);
        return !newer.equals(older);
    }

    public void postprocess(ControlFlowGraph cfg) {
        QuadIterator qi = new QuadIterator(cfg);
        System.out.println("Results:");
        int deadCount = 0;
        while (qi.hasNext()) {
            Quad q = qi.nextQuad();
            if (((TraceFact)(_fc.getPre(q)))._val) continue;
            ++deadCount;
            System.out.println("UNREACHABLE: "+q);
        }
        if (deadCount == 0) {
            System.out.println("All quads are reachable.");
        } else if (deadCount == 1) {
            System.out.println("1 quad is unreachable.");
        } else {
            System.out.println(deadCount + " quads are unreachable.");
        }       
    }
}
