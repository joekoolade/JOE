package joeq.Compiler.Analysis.IPSSA.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.PrintStream;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Analysis.IPSSA.DominatorQuery;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.Dominators;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.Dominators.DominatorNode;
import joeq.Util.SyntheticGraphs.Graph;
import jwutil.util.Assert;

/**
 * A pretty obvious implementation of DominatorQuery, nothing fancy here. 
 * Needs to be optimized for future use.
 * @see DominatorQuery
 * @author  V.Benjamin Livshits
 * @version $Id: SimpleDominatorQuery.java,v 1.7 2004/09/22 22:17:25 joewhaley Exp $
 * */
public class SimpleDominatorQuery implements DominatorQuery {
    private jq_Method _m;    
    private ControlFlowGraph _cfg;

    // Maps we create to answer the queries
    private HashMap _bb2nodeMap;    
    private HashMap _quad2BBMap;
    private boolean _verbose = false;

    public SimpleDominatorQuery(jq_Method m){
        this._m = m;
        this._cfg = CodeCache.getCode(m);
        
        // build BB-level dominators
        Dominators dom = new Dominators(true);
        dom.visitMethod(m);
        DominatorNode root = dom.computeTree();
        
        // create lookup maps
        _bb2nodeMap = new HashMap();
        buildBB2NodeMap(root, _bb2nodeMap);
        
        _quad2BBMap = new HashMap();
        buildQuad2BBMap(_quad2BBMap);
        
        //System.out.println("_bb2nodeMap: " + _bb2nodeMap.size() + ", _quad2BBMap: " + _quad2BBMap.size() + "\n");
    }
        
    private void buildBB2NodeMap(DominatorNode root, HashMap map) {
        BasicBlock bb = root.getBasicBlock();
        Assert._assert(bb != null);
        map.put(bb, root);        
        
        List children = root. getChildren();
        //System.out.println(children.size() + " children \n");
        for(Iterator i = children.iterator(); i.hasNext();){
            DominatorNode child = (DominatorNode)i.next();
            
            buildBB2NodeMap(child, map);
        }
    }
    
    private void buildQuad2BBMap(final HashMap map) {
        _cfg.visitBasicBlocks(new BasicBlockVisitor(){
            public void visitBasicBlock(BasicBlock bb){
                //System.out.println("Block " + bb.toString() + "\n");
                Quad lastQuad = bb.getLastQuad();
                if(lastQuad == null) return;
                for(int i = 0; i <= bb.getQuadIndex(lastQuad); i++){
                    Quad q = (Quad)bb.getQuad(i);
                    
                    map.put(q, bb);
                }    
            }
        });
    }

    public Quad getImmediateDominator(Quad q){
        BasicBlock bb = (BasicBlock)_quad2BBMap.get(q);
        Assert._assert(bb != null);
        
        int pos = bb.getQuadIndex(q);
        if(pos > 0){
            return bb.getQuad(pos - 1);
        }else{
            DominatorNode node = (DominatorNode)_bb2nodeMap.get(bb);
            Assert._assert(node != null);
            
            DominatorNode dom = node.getParent();
            if(dom != null){
                return dom.getBasicBlock().getLastQuad();     
            }else{
                return null;
            }
        }
    }
    
    public boolean isTop(Quad q){
        return getImmediateDominator(q) == null;
    }
    
    public void getDominanceFrontier(Quad q, Set/*<Quad>*/ set){
        /*
         * The idea is to get a dominance frontier from the dominance tree.
         * DF(n) = {m | n dom pred(m), but n ~dom m}, so, there's a dominated predecessor. 
         * */
        Assert._assert(set != null);     
        DominatorNode node = getNode(q);
        processChildren(node.getBasicBlock(), node, set);
        if(_verbose){
            System.err.println("Dominance frontier for " + q.toString_short() + " is: ");
            Iterator iter = set.iterator();
            while(iter.hasNext()){
                Quad dom = (Quad) iter.next();
                //Assert._assert(q != dom, "Didn't expect " + q.toString() + "@ " + 
                //    new QuadProgramLocation(this._m, q).toStringLong() + " to be on its own dom. frontier");
                System.err.print(dom.toString() + " ");
            }
            System.err.println("");
        }
    }
    
    /**
     * Collect nodes on the dominance frontier of root into set. Predecessors of node 
     * are considered for the dominance frontier. 
     * */
    private void processChildren(BasicBlock root, DominatorNode node, Set/*<Quad>*/ set) {
        BasicBlock bb = node.getBasicBlock();
        if(bb.size() == 0) return;
        
        joeq.Util.Templates.ListIterator.BasicBlock successors = bb.getSuccessors().basicBlockIterator();
        while(successors.hasNext()){
            BasicBlock succ = successors.nextBasicBlock();
            
            Assert._assert(dominates(root, root));
            Assert._assert(dominates(succ, succ));
            if(!dominates(root, succ, true)){
                set.add(succ.getQuad(0));
                // AT LEAST one predecessor is not dominated -- no need to look at the others
                break;
            }
        }
        for(Iterator iter = node.getChildren().iterator(); iter.hasNext(); ){
            DominatorNode child = (DominatorNode)iter.next();
            Assert._assert(child != node);
            
            // only the first node of a BB is suspect
            processChildren(root, child, set);                                    
        }    
    }

    /**
     * The default is non-strict dominance.
     **/
    private boolean dominates(BasicBlock root, BasicBlock pred) {
        return dominates(root, pred, false);
    }

    /**
     * Check for dominance.
     * */
    private boolean dominates(BasicBlock root, BasicBlock pred, boolean strict) {
        DominatorNode root_node = (DominatorNode) _bb2nodeMap.get(root);
        DominatorNode node         = (DominatorNode) _bb2nodeMap.get(pred);    
        
        if(!strict && root == pred){
            // nodes non-strictly dominates itself
            return true;
        }
        
        while(node != null){
            node = node.getParent();
            if(node == root_node){
                return true;
            }
        }
        
        return false;
    }

    private DominatorNode getNode(Quad q) {
        BasicBlock bb = (BasicBlock)_quad2BBMap.get(q);
        Assert._assert(bb != null, "No matching basic block for " + q);
        DominatorNode node = (DominatorNode)_bb2nodeMap.get(bb);
        Assert._assert(node != null);

        return node;
    }

    public void getIteratedDominanceFrontier(Quad q, Set/*<Quad>*/ set){
        getDominanceFrontier(q, set);
        
        boolean change = false;
        int i = 1;
        do {
            change = false;
            //System.err.println("Iteration " + i);
            for(Iterator iter = set.iterator(); iter.hasNext(); ){
                Quad domFrontierQuad = (Quad)iter.next();
                //Assert._assert(q != domFrontierQuad, "Didn't expect " + q + " to be on its own dom. frontier");
                
                int oldSetSize = set.size();                
                getDominanceFrontier(domFrontierQuad, set);                
                Assert._assert(set.size() >= oldSetSize);
                
                if(set.size() != oldSetSize){
                    // change detected
                    change = true;
                }
            }
            i++;
        } while (change);
    }
        
    /**
     * Prints the dominator tree on Quads in dot format.
     * */
    public void printDot(PrintStream out){
        Graph g = new Graph(_m.toString(), new Graph.Direction(Graph.Direction.LR));
        for(Iterator iter = new QuadIterator(_cfg); iter.hasNext();){
            Quad q = (Quad)iter.next();
            
            // these IDs should be unique, I hope
            ProgramLocation loc = new QuadProgramLocation(_m, q);
            String src_loc = loc.getSourceFile().toString() + ":" + loc.getLineNumber();
            g.addNode(q.getID(), q.toString_short() + "\\l" + src_loc);
            Quad dom = getImmediateDominator(q);
            if(dom != null){
                g.addNode(dom.getID(), dom.toString_short());
                g.addEdge(q.getID(), dom.getID());
            }
        }
        
        // graph creation is complete
        g.printDot(out);
    }
    
    public static class TestSimpleDominatorQuery implements ControlFlowGraphVisitor {
        public TestSimpleDominatorQuery(){
            CodeCache.AlwaysMap = true;
        }
        
        public void visitCFG(ControlFlowGraph cfg) {
            SimpleDominatorQuery q = new SimpleDominatorQuery(cfg.getMethod());
            q.printDot(System.out);    
        }
        
        public static void Main(String argv[]){
            for(int i = 0; i < argv.length; i++){
                String arg = argv[i];
                
                if(arg == "-v"){
                    // TOOD
                }
            }
        }
    }

    public BasicBlock getBasicBlock(Quad quad) {
        return (BasicBlock) _quad2BBMap.get(quad);
    }
};

