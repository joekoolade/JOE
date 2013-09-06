// LivenessAnalysis.java, created Jun 15, 2003 2:10:14 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.HostedVM;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;
import jwutil.graphs.EdgeGraph;
import jwutil.graphs.Graph;
import jwutil.graphs.ReverseGraph;
import jwutil.math.BitString;
import jwutil.util.Assert;

/**
 * LivenessAnalysis
 * 
 * @author John Whaley
 * @version $Id: LivenessAnalysis.java,v 1.5 2004/09/22 22:17:26 joewhaley Exp $
 */
public class LivenessAnalysis extends Problem {

    Map transferFunctions;
    Fact emptySet;
    TransferFunction emptyTF;

    Solver mySolver;
    
    static final boolean TRACE = false;

    public void initialize(Graph g) {
        Graph g2 = ((EdgeGraph) g).getGraph();
        ControlFlowGraph cfg = (ControlFlowGraph) ((ReverseGraph) g2).getGraph();
        
        if (TRACE) System.out.println("Initializing LivenessAnalysis.");
        if (TRACE) System.out.println(cfg.fullDump());
        
        // size of bit vector is bounded by the number of registers.
        int bitVectorSize = cfg.getRegisterFactory().size() + 1;
        
        if (TRACE) System.out.println("Bit vector size: "+bitVectorSize);
        
        Map regToDefs = new HashMap();
        transferFunctions = new HashMap();
        emptySet = new UnionBitVectorFact(bitVectorSize);
        emptyTF = new GenKillTransferFunction(bitVectorSize);
        
        List.BasicBlock list = cfg.reversePostOrder(cfg.entry());
        for (ListIterator.BasicBlock i = list.basicBlockIterator(); i.hasNext(); ) {
            BasicBlock bb = i.nextBasicBlock();
            BitString gen = new BitString(bitVectorSize);
            BitString kill = new BitString(bitVectorSize);
            for (ListIterator.Quad j = bb.backwardIterator(); j.hasNext(); ) {
                Quad q = j.nextQuad();
                for (ListIterator.RegisterOperand k = q.getDefinedRegisters().registerOperandIterator(); k.hasNext(); ) {
                    Register r = k.nextRegisterOperand().getRegister();
                    int index = r.getNumber() + 1;
                    kill.set(index);
                    gen.clear(index);
                }
                for (ListIterator.RegisterOperand k = q.getUsedRegisters().registerOperandIterator(); k.hasNext(); ) {
                    Register r = k.nextRegisterOperand().getRegister();
                    int index = r.getNumber() + 1;
                    gen.set(index);
                }
            }
            GenKillTransferFunction tf = new GenKillTransferFunction(gen, new BitString(bitVectorSize));
            tf.gen.or(gen);
            tf.kill.or(kill);
            Assert._assert(!transferFunctions.containsKey(bb));
            transferFunctions.put(bb, tf);
        }
        if (TRACE) {
            System.out.println("Transfer functions:");
            for (Iterator i = transferFunctions.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                System.out.println(e.getKey());
                System.out.println(e.getValue());
            }
        }
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Problem#direction()
     */
    public boolean direction() {
        return false;
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
        Problem p = new LivenessAnalysis();
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
    
    public static LivenessAnalysis solve(ControlFlowGraph cfg) {
        LivenessAnalysis p = new LivenessAnalysis();
        Solver s1 = new IterativeSolver();
        p.mySolver = s1;
        solve(cfg, s1, p);
        if (TRACE) {
            System.out.println("Finished solving Liveness.");
            //Solver.dumpResults(cfg, s1);
        }
        return p;
    }
    
    private static void solve(ControlFlowGraph cfg, Solver s, Problem p) {
        s.initialize(p, new EdgeGraph(new ReverseGraph(cfg, Collections.singleton(cfg.exit()))));
        s.solve();
    }

    public boolean isLiveAtOut(BasicBlock bb, Register r) {
        if (bb.getNumberOfSuccessors() > 0)
            bb = bb.getSuccessors().getBasicBlock(0);
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        if (f == null) throw new RuntimeException(bb.toString()+" reg "+r);
        return f.fact.get(r.getNumber()+1);
    }
    
    public boolean isLiveAtIn(BasicBlock bb, Register r) {
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        return f.fact.get(r.getNumber()+1);
    }
    
    public void setLiveAtIn(BasicBlock bb, Register r) {
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        f.fact.set(r.getNumber()+1);
        GenKillTransferFunction tf = (GenKillTransferFunction) transferFunctions.get(bb);
        tf.gen.set(r.getNumber()+1);
    }
    
    public void setKilledAtIn(BasicBlock bb, Register r) {
        BitVectorFact f = (BitVectorFact) mySolver.getDataflowValue(bb);
        f.fact.clear(r.getNumber()+1);
        GenKillTransferFunction tf = (GenKillTransferFunction) transferFunctions.get(bb);
        tf.kill.set(r.getNumber()+1);
    }
}
