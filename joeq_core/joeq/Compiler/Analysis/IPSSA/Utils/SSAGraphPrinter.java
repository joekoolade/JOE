/*
 * Created on Dec 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPSSA.Utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintStream;
import joeq.Compiler.Analysis.IPSSA.SSADefinition;
import joeq.Compiler.Analysis.IPSSA.SSAIterator;
import joeq.Compiler.Analysis.IPSSA.SSAValue;
import jwutil.collections.Pair;
import jwutil.util.Assert;

/**
 * @author V.Benjamin Livshits
 * @version $Id: SSAGraphPrinter.java,v 1.8 2004/09/22 22:17:25 joewhaley Exp $
 *
 * This class provides utilities for printing a SSA connectivity graph.
 */
public class SSAGraphPrinter {
    private static void collectReachedDefinitions(SSADefinition def, DefinitionSet defs) {
        if(defs.contains(def)) {
            // already seen def
            return;
        }
        
        defs.add(def);
        for(Iterator useIter = def.getUseIterator(); useIter.hasNext();) {
            SSAValue value = (SSAValue) useIter.next();
            
            SSADefinition lhsDef = value.getDestination();
            Assert._assert(lhsDef != null);
            // recurse on lhsDef
            collectReachedDefinitions(lhsDef, defs);
        }
    }
    
    private static void printDefinitions(DefinitionSet defs, PrintStream out) {
        printDefinitions(defs.getDefinitionIterator(), out);
    }
    
    private static void printDefinitions(SSAIterator.DefinitionIterator iter, PrintStream out, boolean skipIsolated) {
        // 2) Dump them as nodes
        Set edges = new HashSet();
        out.println("digraph G {\n");
        while(iter.hasNext()) {
            SSADefinition currentDef = iter.nextDefinition();
            Assert._assert(currentDef != null);
            if(!skipIsolated) {
                // always declare the nodes to get them printed
                out.println("\t\""+currentDef + "\" [shape=box];");
            }
            
            SSAIterator.ValueIterator useIter = currentDef.getUseIterator();
            if(!currentDef.getUseIterator().hasNext()) {
                //System.err.println("Definition " + currentDef + " has no uses");
                continue;
            }else {
                //System.err.println("Definition " + currentDef + " has uses");
            }
                        
            do {
                SSAValue value = useIter.nextValue();            
                SSADefinition lhsDef = value.getDestination();
                Assert._assert(lhsDef != null, "Destination of " + value + " is null");
                // add edge def -> lhsDef
                if(currentDef.getID() < lhsDef.getID()) {
                    edges.add(new Pair(currentDef, lhsDef));
                }else {
                    edges.add(new Pair(lhsDef, currentDef));
                }
            } while(useIter.hasNext());            
        }
        out.println("\n");
                
        // 3) Dump the edges
        for(Iterator edgeIter = edges.iterator(); edgeIter.hasNext();) {
            Pair pair = (Pair) edgeIter.next();
            SSADefinition currentDef = (SSADefinition) pair.left;
            SSADefinition lhsDef     = (SSADefinition) pair.right;
                    
            Assert._assert(currentDef != null && lhsDef != null);
            // add edge def -> lhsDef
            out.println("\t\"" + currentDef + "\" -> \"" + lhsDef + "\" \n" ); 
        }
        out.println("}\n");        
    }
    private static void printDefinitions(SSAIterator.DefinitionIterator iter, PrintStream out) {
        printDefinitions(iter, out, true);
    }

    public static void printToDot(DefinitionSet defs, PrintStream out) {
        DefinitionSet reachedDefs = new DefinitionSet();
        for(SSAIterator.DefinitionIterator iter = defs.getDefinitionIterator(); iter.hasNext(); ) {
            SSADefinition def = iter.nextDefinition();
        
            collectReachedDefinitions(def, reachedDefs);    
        }
        
        printDefinitions(reachedDefs, out);
    }
    
    /**
     * Print everything reachable from definition def.
     * */
    public static void printToDot(SSADefinition def, PrintStream out) {
        // 1) Collect all reached definitions by doing a DFS on the graph
        DefinitionSet defs = new DefinitionSet();
        collectReachedDefinitions(def, defs);
        
        printDefinitions(defs, out);
    }
    
    /**
     * Print the complete graph on all definitions.
     * */
    public static void printAllToDot(PrintStream out) {
        printDefinitions(SSADefinition.Helper.getAllDefinitionIterator(), out);
    }
}
