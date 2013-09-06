/*
 * Created on Oct 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package joeq.Compiler.Analysis.IPSSA;

import java.util.Set;
import java.io.PrintStream;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.Quad;

 /**
 * @author V.Benjamin Livshits
 */
public interface DominatorQuery {
    /** The result is null for the top node of the CFG. */
    public Quad getImmediateDominator(Quad q);
    /** Checks if the node is the top node of the CFG. */
    public boolean isTop(Quad q);
    /** Fills set with the dominance frontier of q */
    public void getDominanceFrontier(Quad q, Set/*<Quad>*/ set);
    /** Fills set with the iterated dominance frontier of q */
    public void getIteratedDominanceFrontier(Quad q, Set/*<Quad>*/ set);
    /** Prints the dominator tree on Quads in dot format. */    
    public void printDot(PrintStream out);
    public BasicBlock getBasicBlock(Quad quad); 
};

