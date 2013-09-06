// Dataflow.java, created Tue Jun  4 15:58:53 2002 by joewhaley
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;
import java.util.HashMap;
import java.util.Iterator;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;

/**
 * @author Michael Martin <mcmartin@stanford.edu>
 * @version $Id: DataflowFramework.java,v 1.4 2004/09/22 22:17:26 joewhaley Exp $
 */
public class DataflowFramework {
    private static final boolean TRACE_DATAFLOW = true;
    private static final boolean TRACE_DATAFLOW_QUADS = false;
    private static final java.io.PrintStream DF_OUT = System.err;

    interface FactCollection {
        Fact getPre(Quad q);
        Fact getPost(Quad q);
        Fact getInitial();
        Fact getFinal();
        void setPre(Quad q, Fact dff);
        void setPost(Quad q, Fact dff);
        void setInitial(Fact dff);
        void setFinal(Fact dff);
    }

    interface Fact {
        Fact meetWith(Fact f);
        Fact deepCopy();
    }

    interface Transfer {
        void registerFactCollection(FactCollection fc);
        void preprocess(ControlFlowGraph cfg);
        boolean transfer(Quad q);
        void postprocess(ControlFlowGraph cfg);
    }

    static class DataflowArray implements FactCollection {
        private Fact[] _pre, _post;
        private Fact _initial, _final;

        public DataflowArray(int size) {
            _pre = new Fact[size];
            _post = new Fact[size];
            _initial = null;
            _final = null;
        }
        
        public Fact getPre(Quad q) { return _pre[q.getID()]; }
        public Fact getPost(Quad q) { return _post[q.getID()]; }
        public Fact getInitial() { return _initial; }
        public Fact getFinal() { return _final; }
        public void setPre(Quad q, Fact dff) { _pre[q.getID()] = dff; }
        public void setPost(Quad q, Fact dff) { _post[q.getID()] = dff; }
        public void setInitial(Fact dff) { _initial = dff; }
        public void setFinal(Fact dff) { _final = dff; }
    }

    static class DataflowHash implements FactCollection {
        private HashMap _pre, _post;
        private Fact _initial, _final;

        public DataflowHash() {
            _pre = new HashMap();
            _post = new HashMap();
            _initial = null;
            _final = null;
        }

        public Fact getPre(Quad q) { return (Fact)(_pre.get(new Integer(q.getID()))); }
        public Fact getPost(Quad q) { return (Fact)(_post.get(new Integer(q.getID()))); }
        public Fact getInitial() { return _initial; }
        public Fact getFinal() { return _final; }
        public void setPre(Quad q, Fact dff) { _pre.put(new Integer(q.getID()), dff); }
        public void setPost(Quad q, Fact dff) { _post.put(new Integer(q.getID()), dff); }
        public void setInitial(Fact dff) { _initial = dff; }
        public void setFinal(Fact dff) { _final = dff; }
    }

    static class Intraprocedural implements ControlFlowGraphVisitor {
        private FactCollection _dfc;
        private Transfer _transfer;

        public Intraprocedural (Transfer t) { _transfer = t; _dfc = null; }

        private void determineStorageStyle(ControlFlowGraph cfg) {
            QuadIterator qi = new QuadIterator(cfg);
            int quadCount = 0;
            int quadIDMax = Integer.MIN_VALUE;
            int quadIDMin = Integer.MAX_VALUE;
            while (qi.hasNext()) {
                int qID = qi.nextQuad().getID();
                if (qID > quadIDMax) quadIDMax = qID;
                if (qID < quadIDMin) quadIDMin = qID;
                ++quadCount;
            }
            boolean useArray = (quadIDMin >= 0) || (quadIDMax <= quadCount * 2);

            if (TRACE_DATAFLOW) {
                DF_OUT.println("Number of quads: "+quadCount);
                DF_OUT.println("Minimum Quad ID: "+quadIDMin);
                DF_OUT.println("Maximum Quad ID: "+quadIDMax);
                DF_OUT.println("Using " + (useArray ? "array" : "HashMap") + " implementation.");
            }
            if (useArray)
                _dfc = new DataflowArray(quadIDMax+1);
            else
                _dfc = new DataflowHash();
            _transfer.registerFactCollection(_dfc);
        }

        public void visitCFG(ControlFlowGraph cfg) {
            determineStorageStyle(cfg);
            
            _transfer.preprocess(cfg);

            boolean changed;

            changed = true;
            while (changed) {
                if (TRACE_DATAFLOW) DF_OUT.println("Beginning a new pass.");
                QuadIterator qi = new QuadIterator(cfg);
                changed = false;
                while (qi.hasNext()) {
                    Quad q = qi.nextQuad();
                    if (TRACE_DATAFLOW_QUADS) {
                        DF_OUT.println("Analyzing Quad: "+q);
                    }

                    Fact f = _dfc.getPre(q);
                    if (f != null) {
                        Iterator pi = qi.predecessors();
                        while (pi.hasNext()) {
                            Quad p = (Quad)pi.next();
                            if (p == null) {
                                f.meetWith(_dfc.getInitial());
                            } else {
                                f.meetWith(_dfc.getPost(p));
                            }
                        }
                    }

                    if (_transfer.transfer(q))
                        changed = true;
                }
            }
            if (TRACE_DATAFLOW) DF_OUT.println("No changes, postprocessing...");
            _transfer.postprocess(cfg);
            if (TRACE_DATAFLOW) DF_OUT.println("Method complete.");
        }
    }

    public static class EmptyAnalysis extends jq_MethodVisitor.EmptyVisitor
                                      implements Transfer {
        ControlFlowGraphVisitor.CodeCacheVisitor ccv;

        protected FactCollection _fc;

        public void registerFactCollection(FactCollection fc) { _fc = fc; }

        public void preprocess(ControlFlowGraph cfg) { }
        public boolean transfer(Quad q) { return false; }
        public void postprocess(ControlFlowGraph cfg) { }

        public EmptyAnalysis() {
            ccv = new ControlFlowGraphVisitor.CodeCacheVisitor(new Intraprocedural(this));
        }
        
        public void visitMethod (jq_Method m) {
            if (TRACE_DATAFLOW) DF_OUT.println("Analyzing method: "+m.getName());
            ccv.visitMethod(m);
        }
    }
}
