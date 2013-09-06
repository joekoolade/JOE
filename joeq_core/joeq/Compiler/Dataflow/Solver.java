// Solver.java, created Thu Apr 25 16:32:26 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import java.util.Iterator;
import java.util.Map;
import jwutil.collections.MapFactory;
import jwutil.graphs.Graph;
import jwutil.graphs.Traversals;

/**
 * Solver
 * 
 * @author John Whaley
 * @version $Id: Solver.java,v 1.5 2004/09/22 22:17:26 joewhaley Exp $
 */
public abstract class Solver {

    static final boolean TRACE = false;
    
    /** The dataflow problem to solve. */
    protected Problem problem;
    /** Map factory to create map from locations to dataflow values. */
    protected final MapFactory factory;
    /** Map from locations to dataflow values. */
    protected Map dataflowValues;

    protected Solver(MapFactory factory) {
        this.factory = factory;
    }
    protected Solver() {
        this(MapFactory.hashMapFactory);
    }
    
    /** Returns the direction of the dataflow problem that we are solving. */
    public boolean direction() { return problem.direction(); }
    
    /** Returns an iteration of all graph locations. */
    public abstract Iterator allLocations();
    /** Returns an iteration of all boundary locations. */
    public abstract Iterator boundaryLocations();
    
    /** Initializes the solver to prepare to solve the dataflow
     * problem on the given graph.
     */
    public void initialize(Problem p, Graph graph) {
        this.problem = p;
        p.initialize(graph);
    }
    
    /** Solves this dataflow problem. */
    public abstract void solve();
    
    /** Frees the memory associated with this solver. */
    public void reset() {
        this.dataflowValues = null;
    }
    
    /** (Re-)initialize the map from locations to dataflow values. */
    protected void initializeDataflowValueMap() {
        dataflowValues = factory.makeMap();
        for (Iterator i = allLocations(); i.hasNext(); ) {
            Object c = i.next();
            dataflowValues.put(c, problem.interior());
        }
        for (Iterator i = boundaryLocations(); i.hasNext(); ) {
            Object c = i.next();
            dataflowValues.put(c, problem.boundary());
        }
    }

    /** Get the dataflow value associated with the given location. */
    public Fact getDataflowValue(Object c) {
        return (Fact) dataflowValues.get(c); 
    }
    
    public static void dumpResults(Graph g, Solver s) {
        System.out.println("RESULTS");
        for (Iterator i = Traversals.reversePostOrder(g.getNavigator(), g.getRoots()).iterator(); i.hasNext(); ) {
            Object bb = i.next();
            Fact r = s.getDataflowValue(bb);
            System.out.println(bb+": "+r);
       }
    }
    
    public static void compareResults(Graph g, Solver s1, Solver s2) {
        for (Iterator i = Traversals.reversePostOrder(g.getNavigator(), g.getRoots()).iterator(); i.hasNext(); ) {
            Object bb = i.next();
            Fact r1 = s1.getDataflowValue(bb);
            Fact r2 = s2.getDataflowValue(bb);
            if (!r1.equals(r2)) {
                System.out.println("MISMATCH");
                System.out.println(s1.getClass()+" says "+r1);
                System.out.println(s2.getClass()+" says "+r2);
            }
        }
    }

}
