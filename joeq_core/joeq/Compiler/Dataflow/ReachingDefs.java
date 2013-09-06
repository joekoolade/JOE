// ReachingDefs.java, created Jun 15, 2003 2:10:14 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.HostedVM;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;
import jwutil.collections.BitStringSet;
import jwutil.collections.Pair;
import jwutil.graphs.EdgeGraph;
import jwutil.graphs.Graph;
import jwutil.math.BitString;

/**
 * ReachingDefs
 * 
 * @author John Whaley
 * @version $Id: ReachingDefs.java,v 1.9 2004/09/22 22:17:25 joewhaley Exp $
 */
public class ReachingDefs extends Problem {

    public static class RDVisitor implements ControlFlowGraphVisitor {

        public static boolean DUMP = false;
        public static int SOLVER = 1;
        
        long totalTime;
        
        /* (non-Javadoc)
         * @see joeq.Compiler.Quad.ControlFlowGraphVisitor#visitCFG(joeq.Compiler.Quad.ControlFlowGraph)
         */
        public void visitCFG(ControlFlowGraph cfg) {
            long time = System.currentTimeMillis();
            Problem p = new ReachingDefs();
            Solver s1;
            switch (SOLVER) {
                case 1:
                    s1 = new IterativeSolver();
                    break;
                case 3:
                    s1 = new PriorityQueueSolver();
                    break;
                case 2:
                default:
                    s1 = new SortedSetSolver(BBComparator.INSTANCE);
                    break;
            }
            solve(cfg, s1, p);
            time = System.currentTimeMillis() - time;
            totalTime += time;
            if (DUMP) 
                Solver.dumpResults(cfg, s1);
        }
        
        public String toString() {
            return "Total time: "+totalTime+" ms";
        }
    }
    
    Quad[] quads;
    Map regToDefs;
    Map transferFunctions;
    BitVectorFact emptySet;
    GenKillTransferFunction emptyTF;
    Solver mySolver;

    static final boolean TRACE = false;

    public void initialize(Graph g) {
        ControlFlowGraph cfg = (ControlFlowGraph) ((EdgeGraph) g).getGraph();
        
        if (TRACE) System.out.println(cfg.fullDump());
        
        // size of bit vector is bounded by the max quad id
        int bitVectorSize = cfg.getMaxQuadID()+1;
        
        if (TRACE) System.out.println("Bit vector size: "+bitVectorSize);
        
        regToDefs = new HashMap();
        transferFunctions = new HashMap();
        quads = new Quad[bitVectorSize];
        emptySet = new UnionBitVectorFact(bitVectorSize);
        emptyTF = new GenKillTransferFunction(bitVectorSize);
        
        List.BasicBlock list = cfg.reversePostOrder(cfg.entry());
        for (ListIterator.BasicBlock i = list.basicBlockIterator(); i.hasNext(); ) {
            BasicBlock bb = i.nextBasicBlock();
            BitString gen = new BitString(bitVectorSize);
            for (ListIterator.Quad j = bb.iterator(); j.hasNext(); ) {
                Quad q = j.nextQuad();
                if (!bb.getExceptionHandlers().isEmpty()) {
                    handleEdges(bb, bb.getExceptionHandlerEntries(), gen, null);
                }
                if (q.getDefinedRegisters().isEmpty()) continue;
                int a = q.getID();
                quads[a] = q;
                for (ListIterator.RegisterOperand k = q.getDefinedRegisters().registerOperandIterator(); k.hasNext(); ) {
                    Register r = k.nextRegisterOperand().getRegister();
                    BitString kill = (BitString) regToDefs.get(r);
                    if (kill == null) regToDefs.put(r, kill = new BitString(bitVectorSize));
                    else gen.minus(kill);
                    kill.set(a);
                }
                gen.set(a);
            }
            GenKillTransferFunction tf = new GenKillTransferFunction(gen, new BitString(bitVectorSize));
            handleEdges(bb, bb.getSuccessors(), gen, tf);
        }
        for (Iterator i = transferFunctions.values().iterator(); i.hasNext(); ) {
            GenKillTransferFunction f = (GenKillTransferFunction) i.next();
            for (BitString.BitStringIterator j = f.gen.iterator(); j.hasNext(); ) {
                int a = j.nextIndex();
                Quad q = quads[a];
                for (ListIterator.RegisterOperand k = q.getDefinedRegisters().registerOperandIterator(); k.hasNext(); ) {
                    Register r = k.nextRegisterOperand().getRegister();
                    f.kill.or((BitString) regToDefs.get(r));
                }
            }
        }
        if (TRACE) {
            for (Iterator i = transferFunctions.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                System.out.println(e.getKey());
                System.out.println(e.getValue());
            }
        }
    }

    private void handleEdges(BasicBlock bb, List.BasicBlock bbs, BitString gen, GenKillTransferFunction defaultTF) {
        for (ListIterator.BasicBlock k = bbs.basicBlockIterator(); k.hasNext(); ) {
            BasicBlock bb2 = k.nextBasicBlock();
            Object edge = new Pair(bb, bb2);
            GenKillTransferFunction tf = (GenKillTransferFunction) transferFunctions.get(edge);
            if (tf == null) {
                tf = (defaultTF != null)? defaultTF : new GenKillTransferFunction(gen.size());
                transferFunctions.put(edge, tf);
            }
            tf.gen.or(gen);
        }
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Problem#direction()
     */
    public boolean direction() {
        return true;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Problem#boundary()
     */
    public Fact boundary() {
        return emptySet;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Problem#interior()
     */
    public Fact interior() {
        return emptySet;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Problem#getTransferFunction(java.lang.Object)
     */
    public TransferFunction getTransferFunction(Object e) {
        TransferFunction tf = (TransferFunction) transferFunctions.get(e);
        if (tf == null) tf = emptyTF;
        return tf;
    }

    public static void main(String[] args) {
        HostedVM.initialize();
        HashSet set = new HashSet();
        for (int i=0; i<args.length; ++i) {
            String s = args[i];
            jq_Class c = (jq_Class) jq_Type.parseType(s);
            c.load();
            set.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            set.addAll(Arrays.asList(c.getDeclaredInstanceMethods()));
        }
        Problem p = new ReachingDefs();
        Solver s1 = new IterativeSolver();
        Solver s2 = new SortedSetSolver(BBComparator.INSTANCE);
        Solver s3 = new PriorityQueueSolver();
        for (Iterator i = set.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m.getBytecode() == null) continue;
            System.out.println("Method "+m);
            ControlFlowGraph cfg = CodeCache.getCode(m);
            System.out.println(cfg.fullDump());
            solve(cfg, s1, p);
            solve(cfg, s2, p);
            solve(cfg, s3, p);
            Solver.dumpResults(cfg, s1);
            Solver.compareResults(cfg, s1, s2);
            Solver.compareResults(cfg, s2, s3);
        }
    }
    
    public static ReachingDefs solve(ControlFlowGraph cfg) {
        ReachingDefs p = new ReachingDefs();
        Solver s1 = new IterativeSolver();
        p.mySolver = s1;
        solve(cfg, s1, p);
        if (TRACE) {
            System.out.println("Finished solving ReachingDefs.");
            //Solver.dumpResults(cfg, s1);
        }
        return p;
    }
    
    private static void solve(ControlFlowGraph cfg, Solver s, Problem p) {
        s.initialize(p, new EdgeGraph(cfg));
        s.solve();
    }

    public Set/*Quad*/ getReachingDefs(BasicBlock bb) {
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        return new BitStringSet(f.fact, Arrays.asList(quads));
    }
    
    public Set/*Quad*/ getReachingDefs(BasicBlock bb, Register r) {
        BitString b = (BitString) regToDefs.get(r);
        if (b == null) return Collections.EMPTY_SET;
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        BitString result = (BitString) b.clone();
        result.and(f.fact);
        return new BitStringSet(result, Arrays.asList(quads));
    }
    
    public Set/*Quad*/ getReachingDefs(BasicBlock bb, Quad q) {
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        BitString result = (BitString) f.fact.clone();
        withinBasicBlock(result, bb, q);
        return new BitStringSet(result, Arrays.asList(quads));
    }
    
    public Set/*Quad*/ getReachingDefs(BasicBlock bb, Quad q, Register r) {
        BitString b = (BitString) regToDefs.get(r);
        if (b == null) return Collections.EMPTY_SET;
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        BitString result = (BitString) f.fact.clone();
        withinBasicBlock(result, bb, q);
        result.and(b);
        return new BitStringSet(result, Arrays.asList(quads));
    }
    
    void withinBasicBlock(BitString bs, BasicBlock bb, Quad q2) {
        for (ListIterator.Quad j = bb.iterator(); ; ) {
            Quad q = j.nextQuad();
            if (q == q2) break;
            if (q.getDefinedRegisters().isEmpty()) continue;
            int a = q.getID();
            for (ListIterator.RegisterOperand k = q.getDefinedRegisters().registerOperandIterator(); k.hasNext(); ) {
                Register r = k.nextRegisterOperand().getRegister();
                BitString kill = (BitString) regToDefs.get(r);
                bs.minus(kill);
            }
            bs.set(a);
        }
    }
    
}
