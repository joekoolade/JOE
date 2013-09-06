package joeq.Compiler.Analysis.IPA;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.io.IOException;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.LoadedCallGraph;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Main.Helper;
import jwutil.graphs.SCCTopSortedGraph;
import jwutil.graphs.SCComponent;
import jwutil.graphs.Traversals;
import jwutil.util.Assert;

/**
 * @author jwhaley
 * @version $Id: LoopAnalysis.java,v 1.5 2004/09/22 22:17:30 joewhaley Exp $
 */
public class LoopAnalysis implements ControlFlowGraphVisitor {

    public static void main(String[] args) throws IOException {
        jq_Class c = (jq_Class) Helper.load(args[0]);
        CodeCache.AlwaysMap = true;
        CallGraph cg = new LoadedCallGraph("callgraph");
        LoopAnalysis a = new LoopAnalysis(cg);
        Helper.runPass(c, a);
        System.out.println("Visited methods: "+a.visitedMethods);
        System.out.println("Loop methods: "+a.loopMethods);
        System.out.println("Loop BB: "+a.loopBB);
    }

    CallGraph cg;
    jq_Method caller;
    Set visitedMethods = new HashSet();
    Set loopMethods = new HashSet();
    Set loopBB = new HashSet();

    public LoopAnalysis() {
    }
    
    public LoopAnalysis(CallGraph cg) {
        this.cg = cg;
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.ControlFlowGraphVisitor#visitCFG(joeq.Compiler.Quad.ControlFlowGraph)
     */
    public void visitCFG(ControlFlowGraph cfg) {
        caller = cfg.getMethod();
        if (visitedMethods.contains(caller))
            return;
        visitedMethods.add(caller);
        
        // Find SCCs.
        Set roots = SCComponent.buildSCC(cfg);
        SCCTopSortedGraph g = SCCTopSortedGraph.topSort(roots);
        
        // Find loops.
        for (Iterator i = Traversals.reversePostOrder(g.getNavigator(), roots).iterator();
             i.hasNext(); ) {
            SCComponent scc = (SCComponent) i.next();
            if (scc.isLoop()) {
                for (Iterator j = scc.nodeSet().iterator(); j.hasNext(); ) {
                    BasicBlock bb = (BasicBlock) j.next();
                    loopBB.add(bb);
                    if (cg != null)
                        bb.visitQuads(invoke_visitor);
                }
            }
        }
    }
    
    public boolean isInLoop(jq_Method m, BasicBlock bb) {
        if (loopMethods.contains(m)) return true;
        if (!visitedMethods.contains(m)) {
            visitCFG(CodeCache.getCode(m));
            if (loopMethods.contains(m)) return true;
        }
        if (loopBB.contains(bb)) return true;
        return false;
    }
    
    InvokeVisitor invoke_visitor = new InvokeVisitor();
    public class InvokeVisitor extends QuadVisitor.EmptyVisitor {
        public void visitInvoke(Quad q) {
            super.visitInvoke(q);
            Assert._assert(caller != null);
            Assert._assert(q != null);
            ProgramLocation mc = new ProgramLocation.QuadProgramLocation(caller, q);
            LinkedList w = new LinkedList();
            w.add(mc);
            while (!w.isEmpty()) {
                mc = (ProgramLocation) w.removeFirst();
                Collection targets = cg.getTargetMethods(mc);
                for (Iterator i = targets.iterator(); i.hasNext(); ) {
                    jq_Method callee = (jq_Method) i.next();
                    boolean change = loopMethods.add(callee);
                    if (change) {
                        w.addAll(cg.getCallSites(callee));
                    }
                }
            }
        }
    }
}
