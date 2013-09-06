// GenKillTransferFunction.java, created Mar 22, 2004 2:15:05 PM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import jwutil.math.BitString;
import jwutil.strings.Strings;

public class GenKillTransferFunction implements TransferFunction {

    protected final BitString gen, kill;

    GenKillTransferFunction(int size) {
        this.gen = new BitString(size);
        this.kill = new BitString(size);
    }
    GenKillTransferFunction(BitString g, BitString k) {
        this.gen = g; this.kill = k;
    }

    public String toString() {
        return "   Gen: "+gen+Strings.lineSep+"   Kill: "+kill;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Dataflow.TransferFunction#apply(joeq.Compiler.Dataflow.Fact)
     */
    public Fact apply(Fact f) {
        BitVectorFact r = (BitVectorFact) f;
        BitString s = new BitString(r.fact.size());
        s.or(r.fact);
        s.minus(kill);
        s.or(gen);
        return r.makeNew(s);
    }
    
}