// IterativeSolver.java, created Thu Apr 25 16:32:26 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import jwutil.collections.MapFactory;
import jwutil.graphs.Graph;
import jwutil.graphs.Navigator;
import jwutil.graphs.Traversals;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * Solves a dataflow problem using a iterative technique.  Successively
 * iterates over the locations in the graph in a given order until there
 * are no more changes.
 * 
 * @author John Whaley
 * @version $Id: IterativeSolver.java,v 1.6 2004/09/22 22:17:26 joewhaley Exp $
 */
public class IterativeSolver
    extends Solver
{
    /** The order in which the locations are to be traversed. */
    protected List traversalOrder;
    /** The boundary locations. */
    protected Collection boundaries;
    /** Navigator to navigate the graph of locations. */
    protected Navigator graphNavigator;
    /** Change flag, set to true if we need to iterate more times. */
    protected boolean change;

    public IterativeSolver(MapFactory f) {
        super(f);
    }
    public IterativeSolver() {
        super();
    }

    /** Returns an iteration of the order in which the locations are to be traversed. */
    protected Iterator getTraversalOrder() { return traversalOrder.iterator(); }
    
    /** Get the predecessor locations of the given location. */
    protected Collection getPredecessors(Object c) { return graphNavigator.prev(c); }
    /** Get the successor locations of the given location. */
    protected Collection getSuccessors(Object c) { return graphNavigator.next(c); }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Solver#initialize(Compiler.Dataflow.Problem, Util.Graphs.Graph)
     */
    public void initialize(Problem p, Graph graph) {
        List order = Traversals.reversePostOrder(graph.getNavigator(), graph.getRoots());
        this.initialize(p, graph, order);
    }
    
    /**
     * Initializes this solver with the given dataflow problem, graph, and
     * traversal order.
     * 
     * @see joeq.Compiler.Dataflow.Solver#initialize(joeq.Compiler.Dataflow.Problem, jwutil.graphs.Graph)
     */
    public void initialize(Problem p, Graph graph, List order) {
        super.initialize(p, graph);
        graphNavigator = graph.getNavigator();
        boundaries = graph.getRoots();
        traversalOrder = order;
        if (TRACE) System.out.println("Traversal order: "+traversalOrder);
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Solver#allLocations()
     */
    public Iterator allLocations() { return traversalOrder.iterator(); }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Solver#boundaryLocations()
     */
    public Iterator boundaryLocations() { return boundaries.iterator(); }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Solver#solve()
     */
    public void solve() {
        initializeDataflowValueMap();
        int iterationCount = 0;
        do {
            change = false; if (TRACE) ++iterationCount;
            Iterator i = getTraversalOrder();
            Object o = i.next(); // skip boundary node.
            Assert._assert(boundaries.contains(o));
            while (i.hasNext()) {
                Object c = i.next();
                if (TRACE) System.out.println("Node "+c);
                Iterator j = getPredecessors(c).iterator();
                Object p = j.next();
                if (TRACE) System.out.println("  Predecessor "+p);
                Fact in = (Fact) dataflowValues.get(p);
                while (j.hasNext()) {
                    p = j.next();
                    if (TRACE) System.out.println("  Predecessor "+p);
                    Fact in2 = (Fact) dataflowValues.get(p);
                    in = problem.merge(in, in2);
                }
                if (TRACE) System.out.println(" In set: "+in);
                TransferFunction tf = problem.getTransferFunction(c);
                if (TRACE) System.out.println(" Transfer function:"+Strings.lineSep+tf);
                Fact out = problem.apply(tf, in);
                if (TRACE) System.out.println(" Out set: "+out);
                Fact old = (Fact) dataflowValues.put(c, out);
                if (!change && !problem.compare(old, out)) {
                    change = true;
                    if (TRACE) System.out.println("Changed occurred!");
                }
            }
        } while (change);
        if (TRACE) System.out.println("Number of iterations: "+iterationCount);
    }

}
