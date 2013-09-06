// UnionBitVectorFact.java, created Mar 22, 2004 2:10:20 PM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import jwutil.math.BitString;

/**
 * UnionBitVectorFact
 * 
 * @author jwhaley
 * @version $Id: UnionBitVectorFact.java,v 1.3 2004/09/22 22:17:26 joewhaley Exp $
 */
public class UnionBitVectorFact extends BitVectorFact {

    protected UnionBitVectorFact(int size) {
        super(size);
    }
    
    protected UnionBitVectorFact(BitString s) {
        super(s);
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.Fact#merge(joeq.Compiler.Dataflow.Fact)
     */
    public Fact merge(Fact that) {
        BitVectorFact r = (BitVectorFact) that;
        BitString s = new BitString(this.fact.size());
        s.or(this.fact);
        boolean b = s.or(r.fact);
        if (!b) return this;
        else return new UnionBitVectorFact(s);
    }
    
    public BitVectorFact makeNew(BitString s) {
        return new UnionBitVectorFact(s);
    }
}
