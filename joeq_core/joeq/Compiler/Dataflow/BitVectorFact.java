// BitVectorFact.java, created Mar 22, 2004 2:09:27 PM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import jwutil.math.BitString;

public abstract class BitVectorFact implements Fact {

    protected final BitString fact;

    protected BitVectorFact(int size) {
        this.fact = new BitString(size);
    }
    
    protected BitVectorFact(BitString s) {
        this.fact = s;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Fact#merge(Compiler.Dataflow.Fact)
     */
    public abstract Fact merge(Fact that);
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Fact#equals(Compiler.Dataflow.Fact)
     */
    public boolean equals(Fact that) {
        return this.fact.equals(((BitVectorFact) that).fact);
    }
    
    public String toString() {
        return fact.toString();
    }
    
    public abstract BitVectorFact makeNew(BitString s);
    
}