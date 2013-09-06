// Problem.java, created Thu Apr 25 16:32:26 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import jwutil.graphs.Graph;

/**
 * Problem
 * 
 * @author John Whaley
 * @version $Id: Problem.java,v 1.5 2004/09/22 22:17:26 joewhaley Exp $
 */
public abstract class Problem {

    /**
     * Performs necessary initialization for this dataflow problem.
     * 
     * @param g graph of locations that we will run over
     */
    public void initialize(Graph g) {}

    /**
     * Returns true if this is a forward dataflow problem, false if it is
     * a backward dataflow problem.
     * 
     * @return direction
     */
    public abstract boolean direction();

    /**
     * Returns the boundary value for this dataflow problem.  For a forward
     * problem, this is the value at the entrypoint, whereas for a backward problem,
     * this is the value at the exitpoint.
     * 
     * @return boundary value
     */
    public abstract Fact boundary();

    /**
     * Returns the value that the interior points should be initialized to.
     * 
     * @return value for interior points
     */
    public abstract Fact interior();
    
    /**
     * Returns the transfer function for the given code element.
     * 
     * @param e code element
     * @return transfer function for the given code element
     */
    public abstract TransferFunction getTransferFunction(Object e);
    
    /**
     * Applies the transfer function to the given dataflow value, yielding
     * another dataflow value.
     * 
     * @param tf transfer function
     * @param f dataflow value
     * @return resulting dataflow value
     */
    public Fact apply(TransferFunction tf, Fact f) {
        return tf.apply(f);
    }
    
    /**
     * Compares two dataflow facts, returning true if they are equal and false
     * otherwise.
     * 
     * @param f1 first fact
     * @param f2 second fact
     * @return true if they match, false otherwise
     */
    public boolean compare(Fact f1, Fact f2) {
        return f1.equals(f2);
    }
    
    /**
     * Combines two dataflow values, returning a new value that is the confluence
     * of the two.
     * 
     * @param f1 first fact
     * @param f2 second fact
     * @return confluence of the two facts
     */
    public Fact merge(Fact f1, Fact f2) {
        return f1.merge(f2);
    }
    
    /**
     * Returns the composition of two transfer functions.  The default implementation
     * simply returns a transfer function that applies each of the transfer functions
     * in turn.
     * 
     * @param tf1 first transfer function
     * @param tf2 second transfer function
     * @return composed transfer function
     */
    public TransferFunction compose(TransferFunction tf1, TransferFunction tf2) {
        final TransferFunction t1 = tf1;
        final TransferFunction t2 = tf2;
        return new TransferFunction() {
            public Fact apply(Fact f) {
                Fact f1 = Problem.this.apply(t1, f);
                Fact f2 = Problem.this.apply(t2, f1);
                return f2;
            }
        };
    }
    
    /**
     * Returns the closure of the given transfer function.  The closure is a
     * transfer function that is equivalent to composing the given transfer function
     * with itself repeatedly until the value stablizes.  (A monotone lattice with
     * finite descending chains guarantees this.)
     * 
     * @param tf
     * @return  transfer function representing the closure
     */
    public TransferFunction closure(TransferFunction tf) {
        final TransferFunction t = tf;
        return new TransferFunction() {
            public Fact apply(Fact f) {
                for (;;) {
                    Fact f1 = Problem.this.apply(t, f);
                    if (Problem.this.compare(f, f1)) return f;
                    f = f1;
                }
            }
        };
    }

}
