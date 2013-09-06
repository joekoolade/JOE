// Code2HeapReference.java, created Tue Feb 27  2:59:43 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Assembler;

import java.io.DataOutput;
import java.io.IOException;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;

/**
 * Code2HeapReference
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Code2HeapReference.java,v 1.2 2004/09/30 03:35:29 joewhaley Exp $
 */
public class Code2HeapReference extends Reloc {

    private CodeAddress from_codeloc;
    private HeapAddress to_heaploc;
    
    /** Creates new Code2HeapReference */
    public Code2HeapReference(CodeAddress from_codeloc, HeapAddress to_heaploc) {
        this.from_codeloc = from_codeloc; this.to_heaploc = to_heaploc;
    }

    public CodeAddress getFrom() { return from_codeloc; }
    public HeapAddress getTo() { return to_heaploc; }
    
    public void patch() { from_codeloc.poke(to_heaploc); }
    
    public void dumpCOFF(DataOutput out) throws IOException {
        out.writeInt(from_codeloc.to32BitValue()); // r_vaddr
        out.writeInt(1);                           // r_symndx
        out.writeChar(Reloc.RELOC_ADDR32);         // r_type
    }
    
    public String toString() {
        return "from code:"+from_codeloc.stringRep()+" to heap:"+to_heaploc.stringRep();
    }
    
}
